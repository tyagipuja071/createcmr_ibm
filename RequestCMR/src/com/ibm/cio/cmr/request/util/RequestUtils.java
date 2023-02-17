/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrInternalTypes;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.Lov;
import com.ibm.cio.cmr.request.entity.NotifList;
import com.ibm.cio.cmr.request.entity.NotifListPK;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.StatusDesc;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.model.requestentry.CheckListModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.MassCreateBatchEmailModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.external.CreateCMRBPHandler;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.JPHandler;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFile;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileParser;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.StandardCityServiceClient;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.stdcity.StandardCityRequest;
import com.ibm.cmr.services.client.stdcity.StandardCityResponse;

/**
 * Contains utilities for Request workflow processing
 * 
 * @author Jeffrey Zamora
 * 
 */
public class RequestUtils {

  private static final Logger LOG = Logger.getLogger(RequestUtils.class);
  private static String emailTemplate = null;
  private static String batchemailTemplate = null;
  private static final String SOURCE = "CreateCMR-BP";
  public static final String STATUS_REJECTED = "Rejected";
  public static final String STATUS_INPUT_REQUIRED = "Input Required";
  public static final String US_CMRISSUINGCOUNTRY = "897";
  private static Map<String, String> rejectionReasons = new HashMap<String, String>();

  public static void refresh() {
    emailTemplate = null;
  }

  /**
   * Does all the needed processing for action 'Claim'
   * 
   * @param record
   * @param request
   */
  public static void setClaimDetails(Admin record, HttpServletRequest request) {
    AppUser user = AppUser.getUser(request);
    record.setLockTs(SystemUtil.getCurrentTimestamp());
    record.setLockInd(CmrConstants.YES_NO.Y.toString());
    record.setLockByNm(user.getBluePagesName());
    record.setLockBy(user.getIntranetId());
  }

  /**
   * Clears Lock (Claim) fields
   * 
   * @param record
   * @param request
   */
  public static void clearClaimDetails(Admin record) {
    record.setLockTs(null);
    record.setLockInd(null);
    record.setLockByNm(null);
    record.setLockBy(null);
  }

  /**
   * Creates a basic Workflow history record
   * 
   * @param service
   * @param entityManager
   * @param request
   * @param admin
   * @param cmt
   * @throws CmrException
   * @throws SQLException
   */
  public static void createWorkflowHistory(BaseService<?, ?> service, EntityManager entityManager, HttpServletRequest request, Admin admin,
      String cmt, String action) throws CmrException, SQLException {
    createWorkflowHistory(service, entityManager, request, admin, cmt, action, null, null, false, null, null, null, null);
  }

  /**
   * Creates a basic Comment log record
   * 
   * @param service
   * @param entityManager
   * 
   * @param app
   *          user
   * @param req
   *          id
   * @param cmt
   * @throws CmrException
   * @throws SQLException
   */
  public static void createCommentLog(BaseService<?, ?> service, EntityManager entityManager, AppUser user, long reqId, String cmt)
      throws CmrException, SQLException {
    ReqCmtLog reqCmtLog = new ReqCmtLog();
    ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
    reqCmtLogpk.setCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    reqCmtLog.setId(reqCmtLogpk);
    reqCmtLog.setReqId(reqId);
    reqCmtLog.setCmt(cmt);
    // save cmtlockedIn as Y default for current realese
    reqCmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    reqCmtLog.setCreateById(user.getIntranetId());
    reqCmtLog.setCreateByNm(user.getBluePagesName());
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    service.createEntity(reqCmtLog, entityManager);

  }

