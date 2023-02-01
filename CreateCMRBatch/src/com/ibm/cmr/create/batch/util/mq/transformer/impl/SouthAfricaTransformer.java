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
public class SouthAfricaTransformer extends MCOTransformer {

  private static final Logger LOG = Logger.getLogger(MCOTransformer.class);
  private static final String DEFAULT_CLEAR_NUM = "0";
  private static final String DEFAULT_CLEAR_CHAR = "@";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";

  public SouthAfricaTransformer() {
    super(SystemLocation.SOUTH_AFRICA);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {

    Map<String, String> messageHash = handler.messageHash;
    messageHash.put("MarketingResponseCode", "2");
    messageHash.put("CEdivision", "2");
    messageHash.put("CurrencyCode", "SA");

  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;

    String addrKey = getAddressKey(addrData.getId().getAddrType());

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
    String line7 = "";

    // line1 is always customer name
    line1 = addrData.getCustNm1();
    line2 = addrData.getCustNm2();

    String attnPerson = addrData.getCustNm4();
    if (StringUtils.isNotBlank(attnPerson) && !hasAttnPrefix(attnPerson.toUpperCase())) {
      attnPerson = "Att: " + attnPerson;
    }

    line3 = attnPerson;
    line4 = addrData.getAddrTxt();

    String streetContPoBox = addrData.getAddrTxt2();
    if (StringUtils.isNotBlank(streetContPoBox) && StringUtils.isNotBlank(addrData.getPoBox())) {
      streetContPoBox += ", PO BOX " + addrData.getPoBox();
    } else if (StringUtils.isBlank(streetContPoBox) && StringUtils.isNotBlank(addrData.getPoBox())) {
      streetContPoBox = "PO BOX " + addrData.getPoBox();
    }

    line5 = streetContPoBox;

    String cityPostalCode = addrData.getCity1();
    if (StringUtils.isNotBlank(cityPostalCode) && StringUtils.isNotBlank(addrData.getPostCd())) {
      cityPostalCode += ", " + addrData.getPostCd();
    }

    line6 = cityPostalCode;
    if (crossBorder) {
      line7 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    }

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6, line7 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6 + " | " + line7);

    for (String line : lines) {
      if (StringUtils.isNotBlank(line)) {
        messageHash.put(addrKey + "Address" + lineNo, line);
        lineNo++;
      }
    }

    if (lineNo == 5) {
      messageHash.put(addrKey + "Address5", null);
      messageHash.put(addrKey + "Address6", null);
    } else if (lineNo == 6) {
      messageHash.put(addrKey + "Address6", null);
    }
  }

  private boolean hasAttnPrefix(String attnPerson) {
    String[] attPersonPrefix = { "Att: ", "Att ", "Attention Person", "ATT: ", "ATT ", "att: ", "att ", "ATT:", "Att:" };
    boolean isPrefixFound = false;

    for (String prefix : attPersonPrefix) {
      if (!isPrefixFound) {
        if (attnPerson.startsWith(prefix)) {
          isPrefixFound = true;
        }
      }
    }
    return isPrefixFound;
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    LOG.debug("transformLegacyAddressData South Africa transformer...");
    formatAddressLines(dummyHandler);

    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    } else {
      legacyAddr.setAddrPhone("");
    }

    String poBox = currAddr.getPoBox();
    if (!StringUtils.isEmpty(poBox) && ("ZS01".equals(currAddr.getId().getAddrType()) || "ZP01".equals(currAddr.getId().getAddrType()))) {
      if (!poBox.startsWith("PO BOX ")) {
        legacyAddr.setPoBox("PO BOX " + currAddr.getPoBox());
      } else {
        legacyAddr.setPoBox(poBox);
      }
    } else if (StringUtils.isEmpty(poBox) && ("ZS01".equals(currAddr.getId().getAddrType()) || "ZP01".equals(currAddr.getId().getAddrType()))) {
      legacyAddr.setPoBox("");
    }

