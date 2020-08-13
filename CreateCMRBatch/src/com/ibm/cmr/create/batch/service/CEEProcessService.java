package com.ibm.cmr.create.batch.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtAddrPK;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.impl.SOFMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.TransformerManager;

public class CEEProcessService extends LegacyDirectService {
  public static final Logger LOG = Logger.getLogger(CEEProcessService.class);
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
  private static final String ADDRESS_USE_EXISTS = "Y";
  private static final String ADDRESS_USE_NOT_EXISTS = "N";

  protected void processDupCreate(EntityManager entityManager, Admin admin, CMRRequestContainer cmrObjects) throws Exception {
    LOG.debug("Started Create duplicate CMR processing of Request " + admin.getId().getReqId());
    Data data = cmrObjects.getData();
    if ("821".equals(data.getCmrIssuingCntry().trim())) {
      // finally persist all data
      CmrtCust legacyCust = initEmpty(CmrtCust.class);
      LegacyDirectObjectContainer legacyDupObjects = mapRequestDataForDupCreate(entityManager, cmrObjects);
      legacyCust = legacyDupObjects.getCustomer();
      if (legacyCust == null) {
        throw new Exception("Customer record cannot be created.");
      }

      LOG.info("Creating Legacy duplicate CMR Records for Request ID " + admin.getId().getReqId());
      LOG.info(" - SOF Duplicate Country: " + legacyCust.getId().getSofCntryCode() + " CMR No.: " + legacyCust.getId().getCustomerNo());

      LOG.debug("CMR No. " + data.getCmrNo() + " generated and assigned.");

      // default mapping for DATA and CMRTCUST
      LOG.debug("Mapping default Data values..");

      createEntity(legacyCust, entityManager);
      if (legacyDupObjects.getCustomerExt() != null) {
        createEntity(legacyDupObjects.getCustomerExt(), entityManager);
      }
      for (CmrtAddr legacyAddr : legacyDupObjects.getAddresses()) {
        if (legacyAddr.isForCreate()) {
          createEntity(legacyAddr, entityManager);
        }
      }
      partialCommit(entityManager);
      //completeRecord(entityManager, admin, legacyDupObjects.getCustomerNo(), legacyDupObjects);
    }
  }

  /**
   * From the {@link Admin} record, gathers the {@link Data}, {@link Addr} and
   * other relevant records and updates directly to the legacy cmr db for the
   * duplicate CMR
   * 
   * @param entityManager
   * @param admin
   * @throws Exception
   */
  protected void processDupUpdate(EntityManager entityManager, Admin admin, Data data, CMRRequestContainer cmrObjects) throws Exception {
    LOG.debug("Started Update processing of Request " + admin.getId().getReqId());
    // Add to limited this process just used for RU for now
    if ("821".equals(data.getCmrIssuingCntry().trim())) {
      LegacyDirectObjectContainer legacyDupObjects = mapRequestDupDataForUpdate(entityManager, cmrObjects);

      CmrtCust legacyCust = legacyDupObjects.getCustomer();

      if (legacyCust == null) {
        throw new Exception("Customer record cannot be updated.");
      }

      LOG.info("Updating Legacy Records for Request ID " + admin.getId().getReqId());
      LOG.info(" - SOF Country: " + legacyCust.getId().getSofCntryCode() + " CMR No.: " + legacyCust.getId().getCustomerNo());

      updateEntity(legacyCust, entityManager);

      if (legacyDupObjects.getCustomerExt() != null) {
        updateEntity(legacyDupObjects.getCustomerExt(), entityManager);
      }
      for (CmrtAddr legacyAddr : legacyDupObjects.getAddresses()) {
        if (legacyAddr.isForUpdate()) {
          legacyAddr.setUpdateTs(SystemUtil.getCurrentTimestamp());
          updateEntity(legacyAddr, entityManager);
        } else if (legacyAddr.isForCreate()) {
          createEntity(legacyAddr, entityManager);
        }
      }
      // completeRecord(entityManager, admin, legacyDupObjects.getCustomerNo(),
      // legacyDupObjects);
    }
  }

