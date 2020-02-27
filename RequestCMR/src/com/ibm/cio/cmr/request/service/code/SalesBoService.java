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
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.code.SalesBoModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

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
      AppUser user = AppUser.getUser(request);
      SalesBranchOff sbo = new SalesBranchOff();
      SalesBranchOffPK sboPK = new SalesBranchOffPK();
      sboPK.setIssuingCntry(model.getIssuingCntry());
      sboPK.setRepTeamCd(model.getRepTeamCd());
      sboPK.setSalesBoCd(model.getSalesBoCd());
      sbo.setClientTier(model.getClientTier());
      sbo.setMrcCd(StringUtils.isNotBlank(model.getMrcCd()) ? model.getMrcCd() : "");
      sbo.setIsuCd(model.getIsuCd());
      sbo.setSalesBoDesc(StringUtils.isNotBlank(model.getSalesBoDesc()) ? model.getSalesBoDesc() : "");
      sbo.setId(sboPK);
      switch (model.getAction()) {
      case BaseModel.ACT_INSERT:
        sbo.setCreateById(user.getIntranetId());
        sbo.setCreateTs(SystemUtil.getCurrentTimestamp());
        entityManager.persist(sbo);
        break;
      case BaseModel.ACT_DELETE:
        sbo = entityManager.find(SalesBranchOff.class, sboPK);
        if (sbo != null) {
          entityManager.remove(sbo);
        }
        break;
      case BaseModel.ACT_UPDATE:
        sbo.setUpdateById(user.getIntranetId());
        sbo.setUpdateTs(SystemUtil.getCurrentTimestamp());
        entityManager.merge(sbo);
        break;
      case "REMOVE_MAPPINGS":
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
