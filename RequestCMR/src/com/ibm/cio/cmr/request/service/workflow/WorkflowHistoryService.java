package com.ibm.cio.cmr.request.service.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.WorkflowHistoryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rama Mohan
 * 
 */
@Component
public class WorkflowHistoryService extends BaseService<WorkflowHistoryModel, WfHist> {
  @Override
  protected Logger initLogger() {
    return Logger.getLogger(WorkflowHistoryService.class);
  }

  @Override
  protected void performTransaction(WorkflowHistoryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop
  }

  @Override
  protected List<WorkflowHistoryModel> doSearch(WorkflowHistoryModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    List<WorkflowHistoryModel> results = new ArrayList<WorkflowHistoryModel>();
    String sql = ExternalizedQuery.getSql("WORK_FLOW.HISTORY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());

    WorkflowHistoryModel wfHistModel = null;
    WfHist hist = null;
    String status = null;
    List<CompoundEntity> rs1 = query.getCompundResults(WfHist.class, WfHist.WORKFLOW_HISTORY_MAPPING);
    for (CompoundEntity ent : rs1) {
      hist = ent.getEntity(WfHist.class);
      status = (String) ent.getValue("OVERALL_STATUS");
      wfHistModel = new WorkflowHistoryModel();
      copyValuesFromEntity(hist, wfHistModel);
      wfHistModel.setState(BaseModel.STATE_EXISTING);
      wfHistModel.setOverallStatus(status);
      results.add(wfHistModel);
    }
    return results;
  }

  @Override
  protected WfHist getCurrentRecord(WorkflowHistoryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop
    return null;
  }

  @Override
  protected WfHist createFromModel(WorkflowHistoryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop
    return null;
  }

}
