/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.ApprovalComments;
import com.ibm.cio.cmr.request.entity.ApprovalReq;
import com.ibm.cio.cmr.request.entity.ApprovalTyp;
import com.ibm.cio.cmr.request.entity.Attachment;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Lov;
import com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.approval.ApprovalService;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.impl.JPHandler;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.pdf.RequestToPDFConverter;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.approval.EmailEngine;

/**
 * Batch program to process approvals
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ApprovalBatchService extends BaseBatchService {

  private Map<Long, EmailEngine> requestEngines = new HashMap<Long, EmailEngine>();

  private Map<String, String> docContentDesc = new HashMap<String, String>();

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    LOG.info("Retrieving pending approval requests..");

    initDocContent(entityManager);
    ApprovalTyp approvalType = null;
    ApprovalComments comments = null;
    Admin admin = null;
    ApprovalResponseModel model = null;
    String mailSubject = null;
    EmailEngine engine = null;
    Email mail = null;
    String userComments = null;

    // retrieve the pending approvals
    ApprovalService service = new ApprovalService();
    String sql = ExternalizedQuery.getSql("BATCH.APPROVAL.GETRECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<ApprovalReq> records = query.getResults(ApprovalReq.class);

    for (ApprovalReq request : records) {

      try {
        Thread.currentThread().setName("APPROVAL-" + request.getReqId() + "-" + request.getId().getApprovalId());

        LOG.debug("Processing Approval Request ID " + request.getId().getApprovalId());

        // get type and correct comment
        approvalType = getApprovalType(entityManager, request);
        if (approvalType == null) {
          LOG.warn("Invalid Approval Type specified: " + request.getTypId() + "/" + request.getGeoCd());
          continue;
        }
        comments = getApprovalComment(entityManager, request);

        model = new ApprovalResponseModel();
        model.setApprovalId(request.getId().getApprovalId());
        model.setApproverId(request.getIntranetId());
        admin = service.fillApprovalInformation(request, model, entityManager);
        entityManager.detach(admin);

        switch (request.getStatus()) {
        case CmrConstants.APPROVAL_PENDING_MAIL:
          if (!CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) && !CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
            // change subject for mass change
            if (CmrConstants.REQ_TYPE_DELETE.equals(admin.getReqType()) || CmrConstants.REQ_TYPE_REACTIVATE.equals(admin.getReqType())) {
              mailSubject = BatchUtil.getProperty("email.approval.delete.subject");
            } else {
              mailSubject = BatchUtil.getProperty("email.approval.masschange.subject");
            }
          } else {
            mailSubject = BatchUtil.getProperty("email.approval.subject");
          }
          request.setStatus(CmrConstants.APPROVAL_PENDING_APPROVAL);
          userComments = comments != null ? comments.getComments() : "";
          break;
        case CmrConstants.APPROVAL_PENDING_REMINDER:
          mailSubject = "Reminder: ";
          if (!CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) && !CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
            // change subject for mass change
            if (CmrConstants.REQ_TYPE_DELETE.equals(admin.getReqType()) || CmrConstants.REQ_TYPE_REACTIVATE.equals(admin.getReqType())) {
              mailSubject += BatchUtil.getProperty("email.approval.delete.subject");
            } else {
              mailSubject += BatchUtil.getProperty("email.approval.masschange.subject");
            }
          } else {
            mailSubject += BatchUtil.getProperty("email.approval.subject");
          }
          request.setStatus(CmrConstants.APPROVAL_PENDING_APPROVAL);
          userComments = comments != null ? comments.getComments() : "";
          break;
        case CmrConstants.APPROVAL_OVERRIDE_PENDING:
          request.setStatus(CmrConstants.APPROVAL_OVERRIDE_APPROVED);
          userComments = "";
          break;
        case CmrConstants.APPROVAL_PENDING_CANCELLATION:
          if (CmrConstants.SINGLE_REQUESTS_TYPES.contains(admin.getReqType())) {
            mailSubject = BatchUtil.getProperty("email.cancellation.subject");
          } else {
            mailSubject = BatchUtil.getProperty("email.cancellation.subject.mass");
          }
          request.setStatus(CmrConstants.APPROVAL_CANCELLED);
          userComments = comments != null ? comments.getComments() : "";
          break;
        }

        service.getLogicalStatus(request, model, entityManager);
        LOG.debug("Setting status to " + request.getStatus());
        request.setLastUpdtBy(BATCH_USER_ID);
        request.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        updateEntity(request, entityManager);

        model.setComments("(system generated status change)");

        String sourceSysSkip = admin.getSourceSystId() + ".SKIP";
        String onlySkipPartner = SystemParameters.getString(sourceSysSkip);
        boolean skip = false;

        if (StringUtils.isNotBlank(admin.getSourceSystId()) && "Y".equals(onlySkipPartner)) {
          skip = true;
        }

        if (skip == false) {
          service.sendGenericMail(entityManager, approvalType, model, admin);
        }

        if (CmrConstants.APPROVAL_PENDING_APPROVAL.equals(request.getStatus()) || CmrConstants.APPROVAL_CANCELLED.equals(request.getStatus())) {
          // need to prepare engine
          engine = this.requestEngines.get(request.getReqId());
          if (engine == null) {
            LOG.debug("Initializing email engine for Request " + request.getReqId());
            engine = new EmailEngine(entityManager, request.getReqId());
            this.requestEngines.put(request.getReqId(), engine);
          } else {
            LOG.debug("Using email engine for Request " + request.getReqId());
          }

          if (isEROApproval(approvalType)) {
            engine.initChecklist();
          }
          if (CmrConstants.APPROVAL_PENDING_APPROVAL.equals(request.getStatus())) {
            LOG.debug("Sending approval email for Approval Request ID " + request.getId().getApprovalId());
            mail = engine.generateMail(entityManager, request, approvalType, mailSubject, model.getRequester(), userComments);
          } else if (CmrConstants.APPROVAL_CANCELLED.equals(request.getStatus())) {
            LOG.debug("Sending cancellation notification for Approval Request ID " + request.getId().getApprovalId());
            mail = engine.generateCancellationMail(entityManager, request, approvalType, mailSubject, model.getRequester(), userComments);
          }
          // add the PDF attachment for the request details
          if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) || CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())
              || CmrConstants.REQ_TYPE_DELETE.equals(admin.getReqType()) || CmrConstants.REQ_TYPE_REACTIVATE.equals(admin.getReqType())) {
            if (!isVatExemptApproval(approvalType)) {
              ByteArrayOutputStream bos = new ByteArrayOutputStream();
              RequestToPDFConverter.exportToPdf(entityManager, admin.getId().getReqId(), bos);
              mail.addAttachment("RequestDetails_" + admin.getId().getReqId() + ".pdf", bos);
            }
          } else if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType()) || CmrConstants.REQ_TYPE_MASS_UPDATE.equals(admin.getReqType())) {
            if (CmrConstants.REQ_TYPE_MASS_UPDATE.equals(admin.getReqType())) {
              mail.addAttachment(admin.getFileName() + ".zip");
            } else {
              mail.addAttachment(admin.getFileName());
            }
          }

          /* 1625573: Approvers should receive attachment from the request */
          // add ALL attachments to approvals
          addAttachments(entityManager, mail, request, approvalType);

          mail.setTo(request.getIntranetId());

          // CREATCMR-8813 Add JP Copy Member
          String cmrIssuingCntry = getCmrIssuingCntry(entityManager, request.getReqId());
          if (SystemLocation.JAPAN.equals(cmrIssuingCntry)) {
            JPHandler jpHanler = (JPHandler) RequestUtils.getGEOHandler(cmrIssuingCntry);
            boolean copyFlag = jpHanler.needCopy(entityManager, request);
            if (copyFlag) {
              mail.setCc("ychongg@cn.ibm.com");
              LOG.debug("Copy email to SME:" + mail.getCc() + "as the notification for Req Id:" + request.getReqId());
            }
          }

          mail.send(SystemConfiguration.getValue("MAIL_HOST"));
        }

        LOG.debug("Partially committing for Approval Request " + request.getId().getApprovalId());
        partialCommit(entityManager);
      } catch (Exception e) {
        LOG.debug("Error when processing Approval " + request.getId().getApprovalId(), e);
        LOG.debug("Partially rolling back..");
        partialRollback(entityManager);
      }
    }
    Thread.currentThread().setName("ApprovalService-" + Thread.currentThread().getId());
    return Boolean.TRUE;
  }

  /**
   * Gets the approval type record for the request
   * 
   * @param entityManager
   * @param request
   * @return
   */
  private ApprovalTyp getApprovalType(EntityManager entityManager, ApprovalReq request) {
    String sql = ExternalizedQuery.getSql("APPROVAL.GETTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("TYP_ID", request.getTypId());
    query.setParameter("GEO_CD", request.getGeoCd());
    query.setForReadOnly(true);
    return query.getSingleResult(ApprovalTyp.class);
  }

  /**
   * Gets the approval type record for the request
   * 
   * @param entityManager
   * @param request
   * @return
   */
  private ApprovalComments getApprovalComment(EntityManager entityManager, ApprovalReq request) {
    String sql = ExternalizedQuery.getSql("BATCH.APPROVAL.GETCMT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CREATE_BY", request.getCreateBy().toUpperCase());
    query.setParameter("APPROVAL_ID", request.getId().getApprovalId());
    String status = null;
    switch (request.getStatus()) {
    case CmrConstants.APPROVAL_PENDING_CANCELLATION:
      status = CmrConstants.APPROVAL_PENDING_CANCELLATION;
      break;
    default:
      // only pending cancellation has specific status matching
      status = "x";
    }
    query.setParameter("STATUS", status);
    query.setForReadOnly(true);
    return query.getSingleResult(ApprovalComments.class);
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  /**
   * Checks if the request is type = DPL
   * 
   * @deprecated - no need to check since all attachments will be added
   * @param approvalType
   * @return
   */
  @SuppressWarnings("unused")
  @Deprecated
  private boolean isDplApproval(ApprovalTyp approvalType) {
    if (approvalType != null && approvalType.getTemplateName().trim().toLowerCase().endsWith("dpl.html")) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the request is type = DPL
   * 
   * @param approvalType
   * @return
   */
  private boolean isVatExemptApproval(ApprovalTyp approvalType) {
    if (approvalType != null && approvalType.getTemplateName().trim().toLowerCase().endsWith("vatexempt.html")) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the request is type = DPL
   * 
   * @param approvalType
   * @return
   */
  private boolean isEROApproval(ApprovalTyp approvalType) {
    if (approvalType != null && approvalType.getTemplateName().trim().toLowerCase().endsWith("ero.html")) {
      return true;
    }
    return false;
  }

  /**
   * 
   * @deprecated - use
   *             {@link #addAttachments(EntityManager, Email, ApprovalReq, ApprovalTyp)}
   *             instead
   * @param entityManager
   * @param email
   * @param approval
   * @param approvalType
   * @param docType
   * @param name
   * @throws Exception
   */
  @SuppressWarnings("unused")
  @Deprecated
  private void addDplAttachments(EntityManager entityManager, Email email, ApprovalReq approval, ApprovalTyp approvalType, String docType,
      String name) throws Exception {
    String sql = ExternalizedQuery.getSql("APPROVAL.GET_DPL_ATTACHMENTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", approval.getReqId());
    query.setParameter("DOC_TYP", docType);
    query.setForReadOnly(true);
    List<Attachment> attachments = query.getResults(Attachment.class);
    if (attachments != null && !attachments.isEmpty()) {
      int counter = 1;
      for (Attachment attachment : attachments) {
        attachToMail(attachment, email, name + counter + "_" + approval.getReqId());
        counter++;
      }

    }
  }

  /**
   * Adds all attachments to the approval mail
   * 
   * @param entityManager
   * @param email
   * @param approval
   * @param approvalType
   * @throws Exception
   */
  private void addAttachments(EntityManager entityManager, Email email, ApprovalReq approval, ApprovalTyp approvalType) throws Exception {
    String sql = ExternalizedQuery.getSql("APPROVAL.GET_ALL_ATTACHMENTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", approval.getReqId());
    query.setForReadOnly(true);
    List<Attachment> attachments = query.getResults(Attachment.class);

    if (attachments != null && !attachments.isEmpty()) {
      int counter = 1;
      String currType = null;
      String fileName = null;
      File file = null;
      for (Attachment attachment : attachments) {
        fileName = attachment.getId().getDocLink();
        file = new File(fileName);
        if (!file.exists()) {
          continue;
        }
        if (!attachment.getDocContent().equals(currType)) {
          counter = 1;
        }
        fileName = this.docContentDesc.get(attachment.getDocContent());
        if (fileName == null || "OTH".equals(attachment.getDocContent())) {
          fileName = file.getName();
          // remove .zip
          if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
          }
          // remove actual extension
          if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
          }

          fileName = fileName.replaceAll("[^ 0-9A-Za-z.-_]", "");
          fileName = fileName.replaceAll("[ ]", "_");
          if (fileName.length() > 30) {
            fileName = fileName.substring(0, 27) + "xx";
          }
          fileName = fileName + "_";
          attachToMail(attachment, email, fileName);
        } else {
          fileName = fileName.replaceAll("[ ]", "");
          fileName = fileName.replaceAll("[/]", "_");
          fileName = fileName + "_";
          attachToMail(attachment, email, fileName + counter + "_" + approval.getReqId());
          counter++;
        }
        currType = attachment.getDocContent();
      }

    }
  }

  /**
   * Initializes Doc Contents
   * 
   * @param entityManager
   */
  private void initDocContent(EntityManager entityManager) {
    LOG.debug("Initializing Doc Content...");
    String sql = ExternalizedQuery.getSql("APPROVAL.INIT.DOCCONTENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    List<Lov> lovs = query.getResults(Lov.class);
    if (lovs != null && !lovs.isEmpty()) {
      for (Lov lov : lovs) {
        this.docContentDesc.put(lov.getId().getCd(), lov.getTxt());
      }
    }
  }

  private void attachToMail(Attachment attachment, Email email, String attachmentFileName) throws Exception {
    String fileName = attachment.getId().getDocLink();

    File file = new File(fileName);
    if (!file.exists()) {
      return;
    }
    LOG.debug("Attaching " + file.getName() + " to the approval mail " + (attachmentFileName != null ? "as " + attachmentFileName : "") + ".");
    ZipFile zip = new ZipFile(file);
    try {
      Enumeration<?> entry = zip.entries();
      if (entry.hasMoreElements()) {
        ZipEntry document = (ZipEntry) entry.nextElement();
        InputStream is = zip.getInputStream(document);
        try {

          String ext = document.getName();
          if (ext.contains(".")) {
            ext = ext.substring(ext.lastIndexOf("."));
          }
          String nameToUse = attachmentFileName != null ? attachmentFileName + ext : document.getName();
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          try {
            IOUtils.copy(is, bos);
            email.addAttachment(nameToUse, bos);
          } finally {
            bos.close();
          }
        } finally {
          is.close();
        }
      }
    } finally {
      zip.close();
    }
  }

  private String getCmrIssuingCntry(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("DATA.GET.RECORD.BYID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Data> rs = query.getResults(1, Data.class);

    if (rs != null && rs.size() > 0) {
      return rs.get(0).getCmrIssuingCntry();
    }
    return null;
  }

}
