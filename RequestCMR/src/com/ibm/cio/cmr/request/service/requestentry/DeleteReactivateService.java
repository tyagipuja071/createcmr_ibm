/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.DeleteReactivateModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * Service that just queries the deleted/reactivate records
 * 
 * @author JeffZAMORA
 * 
 */
@Component
public class DeleteReactivateService extends BaseSimpleService<List<DeleteReactivateModel>> {

  @Override
  protected List<DeleteReactivateModel> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    long reqId = (long) params.getParam("reqId");

    String sql = ExternalizedQuery.getSql("DELETE.REACTIVATE.LIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<DeleteReactivateModel> results = new ArrayList<DeleteReactivateModel>();
    DeleteReactivateModel model = null;
    List<Object[]> records = query.getResults();
    for (Object[] record : records) {
      model = new DeleteReactivateModel();
      model.setReqId((long) record[0]);
      model.setCmrNo((String) record[1]);
      model.setName((String) record[2]);
      model.setOrderBlock((String) record[3]);
      model.setDeleted("X".equals(record[4]) ? "Yes" : "");
      results.add(model);
    }
    return results;
  }

}
