package com.ibm.cmr.create.batch.util.mq.handler.impl;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.openjpa.persistence.OpenJPAEntityTransaction;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.MQProcessUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.sof.GenericSOFMessageParser;
import com.ibm.cmr.create.batch.model.RDCLegacyMQMessage;
import com.ibm.cmr.create.batch.model.SOFResponseCUDQMessage;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.TransformerManager;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.SOFServiceClient;
import com.ibm.cmr.services.client.sof.SOFQueryRequest;
import com.ibm.cmr.services.client.sof.SOFQueryResponse;

/**
 * 
 * Handler for SOF messages
 * 
 * @author Guo Feng
 * @author Jeffrey Zamora
 * 
 */
public class SOFMessageHandler extends MQMessageHandler {

  private static final Logger LOG = Logger.getLogger(SOFMessageHandler.class);

  public static final String SOF_RESPONSE_ROOT_ELEM = "softordc";

  public SOFResponseCUDQMessage rdcLegacyMQMessage = null;
  public String resXML = null;
  public boolean dataMessage;
  public boolean mainAddressUpdated;

  public List<String> shippingSequences = null;
  public List<String> countryCSequences = null;
  public List<String> installingSequences = null;

  public SOFMessageHandler(EntityManager entityManager, MqIntfReqQueue mqIntfReqQueue) {
    super(entityManager, mqIntfReqQueue);
  }

  public void setMQMessage(RDCLegacyMQMessage rdcLegacyMQMessage) {
    this.rdcLegacyMQMessage = (SOFResponseCUDQMessage) rdcLegacyMQMessage;
  }

  public void setRequestObj(MqIntfReqQueue mqIntfReqQueue) {
    this.mqIntfReqQueue = mqIntfReqQueue;
  }

  public void setResponseOjb(String resXML) {
    this.resXML = resXML;
  }

