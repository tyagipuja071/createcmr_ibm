/**
 * 
 */
package com.ibm.cio.cmr.request.service.approval;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.ApprovalTyp;
import com.ibm.cio.cmr.request.entity.ApprovalTypPK;
import com.ibm.cio.cmr.request.model.approval.ApprovalTypeModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class ApprovalTypeService extends BaseService<ApprovalTypeModel, ApprovalTyp> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ApprovalTypeService.class);
  }

  @Override
  protected void performTransaction(ApprovalTypeModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

  }

  @Override
  protected List<ApprovalTypeModel> doSearch(ApprovalTypeModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("APPROVAL_TYP.GET_ALL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    if (model.getTypId() > 0 && !StringUtils.isBlank(model.getGeoCd())) {
      query.append(" where TYP_ID = :TYP_ID and GEO_CD = :GEO_CD");
      query.setParameter("TYP_ID", model.getTypId());
      query.setParameter("GEO_CD", model.getGeoCd());
    }
    query.append(" order by case when GEO_CD = 'WW' then 0 else 1 end, GEO_CD, TITLE");
    List<ApprovalTypeModel> records = new ArrayList<ApprovalTypeModel>();
    List<ApprovalTyp> results = query.getResults(ApprovalTyp.class);
    if (results != null) {
      for (ApprovalTyp type : results) {
        ApprovalTypeModel rec = new ApprovalTypeModel();
        copyValuesFromEntity(type, rec);
        records.add(rec);
      }
    }
    return records;
  }

  @Override
  protected ApprovalTyp getCurrentRecord(ApprovalTypeModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    ApprovalTypPK pk = new ApprovalTypPK();
    pk.setTypId(model.getTypId());
    pk.setGeoCd(model.getGeoCd());
    return entityManager.find(ApprovalTyp.class, pk);
  }

  @Override
  protected ApprovalTyp createFromModel(ApprovalTypeModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    ApprovalTypPK pk = new ApprovalTypPK();
    pk.setTypId(model.getTypId());
    pk.setGeoCd(model.getGeoCd());
    ApprovalTyp typ = new ApprovalTyp();
    typ.setId(pk);
    copyValuesToEntity(model, typ);
    return typ;
  }

  @Override
  protected void doBeforeInsert(ApprovalTyp entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("APPROVAL_TYP.GET_MAX_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    Long id = query.getSingleResult(Long.class);
    if (id == null) {
      id = new Long(0);
    }
    id += 1;
    this.log.debug("Setting Approval Type ID to " + id);
    entity.getId().setTypId(id.intValue());
    AppUser user = AppUser.getUser(request);
    this.log.debug("Setting audit details...");
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(SystemUtil.getCurrentTimestamp());
    entity.setLastUpdtBy(user.getIntranetId());
    entity.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
  }

  @Override
  protected void doBeforeUpdate(ApprovalTyp entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    AppUser user = AppUser.getUser(request);
    this.log.debug("Setting audit details...");
    entity.setLastUpdtBy(user.getIntranetId());
    entity.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
  }

  @Override
  protected void doBeforeDelete(ApprovalTyp entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    this.log.debug("Checking references in requests..");
    String sql = ExternalizedQuery.getSql("APPROVAL_TYP.CHECK_REQUESTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("TYP_ID", entity.getId().getTypId());
    query.setParameter("GEO_CD", entity.getId().getGeoCd());
    if (query.exists()) {
      throw new CmrException(MessageUtil.ERROR_APPROVAL_TYPE_CANNOT_BE_DELETED);
    }
    this.log.debug("Checking references in default approvals..");
    sql = ExternalizedQuery.getSql("APPROVAL_TYP.CHECK_DEFAULTS");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("TYP_ID", entity.getId().getTypId());
    query.setParameter("GEO_CD", entity.getId().getGeoCd());
    if (query.exists()) {
      throw new CmrException(MessageUtil.ERROR_APPROVAL_TYPE_CANNOT_BE_DELETED);
    }
  }

}
