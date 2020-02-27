package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.entity.SuppCntryPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.SuppCountryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Jose Belgira
 * 
 * 
 */
@Component
public class SuppCountryMaintainService extends BaseService<SuppCountryModel, SuppCntry> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SuppCountryMaintainService.class);
  }

  @Override
  protected void performTransaction(SuppCountryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<SuppCountryModel> doSearch(SuppCountryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SuppCntry suppCntry = getCurrentRecord(model, entityManager, request);
    SuppCountryModel newModel = new SuppCountryModel();
    copyValuesFromEntity(suppCntry, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected SuppCntry getCurrentRecord(SuppCountryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String cntryCd = model.getCntryCd();
    String sql = ExternalizedQuery.getSql("SYSTEM.SUPP_CNTRY_BY_CNTRY_CD");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNTRY_CD", cntryCd);
    return q.getSingleResult(SuppCntry.class);
  }

  @Override
  protected SuppCntry createFromModel(SuppCountryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    SuppCntry suppCntry = new SuppCntry();
    suppCntry.setId(new SuppCntryPK());
    copyValuesToEntity(model, suppCntry);
    return suppCntry;
  }

  @Override
  protected void doBeforeInsert(SuppCntry entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    entity.setCreateDt(this.currentTimestamp);
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
