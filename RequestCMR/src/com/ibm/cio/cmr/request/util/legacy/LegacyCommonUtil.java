package com.ibm.cio.cmr.request.util.legacy;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemUtil;

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

    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());

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

}
