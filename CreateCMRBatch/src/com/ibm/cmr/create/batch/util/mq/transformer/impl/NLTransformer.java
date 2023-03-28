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

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
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
import com.ibm.cio.cmr.request.util.geo.impl.NLHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * @author George
 * 
 */
public class NLTransformer extends EMEATransformer {

  private static final Logger LOG = Logger.getLogger(NLTransformer.class);

  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02", "ZP02" };
  protected boolean duplicateRecordFound = false;
  protected Map<String, String> dupCMRValues = new HashMap<String, String>();
  protected List<String> dupShippingSequences = null;
  private static final List<String> SUB_TYPES_INTERNAL = Arrays.asList("INTER");

  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode" };

  public static String DEFAULT_LANDED_COUNTRY = "NL";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";
  // private static final String DEFAULT_CLEAR_CHAR = "@";

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
  private static final String[] BELUX_GBMSBM_COUNTRIES = { "788" };
  private static final String SCENARIO_TYPE_SBM = "SBM";
  private static final String SCENARIO_TYPE_GBM = "GBM";
  // private static final String GULF_DUPLICATE = "GULF";
  protected static Map<String, String> SOURCE_CODE_MAP = new HashMap<String, String>();

  static {
    SOURCE_CODE_MAP.put(SystemLocation.NETHERLANDS, "EFO");
  }

