/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Arrays;
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
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.impl.EMEAHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.CEBOGenerate;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * {@link MessageTransformer} implementation for United Kingdom
 * 
 * @author Jeffrey Zamora
 * 
 */
public class UnitedKingdomTransformer extends EMEATransformer {

  private static final Logger LOG = Logger.getLogger(UnitedKingdomTransformer.class);

  // added marekting response code to preserve BP = 5 for updates
  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode" };

  private static final String[] ADDRESS_ORDER = { "ZP01", "ZS01", "ZI01", "ZD01", "ZS02" };

  private static final String DEFAULT_LANDED_COUNTRY = "UK";
  private static final String DEFAULT_CLEAR_CHAR = "@";
  private static final String DEFAULT_CLEAR_6_CHAR = "@@@@@@";
  private static final String DEFAULT_CLEAR_4_CHAR = "@@@@";

  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";

  public UnitedKingdomTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);
  }

  public UnitedKingdomTransformer() {
    super(SystemLocation.UNITED_KINGDOM);
  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    LOG.debug("Handling Data for " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    boolean crossBorder = isCrossBorder(addrData);

    handleEMEADefaults(handler, messageHash, cmrData, addrData, crossBorder);

    handleDataDefaults(handler, messageHash, cmrData, crossBorder, addrData);

    messageHash.put("EmbargoCode", !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "");

    if (update) {
      // send the current marketing response code to avoid loss
      String currMrc = handler.currentCMRValues.get("MarketingResponseCode");
      LOG.debug("Current MRC: " + currMrc);
      if (!StringUtils.isEmpty(currMrc)) {
        messageHash.put("MarketingResponseCode", currMrc);
      } else {
        messageHash.put("MarketingResponseCode", "2");
      }

      for (String field : NO_UPDATE_FIELDS) {
        messageHash.remove(field);
      }

    }

  }

  /**
   * Handles default data values
   * 
   * @param handler
   * @param messageHash
   * @param cmrData
   * @param crossBorder
   * @param addrData
   */
  protected void handleDataDefaults(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData) {
    messageHash.put("SourceCode", "EFO");
    messageHash.put("CEdivision", "2");
    messageHash.put("RepeatAgreement", "1");

    // Commercial Leasing Scenario for UK
    if (MQMsgConstants.CUSTSUBGRP_COMLC.equals(cmrData.getCustSubGrp()))
      messageHash.put("LeasingCompany", "1");
    else
      messageHash.put("LeasingCompany", "0");
    // messageHash.put("LeasingCompany", "0");

    messageHash.put("MarketingResponseCode", "2");
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(cmrData.getCustSubGrp())) {
      messageHash.put("MarketingResponseCode", "5");
    }
    // for FSL scenario, FSLICAM = ZG33 for UK.
    if (MQMsgConstants.CUSTSUBGRP_INFSL.equals(cmrData.getCustSubGrp())) {
      messageHash.put("FSLICAM", "ZG33");
    }
    // for cross border, force to use ****
    messageHash.put("DPCEBO", CEBOGenerate.getUK_CEBO(crossBorder ? "****" : addrData.getPostCd()));
    messageHash.put("CurrencyCode", "NC");

    // for cross border, the AbbreviatedLocation is country id, need to
    // convert to country desc
    if (crossBorder) {
      if (StringUtils.isBlank(cmrData.getAbbrevLocn()))
        messageHash.put("AbbreviatedLocation", LandedCountryMap.getCountryName(addrData.getLandCntry()));
      if (!StringUtils.isBlank(LandedCountryMap.getCountryName(cmrData.getAbbrevLocn())))
        messageHash.put("AbbreviatedLocation", LandedCountryMap.getCountryName(cmrData.getAbbrevLocn()));
    }

    boolean create = "C".equals(handler.adminData.getReqType());
    if (create) {
      messageHash.put("AccAdBo", "AE");
      messageHash.put("DistrictCode", "75");
      messageHash.put("ModeOfPayment", "3");
    } else {
      if (StringUtils.isEmpty(cmrData.getModeOfPayment())) {
        messageHash.put("ModeOfPayment", "3");
      } else {
        messageHash.put("ModeOfPayment", cmrData.getModeOfPayment());
      }
    }

  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {

    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    LOG.debug("Handling Address for " + (update ? "update" : "create") + " request.");

    messageHash.put("AddressType", getTargetAddressType(addrData.getId().getAddrType()));

    boolean crossBorder = isCrossBorder(addrData);

    messageHash.put("SourceCode", "EFO");

    LOG.debug("Handling " + (crossBorder ? "cross-border" : "domestic") + " Data for " + addrData.getCustNm1());
    if (crossBorder) {
      // cross border is any address not belonging to the country

      String line1 = "";
      if (!StringUtils.isBlank(addrData.getCustNm2())) {
        line1 += addrData.getCustNm2();
      }
      if (!StringUtils.isBlank(addrData.getDept())) {
        line1 += (line1.length() > 0 ? ", " : "") + addrData.getDept();
      }
      // if (!StringUtils.isBlank(addrData.getCustPhone())) {
      // line1 += (line1.length() > 0 ? ", " : "") + addrData.getCustPhone();
      // }
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Address1", line1);

      String line2 = "";

      if (!StringUtils.isBlank(addrData.getAddrTxt())) {
        line2 += addrData.getAddrTxt();
      }
      String poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "";
      if (!StringUtils.isEmpty(poBox)) {
        poBox = "PO BOX " + poBox;
        line2 += (line2.length() > 0 ? ", " : "") + poBox;
      }

      String line3 = addrData.getPostCd() + " " + addrData.getCity1();

      String line4 = LandedCountryMap.getCountryName(addrData.getLandCntry());

      int lineNo = 1;
      String[] lines = new String[] { line1, line2, line3, line4 };
      LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4);
      for (String line : lines) {
        if (!StringUtils.isBlank(line)) {
          messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Address" + lineNo, line);
          lineNo++;
        }
      }
      // fill any extra line with ""
      while (lineNo <= 4) {
        messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Address" + lineNo, "");
        lineNo++;
      }

      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "ZipCode", "**** ***");

    } else {
      // domestic

      String line1 = "";
      if (!StringUtils.isBlank(addrData.getCustNm2())) {
        line1 += addrData.getCustNm2();
      }
      if (!StringUtils.isBlank(addrData.getDept())) {
        line1 += (line1.length() > 0 ? ", " : "") + addrData.getDept();
      }
      // CREATCMR-3314
      // if (!StringUtils.isBlank(addrData.getCustPhone())) {
      // line1 += (line1.length() > 0 ? " " : "") + addrData.getCustPhone();
      // }

      String line2 = "";
      if (StringUtils.isBlank(addrData.getAddrTxt())) {
        String poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "";
        if (!StringUtils.isEmpty(poBox)) {
          poBox = "PO BOX " + poBox;
          line2 = poBox;
        }
      } else {
        line2 = addrData.getAddrTxt();
      }

      String line3 = "";
      if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
        line3 = addrData.getAddrTxt2();
      }
      String poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "";
      if (!StringUtils.isEmpty(poBox)) {
        poBox = "PO BOX " + poBox;
        line3 += (line3.length() > 0 ? ", " : "") + poBox;
      }
      String line4 = "";
      if (!StringUtils.isBlank(addrData.getLandCntry()) && addrData.getLandCntry().equalsIgnoreCase("IE")
          && !StringUtils.isBlank(addrData.getPostCd()) && !addrData.getPostCd().equalsIgnoreCase("II1  1II"))
        line4 = addrData.getCity1() + "," + LandedCountryMap.getCountryName(addrData.getLandCntry());
      else
        line4 = addrData.getCity1();

      int lineNo = 1;
      String[] lines = new String[] { line1, line2, line3, line4 };
      LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4);
      for (String line : lines) {
        if (!StringUtils.isBlank(line)) {
          messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Address" + lineNo, line);
          lineNo++;
        }
      }

      // fill any extra line with ""
      while (lineNo <= 4) {
        messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Address" + lineNo, "");
        lineNo++;
      }

    }

    String countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());
    messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Country", countryName);

  }

  /**
   * According to new Address Mapping onboarding sheet
   * 
   * @param handler
   * @param legacyAddr
   */
  private void formatAddressLinesLD(MQMessageHandler handler, CmrtAddr legacyAddr) {

    Addr addrData = handler.addrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    LOG.debug("Legacy Direct -Handling Address for " + (update ? "update" : "create") + " request.");
    boolean crossBorder = isCrossBorder(addrData);

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";
    String addrType = addrData.getId().getAddrType();

    LOG.debug("Handling " + (crossBorder ? "cross-border" : "domestic") + " Data for " + addrData.getCustNm1());
    if (crossBorder) {
      // cross border is any address not belonging to the country

      // line1
      line1 = addrData.getCustNm1();

      if (StringUtils.isEmpty(line2)) {

        // Billing & Shipping -name con't OR Att. Person + phones
        if (MQMsgConstants.ADDR_ZS01.equals(addrType) || MQMsgConstants.ADDR_ZD01.equals(addrType)) {

          if (!StringUtils.isBlank(addrData.getCustNm2())) {
            line2 = addrData.getCustNm2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
            // CREATCMR-3314
            // if (!StringUtils.isBlank(addrData.getCustPhone())) {
            // line2 += (line2.length() > 0 ? ", " : "") +
            // addrData.getCustPhone();
            // }
          }
          // CREATCMR-3314
          // else if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          // line2 = addrData.getCustPhone();
          // }

        }
        // mailing,installing and EPL(Software Update)-name con't OR Att. Person
        if (MQMsgConstants.ADDR_ZP01.equals(addrType) || MQMsgConstants.ADDR_ZI01.equals(addrType) || MQMsgConstants.ADDR_ZS02.equals(addrType)) {

          if (!StringUtils.isBlank(addrData.getCustNm2())) {
            line2 = addrData.getCustNm2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
          }
        }
        if (StringUtils.isEmpty(line2)) {
          // Street -addrTxt
          if (!StringUtils.isBlank(addrData.getAddrTxt())) {
            line2 = addrData.getAddrTxt();
          }
          // mailling and billling Street Con't OR PO BOX -addrTxt2,poBox
          if (MQMsgConstants.ADDR_ZP01.equals(addrType) || MQMsgConstants.ADDR_ZS01.equals(addrType)) {
            if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
              line3 = addrData.getAddrTxt2();
            } else if (!StringUtils.isEmpty(addrData.getPoBox())) {
              line3 = "PO BOX " + addrData.getPoBox();
            }
          } else {
            if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
              line3 = addrData.getAddrTxt2();
            }
          }

          if (StringUtils.isEmpty(line3)) {
            // Postal Code + city -postCd +city1
            line3 = addrData.getPostCd() + ", " + addrData.getCity1();

            // landCntry
            line4 = LandedCountryMap.getCountryName(addrData.getLandCntry());
          } else {
            // Postal Code + city -postCd +city1
            line4 = addrData.getPostCd() + ", " + addrData.getCity1();

            // landCntry
            line5 = LandedCountryMap.getCountryName(addrData.getLandCntry());
          }
        } else {
          // Street -addrTxt
          if (!StringUtils.isBlank(addrData.getAddrTxt())) {
            line3 = addrData.getAddrTxt();
          }

          // mailling and billling Street Con't OR PO BOX -addrTxt2,poBox
          if (MQMsgConstants.ADDR_ZP01.equals(addrType) || MQMsgConstants.ADDR_ZS01.equals(addrType)) {
            if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
              line4 = addrData.getAddrTxt2();
            } else if (!StringUtils.isEmpty(addrData.getPoBox())) {
              line4 = "PO BOX " + addrData.getPoBox();
            }
          } else {
            if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
              line4 = addrData.getAddrTxt2();
            }
          }

          if (StringUtils.isEmpty(line4)) {
            // Postal Code + city -postCd +city1
            line4 = addrData.getPostCd() + ", " + addrData.getCity1();

            // landCntry
            line5 = LandedCountryMap.getCountryName(addrData.getLandCntry());
          } else {
            // Postal Code + city -postCd +city1
            line5 = addrData.getPostCd() + ", " + addrData.getCity1();

            // landCntry
            line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
          }
        }
      }
      // CREATCMR-1559 UkI Db flow change if street and street con't both are
      // filled
      if (!StringUtils.isBlank(addrData.getAddrTxt()) && !StringUtils.isBlank(addrData.getAddrTxt2())) {
        line1 = addrData.getCustNm1();
        // Billing & Shipping -name con't OR Att. Person + phones
        if (MQMsgConstants.ADDR_ZS01.equals(addrType) || MQMsgConstants.ADDR_ZD01.equals(addrType)) {

          if (!StringUtils.isBlank(addrData.getCustNm2())) {
            line2 = addrData.getCustNm2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
            // CREATCMR-3314
            // if (!StringUtils.isBlank(addrData.getCustPhone())) {
            // line2 += (line2.length() > 0 ? ", " : "") +
            // addrData.getCustPhone();
            // }
          }
          // CREATCMR-3314
          // else if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          // line2 = addrData.getCustPhone();
          // }

        }
        // mailing,installing and EPL(Software Update)-name con't OR Att. Person
        if (MQMsgConstants.ADDR_ZP01.equals(addrType) || MQMsgConstants.ADDR_ZI01.equals(addrType) || MQMsgConstants.ADDR_ZS02.equals(addrType)) {

          if (!StringUtils.isBlank(addrData.getCustNm2())) {
            line2 = addrData.getCustNm2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
          }
        }
        // street
        line3 = addrData.getAddrTxt();
        // street con't
        line4 = addrData.getAddrTxt2();
        // Postal Code + city -postCd +city1
        line5 = addrData.getPostCd() + ", " + addrData.getCity1();

        // landCntry
        line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      }
    } else {
      // domestic

      // line1
      line1 = addrData.getCustNm1();
      line2 = addrData.getCustNm2();

      if (StringUtils.isEmpty(line2)) {

        // mailling- Street Con't OR Att Person OR PO BOX
        if (MQMsgConstants.ADDR_ZP01.equals(addrType)) {

          if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
            line2 = addrData.getAddrTxt2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
          } else if (!StringUtils.isEmpty(addrData.getPoBox())) {
            line2 = "PO BOX " + addrData.getPoBox();
          }
        }
        // Billing -Street Con't OR Att Person + Phone OR PO BOX
        if (MQMsgConstants.ADDR_ZS01.equals(addrType)) {

          if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
            line2 = addrData.getAddrTxt2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
            // CREATCMR-3314
            // if (!StringUtils.isBlank(addrData.getCustPhone())) {
            // line2 += (line2.length() > 0 ? ", " : "") +
            // addrData.getCustPhone();
            // }
          }
          // CREATCMR-3314
          // else if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          // line2 = addrData.getCustPhone();
          // }
          else if (!StringUtils.isEmpty(addrData.getPoBox())) {
            line2 = "PO BOX " + addrData.getPoBox().trim();
          }
        }
        // Installing, EPL/SU-Street Con't OR Att Person
        if (MQMsgConstants.ADDR_ZI01.equals(addrType) || MQMsgConstants.ADDR_ZS02.equals(addrType)) {
          if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
            line2 = addrData.getAddrTxt2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
          }
        }
        // Shipping ZD01-Street Con't OR Att Person + Phone
        if (MQMsgConstants.ADDR_ZD01.equals(addrType)) {
          if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
            line2 = addrData.getAddrTxt2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
            // CREATCMR-3314
            // if (!StringUtils.isBlank(addrData.getCustPhone())) {
            // line2 += (line2.length() > 0 ? ", " : "") +
            // addrData.getCustPhone();
            // }
          }
          // CREATCMR-3314
          // else if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          // line2 = addrData.getCustPhone();
          // }
        }

        if (StringUtils.isEmpty(line2)) {
          // Street -addrTxt
          if (!StringUtils.isBlank(addrData.getAddrTxt())) {
            line2 = addrData.getAddrTxt();
          }

          // city
          if (!StringUtils.isBlank(addrData.getCity1())) {
            line3 = addrData.getCity1();
          }

          // Postal Code -postCd
          if (!StringUtils.isBlank(addrData.getPostCd())) {
            line4 = addrData.getPostCd();
          }

        } else {
          // Street -addrTxt
          if (!StringUtils.isBlank(addrData.getAddrTxt())) {
            line3 = addrData.getAddrTxt();
          }

          // city
          if (!StringUtils.isBlank(addrData.getCity1())) {
            line4 = addrData.getCity1();
          }

          // Postal Code -postCd
          if (!StringUtils.isBlank(addrData.getPostCd())) {
            line5 = addrData.getPostCd();
          }
        }

      } else {

        // mailling- Street Con't OR Att Person OR PO BOX
        if (MQMsgConstants.ADDR_ZP01.equals(addrType)) {

          if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
            line3 = addrData.getAddrTxt2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line3 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line3) && !line3.toUpperCase().startsWith("ATT ") && !line3.toUpperCase().startsWith("ATT:")) {
              line3 = "ATT " + line3;
            }
          } else if (!StringUtils.isEmpty(addrData.getPoBox())) {
            line3 = "PO BOX " + addrData.getPoBox();
          }
        }
        // Billing -Street Con't OR Att Person + Phone OR PO BOX
        if (MQMsgConstants.ADDR_ZS01.equals(addrType)) {

          if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
            line3 = addrData.getAddrTxt2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line3 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line3) && !line3.toUpperCase().startsWith("ATT ") && !line3.toUpperCase().startsWith("ATT:")) {
              line3 = "ATT " + line3;
            }
            // CREATCMR-3314
            // if (!StringUtils.isBlank(addrData.getCustPhone())) {
            // line3 += (line3.length() > 0 ? " " : "") +
            // addrData.getCustPhone();
            // }
          }
          // CREATCMR-3314
          // else if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          // line3 = addrData.getCustPhone();
          // }
          else if (!StringUtils.isEmpty(addrData.getPoBox())) {
            line3 = "PO BOX " + addrData.getPoBox().trim();
          }
        }
        // Installing, EPL/SU-Street Con't OR Att Person
        if (MQMsgConstants.ADDR_ZI01.equals(addrType) || MQMsgConstants.ADDR_ZS02.equals(addrType)) {
          if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
            line3 = addrData.getAddrTxt2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line3 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line3) && !line3.toUpperCase().startsWith("ATT ") && !line3.toUpperCase().startsWith("ATT:")) {
              line3 = "ATT " + line3;
            }
          }
        }
        // Shipping ZD01-Street Con't OR Att Person + Phone
        if (MQMsgConstants.ADDR_ZD01.equals(addrType)) {
          if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
            line3 = addrData.getAddrTxt2();
          } else if (!StringUtils.isBlank(addrData.getDept())) {
            line3 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line3) && !line3.toUpperCase().startsWith("ATT ") && !line3.toUpperCase().startsWith("ATT:")) {
              line3 = "ATT " + line3;
            }
            // CREATCMR-3314
            // if (!StringUtils.isBlank(addrData.getCustPhone())) {
            // line3 += (line3.length() > 0 ? " " : "") +
            // addrData.getCustPhone();
            // }
          }
          // CREATCMR-3314
          // else if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          // line3 = addrData.getCustPhone();
          // }
        }

        if (StringUtils.isBlank(line3)) {
          // Street -addrTxt
          if (!StringUtils.isBlank(addrData.getAddrTxt())) {
            line3 = addrData.getAddrTxt();
          }

          // city
          if (!StringUtils.isBlank(addrData.getCity1())) {
            line4 = addrData.getCity1();
          }

          // Postal Code -postCd
          if (!StringUtils.isBlank(addrData.getPostCd())) {
            line5 = addrData.getPostCd();
          }
        } else {

          // Street -addrTxt
          if (!StringUtils.isBlank(addrData.getAddrTxt())) {
            line4 = addrData.getAddrTxt();
          }

          // city
          if (!StringUtils.isBlank(addrData.getCity1())) {
            line5 = addrData.getCity1();
          }

          // Postal Code -postCd
          if (!StringUtils.isBlank(addrData.getPostCd())) {
            line6 = addrData.getPostCd();
          }
        }
      }
      // CREATCMR-1559 UkI Db flow change if street and street con't both are
      // filled
      if (!StringUtils.isBlank(addrData.getAddrTxt()) && !StringUtils.isBlank(addrData.getAddrTxt2())) {
        line1 = addrData.getCustNm1();
        line2 = addrData.getCustNm2();
        // mailling- Att Person OR PO BOX
        if (MQMsgConstants.ADDR_ZP01.equals(addrType)) {

          if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
          } else if (!StringUtils.isEmpty(addrData.getPoBox())) {
            line2 = "PO BOX " + addrData.getPoBox();
          }
        }
        // Billing -Street Con't OR Att Person + Phone OR PO BOX
        if (MQMsgConstants.ADDR_ZS01.equals(addrType)) {

          if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
            // CREATCMR-3314
            // if (!StringUtils.isBlank(addrData.getCustPhone())) {
            // line2 += (line2.length() > 0 ? ", " : "") +
            // addrData.getCustPhone();
            // }
          }
          // CREATCMR-3314
          // else if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          // line2 = addrData.getCustPhone();
          // }
          else if (!StringUtils.isEmpty(addrData.getPoBox())) {
            line2 = "PO BOX " + addrData.getPoBox().trim();
          }
        }
        // Installing, EPL/SU-Street Con't OR Att Person
        if (MQMsgConstants.ADDR_ZI01.equals(addrType) || MQMsgConstants.ADDR_ZS02.equals(addrType)) {
          if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
          }
        }
        // Shipping ZD01-Street Con't OR Att Person + Phone
        if (MQMsgConstants.ADDR_ZD01.equals(addrType)) {
          if (!StringUtils.isBlank(addrData.getDept())) {
            line2 = addrData.getDept().trim();
            if (!StringUtils.isEmpty(line2) && !line2.toUpperCase().startsWith("ATT ") && !line2.toUpperCase().startsWith("ATT:")) {
              line2 = "ATT " + line2;
            }
            // CREATCMR-3314
            // if (!StringUtils.isBlank(addrData.getCustPhone())) {
            // line2 += (line2.length() > 0 ? ", " : "") +
            // addrData.getCustPhone();
            // }
          }
          // CREATCMR-3314
          // else if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          // line2 = addrData.getCustPhone();
          // }
        }

        // street
        line3 = addrData.getAddrTxt();
        // street con't
        line4 = addrData.getAddrTxt2();
        // city
        if (!StringUtils.isBlank(addrData.getCity1())) {
          line5 = addrData.getCity1();
        }

        // Postal Code -postCd
        if (!StringUtils.isBlank(addrData.getPostCd())) {
          line6 = addrData.getPostCd();
        }
      }
    }
    legacyAddr.setAddrLine1(line1);
    legacyAddr.setAddrLine2(line2);
    legacyAddr.setAddrLine3(line3);
    legacyAddr.setAddrLine4(line4);
    legacyAddr.setAddrLine5(line5);
    legacyAddr.setAddrLine6(line6);

  }

  /**
   * Checks if this is a cross-border scenario
   * 
   * @param addr
   * @return
   */
  protected boolean isCrossBorder(Addr addr) {
    return !"GB".equals(addr.getLandCntry());
  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Billing";
    case "ZI01":
      return "Install";
    case "ZD01":
      return "Ship";
    case "ZP01":
      return "Mail";
    case "ZS02":
      return "Soft";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Billing";
    case "ZI01":
      return "Installing";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Mailing";
    case "ZS02":
      return "EPL";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.UNITED_KINGDOM;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "00001";
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
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

  /*
   * Legacy Direct Methods
   */

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();

    LOG.debug("Set max and min range For UKI...");
    if (custSubGrp != null && "INTER".equals(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    String landedCntry = "";
    formatDataLines(dummyHandler);

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {

      /*
       * if (!StringUtils.isEmpty(dummyHandler.messageHash.get("AccAdBo"))) {
       * legacyCust.setAccAdminBo(dummyHandler.messageHash.get("AccAdBo")); }
       */
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("CEdivision"))) {
        legacyCust.setCeDivision(dummyHandler.messageHash.get("CEdivision"));
      }
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("RepeatAgreement"))) {
        legacyCust.setDcRepeatAgreement(dummyHandler.messageHash.get("RepeatAgreement"));
      }
      // Leasing Company ind
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("LeasingCompany"))) {
        legacyCust.setLeasingInd(dummyHandler.messageHash.get("LeasingCompany"));
      }
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("CurrencyCode"))) {
        legacyCust.setCurrencyCd(dummyHandler.messageHash.get("CurrencyCode"));
      }
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("DistrictCode"))) {
        legacyCust.setDistrictCd(dummyHandler.messageHash.get("DistrictCode"));
      }

      // extract the phone from billing as main phone
      for (Addr addr : cmrObjects.getAddresses()) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          legacyCust.setTelNoOrVat(getTrimed(addr.getCustPhone()));
          landedCntry = addr.getLandCntry();
          break;
        }
      }

      // mrc
      String custType = data.getCustSubGrp();
      if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBSPR".equals(custType)) {
        legacyCust.setMrcCd("5");
        legacyCust.setAuthRemarketerInd("Y");
      } else {
        legacyCust.setMrcCd("2");
        legacyCust.setAuthRemarketerInd("N");
      }

    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      for (Addr addr : cmrObjects.getAddresses()) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          legacyCust.setTelNoOrVat(getTrimed(addr.getCustPhone()));
          landedCntry = addr.getLandCntry();
          break;
        }
      }

      String dataEmbargoCd = data.getEmbargoCd();
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin);

      // permanent removal-single inactivation
      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()) && !"TREC".equals(admin.getReqReason())) {
        if (!StringUtils.isBlank(rdcEmbargoCd) && ("E".equals(rdcEmbargoCd) || "C".equals(rdcEmbargoCd))) {
          if (StringUtils.isBlank(data.getEmbargoCd())) {
            legacyCust.setEmbargoCd("");
          }
        }
      }
      // Support temporary reactivation
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

      if ("5".equals(legacyCust.getMrcCd()))
        legacyCust.setAuthRemarketerInd("Y");
      else
        legacyCust.setAuthRemarketerInd("N");

    }

    // common data for C/U
    // formatted data
    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("AbbreviatedLocation"))) {
      legacyCust.setAbbrevLocn(dummyHandler.messageHash.get("AbbreviatedLocation"));
    }

    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("ModeOfPayment"))) {
      legacyCust.setModeOfPayment(dummyHandler.messageHash.get("ModeOfPayment"));
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
    if (!StringUtils.isEmpty(data.getCompany())) {
      legacyCust.setEnterpriseNo(data.getCompany());
    }

    /*
     * DENNIS 10152019: Commented as part of Defect 1883901: UKI - INTERNAL
     * Sub-scenario - abbreviated name character limitation. No special scenario
     * anymore for Internal scenario. This should now flow from the base
     * LegacyDirectService implementation for abbrevName
     */
    // DENNIS: Defect 1846637: PP_United Kingdom and Ireland_Department
    // (Internal) value is not flowing to Legacy and RDC DB properly.
    // if ("INTER".equals(data.getCustSubGrp()) ||
    // "XINTR".equals(data.getCustSubGrp())) {
    // if ("INTER".equals(data.getCustSubGrp())) {
    // String abbrevNm = "IBM/" + data.getIbmDeptCostCenter() + "/" +
    // getInstallingName1(dummyHandler);
    //
    // if (abbrevNm != null && abbrevNm.length() > 22) {
    // abbrevNm = abbrevNm.substring(0, 22);
    // }
    //
    // legacyCust.setAbbrevNm(abbrevNm);
    //
    // }

    if (!StringUtils.isBlank(data.getSpecialTaxCd())) {
      if ("BL".equalsIgnoreCase(data.getSpecialTaxCd().trim())) {
        legacyCust.setTaxCd("");
      } else {
        legacyCust.setTaxCd(data.getSpecialTaxCd());
      }
    }
    if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
      legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
    }

    List<String> isuCdList = Arrays.asList("5K", "11", "05", "4F");
    if (!StringUtils.isEmpty(data.getIsuCd()) && isuCdList.contains(data.getIsuCd())) {
      legacyCust.setIsuCd(data.getIsuCd() + "7");
    } else {
      String isuCtc;
      isuCtc = (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
          + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : "");
      if (isuCtc != null) {
        legacyCust.setIsuCd(isuCtc);
      }
    }

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
    entityManager.merge(data);
    entityManager.flush();
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    if ("N".equals(currAddr.getImportInd()) && MQMsgConstants.ADDR_ZD01.equals(currAddr.getId().getAddrType())) {
      // preferred sequence no for additional shipping
      legacyAddr.getId().setAddrNo(StringUtils.isEmpty(currAddr.getPrefSeqNo()) ? legacyAddr.getId().getAddrNo() : currAddr.getPrefSeqNo());
    }

    formatAddressLinesLD(dummyHandler, legacyAddr);
    if ("ZD01".equals(currAddr.getId().getAddrType()) && !StringUtils.isEmpty(currAddr.getCustPhone())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone().trim());
    } else if ("ZD01".equals(currAddr.getId().getAddrType()) && StringUtils.isEmpty(currAddr.getCustPhone())) {
      legacyAddr.setAddrPhone("");
    }
    String poBox = currAddr.getPoBox();
    if (!StringUtils.isEmpty(poBox) && ("ZS01".equals(currAddr.getId().getAddrType()) || "ZP01".equals(currAddr.getId().getAddrType()))) {
      if (!poBox.startsWith("PO BOX ")) {
        if (poBox.toUpperCase().startsWith("APTO"))
          poBox = poBox.substring(5);
        legacyAddr.setPoBox("PO BOX " + poBox);
      } else {
        legacyAddr.setPoBox(poBox);
      }
    } else if (StringUtils.isEmpty(poBox) && ("ZS01".equals(currAddr.getId().getAddrType()) || "ZP01".equals(currAddr.getId().getAddrType()))) {
      legacyAddr.setPoBox("");
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

  private String getInstallingName1(MQMessageHandler handler) {
    String instName1 = "";
    EntityManager entityManager = handler.getEntityManager();
    if (entityManager == null) {
      return instName1;
    }
    List<Addr> addresses = null;
    String sql = ExternalizedQuery.getSql("UKI.GET_TOPMOST_INSTALLING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", handler.addrData.getId().getReqId());
    query.setForReadOnly(true);
    addresses = query.getResults(Addr.class);
    if (addresses != null && addresses.size() > 0) {
      Addr addr = addresses.get(0);
      instName1 = addr.getCustNm1();
    }

    return instName1;
  }

  @Override
  public void handlePostCdSpecialLogic(CmrtCust cust, Data data, String postcd, EntityManager entityManager) {
    EMEAHandler emea = new EMEAHandler();
    if (!StringUtils.isBlank(postcd)) {
      postcd = postcd.substring(0, postcd.indexOf(" "));
      emea.setEngBoAndAdminBoOnLegacy(cust, data, entityManager, postcd);
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
      if (DEFAULT_CLEAR_CHAR.equals(addr.getCustNm2())) {
        legacyAddr.setAddrLine2("");
      } else {
        legacyAddr.setAddrLine2(addr.getCustNm2());
      }
    }

    if (!StringUtils.isBlank(addr.getAddrTxt())) {
      if (DEFAULT_CLEAR_CHAR.equals(addr.getAddrTxt())) {
        legacyAddr.setStreet("");
      } else {
        legacyAddr.setStreet(addr.getAddrTxt());
      }
    }

    if (!StringUtils.isBlank(addr.getAddrTxt2())) {
      if (DEFAULT_CLEAR_CHAR.equals(addr.getAddrTxt2())) {
        legacyAddr.setStreetNo("");
      } else {
        legacyAddr.setStreetNo(addr.getAddrTxt2());
      }
    }

    if (!StringUtils.isBlank(addr.getCity1())) {
      legacyAddr.setCity(addr.getCity1());
    }

    if (!StringUtils.isBlank(addr.getDept())) {
      if (DEFAULT_CLEAR_CHAR.equals(addr.getDept())) {
        legacyAddr.setContact("");
      } else {
        legacyAddr.setContact(addr.getDept());
      }
    }

    if (!StringUtils.isBlank(addr.getPostCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(addr.getPostCd())) {
        legacyAddr.setZipCode("");
      } else {
        legacyAddr.setZipCode(addr.getPostCd());

        if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(addr.getId().getAddrType()) && !isCrossBorderForMass(addr, legacyAddr)) {
          // DTN: Set again the CmrtCust object on the legacy objects
          // container just to be sure
          handlePostCdSpecialLogic(cust, data, addr.getPostCd(), entityManager);
        }
      }
    }

    String poBox = addr.getPoBox();
    if (!StringUtils.isEmpty(poBox)) {
      if (DEFAULT_CLEAR_CHAR.equals(poBox)) {
        legacyAddr.setPoBox("");
      } else {
        legacyAddr.setPoBox(addr.getPoBox());
      }

    }

    if (!StringUtils.isEmpty(addr.getCustPhone())) {
      setAddrPhoneForMassUpdate(legacyAddr, addr);

    }

    formatMassUpdateAddressLines(entityManager, legacyAddr, addr, false);
    legacyObjects.addAddress(legacyAddr);

  }

  private void setAddrPhoneForMassUpdate(CmrtAddr legacyAddr, MassUpdtAddr addr) {
    if (DEFAULT_CLEAR_CHAR.equals(addr.getCustPhone())) {
      legacyAddr.setAddrPhone("");
    } else if ("ZD01".equals(addr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(addr.getCustPhone());
    }
  }

  @Override
  public void formatMassUpdateAddressLines(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr massUpdtAddr, boolean isFAddr) {
    LOG.debug("***START UK formatMassUpdateAddressLines >>>");
    boolean crossBorder = isCrossBorderForMass(massUpdtAddr, legacyAddr);
    String addrKey = getAddressKey(massUpdtAddr.getId().getAddrType());
    Map<String, String> messageHash = new LinkedHashMap<String, String>();

    messageHash.put("SourceCode", "EF0");

    LOG.debug("Handling " + (crossBorder ? "cross-border" : "domestic") + " Data for " + legacyAddr.getAddrLine1());
    // LINE 1
    String line1 = legacyAddr.getAddrLine1();
    String line2 = legacyAddr.getAddrLine2();
    String line3 = legacyAddr.getAddrLine3();
    String line4 = legacyAddr.getAddrLine4();
    String line5 = legacyAddr.getAddrLine5();
    String line6 = legacyAddr.getAddrLine6();
    String addrType = massUpdtAddr.getId().getAddrType();

    if (crossBorder) {
      // cross border
      if (MQMsgConstants.ADDR_ZP01.equals(addrType)) {
        // mailling- name con't OR Att. Person
        if (!StringUtils.isBlank(massUpdtAddr.getCustNm2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCustNm2())) {
            line2 = "";
          } else {
            line2 = massUpdtAddr.getCustNm2();
          }
        } else if (!StringUtils.isBlank(massUpdtAddr.getCounty()) && line2.toUpperCase().startsWith("ATT")) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCounty())) {
            line2 = "";
          } else {
            line2 = "ATT " + massUpdtAddr.getCounty().trim();
          }
        }
        // mailling-Street Con't OR PO BOX
        if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getAddrTxt2())) {
            line3 = "";
          } else {
            line3 = massUpdtAddr.getAddrTxt2();
          }
        } else if (!StringUtils.isEmpty(massUpdtAddr.getPoBox())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getPoBox())) {
            line3 = "";
            legacyAddr.setPoBox("");
          } else {
            line3 = "PO BOX " + massUpdtAddr.getPoBox();
            legacyAddr.setPoBox(massUpdtAddr.getPoBox());
          }
        }
      }

      if (MQMsgConstants.ADDR_ZS01.equals(addrType)) {
        // Billing- name con't OR Att. Person
        if (!StringUtils.isBlank(massUpdtAddr.getCustNm2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCustNm2())) {
            line2 = "";
          } else {
            line2 = massUpdtAddr.getCustNm2();
          }
        } else if (!StringUtils.isBlank(massUpdtAddr.getCounty()) && line2.toUpperCase().startsWith("ATT")) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCounty())) {
            line2 = "";
          } else {
            line2 = "ATT " + massUpdtAddr.getCounty().trim();
          }
        }
        // Billing-Street Con't OR PO BOX
        if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getAddrTxt2())) {
            line3 = "";
          } else {
            line3 = massUpdtAddr.getAddrTxt2();
          }
        } else if (!StringUtils.isEmpty(massUpdtAddr.getPoBox())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getPoBox())) {
            line3 = "";
            legacyAddr.setPoBox("");
          } else {
            line3 = "PO BOX " + massUpdtAddr.getPoBox();
            legacyAddr.setPoBox(massUpdtAddr.getPoBox());
          }
        }
      }

      if (MQMsgConstants.ADDR_ZI01.equals(addrType)) {
        // Installing- name con't OR Att. Person
        if (!StringUtils.isBlank(massUpdtAddr.getCustNm2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCustNm2())) {
            line2 = "";
          } else {
            line2 = massUpdtAddr.getCustNm2();
          }
        } else if (!StringUtils.isBlank(massUpdtAddr.getCounty()) && line2.toUpperCase().startsWith("ATT")) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCounty())) {
            line2 = "";
          } else {
            line2 = "ATT " + massUpdtAddr.getCounty().trim();
          }
        }
        // Installing-Street Con't
        if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getAddrTxt2())) {
            line3 = "";
          } else {
            line3 = massUpdtAddr.getAddrTxt2();
          }
        }
      }

      if (MQMsgConstants.ADDR_ZD01.equals(addrType)) {
        // Shipping ZD01-Street Con't OR Att Person + Phone
        if (!StringUtils.isBlank(massUpdtAddr.getCustNm2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCustNm2())) {
            line2 = "";
          } else {
            line2 = massUpdtAddr.getCustNm2();
          }
        } else if (!StringUtils.isBlank(massUpdtAddr.getCounty()) && line2.toUpperCase().startsWith("ATT")) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCounty())) {
            line2 = "";
          } else {
            line2 = "ATT " + massUpdtAddr.getCounty().trim();
          }
        }
        // Shipping ZD01-Street Con't
        if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getAddrTxt2())) {
            line3 = "";
          } else {
            line3 = massUpdtAddr.getAddrTxt2();
          }
        }
      }

      if (MQMsgConstants.ADDR_ZS02.equals(addrType)) {
        // EPL ZS02-name con't OR Att. Person
        if (!StringUtils.isBlank(massUpdtAddr.getCustNm2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCustNm2())) {
            line2 = "";
          } else {
            line2 = massUpdtAddr.getCustNm2();
          }
        } else if (!StringUtils.isBlank(massUpdtAddr.getCounty()) && line2.toUpperCase().startsWith("ATT")) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCounty())) {
            line2 = "";
          } else {
            line2 = "ATT " + massUpdtAddr.getCounty().trim();
          }
        }
        // EPL ZS02-Street Con't
        if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getAddrTxt2())) {
            line3 = "";
          } else {
            line3 = massUpdtAddr.getAddrTxt2();
          }
        }
      }

      if (!StringUtils.isEmpty(massUpdtAddr.getAddrTxt())) {
        line4 = massUpdtAddr.getAddrTxt();
        legacyAddr.setStreet(massUpdtAddr.getAddrTxt());
      }

      if (!StringUtils.isEmpty(massUpdtAddr.getPostCd()) || !StringUtils.isEmpty(massUpdtAddr.getCity1())) {
        line5 = (legacyAddr.getZipCode() != null ? legacyAddr.getZipCode().trim() + " " : "")
            + (legacyAddr.getCity() != null ? legacyAddr.getCity().trim() : "");
      }

      if (!StringUtils.isEmpty(massUpdtAddr.getLandCntry())) {
        if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getLandCntry())) {
          line6 = "";
        } else {
          line6 = massUpdtAddr.getLandCntry();
        }
      }

    } else {
      // domestic
      // default domestica handling for cust name 2
      if (!StringUtils.isBlank(massUpdtAddr.getCustNm2())) {
        if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCustNm2())) {
          legacyAddr.setAddrLine2("");
        } else {
          legacyAddr.setAddrLine2(massUpdtAddr.getCustNm2());
        }
      }

      // mailling- Street Con't OR Att Person OR PO BOX
      if (MQMsgConstants.ADDR_ZP01.equals(addrType)) {
        if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getAddrTxt2())) {
            line3 = "";
          } else {
            line3 = massUpdtAddr.getAddrTxt2();
          }
        } else if (!StringUtils.isBlank(massUpdtAddr.getCounty()) && line3.toUpperCase().startsWith("ATT")) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCounty())) {
            line3 = "";
          } else {
            line3 = "ATT " + massUpdtAddr.getCounty().trim();
          }
        } else if (!StringUtils.isEmpty(massUpdtAddr.getPoBox())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getPoBox())) {
            line3 = "";
            legacyAddr.setPoBox("");
          } else {
            line3 = "PO BOX " + massUpdtAddr.getPoBox();
            legacyAddr.setPoBox(massUpdtAddr.getPoBox());
          }
        }
      }

      // Billing - Street Con't OR Att Person OR PO BOX
      if (MQMsgConstants.ADDR_ZS01.equals(addrType)) {
        if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getAddrTxt2())) {
            line3 = "";
          } else {
            line3 = massUpdtAddr.getAddrTxt2();
          }
        } else if (!StringUtils.isBlank(massUpdtAddr.getCounty()) && line3.toUpperCase().startsWith("ATT")) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCounty())) {
            line3 = "";
          } else {
            line3 = "ATT " + massUpdtAddr.getCounty().trim();
          }
        } else if (!StringUtils.isEmpty(massUpdtAddr.getPoBox())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getPoBox())) {
            line3 = "";
            legacyAddr.setPoBox("");
          } else {
            line3 = "PO BOX " + massUpdtAddr.getPoBox();
            legacyAddr.setPoBox(massUpdtAddr.getPoBox());
          }
        }
      }

      // Installing, Street Con't OR Att Person
      if (MQMsgConstants.ADDR_ZI01.equals(addrType)) {
        if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getAddrTxt2())) {
            line3 = "";
          } else {
            line3 = massUpdtAddr.getAddrTxt2();
          }
        } else if (!StringUtils.isBlank(massUpdtAddr.getCounty()) && line3.toUpperCase().startsWith("ATT")) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCounty())) {
            line3 = "";
          } else {
            line3 = "ATT " + massUpdtAddr.getCounty().trim();
          }
        }
      }

      // Shipping ZD01-Street Con't OR Att Person + Phone
      if (MQMsgConstants.ADDR_ZD01.equals(addrType)) {
        if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getAddrTxt2())) {
            line3 = "";
          } else {
            line3 = massUpdtAddr.getAddrTxt2();
          }
        } else if (!StringUtils.isBlank(massUpdtAddr.getCounty()) && line3.toUpperCase().startsWith("ATT")) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCounty())) {
            line3 = "";
          } else {
            line3 = "ATT " + massUpdtAddr.getCounty().trim();
          }
        }
      }

      // EPL, Street Con't OR Att Person
      if (MQMsgConstants.ADDR_ZS02.equals(addrType)) {
        if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getAddrTxt2())) {
            line3 = "";
          } else {
            line3 = massUpdtAddr.getAddrTxt2();
          }
        } else if (!StringUtils.isBlank(massUpdtAddr.getCounty()) && line3.toUpperCase().startsWith("ATT")) {
          if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCounty())) {
            line3 = "";
          } else {
            line3 = "ATT " + massUpdtAddr.getCounty().trim();
          }
        }
      }

      if (!StringUtils.isEmpty(massUpdtAddr.getAddrTxt())) {
        line4 = massUpdtAddr.getAddrTxt();
        legacyAddr.setStreet(massUpdtAddr.getAddrTxt());
      }

      if (!StringUtils.isEmpty(massUpdtAddr.getCity1())) {
        line5 = massUpdtAddr.getCity1();
        legacyAddr.setCity(massUpdtAddr.getCity1());
      }

      if (!StringUtils.isEmpty(massUpdtAddr.getPostCd())) {
        line6 = massUpdtAddr.getPostCd();
        legacyAddr.setZipCode(massUpdtAddr.getPostCd());
      }

    }

    legacyAddr.setAddrLine1(line1);
    legacyAddr.setAddrLine2(line2);
    legacyAddr.setAddrLine3(line3);
    legacyAddr.setAddrLine4(line4);
    legacyAddr.setAddrLine5(line5);
    legacyAddr.setAddrLine6(line6);
    LOG.debug("***END UK formatMassUpdateAddressLines >>>");
  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust cust, CMRRequestContainer cmrObjects, MassUpdtData muData) {
    // default mapping for DATA and CMRTCUST
    LOG.debug("UK >> Mapping default Data values..");

    if (!StringUtils.isBlank(muData.getAbbrevNm())) {
      cust.setAbbrevNm(muData.getAbbrevNm());
    }

    if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
      cust.setAbbrevLocn(muData.getAbbrevLocn());
    }

    if (!StringUtils.isBlank(muData.getModeOfPayment())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getModeOfPayment().trim())) {
        cust.setModeOfPayment("");
      } else {
        cust.setModeOfPayment(muData.getModeOfPayment());
      }
    }

    List<String> isuCdList = Arrays.asList("5K", "11", "05", "4F");
    if (!StringUtils.isEmpty(muData.getIsuCd()) && isuCdList.contains(muData.getIsuCd())) {
      cust.setIsuCd(muData.getIsuCd() + "7");
    } else if (!StringUtils.isEmpty(muData.getIsuCd()) && "34".equals(muData.getIsuCd()) && StringUtils.isBlank(muData.getClientTier())) {
      cust.setIsuCd(muData.getIsuCd() + "7");
    } else {
      String isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
          + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
      if (isuClientTier != null && isuClientTier.endsWith("@")) {
        cust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : cust.getIsuCd().substring(0, 2)) + "7");
      } else if (isuClientTier != null && isuClientTier.length() == 3) {
        cust.setIsuCd(isuClientTier);
      }
    }

    if (!StringUtils.isBlank(muData.getSpecialTaxCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getSpecialTaxCd().trim())) {
        cust.setTaxCd("");
      } else {
        cust.setTaxCd(muData.getSpecialTaxCd());
      }
    }

    if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
      cust.setSalesRepNo(muData.getRepTeamMemberNo());
      cust.setSalesGroupRep(muData.getRepTeamMemberNo());
    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      if (DEFAULT_CLEAR_6_CHAR.equals(muData.getEnterprise().trim())) {
        cust.setEnterpriseNo("");
      } else {
        cust.setEnterpriseNo(muData.getEnterprise());
      }
    }

    if (!StringUtils.isBlank(muData.getCustNm2())) {
      cust.setCeBo(muData.getCustNm2());
    }

    if (!StringUtils.isBlank(muData.getCollectionCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getCollectionCd().trim())) {
        // cust.setCollectionCd("");
        // cust.setDistrictCd("");
        cust.setCollectionCd("");
      } else {
        cust.setCollectionCd(muData.getCollectionCd());
      }
    }

    if (!StringUtils.isBlank(muData.getIsicCd())) {
      cust.setIsicCd(muData.getIsicCd());
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      // String newVat = handleVatMassUpdateChanges(muData.getVat(),
      // cust.getVat());
      if (DEFAULT_CLEAR_CHAR.equals(muData.getVat().trim())) {
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
      if (DEFAULT_CLEAR_4_CHAR.equals(muData.getInacCd().trim())) {
        cust.setInacCd("");
      } else {
        cust.setInacCd(muData.getInacCd());
      }
    }

    if (!StringUtils.isBlank(muData.getMiscBillCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getMiscBillCd().trim())) {
        cust.setEmbargoCd("");
      } else {
        cust.setEmbargoCd(muData.getMiscBillCd());
      }
    }

    if (!StringUtils.isBlank(muData.getOutCityLimit())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getOutCityLimit().trim())) {
        cust.setMailingCond("");
      } else {
        cust.setMailingCond(muData.getOutCityLimit());
      }
    }

    for (MassUpdtAddr addr : cmrObjects.getMassUpdateAddresses()) {
      if (!StringUtils.isBlank(addr.getCustPhone()) && "ZS01".equals(addr.getId().getAddrType())) {
        if (DEFAULT_CLEAR_CHAR.equals(addr.getCustPhone())) {
          cust.setTelNoOrVat("");
        } else {
          cust.setTelNoOrVat(getTrimed(addr.getCustPhone()));
        }
        break;
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
    }

    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    // cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());
  }

  @Override
  public boolean isCrossBorderForMass(MassUpdtAddr addr, CmrtAddr legacyAddr) {
    boolean isCrossBorder = false;
    if (!StringUtils.isEmpty(addr.getLandCntry()) && !UnitedKingdomTransformer.DEFAULT_LANDED_COUNTRY.equals(addr.getLandCntry())) {
      isCrossBorder = true;
    } else if (!StringUtils.isEmpty(legacyAddr.getAddrLine5()) && legacyAddr.getAddrLine5().length() == 2) {
      isCrossBorder = true;
    }
    return isCrossBorder;
  }

  @Override
  public boolean hasCmrtCustExt() {
    return true;
  }

  @Override
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();

    // Customer Tab
    legacyCustExt.setiTaxCode(!StringUtils.isBlank(data.getTaxCd1()) ? data.getTaxCd1() : "");
  }

  @Override
  public boolean isPG01Supported() {
    return true;
  }

  protected String getTrimed(String str) {
    if (StringUtils.isEmpty(str)) {
      str = "";
    } else {
      str = str.trim();
    }
    return str;
  }

}
