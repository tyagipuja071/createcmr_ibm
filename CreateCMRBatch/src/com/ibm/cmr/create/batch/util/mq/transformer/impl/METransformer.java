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
 * {@link MessageTransformer} implementation for ME
 * 
 * @author Paul
 * 
 */
public class METransformer extends EMEATransformer {

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

  private static final List<String> ME_DUPCOUNTRY_LIST = Arrays.asList(SystemLocation.UNITED_ARAB_EMIRATES, SystemLocation.ABU_DHABI,
      SystemLocation.BAHRAIN, SystemLocation.OMAN, SystemLocation.KUWAIT, SystemLocation.QATAR, SystemLocation.JORDAN, SystemLocation.LEBANON,
      SystemLocation.IRAQ);

  private static final Logger LOG = Logger.getLogger(EMEATransformer.class);

  public static String DEFAULT_LANDED_COUNTRY = "AE";
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

  public METransformer(String issuingCntry) {
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
      return "Sold-To";
    case "ZD01":
      return "Ship-To";
    case "ZI01":
      return "Install-at";
    case "ZS02":
      return "Mail-To";
    case "ZP02":
      return "G address (Address in local language)";
    case "ZD02":
      return "IGF Ship-To";
    case "ZP03":
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
      return "Sold-To";
    case "ZD01":
      return "Ship-To";
    case "ZI01":
      return "Install-at";
    case "ZS02":
      return "Mail-To";
    case "ZP02":
      return "G address (Address in local language)";
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
    if ("620".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "BH";
    }
    if ("642".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "MA";
    }
    if ("677".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "AE";
    }
    if ("680".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "AE";
    }
    if ("752".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "IQ";
    }
    if ("762".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "JO";
    }
    if ("767".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "KW";
    }
    if ("768".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "LB";
    }
    if ("772".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "LY";
    }
    if ("805".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "OM";
    }
    if ("808".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "PK";
    }
    if ("823".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "QA";
    }
    if ("832".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "SA";
    }
    if ("849".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "YE";
    }
    if ("850".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "SY";
    }
    if ("865".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "EG";
    }
    if ("729".equals(data.getCmrIssuingCntry())) {
      DEFAULT_LANDED_COUNTRY = "TN";
    }
  }

  /**
   * Checks if this is a cross-border scenario
   * 
   * @param addr
   * @return
   */
  protected boolean isCrossBorder(Addr addr) {
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
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZD02:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZP02:
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_G;
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
    case MQMsgConstants.ADDR_ZP02:
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_G;
    case MQMsgConstants.ADDR_ZP03:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
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
    boolean update = "U".equals(handler.adminData.getReqType());
    boolean crossBorder = isCrossBorder(addrData);

    LOG.debug("Legacy Direct -Handling Address for " + (update ? "update" : "create") + " request.");

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";
    String addrType = addrData.getId().getAddrType();
    String phone = "";
    String addrLineT = "";

    LOG.trace("Handling " + (update ? "update" : "create") + " request.");

    // line1
    line1 = addrData.getCustNm1();

    if (!StringUtils.isBlank(addrData.getCustNm2())) {
      line2 = addrData.getCustNm2();
    } else {
      line2 = "";
    }

    String pobox = addrData.getPoBox();
    String name3 = addrData.getCustNm3();
    if (StringUtils.isNotBlank(name3)) {
      line3 = name3;
    } else if (StringUtils.isNotBlank(pobox)) {
      line3 = "ZP02".equals(addrType) ? "" : "PO BOX ";
      line3 = line3 + pobox;
    }

    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 = addrData.getAddrTxt();
    } else {
      line4 = "";
    }

    // Dept + Postal code + City
    line5 = (addrData.getPostCd() == null ? "" : addrData.getPostCd()) + " " + (addrData.getCity1() == null ? "" : addrData.getCity1());

    // if (!StringUtils.isBlank(addrData.getPoBox())) {
    legacyAddr.setPoBox(addrData.getPoBox());
    // }

