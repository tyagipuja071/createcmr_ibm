/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.dpl.DPLCheckResult;
import com.ibm.comexp.at.exportchecks.ews.EWSProperties;

/**
 * {@link AutomationElement} implementation for DPL check on addresses
 * 
 * @author JeffZAMORA
 * 
 */
public class DPLCheckElement extends ValidatingElement {

  private static final Logger log = Logger.getLogger(DPLCheckElement.class);

  public DPLCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    AppUser user = (AppUser) engineData.get("appUser");

    long reqId = requestData.getAdmin().getId().getReqId();

    AutomationResult<ValidationOutput> output = buildResult(reqId);
    ValidationOutput validation = new ValidationOutput();
    StringBuilder details = new StringBuilder();

    log.debug("Performing DPL check on Request " + reqId);
    try {
      ChangeLogListener.setManager(entityManager);
      GEOHandler geoHandler = null;

      Data data = requestData.getData();
      Admin admin = requestData.getAdmin();

      if (data != null) {
        geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
      }

      List<Addr> addresses = new ArrayList<Addr>();
      for (Addr addr : requestData.getAddresses()) {
        if (!"N".equals(addr.getDplChkResult()) && !"P".equals(addr.getDplChkResult()) && !"F".equals(addr.getDplChkResult())) {
          addresses.add(addr);
        }
      }

      if (addresses == null || addresses.size() == 0) {

        recomputeDPLResult(user, entityManager, requestData);

        if ("U".equals(admin.getReqType()) && StringUtils.isNotEmpty(data.getEmbargoCd()) && !"N".equals(data.getEmbargoCd())) {
          validation.setSuccess(false);
          output.setOnError(true);
          engineData.addRejectionComment("This is a CMR record with a DPL/Embargo Code. ERC approval will be needed to process this request.");
          log.debug(
              "This is a CMR record with a DPL/Embargo Code. ERC approval will be needed to process this request.Hence sending back to processor.");
          details.append(details.toString());
          output.setResults("CMR with DPL/Embargo Code");
          output.setDetails(details.toString());
          output.setProcessOutput(validation);
          return output;
        }

        validation.setSuccess(true);
        validation.setMessage("No DPL check needed.");
        output.setResults("No DPL check needed.");
        output.setDetails("DPL check already completed for all addresses. No DPL check needed.");
        output.setProcessOutput(validation);
        return output;
      }

      for (Addr addr : addresses) {
        // initialize all
        addr.setDplChkResult(null);
      }

      if (geoHandler != null) {
        geoHandler.doBeforeDPLCheck(entityManager, data, addresses);
      }

      DPLCheckResult dplResult = null;
      String errorInfo = null;

      AddressService addrService = new AddressService();
      for (Addr addr : addresses) {
        errorInfo = null;
        if (addr.getDplChkResult() == null) {

          if (geoHandler != null && geoHandler.customerNamesOnAddress() && StringUtils.isBlank(addr.getCustNm1())) {
            // this may be an internal address, skip DPL
            addr.setDplChkResult("N");
            addr.setDplChkById(user.getIntranetId());
            addr.setDplChkByNm(user.getBluePagesName());
            addr.setDplChkErrList(null);
            addr.setDplChkInfo(null);
            addr.setDplChkTs(SystemUtil.getCurrentTimestamp());
            entityManager.merge(addr);
          } else {

            Boolean errorStatus = false;
            try {
              dplResult = addrService.dplCheckAddress(admin, addr, geoHandler != null ? !geoHandler.customerNamesOnAddress() : false);
            } catch (Exception e) {
              log.error("Error in performing DPL Check when call EVS on Request ID " + reqId + " Addr " + addr.getId().getAddrType() + "/"
                  + addr.getId().getAddrSeq(), e);
              if (dplResult == null) {
                dplResult = new DPLCheckResult();
              }
              errorStatus = true;
            }
            if (dplResult.isPassed()) {
              addr.setDplChkResult("P");
              addr.setDplChkById(user.getIntranetId());
              addr.setDplChkByNm(user.getBluePagesName());
              addr.setDplChkErrList(null);
              addr.setDplChkInfo(null);
              addr.setDplChkTs(SystemUtil.getCurrentTimestamp());
              details.append(details.length() > 0 ? "\n" : "");
              details.append("DPL Check for [" + addr.getId().getAddrType() + "/" + addr.getId().getAddrSeq() + "] passed.");
              entityManager.merge(addr);
            } else {
              errorInfo = "";
              if (dplResult.isUnderReview()) {
                errorInfo += " Export under review";
              }
              if (errorStatus) {
                errorInfo = MessageUtil.getMessage(MessageUtil.ERROR_DPL_EVS_ERROR);
              } else if (!StringUtils.isEmpty(dplResult.getFailureDesc())) {
                errorInfo += ", " + dplResult.getFailureDesc();
              }
              details.append(details.length() > 0 ? "\n" : "");
              details.append("Address [" + addr.getId().getAddrType() + "/" + addr.getId().getAddrSeq() + "] FAILED DPL check. - " + errorInfo);
              List<String> available = EWSProperties.listAllDplExportLocation();
              List<String> passed = dplResult.getLocationsPassed();
              StringBuilder failedList = new StringBuilder();
              for (String loc : available) {
                if (passed != null && !passed.contains(loc) && !"ALL".equals(loc)) {
                  failedList.append(failedList.length() > 0 ? ", " : "");
                  failedList.append(loc);
                }
              }

              addr.setDplChkResult("F");
              addr.setDplChkById(user.getIntranetId());
              addr.setDplChkByNm(user.getBluePagesName());
              addr.setDplChkErrList(failedList.toString());
              addr.setDplChkInfo(errorInfo);
              addr.setDplChkTs(SystemUtil.getCurrentTimestamp());
              entityManager.merge(addr);
            }

          }

        } else {
          addr.setDplChkById(user.getIntranetId());
          addr.setDplChkByNm(user.getBluePagesName());
          addr.setDplChkErrList(null);
          addr.setDplChkInfo(null);
          addr.setDplChkTs(SystemUtil.getCurrentTimestamp());
          entityManager.merge(addr);
        }
      }

      entityManager.flush();
      // compute the overall score
      recomputeDPLResult(user, entityManager, requestData);

      Scorecard scorecard = requestData.getScorecard();

      switch (scorecard.getDplChkResult()) {
      case "NR":
        validation.setSuccess(true);
        validation.setMessage("Not Required");
        break;
      case "AP":
        validation.setSuccess(true);
        validation.setMessage("All Passed");
        break;
      case "SF":
        validation.setSuccess(false);
        output.setOnError(true);
        engineData.addRejectionComment("DPL Check Failed for some addresses.");
        validation.setMessage("Some Failed");
        break;
      case "AF":
        validation.setSuccess(false);
        output.setOnError(true);
        engineData.addRejectionComment("DPL Check Failed.");
        validation.setMessage("All Failed");
        break;
      default:
        validation.setSuccess(false);
        output.setOnError(true);
        engineData.addRejectionComment("DPL Check could not be performed.");
        validation.setMessage("Not Done");
        break;
      }

      if ("U".equals(admin.getReqType()) && StringUtils.isNotEmpty(data.getEmbargoCd()) && !"N".equals(data.getEmbargoCd())) {
        validation.setSuccess(false);
        output.setOnError(true);
        engineData.addRejectionComment("This is a CMR record with a DPL/Embargo Code. ERC approval will be needed to process this request.");
        log.debug(
            "This is a CMR record with a DENIAL DPL Block Code. ERC approval will be needed to process this request.Hence sending back to processor.");
        details.append("This is a CMR record with a DPL/Embargo Code. ERC approval will be needed to process this request.");
        validation.setMessage("CMR with DPL/Embargo Code");
      }

    } finally {
      ChangeLogListener.clearManager();
    }

