package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.util.geo.us.USBPHandler;
import com.ibm.cio.cmr.request.automation.util.geo.us.USDetailsContainer;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.BluePagesHelper;

/**
 * 
 * @author RoopakChugh
 *
 */
public class USBranchOffcMapping {
  private static final Logger LOG = Logger.getLogger(USBranchOffcMapping.class);

  private static final String LOGIC = "logic";
  private static final String REQUEST = "request";
  public static String[] INDUSTRY_OCA = { "A", "U", "K", "B", "C" };
  public static String[] INDUSTRY_WYK = { "J", "V", "L", "P", "M" };
  public static String[] INDUSTRY_YUC = { "Y", "G", "E", "H", "X" };
  public static String[] INDUSTRY_1QP = { "W" };
  public static String[] INDUSTRY_WYL = { "R", "D", "W", "T" };
  public static String[] INDUSTRY_WYR = { "F", "S", "N" };
  public static String[] INDUSTRY_1SD = { "Z" };
  public static Map<String, List<String>> indARBOMap = new HashMap<String, List<String>>();
  public static Map<String, List<String>> stateMktgDepMap = new HashMap<String, List<String>>();

  public static List<String> COMP_NO_LIST = Arrays.asList("12526663", "12464170", "12539858", "12489905", "12469039", "12329586", "12393039",
      "12118585", "11206715", "12148960", "12501750", "12528315", "12126752", "12232055", "12543418", "12532055", "12550434", "12518669", "12411167",
      "12236746", "11873808", "12489906", "12370209", "12404472", "11833282", "12262214", "12390240", "12323063", "12234259", "12489908", "12558232",
      "12368779", "12535192", "12579643", "10782401", "12436301", "11363167");

  public static String[] STATE_K9Y = { "CT", "DE", "ME", "MA", "NH", "NJ", "NY", "RI", "VT" };
  public static String[] STATE_M3B = { "IL", "IA", "IN", "KY", "MI", "MN", "NE", "ND", "OH", "SD", "WV", "WI" };
  public static String[] STATE_S6H = { "MD", "DC", "VA" };
  public static String[] STATE_K9W = { "AL", "FL", "GA", "MS", "NC", "SC", "TN" };
  public static String[] STATE_M3A = { "AR", "CO", "KS", "LA", "NM", "OK", "TX", "WY", "PR", "VI" };
  public static String[] STATE_S6G = { "AZ", "CA", "CZ", "ID", "MT", "NV", "OR", "UT", "WA", "AK", "HI", "GU", "AS", "FM", "MH", "MP", "PW", "AP",
      "AE", "AA" };

  public static List<String> STATE_MO_M3A = Arrays.asList("001", "003", "005", "007", "009", "011", "013", "015", "019", "021", "025", "027", "029",
      "033", "037", "039", "041", "043", "047", "049", "051", "053", "055", "057", "059", "061", "063", "065", "067", "073", "075", "077", "079",
      "081", "083", "085", "087", "089", "091", "095", "097", "101", "103", "107", "109", "115", "117", "119", "121", "125", "129", "131", "135",
      "137", "139", "141", "145", "147", "149", "151", "153", "157", "159", "161", "163", "165", "167", "169", "171", "173", "175", "177", "179",
      "181", "183", "185", "186", "187", "189", "195", "197", "199", "201", "203", "205", "207", "209", "211", "213", "215", "217", "219", "221",
      "223", "225", "227", "229", "510");

  public static List<String> STATE_MO_M3B = Arrays.asList("017", "023", "031", "035", "045", "069", "071", "093", "099", "105", "111", "113", "123",
      "127", "133", "143", "155");

  public static List<String> STATE_PA_K9Y = Arrays.asList("001", "011", "015", "017", "023", "025", "027", "029", "035", "037", "041", "043", "045",
      "047", "053", "055", "057", "061", "067", "069", "071", "075", "077", "079", "081", "083", "087", "089", "091", "093", "095", "097", "099",
      "101", "103", "105", "107", "109", "113", "115", "117", "119", "123", "127", "131", "133");

  public static List<String> STATE_PA_M3B = Arrays.asList("003", "005", "007", "009", "013", "019", "021", "031", "033", "039", "049", "051", "059",
      "063", "065", "073", "085", "111", "121", "125", "129");

