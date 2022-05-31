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
import com.ibm.cio.cmr.request.entity.USBusinessPartnerMaster;
import com.ibm.cio.cmr.request.model.code.USBusinessPartnerMasterModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class USBusinessPartnerMasterService extends BaseService<USBusinessPartnerMasterModel, USBusinessPartnerMaster> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USBusinessPartnerMasterService.class);
  }

  @Override
  protected void performTransaction(USBusinessPartnerMasterModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<USBusinessPartnerMasterModel> doSearch(USBusinessPartnerMasterModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("US.GET.US_BP_MASTER");

    String companyNo = request.getParameter("companyNo");

    if (StringUtils.isBlank(companyNo)) {
      companyNo = "";
    }

    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setParameter("COMPANY_NO", "%" + companyNo + "%");

    q.setForReadOnly(true);

    List<USBusinessPartnerMaster> usBusinessPartnerMasterList = q.getResults(USBusinessPartnerMaster.class);
    List<USBusinessPartnerMasterModel> list = new ArrayList<>();

    for (USBusinessPartnerMaster element : usBusinessPartnerMasterList) {
      USBusinessPartnerMasterModel usBusinessPartnerMasterModel = new USBusinessPartnerMasterModel();

      usBusinessPartnerMasterModel.setMandt(element.getId().getMandt());
      usBusinessPartnerMasterModel.setCompanyNo(element.getId().getCompanyNo());
      usBusinessPartnerMasterModel.setCmrNo(element.getCmrNo());
      usBusinessPartnerMasterModel.setKatr10(element.getKatr10());
      usBusinessPartnerMasterModel.setLoevm(element.getLoevm());

      usBusinessPartnerMasterModel.setCreatedBy(element.getCreatedBy());
      usBusinessPartnerMasterModel.setCreateDt(element.getCreateDt());
      usBusinessPartnerMasterModel.setCreatedTsStr(formatter.format(element.getCreateDt()));

      usBusinessPartnerMasterModel.setUpdatedBy(element.getUpdatedBy());
      usBusinessPartnerMasterModel.setUpdateDt(element.getUpdateDt());
      usBusinessPartnerMasterModel.setUpdatedTsStr(formatter.format(element.getUpdateDt()));

      usBusinessPartnerMasterModel.setUpdateType(element.getUpdateType());

      list.add(usBusinessPartnerMasterModel);
    }

    return list;
  }

  @Override
  protected USBusinessPartnerMaster getCurrentRecord(USBusinessPartnerMasterModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    return null;
  }

  @Override
  protected USBusinessPartnerMaster createFromModel(USBusinessPartnerMasterModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    return null;
  }

}
