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
import com.ibm.cio.cmr.request.entity.ProcCenter;
import com.ibm.cio.cmr.request.entity.ProcCenterPK;
import com.ibm.cio.cmr.request.model.code.ProcCenterModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Eduard Bernardo
 * 
 */
@Component
public class ProcCenterAdminService extends BaseService<ProcCenterModel, ProcCenter> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ProcCenterAdminService.class);
  }

  @Override
  protected void doBeforeInsert(ProcCenter entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {

  }

  @Override
  protected void doBeforeUpdate(ProcCenter entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {

  }

  @Override
  protected void performTransaction(ProcCenterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

  }

  @Override
  protected List<ProcCenterModel> doSearch(ProcCenterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.GET.PROCCENTERLIST");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);
    List<Object[]> results = q.getResults();

    List<ProcCenterModel> claimRoleModels = new ArrayList<ProcCenterModel>();
    if (results != null && !results.isEmpty()) {
      ProcCenter center = null;
      ProcCenterPK pk = null;
      ProcCenterModel procCenterModel = null;
      for (Object[] result : results) {
        center = new ProcCenter();
        pk = new ProcCenterPK();
        pk.setCmrIssuingCntry((String) result[0]);
        center.setId(pk);
        center.setProcCenterNm((String) result[1]);
        center.setCmt((String) result[2]);

        procCenterModel = new ProcCenterModel();
        copyValuesFromEntity(center, procCenterModel);
        procCenterModel.setCountry((String) result[3]);
        claimRoleModels.add(procCenterModel);
      }
    }
    return claimRoleModels;
  }

  @Override
  protected ProcCenter getCurrentRecord(ProcCenterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected ProcCenter createFromModel(ProcCenterModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