  /**
   * Processes the {@link MqIntfReqQueue} record and sets the data elements for
   * publishing to MQ
   * 
   * @param mqIntfReqQueue
   * @throws Exception
   */
  public void convertCMRToMQObject(MqIntfReqQueue mqIntfReqQueue) throws Exception {

    // determine the current send sequence
    MqIntfReqQueue reqQueue = mqIntfReqQueue;
    int lastSequence = getLastSequence(reqQueue);

    if (MQMsgConstants.REQ_STATUS_NEW.equals(reqQueue.getReqStatus())) {
      this.dataMessage = true;
    } else {
      this.dataMessage = false;
    }

    LOG.debug("Last Sequence: " + lastSequence);
    LOG.debug("Publish Type: " + (this.dataMessage ? "Data" : "Address"));

    // pure data is for first message of update requests
    boolean pureData = this.updateRequest && this.dataMessage;
    retrieveCurrentRecords(lastSequence);
    checkIfMainAddrIsUpdated();
    MessageTransformer transformer = TransformerManager.getTransformer(this.mqIntfReqQueue.getCmrIssuingCntry());

    if (CmrConstants.REQ_TYPE_CREATE.equals(this.mqIntfReqQueue.getReqType())) {
      this.addressUpdated = false;
    } else if (!pureData) {
      // for updates, only send first message as pure data then any relevant
      // addresses after
      LOG.debug("Checking if current address is updated or added..");
      this.addressUpdated = RequestUtils.isUpdated(this.entityManager, this.addrData, this.cmrData.getCmrIssuingCntry());
      LOG.debug(" - Updated/Added: " + this.addressUpdated);

      // first check, if not updated, check shared address with main if main is
      // updated
      if (!this.addressUpdated) {
        this.addressUpdated = this.mainAddressUpdated && sharesSequenceWithMain(this.addrData);
      }
      if (!this.addressUpdated && transformer != null) {
        // check transformer level
        this.addressUpdated = transformer.shouldForceSendAddress(this, this.addrData);
      }

      // current address on the queue is not updated or added, iterate all
      // addresses and check for the next updated/added one
      if (!this.addressUpdated) {
        int nextSequence = getNextSequenceForUpdates(lastSequence);
        if (nextSequence > 0) {
          // found an updated one, set this as the current address, and the
          // publish sequence
          LOG.debug("Address " + this.addrData.getId().getAddrType() + " (" + this.addrData.getId().getAddrSeq() + ") updated. Sending this one.");
          this.publishedSequence = nextSequence;
          lastSequence = nextSequence;
          this.addressUpdated = true;
        } else {

          if (transformer == null || transformer.shouldCompleteProcess(this.entityManager, this, MQMsgConstants.SOF_STATUS_SUCCESS, true)) {
            // there are no more addresses pending to be sent, abort sending to
            // MQ
            // and complete the request
            LOG.debug("No more addresses to send, completing request with MQ ID " + this.mqIntfReqQueue.getId().getQueryReqId());
            completeUpdateRequest();
            this.skipPublish = true;
          } else {
            LOG.debug("Processing will continue. Transformer indicated non-completion of processing at this point");
          }
        }
      }
    }

    if (this.skipPublish) {
      // no more valid addresses to send, skip publish
      return;
    }
    if (!pureData) {
      LOG.debug("Next Address to Publish:" + this.addrData.getId().getAddrType() + "/" + this.addrData.getId().getAddrSeq());
    } else {
      LOG.debug("Pure Data message for Request " + this.mqIntfReqQueue.getReqId());
    }

    int docNumVal = lastSequence + 1;
    this.doc_num = String.valueOf(docNumVal == 0 ? 1 : docNumVal);
    LOG.debug("Doc Num: " + this.doc_num);

    // add the blank items on the map, for field ordering
    addItemsForMessage(pureData);

    boolean isDelete = MQMsgConstants.REQ_TYPE_DELETE.equals(mqIntfReqQueue.getReqType());

    // basic headers
    messageHash.put("XML_DocumentNumber", isDelete ? MQMsgConstants.XML_DOCUMENTNUMBER_1 : doc_num);
    switch (mqIntfReqQueue.getReqType()) {
    case (MQMsgConstants.REQ_TYPE_CREATE):
      messageHash.put("Operation", MQMsgConstants.MQ_OPERATION_C);
      break;
    case (MQMsgConstants.REQ_TYPE_UPDATE):
      messageHash.put("Operation", MQMsgConstants.MQ_OPERATION_U);
      break;
    case (MQMsgConstants.REQ_TYPE_DELETE):
      messageHash.put("Operation", MQMsgConstants.MQ_OPERATION_D);
      break;
    }
    messageHash.put("UniqueNumber", (isDelete ? MQMsgConstants.XML_DOCUMENTNUMBER_1 : Long.toString(mqIntfReqQueue.getId().getQueryReqId())));

    String sysLoc = mqIntfReqQueue.getCmrIssuingCntry();
    if (transformer != null) {
      sysLoc = transformer.getSysLocToUse();
    }
    messageHash.put("Country", sysLoc);
    messageHash.put("CountryID", LandedCountryMap.getSysLocDescription(sysLoc));
    messageHash.put("CustomerNo", mqIntfReqQueue.getCmrNo());
    messageHash.put("TransactionCode", MQMsgConstants.SOF_TRANSACTION_NEW);

    if (this.updateRequest) {
      messageHash.put("TransactionCode", MQMsgConstants.SOF_TRANSACTION_MODIFY);
    }

    String elementNames = MQProcessUtil.getItemValue("sof." + (this.dataMessage ? (pureData ? "dataonly" : "data") : "address") + ".elements");

    String[] itemNames = elementNames.split(",");
    String elemtValue = "";
    Object value = null;
    String[] mappingArray;

    // now parse the mapped fields and set the values
    for (String itemName : itemNames) {
      elemtValue = MQProcessUtil.getMappingValue(mqIntfReqQueue.getCmrIssuingCntry(), itemName.trim());

      if (itemName.startsWith("XXX")) {
        messageHash.remove(itemName);
        if (transformer != null) {
          itemName = itemName.replace("XXX", transformer.getAddressKey(addrData.getId().getAddrType()));
        } else {
          itemName = itemName.replace("XXX", "");
        }
      }

      if (!StringUtils.isEmpty(elemtValue) && elemtValue.indexOf(".") > 1) {
        try {
          mappingArray = elemtValue.split("\\.");
          if (mappingArray[0].equalsIgnoreCase(Data.class.getSimpleName())) {
            Method m = cmrData.getClass().getMethod(MQMsgConstants.XMLMSG_GET + mappingArray[1].trim());
            value = m.invoke(cmrData);
          }
          if (mappingArray[0].equalsIgnoreCase(Addr.class.getSimpleName())) {
            Method m = addrData.getClass().getMethod(MQMsgConstants.XMLMSG_GET + mappingArray[1].trim());
            value = m.invoke(addrData);
          }
          if (mappingArray[0].equalsIgnoreCase(Admin.class.getSimpleName())) {
            Method m = adminData.getClass().getMethod(MQMsgConstants.XMLMSG_GET + mappingArray[1].trim());
            value = m.invoke(adminData);
          }
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
          e.printStackTrace();
          messageHash.put(itemName.trim(), "");
        }
        messageHash.put(itemName.trim(), (String) value);
      } else
        messageHash.put(itemName.trim(), "");
    }

    if (this.dataMessage) {
      processDataElements(transformer);
    }

    if (!pureData) {
      processAddressElements(transformer);
    }
  }

