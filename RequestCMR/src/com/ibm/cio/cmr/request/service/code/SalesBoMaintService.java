package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.SalesBranchOff;
import com.ibm.cio.cmr.request.entity.SalesBranchOffPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.SalesBoMaintModel;
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
@Controller
public class SalesBoMaintService extends BaseService<SalesBoModel, SalesBranchOff> {

  SalesBoMaintModel maintModel;

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SalesBoMaintService.class);
  }

  @Override
  protected void performTransaction(SalesBoModel salesBoModel, EntityManager entityManager, HttpServletRequest request) throws Exception {
    if (this.maintModel != null) {
      if (StringUtils.isBlank(this.maintModel.getMassAction())) {
        throw new Exception("Action Not Defined");
      }

      if (StringUtils.isBlank(this.maintModel.getIssuingCntry())) {
        throw new Exception("Issuing Country not defined.");
      }

      if (this.maintModel.getMassAction().equals("MASS_SAVE")) {
        ObjectMapper mapper = new ObjectMapper();
        if (this.maintModel.getItems().size() > 0) {
          this.deleteExisting(entityManager, this.maintModel.getIssuingCntry());
          SalesBoModel model = new SalesBoModel();
          for (String stringValue : this.maintModel.getItems()) {
            try {
              model = mapper.readValue(stringValue, SalesBoModel.class);
              if ("DUMMY".equals(model.getRepTeamCd()) && "DUMMY".equals(model.getSalesBoCd()) && "DUMMY".equals(model.getIsuCd())
                  && "DUMMY".equals(model.getClientTier())) {
                continue;
              }
              AppUser user = AppUser.getUser(request);
              SalesBranchOff sbo = new SalesBranchOff();
              SalesBranchOffPK sboPK = new SalesBranchOffPK();
              sboPK.setIssuingCntry(this.maintModel.getIssuingCntry());
              sboPK.setRepTeamCd(model.getRepTeamCd());
              sboPK.setSalesBoCd(model.getSalesBoCd());
              sbo.setClientTier(model.getClientTier());
              sbo.setMrcCd(StringUtils.isNotBlank(model.getMrcCd()) ? model.getMrcCd() : "");
              sbo.setIsuCd(model.getIsuCd());
              sbo.setSalesBoDesc(StringUtils.isNotBlank(model.getSalesBoDesc()) ? model.getSalesBoDesc() : "");
              sbo.setId(sboPK);
              sbo.setCreateById(user.getIntranetId());
              sbo.setCreateTs(SystemUtil.getCurrentTimestamp());
              entityManager.persist(sbo);
            } catch (Exception e) {
              log.warn("Cannot convert to maint object: " + stringValue);
            }
          }
        }

      } else if ("DELETE".equals(this.maintModel.getMassAction())) {
        deleteExisting(entityManager, this.maintModel.getIssuingCntry());
      }
    }
  }

  private void deleteExisting(EntityManager entityManager, String issuingCntry) {
    String sql = "delete from CREQCMR.SALES_BRANCH_OFF where ISSUING_CNTRY = :ISSUING_CNTRY";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", issuingCntry);
    log.debug("Deleting Existing Sales BO Mappings for issuing country - " + issuingCntry);
    query.executeSql();
  }

  @Override
  protected List<SalesBoModel> doSearch(SalesBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SalesBranchOff salesBo = getCurrentRecord(model, entityManager, request);
    SalesBoModel newModel = new SalesBoModel();
    if (salesBo != null) {
      copyValuesFromEntity(salesBo, newModel);
      newModel.setState(BaseModel.STATE_EXISTING);
    }
    return Collections.singletonList(newModel);
  }

  @Override
  protected SalesBranchOff getCurrentRecord(SalesBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String issuingCntry = model.getIssuingCntry();
    if (StringUtils.isNotBlank(issuingCntry) && issuingCntry.length() == 3) {
      String sql = ExternalizedQuery.getSql("SYSTEM.SALES_BRANCH_OFF");
      PreparedQuery q = new PreparedQuery(entityManager, sql);
      q.setParameter("CNTRY", issuingCntry);
      return q.getSingleResult(SalesBranchOff.class);
    }
    return null;
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

  public SalesBoMaintModel getMaintModel() {
    return maintModel;
  }

  public void setMaintModel(SalesBoMaintModel maintModel) {
    this.maintModel = maintModel;
  }

}
