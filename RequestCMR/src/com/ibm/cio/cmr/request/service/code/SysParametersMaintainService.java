package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.SystParameters;
import com.ibm.cio.cmr.request.entity.SystParametersPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.SysParametersModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemParameters;

/**
 * @author Priy Ranjan
 * 
 * 
 */
@Component
public class SysParametersMaintainService extends BaseService<SysParametersModel, SystParameters> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SysParametersMaintainService.class);
  }

  @Override
  protected void performTransaction(SysParametersModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<SysParametersModel> doSearch(SysParametersModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SystParameters sysParam = getCurrentRecord(model, entityManager, request);
    SysParametersModel newModel = new SysParametersModel();
    copyValuesFromEntity(sysParam, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected SystParameters getCurrentRecord(SysParametersModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String parameterCd = model.getParameterCd();
    String sql = ExternalizedQuery.getSql("SYSTEM.SYS_PARAM_BY_PARAM_CD");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("PARAM_CD", parameterCd);
    return q.getSingleResult(SystParameters.class);
  }

  @Override
  protected SystParameters createFromModel(SysParametersModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    SystParameters sysParam = new SystParameters();
    sysParam.setId(new SystParametersPK());
    copyValuesToEntity(model, sysParam);
    return sysParam;
  }

  @Override
  protected void doBeforeInsert(SystParameters entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setCreateTs(this.currentTimestamp);
    entity.setCreateBy(user.getIntranetId());
    entity.setLastUpdtTs(this.currentTimestamp);
    entity.setLastUpdtBy(user.getIntranetId());
  }

  @Override
  protected void doBeforeUpdate(SystParameters entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    entity.setLastUpdtTs(this.currentTimestamp);
  }

  @Override
  protected void doAfterInsert(SystParameters entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doAfterInsert(entity, entityManager, request);
    SystemParameters.refresh();
  }

  @Override
  protected void doAfterUpdate(SystParameters entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doAfterUpdate(entity, entityManager, request);
    SystemParameters.refresh();
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
