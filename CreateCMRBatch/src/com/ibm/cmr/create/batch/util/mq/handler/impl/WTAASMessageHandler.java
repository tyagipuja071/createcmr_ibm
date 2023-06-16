/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.handler.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.util.MQProcessUtil;
import com.ibm.cio.cmr.request.util.wtaas.WtaasRecord;
import com.ibm.cmr.create.batch.util.AttributesPerLineOutputter;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.TransformerManager;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.WtaasClient;
import com.ibm.cmr.services.client.wtaas.WtaasQueryRequest;
import com.ibm.cmr.services.client.wtaas.WtaasQueryResponse;

/**
 * {@link MQMessageHandler} implementation for WTAAS interface
 * 
 * @author Jeffrey Zamora
 * 
 */
public class WTAASMessageHandler extends MQMessageHandler {

  private static final Logger LOG = Logger.getLogger(WTAASMessageHandler.class);
  public static final String WTAAS_REQUEST_ROOT_ELEM = "CR010CR";
  public static final String WTAAS_RESPONSE_ROOT_ELEM = "CR020CR";
  public static final String WTAAS_INT_RESPONSE_ROOT_ELEM = "RY090WM";
  public static final String WTAAS_INT_ERROR_ROOT_ELEM = "RY998WM";

  protected WtaasRecord currentRecord;

  public WTAASMessageHandler(EntityManager entityManager, MqIntfReqQueue mqIntfReqQueue) {
    super(entityManager, mqIntfReqQueue);
  }

  private WTAASMessageHandler() {
  }

