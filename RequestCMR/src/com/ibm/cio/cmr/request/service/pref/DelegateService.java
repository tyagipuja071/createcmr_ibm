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
import com.ibm.cio.cmr.request.entity.Delegate;
import com.ibm.cio.cmr.request.entity.DelegatePK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.pref.DelegateModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class DelegateService extends BaseService<DelegateModel, Delegate> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(DelegateService.class);
  }

  @Override
  protected void performTransaction(DelegateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String action = model.getAction();

    AppUser user = AppUser.getUser(request);

    if ("ADD_DELEGATE".equals(action)) {

      if (delegateExists(entityManager, model.getUserId(), model.getDelegateId())) {
        throw new CmrException(MessageUtil.ERROR_ALREADY_DELEGATE, model.getDelegateNm());
      }

      if (user.getIntranetId().equals(model.getDelegateId())) {
        throw new CmrException(MessageUtil.ERROR_CANNOT_ADD_YOURSELF_AS_DELEGATE);
      }

      Delegate delegate = createFromModel(model, entityManager, request);
      delegate.getId().setDelegateId(delegate.getId().getDelegateId().toLowerCase());
      delegate.setCreateTs(SystemUtil.getCurrentTimestamp());
      createEntity(delegate, entityManager);
    } else if ("ADD_MGR".equals(action)) {
      String employeeId = user.getUserCnum();
      String mgrEmail = BluePagesHelper.getManagerEmail(employeeId);
      if (mgrEmail == null) {
        throw new CmrException(MessageUtil.ERROR_MGR_CANNOT_BE_RETRIEVED);
      }
      Person mgr = BluePagesHelper.getPerson(mgrEmail);
      if (mgr == null) {
        throw new CmrException(MessageUtil.ERROR_MGR_CANNOT_BE_RETRIEVED);
      }
      String mgrName = mgr.getName();
      model.setDelegateId(mgrEmail.toLowerCase());
      model.setDelegateNm(mgrName);

      if (delegateExists(entityManager, model.getUserId(), model.getDelegateId())) {
        throw new CmrException(MessageUtil.ERROR_ALREADY_DELEGATE, model.getDelegateNm());
      }

      Delegate delegate = createFromModel(model, entityManager, request);
      delegate.setCreateTs(SystemUtil.getCurrentTimestamp());
      createEntity(delegate, entityManager);
    } else if ("REMOVE_DELEGATE".equals(action)) {
      Delegate delegate = getCurrentRecord(model, entityManager, request);
      deleteEntity(delegate, entityManager);
    }
  }

  private boolean delegateExists(EntityManager entityManager, String userId, String delegateId) {
    String sql = ExternalizedQuery.getSql("USERPREF.GETDELEGATE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("USER_ID", userId);
    query.setParameter("DELEGATE_ID", delegateId);
    return query.exists();
  }

  @Override
  protected List<DelegateModel> doSearch(DelegateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<DelegateModel> results = new ArrayList<DelegateModel>();

    String sql = ExternalizedQuery.getSql("USERPREF.GETDELEGATES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("USER_ID", model.getUserId());
    query.setForReadOnly(true);

    List<Delegate> rs = query.getResults(50, Delegate.class);
    DelegateModel delModel = null;
    for (Delegate delegate : rs) {
      delModel = new DelegateModel();
      copyValuesFromEntity(delegate, delModel);
      delModel.setState(BaseModel.STATE_EXISTING);
      results.add(delModel);
    }

    return results;
  }

  @Override
  protected Delegate getCurrentRecord(DelegateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("USERPREF.GETDELEGATE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("USER_ID", model.getUserId());
    query.setParameter("DELEGATE_ID", model.getDelegateId());
    List<Delegate> delegates = query.getResults(1, Delegate.class);
    if (delegates != null && delegates.size() > 0) {
      return delegates.get(0);
    }
    return null;
  }

  @Override
  protected Delegate createFromModel(DelegateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    Delegate del = new Delegate();
    DelegatePK pk = new DelegatePK();
    del.setId(pk);
    copyValuesToEntity(model, del);
    return del;
  }

}
