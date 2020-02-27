package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.BaseEntityPk;
import com.ibm.cio.cmr.request.entity.StatusAct;
import com.ibm.cio.cmr.request.entity.StatusActPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.StatusActModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class StatusActMaintainService extends BaseService<StatusActModel, StatusAct> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(StatusActMaintainService.class);
  }

  @Override
  protected void performTransaction(StatusActModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<StatusActModel> doSearch(StatusActModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    StatusAct statusAct = getCurrentRecord(model, entityManager, request);
    StatusActModel newModel = new StatusActModel();
    copyValuesFromEntity(statusAct, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected StatusAct getCurrentRecord(StatusActModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String action = model.getModelAction();
    String sql = ExternalizedQuery.getSql("SYSTEM.STATUS_ACT_BY_ACTION");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("ACTION", action);
    return q.getSingleResult(StatusAct.class);
  }

  @Override
  protected StatusAct createFromModel(StatusActModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    StatusAct statusAct = new StatusAct();
    StatusActPK statusActPk = new StatusActPK();
    statusAct.setId(statusActPk);
    super.copyValuesToEntity(model, statusAct);
    statusActPk.setAction(model.getModelAction());
    statusAct.setId(statusActPk);
    return statusAct;
  }

  @Override
  public void copyValuesToEntity(StatusActModel from, StatusAct to) {
    try {
      from.setAction(from.getModelAction());

      PropertyUtils.copyProperties(to, from);

      BaseEntityPk id = to.getId();

      PropertyUtils.copyProperties(id, from);

    } catch (Exception e) {
      this.log.error("Error when copying properties", e);
    }
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
