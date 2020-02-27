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
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.model.code.SuppCountryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Jose Belgira
 * 
 * 
 */
@Component
public class SuppCountryAdminService extends BaseService<SuppCountryModel, SuppCntry> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SuppCountryAdminService.class);
  }

  @Override
  protected void performTransaction(SuppCountryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<SuppCountryModel> doSearch(SuppCountryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.SUPP_CNTRY");
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT"));
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);
    List<SuppCntry> suppCntries = q.getResults(SuppCntry.class);
    List<SuppCountryModel> suppCountryModels = new ArrayList<>();
    SuppCountryModel suppCountryModel = null;
    for (SuppCntry suppCntry : suppCntries) {
      suppCountryModel = new SuppCountryModel();
      copyValuesFromEntity(suppCntry, suppCountryModel);

      suppCountryModel.setCreateDtStringFormat((formatter.format(suppCntry.getCreateDt())));
      suppCountryModels.add(suppCountryModel);
    }
    return suppCountryModels;
  }

  @Override
  protected SuppCntry getCurrentRecord(SuppCountryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected SuppCntry createFromModel(SuppCountryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
