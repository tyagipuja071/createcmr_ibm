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
import com.ibm.cio.cmr.request.entity.ClaimRoles;
import com.ibm.cio.cmr.request.entity.ClaimRolesPK;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.ClaimRoleModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Eduard Bernardo
 * 
 */
@Component
public class ClaimRolesMaintainService extends BaseService<ClaimRoleModel, ClaimRoles> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ClaimRolesMaintainService.class);
  }

  @Override
  protected void doBeforeInsert(ClaimRoles entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);
  }

  @Override
  protected void doBeforeUpdate(ClaimRoles entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);
  }

  @Override
  protected void performTransaction(ClaimRoleModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<ClaimRoleModel> doSearch(ClaimRoleModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    String internalTyp = model.getInternalTyp();
    String reqStatus = model.getReqStatus();
    String sql = ExternalizedQuery.getSql("SYSTEM.CLAIM_ROLES");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    q.setParameter("INTERNAL_TYP", internalTyp);
    q.setParameter("REQ_STATUS", reqStatus);
    List<CompoundEntity> result = q.getCompundResults(1, ClaimRoles.class, ClaimRoles.CLAIM_ROLES_MAPPING);

    ClaimRoleModel claimRoleModel = null;
    ClaimRoles claimRole = null;
    String country = null;
    String reqStatusDesc = null;
    String internalTypDesc = null;
    if (result != null && !result.isEmpty()) {
      claimRole = result.get(0).getEntity(ClaimRoles.class);
      country = (String) result.get(0).getValue("COUNTRY");
      reqStatusDesc = (String) result.get(0).getValue("REQ_STATUS_DESC");
      internalTypDesc = (String) result.get(0).getValue("INTERNAL_TYP_DESC");

      claimRoleModel = new ClaimRoleModel();
      copyValuesFromEntity(claimRole, claimRoleModel);
      claimRoleModel.setCountry(country);
      claimRoleModel.setReqStatusDesc(reqStatusDesc);
      claimRoleModel.setInternalTypDesc(internalTypDesc);
      claimRoleModel.setState(BaseModel.STATE_EXISTING);
    }

    return Collections.singletonList(claimRoleModel);
  }

  @Override
  protected ClaimRoles getCurrentRecord(ClaimRoleModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    String internalTyp = model.getInternalTyp();
    String reqStatus = model.getReqStatus();
    String sql = ExternalizedQuery.getSql("SYSTEM.CLAIM_ROLES");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    q.setParameter("INTERNAL_TYP", internalTyp);
    q.setParameter("REQ_STATUS", reqStatus);
    return q.getSingleResult(ClaimRoles.class);
  }

  @Override
  protected ClaimRoles createFromModel(ClaimRoleModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    ClaimRoles claimRole = new ClaimRoles();
    claimRole.setId(new ClaimRolesPK());
    copyValuesToEntity(model, claimRole);
    return claimRole;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
