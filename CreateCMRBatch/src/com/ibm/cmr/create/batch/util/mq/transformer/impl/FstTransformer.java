/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * @author Jeffrey Zamora
 * 
 */
public class FstTransformer extends MCOTransformer {

  private static final Logger LOG = Logger.getLogger(FstTransformer.class);

  protected static final String[] ADDRESS_ORDER = { "ZI01", "ZP01", "ZS01", "ZD01", "ZS02" };
  protected static final String[] ADDRESS_ORDER_LD = { "ZS02", "ZP01", "ZS01", "ZD01", "ZI01" };

  private boolean isLDEnabled = true;

  public FstTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData FST Africa transformer...");
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();

    String custType = data.getCustSubGrp();
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      if (MQMsgConstants.CUSTSUBGRP_GOVRN.equals(custType) || "XGOV".equals(custType)) {
        legacyCust.setCustType("G");
      } else if (MQMsgConstants.CUSTSUBGRP_IBMEM.equals(custType)) {
        legacyCust.setCustType("98");
      }
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {

    }

    legacyCust.setAccAdminBo("");
    legacyCust.setBankAcctNo("");
    legacyCust.setBankBranchNo("");
    legacyCust.setBankNo("");
    legacyCust.setCeDivision("3");
    legacyCust.setCurrencyCd("");
    legacyCust.setCeBo("");
    legacyCust.setLangCd("1");
    legacyCust.setLocNo("");
    legacyCust.setMailingCond("");
    legacyCust.setAuthRemarketerInd("0");

    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBP".equals(custType)) {
      legacyCust.setMrcCd("5");
      legacyCust.setAuthRemarketerInd("1");
    } else {
      legacyCust.setMrcCd("3");
    }
    if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
      legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
    } else {
      legacyCust.setSalesGroupRep("");
    }

    legacyCust.setSalesRepNo("DUMMY1");
    legacyCust.setSalesGroupRep("DUMMY1");

    String formatSBO = data.getSalesBusOffCd() + "000";
    legacyCust.setIbo(formatSBO);
    legacyCust.setSbo(formatSBO);

    for (Addr addr : cmrObjects.getAddresses()) {
      if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
        legacyCust.setTelNoOrVat(addr.getCustPhone());
      }
    }

  }

  @Override
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {
    for (Addr addr : cmrObjects.getAddresses()) {
      if ("700".equals(cmrIssuingCntry)) {
        legacyCustExt.setiTaxCode(addr.getTaxOffice());
      }
    }
  }

  @Override
  public boolean hasCmrtCustExt() {
    return true;
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    LOG.debug("transformLegacyAddressData FST Africa transformer...");
    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    }
    formatAddressLinesLD(dummyHandler, legacyAddr);
  }

  private void formatAddressLinesLD(MQMessageHandler handler, CmrtAddr legacyAddr) {
    Addr addrData = handler.addrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    LOG.debug("Legacy Direct formatAddressLinesLD - Handling Address for " + (update ? "update" : "create") + " request.");
    boolean crossBorder = isCrossBorder(addrData);

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";
    String addrType = addrData.getId().getAddrType();

    line1 = addrData.getCustNm1();
    line2 = addrData.getCustNm2();

    if (!StringUtils.isBlank(addrData.getCustNm4())) {
      line3 = addrData.getCustNm4();
    } else if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line3 = addrData.getAddrTxt();
    }

    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 = addrData.getAddrTxt();
    } else if (!StringUtils.isBlank(addrData.getAddrTxt2()) && !StringUtils.isBlank(addrData.getPoBox())) {
      line4 = addrData.getAddrTxt2() + ", " + "PO BOX " + addrData.getPoBox();
    } else if (!StringUtils.isBlank(addrData.getPoBox())) {
      line4 = "PO BOX " + addrData.getPoBox();
    } else if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
      line4 = addrData.getAddrTxt2();
    }

    if (crossBorder) {
      if (!StringUtils.isBlank(addrData.getAddrTxt2()) && !StringUtils.isBlank(addrData.getPoBox())) {
        line5 = addrData.getAddrTxt2() + ", " + "PO BOX " + addrData.getPoBox();
      } else if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
        line5 = addrData.getAddrTxt2();
      } else if (!StringUtils.isBlank(addrData.getPoBox())) {
        line5 = "PO BOX " + addrData.getPoBox();
      } else if (!StringUtils.isBlank(addrData.getCity1()) || !StringUtils.isBlank(addrData.getPostCd())) {
        String cityPostalCode = addrData.getCity1();
        if (StringUtils.isNotBlank(cityPostalCode) && StringUtils.isNotBlank(addrData.getPostCd())) {
          cityPostalCode += ", " + addrData.getPostCd();
        }
        line5 = cityPostalCode;
      }
      line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());

    } else {
      if (!StringUtils.isBlank(addrData.getAddrTxt2()) && !StringUtils.isBlank(addrData.getPoBox())) {
        line5 = addrData.getAddrTxt2() + ", " + "PO BOX " + addrData.getPoBox();
      } else if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
        line5 = addrData.getAddrTxt2();
      } else if (!StringUtils.isBlank(addrData.getPoBox())) {
        line5 = "PO BOX " + addrData.getPoBox();
      }

      String cityPostalCode = addrData.getCity1();
      if (StringUtils.isNotBlank(cityPostalCode) && StringUtils.isNotBlank(addrData.getPostCd())) {
        cityPostalCode += ", " + addrData.getPostCd();
      }
      line6 = cityPostalCode;
    }

    legacyAddr.setAddrLine1(line1);
    legacyAddr.setAddrLine2(line2);
    legacyAddr.setAddrLine3(line3);
    legacyAddr.setAddrLine4(line4);
    legacyAddr.setAddrLine5(line5);
    legacyAddr.setAddrLine6(line6);

  }

  @Override
  public String getAddressUse(Addr addr) {
    if (isLDEnabled) {
      LOG.info("getAddressUse -  LD - FST Africa ");
      return getAddressUseLD(addr);
    } else {
      LOG.info("getAddressUse -  MQ - FST Africa ");
      return getAddressUseMQ(addr);
    }
  }

  private String getAddressUseLD(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  private String getAddressUseMQ(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  @Override
  public String[] getAddressOrder() {
    if (isLDEnabled) {
      LOG.info("getAddressOrder -  LD - FST Africa ");
      return ADDRESS_ORDER_LD;
    } else {
      LOG.info("getAddressOrder -  MQ - FST Africa ");
      return ADDRESS_ORDER;
    }
  }

  @Override
  public String getAddressKey(String addrType) {
    if (isLDEnabled) {
      LOG.info("getAddressKey -  LD - FST Africa ");
      return getAddressKeyLD(addrType);
    } else {
      LOG.info("getAddressKey -  MQ - FST Africa ");
      return getAddressKeyMQ(addrType);
    }
  }

  private String getAddressKeyMQ(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "Mail";
    case "ZD01":
      return "Ship";
    case "ZS01":
      return "Install";
    case "ZS02":
      return "Soft";
    default:
      return "";
    }
  }

  private String getAddressKeyLD(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "EPL";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Installing";
    case "ZS02":
      return "Mailing";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    if (isLDEnabled) {
      LOG.info("getTargetAddressType -  LD -  FST Africa");
      return getTargetAddressTypeLD(addrType);
    } else {
      LOG.info("getTargetAddressType -  MQ -  FST Africa ");
      return getTargetAddressTypeMQ(addrType);
    }
  }

  private String getTargetAddressTypeMQ(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "Mailing";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Installing";
    case "ZS02":
      return "EPL";
    default:
      return "";
    }
  }

  private String getTargetAddressTypeLD(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "EPL";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Installing";
    case "ZS02":
      return "Mailing";
    default:
      return "";
    }
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For FST Africa...");
    if (custSubGrp != null && "INTER".equals(custSubGrp) || custSubGrp != null && "XINTE".equals(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }
}
