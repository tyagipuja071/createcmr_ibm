/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Main {@link GEOHandler} implementation for Canada
 * 
 * @author JeffZAMORA
 *
 */
public class CanadaHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(CanadaHandler.class);
  private static final char SOLD_TO_ADDR_USE = '3';
  private Map<String, String> kunnrToAddrUseMap = new HashMap<>();
  private static final int MAX_ADDR_SEQ = 99999;

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    List<FindCMRRecordModel> converted = new ArrayList<>();
    List<FindCMRRecordModel> records = source.getItems();

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      for (FindCMRRecordModel record : records) {
        if (("ZS01".equals(record.getCmrAddrTypeCode()) && (StringUtils.isBlank(record.getCmrOrderBlock())))
            || ("ZS01".equals(record.getCmrAddrTypeCode()) && (record.getCmrOrderBlock().equals("75")))) {

          boolean isProspects = record != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(record.getCmrOrderBlock());
          if (isProspects) {
            record.setCmrAddrSeq("00001");
          }

          record.setCmrAddrTypeCode("ZS01");
          converted.add(record);
        }
      }
    } else {
      for (FindCMRRecordModel record : records) {
        String addrUse = record.getCmrAddrUse();
        if (StringUtils.isNotBlank(addrUse)) {
          if (addrUse.length() == 1) {
            addConvertedRecord(converted, record, addrUse.charAt(0));
          } else {
            handleMultipleAddrUse(addrUse, converted, record);
          }
        } else if (StringUtils.isBlank(addrUse) && !StringUtils.isEmpty(record.getExtWalletId())) {
          FindCMRRecordModel tempRecord = new FindCMRRecordModel();
          PropertyUtils.copyProperties(tempRecord, record);
          tempRecord.setCmrAddrTypeCode("PG01");
          converted.add(tempRecord);
        }
      }
    }
    Collections.sort(converted);
    source.setItems(converted);
  }

  private void handleMultipleAddrUse(String addrUse, List<FindCMRRecordModel> converted, FindCMRRecordModel record)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

    int soldToIndex = addrUse.indexOf(SOLD_TO_ADDR_USE);
    int retainOrigDataIndex = soldToIndex < 0 ? 0 : soldToIndex;

    char[] addrUses = addrUse.trim().toCharArray();
    String addrUseCopies = "";
    for (int i = 0; i < addrUses.length; i++) {
      if (i == retainOrigDataIndex) {
        addConvertedRecord(converted, record, addrUses[i]);
      } else {
        addrUseCopies += addrUses[i];
      }
    }
    kunnrToAddrUseMap.put(record.getCmrSapNumber(), addrUseCopies);
  }

  private void addConvertedRecord(List<FindCMRRecordModel> converted, FindCMRRecordModel record, char addrUseCh)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    FindCMRRecordModel tempRecord = new FindCMRRecordModel();
    PropertyUtils.copyProperties(tempRecord, record);

    tempRecord.setCmrAddrTypeCode(getUIAddrTypeFromAddrUse(Character.toString(addrUseCh)));
    converted.add(tempRecord);
  }

  private String getUIAddrTypeFromAddrUse(String addrUse) {
    String addrType = "";
    switch (addrUse) {
    case "2":
      addrType = "ZP02";
      break;
    case "3":
      addrType = "ZS01";
      break;
    case "4":
      addrType = "ZD01";
      break;
    case "5":
      addrType = "ZI01";
      break;
    case "6":
      addrType = "ZD02";
      break;
    case "7":
      addrType = "ZP08";
      break;
    case "A":
      addrType = "ZP03";
      break;
    case "B":
      addrType = "ZP04";
      break;
    case "D":
      addrType = "ZP05";
      break;
    case "E":
      addrType = "ZE01";
      break;
    case "L":
      addrType = "ZP06";
      break;
    case "M":
      addrType = "ZP01";
      break;
    case "P":
      addrType = "ZP09";
      break;
    case "R":
      addrType = "ZP07";
      break;
    }
    return addrType;
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    String efc = mainRecord.getCmrEstabFnInd();
    if (StringUtils.isNotBlank(efc) && ArrayUtils.contains(new String[] { "8", "R", "Z", "7", "P" }, efc)) {
      data.setIccTaxExemptStatus(efc);
    }
    data.setTaxCd1(efc);

    data.setSalesBusOffCd(mainRecord.getCmrSellBoNum());
    data.setInstallBranchOff(mainRecord.getCmrInstlBoNum());
    data.setAdminDeptCd(mainRecord.getCmrAccRecvBo());
    data.setContactName1(mainRecord.getCmrPurOrdNo());
    data.setSectorCd(mainRecord.getCmrTaxExemptReas());
    data.setTaxPayerCustCd(mainRecord.getCmrLicNo());
    data.setVatExempt("X".equalsIgnoreCase(mainRecord.getCmrTaxExInd()) ? "Y" : "N");
    data.setTaxCd3("NO_VALUE_RETRIEVED".equals(mainRecord.getCmrQstNo()) ? "" : mainRecord.getCmrQstNo());
    data.setVat(mainRecord.getCmrBusinessReg());
    data.setAbbrevNm(mainRecord.getCmrShortName());
    data.setAbbrevLocn(mainRecord.getCmrDataLine());
    data.setCusInvoiceCopies(mainRecord.getCmrNoInvc());
    data.setSalesTeamCd(mainRecord.getCmrEnggBoGrp());
    data.setLeasingCompanyIndc("1".equals(mainRecord.getCmrLeasingInd()) ? "Y" : "N");
    data.setMiscBillCd("CA".equals(mainRecord.getCmrLtPymntInd()) ? "Y" : "N");
    data.setCreditCd(mainRecord.getCmrCustCreditCode());
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setCustAcctType("");
      if (StringUtils.isNotBlank(mainRecord.getCmrLicNo())) {
        data.setAffiliate(mainRecord.getCmrLicNo());
      }
    } else {
      data.setCustAcctType(mainRecord.getCmrOrderBlock());
    }
    String mainRecBillFreq = mainRecord.getCmrBillPlnTyp();
    if ("YM".equals(mainRecBillFreq)) {
      data.setCollectorNameNo("1");
      data.setSizeCd(data.getCollectorNameNo());
    } else if ("YQ".equals(mainRecBillFreq)) {
      data.setCollectorNameNo("3");
      data.setSizeCd(data.getCollectorNameNo());
    } else {
      data.setCollectorNameNo("");
      data.setSizeCd(data.getCollectorNameNo());
    }
    setLocationNumber(data, mainRecord.getCmrCountryLanded(), mainRecord.getCmrState());

  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
    if (currentRecord != null) {
      String name1 = StringUtils.isEmpty(currentRecord.getCmrName1Plain()) ? "" : currentRecord.getCmrName1Plain();
      String name2 = StringUtils.isEmpty(currentRecord.getCmrName2Plain()) ? "" : currentRecord.getCmrName2Plain();

      if (name1.length() > 30) {
        name1 = name1.substring(0, 30);
      }

      if (name2.length() > 30) {
        name2 = name2.substring(0, 30);
      }

      admin.setMainCustNm1(name1);
      admin.setOldCustNm1(name1);
      admin.setMainCustNm2(name2);
      admin.setOldCustNm2(name2);
    }
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    String addrSeq = address.getId().getAddrSeq();
    if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      addrSeq = StringUtils.leftPad(addrSeq, 5, '0');
      address.getId().setAddrSeq(addrSeq);
    }

    if (StringUtils.isNotBlank(currentRecord.getCmrName3())) {
      address.setDept(currentRecord.getCmrName3());
    }

    if (StringUtils.isNotBlank(currentRecord.getCmrName4())) {
      address.setAddrTxt2(currentRecord.getCmrName4());
    }

  }

  @Override
  public int getName1Length() {
    return 30;
  }

  @Override
  public int getName2Length() {
    return 30;
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      admin.setDelInd(null);
    }
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;

    // PPS CEID
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getPpsceid(), newData.getPpsceid())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "PPSCEID", "-"));
      update.setNewData(newData.getPpsceid());
      update.setOldData(oldData.getPpsceid());
      results.add(update);
    }
    // VAD-VAD Number
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd2(), newData.getTaxCd2())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "VADNumber", "-"));
      update.setNewData(newData.getTaxCd2());
      update.setOldData(oldData.getTaxCd2());
      results.add(update);
    }
    // Sales Branch Office
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getSalesBusOffCd(), newData.getSalesBusOffCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalesBusOff", "-"));
      update.setNewData(newData.getSalesBusOffCd());
      update.setOldData(oldData.getSalesBusOffCd());
      results.add(update);
    }
    // Install Branch Office
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getInstallBranchOff(), newData.getInstallBranchOff())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InstallBranchOff", "-"));
      update.setNewData(newData.getInstallBranchOff());
      update.setOldData(oldData.getInstallBranchOff());
      results.add(update);
    }
    // Mktg Rep @ Team Number
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getRepTeamMemberNo(), newData.getRepTeamMemberNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalesSR", "-"));
      update.setNewData(newData.getRepTeamMemberNo());
      update.setOldData(oldData.getRepTeamMemberNo());
      results.add(update);
    }
    // CS Branch
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getSalesTeamCd(), newData.getSalesTeamCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CsBo", "-"));
      update.setNewData(newData.getSalesTeamCd());
      update.setOldData(oldData.getSalesTeamCd());
      results.add(update);
    }
    // Distribution Mktg Branch TODO: uncomment once DM changes in DATA_RDC is
    // done
    /*
     * if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
     * !equals(oldData.get, newData.getInvoiceDistCd())) { update = new
     * UpdatedDataModel(); update.setDataField(PageManager.getLabel(cmrCountry,
     * "DistMktgBranch", "-")); update.setNewData(newData.getInvoiceDistCd());
     * update.setOldData(oldData.get); results.add(update); }
     */
    // AR-FAAR
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getAdminDeptCd(), newData.getAdminDeptCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "MarketingARDept", "-"));
      update.setNewData(newData.getAdminDeptCd());
      update.setOldData(oldData.getAdminDeptCd());
      results.add(update);
    }
    // Credit Code
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCreditCd(), newData.getCreditCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CreditCd", "-"));
      update.setNewData(newData.getCreditCd());
      update.setOldData(oldData.getCreditCd());
      results.add(update);
    }
    // S/W Billing Frequency
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getSizeCd(), newData.getSizeCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "BillingProcCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSizeCd(), "BillingProcCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSizeCd(), "BillingProcCd", cmrCountry));
      results.add(update);
    }

    // insert Customer Data here
    // Location/Province Code
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getLocationNumber(), newData.getLocationNumber())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "LocationCode", "-"));
      update.setNewData(newData.getLocationNumber());
      update.setOldData(oldData.getLocationNumber());
      results.add(update);
    }
    // Number of Invoices
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCusInvoiceCopies(), newData.getCusInvoiceCopies())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InvoiceSplitCd", "-"));
      update.setNewData(newData.getCusInvoiceCopies());
      update.setOldData(oldData.getCusInvoiceCopies());
      results.add(update);
    }

    // Purchase Order Number
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getContactName1(), newData.getContactName1())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "PurchaseOrdNo", "-"));
      update.setNewData(newData.getContactName1());
      update.setOldData(oldData.getContactName1());
      results.add(update);
    }

    // Late Payment Indicator
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getMiscBillCd(), newData.getMiscBillCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "MiscBillCode", "-"));
      update.setNewData(newData.getMiscBillCd());
      update.setOldData(oldData.getMiscBillCd());
      results.add(update);
    }

    // Order Block Code
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustAcctType(), newData.getCustAcctType())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OrderBlock", "-"));
      update.setNewData(newData.getCustAcctType());
      update.setOldData(oldData.getCustAcctType());
      results.add(update);
    }
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    return false;
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    setAddressRelatedData(entityManager, admin, data, null);
    updateAddrCustNm1(entityManager, StringUtils.EMPTY, data.getId().getReqId());
    if (StringUtils.isNotBlank(data.getCollectorNameNo())) {
      data.setSizeCd(data.getCollectorNameNo());
    } else {
      data.setSizeCd("");
    }
    if (StringUtils.isBlank(data.getVat())){
      data.setVatInd("N");
    } else if (StringUtils.isNotBlank(data.getVat())){
      data.setVatInd("T");
    }  
    
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    if (!"ZS01".equals(addr.getId().getAddrType())) {
      // only update for main address
      return;
    }

    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);

    AdminPK apk = new AdminPK();
    apk.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, apk);

    setAddressRelatedData(entityManager, admin, data, addr);
  }

  @Override
  public boolean customerNamesOnAddress() {
    return false;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return true;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList("PPSCEID", "LocalTax2", "Company", "Enterprise", "SearchTerm", "SitePartyID", "TransportZone").contains(field);
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      Long reqId = data.getId().getReqId();
      List<Addr> addresses = getAddresses(entityManager, reqId);
      for (Addr addr : addresses) {
        String addrUse = kunnrToAddrUseMap.get(addr.getSapNo());
        if (StringUtils.isNotBlank(addrUse)) {
          saveAddrCopies(entityManager, addr, addrUse, reqId, data.getCmrIssuingCntry());
        }
      }
    }
  }

  private void saveAddrCopies(EntityManager entityManager, Addr addrToCopy, String addrUse, Long reqId, String cmrIssuingCntry)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

    char[] addrUses = addrUse.trim().toCharArray();
    // Set import indicator to N to create new Address copy
    for (char addrUseCh : addrUses) {
      String addrType = getUIAddrTypeFromAddrUse(Character.toString(addrUseCh));
      String addrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);

      AddrPK newAddrId = new AddrPK();
      newAddrId.setAddrType(addrType);
      newAddrId.setAddrSeq(addrSeq);
      newAddrId.setReqId(reqId);

      Addr newAddr = new Addr();
      PropertyUtils.copyProperties(newAddr, addrToCopy);
      newAddr.setId(newAddrId);
      newAddr.setImportInd("Y");
      newAddr.setSapNo("");
      newAddr.setIerpSitePrtyId("");

      entityManager.persist(newAddr);
      entityManager.flush();
    }
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("LAND_CNTRY", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "DEPT", "STATE_PROV", "CITY2", "POST_CD", "CUST_PHONE", "PO_BOX",
        "PO_BOX_CITY"));

    return fields;
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    return false;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return true;
  }

  /* new functions for Canada only */

  /**
   * Sets the DATA field values that depend on the main address like Location
   * No, etc
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @param zs01
   */
  private void setAddressRelatedData(EntityManager entityManager, Admin admin, Data data, Addr zs01) {
    Addr mainAddr = zs01;

    if (mainAddr == null) {
      // reuse italy's ZS01
      String sql = ExternalizedQuery.getSql("ITALY.GETINSTALLING");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", data.getId().getReqId());
      mainAddr = query.getSingleResult(Addr.class);
    }
    if (mainAddr == null) {
      return;
    }

    // set preferred language to F for Quebec
    if ("QC".equals(mainAddr.getStateProv())) {
      data.setCustPrefLang("F");
    }

    // set location number based on state/prov
    setLocationNumber(data, mainAddr.getLandCntry(), mainAddr.getStateProv());

    // set CS Branch to first 3 digits of postal code
    if (CmrConstants.CUSTGRP_CROSS.equals(data.getCustGrp()) && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setSalesTeamCd("000");
    } else {
      LOG.debug("Setting CSBranch via postal code-lov mapping...");
      if (mainAddr.getPostCd() != null && mainAddr.getPostCd().length() >= 3) {
        List<Object[]> results = new ArrayList<Object[]>();
        String sql = ExternalizedQuery.getSql("QUERY.GET.CA.CSBRANCH.LOVTXT");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("CMR_ISSUING_CNTRY", data.getCmrIssuingCntry());
        query.setParameter("CD", mainAddr.getPostCd().substring(0, 3));
        results = query.getResults();
        if (results != null && !results.isEmpty()) {
          Object[] sResult = results.get(0);
          String csBranch = sResult[0].toString();
          LOG.debug("CSBranch : " + csBranch);
          data.setSalesTeamCd(csBranch);
        }
      }
    }

    if (mainAddr.getCity1() != null) {
      data.setAbbrevLocn(mainAddr.getCity1().length() > 12 ? mainAddr.getCity1().substring(0, 12) : mainAddr.getCity1());
    }

    // set abbreviated name
    String name = admin.getMainCustNm1();
    if (name != null) {
      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        if ("OEM".equalsIgnoreCase(data.getCustSubGrp())) {
          data.setAbbrevNm(name.length() > 16 ? name.substring(0, 16).toUpperCase() + "/SWG" : name + "/SWG");
        } else {
          data.setAbbrevNm(name.length() > 20 ? name.substring(0, 20).toUpperCase() : name);
        }
      }
    }

    entityManager.merge(data);
  }

  private void setLocationNumber(Data data, String landedCntry, String stateProv) {
    List<String> caribNorthDistCntries = Arrays.asList("AG", "AI", "AW", "BS", "BB", "BM", "BQ", "BV", "CW", "DM", "DO", "GD", "GP", "GY", "HT", "KN",
        "KY", "JM", "LC", "MQ", "MS", "PR", "SR", "SX", "TC", "TT", "VC", "VG");

    if ("CA".equals(landedCntry) && !("USA".equals(data.getCustSubGrp()) || "CND".equals(data.getCustSubGrp()))) {
      if ("AB".equals(stateProv)) {
        data.setLocationNumber("01999");
      } else if ("BC".equals(stateProv)) {
        data.setLocationNumber("02999");
      } else if ("MB".equals(stateProv)) {
        data.setLocationNumber("03999");
      } else if ("NB".equals(stateProv)) {
        data.setLocationNumber("04999");
      } else if ("NL".equals(stateProv) || "NF".equals(stateProv)) {
        data.setLocationNumber("05999");
      } else if ("NT".equals(stateProv)) {
        data.setLocationNumber("06999");
      } else if ("NS".equals(stateProv)) {
        data.setLocationNumber("07999");
      } else if ("ON".equals(stateProv)) {
        data.setLocationNumber("08999");
      } else if ("PE".equals(stateProv)) {
        data.setLocationNumber("09999");
      } else if ("QC".equals(stateProv)) {
        data.setLocationNumber("10999");
      } else if ("SK".equals(stateProv)) {
        data.setLocationNumber("11999");
      } else if ("YT".equals(stateProv)) {
        data.setLocationNumber("12999");
      } else if ("NU".equals(stateProv)) {
        data.setLocationNumber("13999");
      }
    } else if (caribNorthDistCntries.contains(landedCntry)) {
      if (("AG").equals(landedCntry)) {
        data.setLocationNumber("AG000");
      } else if (("AI").equals(landedCntry)) {
        data.setLocationNumber("AI000");
      } else if (("AW").equals(landedCntry)) {
        data.setLocationNumber("AW000");
      } else if (("BS").equals(landedCntry)) {
        data.setLocationNumber("BS000");
      } else if (("BB").equals(landedCntry)) {
        data.setLocationNumber("BB000");
      } else if (("BM").equals(landedCntry)) {
        data.setLocationNumber("BM000");
      } else if (("BQ").equals(landedCntry)) {
        data.setLocationNumber("BQ000");
      } else if (("BV").equals(landedCntry)) {
        data.setLocationNumber("BV000");
      } else if (("CW").equals(landedCntry)) {
        data.setLocationNumber("CW000");
      } else if (("DM").equals(landedCntry)) {
        data.setLocationNumber("DM000");
      } else if (("DO").equals(landedCntry)) {
        data.setLocationNumber("DO000");
      } else if (("GD").equals(landedCntry)) {
        data.setLocationNumber("GD000");
      } else if (("GP").equals(landedCntry)) {
        data.setLocationNumber("GP000");
      } else if (("GY").equals(landedCntry)) {
        data.setLocationNumber("GY000");
      } else if (("HT").equals(landedCntry)) {
        data.setLocationNumber("HT000");
      } else if (("KN").equals(landedCntry)) {
        data.setLocationNumber("KN000");
      } else if (("KY").equals(landedCntry)) {
        data.setLocationNumber("KY000");
      } else if (("JM").equals(landedCntry)) {
        data.setLocationNumber("JM000");
      } else if (("LC").equals(landedCntry)) {
        data.setLocationNumber("LC000");
      } else if (("MQ").equals(landedCntry)) {
        data.setLocationNumber("MQ000");
      } else if (("MS").equals(landedCntry)) {
        data.setLocationNumber("MS000");
      } else if (("PR").equals(landedCntry)) {
        data.setLocationNumber("PR000");
      } else if (("SR").equals(landedCntry)) {
        data.setLocationNumber("SR000");
      } else if (("SX").equals(landedCntry)) {
        data.setLocationNumber("SX000");
      } else if (("TC").equals(landedCntry)) {
        data.setLocationNumber("TC000");
      } else if (("TT").equals(landedCntry)) {
        data.setLocationNumber("TT000");
      } else if (("VC").equals(landedCntry)) {
        data.setLocationNumber("VC000");
      } else if (("VG").equals(landedCntry)) {
        data.setLocationNumber("VG000");
      }
    } else if ("USA".equals(data.getCustSubGrp())) {
      data.setLocationNumber("99999");
    }
  }

  /**
   * Checks absolute equality between the strings
   * 
   * @param val1
   * @param val2
   * @return
   */
  private boolean equals(String val1, String val2) {
    if (val1 == null && val2 != null) {
      return StringUtils.isEmpty(val2.trim());
    }
    if (val1 != null && val2 == null) {
      return StringUtils.isEmpty(val1.trim());
    }
    if (val1 == null && val2 == null) {
      return true;
    }
    return val1.trim().equals(val2.trim());
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = "";

    if (!StringUtils.isEmpty(addrType)) {
      if (isMultiAddrType(addrType)) {
        newAddrSeq = getSeqForMultiAddress(entityManager, addrType, reqId);
      } else {
        newAddrSeq = getAvailAddrSeqNumInclRdc(entityManager, reqId);
      }
    }
    return newAddrSeq;
  }

  private String getSeqForMultiAddress(EntityManager entityManager, String addrType, long reqId) {
    List<Addr> addrs = getAddressByType(entityManager, addrType, reqId);
    int addrCount = addrs.size();
    if (addrCount == 0) {
      return getAvailAddrSeqNumInclRdc(entityManager, reqId);
    }

    String mainAddrSeq = addrs.get(0).getId().getAddrSeq();

    if (mainAddrSeq.length() > 5) {
      mainAddrSeq = mainAddrSeq.substring(0, 5);
    }

    String addressUse = addrType.equals("ZP01") ? "M" : "2";

    Set<String> existingAddrSeqSet = getExistingAddrSeqInclRdc(entityManager, reqId);
    String multiAddrSeq = mainAddrSeq + "-" + String.format("%02d", addrCount) + "-" + addressUse;
    if (existingAddrSeqSet.contains(multiAddrSeq)) {
      while (existingAddrSeqSet.contains(multiAddrSeq)) {
        multiAddrSeq = mainAddrSeq + "-" + String.format("%02d", ++addrCount) + "-" + addressUse;
      }
    }
    return multiAddrSeq;
  }

  private List<Addr> getAddressByType(EntityManager entityManager, String addrType, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GET.BYTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    List<Addr> addrList = query.getResults(Addr.class);
    return addrList;
  }

  private String getAvailAddrSeqNumInclRdc(EntityManager entityManager, long reqId) {
    Set<String> existingAddrSeqSet = getExistingAddrSeqInclRdc(entityManager, reqId);
    int candidateSeqNum = 1;
    int availSeqNum = 0;
    if (existingAddrSeqSet.contains(String.format("%05d", candidateSeqNum))) {
      availSeqNum = candidateSeqNum;
      while (existingAddrSeqSet.contains(String.format("%05d", availSeqNum))) {
        availSeqNum++;
        if (availSeqNum > MAX_ADDR_SEQ) {
          availSeqNum = 1;
        }
      }
    } else {
      availSeqNum = candidateSeqNum;
    }

    return String.format("%05d", availSeqNum);
  }

  private Set<String> getExistingAddrSeqInclRdc(EntityManager entityManager, long reqId) {
    DataPK pk = new DataPK();
    pk.setReqId(reqId);
    Data data = entityManager.find(Data.class, pk);

    String cmrNo = data.getCmrNo();
    Set<String> allAddrSeqFromAddr = getAllSavedSeqFromAddr(entityManager, reqId);
    Set<String> allAddrSeqFromRdc = getAllSavedSeqFromRdc(entityManager, cmrNo);

    Set<String> mergedAddrSet = new HashSet<>();
    mergedAddrSet.addAll(allAddrSeqFromAddr);
    mergedAddrSet.addAll(allAddrSeqFromRdc);

    return mergedAddrSet;
  }

  private Set<String> getAllSavedSeqFromAddr(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("CA.GET.ADDRSEQ.BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<String> results = query.getResults(String.class);

    Set<String> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(results);

    return addrSeqSet;
  }

  private Set<String> getAllSavedSeqFromRdc(EntityManager entityManager, String cmrNo) {
    String sql = ExternalizedQuery.getSql("CA.GET.KNA1_ZZKV_SEQNO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KATR6", SystemLocation.CANADA);
    query.setParameter("ZZKV_CUSNO", cmrNo);

    List<String> resultsRDC = query.getResults(String.class);
    Set<String> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(resultsRDC);

    return addrSeqSet;
  }

  private boolean isMultiAddrType(String addrType) {
    if ("ZP01".equals(addrType) || "ZP02".equals(addrType)) {
      return true;
    }
    return false;
  }

  @Override
  public List<String> getDataFieldsForUpdate(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "ENTERPRISE", "SEARCH_TERM",
        "ISIC_CD", "SUB_INDUSTRY_CD", "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC",
        "GEO_LOCATION_CD", "DUNS_NO", "CUST_ACCT_TYP", "PPSCEID", "SENSITIVE_FLAG", "CMR_NO", "CMR_OWNER", "INAC_TYPE", "SALES_BO_CD", "VAT_EXEMPT",
        "CUST_CLASS", "ABBREV_LOCN", "INSTALL_BRANCH_OFF", "ADMIN_DEPT_CD", "TAX_CD2", "INVOICE_DISTRIBUTION_CD", "SALES_TEAM_CD",
        "REP_TEAM_MEMBER_NO", "COLLECTOR_NO", "LOCN_NO", "CUST_INVOICE_COPIES", "TAX_CD1", "TAX_CD3", "TAX_PAYER_CUST_CD", "LEASING_COMP_INDC",
        "CONTACT_NAME1", "MISC_BILL_CD", "CREDIT_CD", "MAIN_CUST_NM1", "MAIN_CUST_NM2", "SIZE_CD"));
    return fields;
  }

  public String getReqType(EntityManager entityManager, long reqId) {
    String reqType = "";
    String sql = ExternalizedQuery.getSql("ADMIN.GETREQTYPE.CA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      reqType = results.get(0);
    }
    return reqType;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newAddrSeq = "";
    newAddrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    return newAddrSeq;
  }

  private void updateAddrCustNm1(EntityManager entityManager, String custName, long reqId) throws Exception {
    String custNm1 = custName != null ? custName : "";

    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("ADDR.UPDATE.CUSTNM1.CA"));
    query.setParameter("CUST_NM1", custNm1);
    query.setParameter("REQ_ID", reqId);
    query.executeSql();
  }

  private List<Addr> getAddresses(EntityManager entityManager, Long reqId) {
    List<Addr> addresses = null;
    String sql = ExternalizedQuery.getSql("DR.GET.ADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    addresses = query.getResults(Addr.class);
    return addresses;
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    List<String> validISU = getExcelDropdownValues(book.getSheet("Control"), 2);
    List<String> validCTC = getExcelDropdownValues(book.getSheet("Control"), 3);

    XSSFSheet sheet = book.getSheet("Data");
    if (sheet != null) {
      TemplateValidation error = new TemplateValidation("Data");
      XSSFRow row = null;

      for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
        row = sheet.getRow(rowIndex);
        if (row != null) {
          // Validate CMR No
          String cmrNo = validateColValFromCell(row.getCell(0));
          if (StringUtils.isBlank(cmrNo)) {
            error.addError(rowIndex + 1, "<br>CMR No.", "CMR Number is required.");
          }

          // Validate ISU
          String isuCd = validateColValFromCell(row.getCell(8));
          if (StringUtils.isNotBlank(isuCd) && !validISU.contains(isuCd)) {
            error.addError(rowIndex + 1, "<br>ISU", "Invalid ISU value.");
          }

          // Validate CTC
          String ctc = validateColValFromCell(row.getCell(9));
          if (StringUtils.isNotBlank(ctc) && !validCTC.contains(ctc)) {
            error.addError(rowIndex + 1, "<br>CTC", "Invalid CTC value.");
          }
        }
      }

      if (error.hasErrors()) {
        validations.add(error);
      }
    }
  }

  private List<String> getExcelDropdownValues(XSSFSheet sheet, int col) {
    List<String> dropDownValues = new ArrayList<String>();

    if (sheet != null) {
      XSSFRow sheetRow = null;
      XSSFCell rowCell = null;
      int rowIndex = 0;

      for (Row row : sheet) {
        sheetRow = (XSSFRow) row;
        if (rowIndex > 0) {
          rowCell = sheetRow.getCell(col);
          String ddCellVal = validateColValFromCell(rowCell);
          if (StringUtils.isNotBlank(ddCellVal)) {
            dropDownValues.add(ddCellVal);
          } else {
            return dropDownValues;
          }
        }
        rowIndex++;
      }
    }

    return dropDownValues;
  }

}
