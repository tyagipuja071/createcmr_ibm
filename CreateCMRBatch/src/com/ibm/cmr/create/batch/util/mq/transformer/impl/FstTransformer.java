/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.model.BatchEmailModel;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.geo.impl.MCOHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * @author Jeffrey Zamora
 * 
 */
public class FstTransformer extends MCOTransformer {

  private static final Logger LOG = Logger.getLogger(FstTransformer.class);

  protected static final String[] ADDRESS_ORDER = { "ZI01", "ZP01", "ZS01", "ZD01", "ZS02" };
  protected static final String[] ADDRESS_ORDER_LD = { "ZS02", "ZP01", "ZS01", "ZD01", "ZI01" };
  private static final String DEFAULT_CLEAR_NUM = "0";
  private static final List<String> FST_COUNTRY_LIST = Arrays.asList(SystemLocation.MAURITIUS, SystemLocation.MALI, SystemLocation.EQUATORIAL_GUINEA,
      SystemLocation.SENEGAL, SystemLocation.IVORY_COAST, SystemLocation.GABON, SystemLocation.DEMOCRATIC_CONGO, SystemLocation.CONGO_BRAZZAVILLE,
      SystemLocation.DJIBOUTI, SystemLocation.GUINEA_CONAKRY, SystemLocation.CAMEROON, SystemLocation.MADAGASCAR, SystemLocation.MAURITANIA,
      SystemLocation.TOGO, SystemLocation.GAMBIA, SystemLocation.CENTRAL_AFRICAN_REPUBLIC, SystemLocation.BENIN, SystemLocation.BURKINA_FASO,
      SystemLocation.SEYCHELLES, SystemLocation.GUINEA_BISSAU, SystemLocation.NIGER, SystemLocation.CHAD);

  private static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  private static final String CMR_REQUEST_STATUS_CPR = "CPR";
  private static final String CMR_REQUEST_STATUS_PCR = "PCR";

  private boolean isLDEnabled = true;

  public FstTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData FST Africa transformer...");
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    List<String> gmllcScenarios = Arrays.asList("NALLC", "LSLLC", "SZLLC", "NABLC", "LSBLC", "SZBLC", "LLC", "LLCBP");

    String custType = data.getCustSubGrp();
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      if (MQMsgConstants.CUSTSUBGRP_GOVRN.equals(custType) || "XGOV".equals(custType)) {
        legacyCust.setCustType("G");
      } else if (MQMsgConstants.CUSTSUBGRP_IBMEM.equals(custType)) {
        legacyCust.setCustType("98");
      }
      legacyCust.setSalesRepNo("DUMMY1");
      legacyCust.setSalesGroupRep("DUMMY1");
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      legacyCust.setCustType(data.getCrosSubTyp());

      if (StringUtils.isNotBlank(data.getCommercialFinanced())) {
        legacyCust.setModeOfPayment(data.getCommercialFinanced());
      } else {
        String cod = data.getCreditCd();
        if ("Y".equals(cod)) {
          legacyCust.setModeOfPayment("5");
        } else if ("N".equals(cod)) {
          legacyCust.setModeOfPayment("");
        }
      }
      legacyCust.setCreditCd("");