  @Override
  public String buildMQMessage() throws Exception {

    retrieveCurrentValues();

    // get the current records
    int lastSequence = getLastSequence(this.mqIntfReqQueue);
    LOG.debug("Address Sequence: " + lastSequence);
    retrieveCurrentRecords(lastSequence);

    if ("U".equals(this.mqIntfReqQueue.getReqType())) {
      // for updates, send only changed address
      if (lastSequence > 0 && (!"Y".equals(this.addrData.getChangedIndc()) && "Y".equals(this.addrData.getImportInd()))) {
        int nextSequence = getNextSequenceForUpdates(lastSequence);
        if (nextSequence > 0) {
          LOG.debug("Address " + this.addrData.getId().getAddrType() + " (" + this.addrData.getId().getAddrSeq() + ") updated. Sending this one.");
          this.publishedSequence = nextSequence;
          lastSequence = nextSequence;
          LOG.debug("Updated sequence: " + lastSequence);
        } else {
          // complete here
          String cmrNo = this.mqIntfReqQueue.getCmrNo();
          LOG.debug("Completing request, no more left to publish");
          updateMQIntfStatus(MQMsgConstants.REQ_STATUS_COM);
          updateAdminRequest(MQMsgConstants.REQ_STATUS_COM, MQMsgConstants.PROCESSED_FLAG_Y, true);
          if (CmrConstants.REQ_TYPE_UPDATE.equals(this.mqIntfReqQueue.getReqType())) {
            createPartialComment(MQMsgConstants.WH_HIST_CMT_COM_UPDATE + cmrNo, cmrNo);
          } else {
            createPartialComment(MQMsgConstants.WH_HIST_CMT_COM + cmrNo, cmrNo);
          }
          this.skipPublish = true;
        }
      }
    }

    if (this.skipPublish) {
      return null;
    }

    // add items to the map, to order properly
    addItemsForMessage(lastSequence);

    // set the item defaults and XML meta
    String messageId = Long.toString(this.mqIntfReqQueue.getId().getQueryReqId());
    messageId += String.valueOf((char) (65 + lastSequence));
    String messageIdPlus = messageId + "_" + MQMsgConstants.FILE_TIME_FORMATTER.format(new Date());
    this.messageHash.put("CMRRefNo", messageId);
    this.messageHash.put("TransCode", "C".equals(this.mqIntfReqQueue.getReqType()) ? "N" : "M");
    this.messageHash.put("DocType", "CR");
    this.messageHash.put("SourceCode", this.mqIntfReqQueue.getRefnSourceCd() != null ? this.mqIntfReqQueue.getRefnSourceCd() : "");
    this.messageHash.put("DocRefNo", this.mqIntfReqQueue.getDocmRefnNo() != null ? this.mqIntfReqQueue.getDocmRefnNo() : "");
    this.messageHash.put("CntryNo", this.mqIntfReqQueue.getCmrIssuingCntry());

    if (lastSequence > 0 && !"Y".equals(this.addrData.getImportInd())) {
      // not the first XML, and address data is created
      this.messageHash.put("TransCode", "N");
    } else if ("U".equals(this.mqIntfReqQueue.getReqType()) && "Y".equals(this.addrData.getImportInd())) {
      // put the AddrNo
      this.messageHash.put("AddressNo", StringUtils.leftPad(this.addrData.getId().getAddrSeq().trim(), 5, '0'));
    }

    Document document = new Document();
    Element root = new Element(WTAAS_REQUEST_ROOT_ELEM);

    // assign mapped values first from DB
    String mappedElem = null;
    Object mappedValue = null;
    String[] mappingArray = null;
    Method getMethod = null;
    for (String elementName : messageHash.keySet()) {
      mappedElem = MQProcessUtil.getItemValue("wtaas.mapping." + elementName);
      if (!StringUtils.isBlank(mappedElem)) {

        // there is a mapping for this element, try to get value from current
        // objects
        if (!StringUtils.isEmpty(mappedElem) && mappedElem.indexOf(".") > 1) {
          try {
            mappingArray = mappedElem.split("\\.");

            if (mappingArray.length == 2) {
              mappedValue = null;
              if (mappingArray[0].equalsIgnoreCase(Data.class.getSimpleName())) {
                getMethod = Data.class.getMethod(MQMsgConstants.XMLMSG_GET + mappingArray[1].trim());
                mappedValue = getMethod.invoke(this.cmrData);
              }
              if (mappingArray[0].equalsIgnoreCase(Addr.class.getSimpleName())) {
                getMethod = Addr.class.getMethod(MQMsgConstants.XMLMSG_GET + mappingArray[1].trim());
                mappedValue = getMethod.invoke(this.addrData);
              }
              if (mappingArray[0].equalsIgnoreCase(Admin.class.getSimpleName())) {
                getMethod = Admin.class.getMethod(MQMsgConstants.XMLMSG_GET + mappingArray[1].trim());
                mappedValue = getMethod.invoke(this.adminData);
              }
              this.messageHash.put(elementName, (String) mappedValue);
            } else {
              LOG.warn("Invalid mapping for " + elementName + " = " + mappedElem);
              this.messageHash.put(elementName, "");
            }
          } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            this.messageHash.put(elementName, "");
          }
        }
      }
    }