  /**
   * Does the mapping of legacy data to CreateCMR data for creates. The flow
   * uses a base mapping which can be overridden on the specific
   * {@link MessageTransformer} class of the country
   * 
   * @param entityManager
   * @param cmrObjects
   * @throws Exception
   */
  private LegacyDirectObjectContainer mapRequestDataForDupCreate(EntityManager entityManager, CMRRequestContainer cmrObjects) throws Exception {
    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();
    String cmrNo = data.getCmrNo();
    String cntry = data.getDupIssuingCntryCd();
    String companyBK = data.getEnterprise();
    data.setEnterprise(data.getDupEnterpriseNo());
    data.setDupSalesBoCd(data.getSalesBusOffCd());
    data.setDupSalesRepNo(data.getSalesBusOffCd());

    LOG.debug("Issued country. " + cntry + " duplicate issued country used to generated and assigned.");

    MessageTransformer transformer = TransformerManager.getTransformer(cntry);
    legacyObjects.setCustomerNo(cmrNo);
    legacyObjects.setSofCntryCd(cntry);

    LOG.debug("CMR No. " + cmrNo + " generated and assigned.");
    CmrtCust cust = initEmpty(CmrtCust.class);

    // default mapping for DATA and CMRTCUST
    LOG.debug("Mapping default dup Data values..");
    CmrtCustPK custPk = new CmrtCustPK();
    custPk.setCustomerNo(cmrNo);
    custPk.setSofCntryCode(cntry);
    cust.setId(custPk);
    cust.setRealCtyCd(cntry);
    cust.setStatus(LEGACY_STATUS_ACTIVE);
    cust.setAbbrevNm(data.getAbbrevNm());
    cust.setAbbrevLocn(data.getAbbrevLocn());
    cust.setLocNo(data.getLocationNumber());
    cust.setEconomicCd(data.getEconomicCd());
    String legacyLangCdMapValue = getLangCdLegacyMapping(entityManager, data, cntry);
    cust.setLangCd(!StringUtils.isEmpty(legacyLangCdMapValue) ? legacyLangCdMapValue : "");
    cust.setMrcCd(data.getMrcCd());
    cust.setModeOfPayment(data.getModeOfPayment());
    // set up ISU CODE for duplicate country
    cust.setIsuCd((!StringUtils.isEmpty(data.getDupIsuCd()) ? data.getDupIsuCd() : "")
        + (!StringUtils.isEmpty(data.getDupClientTierCd()) ? data.getDupClientTierCd() : ""));

    cust.setCreditCd(data.getCreditCd());
    cust.setTaxCd(data.getSpecialTaxCd());
    cust.setSalesRepNo(data.getRepTeamMemberNo());
    cust.setSalesGroupRep(data.getSalesTeamCd());
    // set up Enterprise number of Duplicate
    cust.setEnterpriseNo((!StringUtils.isEmpty(data.getDupEnterpriseNo()) ? data.getDupEnterpriseNo() : ""));
    cust.setCeBo(data.getEngineeringBo());
    // set up Ibo and Sbo use DUP_SALES_BO_CD
    cust.setIbo((!StringUtils.isEmpty(data.getDupSalesBoCd()) ? data.getDupSalesBoCd() : ""));
    cust.setSbo((!StringUtils.isEmpty(data.getDupSalesBoCd()) ? data.getDupSalesBoCd() : ""));
    cust.setBankNo(data.getIbmBankNumber());

    cust.setCollectionCd(data.getCollectionCd() != null ? data.getCollectionCd() : "");

    cust.setMailingCond(data.getMailingCondition());
    cust.setAccAdminBo(data.getAcAdminBo());
    cust.setEmbargoCd(data.getEmbargoCd());
    cust.setIsicCd(data.getIsicCd());
    if (!StringUtils.isEmpty(data.getInacCd())) {
      cust.setInacCd(data.getInacCd());
    }
    cust.setVat(data.getVat());
    cust.setImsCd(data.getSubIndustryCd());
    cust.setCurrencyCd(data.getLegacyCurrencyCd());
    cust.setInvoiceCpyReqd(" "); // setting default value blank
    // do a dummy transfer here, reuse MQ objects for formatting
    MqIntfReqQueue dummyQueue = new MqIntfReqQueue();
    dummyQueue.setCmrNo(cmrNo);
    dummyQueue.setCmrIssuingCntry(cntry);
    dummyQueue.setReqType(admin.getReqType());
    SOFMessageHandler dummyHandler = new SOFMessageHandler(entityManager, dummyQueue);
    dummyHandler.adminData = admin;
    dummyHandler.cmrData = data;
    if (cmrObjects.getAddresses() != null && !cmrObjects.getAddresses().isEmpty()) {
      dummyHandler.addrData = cmrObjects.getAddresses().get(0);
    }
    dummyHandler.currentAddresses = cmrObjects.getAddresses();
    if (transformer != null) {
      // call the specific transformer to change data as needed
      dummyQueue.setReqStatus(MQMsgConstants.REQ_STATUS_NEW);
      transformer.transformLegacyCustomerData(entityManager, dummyHandler, cust, cmrObjects);
    }
    cust.setLeadingAccNo(cmrNo + cust.getMrcCd());// setting leading account no
                                                  // = cmr+mrc
    capsAndFillNulls(cust, true);
    legacyObjects.setCustomer(cust);

    CmrtAddr legacyAddr = null;
    CmrtAddrPK legacyAddrPk = null;

    String sofKey = null;
    String addressUse = null;

    int seqNo = 1;
    // int nextSeq = 7;

    LOG.debug("Mapping default dup address values..");
    if (transformer != null) {
      for (Addr addr : cmrObjects.getAddresses()) {

        // detach the addr, no updates needed here for now
        entityManager.detach(addr);

        if (transformer.skipLegacyAddressData(entityManager, cmrObjects, addr, true)) {
          LOG.debug("Skipping dup legacy create for address :" + addr.getId().getAddrType());
          continue;
        }

        dummyHandler.addrData = addr;
        dummyQueue.setReqStatus(seqNo == 1 ? MQMsgConstants.REQ_STATUS_NEW : "COM" + (seqNo - 1));
        legacyAddr = initEmpty(CmrtAddr.class);
        legacyAddrPk = new CmrtAddrPK();
        legacyAddrPk.setCustomerNo(cmrNo);
        legacyAddrPk.setSofCntryCode(cntry);
        String newSeqNo = StringUtils.leftPad(Integer.toString(seqNo), 5, '0');
        // Mukesh:Story 1698123
        if ("00001".equals(addr.getId().getAddrSeq()))
          legacyAddrPk.setAddrNo(newSeqNo);
        else
          legacyAddrPk.setAddrNo(addr.getId().getAddrSeq());
        // legacyAddrPk.setAddrNo(Integer.toString(nextSeq));
        // legacyAddrPk.setAddrNo(Integer.toString(seqNo));
        legacyAddr.setId(legacyAddrPk);

        sofKey = transformer.getAddressKey(addr.getId().getAddrType());
        transformer.formatAddressLines(dummyHandler);

        legacyAddr.setAddrLine1(dummyHandler.messageHash.get(sofKey + "Address1"));
        legacyAddr.setAddrLine2(dummyHandler.messageHash.get(sofKey + "Address2"));
        legacyAddr.setAddrLine3(dummyHandler.messageHash.get(sofKey + "Address3"));
        legacyAddr.setAddrLine4(dummyHandler.messageHash.get(sofKey + "Address4"));
        legacyAddr.setAddrLine5(dummyHandler.messageHash.get(sofKey + "Address5"));
        legacyAddr.setAddrLine6(dummyHandler.messageHash.get(sofKey + "Address6"));
        legacyAddr.setAddrLineI(dummyHandler.messageHash.get(sofKey + "AddressI"));
        legacyAddr.setAddrLineO(dummyHandler.messageHash.get(sofKey + "AddressO"));
        legacyAddr.setAddrLineT(dummyHandler.messageHash.get(sofKey + "AddressT"));
        legacyAddr.setAddrLineU(dummyHandler.messageHash.get(sofKey + "AddressU"));

        legacyAddr.setStreet(addr.getAddrTxt());

        if ("ZD01".equals(addr.getId().getAddrType()) && !StringUtils.isEmpty(addr.getCustPhone())) {
          legacyAddr.setAddrPhone("TF" + addr.getCustPhone().trim());
        }
        legacyAddr.setCity(addr.getCity1());
        legacyAddr.setContact(addr.getDept());
        legacyAddr.setCreateTs(SystemUtil.getCurrentTimestamp());
        legacyAddr.setUpdateTs(SystemUtil.getCurrentTimestamp());

        if (!StringUtils.isBlank(addr.getPoBox())) {
          legacyAddr.setPoBox("APTO " + addr.getPoBox().trim());
        } else {
          legacyAddr.setPoBox(addr.getPoBox());
        }

        legacyAddr.setZipCode(addr.getPostCd());
        legacyAddr.setForCreate(true);
        legacyAddr.setForSharedSeq(false);

        transformer.transformLegacyAddressData(entityManager, dummyHandler, cust, legacyAddr, cmrObjects, addr);
        capsAndFillNulls(legacyAddr, true);
        legacyObjects.addAddress(legacyAddr);

        // parse each address use
        addressUse = transformer.getAddressUse(addr);

        modifyAddrUseFields(legacyAddr.getId().getAddrNo(), addressUse, legacyAddr);

        // update the seqno of the original addr record
        if ("00001".equals(addr.getId().getAddrSeq())) {
          updateAddrSeq(entityManager, admin.getId().getReqId(), addr.getId().getAddrType(), addr.getId().getAddrSeq(),
              legacyAddr.getId().getAddrNo() + "", null, legacyAddr.isForSharedSeq());
          seqNo++;
        }
      }
      // Mark as Comment due custExt not used for CIS duplicate countries for
      // now
      // CmrtCustExt custExt = null;
      // CmrtCustExtPK custExtPk = null;

      // boolean isCustExt = transformer.hasCmrtCustExt();
      // if (isCustExt) {
      // LOG.debug("Mapping default Data values with Legacy CmrtCustExt
      // table.....");
      // Initialize the object
      // custExt = initEmpty(CmrtCustExt.class);
      // default mapping for ADDR and CMRTCEXT
      // custExtPk = new CmrtCustExtPK();
      // custExtPk.setCustomerNo(cmrNo);
      // custExtPk.setSofCntryCode(cntry);
      // custExt.setId(custExtPk);

      // if (transformer != null) {
      // transformer.transformLegacyCustomerExtData(entityManager, dummyHandler,
      // custExt, cmrObjects);
      // }
      // custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
      // custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
      // legacyObjects.setCustomerExt(custExt);
      // }
      transformer.transformOtherData(entityManager, legacyObjects, cmrObjects);
      data.setEnterprise(companyBK);
    }

    return legacyObjects;

  }

