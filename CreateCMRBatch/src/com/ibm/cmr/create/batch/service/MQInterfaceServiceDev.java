/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.util.MQProcessUtil;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQTransport;
import com.ibm.cmr.create.batch.util.mq.RecordCollector;
import com.ibm.cmr.create.batch.util.mq.config.MQConfig;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.handler.MessageHandlerFactory;

/**
 * Helper class for {@link MQInterfaceService} to be used by developers
 * 
 * @author JeffZAMORA
 * 
 */
public class MQInterfaceServiceDev extends MQInterfaceService {

  private Map<Long, String> queryIdTargetMap = new HashMap<Long, String>();
  private Map<Long, Integer> queryIdSize = new HashMap<Long, Integer>();
  private long reqId;

  @Override
  protected int publishRecordsToMQ(EntityManager entityManager) throws Exception {

    // collect first any pending messages
    LOG.info("Picking up processing pending records for MQ Interface...");
    RecordCollector collector = new RecordCollector();
    collector.processMQRequest(entityManager);

    // delete old xmls
    LOG.info("Cleaning XML files...");
    int cleanPeriod = Integer.parseInt(BatchUtil.getProperty("XMLOVERLAMPDAYS"));
    String outPath = BatchUtil.getProperty("XMLOUTPATH");
    String cmrHome = SystemConfiguration.getValue("CMR_HOME");
    if (!StringUtils.isEmpty(cmrHome)) {
      outPath = cmrHome + "/createcmr/xml/sof/output/";
    }
    if (!MQProcessUtil.deleteFile(outPath, cleanPeriod)) {
      LOG.warn("Can't delete XML files in " + outPath);
    }

    // get pending data from MQ_INTF_REQ_QUEUE
    LOG.info("Getting pending files from interface queue...");
    List<MqIntfReqQueue> pendingList = getRecordsToPublish(entityManager);

    pendingList.addAll(processRetryRecords(entityManager));

    if (this.reqId > 0) {
      LOG.debug("Filtering dev run for Request ID " + this.reqId);
      MqIntfReqQueue reqIdSelected = null;
      for (MqIntfReqQueue queue : pendingList) {
        if (queue.getReqId() == this.reqId) {
          reqIdSelected = queue;
          break;
        }
      }
      pendingList.clear();
      if (reqIdSelected != null) {
        pendingList.add(reqIdSelected);
      }

    }

    if (pendingList == null || pendingList.isEmpty()) {
      LOG.info("Nothing to publish at this point");
      return 0;
    }

    LOG.info("Initializing Country Map..");
    LandedCountryMap.init(entityManager);

    // publish to correct MQ
    int publishCount = 0;
    Map<String, List<MqIntfReqQueue>> pending = distributeRecordsToConfig(pendingList);
    if (!pending.isEmpty()) {
      MQConfig mqConfig = null;
      // Properties mqProps = null;
      MQTransport transport = null;
      for (String configName : pending.keySet()) {
        // for each configuration, start only 1 MQ transport
        if (pending.get(configName) != null && !pending.get(configName).isEmpty()) {
          mqConfig = MQConfig.getConfig(configName);
          if (mqConfig != null) {
            // mqProps = mqConfig.toEnvProperties(false);

            // LOG.debug(configName + " :: Opening Qmgr: " + mqConfig.getqMqr()
            // + " Queue: " + mqConfig.getOutputQueue());
            // transport = new MQTransport(mqProps, mqConfig.getCcsid());
            try {
              int count = 0;
              for (MqIntfReqQueue queueRecord : pending.get(configName)) {
                LOG.debug("Processing Queue Message " + queueRecord.getId().getQueryReqId() + " Request ID " + queueRecord.getReqId() + " Status "
                    + queueRecord.getReqStatus());
                count = publishMessage(entityManager, queueRecord, transport);
                publishCount += count;
              }
            } finally {
              // LOG.debug("Closing queue manager.");
              // transport.closeMQQueue();
              // transport.closeMQQManager();
            }
          }
        } else {
          LOG.debug("No message to send to MQ config " + configName);
        }
      }
    } else {
      LOG.warn("No record to process with correct MQ configuration.");
    }
    return publishCount;
  }

