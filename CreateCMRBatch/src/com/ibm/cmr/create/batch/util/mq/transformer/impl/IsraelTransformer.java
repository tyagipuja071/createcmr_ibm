/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
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
 * {@link MessageTransformer} implementation for Israel.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class IsraelTransformer extends EMEATransformer {

  private static final Logger LOG = Logger.getLogger(IsraelTransformer.class);

  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode", "ARemark" };

  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02", "CTYA", "CTYB", "CTYC" };

  private static final String[] TRANS_ADDRS = { "CTYA", "CTYB", "CTYC" };
  private static final String SPLIT_MARKER = "SPLIT";

  private static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  private static final String CMR_REQUEST_STATUS_CPR = "CPR";
  private static final String CMR_REQUEST_STATUS_PCR = "PCR";

  // private static final String RIGHT_TO_LEFT_MARKER = "\u202e";

  /**
   */
  public IsraelTransformer() {
    super(SystemLocation.ISRAEL);

  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("LD - transformLegacyCustomerData ISRAEL transformer...");
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    formatDataLines(dummyHandler);

    String kukla = !StringUtils.isEmpty(data.getCustClass()) ? data.getCustClass() : "";
    String kuklaVal = "";

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      // Creates only mapping
      kuklaVal = getDB2KuklaValue(kukla);
      if (!StringUtils.isEmpty(kuklaVal)) {
        legacyCust.setCustType(kuklaVal);
      }
      boolean isLocalFlag = false;
      for (Addr addrVal : cmrObjects.getAddresses()) {
        if ("ZS01".equals(addrVal.getId().getAddrType()) && "IL".equals(addrVal.getLandCntry())) {
          isLocalFlag = true;
          break;
        }
      }

      if (isLocalFlag) {
        String currentVat = data.getVat().toUpperCase();
        String vatSubstr = currentVat.contains("IL") ? currentVat.replace("IL", "") : currentVat;
        legacyCust.setVat(vatSubstr);
      } else {
        if (!StringUtils.isEmpty(data.getVat())) {
          legacyCust.setVat(data.getVat());
        }
      }
      legacyCust.setCollectionCd("TC0");
      legacyCust.setDcRepeatAgreement("0");
      legacyCust.setCeDivision("2");
      legacyCust.setDeptCd("3");
      legacyCust.setInvoiceCpyReqd("04");

      String custType = data.getCustSubGrp();
      if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType)) {
        legacyCust.setAuthRemarketerInd("1");
        legacyCust.setMrcCd("5");
      } else {
        legacyCust.setAuthRemarketerInd("0");
        legacyCust.setMrcCd("2");
      }

      if ("INTER".equals(custType) || "INTSO".equals(custType)) {
        legacyCust.setCreditCd("91");
      } else if ("MOD".equals(custType)) {
        legacyCust.setCreditCd("01");
      } else {
        legacyCust.setCreditCd("90");
      }

      String subtype = data.getCustGrp();
      if (subtype.equals("LOCAL")) {
        legacyCust.setBankNo("0");
      }

      if (StringUtils.isNotEmpty(data.getMiscBillCd())) {
        if (data.getMiscBillCd().equals("NO")) {
          legacyCust.setRealCtyCd("755");
          legacyCust.setBankNo("0"); // RBKXA
          if (!"INTER".equals(custType) || !"INTSO".equals(custType)) {
            legacyCust.setCreditCd("90");
          }
        } else if (data.getMiscBillCd().equals("IBM")) {
          legacyCust.setRealCtyCd("756");
          legacyCust.setBankNo("9");
          if (!"INTER".equals(custType) || !"INTSO".equals(custType)) {
            legacyCust.setCreditCd("01");
          }
        } else if (data.getMiscBillCd().equals("WTC")) {
          legacyCust.setRealCtyCd("756");
          legacyCust.setBankNo("0");
          if (!"INTER".equals(custType) || !"INTSO".equals(custType)) {
            legacyCust.setCreditCd("01");
          }
        }
      }
      if ("INTER".equals(custType) || "INTSO".equals(custType)) {
        legacyCust.setCreditCd("91");
      }
      legacyCust.setLeasingInd("0"); // default Leasing Indicator

    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      // Update only mapping
      DataRdc dataRdc = LegacyDirectUtil.getOldData(entityManager, String.valueOf(data.getId().getReqId()));
      if (dataRdc != null && !data.getCustClass().equals(dataRdc.getCustClass())) {
        kuklaVal = getDB2KuklaValue(kukla);
        if (!StringUtils.isEmpty(kuklaVal)) {
          legacyCust.setCustType(kuklaVal);
        }
      }

      if (!StringUtils.isEmpty(data.getCreditCd())) {
        legacyCust.setDeptCd(data.getCreditCd());
      }

      if (!StringUtils.isEmpty(data.getEnterprise())) {
        legacyCust.setEnterpriseNo(data.getEnterprise());
      } else {
        legacyCust.setEnterpriseNo("");
      }

      boolean isLocalFlag = false;
      for (Addr addrVal : cmrObjects.getAddresses()) {
        if ("ZS01".equals(addrVal.getId().getAddrType()) && "IL".equals(addrVal.getLandCntry())) {
          isLocalFlag = true;
          break;
        }
      }

      if (isLocalFlag) {
        String currentVat = data.getVat().toUpperCase();
        String vatSubstr = currentVat.contains("IL") ? currentVat.replace("IL", "") : currentVat;
        legacyCust.setVat(vatSubstr);
      } else {
        if (!StringUtils.isEmpty(data.getVat())) {
          legacyCust.setVat(data.getVat());
        }
      }

      String dataEmbargoCd = data.getEmbargoCd();
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin);

      if (StringUtils.isNotBlank(admin.getReqReason()) && StringUtils.isNotBlank(rdcEmbargoCd) && "D".equals(rdcEmbargoCd)) {
        if (StringUtils.isBlank(data.getEmbargoCd()) && !CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason())) {
          legacyCust.setEmbargoCd("");
        } else if (CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && StringUtils.isNotEmpty(admin.getReqStatus())
            && StringUtils.isBlank(dataEmbargoCd)) {
          if (admin.getReqStatus().equals(CMR_REQUEST_STATUS_CPR)) {
            legacyCust.setEmbargoCd("");
            data.setOrdBlk("");
            entityManager.merge(data);
            entityManager.flush();
          } else if (admin.getReqStatus().equals(CMR_REQUEST_STATUS_PCR)) {
            legacyCust.setEmbargoCd(rdcEmbargoCd);
            data.setOrdBlk("88");
            entityManager.merge(data);
            entityManager.flush();
          }
        }
      }

      if (StringUtils.isNotEmpty(data.getMiscBillCd())) {
        if (data.getMiscBillCd().equals("NO")) {
          legacyCust.setRealCtyCd("755");
          legacyCust.setBankNo("0"); // RBKXA
        } else if (data.getMiscBillCd().equals("IBM")) {
          legacyCust.setRealCtyCd("756");
          legacyCust.setBankNo("9");
        } else if (data.getMiscBillCd().equals("WTC")) {
          legacyCust.setRealCtyCd("756");
          legacyCust.setBankNo("0");
        }
      }
    }

    String isuCd = !StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "";
    String clientTier = !StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : "";

    if (StringUtils.isEmpty(clientTier)) {
      if (!StringUtils.isEmpty(isuCd)) {
        isuCd = isuCd.concat("7");
      }
      legacyCust.setIsuCd(isuCd);
    } else {
      legacyCust.setIsuCd(isuCd.concat(clientTier));
    }

    for (Addr addr : cmrObjects.getAddresses()) {
      if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
        legacyCust.setTelNoOrVat(addr.getCustPhone());
      }
    }

    if (StringUtils.isNotBlank(data.getSubIndustryCd())) {
      legacyCust.setDistrictCd("0" + data.getSubIndustryCd().substring(0, 1));
    }

    if (StringUtils.isNotBlank(data.getRepTeamMemberNo())) {
      legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
    }

    legacyCust.setEconomicCd("");

  }

  private String getDB2KuklaValue(String kukla) {
    String kuklaVal = "";

    if (!StringUtils.isEmpty(kukla)) {
      switch (kukla) {
      case "71":
        kuklaVal = "98";
        break;
      case "33":
        kuklaVal = "33";
        break;
      default:
        kuklaVal = "WR";
        break;
      }
    }
    return kuklaVal;
  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    boolean update = "U".equals(handler.adminData.getReqType());
    Data cmrData = handler.cmrData;
    Addr addrData = handler.addrData;
    boolean crossBorder = !"IL".equals(addrData.getLandCntry());

    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    handleEMEADefaults(handler, messageHash, cmrData, addrData, crossBorder);

    String embargoCode = !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "";
    messageHash.put("EmbargoCode", embargoCode);

    messageHash.put("SourceCode", "EFO");
    messageHash.put("IBO", cmrData.getInstallBranchOff());
    messageHash.put("Country", SystemLocation.SAP_ISRAEL_SOF_ONLY);
    messageHash.put("MarketingResponseCode", "2");
    // 1317260 - For BP Scenarios send ARemark = YES when MRC code is 5
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(cmrData.getCustSubGrp())) {
      messageHash.put("MarketingResponseCode", "5");
      messageHash.put("ARemark", "YES");
    } else {
      messageHash.put("ARemark", "");
    }

    messageHash.put("CRCode", "90");

    // new tags for Israel
    messageHash.put("CICode", "3");
    messageHash.put("LangCode", "1");
    messageHash.put("CEdivision", "2");
    messageHash.put("InvNumber", "04");
    if (StringUtils.isNotBlank(cmrData.getSubIndustryCd())) {
      messageHash.put("DistrictCode", "0" + cmrData.getSubIndustryCd().substring(0, 1));
    }
    messageHash.put("BankNumber", "");
    messageHash.put("BankBranchNumber", "");

    // MOD scenario
    String vat = cmrData.getVat();

    LOG.trace("Bank Number: " + handler.currentCMRValues.get("BankNumber"));
    boolean isMOD = handler.currentCMRValues.get("BankNumber") != null && handler.currentCMRValues.get("BankNumber").startsWith("9");
    LOG.debug("CMR is determined to be " + (isMOD ? "" : "non-") + "MOD from query service.");
    if ("MOD".equals(cmrData.getCustSubGrp()) || isMOD) {
      messageHash.put("CRCode", "01");
      if (!StringUtils.isEmpty(vat)) {
        messageHash.put("BankNumber", "9" + cmrData.getVat().substring(vat.length() - 1));
      }
    } else {
      if (!StringUtils.isEmpty(handler.cmrData.getVat())) {
        messageHash.put("BankNumber", "0" + cmrData.getVat().substring(vat.length() - 1));
      }
    }

    if (!crossBorder) {
      if (vat != null && vat.length() > 4) {
        messageHash.put("BankBranchNumber", vat.substring(2, vat.length() - 1));
      } else {
        messageHash.put("BankBranchNumber", "");
      }
    } else {
      // empty bank number and bank branch number for cross border
      messageHash.put("BankNumber", "");
      messageHash.put("BankBranchNumber", "");
    }

    if ("INTER".equals(cmrData.getCustSubGrp()) || "INTSO".equals(cmrData.getCustSubGrp())) {
      messageHash.put("CRCode", "91");
    }

    messageHash.put("EnterpriseNo", cmrData.getEnterprise());
    messageHash.put("CustomerType", "WR");

    if (StringUtils.isBlank(cmrData.getEconomicCd())) {
      if (cmrData.getSubIndustryCd() != null) {
        messageHash.put("EconomicCode", "0" + cmrData.getSubIndustryCd());
      }
    } else {
      messageHash.put("EconomicCode", cmrData.getEconomicCd());
    }

    // COD Flag
    String codFlag = !StringUtils.isEmpty(cmrData.getCreditCd()) ? cmrData.getCreditCd() : "";
    messageHash.put("COD", codFlag);

    if (update) {

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

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    LOG.debug("LD - transformLegacyAddressData ISRAEL transformer...");

    formatAddressLinesLD(dummyHandler, legacyAddr);

    String addrType = currAddr.getId().getAddrType();
    boolean isUpdate = "U".equals(cmrObjects.getAdmin().getReqType());
    boolean isNewPairedCreateReq = !isUpdate && Arrays.asList(TRANS_ADDRS).contains(addrType);
    boolean isNewPairedUpdateReq = isUpdate && Arrays.asList(TRANS_ADDRS).contains(addrType) && ("N".equals(currAddr.getImportInd()));

    if (isNewPairedCreateReq || isNewPairedUpdateReq) {
      legacyAddr.setAddrLineO(currAddr.getPairedAddrSeq());

      if (!isUpdate) {
        if ("CTYA".equals(addrType)) {
          legacyAddr.setAddrLineO("00001");
        } else if ("CTYB".equals(addrType)) {
          legacyAddr.setAddrLineO("00002");
        }
      }

    } else if (isUpdate && !"N".equals(currAddr.getImportInd()) && legacyAddr.isForCreate() && Arrays.asList(TRANS_ADDRS).contains(addrType)) {
      legacyAddr.setAddrLineO(SPLIT_MARKER);
    } else if (isUpdate && Arrays.asList(TRANS_ADDRS).contains(addrType) && StringUtils.isBlank(legacyAddr.getAddrLineO())) {
      legacyAddr.setAddrLineO(currAddr.getPairedAddrSeq());
    }
  }

  public void formatAddressLinesLD(MQMessageHandler handler, CmrtAddr legacyAddr) {
    Addr addrData = handler.addrData;
    boolean update = "U".equals(handler.adminData.getReqType());
    boolean crossBorder = !"IL".equals(addrData.getLandCntry());

    String addrKey = getAddressKey(addrData.getId().getAddrType());
    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    messageHash.put("SourceCode", "EFO");
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    LOG.debug("Handling  Data for " + addrData.getCustNm1());
    String addrlU = "";

    // customer name
    String line1 = "";
    if (StringUtils.isNotBlank(addrData.getCustNm1())) {
      line1 = addrData.getCustNm1();
      addrlU += "D";
    }

    // customer name con't
    String line2 = "";
    if (StringUtils.isNotBlank(addrData.getCustNm2())) {
      line2 = addrData.getCustNm2();
      addrlU += "E";
    }

    // attention person
    String line3 = addrData.getDept();
    if (StringUtils.isNotBlank(addrData.getDept())) {
      line3 = addrData.getDept();
      addrlU += "B";
    }

    // Street
    String line4 = "";
    if (StringUtils.isNotBlank(addrData.getAddrTxt())) {
      line4 = addrData.getAddrTxt();
      addrlU += "F";
    }

    // Address Cont
    String line5 = "";
    if (StringUtils.isNotBlank(addrData.getAddrTxt2())) {
      line5 = addrData.getAddrTxt2();
      addrlU += "G";
    }

    // PO BOX
    String line6 = "";
    if (StringUtils.isNotBlank(addrData.getPoBox())) {
      String addrType = addrData.getId().getAddrType();
      if (addrType.equals("ZS01") || addrType.equals("ZP01") || addrType.equals("ZD01")) {
        line6 = "מ.ד " + addrData.getPoBox();
      } else {
        line6 = "PO BOX " + addrData.getPoBox();
      }
      addrlU += "H";
    }

    String line7 = "";
    // postal code + city
    if (StringUtils.isNotBlank(addrData.getPostCd()) || StringUtils.isNotBlank(addrData.getCity1())) {
      line7 = (StringUtils.isNotBlank(addrData.getPostCd()) ? addrData.getPostCd() : "") + " "
          + (StringUtils.isNotBlank(addrData.getCity1()) ? addrData.getCity1() : "");
      line7 = line7.trim();
      addrlU += "I";
    }

    // country
    String line8 = "";
    boolean localAddressType = isLocalAddress(addrData);
    if (crossBorder) {
      if (localAddressType) {
        line8 = LandedCountryMap.getLovCountryName(addrData.getLandCntry());
      } else {
        line8 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      }
      addrlU += "J";
    }

    int lineNo = 1;
    String[] lines = new String[] { line1, line2, line3, line4, line5, line6, line7, line8 };
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6 + " | " + line7 + " | " + line8);
    for (String line : lines) {
      // shifting logic - move up if blank
      if (StringUtils.isNotBlank(line)) {
        if (line != null && line.length() > 30) {
          line = line.substring(0, 30);
        }
        messageHash.put(addrKey + "AddressLD" + lineNo, line);
        lineNo++;
      }
    }

    String countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());
    messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Country", crossBorder ? countryName : "");

    legacyAddr.setAddrLine1(messageHash.get(addrKey + "AddressLD1"));
    legacyAddr.setAddrLine2(messageHash.get(addrKey + "AddressLD2"));
    legacyAddr.setAddrLine3(messageHash.get(addrKey + "AddressLD3"));
    legacyAddr.setAddrLine4(messageHash.get(addrKey + "AddressLD4"));
    legacyAddr.setAddrLine5(messageHash.get(addrKey + "AddressLD5"));
    legacyAddr.setAddrLine6(messageHash.get(addrKey + "AddressLD6"));
    legacyAddr.setAddrLineU(addrlU);
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Addr addrData = handler.addrData;
    boolean update = "U".equals(handler.adminData.getReqType());
    boolean crossBorder = !"IL".equals(addrData.getLandCntry());

    String addrKey = getAddressKey(addrData.getId().getAddrType());
    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    messageHash.put("SourceCode", "EFO");
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    LOG.debug("Handling  Data for " + addrData.getCustNm1());
    // <XXXAddress1> -> name
    // <XXXAddress2> -> ? no contact
    // <XXXAddress3> -> PO BOX (Department when street is not filled)
    // <XXXAddress4> -> Street (PO BOX when street is not filled)
    // <XXXAddress5> -> Postal code + City
    // <XXXAddress6> -> Country

    // customer name
    String line1 = addrData.getCustNm1();

    // name con't or attn
    String line2 = StringUtils.isBlank(addrData.getCustNm2()) ? addrData.getDept() : addrData.getCustNm2();

    // add phone to line 2
    if (!StringUtils.isBlank(addrData.getCustPhone())) {
      line2 += ", " + addrData.getCustPhone();
    }

    // PO BOX
    String line3 = "";
    if (!StringUtils.isBlank(addrData.getPoBox())) {
      line3 = addrData.getPoBox();
    }

    // Street
    String line4 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 = addrData.getAddrTxt();
    }

    // postal code + city
    String line5 = (!StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "") + " "
        + (!StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "");
    line5 = line5.trim();

    // country
    String line6 = "";

    boolean localAddressType = isLocalAddress(addrData);

    if (crossBorder) {
      if (localAddressType) {
        line6 = LandedCountryMap.getLovCountryName(addrData.getLandCntry());
      } else {
        line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      }
    }

    int lineNo = 1;
    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    // fixed mapping value, not move up if blank
    for (String line : lines) {
      if (line != null && line.length() > 30) {
        line = line.substring(0, 30);
      }
      messageHash.put(addrKey + "Address" + lineNo, localAddressType ? reverseNumbers(line) : line);
      lineNo++;
    }

    String countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());
    messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Country", crossBorder ? countryName : "");

    if (!update) {
      // for creates, send TransAddressNumber
      if (CmrConstants.ADDR_TYPE.CTYA.toString().equals(addrData.getId().getAddrType())) {
        messageHash.put("TransAddressNumber", "00001");
      } else if (CmrConstants.ADDR_TYPE.CTYB.toString().equals(addrData.getId().getAddrType())) {
        messageHash.put("TransAddressNumber", "00002");
      } else if (CmrConstants.ADDR_TYPE.CTYC.toString().equals(addrData.getId().getAddrType())) {
        List<Addr> addresses = handler.currentAddresses;

        int ctyCIndex = -1;
        int shipIndex = -1;

        int running = -1;
        for (Addr addr : addresses) {
          // count index from CTYC types only
          if (CmrConstants.ADDR_TYPE.CTYC.toString().equals(addr.getId().getAddrType())) {
            running++;
            if (addr.getId().getAddrType().equals(addrData.getId().getAddrType())
                && addr.getId().getAddrSeq().equals(addrData.getId().getAddrSeq())) {
              ctyCIndex = running;
              break;
            }
          }
        }

        running = -1;
        shipIndex = -1;
        if (ctyCIndex >= 0) {
          for (Addr addr : addresses) {
            running++;
            if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addr.getId().getAddrType())) {
              shipIndex++;
              if (shipIndex == ctyCIndex) {
                // found the shipping index for the country use c
                messageHash.put("TransAddressNumber", StringUtils.leftPad((running + 1) + "", 5, '0'));
                LOG.trace("Country Use Trans Address Number: " + messageHash.get("TransAddressNumber"));
              }
            }
          }
        }
      }

    } else {
      LOG.debug("Checking TransNumber for new shipping addresses...");
      if (CmrConstants.ADDR_TYPE.CTYC.toString().equals(addrData.getId().getAddrType())) {
        List<Addr> addresses = handler.currentAddresses;

        int ctyCIndex = -1;
        int shipIndex = -1;

        int running = -1;
        for (Addr addr : addresses) {
          // count index from CTYC types only
          if (CmrConstants.ADDR_TYPE.CTYC.toString().equals(addr.getId().getAddrType()) && !"Y".equals(addr.getImportInd())) {
            running++;
            if (addr.getId().getAddrType().equals(addrData.getId().getAddrType())
                && addr.getId().getAddrSeq().equals(addrData.getId().getAddrSeq())) {
              ctyCIndex = running;
              break;
            }
          }
        }

        running = -1;
        shipIndex = -1;
        if (ctyCIndex >= 0) {
          for (Addr addr : addresses) {
            running++;
            if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addr.getId().getAddrType()) && !"Y".equals(addr.getImportInd())) {
              shipIndex++;
              if (shipIndex == ctyCIndex) {
                // found the shipping index for the country use c
                messageHash.put("TransAddressNumber",
                    addr.getId().getAddrSeq().length() == 5 ? addr.getId().getAddrSeq() : StringUtils.leftPad((running + 1) + "", 5, '0'));
                LOG.trace("Country Use Trans Address Number: " + messageHash.get("TransAddressNumber"));
              }
            }
          }
        }
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
    case "ZS01":
      return "Mail";
    case "ZI01":
      return "Install";
    case "ZD01":
      return "Ship";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "Soft";
    case "CTYA":
      return "CntryUseA";
    case "CTYB":
      return "CntryUseB";
    case "CTYC":
      return "CntryUseC";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Mailing";
    case "ZI01":
      return "Installing";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "EPL";
    case "CTYA":
      return "CountryUseA";
    case "CTYB":
      return "CountryUseB";
    case "CTYC":
      return "CountryUseC";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.SAP_ISRAEL_SOF_ONLY;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "00001";
  }

  /**
   * Determins if this address is a local address for Israel
   * 
   * @param addr
   * @return
   */
  private boolean isLocalAddress(Addr addr) {
    return Arrays.asList("ZS01", "ZP01", "ZD01").contains(addr.getId().getAddrType());
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_CTYA:
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_A;
    case MQMsgConstants.ADDR_CTYB:
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_B;
    case MQMsgConstants.ADDR_CTYC:
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_C;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For IL...");
    String loc1 = generateCMRNoObj.getLoc1();
    if (custSubGrp != null && ("INTER".equals(custSubGrp) || "INTSO".equals(custSubGrp))) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }

  @Override
  public void transformOtherData(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects, CMRRequestContainer cmrObjects) {
    boolean isUpdate = "U".equals(cmrObjects.getAdmin().getReqType());
    // Do not execute for mass update
    if (cmrObjects != null && cmrObjects.getAdmin() != null && StringUtils.isNotEmpty(cmrObjects.getAdmin().getReqType())
        && !"M".equals(cmrObjects.getAdmin().getReqType())) {

      if (isUpdate) {
        List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
        setAddrlOSharedSplit(legacyAddrList);
      }
    }
  }

  private void setAddrlOSharedSplit(List<CmrtAddr> legacyAddrList) {
    String mailingSeq = "";
    String billingSeq = "";
    String shippingSeq = "";
    boolean soldToUpdated = false;
    for (CmrtAddr addr : legacyAddrList) {
      if (isAddrsSharedSeq(addr)) {
        continue;
      }

      // Note: Local addrs comes before the translated addrs -- see
      // getAddressOrder for the address order
      if ("Y".equals(addr.getIsAddrUseMailing())) {
        mailingSeq = addr.getId().getAddrNo();
      } else if ("Y".equals(addr.getIsAddrUseBilling())) {
        billingSeq = addr.getId().getAddrNo();
      } else if ("Y".equals(addr.getIsAddrUseShipping())) {
        shippingSeq = addr.getId().getAddrNo();
      } else if ("Y".equals(addr.getIsAddressUseA()) && SPLIT_MARKER.equals(addr.getAddrLineO())) {
        addr.setAddrLineO(mailingSeq);
        soldToUpdated = true;
      } else if ("Y".equals(addr.getIsAddressUseB()) && (SPLIT_MARKER.equals(addr.getAddrLineO()) || soldToUpdated)) {
        addr.setAddrLineO(billingSeq);
      } else if ("Y".equals(addr.getIsAddressUseC()) && (SPLIT_MARKER.equals(addr.getAddrLineO()) || soldToUpdated)) {
        addr.setAddrLineO(shippingSeq);
      }
    }
  }

  private boolean isAddrsSharedSeq(CmrtAddr legacyAddr) {

    int yFlags = countYFlags(legacyAddr);
    if (yFlags > 1) {
      return true;
    }

    return false;
  }

  private int countYFlags(CmrtAddr addr) {
    int count = 0;

    if ("Y".equals(addr.getIsAddrUseMailing())) {
      count++;
    }

    if ("Y".equals(addr.getIsAddrUseBilling())) {
      count++;
    }

    if ("Y".equals(addr.getIsAddrUseInstalling())) {
      count++;
    }

    if ("Y".equals(addr.getIsAddrUseShipping())) {
      count++;
    }

    if ("Y".equals(addr.getIsAddrUseEPL())) {
      count++;
    }

    if ("Y".equals(addr.getIsAddressUseA())) {
      count++;
    }

    if ("Y".equals(addr.getIsAddressUseB())) {
      count++;
    }

    if ("Y".equals(addr.getIsAddressUseC())) {
      count++;
    }

    return count;
  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust cust, CMRRequestContainer cmrObjects, MassUpdtData muData) {
    // Abbrev Name
    if (StringUtils.isNotBlank(muData.getAbbrevNm())) {
      cust.setAbbrevNm(muData.getAbbrevNm());
    }
    // Abbrev Location
    if (StringUtils.isNotBlank(muData.getAbbrevLocn())) {
      cust.setAbbrevLocn(muData.getAbbrevLocn());
    }
    // ISIC
    if (StringUtils.isNotBlank(muData.getIsicCd())) {
      cust.setIsicCd(muData.getIsicCd());
    }
    // Preferred Language
    if (StringUtils.isNotBlank(muData.getSvcArOffice())) {
      cust.setLangCd(muData.getSvcArOffice());
    }
    // Tax Code
    if (StringUtils.isNotBlank(muData.getSpecialTaxCd()) && !"@".equals(muData.getSpecialTaxCd())) {
      cust.setTaxCd(muData.getSpecialTaxCd());
    }
    // Collection Code
    if (StringUtils.isNotBlank(muData.getCollectionCd())) {
      if ("@".equals(muData.getCollectionCd())) {
        cust.setCollectionCd("");
      } else {
        cust.setCollectionCd(muData.getCollectionCd());
      }
    }
    // COD Flag
    if (StringUtils.isNotBlank(muData.getEntpUpdtTyp()) && !"@".equals(muData.getEntpUpdtTyp())) {
      cust.setDeptCd(muData.getEntpUpdtTyp());
    }
    // Embargo Code
    if (StringUtils.isNotBlank(muData.getMiscBillCd())) {
      if ("@".equals(muData.getMiscBillCd())) {
        cust.setEmbargoCd("");
      } else {
        cust.setEmbargoCd(muData.getMiscBillCd());
      }
    }
    // VAT
    if (StringUtils.isNotBlank(muData.getVat())) {
      cust.setVat(muData.getVat());
    }
    // ISU Code
    if (StringUtils.isNotBlank(muData.getIsuCd())) {
      cust.setIsuCd(muData.getIsuCd());
    }
    // Client Tier
    String ctc = muData.getClientTier();
    if (StringUtils.isNotBlank(ctc)) {
      String isuCd = cust.getIsuCd();
      if (StringUtils.isNotBlank(isuCd)) {
        if (isuCd.length() == 3) {
          isuCd = StringUtils.substring(isuCd, 0, 2);
        }
        if ("@".equals(ctc)) {
          cust.setIsuCd(isuCd.concat("7"));
        } else {
          cust.setIsuCd(isuCd.concat(ctc));
        }
      }
    }
    // Enterprise Number
    if (StringUtils.isNotBlank(muData.getEnterprise())) {
      cust.setEnterpriseNo(muData.getEnterprise());
    }
    // SBO
    if (StringUtils.isNotBlank(muData.getCustNm1())) {
      cust.setSbo(muData.getCustNm1());
    }
    // INAC/NAC
    if (StringUtils.isNotBlank(muData.getInacCd())) {
      if ("@@@@".equals(muData.getInacCd())) {
        cust.setInacCd("");
      } else {
        cust.setInacCd(muData.getInacCd());
      }
    }
    // Sales Rep
    if (StringUtils.isNotBlank(muData.getRepTeamMemberNo())) {
      cust.setSalesRepNo(muData.getRepTeamMemberNo());
    }
    // Phone Number
    if (StringUtils.isNotBlank(muData.getEmail1())) {
      if ("@".equals(muData.getEmail1())) {
        cust.setTelNoOrVat("");
      } else {
        cust.setTelNoOrVat(muData.getEmail1());
      }
    }
    // KUKLA
    String kukla = muData.getCustClass();
    if (StringUtils.isNotBlank(kukla)) {
      if ("71".equals(kukla)) {
        cust.setCustType("98");
      } else if ("33".equals(kukla)) {
        cust.setCustType(kukla);
      } else {
        cust.setCustType("WR");
      }
    }

    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr addr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {
    String addrType = null;
    if (addr.getId() != null && StringUtils.isNotBlank(addr.getId().getAddrType())) {
      addrType = addr.getId().getAddrType();
    }

    legacyAddr.setForUpdate(true);
    if (StringUtils.isNotBlank(addr.getAddrTxt())) {
      legacyAddr.setStreet(addr.getAddrTxt());
    }

    if (StringUtils.isNotBlank(addr.getCity1())) {
      legacyAddr.setCity(addr.getCity1());
    } else if (StringUtils.isNotBlank(addr.getCustNm1()) && StringUtils.isBlank(addr.getCity1())) {
      legacyAddr.setCity("");
    }

    if (StringUtils.isNotBlank(addr.getPostCd())) {
      legacyAddr.setZipCode(addr.getPostCd());
    } else if (StringUtils.isNotBlank(addr.getCustNm1()) && StringUtils.isBlank(addr.getPostCd())) {
      legacyAddr.setZipCode("");
    }

    // Set Address Lines
    StringBuilder sbAddrLu = new StringBuilder();
    List<String> lstAddrLines = new ArrayList<String>();
    // Cust Name
    if (StringUtils.isNotBlank(addr.getCustNm1())) {
      lstAddrLines.add(addr.getCustNm1());
      sbAddrLu.append("D");
    }
    // Cust Name Cont
    if (StringUtils.isNotBlank(addr.getCustNm2())) {
      lstAddrLines.add(addr.getCustNm2());
      sbAddrLu.append("E");
    }
    // Att Person
    if (StringUtils.isNotBlank(addr.getCustNm4())) {
      lstAddrLines.add(addr.getCustNm4());
      sbAddrLu.append("B");
    }
    // Street
    if (StringUtils.isNotBlank(addr.getAddrTxt())) {
      lstAddrLines.add(addr.getAddrTxt());
      sbAddrLu.append("F");
    }
    // PO Box
    if (StringUtils.isNotBlank(addrType)) {
      if ("ZS01".equals(addrType) || "ZP01".equals(addrType)) {
        if (StringUtils.isNotBlank(addr.getPoBox()) && !(addr.getPoBox()).contains("מ.ד")) {
          lstAddrLines.add("מ.ד " + addr.getPoBox());
        } else {
          lstAddrLines.add(addr.getPoBox());
        }
        sbAddrLu.append("H");
      } else if ("CTYA".equals(addrType) || "CTYB".equals(addrType)) {
        if (StringUtils.isNotBlank(addr.getPoBox())) {
          lstAddrLines.add("PO BOX " + addr.getPoBox());
          sbAddrLu.append("H");
        } else if (StringUtils.isNotBlank(addr.getCustNm1()) && StringUtils.isBlank(addr.getPoBox())) {
          lstAddrLines.add("");
          sbAddrLu.append("H");
        }
      }
    }
    // Address Cont
    if (StringUtils.isNotBlank(addr.getAddrTxt2())) {
      lstAddrLines.add(addr.getAddrTxt2());
      sbAddrLu.append("G");
    }
    // Postal Code
    String postalCdAndCity = null;
    if (StringUtils.isNotBlank(addr.getPostCd())) {
      postalCdAndCity = addr.getPostCd();
    }
    // City
    if (StringUtils.isNotBlank(addr.getCity1())) {
      if (StringUtils.isNotBlank(postalCdAndCity)) {
        postalCdAndCity = postalCdAndCity + " " + addr.getCity1();
      } else {
        postalCdAndCity = addr.getCity1();
      }
    }
    if (StringUtils.isNotBlank(postalCdAndCity)) {
      lstAddrLines.add(postalCdAndCity);
      sbAddrLu.append("I");
    }

    // Land Country
    if (StringUtils.isNotBlank(addr.getLandCntry()) && !"IL".equals(addr.getLandCntry())) {
      lstAddrLines.add(addr.getLandCntry());
      sbAddrLu.append("J");
    }

    if (lstAddrLines.size() > 0) {
      for (int i = 0; i < lstAddrLines.size(); i++) {
        switch (i) {
        case 0:
          legacyAddr.setAddrLine1(lstAddrLines.get(i));
          break;
        case 1:
          legacyAddr.setAddrLine2(lstAddrLines.get(i));
          break;
        case 2:
          legacyAddr.setAddrLine3(lstAddrLines.get(i));
          break;
        case 3:
          legacyAddr.setAddrLine4(lstAddrLines.get(i));
          break;
        case 4:
          legacyAddr.setAddrLine5(lstAddrLines.get(i));
          break;
        case 5:
          legacyAddr.setAddrLine6(lstAddrLines.get(i));
          break;
        }
      }
      legacyAddr.setAddrLineU(sbAddrLu.toString());
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

  @Override
  public boolean sequenceNoUpdateLogic(EntityManager entityManager, CMRRequestContainer cmrObjects, Addr currAddr, boolean flag) {
    return shouldUpdateSequence(entityManager, cmrObjects, currAddr);
  }

  private boolean shouldUpdateSequence(EntityManager entityManager, CMRRequestContainer cmrObjects, Addr currAddr) {
    if (cmrObjects != null && cmrObjects.getAdmin() != null) {
      boolean update = "U".equals(cmrObjects.getAdmin().getReqType());
      if (update) {
        boolean isNew = isSequenceNewlyAdded(entityManager, currAddr);
        if (isNew) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean isSequenceNewlyAdded(EntityManager entityManager, Addr currAddr) {
    AddrRdc addrRdc = LegacyCommonUtil.getAddrRdcRecord(entityManager, currAddr);
    if (addrRdc != null) {
      return false;
    }
    return true;
  }

  @Override
  public boolean skipCreditCodeUpdateForCountry() {
    return true;
  }

}
