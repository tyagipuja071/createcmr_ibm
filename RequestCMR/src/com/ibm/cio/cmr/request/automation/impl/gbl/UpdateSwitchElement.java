package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.ActionOnError;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngine;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.automation.util.geo.ChinaUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;

/**
 *
 * @author Rangoli Saxena
 * @author JeffZAMORA
 *
 */
public class UpdateSwitchElement extends ValidatingElement {

  private static final Logger log = Logger.getLogger(UpdateSwitchElement.class);
  private static final List<String> NCHECK_NO_UPD_COUNTRIES = Arrays.asList(SystemLocation.UNITED_STATES);

  public UpdateSwitchElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    log.debug("Entering global Update Switch Element");
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr addr = requestData.getAddress("PG01");
    long reqId = requestData.getAdmin().getId().getReqId();
    String usRestricted = data.getRestrictInd();
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    // CREATCMR-6074
    String strRequesterId = requestData.getAdmin().getRequesterId().toLowerCase();
    boolean requesterFromTaxTeam = false;
    requesterFromTaxTeam = BluePagesHelper.isUserInUSTAXBlueGroup(strRequesterId);

    AutomationResult<ValidationOutput> output = buildResult(reqId);
    ValidationOutput validation = new ValidationOutput();

    ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
    AutomationUtil automationUtil = AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());
    log.debug(
        "Automation Util for " + data.getCmrIssuingCntry() + " = " + (automationUtil != null ? automationUtil.getClass().getSimpleName() : "none"));

    if (!"U".equals(admin.getReqType())) {
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      output.setDetails("Processing is skipped for non Update requests.");
      log.debug("Processing is skipped for non Update requests.");
    } else if ("U".equals(admin.getReqType()) && engineData.hasPositiveCheckStatus("SKIP_UPDATE_SWITCH")) {
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      output.setDetails("Processing is skipped as requester is from CMDE team.");
      log.debug("Processing is skipped as requester is from CMDE team.");
      // CREATCMR-6074
    } else if ("897".equals(data.getCmrIssuingCntry()) && "U".equals(admin.getReqType()) && requesterFromTaxTeam) {
      validation.setSuccess(true);
      validation.setMessage("Skipped");
      output.setDetails("Processing is skipped as requester is from TAX team.");
      log.debug("Processing is skipped as requester is from TAX team.");
      // CREATCMR-6074
    } else if ("U".equals(admin.getReqType())) {

      GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());

      boolean isLegalNameUpdtd = handler != null && !handler.customerNamesOnAddress() && AutomationUtil.isLegalNameChanged(admin);
      RequestChangeContainer changes = new RequestChangeContainer(entityManager, data.getCmrIssuingCntry(), admin, requestData);

      if("897".equals(data.getCmrIssuingCntry()) && "U".equals(admin.getReqType())){
    	  data.setRestrictInd(usRestricted);
      }

      if (changes.hasDataChanges() || isLegalNameUpdtd) {
        boolean hasCountryLogic = false;
        if (automationUtil != null) {
          hasCountryLogic = automationUtil.runUpdateChecksForData(entityManager, engineData, requestData, changes, output, validation);
        }

        // expected here that validations and negative checks done on the
        // util
        if (!hasCountryLogic) {
          validation.setSuccess(false);
          validation.setMessage("Not Validated");
          output.setDetails("Updates to CMR code fields need verification");
          engineData.addNegativeCheckStatus("UPDT_REVIEW_NEEDED", "Updates to CMR code fields need verification");
          engineData.addRejectionComment("OTH", "IBM/Legacy codes values changed.", "", "");
          log.debug("Updates to CMR code fields need verification");
        }

        // attachment check for china
        if (automationUtil != null && automationUtil instanceof ChinaUtil) {
          ChinaUtil chinaUtil = (ChinaUtil) automationUtil;
          String ret = chinaUtil.geDocContent(entityManager, admin.getId().getReqId());
          if (validation.isSuccess()) {
            if ("Y".equals(ret)) {
              output.setOnError(true);
              validation.setSuccess(false);
              validation.setMessage("Rejected");
              super.setStopOnError(false);
              super.setActionOnError(ActionOnError.fromCode(""));
            }
          }
        }

      } else {
        validation.setSuccess(true);
      }

      log.debug("Validation after data checks: " + validation.isSuccess());

      // handling of sbo
      if ("838".equals(data.getCmrIssuingCntry()) && changes.hasDataChanges() && null != changes.getDataChange("SBO")) {

        UpdatedDataModel updatedSBO = changes.getDataChange("SBO");
        if (StringUtils.isBlank(updatedSBO.getNewData())) {
          // check if CD is present in LOV
          if (!checkIFCodeExistInLov(entityManager, "##SalesBusOff2", data.getCmrIssuingCntry(), updatedSBO.getOldData())) {
            changes.getDataUpdates().remove(updatedSBO);
          }
        }

      }

      if (!output.isOnError() && changes.hasAddressChanges()) {        
        List<UpdatedNameAddrModel> updatedAddrList = changes.getAddressUpdates();
        String addrTypeCode = null;
        // Start CREATCMR-6229
        AppUser user = AutomationEngine.createAutomationAppUser();
        if (!changes.hasDataChanges()) {
          log.debug("1. Check for SOURCE_SYST_ID: " + admin.getSourceSystId());
          boolean paygoToBill = true;
          boolean isDPLpassed = true;
          for (UpdatedNameAddrModel updatedAddrModel : updatedAddrList) {
            addrTypeCode = updatedAddrModel.getAddrTypeCode();
            if (!"PG01".equals(addrTypeCode)) {
              paygoToBill = false;
            }
            if ("PG01".equals(addrTypeCode)) {
              if (!"P".equals(addr.getDplChkResult())) {
                isDPLpassed = false;
                log.debug("DPL failed: " + reqId);
              }
            }
          }
          if (paygoToBill && isDPLpassed) {
            if (payGoAddredited) {
              log.debug("2. Check for SOURCE_SYST_ID: " + admin.getSourceSystId());
              AutomationEngine.createComment(entityManager, "Additional PayGo billing only being added by PayGo accredited partner.", reqId, user);
              admin.setReqStatus("PCP");
              validation.setSuccess(true);
              validation.setMessage("Execution done.");
              output.setDetails("Additional PayGo billing only being added by PayGo accredited partner.");
              output.setResults(validation.getMessage());
              output.setProcessOutput(validation);
              return output;
            }
          }
        }
        // End CREATCMR-6229
        Map<String, String> addressTypes = getAddressTypes(data.getCmrIssuingCntry(), entityManager);

        /*
         * String sql = ExternalizedQuery.getSql("AUTO_UPDATE_EXT_FIND");
         * PreparedQuery query = new PreparedQuery(entityManager, sql);
         * query.setParameter("REQ_ID", reqId); query.setForReadOnly(true);
         * boolean foundWalletid = false; List<Object[]> walletIds =
         * query.getResults(); if (walletIds != null && !walletIds.isEmpty()) {
         * for (Object[] result : walletIds) { String walletId = (String)
         * result[0]; sql = ExternalizedQuery.getSql("AUTO_UPDATE_EXT_CHECK");
         * query = new PreparedQuery(entityManager, sql);
         * query.setParameter("EXT_WALLET_ID", walletId);
         * query.setParameter("REQ_ID", reqId); List<Addr> listDupExtReq =
         * query.getResults(Addr.class); if (listDupExtReq != null &&
         * listDupExtReq.size() > 0) { foundWalletid = true; break; } } } if
         * (foundWalletid) { validation.setSuccess(false);
         * validation.setMessage("Not Validated");
         * engineData.addRejectionComment("DUPADDR",
         * "Duplicate EXTWALLETID on PG01 address on request.", "", "");
         * output.setOnError(true);
         * output.setDetails("Duplicate EXTWALLETID on PG01 address on request."
         * ); log.debug("Duplicate EXTWALLETID on PG01 address on request.");
         * output.setResults(validation.getMessage());
         * output.setProcessOutput(validation); return output; }
         */

        log.debug("Addr types for skip check are: " + scenarioExceptions.getAddressTypesForSkipChecks().toString());
        boolean onlySkipAddr = false;
        if (scenarioExceptions.getAddressTypesForSkipChecks() != null && scenarioExceptions.getAddressTypesForSkipChecks().size() > 0) {
          onlySkipAddr = true;

          for (UpdatedNameAddrModel updatedAddrModel : updatedAddrList) {
            addrTypeCode = addressTypes.get(updatedAddrModel.getAddrType());
            if (StringUtils.isBlank(addrTypeCode)) {
              addrTypeCode = updatedAddrModel.getAddrType();
            }

            if (!scenarioExceptions.getAddressTypesForSkipChecks().contains(updatedAddrModel.getAddrType())) {
              onlySkipAddr = false;
              break;
            }
          }
        }

        if (onlySkipAddr) {
          engineData.setSkipChecks();
          output.setDetails("Updates found on non-relevant addresses only.");
          log.debug("Name/address changes made on non-critical addresses only. No processor review is needed.");
          validation.setSuccess(true);
          validation.setMessage("No Validations");
        } else {
          log.debug("Verifying PayGo Accreditation for " + admin.getSourceSystId());
          // boolean payGoAddredited =
          // AutomationUtil.isPayGoAccredited(entityManager,
          // admin.getSourceSystId());
          // do an initial check for PayGo cmrs
          // if (payGoAddredited && "PG".equals(data.getOrdBlk())) {
          // log.debug(
          // "Allowing name/address changes for PayGo accredited partner " +
          // admin.getSourceSystId() + " Request: " + admin.getId().getReqId());
          // String details = StringUtils.isNotBlank(output.getDetails()) ?
          // output.getDetails() : "";
          // output.setDetails("Skipped address checks on updates to PayGo CMR.
          // Partner accredited for PayGo.\n");
          // validation.setMessage("Validated");
          // validation.setSuccess(true);
          // output.setResults("Validated");
          // output.setProcessOutput(validation);
          // return output;
          // }
          boolean hasCountryLogic = false;
          if (automationUtil != null) {
            hasCountryLogic = automationUtil.runUpdateChecksForAddress(entityManager, engineData, requestData, changes, output, validation);
          }

          // expected here that validations and negative checks done on the
          // util
          if (!hasCountryLogic) {
            if (!"Y".equalsIgnoreCase(admin.getCompVerifiedIndc())) {
              engineData.addNegativeCheckStatus("UPDATE_CHECK_FAIL", "Updates to relevant addresses need verification.");
              output.setDetails("Updates to relevant addresses need verification.");
              log.debug("Name/address changes made on critical addresses. Hence sending back to the processor for review.");
              validation.setSuccess(false);
              validation.setMessage("Not Validated");
            } else {
              output.setDetails("Updates to relevant addresses found but have been marked as Verified.");
              log.debug("Name/address changes made on critical addresses but company is verified. Hence no processor review needed.");
              validation.setSuccess(true);
              validation.setMessage("Validated");
            }
          }

        }

      } else if (NCHECK_NO_UPD_COUNTRIES.contains(data.getCmrIssuingCntry()) && !changes.hasDataChanges() && !changes.hasAddressChanges()
          && !AutomationUtil.isLegalNameChanged(admin)) {
        // Set negative check if country is part of the list and there are no
        // updates/changes at all on the request
        validation.setSuccess(true);
        validation.setMessage("Review Required");
        output.setDetails("No data/address changes made on request.");
        engineData.addNegativeCheckStatus("NO_UPD", "No data/address changes made on request.");
        log.debug("No data/address changes made on request.");
      } else if (!changes.hasDataChanges() && !changes.hasAddressChanges() && isLegalNameUpdtd) {
        validation.setSuccess(true);
        validation.setMessage("Review Required");
        String details = output.getDetails() + "\n" + "Legal Name changes made on request.";
        output.setDetails(details);
        log.debug("Legal Name changes made on request.");
      } else if (!changes.hasDataChanges() && !changes.hasAddressChanges()) {
        // no updates/changes at all on the request
        validation.setSuccess(false);
        validation.setMessage("Not Validated");
        engineData.addRejectionComment("NOUPD", "No data/address changes made on request.", "", "");
        output.setOnError(true);
        output.setDetails("No data/address changes made on request.");
        log.debug("No data/address changes made on request.");
      } else if (validation.isSuccess()) {
        validation.setSuccess(true);
        validation.setMessage("Execution done.");
        if (StringUtils.isEmpty(output.getDetails())) {
          output.setDetails("Update checks performed successfully");
        }
        log.debug("Update checks performed successfully");
      }
    }
    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;
  }

  /**
   * Does a reverse mapping of codes to types
   *
   * @param cntry
   * @param entityManager
   * @return
   */
  private Map<String, String> getAddressTypes(String cntry, EntityManager entityManager) {
    HashMap<String, String> map = new HashMap<>();
    String sql = ExternalizedQuery.getSql("SUMMARY.GET_ADDRESS_TYPES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    List<Object[]> results = query.getResults();
    if (results != null) {
      for (Object[] result : results) {
        map.put((String) result[1], (String) result[0]);
      }
    }
    return map;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_UPDATE_SWITCH;
  }

  @Override
  public String getProcessDesc() {
    return "Global - Update Switch";
  }

  /**
   * Checks absolute equality between the strings
   *
   * @param val1
   * @param val2
   * @return
   */
  public boolean equals(String val1, String val2) {
    if (val1 == null && val2 != null) {
      return StringUtils.isEmpty(val2.trim());
    }
    if (val1 != null && val2 == null) {
      return StringUtils.isEmpty(val1.trim());
    }
    if (val1 == null && val2 == null) {
      return true;
    }
    return val1.trim().equals(val2.trim());
  }

  public boolean checkIFCodeExistInLov(EntityManager entityManager, String fieldId, String cmrIssuingCntry, String code) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.LOVMAINT");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("FIELD_ID", fieldId);
    q.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    q.setParameter("CD", code);
    return q.exists();
  }
}
