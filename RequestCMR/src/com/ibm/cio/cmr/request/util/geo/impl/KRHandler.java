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
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.wtaas.WtaasQueryKeys;
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

  static {

    LANDED_CNTRY_MAP.put(SystemLocation.KOREA, "KR");

  }

  private static final String[] SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Affiliate", "Company", "CAP", "CMROwner", "CustClassCode", "LocalTax2",
      "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office", "Floor", "Building", "County", "City2",
      "Department", "SpecialTaxCd", "SearchTerm", "SalRepNameNo" };

  private static final List<String> COUNTRIES_LIST = Arrays.asList(SystemLocation.KOREA);

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
	  data.setAbbrevNm(mainRecord.getCmrName1Plain());
	  
	    //String cmrIssuingCntry = data.getCmrIssuingCntry();
	    if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12 ) {
	      data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
	    }
	    else{
	    	data.setAbbrevLocn("Korea");
	    }
	   
	  // 【Representative(CEO) name in business license】
	  //data.setContactName1(mainRecord.getUsCmrRestrictTo());
//	    if(data.getContactName1()!= null && data.getContactName1().length() > 30){
//	    	data.setContactName1(data.getContactName1().substring(0,30));
//	    }else{
	    	data.setContactName1(data.getContactName1());
//	    }
	  
	    data.setContactName2(mainRecord.getCmrName2());
	  
	  //GB segment default setting
      if (StringUtils.isEmpty(mainRecord.getCmrTier()) || CmrConstants.FIND_CMR_BLANK_CLIENT_TIER.equals(mainRecord.getCmrTier())) {
          data.setClientTier(CmrConstants.CLIENT_TIER_UNASSIGNED);
        }
      data.setRepTeamMemberNo(mainRecord.getRepTeamMemberNo());
      this.setMRC(admin,data);
      data.setContactName2(data.getContactName2());
      
      //autoSetAbbrevLocnNMOnImport(admin, data, results, mainRecord);
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {

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
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {

  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {

  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {

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

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCollectionCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CollectionCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCollectionCd(), "CollectionCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollectionCd(), "CollectionCd", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getModeOfPayment(), newData.getModeOfPayment())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ModeOfPayment", "-"));
      update.setNewData(newData.getModeOfPayment());
      update.setOldData(oldData.getModeOfPayment());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd2(), newData.getTaxCd2())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getTaxCd2(), "LocalTax2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getTaxCd2(), "LocalTax2", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getEngineeringBo(), newData.getEngineeringBo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EngineeringBo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEngineeringBo(), "EngineeringBo", cmrCountry));
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
      /*
       * testing String originatorIdInAdmin =
       * getOriginatorIdInAdmin(entityManager, reqId);
       * 
       * if (originatorIdInAdmin != null && !originatorIdInAdmin.isEmpty()) {
       * Person ibmerManager = null; ibmerManager =
       * BluePagesHelper.getPerson(originatorIdInAdmin); ibmer =
       * BluePagesHelper.getPerson(BluePagesHelper.getManagerEmail(ibmerManager
       * == null ? "" : ibmerManager.getEmployeeId())); } else { ibmer =
       * BluePagesHelper.getPerson(BluePagesHelper.getManagerEmail(user.
       * getUserCnum())); }
       */
      ibmer = BluePagesHelper.getPerson("mahechi@cn.ibm.com");
      // }

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
