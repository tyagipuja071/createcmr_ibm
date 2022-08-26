/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for NL
 * 
 * @author Paul
 * 
 */
public class TWHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(TWHandler.class);
  private static final boolean RETRIEVE_INVALID_CUSTOMERS = true;

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  static {

    LANDED_CNTRY_MAP.put(SystemLocation.TAIWAN, "TW");

  }

  private static final String[] SKIP_ON_SUMMARY_UPDATE_FIELDS = { "CustomerName1", "CustomerName2", "ChinaCustomerName1", "ChinaCustomerName2",
      "StreetAddress1", "StreetAddress2", "ChinaStreetAddress1", "ChinaStreetAddress2", "PostalCode", "LandedCountry", "SAPNumber", "LocalTax1",
      "LocalTax2", "CustAcctType", "AbbrevLocation", "OriginatorNo", "CommercialFinanced", "CSBOCd", "ContactName2", "Email1", "ContactName1",
      "ContactName3", "BPName", "Email2", "BusnType", "Affiliate", "Email3", "MrcCd", "SalRepNameNo", "CollectionCd", "EngineeringBo", "SitePartyID",
      "SearchTerm", "Company", "CAP", "CMROwner", "CustClassCode", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office",
      "Floor", "Building", "County", "City2", "Department", "SpecialTaxCd", "SensitiveFlag", "StateProv", "City1", "ISU" };

  private static final List<String> COUNTRIES_LIST = Arrays.asList(SystemLocation.TAIWAN);

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {

    data.setIsuCd(mainRecord.getIsuCode() == null ? mainRecord.getIsuCode() : mainRecord.getIsuCode().trim());
    data.setDunsNo(mainRecord.getCmrDuns() == null ? mainRecord.getCmrDuns() : mainRecord.getCmrDuns().trim());
    data.setClientTier(mainRecord.getCmrTier() == null ? mainRecord.getCmrTier() : mainRecord.getCmrTier().trim());
    data.setInvoiceSplitCd(mainRecord.getInvoiceSplitCode() == null ? mainRecord.getInvoiceSplitCode() : mainRecord.getInvoiceSplitCode().trim());

    // jira 2567
    String abbName = mainRecord.getCmrName1Plain() == null ? mainRecord.getCmrName1Plain() : mainRecord.getCmrName1Plain().trim();
    if (!StringUtils.isEmpty(abbName) && abbName.length() > 21) {
      abbName = abbName.substring(0, 21);
    }
    data.setAbbrevNm(abbName);
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {

    address.setCustNm1(currentRecord.getCmrName1Plain() == null ? currentRecord.getCmrName1Plain() : currentRecord.getCmrName1Plain().trim());
    address.setCustNm2(currentRecord.getCmrName2Plain() == null ? currentRecord.getCmrName2Plain() : currentRecord.getCmrName2Plain().trim());
    address.setCustNm3(currentRecord.getCmrIntlName1() == null ? currentRecord.getCmrIntlName1()
        : currentRecord.getCmrIntlName1().replace((char) 12288, ' ').trim().replace(' ', (char) 12288));
    address.setCustNm4(currentRecord.getCmrIntlName2() == null ? currentRecord.getCmrIntlName2()
        : currentRecord.getCmrIntlName2().replace((char) 12288, ' ').trim().replace(' ', (char) 12288));

    String strAdd1 = ((currentRecord.getCmrName4() == null ? "" : currentRecord.getCmrName4().trim()) + " "
        + (currentRecord.getCmrStreetAddress() == null ? "" : currentRecord.getCmrStreetAddress().trim()) + " "
        + (currentRecord.getCmrCity2() == null ? "" : currentRecord.getCmrCity2().trim())).trim();

    String strAdd2 = ((currentRecord.getCmrCity() == null ? "" : currentRecord.getCmrCity().trim()) + " "
        + (currentRecord.getCmrCountryLanded() == null ? "" : currentRecord.getCmrCountryLanded().trim())).trim();

    splitAddress(address, strAdd1, strAdd2, 60, 60);

    address.setDept(currentRecord.getCmrIntlCity1() == null ? currentRecord.getCmrIntlCity1()
        : currentRecord.getCmrIntlCity1().replace((char) 12288, ' ').trim().replace(' ', (char) 12288));
    address.setBldg(currentRecord.getCmrIntlCity2() == null ? currentRecord.getCmrIntlCity2()
        : currentRecord.getCmrIntlCity2().replace((char) 12288, ' ').trim().replace(' ', (char) 12288));
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setCmrOwner("IBM");
  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
    String requesterId = model.getRequesterId();
    if (!StringUtils.isEmpty(requesterId)) {
      Person p = BluePagesHelper.getPerson(requesterId);
      if (p != null) {
        mv.addObject("requesterId_UID", p.getEmployeeId().substring(0, p.getEmployeeId().length() - 3));
      }
    }
    String originatorId = model.getOriginatorId();
    if (!StringUtils.isEmpty(originatorId)) {
      Person p = BluePagesHelper.getPerson(originatorId);
      if (p != null) {
        mv.addObject("originatorId_UID", p.getEmployeeId().substring(0, p.getEmployeeId().length() - 3));
      }
    }
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {

  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {

  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
    if (StringUtils.isEmpty(admin.getMainCustNm1())) {
      String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO_ZS01");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      List<Addr> addresses = query.getResults(Addr.class);
      Addr soldToAddr = new Addr();
      if (addresses != null && addresses.size() > 0) {
        soldToAddr = addresses.get(0);
      }
      if (soldToAddr != null) {
        admin.setMainCustNm1(soldToAddr.getCustNm1());
        admin.setMainCustNm2(soldToAddr.getCustNm2());
      }

    }
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {

  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    // update GeoLocCd in data_rdc table
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    List<DataRdc> records = query.getResults(DataRdc.class);
    if (records != null && records.size() > 0) {
      DataRdc rdc = records.get(0);
      if ("858".equals(data.getCmrIssuingCntry())) {
        rdc.setGeoLocCd(data.getGeoLocationCd());
      }
      entityManager.merge(rdc);
    }
    entityManager.flush();
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM4", "ADDR_TXT", "CITY1", "STATE_PROV", "POST_CD", "LAND_CNTRY"));
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {

    return true;
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;
    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getMktgDept(), newData.getMktgDept())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax1", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getMktgDept(), "LocalTax1", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getMktgDept(), "LocalTax1", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getInvoiceSplitCd(), newData.getInvoiceSplitCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getInvoiceSplitCd(), "LocalTax2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getInvoiceSplitCd(), "LocalTax2", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustAcctType(), newData.getCustAcctType())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CustAcctType", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCustAcctType(), "CustAcctType", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCustAcctType(), "CustAcctType", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getAbbrevLocn(), newData.getAbbrevLocn())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "AbbrevLocation", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getAbbrevLocn(), "AbbrevLocation", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getAbbrevLocn(), "AbbrevLocation", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getOrgNo(), newData.getOrgNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OriginatorNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getOrgNo(), "OriginatorNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getOrgNo(), "OriginatorNo", cmrCountry));
      results.add(update);
    }

    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getCommercialFinanced(),
    // newData.getCommercialFinanced())) {
    // update = new UpdatedDataModel();
    // update.setDataField(PageManager.getLabel(cmrCountry,
    // "CommercialFinanced", "-"));
    // update.setNewData(service.getCodeAndDescription(newData.getCommercialFinanced(),
    // "CommercialFinanced", cmrCountry));
    // update.setOldData(service.getCodeAndDescription(oldData.getCommercialFinanced(),
    // "CommercialFinanced", cmrCountry));
    // results.add(update);
    // }
    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getCsBo(), newData.getCsBo())) {
    // update = new UpdatedDataModel();
    // update.setDataField(PageManager.getLabel(cmrCountry, "CSBOCd", "-"));
    // update.setNewData(service.getCodeAndDescription(newData.getCsBo(),
    // "CSBOCd", cmrCountry));
    // update.setOldData(service.getCodeAndDescription(oldData.getCsBo(),
    // "CSBOCd", cmrCountry));
    // results.add(update);
    // }
    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getContactName2(), newData.getContactName2())) {
    // update = new UpdatedDataModel();
    // update.setDataField(PageManager.getLabel(cmrCountry, "ContactName2",
    // "-"));
    // update.setNewData(service.getCodeAndDescription(newData.getContactName2(),
    // "ContactName2", cmrCountry));
    // update.setOldData(service.getCodeAndDescription(oldData.getContactName2(),
    // "ContactName2", cmrCountry));
    // results.add(update);
    // }
    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getEmail1(), newData.getEmail1())) {
    // update = new UpdatedDataModel();
    // update.setDataField(PageManager.getLabel(cmrCountry, "Email1", "-"));
    // update.setNewData(service.getCodeAndDescription(newData.getEmail1(),
    // "Email1", cmrCountry));
    // update.setOldData(service.getCodeAndDescription(oldData.getEmail1(),
    // "Email1", cmrCountry));
    // results.add(update);
    // }
    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getContactName1(), newData.getContactName1())) {
    // update = new UpdatedDataModel();
    // update.setDataField(PageManager.getLabel(cmrCountry, "ContactName1",
    // "-"));
    // update.setNewData(service.getCodeAndDescription(newData.getContactName1(),
    // "ContactName1", cmrCountry));
    // update.setOldData(service.getCodeAndDescription(oldData.getContactName1(),
    // "ContactName1", cmrCountry));
    // results.add(update);
    // }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getContactName3(), newData.getContactName3())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ContactName3", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getContactName3(), "ContactName3", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getContactName3(), "ContactName3", cmrCountry));
      results.add(update);
    }
    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getBpName(), newData.getBpName())) {
    // update = new UpdatedDataModel();
    // update.setDataField(PageManager.getLabel(cmrCountry, "BPName", "-"));
    // update.setNewData(service.getCodeAndDescription(newData.getBpName(),
    // "BPName", cmrCountry));
    // update.setOldData(service.getCodeAndDescription(oldData.getBpName(),
    // "BPName", cmrCountry));
    // results.add(update);
    // }
    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getEmail2(), newData.getEmail2())) {
    // update = new UpdatedDataModel();
    // update.setDataField(PageManager.getLabel(cmrCountry, "Email2", "-"));
    // update.setNewData(service.getCodeAndDescription(newData.getEmail2(),
    // "Email2", cmrCountry));
    // update.setOldData(service.getCodeAndDescription(oldData.getEmail2(),
    // "Email2", cmrCountry));
    // results.add(update);
    // }
    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getBusnType(), newData.getBusnType())) {
    // update = new UpdatedDataModel();
    // update.setDataField(PageManager.getLabel(cmrCountry, "BusnType", "-"));
    // update.setNewData(service.getCodeAndDescription(newData.getBusnType(),
    // "BusnType", cmrCountry));
    // update.setOldData(service.getCodeAndDescription(oldData.getBusnType(),
    // "BusnType", cmrCountry));
    // results.add(update);
    // }
    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getAffiliate(), newData.getAffiliate())) {
    // update = new UpdatedDataModel();
    // update.setDataField(PageManager.getLabel(cmrCountry, "Affiliate", "-"));
    // update.setNewData(service.getCodeAndDescription(newData.getAffiliate(),
    // "Affiliate", cmrCountry));
    // update.setOldData(service.getCodeAndDescription(oldData.getAffiliate(),
    // "Affiliate", cmrCountry));
    // results.add(update);
    // }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmail3(), newData.getEmail3())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Email3", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmail3(), "Email3", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmail3(), "Email3", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getRestrictTo(), newData.getRestrictTo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "RestrictTo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getRestrictTo(), "RestrictTo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRestrictTo(), "RestrictTo", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getRepTeamMemberNo(), newData.getRepTeamMemberNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalRepNameNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCollectionCd(), newData.getCollectionCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CollectionCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCollectionCd(), "CollectionCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollectionCd(), "CollectionCd", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getEngineeringBo(), newData.getEngineeringBo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EngineeringBo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSitePartyId(), newData.getSitePartyId())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SitePartyID", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSitePartyId(), "SitePartyID", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSitePartyId(), "SitePartyID", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getGeoLocCd(), newData.getGeoLocationCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "GeoLocationCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getGeoLocationCd(), "GeoLocationCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getGeoLocCd(), "GeoLocationCode", cmrCountry));
      results.add(update);
    }
    // If Coverage expired then ignore coverage related field Update
    Boolean expiredCluster = expiredClusterForTW(newData, oldData);
    if (expiredCluster) {
      return;
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSearchTerm(), newData.getSearchTerm())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SearchTerm", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSearchTerm(), "SearchTerm", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSearchTerm(), "SearchTerm", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getMrcCd(), newData.getMrcCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "MrcCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getMrcCd(), "MrcCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getMrcCd(), "MrcCd", cmrCountry));
      results.add(update);
    }

  }

  public boolean expiredClusterForTW(Data newData, DataRdc oldData) {
    String oldCluster = oldData.getApCustClusterId();
    String newCluster = newData.getApCustClusterId();
    long reqId = oldData.getId().getReqId();
    String isuCd = oldData.getIsuCd();
    String gbSegement = oldData.getClientTier();
    String cmrIssuingCntry = oldData.getCmrIssuingCntry();
    EntityManager entityManager = JpaManager.getEntityManager();
    DataPK dataPK = new DataPK();
    dataPK.setReqId(reqId);
    Data data = entityManager.find(Data.class, dataPK);
    // new Cluster won't be empty if CMDE edits it by SuperUser Mode
    if (!StringUtils.isEmpty(oldCluster) && StringUtils.isBlank(newCluster)) {
      String sql = ExternalizedQuery.getSql("QUERY.CHECK.CLUSTER");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISSUING_CNTRY", cmrIssuingCntry);
      query.setParameter("AP_CUST_CLUSTER_ID", oldCluster);
      query.setForReadOnly(true);
      List<String> result = query.getResults(String.class);
      if ((result != null && !result.isEmpty()) && !isuCd.equalsIgnoreCase("C") || !gbSegement.equalsIgnoreCase("32")) {
        return false;
      }
      return true;
    }
    return false;
  }

  public DataRdc getAPClusterDataRdc(long reqId) {
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    EntityManager entityManager = JpaManager.getEntityManager();
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    List<DataRdc> records = query.getResults(DataRdc.class);
    if (records != null && records.size() > 0) {
      for (DataRdc oldData : records) {
        return oldData;
      }
    }
    return null;
  }

  /**
   * Checks absolute equality between the strings
   * 
   * @param val1
   * @param val2
   * @return
   */
  protected boolean equals(String val1, String val2) {
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
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {

    if (!equals(addr.getCustNm1(), addr.getCustNm1Old())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName1", "-"));
      update.setNewData(addr.getCustNm1());
      update.setOldData(addr.getCustNm1Old());
      results.add(update);
    }
    if (!equals(addr.getCustNm2(), addr.getCustNm2Old())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName2", "-"));
      update.setNewData(addr.getCustNm2());
      update.setOldData(addr.getCustNm2Old());
      results.add(update);
    }
    if (!equals(addr.getCustNm3(), addr.getCustNm3Old())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "ChinaCustomerName1", "-"));
      update.setNewData(addr.getCustNm3());
      update.setOldData(addr.getCustNm3Old());
      results.add(update);
    }
    if (!equals(addr.getCustNm4(), addr.getCustNm4Old())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "ChinaCustomerName2", "-"));
      update.setNewData(addr.getCustNm4());
      update.setOldData(addr.getCustNm4Old());
      results.add(update);
    }
    if (!equals(addr.getAddrTxt(), addr.getAddrTxtOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "StreetAddress1", "-"));
      update.setNewData(addr.getAddrTxt());
      update.setOldData(addr.getAddrTxtOld());
      results.add(update);
    }
    if (!equals(addr.getAddrTxt2(), addr.getAddrTxt2Old())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "StreetAddress2", "-"));
      update.setNewData(addr.getAddrTxt2());
      update.setOldData(addr.getAddrTxt2Old());
      results.add(update);
    }
    if (!equals(addr.getDept(), addr.getDeptOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "ChinaStreetAddress1", "-"));
      update.setNewData(addr.getDept());
      update.setOldData(addr.getDeptOld());
      results.add(update);
    }
    if (!equals(addr.getBldg(), addr.getBldgOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "ChinaStreetAddress2", "-"));
      update.setNewData(addr.getBldg());
      update.setOldData(addr.getBldgOld());
      results.add(update);
    }
    if (!equals(addr.getPostCd(), addr.getPostCdOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "PostalCode", "-"));
      update.setNewData(addr.getPostCd());
      update.setOldData(addr.getPostCdOld());
      results.add(update);
    }
    if (!equals(addr.getLandCntry(), addr.getLandCntryOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "LandedCountry", "-"));
      update.setNewData(addr.getLandCntry());
      update.setOldData(addr.getLandCntryOld());
      results.add(update);
    }
    if (!equals(addr.getSapNo(), addr.getSapNoOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "SAPNumber", "-"));
      update.setNewData(addr.getSapNo());
      update.setOldData(addr.getSapNoOld());
      results.add(update);
    }

  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    return RETRIEVE_INVALID_CUSTOMERS;
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorID", "originatorId");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##INACCode", "inacCd");
    map.put("##BPRelationType", "bpRelType");
    map.put("##VAT", "vat");
    map.put("##SitePartyID", "sitePartyId");
    map.put("##RequesterID", "requesterId");
    map.put("##Department", "dept");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##BuyingGroupID", "bgId");
    map.put("##LandedCountry", "landCntry");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##RequestReason", "reqReason");
    map.put("##SAPNumber", "sapNo");
    map.put("##CMRNumber", "cmrNo");
    map.put("##CAP", "capInd");
    map.put("##DUNS", "dunsNo");
    map.put("##CoverageID", "covId");
    map.put("##SalesBusOff", "salesBusOffCd");
    map.put("##MembLevel", "memLvl");
    map.put("##InternalDept", "ibmDeptCostCenter");
    map.put("##RequestType", "reqType");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##EngineeringBo", "engineeringBo");
    map.put("##POBox", "poBox");
    map.put("##Expedite", "expediteInd");
    map.put("##CMROwner", "cmrOwner");
    map.put("##CustomerName2", "custNm2");
    map.put("##LocalTax2", "taxCd2");
    map.put("##CustomerName1", "custNm1");
    map.put("##PostalCode", "postCd");
    map.put("##LocalTax1", "taxCd1");
    map.put("##CustomerName4", "custNm4");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##EconomicCd2", "economicCd");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##ISIC", "isicCd");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##StateProv", "stateProv");
    map.put("##City1", "city1");
    map.put("##CustLang", "custPrefLang");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
    map.put("##ClientTier", "clientTier");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##SOENumber", "soeReqNo");
    map.put("##OriginatorName", "originatorNm");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##CollectionCd", "collectionCd");
    map.put("##CustomerIdCd", "customerIdCd");
    map.put("##CustPhone", "custPhone");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##VATExempt", "vatExempt");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##Enterprise", "enterprise");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##GlobalBuyingGroupID", "gbgId");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }

  public static List<String> getDataFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD",
        "SUB_INDUSTRY_CD", "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD",
        "DUNS_NO", "ABBREV_LOCN"));
    return fields;
  }

  public static boolean isDataUpdated(Data data, DataRdc dataRdc, String cmrIssuingCntry) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;

    for (Field field : Data.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        // check if at least one of the fields is updated
        if (getDataFieldsForUpdateCheck(cmrIssuingCntry).contains(srcName)) {
          try {
            trgField = DataRdc.class.getDeclaredField(field.getName());

            field.setAccessible(true);
            trgField.setAccessible(true);

            Object srcVal = field.get(data);
            Object trgVal = trgField.get(dataRdc);

            if (String.class.equals(field.getType())) {
              String srcStringVal = (String) srcVal;
              if (srcStringVal == null) {
                srcStringVal = "";
              }
              String trgStringVal = (String) trgVal;
              if (trgStringVal == null) {
                trgStringVal = "";
              }
              if (!StringUtils.equals(srcStringVal.trim(), trgStringVal.trim())) {
                LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
                return true;
              }
            } else {
              if (!ObjectUtils.equals(srcVal, trgVal)) {
                LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
                return true;
              }
            }
          } catch (NoSuchFieldException e) {
            // noop
            continue;
          } catch (Exception e) {
            LOG.trace("General error when trying to access field.", e);
            // no stored value or field not on addr rdc, return null
            // for no
            // changes
            continue;
          }
        } else {
          continue;
        }
      }
    }

    return false;
  }

  public boolean isAddrUpdated(Addr addr, AddrRdc addrRdc, String cmrIssuingCntry) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;

    GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);

    for (Field field : Addr.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        // check if field is part of exemption list or is part of what
        // to check
        // for the handler, if specified
        if (GEOHandler.ADDRESS_FIELDS_SKIP_CHECK.contains(srcName)
            || (handler != null && handler.getAddressFieldsForUpdateCheck(cmrIssuingCntry) != null
                && !handler.getAddressFieldsForUpdateCheck(cmrIssuingCntry).contains(srcName))) {
          continue;
        }

        if ("ID".equals(srcName) || "PCSTATEMANAGER".equals(srcName) || "PCDETACHEDSTATE".equals(srcName)) {
          continue;
        }

        try {
          trgField = AddrRdc.class.getDeclaredField(field.getName());

          field.setAccessible(true);
          trgField.setAccessible(true);

          Object srcVal = field.get(addr);
          Object trgVal = trgField.get(addrRdc);

          if (String.class.equals(field.getType())) {
            String srcStringVal = (String) srcVal;
            if (srcStringVal == null) {
              srcStringVal = "";
            }
            String trgStringVal = (String) trgVal;
            if (trgStringVal == null) {
              trgStringVal = "";
            }
            if (!StringUtils.equals(srcStringVal.trim(), trgStringVal.trim())) {
              LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
              return true;
            }
          } else {
            if (!ObjectUtils.equals(srcVal, trgVal)) {
              LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
              return true;
            }
          }
        } catch (NoSuchFieldException e) {
          // noop
          continue;
        } catch (Exception e) {
          LOG.trace("General error when trying to access field.", e);
          // no stored value or field not on addr rdc, return null for
          // no
          // changes
          continue;
        }

      }
    }
    return false;
  }

  @Override
  public void doBeforeDPLCheck(EntityManager entityManager, Data data, List<Addr> addresses) throws Exception {
    // No DPL check for non-latin addresses
    for (Addr addr : addresses) {
      if (Arrays.asList("ZP02").contains(addr.getId().getAddrType())) {
        addr.setDplChkResult("N");
      }
    }
  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "SPECIAL_TAX_CD", "VAT", "ISIC_CD", "EMBARGO_CD", "COLLECTION_CD", "ABBREV_NM",
        "SENSITIVE_FLAG", "CLIENT_TIER", "COMPANY", "INAC_TYPE", "INAC_CD", "ISU_CD", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "MEM_LVL",
        "BP_REL_TYPE", "COMMERCIAL_FINANCED", "ENTERPRISE", "PHONE1", "PHONE3"));
    return fields;
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {

  }

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    // TODO Auto-generated method stub
    List<FindCMRRecordModel> recordsFromSearch = source.getItems();
    List<FindCMRRecordModel> filteredRecords = new ArrayList<>();

    if (recordsFromSearch != null && !recordsFromSearch.isEmpty() && recordsFromSearch.size() > 0) {
      doFilterAddresses(reqEntry, recordsFromSearch, filteredRecords);
      if (!filteredRecords.isEmpty() && filteredRecords.size() > 0 && filteredRecords != null) {
        source.setItems(filteredRecords);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static void doFilterAddresses(RequestEntryModel reqEntry, Object mainRecords, Object filteredRecords) {
    if (mainRecords instanceof java.util.List<?> && filteredRecords instanceof java.util.List<?>) {
      // during convertFrom
      if (reqEntry.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_UPDATE)) {
        List<FindCMRRecordModel> recordsToCheck = (List<FindCMRRecordModel>) mainRecords;
        List<FindCMRRecordModel> recordsToReturn = (List<FindCMRRecordModel>) filteredRecords;
        for (Object tempRecObj : recordsToCheck) {
          if (tempRecObj instanceof FindCMRRecordModel) {
            FindCMRRecordModel tempRec = (FindCMRRecordModel) tempRecObj;
            if (tempRec.getCmrAddrTypeCode().equalsIgnoreCase("ZD01") && tempRec.getCmrAddrSeq().equalsIgnoreCase("040")) {
              tempRec.setCmrAddrTypeCode("ZI01");
              tempRec.setCmrAddrSeq("040");
            }
            if (tempRec.getCmrAddrTypeCode().equalsIgnoreCase("ZP01") && tempRec.getCmrAddrSeq().equalsIgnoreCase("D")) {
              tempRec.setCmrAddrTypeCode("ZP02");
              tempRec.setCmrAddrSeq("D");
            }
            if (!StringUtils.isEmpty(tempRec.getCmrAddrSeq())) {
              recordsToReturn.add(tempRec);
            }
          }
        }
      }

      if (reqEntry.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_CREATE)) {
        List<FindCMRRecordModel> recordsToCheck = (List<FindCMRRecordModel>) mainRecords;
        List<FindCMRRecordModel> recordsToReturn = (List<FindCMRRecordModel>) filteredRecords;
        for (Object tempRecObj : recordsToCheck) {
          if (tempRecObj instanceof FindCMRRecordModel) {
            FindCMRRecordModel tempRec = (FindCMRRecordModel) tempRecObj;
            if (tempRec.getCmrAddrTypeCode().equalsIgnoreCase("ZS01")) {
              // RETURN ONLY THE SOLD-TO ADDRESS FOR CREATES
              recordsToReturn.add(tempRec);
            }
          }
        }
      }
    }
  }

  @Override
  public int getName1Length() {
    // TODO Auto-generated method stub
    return 30;
  }

  @Override
  public int getName2Length() {
    // TODO Auto-generated method stub
    return 30;
  }

  @Override
  public boolean customerNamesOnAddress() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public boolean useSeqNoFromImport() {
    // TODO Auto-generated method stub
    return true;
  }

}
