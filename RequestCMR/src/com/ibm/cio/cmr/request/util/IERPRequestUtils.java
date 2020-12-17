/**
 *
 */
package com.ibm.cio.cmr.request.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.DropDownService;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;

/**
 * @author Dennis Natad
 *
 */
public class IERPRequestUtils extends RequestUtils {

  private static final Logger LOG = Logger.getLogger(RequestUtils.class);
  private static String emailTemplate = null;
  private static String batchemailTemplate = null;

  private static final List<String> FIELDS_CLEAR_LIST = new ArrayList<String>();

  static {
    // FIELDS_CLEAR_LIST.add("CollectionCd");
    // FIELDS_CLEAR_LIST.add("SpecialTaxCd");
    // FIELDS_CLEAR_LIST.add("ModeOfPayment");
    // FIELDS_CLEAR_LIST.add("CrosSubTyp");
    // FIELDS_CLEAR_LIST.add("TipoCliente");
    // FIELDS_CLEAR_LIST.add("CommercialFinanced");
    FIELDS_CLEAR_LIST.add("EmbargoCode");
    // FIELDS_CLEAR_LIST.add("Enterprise");
    // FIELDS_CLEAR_LIST.add("TypeOfCustomer");
    // FIELDS_CLEAR_LIST.add("CodFlag");

    // LD_BYPASS_MASS_UPDT_DUP_FILLS_VAL.add("758");
  }

  public static boolean isCountryDREnabled(EntityManager entityManager, String cntry) {

    if (entityManager == null) {
      entityManager = JpaManager.getEntityManager();
    }

    boolean isDR = false;
    String sql = ExternalizedQuery.getSql("DR.GET_SUPP_CNTRY_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    query.setForReadOnly(true);
    List<Integer> records = query.getResults(Integer.class);
    Integer singleObject = null;

    if (records != null && records.size() > 0) {
      singleObject = records.get(0);
      Integer val = singleObject != null ? singleObject : null;

      if (val != null) {
        isDR = true;
      } else {
        isDR = false;
      }

    } else {
      isDR = false;
    }

    return isDR;
  }

  public static List<String> getLovsDR(EntityManager entityManager, String lovId, String country, boolean codeOnly) {
    List<String> choices = new ArrayList<String>();
    String sql = ExternalizedQuery.getSql("LOV");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("FIELD_ID", "##" + lovId);
    query.setParameter("CMR_CNTRY", country);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      for (Object[] result : results) {
        /*
         * if (codeOnly) { choices.add((String) result[0]); } else {
         * choices.add(result[0] + " | " + result[1]); }
         */
        choices.add(result[1] + "");
      }
    }
    return choices;
  }

