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
import com.ibm.cio.cmr.request.entity.USCompany;
import com.ibm.cio.cmr.request.model.code.USCompanyModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * 
 * @author Priyanka Kandhare
 *
 */

@Component
public class USCompanyService extends BaseService<USCompanyModel, USCompany> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USCompanyService.class);
  }

  @Override
  protected void performTransaction(USCompanyModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<USCompanyModel> doSearch(USCompanyModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("US.GET.US_COMPANY");

    String compNo = request.getParameter("compNo");

    if (StringUtils.isBlank(compNo)) {
      compNo = "";
    }

    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT").toString());
    q.setParameter("COMP_NO", "%" + compNo + "%");
    q.setForReadOnly(true);

    List<USCompany> usCompaniesList = q.getResults(USCompany.class);
    List<USCompanyModel> list = new ArrayList<USCompanyModel>();

    for (USCompany element : usCompaniesList) {

      USCompanyModel usCompanyModel = new USCompanyModel();
      usCompanyModel.setMandt(element.getId().getMandt());
      usCompanyModel.setCompNo(element.getId().getCompNo());
      usCompanyModel.setEntNo(element.getEntNo());
      usCompanyModel.setCompLegalName(element.getCompLegalName());
      usCompanyModel.setLoevm(element.getLoevm());
      usCompanyModel.setKatr10(element.getKatr10());

      usCompanyModel.setCreateBy(element.getCreateBy());
      usCompanyModel.setCreateDt(element.getCreateDt());
      usCompanyModel.setCreatedTsStr(formatter.format(element.getCreateDt()));

      usCompanyModel.setUpdateBy(element.getUpdateBy());
      usCompanyModel.setUpdateDt(element.getUpdateDt());
      usCompanyModel.setUpdatedTsStr(formatter.format(element.getUpdateDt()));

      usCompanyModel.setUpdateType(element.getUpdateType());

      list.add(usCompanyModel);
    }

    return list;
  }

  @Override
  protected USCompany getCurrentRecord(USCompanyModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected USCompany createFromModel(USCompanyModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

}
