package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.MassUpdtDataPK;
import com.ibm.cio.cmr.request.model.requestentry.MassUpdateModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Eduard Bernardo
 * 
 */
@Component
public class MassUpdtDataService extends BaseService<MassUpdateModel, MassUpdtData> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(MassUpdtDataService.class);
  }

  @Override
  protected void performTransaction(MassUpdateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop
  }

  @Override
  protected List<MassUpdateModel> doSearch(MassUpdateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<MassUpdateModel> results = new ArrayList<MassUpdateModel>();

    String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.DATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", model.getParReqId());
    query.setParameter("ITERATION_ID", model.getIterationId());
    query.setParameter("SEQ_NO", model.getSeqNo());
    List<MassUpdtData> rs = query.getResults(MassUpdtData.class);

    MassUpdateModel massModel = null;
    for (MassUpdtData massUpdtData : rs) {
      massModel = new MassUpdateModel();
      copyValuesFromEntity(massUpdtData, massModel);

      results.add(massModel);
    }

    return results;

  }

  @Override
  protected MassUpdtData createFromModel(MassUpdateModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    MassUpdtData massUpdtData = new MassUpdtData();
    MassUpdtDataPK pk = new MassUpdtDataPK();
    massUpdtData.setId(pk);
    copyValuesToEntity(model, massUpdtData);
    return massUpdtData;
  }

  @Override
  protected MassUpdtData getCurrentRecord(MassUpdateModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  public void doSearchById(EntityManager entityManager, MassUpdateModel model) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.DATA.BY.ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", model.getParReqId());
    query.setParameter("ITERATION_ID", model.getIterationId());
    query.setParameter("SEQ_NO", model.getSeqNo());
    MassUpdtData massUpdtData = query.getSingleResult(MassUpdtData.class);

    copyValuesFromEntity(massUpdtData, model);
  }

  public void completeMassRequest(EntityManager entityManager, long parReqId) {
    String sql = ExternalizedQuery.getSql("COMPLETE.MASS.UPDATE.DATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", parReqId);
    query.executeSql();

  }

  public void setToReady(EntityManager entityManager, long parReqId, int iterationId) {
    String sql = ExternalizedQuery.getSql("READY.MASS.UPDATE.DATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", parReqId);
    query.setParameter("ITERATION_ID", iterationId);
    query.executeSql();
  }

  public MassUpdtData getCurrentRecordByIdEnt(String reqId, EntityManager entityManager) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.DATA.BY.ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", reqId);
    query.setParameter("ITERATION_ID", 0);
    query.setParameter("SEQ_NO", 0);

    List<MassUpdtData> rs = query.getResults(1, MassUpdtData.class);

    if (rs != null && rs.size() > 0) {
      return rs.get(0);
    }
    return null;
  }

  protected List<MassUpdateModel> doSearchCurrentMassUpdRec(long parReqId, int iterationId, EntityManager entityManager) throws Exception {
    List<MassUpdateModel> results = new ArrayList<MassUpdateModel>();

    String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.DATA.BY.ID.EMEA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", parReqId);
    query.setParameter("ITERATION_ID", iterationId);
    List<MassUpdtData> rs = query.getResults(MassUpdtData.class);

    MassUpdateModel massModel = null;
    for (MassUpdtData massUpdtData : rs) {
      massModel = new MassUpdateModel();
      copyValuesFromEntity(massUpdtData, massModel);

      results.add(massModel);
    }

    return results;

  }
}
