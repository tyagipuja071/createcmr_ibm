package com.ibm.cio.cmr.request.service.requestentry;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Attachment;
import com.ibm.cio.cmr.request.entity.AttachmentPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.requestentry.AttachmentModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Rangoli
 * 
 */
@Component
public class AttachmentService extends BaseService<AttachmentModel, Attachment> {

  private static final SimpleDateFormat SS_NAME_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(AttachmentService.class);
  }

  @Override
  protected void performTransaction(AttachmentModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String action = model.getAction();
    if ("ADD_ATTACHMENT".equals(action)) {
      Attachment attachment = createFromModel(model, entityManager, request);
      createEntity(attachment, entityManager);
    } else if ("REMOVE_FILE".equals(action)) {
      String link = model.getDocLink() + ".zip";
      model.setDocLink(link);
      Attachment attachment = getCurrentRecord(model, entityManager, request);
      File file = new File(link);
      if (file.exists()) {
        file.delete();
      }
      deleteEntity(attachment, entityManager);
    }
  }

  @Override
  protected List<AttachmentModel> doSearch(AttachmentModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<AttachmentModel> results = new ArrayList<AttachmentModel>();

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ATTACHMENT.SEARCH_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("REQ_ID", model.getReqId());

    List<Attachment> rs = query.getResults(50, Attachment.class);
    AttachmentModel attachmentModel = null;
    for (Attachment attachment : rs) {
      attachmentModel = new AttachmentModel();
      copyValuesFromEntity(attachment, attachmentModel);

      DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      String attachTsStr = "";
      try {
        attachTsStr = sdf.format(attachmentModel.getAttachTs());
        attachmentModel.setAttachTsStr(attachTsStr);
      } catch (Exception ex) {
        attachmentModel.setAttachTsStr("");
      }

      attachmentModel.setState(BaseModel.STATE_EXISTING);
      results.add(attachmentModel);
    }

    return results;
  }

  @Override
  protected Attachment getCurrentRecord(AttachmentModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ATTACHMENT.GETRECORD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setParameter("LINK", model.getDocLink());

    List<Attachment> rs = query.getResults(1, Attachment.class);

    if (rs != null && rs.size() > 0) {
      return rs.get(0);
    }
    return null;
  }

  @Override
  protected Attachment createFromModel(AttachmentModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    Attachment attachment = new Attachment();
    AttachmentPK pk = new AttachmentPK();
    attachment.setId(pk);
    copyValuesToEntity(model, attachment);
    return attachment;
  }

  public void addAttachment(HttpServletRequest request) throws Exception {
    DiskFileItemFactory factory = new DiskFileItemFactory();

    String attachDir = SystemConfiguration.getValue("ATTACHMENT_DIR");
    String tmpDir = attachDir + "/" + "tmp";
    File tmp = new File(tmpDir);
    if (!tmp.exists()) {
      tmp.mkdirs();
    }
    // Set factory constraints
    factory.setSizeThreshold(5000);
    factory.setRepository(tmp);

    // Create a new file upload handler
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> items = upload.parseRequest(request);
    long reqId = 0;
    String docContent = null;
    String cmt = null;
    String token = null;
    String attachMode = null;
    String imgContent = null;
    for (FileItem item : items) {
      if (item.isFormField()) {
        if ("reqId".equals(item.getFieldName())) {
          reqId = Long.parseLong(item.getString());
        }
        if ("docContent".equals(item.getFieldName())) {
          docContent = item.getString();
        }
        if ("attachMode".equals(item.getFieldName())) {
          attachMode = item.getString();
        }
        if ("cmt".equals(item.getFieldName())) {
          cmt = item.getString();
        }
        if ("attachToken".equals(item.getFieldName())) {
          token = item.getString();
        }
        if ("imgContent".equals(item.getFieldName())) {
          imgContent = item.getString();
        }
      }
    }

    log.debug("Attachment: Req ID = " + reqId + ", Content = " + docContent + ", Comment = " + cmt);
    try {
      if ("S".equals(attachMode)) {
        if (!imgContent.contains("base64,")) {
          throw new CmrException(MessageUtil.ERROR_INVALID_SCREENSHOT);
        }
        imgContent = imgContent.substring(imgContent.indexOf("base64,") + 7);

        if (reqId > 0 && !StringUtils.isEmpty(docContent)) {
          File uploadDir = prepareUploadDir(reqId);

          this.log.debug("Rendering image for Request ID " + reqId);

          BufferedImage image = null;
          byte[] imageByte = null;
          imageByte = Base64.getDecoder().decode(imgContent);
          ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
          try {
            image = ImageIO.read(bis);
          } finally {
            bis.close();
          }

          String screenshotName = "Screenshot-" + SS_NAME_FORMATTER.format(new Date()) + ".png";
          String fileName = uploadDir.getAbsolutePath() + "/" + screenshotName;

          // make file separator universal
          fileName = fileName.replaceAll("[\\\\]", "/");

          // create the zip file
          String zipFileName = fileName + ".zip";
          if (!StringUtils.isBlank(zipFileName) && zipFileName.indexOf('\'') > -1) {
            throw new CmrException(MessageUtil.ERROR_FILE_ATTACH_NAME_FORMAT);
          }

          File file = new File(zipFileName);
          if (file.exists()) {
            // file already exists
            throw new CmrException(MessageUtil.ERROR_FILE_ATTACH_NAME);
          }

          FileOutputStream fos = new FileOutputStream(new File(zipFileName));
          try {
            ZipOutputStream zos = new ZipOutputStream(fos);
            try {
              ZipEntry entry = new ZipEntry(screenshotName);
              zos.putNextEntry(entry);

              ImageIO.write(image, "png", zos);

              zos.closeEntry();
            } finally {
              zos.close();
            }
          } finally {
            fos.close();
          }

          // if file has been created, create a record for it
          file = new File(zipFileName);
          if (file.exists()) {
            createAttachment(request, reqId, zipFileName, docContent, cmt);
          }
        }
        if (!StringUtils.isEmpty(token)) {
          request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_ATTACHMENT_ADDED));
        }
      } else {
        if (reqId > 0 && !StringUtils.isEmpty(docContent)) {
          File uploadDir = prepareUploadDir(reqId);
          for (FileItem item : items) {
            if (!item.isFormField()) {
              if ("filecontent".equals(item.getFieldName())) {

                String fileName = uploadDir.getAbsolutePath() + "/" + item.getName();

                // make file separator universal
                fileName = fileName.replaceAll("[\\\\]", "/");

                // create the zip file
                String zipFileName = fileName + ".zip";

                if (!StringUtils.isBlank(zipFileName) && zipFileName.indexOf('\'') > -1) {
                  throw new CmrException(MessageUtil.ERROR_FILE_ATTACH_NAME_FORMAT);
                }

                File file = new File(zipFileName);
                if (file.exists()) {
                  // file already exists
                  throw new CmrException(MessageUtil.ERROR_FILE_ATTACH_NAME);
                }

                int maxSize = Integer.parseInt(SystemConfiguration.getValue("ATTACHMENT_MAX_SIZE"));
                maxSize = maxSize * 1024 * 1024;

                if (item.getSize() > maxSize) {
                  // file size over max
                  throw new CmrException(MessageUtil.ERROR_FILE_ATTACH_SIZE, SystemConfiguration.getValue("ATTACHMENT_MAX_SIZE"));
                }

                FileOutputStream fos = new FileOutputStream(new File(zipFileName));
                try {
                  ZipOutputStream zos = new ZipOutputStream(fos);
                  try {
                    ZipEntry entry = new ZipEntry(item.getName());
                    zos.putNextEntry(entry);

                    // put the file as the single entry
                    byte[] bytes = new byte[1024];
                    int length = 0;
                    InputStream is = item.getInputStream();
                    try {
                      while ((length = is.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                      }
                    } finally {
                      is.close();
                    }

                    zos.closeEntry();
                  } finally {
                    zos.close();
                  }
                } finally {
                  fos.close();
                }

                // if file has been created, create a record for it
                file = new File(zipFileName);
                if (file.exists()) {
                  createAttachment(request, reqId, zipFileName, docContent, cmt);
                }
              }
            }
          }
        }
        if (!StringUtils.isEmpty(token)) {
          request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_ATTACHMENT_ADDED));
        }
      }
    } catch (Exception e) {
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        request.getSession().setAttribute(token, "N," + cmre.getMessage());
      } else {
        request.getSession().setAttribute(token, "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
      }
    }
  }

  /**
   * Creates the attachment record on the database
   * 
   * @param request
   * @param reqId
   * @param fileName
   * @param docContent
   * @param cmt
   * @throws CmrException
   */
  private void createAttachment(HttpServletRequest request, long reqId, String fileName, String docContent, String cmt) throws CmrException {
    AppUser user = AppUser.getUser(request);
    AttachmentModel model = new AttachmentModel();
    model.setReqId(reqId);
    model.setDocLink(fileName);
    model.setAttachTs(SystemUtil.getCurrentTimestamp());
    model.setCmt(cmt);
    model.setDocAttachById(user.getIntranetId());
    model.setDocAttachByNm(user.getBluePagesName());
    model.setDocContent(docContent);
    model.setAction("ADD_ATTACHMENT");
    processTransaction(model, request);
  }

  private File prepareUploadDir(long reqId) {
    String attachDir = SystemConfiguration.getValue("ATTACHMENT_DIR");
    String uploadDirPath = attachDir + "/" + reqId;
    File uploadDir = new File(uploadDirPath);
    if (!uploadDir.exists()) {
      log.debug("Preparing directory " + uploadDirPath);
      uploadDir.mkdir();
    }
    return uploadDir;
  }

  /**
   * Adds an external attachment to the request from the contents of the input
   * stream source
   * 
   * @param entityManager
   * @param reqId
   * @param source
   * @throws CmrException
   * @throws IOException
   */
  public void addExternalAttachment(EntityManager entityManager, AppUser user, long reqId, String docContent, String name, String comments,
      InputStream source) throws CmrException, IOException {
    File uploadDir = prepareUploadDir(reqId);
    String fileName = uploadDir.getAbsolutePath() + "/" + name;

    // make file separator universal
    fileName = fileName.replaceAll("[\\\\]", "/");

    // create the zip file
    String zipFileName = fileName + ".zip";
    if (!StringUtils.isBlank(zipFileName) && zipFileName.indexOf('\'') > -1) {
      throw new CmrException(MessageUtil.ERROR_FILE_ATTACH_NAME_FORMAT);
    }
    File file = new File(zipFileName);
    if (file.exists()) {
      // file already exists
      throw new CmrException(MessageUtil.ERROR_FILE_ATTACH_NAME);
    }

    FileOutputStream fos = new FileOutputStream(new File(zipFileName));
    try {
      ZipOutputStream zos = new ZipOutputStream(fos);
      try {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);

        // put the file as the single entry
        byte[] bytes = new byte[1024];
        int length = 0;
        InputStream is = source;
        try {
          while ((length = is.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
          }
        } finally {
          is.close();
        }

        zos.closeEntry();
      } finally {
        zos.close();
      }
    } finally {
      fos.close();
    }

    // if file has been created, create a record for it
    file = new File(zipFileName);
    if (file.exists()) {
      this.log.debug("Creating attachment of type " + docContent + " filename " + name + " for Request ID " + reqId);
      AttachmentModel model = new AttachmentModel();
      model.setReqId(reqId);
      model.setDocLink(zipFileName);
      model.setAttachTs(SystemUtil.getCurrentTimestamp());
      model.setCmt(comments);
      model.setDocAttachById(user.getIntranetId());
      model.setDocAttachByNm(user.getBluePagesName());
      model.setDocContent(docContent);
      model.setAction("ADD_ATTACHMENT");

      Attachment attachment = createFromModel(model, entityManager, null);
      createEntity(attachment, entityManager);

    }

  }

  /**
   * Removes all current attachments with the given type
   * 
   * @param entityManager
   * @param reqId
   * @param docContent
   * @return
   */
  public void removeAttachmentsOfType(EntityManager entityManager, long reqId, String docContent) {
    String sql = ExternalizedQuery.getSql("ATTACHMENT.GET_BY_TYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("TYPE", docContent);
    List<Attachment> attachments = query.getResults(Attachment.class);
    if (attachments != null) {
      for (Attachment attachment : attachments) {
        String link = attachment.getId().getDocLink();
        this.log.debug("Removing file " + link + " from Request ID " + reqId);
        File file = new File(link);
        if (file.exists()) {
          file.delete();
        }
        deleteEntity(attachment, entityManager);
      }
    }
  }

}
