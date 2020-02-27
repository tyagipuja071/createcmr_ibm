package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.ScorecardPK;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * Handles Scorecard Transactions
 * 
 * @author Jeffrey Zamora
 * 
 */
@Component
public class ScorecardService extends BaseService<RequestEntryModel, Scorecard> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ScorecardService.class);
  }

  @Override
  protected void performTransaction(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop
  }

  @Override
  protected List<RequestEntryModel> doSearch(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<RequestEntryModel> results = new ArrayList<RequestEntryModel>();

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.SCORECARD.SEARCH_BY_REQID");

    PreparedQuery query = new PreparedQuery(entityManager, sql);

    query.setParameter("REQ_ID", model.getReqId());

    List<Scorecard> rs = query.getResults(1, Scorecard.class);

    RequestEntryModel reqEntryModel = null;
    for (Scorecard scorecard : rs) {
      reqEntryModel = new RequestEntryModel();
      copyValuesFromEntity(scorecard, reqEntryModel);

      results.add(reqEntryModel);
    }

    return results;

  }

  @Override
  protected Scorecard getCurrentRecord(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.SCORECARD.SEARCH_BY_REQID");

    PreparedQuery query = new PreparedQuery(entityManager, sql);

    query.setParameter("REQ_ID", model.getReqId());

    List<Scorecard> rs = query.getResults(1, Scorecard.class);

    if (rs != null && rs.size() > 0) {
      return rs.get(0);
    }
    return null;
  }

  @Override
  protected Scorecard createFromModel(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    Scorecard score = new Scorecard();
    ScorecardPK pk = new ScorecardPK();
    score.setId(pk);
    copyValuesToEntity(model, score);
    return score;
  }
}
