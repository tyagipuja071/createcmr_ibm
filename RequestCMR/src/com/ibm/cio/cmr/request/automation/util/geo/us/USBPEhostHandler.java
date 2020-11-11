package com.ibm.cio.cmr.request.automation.util.geo.us;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

public class USBPEhostHandler extends USBPHandler {

  private static final Logger LOG = Logger.getLogger(USBPEhostHandler.class);

  private String cmrType;
  private static final String T1 = "T1";
  private static final String T2 = "T2";

  @Override
  public boolean doInitialValidations(Admin admin, Data data, Addr addr, AutomationResult<OverrideOutput> output, AutomationEngineData engineData) {

    if (StringUtils.isNotBlank(data.getSubIndustryCd()) && data.getSubIndustryCd().startsWith("Y")) {
      output.setResults("Skipped");
      output.setDetails("Federal Pool request detected. Request will be redirected to CMDE queue for review.");
      engineData.addNegativeCheckStatus("_federalPool", "Federal Pool request detected. Further review required.");
      return true;
    }

    if (StringUtils.isNotBlank(data.getEnterprise())) {
      USCeIdMapping mapping = USCeIdMapping.getByEnterprise(data.getEnterprise());
      if (mapping == null || !mapping.isDistributor()) {
        output.setResults("Non-Distributor BP");
        output.setDetails(
            "BP E-Hosting records are only allowed to be created under Distributors, please check and confirm with the Distributor for this transaction.");
        engineData.addRejectionComment("ENT",
            "BP E-Hosting records are only allowed to be created under Distributors, please check and confirm with the Distributor for this transaction.",
            "", "");
        output.setOnError(true);
        return true;
      }
    } else {
      output.setResults("Invalid Enterprise");
      output.setDetails(
          "BP E-Hosting records are only allowed to be created under Distributors, please check and confirm with the Distributor for this transaction.");
      engineData.addRejectionComment("ENT",
          "BP E-Hosting records are only allowed to be created under Distributors, please check and confirm with the Distributor for this transaction.",
          "", "");
      output.setOnError(true);
      return true;
    }

    if (StringUtils.isNotBlank(data.getPpsceid()) && AutomationUtil.checkPPSCEID(data.getPpsceid())) {
      output.setResults("Invalid CEID");
      output.setDetails("Only BP with valid CEID is allowed to setup a Pool record, please check and confirm.");
      engineData.addRejectionComment("CEID", "Only BP with valid CEID is allowed to setup a Pool record, please check and confirm.", "", "");
      output.setOnError(true);
      return true;
    }
    return false;

  }

  @Override
  public boolean processRequest(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<OverrideOutput> output, StringBuilder details, boolean childCompleted, FindCMRRecordModel ibmCmr, RequestData childRequest,
      GEOHandler handler, OverrideOutput overrides) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    long childReqId = admin.getChildReqId();
    Addr zs01 = requestData.getAddress("ZS01");
    String mainCustNm = admin.getMainCustNm1() + (StringUtils.isNotBlank(admin.getMainCustNm2()) ? " " + admin.getMainCustNm2() : "");
    String endUserNm = (StringUtils.isNotBlank(zs01.getDivn()) ? zs01.getDivn() : "")
        + (StringUtils.isNotBlank(zs01.getDept()) ? " " + zs01.getDept() : "");
    if (data.getEnterprise().equals(data.getAffiliate()) && mainCustNm.equals(endUserNm)) {
      this.cmrType = T1;
    } else {
      this.cmrType = T2;
    }

    String childCmrNo = null;

    if (childRequest != null) {
      childCmrNo = childRequest.getData().getCmrNo();
    }
    // prioritize child request here
    if (childCompleted) {
      details.append("Copying CMR values direct CMR " + childCmrNo + " from Child Request " + childReqId + ".\n");
      ibmCmr = createIBMCMRFromChild(childRequest);
    } else {
      // check IBM Direct CMR
      if (ibmCmr == null) {
        // if a rejected child caused the retrieval of a child cmr
        ibmCmr = findIBMCMR(entityManager, handler, requestData, zs01, engineData, childCmrNo);
      }
      if (ibmCmr != null) {
        details.append("Copying CMR values CMR " + ibmCmr.getCmrNum() + " from FindCMR.\n");
        LOG.debug("IBM Direct CMR Found: " + ibmCmr.getCmrNum() + " - " + ibmCmr.getCmrName());
      }
    }

