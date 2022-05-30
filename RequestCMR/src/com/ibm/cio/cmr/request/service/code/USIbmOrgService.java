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
import com.ibm.cio.cmr.request.entity.USIbmOrg;
import com.ibm.cio.cmr.request.model.code.USIbmOrgModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class USIbmOrgService extends BaseService<USIbmOrgModel, USIbmOrg> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USIbmOrgService.class);
  }

  @Override
  protected void performTransaction(USIbmOrgModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<USIbmOrgModel> doSearch(USIbmOrgModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("US.GET.US_IBM_ORG");

    String aLevel1Value = request.getParameter("aLevel1Value");

    if (StringUtils.isBlank(aLevel1Value)) {
      aLevel1Value = "";
    }

    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT").toString());
    q.setParameter("A_LEVEL_1_VALUE", "%" + aLevel1Value + "%");

    q.setForReadOnly(true);

    List<USIbmOrg> usIbmOrgList = q.getResults(USIbmOrg.class);
    List<USIbmOrgModel> list = new ArrayList<USIbmOrgModel>();

    for (USIbmOrg element : usIbmOrgList) {
      USIbmOrgModel usIbmOrg = new USIbmOrgModel();

      usIbmOrg.setMandt(element.getId().getMandt());
      usIbmOrg.setaLevel1Value(element.getId().getaLevel1Value());
      usIbmOrg.setaLevel2Value(element.getId().getaLevel2Value());
      usIbmOrg.setaLevel3Value(element.getId().getaLevel3Value());
      usIbmOrg.setaLevel4Value(element.getId().getaLevel4Value());

      usIbmOrg.setiOrgPrimry(element.getiOrgPrimry());
      usIbmOrg.setiOrgSecndr(element.getiOrgSecndr());
      usIbmOrg.setiOrgPrimryAbbv(element.getiOrgPrimryAbbv());
      usIbmOrg.setiOrgSecndrAbbv(element.getiOrgSecndrAbbv());
      usIbmOrg.setnOrgFull(element.getnOrgFull());

      usIbmOrg.setCreatedBy(element.getCreatedBy());
      usIbmOrg.setCreateDt(element.getCreateDt());
      usIbmOrg.setCreatedTsStr(formatter.format(element.getCreateDt()));

      usIbmOrg.setUpdatedBy(element.getUpdatedBy());
      usIbmOrg.setUpdateDt(element.getUpdateDt());
      usIbmOrg.setUpdatedTsStr(formatter.format(element.getUpdateDt()));

      usIbmOrg.setUpdateType(element.getUpdateType());

      list.add(usIbmOrg);
    }

    return list;
  }

  @Override
  protected USIbmOrg getCurrentRecord(USIbmOrgModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected USIbmOrg createFromModel(USIbmOrgModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

}
