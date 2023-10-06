/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
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
 * {@link MessageTransformer} implementation for Greece.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class GreeceTransformer extends EMEATransformer {

  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode" };

  /*
   * Greece - MQ - Code private static final String[] ADDRESS_ORDER = { "ZP01",
   * "ZS01", "ZD01" };
   */

  // Comment this out when reverting to MQ
  private static final String[] ADDRESS_ORDER = { "ZP01", "ZS01", "ZD01", "ZI01" };

  private static final Logger LOG = Logger.getLogger(GreeceTransformer.class);
  private static final String DEFAULT_CLEAR_CHAR = "@";
  private static final String DEFAULT_CLEAR_NUM = "0";
  public static final String DEFAULT_LANDED_COUNTRY = "GR";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";

  private static final String ADDRESS_USE_EXISTS = "Y";
  private static final String ADDRESS_USE_NOT_EXISTS = "N";
  private static final String ADDRESS_USE_MAILING = "1";
  private static final String ADDRESS_USE_BILLING = "2";
  private static final String ADDRESS_USE_INSTALLING = "3";
  private static final String ADDRESS_USE_SHIPPING = "4";
  private static final String ADDRESS_USE_EPL_MAILING = "5";
  private static final String ADDRESS_USE_LIT_MAILING = "6";
  private static final String ADDRESS_USE_COUNTRY_A = "A";
  private static final String ADDRESS_USE_COUNTRY_B = "B";
  private static final String ADDRESS_USE_COUNTRY_C = "C";
  private static final String ADDRESS_USE_COUNTRY_D = "D";
  private static final String ADDRESS_USE_COUNTRY_E = "E";
  private static final String ADDRESS_USE_COUNTRY_F = "F";
  private static final String ADDRESS_USE_COUNTRY_G = "G";
  private static final String ADDRESS_USE_COUNTRY_H = "H";

  private List<String> flagToNList = new ArrayList<>();

  public GreeceTransformer() {
    super(SystemLocation.GREECE);

  }

  public GreeceTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    boolean update = "U".equals(handler.adminData.getReqType());
    Data cmrData = handler.cmrData;
    Addr addrData = handler.addrData;
    boolean crossBorder = isCrossBorder(addrData);

    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    handleEMEADefaults(handler, messageHash, cmrData, addrData, crossBorder);

    messageHash.put("EmbargoCode", !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "");
    messageHash.put("SourceCode", "EFV");
    messageHash.put("CEdivision", "3");
    messageHash.put("MarketingResponseCode", "3");
    messageHash.put("ARemark", "");
    messageHash.put("EnterpriseNo", cmrData.getEnterprise());
    messageHash.put("CustomerLanguage", "1");

    messageHash.put("CollectionCode", "");

    messageHash.put("EconomicCode", "");
    messageHash.put("InvNumber", "");
    messageHash.put("TaxCode", "");

    String sbo = messageHash.get("SBO");
    if (!StringUtils.isEmpty(sbo) && sbo.length() < 7) {
      sbo = StringUtils.rightPad(sbo, 7, '0');
    }

    String cebo = messageHash.get("DPCEBO");
    if (!StringUtils.isEmpty(cebo) && cebo.length() < 7) {
      cebo = StringUtils.rightPad(cebo, 7, '0');
    }
    messageHash.put("SBO", sbo);
    messageHash.put("IBO", sbo);
    messageHash.put("DPCEBO", cebo);

    messageHash.put("SMR", cmrData.getSalesTeamCd());
    messageHash.put("LangCode", "1");

    // add the CustomerType
    String custType = cmrData.getCustSubGrp();
    if (!StringUtils.isBlank(custType)) {
      if ("BUSPR".equals(custType) || "XBP".equals(custType)) {
        messageHash.put("MarketingResponseCode", "5");
        messageHash.put("ARemark", "YES");
        messageHash.put("CustomerType", "");
      } else if ("GOVRN".equals(custType)) {
        messageHash.put("CustomerType", "G");
      } else {
        messageHash.put("CustomerType", "");
      }
    } else {
      if (update) {
        messageHash.put("CustomerType", !StringUtils.isBlank(cmrData.getCrosSubTyp()) ? cmrData.getCrosSubTyp() : "");
      } else {
        messageHash.put("CustomerType", "");
      }
    }
    if (update) {
      for (String field : NO_UPDATE_FIELDS) {
        messageHash.remove(field);
      }
    }

  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    boolean update = "U".equals(handler.adminData.getReqType());
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean crossBorder = isCrossBorder(addrData);
    String addrType = addrData.getId().getAddrType();

    String addrKey = getAddressKey(addrData.getId().getAddrType());
    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    messageHash.put("SourceCode", "EFV");
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    LOG.debug("Handling  Data for " + addrData.getCustNm1());

    // customer name
    String line1 = addrData.getCustNm1();

    // nickname, or occupation
    String line2 = "";

    if (!StringUtils.isBlank(addrData.getCustNm2())) {
      line2 = addrData.getCustNm2();
    }

    String line3 = "";

    if (!StringUtils.isBlank(addrData.getCustNm4())) {
      line3 = "ATT " + addrData.getCustNm4();
      if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrType)) {
        if (DEFAULT_LANDED_COUNTRY.equals(addrData.getLandCntry())) {
          line3 = "Υ/Ο " + addrData.getCustNm4();
        } else {
          line3 = "ATT " + addrData.getCustNm4();
        }
      }
    } else if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
      line3 = addrData.getAddrTxt2();
    } else if (!StringUtils.isBlank(addrData.getPoBox()) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrType)) {
      line3 = "PO BOX " + addrData.getPoBox();
    } else if (!StringUtils.isBlank(addrData.getPoBox()) && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrType)) {
      if (DEFAULT_LANDED_COUNTRY.equals(addrData.getLandCntry())) {
        line3 = "Τ.Θ. " + addrData.getPoBox();
      } else {
        line3 = "PO BOX " + addrData.getPoBox();
      }
    }

    // Street
    String line4 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 = addrData.getAddrTxt();
    } else if (!StringUtils.isBlank(addrData.getPoBox()) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrType)) {
      line4 = "PO BOX " + addrData.getPoBox();
    } else if (!StringUtils.isBlank(addrData.getPoBox()) && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrType)) {
      if (DEFAULT_LANDED_COUNTRY.equals(addrData.getLandCntry())) {
        line4 = "Τ.Θ. " + addrData.getPoBox();
      } else {
        line4 = "PO BOX " + addrData.getPoBox();
      }
    }

    // postal code + city
    String line5 = (!StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "") + " "
        + (!StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "");
    line5 = line5.trim();

    // country
    String line6 = "";

    if (!crossBorder && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrType)) {
      line6 = "Ελλάδα";
    } else {
      line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    }

    int lineNo = 1;
    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    // fixed mapping value, not move up if blank
    for (String line : lines) {
      messageHash.put(addrKey + "Address" + lineNo, line);
      lineNo++;
    }

    // tax office
    messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "AddressT",
        !StringUtils.isEmpty(addrData.getTaxOffice()) ? addrData.getTaxOffice() : "");

    // vat
    if (!MQMsgConstants.ADDR_ZD01.equals(addrData.getId().getAddrType())) {
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "AddressU",
          !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() : "");
    } else {
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "AddressU", "");
    }

    // main phone

    if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrData.getId().getAddrType())
        || CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrData.getId().getAddrType())) {
      String phone = addrData.getCustPhone();
      if (handler.currentAddresses != null) {
        for (Addr addr : handler.currentAddresses) {
          if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addr.getId().getAddrType())) {
            phone = addr.getCustPhone();
            break;
          }
        }
      }
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Phone", phone);
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "Phone", phone);
    } else {
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Phone", "");
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "Phone", "");
    }

    String countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());
    messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Country", crossBorder ? countryName : "");

  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  /*
   * Greece - MQ Code
   * 
   * @Override public String getAddressKey(String addrType) { switch (addrType)
   * { case "ZP01": return "Mail"; case "ZS01": return "Install"; case "ZD01":
   * return "Ship"; default: return ""; } }
   */

  // Comment this method out when reverting to MQ code
  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Local Language translation of Sold";
    case "ZS01":
      return "Sold To";
    case "ZD01":
      return "Ship To";
    case "ZI01":
      return "Install At";
    default:
      return "";
    }
  }

  /*
   * Greece - MQ Code
   * 
   * @Override public String getTargetAddressType(String addrType) { switch
   * (addrType) { case "ZP01": return "Mailing"; case "ZS01": return
   * "Installing"; case "ZD01": return "Shipping"; default: return ""; } }
   */

  // Comment this method out when reverting to MQ
  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Local Language translation of Sold";
    case "ZS01":
      return "Sold To";
    case "ZD01":
      return "Ship To";
    case "ZI01":
      return "Install At";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.GREECE;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "00003";
  }

  /**
   * Checks if this is a cross-border scenario
   * 
   * @param addr
   * @return
   */
  protected boolean isCrossBorder(Addr addr) {
    return !"GR".equals(addr.getLandCntry());
  }

  /*
   * Greece - MQ Code
   * 
   * @Override public String getAddressUse(Addr addr) { switch
   * (addr.getId().getAddrType()) { case MQMsgConstants.ADDR_ZP01: return
   * MQMsgConstants.SOF_ADDRESS_USE_MAILING +
   * MQMsgConstants.SOF_ADDRESS_USE_BILLING; case MQMsgConstants.ADDR_ZS01:
   * return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING +
   * MQMsgConstants.SOF_ADDRESS_USE_SHIPPING +
   * MQMsgConstants.SOF_ADDRESS_USE_EPL; case MQMsgConstants.ADDR_ZD01: return
   * MQMsgConstants.SOF_ADDRESS_USE_SHIPPING; default: return
   * MQMsgConstants.SOF_ADDRESS_USE_SHIPPING; } }
   */

  // Comment this method out when reverting to MQ
  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZP02:
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

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData GREECE transformer...");
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    formatDataLines(dummyHandler);
    String landedCntry = "";
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("CEdivision"))) {
        legacyCust.setCeDivision(dummyHandler.messageHash.get("CEdivision"));
      }
      legacyCust.setAccAdminBo("");
      // legacyCust.setCeDivision("2"); // extract the phone from billing
      // as main phone
      for (Addr addr : cmrObjects.getAddresses()) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          legacyCust.setTelNoOrVat(addr.getCustPhone());
          landedCntry = addr.getLandCntry();
          break;
        }
      }
      // other fields to be transformed is pending
      // mrc
      String custType = data.getCustSubGrp();
      legacyCust.setDcRepeatAgreement("0"); // CAGXB
      legacyCust.setLeasingInd("0"); // CIEDC
      legacyCust.setAuthRemarketerInd("0"); // CIEXJ
      if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBP".equals(custType)) {
        legacyCust.setMrcCd("5");
        legacyCust.setAuthRemarketerInd("1");
      } else {
        legacyCust.setMrcCd("3");
      }
      legacyCust.setLangCd("1");
      if (MQMsgConstants.CUSTSUBGRP_GOVRN.equals(custType)) {
        legacyCust.setCustType("G");
      }
      String formatSBO = data.getSalesBusOffCd() + "0000";
      legacyCust.setIbo(formatSBO);
      legacyCust.setSbo(formatSBO);
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      for (Addr addr : cmrObjects.getAddresses()) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          if (!StringUtils.isEmpty(addr.getCustPhone())) {
            legacyCust.setTelNoOrVat(addr.getCustPhone());
          } else {
            legacyCust.setTelNoOrVat("");
          }
          landedCntry = addr.getLandCntry();
          break;
        }
      }
      String dataEmbargoCd = data.getEmbargoCd();
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin); // permanent
                                                                                            // removal-single
      // inactivation
      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()) && !"TREC".equals(admin.getReqReason())) {
        if (!StringUtils.isBlank(rdcEmbargoCd) && ("Y".equals(rdcEmbargoCd))) {
          if (StringUtils.isBlank(data.getEmbargoCd())) {
            legacyCust.setEmbargoCd("");
          }
        }
      } // Support temporary reactivation
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

      if (!StringUtils.isBlank(data.getAbbrevNm())) {
        legacyCust.setAbbrevNm(data.getAbbrevNm());
      } else {
        legacyCust.setAbbrevNm("");
      }

      String isuClientTier = (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
          + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : "");
      if (isuClientTier != null && isuClientTier.length() == 3) {
        legacyCust.setIsuCd(isuClientTier);
      } else {
        legacyCust.setIsuCd("");
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
        legacyCust.setIbo(data.getSalesBusOffCd() + "0000");
        legacyCust.setSbo(data.getSalesBusOffCd() + "0000");
      } else {
        legacyCust.setIbo("");
        legacyCust.setSbo("");
      }
      if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
        legacyCust.setSalesRepNo(data.getRepTeamMemberNo());
      } else {
        legacyCust.setSalesRepNo("");
      }

      if (!StringUtils.isBlank(data.getSalesTeamCd())) {
        legacyCust.setSalesGroupRep(data.getSalesTeamCd());
      } else {
        legacyCust.setSalesGroupRep("");
      }

      if (!StringUtils.isBlank(data.getCollectionCd())) {
        legacyCust.setCollectionCd(data.getCollectionCd());
      } else {
        legacyCust.setCollectionCd("");
      }

    } // common data for C/U
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

    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("ModeOfPayment"))) {
      legacyCust.setModeOfPayment(dummyHandler.messageHash.get("ModeOfPayment"));
    }

    legacyCust.setCustType(dummyHandler.messageHash.get("CustomerType"));
    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("EconomicCode"))) {
      legacyCust.setEconomicCd(dummyHandler.messageHash.get("EconomicCode"));
    }
    legacyCust.setBankBranchNo(data.getIbmDeptCostCenter() != null ? data.getIbmDeptCostCenter() : "");
    if (!StringUtils.isBlank(data.getEnterprise())) {
      legacyCust.setEnterpriseNo(data.getEnterprise());
    } else {
      legacyCust.setEnterpriseNo("");
    }
    legacyCust.setCeBo("");
    // CREATCMR-4293
    if (!StringUtils.isEmpty(data.getIsuCd())) {
      if (StringUtils.isEmpty(data.getClientTier())) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      }
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
  public boolean hasCmrtCustExt() {
    return true;
  }

  @Override
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {
    for (Addr addr : cmrObjects.getAddresses()) {
      if (addr.getId().getAddrType().equalsIgnoreCase(CmrConstants.ADDR_TYPE.ZP01.toString()) && StringUtils.isNotBlank(addr.getTaxOffice())) {
        legacyCustExt.setiTaxCode((addr.getTaxOffice()));
      }
    }
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    formatAddressLines(dummyHandler);
    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    }
    legacyAddr.setAddrLineT("");
    legacyAddr.setAddrLineU("");
  }

  @Override
  public void transformOtherData(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects, CMRRequestContainer cmrObjects) {
    long reqId = cmrObjects.getAdmin().getId().getReqId();
    boolean update = "U".equals(cmrObjects.getAdmin().getReqType());

    List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
    List<Addr> addrList = cmrObjects.getAddresses();
    if (!update) {
      int seqStartForRequiredAddr = 2;
      for (int i = 0; i < addrList.size(); i++) {
        Addr addr = addrList.get(i);
        String addrSeq = addr.getId().getAddrSeq();
        String addrType = addr.getId().getAddrType();
        if (addrSeq.equals("00001")) {
          if (addrType.equalsIgnoreCase(CmrConstants.ADDR_TYPE.ZP01.toString())) {
            // copy mailing from billing
            copyMailingFromBilling(legacyObjects, legacyAddrList.get(i));
          }
          updateRequiredAddresses(entityManager, reqId, addrList.get(i).getId().getAddrType(), legacyAddrList.get(i).getId().getAddrNo(),
              changeSeqNo(seqStartForRequiredAddr++), legacyObjects, i);
        }
        modifyAddrUseFields(getAddressUse(addr), legacyAddrList.get(i));
      }
    } else {
      updateMailingAddrWithBillingData(legacyObjects);
      modifSoldToYFlag(legacyObjects);
      legacyObjects.getCustomer().setLangCd("1");
    }
  }

  private void updateMailingAddrWithBillingData(LegacyDirectObjectContainer legacyObjects) {
    CmrtAddr billingAddr = null;
    int mailingAddrIndex = -1;
    for (int i = 0; i < legacyObjects.getAddresses().size(); i++) {
      if ("Y".equals(legacyObjects.getAddresses().get(i).getIsAddrUseMailing())) {
        mailingAddrIndex = i;
      } else if ("Y".equals(legacyObjects.getAddresses().get(i).getIsAddrUseBilling())) {
        billingAddr = legacyObjects.getAddresses().get(i);
      }
    }

    if (billingAddr != null && billingAddr.isForUpdate()) {
      if (billingAddr != null && mailingAddrIndex != -1) {
        LOG.info("GREECE -- UPDATING - update mailing");
        CmrtAddr oldMailingAddr = legacyObjects.getAddresses().get(mailingAddrIndex);
        CmrtAddr newMailingAddr = (CmrtAddr) SerializationUtils.clone(billingAddr);
        newMailingAddr.getId().setAddrNo(oldMailingAddr.getId().getAddrNo());
        newMailingAddr.setForUpdate(true);
        modifyAddrUseFields(MQMsgConstants.SOF_ADDRESS_USE_MAILING, newMailingAddr);
        legacyObjects.getAddresses().set(mailingAddrIndex, newMailingAddr);

      } else if (billingAddr != null && mailingAddrIndex == -1) {
        LOG.info("GREECE -- UPDATING - create new mailing");
        legacyObjects.getAddresses().add(createNewMailingData(billingAddr, getAvailableAddrSeq(legacyObjects.getAddresses())));
      }
    } else if (billingAddr != null && billingAddr.isForCreate()) {
      LOG.info("GREECE -- UPDATING - create new mailing from new billing");
      legacyObjects.getAddresses().add(createNewMailingData(billingAddr, getAvailableAddrSeq(legacyObjects.getAddresses())));
    }
  }

  private CmrtAddr createNewMailingData(CmrtAddr billingAddr, String addrSeq) {
    CmrtAddr newMailingAddr = (CmrtAddr) SerializationUtils.clone(billingAddr);
    newMailingAddr.getId().setAddrNo(addrSeq);
    newMailingAddr.setForCreate(true);
    modifyAddrUseFields(MQMsgConstants.SOF_ADDRESS_USE_MAILING, newMailingAddr);
    return newMailingAddr;
  }

  private void modifSoldToYFlag(LegacyDirectObjectContainer legacyObjects) {

    if (!flagToNList.isEmpty()) {
      for (int i = 0; i < legacyObjects.getAddresses().size(); i++) {
        if ("Y".equals(legacyObjects.getAddresses().get(i).getIsAddrUseInstalling())) {
          if (flagToNList.contains("ZD01")) {
            legacyObjects.getAddresses().get(i).setIsAddrUseShipping("N");
          }
          if (flagToNList.contains("ZI01")) {
            legacyObjects.getAddresses().get(i).setIsAddrUseEPL("N");
          }
          if (flagToNList.contains("ZP01")) {
            legacyObjects.getAddresses().get(i).setIsAddrUseBilling("N");
            legacyObjects.getAddresses().get(i).setIsAddrUseMailing("N");
          }

          legacyObjects.getAddresses().get(i).setForUpdate(true);
        }
      }
    }
  }

  private String getAvailableAddrSeq(List<CmrtAddr> legacyAddr) {
    List<Integer> sequences = new ArrayList<>();
    for (CmrtAddr addr : legacyAddr) {
      sequences.add(Integer.parseInt(addr.getId().getAddrNo()));
    }
    int maxSeq = Collections.max(sequences);
    return String.format("%05d", maxSeq + 1);
  }

  private void updateRequiredAddresses(EntityManager entityManager, long reqId, String addrType, String oldSeq, String newSeq,
      LegacyDirectObjectContainer legacyObjects, int index) {
    updateAddrSeq(entityManager, reqId, addrType, oldSeq, newSeq, null);
    legacyObjects.getAddresses().get(index).getId().setAddrNo(newSeq);
  }

  private void copyMailingFromBilling(LegacyDirectObjectContainer legacyObjects, CmrtAddr billingAddr) {
    CmrtAddr mailingAddr = (CmrtAddr) SerializationUtils.clone(billingAddr);
    mailingAddr.getId().setAddrNo(String.format("%05d", 1)); // should be 00001
    modifyAddrUseFields(MQMsgConstants.SOF_ADDRESS_USE_MAILING, mailingAddr);
    legacyObjects.getAddresses().add(mailingAddr);
  }

  private String changeSeqNo(int newSeq) {
    return String.format("%05d", newSeq);
  }

  private void updateAddrSeq(EntityManager entityManager, long reqId, String addrType, String oldSeq, String newSeq, String kunnr) {
    String updateSeq = ExternalizedQuery.getSql("LEGACYD.UPDATE_ADDR_SEQ");
    PreparedQuery q = new PreparedQuery(entityManager, updateSeq);
    q.setParameter("NEW_SEQ", newSeq);
    q.setParameter("REQ_ID", reqId);
    q.setParameter("TYPE", addrType);
    q.setParameter("OLD_SEQ", oldSeq);
    q.setParameter("SAP_NO", kunnr);
    LOG.debug("GREECE - Assigning address sequence " + newSeq + " to " + addrType + " address.");
    q.executeSql();
  }

  private void modifyAddrUseFieldsforMassUpdt(String addrUse, CmrtAddr legacyAddr, CmrtAddr oldAddr, String addrType) {

    if (("mail".equalsIgnoreCase(addrType) || "bill".equalsIgnoreCase(addrType)) && oldAddr != null) {
      legacyAddr.setIsAddrUseMailing(oldAddr.getIsAddrUseMailing());
      legacyAddr.setIsAddrUseBilling(oldAddr.getIsAddrUseBilling());
    } else {
      legacyAddr.setIsAddrUseMailing(ADDRESS_USE_NOT_EXISTS);
      legacyAddr.setIsAddrUseBilling(ADDRESS_USE_NOT_EXISTS);
    }
    legacyAddr.setIsAddrUseInstalling(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddrUseShipping(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddrUseEPL(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddrUseLitMailing(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseA(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseB(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseC(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseD(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseE(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseF(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseG(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseH(ADDRESS_USE_NOT_EXISTS);
    if (oldAddr != null) {
      if (ADDRESS_USE_NOT_EXISTS.equalsIgnoreCase(oldAddr.getIsAddrUseMailing())) {
        return;
      }
    }
    for (String use : addrUse.split("")) {
      switch (use) {
      case ADDRESS_USE_MAILING:
        legacyAddr.setIsAddrUseMailing(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_BILLING:
        legacyAddr.setIsAddrUseBilling(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_INSTALLING:
        legacyAddr.setIsAddrUseInstalling(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_SHIPPING:
        legacyAddr.setIsAddrUseShipping(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_EPL_MAILING:
        legacyAddr.setIsAddrUseEPL(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_LIT_MAILING:
        legacyAddr.setIsAddrUseLitMailing(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_A:
        legacyAddr.setIsAddressUseA(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_B:
        legacyAddr.setIsAddressUseB(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_C:
        legacyAddr.setIsAddressUseC(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_D:
        legacyAddr.setIsAddressUseD(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_E:
        legacyAddr.setIsAddressUseE(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_F:
        legacyAddr.setIsAddressUseF(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_G:
        legacyAddr.setIsAddressUseG(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_H:
        legacyAddr.setIsAddressUseH(ADDRESS_USE_EXISTS);
        break;
      }
    }
  }

  private void modifyAddrUseFields(String addrUse, CmrtAddr legacyAddr) {
    setAddrUseFieldsToN(legacyAddr);
    for (String use : addrUse.split("")) {
      switch (use) {
      case ADDRESS_USE_MAILING:
        legacyAddr.setIsAddrUseMailing(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_BILLING:
        legacyAddr.setIsAddrUseBilling(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_INSTALLING:
        legacyAddr.setIsAddrUseInstalling(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_SHIPPING:
        legacyAddr.setIsAddrUseShipping(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_EPL_MAILING:
        legacyAddr.setIsAddrUseEPL(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_LIT_MAILING:
        legacyAddr.setIsAddrUseLitMailing(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_A:
        legacyAddr.setIsAddressUseA(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_B:
        legacyAddr.setIsAddressUseB(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_C:
        legacyAddr.setIsAddressUseC(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_D:
        legacyAddr.setIsAddressUseD(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_E:
        legacyAddr.setIsAddressUseE(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_F:
        legacyAddr.setIsAddressUseF(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_G:
        legacyAddr.setIsAddressUseG(ADDRESS_USE_EXISTS);
        break;
      case ADDRESS_USE_COUNTRY_H:
        legacyAddr.setIsAddressUseH(ADDRESS_USE_EXISTS);
        break;
      }
    }
  }

  private void setAddrUseFieldsToN(CmrtAddr legacyAddr) {
    legacyAddr.setIsAddrUseMailing(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddrUseBilling(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddrUseInstalling(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddrUseShipping(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddrUseEPL(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddrUseLitMailing(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseA(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseB(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseC(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseD(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseE(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseF(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseG(ADDRESS_USE_NOT_EXISTS);
    legacyAddr.setIsAddressUseH(ADDRESS_USE_NOT_EXISTS);
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For GR...");
    if (custSubGrp != null && "INTER".equals(custSubGrp) || custSubGrp != null && "XINTR".equals(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
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

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust cust, CMRRequestContainer cmrObjects, MassUpdtData muData) {
    LOG.debug("Mapping default Data values from transformLegacyCustomerDataMassUpdate.");
    if (!StringUtils.isBlank(muData.getAbbrevNm())) {
      cust.setAbbrevNm(muData.getAbbrevNm());
    }

    if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
      cust.setAbbrevLocn(muData.getAbbrevLocn());
    }

    String isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
        + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
    if (isuClientTier != null && isuClientTier.endsWith("@")) {
      cust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : cust.getIsuCd().substring(0, 2)) + "7");
    } else if (isuClientTier != null && isuClientTier.length() == 3) {
      cust.setIsuCd(isuClientTier);
    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      if ("@@@@@@".equals(muData.getEnterprise())) {
        cust.setEnterpriseNo("");
      } else {
        cust.setEnterpriseNo(muData.getEnterprise());
      }
    }

    if (!StringUtils.isBlank(muData.getCollectionCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getCollectionCd())) {
        cust.setCollectionCd("");
      } else {
        cust.setCollectionCd(muData.getCollectionCd());
      }
    }

    if (!StringUtils.isBlank(muData.getIsicCd())) {
      cust.setIsicCd(muData.getIsicCd());
    }

    if (!StringUtils.isBlank(muData.getInacCd())) {
      if ("@@@@".equals(muData.getInacCd().trim())) {
        cust.setInacCd("");
      } else {
        cust.setInacCd(muData.getInacCd());
      }
    }

    if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
      String subInd = muData.getSubIndustryCd();
      cust.setImsCd(subInd);
    }

    if (!StringUtils.isBlank(muData.getModeOfPayment())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getModeOfPayment().trim())) {
        cust.setModeOfPayment("");
      } else {
        cust.setModeOfPayment(muData.getModeOfPayment());
      }
    }

    if (!StringUtils.isBlank(muData.getMiscBillCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getMiscBillCd())) {
        cust.setEmbargoCd("");
      } else {
        cust.setEmbargoCd(muData.getMiscBillCd());
      }
    }

    if (!StringUtils.isBlank(muData.getRestrictTo())) {
      if (DEFAULT_CLEAR_NUM.equals(muData.getRestrictTo())) {
        cust.setTelNoOrVat("");
      } else {
        cust.setTelNoOrVat(muData.getRestrictTo());
      }
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      cust.setVat(muData.getVat());
    }

    if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
      cust.setSalesRepNo(muData.getRepTeamMemberNo());
      String salesGroupRep = LegacyCommonUtil.getSalesGroupRepMap(entityManager, cmrIssuingCntry, muData.getRepTeamMemberNo());
      cust.setSalesGroupRep(salesGroupRep);
    }

    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    // cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());
    if (!StringUtils.isEmpty(muData.getIsuCd()) && "5K".equals(muData.getIsuCd())) {
      cust.setIsuCd(muData.getIsuCd() + "7");
    } else {
      isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
          + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
      if (isuClientTier != null && isuClientTier.length() == 3) {
        cust.setIsuCd(isuClientTier);
      }
    }
  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr addr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {
    legacyAddr.setForUpdate(true);
    CmrtAddr mailingAddr = null;
    CmrtAddr billingAddr = null;
    String sql = ExternalizedQuery.getSql("GREECE.GET.MAILINGADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", SystemLocation.GREECE);
    query.setParameter("RCUXA", legacyAddr.getId().getCustomerNo());
    query.setParameter("ADDRMAIL", "Y");
    mailingAddr = query.getSingleResult(CmrtAddr.class);

    String sqlBillingAddr = ExternalizedQuery.getSql("GREECE.GET.BILLINGADDR");
    PreparedQuery queryBillingAddr = new PreparedQuery(entityManager, sqlBillingAddr);
    queryBillingAddr.setParameter("RCYAA", SystemLocation.GREECE);
    queryBillingAddr.setParameter("RCUXA", legacyAddr.getId().getCustomerNo());
    queryBillingAddr.setParameter("ADDRBILL", "Y");
    billingAddr = queryBillingAddr.getSingleResult(CmrtAddr.class);

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

    if ("ZD01".equals(addr.getId().getAddrType())) {
      if (!StringUtils.isBlank(addr.getCustPhone())) {
        if (DEFAULT_CLEAR_NUM.equals(addr.getCustPhone())) {
          legacyAddr.setAddrPhone("");
        } else {
          legacyAddr.setAddrPhone(addr.getCustPhone());
        }
      }
    }

    if (!StringUtils.isBlank(addr.getPostCd())) {
      legacyAddr.setZipCode(addr.getPostCd());
    }

    String poBox = addr.getPoBox();
    if (!StringUtils.isEmpty(poBox)) {
      legacyAddr.setPoBox(addr.getPoBox());
    }
    formatMassUpdateAddressLines(entityManager, legacyAddr, addr, false);
    legacyObjects.addAddress(legacyAddr);

    if ("Y".equals(legacyAddr.getIsAddrUseBilling())) {
      CmrtAddr newMailingAddr = (CmrtAddr) SerializationUtils.clone(legacyAddr);
      if (!mailingAddr.equals(billingAddr)) {
        newMailingAddr.getId().setAddrNo(mailingAddr.getId().getAddrNo());
      } else {
        newMailingAddr.getId().setAddrNo(String.format("%05d", 1));
      }
      modifyAddrUseFieldsforMassUpdt(MQMsgConstants.SOF_ADDRESS_USE_MAILING, newMailingAddr, mailingAddr, "mail");
      newMailingAddr.setForUpdate(true);
      legacyObjects.addAddress(newMailingAddr);
    }

    if ("N".equals(legacyAddr.getIsAddrUseBilling()) && "Y".equals(legacyAddr.getIsAddrUseMailing())) {
      CmrtAddr newBillingAddr = (CmrtAddr) SerializationUtils.clone(legacyAddr);
      newBillingAddr.getId().setAddrNo(billingAddr.getId().getAddrNo());
      modifyAddrUseFieldsforMassUpdt(MQMsgConstants.SOF_ADDRESS_USE_MAILING, newBillingAddr, billingAddr, "bill");
      newBillingAddr.setForUpdate(true);
      legacyObjects.addAddress(newBillingAddr);
    }

  }

  @Override
  public void formatMassUpdateAddressLines(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr massUpdtAddr, boolean isFAddr) {
    LOG.debug("*** START GR formatMassUpdateAddressLines >>>");
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

    // customer name
    line1 = legacyAddr.getAddrLine1();

    if (!StringUtils.isBlank(massUpdtAddr.getCustNm2())) {
      line2 = massUpdtAddr.getCustNm2();
    }

    // Att Person or Address Con't/Occupation
    if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
      line3 = massUpdtAddr.getAddrTxt2();
    } else if (!StringUtils.isBlank(massUpdtAddr.getCustNm4())) {
      if (!StringUtils.isEmpty(line3) && !line3.toUpperCase().startsWith("ATT ") && !line3.toUpperCase().startsWith("ATT:")) {
        line3 = "ATT " + line3;
      }
      if (!StringUtils.isEmpty(line3) && !line3.toUpperCase().startsWith("Υ/Ο ") && !line3.toUpperCase().startsWith("Υ/Ο:")) {
        line3 = "Υ/Ο " + line3;
      }

      if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(massUpdtAddr.getId().getAddrType())) {
        line3 = "Υ/Ο " + massUpdtAddr.getCustNm4().trim();
      } else {
        line3 = "ATT " + massUpdtAddr.getCustNm4().trim();
      }

    }

    // Street OR PO BOX
    if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt())) {
      line4 = massUpdtAddr.getAddrTxt();
    } else if (!StringUtils.isEmpty(massUpdtAddr.getPoBox())) {
      if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(massUpdtAddr.getId().getAddrType())) {
        line4 = "Τ.Θ. " + massUpdtAddr.getPoBox();
      } else {
        line4 = "PO BOX " + massUpdtAddr.getPoBox();
      }
      legacyAddr.setPoBox(massUpdtAddr.getPoBox());
    }

    if (!StringUtils.isEmpty(massUpdtAddr.getPostCd()) || !StringUtils.isEmpty(massUpdtAddr.getCity1())) {
      line5 = (legacyAddr.getZipCode() != null ? legacyAddr.getZipCode().trim() + " " : "")
          + (legacyAddr.getCity() != null ? legacyAddr.getCity().trim() : "");
    }

    if (!StringUtils.isEmpty(massUpdtAddr.getLandCntry())) {
      line6 = LandedCountryMap.getCountryName(massUpdtAddr.getLandCntry()).toUpperCase();
    }

    String[] lines = new String[] { (line1 != null ? line1.trim() : ""), (line2 != null ? line2.trim() : ""), (line3 != null ? line3.trim() : ""),
        (line4 != null ? line4.trim() : ""), (line5 != null ? line5.trim() : ""), (line6 != null ? line6.trim() : "") };

    int lineNo = 1;
    LOG.debug("Lines: " + (line1 != null ? line1.trim() : "") + " | " + (line2 != null ? line2.trim() : "") + " | "
        + (line3 != null ? line3.trim() : "") + " | " + (line4 != null ? line4.trim() : "") + " | " + (line5 != null ? line5.trim() : "") + " | "
        + (line6 != null ? line6.trim() : ""));

    for (String line : lines) {
      messageHash.put(getAddressKey((massUpdtAddr.getId().getAddrType()) + "Address" + lineNo).toString(), line);
      lineNo++;
    }
    legacyAddr.setAddrLine1(line1 != null ? line1.trim() : "");
    legacyAddr.setAddrLine2(line2 != null ? line2.trim() : "");
    legacyAddr.setAddrLine3(line3 != null ? line3.trim() : "");
    legacyAddr.setAddrLine4(line4 != null ? line4.trim() : "");
    legacyAddr.setAddrLine5(line5 != null ? line5.trim() : "");
    legacyAddr.setAddrLine6(line6 != null ? line6.trim() : "");
  }

  @Override
  public void transformLegacyCustomerExtDataMassUpdate(EntityManager entityManager, CmrtCustExt custExt, CMRRequestContainer cmrObjects,
      MassUpdtData muData, String cmr) throws Exception {
    // for tax office
    List<MassUpdtAddr> muaList = cmrObjects.getMassUpdateAddresses();
    if (muaList != null && muaList.size() > 0) {
      for (MassUpdtAddr mua : muaList) {
        if ("ZP01".equals(mua.getId().getAddrType())) {
          if (!StringUtils.isBlank(mua.getFloor())) {
            if (DEFAULT_CLEAR_CHAR.equals(mua.getFloor())) {
              custExt.setiTaxCode("");
            } else {
              custExt.setiTaxCode(mua.getFloor());
            }
            break;
          }

        }
      }
    }
    List<MassUpdtAddr> muAddrList = cmrObjects.getMassUpdateAddresses();
    MassUpdtAddr zp01Addr = new MassUpdtAddr();
    for (MassUpdtAddr muAddr : muAddrList) {
      if ("ZP01".equals(muAddr.getId().getAddrType())) {
        zp01Addr = muAddr;
        break;
      }
    }
    if (zp01Addr != null && !StringUtils.isBlank(zp01Addr.getFloor())) {
      if ("@".equals(zp01Addr.getFloor())) {
        custExt.setiTaxCode("");
      } else {
        custExt.setiTaxCode(zp01Addr.getFloor());
      }
    }
  }

  @Override
  public boolean sequenceNoUpdateLogic(EntityManager entityManager, CMRRequestContainer cmrObjects, Addr currAddr, boolean flag) {
    if (isSharedSequence(cmrObjects, currAddr)) {
      return true;
    }
    return false;
  }

  private boolean isSharedSequence(CMRRequestContainer cmrObjects, Addr currAddr) {
    Map<String, Integer> sequences = new HashMap<String, Integer>();
    String zs01Seq = "";

    for (Addr addr : cmrObjects.getAddresses()) {
      if (!sequences.containsKey(addr.getId().getAddrSeq())) {
        sequences.put(addr.getId().getAddrSeq(), 0);
      }
      sequences.put(addr.getId().getAddrSeq(), sequences.get(addr.getId().getAddrSeq()) + 1);

      if ("ZS01".equals(addr.getId().getAddrType())) {
        zs01Seq = addr.getId().getAddrSeq();
      }
    }
    LOG.debug("GR Sequences : " + sequences);

    if (sequences.get(currAddr.getId().getAddrSeq()) > 1) {
      // ZS01 and current address have the same sequence

      if (zs01Seq.equals(currAddr.getId().getAddrSeq()) && !("ZS01".equals(currAddr.getId().getAddrType()))) {
        flagToNList.add(currAddr.getId().getAddrType());
      }

      return true;
    } else {
      return false;
    }
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
