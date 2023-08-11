package com.ibm.cmr.create.batch.util.mq;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueuePK;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.create.batch.util.mq.config.MQConfig;

public class RecordCollector {

  protected Logger LOG = Logger.getLogger(RecordCollector.class);

  public void processMQRequest(EntityManager entityManager) throws CmrException, SQLException {

    String sql = ExternalizedQuery.getSql("MQREQUEST.GETADMINREQ");
    String queryMapping = Admin.MQ_REQUESTS_MAPPING;
    PreparedQuery adminReqQuery = new PreparedQuery(entityManager, sql);
    adminReqQuery.setForReadOnly(true);
    adminReqQuery.setParameter("REQ_STATUS", MQMsgConstants.REQ_STATUS_PCP);
    List<CompoundEntity> rs = null;
    rs = adminReqQuery.getCompundResults(Admin.class, queryMapping);

    LOG.info("There are " + rs.size() + " record to be processed.");
    String targetSys = null;
    MQConfig config = null;
    if (rs != null) {
      for (CompoundEntity entity : rs) {
        LOG.info("The request id is " + ((Long) entity.getValue("REQ_ID")).toString());
        MqIntfReqQueue mqIntfReqQueue = getMqIntfReqQueueByReqId(((Long) entity.getValue("REQ_ID")).toString(), entityManager);

        String mqStatus = MQMsgConstants.REQ_STATUS_NEW;
        if (mqIntfReqQueue != null) {
          mqStatus = getMQStatus(mqIntfReqQueue.getReqStatus());
        }
        mqIntfReqQueue = new MqIntfReqQueue();
        MqIntfReqQueuePK mqIntfReqQueuePK = new MqIntfReqQueuePK();
        mqIntfReqQueuePK.setQueryReqId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "QUERY_REQ_ID", "CREQCMR"));
        mqIntfReqQueue.setId(mqIntfReqQueuePK);
        mqIntfReqQueue.setReqId(((Long) entity.getValue("REQ_ID")).longValue());
        if (entity.getValue("CMR_NO") != null && !entity.getValue("CMR_NO").toString().startsWith("P")) {
          mqIntfReqQueue.setCmrNo((String) entity.getValue("CMR_NO"));
        }
        mqIntfReqQueue.setCmrIssuingCntry((String) entity.getValue("CMR_ISSUING_CNTRY"));
        mqIntfReqQueue.setReqStatus(mqStatus);
        mqIntfReqQueue.setReqType((String) entity.getValue("REQ_TYPE"));
        mqIntfReqQueue.setCreateTs((Date) entity.getValue("LAST_UPDT_TS"));
        mqIntfReqQueue.setCreateBy((String) entity.getValue("LAST_UPDT_BY"));
        mqIntfReqQueue.setLastUpdtTs((Date) entity.getValue("LAST_UPDT_TS"));
        mqIntfReqQueue.setLastUpdtBy((String) entity.getValue("LAST_UPDT_BY"));
        config = MQConfig.getConfigForCountry(mqIntfReqQueue.getCmrIssuingCntry());
        if (config != null) {
          targetSys = config.getTargetSystem();
        }
        if (StringUtils.isEmpty(targetSys)) {
          targetSys = MQMsgConstants.MQ_TARGET_SYS;
        }
        mqIntfReqQueue.setTargetSys(targetSys);
        mqIntfReqQueue.setMqInd(MQMsgConstants.FLAG_NO);
        entityManager.persist(mqIntfReqQueue);
        updateAdminByMQRequest(mqIntfReqQueue, entityManager);
      }
    }
  }

  private void updateAdminByMQRequest(MqIntfReqQueue mqIntfReqQueue, EntityManager entityManager) throws CmrException, SQLException {

    // Lock Admin,
    // Update ADMIN.PROCESSING_STATUS, ADMIN.PROCESSING_TS ???

    AdminPK pk = new AdminPK();
    pk.setReqId(mqIntfReqQueue.getReqId());
    Admin admin = entityManager.find(Admin.class, pk);
    if (admin != null) {
      LOG.info("Updating Admin status and lock for Request ID " + pk.getReqId());
      admin.setLockInd("Y");
      admin.setLockTs(SystemUtil.getCurrentTimestamp());
      admin.setLockBy(MQMsgConstants.MQ_APP_USER);
      admin.setLockByNm(MQMsgConstants.MQ_APP_USER);
      admin.setProcessedFlag(MQMsgConstants.PROCESSED_FLAG_WX);
      admin.setProcessedTs(SystemUtil.getCurrentTimestamp());
      admin.setReqStatus(CmrConstants.REQUEST_STATUS.PCR.toString());
      admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      admin.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
      entityManager.merge(admin);
      entityManager.flush();
    }

    WfHist hist = new WfHist();
    WfHistPK histpk = new WfHistPK();
    histpk.setWfId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "WF_ID"));
    hist.setId(histpk);
    hist.setReqStatus(CmrConstants.REQUEST_STATUS.PCR.toString());
    hist.setCreateById(MQMsgConstants.MQ_APP_USER);
    hist.setCreateByNm(MQMsgConstants.MQ_APP_USER);
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setReqId(mqIntfReqQueue.getReqId());
    hist.setRejReason(null);
    hist.setCmt("Processing Started.");
    hist.setReqStatusAct("Claim");
    entityManager.persist(hist);
    entityManager.flush();
  }

  protected MqIntfReqQueue getMqIntfReqQueueByReqId(String req_id, EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("MQREQUEST.GETMQREQBYREQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", req_id);
    query.setForReadOnly(true);
    return query.getSingleResult(MqIntfReqQueue.class);
  }

  protected String getMQStatus(String req_status) {
    int lastSeq = 1;
    if (req_status == null | req_status.equalsIgnoreCase(""))
      return MQMsgConstants.REQ_STATUS_NEW;
    if (req_status.equalsIgnoreCase(MQMsgConstants.REQ_STATUS_SER + "1"))
      return MQMsgConstants.REQ_STATUS_NEW;
    if (req_status.equalsIgnoreCase(MQMsgConstants.REQ_STATUS_SER + "0"))
      return MQMsgConstants.REQ_STATUS_NEW;
    if (req_status != null && req_status.contains(MQMsgConstants.REQ_STATUS_SER)) {
      lastSeq = Integer.parseInt(req_status.substring(3));
      return MQMsgConstants.REQ_STATUS_COM + (lastSeq - 1);
    }
    return MQMsgConstants.REQ_STATUS_NEW;
  }
}
