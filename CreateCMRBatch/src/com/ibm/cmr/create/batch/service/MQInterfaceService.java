/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.jdom.JDOMException;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueuePK;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.MQProcessUtil;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.MQTransport;
import com.ibm.cmr.create.batch.util.mq.RecordCollector;
import com.ibm.cmr.create.batch.util.mq.config.MQConfig;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.handler.MessageHandlerFactory;

/**
 * Generic MQ Interface that should be able to handle countries connecting to
 * different QMgrs and Queues
 * 
 * @author Jeffrey Zamora
 * 
 */
public class MQInterfaceService extends BaseBatchService {

  public static final String PUBLISH = "PUB";
  public static final String SUBSCRIBE = "SUB";
  public static final String NO_RETRIEVE = "X";

  private static final int ONE_MINUTE = 1000 * 60;
  private static final int ONE_HOUR = ONE_MINUTE * 60;

  private String mode;

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {

    LOG.info("Initializing MQ Configurations...");
    MQConfig.initConfigurations();

    long WAIT_TIME = 8000; // 8 seconds

    String waitTime = BatchUtil.getProperty("MQ_WAIT_TIME");
    if (!StringUtils.isEmpty(waitTime) && StringUtils.isNumeric(waitTime)) {
      WAIT_TIME = Long.parseLong(waitTime);
    }
    int WAIT_COUNT = 10; // wait 10 times for 8 second intervals

    String waitCnt = BatchUtil.getProperty("MQ_WAIT_COUNT");
    if (!StringUtils.isEmpty(waitCnt) && StringUtils.isNumeric(waitCnt)) {
      WAIT_COUNT = Integer.parseInt(waitCnt);
    }

    LOG.debug("MQ Wait Time: " + WAIT_TIME);
    LOG.debug("MQ Retry Count: " + WAIT_COUNT);

    if (PUBLISH.equals(this.mode)) {
      boolean continueProcessing = true;
      while (continueProcessing) {
        entityManager.clear();
        int published = publishRecordsToMQ(entityManager);
        if (published != 0) {
          partialCommit(entityManager);
        }

        int waitCount = published == 0 ? WAIT_COUNT : 1;
        LOG.info("Publish complete, message processing starting..");
        boolean collected = false;
        while (waitCount <= WAIT_COUNT && !collected) {
          Thread.sleep(WAIT_TIME);
          LOG.debug("Checking responses..");
          int messages = processMessagesFromMQ(entityManager);
          if (messages == 0) {
            waitCount++;
          } else {
            LOG.debug("Messages collected and processed. Redoing publish..");
            collected = true;
            partialCommit(entityManager);
          }
        }

        if (!collected && published == 0) {
          continueProcessing = false;
          LOG.debug("No messages published and collected for the period. Exiting service.");
        } else {
          LOG.debug("Messages were published and collected. Recycling...");
          Thread.sleep(1000);
        }
      }
    } else if (SUBSCRIBE.equals(this.mode)) {
      processMessagesFromMQ(entityManager);
    } else {
      LOG.warn("No mode specified for MQ interface.");
    }
    return true;
  }

