/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;

/**
 * Service class to maintain Requests. The process sends out notifications to
 * requesters for Drafts without activity after a defined period of time
 * 
 * @author 136786PH1
 *
 */
public class RequestMaintService extends BaseBatchService {

  private static final Logger LOG = Logger.getLogger(RequestMaintService.class);

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    // get requests which are still in DRA/PRJ and last updated before a defined
    // period
    int closeDays = 45; // default to 45 days
    int autoCloseParam = SystemParameters.getInt("AUTOCLOSE.DAYS");
    if (autoCloseParam > 0) {
      closeDays = 45;
    }

    sendWarningMessages(entityManager, closeDays);

    autoCloseRequests(entityManager, closeDays);

    return true;
  }

  /**
   * Sends out a warning to the requester of the request which has not been
   * updated in a specified period of time
   * 
   * @param entityManager
   * @param closeDays
   */
  private void sendWarningMessages(EntityManager entityManager, int closeDays) {
    LOG.debug("Gathering requests for close warning..");
    String sql = ExternalizedQuery.getSql("BATCH.AUTOCLOSE.WARN");
    sql = StringUtils.replace(sql, ":DAYS", closeDays + "");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("DAYS", closeDays);
    query.setForReadOnly(true);
    List<Admin> warnList = query.getResults(Admin.class);
    Map<String, List<Admin>> reqIdMap = new HashMap<String, List<Admin>>();
    // map first all requests under same requester, minimize mails
    Timestamp ts = SystemUtil.getActualTimestamp();
    LOG.debug(warnList.size() + " requests to send warnings to.");
    int currCount = 0;
    for (Admin admin : warnList) {
      boolean ibmer = admin.getRequesterId() != null && admin.getRequesterId().toLowerCase().endsWith("ibm.com");
      boolean external = !StringUtils.isBlank(admin.getSourceSystId());
      if (!external && ibmer) {
        reqIdMap.putIfAbsent(admin.getRequesterId().toLowerCase(), new ArrayList<>());
        reqIdMap.get(admin.getRequesterId().toLowerCase()).add(admin);
      }
      admin.setWarnMsgSentDt(ts);
      LOG.debug("Setting warn date for Request " + admin.getId().getReqId());
      admin.setUseParentManager(true);
      entityManager.merge(admin);
      currCount++;
      if (currCount % 200 == 0 && currCount > 0) {
        LOG.debug("Flushing at " + currCount + " records.");
        entityManager.flush();
      }
      keepAlive();
    }

    for (String requesterId : reqIdMap.keySet()) {
      sendEmailWarning(requesterId, reqIdMap.get(requesterId));
      keepAlive();
    }
  }

  /**
   * Gets the requests where the warning message has been sent more than 2 days
   * already. The warning message sent date is cleared everytime the admin
   * record is saved by CreateCMR
   * 
   * @param entityManager
   * @param closeDays
   * @throws SQLException
   * @throws CmrException
   */
  private void autoCloseRequests(EntityManager entityManager, int closeDays) throws CmrException, SQLException {
    LOG.debug("Gathering requests for auto-closing..");
    String sql = ExternalizedQuery.getSql("BATCH.AUTOCLOSE.EXEC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    List<Admin> closeList = query.getResults(Admin.class);
    Timestamp ts = SystemUtil.getActualTimestamp();

    String comment = "Auto closed by System due to inactivity.";
    LOG.debug(closeList.size() + " requests to auto-close.");
    for (Admin admin : closeList) {
      Thread.currentThread().setName("REQ-" + admin.getId().getReqId());

      // CREATCMR-7629 cancel approval before closing if any
      cancelApprovalsBeforeAutoClose(entityManager, admin.getId().getReqId());

      admin.setLastUpdtTs(ts);
      admin.setReqStatus("CLO");
      admin.setLockInd("N");
      admin.setLockByNm(null);
      admin.setLockBy(null);
      admin.setLockTs(null);
      admin.setLastUpdtBy(BATCH_USER_ID);
      LOG.debug("Closing Request " + admin.getId().getReqId());
      admin.setUseParentManager(true);
      updateEntity(admin, entityManager);

      RequestUtils.createWorkflowHistoryFromBatch(entityManager, BATCH_USER_ID, admin, comment, "Auto-Close", null, null, true, false, null);
      RequestUtils.createCommentLogFromBatch(entityManager, BATCH_USER_ID, admin.getId().getReqId(), comment);
      keepAlive();

    }

  }

  /**
   * Sends the warning email to the Requester
   * 
   * @param admin
   * @return
   */
  private void sendEmailWarning(String requesterId, List<Admin> reqIdList) {
    // construct the content
    String applicationUrl = SystemConfiguration.getValue("APPLICATION_URL") + "/request/";
    StringBuilder content = new StringBuilder();
    content.append("<html>");
    content.append("<head><style>body { font-family: IBM Plex Sans, Calibri; font-size:12px}</style></head>");
    content.append("<body>");
    content.append("Please note that there are several requests in CreateCMR under your name that have been inactive for a long time.<br>");
    content.append("These requests will automatically be closed by the system in <strong>2 days</strong> if they are not updated.<br>");
    content.append("To keep your request, please go into CreateCMR and perform the <strong>Save</strong> action once on your request.<br><br>");
    content.append("Requests to be closed:<br>");
    for (Admin admin : reqIdList) {
      String name = !StringUtils.isBlank(admin.getMainCustNm1()) ? admin.getMainCustNm1() : "-no name specified-";
      content.append("<a href=\"").append(applicationUrl).append(admin.getId().getReqId()).append("\">")
          .append(admin.getId().getReqId() + " - " + name).append("</a><br>");
    }
    content.append(
        "<br><br>If you need to reactivate an Auto-Closed request, please reach out to <strong>CI Operations</strong> via their <a href=\"https://chiefdataoffice.slack.com/archives/CRVNPAF17\">Slack Channel</a>");
    content.append("<br><br>CreateCMR Admin");
    content.append("</body>");
    content.append("</html>");

    // send the mail
    String host = SystemConfiguration.getValue("MAIL_HOST");
    String from = SystemConfiguration.getValue("MAIL_FROM");
    String subject = "For Your Action: You have inactive requests in CreateCMR";

    Email mail = new Email();
    mail.setTo(requesterId);
    mail.setFrom(from);
    mail.setMessage(content.toString());
    mail.setSubject(subject);
    mail.setType(MessageType.HTML);
    try {
      mail.send(host);
    } catch (Exception e) {
      LOG.warn("Notification mail cannot be sent for requester " + requesterId, e);
    }
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  private void cancelApprovalsBeforeAutoClose(EntityManager entityManager, long requestID) {
    LOG.debug("Processing Cancel Approvals before auto closing request :-" + requestID);
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("BATCH.AUTOCLOSE.APPROVAL"));
    query.setParameter("REQ_ID", requestID);
    query.executeSql();
  }
}
