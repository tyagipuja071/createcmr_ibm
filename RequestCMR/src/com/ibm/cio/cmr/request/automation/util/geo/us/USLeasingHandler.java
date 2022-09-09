package com.ibm.cio.cmr.request.automation.util.geo.us;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.CopyAttachmentUtil;
import com.ibm.cio.cmr.request.automation.util.DummyServletRequest;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * 
 * @author Shivangi
 *
 */
public class USLeasingHandler extends USBPHandler {
  private static final Logger LOG = Logger.getLogger(USLeasingHandler.class);

  @Override
  public boolean doInitialValidations(Admin admin, Data data, Addr addr, AutomationResult<OverrideOutput> output, AutomationEngineData engineData) {
    // check the scenario
    String custGrp = data.getCustGrp();
    String custSubGrp = data.getCustSubGrp();
    if ("BYMODEL".equals(custSubGrp)) {
      String type = admin.getCustType();
      if (!USUtil.LEASING.equals(type)) {
        output.setResults("Skipped");
        output.setDetails("Non Leasing create by model scenario not supported.");
        return true;
      }
    } else if (!TYPE_LEASING.equals(custGrp) || ((!SUB_TYPE_LEASE_SVR_CONT.equals(custSubGrp)) && (!SUB_TYPE_LEASE_3CC.equals(custSubGrp))
        && (!(SUB_TYPE_LEASE_NO_RESTRICT.equals(custSubGrp) && !StringUtils.isBlank(addr.getDivn()))))) {
      output.setResults("Skipped");
      output.setDetails("Non Leasing scenario not supported.");
      return true;
    }
    ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
    if (scenarioExceptions != null) {
      scenarioExceptions.setSkipCompanyVerification(true);
    }
    return false;
  }

  @Override
  protected void performAction(AutomationEngineData engineData, String msg) {
    engineData.addRejectionComment("OTH", msg, "", "");
  }