  /**
   * Publishes data to the defined MQ for that country. Performs the following:<br>
   * <ol>
   * <li>Get any Processing Pending records for MQ then create records under
   * MQ_INTF_REQ_QUEUE</li>
   * <li>Get the pending items under MQ_INTF_REQ_QUEUE</li>
   * <li>Connect to the correct MQ for that country where to publish the record
   * and put the message</li>
   * </ol>
   * 
   * @param entityManager
   * @throws Exception
   */
  protected int publishRecordsToMQ(EntityManager entityManager) throws Exception {

    // collect first any pending messages
    LOG.info("Picking up processing pending records for MQ Interface...");
    RecordCollector collector = new RecordCollector();
    collector.processMQRequest(entityManager);

    LOG.debug("Records collected, committing..");
    partialCommit(entityManager);

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
      Properties mqProps = null;
      MQTransport transport = null;
      for (String configName : pending.keySet()) {
        // for each configuration, start only 1 MQ transport
        if (pending.get(configName) != null && !pending.get(configName).isEmpty()) {
          mqConfig = MQConfig.getConfig(configName);
          if (mqConfig != null) {
            mqProps = mqConfig.toEnvProperties(false);

            LOG.debug(configName + " :: Opening Qmgr: " + mqConfig.getqMqr() + " Queue: " + mqConfig.getOutputQueue());
            transport = new MQTransport(mqProps, mqConfig.getCcsid());
            transport.open(false);
            try {
              int count = 0;
              for (MqIntfReqQueue queueRecord : pending.get(configName)) {
                LOG.debug("Processing Queue Message " + queueRecord.getId().getQueryReqId() + " Request ID " + queueRecord.getReqId() + " Status "
                    + queueRecord.getReqStatus());
                try {
                  count = publishMessage(entityManager, queueRecord, transport);
                  publishCount += count;
                  partialCommit(entityManager);
                } catch (Exception e) {
                  LOG.warn("Error has been encountered for MQ Request ID "+queueRecord.getId().getQueryReqId(),e);
                  partialRollback(entityManager);
                }
              }
            } finally {
              LOG.debug("Closing queue manager.");
              transport.closeMQQueue();
              transport.closeMQQManager();
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

  /**
   * Publishes the record to the MQ and updates the statuses accordingly
   * 
   * @param entityManager
   * @param queueRecord
   * @param transport
   * @throws Exception
   */
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
        LOG.info("Sending message...");
        sendSuccess = transport.sendMessage(xmlString);
        LOG.info("Message sent.");
        if (sendSuccess) {
          transport.commit();
        }
        flag = false;
        msgHandler.updateMQIntfReqStatus(true);
        return 1;
      } catch (Exception e) {
        transport.backout();
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

  /**
   * Divides the pending list according to the MQ configuration for that
   * particular CMR issuing country
   * 
   * @param pendingList
   * @return
   */
  public Map<String, List<MqIntfReqQueue>> distributeRecordsToConfig(List<MqIntfReqQueue> pendingList) {
    String country = null;
    String configName = null;
    Map<String, List<MqIntfReqQueue>> dividedMap = new HashMap<String, List<MqIntfReqQueue>>();
    for (MqIntfReqQueue pending : pendingList) {
      country = pending.getCmrIssuingCntry();
      configName = MQConfig.getConfigName(country);
      if (configName != null) {
        if (!dividedMap.containsKey(configName)) {
          dividedMap.put(configName, new ArrayList<MqIntfReqQueue>());
        }
        dividedMap.get(configName).add(pending);
      } else {
        LOG.warn("CMR Issuing Country " + country + " is on the pending list but has no defined MQ configuration. Skipping Record "
            + pending.getId().getQueryReqId() + " for Request ID " + pending.getReqId());
      }
    }
    return dividedMap;
  }

  /**
   * Connects to all queues defined and gets all pending messages. Proceeds to
   * update the status of the MQ_INTF_REQ_QUEUE message afterwards
   * 
   * @param entityManager
   * @throws Exception
   */
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
    Properties mqProps = null;
    MQTransport transport = null;

    int batchSize = Integer.parseInt(BatchUtil.getProperty("SUBBATCH"));
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
          mqProps = mqConfig.toEnvProperties(true);

          LOG.debug(configName + " :: Opening Qmgr: " + mqConfig.getqMqr() + " Queue: " + mqConfig.getInputQueue());
          transport = new MQTransport(mqProps, mqConfig.getCcsid());
          transport.open(true);
          try {
            mqData = transport.getAllMessage(batchSize);
            if (mqData != null) {
              LOG.debug("Adding " + mqData.size() + " records from " + configName + " config.");
              allData.addAll(mqData);
            }
          } finally {
            LOG.debug("Closing queue manager.");
            transport.closeMQQueue();
            transport.closeMQQManager();
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
                try {
                  msgHandler.processMQMessage(xmlString);
                  partialCommit(entityManager);
                } catch (Exception e) {
                  LOG.warn("Error has been encountered for MQ Request ID "+mqIntfReqQueue.getId().getQueryReqId(),e);
                  partialRollback(entityManager);
                }
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

  /**
   * Extracts the Unique Number from the XML
   * 
   * @param xmlStr
   * @return
   * @throws Exception
   */

  public String getUniqueNumber(String xmlStr) throws Exception {

    return MessageHandlerFactory.extractUniqueNumber(xmlStr);
  }

  /**
   * Gets the {@link MqIntfReqQueue} record based on the uniqueNumber
   * 
   * @param entityManager
   * @param queueReqId
   * @return
   */
  protected MqIntfReqQueue getQueueRecordById(EntityManager entityManager, String queueReqId) {

    MqIntfReqQueue mqIntfReqQueue = null;
    MqIntfReqQueuePK mqIntfReqQueuePK = new MqIntfReqQueuePK();
    mqIntfReqQueuePK.setQueryReqId(Long.parseLong(queueReqId));

    mqIntfReqQueue = entityManager.find(MqIntfReqQueue.class, mqIntfReqQueuePK);
    return mqIntfReqQueue;
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  /**
   * Saves XMLs received from the Queue that were not formatted properly
   * 
   * @param xml
   * @throws FileNotFoundException
   * @throws IOException
   * @throws JDOMException
   */
  protected void saveUnrecognizedMsg(String message) {
    String xmlPath = BatchUtil.getProperty("XMLETCPATH");
    String cmrHome = SystemConfiguration.getValue("CMR_HOME");
    if (!StringUtils.isEmpty(cmrHome)) {
      xmlPath = cmrHome + "/createcmr/xml/sof/etc/";
    }
    File outDir = new File(xmlPath);
    if (!outDir.exists()) {
      outDir.mkdirs();
    }

    LOG.info("Saving OTHER message received from queue..");
    String outPath = xmlPath + "ETC_MESSAGES.txt";

    try {
      FileOutputStream fos = new FileOutputStream(outPath, true);
      try {
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        try {
          PrintWriter pw = new PrintWriter(osw);
          try {
            pw.println(MQMsgConstants.FILE_TIME_FORMATTER.format(new Date()) + ": Message receieved.");
            pw.println(message);
            pw.println("-------");
            pw.println();
            pw.println();
          } finally {
            pw.close();
          }
        } finally {
          osw.close();
        }
      } finally {
        fos.close();
      }
    } catch (Exception e) {
      LOG.warn("OTHER message cannot be saved. " + e.getMessage());
    }
  }

  /**
   * Collects pending records to be queued on the {@link MqIntfReqQueue} table
   * 
   * @param entityManager
   * @param limitRecords
   * @return
   */
  public List<MqIntfReqQueue> getRecordsToPublish(EntityManager entityManager) {
    List<MqIntfReqQueue> queue = new ArrayList<MqIntfReqQueue>();

    String sql = ExternalizedQuery.getSql("MQREQUEST.GETMQREQ");

    try {
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      queue.addAll(query.getResults(MqIntfReqQueue.class));
      LOG.info("The MQ request size is " + queue.size());
    } catch (Exception e) {
      LOG.error("Error in retrieving pending records.", e);
    }
    return queue;
  }

  /**
   * Collects pending records to be queued on the {@link MqIntfReqQueue} table
   * 
   * @param entityManager
   * @param limitRecords
   * @return
   */
  public List<MqIntfReqQueue> processRetryRecords(EntityManager entityManager) {
    List<MqIntfReqQueue> queue = new ArrayList<MqIntfReqQueue>();
    List<MqIntfReqQueue> resendQueue = new ArrayList<MqIntfReqQueue>();

    LOG.debug("Processing records for retry");
    String sql = ExternalizedQuery.getSql("MQREQUEST.GETMQREQ.RETRY");

    MQMessageHandler handler = null;
    try {
      // try to resend / retry records
      // Retry = every few mins
      // Resend = every few hours
      long currTime = SystemUtil.getActualTimestamp().getTime();

      int retryPeriod = 0;
      int retryWaitPeriod = 0;
      int resendPeriod = 0;
      int resendWaitPeriod = 0;

      PreparedQuery query = new PreparedQuery(entityManager, sql);
      queue = query.getResults(MqIntfReqQueue.class);
      if (queue != null && !queue.isEmpty()) {

        // two iterations, one just to update PUBx to RETRY
        for (MqIntfReqQueue record : queue) {
          handler = MessageHandlerFactory.createMessageHandler(entityManager, record);
          if (handler != null && handler.retrySupported() && record.getReqStatus().startsWith(MQMsgConstants.REQ_STATUS_PUB)) {
            // request is in PUBx status for longer than the retry/resend wait
            // period, set to RETRY / RESEND

            resendWaitPeriod = SystemParameters.getInt(record.getTargetSys() + ".RETRY.WAIT");
            if (resendWaitPeriod <= 0) {
              resendWaitPeriod = Integer.parseInt(BatchUtil.getProperty("DEFAULT_RETRY_WAIT_PERIOD"));
            }
            resendWaitPeriod = resendWaitPeriod * ONE_HOUR;

            retryWaitPeriod = SystemParameters.getInt(record.getTargetSys() + ".RESEND.WAIT");
            if (retryWaitPeriod <= 0) {
              retryWaitPeriod = Integer.parseInt(BatchUtil.getProperty("DEFAULT_RETRY_WAIT_PERIOD"));
            }
            retryWaitPeriod = retryWaitPeriod * ONE_MINUTE;
            long lastUpdated = record.getLastUpdtTs().getTime();

            long created = record.getCreateTs().getTime();

            if ((currTime - created) > resendWaitPeriod) {
              LOG.debug("Record " + record.getId().getQueryReqId() + " has been waiting long enough, setting to RESEND.");
              record.setReqStatus(MQMsgConstants.REQ_STATUS_RESEND);
              record.setCreateTs(SystemUtil.getCurrentTimestamp());
              record.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
              record.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
              updateEntity(record, entityManager);
            } else if ((currTime - lastUpdated) > retryWaitPeriod) {
              LOG.debug("Record " + record.getId().getQueryReqId() + " has been pending a while, setting to RETRY.");
              record.setReqStatus(MQMsgConstants.REQ_STATUS_RETRY);
              record.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
              record.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
              updateEntity(record, entityManager);
            }
          }
        }

        for (MqIntfReqQueue record : queue) {
          if (MQMsgConstants.REQ_STATUS_RETRY.equals(record.getReqStatus())) {
            retryPeriod = SystemParameters.getInt(record.getTargetSys() + ".RETRY");
            if (retryPeriod <= 0) {
              retryPeriod = Integer.parseInt(BatchUtil.getProperty("DEFAULT_RETRY_PERIOD"));
            }
            retryPeriod = retryPeriod * ONE_MINUTE;
            long lastUpdated = record.getLastUpdtTs().getTime();

            if ((currTime - lastUpdated) > retryPeriod) {
              LOG.debug("Record " + record.getId().getQueryReqId() + " is for RETRY. Adding to queue.");
              resendQueue.add(record);
            }
          }
          if (MQMsgConstants.REQ_STATUS_RESEND.equals(record.getReqStatus())) {
            resendPeriod = SystemParameters.getInt(record.getTargetSys() + ".RESEND");
            if (resendPeriod <= 0) {
              resendPeriod = Integer.parseInt(BatchUtil.getProperty("DEFAULT_RESEND_PERIOD"));
            }
            resendPeriod = resendPeriod * ONE_HOUR;
            long lastUpdated = record.getLastUpdtTs().getTime();

            if ((currTime - lastUpdated) > resendPeriod) {
              LOG.debug("Record " + record.getId().getQueryReqId() + " is for RESEND. Adding to queue.");
              resendQueue.add(record);
            }
          }
        }
      }
      LOG.info("The MQ retry request size is " + resendQueue.size());
    } catch (Exception e) {
      LOG.error("Error in retrieving pending records.", e);
    }
    return resendQueue;
  }

  @Override
  protected boolean useServicesConnections() {
    return true;
  }
}
