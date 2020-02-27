/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.A11t0scc;
import com.ibm.cio.cmr.request.entity.A11t0sccPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.SCCModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author sonali Jain
 * 
 */
@Component
public class SCCMaintainService extends BaseService<SCCModel, A11t0scc> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SCCMaintainService.class);
  }

  @Override
  protected void performTransaction(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<SCCModel> doSearch(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    A11t0scc field = getCurrentRecord(model, entityManager, request);
    SCCModel newModel = new SCCModel();
    copyValuesFromEntity(field, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected A11t0scc getCurrentRecord(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.GETSCC");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    DecimalFormat df = new DecimalFormat("#");
    q.setParameter("STATE", model.getnSt() != null ? model.getnSt().trim().toUpperCase() : "x");
    q.setParameter("CITY", model.getnCity() != null ? model.getnCity().trim().toUpperCase() : "x");
    q.setParameter("COUNTY", model.getnCnty() != null ? model.getnCnty().trim().toUpperCase() : "x");
    q.setParameter("ZIP", df.format(model.getcZip()).trim());
    return q.getSingleResult(A11t0scc.class);
  }

  @Override
  protected A11t0scc createFromModel(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    A11t0scc scc = new A11t0scc();
    A11t0sccPK pk = new A11t0sccPK();
    pk.setcZip(model.getcZip());
    pk.setnCity(model.getnCity().toUpperCase());
    pk.setnCnty(model.getnCnty().toUpperCase());
    pk.setnSt(model.getnSt());
    scc.setiTable("SC");
    scc.setId(pk);
    scc.setcCity(0);
    scc.setcCnty(getNextCounty(entityManager, model.getnSt(), model.getnCity().toUpperCase()));
    scc.setcSt(getStateID(entityManager, model.getnSt()));
    scc.setdSccActive(0);
    scc.setfAlphaDup("x");
    scc.setfExtra1("x");
    scc.setfExtra2("x");
    scc.setfAlphaDup("x");
    scc.setfLastAlphaDup("x");
    scc.setfLastNumrcDup("x");
    scc.setfMultplZips("x");
    scc.setfNumrcDup("x");
    scc.setfOtsdCityLimit("x");
    return scc;
  }

  private float getStateID(EntityManager entityManager, String stateCode) {
    if (stateCode.equals("''")) {
      return 0;
    }
    String sql = ExternalizedQuery.getSql("SCC.GET_STATE_KEY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("STATE", stateCode);
    String code = query.getSingleResult(String.class);
    if (!StringUtils.isEmpty(code)) {
      return Float.parseFloat(code);
    }
    return 0;

  }

  private float getNextCounty(EntityManager entityManager, String stateCode, String city) {
    String sql = ExternalizedQuery.getSql("SCC.GET_MAX_CNTY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("STATE", stateCode);
    query.setParameter("CITY", city);
    String code = query.getSingleResult(String.class);
    if (!StringUtils.isEmpty(code)) {
      return Float.parseFloat(code) + 1;
    }
    return 0;

  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
