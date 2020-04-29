/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

/**
 * @author Jeffrey Zamora
 * 
 */
public class PortugalTransformer extends SpainTransformer {

  private static final Logger LOG = Logger.getLogger(PortugalTransformer.class);

  public PortugalTransformer() {
    super(SystemLocation.PORTUGAL);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {

    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    LOG.debug("Handling Data for " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    boolean crossBorder = isCrossBorder(addrData);

    String vat = cmrData.getVat();
    // trim VAT prefix for Local
    if (!crossBorder) {
      if (!StringUtils.isEmpty(vat) && vat.matches("^[A-Z]{2}.*")) {
        messageHash.put("VAT", vat.substring(2, vat.length()));
      }
    }

    messageHash.put("EnterpriseNo", !StringUtils.isEmpty(cmrData.getEnterprise()) ? cmrData.getEnterprise() : "");
    messageHash.put("ModeOfPayment", !StringUtils.isEmpty(cmrData.getModeOfPayment()) ? cmrData.getModeOfPayment() : "");
    messageHash.put("DistrictCode", !StringUtils.isEmpty(cmrData.getTerritoryCd()) ? cmrData.getTerritoryCd() : "");
    messageHash.put("EmbargoCode", !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "");
    messageHash.put("EconomicCode", "");

    if (update) {
      String phone = getBillingPhone(handler);
      messageHash.put("MailPhone", phone);

      // 1508376 - Spain - update error related to ISU
      if (StringUtils.isEmpty(cmrData.getIsuCd()) || StringUtils.isEmpty(cmrData.getClientTier())) {
        messageHash.put("ISU", "");
      }

    }
    handleDataDefaults(handler, messageHash, cmrData, crossBorder, addrData);
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Addr addrData = handler.addrData;
    boolean update = "U".equals(handler.adminData.getReqType());
    boolean crossBorder = isCrossBorder(addrData);

    Data cmrData = handler.cmrData;
    Map<String, String> messageHash = handler.messageHash;
    String addrKey = getAddressKey(addrData.getId().getAddrType());
    LOG.debug("Handling " + (update ? "update" : "create") + " request.");

    handler.messageHash.put("SourceCode", "FO5");
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";

    String vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() : "";
    String vatDigit = vat;
    if (vat.matches("^[A-Z]{2}.*")) {
      vatDigit = vat.substring(2, vat.length());
    }

    // fiscal address
    if (MQMsgConstants.ADDR_ZP02.equals(addrData.getId().getAddrType())) {
      line1 = vatDigit;
      if (crossBorder)
        line1 = vat;
    } else {
      line1 = addrData.getCustNm1();
      line2 = addrData.getCustNm2();
      if (StringUtils.isEmpty(line2)) {
        line2 = addrData.getAddrTxt2();
      }

      line3 = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4().trim() : "";
      if (!StringUtils.isEmpty(line3) && !line3.toUpperCase().startsWith("ATT ") && !line3.toUpperCase().startsWith("ATT:")) {
        line3 = "ATT: " + line3;
      }
      if (line3.length() > 30) {
        line3 = line3.substring(0, 30);
      }
      if (StringUtils.isEmpty(line3) && !StringUtils.isEmpty(addrData.getCustNm2())) {
        line3 = addrData.getAddrTxt2();
      }

      line4 = addrData.getAddrTxt();
      String poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "";
      if (!StringUtils.isEmpty(poBox) && !poBox.toUpperCase().startsWith("APTO")) {
        poBox = " APTO " + poBox;
      }
      line4 = (StringUtils.isEmpty(line4) ? "" : line4) + poBox;

      line5 = addrData.getPostCd() + " " + addrData.getCity1();
      line6 = "";
      if (crossBorder) {
        line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      } else if (MQMsgConstants.ADDR_ZS02.equals(addrData.getId().getAddrType())) {
        line6 = "Portugal";
      } else if (MQMsgConstants.ADDR_ZD01.equals(addrData.getId().getAddrType()) && update) {
        line6 = addrData.getCustPhone();
      }
    }

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    for (String line : lines) {
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Address" + lineNo, line);
      lineNo++;
    }

    if (MQMsgConstants.ADDR_ZS01.equals(addrData.getId().getAddrType()) && crossBorder) {
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "AddressT", vat);
    } else if (MQMsgConstants.ADDR_ZS01.equals(addrData.getId().getAddrType())) {
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "AddressT", vatDigit);
    } else {
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "AddressT", "");
    }

    if (MQMsgConstants.ADDR_ZP01.equals(addrData.getId().getAddrType())) {
      // send to MailPhone the billing phone number
      String phone = getBillingPhone(handler);
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Phone", phone);
    } else {
      // always clear *Phone otherwise
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Phone", "");
    }