  /**
   * Adds the user in the notify list
   * 
   * @param userId
   * @param reqId
   */
  public static void addToNotifyList(BaseService<?, ?> service, EntityManager entityManager, AppUser user, long reqId) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.CHECKNOTIFLIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("USER_ID", user.getIntranetId());
    if (!query.exists()) {
      NotifList notif = new NotifList();
      NotifListPK pk = new NotifListPK();
      pk.setReqId(reqId);
      pk.setNotifId(user.getIntranetId());
      notif.setId(pk);
      notif.setNotifNm(user.getBluePagesName());

      service.createEntity(notif, entityManager);
    }
  }

  public static void createWorkflowHistory(BaseService<?, ?> service, EntityManager entityManager, HttpServletRequest request, Admin admin,
      String cmt, String action, String sendToId, String sendToNm, boolean complete, String rejectReason, String rejReasonCd, String rejSupplInfo1,
      String rejSupplInfo2) throws CmrException, SQLException {
    AppUser user = AppUser.getUser(request);

    completeLastHistoryRecord(entityManager, admin.getId().getReqId());

    WfHist hist = new WfHist();
    WfHistPK histpk = new WfHistPK();
    histpk.setWfId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "WF_ID"));
    hist.setId(histpk);
    hist.setCmt(cmt);
    hist.setReqStatus(admin.getReqStatus());
    hist.setCreateById(user.getIntranetId());
    hist.setCreateByNm(user.getBluePagesName());
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setReqId(admin.getId().getReqId());
    hist.setRejReason(rejectReason);
    hist.setRejReasonCd(rejReasonCd);
    hist.setRejSupplInfo1(rejSupplInfo1);
    hist.setRejSupplInfo2(rejSupplInfo2);
    // String actionDesc = getActionDescription(entityManager, action);
    String actionDesc = action != null && action.length() > 3 ? action : getActionDescription(entityManager, action);
    hist.setReqStatusAct(actionDesc);

    if (sendToId != null) {
      hist.setSentToId(sendToId);
    }

    if (sendToNm != null) {
      hist.setSentToNm(sendToNm);
    }
    if (StringUtils.isNotBlank(rejReasonCd) && StringUtils.isBlank(rejectReason)) {
      hist.setRejReason(getRejectionReason(entityManager, rejReasonCd));
    }
    if (StringUtils.isNotBlank(hist.getRejReason()) && hist.getRejReason().length() > 60) {
      hist.setRejReason(hist.getRejReason().substring(0, 60));
    }

    // trim comment
    if (hist.getCmt() != null && hist.getCmt().length() > 1000) {
      hist.setCmt(hist.getCmt().substring(0, 999));
    }

    if (hist.getRejReason() != null && hist.getRejReason().length() > 60) {
      hist.setRejReason(hist.getRejReason().substring(0, 56) + "...");
    }

    if (complete) {
      hist.setCompleteTs(SystemUtil.getCurrentTimestamp());
    }

    service.createEntity(hist, entityManager);

    sendEmailNotifications(entityManager, admin, hist);
  }

  public static void createWorkflowHistory(BaseService<?, ?> service, EntityManager entityManager, String user, Admin admin, String cmt,
      String action, String sendToId, String sendToNm, boolean complete, String rejectReason, String rejReasonCd) throws CmrException, SQLException {

    completeLastHistoryRecord(entityManager, admin.getId().getReqId());

    WfHist hist = new WfHist();
    WfHistPK histpk = new WfHistPK();
    histpk.setWfId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "WF_ID"));
    hist.setId(histpk);
    hist.setCmt(cmt);
    hist.setReqStatus(admin.getReqStatus());
    hist.setCreateById(user);
    hist.setCreateByNm(user);
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setReqId(admin.getId().getReqId());
    hist.setRejReason(rejectReason);
    hist.setRejReasonCd(rejReasonCd);
    // String actionDesc = getActionDescription(entityManager, action);
    String actionDesc = action != null && action.length() > 3 ? action : getActionDescription(entityManager, action);
    hist.setReqStatusAct(actionDesc);

    if (sendToId != null) {
      hist.setSentToId(sendToId);
    }

    if (sendToNm != null) {
      hist.setSentToNm(sendToNm);
    }

    if (complete) {
      hist.setCompleteTs(SystemUtil.getCurrentTimestamp());
    }

    // trim comment
    if (hist.getCmt() != null && hist.getCmt().length() > 1000) {
      hist.setCmt(hist.getCmt().substring(0, 999));
    }

    if (hist.getRejReason() != null && hist.getRejReason().length() > 60) {
      hist.setRejReason(hist.getRejReason().substring(0, 56) + "...");
    }

    service.createEntity(hist, entityManager);

    sendEmailNotifications(entityManager, admin, hist);
  }

  private static void completeLastHistoryRecord(EntityManager entityManager, long reqId) {
    PreparedQuery update = new PreparedQuery(entityManager, ExternalizedQuery.getSql("WORK_FLOW.COMPLETE_LAST"));
    update.setParameter("REQ_ID", reqId);
    update.executeSql();
  }

  private static String getActionDescription(EntityManager entityManager, String action) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETACTIONDESC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ACTION", action);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      return (String) results.get(0)[0];
    }
    return action;
  }

  public static void sendEmailNotifications(EntityManager entityManager, Admin admin, WfHist history) {

    String sourceSysSkip = admin.getSourceSystId() + ".SKIP";
    String onlySkipPartner = SystemParameters.getString(sourceSysSkip);
    boolean skip = false;
    String reqStatus = admin.getReqStatus();
    List<String> reqStatusFrMailNotif = Arrays.asList("PRJ", "COM");
    // CREATCMR - 2625 -> WW Status Change notifications in CreateCMR
    if ((StringUtils.isNotBlank(admin.getSourceSystId()) && "Y".equals(onlySkipPartner)) || (!reqStatusFrMailNotif.contains(reqStatus))) {
      skip = true;
    }

    if (skip) {
      return;
    }

    sendEmailNotifications(entityManager, admin, history, false, false);
  }

  /**
   * Sends the email notification when a status of a request changes
   * 
   * @param entityManager
   * @param admin
   * @param history
   */
  public static void sendEmailNotifications(EntityManager entityManager, Admin admin, WfHist history, boolean excludeRequester,
      boolean legacyDirect) {
    List<String> reqStatusFrMailNotif = Arrays.asList("PRJ", "COM");
    if (!reqStatusFrMailNotif.contains(admin.getReqStatus())) {
      return;
    }
    String cmrno = "";
    String siteId = "";
    String rejectReason = history.getRejReason();
    String rejReasonCd = history.getRejReasonCd();

    String cmrIssuingCountry = CreateCMRBPHandler.getCmrIssuingCntry(entityManager, admin);

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETNOTIFLIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", history.getReqId());
    query.setParameter("USER_ID", history.getCreateById());
    query.setParameter("REQ_STATUS", history.getReqStatus());

    StringBuilder recipients = new StringBuilder();
    String status = null;
    List<Object[]> results = query.getResults();
    for (Object[] result : results) {
      if (!excludeRequester || (excludeRequester && !result[0].toString().toLowerCase().equals(admin.getRequesterId().toLowerCase()))) {
        recipients.append(recipients.length() > 0 ? "," : "");
        recipients.append((String) result[0]);
        if (status == null) {
          status = (String) result[1];
        }
      }
    }

    String processingType = getProcessingType(entityManager, admin.getId().getReqId());
    if (processingType == null) {
      processingType = "";
    }

    // add to this list in the future if needed
    List<String> forceSendTypes = Arrays.asList("LD", "MD", "MA");

    if (!StringUtils.isBlank(admin.getSourceSystId())) {
      // for external creations, ensure that the requesterId is always notified,
      // Story 1741739
      LOG.debug("Appending requester ID " + admin.getRequesterId() + " for external request.");
      recipients.append(recipients.length() > 0 ? "," : "");
      recipients.append(admin.getRequesterId());

      // for any external requests, only send when status is PRJ or COM
      // for COM, only send the first COM notif (RDC_PROCESSING_MSG is empty)
      if (!"PRJ".equals(admin.getReqStatus()) && !"COM".equals(admin.getReqStatus())) {
        return;
      }

      if (!forceSendTypes.contains(processingType)) {
        // for legacy direct, always send
        if ("COM".equals(admin.getReqStatus()) && !StringUtils.isBlank(admin.getRdcProcessingStatus())) {
          return;
        }
      }

      // CMR-3996 - if CreateCMR is the source, do not notify anyone
      if ("CreateCMR".equals(admin.getSourceSystId())) {
        return;
      }

    }

    if (recipients.toString().trim().length() == 0 && !("PPN".equals(history.getReqStatus()))) {
      return; // no recipients, just return
    }

    // code to retrieve the CMR number and Site id for req_ID

    sql = ExternalizedQuery.getSql("REQUESTENTRY.GETCMRANDSITEID");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", history.getReqId());
    Data data = query.getSingleResult(Data.class);

    cmrno = data.getCmrNo();
    siteId = data.getSitePartyId();

    if (rejectReason == null) {
      rejectReason = "";
    }

    if (rejReasonCd == null) {
      rejReasonCd = "";
    }
    if (cmrno == null) {
      cmrno = "";
    }
    if (siteId == null) {
      siteId = "";
    }

    String from = SystemConfiguration.getValue("MAIL_FROM");
    String subject = SystemConfiguration.getValue("MAIL_SUBJECT");
    if (emailTemplate == null) {
      emailTemplate = getEmailTemplate();
    }

    // // add a completed to subject for completed requests
    // if ("COM".equals(history.getReqStatus())) {
    // subject += " - Completed";
    // }

    String email = new String(emailTemplate);
    if (!StringUtils.isBlank(admin.getSourceSystId())) {
      String extTemplate = getExternalEmailTemplate(admin.getSourceSystId());
      if (!StringUtils.isBlank(extTemplate)) {
        email = extTemplate;
      }
    }
    String type = "-";
    String reqType = admin.getReqType();
    if ("C".equals(reqType)) {
      type = "Create";
    } else if ("U".equals(reqType)) {
      type = "Update";
    } else if ("M".equals(reqType)) {
      type = "Mass Update";
    } else if ("D".equals(reqType)) {
      type = "Delete";
    } else if ("R".equals(reqType)) {
      type = "Reactivate";
    } else if ("N".equals(reqType)) {
      type = "Mass Create";
    } else if ("E".equals(reqType)) {
      // CREATCMR-6639
      if (cmrIssuingCountry != null && US_CMRISSUINGCOUNTRY.equalsIgnoreCase(cmrIssuingCountry)) {
        type = "Update Enterprise Name";
      } else {
        type = "Update by Enterprise";
      }
      LOG.debug("ReqType is " + type.toString() + " , and status is " + status.toString());
    } else {
      type = "-";
    }
    // String type = "C".equals(admin.getReqType()) ? "Create" :
    // ("U".equals(admin.getReqType()) ? "Update" : "-");

    String customerName = "-";
    if (!StringUtils.isBlank(admin.getMainCustNm1())) {
      customerName = admin.getMainCustNm1();
    }
    if (!StringUtils.isBlank(admin.getMainCustNm2())) {
      customerName += " " + admin.getMainCustNm2();
    }
    customerName = customerName.trim();
    /*
     * Project : CreateCMR(Story 1185882:URL link of the request part of e-mail
     * notification) File Name : RequestUtil.java Purpose : add url link in
     * e-mail notification Created on (YYYY-MM-DD) : 2017-05-03 Author : Mukesh
     * Kumar
     */
    // ==================Story 1185882 START=========================
    String directUrlLink = SystemConfiguration.getValue("APPLICATION_URL") + "/login?r=" + admin.getId().getReqId();
    // ==================Story 1185882 END=========================

    directUrlLink = "Click <a href=\"" + directUrlLink + "\">Here</a>";

    boolean includeUser = false;
    String embeddedLink = "";
    if ("COM".equals(history.getReqStatus()) || "COM".equals(admin.getReqStatus())) {
      embeddedLink = Feedback.genEmbeddedNPSLink(entityManager, admin, data.getCmrIssuingCntry());
    } else if ("PPN".equals(history.getReqStatus())) {
      embeddedLink = Feedback.generateEmeddedContactLink(data);
      includeUser = !StringUtils.isEmpty(embeddedLink);
    }

    if (includeUser) {
      recipients.append(recipients.length() > 0 ? "," : "");
      recipients.append(admin.getRequesterId()); // use requester id explicitly
                                                 // for PPN
      if (StringUtils.isEmpty(status)) {
        status = "Processing Pending"; // bad bad hardcoding
      }
    }
    if (recipients.toString().length() == 0) {
      return;
    }

    String statusFromDb = getStatusDesc(entityManager, admin.getReqStatus());
    if (!StringUtils.isEmpty(statusFromDb)) {
      status = statusFromDb;
    }

    if (subject.contains("{0}")) {
      if (cmrIssuingCountry != null && US_CMRISSUINGCOUNTRY.equalsIgnoreCase(cmrIssuingCountry) && status != null && status.equals(STATUS_REJECTED)
          && !StringUtils.isBlank(admin.getSourceSystId()) && SOURCE.equalsIgnoreCase(admin.getSourceSystId())) {
        subject = MessageFormat.format(subject, history.getReqId() + "", STATUS_INPUT_REQUIRED);
      } else {
        // 1048370 - add request id in the mail
        subject = MessageFormat.format(subject, history.getReqId() + "", status);
      }
    }

    String histContent = history.getCmt();
    histContent = histContent != null ? StringUtils.replace(histContent, "\n", "<br>") : "-";
    if (status != null && status.toUpperCase().contains("REJECTED")) {
      StringBuffer temp = new StringBuffer(email);
      int tempstart = temp.indexOf("{5}");
      int insertstart = tempstart + 17;
      String rejRes = "<tr><th style=\"background:#EEE;text-align:left;width:200px\">Reject Reason:</th><td style=\"background:#EEE;\">{10}</td></tr>";
      if (cmrIssuingCountry != null && US_CMRISSUINGCOUNTRY.equalsIgnoreCase(cmrIssuingCountry) && !StringUtils.isBlank(admin.getSourceSystId())
          && SOURCE.equalsIgnoreCase(admin.getSourceSystId())) {
        rejRes = "<tr><th style=\"background:#EEE;text-align:left;width:200px\">Input Required Reason:</th><td style=\"background:#EEE;\">{10}</td></tr>";
      }
      rejRes = formatRejectionInfo(rejRes, history);
      temp.insert(insertstart, rejRes);
      email = temp.toString();
    } else {
      StringBuffer temp = new StringBuffer(email);
      int tempstart = temp.indexOf("{5}");
      int insertstart = tempstart + 17;
      String rejRes = "{10}";
      temp.insert(insertstart, rejRes);
      email = temp.toString();
    }

    List<Object> params = new ArrayList<>();
    params.add(history.getReqId() + ""); // {0}
    params.add(customerName); // {1}
    params.add(siteId); // {2}
    params.add(cmrno); // {3}
    params.add(type); // {4}
    if (cmrIssuingCountry != null && US_CMRISSUINGCOUNTRY.equalsIgnoreCase(cmrIssuingCountry) && status != null && status.equals(STATUS_REJECTED)
        && !StringUtils.isBlank(admin.getSourceSystId()) && SOURCE.equalsIgnoreCase(admin.getSourceSystId())) {
      params.add(STATUS_INPUT_REQUIRED);
    } else {
      params.add(status); // {5}
    }
    if (history.getCreateById().equals(history.getCreateByNm())) {
      params.add(history.getCreateById()); // {6}
    } else {
      params.add(history.getCreateByNm() + " (" + history.getCreateById() + ")"); // {6}
    }
    params.add(CmrConstants.DATE_FORMAT().format(history.getCreateTs())); // {7}
    params.add(histContent); // {8}
    params.add(directUrlLink); // {9}
    params.add(rejectReason); // {10}
    params.add(embeddedLink); // {11}

    String country = getIssuingCountry(entityManager, cmrIssuingCountry);
    country = cmrIssuingCountry + (StringUtils.isBlank(country) ? "" : " - " + country);

    email = StringUtils.replace(email, "$COUNTRY$", country);

    if (!StringUtils.isBlank(admin.getSourceSystId())) {
      ExternalSystemUtil.addExternalMailParams(entityManager, params, admin);
    }

    email = MessageFormat.format(email, params.toArray(new Object[0]));

    String host = SystemConfiguration.getValue("MAIL_HOST");

    Email mail = new Email();
    mail.setSubject(subject);
    mail.setTo(recipients.toString());
    mail.setFrom(from);

    mail.setMessage(email);
    mail.setType(MessageType.HTML);

    String sourceSysSkip = admin.getSourceSystId() + ".SKIP";
    String onlySkipPartner = SystemParameters.getString(sourceSysSkip);
    boolean skip = false;

    if (StringUtils.isNotBlank(admin.getSourceSystId()) && "Y".equals(onlySkipPartner)) {
      skip = true;
    }

    if (skip == false) {
      mail.send(host);
    }

  }

  /**
   * String gets the fully qualified country name
   * 
   * @param entityManager
   * @param country
   * @return
   */
  private static String getIssuingCountry(EntityManager entityManager, String country) {
    // TODO move to cmr-queries
    try {
      String sql = "select NM from CREQCMR.SUPP_CNTRY where CNTRY_CD = :CNTRY";
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CNTRY", country);
      query.setForReadOnly(true);
      return query.getSingleResult(String.class);
    } catch (Exception e) {
      LOG.warn("Error in getting issuing country name", e);
      return null;
    }
  }

  private static String formatRejectionInfo(String current, WfHist rejection) {
    StringBuilder rejInfo = new StringBuilder();
    rejInfo.append(current);
    String info1 = rejection.getRejSupplInfo1();
    if (StringUtils.isBlank(info1)) {
      info1 = "(not specified)";
    }
    String info2 = rejection.getRejSupplInfo2();
    if (StringUtils.isBlank(info2)) {
      info2 = "(not specified)";
    }
    if (!StringUtils.isBlank(rejection.getRejReasonCd())) {
      switch (rejection.getRejReasonCd()) {
      case "DUPC":
        rejInfo.append("<tr><th style=\"background:#EEE;text-align:left;width:200px\">Duplicate CMR#:</th><td style=\"background:#EEE;\">" + info1
            + "</td></tr>");
        rejInfo.append("<tr><th style=\"background:#EEE;text-align:left;width:200px\">Sold-to KUNNR:</th><td style=\"background:#EEE;\">" + info2
            + "</td></tr>");
        break;
      case "ADDR":
        break;
      case "IBM":
        break;
      case "VAT":
        break;
      case "MDOC":
        rejInfo.append("<tr><th style=\"background:#EEE;text-align:left;width:200px\">Missing Document:</th><td style=\"background:#EEE;\">" + info1
            + "</td></tr>");
        break;
      case "MAPP":
        rejInfo.append("<tr><th style=\"background:#EEE;text-align:left;width:200px\">Approval Type:</th><td style=\"background:#EEE;\">" + info1
            + "</td></tr>");
        rejInfo.append(
            "<tr><th style=\"background:#EEE;text-align:left;width:200px\">Approver:</th><td style=\"background:#EEE;\">" + info2 + "</td></tr>");
        break;
      case "OTH":
        break;
      case "DUPR":
        rejInfo.append("<tr><th style=\"background:#EEE;text-align:left;width:200px\">Duplicate Request ID:</th><td style=\"background:#EEE;\">"
            + info1 + "</td></tr>");
        break;
      case "UNCL":
        break;
      case "PROS":
        break;
      case "TYPR":
        rejInfo.append(
            "<tr><th style=\"background:#EEE;text-align:left;width:200px\">Correct Type:</th><td style=\"background:#EEE;\">" + info1 + "</td></tr>");
        break;
      }
    }
    return rejInfo.toString();
  }

  private static String getStatusDesc(EntityManager entityManager, String reqStatus) {
    // catch all code to fix all null status
    // 1766947: <Conversion API>While providing email id other than IBM id in
    // requesterid parameter, a status with 'null' appears in the email
    // notification for "New Status" tag.
    // jzamora
    String sql = ExternalizedQuery.getSql("SYSTEM.STATUSDESCMAINT");
    try {
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setForReadOnly(true);
      query.setParameter("REQ_STATUS", reqStatus);
      StatusDesc statusDesc = query.getSingleResult(StatusDesc.class);

      if (statusDesc != null) {
        return statusDesc.getStatusDesc();
      }
    } catch (Exception e) {
      LOG.warn("Cannot get status from status desc. Error: " + e.getMessage());
    }
    return null;

  }

  private static String getEmailTemplate() {
    StringBuilder sb = new StringBuilder();
    try {
      InputStream is = null;
      if (batchemailTemplate != null) {
        is = ConfigUtil.getResourceStream("cmr-email_batch.html");
      } else {
        is = ConfigUtil.getResourceStream("cmr-email.html");
      }

      try {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        try {
          BufferedReader br = new BufferedReader(isr);
          try {
            String line = null;
            while ((line = br.readLine()) != null) {
              sb.append(line);
            }
          } finally {
            br.close();
          }
        } finally {
          isr.close();
        }
      } finally {
        is.close();
      }
    } catch (Exception e) {
      LOG.error("Error when loading Email template.", e);
    }
    return sb.toString();
  }

  private static String getExternalEmailTemplate(String sourceSystId) {
    try {
      InputStream is = ConfigUtil.getResourceStream((sourceSystId.toLowerCase()) + ".html");

      try {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        try {
          BufferedReader br = new BufferedReader(isr);
          try {
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
              sb.append(line);
            }
            return sb.toString();
          } finally {
            br.close();
          }
        } finally {
          isr.close();
        }
      } finally {
        is.close();
      }
    } catch (Exception e) {
      LOG.error("Error when loading Email template.", e);
      return null;
    }
  }

  private static String getCMREmailTemplate() {
    StringBuilder sb = new StringBuilder();
    try {
      InputStream is = ConfigUtil.getResourceStream("cmr-email_mc.html");
      try {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        try {
          BufferedReader br = new BufferedReader(isr);
          try {
            String line = null;
            while ((line = br.readLine()) != null) {
              sb.append(line);
            }
          } finally {
            br.close();
          }
        } finally {
          isr.close();
        }
      } finally {
        is.close();
      }
    } catch (Exception e) {
      LOG.error("Error when loading Email template.", e);
    }
    return sb.toString();
  }

  public static CmrInternalTypes computeInternalType(EntityManager entityManager, String reqType, String cmrIssuingCountry, long reqId) {
    String typeSql = ExternalizedQuery.getSql("REQUESTENTRY.FIND_INTERNAL_TYPE");
    PreparedQuery typeQuery = new PreparedQuery(entityManager, typeSql);
    typeQuery.setParameter("REQ_TYPE", reqType);
    typeQuery.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCountry);
    typeQuery.setForReadOnly(true);
    List<CmrInternalTypes> types = typeQuery.getResults(CmrInternalTypes.class);
    String checkSql = "";
    PreparedQuery query = null;
    for (CmrInternalTypes type : types) {
      checkSql = "select 1 from CREQCMR.ADMIN a, CREQCMR.DATA d where a.REQ_ID = d.REQ_ID and a.REQ_ID = :REQ_ID and ";
      checkSql += type.getCondition();
      query = new PreparedQuery(entityManager, checkSql);
      query.setParameter("REQ_ID", reqId);
      if (query.exists()) {
        return type;
      }
    }
    return null;
  }

  public static GEOHandler getGEOHandler(String cmrIssuingCntry) {
    String handlerClass = SystemConfiguration.getSystemProperty("geoHandler." + cmrIssuingCntry);
    try {
      if (!StringUtils.isEmpty(handlerClass)) {
        GEOHandler converter = (GEOHandler) Class.forName(handlerClass).newInstance();
        return converter;
      }
    } catch (Exception e) {
    }
    return null;
  }

  public static synchronized WfHist createWorkflowHistoryFromBatch(EntityManager entityManager, String user, Admin admin, String cmt, String action,
      String sendToId, String sendToNm, boolean complete) throws CmrException, SQLException {
    return createWorkflowHistoryFromBatch(entityManager, user, admin, cmt, action, sendToId, sendToNm, complete, true);
  }

  public static synchronized WfHist createWorkflowHistoryFromBatch(EntityManager entityManager, String user, Admin admin, String cmt, String action,
      String sendToId, String sendToNm, boolean complete, boolean sendMail, String rejReason) throws CmrException, SQLException {
    return createWorkflowHistoryFromBatch(entityManager, user, admin, cmt, action, sendToId, sendToNm, complete, sendMail, rejReason, "OTH", null,
        null);
  }

  public static synchronized WfHist createWorkflowHistoryFromBatch(EntityManager entityManager, String user, Admin admin, String cmt, String action,
      String sendToId, String sendToNm, boolean complete, boolean sendMail, String rejReason, String rejCode, String info1, String info2)
      throws CmrException, SQLException {
    completeLastHistoryRecord(entityManager, admin.getId().getReqId());

    WfHist hist = new WfHist();
    WfHistPK histpk = new WfHistPK();
    histpk.setWfId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "WF_ID"));
    hist.setId(histpk);
    hist.setCmt(cmt);
    hist.setReqStatus(admin.getReqStatus());
    hist.setCreateById(user);
    hist.setCreateByNm(user);
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setReqId(admin.getId().getReqId());
    hist.setRejReason(rejReason);
    hist.setReqStatusAct(action);

    if (sendToId != null) {
      hist.setSentToId(sendToId);
    }

    if (sendToNm != null) {
      hist.setSentToNm(sendToNm);
    }

    if (complete) {
      hist.setCompleteTs(SystemUtil.getCurrentTimestamp());
    }

    entityManager.persist(hist);
    entityManager.flush();

    String sourceSysSkip = admin.getSourceSystId() + ".SKIP";
    String onlySkipPartner = SystemParameters.getString(sourceSysSkip);
    boolean skip = false;
    if (StringUtils.isNotBlank(admin.getSourceSystId()) && "Y".equals(onlySkipPartner)) {
      skip = true;
    }

    if (sendMail && (skip == false)) {
      sendEmailNotifications(entityManager, admin, hist);
    }

    return hist;
  }

  public static synchronized WfHist createWorkflowHistoryFromBatch(EntityManager entityManager, String user, Admin admin, String cmt, String action,
      String sendToId, String sendToNm, boolean complete, boolean sendMail) throws CmrException, SQLException {
    return createWorkflowHistoryFromBatch(entityManager, user, admin, cmt, action, sendToId, sendToNm, complete, sendMail, null);
  }

  public static synchronized void createCommentLogFromBatch(EntityManager entityManager, String user, long reqId, String cmt)
      throws CmrException, SQLException {
    int cmtLength = cmt.length();
    if (cmtLength < 2000) {
      ReqCmtLog cmtLog = new ReqCmtLog();
      ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
      reqCmtLogpk.setCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
      cmtLog.setId(reqCmtLogpk);
      cmtLog.setReqId(reqId);
      cmtLog.setCmt(cmt);
      cmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
      cmtLog.setCreateById(user);
      cmtLog.setCreateByNm(user);
      cmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
      cmtLog.setUpdateTs(cmtLog.getCreateTs());

      entityManager.persist(cmtLog);
      entityManager.flush();

    } else {
      int CMT_PART_SIZE = 1800;
      List<String> cmtList = splitBySize(cmt, CMT_PART_SIZE);
      for (String partCmt : cmtList) {

        ReqCmtLog cmtLog = new ReqCmtLog();
        ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
        reqCmtLogpk.setCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
        cmtLog.setId(reqCmtLogpk);
        cmtLog.setReqId(reqId);
        cmtLog.setCmt(partCmt);
        cmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
        cmtLog.setCreateById(user);
        cmtLog.setCreateByNm(user);
        cmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
        cmtLog.setUpdateTs(cmtLog.getCreateTs());

        entityManager.persist(cmtLog);
        entityManager.flush();

      }
    }
  }

  /**
   * Break the cmt in multiple parts when cmt length is more than 2000
   * characters
   * 
   * @param str
   * @param size
   */
  public static List<String> splitBySize(String str, int size) {
    StringTokenizer tok = new StringTokenizer(str, " ");
    StringBuilder output = new StringBuilder(str.length());
    List<String> strList = new ArrayList<String>((str.length() + size - 1) / size);
    int lineLen = 0;
    while (tok.hasMoreTokens()) {
      String word = tok.nextToken();
      output.append(word);
      lineLen = output.toString().length();

      if (lineLen < size) {
        output.append(" ");
      } else {
        strList.add(output.toString());
        lineLen = 0;
        output = new StringBuilder(str.length());
      }
    }
    if (output.toString().length() > 0) {
      strList.add(output.toString());
    }

    return strList;
  }

  /**
   * Sends the email notification when a status of a request changes
   * 
   * @param entityManager
   * @param admin
   * @param history
   */
  public static void sendBatchEmailNotifications(EntityManager entityManager, List<MassCreateBatchEmailModel> ufailList, WfHist history) {

    batchemailTemplate = "cmr-email_batch.html";
    refresh();

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETNOTIFLIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", history.getReqId());
    query.setParameter("USER_ID", history.getCreateById());
    query.setParameter("REQ_STATUS", history.getReqStatus());

    StringBuilder recipients = new StringBuilder();
    String status = null;
    List<Object[]> results = query.getResults();
    for (Object[] result : results) {
      recipients.append(recipients.length() > 0 ? "," : "");
      recipients.append((String) result[0]);
      if (status == null) {
        status = (String) result[1];
      }
    }

    if (recipients.toString().trim().length() == 0) {
      return; // no recipients, just return
    }

    String from = SystemConfiguration.getValue("MAIL_FROM");
    String subject = "Mass Create Processing encountered errors during Auto-Update for Request ID {0}";
    if (emailTemplate == null) {
      emailTemplate = getEmailTemplate();
    }

    if (subject.contains("{0}")) {
      // 1048370 - add request id in the mail
      subject = MessageFormat.format(subject, history.getReqId() + "");
    }

    // add a completed to subject for completed requests
    if ("COM".equals(history.getReqStatus())) {
      subject += " - Completed";
    }

    String email = new String(emailTemplate);
    StringBuffer temp = new StringBuffer(email);
    StringBuffer mailrec = new StringBuffer();
    int tempstart = temp.indexOf("Error Message");
    for (MassCreateBatchEmailModel failList : ufailList) {
      mailrec.append("<tr>");
      mailrec.append("<td>");
      mailrec.append(failList.getCustName());
      mailrec.append("</td>");
      mailrec.append("<td>");
      mailrec.append(failList.getCmrNo());
      mailrec.append("</td>");
      mailrec.append("<td>");
      mailrec.append(failList.getErrorMsg());
      mailrec.append("</td>");
      mailrec.append("<tr>");
    }

    int insertstart = tempstart + 31;
    temp.insert(insertstart, mailrec.toString());
    email = temp.toString();
    String host = SystemConfiguration.getValue("MAIL_HOST");

    Email mail = new Email();
    mail.setSubject(subject);
    mail.setTo(recipients.toString());
    mail.setFrom(from);
    mail.setMessage(email);
    mail.setType(MessageType.HTML);

    Admin admin = new Admin();
    String sourceSysSkip = admin.getSourceSystId() + ".SKIP";
    String onlySkipPartner = SystemParameters.getString(sourceSysSkip);
    boolean skip = false;

    if (StringUtils.isNotBlank(admin.getSourceSystId()) && "Y".equals(onlySkipPartner)) {
      skip = true;
    }

    if (skip == false) {
      mail.send(host);
    }
    batchemailTemplate = null;
    refresh();
  }

  /**
   * Sends the email notification when a status of a request changes
   * 
   * @param entityManager
   * @param admin
   * @param history
   * @throws Exception
   */
  public static void sendMassCreateCMRNotifications(EntityManager entityManager, List<MassCreateBatchEmailModel> records, Admin admin)
      throws Exception {

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETNOTIFLIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    query.setParameter("USER_ID", "x");
    query.setParameter("REQ_STATUS", admin.getReqStatus());

    StringBuilder recipients = new StringBuilder();
    String status = null;
    List<Object[]> results = query.getResults();
    for (Object[] result : results) {
      recipients.append(recipients.length() > 0 ? "," : "");
      recipients.append((String) result[0]);
      if (status == null) {
        status = (String) result[1];
      }
    }

    if (recipients.toString().trim().length() == 0) {
      return; // no recipients, just return
    }

    String from = SystemConfiguration.getValue("MAIL_FROM");
    String subject = "CMR Nos. Generated and Processing Summary for Mass Create Request ID {0}";
    String template = getCMREmailTemplate();

    if (subject.contains("{0}")) {
      // 1048370 - add request id in the mail
      subject = MessageFormat.format(subject, admin.getId().getReqId() + "");
    }

    String email = new String(template);

    StringBuilder sb = new StringBuilder();
    for (MassCreateBatchEmailModel record : records) {
      sb.append("<tr>");
      sb.append("  <td>").append(record.getRowNo()).append("</td>");
      sb.append("  <td>").append(StringUtils.isBlank(record.getCmrNo()) ? "&nbsp;" : record.getCmrNo()).append("</td>");
      sb.append("  <td>").append(record.getCustName()).append("</td>");
      sb.append("  <td>").append(record.getSapNo()).append("</td>");
      sb.append("  <td>").append(StringUtils.isBlank(record.getErrorMsg()) ? "&nbsp;" : record.getErrorMsg()).append("</td>");
      sb.append("</tr>");
    }

    String fileName = admin.getFileName();
    File massCreateFile = new File(fileName);
    if (massCreateFile.exists()) {
      try {
        LOG.debug("Updating file with CMR Nos.");
        MassCreateFileParser parser = new MassCreateFileParser();
        MassCreateFile file = null;
        FileInputStream is = new FileInputStream(massCreateFile);
        try {
          file = parser.parse(is, admin.getId().getReqId(), admin.getIterationId(), false);
        } finally {
          is.close();
        }
        if (file != null) {
          for (MassCreateFileRow row : file.getRows()) {
            for (MassCreateBatchEmailModel model : records) {
              if (model.getRowNo() == row.getSeqNo()) {
                row.setCmrNo(model.getCmrNo());
                break;
              }
            }
          }
        }
        file.updateFile(parser, admin.getFileName(), true);

      } catch (Exception e) {
        LOG.error("Error when parsing file.", e);
      }
    }
    email = MessageFormat.format(email, admin.getId().getReqId() + "", sb.toString());

    String host = SystemConfiguration.getValue("MAIL_HOST");

    Email mail = new Email();
    mail.setSubject(subject);
    mail.setTo(recipients.toString());
    mail.setFrom(from);
    mail.setMessage(email);
    mail.setType(MessageType.HTML);
    if (massCreateFile.exists()) {
      mail.addAttachment(admin.getFileName());
    }

    mail.send(host);
  }

  /**
   * Checks if this {@link Addr} record has been updated. This method compares
   * with the {@link AddrRdc} equivalent and compares per field and filters
   * given the configuration on the corresponding {@link GEOHandler} for the
   * given CMR issuing country
   * 
   * @param entityManager
   * @param addr
   * @param cmrIssuingCntry
   * @return
   */
  public static boolean isUpdated(EntityManager entityManager, Addr addr, String cmrIssuingCntry) {
    if (entityManager == null || addr == null) {
      return false;
    }
    AddrPK pk = new AddrPK();
    pk.setAddrSeq(addr.getId().getAddrSeq());
    pk.setAddrType(addr.getId().getAddrType());
    pk.setReqId(addr.getId().getReqId());

    AddrRdc addrRdc = entityManager.find(AddrRdc.class, pk);
    if (addrRdc == null) {
      return false;
    }
    entityManager.detach(addrRdc);
    return isUpdated(addr, addrRdc, cmrIssuingCntry);
  }

  /**
   * Checks if this {@link Addr} record has been updated. This method compares
   * with the {@link AddrRdc} equivalent and compares per field and filters
   * given the configuration on the corresponding {@link GEOHandler} for the
   * given CMR issuing country
   * 
   * @param addr
   * @param addrRdc
   * @param cmrIssuingCntry
   * @return
   */
  public static boolean isUpdated(Addr addr, AddrRdc addrRdc, String cmrIssuingCntry) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;

    GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);

    if (JPHandler.isJPIssuingCountry(cmrIssuingCntry)) {
      // JP KANAKATA = CUST_NM4 + PO_BOX_CITY
      addrRdc.setCustNm4((addrRdc.getCustNm4() == null ? "" : addrRdc.getCustNm4()) + (addrRdc.getPoBoxCity() == null ? "" : addrRdc.getPoBoxCity()));
      addrRdc.setPoBoxCity(null);
      // JP Address = ADDR_TXT + ADDR_TXT_2
      addrRdc.setAddrTxt((addrRdc.getAddrTxt() == null ? "" : addrRdc.getAddrTxt()) + (addrRdc.getAddrTxt2() == null ? "" : addrRdc.getAddrTxt2()));
      addrRdc.setAddrTxt2(null);
    }

    for (Field field : Addr.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        // check if field is part of exemption list or is part of what to check
        // for the handler, if specified
        if (GEOHandler.ADDRESS_FIELDS_SKIP_CHECK.contains(srcName)
            || (handler != null && handler.getAddressFieldsForUpdateCheck(cmrIssuingCntry) != null
                && !handler.getAddressFieldsForUpdateCheck(cmrIssuingCntry).contains(srcName))) {
          continue;
        }

        if ("ID".equals(srcName) || "PCSTATEMANAGER".equals(srcName) || "PCDETACHEDSTATE".equals(srcName)) {
          continue;
        }

        try {
          trgField = AddrRdc.class.getDeclaredField(field.getName());

          field.setAccessible(true);
          trgField.setAccessible(true);

          Object srcVal = field.get(addr);
          Object trgVal = trgField.get(addrRdc);

          if (String.class.equals(field.getType())) {
            String srcStringVal = (String) srcVal;
            if (srcStringVal == null) {
              srcStringVal = "";
            }
            String trgStringVal = (String) trgVal;
            if (trgStringVal == null) {
              trgStringVal = "";
            }
            if (!StringUtils.equals(srcStringVal.trim(), trgStringVal.trim())) {
              LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
              return true;
            }
          } else {
            if (!ObjectUtils.equals(srcVal, trgVal)) {
              LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
              return true;
            }
          }
        } catch (NoSuchFieldException e) {
          // noop
          continue;
        } catch (Exception e) {
          LOG.trace("General error when trying to access field.", e);
          // no stored value or field not on addr rdc, return null for no
          // changes
          continue;
        }

      }
    }
    return false;
  }

  public static String generateChecklistLocalName(HttpServletRequest request) {
    CheckListModel checklist = (CheckListModel) request.getAttribute("checklist");
    String value = "";
    if (checklist != null && checklist.getLocalCustNm() != null) {
      value = StringUtils.replace(checklist.getLocalCustNm(), "\"", "&quot;");
    }
    return "<input type=\"text\" dojoType=\"dijit.form.TextBox\" style=\"width:400px\" maxlength=\"70\" name=\"localCustNm\" value=\"" + value
        + "\">";
  }

  public static String generateChecklistLocalAddress(HttpServletRequest request) {
    CheckListModel checklist = (CheckListModel) request.getAttribute("checklist");
    String value = "";
    if (checklist != null && checklist.getLocalAddr() != null) {
      value = StringUtils.replace(checklist.getLocalAddr(), "\"", "&quot;");
    }
    return "<input type=\"text\" dojoType=\"dijit.form.TextBox\" style=\"width:400px\" maxlength=\"70\" name=\"localAddr\" value=\"" + value + "\">";
  }

  public static String generateChecklistFreeTxtField1(HttpServletRequest request) {
    CheckListModel checklist = (CheckListModel) request.getAttribute("checklist");
    String value = "";
    if (checklist != null && checklist.getFreeTxtField1() != null) {
      value = StringUtils.replace(checklist.getFreeTxtField1(), "\"", "&quot;");
    }
    return "<input type=\"text\" dojoType=\"dijit.form.TextBox\" style=\"width:400px\" maxlength=\"70\" name=\"freeTxtField1\" value=\"" + value
        + "\">";
  }

  public static String generateChecklistFreeTxtField2(HttpServletRequest request) {
    CheckListModel checklist = (CheckListModel) request.getAttribute("checklist");
    String value = "";
    if (checklist != null && checklist.getFreeTxtField2() != null) {
      value = StringUtils.replace(checklist.getFreeTxtField2(), "\"", "&quot;");
    }
    return "<input type=\"text\" dojoType=\"dijit.form.TextBox\" style=\"width:400px\" maxlength=\"70\" name=\"freeTxtField2\" value=\"" + value
        + "\">";
  }

  public static String generateChecklistFreeTxtField3(HttpServletRequest request) {
    CheckListModel checklist = (CheckListModel) request.getAttribute("checklist");
    String value = "";
    if (checklist != null && checklist.getFreeTxtField3() != null) {
      value = StringUtils.replace(checklist.getFreeTxtField3(), "\"", "&quot;");
    }
    return "<input type=\"text\" dojoType=\"dijit.form.TextBox\" style=\"width:400px\" maxlength=\"70\" name=\"freeTxtField3\" value=\"" + value
        + "\">";
  }

  /**
   * Gets the sub industry code for the given ISIC
   * 
   * @param entityManager
   * @param isic
   * @param issuingCntry
   * @return
   */
  public static String getSubIndustryCd(EntityManager entityManager, String isic, String issuingCntry) {
    String sql = ExternalizedQuery.getSql("AUTOMATION.GET_SUB_INDUSTRY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISIC", isic);
    query.setParameter("GEO", "897".equals(issuingCntry) ? "US" : "WW");
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);

  }

  /**
   * Checks whether automation is configued for a particular country or not
   * 
   * @param entityManager
   * @param country
   * @return
   */
  public static String getAutomationConfig(EntityManager entityManager, String country) {
    String sql = ExternalizedQuery.getSql("AUTOMATION.CHECK_AND_RECOVER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("CNTRY", country != null && country.length() > 3 ? country.substring(0, 3) : country);
    List<Object[]> results = query.getResults(1);
    if (results != null && !results.isEmpty()) {
      return (String) results.get(0)[0];
    }
    return "";

  }

  /**
   * Checks the usage configuration for tradestyle names
   * 
   * @param entityManager
   * @param country
   * @return
   */
  public static String getTradestyleUsage(EntityManager entityManager, String country) {
    String sql = ExternalizedQuery.getSql("AUTOMATION.GET_TRADESTYLE_USAGE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("CNTRY", country != null && country.length() > 3 ? country.substring(0, 3) : country);
    String result = query.getSingleResult(String.class);
    return StringUtils.isNotBlank(result) ? result : "";

  }

  /**
   * Checks whether automation is configued for a particular country or not
   * 
   * @param entityManager
   * @param country
   * @return
   */
  public static String isDnBCountry(EntityManager entityManager, String country) {
    String sql = ExternalizedQuery.getSql("AUTOMATION.IS_DNB_COUNTRY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("CNTRY", country != null && country.length() > 3 ? country.substring(0, 3) : country);
    return query.getSingleResult(String.class);
  }

  /**
   * Checks whether quick search is configued as first interface for a
   * particular country or not
   * 
   * @param entityManager
   * @param country
   * @return
   */
  public static String isQuickSearchFirstEnabled(EntityManager entityManager, String country) {
    String sql = ExternalizedQuery.getSql("AUTOMATION.START_FROM_QUICK_SEARCH");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("CNTRY", country != null && country.length() > 3 ? country.substring(0, 3) : country);
    return query.getSingleResult(String.class);
  }

  /**
   * Gets the processing center where the request will be sent to
   * 
   * @param entityManager
   * @param country
   * @return
   */
  public static String getProcessingCenter(EntityManager entityManager, String country) {
    String sql = ExternalizedQuery.getSql("AUTOMATION.GET_PROC_CENTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", country);
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

  /**
   * Checks the current configuration for the automation for the country
   * 
   * @param country
   * @return
   */
  public static boolean isRequesterAutomationEnabled(String country) {
    try {
      EntityManager entityManager = JpaManager.getEntityManager();
      try {
        String autoConfig = getAutomationConfig(entityManager, country);
        return AutomationConst.AUTOMATE_REQUESTER.equals(autoConfig) || AutomationConst.AUTOMATE_BOTH.equals(autoConfig);
      } finally {
        entityManager.clear();
        entityManager.close();
      }
    } catch (Exception e) {
      LOG.warn("Status of requester automation cannot be determined", e);
      return false;
    }
  }

  /**
   * Checks whether quick search is configued as first interface for a
   * particular country or not
   * 
   * @param country
   * @return
   */
  public static boolean isQuickSearchFirstEnabled(String country) {
    try {
      EntityManager entityManager = JpaManager.getEntityManager();
      try {
        String quickSearchFirst = isQuickSearchFirstEnabled(entityManager, country);
        if ("Y".equals(quickSearchFirst)) {
          return true;
        } else {
          return false;
        }
      } finally {
        entityManager.clear();
        entityManager.close();
      }
    } catch (Exception e) {
      LOG.warn("Status of Quick Search cannot be determined", e);
      return false;
    }
  }

  public static String getAccessToken(HttpServletRequest request) {
    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals("LtpaToken")) {
        return cookie.getValue();
      }
    }
    return null;
  }

  private static String getProcessingType(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("MAIL.CHECK_PROC_TYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

  /**
   * Derives country information for the US
   * 
   * @param cmrRecord
   * @param company
   */
  public static void deriveUSCounty(FindCMRRecordModel cmrRecord, DnBCompany company) {
    String countyName = null;
    String stateCode = cmrRecord.getCmrState();
    if (!StringUtils.isBlank(company.getPrimaryCounty())) {
      countyName = company.getPrimaryCounty().toUpperCase();
    } else if (!StringUtils.isBlank(company.getMailingCounty())) {
      countyName = company.getMailingCounty().toUpperCase();
    }
    if (countyName != null && stateCode != null) {
      LOG.debug("Deriving county code from County " + countyName + " State " + stateCode);
      if (countyName.endsWith("COUNTY")) {
        // get name without county
        countyName = countyName.substring(0, countyName.lastIndexOf("COUNTY")).trim();
      }
      String sql = ExternalizedQuery.getSql("QUICK_SEARCH.DNB.USCOUNTY");
      EntityManager entityManager = JpaManager.getEntityManager();
      try {
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setForReadOnly(true);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT", "100"));
        query.setParameter("STATE", stateCode);
        query.setParameter("COUNTY", "%" + countyName + "%");
        List<Object[]> results = query.getResults();
        if (results != null) {
          for (Object[] result : results) {
            if (result[1].equals(countyName)) {
              cmrRecord.setCmrCountyCode((String) result[0]);
              cmrRecord.setCmrCounty((String) result[1]);
              break;
            }
          }
        }
        if (cmrRecord.getCmrCountyCode() == null) {
          sql = ExternalizedQuery.getSql("QUICK_SEARCH.DNB.USCOUNTY.ALL");
          query = new PreparedQuery(entityManager, sql);
          query.setForReadOnly(true);
          query.setParameter("MANDT", SystemConfiguration.getValue("MANDT", "100"));
          query.setParameter("STATE", stateCode);
          results = query.getResults();
          if (results != null) {
            // two iterations, equals first then like
            for (Object[] result : results) {
              String checkCounty = (String) result[1];
              if (StringUtils.getLevenshteinDistance(countyName, checkCounty) <= 4) {
                cmrRecord.setCmrCountyCode((String) result[0]);
                cmrRecord.setCmrCounty((String) result[1]);
                break;
              }
            }
          }
        }
        processUSStandardCityName(cmrRecord);
        LOG.debug("City/County Computed: City: " + cmrRecord.getCmrCity() + " " + cmrRecord.getCmrCountyCode() + " - " + cmrRecord.getCmrCounty());
      } catch (Exception e) {
        LOG.warn("County and City computation error.", e);
      } finally {
        entityManager.close();
      }
    }
  }

  /**
   * Determines the City Name to use
   * 
   * @param cmrRecord
   * @return
   * @throws Exception
   */
  public static void processUSStandardCityName(FindCMRRecordModel record) throws Exception {
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    StandardCityServiceClient stdCityClient = CmrServicesFactory.getInstance().createClient(baseUrl, StandardCityServiceClient.class);
    StandardCityRequest stdCityRequest = new StandardCityRequest();
    stdCityRequest.setCountry(record.getCmrCountryLanded());
    stdCityRequest.setCity(record.getCmrCity());
    stdCityRequest.setState(record.getCmrState());
    stdCityRequest.setSysLoc(SystemLocation.UNITED_STATES);
    stdCityRequest.setCountyName(record.getCmrCounty());
    stdCityRequest.setPostalCode(record.getCmrPostalCode());
    stdCityRequest.setStreet1(record.getCmrStreetAddress());
    stdCityRequest.setStreet2(record.getCmrStreetAddressCont());
    stdCityClient.setStandardCityRequest(stdCityRequest);

    StandardCityResponse resp = stdCityClient.executeAndWrap(StandardCityResponse.class);
    if (resp != null && resp.isSuccess()) {
      if (resp.isCityMatched()) {
        record.setCmrCity(resp.getStandardCity());
      }
      if (StringUtils.isBlank(record.getCmrCounty())) {
        record.setCmrCountyCode(resp.getStandardCountyCd());
        record.setCmrCounty(resp.getStandardCountyName());
      }
    }
  }

  /**
   * Gets the default coverage per country
   * 
   * @param entityManager
   * @param country
   * @return
   */
  public static String getDefaultCoverage(EntityManager entityManager, String country) {
    String sql = ExternalizedQuery.getSql("AUTO.GET_DEFAULT_COVERAGE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", country);
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

  /**
   * Returns true if the partner is accredited for the Pay-Go process
   * 
   * @param entityManager
   * @param sourceSystId
   * @return
   */
  public static boolean isPayGoAccredited(EntityManager entityManager, String sourceSystId) {
    if (StringUtils.isBlank(sourceSystId)) {
      return false;
    }
    String sql = ExternalizedQuery.getSql("PAYGO.CHECK");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("SYST_ID", sourceSystId.length() > 12 ? sourceSystId.substring(0, 12) : sourceSystId);
    query.setParameter("SERVICE_ID", sourceSystId.length() > 20 ? sourceSystId.substring(0, 20) : sourceSystId);
    query.setForReadOnly(true);
    return query.exists();
  }

  /**
   * Initializes the LOVs for Rejection Reason
   * 
   * @param entityManager
   */
  public static String getRejectionReason(EntityManager entityManager, String rejectionReasonCd) {
    if (rejectionReasons == null || rejectionReasons.isEmpty()) {
      rejectionReasons = new HashMap<String, String>();
      LOG.debug("Initializing Rejection Reasons");
      String sql = ExternalizedQuery.getSql("AUTO.REJECTION_CODES");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setForReadOnly(true);
      List<Lov> codes = query.getResults(Lov.class);
      if (codes != null) {
        for (Lov code : codes) {
          rejectionReasons.put(code.getId().getCd(), code.getTxt());
        }
      }
    }

    return rejectionReasons.get(rejectionReasonCd);
  }

  public static String getProcessingType(EntityManager entityManager, String country) {
    String sql = ExternalizedQuery.getSql("AUTO.GET_PROCESSING_TYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", country);
    query.setForReadOnly(true);
    String result = query.getSingleResult(String.class);
    if (StringUtils.isEmpty(result)) {
      return result;
    }
    return null;
  }

  /**
   * Tries to extract a request ID value from the given generic model
   * 
   * @param model
   * @return
   * @throws SecurityException
   * @throws NoSuchFieldException
   */
  public static String extractRequestId(Object model) {
    if (model == null) {
      return "-Unknown-";
    }
    try {
      Field reqId = model.getClass().getDeclaredField("reqId");
      if (reqId == null) {
        reqId = model.getClass().getDeclaredField("requestId");
      }
      if (reqId != null) {
        reqId.setAccessible(true);
        Object value = reqId.get(model);
        if (value != null) {
          String sValue = value.toString();
          if ("0".equals(sValue)) {
            return "-Unknown-";
          }
          return sValue;
        }
      }
    } catch (Exception e) {
      // do nothing
    }
    return "-Unknown-";
  }

  /**
   * Ensures the prospect to legal flag is set properly
   * 
   * @param admin
   * @param data
   */
  public static void setProspLegalConversionFlag(EntityManager entityManager, Admin admin, Data data) {
    if (!"C".equals(admin.getReqType())) {
      admin.setProspLegalInd(null);
      // no flag for non creates
      return;
    }
    if ("COM".equals(admin.getReqType())) {
      // ignore completed records
      return;
    }
    String cmrNo = data.getCmrNo();
    DataPK pk = new DataPK();
    pk.setReqId(data.getId().getReqId());
    DataRdc rdc = entityManager.find(DataRdc.class, pk);
    if (rdc != null && StringUtils.isBlank(cmrNo) && !StringUtils.isBlank(rdc.getCmrNo()) && rdc.getCmrNo().startsWith("P")) {
      // for other implementations, get prospect cmr no from data_rdc
      cmrNo = rdc.getCmrNo();
    }
    if (StringUtils.isBlank(cmrNo)) {
      // blank out, no prospect imported
      admin.setProspLegalInd(null);
    } else {
      if (cmrNo.startsWith("P")) {
        // prospect imported
        admin.setProspLegalInd("Y");
      } else {
        admin.setProspLegalInd(null);
      }
    }

  }

}
