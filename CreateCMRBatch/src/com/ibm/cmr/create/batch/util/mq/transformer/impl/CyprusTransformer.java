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
 * {@link MessageTransformer} implementation for Cyprus.
 * 
 * @author Dhananjay Yadav
 * 
 */
public class CyprusTransformer extends EMEATransformer {

  private static final Logger LOG = Logger.getLogger(CyprusTransformer.class);
  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode" };
  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02" };
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";
  private static final String DEFAULT_CLEAR_CHAR = "@";
  private static final String DEFAULT_CLEAR_NUM = "0";

  public CyprusTransformer() {
    super(SystemLocation.CYPRUS);
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.CYPRUS;
  }

  private void formatDataLinesCy(MQMessageHandler handler) {
    boolean update = "U".equals(handler.adminData.getReqType());
    Data cmrData = handler.cmrData;
    Addr addrData = handler.addrData;
    boolean crossBorder = isCrossBorder(addrData);

    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    handleEMEADefaults(handler, messageHash, cmrData, addrData, crossBorder);

    messageHash.put("EmbargoCode", !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "");
    messageHash.put("SourceCode", "EFW");
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
  public void formatAddressLines(MQMessageHandler mqHandler) {
    Map<String, String> messageHash = mqHandler.messageHash;
    Addr addrData = mqHandler.addrData;
    String addrType = addrData.getId().getAddrType();

    String addrKey = getAddressKey(addrData.getId().getAddrType());

    mqHandler.messageHash.put("SourceCode", "EFW");
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

    // line1 is customer name
    line1 = addrData.getCustNm1();
    // line2 is customer name con't
    line2 = addrData.getCustNm2();

    if (!StringUtils.isBlank(addrData.getCustNm4())) {
      line3 = "ATT " + addrData.getCustNm4();
    } else if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
      line3 = addrData.getAddrTxt2();
    } else if (!StringUtils.isBlank(addrData.getPoBox())
        && (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrType) || CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrType))) {
      line3 = "PO BOX " + addrData.getPoBox();
    }

    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 = addrData.getAddrTxt();
    } else if (!StringUtils.isBlank(addrData.getPoBox())
        && (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrType) || CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrType))) {
      line4 = "PO BOX " + addrData.getPoBox();
    }

    line5 = (!StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "") + " "
        + (!StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "");
    line5 = line5.trim();

    // country
    line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());

    int lineNo = 1;
    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    // fixed mapping value, not move up if blank
    for (String line : lines) {
      messageHash.put(addrKey + "Address" + lineNo, line);
      lineNo++;
    }
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    LOG.debug("transformLegacyAddressData Cyprus transformer...");
    formatAddressLines(dummyHandler);
    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    }

    if (!StringUtils.isBlank(currAddr.getPoBox())) {
      legacyAddr.setPoBox(currAddr.getPoBox());
    } else {
      legacyAddr.setPoBox("");
    }
  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    formatDataLinesCy(handler);
    handler.messageHash.put("SourceCode", "EFW");
  }

  protected boolean isCrossBorder(Addr addr) {
    return !"CY".equals(addr.getLandCntry());
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
      return "EPL";
    default:
      return "";
    }
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "1";
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
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
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
    default:
      return "";
    }
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {

    LOG.debug("transformLegacyCustomerData CYPRUS transformer...");

    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    formatDataLines(dummyHandler);
    String landedCntry = "";

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      for (Addr addr : cmrObjects.getAddresses()) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          legacyCust.setTelNoOrVat(addr.getCustPhone());
          landedCntry = addr.getLandCntry();
          break;
        }
      }

      if (data.getCustSubGrp().equals("BUSPR") || data.getCustSubGrp().equals("CRBUS")) {
        legacyCust.setAuthRemarketerInd("1");
      }
      // MRC_CODE_CMKDA
      String custType = data.getCustSubGrp();
      if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "CRBUS".equals(custType)) {
        legacyCust.setMrcCd("5");
      } else {
        legacyCust.setMrcCd("3");
      }

      legacyCust.setLangCd("");
      legacyCust.setTaxCd("");

      legacyCust.setSalesGroupRep(!StringUtils.isEmpty(data.getRepTeamMemberNo()) ? data.getRepTeamMemberNo() : ""); // REMXD

      String formatSBO = data.getSalesBusOffCd() + "0000";
      legacyCust.setIbo(formatSBO);
      legacyCust.setSbo(formatSBO);

      String custSubType = data.getCustSubGrp();
      if (MQMsgConstants.CUSTSUBGRP_GOVRN.equals(custSubType)) {
        legacyCust.setCustType("G");
      }

      legacyCust.setDcRepeatAgreement("0"); // CAGXB
      legacyCust.setLeasingInd("0"); // CIEDC
      legacyCust.setAuthRemarketerInd("0"); // CIEXJ
      if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "CRBUS".equals(custType)) {
        legacyCust.setMrcCd("5");
        legacyCust.setAuthRemarketerInd("1");
      }
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      for (Addr addr : cmrObjects.getAddresses()) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          if (StringUtils.isEmpty(addr.getCustPhone())) {
            legacyCust.setTelNoOrVat("");
          } else {
            legacyCust.setTelNoOrVat(addr.getCustPhone());
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

      if (!StringUtils.isBlank(data.getSalesTeamCd())) {
        legacyCust.setSalesRepNo(data.getSalesTeamCd());
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

      if (!StringUtils.isBlank(data.getModeOfPayment()) && data.getModeOfPayment().equals("5")) {
        legacyCust.setModeOfPayment(data.getModeOfPayment());
      } else {
        legacyCust.setModeOfPayment("");
      }

      long reqId = cmrObjects.getAdmin().getId().getReqId();
      try {
        if (LegacyDirectUtil.checkFieldsUpdated(entityManager, cmrIssuingCntry, admin, reqId)) {
          legacyCust.setAbbrevNm(data.getAbbrevNm());
          legacyCust.setAbbrevLocn(data.getAbbrevLocn());
          legacyCust.setIsicCd(data.getIsicCd());
          legacyCust.setImsCd(data.getSubIndustryCd());
          legacyCust.setInacCd(data.getInacCd());
          legacyCust.setIsicCd(isuClientTier);
          legacyCust.setSbo(data.getSalesBusOffCd());
          legacyCust.setSalesRepNo(data.getSalesTeamCd());
          legacyCust.setCollectionCd(data.getCollectionCd());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // common data for C/U
    legacyCust.setCeDivision(""); // CCEDA
    legacyCust.setAccAdminBo(""); // RACBO

    // SBO,IBO,REMXA,REMXD
    if (!StringUtils.isBlank(data.getSalesBusOffCd())) {
      legacyCust.setIbo(data.getSalesBusOffCd() + "0000");
      legacyCust.setSbo(data.getSalesBusOffCd() + "0000");
    } else {
      legacyCust.setIbo("");
      legacyCust.setSbo("");
    }

    // formatted data
    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("AbbreviatedLocation"))) {
      legacyCust.setAbbrevLocn(dummyHandler.messageHash.get("AbbreviatedLocation"));
    }

    // Vat
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

    legacyCust.setCeBo("");
    legacyCust.setCustType(dummyHandler.messageHash.get("CustomerType"));

    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("EconomicCode"))) {
      legacyCust.setEconomicCd(dummyHandler.messageHash.get("EconomicCode"));
    }

    // other fields to be transformed is pending
    legacyCust.setBankBranchNo(data.getIbmDeptCostCenter() != null ? data.getIbmDeptCostCenter() : "");
    legacyCust.setEnterpriseNo(!StringUtils.isEmpty(data.getEnterprise()) ? data.getEnterprise() : "");
    // CREATCMR-4293
    if (!StringUtils.isEmpty(data.getIsuCd())) {
      if (StringUtils.isEmpty(data.getClientTier())) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      }
    }

  }

  @Override
  public boolean hasCmrtCustExt() {
    return true;
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
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For CY...");
    if (custSubGrp != null && "INTER".equals(custSubGrp) || custSubGrp != null && "CRINT".equals(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust cust, CMRRequestContainer cmrObjects, MassUpdtData muData) {
    LOG.debug("CY >> Mapping default Data values..");
    LegacyCommonUtil.setlegacyCustDataMassUpdtFields(entityManager, cust, muData);

    if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
      cust.setImsCd(muData.getSubIndustryCd());
    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      if ("@@@@@@".equals(muData.getEnterprise().trim())) {
        cust.setEnterpriseNo("");
      } else {
        cust.setEnterpriseNo(muData.getEnterprise());
      }
    }

    List<MassUpdtAddr> muaList = cmrObjects.getMassUpdateAddresses();
    if (muaList != null && muaList.size() > 0) {
      for (MassUpdtAddr mua : muaList) {
        if ("ZS01".equals(mua.getId().getAddrType())) {
          if (!StringUtils.isBlank(mua.getCounty())) {
            if (DEFAULT_CLEAR_NUM.equals(mua.getCounty().trim())) {
              cust.setTelNoOrVat("");
            } else {
              cust.setTelNoOrVat(mua.getCounty());
            }
          }
          break;
        }
      }
    }
    String isuClientTier;
    if (!StringUtils.isEmpty(muData.getIsuCd()) && "5K".equals(muData.getIsuCd())) {
      cust.setIsuCd(muData.getIsuCd() + "7");
    } else {
      isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
          + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
      if (isuClientTier != null && isuClientTier.endsWith("@")) {
        cust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : cust.getIsuCd().substring(0, 2)) + "7");
      } else if (isuClientTier != null && isuClientTier.length() == 3) {
        cust.setIsuCd(isuClientTier);
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

    if (!StringUtils.isBlank(addr.getCounty())) {
      if (addr.getId().getAddrType().equals("ZD01")) {
        if (DEFAULT_CLEAR_NUM.equals(addr.getCounty().trim())) {
          legacyAddr.setAddrPhone("");
        } else {
          legacyAddr.setAddrPhone(addr.getCounty());
        }
      }

    }

    formatMassUpdateAddressLines(entityManager, legacyAddr, addr, false);
    legacyObjects.addAddress(legacyAddr);

  }

  @Override
  public void formatMassUpdateAddressLines(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr massUpdtAddr, boolean isFAddr) {
    LOG.debug("***START CY formatMassUpdateAddressLines >>>");

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
      if (DEFAULT_CLEAR_CHAR.equals(massUpdtAddr.getCustNm2())) {
        line2 = "";
      } else {
        line2 = massUpdtAddr.getCustNm2();
      }
    }

    // Att Person or Address Con't/Occupation
    if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
      line3 = massUpdtAddr.getAddrTxt2();
    } else if (!StringUtils.isBlank(massUpdtAddr.getCustNm4())) {
      if (!StringUtils.isEmpty(line3) && !line3.toUpperCase().startsWith("ATT ") && !line3.toUpperCase().startsWith("ATT:")) {
        line3 = "ATT " + line3;
      }
      line3 = "ATT " + massUpdtAddr.getCustNm4().trim();
    }

    // Street OR PO BOX
    if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt())) {
      line4 = massUpdtAddr.getAddrTxt();
    } else if (!StringUtils.isBlank(massUpdtAddr.getPoBox())) {
      line4 = "PO BOX " + massUpdtAddr.getPoBox();
      legacyAddr.setPoBox(massUpdtAddr.getPoBox());
    }

    if (!StringUtils.isEmpty(massUpdtAddr.getPostCd()) || !StringUtils.isEmpty(massUpdtAddr.getCity1())) {
      line5 = (legacyAddr.getZipCode() != null ? legacyAddr.getZipCode().trim() + " " : "")
          + (legacyAddr.getCity() != null ? legacyAddr.getCity().trim() : "");
    }

    if (!StringUtils.isBlank(massUpdtAddr.getLandCntry())) {
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
  public boolean enableTempReactOnUpdates() {
    return true;
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
