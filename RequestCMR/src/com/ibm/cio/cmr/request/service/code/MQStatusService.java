/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.MqIntfReqData;
import com.ibm.cio.cmr.request.entity.MqIntfReqDataPK;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueuePK;
import com.ibm.cio.cmr.request.model.MQStatusModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class MQStatusService extends BaseService<MQStatusModel, MqIntfReqQueue> {

  private String xml;

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(MQStatusService.class);
  }

  @Override
  protected void performTransaction(MQStatusModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String action = request.getParameter("action");
    AppUser user = AppUser.getUser(request);

    MqIntfReqQueue queue = getCurrentRecord(model, entityManager, request);
    if (queue == null) {
      this.log.warn("Cannot locate MQ Intf Record with ID " + model.getQueryReqId());
      throw new Exception("Cannot locate record with ID " + model.getQueryReqId());
    }
    if ("RESEND".equals(action)) {
      if ("U".equals(queue.getReqType()) || ("C".equals(queue.getReqType()) && "PUB1".equals(queue.getReqStatus()))) {
        queue.setReqStatus("NEW");
        queue.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        queue.setLastUpdtBy(user.getIntranetId());
        updateEntity(queue, entityManager);
      } else {
        throw new Exception("Cannot resend the record with the given status and type.");
      }
    } else if ("STOP".equals(action)) {
      if (queue.getReqStatus().contains("WAIT") || queue.getReqStatus().contains("PUB")) {
        queue.setReqStatus("SER1");
        queue.setErrorCd("GEN");
        queue.setExecpMessage("Stopped by " + user.getIntranetId());
        queue.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        queue.setLastUpdtBy(user.getIntranetId());
        updateEntity(queue, entityManager);
      } else {
        throw new Exception("Cannot stop the record with the given status and type.");
      }
    } else if ("XML".equals(action)) {
      String fileName = request.getParameter("fileName");
      long queryId = model.getQueryReqId();
      MqIntfReqDataPK pk = new MqIntfReqDataPK();
      pk.setFileName(fileName);
      pk.setQueryReqId(queryId);
      MqIntfReqData data = entityManager.find(MqIntfReqData.class, pk);
      if (data != null) {
        this.xml = data.getContents();
      }
    }

  }

  @Override
  protected List<MQStatusModel> doSearch(MQStatusModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("MQSTATUS.SEARCH");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    if (model.getReqId() > 0) {
      query.append(" and a.REQ_ID = :REQ_ID");
      query.setParameter("REQ_ID", model.getReqId());
    }
    if (model.getQueryReqId() > 0) {
      query.append(" and a.QUERY_REQ_ID = :QUERY_ID");
      query.setParameter("QUERY_ID", model.getQueryReqId());
    }
    if (!StringUtils.isEmpty(model.getReqStatus())) {
      switch (model.getReqStatus()) {
      case "P":
        query.append(" and a.REQ_STATUS <> 'COM' and a.REQ_STATUS not like 'SER%'");
        break;
      case "C":
        query.append(" and a.REQ_STATUS = 'COM'");
        break;
      case "E":
        query.append(" and a.REQ_STATUS like 'SER%'");
        break;
      }
    }

    if (!StringUtils.isEmpty(model.getCmrIssuingCntry())) {
      query.append(" and a.CMR_ISSUING_CNTRY = :CNTRY");
      query.setParameter("CNTRY", model.getCmrIssuingCntry());
    }
    query.append("order by a.QUERY_REQ_ID desc");
    query.setForReadOnly(true);
    List<CompoundEntity> results = query.getCompundResults(200, MqIntfReqQueue.class, "MQStatusMapping");

    MqIntfReqQueue queue = null;
    List<MQStatusModel> list = new ArrayList<MQStatusModel>();
    if (results != null) {
      MQStatusModel entry = null;
      long diff = 0;
      long curr = new Date().getTime();
      for (CompoundEntity record : results) {
        queue = record.getEntity(MqIntfReqQueue.class);
        entry = new MQStatusModel();
        copyValuesFromEntity(queue, entry);
        if (entry.getLastUpdtTs() != null) {
          diff = curr - entry.getLastUpdtTs().getTime();
          if (diff > (1000 * 60 * 10)) {
            entry.setWarning("Y");
          }
        }
        entry.setAdminStatus((String) record.getValue("ADMIN_STATUS"));
        entry.setAdminStatusDesc((String) record.getValue("ADMIN_STATUS_DESC"));
        entry.setHasData((String) record.getValue("HAS_DATA"));
        list.add(entry);
      }
    }
    return list;
  }

  @Override
  protected MqIntfReqQueue getCurrentRecord(MQStatusModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    MqIntfReqQueuePK pk = new MqIntfReqQueuePK();
    pk.setQueryReqId(model.getQueryReqId());
    MqIntfReqQueue queue = entityManager.find(MqIntfReqQueue.class, pk);
    return queue;
  }

  @Override
  protected MqIntfReqQueue createFromModel(MQStatusModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  public String getXml() {
    return xml;
  }

}