    if ("N".equals(addrData.getImportInd()) && MQMsgConstants.ADDR_ZD01.equals(addrData.getId().getAddrType())) {
      // preferred sequence no for additional shipping
      messageHash.put("SequenceNo", StringUtils.isEmpty(addrData.getPrefSeqNo()) ? "" : addrData.getPrefSeqNo());
    }

  }

  @Override
  protected void handleDataDefaults(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData) {
    handler.messageHash.put("SourceCode", "FO5");
    messageHash.put("CEdivision", "3");

    String sbo = messageHash.get("SBO");
    messageHash.put("IBO", sbo);
    messageHash.put("MarketingResponseCode", "3");

    String custType = cmrData.getCustSubGrp();
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBP".equals(custType)) {
      messageHash.put("MarketingResponseCode", "5");
      messageHash.put("ARemark", "YES");
      messageHash.put("CustomerType", "BP");
    } else if ("GOVRN".equals(custType)) {
      messageHash.put("CustomerType", "G");
    } else if ("INTER".equals(custType) || "INTSO".equals(custType) || "ININV".equals(custType)) {
      messageHash.put("CustomerType", "91");
    } else {
      messageHash.put("ARemark", "");
    }

    messageHash.put("LangCode", "1");
    messageHash.put("CICode", "3");
    messageHash.put("CustomerLanguage", "1");
    messageHash.put("AccAdBo", "");
    messageHash.put("AccAdmDSC", "");

    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBP".equals(custType)) {
      messageHash.put("CEdivision", "2");
    }

    boolean create = "C".equals(handler.adminData.getReqType());
    if (create) {
      messageHash.put("NationalCust", "N");
      messageHash.put("DPCEBO", "00A0811");

    }

  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.PORTUGAL;
  }

  @Override
  protected boolean isCrossBorder(Addr addr) {
    return !"PT".equals(addr.getLandCntry());
  }

  @Override
  public boolean shouldSendAddress(EntityManager entityManager, MQMessageHandler handler, Addr nextAddr) {
    if (nextAddr == null) {
      return false;
    }
    Data cmrData = handler.cmrData;
    if (cmrData != null && StringUtils.isEmpty(cmrData.getVat()) && "ZP02".equals(nextAddr.getId().getAddrType())) {
      // 1562483 - do not send fiscal if VAT is empty
      return false;
    }
    return true;
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    String landedCntry = "";

    formatDataLines(dummyHandler);

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      legacyCust.setLangCd(StringUtils.isEmpty(legacyCust.getLangCd()) ? dummyHandler.messageHash.get("CustomerLanguage") : legacyCust.getLangCd());
      legacyCust.setAccAdminBo("Y60382");
      legacyCust.setCeDivision("2");

      // extract the phone from billing as main phone
      for (Addr addr : cmrObjects.getAddresses()) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          legacyCust.setTelNoOrVat(addr.getCustPhone());
          landedCntry = addr.getLandCntry();
          break;
        }
      }

      // mrc
      String custSubType = data.getCustSubGrp();
      if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custSubType) || "XBP".equals(custSubType)) {
        legacyCust.setMrcCd("5");
        legacyCust.setAuthRemarketerInd("Y");
      }
      
      //Type Of Customer
      if (MQMsgConstants.CUSTSUBGRP_GOVRN.equals(custSubType)) {
        legacyCust.setCustType("G");
      } else if (MQMsgConstants.CUSTSUBGRP_INTSO.equals(custSubType) || "CRISO".equals(custSubType)) {
        legacyCust.setCustType("91");
      }

      // common data for C/U
      // formatted data
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("AbbreviatedLocation"))) {
        legacyCust.setAbbrevLocn(dummyHandler.messageHash.get("AbbreviatedLocation"));
      }

      if (zs01CrossBorder(dummyHandler) && !StringUtils.isEmpty(dummyHandler.cmrData.getVat())) {
        if (dummyHandler.cmrData.getVat().matches("^[A-Z]{2}.*")) {
          legacyCust.setVat(landedCntry + dummyHandler.cmrData.getVat().substring(2));
        } else {
          legacyCust.setVat(landedCntry + dummyHandler.cmrData.getVat());
        }
      } else {
        if (!StringUtils.isEmpty(dummyHandler.messageHash.get("VAT"))) {
          legacyCust.setVat(dummyHandler.messageHash.get("VAT"));
        }
      }
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("EconomicCode"))) {
        legacyCust.setEconomicCd(dummyHandler.messageHash.get("EconomicCode"));
      }
      legacyCust.setDistrictCd(data.getCollectionCd() != null ? data.getCollectionCd() : "");
      legacyCust.setBankBranchNo(data.getIbmDeptCostCenter() != null ? data.getIbmDeptCostCenter() : "");
    }
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For Portugal...");
    if (custSubGrp != null
        && ("INTER".equals(custSubGrp) || "CRINT".equals(custSubGrp))) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(998999);
    }
  }

}
