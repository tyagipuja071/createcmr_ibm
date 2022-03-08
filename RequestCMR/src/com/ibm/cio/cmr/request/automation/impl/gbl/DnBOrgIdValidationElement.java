/**
 *
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.CompanyVerifier;
import com.ibm.cio.cmr.request.automation.ProcessType;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.dnb.DnbOrganizationId;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

/**
 * {@link AutomationElement} implementation for the D&B Org Id Validation
 *
 * @author RoopakChugh
 *
 */
public class DnBOrgIdValidationElement extends ValidatingElement implements CompanyVerifier {

  private static final Logger LOG = Logger.getLogger(DnBOrgIdValidationElement.class);

  public DnBOrgIdValidationElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    Addr soldTo = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    AutomationResult<ValidationOutput> result = buildResult(admin.getId().getReqId());
    ValidationOutput output = new ValidationOutput();

    if ("U".equals(admin.getReqType()) && engineData.hasPositiveCheckStatus(AutomationEngineData.SKIP_DNB_ORGID_VAL)) {
      output.setSuccess(true);
      output.setMessage("Skipped");
      result.setDetails("Processing is skipped as DnB validation NOT DONE or FAILED.");
      result.setResults("Skipped");
      result.setProcessOutput(output);
      LOG.debug("Processing is skipped as DnB validation NOT DONE or FAILED.");
      return result;
    }
    if ("Y".equals(admin.getMatchOverrideIndc()) && DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
      result.setResults("Overriden");
      output.setSuccess(true);
      output.setMessage("Overriden");
      result.setProcessOutput(output);
      result.setDetails(
          "D&B matches were chosen to be overridden by the requester.\nSupporting documentation is provided by the requester as attachment.");
      List<String> dnbOverrideCountryList = SystemParameters.getList("DNB_OVR_CNTRY_LIST");
      if (dnbOverrideCountryList == null || !dnbOverrideCountryList.contains(data.getCmrIssuingCntry())) {
        engineData.addNegativeCheckStatus("_dnbOverride", "D&B matches were chosen to be overridden by the requester.");
      }
      return result;
    }
    if (StringUtils.isBlank(data.getTaxCd1()) && (!SystemLocation.NETHERLANDS.equals(data.getCmrIssuingCntry()))) {
      result.setResults("OrgID not found");
      result.setDetails("Org ID was not provided on the request.");
      output.setSuccess(false);
      output.setMessage("OrgID not found");
      result.setProcessOutput(output);
      // engineData.addNegativeCheckStatus("_orgIdMissing", "Org ID was not
      // provided on the request.");
      result.setOnError(false);
      return result;
    }

