/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.DefaultApprovalConditions;
import com.ibm.cio.cmr.request.entity.DefaultApprovalConditionsPK;
import com.ibm.cio.cmr.request.entity.DefaultApprovals;
import com.ibm.cio.cmr.request.entity.DefaultApprovalsPK;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.code.DefaultApprovalModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class DefaultApprovalMaintService extends BaseService<DefaultApprovalModel, DefaultApprovals> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(DefaultApprovalMaintService.class);
  }

  @Override
  protected void performTransaction(DefaultApprovalModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    if ("MASS_DELETE".equals(model.getMassAction())) {
      List<KeyContainer> keys = extractKeys(model);
      long defaultApprovalId = -1;

      String sql = null;
      PreparedQuery query = null;
      for (KeyContainer key : keys) {
        defaultApprovalId = Long.parseLong(key.getKey("defaultApprovalId"));
        this.log.debug("Deleting Default Approval ID " + defaultApprovalId);

        sql = "delete from CREQCMR.DEFAULT_APPROVAL_RECIPIENTS where DEFAULT_APPROVAL_ID = :ID";
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("ID", defaultApprovalId);
        query.executeSql();

        sql = "delete from CREQCMR.DEFAULT_APPROVAL_CONDITIONS where DEFAULT_APPROVAL_ID = :ID";
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("ID", defaultApprovalId);
        query.executeSql();

        sql = "delete from CREQCMR.DEFAULT_APPROVALS where DEFAULT_APPROVAL_ID = :ID";
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("ID", defaultApprovalId);
        query.executeSql();

      }
    }
  }

  @Override
  protected List<DefaultApprovalModel> doSearch(DefaultApprovalModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    DefaultApprovalModel record = new DefaultApprovalModel();
    DefaultApprovals approval = getCurrentRecord(model, entityManager, request);
    if (!StringUtils.isBlank(approval.getApprovalMailContent())) {
      String[] parts = approval.getApprovalMailContent().split("[|]");
      record.setApprovalMailSubject(parts[0]);
      if (parts[1] != null) {
        record.setApprovalMailBody(parts[1]);
      }
    }
    copyValuesFromEntity(approval, record);
    return Collections.singletonList(record);
  }

  @Override
  protected DefaultApprovals getCurrentRecord(DefaultApprovalModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.GET_DEFAULT_APPR.DETAILS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", model.getDefaultApprovalId());
    return query.getSingleResult(DefaultApprovals.class);
  }

  @Override
  protected DefaultApprovals createFromModel(DefaultApprovalModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    DefaultApprovalsPK pk = new DefaultApprovalsPK();
    DefaultApprovals approval = new DefaultApprovals();
    approval.setId(pk);
    copyValuesToEntity(model, approval);
    if (!StringUtils.isBlank(model.getApprovalMailSubject()) || !StringUtils.isBlank(model.getApprovalMailBody())) {
      String content = model.getApprovalMailSubject() + "[|]" + model.getApprovalMailBody();
      approval.setApprovalMailContent(content);
    }
    return approval;
  }

  @Override
  public void copyValuesToEntity(DefaultApprovalModel from, DefaultApprovals to) {
    super.copyValuesToEntity(from, to);
    if (!StringUtils.isBlank(from.getApprovalMailSubject()) || !StringUtils.isBlank(from.getApprovalMailBody())) {
      String content = from.getApprovalMailSubject() + "|" + from.getApprovalMailBody();
      to.setApprovalMailContent(content);
    }
  }

  @Override
  protected void doBeforeInsert(DefaultApprovals entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {

    String sql = ExternalizedQuery.getSql("SYSTEM.GET_MAX_DEFAULT_APPR_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    Long id = query.getSingleResult(Long.class);
    if (id == null) {
      id = new Long(0);
    }
    id += 1;
    this.log.debug("Setting Default Approval ID to " + id);
    entity.getId().setDefaultApprovalId(id);

    String geoCd = getGeoCode(entityManager, entity);
    this.log.debug("Setting GEO Code to " + geoCd);
    entity.setGeoCd(geoCd);
    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(SystemUtil.getCurrentTimestamp());
    entity.setLastUpdtBy(user.getIntranetId());
    entity.setLastUpdtTs(SystemUtil.getCurrentTimestamp());

  }

  @Override
  protected void doBeforeUpdate(DefaultApprovals entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String geoCd = getGeoCode(entityManager, entity);
    this.log.debug("Setting GEO Code to " + geoCd);
    entity.setGeoCd(geoCd);
    AppUser user = AppUser.getUser(request);
    entity.setLastUpdtBy(user.getIntranetId());
    entity.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
  }

  private String getGeoCode(EntityManager entityManager, DefaultApprovals entity) {
    String sql = ExternalizedQuery.getSql("SYSTEM.GET_TYPE_GEO_CD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("TYPE_ID", entity.getTypId());
    String geoCd = query.getSingleResult(String.class);
    return geoCd;
  }

  @Override
  protected void doAfterInsert(DefaultApprovals entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doAfterInsert(entity, entityManager, request);

    // make sure the CmrIssuingCntry condition is there
    String sql = ExternalizedQuery.getSql("SYSTEM.DEFAULT_APPR.CHECKTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_TYPE", "%" + entity.getRequestTyp() + "%");

    DefaultApprovalConditions cmrIssuingCntryCond = new DefaultApprovalConditions();
    DefaultApprovalConditionsPK cmrIssuingCntryCondPK = new DefaultApprovalConditionsPK();
    cmrIssuingCntryCondPK.setDefaultApprovalId(entity.getId().getDefaultApprovalId());
    cmrIssuingCntryCondPK.setSequenceNo(1);
    cmrIssuingCntryCond.setId(cmrIssuingCntryCondPK);
    cmrIssuingCntryCond.setConditionLevel(1);
    cmrIssuingCntryCond.setDatabaseFieldName("CMR_ISSUING_CNTRY");
    cmrIssuingCntryCond.setFieldId("CmrIssuingCntry");

    List<String> countries = query.getResults(String.class);
    if (countries != null && countries.size() > 0 && countries.size() == 1) {
      cmrIssuingCntryCond.setOperator("EQ");
      cmrIssuingCntryCond.setValue(countries.get(0));
    } else {
      cmrIssuingCntryCond.setOperator("IN");
      cmrIssuingCntryCond.setValue("(please specify)");
    }

    this.log.debug("Adding CmrIssuingCntry condition...");
    createEntity(cmrIssuingCntryCond, entityManager);
  }
}