    if (SystemLocation.JORDAN.equals(cmrData.getCmrIssuingCntry())) {
      String cntryUse = cmrData.getCountryUse();
      if ("ZP02".equals(addrData.getId().getAddrType())) {
        if (!StringUtils.isBlank(addrData.getBldg())) {
          if (addrData.getBldg().length() <= 50) {
            line6 = addrData.getBldg();
          } else {
            line6 = addrData.getBldg().substring(0, 49);
          }
        }
      } else {
        if (!StringUtils.isBlank(cntryUse)) {
          if ("762PS".equals(cntryUse)) {
            line6 = "Palestine";
          } else {
            if (!StringUtils.isBlank(addrData.getLandCntry())) {
              line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
            } else {
              line6 = "";
            }
          }
        }
      }

    } else if (SystemLocation.PAKISTAN.equals(cmrData.getCmrIssuingCntry())) {
      String cntryUse = cmrData.getCountryUse();

      if ("ZP02".equals(addrData.getId().getAddrType())) {
        if (!StringUtils.isBlank(addrData.getBldg())) {
          if (addrData.getBldg().length() <= 50) {
            line6 = addrData.getBldg();
          } else {
            line6 = addrData.getBldg().substring(0, 49);
          }
        }
      } else {
        if (!StringUtils.isBlank(cntryUse)) {
          if ("808AF".equals(cntryUse)) {
            line6 = "Afganistan";
          } else {
            if (!StringUtils.isBlank(addrData.getLandCntry())) {
              line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
            } else {
              line6 = "";
            }
          }
        }
      }
    } else {
      if ("ZP02".equals(addrData.getId().getAddrType())) {
        if (!StringUtils.isBlank(addrData.getBldg())) {
          if (addrData.getBldg().length() <= 50) {
            line6 = addrData.getBldg();
          } else {
            line6 = addrData.getBldg().substring(0, 49);
          }
        }
      } else {
        if (!StringUtils.isBlank(addrData.getLandCntry())) {
          line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
        } else {
          line6 = "";
        }
      }
    }

    // if (!StringUtils.isBlank(addrData.getCustPhone())) {
    // phone = addrData.getCustPhone().trim();
    // } else {
    // phone = "";
    // }

    if (!StringUtils.isBlank(addrData.getTaxOffice())) {
      addrLineT = addrData.getTaxOffice();
    } else {
      addrLineT = "";
    }

    legacyAddr.setItCompanyProvCd(!StringUtils.isBlank(addrData.getStateProv()) ? addrData.getStateProv() : "");

    legacyAddr.setAddrLine1(line1);
    legacyAddr.setAddrLine2(line2);
    legacyAddr.setAddrLine3(line3);
    legacyAddr.setAddrLine4(line4);
    legacyAddr.setAddrLine5(line5);
    legacyAddr.setCity(addrData.getCity1());
    legacyAddr.setZipCode(addrData.getPostCd());
    legacyAddr.setAddrLine6(line6);
    legacyAddr.setStreet(line4);
    legacyAddr.setAddrPhone(phone);
    legacyAddr.setAddrLineT(addrLineT);
    legacyAddr.setDistrict(addrData.getDept());
    // CMR-4937
    legacyAddr.setAddrLineU("");

  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    System.out.println("_custSubGrp = " + custSubGrp);

    LOG.debug("Set max and min range of cmrNo..");
    // if (_custSubGrp == "INTER" || _custSubGrp == "XINT") {
    if (custSubGrp.contains("IN")) {
      if (!StringUtils.isBlank(data.getAbbrevNm()) && data.getAbbrevNm().startsWith("DUMMY")) {
        generateCMRNoObj.setMin(985001);
        generateCMRNoObj.setMax(985999);
      } else {
        generateCMRNoObj.setMin(990300);
        generateCMRNoObj.setMax(999990);
      }
    } else if (custSubGrp.contains("BP") || custSubGrp.contains("BUS")) {
      generateCMRNoObj.setMin(1000);
      generateCMRNoObj.setMax(9999);
      LOG.debug("that is ME BP CMR");
    } else {
      generateCMRNoObj.setMin(15000);
      generateCMRNoObj.setMax(984880);
    }

