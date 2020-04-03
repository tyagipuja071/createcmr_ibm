/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * {@link MessageTransformer} implementation for Greece.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class GreeceTransformer extends EMEATransformer {

  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode" };

  private static final String[] ADDRESS_ORDER = { "ZP01", "ZS01", "ZD01", "ZI01" };

  private static final Logger LOG = Logger.getLogger(GreeceTransformer.class);

  public static final String DEFAULT_LANDED_COUNTRY = "ES";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";
  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";

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

  public GreeceTransformer() {
    super(SystemLocation.GREECE);

  }

  public GreeceTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

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

    messageHash.put("EmbargoCode", !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "");
    messageHash.put("SourceCode", "EFV");
    messageHash.put("CEdivision", "3");
    messageHash.put("MarketingResponseCode", "3");
    messageHash.put("ARemark", "");
    messageHash.put("EnterpriseNo", cmrData.getEnterprise());

    if (update) {
      messageHash.put("CollectionCode", cmrData.getCollectionCd());
    } else {
      messageHash.put("CollectionCode", "");
    }
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

    messageHash.put("SourceCode", "EFV");
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    LOG.debug("Handling  Data for " + addrData.getCustNm1());

    // customer name
    String line1 = addrData.getCustNm1();

    // nickname, or occupation
    String line2 = "";

    if (!StringUtils.isBlank(addrData.getCustNm2())) {
      line2 = addrData.getCustNm2();
    } else {
      // move occupation up
      line2 = addrData.getAddrTxt2();
    }

    // occupation or PO Box or Phone (for additional shipping)
    String line3 = "";
    if (!StringUtils.isBlank(addrData.getCustNm2())) {
      // nickname specified, so line3 = occupation or pobox or phone
      if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrData.getId().getAddrType())) {
        line3 = addrData.getCustPhone();
      } else {
        if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
          line3 = addrData.getAddrTxt2();
        } else {
          line3 = addrData.getPoBox();
        }
      }
    } else {
      // occupation moved up, this is pobox or phone
      if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrData.getId().getAddrType())) {
        line3 = addrData.getCustPhone();
      } else {
        line3 = addrData.getPoBox();
      }
    }

    // Street
    String line4 = addrData.getAddrTxt();

    // postal code + city
    String line5 = (!StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "") + " "
        + (!StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "");
    line5 = line5.trim();

    // country
    String line6 = "";

    if (MQMsgConstants.ADDR_ZS01.equals(addrData.getId().getAddrType())) {
      line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    }

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
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "AddressU", !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat()
          : "");
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

    String countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());
    messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Country", crossBorder ? countryName : "");

  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Local Language translation of Sold";
    case "ZS01":
      return "Sold To";
    case "ZD01":
      return "Ship To";
    case "ZI01":
      return "Install At";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Local Language translation of Sold";
    case "ZS01":
      return "Sold To";
    case "ZD01":
      return "Ship To";
    case "ZI01":
      return "Install At";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.GREECE;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "2";
  }

  /**
   * Checks if this is a cross-border scenario
   * 
   * @param addr
   * @return
   */
  protected boolean isCrossBorder(Addr addr) {
    return !"GR".equals(addr.getLandCntry());
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZP02:
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

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData GREECE transformer...");
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    formatDataLines(dummyHandler);
    String landedCntry = "";
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) { // Leasing
      // Company
      // ind
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("LeasingCompany"))) {
        legacyCust.setLeasingInd(dummyHandler.messageHash.get("LeasingCompany"));
      }
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("CEdivision"))) {
        legacyCust.setCeDivision(dummyHandler.messageHash.get("CEdivision"));
      }
      legacyCust.setAccAdminBo("Y60382");
      // legacyCust.setCeDivision("2"); // extract the phone from billing
      // as main phone
      for (Addr addr : cmrObjects.getAddresses()) {
        if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
          legacyCust.setTelNoOrVat(addr.getCustPhone());
          landedCntry = addr.getLandCntry();
          break;
        }
      }
      // other fields to be transformed is pending
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
    } // common data for C/U
    // formatted data
    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("AbbreviatedLocation"))) {
      legacyCust.setAbbrevLocn(dummyHandler.messageHash.get("AbbreviatedLocation"));
    }
    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("ModeOfPayment"))) {
      legacyCust.setModeOfPayment(dummyHandler.messageHash.get("ModeOfPayment"));
    }
    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("EconomicCode"))) {
      legacyCust.setEconomicCd(dummyHandler.messageHash.get("EconomicCode"));
    } // other fields to be transformed is pending
    legacyCust.setDistrictCd(data.getCollectionCd() != null ? data.getCollectionCd() : "");
    legacyCust.setBankBranchNo(data.getIbmDeptCostCenter() != null ? data.getIbmDeptCostCenter() : "");
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
  public void transformOtherData(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects, CMRRequestContainer cmrObjects) {
    long reqId = cmrObjects.getAdmin().getId().getReqId();
    List<Addr> addrList = cmrObjects.getAddresses();
    List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
    
    int seqStartForRequiredAddr = 2;
    for (int i = 0; i < addrList.size(); i++) {
      Addr addr = addrList.get(i);
      String addrSeq = addr.getId().getAddrSeq();
      String addrType = addr.getId().getAddrType();

      if (addrSeq.equals("00001")) {
        if (addrType.equalsIgnoreCase(CmrConstants.ADDR_TYPE.ZP01.toString())) {
          // copy mailing from billing
          copyMailingFromBilling(legacyObjects, legacyAddrList.get(i));
        } 
        updateRequiredAddresses(entityManager, reqId, addrList.get(i).getId().getAddrType(), legacyAddrList.get(i).getId().getAddrNo(),
            changeSeqNo(seqStartForRequiredAddr++), legacyObjects, i);
      }
      modifyAddrUseFields(getAddressUse(addr), legacyAddrList.get(i));
    }
  }

  private void updateRequiredAddresses(EntityManager entityManager, long reqId, String addrType, String oldSeq, String newSeq,
      LegacyDirectObjectContainer legacyObjects, int index) {
    updateAddrSeq(entityManager, reqId, addrType, oldSeq, newSeq, null);
    legacyObjects.getAddresses().get(index).getId().setAddrNo(newSeq);
  }
  
  private void copyMailingFromBilling(LegacyDirectObjectContainer legacyObjects, CmrtAddr billingAddr) {
    CmrtAddr mailingAddr = (CmrtAddr) SerializationUtils.clone(billingAddr);
    mailingAddr.getId().setAddrNo(String.format("%05d", 1)); // should be 00001
    modifyAddrUseFields(MQMsgConstants.SOF_ADDRESS_USE_MAILING, mailingAddr);
    legacyObjects.getAddresses().add(mailingAddr);
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
    LOG.debug("GREECE - Assigning address sequence " + newSeq + " to " + addrType + " address.");
    q.executeSql();
  }
  
  private void modifyAddrUseFields(String addrUse, CmrtAddr legacyAddr) {
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
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For GR...");
    if (custSubGrp != null && "INTER".equals(custSubGrp) || custSubGrp != null && "XINTR".equals(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }

}
