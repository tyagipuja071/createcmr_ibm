package com.ibm.cio.cmr.request.service.code;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.USEnterPrisePK;
import com.ibm.cio.cmr.request.entity.USEnterprise;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.USEnterpriseModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * 
 * @author Bikash Das
 * 
 */

@Component
public class USEnterpriseMaintainService extends BaseService<USEnterpriseModel, USEnterprise> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USEnterpriseMaintainService.class);
  }

  @Override
  protected void performTransaction(USEnterpriseModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
  }

  @Override
  protected List<USEnterpriseModel> doSearch(USEnterpriseModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    USEnterprise currentModel = getCurrentRecord(model, entityManager, request);
    USEnterpriseModel newModel = new USEnterpriseModel();

    copyValuesFromEntity(currentModel, newModel);

    newModel.setState(BaseModel.STATE_EXISTING);
    newModel.setCreatedTsStr(formatter.format(currentModel.getCreateDt()));
    newModel.setUpdatedTsStr(formatter.format(currentModel.getUpdateDt()));

    return Collections.singletonList(newModel);
  }

  @Override
  protected USEnterprise getCurrentRecord(USEnterpriseModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("US.GET.US_ENTERPRISE_BY_ENT_NO");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setParameter("ENT_NO", model.getEntNo() == null ? "" : model.getEntNo());

    return q.getSingleResult(USEnterprise.class);
  }

  @Override
  protected USEnterprise createFromModel(USEnterpriseModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    USEnterprise usEnterprise = new USEnterprise();
    USEnterPrisePK pk = new USEnterPrisePK();

    pk.setMandt(SystemConfiguration.getValue("MANDT"));
    pk.setEntNo(model.getEntNo());

    usEnterprise.setId(pk);
    usEnterprise.setEntLegalName(model.getEntLegalName());
    if ("on".equals(model.getLoevm())) {
      usEnterprise.setLoevm("");
    } else {
      usEnterprise.setLoevm("X");
    }
    usEnterprise.setKatr10(model.getKatr10());
    usEnterprise.setUpdateType(model.getAction());
    usEnterprise.setEntTypeCode("");

    Timestamp ts = SystemUtil.getCurrentTimestamp();
    usEnterprise.setCreateDt(ts);
    usEnterprise.setUpdateDt(ts);

    AppUser user = AppUser.getUser(request);
    usEnterprise.setCreateBy(user.getIntranetId());
    usEnterprise.setUpdateBy(user.getIntranetId());

    return usEnterprise;
  }

  @Override
  protected void doBeforeInsert(USEnterprise entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateDt(this.currentTimestamp);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateDt(this.currentTimestamp);
    if ("X".equals(entity.getLoevm())) {
      entity.setLoevm("X");
      entity.setKatr10("");
      entity.setUpdateType(BaseModel.ACT_DELETE);
    } else {
      entity.setLoevm("");
      entity.setKatr10("");
      entity.setUpdateType(BaseModel.ACT_INSERT);
    }

  }

  @Override
  protected void doBeforeUpdate(USEnterprise entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    entity.setUpdateType(BaseModel.ACT_UPDATE);

    entity.setUpdateBy(AppUser.getUser(request).getIntranetId());
    entity.setUpdateDt(SystemUtil.getCurrentTimestamp());
    entity.setEntTypeCode("");

    if ("on".equals(entity.getLoevm())) {
      entity.setLoevm("");
      entity.setUpdateType(BaseModel.ACT_UPDATE);
    } else {
      entity.setLoevm("X");
      entity.setUpdateType(BaseModel.ACT_DELETE);
    }

    if (StringUtils.isBlank(entity.getKatr10())) {
      entity.setKatr10("");
    }

  }

}
