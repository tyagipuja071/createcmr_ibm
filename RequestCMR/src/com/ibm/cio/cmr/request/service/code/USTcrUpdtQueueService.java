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
import com.ibm.cio.cmr.request.entity.USTCRUpdtQueue;
import com.ibm.cio.cmr.request.model.code.USTCRUpdtQueueModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class USTcrUpdtQueueService extends BaseService<USTCRUpdtQueueModel, USTCRUpdtQueue> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(USTcrUpdtQueueService.class);
  }

  @Override
  protected void performTransaction(USTCRUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<USTCRUpdtQueueModel> doSearch(USTCRUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("US.GET.US_TCR_UPDT_QUEUE");

    String tcrFileNm = request.getParameter("tcrFileNm");

    if (StringUtils.isBlank(tcrFileNm)) {
      tcrFileNm = "";
    }

    PreparedQuery q = new PreparedQuery(entityManager, sql);

    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setParameter("TCR_INPUT_FILE_NM", "%" + tcrFileNm + "%");

    q.setForReadOnly(true);

    List<USTCRUpdtQueue> usTcrUpdtQueueList = q.getResults(USTCRUpdtQueue.class);
    List<USTCRUpdtQueueModel> list = new ArrayList<>();

    for (USTCRUpdtQueue element : usTcrUpdtQueueList) {
      USTCRUpdtQueueModel usTcrUpdtQueueModel = new USTCRUpdtQueueModel();

      usTcrUpdtQueueModel.setMandt(element.getId().getMandt());
      usTcrUpdtQueueModel.setTcrFileNm(element.getId().getTcrFileNm());
      usTcrUpdtQueueModel.setSeqNo(element.getId().getSeqNo());
      usTcrUpdtQueueModel.setLineContent(element.getLineContent());
      usTcrUpdtQueueModel.setCmrNo(element.getCmrNo());
      usTcrUpdtQueueModel.setTaxCustTyp1(element.getTaxCustTyp1());
      usTcrUpdtQueueModel.setTaxClass1(element.getTaxClass1());
      usTcrUpdtQueueModel.setTaxCustTyp2(element.getTaxCustTyp2());
      usTcrUpdtQueueModel.setTaxClass2(element.getTaxClass2());
      usTcrUpdtQueueModel.setTaxCustTyp3(element.getTaxCustTyp3());
      usTcrUpdtQueueModel.setTaxClass3(element.getTaxClass3());
      usTcrUpdtQueueModel.setTaxExemptStatus1(element.getTaxExemptStatus1());
      usTcrUpdtQueueModel.setTaxExemptStatus2(element.getTaxExemptStatus2());
      usTcrUpdtQueueModel.setTaxExemptStatus3(element.getTaxExemptStatus3());
      usTcrUpdtQueueModel.setProcStatus(element.getProcStatus());
      usTcrUpdtQueueModel.setProcMsg(element.getProcMsg());
      usTcrUpdtQueueModel.setCreatedBy(element.getCreateBy());
      usTcrUpdtQueueModel.setCreateDt(element.getCreateDt());
      usTcrUpdtQueueModel.setCreatedTsStr(formatter.format(element.getCreateDt()));
      usTcrUpdtQueueModel.setUpdatedBy(element.getUpdateBy());
      usTcrUpdtQueueModel.setUpdateDt(element.getUpdateDt());
      usTcrUpdtQueueModel.setUpdatedTsStr(formatter.format(element.getUpdateDt()));
      usTcrUpdtQueueModel.setKatr10(element.getKatr10());

      list.add(usTcrUpdtQueueModel);
    }

    return list;
  }

  @Override
  protected USTCRUpdtQueue getCurrentRecord(USTCRUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected USTCRUpdtQueue createFromModel(USTCRUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

}
