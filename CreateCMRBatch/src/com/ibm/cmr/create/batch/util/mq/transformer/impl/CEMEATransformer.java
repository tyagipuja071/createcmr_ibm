/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueuePK;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.impl.CEMEAHandler;
import com.ibm.cio.cmr.request.util.sof.GenericSOFMessageParser;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.GenerateCMRNoClient;
import com.ibm.cmr.services.client.SOFServiceClient;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoResponse;
import com.ibm.cmr.services.client.sof.SOFQueryRequest;
import com.ibm.cmr.services.client.sof.SOFQueryResponse;

/**
 * @author Jeffrey Zamora
 * 
 */
public class CEMEATransformer extends MessageTransformer {

  private static final Logger LOG = Logger.getLogger(CEMEATransformer.class);

  protected static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02", "ZP02" };

  private static final String[] GULF_ORIGINAL_COUNTRIES = { "677", "680", "620", "832", "805", "767", "823", "762", "768", "772", "849" };
  private static final String GULF_DUPLICATE = "GULF";
  private static final String CIS_DUPLICATE = "CIS";

  protected boolean duplicateRecordFound = false;
  protected Map<String, String> dupCMRValues = new HashMap<String, String>();
  protected List<String> dupShippingSequences = null;

  protected static Map<String, String> SOURCE_CODE_MAP = new HashMap<String, String>();

  static {
    SOURCE_CODE_MAP.put(SystemLocation.ABU_DHABI, "C57");
    SOURCE_CODE_MAP.put(SystemLocation.ALBANIA, "CZ9");
    SOURCE_CODE_MAP.put(SystemLocation.ARMENIA, "1CZ");
    SOURCE_CODE_MAP.put(SystemLocation.AUSTRIA, "CZZ");
    SOURCE_CODE_MAP.put(SystemLocation.AZERBAIJAN, "C7Z");
    SOURCE_CODE_MAP.put(SystemLocation.BAHRAIN, "C60");
    SOURCE_CODE_MAP.put(SystemLocation.BELARUS, "2CZ");
    SOURCE_CODE_MAP.put(SystemLocation.BOSNIA_AND_HERZEGOVINA, "C1Z");
    SOURCE_CODE_MAP.put(SystemLocation.BULGARIA, "CZ2");
    SOURCE_CODE_MAP.put(SystemLocation.CROATIA, "C2Z");
    SOURCE_CODE_MAP.put(SystemLocation.CZECH_REPUBLIC, "CZ3");
    SOURCE_CODE_MAP.put(SystemLocation.EGYPT, "C54");
    SOURCE_CODE_MAP.put(SystemLocation.GEORGIA, "3CZ");
    SOURCE_CODE_MAP.put(SystemLocation.HUNGARY, "CZ5");
    SOURCE_CODE_MAP.put(SystemLocation.IRAQ, "C68");
    SOURCE_CODE_MAP.put(SystemLocation.JORDAN, "C50");
    SOURCE_CODE_MAP.put(SystemLocation.KAZAKHSTAN, "4CZ");
    SOURCE_CODE_MAP.put(SystemLocation.KUWAIT, "C61");
    SOURCE_CODE_MAP.put(SystemLocation.KYRGYZSTAN, "5CZ");
    SOURCE_CODE_MAP.put(SystemLocation.LEBANON, "C52");
    SOURCE_CODE_MAP.put(SystemLocation.LIBYA, "C64");
    SOURCE_CODE_MAP.put(SystemLocation.MACEDONIA, "C3Z");
    SOURCE_CODE_MAP.put(SystemLocation.MOLDOVA, "6CZ");
    SOURCE_CODE_MAP.put(SystemLocation.MONTENEGRO, "C4Z");
    SOURCE_CODE_MAP.put(SystemLocation.MOROCCO, "WDE");
    SOURCE_CODE_MAP.put(SystemLocation.OMAN, "C58");
    SOURCE_CODE_MAP.put(SystemLocation.PAKISTAN, "C55");
    SOURCE_CODE_MAP.put(SystemLocation.POLAND, "CZ6");
    SOURCE_CODE_MAP.put(SystemLocation.QATAR, "C62");
    SOURCE_CODE_MAP.put(SystemLocation.ROMANIA, "CZ7");
    SOURCE_CODE_MAP.put(SystemLocation.RUSSIAN_FEDERATION, "CZ8");
    SOURCE_CODE_MAP.put(SystemLocation.SAUDI_ARABIA, "C63");
    SOURCE_CODE_MAP.put(SystemLocation.SERBIA, "C4Z");
    SOURCE_CODE_MAP.put(SystemLocation.SLOVAKIA, "CZ4");
    SOURCE_CODE_MAP.put(SystemLocation.SLOVENIA, "C5Z");
    SOURCE_CODE_MAP.put(SystemLocation.SYRIAN_ARAB_REPUBLIC, "C53");
    SOURCE_CODE_MAP.put(SystemLocation.TAJIKISTAN, "C9Z");
    SOURCE_CODE_MAP.put(SystemLocation.TURKMENISTAN, "C8Z");
    SOURCE_CODE_MAP.put(SystemLocation.UKRAINE, "7CZ");
    SOURCE_CODE_MAP.put(SystemLocation.UNITED_ARAB_EMIRATES, "C56");
    SOURCE_CODE_MAP.put(SystemLocation.UZBEKISTAN, "C6Z");
    SOURCE_CODE_MAP.put(SystemLocation.YEMEN, "C59");
    SOURCE_CODE_MAP.put(SystemLocation.GULF, "C51");
  }

