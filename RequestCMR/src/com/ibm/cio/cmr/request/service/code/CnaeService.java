package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.ReftBrCnae;
import com.ibm.cio.cmr.request.entity.ReftBrCnaePK;
import com.ibm.cio.cmr.request.model.code.CnaeModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class CnaeService extends BaseService<CnaeModel, ReftBrCnae> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(CnaeService.class);
  }

  @Override
  protected void performTransaction(CnaeModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<CnaeModel> doSearch(CnaeModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    String sql = ExternalizedQuery.getSql("CNAE.SEARCH");
    String cnae = request.getParameter("cnaeNo");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNAE", "%" + (StringUtils.isNotEmpty(cnae) ? cnae.toUpperCase() : ""));
    q.setForReadOnly(true);
    List<ReftBrCnae> cnaeList = q.getResults(ReftBrCnae.class);
    List<CnaeModel> list = new ArrayList<>();
    CnaeModel cnaeModel = null;
    for (ReftBrCnae element : cnaeList) {
      cnaeModel = new CnaeModel();
      cnaeModel.setCnaeNo(element.getId().getCnaeNo());
      cnaeModel.setCnaeDescrip(element.getCnaeDescrip());
      cnaeModel.setIsicCd(element.getIsicCd());
      cnaeModel.setIsuCd(element.getIsuCd());
      cnaeModel.setSubIndustryCd(element.getSubIndustryCd());
      list.add(cnaeModel);
    }
    return list;
  }

  @Override
  protected ReftBrCnae getCurrentRecord(CnaeModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    ReftBrCnaePK pk = new ReftBrCnaePK();
    pk.setCnaeNo(model.getCnaeNo());
    ReftBrCnae queue = entityManager.find(ReftBrCnae.class, pk);
    return queue;
  }

  @Override
  protected ReftBrCnae createFromModel(CnaeModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

}
