package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.FieldLbl;
import com.ibm.cio.cmr.request.model.code.FieldLabelModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rochelle Salazar
 * 
 */
@Component
public class FieldLabelService extends BaseService<FieldLabelModel, FieldLbl> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(FieldLabelService.class);
  }

  @Override
  protected void performTransaction(FieldLabelModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<FieldLabelModel> doSearch(FieldLabelModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.FIELDLBL");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    if (StringUtils.isBlank(cmrIssuingCntry)) {
      cmrIssuingCntry = "";
    }
    String fieldId = model.getFieldId();
    if (StringUtils.isBlank(fieldId)) {
      fieldId = "XXX";
    }
    q.setParameter("CNTRY", "%" + cmrIssuingCntry + "%");
    q.setParameter("FIELD_ID", fieldId);
    List<FieldLabelModel> list = new ArrayList<>();
    List<FieldLbl> record = q.getResults(FieldLbl.class);
    FieldLabelModel fieldLabelModel = null;
    for (FieldLbl flblrec : record) {
      fieldLabelModel = new FieldLabelModel();
      fieldLabelModel.setLbl(flblrec.getLbl());
      fieldLabelModel.setCmt(flblrec.getCmt());
      fieldLabelModel.setFieldId(flblrec.getId().getFieldId());
      fieldLabelModel.setCmrIssuingCntry(flblrec.getId().getCmrIssuingCntry());
      list.add(fieldLabelModel);
    }
    return list;
  }

  @Override
  protected FieldLbl getCurrentRecord(FieldLabelModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected FieldLbl createFromModel(FieldLabelModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
