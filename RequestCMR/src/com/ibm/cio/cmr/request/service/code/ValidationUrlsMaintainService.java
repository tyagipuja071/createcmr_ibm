/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.ValidationUrl;
import com.ibm.cio.cmr.request.entity.ValidationUrlPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.requestentry.ValidationUrlModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Eduard Bernardo
 * 
 */
@Component
public class ValidationUrlsMaintainService extends BaseService<ValidationUrlModel, ValidationUrl> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ValidationUrlsMaintainService.class);
  }

  @Override
  protected void doBeforeInsert(ValidationUrl entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdtBy(user.getIntranetId());
    entity.setUpdtTs(this.currentTimestamp);

    String sql = ExternalizedQuery.getSql("SYSTEM.GET_MAX_VAL_SEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", entity.getId().getCntryCd());
    Integer max = query.getSingleResult(Integer.class);
    if (max == null) {
      max = new Integer(0);
    }
    max = max + 1;
    entity.getId().setDisplaySeqNo(max);
  }

  @Override
  protected void doBeforeUpdate(ValidationUrl entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdtBy(user.getIntranetId());
    entity.setUpdtTs(this.currentTimestamp);
  }

  @Override
  protected void performTransaction(ValidationUrlModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
  }

  @Override
  protected List<ValidationUrlModel> doSearch(ValidationUrlModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    ValidationUrl valUrl = getCurrentRecord(model, entityManager, request);
    ValidationUrlModel newModel = new ValidationUrlModel();
    copyValuesFromEntity(valUrl, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected ValidationUrl getCurrentRecord(ValidationUrlModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    long displaySeqNo = model.getDisplaySeqNo();
    String cntryCd = model.getCntryCd();
    String sql = ExternalizedQuery.getSql("SYSTEM.VALIDATION_URL");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("DISPLAY_SEQ_NO", displaySeqNo);
    q.setParameter("CNTRY_CD", cntryCd);
    return q.getSingleResult(ValidationUrl.class);
  }

  @Override
  protected ValidationUrl createFromModel(ValidationUrlModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    ValidationUrl valUrl = new ValidationUrl();
    valUrl.setId(new ValidationUrlPK());
    copyValuesToEntity(model, valUrl);
    return valUrl;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}