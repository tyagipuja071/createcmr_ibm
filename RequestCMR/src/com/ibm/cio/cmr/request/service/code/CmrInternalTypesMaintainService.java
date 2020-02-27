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
import com.ibm.cio.cmr.request.entity.CmrInternalTypes;
import com.ibm.cio.cmr.request.entity.CmrInternalTypesPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.CmrInternalTypesModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author Max
 * 
 */
@Component
public class CmrInternalTypesMaintainService extends BaseService<CmrInternalTypesModel, CmrInternalTypes> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(CmrInternalTypesMaintainService.class);
  }

  @Override
  protected void doBeforeInsert(CmrInternalTypes entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);
  }

  @Override
  protected void doBeforeUpdate(CmrInternalTypes entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);
  }

  @Override
  protected void performTransaction(CmrInternalTypesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<CmrInternalTypesModel> doSearch(CmrInternalTypesModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    CmrInternalTypes cmrInternalTypes = getCurrentRecord(model, entityManager, request);
    CmrInternalTypesModel newModel = new CmrInternalTypesModel();
    copyValuesFromEntity(cmrInternalTypes, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected CmrInternalTypes getCurrentRecord(CmrInternalTypesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    String internalTyp = model.getInternalTyp();
    String sql = ExternalizedQuery.getSql("SYSTEM.CMR_INTERNAL_TYPES");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    q.setParameter("INTERNAL_TYP", internalTyp);
    return q.getSingleResult(CmrInternalTypes.class);
  }

  @Override
  protected CmrInternalTypes createFromModel(CmrInternalTypesModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    CmrInternalTypes cmrInternalTypes = new CmrInternalTypes();
    CmrInternalTypesPK pk = new CmrInternalTypesPK();
    pk.setCmrIssuingCntry(model.getCmrIssuingCntry());
    pk.setInternalTyp(model.getInternalTyp());
    cmrInternalTypes.setId(pk);
    cmrInternalTypes.setCondition(model.getCondition());
    cmrInternalTypes.setCreateBy(model.getCreateBy());
    cmrInternalTypes.setCreateTs(model.getCreateTs());
    cmrInternalTypes.setInternalTypDesc(model.getInternalTypDesc());
    cmrInternalTypes.setPriority(model.getPriority());
    cmrInternalTypes.setReqTyp(model.getReqTyp());
    cmrInternalTypes.setSepValInd(model.getSepValInd());
    cmrInternalTypes.setStatus(model.getStatus());
    cmrInternalTypes.setUpdateBy(model.getUpdateBy());
    cmrInternalTypes.setUpdateTs(model.getUpdateTs());

    return cmrInternalTypes;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