  @Override
  protected int publishMessage(EntityManager entityManager, MqIntfReqQueue queueRecord, MQTransport transport) throws Exception {
    LOG.debug("Publishing message to queue..");
    MQMessageHandler msgHandler = MessageHandlerFactory.createMessageHandler(entityManager, queueRecord);
    if (msgHandler == null) {
      LOG.warn("No message handler defined for " + queueRecord.getCmrIssuingCntry());
      return 0;
    }
    String xmlString = "";
    try {
      xmlString = msgHandler.buildMQMessage();
    } catch (Exception e) {
      LOG.debug("An error occurred while building MQ message for  " + queueRecord.getId().getQueryReqId() + " Request ID: " + queueRecord.getReqId(),
          e);
      msgHandler.updateMQIntfReqStatus(false);
      return 0;
    }

    if (msgHandler.skipPublish) {
      LOG.info("MQ Message Handler indicated to skip publishing. Ending process.");
      return 0;
    }

    boolean sendSuccess = false;
    boolean flag = true;
    int counter = 0;
    while (flag) {
      try {
        LOG.info("DUMMY Send message...");
        LOG.debug("Message: " + xmlString);
        sendSuccess = true;// transport.sendMessage(xmlString);
        LOG.debug("Putting: " + queueRecord.getId().getQueryReqId() + " / " + queueRecord.getTargetSys() + "," + queueRecord.getCmrIssuingCntry()
            + "," + queueRecord.getCmrNo());
        this.queryIdTargetMap.put(queueRecord.getId().getQueryReqId(), queueRecord.getTargetSys() + "," + queueRecord.getCmrIssuingCntry() + ","
            + queueRecord.getCmrNo());
        Integer curr = this.queryIdSize.get(queueRecord.getId().getQueryReqId());
        if (curr == null) {
          curr = 0;
        }
        this.queryIdSize.put(queueRecord.getId().getQueryReqId(), curr + 1);
        LOG.info("Message sent.");
        if (sendSuccess) {
          LOG.debug("DUMMY MQ commit..");
          // transport.commit();
        }
        flag = false;
        msgHandler.updateMQIntfReqStatus(true);
        return 1;
      } catch (Exception e) {
        LOG.debug("DUMMY MQ backout..");
        // transport.backout();
        counter++;
        LOG.error("An error occurred while sending. " + e.getMessage(), e);
        if (counter == 3) {
          flag = false;
          LOG.debug("Message cannot be sent in 3 attempts");
          LOG.debug("Error while putting message to queue.", e);
          msgHandler.updateMQIntfReqStatus(false);
        } else {
          LOG.debug("Retrying to send message..");
        }
      }
    }
    return 0;

  }

