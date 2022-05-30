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
import com.ibm.cio.cmr.request.entity.USTCRUpdtQueue;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.USTCRUpdtQueueModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class USTcrUpdtQueueMaintainService extends BaseService<USTCRUpdtQueueModel, USTCRUpdtQueue> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USTcrUpdtQueueMaintainService.class);
  }

  @Override
  protected void performTransaction(USTCRUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<USTCRUpdtQueueModel> doSearch(USTCRUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    USTCRUpdtQueue currentModel = getCurrentRecord(model, entityManager, request);
    USTCRUpdtQueueModel newModel = new USTCRUpdtQueueModel();

    copyValuesFromEntity(currentModel, newModel);

    newModel.setState(BaseModel.STATE_EXISTING);
    newModel.setCreatedBy(currentModel.getCreateBy());
    newModel.setCreatedTsStr(formatter.format(currentModel.getCreateDt()));
    newModel.setUpdatedBy(currentModel.getUpdateBy());
    newModel.setUpdatedTsStr(formatter.format(currentModel.getUpdateDt()));

    return Collections.singletonList(newModel);
  }

  @Override
  protected USTCRUpdtQueue getCurrentRecord(USTCRUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("US.GET.US_TCR_UPDT_QUEUE_BY_TCR_INPUT_FILE_NM_AND_SEQ_NO");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setParameter("TCR_INPUT_FILE_NM", model.getTcrFileNm() == null ? "" : model.getTcrFileNm());
    q.setParameter("SEQ_NO", model.getSeqNo() == null ? "" : model.getSeqNo());

    return q.getSingleResult(USTCRUpdtQueue.class);

  }

  @Override
  protected USTCRUpdtQueue createFromModel(USTCRUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected void doBeforeInsert(USTCRUpdtQueue entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
  }

  @Override
  protected void doBeforeUpdate(USTCRUpdtQueue entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
  }

}
