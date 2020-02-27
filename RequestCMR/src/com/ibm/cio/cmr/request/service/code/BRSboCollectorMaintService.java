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
import com.ibm.cio.cmr.request.entity.ReftBrSboCollector;
import com.ibm.cio.cmr.request.entity.ReftBrSboCollectorPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.BRSboCollectorModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author RangoliSaxena
 * 
 */
@Component
public class BRSboCollectorMaintService extends BaseService<BRSboCollectorModel, ReftBrSboCollector> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(BRSboCollectorMaintService.class);
  }

  @Override
  protected void performTransaction(BRSboCollectorModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
  }

  @Override
  protected void doBeforeInsert(ReftBrSboCollector entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setLastUpdtBy(user.getIntranetId());
    entity.setLastUpdtTs(this.currentTimestamp);
  }

  @Override
  protected void doBeforeUpdate(ReftBrSboCollector entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setLastUpdtBy(user.getIntranetId());
    entity.setLastUpdtTs(this.currentTimestamp);
  }

  @Override
  protected List<BRSboCollectorModel> doSearch(BRSboCollectorModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    ReftBrSboCollector sboRecord = getCurrentRecord(model, entityManager, request);
    BRSboCollectorModel newModel = new BRSboCollectorModel();
    copyValuesFromEntity(sboRecord, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected ReftBrSboCollector getCurrentRecord(BRSboCollectorModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("BR_SBO_COLLECTOR.GET_RECORD");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("STATE_CD", model.getStateCd());
    return q.getSingleResult(ReftBrSboCollector.class);
  }

  @Override
  protected ReftBrSboCollector createFromModel(BRSboCollectorModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    ReftBrSboCollector sbo = new ReftBrSboCollector();
    sbo.setId(new ReftBrSboCollectorPK());
    copyValuesToEntity(model, sbo);
    return sbo;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
