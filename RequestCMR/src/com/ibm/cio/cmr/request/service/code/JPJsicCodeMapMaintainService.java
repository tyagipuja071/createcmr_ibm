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
import com.ibm.cio.cmr.request.entity.JpJsicCodeMap;
import com.ibm.cio.cmr.request.entity.JpJsicCodeMapPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.JpJsicCodeMapModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * 
 * @author XiangBinLiu
 *
 */

@Component
public class JPJsicCodeMapMaintainService extends BaseService<JpJsicCodeMapModel, JpJsicCodeMap> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(JPJsicCodeMapMaintainService.class);
  }

  @Override
  protected void performTransaction(JpJsicCodeMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<JpJsicCodeMapModel> doSearch(JpJsicCodeMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    JpJsicCodeMap currentModel = getCurrentRecord(model, entityManager, request);
    JpJsicCodeMapModel newModel = new JpJsicCodeMapModel();

    copyValuesFromEntity(currentModel, newModel);

    newModel.setState(BaseModel.STATE_EXISTING);
    if (currentModel.getCreateTs() != null) {
      newModel.setCreateTsStr(formatter.format(currentModel.getCreateTs()));
    }
    if (currentModel.getUpdateTs() != null) {
      newModel.setUpdateTsStr(formatter.format(currentModel.getUpdateTs()));
    }

    return Collections.singletonList(newModel);
  }

  @Override
  protected JpJsicCodeMap getCurrentRecord(JpJsicCodeMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("JP.GET.SPECIFIC_JSIC_CODE_MAP");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("JSIC_CD", model.getJsicCd() == null ? "" : model.getJsicCd());
    q.setParameter("SUB_INDUSTRY_CD", model.getSubIndustryCd() == null ? "" : model.getSubIndustryCd());
    q.setParameter("ISU_CD", model.getIsuCd() == null ? "" : model.getIsuCd());
    q.setParameter("ISIC_CD", model.getIsicCd() == null ? "" : model.getIsicCd());
    q.setParameter("DEPT", model.getDept() == null ? "" : model.getDept());

    return q.getSingleResult(JpJsicCodeMap.class);
  }

  @Override
  protected JpJsicCodeMap createFromModel(JpJsicCodeMapModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    JpJsicCodeMap jpJsicCodeMap = new JpJsicCodeMap();
    JpJsicCodeMapPK pk = new JpJsicCodeMapPK();

    pk.setJsicCd(model.getJsicCd());
    pk.setSubIndustryCd(model.getSubIndustryCd());
    pk.setIsuCd(model.getIsuCd());
    pk.setIsicCd(model.getIsicCd());
    pk.setDept(model.getDept());

    jpJsicCodeMap.setId(pk);
    jpJsicCodeMap.setSectorCd(model.getSectorCd());

    Timestamp ts = SystemUtil.getCurrentTimestamp();
    jpJsicCodeMap.setCreateTs(ts);
    jpJsicCodeMap.setUpdateTs(ts);

    AppUser user = AppUser.getUser(request);
    jpJsicCodeMap.setCreateBy(user.getIntranetId());
    jpJsicCodeMap.setUpdateBy(user.getIntranetId());

    return jpJsicCodeMap;
  }

  @Override
  protected void doBeforeInsert(JpJsicCodeMap entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);

  }

  @Override
  protected void doBeforeUpdate(JpJsicCodeMap entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);
  }

}
