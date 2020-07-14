package com.ibm.cio.cmr.request.util.legacy;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
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
      if (DEFAULT_CLEAR_CHAR.equals(muData.getSpecialTaxCd().trim())) {
        cust.setTaxCd("");
      } else {
        cust.setTaxCd(muData.getSpecialTaxCd());
      }
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getVat().trim())) {
        cust.setVat("");
      } else {
        cust.setVat(muData.getVat());
      }
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
      if (DEFAULT_CLEAR_CHAR.equals(addr.getCustNm2())) {
        legacyAddr.setAddrLine2("");
      } else {
        legacyAddr.setAddrLine2(addr.getCustNm2());
      }
    }

    if (!StringUtils.isBlank(addr.getAddrTxt())) {
      if (DEFAULT_CLEAR_CHAR.equals(addr.getAddrTxt())) {
        legacyAddr.setStreet("");
      } else {
        legacyAddr.setStreet(addr.getAddrTxt());
      }
    }

    if (!StringUtils.isBlank(addr.getAddrTxt2())) {
      if (DEFAULT_CLEAR_CHAR.equals(addr.getAddrTxt2())) {
        legacyAddr.setStreetNo("");
      } else {
        legacyAddr.setStreetNo(addr.getAddrTxt2());
      }
    }

    if (!StringUtils.isBlank(addr.getCity1())) {
      legacyAddr.setCity(addr.getCity1());
    }

    if (!StringUtils.isBlank(addr.getCustPhone())) {
      if (DEFAULT_CLEAR_CHAR.equals(addr.getCustPhone())) {
        legacyAddr.setAddrPhone("");
      } else {
        legacyAddr.setAddrPhone(addr.getCustPhone());
      }
    }

    String poBox = addr.getPoBox();
    if (!StringUtils.isEmpty(poBox)) {
      if (DEFAULT_CLEAR_CHAR.equals(poBox)) {
        legacyAddr.setPoBox("");
      } else {
        legacyAddr.setPoBox(addr.getPoBox());
      }
    }

  }

  public static void setkna1MassUpdtCommonRules(EntityManager cmmaMgr, Kna1 kna1, MassUpdtData muData, List<Kna1> forUpdate, CmrtCust cust,
      MassUpdtAddr addr) {

  }

}
