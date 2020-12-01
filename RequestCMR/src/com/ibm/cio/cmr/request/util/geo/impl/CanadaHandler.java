/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
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
  private static final String[] USABLE_ADDRESSES = new String[] { "ZS01", "ZI01", "ZP01" };

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    List<FindCMRRecordModel> converted = new ArrayList<>();
    List<FindCMRRecordModel> records = source.getItems();

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      for (FindCMRRecordModel record : records) {
        if ("ZS01".equals(record.getCmrAddrTypeCode()) && StringUtils.isBlank(record.getCmrOrderBlock())) {
          record.setCmrAddrTypeCode("ZS01");
          converted.add(record);
        }
      }
    } else {
      for (FindCMRRecordModel record : records) {
        // if address is usable, process and add to converted
        if (Arrays.asList(USABLE_ADDRESSES).contains(record.getCmrAddrTypeCode())) {
          if ("ZS01".equals(record.getCmrAddrTypeCode()) && StringUtils.isBlank(record.getCmrOrderBlock())) {
            // set the address type to Install At for CreateCMR
            record.setCmrAddrTypeCode("ZS01");
          } else if ("ZS01".equals(record.getCmrAddrTypeCode()) && StringUtils.isNotBlank(record.getCmrOrderBlock())
              && record.getCmrOrderBlock().equals("90")) {
            // set the address type to HW/SW Billing At for CreateCMR
            record.setCmrAddrTypeCode("ZP01");
          } else if ("ZP01".equals(record.getCmrAddrTypeCode())) {
            // set the address type to Invoice To for CreateCMR
            record.setCmrAddrTypeCode("ZI01");
          }

          converted.add(record);
        }
      }
    }

    Collections.sort(converted);
    source.setItems(converted);
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    if (mainRecord != null && StringUtils.isBlank(mainRecord.getCmrShortName())) {
      String custName = mainRecord.getCmrName1Plain();
      if (StringUtils.isNotBlank(custName)) {
        data.setAbbrevNm(custName.length() > 20 ? custName.substring(0, 20).toUpperCase() : custName);
      }
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
    String[] parts = null;

    String name1 = admin.getMainCustNm1();
    String name2 = admin.getMainCustNm2();
    parts = splitName(name1, name2, 28, 24);
    admin.setMainCustNm1(parts[0]);
    admin.setOldCustNm1(parts[0]);
    admin.setMainCustNm2(parts[1]);
    admin.setOldCustNm2(parts[1]);

  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
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
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCollectorNo(), newData.getCollectorNameNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "BillingProcCd", "-"));
      update.setNewData(newData.getCollectorNameNo());
      update.setOldData(oldData.getCollectorNo());
      results.add(update);
    }
    // insert Customer Data here
    // Location/Province Code
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getLoc(), newData.getLocationNumber())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "LocationCode", "-"));
      update.setNewData(newData.getLocationNumber());
      update.setOldData(oldData.getLoc());
      results.add(update);
    }
    // Employee Size
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getOrgNo(), newData.getOrgNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SizeCode", "-"));
      update.setNewData(newData.getOrgNo());
      update.setOldData(oldData.getOrgNo());
      results.add(update);
    }
    // Number of Invoices
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getInvoiceSplitCd(), newData.getInvoiceSplitCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InvoiceSplitCd", "-"));
      update.setNewData(newData.getInvoiceSplitCd());
      update.setOldData(oldData.getInvoiceSplitCd());
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
    return Arrays.asList("PPSCEID", "LocalTax2", "Company", "Enterprise", "SearchTerm").contains(field);
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    return null;
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    return false;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
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
    List<String> caribNorthDistCntries = Arrays.asList("BS", "BB", "BM", "KY", "GY", "JM", "AW", "LC", "SR", "TT", "CW");
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
    if ("CA".equals(mainAddr.getLandCntry()) && !("USA".equals(data.getCustSubGrp()) || "CND".equals(data.getCustSubGrp()))) {
      if ("AB".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("01999");
      } else if ("BC".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("02999");
      } else if ("MB".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("03999");
      } else if ("NB".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("04999");
      } else if ("NL".equals(mainAddr.getStateProv()) || "NF".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("05999");
      } else if ("NT".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("06999");
      } else if ("NS".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("07999");
      } else if ("ON".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("08999");
      } else if ("PE".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("09999");
      } else if ("QC".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("10999");
      } else if ("SK".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("11999");
      } else if ("YT".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("12999");
      } else if ("NU".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("13999");
      }
    } else if (caribNorthDistCntries.contains(mainAddr.getLandCntry())) {
      data.setLocationNumber("99000");
    } else if ("USA".equals(data.getCustSubGrp())) {
      data.setLocationNumber("99999");
    }

    // set CS Branch to first 3 digits of postal code
    if (mainAddr.getPostCd() != null && mainAddr.getPostCd().length() >= 3) {
      data.setSalesTeamCd(mainAddr.getPostCd().substring(0, 3));
    }
    if (mainAddr.getCity1() != null) {
      data.setAbbrevLocn(mainAddr.getCity1().length() > 12 ? mainAddr.getCity1().substring(0, 12) : mainAddr.getCity1());
    }

    // set abbreviated name
    String name = admin.getMainCustNm1();
    if (name != null) {
      if ("OEM".equalsIgnoreCase(data.getCustSubGrp())) {
        data.setAbbrevNm(name.length() > 16 ? name.substring(0, 16).toUpperCase() + "/SWG" : name + "/SWG");
      } else {
        data.setAbbrevNm(name.length() > 20 ? name.substring(0, 20).toUpperCase() : name);
      }
    }

    entityManager.merge(data);
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

}
