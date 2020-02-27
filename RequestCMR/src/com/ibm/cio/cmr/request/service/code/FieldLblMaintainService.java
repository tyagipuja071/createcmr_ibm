package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.FieldLbl;
import com.ibm.cio.cmr.request.entity.FieldLblPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.FieldLabelModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rochelle Salazar
 * 
 */
@Component
public class FieldLblMaintainService extends BaseService<FieldLabelModel, FieldLbl> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(FieldLblMaintainService.class);
  }

  @Override
  protected void performTransaction(FieldLabelModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<FieldLabelModel> doSearch(FieldLabelModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    FieldLbl fldlbl = getCurrentRecord(model, entityManager, request);
    FieldLabelModel newModel = new FieldLabelModel();
    copyValuesFromEntity(fldlbl, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected FieldLbl getCurrentRecord(FieldLabelModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String fieldId = model.getFieldId();
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    String sql = ExternalizedQuery.getSql("SYSTEM.FLDLBLMAINT");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("FIELD_ID", fieldId);
    q.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    return q.getSingleResult(FieldLbl.class);
  }

  @Override
  protected FieldLbl createFromModel(FieldLabelModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    FieldLbl fieldLbl = new FieldLbl();
    FieldLblPK pk = new FieldLblPK();
    pk.setFieldId(model.getFieldId());
    pk.setCmrIssuingCntry(model.getCmrIssuingCntry());
    fieldLbl.setId(pk);
    fieldLbl.setCmt(model.getCmt());
    fieldLbl.setLbl(model.getLbl());
    return fieldLbl;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
