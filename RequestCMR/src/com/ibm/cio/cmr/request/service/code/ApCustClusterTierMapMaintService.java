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
import com.ibm.cio.cmr.request.entity.ApCustClusterTierMap;
import com.ibm.cio.cmr.request.entity.ApCustClusterTierMapPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.ApCustClusterTierMapModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Anuja Srivastava
 * 
 */
@Component
public class ApCustClusterTierMapMaintService extends BaseService<ApCustClusterTierMapModel, ApCustClusterTierMap> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ApCustClusterTierMapMaintService.class);
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

  @Override
  protected void performTransaction(ApCustClusterTierMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected void doBeforeInsert(ApCustClusterTierMap entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdtBy(user.getIntranetId());
    entity.setUpdtTs(this.currentTimestamp);
  }

  @Override
  protected void doBeforeUpdate(ApCustClusterTierMap entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdtBy(user.getIntranetId());
    entity.setUpdtTs(this.currentTimestamp);
  }

  @Override
  protected List<ApCustClusterTierMapModel> doSearch(ApCustClusterTierMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    ApCustClusterTierMap acctmRecord = getCurrentRecord(model, entityManager, request);
    ApCustClusterTierMapModel newModel = new ApCustClusterTierMapModel();
    copyValuesFromEntity(acctmRecord, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected ApCustClusterTierMap getCurrentRecord(ApCustClusterTierMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    String sql = ExternalizedQuery.getSql("AP_CUST_CLUSTER_TIER_MAP.GET_RECORD");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("ISSUING_CNTRY", model.getIssuingCntry());
    q.setParameter("CLUSTER_ID", model.getApCustClusterId());
    q.setParameter("CLIENT_TIER_CD", model.getClientTierCd());
    q.setParameter("ISU_CD", model.getIsuCode());
    return q.getSingleResult(ApCustClusterTierMap.class);
  }

  @Override
  protected ApCustClusterTierMap createFromModel(ApCustClusterTierMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    // TODO Auto-generated method stub
    ApCustClusterTierMap ap = new ApCustClusterTierMap();
    ap.setId(new ApCustClusterTierMapPK());
    copyValuesToEntity(model, ap);
    return ap;
  }

}
