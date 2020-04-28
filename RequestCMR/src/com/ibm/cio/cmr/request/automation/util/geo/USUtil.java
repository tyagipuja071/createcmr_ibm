package com.ibm.cio.cmr.request.automation.util.geo;

import java.io.InputStream;
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
import com.ibm.cio.cmr.request.util.JpaManager;
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
 *
 */

public class USUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(USUtil.class);
  public static final String COMMERCIAL = "1";
  public static final String STATE_LOCAL = "2";
  public static final String LEASING = "3";
  public static final String FEDERAL = "4";
  public static final String INTERNAL = "5";
  public static final String POWER_OF_ATTORNEY = "6";
  public static final String BUSINESS_PARTNER = "7";

  public static String[] INDUSTRY_OCA = { "A", "U", "K", "B", "C" };
  public static String[] INDUSTRY_WYK = { "J", "V", "L", "P", "M" };
  public static String[] INDUSTRY_YUC = { "Y", "G", "E", "H", "X" };
  public static String[] INDUSTRY_1QP = { "W" };
  public static String[] INDUSTRY_WYL = { "R", "D", "W", "T" };
  public static String[] INDUSTRY_WYR = { "F", "S", "N" };
  public static Map<String, List<String>> indARBOMap = new HashMap<String, List<String>>();
  public static List<USBranchOffcMapping> svcARBOMappings = new ArrayList<USBranchOffcMapping>();
  public static List<USBranchOffcMapping> boMappings = new ArrayList<USBranchOffcMapping>();
  public static Map<String, List<String>> stateMktgDepMap = new HashMap<String, List<String>>();

  private Map<String, USDetailsContainer> usDetailsMap = new HashMap<String, USDetailsContainer>();

  public static String[] COMP_NO_LIST = { "12526663", "12464170", "12539858", "12489905", "12469039", "12329586", "12393039", "12118585", "11206715",
      "12148960", "12501750", "12528315", "12126752", "12232055", "12543418", "12532055", "12550434", "12518669", "12411167", "12236746", "11873808",
      "12489906", "12370209", "12404472", "11833282", "12262214", "12390240", "12323063", "12234259", "12489908", "12558232", "12368779", "12535192",
      "12579643", "10782401", "12436301", "11363167" };

  public static String[] STATE_K9Y = { "CT", "DE", "ME", "MA", "NH", "NJ", "NY", "RI", "VT" };
  public static String[] STATE_M3B = { "IL", "IA", "MN", "NE", "ND", "OH", "SD", "WV", "WI" };
  public static String[] STATE_S6H = { "MD", "DC", "VA" };
  public static String[] STATE_K9W = { "AL", "FL", "GA", "MS", "NC", "SC", "TN" };
  public static String[] STATE_M3A = { "AR", "CO", "KS", "LA", "NM", "OK", "TX", "WY", "PR", "VI" };
  public static String[] STATE_S6G = { "AZ", "CA", "ID", "MT", "NV", "OR", "UT", "WA", "AK", "HI", "GU", "AS", "FM", "MH", "MP", "PW", "AP", "AE",
      "AA" };

  @SuppressWarnings("unchecked")
  public USUtil() {
    LOG.debug("Initializing US Util");
    // create map to store mktg AR Dept and industry mapping
    LOG.debug("US - creating map to store mktg AR Dept and industry mapping");
    indARBOMap.put("OCA", Arrays.asList(INDUSTRY_OCA));
    indARBOMap.put("WYK", Arrays.asList(INDUSTRY_WYK));
    indARBOMap.put("YUC", Arrays.asList(INDUSTRY_YUC));
    indARBOMap.put("1QP", Arrays.asList(INDUSTRY_1QP));
    indARBOMap.put("WYL", Arrays.asList(INDUSTRY_WYL));
    indARBOMap.put("WYR", Arrays.asList(INDUSTRY_WYR));

    // create map to store mktg Dept and state mapping
    LOG.debug("US - creating map to store mktg Dept and state mapping");
    stateMktgDepMap.put("K9Y", Arrays.asList(STATE_K9Y));
    stateMktgDepMap.put("M3B", Arrays.asList(STATE_M3B));
    stateMktgDepMap.put("S6H", Arrays.asList(STATE_S6H));
    stateMktgDepMap.put("K9W", Arrays.asList(STATE_K9W));
    stateMktgDepMap.put("M3A", Arrays.asList(STATE_M3A));
    stateMktgDepMap.put("S6G", Arrays.asList(STATE_S6G));

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
    long reqId = requestData.getAdmin().getId().getReqId();
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    StringBuilder eleResults = new StringBuilder();
    ArrayList<String> scenarioList = new ArrayList<String>();
    String scenarioSubType = "";

    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = data.getCustSubGrp();
    }
    LOG.debug("US : Performing field computations for req_id : " + reqId);
    // computation start
    if (!boMappings.isEmpty()) {
      for (USBranchOffcMapping mapping : boMappings) {
        scenarioList.add((StringUtils.isBlank(mapping.getScenario()) ? "" : mapping.getScenario()));
      }
      if (StringUtils.isNotBlank(scenarioSubType) && scenarioList != null && scenarioList.contains(scenarioSubType)) {
        for (USBranchOffcMapping mapping : boMappings) {

          String csoSite = "";
          String mktgDept = "";
          String mtkgArDept = "";
          String svcArOffice = "";
          String pccArDept = "";

          csoSite = ("request".equalsIgnoreCase(mapping.getCsoSite())) ? ((StringUtils.isBlank(data.getCsoSite()) ? "" : data.getCsoSite()))
              : ((StringUtils.isBlank(mapping.getCsoSite()) ? "" : mapping.getCsoSite()));

          if (!"logic".equalsIgnoreCase(mapping.getMktgDept())) {
            mktgDept = ("request".equalsIgnoreCase(mapping.getMktgDept())) ? ((StringUtils.isBlank(data.getMktgDept()) ? "" : data.getMktgDept()))
                : ((StringUtils.isBlank(mapping.getMktgDept()) ? "" : mapping.getMktgDept()));
          } else {
            mktgDept = getMktgDept(entityManager, requestData, engineData);
          }

          if (!"logic".equalsIgnoreCase(mapping.getMtkgArDept())) {
            mtkgArDept = ("request".equalsIgnoreCase(mapping.getMtkgArDept()))
                ? ((StringUtils.isBlank(data.getMtkgArDept()) ? "" : data.getMtkgArDept()))
                : ((StringUtils.isBlank(mapping.getMtkgArDept()) ? "" : mapping.getMtkgArDept()));
          } else {
            mtkgArDept = getMtkgArDept(entityManager, requestData, engineData);
          }

          if (!"logic".equalsIgnoreCase(mapping.getSvcArOffice())) {
            svcArOffice = ("request".equalsIgnoreCase(mapping.getSvcArOffice()))
                ? ((StringUtils.isBlank(data.getSvcArOffice()) ? "" : data.getSvcArOffice()))
                : ((StringUtils.isBlank(mapping.getSvcArOffice()) ? "" : mapping.getSvcArOffice()));
          } else {
            svcArOffice = getSvcArOffice(entityManager, requestData, engineData, mtkgArDept);
          }

          if (!"logic".equalsIgnoreCase(mapping.getPccArDept())) {
            pccArDept = ("request".equalsIgnoreCase(mapping.getPccArDept())) ? ((StringUtils.isBlank(data.getPccArDept()) ? "" : data.getPccArDept()))
                : ((StringUtils.isBlank(mapping.getPccArDept()) ? "" : mapping.getPccArDept()));
          } else {
            pccArDept = getPccArDept(entityManager, requestData, engineData);
          }

          details.append("\nSetting Fields based on US Scenarios:");
          details.append("\nCSO Site = " + csoSite);
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CSO_SITE", data.getCsoSite(), csoSite);

          details.append("\nMarketing Department = " + mktgDept);
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "MKTG_DEPT", data.getMktgDept(), mktgDept);

          details.append("\nMarketing A/R Department = " + mtkgArDept);
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), mtkgArDept);

          details.append("\nSVC A/R Office = " + svcArOffice);
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SVC_AR_OFFICE", data.getSvcArOffice(), svcArOffice);

          details.append("\nPCC A/R Department = " + pccArDept);
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "PCC_AR_DEPT", data.getPccArDept(), pccArDept);
        }
      } else {
        engineData.addNegativeCheckStatus("verifyBranchOffc", "Branch Office Codes need to be verified.");
      }
    }

    if (results != null && !results.isOnError()) {

    } else {
      eleResults.append("Error On Field Calculation.");
    }
    // computation end
    results.setResults(eleResults.toString());
    results.setDetails(details.toString());
    results.setProcessOutput(overrides);

    return results;
  }

  private String getPccArDept(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws Exception {
    String pccArDept = "";
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = data.getCustSubGrp();
    }
    if ("BYMODEL".equals(scenarioSubType)) {
      if ("5AA".equalsIgnoreCase(data.getPccArDept())) {
        pccArDept = "G8M";
      } else {
        USDetailsContainer usDetails = USUtil.determineUSCMRDetails(entityManager, requestData.getAdmin().getModelCmrNo(), engineData);
        pccArDept = usDetails.getPccArDept();
      }
    }
    return pccArDept;
  }

  private String getMktgDept(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws Exception {
    String mktgDept = "";
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr installAt = requestData.getAddress("ZI01");
    String stateToMatch = StringUtils.isBlank(installAt.getStateProv()) ? "" : installAt.getStateProv();
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = data.getCustSubGrp();
    }
    if ("FEDERAL".equals(scenarioSubType) || "CAMOUFLAGED".equals(scenarioSubType)) {
      for (Map.Entry<String, List<String>> entry : stateMktgDepMap.entrySet()) {
        List<String> stateList = entry.getValue();
        if (stateList != null && stateList.size() != 0 && stateList.contains(stateToMatch)) {
          mktgDept = entry.getKey();
          break;
        }
      }
    } else if ("POA".equals(scenarioSubType)) {
      for (Map.Entry<String, List<String>> entry : stateMktgDepMap.entrySet()) {
        List<String> stateList = entry.getValue();
        if (stateList != null && stateList.size() != 0 && stateList.contains(stateToMatch)) {
          mktgDept = entry.getKey();
          break;
        }
      }
    }
    return mktgDept;
  }

  private String getMtkgArDept(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws Exception {
    String mtkgArDept = "";
    // get request admin and data
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = data.getCustSubGrp();
    }
    if ("POA".equals(scenarioSubType)) {
      if (data != null && StringUtils.isNotBlank(data.getCompany()) && Arrays.asList(COMP_NO_LIST).contains(data.getCompany())) {
        mtkgArDept = "14W";
      } else {
        mtkgArDept = "28W";
      }
    } else {
      if (data != null && StringUtils.isNotBlank(data.getEnterprise())) {

        String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
        String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
        String sql = ExternalizedQuery.getSql("AUTO.GET_MKTG_AR_DEPT_USCMR", usSchema);
        String dbId = QueryClient.USCMR_APP_ID;

        QueryRequest query = new QueryRequest();
        query.setSql(sql);
        query.setRows(1);
        query.addField("I_CUST_OFF_3");

        QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
        QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);
        if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
          Map<String, Object> record = response.getRecords().get(0);
          mtkgArDept = (String) record.get("I_CUST_OFF_3");
        }
      } else if ("".equals(mtkgArDept)) {
        String indToMatch = StringUtils.isBlank(data.getSubIndustryCd()) ? "" : data.getSubIndustryCd().substring(0, 1);
        // iterate and display values
        for (Map.Entry<String, List<String>> entry : indARBOMap.entrySet()) {
          List<String> indstryList = entry.getValue();
          if (indstryList != null && indstryList.size() != 0 && indstryList.contains(indToMatch)) {
            mtkgArDept = entry.getKey();
            break;
          }
        }
      }
    }

    return mtkgArDept;
  }

  private String getSvcArOffice(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData, String mtkgArDept)
      throws Exception {
    String svcArOffice = "";
    if (!USUtil.svcARBOMappings.isEmpty()) {
      for (USBranchOffcMapping mapping : USUtil.svcARBOMappings) {
        if (mtkgArDept.equalsIgnoreCase(mapping.getMktgDepartmentAR())) {
          svcArOffice = StringUtils.isNotBlank(mapping.getSvcOfficeAR()) ? mapping.getSvcOfficeAR() : "";
          break;
        }
      }
    }
    return svcArOffice;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    boolean valid = true;
    String[] scnarioList = { "HOSPITALS", "SCHOOL PUBLIC", "SCHOOL CHARTER", "SCHOOL PRIV", "SCHOOL PAROCHL", "SCHOOL COLLEGE", "STATE", "SPEC DIST",
        "COUNTY", "CITY", "32C", "TPPS", "3CC", "SVR CONT", "POOL", "DEVELOP", "E-HOST", "FEDSTATE", "FEDERAL", "POA" };
    String scenarioSubType = "";
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = StringUtils.isBlank(data.getCustSubGrp()) ? "" : data.getCustSubGrp();
    }

    if (Arrays.asList(scnarioList).contains(scenarioSubType)) {
      engineData.addNegativeCheckStatus("US_SCENARIO_CHK", "Automated checks cannot be performed for this scenario.");
      valid = true;
    } else if ("CAMOUFLAGED".equals(scenarioSubType)) {
      String sql = ExternalizedQuery.getSql("AUTO.CHK_CMDE_USER");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQUESTER_ID", admin.getRequesterId());
      String procCntr = query.getSingleResult(String.class);
      if (StringUtils.isNotBlank(procCntr) && "Kuala Lumpur".equalsIgnoreCase(procCntr)) {
        valid = true;
      } else {
        engineData.addRejectionComment(
            "Federal CMR with restricted ISIC code is only allowed to be requested via FedCMR, please raise the request in FedCMR.");
        details.append("\nFederal CMR with restricted ISIC code is only allowed to be requested via FedCMR, please raise the request in FedCMR.")
            .append("\n");
        valid = false;
      }
    }
    return valid;
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
    if (query.exists()) {
      // skip checks if requester is from USCMDE team
      LOG.debug("Requester is from US CMDE team, skipping update checks.");
      output.setDetails("Requester is from US CMDE team, skipping update checks.\n");
      validation.setMessage("Skipped");
      validation.setSuccess(true);
    } else {
      EntityManager cedpManager = JpaManager.getEntityManager("CEDP");
      boolean hasNegativeCheck = false;
      USDetailsContainer detailsCont = determineUSCMRDetails(entityManager, requestData.getData().getCmrNo(), engineData);
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
                // TODO check if requester is from TaxTeam
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
                String error = performCSPCheck(cedpManager, entityManager, data);
                if (StringUtils.isNotBlank(error)) {
                  engineData.addRejectionComment(error);
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
            case "Enterprise":
            case "Affiliate":
              if (!enterpriseAffiliateUpdated) {
                if (BUSINESS_PARTNER.equals(custTypeCd)) {
                  engineData.addNegativeCheckStatus("ENT_AFF_BUSPR", "Enterprise/Affiliate change on a Business Partner record needs validation.");
                  failedChecks.put("ENT_AFF_BUSPR", "Enterprise/Affiliate change on a Business Partner record needs validation.");
                } else {
                  String error = performEnterpriseAffiliateCheck(cedpManager, entityManager, requestData);
                  if (StringUtils.isNotBlank(error)) {
                    engineData.addRejectionComment(error);
                    LOG.debug(error);
                    output.setDetails(error);
                    output.setOnError(true);
                    validation.setMessage("Validation Failed");
                    validation.setSuccess(false);
                    return true;
                  }
                }
                enterpriseAffiliateUpdated = true;
              }
              break;
            case "ISIC":
              String error = performISICCheck(cedpManager, entityManager, requestData);
              if (StringUtils.isNotBlank(error)) {
                engineData.addRejectionComment(error);
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
              // SKIP THESE FIELDS
              break;
            default:
              // Set Negative check status for any other fields updated.
              hasNegativeCheck = true;
              break;
            }
          }
        }

        if ("CSP".equals(admin.getReqReason())) {
          String error = performCSPCheck(cedpManager, entityManager, data);
          if (StringUtils.isNotBlank(error)) {
            engineData.addRejectionComment(error);
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
          details.append("\nPlease check Request Summary for more details.");
        }
        validation.setMessage("Review needed");
        validation.setSuccess(false);
      }
    }
    return true;
  }

  private String performISICCheck(EntityManager cedpManager, EntityManager entityManager, RequestData requestData) throws Exception {
    Data data = requestData.getData();
    String error = "The CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle.";
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_CMR_REVENUE");
    PreparedQuery query = new PreparedQuery(cedpManager, sql);
    query.setParameter("CMR_NO", data.getCmrNo());
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      float revenue = (float) (results.get(0)[1] != null ? results.get(0)[1] : 0);
      if (revenue > 100000) {
        return error;
      } else if (revenue == 0) {
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
              // TODO check BRSCH from zzkv_sic
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

  private String performEnterpriseAffiliateCheck(EntityManager cedpManager, EntityManager entityManager, RequestData requestData) throws Exception {
    Data data = requestData.getData();
    String error = "The CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle.";
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_CMR_REVENUE");
    PreparedQuery query = new PreparedQuery(cedpManager, sql);
    query.setParameter("CMR_NO", data.getCmrNo());
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      float revenue = (float) (results.get(0)[1] != null ? results.get(0)[1] : 0);
      if (revenue > 0) {
        return error;
      } else if (revenue == 0) {
        sql = ExternalizedQuery.getSql("AUTO.US.AFF_ENT_DUNS_CHECK");
        query = new PreparedQuery(cedpManager, sql);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        query.setParameter("CMR_NO", data.getCmrNo());
        query.setParameter("ENTERPRISE", data.getEnterprise());
        query.setParameter("AFFILIATE", data.getAffiliate());
        query.setForReadOnly(true);
        if (!query.exists()) {
          return error;
        } else {
          CmrClientService odmService = new CmrClientService();
          RequestEntryModel model = requestData.createModelFromRequest();
          Addr soldTo = requestData.getAddress("ZS01");
          ModelMap response = new ModelMap();

          boolean success = odmService.getBuyingGroup(entityManager, soldTo, model, response);
          String gbgId = (String) response.get("globalBuyingGroupID");
          if (!success || gbgId == null || (success && gbgId != null && !gbgId.equals(data.getGbgId()))) {
            return error;
          }
        }
      }
    }
    return null;
  }

  private String performCSPCheck(EntityManager cedpManager, EntityManager entityManager, Data data) {
    boolean requesterFromCSPTeam = true;
    // TODO check team heirarchy
    if (!requesterFromCSPTeam) {
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
            // TODO implement overrides
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
    if (query.exists()) {
      // skip checks if requester is from USCMDE team
      validation.setSuccess(true);
    } else {
      StringBuilder details = new StringBuilder();
      USDetailsContainer detailsCont = determineUSCMRDetails(entityManager, requestData.getData().getCmrNo(), engineData);
      String custTypCd = detailsCont.getCustTypCd();

      // check addresses
      if (StringUtils.isNotBlank(custTypCd) && !"NA".equals(custTypCd)) {
        if (LEASING.equals(custTypCd) || BUSINESS_PARTNER.equals(custTypCd)) {
          engineData.addNegativeCheckStatus("UPD_REVIEW_NEEDED",
              "Address updates for " + (custTypCd.equals(LEASING) ? "Leasing" : "Business Partner") + " scenario found.");
          details.append("Address updates for " + (custTypCd.equals(LEASING) ? "Leasing" : "Business Partner")
              + " scenario found. Processor review will be required.").append("\n");
          validation.setMessage("Review needed");
          validation.setSuccess(false);
        } else {
          List<String> addrTypesChanged = new ArrayList<String>();
          for (UpdatedNameAddrModel addrModel : changes.getAddressUpdates()) {
            addrTypesChanged.add(addrModel.getAddrType());
          }
          if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZS01)) {
            closelyMatchAddressWithDnbRecords(requestData, engineData, "ZS01", details, validation);
          }

          if (addrTypesChanged.contains(CmrConstants.ADDR_TYPE.ZP01)) {
            Addr zp01 = requestData.getAddress("ZP01");
            boolean immutableAddrFound = false;
            List<String> immutableAddrList = Arrays.asList("150 KETTLETOWN RD", "6303 BARFIELD RD", "PO BOX 12195 BLDG 061", "1 N CASTLE DR",
                "7100 HIGHLANDS PKWY", "294 ROUTE 100", "6710 ROCKLEDGE DR");
            String addrTxt = zp01.getAddrTxt() + (StringUtils.isNotBlank(zp01.getAddrTxt2()) ? " " + zp01.getAddrTxt2() : "");
            for (String streetAddr : immutableAddrList) {
              if (addrTxt.contains(streetAddr)) {
                immutableAddrFound = true;
                break;
              }
            }
            if (immutableAddrFound) {
              engineData.addNegativeCheckStatus("IMMUTABLE_ADDR_FOUND", "Invoice-to address cannot be modified.");
              details.append("Invoice-to address cannot be modified.").append("\n");
              validation.setMessage("Review needed");
              validation.setSuccess(false);
            } else {
              closelyMatchAddressWithDnbRecords(requestData, engineData, "ZP01", details, validation);
            }
          }
        }
      } else

      {
        validation.setSuccess(false);
        validation.setMessage("Unknown CustType");
        details.append("Customer Type could not be determined. Update checks for address could not be run.").append("\n");
        output.setOnError(true);
      }
      output.setDetails(details.toString());
    }
    return true;
  }

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

  public static USDetailsContainer determineUSCMRDetails(EntityManager entityManager, String cmrNo, AutomationEngineData engineData)
      throws Exception {
    // get request admin and data
    USDetailsContainer usDetails = new USDetailsContainer();
    String custTypCd = "NA";
    String entType = "";
    String leasingCo = "";
    String bpAccTyp = "";
    String cGem = "";
    String usRestrictTo = "";
    String companyNo = "";
    String pccArDept = "";

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

    usDetails.setPccArDept(pccArDept);
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
