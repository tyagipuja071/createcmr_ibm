/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.DefaultApprovalConditions;
import com.ibm.cio.cmr.request.entity.DefaultApprovalConditionsPK;
import com.ibm.cio.cmr.request.model.code.DefaultApprovalConditionsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class DefaultApprovalConditionsService extends BaseService<DefaultApprovalConditionsModel, DefaultApprovalConditions> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(DefaultApprovalConditionsService.class);
  }

  @Override
  protected void performTransaction(DefaultApprovalConditionsModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    if ("ADD_CONDITION".equals(model.getAction())) {
      DefaultApprovalConditions condition = createFromModel(model, entityManager, request);
      Integer max = getMaxSequenceNo(entityManager, model.getDefaultApprovalId());
      if (max == null) {
        max = new Integer(0);
      }
      max = max + 1;
      condition.getId().setSequenceNo(max);
      model.setSequenceNo(max);
      this.log.debug("Adding condition " + model.getSequenceNo());
      createEntity(condition, entityManager);
    } else if ("REMOVE_CONDITION".equals(model.getAction())) {
      DefaultApprovalConditions condition = getCurrentRecord(model, entityManager, request);
      this.log.debug("Removing condition " + model.getSequenceNo());
      deleteEntity(condition, entityManager);
    } else if ("UPDATE_CONDITION".equals(model.getAction())) {
      DefaultApprovalConditions condition = getCurrentRecord(model, entityManager, request);
      copyValuesToEntity(model, condition);
      this.log.debug("Updating condition " + model.getSequenceNo());
      updateEntity(condition, entityManager);
    }
  }

  private Integer getMaxSequenceNo(EntityManager entityManager, long defaultApprovalId) {
    String sql = ExternalizedQuery.getSql("SYSTEM.DEFAULT_APPROVALS.GET_MAX_SEQUENCE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("DEFAULT_APPROVAL_ID", defaultApprovalId);
    return query.getSingleResult(Integer.class);
  }

  @Override
  protected List<DefaultApprovalConditionsModel> doSearch(DefaultApprovalConditionsModel model, EntityManager entityManager,
      HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected DefaultApprovalConditions getCurrentRecord(DefaultApprovalConditionsModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.DEFAULT_APPROVALS.GET_CONDITION");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("DEFAULT_APPROVAL_ID", model.getDefaultApprovalId());
    query.setParameter("SEQUENCE_NO", model.getSequenceNo());
    return query.getSingleResult(DefaultApprovalConditions.class);
  }

  @Override
  protected DefaultApprovalConditions createFromModel(DefaultApprovalConditionsModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    DefaultApprovalConditionsPK pk = new DefaultApprovalConditionsPK();
    DefaultApprovalConditions condition = new DefaultApprovalConditions();
    condition.setId(pk);
    copyValuesToEntity(model, condition);
    return condition;
  }

}