    String streetCont = currAddr.getAddrTxt2();
    String street = currAddr.getAddrTxt();
    if ("ZS01".equals(currAddr.getId().getAddrType()) || "ZP01".equals(currAddr.getId().getAddrType())) {
      if (StringUtils.isEmpty(street) && !StringUtils.isEmpty(poBox) && !StringUtils.isEmpty(streetCont)) {
        if (!poBox.startsWith("PO BOX ")) {
          legacyAddr.setAddrLine4(streetCont + ", " + "PO BOX " + currAddr.getPoBox());
        }
      }
    }

  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData South Africa transformer...");
    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();
    boolean isCrossBorder = false;
    List<String> gmllcScenarios = Arrays.asList("NALLC", "LSLLC", "SZLLC", "NABLC", "LSBLC", "SZBLC");
    String landedCntry = "";
    // formatDataLines(dummyHandler);
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      String custSubGrp = data.getCustSubGrp();
      String[] busPrSubGrp = { "LSBP", "SZBP", "ZABP", "NABP", "ZAXBP", "NAXBP", "LSXBP", "SZXBP" };

      boolean isBusPr = Arrays.asList(busPrSubGrp).contains(custSubGrp);
      if (isBusPr) {
        legacyCust.setAuthRemarketerInd("1");
      } else {
        legacyCust.setAuthRemarketerInd("0");
      }

      legacyCust.setCeDivision("2");
      legacyCust.setCurrencyCd("SA");
      legacyCust.setTaxCd(data.getSpecialTaxCd());
      legacyCust.setMrcCd("2");
      legacyCust.setCollectionCd("000001");
      legacyCust.setCreditCd(""); // blank on new creates
      legacyCust.setDcRepeatAgreement("0");
      legacyCust.setLeasingInd("0");

