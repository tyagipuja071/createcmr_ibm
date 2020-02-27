/**
 * 
 */
package com.ibm.cio.cmr.request.service.login;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.entity.UserRoles;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class LoginService extends BaseSimpleService<List<UserRoles>> {

  @Override
  protected List<UserRoles> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String sql = ExternalizedQuery.getSql("LOGIN.GET_ROLES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("USER_ID", params.getParam("USER_ID"));
    query.setForReadOnly(true);
    List<UserRoles> roleList = query.getResults(UserRoles.class);
    return roleList;
  }

}
