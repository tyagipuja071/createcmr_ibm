package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.StatusDesc;
import com.ibm.cio.cmr.request.entity.StatusDescPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.StatusDescModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rochelle Salazar
 * 
 */
@Component
public class StatusDescMaintService extends BaseService<StatusDescModel, StatusDesc> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(StatusDescMaintService.class);
  }

  @Override
  protected void performTransaction(StatusDescModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<StatusDescModel> doSearch(StatusDescModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    StatusDesc statusdesc = getCurrentRecord(model, entityManager, request);
    StatusDescModel newModel = new StatusDescModel();
    copyValuesFromEntity(statusdesc, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected StatusDesc getCurrentRecord(StatusDescModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String reqStatus = model.getReqStatus();
    String sql = ExternalizedQuery.getSql("SYSTEM.STATUSDESCMAINT");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("REQ_STATUS", reqStatus);
    return q.getSingleResult(StatusDesc.class);
  }

  @Override
  protected StatusDesc createFromModel(StatusDescModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    StatusDesc status = new StatusDesc();
    StatusDescPK pk = new StatusDescPK();
    pk.setReqStatus(model.getReqStatus());
    status.setId(pk);
    status.setCmrIssuingCntry(model.getCmrIssuingCntry());
    status.setStatusDesc(model.getStatusDesc());
    return status;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