  private String getLangCdLegacyMapping(EntityManager entityManager, Data data, String cntry) {
    if (entityManager == null) {
      return null;
    }
    String res = "";
    try {
      String sql = ExternalizedQuery.getSql("GET_LEGACY_LANG_CD");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CD", data.getCustPrefLang());
      query.setParameter("CNTRY", cntry);
      query.setForReadOnly(true);
      res = query.getSingleResult(String.class);
    } catch (Exception e) {
      e.getMessage();
      LOG.debug("Some error occured while extracting Land Cd Legacy Mapping from DB.Returning blank");
      return "";
    }
    return res;
  }

  private void updateAddrSeq(EntityManager entityManager, long reqId, String addrType, String oldSeq, String newSeq, String kunnr,
      boolean sharedSeq) {
    String updateSeq = ExternalizedQuery.getSql("LEGACYD.UPDATE_ADDR_SEQ");
    PreparedQuery q = new PreparedQuery(entityManager, updateSeq);
    q.setParameter("NEW_SEQ", newSeq);
    q.setParameter("REQ_ID", reqId);
    q.setParameter("TYPE", addrType);
    q.setParameter("OLD_SEQ", oldSeq);
    q.setParameter("SAP_NO", kunnr);
    LOG.debug("Assigning address sequence " + newSeq + " to " + addrType + " address.");
    q.executeSql();

    if (!sharedSeq) {
      updateSeq = ExternalizedQuery.getSql("LEGACYD.UPDATE_ADDR_SEQ_RDC");
      q = new PreparedQuery(entityManager, updateSeq);
      q.setParameter("NEW_SEQ", newSeq);
      q.setParameter("REQ_ID", reqId);
      q.setParameter("TYPE", addrType);
      q.setParameter("OLD_SEQ", oldSeq);
      q.setParameter("SAP_NO", kunnr);
      q.executeSql();
    }
  }

