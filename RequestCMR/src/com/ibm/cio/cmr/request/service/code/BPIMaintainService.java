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
import com.ibm.cio.cmr.request.entity.BPCodeInt;
import com.ibm.cio.cmr.request.entity.BPCodeIntPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.BPIntModel;
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
public class BPIMaintainService extends BaseService<BPIntModel, BPCodeInt> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(BPIMaintainService.class);
  }

  @Override
  protected void performTransaction(BPIntModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<BPIntModel> doSearch(BPIntModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    BPCodeInt field = getCurrentRecord(model, entityManager, request);
    BPIntModel newModel = new BPIntModel();
    copyValuesFromEntity(field, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    newModel.setCreatedBy(field.getCreateBy());
    newModel.setCreateDt(formatter.format(field.getCreateDate()));
    newModel.setUpdatedBy(field.getUpdateBy());
    newModel.setUpdateDt(formatter.format(field.getUpdateDate()));
    return Collections.singletonList(newModel);
  }

  @Override
  protected BPCodeInt getCurrentRecord(BPIntModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.GETBPI2");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("BPCODE", model.getBpCode() != null ? model.getBpCode() : "");
    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    return q.getSingleResult(BPCodeInt.class);
  }

  @Override
  protected BPCodeInt createFromModel(BPIntModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    BPCodeInt bpi = new BPCodeInt();
    BPCodeIntPK pk = new BPCodeIntPK();
    pk.setBpCode(model.getBpCode());
    pk.setMandt(SystemConfiguration.getValue("MANDT"));
    bpi.setId(pk);
    bpi.setnBpAbbrevNm(model.getnBpAbbrevNm().toUpperCase());
    bpi.setnBpFullNm(model.getnBpFullNm().toUpperCase());
    bpi.setnKatr10("");
    Timestamp ts = SystemUtil.getCurrentTimestamp();
    AppUser user = AppUser.getUser(request);
    bpi.setCreateDate(ts);
    bpi.setUpdateDate(ts);
    bpi.setCreateBy(user.getIntranetId());
    bpi.setUpdateBy(user.getIntranetId());

    return bpi;
  }

  @Override
  protected void doBeforeInsert(BPCodeInt entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("SYSTEM.GETBPI2");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("BPCODE", entity.getId().getBpCode());
    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    if (q.exists()) {
      throw new CmrException(
          new Exception("Please enter another BP Code as the " + entity.getId().getBpCode() + " is already entered in this table."));
    }
    super.doBeforeInsert(entity, entityManager, request);
    entity.setUpdateBy(AppUser.getUser(request).getIntranetId());
    entity.setUpdateDate(SystemUtil.getCurrentTimestamp());
    entity.setUpdateType(BaseModel.ACT_INSERT);
    entity.setnLoevm("");
  }

  @Override
  protected void doBeforeDelete(BPCodeInt entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeDelete(entity, entityManager, request);
    entity.setUpdateType(BaseModel.ACT_DELETE);
  }

  @Override
  protected void doBeforeUpdate(BPCodeInt entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    entity.setnBpAbbrevNm(entity.getnBpAbbrevNm().toUpperCase());
    entity.setnBpFullNm(entity.getnBpFullNm().toUpperCase());
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
