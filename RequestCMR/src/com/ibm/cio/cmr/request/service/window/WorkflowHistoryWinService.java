/**
 * 
 */
package com.ibm.cio.cmr.request.service.window;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.window.WorkflowHistWinModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class WorkflowHistoryWinService extends BaseSimpleService<WorkflowHistWinModel> {

  @Override
  protected WorkflowHistWinModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String sql = ExternalizedQuery.getSql("WORKFLOW.GETRECORD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", params.getParam("reqId"));
    List<Admin> records = query.getResults(1, Admin.class);
    if (records != null && records.size() > 0) {
      WorkflowHistWinModel model = new WorkflowHistWinModel();
      Admin admin = records.get(0);
      model.setReqId(admin.getId().getReqId());
      model.setCustomerName(admin.getMainCustNm1());
      model.setExpedite("Y".equals(admin.getExpediteInd()) ? "Yes" : "No");

      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()))
        model.setRequestType("Create");
      else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()))
        model.setRequestType("Update");
      else if (CmrConstants.REQ_TYPE_MASS_UPDATE.equals(admin.getReqType()))
        model.setRequestType("Mass Update");
      else if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType()))
        model.setRequestType("Mass Create");
      else if (CmrConstants.REQ_TYPE_REACTIVATE.equals(admin.getReqType()))
        model.setRequestType("Reactivate");
      else if (CmrConstants.REQ_TYPE_DELETE.equals(admin.getReqType()))
        model.setRequestType("Delete");

      return model;
    }
    return null;
  }
}
