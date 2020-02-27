package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.BdsTblInfo;
import com.ibm.cio.cmr.request.model.code.BusinessDataSrcModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rochelle Salazar
 * 
 */
@Component
public class BusinessDataSrcService extends BaseService<BusinessDataSrcModel, BdsTblInfo> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(BusinessDataSrcService.class);
  }

  @Override
  protected void performTransaction(BusinessDataSrcModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<BusinessDataSrcModel> doSearch(BusinessDataSrcModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.BDS");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    List<BusinessDataSrcModel> list = new ArrayList<>();
    List<BdsTblInfo> record = q.getResults(BdsTblInfo.class);
    BusinessDataSrcModel bdsmodel = null;
    for (BdsTblInfo bdsrec : record) {
      bdsmodel = new BusinessDataSrcModel();
      bdsmodel.setCd(bdsrec.getCd());
      bdsmodel.setCmt(bdsrec.getCmt());
      bdsmodel.setDesc(bdsrec.getDesc());
      bdsmodel.setDispType(bdsrec.getDispType());
      bdsmodel.setFieldId(bdsrec.getId().getFieldId());
      bdsmodel.setOrderByField(bdsrec.getOrderByField());
      bdsmodel.setSchema(bdsrec.getSchema());
      bdsmodel.setTbl(bdsrec.getTbl());
      list.add(bdsmodel);
    }
    return list;
  }

  @Override
  protected BdsTblInfo getCurrentRecord(BusinessDataSrcModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected BdsTblInfo createFromModel(BusinessDataSrcModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
