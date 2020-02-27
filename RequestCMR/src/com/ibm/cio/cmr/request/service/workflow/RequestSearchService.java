/**
 * 
 */
package com.ibm.cio.cmr.request.service.workflow;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.workflow.RequestSearchCriteriaModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Rangoli Saxena
 * 
 */
@Component
public class RequestSearchService extends BaseService<RequestSearchCriteriaModel, Admin> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(WorkflowService.class);
  }

  @Override
  protected void performTransaction(RequestSearchCriteriaModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<RequestSearchCriteriaModel> doSearch(RequestSearchCriteriaModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    List<RequestSearchCriteriaModel> results = new ArrayList<RequestSearchCriteriaModel>();

    // String sql = ExternalizedQuery.getSql("WORKFLOW.OPEN_REQ_LIST");
    //

    String toUse = "WORKFLOW.SRCH_REQ_LIST_NO_CUST";
    String mapping = Admin.WORKFLOW_REQUESTS_MAPPING;
    if (!StringUtils.isEmpty(model.getCustomerName())) {
      toUse = "WORKFLOW.SRCH_REQ_LIST";
      mapping = Admin.WORKFLOW_SEARCH_WITH_CUST_MAPPING;
    }

    AppUser user = AppUser.getUser(request);

    String order = user.isShowLatestFirst() ? "desc" : "asc";

    try {

      PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql(toUse));
      query.setForReadOnly(true);

      StringBuilder sql = new StringBuilder();
      sql.append(ExternalizedQuery.getSql(toUse));
      // SimpleDateFormat dateFormat = CmrConstants.DATE_FORMAT;
      SimpleDateFormat timeFormat = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT") + " HH:mm:ss");

      int resultRows = -1;
      if (model.getResultRows() != null)
        resultRows = Integer.parseInt(model.getResultRows());

      // assign where clause
      if (!StringUtils.isEmpty(model.getWfReqId())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_REQUESTERID"));
        query.setParameter("WF_REQUESTER_ID", model.getWfReqId());
      }
      if (!StringUtils.isEmpty(model.getWfOrgId())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_ORGID"));
        query.setParameter("WF_ORG_ID", model.getWfOrgId());
      }
      if (!StringUtils.isEmpty(model.getWfClaimById())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_CLAIMBYID"));
        query.setParameter("WFCLM_BY_ID", model.getWfClaimById());
      }

      if (!StringUtils.isEmpty(model.getWfProcCentre())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_PROC_CNTR"));
        query.setParameter("PROCESSING_CENTRE", model.getWfProcCentre());
      }
      if (!StringUtils.isEmpty(model.getCmrIssuingCountry())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_CMRISSU_CNTRY"));
        query.setParameter("CMR_ISSU_CNTRY", model.getCmrIssuingCountry());
      }
      if (!StringUtils.isEmpty(model.getCmrNoCriteria())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_CMR_NO"));
        query.setParameter("CMR_NO", model.getCmrNoCriteria());

        model.setCmrOwnerCriteria(null); // do this to automatically handle mass
                                         // change requests
      }
      if (!StringUtils.isEmpty(model.getCmrOwnerCriteria())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_CMR_OWNER"));
        query.setParameter("CMR_OWNER", model.getCmrOwnerCriteria());
      }
      if (!StringUtils.isEmpty(model.getRequestType())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_REQ_TYPE"));
        query.setParameter("REQUEST_TYPE", model.getRequestType());
      }

      if (!StringUtils.isEmpty(model.getCustomerName())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_CUSNAME"));
        if ("C".equals(model.getSearchCusType())) {
          query.setParameter("CUSTOMER_NAME", "%" + model.getCustomerName().toUpperCase() + "%");
        } else if ("B".equals(model.getSearchCusType())) {
          query.setParameter("CUSTOMER_NAME", model.getCustomerName().toUpperCase() + "%");
        } else {
          query.setParameter("CUSTOMER_NAME", model.getCustomerName().toUpperCase());
        }
      }

      if (!StringUtils.isEmpty(model.getProcStatus())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_PROC_STATUS"));
        query.setParameter("PROCESSED_FLAG", model.getProcStatus());
      }

      if (!StringUtils.isEmpty(model.getRequestId())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_REQID"));
        query.setParameter("REQUEST_ID", new BigInteger(model.getRequestId()));
      }
      if (!StringUtils.isEmpty(model.getRequestStatus())) {
        if ("OPA".equals(model.getRequestStatus())) {
          query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_REQSTAT_OPA"));
        } else if ("OPP".equals(model.getRequestStatus())) {
          query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_REQSTAT_OPP"));
        } else {
          query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_REQSTAT"));
          query.setParameter("REQUEST_STATUS", model.getRequestStatus());
        }
      }

      if (CmrConstants.YES_NO.Y.toString().equals(model.getExpediteChk())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_EXP"));
        query.setParameter("EXPEDITE_CHKVALUE", model.getExpediteChk());
      }
      if (!StringUtils.isEmpty(model.getCreateDateFrom())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_CRT_FRM"));
        Date createDateFrom = timeFormat.parse(model.getCreateDateFrom() + " 00:00:00");
        // Date createDateFrom = dateFormat.parse(model.getCreateDateFrom());
        query.setParameter("CREATE_FROM", new Timestamp(createDateFrom.getTime()));
      }
      if (!StringUtils.isEmpty(model.getCreateDateTo())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_CRT_TO"));
        Date createDateTo = timeFormat.parse(model.getCreateDateTo() + " 23:59:59");
        // Date createDateTo = dateFormat.parse(model.getCreateDateTo());
        query.setParameter("CREATE_TO", new Timestamp(createDateTo.getTime()));
      }
      if (!StringUtils.isEmpty(model.getLastActDateFrom())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_LSTUPD_FRM"));
        Date lastDateFrom = timeFormat.parse(model.getLastActDateFrom() + " 00:00:00");
        // Date lastDateFrom = dateFormat.parse(model.getLastActDateFrom());
        query.setParameter("LASTUPDT_FROM", new Timestamp(lastDateFrom.getTime()));
      }
      if (!StringUtils.isEmpty(model.getLastActDateTo())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_LSTUPD_TO"));
        Date lastDateTo = timeFormat.parse(model.getLastActDateTo() + " 23:59:59");
        // Date lastDateTo = dateFormat.parse(model.getLastActDateTo());
        query.setParameter("LASTUPDT_TO", new Timestamp(lastDateTo.getTime()));
      }

      if (!StringUtils.isEmpty(model.getProcessedBy())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_PROCESSED_BY"));
        query.setParameter("PROCESSED_BY", model.getProcessedBy());
      }

      if (!StringUtils.isEmpty(model.getPendingAppr())) {
        query.append(ExternalizedQuery.getSql("WORKFLOW.CLAUSE_PENDING_APPR"));

      }

      query.append(" ORDER BY a.REQ_ID " + order + " ");

      query.setParameter("REQUESTER_ID", user.getIntranetId());
      query.setParameter("PROC_CENTER", user.getProcessingCenter());
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      List<CompoundEntity> rs = query.getCompundResults(resultRows, Admin.class, mapping);

      RequestSearchCriteriaModel requestSearchCriteriaModel = null;

      Admin a = null;
      Addr addr = null;
      Data data = null;
      String status = null;
      String reqType = null;
      String claim = null;
      String ownerDesc = null;
      String countryDesc = null;
      String processingStatus = null;

      String canClaim = null;
      String canClaimAll = null;
      String typeDesc = null;
      String pendingAppr = null;
      for (CompoundEntity entity : rs) {
        a = entity.getEntity(Admin.class);
        addr = entity.getEntity(Addr.class);
        data = entity.getEntity(Data.class);
        status = (String) entity.getValue("OVERALL_STATUS");
        reqType = (String) entity.getValue("REQ_TYPE_TEXT");
        claim = (String) entity.getValue("CLAIM_FIELD");
        ownerDesc = (String) entity.getValue("OWNER_DESC");
        countryDesc = (String) entity.getValue("COUNTRY_DESC");
        processingStatus = (String) entity.getValue("PROCESSING_STATUS");
        canClaim = (String) entity.getValue("CAN_CLAIM");
        canClaimAll = (String) entity.getValue("CAN_CLAIM_ALL");
        typeDesc = (String) entity.getValue("TYPE_DESCRIPTION");
        pendingAppr = (String) entity.getValue("PENDING_APPROVALS");

        requestSearchCriteriaModel = new RequestSearchCriteriaModel();
        if (a != null) {
          copyValuesFromEntity(a, requestSearchCriteriaModel);
          requestSearchCriteriaModel.setCustName(concat(a.getMainCustNm1(), a.getMainCustNm2()));

        }
        if (addr != null) {
          requestSearchCriteriaModel.setCustName(concat(addr.getCustNm1(), addr.getCustNm2()));
        }

        if (data != null) {
          requestSearchCriteriaModel.setCmrNo(data.getCmrNo());
          requestSearchCriteriaModel.setCmrOwner(data.getCmrOwner());
          requestSearchCriteriaModel.setCmrIssuingCntry(data.getCmrIssuingCntry());
        }

        requestSearchCriteriaModel.setCmrOwnerDesc(ownerDesc);
        requestSearchCriteriaModel.setCmrIssuingCntryDesc(countryDesc);
        requestSearchCriteriaModel.setOverallStatus(status);
        requestSearchCriteriaModel.setReqTypeText(reqType);
        requestSearchCriteriaModel.setClaimField(claim);
        requestSearchCriteriaModel.setProcessingStatus(processingStatus);
        requestSearchCriteriaModel.setCanClaim(canClaim);
        requestSearchCriteriaModel.setCanClaimAll(canClaimAll);
        requestSearchCriteriaModel.setTypeDescription(typeDesc);

        requestSearchCriteriaModel.setProspect(a.getProspLegalInd());
        requestSearchCriteriaModel.setIterationId(a.getIterationId());
        requestSearchCriteriaModel.setPendingAppr(pendingAppr);

        results.add(requestSearchCriteriaModel);
      }
      return results;
    } catch (Exception e) {
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }

  }

  @Override
  protected Admin getCurrentRecord(RequestSearchCriteriaModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Admin createFromModel(RequestSearchCriteriaModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

}
