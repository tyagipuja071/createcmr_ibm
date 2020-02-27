package com.ibm.cio.cmr.request.automation.impl.gbl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.MatchingOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.entity.SuppCntryPK;

public class DnBCheckElement extends ValidatingElement {

  private static final Logger LOG = Logger.getLogger(DnBCheckElement.class);

  private static final String COMPANY_VERIFIED_INDC_YES = "Y";
  public static final String RESULT_ACCEPTED = "Accepted";
  public static final String MATCH_INDC_YES = "Y";

  public DnBCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    long reqId = requestData.getAdmin().getId().getReqId();

    AutomationResult<ValidationOutput> result = buildResult(reqId);
    ValidationOutput validation = new ValidationOutput();
    Admin admin = requestData.getAdmin();
    SuppCntryPK suppPk = new SuppCntryPK();
    suppPk.setCntryCd(requestData.getData().getCmrIssuingCntry());
    SuppCntry cntry = entityManager.find(SuppCntry.class, suppPk);

    LOG.debug("Entering DnB Check Element");
    if (requestData.getAdmin().getReqType().equalsIgnoreCase("U")) {
      validation.setSuccess(true);
      validation.setMessage("Skipping DnB check");
      result.setDetails("Skipping DnB check for updates");
      LOG.debug("Skipping DnB check for updates");
    } else if (admin.getCompVerifiedIndc() != null && admin.getCompVerifiedIndc().equalsIgnoreCase(COMPANY_VERIFIED_INDC_YES)) {
      validation.setSuccess(true);
      validation.setMessage("Skipping DnB check");
      result.setDetails("DnB check is skipped because company information has been marked as verified.");
      LOG.debug("DnB check is skipped because company information has been marked as verified.");
    } else if (StringUtils.isEmpty(admin.getCompVerifiedIndc())
        || (admin.getCompVerifiedIndc() != null && !admin.getCompVerifiedIndc().equalsIgnoreCase(COMPANY_VERIFIED_INDC_YES))) {
      if (cntry.getDnbPrimaryIndc() != null && cntry.getDnbPrimaryIndc().equalsIgnoreCase("Y")) {
        Scorecard scorecard = requestData.getScorecard();
        if ((scorecard.getFindDnbResult() != null && !scorecard.getFindDnbResult().equalsIgnoreCase(RESULT_ACCEPTED))
            || StringUtils.isBlank(requestData.getData().getDunsNo())) {
          if (admin.getMatchOverrideIndc() == null
              || (!StringUtils.isEmpty(admin.getMatchOverrideIndc()) && !admin.getMatchOverrideIndc().equalsIgnoreCase(MATCH_INDC_YES))) {
            AutomationResult<MatchingOutput> dnbMatchingResult = new AutomationResult<MatchingOutput>();
            DnBMatchingElement dnbMatchingElement = new DnBMatchingElement(admin.getReqType(), null, false, false);
            try {
              dnbMatchingResult = dnbMatchingElement.executeElement(entityManager, requestData, engineData);
            } catch (Exception e) {
              LOG.debug("Error on DNB Matching" + e.getMessage());
            }
            if (dnbMatchingResult != null && !dnbMatchingResult.isOnError()) {
              requestData.getAdmin().setMatchIndc("D");
              validation.setSuccess(false);
              validation.setMessage("Matches found");
              result.setDetails("High confidence D&B matches were found. No override from users was recorded.");
              result.setOnError(true);
              engineData.addRejectionComment("High confidence D&B matches were found. No override from users was recorded.");
              LOG.debug("High confidence D&B matches were found. No override from user was recorded.\n" + dnbMatchingResult.getDetails());
            } else if (dnbMatchingResult != null && dnbMatchingResult.isOnError()) {
              validation.setSuccess(true);
              validation.setMessage("Review Needed");
              result.setDetails("Processor review is required as no high confidence D&B matches were found.");
              engineData.addNegativeCheckStatus("DNBCheck", "Processor review is required as no high confidence D&B matches were found.");
              LOG.debug("Processor review is required as no high confidence D&B matches were found.");
            }
          } else if (!StringUtils.isEmpty(admin.getMatchOverrideIndc()) && admin.getMatchOverrideIndc().equalsIgnoreCase(MATCH_INDC_YES)) {
            validation.setSuccess(true);
            validation.setMessage("Review Needed");
            result.setDetails("D&B matches were chosen to be overridden by the requester and needs to be reviewed");
            engineData.addNegativeCheckStatus("DNBCheck", "D&B matches were chosen to be overridden by the requester and needs to be reviewed");
            LOG.debug("D&B matches were chosen to be overridden by the requester and needs to be reviewed");
          }
        } else if (scorecard.getFindDnbResult() != null && scorecard.getFindDnbResult().equalsIgnoreCase(RESULT_ACCEPTED)
            && !StringUtils.isBlank(requestData.getData().getDunsNo())) {
          validation.setSuccess(true);
          validation.setMessage("Duns Imported");
          result.setDetails("DUNS record has been imported into the request.");
          admin.setCompVerifiedIndc(COMPANY_VERIFIED_INDC_YES);
          admin.setCompInfoSrc("D&B");
          LOG.debug("DUNS record has been imported into the request.");
        }
      } else if (StringUtils.isEmpty(cntry.getDnbPrimaryIndc())
          || (cntry.getDnbPrimaryIndc() != null && !cntry.getDnbPrimaryIndc().equalsIgnoreCase("Y"))) {
        validation.setSuccess(true);
        validation.setMessage("Skip DnB check");
        result.setDetails("Skipping DnB check as D&B is not primary source for the country.");
        LOG.debug("Skipping DnB check as D&B is not primary source for the country.");
      }
    } else {
      validation.setSuccess(true);
      validation.setMessage("D&B check Done");
      result.setDetails("D&B check Completed");
      LOG.debug("D&B check Completed successfully.");
    }

    result.setResults(validation.getMessage());
    result.setProcessOutput(validation);
    return result;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_DNB_CHECK;
  }

  @Override
  public String getProcessDesc() {

    return "Global- D&B Check Element";
  }

}
