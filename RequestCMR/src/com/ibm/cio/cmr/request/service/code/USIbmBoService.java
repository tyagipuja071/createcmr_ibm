package com.ibm.cio.cmr.request.service.code;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.USIbmBo;
import com.ibm.cio.cmr.request.model.code.USIbmBoModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class USIbmBoService extends BaseService<USIbmBoModel, USIbmBo> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USIbmBoService.class);
  }

  @Override
  protected void performTransaction(USIbmBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<USIbmBoModel> doSearch(USIbmBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("US.GET.US_IBM_BO");

    String iOff = request.getParameter("iOff");

    if (StringUtils.isBlank(iOff)) {
      iOff = "";
    }

    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT").toString());
    q.setParameter("I_OFF", "%" + iOff + "%");

    q.setForReadOnly(true);

    List<USIbmBo> usIbmBoList = q.getResults(USIbmBo.class);
    List<USIbmBoModel> list = new ArrayList<USIbmBoModel>();

    for (USIbmBo element : usIbmBoList) {
      USIbmBoModel usIbmBo = new USIbmBoModel();
      usIbmBo.setMandt(element.getId().getMandt());
      usIbmBo.setiOff(element.getId().getiOff());

      usIbmBo.setaLevel1Value(element.getaLevel1Value());
      usIbmBo.setaLevel2Value(element.getaLevel2Value());
      usIbmBo.setaLevel3Value(element.getaLevel3Value());
      usIbmBo.setaLevel4Value(element.getaLevel4Value());

      usIbmBo.setnOff(element.getnOff());
      usIbmBo.setfDistrcOn(element.getfDistrcOn());
      usIbmBo.setiArOff(element.getiArOff());
      usIbmBo.setfApplicCash(element.getfApplicCash());
      usIbmBo.setfApplicColl(element.getfApplicColl());
      usIbmBo.setfOffFunc(element.getfOffFunc());
      usIbmBo.setqTieLineTelOff(element.getqTieLineTelOff());

      usIbmBo.settInqAddrLine1(element.gettInqAddrLine1());
      usIbmBo.settInqAddrLine2(element.gettInqAddrLine2());
      usIbmBo.setnInqCity(element.getnInqCity());
      usIbmBo.setnInqSt(element.getnInqSt());
      usIbmBo.setcInqZip(element.getcInqZip());
      usIbmBo.setcInqCnty(element.getcInqCnty());
      usIbmBo.setnInqScc(element.getnInqScc());

      usIbmBo.settRemitToAddrL1(element.gettRemitToAddrL1());
      usIbmBo.settRemitToAddrL2(element.gettRemitToAddrL2());
      usIbmBo.setnRemitToCity(element.getnRemitToCity());
      usIbmBo.setnRemitToSt(element.getnRemitToSt());
      usIbmBo.setcRemitToZip(element.getcRemitToZip());
      usIbmBo.setcRemitToCnty(element.getcRemitToCnty());
      usIbmBo.setnRemitToScc(element.getnRemitToScc());

      usIbmBo.settPhysicAddrLn1(element.gettPhysicAddrLn1());
      usIbmBo.settPhysicAddrLn2(element.gettPhysicAddrLn2());
      usIbmBo.setnPhysicCity(element.getnPhysicCity());
      usIbmBo.setnPhysicSt(element.getnPhysicSt());
      usIbmBo.setcPhysicZip(element.getcPhysicZip());
      usIbmBo.setcPhysicCnty(element.getcPhysicCnty());
      usIbmBo.setnPhysicScc(element.getnPhysicScc());

      usIbmBo.setiCtrlgOff(element.getiCtrlgOff());

      usIbmBo.setCreatedBy(element.getCreatedBy());
      usIbmBo.setCreateDt(element.getCreateDt());
      usIbmBo.setCreatedTsStr(formatter.format(element.getCreateDt()));

      usIbmBo.setUpdatedBy(element.getUpdatedBy());
      usIbmBo.setUpdateDt(element.getUpdateDt());
      usIbmBo.setUpdatedTsStr(formatter.format(element.getUpdateDt()));

      usIbmBo.setUpdateType(element.getUpdateType());

      list.add(usIbmBo);
    }

    return list;
  }

  @Override
  protected USIbmBo getCurrentRecord(USIbmBoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected USIbmBo createFromModel(USIbmBoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

}
