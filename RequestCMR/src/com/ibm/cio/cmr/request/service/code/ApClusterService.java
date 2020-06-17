package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.ApCustClusterTierMap;
import com.ibm.cio.cmr.request.entity.ApCustClusterTierMapPK;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.code.ApClusterModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class ApClusterService extends BaseService<ApClusterModel, ApCustClusterTierMap> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ApClusterService.class);
  }

  @Override
  protected void performTransaction(ApClusterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    if (model != null) {
      if (StringUtils.isBlank(model.getAction())) {
        throw new Exception("No action defined");
      }
      ApCustClusterTierMapPK apClPK = new ApCustClusterTierMapPK();

      switch (model.getAction()) {
      case "REMOVE_MAPPINGS":
        List<KeyContainer> keys = extractKeys(model);
        ApCustClusterTierMap selectedCluster = null;
        String apCluId = null;
        String issuingCntry = null;
        String isuCode = null;
        String ctc = null;
        for (KeyContainer key : keys) {
          issuingCntry = key.getKey("issuingCntry");
          isuCode = key.getKey("isuCode");
          ctc = key.getKey("clientTierCd");
          apCluId = key.getKey("apCustClusterId");
          apClPK.setApCustClusterId(StringUtils.isNotBlank(apCluId) ? apCluId : "");
          apClPK.setIssuingCntry(StringUtils.isNotBlank(issuingCntry) ? issuingCntry : "");
          apClPK.setClientTierCd(StringUtils.isNotBlank(ctc) ? ctc : "");
          apClPK.setIsuCode(StringUtils.isNotBlank(isuCode) ? isuCode : "");
          selectedCluster = entityManager.find(ApCustClusterTierMap.class, apClPK);
          if (selectedCluster != null) {
            ApCustClusterTierMap merged = entityManager.merge(selectedCluster);
            if (merged != null) {
              entityManager.remove(merged);
            }
            entityManager.flush();
          }
        }

      }
    }

  }

  @Override
  protected List<ApClusterModel> doSearch(ApClusterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    // SimpleDateFormat formatter = new
    // SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT")); // ?
    String sql = ExternalizedQuery.getSql("AP_CUST_CLUSTER_TIER_MAP.GET_LIST");
    String cmrIssuingCntry = model.getIssuingCntry();
    if (StringUtils.isEmpty(cmrIssuingCntry)) {
      cmrIssuingCntry = "";
    }
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNTRY", "%" + cmrIssuingCntry + "%");
    q.setForReadOnly(true);
    List<ApCustClusterTierMap> acctmList = q.getResults(ApCustClusterTierMap.class);
    List<ApClusterModel> acctmModelList = new ArrayList<>();
    ApClusterModel acctmModel = null;
    for (ApCustClusterTierMap acctmRecord : acctmList) {
      acctmModel = new ApClusterModel();
      copyValuesFromEntity(acctmRecord, acctmModel);
      acctmModel.setIssuingCntry(acctmRecord.getId().getIssuingCntry());
      acctmModel.setIsuCode(acctmRecord.getId().getIsuCode());
      acctmModel.setCreateBy(acctmRecord.getCreateBy());
      acctmModel.setUpdtBy(acctmRecord.getUpdtBy());
      acctmModel.setDefaultIndc(acctmRecord.getDefaultIndc());
      acctmModelList.add(acctmModel);
    }
    return acctmModelList;
  }

  @Override
  protected ApCustClusterTierMap getCurrentRecord(ApClusterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected ApCustClusterTierMap createFromModel(ApClusterModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
