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
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.dnb.DnbData;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

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

    long reqId = requestData.getAdmin().getId().getReqId();

    AutomationResult<ValidationOutput> result = buildResult(reqId);
    ValidationOutput validation = new ValidationOutput();
    GEOHandler handler = RequestUtils.getGEOHandler(requestData.getData().getCmrIssuingCntry());
    Admin admin = requestData.getAdmin();

    boolean ifDnBAccepted = false;
    boolean ifDnBRejected = false;
    boolean ifDnBNotRequired = false;
    boolean isSourceSysIDBlank = StringUtils.isBlank(admin.getSourceSystId()) ? true : false;

    LOG.debug("Entering China API Check Element");
    if (requestData.getAdmin().getReqType().equalsIgnoreCase("U")) {
      validation.setSuccess(true);
      validation.setMessage("Skipping China API check");
      result.setDetails("Skipping China API check for updates");
      LOG.debug("Skipping China - API check for updates");
    }

    else {
      Scorecard scorecard = requestData.getScorecard();
      if (scorecard.getFindDnbResult() != null) {
        ifDnBAccepted = scorecard.getFindDnbResult().equalsIgnoreCase(RESULT_ACCEPTED) && !StringUtils.isBlank(requestData.getData().getDunsNo());
        ifDnBRejected = scorecard.getFindDnbResult().equalsIgnoreCase(RESULT_REJECTED);
        ifDnBNotRequired = scorecard.getFindDnbResult().equalsIgnoreCase("Not Required");
      }
      if (!ifDnBAccepted && !ifDnBRejected && !ifDnBNotRequired) {
        if (admin.getMatchOverrideIndc() == null
            || (!StringUtils.isEmpty(admin.getMatchOverrideIndc()) && !admin.getMatchOverrideIndc().equalsIgnoreCase(MATCH_INDC_YES))) {
          MatchingResponse<DnBMatchingResponse> dnbMatchingResult = new MatchingResponse<DnBMatchingResponse>();
          try {
            dnbMatchingResult = DnBUtil.getMatches(requestData, engineData, "ZS01");
          } catch (Exception e) {
            LOG.debug("Error on China - API Matching" + e.getMessage());
          }
          boolean hasValidMatches = DnBUtil.hasValidMatches(dnbMatchingResult);
          if (dnbMatchingResult != null && hasValidMatches && isSourceSysIDBlank) {
            requestData.getAdmin().setMatchIndc("D");
            validation.setSuccess(false);
            validation.setMessage("Matches found");
            result.setDetails("High confidence China - API matches were found. No override from users was recorded.");
            result.setOnError(true);
            engineData.addRejectionComment("OTH", "High confidence China - API matches were found. No override from users was recorded.", "", "");
            LOG.debug("High confidence China - API matches were found. No override from user was recorded.\n");
          } else if (!hasValidMatches && isSourceSysIDBlank) {
            validation.setSuccess(true);
            validation.setMessage("Review Needed");
            result.setDetails("Processor review is required as no high confidence China - API matches were found.");

            engineData.addNegativeCheckStatus("ChinaAPICheck", "No high confidence China - API matches were found.");
            LOG.debug("Processor review is required as no high confidence China - API matches were found.");
          } else if (!isSourceSysIDBlank) {
            validation.setSuccess(true);
            validation.setMessage("Skipped");
            result.setDetails("The request was created from an external system and China - API searches is not available.");
            LOG.debug("The request was created from an external system and China - API searches is not available.");
          }
        } else if (!StringUtils.isEmpty(admin.getMatchOverrideIndc()) && admin.getMatchOverrideIndc().equalsIgnoreCase(MATCH_INDC_YES)) {
          validation.setSuccess(true);
          String message = "China - API matches were chosen to be overridden by the requester.";
          if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, reqId)) {
            validation.setMessage("Overridden");
            result.setDetails(message + "\nSupporting documentation is provided by the requester as attachment.");
          } else {
            validation.setMessage("Review required.");
            result.setDetails(message + "\nNo supporting documentation is provided by the requester.");
            engineData.addNegativeCheckStatus("ChinaAPICheck", "China - API Check matches were chosen to be overridden by the requester");
            LOG.debug("China - API matches were chosen to be overridden by the requester and needs to be reviewed");
          }

        }
      } else if (ifDnBAccepted) {

        DnbData dnb = CompanyFinder.getDnBDetails(requestData.getData().getDunsNo());
        boolean writeSuccess = true;
        if (dnb != null && dnb.getResults() != null && !dnb.getResults().isEmpty()) {
          if ("O".equals(dnb.getResults().get(0).getOperStatusCode())) {
            result.setOnError(true);
            validation.setSuccess(false);
            validation.setMessage("Failed");
            result.setDetails("Company is Out of Business based on China - API records.");
            engineData.addRejectionComment("OTH", "Company is Out of Business based on China - API records.", "", "");
            // admin.setCompVerifiedIndc(COMPANY_VERIFIED_INDC_YES);
            // admin.setCompInfoSrc("D&B");
            engineData.setCompanySource("ChinaAPI");
            LOG.debug("China - API Check record is marked as Out of Business.");
            writeSuccess = false;
          }
        }

        if (writeSuccess) {
          validation.setSuccess(true);
          validation.setMessage("DUNS Imported");
          result.setDetails("China - API Check record has been imported into the request.");
          // admin.setCompVerifiedIndc(COMPANY_VERIFIED_INDC_YES);
          // admin.setCompInfoSrc("D&B");
          engineData.setCompanySource("ChinaAPI");
          LOG.debug("China - API Check record has been imported into the request.");
        }
      } else if (ifDnBRejected || ifDnBNotRequired) {
        validation.setSuccess(true);
        validation.setMessage("Skipped");
        result.setDetails("Skipping China - API Check. Search rejected or not required.");
        LOG.debug("Skipping China - API Check as China - API search found to be rejected");
      }
    }

    result.setResults(validation.getMessage());
    result.setProcessOutput(validation);
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
