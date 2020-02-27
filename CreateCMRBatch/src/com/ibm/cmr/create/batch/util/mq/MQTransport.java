package com.ibm.cmr.create.batch.util.mq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

public class MQTransport {

  private Logger LOG = Logger.getLogger(MQTransport.class);

  private MQQueueManager qManager = null;
  private MQQueue mQQueue = null;
  private final String qmName;
  private final String qName;
  private int queuedepth = 0;
  private MQQueue queue_depth_access;
  private final MQMessage outputMsg = new MQMessage();
  private final MQPutMessageOptions putOpts = new MQPutMessageOptions();
  private final MQGetMessageOptions getOpts = new MQGetMessageOptions();

  public final int MQPUT = 100;
  public final int MQFMT_STRING = 1;
  boolean ccsidOverride = false;

  public MQTransport(Properties envProp, int CCSID) {
    qName = envProp.getProperty(MQMsgConstants.MQ_QUEUE);
    qmName = envProp.getProperty(MQMsgConstants.MQ_QUEUE_MANAGER);

    MQEnvironment.hostname = envProp.getProperty(MQMsgConstants.MQ_HOST_NAME);
    MQEnvironment.port = Integer.parseInt(envProp.getProperty(MQMsgConstants.MQ_PORT));
    MQEnvironment.channel = envProp.getProperty(MQMsgConstants.MQ_CHANNEL);
    MQEnvironment.userID = envProp.getProperty(MQMsgConstants.MQ_USER_ID);
    MQEnvironment.password = envProp.getProperty(MQMsgConstants.MQ_PASSWORD);
    MQEnvironment.CCSID = CCSID > 0 ? CCSID : 1208;
    MQEnvironment.sslCipherSuite = envProp.getProperty(MQMsgConstants.MQ_CIPHER);
    setMQMDFormat(MQFMT_STRING, MQPUT);

    if (CCSID > 0) {
      this.ccsidOverride = true;
    }
  }

  public MQTransport(Properties envProp) {
    this(envProp, 1208);
    this.ccsidOverride = false;
  }

  /**
   * Function Name: setMQMDFormat Purpose: Sets the Format in the MQMD of a
   * message
   * 
   * @param format
   * @param verb
   */
  public void setMQMDFormat(int format, int verb) {
    if (verb == MQPUT) {
      switch (format) {

      case (MQFMT_STRING):
        outputMsg.format = MQConstants.MQFMT_STRING;
        break;

      default:
        outputMsg.format = MQConstants.MQFMT_NONE;
        break;
      }
    }

  }

  /**
   * Method for initialization
   * 
   * @throws Exception
   */
  public final MQQueueManager getMQQueueManager() throws MQException {
    try {
      if (qManager == null || !qManager.isConnected()) {
        qManager = new MQQueueManager(qmName);
        LOG.trace("qManager opened for " + qmName + " " + qManager.isConnected() + " " + qManager.hashCode());
      }
    } catch (MQException e) {
      e.printStackTrace();
      LOG.debug("Error in opening queue manager.", e);
    }
    return qManager;
  }

  public final MQQueue getMQQueue(String optionType) throws Exception {

    int openOptions = 0;
    if (optionType != null && optionType.equals(MQMsgConstants.MQ_QUEUE_OUTPUT)) { // set
                                                                                   // up
                                                                                   // open
      // options for the
      // send queue
      openOptions = MQConstants.MQOO_OUTPUT | MQConstants.MQOO_FAIL_IF_QUIESCING;
    } else { // set up open options for the send queue
      openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_FAIL_IF_QUIESCING;
    }
    // open the queue for sending to IMS
    if (mQQueue == null || !mQQueue.isOpen()) {
      this.mQQueue = qManager.accessQueue(qName, openOptions, null, null, null);
    }

    return mQQueue;
  }

  /**
   * Method to put the message into MQ
   * 
   * @param xmlString
   * @throws Exception
   */