    // match against D&B
    DnBMatchingResponse dnbMatch = matchAgainstDnB(handler, requestData, zs01, engineData, details, overrides, ibmCmr != null);

    if (ibmCmr == null) {

      LOG.debug("No IBM Pool CMR for the end user.");
      childReqId = createChildRequest(entityManager, requestData, engineData);
      details.append("No IBM Pool CMR for the end user.\n");

      if (childCompleted) {
        String childError = "Child Request " + childReqId + " was completed but Pool CMR cannot be determined. Manual validation needed.";
        details.append(childError + "\n");
        engineData.addNegativeCheckStatus("_usChildError", childError);
        output.setDetails(details.toString());
        output.setResults("Issues Encountered");
        return false;
      } else {
        String childErrorMsg = "- IBM Pool CMR request creation cannot be done, errors were encountered -";
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
            details.append("Child Request " + childReqId + " created for the Pool CMR record of " + zs01.getDivn()
                + (!StringUtils.isBlank(zs01.getDept()) ? " " + zs01.getDept() : "")
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
  public void copyAndFillIBMData(EntityManager entityManager, GEOHandler handler, FindCMRRecordModel ibmCmr, RequestData requestData,
      AutomationEngineData engineData, StringBuilder details, OverrideOutput overrides, RequestData childRequest) {
    // TODO Auto-generated method stub

  }

  @Override
  public void doFinalValidations(AutomationEngineData engineData, RequestData requestData, StringBuilder details, OverrideOutput overrides,
      AutomationResult<OverrideOutput> result, FindCMRRecordModel ibmCmr) {
    // TODO Auto-generated method stub

  }

  @Override
  protected FindCMRRecordModel getIBMCMRBestMatch(AutomationEngineData engineData, RequestData requestData, List<DuplicateCMRCheckResponse> matches)
      throws CmrException {

    for (DuplicateCMRCheckResponse record : matches) {
      LOG.debug(" - Duplicate: (Restrict To: " + record.getUsRestrictTo() + ", Grade: " + record.getMatchGrade() + ")" + record.getCompany() + " - "
          + record.getCmrNo() + " - " + record.getAddrType() + " - " + record.getStreetLine1());
      // IBM Direct CMRs have blank restrict to
      if (T1.equals(cmrType)) {
        if (RESTRICT_TO_END_USER.equals(record.getUsRestrictTo()) && "P".equals(record.getUsBpAccType())) {
          LOG.debug("CMR No. " + record.getCmrNo() + " matches Pool CMR Criteria. Getting CMR Details..");
          String overrides = "addressType=ZS01&cmrOwner=IBM&showCmrType=R&customerNumber=" + record.getCmrNo();
          FindCMRResultModel result = CompanyFinder.getCMRDetails(SystemLocation.UNITED_STATES, record.getCmrNo(), 5, null, overrides);
          if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
            return result.getItems().get(0);
          }
        } else {
          LOG.debug("CMR No. " + record.getCmrNo() + " does not meet the Pool CMR criteria");
        }
      } else if (T2.equals(cmrType)) {
        if (RESTRICT_TO_END_USER.equals(record.getUsRestrictTo()) && "TT2".equals(record.getUsCsoSite())) {
          LOG.debug("CMR No. " + record.getCmrNo() + " matches T2 Pool CMR criteria. Getting CMR Details..");
          String overrides = "addressType=ZS01&cmrOwner=IBM&showCmrType=R&customerNumber=" + record.getCmrNo();
          FindCMRResultModel result = CompanyFinder.getCMRDetails(SystemLocation.UNITED_STATES, record.getCmrNo(), 5, null, overrides);
          if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
            return result.getItems().get(0);
          }
        } else {
          LOG.debug("CMR No. " + record.getCmrNo() + " does not meet the T2 Pool CMR criteria");
        }
      }
    }
    return null;

  }

  @Override
  protected void setChildRequestScenario(Data data, Data childData, Admin childAdmin, StringBuilder details) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isChildRequestSupported() {
    return true;
  }

}
