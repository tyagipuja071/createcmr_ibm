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
import com.ibm.cio.cmr.request.entity.Users;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class UserAdminService extends BaseService<UserModel, Users> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(UserAdminService.class);
  }

  @Override
  protected void performTransaction(UserModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<UserModel> doSearch(UserModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = "select * from CMMA.USERS where ";
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT"));
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    String userId = model.getUserId();
    String userName = model.getUserName();
    q.append(" 1 = 1");
    if (StringUtils.isBlank(userId) && StringUtils.isBlank(userName) && StringUtils.isBlank(model.getRole())
        && StringUtils.isBlank(model.getRequesterFor())) {
      // do not retrieve
      q.append(" and USER_ID = 'xxx'");
    } else {
      if (!StringUtils.isBlank(userName)) {
        q.append(" and upper(USER_NAME) like :USER_NAME ");
        q.setParameter("USER_NAME", "%" + userName.toUpperCase() + "%");
      }
      if (!StringUtils.isBlank(userId)) {
        q.append(" and upper(USER_ID) like :USER_ID ");
        q.setParameter("USER_ID", "%" + userId.toUpperCase() + "%");
      }

      if (!StringUtils.isBlank(model.getRole())) {
        q.append(" and exists (select 1 from CMMA.USER_ROLES r where CMMA.USERS.USER_ID = r.USER_ID and r.ROLE_ID = :ROLE) ");
        q.setParameter("ROLE", model.getRole());
      }

      if (!StringUtils.isBlank(model.getRequesterFor())) {
        q.append(
            " and exists (select 1 from CREQCMR.ADMIN a, CREQCMR.DATA d where a.REQ_ID = d.REQ_ID and a.REQ_STATUS in ('PRJ', 'COM', 'AUT', 'RET', 'REP', 'CPN', 'PPN') and d.CMR_ISSUING_CNTRY = :COUNTRY and lower(a.REQUESTER_ID) = lower(CMMA.USERS.USER_ID))");
        q.setParameter("COUNTRY", model.getRequesterFor());
      }

    }

    q.append(" and nvl(COMP_CD,'') <> 'GTS'");
    q.append(" order by USER_NAME");
    q.setForReadOnly(true);
    List<Users> users = q.getResults(Users.class);
    List<UserModel> list = new ArrayList<>();
    UserModel userModel = null;
    for (Users user : users) {
      userModel = new UserModel();
      userModel.setUserId(user.getId().getUserId());
      userModel.setComments(user.getComments());
      userModel.setCreateBy(user.getCreateBy());
      userModel.setCreateTsString(formatter.format(user.getCreateTs()));
      userModel.setStatus(user.getStatus());
      userModel.setUpdateBy(user.getUpdateBy());
      userModel.setUpdateTsString(formatter.format(user.getUpdateTs()));
      userModel.setUserName(user.getUserName());
      list.add(userModel);
    }
    return list;
  }

  @Override
  protected Users getCurrentRecord(UserModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected Users createFromModel(UserModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
