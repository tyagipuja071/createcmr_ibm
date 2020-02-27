package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.CntryGeoDef;
import com.ibm.cio.cmr.request.entity.CntryGeoDefPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.CntryGeoDefModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Rochelle Salazar
 * 
 */
@Controller
public class CntryGeoDefMaintService extends BaseService<CntryGeoDefModel, CntryGeoDef> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(CntryGeoDefMaintService.class);
  }

  @Override
  protected void performTransaction(CntryGeoDefModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<CntryGeoDefModel> doSearch(CntryGeoDefModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    CntryGeoDef cntryGeoDef = getCurrentRecord(model, entityManager, request);
    CntryGeoDefModel newModel = new CntryGeoDefModel();
    copyValuesFromEntity(cntryGeoDef, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected CntryGeoDef getCurrentRecord(CntryGeoDefModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String geoCd = model.getGeoCd();
    String cmrIssuingCntry = model.getCmrIssuingCntry();

    String sql = ExternalizedQuery.getSql("SYSTEM.CNTRYGEODEFMAINT");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("GEO_CD", geoCd);
    q.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    return q.getSingleResult(CntryGeoDef.class);
  }

  @Override
  protected CntryGeoDef createFromModel(CntryGeoDefModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    CntryGeoDef cntryGeoDef = new CntryGeoDef();
    CntryGeoDefPK pk = new CntryGeoDefPK();
    pk.setGeoCd(model.getGeoCd());
    pk.setCmrIssuingCntry(model.getCmrIssuingCntry());
    cntryGeoDef.setId(pk);
    cntryGeoDef.setCntryDesc(model.getCntryDesc());
    cntryGeoDef.setComments(model.getComments());
    cntryGeoDef.setCreateBy(model.getCreateBy());
    cntryGeoDef.setCreateTs(model.getCreateTs());
    cntryGeoDef.setLastUpdtBy(model.getLastUpdtBy());
    cntryGeoDef.setLastUpdtTs(model.getLastUpdtTs());
    return cntryGeoDef;
  }

  @Override
  protected void doBeforeInsert(CntryGeoDef entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setLastUpdtBy(user.getIntranetId());
    entity.setLastUpdtTs(this.currentTimestamp);
  }

  @Override
  protected void doBeforeUpdate(CntryGeoDef entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setLastUpdtBy(user.getIntranetId());
    entity.setLastUpdtTs(this.currentTimestamp);
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
