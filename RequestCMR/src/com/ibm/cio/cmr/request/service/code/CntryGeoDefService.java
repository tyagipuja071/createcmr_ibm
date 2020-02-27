package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.CntryGeoDef;
import com.ibm.cio.cmr.request.model.code.CntryGeoDefModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rochelle Salazar
 * 
 */
@Controller
public class CntryGeoDefService extends BaseService<CntryGeoDefModel, CntryGeoDef> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(CntryGeoDefService.class);
  }

  @Override
  protected void performTransaction(CntryGeoDefModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<CntryGeoDefModel> doSearch(CntryGeoDefModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    String sql = ExternalizedQuery.getSql("SYSTEM.CNTRYGEODEF");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    List<CntryGeoDefModel> list = new ArrayList<>();
    List<CntryGeoDef> record = q.getResults(CntryGeoDef.class);

    CntryGeoDefModel cntryGeoDefModel = null;
    for (CntryGeoDef cntryGeoDef : record) {
      cntryGeoDefModel = new CntryGeoDefModel();
      cntryGeoDefModel.setCmrIssuingCntry(cntryGeoDef.getId().getCmrIssuingCntry());
      cntryGeoDefModel.setGeoCd(cntryGeoDef.getId().getGeoCd());
      cntryGeoDefModel.setCntryDesc(cntryGeoDef.getCntryDesc());
      cntryGeoDefModel.setComments(cntryGeoDef.getComments());
      list.add(cntryGeoDefModel);
    }
    return list;
  }

  @Override
  protected CntryGeoDef getCurrentRecord(CntryGeoDefModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected CntryGeoDef createFromModel(CntryGeoDefModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }
}
