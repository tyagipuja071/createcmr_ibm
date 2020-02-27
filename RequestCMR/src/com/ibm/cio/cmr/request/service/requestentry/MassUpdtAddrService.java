package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtAddrPK;
import com.ibm.cio.cmr.request.model.requestentry.MassUpdateAddressModel;
import com.ibm.cio.cmr.request.model.requestentry.MassUpdateModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Eduard Bernardo
 * 
 */
@Component
public class MassUpdtAddrService extends BaseService<MassUpdateAddressModel, MassUpdtAddr> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(MassUpdtAddrService.class);
  }

  @Override
  protected void performTransaction(MassUpdateAddressModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop
  }

  @Override
  protected List<MassUpdateAddressModel> doSearch(MassUpdateAddressModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    List<MassUpdateAddressModel> results = new ArrayList<MassUpdateAddressModel>();
    String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.ADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", model.getParReqId());
    query.setParameter("ITERATION_ID", model.getIterationId());
    query.setParameter("SEQ_NO", model.getSeqNo());

    List<MassUpdtAddr> rs = query.getResults(MassUpdtAddr.class);

    MassUpdateAddressModel massAddrModel = null;
    for (MassUpdtAddr massUpdtAddr : rs) {
      massAddrModel = new MassUpdateAddressModel();
      copyValuesFromEntity(massUpdtAddr, massAddrModel);

      results.add(massAddrModel);
    }

    return results;

  }

  @Override
  protected MassUpdtAddr getCurrentRecord(MassUpdateAddressModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected MassUpdtAddr createFromModel(MassUpdateAddressModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    MassUpdtAddr massUpdtAddr = new MassUpdtAddr();
    MassUpdtAddrPK pk = new MassUpdtAddrPK();
    massUpdtAddr.setId(pk);
    copyValuesToEntity(model, massUpdtAddr);
    return massUpdtAddr;
  }

  public List<MassUpdateAddressModel> doSearchById(EntityManager entityManager, MassUpdateModel model) throws Exception {
    List<MassUpdateAddressModel> results = new ArrayList<MassUpdateAddressModel>();

    String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.ADDR.BY.ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", model.getParReqId());
    query.setParameter("ITERATION_ID", model.getIterationId());
    query.setParameter("SEQ_NO", model.getSeqNo());
    List<MassUpdtAddr> rs = query.getResults(MassUpdtAddr.class);

    MassUpdateAddressModel massAddrModel = null;
    for (MassUpdtAddr massUpdtAddr : rs) {
      massAddrModel = new MassUpdateAddressModel();
      copyValuesFromEntity(massUpdtAddr, massAddrModel);

      results.add(massAddrModel);
    }

    return results;
  }
}
