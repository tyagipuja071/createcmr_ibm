/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.ApprovalReq;
import com.ibm.cio.cmr.request.entity.ApprovalReqPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.DefaultApprovalRecipients;
import com.ibm.cio.cmr.request.entity.DefaultApprovals;
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
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.wtaas.WtaasRecord;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for KR
 * 
 * @author Paul
 * 
 */
public class KRHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(KRHandler.class);
  private static final boolean RETRIEVE_INVALID_CUSTOMERS = true;

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();
  protected WtaasRecord currentRecord;
  static {

    LANDED_CNTRY_MAP.put(SystemLocation.KOREA, "KR");

  }

  private static final String[] SKIP_ON_SUMMARY_UPDATE_FIELDS = { "AbbrevLocation", "ContactName1", "RestrictTo", "Phone1", "ModeOfPayment",
      "SalRepNameNo", "MrcCd", "OriginatorNo", "CommercialFinanced", "ContactName2", "CreditCd", "ContactName3", "CustomerName1", "CustomerName2",
      "CustomerName3", "BillingPstlAddr", "CustomerName4", "DIVN", "LandedCountry", "StateProv", "City1", "City2", "StreetAddress1", "StreetAddress2",
      "PostalCode", "POBox", "transportZone", "Contact", "Department", "Floor", "POBoxCity", "Office", "TaxOffice", "CustPhone", "SAPNumber",
      "Affiliate", "Company", "CAP", "CMROwner", "CustClassCode", "LocalTax2", "SitePartyID", "Division", "POBoxPostalCode", "CustFAX", "Building",
      "County", "SpecialTaxCd", "SearchTerm" };

  private static final List<String> COUNTRIES_LIST = Arrays.asList(SystemLocation.KOREA);

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    // jira 2235
    String abbName = mainRecord.getCmrName1Plain() == null ? mainRecord.getCmrName1Plain() : mainRecord.getCmrName1Plain().trim();
    if (!StringUtils.isEmpty(abbName) && abbName.length() > 21) {
      abbName = abbName.substring(0, 21);
    }
    data.setAbbrevNm(abbName);

    String abbLoc = mainRecord.getCmrCity() == null ? mainRecord.getCmrCity() : mainRecord.getCmrCity().trim();
    if (!StringUtils.isEmpty(abbLoc) && abbLoc.length() > 12) {
      abbLoc = abbLoc.substring(0, 12);
    }
    data.setAbbrevLocn(abbLoc);

    /*
     * if (mainRecord.getCmrCountryLandedDesc() != null &&
     * (mainRecord.getCmrCountryLandedDesc().length() != 0)) {
     * data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc()); } else {
     * data.setAbbrevLocn(this.currentRecord.get(WtaasQueryKeys.Data.AbbrLoc));
     * }
     */

    data.setClientTier(mainRecord.getCmrTier() == null ? mainRecord.getCmrTier() : mainRecord.getCmrTier().trim());
    // data.setClientTier(this.currentRecord.get(WtaasQueryKeys.Data.GB_SegCode));
    // ?Representative(CEO) name in business license?
    // data.setContactName1(mainRecord.getUsCmrRestrictTo());
    // if(data.getContactName1()!= null && data.getContactName1().length() >
    // 30){
    // data.setContactName1(data.getContactName1().substring(0,30));
    // }else{
    data.setContactName1(data.getContactName1());
    // }

    // data.setContactName2(mainRecord.getCmrName2());

    // GB segment default setting
    if (StringUtils.isEmpty(mainRecord.getCmrTier()) || CmrConstants.FIND_CMR_BLANK_CLIENT_TIER.equals(mainRecord.getCmrTier())) {
      data.setClientTier(CmrConstants.CLIENT_TIER_UNASSIGNED);
    }
    data.setRepTeamMemberNo(mainRecord.getRepTeamMemberNo());
    this.setMRC(admin, data);
    data.setContactName2(data.getContactName2());
    // jira-2243: setup default values for update/import scenario Added by
    // IBM-CIC(LIU XUE)
    if (data.getCustPrefLang() == "" || data.getCustPrefLang() == null) {
      data.setCustPrefLang("3");
    }

    if (data.getInstallRep() == "" || data.getInstallRep() == null) {
      data.setInstallRep("1");
    }

    if (data.getPhone1() == "" || data.getPhone1() == null) {
      data.setPhone1("1");
    }
    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && "5K".equals(data.getIsuCd())) {
      data.setClientTier("");
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {

    address.setCity1(currentRecord.getCmrCity() == null ? currentRecord.getCmrCity() : currentRecord.getCmrCity().trim());
    address.setCity2(currentRecord.getCmrCity2() == null ? currentRecord.getCmrCity2() : currentRecord.getCmrCity2().trim());
    address.setCustNm1(currentRecord.getCmrName1Plain() == null ? currentRecord.getCmrName1Plain() : currentRecord.getCmrName1Plain().trim());
    address.setCustNm2(currentRecord.getCmrName2Plain() == null ? currentRecord.getCmrName2Plain() : currentRecord.getCmrName2Plain().trim());
    address.setCustNm3(currentRecord.getCmrIntlName1() == null ? currentRecord.getCmrIntlName1() : currentRecord.getCmrIntlName1().trim());
    address.setCustNm4(currentRecord.getCmrIntlCity1() == null ? currentRecord.getCmrIntlCity1() : currentRecord.getCmrIntlCity1().trim());
    address.setDivn(currentRecord.getCmrIntlCity2() == null ? currentRecord.getCmrIntlCity2() : currentRecord.getCmrIntlCity2().trim());
    address.setStateProv("");
    address.setAddrTxt2(
        currentRecord.getCmrStreetAddressCont() == null ? currentRecord.getCmrStreetAddressCont() : currentRecord.getCmrStreetAddressCont().trim());
    address.setTaxOffice(currentRecord.getCmrTaxOffice() == null ? currentRecord.getCmrTaxOffice() : currentRecord.getCmrTaxOffice().trim());
    address.setDept(currentRecord.getCmrDept() == null ? currentRecord.getCmrDept() : currentRecord.getCmrDept().trim());
    address.setPoBoxPostCd(
        currentRecord.getCmrPOBoxPostCode() == null ? currentRecord.getCmrPOBoxPostCode() : currentRecord.getCmrPOBoxPostCode().trim());
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setCmrOwner("IBM");
    data.setCustPrefLang("3");
    data.setInstallRep("1");
    data.setPhone1("1");

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

  /*
   * private WtaasRecord retrieveWTAASValues(FindCMRRecordModel mainRecord)
   * throws Exception { String cmrIssuingCntry = mainRecord.getCmrIssuedBy();
   * String cmrNo = mainRecord.getCmrNum();
   * 
   * try { WtaasClient client =
   * CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue(
   * "CMR_SERVICES_URL"), WtaasClient.class);
   * 
   * WtaasQueryRequest request = new WtaasQueryRequest();
   * request.setCmrNo(cmrNo); request.setCountry(cmrIssuingCntry);
   * 
   * WtaasQueryResponse response = client.executeAndWrap(WtaasClient.QUERY_ID,
   * request, WtaasQueryResponse.class); if (response == null ||
   * !response.isSuccess()) {
   * LOG.warn("Error or no response from WTAAS query."); throw new
   * CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE, new
   * Exception("Error or no response from WTAAS query.")); } if
   * ("F".equals(response.getData().get("Status"))) { LOG.warn("Customer " +
   * cmrNo + " does not exist in WTAAS."); throw new
   * CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE, new Exception("Customer " +
   * cmrNo + " does not exist in WTAAS.")); }
   * 
   * WtaasRecord record = WtaasRecord.createFrom(response); // record =
   * WtaasRecord.dummy(); return record;
   * 
   * } catch (Exception e) {
   * LOG.warn("An error has occurred during retrieval of the values.", e); throw
   * new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE, e); }
   * 
   * }
   */ @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    // remove the beginning comma
    if (addr.getCountyName() != null && addr.getCountyName().startsWith(",")) {
      String countyName = addr.getCountyName();
      addr.setCountyName(countyName.substring(1));
    }
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {

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

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getAbbrevLocn(), newData.getAbbrevLocn())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "AbbrevLocation", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getAbbrevLocn(), "AbbrevLocation", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getAbbrevLocn(), "AbbrevLocation", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getContactName1(), newData.getContactName1())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ContactName1", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getContactName1(), "ContactName1", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getContactName1(), "ContactName1", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getRestrictTo(), newData.getRestrictTo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "RestrictTo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getRestrictTo(), "RestrictTo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRestrictTo(), "RestrictTo", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getPhone1(), newData.getPhone1())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Phone1", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getPhone1(), "Phone1", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getPhone1(), "Phone1", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getModeOfPayment(), newData.getModeOfPayment())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ModeOfPayment", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getModeOfPayment(), "ModeOfPayment", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getModeOfPayment(), "ModeOfPayment", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getRepTeamMemberNo(), newData.getRepTeamMemberNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalRepNameNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getMrcCd(), newData.getMrcCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "MrcCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getMrcCd(), "MrcCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getMrcCd(), "MrcCd", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getOrgNo(), newData.getOrgNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OriginatorNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getOrgNo(), "OriginatorNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getOrgNo(), "OriginatorNo", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCommercialFinanced(), newData.getCommercialFinanced())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CommercialFinanced", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCommercialFinanced(), "CommercialFinanced", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCommercialFinanced(), "CommercialFinanced", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getContactName2(), newData.getContactName2())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ContactName2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getContactName2(), "ContactName2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getContactName2(), "ContactName2", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCreditCd(), newData.getCreditCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CreditCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCreditCd(), "CreditCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCreditCd(), "CreditCd", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getContactName3(), newData.getContactName3())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ContactName3", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getContactName3(), "ContactName3", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getContactName3(), "ContactName3", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSearchTerm(), newData.getSearchTerm())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SearchTerm", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSearchTerm(), "SearchTerm", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSearchTerm(), "SearchTerm", cmrCountry));
      results.add(update);
    }
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
      update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName3", "-"));
      update.setNewData(addr.getCustNm3());
      update.setOldData(addr.getCustNm3Old());
      results.add(update);
    }
    if (!equals(addr.getBillingPstlAddr(), addr.getBillingPstlAddrOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "BillingPstlAddr", "-"));
      update.setNewData(addr.getBillingPstlAddr());
      update.setOldData(addr.getBillingPstlAddrOld());
      results.add(update);
    }
    if (!equals(addr.getCustNm4(), addr.getCustNm4Old())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName4", "-"));
      update.setNewData(addr.getCustNm4());
      update.setOldData(addr.getCustNm4Old());
      results.add(update);
    }
    if (!equals(addr.getDivn(), addr.getDivnOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "DIVN", "-"));
      update.setNewData(addr.getDivn());
      update.setOldData(addr.getDivnOld());
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
    if (!equals(addr.getStateProv(), addr.getStateProvOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "StateProv", "-"));
      update.setNewData(addr.getStateProv());
      update.setOldData(addr.getStateProvOld());
      results.add(update);
    }
    if (!equals(addr.getCity1(), addr.getCity1Old())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "City1", "-"));
      update.setNewData(addr.getCity1());
      update.setOldData(addr.getCity1Old());
      results.add(update);
    }
    if (!equals(addr.getCity2(), addr.getCity2Old())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "City2", "-"));
      update.setNewData(addr.getCity2());
      update.setOldData(addr.getCity2Old());
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
    if (!equals(addr.getPostCd(), addr.getPostCdOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "PostalCode", "-"));
      update.setNewData(addr.getPostCd());
      update.setOldData(addr.getPostCdOld());
      results.add(update);
    }
    if (!equals(addr.getPoBox(), addr.getPoBoxOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "POBox", "-"));
      update.setNewData(addr.getPoBox());
      update.setOldData(addr.getPoBoxOld());
      results.add(update);
    }
    if (!equals(addr.getTransportZone(), addr.getTransportZoneOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "transportZone", "-"));
      update.setNewData(addr.getTransportZone());
      update.setOldData(addr.getTransportZoneOld());
      results.add(update);
    }
    if (!equals(addr.getContact(), addr.getContactOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "Contact", "-"));
      update.setNewData(addr.getContact());
      update.setOldData(addr.getContactOld());
      results.add(update);
    }
    if (!equals(addr.getDept(), addr.getDeptOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "Department", "-"));
      update.setNewData(addr.getDept());
      update.setOldData(addr.getDeptOld());
      results.add(update);
    }
    if (!equals(addr.getFloor(), addr.getFloorOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "Floor", "-"));
      update.setNewData(addr.getFloor());
      update.setOldData(addr.getFloorOld());
      results.add(update);
    }
    if (!equals(addr.getPoBoxCity(), addr.getPoBoxCityOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "POBoxCity", "-"));
      update.setNewData(addr.getPoBoxCity());
      update.setOldData(addr.getPoBoxCityOld());
      results.add(update);
    }
    if (!equals(addr.getOffice(), addr.getOfficeOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "Office", "-"));
      update.setNewData(addr.getOffice());
      update.setOldData(addr.getOfficeOld());
      results.add(update);
    }
    if (!equals(addr.getTaxOffice(), addr.getTaxOfficeOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "TaxOffice", "-"));
      update.setNewData(addr.getTaxOffice());
      update.setOldData(addr.getTaxOfficeOld());
      results.add(update);
    }
    if (!equals(addr.getCustPhone(), addr.getCustPhoneOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "CustPhone", "-"));
      update.setNewData(addr.getCustPhone());
      update.setOldData(addr.getCustPhoneOld());
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
    map.put("##CustPhone", "custPhone");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##VATExempt", "vatExempt");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##Enterprise", "enterprise");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##ParentCompanyNo", "dealerNo");
    map.put("##InstallRep", "installRep");
    map.put("##Phone1", "phone1");
    map.put("##MrcCd", "mrcCd");
    map.put("##CreditCd", "creditCd");
    map.put("##OrgNo", "orgNo");
    map.put("##BillingPstlAddr", "billingPstlAddr");
    map.put("##DIVN", "divn");
    map.put("##Contact", "contact");
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
            if (tempRec.getCmrAddrTypeCode() != null && tempRec.getCmrAddrTypeCode().equalsIgnoreCase("ZS01")) {
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

  @Override
  public ApprovalReq handleBPMANAGERApproval(EntityManager entityManager, long reqId, ApprovalReq approver, DefaultApprovals defaultApprovals,
      DefaultApprovalRecipients recipients, AppUser user, RequestEntryModel model) throws CmrException, SQLException {
    ApprovalReq theApprovalReq = saveAproval(entityManager, reqId, approver, defaultApprovals, recipients, user);

    if (theApprovalReq != null) {
      Person ibmer = null;
      String originatorIdInAdmin = getOriginatorIdInAdmin(entityManager, reqId);

      if (originatorIdInAdmin != null && !originatorIdInAdmin.isEmpty()) {
        Person ibmerManager = null;
        ibmerManager = BluePagesHelper.getPerson(originatorIdInAdmin);
        ibmer = BluePagesHelper.getPerson(BluePagesHelper.getManagerEmail(ibmerManager == null ? "" : ibmerManager.getEmployeeId()));
      } else {
        ibmer = BluePagesHelper.getPerson(BluePagesHelper.getManagerEmail(user.getUserCnum()));
      }

      if (ibmer != null && theApprovalReq != null) {

        theApprovalReq.setIntranetId(ibmer.getEmail());
        theApprovalReq.setNotesId(ibmer.getNotesEmail());
        theApprovalReq.setDisplayName(ibmer.getName());

        entityManager.merge(theApprovalReq);
        entityManager.flush();
      }

    }
    return theApprovalReq;
  }

  private ApprovalReq saveAproval(EntityManager entityManager, long reqId, ApprovalReq approver, DefaultApprovals defaultApprovals,
      DefaultApprovalRecipients recipients, AppUser user) throws CmrException, SQLException {

    long approverId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "APPROVAL_ID", "CREQCMR");
    ApprovalReqPK approverPk = new ApprovalReqPK();
    approverPk.setApprovalId(approverId);
    approver.setId(approverPk);
    approver.setReqId(reqId);
    approver.setTypId(defaultApprovals.getTypId());
    approver.setGeoCd(defaultApprovals.getGeoCd());
    approver.setIntranetId(recipients.getId().getIntranetId());
    approver.setNotesId(recipients.getNotesId());
    approver.setDisplayName(recipients.getDisplayName());
    approver.setStatus(CmrConstants.APPROVAL_PENDING_MAIL);
    approver.setCreateBy(user.getIntranetId());
    approver.setLastUpdtBy(user.getIntranetId());
    approver.setCreateTs(SystemUtil.getCurrentTimestamp());
    approver.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    approver.setRequiredIndc(CmrConstants.APPROVAL_DEFLT_REQUIRED_INDC);
    entityManager.persist(approver);
    entityManager.flush();

    return approver;
  }

  private void setAbbrevNM(Data data, String abbrevNM) {

    if (!StringUtils.isBlank(abbrevNM))
      if (abbrevNM.length() > 21)
        data.setAbbrevNm(abbrevNM.substring(0, 21));
      else
        data.setAbbrevNm(abbrevNM);
  }

  private void setMRC(Admin admin, Data data) {
    String[] arryISUCdForMRC3 = { "32", "34" };
    String isuCd = data.getIsuCd();
    Boolean mrcFlag3 = false;
    if (admin.getReqType().equals("C")) {
      data.setMrcCd("");
      if (!data.getIsuCd().isEmpty() && data.getIsuCd().length() > 0) {
        for (String s : arryISUCdForMRC3) {
          if (isuCd != null && s.equals(isuCd)) {
            mrcFlag3 = true;
          } else {
            // do nothing
          }
        }
      }
      if (mrcFlag3.equals(true)) {
        data.setMrcCd("3");
      } else if (mrcFlag3.equals(false)) {
        data.setMrcCd("2");
      }
    }
  }

  private String getOriginatorIdInAdmin(EntityManager entityManager, long reqId) {

    AdminPK adminPk = new AdminPK();
    adminPk.setReqId(reqId);
    Admin admin = entityManager.find(Admin.class, adminPk);

    String originatorIdInAdmin = null;
    originatorIdInAdmin = admin.getOriginatorId();
    return originatorIdInAdmin;
  };
}
