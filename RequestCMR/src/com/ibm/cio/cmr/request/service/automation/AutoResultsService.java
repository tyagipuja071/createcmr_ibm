/**
 * 
 */
package com.ibm.cio.cmr.request.service.automation;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.AutomationResults;
import com.ibm.cio.cmr.request.entity.AutomationResultsPK;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.model.automation.AutoResultsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * Service class to handle automation results
 * 
 * @author JeffZAMORA
 * 
 */
@Component
public class AutoResultsService extends BaseService<AutoResultsModel, AutomationResults> {

  @Override
  protected void performTransaction(AutoResultsModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<AutoResultsModel> doSearch(AutoResultsModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("AUTOMATION.UI.GET_RESULTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setForReadOnly(true);

    List<CompoundEntity> results = query.getCompundResults(AutomationResults.class, AutomationResults.AUTO_RESULTS_MAPPING);

    List<AutoResultsModel> resultList = new ArrayList<AutoResultsModel>();
    AutoResultsModel resultModel = null;
    AutomationResults result = null;
    for (CompoundEntity compoundResult : results) {
      result = compoundResult.getEntity(AutomationResults.class);

      resultModel = new AutoResultsModel();
      copyValuesFromEntity(result, resultModel);
      resultModel.setReqId(result.getId().getReqId());
      resultModel.setProcessCd(result.getId().getProcessCd());
      resultModel.setAutomationResultId(result.getId().getAutomationResultId());
      resultModel.setMatchImport((String) compoundResult.getValue("MATCH_IMPORT"));
      resultModel.setOverrideImport((String) compoundResult.getValue("OVERRIDE_IMPORT"));
      resultList.add(resultModel);
    }
    return resultList;
  }

  @Override
  protected AutomationResults getCurrentRecord(AutoResultsModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    AutomationResultsPK pk = new AutomationResultsPK();
    pk.setAutomationResultId(model.getAutomationResultId());
    pk.setProcessCd(model.getProcessCd());
    pk.setReqId(model.getReqId());
    return entityManager.find(AutomationResults.class, pk);
  }

  @Override
  protected AutomationResults createFromModel(AutoResultsModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    AutomationResults results = new AutomationResults();
    copyValuesToEntity(model, results);
    return results;
  }

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(AutoResultsService.class);
  }

}
