package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Roles;
import com.ibm.cio.cmr.request.entity.RolesPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.RolesModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class RolesMaintainService extends BaseService<RolesModel, Roles> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(RolesMaintainService.class);
  }

  @Override
  protected void performTransaction(RolesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<RolesModel> doSearch(RolesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    Roles roles = getCurrentRecord(model, entityManager, request);
    RolesModel newModel = new RolesModel();
    copyValuesFromEntity(roles, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    if (newModel.getStatus() != null && newModel.getStatus().length() > 0) {
      newModel.setStatusDefinition(newModel.getStatus().equals("1") ? "Active" : "Inactive");
    }
    if (newModel.getApplicationCd() != null && newModel.getApplicationCd().length() > 0) {
      switch (newModel.getApplicationCd()) {
      case "R":
        newModel.setApplicationCdDefinition("Request CMR");
        break;
      case "C":
        newModel.setApplicationCdDefinition("Create CMR");
        break;
      case "F":
        newModel.setApplicationCdDefinition("Find CMR");
        break;
      case "RCF":
        newModel.setApplicationCdDefinition("All");
        break;
      }
    }

    return Collections.singletonList(newModel);
  }

  @Override
  protected Roles getCurrentRecord(RolesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String roleId = model.getRoleId();
    String sql = ExternalizedQuery.getSql("SYSTEM.ROLES_BY_ROLES_ID");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("ROLE_ID", roleId);
    return q.getSingleResult(Roles.class);
  }

  @Override
  protected Roles createFromModel(RolesModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    Roles roles = new Roles();
    roles.setId((new RolesPK()));
    copyValuesToEntity(model, roles);
    return roles;
  }

  @Override
  protected void doBeforeInsert(Roles entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdateTs(this.currentTimestamp);
  }

  @Override
  protected void doBeforeUpdate(Roles entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    entity.setUpdateTs(this.currentTimestamp);
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
