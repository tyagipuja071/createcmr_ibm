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
import com.ibm.cio.cmr.request.entity.GCARSUpdtQueue;
import com.ibm.cio.cmr.request.model.code.GCARSUpdtQueueModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class GCARSUpdtQueueService extends BaseService<GCARSUpdtQueueModel, GCARSUpdtQueue> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(GCARSUpdtQueueService.class);
  }

  @Override
  protected void performTransaction(GCARSUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<GCARSUpdtQueueModel> doSearch(GCARSUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));

    String sourceName = request.getParameter("sourceName");
    String searchCriteria = request.getParameter("searchCriteria");

    PreparedQuery q = null;

    String sqlQuery = "";
    String paramKey = "";
    String paramValue = "";

    if (StringUtils.isNotBlank(sourceName) && StringUtils.isNotEmpty(searchCriteria)) {
      if (searchCriteria.equals("CRTD")) {
        sqlQuery = "BR.GET.GCARS_UPDT_QUEUE_BY_CREATE_DT";
        paramKey = "CREATE_DT";
        paramValue = "%" + sourceName + "%";
      } else if (searchCriteria.equals("CMRN")) {
        sqlQuery = "BR.GET.GCARS_UPDT_QUEUE_BY_CMR_NO";
        paramKey = "CMR_NO";
        paramValue = sourceName;
      } else if (searchCriteria.equals("SRCN")) {
        sqlQuery = "BR.GET.GCARS_UPDT_QUEUE";
        paramKey = "SOURCE_NAME";
        paramValue = "%" + sourceName + "%";
      } else if (searchCriteria.equals("PROCS")) {
        sqlQuery = "BR.GET.GCARS_UPDT_QUEUE_BY_PROC_STATUS";
        paramKey = "PROC_STATUS";
        paramValue = sourceName;
      } else {
        sqlQuery = "BR.GET.GCARS_UPDT_QUEUE";
        paramKey = "SOURCE_NAME";
        paramValue = "%" + sourceName + "%";
      }
    } else {
      if (StringUtils.isBlank(sourceName)) {
        sourceName = "";
      }
      sqlQuery = "BR.GET.GCARS_UPDT_QUEUE";
      paramKey = "SOURCE_NAME";
      paramValue = "%" + sourceName + "%";
    }

    String sql = ExternalizedQuery.getSql(sqlQuery);
    q = new PreparedQuery(entityManager, sql);
    q.setParameter(paramKey, paramValue);
    q.setForReadOnly(true);

    List<GCARSUpdtQueue> gcarsUpdtQueueList = q.getResults(GCARSUpdtQueue.class);
    List<GCARSUpdtQueueModel> list = new ArrayList<>();

    for (GCARSUpdtQueue element : gcarsUpdtQueueList) {
      GCARSUpdtQueueModel gcarsUpdtQueueModel = new GCARSUpdtQueueModel();

      gcarsUpdtQueueModel.setSourceName(element.getId().getSourceName());
      gcarsUpdtQueueModel.setSeqNo(element.getId().getSeqNo());
      gcarsUpdtQueueModel.setCmrIssuingCntry(element.getId().getCmrIssuingCntry());
      gcarsUpdtQueueModel.setCmrNo(element.getId().getCmrNo());
      gcarsUpdtQueueModel.setCodCondition(element.getCodCondition());
      gcarsUpdtQueueModel.setCodRsn(element.getCodRsn());
      gcarsUpdtQueueModel.setCodEffDate(element.getCodEffDate());
      gcarsUpdtQueueModel.setProcStatus(element.getProcStatus());
      gcarsUpdtQueueModel.setProcMsg(element.getProcMsg());
      gcarsUpdtQueueModel.setCreatedBy(element.getCreatedBy());
      gcarsUpdtQueueModel.setCreateDt(element.getCreateDt());
      gcarsUpdtQueueModel.setUpdatedBy(element.getUpdatedBy());
      gcarsUpdtQueueModel.setUpdateDt(element.getUpdateDt());
      gcarsUpdtQueueModel.setKatr10(element.getKatr10());
      gcarsUpdtQueueModel.setCreatedTsStr(formatter.format(element.getCreateDt()));
      gcarsUpdtQueueModel.setUpdatedTsStr(formatter.format(element.getUpdateDt()));

      list.add(gcarsUpdtQueueModel);
    }

    return list;
  }

  @Override
  protected GCARSUpdtQueue getCurrentRecord(GCARSUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected GCARSUpdtQueue createFromModel(GCARSUpdtQueueModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

}