  public final boolean sendMessage(String xmlString) throws MQException, IOException {

    boolean status = false;

    try {
      if (qManager == null || !qManager.isConnected()) {
        getMQQueueManager();
      }
      if (mQQueue == null || !mQQueue.isOpen()) {
        getMQQueue(MQMsgConstants.MQ_QUEUE_OUTPUT);
      }
      if (this.ccsidOverride) {

      } else {

      }
      outputMsg.setVersion(MQConstants.MQMD_VERSION_1);
      outputMsg.expiry = MQConstants.MQEI_UNLIMITED;
      outputMsg.messageType = MQConstants.MQMT_DATAGRAM;
      outputMsg.priority = MQConstants.MQPRI_PRIORITY_AS_Q_DEF;
      outputMsg.persistence = MQConstants.MQPER_PERSISTENT;

      if (this.ccsidOverride) {
        outputMsg.characterSet = MQEnvironment.CCSID;
      }
      // create a new message object
      outputMsg.clearMessage();

      outputMsg.writeString(xmlString);
      // Start:Added for DSW Publish,send one message to multiple destination
      // using topic
      mQQueue.put(outputMsg, putOpts);

      status = true;

    } catch (MQException ex) {
      LOG.error("A WebSphere MQ error occurred : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode);
    } catch (IOException ex) {
      LOG.error("An error occurred whilst writing to the message buffer. ", ex);
    } catch (Exception ex) {
      LOG.error("An general error error occurred.", ex);
      ex.printStackTrace();
    }
    return status;
  }

  public void commit() {
    try {
      qManager.commit();

    } catch (MQException mqex) {
      LOG.error("Error in commit " + mqex.getMessage());

    } catch (Exception ex) {
      LOG.error("Error in commit " + ex.getMessage());

    }

  }

  public void backout() {
    try {
      qManager.backout();

    } catch (MQException mqex) {
      LOG.error("Error in backout " + mqex.getMessage());

    } catch (Exception ex) {
      LOG.error("Error in backout " + ex.getMessage());
    }

  }

  public final void closeMQQueue() {
    try {
      if (mQQueue != null) {
        mQQueue.close();
      }

    } catch (MQException mqex) {
      mqex.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public final void closeMQQManager() {
    try {
      if (qManager != null) {
        qManager.disconnect();
      }

    } catch (MQException mqex) {
      mqex.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();
    } // try
  }

  public final String getMessage() {
    // connect to Queue Manager using a properties table
    String message = "";
    try {
      MQMessage myMessage = new MQMessage();
      myMessage.correlationId = MQConstants.MQCI_NONE;
      myMessage.messageId = MQConstants.MQMI_NONE;

      if (qManager == null || !qManager.isConnected()) {
        getMQQueueManager();
      }
      if (mQQueue == null || !mQQueue.isOpen()) {
        getMQQueue(MQMsgConstants.MQ_QUEUE_INPUT);
      }
      // Set the put message options.
      getOpts.options = getOpts.options + MQConstants.MQGMO_SYNCPOINT;// Get
                                                                      // messages
                                                                      // under
                                                                      // sync
                                                                      // point
                                                                      // control�0„5
      getOpts.options = getOpts.options + MQConstants.MQGMO_WAIT; // Wait if no
                                                                  // messages on
                                                                  // the Queue
      getOpts.options = getOpts.options + MQConstants.MQGMO_FAIL_IF_QUIESCING;// Fail
                                                                              // if
                                                                              // Queue
                                                                              // Manager
                                                                              // Quiescing
      getOpts.waitInterval = 1000; // Sets the time limit for the wait.

      mQQueue.get(myMessage, getOpts);
      message = myMessage.readStringOfCharLength(myMessage.getMessageLength());

    } catch (MQException ex) {
      LOG.trace("A WebSphere MQ error occurred : Completion code " + ex.completionCode + " Reason code " + ex.reasonCode);
    } catch (IOException ex) {
      LOG.trace("An error occurred whilst writing to the message buffer: " + ex);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return message;
  }

  public final ArrayList<String> getAllMessage(int batchSize) {
    // connect to Queue Manager using a properties table
    LOG.info("Retrieving messages from MQ..");
    ArrayList<String> allMessages = new ArrayList<String>();
    try {
      int depth = getQueuedepth();
      if (depth > batchSize)
        depth = batchSize;

      LOG.debug(depth + " messages on the queue.");
      for (int i = 0; i < depth; i++) {
        MQMessage myMessage = new MQMessage();
        myMessage.correlationId = MQConstants.MQCI_NONE;
        myMessage.messageId = MQConstants.MQMI_NONE;

        mQQueue.get(myMessage);
        String message = myMessage.readStringOfCharLength(myMessage.getMessageLength());
        LOG.debug("The Message from queue is " + message);
        int first = message.indexOf("<");
        if (first >= 0) {
          allMessages.add(message.substring(first));
        } else {
          allMessages.add(message);
        }
      }
    } catch (MQException mqex) {
      mqex.printStackTrace();
    } catch (Exception ex) {

      ex.printStackTrace();
    }
    return allMessages;
  }

  public final int getQueuedepth() {
    // connect to Queue Manager using a properties table
    try {
      queue_depth_access = qManager.accessQueue(qName, MQConstants.MQOO_SET | MQConstants.MQOO_INQUIRE);
      queuedepth = queue_depth_access.getCurrentDepth();
      queue_depth_access.close();

    } catch (MQException mqex) {
      mqex.printStackTrace();
    } catch (Exception ex) {

      ex.printStackTrace();
    }

    return queuedepth;
  }

  public void setMQConnection(String queueType) {
    /*********
     * Connect with Queue Manager and open the MQ at the beginning for better
     * performance with Manager and Queue
     *****/
    boolean flag = true;
    int counter = 0;
    while (flag) {
      try {
        getMQQueueManager();
        getMQQueue(queueType);
        flag = false;
      } catch (Exception ex) {
        System.err.println("Queue manager can not be connected or can not connect to Queue, Please check log file");
        counter++;
        if (counter == 3) {
          flag = false;
          LOG.info("3 retry completed sending mail now");
          LOG.info(" Error in setMQConnection" + ex.getMessage());
          System.exit(1);
        }
        LOG.error(ex.getMessage());
      }
    }
    /*********
     * End:Connect with Queue Manager and open the MQ at the beginning for
     * better performance with Manager and Queue
     *****/
  }

  public void open(boolean receive) throws Exception {
    LOG.debug("Opening queue manager " + qmName);
    getMQQueueManager();

    if (qManager == null || !qManager.isConnected()) {
      throw new Exception("Cannot connect to Queue Manager .." + qmName);
    }
    if (!receive) {
      LOG.debug("Opening output queue " + qName);
      getMQQueue(MQMsgConstants.MQ_QUEUE_OUTPUT);
    } else {
      LOG.debug("Opening input queue " + qName);
      getMQQueue(MQMsgConstants.MQ_QUEUE_INPUT);
    }

  }
}
