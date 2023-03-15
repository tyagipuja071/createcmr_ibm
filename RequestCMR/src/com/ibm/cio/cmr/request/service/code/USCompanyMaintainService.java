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
import com.ibm.cio.cmr.request.entity.USCompany;
import com.ibm.cio.cmr.request.entity.USCompanyPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.USCompanyModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * 
 * @author Priyanka Kandhare
 *
 */

@Component
public class USCompanyMaintainService extends BaseService<USCompanyModel, USCompany> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USCompanyMaintainService.class);
  }

  @Override
  protected void performTransaction(USCompanyModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<USCompanyModel> doSearch(USCompanyModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    USCompany currentModel = getCurrentRecord(model, entityManager, request);
    USCompanyModel newModel = new USCompanyModel();

    copyValuesFromEntity(currentModel, newModel);

    newModel.setState(BaseModel.STATE_EXISTING);
    newModel.setCreatedTsStr(formatter.format(currentModel.getCreateDt()));
    newModel.setUpdatedTsStr(formatter.format(currentModel.getUpdateDt()));

    return Collections.singletonList(newModel);
  }

  @Override
  protected USCompany getCurrentRecord(USCompanyModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("US.GET.US_COMPANY_BY_COMP_NO");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setParameter("COMP_NO", model.getCompNo() == null ? "" : model.getCompNo());

    return q.getSingleResult(USCompany.class);
  }

  @Override
  protected USCompany createFromModel(USCompanyModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    USCompany usCompany = new USCompany();
    USCompanyPK pk = new USCompanyPK();

    pk.setMandt(SystemConfiguration.getValue("MANDT"));
    pk.setCompNo(model.getCompNo());

    usCompany.setId(pk);
    usCompany.setEntNo(model.getEntNo());
    usCompany.setCompLegalName(model.getCompLegalName());
    usCompany.setLoevm(model.getLoevm());
    usCompany.setKatr10(model.getKatr10());

    Timestamp ts = SystemUtil.getCurrentTimestamp();
    usCompany.setCreateDt(ts);
    usCompany.setUpdateDt(ts);

    AppUser user = AppUser.getUser(request);
    usCompany.setCreateBy(user.getIntranetId());
    usCompany.setUpdateBy(user.getIntranetId());

    return usCompany;
  }

  @Override
  protected void doBeforeInsert(USCompany entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
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
  protected void doBeforeUpdate(USCompany entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    entity.setUpdateType(BaseModel.ACT_UPDATE);

    entity.setUpdateBy(AppUser.getUser(request).getIntranetId());
    entity.setUpdateDt(SystemUtil.getCurrentTimestamp());

    if ("X".equals(entity.getLoevm())) {
      entity.setLoevm("X");
      entity.setUpdateType(BaseModel.ACT_DELETE);
    } else {
      entity.setLoevm("");
      entity.setUpdateType(BaseModel.ACT_UPDATE);
    }

    if (StringUtils.isBlank(entity.getKatr10())) {
      entity.setKatr10("");
    }

  }

}
