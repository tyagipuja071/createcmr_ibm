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
import com.ibm.cio.cmr.request.entity.USEnterprise;
import com.ibm.cio.cmr.request.model.code.USEnterpriseModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * 
 * @author Bikash Das
 *
 */
@Component
public class USEnterpriseService extends BaseService<USEnterpriseModel, USEnterprise> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USEnterpriseService.class);
  }

  @Override
  protected void performTransaction(USEnterpriseModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<USEnterpriseModel> doSearch(USEnterpriseModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("US.GET.US_ENTERPRISE");

    String entNo = request.getParameter("entNo");

    if (StringUtils.isBlank(entNo)) {
      entNo = "";
    }

    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT").toString());
    q.setParameter("ENT_NO", "%" + entNo + "%");
    q.setForReadOnly(true);

    List<USEnterprise> usEnterpriseList = q.getResults(USEnterprise.class);
    List<USEnterpriseModel> list = new ArrayList<USEnterpriseModel>();

    for (USEnterprise element : usEnterpriseList) {

      USEnterpriseModel usEnterpriseModel = new USEnterpriseModel();
      usEnterpriseModel.setMandt(element.getId().getMandt());
      usEnterpriseModel.setEntNo(element.getId().getEntNo());
      usEnterpriseModel.setEntLegalName(element.getEntLegalName());
      usEnterpriseModel.setLoevm(element.getLoevm());
      usEnterpriseModel.setKatr10(element.getKatr10());

      usEnterpriseModel.setCreateBy(element.getCreateBy());
      usEnterpriseModel.setCreateDt(element.getCreateDt());
      usEnterpriseModel.setCreatedTsStr(formatter.format(element.getCreateDt()));

      usEnterpriseModel.setUpdateBy(element.getUpdateBy());
      usEnterpriseModel.setUpdateDt(element.getUpdateDt());
      usEnterpriseModel.setUpdatedTsStr(formatter.format(element.getUpdateDt()));

      usEnterpriseModel.setUpdateType(element.getUpdateType());

      list.add(usEnterpriseModel);
    }

    return list;
  }

  @Override
  protected USEnterprise getCurrentRecord(USEnterpriseModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected USEnterprise createFromModel(USEnterpriseModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

}