    if ("Y".equalsIgnoreCase(this.adminData.getProspLegalInd()) && MQMsgConstants.REQ_TYPE_CREATE.equals(this.mqIntfReqQueue.getReqType())
        && StringUtils.isNotBlank(this.cmrData.getCmrNo()) && this.cmrData.getCmrNo().startsWith("P")) {
      LOG.debug("CREATECMR - 9713 INDIA MQ ISSUE LOGS ----> cmr number made null");
      this.cmrData.setCmrNo(null);
      this.mqIntfReqQueue.setCmrNo(null);
      this.messageHash.put("CustNo", null);
      LOG.debug("the value of TransCode is ---> " + this.messageHash.get("TransCode"));
      LOG.debug("the value of lastSequence is ---> " + lastSequence);
    } else if (MQMsgConstants.REQ_TYPE_CREATE.equals(this.mqIntfReqQueue.getReqType()) && lastSequence == 0
        && !StringUtils.isEmpty(this.cmrData.getCmrNo())) {
      LOG.debug("Setting CMR No to user supplied value: " + this.cmrData.getCmrNo());
      this.messageHash.put("CustNo", this.cmrData.getCmrNo());
    } else if (MQMsgConstants.REQ_TYPE_CREATE.equals(this.mqIntfReqQueue.getReqType()) && lastSequence == 0
        && !StringUtils.isEmpty(this.cmrData.getCmrNoPrefix())) {
      LOG.debug("Setting CMR No Prefix to user supplied value: " + this.cmrData.getCmrNoPrefix());
      this.messageHash.put("CustNo", this.cmrData.getCmrNoPrefix());
    } else if (MQMsgConstants.REQ_TYPE_CREATE.equals(this.mqIntfReqQueue.getReqType()) && lastSequence > 0) {
      LOG.debug("Setting CMR No to generated value: " + this.mqIntfReqQueue.getCmrNo());
      this.messageHash.put("CustNo", this.mqIntfReqQueue.getCmrNo());
    } else if ("U".equals(this.mqIntfReqQueue.getReqType())) {
      this.messageHash.put("CustNo", this.mqIntfReqQueue.getCmrNo());
    }

    MessageTransformer transformer = TransformerManager.getTransformer(this.mqIntfReqQueue.getCmrIssuingCntry());
    if (transformer != null) {
      if (lastSequence < 1) {
        if ("C".equals(this.mqIntfReqQueue.getReqType()) && !StringUtils.isBlank(this.cmrData.getCmrNo())
            && StringUtils.isBlank(this.mqIntfReqQueue.getCorrelationId())) {
          // for re-processing of non double create requests
          this.messageHash.put("TransCode", "M");
        }
        LOG.trace("Formatting data lines..");
        transformer.formatDataLines(this);
      } else {
        // if this is not the first message, all transactions are M regardless
        // of type
        this.messageHash.put("TransCode", "M");
      }
      LOG.trace("Formatting address lines..");
      transformer.formatAddressLines(this);

      // remove address lines for non-updated main address, only put address use
      if ("U".equals(this.mqIntfReqQueue.getReqType()) && lastSequence == 0 && !isMainAddrUpdated()) {
        for (int i = 1; i <= 6; i++) {
          this.messageHash.remove("AddrLine" + i);
        }
      }
    }

    if ("U".equals(this.mqIntfReqQueue.getReqType())) {
      boolean hasAddressLines = false;
      for (int i = 1; i <= 6; i++) {
        String addressLine = this.messageHash.get("AddrLine" + i);
        if (!StringUtils.isBlank(addressLine)) {
          hasAddressLines = true;
        }
      }
      if (hasAddressLines) {
        if (this.addrData == null || !"N".equals(this.addrData.getImportInd())) {
          for (int i = 1; i <= 6; i++) {
            String addressLine = this.messageHash.get("AddrLine" + i);
            if (StringUtils.isBlank(addressLine)) {
              // for updates, clear out any blank lines
              this.messageHash.put("AddrLine" + i, "@");
            }
          }
        }
      }
    }

    // pad CMRRefNo with leading zeros
    String cmrRefNo = this.messageHash.get("CMRRefNo");
    if (cmrRefNo != null) {
      this.messageHash.put("CMRRefNo", StringUtils.leftPad(cmrRefNo, 10, '0'));
    }

    // now assign all as attributes, in uppercase
    String attributeVal = null;
    for (String elementName : this.messageHash.keySet()) {
      attributeVal = this.messageHash.get(elementName);
      root.setAttribute(elementName, attributeVal != null ? attributeVal.toUpperCase() : "");
    }

    document.setRootElement(root);

    String outPath = BatchUtil.getProperty("XMLOUTPATH");
    String cmrHome = SystemConfiguration.getValue("CMR_HOME");
    if (!StringUtils.isEmpty(cmrHome)) {
      outPath = cmrHome + "/createcmr/xml/sof/output/";
    }
    String fileName = messageIdPlus + ".xml";

