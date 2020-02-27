/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.entity.Lov;
import com.ibm.cio.cmr.request.entity.LovPK;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.code.InternalDivDeptModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class InternalDivDeptService extends BaseSimpleService<ProcessResultModel> {

  private static final Logger LOG = Logger.getLogger(InternalDivDeptService.class);
  private static final String ACTION_ADD_DIVISION = "ADD_DIV";
  private static final String ACTION_MAP_DEPT = "MAP_DEPT";

  @Override
  protected ProcessResultModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    ProcessResultModel result = new ProcessResultModel();
    InternalDivDeptModel model = (InternalDivDeptModel) params.getParam("model");
    String action = model.getAction();
    LOG.debug("Executing action " + action);
    if (StringUtils.isBlank(action)) {
      result.setSuccess(false);
      result.setMessage("No action specified.");
    } else {
      switch (action) {
      case ACTION_ADD_DIVISION:
        addDivision(entityManager, model.getDiv());
        result.setSuccess(true);
        break;
      case ACTION_MAP_DEPT:
        mapDepartment(entityManager, model.getDiv(), model.getDept());
        result.setSuccess(true);
        break;
      default:
        result.setSuccess(false);
        result.setMessage("Unsupported action " + action + ".");
        break;
      }
    }
    return result;
  }

  private void addDivision(EntityManager entityManager, String division) {
    String sql = ExternalizedQuery.getSql("DIV_DEPT.CHECK_DIV");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("DIV", division);
    Lov lovDiv = query.getSingleResult(Lov.class);
    if (lovDiv == null) {
      LovPK pk = new LovPK();
      pk.setCd(division);
      pk.setCmrIssuingCntry("*");
      pk.setFieldId("##InternalDivision");
      lovDiv = new Lov();
      lovDiv.setId(pk);
      lovDiv.setCmt("Added from Code Maint");
      lovDiv.setDefaultInd("N");
      lovDiv.setDispType("B");
      lovDiv.setTxt("IBM DIVISION");
      sql = ExternalizedQuery.getSql("DIV_DEPT.CHECK_DIV_MAX");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("DIV", division);
      Long max = query.getSingleResult(Long.class);
      lovDiv.setDispOrder(max.intValue() + 1);
      LOG.debug("Adding IBM DIVISION " + division);
      entityManager.persist(lovDiv);
      entityManager.flush();
    } else {
      LOG.debug("Division " + division + " exists. Skipping creation.");
    }
  }

  private void mapDepartment(EntityManager entityManager, String division, String dept) {
    addDivision(entityManager, division);

    String sql = ExternalizedQuery.getSql("DIV_DEPT.CHECK_DEPT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("DEPT", dept);
    Lov lovDept = query.getSingleResult(Lov.class);
    if (lovDept == null) {
      LovPK pk = new LovPK();
      pk.setCd(dept);
      pk.setCmrIssuingCntry("*");
      pk.setFieldId("##InternalDivDept");
      lovDept = new Lov();
      lovDept.setId(pk);
      lovDept.setCmt("Added from Code Maint");
      lovDept.setDefaultInd("N");
      lovDept.setDispType("T");
      lovDept.setTxt(division + "-" + dept + " - IBM DIV/DEPT");
      sql = ExternalizedQuery.getSql("DIV_DEPT.CHECK_DEPT_MAX");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("DEPT", dept);
      Long max = query.getSingleResult(Long.class);
      lovDept.setDispOrder(max.intValue() + 1);
      LOG.debug("Adding IBM DIVISION/DEPARTMENT " + division + "/" + dept);
      entityManager.persist(lovDept);
      entityManager.flush();
    } else {
      lovDept.setTxt(division + "-" + dept + " - IBM DIV/DEPT");
      LOG.debug("Updating IBM DIVISION/DEPARTMENT (" + lovDept.getTxt() + ") to " + division + "/" + dept);
      entityManager.merge(lovDept);
      entityManager.flush();
    }
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

}
