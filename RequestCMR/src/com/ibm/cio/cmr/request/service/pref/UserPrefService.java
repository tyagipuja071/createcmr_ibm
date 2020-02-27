/**
 * 
 */
package com.ibm.cio.cmr.request.service.pref;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.UserPref;
import com.ibm.cio.cmr.request.entity.UserPrefPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.pref.UserPrefModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * Service to process User Preferences
 * 
 * @author Jeffrey Zamora
 * 
 */
@Component
public class UserPrefService extends BaseService<UserPrefModel, UserPref> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(UserPrefService.class);
  }

  @Override
  protected void performTransaction(UserPrefModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // not implemented
  }

  @Override
  protected List<UserPrefModel> doSearch(UserPrefModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    UserPref pref = getCurrentRecord(model, entityManager, request);
    List<UserPrefModel> list = new ArrayList<UserPrefModel>();
    if (pref != null) {
      UserPrefModel prefm = new UserPrefModel();
      copyValuesFromEntity(pref, prefm);
      prefm.setManagerName(model.getManagerName());
      prefm.setState(BaseModel.STATE_EXISTING);
      list.add(prefm);
    }
    return list;
  }

  @Override
  protected UserPref getCurrentRecord(UserPrefModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("USERPREF.GETRECORD");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQUESTER_ID", model.getRequesterId());

    List<UserPref> rs = query.getResults(1, UserPref.class);
    return rs.size() > 0 ? rs.get(0) : null;
  }

  @Override
  protected UserPref createFromModel(UserPrefModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    UserPref pref = new UserPref();
    UserPrefPK prefPK = new UserPrefPK();
    pref.setId(prefPK);
    copyValuesToEntity(model, pref);
    return pref;
  }

}