  private static void modifyAddrUseFields(String seqNo, String addrUse, CmrtAddr legacyAddr) {

    for (String use : addrUse.split("")) {
      if (!StringUtils.isEmpty(use)) {

        if (ADDRESS_USE_MAILING.equals(use)) {
          legacyAddr.setIsAddrUseMailing(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseMailing(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_BILLING.equals(use)) {
          legacyAddr.setIsAddrUseBilling(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseBilling(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_INSTALLING.equals(use)) {
          legacyAddr.setIsAddrUseInstalling(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseInstalling(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_SHIPPING.equals(use)) {
          legacyAddr.setIsAddrUseShipping(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseShipping(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_EPL_MAILING.equals(use)) {
          legacyAddr.setIsAddrUseEPL(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseEPL(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_LIT_MAILING.equals(use)) {
          legacyAddr.setIsAddrUseLitMailing(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddrUseLitMailing(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_A.equals(use)) {
          legacyAddr.setIsAddressUseA(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseA(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_B.equals(use)) {
          legacyAddr.setIsAddressUseB(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseB(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_C.equals(use)) {
          legacyAddr.setIsAddressUseC(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseC(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_D.equals(use)) {
          legacyAddr.setIsAddressUseD(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseD(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_E.equals(use)) {
          legacyAddr.setIsAddressUseE(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseE(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_F.equals(use)) {
          legacyAddr.setIsAddressUseF(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseF(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_G.equals(use)) {
          legacyAddr.setIsAddressUseG(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseG(ADDRESS_USE_NOT_EXISTS);
        }

        if (ADDRESS_USE_COUNTRY_H.equals(use)) {
          legacyAddr.setIsAddressUseH(ADDRESS_USE_EXISTS);
        } else {
          legacyAddr.setIsAddressUseH(ADDRESS_USE_NOT_EXISTS);
        }

      }
    }
  }

  /**
   * Does the mapping of legacy data to CreateCMR data for updates. The flow
   * uses a base mapping which can be overridden on the specific
   * {@link MessageTransformer} class of the country
   * 
   * @param entityManager
   * @param cmrObjects
   * @throws Exception
   */
  private LegacyDirectObjectContainer mapRequestDupDataForUpdate(EntityManager entityManager, CMRRequestContainer cmrObjects) throws Exception {

    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();
    String cmrNo = data.getCmrNo();
    String cntryBK = data.getCmrIssuingCntry();
    String cntry = data.getDupIssuingCntryCd();
    String isuCd = data.getIsuCd();
    String clientTBK = data.getClientTier();
    String companyBK = data.getEnterprise();
    String enterpriseBK = data.getTaxCd2();
    data.setDupSalesBoCd(data.getSalesBusOffCd());
    data.setDupSalesRepNo(data.getSalesBusOffCd());
    data.setCmrIssuingCntry(cntry);
    data.setIsuCd(data.getDupIsuCd());
    data.setClientTier(data.getDupClientTierCd());
    data.setEnterprise(data.getDupEnterpriseNo());
    data.setTaxCd2(data.getTaxCd3());

    MessageTransformer transformer = TransformerManager.getTransformer(cntry);

    LegacyDirectObjectContainer legacyObjects = LegacyDirectUtil.getLegacyDBValues(entityManager, cntry, cmrNo, false, transformer.hasAddressLinks());

    CmrtCust cust = legacyObjects.getCustomer();

    // default mapping for DATA and CMRTCUST
    LOG.debug("Mapping default dup Data values..");
    cust.setStatus(LEGACY_STATUS_ACTIVE);
    if (!StringUtils.isBlank(data.getAbbrevNm())) {
      cust.setAbbrevNm(data.getAbbrevNm());
    }
    if (!StringUtils.isBlank(data.getAbbrevLocn())) {
      cust.setAbbrevLocn(data.getAbbrevLocn());
    }
    if (!StringUtils.isBlank(data.getLocationNumber())) {
      cust.setLocNo(data.getLocationNumber());
    }
    if (!StringUtils.isBlank(data.getEconomicCd())) {
      cust.setEconomicCd(data.getEconomicCd());
    }

    String legacyLangCdMapValue = getLangCdLegacyMapping(entityManager, data, cntry);
    cust.setLangCd(!StringUtils.isEmpty(legacyLangCdMapValue) ? legacyLangCdMapValue : " ");

    if (!StringUtils.isBlank(data.getMrcCd())) {
      cust.setMrcCd(data.getMrcCd());
    }

    cust.setModeOfPayment(data.getModeOfPayment() != null ? data.getModeOfPayment() : "");

    String isuClientTier = (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
        + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : "");
    if (isuClientTier != null && isuClientTier.length() == 3) {
      cust.setIsuCd(isuClientTier);
    }

    if (!StringUtils.isBlank(data.getCreditCd())) {
      cust.setCreditCd(data.getCreditCd());
    }
    if (!StringUtils.isBlank(data.getSpecialTaxCd())) {
      cust.setTaxCd(data.getSpecialTaxCd());
    } else {
      cust.setTaxCd("");
    }

    if (!StringUtils.isBlank(data.getRepTeamMemberNo())) {
      cust.setSalesRepNo(data.getRepTeamMemberNo());
    }
    if (!StringUtils.isBlank(data.getSalesTeamCd())) {
      cust.setSalesGroupRep(data.getSalesTeamCd());
    }
    // Keep using duplicate EnterpriseNo
    cust.setEnterpriseNo((!StringUtils.isEmpty(data.getDupEnterpriseNo()) ? data.getDupEnterpriseNo() : ""));

    if (!StringUtils.isBlank(data.getEngineeringBo())) {
      cust.setCeBo(data.getEngineeringBo());
    }
    // Keep using duplicate sales BO code
    if (!StringUtils.isBlank(data.getDupSalesBoCd())) {
      cust.setIbo(data.getDupSalesBoCd());
      cust.setSbo(data.getDupSalesBoCd());
    }
    if (!StringUtils.isBlank(data.getIbmDeptCostCenter())) {
      cust.setBankBranchNo(data.getIbmDeptCostCenter());
    }
    if (!StringUtils.isBlank(data.getIbmBankNumber())) {
      cust.setBankNo(data.getIbmBankNumber());
    }

    cust.setCollectionCd(data.getCollectionCd() != null ? data.getCollectionCd() : "");

    cust.setMailingCond(data.getMailingCondition() != null ? data.getMailingCondition() : "");
    if (!StringUtils.isBlank(data.getAcAdminBo())) {
      cust.setAccAdminBo(data.getAcAdminBo());
    }

    cust.setEmbargoCd(data.getEmbargoCd() != null ? data.getEmbargoCd() : "");

    if (!StringUtils.isBlank(data.getIsicCd())) {
      cust.setIsicCd(data.getIsicCd());
    }
    cust.setInacCd(data.getInacCd() != null ? data.getInacCd() : "");
    if (!StringUtils.isBlank(data.getVat())) {
      cust.setVat(data.getVat());
    }
    if (!StringUtils.isBlank(data.getSubIndustryCd())) {
      cust.setImsCd(data.getSubIndustryCd());
    }
    if (!StringUtils.isBlank(data.getLegacyCurrencyCd())) {
      cust.setCurrencyCd(data.getLegacyCurrencyCd());
    }

    cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    cust.setUpdStatusTs(SystemUtil.getCurrentTimestamp());

    // Setting status=A in case of single reactivation requests
    if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()) && CMR_REACTIVATION_REQUEST_REASON.equals(admin.getReqReason())) {
      cust.setStatus(LEGACY_STATUS_ACTIVE);
    }

    // do a dummy transfer here, reuse MQ objects for formatting
    MqIntfReqQueue dummyQueue = new MqIntfReqQueue();
    dummyQueue.setCmrNo(cmrNo);
    dummyQueue.setCmrIssuingCntry(cntry);
    dummyQueue.setReqType(admin.getReqType());
    SOFMessageHandler dummyHandler = new SOFMessageHandler(entityManager, dummyQueue);
    dummyHandler.adminData = admin;
    dummyHandler.cmrData = data;
    if (cmrObjects.getAddresses() != null && !cmrObjects.getAddresses().isEmpty()) {
      dummyHandler.addrData = cmrObjects.getAddresses().get(0);
    }
    dummyHandler.currentAddresses = cmrObjects.getAddresses();
    if (transformer != null) {
      // call the specific transformer to change data as needed
      dummyQueue.setReqStatus(MQMsgConstants.REQ_STATUS_NEW);
      transformer.transformLegacyCustomerData(entityManager, dummyHandler, cust, cmrObjects);
    }
    capsAndFillNulls(cust, true);
    legacyObjects.setCustomer(cust);

    CmrtAddr legacyAddr = null;
    CmrtAddrPK legacyAddrPk = null;

    String sofKey = null;
    String addressUse = null;

    boolean zs01Updated = false;
    String zs01SeqNo = null;

    Map<String, Integer> sequences = new HashMap<String, Integer>();
    for (Addr addr : cmrObjects.getAddresses()) {
      if ("ZS01".equals(addr.getId().getAddrType())) {
        zs01SeqNo = addr.getId().getAddrSeq();

        zs01Updated = "Y".equals(addr.getChangedIndc());
      }
      if (!sequences.containsKey(addr.getId().getAddrSeq())) {
        sequences.put(addr.getId().getAddrSeq(), 0);
      }
      sequences.put(addr.getId().getAddrSeq(), sequences.get(addr.getId().getAddrSeq()) + 1);
    }
    LOG.debug("Sequences : " + sequences);

    Queue<Integer> availableSequences = new LinkedList<Integer>();
    availableSequences = getAvailableSequences(entityManager, cntry, cmrNo);
    for (Addr addr : cmrObjects.getAddresses()) {

      if (availableSequences.contains(Integer.parseInt(addr.getId().getAddrSeq()))) {
        availableSequences.remove(Integer.parseInt(addr.getId().getAddrSeq()));
        LOG.debug("Removing seq=" + addr.getId().getAddrSeq() + " from available sequences as it already exists on request");
      }
    }

    List<String> rdcSeq = checkIfSeqExistOnRDC(entityManager, cmrNo, cntry);

    if (!rdcSeq.isEmpty()) {
      for (int i = 0; i < rdcSeq.size(); i++) {
        if (availableSequences.contains(Integer.parseInt(rdcSeq.get(i)))) {
          availableSequences.remove(Integer.parseInt(rdcSeq.get(i)));
          LOG.debug("Removing seq=" + rdcSeq.get(i) + " from available sequences as it already exist on RDC.");
        }
      }
    }

    int seqNo = 0;
    LOG.debug("Mapping default address values..");
    if (transformer != null) {

      for (Addr addr : cmrObjects.getAddresses()) {
        if (transformer.skipLegacyAddressData(entityManager, cmrObjects, addr, false)) {
          LOG.debug("Skipping legacy update for address :" + addr.getId().getAddrType());
          continue;
        }
        boolean addrUpdated = RequestUtils.isUpdated(entityManager, addr, data.getCmrIssuingCntry());
        // iterate each address on the request and check if it needs to be
        // processed
        dummyHandler.addrData = addr;
        dummyQueue.setReqStatus(seqNo == 0 ? MQMsgConstants.REQ_STATUS_NEW : "COM" + (seqNo - 1));

        addrUpdated = "N".equals(addr.getImportInd()) || "Y".equals(addr.getChangedIndc());

        // address was directly updated, or was not updated and shares sequence
        // with sold to
        if (addrUpdated || (zs01Updated && zs01SeqNo.equals(addr.getId().getAddrSeq()))) {

          if ("ZS01".equals(addr.getId().getAddrType())) {
            LOG.debug("ZS01 address is updated, directly updating relevant records");
            // zs01 addresses should not change sequence, move everything else
            // Mukesh:Story 1698123
            legacyAddr = legacyObjects.findBySeqNo(addr.getId().getAddrSeq());
            if (legacyAddr == null) {
              throw new Exception("Cannot find legacy address with sequence " + addr.getId().getAddrSeq());
            }
            legacyAddr.setForUpdate(true);
          } else if ("N".equals(addr.getImportInd()) || sequences.get(addr.getId().getAddrSeq()) > 1) {
            // a. an added address
            // b. address shares a sequence
            LOG.trace("Created? " + ("N".equals(addr.getImportInd())) + " Shared? " + (sequences.get(addr.getId().getAddrSeq()) > 1));
            LOG.debug(addr.getId().getAddrType() + " address needs to be created on a new sequence.");
            // create a new address
            legacyAddr = initEmpty(CmrtAddr.class);
            legacyAddrPk = new CmrtAddrPK();
            legacyAddrPk.setCustomerNo(cmrNo);
            legacyAddrPk.setSofCntryCode(cntry);
            int newSeq;
            String newAddrSeq;
            if (transformer.sequenceNoUpdateLogic(entityManager, cmrObjects, addr, false)) {
              newSeq = availableSequences.remove();
              newAddrSeq = Integer.toString(newSeq);
            } else {
              newAddrSeq = addr.getId().getAddrSeq();
            }

            newAddrSeq = StringUtils.leftPad(newAddrSeq, 5, '0');
            LOG.debug("Assigning Sequence " + newAddrSeq + " to " + addr.getId().getAddrType() + " address");
            // Mukesh:Story 1698123
            legacyAddrPk.setAddrNo(newAddrSeq);
            legacyAddr.setId(legacyAddrPk);
            legacyAddr.setForUpdate(false);
            legacyAddr.setForCreate(true);
            if (sequences.get(addr.getId().getAddrSeq()) > 1)
              legacyAddr.setForSharedSeq(true);
            else
              legacyAddr.setForSharedSeq(false);
            legacyAddr.setCreateTs(SystemUtil.getCurrentTimestamp());
          } else {
            // plain update
            LOG.debug(addr.getId().getAddrType() + " address is updated, directly updating relevant records");
            // Mukesh:Story 1698123
            if ("ZP01".equals(addr.getId().getAddrType()) && StringUtils.isEmpty(addr.getSapNo())
                && SystemLocation.ITALY.equals(data.getCmrIssuingCntry())) {
              // legacyAddr = legacyObjects.findByAddressUse(addressUse);
              legacyAddr = legacyObjects.findByAddressUseFlag("2");
              // legacyAddr.getId().setAddrNo(addr.getId().getAddrSeq());
            } else {
              legacyAddr = legacyObjects.findBySeqNo(addr.getId().getAddrSeq());
            }

            if (legacyAddr == null) {
              throw new Exception("Cannot find legacy address with sequence " + addr.getId().getAddrSeq());
            }
            legacyAddr.setForUpdate(true);
          }

          legacyAddr.setAddressUse(transformer.getAddressUse(addr));
          sofKey = transformer.getAddressKey(addr.getId().getAddrType());

          if (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, cntry)) {
            transformer.transformLegacyAddressData(entityManager, dummyHandler, cust, legacyAddr, cmrObjects, addr);
          } else {
            transformer.formatAddressLines(dummyHandler);
          }

          legacyAddr.setAddrLine1(dummyHandler.messageHash.get(sofKey + "Address1"));
          legacyAddr.setAddrLine2(dummyHandler.messageHash.get(sofKey + "Address2"));
          legacyAddr.setAddrLine3(dummyHandler.messageHash.get(sofKey + "Address3"));
          legacyAddr.setAddrLine4(dummyHandler.messageHash.get(sofKey + "Address4"));
          legacyAddr.setAddrLine5(dummyHandler.messageHash.get(sofKey + "Address5"));
          legacyAddr.setAddrLine6(dummyHandler.messageHash.get(sofKey + "Address6"));
          if (!StringUtils.isBlank(dummyHandler.messageHash.get(sofKey + "AddressI"))) {
            legacyAddr.setAddrLineI(dummyHandler.messageHash.get(sofKey + "AddressI"));
          }
          if (!StringUtils.isBlank(dummyHandler.messageHash.get(sofKey + "AddressO"))) {
            legacyAddr.setAddrLineO(dummyHandler.messageHash.get(sofKey + "AddressO"));
          }
          if (!StringUtils.isBlank(dummyHandler.messageHash.get(sofKey + "AddressT"))) {
            legacyAddr.setAddrLineT(dummyHandler.messageHash.get(sofKey + "AddressT"));
          }
          if (!StringUtils.isBlank(dummyHandler.messageHash.get(sofKey + "AddressU"))) {
            legacyAddr.setAddrLineU(dummyHandler.messageHash.get(sofKey + "AddressU"));
          }

          if (!StringUtils.isBlank(addr.getAddrTxt())) {
            legacyAddr.setStreet(addr.getAddrTxt());
          }
          if ("ZD01".equals(addr.getId().getAddrType()) && !StringUtils.isEmpty(addr.getCustPhone())) {
            legacyAddr.setAddrPhone("TF" + addr.getCustPhone().trim());
          }
          if (!StringUtils.isBlank(addr.getCity1())) {
            legacyAddr.setCity(addr.getCity1());
          }
          if (!StringUtils.isBlank(addr.getDept())) {
            legacyAddr.setContact(addr.getDept());
          }
          legacyAddr.setUpdateTs(SystemUtil.getCurrentTimestamp());

          if (!StringUtils.isBlank(addr.getPoBox())) {
            legacyAddr.setPoBox("APTO " + addr.getPoBox().trim());
          }

          if (!StringUtils.isBlank(addr.getPostCd())) {
            legacyAddr.setZipCode(addr.getPostCd());
          }
          transformer.transformLegacyAddressData(entityManager, dummyHandler, cust, legacyAddr, cmrObjects, addr);
          capsAndFillNulls(legacyAddr, true);

          if (legacyAddr.isForCreate()) {
            legacyObjects.addAddress(legacyAddr);

            if (!"N".equals(addr.getImportInd())) {
              LOG.debug("Untagging use " + legacyAddr.getAddressUse() + " on sequence " + addr.getId().getAddrSeq());

              modifyAddrUseForSequence(addr.getId().getAddrSeq(), legacyAddr.getAddressUse(), legacyObjects);
            }

            // update the seqno of the original addr record
            updateAddrSeq(entityManager, admin.getId().getReqId(), addr.getId().getAddrType(), addr.getId().getAddrSeq(),
                legacyAddr.getId().getAddrNo() + "", null, legacyAddr.isForSharedSeq());

          }
        } else if ("ZP02".equals(addr.getId().getAddrType())) {
          // Defect 1759962: Spain - address F
          legacyAddr = legacyObjects.findBySeqNo(addr.getId().getAddrSeq());
          if (legacyAddr == null) {
            throw new Exception("Cannot find legacy address with sequence " + addr.getId().getAddrSeq());
          }
          legacyAddr.setForUpdate(true);

          legacyAddr.setAddressUse(transformer.getAddressUse(addr));

          sofKey = transformer.getAddressKey(addr.getId().getAddrType());
          transformer.formatAddressLines(dummyHandler);

          legacyAddr.setAddrLine1(dummyHandler.messageHash.get(sofKey + "Address1"));
          legacyAddr.setAddrLine2(dummyHandler.messageHash.get(sofKey + "Address2"));
          legacyAddr.setAddrLine3(dummyHandler.messageHash.get(sofKey + "Address3"));
          legacyAddr.setAddrLine4(dummyHandler.messageHash.get(sofKey + "Address4"));
          legacyAddr.setAddrLine5(dummyHandler.messageHash.get(sofKey + "Address5"));
          legacyAddr.setAddrLine6(dummyHandler.messageHash.get(sofKey + "Address6"));

          if (!StringUtils.isBlank(addr.getAddrTxt())) {
            legacyAddr.setStreet(addr.getAddrTxt());
          }
          if (!StringUtils.isBlank(addr.getCustPhone())) {
            legacyAddr.setAddrPhone(addr.getCustPhone());
          }
          if (!StringUtils.isBlank(addr.getCity1())) {
            legacyAddr.setCity(addr.getCity1());
          }
          if (!StringUtils.isBlank(addr.getPoBox())) {
            legacyAddr.setPoBox("APTO " + addr.getPoBox().trim());
          }

          if (!StringUtils.isBlank(addr.getPostCd())) {
            legacyAddr.setZipCode(addr.getPostCd());
          }
          transformer.transformLegacyAddressData(entityManager, dummyHandler, cust, legacyAddr, cmrObjects, addr);
          capsAndFillNulls(legacyAddr, true);
          LOG.trace("LegacyAddr No: " + legacyAddr.getId().getAddrNo() + " Addr Seq: " + addr.getId().getAddrType());
        }
        seqNo++;
      }

      // clearAddressUses(entityManager, cntry, cmrNo);
      // parse each address use
      // legacyObjects.getUses().clear();
      for (CmrtAddr currAddr : legacyObjects.getAddresses()) {
        addressUse = currAddr.getAddressUse();
        LOG.trace("Address No: " + currAddr.getId().getAddrNo() + " Uses: " + addressUse);
        if (addressUse != null && !"".equals(addressUse)) {
          for (String use : addressUse.split("")) {
            if (!StringUtils.isEmpty(use)) {

              modifyAddrUseFields(currAddr.getId().getAddrNo(), addressUse, currAddr);
            }
          }
        }
      }

      // Mark as Comment due custExt not used for CIS duplicate countries for
      // now
      // Mukesh :Dev for custExt tab
      // boolean isCustExt = transformer.hasCmrtCustExt();
      // if (isCustExt) {
      // CmrtCustExt custExt = legacyObjects.getCustomerExt();
      // if (transformer != null) {
      // transformer.transformLegacyCustomerExtData(entityManager, dummyHandler,
      // custExt, cmrObjects);
      // }
      // custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
      // custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
      // legacyObjects.setCustomerExt(custExt);
      // }
      // rebuild the address use table

      data.setDupSalesBoCd(data.getSalesBusOffCd());
      data.setDupSalesRepNo(data.getSalesBusOffCd());
      data.setCmrIssuingCntry(cntryBK);
      data.setIsuCd(isuCd);
      data.setClientTier(clientTBK);
      data.setEnterprise(companyBK);
      data.setTaxCd2(enterpriseBK);

      transformer.transformOtherData(entityManager, legacyObjects, cmrObjects);
    }

    return legacyObjects;
  }

  /**
   * Gets a list of available sequences
   * 
   * @param entityManager
   * @param country
   * @param cmrNo
   * @return
   */
  private Queue<Integer> getAvailableSequences(EntityManager entityManager, String country, String cmrNo) {
    Queue<Integer> queue = new LinkedList<>();
    String sql = "";

    if (SystemLocation.ITALY.equals(country)) {
      sql = ExternalizedQuery.getSql("LEGACYD.SEQNOTABLE.IT");
    } else {
      sql = ExternalizedQuery.getSql("LEGACYD.SEQNOTABLE");
    }

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", country);
    query.setParameter("CMR_NO", cmrNo);
    List<Integer> sequences = query.getResults(Integer.class);
    if (sequences == null || sequences.isEmpty()) {
      // no available in 2K sequences, just get max
      sql = ExternalizedQuery.getSql("LEGACYD.MAXSEQ");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", country);
      query.setParameter("CMR_NO", cmrNo);
      Integer max = query.getSingleResult(Integer.class);
      if (max != null) {
        sequences = Collections.singletonList(max);
      }
    }
    queue.addAll(sequences);
    return queue;
  }

  private List<String> checkIfSeqExistOnRDC(EntityManager entityManager, String cmrNo, String cntry) {
    LOG.debug("Searching for DATA_RDC records for Legacy Processing ");
    String sql = ExternalizedQuery.getSql("GET.RDC_SEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("CNTRY", cntry);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);
    List<String> rdcSequences = query.getResults(String.class);
    LOG.debug("RDC sequences =" + rdcSequences);
    return rdcSequences;
  }

  private void completeRecord(EntityManager entityManager, Admin admin, String cmrNo, LegacyDirectObjectContainer legacyObjects)
      throws CmrException, SQLException {
    LOG.info("Completing legacy processing for  Request " + admin.getId().getReqId());
    admin.setLockBy(null);
    admin.setLockByNm(null);
    admin.setLockInd("N");
    String isuCntry = legacyObjects.getCustomer().getId().getSofCntryCode();

    String message = "Process Dup Records For " + isuCntry + " successfully on the Legacy Database. CMR No. " + cmrNo
        + (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) ? " assigned." : " updated.");

    // add the sequences generated
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      StringBuilder seqCmt = new StringBuilder();
      for (CmrtAddr addr : legacyObjects.getAddresses()) {
        int cnt = 0;
        List<String> uses = new ArrayList<String>();
        uses = legacyObjects.getUsesBySequenceNo(addr.getId().getAddrNo());
        for (String use : uses) {
          seqCmt.append(cnt > 0 ? ", " : "");
          seqCmt.append(LegacyDirectUtil.USES.get(use));
          cnt++;
        }
        seqCmt.append(": ").append(addr.getId().getAddrNo()).append("\n");
      }
      if (seqCmt.toString().trim().length() > 0) {
        message += "\nDup CMR Sequences Generated:\n" + seqCmt.toString();
      }
      message = message.trim();
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      StringBuilder seqCmt = new StringBuilder();
      for (CmrtAddr addr : legacyObjects.getAddresses()) {
        if (addr.isForCreate()) {
          int cnt = 0;
          List<String> uses = new ArrayList<String>();
          uses = legacyObjects.getUsesBySequenceNo(addr.getId().getAddrNo());
          for (String use : uses) {
            seqCmt.append(cnt > 0 ? ", " : "");
            seqCmt.append(LegacyDirectUtil.USES.get(use));
            cnt++;
          }
          seqCmt.append(": ").append(addr.getId().getAddrNo()).append("\n");
        }
      }
      if (seqCmt.toString().trim().length() > 0) {
        message += "\nSequences Generated:\n" + seqCmt.toString();
      }
      message = message.trim();
    }
    createComment(entityManager, message, admin.getId().getReqId());
    partialCommit(entityManager);
  }
}
