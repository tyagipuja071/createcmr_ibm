package com.ibm.cio.cmr.request.service.automation;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.UpdateSwitchElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.RejectionContainer;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.automation.UpdateCheckModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
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
    RequestEntryModel reqEntryModel = null;
    Data data = new Data();
    AdminPK adminPk = new AdminPK();

    UpdateSwitchElement updtElement = new UpdateSwitchElement(null, null, false, false);
    LOG.debug("Processing doProcess() method of  AutoCheckService");
    updtChkModel = (UpdateCheckModel) params.getParam("model");
    reqEntryModel = (RequestEntryModel) params.getParam("reqModel");

    if (updtChkModel != null && reqEntryModel != null) {
      adminPk.setReqId(reqEntryModel.getReqId());
      Admin admin = entityManager.find(Admin.class, adminPk);
      PropertyUtils.copyProperties(data, reqEntryModel);
      if (admin != null)
        PropertyUtils.copyProperties(admin, reqEntryModel);
      RequestData requestData = new RequestData(entityManager, reqEntryModel.getReqId());
      requestData.setData(data);
      requestData.setAdmin(admin);
      AutomationEngineData engineData = new AutomationEngineData();
      AutomationResult<ValidationOutput> updtElementResult = updtElement.executeElement(entityManager, requestData, engineData);

      if (updtElementResult != null) {
        Map<String, String> pendingChecks = (Map<String, String>) engineData.get(AutomationEngineData.NEGATIVE_CHECKS);
        updtChkModel.setResult(updtElementResult.getResults());
        updtChkModel.setOnError(updtElementResult.isOnError());
        List<RejectionContainer> rejectContList = (List<RejectionContainer>) engineData.get("rejections");
        String message = "";
        String negativeChecksMessage = "";
        if (rejectContList != null && !rejectContList.isEmpty()) {
          for (RejectionContainer r : rejectContList) {
            message = message + r.getRejComment() + "\n";
          }
        }

        if (!updtElementResult.isOnError()) {
          if (pendingChecks != null && !pendingChecks.isEmpty()) {
            for (String pendingCheck : pendingChecks.values()) {
              negativeChecksMessage = negativeChecksMessage + pendingCheck + "\n";
            }
          }
        }
        updtChkModel.setRejectionMsg(message.replaceAll("\n", "<br/>"));
        updtChkModel.setNegativeChksMsg(negativeChecksMessage.replace("\n", "<br/>"));
      }
    }

    return updtChkModel;
  }
}