  public static List<String> getBDSChoicesDR(EntityManager entityManager, String bdsId, String country, boolean codeOnly) {
    ParamContainer params = new ParamContainer();
    List<String> choices = new ArrayList<String>();
    DropDownService service = new DropDownService();
    PreparedQuery query = service.getBDSSql(bdsId, entityManager, params, country);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      for (Object[] result : results) {
        /*
         * if (codeOnly) { choices.add((String) result[0]); } else {
         * choices.add(result[0] + " | " + result[1]); }
         */
        choices.add(result[1] + "");
      }
    }
    return choices;
  }

  public static List<String> addSpecialCharToLovDR(List<String> lovList, String cntry, boolean codeOnly, String fieldId) {
    List<String> tempList = new ArrayList<String>();
    // if (SystemLocation.UNITED_KINGDOM.equals(cntry) ||
    // SystemLocation.IRELAND.equals(cntry)) {
    if (FIELDS_CLEAR_LIST.contains(fieldId)) {
      LOG.debug("***Field " + fieldId + " is on clear list. Adding '@'");
      if (codeOnly) {
        tempList.add("@");
      } /*
         * else { tempList.add("@ | Clear field"); }
         */
      for (String choice : lovList) {
        tempList.add(choice);
      }
    } else {
      LOG.debug("***Field " + fieldId + " is NOT on clear list. Returning ORIGINAL list.");
      tempList = lovList;
    }
    // } else {
    // tempList = lovList;
    // }
    return tempList;
  }

  public static void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    GEOHandler handler = getGEOHandler(country);
    handler.validateMassUpdateTemplateDupFills(validations, book, maxRows, country);
  }

  public static void sendEmailNotifications(EntityManager entityManager, Admin admin, WfHist history, String siteIds, String emailCmt) {
    String cmrno = "";
    String siteId = siteIds == null ? "" : siteIds;
    String rejectReason = history.getRejReason();
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.IERP.GETNOTIFLIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", history.getReqId());
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

    // code to retrive the CMR number and Site id for req_ID

    sql = ExternalizedQuery.getSql("REQUESTENTRY.GETCMRANDSITEID");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", history.getReqId());
    List<Data> res = query.getResults(Data.class);
    for (Data result : res) {
      cmrno = result.getCmrNo();
    }

    Data data = res != null && res.size() > 0 ? res.get(0) : new Data();

    if (rejectReason == null) {
      rejectReason = "";
    }
    if (cmrno == null) {
      cmrno = "";
    }

    String from = SystemConfiguration.getValue("MAIL_FROM");
    String subject = SystemConfiguration.getValue("MAIL_SUBJECT");
    if (emailTemplate == null) {
      emailTemplate = getEmailTemplate();
    }

    if (subject.contains("{0}")) {
      // 1048370 - add request id in the mail
      subject = MessageFormat.format(subject, history.getReqId() + "", status);
    }
    // add a completed to subject for completed requests
    // if ("COM".equals(history.getReqStatus())) {
    // subject += " - Completed";
    // }

    String email = new String(emailTemplate);
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
      type = "Update by Enterprise";
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

    String feedbackLink = "COM".equals(history.getReqStatus()) ? Feedback.generateEmeddedFeedbackLink(data) : "";
    String histContent = emailCmt;// history.getCmt();
    histContent = histContent != null ? StringUtils.replace(histContent, "\n", "<br>") : "-";
    if (status != null && status.equals("Rejected")) {
      StringBuffer temp = new StringBuffer(email);
      int tempstart = temp.indexOf("{5}");
      int insertstart = tempstart + 17;
      String rejRes = "<tr><th style=\"text-align:left;width:200px\">Reject Reason:</th><td>{10}</td></tr>";
      temp.insert(insertstart, rejRes);
      email = temp.toString();

      String cmrIssuingCountry = data.getCmrIssuingCntry();
      String country = getIssuingCountry(entityManager, cmrIssuingCountry);
      country = cmrIssuingCountry + (StringUtils.isBlank(country) ? "" : " - " + country);
      email = StringUtils.replace(email, "$COUNTRY$", country);

      email = MessageFormat.format(email, history.getReqId() + "", customerName, siteId, cmrno, type, status,
          history.getCreateByNm() + " (" + history.getCreateById() + ")", CmrConstants.DATE_FORMAT().format(history.getCreateTs()), histContent,
          directUrlLink, rejectReason, feedbackLink);
    } else {

      String cmrIssuingCountry = data.getCmrIssuingCntry();
      String country = getIssuingCountry(entityManager, cmrIssuingCountry);
      country = cmrIssuingCountry + (StringUtils.isBlank(country) ? "" : " - " + country);
      email = StringUtils.replace(email, "$COUNTRY$", country);

      email = MessageFormat.format(email, history.getReqId() + "", customerName, siteId, cmrno, type, status,
          history.getCreateByNm() + " (" + history.getCreateById() + ")", CmrConstants.DATE_FORMAT().format(history.getCreateTs()), histContent,
          directUrlLink, "", feedbackLink);
    }
    String host = SystemConfiguration.getValue("MAIL_HOST");

    if (!StringUtils.isBlank(admin.getSourceSystId()) && "CreateCMR-BP".equalsIgnoreCase(admin.getSourceSystId())) {
      String extTemplate = getExternalEmailTemplate(admin.getSourceSystId());
      if (!StringUtils.isBlank(extTemplate)) {
        email = extTemplate;
      }

      List<Object> params = new ArrayList<>();
      params.add(history.getReqId() + ""); // {0}
      params.add(customerName); // {1}
      params.add(siteId); // {2}
      params.add(cmrno); // {3}
      params.add(type); // {4}
      params.add(status); // {5}
      params.add(history.getCreateByNm() + " (" + history.getCreateById() + ")"); // {6}
      params.add(CmrConstants.DATE_FORMAT().format(history.getCreateTs())); // {7}
      params.add(histContent); // {8}
      params.add(directUrlLink); // {9}
      params.add(rejectReason); // {10}
      params.add(feedbackLink); // {11}

      String cmrIssuingCountry = data.getCmrIssuingCntry();
      String country = getIssuingCountry(entityManager, cmrIssuingCountry);
      country = cmrIssuingCountry + (StringUtils.isBlank(country) ? "" : " - " + country);
      email = StringUtils.replace(email, "$COUNTRY$", country);

      ExternalSystemUtil.addExternalMailParams(entityManager, params, admin); // {12},{13}
      email = MessageFormat.format(email, params.toArray(new Object[0]));
    }

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

  private static void completeLastHistoryRecord(EntityManager entityManager, long reqId) {
    PreparedQuery update = new PreparedQuery(entityManager, ExternalizedQuery.getSql("WORK_FLOW.COMPLETE_LAST"));
    update.setParameter("REQ_ID", reqId);
    update.executeSql();
  }

  public static synchronized WfHist createWorkflowHistoryFromBatch(EntityManager entityManager, String user, Admin admin, String cmt, String action,
      String sendToId, String sendToNm, boolean complete) throws CmrException, SQLException {

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
    hist.setRejReason(null);
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

    // sendEmailNotifications(entityManager, admin, hist);

    return hist;
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

}
