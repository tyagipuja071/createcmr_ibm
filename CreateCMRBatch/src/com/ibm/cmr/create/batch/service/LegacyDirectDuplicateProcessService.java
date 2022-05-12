package com.ibm.cmr.create.batch.service;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtAddrPK;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.CmrtCustExtPK;
import com.ibm.cio.cmr.request.entity.CmrtCustPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.impl.SOFMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.TransformerManager;

public class LegacyDirectDuplicateProcessService extends LegacyDirectService {

  private static final Logger LOG = Logger.getLogger(LegacyDirectDuplicateProcessService.class);
  public static final String LEGACY_STATUS_ACTIVE = "A";
  LegacyDirectService legacyService = new LegacyDirectService();

  protected void processDupCreate(EntityManager entityManager, Admin admin, CMRRequestContainer cmrObjects) throws Exception {
    LOG.debug("Started Create duplicate CMR processing of Request " + admin.getId().getReqId());
    Data data = cmrObjects.getData();
    String parCountry = data.getCmrIssuingCntry();
    LegacyDirectObjectContainer legacyDupObjects = mapRequestDataForDupCreate(entityManager, cmrObjects);
    // finally persist all data
    CmrtCust legacyCust = legacyService.initEmpty(CmrtCust.class);
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
    // reverting back to issuing cntry for further processing
    data.setCmrIssuingCntry(parCountry);
  }

  private LegacyDirectObjectContainer mapRequestDataForDupCreate(EntityManager entityManager, CMRRequestContainer cmrObjects) throws Exception {

    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();
    String cmrNo = data.getCmrNo();
    String cntry = data.getCmrIssuingCntry();
    MessageTransformer parentTransformer = TransformerManager.getTransformer(cntry);
    String targetCountry = null;
    if (parentTransformer != null) {
      targetCountry = parentTransformer.getGmllcDupCreation(data);
    }
    targetCountry = "NA".equals(targetCountry) ? data.getCmrIssuingCntry() : targetCountry;
    // Process using target country mappings
    MessageTransformer transformer = TransformerManager.getTransformer(targetCountry);

    legacyObjects.setCustomerNo(cmrNo);
    legacyObjects.setSofCntryCd(targetCountry);

    LOG.debug("CMR No. " + cmrNo + " generated and assigned.");
    CmrtCust cust = legacyService.initEmpty(CmrtCust.class);

    // default mapping for DATA and CMRTCUST
    LOG.debug("Mapping default Data values..");
    CmrtCustPK custPk = new CmrtCustPK();
    custPk.setCustomerNo(cmrNo);
    custPk.setSofCntryCode(targetCountry);
    cust.setId(custPk);
    cust.setRealCtyCd(targetCountry);
    cust.setStatus(LEGACY_STATUS_ACTIVE);
    cust.setAbbrevNm(data.getAbbrevNm());
    cust.setAbbrevLocn(data.getAbbrevLocn());
    cust.setLocNo(data.getLocationNumber());
    cust.setEconomicCd(data.getEconomicCd());
    String legacyLangCdMapValue = legacyService.getLangCdLegacyMapping(entityManager, data, targetCountry);
    cust.setLangCd(!StringUtils.isEmpty(legacyLangCdMapValue) ? legacyLangCdMapValue : "");
    cust.setMrcCd(data.getMrcCd());
    cust.setModeOfPayment(data.getModeOfPayment());
    cust.setIsuCd(
        (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "") + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : ""));
    cust.setCreditCd(data.getCreditCd());
    cust.setTaxCd(data.getSpecialTaxCd());

    cust.setSalesRepNo(data.getRepTeamMemberNo());
    cust.setSalesGroupRep(data.getSalesTeamCd());

    if (!StringUtils.isEmpty(data.getEnterprise())) {
      cust.setEnterpriseNo(data.getEnterprise());
    }
    cust.setCeBo(data.getEngineeringBo());
    cust.setIbo((!StringUtils.isEmpty(data.getSalesBusOffCd()) ? data.getSalesBusOffCd() : ""));
    cust.setSbo((!StringUtils.isEmpty(data.getSalesBusOffCd()) ? data.getSalesBusOffCd() : ""));
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
    dummyQueue.setCmrIssuingCntry(targetCountry);
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
    legacyService.capsAndFillNulls(cust, true);
    legacyObjects.setCustomer(cust);

    CmrtAddr legacyAddr = null;
    CmrtAddrPK legacyAddrPk = null;

    String sofKey = null;
    String addressUse = null;

    int seqNo = 1;
    // int nextSeq = 7;

    LOG.debug("Mapping default address values..");
    if (transformer != null) {
      for (Addr addr : cmrObjects.getAddresses()) {

        // detach the addr, no updates needed here for now
        entityManager.detach(addr);

        if (transformer.skipLegacyAddressData(entityManager, cmrObjects, addr, true)) {
          LOG.debug("Skipping legacy create for address :" + addr.getId().getAddrType());
          continue;
        }

        dummyHandler.addrData = addr;
        dummyQueue.setReqStatus(seqNo == 1 ? MQMsgConstants.REQ_STATUS_NEW : "COM" + (seqNo - 1));
        legacyAddr = legacyService.initEmpty(CmrtAddr.class);
        legacyAddrPk = new CmrtAddrPK();
        legacyAddrPk.setCustomerNo(cmrNo);
        legacyAddrPk.setSofCntryCode(targetCountry);
        String newSeqNo = StringUtils.leftPad(Integer.toString(seqNo), 5, '0');

        if ("00001".equals(addr.getId().getAddrSeq()))
          legacyAddrPk.setAddrNo(newSeqNo);
        else
          legacyAddrPk.setAddrNo(addr.getId().getAddrSeq());

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
          legacyAddr.setAddrPhone(addr.getCustPhone());
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
        legacyService.capsAndFillNulls(legacyAddr, true);
        legacyObjects.addAddress(legacyAddr);

        // parse each address use
        addressUse = transformer.getAddressUse(addr);

        LegacyDirectService.modifyAddrUseFields(legacyAddr.getId().getAddrNo(), addressUse, legacyAddr);

        // update the seqno of the original addr record
        if ("00001".equals(addr.getId().getAddrSeq())) {
          legacyService.updateAddrSeq(entityManager, admin.getId().getReqId(), addr.getId().getAddrType(), addr.getId().getAddrSeq(),
              legacyAddr.getId().getAddrNo() + "", null, legacyAddr.isForSharedSeq());
          seqNo++;
        }
      }

      CmrtCustExt custExt = null;
      CmrtCustExtPK custExtPk = null;

      boolean isCustExt = transformer.hasCmrtCustExt();
      if (isCustExt) {
        LOG.debug("Mapping default Data values with Legacy CmrtCustExt table.....");
        // Initialize the object
        custExt = legacyService.initEmpty(CmrtCustExt.class);
        // default mapping for ADDR and CMRTCEXT
        custExtPk = new CmrtCustExtPK();
        custExtPk.setCustomerNo(cmrNo);
        custExtPk.setSofCntryCode(targetCountry);
        custExt.setId(custExtPk);

        if (transformer != null) {
          transformer.transformLegacyCustomerExtData(entityManager, dummyHandler, custExt, cmrObjects);
        }
        custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
        custExt.setAeciSubDt(SystemUtil.getDummyDefaultDate());
        legacyObjects.setCustomerExt(custExt);
      }
      transformer.transformOtherData(entityManager, legacyObjects, cmrObjects);
      // Modify the request values for the target country specific changes
      parentTransformer.transformLegacyDataForDupCreation(entityManager, legacyObjects, cmrObjects);

    }

    return legacyObjects;
  }

}
