package com.ibm.cio.cmr.request.util.legacy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.model.BatchEmailModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;

/**
 * Class that contains utilities for Legacy
 *
 * @author JeffZAMORA
 *
 */
public class LegacyCommonUtil {

  private static final Logger LOG = Logger.getLogger(LegacyCommonUtil.class);

  private static final String DEFAULT_CLEAR_CHAR = "@";
  private static final String DEFAULT_CLEAR_4_CHAR = "@@@@";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";

  public static void setlegacyCustDataMassUpdtFields(EntityManager entityManager, CmrtCust cust, MassUpdtData muData) {

    if (!StringUtils.isBlank(muData.getAbbrevNm())) {
      cust.setAbbrevNm(muData.getAbbrevNm());
    }

    if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
      cust.setAbbrevLocn(muData.getAbbrevLocn());
    }

    if (!StringUtils.isBlank(muData.getIsicCd())) {
      cust.setIsicCd(muData.getIsicCd());
    }

    if (!StringUtils.isBlank(muData.getCollectionCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getCollectionCd().trim())) {
        cust.setCollectionCd("");
      } else {
        cust.setCollectionCd(muData.getCollectionCd());
      }
    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getEnterprise().trim())) {
        cust.setEnterpriseNo("");
      } else {
        cust.setEnterpriseNo(muData.getEnterprise());
      }
    }

    if (!StringUtils.isBlank(muData.getInacCd())) {
      if (DEFAULT_CLEAR_4_CHAR.equals(muData.getInacCd().trim())) {
        cust.setInacCd("");
      } else {
        cust.setInacCd(muData.getInacCd());
      }
    }

    if (!StringUtils.isBlank(muData.getMiscBillCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getMiscBillCd().trim())) {
        cust.setEmbargoCd("");
      } else {
        cust.setEmbargoCd(muData.getMiscBillCd());
      }
    }

    String isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
        + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
    if (isuClientTier != null && isuClientTier.length() == 3) {
      cust.setIsuCd(isuClientTier);
    }

    if (!StringUtils.isBlank(muData.getModeOfPayment())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getModeOfPayment().trim())) {
        cust.setModeOfPayment("");
      } else {
        cust.setModeOfPayment(muData.getModeOfPayment());
      }
    }

    if (!StringUtils.isBlank(muData.getSpecialTaxCd())) {
      cust.setTaxCd(muData.getSpecialTaxCd());
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      cust.setVat(muData.getVat());
    }

    if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
      cust.setImsCd(muData.getSubIndustryCd());
    }

    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    // cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());

  }

  public static void transformBasicLegacyAddressMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr addr, String cntry,
      CmrtCust cust, Data data) {

    if (!StringUtils.isBlank(addr.getCustNm1())) {
      legacyAddr.setAddrLine1(addr.getCustNm1());
    }

    if (!StringUtils.isBlank(addr.getCustNm2())) {
      legacyAddr.setAddrLine2(addr.getCustNm2());
    }

    if (!StringUtils.isBlank(addr.getAddrTxt())) {
      legacyAddr.setStreet(addr.getAddrTxt());
    }

    if (!StringUtils.isBlank(addr.getAddrTxt2())) {
      legacyAddr.setStreetNo(addr.getAddrTxt2());
    }

    if (!StringUtils.isBlank(addr.getCity1())) {
      legacyAddr.setCity(addr.getCity1());
    }

    String poBox = addr.getPoBox();
    if (!StringUtils.isEmpty(poBox)) {
      legacyAddr.setPoBox(addr.getPoBox());
    }
  }

  public static void processDB2TemporaryReactChanges(Admin admin, CmrtCust legacyCust, Data data, EntityManager entityManager) {
    String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin);
    String dataEmbargoCd = data.getEmbargoCd();
    if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason())
        && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && admin.getReqStatus() != null
        && admin.getReqStatus().equals(CMR_REQUEST_STATUS_CPR) && (rdcEmbargoCd != null && !StringUtils.isBlank(rdcEmbargoCd))
        && "Y".equals(rdcEmbargoCd) && (dataEmbargoCd == null || StringUtils.isBlank(dataEmbargoCd))) {
      legacyCust.setEmbargoCd("");
      blankOrdBlockFromData(entityManager, data);
    }
    if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason())
        && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && admin.getReqStatus() != null
        && admin.getReqStatus().equals(CMR_REQUEST_STATUS_PCR) && (rdcEmbargoCd != null && !StringUtils.isBlank(rdcEmbargoCd))
        && "Y".equals(rdcEmbargoCd) && (dataEmbargoCd == null || StringUtils.isBlank(dataEmbargoCd))) {
      legacyCust.setEmbargoCd(rdcEmbargoCd);
      resetOrdBlockToData(entityManager, data);
    }

  }

  private static void blankOrdBlockFromData(EntityManager entityManager, Data data) {
    data.setOrdBlk("");
    entityManager.merge(data);
    entityManager.flush();
  }

  private static void resetOrdBlockToData(EntityManager entityManager, Data data) {
    data.setOrdBlk("88");
    entityManager.merge(data);
    entityManager.flush();
  }

  /**
   * Get the salesGroupRep mapped to repTeamMemberNo
   * 
   * @param entityManager
   * @param cmrIssuingCntry
   * @param repTeamMembeNo
   * @return String
   */
  public static String getSalesGroupRepMap(EntityManager entityManager, String cmrIssuingCntry, String repTeamMemberNo) {
    String sql = ExternalizedQuery.getSql("QUERY.DATA.GET.SALESBO_CD");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("ISSUING_CNTRY", cmrIssuingCntry);
    q.setParameter("REP_TEAM_CD", repTeamMemberNo);
    String salesGroupRep = q.getSingleResult(String.class);
    return salesGroupRep;
  }

  public static String removeATT(String addrLine) {
    addrLine = StringUtils.replace(addrLine, "ATTN:", "");
    addrLine = StringUtils.replace(addrLine, "ATTN.:", "");
    addrLine = StringUtils.replace(addrLine, "ATTN :", "");
    addrLine = StringUtils.replace(addrLine, "ATTN.", "");
    addrLine = StringUtils.replace(addrLine, "ATTN;", "");
    addrLine = StringUtils.replace(addrLine, "ATTN", "");
    addrLine = StringUtils.replace(addrLine, "ATT.:", "");
    addrLine = StringUtils.replace(addrLine, "ATT :", "");
    addrLine = StringUtils.replace(addrLine, "ATT.", "");
    addrLine = StringUtils.replace(addrLine, "ATT:", "");
    addrLine = StringUtils.replace(addrLine, "ATT ", "");
    addrLine = StringUtils.replace(addrLine, "ATT;", "");
    addrLine = StringUtils.replace(addrLine, "ATT.", "");
    addrLine = StringUtils.replace(addrLine, "ATT", "");

    return addrLine.trim();
  }

  public static boolean isCdFoundInLov(EntityManager entityManager, String cntry, String fieldId, String cd) {
    String sql = ExternalizedQuery.getSql("COUNT.LOVBYCD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    query.setParameter("FIELD_ID", fieldId);
    query.setParameter("CD", cd);
    int count = query.getSingleResult(Integer.class);

    if (count > 0) {
      return true;
    }
    return false;
  }

  public static String doFormatPoBox(String poBox) {
    boolean poBoxFlag = poBox.contains("PO BOX");
    if (poBoxFlag) {
      return poBox.substring(6).trim();
    } else {
      return poBox;
    }
  }

  public static AddrRdc getAddrRdcRecord(EntityManager entityManager, Addr addr) {
    LOG.debug("Searching for Addr_RDC records for Legacy Processing " + addr.getId().getReqId());
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("SEQ", addr.getId().getAddrSeq());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setForReadOnly(true);
    return query.getSingleResult(AddrRdc.class);

  }

  public static void sendfieldUpdateEmailNotification(EntityManager entityManager, BatchEmailModel params, String emailTemplatename) {
    String emailTemplate = null;
    String from = SystemConfiguration.getValue("MAIL_FROM");
    String host = SystemConfiguration.getValue("MAIL_HOST");
    if (params != null) {
      String subject = params.getMailSubject();
      String receipent = params.getReceipent();
      if (StringUtils.isBlank(receipent)) {
        LOG.debug("Email subject is empty ,hence no email config is supported");
        return;
      } else if (StringUtils.isBlank(subject)) {
        LOG.debug("Email receipent is empty ,hence no email config is supported");
        return;
      }
      List<Object> mailparams = new ArrayList<>();
      if (emailTemplatename != null) {
        emailTemplate = getEmailTemplate(emailTemplatename);
        if (emailTemplate != null && StringUtils.isNotBlank(emailTemplate)) {
          String email = new String(emailTemplate);

          // adding params
          mailparams.add(params.getRequestId());
          mailparams.add(params.getRequesterName());
          mailparams.add(params.getRequesterId());
          mailparams.add(params.getIssuingCountry());
          mailparams.add(params.getSubregion());
          mailparams.add(params.getCustNm());
          mailparams.add(params.getCmrNumber());
          if (params.isEnableAddlField1()) {
            mailparams.add(params.getAddtlField1Value());
          }
          if (params.isEnableAddlField2()) {
            mailparams.add(params.getAddtlField2Value());
          }
          mailparams.add(params.getDirectUrlLink());

          email = StringUtils.replace(email, params.getStringToReplace(), params.getValToBeReplaceBy());
          email = MessageFormat.format(email, mailparams.toArray(new Object[0]));
          Email mail = new Email();
          mail.setSubject(subject);
          mail.setTo(receipent);
          mail.setFrom(from);
          mail.setMessage(email);
          mail.setType(MessageType.HTML);
          mail.send(host);
        }
      } else {
        LOG.debug("Email cannot be generated as no params are found");
        return;
      }

    }

  }

  private static String getEmailTemplate(String mailTemplate) {
    StringBuilder sb = new StringBuilder();
    try {
      InputStream is = null;
      if (mailTemplate != null) {
        is = ConfigUtil.getResourceStream(mailTemplate);
      } else {
        return null;
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

  public static DataRdc getOldData(EntityManager entityManager, String reqId) {
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    List<DataRdc> records = query.getResults(DataRdc.class);
    DataRdc oldData = new DataRdc();

    if (records != null && records.size() > 0) {
      oldData = records.get(0);
    } else {
      oldData = null;
    }

    return oldData;
  }

  public static boolean isCheckDummyUpdate(MassUpdtAddr massUpdtAddr) {
    boolean isDummy = true;
    if (!StringUtils.isBlank(massUpdtAddr.getCustNm1()) || !StringUtils.isBlank(massUpdtAddr.getCustNm2())
        || !StringUtils.isBlank(massUpdtAddr.getCounty()) || !StringUtils.isBlank(massUpdtAddr.getAddrTxt())
        || !StringUtils.isBlank(massUpdtAddr.getAddrTxt2()) || !StringUtils.isBlank(massUpdtAddr.getPoBox())
        || !StringUtils.isBlank(massUpdtAddr.getCity1()) || !StringUtils.isBlank(massUpdtAddr.getPostCd())
        || !StringUtils.isBlank(massUpdtAddr.getLandCntry())) {
      isDummy = false;
    }
    return isDummy;
  }

  public static Admin getAdminByReqId(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ADMIN.SEARCH_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Admin> records = query.getResults(1, Admin.class);
    if (records != null && records.size() > 0) {
      return records.get(0);
    }
    return null;
  }

}