  static {
    // create map to store mktg AR Dept and industry mapping
    LOG.debug("US - creating map to store mktg AR Dept and industry mapping");
    indARBOMap.put("OCA", Arrays.asList(INDUSTRY_OCA));
    indARBOMap.put("WYK", Arrays.asList(INDUSTRY_WYK));
    indARBOMap.put("YUC", Arrays.asList(INDUSTRY_YUC));
    indARBOMap.put("1QP", Arrays.asList(INDUSTRY_1QP));
    indARBOMap.put("WYL", Arrays.asList(INDUSTRY_WYL));
    indARBOMap.put("WYR", Arrays.asList(INDUSTRY_WYR));
    indARBOMap.put("1SD", Arrays.asList(INDUSTRY_1SD));

    // create map to store mktg Dept and state mapping
    LOG.debug("US - creating map to store mktg Dept and state mapping");
    stateMktgDepMap.put("K9Y", Arrays.asList(STATE_K9Y));
    stateMktgDepMap.put("M3B", Arrays.asList(STATE_M3B));
    stateMktgDepMap.put("S6H", Arrays.asList(STATE_S6H));
    stateMktgDepMap.put("K9W", Arrays.asList(STATE_K9W));
    stateMktgDepMap.put("M3A", Arrays.asList(STATE_M3A));
    stateMktgDepMap.put("S6G", Arrays.asList(STATE_S6G));
  }

  private static final String PAYGO_MKT_ARBO = "1SD";
  private static final String PAYGO_SVC_ARBO = "IKE";

  private String scenario;
  private String csoSite;
  private String mktgDept;
  private String mtkgArDept;
  private String svcArOffice;
  private String pccArDept;

  // for mktg svc mapping
  private String mktgDepartmentAR;
  private String svcOfficeAR;

  /**
   * Gets the value of CSO Site based on request data and mapping
   * 
   * @param entityManager
   * @param requestData
   * @return
   * @throws CmrException
   */
  public String getCsoSite(EntityManager entityManager, RequestData requestData) throws CmrException {
    String calculatedCsoSite = "";
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    switch (csoSite) {
    case REQUEST:
      calculatedCsoSite = StringUtils.isBlank(data.getCsoSite()) ? "" : data.getCsoSite();
      break;
    case LOGIC:
      if (USUtil.SC_BP_POOL.equals(scenario)) {
        if ("CreateCMR-BP".equals(admin.getSourceSystId())) {
          if (USBPHandler.BP_INDIRECT_REMARKETER.equals(data.getBpName())) {
            calculatedCsoSite = "DV4";
          } else if (USBPHandler.BP_MANAGING_IR.equals(data.getBpName())) {
            calculatedCsoSite = "YBV";
          }
        } else if (BluePagesHelper.getPerson(admin.getRequesterId()) != null) {
          calculatedCsoSite = "TT2";
        }
      }
      break;
    default:
      calculatedCsoSite = StringUtils.isBlank(csoSite) ? "" : csoSite;
      break;
    }
    return calculatedCsoSite;
  }

  /**
   * Gets the value of PCC AR Dept based on request data and mapping
   * 
   * @param entityManager
   * @param requestData
   * @return
   * @throws Exception
   */
  public String getPccArDept(EntityManager entityManager, RequestData requestData) throws Exception {
    String calculatedPccArDept = "";
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    switch (pccArDept) {
    case REQUEST:
      calculatedPccArDept = StringUtils.isBlank(data.getPccArDept()) ? "" : data.getPccArDept();
      break;
    case LOGIC:
      if (USUtil.SC_BP_END_USER.equals(scenario) || USUtil.SC_BP_POOL.equals(scenario) || USUtil.SC_BP_DEVELOP.equals(scenario)
          || USUtil.SC_BP_E_HOST.equals(scenario) || USUtil.SC_FSP_END_USER.equals(scenario) || USUtil.SC_FSP_POOL.equals(scenario)) {
        String mktArBo = getMtkgArDept(entityManager, requestData);
        if ("DI3".equals(mktArBo)) {
          calculatedPccArDept = "G8G";
        } else {
          calculatedPccArDept = "G8M";
        }
      }
      break;
    default:
      if (USUtil.SC_BYMODEL.equals(data.getCustSubGrp())
          && !(USUtil.SC_FED_REGULAR.equals(scenario) || USUtil.SC_FED_POA.equals(scenario) || USUtil.SC_REST_OIO.equals(scenario))) {
        USDetailsContainer usDetails = USUtil.determineUSCMRDetails(entityManager, admin.getModelCmrNo(), requestData);
        if ("5AA".equalsIgnoreCase(usDetails.getPccArDept())) {
          calculatedPccArDept = "G8M";
        } else {
          calculatedPccArDept = usDetails.getPccArDept();
        }
      } else {
        calculatedPccArDept = StringUtils.isBlank(pccArDept) ? "" : pccArDept;
      }
      break;
    }
    return calculatedPccArDept;
  }

