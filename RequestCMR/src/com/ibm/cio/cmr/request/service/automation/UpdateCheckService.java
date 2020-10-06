package com.ibm.cio.cmr.request.service.automation;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.UpdateSwitchElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
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

      AutomationResult<ValidationOutput> updtElementResult = updtElement.executeElement(entityManager, requestData, null);

      if (updtElementResult != null) {
        updtChkModel.setResult(updtElementResult.getResults());
        updtChkModel.setSuccess(updtElementResult.getProcessOutput().isSuccess());
        updtChkModel.setDetails(updtElementResult.getDetails());
      }
    }

    return updtChkModel;
  }
}