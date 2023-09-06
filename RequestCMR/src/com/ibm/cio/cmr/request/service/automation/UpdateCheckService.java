package com.ibm.cio.cmr.request.service.automation;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.UpdateSwitchElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.RejectionContainer;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.automation.UpdateCheckModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.ui.PageManager;

/**
 * @author PoojaTyagi
 * 
 */
@Component
public class UpdateCheckService extends BaseSimpleService<UpdateCheckModel> {

  private static final Logger LOG = Logger.getLogger(UpdateCheckService.class);

  @Override
  protected UpdateCheckModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    UpdateCheckModel updtChkModel = null;
    RequestEntryModel reqEntryModel = null;

    UpdateSwitchElement updtElement = new UpdateSwitchElement(null, null, false, false);
    LOG.debug("Processing doProcess() method of  AutoCheckService");
    updtChkModel = (UpdateCheckModel) params.getParam("model");
    reqEntryModel = (RequestEntryModel) params.getParam("reqModel");

    if (updtChkModel != null && reqEntryModel != null) {
      RequestData requestData = new RequestData(entityManager, reqEntryModel.getReqId());
      Data data = requestData.getData();
      Admin admin = requestData.getAdmin();
      if (data != null) {
        entityManager.detach(data);
        // CREATCMR-10246 not copying due to field name difference
        setModeOfPayment(reqEntryModel);
        PropertyUtils.copyProperties(data, reqEntryModel);
      }
      if (admin != null) {
        entityManager.detach(admin);
        PropertyUtils.copyProperties(admin, reqEntryModel);
      }
      AutomationEngineData engineData = new AutomationEngineData();
      AutomationResult<ValidationOutput> updtElementResult = updtElement.executeElement(entityManager, requestData, engineData);

      if (updtElementResult != null) {
        Map<String, String> pendingChecks = engineData.getPendingChecks();
        updtChkModel.setResult(updtElementResult.getResults());
        updtChkModel.setOnError(updtElementResult.isOnError());
        List<RejectionContainer> rejectContList = engineData.getRejectionReasons();
        String message = "";
        String negativeChecksMessage = "";
        if (rejectContList != null && !rejectContList.isEmpty()) {
          for (RejectionContainer r : rejectContList) {
            message = message + r.getRejComment() + "\n";
          }
        }

        if (!updtElementResult.isOnError()) {
          if (pendingChecks != null && !pendingChecks.isEmpty()) {
            // for (String pendingCheck : pendingChecks.values()) {
            // negativeChecksMessage = negativeChecksMessage + pendingCheck +
            // "\n";
            // }
            if (StringUtils.isNotBlank(updtElementResult.getDetails())) {
              negativeChecksMessage = negativeChecksMessage + updtElementResult.getDetails();
            }
          }
        }
        updtChkModel.setRejectionMsg(message.replaceAll("\n", "<br/>"));
        updtChkModel.setNegativeChksMsg(negativeChecksMessage.replace("\n", "<br/>"));
      }
    }

    return updtChkModel;
  }

  private void setModeOfPayment(RequestEntryModel reqEntryModel) {
    if (PageManager.fromGeo("MCO", reqEntryModel.getCmrIssuingCntry()) || PageManager.fromGeo("NORDX", reqEntryModel.getCmrIssuingCntry())
        || PageManager.fromGeo("EMEA", reqEntryModel.getCmrIssuingCntry())) {
      reqEntryModel.setModeOfPayment(reqEntryModel.getPaymentMode());
    }

  }
}