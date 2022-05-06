package com.ibm.cio.cmr.request.service.code;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.USBusinessPartnerMaster;
import com.ibm.cio.cmr.request.entity.USBusinessPartnerMasterPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.USBusinessPartnerMasterModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

@Component
public class USBusinessPartnerMasterMaintainService extends BaseService<USBusinessPartnerMasterModel, USBusinessPartnerMaster> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USBusinessPartnerMasterMaintainService.class);
  }

  @Override
  protected void performTransaction(USBusinessPartnerMasterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<USBusinessPartnerMasterModel> doSearch(USBusinessPartnerMasterModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    USBusinessPartnerMaster currentModel = getCurrentRecord(model, entityManager, request);
    USBusinessPartnerMasterModel newModel = new USBusinessPartnerMasterModel();

    copyValuesFromEntity(currentModel, newModel);

    newModel.setState(BaseModel.STATE_EXISTING);
    newModel.setCreatedTsStr(formatter.format(currentModel.getCreateDt()));
    newModel.setUpdatedTsStr(formatter.format(currentModel.getUpdateDt()));

    return Collections.singletonList(newModel);
  }

  @Override
  protected USBusinessPartnerMaster getCurrentRecord(USBusinessPartnerMasterModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    String sql = ExternalizedQuery.getSql("US.GET.US_BP_MASTER_FOR_COMPANY_NO");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setParameter("COMPANY_NO", model.getCompanyNo() == null ? "" : model.getCompanyNo());

    return q.getSingleResult(USBusinessPartnerMaster.class);
  }

  @Override
  protected USBusinessPartnerMaster createFromModel(USBusinessPartnerMasterModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    USBusinessPartnerMaster usBusinessPartnerMaster = new USBusinessPartnerMaster();
    USBusinessPartnerMasterPK pk = new USBusinessPartnerMasterPK();

    pk.setMandt(SystemConfiguration.getValue("MANDT"));
    pk.setCompanyNo(model.getCompanyNo());

    usBusinessPartnerMaster.setId(pk);
    usBusinessPartnerMaster.setCmrNo(model.getCmrNo());
    usBusinessPartnerMaster.setKatr10("");

    Timestamp ts = SystemUtil.getCurrentTimestamp();
    usBusinessPartnerMaster.setCreateDt(ts);
    usBusinessPartnerMaster.setUpdateDt(ts);

    AppUser user = AppUser.getUser(request);
    usBusinessPartnerMaster.setCreatedBy(user.getIntranetId());
    usBusinessPartnerMaster.setUpdatedBy(user.getIntranetId());

    return usBusinessPartnerMaster;
  }

  @Override
  protected void doBeforeInsert(USBusinessPartnerMaster entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    String loevm = request.getParameter("lovem");

    if ("X".equals(loevm)) {
      entity.setLoevm("X");
      entity.setKatr10("");
      entity.setUpdateType(BaseModel.ACT_DELETE);
    } else {
      entity.setLoevm("");
      entity.setKatr10("");
      entity.setUpdateType(BaseModel.ACT_INSERT);
    }

    AppUser user = AppUser.getUser(request);
    entity.setCreatedBy(user.getIntranetId());
    entity.setCreateDt(this.currentTimestamp);
    entity.setUpdatedBy(user.getIntranetId());
    entity.setUpdateDt(this.currentTimestamp);
  }

  @Override
  protected void doBeforeUpdate(USBusinessPartnerMaster entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    entity.setCmrNo(entity.getCmrNo());
    entity.setUpdatedBy(AppUser.getUser(request).getIntranetId());
    entity.setUpdateDt(SystemUtil.getCurrentTimestamp());

    if ("X".equals(entity.getLoevm())) {
      entity.setLoevm("X");
      entity.setUpdateType(BaseModel.ACT_DELETE);
    } else {
      entity.setLoevm("");
      entity.setUpdateType(BaseModel.ACT_UPDATE);
    }

    entity.setKatr10("");

  }

}
