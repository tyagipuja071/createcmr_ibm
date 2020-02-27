package com.ibm.cio.cmr.request.service.code;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.ReftBrSboCollector;
import com.ibm.cio.cmr.request.model.code.BRSboCollectorModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class BRSboCollectorService extends BaseService<BRSboCollectorModel, ReftBrSboCollector> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(BRSboCollectorService.class);
  }

  @Override
  protected void performTransaction(BRSboCollectorModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<BRSboCollectorModel> doSearch(BRSboCollectorModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT"));
    String sql = ExternalizedQuery.getSql("BR_SBO_COLLECTOR.GET_LIST");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);
    List<ReftBrSboCollector> sboList = q.getResults(ReftBrSboCollector.class);
    List<BRSboCollectorModel> sboModelList = new ArrayList<>();
    BRSboCollectorModel sboModel = null;
    for (ReftBrSboCollector sboRecord : sboList) {
      sboModel = new BRSboCollectorModel();
      copyValuesFromEntity(sboRecord, sboModel);
      sboModel.setCreateTsString(formatter.format(sboRecord.getCreateTs()));
      sboModel.setUpdtTsString(formatter.format(sboRecord.getLastUpdtTs()));
      sboModelList.add(sboModel);
    }
    return sboModelList;
  }

  @Override
  protected ReftBrSboCollector getCurrentRecord(BRSboCollectorModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected ReftBrSboCollector createFromModel(BRSboCollectorModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
