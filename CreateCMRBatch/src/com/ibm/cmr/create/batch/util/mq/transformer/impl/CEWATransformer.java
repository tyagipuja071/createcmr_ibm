/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

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
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
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
public class CEWATransformer extends MCOTransformer {

  private static final Logger LOG = Logger.getLogger(CEWATransformer.class);
  private static final String DEFAULT_CLEAR_NUM = "0";
  private static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  private static final String CMR_REQUEST_STATUS_CPR = "CPR";
  private static final String CMR_REQUEST_STATUS_PCR = "PCR";

  public CEWATransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData CEWA Africa transformer...");
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();

    String custType = data.getCustSubGrp();
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      if (MQMsgConstants.CUSTSUBGRP_GOVRN.equals(custType) || "XGOV".equals(custType)) {
        legacyCust.setCustType("G");
      } else if (MQMsgConstants.CUSTSUBGRP_IBMEM.equals(custType)) {
        legacyCust.setCustType("98");
      }

      legacyCust.setSalesRepNo("DUMMY1");
      legacyCust.setSalesGroupRep("DUMMY1");
      legacyCust.setDcRepeatAgreement("0");
      legacyCust.setLeasingInd("0");

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

      // legacyCust.setSalesRepNo(data.getRepTeamMemberNo());
      // legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());

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

    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || "XBP".equals(custType) || "LLCBP".equals(custType)) {
      legacyCust.setMrcCd("5");
      legacyCust.setAuthRemarketerInd("1");
    } else {
      legacyCust.setMrcCd("3");
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

    if (StringUtils.isNotBlank(data.getIbmDeptCostCenter()) && isInternalScenario(data, admin)) {
      String deptCd = data.getIbmDeptCostCenter().substring(2);
      legacyCust.setDeptCd(deptCd);
    } else {
      legacyCust.setDeptCd("");
    }
    // CREATCMR-4293
    List<String> custSubGrp_list = Arrays.asList("BUSPR", "LSBP", "LSXBP", "NABP", "NAXBP", "SZBP", "SZXBP", "XBP", "ZABP", "ZAXBP", "INTER", "LSINT",
        "LSXIN", "NAINT", "NAXIN", "SZINT", "SZXIN", "XINTE", "ZAINT", "ZAXIN");
    if (!SystemLocation.MALTA.equals(cmrIssuingCntry)) {
      if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
        if (!StringUtils.isEmpty(data.getIsuCd()) && ("21".equals(data.getIsuCd()) || "8B".equals(data.getIsuCd()))) {
          if (StringUtils.isEmpty(data.getClientTier())) {
            legacyCust.setIsuCd(data.getIsuCd() + "7");
          }
        }
      }
      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        if (!StringUtils.isEmpty(data.getCustSubGrp())) {
          if (custSubGrp_list.contains(data.getCustSubGrp())) {
            if (!StringUtils.isEmpty(data.getIsuCd()) && ("21".equals(data.getIsuCd()) || "8B".equals(data.getIsuCd()))) {
              if (StringUtils.isEmpty(data.getClientTier())) {
                legacyCust.setIsuCd(data.getIsuCd() + "7");
              }
            }
          }
        }
      }
    }

    if (!StringUtils.isEmpty(data.getIsuCd()) && "5K".equals(data.getIsuCd()) && !SystemLocation.MALTA.equals(data.getCmrIssuingCntry())) {
      legacyCust.setIsuCd(data.getIsuCd() + "7");
    }	
  }

  private boolean isInternalScenario(Data data, Admin admin) {
    String reqType = admin.getReqType();

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      String custSubGrp = data.getCustSubGrp();
      return ("INTER".equals(custSubGrp) || "XINTE".equals(custSubGrp));
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType) && data != null && data.getCmrNo() != null) {
      return data.getCmrNo().startsWith("99");
    }
    return false;
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

    if (SystemLocation.TANZANIA.equals(cmrIssuingCntry)) {
      if (StringUtils.isNotBlank(data.getTaxCd1())) {
        legacyCustExt.setiTaxCode(data.getTaxCd1());
      } else {
        legacyCustExt.setiTaxCode("");
      }
    }
  }

  @Override
  public boolean hasCmrtCustExt() {
    if (SystemLocation.TANZANIA.equals(cmrIssuingCntry)) {
      return true;
    }
    return false;
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    LOG.debug("transformLegacyAddressData CEWA Africa transformer...");
    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    } else {
      if (cmrObjects.getAdmin().getReqType().equals(CmrConstants.REQ_TYPE_CREATE)) {
        legacyAddr.setAddrPhone("");
      }
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
  public void formatDataLines(MQMessageHandler handler) {
    LOG.debug("transformLegacyAddressData CEWA Africa calling formatDataLines...");
    super.formatDataLines(handler);
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    LOG.debug("transformLegacyAddressData CEWA Africa calling formatAddressLines...");
    super.formatAddressLines(handler);
  }

  @Override
  public String getGmllcDupCreation(Data data) {
    if ("764".equals(data.getCmrIssuingCntry())) {
      return "NA";
    }

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
          legacyCust.setIsuCd("34Q");
          legacyCust.setSalesRepNo("DUMMY1");
          legacyCust.setSbo("0080000");
          legacyCust.setIbo("0080000");
        }
      }
    }
    CmrtCustExt legacyCustExt = legacyObjects.getCustomerExt();
    if (legacyCustExt != null) {
      legacyObjects.setCustomerExt(null);
    }
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For CEWA Africa...");
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

    String enableCOFMailFlag = SystemParameters.getString("ENABLE_COF_MAIL");
    if (StringUtils.isBlank(oldCOF) && currCOFHasValidValue) {
      if ("Y".equals(enableCOFMailFlag)) {
        LOG.debug("Mail flag return - COF ");
        return "COF";
      } else {
        LOG.debug("Mail flag return - NA ");
        return "NA";
      }
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

    params.setEnableAddlField1(true);
    params.setAddtlField1Value(data.getCreditCd() != null ? data.getCreditCd() : "");
    params.setEnableAddlField2(true);
    params.setAddtlField2Value(data.getCommercialFinanced() != null ? data.getCommercialFinanced() : "");

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
    LOG.debug("CEWA mass update >> Mapping default Data values..");
    LegacyCommonUtil.setlegacyCustDataMassUpdtFields(entityManager, legacyCust, muData);

    if (!StringUtils.isBlank(muData.getRestrictTo())) {
      if (DEFAULT_CLEAR_NUM.equals(muData.getRestrictTo())) {
        legacyCust.setTelNoOrVat("");
      } else {
        legacyCust.setTelNoOrVat(muData.getRestrictTo());
      }
    }

    if (!StringUtils.isBlank(muData.getAffiliate())) {
      if ("@@@@@@".equals(muData.getAffiliate())) {
        legacyCust.setDeptCd("");
      } else {
        String deptCd = muData.getAffiliate().substring(2);
        legacyCust.setDeptCd(deptCd);
      }
    }

    if (!StringUtils.isBlank(muData.getCustNm1())) {
      String formatSBO = muData.getCustNm1() + "000";
      legacyCust.setIbo(formatSBO);
      legacyCust.setSbo(formatSBO);
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      if ("@".equals(muData.getVat())) {
        legacyCust.setVat("");
      } else {
        legacyCust.setVat(muData.getVat());
      }
    }

    // COF flag
    if (StringUtils.isNotBlank(muData.getOutCityLimit())) {
      if ("@".equals(muData.getOutCityLimit())) {
        legacyCust.setModeOfPayment("");
      } else {
        legacyCust.setModeOfPayment(muData.getOutCityLimit());
      }
    }

    // COD flag
    if (StringUtils.isNotBlank(muData.getEntpUpdtTyp())) {
      String cod = muData.getEntpUpdtTyp();
      if ("@".equals(muData.getEntpUpdtTyp())) {
        legacyCust.setModeOfPayment("");
      } else {
        if ("Y".equals(cod)) {
          legacyCust.setModeOfPayment("5");
        } else if ("N".equals(cod)) {
          legacyCust.setModeOfPayment("");
        }
      }
    }

    if (StringUtils.isNotBlank(muData.getClientTier()) && "5K".equals(muData.getIsuCd())) {
      legacyCust.setIsuCd(muData.getIsuCd() + "7");
    } else {
      String isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
          + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
      if (isuClientTier != null && isuClientTier.endsWith("@")) {
        legacyCust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : legacyCust.getIsuCd().substring(0, 2)) + "7");
      } else if (isuClientTier != null) {
        legacyCust.setIsuCd(isuClientTier);
      }
    }

  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr muAddr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {
    LOG.debug("CEWA mass update >> Mapping address lines..");
    LegacyCommonUtil.transformBasicLegacyAddressMassUpdate(entityManager, legacyAddr, muAddr, cntry, cust, data);
    legacyAddr.setForUpdate(true);
    if (!LegacyCommonUtil.isCheckDummyUpdate(muAddr)) {
      LOG.debug("isCheckDummyUpdate false...");
      if (!StringUtils.isBlank(muAddr.getAddrTxt())) {
        legacyAddr.setStreet(muAddr.getAddrTxt());
      } else {
        legacyAddr.setStreet("");
      }
      if (!StringUtils.isBlank(muAddr.getAddrTxt2())) {
        legacyAddr.setStreetNo(muAddr.getAddrTxt2());
      } else {
        legacyAddr.setStreetNo("");
      }
      if (!StringUtils.isEmpty(muAddr.getPoBox())) {
        legacyAddr.setPoBox(muAddr.getPoBox());
      } else {
        legacyAddr.setPoBox("");
      }
      if (!StringUtils.isBlank(muAddr.getPostCd())) {
        legacyAddr.setZipCode(muAddr.getPostCd());
      } else {
        legacyAddr.setZipCode("");
      }
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
    LOG.debug("Start CEWA formatMassUpdateAddressLines...");
    if (LegacyCommonUtil.isCheckDummyUpdate(massUpdtAddr)) {
      LOG.debug("Dummy update , skip address update...");
      return;
    }
    boolean crossBorder = isCrossBorderForMass(massUpdtAddr, legacyAddr);
    LOG.debug("isCrossBorderForMass : " + crossBorder);

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";
    String postalCd = massUpdtAddr.getPostCd();
    String city = massUpdtAddr.getCity1();

    line1 = massUpdtAddr.getCustNm1();
    line2 = massUpdtAddr.getCustNm2();

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

      String postalCdCity = massUpdtAddr.getPostCd();
      if (StringUtils.isNotBlank(city) && StringUtils.isNotBlank(postalCd)) {
        postalCdCity += " " + city;
      } else if (StringUtils.isNotBlank(city) && StringUtils.isBlank(postalCd)) {
        postalCdCity = city;
      }
      line5 = postalCdCity;

      line6 = LandedCountryMap.getCountryName(massUpdtAddr.getLandCntry()).toUpperCase();

    } else {
      LOG.debug("performing local setup");
      if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2()) && !StringUtils.isBlank(massUpdtAddr.getPoBox())) {
        line5 = massUpdtAddr.getAddrTxt2() + ", " + "PO BOX " + massUpdtAddr.getPoBox();
      } else if (!StringUtils.isBlank(massUpdtAddr.getAddrTxt2())) {
        line5 = massUpdtAddr.getAddrTxt2();
      } else if (!StringUtils.isBlank(massUpdtAddr.getPoBox())) {
        line5 = "PO BOX " + massUpdtAddr.getPoBox();
      }

      String postalCdCity = massUpdtAddr.getPostCd();
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

    LOG.debug("End CEWA formatMassUpdateAddressLines...");

  }

  @Override
  public boolean isCrossBorderForMass(MassUpdtAddr addr, CmrtAddr legacyAddr) {
    boolean isCrossBorder = false;
    String cd = MCOHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    if (!StringUtils.isEmpty(addr.getLandCntry()) && !cd.equals(addr.getLandCntry())) {
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

    String cmrNo = null;
    String issuingCntry = null;

    if (cmrObjects.getData() != null) {
      cmrNo = cmrObjects.getData().getCmrNo();
      issuingCntry = cmrObjects.getData().getCmrIssuingCntry();
    }

    if (cmrNo != null && issuingCntry != null && cmrNo.startsWith("99") && isOldInternalCmr(entityManager, cmrNo, issuingCntry)) {
      return true;
    }

    return false;
  }

  private boolean isOldInternalCmr(EntityManager entityManager, String cmrNo, String country) {

    boolean isAnOldCmr = false;
    List<String> rdcAddrTypes = getRdcAddressTypes(entityManager, cmrNo, country);
    List<CmrtAddr> origLDAddr = getOrigLDAddresses(entityManager, cmrNo, country);

    boolean rdcOnlyHasZS01 = false;
    boolean legacyIsNotSharedSeq = false;
    // Check if RDC only contains ZS01
    if (!rdcAddrTypes.isEmpty() && rdcAddrTypes.size() == 1 && "ZS01".equals(rdcAddrTypes.get(0))) {
      rdcOnlyHasZS01 = true;
    } else {
      return false;
    }

    // Check if Legacy is not shared sequence
    if (origLDAddr.size() > 1 && !isAddrsSharedSeq(origLDAddr)) {
      legacyIsNotSharedSeq = true;
    }

    if (rdcOnlyHasZS01 && legacyIsNotSharedSeq) {
      return true;
    }

    return isAnOldCmr;
  }

  private boolean isAddrsSharedSeq(List<CmrtAddr> addrList) {

    for (CmrtAddr addr : addrList) {
      int yFlags = countYFlags(addr);
      if (yFlags > 1) {
        return true;
      }
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

    return count;
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
  public void transformLegacyCustomerExtDataMassUpdate(EntityManager entityManager, CmrtCustExt custExt, CMRRequestContainer cmrObjects,
      MassUpdtData muData, String cmr) throws Exception {
    if (!StringUtils.isBlank(muData.getCompany())) {
      if (SystemLocation.TANZANIA.equals(cmrIssuingCntry)) {
        custExt.setiTaxCode(muData.getCompany());
      }
    }
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "00001";
  }

  private List<String> getRdcAddressTypes(EntityManager entityManager, String cmrNo, String cntry) {
    LOG.debug("Get RDC Address types ");
    String sql = ExternalizedQuery.getSql("GET.RDC_ADDR_TYPES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("CNTRY", cntry);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);
    List<String> rdcSequences = query.getResults(String.class);
    LOG.debug("RDC addr types =" + rdcSequences);
    return rdcSequences;
  }

  private List<CmrtAddr> getOrigLDAddresses(EntityManager entityManager, String cmrNo, String country) {

    String sql = ExternalizedQuery.getSql("LEGACYD.GETADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", country);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(true);
    List<CmrtAddr> addresses = query.getResults(CmrtAddr.class);
    if (addresses != null) {
      LOG.debug(">> checkLDAddress for CMR# " + cmrNo + " > " + addresses.size());
    }
    return addresses;
  }

  @Override
  public String getDupCreationCountryId(EntityManager entityManager, String cntry, String cmrNo) {
    if ("764".equals(cntry) || "780".equals(cntry))
      return "NA";
    else
      return "764";
  }

  @Override
  public void getTargetCountryId(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, String cntry, String cmrNo) {
    if (!"764".equals(cntry)) {
      generateCMRNoObj.setLoc2("764");
    }
  }
}
