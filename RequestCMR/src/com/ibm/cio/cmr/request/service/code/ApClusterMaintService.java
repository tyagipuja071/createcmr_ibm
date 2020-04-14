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
import com.ibm.cio.cmr.request.entity.ApCustClusterTierMap;
import com.ibm.cio.cmr.request.entity.ApCustClusterTierMapPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.ApClusterMaintModel;
import com.ibm.cio.cmr.request.model.code.ApClusterModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * 
 * @author PoojaTyagi
 *
 */
@Controller
public class ApClusterMaintService extends BaseService<ApClusterModel, ApCustClusterTierMap> {

  ApClusterMaintModel maintModel;

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ApClusterMaintService.class);
  }

  @Override
  protected void performTransaction(ApClusterModel apClusterModel, EntityManager entityManager, HttpServletRequest request) throws Exception {
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
          ApClusterModel model = new ApClusterModel();
          for (String stringValue : this.maintModel.getItems()) {
            try {
              model = mapper.readValue(stringValue, ApClusterModel.class);
              if ("DUMMY".equals(model.getApCustClusterId()) && "DUMMY".equals(model.getClusterDesc()) && "DUMMY".equals(model.getIsuCode())
                  && "DUMMY".equals(model.getClientTierCd())) {
                continue;
              }
              AppUser user = AppUser.getUser(request);
              ApCustClusterTierMap apCluster = new ApCustClusterTierMap();
              ApCustClusterTierMapPK apClusterPK = new ApCustClusterTierMapPK();
              apClusterPK.setIssuingCntry(this.maintModel.getIssuingCntry());
              apClusterPK.setApCustClusterId(model.getApCustClusterId());
              apClusterPK.setClientTierCd(model.getClientTierCd());
              apClusterPK.setIsuCode(model.getIsuCode());
              apCluster.setClusterDesc(StringUtils.isNotBlank(model.getClusterDesc()) ? model.getClusterDesc() : "");
              apCluster.setId(apClusterPK);
              apCluster.setCreateBy(user.getIntranetId());
              apCluster.setCreateTs(SystemUtil.getCurrentTimestamp());
              apCluster.setDefaultIndc(StringUtils.isNotEmpty(model.getDefaultIndc()) ? model.getDefaultIndc() : null);
              entityManager.persist(apCluster);
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
    String sql = "delete from CREQCMR.AP_CUST_CLUSTER_TIER_MAP where ISSUING_CNTRY = :ISSUING_CNTRY";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", issuingCntry);
    log.debug("Deleting Existing AP Cluster Mappings for issuing country - " + issuingCntry);
    query.executeSql();
  }

  @Override
  protected List<ApClusterModel> doSearch(ApClusterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    ApCustClusterTierMap apCluster = getCurrentRecord(model, entityManager, request);
    ApClusterModel newModel = new ApClusterModel();
    if (apCluster != null) {
      copyValuesFromEntity(apCluster, newModel);
      newModel.setState(BaseModel.STATE_EXISTING);
    }
    return Collections.singletonList(newModel);
  }

  @Override
  protected ApCustClusterTierMap getCurrentRecord(ApClusterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String issuingCntry = model.getIssuingCntry();
    if (StringUtils.isNotBlank(issuingCntry) && issuingCntry.length() == 3) {
      String sql = ExternalizedQuery.getSql("SYSTEM.AP_CUST_CLUSTER_MAP");
      PreparedQuery q = new PreparedQuery(entityManager, sql);
      q.setParameter("CNTRY", issuingCntry);
      return q.getSingleResult(ApCustClusterTierMap.class);
    }
    return null;
  }

  @Override
  protected ApCustClusterTierMap createFromModel(ApClusterModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    ApCustClusterTierMap apCluster = new ApCustClusterTierMap();
    ApCustClusterTierMapPK pk = new ApCustClusterTierMapPK();
    pk.setIssuingCntry(model.getIssuingCntry());
    pk.setApCustClusterId(model.getApCustClusterId());
    pk.setIsuCode(model.getIsuCode());
    pk.setClientTierCd(model.getClientTierCd());
    apCluster.setId(pk);
    apCluster.setClusterDesc(model.getClusterDesc());
    return apCluster;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

  public ApClusterMaintModel getMaintModel() {
    return maintModel;
  }

  public void setMaintModel(ApClusterMaintModel maintModel) {
    this.maintModel = maintModel;
  }

}
