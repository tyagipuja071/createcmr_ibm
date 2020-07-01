/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.handler;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.MqIntfReqData;
import com.ibm.cio.cmr.request.entity.MqIntfReqDataPK;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.TransformerManager;

/**
 * Represents a message handler for MQ interfaces
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class MQMessageHandler {

  private static final Logger LOG = Logger.getLogger(MQMessageHandler.class);

  protected EntityManager entityManager = null;
  public MqIntfReqQueue mqIntfReqQueue = null;
  public Admin adminData = null;
  public Data cmrData = null;
  public Addr addrData = null;
  public boolean addressUpdated = false;
  public Map<String, String> messageHash = new LinkedHashMap<String, String>();
  public List<Addr> currentAddresses = null;
  public String doc_num = null;
  public boolean updateRequest = false;
  public int publishedSequence = -1;
  public boolean skipPublish = false;

  public Map<String, String> currentCMRValues = new HashMap<String, String>();

  public MQMessageHandler(EntityManager entityManager, MqIntfReqQueue mqIntfReqQueue) {
    this.entityManager = entityManager;
    this.mqIntfReqQueue = mqIntfReqQueue;
    this.updateRequest = CmrConstants.REQ_TYPE_UPDATE.equals(mqIntfReqQueue.getReqType());
  }

  public MQMessageHandler() {

  }

  /**
   * Builds the message to be placed on the queue
   * 
   * @return
   * @throws Exception
   */
  public abstract String buildMQMessage() throws Exception;

  /**
   * Handles the messages retrieved from the queue
   * 
   * @param xmlData
   * @throws Exception
   */
  public abstract void processMQMessage(String xmlData) throws Exception;

  /**
   * Calls the interface to retrieve the current values of the record and
   * populates the variable currentCMRValues
   */
  public abstract void retrieveCurrentValues();

  /**
   * Returns true if retries are supported by this handler
   * 
   * @return
   */
  public abstract boolean retrySupported();

  /**
   * Retrieves the current {@link Admin}, {@link Data}, and {@link Addr} records
   * 
   * @param lastSequence
   * @throws Exception
   */
  protected void retrieveCurrentRecords(int lastSequence) throws Exception {
    long reqId = this.mqIntfReqQueue.getReqId();
    LOG.debug("Retrieving Admin, Data, and current Address records for Request " + reqId);
    AdminPK adminPk = new AdminPK();
    adminPk.setReqId(reqId);
    this.adminData = this.entityManager.find(Admin.class, adminPk);
    if (this.adminData == null) {
      throw new Exception("Admin record cannot be retrieved for Request " + reqId);
    }

    DataPK dataPk = new DataPK();
    dataPk.setReqId(reqId);
    this.cmrData = this.entityManager.find(Data.class, dataPk);
    if (this.cmrData == null) {
      throw new Exception("Data record cannot be retrieved for Request " + reqId);
    }

    this.addrData = getNextAddressData(this.mqIntfReqQueue, lastSequence < 0 ? 0 : lastSequence);

  }

  /**
   * Completes the last workflow history record before creating a new one
   * 
   * @param entityManager
   * @param reqId
   */
  public static void completeLastHistoryRecord(EntityManager entityManager, long reqId) {
    PreparedQuery update = new PreparedQuery(entityManager, ExternalizedQuery.getSql("WORK_FLOW.COMPLETE_LAST"));
    update.setParameter("REQ_ID", reqId);
    update.executeSql();
  }

  public String getCustomerLocationCode(Addr addr, String postCd) {

    String customerLocationCode = "";

    // first check if the incoming country can be found or not in the table
    // if yes, then using the country code to get the customer location number
    // if not, then using post code and city to fetch the customer location
    // number.

    if (!"FR".equalsIgnoreCase(addr.getLandCntry())) {
      String sql4CountryIn = ExternalizedQuery.getSql("QUERY.GET.CUSLOC4C");
      PreparedQuery query4CountryIn = new PreparedQuery(this.entityManager, sql4CountryIn);
      query4CountryIn.setParameter("LANDED_CNTRY", addr.getLandCntry());
      LOG.debug("sql4C==" + sql4CountryIn + " with parameter, LANDED_CNTRY==" + addr.getLandCntry());

      List<Object[]> results = query4CountryIn.getResults(1);
      if (results != null && results.size() > 0) {
        Object obj[] = new Object[] { (results.get(0)) };
        customerLocationCode = (String) obj[0];
      }
    }

    if (StringUtils.isEmpty(customerLocationCode)) {
      String city = StringUtils.isNotEmpty(addr.getCity1()) ? addr.getCity1() : "";

      String sql4L = ExternalizedQuery.getSql("QUERY.GET.CUSLOC4L");
      LOG.debug("sql4L==" + sql4L + " with parameter CITY==" + city.toUpperCase().trim() + "%" + "  and POST_CD==" + postCd);
      PreparedQuery queryCusLoc4L = new PreparedQuery(this.entityManager, sql4L);
      queryCusLoc4L.setParameter("CITY", city.toUpperCase().trim() + "%");
      queryCusLoc4L.setParameter("POST_CD", postCd);

      List<Object[]> results1 = queryCusLoc4L.getResults(1);
      if (results1 != null && results1.size() > 0) {
        LOG.debug("cusLocN==" + results1.get(0));

        Object obj[] = new Object[] { (results1.get(0)) };
        customerLocationCode = (String) obj[0];
      }

    }

    LOG.debug("customerLocationCode returned is ==" + customerLocationCode);
    return customerLocationCode;

  }

  /**
   * Update the REQ_STATUS of CREQCMR.MQ_INTF_REQ_QUEUE
   * 
   * @param String
   *          status
   * @return
   */
  public void updateMQIntfErrorStatus(String status, String errorCd, String errorMsg) {

    mqIntfReqQueue.setReqStatus(status);
    mqIntfReqQueue.setErrorCd(errorCd);
    mqIntfReqQueue.setExecpMessage(errorMsg);
    mqIntfReqQueue.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    mqIntfReqQueue.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
    entityManager.merge(mqIntfReqQueue);
    LOG.debug("Updating CREQCMR.MQ_INTF_REQ_QUEUE records for REQ_STATUS and Error Message, reqId = " + mqIntfReqQueue.getId().getQueryReqId());
  }

  /**
   * Update the REQ_STATUS of CREQCMR.MQ_INTF_REQ_QUEUE
   * 
   * @param String
   *          status
   * @return
   */
  public void updateMQIntfStatus(String status) throws Exception {
    mqIntfReqQueue.setReqStatus(status);
    mqIntfReqQueue.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    mqIntfReqQueue.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
    // clear code and message if there are no errors
    mqIntfReqQueue.setErrorCd(null);
    mqIntfReqQueue.setExecpMessage(null);
    entityManager.merge(mqIntfReqQueue);
    LOG.debug("Updating CREQCMR.MQ_INTF_REQ_QUEUE records for REQ_STATUS, reqId = " + mqIntfReqQueue.getId().getQueryReqId());
  }

  public void updateAdminRequest(String reqStatus, String processedFlag, boolean complete) throws CmrException, SQLException {
    updateAdminRequest(reqStatus, processedFlag, complete, null);
  }

  /**
   * Updates the relevant {@link Admin} record
   * 
   * @param reqStatus
   * @param processedFlag
   * @param complete
   * @throws CmrException
   * @throws SQLException
   */
  public void updateAdminRequest(String reqStatus, String processedFlag, boolean complete, String addtlComments) throws CmrException, SQLException {

    AdminPK pk = new AdminPK();
    pk.setReqId(mqIntfReqQueue.getReqId());
    Admin admin = entityManager.find(Admin.class, pk);
    if (admin != null) {
      LOG.info("Updating Admin status and lock for Request ID " + pk.getReqId());
      admin.setLockInd(MQMsgConstants.FLAG_NO);
      admin.setLockTs(null);
      admin.setLockBy(null);
      admin.setLockByNm(null);
      admin.setProcessedFlag(processedFlag);
      admin.setProcessedTs(SystemUtil.getCurrentTimestamp());
      admin.setReqStatus(reqStatus);
      admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      admin.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
      entityManager.merge(admin);
      entityManager.flush();
    }

    completeLastHistoryRecord(entityManager, mqIntfReqQueue.getReqId());

    // WH_HIST??
    WfHist hist = new WfHist();
    WfHistPK histpk = new WfHistPK();
    histpk.setWfId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "WF_ID"));
    hist.setId(histpk);
    hist.setReqStatus(reqStatus);
    hist.setCreateById(MQMsgConstants.MQ_APP_USER);
    hist.setCreateByNm(MQMsgConstants.MQ_APP_USER);
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setReqId(mqIntfReqQueue.getReqId());
    hist.setRejReason(null);
    if (complete) {
      if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
        hist.setCmt(MQMsgConstants.WH_HIST_CMT_COM_UPDATE + mqIntfReqQueue.getCmrNo());
      } else {
        hist.setCmt(MQMsgConstants.WH_HIST_CMT_COM + mqIntfReqQueue.getCmrNo());
      }
      hist.setReqStatusAct(MQMsgConstants.WH_HIST_COM);
      hist.setCompleteTs(SystemUtil.getCurrentTimestamp());
    } else {
      hist.setCmt(MQMsgConstants.WH_COMMENT_REJECT);
      hist.setReqStatusAct(MQMsgConstants.WH_REJECT);
    }
    if (addtlComments != null) {
      hist.setCmt(hist.getCmt() + addtlComments);
    }
    entityManager.persist(hist);
  }

  /**
   * Updates the MQ interface records depending on the status
   * 
   * @param noError
   * @throws Exception
   */
  public void updateMQIntfReqStatus(boolean noError) throws Exception {
    int lastSequence = this.publishedSequence > 0 ? publishedSequence : getLastSequence(this.mqIntfReqQueue);
    if (noError) {
      LOG.debug("Other addresses pending, setting to PUB" + (lastSequence + 1));
      updateMQIntfStatus(MQMsgConstants.REQ_STATUS_PUB + (lastSequence + 1));
    } else {
      LOG.debug("Error was encountered during processing. Sending request back to processor.");
      updateMQIntfErrorStatus(MQMsgConstants.REQ_STATUS_SER + lastSequence, "001", "The values of Address or CMR Data are not correct!");
      updateAdminRequest(MQMsgConstants.REQ_STATUS_PPN, MQMsgConstants.PROCESSED_FLAG_E, false);
    }
  }

  /**
   * Gets the last sequence of publish
   * 
   * @param reqQueue
   * @return
   */
  protected int getLastSequence(MqIntfReqQueue reqQueue) {
    String intfStatus = reqQueue.getReqStatus();
    int lastSeq = -1;
    if (MQMsgConstants.REQ_STATUS_NEW.equals(intfStatus) || MQMsgConstants.REQ_STATUS_RETRY.equals(intfStatus)
        || MQMsgConstants.REQ_STATUS_RESEND.equals(intfStatus)) {
      return 0;
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
    if (intfStatus != null && intfStatus.contains(MQMsgConstants.REQ_STATUS_WAIT)) {
      if (!MQMsgConstants.REQ_STATUS_WAIT.equals(intfStatus)) {
        lastSeq = Integer.parseInt(intfStatus.substring(4));
      }
    }
    return lastSeq;
  }

  /**
   * Gets the next address data on the queue depending on the last sequence
   * retrieved
   * 
   * @param reqQueue
   * @param lastSequence
   * @return
   */
  protected Addr getNextAddressData(MqIntfReqQueue reqQueue, int lastSequence) {

    // default order
    String[] order = { "ZS01", "ZI01", "ZP01", "ZD01" };

    MessageTransformer transformer = TransformerManager.getTransformer(reqQueue.getCmrIssuingCntry());
    if (transformer != null) {
      // order from the transformer
      if (transformer.getAddressOrder() != null) {
        order = transformer.getAddressOrder();
      }

    }
    String sql = ExternalizedQuery.getSql("MQREQUEST.GETNEXTADDR");

    StringBuilder addrTypeClause = new StringBuilder();
    if (order != null && order.length > 0) {

      for (String type : order) {
        LOG.debug("Looking for Address Types " + type);
        addrTypeClause.append(addrTypeClause.length() > 0 ? ", " : "");
        addrTypeClause.append("'" + type + "'");
      }
    }

    if (addrTypeClause.length() > 0) {
      sql += " and ADDR_TYPE in ( " + addrTypeClause.toString() + ") ";
    }
    StringBuilder orderBy = new StringBuilder();
    int orderIndex = 0;
    for (String type : order) {
      orderBy.append(" when ADDR_TYPE = '").append(type).append("' then ").append(orderIndex);
      orderIndex++;
    }
    orderBy.append(" else 25 end, ADDR_TYPE, case when IMPORT_IND = 'Y' then 0 else 1 end, ADDR_SEQ ");
    sql += " order by case " + orderBy.toString();

    PreparedQuery query = new PreparedQuery(this.entityManager, sql);
    query.setParameter("REQ_ID", reqQueue.getReqId());
    query.setForReadOnly(true);
    List<Addr> addrList = query.getResults(Addr.class);
    if (addrList != null && !addrList.isEmpty()) {
      this.currentAddresses = addrList;
      if (addrList.size() > lastSequence) {
        return addrList.get(lastSequence);
      }
    }

    return null;
  }

  /**
   * Creates a {@link ReqCmtLog} entry for the request
   * 
   * @param comment
   * @param cmrNo
   * @throws CmrException
   * @throws SQLException
   */
  public void createPartialComment(String comment, String cmrNo) throws CmrException, SQLException {
    long reqId = this.mqIntfReqQueue.getReqId();
    ReqCmtLog log = new ReqCmtLog();
    ReqCmtLogPK pk = new ReqCmtLogPK();
    LOG.debug("Creating request comment for CMR No " + cmrNo + " assigned.");
    long cmtId = SystemUtil.getNextID(this.entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID");
    pk.setCmtId(cmtId);
    log.setId(pk);
    log.setCmt(comment);
    log.setCreateById(MQMsgConstants.MQ_APP_USER);
    log.setCreateByNm(MQMsgConstants.MQ_APP_USER);
    log.setCreateTs(SystemUtil.getCurrentTimestamp());
    log.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    log.setUpdateTs(log.getCreateTs());
    log.setReqId(reqId);
    this.entityManager.persist(log);
  }

  public List<String> removeUnChangedDataItems(long reqId, Data newData) {
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    List<String> results = new ArrayList<String>();
    List<DataRdc> records = query.getResults(DataRdc.class);
    if (records != null && records.size() > 0) {
      for (DataRdc oldData : records) {
        if (equals(oldData.getRepTeamMemberNo(), newData.getRepTeamMemberNo()))
          results.add("SR");
        if (equals(oldData.getSalesBusOffCd(), newData.getSalesBusOffCd()))
          results.add("SBO");
        if (equals(oldData.getInstallBranchOff(), newData.getInstallBranchOff()))
          results.add("IBO");
        if (equals(oldData.getTaxCd1(), newData.getTaxCd1()))
          results.add("Siret");
        if (equals(oldData.getVat(), newData.getVat()))
          results.add("VAT");
      }
    }
    return results;
  }

  /**
   * Saves the XML under the {@link MqIntfReqQueue} record
   * 
   * @param xmlData
   * @param fileName
   * @param queryReqId
   */
  protected void saveXmlContentToDB(String xmlData, String fileName, long queryReqId) {
    try {
      Timestamp ts = SystemUtil.getCurrentTimestamp();
      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      String tsFileName = formatter.format(ts);
      LOG.debug("Saving XML " + queryReqId + "/" + tsFileName + " to Database..");
      MqIntfReqData data = new MqIntfReqData();
      MqIntfReqDataPK pk = new MqIntfReqDataPK();
      pk.setQueryReqId(queryReqId);
      pk.setFileName(queryReqId + "_" + tsFileName + ".xml");
      data.setId(pk);
      data.setContents(xmlData);
      data.setCreateTs(SystemUtil.getCurrentTimestamp());
      this.entityManager.persist(data);
      this.entityManager.flush();
      LOG.debug("XML content saved to database..");
    } catch (Exception e) {
      LOG.debug("Cannot save XML to DB.", e);
    }
  }

  public static void main(String[] args) {
    Timestamp ts = new Timestamp(new Date().getTime());
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    String tsFileName = formatter.format(ts);
    System.out.println(tsFileName);
  }

  /**
   * Checks absolute equality between the strings
   * 
   * @param val1
   * @param val2
   * @return
   */
  public boolean equals(String val1, String val2) {
    if (val1 == null && val2 != null) {
      return StringUtils.isEmpty(val2.trim());
    }
    if (val1 != null && val2 == null) {
      return StringUtils.isEmpty(val1.trim());
    }
    if (val1 == null && val2 == null) {
      return true;
    }
    return val1.trim().equals(val2.trim());
  }

  public EntityManager getEntityManager() {
    return this.entityManager;
  }

}
