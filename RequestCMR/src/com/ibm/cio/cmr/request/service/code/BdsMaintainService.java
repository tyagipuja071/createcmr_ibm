package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.BdsTblInfo;
import com.ibm.cio.cmr.request.entity.BdsTblInfoPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.BusinessDataSrcModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rochelle Salazar
 * 
 */
@Component
public class BdsMaintainService extends BaseService<BusinessDataSrcModel, BdsTblInfo> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(BdsMaintainService.class);
  }

  @Override
  protected void performTransaction(BusinessDataSrcModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<BusinessDataSrcModel> doSearch(BusinessDataSrcModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    BdsTblInfo bds = getCurrentRecord(model, entityManager, request);
    BusinessDataSrcModel newModel = new BusinessDataSrcModel();
    copyValuesFromEntity(bds, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected BdsTblInfo getCurrentRecord(BusinessDataSrcModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String fieldId = model.getFieldId();
    String sql = ExternalizedQuery.getSql("SYSTEM.BDSMAINT");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("FIELD_ID", fieldId);
    return q.getSingleResult(BdsTblInfo.class);
  }

  @Override
  protected BdsTblInfo createFromModel(BusinessDataSrcModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    BdsTblInfo bds = new BdsTblInfo();
    BdsTblInfoPK pk = new BdsTblInfoPK();
    pk.setFieldId(model.getFieldId());
    bds.setId(pk);
    bds.setCd(model.getCd());
    bds.setCmt(model.getCmt());
    bds.setDesc(model.getDesc());
    bds.setDispType(model.getDispType());
    bds.setOrderByField(model.getOrderByField());
    bds.setSchema(model.getSchema());
    bds.setTbl(model.getTbl());
    return bds;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
