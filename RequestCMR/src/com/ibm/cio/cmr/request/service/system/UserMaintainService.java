/**
 * 
 */
package com.ibm.cio.cmr.request.service.system;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Users;
import com.ibm.cio.cmr.request.entity.UsersPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class UserMaintainService extends BaseService<UserModel, Users> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(UserMaintainService.class);
  }

  @Override
  protected void performTransaction(UserModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<UserModel> doSearch(UserModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    Users user = getCurrentRecord(model, entityManager, request);
    UserModel newModel = new UserModel();
    copyValuesFromEntity(user, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected Users getCurrentRecord(UserModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String id = model.getUserId();
    String sql = ExternalizedQuery.getSql("SYSTEM.USER");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("ID", id);
    return q.getSingleResult(Users.class);
  }

  @Override
  protected Users createFromModel(UserModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    Users user = new Users();
    UsersPK pk = new UsersPK();
    pk.setUserId(model.getUserId());
    user.setId(pk);
    user.setComments(model.getComments());
    user.setStatus(model.getStatus());
    user.setUserName(model.getUserName());
    return user;
  }

  @Override
  protected void doBeforeInsert(Users entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    if (entity.getId() != null && entity.getId().getUserId() != null) {
      entity.getId().setUserId(entity.getId().getUserId().toLowerCase());
    }
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);
  }

  @Override
  protected void doBeforeUpdate(Users entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
