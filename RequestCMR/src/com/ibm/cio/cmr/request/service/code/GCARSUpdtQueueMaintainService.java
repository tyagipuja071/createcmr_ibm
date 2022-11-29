package com.ibm.cio.cmr.request.service.code;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.GCARSUpdtQueue;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.GCARSUpdtQueueModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class GCARSUpdtQueueMaintainService extends BaseService<GCARSUpdtQueueModel, GCARSUpdtQueue> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(GCARSUpdtQueueMaintainService.class);
  }

  @Override
  protected void performTransaction(GCARSUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<GCARSUpdtQueueModel> doSearch(GCARSUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    GCARSUpdtQueue currentModel = getCurrentRecord(model, entityManager, request);
    GCARSUpdtQueueModel newModel = new GCARSUpdtQueueModel();

    copyValuesFromEntity(currentModel, newModel);

    newModel.setState(BaseModel.STATE_EXISTING);
    newModel.setCreatedBy(currentModel.getCreatedBy());
    newModel.setCreatedTsStr(formatter.format(currentModel.getCreateDt()));
    newModel.setUpdatedBy(currentModel.getUpdatedBy());
    newModel.setUpdatedTsStr(formatter.format(currentModel.getUpdateDt()));

    return Collections.singletonList(newModel);
  }

  @Override
  protected GCARSUpdtQueue getCurrentRecord(GCARSUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("BR.GET.GCARS_UPDT_QUEUE_BY_SOURCE_NAME_AND_SEQ_NO");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("SOURCE_NAME", model.getSourceName() == null ? "" : model.getSourceName());
    q.setParameter("SEQ_NO", model.getSeqNo() == null ? "" : model.getSeqNo());

    return q.getSingleResult(GCARSUpdtQueue.class);

  }

  @Override
  protected GCARSUpdtQueue createFromModel(GCARSUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected void doBeforeInsert(GCARSUpdtQueue entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
  }

  @Override
  protected void doBeforeUpdate(GCARSUpdtQueue entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
  }

}