    output.setDetails(details.toString());
    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;
  }

  private void recomputeDPLResult(AppUser user, EntityManager entityManager, RequestData requestData) {

    Scorecard scorecard = requestData.getScorecard();
    long reqId = requestData.getAdmin().getId().getReqId();

    log.debug("Recomputing DPL Results for Request ID " + reqId);
    int all = 0;
    int passed = 0;
    int failed = 0;
    int notdone = 0;
    int notrequired = 0;

    for (Addr address : requestData.getAddresses()) {
      all++;
      if ("P".equals(address.getDplChkResult())) {
        passed++;
      }
      if ("F".equals(address.getDplChkResult())) {
        failed++;
      }
      if ("N".equals(address.getDplChkResult())) {
        notrequired++;
      }
      if (StringUtils.isBlank(address.getDplChkResult())) {
        notdone++;
      }
    }

    if (all == notrequired) {
      scorecard.setDplChkResult("NR");
      // not required
    } else if (all == passed + notrequired) {
      scorecard.setDplChkResult("AP");
      // all passed
    } else if (all == failed + notrequired) {
      // all failed
      scorecard.setDplChkResult("AF");
    } else if (passed > 0 && all != passed) {
      // some passed, some failed/not done
      scorecard.setDplChkResult("SF");
    }

    // if there is at least one Not done, set to not done
    if (notdone > 0) {
      scorecard.setDplChkResult("Not Done");
    }
    if (notdone != all) {
      // update if DPL has indeed been performed
      scorecard.setDplChkTs(SystemUtil.getCurrentTimestamp());
      scorecard.setDplChkUsrId(user.getIntranetId());
      scorecard.setDplChkUsrNm(user.getBluePagesName());
    }
    log.debug(" - DPL Status for Request ID " + reqId + " : " + scorecard.getDplChkResult());
    updateEntity(scorecard, entityManager);
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_DPL_CHECK;
  }

  @Override
  public String getProcessDesc() {
    return "DPL Check";
  }

}
