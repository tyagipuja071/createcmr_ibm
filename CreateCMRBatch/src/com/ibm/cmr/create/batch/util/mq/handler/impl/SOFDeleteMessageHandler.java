package com.ibm.cmr.create.batch.util.mq.handler.impl;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cmr.create.batch.model.SOFResponseCUDQMessage;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;

public class SOFDeleteMessageHandler extends SOFMessageHandler {

  public SOFDeleteMessageHandler(EntityManager entityManager, MqIntfReqQueue mqIntfReqQueue) {
    super(entityManager, mqIntfReqQueue);
  }

  @Override
  public void updateMQIntfReqStatus(boolean noError) throws Exception {
    if (noError)
      // update CREQCMR.MQ_INTF_REQ_QUEUE REQ_STATUS to "PUB"
      updateMQIntfStatus(MQMsgConstants.REQ_STATUS_PUB);
    else
      updateMQIntfErrorStatus(MQMsgConstants.REQ_STATUS_SER, "001", "The values of Address or CMR Data are not correct!");
  }

  @Override
  public void saveMsgToDB() throws Exception {
    if (MQMsgConstants.SOF_STATUS_SUCCESS.equalsIgnoreCase(((SOFResponseCUDQMessage) rdcLegacyMQMessage).getStatus().trim())) {
      // update CREQCMR.MQ_INTF_REQ_QUEUE REQ_STATUS to "COM"
      updateMQIntfStatus(MQMsgConstants.REQ_STATUS_COM);
    } else {
      // update CREQCMR.MQ_INTF_REQ_QUEUE REQ_STATUS to "SER"
      // update CREQCMR.MQ_INTF_REQ_QUEUE ERROR_CD,EXECP_MESSAGE
      updateMQIntfErrorStatus(MQMsgConstants.REQ_STATUS_SER, "001", ((SOFResponseCUDQMessage) rdcLegacyMQMessage).getMessage());
    }
  }
}
