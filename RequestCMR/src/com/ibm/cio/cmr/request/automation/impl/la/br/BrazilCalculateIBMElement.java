/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.la.br;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.OverridingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.geo.BrazilUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.GeoContactInfo;
import com.ibm.cio.cmr.request.entity.GeoTaxInfo;
import com.ibm.cio.cmr.request.entity.ReftBrCnae;
import com.ibm.cio.cmr.request.entity.ReftBrSboCollector;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.la.br.ConsultaCCCResponse;
import com.ibm.cmr.services.client.automation.la.br.MidasRequest;
import com.ibm.cmr.services.client.automation.la.br.MidasResponse;
import com.ibm.cmr.services.client.automation.la.br.SintegraResponse;

/**
 * Connects to the CEDP Data Lake, MIDAS service to fetch values
 * 
 * @author Rangoli Saxena
 * 
 */
public class BrazilCalculateIBMElement extends OverridingElement {

  private static final Logger LOG = Logger.getLogger(BrazilCalculateIBMElement.class);

  private static final List<String> sboToBeChecked = Arrays.asList("504", "505", "556", "657", "758", "759", "761", "763", "764", "765");
  public boolean sboToBeUnchanged = false;

  public BrazilCalculateIBMElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @SuppressWarnings("unchecked")
  @Override
  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    long reqId = requestData.getAdmin().getId().getReqId();

    Addr soldTo = requestData.getAddress("ZS01");
    Addr installAt = requestData.getAddress("ZI01");
    boolean ifErrorSoldTo = false;
    boolean ifErrorInstallAt = false;
    String reqType = admin.getReqType();
    sboToBeUnchanged = sboToBeChecked.contains(data.getSalesBusOffCd());

    // save the request data, this populates the internally computed and default
    // field values
    saveRequestDataUsingHandlers(entityManager, admin, data, soldTo, installAt);

    AutomationResult<OverrideOutput> results = buildResult(reqId);
    OverrideOutput overrides = new OverrideOutput(false);
    StringBuilder details = new StringBuilder();
    String scenarioSubType = "";
    String vat = null;
    AutomationResponse<MidasResponse> midasResponse = null;

    // compute for the scenario subtype
    if ("C".equals(admin.getReqType()) && data != null) {
      scenarioSubType = data.getCustSubGrp();
    } else if ("U".equals(admin.getReqType())) {
      scenarioSubType = LAHandler.getScenarioTypeForUpdateBRV2(entityManager, data, requestData.getAddresses());
    }

    // Set Data.LOCN_NO to 91000 for stateProv 'RS' -- fix for story 1723916
    if (SystemLocation.BRAZIL.equals(data.getCmrIssuingCntry()) && soldTo != null && "RS".equals(soldTo.getStateProv())) {
      LOG.debug("Setting DATA.LOCN_NO for StateProv 'RS' to '91000'");
      overrides.addOverride(getProcessCode(), "DATA", "LOCN_NO", data.getLocationNumber(), "91000");
    } else if (SystemLocation.BRAZIL.equals(data.getCmrIssuingCntry()) && StringUtils.isNotBlank(data.getLocationNumber())
        && data.getLocationNumber().equals("90000")) {
      LOG.debug("Clearing DATA.LOCN_NO for value '90000'");
      overrides.addOverride(getProcessCode(), "DATA", "LOCN_NO", data.getLocationNumber(), "");
    }

    // skip setting of sbo via state for this scenarios
    final List<String> skipScenarios = Arrays.asList("IBMEM", "PRIPE", "BUSPR", "INTER");
    final List<String> skipSBO = Arrays.asList("515", "979", "461", "010");