  @Override
  protected int processMessagesFromMQ(EntityManager entityManager) throws Exception {

    // delete old xmls
    LOG.info("Cleaning XML files...");
    int cleanPeriod = Integer.parseInt(BatchUtil.getProperty("XMLOVERLAMPDAYS"));
    String inPath = BatchUtil.getProperty("XMLINPATH");
    String cmrHome = SystemConfiguration.getValue("CMR_HOME");
    if (!StringUtils.isEmpty(cmrHome)) {
      inPath = cmrHome + "/createcmr/xml/sof/input/";
    }

    if (!MQProcessUtil.deleteFile(inPath, cleanPeriod)) {
      LOG.warn("Can't delete XML files in " + inPath);
    }

    MQConfig mqConfig = null;

    List<String> allData = new ArrayList<String>();
    List<String> mqData = null;
    LOG.info("Retreiving all messages from defined queues..");
    for (String configName : MQConfig.getConfigNames()) {
      mqConfig = MQConfig.getConfig(configName);
      if (mqConfig != null) {

        if (NO_RETRIEVE.equals(mqConfig.getInputQueue())) {
          // LOG.debug("Configuration " + configName +
          // " not configured to receieve messages. Skipping.");
        } else {
          // mqProps = mqConfig.toEnvProperties(true);

          // LOG.debug(configName + " :: Opening Qmgr: " + mqConfig.getqMqr() +
          // " Queue: " + mqConfig.getInputQueue());
          // transport = new MQTransport(mqProps, mqConfig.getCcsid());
          // transport.open(true);
          try {
            // mqData = transport.getAllMessage(batchSize);
            mqData = buildDummyMQReplies();
            if (mqData != null) {
              LOG.debug("Adding " + mqData.size() + " records from " + configName + " config.");
              allData.addAll(mqData);
            }
          } finally {
            // LOG.debug("Closing queue manager.");
            // transport.closeMQQueue();
            // transport.closeMQQManager();
          }
        }
      }
    }

    if (allData.isEmpty()) {
      LOG.info("No pending MQ messages to process.");
      return 0;
    } else {
      LOG.info("Processing " + allData.size() + " messages from MQ.");

      String uniqueNum = null;
      MqIntfReqQueue mqIntfReqQueue = null;
      MQMessageHandler msgHandler = null;
      int collectCount = 0;
      for (String xmlString : allData) {
        LOG.debug("Processing XML :" + xmlString);
        try {
          if (xmlString.indexOf("<") >= 0) {
            uniqueNum = getUniqueNumber(xmlString);
            LOG.debug("The UniqueNumber is : " + uniqueNum);

            if (uniqueNum != null) {
              mqIntfReqQueue = getQueueRecordById(entityManager, uniqueNum);
              if (mqIntfReqQueue != null) {
                LOG.debug("Processing MQ ID " + mqIntfReqQueue.getId().getQueryReqId());
                msgHandler = MessageHandlerFactory.createMessageHandler(entityManager, mqIntfReqQueue);
                msgHandler.processMQMessage(xmlString);
                collectCount++;
              } else {
                LOG.warn("Queue Request " + uniqueNum + " cannot be located.");
                saveUnrecognizedMsg(xmlString);
              }
            } else {
              LOG.warn("Incorrect or empty unique number format. Cannot process message.");
              saveUnrecognizedMsg(xmlString);
            }
          } else {
            LOG.warn("Message is not an XML. Skipping.");
            saveUnrecognizedMsg(xmlString);
          }
        } catch (Exception e) {
          LOG.error("Error in processing MQ message with uniqeNum " + uniqueNum, e);
          saveUnrecognizedMsg(xmlString);
        }
      }
      return collectCount;
    }

  }

  private List<String> buildDummyMQReplies() {
    String[] parts = null;
    String system = null;
    String country = null;
    String cmrNo = null;
    List<String> dummyData = new ArrayList<String>();
    for (Long queryId : this.queryIdTargetMap.keySet()) {
      LOG.debug("Building dummy reply for Query ID " + queryId);
      parts = this.queryIdTargetMap.get(queryId).split(",");
      system = parts[0];
      country = parts[1];
      cmrNo = parts[2];
      if (StringUtils.isEmpty(cmrNo) || "NULL".equals(cmrNo.toUpperCase().trim())) {
        cmrNo = null;
      }
      LOG.debug("Query ID " + queryId + " Target: " + system + " Country: " + country + " CMR No: " + cmrNo);
      switch (system) {
      case "SOF":
        dummyData.add(buildSOFReply(queryId, country, cmrNo));
        break;
      case "WTAAS":
        dummyData.add(buildIntermediateWTAASReply(queryId, country, cmrNo));
        dummyData.add(buildWTAASReply(queryId, country, cmrNo));
        break;
      }
    }
    this.queryIdTargetMap.clear();
    return dummyData;
  }

