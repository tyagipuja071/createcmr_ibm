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
import com.ibm.cio.cmr.request.entity.JpBoCodesMap;
import com.ibm.cio.cmr.request.entity.JpBoCodesMapPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.JpBoCodesMapModel;
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
public class JPBoCodesMapMaintainService extends BaseService<JpBoCodesMapModel, JpBoCodesMap> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(JPBoCodesMapMaintainService.class);
  }

  @Override
  protected void performTransaction(JpBoCodesMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<JpBoCodesMapModel> doSearch(JpBoCodesMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    JpBoCodesMap currentModel = getCurrentRecord(model, entityManager, request);
    JpBoCodesMapModel newModel = new JpBoCodesMapModel();

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
  protected JpBoCodesMap getCurrentRecord(JpBoCodesMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("JP.GET.SPECIFIC_BO_CODES_MAP");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("SUBSIDIARY_CD", model.getSubsidiaryCd() == null ? "" : model.getSubsidiaryCd());
    q.setParameter("OFFICE_CD", model.getOfficeCd() == null ? "" : model.getOfficeCd());
    q.setParameter("SUB_OFFICE_CD", model.getSubOfficeCd() == null ? "" : model.getSubOfficeCd());

    return q.getSingleResult(JpBoCodesMap.class);
  }

  @Override
  protected JpBoCodesMap createFromModel(JpBoCodesMapModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    JpBoCodesMap JpBoCodesMap = new JpBoCodesMap();
    JpBoCodesMapPK pk = new JpBoCodesMapPK();

    pk.setSubsidiaryCd(model.getSubsidiaryCd());
    pk.setOfficeCd(model.getOfficeCd());
    pk.setSubOfficeCd(model.getSubOfficeCd());

    JpBoCodesMap.setId(pk);
    JpBoCodesMap.setBoCd(model.getBoCd());
    JpBoCodesMap.setFieldSalesCd(model.getFieldSalesCd());
    JpBoCodesMap.setSalesOfficeCd(model.getSalesOfficeCd());
    JpBoCodesMap.setMktgDivCd(model.getMktgDivCd());
    JpBoCodesMap.setMrcCd(model.getMrcCd());
    JpBoCodesMap.setDeptCd(model.getDeptCd());
    JpBoCodesMap.setMktgDeptName(model.getMktgDeptName());
    JpBoCodesMap.setClusterId(model.getClusterId());
    JpBoCodesMap.setClientTierCd(model.getClientTierCd());
    JpBoCodesMap.setIsuCdOverride(model.getIsuCdOverride());
    JpBoCodesMap.setIsicCd(model.getIsicCd());

    Timestamp ts = SystemUtil.getCurrentTimestamp();
    JpBoCodesMap.setCreateTs(ts);
    JpBoCodesMap.setUpdateTs(ts);

    AppUser user = AppUser.getUser(request);
    JpBoCodesMap.setCreateBy(user.getIntranetId());
    JpBoCodesMap.setUpdateBy(user.getIntranetId());

    return JpBoCodesMap;
  }

  @Override
  protected void doBeforeInsert(JpBoCodesMap entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    AppUser user = AppUser.getUser(request);
    entity.setCreateBy(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);

  }

  @Override
  protected void doBeforeUpdate(JpBoCodesMap entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdateBy(user.getIntranetId());
    entity.setUpdateTs(this.currentTimestamp);
  }

}
