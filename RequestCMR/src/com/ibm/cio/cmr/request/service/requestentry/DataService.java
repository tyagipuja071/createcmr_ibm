package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Sonali Jain
 * 
 */
@Component
public class DataService extends BaseService<RequestEntryModel, Data> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(DataService.class);
  }

  @Override
  protected void performTransaction(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop
  }

  @Override
  protected List<RequestEntryModel> doSearch(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<RequestEntryModel> results = new ArrayList<RequestEntryModel>();

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.DATA.SEARCH_BY_REQID");

    PreparedQuery query = new PreparedQuery(entityManager, sql);

    query.setParameter("REQ_ID", model.getReqId());

    List<Data> rs = query.getResults(1, Data.class);

    RequestEntryModel reqEntryModel = null;
    for (Data data : rs) {
      reqEntryModel = new RequestEntryModel();
      copyValuesFromEntity(data, reqEntryModel);

      results.add(reqEntryModel);
    }

    return results;

  }

  @Override
  protected Data getCurrentRecord(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.DATA.SEARCH_BY_REQID");

    PreparedQuery query = new PreparedQuery(entityManager, sql);

    query.setParameter("REQ_ID", model.getReqId());

    List<Data> rs = query.getResults(1, Data.class);

    if (rs != null && rs.size() > 0) {
      return rs.get(0);
    }
    return null;
  }

  @Override
  protected Data createFromModel(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    Data data = new Data();
    DataPK pk = new DataPK();
    data.setId(pk);
    copyValuesToEntity(model, data);
    return data;
  }

  public Data getCurrentRecordById(long reqId, EntityManager entityManager) throws Exception {
    String sql = ExternalizedQuery.getSql("DATA.GET.RECORD.BYID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Data> rs = query.getResults(1, Data.class);

    if (rs != null && rs.size() > 0) {
      return rs.get(0);
    }
    return null;
  }

}
