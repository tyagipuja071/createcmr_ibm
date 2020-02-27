package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.StatusDesc;
import com.ibm.cio.cmr.request.model.code.StatusDescModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rochelle Salazar
 * 
 */
@Component
public class StatusDescService extends BaseService<StatusDescModel, StatusDesc> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(StatusDescService.class);
  }

  @Override
  protected void performTransaction(StatusDescModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<StatusDescModel> doSearch(StatusDescModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.STATUSDESC");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    List<StatusDescModel> list = new ArrayList<>();
    List<StatusDesc> record = q.getResults(StatusDesc.class);
    StatusDescModel statusDescModel = null;
    for (StatusDesc statusDesc : record) {
      statusDescModel = new StatusDescModel();
      statusDescModel.setStatusDesc(statusDesc.getStatusDesc());
      statusDescModel.setReqStatus(statusDesc.getId().getReqStatus());
      statusDescModel.setCmrIssuingCntry(statusDesc.getCmrIssuingCntry());
      list.add(statusDescModel);
    }
    return list;
  }

  @Override
  protected StatusDesc getCurrentRecord(StatusDescModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected StatusDesc createFromModel(StatusDescModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