  public CEMEATransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    Map<String, String> messageHash = handler.messageHash;

    Data cmrData = handler.cmrData;
    Admin admin = handler.adminData;
    boolean update = "U".equals(admin.getReqType());

    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    messageHash.put("MarketingResponseCode", "3");

    String sbo = messageHash.get("SBO");

    if (!StringUtils.isEmpty(sbo)) {
      sbo = StringUtils.rightPad(sbo, 7, '0');
    }
    messageHash.put("SBO", sbo);
    messageHash.put("IBO", sbo);

    String cebo = messageHash.get("DPCEBO");
    if (!StringUtils.isEmpty(cebo)) {
      cebo = StringUtils.rightPad(cebo, 7, '0');
    }
    messageHash.put("DPCEBO", cebo);

    String custType = !StringUtils.isEmpty(cmrData.getCustSubGrp()) ? cmrData.getCustSubGrp() : "";
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || custType.contains("BP")) {
      messageHash.put("ARemark", "YES");
      messageHash.put("CustomerType", "BP");
      messageHash.put("MarketingResponseCode", "5");
    } else {
      messageHash.put("ARemark", "");
      messageHash.put("CustomerType", "");
    }

    if (!update) {
      messageHash.put("CEdivision", "3");
      messageHash.put("CICode", "3");
    }

    // transform langCode (RDC to SOF)
    if (!StringUtils.isEmpty(cmrData.getCustPrefLang()) && "D".equals(cmrData.getCustPrefLang())) {
      messageHash.put("LangCode", "1");
    } else {
      messageHash.put("LangCode", "2");
    }

    if (SystemLocation.AUSTRIA.equals(cmrData.getCmrIssuingCntry())) {
      // send Bundeslandercode to CustomerLocation
      messageHash.put("CustomerLocationNo", !StringUtils.isEmpty(cmrData.getLocationNumber()) ? cmrData.getLocationNumber() : "");

      if ("INTSO".equals(custType) || "XISO".equals(custType)) {
        messageHash.put("AllianceType", "AA");
        messageHash.put("LeasingCompany", "YES");
      }
    } else {
      // send cntry + IMS to CustomerLocation
      messageHash.put("CustomerLocationNo",
          !StringUtils.isEmpty(cmrData.getSubIndustryCd()) ? cmrData.getCmrIssuingCntry() + cmrData.getSubIndustryCd() : "");
      // send telephone under IBM data
      messageHash.put("TelephoneNo", !StringUtils.isEmpty(cmrData.getPhone1()) ? cmrData.getPhone1() : "");
    }

    if (!update && SystemLocation.AUSTRIA.equals(cmrData.getCmrIssuingCntry())) {
      // defaults for austria create
      messageHash.put("EPLLanguageCode", "1");
      messageHash.put("EducationAllowance", "NO");
      messageHash.put("DPCEBO", "1100000");
    }

    messageHash.put("CoF", !StringUtils.isEmpty(cmrData.getCommercialFinanced()) ? cmrData.getCommercialFinanced() : "");
    messageHash.put("EmbargoCode", !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "");
    messageHash.put("AECISubDate", !StringUtils.isEmpty(cmrData.getAgreementSignDate()) ? cmrData.getAgreementSignDate() : "");
    messageHash.put("TeleCovRep", !StringUtils.isEmpty(cmrData.getBpSalesRepNo()) ? cmrData.getBpSalesRepNo() : "");
    messageHash.put("BankBranchNo", !StringUtils.isEmpty(cmrData.getCompany()) ? cmrData.getCompany() : "");
    messageHash.put("BankAccountNo", !StringUtils.isEmpty(cmrData.getTaxCd1()) ? cmrData.getTaxCd1() : "");
    messageHash.put("EnterpriseNo", !StringUtils.isEmpty(cmrData.getEnterprise()) ? cmrData.getEnterprise() : "");
    messageHash.put("CreditCode", !StringUtils.isEmpty(cmrData.getCreditCd()) ? cmrData.getCreditCd() : "");
    messageHash.put("CurrencyCode", !StringUtils.isEmpty(cmrData.getLegacyCurrencyCd()) ? cmrData.getLegacyCurrencyCd() : "");

    boolean internal = custType.contains("IN");
    boolean bp = MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || custType.contains("BP");

    if (!update) {
      String cmrNo = cmrData.getCmrNo();
      if (StringUtils.isEmpty(cmrNo) && "XCEM".equals(custType)) {
        cmrNo = generateCMRNoForDualCreate(handler, cmrData.getCmrIssuingCntry(), false, true, false);
      } else if (StringUtils.isEmpty(cmrNo) && !SystemLocation.AUSTRIA.equals(cmrData.getCmrIssuingCntry()) && bp) {
        // CEEME cmrNo range for BP records 002000 to 009999
        cmrNo = generateCMRNoForDualCreate(handler, cmrData.getCmrIssuingCntry(), false, false, true);
      }
      messageHash.put("CustomerNo", !StringUtils.isEmpty(cmrNo) ? cmrNo : "");
    }

    String duplicateIndicator = getDuplicateIndicator(handler);
    if (MQMsgConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType())
        && MQMsgConstants.REQ_STATUS_NEW.equals(handler.mqIntfReqQueue.getReqStatus()) && duplicateIndicator != null
        && StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
      LOG.debug("Duplicate process scenario for MQ ID " + handler.mqIntfReqQueue.getId().getQueryReqId() + " Request ID "
          + handler.cmrData.getId().getReqId());
      String cmrNo = cmrData.getCmrNo();

      if (StringUtils.isEmpty(cmrNo)) {
        if (GULF_DUPLICATE.equals(duplicateIndicator)) {
          cmrNo = generateCMRNoForDualCreate(handler, SystemLocation.GULF, internal, false, bp);
        } else {
          cmrNo = generateCMRNoForDualCreate(handler, cmrData.getDupIssuingCntryCd(), internal, false, bp);
        }
        if (cmrNo == null) {
          LOG.warn("Warning, a CMR No cannot be generated for the duplicate process scenario.");
        } else {
          LOG.info("CMR No " + cmrNo + " found as next available for duplicate process scenario.");
          messageHash.put("CustomerNo", cmrNo);
        }
      } else {
        LOG.debug("CMR No. " + cmrNo + " specified for dual creates. Using this CN.");
      }
    } else if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
      LOG.debug("Correlated request with MQ ID " + handler.mqIntfReqQueue.getCorrelationId() + ", setting CMR No. "
          + handler.mqIntfReqQueue.getCmrNo());
      messageHash.put("CustomerNo", handler.mqIntfReqQueue.getCmrNo());
    }

    if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
      if (GULF_DUPLICATE.equals(duplicateIndicator)) {
        cebo = cmrData.getCmrIssuingCntry();
        if (!StringUtils.isEmpty(cebo)) {
          cebo = StringUtils.rightPad(cebo, 7, '0');
        }
        messageHash.put("DPCEBO", cebo);
        messageHash.put("CustomerLocationNo", !StringUtils.isEmpty(cmrData.getSubIndustryCd()) ? "675" + cmrData.getSubIndustryCd() : "");
        messageHash.put("EnterpriseNo", "");
      } else if (CIS_DUPLICATE.equals(duplicateIndicator)) {
        String isu = cmrData.getDupIsuCd() + cmrData.getDupClientTierCd();
        messageHash.put("ISU", isu);
        messageHash.put("SR", cmrData.getDupSalesRepNo());
        sbo = cmrData.getDupSalesBoCd();

        if (!StringUtils.isEmpty(sbo)) {
          sbo = StringUtils.rightPad(sbo, 7, '0');
        }
        messageHash.put("SBO", sbo);
        messageHash.put("IBO", sbo);
        messageHash.put("EnterpriseNo", cmrData.getDupEnterpriseNo());
        messageHash.put("CustomerLocationNo",
            !StringUtils.isEmpty(cmrData.getSubIndustryCd()) ? cmrData.getDupIssuingCntryCd() + cmrData.getSubIndustryCd() : "");
      }
      messageHash.put("CollectionCode", "");
    }

    messageHash.put("CustomerType", "N");
    if (SystemLocation.ABU_DHABI.equals(cmrData.getCmrIssuingCntry()) || SystemLocation.GULF.equals(cmrData.getCmrIssuingCntry())) {
      messageHash.put("CustomerType", !StringUtils.isEmpty(cmrData.getBpAcctTyp()) ? cmrData.getBpAcctTyp() : "");
    }

  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    
    String transCode = messageHash.get("TransactionCode");
    boolean addrUpdate = !StringUtils.isEmpty(transCode) && transCode.equals("M") ? true : false;

    String addrKey = getAddressKey(addrData.getId().getAddrType());
    String addrNumKey = getDuplicateAddressType(addrData.getId().getAddrType());

    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    boolean crossBorder = isCrossBorder(addrData);

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";

    // line1 is always customer name
    line1 = addrData.getCustNm1();

    if (!SystemLocation.AUSTRIA.equals(cmrData.getCmrIssuingCntry())) {
      // Domestic and Cross-border - CEE, ME
      // line2 = Name2 or (Name2 + PoBox)
      // line3 = Name3 or (Name3 + PoBox)
      // line4 = Street
      // line5 = City + Postal Code
      // line6 = Country

      boolean localAddressType = isLocalAddress(addrData);

      line2 = !StringUtils.isEmpty(addrData.getCustNm2()) ? addrData.getCustNm2() : "";
      line3 = !StringUtils.isEmpty(addrData.getCustNm3()) ? addrData.getCustNm3() : "";

      // append PO BOX in Name2/Name3 (if exists)
      String poBox = "";
      if (localAddressType) {
        poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "";
      } else {
        poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? "PO BOX " + addrData.getPoBox() : "";
      }
      if (!StringUtils.isEmpty(poBox)) {
        String name2PoBox = (!StringUtils.isEmpty(line2) ? line2 + ", " : "") + poBox;
        String name3PoBox = (!StringUtils.isEmpty(line3) ? line3 + ", " : "") + poBox;

        // prioritize blank line > line3 > line2
        if (StringUtils.isEmpty(line2)) {
          line2 = name2PoBox;
        } else if (StringUtils.isEmpty(line3)) {
          line3 = name3PoBox;
        } else if (name3PoBox.length() <= 30) {
          line3 = name3PoBox;
        } else if (name2PoBox.length() <= 30) {
          line2 = name2PoBox;
        }
      }

      line4 = addrData.getAddrTxt();

      String postCode = addrData.getPostCd() != null ? addrData.getPostCd().trim() : "";
      if (postCode.matches("0*")) {
        postCode = "";
      }
      line5 = (!StringUtils.isBlank(postCode) ? postCode + " " + addrData.getCity1() : addrData.getCity1());

      // handle landedCountry for 707 (CS/ME/RS)
      if (SystemLocation.SERBIA.equals(cmrData.getCmrIssuingCntry()) && !StringUtils.isEmpty(cmrData.getCountryUse()) && !crossBorder) {
        String cntryNm = "Serbia";
        String cntryKey = "RS"; // to retrieve local country name
        if ("707ME".equals(cmrData.getCountryUse()) && !cntryKey.equals(addrData.getLandCntry())) {
          cntryNm = "Montenegro";
          cntryKey = "ME";
        } else if ("707CS".equals(cmrData.getCountryUse()) && !cntryKey.equals(addrData.getLandCntry())) {
          cntryNm = "Kosovo";
          cntryKey = "CS";
        }

        if (localAddressType) {
          /*
           * 1496135: Importing G address from SOF for Update Requests jz: add
           * local country name text box
           */
          line6 = addrData.getBldg();// LandedCountryMap.getLocalCountryName(cntryKey);
        } else {
          line6 = cntryNm;
        }
      } else if (localAddressType) {
        /*
         * 1496135: Importing G address from SOF for Update Requests jz: add
         * local country name text box
         */
        line6 = addrData.getBldg();// LandedCountryMap.getLocalCountryName(cntryKey);
      } else {
        line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      }
    } else {
      // Austria
      // Domestic and Cross-border - Austria
      // line2 = Name2
      // line3 = Name3 or (ATT + phone)
      // line4 = Street or (Street + PO Box)
      // line5 = Postal Code for domestic
      // line6 = City for domestic, Landed Country CODE + City for cross border

      line2 = !StringUtils.isEmpty(addrData.getCustNm2()) ? addrData.getCustNm2() : "";
      line3 = !StringUtils.isEmpty(addrData.getCustNm3()) ? addrData.getCustNm3() : "";
      if (StringUtils.isEmpty(line3)) {
        if (!StringUtils.isEmpty(addrData.getCustNm4())) {
          line3 = addrData.getCustNm4() + (!StringUtils.isEmpty(addrData.getCustPhone()) ? ", " + addrData.getCustPhone() : "");
        } else {
          line3 = StringUtils.isEmpty(addrData.getCustPhone()) ? addrData.getCustPhone() : "";
        }
      }

      line4 = !StringUtils.isEmpty(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "";
      String poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "";
      if (!StringUtils.isEmpty(poBox)) {
        poBox = "PO BOX " + poBox;
        line4 = (StringUtils.isEmpty(line4) ? "" : line4 + ", ") + poBox;
      }

      line5 = addrData.getPostCd();

      if (crossBorder) {
        line6 = addrData.getLandCntry() + "-" + addrData.getCity1();
      } else {
        line6 = addrData.getCity1();
      }

    }

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    for (String line : lines) {
      messageHash.put(addrKey + "Address" + lineNo, line);
      lineNo++;
    }

    // use the address sequence of duplicate record if address update
    if (addrUpdate && !StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
      loadDuplicateRecord(handler.mqIntfReqQueue.getCmrNo(), handler.mqIntfReqQueue.getCmrIssuingCntry());

      if (dupCMRValues != null && !dupCMRValues.isEmpty()) {
        String addrSeq = dupCMRValues.get(addrNumKey + "AddressNumber");
        if (!StringUtils.isEmpty(addrSeq)) {
          messageHash.put("AddressNumber", StringUtils.leftPad(addrSeq, 5, '0'));
        }
      }
    }
    // Story 1733554 Moroco for ICE field
    if (SystemLocation.MOROCCO.equals(cmrData.getCmrIssuingCntry())) {
      String addrTyp = !StringUtils.isEmpty(addrData.getId().getAddrType()) ? addrData.getId().getAddrType() : "";
      String ice = !StringUtils.isEmpty(addrData.getDept()) ? addrData.getDept() : "";
      if ("ZP01".equalsIgnoreCase(addrTyp) && "XXX".equalsIgnoreCase(ice)) {
        messageHash.put("ICE", "");
      } else if ("ZP01".equalsIgnoreCase(addrTyp)) {
        messageHash.put("ICE", ice);
      }
    }
 }

  protected boolean isCrossBorder(Addr addr) {
    String cd = CEMEAHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    return cd != null && !cd.equals(addr.getLandCntry());
  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Install";
    case "ZI01":
      return "Mail";
    case "ZD01":
      return "Ship";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "Soft";
    case "ZP02":
      return "CntryUseG";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Installing";
    case "ZI01":
      return "Mailing";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "EPL";
    case "ZP02":
      return "CountryUseG";
    default:
      return "";
    }
  }

  protected String getDuplicateAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Installing";
    case "ZI01":
      return "Mailing";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "EplMailing";
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
    return "3";
  }

  private boolean isLocalAddress(Addr addr) {
    return Arrays.asList("ZP02").contains(addr.getId().getAddrType());
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZP02:
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_G;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  /**
   * Calls the Generate CMR no service to get the next available CMR no
   * 
   * @param handler
   * @param targetSystem
   * @return
   */
  protected String generateCMRNoForDualCreate(MQMessageHandler handler, String targetSystem, boolean internal, boolean cemex, boolean bp) {

    try {
      GenerateCMRNoRequest request = new GenerateCMRNoRequest();
      request.setLoc1(handler.mqIntfReqQueue.getCmrIssuingCntry());
      request.setLoc2(targetSystem);
      request.setMandt(SystemConfiguration.getValue("MANDT"));
      request.setSystem(GenerateCMRNoClient.SYSTEM_SOF);
      if (internal) {
        request.setMin(990000);
        request.setMax(999999);
      } else if (cemex) {
        request.setMin(500000);
        request.setMax(799999);
      } else if (bp) {
        request.setMin(2000);
        request.setMax(9999);
      }
      LOG.debug("Generating CMR No for " + handler.mqIntfReqQueue.getCmrIssuingCntry() + " and " + targetSystem);
      GenerateCMRNoClient client = CmrServicesFactory.getInstance().createClient(BaseBatchService.BATCH_SERVICE_URL, GenerateCMRNoClient.class);

      GenerateCMRNoResponse response = client.executeAndWrap(request, GenerateCMRNoResponse.class);

      if (response.isSuccess()) {
        return response.getCmrNo();
      } else {
        LOG.error("CMR No cannot be generated. Error: " + response.getMsg());
        return null;
      }
    } catch (Exception e) {
      LOG.error("Error in generating CMR no", e);
      return null;
    }

  }

  protected boolean hasDuplicateRecord(MQMessageHandler handler, String dupCntry, String duplicateIndicator) {
    String cmrNo = handler.mqIntfReqQueue.getCmrNo();
    String origCntry = handler.mqIntfReqQueue.getCmrIssuingCntry();
    loadDuplicateRecord(cmrNo, dupCntry);

    if (dupCMRValues != null && !dupCMRValues.isEmpty()) {
      String responseCmrNo = dupCMRValues.get("CustomerNo");
      if (StringUtils.isEmpty(responseCmrNo)) {
        LOG.debug("No duplicate record found for cmrNo: " + cmrNo + " under ctyCode: " + dupCntry);
        return false;
      }

      // verify the duplicate record
      if (GULF_DUPLICATE.equals(duplicateIndicator)) {
        String eBo = dupCMRValues.get("DPCEBO");
        // for GULF duplicate, check eBO
        if (eBo != null && eBo.startsWith(origCntry)) {
          LOG.trace("675 Duplicate record exists for cmrNo: " + cmrNo);
          return true;
        }
      } else if (CIS_DUPLICATE.equals(duplicateIndicator)) {
        String abbrevNm = dupCMRValues.get("CompanyName");
        if (abbrevNm != null && abbrevNm.endsWith(" CIS")) {
          LOG.trace("CIS Duplicate record exists for cntry: " + dupCntry + ", cmrNo: " + cmrNo);
          return true;
        }
      }
    }
    return false;
  }

  protected void loadDuplicateRecord(String cmrNo, String dupCntry) {
    SOFQueryRequest request = new SOFQueryRequest();
    request.setCmrIssuingCountry(dupCntry);
    request.setCmrNo(cmrNo);

    LOG.info("Retrieving Legacy values for duplicate CMR No " + cmrNo + " from SOF (" + dupCntry + ")");

    try {
      SOFServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"),
          SOFServiceClient.class);
      SOFQueryResponse response = client.executeAndWrap(SOFServiceClient.QUERY_APP_ID, request, SOFQueryResponse.class);
      if (response.isSuccess()) {
        String xmlData = response.getData();

        GenericSOFMessageParser dupHandler = new GenericSOFMessageParser();
        ByteArrayInputStream bis = new ByteArrayInputStream(xmlData.getBytes());
        try {
          SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
          parser.parse(new InputSource(bis), dupHandler);
        } finally {
          bis.close();
        }

        dupCMRValues = dupHandler.getValues();
        dupShippingSequences = dupHandler.getShippingSequences();

        if (dupShippingSequences != null && !dupShippingSequences.isEmpty()) {
          LOG.trace("Duplicate Shipping Sequences: " + dupShippingSequences.size());
        } else {
          LOG.trace("Duplicate Shipping Sequences is empty");
        }

        if (dupCMRValues != null && !dupCMRValues.isEmpty()) {
          String responseCmrNo = dupCMRValues.get("CustomerNo");
          if (StringUtils.isEmpty(responseCmrNo)) {
            LOG.debug("No duplicate record found for cmrNo: " + cmrNo + " under ctyCode: " + dupCntry);
          }
        }
      }
    } catch (Exception e) {
      LOG.warn("An error has occurred during retrieval of the duplicate values.", e);
      dupCMRValues = new HashMap<String, String>();
    }
  }

  @Override
  public boolean shouldCompleteProcess(EntityManager entityManager, MQMessageHandler handler, String responseStatus, boolean fromUpdateFlow) {
    try {
      if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
        return true;
      }
      String duplicateIndicator = getDuplicateIndicator(handler);
      if (duplicateIndicator != null) {
        MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
        Data data = handler.cmrData;
        boolean update = "U".equals(handler.adminData.getReqType());
        if ("Y".equals(currentQ.getMqInd()) || MQMsgConstants.REQ_STATUS_COM.equals(currentQ.getReqStatus())) {
          LOG.debug("MQ record already previously completed, skipping implement dual create/update process.");
          return true;
        }
        LOG.debug("Completing initial request " + currentQ.getId().getQueryReqId());
        Timestamp ts = SystemUtil.getCurrentTimestamp();
        currentQ.setReqStatus(MQMsgConstants.REQ_STATUS_COM);
        currentQ.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
        currentQ.setLastUpdtTs(ts);
        currentQ.setMqInd("Y");

        MqIntfReqQueue duplicateQ = new MqIntfReqQueue();
        MqIntfReqQueuePK duplicateQPK = new MqIntfReqQueuePK();
        long id = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "QUERY_REQ_ID", "CREQCMR");
        if (GULF_DUPLICATE.equals(duplicateIndicator)) {
          LOG.debug("Creating request for 675 with MQ ID " + id);
          duplicateQ.setCmrIssuingCntry(SystemLocation.GULF);
        } else {
          LOG.debug("Creating request for " + data.getDupIssuingCntryCd() + " with MQ ID " + id);
          duplicateQ.setCmrIssuingCntry(data.getDupIssuingCntryCd());
        }
        duplicateQPK.setQueryReqId(id);
        duplicateQ.setId(duplicateQPK);

        duplicateQ.setCmrNo(currentQ.getCmrNo());
        duplicateQ.setCorrelationId(currentQ.getId().getQueryReqId() + "");
        duplicateQ.setCreateBy(MQMsgConstants.MQ_APP_USER);
        duplicateQ.setCreateTs(ts);
        duplicateQ.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
        duplicateQ.setLastUpdtTs(ts);
        duplicateQ.setMqInd("N");
        duplicateQ.setReqId(currentQ.getReqId());
        duplicateQ.setReqStatus(MQMsgConstants.REQ_STATUS_NEW);
        if (update && !duplicateRecordFound) {
          duplicateQ.setReqType(CmrConstants.REQ_TYPE_CREATE);
        } else {
          duplicateQ.setReqType(currentQ.getReqType());
        }

        duplicateQ.setTargetSys(currentQ.getTargetSys());

        if (GULF_DUPLICATE.equals(duplicateIndicator)) {
          handler.createPartialComment("Handling Gulf (675) record for dual process scenario", handler.mqIntfReqQueue.getCmrNo());
        } else {
          handler.createPartialComment("Handling System Location " + data.getDupIssuingCntryCd() + " record for dual process scenario",
              handler.mqIntfReqQueue.getCmrNo());
        }
        entityManager.merge(currentQ);
        entityManager.persist(duplicateQ);
        entityManager.flush();

        return false;
      } else {
        LOG.debug("No need to create duplicates. Completing request.");
        return true;
      }
    } catch (Exception e) {
      LOG.error("Error in completing dual process request. Skipping dual process and completing request", e);
      return true;
    }
  }

  /**
   * Returns either GULF or CIS or null
   * 
   * @param handler
   * @return
   */
  private String getDuplicateIndicator(MQMessageHandler handler) {
    Data data = handler.cmrData;
    if (data == null) {
      return null;
    }

    boolean update = "U".equals(handler.adminData.getReqType());
    String cntry = handler.mqIntfReqQueue.getCmrIssuingCntry();
    if (!update) {
      if ("Y".equals(data.getDupCmrIndc())) {
        // this is to create a 675 cmr
        return GULF_DUPLICATE;
      }
      if ("Y".equals(data.getCisServiceCustIndc())) {
        // this is for CIS Service Customer scenario create
        return CIS_DUPLICATE;
      }
    } else {
      if ("Y".equals(data.getDupCmrIndc())) {
        // 675 ticked - this is to update 675 cmr (create if not exists)
        if (hasDuplicateRecord(handler, SystemLocation.GULF, GULF_DUPLICATE)) {
          duplicateRecordFound = true;
        }
        return GULF_DUPLICATE;
      }
      if ("Y".equals(data.getCisServiceCustIndc())) {
        // this is for CIS scenario update (create if not exists)
        if (hasDuplicateRecord(handler, data.getDupIssuingCntryCd(), CIS_DUPLICATE)) {
          duplicateRecordFound = true;
        }
        return CIS_DUPLICATE;
      }

      // 675 not ticked - check if record has duplicate (for 675 case only)
      if (Arrays.asList(GULF_ORIGINAL_COUNTRIES).contains(cntry)) {
        // GULF original candidate
        if (hasDuplicateRecord(handler, SystemLocation.GULF, GULF_DUPLICATE)) {
          duplicateRecordFound = true;
          return GULF_DUPLICATE;
        }
      }
    }

    duplicateRecordFound = false;
    return null;

  }

  @Override
  public boolean shouldSendAddress(EntityManager entityManager, MQMessageHandler handler, Addr nextAddr) {
    if (nextAddr == null) {
      return false;
    }
    if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && "ZP02".equals(nextAddr.getId().getAddrType())) {
      return false;
    }

    return true;
  }
}
