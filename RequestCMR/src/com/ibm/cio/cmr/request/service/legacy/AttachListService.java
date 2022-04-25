/**
 * 
 */
package com.ibm.cio.cmr.request.service.legacy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Attachment;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.legacy.AttachListModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class AttachListService extends BaseSimpleService<AttachListModel> {

  private static final Logger LOG = Logger.getLogger(AttachListService.class);

  @Override
  protected AttachListModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    AttachListModel model = new AttachListModel();
    long reqId = (long) params.getParam("reqId");
    model.setReqId(reqId);
    AdminPK adminPk = new AdminPK();
    adminPk.setReqId(reqId);
    Admin admin = entityManager.find(Admin.class, adminPk);
    if (admin == null) {
      return model;
    }

    entityManager.detach(admin);

    LOG.debug("Getting file xls under Request " + reqId);
    // extract files from filename first
    String fileName = admin.getFileName();
    if (!StringUtils.isBlank(fileName)) {
      File fileNameO = new File(fileName).getParentFile();
      if (fileNameO.exists() && fileNameO.isDirectory()) {
        LOG.debug("Getting files under " + fileNameO.getAbsolutePath());
        for (File f : fileNameO.listFiles()) {
          model.getFiles().add(f.getAbsolutePath());
        }
      } else {
        LOG.debug("Directory " + fileNameO.getAbsolutePath() + " does not exist.");
      }
    }

    LOG.debug("Getting attachments under Request " + reqId);
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ATTACHMENT.SEARCH_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    List<Attachment> attachments = query.getResults(Attachment.class);
    if (attachments != null && !attachments.isEmpty()) {
      List<File> parents = new ArrayList<File>();
      for (Attachment attachment : attachments) {
        File fileNameO = new File(attachment.getId().getDocLink()).getParentFile();
        if (fileNameO.exists() && fileNameO.isDirectory() && !parents.contains(fileNameO)) {
          parents.add(fileNameO);
        } else {
          LOG.debug("Directory " + fileNameO.getAbsolutePath() + " does not exist.");
        }
      }
      for (File dir : parents) {
        LOG.debug("Getting files under " + dir.getAbsolutePath());
        for (File f : dir.listFiles()) {
          model.getFiles().add(f.getAbsolutePath());
        }
      }
    }
    // get list from attachment
    return model;
  }

}
