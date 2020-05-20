/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.List;
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
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * @author Dhananjay Yadav
 * 
 */
public class PortugalTransformer extends MessageTransformer {

  private static final String[] ADDRESS_ORDER = { "ZP01", "ZS01", "ZI01", "ZD01", "ZS02", "ZP02" };

  private static final Logger LOG = Logger.getLogger(PortugalTransformer.class);

  public static final String DEFAULT_LANDED_COUNTRY = "PT";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";

  public PortugalTransformer(String issuingCntry) {
    super(issuingCntry);
  }

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

      if (StringUtils.isEmpty(cmrData.getIsuCd()) || StringUtils.isEmpty(cmrData.getClientTier())) {
        messageHash.put("ISU", "");
      }
    }
    handleDataDefaults(handler, messageHash, cmrData, crossBorder, addrData);
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());
    boolean crossBorder = isCrossBorder(addrData);

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

  protected void handleDataDefaults(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData) {
    handler.messageHash.put("SourceCode", "FO5");
    messageHash.put("CEdivision", "3");

    String sbo = messageHash.get("SBO");
    messageHash.put("IBO", sbo);
    messageHash.put("MarketingResponseCode", "3");

    String custType = cmrData.getCustSubGrp();
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBP".equals(custType)) {
      messageHash.put("MarketingResponseCode", "5");
      messageHash.put("CEdivision", "2");
      messageHash.put("ARemark", "YES");
    } else if (MQMsgConstants.CUSTSUBGRP_GOVRN.equals(custType)) {
      messageHash.put("CustomerType", "G");
    } else if (MQMsgConstants.CUSTSUBGRP_INTER.equals(custType) || MQMsgConstants.CUSTSUBGRP_INTSO.equals(custType)) {
      messageHash.put("CustomerType", "91");
    } else {
      messageHash.put("ARemark", "");
    }

    messageHash.put("LangCode", "1");
    messageHash.put("CICode", "3");
    messageHash.put("CustomerLanguage", "1");
    messageHash.put("AccAdBo", "");
    messageHash.put("AccAdmDSC", "");

    boolean create = "C".equals(handler.adminData.getReqType());
    if (create) {
      messageHash.put("NationalCust", "N");
      messageHash.put("DPCEBO", "00A0811");
    }
    
  }

  /**
   * Gets the phone from the billing address
   * 
   * @param handler
   * @return
   */
  protected String getBillingPhone(MQMessageHandler handler) {
    List<Addr> addresses = handler.currentAddresses;
    if (addresses != null) {
      for (Addr addr : addresses) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          return addr.getCustPhone();
        }
      }
    }
    return "";
  }
  
  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Mail";
    case "ZI01":
      return "Install";
    case "ZD01":
      return "Ship";
    case "ZS01":
      return "Billing";
    case "ZS02":
      return "Soft";
    case "ZP02":
      return "Fiscal";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Mailing";
    case "ZI01":
      return "Installing";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Billing";
    case "ZS02":
      return "EPL";
    case "ZP02":
      return "Fiscal";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.PORTUGAL;
  }
  
  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "00002";
  }
  
  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZP02:
      return MQMsgConstants.SOF_ADDRESS_USE_FISCAL;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

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
  
  protected boolean zs01CrossBorder(MQMessageHandler handler) {
    EntityManager entityManager = handler.getEntityManager();
    if (entityManager == null) {
      return false;
    }
    List<Addr> addresses = null;
    if (handler.currentAddresses == null) {
      String sql = ExternalizedQuery.getSql("MQREQUEST.GETNEXTADDR");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", handler.addrData.getId().getReqId());
      query.setForReadOnly(true);
      addresses = query.getResults(Addr.class);
    } else {
      addresses = handler.currentAddresses;
    }
    if (addresses != null) {
      for (Addr addr : addresses) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          return isCrossBorder(addr);
        }
      }
    }
    return false;
  }

  /*
   * Legacy Direct Methods
   */
  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    String landedCntry = "";
    
    formatDataLines(dummyHandler);

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      legacyCust.setLangCd(StringUtils.isEmpty(legacyCust.getLangCd()) ? dummyHandler.messageHash.get("CustomerLanguage") : legacyCust.getLangCd());
      
      // extract the phone from billing as main phone
      for (Addr addr : cmrObjects.getAddresses()) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          legacyCust.setTelNoOrVat("TF"+addr.getCustPhone());
          landedCntry = addr.getLandCntry();
          break;
        }
      }

      // mrc
      String custSubType = data.getCustSubGrp();
      if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custSubType) || "XBP".equals(custSubType)) {
        legacyCust.setMrcCd("5");
        legacyCust.setAuthRemarketerInd("Y");
      } else {
        legacyCust.setMrcCd("3");
      }
      
      if (MQMsgConstants.CUSTSUBGRP_GOVRN.equals(custSubType)) {
        legacyCust.setCustType("G");
      } else if (MQMsgConstants.CUSTSUBGRP_INTSO.equals(custSubType) || MQMsgConstants.CUSTSUBGRP_INTSO.equals(custSubType)) {
        legacyCust.setCustType("91");
      }

    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      for (Addr addr : cmrObjects.getAddresses()) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          if (!StringUtils.isEmpty(addr.getCustPhone())) {
            legacyCust.setTelNoOrVat("TF"+addr.getCustPhone());
          }
          landedCntry = addr.getLandCntry();
          break;
        }
      }

      String dataEmbargoCd = data.getEmbargoCd();
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin);

      // permanent removal-single inactivation
      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()) && !"TREC".equals(admin.getReqReason())) {
        if (!StringUtils.isBlank(rdcEmbargoCd) && "E".equals(rdcEmbargoCd)) {
          if (StringUtils.isBlank(data.getEmbargoCd())) {
            legacyCust.setEmbargoCd("");
          }
        }
      }

      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason())
          && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && admin.getReqStatus() != null
          && admin.getReqStatus().equals(CMR_REQUEST_STATUS_CPR) && (rdcEmbargoCd != null && !StringUtils.isBlank(rdcEmbargoCd))
          && "E".equals(rdcEmbargoCd) && (dataEmbargoCd == null || StringUtils.isBlank(dataEmbargoCd))) {
        legacyCust.setEmbargoCd("");
        blankOrdBlockFromData(entityManager, data);
      }

      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason())
          && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && admin.getReqStatus() != null
          && admin.getReqStatus().equals(CMR_REQUEST_STATUS_PCR) && (rdcEmbargoCd != null && !StringUtils.isBlank(rdcEmbargoCd))
          && "E".equals(rdcEmbargoCd) && (dataEmbargoCd == null || StringUtils.isBlank(dataEmbargoCd))) {
        legacyCust.setEmbargoCd(rdcEmbargoCd);
        resetOrdBlockToData(entityManager, data);
      }
    }

    // common data for C/U
    
    legacyCust.setCeDivision("3");
    
    // LANG_CD
    legacyCust.setLangCd("1");
    
    // Type of Customer : CMRTCUST.CCUAI
    legacyCust.setCustType(!StringUtils.isBlank(data.getCrosSubTyp()) ? data.getCrosSubTyp() : "");
    
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
    
    legacyCust.setDistrictCd(data.getTerritoryCd() != null ? data.getTerritoryCd() : "");
    legacyCust.setBankBranchNo(data.getCollectionCd() != null ? data.getCollectionCd() : "");
  }

  private void blankOrdBlockFromData(EntityManager entityManager, Data data) {
    data.setOrdBlk("");
    entityManager.merge(data);
    entityManager.flush();
  }

  private void resetOrdBlockToData(EntityManager entityManager, Data data) {
    data.setOrdBlk("88");
    entityManager.merge(data);
    entityManager.flush();
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    if ("N".equals(currAddr.getImportInd()) && MQMsgConstants.ADDR_ZD01.equals(currAddr.getId().getAddrType())) {
      legacyAddr.getId().setAddrNo(StringUtils.isEmpty(currAddr.getPrefSeqNo()) ? legacyAddr.getId().getAddrNo() : currAddr.getPrefSeqNo());
    }
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For Portugal...");
    
    if (custSubGrp != null && ("INTER".equals(custSubGrp) || "CRINT".equals(custSubGrp))) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(998999);
    } else if (custSubGrp != null && ("INTSO".equals(custSubGrp) || "CRISO".equals(custSubGrp))) {
      generateCMRNoObj.setMin(997000);
      generateCMRNoObj.setMax(999999);
    }
  }

}
