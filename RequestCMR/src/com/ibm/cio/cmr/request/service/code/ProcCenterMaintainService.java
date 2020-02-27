/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.ProcCenter;
import com.ibm.cio.cmr.request.entity.ProcCenterPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.ProcCenterModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Eduard Bernardo
 * 
 */
@Component
public class ProcCenterMaintainService extends BaseService<ProcCenterModel, ProcCenter> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ProcCenterMaintainService.class);
  }

  @Override
  protected void performTransaction(ProcCenterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<ProcCenterModel> doSearch(ProcCenterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    ProcCenterModel procCenterModel = null;
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    String sql = ExternalizedQuery.getSql("SYSTEM.PROCCENTER");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    List<Object[]> results = q.getResults(1);

    if (results != null && !results.isEmpty()) {
      Object[] result = results.get(0);
      ProcCenter center = null;
      ProcCenterPK pk = null;
      center = new ProcCenter();
      pk = new ProcCenterPK();
      pk.setCmrIssuingCntry((String) result[0]);
      center.setId(pk);
      center.setProcCenterNm((String) result[1]);
      center.setCmt((String) result[2]);

      procCenterModel = new ProcCenterModel();
      copyValuesFromEntity(center, procCenterModel);
      procCenterModel.setCountry((String) result[3]);
      procCenterModel.setState(BaseModel.STATE_EXISTING);
    }

    return Collections.singletonList(procCenterModel);
  }

  @Override
  protected ProcCenter getCurrentRecord(ProcCenterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    String sql = ExternalizedQuery.getSql("SYSTEM.PROCCENTER");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    return q.getSingleResult(ProcCenter.class);
  }

  @Override
  protected ProcCenter createFromModel(ProcCenterModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    ProcCenter procCenter = new ProcCenter();
    procCenter.setId(new ProcCenterPK());
    copyValuesToEntity(model, procCenter);
    return procCenter;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