    if (custSubGrp.contains("IN")) {
      if (!StringUtils.isBlank(data.getAbbrevNm()) && data.getAbbrevNm().startsWith("DUMMY")) {
        if ("832".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985901);
          generateCMRNoObj.setMax(985999);
        } else if ("677".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985001);
          generateCMRNoObj.setMax(985082);
        } else if ("680".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985083);
          generateCMRNoObj.setMax(985164);
        } else if ("805".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985165);
          generateCMRNoObj.setMax(985246);
        } else if ("849".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985247);
          generateCMRNoObj.setMax(985328);
        } else if ("620".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985329);
          generateCMRNoObj.setMax(985410);
        } else if ("767".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985411);
          generateCMRNoObj.setMax(985492);
        } else if ("823".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985493);
          generateCMRNoObj.setMax(985574);
        } else if ("772".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985575);
          generateCMRNoObj.setMax(985656);
        } else if ("762".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985657);
          generateCMRNoObj.setMax(985738);
        } else if ("768".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985739);
          generateCMRNoObj.setMax(985820);
        } else if ("752".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(985821);
          generateCMRNoObj.setMax(985900);
        }
      } else {
        if ("832".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(999001);
          generateCMRNoObj.setMax(999999);
        } else if ("677".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(990000);
          generateCMRNoObj.setMax(990820);
        } else if ("680".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(990821);
          generateCMRNoObj.setMax(991640);
        } else if ("805".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(991641);
          generateCMRNoObj.setMax(992460);
        } else if ("849".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(992461);
          generateCMRNoObj.setMax(993280);
        } else if ("620".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(993281);
          generateCMRNoObj.setMax(994100);
        } else if ("767".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(994101);
          generateCMRNoObj.setMax(994290);
        } else if ("823".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(994291);
          generateCMRNoObj.setMax(995740);
        } else if ("772".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(995741);
          generateCMRNoObj.setMax(996560);
        } else if ("762".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(996561);
          generateCMRNoObj.setMax(997380);
        } else if ("768".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(997381);
          generateCMRNoObj.setMax(998200);
        } else if ("752".equals(data.getCmrIssuingCntry())) {
          generateCMRNoObj.setMin(998201);
          generateCMRNoObj.setMax(999000);
        }
      }
    } else if (custSubGrp.contains("BP") || custSubGrp.contains("BUS")) {
      if ("832".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(8801);
        generateCMRNoObj.setMax(9999);
      } else if ("677".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(1000);
        generateCMRNoObj.setMax(1725);
      } else if ("680".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(1726);
        generateCMRNoObj.setMax(2450);
      } else if ("805".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(2451);
        generateCMRNoObj.setMax(3175);
      } else if ("849".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(3176);
        generateCMRNoObj.setMax(3900);
      } else if ("620".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(3901);
        generateCMRNoObj.setMax(4625);
      } else if ("767".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(4626);
        generateCMRNoObj.setMax(5350);
      } else if ("823".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(5351);
        generateCMRNoObj.setMax(6075);
      } else if ("772".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(6076);
        generateCMRNoObj.setMax(6800);
      } else if ("762".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(6801);
        generateCMRNoObj.setMax(7525);
      } else if ("768".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(7526);
        generateCMRNoObj.setMax(8250);
      } else if ("752".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(8251);
        generateCMRNoObj.setMax(8800);
      }
      LOG.debug("that is ME BP CMR");
    } else {
      if ("832".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(900001);
        generateCMRNoObj.setMax(980000);
      } else if ("677".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(10000);
        generateCMRNoObj.setMax(90000);
      } else if ("680".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(90001);
        generateCMRNoObj.setMax(171000);
      } else if ("805".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(171001);
        generateCMRNoObj.setMax(252000);
      } else if ("849".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(252001);
        generateCMRNoObj.setMax(333000);
      } else if ("620".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(333001);
        generateCMRNoObj.setMax(414000);
      } else if ("767".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(414001);
        generateCMRNoObj.setMax(495000);
      } else if ("823".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(495001);
        generateCMRNoObj.setMax(576000);
      } else if ("772".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(576001);
        generateCMRNoObj.setMax(657000);
      } else if ("762".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(657001);
        generateCMRNoObj.setMax(738000);
      } else if ("768".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(738001);
        generateCMRNoObj.setMax(819000);
      } else if ("752".equals(data.getCmrIssuingCntry())) {
        generateCMRNoObj.setMin(819001);
        generateCMRNoObj.setMax(900000);
      }
      LOG.debug("that is ME BP CMR");
    }

    if ("Y".equals(data.getDupCmrIndc()) || "677, 680, 805, 849, 620, 767, 823, 772, 762, 768, 832, 752".indexOf(data.getCmrIssuingCntry()) > -1) {
      generateCMRNoObj.setLoc2("675");
    }
  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr addr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {
    legacyAddr.setForUpdate(true);

    if (!StringUtils.isBlank(addr.getCustNm1())) {
      legacyAddr.setAddrLine1(addr.getCustNm1());

    }

    if (!StringUtils.isBlank(addr.getCustNm2())) {
      if ("@".equals(addr.getCustNm2())) {
        legacyAddr.setAddrLine2("");
      } else {
        legacyAddr.setAddrLine2(addr.getCustNm2());

      }
    }

    if (!StringUtils.isBlank(addr.getAddrTxt())) {
      legacyAddr.setStreet(addr.getAddrTxt());
      legacyAddr.setAddrLine4(addr.getAddrTxt());

    }

    String pobox = addr.getPoBox();
    String name3 = addr.getCustNm3();

    if (!StringUtils.isBlank(name3)) {
      if ("@".equals(name3)) {
        legacyAddr.setAddrLine3("");
      } else {
        legacyAddr.setAddrLine3(name3);
      }
    } else if (!StringUtils.isBlank(pobox)) {
      String line3 = "ZP02".equals(addr.getAddrTxt()) ? "" : "PO BOX ";
      line3 = line3 + pobox;
      if ("@".equals(pobox)) {
        legacyAddr.setAddrLine3(line3);
      } else {
        legacyAddr.setAddrLine3(line3);
      }
    }

    if (!StringUtils.isBlank(addr.getPoBox())) {
      if ("@".equals(addr.getPoBox())) {
        legacyAddr.setPoBox("");
      } else {
        legacyAddr.setPoBox(addr.getPoBox());
      }
    }
    // legacy addr line5 is set in order district+postCd+City
    StringBuilder addrLine5 = new StringBuilder();

    if (!StringUtils.isBlank(addr.getDept())) {
      if ("@".equals(addr.getDept())) {
        legacyAddr.setDistrict("");
        // addrLine5.append(" ");
      } else {
        addrLine5.append(addr.getDept() + " ");
        legacyAddr.setDistrict(addr.getDept());

      }
    }

    if (!StringUtils.isBlank(addr.getPostCd())) {
      if ("@".equals(addr.getPostCd())) {
        legacyAddr.setZipCode("");
        // addrLine5.append(" ");
      } else {
        legacyAddr.setZipCode(addr.getPostCd());
        addrLine5.append(addr.getPostCd() + " ");
      }
    }

    if (!StringUtils.isBlank(addr.getCity1())) {
      if ("@".equals(addr.getCity1())) {
        legacyAddr.setCity("");
        // addrLine5.append(" ");
      } else {
        legacyAddr.setCity(addr.getCity1());
        addrLine5.append(addr.getCity1() + " ");
      }
    }

    if (!StringUtils.isBlank(addr.getCustLangCd())) {
      legacyAddr.setLanguage(addr.getCustLangCd());

    }

    if (!StringUtils.isBlank(addrLine5.toString())) {
      legacyAddr.setAddrLine5(addrLine5.toString());

    }

    if ("ZP02".equals(addr.getId().getAddrType())) {
      if (!StringUtils.isBlank(addr.getDivn())) {
        if ("@".equals(addr.getDivn())) {
          legacyAddr.setAddrLine6("");
        } else {
          legacyAddr.setAddrLine6(addr.getDivn());
        }
      }
    } else {
      if (!StringUtils.isBlank(addr.getLandCntry())) {
        legacyAddr.setAddrLine6(LandedCountryMap.getCountryName(addr.getLandCntry()));
      }
    }

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
    String landedCntry = "";

    setDefaultLandedCountry(data);
    formatDataLines(dummyHandler);

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {

      legacyCust.setAccAdminBo("");
      legacyCust.setCeDivision("");
      // CMR-5993
      String cntry = legacyCust.getId().getSofCntryCode().trim();
      String sales_Rep_ID = cntry + cntry;
      if ((SCENARIO_TYPE_SBM.equals(data.getCustGrp()) || SCENARIO_TYPE_GBM.equals(data.getCustGrp()))
          && Arrays.asList(ME_GBMSBM_COUNTRIES).contains(cntry)) {
        sales_Rep_ID = "530530";
      }
      legacyCust.setSalesGroupRep(sales_Rep_ID);
      legacyCust.setSalesRepNo(sales_Rep_ID);
      legacyCust.setDcRepeatAgreement("0");
      legacyCust.setLeasingInd("0");
      legacyCust.setAuthRemarketerInd("0");
      legacyCust.setCeDivision("2");
      legacyCust.setLangCd("");
      legacyCust.setDeptCd("");
      legacyCust.setCurrencyCd("");
      legacyCust.setTaxCd("");
      legacyCust.setOverseasTerritory("");
      legacyCust.setInvoiceCpyReqd("");
      legacyCust.setCustType("");

      if (SystemLocation.ABU_DHABI.equals(data.getCmrIssuingCntry()) && !StringUtils.isBlank(data.getBpAcctTyp())) {
        legacyCust.setCustType(data.getBpAcctTyp());
      } else {
        legacyCust.setCustType("N");
      }

      // extract the phone from billing as main phone
      for (Addr addr : cmrObjects.getAddresses()) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          legacyCust.setTelNoOrVat(addr.getCustPhone());
          landedCntry = addr.getLandCntry();
          break;
        }
      }

      if (data.getCustSubGrp().contains("CO") || data.getCustSubGrp().contains("TH") || data.getCustSubGrp().contains("TP")
          || data.getCustSubGrp().contains("PRI") || data.getCustSubGrp().contains("PC")) {// commerical
        if (!StringUtils.isBlank(data.getEnterprise())) {
          legacyCust.setEnterpriseNo(data.getEnterprise());
        } else {
          legacyCust.setEnterpriseNo("");
        }
      } else if (data.getCustSubGrp().contains("IN")) {// internal
        legacyCust.setEnterpriseNo("");
      } else {// bp
        legacyCust.setEnterpriseNo("");
      }

      if (!StringUtils.isBlank(data.getPhone1())) {
        legacyCust.setTelNoOrVat(data.getPhone1());
      } else {
        legacyCust.setTelNoOrVat("");
      }

      if (!StringUtils.isBlank(data.getTaxCd1())) {
        legacyCust.setBankAcctNo(data.getTaxCd1());
      } else {
        legacyCust.setBankAcctNo("");
      }

      if ("762PS".equals(data.getCountryUse())) {
        legacyCust.setRealCtyCd("762");
      } else if ("808AF".equals(data.getCountryUse())) {
        legacyCust.setRealCtyCd("614");
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

      if ("762PS".equals(data.getCountryUse())) {
        legacyCust.setRealCtyCd("762");
      } else if ("808AF".equals(data.getCountryUse())) {
        legacyCust.setRealCtyCd("614");
      }

      if (!StringUtils.isBlank(data.getEnterprise())) {
        legacyCust.setEnterpriseNo(data.getEnterprise());
      } else {
        legacyCust.setEnterpriseNo("");
      }

      if (SystemLocation.ABU_DHABI.equals(data.getCmrIssuingCntry()) && !StringUtils.isBlank(data.getBpAcctTyp())) {
        legacyCust.setCustType(data.getBpAcctTyp());
      }

      // CMR-5993
      String cntry = legacyCust.getId().getSofCntryCode().trim();
      String sales_Rep_ID = cntry + cntry;
      if ((SCENARIO_TYPE_SBM.equals(data.getCustGrp()) || SCENARIO_TYPE_GBM.equals(data.getCustGrp()))
          && Arrays.asList(ME_GBMSBM_COUNTRIES).contains(cntry)) {
        sales_Rep_ID = "530530";
      }

      if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
        legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
      } else {
        legacyCust.setSalesGroupRep(sales_Rep_ID);
      }

      if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
        legacyCust.setSalesRepNo(data.getRepTeamMemberNo());
      } else {
        legacyCust.setSalesRepNo(sales_Rep_ID);
      }

      if (!StringUtils.isBlank(data.getPhone1())) {
        legacyCust.setTelNoOrVat(data.getPhone1());
      } else {
        legacyCust.setTelNoOrVat("");
      }

      if (!StringUtils.isBlank(data.getTaxCd1())) {
        legacyCust.setBankAcctNo(data.getTaxCd1());
      } else {
        legacyCust.setBankAcctNo("");
      }

      String dataEmbargoCd = data.getEmbargoCd();
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin);
      String cof = data.getCommercialFinanced();
      if (!StringUtils.isBlank(cof)) {
        if ("R".equals(cof) || "S".equals(cof) || "T".equals(cof) || "C".equals(cof)) {
          legacyCust.setModeOfPayment(cof);
        }
      } else {
        legacyCust.setModeOfPayment("");
      }

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
        LOG.debug("CEETransformer legacyCust.setEmbargoCd - blank");
        legacyCust.setEmbargoCd("");
        blankOrdBlockFromData(entityManager, data);
        LOG.debug("CEETransformer - resetOrdBlockToData - data.getOrdBlk -" + data.getOrdBlk());
        LOG.debug("CEETransformer - resetOrdBlockToData - data.getEmbargoCd -" + data.getEmbargoCd());
      }

      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason())
          && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && admin.getReqStatus() != null
          && admin.getReqStatus().equals(CMR_REQUEST_STATUS_PCR) && (rdcEmbargoCd != null && !StringUtils.isBlank(rdcEmbargoCd))
          && "E".equals(rdcEmbargoCd) && (dataEmbargoCd == null || StringUtils.isBlank(dataEmbargoCd))) {
        LOG.debug("CEETransformer legacyCust.setEmbargoCd - " + rdcEmbargoCd);
        legacyCust.setEmbargoCd(rdcEmbargoCd);
        resetOrdBlockToData(entityManager, data);
        LOG.debug("CEETransformer - resetOrdBlockToData - data.getOrdBlk -" + data.getOrdBlk());
        LOG.debug("CEETransformer - resetOrdBlockToData - data.getEmbargoCd -" + data.getEmbargoCd());
      }
      if (CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && CMR_REQUEST_STATUS_PCR.equals(admin.getReqStatus())
          && "Wx".equals(admin.getProcessedFlag())) {
        LOG.debug("CEETransformer legacyCust.setEmbargoCd - E ");
        legacyCust.setEmbargoCd("E");
        resetOrdBlockToData(entityManager, data);
        LOG.debug("CEETransformer - resetOrdBlockToData - data.getOrdBlk -" + data.getOrdBlk());
        LOG.debug("CEETransformer - resetOrdBlockToData - data.getEmbargoCd -" + data.getEmbargoCd());
      }
    }

    if (!StringUtils.isBlank(data.getSubIndustryCd())) {
      legacyCust.setLocNo(legacyCust.getId().getSofCntryCode() + data.getSubIndustryCd());
    }

    String dataEmbargoCd = data.getEmbargoCd();
    if (dataEmbargoCd != null) {
      legacyCust.setEmbargoCd(dataEmbargoCd);
    } else {
      legacyCust.setEmbargoCd("");
    }

    String cebo = data.getEngineeringBo();
    if (!StringUtils.isBlank(cebo)) {
      legacyCust.setCeBo(cebo);
    } else {
      legacyCust.setCeBo("");
    }

    if (!StringUtils.isBlank(data.getSalesBusOffCd())) {
      String sbo = StringUtils.rightPad(data.getSalesBusOffCd(), 7, '0');
      if (sbo.length() < 7) {
        sbo = StringUtils.rightPad(sbo, 7, '0');
      }
      // CMR-5993
      if ((SCENARIO_TYPE_SBM.equals(data.getCustGrp()) || SCENARIO_TYPE_GBM.equals(data.getCustGrp()))
          && Arrays.asList(ME_GBMSBM_COUNTRIES).contains(legacyCust.getId().getSofCntryCode())) {
        sbo = "5300000";
      }
      legacyCust.setSbo(sbo);
      legacyCust.setIbo(sbo);
    } else {
      legacyCust.setSbo("");
      legacyCust.setIbo("");
    }

    // common data for C/U
    // formatted data
    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("AbbreviatedLocation"))) {
      legacyCust.setAbbrevLocn(dummyHandler.messageHash.get("AbbreviatedLocation"));
    }

    String vat = dummyHandler.cmrData.getVat();
    // not for internal scenario
    // if ("C".equals(admin.getReqType()) && !StringUtils.isEmpty(vat) &&
    // !data.getCustSubGrp().contains("IN") &&
    // !data.getCustSubGrp().contains("BP")
    // && !data.getVatExempt().equals("Y")) {
    // if ("821".equals(data.getCmrIssuingCntry())) {
    // if (vat.matches("^[A-Z]{2}.*")) {
    // String prefix = vat.substring(0, 2);
    // legacyCust.setVat(vat.replace(prefix, ""));
    // } else {
    // legacyCust.setVat(vat);
    // }
    // } else if
    // (("644,668,693,704,708,740,820,826").contains(data.getCmrIssuingCntry()))
    // {
    // if (vat.matches("^[A-Z]{2}.*")) {
    // legacyCust.setVat(vat);
    // } else {
    // String zs01AddrssCntry = null;
    // if (dummyHandler.currentAddresses != null) {
    // for (Addr addr : dummyHandler.currentAddresses) {
    // if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
    // zs01AddrssCntry = addr.getLandCntry();
    // break;
    // }
    // }
    // legacyCust.setVat(zs01AddrssCntry + vat);
    // }
    // }
    // } else {
    // if (!StringUtils.isEmpty(vat)) {
    // legacyCust.setVat(vat);
    // } else {
    // legacyCust.setVat("");
    // }
    //
    // }
    // } else {
    if (!StringUtils.isEmpty(vat)) {
      legacyCust.setVat(vat);
    } else {
      legacyCust.setVat("");
    }
    // }

    // if (!StringUtils.isEmpty(dummyHandler.messageHash.get("EconomicCode"))) {
    // legacyCust.setEconomicCd(dummyHandler.messageHash.get("EconomicCode"));
    // }

    if (!StringUtils.isEmpty(data.getIbmDeptCostCenter())) {
      legacyCust.setBankBranchNo(data.getIbmDeptCostCenter());
    }

    // remove DistrictCd
    // if (!StringUtils.isEmpty(data.getCollectionCd())) {
    // legacyCust.setDistrictCd(data.getCollectionCd().substring(0, 2));
    // }
    // legacyCust.setBankBranchNo(data.getIbmDeptCostCenter() != null ?
    // data.getIbmDeptCostCenter() : "");

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
      }
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
    if (!StringUtils.isBlank(muData.getSvcArOffice())) {
      if ("@".equals(muData.getSvcArOffice())) {
        cust.setLangCd("");
      } else {
        cust.setLangCd(muData.getSvcArOffice());
      }
    }

    if (SystemLocation.ABU_DHABI.equals(cust.getId().getSofCntryCode()) && !StringUtils.isBlank(muData.getCurrencyCd())) {
      if ("@".equals(muData.getCurrencyCd())) {
        cust.setCustType("");
      } else {
        cust.setCustType(muData.getCurrencyCd());
      }
    }

    if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
      cust.setLocNo(cust.getId().getSofCntryCode() + muData.getSubIndustryCd());
    }

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

    if (!StringUtils.isBlank(muData.getMiscBillCd())) {
      if ("@".equals(muData.getMiscBillCd())) {
        cust.setEmbargoCd("");
      } else {
        cust.setEmbargoCd(muData.getMiscBillCd());
      }
    }
    // we use RestrictTo to store CoF in muData
    if (!StringUtils.isBlank(muData.getRestrictTo())) {
      if ("@".equals(muData.getRestrictTo())) {
        cust.setModeOfPayment("");
      } else {
        cust.setModeOfPayment(muData.getRestrictTo());
      }
    }

    String isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
        + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
    if (isuClientTier != null && isuClientTier.endsWith("@")) {
      cust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : cust.getIsuCd().substring(0, 2)) + "7");
    } else if (isuClientTier != null && isuClientTier.length() == 3) {
      cust.setIsuCd(isuClientTier);
    }

    if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
      if ("@".equals(muData.getRepTeamMemberNo())) {
        cust.setSalesRepNo("");
        cust.setSalesGroupRep("");
      } else {
        cust.setSalesRepNo(muData.getRepTeamMemberNo());
        cust.setSalesGroupRep(muData.getRepTeamMemberNo());
      }
    }

    String cebo = "";
    if (!StringUtils.isBlank(muData.getCustNm2())) {
      cebo = muData.getCustNm2();
      // if (cebo.length() < 7) {
      // cebo = StringUtils.rightPad(cebo, 7, '0');
      // }
      if ("@".equals(muData.getCustNm2())) {
        cust.setCeBo("");
      } else {
        cust.setCeBo(cebo);
      }
    }

    if (!StringUtils.isBlank(muData.getCompany())) {
      if ("@".equals(muData.getCompany())) {
        cust.setEnterpriseNo("");
      } else {
        cust.setEnterpriseNo(muData.getCompany());
      }
    }

    // if (!cust.getId().getCustomerNo().startsWith("99") &&
    // !StringUtils.isBlank(muData.getCompany())) {
    // if ("@".equals(muData.getCompany())) {
    // cust.setEnterpriseNo("");
    // } else {
    // cust.setEnterpriseNo(muData.getCompany());
    // }
    // } else if (cust.getId().getCustomerNo().startsWith("99")) {
    // cust.setEnterpriseNo("");
    // }

    // Email1 used to store phone
    if (!StringUtils.isBlank(muData.getEmail1())) {
      if ("@".equals(muData.getEmail1())) {
        cust.setTelNoOrVat("");
      } else {
        cust.setTelNoOrVat(muData.getEmail1());
      }
    }

    // List<MassUpdtAddr> muaList = cmrObjects.getMassUpdateAddresses();
    // if (muaList != null && muaList.size() > 0) {
    // for (MassUpdtAddr mua : muaList) {
    // if ("ZP01".equals(mua.getId().getAddrType())) {
    // if (!StringUtils.isBlank(mua.getCustPhone())) {
    // if (DEFAULT_CLEAR_CHAR.equals(mua.getCustPhone())) {
    // cust.setTelNoOrVat("");
    // } else {
    // cust.setTelNoOrVat(mua.getCustPhone());
    // }
    // break;
    // }
    // }
    // }
    // }

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

    // SBO
    if (!StringUtils.isBlank(muData.getCustNm1())) {
      if ("@".equals(muData.getCustNm1())) {
        cust.setSbo("");
        cust.setIbo("");
      } else {
        String sbo = muData.getCustNm1();
        if (sbo.length() < 7) {
          sbo = StringUtils.rightPad(sbo, 7, '0');
        }
        cust.setSbo(sbo);
        cust.setIbo(sbo);
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
    if ("BH".equals(DEFAULT_LANDED_COUNTRY) || "MA".equals(DEFAULT_LANDED_COUNTRY) || "AE".equals(DEFAULT_LANDED_COUNTRY)
        || "KW".equals(DEFAULT_LANDED_COUNTRY) || "OM".equals(DEFAULT_LANDED_COUNTRY) || "QA".equals(DEFAULT_LANDED_COUNTRY)
        || "SA".equals(DEFAULT_LANDED_COUNTRY) || "GC".equals(DEFAULT_LANDED_COUNTRY)) {
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

    // Morocco use Phone3 to store ICE field
    if (SystemLocation.MOROCCO.equals(data.getCmrIssuingCntry()) && !StringUtils.isBlank(data.getPhone3())) {
      legacyCustExt.setiTaxCode(data.getPhone3());
    }
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

    if (!StringUtils.isBlank(muData.getNewEntpName1())) {
      if ("@".equals(muData.getNewEntpName1())) {
        custExt.setiTaxCode("");
      } else {
        if (muData.getNewEntpName1().length() > 9) {
          custExt.setiTaxCode(muData.getNewEntpName1().substring(0, 8));
        } else {
          custExt.setiTaxCode(muData.getNewEntpName1());
        }
      }
    }

    if (!StringUtils.isBlank(muData.getAffiliate())) {
      if ("@".equals(muData.getAffiliate())) {
        custExt.setTeleCovRep("");
      } else {
        custExt.setTeleCovRep(muData.getAffiliate());
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
    LOG.debug("ME - Assigning address sequence " + newSeq + " to " + addrType + " address.");
    q.executeSql();
  }

  @Override
  public String getDupCreationCountryId(EntityManager entityManager, String cntry, String cmrNo) {
    if (ME_DUPCOUNTRY_LIST.contains(cntry))
      return "675";
    else
      return "NA";
  }

  @Override
  public void getTargetCountryId(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, String cntry, String cmrNo) {
    if ("677, 680, 805, 849, 620, 767, 823, 772, 762, 768, 832, 752".indexOf(cntry) > -1) {
      generateCMRNoObj.setLoc2("675");
    }
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
