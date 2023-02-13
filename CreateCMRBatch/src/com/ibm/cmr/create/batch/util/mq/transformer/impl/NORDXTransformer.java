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
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * {@link MessageTransformer} implementation for Nordics
 * 
 * @author Paul
 * 
 */
public class NORDXTransformer extends EMEATransformer {

  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode" };

  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02", "ZP02", "ZD02", "ZP03" };

  private static final List<String> CEE_COUNTRY_LIST = Arrays.asList(SystemLocation.SLOVAKIA, SystemLocation.KYRGYZSTAN, SystemLocation.SERBIA,
      SystemLocation.ARMENIA, SystemLocation.AZERBAIJAN, SystemLocation.TURKMENISTAN, SystemLocation.TAJIKISTAN, SystemLocation.ALBANIA,
      SystemLocation.BELARUS, SystemLocation.BULGARIA, SystemLocation.GEORGIA, SystemLocation.KAZAKHSTAN, SystemLocation.BOSNIA_AND_HERZEGOVINA,
      SystemLocation.MACEDONIA, SystemLocation.SLOVENIA, SystemLocation.HUNGARY, SystemLocation.UZBEKISTAN, SystemLocation.MOLDOVA,
      SystemLocation.POLAND, SystemLocation.RUSSIAN_FEDERATION, SystemLocation.ROMANIA, SystemLocation.UKRAINE, SystemLocation.CROATIA);

  private static final List<String> ME_COUNTRY_LIST = Arrays.asList(SystemLocation.BAHRAIN, SystemLocation.MOROCCO, SystemLocation.GULF,
      SystemLocation.UNITED_ARAB_EMIRATES, SystemLocation.ABU_DHABI, SystemLocation.IRAQ, SystemLocation.JORDAN, SystemLocation.KUWAIT,
      SystemLocation.LEBANON, SystemLocation.LIBYA, SystemLocation.OMAN, SystemLocation.PAKISTAN, SystemLocation.QATAR, SystemLocation.SAUDI_ARABIA,
      SystemLocation.YEMEN, SystemLocation.SYRIAN_ARAB_REPUBLIC, SystemLocation.EGYPT, SystemLocation.TUNISIA_SOF, SystemLocation.GULF);

  private static final Logger LOG = Logger.getLogger(EMEATransformer.class);

  public static String DEFAULT_LANDED_COUNTRY = "AE";
  public String SUB_REGION_COUNTRY = "";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";
  private static final String DEFAULT_CLEAR_CHAR = "@";

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
  private static final String[] GULF_ORIGINAL_COUNTRIES = { "677", "680", "620", "832", "805", "767", "823", "762", "768", "772", "849", "752" };
  private static final String[] ME_GBMSBM_COUNTRIES = { "677", "680", "620", "832", "805", "767", "823", "675" };
  private static final String SCENARIO_TYPE_SBM = "SBM";
  private static final String SCENARIO_TYPE_GBM = "GBM";
  private static final String GULF_DUPLICATE = "GULF";

  public NORDXTransformer(String issuingCntry) {
    super(issuingCntry);

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
    messageHash.put("TaxCode", "");
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
    // <XXXAddress1> -> name
    // <XXXAddress2> -> name 2,
    // <XXXAddress3> -> street
    // <XXXAddress4> -> Street con't
    // <XXXAddress5> -> District + Postal code + City
    // <XXXAddress6> -> Country

    // customer name
    String line1 = addrData.getCustNm1();

    // name 2, as nickname
    String line2 = addrData.getCustNm2();

    // Street
    String line3 = addrData.getAddrTxt();

    // Street Con't
    String line4 = addrData.getAddrTxt2();

    // district + postal code + city
    String line5 = "";

    if (!StringUtils.isBlank(addrData.getDept())) {
      line5 += (line5.length() > 0 ? " " : "") + addrData.getDept();
    }
    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line5 += (line5.length() > 0 ? " " : "") + addrData.getPostCd();
    }
    if (!StringUtils.isBlank(addrData.getCity1())) {
      line5 += (line5.length() > 0 ? " " : "") + addrData.getCity1();
    }

    line5 = line5.trim();

