/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
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

  private static final String[] ADDRESS_ORDER = { "ZP01", "ZS01", "ZI01", "ZD01", "ZS02" };

  private static final Logger LOG = Logger.getLogger(PortugalTransformer.class);
  private static final String DEFAULT_CLEAR_NUM = "0";
  private static final String DEFAULT_CLEAR_CHAR = "@";
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
    if (!StringUtils.isEmpty(cmrData.getVat()) && vat.matches("^[A-Z]{2}.*")) {
      vatDigit = vat.substring(2, vat.length());
    }

    line1 = addrData.getCustNm1();
    line2 = addrData.getCustNm2();

    if (StringUtils.isEmpty(line2)) {
      line2 = !StringUtils.isBlank(addrData.getCustNm4()) ? addrData.getCustNm4().trim() : "";
      if (!StringUtils.isEmpty(addrData.getCustNm4()) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
        line2 = "ATT " + line2;
      }
    }

    if (StringUtils.isEmpty(line2)) {
      line2 = addrData.getAddrTxt2();
    }

    if (!StringUtils.isEmpty(line2) && (!line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT: "))
        && !StringUtils.isEmpty(addrData.getCustNm4())) {
      line3 = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4().trim() : "";
      if (!StringUtils.isEmpty(line3) && !line3.toUpperCase().startsWith("ATT ") && !line3.toUpperCase().startsWith("ATT:")) {
        line3 = "ATT " + line3;
      }
    }

    if ((StringUtils.isEmpty(line3) && !StringUtils.isEmpty(addrData.getCustNm2()))
        || (!StringUtils.isEmpty(line2) && (line2.toUpperCase().startsWith("ATT ") || line2.toUpperCase().startsWith("ATT:")))) {
      line3 = addrData.getAddrTxt2();
    }

    if (!StringUtils.isEmpty(line3) && line3.length() > 30) {
      line3 = line3.substring(0, 30);
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
    } /*
       * else if
       * (MQMsgConstants.ADDR_ZD01.equals(addrData.getId().getAddrType()) &&
       * update) { line6 = addrData.getCustPhone(); }
       */

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    for (String line : lines) {
      if (StringUtils.isNotBlank(line)) {
        messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Address" + lineNo, line);
        lineNo++;
      }
    }

    /*
     * if (MQMsgConstants.ADDR_ZS01.equals(addrData.getId().getAddrType()) &&
     * crossBorder) {
     * messageHash.put(getAddressKey(addrData.getId().getAddrType()) +
     * "AddressT", vat); } else if
     * (MQMsgConstants.ADDR_ZS01.equals(addrData.getId().getAddrType())) {
     * messageHash.put(getAddressKey(addrData.getId().getAddrType()) +
     * "AddressT", vatDigit); } else {
     * messageHash.put(getAddressKey(addrData.getId().getAddrType()) +
     * "AddressT", ""); }
     */

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

    String custType = cmrData.getCustSubGrp();
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBP".equals(custType)) {
      messageHash.put("MarketingResponseCode", "5");
      messageHash.put("CEdivision", "2");
      messageHash.put("ARemark", "YES");
    } else if (MQMsgConstants.CUSTSUBGRP_GOVRN.equals(custType) || "CRGOV".equals(custType)) {
      messageHash.put("CustomerType", "G");
    } else if (MQMsgConstants.CUSTSUBGRP_INTER.equals(custType) || MQMsgConstants.CUSTSUBGRP_INTSO.equals(custType) || "CRINT".equals(custType)
        || "CRISO".equals(custType)) {
      messageHash.put("CustomerType", "91");
    } else {
      messageHash.put("ARemark", "");
      messageHash.put("MarketingResponseCode", "3");
    }

    String sbo = messageHash.get("SBO");
    messageHash.put("IBO", sbo);

    messageHash.put("CICode", "3");
    messageHash.put("AccAdBo", "");
    messageHash.put("LangCode", "1");
    messageHash.put("AccAdmDSC", "");
    messageHash.put("CEdivision", "3");
    messageHash.put("CustomerLanguage", "1");
    handler.messageHash.put("SourceCode", "FO5");
    messageHash.put("MarketingResponseCode", "3");

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
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  protected boolean isCrossBorder(Addr addr) {
    return !"PT".equals(addr.getLandCntry());
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
      legacyCust.setDcRepeatAgreement("0"); // CAGXB
      legacyCust.setLeasingInd("0"); // CIEDC
      legacyCust.setCeBo("00A0811"); // ceBo

      // CeDivision
      legacyCust.setCeDivision("3");
      legacyCust.setLangCd(StringUtils.isEmpty(legacyCust.getLangCd()) ? dummyHandler.messageHash.get("CustomerLanguage") : legacyCust.getLangCd());

      // extract the phone from billing as main phone
      for (Addr addr : cmrObjects.getAddresses()) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          if (!StringUtils.isEmpty(addr.getCustPhone())) {
            legacyCust.setTelNoOrVat("TF" + addr.getCustPhone());
            landedCntry = addr.getLandCntry();
            break;
          }
        }
      }

      // mrc
      String custSubType = data.getCustSubGrp();
      if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custSubType) || "XBP".equals(custSubType)) {
        legacyCust.setMrcCd("5");
        legacyCust.setAuthRemarketerInd("1");
      } else {
        legacyCust.setMrcCd("3");
        legacyCust.setAuthRemarketerInd("0");
      }

      if (!StringUtils.isEmpty(data.getIsuCd()) && "5K".equals(data.getIsuCd())) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      } else {
        legacyCust.setIsuCd((!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
            + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : ""));
      }
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      for (Addr addr : cmrObjects.getAddresses()) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          if (!StringUtils.isEmpty(addr.getCustPhone())) {
            legacyCust.setTelNoOrVat("TF" + addr.getCustPhone());
            landedCntry = addr.getLandCntry();
            break;
          } else
            legacyCust.setTelNoOrVat("");
        }
      }

      String dataEmbargoCd = data.getEmbargoCd();
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin);

      // permanent removal-single inactivation
      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()) && !"TREC".equals(admin.getReqReason())) {
        if (!StringUtils.isBlank(rdcEmbargoCd) && "Y".equals(rdcEmbargoCd)) {
          if (StringUtils.isBlank(data.getEmbargoCd())) {
            legacyCust.setEmbargoCd("");
          }
        }
      }

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
      String isuClientTier;
      if (!StringUtils.isEmpty(data.getIsuCd()) && "5K".equals(data.getIsuCd())) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      } else {
        isuClientTier = (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
            + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : "");
        if (isuClientTier != null && isuClientTier.length() == 3) {
          legacyCust.setIsuCd(isuClientTier);
        }
      }
    }

    // common data for C/U
    if (!StringUtils.isBlank(data.getAbbrevNm())) {
      legacyCust.setAbbrevNm(data.getAbbrevNm());
    } else {
      legacyCust.setAbbrevNm("");
    }

    // vat
    if (zs01CrossBorder(dummyHandler) && !StringUtils.isEmpty(dummyHandler.cmrData.getVat())) {
      legacyCust.setVat(dummyHandler.cmrData.getVat());
    } else {
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("VAT"))) {
        legacyCust.setVat(dummyHandler.messageHash.get("VAT"));
      } else {
        legacyCust.setVat("");
      }
    }

    String isuClientTier = (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
        + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : "");
    if (isuClientTier != null && isuClientTier.length() == 3) {
      legacyCust.setIsuCd(isuClientTier);
    } else {
      legacyCust.setIsuCd("");
    }
    // CREATCMR-4293
    if (!StringUtils.isEmpty(data.getIsuCd())) {
      if (StringUtils.isEmpty(data.getClientTier())) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      }
    }

    if (!StringUtils.isBlank(data.getIsicCd())) {
      legacyCust.setIsicCd(data.getIsicCd());
    } else {
      legacyCust.setIsicCd("");
    }

    if (!StringUtils.isBlank(data.getSubIndustryCd())) {
      legacyCust.setImsCd(data.getSubIndustryCd());
    } else {
      legacyCust.setImsCd("");
    }

    // SBO,IBO,REMXA,REMXD
    if (!StringUtils.isBlank(data.getSalesBusOffCd())) {
      legacyCust.setIbo(data.getSalesBusOffCd());
      legacyCust.setSbo(data.getSalesBusOffCd());
    } else {
      legacyCust.setIbo("");
      legacyCust.setSbo("");
    }
    if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
      legacyCust.setSalesRepNo(data.getRepTeamMemberNo());
    } else {
      legacyCust.setSalesRepNo("");
    }

    if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
      legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
    } else {
      legacyCust.setSalesGroupRep("");
    }

    if (!StringUtils.isBlank(data.getCollectionCd())) {
      legacyCust.setCollectionCd(data.getCollectionCd());
    } else {
      legacyCust.setCollectionCd("");
    }

    // Type of Customer : CMRTCUST.CCUAI
    legacyCust.setCustType(!StringUtils.isBlank(data.getCrosSubTyp()) ? data.getCrosSubTyp() : "");

    // LANG_CD
    legacyCust.setLangCd("1");

    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("AbbreviatedLocation"))) {
      legacyCust.setAbbrevLocn(dummyHandler.messageHash.get("AbbreviatedLocation"));
    }

    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("EconomicCode"))) {
      legacyCust.setEconomicCd(dummyHandler.messageHash.get("EconomicCode"));
    }

    legacyCust.setDistrictCd(data.getTerritoryCd() != null ? data.getTerritoryCd() : "");
    // legacyCust.setBankBranchNo(data.getCollectionCd() != null ?
    // data.getCollectionCd() : "");
    legacyCust.setBankBranchNo("");
    if (!StringUtils.isEmpty(data.getIsuCd()) && "5K".equals(data.getIsuCd())) {
      legacyCust.setIsuCd(data.getIsuCd() + "7");
    }
  }

  private void blankOrdBlockFromData(EntityManager entityManager, Data data) {
    data.setOrdBlk("");
    entityManager.merge(data);
    entityManager.flush();
  }

  private void resetOrdBlockToData(EntityManager entityManager, Data data) {
    data.setOrdBlk("88");
    data.setEmbargoCd("Y");
    entityManager.merge(data);
    entityManager.flush();
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    formatAddressLines(dummyHandler);
    if (MQMsgConstants.ADDR_ZS01.equals(currAddr.getId().getAddrType())) {
      if (!(StringUtils.isBlank(currAddr.getCustPhone())) || !(StringUtils.isEmpty(currAddr.getCustPhone()))) {
        legacyAddr.setAddrPhone("");
      }
    }
    if (MQMsgConstants.ADDR_ZD01.equals(currAddr.getId().getAddrType())) {
      if (!(StringUtils.isBlank(currAddr.getCustPhone())) || !(StringUtils.isEmpty(currAddr.getCustPhone()))) {
        legacyAddr.setAddrPhone("TF" + currAddr.getCustPhone());
      }
    }
    if (MQMsgConstants.ADDR_ZS01.equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrLineT("");
    }
    legacyAddr.setLanguage(!StringUtils.isBlank(currAddr.getLandCntry()) ? currAddr.getLandCntry() : "");
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

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust legacyCust, CMRRequestContainer cmrObjects,
      MassUpdtData muData) {

    // default mapping for DATA and CMRTCUST
    LOG.debug("Mapping default Data values..");

    if (!StringUtils.isBlank(muData.getAbbrevNm())) {
      legacyCust.setAbbrevNm(muData.getAbbrevNm());
    }

    if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
      legacyCust.setAbbrevLocn(muData.getAbbrevLocn());
    }

    if (!StringUtils.isBlank(muData.getModeOfPayment())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getModeOfPayment().trim())) {
        legacyCust.setModeOfPayment("");
      } else {
        legacyCust.setModeOfPayment(muData.getModeOfPayment());
      }
    }

    String isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
        + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
    if (isuClientTier != null && isuClientTier.endsWith("@")) {
      legacyCust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : legacyCust.getIsuCd().substring(0, 2)) + "7");
    } else if (isuClientTier != null && isuClientTier.length() == 3) {
      legacyCust.setIsuCd(isuClientTier);
    }

    if (!StringUtils.isBlank(muData.getSpecialTaxCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getSpecialTaxCd().trim())) {
        legacyCust.setTaxCd("");
      } else {
        legacyCust.setTaxCd(muData.getSpecialTaxCd());
      }
    }

    if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
      legacyCust.setSalesRepNo(muData.getRepTeamMemberNo());
      legacyCust.setSalesGroupRep(muData.getRepTeamMemberNo());
    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      if ("@@@@@@".equals(muData.getEnterprise())) {
        legacyCust.setEnterpriseNo("");
      } else {
        legacyCust.setEnterpriseNo(muData.getEnterprise());
      }
    }

    if (!StringUtils.isBlank(muData.getCustNm2())) {
      legacyCust.setCeBo(muData.getCustNm2());
    }

    if (!StringUtils.isBlank(muData.getCollectionCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getCollectionCd())) {
        legacyCust.setDistrictCd("");
        legacyCust.setCollectionCd("");
      } else {
        // legacyCust.setDistrictCd(muData.getCollectionCd());
        String distCode = getDistrictCodeForMassUpdate(muData.getCollectionCd().trim(), entityManager);
        legacyCust.setDistrictCd(distCode != null ? distCode : "");
        legacyCust.setCollectionCd(muData.getCollectionCd());
      }
    }

    if (!StringUtils.isBlank(muData.getIsicCd())) {
      legacyCust.setIsicCd(muData.getIsicCd());
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getVat().trim())) {
        legacyCust.setVat("");
      } else {
        String vat = muData.getVat();
        if (!StringUtils.isEmpty(vat) && vat.matches("^[A-Z]{2}.*") && "PT".equals(vat.substring(0, 2))) {
          legacyCust.setVat(vat.substring(2, vat.length()));
        } else {
          legacyCust.setVat(muData.getVat());
        }
      }
    }

    if (!StringUtils.isBlank(muData.getCustNm1())) {
      legacyCust.setSbo(muData.getCustNm1());
      legacyCust.setIbo(muData.getCustNm1());
    }

    if (!StringUtils.isBlank(muData.getInacCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getInacCd().trim())) {
        legacyCust.setInacCd("");
      } else {
        legacyCust.setInacCd(muData.getInacCd());
      }
    }

    if (!StringUtils.isBlank(muData.getMiscBillCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getMiscBillCd().trim())) {
        legacyCust.setEmbargoCd("");
      } else {
        legacyCust.setEmbargoCd(muData.getMiscBillCd());
      }
    }

    if (!StringUtils.isBlank(muData.getOutCityLimit())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getOutCityLimit().trim())) {
        legacyCust.setMailingCond("");
      } else {
        legacyCust.setMailingCond(muData.getOutCityLimit());
      }
    }

    if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
      String subInd = muData.getSubIndustryCd();
      legacyCust.setImsCd(subInd);
    }
    legacyCust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    // legacyCust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());
    if (!StringUtils.isEmpty(muData.getIsuCd()) && "5K".equals(muData.getIsuCd())) {
      legacyCust.setIsuCd(muData.getIsuCd() + "7");
    } else {
      isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
          + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
      if (isuClientTier != null && isuClientTier.length() == 3) {
        legacyCust.setIsuCd(isuClientTier);
      }
    }
  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr addr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {
    legacyAddr.setForUpdate(true);
    LegacyCommonUtil.transformBasicLegacyAddressMassUpdate(entityManager, legacyAddr, addr, cntry, cust, data);

    if (!StringUtils.isBlank(addr.getPostCd())) {
      legacyAddr.setZipCode(addr.getPostCd());
    }

    if ("ZS01".equals(addr.getId().getAddrType())) {
      if (!StringUtils.isBlank(addr.getCustPhone())) {
        if (DEFAULT_CLEAR_NUM.equals(addr.getCustPhone())) {
          cust.setTelNoOrVat("");
        } else {
          cust.setTelNoOrVat("TF" + addr.getCustPhone());
        }
      }
    }

    if ("ZD01".equals(addr.getId().getAddrType())) {
      if (!StringUtils.isBlank(addr.getCustPhone())) {
        if (DEFAULT_CLEAR_NUM.equals(addr.getCustPhone())) {
          legacyAddr.setAddrPhone("");
        } else {
          legacyAddr.setAddrPhone("TF" + addr.getCustPhone());
        }
      }
    }
    legacyAddr.setLanguage(!StringUtils.isBlank(addr.getLandCntry()) ? addr.getLandCntry() : "");
    formatMassUpdateAddressLines(entityManager, legacyAddr, addr, false);
    legacyObjects.addAddress(legacyAddr);

  }

  @Override
  public void formatMassUpdateAddressLines(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr massUpdtAddr, boolean isFAddr) {
    LOG.debug("*** START PT formatMassUpdateAddressLines >>>");
    boolean crossBorder = isCrossBorderForMass(massUpdtAddr, legacyAddr);
    String addrKey = getAddressKey(massUpdtAddr.getId().getAddrType());
    Map<String, String> messageHash = new LinkedHashMap<String, String>();

    messageHash.put("SourceCode", "EF0");
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    String line1 = legacyAddr.getAddrLine1();
    String line2 = legacyAddr.getAddrLine2();
    String line3 = legacyAddr.getAddrLine3();
    String line4 = legacyAddr.getAddrLine4();
    String line5 = legacyAddr.getAddrLine5();
    String line6 = legacyAddr.getAddrLine6();

    line1 = StringUtils.isNotEmpty(massUpdtAddr.getCustNm1()) ? massUpdtAddr.getCustNm1() : line1;

    if (!StringUtils.isBlank(massUpdtAddr.getCustNm2())) {
      line2 = massUpdtAddr.getCustNm2();
    } else if (!StringUtils.isEmpty(massUpdtAddr.getCounty())) {
      line2 = massUpdtAddr.getCounty().trim();
      if (!StringUtils.isEmpty(massUpdtAddr.getCounty()) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
        line2 = "ATT " + line2;
      }
    } else {
      line2 = !StringUtils.isBlank(massUpdtAddr.getAddrTxt2()) ? massUpdtAddr.getAddrTxt2() : line2;
    }

    if (!StringUtils.isEmpty(line2) && (!line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT: "))
        && !StringUtils.isEmpty(massUpdtAddr.getCounty())) {
      line3 = massUpdtAddr.getCounty().trim();
      if (!StringUtils.isEmpty(massUpdtAddr.getCounty()) && !line3.toUpperCase().startsWith("ATT ") && !line3.toUpperCase().startsWith("ATT:")) {
        line3 = "ATT " + line3;
      }
    }

    if ((StringUtils.isEmpty(massUpdtAddr.getCounty()) && !StringUtils.isEmpty(massUpdtAddr.getCustNm2()))
        || (!StringUtils.isEmpty(massUpdtAddr.getCounty()) && (line2.toUpperCase().startsWith("ATT ") || line2.toUpperCase().startsWith("ATT:")))) {
      line3 = !StringUtils.isBlank(massUpdtAddr.getAddrTxt2()) ? massUpdtAddr.getAddrTxt2() : line3;
      if (StringUtils.isEmpty(massUpdtAddr.getAddrTxt2()) && line3.toUpperCase().startsWith("ATT ") && line3.toUpperCase().startsWith("ATT:")) {
        line3 = "";
      }
    }

    if (!StringUtils.isEmpty(line3) && line3.length() > 30) {
      line3 = line3.substring(0, 30);
    }

    if (!StringUtils.isEmpty(massUpdtAddr.getAddrTxt()) || !StringUtils.isEmpty(massUpdtAddr.getPoBox())) {
      line4 = !StringUtils.isEmpty(massUpdtAddr.getAddrTxt()) ? massUpdtAddr.getAddrTxt() : "";
      String poBox = !StringUtils.isEmpty(massUpdtAddr.getPoBox()) ? massUpdtAddr.getPoBox() : "";
      if (!StringUtils.isEmpty(poBox) && !poBox.toUpperCase().startsWith("APTO")) {
        poBox = " APTO " + poBox;
      }
      line4 = StringUtils.isEmpty(poBox) ? line4 : (line4 + poBox);
      if (!StringUtils.isEmpty(poBox)) {
        legacyAddr.setPoBox(poBox);
        legacyAddr.setStreet("");
      } else if (!StringUtils.isEmpty(massUpdtAddr.getAddrTxt())) {
        legacyAddr.setPoBox("");
      }
    }

    if (!StringUtils.isEmpty(massUpdtAddr.getPostCd()) || !StringUtils.isEmpty(massUpdtAddr.getCity1())) {
      line5 = (legacyAddr.getZipCode() != null ? legacyAddr.getZipCode().trim() + " " : "")
          + (legacyAddr.getCity() != null ? legacyAddr.getCity().trim() : "");
    }

    if (crossBorder) {
      if (!StringUtils.isBlank(massUpdtAddr.getLandCntry())) {
        line6 = LandedCountryMap.getCountryName(massUpdtAddr.getLandCntry()).toUpperCase();
      }
    } else {
      if (MQMsgConstants.ADDR_ZS02.equals(massUpdtAddr.getId().getAddrType())) {
        line6 = "Portugal";
      } else {
        line6 = "";
      }
    }

    String[] lines = new String[] { (line1 != null ? line1.trim() : ""), (line2 != null ? line2.trim() : ""), (line3 != null ? line3.trim() : ""),
        (line4 != null ? line4.trim() : ""), (line5 != null ? line5.trim() : ""), (line6 != null ? line6.trim() : "") };
    int lineNo = 1;
    LOG.debug("Lines: " + (line1 != null ? line1.trim() : "") + " | " + (line2 != null ? line2.trim() : "") + " | "
        + (line3 != null ? line3.trim() : "") + " | " + (line4 != null ? line4.trim() : "") + " | " + (line5 != null ? line5.trim() : "") + " | "
        + (line6 != null ? line6.trim() : ""));

    for (String line : lines) {
      if (StringUtils.isNotBlank(line)) {
        messageHash.put((addrKey + "Address" + lineNo).toString(), line.trim());
        lineNo++;
      }
    }

    legacyAddr.setAddrLine1(messageHash.get(addrKey + "Address1") != null ? messageHash.get(addrKey + "Address1") : "");
    legacyAddr.setAddrLine2(messageHash.get(addrKey + "Address2") != null ? messageHash.get(addrKey + "Address2") : "");
    legacyAddr.setAddrLine3(messageHash.get(addrKey + "Address3") != null ? messageHash.get(addrKey + "Address3") : "");
    legacyAddr.setAddrLine4(messageHash.get(addrKey + "Address4") != null ? messageHash.get(addrKey + "Address4") : "");
    legacyAddr.setAddrLine5(messageHash.get(addrKey + "Address5") != null ? messageHash.get(addrKey + "Address5") : "");
    legacyAddr.setAddrLine6(messageHash.get(addrKey + "Address6") != null ? messageHash.get(addrKey + "Address6") : "");

    /*
     * legacyAddr.setAddrLine1(line1 != null ? line1.trim() : "");
     * legacyAddr.setAddrLine2(line2 != null ? line2.trim() : "");
     * legacyAddr.setAddrLine3(line3 != null ? line3.trim() : "");
     * legacyAddr.setAddrLine4(line4 != null ? line4.trim() : "");
     * legacyAddr.setAddrLine5(line5 != null ? line5.trim() : "");
     * legacyAddr.setAddrLine6(line6 != null ? line6.trim() : "");
     */
  }

  @Override
  public boolean enableTempReactOnUpdates() {
    return true;
  }

  private String getDistrictCodeForMassUpdate(String collectionCd, EntityManager entityManager) {
    String distCd = null;
    if (entityManager == null) {
      return null;
    }
    String sql = ExternalizedQuery.getSql("GET_DISTRICT_CODE_PT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CD", collectionCd);
    query.setForReadOnly(true);
    distCd = query.getSingleResult(String.class);
    return distCd;
  }

  @Override
  public boolean isCrossBorderForMass(MassUpdtAddr addr, CmrtAddr legacyAddr) {
    boolean isCrossBorder = false;
    if (!StringUtils.isEmpty(addr.getLandCntry()) && !"PT".equals(addr.getLandCntry())) {
      isCrossBorder = true;
    }
    return isCrossBorder;
  }

  @Override
  public boolean isUpdateNeededOnAllAddressType(EntityManager entityManager, CMRRequestContainer cmrObjects) {
    List<Addr> addresses = cmrObjects.getAddresses();
    for (Addr addr : addresses) {
      if ("ZS01".equals(addr.getId().getAddrType())) {
        AddrRdc addrRdc = LegacyCommonUtil.getAddrRdcRecord(entityManager, addr);
        String currPhone = addr.getCustPhone() != null ? addr.getCustPhone() : "";
        String oldPhone = addrRdc.getCustPhone() != null ? addrRdc.getCustPhone() : "";
        if (addrRdc == null || (addrRdc != null && !currPhone.equals(oldPhone))) {
          return true;
        }
      }
    }
    return false;
  }

}
