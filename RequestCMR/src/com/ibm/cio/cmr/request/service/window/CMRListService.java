/**
 * 
 */
package com.ibm.cio.cmr.request.service.window;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.MassUpdateModel;
import com.ibm.cio.cmr.request.model.window.CMRListModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class CMRListService extends BaseSimpleService<CMRListModel> {
  private static Logger LOG = Logger.getLogger(CMRListService.class);

  @Override
  protected CMRListModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {

    return null;
  }

  public List<MassUpdateModel> getCMRList(MassUpdateModel model, HttpServletRequest request) throws CmrException {
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      List<MassUpdateModel> results = new ArrayList<MassUpdateModel>();
      long parReqId = model.getParReqId();

      String sql = ExternalizedQuery.getSql("REQUESTENTRY.REACTIVATE.GET_CMR_LIST");

      PreparedQuery query = new PreparedQuery(entityManager, sql);

      query.setParameter("REQ_ID", model.getParReqId());

      List<String> rs = query.getResults(String.class);

      MassUpdateModel massUpdtModel = null;
      for (String massUpdtRec : rs) {
        massUpdtModel = new MassUpdateModel();
        massUpdtModel.setCmrNo(massUpdtRec);
        massUpdtModel.setParReqId(parReqId);
        // copyValuesFromEntity(massUpdtRec, massUpdtModel);
        results.add(massUpdtModel);
      }
      return results;
    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        LOG.error("Unexpected error occurred", e);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }

  }

}
