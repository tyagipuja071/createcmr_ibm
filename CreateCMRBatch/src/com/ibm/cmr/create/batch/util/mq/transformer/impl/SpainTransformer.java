/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
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
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SpainTransformer extends MessageTransformer {

  private static final String[] ADDRESS_ORDER = { "ZP01", "ZS01", "ZI01", "ZD01", "ZS02", "ZP02" };

  private static final Logger LOG = Logger.getLogger(SpainTransformer.class);

  private static final List<String> EU_COUNTRIES = Arrays.asList("AT", "BE", "BG", "HR", "CY", "CZ", "DE", "DK", "EE", "GR", "FI", "FR", "GB", "HU",
      "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK");

  private static final List<String> NON_EU_COUNTRIES = Arrays.asList("SG", "BA", "GE", "IL", "CO", "CR", "SA", "MA", "SO", "MU", "SS", "BI", "ZA",
      "AE", "NA", "ML", "GU", "CA", "AW");

  public static final String DEFAULT_LANDED_COUNTRY = "ES";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";

  public SpainTransformer(String issuingCntry) {
    super(issuingCntry);

  }

  public SpainTransformer() {
    super(SystemLocation.SPAIN);

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
    // trim VAT prefix for Local Defect 1481070 fix: change cross border based
    // on billing only
    if (!zs01CrossBorder(handler)) {
      if (!StringUtils.isEmpty(vat)) {
        messageHash.put("VAT", vat);
      }
    }

    // for cross border,
    // the AbbreviatedLocation is VAT, and VAT is constant
    // Defect 1481070 fix: change cross border based on billing only
    if (zs01CrossBorder(handler)) {

      // rework for story 1618024

      if (!StringUtils.isEmpty(vat)) {
        // CB with VAT : AbbrevLoc =VAT, VAT =CEE000000
        // if (vat.matches("^[A-Z]{2}.*")) {
        messageHash.put("AbbreviatedLocation", cmrData.getAbbrevLocn());
        // } else {
        // messageHash.put("AbbreviatedLocation", vat);
        // }

        // for nonEU, set CustomerLocation = Country Code + 000, 999 if EU
        if (EU_COUNTRIES.contains(addrData.getLandCntry())) {
          messageHash.put("LocationNumber", addrData.getLandCntry() + "999");
        } else if (NON_EU_COUNTRIES.contains(addrData.getLandCntry())) {
          messageHash.put("LocationNumber", addrData.getLandCntry() + "999");
        } else {
          messageHash.put("LocationNumber", addrData.getLandCntry() + "000");
        }
        messageHash.put("VAT", "CEE000000");
      } else {
        // CB without VAT : AbbrevLoc =Country Name, VAT =A00000000
        messageHash.put("AbbreviatedLocation", LandedCountryMap.getCountryName(addrData.getLandCntry()));
        messageHash.put("VAT", "A00000000");
      }
      // if (EU_COUNTRIES.contains(addrData.getLandCntry())) {
      // if (!StringUtils.isEmpty(vat) && vat.matches("^[A-Z]{2}.*")) {
      // messageHash.put("AbbreviatedLocation", vat.substring(2, vat.length()));
      // } else {
      // messageHash.put("AbbreviatedLocation", !StringUtils.isEmpty(vat) ? vat
      // : "");
      // }
      // messageHash.put("VAT", "CEE000000");
      // } else {
      // if (!StringUtils.isEmpty(vat) && vat.matches("^[A-Z]{2}.*")) {
      // messageHash.put("AbbreviatedLocation", vat.substring(2, vat.length()));
      // } else if (!StringUtils.isEmpty(vat)) {
      // messageHash.put("AbbreviatedLocation", vat);
      // } else {
      // messageHash.put("AbbreviatedLocation",
      // LandedCountryMap.getCountryName(addrData.getLandCntry()));
      // }
      // messageHash.put("VAT", "A00000000");
      // }
    }

    messageHash.put("EnterpriseNo", !StringUtils.isEmpty(cmrData.getEnterprise()) ? cmrData.getEnterprise() : "");
    if (StringUtils.isEmpty(messageHash.get("LocationNumber"))) {
      messageHash.put("LocationNumber", !StringUtils.isEmpty(cmrData.getLocationNumber()) ? cmrData.getLocationNumber() : "");
    }
    messageHash.put("ModeOfPayment", !StringUtils.isEmpty(cmrData.getModeOfPayment()) ? cmrData.getModeOfPayment() : "");
    messageHash.put("CurrencyCode", !StringUtils.isEmpty(cmrData.getLegacyCurrencyCd()) ? cmrData.getLegacyCurrencyCd() : "");
    // Defect 1471265: collection code - SPAIN Fix : commenting below line
    messageHash.put("DistrictCode", !StringUtils.isEmpty(cmrData.getCollectionCd()) ? cmrData.getCollectionCd() : "");
    messageHash.put("CollectionCode", "");

    String embargoCode = !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "";
    messageHash.put("EmbargoCode", embargoCode);

    if (!StringUtils.isEmpty(cmrData.getMailingCondition()) && !"X".equals(cmrData.getMailingCondition())) {
      messageHash.put("MailingCondition", cmrData.getMailingCondition());
    } else {
      messageHash.put("MailingCondition", update ? "@" : "");
    }

    if (!StringUtils.isEmpty(cmrData.getSubIndustryCd())) {
      messageHash.put("EconomicCode", cmrData.getSubIndustryCd().substring(0, 1) + cmrData.getSubIndustryCd());
    } else {
      messageHash.put("EconomicCode", "");
    }
    handleDataDefaults(handler, messageHash, cmrData, crossBorder, addrData);

    // RTC1406039: if vat is empty, send dummy VAT A00000000
    if (StringUtils.isEmpty(messageHash.get("VAT"))) {
      messageHash.put("VAT", "A00000000");
    }

    if (update) {
      String phone = getBillingPhone(handler);
      messageHash.put("MailPhone", phone);

      // 1508376 - Spain - update error related to ISU
      if (StringUtils.isEmpty(cmrData.getIsuCd()) || StringUtils.isEmpty(cmrData.getClientTier())) {
        messageHash.put("ISU", "");
      }
    }

  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());
    boolean crossBorder = isCrossBorder(addrData);

    String addrKey = getAddressKey(addrData.getId().getAddrType());
    LOG.trace("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    messageHash.put("SourceCode", "EF0");
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

    // fiscal address
    if (MQMsgConstants.ADDR_ZP02.equals(addrData.getId().getAddrType())) {
      if (MQMsgConstants.CUSTSUBGRP_PRICU.equals(cmrData.getCustSubGrp()) || MQMsgConstants.CUSTSUBGRP_IBMEM.equals(cmrData.getCustSubGrp())) {
        line1 = "F" + addrData.getCustNm1();
      } else {
        line1 = "J" + addrData.getCustNm1();
      }
      line2 = "CL" + addrData.getAddrTxt();

      StringBuilder street = new StringBuilder();
      line2 = StringUtils.replace(line2, " - ", "-");
      line2 = StringUtils.replace(line2, "- ", "-");
      line2 = StringUtils.replace(line2, " -", "-");
      String[] parts = line2.split("[^A-Za-zÁáÉéÍíÓóÚúÑñ.0-9]");
      for (String part : parts) {
        if (!StringUtils.isEmpty(part) && (StringUtils.isNumeric(part) || (part.matches(".*\\d{1}.*") && part.contains("-")))) {
          line3 = part;
        } else if (!StringUtils.isEmpty(part)) {
          street.append(street.length() > 0 ? " " : "");
          street.append(part);
        }
      }

      line2 = street.toString();
      if (StringUtils.isEmpty(addrData.getAddrTxt()) && !StringUtils.isEmpty(addrData.getPoBox())) {
        line2 = "CLAPTO " + addrData.getPoBox().replaceAll("[^\\d]", "");
      }
      line3 = StringUtils.leftPad(line3, 5, '0');

      line4 = addrData.getCity1();
      if (crossBorder) {
        line5 = "88888";
      } else {
        line5 = addrData.getPostCd();
      }
    } else {
      line1 = addrData.getCustNm1();
      line2 = addrData.getCustNm2();
      if (StringUtils.isEmpty(line2) && crossBorder) {
        line2 = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4().trim() : "";
        if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
          line2 = "ATT " + line2;
        }
        if (line2.length() > 30) {
          line2 = line2.substring(0, 30);
        }
      }
      if (StringUtils.isEmpty(line2)) {
        line2 = addrData.getAddrTxt2();
      }

      line3 = addrData.getAddrTxt();
      String poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "";
      if (!StringUtils.isEmpty(poBox) && !poBox.toUpperCase().startsWith("APTO")) {
        poBox = " APTO " + poBox;
      }
      line3 = (StringUtils.isEmpty(line3) ? "" : line3) + poBox;

      line4 = addrData.getPostCd() + " " + addrData.getCity1();

      if (crossBorder) {
        line5 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      } else {
        line5 = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4().trim() : "";
        if (!StringUtils.isEmpty(line5) && !line5.toUpperCase().startsWith("ATT ") && !line5.toUpperCase().startsWith("ATT:")) {
          // Defect 1740670: SPAIN - attention person - ATT
          line5 = "ATT " + line5;
        }
        if (line5.length() > 30) {
          line5 = line5.substring(0, 30);
        }
      }

      line6 = "";
      if (MQMsgConstants.ADDR_ZD01.equals(addrData.getId().getAddrType()) && update) {
        line6 = addrData.getCustPhone();
      }

      if (!update && MQMsgConstants.ADDR_ZP01.equals(addrData.getId().getAddrType())) {
        // send to MailPhone the billing phone number
        String phone = getBillingPhone(handler);
        messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Phone", phone);
      } else {
        // always clear *Phone otherwise
        messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Phone", "");
      }
    }

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    for (String line : lines) {
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Address" + lineNo, line);
      lineNo++;
    }

    if ("N".equals(addrData.getImportInd()) && MQMsgConstants.ADDR_ZD01.equals(addrData.getId().getAddrType())) {
      // preferred sequence no for additional shipping
      messageHash.put("SequenceNo", StringUtils.isEmpty(addrData.getPrefSeqNo()) ? "" : addrData.getPrefSeqNo());
    }
  }

  protected void handleDataDefaults(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData) {
    messageHash.put("SourceCode", "EF0");
    messageHash.put("CEdivision", "2");

    String sbo = messageHash.get("SBO");
    messageHash.put("IBO", sbo);
    messageHash.put("MarketingResponseCode", "3");

    String custType = cmrData.getCustSubGrp();
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBP".equals(custType)) {
      messageHash.put("MarketingResponseCode", "5");
      messageHash.put("ARemark", "YES");
    } else {
      messageHash.put("ARemark", "");
    }

    messageHash.put("CustomerType", "");
    messageHash.put("LangCode", "5");
    messageHash.put("CICode", "3");

    String lang = cmrData.getCustPrefLang();
    if (!StringUtils.isEmpty(lang) && "c".equalsIgnoreCase(lang)) {
      messageHash.put("CustomerLanguage", "2");
    } else if (!StringUtils.isEmpty(lang) && "E".equalsIgnoreCase(lang)) {
      messageHash.put("CustomerLanguage", "3");
    } else {
      messageHash.put("CustomerLanguage", "1");
    }

    boolean create = "C".equals(handler.adminData.getReqType());
    if (create) {
      messageHash.put("AccAdBo", "Y60382");
      SimpleDateFormat sdf = new SimpleDateFormat("yy");
      messageHash.put("AccAdmDSC", "A" + sdf.format(new Date()));
      messageHash.put("NationalCust", crossBorder ? "N" : "Y");
    } else {
      messageHash.put("AccAdBo", "");
      messageHash.put("AccAdmDSC", "");
      messageHash.put("NationalCust", "");
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
    return SystemLocation.SPAIN;
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
    return !"ES".equals(addr.getLandCntry());
  }

  /**
   * Defect 1481070: Checks if Billing is a crossborder for VAT dummy logic
   * 
   */
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
      String custType = data.getCustSubGrp();
      if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBP".equals(custType)) {
        legacyCust.setMrcCd("5");
        legacyCust.setAuthRemarketerInd("Y");
      } else {
        legacyCust.setMrcCd("3");
      }

      legacyCust.setSalesGroupRep(data.getRepTeamMemberNo() != null ? data.getRepTeamMemberNo() : "");
      if (!StringUtils.isEmpty(data.getIsuCd()) && ("5K".equals(data.getIsuCd()) || "3T".equals(data.getIsuCd()))) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      } else {
        legacyCust.setIsuCd((!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
            + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : ""));
      }
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      for (Addr addr : cmrObjects.getAddresses()) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          if (!StringUtils.isEmpty(addr.getCustPhone())) {
            legacyCust.setTelNoOrVat(addr.getCustPhone());
          }
          landedCntry = addr.getLandCntry();
          break;
        }
      }

      legacyCust.setSalesGroupRep(data.getRepTeamMemberNo() != null ? data.getRepTeamMemberNo() : "");

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

      // Mukesh : Story 1597678: Support temporary reactivation requests due to
      // Embargo Code handling
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
      if (!StringUtils.isEmpty(data.getIsuCd()) && ("5K".equals(data.getIsuCd()) || "3T".equals(data.getIsuCd()))) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      } else {
        legacyCust.setIsuCd((!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
            + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : ""));
      }
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
      if ("GR".equals(landedCntry)) {
        legacyCust.setVat("EL" + dummyHandler.cmrData.getVat().substring(2));
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

    if (StringUtils.isNotEmpty(data.getCollectionCd())) {
      legacyCust.setCollectionCd("");
    }

    legacyCust.setBankBranchNo(data.getIbmDeptCostCenter() != null ? data.getIbmDeptCostCenter() : "");
    // CREATCMR-4293
    if (!StringUtils.isEmpty(data.getIsuCd())) {
      if (StringUtils.isEmpty(data.getClientTier())) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      }
    }

    if (!StringUtils.isEmpty(data.getIsuCd()) && ("5K".equals(data.getIsuCd()) || "3T".equals(data.getIsuCd()))) {
      legacyCust.setIsuCd(data.getIsuCd() + "7");
    } else {
      legacyCust.setIsuCd(
          (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "") + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : ""));
    }
  }

  private void blankOrdBlockFromData(EntityManager entityManager, Data data) {
    data.setOrdBlk("");
    entityManager.merge(data);
    entityManager.flush();
  }

  private void resetOrdBlockToData(EntityManager entityManager, Data data) {
    data.setOrdBlk("88");
    data.setEmbargoCd("E");
    entityManager.merge(data);
    entityManager.flush();
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    formatAddressLines(dummyHandler);
    if ("N".equals(currAddr.getImportInd()) && MQMsgConstants.ADDR_ZD01.equals(currAddr.getId().getAddrType())) {
      // preferred sequence no for additional shipping
      // Mukesh:Story 1698123
      legacyAddr.getId().setAddrNo(StringUtils.isEmpty(currAddr.getPrefSeqNo()) ? legacyAddr.getId().getAddrNo() : currAddr.getPrefSeqNo());
    }
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String isicCd = data.getIsicCd();
    String sbo = data.getSalesBusOffCd();
    LOG.debug("Set max and min range of cmrNo..");
    if ((sbo != null && "1080810".equals(sbo)) && (isicCd != null && "0000".equals(isicCd))) {
      generateCMRNoObj.setMin(997000);
      generateCMRNoObj.setMax(997999);
    }
    if ((sbo != null && "0020200".equals(sbo)) && (isicCd != null && "0000".equals(isicCd))) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(996999);
    }
  }

  public HashMap<String, String> handlePostalCodeChanges(String postalCode) {
    HashMap<String, String> changes = new HashMap<String, String>();

    if (postalCode != null) {
      String startChars = postalCode.substring(0, 2);
      HashMap<String, String> locEsPosCuslocEbo = (HashMap<String, String>) CmrConstants.ES_POSTAL_CUSLOC_EBO_MAP.get(startChars);
      changes = locEsPosCuslocEbo;
    }

    return changes;
  }

  @Override
  public String handleVatMassUpdateChanges(String newStartingLetter, String legacyVat) {
    String fullVat = "";
    String locLegacyVat = !StringUtils.isEmpty(legacyVat) && legacyVat.length() > 2 ? legacyVat.replace(legacyVat.substring(2, 3), newStartingLetter)
        : "";

    if (!StringUtils.isEmpty(locLegacyVat)) {
      // if what is parsed is not empty, return with new replaced value
      fullVat = locLegacyVat;
    } else {
      // if what is parsed is coming up as empty, use the param as defaulted
      // value
      fullVat = legacyVat;
    }

    return fullVat;
  }

  @Override
  public boolean isCrossBorderForMass(MassUpdtAddr addr, CmrtAddr legacyAddr) {
    boolean isCrossBorder = false;
    if (!StringUtils.isEmpty(addr.getLandCntry()) && !SpainTransformer.DEFAULT_LANDED_COUNTRY.equals(addr.getLandCntry())) {
      isCrossBorder = true;
    } else if (!StringUtils.isEmpty(legacyAddr.getAddrLine5()) && legacyAddr.getAddrLine5().length() == 2) {
      isCrossBorder = true;
    }
    return isCrossBorder;
  }

  @Override
  public void handlePostCdSpecialLogic(CmrtCust cust, Data data, String postcd, EntityManager entityManager) {

    if (postcd != null) {
      String startChar = postcd.substring(0, 2);

      HashMap<String, String> locEsPosCuslocEbo = (HashMap<String, String>) CmrConstants.ES_POSTAL_CUSLOC_EBO_MAP.get(startChar);

      String cusLocNum = locEsPosCuslocEbo.get("cusLocNum");
      String cebo = locEsPosCuslocEbo.get("ebo");

      if (!StringUtils.isEmpty(cebo)) {
        cust.setCeBo(cebo);
      }

      if (!StringUtils.isEmpty(cusLocNum)) {
        cust.setLocNo(cusLocNum);
      }
    }
  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust cust, CMRRequestContainer cmrObjects, MassUpdtData muData) {
    // default mapping for DATA and CMRTCUST
    LOG.debug("Mapping default Data values..");

    if (!StringUtils.isBlank(muData.getAbbrevNm())) {
      cust.setAbbrevNm(muData.getAbbrevNm());
    }

    if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
      cust.setAbbrevLocn(muData.getAbbrevLocn());
    }

    if (!StringUtils.isBlank(muData.getModeOfPayment())) {
      if ("@".equals(muData.getModeOfPayment().trim())) {
        cust.setModeOfPayment("");
      } else {
        cust.setModeOfPayment(muData.getModeOfPayment());
      }
    }

    if (StringUtils.isNotBlank(muData.getClientTier()) && ("5K".equals(muData.getIsuCd()) || "3T".equals(muData.getIsuCd()))) {
      cust.setIsuCd(muData.getIsuCd() + "7");
    } else {
      String isuCd = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "");
      if (isuCd != null) {
        cust.setIsuCd(isuCd);
      }
    }

    if (!StringUtils.isBlank(muData.getSpecialTaxCd())) {
      if ("@".equals(muData.getSpecialTaxCd().trim())) {
        cust.setTaxCd("");
      } else {
        cust.setTaxCd(muData.getSpecialTaxCd());
      }
    }

    if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
      cust.setSalesRepNo(muData.getRepTeamMemberNo());
      cust.setSalesGroupRep(muData.getRepTeamMemberNo());
    }

    if (!StringUtils.isBlank(muData.getCustNm2())) {
      cust.setCeBo(muData.getCustNm2());
    }

    if (!StringUtils.isBlank(muData.getCollectionCd())) {
      if ("@".equals(muData.getCollectionCd().trim())) {
        // cust.setCollectionCd("");
        cust.setDistrictCd("");
      } else {
        cust.setDistrictCd(muData.getCollectionCd());
      }
    }

    if (!StringUtils.isBlank(muData.getIsicCd())) {
      cust.setIsicCd(muData.getIsicCd());
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      String newVat = handleVatMassUpdateChanges(muData.getVat(), cust.getVat());
      cust.setVat(newVat);
    }

    if (!StringUtils.isBlank(muData.getCustNm1())) {
      cust.setSbo(muData.getCustNm1());
      cust.setIbo(muData.getCustNm1());
    }

    if (!StringUtils.isBlank(muData.getInacCd())) {
      if ("@".equals(muData.getInacCd().trim())) {
        cust.setInacCd("");
      } else {
        cust.setInacCd(muData.getInacCd());
      }
    }

    if (!StringUtils.isBlank(muData.getMiscBillCd())) {
      if ("@".equals(muData.getMiscBillCd().trim())) {
        cust.setEmbargoCd("");
      } else {
        cust.setEmbargoCd(muData.getMiscBillCd());
      }
    }

    if (!StringUtils.isBlank(muData.getOutCityLimit())) {
      if ("@".equals(muData.getOutCityLimit().trim())) {
        cust.setMailingCond("");
      } else {
        cust.setMailingCond(muData.getOutCityLimit());
      }
    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      if ("@".equals(muData.getEnterprise().trim())) {
        cust.setEnterpriseNo("");
      } else {
        cust.setEnterpriseNo(muData.getEnterprise());
      }
    }

    if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
      String subInd = muData.getSubIndustryCd();
      cust.setImsCd(subInd);
      // Defect 1776715: Fix for Economic code
      String firstChar = String.valueOf(subInd.charAt(0));
      StringBuilder builder = new StringBuilder();
      builder.append(firstChar);
      builder.append(subInd);
      LOG.debug("***Auto setting Economic code as > " + builder.toString());
      cust.setEconomicCd(builder.toString());
      if (StringUtils.isNotBlank(muData.getClientTier()) && ("5K".equals(muData.getIsuCd()) || "3T".equals(muData.getIsuCd()))) {
        cust.setIsuCd(muData.getIsuCd() + "7");
      } else {
        String isuCd = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "");
        if (isuCd != null) {
          cust.setIsuCd(isuCd);
        }
      }
    }

    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    // cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());

    String isuClientTier;
    if (!StringUtils.isEmpty(muData.getIsuCd()) && ("5K".equals(muData.getIsuCd()) || "3T".equals(muData.getIsuCd()))) {
      cust.setIsuCd(muData.getIsuCd() + "7");
    } else {
      isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
          + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
      if (isuClientTier != null && isuClientTier.contains("@")) {
        cust.setIsuCd("7");
      } else if (isuClientTier != null && isuClientTier.length() == 3) {
        cust.setIsuCd(isuClientTier);
      }
    }

  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr addr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {
    CmrtAddr legacyFiscalAddr = null;

    if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(addr.getId().getAddrType())) {
      legacyFiscalAddr = LegacyDirectUtil.getLegacyFiscalAddr(entityManager, cntry, addr.getCmrNo(), true);
      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setForUpdate(true);
      }
    }

    legacyAddr.setForUpdate(true);

    if (!StringUtils.isBlank(addr.getCustNm1())) {
      legacyAddr.setAddrLine1(addr.getCustNm1());

      if (legacyFiscalAddr != null) {
        String prefix = !StringUtils.isEmpty(legacyFiscalAddr.getAddrLine1()) ? legacyFiscalAddr.getAddrLine1().substring(0, 1) : "";
        legacyFiscalAddr.setAddrLine1(prefix + addr.getCustNm1());
      }

    }

    if (!StringUtils.isBlank(addr.getCustNm2())) {
      legacyAddr.setAddrLine2(addr.getCustNm2());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setAddrLine2("CL" + addr.getCustNm2());
      }
    }

    if (!StringUtils.isBlank(addr.getAddrTxt())) {
      legacyAddr.setStreet(addr.getAddrTxt());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setStreet(addr.getAddrTxt());
      }
    }

    if (!StringUtils.isBlank(addr.getAddrTxt2())) {
      legacyAddr.setStreetNo(addr.getAddrTxt2());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setStreetNo(addr.getAddrTxt2());
      }
    }

    if (!StringUtils.isBlank(addr.getCity1())) {
      legacyAddr.setCity(addr.getCity1());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setCity(addr.getCity1());
      }
    }

    if (!StringUtils.isBlank(addr.getDept())) {
      legacyAddr.setContact(addr.getDept());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setContact(addr.getDept());
      }
    }

    if (!StringUtils.isBlank(addr.getPostCd())) {
      legacyAddr.setZipCode(addr.getPostCd());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setZipCode(addr.getPostCd());
      }

      if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(addr.getId().getAddrType()) && isCrossBorderForMass(addr, legacyAddr)) {
        handlePostCdSpecialLogic(cust, data, addr.getPostCd(), entityManager);
      }
    }

    String poBox = addr.getPoBox();
    if (!StringUtils.isEmpty(poBox) && !poBox.toUpperCase().startsWith("APTO")) {
      poBox = " APTO " + poBox;
      legacyAddr.setPoBox(addr.getPoBox());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setPoBox(addr.getPoBox());
      }
    }

    boolean crossBorder = false;
    if (!StringUtils.isEmpty(addr.getLandCntry()) && !"ES".equals(addr.getLandCntry())) {
      crossBorder = true;
    } else {
      crossBorder = false;
    }

    if (!StringUtils.isBlank(addr.getLandCntry()) && crossBorder) {
      legacyAddr.setAddrLine5(LandedCountryMap.getCountryName(addr.getLandCntry()));

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setPoBox(LandedCountryMap.getCountryName(addr.getLandCntry()));
      }
    }

    if (!StringUtils.isBlank(addr.getCounty()) && !crossBorder) {
      legacyAddr.setAddrLine5(addr.getCounty());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setPoBox(addr.getCounty());
      }
    }

    formatMassUpdateAddressLines(entityManager, legacyAddr, addr, false);
    legacyObjects.addAddress(legacyAddr);

    if (legacyFiscalAddr != null) {
      formatMassUpdateAddressLines(entityManager, legacyFiscalAddr, addr, true);
      legacyObjects.addAddress(legacyFiscalAddr);
    }

  }

  @Override
  public void formatMassUpdateAddressLines(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr massUpdtAddr, boolean isFAddr) {
    boolean crossBorder = isCrossBorderForMass(massUpdtAddr, legacyAddr);
    String addrKey = getAddressKey(massUpdtAddr.getId().getAddrType());
    Map<String, String> messageHash = new LinkedHashMap<String, String>();

    messageHash.put("SourceCode", "EF0");
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

    // fiscal address
    if (isFAddr) {
      line1 = legacyAddr.getAddrLine1();
      line2 = "CL " + legacyAddr.getStreet();

      StringBuilder street = new StringBuilder();
      line2 = StringUtils.replace(line2, " - ", "-");
      line2 = StringUtils.replace(line2, "- ", "-");
      line2 = StringUtils.replace(line2, " -", "-");
      String[] parts = line2.split("[^A-Za-zÁáÉéÍíÓóÚúÑñ.0-9]");
      for (String part : parts) {
        if (!StringUtils.isEmpty(part) && (StringUtils.isNumeric(part) || (part.matches(".*\\d{1}.*") && part.contains("-")))) {
          line3 = part;
        } else if (!StringUtils.isEmpty(part)) {
          street.append(street.length() > 0 ? " " : "");
          street.append(part);
        }
      }

      line2 = street.toString();
      if (StringUtils.isEmpty(legacyAddr.getStreet()) && !StringUtils.isEmpty(legacyAddr.getPoBox())) {
        line2 = "CLAPTO " + legacyAddr.getPoBox().replaceAll("[^\\d]", "");
      }
      line3 = StringUtils.leftPad(line3, 5, '0');

      line4 = legacyAddr.getCity();
      if (crossBorder) {
        line5 = "88888";
      } else {
        line5 = legacyAddr.getZipCode();
      }
    } else {
      line1 = legacyAddr.getAddrLine1();
      line2 = legacyAddr.getAddrLine2();

      if (StringUtils.isEmpty(line2) && crossBorder) {
        if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
          line2 = "ATT " + line2;
        }
        if (line2.length() > 30) {
          line2 = line2.substring(0, 30);
        }
      }

      if (StringUtils.isEmpty(line2)) {
        line2 = legacyAddr.getStreetNo();
      }

      line3 = legacyAddr.getStreet() != null ? legacyAddr.getStreet().trim() : "";

      String poBox = !StringUtils.isEmpty(legacyAddr.getPoBox()) ? legacyAddr.getPoBox() : "";
      if (!StringUtils.isEmpty(poBox) && !poBox.toUpperCase().startsWith("APTO")) {
        poBox = " APTO " + poBox;
      }

      line3 = (StringUtils.isEmpty(line3) ? poBox : line3.trim()) + poBox;
      line4 = (legacyAddr.getZipCode() != null ? legacyAddr.getZipCode().trim() : "") + " "
          + (legacyAddr.getCity() != null ? legacyAddr.getCity().trim() : "");

      if (!crossBorder) {
        line5 = !StringUtils.isEmpty(legacyAddr.getAddrLine5()) ? legacyAddr.getAddrLine5().trim() : "";
        if (!StringUtils.isEmpty(line5) && !line5.toUpperCase().startsWith("ATT ") && !line5.toUpperCase().startsWith("ATT:")) {
          // Defect 1740670: SPAIN - attention person - ATT
          line5 = "ATT " + line5;
        }
        if (line5.length() > 30) {
          line5 = line5.substring(0, 30);
        }
      } else {
        if (!StringUtils.isEmpty(massUpdtAddr.getLandCntry())) {
          line5 = massUpdtAddr.getLandCntry();
        } else {
          line5 = legacyAddr.getAddrLine5();
        }
      }

      line6 = "";
      if ("Y".equalsIgnoreCase(legacyAddr.getIsAddrUseShipping())) {
        // DTN: Commented because we are not passing phone numbers on the
        // template
        // line6 = legacyAddr.getAddrPhone();
      }
    }

    String[] lines = new String[] { (line1 != null ? line1.trim() : ""), (line2 != null ? line2.trim() : ""), (line3 != null ? line3.trim() : ""),
        (line4 != null ? line4.trim() : ""), (line5 != null ? line5.trim() : "") };
    int lineNo = 1;
    LOG.debug("Lines: " + (line1 != null ? line1.trim() : "") + " | " + (line2 != null ? line2.trim() : "") + " | "
        + (line3 != null ? line3.trim() : "") + " | " + (line4 != null ? line4.trim() : "") + " | " + (line5 != null ? line5.trim() : ""));

    for (String line : lines) {
      messageHash.put(getAddressKey((massUpdtAddr.getId().getAddrType()) + "Address" + lineNo).toString(), line);
      lineNo++;
    }

    legacyAddr.setAddrLine1(line1 != null ? line1.trim() : "");
    legacyAddr.setAddrLine2(line2 != null ? line2.trim() : "");
    legacyAddr.setAddrLine3(line3 != null ? line3.trim() : "");
    legacyAddr.setAddrLine4(line4 != null ? line4.trim() : "");
    legacyAddr.setAddrLine5(line5 != null ? line5.trim() : "");

  }

}
