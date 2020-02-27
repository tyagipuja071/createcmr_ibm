/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.model.requestentry.CmtModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;

/**
 * Comment log Service for request entry
 * 
 * @author Sonali Jain
 * 
 */
@Component
public class CommentLogService extends BaseService<CmtModel, ReqCmtLog> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(CommentLogService.class);
  }

  @Override
  protected void performTransaction(CmtModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException,
      IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException {

    AppUser user = AppUser.getUser(request);
    try {
      RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), model.getCmt());
    } catch (Exception e) {
      this.log.error("An error occurred during comment saving.", e);
      throw e;
    }
  }

  @Override
  protected List<CmtModel> doSearch(CmtModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    List<CmtModel> results = new ArrayList<CmtModel>();
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.REQ_CMT_LOG.SEARCH_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setForReadOnly(true);
    List<ReqCmtLog> rs = null;
    rs = query.getResults(ReqCmtLog.class);

    CmtModel cmtModel = null;

    String cmt = null;
    Date createTs = null;
    String createByNm = null;
    if (rs != null) {
      for (ReqCmtLog reqCmtLog : rs) {

        cmt = reqCmtLog.getCmt();
        createTs = reqCmtLog.getCreateTs();
        createByNm = reqCmtLog.getCreateByNm();
        cmtModel = new CmtModel();
        cmtModel.setCmt(cmt);
        cmtModel.setCreateTs(createTs);
        cmtModel.setCreateByNm(createByNm);

        results.add(cmtModel);

      }
    }

    return results;
  }

  @Override
  protected ReqCmtLog getCurrentRecord(CmtModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected ReqCmtLog createFromModel(CmtModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    ReqCmtLog list = new ReqCmtLog();
    ReqCmtLogPK pk = new ReqCmtLogPK();
    list.setId(pk);
    copyValuesToEntity(model, list);
    return list;
  }

}