    String countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());
    ;

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
      return "Mail-To";
    case "ZD01":
      return "Ship-To";
    case "ZI01":
      return "Install-at";
    case "ZS02":
      return "EPL";
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
      return "Mail-To";
    case "ZD01":
      return "Ship-To";
    case "ZI01":
      return "Install-at";
    case "ZS02":
      return "EPL";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    String toUse = getCmrIssuingCntry();
    return toUse;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "00001";
  }

  public void setDefaultLandedCountry(Data data) {
    if (SystemLocation.SWEDEN.equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "SE";// Sweden
    }
    if (SystemLocation.NORWAY.equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "NO";// Norway
    }
    if (SystemLocation.FINLAND.equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "FI";// Finland
      if (StringUtils.isNotBlank(data.getCountryUse()) && data.getCountryUse().length() == 5) {
        DEFAULT_LANDED_COUNTRY = data.getCountryUse().substring(3, 5);
        SUB_REGION_COUNTRY = data.getCountryUse().substring(3, 5);
      }
    }
    if (SystemLocation.DENMARK.equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "DK";// Denmark
      if (StringUtils.isNotBlank(data.getCountryUse()) && data.getCountryUse().length() == 5) {
        SUB_REGION_COUNTRY = data.getCountryUse().substring(3, 5);
      }
    }
  }

  /**
   * Checks if this is a cross-border scenario
   * 
   * @param addr
   * @return
   */
  protected boolean isCrossBorder(Addr addr) {
    if (DEFAULT_LANDED_COUNTRY.equals("DK")) {
      if (StringUtils.isBlank(SUB_REGION_COUNTRY)) {// blank means Denmark
        return !"DK".equals(addr.getLandCntry());
      } else {
        return true;
      }
    }
    return !DEFAULT_LANDED_COUNTRY.equals(addr.getLandCntry());
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZP03:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZD02:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZP02:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  public String getAddressUseByType(String addrType) {
    switch (addrType) {
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZP03:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZD02:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZP02:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
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
    Admin adminData = handler.adminData;
    boolean update = "U".equals(handler.adminData.getReqType());
    boolean crossBorder = isCrossBorder(addrData);

    LOG.debug("Legacy Direct -Handling Address for " + (update ? "update" : "create") + " request.");

    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";
    String addrType = addrData.getId().getAddrType();
    String phone = "";
    String addrLineT = "";

    String custName = StringUtils.isNotBlank(addrData.getCustNm1()) ? addrData.getCustNm1() : "";
    String custNameCond = StringUtils.isNotBlank(addrData.getCustNm2()) ? addrData.getCustNm2() : "";
    String additionalInfo = StringUtils.isNotBlank(addrData.getCustNm3()) ? addrData.getCustNm3() : "";
    String attPerson = StringUtils.isNotBlank(addrData.getCustNm4()) ? addrData.getCustNm4() : "";
    String street = StringUtils.isNotBlank(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "";
    String streetCond = StringUtils.isNotBlank(addrData.getAddrTxt2()) ? addrData.getAddrTxt2() : "";
    String pobox = StringUtils.isNotBlank(addrData.getPoBox()) ? addrData.getPoBox() : "";

    String comboStreetCondPobox = "";
    if (StringUtils.isNotBlank(streetCond)) {
      comboStreetCondPobox = streetCond;
    }
    if (StringUtils.isNotBlank(pobox)) {
      if (StringUtils.isNotBlank(comboStreetCondPobox)) {
        comboStreetCondPobox += ", PO BOX " + pobox;
      } else {
        comboStreetCondPobox += "PO BOX " + pobox;
      }
    }
    String city = StringUtils.isNotBlank(addrData.getCity1()) ? addrData.getCity1() : "";
    String postCode = StringUtils.isNotBlank(addrData.getPostCd()) ? addrData.getPostCd() : "";

    String landedCntry = StringUtils.isNotBlank(addrData.getLandCntry()) ? addrData.getLandCntry() : "";
    String fullCntryName = "";
    if (StringUtils.isNotBlank(landedCntry)) {
      EntityManager entityManager = handler.getEntityManager();
      String sql = ExternalizedQuery.getSql("ADDRESS.GETCNTRY");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", landedCntry);
      fullCntryName = query.getSingleResult(String.class);
    }
    fullCntryName = StringUtils.isNotBlank(fullCntryName) ? (fullCntryName.length() >= 30 ? fullCntryName.substring(0, 30) : fullCntryName) : "";

    List<String> addrAttrList = Arrays.asList(custName, custNameCond, additionalInfo, attPerson, street, comboStreetCondPobox, postCode + " " + city);
    String[] lines = new String[7];
    int strCount = 0;
    for (int i = 0; i < addrAttrList.size(); i++) {
      if (StringUtils.isNotBlank(addrAttrList.get(i))) {
        lines[strCount] = addrAttrList.get(i);
        strCount++;
      }
    }

    if (crossBorder) {
      if (strCount < lines.length - 1) {
        lines[strCount] = fullCntryName;
      } else {
        lines[strCount - 1] = fullCntryName;
      }
    }

    legacyAddr.setAddrLine1(lines[0]);
    legacyAddr.setAddrLine2(lines[1]);
    legacyAddr.setAddrLine3(lines[2]);
    legacyAddr.setAddrLine4(lines[3]);
    legacyAddr.setAddrLine5(lines[4]);
    legacyAddr.setAddrLine6(lines[5]);
    legacyAddr.setCity(addrData.getCity1());

    if (StringUtils.isNotBlank(addrData.getPostCd())) {
      legacyAddr.setZipCode(addrData.getPostCd());
    }

    if (StringUtils.isNotBlank(addrData.getAddrTxt())) {
      legacyAddr.setStreet(addrData.getAddrTxt());
    }
    legacyAddr.setPoBox(StringUtils.isNotBlank(pobox) ? pobox : "");
    legacyAddr.setAddrPhone(phone);
    legacyAddr.setAddrLineT(addrLineT);
    legacyAddr.setDistrict(addrData.getDept());
    legacyAddr.setAddrLineU("");

  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    System.out.println("_custSubGrp = " + custSubGrp);

    LOG.debug("Set max and min range of cmrNo..");
    if ("CBINT".equals(custSubGrp) || "DKINT".equals(custSubGrp) || "EEINT".equals(custSubGrp) || "FIINT".equals(custSubGrp)
        || "FOINT".equals(custSubGrp) || "GLINT".equals(custSubGrp) || "INTER".equals(custSubGrp) || "ISINT".equals(custSubGrp)
        || "LTINT".equals(custSubGrp) || "LVINT".equals(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(996999);
      LOG.debug("that is Nordics INTER CMR");
    } else {
      generateCMRNoObj.setMin(1);
      generateCMRNoObj.setMax(979999);
      LOG.debug("that is Nordics No INTER CMR");
    }
    if ("CBISO".equals(custSubGrp) || "DKISO".equals(custSubGrp) || "EEISO".equals(custSubGrp) || "FIISO".equals(custSubGrp)
        || "FOISO".equals(custSubGrp) || "GLISO".equals(custSubGrp) || "INTSO".equals(custSubGrp) || "ISISO".equals(custSubGrp)
        || "LTISO".equals(custSubGrp) || "LVISO".equals(custSubGrp)) {
      generateCMRNoObj.setMin(997000);
      generateCMRNoObj.setMax(997999);
      LOG.debug("that is Nordics INTER OS CMR");
    }
  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr addr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {
    legacyAddr.setForUpdate(true);
    boolean dummyReq = false;
    if (StringUtils.isBlank(addr.getCustNm1())) {
      dummyReq = true;
    }
    if (dummyReq) {
      return;
    }

    boolean crossBorder = false;
    if (DEFAULT_LANDED_COUNTRY.equals("DK")) {
      if (StringUtils.isBlank(SUB_REGION_COUNTRY)) {// blank means Denmark
        crossBorder = !"DK".equals(addr.getLandCntry());
      } else {
        crossBorder = true;
      }
    } else {
      crossBorder = !DEFAULT_LANDED_COUNTRY.equals(addr.getLandCntry());
    }
    String custName = StringUtils.isNotBlank(addr.getCustNm1()) ? addr.getCustNm1() : "";
    String custNameCond = StringUtils.isNotBlank(addr.getCustNm2()) ? addr.getCustNm2() : "";
    String additionalInfo = StringUtils.isNotBlank(addr.getCustNm3()) ? addr.getCustNm3() : "";
    String attPerson = StringUtils.isNotBlank(addr.getCustNm4()) ? addr.getCustNm4() : "";
    String street = StringUtils.isNotBlank(addr.getAddrTxt()) ? addr.getAddrTxt() : "";
    String streetCond = StringUtils.isNotBlank(addr.getAddrTxt2()) ? addr.getAddrTxt2() : "";
    String pobox = StringUtils.isNotBlank(addr.getPoBox()) ? addr.getPoBox() : "";

    String comboStreetCondPobox = "";
    if (StringUtils.isNotBlank(streetCond)) {
      comboStreetCondPobox = streetCond;
    }
    if (StringUtils.isNotBlank(pobox)) {
      if (StringUtils.isNotBlank(comboStreetCondPobox)) {
        comboStreetCondPobox += ", PO BOX " + pobox;
      } else {
        comboStreetCondPobox += "PO BOX " + pobox;
      }
    }

    String city = StringUtils.isNotBlank(addr.getCity1()) ? addr.getCity1() : "";
    String postCode = StringUtils.isNotBlank(addr.getPostCd()) ? addr.getPostCd() : "";

    String landedCntry = StringUtils.isNotBlank(addr.getLandCntry()) ? addr.getLandCntry() : "";
    String fullCntryName = "";
    if (StringUtils.isNotBlank(landedCntry)) {
      String sql = ExternalizedQuery.getSql("ADDRESS.GETCNTRY");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", landedCntry);
      fullCntryName = query.getSingleResult(String.class);
    }

    List<String> addrAttrList = Arrays.asList(custName, custNameCond, additionalInfo, attPerson, street, comboStreetCondPobox, postCode + " " + city);
    String[] lines = new String[7];
    int strCount = 0;
    for (int i = 0; i < addrAttrList.size(); i++) {
      if (StringUtils.isNotBlank(addrAttrList.get(i))) {
        lines[strCount] = addrAttrList.get(i);
        strCount++;
      }
    }

    if (crossBorder) {
      if (strCount < lines.length - 1) {
        lines[strCount] = fullCntryName;
      } else {
        lines[strCount - 1] = fullCntryName;
      }
    }

    legacyAddr.setAddrLine1(lines[0]);
    legacyAddr.setAddrLine2(lines[1]);
    legacyAddr.setAddrLine3(lines[2]);
    legacyAddr.setAddrLine4(lines[3]);
    legacyAddr.setAddrLine5(lines[4]);
    legacyAddr.setAddrLine6(lines[5]);

    if (StringUtils.isNotBlank(city)) {
      legacyAddr.setCity(city);
    }

    if (StringUtils.isNotBlank(postCode)) {
      legacyAddr.setZipCode(postCode);
    }

    if (StringUtils.isNotBlank(addr.getAddrTxt())) {
      legacyAddr.setStreet(addr.getAddrTxt());
    }
    legacyAddr.setPoBox(StringUtils.isNotBlank(pobox) ? pobox : legacyAddr.getPoBox());
    legacyAddr.setAddrLineU("");

    // boolean crossBorder = false;
    // if (!StringUtils.isEmpty(addr.getLandCntry()) &&
    // !"ES".equals(addr.getLandCntry())) {
    // crossBorder = true;
    // } else {
    // crossBorder = false;
    // }
    // formatMassUpdateAddressLines(entityManager, legacyAddr, addr, false);

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

    line3 = legacyAddr.getAddrLine3() != null ? legacyAddr.getAddrLine3().trim() : "";

    String poBox = !StringUtils.isEmpty(legacyAddr.getPoBox()) ? legacyAddr.getPoBox() : "";
    if (!StringUtils.isEmpty(poBox) && !poBox.toUpperCase().startsWith("APTO")) {
      poBox = " APTO " + poBox;
    }

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
      // DTN: Commented because we are not passing phone numbers on
      // the
      // template
      // line6 = legacyAddr.getAddrPhone();
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

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();

    setDefaultLandedCountry(data);
    formatDataLines(dummyHandler);

    String landedCntry = "";
    String mailPostCode = "";
    boolean zs01changed = false;
    for (Addr addr : cmrObjects.getAddresses()) {
      if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
        if ("Y".equals(addr.getChangedIndc())) {
          zs01changed = true;
        }
        legacyCust.setTelNoOrVat(addr.getCustPhone());
        landedCntry = addr.getLandCntry();
        mailPostCode = addr.getPostCd();
        break;
      }
    }

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {

      String cntry = legacyCust.getId().getSofCntryCode().trim();

      legacyCust.setCeDivision("");
      legacyCust.setDcRepeatAgreement("0");
      legacyCust.setAuthRemarketerInd("0");
      legacyCust.setCeDivision("2");
      legacyCust.setDeptCd("");
      legacyCust.setEnterpriseNo("");
      legacyCust.setEducAllowance("");
      legacyCust.setLeasingInd("0");

      if (SystemLocation.NORWAY.equals(data.getCmrIssuingCntry())) {
        legacyCust.setCeBo("");
      } else if (SystemLocation.FINLAND.equals(data.getCmrIssuingCntry())) {
        legacyCust.setCeBo("X900000");
      } else if (SystemLocation.SWEDEN.equals(data.getCmrIssuingCntry())) {
        int postCd = 0;
        String head = "0";
        if (StringUtils.isNotBlank(mailPostCode) && mailPostCode.length() >= 2) {
          head = mailPostCode.trim().substring(0, 2);
          if (StringUtils.isNumeric(head)) {
            postCd = Integer.valueOf(head);
          }
        }
        int beginPost = Integer.valueOf(postCd);
        if (!DEFAULT_LANDED_COUNTRY.equals(landedCntry)) {
          legacyCust.setCeBo("130");
        } else if (beginPost > 9 && beginPost < 20) {
          legacyCust.setCeBo("130");
        } else if (beginPost > 19 && beginPost < 40) {
          legacyCust.setCeBo("140");
        } else if (beginPost > 39 && beginPost < 58) {
          legacyCust.setCeBo("110");
        } else if (beginPost > 57 && beginPost < 77) {
          legacyCust.setCeBo("130");
        } else if (beginPost > 76 && beginPost < 99) {
          legacyCust.setCeBo("130");
        }

      } else if (SystemLocation.DENMARK.equals(data.getCmrIssuingCntry())) {
        if ("678".equals(data.getCountryUse())) {
          int postCd = 10001;
          if (StringUtils.isNotBlank(mailPostCode)) {
            if (StringUtils.isNumeric(mailPostCode)) {
              postCd = Integer.valueOf(mailPostCode.trim());
            }
          }
          if ("CROSS".equals(data.getCustGrp())) {
            legacyCust.setCeBo("000281X");
          } else if (postCd >= 0 && postCd < 5000) {
            legacyCust.setCeBo("000281X");
          } else if (postCd > 4999 && postCd < 7400) {
            legacyCust.setCeBo("000246X");
          } else if (postCd > 7399 && postCd < 10000) {
            legacyCust.setCeBo("000245X");
          }
        } else if ("678FO".equals(data.getCountryUse())) {
          legacyCust.setCeBo("000200F");
        } else if ("678GL".equals(data.getCountryUse())) {
          legacyCust.setCeBo("000200G");
        } else if ("678IS".equals(data.getCountryUse())) {
          legacyCust.setCeBo("000200I");
        }
      }

      if (SystemLocation.DENMARK.equals(cntry)) {
        legacyCust.setCurrencyCd("");
      } else if (SystemLocation.FINLAND.equals(cntry)) {
        legacyCust.setCurrencyCd("EU");
      } else if (SystemLocation.NORWAY.equals(cntry)) {
        legacyCust.setCurrencyCd("");
      } else if (SystemLocation.SWEDEN.equals(cntry)) {
        legacyCust.setCurrencyCd("");
      }

      if (SystemLocation.NORWAY.equals(data.getCmrIssuingCntry())) {
        legacyCust.setOverseasTerritory("");
      } else if (SystemLocation.SWEDEN.equals(data.getCmrIssuingCntry())) {
        legacyCust.setOverseasTerritory("");
      } else if (SystemLocation.DENMARK.equals(data.getCmrIssuingCntry())) {
        if ("678".equals(data.getCountryUse())) {
          legacyCust.setOverseasTerritory("");
        } else if ("678FO".equals(data.getCountryUse())) {
          legacyCust.setOverseasTerritory("102");
        } else if ("678GL".equals(data.getCountryUse())) {
          legacyCust.setOverseasTerritory("103");
        } else if ("678IS".equals(data.getCountryUse())) {
          legacyCust.setOverseasTerritory("742");
        }
      } else if (SystemLocation.FINLAND.equals(data.getCmrIssuingCntry())) {
        if ("702".equals(data.getCountryUse())) {
          legacyCust.setOverseasTerritory("");
        } else if ("702EE".equals(data.getCountryUse())) {
          legacyCust.setOverseasTerritory("104");
        } else if ("702LT".equals(data.getCountryUse())) {
          legacyCust.setOverseasTerritory("106");
        } else if ("702LV".equals(data.getCountryUse())) {
          legacyCust.setOverseasTerritory("105");
        }
      }

      // MRC BP scenario set 5, else 2;CIEXJ BP set 1, else 0
      if (!StringUtils.isEmpty(data.getCustSubGrp())
          && Arrays.asList("BUSPR", "CBBUS", "DKBUS", "EEBUS", "FIBUS", "FOBUS", "GLBUS", "ISBUS", "LTBUS", "LVBUS").contains(data.getCustSubGrp())) {
        legacyCust.setMrcCd("5");
        legacyCust.setAuthRemarketerInd("1");
      } else {
        legacyCust.setAuthRemarketerInd("0");
        legacyCust.setMrcCd("2");
      }

      // leading Account number
      if (StringUtils.isNotBlank(data.getCompany()) && data.getCompany().length() > 4) {
        legacyCust.setLeadingAccNo(data.getCompany() + legacyCust.getMrcCd());
      } else {
        legacyCust.setLeadingAccNo(legacyCust.getId().getCustomerNo() + legacyCust.getMrcCd());
      }

      if (SystemLocation.NORWAY.equals(data.getCmrIssuingCntry())) {
        legacyCust.setRealCtyCd("806");
      } else if (SystemLocation.SWEDEN.equals(data.getCmrIssuingCntry())) {
        legacyCust.setRealCtyCd("846");
      } else if (SystemLocation.DENMARK.equals(data.getCmrIssuingCntry())) {
        if ("678".equals(data.getCountryUse())) {
          legacyCust.setRealCtyCd("678");
        } else if ("678FO".equals(data.getCountryUse())) {
          legacyCust.setRealCtyCd("678");
        } else if ("678GL".equals(data.getCountryUse())) {
          legacyCust.setRealCtyCd("678");
        } else if ("678IS".equals(data.getCountryUse())) {
          legacyCust.setRealCtyCd("742");
        }
      } else if (SystemLocation.FINLAND.equals(data.getCmrIssuingCntry())) {
        if ("702".equals(data.getCountryUse())) {
          legacyCust.setRealCtyCd("702");
        } else if ("702EE".equals(data.getCountryUse())) {
          legacyCust.setRealCtyCd("602");
        } else if ("702LT".equals(data.getCountryUse())) {
          legacyCust.setRealCtyCd("638");
        } else if ("702LV".equals(data.getCountryUse())) {
          legacyCust.setRealCtyCd("608");
        }
      }
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {

      String cntry = legacyCust.getId().getSofCntryCode().trim();

      if (!StringUtils.isBlank(data.getTerritoryCd())) {
        legacyCust.setOverseasTerritory(data.getTerritoryCd());
      }

      if (SystemLocation.SWEDEN.equals(cntry) && "SE".equals(landedCntry) && zs01changed) {
        int postCd = 0;
        String head = "0";
        if (StringUtils.isNotBlank(mailPostCode) && mailPostCode.length() >= 2) {
          head = mailPostCode.trim().substring(0, 2);
          if (StringUtils.isNumeric(head)) {
            postCd = Integer.valueOf(head);
          }
        }
        int beginPost = Integer.valueOf(postCd);
        if (!DEFAULT_LANDED_COUNTRY.equals(landedCntry)) {
          legacyCust.setCeBo("130");
        } else if (beginPost > 9 && beginPost < 20) {
          legacyCust.setCeBo("130");
        } else if (beginPost > 19 && beginPost < 40) {
          legacyCust.setCeBo("140");
        } else if (beginPost > 39 && beginPost < 58) {
          legacyCust.setCeBo("110");
        } else if (beginPost > 57 && beginPost < 77) {
          legacyCust.setCeBo("130");
        } else if (beginPost > 76 && beginPost < 99) {
          legacyCust.setCeBo("130");
        }

      } else if (SystemLocation.DENMARK.equals(cntry) && StringUtils.isBlank(SUB_REGION_COUNTRY) && "DK".equals(landedCntry) && zs01changed) {
        int postCd = 10001;
        if (StringUtils.isNotBlank(mailPostCode)) {
          if (StringUtils.isNumeric(mailPostCode)) {
            postCd = Integer.valueOf(mailPostCode.trim());
          }
        }
        if (postCd >= 0 && postCd < 5000) {
          legacyCust.setCeBo("000281X");
        } else if (postCd > 4999 && postCd < 7400) {
          legacyCust.setCeBo("000246X");
        } else if (postCd > 7399 && postCd < 10000) {
          legacyCust.setCeBo("000245X");
        }
      } else {
        String sql = ExternalizedQuery.getSql("LEGACYD.GETCUST");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("COUNTRY", cntry);
        query.setParameter("CMR_NO", legacyCust.getId().getCustomerNo());
        query.setForReadOnly(true);
        CmrtCust cust = query.getSingleResult(CmrtCust.class);
        if (cust == null) {
          LOG.error("Legacy Direct -can't find LegacyCust for request:" + admin.getId().getReqId());
        } else {
          legacyCust.setCeBo(cust.getCeBo());
        }
      }

      if (SystemLocation.DENMARK.equals(cntry)) {
        if ("DKK".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("");
        } else if ("EUR".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("EU");
        } else if ("USD".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("US");
        }
      } else if (SystemLocation.FINLAND.equals(cntry)) {
        if ("EUR".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("EU");
        } else if ("USD".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("US");
        }
      } else if (SystemLocation.NORWAY.equals(cntry)) {
        if ("NOK".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("");
        } else if ("EUR".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("EU");
        } else if ("USD".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("US");
        }
      } else if (SystemLocation.SWEDEN.equals(cntry)) {
        if ("SEK".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("");
        } else if ("EUR".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("EU");
        } else if ("USD".equals(data.getCurrencyCd())) {
          legacyCust.setCurrencyCd("US");
        }
      }

      // leading Account number
      if (StringUtils.isNotBlank(data.getCompany())) {
        legacyCust.setLeadingAccNo(data.getCompany() + legacyCust.getMrcCd());
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

      // Story 1597678: Support temporary reactivation requests due to
      // Embargo Code handling
      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason())
          && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && admin.getReqStatus() != null
          && admin.getReqStatus().equals(CMR_REQUEST_STATUS_CPR) && (rdcEmbargoCd != null && !StringUtils.isBlank(rdcEmbargoCd))
          && "D".equals(rdcEmbargoCd) && (dataEmbargoCd == null || StringUtils.isBlank(dataEmbargoCd))) {
        legacyCust.setEmbargoCd("");
        blankOrdBlockFromData(entityManager, data);
      }

      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason())
          && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && admin.getReqStatus() != null
          && admin.getReqStatus().equals(CMR_REQUEST_STATUS_PCR) && (rdcEmbargoCd != null && !StringUtils.isBlank(rdcEmbargoCd))
          && "D".equals(rdcEmbargoCd) && (dataEmbargoCd == null || StringUtils.isBlank(dataEmbargoCd))) {
        legacyCust.setEmbargoCd(rdcEmbargoCd);
        resetOrdBlockToData(entityManager, data);
      }
      if (CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && CMR_REQUEST_STATUS_PCR.equals(admin.getReqStatus())
          && "Wx".equals(admin.getProcessedFlag())) {
        legacyCust.setEmbargoCd("D");
        resetOrdBlockToData(entityManager, data);
        // changeImportIndForShareAddrTRCE(entityManager,
        // admin.getId().getReqId());
      }
    }

    legacyCust.setLocNo(landedCntry + "000");
    legacyCust.setAbbrevNm(StringUtils.isBlank(data.getAbbrevNm()) ? "" : data.getAbbrevNm());
    legacyCust.setAbbrevLocn(StringUtils.isBlank(data.getAbbrevLocn()) ? "" : data.getAbbrevLocn());
    legacyCust.setInacCd(StringUtils.isBlank(data.getInacCd()) ? "" : data.getInacCd());
    legacyCust.setMailingCond("");

    if (!StringUtils.isBlank(data.getCustPrefLang())) {
      if (SystemLocation.NORWAY.equals(data.getCmrIssuingCntry())) {
        if ("E".equals(data.getCustPrefLang())) {
          legacyCust.setLangCd("3");
        } else {
          legacyCust.setLangCd("1");
        }
      } else if (SystemLocation.SWEDEN.equals(data.getCmrIssuingCntry())) {
        if ("E".equals(data.getCustPrefLang())) {
          legacyCust.setLangCd("3");
        } else {
          legacyCust.setLangCd("S");
        }

      } else if (SystemLocation.DENMARK.equals(data.getCmrIssuingCntry())) {
        if ("E".equals(data.getCustPrefLang())) {
          legacyCust.setLangCd("3");
        } else {
          legacyCust.setLangCd("1");
        }
      } else if (SystemLocation.FINLAND.equals(data.getCmrIssuingCntry())) {
        if ("E".equals(data.getCustPrefLang())) {
          legacyCust.setLangCd("3");
        } else if ("U".equals(data.getCustPrefLang())) {
          legacyCust.setLangCd("1");
        } else {// V
          legacyCust.setLangCd("2");
        }
      }
    }

    if (!StringUtils.isBlank(data.getCustClass())) {
      if ("71".equals(data.getCustClass())) {
        legacyCust.setCustType("98");
      } else if ("85".equals(data.getCustClass())) {
        legacyCust.setCustType("97");
      } else if (Arrays.asList("33", "35", "43", "45", "46").contains(data.getCustClass())) {
        legacyCust.setCustType(data.getCustClass());
      } else {
        legacyCust.setCustType("");
      }
    }

    if (!StringUtils.isBlank(data.getCollectionCd())) {
      legacyCust.setCollectionCd(data.getCollectionCd());
    } else {
      legacyCust.setCollectionCd("");
    }

    // if (!StringUtils.isBlank(data.getAcAdminBo())) {
    // legacyCust.setAccAdminBo(data.getAcAdminBo());
    // } else {
    // legacyCust.setAccAdminBo("");
    // }

    if (!StringUtils.isBlank(data.getTaxCd1())) {
      legacyCust.setTaxCd(data.getTaxCd1());
    } else {
      legacyCust.setTaxCd("");
    }

    String oldMopa = StringUtils.isBlank(legacyCust.getModeOfPayment()) ? "" : legacyCust.getModeOfPayment();
    String newMopa = "";
    if (!StringUtils.isBlank(data.getModeOfPayment())) {
      if (SystemLocation.NORWAY.equals(data.getCmrIssuingCntry()) || SystemLocation.SWEDEN.equals(data.getCmrIssuingCntry())
          || SystemLocation.DENMARK.equals(data.getCmrIssuingCntry())) {
        if ("WCFI".equals(data.getModeOfPayment())) {
          newMopa = "R";
        } else if ("A001".equals(data.getModeOfPayment())) {
          newMopa = "";
        }
      } else if (SystemLocation.FINLAND.equals(data.getCmrIssuingCntry())) {
        if ("WCFI".equals(data.getModeOfPayment())) {
          newMopa = "R";
        } else if ("A001".equals(data.getModeOfPayment())) {
          newMopa = "";
        } else if ("P004".equals(data.getModeOfPayment())) {
          newMopa = "5";
        }
      }
    }
    boolean mopaChanged = !StringUtils.equals(oldMopa, newMopa);
    if ("C".equals(admin.getReqType()) || ("U".equals(admin.getReqType()) && mopaChanged)) {
      legacyCust.setModeOfPayment(newMopa);
    }

    if (!StringUtils.isEmpty(data.getVat())) {
      legacyCust.setVat(data.getVat());
    } else {
      legacyCust.setVat("");
    }

    if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
      legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
      legacyCust.setSalesRepNo(data.getRepTeamMemberNo());
    } else {
      legacyCust.setSalesGroupRep("");
      legacyCust.setSalesRepNo("");
    }

    String dataEmbargoCd = data.getEmbargoCd();
    if (dataEmbargoCd != null) {
      legacyCust.setEmbargoCd(dataEmbargoCd);
    } else {
      legacyCust.setEmbargoCd("");
    }

    if (!StringUtils.isBlank(data.getSubIndustryCd())) {
      legacyCust.setImsCd(data.getSubIndustryCd());
    } else {
      legacyCust.setImsCd("");
    }

    if (!StringUtils.isEmpty(data.getIsuCd()) && !StringUtils.isEmpty(data.getClientTier())) {
      legacyCust.setIsuCd(data.getIsuCd() + data.getClientTier());
    } else if (!StringUtils.isEmpty(data.getIsuCd()) && StringUtils.isEmpty(data.getClientTier())) {
      legacyCust.setIsuCd(data.getIsuCd() + "7");
    } else {
      legacyCust.setIsuCd("");
    }
    // CREATCMR-4293
    if (!StringUtils.isEmpty(data.getIsuCd())) {
      if (StringUtils.isEmpty(data.getClientTier())) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      }
    }

    String newSbo = "";
    if (SystemLocation.DENMARK.equals(data.getCmrIssuingCntry())) {
      newSbo = "3420ISU";
    } else if (SystemLocation.FINLAND.equals(data.getCmrIssuingCntry())) {
      if ("702".equals(data.getCountryUse())) {
        newSbo = "3450ISU";
      } else if ("702EE".equals(data.getCountryUse())) {
        newSbo = "0370ISU";
      } else if ("702LT".equals(data.getCountryUse())) {
        newSbo = "0390ISU";
      } else if ("702LV".equals(data.getCountryUse())) {
        newSbo = "0380ISU";
      }
    } else if (SystemLocation.SWEDEN.equals(data.getCmrIssuingCntry())) {
      newSbo = "3420ISU";
    } else if (SystemLocation.NORWAY.equals(data.getCmrIssuingCntry())) {
      int postCd = Integer.valueOf(StringUtils.isNumeric(mailPostCode.trim()) ? mailPostCode.trim() : "10001");

      if (!"NO".equals(landedCntry.trim())) {
        newSbo = "1000ISU";// CMR-1650 SBO value for cross
      } else if (postCd >= 0 && postCd < 4000) {
        newSbo = "1000ISU";
      } else if (postCd > 3999 && postCd < 5000) {
        newSbo = "4000ISU";
      } else if (postCd > 4999 && postCd < 7000) {
        newSbo = "5000ISU";
      } else if (postCd > 6999 && postCd < 10000) {
        newSbo = "7000ISU";
      }
    }
    legacyCust.setSbo(newSbo);
    legacyCust.setIbo(newSbo);
  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust cust, CMRRequestContainer cmrObjects, MassUpdtData muData) { // default
    LOG.debug("Mapping default Data values..");
    Data data = cmrObjects.getData();
    setDefaultLandedCountry(data);
    String issuingCntry = cust.getId().getSofCntryCode();

    if (!(StringUtils.isNotBlank(muData.getAbbrevNm()) || StringUtils.isNotBlank(muData.getSvcArOffice())
        || StringUtils.isNotBlank(muData.getCurrencyCd()) || StringUtils.isNotBlank(muData.getSubIndustryCd())
        || StringUtils.isNotBlank(muData.getSearchTerm()) || StringUtils.isNotBlank(muData.getSpecialTaxCd())
        || StringUtils.isNotBlank(muData.getNewEntpName1()) || StringUtils.isNotBlank(muData.getAbbrevLocn())
        || StringUtils.isNotBlank(muData.getMiscBillCd()) || StringUtils.isNotBlank(muData.getModeOfPayment())
        || StringUtils.isNotBlank(muData.getIsuCd()) || StringUtils.isNotBlank(muData.getRepTeamMemberNo())
        || StringUtils.isNotBlank(muData.getCompany()) || StringUtils.isNotBlank(muData.getEmail1())
        || StringUtils.isNotBlank(muData.getCollectionCd()) || StringUtils.isNotBlank(muData.getIsicCd()) || StringUtils.isNotBlank(muData.getVat())
        || StringUtils.isNotBlank(muData.getInacCd()))) {
      // dummyReq
      return;
    }
    if (!StringUtils.isBlank(muData.getAbbrevNm())) {
      if ("@".equals(muData.getAbbrevNm())) {
        cust.setAbbrevNm("");
      } else {
        cust.setAbbrevNm(muData.getAbbrevNm());
      }
    }

    // use svcArOffice to store custLang
    if (!StringUtils.isBlank(muData.getSvcArOffice())) {
      if (SystemLocation.NORWAY.equals(cust.getId().getSofCntryCode())) {
        if ("E".equals(muData.getSvcArOffice())) {
          cust.setLangCd("3");
        } else {
          cust.setLangCd("1");
        }
      } else if (SystemLocation.SWEDEN.equals(cust.getId().getSofCntryCode())) {
        if ("E".equals(muData.getSvcArOffice())) {
          cust.setLangCd("3");
        } else {
          cust.setLangCd("S");
        }

      } else if (SystemLocation.DENMARK.equals(cust.getId().getSofCntryCode())) {
        if ("E".equals(muData.getSvcArOffice())) {
          cust.setLangCd("3");
        } else {
          cust.setLangCd("1");
        }
      } else if (SystemLocation.FINLAND.equals(cust.getId().getSofCntryCode())) {
        if ("E".equals(muData.getSvcArOffice())) {
          cust.setLangCd("3");
        } else if ("U".equals(muData.getSvcArOffice())) {
          cust.setLangCd("1");
        } else {// V
          cust.setLangCd("2");
        }
      }
    }

    if (!StringUtils.isBlank(muData.getCurrencyCd())) {
      if (SystemLocation.DENMARK.equals(cust.getId().getSofCntryCode())) {
        if ("DKK".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("");
        } else if ("EUR".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("EU");
        } else if ("USD".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("US");
        }
      } else if (SystemLocation.FINLAND.equals(cust.getId().getSofCntryCode())) {
        if ("EUR".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("EU");
        } else if ("USD".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("US");
        }
      } else if (SystemLocation.NORWAY.equals(cust.getId().getSofCntryCode())) {
        if ("NOK".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("");
        } else if ("EUR".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("EU");
        } else if ("USD".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("US");
        }
      } else if (SystemLocation.SWEDEN.equals(cust.getId().getSofCntryCode())) {
        if ("SEK".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("");
        } else if ("EUR".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("EU");
        } else if ("USD".equals(muData.getCurrencyCd())) {
          cust.setCurrencyCd("US");
        }
      }
    }

    // if (!StringUtils.isBlank(muData.getSearchTerm())) {
    // if ("@".equals(muData.getSearchTerm())) {
    // cust.setAccAdminBo("");
    // } else {
    // cust.setAccAdminBo(muData.getSearchTerm());
    // }
    // }

    if (!StringUtils.isBlank(muData.getSpecialTaxCd())) {
      if ("@".equals(muData.getSpecialTaxCd())) {
        cust.setTaxCd("");
      } else {
        cust.setTaxCd(muData.getSpecialTaxCd());
      }
    }

    // leading Account number
    if (!StringUtils.isBlank(muData.getNewEntpName1())) {
      if ("@".equals(muData.getNewEntpName1())) {
        cust.setLeadingAccNo("");
      } else {
        cust.setLeadingAccNo(muData.getNewEntpName1() + cust.getMrcCd());
      }
    }

    if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
      cust.setAbbrevLocn(muData.getAbbrevLocn());
    }

    if (!StringUtils.isBlank(muData.getMiscBillCd())) {
      if ("@".equals(muData.getMiscBillCd())) {
        cust.setEmbargoCd("");
      } else {
        cust.setEmbargoCd(muData.getMiscBillCd());
      }
    }

    if (!StringUtils.isBlank(muData.getModeOfPayment())) {
      String oldMopa = StringUtils.isBlank(cust.getModeOfPayment()) ? "" : cust.getModeOfPayment();
      String newMopa = "";
      if (SystemLocation.NORWAY.equals(issuingCntry) || SystemLocation.SWEDEN.equals(issuingCntry) || SystemLocation.DENMARK.equals(issuingCntry)) {
        if ("WCFI".equals(muData.getModeOfPayment())) {
          newMopa = "R";
        } else if ("A001".equals(muData.getModeOfPayment())) {
          newMopa = "";
        }
      } else if (SystemLocation.FINLAND.equals(issuingCntry)) {
        if ("WCFI".equals(muData.getModeOfPayment())) {
          newMopa = "R";
        } else if ("A001".equals(muData.getModeOfPayment())) {
          newMopa = "";
        } else if ("P004".equals(muData.getModeOfPayment())) {
          newMopa = "5";
        }
      }
      if (!StringUtils.equals(oldMopa, newMopa)) {
        cust.setModeOfPayment(newMopa);
      }
    }

    String isuClientTier;
    isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
        + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
    if (isuClientTier != null && isuClientTier.endsWith("@")) {
      cust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : cust.getIsuCd().substring(0, 2)) + "7");
    } else if (isuClientTier != null && isuClientTier.length() == 3) {
      cust.setIsuCd(isuClientTier);
    }

    // if (data.getCmrIssuingCntry().equals("702")) {
    // if (data.getCountryUse().equals("702")) {
    // if (isuClientTier != null && ("5K".equals(muData.getIsuCd()) ||
    // "04".equals(muData.getIsuCd()))) {
    // cust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ?
    // muData.getIsuCd() : cust.getIsuCd().substring(0, 2)) + "7");
    // }
    // } else if (Arrays.asList("5K", "21", "8B").contains(muData.getIsuCd())) {
    // cust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ?
    // muData.getIsuCd() : cust.getIsuCd().substring(0, 2)) + "7");
    // }
    // }

    if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
      if ("@".equals(muData.getRepTeamMemberNo())) {
        cust.setSalesRepNo("");
        cust.setSalesGroupRep("");
      } else {
        cust.setSalesRepNo(muData.getRepTeamMemberNo());
        cust.setSalesGroupRep(muData.getRepTeamMemberNo());
      }
    }

    if (!StringUtils.isBlank(muData.getCompany())) {
      if ("@".equals(muData.getCompany())) {
        cust.setEnterpriseNo("");
      } else {
        cust.setEnterpriseNo(muData.getCompany());
      }
    }

    // Email1 used to store phone
    if (!StringUtils.isBlank(muData.getEmail1())) {
      if ("@".equals(muData.getEmail1())) {
        cust.setTelNoOrVat("");
      } else {
        cust.setTelNoOrVat(muData.getEmail1());
      }
    }

    if (!StringUtils.isBlank(muData.getCollectionCd())) {
      if ("@@@@@@".equals(muData.getCollectionCd())) {
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

    if (!StringUtils.isBlank(muData.getInacCd())) {
      if ("@@@@".equals(muData.getInacCd())) {
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

  }

  private void resetOrdBlockToData(EntityManager entityManager, Data data) {
    data.setOrdBlk("88");
    data.setEmbargoCd("D");
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
    LOG.debug("Nordics -- Searching for ADDR records for Request " + reqId);
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
    Data data = cmrObjects.getData();
    List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
    MassUpdtData muData = cmrObjects.getMassUpdateData();
    Admin admin = cmrObjects.getAdmin();
    CmrtCust legacyCust = legacyObjects.getCustomer();

    // leading Account number
    if ("M".equals(admin.getReqType())) {
      // leading Account number
      if (!StringUtils.isBlank(muData.getNewEntpName1())) {
        if ("@".equals(muData.getNewEntpName1())) {
          legacyCust.setLeadingAccNo("");
        } else {
          legacyCust.setLeadingAccNo(muData.getNewEntpName1() + legacyCust.getMrcCd());
        }
      }
    } else {
      if (StringUtils.isNotBlank(data.getCompany()) && data.getCompany().length() > 4) {
        legacyCust.setLeadingAccNo(data.getCompany() + legacyCust.getMrcCd());
      } else {
        legacyCust.setLeadingAccNo(legacyCust.getId().getCustomerNo() + legacyCust.getMrcCd());
      }
    }

    // int seqStartForRequiredAddr = 1;
    // for (int i = 0; i < addrList.size(); i++) {
    // Addr addr = addrList.get(i);
    // String addrSeq = addr.getId().getAddrSeq();
    // String addrType = addr.getId().getAddrType();
    //
    // if (addrSeq.equals("1")) {
    //
    // updateRequiredAddresses(entityManager, reqId,
    // addrList.get(i).getId().getAddrType(),
    // legacyAddrList.get(i).getId().getAddrNo(),
    // changeSeqNo(seqStartForRequiredAddr++), legacyObjects, i);
    // }
    //
    // }

    String reqType = cmrObjects.getAdmin().getReqType();
    long requestId = cmrObjects.getAdmin().getId().getReqId();

    Map<String, String> addrSeqToAddrUseMap = new HashMap<String, String>();
    addrSeqToAddrUseMap = mapSeqNoToAddrUse(getAddrLegacy(entityManager, String.valueOf(requestId)));

    LOG.debug("LEGACY -- Nordics OVERRIDE transformOtherData");
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
    return true;
  }

  @Override
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {

    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();

    if (!StringUtils.isBlank(data.getEngineeringBo())) {
      legacyCustExt.setAcAdminBo(data.getEngineeringBo());
    }

  }

  @Override
  public void transformLegacyCustomerExtDataMassUpdate(EntityManager entityManager, CmrtCustExt custExt, CMRRequestContainer cmrObjects,
      MassUpdtData muData, String cmr) throws Exception {
    // if (!StringUtils.isBlank(muData.getSearchTerm())) {
    // if ("@".equals(muData.getSearchTerm())) {
    // custExt.setAcAdminBo("");
    // } else {
    // custExt.setAcAdminBo(muData.getSearchTerm());
    // }
    // }

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
    LOG.debug("Nordics - Assigning address sequence " + newSeq + " to " + addrType + " address.");
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

  public void changeImportIndForShareAddrTRCE(EntityManager entityManager, long reqId) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("ND.ADDR.Y.IMPORTIND"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();
  }

}
