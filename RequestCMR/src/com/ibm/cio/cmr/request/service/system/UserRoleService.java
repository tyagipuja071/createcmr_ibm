/**
 * 
 */
package com.ibm.cio.cmr.request.service.system;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.UserRoles;
import com.ibm.cio.cmr.request.entity.UserRolesPK;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.system.UserRoleModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class UserRoleService extends BaseService<UserRoleModel, UserRoles> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(UserRoleService.class);
  }

  @Override
  protected void performTransaction(UserRoleModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    AppUser user = AppUser.getUser(request);
    if ("REMOVE_ROLES".equals(model.getMassAction())) {
      String sql = ExternalizedQuery.getSql("SYSTEM.REMOVE_ROLE");
      PreparedQuery q = new PreparedQuery(entityManager, sql);
      List<KeyContainer> keys = extractKeys(model);

      for (KeyContainer key : keys) {
        q.setParameter("USER_ID", key.getKey("userId"));
        q.setParameter("ROLE_ID", key.getKey("roleId"));
        q.setParameter("SUB_ROLE_ID", StringUtils.isEmpty(key.getKey("subRoleId")) ? "" : key.getKey("subRoleId"));
        log.debug("Removing role " + key.getKey("roleId") + " (" + key.getKey("subRoleId") + ") from " + key.getKey("userId"));
        q.executeSql();

        SystemUtil.logSystemAdminAction(entityManager, user, "USER_ROLES", "D", key.getKey("userId"), "", key.getKey("roleId"), "");
      }

    } else if ("ADD_ROLES".equals(model.getAction())) {
      String rolesToAdd = model.getRolesToAdd();
      String[] rolePairs = rolesToAdd.split(",");
      String[] role = null;

      UserRolesPK pk = null;
      UserRoles userRole = null;
      for (String pair : rolePairs) {
        role = pair.split(":");
        if (role.length > 0) {
          pk = new UserRolesPK();
          pk.setUserId(model.getUserId());
          pk.setRoleId(role[0]);
          if (role.length == 2) {
            pk.setSubRoleId(role[1]);
          } else {
            pk.setSubRoleId("");
          }

          userRole = new UserRoles();
          userRole.setId(pk);
          userRole.setCntryCd("ALL");
          userRole.setComments(model.getComments());
          userRole.setCreateBy(user.getIntranetId());
          userRole.setCreateTs(this.currentTimestamp);
          userRole.setStatus("1");
          userRole.setUpdateBy(user.getIntranetId());
          userRole.setUpdateTs(this.currentTimestamp);

          this.log.debug("Adding " + pk.getRoleId() + " (" + pk.getSubRoleId() + ") role to " + pk.getUserId());
          createEntity(userRole, entityManager);

          SystemUtil.logSystemAdminAction(entityManager, user, "USER_ROLES", "I", model.getUserId(), "", "",
              pk.getRoleId() + " | " + pk.getSubRoleId());

        }
      }

    }
  }

  @Override
  protected List<UserRoleModel> doSearch(UserRoleModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.GET_ROLES");
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT"));
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("USER_ID", model.getUserId());
    List<CompoundEntity> roles = q.getCompundResults(UserRoles.class, UserRoles.USER_ROLE_MAPPING);
    List<UserRoleModel> list = new ArrayList<>();
    UserRoleModel newModel = null;
    UserRoles role = null;
    for (CompoundEntity crole : roles) {
      role = crole.getEntity(UserRoles.class);
      newModel = new UserRoleModel();
      newModel.setCntryCode(role.getCntryCd());
      newModel.setUserId(role.getId().getUserId());
      newModel.setRoleId(role.getId().getRoleId());
      newModel.setSubRoleId(role.getId().getSubRoleId());
      newModel.setCreateBy(role.getCreateBy());
      newModel.setUpdateBy(role.getUpdateBy());
      newModel.setCreateTsString(formatter.format(role.getCreateTs()));
      newModel.setUpdateTsString(formatter.format(role.getUpdateTs()));
      newModel.setComments(role.getComments());

      newModel.setRoleDesc((String) crole.getValue("ROLE_DESC"));
      newModel.setSubRoleDesc((String) crole.getValue("SUB_ROLE_DESC"));
      newModel.setRoleStatus((String) crole.getValue("ROLE_STATUS"));
      list.add(newModel);
    }
    return list;
  }

  @Override
  protected UserRoles getCurrentRecord(UserRoleModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected UserRoles createFromModel(UserRoleModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
