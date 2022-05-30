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
import com.ibm.cio.cmr.request.entity.USIbmOrg;
import com.ibm.cio.cmr.request.entity.USIbmOrgPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.USIbmOrgModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

@Component
public class USIbmOrgMaintainService extends BaseService<USIbmOrgModel, USIbmOrg> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USIbmOrgMaintainService.class);
  }

  @Override
  protected void performTransaction(USIbmOrgModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<USIbmOrgModel> doSearch(USIbmOrgModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    USIbmOrg currentModel = getCurrentRecord(model, entityManager, request);
    USIbmOrgModel newModel = new USIbmOrgModel();

    copyValuesFromEntity(currentModel, newModel);

    newModel.setState(BaseModel.STATE_EXISTING);
    newModel.setCreatedTsStr(formatter.format(currentModel.getCreateDt()));
    newModel.setUpdatedTsStr(formatter.format(currentModel.getUpdateDt()));

    return Collections.singletonList(newModel);
  }

  @Override
  protected USIbmOrg getCurrentRecord(USIbmOrgModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("US.GET.US_IBM_ORG_BY_LEVEL_VALUE");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setParameter("A_LEVEL_1_VALUE", model.getaLevel1Value() == null ? "" : model.getaLevel1Value());
    q.setParameter("A_LEVEL_2_VALUE", model.getaLevel2Value() == null ? "" : model.getaLevel2Value());
    q.setParameter("A_LEVEL_3_VALUE", model.getaLevel3Value() == null ? "" : model.getaLevel3Value());
    q.setParameter("A_LEVEL_4_VALUE", model.getaLevel4Value() == null ? "" : model.getaLevel4Value());

    return q.getSingleResult(USIbmOrg.class);
  }

  @Override
  protected USIbmOrg createFromModel(USIbmOrgModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    USIbmOrg usIbmOrg = new USIbmOrg();
    USIbmOrgPK pk = new USIbmOrgPK();

    pk.setMandt(SystemConfiguration.getValue("MANDT"));
    pk.setaLevel1Value(model.getaLevel1Value());
    pk.setaLevel2Value(model.getaLevel2Value());
    pk.setaLevel3Value(model.getaLevel3Value());
    pk.setaLevel4Value(model.getaLevel4Value());

    usIbmOrg.setId(pk);
    usIbmOrg.setiOrgPrimry(model.getiOrgPrimry());
    usIbmOrg.setiOrgSecndr(model.getiOrgSecndr());
    usIbmOrg.setiOrgPrimryAbbv(model.getiOrgPrimryAbbv());
    usIbmOrg.setiOrgSecndrAbbv(model.getiOrgSecndrAbbv());
    usIbmOrg.setnOrgFull(model.getnOrgFull());

    usIbmOrg.setUpdateType(model.getUpdateType());

    Timestamp ts = SystemUtil.getCurrentTimestamp();
    usIbmOrg.setCreateDt(ts);
    usIbmOrg.setUpdateDt(ts);

    AppUser user = AppUser.getUser(request);
    usIbmOrg.setCreatedBy(user.getIntranetId());
    usIbmOrg.setUpdatedBy(user.getIntranetId());

    return usIbmOrg;
  }

  @Override
  protected void doBeforeInsert(USIbmOrg entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    entity.setUpdateType(BaseModel.ACT_INSERT);

    AppUser user = AppUser.getUser(request);
    entity.setCreatedBy(user.getIntranetId());
    entity.setCreateDt(this.currentTimestamp);
    entity.setUpdatedBy(user.getIntranetId());
    entity.setUpdateDt(this.currentTimestamp);
  }

  @Override
  protected void doBeforeUpdate(USIbmOrg entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    entity.setUpdateType(BaseModel.ACT_UPDATE);

    entity.setUpdatedBy(AppUser.getUser(request).getIntranetId());
    entity.setUpdateDt(SystemUtil.getCurrentTimestamp());
  }

}
