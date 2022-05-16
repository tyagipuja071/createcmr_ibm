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
import com.ibm.cio.cmr.request.entity.USIbmBo;
import com.ibm.cio.cmr.request.entity.USIbmBoPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.USIbmBoModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

@Component
public class USIbmBoMaintainService extends BaseService<USIbmBoModel, USIbmBo> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USIbmBoMaintainService.class);
  }

  @Override
  protected void performTransaction(USIbmBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<USIbmBoModel> doSearch(USIbmBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    USIbmBo currentModel = getCurrentRecord(model, entityManager, request);
    USIbmBoModel newModel = new USIbmBoModel();

    copyValuesFromEntity(currentModel, newModel);

    newModel.setState(BaseModel.STATE_EXISTING);
    newModel.setCreatedTsStr(formatter.format(currentModel.getCreateDt()));
    newModel.setUpdatedTsStr(formatter.format(currentModel.getUpdateDt()));

    return Collections.singletonList(newModel);
  }

  @Override
  protected USIbmBo getCurrentRecord(USIbmBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("US.GET.US_IBM_BO_BY_I_OFF");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setParameter("I_OFF", model.getiOff() == null ? "" : model.getiOff());

    return q.getSingleResult(USIbmBo.class);
  }

  @Override
  protected USIbmBo createFromModel(USIbmBoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    USIbmBo usIbmBo = new USIbmBo();
    USIbmBoPK pk = new USIbmBoPK();

    pk.setMandt(SystemConfiguration.getValue("MANDT"));
    pk.setiOff(model.getiOff());

    usIbmBo.setId(pk);
    usIbmBo.setaLevel1Value(model.getaLevel1Value());
    usIbmBo.setaLevel2Value(model.getaLevel2Value());
    usIbmBo.setaLevel3Value(model.getaLevel3Value());
    usIbmBo.setaLevel4Value(model.getaLevel4Value());
    usIbmBo.setnOff(model.getnOff());
    usIbmBo.setfDistrcOn(model.getfDistrcOn());
    usIbmBo.setiArOff(model.getiArOff());
    usIbmBo.setfApplicCash(model.getfApplicCash());
    usIbmBo.setfApplicColl(model.getfApplicColl());
    usIbmBo.setfOffFunc(model.getfOffFunc());
    usIbmBo.setqTieLineTelOff(model.getqTieLineTelOff());
    usIbmBo.settInqAddrLine1(model.gettInqAddrLine1());
    usIbmBo.settInqAddrLine2(model.gettInqAddrLine2());
    usIbmBo.setnInqCity(model.getnInqCity());
    usIbmBo.setnInqSt(model.getnInqSt());
    usIbmBo.setcInqZip(model.getcInqZip());
    usIbmBo.setcInqCnty(model.getcInqCnty());
    usIbmBo.setnInqScc(model.getnInqScc());
    usIbmBo.settRemitToAddrL1(model.gettRemitToAddrL1());
    usIbmBo.settRemitToAddrL2(model.gettRemitToAddrL2());
    usIbmBo.setnRemitToCity(model.getnRemitToCity());
    usIbmBo.setnRemitToSt(model.getnRemitToSt());
    usIbmBo.setcRemitToZip(model.getcRemitToZip());
    usIbmBo.setcRemitToCnty(model.getcRemitToCnty());
    usIbmBo.setnRemitToScc(model.getnRemitToScc());
    usIbmBo.settPhysicAddrLn1(model.gettPhysicAddrLn1());
    usIbmBo.settPhysicAddrLn2(model.gettPhysicAddrLn2());
    usIbmBo.setnPhysicCity(model.getnPhysicCity());
    usIbmBo.setnPhysicSt(model.getnPhysicSt());
    usIbmBo.setcPhysicZip(model.getcPhysicZip());
    usIbmBo.setcPhysicCnty(model.getcPhysicCnty());
    usIbmBo.setnPhysicScc(model.getnPhysicScc());
    usIbmBo.setiCtrlgOff(model.getiCtrlgOff());

    // usIbmBo.setUpdateType(BaseModel.ACT_INSERT);

    Timestamp ts = SystemUtil.getCurrentTimestamp();
    usIbmBo.setCreateDt(ts);
    usIbmBo.setUpdateDt(ts);

    AppUser user = AppUser.getUser(request);
    usIbmBo.setCreatedBy(user.getIntranetId());
    usIbmBo.setUpdatedBy(user.getIntranetId());

    return usIbmBo;
  }

  @Override
  protected void doBeforeInsert(USIbmBo entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    entity.setUpdateType(BaseModel.ACT_INSERT);

    AppUser user = AppUser.getUser(request);
    entity.setCreatedBy(user.getIntranetId());
    entity.setCreateDt(this.currentTimestamp);
    entity.setUpdatedBy(user.getIntranetId());
    entity.setUpdateDt(this.currentTimestamp);

    String cInqZip = request.getParameter("cInqZip");
    String cPhysicZip = request.getParameter("cPhysicZip");

    if (StringUtils.isBlank(cInqZip)) {
      entity.setcInqZip(null);
    }

    if (StringUtils.isBlank(cPhysicZip)) {
      entity.setcPhysicZip(null);
    }

  }

  @Override
  protected void doBeforeUpdate(USIbmBo entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);

    entity.setUpdateType(BaseModel.ACT_UPDATE);

    entity.setUpdatedBy(AppUser.getUser(request).getIntranetId());
    entity.setUpdateDt(SystemUtil.getCurrentTimestamp());

    String cInqZip = request.getParameter("cInqZip");
    String cPhysicZip = request.getParameter("cPhysicZip");

    if (StringUtils.isBlank(cInqZip)) {
      entity.setcInqZip(null);
    }

    if (StringUtils.isBlank(cPhysicZip)) {
      entity.setcPhysicZip(null);
    }

  }

}
