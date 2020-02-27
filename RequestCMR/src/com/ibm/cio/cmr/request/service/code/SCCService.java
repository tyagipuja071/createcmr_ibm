/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.A11t0scc;
import com.ibm.cio.cmr.request.entity.FieldInfo;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.code.SCCModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class SCCService extends BaseService<SCCModel, A11t0scc> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SCCService.class);
  }

  @Override
  protected void performTransaction(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    if ("MASS_DELETE".equals(model.getMassAction())) {
      List<KeyContainer> keys = extractKeys(model);
      String fieldId = null;
      String cntry = null;
      String seqNo = null;
      FieldInfo info = null;
      String sql = "select * from CREQCMR.FIELD_INFO where FIELD_ID = :ID and CMR_ISSUING_CNTRY = :CNTRY and SEQ_NO = :SEQ_NO";
      PreparedQuery query = null;
      for (KeyContainer key : keys) {
        fieldId = key.getKey("fieldId");
        cntry = key.getKey("cmrIssuingCntry");
        seqNo = key.getKey("seqNo");
        if ("1".equals(seqNo)) {
          // sequence 1 cannot be deleted
          throw new CmrException(24);
        }
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("ID", fieldId);
        query.setParameter("CNTRY", cntry);
        query.setParameter("SEQ_NO", Integer.parseInt(seqNo));
        info = query.getSingleResult(FieldInfo.class);
        if (info != null) {
          deleteEntity(info, entityManager);
        }
      }
    }
  }

  @Override
  protected List<SCCModel> doSearch(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    String sql = ExternalizedQuery.getSql("SYSTEM.SCCLIST");
    String city = request.getParameter("nCity");
    String state = request.getParameter("nSt");
    String county = request.getParameter("nCnty");
    if (StringUtils.isBlank(state)) {
      state = "xxx"; // to retrieve nothing
    }
    if (StringUtils.isBlank(city)) {
      city = ""; // to retrieve all cities
    }
    if (StringUtils.isBlank(county)) {
      county = ""; // to retrieve all counties
    }
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("STATE", "%" + state.toUpperCase() + "%");
    q.setParameter("CITY", "%" + city.toUpperCase() + "%");
    q.setParameter("COUNTY", "%" + county.toUpperCase() + "%");
    q.setForReadOnly(true);
    List<A11t0scc> sccList = q.getResults(A11t0scc.class);
    List<SCCModel> list = new ArrayList<>();
    SCCModel sccModel = null;
    for (A11t0scc scc : sccList) {
      sccModel = new SCCModel();
      sccModel.setcZip(scc.getId().getcZip());
      sccModel.setnCity(scc.getId().getnCity());
      sccModel.setnSt(scc.getId().getnSt());
      sccModel.setnCnty(scc.getId().getnCnty());
      list.add(sccModel);
    }
    return list;
  }

  @Override
  protected A11t0scc getCurrentRecord(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected A11t0scc createFromModel(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
