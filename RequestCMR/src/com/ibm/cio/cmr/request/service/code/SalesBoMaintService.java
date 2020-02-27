package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.SalesBranchOff;
import com.ibm.cio.cmr.request.entity.SalesBranchOffPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.SalesBoModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * 
 * @author RoopakChugh
 *
 */
@Controller
public class SalesBoMaintService extends BaseService<SalesBoModel, SalesBranchOff> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SalesBoMaintService.class);
  }

  @Override
  protected void performTransaction(SalesBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<SalesBoModel> doSearch(SalesBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SalesBranchOff salesBo = getCurrentRecord(model, entityManager, request);
    SalesBoModel newModel = new SalesBoModel();
    copyValuesFromEntity(salesBo, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected SalesBranchOff getCurrentRecord(SalesBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String issuingCntry = model.getIssuingCntry();
    String repTeamCd = model.getRepTeamCd();
    String salesBoCd = model.getSalesBoCd();
    String sql = ExternalizedQuery.getSql("SYSTEM.SALES_BRANCH_OFF_MAINT");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("REP_TEAM_CD", repTeamCd);
    q.setParameter("ISSUING_CNTRY", issuingCntry);
    q.setParameter("SALES_BO_CD", salesBoCd);
    return q.getSingleResult(SalesBranchOff.class);
  }

  @Override
  protected SalesBranchOff createFromModel(SalesBoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    SalesBranchOff salesBo = new SalesBranchOff();
    SalesBranchOffPK pk = new SalesBranchOffPK();
    pk.setIssuingCntry(model.getIssuingCntry());
    pk.setRepTeamCd(model.getRepTeamCd());
    pk.setSalesBoCd(model.getSalesBoCd());
    salesBo.setId(pk);
    salesBo.setSalesBoDesc(model.getSalesBoDesc());
    salesBo.setIsuCd(model.getIsuCd());
    salesBo.setClientTier(model.getClientTier());
    salesBo.setMrcCd(model.getMrcCd());
    return salesBo;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
