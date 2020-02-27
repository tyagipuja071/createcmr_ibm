/**
 * 
 */
package com.ibm.cio.cmr.request.service.pref;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class RoleService extends BaseSimpleService<List<String>> {

  @Override
  protected List<String> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String sql = ExternalizedQuery.getSql("USER_PREF.ROLES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("USER_ID", params.getParam("USER_ID"));
    List<Object[]> roleList = query.getResults();
    List<String> roles = new ArrayList<String>();
    for (Object[] rec : roleList) {
      roles.add((String) rec[0]);
    }
    return roles;
  }

}