    XMLOutputter xmlOutputter = getXmlOutputter(Format.getPrettyFormat());

    StringWriter sw = new StringWriter();
    try {
      xmlOutputter.output(document, sw);

      String xmlTemp = sw.toString();

      xmlTemp = StringEscapeUtils.unescapeXml(xmlTemp);
      saveXmlContentToDB(sw.toString(), fileName, this.mqIntfReqQueue.getId().getQueryReqId());

      LOG.trace(xmlTemp);
    } finally {
      sw.close();
    }

    try {
      // xmlOutputter.output(document, new FileOutputStream(outPath +
      // fileName));
    } catch (Exception e) {
      LOG.warn("XML source cannot be saved for " + fileName);
    }

    String xmlTemp = xmlOutputter.outputString(document);

    xmlTemp = StringEscapeUtils.unescapeXml(xmlTemp);

    return xmlTemp;
  }

  /**
   * Adds blank items to the message map, for use in items ordering
   */
  private void addItemsForMessage(int lastSequence) {
    // add blanks to properly order the values
    // common fields first
    List<String> items = new ArrayList<String>();

    String elementsKey = null;
    if (lastSequence == 0) {
      elementsKey = "wtaas.common.elements";
    } else {
      elementsKey = "wtaas.address.elements";
    }
    LOG.trace("Last Sequence: " + lastSequence + " - Adding items for " + elementsKey);
    String elements = MQProcessUtil.getItemValue(elementsKey);
    if (!StringUtils.isEmpty(elements)) {
      items.addAll(Arrays.asList(elements.split(",")));
    }

    for (String item : items) {
      messageHash.put(item, "");
    }
  }

  @Override
  public void processMQMessage(String xmlData) throws Exception {
    if (xmlData == null) {
      LOG.warn("XML reply is empty. ");
      return;
    }
    if (!xmlData.startsWith("<") && xmlData.contains("<")) {
      xmlData = xmlData.substring(xmlData.indexOf("<"));
    }

    String inPath = BatchUtil.getProperty("XMLINPATH");
    String cmrHome = SystemConfiguration.getValue("CMR_HOME");
    if (!StringUtils.isEmpty(cmrHome)) {
      inPath = cmrHome + "/createcmr/xml/sof/input/";
    }

    WTAASReply reply = null;
    WTAASResponseHandler handler = new WTAASResponseHandler();
    StringReader read = new StringReader(xmlData);
    StringReader read2 = new StringReader(xmlData);
    try {
      InputSource source = new InputSource(read);
      InputSource source2 = new InputSource(read2);

      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.newSAXParser().parse(source, handler);

      reply = handler.getReply();

      if (reply != null) {
        SAXBuilder builder = new SAXBuilder();
        builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document document = builder.build(source2);

        // save the XML
        XMLOutputter xmlOutputter = getXmlOutputter(Format.getPrettyFormat());
        File outXml = new File(inPath + reply.getRefNo() + "_" + MQMsgConstants.FILE_TIME_FORMATTER.format(new Date()) + ".xml");

        int cnt = 1;
        while (outXml.exists()) {
          outXml = new File(inPath + reply.getRefNo() + "_" + MQMsgConstants.FILE_TIME_FORMATTER.format(new Date()) + "_" + cnt + ".xml");
          cnt++;
        }

        LOG.trace("Writing the reply xml as " + outXml.getName());
        try (FileOutputStream fos = new FileOutputStream(outXml)) {
          try {
            xmlOutputter.output(document, fos);
          } catch (Exception e) {
            LOG.warn("Warning, physical file not saved.");
          }
        }

        StringWriter sw = new StringWriter();
        try {
          LOG.trace("Reply XML: ");
          xmlOutputter.output(document, sw);
          saveXmlContentToDB(sw.toString(), outXml.getName(), this.mqIntfReqQueue.getId().getQueryReqId());
          LOG.trace(sw.toString());
        } finally {
          sw.close();
        }
        processReply(reply);
      } else {
        LOG.warn("Invalid reply from WTAAS. Ignoring message.");
      }
    } catch (Exception e) {
      LOG.debug("Error in parsing the XML", e);
      throw e;
    } finally {
      read.close();
      read2.close();
    }

  }

  /**
   * Processes the WTAAS reply
   * 
   * @param reply
   * @throws Exception
   */
  private void processReply(WTAASReply reply) throws Exception {
    int lastSequence = getLastSequence(this.mqIntfReqQueue);
    LOG.debug("Process MQ: Last Sequence = " + lastSequence + " from Status " + this.mqIntfReqQueue.getReqStatus());
    retrieveCurrentRecords(lastSequence);

    String cmrNo = reply.getCmrNo();

    if (MQMsgConstants.REQ_STATUS_COM.equals(this.mqIntfReqQueue.getReqStatus()) || this.mqIntfReqQueue.getReqStatus().startsWith("SER")) {
      // do not process any reply anymore for completed requests
      LOG.warn("A reply was received for Queue Request ID " + this.mqIntfReqQueue.getId().getQueryReqId()
          + ", but it's already completed or already in error. Ignoring.");
    } else {
      if (!reply.isError()) {
        if (reply.isIntermediate()) {

          // only update to WAITX if the status is still in Published state
          if (this.mqIntfReqQueue.getReqStatus().contains(MQMsgConstants.REQ_STATUS_PUB)) {
            LOG.info("Intermediate reply receieved for Ref. " + reply.getRefNo() + ". Waiting for final status.");

            // for intermediate, we set to status = WAITX
            if (!StringUtils.isEmpty(reply.getDocRefNo())) {
              this.mqIntfReqQueue.setDocmRefnNo(reply.getDocRefNo());
              this.mqIntfReqQueue.setRefnSourceCd(reply.getSourceCode());
            }
            updateMQIntfStatus(MQMsgConstants.REQ_STATUS_WAIT + lastSequence);
            if (lastSequence == 1) {
              createPartialComment("Request acknowledged. Document Reference No: " + reply.getDocRefNo() + ", Source Code: " + reply.getSourceCode(),
                  cmrNo);
            }
          } else {
            LOG.warn("Intermediate reply receieved for Ref. " + reply.getRefNo() + " but the queue status is already "
                + this.mqIntfReqQueue.getReqStatus() + ". Ignoring..");
          }

        } else {
          LOG.info("CR reply received. Continuing processing..");
          if (StringUtils.isEmpty(this.mqIntfReqQueue.getDocmRefnNo())) {
            this.mqIntfReqQueue.setDocmRefnNo(reply.getDocRefNo());
          }
          if (StringUtils.isEmpty(this.mqIntfReqQueue.getRefnSourceCd())) {
            this.mqIntfReqQueue.setRefnSourceCd(reply.getSourceCode());
          }

          if ((lastSequence == 1 && MQMsgConstants.REQ_TYPE_CREATE.equals(this.mqIntfReqQueue.getReqType())) || (!StringUtils.isEmpty(cmrNo)
              && StringUtils.isEmpty(this.mqIntfReqQueue.getCmrNo()) && MQMsgConstants.REQ_TYPE_CREATE.equals(this.mqIntfReqQueue.getReqType()))) {
            // this is the first message after create, update CMR No and create
            // comment
            LOG.debug("Processing CMR No. from reply :  " + cmrNo);
            String addrNum = reply.getAddrSeq();
            this.mqIntfReqQueue.setCmrNo(cmrNo);
            this.entityManager.merge(mqIntfReqQueue);
            this.cmrData.setCmrNo(cmrNo);
            if (this.adminData != null && "Y".equals(this.adminData.getProspLegalInd())) {
              try {
                this.cmrData.setProspectSeqNo(Integer.parseInt(addrNum) + "");

              } catch (NumberFormatException e) {
                this.cmrData.setProspectSeqNo(addrNum);
              }

              MessageTransformer transformer = TransformerManager.getTransformer(this.cmrData.getCmrIssuingCntry());
              if (transformer != null && transformer.getFixedAddrSeqForProspectCreation() != null) {
                LOG.debug("Fixed Sequence No to use for Prospect Conversion is " + transformer.getFixedAddrSeqForProspectCreation());
                this.cmrData.setProspectSeqNo(transformer.getFixedAddrSeqForProspectCreation() + "");
              }

            }
            entityManager.merge(this.cmrData);
            createPartialComment(
                "CMR No. " + cmrNo + " has been assigned to the request (System Location " + this.mqIntfReqQueue.getCmrIssuingCntry() + ").", cmrNo);
          }

          Addr nextAddr = lastSequence >= 0 ? getNextAddressData(this.mqIntfReqQueue, lastSequence) : null;
          if (nextAddr != null) {
            LOG.debug("Other addresses pending, setting to COM" + lastSequence);
            updateMQIntfStatus(MQMsgConstants.REQ_STATUS_COM + lastSequence);
          } else {
            // no more addresses left, set to final COM status

            MessageTransformer transformer = TransformerManager.getTransformer(this.mqIntfReqQueue.getCmrIssuingCntry());
            if (transformer == null || transformer.shouldCompleteProcess(this.entityManager, this, MQMsgConstants.SOF_STATUS_SUCCESS, true)) {
              // there are no more addresses pending to be sent, abort sending
              // to
              // MQ
              // and complete the request
              LOG.debug("No more addresses left, setting to final COM status.");
              updateMQIntfStatus(MQMsgConstants.REQ_STATUS_COM);
              updateAdminRequest(MQMsgConstants.REQ_STATUS_COM, MQMsgConstants.PROCESSED_FLAG_Y, true);
              if (CmrConstants.REQ_TYPE_UPDATE.equals(this.mqIntfReqQueue.getReqType())) {
                createPartialComment(MQMsgConstants.WH_HIST_CMT_COM_UPDATE + cmrNo, cmrNo);
              } else {
                createPartialComment(MQMsgConstants.WH_HIST_CMT_COM + cmrNo, cmrNo);
              }
              this.skipPublish = true;
            } else {
              LOG.debug("Processing will continue. Transformer indicated non-completion of processing at this point");
            }
          }
        }
      } else {
        String errorMsg = reply.getErrorMsg();
        if (reply.isIntermediate()) {
          errorMsg = "A processing error occured. Pls check with your system administrator.";
        }
        LOG.debug("Error was encountered during processing. Sending request back to processor.");
        if (StringUtils.isEmpty(this.mqIntfReqQueue.getDocmRefnNo())) {
          this.mqIntfReqQueue.setDocmRefnNo(reply.getDocRefNo());
        }
        if (StringUtils.isEmpty(this.mqIntfReqQueue.getRefnSourceCd())) {
          this.mqIntfReqQueue.setRefnSourceCd(reply.getSourceCode());
        }
        updateMQIntfErrorStatus(MQMsgConstants.REQ_STATUS_SER + lastSequence, "001", errorMsg);
        updateAdminRequest(MQMsgConstants.REQ_STATUS_PPN, MQMsgConstants.PROCESSED_FLAG_E, false);
      }
    }
  }

  /**
   * Container for a WTAAS xml reply message
   * 
   * @author Jeffrey Zamora
   * 
   */
  public class WTAASReply {

    public static final String CR_STATUS_PROCESSED = "P";
    public static final String CR_STATUS_ERROR = "E";

    public static final String RY_STATUS_PROCESSED = "S";
    public static final String RY_STATUS_ERROR = "F";

    private String refNo;
    private String cmrIssuingCntry;
    private String docType;
    private String sourceCode;
    private String docRefNo;
    private String cmrNo;
    private String errorMsg;
    private String addrSeq;
    private String status;
    private boolean intermediate;

    public boolean isError() {
      return CR_STATUS_ERROR.equals(this.status) || RY_STATUS_ERROR.equals(this.status);
    }

    public String getRefNo() {
      return refNo;
    }

    public void setRefNo(String refNo) {
      this.refNo = refNo;
    }

    public String getCmrIssuingCntry() {
      return cmrIssuingCntry;
    }

    public void setCmrIssuingCntry(String cmrIssuingCntry) {
      this.cmrIssuingCntry = cmrIssuingCntry;
    }

    public String getDocType() {
      return docType;
    }

    public void setDocType(String docType) {
      this.docType = docType;
    }

    public String getSourceCode() {
      return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
      this.sourceCode = sourceCode;
    }

    public String getDocRefNo() {
      return docRefNo;
    }

    public void setDocRefNo(String docRefNo) {
      this.docRefNo = docRefNo;
    }

    public String getCmrNo() {
      return cmrNo;
    }

    public void setCmrNo(String cmrNo) {
      this.cmrNo = cmrNo;
    }

    public String getErrorMsg() {
      return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
      this.errorMsg = errorMsg;
    }

    public String getAddrSeq() {
      return addrSeq;
    }

    public void setAddrSeq(String addrSeq) {
      this.addrSeq = addrSeq;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public boolean isIntermediate() {
      return intermediate;
    }

    public void setIntermediate(boolean intermediate) {
      this.intermediate = intermediate;
    }
  }

  /**
   * Parser for a WTAAS xml reply message
   * 
   * @author Jeffrey Zamora
   * 
   */
  public class WTAASResponseHandler extends DefaultHandler {

    private WTAASReply reply;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      this.reply = new WTAASReply();
      if (WTAAS_RESPONSE_ROOT_ELEM.equals(qName)) {
        this.reply.setCmrIssuingCntry(attributes.getValue("CntryNo"));
        this.reply.setCmrNo(attributes.getValue("CustNo"));
        this.reply.setDocRefNo(attributes.getValue("DocRefNo"));
        this.reply.setDocType(attributes.getValue("DocType"));
        this.reply.setErrorMsg(attributes.getValue("ErrorMsg"));
        this.reply.setRefNo(attributes.getValue("CMRRefNo"));
        this.reply.setSourceCode(attributes.getValue("SourceCode"));
        this.reply.setStatus(attributes.getValue("Status"));
      } else if (WTAAS_INT_RESPONSE_ROOT_ELEM.equals(qName)) {
        this.reply.setErrorMsg(attributes.getValue("Status_Desc"));
        this.reply.setDocRefNo(attributes.getValue("DocRefNo"));
        this.reply.setRefNo(attributes.getValue("CMRRefNo"));
        this.reply.setSourceCode(attributes.getValue("SourceCode"));
        this.reply.setStatus(attributes.getValue("Status"));
        this.reply.setIntermediate(true);
      } else if (WTAAS_INT_ERROR_ROOT_ELEM.equals(qName)) {
        this.reply.setRefNo(attributes.getValue("CMRRefNo"));
        this.reply.setDocRefNo(attributes.getValue("DocRefNo"));
        this.reply.setStatus(attributes.getValue("Status"));
        this.reply.setSourceCode(attributes.getValue("SourceCode"));
        this.reply.setErrorMsg(attributes.getValue("Error_Msg"));
        this.reply.setIntermediate(true);
      } else {
        // unhandled XML type
        this.reply.setRefNo(attributes.getValue("CMRRefNo"));
        this.reply.setStatus(WTAASReply.CR_STATUS_ERROR);
        this.reply.setErrorMsg("Unknown reply received from WTAAS. Message: "
            + (attributes.getValue("Error_Msg") != null ? attributes.getValue("Error_Msg") : "(none)"));
      }
      String uniqueNum = this.reply.getRefNo();
      if (StringUtils.isNumeric(uniqueNum)) {
        this.reply.setRefNo(String.valueOf(Integer.parseInt(uniqueNum)));
      }

      if (StringUtils.isEmpty(this.reply.getDocRefNo())) {
        this.reply.setDocRefNo("(not specified)");
      }

    }

    public WTAASReply getReply() {
      return reply;
    }

  }

  @Override
  public void retrieveCurrentValues() {

    if (!CmrConstants.REQ_TYPE_UPDATE.equals(this.mqIntfReqQueue.getReqType())) {
      return;
    }
    String cmrIssuingCntry = this.mqIntfReqQueue.getCmrIssuingCntry();
    String cmrNo = this.mqIntfReqQueue.getCmrNo();

    try {
      WtaasClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), WtaasClient.class);

      WtaasQueryRequest request = new WtaasQueryRequest();
      request.setCmrNo(cmrNo);
      request.setCountry(cmrIssuingCntry);

      WtaasQueryResponse response = client.executeAndWrap(WtaasClient.QUERY_ID, request, WtaasQueryResponse.class);
      if (response == null || !response.isSuccess()) {
        LOG.warn("Error or no response from WTAAS query.");
        return;
      }

      this.currentRecord = WtaasRecord.createFrom(response);

    } catch (Exception e) {
      LOG.warn("An error has occurred during retrieval of the values.", e);
    }
  }

  @Override
  public boolean retrySupported() {
    return true;
  }

  public static boolean isHandled(String xmlData) {
    if (xmlData == null) {
      return false;
    }
    if (xmlData.contains("<" + WTAAS_RESPONSE_ROOT_ELEM) || xmlData.contains("<" + WTAAS_INT_RESPONSE_ROOT_ELEM)
        || xmlData.contains("<" + WTAAS_INT_ERROR_ROOT_ELEM)) {
      return true;
    }
    return false;
  }

  public static String extractUniqueNumber(String xmlData) throws Exception {
    LOG.trace("The XML from MQ is " + xmlData);
    WTAASReply reply = null;
    WTAASMessageHandler msgHandler = new WTAASMessageHandler();
    WTAASResponseHandler handler = msgHandler.new WTAASResponseHandler();
    StringReader read = new StringReader(xmlData);
    try {
      InputSource source = new InputSource(read);

      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.newSAXParser().parse(source, handler);

      reply = handler.getReply();

      String uniqueNum = reply.getRefNo();

      if (uniqueNum.startsWith("G")) {
        return uniqueNum;
      }
      if (StringUtils.isNumeric(uniqueNum)) {
        return String.valueOf(Integer.parseInt(uniqueNum));
      } else {
        String test = uniqueNum.substring(0, uniqueNum.length() - 1);
        if (StringUtils.isNumeric(test)) {
          return String.valueOf(Integer.parseInt(test));
        }
        if (test.contains("G")) {
          return test.substring(test.indexOf("G")); // newco
        }
        return uniqueNum;
      }
    } finally {
      read.close();
    }
  }

  /**
   * Checks if the ZS01 address was updated
   * 
   * @return
   */
  private boolean isMainAddrUpdated() {
    for (Addr addr : this.currentAddresses) {
      if ("ZS01".equals(addr.getId().getAddrType()) && "Y".equals(addr.getChangedIndc())) {
        return true;
      }
      // if (!"Y".equals(addr.getImportInd())) {
      // // a new address was added, need to recompute address use
      // return true;
      // }
    }
    return false;
  }

  /**
   * Gets the next sequence for updates
   * 
   * @param currSequence
   * @return
   */
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
      updated = "Y".equals(addr.getChangedIndc()) || !"Y".equals(addr.getImportInd());
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

  public XMLOutputter getXmlOutputter(Format format) {
    AttributesPerLineOutputter outputter = new AttributesPerLineOutputter(1);
    XMLOutputter fmt = new XMLOutputter(format, outputter);
    return fmt;
  }

}
