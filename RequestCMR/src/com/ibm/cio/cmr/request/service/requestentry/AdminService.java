package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rama Mohan
 * 
 */
@Component
public class AdminService extends BaseService<RequestEntryModel, Admin> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(AdminService.class);
  }

  @Override
  protected void performTransaction(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop
  }

  @Override
  protected List<RequestEntryModel> doSearch(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<RequestEntryModel> results = new ArrayList<RequestEntryModel>();

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ADMIN.SEARCH_BY_REQID");

    PreparedQuery query = new PreparedQuery(entityManager, sql);

    query.setParameter("REQ_ID", model.getReqId());

    List<Admin> rs = query.getResults(1, Admin.class);

    RequestEntryModel reqEntryModel = null;
    for (Admin admin : rs) {
      reqEntryModel = new RequestEntryModel();
      copyValuesFromEntity(admin, reqEntryModel);

      results.add(reqEntryModel);
    }

    return results;

  }

  @Override
  protected Admin getCurrentRecord(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ADMIN.SEARCH_BY_REQID");

    PreparedQuery query = new PreparedQuery(entityManager, sql);

    query.setParameter("REQ_ID", model.getReqId());

    List<Admin> rs = query.getResults(1, Admin.class);

    if (rs != null && rs.size() > 0) {
      return rs.get(0);
    }
    return null;
  }

  @Override
  protected Admin createFromModel(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    Admin admin = new Admin();
    AdminPK pk = new AdminPK();
    admin.setId(pk);
    copyValuesToEntity(model, admin);
    return admin;
  }

  public void updateMassFields(EntityManager entityManager, long reqId, int newIterId, String fileName, String massUpdtRdcOnly) throws Exception {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.MASS.UPDATE.FILE");
    PreparedQuery updateQuery = new PreparedQuery(entityManager, sql);
    updateQuery.setParameter("REQ_ID", reqId);
    updateQuery.setParameter("ITERATION_ID", newIterId);
    updateQuery.setParameter("FILE_NAME", fileName);
    updateQuery.setParameter("MASS_UPDT_RDC_ONLY", massUpdtRdcOnly);

    log.debug("Updating Admin records for mass reqId = " + reqId);
    updateQuery.executeSql();
  }

  public Admin getCurrentRecordById(long reqId, EntityManager entityManager) throws Exception {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GET.ADMIN.RECORD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    return query.getSingleResult(Admin.class);
  }
}
