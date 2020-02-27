/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.NotifList;
import com.ibm.cio.cmr.request.entity.NotifListPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.requestentry.NotifyListModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class NotifyListService extends BaseService<NotifyListModel, NotifList> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(NotifyListService.class);
  }

  @Override
  protected void performTransaction(NotifyListModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String action = model.getAction();

    AppUser user = AppUser.getUser(request);
    String dupChkProcess = request.getAttribute("dupReqChkProcess") != null ? request.getAttribute("dupReqChkProcess").toString() : "";

    if ("ADD_NOTIFY".equals(action)) {

      if (notifyExists(entityManager, model.getNotifId(), model.getReqId())) {
        throw new CmrException(MessageUtil.ERROR_ALREADY_NOTIFY, model.getNotifNm());
      }

      if (user.getIntranetId().equals(model.getNotifId()) && dupChkProcess != "Y") {
        throw new CmrException(MessageUtil.ERROR_CANNOT_ADD_YOURSELF_AS_NOTIFY);
      }

      NotifList delegate = createFromModel(model, entityManager, request);
      createEntity(delegate, entityManager);
    } else if ("REMOVE_NOTIF".equals(action)) {
      NotifList NotifList = getCurrentRecord(model, entityManager, request);
      deleteEntity(NotifList, entityManager);
    }

  }

  @Override
  protected List<NotifyListModel> doSearch(NotifyListModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.NOTIFLIST.SEARCH");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("REQ_ID", model.getReqId());
    query.setParameter("REQUESTER_ID", AppUser.getUser(request).getIntranetId());

    List<CompoundEntity> records = query.getCompundResults(NotifList.class, NotifList.NOTIF_LIST_MAPPING);

    List<NotifyListModel> list = new ArrayList<NotifyListModel>();
    NotifyListModel notifModel = null;
    NotifList notif = null;
    for (CompoundEntity compound : records) {
      notif = compound.getEntity(NotifList.class);
      notifModel = new NotifyListModel();
      copyValuesFromEntity(notif, notifModel);

      notifModel.setNoEmail((String) compound.getValue("RECEIVE_MAIL_IND"));
      notifModel.setRemovable((String) compound.getValue("REMOVABLE"));
      notifModel.setState(BaseModel.STATE_EXISTING);

      list.add(notifModel);
    }
    return list;
  }

  @Override
  protected NotifList getCurrentRecord(NotifyListModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("NOTIFY.GETRECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setParameter("NOTIF_ID", model.getNotifId());
    List<NotifList> notifyList = query.getResults(1, NotifList.class);
    if (notifyList != null && notifyList.size() > 0) {
      return notifyList.get(0);
    }
    return null;
  }

  @Override
  protected NotifList createFromModel(NotifyListModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    NotifList list = new NotifList();
    NotifListPK pk = new NotifListPK();
    list.setId(pk);
    copyValuesToEntity(model, list);
    return list;
  }

  private boolean notifyExists(EntityManager entityManager, String notifyId, long reqId) {
    String sql = ExternalizedQuery.getSql("NOTIFY.GETRECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("NOTIF_ID", notifyId);
    return query.exists();
  }

}
