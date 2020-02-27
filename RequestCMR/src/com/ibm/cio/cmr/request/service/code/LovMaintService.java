package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Lov;
import com.ibm.cio.cmr.request.entity.LovPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.LovModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rochelle Salazar
 * 
 */
@Controller
public class LovMaintService extends BaseService<LovModel, Lov> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(LovMaintService.class);
  }

  @Override
  protected void performTransaction(LovModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<LovModel> doSearch(LovModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    Lov lov = getCurrentRecord(model, entityManager, request);
    LovModel newModel = new LovModel();
    copyValuesFromEntity(lov, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected Lov getCurrentRecord(LovModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String fieldId = model.getFieldId();
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    String code = model.getCd();
    String sql = ExternalizedQuery.getSql("SYSTEM.LOVMAINT");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("FIELD_ID", fieldId);
    q.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    q.setParameter("CD", code);
    return q.getSingleResult(Lov.class);
  }

  @Override
  protected Lov createFromModel(LovModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    Lov lov = new Lov();
    LovPK pk = new LovPK();
    pk.setFieldId(model.getFieldId());
    pk.setCmrIssuingCntry(model.getCmrIssuingCntry());
    pk.setCd(model.getCd());
    lov.setId(pk);
    lov.setCmt(model.getCmt());
    lov.setDefaultInd(model.getDefaultInd());
    lov.setDispOrder(model.getDispOrder());
    lov.setDispType(model.getDispType());
    lov.setTxt(model.getTxt());
    return lov;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
