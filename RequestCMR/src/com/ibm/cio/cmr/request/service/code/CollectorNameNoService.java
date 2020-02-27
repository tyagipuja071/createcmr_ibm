package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.CollectorNameNo;
import com.ibm.cio.cmr.request.entity.CollectorNameNoPK;
import com.ibm.cio.cmr.request.model.code.CollectorNameNoModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;

@Controller
public class CollectorNameNoService extends BaseService<CollectorNameNoModel, CollectorNameNo> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(CollectorNameNoService.class);
  }

  @Override
  protected void performTransaction(CollectorNameNoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  protected List<CollectorNameNoModel> doSearch2(CollectorNameNoModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    String sql1 = ExternalizedQuery.getSql("SYSTEM.COLLECTORNAMENO3");
    String cntry = request.getParameter("cmrIssuingCntry");
    String collNo = request.getParameter("collectorno");
    PreparedQuery q = null;

    q = new PreparedQuery(entityManager, sql1);
    q.setParameter("CNTRY", cntry);
    q.setParameter("COLL_NO", collNo);

    List<CollectorNameNoModel> list = new ArrayList<>();
    List<CollectorNameNo> record = q.getResults(CollectorNameNo.class);

    CollectorNameNoModel cnnModel = null;
    for (CollectorNameNo collectorNameNo : record) {
      CollectorNameNoPK cnnPK = collectorNameNo.getId();
      cnnModel = new CollectorNameNoModel();
      cnnModel.setCollectorNo(cnnPK.getCollectorNo());
      cnnModel.setCmrIssuingCntry(cnnPK.getIssuingCntry());
      cnnModel.setCreateBy(collectorNameNo.getCreateById());
      cnnModel.setCreateTs(collectorNameNo.getCreateTs());
      cnnModel.setLastUpdtBy(collectorNameNo.getUpdateById());
      cnnModel.setLastUpdtTs(collectorNameNo.getUpdateTs());
      list.add(cnnModel);
    }
    return list;
  }

  @Override
  protected List<CollectorNameNoModel> doSearch(CollectorNameNoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    List<CollectorNameNoModel> list = new ArrayList<>();

    if (StringUtils.isEmpty(model.getCollectorNo())) {
      String sql1 = ExternalizedQuery.getSql("SYSTEM.COLLECTORNAMENO1");
      String sql2 = ExternalizedQuery.getSql("SYSTEM.COLLECTORNAMENO2");
      String cntry = request.getParameter("cmrIssuingCntry");
      PreparedQuery q = null;

      if (StringUtils.isBlank(cntry)) {
        q = new PreparedQuery(entityManager, sql1);
      } else {
        q = new PreparedQuery(entityManager, sql2);
        q.setParameter("CNTRY", cntry);
      }

      List<CollectorNameNo> record = q.getResults(CollectorNameNo.class);
      CollectorNameNoModel cnnModel = null;
      for (CollectorNameNo collectorNameNo : record) {
        CollectorNameNoPK cnnPK = collectorNameNo.getId();
        cnnModel = new CollectorNameNoModel();
        cnnModel.setCollectorNo(cnnPK.getCollectorNo());
        cnnModel.setCmrIssuingCntry(cnnPK.getIssuingCntry());
        list.add(cnnModel);
      }
    } else {
      list = doSearch2(model, entityManager, request);
    }

    return list;
  }

  @Override
  protected CollectorNameNo getCurrentRecord(CollectorNameNoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected CollectorNameNo createFromModel(CollectorNameNoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    CollectorNameNo collNameNo = new CollectorNameNo();
    CollectorNameNoPK pk = new CollectorNameNoPK();
    pk.setCollectorNo(model.getCollectorNo());
    pk.setIssuingCntry(model.getCmrIssuingCntry());
    collNameNo.setCollectorName("");
    collNameNo.setId(pk);
    collNameNo.setCreateById(model.getCreateBy());
    collNameNo.setCreateTs(model.getCreateTs());
    collNameNo.setUpdateById(model.getLastUpdtBy());
    collNameNo.setUpdateTs(model.getLastUpdtTs());
    return collNameNo;
  }

  @Override
  protected void doBeforeInsert(CollectorNameNo entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setCreateById(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdateById(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);
  }

  @Override
  protected void doAfterUpdate(CollectorNameNo entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doAfterUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdateById(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);

  }

}
