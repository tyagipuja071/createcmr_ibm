package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.GBGMatchingElement;
import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.ApprovalReq;
import com.ibm.cio.cmr.request.entity.CmrCloningQueue;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.DefaultApprovals;
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
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.service.requestentry.GeoContactInfoService;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

public class CNHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(CNHandler.class);
  private static final String[] DE_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "LocalTax1", "LocalTax2", "SitePartyID", "Division", "POBoxCity", "CustFAX",
      "City2", "Affiliate", "Company", "INACType" };
  public static final int CN_STREET_ADD_TXT = 70;
  public static final int CN_STREET_ADD_TXT2 = 70;
  public static final int CN_CUST_NAME_1 = 70;
  public static final int CN_CUST_NAME_2 = 70;
  private String[] CN_S_S_INAC_CODE = { "GC58", "GC55", "GC34", "G063", "N670", "J273", "5395", "N806", "J258", "CG06", "4089", "1922" };
  private String KYNDRYL_CLUSTER = "09058";

  public static List<String> getDataFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD",
        "SUB_INDUSTRY_CD", "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD",
        "DUNS_NO", "PPSCEID", "MEM_LVL", "BP_REL_TYPE"));
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
    // DnB addrTxt2 fix
    if (!StringUtils.isEmpty(currentRecord.getCmrName4())) {
      address.setAddrTxt2(currentRecord.getCmrName4());
    }

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
        if ("XXXX".equals(data.getInacCd())) {
          data.setInacType("I");
        } else {
          data.setInacType("N");
        }
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
    }

    // set inac type
    String inac = data.getInacCd();
    if (!StringUtils.isEmpty(inac)) {
      // get starting char of the inac code
      String startCd = inac.substring(0, 1);
      LOG.debug(">>> START Char of INAC CODE is " + startCd);

      if (StringUtils.isAlpha(startCd)) {
        if ("XXXX".equals(inac)) {
          LOG.debug(">>> INAC CODE is XXXX setting type to I");
          data.setInacType("I");
        } else {
          // set type as N if alpha
          LOG.debug(">>> START Char of INAC CODE is ALPHA setting type to N");
          data.setInacType("N");
        }
      } else if (StringUtils.isNumeric(startCd)) {
        LOG.debug(">>> START Char of INAC CODE is NUMERIC setting type to I");
        data.setInacType("I");
      } else {
        data.setInacType("");
      }
    }

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
        && (data.getCustSubGrp() == null
            || !(data.getCustSubGrp().equals("INTER") || data.getCustSubGrp().equals("BUSPR") || data.getCustSubGrp().equals("PRIV")))
        && StringUtils.isBlank(data.getIsicCd()) && StringUtils.isNotBlank(data.getBusnType())) {
      getIsicByDNB(entityManager, data);
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && (data.getCapInd() == null || data.getCapInd().equals("N"))
        && !isBPUser(data)) {
      getIsicByDNB(entityManager, data);
    }
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
        && (data.getCustSubGrp() != null
            && !(data.getCustSubGrp().equals("INTER") || data.getCustSubGrp().equals("BUSPR") || data.getCustSubGrp().equals("PRIV")))
        && StringUtils.isBlank(data.getGbgId())) {
      Addr soldToAddr = null;
      if (StringUtils.isNotEmpty(admin.getMainCustNm1())) {
        String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO_ZS01");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", admin.getId().getReqId());
        soldToAddr = query.getSingleResult(Addr.class);
      }
      if (soldToAddr != null) {
        getGBGIdViaLH(entityManager, admin, data, soldToAddr);
        // getGBGId(entityManager, admin, data, soldToAddr);
      }

    }

    // data.setSearchTerm(data.getCovId());

    // convert chinese name and address to DBCS
    List<IntlAddr> intlAddrList = getINTLAddrCountByReqId(entityManager, data.getId().getReqId());

    for (IntlAddr intlAddr : intlAddrList) {
      intlAddr.setIntlCustNm1(convert2DBCS(intlAddr.getIntlCustNm1()));
      intlAddr.setIntlCustNm2(convert2DBCS(intlAddr.getIntlCustNm2()));
      intlAddr.setIntlCustNm3(convert2DBCS(intlAddr.getIntlCustNm3()));
      intlAddr.setAddrTxt(convert2DBCS(intlAddr.getAddrTxt()));
      intlAddr.setIntlCustNm4(convert2DBCS(intlAddr.getIntlCustNm4()));
      intlAddr.setCity2(convert2DBCS(intlAddr.getCity2()));
      updateCNIntlAddr(intlAddr, entityManager);
      entityManager.merge(intlAddr);
    }
    entityManager.flush();

  }

  // existing CMR and linked to CEID
  // 1. Search term = ‘08036’, please do not define it as BP CMR.
  // 2. Search term = ‘04182’, please define it as BP CMR.
  // 3. Search term in invalid value (‘00000’,’000000’,BLANK Value, Non numeric
  // (wording)) and the CMR prefix start with ‘1’ or ‘2’, please define it as BP
  // CMR
  // existing CMR but do not linked to CEID
  // 1. Search term ≠ ‘04182’, please do not define it as BP CMR.
  // 2. Search term in invalid value (‘00000’,’000000’,BLANK Value, Non numeric
  // (wording)) and the CMR prefix start with ‘1’ or ‘2’, please define it as BP
  // CMR
  // 3. Search term = ‘04182’, please define it as BP CMR.
  private boolean isBPUser(Data data) {
    if (StringUtils.isNotBlank(data.getPpsceid())) {
      if ("08036".equals(data.getSearchTerm())) {
        return false;
      }
      if ("00075".equals(data.getSearchTerm())) {
        return true;
      } else if ((StringUtils.isBlank(data.getSearchTerm()) || "00000".equals(data.getSearchTerm()) || "000000".equals(data.getSearchTerm())
          || data.getSearchTerm().matches("[^0-9]+")) && data.getCmrNo() != null
          && (data.getCmrNo().startsWith("1") || data.getCmrNo().startsWith("2"))) {
        return true;
      } else {
        return false;
      }
    } else {
      if ((StringUtils.isBlank(data.getSearchTerm()) || "00000".equals(data.getSearchTerm()) || "000000".equals(data.getSearchTerm())
          || data.getSearchTerm().matches("[^0-9]+")) && data.getCmrNo() != null
          && (data.getCmrNo().startsWith("1") || data.getCmrNo().startsWith("2"))) {
        return true;
      } else if ("00075".equals(data.getSearchTerm())) {
        return true;
      } else {
        return false;
      }
    }
  }

  private void getGBGIdByGBGservice(EntityManager entityManager, Admin admin, Data data, Addr currentAddress, String dunsNo, boolean flag)
      throws Exception {
    if (!flag) {
      // TODO Auto-generated method stub
      GBGFinderRequest request = new GBGFinderRequest();
      request.setMandt(SystemConfiguration.getValue("MANDT"));

      request.setCity(currentAddress.getCity1());
      request
          .setCustomerName(currentAddress.getCustNm1() + (StringUtils.isBlank(currentAddress.getCustNm2()) ? "" : " " + currentAddress.getCustNm2()));

      String nameUsed = request.getCustomerName();
      LOG.debug("Checking GBG for " + nameUsed);
      // usedNames.add(nameUsed.toUpperCase());
      request.setIssuingCountry(data.getCmrIssuingCntry());
      request.setStreetLine1(currentAddress.getAddrTxt());
      request.setStreetLine2(currentAddress.getAddrTxt2());
      request.setLandedCountry(currentAddress.getLandCntry());
      request.setPostalCode(currentAddress.getPostCd());
      request.setStateProv(currentAddress.getStateProv());
      if (!StringUtils.isBlank(data.getVat())) {
        request.setOrgId(data.getVat());
      }
      request.setMinConfidence("6");
      if (StringUtils.isNotBlank(dunsNo)) {
        request.setDunsNo(dunsNo);
      } else {
        request.setDunsNo(data.getDunsNo());
      }
      MatchingResponse<?> rawResponse = null;
      try {
        MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
            MatchingServiceClient.class);
        client.setRequestMethod(Method.Get);
        client.setReadTimeout(1000 * 60 * 5);

        LOG.debug("Connecting to the GBG Finder Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
        rawResponse = client.executeAndWrap(MatchingServiceClient.GBG_SERVICE_ID, request, MatchingResponse.class);

      } catch (Exception e) {
        LOG.error("CMR Error:" + e.getMessage());
      }
      ObjectMapper mapper = new ObjectMapper();
      String json = null;
      if (rawResponse == null) {
        json = "";
      } else {
        json = mapper.writeValueAsString(rawResponse);
      }

      TypeReference<MatchingResponse<GBGResponse>> ref = new TypeReference<MatchingResponse<GBGResponse>>() {
      };
      MatchingResponse<GBGResponse> response = null;
      if (StringUtils.isNotBlank(json)) {
        response = mapper.readValue(json, ref);
      }

      if (response != null && response.getMatched()) {
        List<GBGResponse> gbgMatches = response.getMatches();
        Collections.sort(gbgMatches, new GBGComparator(data.getCmrIssuingCntry()));

        if (gbgMatches.size() > 5) {
          gbgMatches = gbgMatches.subList(0, 4);
        }
        boolean domesticGBGFound = false;
        for (GBGResponse gbg : gbgMatches) {
          if (gbg.isDomesticGBG()) {
            domesticGBGFound = true;
            break;
          }
        }

        for (GBGResponse gbg : gbgMatches) {
          if (gbg.isDomesticGBG() || !domesticGBGFound) {
            data.setGbgId(gbg.getGbgId());
            data.setGbgDesc(gbg.getGbgName());
            data.setBgId(gbg.getBgId());
            data.setBgDesc(gbg.getBgName());
            data.setBgRuleId(gbg.getLdeRule());
            setBGValues(data, currentAddress);
            entityManager.merge(data);
            entityManager.flush();
            break;
          }
        }
      }
    } else {
      try {
        // List<GBGResponse> matches = new ArrayList<GBGResponse>();

        // do a matching against GU DUNS first
        String sql = ExternalizedQuery.getSql("GBG.QUERY_BY_CMRNO");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        query.setParameter("KATR6", data.getCmrIssuingCntry());
        query.setParameter("CMR_NO", dunsNo);
        query.setForReadOnly(true);

        // 0 gbg id
        // 1 gbg name
        // 2 bg id
        // 3 bg name
        // 4 lde rule
        // 5 int acct type
        // 6 cmr count
        List<Object[]> results = query.getResults();
        if (results != null) {
          GBGResponse gbg = null;
          for (Object[] result : results) {
            gbg = new GBGResponse();
            gbg.setGbgId((String) result[0]);
            gbg.setGbgName((String) result[1]);
            gbg.setBgId((String) result[2]);
            gbg.setBgName((String) result[3]);
            gbg.setLdeRule((String) result[4]);
            data.setGbgId(gbg.getGbgId());
            data.setGbgDesc(gbg.getGbgName());
            data.setBgId(gbg.getBgId());
            data.setBgDesc(gbg.getBgName());
            data.setBgRuleId(gbg.getLdeRule());
            setBGValues(data, currentAddress);
            entityManager.merge(data);
            entityManager.flush();
            break;
          }
        }
      } catch (Exception e) {
        LOG.error("CMR Error:" + e.getMessage());
      }
    }

  }

  private void setBGValues(Data data, Addr currentAddress) throws Exception {
    if ("GB300S7F".equals(data.getGbgId())) {
      if ("GZ".equals(currentAddress.getStateProv()) || "YN".equals(currentAddress.getStateProv()) || "GD".equals(currentAddress.getStateProv())
          || "SC".equals(currentAddress.getStateProv()) || "CQ".equals(currentAddress.getStateProv()) || "GX".equals(currentAddress.getStateProv())
          || "HI".equals(currentAddress.getStateProv())) {
        data.setBgId("DB002KDH");
        data.setBgDesc("RCCB SOUTH");
      } else if ("NM".equals(currentAddress.getStateProv()) || "SN".equals(currentAddress.getStateProv())
          || "HE".equals(currentAddress.getStateProv()) || "LN".equals(currentAddress.getStateProv()) || "NX".equals(currentAddress.getStateProv())
          || "BJ".equals(currentAddress.getStateProv()) || "GS".equals(currentAddress.getStateProv()) || "QH".equals(currentAddress.getStateProv())
          || "HA".equals(currentAddress.getStateProv()) || "TJ".equals(currentAddress.getStateProv()) || "HL".equals(currentAddress.getStateProv())
          || "XJ".equals(currentAddress.getStateProv()) || "JL".equals(currentAddress.getStateProv()) || "XZ".equals(currentAddress.getStateProv())
          || "SX".equals(currentAddress.getStateProv())) {
        data.setBgId("DB002CBD");
        data.setBgDesc("RCCB NORTH");
      } else if ("JS".equals(currentAddress.getStateProv()) || "JX".equals(currentAddress.getStateProv())
          || "AH".equals(currentAddress.getStateProv())) {
        data.setBgId("DB002C9T");
        data.setBgDesc("RCCB EAST1");
      } else if ("ZJ".equals(currentAddress.getStateProv()) || "HB".equals(currentAddress.getStateProv())
          || "HN".equals(currentAddress.getStateProv()) || "SH".equals(currentAddress.getStateProv()) || "FJ".equals(currentAddress.getStateProv())
          || "SD".equals(currentAddress.getStateProv())) {
        data.setBgId("DB002CF1");
        data.setBgDesc("RCCB EAST2");
      }
    }
  }

  /**
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @param currentAddress
   * @throws Exception
   * @deprecated - Use the new method
   *             {@link #getGBGIdViaLH(EntityManager, Admin, Data, Addr)}
   */
  @Deprecated
  private void getGBGId(EntityManager entityManager, Admin admin, Data data, Addr currentAddress) throws Exception {

    String companyName = null;
    if (StringUtils.isNotBlank(data.getBusnType())) {
      companyName = DnBUtil.getCNApiCompanyNameData4GBG(data.getBusnType());
    }
    if (StringUtils.isNotBlank(companyName)) {
      // 2, Check FindCMR NON Latin with Chinese name - single byte
      CompanyRecordModel searchModelFindCmrCN = new CompanyRecordModel();
      searchModelFindCmrCN.setIssuingCntry(data.getCmrIssuingCntry());
      searchModelFindCmrCN.setName(companyName);
      List<CompanyRecordModel> resultFindCmrCN = null;
      resultFindCmrCN = CompanyFinder.findCompanies(searchModelFindCmrCN);
      if (!resultFindCmrCN.isEmpty() && resultFindCmrCN.size() > 0) {
        ArrayList<String> cmrList = new ArrayList<String>();
        for (CompanyRecordModel cmr : resultFindCmrCN) {
          if (cmr.getAltName().endsWith(companyName)) {
            cmrList.add(cmr.getCmrNo());
            // break;
          }
        }
        if (cmrList.size() > 0) {
          for (int i = 0; i < cmrList.size(); i++) {
            String cmr = cmrList.get(i);
            getGBGIdByGBGservice(entityManager, admin, data, currentAddress, cmr, true);
            if (StringUtils.isNotBlank(data.getGbgId()) && StringUtils.isNotBlank(data.getBgId())) {
              break;
            }
          }
        }
        if (StringUtils.isBlank(data.getGbgId()) && StringUtils.isBlank(data.getBgId())) {
          getGBGIdByGBGservice(entityManager, admin, data, currentAddress, resultFindCmrCN.get(0).getDunsNo(), false);
        }
        // }
      }
    }

    // else {
    // getGBGIdByGBGservice(entityManager, admin, data, currentAddress, null,
    // false);
    // }
  }

  /**
   * Incrementally checks current DUNS and parent DUNS for GBG assignment.
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @param currentAddress
   * @throws Exception
   */
  private void getGBGIdViaLH(EntityManager entityManager, Admin admin, Data data, Addr currentAddress) throws Exception {
    String socialCreditCode = data.getBusnType();
    String dunsNo = data.getDunsNo();
    if (!StringUtils.isBlank(socialCreditCode)) {
      LOG.debug("Find DUNS using Social Credit Code " + socialCreditCode + " for " + currentAddress.getLandCntry());
      List<DnBMatchingResponse> matches = DnBUtil.findByOrgId(socialCreditCode, currentAddress.getLandCntry());
      if (!matches.isEmpty()) {
        dunsNo = matches.get(0).getDunsNo();
      }
    }
    boolean matchedFlag = false;
    while (!matchedFlag) {
      if (!StringUtils.isBlank(dunsNo)) {
        DnBCompany dnbData = DnBUtil.getDnBDetails(dunsNo);
        if (dnbData != null) {
          if (dunsNo != null && dunsNo.equals(dnbData.getDuDunsNo())) {
            LOG.debug("No Parent for DUNS " + dnbData.getDunsNo());
            break;
          } else {
            dunsNo = dnbData.getDuDunsNo();
          }
        } else {
          break;
        }
      } else {
        break;
      }
    }
    if (!StringUtils.isBlank(dunsNo)) {

      LOG.debug("Checking GBG assignment for DUNS " + dunsNo);
      // iterate here base duns, then going up one parent at a time to get
      // GBGs assigned
      String sql = ExternalizedQuery.getSql("CN.FIND_GBG");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("DUNS_NO", dunsNo);
      query.setForReadOnly(true);
      List<Object[]> results = query.getResults();
      if (results != null && !results.isEmpty()) {
        // found here, stop
        Object[] rec = results.get(0);

        String bgId = (String) rec[0];
        String bgDesc = (String) rec[1];
        String gbgId = (String) rec[2];
        String gbgDesc = (String) rec[3];
        String ldeRule = (String) rec[4];

        data.setGbgId(gbgId);
        data.setGbgDesc(gbgDesc);
        data.setBgId(bgId);
        data.setBgDesc(bgDesc);
        data.setBgRuleId(ldeRule);

        String currBgId = bgId;
        setBGValues(data, currentAddress);
        if (currBgId != null && currBgId.equals(data.getBgId())) {
          // bgId was not replaced, assign the LDE rules to fields
          LOG.debug("Assigning field values based on LDE rules..");
          GBGMatchingElement gbgElem = new GBGMatchingElement("", "", false, false);
          RequestData requestData = RequestData.wrap(admin, data, null, currentAddress);
          gbgElem.importLDE(entityManager, requestData, ldeRule);
        }
        entityManager.merge(data);
        entityManager.flush();
        matchedFlag = true;
      }
      if (matchedFlag) {
        LOG.debug("Matched =  GBG ID: " + data.getGbgId() + " BG ID: " + data.getBgId());
      } else {
        LOG.debug("No GBG ID found.");
      }

    } else {
      LOG.debug("No DUNS No. found for GBG matching.");
    }

  }

  /**
   * Comparator for {@link GBGResponse}
   * 
   * @author JeffZAMORA
   * 
   */
  private class GBGComparator implements Comparator<GBGResponse> {

    private String landedCountry;

    public GBGComparator(String landedCountry) {
      this.landedCountry = landedCountry;
    }

    @Override
    public int compare(GBGResponse o1, GBGResponse o2) {
      // matched cmrs on the country
      if (this.landedCountry.equals(o1.getCountry()) && !this.landedCountry.equals(o2.getCountry())) {
        return -1;
      }
      if (!this.landedCountry.equals(o1.getCountry()) && this.landedCountry.equals(o2.getCountry())) {
        return 1;
      }
      // cmr count
      if (o1.getCmrCount() > o2.getCmrCount()) {
        return -1;
      }
      if (o1.getCmrCount() < o2.getCmrCount()) {
        return 1;
      }

      // Null pointer exception encountered. when comparing using this.
      if (StringUtils.isNotBlank(o1.getLdeRule()) && StringUtils.isNotBlank(o2.getLdeRule())) {
        // rule is country specific
        if (o1.getLdeRule().contains(this.landedCountry) && !o2.getLdeRule().contains(this.landedCountry)) {
          return -1;
        }
        if (!o1.getLdeRule().contains(this.landedCountry) && o2.getLdeRule().contains(this.landedCountry)) {
          return 1;
        }
      }

      return o1.getBgId().compareTo(o2.getBgId());
    }

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

    List<GeoContactInfo> geoContactInfoList = new ArrayList<GeoContactInfo>();
    geoContactInfoList = getGeoContactInfoByReqId(entityManager, admin.getId().getReqId());

    if (geoContactInfoList != null && geoContactInfoList.size() > 0) {
      for (GeoContactInfo geoContactInfo : geoContactInfoList) {
        GeoContactInfo merged = entityManager.merge(geoContactInfo);
        if (merged != null) {
          entityManager.remove(merged);
        }
      }
      entityManager.flush();
    }

    if (intlAddrList != null && intlAddrList.size() > 0) {
      for (IntlAddr intlAddr : intlAddrList) {
        IntlAddr merged = entityManager.merge(intlAddr);
        if (merged != null) {
          entityManager.remove(merged);
        }
      }

      // remove INTL_ADDR_RDC too
      if (intlAddrRdcList != null && intlAddrRdcList.size() > 0) {
        for (IntlAddrRdc intlAddrRdc : intlAddrRdcList) {
          IntlAddrRdc merged = entityManager.merge(intlAddrRdc);
          if (merged != null) {
            entityManager.remove(merged);
          }
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
        String state = addr.getStateProv() != null ? addr.getStateProv().toUpperCase() : "";
        String cmtMsg = "Your address city value on address type " + addr.getId().getAddrType() + ", sequence number " + addr.getId().getAddrSeq()
            + " has been changed because the city value is not on CreateCMR.";
        if (!containsWhiteSpace(addr.getCity1()) && !StringUtils.isEmpty(city1Upper) && !CmrConstants.CN_NON_SPACED_CITIES.contains(city1Upper)
            && !"TREC".equals(admin.getReqReason())) {
          // 1. query code from LOV
          String cdOfUpperDesc = getCNCityCdByUpperDesc(entityManager, city1Upper, state);
          // 2. query desc from lov with the use of code from 1
          String enDesc = getCnCityEngDescById(entityManager, cdOfUpperDesc);
          // 3. save desc as addr.city1
          cmtMsg += "\n\nFrom: " + addr.getCity1() + "    To: " + enDesc;
          addr.setCity1(enDesc);

          if (StringUtils.isEmpty(enDesc)) {
            cmtMsg += "\n\nPlease note that you will not be able to submit the request for processing if you do not supply a value for your address city field.";
          }

          createCommentLog(entityManager, admin, cmtMsg);
        } else if (!"TREC".equals(admin.getReqReason())) {
          // ASSUMPTION: CITY CONTAINS WHITESPACE
          // 1. query desc from lov with the use of desc from toUpperCase
          String enDesc = getCNCityCdByUpperDesc(entityManager, city1Upper, state);
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

        String adrnr = getAddressAdrnr(entityManager, SystemConfiguration.getValue("MANDT"), cmr, addr.getId().getAddrType(),
            addr.getPairedAddrSeq());
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

        String qryKnvkAddr = ExternalizedQuery.getSql("GET_CONTINFO_ON_IMPORT_FOR_CN");
        PreparedQuery queryKnvK = new PreparedQuery(entityManager, qryKnvkAddr);
        queryKnvK.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        queryKnvK.setParameter("CMR", cmr);
        queryKnvK.setParameter("KATR6", SystemLocation.CHINA);
        queryKnvK.setParameter("KTOKD", addr.getId().getAddrType());
        queryKnvK.setParameter("ZZKV_SEQNO", addr.getPairedAddrSeq());

        List<Object[]> results = queryKnvK.getResults();

        if (results != null && !results.isEmpty()) {
          Object[] sResult = results.get(0);

          GeoContactInfo geoContactInfo = new GeoContactInfo();
          GeoContactInfoPK ePk = new GeoContactInfoPK();
          int contactId = 1;
          geoContactInfo.setContactPhone(sResult[0] != null ? sResult[0].toString() : "");
          geoContactInfo.setContactFunc(sResult[1] != null ? sResult[1].toString() : "");
          geoContactInfo.setContactName(sResult[2] != null ? sResult[2].toString() : "");
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

          // Phone #
          addr.setCustPhone(geoContactInfo.getContactPhone());
          entityManager.merge(addr);

          AddrRdc addrRdc = getAddrRdc(entityManager, addr.getId().getReqId(), addr.getId().getAddrType(), addr.getId().getAddrSeq());
          String contactName = geoContactInfo.getContactName();
          String contactTitle = geoContactInfo.getContactFunc();
          addrRdc.setDivn(contactName);
          addrRdc.setTaxOffice(contactTitle);
          entityManager.merge(addrRdc);
          entityManager.flush();
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

    if (!StringUtils.isBlank(admin.getReqType()) && admin.getReqType().equalsIgnoreCase("U") && !StringUtils.isBlank(admin.getReqStatus())
        && admin.getReqStatus().equalsIgnoreCase("DRA")) {
      if (data.getSearchTerm() == null || StringUtils.isBlank(data.getSearchTerm())
          || (data.getSearchTerm() != null && (data.getSearchTerm().trim().equalsIgnoreCase("00000") || data.getSearchTerm().matches("[^0-9]+")))) {
        if (data.getCmrNo().startsWith("1") || data.getCmrNo().startsWith("2")) {
          data.setClientTier("Z");
          data.setSearchTerm("00075");
        }
      }
    }
    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && StringUtils.isBlank(data.getDunsNo())) {
      getDunsNo(entityManager, data);
    }

    // Coverage 1H22 CREATCMR-4790, set expired Search Term 00000
    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      if (expiredSearchTerm(entityManager, data.getSearchTerm())) {
        if (Arrays.asList(CN_S_S_INAC_CODE).contains(data.getInacCd())) {
          data.setClientTier("0");
          data.setSearchTerm(getSSSearchTerm(entityManager, data.getInacCd()));
        } else if ("21".equals(data.getIsuCd()) || "60".equals(data.getIsuCd())) {
          data.setClientTier("Z");
          data.setSearchTerm("00000");
        } else if (isBPUser(data)) {
          data.setSearchTerm("00075");
        } else {
          data.setClientTier("Q");
          data.setSearchTerm("00000");
        }
      }
    }
  }

  private boolean expiredSearchTerm(EntityManager entityManager, String searchTerm) {
    if (!StringUtils.isEmpty(searchTerm)) {
      if (!existInList(entityManager, searchTerm)) {
        return true;
      }
    }
    return false;
  }

  private boolean existInList(EntityManager entityManager, String searchTerm) {
    if (StringUtils.isEmpty(searchTerm)) {
      return true;
    }
    if (searchTerm.length() > 5) {
      searchTerm = searchTerm.substring(0, 5);
    }
    String sql = ExternalizedQuery.getSql("QUERY.CHECK.CLUSTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", "641");
    query.setParameter("AP_CUST_CLUSTER_ID", searchTerm);
    List<String> result = query.getResults(String.class);
    if (result != null && !result.isEmpty()) {
      return true;
    }
    return false;
  }

  private String getSSSearchTerm(EntityManager entityManager, String inacCd) {
    if (StringUtils.isEmpty(inacCd)) {
      return null;
    }
    String searchTerm = "";
    String sql = ExternalizedQuery.getSql("QUERY.GET.CLUSTER_BY_INACCD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", "641");
    query.setParameter("INACCD", "%" + inacCd + "%");
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      searchTerm = results.get(0);
    }
    return searchTerm;

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
        "POST_CD", "LAND_CNTRY", "PO_BOX", "CUST_PHONE", "DIVN", "TAX_OFFICE"));
    return fields;
  }

  public static List<String> getAddressFieldsForBatchUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "DEPT", "ADDR_TXT", "ADDR_TXT_2", "BLDG", "OFFICE", "CITY1", "CITY2",
        "POST_CD", "LAND_CNTRY", "PO_BOX", "CUST_PHONE", "DIVN", "TAX_OFFICE"));
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
      } // namePart3 = temp2;

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

  private String getCNCityCdByUpperDesc(EntityManager entityManager, String cityDesc, String state) {
    String cdOfUpperDesc = "";
    List<Object[]> results = new ArrayList<Object[]>();
    String qryCNCityCdByUpperDesc = ExternalizedQuery.getSql("GET.CN_ENCITY_BY_UPPER_DESC");
    PreparedQuery query = new PreparedQuery(entityManager, qryCNCityCdByUpperDesc);
    String cityDescTemp = cityDesc + "%";
    query.setParameter("DESC", cityDescTemp);
    query.setParameter("FIELD_ID", CmrConstants.CN_CITIES_UPPER_FIELD_ID);
    query.setParameter("CNTRY", SystemLocation.CHINA);

    results = query.getResults();

    if (results != null && !results.isEmpty()) {
      if (results.size() == 1) {
        Object[] sResult = results.get(0);
        cdOfUpperDesc = sResult[1].toString();
      } else if (results.size() > 1) {
        // in case duplicate City name with different State/prov
        for (int i = 0; i < results.size(); i++) {
          Object[] sResult = results.get(i);
          String cityCd = sResult[1].toString();
          if (cityMatchState(cityCd, state)) {
            cdOfUpperDesc = sResult[1].toString();
          }
        }
      }

    }

    return cdOfUpperDesc;
  }

  private boolean cityMatchState(String cityCd, String state) {
    if (cityCd == null || state == null) {
      return false;
    }
    String temp = cityCd.substring(0, 2);
    if (state.equals(temp)) {
      return true;
    }
    return false;
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

  @Override
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
    aCnDistrict = aCnDistrict != null ? aCnDistrict.trim().toLowerCase() : "";
    aCnCustName = aCnCustName != null ? aCnCustName.trim().toLowerCase() : "";
    aCnCustNameCont = aCnCustNameCont != null ? aCnCustNameCont.trim().toLowerCase() : "";
    aCnStreetAddrTxt = aCnStreetAddrTxt != null ? aCnStreetAddrTxt.trim().toLowerCase() : "";
    aCnStreetAddrTxt2 = aCnStreetAddrTxt2 != null ? aCnStreetAddrTxt2.trim().toLowerCase() : "";

    String aDept = addr.getDept() != null ? addr.getDept().trim().toLowerCase() : "";
    String aOfc = addr.getOffice() != null ? addr.getOffice().trim().toLowerCase() : "";
    String aPostBox = addr.getPoBox() != null ? addr.getPoBox().trim().toLowerCase() : "";
    // String aPhoneNum = addr.getCustPhone() != null ?
    // addr.getCustPhone().trim().toLowerCase() : "";
    String aTransportZone = addr.getTransportZone() != null ? addr.getTransportZone().trim().toLowerCase() : "";

    String mAddrTxt = model.getAddrTxt() != null ? model.getAddrTxt().trim().toLowerCase() : "";
    String mAddrTxt2 = model.getAddrTxt2() != null ? model.getAddrTxt2().trim().toLowerCase() : "";
    String mCity1 = model.getCity1DrpDown() != null ? model.getCity1DrpDown().trim().toLowerCase() : "";
    String mCity2 = model.getCity2() != null ? model.getCity2().trim().toLowerCase() : "";
    String mBldg = model.getBldg() != null ? model.getBldg().trim().toLowerCase() : "";
    String mStateProv = model.getStateProv() != null ? model.getStateProv().trim().toLowerCase() : "";
    String mPostCd = model.getPostCd() != null ? model.getPostCd().trim().toLowerCase() : "";
    String mCnDistrict = model.getCnDistrict() != null ? model.getCnDistrict().trim().toLowerCase() : "";
    String mDept = model.getDept() != null ? model.getDept().trim().toLowerCase() : "";
    String mOfc = model.getOffice() != null ? model.getOffice().trim().toLowerCase() : "";
    String mPostBox = model.getPoBox() != null ? model.getPoBox().trim().toLowerCase() : "";
    // String mPhoneNum = model.getCustPhone() != null ?
    // model.getCustPhone().trim().toLowerCase() : "";
    String mTransportZone = model.getTransportZone() != null ? model.getTransportZone().trim().toLowerCase() : "";
    String mCnCustName = model.getCnCustName1() != null ? model.getCnCustName1().trim().toLowerCase() : "";
    String mCnCustNameCont = model.getCnCustName2() != null ? model.getCnCustName2().trim().toLowerCase() : "";
    String mCnStreetAddrTxt = model.getCnAddrTxt() != null ? model.getCnAddrTxt().trim().toLowerCase() : "";
    String mCnStreetAddrTxt2 = model.getCnAddrTxt2() != null ? model.getCnAddrTxt2().trim().toLowerCase() : "";

    LOG.debug(aAddrTxt);
    LOG.debug(mAddrTxt);
    LOG.debug(aAddrTxt2);
    LOG.debug(mAddrTxt2);
    LOG.debug(aCity1);
    LOG.debug(mCity1);
    LOG.debug(aCity2);
    LOG.debug(mCity2);
    LOG.debug(aBldg);
    LOG.debug(mBldg);
    LOG.debug(aStateProv);
    LOG.debug(mStateProv);
    LOG.debug(aPostCd);
    LOG.debug(mPostCd);
    LOG.debug(aCnDistrict);
    LOG.debug(mCnDistrict);
    LOG.debug(aCnCustName);
    LOG.debug(mCnCustName);
    LOG.debug(aCnCustNameCont);
    LOG.debug(mCnCustNameCont);
    LOG.debug(aCnStreetAddrTxt);
    LOG.debug(mCnStreetAddrTxt);
    LOG.debug(aCnStreetAddrTxt2);
    LOG.debug(mCnStreetAddrTxt2);
    LOG.debug(aDept);
    LOG.debug(mDept);
    LOG.debug(aOfc);
    LOG.debug(mOfc);
    LOG.debug(aPostBox);
    LOG.debug(mPostBox);
    LOG.debug(aTransportZone);
    LOG.debug(mTransportZone);

    if (!StringUtils.equals(aAddrTxt, mAddrTxt) || !StringUtils.equals(aAddrTxt2, mAddrTxt2) || !StringUtils.equals(aCity1, mCity1)
        || !StringUtils.equals(aCity2, mCity2) || !StringUtils.equals(aBldg, mBldg) || !StringUtils.equals(aStateProv, mStateProv)
        || !StringUtils.equals(aPostCd, mPostCd) || !StringUtils.equals(aCnDistrict, mCnDistrict) || !StringUtils.equals(aDept, mDept)
        || !StringUtils.equals(aOfc, mOfc) || !StringUtils.equals(aPostBox, mPostBox) || !StringUtils.equals(aTransportZone, mTransportZone)
        || !StringUtils.equals(aCnCustName, mCnCustName) || !StringUtils.equals(aCnCustNameCont, mCnCustNameCont)
        || !StringUtils.equals(aCnStreetAddrTxt, mCnStreetAddrTxt) || !StringUtils.equals(aCnStreetAddrTxt2, mCnStreetAddrTxt2)) {
      LOG.debug("isClearDPL=true");
      return true;
    }
    LOG.debug("isClearDPL=false");
    return false;
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {

    String addrType = addr.getId().getAddrType();
    String seqNo = addr.getId().getAddrSeq();

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
      if (!equals(convert2DBCS(iAddr.getIntlCustNm1()), convert2DBCS(iAddrRdc.getIntlCustNm1()))) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrTypeCode(addrType);
        update.setAddrSeq(seqNo);
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaCustomerName1", "-"));
        update.setNewData(iAddr.getIntlCustNm1());
        update.setOldData(iAddrRdc.getIntlCustNm1());
        results.add(update);
      }
      if (!equals(convert2DBCS(iAddr.getIntlCustNm2()), convert2DBCS(iAddrRdc.getIntlCustNm2()))) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrTypeCode(addrType);
        update.setAddrSeq(seqNo);
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaCustomerName2", "-"));
        update.setNewData(iAddr.getIntlCustNm2());
        update.setOldData(iAddrRdc.getIntlCustNm2());
        results.add(update);
      }
      if (!equals(convert2DBCS(iAddr.getIntlCustNm3()), convert2DBCS(iAddrRdc.getIntlCustNm3()))) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrTypeCode(addrType);
        update.setAddrSeq(seqNo);
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaCustomerName3", "-"));
        update.setNewData(iAddr.getIntlCustNm3());
        update.setOldData(iAddrRdc.getIntlCustNm3());
        results.add(update);
      }
      if (!equals(convert2DBCS(iAddr.getAddrTxt()), convert2DBCS(iAddrRdc.getAddrTxt()))) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrTypeCode(addrType);
        update.setAddrSeq(seqNo);
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaStreetAddress1", "-"));
        update.setNewData(iAddr.getAddrTxt());
        update.setOldData(iAddrRdc.getAddrTxt());
        results.add(update);
      }
      if (!equals(convert2DBCS(iAddr.getIntlCustNm4()), convert2DBCS(iAddrRdc.getIntlCustNm4()))) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrTypeCode(addrType);
        update.setAddrSeq(seqNo);
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaStreetAddress2", "-"));
        update.setNewData(iAddr.getIntlCustNm4());
        update.setOldData(iAddrRdc.getIntlCustNm4());
        results.add(update);
      }
      // city
      if (!equals(convert2DBCS(iAddr.getCity1()), convert2DBCS(iAddrRdc.getCity1()))) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrTypeCode(addrType);
        update.setAddrSeq(seqNo);
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "DropDownCityChina", "-"));
        update.setNewData(iAddr.getCity1());
        update.setOldData(iAddrRdc.getCity1());
        results.add(update);
      }
      // district
      if (!equals(convert2DBCS(iAddr.getCity2()), convert2DBCS(iAddrRdc.getCity2()))) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrTypeCode(addrType);
        update.setAddrSeq(seqNo);
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaCity2", "-"));
        update.setNewData(iAddr.getCity2());
        update.setOldData(iAddrRdc.getCity2());
        results.add(update);
      }
    }

    List<GeoContactInfo> geoAddrList = getGeoContactInfoByReqId(entityManager, addr.getId().getReqId());
    GeoContactInfo geoAddr = null;
    for (GeoContactInfo gAddr : geoAddrList) {
      if (gAddr.getContactType().equals(addr.getId().getAddrType()) && gAddr.getContactSeqNum().equals(addr.getId().getAddrSeq())) {
        geoAddr = gAddr;
      }
    }
    if (geoAddr != null) {
      // contact name
      if (!equals(convert2DBCS(geoAddr.getContactName()), convert2DBCS(addr.getDivnOld()))) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrTypeCode(addrType);
        update.setAddrSeq(seqNo);
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "ChinaCustomerCntName", "-"));
        update.setNewData(geoAddr.getContactName());
        update.setOldData(addr.getDivnOld());
        results.add(update);
      }
      // contact title
      if (!equals(convert2DBCS(geoAddr.getContactFunc()), convert2DBCS(addr.getTaxOffice()))) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrTypeCode(addrType);
        update.setAddrSeq(seqNo);
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "CustomerCntJobTitle", "-"));
        update.setNewData(geoAddr.getContactFunc());
        update.setOldData(addr.getTaxOffice());
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
    if (sequence != null) {
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

  public static List<Addr> getAddrByReqId(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("QUERY.ADDR_BY_REQ_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Addr> addrList;
    try {
      addrList = query.getResults(Addr.class);
    } catch (Exception ex) {
      LOG.error("An error occured in getting the ADDR records");
      throw ex;
    }
    return addrList;
  }

  private List<GeoContactInfo> getGeoContactInfoByReqId(EntityManager entityManager, long reqId) {
    // TODO Auto-generated method stub
    String sql = ExternalizedQuery.getSql("CONTACTINFO.FINDALL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<GeoContactInfo> geoContactInfoList;
    try {
      geoContactInfoList = query.getResults(GeoContactInfo.class);
    } catch (Exception ex) {
      LOG.error("An error occured in getting the GEO_ADL_CONT_DTL records");
      throw ex;
    }
    return geoContactInfoList;
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
    reqCmtLog.setCreateById("CreateCMR");
    reqCmtLog.setCreateByNm("CreateCMR");
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    em.persist(reqCmtLog);

  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
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

  @Override
  public String getCMRNo(EntityManager rdcMgr, String kukla, String mandt, String katr6, String cmrNo, CmrCloningQueue cloningQueue) {
    LOG.debug("getChinaCMR :: START");
    List<Object[]> results;
    String cmr = "";
    String sql = ExternalizedQuery.getSql("GET.KEY_AUTO_GEN.SEQNO");
    sql = StringUtils.replaceOnce(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replaceOnce(sql, ":KATR6", "'" + katr6 + "'");

    if (CmrConstants.CN_KUKLA81.equals(kukla) || CmrConstants.CN_KUKLA85.equals(kukla)) {
      LOG.debug("getChinaCMR :: RETRIEVING KUKLA 81 or 85 CMR");
      sql = StringUtils.replaceOnce(sql, ":KEYID", "'" + CmrConstants.CN_KUKLA81_KEYID + "'");
    } else if (CmrConstants.CN_KUKLA45.equals(kukla)) {
      LOG.debug("getChinaCMR :: RETRIEVING KUKLA 45 CMR");
      sql = StringUtils.replaceOnce(sql, ":KEYID", "'" + CmrConstants.CN_KUKLA45_KEYID + "'");
    } else { // use default key_id
      LOG.debug("getChinaCMR :: RETRIEVING DEFAULT CMR");
      sql = StringUtils.replaceOnce(sql, ":KEYID", "'" + CmrConstants.CN_DEFAULT_KEYID + "'");
    }

    results = rdcMgr.createNativeQuery(sql).getResultList();
    if (results != null && results.size() > 0) {
      String tempcmr = (String) results.get(0)[2];

      if (tempcmr != null) {
        int num = new Integer(tempcmr).intValue();
        num = num + 1;
        cmr = String.valueOf(num);
      }
    }
    LOG.debug("getChinaCMR :: GENERATED CMR >> " + cmr + " :: END");
    LOG.debug("getChinaCMR :: END");
    return cmr;
  }

  public void convertChinaStateNameToStateCode(Addr addr, FindCMRRecordModel cmr, EntityManager entityManager) {
    LOG.debug("Convert China StateName to StateCode Begin >>>");
    String stateCode = null;
    String stateName = cmr.getCmrState().trim();
    stateName = stateName != null ? stateName + "%" : "";
    List<Object[]> results = new ArrayList<Object[]>();
    String cnStateProvCD = ExternalizedQuery.getSql("GET.CN_STATE_PROV_CD");
    PreparedQuery query = new PreparedQuery(entityManager, cnStateProvCD);
    query.setParameter("STATE_PROV_DESC", stateName);
    results = query.getResults();
    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      stateCode = sResult[0].toString();
    }
    if (StringUtils.isNotBlank(stateCode)) {
      addr.setStateProv(stateCode);
      cmr.setCmrState(stateCode);
      LOG.debug("Convert China StateName to StateCode End >>>");
    }
  }

  public void setCNAddressENCityOnImport(Addr addr, FindCMRRecordModel cmr, EntityManager entityManager) {
    // TODO Auto-generated method stub
    LOG.debug("Convert China ENCITY Begin >>>");
    String city1Upper = addr.getCity1() != null ? addr.getCity1().toUpperCase() : "";
    city1Upper = city1Upper.replaceAll("[^a-zA-Z]+", "");
    String state = addr.getStateProv() != null ? addr.getStateProv().toUpperCase() : "";
    if (!containsWhiteSpace(addr.getCity1()) && !StringUtils.isEmpty(city1Upper) && !CmrConstants.CN_NON_SPACED_CITIES.contains(city1Upper)) {
      // 1. query code from LOV
      String cdOfUpperDesc = getCNCityCdByUpperDesc(entityManager, city1Upper, state);
      // 2. query desc from lov with the use of code from 1
      String enDesc = getCnCityEngDescById(entityManager, cdOfUpperDesc);
      // 3. save desc as addr.city1
      addr.setCity1(enDesc);
      cmr.setCmrIntlCity1(enDesc);
      LOG.debug("Convert China ENCITY End >>> enCity is " + enDesc);
    } else {
      // ASSUMPTION: CITY CONTAINS WHITESPACE
      // 1. query desc from lov with the use of desc from toUpperCase
      String enDesc = getCNCityCdByUpperDesc(entityManager, city1Upper, state);
      // 2. save desc as addr.city1
      if (StringUtils.isEmpty(enDesc) || StringUtils.isBlank(enDesc)) {
        addr.setCity1(enDesc);
      } else {
        String newCity = getCnCityEngDescById(entityManager, enDesc);
        addr.setCity1(newCity);
        cmr.setCmrIntlCity1(enDesc);
        LOG.debug("Convert China ENCITY End >>> enCity is " + newCity);
      }
    }
  }

  public void setCNAddressCityOnImport(AddressModel model, FindCMRRecordModel cmr, Addr addr, EntityManager entityManager) {
    // TODO Auto-generated method stub
    LOG.debug("Convert China CNCITY Begin >>>");
    String stateProv = addr.getStateProv();
    String cityTxt = addr.getCity1();
    String cityCode = null;
    String qryChinaCityID = ExternalizedQuery.getSql("GET.GEO_CITIES_BYID");
    stateProv += "%";
    List<Object[]> results = new ArrayList<Object[]>();
    PreparedQuery query = new PreparedQuery(entityManager, qryChinaCityID);
    query.setParameter("CITY_DESC", cityTxt);
    query.setParameter("ISSUING_CNTRY", SystemLocation.CHINA);
    query.setParameter("STATE_CD", stateProv);
    results = query.getResults();
    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      cityCode = sResult[1].toString();
    }
    LOG.debug("Convert China CNCITY Begin get cityCode is " + cityCode);
    String cnCity = null;
    if (StringUtils.isNotBlank(cityCode)) {
      String qryChinaCityTxt = ExternalizedQuery.getSql("GET.CN_CITY_TXT");
      PreparedQuery queryCity = new PreparedQuery(entityManager, qryChinaCityTxt);
      queryCity.setParameter("CD", cityCode);
      List<Object[]> results2 = queryCity.getResults();
      if (results2 != null && !results2.isEmpty()) {
        Object[] sResult = results2.get(0);
        cnCity = sResult[0].toString();
      }
    }
    LOG.debug("Convert China CNCITY End >>> cnCity is " + cnCity);
    if (StringUtils.isNotEmpty(cnCity)) {
      model.setCnCity(cnCity);
      cmr.setCmrIntlCity1(cnCity);
    }
  }

  public String convert2DBCS(String value) {
    String modifiedVal = null;
    if (value != null && value.length() > 0) {
      modifiedVal = value;
      modifiedVal = modifiedVal.replace("1", "１");
      modifiedVal = modifiedVal.replace("2", "２");
      modifiedVal = modifiedVal.replace("3", "３");
      modifiedVal = modifiedVal.replace("4", "４");
      modifiedVal = modifiedVal.replace("5", "５");
      modifiedVal = modifiedVal.replace("6", "６");
      modifiedVal = modifiedVal.replace("7", "７");
      modifiedVal = modifiedVal.replace("8", "８");
      modifiedVal = modifiedVal.replace("9", "９");
      modifiedVal = modifiedVal.replace("0", "０");
      modifiedVal = modifiedVal.replace("a", "ａ");
      modifiedVal = modifiedVal.replace("b", "ｂ");
      modifiedVal = modifiedVal.replace("c", "ｃ");
      modifiedVal = modifiedVal.replace("d", "ｄ");
      modifiedVal = modifiedVal.replace("e", "ｅ");
      modifiedVal = modifiedVal.replace("f", "ｆ");
      modifiedVal = modifiedVal.replace("g", "ｇ");
      modifiedVal = modifiedVal.replace("h", "ｈ");
      modifiedVal = modifiedVal.replace("i", "ｉ");
      modifiedVal = modifiedVal.replace("j", "ｊ");
      modifiedVal = modifiedVal.replace("k", "ｋ");
      modifiedVal = modifiedVal.replace("l", "ｌ");
      modifiedVal = modifiedVal.replace("m", "ｍ");
      modifiedVal = modifiedVal.replace("n", "ｎ");
      modifiedVal = modifiedVal.replace("o", "ｏ");
      modifiedVal = modifiedVal.replace("p", "ｐ");
      modifiedVal = modifiedVal.replace("q", "ｑ");
      modifiedVal = modifiedVal.replace("r", "ｒ");
      modifiedVal = modifiedVal.replace("s", "ｓ");
      modifiedVal = modifiedVal.replace("t", "ｔ");
      modifiedVal = modifiedVal.replace("u", "ｕ");
      modifiedVal = modifiedVal.replace("v", "ｖ");
      modifiedVal = modifiedVal.replace("w", "ｗ");
      modifiedVal = modifiedVal.replace("x", "ｘ");
      modifiedVal = modifiedVal.replace("y", "ｙ");
      modifiedVal = modifiedVal.replace("z", "ｚ");
      modifiedVal = modifiedVal.replace("A", "Ａ");
      modifiedVal = modifiedVal.replace("B", "Ｂ");
      modifiedVal = modifiedVal.replace("C", "Ｃ");
      modifiedVal = modifiedVal.replace("D", "Ｄ");
      modifiedVal = modifiedVal.replace("E", "Ｅ");
      modifiedVal = modifiedVal.replace("F", "Ｆ");
      modifiedVal = modifiedVal.replace("G", "Ｇ");
      modifiedVal = modifiedVal.replace("H", "Ｈ");
      modifiedVal = modifiedVal.replace("I", "Ｉ");
      modifiedVal = modifiedVal.replace("J", "Ｊ");
      modifiedVal = modifiedVal.replace("K", "Ｋ");
      modifiedVal = modifiedVal.replace("L", "Ｌ");
      modifiedVal = modifiedVal.replace("M", "Ｍ");
      modifiedVal = modifiedVal.replace("N", "Ｎ");
      modifiedVal = modifiedVal.replace("O", "Ｏ");
      modifiedVal = modifiedVal.replace("P", "Ｐ");
      modifiedVal = modifiedVal.replace("Q", "Ｑ");
      modifiedVal = modifiedVal.replace("R", "Ｒ");
      modifiedVal = modifiedVal.replace("S", "Ｓ");
      modifiedVal = modifiedVal.replace("T", "Ｔ");
      modifiedVal = modifiedVal.replace("U", "Ｕ");
      modifiedVal = modifiedVal.replace("V", "Ｖ");
      modifiedVal = modifiedVal.replace("W", "Ｗ");
      modifiedVal = modifiedVal.replace("X", "Ｘ");
      modifiedVal = modifiedVal.replace("Y", "Ｙ");
      modifiedVal = modifiedVal.replace("Z", "Ｚ");
      modifiedVal = modifiedVal.replace(" ", "　");
      modifiedVal = modifiedVal.replace("&", "＆");
      modifiedVal = modifiedVal.replace("-", "－");
      modifiedVal = modifiedVal.replace(".", "．");
      modifiedVal = modifiedVal.replace(",", "，");
      modifiedVal = modifiedVal.replace(":", "：");
      modifiedVal = modifiedVal.replace("_", "＿");
      modifiedVal = modifiedVal.replace("(", "（");
      modifiedVal = modifiedVal.replace(")", "）");
    }
    return modifiedVal;
  }

  private boolean updateCNIntlAddr(IntlAddr intlAddr, EntityManager entityManager) {

    int tempNewLen = 0;
    String newTxt = "";

    if (StringUtils.isNotBlank(intlAddr.getAddrTxt()) && CNHandler.getLengthInUtf8(intlAddr.getAddrTxt()) > CNHandler.CN_STREET_ADD_TXT) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(intlAddr.getAddrTxt(), CNHandler.CN_STREET_ADD_TXT, CNHandler.CN_STREET_ADD_TXT);
      newTxt = intlAddr.getAddrTxt() != null ? intlAddr.getAddrTxt().substring(0, tempNewLen) : "";
      String excess = intlAddr.getAddrTxt().substring(tempNewLen);
      intlAddr.setAddrTxt(newTxt);
      intlAddr.setIntlCustNm4(excess + (intlAddr.getIntlCustNm4() != null ? intlAddr.getIntlCustNm4() : ""));
    }

    if (StringUtils.isNotBlank(intlAddr.getIntlCustNm4()) && CNHandler.getLengthInUtf8(intlAddr.getIntlCustNm4()) > CNHandler.CN_STREET_ADD_TXT2) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(intlAddr.getIntlCustNm4(), CNHandler.CN_STREET_ADD_TXT2, CNHandler.CN_STREET_ADD_TXT2);
      newTxt = intlAddr.getIntlCustNm4() != null ? intlAddr.getIntlCustNm4().substring(0, tempNewLen) : "";
      intlAddr.setIntlCustNm4(newTxt);
    }

    if (StringUtils.isNotBlank(intlAddr.getIntlCustNm1()) && CNHandler.getLengthInUtf8(intlAddr.getIntlCustNm1()) > CNHandler.CN_CUST_NAME_1) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(intlAddr.getIntlCustNm1(), CNHandler.CN_CUST_NAME_1, CNHandler.CN_CUST_NAME_1);
      newTxt = intlAddr.getIntlCustNm1() != null ? intlAddr.getIntlCustNm1().substring(0, tempNewLen) : "";
      String excess = intlAddr.getIntlCustNm1().substring(tempNewLen);
      intlAddr.setIntlCustNm1(newTxt);
      intlAddr.setIntlCustNm2(excess + (intlAddr.getIntlCustNm2() != null ? intlAddr.getIntlCustNm2() : ""));
    }

    if (StringUtils.isNotBlank(intlAddr.getIntlCustNm2()) && CNHandler.getLengthInUtf8(intlAddr.getIntlCustNm2()) > CNHandler.CN_CUST_NAME_2) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(intlAddr.getIntlCustNm2(), CNHandler.CN_CUST_NAME_2, CNHandler.CN_CUST_NAME_2);
      newTxt = intlAddr.getIntlCustNm2() != null ? intlAddr.getIntlCustNm2().substring(0, tempNewLen) : "";
      intlAddr.setIntlCustNm2(newTxt);
    }

    if (StringUtils.isNotBlank(intlAddr.getIntlCustNm3()) && CNHandler.getLengthInUtf8(intlAddr.getIntlCustNm3()) > CNHandler.CN_CUST_NAME_2) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(intlAddr.getIntlCustNm3(), CNHandler.CN_CUST_NAME_2, CNHandler.CN_CUST_NAME_2);
      newTxt = intlAddr.getIntlCustNm3() != null ? intlAddr.getIntlCustNm3().substring(0, tempNewLen) : "";
      intlAddr.setIntlCustNm3(newTxt);
    }

    if (StringUtils.isNotBlank(intlAddr.getCity2()) && CNHandler.getLengthInUtf8(intlAddr.getCity2()) > CNHandler.CN_CUST_NAME_2) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(intlAddr.getCity2(), CNHandler.CN_CUST_NAME_2, CNHandler.CN_CUST_NAME_2);
      newTxt = intlAddr.getCity2() != null ? intlAddr.getCity2().substring(0, tempNewLen) : "";
      intlAddr.setCity2(newTxt);
    }

    return true;
  }

  @Override
  public void handleMEUCondApproval(EntityManager entityManager, ApprovalResponseModel approval, Data data) {
    data.setCustAcctType("US");
    data.setBioChemMissleMfg(
        SystemUtil.getCurrentTimestamp() != null ? SystemUtil.getCurrentTimestamp().toString().substring(0, 10).replaceAll("-", "") : "");
    data.setIcmsInd("Y");
    // TODO Auto-generated method stub
    if (approval != null && approval.getComments() != null && approval.getComments().contains("MEU")) {
      data.setMilitary("Y");
    }
    entityManager.merge(data);
    entityManager.flush();
  }

  public static void doBeforeSendForProcessing(EntityManager entityManager, Admin admin, Data data, RequestEntryModel model) {
    if (shouldSetAsterisk()) {
      setAddrNmAsterisk(entityManager, admin, data);
    }
    // if (model.getReqType() != null && model.getReqType().equals("U") &&
    // (data.getCapInd() == null || !data.getCapInd().equals("Y"))) {
    // getIsicByDNB(entityManager, data);
    // } else
    if (model.getReqType().equals("C") && model.getCustSubGrp() != null
        && !(model.getCustSubGrp().equals("INTER") || model.getCustSubGrp().equals("BUSPR") || model.getCustSubGrp().equals("PRIV"))) {
      getIsicByDNB(entityManager, data);
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && StringUtils.isBlank(data.getIsicCd())
        && (data.getCapInd() == null || data.getCapInd().equals("N"))) {
      getIsicByDNB(entityManager, data);
    }
  }

  private static void getDunsNo(EntityManager entityManager, Data data) {

    String dunsNo = null;
    RequestData requestData = new RequestData(entityManager, data.getId().getReqId());
    MatchingResponse<DnBMatchingResponse> response = null;
    try {
      response = DnBUtil.getMatches(requestData, null, "ZS01");
      if (response != null && DnBUtil.hasValidMatches(response)) {
        DnBMatchingResponse dnbRecord = response.getMatches().get(0);
        if (dnbRecord.getConfidenceCode() >= 8) {
          dunsNo = dnbRecord.getDunsNo();
        }
      }
    } catch (Exception e) {
      LOG.error("Error occured on get dunsNo from DNB.", e);
    }

    data.setDunsNo(dunsNo);
  }

  public static void getIsicByDNB(EntityManager entityManager, Data data) {

    String dunsNo = null;
    if (StringUtils.isBlank(data.getDunsNo())) {
      RequestData requestData = new RequestData(entityManager, data.getId().getReqId());
      MatchingResponse<DnBMatchingResponse> response = null;
      try {
        response = DnBUtil.getMatches(requestData, null, "ZS01");
      } catch (Exception e) {
        LOG.error("Error occured on get dunsNo from DNB.", e);
      }
      if (response != null && DnBUtil.hasValidMatches(response)) {
        DnBMatchingResponse dnbRecord = response.getMatches().get(0);
        if (dnbRecord.getConfidenceCode() >= 8) {
          dunsNo = dnbRecord.getDunsNo();
        }
      }
    } else {
      dunsNo = data.getDunsNo();
    }
    if (StringUtils.isNotBlank(dunsNo)) {
      DnBCompany dnbData = null;
      try {
        dnbData = DnBUtil.getDnBDetails(dunsNo);
      } catch (Exception e) {
        LOG.error("Error occured on get ISIC from DNB.", e);
      }
      if (dnbData != null && StringUtils.isNotBlank(dnbData.getIbmIsic())) {
        if (!dnbData.getIbmIsic().equals(data.getIsicCd())) {
          data.setIsicCd(dnbData.getIbmIsic());
          entityManager.merge(data);
          entityManager.flush();
        }
      }
    }
  }

  private static boolean shouldSetAsterisk() {
    return true;
  }

  private static void setAddrNmAsterisk(EntityManager entityManager, Admin admin, Data data) {
    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      return;
    }
    GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    List<Addr> addresses = getAddrByReqId(entityManager, data.getId().getReqId());
    if (addresses != null && addresses.size() > 0) {
      for (Addr addr : addresses) {
        if ("PRIV".equals(data.getCustSubGrp()) || "INTER".equals(data.getCustSubGrp()) || "AQSTN".equals(data.getCustSubGrp())) {
          IntlAddr intlAddr = handler.getIntlAddrById(addr, entityManager);
          if (intlAddr != null) {
            if (StringUtils.isBlank(intlAddr.getIntlCustNm1())) {
              intlAddr.setIntlCustNm1("*");
            }
            entityManager.merge(intlAddr);
            entityManager.flush();
          } else {
            // TODO
            // creteIntlAddr();
          }
        } else if ("CROSS".equals(data.getCustSubGrp())) {
          IntlAddr intlAddr = handler.getIntlAddrById(addr, entityManager);
          if (intlAddr != null) {
            if (StringUtils.isBlank(intlAddr.getIntlCustNm1())) {
              intlAddr.setIntlCustNm1("*");
            }
            if (StringUtils.isBlank(intlAddr.getCity1())) {
              intlAddr.setCity1("*");
            }
            entityManager.merge(intlAddr);
            entityManager.flush();
          } else {
            // TODO
            // creteIntlAddr();
          }
        }
      }
    }
  }

  private AddrRdc getAddrRdc(EntityManager entityManager, long reqId, String addrType, String addrSeq) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ADDRRDC.SEARCH_BY_REQID_TYPE_SEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    query.setParameter("ADDR_SEQ", addrSeq);
    query.setForReadOnly(true);
    return query.getSingleResult(AddrRdc.class);
  }

  @Override
  public void setReqStatusAfterApprove(EntityManager entityManager, ApprovalResponseModel approval, ApprovalReq req, Admin admin) {
    String reqTypeId = String.valueOf(req.getTypId());
    String defaultApprovalId = String.valueOf(req.getDefaultApprovalId());
    String approvalDesc = getApprovalDesc(entityManager, defaultApprovalId);
    LOG.debug(">>> CN setReqStatusAfterApprove...");
    LOG.debug("reqTypeId = " + reqTypeId);
    LOG.debug("defaultApprovalId = " + defaultApprovalId);
    LOG.debug("approvalDesc = " + approvalDesc);
    LOG.debug("currentStatus = " + approval.getCurrentStatus());
    LOG.debug("processing = " + approval.getProcessing());
    // CREATCMR-6548
    String reqScenario = getSubScenario(entityManager, admin.getId().getReqId());
    if ("CROSS".equalsIgnoreCase(reqScenario)) {
      return;
    }

    if (approvalDesc != null
        && (CmrConstants.CN_ERO_APPROVAL_DESC.equals(approvalDesc) || CmrConstants.CN_ECO_LEADER_APPROVAL_DESC.equals(approvalDesc)
            || CmrConstants.CN_TECH_LEADER_APPROVAL_DESC.equals(approvalDesc))
        && "Approved".equals(approval.getCurrentStatus()) && "Y".equals(approval.getProcessing()) && "PPN".equals(admin.getReqStatus())) {
      LOG.debug("Setting request " + req.getTypId() + " to automation process");
      admin.setReqStatus(AutomationConst.STATUS_AUTOMATED_PROCESSING);
    }
  }

  private String getApprovalDesc(EntityManager entityManager, String id) {
    String sql = ExternalizedQuery.getSql("SYSTEM.GET_DEFAULT_APPR.DETAILS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", id);
    DefaultApprovals approve = query.getSingleResult(DefaultApprovals.class);
    if (approve != null) {
      return approve.getDefaultApprovalDesc();
    }
    return null;
  }

  // CREATCMR-6548
  private String getSubScenario(EntityManager entityManager, long id) {
    String subScenario = "";
    List<Object[]> results = new ArrayList<Object[]>();
    String sql = ExternalizedQuery.getSql("QUERY.DATA.GET.CUSTSUBGRP.BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", id);
    results = query.getResults();
    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      subScenario = sResult[0] != null ? sResult[0].toString() : "";
    }
    return subScenario;
  }

}
