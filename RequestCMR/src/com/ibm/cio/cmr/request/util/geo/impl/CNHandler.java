package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.GeoContactInfo;
import com.ibm.cio.cmr.request.entity.GeoContactInfoPK;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.entity.IntlAddrPK;
import com.ibm.cio.cmr.request.entity.IntlAddrRdc;
import com.ibm.cio.cmr.request.entity.IntlAddrRdcPK;
import com.ibm.cio.cmr.request.entity.KunnrExt;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.Sadr;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.service.requestentry.GeoContactInfoService;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

public class CNHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(DEHandler.class);
  private static final String[] DE_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "LocalTax1", "LocalTax2", "SearchTerm", "SitePartyID", "Division", "POBoxCity",
      "CustFAX", "City2", "Affiliate", "Company", "INACType" };
  public static final int CN_STREET_ADD_TXT = 70;
  public static final int CN_STREET_ADD_TXT2 = 70;
  public static final int CN_CUST_NAME_1 = 70;
  public static final int CN_CUST_NAME_2 = 70;

  public static List<String> getDataFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD",
        "SUB_INDUSTRY_CD", "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD",
        "DUNS_NO"));
    return fields;
  }

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
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
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    if (CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock())) {
      data.setProspectSeqNo(mainRecord.getCmrAddrSeq());
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
    String[] name1Name2Val = getRDcName1Name2Values(currentRecord.getCmrSapNumber());
    String name1 = StringUtils.isEmpty(name1Name2Val[0]) ? "" : name1Name2Val[0].toString();
    String name2 = StringUtils.isEmpty(name1Name2Val[1]) ? "" : name1Name2Val[1].toString();
    name1Name2Val = splitName(name1, name2, 30, 40);

    admin.setMainCustNm1(name1Name2Val[0]);
    admin.setOldCustNm1(name1Name2Val[0]);
    admin.setMainCustNm2(name1Name2Val[1]);
    admin.setOldCustNm2(name1Name2Val[1]);
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    String[] parts = null;
    String name1 = currentRecord.getCmrName1Plain();
    String name2 = currentRecord.getCmrName2Plain();

    parts = splitName(name1, name2, 35, 35);
    address.setCustNm1(parts[0]);
    address.setCustNm2(parts[1]);
    address.setCustNm3(currentRecord.getCmrName3());
    address.setAddrTxt2(currentRecord.getCmrName4());

    if (!StringUtils.isEmpty(parts[2])) {
      address.setDept(parts[2]);
    }

    if (currentRecord.getCmrOrderBlock() != null && CmrConstants.LEGAL_ORDER_BLOCK.equals(currentRecord.getCmrOrderBlock())) {
      address.setPairedAddrSeq(currentRecord.getCmrAddrSeq());
    }
  }

  @Override
  public int getName1Length() {
    return 35;
  }

  @Override
  public int getName2Length() {
    return 35;
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

    if (StringUtils.isEmpty(admin.getMainCustNm1())) {
      String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO_ZS01");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      Addr soldToAddr = query.getSingleResult(Addr.class);

      if (soldToAddr != null) {
        admin.setMainCustNm1(soldToAddr.getCustNm1());
        admin.setMainCustNm2(soldToAddr.getCustNm2());
      }

    }

  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    if (data.getInacCd() != null) {
      if (data.getInacCd().matches("[a-zA-Z]{1}[a-zA-Z0-9]{3}")) {
        data.setInacType("N");
      } else if (data.getInacCd().matches("[0-9]{1}[a-zA-Z0-9]{3}")) {
        data.setInacType("I");
      } else if (data.getInacCd().equals("")) {
        data.setInacType("");
      }
    }
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      if (data.getCustSubGrp() != null && "INTSO".equals(data.getCustSubGrp()) && data.getCustClass() != null
          && CmrConstants.CUST_CLASS_85.equals(data.getCustClass())) {
        if (data.getIbmDeptCostCenter() == null || "".equals(data.getIbmDeptCostCenter())) {
          data.setIbmDeptCostCenter(CmrConstants.DEFAULT_IBM_DEPT_COST_CENTER);
        }
      } else if (data.getCustSubGrp() != null && !"INTAM".equals(data.getCustSubGrp()) && !"INTIN".equals(data.getCustSubGrp())) {
        data.setIbmDeptCostCenter("");
      }
      if (data.getAbbrevNm() == null || "".equals(data.getAbbrevNm())) {
        if (admin.getMainCustNm1() != null && admin.getMainCustNm1().length() > 30) {
          data.setAbbrevNm(admin.getMainCustNm1().substring(0, 30));
        } else {
          data.setAbbrevNm(admin.getMainCustNm1());
        }
      }
      if ("DRA".equalsIgnoreCase(admin.getReqStatus())) {
        if (data.getCustSubGrp() != null && "AQSTN".equals(data.getCustSubGrp())) {
          data.setRdcComment("Acquisition");
        } else {
          data.setRdcComment("");
        }
      }
    }

    // set inac type
    String inac = data.getInacCd();
    if (!StringUtils.isEmpty(inac)) {
      // get starting char of the inac code
      String startCd = inac.substring(0, 1);
      LOG.debug(">>> START Char of INAC CODE is " + startCd);

      if (StringUtils.isAlpha(startCd)) {
        // set type as N if alpha
        LOG.debug(">>> START Char of INAC CODE is ALPHA setting type to N");
        data.setInacType("N");
      } else if (StringUtils.isNumeric(startCd)) {
        LOG.debug(">>> START Char of INAC CODE is NUMERIC setting type to I");
        data.setInacType("I");
      } else {
        data.setInacType("");
      }
    }

    // data.setSearchTerm(data.getCovId());

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public boolean customerNamesOnAddress() {
    return true;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return false;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(DE_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    // QUERY RDC
    String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Addr> addresses = query.getResults(Addr.class);
    Addr soldToAddr = new Addr();
    List<IntlAddr> intlAddrList = new ArrayList<IntlAddr>();
    List<IntlAddrRdc> intlAddrRdcList = new ArrayList<IntlAddrRdc>();

    intlAddrList = getINTLAddrCountByReqId(entityManager, admin.getId().getReqId());
    intlAddrRdcList = getINTLAddrRdcByReqId(entityManager, admin.getId().getReqId());

    if (intlAddrList != null && intlAddrList.size() > 0) {
      for (IntlAddr intlAddr : intlAddrList) {
        entityManager.remove(intlAddr);
      }

      // remove INTL_ADDR_RDC too
      if (intlAddrRdcList != null && intlAddrRdcList.size() > 0) {
        for (IntlAddrRdc intlAddrRdc : intlAddrRdcList) {
          entityManager.remove(intlAddrRdc);
        }
      }

      entityManager.flush();
    }

    for (Addr addr : addresses) {
      String sapNo = addr.getSapNo();

      if ("ZS01".equals(addr.getId().getAddrType())) {
        soldToAddr = addr;
      }

      try {
        String spid = "";
        String cmr = "";

        if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
          spid = getRDcIerpSitePartyId(sapNo);
          KunnrExt addlAddDetail = getKunnrExtDetails(entityManager, sapNo);
          addr.setIerpSitePrtyId(spid);
          addr.setPairedAddrSeq(getCnAddrSeqById(entityManager, addr.getSapNo(), SystemConfiguration.getValue("MANDT")));

          if (addlAddDetail != null) {
            addr.setBldg(addlAddDetail.getBuilding() != null ? addlAddDetail.getBuilding() : "");
            addr.setFloor(addlAddDetail.getFloor() != null ? addlAddDetail.getFloor() : "");
            addr.setOffice(addlAddDetail.getOffice() != null ? addlAddDetail.getOffice() : "");
            addr.setDept(addlAddDetail.getDepartment() != null ? addlAddDetail.getDepartment() : "");
            data.setPrivIndc(addlAddDetail.getPrivacyInd() != null ? addlAddDetail.getPrivacyInd() : null);
          }

          cmr = data.getCmrNo();
        } else if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          addr.setIerpSitePrtyId(spid);
          cmr = admin.getModelCmrNo();
        }

        /*
         * DENNIS: AUTO IDENTIFY THE CITIES THAT WILL NOT MAP TO OUR CITY LIST
         */
        // If it does not contains white space and it is not one of the cities
        // that we know that are whole words, that means we need to convert it
        // to the value on our list.
        String city1Upper = addr.getCity1() != null ? addr.getCity1().toUpperCase() : "";
        city1Upper = city1Upper.replaceAll("[^a-zA-Z]+", "");
        String cmtMsg = "Your address city value on address type " + addr.getId().getAddrType() + ", sequence number " + addr.getId().getAddrSeq()
            + " has been changed because the city value is not on CreateCMR.";
        if (!containsWhiteSpace(addr.getCity1()) && !StringUtils.isEmpty(city1Upper) && !CmrConstants.CN_NON_SPACED_CITIES.contains(city1Upper)) {
          // 1. query code from LOV
          String cdOfUpperDesc = getCNCityCdByUpperDesc(entityManager, city1Upper);
          // 2. query desc from lov with the use of code from 1
          String enDesc = getCnCityEngDescById(entityManager, cdOfUpperDesc);
          // 3. save desc as addr.city1
          cmtMsg += "\n\nFrom: " + addr.getCity1() + "    To: " + enDesc;
          addr.setCity1(enDesc);

          if (StringUtils.isEmpty(enDesc)) {
            cmtMsg += "\n\nPlease note that you will not be able to submit the request for processing if you do not supply a value for your address city field.";
          }

          createCommentLog(entityManager, admin, cmtMsg);
        } else {
          // ASSUMPTION: CITY CONTAINS WHITESPACE
          // 1. query desc from lov with the use of desc from toUpperCase
          String enDesc = getCNCityCdByUpperDesc(entityManager, city1Upper);
          // 2. save desc as addr.city1
          if (StringUtils.isEmpty(enDesc) || StringUtils.isBlank(enDesc)) {
            cmtMsg += "\n\nFrom: " + addr.getCity1() + "    To: " + enDesc;
            addr.setCity1(enDesc);

            if (StringUtils.isEmpty(enDesc)) {
              cmtMsg += "\n\nPlease note that you will not be able to submit the request for processing if you do not supply a value for your address city field.";
            }
            createCommentLog(entityManager, admin, cmtMsg);
          } else {
            String newCity = getCnCityEngDescById(entityManager, enDesc);
            addr.setCity1(newCity);

            if (!addr.getCity1().equals(newCity)) {
              cmtMsg += "\n\nFrom: " + addr.getCity1() + "    To: " + enDesc;
              createCommentLog(entityManager, admin, cmtMsg);
            }

          }

        }

        entityManager.merge(addr);
        entityManager.merge(data);
        entityManager.flush();

        String adrnr = getAddressAdrnr(entityManager, SystemConfiguration.getValue("MANDT"), cmr, addr.getId().getAddrType(), addr.getPairedAddrSeq());
        Sadr sadr = getChinaAddtlAddr(entityManager, adrnr, SystemConfiguration.getValue("MANDT"));

        if (sadr != null) {
          IntlAddr iAddr = new IntlAddr();
          IntlAddrPK iAddrPK = new IntlAddrPK();
          IntlAddrRdc iAddrRdc = new IntlAddrRdc();
          IntlAddrRdcPK iAddrRdcPK = new IntlAddrRdcPK();

          iAddrPK.setReqId(addr.getId().getReqId());
          iAddrPK.setAddrSeq(addr.getId().getAddrSeq());
          iAddrPK.setAddrType(addr.getId().getAddrType());

          iAddr.setId(iAddrPK);
          iAddr.setIntlCustNm1(sadr.getName1());
          iAddr.setIntlCustNm2(sadr.getName2());
          iAddr.setCity1(sadr.getOrt01());
          iAddr.setCity2(sadr.getOrt02());
          iAddr.setAddrTxt(sadr.getStras());
          iAddr.setIntlCustNm4(sadr.getName4());
          iAddr.setLangCd(sadr.getSpras());

          entityManager.persist(iAddr);

          iAddrRdcPK.setReqId(addr.getId().getReqId());
          iAddrRdcPK.setAddrSeq(addr.getId().getAddrSeq());
          iAddrRdcPK.setAddrType(addr.getId().getAddrType());

          iAddrRdc.setId(iAddrRdcPK);
          iAddrRdc.setIntlCustNm1(sadr.getName1());
          iAddrRdc.setIntlCustNm2(sadr.getName2());
          iAddrRdc.setCity1(sadr.getOrt01());
          iAddrRdc.setCity2(sadr.getOrt02());
          iAddrRdc.setAddrTxt(sadr.getStras());
          iAddrRdc.setIntlCustNm4(sadr.getName4());
          iAddrRdc.setLangCd(sadr.getSpras());

          entityManager.persist(iAddrRdc);

        }

        if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          String qryKnvkAddr = ExternalizedQuery.getSql("GET_CONTINFO_ON_IMPORT_FOR_CN");
          PreparedQuery queryKnvK = new PreparedQuery(entityManager, qryKnvkAddr);
          queryKnvK.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          queryKnvK.setParameter("CMR", cmr);
          queryKnvK.setParameter("KATR6", SystemLocation.CHINA);

          List<Object[]> results = queryKnvK.getResults();

          if (results != null && !results.isEmpty()) {
            Object[] sResult = results.get(0);

            GeoContactInfo geoContactInfo = new GeoContactInfo();
            GeoContactInfoPK ePk = new GeoContactInfoPK();
            int contactId = 1;
            geoContactInfo.setContactFunc(sResult[0].toString());
            geoContactInfo.setContactName(sResult[1].toString());
            geoContactInfo.setContactPhone(sResult[2].toString());
            geoContactInfo.setContactType(addr.getId().getAddrType());
            geoContactInfo.setContactSeqNum(addr.getId().getAddrSeq());
            geoContactInfo.setCreateById(admin.getRequesterId());
            geoContactInfo.setCreateTs(SystemUtil.getCurrentTimestamp());

            try {
              contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(admin.getId().getReqId()));
              ePk.setContactInfoId(contactId);
            } catch (CmrException ex) {
              LOG.debug("Exception while getting contactId : " + ex.getMessage(), ex);
            }
            ePk.setReqId(data.getId().getReqId());
            geoContactInfo.setId(ePk);
            entityManager.persist(geoContactInfo);
            entityManager.flush();
          }
        }

      } catch (Exception e) {
        LOG.error("Error occured on setting SPID after import.");
        e.printStackTrace();
      }

    }

    if (soldToAddr != null && StringUtils.isEmpty(admin.getMainCustNm1())) {
      admin.setMainCustNm1(soldToAddr.getCustNm1());
      admin.setMainCustNm2(soldToAddr.getCustNm2());
      admin.setOldCustNm1(soldToAddr.getCustNm1());
      admin.setOldCustNm2(soldToAddr.getCustNm2());

      entityManager.merge(admin);
    }

    if (StringUtils.isBlank(data.getClientTier())) {
      data.setClientTier("BL");
    }

  }

  private IntlAddr getIntlAddr(EntityManager entityManager, Addr address) {
    IntlAddr iAddr = new IntlAddr();
    AddressService addSvc = new AddressService();
    iAddr = addSvc.getIntlAddrById(address, entityManager);
    return iAddr;
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "DEPT", "ADDR_TXT", "ADDR_TXT_2", "BLDG", "OFFICE", "CITY1", "CITY2",
        "POST_CD", "LAND_CNTRY", "PO_BOX"));
    return fields;
  }

  public static List<String> getAddressFieldsForBatchUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "DEPT", "ADDR_TXT", "ADDR_TXT_2", "BLDG", "OFFICE", "CITY1", "CITY2",
        "POST_CD", "LAND_CNTRY", "PO_BOX"));
    return fields;
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    if (SystemLocation.CHINA.equals(cmrIssiungCntry)) {
      return true;
    } else {
      return false;
    }
  }

  private KunnrExt getKunnrExtDetails(EntityManager entityManager, String kunnr) throws Exception {
    // String qryKunnrExt =
    // ExternalizedQuery.getSql("GET.KUNNR_EXT.BY_KUNNR_MANDT");
    // PreparedQuery query = new PreparedQuery(entityManager, qryKunnrExt);
    // query.setParameter("KUNNR", kunnr);
    // query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    // KunnrExt results = query.getSingleResult(KunnrExt.class);
    // return results;

    KunnrExt ke = new KunnrExt();
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KUNNR_EXT.BY_KUNNR_MANDT");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("BUILDING");
    query.addField("DEPARTMENT");
    query.addField("FLOOR");
    query.addField("OFFICE");
    query.addField("PRIVACY_IND");

    LOG.debug("Getting existing KUNNNR_EXT details from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);

      ke.setBuilding(record.get("BUILDING") != null ? record.get("BUILDING").toString() : "");
      ke.setDepartment(record.get("DEPARTMENT") != null ? record.get("DEPARTMENT").toString() : "");
      ke.setFloor(record.get("FLOOR") != null ? record.get("FLOOR").toString() : "");
      ke.setOffice(record.get("OFFICE") != null ? record.get("OFFICE").toString() : "");
      ke.setPrivacyInd(record.get("PRIVACY_IND") != null ? record.get("PRIVACY_IND").toString() : "");
      LOG.debug("***RETURNING BUILDING > " + ke.getBuilding());
      LOG.debug("***RETURNING DEPARTMENT > " + ke.getDepartment());
      LOG.debug("***RETURNING FLOOR > " + ke.getFloor());
      LOG.debug("***RETURNING OFFICE > " + ke.getOffice());
      LOG.debug("***RETURNING PRIVACY_IND > " + ke.getPrivacyInd());
    }

    return ke;

  }

  private String getRDcIerpSitePartyId(String kunnr) throws Exception {
    String spid = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.IERP.BRAN5");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("BRAN5");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing BRAN5 value from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      spid = record.get("BRAN5") != null ? record.get("BRAN5").toString() : "";
      LOG.debug("***RETURNING BRAN5 > " + spid + " WHERE KUNNR IS > " + kunnr);
    }

    return spid;
  }

  @Override
  protected String[] splitName(String name1, String name2, int length1, int length2) {
    String name = name1 + " " + (name2 != null ? name2 : "");
    String[] parts = name.split("[ ]");

    String namePart1 = "";
    String namePart2 = "";
    String namePart3 = "";

    boolean part1Ok = false;
    for (String part : parts) {
      if ((namePart1 + " " + part).trim().length() > length1 || part1Ok) {
        part1Ok = true;
        namePart2 += " " + part;
      } else {
        namePart1 += " " + part;
      }
    }
    namePart1 = namePart1.trim();

    if (name != null && !StringUtils.isEmpty(name.trim())) {
      if (namePart1.length() == 0) {
        namePart1 = name.substring(0, length1);
        namePart2 = name.substring(length1);
      }
    } else {
      return new String[] { "", "", "" };
    }

    if (namePart1.length() > length1) {
      namePart1 = namePart1.substring(0, length1);
    }
    namePart2 = namePart2.trim();
    if (namePart2.length() > 35) {

      String[] tmpAr = namePart2.split(" ");
      int idxStart = 0;
      String ret = "";
      for (int i = 0; i < tmpAr.length; i++) {
        ret = ret + " " + tmpAr[i];
        idxStart = i + 1;
        String base = ret + " " + tmpAr[i + 1];
        if (base.length() > 35) {
          break;
        }
      }

      String temp1 = ret;
      namePart2 = temp1.trim();

      for (int i = idxStart; i < tmpAr.length; i++) {
        namePart3 = namePart3 + " " + tmpAr[i];
      }// namePart3 = temp2;

      namePart3 = namePart3.trim();

      LOG.debug("namePart2 >>> " + namePart2);
      System.out.println("idxStart >>> " + idxStart);
      System.out.println("namePart3 >>>" + namePart3);

    }

    return new String[] { namePart1, namePart2, namePart3 };

  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  public static boolean isCNIssuingCountry(String issuingCntry) {
    if (SystemLocation.CHINA.equals(issuingCntry)) {
      return true;
    } else {
      return false;
    }
  }

  public String getChinaCityID(EntityManager entityManager, String stateProv, String cityTxt) {
    String cityID = "";
    String qryChinaCityID = ExternalizedQuery.getSql("GET.GEO_CITIES_BYID");
    stateProv += "%";
    PreparedQuery query = new PreparedQuery(entityManager, qryChinaCityID);
    query.setParameter("CITY_DESC", cityTxt);
    query.setParameter("ISSUING_CNTRY", SystemLocation.CHINA);
    query.setParameter("STATE_CD", stateProv);
    List<Object[]> results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      cityID = sResult[1].toString();
    }

    return cityID;

  }

  public String getAddressAdrnr(EntityManager entityManager, String mandt, String cmr, String ktokd, String seq) {
    String adrnr = "";
    String qryChinaAdrnr = ExternalizedQuery.getSql("GET.KNA1_ADRNR");
    PreparedQuery query = new PreparedQuery(entityManager, qryChinaAdrnr);
    query.setParameter("CMR", cmr);
    query.setParameter("ADDR_TYPE", ktokd);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", SystemLocation.CHINA);
    query.setParameter("ADDR_SEQ", seq);
    List<Object[]> results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      adrnr = sResult[1].toString();
    }

    return adrnr;
  }

  public Sadr getChinaAddtlAddr(EntityManager entityManager, String adrnr, String mandt) {
    Sadr sadr = new Sadr();
    String qryAddlAddr = ExternalizedQuery.getSql("GET.CN_SADR_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, qryAddlAddr);
    query.setParameter("ADRNR", adrnr);
    query.setParameter("MANDT", mandt);
    sadr = query.getSingleResult(Sadr.class);

    return sadr;
  }

  private boolean containsWhiteSpace(String val) {
    Pattern pattern = Pattern.compile("\\s");
    boolean found = false;
    if (val != null) {
      Matcher matcher = pattern.matcher(val);
      found = matcher.find();
      return found;
    }
    return found;
  }

  private String getCNCityCdByUpperDesc(EntityManager entityManager, String desc) {
    String cdOfUpperDesc = "";
    List<Object[]> results = new ArrayList<Object[]>();
    String qryCNCityCdByUpperDesc = ExternalizedQuery.getSql("GET.CN_ENCITY_BY_UPPER_DESC");
    PreparedQuery query = new PreparedQuery(entityManager, qryCNCityCdByUpperDesc);
    query.setParameter("DESC", desc);
    query.setParameter("FIELD_ID", CmrConstants.CN_CITIES_UPPER_FIELD_ID);
    query.setParameter("CNTRY", SystemLocation.CHINA);

    results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      cdOfUpperDesc = sResult[1].toString();
    }

    return cdOfUpperDesc;
  }

  private String getCnCityEngDescById(EntityManager entityManager, String cityId) {
    String enDesc = "";
    List<Object[]> results = new ArrayList<Object[]>();
    String qryCNCityEnDescById = ExternalizedQuery.getSql("GET.GEO_CITIES_BY_CITYID");
    PreparedQuery query = new PreparedQuery(entityManager, qryCNCityEnDescById);
    query.setParameter("CITY_ID", cityId);
    query.setParameter("ISSUING_CNTRY", SystemLocation.CHINA);

    results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      enDesc = sResult[2].toString();
    }

    return enDesc;
  }

  private String getCnCityEngDescByDesc(EntityManager entityManager, String cityDesc) {
    String enDesc = "";
    List<Object[]> results = new ArrayList<Object[]>();
    String qryCNCityEnDescByDesc = ExternalizedQuery.getSql("GET.GEO_CITIES_BY_CITYDESC");
    PreparedQuery query = new PreparedQuery(entityManager, qryCNCityEnDescByDesc);
    query.setParameter("CITY_DESC", cityDesc);
    query.setParameter("ISSUING_CNTRY", SystemLocation.CHINA);

    results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      enDesc = sResult[2].toString();
    }

    return enDesc;
  }

  private String getCnAddrSeqById(EntityManager entityManager, String kunnr, String mandt) {
    String seqNo = "";
    List<Object[]> results = new ArrayList<Object[]>();
    String cnAddrSeqById = ExternalizedQuery.getSql("GET.KNA1_ZZKV_SEQNO");
    PreparedQuery query = new PreparedQuery(entityManager, cnAddrSeqById);
    query.setParameter("KUNNR", kunnr);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", SystemLocation.CHINA);
    results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      seqNo = sResult[1].toString();
    }

    return seqNo;
  }

  public String[] getRDcName1Name2Values(String kunnr) throws Exception {
    String[] name1Name2Val = new String[2];

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.LA.NAME1.NAME2");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("NAME1");
    query.addField("NAME2");
    query.addField("KUNNR");

    LOG.debug("Getting existing NAME1, NAME2 value from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      name1Name2Val[0] = record.get("NAME1") != null ? record.get("NAME1").toString() : "";
      name1Name2Val[1] = record.get("NAME2") != null ? record.get("NAME2").toString() : "";
    }

    return name1Name2Val;
  }

  public IntlAddr getIntlAddrById(Addr addr, EntityManager entityManager) {
    IntlAddr iAddr = new IntlAddr();
    String addrType = "";

    if (addr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZS01.toString())) {
      addrType = CmrConstants.ADDR_TYPE.ZS01.toString();
    } else if (addr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZP01.toString())) {
      addrType = CmrConstants.ADDR_TYPE.ZP01.toString();
    } else if (addr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZD01.toString())) {
      addrType = CmrConstants.ADDR_TYPE.ZD01.toString();
    } else if (addr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZI01.toString())) {
      addrType = CmrConstants.ADDR_TYPE.ZI01.toString();
    }

    String qryIntlAddrById = ExternalizedQuery.getSql("GET.INTL_ADDR_BY_ID");

    PreparedQuery query = new PreparedQuery(entityManager, qryIntlAddrById);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
    query.setParameter("ADDR_TYPE", addrType);

    iAddr = query.getSingleResult(IntlAddr.class);

    return iAddr;
  }

  /**
   * Checks if this {@link Data} record has been updated. This method compares
   * with the {@link DataRdc} equivalent and compares per field and filters
   * given the configuration on the corresponding {@link GEOHandler} for the
   * given CMR issuing country. If at least one field is not empty, it will
   * return true.
   * 
   * @param data
   * @param dataRdc
   * @param cmrIssuingCntry
   * @return
   */
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
            // no stored value or field not on addr rdc, return null for no
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

  public boolean isAddrUpdated(Addr addr, AddrRdc addrRdc, String cmrIssuingCntry, Data data, EntityManager entityManager) {
    boolean isAddrUpdated = false;

    isAddrUpdated = RequestUtils.isUpdated(addr, addrRdc, cmrIssuingCntry);
    AddressService aService = new AddressService();

    // Now the tricky part, we need to compare INTL_ADDR with SADR
    // do only if there is no ADDR data updated
    if (!isAddrUpdated) {
      // get INTL_ADDR
      IntlAddr iAddr = aService.getIntlAddrById(addr, entityManager);
      // get SADR
      String adrnr = getAddressAdrnr(entityManager, SystemConfiguration.getValue("MANDT"), data.getCmrNo(), addr.getId().getAddrType(),
          addr.getPairedAddrSeq());
      Sadr sadr = getChinaAddtlAddr(entityManager, adrnr, SystemConfiguration.getValue("MANDT"));

      if (iAddr != null && sadr != null) {
        if (iAddr.getIntlCustNm1() != null && sadr != null && !iAddr.getIntlCustNm1().equalsIgnoreCase(sadr.getName1())) {
          return true;
        } else if (iAddr.getIntlCustNm2() != null && sadr != null && !iAddr.getIntlCustNm2().equalsIgnoreCase(sadr.getName2())) {
          return true;
        } else if (iAddr.getCity1() != null && sadr != null && !iAddr.getCity1().equalsIgnoreCase(sadr.getOrt01())) {
          return true;
        } else if (iAddr.getCity2() != null && sadr != null && !iAddr.getCity2().equalsIgnoreCase(sadr.getOrt02())) {
          return true;
        } else if (iAddr.getAddrTxt() != null && sadr != null && !iAddr.getAddrTxt().equalsIgnoreCase(sadr.getStras())) {
          return true;
        } else if (iAddr.getIntlCustNm4() != null && sadr != null && !iAddr.getIntlCustNm4().equalsIgnoreCase(sadr.getStrs2())) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }

    } else {
      return isAddrUpdated;
    }

  }

  public static boolean isClearDPL(AddressModel model, Addr addr, EntityManager entityManager) {
    AddressService aService = new AddressService();
    IntlAddr iAddr = aService.getIntlAddrById(addr, entityManager);

    String aAddrTxt = addr.getAddrTxt() != null ? addr.getAddrTxt().trim().toLowerCase() : "";
    String aAddrTxt2 = addr.getAddrTxt2() != null ? addr.getAddrTxt2().trim().toLowerCase() : "";
    String aCity1 = addr.getCity1() != null ? addr.getCity1().trim().toLowerCase() : "";
    String aCity2 = addr.getCity2() != null ? addr.getCity2().trim().toLowerCase() : "";
    String aBldg = addr.getBldg() != null ? addr.getBldg().trim().toLowerCase() : "";
    String aStateProv = addr.getStateProv() != null ? addr.getStateProv().trim().toLowerCase() : "";
    String aPostCd = addr.getPostCd() != null ? addr.getPostCd().trim().toLowerCase() : "";

    String aCnDistrict = "";
    String aCnCustName = "";
    String aCnCustNameCont = "";
    String aCnStreetAddrTxt = "";
    String aCnStreetAddrTxt2 = "";

    if (iAddr != null) {
      aCnDistrict = iAddr.getCity2();
      aCnCustName = iAddr.getIntlCustNm1();
      aCnCustNameCont = iAddr.getIntlCustNm2();
      aCnStreetAddrTxt = iAddr.getAddrTxt();
      aCnStreetAddrTxt2 = iAddr.getIntlCustNm4();
    }

    String aDept = addr.getDept() != null ? addr.getDept().trim().toLowerCase() : "";
    String aOfc = addr.getOffice() != null ? addr.getOffice().trim().toLowerCase() : "";
    String aPostBox = addr.getPoBox() != null ? addr.getPoBox().trim().toLowerCase() : "";
    String aPhoneNum = addr.getCustPhone() != null ? addr.getCustPhone().trim().toLowerCase() : "";
    String aTransportZone = addr.getTransportZone() != null ? addr.getTransportZone().trim().toLowerCase() : "";

    String mAddrTxt = model.getAddrTxt() != null ? model.getAddrTxt().trim().toLowerCase() : "";
    String mAddrTxt2 = model.getAddrTxt2() != null ? model.getAddrTxt2().trim().toLowerCase() : "";
    String mCity1 = model.getCity1() != null ? model.getCity1().trim().toLowerCase() : "";
    String mCity2 = model.getCity2() != null ? model.getCity2().trim().toLowerCase() : "";
    String mBldg = model.getBldg() != null ? model.getBldg().trim().toLowerCase() : "";
    String mStateProv = model.getStateProv() != null ? model.getStateProv().trim().toLowerCase() : "";
    String mPostCd = model.getPostCd() != null ? model.getPostCd().trim().toLowerCase() : "";
    String mCnDistrict = model.getCnDistrict() != null ? model.getPostCd().trim().toLowerCase() : "";
    String mDept = model.getDept() != null ? model.getDept().trim().toLowerCase() : "";
    String mOfc = model.getOffice() != null ? model.getOffice().trim().toLowerCase() : "";
    String mPostBox = model.getPoBox() != null ? model.getPoBox().trim().toLowerCase() : "";
    String mPhoneNum = model.getCustPhone() != null ? model.getCustPhone().trim().toLowerCase() : "";
    String mTransportZone = model.getTransportZone() != null ? model.getTransportZone().trim().toLowerCase() : "";
    String mCnCustName = model.getCnCustName1() != null ? model.getCnCustName1().trim().toLowerCase() : "";
    String mCnCustNameCont = model.getCnCustName2() != null ? model.getCnCustName2().trim().toLowerCase() : "";
    String mCnStreetAddrTxt = model.getCnAddrTxt() != null ? model.getCnAddrTxt().trim().toLowerCase() : "";
    String mCnStreetAddrTxt2 = model.getCnAddrTxt2() != null ? model.getCnAddrTxt2().trim().toLowerCase() : "";

    if (!StringUtils.equals(aAddrTxt, mAddrTxt) || !StringUtils.equals(aAddrTxt2, mAddrTxt2) || !StringUtils.equals(aCity1, mCity1)
        || !StringUtils.equals(aCity2, mCity2) || !StringUtils.equals(aBldg, mBldg) || !StringUtils.equals(aStateProv, mStateProv)
        || !StringUtils.equals(aPostCd, mPostCd) || !StringUtils.equals(aCnDistrict, mCnDistrict) || !StringUtils.equals(aDept, mDept)
        || !StringUtils.equals(aOfc, mOfc) || !StringUtils.equals(aPostBox, mPostBox) || !StringUtils.equals(aPhoneNum, mPhoneNum)
        || !StringUtils.equals(aTransportZone, mTransportZone) || !StringUtils.equals(aCnCustName, mCnCustName)
        || !StringUtils.equals(aCnCustNameCont, mCnCustNameCont) || !StringUtils.equals(aCnStreetAddrTxt, mCnStreetAddrTxt)
        || !StringUtils.equals(aCnStreetAddrTxt2, mCnStreetAddrTxt2)) {
      return true;
    }

    return false;
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    // get INTL_ADDR
    IntlAddr iAddr = getIntlAddrByIdReqSummary(addr, entityManager);
    IntlAddrRdc iAddrRdc = getIntlAddrRdcByIdReqSummary(addr, entityManager);

    // get SADR
    if (iAddrRdc == null) {
      iAddrRdc = new IntlAddrRdc();
      String pairedSeqNo = "";

      if (StringUtils.isBlank(addr.getPairedAddrSeq())) {
        pairedSeqNo = getCnAddrSeqById(entityManager, addr.getSapNo(), SystemConfiguration.getValue("MANDT"));
      } else {
        pairedSeqNo = addr.getPairedAddrSeq();
      }

      String adrnr = getAddressAdrnr(entityManager, SystemConfiguration.getValue("MANDT"), addr.getCmrNumber(), addr.getId().getAddrType(),
          pairedSeqNo);
      Sadr sadr = getChinaAddtlAddr(entityManager, adrnr, SystemConfiguration.getValue("MANDT"));

      if (sadr != null) {
        IntlAddrRdcPK iAddrRdcPK = new IntlAddrRdcPK();

        iAddrRdcPK.setReqId(addr.getId().getReqId());
        iAddrRdcPK.setAddrSeq(addr.getId().getAddrSeq());
        iAddrRdcPK.setAddrType(addr.getId().getAddrType());

        iAddrRdc.setId(iAddrRdcPK);
        iAddrRdc.setIntlCustNm1(sadr.getName1());
        iAddrRdc.setIntlCustNm2(sadr.getName2());
        iAddrRdc.setCity1(sadr.getOrt01());
        iAddrRdc.setCity2(sadr.getOrt02());
        iAddrRdc.setAddrTxt(sadr.getStras());
        iAddrRdc.setIntlCustNm4(sadr.getStrs2());
        iAddrRdc.setLangCd(sadr.getSpras());
      }
    }

    if (iAddr != null && iAddrRdc != null) {
      if (!equals(iAddr.getIntlCustNm1(), iAddrRdc.getIntlCustNm1())) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaCustomerName1", "-"));
        update.setNewData(iAddr.getIntlCustNm1());
        update.setOldData(iAddrRdc.getIntlCustNm1());
        results.add(update);
      }
      if (!equals(iAddr.getIntlCustNm2(), iAddrRdc.getIntlCustNm2())) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaCustomerName2", "-"));
        update.setNewData(iAddr.getIntlCustNm2());
        update.setOldData(iAddrRdc.getIntlCustNm2());
        results.add(update);
      }
      if (!equals(iAddr.getAddrTxt(), iAddrRdc.getAddrTxt())) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaStreetAddress1", "-"));
        update.setNewData(iAddr.getAddrTxt());
        update.setOldData(iAddrRdc.getAddrTxt());
        results.add(update);
      }
      if (!equals(iAddr.getIntlCustNm4(), iAddrRdc.getIntlCustNm4())) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaStreetAddress2", "-"));
        update.setNewData(iAddr.getIntlCustNm4());
        update.setOldData(iAddrRdc.getIntlCustNm4());
        results.add(update);
      }
      // city
      if (!equals(iAddr.getCity1(), iAddrRdc.getCity1())) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "DropDownCityChina", "-"));
        update.setNewData(iAddr.getCity1());
        update.setOldData(iAddrRdc.getCity1());
        results.add(update);
      }
      // district
      if (!equals(iAddr.getCity2(), iAddrRdc.getCity2())) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaCity2", "-"));
        update.setNewData(iAddr.getCity2());
        update.setOldData(iAddrRdc.getCity2());
        results.add(update);
      }
    }
  }

  /**
   * Checks absolute equality between the strings
   * 
   * @param val1
   * @param val2
   * @return
   */
  public boolean equals(String val1, String val2) {
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

  public IntlAddr getIntlAddrByIdReqSummary(UpdatedAddr addr, EntityManager entityManager) {
    IntlAddr iAddr = new IntlAddr();
    String qryIntlAddrById = ExternalizedQuery.getSql("GET.INTL_ADDR_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, qryIntlAddrById);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());

    iAddr = query.getSingleResult(IntlAddr.class);

    return iAddr;
  }

  public IntlAddrRdc getIntlAddrRdcByIdReqSummary(UpdatedAddr addr, EntityManager entityManager) {
    IntlAddrRdc iAddrRdc = new IntlAddrRdc();
    String qryIntlAddrById = ExternalizedQuery.getSql("GET.INTL_ADDR_RDC_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, qryIntlAddrById);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());

    iAddrRdc = query.getSingleResult(IntlAddrRdc.class);

    return iAddrRdc;
  }

  public static int getLengthInUtf8(CharSequence sequence) {
    LOG.debug(">>>>>>>>> getLengthInUtf8() :: Start");
    int count = 0;
    for (int i = 0, len = sequence.length(); i < len; i++) {
      char ch = sequence.charAt(i);
      if (ch <= 0x7F) {
        count++;
      } else if (ch <= 0x7FF) {
        count += 2;
      } else if (Character.isHighSurrogate(ch)) {
        count += 4;
        ++i;
      } else {
        count += 3;
      }
    }

    LOG.debug(">>>>>>>>> getLengthInUtf8() : length = " + count);
    LOG.debug(">>>>>>>>> getLengthInUtf8() :: End");
    return count;
  }

  public static int getMaxWordLengthInUtf8(CharSequence sequence, int dbLength, int defaultLength) {
    LOG.debug(">>>>>>>>> getMaxWordLengthInUtf8() :: Start");
    int previousCount = 0;
    int count = 0;
    for (int i = 0, len = sequence.length(); i < len; i++) {
      char ch = sequence.charAt(i);
      if (ch <= 0x7F) {
        previousCount = 1;
      } else if (ch <= 0x7FF) {
        previousCount = 2;
      } else if (Character.isHighSurrogate(ch)) {
        previousCount = 4;
        ++i;
      } else {
        previousCount = 3;
      }

      // get the Max count in CharSequence;
      if (previousCount != 0 && previousCount > count) {
        count = previousCount;
      }
    }

    LOG.debug(">>>>>>>>> getMaxWordLengthInUtf8() : Max count = " + count);
    LOG.debug(">>>>>>>>> getMaxWordLengthInUtf8() : DB Length = " + dbLength);
    LOG.debug(">>>>>>>>> getMaxWordLengthInUtf8() : Default Length = " + defaultLength);
    LOG.debug(">>>>>>>>> getMaxWordLengthInUtf8() : Math.floor(dbLength/count) = " + Math.floor(dbLength / count));
    LOG.debug(">>>>>>>>> RDCStringUtil.getMaxWordLengthInUtf8() :: End");

    if (count == 1) {
      LOG.debug(">>>>>>>>> getMaxWordLengthInUtf8() : Max count = 1, so return defaultLength.");
      return defaultLength;
    }

    return new Double(Math.floor(dbLength / count)).intValue();
  }

  public GeoContactInfo getGeoContactInfoById(Addr addr, EntityManager entityManager) {
    GeoContactInfo geoContactInfo = null;
    String addrType = "";

    if (addr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZS01.toString())) {
      addrType = CmrConstants.ADDR_TYPE.ZS01.toString();
    } else if (addr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZP01.toString())) {
      addrType = CmrConstants.ADDR_TYPE.ZP01.toString();
    } else if (addr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZD01.toString())) {
      addrType = CmrConstants.ADDR_TYPE.ZD01.toString();
    } else if (addr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZI01.toString())) {
      addrType = CmrConstants.ADDR_TYPE.ZI01.toString();
    }
    if ("ZS01".equals(addrType)) {
      String qryGeoContactInfoById = ExternalizedQuery.getSql("GET.GEO_CONTACT_INFO_BY_ID");
      PreparedQuery query = new PreparedQuery(entityManager, qryGeoContactInfoById);
      query.setParameter("REQ_ID", addr.getId().getReqId());
      query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
      query.setParameter("ADDR_TYPE", addrType);

      geoContactInfo = query.getSingleResult(GeoContactInfo.class);
    }

    return geoContactInfo;
  }

  public List<IntlAddr> getINTLAddrCountByReqId(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("QUERY.INTL_ADDR_BY_REQ_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<IntlAddr> intlAddrList;
    try {
      intlAddrList = query.getResults(IntlAddr.class);
    } catch (Exception ex) {
      LOG.error("An error occured in getting the INTL_ADDR records");
      throw ex;
    }
    return intlAddrList;
  }

  public List<IntlAddrRdc> getINTLAddrRdcByReqId(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("QUERY.INTL_ADDR_RDC_BY_REQ_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<IntlAddrRdc> intlAddrRdcList;
    try {
      intlAddrRdcList = query.getResults(IntlAddrRdc.class);
    } catch (Exception ex) {
      LOG.error("An error occured in getting the INTL_ADDR_RDC records");
      throw ex;
    }
    return intlAddrRdcList;
  }

  public void createCommentLog(EntityManager em, Admin admin, String message) throws CmrException, SQLException {
    LOG.info("Creating Comment Log for Req ID " + admin.getId().getReqId());
    ReqCmtLog reqCmtLog = new ReqCmtLog();
    ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
    reqCmtLogpk.setCmtId(SystemUtil.getNextID(em, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    reqCmtLog.setId(reqCmtLogpk);
    reqCmtLog.setReqId(admin.getId().getReqId());
    reqCmtLog.setCmt(message != null ? message : "No message provided.");
    // save cmtlockedIn as Y default for current realese
    reqCmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    reqCmtLog.setCreateById(admin.getLastUpdtBy());
    reqCmtLog.setCreateByNm("CreateCMR");
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    em.persist(reqCmtLog);

  }

  
@Override
public Map<String, String> getUIFieldIdMap(){ 
Map<String, String> map = new HashMap<String, String>();
map.put("##OriginatorName", "originatorNm");
map.put("##SensitiveFlag", "sensitiveFlag");
map.put("##INACType", "inacType");
map.put("##ISU", "isuCd");
map.put("##Building", "bldg");
map.put("##CMROwner", "cmrOwner");
map.put("##DropDownCityChina", "cnCity");
map.put("##PPSCEID", "ppsceid");
map.put("##CustLang", "custPrefLang");
map.put("##ExportCodesCountry", "custAcctType");
map.put("##GlobalBuyingGroupID", "gbgId");
map.put("##ExportCodesTDOIndicator", "icmsInd");
map.put("##CoverageID", "covId");
map.put("##OriginatorID", "originatorId");
map.put("##BPRelationType", "bpRelType");
map.put("##ChinaCustomerCntName", "cnCustContNm");
map.put("##CAP", "capInd");
map.put("##RequestReason", "reqReason");
map.put("##POBox", "poBox");
map.put("##ParentCompanyNo", "dealerNo");
map.put("##SocialCreditCd", "busnType");
map.put("##LandedCountry", "landCntry");
map.put("##CMRIssuingCountry", "cmrIssuingCntry");
map.put("##INACCode", "inacCd");
map.put("##CustPhone", "custPhone");
map.put("##CustomerScenarioType", "custGrp");
map.put("##ChinaCity2", "cnDistrict");
map.put("##ClassCode", "custClass");
map.put("##City1", "city1");
map.put("##City2", "city2");
map.put("##RequestingLOB", "requestingLob");
map.put("##AddrStdRejectReason", "addrStdRejReason");
map.put("##ExpediteReason", "expediteReason");
map.put("##VAT", "vat");
map.put("##CMRNumber", "cmrNo");
map.put("##Office", "office");
map.put("##Subindustry", "subIndustryCd");
map.put("##EnterCMR", "enterCMRNo");
map.put("##StateProvChina", "stateProv");
map.put("##DisableAutoProcessing", "disableAutoProc");
map.put("##ChinaStreetAddress1", "cnAddrTxt");
map.put("##Expedite", "expediteInd");
map.put("##BGLDERule", "bgRuleId");
map.put("##ProspectToLegalCMR", "prospLegalInd");
map.put("##ChinaStreetAddress2", "cnAddrTxt2");
map.put("##RdcComment", "rdcComment");
map.put("##ChinaSearchTerm", "searchTerm");
map.put("##ExportCodesTDOdate", "bioChemMissleMfg");
map.put("##ClientTier", "clientTier");
map.put("##CustomerCntPhone2", "cnCustContPhone2");
map.put("##IERPSitePrtyId", "ierpSitePrtyId");
map.put("##DropDownCity", "city1DrpDown");
map.put("##SAPNumber", "sapNo");
map.put("##Department", "dept");
map.put("##StreetAddress2", "addrTxt2");
map.put("##Company", "company");
map.put("##StreetAddress1", "addrTxt");
map.put("##CustomerName1", "custNm1");
map.put("##CustomerName2", "custNm2");
map.put("##ISIC", "isicCd");
map.put("##CustomerName3", "custNm3");
map.put("##CustomerCntJobTitle", "cnCustContJobTitle");
map.put("##Enterprise", "enterprise");
map.put("##IbmDeptCostCenter", "ibmDeptCostCenter");
map.put("##PostalCode", "postCd");
map.put("##InterAddrKey", "cnInterAddrKey");
map.put("##TransportZone", "transportZone");
map.put("##DUNS", "dunsNo");
map.put("##BuyingGroupID", "bgId");
map.put("##ChinaCustomerName1", "cnCustName1");
map.put("##RequesterID", "requesterId");
map.put("##ChinaCustomerName2", "cnCustName2");
map.put("##GeoLocationCode", "geoLocationCd");
map.put("##ChinaCustomerName3", "cnCustName3");
map.put("##MembLevel", "memLvl");
map.put("##GovIndicator", "govType");
map.put("##PrivacyIndc", "privIndc");
map.put("##RequestType", "reqType");
map.put("##CustomerScenarioSubType", "custSubGrp");
return map;
}
  
  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }
}
