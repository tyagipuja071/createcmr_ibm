package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.SalesBranchOff;
import com.ibm.cio.cmr.request.entity.SalesBranchOffPK;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.code.SalesBoModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * 
 * @author RoopakChugh
 *
 */
@Component
public class SalesBoService extends BaseService<SalesBoModel, SalesBranchOff> {
  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SalesBoService.class);
  }

  @Override
  protected void performTransaction(SalesBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    if (model != null) {
      if (StringUtils.isBlank(model.getAction())) {
        throw new Exception("No action defined");
      }
      if ("REMOVE_MAPPINGS".equals(model.getAction())) {
        List<KeyContainer> keys = extractKeys(model);
        SalesBranchOff selectedSbo = null;
        String issuingCntry = null;
        String repTeamCd = null;
        String salesBoCd = null;
        for (KeyContainer key : keys) {
          issuingCntry = key.getKey("issuingCntry");
          repTeamCd = key.getKey("repTeamCd");
          salesBoCd = key.getKey("salesBoCd");
          SalesBranchOffPK pk = new SalesBranchOffPK();
          pk.setIssuingCntry(StringUtils.isNotBlank(issuingCntry) ? issuingCntry : "");
          pk.setRepTeamCd(StringUtils.isNotBlank(repTeamCd) ? repTeamCd : "");
          pk.setSalesBoCd(StringUtils.isNotBlank(salesBoCd) ? salesBoCd : "");
          selectedSbo = entityManager.find(SalesBranchOff.class, pk);
          if (selectedSbo != null) {
            entityManager.remove(selectedSbo);
          }
        }

      }
    }
  }

  @Override
  protected List<SalesBoModel> doSearch(SalesBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.SALES_BRANCH_OFF");
    String cmrIssuingCntry = request.getParameter("issuingCntry");
    if (StringUtils.isEmpty(cmrIssuingCntry)) {
      cmrIssuingCntry = "";
    }
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNTRY", "%" + cmrIssuingCntry + "%");
    q.setForReadOnly(true);
    List<SalesBoModel> list = new ArrayList<>();
    List<SalesBranchOff> record = q.getResults(SalesBranchOff.class);

    SalesBoModel sboModel = null;
    for (SalesBranchOff sbo : record) {
      sboModel = new SalesBoModel();
      sboModel.setIssuingCntry(sbo.getId().getIssuingCntry());
      sboModel.setRepTeamCd(sbo.getId().getRepTeamCd());
      sboModel.setSalesBoCd(sbo.getId().getSalesBoCd());
      sboModel.setSalesBoDesc(sbo.getSalesBoDesc());
      sboModel.setMrcCd(sbo.getMrcCd());
      sboModel.setClientTier(sbo.getClientTier());
      sboModel.setIsuCd(sbo.getIsuCd());
      list.add(sboModel);
    }
    return list;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

  @Override
  protected SalesBranchOff getCurrentRecord(SalesBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected SalesBranchOff createFromModel(SalesBoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

}
