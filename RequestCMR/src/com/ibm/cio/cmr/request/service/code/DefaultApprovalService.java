/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.DefaultApprovals;
import com.ibm.cio.cmr.request.model.code.DefaultApprovalModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class DefaultApprovalService extends BaseService<DefaultApprovalModel, DefaultApprovals> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(DefaultApprovalService.class);
  }

  @Override
  protected void performTransaction(DefaultApprovalModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<DefaultApprovalModel> doSearch(DefaultApprovalModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.GET_DEFAULT_APPR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<CompoundEntity> results = query.getCompundResults(DefaultApprovals.class, DefaultApprovals.DEFAULT_APPROVAL_MAPPING);
    List<DefaultApprovalModel> records = new ArrayList<DefaultApprovalModel>();
    if (results != null) {
      DefaultApprovals appr = null;
      String title = null;
      String countries = null;
      DefaultApprovalModel record = null;
      for (CompoundEntity entity : results) {
        appr = entity.getEntity(DefaultApprovals.class);
        title = (String) entity.getValue("TITLE");
        countries = (String) entity.getValue("COUNTRY");
        record = new DefaultApprovalModel();
        copyValuesFromEntity(appr, record);
        record.setTypDesc(title);
        record.setLastUpdtBy(countries);
        records.add(record);
      }
    }
    return records;
  }

  @Override
  protected DefaultApprovals getCurrentRecord(DefaultApprovalModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected DefaultApprovals createFromModel(DefaultApprovalModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

}
