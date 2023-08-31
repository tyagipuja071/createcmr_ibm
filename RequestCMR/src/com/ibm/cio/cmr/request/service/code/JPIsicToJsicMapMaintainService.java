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
import com.ibm.cio.cmr.request.entity.JpIsicToJsicMap;
import com.ibm.cio.cmr.request.entity.JpIsicToJsicMapPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.JpIsicToJsicMapModel;
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
public class JPIsicToJsicMapMaintainService extends BaseService<JpIsicToJsicMapModel, JpIsicToJsicMap> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(JPIsicToJsicMapMaintainService.class);
  }

  @Override
  protected void performTransaction(JpIsicToJsicMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<JpIsicToJsicMapModel> doSearch(JpIsicToJsicMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    JpIsicToJsicMap currentModel = getCurrentRecord(model, entityManager, request);
    JpIsicToJsicMapModel newModel = new JpIsicToJsicMapModel();

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
  protected JpIsicToJsicMap getCurrentRecord(JpIsicToJsicMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("JP.GET.SPECIFIC_ISIC_TO_JSIC_MAP");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("JSIC_CD", model.getJsicCd() == null ? "" : model.getJsicCd());
    q.setParameter("MANDT", model.getMandt() == null ? "" : model.getMandt());

    return q.getSingleResult(JpIsicToJsicMap.class);
  }

  @Override
  protected JpIsicToJsicMap createFromModel(JpIsicToJsicMapModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    JpIsicToJsicMap jpIsicToJsicMap = new JpIsicToJsicMap();
    JpIsicToJsicMapPK pk = new JpIsicToJsicMapPK();

    pk.setJsicCd(model.getJsicCd());
    pk.setMandt(model.getMandt());

    jpIsicToJsicMap.setId(pk);
    jpIsicToJsicMap.setIsicCd(model.getIsicCd());

    Timestamp ts = SystemUtil.getCurrentTimestamp();
    jpIsicToJsicMap.setCreateTs(ts);
    jpIsicToJsicMap.setUpdateTs(ts);

    AppUser user = AppUser.getUser(request);
    jpIsicToJsicMap.setCreateBy(user.getIntranetId());
    jpIsicToJsicMap.setUpdateBy(user.getIntranetId());

    return jpIsicToJsicMap;
  }

  @Override
  protected void doBeforeInsert(JpIsicToJsicMap entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);

  }

  @Override
  protected void doBeforeUpdate(JpIsicToJsicMap entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);
  }

}
