/**
 * 
 */
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
import com.ibm.cio.cmr.request.entity.RestrictToCode;
import com.ibm.cio.cmr.request.entity.RestrictToCodePK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.RestrictModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author sonali Jain
 * 
 */
@Component
public class RestrictMaintainService extends BaseService<RestrictModel, RestrictToCode> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(RestrictMaintainService.class);
  }

  @Override
  protected void performTransaction(RestrictModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<RestrictModel> doSearch(RestrictModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    RestrictToCode field = getCurrentRecord(model, entityManager, request);
    RestrictModel newModel = new RestrictModel();
    copyValuesFromEntity(field, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    newModel.setCreatedBy(field.getCreateBy());
    newModel.setCreateDt(formatter.format(field.getCreateDate()));
    newModel.setUpdatedBy(field.getUpdateBy());
    newModel.setUpdateDt(formatter.format(field.getUpdateDate()));

    return Collections.singletonList(newModel);
  }

  @Override
  protected RestrictToCode getCurrentRecord(RestrictModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.GETRST2");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setParameter("RSTCODE", model.getRestrictToCd() != null ? model.getRestrictToCd() : "");
    return q.getSingleResult(RestrictToCode.class);
  }

  @Override
  protected RestrictToCode createFromModel(RestrictModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    RestrictToCode rst = new RestrictToCode();
    RestrictToCodePK pk = new RestrictToCodePK();
    pk.setRestrictToCd(model.getRestrictToCd());
    pk.setMandt(SystemConfiguration.getValue("MANDT"));
    rst.setId(pk);
    rst.setnRestrictToAbbrevNm(model.getnRestrictToAbbrevNm().toUpperCase());
    rst.setnRestrictToNm(model.getnRestrictToNm().toUpperCase());
    rst.setnKatr10("");
    Timestamp ts = SystemUtil.getCurrentTimestamp();
    AppUser user = AppUser.getUser(request);
    rst.setCreateDate(ts);
    rst.setUpdateDate(ts);
    rst.setCreateBy(user.getIntranetId());
    rst.setUpdateBy(user.getIntranetId());

    return rst;
  }

  @Override
  protected void doBeforeInsert(RestrictToCode entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("SYSTEM.GETRST2");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("RSTCODE", entity.getId().getRestrictToCd());
    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    if (q.exists()) {
      throw new CmrException(
          new Exception("Please enter another Restrict To Code as the " + entity.getId().getRestrictToCd() + " is already entered in this table."));
    }
    super.doBeforeInsert(entity, entityManager, request);
    entity.setUpdateBy(AppUser.getUser(request).getIntranetId());
    entity.setUpdateDate(SystemUtil.getCurrentTimestamp());
    entity.setUpdateType(BaseModel.ACT_INSERT);
    entity.setnLoevm("");
  }

  @Override
  protected void doBeforeDelete(RestrictToCode entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeDelete(entity, entityManager, request);
    entity.setUpdateType(BaseModel.ACT_DELETE);
  }

  @Override
  protected void doBeforeUpdate(RestrictToCode entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    entity.setnRestrictToAbbrevNm(entity.getnRestrictToAbbrevNm().toUpperCase());
    entity.setnRestrictToNm(entity.getnRestrictToNm().toUpperCase());
    entity.setUpdateBy(AppUser.getUser(request).getIntranetId());
    entity.setUpdateDate(SystemUtil.getCurrentTimestamp());
    if ("X".equals(entity.getnLoevm())) {
      entity.setnLoevm("X");
      entity.setUpdateType(BaseModel.ACT_DELETE);
    } else {
      entity.setnLoevm("");
      entity.setUpdateType(BaseModel.ACT_UPDATE);
    }
    entity.setnKatr10("");

  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
