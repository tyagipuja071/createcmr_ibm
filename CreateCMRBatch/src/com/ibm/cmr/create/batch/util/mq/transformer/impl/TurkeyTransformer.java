/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

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
import com.ibm.cmr.create.batch.util.SingleQuoteAttributeOutputtter;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * {@link MessageTransformer} implementation for Turkey
 * 
 * @author Jeffrey Zamora
 * 
 */
public class TurkeyTransformer extends EMEATransformer {
  // public class TurkeyTransformer extends MessageTransformer {

  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode" };

  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZD01", "ZI01" };

  private static final Logger LOG = Logger.getLogger(EMEATransformer.class);

  public static final String DEFAULT_LANDED_COUNTRY = "TR";
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

  public static void main(String[] args) {
    String s = "TÜRKİYE";
    System.out.println(s);
    String s1 = "T\u00dcRK\u0130YE";
    System.out.println(s1);
    System.out.println((int) 'İ');
  }

  public TurkeyTransformer(String issuingCntry) {
    super(issuingCntry);

  }

  public TurkeyTransformer() {
    super(SystemLocation.TURKEY);

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
      if ("BUSPR".equals(custType) || "XBP".equals(custType)) {
        messageHash.put("MarketingResponseCode", "5");
        messageHash.put("ARemark", "YES");
        messageHash.put("CustomerType", "BP");
      } else if ("GOVRN".equals(custType)) {
        messageHash.put("CustomerType", "G");
      } else if ("INTER".equals(custType) || "XINT".equals(custType)) {
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

    String countryName = "TURKIYE";
    if (!crossBorder && MQMsgConstants.ADDR_ZP01.equals(addrData.getId().getAddrType())) {
      countryName = "T\u00dcRK\u0130YE";
    } else if (crossBorder) {
      countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());
    }

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

      char[] problematicCharList = new char[12];

      problematicCharList[0] = '\u00c7'; // Ç
      problematicCharList[1] = '\u00e7'; // ç
      problematicCharList[2] = '\u011e'; // Ğ
      problematicCharList[3] = '\u011f'; // ğ
      problematicCharList[4] = '\u0130'; // İ
      problematicCharList[5] = '\u0131'; // ı
      problematicCharList[6] = '\u00d6'; // Ö
      problematicCharList[7] = '\u00f6'; // ö
      problematicCharList[8] = '\u015e'; // Ş
      problematicCharList[9] = '\u015f'; // ş
      problematicCharList[10] = '\u00dc'; // Ü
      problematicCharList[11] = '\u00fc'; // ü

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

      for (String key : addressDataMap.keySet()) {
        for (char problematicChar : problematicCharList) {
          if (!(StringUtils.isEmpty(addressDataMap.get(key)))) {
            for (int i = 0; i < addressDataMap.get(key).length(); i++) {
              int index = addressDataMap.get(key).indexOf(problematicChar);
              if (index >= 0) {
                String data = null;
                switch (addressDataMap.get(key).charAt(index)) {
                case '\u00c7':// Ç
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'C');
                  addressDataMap.put(key, data);
                  break;
                case '\u00e7': // ç
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'c');
                  addressDataMap.put(key, data);
                  break;
                case '\u011e': // Ğ
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'G');
                  addressDataMap.put(key, data);
                  break;
                case '\u011f': // ğ
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'g');
                  addressDataMap.put(key, data);
                  break;
                case '\u0130': // İ
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'I');
                  addressDataMap.put(key, data);
                  break;
                case '\u0131': // ı
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'i');
                  addressDataMap.put(key, data);
                  break;
                case '\u00d6': // Ö
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'O');
                  addressDataMap.put(key, data);
                  break;
                case '\u00f6': // ö
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'o');
                  addressDataMap.put(key, data);
                  break;
                case '\u015e': // Ş
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'S');
                  addressDataMap.put(key, data);
                  break;
                case '\u015f': // ş
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 's');
                  addressDataMap.put(key, data);
                  break;
                case '\u00dc': // Ü
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'U');
                  addressDataMap.put(key, data);
                  break;
                case '\u00fc': // ü
                  data = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'u');
                  addressDataMap.put(key, data);
                  break;
                }
              }
            }
          }
        }
      }

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
      return "Address in local language";
    case "ZS01":
      return "Sold-To";
    case "ZD01":
      return "Ship-To";
    case "ZI01":
      return "Install-At";
    default:
      return "";

    // case "ZP01":
    // return "Mail";
    // case "ZS01":
    // return "Install";
    // case "ZD01":
    // return "Ship";
    // default:
    // return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Address in local language";
    case "ZS01":
      return "Sold-To";
    case "ZD01":
      return "Ship-To";
    case "ZI01":
      return "Install-At";
    default:
      return "";

    // case "ZP01":
    // return "Mailing";
    // case "ZS01":
    // return "Installing";
    // case "ZD01":
    // return "Shipping";
    // default:
    // return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.TURKEY;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "00001";
  }

  /**
   * Checks if this is a cross-border scenario
   * 
   * @param addr
   * @return
   */
  protected boolean isCrossBorder(Addr addr) {
    return !"TR".equals(addr.getLandCntry());
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZP01:
      // return MQMsgConstants.SOF_ADDRESS_USE_MAILING +
      // MQMsgConstants.SOF_ADDRESS_USE_BILLING;
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;

    // switch (addr.getId().getAddrType()) {
    // case MQMsgConstants.ADDR_ZP01:
    // return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    // case MQMsgConstants.ADDR_ZS01:
    // return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    // case MQMsgConstants.ADDR_ZD01:
    // return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    // case MQMsgConstants.ADDR_ZI01:
    // return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    // default:
    // return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  public String getAddressUseByType(String addrType) {
    switch (addrType) {
    case MQMsgConstants.ADDR_ZP01:
      // return MQMsgConstants.SOF_ADDRESS_USE_MAILING +
      // MQMsgConstants.SOF_ADDRESS_USE_BILLING;
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
  public XMLOutputter getXmlOutputter(Format format) {
    return new SingleQuoteAttributeOutputtter(format);
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

    line3 = addrData.getAddrTxt();

    if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
      line4 = addrData.getAddrTxt2();
    } else {
      line4 = "";
    }

    // Dept + Postal code + City
    line5 = addrData.getDept() + " " + addrData.getPostCd() + " " + addrData.getCity1();

    if (!StringUtils.isBlank(addrData.getLandCntry())) {
      line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    } else {
      line6 = "Turkey";
    }

    if (!StringUtils.isBlank(addrData.getCustPhone())) {
      phone = addrData.getCustPhone().trim();
    } else {
      phone = "";
    }

    if (!StringUtils.isBlank(addrData.getTaxOffice())) {
      addrLineT = addrData.getTaxOffice();
    } else {
      addrLineT = "";
    }

    // ZS01 Installing Address
    // if (MQMsgConstants.ADDR_ZS01.equals(addrType)) {
    //
    // line3 = "";
    //
    // // Street
    // if (!StringUtils.isBlank(addrData.getAddrTxt())) {
    // line4 = addrData.getAddrTxt();
    // }
    //
    // // Dept + Postal code + City
    // line5 = addrData.getDept() + " " + addrData.getPostCd() + " " +
    // addrData.getCity1();
    //
    // // Country Landed :"full country name" based on landed cty
    // if (!StringUtils.isBlank(addrData.getLandCntry())) {
    // line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    // }
    // }
    // // ZP01 Billing Address
    // if (MQMsgConstants.ADDR_ZP01.equals(addrType)) {
    //
    // line3 = addrData.getAddrTxt();
    // line4 = addrData.getAddrTxt2();
    //
    // // Dept + Postal code + City
    // line5 = addrData.getDept() + " " + addrData.getPostCd() + " " +
    // addrData.getCity1();
    //
    // if (!StringUtils.isBlank(addrData.getLandCntry())) {
    // line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    // }
    // }
    //
    // // Company Address
    // if (MQMsgConstants.ADDR_ZD01.equals(addrType)) {
    //
    // line3 = addrData.getAddrTxt();
    // line4 = addrData.getAddrTxt2();
    //
    // // Dept + Postal code + City
    // line5 = addrData.getDept() + " " + addrData.getPostCd() + " " +
    // addrData.getCity1();
    //
    // if (!StringUtils.isBlank(addrData.getLandCntry())) {
    // line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    // }
    // }

    if (addrData.getLandCntry().equals("TR")) {
      legacyAddr.setItCompanyProvCd(!StringUtils.isBlank(addrData.getStateProv()) ? addrData.getStateProv() : "");
    } else {
      legacyAddr.setItCompanyProvCd("");
    }

    legacyAddr.setAddrLine1(line1);
    legacyAddr.setAddrLine2(line2);
    legacyAddr.setAddrLine3(line3);
    legacyAddr.setAddrLine4(line4);
    legacyAddr.setAddrLine5(line5);
    legacyAddr.setAddrLine6(line6);
    legacyAddr.setAddrPhone(phone);
    legacyAddr.setAddrLineT(addrLineT);

  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    System.out.println("_custSubGrp = " + custSubGrp);

    LOG.debug("Set max and min range of cmrNo..");
    // if (_custSubGrp == "INTER" || _custSubGrp == "XINT") {
    if ("INTER".equals(custSubGrp) || "XINT".equals(custSubGrp)) {
      generateCMRNoObj.setMin(993110);
      generateCMRNoObj.setMax(998899);
      LOG.debug("that is TR INTER CMR");
    } else {
      generateCMRNoObj.setMin(330000);
      generateCMRNoObj.setMax(999999);
      LOG.debug("that is TR No INTER CMR");
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
      if ("@".equals(addr.getCustNm2())) {
        legacyAddr.setAddrLine2("");
      } else {
        legacyAddr.setAddrLine2(addr.getCustNm2());

        if (legacyFiscalAddr != null) {
          legacyFiscalAddr.setAddrLine2("CL" + addr.getCustNm2());
        }
      }
    }

    if (!StringUtils.isBlank(addr.getAddrTxt())) {
      legacyAddr.setStreet(addr.getAddrTxt());
      legacyAddr.setAddrLine4(addr.getAddrTxt());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setStreet(addr.getAddrTxt());
        legacyFiscalAddr.setAddrLine4(addr.getAddrTxt());
      }
    }

    if (!StringUtils.isBlank(addr.getCustNm4())) {
      legacyAddr.setAddrLine3(addr.getCustNm4());
      legacyAddr.setContact(addr.getCustNm4());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setAddrLine3("CL" + addr.getCustNm4());
        legacyFiscalAddr.setContact(addr.getCustNm4());
      }
    }

    if (!StringUtils.isBlank(addr.getAddrTxt2())) {
      legacyAddr.setAddrLine3(addr.getAddrTxt2());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setAddrLine3(addr.getAddrTxt2());
      }
    }

    // legacy addr line5 is set in order district+postCd+City
    StringBuilder addrLine5 = new StringBuilder();

    if (!StringUtils.isBlank(addr.getDept())) {
      legacyAddr.setDistrict(addr.getDept());
      addrLine5.append(addr.getDept() + " ");
      if ("@".equals(addr.getDept())) {
        legacyAddr.setDistrict("");
      } else {
        legacyAddr.setDistrict(addr.getDept());
      }
    }

    if (!StringUtils.isBlank(addr.getDept())) {
      legacyAddr.setContact(addr.getDept());
      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setDistrict(addr.getDept());
      }
    }

    if (!StringUtils.isBlank(addr.getPostCd())) {
      legacyAddr.setZipCode(addr.getPostCd());
      addrLine5.append(addr.getPostCd() + " ");

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setZipCode(addr.getPostCd());
      }

      if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(addr.getId().getAddrType()) && isCrossBorderForMass(addr, legacyAddr)) {
        handlePostCdSpecialLogic(cust, data, addr.getPostCd(), entityManager);
      }
    }

    if (!StringUtils.isBlank(addr.getCity1())) {
      legacyAddr.setCity(addr.getCity1());
      addrLine5.append(addr.getCity1());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setCity(addr.getCity1());
      }
    }

    if (!StringUtils.isBlank(addrLine5.toString())) {
      legacyAddr.setAddrLine5(addrLine5.toString());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setAddrLine5(addrLine5.toString());
      }
    }

    if (!StringUtils.isEmpty(addr.getLandCntry())) {
      legacyAddr.setAddrLine6(addr.getLandCntry());

      if (legacyFiscalAddr != null) {
        legacyFiscalAddr.setAddrLine6(addr.getLandCntry());
      }
    }

    // String poBox = addr.getPoBox();
    // if (!StringUtils.isEmpty(poBox) &&
    // !poBox.toUpperCase().startsWith("APTO")) {
    // poBox = " APTO " + poBox;
    // legacyAddr.setPoBox(addr.getPoBox());
    //
    // if (legacyFiscalAddr != null) {
    // legacyFiscalAddr.setPoBox(addr.getPoBox());
    // }
    // }

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
    // legacyObjects.addAddress(legacyAddr);

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
        // DTN: Commented because we are not passing phone numbers on
        // the
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

      // CMR-2279:Turkey-ISR set based on SBO
      if (!StringUtils.isBlank(data.getSalesBusOffCd())) {

        String sql = ExternalizedQuery.getSql("LEGACY.GET_ISR_BYSBO");
        PreparedQuery q = new PreparedQuery(entityManager, sql);
        q.setParameter("SBO", data.getSalesBusOffCd());
        q.setParameter("CNTRY", data.getCmrIssuingCntry());
        String isr = q.getSingleResult(String.class);
        if (!StringUtils.isBlank(isr)) {
          legacyCust.setSalesRepNo(isr);
          cmrObjects.getData().setRepTeamMemberNo(isr);
        } else {
          legacyCust.setSalesRepNo("");
          cmrObjects.getData().setRepTeamMemberNo("");
        }
      }

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

      // // CMR-2279:Turkey-ISR set based on SBO
      // if (!StringUtils.isBlank(data.getSalesBusOffCd())) {
      //
      // String sql = ExternalizedQuery.getSql("LEGACY.GET_ISR_BYSBO");
      // PreparedQuery q = new PreparedQuery(entityManager, sql);
      // q.setParameter("SBO", data.getSalesBusOffCd());
      // q.setParameter("CNTRY", data.getCmrIssuingCntry());
      // String isr = q.getSingleResult(String.class);
      // if (!StringUtils.isBlank(isr)) {
      // legacyCust.setSalesRepNo(isr);
      // cmrObjects.getData().setRepTeamMemberNo(isr);
      // } else {
      // legacyCust.setSalesRepNo("");
      // cmrObjects.getData().setRepTeamMemberNo("");
      // }
      // }

      String dataEmbargoCd = data.getEmbargoCd();
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin);

      // CMR-2093:Turkey - Requirement for CoF (Comercial Financed) field
      String cof = data.getCommercialFinanced();
      if (!StringUtils.isBlank(cof)) {
        if ("R".equals(cof) || "S".equals(cof) || "T".equals(cof)) {
          legacyCust.setModeOfPayment(cof);
        }
      } else {
        legacyCust.setModeOfPayment("");
      }

      String ecoCode = data.getEconomicCd();
      if (!StringUtils.isBlank(ecoCode)) {
        legacyCust.setEconomicCd(ecoCode);
      } else {
        legacyCust.setEconomicCd("");
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
    // For Liu hao move TR rule change
    legacyCust.setSalesRepNo(data.getSalesTeamCd());
    legacyCust.setSalesGroupRep(data.getSalesTeamCd());

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

    if (!StringUtils.isEmpty(data.getIbmDeptCostCenter())) {
      legacyCust.setBankBranchNo(data.getIbmDeptCostCenter());
    }
    if (!StringUtils.isEmpty(data.getCollectionCd())) {
      legacyCust.setDistrictCd(data.getCollectionCd().substring(0, 2));
    }
    // legacyCust.setBankBranchNo(data.getIbmDeptCostCenter() != null ?
    // data.getIbmDeptCostCenter() : "");
  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust cust, CMRRequestContainer cmrObjects, MassUpdtData muData) { // default
    LOG.debug("Mapping default Data values..");

    if (!StringUtils.isBlank(muData.getAbbrevNm())) {
      cust.setAbbrevNm(muData.getAbbrevNm());
    }

    if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
      cust.setAbbrevLocn(muData.getAbbrevLocn());
    }
    // CMR-1728/CMR-2093, For Turkey, we use RestrictTo to store CoF in
    // muData
    if (!StringUtils.isBlank(muData.getRestrictTo())) {
      if ("@".equals(muData.getRestrictTo())) {
        cust.setModeOfPayment("");
      } else {
        cust.setModeOfPayment(muData.getRestrictTo());
      }
    }
    // CMR-1728 For Turkey, we use CsoSite to store EconomicCode in muData
    if (!StringUtils.isBlank(muData.getCsoSite())) {
      if ("@".equals(muData.getCsoSite())) {
        cust.setEconomicCd("");
      } else {
        cust.setEconomicCd(muData.getCsoSite());
      }
    }

    String isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
        + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
    if (isuClientTier != null && isuClientTier.length() == 3) {
      cust.setIsuCd(isuClientTier);
    }

    if (!StringUtils.isBlank(muData.getSpecialTaxCd())) {
      cust.setTaxCd(muData.getSpecialTaxCd());
    }

    if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
      cust.setSalesRepNo(muData.getRepTeamMemberNo());
      cust.setSalesGroupRep(muData.getRepTeamMemberNo());
    } else {
      // CMR-2279:Turkey-ISR set based on SBO
      if (!StringUtils.isBlank(muData.getCustNm1())) {

        String sql = ExternalizedQuery.getSql("LEGACY.GET_ISR_BYSBO");
        PreparedQuery q = new PreparedQuery(entityManager, sql);
        q.setParameter("SBO", muData.getCustNm1());
        q.setParameter("CNTRY", SystemLocation.TURKEY);
        String isr = q.getSingleResult(String.class);
        if (!StringUtils.isBlank(isr)) {
          cust.setSalesRepNo(isr);
          cmrObjects.getMassUpdateData().setRepTeamMemberNo(isr);
        } else {
          cust.setSalesRepNo("");
          cmrObjects.getMassUpdateData().setRepTeamMemberNo("");
        }
      }
    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      if ("@".equals(muData.getEnterprise())) {
        cust.setEnterpriseNo("");
      } else {
        cust.setEnterpriseNo(muData.getEnterprise());
      }
    }

    if (!StringUtils.isBlank(muData.getCustNm2())) {
      cust.setCeBo(muData.getCustNm2());
    }

    List<MassUpdtAddr> muaList = cmrObjects.getMassUpdateAddresses();
    if (muaList != null && muaList.size() > 0) {
      for (MassUpdtAddr mua : muaList) {
        if ("ZP01".equals(mua.getId().getAddrType())) {
          if (!StringUtils.isBlank(mua.getCustPhone())) {
            if (DEFAULT_CLEAR_CHAR.equals(mua.getCustPhone())) {
              cust.setTelNoOrVat("");
            } else {
              cust.setTelNoOrVat(mua.getCustPhone());
            }
            break;
          }
        }
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
      cust.setIsicCd(muData.getIsicCd());
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      if ("@".equals(muData.getCollectionCd())) {
        cust.setVat("");
      } else {
        cust.setVat(muData.getVat());
      }
    }

    if (!StringUtils.isBlank(muData.getCustNm1())) {
      cust.setSbo(muData.getCustNm1());
      cust.setIbo(muData.getCustNm1());
    }

    if (!StringUtils.isBlank(muData.getInacCd())) {
      if ("@".equals(muData.getInacCd())) {
        cust.setInacCd("");
      } else {
        cust.setInacCd(muData.getInacCd());
      }
    }

    if (!StringUtils.isBlank(muData.getMiscBillCd())) {
      if ("@".equals(muData.getMiscBillCd())) {
        cust.setEmbargoCd("");
      } else {
        cust.setEmbargoCd(muData.getMiscBillCd());
      }
    }

    if (!StringUtils.isBlank(muData.getOutCityLimit())) {
      cust.setMailingCond(muData.getOutCityLimit());
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
    }

    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());

    // CMR-2279 Turkey update massUpdateData ,For liu hao remove TR change
    entityManager.merge(cmrObjects.getMassUpdateData());
    entityManager.flush();
  }

  private void resetOrdBlockToData(EntityManager entityManager, Data data) {
    data.setOrdBlk("88");
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

  private List<Addr> getAddrLegacy(EntityManager entityManager, String reqId) {
    LOG.debug("Turkey -- Searching for ADDR records for Request " + reqId);
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
    String reqType = cmrObjects.getAdmin().getReqType();
    long requestId = cmrObjects.getAdmin().getId().getReqId();

    Map<String, String> addrSeqToAddrUseMap = new HashMap<String, String>();
    if ("M".equals(reqType)) {
      List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
      addrSeqToAddrUseMap = mapSeqNoToAddrUseLegacy(legacyAddrList, cmrObjects.getMassUpdateAddresses());
    } else {
      addrSeqToAddrUseMap = mapSeqNoToAddrUse(getAddrLegacy(entityManager, String.valueOf(requestId)));
    }
    LOG.debug("LEGACY -- Turkey OVERRIDE transformOtherData");
    LOG.debug("addrSeqToAddrUseMap size: " + addrSeqToAddrUseMap.size());
    for (CmrtAddr legacyAddr : legacyObjects.getAddresses()) {
      modifyAddrUseFields(legacyAddr.getId().getAddrNo(), addrSeqToAddrUseMap.get(legacyAddr.getId().getAddrNo()), legacyAddr);
    }

    if ("C".equals(cmrObjects.getAdmin().getReqType())) {
      List<Addr> addrList = cmrObjects.getAddresses();
      List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
      for (int i = 0; i < addrList.size(); i++) {
        Addr addr = addrList.get(i);
        String addrType = addr.getId().getAddrType();
        if (addrType.equalsIgnoreCase(CmrConstants.ADDR_TYPE.ZP01.toString())) {
          // copy mailing from billing
          copyMailingFromBilling(legacyObjects, legacyAddrList.get(i));
        }
      }

      // copyMailingFromBilling(legacyObjects, billingAddr);
    }
  }

  private Map<String, String> mapSeqNoToAddrUseLegacy(List<CmrtAddr> legacyAddrList, List<MassUpdtAddr> muAddrlist) {
    Map<String, String> addrSeqToAddrUseMap = new HashMap<>();
    for (CmrtAddr legacyAddr : legacyAddrList) {
      for (MassUpdtAddr muAddr : muAddrlist) {
        if (muAddr.getAddrSeqNo().equals(legacyAddr.getId().getAddrNo())) {
          addrSeqToAddrUseMap.put(legacyAddr.getId().getAddrNo(), getAddressUseByType(muAddr.getId().getAddrType()));
          break;
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
    boolean crossBorder = false;
    String landedCountry = "";
    for (Addr addr : cmrObjects.getAddresses()) {
      // additional Address
      if (MQMsgConstants.ADDR_ZP01.equals(addr.getId().getAddrType())) {

        if (!StringUtils.isBlank(addr.getTaxOffice())) {
        legacyCustExt.setiTaxCode((addr.getTaxOffice()));
        } else {
          legacyCustExt.setiTaxCode("");
        }
      }

    }

    // // IBM Tab
    // legacyCustExt.setItCompanyCustomerNo(!StringUtils.isEmpty(data.getCompany())
    // ? data.getCompany() : ""); // CODCP
    // legacyCustExt.setAffiliate(!StringUtils.isBlank(data.getAffiliate()) ?
    // data.getAffiliate() : "");
    // legacyCustExt.setItCodeSSV(!StringUtils.isBlank(data.getCollectionCd()) ?
    // data.getCollectionCd() : "");
    //
    // // Customer Tab
    // legacyCustExt.setiTaxCode(!StringUtils.isBlank(data.getTaxCd1()) ?
    // data.getTaxCd1() : "");
    // legacyCustExt.setItIVA(!StringUtils.isBlank(data.getSpecialTaxCd()) ?
    // data.getSpecialTaxCd() : "");
    // legacyCustExt.setItIdentClient(!StringUtils.isBlank(data.getIdentClient())
    // ? data.getIdentClient() : "");
    //
    // // 4 new fields
    // legacyCustExt.setTipoCliente(!StringUtils.isBlank(data.getIcmsInd()) ?
    // data.getIcmsInd() : "");
    // legacyCustExt.setCoddes(!StringUtils.isBlank(data.getHwSvcsRepTeamNo()) ?
    // data.getHwSvcsRepTeamNo() : "");
    // legacyCustExt.setPec(!StringUtils.isBlank(data.getEmail2()) ?
    // data.getEmail2() : "");
    // legacyCustExt.setIndEmail(!StringUtils.isBlank(data.getEmail3()) ?
    // data.getEmail3() : "");
    //
    // if (crossBorder) {
    // legacyCustExt.setiTaxCode(!StringUtils.isBlank(data.getTaxCd1()) ?
    // landedCountry + data.getTaxCd1() : "");
    // }
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
              custExt.setiTaxCode(muData.getNewEntpName1());
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

  private void copyMailingFromBilling(LegacyDirectObjectContainer legacyObjects, CmrtAddr billingAddr) {
    CmrtAddr mailingAddr = (CmrtAddr) SerializationUtils.clone(billingAddr);
    mailingAddr.getId().setAddrNo("00001");
    mailingAddr.setIsAddrUseMailing(ADDRESS_USE_EXISTS);
    mailingAddr.setIsAddrUseBilling(ADDRESS_USE_NOT_EXISTS);
    // modifyAddrUseFields(MQMsgConstants.SOF_ADDRESS_USE_MAILING, mailingAddr);
    legacyObjects.getAddresses().add(mailingAddr);
  }

}