  /**
   * Gets the value of Marketing Dept based on request data and mapping
   * 
   * @param entityManager
   * @param requestData
   * @return
   * @throws Exception
   */
  public String getMktgDept(EntityManager entityManager, RequestData requestData) throws Exception {
    String calculatedMktgDept = "";
    Data data = requestData.getData();
    Addr installAt = requestData.getAddress("ZS01");
    String stateToMatch = "";
    String countyToMatch = "";
    if (installAt != null) {
      stateToMatch = StringUtils.isBlank(installAt.getStateProv()) ? "" : installAt.getStateProv();
      countyToMatch = StringUtils.isBlank(installAt.getCounty()) ? "" : installAt.getCounty();
    }

    switch (mktgDept) {
    case REQUEST:
      calculatedMktgDept = StringUtils.isBlank(data.getMktgDept()) ? "" : data.getMktgDept();
      break;
    case LOGIC:
      if (USUtil.FEDERAL_SCENARIOS.contains(scenario) && !USUtil.SC_FED_FEDSTATE.equals(scenario)) {
        if ("MO".equals(stateToMatch)) {
          if (STATE_MO_M3A != null && STATE_MO_M3A.contains(countyToMatch)) {
            calculatedMktgDept = "M3A";
          } else if (STATE_MO_M3B != null && STATE_MO_M3B.contains(countyToMatch)) {
            calculatedMktgDept = "M3B";
          }
        } else if ("PA".equals(stateToMatch)) {
          if (STATE_PA_K9Y != null && STATE_PA_K9Y.contains(countyToMatch)) {
            calculatedMktgDept = "K9Y";
          } else if (STATE_PA_M3B != null && STATE_PA_M3B.contains(countyToMatch)) {
            calculatedMktgDept = "M3B";
          }
        } else {
          for (Entry<String, List<String>> entry : stateMktgDepMap.entrySet()) {
            List<String> stateList = entry.getValue();
            if (stateList != null && stateList.size() != 0 && stateList.contains(stateToMatch)) {
              calculatedMktgDept = entry.getKey();
              break;
            }
          }
        }
      }
      break;
    default:
      calculatedMktgDept = StringUtils.isBlank(mktgDept) ? "" : mktgDept;
      break;
    }
    return calculatedMktgDept;
  }

  /**
   * Gets the value of Marketing AR Dept based on request data and mapping
   * 
   * @param entityManager
   * @param requestData
   * @return
   * @throws Exception
   */
  public String getMtkgArDept(EntityManager entityManager, RequestData requestData) throws Exception {
    String calculatedMtkgArDept = "";
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    switch (mtkgArDept) {
    case REQUEST:
      calculatedMtkgArDept = StringUtils.isBlank(data.getMtkgArDept()) ? "" : data.getMtkgArDept();
      break;
    case LOGIC:
      LOG.debug("Calculating Mktg Ar BO using logic");
      if (USUtil.SC_FED_POA.equals(scenario)) {
        if (data != null && StringUtils.isNotBlank(data.getCompany()) && COMP_NO_LIST != null && COMP_NO_LIST.contains(data.getCompany())) {
          calculatedMtkgArDept = "14W";
        } else {
          calculatedMtkgArDept = "28W";
        }
      } else if (USUtil.SC_BP_POOL.equals(scenario)) {
        if ("CreateCMR-BP".equals(admin.getSourceSystId())) {
          if ("MIR".equals(data.getBpName()) || "IRMR".equals(data.getBpName())) {
            calculatedMtkgArDept = "DI3";
          }
        } else if (BluePagesHelper.getPerson(admin.getRequesterId()) != null) {
          calculatedMtkgArDept = "7NZ";
        }
      } else if ("Y".equals(admin.getPaygoProcessIndc())) {
        calculatedMtkgArDept = PAYGO_MKT_ARBO;
      } else {
        if (data != null && StringUtils.isNotBlank(data.getEnterprise())) {
          /*
           * LOG.
           * debug("Getting Marketing A/R BO with highest CMR count belonging to the enterprise="
           * + data.getEnterprise()); String url =
           * SystemConfiguration.getValue("CMR_SERVICES_URL"); String usSchema =
           * SystemConfiguration.getValue("US_CMR_SCHEMA"); String sql =
           * ExternalizedQuery.getSql("AUTO.GET_MKTG_AR_DEPT_USCMR", usSchema);
           * sql = StringUtils.replace(sql, ":ENTERPRISE", "'" +
           * (StringUtils.isNotBlank(data.getEnterprise()) ?
           * data.getEnterprise() : "") + "'"); String dbId =
           * QueryClient.USCMR_APP_ID;
           * 
           * QueryRequest query = new QueryRequest(); query.setSql(sql);
           * query.setRows(1); query.addField("I_CUST_OFF_3");
           * 
           * QueryClient client =
           * CmrServicesFactory.getInstance().createClient(url,
           * QueryClient.class); QueryResponse response =
           * client.executeAndWrap(dbId, query, QueryResponse.class); if
           * (response.isSuccess() && response.getRecords() != null &&
           * response.getRecords().size() != 0) { Map<String, Object> record =
           * response.getRecords().get(0); calculatedMtkgArDept = (String)
           * record.get("I_CUST_OFF_3"); }
           */
          if (StringUtils.isNotBlank(data.getMtkgArDept())) {
            calculatedMtkgArDept = data.getMtkgArDept().trim();
          }
        }
        if (StringUtils.isBlank(calculatedMtkgArDept)) {
          LOG.debug("Marketing A/R BO Blank. Calculating using Sub Industry Mapping");
          String indToMatch = StringUtils.isBlank(data.getSubIndustryCd()) ? "" : data.getSubIndustryCd().substring(0, 1);
          // iterate and display values
          for (Entry<String, List<String>> entry : indARBOMap.entrySet()) {
            List<String> industryList = entry.getValue();
            LOG.debug("Checking Entry for ARBO>>" + entry.getKey());
            if (industryList != null && industryList.size() != 0 && industryList.contains(indToMatch)) {
              calculatedMtkgArDept = entry.getKey();
              break;
            }
          }
        }
      }
      break;
    default:
      if ("Y".equals(admin.getPaygoProcessIndc())) {
        calculatedMtkgArDept = PAYGO_MKT_ARBO;
      } else {
        calculatedMtkgArDept = StringUtils.isBlank(mtkgArDept) ? "" : mtkgArDept;
      }
      break;
    }
    LOG.debug("Calculated MktgARBO=" + calculatedMtkgArDept);
    return calculatedMtkgArDept;
  }

