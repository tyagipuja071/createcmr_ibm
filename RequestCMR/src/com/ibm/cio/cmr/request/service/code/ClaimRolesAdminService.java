/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.ClaimRoles;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.model.code.ClaimRoleModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Eduard Bernardo
 * 
 */
@Component
public class ClaimRolesAdminService extends BaseService<ClaimRoleModel, ClaimRoles> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ClaimRolesAdminService.class);
  }

  @Override
  protected void doBeforeInsert(ClaimRoles entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {

  }

  @Override
  protected void doBeforeUpdate(ClaimRoles entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {

  }

  @Override
  protected void performTransaction(ClaimRoleModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

  }

  @Override
  protected List<ClaimRoleModel> doSearch(ClaimRoleModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.GET.CLAIMROLESLIST");
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT"));
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);
    List<CompoundEntity> result = q.getCompundResults(ClaimRoles.class, ClaimRoles.CLAIM_ROLES_LIST_MAPPING);
    List<ClaimRoleModel> claimRoleModels = new ArrayList<>();
    ClaimRoleModel claimRoleModel = null;
    ClaimRoles claimRole = null;
    String country = null;
    String reqStatusDesc = null;
    String claimRoleDesc = null;
    String claimSubRoleDesc = null;
    String internalTypDesc = null;

    for (CompoundEntity ent : result) {
      claimRole = ent.getEntity(ClaimRoles.class);
      country = (String) ent.getValue("COUNTRY");
      reqStatusDesc = (String) ent.getValue("REQ_STATUS_DESC");
      claimRoleDesc = (String) ent.getValue("CLAIM_ROLE_DESC");
      claimSubRoleDesc = (String) ent.getValue("CLAIM_SUB_ROLE_DESC");
      internalTypDesc = (String) ent.getValue("INTERNAL_TYP_DESC");
      claimRoleModel = new ClaimRoleModel();
      copyValuesFromEntity(claimRole, claimRoleModel);
      claimRoleModel.setCountry(country);
      claimRoleModel.setReqStatusDesc(reqStatusDesc);
      claimRoleModel.setClaimRoleDesc(claimRoleDesc);
      claimRoleModel.setClaimSubRoleDesc(claimSubRoleDesc);
      claimRoleModel.setInternalTypDesc(internalTypDesc);
      claimRoleModel.setCreateTsString(formatter.format(claimRole.getCreateTs()));
      claimRoleModel.setUpdateTsString(formatter.format(claimRole.getUpdateTs()));
      claimRoleModels.add(claimRoleModel);
    }
    return claimRoleModels;
  }

  @Override
  protected ClaimRoles getCurrentRecord(ClaimRoleModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected ClaimRoles createFromModel(ClaimRoleModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
