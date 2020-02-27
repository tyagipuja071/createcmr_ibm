/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.FieldInfo;
import com.ibm.cio.cmr.request.entity.FieldInfoPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.FieldInfoModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author sonali Jain
 * 
 */
@Component
public class FieldInfoMaintainService extends BaseService<FieldInfoModel, FieldInfo> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(FieldInfoMaintainService.class);
  }

  @Override
  protected void performTransaction(FieldInfoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<FieldInfoModel> doSearch(FieldInfoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    FieldInfo field = getCurrentRecord(model, entityManager, request);
    FieldInfoModel newModel = new FieldInfoModel();
    copyValuesFromEntity(field, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected FieldInfo getCurrentRecord(FieldInfoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String fieldId = model.getFieldId();
    long seqNo = model.getSeqNo();
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    String sql = ExternalizedQuery.getSql("SYSTEM.FIELD");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("FIELD_ID", fieldId);
    q.setParameter("SEQ_NO", seqNo);
    q.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    return q.getSingleResult(FieldInfo.class);
  }

  @Override
  protected FieldInfo createFromModel(FieldInfoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    FieldInfo fieldInfo = new FieldInfo();
    FieldInfoPK pk = new FieldInfoPK();
    pk.setFieldId(model.getFieldId());
    pk.setSeqNo(model.getSeqNo());
    pk.setCmrIssuingCntry(model.getCmrIssuingCntry());
    fieldInfo.setId(pk);
    fieldInfo.setChoice(model.getChoice());
    fieldInfo.setType(model.getType());
    fieldInfo.setMinLength(model.getMinLength());
    fieldInfo.setMaxLength(model.getMaxLength());
    fieldInfo.setValidation(model.getValidation());
    fieldInfo.setRequired(model.getRequired());
    fieldInfo.setDependsOn(model.getDependsOn());
    fieldInfo.setDependsSetting(model.getDependsSetting());
    fieldInfo.setCondReqInd(model.getCondReqInd());
    fieldInfo.setReadOnlyReqInd(model.getReadOnlyReqInd());
    fieldInfo.setReadOnlyInfoInd(model.getReadOnlyInfoInd());
    fieldInfo.setReadOnlyRevInd(model.getReadOnlyRevInd());
    fieldInfo.setReadOnlyProcInd(model.getReadOnlyProcInd());
    fieldInfo.setValDependsOn(model.getValDependsOn());
    fieldInfo.setCmt(model.getCmt());
    return fieldInfo;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
