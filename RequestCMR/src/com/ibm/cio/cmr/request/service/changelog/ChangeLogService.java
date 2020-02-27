/**
 * 
 */
package com.ibm.cio.cmr.request.service.changelog;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.RequestChangeLog;
import com.ibm.cio.cmr.request.model.changelog.ChangeLogModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Eduard Bernardo
 * 
 */
@Component
public class ChangeLogService extends BaseService<ChangeLogModel, RequestChangeLog> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(ChangeLogService.class);
  }

  @Override
  protected void performTransaction(ChangeLogModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {

  }

  @Override
  protected List<ChangeLogModel> doSearch(ChangeLogModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    List<ChangeLogModel> list = new ArrayList<ChangeLogModel>();
    SimpleDateFormat timeFormat = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT") + " HH:mm:ss");
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("CHANGELOG.SEARCH");
    PreparedQuery query = new PreparedQuery(entityManager, sql);

    String requestId = request.getParameter("requestIdStr");
    String userId = request.getParameter("userId");
    String tablName = request.getParameter("tablName");
    String changeDateFrom = request.getParameter("changeDateFrom");
    String changeDateTo = request.getParameter("changeDateTo");
    String loadRec = request.getParameter("loadRec");
    String cmrNo = request.getParameter("cmrNo");
    if (!"Y".equalsIgnoreCase(loadRec)) {
      return list; // search function has not yet been performed
    }

    if (StringUtils.isEmpty(userId)) {
      userId = ""; // ensure userId is an empty string
    }
    query.setParameter("USER_ID", "%" + userId.trim().toUpperCase() + "%");

    if (!StringUtils.isEmpty(requestId)) {
      query.append("and trim(REQUEST_ID) = :REQUEST_ID");
      query.setParameter("REQUEST_ID", requestId.trim());
    }

    if (!StringUtils.isEmpty(tablName)) {
      query.append("and trim(upper(TABL_NAME)) = :TABL_NAME");
      query.setParameter("TABL_NAME", tablName.trim().toUpperCase());
    }

    if (!StringUtils.isEmpty(changeDateFrom)) {
      Date dateFrom = timeFormat.parse(changeDateFrom + " 00:00:00");
      query.append("and CHANGE_TS >= :CHANGE_FROM");
      query.setParameter("CHANGE_FROM", new Timestamp(dateFrom.getTime()));
    }

    if (!StringUtils.isEmpty(changeDateTo)) {
      Date dateTo = timeFormat.parse(changeDateTo + " 23:59:59");
      query.append("and CHANGE_TS <= :CHANGE_TO");
      query.setParameter("CHANGE_TO", new Timestamp(dateTo.getTime()));
    }

    if (!StringUtils.isEmpty(cmrNo)) {
      query.append("and ((REQUEST_ID in (select REQ_ID from CREQCMR.DATA where CMR_NO = :CMR_NO))");
      query.append("  or (REQUEST_ID in (select PAR_REQ_ID from CREQCMR.MASS_CREATE where CMR_NO = :CMR_NO))");
      query.append("  or (REQUEST_ID in (select PAR_REQ_ID from CREQCMR.MASS_UPDT where CMR_NO = :CMR_NO)))");
      query.setParameter("CMR_NO", cmrNo);
    }

    query.append("order by CHANGE_TS desc");

    query.setForReadOnly(true);
    List<RequestChangeLog> logs = query.getResults(RequestChangeLog.class);

    ChangeLogModel logModel = null;
    for (RequestChangeLog log : logs) {
      logModel = new ChangeLogModel();
      copyValuesFromEntity(log, logModel);
      logModel.setChangeTsStr(formatter.format(log.getId().getChangeTs()));
      list.add(logModel);
    }

    return list;
  }

  @Override
  protected RequestChangeLog getCurrentRecord(ChangeLogModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected RequestChangeLog createFromModel(ChangeLogModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

}