  /**
   * Checks if the current address shares sequence with main addr
   * 
   * @param addr
   * @return
   */
  private boolean sharesSequenceWithMain(Addr addr) {
    if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(addr.getId().getAddrType())) {
      // this is THE main address
      return false;
    }
    if (!"Y".equals(addr.getImportInd())) {
      // this was imported, return false
      return false;
    }
    LOG.debug("Checking if address shares sequence no with Main address");
    if (this.currentAddresses != null) {
      for (Addr currAddr : this.currentAddresses) {
        if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(currAddr.getId().getAddrType())) {
          // main address found
          boolean shared = addr.getId().getAddrSeq().equals(currAddr.getId().getAddrSeq());
          LOG.debug(" - shared? " + shared);
          return shared;
        }
      }
    }
    return false;
  }

  /**
   * Checks if the ZS01 address is updated
   */
  private void checkIfMainAddrIsUpdated() {
    if (this.currentAddresses != null) {
      for (Addr addr : this.currentAddresses) {
        if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(addr.getId().getAddrType())) {
          // this is the main address, check if updated
          LOG.debug("Checking if main address is updted.");
          this.mainAddressUpdated = RequestUtils.isUpdated(this.entityManager, addr, this.mqIntfReqQueue.getCmrIssuingCntry());
          LOG.debug(" - main address " + (this.mainAddressUpdated ? "" : "not ") + "updated");
        }
      }
    }
  }

  private void processDataElements(MessageTransformer transformer) {
    if (CmrConstants.REQ_TYPE_CREATE.equals(mqIntfReqQueue.getReqType())) {
      messageHash.put("CustomerNo", "");
    }

    messageHash.put("ISU", cmrData.getIsuCd() + cmrData.getClientTier());
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equalsIgnoreCase(cmrData.getCustSubGrp())) {
      messageHash.put("IsBusinessPartner", MQMsgConstants.FLAG_YES);
    } else {
      messageHash.put("IsBusinessPartner", MQMsgConstants.FLAG_NO);
    }

    messageHash.put("PrintSequenceNo", "4");

    if (!updateRequest)
      if (MQMsgConstants.CUSTSUBGRP_COMLC.equalsIgnoreCase(cmrData.getCustSubGrp())) {
        messageHash.put("LeasingCompany", MQMsgConstants.FLAG_YES);
      } else {
        messageHash.put("LeasingCompany", MQMsgConstants.FLAG_NO);
      }

    if (transformer != null) {
      LOG.debug("Formatting data values for Country " + cmrData.getCmrIssuingCntry());
      transformer.formatDataLines(this);
    }

    if (updateRequest) {
      // don't send leasing for updates
      messageHash.remove("LeasingCompany");

      // use current MRC for updates
      String currMrc = this.currentCMRValues.get("MarketingResponseCode");
      LOG.debug("Current MRC: " + currMrc);
      if (!StringUtils.isEmpty(currMrc)) {
        messageHash.put("MarketingResponseCode", currMrc);
      }
    }

  }

  private void processAddressElements(MessageTransformer transformer) {
    boolean sendAsCreate = false;

    if (CmrConstants.REQ_TYPE_CREATE.equals(this.mqIntfReqQueue.getReqType())) {
      LOG.debug("This is a create request, send address as create");
      sendAsCreate = false; // reverse to not mess with the trans codes
    } else if ("U".equals(this.mqIntfReqQueue.getReqType()) && !"Y".equals(addrData.getImportInd())) {
      // this is an update request, and the address has been added
      sendAsCreate = true;
      LOG.trace("New address on an update request, sending as create.");
    } else {
      // send as special create if the address is a new one, or has changed
      sendAsCreate = "U".equals(this.mqIntfReqQueue.getReqType()) && !"Y".equals(addrData.getImportInd());
      LOG.trace("New address for update? " + sendAsCreate + " Address Updated? " + this.addressUpdated);

      sendAsCreate = sendAsCreate || (this.mainAddressUpdated && sharesSequenceWithMain(this.addrData));
      sendAsCreate = sendAsCreate || (this.addressUpdated && hasSharedSequenceNo(this.addrData));

      if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(this.addrData.getId().getAddrType())) {
        // this is the main address, never send as create
        LOG.trace("This is the main address, sending as Update only.");
        sendAsCreate = false;
      }
    }

    LOG.trace("Send as Create? " + sendAsCreate);

    if (sendAsCreate) {
      // this address should be sent as create
      LOG.debug("Address " + addrData.getId().getAddrType() + " (" + addrData.getId().getAddrSeq()
          + ") to be sent as create. Setting to TransactionCode = N");
      messageHash.put("TransactionCode", MQMsgConstants.SOF_TRANSACTION_NEW);
      messageHash.put("AddressNumber", "-----");
      messageHash.put("Operation", MQMsgConstants.MQ_OPERATION_C);
    } else {
      LOG.debug("Address " + addrData.getId().getAddrType() + " (" + addrData.getId().getAddrSeq() + ") sending as plain update ");
      // for any other scenario, put address number
      messageHash.put("AddressNumber", addrData.getId().getAddrSeq());
    }

    messageHash.put("AddressType", "Mailing");
    messageHash.put("AddressUse", MQMsgConstants.SOF_ADDRESS_USE_MAILING);

    if (transformer != null) {
      messageHash.put("AddressType", transformer.getTargetAddressType(this.addrData.getId().getAddrType()));
      messageHash.put("AddressUse", transformer.getAddressUse(this.addrData));
      LOG.debug("Formatting address values for Country " + cmrData.getCmrIssuingCntry());
      transformer.formatAddressLines(this);
    }
  }

  @Override
  public String buildMQMessage() throws Exception {

    if (CmrConstants.REQ_TYPE_UPDATE.equals(mqIntfReqQueue.getReqType())) {
      retrieveCurrentValues();
    }

    if (MQMsgConstants.REQ_STATUS_NEW.equals(this.mqIntfReqQueue.getReqStatus())) {
      createPartialComment("Processing started.  Reference ID: " + this.mqIntfReqQueue.getId().getQueryReqId(), "");
    }
    convertCMRToMQObject(this.mqIntfReqQueue);

    if (this.skipPublish) {
      return null;
    }
    String fileName = messageHash.get(MQMsgConstants.XMLMSG_UNIQUENUMBER) + "_" + MQMsgConstants.FILE_TIME_FORMATTER.format(new Date()) + ".xml";

    String outPath = BatchUtil.getProperty("XMLOUTPATH");
    String cmrHome = SystemConfiguration.getValue("CMR_HOME");
    if (!StringUtils.isEmpty(cmrHome)) {
      outPath = cmrHome + "/createcmr/xml/sof/output/";
    }
    XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());

    MessageTransformer transformer = TransformerManager.getTransformer(this.mqIntfReqQueue.getCmrIssuingCntry());
    if (transformer != null) {
      XMLOutputter specificOutputter = transformer.getXmlOutputter(Format.getPrettyFormat());
      if (specificOutputter != null) {
        LOG.debug("Using XML Outputter class " + specificOutputter.getClass().getName() + " for " + this.mqIntfReqQueue.getCmrIssuingCntry());
        xmlOutputter = specificOutputter;
      }
    }

    Document document = new Document();
    Element root = new Element(MQMsgConstants.XMLMSG_RDCTOSOF);
    document.addContent(root);
    Element documentElement = new Element(MQMsgConstants.XMLMSG_DOCUMENT);
    documentElement.setAttribute(MQMsgConstants.XMLMSG_FORM, MQMsgConstants.XMLMSG_XMLDOC);
    root.addContent(documentElement);

    // add the message headers first
    addMessageHeaders(documentElement, this.mqIntfReqQueue);

    Set<String> elementSet = messageHash.keySet();
    Iterator<String> iter = elementSet.iterator();
    while (iter.hasNext()) {
      String element = iter.next();
      Element elementCurrent = new Element(MQMsgConstants.XMLMSG_ITEM);
      elementCurrent.setAttribute(MQMsgConstants.XMLMSG_NAME, element);
      documentElement.addContent(elementCurrent);
      Element text = new Element(MQMsgConstants.XMLMSG_TEXT);
      elementCurrent.addContent(text.setText(messageHash.get(element)));
    }

    try {
      if (LOG.isTraceEnabled()) {
        LOG.trace("XML output:");
        StringWriter sw = new StringWriter();
        try {
          xmlOutputter.output(document, sw);
          saveXmlContentToDB(sw.toString(), fileName, this.mqIntfReqQueue.getId().getQueryReqId());
          LOG.trace(sw.toString());
        } finally {
          sw.close();
        }
      }
      xmlOutputter.output(document, new FileOutputStream(outPath + fileName));
    } catch (Exception e) {
    }
    return xmlOutputter.outputString(document);
  }

  /**
   * Adds blank items to the message map, for use in items ordering
   */
  private void addItemsForMessage(boolean pureData) {
    // add blanks to properly order the values
    // common fields first
    List<String> items = new ArrayList<String>();

    String elements = MQProcessUtil.getItemValue("sof.common.elements");
    if (!StringUtils.isEmpty(elements)) {
      items.addAll(Arrays.asList(elements.split(",")));
    }
    if (this.dataMessage) {
      elements = MQProcessUtil.getItemValue(pureData ? "sof.dataonly.elements" : "sof.data.elements");
      if (!StringUtils.isEmpty(elements)) {
        items.addAll(Arrays.asList(elements.split(",")));
      }
    } else {
      elements = MQProcessUtil.getItemValue("sof.address.elements");
      if (!StringUtils.isEmpty(elements)) {
        items.addAll(Arrays.asList(elements.split(",")));
      }
    }

    for (String item : items) {
      messageHash.put(item, "");
    }
  }

  /**
   * Add the SOF Mesage Headers
   * 
   * @param documentElement
   * @param queue
   */
  private void addMessageHeaders(Element documentElement, MqIntfReqQueue queue) {
    String reqType = queue.getReqType();
    List<String> dateTimeFields = null;
    if (MQMsgConstants.REQ_TYPE_DELETE.equals(reqType)) {
      dateTimeFields = Arrays.asList("created");
    } else {
      dateTimeFields = Arrays.asList("created", "modified", "revised", "lastaccessed", "addedtofile", "revisions");
    }

    // timestamp fields first
    String createTs = MQMsgConstants.TIME_FORMATTER.format(mqIntfReqQueue.getCreateTs());
    String updateTs = MQMsgConstants.TIME_FORMATTER.format(mqIntfReqQueue.getLastUpdtTs());
    for (String elementName : dateTimeFields) {
      Element elementCurrent = new Element(elementName);
      documentElement.addContent(elementCurrent);
      Element datetime = new Element(MQMsgConstants.XMLMSG_DATETIME);
      datetime.setAttribute(MQMsgConstants.XMLMSG_DST, "true");
      if ("created".equals(elementName)) {
        elementCurrent.addContent(datetime.setText(createTs));
      } else {
        elementCurrent.addContent(datetime.setText(updateTs));
      }
    }

    // updated by meta
    Element elementCurrent = new Element(MQMsgConstants.XMLMSG_UPDATEDBY);
    documentElement.addContent(elementCurrent);
    Element childElement = new Element(MQMsgConstants.XMLMSG_NAME);
    elementCurrent.addContent(childElement.setText(queue.getCreateBy()));

  }

  @Override
  public void processMQMessage(String xmlData) throws Exception {
    this.resXML = xmlData;

    String inPath = BatchUtil.getProperty("XMLINPATH");
    String cmrHome = SystemConfiguration.getValue("CMR_HOME");
    if (!StringUtils.isEmpty(cmrHome)) {
      inPath = cmrHome + "/createcmr/xml/sof/input/";
    }

    StringReader read = new StringReader(resXML);
    InputSource source = new InputSource(read);
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(source);

    List<String> xmlFirstPartElementList = new ArrayList<String>();
    xmlFirstPartElementList = Arrays.asList(MQMsgConstants.MESSAGE_FRONT_ELEMENTS);

    // Get root element
    Element rootElement = doc.getRootElement();
    // LOG.trace("Root: " + rootName); //

    // Get document element
    Element documentElement = rootElement.getChild(MQMsgConstants.XMLMSG_DOCUMENT);

    // Get children element list
    @SuppressWarnings("unchecked")
    List<Element> elementList = documentElement.getChildren();

    String uniqueNum = "";
    String name = "";
    String value = "";
    if (this.rdcLegacyMQMessage == null) {
      this.rdcLegacyMQMessage = new SOFResponseCUDQMessage();
    }
    for (int i = 0; i < elementList.size(); i++) {
      Element lineElement = elementList.get(i);
      String elementName = lineElement.getName();
      name = lineElement.getName();

      if (xmlFirstPartElementList.contains(elementName) == true && !elementName.equals(MQMsgConstants.XMLMSG_UPDATEDBY)) {
        value = lineElement.getChildText(MQMsgConstants.XMLMSG_DATETIME);
      } else if (elementName.equals(MQMsgConstants.XMLMSG_UPDATEDBY)) {
        value = lineElement.getChildText(MQMsgConstants.XMLMSG_NAME);
      } else if (elementName.equals(MQMsgConstants.XMLMSG_ITEM)) {
        name = lineElement.getAttributeValue(MQMsgConstants.XMLMSG_NAME);
        value = lineElement.getChildText(MQMsgConstants.XMLMSG_TEXT);
        if (name.equalsIgnoreCase(MQMsgConstants.XMLMSG_UNIQUENUMBER))
          uniqueNum = value;
      } else {
        LOG.trace("Illegal elementName: " + elementName);
      }
      // Set Value
      Method m = null;
      m = rdcLegacyMQMessage.getClass().getMethod(MQMsgConstants.XMLMSG_SET + name, String.class);
      if (m != null && this.rdcLegacyMQMessage != null) {
        m.invoke(rdcLegacyMQMessage, value);
      }

    }
    XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
    String fileName = uniqueNum.trim() + "_" + MQMsgConstants.FILE_TIME_FORMATTER.format(new Date()) + ".xml";
    String xmlFileName = inPath + fileName;
    try {
      xmlOutputter.output(doc, new FileOutputStream(xmlFileName));
    } catch (Exception e) {
      LOG.warn("Warning, physical file not saved.");
    }

    if (LOG.isTraceEnabled()) {
      StringWriter sw = new StringWriter();
      try {
        LOG.trace("Reply XML: ");
        xmlOutputter.output(doc, sw);
        saveXmlContentToDB(sw.toString(), fileName, this.mqIntfReqQueue.getId().getQueryReqId());
        LOG.trace(sw.toString());
      } finally {
        sw.close();
      }
    }

    saveMsgToDB();
  }

  public void saveMsgToDB() throws Exception {
    int lastSequence = getLastSequence(this.mqIntfReqQueue);
    LOG.debug("Process MQ: Last Sequence = " + lastSequence + " from Status " + this.mqIntfReqQueue.getReqStatus());
    String cmrNo = rdcLegacyMQMessage.getCustomerNo();

    retrieveCurrentRecords(lastSequence);

    MessageTransformer transformer = TransformerManager.getTransformer(this.mqIntfReqQueue.getCmrIssuingCntry());
    String replyStatus = rdcLegacyMQMessage.getStatus().trim();
    LOG.debug("Status: " + replyStatus + " for Query ID " + this.mqIntfReqQueue.getId().getQueryReqId());
    if (MQMsgConstants.SOF_STATUS_SUCCESS.equalsIgnoreCase(rdcLegacyMQMessage.getStatus().trim())) {

      if (!this.mqIntfReqQueue.getReqStatus().contains("PUB")) {
        // this message is obsolete, status should be PUB when updating status
        createPartialComment(
            "A successful reply was receieved for this request in the middle of processing. Please check with your system administrator.", cmrNo);
        return;
      }
      Addr nextAddr = this.addrData;

      Addr lastAddr = lastSequence > 0 && this.currentAddresses != null && this.currentAddresses.size() >= lastSequence ? this.currentAddresses
          .get(lastSequence - 1) : null;
      if ("U".contains(this.mqIntfReqQueue.getReqType()) && lastAddr != null && !"Y".equals(lastAddr.getImportInd())) {
        LOG.debug("Last address processed: " + lastAddr.getId().getAddrType() + " (" + lastAddr.getId().getAddrSeq() + ")");
        String addrNum = rdcLegacyMQMessage.getAddressNo();
        String type = lastAddr.getId().getAddrType();
        if (transformer != null) {
          type = transformer.getTargetAddressType(lastAddr.getId().getAddrType());
        }

        if (this.currentAddresses != null) {
          boolean sequenceExists = false;
          if (StringUtils.isEmpty(addrNum)) {
            sequenceExists = true;
          } else {
            for (Addr addr : this.currentAddresses) {
              if (addr.getId().getAddrType().equals(lastAddr.getId().getAddrType()) && addr.getId().getAddrSeq().equals(addrNum)) {
                sequenceExists = true;
              }
            }
          }
          if (!sequenceExists) {
            LOG.debug("Returned sequence does not exist on the request, updating to " + addrNum);
            String updateSeq = ExternalizedQuery.getSql("MQREQUEST.UPDATE_ADDR_SEQ");
            PreparedQuery q = new PreparedQuery(this.entityManager, updateSeq);
            q.setParameter("NEW_SEQ", addrNum);
            q.setParameter("REQ_ID", lastAddr.getId().getReqId());
            q.setParameter("TYPE", lastAddr.getId().getAddrType());
            q.setParameter("OLD_SEQ", lastAddr.getId().getAddrSeq());
            LOG.debug("Assigning address sequence " + addrNum + " to " + type + " address.");
            try {
              q.executeSql();
              createPartialComment("Address Number " + addrNum + " assigned to " + type + " address.", cmrNo);

              OpenJPAEntityTransaction transaction = (OpenJPAEntityTransaction) entityManager.getTransaction();
              if (transaction != null && transaction.isActive() && !transaction.getRollbackOnly()) {
                LOG.debug("Transaction partially committed");
                transaction.commitAndResume();
              }

            } catch (Exception e) {
              LOG.warn("Cannot update address sequence,", e);
            }
          } else {
            LOG.debug("Returned sequence " + addrNum + " exists on the request, skipping.");
          }
        }
      }

      int nextSequence = lastSequence;
      if (transformer != null) {
        boolean send = false;
        while (nextAddr != null && !send) {
          send = transformer.shouldSendAddress(entityManager, this, nextAddr);
          LOG.trace("Checking address sequence " + nextSequence + " send? " + send);
          if (!send) {
            nextSequence++;
            nextAddr = getNextAddressData(this.mqIntfReqQueue, nextSequence);
          }
        }
      }
      LOG.debug("Next Address : " + (nextAddr != null ? nextAddr.getId().getAddrType() + "/" + nextAddr.getId().getAddrSeq() : "none"));

      if (nextAddr != null) {
        LOG.debug("Other addresses pending, setting to COM" + nextSequence);
        updateMQIntfStatus(MQMsgConstants.REQ_STATUS_COM + nextSequence);
      } else {

        if (transformer == null || transformer.shouldCompleteProcess(this.entityManager, this, MQMsgConstants.SOF_STATUS_SUCCESS, false)) {

          // no more addresses left, set to final COM status
          LOG.debug("No more addresses left, setting to final COM status.");
          updateMQIntfStatus(MQMsgConstants.REQ_STATUS_COM);

          String addtlComments = null;
          if (this.updateRequest) {
            StringBuilder sbSeq = new StringBuilder();
            List<Addr> latestAddr = getLatestAddresses();
            if (latestAddr != null) {
              for (Addr addr : latestAddr) {
                if (!"Y".equals(addr.getImportInd())) {
                  String type = addr.getId().getAddrType();
                  if (transformer != null) {
                    type = transformer.getTargetAddressType(addr.getId().getAddrType());
                  }
                  // this is a new address on an update;
                  sbSeq.append("\n");
                  sbSeq.append(type + " = " + addr.getId().getAddrSeq());
                }
              }
            }
            if (sbSeq.length() > 0) {
              addtlComments = "\nSequences generated:" + sbSeq.toString();
            }
          }

          if (StringUtils.isEmpty(this.mqIntfReqQueue.getCmrNo())) {
            this.mqIntfReqQueue.setCmrNo(cmrNo);
            this.entityManager.merge(mqIntfReqQueue);
          }
          updateAdminRequest(MQMsgConstants.REQ_STATUS_COM, MQMsgConstants.PROCESSED_FLAG_Y, true, addtlComments);
          if (this.updateRequest) {
            createPartialComment(MQMsgConstants.WH_HIST_CMT_COM_UPDATE + cmrNo, cmrNo);
          } else {
            createPartialComment(MQMsgConstants.WH_HIST_CMT_COM + cmrNo, cmrNo);
          }
        }
      }
      if (lastSequence == 1 && MQMsgConstants.REQ_TYPE_CREATE.equals(this.mqIntfReqQueue.getReqType())) {
        // this is the first message after create, update CMR No and create
        // comment
        LOG.debug("Processing CMR No. from reply :  " + cmrNo);
        String addrNum = rdcLegacyMQMessage.getAddressNo();
        this.mqIntfReqQueue.setCmrNo(cmrNo);
        this.entityManager.merge(mqIntfReqQueue);
        this.cmrData.setCmrNo(cmrNo);

        if (transformer != null) {
          transformer.setValuesAfterInitialSuccess(this);
        }
        if (this.adminData != null && "Y".equals(this.adminData.getProspLegalInd())) {
          try {
            this.cmrData.setProspectSeqNo(Integer.parseInt(addrNum) + "");

          } catch (NumberFormatException e) {
            this.cmrData.setProspectSeqNo(addrNum);
          }

          if (transformer != null && transformer.getFixedAddrSeqForProspectCreation() != null) {
            LOG.debug("Fixed Sequence No to use for Prospect Conversion is " + transformer.getFixedAddrSeqForProspectCreation());
            this.cmrData.setProspectSeqNo(transformer.getFixedAddrSeqForProspectCreation() + "");
          }

        }
        entityManager.merge(cmrData);
        createPartialComment("CMR No. " + cmrNo + " has been assigned to the request (System Location " + this.mqIntfReqQueue.getCmrIssuingCntry()
            + ").", cmrNo);
      }

    } else if (MQMsgConstants.SOF_STATUS_ANA.equals(replyStatus)) {
      // anagrafico specific reply
      String anagraficoMsg = rdcLegacyMQMessage.getMessage();
      if (StringUtils.isBlank(anagraficoMsg)) {
        createPartialComment("Anagrafico Status: " + replyStatus + ". No message supplied.", this.mqIntfReqQueue.getCmrNo());
      } else {
        createPartialComment("Anagrafico Reply: " + anagraficoMsg, this.mqIntfReqQueue.getCmrNo());
      }
      if (transformer != null && transformer.shouldCompleteProcess(this.entityManager, this, MQMsgConstants.SOF_STATUS_ANA, false)) {
        completeRequestFromReply(transformer, cmrNo);
      }
    } else {
      if (!this.mqIntfReqQueue.getReqStatus().contains("PUB")) {
        // this message is obsolete, status should be PUB when updating status
        // for errors, just return
        return;
      }
      LOG.debug("Error was encountered during processing. Sending request back to processor.");
      updateMQIntfErrorStatus(MQMsgConstants.REQ_STATUS_SER + lastSequence, "001", rdcLegacyMQMessage.getMessage());
      updateAdminRequest(MQMsgConstants.REQ_STATUS_PPN, MQMsgConstants.PROCESSED_FLAG_E, false);
    }
  }

  private List<Addr> getLatestAddresses() {
    // place here for now, move to cmr-queries after 2 weeks on prod
    String sql = "select * from CREQCMR.ADDR where REQ_ID = :REQ_ID";
    PreparedQuery query = new PreparedQuery(this.entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("REQ_ID", this.mqIntfReqQueue.getReqId());
    return query.getResults(Addr.class);
  }

  /**
   * Checks if the address has a shared sequence no
   * 
   * @param addr
   * @return
   */
  protected boolean hasSharedSequenceNo(Addr addr) {
    // just compare with imported values, this will work even if some addresses
    // on the request were deleted
    LOG.debug("Checking for shared address sequences...");
    String sql = ExternalizedQuery.getSql("MQREQUEST.SOF.CHECK_SEQUENCE");
    PreparedQuery query = new PreparedQuery(this.entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("SEQ", addr.getId().getAddrSeq());
    query.setParameter("TYPE", addr.getId().getAddrType());
    boolean hasShared = query.exists();
    if (hasShared) {
      LOG.debug("Address Number is SHARED for this address.");
    } else {
      LOG.debug("Address Number is unique for this address.");
    }
    return hasShared;
  }

  /**
   * Gets the last sequence of publish
   * 
   * @param reqQueue
   * @return
   */
  @Override
  protected int getLastSequence(MqIntfReqQueue reqQueue) {
    String intfStatus = reqQueue.getReqStatus();
    int lastSeq = -1;
    if (MQMsgConstants.REQ_STATUS_NEW.equals(intfStatus)) {
      return this.updateRequest && this.dataMessage ? -1 : 0;
    }
    if (intfStatus != null && intfStatus.contains(MQMsgConstants.REQ_STATUS_COM)) {
      if (!MQMsgConstants.REQ_STATUS_COM.equals(intfStatus)) {
        lastSeq = Integer.parseInt(intfStatus.substring(3));
      }
    }
    if (intfStatus != null && intfStatus.contains(MQMsgConstants.REQ_STATUS_PUB)) {
      if (!MQMsgConstants.REQ_STATUS_PUB.equals(intfStatus)) {
        lastSeq = Integer.parseInt(intfStatus.substring(3));
      }
    }
    return lastSeq;
  }

  protected int getNextSequenceForUpdates(int currSequence) {
    boolean updated = false;
    int sequence = currSequence;
    MessageTransformer transformer = TransformerManager.getTransformer(this.mqIntfReqQueue.getCmrIssuingCntry());
    while (!updated) {
      LOG.debug("Checking next address");
      Addr addr = this.currentAddresses != null && this.currentAddresses.size() > sequence ? this.currentAddresses.get(sequence)
          : getNextAddressData(this.mqIntfReqQueue, sequence);
      if (addr == null) {
        updated = true;
        break;
      }
      LOG.debug("Checking address " + addr.getId().getAddrType() + " (" + addr.getId().getAddrSeq() + ") if updated or added..");
      updated = this.mainAddressUpdated && sharesSequenceWithMain(addr);
      updated = updated || !"Y".equals(addr.getImportInd()) || RequestUtils.isUpdated(this.entityManager, addr, this.cmrData.getCmrIssuingCntry());
      LOG.debug(" - Updated/Added: " + updated);
      if (updated) {
        this.addrData = addr;
        return sequence;
      } else if (transformer != null && transformer.shouldForceSendAddress(this, addr)) {
        this.addrData = addr;
        return sequence;
      } else {
        sequence += 1;
      }
    }
    return -1;
  }

  /**
   * Completes a request based on the reply received
   * 
   * @param transformer
   * @param cmrNo
   * @throws Exception
   */
  protected void completeRequestFromReply(MessageTransformer transformer, String cmrNo) throws Exception {
    LOG.debug("No more addresses left, setting to final COM status.");
    updateMQIntfStatus(MQMsgConstants.REQ_STATUS_COM);

    String addtlComments = null;
    if (this.updateRequest) {
      StringBuilder sbSeq = new StringBuilder();
      List<Addr> latestAddr = getLatestAddresses();
      if (latestAddr != null) {
        for (Addr addr : latestAddr) {
          if (!"Y".equals(addr.getImportInd())) {
            String type = addr.getId().getAddrType();
            if (transformer != null) {
              type = transformer.getTargetAddressType(addr.getId().getAddrType());
            }
            // this is a new address on an update;
            sbSeq.append("\n");
            sbSeq.append(type + " = " + addr.getId().getAddrSeq());
          }
        }
      }
      if (sbSeq.length() > 0) {
        addtlComments = "\nSequences generated:" + sbSeq.toString();
      }
    }

    if (StringUtils.isEmpty(this.mqIntfReqQueue.getCmrNo())) {
      this.mqIntfReqQueue.setCmrNo(cmrNo);
      this.entityManager.merge(mqIntfReqQueue);
    }
    updateAdminRequest(MQMsgConstants.REQ_STATUS_COM, MQMsgConstants.PROCESSED_FLAG_Y, true, addtlComments);
  }

  /**
   * Completes an update request when processor finds no more addresses to send
   * 
   * @throws Exception
   */
  protected void completeUpdateRequest() throws Exception {
    String cmrNo = this.mqIntfReqQueue.getCmrNo();
    LOG.debug("No more addresses left, setting to final COM status.");

    MessageTransformer transformer = TransformerManager.getTransformer(this.mqIntfReqQueue.getCmrIssuingCntry());

    StringBuilder sbSeq = new StringBuilder();

    if (this.currentAddresses != null) {
      for (Addr addr : this.currentAddresses) {
        if (!"Y".equals(addr.getImportInd())) {
          String type = addr.getId().getAddrType();
          if (transformer != null) {
            type = transformer.getTargetAddressType(addr.getId().getAddrType());
          }
          // this is a new address on an update;
          sbSeq.append("\n");
          sbSeq.append(type + " = " + addr.getId().getAddrSeq());
        }
      }
    }
    String addtlComments = sbSeq.length() > 0 ? "\nSequences generated: " + sbSeq.toString() : null;

    updateMQIntfStatus(MQMsgConstants.REQ_STATUS_COM);
    updateAdminRequest(MQMsgConstants.REQ_STATUS_COM, MQMsgConstants.PROCESSED_FLAG_Y, true, addtlComments);

    StringBuilder sbComment = new StringBuilder();
    sbComment.append(MQMsgConstants.WH_HIST_CMT_COM_UPDATE + this.mqIntfReqQueue.getCmrNo());
    if (sbSeq.length() > 0) {
      sbComment.append("\n").append("Sequences generated:");
      sbComment.append(sbSeq.toString());
    }

    createPartialComment(sbComment.toString(), cmrNo);

  }

  @Override
  public void retrieveCurrentValues() {
    this.currentCMRValues = new HashMap<String, String>();
    String cmrIssuingCntry = this.mqIntfReqQueue.getCmrIssuingCntry();
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }
    if (SystemLocation.ISRAEL.equals(cmrIssuingCntry)) {
      cmrIssuingCntry = SystemLocation.SAP_ISRAEL_SOF_ONLY;
    }
    String cmrNo = this.mqIntfReqQueue.getCmrNo();
    SOFQueryRequest request = new SOFQueryRequest();
    request.setCmrIssuingCountry(cmrIssuingCntry);
    request.setCmrNo(cmrNo);

    LOG.info("Retrieving Legacy values for CMR No " + cmrNo + " from SOF (" + cmrIssuingCntry + ")");

    try {
      SOFServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"),
          SOFServiceClient.class);
      SOFQueryResponse response = client.executeAndWrap(SOFServiceClient.QUERY_APP_ID, request, SOFQueryResponse.class);
      if (response.isSuccess()) {
        String xmlData = response.getData();

        GenericSOFMessageParser handler = new GenericSOFMessageParser();
        ByteArrayInputStream bis = new ByteArrayInputStream(xmlData.getBytes());
        try {
          SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
          parser.parse(new InputSource(bis), handler);
        } finally {
          bis.close();
        }

        this.currentCMRValues = handler.getValues();
        this.shippingSequences = handler.getShippingSequences();
        this.countryCSequences = handler.getCountryCSequences();
        this.installingSequences = handler.getInstallingSequences();

        if (this.shippingSequences != null && !this.shippingSequences.isEmpty()) {
          LOG.trace("Shipping Sequences: " + this.shippingSequences.size());
        } else {
          LOG.trace("Shipping Sequences is empty");
        }
      }
    } catch (Exception e) {
      LOG.warn("An error has occurred during retrieval of the values.", e);
      this.currentCMRValues = new HashMap<String, String>();
      this.shippingSequences = new ArrayList<>();
      this.countryCSequences = new ArrayList<>();
      this.installingSequences = new ArrayList<>();
    }
  }

  @Override
  public boolean retrySupported() {
    return false;
  }

  public static boolean isHandled(String xmlData) {
    if (xmlData == null) {
      return false;
    }

    if (xmlData.contains("<" + SOF_RESPONSE_ROOT_ELEM)) {
      return true;
    }
    return false;
  }

  public static String extractUniqueNumber(String xmlData) throws Exception {
    LOG.trace("The XML from MQ is " + xmlData);
    StringReader read = new StringReader(xmlData);
    InputSource source = new InputSource(read);
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(source);
    Element root = doc.getRootElement();
    @SuppressWarnings("rawtypes")
    List itemList = root.getChild(MQMsgConstants.XMLMSG_DOCUMENT).getChildren(MQMsgConstants.XMLMSG_ITEM);
    for (int i = 0; i < itemList.size(); i++) {
      Element itemElement = (Element) itemList.get(i);
      if (itemElement.getAttributeValue(MQMsgConstants.XMLMSG_NAME).equals(MQMsgConstants.XMLMSG_UNIQUENUMBER)) {
        String uniqueNum = itemElement.getChildText(MQMsgConstants.XMLMSG_TEXT).trim();
        return uniqueNum;
        // String[] uniqueNumArray = uniqueNum.split("_");
        // if (uniqueNumArray.length == 2) {
        // return uniqueNumArray[1];
        // } else {
        // return null;
        // }
      }

    }
    return null;
  }
}
