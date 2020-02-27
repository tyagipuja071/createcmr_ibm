package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.StatusAct;
import com.ibm.cio.cmr.request.model.code.StatusActModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Jose Belgira
 * 
 * 
 */

@Component
public class StatusActAdminService extends BaseService<StatusActModel, StatusAct> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(StatusActAdminService.class);
  }

  @Override
  protected void performTransaction(StatusActModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<StatusActModel> doSearch(StatusActModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.STATUS_ACT");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);
    List<StatusAct> statusActs = q.getResults(StatusAct.class);
    List<StatusActModel> statusActModels = new ArrayList<>();
    List<Object[]> descList = getCMRdescription(entityManager);
    StatusActModel statusActModel = null;
    for (StatusAct statusAct : statusActs) {
      statusActModel = new StatusActModel();
      copyValuesFromEntity(statusAct, statusActModel);
      statusActModels.add(statusActModel);
    }

    compareCMRDescription(statusActModels, descList);

    return statusActModels;
  }

  @Override
  protected StatusAct getCurrentRecord(StatusActModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected StatusAct createFromModel(StatusActModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  private List<Object[]> getCMRdescription(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("QUERY.SYSTEM.GET_SUPP_CNTRY");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);
    List<Object[]> result = null;
    try {
      result = q.getResults();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  private void compareCMRDescription(List<StatusActModel> modelList, List<Object[]> listOfDesc) {
    for (Object[] objects : listOfDesc) {
      for (Object object : objects) {
        for (StatusActModel model : modelList) {
          if (model.getCmrIssuingCntry().equals("*") || model.getCmrIssuingCntry().equals("000")) {
            model.setCmrIssuingCntryDesc("Worldwide");
          } else {
            if (object.toString().trim().contains(model.getCmrIssuingCntry())
                && (!model.getCmrIssuingCntry().equalsIgnoreCase(object.toString().trim()))) {
              model.setCmrIssuingCntryDesc(object.toString().trim());
            }
          }
        }
      }
    }
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
