/**
 *
 */
package com.ibm.cio.cmr.request.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.DropDownService;
import com.ibm.cio.cmr.request.util.external.CreateCMRBPHandler;
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
  private static final String usFspEmailTemplate = "createcmr-bp_end_user";

  private static final List<String> FIELDS_CLEAR_LIST = new ArrayList<String>();

  static {
    FIELDS_CLEAR_LIST.add("CollectionCd");
    FIELDS_CLEAR_LIST.add("SpecialTaxCd");
    FIELDS_CLEAR_LIST.add("ModeOfPayment");
    FIELDS_CLEAR_LIST.add("CrosSubTyp");
    FIELDS_CLEAR_LIST.add("TipoCliente");
    FIELDS_CLEAR_LIST.add("CommercialFinanced");
    FIELDS_CLEAR_LIST.add("EmbargoCode");
    FIELDS_CLEAR_LIST.add("OrderBlock");
    FIELDS_CLEAR_LIST.add("Enterprise");
    FIELDS_CLEAR_LIST.add("TypeOfCustomer");
    FIELDS_CLEAR_LIST.add("CodFlag");
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
        if (codeOnly) {
          choices.add((String) result[0]);
        }
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
        if (codeOnly) {
          choices.add((String) result[0]);
        }
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
    if (!"PRJ".equals(admin.getReqStatus()) && !"COM".equals(admin.getReqStatus())) {
      // CREATCMR-2625,6677
      return;
    }

    DataPK dataPk = new DataPK();
    dataPk.setReqId(admin.getId().getReqId());
    Data data = entityManager.find(Data.class, dataPk);

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

    if (data != null) {
      cmrno = data.getCmrNo();
    }

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
      // CREATCMR-6639
      if (data != null && US_CMRISSUINGCOUNTRY.equalsIgnoreCase(data.getCmrIssuingCntry())) {
        type = "Update Enterprise Name";
      } else {
        type = "Update by Enterprise";
      }
      // type = "Update by Enterprise";
    } else {
      type = "-";
    }
    // String type = "C".equals(admin.getReqType()) ? "Create" :
    // ("U".equals(admin.getReqType()) ? "Update" : "-");

    if (data != null && "641".equals(data.getCmrIssuingCntry())) {
      if ("COM".equals(admin.getReqStatus())) {
        addNotifyCN(data, recipients, entityManager);
      }
    }

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

    boolean usEndUserScenario = !StringUtils.isBlank(data.getCustSubGrp()) && data.getCustSubGrp().equals(USUtil.SC_FSP_END_USER);
    if ((!StringUtils.isBlank(admin.getSourceSystId()) && "CreateCMR-BP".equalsIgnoreCase(admin.getSourceSystId())) || usEndUserScenario) {
      String extTemplate = getExternalEmailTemplate(usEndUserScenario ? usFspEmailTemplate : admin.getSourceSystId());
      if (!StringUtils.isBlank(extTemplate)) {
        email = extTemplate;
      }

      String cmrIssuingCountry = data.getCmrIssuingCntry();
      String country = getIssuingCountry(entityManager, cmrIssuingCountry);
      country = cmrIssuingCountry + (StringUtils.isBlank(country) ? "" : " - " + country);

      List<Object> params = new ArrayList<>();
      params.add(history.getReqId() + ""); // {0}
      params.add(customerName); // {1}
      params.add(country); // {2}
      params.add(cmrno); // {3}
      params.add(siteId); // {4}
      params.add(type); // {5}
      params.add(status); // {6}
      params.add(history.getCreateByNm() + " (" + history.getCreateById() + ")"); // {7}
      params.add(CmrConstants.DATE_FORMAT().format(history.getCreateTs())); // {8}
      params.add(histContent); // {9}
      params.add(rejectReason); // {10}
      params.add(feedbackLink); // {11}
      email = StringUtils.replace(email, "$COUNTRY$", country);

      if (usEndUserScenario) {
        new CreateCMRBPHandler().addEmailParams(entityManager, params, admin);
      } else {
        ExternalSystemUtil.addExternalMailParams(entityManager, params, admin); // {12},{13}
      }
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

  public static String getOrderBlockFromDataRdc(EntityManager entityManager, Admin admin) {
    LOG.debug("Batch: Order block in DATA_RDC req_id:" + admin.getId().getReqId());
    String oldOrderBlock = "";
    String sql = ExternalizedQuery.getSql("GET.ORDER_BLOCK_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      oldOrderBlock = result[0] != null ? (String) result[0] : "";
    }

    LOG.debug("Order block of Data_RDC>" + oldOrderBlock);
    return oldOrderBlock;
  }

  public static boolean isTimeStampEquals(Date date) {
    @SuppressWarnings("serial")
    Timestamp ts = new Timestamp(Long.MIN_VALUE) {
      @Override
      public String toString() {
        return "0000-00-00 00:00:00.000";
      }
    };
    String sDate1 = ts.toString().substring(0, 3);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    String sDate2 = sdf.format(date).substring(0, 3);

    LOG.info("Check equality of sDate1 :" + sDate1 + " sDate2 :" + sDate2);
    return sDate1.equals(sDate2) ? true : false;
  }

  public static int checkNoOfWorkingDays(Date processedTs, Timestamp currentTimestamp) {
    LOG.debug("processedTs=" + processedTs + " currentTimestamp=" + currentTimestamp);

    int workingDays = 0;
    String curStringDate = currentTimestamp.toString();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    try {

      Calendar start = Calendar.getInstance();
      start.setTime(processedTs);

      Calendar end = Calendar.getInstance();
      end.setTime(sdf.parse(curStringDate));

      while (!start.after(end)) {
        int day = start.get(Calendar.DAY_OF_WEEK);
        if ((day != Calendar.SATURDAY) && (day != Calendar.SUNDAY))
          workingDays++;
        start.add(Calendar.DATE, 1);
      }
      LOG.debug("No of workingDays=" + workingDays);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return workingDays;
  }

  private static void addNotifyCN(Data data, StringBuilder recipients, EntityManager em) {
    StringBuilder cnEcoNotifyList = getCnEcoNotifyList();
    if (data != null && cnEcoNotifyList.length() > 0 && "ECOSY".equals(data.getCustSubGrp())) {
      recipients.append(",");
      recipients.append(cnEcoNotifyList);
    }
  }

  private static StringBuilder getCnEcoNotifyList() {
    StringBuilder cnEcoNotifyList = new StringBuilder();
    cnEcoNotifyList.append(SystemParameters.getString("CN_ECOSY_COM_NOTIFY"));
    return cnEcoNotifyList;
  }

  public static String getCsboByPostal(EntityManager entityMgr, String postCd) {
    String csbo = "";

    String sql = ExternalizedQuery.getSql("JP.MASS.GET.CSBO.BY.POSTAL");
    PreparedQuery query = new PreparedQuery(entityMgr, sql);
    query.setParameter("CMR_ISSUING_CNTRY", SystemLocation.JAPAN);
    query.setParameter("POST_CD", postCd);
    query.setForReadOnly(true);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      csbo = result[1] != null ? (String) result[1] : "";
    }
    LOG.debug("getCsboByPostal() --> postCd (" + postCd + ") csbo (" + csbo + ")");
    return csbo;
  }

  public static String getLocationByPostal(EntityManager entityMgr, String postCd) {
    String locn = "";

    String sql = ExternalizedQuery.getSql("JP.MASS.GET.LOCN.BY.POSTAL");
    PreparedQuery query = new PreparedQuery(entityMgr, sql);
    query.setParameter("CMR_ISSUING_CNTRY", SystemLocation.JAPAN);
    query.setParameter("POST_CD", postCd);
    query.setForReadOnly(true);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      locn = result[1] != null ? (String) result[1] : "";
    }
    LOG.debug("getLocationByPostal() --> postCd (" + postCd + ") csbo (" + locn + ")");
    return locn;
  }

  public static String getIsicByJsic(EntityManager entityMgr, String jsic) {
    String isic = "";

    String sql = ExternalizedQuery.getSql("JP.MASS.GET.ISIC.BY.JSIC");
    PreparedQuery query = new PreparedQuery(entityMgr, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("JSIC_CD", jsic);
    query.setForReadOnly(true);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      isic = result[1] != null ? (String) result[1] : "";
    }
    LOG.debug("getIsicByJsic() --> jsic (" + jsic + ") isic (" + isic + ")");
    return isic;
  }

  public static Object[] getSubindustryISUByIsic(EntityManager entityMgr, String isic) {
    String sql = ExternalizedQuery.getSql("JP.MASS.GET.SUBIND.ISU");

    PreparedQuery query = new PreparedQuery(entityMgr, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("ZZKV_SIC", isic);
    query.setForReadOnly(true);

    List<Object[]> results = query.getResults();
    Object[] result = null;
    if (results != null && results.size() > 0) {
      result = results.get(0);
    }
    LOG.debug("getSubindustryISUByIsic() --> isic (" + isic + ") subind (" + result[1] + ") isu (" + result[2] + ")");
    return result;
  }

  public static Object[] getIsicMrcCtcIsuSortlJP(EntityManager entityMgr, String ofcd) {
    String sql = ExternalizedQuery.getSql("JP.MASS.GET.MAPPED.FIELDS.BY.OFCD");

    PreparedQuery query = new PreparedQuery(entityMgr, sql);
    query.setParameter("OFFICE_CD", ofcd);
    query.setForReadOnly(true);

    List<Object[]> results = query.getResults();
    Object[] result = null;
    if (results != null && results.size() > 0) {
      result = results.get(0);
    }
    LOG.debug("getIsicMrcCtcIsuSortlJP() --> ofcd (" + ofcd + ") isic (" + result[1] + ") mrc (" + result[2] + ") ctc (" + result[3] + ") isu ("
        + result[4] + ") sortl (" + result[5] + ")");
    return result;

  }

  public static void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country,
      Admin admin) {
    GEOHandler handler = getGEOHandler(country);
    handler.validateMassUpdateTemplateDupFills(validations, book, maxRows, country, admin);
  }

}
