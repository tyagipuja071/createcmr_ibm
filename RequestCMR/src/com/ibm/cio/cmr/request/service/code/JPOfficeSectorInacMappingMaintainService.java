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
import com.ibm.cio.cmr.request.entity.JPOfficeSectorInacMap;
import com.ibm.cio.cmr.request.entity.JPOfficeSectorInacMapPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.JPOfficeSectorInacMapModel;
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
public class JPOfficeSectorInacMappingMaintainService extends BaseService<JPOfficeSectorInacMapModel, JPOfficeSectorInacMap> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(JPOfficeSectorInacMappingMaintainService.class);
  }

  @Override
  protected void performTransaction(JPOfficeSectorInacMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<JPOfficeSectorInacMapModel> doSearch(JPOfficeSectorInacMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    JPOfficeSectorInacMap currentModel = getCurrentRecord(model, entityManager, request);
    JPOfficeSectorInacMapModel newModel = new JPOfficeSectorInacMapModel();

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
  protected JPOfficeSectorInacMap getCurrentRecord(JPOfficeSectorInacMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    String sql = ExternalizedQuery.getSql("JP.GET.SPECIFIC_JP_OFFICE_SECTOR_INAC_MAPPING");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("OFFICE_CD", model.getOfficeCd() == null ? "" : model.getOfficeCd());
    q.setParameter("INAC_CD", model.getInacCd() == null ? "" : model.getInacCd());
    q.setParameter("SECTOR_CD", model.getSectorCd() == null ? "" : model.getSectorCd());
    q.setParameter("AP_CUST_CLUSTER_ID", model.getApCustClusterId() == null ? "" : model.getApCustClusterId());

    return q.getSingleResult(JPOfficeSectorInacMap.class);
  }

  @Override
  protected JPOfficeSectorInacMap createFromModel(JPOfficeSectorInacMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    JPOfficeSectorInacMap jPOfficeSectorInacMap = new JPOfficeSectorInacMap();
    JPOfficeSectorInacMapPK pk = new JPOfficeSectorInacMapPK();

    pk.setOfficeCd(model.getOfficeCd());
    pk.setInacCd(model.getInacCd());
    pk.setSectorCd(model.getSectorCd());
    pk.setApCustClusterId(model.getApCustClusterId());

    jPOfficeSectorInacMap.setId(pk);

    Timestamp ts = SystemUtil.getCurrentTimestamp();
    jPOfficeSectorInacMap.setCreateTs(ts);
    jPOfficeSectorInacMap.setUpdateTs(ts);

    AppUser user = AppUser.getUser(request);
    jPOfficeSectorInacMap.setCreateBy(user.getIntranetId());
    jPOfficeSectorInacMap.setUpdateBy(user.getIntranetId());

    return jPOfficeSectorInacMap;
  }

  @Override
  protected void doBeforeInsert(JPOfficeSectorInacMap entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);

  }

  @Override
  protected void doBeforeUpdate(JPOfficeSectorInacMap entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);

  }

}
