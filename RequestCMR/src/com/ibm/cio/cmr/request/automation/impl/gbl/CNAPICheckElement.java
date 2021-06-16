package com.ibm.cio.cmr.request.automation.impl.gbl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.CompanyVerifier;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.cn.CNResponse;

public class CNAPICheckElement extends ValidatingElement implements CompanyVerifier {

  private static final Logger LOG = Logger.getLogger(CNAPICheckElement.class);

  // private static final String COMPANY_VERIFIED_INDC_YES = "Y";
  public static final String RESULT_ACCEPTED = "Accepted";
  public static final String MATCH_INDC_YES = "Y";
  public static final String RESULT_REJECTED = "Rejected";

  public CNAPICheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Addr soldTo = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    // ScenarioExceptionsUtil scenarioExceptions =
    // getScenarioExceptions(entityManager, requestData, engineData);
    // AutomationUtil countryUtil =
    // AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());

    AutomationResult<ValidationOutput> result = buildResult(admin.getId().getReqId());
    // boolean matchDepartment = false;
    // if (engineData.get(AutomationEngineData.MATCH_DEPARTMENT) != null) {
    // matchDepartment = (boolean)
    // engineData.get(AutomationEngineData.MATCH_DEPARTMENT);
    // }

    ValidationOutput output = new ValidationOutput();

    // if (!scenarioExceptions.isSkipDuplicateChecks()) {
    if (StringUtils.isNotBlank(admin.getDupCmrReason())) {
      StringBuilder details = new StringBuilder();
      details.append("User requested to proceed with Duplicate CMR Creation.").append("\n\n");
      details.append("Reason provided - ").append("\n");
      details.append(admin.getDupCmrReason()).append("\n");
      result.setDetails(details.toString());
      result.setResults("Overridden");
      result.setOnError(false);
    } else if (soldTo != null) {

      String SCENARIO_LOCAL_NRML = "NRML";
      String SCENARIO_LOCAL_EMBSA = "EMBSA";
      String SCENARIO_CROSS_CROSS = "CROSS";
      String SCENARIO_LOCAL_AQSTN = "AQSTN";
      String SCENARIO_LOCAL_BLUMX = "BLUMX";
      String SCENARIO_LOCAL_MRKT = "MRKT";
      String SCENARIO_LOCAL_BUSPR = "BUSPR";
      String SCENARIO_LOCAL_INTER = "INTER";
      String SCENARIO_LOCAL_PRIV = "PRIV";
      boolean ifAQSTNHasCN = true;
      if (data.getCustSubGrp() != null && (SCENARIO_LOCAL_NRML.equals(data.getCustSubGrp()) || SCENARIO_LOCAL_EMBSA.equals(data.getCustSubGrp())
          || SCENARIO_LOCAL_AQSTN.equals(data.getCustSubGrp()) || SCENARIO_LOCAL_BLUMX.equals(data.getCustSubGrp())
          || SCENARIO_LOCAL_MRKT.equals(data.getCustSubGrp()) || SCENARIO_LOCAL_BUSPR.equals(data.getCustSubGrp())
          || SCENARIO_LOCAL_INTER.equals(data.getCustSubGrp()))) {
        CompanyRecordModel searchModel = new CompanyRecordModel();
        searchModel.setIssuingCntry(data.getCmrIssuingCntry());
        searchModel.setCountryCd(soldTo.getLandCntry());
        if (StringUtils.isNotEmpty(data.getTaxCd1())) {
          searchModel.setTaxCd1(data.getTaxCd1());
        } else {
          GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
          IntlAddr iAddr = new IntlAddr();
          // CompanyRecordModel cmrData = null;
          iAddr = handler.getIntlAddrById(soldTo, entityManager);
          // searchModel.setCmrNo(cmrNo);
          if (iAddr != null) {

            searchModel.setTaxCd1(iAddr.getIntlCustNm1() + " " + (iAddr.getIntlCustNm2() != null ? iAddr.getIntlCustNm2() : ""));
          }
        }
        try {
          AutomationResponse<CNResponse> cmrsData = CompanyFinder.getCNApiInfo(searchModel);
          if (cmrsData != null && cmrsData.isSuccess()) {
            // cmrData = cmrsData.get(0);
            result.setResults("Matches Found");
            StringBuilder details = new StringBuilder();
            result.setResults("Found China API Data.");
            // engineData.addRejectionComment("DUPC", "Customer already exists /
            // duplicate CMR", "", "");
            // to allow overides later
            // requestData.getAdmin().setMatchIndc("C");
            result.setOnError(false);
            details.append("\n");
            // logDuplicateCMR(details, cmrData);
            result.setProcessOutput(output);
            result.setDetails(details.toString().trim());
          } else {
            result.setDetails("No China API Data were found.");
            result.setResults("No Matches");
            engineData.addRejectionComment("NOCN", "No China API Data were found.", "", "");
            result.setOnError(true);
          }
        } catch (Exception e) {
          e.printStackTrace();
          result.setDetails("Error on get China API Data Check.");
          engineData.addRejectionComment("OTH", "Error on  get China API Data Check.", "", "");
          result.setOnError(true);
          result.setResults("Error on  get China API Data Check.");
        }

      }
    } else {
      result.setDetails("Missing main address on the request.");
      engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
      result.setResults("No Matches");
      result.setOnError(true);
    }
    // } else {
    // result.setDetails("Skipping Duplicate CMR checks for scenario");
    // log.debug("Skipping Duplicate CMR checks for scenario");
    // result.setResults("Skipped");
    // result.setOnError(false);
    // }
    return result;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.CN_API_CHECK;
  }

  @Override
  public String getProcessDesc() {

    return "China - API Check Element";
  }

}
