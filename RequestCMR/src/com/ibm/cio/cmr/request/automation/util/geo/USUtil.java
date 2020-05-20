package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.ui.ModelMap;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.automation.util.geo.us.USDetailsContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * 
 * @author Rangoli Saxena
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
  // IGS
  public static final String SC_IGSF = "IGSF";
  public static final String SC_IGS = "IGS";
  // State/Local Government
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
  public static final String SC_LEASE_32C = "32C";
  public static final String SC_LEASE_TPPS = "TPPS";
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
  public static final String SC_BP_DEVELOP = "DEVELOP";
  public static final String SC_BP_E_HOST = "E-HOST";
  // BYMODEL
  public static final String SC_BYMODEL = "BYMODEL";

  public static List<USBranchOffcMapping> svcARBOMappings = new ArrayList<USBranchOffcMapping>();
  public static List<USBranchOffcMapping> boMappings = new ArrayList<USBranchOffcMapping>();
  private static Map<String, USDetailsContainer> usDetailsMap = new HashMap<String, USDetailsContainer>();

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
        ClassLoader loader = USUtil.class.getClassLoader();
        // FileInputStream in = new
        // FileInputStream("C:\\Users\\RangoliSaxena\\git\\createcmr\\RequestCMR\\config\\us-branchoff-mapping.xml");
        InputStream is = loader.getResourceAsStream("us-branchoff-mapping.xml");
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
        ClassLoader loader = USUtil.class.getClassLoader();
        // FileInputStream in = new
        // FileInputStream("C:\\Users\\RangoliSaxena\\git\\createcmr\\RequestCMR\\config\\us-mktgsvc-mapping.xml");
        InputStream is = loader.getResourceAsStream("us-mktgsvc-mapping.xml");
        USUtil.svcARBOMappings = (ArrayList<USBranchOffcMapping>) digester.parse(is);
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
        scenarioSubType = determineCustSubScenario(entityManager, admin.getModelCmrNo(), engineData);
      }
      LOG.debug("US : Performing field computations for req_id : " + admin.getId().getReqId());
      // computation start
      if (engineData.hasPositiveCheckStatus(AutomationEngineData.BO_COMPUTATION)) {
        details.append("Branch Office codes computed by another element/external process.");
      } else {
        if (!boMappings.isEmpty() && StringUtils.isNotBlank(scenarioSubType) && !SC_INTERNAL.equals(scenarioSubType)) {
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
        } else if (INTERNAL.equals(scenarioSubType)) {
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

      if ("C".equals(admin.getReqType()) && StringUtils.isNotEmpty(data.getIsicCd()) && StringUtils.isNotEmpty(data.getUsSicmen())
          && !"357X".equals(data.getIsicCd()) && !data.getIsicCd().equals(data.getUsSicmen())) {
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "US_SICMEN", data.getUsSicmen(), data.getIsicCd());
      }
      // if scenario is OEMSW or OEMHW set isic to 357X
      if (SC_REST_OEMSW.equals(scenarioSubType) || SC_REST_OEMHW.equals(scenarioSubType) || SC_REST_TPD.equals(scenarioSubType)
          || SC_REST_SSD.equals(scenarioSubType) || SC_REST_DB4.equals(scenarioSubType)) {
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISIC_CD", data.getIsicCd(), "357X");
        // overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE,
        // "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), "ZC");
      }
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
    String[] scenarioList = { SC_SCHOOL_PUBLIC, SC_SCHOOL_CHARTER, SC_SCHOOL_PRIV, SC_SCHOOL_PAROCHL, SC_SCHOOL_COLLEGE, SC_STATE_STATE,
        SC_STATE_DIST, SC_STATE_COUNTY, SC_STATE_CITY, SC_LEASE_32C, SC_LEASE_TPPS, SC_LEASE_3CC, SC_LEASE_SVR_CONT, SC_BP_POOL, SC_BP_DEVELOP,
        SC_BP_E_HOST, SC_FED_REGULAR, SC_FED_CLINIC, SC_FED_FEDSTATE, SC_FED_HEALTHCARE, SC_FED_HOSPITAL, SC_FED_INDIAN_TRIBE, SC_FED_NATIVE_CORP,
        SC_FED_POA, SC_FED_TRIBAL_BUS, SC_BP_END_USER, SC_DUMMY, SC_IGS, SC_IGSF, SC_REST_SSI, SC_INTERNAL };
    String[] skipCompanyChecksScenarioList = { SC_BP_POOL, SC_BP_DEVELOP, SC_BP_E_HOST, SC_BP_END_USER, SC_LEASE_3CC, SC_LEASE_SVR_CONT, SC_INTERNAL,
        SC_DUMMY, SC_IGS, SC_IGSF, SC_REST_SSI, SC_STATE_DIST, SC_FED_REGULAR, SC_FED_CLINIC, SC_FED_FEDSTATE, SC_FED_HEALTHCARE, SC_FED_HOSPITAL,
        SC_FED_INDIAN_TRIBE, SC_FED_NATIVE_CORP, SC_FED_POA, SC_FED_TRIBAL_BUS, SC_STATE_COUNTY, SC_STATE_CITY, SC_STATE_STATE, SC_STATE_HOSPITALS,
        SC_SCHOOL_PUBLIC, SC_SCHOOL_CHARTER, SC_SCHOOL_PRIV, SC_SCHOOL_PAROCHL, SC_SCHOOL_COLLEGE, SC_LEASE_LPMA };
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
      if (SC_BYMODEL.equals(scenarioSubType)) {
        try {
          scenarioSubType = determineCustSubScenario(entityManager, admin.getModelCmrNo(), engineData);
        } catch (Exception e) {
          LOG.error("CMR Scenario for Create by model request could not be determined.", e);
        }
      }
    }

    if (StringUtils.isBlank(scenarioSubType) || Arrays.asList(scenarioList).contains(scenarioSubType)) {
      String scenarioDesc = getScenarioDesc(entityManager, scenarioSubType);
      if (SC_BYMODEL.equals(data.getCustSubGrp())) {
        engineData.addNegativeCheckStatus("US_SCENARIO_CHK", "Processor review required as imported CMR belongs to " + scenarioDesc + " scenario.");
        details.append("Processor review required as imported CMR belongs to " + scenarioDesc + " scenario.").append("\n");
        // skip Dnb check and matching
        if (engineData.hasPositiveCheckStatus("SKIP_COMP_CHECK") || Arrays.asList(skipCompanyChecksScenarioList).contains(scenarioSubType)) {
          ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
          if (scenarioExceptions != null) {
            scenarioExceptions.setSkipCompanyVerification(true);
          }
        }
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
      } else {
        engineData.addRejectionComment("OTH",
            "Federal CMR with restricted ISIC code is only allowed to be requested via FedCMR, please raise the request in FedCMR.", "", "");
        details.append("\nFederal CMR with restricted ISIC code is only allowed to be requested via FedCMR, please raise the request in FedCMR.")
            .append("\n");
        valid = false;
      }
    }
    return valid;
  }

  private String getScenarioDesc(EntityManager entityManager, String scenarioSubType) {
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
    if (query.exists() && "Y".equals(SystemParameters.getString("US.SKIP_UPDATE_CHECKS_FOR_CMDE"))) {
      // skip checks if requester is from USCMDE team
      LOG.debug("Requester is from US CMDE team, skipping update checks.");
      output.setDetails("Requester is from US CMDE team, skipping update checks.\n");
      validation.setMessage("Skipped");
      validation.setSuccess(true);
    } else {
      EntityManager cedpManager = JpaManager.getEntityManager("CEDP");
      boolean hasNegativeCheck = false;
      USDetailsContainer detailsCont = determineUSCMRDetails(entityManager, requestData.getData().getCmrNo());
      String custTypeCd = detailsCont.getCustTypCd();
      List<String> allowedCodesAddition = Arrays.asList("F", "G", "C", "D", "V", "W", "X");
      List<String> allowedCodesRemoval = Arrays.asList("F", "G", "C", "D", "A", "B", "H", "M", "N");
      Map<String, String> failedChecks = new HashMap<String, String>();
      boolean requesterFromTaxTeam = false;
      boolean enterpriseAffiliateUpdated = false;

      try {
        for (UpdatedDataModel updatedDataModel : changes.getDataUpdates()) {
          if (updatedDataModel != null) {
            LOG.debug("Checking updates for : " + new ObjectMapper().writeValueAsString(updatedDataModel));
            String field = updatedDataModel.getDataField();
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
            case "Marketing A/R Department":
              // set negative check status for FEDERAL Power of Attorney and BP
              if ((FEDERAL.equals(custTypeCd) || POWER_OF_ATTORNEY.equals(custTypeCd) || BUSINESS_PARTNER.equals(custTypeCd))) {
                failedChecks.put(field, field + " updated.");
                hasNegativeCheck = true;
              }
              break;
            case "Miscellaneous Bill Code":
              List<String> newCodes = Arrays.asList(updatedDataModel.getNewData().split(""));
              List<String> oldCodes = Arrays.asList(updatedDataModel.getOldData().split(""));

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
                  engineData.addRejectionComment("OTH", error, "", "");
                  LOG.debug(error);
                  output.setDetails(error);
                  output.setOnError(true);
                  validation.setMessage("Validation Failed");
                  validation.setSuccess(false);
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
                      engineData.addRejectionComment("OTH", error, "", "");
                      LOG.debug(error);
                      output.setDetails(error);
                      output.setOnError(true);
                      validation.setMessage("Validation Failed");
                      validation.setSuccess(false);
                      return true;
                    }
                  }
                }
                enterpriseAffiliateUpdated = true;
              }
              break;
            case "ISIC":
              String error = performISICCheck(cedpManager, entityManager, requestData);
              if (StringUtils.isNotBlank(error)) {
                engineData.addRejectionComment("OTH", error, "", "");
                LOG.debug(error);
                output.setDetails(error);
                output.setOnError(true);
                validation.setMessage("Validation Failed");
                validation.setSuccess(false);
                return true;
              }
              break;

            case "Abbreviated Name (TELX1)":
            case "PCC A/R Department":
            case "SVC A/R Office":
            case "Size Code":
            case "CAP Record":
            case "iERP Site Party ID":
              // SKIP THESE FIELDS
              break;
            default:
              // Set Negative check status for any other fields updated.
              failedChecks.put(field, field + " updated.");
              hasNegativeCheck = true;
              break;
            }
          }
        }

        if ("CSP".equals(admin.getReqReason()) && !changes.isDataChanged("ISIC")) {
          String error = performCSPCheck(cedpManager, entityManager, data, admin);
          if (StringUtils.isNotBlank(error)) {
            engineData.addRejectionComment("OTH", error, "", "");
            LOG.debug(error);
            output.setOnError(true);
            output.setDetails(error);
            validation.setMessage("Validation Failed");
            validation.setSuccess(false);
            return true;
          }
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

  /**
   * Checks to perform if ISIC updated
   * 
   * @param cedpManager
   * @param entityManager
   * @param requestData
   * @return a string with error message if some issues encountered during
   *         checks, null if validated.
   * @throws Exception
   */
  private String performISICCheck(EntityManager cedpManager, EntityManager entityManager, RequestData requestData) throws Exception {
    Data data = requestData.getData();
    String error = "The CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle.";
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
        return error;
      } else if (revenue.floatValue() == 0) {
        String dunsNo = "";
        if (StringUtils.isNotBlank(data.getDunsNo())) {
          dunsNo = data.getDunsNo();
        } else {
          MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, "ZS01");
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
            if (!dnbData.getIbmIsic().equals(data.getIsicCd())) {
              return error;
            } else {
              sql = ExternalizedQuery.getSql("AUTO.US.GET_ISU_BY_ISIC");
              query = new PreparedQuery(entityManager, sql);
              query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
              query.setParameter("ISIC", data.getIsicCd());
              query.setForReadOnly(true);
              String brsch = query.getSingleResult(String.class);
              if (!data.getIsuCd().equals(brsch)) {
                return error;
              }
            }
          } else {
            return error;
          }
        } else {
          return error;
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
    String error = "The CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle.";
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
        return error;
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
          return error;
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
        if (!"Ok".equals(creationCapChanged) || !"Ok".equals(sicValidation) || !"Ok".equals(revenue)) {
          return "'The CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle.";
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

    String sqlKey = ExternalizedQuery.getSql("AUTO.US.CHECK_CMDE");
    PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
    query.setParameter("EMAIL", admin.getRequesterId());
    query.setForReadOnly(true);
    if (query.exists() && "Y".equals(SystemParameters.getString("US.SKIP_UPDATE_CHECKS_FOR_CMDE"))) {
      // skip checks if requester is from USCMDE team
      validation.setSuccess(true);
    } else {
      StringBuilder details = new StringBuilder(output.getDetails());
      details.append("\n");
      USDetailsContainer detailsCont = determineUSCMRDetails(entityManager, requestData.getData().getCmrNo());
      String custTypCd = detailsCont.getCustTypCd();

      // check addresses
      if (StringUtils.isNotBlank(custTypCd) && !"NA".equals(custTypCd)) {
        if (BUSINESS_PARTNER.equals(custTypCd) || LEASING.equals(custTypCd)) {
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
          if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZS01.toString())) {
            closelyMatchAddressWithDnbRecords(requestData, engineData, "ZS01", details, validation);
          }

          if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZI01.toString())) {
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
            closelyMatchAddressWithDnbRecords(requestData, engineData, "ZP01", details, validation);
          }
        }

        // check if validated
        if (validation.isSuccess()) {
          output.setDetails("Updated ADDRESS elements were validated successfully.\n");
          validation.setMessage("Validated");
          validation.setSuccess(true);
        }

      } else {
        validation.setSuccess(false);
        validation.setMessage("Unknown CustType");
        details.append("Customer Type could not be determined. Update checks for address could not be run.").append("\n");
        output.setOnError(true);
      }
      output.setDetails(details.toString());
    }
    return true;
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
  private void closelyMatchAddressWithDnbRecords(RequestData requestData, AutomationEngineData engineData, String addrType, StringBuilder details,
      ValidationOutput validation) throws Exception {
    String addrDesc = "ZS01".equals(addrType) ? "Install-at" : "Invoice-at";
    Addr addr = requestData.getAddress(addrType);
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, addrType);
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
            engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_" + addrType,
                "High confidence D&B matches did not match the " + addrDesc + " address data. ");
            details.append("High confidence D&B matches did not match the " + addrDesc + " address data.").append("\n");
            validation.setMessage("Review needed");
            validation.setSuccess(false);
          }
        } else {
          engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_" + addrType, "No High Quality D&B Matches were found for " + addrDesc + " address.");
          details.append("No High Quality D&B Matches were found for " + addrDesc + " address.").append("\n");
          validation.setMessage("Review needed");
          validation.setSuccess(false);
        }
      } else {
        engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_" + addrType, "No D&B Matches were found for " + addrDesc + " address.");
        details.append("No D&B Matches were found for " + addrDesc + " address.").append("\n");
        validation.setMessage("Review needed");
        validation.setSuccess(false);
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
  public static String determineCustSubScenario(EntityManager entityManager, String cmrNo, AutomationEngineData engineData) throws Exception {
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
    String name3 = "";
    String name4 = "";

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
    if (StringUtils.isNotBlank(usRestricTo)) {
      sql = ExternalizedQuery.getSql("AUTO.US.GET_US_RESTR_TO_LOV");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("RESTRICT_TO", usRestricTo);
      usRestrictToLOV = query.getSingleResult(String.class);
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
      if (("IRCSO".equals(usRestrictToLOV) || "BPQS".equals(usRestrictToLOV)) && "E".equals(bpAccTyp)) {
        custSubGroup = SC_BP_END_USER;
      } else if ("P".equals(bpAccTyp)) {
        custSubGroup = SC_BP_POOL;
      } else if ("D".equals(bpAccTyp)) {
        custSubGroup = SC_BP_DEVELOP;
      } else if ("E".equals(bpAccTyp)) {
        String[] addrTypList = { "ZS01", "ZI01" };
        for (String addrTyp : Arrays.asList(addrTypList)) {
          // get RDC values
          sql = ExternalizedQuery.getSql("AUTO.US.GET_ATTN_RDC");
          query = new PreparedQuery(entityManager, sql);
          query.setParameter("CMR_NO", cmrNo);
          query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          query.setParameter("ADDR_TYP", addrTyp);
          query.setForReadOnly(true);
          List<Object[]> resultList = query.getResults(1);
          if (resultList != null && resultList.size() > 0) {
            name3 = (String) resultList.get(0)[0];
            name4 = (String) resultList.get(0)[1];
          }

          if ((StringUtils.isNotBlank(name3)
              && (name3.toUpperCase().contains("e-hosting".toUpperCase()) || name3.toUpperCase().contains("e-host".toUpperCase())
                  || name3.toUpperCase().contains("ehosting".toUpperCase()) || name3.toUpperCase().contains("ehost".toUpperCase())))
              || (StringUtils.isNotBlank(name4)
                  && (name4.toUpperCase().contains("e-hosting".toUpperCase()) || name4.toUpperCase().contains("e-host".toUpperCase())
                      || name4.toUpperCase().contains("ehosting".toUpperCase()) || name4.toUpperCase().contains("ehost".toUpperCase())))) {
            custSubGroup = SC_BP_E_HOST;
            break;
          }
        }
      }
    }
    return custSubGroup;
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

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
    String sql = ExternalizedQuery.getSql("AUTO.GET_CODES_USCMR", usSchema);
    sql = StringUtils.replace(sql, ":CMR_NO", "'" + cmrNo + "'");
    String dbId = QueryClient.USCMR_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.setRows(1);
    query.addField("I_ENT_TYPE");
    query.addField("I_BP_ACCOUNT_TYPE");
    query.addField("C_LEASING_CO");
    query.addField("C_GEM");
    query.addField("C_COM_RESTRCT_CODE");
    query.addField("I_CO");
    query.addField("I_CUST_OFF_5");
    query.addField("I_CUST_OFF_3");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (!response.isSuccess()) {
      custTypCd = "NA";

    } else if (response.getRecords() == null || response.getRecords().size() == 0) {
      custTypCd = "NA";
    } else {
      Map<String, Object> record = response.getRecords().get(0);
      entType = (String) record.get("I_ENT_TYPE");
      leasingCo = (String) record.get("C_LEASING_CO");
      bpAccTyp = (String) record.get("I_BP_ACCOUNT_TYPE");
      cGem = (String) record.get("C_GEM");
      usRestrictTo = (String) record.get("C_COM_RESTRCT_CODE");
      companyNo = String.valueOf(record.get("I_CO"));
      pccArDept = (String) record.get("I_CUST_OFF_5");
      mtkgArDept = (String) record.get("I_CUST_OFF_3");
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
    }
    usDetails.setCustTypCd(custTypCd);
    usDetails.setEntType(entType);
    usDetails.setLeasingCo(leasingCo);
    usDetails.setBpAccTyp(bpAccTyp);
    usDetails.setcGem(cGem);
    usDetails.setUsRestrictTo(usRestrictTo);
    usDetails.setCompanyNo(companyNo);
    usDetails.setMktgArDept(mtkgArDept);

    usDetails.setPccArDept(pccArDept);
    usDetailsMap.put(cmrNo, usDetails);
    return usDetails;
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
          if (DnBUtil.closelyMatchesDnb(addr.getLandCntry(), addr, admin, dnbRecord, addr.getDivn())) {
            closeMatches.add(dnbRecord);
          }
        }
      }
    }

    return closeMatches;
  }

}
