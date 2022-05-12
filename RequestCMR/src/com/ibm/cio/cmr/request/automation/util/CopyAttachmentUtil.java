package com.ibm.cio.cmr.request.automation.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Attachment;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.AttachmentService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * 
 * @author Shivangi
 *
 */
public class CopyAttachmentUtil {
  private static final Logger LOG = Logger.getLogger(CopyAttachmentUtil.class);
  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

  public static void copyAttachmentsByType(EntityManager entityManager, RequestData requestData, long childReqId, String attachmentType)
      throws CmrException, IOException {
    Admin admin = requestData.getAdmin();
    long reqId = admin.getId().getReqId();
    String sql = ExternalizedQuery.getSql("ATTACHMENT.GET_BY_TYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("TYPE", attachmentType);
    List<Attachment> attachments = query.getResults(Attachment.class);
    if (attachments != null) {
      for (Attachment attachment : attachments) {
        copyAttachment(entityManager, requestData, childReqId, attachment);
      }
    }
  }

  public static void copyAttachment(EntityManager entityManager, RequestData requestData, long childReqId, Attachment attachment)
      throws CmrException, IOException {
    AppUser user = new AppUser();
    user.setIntranetId(requestData.getAdmin().getRequesterId());
    user.setBluePagesName(requestData.getAdmin().getRequesterNm());
    if (attachment != null) {
      String docLink = attachment.getId().getDocLink();
      if (StringUtils.isNotBlank(docLink) && docLink.endsWith(".zip")) {
        docLink = docLink.substring(0, docLink.length() - 4);
      }
      File file = new File(docLink + ".zip");
      // JZ: add here the last 3 historical locations of the file
      if (!file.exists()) {
        String name = file.getName();
        File parent = file.getParentFile();
        String reqIdDir = parent.getName();
        // 2020 prod
        file = new File("/gsa/nhbgsa/projects/c/cmma2020/prod" + File.separator + reqIdDir + File.separator + name);
        LOG.debug(" - Checking historical location: " + file.getAbsolutePath());
        if (!file.exists()) {
          // 2020 prod2
          file = new File("/gsa/nhbgsa/projects/c/cmma2020/prod2" + File.separator + reqIdDir + File.separator + name);
          LOG.debug(" - Checking historical location: " + file.getAbsolutePath());
          if (!file.exists()) {
            // 2018
            file = new File("/gsa/nhbgsa/projects/c/cmma2018/prod" + File.separator + reqIdDir + File.separator + name);
            LOG.debug(" - Checking historical location: " + file.getAbsolutePath());
            if (!file.exists()) {
              throw new CmrException(MessageUtil.ERROR_FILE_DL_ERROR);
            }
          }
        }
      }
      ZipFile zip = new ZipFile(file);
      try {
        Enumeration<?> entry = zip.entries();
        if (entry.hasMoreElements()) {
          ZipEntry document = (ZipEntry) entry.nextElement();
          InputStream is = zip.getInputStream(document);
          try {
            String dlfileName = docLink.substring(docLink.lastIndexOf("/") + 1);
            String type = "";
            try {
              type = MIME_TYPES.getContentType(docLink);
            } catch (Exception e) {
            }
            if (StringUtils.isEmpty(type)) {
              type = "application/octet-stream";
            }
            try {
              AttachmentService attachmentService = new AttachmentService();
              attachmentService.addExternalAttachment(entityManager, user, childReqId, attachment.getDocContent(), dlfileName,
                  "Save the attachment to the Child Request", is);
            } catch (Exception e) {
              LOG.warn("Unable to save Attachment", e);
            }
          } finally {
            is.close();
          }
        }
      } finally {
        zip.close();
      }
    }
  }
}