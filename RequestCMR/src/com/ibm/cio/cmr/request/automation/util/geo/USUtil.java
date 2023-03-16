package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.ui.ModelMap;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DupCMRCheckElement;
import com.ibm.cio.cmr.request.automation.impl.us.USDuplicateCheckElement;
import com.ibm.cio.cmr.request.automation.impl.us.USSosRpaCheckElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.automation.util.DummyServletRequest;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.automation.util.geo.us.USDetailsContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.KnvvExt;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.revivedcmr.RevivedCMRModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.us.SosRequest;
import com.ibm.cmr.services.client.automation.us.SosResponse;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;
import com.itextpdf.styledxmlparser.jsoup.internal.StringUtil;

/**
 * 
 * @author RoopakChugh
 *
 */

public class USUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(USUtil.class);
  // Customer Groups
  public static final String CG_COMMERCIAL = "1";
  public static final String CG_COMMERCIAL_FRANCHISE = "2";
  public static final String CG_COMMERCIAL_RESTRICTED = "3";
  public static final String CG_COMMERCIAL_REST_DEALER = "4";
  public static final String CG_THIRD_P_BUSINESS_PARTNER = "5";
  public static final String CG_THIRD_P_LEASING = "6";
  public static final String CG_STATE_LOCAL = "7";
  public static final String CG_STATE_LOCAL_PUBLIC = "8";
  public static final String CG_FEDERAL = "9";
  public static final String CG_FEDERAL_INDIAN = "10";
  public static final String CG_FEDERAL_ALASKA = "11";
  public static final String CG_IGS = "12";
  public static final String CG_INTERNAL = "13";
  public static final String CG_BY_MODEL = "14";
  public static final String CG_THIRD_P_FSP = "15";

  // Cust Types for determination
  public static final String COMMERCIAL = "1";
  public static final String STATE_LOCAL = "2";
  public static final String LEASING = "3";
  public static final String FEDERAL = "4";
  public static final String INTERNAL = "5";
  public static final String POWER_OF_ATTORNEY = "6";
  public static final String BUSINESS_PARTNER = "7";

  // SUB SCENARIOS
  // COMMERCIAL SUB_SCENARIOS
  public static final String SC_COMM_REGULAR = "REGULAR";
  public static final String SC_PVT_HOUSEHOLD = "PRIV";
  public static final String SC_BROKER = "BROKER";
  public static final String SC_DUMMY = "DUMMYC";
  public static final String SC_CSP = "CSP";
  public static final String SC_DOMINO = "DOM";
  public static final String SC_HILTON = "HILT";
  public static final String SC_FLORIDA = "FPL";
  public static final String SC_IBME = "IBMEM";
  public static final String SC_REST_OIO = "OIO";
  public static final String SC_REST_OEMHW = "OEMHW";
  public static final String SC_REST_OEMSW = "OEM-SW";
  public static final String SC_REST_TPD = "TPD";
  public static final String SC_REST_SSD = "SSD";
  public static final String SC_REST_DB4 = "DB4";
  public static final String SC_REST_GRNTS = "GRNTS";
  public static final String SC_REST_LBPS = "LBPS";
  public static final String SC_REST_LIIS = "LIIS";
  public static final String SC_REST_RFBPO = "RFBPO";
  public static final String SC_REST_SSI = "SSI";
  public static final String SC_REST_ICC = "ICC";
  public static final String SC_REST_SVMP = "SVMP";
  public static final String SC_REST_KYN = "KYN";
  // IGS
  public static final String SC_IGSF = "IGSF";
  public static final String SC_IGS = "IGS";
  // State/Local Government
  public static final String SC_STATE = "LOCAL GOV";
  public static final String SC_STATE_DIST = "SPEC DIST";
  public static final String SC_STATE_COUNTY = "COUNTY";
  public static final String SC_STATE_CITY = "CITY";
  public static final String SC_STATE_STATE = "STATE";
  public static final String SC_STATE_HOSPITALS = "HOSPITALS";
  public static final String SC_SCHOOL_PUBLIC = "SCHOOL PUBLIC";
  public static final String SC_SCHOOL_CHARTER = "SCHOOL CHARTER";
  public static final String SC_SCHOOL_PRIV = "SCHOOL PRIV";
  public static final String SC_SCHOOL_PAROCHL = "SCHOOL PAROCHL";
  public static final String SC_SCHOOL_COLLEGE = "SCHOOL COLLEGE";
  // Leasing
  public static final String SC_LEASE_NO_RESTRICT = "NO RESTRICT";
  public static final String SC_LEASE_3CC = "3CC";
  public static final String SC_LEASE_IPMA = "IPMA";
  public static final String SC_LEASE_LPMA = "LPMA";
  public static final String SC_LEASE_SVR_CONT = "SVR CONT";
  // Federal
  public static final String SC_FED_REGULAR = "FEDERAL";
  public static final String SC_FED_CAMOUFLAGED = "CAMOUFLAGED";
  public static final String SC_FED_INDIAN_TRIBE = "INDIAN TRIBE";
  public static final String SC_FED_TRIBAL_BUS = "TRIBAL BUS";
  public static final String SC_FED_HEALTHCARE = "HEALTHCARE";
  public static final String SC_FED_HOSPITAL = "HOSPITAL";
  public static final String SC_FED_CLINIC = "CLINIC";
  public static final String SC_FED_NATIVE_CORP = "NATIVE CORP";
  // Power of Attorney
  public static final String SC_FED_FEDSTATE = "FEDSTATE";
  public static final String SC_FED_POA = "POA";
  // Internal
  public static final String SC_INTERNAL = "INTERNAL";
  // Business Partner
  public static final String SC_BP_END_USER = "END USER";
  public static final String SC_BP_POOL = "POOL";
  public static final String SC_FSP_END_USER = "FSP END USER";
  public static final String SC_FSP_POOL = "FSP POOL";
  public static final String SC_BP_DEVELOP = "DEVELOP";
  public static final String SC_BP_E_HOST = "E-HOST";
  // BYMODEL
  public static final String SC_BYMODEL = "BYMODEL";

  public static final List<String> FEDERAL_SCENARIOS = Arrays.asList(SC_FED_CAMOUFLAGED, SC_FED_CLINIC, SC_FED_FEDSTATE, SC_FED_HEALTHCARE,
      SC_FED_HOSPITAL, SC_FED_INDIAN_TRIBE, SC_FED_NATIVE_CORP, SC_FED_POA, SC_FED_REGULAR, SC_FED_TRIBAL_BUS);
  private static final List<String> CSP_IRRELEVANT_UPDATE_FIELDS = Arrays.asList("ISU Code", "GEO Location Code", "Coverage Type/ID", "BG LDE Rule",
      "Buying Group ID", "Client Tier", "DUNS No.");
  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("County", "PO Box");

  private static final List<String> RELEVANT_ADDRESS_FIELDS_RPA_UPDATES = Arrays.asList("Customer Name", "Customer Name 2", "Address", "Address Con",
      "City", "State/Province");

  private static final List<String> HEALTH_CARE_EDUCATION_ISIC = Arrays.asList("8030", "8010", "8511");

  public static List<USBranchOffcMapping> svcARBOMappings = new ArrayList<USBranchOffcMapping>();
  public static List<USBranchOffcMapping> boMappings = new ArrayList<USBranchOffcMapping>();
  private static Map<String, USDetailsContainer> usDetailsMap = new HashMap<String, USDetailsContainer>();
  private static List<USFedIsicMapping> usFedIsicMap = new ArrayList<USFedIsicMapping>();

  @SuppressWarnings("unchecked")
  public USUtil() {
    LOG.debug("Initializing US Util");
    // initialize mapping per scenario
    LOG.debug("US - initializing mapping per scenario");
    if (USUtil.boMappings.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("mappings", ArrayList.class);

      digester.addObjectCreate("mappings/mapping", USBranchOffcMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/scenario", "scenario");
      digester.addBeanPropertySetter("mappings/mapping/csoSite", "csoSite");
      digester.addBeanPropertySetter("mappings/mapping/mktgDept", "mktgDept");
      digester.addBeanPropertySetter("mappings/mapping/mtkgArDept", "mtkgArDept");
      digester.addBeanPropertySetter("mappings/mapping/svcArOffice", "svcArOffice");
      digester.addBeanPropertySetter("mappings/mapping/pccArDept", "pccArDept");
      digester.addSetNext("mappings/mapping", "add");
      try {
        InputStream is = ConfigUtil.getResourceStream("us-branchoff-mapping.xml");
        USUtil.boMappings = (ArrayList<USBranchOffcMapping>) digester.parse(is);
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }

    // initialize mktg AR dept and svc AR Office mapping
    LOG.debug("US - initializing mktg AR dept and svc AR Office mapping.");
    if (USUtil.svcARBOMappings.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("mappings", ArrayList.class);

      digester.addObjectCreate("mappings/mapping", USBranchOffcMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/mktgDepartmentAR", "mktgDepartmentAR");
      digester.addBeanPropertySetter("mappings/mapping/svcOfficeAR", "svcOfficeAR");
      digester.addSetNext("mappings/mapping", "add");
      try {
        InputStream is = ConfigUtil.getResourceStream("us-mktgsvc-mapping.xml");
        USUtil.svcARBOMappings = (ArrayList<USBranchOffcMapping>) digester.parse(is);
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }

    LOG.debug("US - initializing mapping for Fed Isic for DnB matches");
    if (USUtil.usFedIsicMap.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("mappings", ArrayList.class);

      digester.addObjectCreate("mappings/mapping", USFedIsicMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/dnb-isic", "dnbIsic");
      digester.addBeanPropertySetter("mappings/mapping/cmr-isic", "cmrIsic");
      digester.addBeanPropertySetter("mappings/mapping/dnb-subInd", "dnbSubInd");
      digester.addBeanPropertySetter("mappings/mapping/cmr-subInd", "cmrSubInd");
      digester.addSetNext("mappings/mapping", "add");
      try {
        InputStream is = ConfigUtil.getResourceStream("us-fed-isic-mapping.xml");
        USUtil.usFedIsicMap = (ArrayList<USFedIsicMapping>) digester.parse(is);
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    StringBuilder eleResults = new StringBuilder();
    String scenarioSubType = "";
    boolean boCodesCalculated = false;

    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = data.getCustSubGrp();
      if (SC_BYMODEL.equals(scenarioSubType)) {
        scenarioSubType = determineCustSubScenario(entityManager, admin.getModelCmrNo(), engineData, requestData);
      }
      if (SC_BP_END_USER.equals(scenarioSubType)) {
        // check duplicate cmr again for BP@EU
        details.append("Performing Duplicate CMR checks for BP@EU Request after determining Direct CMR").append("\n");
        USDuplicateCheckElement dupCheckElement = new USDuplicateCheckElement(null, null, false, false);
        MatchingResponse<DuplicateCMRCheckResponse> response = dupCheckElement.getCMRMatches(entityManager, requestData, engineData);
        if (response.getSuccess() && !response.getMatches().isEmpty()) {
          List<DuplicateCMRCheckResponse> cmrCheckMatches = response.getMatches();
          details.append(cmrCheckMatches.size() + " record(s) found.");
          if (cmrCheckMatches.size() > 3) {
            cmrCheckMatches = cmrCheckMatches.subList(0, 5);
            details.append("Showing top 3 matches only.");
          }
          List<String> duplicateList = new ArrayList<String>();
          List<String> soldToKunnrsList = new ArrayList<String>();
          for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
            details.append("\n");
            LOG.debug("Duplicate CMRs Found..");
            duplicateList.add(cmrCheckRecord.getCmrNo());
            soldToKunnrsList.add(dupCheckElement.getZS01Kunnr(cmrCheckRecord.getCmrNo(), SystemLocation.UNITED_STATES));
            DupCMRCheckElement.logDuplicateCMR(details, cmrCheckRecord);
            if (!StringUtils.isBlank(cmrCheckRecord.getUsRestrictTo())) {
              details.append("US Restrict To =  " + cmrCheckRecord.getUsRestrictTo()).append("\n");
            } else {
              details.append("US Restrict To = -blank- ").append("\n");
            }
          }
          engineData.addRejectionComment("DUPC", "There were possible duplicate CMRs found with the same data.",
              StringUtils.join(dupCheckElement.removeDupEntriesFrmList(duplicateList), ", "),
              StringUtils.join(dupCheckElement.removeDupEntriesFrmList(soldToKunnrsList), ", "));
          admin.setMatchIndc("C");
          results.setResults("Duplicate BP CMR(s) Found");
          results.setDetails(details.toString());
          results.setOnError(true);
          return results;
        } else {
          details.append("No Duplicate CMRs found").append("\n");
        }
      }

      if (SC_LEASE_IPMA.equals(scenarioSubType) || SC_LEASE_LPMA.equals(scenarioSubType)) {
        Addr zi01 = requestData.getAddress("ZI01");

        String divn = "IBM Credit LLC";
        String dept = SC_LEASE_IPMA.equals(scenarioSubType) ? "IPMA" : "LPMA";
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
          addrModel.setDept(dept);
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
          overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "DIVN", zi01.getDivn(), divn);
          overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "DEPT", zi01.getDept(), dept);
          overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "ADDR_TXT", zi01.getAddrTxt(), address);
          overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "CITY1", zi01.getCity1(), city);
          overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "STATE_PROV", zi01.getStateProv(), state);
          overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "POST_CD", zi01.getPostCd(), postCd);
          overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "LAND_CNTRY", zi01.getLandCntry(), landCntry);
        }
      }

      LOG.debug("US : Performing field computations for req_id : " + admin.getId().getReqId());
      // computation start
      if (engineData.hasPositiveCheckStatus(AutomationEngineData.BO_COMPUTATION)) {
        details.append("Branch Office codes computed by another element/external process.");
      } else {
        if (StringUtils.isNotEmpty(data.getEnterprise()) && data.getEnterprise().equals("6500871")) {
          String csoSite = "PAH";
          String mktgDept = "SVB";
          String mtkgArDept = "SD3";
          String svcArOffice = "IJ9";

          details.append("Setting Fields based on US Scenarios:").append("\n");
          details.append("CSO Site = " + csoSite).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CSO_SITE", data.getCsoSite(), csoSite);

          details.append("Marketing Department = " + mktgDept).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "MKTG_DEPT", data.getMktgDept(), mktgDept);

          details.append("Marketing A/R Department = " + mtkgArDept).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), mtkgArDept);

          details.append("SVC A/R Office = " + svcArOffice).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SVC_AR_OFFICE", data.getSvcArOffice(), svcArOffice);

          if (!boMappings.isEmpty() && StringUtils.isNotBlank(scenarioSubType) && !SC_INTERNAL.equals(scenarioSubType)) {
            for (USBranchOffcMapping mapping : boMappings) {
              if (mapping.getScenario().equalsIgnoreCase(scenarioSubType)) {
                String pccArDept = mapping.getPccArDept(entityManager, requestData);
                details.append("PCC A/R Department = " + pccArDept).append("\n");
                overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "PCC_AR_DEPT", data.getPccArDept(), pccArDept);
              }
            }
          }
          boCodesCalculated = true;
        } else if (!boMappings.isEmpty() && StringUtils.isNotBlank(scenarioSubType) && !SC_INTERNAL.equals(scenarioSubType)) {
          for (USBranchOffcMapping mapping : boMappings) {
            if (mapping.getScenario().equalsIgnoreCase(scenarioSubType)) {
              String csoSite = mapping.getCsoSite(entityManager, requestData);
              String mktgDept = mapping.getMktgDept(entityManager, requestData);
              String mtkgArDept = mapping.getMtkgArDept(entityManager, requestData);
              String svcArOffice = mapping.getSvcArOffice(entityManager, requestData);
              String pccArDept = mapping.getPccArDept(entityManager, requestData);

              details.append("Setting Fields based on US Scenarios:").append("\n");
              details.append("CSO Site = " + csoSite).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CSO_SITE", data.getCsoSite(), csoSite);

              details.append("Marketing Department = " + mktgDept).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "MKTG_DEPT", data.getMktgDept(), mktgDept);

              details.append("Marketing A/R Department = " + mtkgArDept).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), mtkgArDept);

              details.append("SVC A/R Office = " + svcArOffice).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SVC_AR_OFFICE", data.getSvcArOffice(), svcArOffice);

              details.append("PCC A/R Department = " + pccArDept).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "PCC_AR_DEPT", data.getPccArDept(), pccArDept);

              if (StringUtils.isNotBlank(csoSite) && StringUtils.isNotBlank(mktgDept) && StringUtils.isNotBlank(mtkgArDept)
                  && StringUtils.isNotBlank(svcArOffice) && StringUtils.isNotBlank(pccArDept)) {
                boCodesCalculated = true;
              }
              break;
            }
          }
        }

        if (boCodesCalculated) {
          details.append("Branch Office codes computed successfully.");
        } else if (SC_INTERNAL.equals(scenarioSubType)) {
          if (SC_BYMODEL.equals(data.getCustSubGrp())) {
            details.append("Skipping calculation of Branch Office codes because the CMR imported on the request is of INTERNAL Scenario.");
          } else {
            details.append("Skipping calculation of Branch Office codes because the request is of INTERNAL Scenario.");
          }
        } else {
          details.append("Branch Office codes could not be computed for this scenario.");
          engineData.addNegativeCheckStatus("verifyBranchOffc", "Branch Office Codes need to be verified.");

        }
      }

      // set ISU CTC if not found
      if (StringUtils.isBlank(data.getIsuCd())) {
        details.append("\nISU/Client Tier blank on the request. Setting ISU-CTC to 34-Q.").append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(), "34");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER", data.getClientTier(), "Q");
      }

      if (StringUtils.isNotEmpty(data.getIsicCd())) {
        if (StringUtils.isNotEmpty(data.getUsSicmen()) && !"357X".equals(data.getIsicCd()) && !data.getIsicCd().equals(data.getUsSicmen())) {
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "US_SICMEN", data.getUsSicmen(), data.getIsicCd());
        }

        if ("C".equals(admin.getReqType())
            && (data.getIsicCd().startsWith("90") || data.getIsicCd().startsWith("91") || data.getIsicCd().startsWith("92"))
            && StringUtils.isNotBlank(scenarioSubType) && !FEDERAL_SCENARIOS.contains(scenarioSubType)
            && StringUtils.isNotBlank(admin.getSourceSystId()) && !admin.getSourceSystId().equals("FedCMR")) {
          if (SC_BP_END_USER.equals(scenarioSubType)) {
            details.append("Federal ISIC found on the request for BP@EU. Treating as validated.\n");
            // engineData.addNegativeCheckStatus("FEDERAL_ISIC", "Federal ISIC
            // found on the request for non-Federal scenario.");
          } else {
            details.append("Federal ISIC found on the request for non-Federal scenario.\n");
            engineData.addNegativeCheckStatus("FEDERAL_ISIC", "Federal ISIC found on the request for non-Federal scenario.");
          }
        }
      }

      // if scenario is OEMSW or OEMHW set isic to 357X
      if (SC_REST_OEMSW.equals(scenarioSubType) || SC_REST_OEMHW.equals(scenarioSubType) || SC_REST_TPD.equals(scenarioSubType)
          || SC_REST_SSD.equals(scenarioSubType) || SC_REST_DB4.equals(scenarioSubType)) {
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISIC_CD", data.getIsicCd(), "357X");
        if (SC_REST_TPD.equals(scenarioSubType) || SC_REST_SSD.equals(scenarioSubType) || SC_REST_DB4.equals(scenarioSubType)) {
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), "ZC");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "US_SICMEN", data.getUsSicmen(), "357X");
        }
      }

      // if scenario is KYN
      if (SC_REST_KYN.equals(scenarioSubType)) {
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISIC_CD", data.getIsicCd(), "7229");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "RESTRICT_TO", data.getRestrictTo(), "KYN");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), "BD");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "US_SICMEN", data.getUsSicmen(), "7229");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "AFFILIATE", data.getAffiliate(), "6500871");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "COMPANY", data.getCompany(), "12641341");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ENTERPRISE", data.getEnterprise(), "6500871");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "INAC_CD", data.getInacCd(), "6272");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "INAC_TYPE", data.getInacType(), "I");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "TAX_CD1", data.getTaxCd1(), "J666");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), "SD3");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SVC_AR_OFFICE", data.getSvcArOffice(), "IJ9");
      }

      // CREATCMR-6342
      // if (CG_BY_MODEL.equals(data.getCustGrp()) &&
      // StringUtils.isNotEmpty(data.getMiscBillCd())) {
      // String miscBillCode = "";
      // miscBillCode = data.getMiscBillCd().toUpperCase();
      // if (miscBillCode.matches(".*[ABMNH]+.*")) {
      // miscBillCode = miscBillCode.replaceAll("[ABMNH]", "");
      // overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE,
      // "DATA", "MISC_BILL_CD", data.getMiscBillCd(), miscBillCode.trim());
      // }
      // }
      // CREATCMR-6342

      // CREATCMR-7584
      // if ("X".equals(data.getSpecialTaxCd())) {
      // details.append("Tax Exempt Status cannot be 'X'. Clearing Tax Exempt
      // Status value.").append("\n");
      // overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE,
      // "DATA", "SPECIAL_TAX_CD", data.getSpecialTaxCd(), "");
      // }

    } else if ("U".equals(admin.getReqType())) {
      eleResults.append("Skipped");
      details.append("Skipping BO codes computations for update requests.");
    }

    if (results == null || (results != null && results.isOnError())) {
      eleResults.append("Error On Field Calculation.");
    }
    // computation end
    results.setResults(eleResults.toString());
    results.setDetails(details.toString());
    results.setProcessOutput(overrides);

    return results;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    boolean valid = true;
    String[] scenarioList = { SC_STATE, SC_SCHOOL_PUBLIC, SC_SCHOOL_CHARTER, SC_SCHOOL_PRIV, SC_SCHOOL_PAROCHL, SC_SCHOOL_COLLEGE, SC_STATE_STATE,
        SC_STATE_DIST, SC_STATE_COUNTY, SC_STATE_CITY, SC_LEASE_LPMA, SC_FED_REGULAR, SC_FED_CLINIC, SC_FED_FEDSTATE, SC_FED_HEALTHCARE,
        SC_FED_HOSPITAL, SC_FED_INDIAN_TRIBE, SC_FED_NATIVE_CORP, SC_FED_POA, SC_FED_TRIBAL_BUS };
    String[] skipCompanyChecksScenarioList = { SC_BP_DEVELOP, SC_BP_E_HOST, SC_BP_END_USER, SC_LEASE_3CC, SC_LEASE_SVR_CONT, SC_INTERNAL, SC_DUMMY,
        SC_IGS, SC_IGSF, SC_REST_SSI, SC_STATE_DIST, SC_FED_REGULAR, SC_FED_CLINIC, SC_FED_FEDSTATE, SC_FED_HEALTHCARE, SC_FED_HOSPITAL,
        SC_FED_INDIAN_TRIBE, SC_FED_NATIVE_CORP, SC_FED_POA, SC_FED_TRIBAL_BUS, SC_STATE_COUNTY, SC_STATE_CITY, SC_STATE_STATE, SC_STATE_HOSPITALS,
        SC_SCHOOL_PUBLIC, SC_SCHOOL_CHARTER, SC_SCHOOL_PRIV, SC_SCHOOL_PAROCHL, SC_SCHOOL_COLLEGE, SC_LEASE_LPMA, SC_PVT_HOUSEHOLD, SC_IBME };
    String scenarioSubType = "";
    if (StringUtils.isNotEmpty(admin.getSourceSystId()) && admin.getSourceSystId().equals("FedCMR")) {
      Addr zs01 = requestData.getAddress("ZS01");
      Addr zi01 = requestData.getAddress("ZI01");
      boolean checkCounty = true;
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
      ScenarioExceptionsUtil exc = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
      details.append("CMR belongs to " + scenarioSubType + " scenario. ").append("\n");
      if (checkCounty) {
        if (zs01 != null) {
          if (zs01.getCounty().isEmpty()) {
            engineData.addNegativeCheckStatus("_nullCounty", scenarioSubType + " request need to be send to CMDE queue for review. ");
            details.append("Install At county value is null. The request need to be send to CMDE queue for review. ").append("\n");
            checkCounty = false;
          }
        }
        if (zi01 != null) {
          if (zi01.getCounty().isEmpty()) {
            engineData.addNegativeCheckStatus("_nullCounty", scenarioSubType + " request need to be send to CMDE queue for review. ");
            details.append("Invoice To county value is null. The request need to be send to CMDE queue for review. ").append("\n");
            checkCounty = false;
          }
        }
      }
      if (exc != null) {
        exc.setSkipCompanyVerification(true);
        exc.setSkipDuplicateChecks(true);
        engineData.put("SCENARIO_EXCEPTIONS", exc);
      }
      return true;
    }

    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
      if (SC_BYMODEL.equals(scenarioSubType)) {
        try {
          scenarioSubType = determineCustSubScenario(entityManager, admin.getModelCmrNo(), engineData, requestData);
          LOG.debug("ScenarioSubType:" + scenarioSubType);
        } catch (Exception e) {
          LOG.error("CMR Scenario for Create by model request could not be determined.", e);
        }

        if (data.getBgId() != null) {
          engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
          LOG.debug("Skip Coverage for Create By Model.");
        }

        // skip Dnb check and matching
        if (engineData.hasPositiveCheckStatus("SKIP_COMP_CHECK") || Arrays.asList(skipCompanyChecksScenarioList).contains(scenarioSubType)) {
          ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
          if (scenarioExceptions != null) {
            scenarioExceptions.setSkipCompanyVerification(true);
          }
        }

        // skip GBG for Internal -create by model
        if (SC_INTERNAL.equals(scenarioSubType)) {
          setDummyGBGMatchForInternal(engineData);
        }

      }
    }

    if (SC_IBME.equals(data.getCustSubGrp())) {
      Person person = null;
      if (StringUtils.isNotBlank(admin.getMainCustNm1())) {
        try {
          String mainCustName = admin.getMainCustNm1() + (StringUtils.isNotBlank(admin.getMainCustNm2()) ? " " + admin.getMainCustNm2() : "");
          person = BluePagesHelper.getPersonByName(mainCustName, data.getCmrIssuingCntry());
          if (person == null) {
            engineData.addRejectionComment("OTH", "Employee details not found in IBM People.", "", "");
            details.append("Employee details not found in IBM People.").append("\n");
            return false;
          } else {
            details.append("Employee details validated with IBM BluePages for " + person.getName() + "(" + person.getEmail() + ").").append("\n");
          }
        } catch (Exception e) {
          LOG.error("Not able to check name against bluepages", e);
          engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Not able to check name against bluepages for scenario IBM Employee.");
          return false;
        }
      } else {
        LOG.warn("Not able to check name against bluepages, Customer Name 1 not found on the main address");
        engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED", "Customer Name 1 not found on the main address");
        return false;
      }
    }

    if (StringUtils.isBlank(scenarioSubType) || Arrays.asList(scenarioList).contains(scenarioSubType)) {
      String scenarioDesc = getScenarioDesc(entityManager, scenarioSubType);
      LOG.debug("Skip Coverage for Create By Model CMR if BgId is not null " + data.getBgId());
      if (SC_BYMODEL.equals(data.getCustSubGrp())) {
        if (data.getBgId() != null) {
          engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
          LOG.debug("Skip Coverage for Create By Model.");
        }
        engineData.addNegativeCheckStatus("US_SCENARIO_CHK", "Processor review required as imported CMR belongs to " + scenarioDesc + " scenario.");
        details.append("Processor review required as imported CMR belongs to " + scenarioDesc + " scenario.").append("\n");
      } else {
        engineData.addNegativeCheckStatus("US_SCENARIO_CHK", "Processor review required as the request is for " + scenarioDesc + " scenario.");
        details.append("Processor review required as the request is for " + scenarioDesc + " scenario.").append("\n");
      }

      valid = true;
    } else if (SC_FED_CAMOUFLAGED.equals(scenarioSubType)) {
      String sql = ExternalizedQuery.getSql("AUTO.CHK_CMDE_USER");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQUESTER_ID", admin.getRequesterId());
      String procCntr = query.getSingleResult(String.class);
      if (StringUtils.isNotBlank(procCntr) && "Kuala Lumpur".equalsIgnoreCase(procCntr)) {
        valid = true;
      } else if (StringUtils.isNotEmpty(admin.getSourceSystId()) && admin.getSourceSystId().equals("FedCMR")) {
        valid = true;
      } else {
        engineData.addRejectionComment("OTH",
            "Federal CMR with restricted ISIC code is only allowed to be requested via FedCMR, please raise the request in FedCMR.", "", "");
        details.append("\nFederal CMR with restricted ISIC code is only allowed to be requested via FedCMR, please raise the request in FedCMR.")
            .append("\n");
        valid = false;
      }
    } else if (SC_COMM_REGULAR.equals(scenarioSubType)
        && (HEALTH_CARE_EDUCATION_ISIC.contains(data.getUsSicmen()) || HEALTH_CARE_EDUCATION_ISIC.contains(data.getIsicCd()))) {
      // CMR-3880 - add check for SUBIND/ISIC here for Health/Educ
      engineData.addNegativeCheckStatus("US_SCENARIO_CHK",
          "Processor review required since ISIC belongs to Healthcare/Education. Scenario verification needed.");
      details.append("Processor review required since ISIC belongs to Healthcare/Education. Scenario verification needed.").append("\n");
      valid = true;
    }
    return valid;
  }

  public static String getScenarioDesc(EntityManager entityManager, String scenarioSubType) {
    String sql = ExternalizedQuery.getSql("GET_SCENARIO_DESC_US");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("SUB_SCENARIO", scenarioSubType);
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    String sqlKey = ExternalizedQuery.getSql("AUTO.US.CHECK_CMDE");
    PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
    query.setParameter("EMAIL", admin.getRequesterId());
    query.setForReadOnly(true);
    if (query.exists() && "Y".equals(SystemParameters.getString("US.SKIP_UPDATE_CHECK"))) {
      // skip checks if requester is from USCMDE team
      admin.setScenarioVerifiedIndc("Y");
      LOG.debug("Requester is from US CMDE team, skipping update checks.");
      output.setDetails("Requester is from US CMDE team, skipping update checks.\n");
      validation.setMessage("Skipped");
      validation.setSuccess(true);
    } else if (StringUtils.isNotEmpty(admin.getSourceSystId()) && admin.getSourceSystId().equals("FedCMR")) {
      // Cmde review for federal- POA
      if (StringUtils.isNotEmpty(data.getCustClass()) && data.getCustClass().equals("15")) {
        engineData.addNegativeCheckStatus("OTH", "Updated elements cannot be checked automatically.");
        LOG.debug("Updated elements cannot be checked automatically.");
        output.setDetails("Federal request for POA requires CMDE review.\n");
        validation.setMessage("Review needed");
        validation.setSuccess(false);
      }
    } else {
      admin.setScenarioVerifiedIndc("N");
      EntityManager cedpManager = JpaManager.getEntityManager("CEDP");
      boolean hasNegativeCheck = false;
      // USDetailsContainer detailsCont = determineUSCMRDetails(entityManager,
      // requestData.getData().getCmrNo());
      // String custTypeCd = detailsCont.getCustTypCd();
      String custTypeCd = "";
      if (!StringUtils.isEmpty(admin.getCustType())) {
        custTypeCd = admin.getCustType();
      }
      List<String> allowedCodesAddition = Arrays.asList("F", "G", "C", "D", "V", "W", "X");
      List<String> allowedCodesRemoval = Arrays.asList("F", "G", "C", "D", "A", "B", "H", "M", "N");
      Map<String, String> failedChecks = new HashMap<String, String>();
      boolean requesterFromTaxTeam = false;
      boolean enterpriseAffiliateUpdated = false;
      boolean isicCheckDone = false;

      try {
        for (UpdatedDataModel updatedDataModel : changes.getDataUpdates()) {
          if (updatedDataModel != null) {
            LOG.debug("Checking updates for : " + new ObjectMapper().writeValueAsString(updatedDataModel));
            String field = updatedDataModel.getDataField();
            if ("CSP".equals(admin.getReqReason()) && CSP_IRRELEVANT_UPDATE_FIELDS.contains(field)) {
              continue;
            }
            switch (field) {
            case "Tax Class / Code 1":
            case "Tax Class / Code 2":
            case "Tax Class / Code 3":
            case "Tax Exempt Status":
            case "ICC Tax Class":
            case "ICC Tax Exempt Status":
            case "Out of City Limits":
              if (!failedChecks.containsKey("TAX_TEAM") && !requesterFromTaxTeam) {
                requesterFromTaxTeam = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(),
                    SystemParameters.getString("US.TAX_TEAM_HEAD"));
                if (!requesterFromTaxTeam) {
                  failedChecks.put("TAX_TEAM", "Requester not from Tax Team.");
                  hasNegativeCheck = true;
                }
              }
              break;
            case "CSO Site":
            case "Marketing Department":
              // set negative check status for FEDERAL Power of Attorney and BP
              if ((FEDERAL.equals(custTypeCd) || POWER_OF_ATTORNEY.equals(custTypeCd) || BUSINESS_PARTNER.equals(custTypeCd))) {
                failedChecks.put(field, field + " updated.");
                hasNegativeCheck = true;
              }
              break;
            case "Marketing A/R Department":
              // set negative check status if requester id is different than
              // specified
              if (SystemParameters.getString("US.AR_UPDT_REQUESTER") != null
                  && !admin.getRequesterId().equals(SystemParameters.getString("US.AR_UPDT_REQUESTER"))) {
                failedChecks.put(field, field + " updated.");
                hasNegativeCheck = true;
              }
              break;
            case "Miscellaneous Bill Code":
              List<String> newCodes = updatedDataModel.getNewData() != null ? Arrays.asList(updatedDataModel.getNewData().split(""))
                  : new ArrayList<String>();
              List<String> oldCodes = updatedDataModel.getOldData() != null ? Arrays.asList(updatedDataModel.getOldData().split(""))
                  : new ArrayList<String>();

              for (String s : oldCodes) {
                if (!newCodes.contains(s) && !allowedCodesRemoval.contains(s) && !hasNegativeCheck) {
                  failedChecks.put("MBC", "Restriced Miscellaneous Bill Codes changed.");
                  hasNegativeCheck = true;
                  break;
                }
              }
              for (String s : newCodes) {
                if (!oldCodes.contains(s) && !allowedCodesAddition.contains(s) && !hasNegativeCheck) {
                  failedChecks.put("MBC", "Restriced Miscellaneous Bill Codes changed.");
                  hasNegativeCheck = true;
                  break;
                }
              }
              break;
            case "ISU Code":
              if ("5B".equals(updatedDataModel.getNewData())) {
                String error = performCSPCheck(cedpManager, entityManager, data, admin);
                if (StringUtils.isNotBlank(error)) {
                  LOG.debug(error);
                  output.setDetails(error);
                  validation.setMessage("Validation Failed");
                  validation.setSuccess(false);
                  if (StringUtils.isBlank(admin.getSourceSystId())) {
                    engineData.addRejectionComment("OTH", error, "", "");
                    output.setOnError(true);
                  } else {
                    engineData.addNegativeCheckStatus("BP_" + field, error);
                  }
                  return true;
                }
              } else {
                failedChecks.put("ISU", "ISU Code updated.");
                hasNegativeCheck = true;
              }
              break;
            case "Enterprise Number":
            case "Affiliate Number":
              if (!enterpriseAffiliateUpdated) {
                if (BUSINESS_PARTNER.equals(custTypeCd)) {
                  engineData.addNegativeCheckStatus("ENT_AFF_BUSPR", "Enterprise/Affiliate change on a Business Partner record needs validation.");
                  failedChecks.put("ENT_AFF_BUSPR", "Enterprise/Affiliate change on a Business Partner record needs validation.");
                } else {
                  String error = performEnterpriseAffiliateCheck(cedpManager, entityManager, requestData);
                  if (StringUtils.isNotBlank(error)) {
                    if ("BG_ERROR".equals(error)) {
                      hasNegativeCheck = true;
                      failedChecks.put(error,
                          "The projected global buying group during Enterprise/Affiliate checks did not match the one on the request.");
                    } else {
                      LOG.debug(error);
                      output.setDetails(error);
                      validation.setMessage("Validation Failed");
                      validation.setSuccess(false);
                      if (StringUtils.isBlank(admin.getSourceSystId())) {
                        engineData.addRejectionComment("OTH", error, "", "");
                        output.setOnError(true);
                      } else {
                        engineData.addNegativeCheckStatus("BP_" + field, error);
                      }
                      return true;
                    }
                  }
                }
                enterpriseAffiliateUpdated = true;
              }
              break;
            case "ISIC":
            case "SICMEN":
              if (!isicCheckDone) {
                String error = performISICCheck(cedpManager, entityManager, requestData, updatedDataModel);
                if (StringUtils.isNotBlank(error)) {
                  LOG.debug(error);
                  output.setDetails(error);
                  validation.setMessage("Validation Failed");
                  validation.setSuccess(false);
                  if (StringUtils.isBlank(admin.getSourceSystId())) {
                    engineData.addRejectionComment("OTH", error, "", "");
                    output.setOnError(true);
                  } else {
                    engineData.addNegativeCheckStatus("BP_" + field, error);
                  }
                  return true;
                }
                isicCheckDone = true;
              }
              break;

            case "Abbreviated Name (TELX1)":
            case "PCC A/R Department":
            case "SVC A/R Office":
            case "Size Code":
            case "CAP Record":
            case "iERP Site Party ID":
            case "Restricted":
              // SKIP THESE FIELDS
              break;
            default:
              boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
              if ("BP Relation Type".equals(field) && payGoAddredited) {
                // NOOP
              } else {
                // Set Negative check status for any other fields updated.
                failedChecks.put(field, field + " updated.");
                hasNegativeCheck = true;
              }
              break;
            }
          }
        }

        if ("CSP".equals(admin.getReqReason())) {
          String error = performCSPCheck(cedpManager, entityManager, data, admin);
          if (StringUtils.isNotBlank(error)) {
            LOG.debug(error);
            output.setDetails(error);
            validation.setMessage("Validation Failed");
            validation.setSuccess(false);
            if (StringUtils.isBlank(admin.getSourceSystId())) {
              engineData.addRejectionComment("OTH", error, "", "");
              output.setOnError(true);
            } else {
              engineData.addNegativeCheckStatus("BP_CSP", error);
            }
            return true;
          }
        }

        boolean isPayGo = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
        if (changes.isLegalNameChanged() && !isPayGo) {
          hasNegativeCheck = validateLegalNameChange(requestData, failedChecks);
        }
        
      } finally {
        cedpManager.clear();
        cedpManager.close();
      }
      if (hasNegativeCheck) {
        engineData.addNegativeCheckStatus("RESTRICED_DATA_UPDATED", "Updated elements cannot be checked automatically.");
        LOG.debug("Updated elements cannot be checked automatically.");
        output.setDetails("Updated elements cannot be checked automatically.\n");
        if (failedChecks != null && failedChecks.size() > 0) {
          StringBuilder details = new StringBuilder();
          details.append("Updated elements cannot be checked automatically.\nDetails:").append("\n");
          for (String failedCheck : failedChecks.values()) {
            details.append(" - " + failedCheck).append("\n");
          }
          output.setDetails(details.toString());
        }
        validation.setMessage("Review needed");
        validation.setSuccess(false);
      } else {
        output.setDetails("Updated DATA elements were validated successfully.\n");
        validation.setMessage("Validated");
        validation.setSuccess(true);
      }
    }
    return true;
  }

  private boolean validateLegalNameChange(RequestData requestData, Map<String, String> failedChecks) throws Exception {
    MatchingResponse<DnBMatchingResponse> dnbResponse = DnBUtil.getMatches(requestData, null, "ZS01");
    if (dnbResponse != null && DnBUtil.hasValidMatches(dnbResponse)) {
      return false;
    } else {
      failedChecks.put("CUST_NM_UPDATED", "The request needs further review: legal name change should be validated");
      return true;
    }
  }

  /**
   * Checks to perform if ISIC/SICMEN updated
   * 
   * @param cedpManager
   * @param entityManager
   * @param requestData
   * @param updatedDataModel
   * @return a string with error message if some issues encountered during
   *         checks, null if validated.
   * @throws Exception
   */
  private String performISICCheck(EntityManager cedpManager, EntityManager entityManager, RequestData requestData, UpdatedDataModel updatedDataModel)
      throws Exception {
    Data data = requestData.getData();
    String updatedValue = updatedDataModel.getNewData();
    String error = "The CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle .\nLink:- https://jsw.ibm.com/projects/CMDE/summary";
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_CMR_REVENUE");
    PreparedQuery query = new PreparedQuery(cedpManager, sql);
    query.setParameter("CMR_NO", data.getCmrNo());
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      BigDecimal revenue = new BigDecimal(0);
      if (results.get(0)[1] != null) {
        revenue = (BigDecimal) results.get(0)[1];
      }
      if (revenue.floatValue() > 100000) {
        return error + "\n- CMR with revenue > 100K";
      } else if (revenue.floatValue() == 0) {
        String dunsNo = "";
        if (StringUtils.isNotBlank(data.getDunsNo())) {
          dunsNo = data.getDunsNo();
        } else {
          MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, null, "ZS01");
          if (response != null && DnBUtil.hasValidMatches(response)) {
            DnBMatchingResponse dnbRecord = response.getMatches().get(0);
            if (dnbRecord.getConfidenceCode() >= 8) {
              dunsNo = dnbRecord.getDunsNo();
            }
          }
        }

        if (StringUtils.isNotBlank(dunsNo)) {
          DnBCompany dnbData = DnBUtil.getDnBDetails(dunsNo);
          if (dnbData != null && StringUtils.isNotBlank(dnbData.getIbmIsic())) {
            if (!dnbData.getIbmIsic().equals(updatedValue)) {
              return error + "\n- Requested ISIC did not match value in D&B";
            } else {
              sql = ExternalizedQuery.getSql("AUTO.US.GET_ISU_BY_ISIC");
              query = new PreparedQuery(entityManager, sql);
              query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
              query.setParameter("ISIC", updatedValue);
              query.setForReadOnly(true);
              String brsch = query.getSingleResult(String.class);
              if (!data.getIsuCd().equals(brsch)) {
                return error + "\n- ISU/Industry impact";
              } else {
                // check if isic and sicmen are equal if not set them equal
                if (data.getIsicCd() != null && !data.getIsicCd().equals(data.getUsSicmen())) {
                  if ("ISIC".equals(updatedDataModel.getDataField())) {
                    data.setUsSicmen(updatedValue);
                  } else {
                    data.setIsicCd(updatedValue);
                  }
                }

              }
            }
          } else {
            return error + "\n- Isic is blank";
          }
        } else {
          return error + "\n- Duns No. is blank";
        }
      }
    }
    return null;
  }

  /**
   * Checks to perform if Enterprise or Affiliate field updated.
   * 
   * @param cedpManager
   * @param entityManager
   * @param requestData
   * @return An error message if validation failed, null if validated.
   * @throws Exception
   */
  private String performEnterpriseAffiliateCheck(EntityManager cedpManager, EntityManager entityManager, RequestData requestData) throws Exception {
    Data data = requestData.getData();
    String error = "The CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle.\nLink:- https://jsw.ibm.com/projects/CMDE/summary";
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_CMR_REVENUE");
    PreparedQuery query = new PreparedQuery(cedpManager, sql);
    query.setParameter("CMR_NO", data.getCmrNo());
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      BigDecimal revenue = new BigDecimal(0);
      if (results.get(0)[1] != null) {
        revenue = (BigDecimal) results.get(0)[1];
      }
      if (revenue.floatValue() > 0) {
        return error + "\n- CMR with revenue";
      } else if (revenue.floatValue() == 0) {
        sql = ExternalizedQuery.getSql("AUTO.US.AFF_ENT_DUNS_CHECK");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        query.setParameter("CMR_NO", data.getCmrNo());
        query.setParameter("ENTERPRISE", data.getEnterprise());
        query.setParameter("AFFILIATE", data.getAffiliate());
        query.setForReadOnly(true);
        results = query.getResults(1);
        if (results != null && !results.isEmpty()) {
          // String guDunsNo = (String) results.get(0)[0];
          String gbgIdDb = (String) results.get(0)[1];

          CmrClientService odmService = new CmrClientService();
          RequestEntryModel model = requestData.createModelFromRequest();
          Addr soldTo = requestData.getAddress("ZS01");
          ModelMap response = new ModelMap();

          odmService.getBuyingGroup(entityManager, soldTo, model, response);
          String gbgId = (String) response.get("globalBuyingGroupID");
          if (StringUtils.isBlank(gbgId)) {
            gbgId = gbgIdDb;
          }

          if (StringUtils.isBlank(gbgId) || (StringUtils.isNotBlank(gbgId) && !gbgId.equals(data.getGbgId()))) {
            return "BG_ERROR";
          }

        } else {
          return error + "\n- Target enterprise/affiliate is not under the same GU DUNs/parent";
        }
      }
    }
    return null;
  }

  /**
   * Performs check if ISU updated to 5B or the request is a CSP update
   * 
   * @param cedpManager
   * @param entityManager
   * @param data
   * @param admin
   * @return error message if any encountered, null if validated
   */
  private String performCSPCheck(EntityManager cedpManager, EntityManager entityManager, Data data, Admin admin) {
    if (!BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), SystemParameters.getString("US.CSP_HEAD"))) {
      return "Only members of the CSP team can request for converting a CMR to CSP.  Kindly check with CMDE or your manager.";
    } else {
      String sql = ExternalizedQuery.getSql("AUTO.US.CHECK_CSP_VALID");
      PreparedQuery query = new PreparedQuery(cedpManager, sql);
      query.setParameter("CMR_NO", data.getCmrNo());
      query.setForReadOnly(true);
      List<Object[]> results = query.getResults(1);
      if (results != null && results.size() > 0) {
        String creationCapChanged = (String) results.get(0)[2];
        String sicValidation = (String) results.get(0)[3];
        String revenue = (String) results.get(0)[4];
        String error = "The CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle.\nLink:- https://jsw.ibm.com/projects/CMDE/summary";
        if (!"Ok".equals(creationCapChanged) || !"Ok".equals(sicValidation) || !"Ok".equals(revenue)) {
          if (!"Ok".equals(revenue)) {
            error += "\n- CMR with revenue";
          }
          if (!"Ok".equals(creationCapChanged)) {
            error += "\n- Not new CMR (for CMR pass the 30 days period)";
          }
          if (!"Ok".equals(sicValidation)) {
            error += "\n- ISU-ISIC validation failed";
          }
          return error;

        } else {
          sql = ExternalizedQuery.getSql("AUTO.US.GET_CSP_AFFILIATE");
          query = new PreparedQuery(cedpManager, sql);
          query.setParameter("CMR_NO", data.getCmrNo());
          query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          query.setForReadOnly(true);
          results = query.getResults(1);
          if (results != null && results.size() > 0) {
            String konzs = (String) results.get(0)[3];
            if (StringUtils.isNotBlank(konzs)) {
              data.setAffiliate(konzs);
              data.setCustClass("52");
              entityManager.merge(data);
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    // init
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    List<Addr> addresses = null;
    // D - duplicates, R - review
    Set<String> resultCodes = new HashSet<String>();

    LOG.debug("Verifying PayGo Accreditation for " + admin.getSourceSystId());
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    String processingType = RequestUtils.getProcessingType(entityManager, data.getCmrIssuingCntry());
    boolean isOnlyPayGoUpdated = changes != null && changes.isAddressChanged("PG01") && !changes.isAddressChanged("ZS01")
        && !changes.isAddressChanged("ZI01");
    // do an initial check for PayGo cmrs
    /*
     * if (payGoAddredited && "PG".equals(data.getOrdBlk())) {
     * LOG.debug("Allowing name/address changes for PayGo accredited partner " +
     * admin.getSourceSystId() + " Request: " + admin.getId().getReqId());
     * output.
     * setDetails("Skipped checks on updates to PayGo CMR. Partner accredited for PayGo.\n"
     * ); validation.setMessage("Validated"); validation.setSuccess(true);
     * return true; }
     */
    String sqlKey = ExternalizedQuery.getSql("AUTO.US.CHECK_CMDE");
    PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
    query.setParameter("EMAIL", admin.getRequesterId());
    query.setForReadOnly(true);
    if (query.exists() && "Y".equals(SystemParameters.getString("US.SKIP_UPDATE_CHECK"))) {
      // skip checks if requester is from USCMDE team
      LOG.debug("Requester is from CMDE team, skipping Update checks.");
      output.setDetails("Requester is from CMDE team, skipping Update checks.\n");
      validation.setMessage("Update checks Skipped");
      validation.setSuccess(true);
    } else if (StringUtils.isNotEmpty(admin.getSourceSystId()) && admin.getSourceSystId().equals("FedCMR")) {
      // skip computation for federal source
    } else {
      String dataDetails = output.getDetails() != null ? output.getDetails() : "";
      StringBuilder details = new StringBuilder(dataDetails);
      details.append("\n");
      USDetailsContainer detailsCont = determineUSCMRDetails(entityManager, requestData.getData().getCmrNo());
      String custTypCd = detailsCont.getCustTypCd();

      // check addresses
      if (StringUtils.isNotBlank(custTypCd) && !"NA".equals(custTypCd)) {
        if (BUSINESS_PARTNER.equals(custTypCd) || LEASING.equals(custTypCd) && !isOnlyPayGoUpdated) {
          engineData.addNegativeCheckStatus("UPD_REVIEW_NEEDED",
              "Address updates for " + (custTypCd.equals(LEASING) ? "Leasing" : "Business Partner") + " scenario found.");
          details.append("Address updates for " + (custTypCd.equals(LEASING) ? "Leasing" : "Business Partner")
              + " scenario found. Processor review will be required.").append("\n");
          validation.setMessage("Review needed");
          validation.setSuccess(false);
        } else {
          List<String> addrTypesChanged = new ArrayList<String>();
          for (UpdatedNameAddrModel addrModel : changes.getAddressUpdates()) {
            if (!addrTypesChanged.contains(addrModel.getAddrTypeCode())) {
              addrTypesChanged.add(addrModel.getAddrTypeCode());
            }
          }
          boolean isBPAccount = false;
          if (StringUtils.isNotBlank(data.getRestrictTo())) {
            if ("BPQS".equals(data.getRestrictTo()) || "IRCSO".equals(data.getRestrictTo())) {
              isBPAccount = true;
            }
          }

          if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZS01.toString())
              && (!payGoAddredited || ("".equals(data.getOrdBlk()) && payGoAddredited)) && (!isBPAccount)) {
            closelyMatchAddressWithDnbRecords(entityManager, requestData, engineData, "ZS01", details, validation, output);
          }
          if (relevantAddressFieldForUpdates(changes, requestData.getAddress("ZS01"))) {
            matchAddressWithSosRecords(entityManager, requestData, engineData, "ZS01", details, validation, output);
          }

          if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZI01.toString()) && !payGoAddredited) {
            UpdatedNameAddrModel addrTxt = changes.getAddressChange("ZI01", "Address");
            UpdatedNameAddrModel addrTxt2 = changes.getAddressChange("ZI01", "Address Cont");
            if (addrTxt != null) {
              boolean immutableAddrFound = false;
              List<String> immutableAddrList = Arrays.asList("150 KETTLETOWN RD", "6303 BARFIELD RD", "PO BOX 12195 BLDG 061", "1 N CASTLE DR",
                  "7100 HIGHLANDS PKWY", "294 ROUTE 100", "6710 ROCKLEDGE DR");
              String oldStreetAddress = addrTxt.getOldData().trim();
              if (addrTxt2 != null && StringUtils.isNotBlank(addrTxt2.getOldData())) {
                oldStreetAddress += (" " + addrTxt2.getOldData().trim());
              }
              for (String streetAddr : immutableAddrList) {
                if (oldStreetAddress.contains(streetAddr)) {
                  immutableAddrFound = true;
                  break;
                }
              }
              if (immutableAddrFound) {
                engineData.addRejectionComment("OTH", "Invoice-to address cannot be modified.", "", "");
                details.append("Invoice-to address cannot be modified.").append("\n");
                validation.setMessage("Review needed");
                validation.setSuccess(false);
                output.setDetails(details.toString());
                output.setOnError(true);
                return true;
              }
            }

            if (isRelevantAddressFieldUpdated(changes, requestData.getAddress("ZI01"))
                && (!payGoAddredited || ("".equals(data.getOrdBlk()) && payGoAddredited)) && (!isBPAccount)) {
              closelyMatchAddressWithDnbRecords(entityManager, requestData, engineData, "ZI01", details, validation, output);
            }
            if (relevantAddressFieldForUpdates(changes, requestData.getAddress("ZI01"))) {
              matchAddressWithSosRecords(entityManager, requestData, engineData, "ZI01", details, validation, output);
            }
          }

          if (payGoAddredited && addrTypesChanged.contains(CmrConstants.RDC_PAYGO_BILLING.toString())) {
            addresses = requestData.getAddresses(CmrConstants.RDC_PAYGO_BILLING.toString());
            for (Addr addr : addresses) {
              String addrType = addr.getId().getAddrType();
              if ("N".equals(addr.getImportInd()) && !StringUtils.isEmpty(addr.getExtWalletId())) {
                LOG.debug("Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                boolean duplicate = addressExists(entityManager, addr);
                if (duplicate) {
                  LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  details.append("Address " + addrType + "(" + addr.getId().getAddrSeq() + ") provided matches an existing Bill-To address.\n");
                  resultCodes.add("D");
                } else {
                  LOG.debug(" - NO duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                  details.append(" - NO duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")" + "with same attentionTo");
                }
              }
            }
          }
        }

        if (resultCodes.contains("D")) {
          // prioritize duplicates, set error
          output.setOnError(true);
          engineData.addRejectionComment("DUPADDR", "One or more new addresses matches existing addresses on record.", "", "");
          validation.setSuccess(false);
          validation.setMessage("Duplicate Address");

        }

        // check if validated
        if (validation.isSuccess()) {
          output.setDetails("Updated ADDRESS elements were validated successfully.\n");
          validation.setMessage("Validated");
          validation.setSuccess(true);
        }
      } else if (!StringUtils.isEmpty(processingType) && "TC".equals(processingType)) {
        validation.setSuccess(false);
        validation.setMessage("Unknown CustType");
        details.append("Customer Type could not be determined. Update checks for address could not be run.").append("\n");
        output.setOnError(true);
      } else {
        output.setDetails("Updated ADDRESS elements were validated successfully.\n");
        validation.setMessage("Validated");
        validation.setSuccess(true);
      }
      output.setDetails(details.toString());

    }

    return true;

  }

  /**
   * Validates if address closely matches with SOS records matched.
   * 
   * @param requestData
   * @param engineData
   * @param addrType
   * @param details
   * @param validation
   * @throws Exception
   */
  private void matchAddressWithSosRecords(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData, String addrType,
      StringBuilder details, ValidationOutput validation, AutomationResult<ValidationOutput> output) throws Exception {
    String addrDesc = "ZS01".equals(addrType) ? "Install-at" : "Invoice-at";
    Addr addr = requestData.getAddress(addrType);
    Admin admin = requestData.getAdmin();
    AutomationResponse<SosResponse> response = USSosRpaCheckElement.getSosMatches(admin.getId().getReqId(), addr, admin);
    if (response != null && response.isSuccess() && response.getRecord() != null) {
      details.append(addrDesc + " address details matched successfully with SOS.").append("\n");
    } else {
      /*
       * engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_" + "ZS01",
       * "SOS Matching couldn't be performed for " + addrDesc + " address.");
       */
      details.append(addrDesc + " address details did not match with SOS.").append("\n");
    }
    validation.setMessage("Validated.");
    validation.setSuccess(true);
  }

  /**
   * Validates if address closely matches with DnB records matched.
   * 
   * @param requestData
   * @param engineData
   * @param addrType
   * @param details
   * @param validation
   * @throws Exception
   */
  private void closelyMatchAddressWithDnbRecords(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      String addrType, StringBuilder details, ValidationOutput validation, AutomationResult<ValidationOutput> output) throws Exception {
    String addrDesc = "ZS01".equals(addrType) ? "Install-at" : "Invoice-at";
    Addr addr = requestData.getAddress(addrType);
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, engineData, addrType);
    if (response.getSuccess()) {
      if (response.getMatched() && !response.getMatches().isEmpty()) {
        if (DnBUtil.hasValidMatches(response)) {
          boolean isAddressMatched = false;
          for (DnBMatchingResponse record : response.getMatches()) {
            if (record.getConfidenceCode() > 7 && DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), addr, admin, record)) {
              isAddressMatched = true;
              break;
            }
          }
          if (isAddressMatched) {
            details.append(addrDesc + " address details matched successfully with High Quality D&B Matches.").append("\n");
            validation.setMessage("Validated.");
            validation.setSuccess(true);
          } else {
            // company proof
            // if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager,
            // admin.getId().getReqId())) {
            validation.setMessage("Validated");
            details.append("High confidence D&B matches did not match the " + addrDesc + " address data.").append("\n");
            details.append("Supporting documentation is provided by the requester as attachment for " + addrDesc).append("\n");
            validation.setSuccess(true);
            /*
             * } else { validation.setMessage("Rejected");
             * validation.setSuccess(false);
             * details.append("High confidence D&B matches did not match the " +
             * addrDesc + " address data.").append("\n"); details.
             * append("\nNo supporting documentation is provided by the requester for "
             * + addrDesc + " address."); engineData.addRejectionComment("OTH",
             * "No supporting documentation is provided by the requester for " +
             * addrDesc + " address.", "", ""); output.setOnError(true);
             * output.setDetails(details.toString()); if (payGoAddredited) {
             * admin.setPaygoProcessIndc("Y"); } LOG.
             * debug("D&B matches were chosen to be overridden by the requester and needs to be reviewed"
             * ); }
             */
          }
        } else {
          // company proof
          // if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager,
          // admin.getId().getReqId())) {
          validation.setMessage("Validated");
          details.append("No High Quality D&B Matches were found for " + addrDesc + " address.").append("\n");
          details.append("Supporting documentation is provided by the requester as attachment for " + addrDesc).append("\n");
          validation.setSuccess(true);
          /*
           * } else { validation.setMessage("Rejected");
           * validation.setSuccess(false);
           * details.append("No High Quality D&B Matches were found for " +
           * addrDesc + " address.").append("\n"); details.
           * append("\nNo supporting documentation is provided by the requester for "
           * + addrDesc + " address."); engineData.addRejectionComment("OTH",
           * "No supporting documentation is provided by the requester for " +
           * addrDesc + " address.", "", ""); output.setOnError(true);
           * output.setDetails(details.toString()); if (payGoAddredited) {
           * admin.setPaygoProcessIndc("Y"); } LOG.
           * debug("D&B matches were chosen to be overridden by the requester and needs to be reviewed"
           * ); }
           */
        }
      } else {
        // company proof
        // if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager,
        // admin.getId().getReqId())) {
        validation.setMessage("Validated");
        details.append("No D&B Matches were found for " + addrDesc + " address.").append("\n");
        details.append("Supporting documentation is provided by the requester as attachment for " + addrDesc).append("\n");
        validation.setSuccess(true);
        /*
         * } else { validation.setMessage("Rejected");
         * validation.setSuccess(false);
         * details.append("No D&B Matches were found for " + addrDesc +
         * " address.").append("\n"); engineData.addRejectionComment("OTH",
         * "No supporting documentation is provided by the requester for " +
         * addrDesc + " address.", "", ""); output.setOnError(true);
         * output.setDetails(details.toString()); if (payGoAddredited) {
         * admin.setPaygoProcessIndc("Y"); } LOG.
         * debug("D&B matches were chosen to be overridden by the requester and needs to be reviewed"
         * ); }
         */
      }
    } else {
      engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_" + "ZS01", "D&B Matching couldn't be performed for " + addrDesc + " address.");
      details.append("D&B Matching couldn't be performed for " + addrDesc + " address.").append("\n");
      validation.setMessage("Review needed");
      validation.setSuccess(false);
    }
  }

  /**
   * determines CustSubScenario for Model CMR requests/Update requests
   * 
   * @param entityManager
   * @param cmrNo
   * @return
   * @throws Exception
   */
  public static String determineCustSubScenario(EntityManager entityManager, String cmrNo, AutomationEngineData engineData, RequestData requestData)
      throws Exception {
    // get request admin and data

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    String custSubGroup = "";

    String custTypCd = "";
    String bpAccTyp = "";
    String usRestricTo = "";
    String companyNo = "";
    String mtkgArDept = "";

    String custClass = "";
    String isicCd = "";
    String subIndustryCd = "";
    String affiliate = "";

    USDetailsContainer usDetails = determineUSCMRDetails(entityManager, cmrNo, requestData);

    custTypCd = usDetails.getCustTypCd();
    usRestricTo = usDetails.getUsRestrictTo();
    companyNo = usDetails.getCompanyNo();
    bpAccTyp = usDetails.getBpAccTyp();
    mtkgArDept = usDetails.getMktgArDept();

    // get RDC values
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_RDC_VALUES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      custClass = (String) results.get(0)[0];
      isicCd = (String) results.get(0)[1];
      subIndustryCd = (String) results.get(0)[2];
      affiliate = (String) results.get(0)[3];
    }

    // get Customer Type
    if (StringUtils.isNotBlank(admin.getModelCmrNo())) {
      String modelcmrNo = admin.getModelCmrNo();

      String sqlcust = ExternalizedQuery.getSql("AUTO.US.GET_CUSTTYPE");
      PreparedQuery querycust = new PreparedQuery(entityManager, sqlcust);
      querycust.setParameter("CMR_NO", modelcmrNo);
      querycust.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      querycust.setForReadOnly(true);
      String result = querycust.getSingleResult(String.class);

      if (result != null) {
        custTypCd = result;
      }
      LOG.debug("--------- Customer Type is : " + custTypCd);
    }

    if (StringUtils.isBlank(custTypCd)) {
      if (!StringUtils.isBlank(admin.getCustType())) {
        custTypCd = admin.getCustType();
        LOG.debug("--------- Customer Type is From admin : " + custTypCd);
      }
    }

    // US restrict to LOV mapping
    String usRestrictToLOV = "";
    if (StringUtils.isNotBlank(usRestricTo) && StringUtils.isNumeric(usRestricTo)) {
      sql = ExternalizedQuery.getSql("AUTO.US.GET_US_RESTR_TO_LOV");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("RESTRICT_TO", usRestricTo);
      usRestrictToLOV = query.getSingleResult(String.class);
    } else {
      usRestrictToLOV = usRestricTo;
    }

    // determine cust scenarios
    if ((requestData.getAdmin().getSourceSystId() != null) && requestData.getAdmin().getSourceSystId().equals("FedCMR")) {
      if (requestData.getAdmin().getCustType().equals(FEDERAL)) {
        custSubGroup = SC_FED_REGULAR;
      } else if (requestData.getAdmin().getCustType().equals(POWER_OF_ATTORNEY)) {
        custSubGroup = SC_FED_POA;
      }
      return custSubGroup;
    }

    if (COMMERCIAL.equals(custTypCd)) {
      if (StringUtils.isNotBlank(usRestrictToLOV)) {
        // sql = ExternalizedQuery.getSql("AUTO.US.GET_US_RESTR_TO_LOV");
        // query = new PreparedQuery(entityManager, sql);
        // query.setParameter("RESTRICT_TO", usRestricTo);
        // String usRestrictToLOV = query.getSingleResult(String.class);
        // US restrict to filters
        if ("OIO".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_OIO;
        } else if ("OEMHQ".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_OEMHW;
        } else if ("OEMHQ".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_OEMSW;
        } else if ("TPD".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_TPD;
        } else if ("SSD".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_SSD;
        } else if ("DB4".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_DB4;
        } else if ("GRNTS".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_GRNTS;
        } else if ("LBPS".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_LBPS;
        } else if ("LIIS".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_LIIS;
        } else if ("RFBPO".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_RFBPO;
        } else if ("SSI".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_SSI;
        } else if ("ICC".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_ICC;
        } else if ("SVMP".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_SVMP;
        }
      } else {
        // affiliate filters with condition of usRestrictTo=blank
        if ("2539231".equals(affiliate)) {
          custSubGroup = SC_DOMINO;
        } else if ("4276400".equals(affiliate)) {
          custSubGroup = SC_HILTON;
        } else if ("3435500".equals(affiliate)) {
          custSubGroup = SC_FLORIDA;
        }
      }

      if (StringUtils.isBlank(custSubGroup)) {
        // If still no cust group found
        // other filters in order of
        // 1.company filters
        // 2.isic filters
        // 3.kukla filters
        if ("12554525".equals(companyNo)) {
          custSubGroup = SC_DUMMY;
        } else if ("9500".equals(isicCd)) {
          custSubGroup = SC_PVT_HOUSEHOLD;
        } else if ("5159".equals(isicCd) && "672".equals(mtkgArDept)) {
          custSubGroup = SC_BROKER;
        } else if ("85".equals(custClass)) {
          custSubGroup = SC_IGS;
        } else if ("81".equals(custClass)) {
          custSubGroup = SC_IGSF;
        } else if ("52".equals(custClass)) {
          custSubGroup = SC_CSP;
        } else if (("11".equals(custClass) || "18".equals(custClass) || "19".equals(custClass)) && StringUtils.isBlank(usRestricTo)) {
          custSubGroup = SC_COMM_REGULAR;
        } else if ("BYMODEL".equals(data.getCustSubGrp())) {
          custSubGroup = SC_BYMODEL;
        }
      }
    } else if (STATE_LOCAL.equals(custTypCd)) {
      if (("13".equals(custClass) || "14".equals(custClass) || "16".equals(custClass) || "17".equals(custClass))
          && StringUtils.isBlank(usRestricTo)) {
        custSubGroup = SC_STATE_DIST;
      } else {
        custSubGroup = SC_STATE;
      }
      engineData.addPositiveCheckStatus("SKIP_COMP_CHECK");
    } else if (LEASING.equals(custTypCd)) {
      custSubGroup = SC_LEASE_3CC;
      engineData.addPositiveCheckStatus("SKIP_COMP_CHECK");
    } else if (FEDERAL.equals(custTypCd)) {
      if ("12".equals(custClass)) {
        custSubGroup = SC_FED_REGULAR;
      }
      engineData.addPositiveCheckStatus("SKIP_COMP_CHECK");
    } else if (POWER_OF_ATTORNEY.equals(custTypCd)) {
      if ("15".equals(custClass) && StringUtils.isNotBlank(subIndustryCd) && !subIndustryCd.startsWith("Y")) {
        custSubGroup = SC_FED_FEDSTATE;
      } else if ("15".equals(custClass) && StringUtils.isNotBlank(subIndustryCd) && subIndustryCd.startsWith("Y")) {
        custSubGroup = SC_FED_POA;
      }
    } else if (INTERNAL.equals(custTypCd)) {
      if ("81".equals(custClass)) {
        custSubGroup = SC_INTERNAL;
      }
    } else if (BUSINESS_PARTNER.equals(custTypCd)) {
      if ("P".equals(bpAccTyp)) {
        custSubGroup = SC_BP_POOL;
      } else if ("D".equals(bpAccTyp)) {
        custSubGroup = SC_BP_DEVELOP;
      } else if ("E".equals(bpAccTyp)) {
        Addr zs01 = requestData.getAddress("ZS01");
        String divn = zs01.getDivn();
        String dept = zs01.getDept();
        if ((StringUtils.isNotBlank(divn) && (divn.toUpperCase().contains("e-host".toUpperCase())
            || divn.toUpperCase().contains("ehost".toUpperCase()) || divn.toUpperCase().contains("e host".toUpperCase())))
            || (StringUtils.isNotBlank(dept) && (dept.toUpperCase().contains("e-host".toUpperCase())
                || dept.toUpperCase().contains("ehost".toUpperCase()) || dept.toUpperCase().contains("e host".toUpperCase())))) {
          custSubGroup = SC_BP_E_HOST;
        } else if ("IRCSO".equals(usRestrictToLOV) || "BPQS".equals(usRestrictToLOV)) {
          custSubGroup = SC_BP_END_USER;
        }
      }
    }
    return custSubGroup;

  }

  @Override
  public String getOriginalScenarioForRevivedCMRs(EntityManager entityManager, String cmrNo) throws Exception {
    // get request admin and data
    String custSubGroup = "";

    String custTypCd = "";
    String bpAccTyp = "";
    String usRestricTo = "";
    String companyNo = "";
    String mtkgArDept = "";

    String custClass = "";
    String isicCd = "";
    String subIndustryCd = "";
    String affiliate = "";

    USDetailsContainer usDetails = determineUSCMRDetails(entityManager, cmrNo);

    custTypCd = usDetails.getCustTypCd();
    usRestricTo = usDetails.getUsRestrictTo();
    companyNo = usDetails.getCompanyNo();
    bpAccTyp = usDetails.getBpAccTyp();
    mtkgArDept = usDetails.getMktgArDept();

    // get RDC values
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_RDC_VALUES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      custClass = (String) results.get(0)[0];
      isicCd = (String) results.get(0)[1];
      subIndustryCd = (String) results.get(0)[2];
      affiliate = (String) results.get(0)[3];
    }

    // US restrict to LOV mapping
    String usRestrictToLOV = "";
    if (StringUtils.isNotBlank(usRestricTo) && StringUtils.isNumeric(usRestricTo)) {
      sql = ExternalizedQuery.getSql("AUTO.US.GET_US_RESTR_TO_LOV");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("RESTRICT_TO", usRestricTo);
      usRestrictToLOV = query.getSingleResult(String.class);
    } else {
      usRestrictToLOV = usRestricTo;
    }

    // determine cust scenarios
    if (COMMERCIAL.equals(custTypCd)) {
      if (StringUtils.isNotBlank(usRestrictToLOV)) {
        // sql = ExternalizedQuery.getSql("AUTO.US.GET_US_RESTR_TO_LOV");
        // query = new PreparedQuery(entityManager, sql);
        // query.setParameter("RESTRICT_TO", usRestricTo);
        // String usRestrictToLOV = query.getSingleResult(String.class);
        // US restrict to filters
        if ("OIO".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_OIO;
        } else if ("OEMHQ".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_OEMHW;
        } else if ("OEMHQ".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_OEMSW;
        } else if ("TPD".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_TPD;
        } else if ("SSD".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_SSD;
        } else if ("DB4".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_DB4;
        } else if ("GRNTS".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_GRNTS;
        } else if ("LBPS".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_LBPS;
        } else if ("LIIS".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_LIIS;
        } else if ("RFBPO".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_RFBPO;
        } else if ("SSI".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_SSI;
        } else if ("ICC".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_ICC;
        } else if ("SVMP".equals(usRestrictToLOV)) {
          custSubGroup = SC_REST_SVMP;
        }
      } else {
        // affiliate filters with condition of usRestrictTo=blank
        if ("2539231".equals(affiliate)) {
          custSubGroup = SC_DOMINO;
        } else if ("4276400".equals(affiliate)) {
          custSubGroup = SC_HILTON;
        } else if ("3435500".equals(affiliate)) {
          custSubGroup = SC_FLORIDA;
        }
      }

      if (StringUtils.isBlank(custSubGroup)) {
        // If still no cust group found
        // other filters in order of
        // 1.company filters
        // 2.isic filters
        // 3.kukla filters
        if ("12554525".equals(companyNo)) {
          custSubGroup = SC_DUMMY;
        } else if ("9500".equals(isicCd)) {
          custSubGroup = SC_PVT_HOUSEHOLD;
        } else if ("5159".equals(isicCd) && "672".equals(mtkgArDept)) {
          custSubGroup = SC_BROKER;
        } else if ("85".equals(custClass)) {
          custSubGroup = SC_IGS;
        } else if ("81".equals(custClass)) {
          custSubGroup = SC_IGSF;
        } else if ("52".equals(custClass)) {
          custSubGroup = SC_CSP;
        } else if (("11".equals(custClass) || "18".equals(custClass) || "19".equals(custClass)) && StringUtils.isBlank(usRestricTo)) {
          custSubGroup = SC_COMM_REGULAR;
        }
      }
    } else if (STATE_LOCAL.equals(custTypCd)) {
      if (("13".equals(custClass) || "14".equals(custClass) || "16".equals(custClass) || "17".equals(custClass))
          && StringUtils.isBlank(usRestricTo)) {
        custSubGroup = SC_STATE_DIST;
      } else {
        custSubGroup = SC_STATE;
      }
    } else if (LEASING.equals(custTypCd)) {
      custSubGroup = SC_LEASE_3CC;
    } else if (FEDERAL.equals(custTypCd)) {
      if ("12".equals(custClass)) {
        custSubGroup = SC_FED_REGULAR;
      }
    } else if (POWER_OF_ATTORNEY.equals(custTypCd)) {
      if ("15".equals(custClass) && StringUtils.isNotBlank(subIndustryCd) && !subIndustryCd.startsWith("Y")) {
        custSubGroup = SC_FED_FEDSTATE;
      } else if ("15".equals(custClass) && StringUtils.isNotBlank(subIndustryCd) && subIndustryCd.startsWith("Y")) {
        custSubGroup = SC_FED_POA;
      }
    } else if (INTERNAL.equals(custTypCd)) {
      if ("81".equals(custClass)) {
        custSubGroup = SC_INTERNAL;
      }
    } else if (BUSINESS_PARTNER.equals(custTypCd)) {
      if ("P".equals(bpAccTyp)) {
        custSubGroup = SC_BP_POOL;
      } else if ("D".equals(bpAccTyp)) {
        custSubGroup = SC_BP_DEVELOP;
      } else if ("E".equals(bpAccTyp)) { // TODO convert to providing divn and
                                         // dept
        // Addr zs01 = requestData.getAddress("ZS01");
        // String divn = zs01.getDivn();
        // String dept = zs01.getDept();
        // if ((StringUtils.isNotBlank(divn) &&
        // (divn.toUpperCase().contains("e-host".toUpperCase())
        // || divn.toUpperCase().contains("ehost".toUpperCase()) ||
        // divn.toUpperCase().contains("e host".toUpperCase())))
        // || (StringUtils.isNotBlank(dept) &&
        // (dept.toUpperCase().contains("e-host".toUpperCase())
        // || dept.toUpperCase().contains("ehost".toUpperCase()) ||
        // dept.toUpperCase().contains("e host".toUpperCase())))) {
        // custSubGroup = SC_BP_E_HOST;
        // } else if ("IRCSO".equals(usRestrictToLOV) ||
        // "BPQS".equals(usRestrictToLOV)) {
        // custSubGroup = SC_BP_END_USER;
        // }
      }
    }
    return custSubGroup;
  }

  @Override
  public boolean performCountrySpecificCoverageCalculationsForRevivedCMRs(RevivedCMRModel revCmrModel, String scenario, String covFrom,
      CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    // String scenarioSubType = "";
    // Data data = requestData.getData();
    // scenarioSubType = data.getCustSubGrp();
    if (SC_REST_KYN.equals(scenario)) {
      // overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA",
      // "ISIC_CD", data.getIsicCd(), "7229");
      // overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA",
      // "US_SICMEN", data.getUsSicmen(), "7229");
      // overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA",
      // "INAC_TYPE", data.getInacType(), "I");
      // overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA",
      // "INAC_CD", data.getInacCd(), "6272");
      // overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA",
      // "AFFILIATE", data.getAffiliate(), "6500871");
      // overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA",
      // "COMPANY", data.getCompany(), "12641341");
      // overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA",
      // "ENTERPRISE", data.getEnterprise(), "6500871");
    }
    return true;
  }

  @Override
  public boolean doCountryFieldComputationsForRevivedCMRs(EntityManager entityManager, RevivedCMRModel revCmrModel) throws Exception {

    USDetailsContainer usDetails = determineFields(entityManager, revCmrModel.getKunnr());
    revCmrModel.setCsoSite(usDetails.getCsoSite());
    revCmrModel.setPccArDept(usDetails.getPccArDept());
    revCmrModel.setMtkgArDept(usDetails.getMktgArDept());
    revCmrModel.setMktgDept(usDetails.getMktgDept());
    revCmrModel.setSvcArOffice(usDetails.getSvrArOffice());
    revCmrModel.setMiscBilling(usDetails.getMiscBilling());
    revCmrModel.setUsSicmen(usDetails.getSicmen());

    revCmrModel.setIsuCd("34");
    revCmrModel.setClientTier("Q");

    // if scenario is OEMSW or OEMHW set isic to 357X
    // if (SC_REST_OEMSW.equals(scenario) || SC_REST_OEMHW.equals(scenario) ||
    // SC_REST_TPD.equals(scenario) || SC_REST_SSD.equals(scenario)
    // || SC_REST_DB4.equals(scenario)) {
    // revCmrModel.setIsicCd("357X");
    // if (SC_REST_TPD.equals(scenario) || SC_REST_SSD.equals(scenario) ||
    // SC_REST_DB4.equals(scenario)) {
    // revCmrModel.setSubIndustryCd("ZC");
    // revCmrModel.setUsSicmen("357X");
    // }
    // }
    //
    // // if scenario is KYN
    // if (SC_REST_KYN.equals(scenario)) {
    // revCmrModel.setIsicCd("7229");
    // revCmrModel.setSubIndustryCd("BD");
    // revCmrModel.setUsSicmen("7229");
    // revCmrModel.setMtkgArDept("SD3");
    // revCmrModel.setSvcArOffice("IJ9");
    //
    // }

    return true;
  }

  public static USDetailsContainer determineUSCMRDetails(EntityManager entityManager, String cmrNo, RequestData requestData) throws Exception {
    // get request admin and data
    if (usDetailsMap.containsKey(cmrNo) && usDetailsMap.get(cmrNo) != null) {
      return usDetailsMap.get(cmrNo);
    }
    USDetailsContainer usDetails = new USDetailsContainer();
    String custTypCd = "NA";
    String entType = "";
    String leasingCo = "";
    String bpAccTyp = "";
    String cGem = "";
    String usRestrictTo = "";
    String companyNo = "";
    String pccArDept = "";
    String mtkgArDept = "";
    String mktgDept = "";
    String csoSite = "";
    String svrArOffice = "";
    String miscBilling = "";
    String sicmen = "";

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      if ("6".equals(data.getCustGrp().trim())) {
        if ("3CC".equals(data.getCustSubGrp().trim())) {
          if ("3".equals(admin.getCustType().trim())) {
            if (!StringUtils.isEmpty(admin.getMainCustNm1()) && !StringUtils.isEmpty(data.getEnterprise())) {
              if ("IBM Credit LLC".equals(admin.getMainCustNm1().trim()) && "4482735".equals(data.getEnterprise().trim())) {
                leasingCo = "45K";
              }
            }
          }
        } else {
          if (!StringUtils.isEmpty(admin.getCustType()) && !StringUtils.isEmpty(data.getEnterprise())) {
            if ("3".equals(admin.getCustType().trim()) && !"4482735".equals(data.getEnterprise().trim())) {
              leasingCo = "00K";
            }
          }
        }
      }
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {

      if (!StringUtils.isEmpty(admin.getMainCustNm1()) && !StringUtils.isEmpty(data.getEnterprise())) {
        if ("IBM Credit LLC".equals(admin.getMainCustNm1().trim()) && "4482735".equals(data.getEnterprise().trim())) {
          leasingCo = "45K";
        }
      }

      if (!StringUtils.isEmpty(data.getEnterprise())) {
        if (!"4482735".equals(data.getEnterprise().trim())) {
          leasingCo = "00K";
        }
      }

    }

    entType = getEntType(entityManager, requestData.getData().getEnterprise());
    bpAccTyp = requestData.getData().getBpAcctTyp();
    cGem = getcGem(entityManager, cmrNo);
    companyNo = requestData.getData().getCompany();
    pccArDept = requestData.getData().getPccArDept();
    mtkgArDept = requestData.getData().getMtkgArDept();

    if ("P".equals(entType)) {
      custTypCd = POWER_OF_ATTORNEY;
    } else if ("F".equals(entType)) {
      custTypCd = FEDERAL;
    } else if ("I".equals(entType)) {
      custTypCd = INTERNAL;
    } else if ("C".equals(entType) || StringUtils.isNotBlank(leasingCo)) {
      custTypCd = LEASING;
    } else if (StringUtils.isNotBlank(bpAccTyp)) {
      custTypCd = BUSINESS_PARTNER;
    } else if ("2".equals(cGem)) {
      custTypCd = STATE_LOCAL;
    } else {
      custTypCd = COMMERCIAL;
    }

    // GET US_RESTRICT_TO_CODE
    String restrictCd = getUSRestrictToCode(entityManager, cmrNo);

    usDetails.setCustTypCd(custTypCd);
    usDetails.setEntType(entType);
    usDetails.setLeasingCo(leasingCo);
    usDetails.setBpAccTyp(bpAccTyp);
    usDetails.setcGem(cGem);
    // usDetails.setUsRestrictTo(usRestrictTo);
    usDetails.setUsRestrictTo(restrictCd);
    usDetails.setCompanyNo(companyNo);
    usDetails.setMktgArDept(mtkgArDept);
    usDetails.setMktgDept(mktgDept);
    usDetails.setCsoSite(csoSite);
    usDetails.setSvrArOffice(svrArOffice);
    usDetails.setMiscBilling(miscBilling);
    usDetails.setSicmen(sicmen);

    usDetails.setPccArDept(pccArDept);
    usDetailsMap.put(cmrNo, usDetails);
    return usDetails;
  }

  public static USDetailsContainer determineFields(EntityManager entityManager, String kunnr) throws Exception {
    // get request admin and data
    // if (usDetailsMap.containsKey(cmrNo) && usDetailsMap.get(cmrNo) != null) {
    // return usDetailsMap.get(cmrNo);
    // }
    USDetailsContainer usDetails = new USDetailsContainer();
    String custTypCd = "NA";
    String entType = "";
    String leasingCo = "";
    String bpAccTyp = "";
    String cGem = "";
    String usRestrictTo = "";
    String companyNo = "";
    String pccArDept = "";
    String mtkgArDept = "";
    String mktgDept = "";
    String csoSite = "";
    String svrArOffice = "";
    String miscBilling = "";
    String sicmen = "";

    String sql = ExternalizedQuery.getSql("GET.KNVVEXT.REVIVED");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KUNNR", kunnr);

    List<KnvvExt> knvvExtList = query.getResults(KnvvExt.class);
    if (knvvExtList == null && knvvExtList.size() == 0) {
      custTypCd = "NA";
    } else {
      for (KnvvExt knvvExt : knvvExtList) {
        pccArDept = knvvExt.getPccArBo();
        mtkgArDept = knvvExt.getMktgArDept();
        mktgDept = knvvExt.getMktgDept();
        csoSite = knvvExt.getCsoSite();
        svrArOffice = knvvExt.getSvcArOfc();
        miscBilling = knvvExt.getMiscBilling();
      }
    }

    usDetails.setMktgArDept(mtkgArDept);
    usDetails.setMktgDept(mktgDept);
    usDetails.setCsoSite(csoSite);
    usDetails.setSvrArOffice(svrArOffice);
    usDetails.setMiscBilling(miscBilling);
    // usDetails.setSicmen(sicmen);

    usDetails.setPccArDept(pccArDept);
    // usDetailsMap.put(cmrNo, usDetails);
    return usDetails;
  }

  public static String getUSRestrictToCode(EntityManager entityManager, String cmrNo) {
    String code = "";
    try {
      LOG.debug("Getting US restrict To code from ADDLCTRYDATA...");
      String sql = ExternalizedQuery.getSql("AUTO.GET_US_RESTRICT_TO");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("CMR_NO", cmrNo);
      query.setForReadOnly(true);
      code = query.getSingleResult(String.class);

      if ("NO_VALUE_RETRIEVED".equalsIgnoreCase(code)) {
        code = "";
      }
    } catch (Exception e) {
      LOG.error("Error in querying the database table ADDLCTRYDATA ! ");
    }

    return code;
  }

  /**
   * Gets SOS-RPA matches for BP end users
   * 
   * @param handler
   * @param requestData
   * @param engineData
   * @return
   * @throws Exception
   */
  public static List<SosResponse> getSosRpaMatchesForBPEndUser(GEOHandler handler, RequestData requestData, AutomationEngineData engineData,
      String addrType) throws Exception {
    List<SosResponse> closeMatch = new ArrayList<SosResponse>();
    AutomationResponse<SosResponse> response = new AutomationResponse<SosResponse>();
    Admin admin = requestData.getAdmin();
    long reqId = admin.getId().getReqId();
    Addr address = requestData.getAddress(addrType);
    if (address != null) {
      AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
          AutomationServiceClient.class);
      autoClient.setReadTimeout(1000 * 60 * 5);
      autoClient.setRequestMethod(Method.Post);

      // calling SOS-RPA Service
      LOG.debug("Calling SOS-RPA Service for" + addrType + " address for Req_id : " + reqId);
      SosRequest requestInstallAt = new SosRequest();
      requestInstallAt.setName((StringUtils.isNotBlank(address.getDivn()) ? address.getDivn() : ""));
      requestInstallAt.setCity1(address.getCity1());
      requestInstallAt.setAddrTxt((StringUtils.isNotBlank(address.getAddrTxt()) ? address.getAddrTxt() : "")
          + (StringUtils.isNotBlank(address.getAddrTxt2()) ? " " + address.getAddrTxt2() : ""));
      requestInstallAt.setState(address.getStateProv());

      LOG.debug("Connecting to the SOS - RPA Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
      AutomationResponse<?> rawResponseInstallAt = autoClient.executeAndWrap(AutomationServiceClient.US_SOS_RPA_SERVICE_ID, requestInstallAt,
          AutomationResponse.class);

      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponseInstallAt);
      LOG.trace("SOS-RPA Service Response : " + json);
      TypeReference<AutomationResponse<SosResponse>> ref = new TypeReference<AutomationResponse<SosResponse>>() {
      };
      response = mapper.readValue(json, ref);
      if (response != null && response.isSuccess() && response.getRecord() != null) {
        closeMatch.add(response.getRecord());
      }
    }
    return closeMatch;
  }

  /**
   * Gets D&B matches for BP end users
   * 
   * @param handler
   * @param requestData
   * @param engineData
   * @return
   * @throws Exception
   */
  public static List<DnBMatchingResponse> getMatchesForBPEndUser(GEOHandler handler, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    List<DnBMatchingResponse> closeMatches = new ArrayList<DnBMatchingResponse>();
    MatchingResponse<DnBMatchingResponse> response = new MatchingResponse<DnBMatchingResponse>();
    Addr addr = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    GBGFinderRequest request = new GBGFinderRequest();
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    if (addr != null) {
      request.setCity(addr.getCity1());
      request.setCustomerName(addr.getDivn());
      request.setStreetLine1(addr.getAddrTxt());
      request.setStreetLine2(addr.getAddrTxt2());
      request.setLandedCountry(addr.getLandCntry());
      request.setPostalCode(addr.getPostCd());
      request.setStateProv(addr.getStateProv());
      request.setMinConfidence("8");
      MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
          MatchingServiceClient.class);
      client.setReadTimeout(1000 * 60 * 5);
      LOG.debug("Connecting to the Advanced D&B Matching Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
      MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.DNB_SERVICE_ID, request, MatchingResponse.class);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponse);

      TypeReference<MatchingResponse<DnBMatchingResponse>> ref = new TypeReference<MatchingResponse<DnBMatchingResponse>>() {
      };

      response = mapper.readValue(json, ref);

      if (response != null && response.getMatched()) {
        for (DnBMatchingResponse dnbRecord : response.getMatches()) {
          if (DnBUtil.closelyMatchesDnb(addr.getLandCntry(), addr, admin, dnbRecord, addr.getDivn(), false)) {
            closeMatches.add(dnbRecord);
          }
        }
      }
    }

    return closeMatches;
  }

  private void setDummyGBGMatchForInternal(AutomationEngineData engineData) {
    GBGResponse calcGbg = new GBGResponse();
    calcGbg.setBgId("DB22C85F");
    calcGbg.setBgName("INTERNATIONAL BUSINESS MACHINE CORPORATION");
    calcGbg.setCmrCount(1);
    calcGbg.setGbgId("GB000W0K");
    calcGbg.setGbgName("INTERNATIONAL BUSINESS MACHINE CORPORATION");
    calcGbg.setLdeRule("BG_CNTRY_US_Affno_4600000");
    engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
    engineData.put(AutomationEngineData.GBG_MATCH, calcGbg);
  }

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("E", "M");
  }

  private boolean isRelevantAddressFieldUpdated(RequestChangeContainer changes, Addr addr) {
    if (addr != null) {
      List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
      if (addrChanges == null) {
        return false;
      }
      for (UpdatedNameAddrModel change : addrChanges) {
        if (!NON_RELEVANT_ADDRESS_FIELDS.contains(change.getDataField())) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean relevantAddressFieldForUpdates(RequestChangeContainer changes, Addr addr) {
    if (addr != null) {
      List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
      if (addrChanges == null) {
        return false;
      }
      for (UpdatedNameAddrModel change : addrChanges) {
        if (RELEVANT_ADDRESS_FIELDS_RPA_UPDATES.contains(change.getDataField())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void performCoverageBasedOnGBG(CalculateCoverageElement covElement, EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData, String covFrom,
      CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    Data data = requestData.getData();
    String bgId = data.getBgId();
    String gbgId = data.getGbgId();
    String country = data.getCmrIssuingCntry();
    String sql = ExternalizedQuery.getSql("QUERY.GET_GBG_FROM_LOV");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CD", gbgId);
    query.setParameter("COUNTRY", country);
    query.setForReadOnly(true);
    String result = query.getSingleResult(String.class);
    LOG.debug("perform coverage based on GBG-------------");
    LOG.debug("result--------" + result);
    if (result != null || bgId.equals("DB502GQG")) {
      LOG.debug("Setting isu ctc to 5K based on gbg matching.");
      details.append("Setting isu ctc to 5K based on gbg matching.");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "5K");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "");
    }
    LOG.debug("isu" + data.getIsuCd());
    LOG.debug("client tier" + data.getClientTier());
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {
    String scenarioSubType = "";
    Data data = requestData.getData();
    scenarioSubType = data.getCustSubGrp();
    if (SC_REST_KYN.equals(scenarioSubType)) {
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "ISIC_CD", data.getIsicCd(), "7229");
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "US_SICMEN", data.getUsSicmen(), "7229");
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INAC_TYPE", data.getInacType(), "I");
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "INAC_CD", data.getInacCd(), "6272");
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "AFFILIATE", data.getAffiliate(), "6500871");
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "COMPANY", data.getCompany(), "12641341");
      overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "ENTERPRISE", data.getEnterprise(), "6500871");
    }
    return true;
  }

  public boolean addressExists(EntityManager entityManager, Addr addrToCheck) {

    String sql = ExternalizedQuery.getSql("AUTO.US.CHECK_IF_ADDRESS_EXIST_ZP01PAYGO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addrToCheck.getId().getReqId());
    query.setParameter("ADDR_SEQ", addrToCheck.getId().getAddrSeq());
    query.setParameter("LAND_CNTRY", addrToCheck.getLandCntry());
    query.setParameter("CITY", addrToCheck.getCity1());
    if (addrToCheck.getAddrTxt() != null) {
      query.append(" and lower(ADDR_TXT) like lower(:ADDR_TXT)");
      query.setParameter("ADDR_TXT", addrToCheck.getAddrTxt());
    }
    if (addrToCheck.getAddrTxt2() != null) {
      query.append(" and lower(DEPT) like lower(:DEPT)");
      query.setParameter("DEPT", "%" + addrToCheck.getAddrTxt2() + "%");
    }
    if (addrToCheck.getDept() != null) {
      query.append(" and lower(DEPT) like lower(:DEPT)");
      query.setParameter("DEPT", "%" + addrToCheck.getDept() + "%");
    }
    if (addrToCheck.getFloor() != null) {
      query.append(" and lower(FLOOR) like lower(:FLOOR)");
      query.setParameter("FLOOR", addrToCheck.getFloor());
    }
    if (addrToCheck.getBldg() != null) {
      query.append(" and BLDG= :BLDG");
      query.setParameter("BLDG", addrToCheck.getBldg());
    }
    if (addrToCheck.getStateProv() != null) {
      query.append(" and STATE_PROV = :STATE");
      query.setParameter("STATE", addrToCheck.getStateProv());
    }
    if (addrToCheck.getPostCd() != null) {
      query.append(" and POST_CD= :POST_CD");
      query.setParameter("POST_CD", addrToCheck.getPostCd());
    }
    if (addrToCheck.getCustPhone() != null) {
      query.append(" and CUST_PHONE = :PHONE");
      query.setParameter("PHONE", addrToCheck.getCustPhone());
    }
    if (addrToCheck.getCounty() != null) {
      query.append(" and COUNTY= :COUNTY");
      query.setParameter("COUNTY", addrToCheck.getCounty());
    }
    if (addrToCheck.getExtWalletId() != null) {
      query.append(" and EXT_WALLET_ID= :EXT_WALLET_ID");
      query.setParameter("EXT_WALLET_ID", addrToCheck.getExtWalletId());
    }

    return query.exists();
  }

  public static String validateForSCC(EntityManager entityManager, Long reqId) {

    String flag = "N";

    String landCntry = "";
    String stateProv = "";
    String county = "";
    String city1 = "";

    String sql1 = ExternalizedQuery.getSql("US.GET.ADDRESS_BY_REQ_ID");
    PreparedQuery query1 = new PreparedQuery(entityManager, sql1);
    query1.setParameter("REQ_ID", reqId);

    List<Object[]> results1 = query1.getResults();
    if (results1 != null && !results1.isEmpty()) {
      Object[] scc = results1.get(0);
      landCntry = (String) scc[0];
      stateProv = (String) scc[1];
      county = (String) scc[2];
      city1 = (String) scc[3];
    }

    if (!StringUtils.isNumeric(county)) {
      county = "0";
    }

    String sql2 = ExternalizedQuery.getSql("QUERY.US_CMR_SCC.GET_SCC_BY_LAND_CNTRY_ST_CNTY_CITY");
    PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
    query2.setParameter("LAND_CNTRY", landCntry);
    query2.setParameter("N_ST", stateProv);
    query2.setParameter("C_CNTY", county);
    query2.setParameter("N_CITY", city1);

    List<Object[]> results2 = query2.getResults();
    if (results2 != null && !results2.isEmpty()) {
      Object[] result = results2.get(0);
      if (!"".equals(result[0])) {
        flag = "Y";
      }
    }

    return flag;

  }

  public static String getcGem(EntityManager entityManager, String cmrNo) {
    String code = "";
    try {
      LOG.debug("Getting cGem...");
      String sql = ExternalizedQuery.getSql("AUTO.GET_CGEM");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("CMR_NO", cmrNo);
      query.setForReadOnly(true);
      code = query.getSingleResult(String.class);
      if (code == null) {
        code = "";
      }
    } catch (Exception e) {
      LOG.error("Error in Getting cGem... ");
    }

    return code;
  }

  public static String getEntType(EntityManager entityManager, String entNo) {
    String code = "";
    try {
      LOG.debug("Getting EntType...");
      String sql = ExternalizedQuery.getSql("AUTO.GET_ENTTYPE");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("ENT_NO", entNo);
      query.setForReadOnly(true);
      code = query.getSingleResult(String.class);
      if (code == null) {
        code = "";
      }
    } catch (Exception e) {
      LOG.error("Error in Getting EntType.. ");
    }

    return code;
  }

  public static USDetailsContainer determineUSCMRDetails(EntityManager entityManager, String cmrNo) throws Exception {
    // get request admin and data
    if (usDetailsMap.containsKey(cmrNo) && usDetailsMap.get(cmrNo) != null) {
      return usDetailsMap.get(cmrNo);
    }
    USDetailsContainer usDetails = new USDetailsContainer();
    String custTypCd = "NA";
    String entType = "";
    String leasingCo = "";
    String bpAccTyp = "";
    String cGem = "";
    String usRestrictTo = "";
    String companyNo = "";
    String pccArDept = "";
    String mtkgArDept = "";

    cGem = getcGem(entityManager, cmrNo);

    if ("P".equals(entType)) {
      custTypCd = POWER_OF_ATTORNEY;
    } else if ("F".equals(entType)) {
      custTypCd = FEDERAL;
    } else if ("I".equals(entType)) {
      custTypCd = INTERNAL;
    } else if ("C".equals(entType) || StringUtils.isNotBlank(leasingCo)) {
      custTypCd = LEASING;
    } else if (StringUtils.isNotBlank(bpAccTyp)) {
      custTypCd = BUSINESS_PARTNER;
    } else if ("2".equals(cGem)) {
      custTypCd = STATE_LOCAL;
    } else {
      custTypCd = COMMERCIAL;
    }

    // GET US_RESTRICT_TO_CODE
    String restrictCd = getUSRestrictToCode(entityManager, cmrNo);

    usDetails.setCustTypCd(custTypCd);
    usDetails.setEntType(entType);
    usDetails.setLeasingCo(leasingCo);
    usDetails.setBpAccTyp(bpAccTyp);
    usDetails.setcGem(cGem);
    // usDetails.setUsRestrictTo(usRestrictTo);
    usDetails.setUsRestrictTo(restrictCd);
    usDetails.setCompanyNo(companyNo);
    usDetails.setMktgArDept(mtkgArDept);

    usDetails.setPccArDept(pccArDept);
    usDetailsMap.put(cmrNo, usDetails);
    return usDetails;
  }

  @Override
  public void tweakDnBMatchingResponse(EntityManager entityManager, Data data, String field) {
    LOG.debug("tweakDnBMatchingResponse USUtil");
    String subScenario = data.getCustGrp();
    List<String> dnbIsicOverrideScenarios = Arrays.asList(CG_COMMERCIAL, CG_COMMERCIAL_FRANCHISE, CG_COMMERCIAL_RESTRICTED);
    if (!StringUtil.isBlank(subScenario) && dnbIsicOverrideScenarios.contains(subScenario)) {
      if (!usFedIsicMap.isEmpty()) {
        for (USFedIsicMapping mapping : usFedIsicMap) {
          String isicOverridedValue = mapping.getCmrIsic();
          String subIndOverrideValue = mapping.getCmrSubInd();
          if (data.getIsicCd().equals(mapping.getDnbIsic()) && !isicOverridedValue.isEmpty() && "ISIC_CD".equals(field)) {
            data.setIsicCd(isicOverridedValue);
          }
          if (data.getSubIndustryCd().equals(mapping.getDnbSubInd()) && data.getIsicCd().equals(mapping.getCmrIsic())
              && "SUB_INDUSTRY_CD".equals(field) && !subIndOverrideValue.isEmpty()) {
            data.setSubIndustryCd(subIndOverrideValue);
          }
        }
      }
    }
  }

}
