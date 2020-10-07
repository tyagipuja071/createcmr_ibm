package com.ibm.cio.cmr.request.service.automation;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.UpdateSwitchElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.RejectionContainer;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.automation.UpdateCheckModel;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

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
    UpdateSwitchElement updtElement = new UpdateSwitchElement(null, null, false, false);
    LOG.debug("Processing doProcess() method of  AutoCheckService");
    updtChkModel = (UpdateCheckModel) params.getParam("model");
    if (updtChkModel != null) {
      Long reqId = Long.parseLong(updtChkModel.getReqId());
      RequestData requestData = new RequestData(entityManager, reqId);
      AutomationEngineData engineData = new AutomationEngineData();
      AutomationResult<ValidationOutput> updtElementResult = updtElement.executeElement(entityManager, requestData, engineData);

      if (updtElementResult != null) {
        updtChkModel.setResult(updtElementResult.getResults());
        updtChkModel.setOnError(updtElementResult.isOnError());
        List<RejectionContainer> rejectContList = (List<RejectionContainer>) engineData.get("rejections");
        String message = "";
        if (rejectContList != null && !rejectContList.isEmpty()) {
          for (RejectionContainer r : rejectContList) {
            message = message + r.getRejComment() + "\n";
          }
        }
        updtChkModel.setValidationMessage(message);
      }
    }

    return updtChkModel;
  }
}