  /**
   * 
   * Gets the value of SVC AR Dept based on request data and mapping
   * 
   * @param entityManager
   * @param requestData
   * @return
   * @throws Exception
   */
  public String getSvcArOffice(EntityManager entityManager, RequestData requestData) throws Exception {
    String calculatedSvcArOffice = "";
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    switch (svcArOffice) {
    case REQUEST:
      calculatedSvcArOffice = StringUtils.isBlank(data.getSvcArOffice()) ? "" : data.getSvcArOffice();
      break;
    case LOGIC:
      if (!USUtil.svcARBOMappings.isEmpty()) {
        String mtkgArDept = getMtkgArDept(entityManager, requestData);
        for (USBranchOffcMapping svcMapping : USUtil.svcARBOMappings) {
          if (mtkgArDept.equalsIgnoreCase(svcMapping.getMktgDepartmentAR())) {
            calculatedSvcArOffice = StringUtils.isNotBlank(svcMapping.getSvcOfficeAR()) ? svcMapping.getSvcOfficeAR() : "";
            break;
          }
        }
      }
      break;
    default:
      if ("Y".equals(admin.getPaygoProcessIndc())) {
        calculatedSvcArOffice = PAYGO_SVC_ARBO;
      } else {
        calculatedSvcArOffice = StringUtils.isBlank(svcArOffice) ? "" : svcArOffice;
      }
      break;
    }
    // catch all for paygo
    if (StringUtils.isBlank(calculatedSvcArOffice) && "Y".equals(admin.getPaygoProcessIndc())) {
      calculatedSvcArOffice = PAYGO_SVC_ARBO;
    }
    return calculatedSvcArOffice;
  }

  public String getScenario() {
    return scenario;
  }

  public void setScenario(String scenario) {
    this.scenario = scenario;
  }

  public void setCsoSite(String csoSite) {
    this.csoSite = csoSite;
  }

  public void setMktgDept(String mktgDept) {
    this.mktgDept = mktgDept;
  }

  public void setMtkgArDept(String mtkgArDept) {
    this.mtkgArDept = mtkgArDept;
  }

  public void setSvcArOffice(String svcArOffice) {
    this.svcArOffice = svcArOffice;
  }

  public void setPccArDept(String pccArDept) {
    this.pccArDept = pccArDept;
  }

  public String getMktgDepartmentAR() {
    return mktgDepartmentAR;
  }

  public void setMktgDepartmentAR(String mktgDepartmentAR) {
    this.mktgDepartmentAR = mktgDepartmentAR;
  }

  public String getSvcOfficeAR() {
    return svcOfficeAR;
  }

  public void setSvcOfficeAR(String svcOfficeAR) {
    this.svcOfficeAR = svcOfficeAR;
  }

}
