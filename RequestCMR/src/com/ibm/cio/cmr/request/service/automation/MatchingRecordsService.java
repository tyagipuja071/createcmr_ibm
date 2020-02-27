package com.ibm.cio.cmr.request.service.automation;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationOutput;
import com.ibm.cio.cmr.request.automation.out.MatchingOutput;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.AutomationMatchingPK;
import com.ibm.cio.cmr.request.model.automation.AutoResImportModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * Service class to handle matching results
 * 
 * @author PoojaTyagi
 * 
 */
@Component
public class MatchingRecordsService extends BaseService<AutoResImportModel, AutomationMatching> {

  private static final Logger LOG = Logger.getLogger(MatchingRecordsService.class);

  @Override
  protected void performTransaction(AutoResImportModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    AutomationOutput out = null;
    if ("DATA_OVERRIDE".equals(model.getAction())) {
      out = new OverrideOutput(false);
    } else if ("MATCH_IMPORT".equals(model.getAction())) {
      out = new MatchingOutput();
    }

    String processCd = request.getParameter("processCd");
    String automationResID = request.getParameter("automationResultId");
    String reqId = request.getParameter("requestId");
    long requestId = -1;
    if (StringUtils.isNotBlank(reqId) && StringUtils.isNumeric(reqId)) {
      requestId = Long.parseLong(reqId);
    }
    String itemNoString = request.getParameter("itemNo");
    long itemNo = -1;
    if (StringUtils.isNotBlank(itemNoString) && StringUtils.isNumeric(itemNoString)) {
      itemNo = Long.parseLong(itemNoString);
    }
    long automationResultId = -1;
    if (StringUtils.isNotBlank(automationResID) && StringUtils.isNumeric(automationResID)) {
      automationResultId = Long.parseLong(automationResID);
    }
    if (requestId != -1 && automationResultId != -1) {
      RequestData requestData = new RequestData(entityManager, model.getRequestId());
      AppUser user = AppUser.getUser(request);
      if (out != null)
        out.apply(entityManager, user, requestData, automationResultId, itemNo, processCd, false);
      LOG.debug("Match Applied!");
    } else {
      throw new Exception("Invalid Request ID / Automation Result ID .");
    }
  }

  @Override
  protected List<AutoResImportModel> doSearch(AutoResImportModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("AUTOMATION.GET_MATCHES_IMPORT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    String processCd = request.getParameter("processCd");
    String automationResID = request.getParameter("automationResultId");
    String reqId = request.getParameter("requestId");
    long requestId = -1;
    if (StringUtils.isNotBlank(reqId) && StringUtils.isNumeric(reqId)) {
      requestId = Long.parseLong(reqId);
    }
    query.setParameter("AUTOMATION_RESULT_ID", automationResID);
    query.setParameter("PROCESS_CD", processCd);
    query.setForReadOnly(true);

    List<AutomationMatching> results = query.getResults(AutomationMatching.class);

    List<AutoResImportModel> resultList = new ArrayList<AutoResImportModel>();
    AutoResImportModel resultModel = null;
    for (AutomationMatching matchingResult : results) {
      resultModel = new AutoResImportModel();
      resultModel.setAutomationResultId(matchingResult.getId().getAutomationResultId());
      resultModel.setItemNo(matchingResult.getId().getItemNo());
      resultModel.setProcessCd(matchingResult.getId().getProcessCd());
      resultModel.setMatchKeyValue(matchingResult.getId().getMatchKeyValue());
      resultModel.setMatchGradeValue(matchingResult.getMatchGradeValue());
      resultModel.setImportedIndc(matchingResult.getImportedIndc());
      resultModel.setRequestId(requestId);
      resultList.add(resultModel);
    }
    return resultList;
  }

  @Override
  protected AutomationMatching getCurrentRecord(AutoResImportModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    AutomationMatchingPK pk = new AutomationMatchingPK();
    pk.setAutomationResultId(model.getAutomationResultId());
    pk.setProcessCd(model.getProcessCd());
    pk.setAutomationResultId(model.getAutomationResultId());
    return entityManager.find(AutomationMatching.class, pk);
  }

  @Override
  protected AutomationMatching createFromModel(AutoResImportModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    AutomationMatching results = new AutomationMatching();
    copyValuesToEntity(model, results);
    return results;
  }

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(AutoResultsService.class);
  }

}