  @Override
  public void invoiceToOverrides(RequestData requestData, OverrideOutput overrides, EntityManager entityManager) {
    Data data = requestData.getData();
    String custSubGrp = data.getCustSubGrp();
    if (SUB_TYPE_LEASE_3CC.equals(custSubGrp) || SUB_TYPE_LEASE_SVR_CONT.equals(custSubGrp)) {
      // check invoice-to address if not add
      Addr zi01 = requestData.getAddress("ZI01");

      String divn = "";
      String address = "7100 Highlands Parkway";
      String address2 = "";
      String city = "Smyrna";
      String state = "GA";
      String postCd = "30082-4859";
      String landCntry = "US";

      if (zi01 == null) {

        LOG.debug("Adding the main address..");
        AddressService addrService = new AddressService();
        AddressModel addrModel = new AddressModel();
        addrModel.setReqId(data.getId().getReqId());
        addrModel.setDivn(divn);
        addrModel.setLandCntry(landCntry);
        addrModel.setAddrTxt(address);
        addrModel.setAddrTxt2(address2);
        addrModel.setCity1(city);
        addrModel.setStateProv(state);
        addrModel.setPostCd(postCd);
        addrModel.setState(BaseModel.STATE_NEW);
        addrModel.setAction("ADD_ADDRESS");

        addrModel.setAddrType(CmrConstants.ADDR_TYPE.ZI01.toString());
        addrModel.setCmrIssuingCntry(data.getCmrIssuingCntry());
        try {
          AppUser user = new AppUser();
          user.setIntranetId(requestData.getAdmin().getRequesterId());
          user.setBluePagesName(requestData.getAdmin().getRequesterNm());
          DummyServletRequest dummyReq = new DummyServletRequest();
          if (dummyReq.getSession() != null) {
            LOG.trace("Session found for dummy req");
            dummyReq.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, user);
          } else {
            LOG.warn("Session not found for dummy req");
          }
          addrService.performTransaction(addrModel, entityManager, dummyReq);
        } catch (Exception e) {
          LOG.error("An error occurred while adding ZI01 address", e);
        }
        entityManager.flush();
      } else {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "ADDR_TXT", zi01.getAddrTxt(), address);
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "CITY1", zi01.getCity1(), city);
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "STATE_PROV", zi01.getStateProv(), state);
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "POST_CD", zi01.getPostCd(), postCd);
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "LAND_CNTRY", zi01.getLandCntry(), landCntry);
      }
    }
  }

  @Override
  public void copyAndFillIBMData(EntityManager entityManager, GEOHandler handler, RequestData requestData, AutomationEngineData engineData,
      StringBuilder details, OverrideOutput overrides, RequestData childRequest, FindCMRRecordModel ibmCmr) {

    Data data = requestData.getData();
    String custSubGroup = data.getCustSubGrp();
    if (ibmCmr != null) {
      if (!StringUtils.isBlank(ibmCmr.getCmrSapNumber())) {
        details.append(
            "\nCopying IBM Codes from IBM CMR " + ibmCmr.getCmrNum() + " - " + ibmCmr.getCmrName() + " (" + ibmCmr.getCmrSapNumber() + "): \n");
      } else {
        details.append("\nCopying IBM Codes from IBM CMR " + ibmCmr.getCmrNum() + " - " + ibmCmr.getCmrName() + ": \n");
      }

      String affiliate = ibmCmr.getCmrAffiliate();
      String isic = ibmCmr.getCmrIsic();
      boolean federalPoa = isic != null && (isic.startsWith("90") || isic.startsWith("91") || isic.startsWith("92"));
      if (federalPoa) {
        affiliate = ibmCmr.getCmrEnterpriseNumber();
      }
      if (!StringUtils.isBlank(ibmCmr.getCmrAffiliate())) {
        details.append(" - Affiliate: " + ibmCmr.getCmrAffiliate() + (federalPoa ? " (Enterprise from Federal/POA)" : "") + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "AFFILIATE", data.getAffiliate(), affiliate);
      }

      if (!StringUtils.isBlank(ibmCmr.getCmrIsu())) {
        details.append(" - ISU: " + ibmCmr.getCmrIsu() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "ISU_CD", data.getIsuCd(), ibmCmr.getCmrIsu());
        details.append(" - Client Tier: " + ibmCmr.getCmrTier() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CLIENT_TIER", data.getClientTier(), ibmCmr.getCmrTier());
      }

      if (!StringUtils.isBlank(ibmCmr.getCmrInac())) {
        details.append(" - NAC/INAC: " + ("I".equals(ibmCmr.getCmrInacType()) ? "INAC" : ("N".equals(ibmCmr.getCmrInacType()) ? "NAC" : "-")) + " "
            + ibmCmr.getCmrInac() + (ibmCmr.getCmrInacDesc() != null ? "( " + ibmCmr.getCmrInacDesc() + ")" : ""));
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "INAC_TYPE", data.getInacType(), ibmCmr.getCmrInacType());
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "INAC_CD", data.getInacCd(), ibmCmr.getCmrInac());
      }

      // add here gbg and cov
      LOG.debug("Getting Buying Group/Coverage values");
      String bgId = ibmCmr.getCmrBuyingGroup();
      GBGResponse calcGbg = new GBGResponse();
      if (!StringUtils.isBlank(bgId)) {
        calcGbg.setBgId(ibmCmr.getCmrBuyingGroup());
        calcGbg.setBgName(ibmCmr.getCmrBuyingGroupDesc());
        calcGbg.setCmrCount(1);
        calcGbg.setGbgId(ibmCmr.getCmrGlobalBuyingGroup());
        calcGbg.setGbgName(ibmCmr.getCmrGlobalBuyingGroupDesc());
        calcGbg.setLdeRule(ibmCmr.getCmrLde());
      } else {
        calcGbg.setBgId("BGNONE");
        calcGbg.setBgName("None");
        calcGbg.setCmrCount(1);
        calcGbg.setGbgId("GBGNONE");
        calcGbg.setGbgName("None");
        calcGbg.setLdeRule("BG_DEFAULT");
      }
      if (!StringUtils.isBlank(calcGbg.getGbgId())) {
        details.append(" - GBG: " + calcGbg.getGbgId() + "(" + (StringUtils.isBlank(calcGbg.getGbgName()) ? "not specified" : calcGbg.getGbgName())
            + ")" + "\n");
      } else {
        details.append(" - GBG: none\n");
      }
      if (!StringUtils.isBlank(calcGbg.getBgId())) {
        details.append(
            " - BG: " + calcGbg.getBgId() + "(" + (StringUtils.isBlank(calcGbg.getBgName()) ? "not specified" : calcGbg.getBgName()) + ")" + "\n");
      } else {
        details.append(" - BG: none\n");
      }
      if (!StringUtils.isBlank(calcGbg.getBgId())) {
        details.append(" - LDE Rule: " + calcGbg.getLdeRule() + "\n");
      } else {
        details.append(" - LDE Rule: none\n");
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.put(AutomationEngineData.GBG_MATCH, calcGbg);

      LOG.debug("BG ID: " + calcGbg.getBgId());
      String calcCovId = ibmCmr.getCmrCoverage();
      if (StringUtils.isBlank(calcCovId)) {
        calcCovId = RequestUtils.getDefaultCoverage(entityManager, "US");
      }
      details.append(" - Coverage: " + calcCovId + (ibmCmr.getCmrCoverageName() != null ? " (" + ibmCmr.getCmrCoverageName() + ")" : "") + "\n");
      LOG.debug("Coverage: " + calcCovId);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      engineData.put(AutomationEngineData.COVERAGE_CALCULATED, calcCovId);

      details.append(" - SICMEN: " + ibmCmr.getCmrIsic() + "\n");
      details.append(" - ISIC: " + ibmCmr.getCmrIsic() + "\n");
      details.append(" - Subindustry: " + ibmCmr.getCmrSubIndustry() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "ISIC_CD", data.getIsicCd(), ibmCmr.getCmrIsic());
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "US_SICMEN", data.getUsSicmen(), ibmCmr.getCmrIsic());
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), ibmCmr.getCmrSubIndustry());
      if (SUB_TYPE_LEASE_3CC.equals(custSubGroup)) {
        details.append(" - CSO Site: 3CC\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CSO_SITE", data.getCsoSite(), "3CC");
        details.append(" - Marketing A/R Dept: 3CC\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), "3CC");
      } else {
        details.append(" - CSO Site: " + ibmCmr.getUsCmrCsoSite() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CSO_SITE", data.getCsoSite(), ibmCmr.getUsCmrCsoSite());
        details.append(" - Marketing A/R Dept: " + ibmCmr.getUsCmrCsoSite() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), ibmCmr.getUsCmrMktgArDept());
      }
    }

    details.append(" - PCC A/R Dept: G8M\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "PCC_AR_DEPT", data.getPccArDept(), "G8M");
    
    String processingType = getProcessingTypeForUS(entityManager, "897");
    
    if (childRequest != null) {
      if (!StringUtils.isBlank(childRequest.getData().getMktgDept())) {
        details.append(" - Marketing Dept: " + childRequest.getData().getMktgDept() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MKTG_DEPT", data.getMktgDept(), childRequest.getData().getMktgDept());
      }
      if (!StringUtils.isBlank(childRequest.getData().getSvcArOffice())) {
        details.append(" - SVC A/R Office: " + childRequest.getData().getSvcArOffice() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SVC_AR_OFFICE", data.getSvcArOffice(),
            childRequest.getData().getSvcArOffice());
      }
    } else if (ibmCmr != null && "TC".equals(processingType)) {
      try {
        getMktgSvcUsCmr(ibmCmr.getCmrNum(), details, overrides, data);
      } catch (Exception e) {
        LOG.error("An error occurred while retrieving values from USCMR", e);
      }
    }

    createAddressOverrides(entityManager, handler, requestData, engineData, details, overrides, childRequest, ibmCmr);

    // do final checks on request data
    Admin admin = requestData.getAdmin();
    String custSubGrp = data.getCustSubGrp();
    Addr installAt = requestData.getAddress("ZS01");
    if (SUB_TYPE_LEASE_NO_RESTRICT.equals(custSubGrp) && !StringUtils.isBlank(installAt.getDivn())) {
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_TO", data.getRestrictTo(), "");
    } else if (SUB_TYPE_LEASE_3CC.equals(custSubGrp) || SUB_TYPE_LEASE_SVR_CONT.equals(custSubGrp)) {
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ADMN", "MAIN_CUST_NM1", admin.getMainCustNm1(), "IBM Credit LLC");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "ENTERPRISE", data.getEnterprise(), "4482735");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "COMPANY", data.getCompany(), "12003567");
      if (SUB_TYPE_LEASE_3CC.equals(custSubGrp)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_TO", data.getRestrictTo(), "ICC");
      } else if (SUB_TYPE_LEASE_SVR_CONT.equals(custSubGrp)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_TO", data.getRestrictTo(), "");
      }
    }

    details.append("Branch Office codes computed successfully.");
    engineData.addPositiveCheckStatus(AutomationEngineData.BO_COMPUTATION);

  }

  private void getMktgSvcUsCmr(String cmrNo, StringBuilder details, OverrideOutput overrides, Data data) throws Exception {
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
    String sql = ExternalizedQuery.getSql("AUTO.GET_MKTG_SVC_USCMR", usSchema);
    sql = StringUtils.replace(sql, ":CMR_NO", "'" + cmrNo + "'");
    String dbId = QueryClient.USCMR_APP_ID;
    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.setRows(1);
    query.addField("MKTG_DEPT");
    query.addField("SVC_AR_OFFICE");
    QueryClient client;
    QueryResponse response;
    client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    response = client.executeAndWrap(dbId, query, QueryResponse.class);
    if (response.isSuccess()) {
      Map<String, Object> record = response.getRecords().get(0);
      String mtkgDept = (String) record.get("MKTG_DEPT");
      String svcArDept = (String) record.get("SVC_AR_OFFICE");
      if (!StringUtils.isBlank(mtkgDept)) {
        details.append(" - Marketing Dept: " + mtkgDept + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MKTG_DEPT", data.getMktgDept(), mtkgDept);
      }
      if (!StringUtils.isBlank(svcArDept)) {
        details.append(" - SVC A/R Office: " + svcArDept + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SVC_AR_OFFICE", data.getSvcArOffice(), svcArDept);
      }
    }
  }

  @Override
  public void doFinalValidations(AutomationEngineData engineData, RequestData requestData, StringBuilder details, OverrideOutput overrides,
      FindCMRRecordModel ibmCmr, AutomationResult<OverrideOutput> result) {
    // NOOP
  }

  @Override
  public boolean processRequest(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<OverrideOutput> output, StringBuilder details, boolean childCompleted, RequestData childRequest, GEOHandler handler,
      FindCMRRecordModel ibmCmr, OverrideOutput overrides) throws Exception {

    Admin admin = requestData.getAdmin();
    Addr addr = requestData.getAddress("ZS01");
    long childReqId = admin.getChildReqId();

    // match against D&B
    DnBMatchingResponse dnbMatch = matchAgainstDnB(handler, requestData, addr, engineData, details, overrides, ibmCmr != null);

    // match against SOS-RPA
    matchAgainstSosRpa(handler, requestData, addr, engineData, details, overrides, ibmCmr != null);

    if (ibmCmr == null) {

      LOG.debug("No IBM Direct CMR for the end user.");
      childReqId = createChildRequest(entityManager, requestData, engineData);
      details.append("No IBM Direct CMR for the end user.\n");

      if (childCompleted) {
        String childError = "Child Request " + childReqId + " was completed but Direct CMR cannot be determined. Manual validation needed.";
        details.append(childError + "\n");
        engineData.addNegativeCheckStatus("_usChildError", childError);
        output.setDetails(details.toString());
        output.setResults("Issues Encountered");
        return false;
      } else {
        String childErrorMsg = "- IBM Direct CMR request creation cannot be done, errors were encountered -";
        if (childReqId <= 0) {
          details.append(childErrorMsg + "\n");
          // engineData.addNegativeCheckStatus("_usBpRejected", childErrorMsg);
          engineData.addRejectionComment("OTH", childErrorMsg, "", "");
          output.setDetails(details.toString());
          output.setOnError(true);
          output.setResults("Issues Encountered");
          return false;
        } else {
          String childDetails = completeChildRequestDataAndAddress(entityManager, requestData, engineData, childReqId, dnbMatch);
          if (childDetails == null) {
            details.append(childErrorMsg + "\n");
            // engineData.addNegativeCheckStatus("_usBpRejected",
            // childErrorMsg);
            engineData.addRejectionComment("OTH", childErrorMsg, "", "");
            output.setOnError(true);
            output.setResults("Issues Encountered");
            output.setDetails(details.toString());
            return false;
          } else {
            details.append("Child Request " + childReqId + " created for the IBM Direct CMR record of " + addr.getDivn()
                + (!StringUtils.isBlank(addr.getDept()) ? " " + addr.getDept() : "")
                + ".\nThe system will wait for completion of the child record bfore processing the request.\n");
            details.append(childDetails + "\n");
            setWaiting(true);
            output.setDetails(details.toString());
            output.setResults("Waiting on Child Request");
            output.setOnError(false);
            return false;
          }

        }
      }
    }
    return true;
  }

  @Override
  protected void modifyChildDataValues(EntityManager entityManager, RequestData requestData, RequestData childReqData, StringBuilder details) {
    long childRequestId = childReqData.getAdmin().getId().getReqId();
    try {
      CopyAttachmentUtil.copyAttachmentsByType(entityManager, requestData, childRequestId, "COMP");
      childReqData.getAdmin().setMatchOverrideIndc(requestData.getAdmin().getMatchOverrideIndc());
    } catch (Exception e) {
      LOG.error("An error occurred while copying the attachment to child request - " + childRequestId, e);
    }
  }

  @Override
  protected FindCMRRecordModel getIBMCMRBestMatch(AutomationEngineData engineData, RequestData requestData, List<DuplicateCMRCheckResponse> matches)
      throws CmrException {
    for (DuplicateCMRCheckResponse record : matches) {
      LOG.debug(" - Duplicate: (Restrict To: " + record.getUsRestrictTo() + ", Grade: " + record.getMatchGrade() + ")" + record.getCompany() + " - "
          + record.getCmrNo() + " - " + record.getAddrType() + " - " + record.getStreetLine1());
      if (StringUtils.isBlank(record.getUsRestrictTo())) {
        // check US CMR DB first to confirm no restriction
        if (hasBlankRestrictionCodeInUSCMR(record.getCmrNo())) {

          // IBM Direct CMRs have blank restrict to
          LOG.debug("CMR No. " + record.getCmrNo() + " has BLANK restriction code in US CMR. Getting CMR Details..");
          String overrides = "addressType=ZS01&cmrOwner=IBM&showCmrType=R&customerNumber=" + record.getCmrNo();
          FindCMRResultModel result = CompanyFinder.getCMRDetails(SystemLocation.UNITED_STATES, record.getCmrNo(), 5, null, overrides);
          if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
            return result.getItems().get(0);
          }
        } else {
          LOG.debug("CMR No. " + record.getCmrNo() + " has non-blank restriction code in US CMR");
        }
      }
    }
    return null;
  }

  @Override
  protected void setChildRequestScenario(Data data, Data childData, Admin childAdmin, StringBuilder details) {

    String isic = data.getUsSicmen();
    if (StringUtils.isBlank(isic)) {
      isic = data.getIsicCd();
    }
    if (StringUtils.isBlank(isic)) {
      isic = "";
    }

    String affiliate = data.getAffiliate();

    String typeDesc = null;
    String subTypeDesc = null;
    String type = null;
    String subType = null;
    String custType = null;

    int isicNumeric = 0;
    if (StringUtils.isNumeric(isic.substring(0, 2))) {
      isicNumeric = Integer.parseInt(isic.substring(0, 2));
    }

    if (isicNumeric >= 94 && isicNumeric <= 97) {
      typeDesc = "State and Local";
      type = TYPE_STATE_AND_LOCAL;
      custType = USUtil.STATE_LOCAL;
      switch (isicNumeric) {
      case 94:
        subTypeDesc = "State and Local - State";
        subType = SUB_TYPE_STATE_AND_LOCAL_STATE;
        break;
      case 95:
        subTypeDesc = "State and Local - County";
        subType = SUB_TYPE_STATE_AND_LOCAL_COUNTY;
        break;
      case 96:
        subTypeDesc = "State and Local - City";
        subType = SUB_TYPE_STATE_AND_LOCAL_CITY;
        break;
      case 97:
        subTypeDesc = "State and Local - District";
        subType = SUB_TYPE_STATE_AND_LOCAL_DISTRICT;
        break;
      }
    } else if (isicNumeric >= 90 && isicNumeric <= 92) {
      typeDesc = "Federal";
      type = TYPE_FEDERAL;
      if (AFFILIATE_FEDERAL.equals(affiliate)) {
        subTypeDesc = "Federal Gov't Regular";
        subType = SUB_TYPE_FEDERAL_REGULAR_GOVT;
        custType = USUtil.FEDERAL;
      } else {
        subTypeDesc = "Power of Attorney";
        subType = SUB_TYPE_FEDERAL_POA;
        custType = USUtil.POWER_OF_ATTORNEY;
      }
    } else {
      typeDesc = "Commercial";
      type = TYPE_COMMERCIAL;
      subTypeDesc = "Regular Commercial CMR";
      subType = SUB_TYPE_COMMERCIAL_REGULAR;
      custType = USUtil.COMMERCIAL;
    }

    details.append(" - Type: " + typeDesc + "\n");
    details.append(" - Sub-type: " + subTypeDesc + "\n");
    childData.setCustGrp(type);
    childData.setCustSubGrp(subType);
    childAdmin.setCustType(custType);
  }

  public static String getProcessingTypeForUS(EntityManager entityManager, String country) {
    String sql = ExternalizedQuery.getSql("AUTO.GET_PROCESSING_TYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", country);
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

}