      if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
        legacyCust.setSalesRepNo(data.getRepTeamMemberNo());
        if (!data.getRepTeamMemberNo().equals(legacyCust.getSalesGroupRep())) {
          legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
        }
      } else {
        legacyCust.setSalesRepNo("");
      }

      String subregion = data.getCountryUse();
      if (!StringUtils.isBlank(subregion) && "864LS".equals(subregion)) {
        legacyCust.setRealCtyCd("711");
      } else if (!StringUtils.isBlank(subregion) && "864NA".equals(subregion)) {
        legacyCust.setRealCtyCd("682");
      } else if (!StringUtils.isBlank(subregion) && "864SZ".equals(subregion)) {
        legacyCust.setRealCtyCd("853");
      }
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {

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

      String creditCd = legacyCust.getCreditCd();
      legacyCust.setCreditCd(creditCd);

      String dataEmbargoCd = data.getEmbargoCd();
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin); // permanent
                                                                                            // removal-single
      // inactivation
      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason())
          && !CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason())) {
        if (!StringUtils.isBlank(rdcEmbargoCd) && ("Y".equals(rdcEmbargoCd))) {
          if (StringUtils.isBlank(data.getEmbargoCd())) {
            legacyCust.setEmbargoCd("");
          }
        }
      }

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

      if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
        DataRdc rdcData = null;
        rdcData = LegacyDirectUtil.getOldData(entityManager, String.valueOf(data.getId().getReqId()));
        if (rdcData != null && !data.getRepTeamMemberNo().equals(rdcData.getRepTeamMemberNo())) {
          legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
        }
        legacyCust.setSalesRepNo(data.getRepTeamMemberNo());
      } else {
        legacyCust.setSalesGroupRep("");
        legacyCust.setSalesRepNo("");
      }

    }

    if (!StringUtils.isBlank(data.getSalesBusOffCd())) {
      String formatSBO = data.getSalesBusOffCd() + "000";
      legacyCust.setIbo(formatSBO);
      legacyCust.setSbo(formatSBO);
    } else {
      legacyCust.setIbo("");
      legacyCust.setSbo("");
    }

    for (Addr addr : cmrObjects.getAddresses()) {
      if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
        if (!StringUtils.isEmpty(addr.getCustPhone())) {
          legacyCust.setTelNoOrVat(addr.getCustPhone());
        } else {
          legacyCust.setTelNoOrVat("");
        }
        break;
      }
    }

    if (!StringUtils.isBlank(data.getAdminDeptLine())) {
      if (data.getAdminDeptLine().length() == 6)
        legacyCust.setDeptCd(data.getAdminDeptLine().substring(2, 6));
    }

    legacyCust.setAbbrevNm(!StringUtils.isEmpty(data.getAbbrevNm()) ? data.getAbbrevNm().toUpperCase() : data.getAbbrevNm());
    legacyCust.setLangCd("1");
    legacyCust.setMrcCd("2");
    legacyCust.setCustType(data.getCrosSubTyp());
    legacyCust.setBankBranchNo("");

    for (Addr addr : cmrObjects.getAddresses()) {
      if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
        landedCntry = addr.getLandCntry();
        if (isCrossBorder(addr)) {
          isCrossBorder = true;
        }
        break;
      }
    }

    if (dummyHandler != null && !StringUtils.isEmpty(dummyHandler.messageHash.get("VAT"))) {
      legacyCust.setVat(dummyHandler.messageHash.get("VAT"));
    } else {
      legacyCust.setVat(data.getVat());
    }

    // CREATCMR-4293
    if (!StringUtils.isEmpty(data.getIsuCd())) {
      if (StringUtils.isEmpty(data.getClientTier())) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      }
    }

    if (!StringUtils.isEmpty(data.getIsuCd()) && "5K".equals(data.getIsuCd())) {
      legacyCust.setIsuCd(data.getIsuCd() + "7");
    }

    // CREATCMR-7985
    legacyCust.setEnterpriseNo(!StringUtils.isEmpty(data.getEnterprise()) ? data.getEnterprise() : "");
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
  public boolean hasCmrtCustExt() {
    return true;
  }

  @Override
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      legacyCustExt.setTeleCovRep("Z13100");
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      String collBO = data.getCollBoId();
      if (!StringUtils.isEmpty(collBO) && collBO.length() == 4) {
        collBO = "Z1" + collBO;
      } else if (StringUtils.isEmpty(collBO)) {
        collBO = "";
      }
      legacyCustExt.setTeleCovRep(collBO);
    }
  }

  @Override
  public void transformLegacyCustomerExtDataMassUpdate(EntityManager entityManager, CmrtCustExt custExt, CMRRequestContainer cmrObjects,
      MassUpdtData muData, String cmr) throws Exception {

    if (!StringUtils.isBlank(muData.getTaxCd1())) {
      if (DEFAULT_CLEAR_NUM.equals(muData.getTaxCd1())) {
        custExt.setTeleCovRep("");
      } else {
        String collBO = muData.getTaxCd1();
        if (!StringUtils.isEmpty(collBO) && collBO.length() == 4) {
          collBO = "Z1" + collBO;
        }
        custExt.setTeleCovRep(collBO);
      }
    }

  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr muAddr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {

    LegacyCommonUtil.transformBasicLegacyAddressMassUpdate(entityManager, legacyAddr, muAddr, cntry, cust, data);
    legacyAddr.setForUpdate(true);

    if ("ZD01".equals(muAddr.getId().getAddrType())) {
      if (!StringUtils.isBlank(muAddr.getCustPhone())) {
        if (DEFAULT_CLEAR_NUM.equals(muAddr.getCustPhone())) {
          legacyAddr.setAddrPhone("");
        } else {
          legacyAddr.setAddrPhone(muAddr.getCustPhone());
        }
      }
    }

    if (!StringUtils.isBlank(muAddr.getPostCd())) {
      if (DEFAULT_CLEAR_NUM.equals(muAddr.getPostCd())) {
        legacyAddr.setZipCode("");
      } else {
        legacyAddr.setZipCode(muAddr.getPostCd());
      }
    }

    formatMassUpdateAddressLines(entityManager, legacyAddr, muAddr, false);
    legacyObjects.addAddress(legacyAddr);
  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust legacyCust, CMRRequestContainer cmrObjects,
      MassUpdtData muData) {
    LOG.debug("ZA >> Mapping default Data values..");
    LegacyCommonUtil.setlegacyCustDataMassUpdtFields(entityManager, legacyCust, muData);

    if (!StringUtils.isBlank(muData.getRestrictTo())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getRestrictTo())) {
        legacyCust.setTelNoOrVat("");
      } else {
        legacyCust.setTelNoOrVat(muData.getRestrictTo());
      }
    }

    if (!StringUtils.isBlank(muData.getVat())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getVat())) {
        legacyCust.setVat("");
      }
    }

    if (!StringUtils.isBlank(muData.getAffiliate())) {
      if (muData.getAffiliate().length() == 6) {
        legacyCust.setDeptCd(muData.getAffiliate().substring(2, 6));
      } else if (muData.getAffiliate().length() <= 4) {
        legacyCust.setDeptCd(muData.getAffiliate());
      }
    }

    if (!StringUtils.isBlank(muData.getCustNm1()) && !DEFAULT_CLEAR_CHAR.equals(muData.getCustNm1())) {
      String formatSBO = muData.getCustNm1() + "000";
      legacyCust.setSbo(formatSBO);
      legacyCust.setIbo(formatSBO);
    }

    if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
      legacyCust.setSalesRepNo(muData.getRepTeamMemberNo());
      legacyCust.setSalesGroupRep(muData.getRepTeamMemberNo());
    }

    if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
      String subInd = muData.getSubIndustryCd();
      legacyCust.setImsCd(subInd);
    }

    if (StringUtils.isNotBlank(muData.getSvcArOffice()) && !DEFAULT_CLEAR_CHAR.equals(muData.getSvcArOffice())) {
      legacyCust.setModeOfPayment(muData.getSvcArOffice());
    } else if (StringUtils.isNotBlank(muData.getMilitary())) {
      String cod = muData.getMilitary();
      if ("Y".equals(cod)) {
        legacyCust.setModeOfPayment("5");
      } else if ("N".equals(cod) || DEFAULT_CLEAR_CHAR.equals(cod)) {
        legacyCust.setModeOfPayment("");
      }
    } else if (StringUtils.isNotBlank(muData.getSvcArOffice()) && DEFAULT_CLEAR_CHAR.equals(muData.getSvcArOffice())) {
      legacyCust.setModeOfPayment("");
    }

    if (StringUtils.isNotBlank(muData.getClientTier()) && "5K".equals(muData.getIsuCd())) {
      legacyCust.setIsuCd(muData.getIsuCd() + "7");
    } else {
      String isuCd = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
          + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
      if (isuCd != null && isuCd.endsWith("@")) {
        legacyCust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : legacyCust.getIsuCd().substring(0, 2)) + "7");
      } else if (isuCd != null) {
        legacyCust.setIsuCd(isuCd);
      }

    }

    if (!StringUtils.isBlank(muData.getEnterprise())) {
      if ("@@@@@@".equals(muData.getEnterprise().trim())) {
        legacyCust.setEnterpriseNo("");
      } else {
        legacyCust.setEnterpriseNo(muData.getEnterprise());
      }
    }

  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    LOG.debug("Set max and min range For ZA...");
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    String[] interalSubGrp = { "ZAXIN", "NAXIN", "LSXIN", "SZXIN", "ZAINT", "NAINT", "LSINT", "SZINT" };
    boolean isInternal = Arrays.asList(interalSubGrp).contains(custSubGrp);

    if (isInternal) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
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
      } else if (!"ZS01".equals(addr.getId().getAddrType()) && StringUtils.isEmpty(addr.getSapNo())) {
        addr.setChangedIndc("Y");
      }
    }

    return false;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "00001";
  }

  @Override
  public String getGmllcDupCreation(Data data) {
    List<String> validScenarios = Arrays.asList("NALLC", "LSLLC", "SZLLC", "NABLC", "LSBLC", "SZBLC");
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
    List<String> bpGMLLCScenarios = Arrays.asList("NABLC", "LSBLC", "SZBLC");
    boolean isGMLLC = false;
    if (data != null) {
      if (!"NA".equals(getGmllcDupCreation(data))) {
        isGMLLC = true;
      }
      if (legacyCust != null && isGMLLC) {
        if (bpGMLLCScenarios.contains(data.getCustSubGrp())) {
          legacyCust.setIsuCd("8B7");
          legacyCust.setSalesRepNo("DUMMY1");
          legacyCust.setSalesGroupRep("DUMMY1");
          legacyCust.setSbo("0010000");
          legacyCust.setIbo("0010000");
          legacyCust.setInacCd("");
          legacyCust.setMrcCd("5");
          legacyCust.setLeadingAccNo(legacyCust.getId().getCustomerNo() + legacyCust.getMrcCd());
        } else {
          legacyCust.setIsuCd("34Q");
          legacyCust.setSalesRepNo("DUMMY1");
          legacyCust.setSalesGroupRep("DUMMY1");
          legacyCust.setSbo("0080000");
          legacyCust.setIbo("0080000");
        }
      }
    }
  }

  @Override
  public String getMailSendingFlag(Data data, Admin admin, EntityManager entityManager) {
    String oldCOD = "";
    String oldCOF = "";
    String sql = ExternalizedQuery.getSql("GET.OLD_COD_COF_BY_REQID");
    String currentCOF = data.getCommercialFinanced();
    String currentCOD = data.getCreditCd();
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      oldCOD = result[0] != null ? (String) result[0] : "";
      oldCOF = result[1] != null ? (String) result[0] : "";
    }

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
    String subregion = data.getCountryUse();
    subregion = StringUtils.isNotBlank(subregion) && subregion.length() > 3 ? subregion.substring(3, 5) : "";
    params.setSubregion(subregion);
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
  public void formatMassUpdateAddressLines(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr massUpdtAddr, boolean isFAddr) {
    LOG.debug("*** START GR formatMassUpdateAddressLines >>>");
    if (LegacyCommonUtil.isCheckDummyUpdate(massUpdtAddr)) {
      return; // dummy update , skip address update
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
    String line7 = "";

    // line1 is always customer name
    line1 = massUpdtAddr.getCustNm1();
    line2 = massUpdtAddr.getCustNm2();

    String attnPerson = massUpdtAddr.getCounty();
    if (StringUtils.isNotBlank(attnPerson) && !hasAttnPrefix(attnPerson.toUpperCase())) {
      attnPerson = "Att: " + attnPerson;
    }

    line3 = attnPerson;
    line4 = massUpdtAddr.getAddrTxt();

    legacyAddr.setStreet(!StringUtils.isBlank(line4) ? line4 : "");

    String streetContPoBox = massUpdtAddr.getAddrTxt2();
    String poBox = !StringUtils.isBlank(massUpdtAddr.getPoBox()) ? "PO BOX " + massUpdtAddr.getPoBox() : "";
    if (StringUtils.isNotBlank(streetContPoBox) && StringUtils.isNotBlank(massUpdtAddr.getPoBox())) {
      streetContPoBox += ", PO BOX " + massUpdtAddr.getPoBox();
    } else if (StringUtils.isBlank(streetContPoBox) && StringUtils.isNotBlank(massUpdtAddr.getPoBox())) {
      streetContPoBox = "PO BOX " + massUpdtAddr.getPoBox();
    }

    legacyAddr.setPoBox(poBox);
    legacyAddr.setStreetNo("");
    line5 = streetContPoBox;

    String cityPostalCode = massUpdtAddr.getCity1();
    if (StringUtils.isNotBlank(cityPostalCode) && StringUtils.isNotBlank(massUpdtAddr.getPostCd())) {
      cityPostalCode += ", " + massUpdtAddr.getPostCd();
    }

    legacyAddr.setCity(!StringUtils.isBlank(massUpdtAddr.getCity1()) ? massUpdtAddr.getCity1() : "");
    legacyAddr.setZipCode(!StringUtils.isBlank(massUpdtAddr.getPostCd()) ? massUpdtAddr.getPostCd() : "");

    line6 = cityPostalCode;
    if (crossBorder && !StringUtils.isEmpty(massUpdtAddr.getLandCntry())) {
      line7 = LandedCountryMap.getCountryName(massUpdtAddr.getLandCntry());
    }

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6, line7 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6 + " | " + line7);

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
  public boolean isCrossBorderForMass(MassUpdtAddr addr, CmrtAddr legacyAddr) {
    boolean isCrossBorder = false;
    String cd = MCOHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    if (!StringUtils.isEmpty(addr.getLandCntry()) && !cd.equals(addr.getLandCntry())) {
      isCrossBorder = true;
    }
    return isCrossBorder;
  }

  @Override
  public boolean skipCreditCodeUpdateForCountry() {
    return true;
  }

  @Override
  public String getDupCreationCountryId(EntityManager entityManager, String cntry, String cmrNo) {
    return "764";
  }

  @Override
  public void getTargetCountryId(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, String cntry, String cmrNo) {
    generateCMRNoObj.setLoc2("764");
  }

}
