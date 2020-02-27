/**
 * 
 */
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
import com.ibm.cio.cmr.request.entity.ValidationUrl;
import com.ibm.cio.cmr.request.model.requestentry.ValidationUrlModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Eduard Bernardo
 * 
 */
@Component
public class ValidationUrlsAdminService extends BaseService<ValidationUrlModel, ValidationUrl> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ValidationUrlsAdminService.class);
  }

  @Override
  protected void performTransaction(ValidationUrlModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<ValidationUrlModel> doSearch(ValidationUrlModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.VALIDATION_URLSLIST");
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT"));
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);
    List<ValidationUrl> valUrls = q.getResults(ValidationUrl.class);
    List<ValidationUrlModel> valUrlModels = new ArrayList<>();
    ValidationUrlModel valModel = null;
    for (ValidationUrl valUrl : valUrls) {
      valModel = new ValidationUrlModel();
      copyValuesFromEntity(valUrl, valModel);

      valModel.setCreateTsString(formatter.format(valUrl.getCreateTs()));
      valModel.setUpdtTsString(formatter.format(valUrl.getUpdtTs()));
      valUrlModels.add(valModel);
    }
    return valUrlModels;
  }

  @Override
  protected ValidationUrl getCurrentRecord(ValidationUrlModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected ValidationUrl createFromModel(ValidationUrlModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
