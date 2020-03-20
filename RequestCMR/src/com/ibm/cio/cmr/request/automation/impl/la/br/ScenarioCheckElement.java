package com.ibm.cio.cmr.request.automation.impl.la.br;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;

public class ScenarioCheckElement extends ValidatingElement {

  private static final Logger log = Logger.getLogger(ScenarioCheckElement.class);

  public ScenarioCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    long reqId = requestData.getAdmin().getId().getReqId();

    AutomationResult<ValidationOutput> output = buildResult(reqId);
    ValidationOutput validation = new ValidationOutput();
    log.debug("Entering performBRScenarioCheck()");
    try {
      ChangeLogListener.setManager(entityManager);
      String scenarioSubType = null;
      String reqReason = requestData.getAdmin().getReqReason();
      Data data = requestData.getData();
      Admin admin = requestData.getAdmin();
      String soldToVat = null;
      soldToVat = requestData.getAddress("ZS01").getVat();

      if ("C".equals(admin.getReqType()) && data != null) {
        scenarioSubType = data.getCustSubGrp();
      } else if ("U".equals(admin.getReqType())) {
        scenarioSubType = LAHandler.getScenarioTypeForUpdateBRV2(entityManager, data, requestData.getAddresses());
      }

      if (scenarioSubType != null && StringUtils.isNotEmpty(scenarioSubType)
          && ("CROSS".equals(scenarioSubType) || "PRIPE".equals(scenarioSubType) || "IBMEM".equals(scenarioSubType))) {
        validation.setSuccess(false);
        validation.setMessage("Scenario need review");
        output.setDetails(
            LAHandler.getScenarioDescBR(scenarioSubType) + " Scenario found on the request. Hence sending back to the processor for the review ");
        output.setOnError(true);
        engineData.addRejectionComment("Non-automated Scenario" + LAHandler.getScenarioDescBR(scenarioSubType) + " Scenario found on the request");
        log.debug(
            LAHandler.getScenarioDescBR(scenarioSubType) + " Scenario found on the request. Hence sending back to the processor for the review ");
      } else if (scenarioSubType != null && StringUtils.isNotEmpty(scenarioSubType) && "SOFTL".equals(scenarioSubType)
          && !StringUtils.isEmpty(soldToVat) && (soldToVat.matches("0{14}") || soldToVat.matches("9{14}"))
          && (reqReason != null && StringUtils.isNotEmpty(reqReason) && !"AUCO".equalsIgnoreCase(reqReason))) {
        validation.setSuccess(false);
        validation.setMessage("Scenario need review");
        output.setDetails(LAHandler.getScenarioDescBR(scenarioSubType)
            + " Scenario found on the request and VAT contains all 0s or 9s. Hence sending back to the processor for the review ");
        output.setOnError(true);
        engineData
            .addRejectionComment(LAHandler.getScenarioDescBR(scenarioSubType) + " Scenario found on the request and VAT contains all 0s or 9s.");
        log.debug(LAHandler.getScenarioDescBR(scenarioSubType)
            + " Scenario found on the request and VAT contains all 0s or 9s. Hence sending back to the processor for the review ");
      } else if (engineData.getNegativeCheckStatus("StateFiscalCode") != null) {
        validation.setSuccess(false);
        validation.setMessage("Fiscal Code empty");
        output.setDetails("Sending back to Processor as state fiscal code is empty.Enter valid fiscal code value.");
        output.setOnError(true);
        engineData.addRejectionComment("State fiscal code is empty.Enter valid fiscal code value.");
        log.debug("Sending back to Processor as state fiscal code is empty.Enter valid fiscal code value.");
      } else if (scenarioSubType == null || "".equals(scenarioSubType)) {
        validation.setSuccess(true);
        validation.setMessage("Skip scenario check.");
        output.setDetails("Scenario type can't be identified on the request.");
        log.debug("Scenario is " + scenarioSubType + " Hence scenario check skipped.");
      } else {
        validation.setSuccess(true);
        validation.setMessage("Scenario check done.");
        output.setDetails(LAHandler.getScenarioDescBR(scenarioSubType) + " Scenario found on the request.No processor review is needed.");
        log.debug(LAHandler.getScenarioDescBR(scenarioSubType) + " Scenario found on the request. No processor review is needed. ");
      }

      // let's do a final save like on the UI if the requester automation is
      // enabled.

    } finally {
      ChangeLogListener.clearManager();
    }

    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.BR_SCENARIO;
  }

  @Override
  public String getProcessDesc() {
    return "Brazil - Scenario Check";
  }

}