    if (StringUtils.isBlank(data.getTaxCd2()) && (SystemLocation.NETHERLANDS.equals(data.getCmrIssuingCntry()))) {
      result.setResults("OrgID not found");
      result.setDetails("Org ID was not provided on the request.");
      output.setSuccess(false);
      output.setMessage("OrgID not found");
      result.setProcessOutput(output);
      result.setOnError(false);
      return result;
    }
    if (soldTo != null) {
      boolean shouldThrowError = !"Y".equals(admin.getCompVerifiedIndc()) && StringUtils.isBlank(admin.getSourceSystId());
      if ("U".equalsIgnoreCase(admin.getReqType())) {
        shouldThrowError = false;
      }
      boolean hasValidMatches = false;
      List<DnBMatchingResponse> dnbMatches = new ArrayList<DnBMatchingResponse>();
      DnBMatchingResponse dnbMatch = (DnBMatchingResponse) engineData.get("dnbMatching");
      if (dnbMatch != null) {
        dnbMatches.add(dnbMatch);
      }
      if (dnbMatch == null) {
        MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, engineData, "ZS01");
        hasValidMatches = DnBUtil.hasValidMatches(response);
        if (response != null && response.getMatched()) {
          dnbMatches = response.getMatches();
          if (!hasValidMatches) {
            // if no valid matches - do not process records
            result.setResults("No Matches");
            result.setDetails("No high quality matches with D&B records. Please import from D&B search.");
            engineData.addNegativeCheckStatus("DnBMatch", "No high quality matches with D&B records. Please import from D&B search.");
            dnbMatches = new ArrayList<DnBMatchingResponse>();
          }
        }
      }
      if (!dnbMatches.isEmpty()) {
        // actions to be performed only when matches with high confidence are
        // found
        boolean isOrgIdMatched = false;
        StringBuilder details = new StringBuilder();

        // process records and overrides
        DnBMatchingResponse highestCloseMatch = null;

        for (DnBMatchingResponse dnbRecord : dnbMatches) {
          if (dnbRecord.getConfidenceCode() > 7 && DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), soldTo, admin, dnbRecord)) {
            String dnbOrgId = DnBUtil.getTaxCode1(dnbRecord.getDnbCountry(), dnbRecord.getOrgIdDetails());
            if (SystemLocation.NETHERLANDS.equals(data.getCmrIssuingCntry())) {
              if (data.getTaxCd2().equals(dnbOrgId)) {
                highestCloseMatch = dnbRecord;
                isOrgIdMatched = true;
                break;
              }
            } else if(data.getTaxCd1().equals(dnbOrgId)) {
              highestCloseMatch = dnbRecord;
              isOrgIdMatched = true;
              break;
            }
          }
        }
        if (isOrgIdMatched) {
          LOG.debug("Org ID validated successfully");
          result.setResults("Org ID validated");
          details.append("Org ID validated successfully with high confidence DnB match "
              + (highestCloseMatch != null ? "[Duns-" + highestCloseMatch.getDunsNo() + "]" : ""));
          output.setSuccess(true);
          output.setMessage("Org ID validated");
          processDnBFields(entityManager, data, highestCloseMatch, details);
          LOG.trace(new ObjectMapper().writeValueAsString(highestCloseMatch));
        } else {
          LOG.debug("Org ID not validated");
          result.setResults("Org ID not validated");
          details.append("Org ID value did not match with the highest confidence D&B match.");
          output.setSuccess(false);
          output.setMessage("Org ID not validated");
          processDnBFields(entityManager, data, dnbMatches.get(0), details);

          if (SystemLocation.NETHERLANDS.equals(data.getCmrIssuingCntry())) {
            result.setOnError(false);
            engineData.addNegativeCheckStatus("_orgIdMatchNotFound", "Org ID value did not match with the highest confidence D&B match.");
          } else {
            result.setOnError(shouldThrowError);
            engineData.addRejectionComment("_orgIdMatchNotFound", "Org ID value did not match with the highest confidence D&B match.", "", "");
          }
          LOG.trace(new ObjectMapper().writeValueAsString(highestCloseMatch));
        }
        result.setDetails(details.toString().trim());
        result.setProcessOutput(output);
      } else {
        result.setDetails("No D&B record was found using advanced matching.");
        engineData.addNegativeCheckStatus("_nodnbmatchOrg", "No matches with D&B records. Please import from D&B search.");
        output.setSuccess(false);
        output.setMessage("No D&B match found");
        result.setProcessOutput(output);
        result.setResults("No Matches");
      }

    } else {
      result.setDetails("Missing main address on the request.");
      engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
      output.setSuccess(false);
      output.setMessage("No main address found");
      result.setProcessOutput(output);
      result.setResults("No Matches");
      result.setOnError(true);
    }
    return result;
  }

  /**
   *
   * Processes DnB fields for a particular Dnb Record, creates details for
   * results and matching records for importing Dnb data to the request.
   *
   * @param entityManager
   * @param data
   * @param dnbRecord
   * @param output
   * @param details
   * @param itemNo
   * @throws Exception
   */
  private void processDnBFields(EntityManager entityManager, Data data, DnBMatchingResponse dnbRecord, StringBuilder details) throws Exception {
    details.append("\n");

    LOG.debug("High Confidence Match found via D&B matching..");

    details.append("DUNS No. = " + dnbRecord.getDunsNo()).append("\n");
    details.append("Confidence Code = " + dnbRecord.getConfidenceCode()).append("\n");
    details.append("Company Name =  " + dnbRecord.getDnbName()).append("\n");
    details.append("Address =  " + dnbRecord.getDnbStreetLine1()).append("\n");
    if (!StringUtils.isBlank(dnbRecord.getDnbStreetLine2())) {
      details.append("Address (cont)=  " + dnbRecord.getDnbStreetLine2()).append("\n");
    }
    if (!StringUtils.isBlank(dnbRecord.getDnbCity())) {
      details.append("City =  " + dnbRecord.getDnbCity()).append("\n");
    }
    if (!StringUtils.isBlank(dnbRecord.getDnbStateProv())) {
      details.append("State =  " + dnbRecord.getDnbStateProv()).append("\n");
    }
    if (!StringUtils.isBlank(dnbRecord.getDnbPostalCode())) {
      details.append("Postal Code =  " + dnbRecord.getDnbPostalCode()).append("\n");
    }
    if (!StringUtils.isBlank(dnbRecord.getDnbCountry())) {
      details.append("Country =  " + dnbRecord.getDnbCountry()).append("\n");
    }
    String orgIdMatch = "Y".equals(dnbRecord.getOrgIdMatch()) ? "Matched" : ("N".equals(dnbRecord.getOrgIdMatch()) ? "Not Matched" : "Not Done");
    details.append("Org ID Matching =  " + orgIdMatch).append("\n");

    List<DnbOrganizationId> orgIDDetails = dnbRecord.getOrgIdDetails();

    details.append("Organization IDs:");
    boolean relevantOrgId = false;
    for (int i = 0; i < orgIDDetails.size(); i++) {
      DnbOrganizationId orgId = orgIDDetails.get(i);
      if (DnBUtil.isRelevant(dnbRecord.getDnbCountry(), orgId)) {
        details.append("\n - " + orgId.getOrganizationIdType() + " = " + orgId.getOrganizationIdCode());
        relevantOrgId = true;
      }
    }

    if (!relevantOrgId) {
      details.append("(No relevant Org Id found)\n");
    } else {
      details.append("\n");
    }

    LOG.debug("Connecting to D&B details service..");
    DnBCompany dnbData = DnBUtil.getDnBDetails(dnbRecord.getDunsNo());
    if (dnbData != null) {

      if (!StringUtils.isBlank(dnbData.getPrimaryCounty())) {
        details.append("County =  " + dnbData.getPrimaryCounty()).append("\n");
      }

      details.append("ISIC =  " + dnbData.getIbmIsic() + " (" + dnbData.getIbmIsicDesc() + ")").append("\n");
      String subInd = RequestUtils.getSubIndustryCd(entityManager, dnbData.getIbmIsic(), data.getCmrIssuingCntry());
      if (subInd != null) {
        details.append("Subindustry Code  =  " + subInd).append("\n");
      }
    }
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_DNB_ORGID;
  }

  @Override
  public String getProcessDesc() {
    return "D&B Org ID Validation";
  }

  @Override
  public ProcessType getProcessType() {
    return ProcessType.Validation;
  }

}