  private String buildWTAASReply(Long queryId, String country, String cmrNo) {
    String refNo = queryId + "";
    Integer size = this.queryIdSize.get(queryId);
    if (size == 0) {
      size = 1;
    }
    refNo += String.valueOf((char) (64 + size));
    refNo = StringUtils.leftPad(refNo + "", 10, '0');
    String dummyCN = queryId + "";
    if (dummyCN.length() > 6) {
      dummyCN = dummyCN.substring(dummyCN.length() - 6, dummyCN.length());
    }
    if (cmrNo != null) {
      dummyCN = cmrNo;
    }
    dummyCN = StringUtils.leftPad(dummyCN, 6, '0');
    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    xml.append("<CR020CR");
    xml.append("   CMRRefNo=\"" + refNo + "\"");
    xml.append("   CntryNo=\"" + country + "\"");
    xml.append("   DocType=\"CR\"");
    xml.append("   SourceCode=\"XXX\"");
    xml.append("   DocRefNo=\"DUMMYREF\"");
    xml.append("   CustNo=\"" + dummyCN + "\"");
    xml.append("   Status=\"P\"");
    xml.append("   ErrorMsg=\" \"");
    xml.append(" />");
    xml.append("");
    return xml.toString();
  }

  private String buildIntermediateWTAASReply(Long queryId, String country, String cmrNo) {
    String refNo = queryId + "";
    Integer size = this.queryIdSize.get(queryId);
    if (size == 0) {
      size = 1;
    }
    refNo += String.valueOf((char) (64 + size));
    refNo = StringUtils.leftPad(refNo + "", 10, '0');
    String dummyCN = queryId + "";
    if (dummyCN.length() > 6) {
      dummyCN = dummyCN.substring(dummyCN.length() - 6, dummyCN.length());
    }
    if (cmrNo != null) {
      dummyCN = cmrNo;
    }
    dummyCN = StringUtils.leftPad(dummyCN, 6, '0');
    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    xml.append("<RY090WM");
    xml.append("   CMRRefNo=\"" + refNo + "\"");
    xml.append("   Status=\"S\"");
    xml.append("   Status_Desc=\"Success\"");
    xml.append("   SourceCode=\"XXX\"");
    xml.append("   DocRefNo=\"DUMMYREF\"");
    xml.append("   DocDate=\"000000\"");
    xml.append("   DocType=\"RY\"");
    xml.append(" />");
    xml.append("");
    return xml.toString();
  }

  private String buildSOFReply(Long queryId, String country, String cmrNo) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    String refNo = StringUtils.leftPad(queryId + "", 8, '0');
    String dummyCN = queryId + "";
    if (dummyCN.length() > 6) {
      dummyCN = dummyCN.substring(dummyCN.length() - 6, dummyCN.length());
    }
    if (cmrNo != null) {
      dummyCN = cmrNo;
    }
    dummyCN = StringUtils.leftPad(dummyCN, 6, '0');
    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    xml.append("<softordc>");
    xml.append("  <document form=\"xmlDoc\">");
    xml.append("    <created>");
    xml.append("      <datetime dst=\"true\">" + sdf.format(new Date()) + "</datetime>");
    xml.append("    </created>");
    xml.append("    <item name=\"Country\">");
    xml.append("      <text>" + country + "</text>");
    xml.append("    </item>");
    xml.append("    <item name=\"UniqueNumber\">");
    xml.append("      <text>" + queryId + "</text>");
    xml.append("    </item>");
    xml.append("    <item name=\"XML_DocumentNumber\">");
    xml.append("      <text>CRF" + refNo + "</text>");
    xml.append("    </item>");
    xml.append("    <item name=\"Status\">");
    xml.append("      <text>CRP</text>");
    xml.append("    </item>");
    xml.append("    <item name=\"CustomerNo\">");
    xml.append("      <text>" + dummyCN + "</text>");
    xml.append("    </item>");
    xml.append("    <item name=\"AddressNo\">");
    xml.append("      <text></text>");
    xml.append("    </item>");
    xml.append("    <item name=\"Message\">");
    xml.append("      <text />");
    xml.append("    </item>");
    xml.append("  </document>");
    xml.append("</softordc>");
    xml.append("");
    return xml.toString();
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

}