      legacyCust.setSalesRepNo(data.getRepTeamMemberNo());
      legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());

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
    }

    legacyCust.setAccAdminBo("");
    legacyCust.setBankAcctNo("");
    legacyCust.setBankBranchNo("");
    legacyCust.setBankNo("");
    legacyCust.setCeDivision("3");
    legacyCust.setCurrencyCd("");
    legacyCust.setCeBo("");
    legacyCust.setLangCd("1");
    legacyCust.setLocNo("");
    legacyCust.setMailingCond("");
    legacyCust.setAuthRemarketerInd("0");

    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBP".equals(custType)) {
      legacyCust.setMrcCd("5");
      legacyCust.setAuthRemarketerInd("1");
    } else {
      legacyCust.setMrcCd("3");
    }
    if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
      legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
    } else {
      legacyCust.setSalesGroupRep("");
    }

    if (!StringUtils.isBlank(data.getVat())) {
      legacyCust.setVat(data.getVat());
    } else {
      legacyCust.setVat("");
    }

    String formatSBO = data.getSalesBusOffCd() + "000";
    legacyCust.setIbo(formatSBO);
    legacyCust.setSbo(formatSBO);

    for (Addr addr : cmrObjects.getAddresses()) {
      if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
        legacyCust.setTelNoOrVat(addr.getCustPhone());
      }
    }

    if (StringUtils.isNotBlank(data.getIbmDeptCostCenter())) {
      String deptCd = data.getIbmDeptCostCenter().substring(2);
      legacyCust.setDeptCd(deptCd);
    }

    // if (!StringUtils.isEmpty(data.getCustSubGrp()) &&
    // gmllcScenarios.contains(data.getCustSubGrp())) {
    // legacyCust.setIsuCd("32");
    // legacyCust.setSbo("0080");
    // }
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
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();

    if (SystemLocation.MADAGASCAR.equals(cmrIssuingCntry)) {
      if (StringUtils.isNotBlank(data.getBusnType())) {
        legacyCustExt.setiTaxCode(data.getBusnType());
      }
    }
  }

  @Override
  public void transformLegacyCustomerExtDataMassUpdate(EntityManager entityManager, CmrtCustExt custExt, CMRRequestContainer cmrObjects,
      MassUpdtData muData, String cmr) throws Exception {
    if (!StringUtils.isBlank(muData.getNewEntpName1())) {
      if (SystemLocation.MADAGASCAR.equals(cmrIssuingCntry)) {
        custExt.setiTaxCode(muData.getNewEntpName1());
      }
    }
  }

  @Override
  public boolean hasCmrtCustExt() {
    if (SystemLocation.MADAGASCAR.equals(cmrIssuingCntry)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    LOG.debug("transformLegacyAddressData FST Africa transformer...");
    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    }
    formatAddressLinesLD(dummyHandler, legacyAddr);
  }

  private void formatAddressLinesLD(MQMessageHandler handler, CmrtAddr legacyAddr) {
    Addr addrData = handler.addrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    LOG.debug("Legacy Direct formatAddressLinesLD - Handling Address for " + (update ? "update" : "create") + " request.");
    boolean crossBorder = isCrossBorder(addrData);

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";
    String addtlName = addrData.getCustNm4();
    String street = addrData.getAddrTxt();
    String streetCont = addrData.getAddrTxt2();
    String poBox = addrData.getPoBox();
    String poBoxWithPrefix = "PO BOX " + addrData.getPoBox();
    String postalCd = addrData.getPostCd();
    String city = addrData.getCity1();
    String streetContPoBox = streetCont + ", " + poBoxWithPrefix;

    line1 = addrData.getCustNm1();
    line2 = addrData.getCustNm2();

    if (StringUtils.isNotBlank(addtlName)) {
      line3 = addtlName;
    } else if (StringUtils.isBlank(addtlName) && StringUtils.isNotBlank(street)) {
      line3 = street;
    }

    if (StringUtils.isNotBlank(addtlName) && StringUtils.isNotBlank(street)) {
      line4 = street;
    }

    if (StringUtils.isBlank(line4)) {
      if (StringUtils.isNotBlank(streetCont) && StringUtils.isNotBlank(poBox)) {
        line4 = streetContPoBox;
      } else if (StringUtils.isNotBlank(poBox)) {
        line4 = poBoxWithPrefix;
      } else if (StringUtils.isNotBlank(streetCont)) {
        line4 = streetCont;
      }
    }

    if (crossBorder) {
      String postalCdCity = postalCd;
      if (StringUtils.isNotBlank(city) && StringUtils.isNotBlank(postalCd)) {
        postalCdCity += " " + city;
      } else if (StringUtils.isNotBlank(city) && StringUtils.isBlank(postalCd)) {
        postalCdCity = city;
      }
      line5 = postalCdCity;
      line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    } else {

      if (StringUtils.isNotBlank(addtlName) && StringUtils.isNotBlank(street)) {
        if (StringUtils.isNotBlank(streetCont) && StringUtils.isNotBlank(poBox)) {
          line5 = streetContPoBox;
        } else if (StringUtils.isNotBlank(streetCont)) {
          line5 = streetCont;
        } else if (StringUtils.isNotBlank(poBox)) {
          line5 = poBoxWithPrefix;
        }
      }

      String postalCdCity = postalCd;
      if (StringUtils.isNotBlank(city) && StringUtils.isNotBlank(postalCd)) {
        postalCdCity += " " + city;
      } else if (StringUtils.isNotBlank(city) && StringUtils.isBlank(postalCd)) {
        postalCdCity = city;
      }
      line6 = postalCdCity;
    }

    legacyAddr.setAddrLine1(line1);
    legacyAddr.setAddrLine2(line2);
    legacyAddr.setAddrLine3(line3);
    legacyAddr.setAddrLine4(line4);
    legacyAddr.setAddrLine5(line5);
    legacyAddr.setAddrLine6(line6);

  }

  @Override
  public String getAddressUse(Addr addr) {
    if (isLDEnabled) {
      LOG.info("getAddressUse -  LD - FST Africa ");
      return getAddressUseLD(addr);
    } else {
      LOG.info("getAddressUse -  MQ - FST Africa ");
      return getAddressUseMQ(addr);
    }
  }

  private String getAddressUseLD(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS02:
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

  private String getAddressUseMQ(Addr addr) {
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
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  @Override
  public String[] getAddressOrder() {
    if (isLDEnabled) {
      LOG.info("getAddressOrder -  LD - FST Africa ");
      return ADDRESS_ORDER_LD;
    } else {
      LOG.info("getAddressOrder -  MQ - FST Africa ");
      return ADDRESS_ORDER;
    }
  }

  @Override
  public String getAddressKey(String addrType) {
    if (isLDEnabled) {
      LOG.info("getAddressKey -  LD - FST Africa ");
      return getAddressKeyLD(addrType);
    } else {
      LOG.info("getAddressKey -  MQ - FST Africa ");
      return getAddressKeyMQ(addrType);
    }
  }

  private String getAddressKeyMQ(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "Mail";
    case "ZD01":
      return "Ship";
    case "ZS01":
      return "Install";
    case "ZS02":
      return "Soft";
    default:
      return "";
    }
  }

  private String getAddressKeyLD(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "EPL";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Installing";
    case "ZS02":
      return "Mailing";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    if (isLDEnabled) {
      LOG.info("getTargetAddressType -  LD -  FST Africa");
      return getTargetAddressTypeLD(addrType);
    } else {
      LOG.info("getTargetAddressType -  MQ -  FST Africa ");
      return getTargetAddressTypeMQ(addrType);
    }
  }

  private String getTargetAddressTypeMQ(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "Mailing";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Installing";
    case "ZS02":
      return "EPL";
    default:
      return "";
    }
  }

  private String getTargetAddressTypeLD(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "EPL";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Installing";
    case "ZS02":
      return "Mailing";
    default:
      return "";
    }
  }

  @Override
  public String getGmllcDupCreation(Data data) {
    List<String> validScenarios = Arrays.asList("NALLC", "LSLLC", "SZLLC", "NABLC", "LSBLC", "SZBLC", "LLC", "LLCBP");
    if (data != null && StringUtils.isNotEmpty(data.getCustSubGrp()) && validScenarios.contains(data.getCustSubGrp())) {
      return "764";
    }
    return "NA";
  }

  @Override
  public void transformLegacyDataForDupCreation(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects,
      CMRRequestContainer cmrObjects) {
    CmrtCust legacyCust = legacyObjects.getCustomer();
    Data data = cmrObjects.getData();
    List<String> bpGMLLCScenarios = Arrays.asList("NABLC", "LSBLC", "SZBLC", "LLCBP");
    boolean isGMLLC = false;
    if (data != null) {
      if (!"NA".equals(getGmllcDupCreation(data))) {
        isGMLLC = true;
      }
      if (legacyCust != null && isGMLLC) {
        if (bpGMLLCScenarios.contains(data.getCustSubGrp())) {
          legacyCust.setIsuCd("8B7");
          legacyCust.setSalesRepNo("DUMMY1");
          legacyCust.setSbo("0010000");
          legacyCust.setIbo("0010000");
          legacyCust.setInacCd("");
        } else {
          legacyCust.setIsuCd("32S");
          legacyCust.setSalesRepNo("DUMMY1");
          legacyCust.setSbo("0080000");
          legacyCust.setIbo("0080000");
        }
      }
    }
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For FST Africa...");
    if (custSubGrp != null && "INTER".equals(custSubGrp) || custSubGrp != null && "XINTE".equals(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }

  @Override
  public String getMailSendingFlag(Data data, Admin admin, EntityManager entityManager) {
    DataRdc oldData = LegacyCommonUtil.getOldData(entityManager, String.valueOf(data.getId().getReqId()));
    String oldCOD = "";
    String oldCOF = "";
    if (oldData != null) {
      oldCOD = oldData.getCreditCd();
      oldCOF = oldData.getCommercialFinanced();
    }

    String currentCOF = data.getCommercialFinanced();
    String currentCOD = data.getCreditCd();
    boolean currCOFHasValidValue = "R".equals(currentCOF) || "S".equals(currentCOF) || "T".equals(currentCOF);

    if (StringUtils.isBlank(oldCOF) && currCOFHasValidValue) {
      return "COF";
    } else if ((StringUtils.isBlank(oldCOD) || "N".equals(oldCOD)) && "Y".equals(currentCOD)) {
      return "COD";
    } else {
      return "NA";
    }
  }

  @Override
  public String getEmailTemplateName(String type) {
    if ("COD".equals(type)) {
      return "cmr-email_cod.html";
    } else {
      return "cmr-email_cof.html";
    }
  }

  @Override
  public BatchEmailModel getMailFormatParams(EntityManager entityManager, CMRRequestContainer cmrObjects, String type) {
    BatchEmailModel params = new BatchEmailModel();
    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();
    String directUrlLink = SystemConfiguration.getValue("APPLICATION_URL") + "/login?r=" + admin.getId().getReqId();
    directUrlLink = "Click <a href=\"" + directUrlLink + "\">Here</a>";
    params.setRequesterName(admin.getRequesterNm());
    params.setRequesterId(admin.getRequesterId());
    params.setRequestId(Long.toString(admin.getId().getReqId()));
    String custNm = admin.getMainCustNm1() + (StringUtils.isNotBlank(admin.getMainCustNm2()) ? ("" + admin.getMainCustNm2()) : "");
    params.setCustNm(custNm.trim());
    params.setIssuingCountry(data.getCmrIssuingCntry());
    params.setCmrNumber(data.getCmrNo());
    params.setSubregion("N/A");
    params.setDirectUrlLink(directUrlLink);
    String receipent = "";
    String mailSubject = "";
    if ("COD".equals(type)) {
      mailSubject = SystemParameters.getString("COD_MAIL_SUBJECT");
      receipent = SystemParameters.getString("COD_MAIL_RECIEVER");
    } else if ("COF".equals(type)) {
      mailSubject = SystemParameters.getString("COF_MAIL_SUBJECT");
      receipent = SystemParameters.getString("COF_MAIL_RECIEVER");
    }
    params.setReceipent(receipent);
    params.setMailSubject(mailSubject);
    params.setStringToReplace("xxxxxx");
    params.setValToBeReplaceBy(data.getCmrNo());
    return params;
  }

  @Override
  public String getReqStatusForSendingMail(String mailFlag) {
    if ("COD".equals(mailFlag)) {
      return "PCO";
    } else if ("COF".equals(mailFlag)) {
      return "COM";
    }
    return null;
  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust legacyCust, CMRRequestContainer cmrObjects,
      MassUpdtData muData) {
    LOG.debug("FST mass update >> Mapping default Data values..");
    LegacyCommonUtil.setlegacyCustDataMassUpdtFields(entityManager, legacyCust, muData);

    if (!StringUtils.isBlank(muData.getRestrictTo())) {
      if (DEFAULT_CLEAR_NUM.equals(muData.getRestrictTo())) {
        legacyCust.setTelNoOrVat("");
      } else {
        legacyCust.setTelNoOrVat(muData.getRestrictTo());
      }
    }

    if (!StringUtils.isBlank(muData.getAffiliate())) {
      String deptCd = muData.getAffiliate().substring(2);
      legacyCust.setDeptCd(deptCd);
    }

    if (!StringUtils.isBlank(muData.getCustNm1())) {
      String formatSBO = muData.getCustNm1() + "000";
      legacyCust.setIbo(formatSBO);
      legacyCust.setSbo(formatSBO);
    }

    if (StringUtils.isNotBlank(muData.getOutCityLimit())) {
      legacyCust.setModeOfPayment(muData.getOutCityLimit());
    }

    if (StringUtils.isNotBlank(muData.getEntpUpdtTyp())) {
      String cod = muData.getEntpUpdtTyp();
      if ("Y".equals(cod)) {
        legacyCust.setModeOfPayment("5");
      } else if ("N".equals(cod)) {
        legacyCust.setModeOfPayment("");
      }
    }

  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr muAddr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {
    LOG.debug("FST mass update >> Mapping address lines..");
    legacyAddr.setForUpdate(true);
    LegacyCommonUtil.transformBasicLegacyAddressMassUpdate(entityManager, legacyAddr, muAddr, cntry, cust, data);

    if (!StringUtils.isBlank(muAddr.getPostCd())) {
      legacyAddr.setZipCode(muAddr.getPostCd());
    }

    if (!StringUtils.isBlank(muAddr.getCustPhone())) {
      if (muAddr.getId().getAddrType().equals("ZD01")) {
        if (DEFAULT_CLEAR_NUM.equals(muAddr.getCustPhone().trim())) {
          legacyAddr.setAddrPhone("");
        } else {
          legacyAddr.setAddrPhone(muAddr.getCustPhone());
        }
      }
    }

    formatMassUpdateAddressLines(entityManager, legacyAddr, muAddr, false);
    legacyObjects.addAddress(legacyAddr);
  }

  @Override
  public void formatMassUpdateAddressLines(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr massUpdtAddr, boolean isFAddr) {
    LOG.debug("Start FST formatMassUpdateAddressLines...");
    boolean crossBorder = isCrossBorderForMass(massUpdtAddr, legacyAddr);
    LOG.debug("isCrossBorderForMass : " + crossBorder);

    String line1 = legacyAddr.getAddrLine1();
    String line2 = legacyAddr.getAddrLine2();
    String line3 = legacyAddr.getAddrLine3();
    String line4 = legacyAddr.getAddrLine4();
    String line5 = legacyAddr.getAddrLine5();
    String line6 = legacyAddr.getAddrLine6();

    // Additional name/additional info
    if (!StringUtils.isBlank(massUpdtAddr.getCounty())) {
      line3 = massUpdtAddr.getCounty();
    }

    if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt())) {
      line4 = massUpdtAddr.getAddrTxt();
    }

    if (crossBorder) {
      LOG.debug("performing crossBorder setup");

      // StreetCont, POBox || Additional name/additional info
      if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2()) && !StringUtils.isBlank(massUpdtAddr.getPoBox())) {
        line3 = massUpdtAddr.getAddrTxt2() + ", " + "PO BOX " + massUpdtAddr.getPoBox();
      } else if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
        line3 = massUpdtAddr.getAddrTxt2();
      } else if (!StringUtils.isBlank(massUpdtAddr.getPoBox())) {
        line3 = "PO BOX " + massUpdtAddr.getPoBox();
      }

      if (StringUtils.isNotBlank(massUpdtAddr.getCity1())) {
        String cityPostalCode = massUpdtAddr.getCity1();
        if (StringUtils.isNotBlank(cityPostalCode) && StringUtils.isNotBlank(massUpdtAddr.getPostCd())) {
          cityPostalCode += ", " + massUpdtAddr.getPostCd();
        }
        line5 = cityPostalCode;
      } else {
        if (StringUtils.isNotBlank(line6))
          line5 = line6;
      }
      if (!StringUtils.isBlank(massUpdtAddr.getLandCntry())) {
        line6 = LandedCountryMap.getCountryName(massUpdtAddr.getLandCntry()).toUpperCase();
      }
    } else {
      LOG.debug("performing local setup");
      if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2()) && !StringUtils.isBlank(massUpdtAddr.getPoBox())) {
        line5 = massUpdtAddr.getAddrTxt2() + ", " + "PO BOX " + massUpdtAddr.getPoBox();
      } else if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
        line5 = massUpdtAddr.getAddrTxt2();
      } else if (!StringUtils.isBlank(massUpdtAddr.getPoBox())) {
        line5 = "PO BOX " + massUpdtAddr.getPoBox();
      }

      if (StringUtils.isNotBlank(massUpdtAddr.getCity1())) {
        String cityPostalCode = massUpdtAddr.getCity1();
        if (StringUtils.isNotBlank(cityPostalCode) && StringUtils.isNotBlank(massUpdtAddr.getPostCd())) {
          cityPostalCode += ", " + massUpdtAddr.getPostCd();
        }
        line6 = cityPostalCode;
      } else {
        line6 = legacyAddr.getAddrLine6();
      }
    }

    legacyAddr.setAddrLine1(line1);
    legacyAddr.setAddrLine2(line2);
    legacyAddr.setAddrLine3(line3);
    legacyAddr.setAddrLine4(line4);
    legacyAddr.setAddrLine5(line5);
    legacyAddr.setAddrLine6(line6);

    LOG.debug("End FST formatMassUpdateAddressLines...");

  }

  @Override
  public boolean isCrossBorderForMass(MassUpdtAddr addr, CmrtAddr legacyAddr) {
    boolean countryFound = false;
    String cd = MCOHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    List<String> listCountryNames = new ArrayList<String>();
    for (String countryCode : FST_COUNTRY_LIST) {
      String cd1 = MCOHandler.LANDED_CNTRY_MAP.get(countryCode);
      String countryName = LandedCountryMap.getCountryName(cd1);
      listCountryNames.add(countryName);
    }
    for (String names : listCountryNames) {
      if (names.toUpperCase().equals(legacyAddr.getAddrLine6().toUpperCase())) {
        countryFound = true;
        break;
      }
    }
    String countryName = LandedCountryMap.getCountryName(cd);
    boolean isCrossBorder = false;
    if (!StringUtils.isEmpty(addr.getLandCntry()) && !cd.equals(addr.getLandCntry())) {
      isCrossBorder = true;
    } else if (!StringUtils.isEmpty(legacyAddr.getAddrLine6()) && countryFound && !legacyAddr.getAddrLine6().equals(countryName)) {
      isCrossBorder = true;
    }
    return isCrossBorder;
  }

  @Override
  public boolean isUpdateNeededOnAllAddressType(EntityManager entityManager, CMRRequestContainer cmrObjects) {
    List<Addr> addresses = cmrObjects.getAddresses();
    boolean isPhoneUpdated = false;
    for (Addr addr : addresses) {
      if ("ZS01".equals(addr.getId().getAddrType())) {
        AddrRdc addrRdc = LegacyCommonUtil.getAddrRdcRecord(entityManager, addr);
        String currPhone = addr.getCustPhone() != null ? addr.getCustPhone() : "";
        String oldPhone = addrRdc.getCustPhone() != null ? addrRdc.getCustPhone() : "";
        if (addrRdc == null || (addrRdc != null && !currPhone.equals(oldPhone))) {
          isPhoneUpdated = true;
          return true;
        }
      }
      if (!"ZS01".equals(addr.getId().getAddrType()) && isPhoneUpdated) {
        addr.setChangedIndc("Y");
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
  public String getFixedAddrSeqForProspectCreation() {
    return "00003";
  }
}
