package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ibm.cio.cmr.request.entity.Lov;
import com.ibm.cio.cmr.request.entity.LovPK;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.code.GermanyDeptModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.JpaManager;

@Controller
public class GermanyDeptService extends BaseSimpleService<ProcessResultModel> {

  private static final Logger LOG = Logger.getLogger(GermanyDeptService.class);
  private static final String ACTION_ADD_DEPT = "ADD_DEPT";
  private static final String ACTION_REMOVE_DEPT = "REMOVE_DEPT";

  @Override
  protected ProcessResultModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    ProcessResultModel result = new ProcessResultModel();
    GermanyDeptModel model = (GermanyDeptModel) params.getParam("model");
    String action = model.getAction();
    LOG.debug("Executing action " + action);
    if (StringUtils.isBlank(action)) {
      result.setSuccess(false);
      result.setMessage("No action specified.");
    } else {
      switch (action) {
      case ACTION_ADD_DEPT:
        addDepartment(entityManager, model, request);
        result.setSuccess(true);
        break;
      case ACTION_REMOVE_DEPT:
        removeDepartment(entityManager, model, request);
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

  @Override
  protected boolean isTransactional() {
    return true;
  }

  public void addDepartment(EntityManager entityManager, GermanyDeptModel model, HttpServletRequest request) {
    Lov lovDiv = new Lov();
    LovPK pk = new LovPK();
    pk.setCd(model.getDeptName());
    pk.setCmrIssuingCntry("724");
    pk.setFieldId("##GermanyInternalDepartment");
    lovDiv.setId(pk);
    lovDiv.setCmt("Added from Code Maint");
    lovDiv.setDefaultInd("N");
    lovDiv.setDispType("B");
    lovDiv.setTxt("IBM DEPARTMENT");
    lovDiv.setDispOrder(1);
    LOG.debug("Adding IBM DEPARTMENT " + request);
    try {
      entityManager.persist(lovDiv);
      entityManager.flush();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public List<GermanyDeptModel> search(GermanyDeptModel model, HttpServletRequest request) {
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("GERMANY_DEPT.VIEW");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    String deptName = (!StringUtils.isBlank(model.getDeptName())) ? model.getDeptName() + "%" : "";
    query.setParameter("DEPT", deptName);
    System.out.println(query.getResults());
    List<String> cdList = query.getResults(String.class);
    System.out.println(cdList);

    List<GermanyDeptModel> list = new ArrayList<>();
    GermanyDeptModel gdModel = null;
    for (String rt : cdList) {
      gdModel = new GermanyDeptModel();
      gdModel.setDeptName(rt);
      list.add(gdModel);
    }
    System.out.println(list);
    return list;

  }

  public void removeDepartment(EntityManager entityManager, GermanyDeptModel model, HttpServletRequest request) {
    LovPK pk = new LovPK();
    pk.setCd(model.getDeptName());
    pk.setCmrIssuingCntry("724");
    pk.setFieldId("##GermanyInternalDepartment");
    Lov lovDiv = entityManager.find(Lov.class, pk);
    entityManager.remove(lovDiv);
    entityManager.flush();

  }

}
