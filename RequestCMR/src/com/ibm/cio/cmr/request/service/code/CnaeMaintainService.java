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
import com.ibm.cio.cmr.request.entity.ReftBrCnae;
import com.ibm.cio.cmr.request.entity.ReftBrCnaePK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.CnaeModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

/**
 * @author RoopakChugh
 * 
 */
@Component
public class CnaeMaintainService extends BaseService<CnaeModel, ReftBrCnae> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(CnaeMaintainService.class);
  }

  @Override
  protected void performTransaction(CnaeModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<CnaeModel> doSearch(CnaeModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    ReftBrCnae field = getCurrentRecord(model, entityManager, request);
    CnaeModel newModel = new CnaeModel();
    copyValuesFromEntity(field, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected ReftBrCnae getCurrentRecord(CnaeModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("CNAE.SEARCH");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNAE", model.getCnaeNo() != null ? model.getCnaeNo().trim().toUpperCase() : "x");
    return q.getSingleResult(ReftBrCnae.class);
  }

  @Override
  protected ReftBrCnae createFromModel(CnaeModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    ReftBrCnae cnae = new ReftBrCnae();
    ReftBrCnaePK pk = new ReftBrCnaePK();
    pk.setCnaeNo(model.getCnaeNo());
    cnae.setId(pk);
    cnae.setCnaeDescrip(model.getCnaeDescrip());
    cnae.setIsuCd(model.getIsuCd());
    cnae.setIsicCd(model.getIsicCd());
    cnae.setSubIndustryCd(model.getSubIndustryCd());
    return cnae;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

  @Override
  protected void doBeforeInsert(ReftBrCnae entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setLastUpdtBy(user.getIntranetId());
    entity.setLastUpdtTs(this.currentTimestamp);
  }
}