  public NLTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

  }

  protected boolean isCrossBorder(Addr addr) {
    String cd = NLHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    return cd != null && !cd.equals(addr.getLandCntry());
  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Bill-To";
    case "ZS01":
      return "Install-At";
    case "ZD01":
      return "Ship-To";
    case "ZI01":
      return "EPL";
    case "ZP02":
      return "IGF Bill-To";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Bill-To";
    case "ZS01":
      return "Install-At";
    case "ZD01":
      return "Ship-To";
    case "ZI01":
      return "EPL";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return getCmrIssuingCntry();
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "1";
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZP02:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  public String getAddressUseByType(String addrType) {
    switch (addrType) {
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  @Override
  public boolean shouldCompleteProcess(EntityManager entityManager, MQMessageHandler handler, String responseStatus, boolean fromUpdateFlow) {
    try {
      if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
        return true;
      }

      LOG.debug("No need to create duplicates. Completing request.");
      return true;

    } catch (Exception e) {
      LOG.error("Error in completing dual process request. Skipping dual process and completing request", e);
      return true;
    }
  }

  @Override
  public boolean shouldSendAddress(EntityManager entityManager, MQMessageHandler handler, Addr nextAddr) {
    if (nextAddr == null) {
      return false;
    }
    Data cmrData = handler.cmrData;
    if (cmrData != null && StringUtils.isEmpty(cmrData.getTaxCd2()) && "ZKVK".equals(nextAddr.getId().getAddrType())) {
      // 1675871 - do not send VAT/KVK if VAT/KVK is empty
      return false;
    }
    if (cmrData != null && StringUtils.isEmpty(cmrData.getVat()) && "ZVAT".equals(nextAddr.getId().getAddrType())) {
      // 1675871 - do not send VAT/KVK if VAT/KVK is empty
      return false;
    }
    return true;
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
    // handleDataDefaults(handler, messageHash, cmrData, crossBorder,
    // addrData);

    messageHash.put("SourceCode", "FOU");
    messageHash.put("LangCode", "1");
    messageHash.put("CEdivision", "2");
    messageHash.put("MarketingResponseCode", "3");
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(cmrData.getCustSubGrp())) {
      messageHash.put("MarketingResponseCode", "5");
      messageHash.put("ARemark", "YES");
    } else {
      messageHash.put("ARemark", "");
    }

    if (update) {
      messageHash.put("CollectionCode", cmrData.getCollectionCd());
    } else {
      messageHash.put("CollectionCode", "");
    }
    messageHash.put("EconomicCode", cmrData.getEconomicCd());
    messageHash.put("InvNumber", "");
    messageHash.put("TaxCode", cmrData.getTaxCd1());
    messageHash.put("EnterpriseNo", cmrData.getEnterprise());

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
    messageHash.put("EmbargoCode", !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "");

    // add the CustomerType

    String custType = cmrData.getCustSubGrp();
    if (!StringUtils.isBlank(custType)) {
      if (custType.contains("BUS") || custType.contains("BP")) {
        messageHash.put("MarketingResponseCode", "5");
        messageHash.put("ARemark", "YES");
        messageHash.put("CustomerType", "BP");
      } else if ("GOVRN".equals(custType)) {
        messageHash.put("CustomerType", "G");
      } else if (custType.contains("INT")) {
        messageHash.put("CustomerType", "91");
      } else {
        messageHash.put("CustomerType", "");
      }
    } else {
      messageHash.put("CustomerType", "");
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

    String addrKey = getAddressKey(addrData.getId().getAddrType());
    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    messageHash.put("SourceCode", "FOU");
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    LOG.debug("Handling  Data for " + addrData.getCustNm1());
    // <XXXAddress1> -> Customer Name
    // <XXXAddress2> -> Name Con't
    // <XXXAddress3> -> Title/A and First name/c and Last name
    // <XXXAddress4> -> Street/F and Street number/G or PO BOX
    // <XXXAddress5> -> Postal Code/H and City/I
    // <XXXAddress6> -> Country

    // customer name
    String line1 = addrData.getCustNm1();

    // name 2, as nickname
    String line2 = "";
    if (!StringUtils.isBlank(addrData.getCustNm2())) {
      line2 += (line2.length() > 0 ? " " : "") + addrData.getCustNm2();
    }

    // Title/A and First name/c and Last name/B
    String line3 = "";
    if (!StringUtils.isBlank(addrData.getDept())) {
      line3 += (line3.length() > 0 ? " " : "") + addrData.getDept();
    }
    if (!StringUtils.isBlank(addrData.getCustNm3())) {
      line3 += (line3.length() > 0 ? " " : "") + addrData.getCustNm3();
    }
    if (!StringUtils.isBlank(addrData.getCustNm4())) {
      line3 += (line3.length() > 0 ? " " : "") + addrData.getCustNm4();
    }

    // Street/F and Street number/G or PO BOX/J
    String line4 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 += (line4.length() > 0 ? " " : "") + addrData.getAddrTxt();
    }
    if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
      line4 += (line4.length() > 0 ? " " : "") + addrData.getAddrTxt2();
    }
    if (line4.length() == 0) {
      if (!StringUtils.isBlank(addrData.getPoBox())) {
        line4 += (line4.length() > 0 ? " " : "") + addrData.getPoBox();
      }
    }
    // Postal Code/H and City/I
    String line5 = "";

    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line5 += (line5.length() > 0 ? " " : "") + addrData.getPostCd();
    }
    if (!StringUtils.isBlank(addrData.getCity1())) {
      line5 += (line5.length() > 0 ? " " : "") + addrData.getCity1();
    }

    line5 = line5.trim();

    String countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());

    // country
    String line6 = countryName;

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
    messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Country", countryName);

    // Shipping, installing, software address
    String addressType = getTargetAddressType(addrData.getId().getAddrType());

    if (addressType.equalsIgnoreCase("Address in local language")) {

      Map<String, String> addressDataMap = new HashMap<String, String>();

      addressDataMap.put("addrTxt", addrData.getAddrTxt());
      addressDataMap.put("addrTxt2", addrData.getAddrTxt2());
      addressDataMap.put("bldg", addrData.getBldg());
      addressDataMap.put("city1", addrData.getCity1());
      addressDataMap.put("city2", addrData.getCity2());
      addressDataMap.put("county", addrData.getCounty());
      addressDataMap.put("countyName", addrData.getCountyName());
      addressDataMap.put("custNm1", addrData.getCustNm1());
      addressDataMap.put("custNm2", addrData.getCustNm2());
      addressDataMap.put("custNm3", addrData.getCustNm3());
      addressDataMap.put("custNm4", addrData.getCustNm4());
      addressDataMap.put("dept", addrData.getDept());
      addressDataMap.put("division", addrData.getDivn());
      addressDataMap.put("floor", addrData.getFloor());
      addressDataMap.put("office", addrData.getOffice());
      addressDataMap.put("poBox", addrData.getPoBox());
      addressDataMap.put("poBoxCity", addrData.getPoBoxCity());
      addressDataMap.put("poBoxPostCd", addrData.getPoBoxPostCd());
      addressDataMap.put("postCd", addrData.getPostCd());
      addressDataMap.put("stateProv", addrData.getStateProv());
      addressDataMap.put("stdCityNm", addrData.getStdCityNm());
      addressDataMap.put("taxOffice", addrData.getTaxOffice());

      if (!(StringUtils.isEmpty(addressDataMap.get("addrTxt"))) && !(addressDataMap.get("addrTxt").equals(handler.addrData.getAddrTxt()))) {
        handler.addrData.setAddrTxt(addressDataMap.get("addrTxt"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("addrTxt2"))) && !(addressDataMap.get("addrTxt2").equals(handler.addrData.getAddrTxt2()))) {
        handler.addrData.setAddrTxt2(addressDataMap.get("addrTxt2"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("bldg"))) && !(addressDataMap.get("bldg").equals(handler.addrData.getBldg()))) {
        handler.addrData.setBldg(addressDataMap.get("bldg"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("city1"))) && !(addressDataMap.get("city1").equals(handler.addrData.getCity1()))) {
        handler.addrData.setCity1(addressDataMap.get("city1"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("city2"))) && !(addressDataMap.get("city2").equals(handler.addrData.getCity2()))) {
        handler.addrData.setCity2(addressDataMap.get("city2"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("county"))) && !(addressDataMap.get("county").equals(handler.addrData.getCounty()))) {
        handler.addrData.setCounty(addressDataMap.get("county"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("countyName"))) && !(addressDataMap.get("countyName").equals(handler.addrData.getCountyName()))) {
        handler.addrData.setCountyName(addressDataMap.get("countyName"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("custNm1"))) && !(addressDataMap.get("custNm1").equals(handler.addrData.getCustNm1()))) {
        handler.addrData.setCustNm1(addressDataMap.get("custNm1"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("custNm2"))) && !(addressDataMap.get("custNm2").equals(handler.addrData.getCustNm2()))) {
        handler.addrData.setCustNm2(addressDataMap.get("custNm2"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("custNm3"))) && !(addressDataMap.get("custNm3").equals(handler.addrData.getCustNm3()))) {
        handler.addrData.setCustNm3(addressDataMap.get("custNm3"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("custNm4"))) && !(addressDataMap.get("custNm4").equals(handler.addrData.getCustNm4()))) {
        handler.addrData.setCustNm4(addressDataMap.get("custNm4"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("dept"))) && !(addressDataMap.get("dept").equals(handler.addrData.getDept()))) {
        handler.addrData.setDept(addressDataMap.get("dept"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("division"))) && !(addressDataMap.get("division").equals(handler.addrData.getDivn()))) {
        handler.addrData.setDept(addressDataMap.get("division"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("floor"))) && !(addressDataMap.get("floor").equals(handler.addrData.getFloor()))) {
        handler.addrData.setFloor(addressDataMap.get("floor"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("office"))) && !(addressDataMap.get("office").equals(handler.addrData.getOffice()))) {
        handler.addrData.setOffice(addressDataMap.get("office"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("poBox"))) && !(addressDataMap.get("poBox").equals(handler.addrData.getPoBox()))) {
        handler.addrData.setPoBox(addressDataMap.get("poBox"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("poBoxCity"))) && !(addressDataMap.get("poBoxCity").equals(handler.addrData.getPoBoxCity()))) {
        handler.addrData.setPoBoxCity(addressDataMap.get("poBoxCity"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("poBoxPostCd")))
          && !(addressDataMap.get("poBoxPostCd").equals(handler.addrData.getPoBoxPostCd()))) {
        handler.addrData.setPoBoxPostCd(addressDataMap.get("poBoxPostCd"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("postCd"))) && !(addressDataMap.get("postCd").equals(handler.addrData.getPostCd()))) {
        handler.addrData.setPostCd(addressDataMap.get("postCd"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("stateProv"))) && !(addressDataMap.get("stateProv").equals(handler.addrData.getStateProv()))) {
        handler.addrData.setStateProv(addressDataMap.get("stateProv"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("stdCityNm"))) && !(addressDataMap.get("stdCityNm").equals(handler.addrData.getStdCityNm()))) {
        handler.addrData.setStdCityNm(addressDataMap.get("stdCityNm"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("taxOffice"))) && !(addressDataMap.get("taxOffice").equals(handler.addrData.getTaxOffice()))) {
        handler.addrData.setTaxOffice(addressDataMap.get("taxOffice"));
      }
    }
  }

  public void setDefaultLandedCountry(Data data) {

    if ("788".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "NL";
    }
  }

  @Override
  public boolean isCrossBorderForMass(MassUpdtAddr addr, CmrtAddr legacyAddr) {
    boolean isCrossBorder = false;
    if (!StringUtils.isEmpty(addr.getLandCntry()) && !DEFAULT_LANDED_COUNTRY.equals(addr.getLandCntry())) {
      isCrossBorder = true;
    } else if (!StringUtils.isEmpty(legacyAddr.getAddrLine5()) && legacyAddr.getAddrLine5().length() == 2) {
      isCrossBorder = true;
    }
    return isCrossBorder;
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {

    formatAddressLinesLD(dummyHandler, legacyAddr);
  }

  private void formatAddressLinesLD(MQMessageHandler handler, CmrtAddr legacyAddr) {

    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());
    boolean crossBorder = isCrossBorder(addrData);

    LOG.debug("Legacy Direct -Handling Address for " + (update ? "update" : "create") + " request.");

    String addrType = addrData.getId().getAddrType();
    String phone = "";
    String addrLineT = "";

    LOG.trace("Handling " + (update ? "update" : "create") + " request.");

    LOG.debug("Handling  Data for " + addrData.getCustNm1());
    // https://jsw.ibm.com/browse/CREATCMR-990 2021-1-29
    // <XXXAddress1> -> Customer Name 1
    // <XXXAddress2> -> Customer Name 2 []
    // <XXXAddress3> -> Customer name3 + Attention Person + PO BOX []
    // <XXXAddress4> -> Street Address []
    // <XXXAddress5> -> Postal Code + City <>
    // <XXXAddress6> -> Country

    // customer name
    String line1 = addrData.getCustNm1();

    // name 2, as nickname
    String line2 = "";
    if (!StringUtils.isBlank(addrData.getCustNm2())) {
      line2 += (line2.length() > 0 ? " " : "") + addrData.getCustNm2();
    }

    // line 3 = Customer name3 + Attention Person + PO BOX
    String line3 = "";

    if (!StringUtils.isBlank(addrData.getCustNm3())) {
      line3 += (line3.length() > 0 ? " " : "") + addrData.getCustNm3();
    }

    if (StringUtils.isEmpty(line3)) {
      if (!StringUtils.isBlank(addrData.getCustNm4())) {
        line3 += (line3.length() > 0 ? " " : "") + "ATT " + addrData.getCustNm4();
      }
    }
    if (StringUtils.isEmpty(line3)) {
      if (!StringUtils.isBlank(addrData.getPoBox())) {
        line3 += (line3.length() > 0 ? " " : "") + "PO BOX " + addrData.getPoBox();
      }
    }

    // Street Address
    String line4 = "";
    // Postal Code + City
    String line5 = "";
    // country
    String line6 = "";

    String countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());
    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 += (line4.length() > 0 ? " " : "") + addrData.getAddrTxt();
    }

    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line5 += (line5.length() > 0 ? " " : "") + addrData.getPostCd();
    }
    if (!StringUtils.isBlank(addrData.getCity1())) {
      line5 += (line5.length() > 0 ? " " : "") + addrData.getCity1();
    }

    line5 = line5.trim();
    if (StringUtils.isEmpty(line2)) {
      if (StringUtils.isEmpty(line3) && !StringUtils.isEmpty(line4) && !StringUtils.isEmpty(line5)) {
        line2 = line4;
        line3 = line5;
        line4 = "";
        line5 = "";
      } else if (!StringUtils.isEmpty(line3) && StringUtils.isEmpty(line4) && !StringUtils.isEmpty(line5)) {
        line2 = line3;
        line3 = line5;
        line5 = "";
      } else if (!StringUtils.isEmpty(line3) && !StringUtils.isEmpty(line4) && !StringUtils.isEmpty(line5)) {
        line2 = line3;
        line3 = line4;
        line4 = line5;
        line5 = "";
      }
    } else if (!StringUtils.isEmpty(line2)) {
      if (StringUtils.isEmpty(line3) && !StringUtils.isEmpty(line4) && !StringUtils.isEmpty(line5)) {
        line3 = line4;
        line4 = line5;
        line5 = "";
      } else if (StringUtils.isEmpty(line3) && !StringUtils.isEmpty(line4) && StringUtils.isEmpty(line5)) {
        line3 = line4;
        line4 = "";
      } else if (!StringUtils.isEmpty(line3) && StringUtils.isEmpty(line4) && !StringUtils.isEmpty(line5)) {
        line4 = line5;
        line5 = "";
      }
    }

    if (!crossBorder) {
      line6 = "";
    } else if (crossBorder) {
      // country
      line6 = countryName;
    }
    if (!StringUtils.isEmpty(line6) && StringUtils.isEmpty(line5)) {
      line5 = line6;
      line6 = "";
      if (StringUtils.isEmpty(line4)) {
        line4 = line5;
        line5 = "";
      }
    }

    if (!StringUtils.isBlank(addrData.getTaxOffice())) {
      addrLineT = addrData.getTaxOffice();
    } else {
      addrLineT = "";
    }

    legacyAddr.setItCompanyProvCd("");

    legacyAddr.setAddrLine1(line1);
    legacyAddr.setAddrLine2(line2);
    legacyAddr.setAddrLine3(line3);
    legacyAddr.setAddrLine4(line4);
    legacyAddr.setAddrLine5(line5);
    legacyAddr.setCity("");
    legacyAddr.setZipCode("");
    legacyAddr.setAddrLine6(line6);
    legacyAddr.setStreetNo("");
    legacyAddr.setAddrPhone("");
    legacyAddr.setAddrLineT(addrLineT);
    legacyAddr.setDistrict("");
    legacyAddr.setContact("");
    legacyAddr.setAddrLineU("");

    legacyAddr.setFirstName("");
    legacyAddr.setLastName("");
    legacyAddr.setTitle("");
    legacyAddr.setName("");
    legacyAddr.setStreet("");

    legacyAddr.setPoBox("");
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("_custSubGrp = " + custSubGrp);

    LOG.debug("Set max and min range of cmrNo..");
    // 788 Internal - 99xxxx
    if (SUB_TYPES_INTERNAL.contains(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr addr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {
    if (!StringUtils.isBlank(addr.getCustPhone()) && "ZS01".equalsIgnoreCase(addr.getId().getAddrType())) {
      if ("@".equals(addr.getCustPhone())) {
        cust.setTelNoOrVat("");
      } else {
        cust.setTelNoOrVat(addr.getCustPhone());
      }
    }
    legacyAddr.setForUpdate(true);
    formatMassUpdateAddressLines(entityManager, legacyAddr, addr, false);
    legacyObjects.addAddress(legacyAddr);
  }

  @Override
  public void formatMassUpdateAddressLines(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr massUpdtAddr, boolean isFAddr) {
    LOG.debug("*** START NL formatMassUpdateAddressLines >>>");
    if (LegacyCommonUtil.isCheckDummyUpdate(massUpdtAddr)) {
      return;
    }
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

    // line1 is always customer name
    line1 = massUpdtAddr.getCustNm1();
    line2 = massUpdtAddr.getCustNm2();
    line4 = massUpdtAddr.getAddrTxt();

    line3 = massUpdtAddr.getCustNm3();
    if (!StringUtils.isBlank(massUpdtAddr.getCustNm4())) {
      line3 += (line3.length() > 0 ? " " : "") + "ATT " + massUpdtAddr.getCustNm4();
    }
    if (!StringUtils.isBlank(massUpdtAddr.getPoBox())) {
      line3 += (line3.length() > 0 ? " " : "") + "PO BOX " + massUpdtAddr.getPoBox();
    }

    if (!StringUtils.isBlank(massUpdtAddr.getPostCd())) {
      line5 += (line5.length() > 0 ? " " : "") + massUpdtAddr.getPostCd();
    }
    if (!StringUtils.isBlank(massUpdtAddr.getCity1())) {
      line5 += (line5.length() > 0 ? " " : "") + massUpdtAddr.getCity1();
    }

    if (crossBorder && !StringUtils.isEmpty(massUpdtAddr.getLandCntry())) {
      line6 = LandedCountryMap.getCountryName(massUpdtAddr.getLandCntry());
    }

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);

    for (String line : lines) {
      if (StringUtils.isNotBlank(line)) {
        messageHash.put(addrKey + "Address" + lineNo, line.trim());
        lineNo++;
      }
    }

    legacyAddr.setAddrLine1(messageHash.get(addrKey + "Address1") != null ? messageHash.get(addrKey + "Address1") : "");
    legacyAddr.setAddrLine2(messageHash.get(addrKey + "Address2") != null ? messageHash.get(addrKey + "Address2") : "");
    legacyAddr.setAddrLine3(messageHash.get(addrKey + "Address3") != null ? messageHash.get(addrKey + "Address3") : "");
    legacyAddr.setAddrLine4(messageHash.get(addrKey + "Address4") != null ? messageHash.get(addrKey + "Address4") : "");
    legacyAddr.setAddrLine5(messageHash.get(addrKey + "Address5") != null ? messageHash.get(addrKey + "Address5") : "");
    legacyAddr.setAddrLine6(messageHash.get(addrKey + "Address6") != null ? messageHash.get(addrKey + "Address6") : "");
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    Addr addrs = dummyHandler.addrData;
    String landedCntry = "";

    setDefaultLandedCountry(data);
    formatDataLines(dummyHandler);

    // CREATCMR-1042 2021-1-29
    legacyCust.setSbo(data.getEngineeringBo() == null ? "" : data.getEngineeringBo());
    legacyCust.setIbo(data.getEngineeringBo() == null ? "" : data.getEngineeringBo());
    legacyCust.setTaxCd(data.getTaxCd1() == null ? "" : data.getTaxCd1());
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {

      legacyCust.setLocNo("");

      // CREATCMR-1042 2021-1-29
      legacyCust.setCeBo("211");

      legacyCust.setAccAdminBo("");
      legacyCust.setCeDivision("");

      // George CREATCMR-371 send value 999999 to DB2, to both REMXA and REMXD
      String sales_Rep_ID = "999999";
      legacyCust.setSalesGroupRep(sales_Rep_ID);
      legacyCust.setSalesRepNo(sales_Rep_ID);
      legacyCust.setDcRepeatAgreement("0");
      legacyCust.setLeasingInd("0");
      legacyCust.setAuthRemarketerInd("0");
      legacyCust.setCeDivision("2");

      legacyCust.setCurrencyCd("");
      legacyCust.setOverseasTerritory("");
      legacyCust.setInvoiceCpyReqd("");
      legacyCust.setCustType("");
      // George CREATCMR-546
      legacyCust.setDeptCd(data.getIbmDeptCostCenter() == null ? "" : data.getIbmDeptCostCenter());

      // legacyCust.setLangCd(data.getCustPrefLang() == null ? "" :
      // data.getCustPrefLang());
      legacyCust.setBankAcctNo("");

      // extract the phone from billing as main phone
      for (Addr addr : cmrObjects.getAddresses()) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          legacyCust.setTelNoOrVat(addr.getCustPhone());
          landedCntry = addr.getLandCntry();
          break;
        }
      }
      legacyCust.setEnterpriseNo(data.getEnterprise() == null ? "" : data.getEnterprise());
      legacyCust.setRealCtyCd("788");
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      legacyCust.setCeBo("211");
      for (Addr addr : cmrObjects.getAddresses()) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          if (!StringUtils.isEmpty(addr.getCustPhone())) {
            legacyCust.setTelNoOrVat(addr.getCustPhone());
          }
          landedCntry = addr.getLandCntry();
          break;
        }
      }

      legacyCust.setRealCtyCd("788");
      legacyCust.setCustType("");

      // CMR-5993
      String cntry = legacyCust.getId().getSofCntryCode().trim();

      legacyCust.setBankAcctNo("");

      String dataEmbargoCd = data.getEmbargoCd();
      if (dataEmbargoCd != null) {
        legacyCust.setEmbargoCd(dataEmbargoCd);
      } else {
        legacyCust.setEmbargoCd("");
      }

      if (!StringUtils.isEmpty(data.getIsuCd())) {
        if (StringUtils.isEmpty(data.getClientTier())) {
          legacyCust.setIsuCd(data.getIsuCd() + "7");
        } else if (!StringUtils.isEmpty(data.getClientTier())) {
          String isuClientTier = (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
              + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : "");
          legacyCust.setIsuCd(isuClientTier);
        }
      }

      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin);
      // CREATCMR-845
      legacyCust.setModeOfPayment(data.getModeOfPayment() == null ? "" : data.getModeOfPayment());

      // permanent removal-single inactivation
      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()) && !"TREC".equals(admin.getReqReason())) {
        if (!StringUtils.isBlank(rdcEmbargoCd) && "E".equals(rdcEmbargoCd)) {
          if (StringUtils.isBlank(data.getEmbargoCd())) {
            legacyCust.setEmbargoCd("");
          }
        }
      }

      // Story 1597678: Support temporary reactivation requests due to
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

      if (CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && CMR_REQUEST_STATUS_PCR.equals(admin.getReqStatus())
          && "Wx".equals(admin.getProcessedFlag())) {
        legacyCust.setEmbargoCd("E");
        resetOrdBlockToData(entityManager, data);
      }
    }
    // boolean crossBorder = isCrossBorder(addrs);
    String langCd = data.getCustPrefLang();
    if (langCd != null) {
      if ("E".equalsIgnoreCase(langCd))
        legacyCust.setLangCd("1");
      if ("N".equalsIgnoreCase(langCd))
        legacyCust.setLangCd("");
    }

    // if (crossBorder) {
    // if (langCd != null && "E".equalsIgnoreCase(langCd)) {
    // legacyCust.setLangCd("1");
    // }
    // } else if (!crossBorder) {
    // if (langCd != null && "N".equalsIgnoreCase(langCd)) {
    // legacyCust.setLangCd("");
    // }
    // }
    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("AbbreviatedLocation"))) {
      legacyCust.setAbbrevLocn(dummyHandler.messageHash.get("AbbreviatedLocation"));
    }

    String vat = dummyHandler.cmrData.getVat();

    if (!StringUtils.isEmpty(vat)) {
      legacyCust.setVat(vat);
    } else {
      legacyCust.setVat("");
    }

    if (StringUtils.isEmpty(data.getCustSubGrp())) {
      legacyCust.setMrcCd("3");
    } else if (!StringUtils.isEmpty(data.getCustSubGrp()) && (data.getCustSubGrp().contains("BP") || data.getCustSubGrp().contains("BUS"))) {
      legacyCust.setMrcCd("5");
    } else {
      legacyCust.setMrcCd("3");
    }
    // CREATCMR-4293
    if (!StringUtils.isEmpty(data.getIsuCd())) {
      if (StringUtils.isEmpty(data.getClientTier())) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      } else if (!StringUtils.isEmpty(data.getClientTier())) {
        String isuClientTier = (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
            + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : "");
        legacyCust.setIsuCd(isuClientTier);
      }
    }

    List<String> isuCdList = Arrays.asList("5K", "15", "4A", "04", "28");
    if (!StringUtils.isEmpty(data.getIsuCd()) && isuCdList.contains(data.getIsuCd())) {
      legacyCust.setIsuCd(data.getIsuCd() + "7");
    }

  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust cust, CMRRequestContainer cmrObjects, MassUpdtData muData) { // default
    LOG.debug("Mapping default Data values..");

    if (!StringUtils.isBlank(muData.getAbbrevNm())) {
      if ("@".equals(muData.getAbbrevNm())) {
        cust.setAbbrevNm("");
      } else {
        cust.setAbbrevNm(muData.getAbbrevNm());
      }
    }

    // use svcArOffice to store custLang
    // for 788, E - 1, N - ""
    if (!StringUtils.isBlank(muData.getSvcArOffice())) {
      if ("@".equals(muData.getSvcArOffice())) {
        cust.setLangCd("");
      } else {
        String langCd = "";
        if ("N".equalsIgnoreCase(muData.getSvcArOffice()))
          langCd = "";
        if ("E".equalsIgnoreCase(muData.getSvcArOffice()))
          langCd = "1";
        cust.setLangCd(langCd);
      }
    }
    cust.setCustType("");

    // if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
    // cust.setLocNo(cust.getId().getSofCntryCode() +
    // muData.getSubIndustryCd());
    // }

    // RABXA :Bank Account Number
    if (SystemLocation.CROATIA.equals(cust.getId().getSofCntryCode())) {
      if (!StringUtils.isBlank(muData.getSearchTerm())) {
        if ("@".equals(muData.getSearchTerm())) {
          cust.setBankAcctNo("");
        } else {
          cust.setBankAcctNo(muData.getSearchTerm());
        }
      }
    } else {
      if (!StringUtils.isBlank(muData.getEmail2())) {
        if ("@".equals(muData.getEmail2())) {
          cust.setBankAcctNo("");
        } else {
          cust.setBankAcctNo(muData.getEmail2());
        }
      }
    }

    if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
      cust.setAbbrevLocn(muData.getAbbrevLocn());
    }

    // CREATCMR-845
    if (!StringUtils.isBlank(muData.getModeOfPayment())) {
      if ("@".equals(muData.getRestrictTo())) {
        cust.setModeOfPayment("");
      } else {
        cust.setModeOfPayment(muData.getModeOfPayment());
      }
    }

    if (!StringUtils.isEmpty(muData.getIsuCd()) && "5K".equals(muData.getIsuCd())) {
      cust.setIsuCd(muData.getIsuCd() + "7");
    } else {
      String isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
          + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
      if (isuClientTier != null && isuClientTier.endsWith("@")) {
        cust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : cust.getIsuCd().substring(0, 2)) + "7");
      } else if (isuClientTier.contains("@")) {
        cust.setIsuCd("7");
      } else if (isuClientTier != null && isuClientTier.length() == 3) {
        cust.setIsuCd(isuClientTier);
      }
    }

    if (!StringUtils.isBlank(muData.getSearchTerm())) {
      if ("@".equals(muData.getSearchTerm())) {
        cust.setSalesRepNo("");
        cust.setSalesGroupRep("");
      } else {
        cust.setSalesRepNo(muData.getSearchTerm());
        cust.setSalesGroupRep(muData.getSearchTerm());
      }
    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      if ("@".equals(muData.getEnterprise())) {
        cust.setEnterpriseNo("");
      } else {
        cust.setEnterpriseNo(muData.getEnterprise());
      }
    }

    if (!StringUtils.isBlank(muData.getCollectionCd())) {
      if ("@".equals(muData.getCollectionCd())) {
        cust.setCollectionCd("");
      } else {
        cust.setCollectionCd(muData.getCollectionCd());
      }
    }

    if (!StringUtils.isBlank(muData.getIsicCd())) {
      if ("@".equals(muData.getIsicCd())) {
        cust.setIsicCd("");
      } else {
        cust.setIsicCd(muData.getIsicCd());
      }
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      if ("@".equals(muData.getVat())) {
        cust.setVat("");
      } else {
        cust.setVat(muData.getVat());
      }
    }

    // SBO IBO
    if (!StringUtils.isBlank(muData.getCustNm2())) {
      if ("@".equals(muData.getCustNm2())) {
        cust.setSbo("");
        cust.setIbo("");
      } else {
        cust.setSbo(muData.getCustNm2());
        cust.setIbo(muData.getCustNm2());
      }
    }

    if (!StringUtils.isBlank(muData.getInacCd())) {
      if ("@".equals(muData.getInacCd())) {
        cust.setInacCd("");
      } else {
        cust.setInacCd(muData.getInacCd());
      }
    }

    if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
      String subInd = muData.getSubIndustryCd();
      cust.setImsCd(subInd);
      // cust.setEconomicCd(subInd);
    }

    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());

    if (!StringUtils.isBlank(muData.getCsoSite())) {
      if ("@".equals(muData.getCsoSite())) {
        cust.setEconomicCd("");
      } else {
        cust.setEconomicCd(muData.getCsoSite());
      }
    }

    if (!StringUtils.isBlank(muData.getOrdBlk())) {
      if ("@".equals(muData.getOrdBlk())) {
        cust.setEmbargoCd("");
      } else {
        cust.setEmbargoCd(muData.getOrdBlk());
      }
    }
  }

  private void resetOrdBlockToData(EntityManager entityManager, Data data) {
    data.setOrdBlk("88");
    data.setEmbargoCd("E");
    entityManager.merge(data);
    entityManager.flush();
  }

  private void blankOrdBlockFromData(EntityManager entityManager, Data data) {
    data.setOrdBlk("");
    entityManager.merge(data);
    entityManager.flush();
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
      handler.currentAddresses = addresses;
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

  protected void handleDataDefaults(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData) {
    messageHash.put("SourceCode", "EF0");
    messageHash.put("CEdivision", "2");

    String sbo = messageHash.get("SBO");
    messageHash.put("IBO", sbo);
    messageHash.put("MarketingResponseCode", "3");

    String custType = cmrData.getCustSubGrp();
    if (custType.contains("BP") || custType.contains("BUS")) {
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

  private List<Addr> getAddrLegacy(EntityManager entityManager, String reqId) {
    LOG.debug("CEE -- Searching for ADDR records for Request " + reqId);
    String sql = ExternalizedQuery.getSql("LEGACYD.GET.ADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.getResults(Addr.class);
  }

  public void modifyAddrUseFields(String seqNo, String addrUse, CmrtAddr legacyAddr) {
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
  public void transformOtherData(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects, CMRRequestContainer cmrObjects) {

    long reqId = cmrObjects.getAdmin().getId().getReqId();
    List<Addr> addrList = cmrObjects.getAddresses();
    List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();

    int seqStartForRequiredAddr = 1;
    for (int i = 0; i < addrList.size(); i++) {
      Addr addr = addrList.get(i);
      String addrSeq = addr.getId().getAddrSeq();
      String addrType = addr.getId().getAddrType();

      if (addrSeq.equals("1")) {

        updateRequiredAddresses(entityManager, reqId, addrList.get(i).getId().getAddrType(), legacyAddrList.get(i).getId().getAddrNo(),
            changeSeqNo(seqStartForRequiredAddr++), legacyObjects, i);
      }

    }

    String reqType = cmrObjects.getAdmin().getReqType();
    long requestId = cmrObjects.getAdmin().getId().getReqId();

    Map<String, String> addrSeqToAddrUseMap = new HashMap<String, String>();
    addrSeqToAddrUseMap = mapSeqNoToAddrUse(getAddrLegacy(entityManager, String.valueOf(requestId)));
    if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      addrSeqToAddrUseMap = mapSeqNoToAddrUse(cmrObjects.getAddresses());
    }
    LOG.debug("LEGACY -- ME OVERRIDE transformOtherData");
    LOG.debug("addrSeqToAddrUseMap size: " + addrSeqToAddrUseMap.size());
    for (CmrtAddr legacyAddr : legacyObjects.getAddresses()) {
      if ("C".equals(cmrObjects.getAdmin().getReqType())) {
        modifyAddrUseFields(legacyAddr.getId().getAddrNo(), addrSeqToAddrUseMap.get(legacyAddr.getId().getAddrNo()), legacyAddr);
      }
    }

  }

  private Map<String, String> mapSeqNoToAddrUseLegacy(List<CmrtAddr> legacyAddrList, List<MassUpdtAddr> muAddrlist) {
    Map<String, String> addrSeqToAddrUseMap = new HashMap<>();
    for (CmrtAddr legacyAddr : legacyAddrList) {
      for (MassUpdtAddr muAddr : muAddrlist) {
        if (muAddr.getAddrSeqNo().equals(legacyAddr.getId().getAddrNo())) {
          addrSeqToAddrUseMap.put(legacyAddr.getId().getAddrNo(), getAddressUseByType(muAddr.getId().getAddrType()));
          break;
        } else if ("ZP01".equals(muAddr.getId().getAddrType()) && "Y".equals(legacyAddr.getIsAddrUseBilling())) {
          addrSeqToAddrUseMap.put(legacyAddr.getId().getAddrNo(), getAddressUseByType(muAddr.getId().getAddrType()));
        }
      }
    }
    return addrSeqToAddrUseMap;
  }

  private Map<String, String> mapSeqNoToAddrUse(List<Addr> addrList) {
    Map<String, String> addrSeqToAddrUseMap = new HashMap<>();
    for (Addr addr : addrList) {
      addrSeqToAddrUseMap.put(addr.getId().getAddrSeq(), getAddressUse(addr));
    }
    return addrSeqToAddrUseMap;
  }

  @Override
  public boolean hasCmrtCustExt() {
    if ("BE".equals(DEFAULT_LANDED_COUNTRY) || "NL".equals(DEFAULT_LANDED_COUNTRY) || "LU".equals(DEFAULT_LANDED_COUNTRY)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {

    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();

    if (!StringUtils.isBlank(data.getBpSalesRepNo())) {
      legacyCustExt.setTeleCovRep(data.getBpSalesRepNo());
    }
    legacyCustExt.setiTaxCode(data.getTaxCd2() == null ? "" : data.getTaxCd2());
    legacyCustExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
  }

  @Override
  public void transformLegacyCustomerExtDataMassUpdate(EntityManager entityManager, CmrtCustExt custExt, CMRRequestContainer cmrObjects,
      MassUpdtData muData, String cmr) throws Exception {

    // for tax office
    // List<MassUpdtAddr> muaList = cmrObjects.getMassUpdateAddresses();
    // if (muaList != null && muaList.size() > 0) {
    // for (MassUpdtAddr mua : muaList) {
    // if ("ZP01".equals(mua.getId().getAddrType())) {
    // if (!StringUtils.isBlank(mua.getFloor())) {
    // if (DEFAULT_CLEAR_CHAR.equals(mua.getFloor())) {
    // custExt.setiTaxCode("");
    // } else {
    // custExt.setiTaxCode(mua.getFloor());
    // }
    // break;
    // }
    //
    // }
    // }
    // }

    // if (!StringUtils.isBlank(muData.getNewEntpName1())) {
    // if ("@".equals(muData.getNewEntpName1())) {
    // custExt.setiTaxCode("");
    // } else {
    // if (muData.getNewEntpName1().length() > 9) {
    // custExt.setiTaxCode(muData.getNewEntpName1().substring(0, 8));
    // } else {
    // custExt.setiTaxCode(muData.getNewEntpName1());
    // }
    // }
    // }

    if (!StringUtils.isBlank(muData.getAffiliate())) {
      if ("@".equals(muData.getAffiliate())) {
        custExt.setTeleCovRep("");
      } else {
        custExt.setTeleCovRep(muData.getAffiliate());
      }
    }

    if (!StringUtils.isBlank(muData.getRestrictTo())) {
      if ("@".equals(muData.getRestrictTo())) {
        custExt.setiTaxCode("");
      } else {
        custExt.setiTaxCode(muData.getRestrictTo());
      }
    }

    // List<MassUpdtAddr> muAddrList = cmrObjects.getMassUpdateAddresses();
    // MassUpdtAddr zp01Addr = new MassUpdtAddr();
    // for (MassUpdtAddr muAddr : muAddrList) {
    // if ("ZP01".equals(muAddr.getId().getAddrType())) {
    // zp01Addr = muAddr;
    // break;
    // }
    // }
    // if (zp01Addr != null && !StringUtils.isBlank(zp01Addr.getFloor())) {
    // if ("@".equals(zp01Addr.getFloor())) {
    // custExt.setiTaxCode("");
    // } else {
    // custExt.setiTaxCode(zp01Addr.getFloor());
    // }
    // }
  }

  private void copyMailingFromBilling(LegacyDirectObjectContainer legacyObjects, CmrtAddr billingAddr) {
    CmrtAddr mailingAddr = SerializationUtils.clone(billingAddr);
    mailingAddr.getId().setAddrNo("00001");
    mailingAddr.setIsAddrUseMailing(ADDRESS_USE_EXISTS);
    mailingAddr.setIsAddrUseBilling(ADDRESS_USE_NOT_EXISTS);
    // modifyAddrUseFields(MQMsgConstants.SOF_ADDRESS_USE_MAILING, mailingAddr);
    legacyObjects.getAddresses().add(mailingAddr);
  }

  private void copyBillingFromMailing(LegacyDirectObjectContainer legacyObjects, CmrtAddr mailingAddr, String billingseq) {
    CmrtAddr billingAddr = SerializationUtils.clone(mailingAddr);
    billingAddr.getId().setAddrNo(billingseq);
    billingAddr.setIsAddrUseMailing(ADDRESS_USE_NOT_EXISTS);
    billingAddr.setIsAddrUseBilling(ADDRESS_USE_EXISTS);
    billingAddr.setForCreate(true);
    billingAddr.setForUpdate(false);
    // modifyAddrUseFields(MQMsgConstants.SOF_ADDRESS_USE_MAILING, mailingAddr);
    legacyObjects.getAddresses().add(billingAddr);
  }

  public String getSeqForBilling(EntityManager entityManager, long reqId) {
    String maxseq = "";
    int addrSeq = 0;
    String sql = ExternalizedQuery.getSql("TR.GETSEQFORBILLING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Object[]> results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      maxseq = sResult[0].toString();
    }
    addrSeq = Integer.parseInt(maxseq);
    addrSeq++;

    maxseq = Integer.toString(addrSeq);
    maxseq = StringUtils.leftPad(maxseq, 5, '0');

    LOG.debug("Get Copy Billing Seq = " + maxseq);

    return maxseq;
  }

  public String getMassSeqForBilling(EntityManager entityManager, String cmrNo, String cntry) {
    String maxseq = "";
    int addrSeq = 0;
    String sql = ExternalizedQuery.getSql("TR.MASS.GETSEQFORBILLING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_NUM", cmrNo);
    query.setParameter("CNTRY", cntry);
    List<Object[]> results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      maxseq = sResult[0].toString();
    }
    addrSeq = Integer.parseInt(maxseq);

    if (addrSeq < 6) {
      addrSeq = 6;
    }
    addrSeq++;

    maxseq = Integer.toString(addrSeq);
    maxseq = StringUtils.leftPad(maxseq, 5, '0');

    LOG.debug("Get Copy Billing Seq = " + maxseq);

    return maxseq;
  }

  @Override
  public boolean sequenceNoUpdateLogic(EntityManager entityManager, CMRRequestContainer cmrObjects, Addr currAddr, boolean flag) {
    return false;
  }

  private void updateRequiredAddresses(EntityManager entityManager, long reqId, String addrType, String oldSeq, String newSeq,
      LegacyDirectObjectContainer legacyObjects, int index) {
    updateAddrSeq(entityManager, reqId, addrType, oldSeq, newSeq, null);
    legacyObjects.getAddresses().get(index).getId().setAddrNo(newSeq);
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
    LOG.debug("CEE - Assigning address sequence " + newSeq + " to " + addrType + " address.");
    q.executeSql();
  }

  @Override
  public boolean isUpdateNeededOnAllAddressType(EntityManager entityManager, CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();
    if (CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason())) {
      return true;
    } else {
      return false;
    }
  }
}
