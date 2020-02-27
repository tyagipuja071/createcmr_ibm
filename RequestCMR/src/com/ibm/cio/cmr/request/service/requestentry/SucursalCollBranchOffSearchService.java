package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.SucursalCollBranchOffModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author Mukesh
 *
 */
@Component
public class SucursalCollBranchOffSearchService extends BaseSimpleService<List<SucursalCollBranchOffModel>>{

  @Override
  protected List<SucursalCollBranchOffModel> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params)
      throws CmrException {
    SucursalCollBranchOffModel model = (SucursalCollBranchOffModel) params.getParam("model");
    
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.SUBIND_SUC_COLL_BO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.append(" where ISSUING_CNTRY = :ISSUING_CNTRY");
   // System.out.println("model.getIssuingCntrySearch()->"+model.getIssuingCntrySearch());
    query.setParameter("ISSUING_CNTRY", model.getIssuingCntrySearch());

   if (!StringUtils.isEmpty(model.getCollBOIDSearch())) {
      query.append(" and upper(COLL_BO_ID) like :COLL_BO_ID");
      query.setParameter("COLL_BO_ID", "%" + model.getCollBOIDSearch().toUpperCase()+ "%");
    }
   if (!StringUtils.isEmpty(model.getCollBODescSearch())) {
      query.append(" and upper(COLL_BO_DESC) like :COLL_BO_DESC");
      query.setParameter("COLL_BO_DESC", "%" + model.getCollBODescSearch().toUpperCase() + "%");
    }
   
    query.append(" order by COLL_BO_DESC asc");
    List<Object[]> qResults = query.getResults();

    List<SucursalCollBranchOffModel> results = new ArrayList<SucursalCollBranchOffModel>();
    SucursalCollBranchOffModel resultModel = null;
    for (Object[] result : qResults) {
      resultModel = new SucursalCollBranchOffModel();
      resultModel.setCollBOIDSearch((String) result[1]);
      resultModel.setCollBODescSearch((String)result[2]);
      results.add(resultModel);
    }
    return results;
  }
 }