    // Set Data.SALES_BO_CD for stateProv Coverage change !!
    final List<String> STATEPROV_763 = Arrays.asList("AM", "PA", "AC", "RO", "RR", "AP", "TO", "MA", "PI", "CE", "RN", "PB", "PE", "AL", "SE", "BA");
    final List<String> STATEPROV_504 = Arrays.asList("DF", "GO", "MT", "MS");
    final List<String> STATEPROV_758 = Arrays.asList("PR", "SC", "RS");
    if (SystemLocation.BRAZIL.equals(data.getCmrIssuingCntry()) && soldTo != null && reqType.equals("C")
        && (!skipScenarios.contains(data.getCustSubGrp()) && !skipSBO.contains(data.getSalesBusOffCd()))) {
      if ("ES".equals(soldTo.getStateProv()) || "RJ".equals(soldTo.getStateProv())) {
        overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "761");
      } else if ("SP".equals(soldTo.getStateProv())) {
        overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "764");
      } else if ("MG".equals(soldTo.getStateProv())) {
        overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "556");
      } else if (STATEPROV_758.contains(soldTo.getStateProv())) {
        overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "758");
      } else if (STATEPROV_504.contains(soldTo.getStateProv())) {
        overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "504");
      } else if (STATEPROV_763.contains(soldTo.getStateProv())) {
        overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "763");
      }
    }

    // if the request is an update, do not calculate IBM fields
    if ("U".equals(admin.getReqType()) && !"REAC".equalsIgnoreCase(admin.getReqReason())) {

      boolean checkIfFiscalCodeExists = true;
      checkIfFiscalCodeExists = ifStateFiscalCodeEmpty(scenarioSubType, details, overrides, engineData, admin, data, soldTo, installAt,
          midasResponse);
      if (checkIfFiscalCodeExists == false) {
        ifErrorSoldTo = checkSintegraResponse(soldTo.getVat(), soldTo.getStateProv(), results, overrides, "ZS01", soldTo, "Sold To", details, data);
        if ("LEASI".equalsIgnoreCase(scenarioSubType)) {
          ifErrorInstallAt = checkSintegraResponse(installAt.getVat(), installAt.getStateProv(), results, overrides, "ZI01", installAt, "Install At",
              details, data);
        }
        if (ifErrorSoldTo || ifErrorInstallAt) {
          LOG.debug("State Fiscal Code is empty , hence adding negative check status");
          // results.setResults("Fiscal Code empty");
          engineData.addNegativeCheckStatus("StateFiscalCode", "State/Fiscal code is empty");
          // details.append("\nSending back to Processor as state fiscal code is
          // empty.Enter valid fiscal code value.");
          // engineData.addRejectionComment("State Fiscal Code is Empty.");
          // results.setOnError(true);
        }
        results.setResults("Skipped.");
        results.setDetails("Skipping retrieval of IBM values for updates.");
        results.setProcessOutput(overrides);
      } else {
        results.setProcessOutput(overrides);
        results.setResults("Skipped.");
        results.setDetails("Skipping retrieval of IBM values for updates.");
      }
      return results;
    }

    if ("5COMP".equalsIgnoreCase(scenarioSubType) || "5PRIP".equalsIgnoreCase(scenarioSubType) || "SOFTL".equalsIgnoreCase(scenarioSubType)
        || "NEW".equalsIgnoreCase(scenarioSubType) || "AUCO".equalsIgnoreCase(admin.getReqReason()) || "IBMEM".equalsIgnoreCase(scenarioSubType)
        || "PRIPE".equalsIgnoreCase(scenarioSubType)) {

      boolean checkIfFiscalCodeExists = true;
      checkIfFiscalCodeExists = ifStateFiscalCodeEmpty(scenarioSubType, details, overrides, engineData, admin, data, soldTo, installAt,
          midasResponse);
      if (checkIfFiscalCodeExists == false) {
        ifErrorSoldTo = checkSintegraResponse(soldTo.getVat(), soldTo.getStateProv(), results, overrides, "ZS01", soldTo, "Sold To", details, data);
        if ("LEASI".equalsIgnoreCase(scenarioSubType)) {
          ifErrorInstallAt = checkSintegraResponse(installAt.getVat(), installAt.getStateProv(), results, overrides, "ZI01", installAt, "Install At",
              details, data);
        }
        if (ifErrorSoldTo || ifErrorInstallAt) {
          LOG.debug("State Fiscal Code is empty , hence adding negative check status");
          // results.setResults("Fiscal Code empty");
          engineData.addNegativeCheckStatus("StateFiscalCode", "State/Fiscal code is empty");
          // details.append("\nSending back to Processor as state fiscal code is
          // empty.Enter valid fiscal code value.");
          // engineData.addRejectionComment("State Fiscal Code is Empty.");
          // results.setOnError(true);
        }
        results.setResults("Execution Not Performed.");
        results.setDetails("Skipping retrieval of IBM values for scenarios.");
        // results.setDetails(details.toString());
        results.setProcessOutput(overrides);
      } else {
        LOG.debug("Scenario " + scenarioSubType + " does not need calculation..");
        results.setResults("Execution Not Performed.");
        results.setDetails("Skipping retrieval of IBM values for scenarios.");
        results.setProcessOutput(overrides);
      }
      return results;

    } else if ("SOFTL".equalsIgnoreCase(scenarioSubType) && !StringUtils.isBlank(soldTo.getVat())
        && (soldTo.getVat().matches("0{14}") || soldTo.getVat().matches("9{14}"))) {

      boolean checkIfFiscalCodeExists = true;
      checkIfFiscalCodeExists = ifStateFiscalCodeEmpty(scenarioSubType, details, overrides, engineData, admin, data, soldTo, installAt,
          midasResponse);
      if (checkIfFiscalCodeExists == false) {
        ifErrorSoldTo = checkSintegraResponse(soldTo.getVat(), soldTo.getStateProv(), results, overrides, "ZS01", soldTo, "Sold To", details, data);
        if ("LEASI".equalsIgnoreCase(scenarioSubType)) {
          ifErrorInstallAt = checkSintegraResponse(installAt.getVat(), installAt.getStateProv(), results, overrides, "ZI01", installAt, "Install At",
              details, data);
        }
        if (ifErrorSoldTo || ifErrorInstallAt) {
          LOG.debug("State Fiscal Code is empty , hence adding negative check status");
          // results.setResults("Fiscal Code empty");
          engineData.addNegativeCheckStatus("StateFiscalCode", "State/Fiscal code is empty");
          // details.append("\nSending back to Processor as state fiscal code is
          // empty.Enter valid fiscal code value.");
          // engineData.addRejectionComment("State Fiscal Code is Empty.");
          // results.setOnError(true);
        }
        // results.setDetails(details.toString());
        results.setResults("Execution Not Performed.");
        results.setDetails("Skipping retrieval of IBM values for Softlayer scenario as VAT contains either all 0s or all 9s.");
        results.setProcessOutput(overrides);
      } else {
        LOG.debug("Scenario " + scenarioSubType + " does not need calculation..");
        results.setResults("Execution Not Performed.");
        results.setDetails("Skipping retrieval of IBM values for Softlayer scenario as VAT contains either all 0s or all 9s.");
        results.setProcessOutput(overrides);
      }
      return results;
    } else if ("CROSS".equalsIgnoreCase(scenarioSubType)) {
      details.append("\nValues computed for Cross Border scenario : \n");
      vat = soldTo.getVat();
      if (StringUtils.isNotBlank(vat) && engineData.get(vat) != null) {
        midasResponse = (AutomationResponse<MidasResponse>) engineData.get(vat);
      } else {
        midasResponse = queryMidas(reqId, vat);
      }

      String sql = ExternalizedQuery.getSql("BR.AUTO.GET_VAL_FROM_STATE");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("STATE", "SP");
      ReftBrSboCollector sbo = query.getSingleResult(ReftBrSboCollector.class);
      if (sbo != null) {
        details.append("Collector Number = " + sbo.getCollectorNo() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "COLLECTOR_NO", data.getCollectorNameNo(), sbo.getCollectorNo());

        if (!sboToBeUnchanged && reqType.equals("C")) {
          overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo.getSbo());
        }
        details.append("Search Term/Sales Branch Office = " + data.getSalesBusOffCd() + "\n");

        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "MRC_CD", data.getMrcCd(), sbo.getMrcCd());
        }
        details.append("Market Responsibility Code (MRC) = " + data.getMrcCd() + "\n");

        // SET ISU based on MRC
        String isu = getISUCode(entityManager, sbo.getMrcCd(), "");
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), isu);
        }
        details.append("ISU = " + data.getIsuCd() + "\n");

        // SET Client Tier based on MRC and ISU
        String clientTier = getClientTier(entityManager, sbo.getMrcCd(), isu);
        details.append("Client Tier = " + clientTier + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), clientTier);

        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE", data.getMrcCd(), sbo.getMrcCd());
        }
        details.append("Country Use = " + data.getMrcCd() + "\n");

        // SET MRC & Country Use as per sboToBeChecked only
        String autoSbo = sbo.getSbo();
        if (sboToBeChecked.contains(autoSbo)) {
          overrides.addOverride(getProcessCode(), "DATA", "MRC_CD", data.getMrcCd(), "Q");
          LOG.debug("Override value of MRC_CD" + data.getMrcCd());
          overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE", data.getMrcCd(), "Q");
          LOG.debug("Override value of CNTRY_USE" + data.getMrcCd());
        }

        details.append("Sales Rep No = " + sbo.getSbo() + "001" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(), sbo.getSbo() + "001");
      }

      boolean checkIfFiscalCodeExists = true;
      checkIfFiscalCodeExists = ifStateFiscalCodeEmpty(scenarioSubType, details, overrides, engineData, admin, data, soldTo, installAt,
          midasResponse);
      if (checkIfFiscalCodeExists == false) {
        ifErrorSoldTo = checkSintegraResponse(soldTo.getVat(), soldTo.getStateProv(), results, overrides, "ZS01", soldTo, "Sold To", details, data);
        if ("LEASI".equalsIgnoreCase(scenarioSubType)) {
          ifErrorInstallAt = checkSintegraResponse(installAt.getVat(), installAt.getStateProv(), results, overrides, "ZI01", installAt, "Install At",
              details, data);
        }
        if (ifErrorSoldTo || ifErrorInstallAt) {
          LOG.debug("State Fiscal Code is empty , hence adding negative check status");
          // results.setResults("Fiscal Code empty");
          engineData.addNegativeCheckStatus("StateFiscalCode", "State/Fiscal code is empty");
          // details.append("\nSending back to Processor as state fiscal code is
          // empty.Enter valid fiscal code value.");
          // engineData.addRejectionComment("State Fiscal Code is Empty.");
          // results.setOnError(true);
        }
        results.setResults("Successful Execution");
      } else {
        results.setResults("Successful Execution");
      }
      results.setDetails(details.toString());
      results.setProcessOutput(overrides);
      return results;

      /*
       * boolean checkIfFiscalCodeExists = true; checkIfFiscalCodeExists =
       * ifStateFiscalCodeEmpty(scenarioSubType, details, overrides, engineData,
       * admin, data, soldTo, installAt, midasResponse); if
       * (checkIfFiscalCodeExists == false) {
       * details.append("Sold To State Fiscal Code= " + "ISENTO" + "\n");
       * overrides.addOverride(getProcessCode(), "ZS01", "TAX_CD_1",
       * soldTo.getTaxCd1(), "ISENTO"); }
       */
    } else {

      if ("LEASI".equalsIgnoreCase(scenarioSubType)) {
        vat = installAt.getVat();
      } else {
        vat = soldTo.getVat();
      }

      if (StringUtils.isNotBlank(vat) && engineData.get(vat) != null) {
        midasResponse = (AutomationResponse<MidasResponse>) engineData.get(vat);
      } else {
        midasResponse = queryMidas(reqId, vat);
      }

      // root matching first
      boolean rootMatchFound = performRootMatching(entityManager, data, vat, reqId, overrides, details, scenarioSubType, reqType);
      if (!rootMatchFound) {
        // no matches, calculate using check tables
        LOG.debug("No root matches found for VAT " + vat);
        computeValuesPerMappings(entityManager, details, overrides, reqId, data, midasResponse, soldTo, reqType);
      } else {
        // Defect CMR-392
        computeISICPerMappings(entityManager, details, overrides, reqId, data, midasResponse, soldTo);
        // CMR - 720
        setDefaultCollectorNo(entityManager, overrides, data, midasResponse, soldTo);
      }

      // override scenario fields
      calculateScenarioFields(entityManager, scenarioSubType, details, overrides, engineData, admin, data, soldTo, installAt, midasResponse);

      // scenario fields end
      boolean checkIfFiscalCodeExists = true;
      checkIfFiscalCodeExists = ifStateFiscalCodeEmpty(scenarioSubType, details, overrides, engineData, admin, data, soldTo, installAt,
          midasResponse);
      if (checkIfFiscalCodeExists == false) {
        ifErrorSoldTo = checkSintegraResponse(soldTo.getVat(), soldTo.getStateProv(), results, overrides, "ZS01", soldTo, "Sold To", details, data);
        if ("LEASI".equalsIgnoreCase(scenarioSubType)) {
          ifErrorInstallAt = checkSintegraResponse(vat, installAt.getStateProv(), results, overrides, "ZI01", installAt, "Install At", details, data);
        }
        if (ifErrorSoldTo || ifErrorInstallAt) {
          LOG.debug("State Fiscal Code is empty , hence adding negative check status");
          // results.setResults("Fiscal Code empty");
          engineData.addNegativeCheckStatus("StateFiscalCode", "State/Fiscal code is empty");
          // details.append("\nSending back to Processor as state fiscal code is
          // empty.Enter valid fiscal code value.");
          // engineData.addRejectionComment("State Fiscal Code is Empty.");
          // results.setOnError(true);
        }
        results.setResults("Successful Execution");
      } else {
        results.setResults("Successful Execution");
      }
      results.setDetails(details.toString());
      results.setProcessOutput(overrides);

    }
    return results;
  }

  /**
   * Queries MIDAS service and returns the response from the given VAT
   * 
   * @param vat
   * @return
   */
  private AutomationResponse<MidasResponse> queryMidas(long reqId, String vat) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    // calling MIDAS Service
    LOG.debug("Calling MIDAS Service for Sold To address for Req_id : " + reqId);
    MidasRequest requestSoldTo = new MidasRequest();
    requestSoldTo.setCnpj(vat);

    LOG.debug("Connecting to the MIDAS Service (ZS01) at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponseSoldTo = client.executeAndWrap(AutomationServiceClient.BR_MIDAS_SERVICE_ID, requestSoldTo,
        AutomationResponse.class);
    ObjectMapper mapperSoldTo = new ObjectMapper();
    String jsonSoldTo = mapperSoldTo.writeValueAsString(rawResponseSoldTo);
    LOG.trace("MIDAS Service Response for VAT " + vat + ": " + jsonSoldTo);

    TypeReference<AutomationResponse<MidasResponse>> refSoldTo = new TypeReference<AutomationResponse<MidasResponse>>() {
    };
    return mapperSoldTo.readValue(jsonSoldTo, refSoldTo);

  }

  /**
   * Performs root matching against the VAT supplied and returns true if root
   * matches are found.
   * 
   * @param entityManager
   * @param data
   * @param vat
   * @param reqId
   * @param overrides
   * @param details
   * @return
   */
  private boolean performRootMatching(EntityManager entityManager, Data data, String vat, long reqId, OverrideOutput overrides, StringBuilder details,
      String scenarioSubType, String reqType) {
    EntityManager cedpManager = JpaManager.getEntityManager("CEDP");
    String kukla = "";
    sboToBeUnchanged = sboToBeChecked.contains(data.getSalesBusOffCd());
    // compute kukla
    if (!StringUtils.isBlank(scenarioSubType) && "BUSPR".equalsIgnoreCase(scenarioSubType)) {
      kukla = "45";
      return false;
    } else if (!StringUtils.isBlank(scenarioSubType) && "INTER".equalsIgnoreCase(scenarioSubType)) {
      kukla = "81";
      return false;
    } else {
      kukla = "";
    }
    try {

      LOG.debug("Querying CEDP data lake for root matching for Req_id : " + reqId);
      String sql = ExternalizedQuery.getSql("BR.AUTO.CEDP.ROOT_MATCH");
      PreparedQuery query = new PreparedQuery(cedpManager, sql);

      String rootVat = vat != null && vat.length() > 8 ? vat.substring(0, 8) : vat;
      query.setParameter("VAT", rootVat + "%");
      query.setParameter("KUKLA", kukla);

      List<Object[]> rootMatches = query.getResults(1);
      LOG.debug("Root matches found for VAT " + vat);
      if (rootMatches != null && rootMatches.size() > 0) {
        Object[] rootMatch = rootMatches.get(0);
        LOG.debug("Setting values from root match for Req_id : " + reqId);
        details.append("Matches found from Root Matching.\n");
        details.append("Results from Root Matching : \n");

        String inac = (String) rootMatch[7];
        details.append("INAC/NAC Code = " + inac + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "INAC_CD", data.getInacCd(), inac);

        if (!StringUtils.isBlank(inac)) {
          String inacType = "N";
          if (StringUtils.isNumeric(inac)) {
            inacType = "I";
          }
          details.append("INAC Type = " + inacType + "\n");
          overrides.addOverride(getProcessCode(), "DATA", "INAC_TYPE", data.getInacType(), inacType);
        }

        details.append("Company Number = " + (String) rootMatch[6] + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "COMPANY", data.getCompany(), (String) rootMatch[6]);

        details.append("Collector Number = " + (String) rootMatch[11] + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "COLLECTOR_NO", data.getCollectorNameNo(), (String) rootMatch[11]);

        if (!sboToBeUnchanged && reqType.equals("C")) {
          overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), (String) rootMatch[8]);
        }
        details.append("Search Term/Sales Branch Office = " + data.getSalesBusOffCd() + "\n");

        String mrc = (String) rootMatch[9];
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "MRC_CD", data.getMrcCd(), mrc);
        }
        details.append("Market Responsibility Code (MRC) = " + data.getMrcCd() + "\n");

        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE", data.getMrcCd(), (String) rootMatch[9]);
        }
        details.append("Country Use = " + data.getMrcCd() + "\n");

        // SET MRC & Country Use as per sboToBeChecked only
        String autoSbo = (String) rootMatch[8];
        if (sboToBeChecked.contains(autoSbo)) {
          overrides.addOverride(getProcessCode(), "DATA", "MRC_CD", data.getMrcCd(), "Q");
          LOG.debug("Override value of MRC_CD" + data.getMrcCd());
          overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE", data.getMrcCd(), "Q");
          LOG.debug("Override value of CNTRY_USE" + data.getMrcCd());
        }

        details.append("Sales Rep No = " + (String) rootMatch[8] + "001" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(), (String) rootMatch[8] + "001");

        // commented for defect CMR-392
        /*
         * details.append("ISIC = " + (String) rootMatch[10] + "\n");
         * overrides.addOverride(getProcessCode(), "DATA", "ISIC_CD",
         * data.getIsicCd(), (String) rootMatch[10]);
         * 
         * String subIndustry = RequestUtils.getSubIndustryCd(entityManager,
         * (String) rootMatch[10], data.getCmrIssuingCntry());
         * details.append("Subindustry Code = " + subIndustry + "\n");
         * overrides.addOverride(getProcessCode(), "DATA", "SUB_INDUSTRY_CD",
         * data.getSubIndustryCd(), subIndustry);
         * 
         * String legacyIndustry = getLegacyIndustryCd(entityManager,
         * subIndustry); details.append("Legacy Industry Code = " +
         * legacyIndustry + "\n"); overrides.addOverride(getProcessCode(),
         * "DATA", "LEGACY_IND_CD", data.getLegacyIndustryCode(),
         * legacyIndustry);
         */

        String isu = getISUCode(entityManager, mrc, (String) rootMatch[14]);
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), isu);
        }
        details.append("ISU = " + data.getIsuCd() + "\n");

        String clientTier = getClientTier(entityManager, mrc, isu);
        if (StringUtils.isBlank(clientTier)) {
          clientTier = (String) rootMatch[15];
        }
        details.append("Client Tier = " + clientTier + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), clientTier);

        return true;
      } else {
        return false;
      }
    } finally {
      cedpManager.clear();
      cedpManager.close();
    }
  }

  /**
   * Uses the check tables to compute ISIC for the values to assign to fields
   * 
   * @param entityManager
   * @param details
   * @param overrides
   * @param reqId
   * @param data
   * @param midasResponse
   * @param soldTo
   */
  private void computeISICPerMappings(EntityManager entityManager, StringBuilder details, OverrideOutput overrides, long reqId, Data data,
      AutomationResponse<MidasResponse> midasResponse, Addr soldTo) {
    LOG.debug("Root Match found. Setting computed values for ISIC, Sub industry and legacy ind for Req_id : " + reqId);
    if (midasResponse != null && midasResponse.isSuccess()) {
      details.append("\nComputed fields using check tables: \n");
      String cnae = midasResponse.getRecord().getCnae();
      String state = midasResponse.getRecord().getState();
      LOG.debug("Cnae : " + cnae);
      String sql = ExternalizedQuery.getSql("BR.AUTO.GET_VAL_FROM_CNAE");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CNAE", cnae);
      ReftBrCnae cnaeRecord = query.getSingleResult(ReftBrCnae.class);
      if (cnaeRecord != null) {
        // SET ISIC
        details.append("ISIC = " + cnaeRecord.getIsicCd() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "ISIC_CD", data.getIsicCd(), cnaeRecord.getIsicCd());

        // SET SIC - Fix for SubIndustryCode not shown on UI
        details.append("Subindustry Code = " + cnaeRecord.getSubIndustryCd() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), cnaeRecord.getSubIndustryCd());

        String legacyIndustry = getLegacyIndustryCd(entityManager, cnaeRecord.getSubIndustryCd());
        details.append("Legacy Industry Code = " + legacyIndustry + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "LEGACY_IND_CD", data.getLegacyIndustryCode(), legacyIndustry);
      }
    }
  }

  /**
   * Uses the check tables to compute for the values to assign to fields
   * 
   * @param entityManager
   * @param details
   * @param overrides
   * @param reqId
   * @param data
   * @param midasResponse
   * @param soldTo
   */
  private void computeValuesPerMappings(EntityManager entityManager, StringBuilder details, OverrideOutput overrides, long reqId, Data data,
      AutomationResponse<MidasResponse> midasResponse, Addr soldTo, String reqType) {
    sboToBeUnchanged = sboToBeChecked.contains(data.getSalesBusOffCd());
    LOG.debug("Setting computed values for data fields for Req_id : " + reqId);

    details.append("No VAT root matches.\n");
    details.append("Computed fields using check tables: \n\n");

    if (midasResponse != null && midasResponse.isSuccess()) {
      String cnae = midasResponse.getRecord().getCnae();
      String state = midasResponse.getRecord().getState();
      LOG.debug("Cnae : " + cnae);
      LOG.debug("State : " + state);
      String sql = ExternalizedQuery.getSql("BR.AUTO.GET_VAL_FROM_CNAE");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CNAE", cnae);
      ReftBrCnae cnaeRecord = query.getSingleResult(ReftBrCnae.class);

      sql = ExternalizedQuery.getSql("BR.AUTO.GET_VAL_FROM_STATE");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("STATE", state);
      ReftBrSboCollector sbo = query.getSingleResult(ReftBrSboCollector.class);

      if (sbo != null) {
        details.append("Collector Number = " + sbo.getCollectorNo() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "COLLECTOR_NO", data.getCollectorNameNo(), sbo.getCollectorNo());

        if (!sboToBeUnchanged && reqType.equals("C")) {
          overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo.getSbo());
        }
        details.append("Search Term/Sales Branch Office = " + data.getSalesBusOffCd() + "\n");

        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "MRC_CD", data.getMrcCd(), sbo.getMrcCd());
        }
        details.append("Market Responsibility Code (MRC) = " + data.getMrcCd() + "\n");

        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE", data.getMrcCd(), sbo.getMrcCd());
        }
        details.append("Country Use = " + data.getMrcCd() + "\n");

        // SET MRC & Country Use as per sboToBeChecked only
        String autoSbo = sbo.getSbo();
        if (sboToBeChecked.contains(autoSbo)) {
          overrides.addOverride(getProcessCode(), "DATA", "MRC_CD", data.getMrcCd(), "Q");
          LOG.debug("Override value of MRC_CD" + data.getMrcCd());
          overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE", data.getMrcCd(), "Q");
          LOG.debug("Override value of CNTRY_USE" + data.getMrcCd());
        }

        details.append("Sales Rep No = " + sbo.getSbo() + "001" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(), sbo.getSbo() + "001");
      }

      if (cnaeRecord != null) {
        // SET ISIC
        details.append("ISIC = " + cnaeRecord.getIsicCd() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "ISIC_CD", data.getIsicCd(), cnaeRecord.getIsicCd());

        // SET SIC - Fix for SubIndustryCode not shown on UI
        details.append("Subindustry Code = " + cnaeRecord.getSubIndustryCd() + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), cnaeRecord.getSubIndustryCd());

        String legacyIndustry = getLegacyIndustryCd(entityManager, cnaeRecord.getSubIndustryCd());
        details.append("Legacy Industry Code = " + legacyIndustry + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "LEGACY_IND_CD", data.getLegacyIndustryCode(), legacyIndustry);

        // SET ISU
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), cnaeRecord.getIsuCd());
        }
        details.append("ISU Code = " + data.getIsuCd() + "\n");

        String clientTier = null;
        if (sbo != null) {
          clientTier = getClientTier(entityManager, sbo.getMrcCd(), cnaeRecord.getIsuCd());
        }
        details.append("Client Tier = " + clientTier + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), clientTier);

        if ("5B".equalsIgnoreCase(cnaeRecord.getIsuCd())) {
          state = soldTo.getStateProv();
          sql = ExternalizedQuery.getSql("BR.AUTO.GET_VAL_FROM_STATE");
          query = new PreparedQuery(entityManager, sql);
          query.setParameter("STATE", "M" + state);
          ReftBrSboCollector sboMSP = query.getSingleResult(ReftBrSboCollector.class);

          if (sboMSP != null) {
            // details.append("Search Term/Sales Branch Office = " +
            // sboMSP.getSbo() + "\n");
            // overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD",
            // data.getSalesBusOffCd(), sboMSP.getSbo());

            // if (!sboToBeUnchanged) {
            // overrides.addOverride(getProcessCode(), "DATA", "MRC_CD",
            // data.getMrcCd(), sboMSP.getMrcCd());
            // }
            // details.append("Market Responsibility Code (MRC) = " +
            // data.getMrcCd() + "\n");
            //
            // if (!sboToBeUnchanged) {
            // overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE",
            // data.getMrcCd(), sboMSP.getMrcCd());
            // }
            // details.append("Country Use = " + data.getMrcCd() + "\n");
            // SET MRC & Country Use as per sboToBeChecked only
            // String autoSbo = sboMSP.getSbo();
            // if (sboToBeChecked.contains(autoSbo)) {
            // overrides.addOverride(getProcessCode(), "DATA", "MRC_CD",
            // data.getMrcCd(), "Q");
            // LOG.debug("Override value of MRC_CD" + data.getMrcCd());
            // overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE",
            // data.getMrcCd(), "Q");
            // LOG.debug("Override value of CNTRY_USE" + data.getMrcCd());
            // data.getMrcCd(), "Q");
            // }
          }

          sql = ExternalizedQuery.getSql("BR.AUTO.GET_COLLECTOR_FROM_STATE");
          query = new PreparedQuery(entityManager, sql);
          query.setParameter("STATE", state);
          List<String> collectorList = query.getResults(String.class);

          if (collectorList != null && collectorList.size() > 0) {
            for (String collectorNo : collectorList) {
              details.append("Collector Number = " + collectorNo + "\n");
              overrides.addOverride(getProcessCode(), "DATA", "COLLECTOR_NO", data.getCollectorNameNo(), collectorNo);
            }
          }
        }
      }
    }
  }

  /**
   * Computes the values to be assigned to the request based on the scenario of
   * the request
   * 
   * @param scenarioSubType
   * @param details
   * @param overrides
   * @param engineData
   * @param admin
   * @param data
   * @param soldTo
   * @param installAt
   * @param midasResponse
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void calculateScenarioFields(EntityManager entityManager, String scenarioSubType, StringBuilder details, OverrideOutput overrides,
      AutomationEngineData engineData, Admin admin, Data data, Addr soldTo, Addr installAt, AutomationResponse<MidasResponse> midasResponse)
      throws Exception {
    sboToBeUnchanged = sboToBeChecked.contains(data.getSalesBusOffCd());

    details.append("\nValues computed per scenario (overrides previous values): \n");

    // scenario fields start
    if ("C".equals(admin.getReqType()) || ("U".equals(admin.getReqType()) && "REAC".equalsIgnoreCase(admin.getReqReason()))) {
      if (!sboToBeUnchanged) {
        if (SystemLocation.BRAZIL.equals(data.getCmrIssuingCntry()) && soldTo != null && "ES".equals(soldTo.getStateProv())) {
          LOG.debug("Setting DATA.SALES_BO_CD for StateProv 'ES' to '761'");
          overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "761");
        }
      }

      if ("COMME".equalsIgnoreCase(scenarioSubType)) {
        // SET IBM Bank Number
        details.append("IBM BANK NO = " + "34A" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "IBM_BANK_NO", data.getIbmBankNumber(), "34A");

      } else if ("GOVDI".equalsIgnoreCase(scenarioSubType) || "GOVIN".equalsIgnoreCase(scenarioSubType)) {
        // SET IBM Bank Number
        details.append("IBM BANK NO = " + "34A" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "IBM_BANK_NO", data.getIbmBankNumber(), "34A");

        if (StringUtils.isBlank(data.getGovType())) {
          details.append("GOVERNMENT = " + "OU" + " (" + "OTHER" + ")\n");
          overrides.addOverride(getProcessCode(), "DATA", "GOVERNMENT", "", "OU");
        }

      } else if ("LEASI".equalsIgnoreCase(scenarioSubType)) {
        AutomationResponse<MidasResponse> responseSoldTo = null;
        if (StringUtils.isNotBlank(soldTo.getVat()) && engineData.get(soldTo.getVat()) != null) {
          responseSoldTo = (AutomationResponse<MidasResponse>) engineData.get(soldTo.getVat());
        } else {
          responseSoldTo = queryMidas(admin.getId().getReqId(), soldTo.getVat());
        }
        if (responseSoldTo != null && responseSoldTo.isSuccess() && midasResponse != null && midasResponse.isSuccess()) {
          String abbrevName = ((midasResponse.getRecord().getCompanyName() + " /" + responseSoldTo.getRecord().getCompanyName()).length() > 30)
              ? (midasResponse.getRecord().getCompanyName() + " /" + responseSoldTo.getRecord().getCompanyName()).substring(0, 30)
              : (midasResponse.getRecord().getCompanyName() + " /" + responseSoldTo.getRecord().getCompanyName());
          LOG.debug("Sold To Company Name : " + responseSoldTo.getRecord().getCompanyName());
          LOG.debug("Install At Company Name : " + midasResponse.getRecord().getCompanyName());
          // SET Tax Payer Code
          details.append("Abbreviated Name (TELX1) = " + abbrevName + "\n");
          overrides.addOverride(getProcessCode(), "DATA", "ABBREV_NM", data.getAbbrevNm(), abbrevName);
        }

        // SET IBM Bank Number
        details.append("IBM BANK NO = " + "001" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "IBM_BANK_NO", data.getIbmBankNumber(), "001");

        // SET ICMS IND
        details.append("ICMS IND = " + "N" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "ICMS_IND", data.getIcmsInd(), "1");

        // SET Tax Payer Code
        details.append("Tax Payer Cust Code = " + CmrConstants.DEFAULT_TAX_PAYER_CUS_CD_1 + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "TAX_PAYER_CUST_CD", data.getTaxPayerCustCd(), CmrConstants.DEFAULT_TAX_PAYER_CUS_CD_1);
      } else if ("BUSPR".equalsIgnoreCase(scenarioSubType)) {
        // SET IBM Bank Number
        details.append("IBM BANK NO = " + "34A" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "IBM_BANK_NO", data.getIbmBankNumber(), "34A");

        // SET SBO
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "461");
        }
        details.append("Search Term/Sales Branch Office = " + "461" + "\n");

        // SET MRC
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "MRC_CD", data.getMrcCd(), "Z");
        }
        details.append("Market Responsibility Code (MRC) = " + "Z" + "\n");

        // SET Country Use based on MRC
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE", data.getMrcCd(), "Z");
        }
        details.append("Country Use = " + "Z" + "\n");

        // SET ISU based on MRC
        String isu = getISUCode(entityManager, "Z", "");
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), isu);
        }
        details.append("ISU = " + isu + "\n");

        // SET Client Tier based on MRC and ISU
        String clientTier = getClientTier(entityManager, "Z", isu);
        details.append("Client Tier = " + clientTier + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), clientTier);

        // SET Sales Rep No
        details.append("Sales Rep No = " + "606900" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(), "606900");

        // SET Partnership Indicator
        details.append("Partnership Ind = " + CmrConstants.DEFAULT_BUSPR_PARTNERSHIP_IND + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "PARTNERSHIP_IND", data.getPartnershipInd(), CmrConstants.DEFAULT_BUSPR_PARTNERSHIP_IND);
      } else if ("INTER".equalsIgnoreCase(scenarioSubType)) {
        // SET INAC Code
        details.append("INAC/NAC Code = " + "" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "INAC_CD", data.getInacCd(), "");

        // SET INAC Type
        details.append("INAC Type = " + "" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "INAC_TYPE", data.getInacType(), "");

        // SET Company Number
        details.append("Company Number = " + "" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "COMPANY", data.getCompany(), "");

        // SET IBM Bank Number
        details.append("IBM BANK NO = " + "34A" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "IBM_BANK_NO", data.getIbmBankNumber(), "34A");

        // SET SBO
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "010");
        }
        details.append("Search Term/Sales Branch Office = " + data.getSalesBusOffCd() + "\n");

        // SET MRC
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "MRC_CD", data.getMrcCd(), "9");
        }
        details.append("Market Responsibility Code (MRC) = " + data.getMrcCd() + "\n");

        // SET Country Use based on MRC
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE", data.getMrcCd(), "9");
        }
        details.append("Country Use = " + data.getMrcCd() + "\n");

        // SET ISU based on MRC

        String isu = getISUCode(entityManager, "9", "");
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), isu);
        }
        details.append("ISU = " + data.getIsuCd() + "\n");

        // SET Client Tier based on MRC and ISU
        String clientTier = getClientTier(entityManager, "9", isu);
        details.append("Client Tier = " + clientTier + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), clientTier);

        // SET Sales Rep No
        details.append("Sales Rep No = " + "010200" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(), "010200");

        // SET Partnership Indicator
        details.append("Partnership Ind = " + CmrConstants.DEFAULT_BUSPR_PARTNERSHIP_IND + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "PARTNERSHIP_IND", data.getPartnershipInd(), CmrConstants.DEFAULT_BUSPR_PARTNERSHIP_IND);

        // SET ISIC
        details.append("ISIC = " + "0000" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "ISIC_CD", data.getIsicCd(), "0000");

        // SET SIC - Fix for SubIndustryCode not shown on UI
        details.append("SUB INDUSTRY CODE = " + "ZF" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), "ZF");

        // SET Legacy Industry Code
        String legacyIndustry = getLegacyIndustryCd(entityManager, "ZF");
        details.append("Legacy Industry Code = " + legacyIndustry + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "LEGACY_IND_CD", data.getLegacyIndustryCode(), legacyIndustry);

        // SET Collector Branch Office
        /*
         * details.append("Sucursal/Collection Branch Office = " + "999999" +
         * "\n"); overrides.addOverride(getProcessCode(), "DATA", "COLL_BO_ID",
         * model.getCollBoId(), "999999");
         */
        // SET Collector Number - Defect 1924017 - commenting again due Defect
        // CMR-295
        /*
         * details.append("Collector Number = " + "999999" + "\n");
         * overrides.addOverride(getProcessCode(), "DATA", "COLLECTOR_NO",
         * data.getCollectorNameNo(), "999999");
         */

      } else if ("CC3CC".equalsIgnoreCase(scenarioSubType)) {
        if (midasResponse != null && midasResponse.isSuccess()) {
          String abbrevName = (midasResponse.getRecord().getCompanyName().length() > 26)
              ? "CC3/" + (midasResponse.getRecord().getCompanyName()).substring(0, 26) : "CC3/" + (midasResponse.getRecord().getCompanyName());
          LOG.debug("Sold To Company Name : " + midasResponse.getRecord().getCompanyName());
          // SET Abbreviated Name
          details.append("Abbreviated Name (TELX1) = " + abbrevName + "\n");
          overrides.addOverride(getProcessCode(), "DATA", "ABBREV_NM", data.getAbbrevNm(), abbrevName);
        }
      } else if ("SOFTL".equalsIgnoreCase(scenarioSubType)) {
        // SET Abbreviated Name
        details.append("Abbreviated Name (TELX1) = " + "SOFTLAYER USE ONLY" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "ABBREV_NM", data.getAbbrevNm(), "SOFTLAYER USE ONLY");

        // SET SBO
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "509");
        }
        details.append("Search Term/Sales Branch Office = " + data.getSalesBusOffCd() + "\n");
        // SET MRC
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "MRC_CD", data.getMrcCd(), "Y");
        }
        details.append("Market Responsibility Code (MRC) = " + data.getMrcCd() + "\n");

        // SET Country Use based on MRC
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "CNTRY_USE", data.getMrcCd(), "Y");
        }
        details.append("Country Use = " + data.getMrcCd() + "\n");

        // SET ISU based on MRC
        String isu = getISUCode(entityManager, "Y", "");
        if (!sboToBeUnchanged) {
          overrides.addOverride(getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), isu);
        }
        details.append("ISU = " + data.getIsuCd() + "\n");

        // SET Client Tier based on MRC and ISU
        String clientTier = getClientTier(entityManager, "Y", isu);
        details.append("Client Tier = " + clientTier + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), clientTier);

        // SET Sales Rep No
        details.append("Sales Rep No = " + "509001" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "REP_TEAM_MEMBER_NO", data.getRepTeamMemberNo(), "509001");

        // SET ISIC
        details.append("ISIC = " + "7499" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "ISIC_CD", data.getIsicCd(), "7499");

        // SET Sub Industry Code
        details.append("Subindustry Code = " + "WQ" + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), "WQ");

        String legacyIndustry = getLegacyIndustryCd(entityManager, "WQ");
        details.append("Legacy Industry Code = " + legacyIndustry + "\n");
        overrides.addOverride(getProcessCode(), "DATA", "LEGACY_IND_CD", data.getLegacyIndustryCode(), legacyIndustry);

        // SET Collector No
        if (midasResponse != null && midasResponse.isSuccess()) {
          String stateSoft = midasResponse.getRecord().getState();
          LOG.debug("State : " + stateSoft);
          String sql = ExternalizedQuery.getSql("BR.AUTO.GET_VAL_FROM_STATE");
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("STATE", stateSoft);
          ReftBrSboCollector sbo = query.getSingleResult(ReftBrSboCollector.class);

          if (sbo != null) {
            details.append("Collector Number = " + sbo.getCollectorNo() + "\n");
            overrides.addOverride(getProcessCode(), "DATA", "COLLECTOR_NO", data.getCollectorNameNo(), sbo.getCollectorNo());
          }
        }
      }
    }

    if (StringUtils.isBlank(data.getGovType())) {
      data.setGovType("OU");
    }
  }

  /**
   * Calls the {@link GEOHandler} functions to save the request data to
   * auto-populate the internal fields
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @throws Exception
   */
  private void saveRequestDataUsingHandlers(EntityManager entityManager, Admin admin, Data data, Addr soldTo, Addr installAt) throws Exception {
    // before matching, do a request save to populate the missing hidden items

    LOG.debug("Saving the request before final calculations..");
    LAHandler geoHandler = new LAHandler();
    geoHandler.doBeforeAdminSave(entityManager, admin, data.getCmrIssuingCntry());
    geoHandler.setDataDefaultsOnCreate(data, entityManager);
    geoHandler.doBeforeDataSave(entityManager, admin, data, data.getCmrIssuingCntry());
    if (StringUtils.isNotBlank(data.getCustSubGrp()) && !data.getCustSubGrp().equals("CROSS")
        && (StringUtils.isBlank(data.getCrosTyp()) || StringUtils.isBlank(data.getCrosSubTyp()))) {
      geoHandler.setCustScenarioValues(entityManager, admin, data);
    }

    // Fixes for Jira defects CMR-697 and CMR-698, auto handles the contact info
    // and tax info entries for requests made using old UI.
    // Additional solution for defect CMR-745
    checkContactsForAutomationRequest(entityManager, admin, data, geoHandler);
    removeBlankTaxInfoForUpdateRequest(entityManager, admin, data, geoHandler);

    // set values for Location code (required on backend only)
    AddressService addrService = new AddressService();
    if (soldTo != null) {
      geoHandler.doBeforeAddrSave(entityManager, soldTo, data.getCmrIssuingCntry());
      addrService.assignLocationCode(entityManager, soldTo, data.getCmrIssuingCntry());
      addrService.updateDataForBRCreate(entityManager, null, soldTo);
    }
    if (installAt != null) {
      geoHandler.doBeforeAddrSave(entityManager, installAt, data.getCmrIssuingCntry());
      addrService.assignLocationCode(entityManager, installAt, data.getCmrIssuingCntry());
    }

  }

  /**
   * checks if the tax info table has blank entries and removes them
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @param geoHandler
   */

  private void removeBlankTaxInfoForUpdateRequest(EntityManager entityManager, Admin admin, Data data, LAHandler geoHandler) {
    LOG.debug("Getting tax info from V2 inputs for blank Tax code...");
    String sql = ExternalizedQuery.getSql("BR.GET_TAX_CD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    GeoTaxInfo taxInfo = query.getSingleResult(GeoTaxInfo.class);
    if (taxInfo != null) {
      if (StringUtils.isBlank(taxInfo.getTaxSeparationIndc())) {
        LOG.debug("Deleting Tax Info for Request " + admin.getId().getReqId() + " for tax seperation indc null.");
        GeoTaxInfo merged = entityManager.merge(taxInfo);
        if (merged != null) {
          entityManager.remove(merged);
        }
        entityManager.flush();
      }
    }
  }

  /**
   * 
   * Checks GeoContactInfo table for requests and creates contacts if missing.
   * 
   * @param entityManager
   * @param admin
   * @param data
   */
  private void checkContactsForAutomationRequest(EntityManager entityManager, Admin admin, Data data, LAHandler geoHandler) {

    boolean hasEM001 = false;

    LOG.debug("Getting contact info from V2 inputs..");
    String sql = ExternalizedQuery.getSql("BR.GET_DISTINCT_MAILS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    LOG.debug("Checking for existing contacts on the request, if any.");
    List<GeoContactInfo> contacts = query.getResults(GeoContactInfo.class);
    if (contacts != null && contacts.size() > 0) {
      for (GeoContactInfo contact : contacts) {
        if (contact.getContactSeqNum().equals("001")) {
          String type = contact.getContactType();
          if (type.equals("EM")) {
            LOG.debug("Found EM-001 Contact on the request.");
            hasEM001 = true;
          }
        }
      }
    }

    String email = data.getEmail1();

    if (StringUtils.isNotBlank(email)) {
      if (!hasEM001) {
        geoHandler.createNewEmailContact(entityManager, data, admin, email, "EM", "001");
        LOG.debug("Created contact EM-001 for request id: " + admin.getId().getReqId());
      }
    }
  }

  /**
   * Gets the ISU code to be assigned to the request based on the MRC
   * 
   * @param entityManager
   * @param mrc
   * @return
   */
  private String getISUCode(EntityManager entityManager, String mrc, String origIsu) {
    String prepQuery = ExternalizedQuery.getSql("IMPORT.LA.MRC.ISU");
    PreparedQuery query = new PreparedQuery(entityManager, prepQuery);
    query.setParameter("MRCCD", mrc);
    query.setParameter("CNTRY", SystemLocation.BRAZIL);
    String isu = "";
    List<String> queryResults = query.getResults(String.class);
    if (queryResults != null) {
      if (queryResults.size() == 1) {
        isu = queryResults.get(0);
      } else {
        // return original ISU if there are more than one results
        isu = origIsu;
      }
    }
    return isu;
  }

  /**
   * Computes for the Client Tier value based on MRC and ISU
   * 
   * @param entityManager
   * @param mrc
   * @param isu
   * @return
   */
  private String getClientTier(EntityManager entityManager, String mrc, String isu) {
    String prepQuery = ExternalizedQuery.getSql("IMPORT.LA.CLIENTTIER_CODE");
    PreparedQuery query = new PreparedQuery(entityManager, prepQuery);
    query.setParameter("MRCCD", mrc);
    query.setParameter("ISUCD", isu);
    String clientTier = "";
    List<String> queryResults = query.getResults(String.class);
    if (queryResults != null) {
      if (queryResults.size() == 1) {
        clientTier = queryResults.get(0);
      } else {
        // return blank if there are more than one results
        clientTier = "";
      }
    }
    return clientTier;
  }

  /**
   * Gets the legacy industry code (BRSCH) from ZZKV_SIC
   * 
   * @param entityManager
   * @param subIndustry
   * @return
   */
  private String getLegacyIndustryCd(EntityManager entityManager, String subIndustry) {
    String prepQuery = ExternalizedQuery.getSql("BR.GET_LEGACY_IND");
    PreparedQuery query = new PreparedQuery(entityManager, prepQuery);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("SUB_IND", subIndustry);
    List<String> queryResults = query.getResults(String.class);
    if (queryResults != null && !queryResults.isEmpty()) {
      return queryResults.get(0);
    } else {
      return "";
    }
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.BR_CALCULATE;
  }

  @Override
  public String getProcessDesc() {
    return "Brazil - Calculate IBM Fields";
  }

  private boolean ifStateFiscalCodeEmpty(String scenarioSubType, StringBuilder details, OverrideOutput overrides, AutomationEngineData engineData,
      Admin admin, Data data, Addr soldTo, Addr installAt, AutomationResponse<MidasResponse> midasResponse) throws Exception {
    boolean ifstateFiscalExists = true;
    if (midasResponse != null && midasResponse.isSuccess() && StringUtils.isEmpty(midasResponse.getRecord().getStateFiscalCode())) {
      ifstateFiscalExists = false;
    }
    if ("LEASI".equalsIgnoreCase(scenarioSubType)) {
      AutomationResponse<MidasResponse> responseSoldTo = null;
      if (StringUtils.isNotBlank(soldTo.getVat()) && engineData.get(soldTo.getVat()) != null) {
        responseSoldTo = (AutomationResponse<MidasResponse>) engineData.get(soldTo.getVat());
      } else {
        responseSoldTo = queryMidas(admin.getId().getReqId(), soldTo.getVat());
      }
      if (responseSoldTo != null && responseSoldTo.isSuccess() && StringUtils.isEmpty(responseSoldTo.getRecord().getStateFiscalCode())) {
        ifstateFiscalExists = (ifstateFiscalExists && false);
      }
    }

    return ifstateFiscalExists;

  }

  private boolean checkSintegraResponse(String vat, String state, AutomationResult<OverrideOutput> results, OverrideOutput overrides, String addrType,
      Addr addr, String addrTypeDesc, StringBuilder details, Data data) {
    LOG.debug("State Fiscal code not found on the request. Querying Sintgra API again.");
    AutomationResponse<SintegraResponse> response = null;
    AutomationResponse<ConsultaCCCResponse> consultaResponse = null;
    boolean ifError = false;
    String status = null;
    String stateFiscalCode = null;
    String icms = null;
    String code = null;
    try {
      if (vat != null && state != null) {
        response = BrazilUtil.querySintegra(vat, state);
        if (response.isSuccess()) {
          SintegraResponse sintegraResponse = response.getRecord();
          LOG.debug("Got Response from Sintegra...");
          if (sintegraResponse != null) {
            status = sintegraResponse.getStateFiscalCodeStatus();
            stateFiscalCode = sintegraResponse.getStateFiscalCode();
            // start
            if (status != null) {
              if ("Habilitado".equalsIgnoreCase(status) || "Ativo".equalsIgnoreCase(status) || "habilitada".equalsIgnoreCase(status)
                  || "Ativa".equalsIgnoreCase(status)) {
                if ("DF".equals(state) && sintegraResponse.getStateFiscalCodeObservation() != null
                    && "NÃO CADASTRADO COMO CONTRIBUINTE ICMS".equalsIgnoreCase(sintegraResponse.getStateFiscalCodeObservation())) {
                  icms = "1";
                } else {
                  icms = "2";
                }
                code = stateFiscalCode;
              } else {
                icms = "1";
                code = "ISENTO";
              }

              if ("ISENTO".equals(code)) {
                consultaResponse = BrazilUtil.querySintegraByConsulata(vat, state);
                if (consultaResponse.isSuccess()) {
                  ConsultaCCCResponse consulta = consultaResponse.getRecord();
                  if (consulta != null) {
                    status = consulta.getStateFiscalCodeStatus();
                    if ("Habilitado".equalsIgnoreCase(status) || "Ativo".equalsIgnoreCase(status) || "habilitada".equalsIgnoreCase(status)
                        || "Ativa".equalsIgnoreCase(status)) {
                      code = consulta.getStateFiscalCode();
                    } else {
                      code = "ISENTO";
                    }
                  }
                }
              }

              details.append(addrTypeDesc + " State Fiscal Code Status = " + status + "\n");
              if ("ZS01".equals(addrType)) {
                LOG.debug("Setting ICMS and TAX_PAYER_CUST_CD to " + icms);
                details.append("ICMS IND = " + ("1".equalsIgnoreCase(icms) ? "No" : "Yes") + "\n");
                overrides.addOverride(getProcessCode(), "DATA", "ICMS_IND", data.getIcmsInd(), icms);
                overrides.addOverride(getProcessCode(), "DATA", "TAX_PAYER_CUST_CD", data.getTaxPayerCustCd(), icms);
              }

              if (code != null) {
                String sfc = ((!StringUtils.isEmpty(code) && code.length() > 16) ? code.substring(0, 16) : code);
                details.append(addrTypeDesc + " State Fiscal Code= " + sfc + "\n");
                overrides.addOverride(getProcessCode(), addrType, "TAX_CD_1", addr.getTaxCd1(), sfc);
              }
            }
            // end
          } else {
            ifError = true;
            LOG.debug("Warning: State Fiscal Code cannot be determined from Sintegra .\n");
          }
        } else {
          consultaResponse = BrazilUtil.querySintegraByConsulata(vat, state);
          if (consultaResponse.isSuccess()) {
            ConsultaCCCResponse consulta = consultaResponse.getRecord();
            if (consulta != null) {
              status = consulta.getStateFiscalCodeStatus();
              if ("Habilitado".equalsIgnoreCase(status) || "Ativo".equalsIgnoreCase(status) || "habilitada".equalsIgnoreCase(status)
                  || "Ativa".equalsIgnoreCase(status)) {
                stateFiscalCode = consulta.getStateFiscalCode();
              } else {
                status = "ISENTO";
                stateFiscalCode = "ISENTO";
              }
            }
          } else {
            status = "ISENTO";
            stateFiscalCode = "ISENTO";
          }

          details.append(addrTypeDesc + " State Fiscal Code cannot be determined from Sintegra.Setting to ISENTO\n");
          details.append(addrTypeDesc + " State Fiscal Code Status = " + status + "\n");
          details.append(addrTypeDesc + " State Fiscal Code= " + "ISENTO" + "\n");
          overrides.addOverride(getProcessCode(), addrType, "TAX_CD_1", addr.getTaxCd1(), stateFiscalCode);
          // Fix for CMR-726
          if ("ZS01".equals(addrType)) {
            LOG.debug("Setting ICMS and TAX_PAYER_CUST_CD to 1");
            details.append("ICMS IND = " + "No" + "\n");
            overrides.addOverride(getProcessCode(), "DATA", "ICMS_IND", data.getIcmsInd(), "1");
            overrides.addOverride(getProcessCode(), "DATA", "TAX_PAYER_CUST_CD", data.getTaxPayerCustCd(), "1");
          }
        }
      }
    } catch (Exception e) {
      ifError = true;
      LOG.warn("Sintegra call still failed for CNPJ: " + addr.getVat() + ".", e);
    }
    return ifError;
  }

  private void setDefaultCollectorNo(EntityManager entityManager, OverrideOutput overrides, Data data,
      AutomationResponse<MidasResponse> midasResponse, Addr soldTo) {
    if (midasResponse != null && midasResponse.isSuccess()) {
      String state = midasResponse.getRecord().getState();
      LOG.debug("State : " + state);
      String sql = ExternalizedQuery.getSql("BR.AUTO.GET_VAL_FROM_STATE");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("STATE", state);
      ReftBrSboCollector sbo = query.getSingleResult(ReftBrSboCollector.class);
      if (sbo != null) {
        LOG.debug("Setting Default Collector No : " + sbo.getCollectorNo());
        overrides.addOverride(getProcessCode(), "DATA", "DEFLT_COLLECTOR_NO", data.getDefltCollectorNo(), sbo.getCollectorNo());
      } else {
        LOG.debug("No collector no found for state" + state);
      }
    }

  }

}
