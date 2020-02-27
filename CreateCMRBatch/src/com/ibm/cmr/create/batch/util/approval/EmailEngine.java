/**
 * 
 */
package com.ibm.cmr.create.batch.util.approval;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.approval.ApprovalController;
import com.ibm.cio.cmr.request.controller.approval.ApprovalController.ApprovalType;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.ApprovalReq;
import com.ibm.cio.cmr.request.entity.ApprovalTyp;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.DefaultApprovals;
import com.ibm.cio.cmr.request.entity.DefaultApprovalsPK;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.ProlifChecklist;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil.ChecklistResponse;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;
import com.ibm.cmr.create.batch.util.BatchUtil;

/**
 * Email generator based on templates
 * 
 * @author Jeffrey Zamora
 * 
 */
public class EmailEngine {

  private static final Logger LOG = Logger.getLogger(EmailEngine.class);
  private long reqId;
  private EntityManager entityManager;
  private Admin admin;
  private List<Addr> addresses;
  private List<UpdatedDataModel> updates;
  private List<UpdatedNameAddrModel> addrUpdates;
  private Data data;
  private DataRdc dataRdc;
  private VelocityEngine engine;
  private VelocityContext requestContext;
  private String dataFieldsTemplate;

  /**
   * Creates an instance of the {@link EmailEngine}
   * 
   * @param entityManager
   * @param reqId
   * @throws CmrException
   * @throws IOException
   */
  public EmailEngine(EntityManager entityManager, long reqId) throws CmrException, IOException {
    this.reqId = reqId;
    this.entityManager = entityManager;
    initEngine();
  }

  /**
   * Initializes the velocity engine and main entities connected to the request
   * 
   * @throws CmrException
   * @throws IOException
   */
  private void initEngine() throws CmrException, IOException {
    LOG.debug("Initializing Velocity Engine...");
    // init engine
    this.engine = new VelocityEngine();
    this.engine.addProperty(Velocity.RESOURCE_LOADER, "classpath");
    this.engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    this.engine.init();

    LOG.debug("Initializing Request Data...");
    // init the data
    String sql = ExternalizedQuery.getSql("BATCH.EMAILENGINE.GETMETA");
    PreparedQuery query = new PreparedQuery(this.entityManager, sql);
    query.setParameter("REQ_ID", this.reqId);
    query.setForReadOnly(true);

    List<CompoundEntity> entities = query.getCompundResults(1, Admin.class, Admin.BATCH_EMAIL_ENGINE_GET_RECORD);
    if (entities != null && entities.size() > 0) {
      CompoundEntity entity = entities.get(0);
      this.admin = entity.getEntity(Admin.class);
      this.data = entity.getEntity(Data.class);

      sql = ExternalizedQuery.getSql("BATCH.EMAILENGINE.GETMETARDC");
      query = new PreparedQuery(this.entityManager, sql);
      query.setParameter("REQ_ID", this.reqId);
      query.setForReadOnly(true);

      this.dataRdc = query.getSingleResult(DataRdc.class);

      initAddresses();
    }

    String reqType = this.admin.getReqType();
    switch (reqType) {
    case CmrConstants.REQ_TYPE_CREATE:
      String countryCreateTemplate = BatchUtil.getProperty("email.template.create." + this.data.getCmrIssuingCntry());
      if (!StringUtils.isEmpty(countryCreateTemplate)) {
        this.dataFieldsTemplate = countryCreateTemplate;
      } else {
        this.dataFieldsTemplate = BatchUtil.getProperty("email.template.create");
      }
      break;
    case CmrConstants.REQ_TYPE_UPDATE:
      String countryUpdateTemplate = BatchUtil.getProperty("email.template.update." + this.data.getCmrIssuingCntry());
      if (!StringUtils.isEmpty(countryUpdateTemplate)) {
        this.dataFieldsTemplate = countryUpdateTemplate;
      } else {
        this.dataFieldsTemplate = BatchUtil.getProperty("email.template.update");
      }
      initUpdates();
      break;
    case CmrConstants.REQ_TYPE_MASS_CREATE:
    case CmrConstants.REQ_TYPE_MASS_UPDATE:
    case CmrConstants.REQ_TYPE_DELETE:
    case CmrConstants.REQ_TYPE_REACTIVATE:
    case CmrConstants.REQ_TYPE_UPDT_BY_ENT:
      this.dataFieldsTemplate = BatchUtil.getProperty("email.template.blank");
    }

    LOG.debug("Data template: " + this.dataFieldsTemplate);

    initContext();
  }

  /**
   * Retrieves the address records for single-requests
   */
  private void initAddresses() {
    String sql = ExternalizedQuery.getSql("BATCH.EMAILENGINE.GETADDR");
    PreparedQuery query = new PreparedQuery(this.entityManager, sql);
    query.setParameter("REQ_ID", this.reqId);
    query.setForReadOnly(true);
    this.addresses = query.getResults(Addr.class);
  }

  /**
   * Initializes update-related fields
   * 
   * @throws CmrException
   */
  private void initUpdates() throws CmrException {
    RequestSummaryService service = new RequestSummaryService();
    this.updates = new ArrayList<UpdatedDataModel>();
    this.updates.addAll(service.getUpdatedData(this.data, this.reqId, "C"));
    this.updates.addAll(service.getUpdatedData(this.data, this.reqId, "IBM"));
    this.addrUpdates = service.getUpdatedNameAddr(this.reqId);
  }

  private void initEnterpriseUpdate(EntityManager entityManager, long reqId) {
    LOG.debug("Getting Update by Enterprise Information..");
    String sql = ExternalizedQuery.getSql("BATCH.EMAILENGINE.GETUPDTENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    MassUpdtData data = query.getSingleResult(MassUpdtData.class);
    this.requestContext.put("ent", data);
  }

  /**
   * Initializes the velocity context
   * 
   * @throws IOException
   */
  private void initContext() throws IOException {
    LOG.debug("Initializing context..");
    this.requestContext = new VelocityContext();
    this.requestContext.put("data", this.data);
    this.requestContext.put("rdc", this.dataRdc);
    this.requestContext.put("admin", this.admin);
    this.requestContext.put("addresses", this.addresses);

    String applicationURL = SystemConfiguration.getValue("APPLICATION_URL") + "/login";
    this.requestContext.put("applicationURL", applicationURL);

    for (Addr addr : this.addresses) {
      if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(addr.getId().getAddrType())) {
        this.requestContext.put("soldTo", addr);
        break;
      }
    }
    if (CmrConstants.REQ_TYPE_UPDATE.equals(this.admin.getReqType())) {
      this.requestContext.put("updatedFields", this.updates);
      this.requestContext.put("updatedAddrFields", this.addrUpdates);
    }

    // initialize the data template first
    Template dataTemplate = this.engine.getTemplate(this.dataFieldsTemplate);
    StringWriter data = new StringWriter();
    try {
      dataTemplate.merge(this.requestContext, data);
    } finally {
      data.close();
    }
    this.requestContext.put("dataFields", data.toString());

    // initialize the head context
    Template headTemplate = this.engine.getTemplate(BatchUtil.getProperty("email.template.head"));
    StringWriter head = new StringWriter();
    try {
      headTemplate.merge(this.requestContext, head);
    } finally {
      head.close();
    }
    this.requestContext.put("head", head.toString());

    // initialize signature
    Template sigTemplate = this.engine.getTemplate(BatchUtil.getProperty("email.template.signature"));
    StringWriter signature = new StringWriter();
    try {
      sigTemplate.merge(this.requestContext, signature);
    } finally {
      signature.close();
    }
    this.requestContext.put("signature", signature.toString());

    // init total record count for mass change requests
    if (!CmrConstants.REQ_TYPE_CREATE.equals(this.admin.getReqType()) && !CmrConstants.REQ_TYPE_UPDATE.equals(this.admin.getReqType())) {
      LOG.debug("Getting total records for mass change.");
      String sql = null;
      if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(this.admin.getReqType())) {
        sql = ExternalizedQuery.getSql("BATCH.EMAILENGINE.GETTOTAL.UPDT");
      } else {
        sql = ExternalizedQuery.getSql("BATCH.EMAILENGINE.GETTOTAL.UPDT");
      }
      PreparedQuery query = new PreparedQuery(this.entityManager, sql);
      query.setParameter("REQ_ID", this.admin.getId().getReqId());
      query.setParameter("ITERATION_ID", this.admin.getIterationId());
      String records = query.getSingleResult(String.class);
      LOG.debug("Total Records: " + records);
      this.requestContext.put("numberOfRecords", records);

      if (CmrConstants.REQ_TYPE_UPDT_BY_ENT.equals(this.admin.getReqType())) {
        initEnterpriseUpdate(entityManager, admin.getId().getReqId());
      }
    }

  }

  /**
   * Generates an {@link Email} object derived from the supplied template and
   * base templates. The object will not contain the recipient list
   * 
   * @param approvalId
   * @param templateLoc
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws ParseException
   */
  public Email generateMail(EntityManager entityManager, ApprovalReq request, ApprovalTyp approvalType, String subject, String requester,
      String comments) throws IOException, NoSuchAlgorithmException, ParseException {

    if (approvalType != null && approvalType.getTitle().equals(BatchUtil.getProperty("vat.exempt.approval"))) {
      return generateVatExemptMail(request, approvalType, subject, requester, comments);
    }
    VelocityContext approvalContext = new VelocityContext(this.requestContext);

    subject = "Your Approval Needed: " + approvalType.getTitle() + " - CMR Request " + request.getReqId();
    String mailContent = approvalType.getDescription();
    if (request.getDefaultApprovalId() > 0) {
      DefaultApprovals approvals = getDefaultApproval(entityManager, request.getDefaultApprovalId());
      if (approvals != null && !StringUtils.isBlank(approvals.getApprovalMailContent())) {
        String[] parts = approvals.getApprovalMailContent().split("[|]");
        subject = "Your Approval Needed: " + parts[0] + " - CMR Request " + request.getReqId();
        mailContent = parts[1];
      }
    }
    approvalContext.put("requester", requester);
    approvalContext.put("description", mailContent);
    approvalContext.put("request", request);
    approvalContext.put("comments", comments);

    long approvalId = request.getId().getApprovalId();
    String approver = request.getIntranetId();
    String approvalCode = ApprovalController.encodeUrlParam(approvalId, ApprovalType.A, approver);
    String rejectionCode = ApprovalController.encodeUrlParam(approvalId, ApprovalType.R, approver);
    String condApprovalCode = ApprovalController.encodeUrlParam(approvalId, ApprovalType.C, approver);
    String appUrl = SystemConfiguration.getValue("APPLICATION_URL");
    approvalContext.put("approvalHref", appUrl + "/approval/" + approvalCode);
    approvalContext.put("rejectionHref", appUrl + "/approval/" + rejectionCode);
    approvalContext.put("conditionalHref", appUrl + "/approval/" + condApprovalCode);

    // initialize the links
    Template linksTemplate = this.engine.getTemplate(BatchUtil.getProperty("email.template.links"));
    StringWriter links = new StringWriter();
    try {
      linksTemplate.merge(approvalContext, links);
    } finally {
      links.close();
    }
    approvalContext.put("approvalLinks", links.toString());

    // initialize the specific template
    Template template = this.engine.getTemplate(approvalType.getTemplateName());
    StringWriter generated = new StringWriter();
    try {
      template.merge(approvalContext, generated);
    } finally {
      generated.close();
    }

    // subject = fillSubject(subject, approvalContext);

    String content = generated.toString();

    LOG.debug("Creating the mail object...");
    Email mail = new Email();
    mail.setFrom(SystemConfiguration.getValue("MAIL_FROM"));
    mail.setSubject(subject);
    mail.setMessage(content);
    mail.setType(MessageType.HTML);
    return mail;
  }

  private Email generateVatExemptMail(ApprovalReq request, ApprovalTyp approvalType, String subject, String requester, String comments)
      throws IOException, NoSuchAlgorithmException, ParseException {

    VelocityContext approvalContext = new VelocityContext(this.requestContext);

    approvalContext.put("requester", requester);
    approvalContext.put("description", approvalType.getDescription());
    approvalContext.put("request", request);
    approvalContext.put("comments", comments);

    long approvalId = request.getId().getApprovalId();
    String approver = request.getIntranetId();
    String approvalCode = ApprovalController.encodeUrlParam(approvalId, ApprovalType.A, approver);
    String rejectionCode = ApprovalController.encodeUrlParam(approvalId, ApprovalType.R, approver);
    // String condApprovalCode = ApprovalController.encodeUrlParam(approvalId,
    // ApprovalType.C, approver);
    String appUrl = SystemConfiguration.getValue("APPLICATION_URL");
    approvalContext.put("approvalHref", appUrl + "/approval/" + approvalCode);
    approvalContext.put("rejectionHref", appUrl + "/approval/" + rejectionCode);
    // approvalContext.put("conditionalHref", appUrl + "/approval/" +
    // condApprovalCode);

    // initialize the links
    Template linksTemplate = this.engine.getTemplate(BatchUtil.getProperty("email.template.vatexemptlinks"));
    StringWriter links = new StringWriter();
    try {
      linksTemplate.merge(approvalContext, links);
    } finally {
      links.close();
    }
    approvalContext.put("approvalLinks", links.toString());

    // initialize the specific template
    Template template = this.engine.getTemplate(approvalType.getTemplateName());
    StringWriter generated = new StringWriter();
    try {
      template.merge(approvalContext, generated);
    } finally {
      generated.close();
    }

    subject = fillSubject(subject, approvalContext);

    String content = generated.toString();

    LOG.debug("Creating the mail object...");
    Email mail = new Email();
    mail.setFrom(SystemConfiguration.getValue("MAIL_FROM"));
    mail.setSubject(subject);
    mail.setMessage(content);
    mail.setType(MessageType.HTML);
    return mail;
  }

  /**
   * Generates an {@link Email} object derived from the supplied template and
   * base templates. The object will not contain the recipient list
   * 
   * @param approvalId
   * @param templateLoc
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws ParseException
   */
  public Email generateCancellationMail(EntityManager entityManager, ApprovalReq request, ApprovalTyp approvalType, String subject, String requester,
      String comments) throws IOException, NoSuchAlgorithmException, ParseException {

    VelocityContext approvalContext = new VelocityContext(this.requestContext);

    subject = "Approval Request Cancellation: " + approvalType.getTitle() + " - CMR Request " + request.getReqId();
    if (request.getDefaultApprovalId() > 0) {
      DefaultApprovals approvals = getDefaultApproval(entityManager, request.getDefaultApprovalId());
      if (approvals != null && !StringUtils.isBlank(approvals.getApprovalMailContent())) {
        String[] parts = approvals.getApprovalMailContent().split("[|]");
        subject = "Approval Request Cancellation: " + parts[0] + " - CMR Request " + request.getReqId();
      }
    }

    approvalContext.put("requester", requester);
    approvalContext.put("request", request);
    approvalContext.put("comments", comments);

    // initialize the cancellation template
    Template template = this.engine.getTemplate(BatchUtil.getProperty("email.template.cancel"));
    StringWriter generated = new StringWriter();
    try {
      template.merge(approvalContext, generated);
    } finally {
      generated.close();
    }

    // subject = fillSubject(subject, approvalContext);

    String content = generated.toString();

    LOG.debug("Creating the cancellation mail object...");
    Email mail = new Email();
    mail.setFrom(SystemConfiguration.getValue("MAIL_FROM"));
    mail.setSubject(subject);
    mail.setMessage(content);
    mail.setType(MessageType.HTML);
    return mail;
  }

  /**
   * Parses the subject as a {@link Template} object and fills the information
   * from the context
   * 
   * @param subject
   * @return
   * @throws ParseException
   * @throws IOException
   */
  private String fillSubject(String subject, VelocityContext approvalContext) throws ParseException, IOException {
    RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
    StringReader reader = new StringReader(subject);
    try {
      SimpleNode node = runtimeServices.parse(reader, "SubjTempl");
      Template template = new Template();
      template.setRuntimeServices(runtimeServices);
      template.setData(node);
      template.initDocument();

      StringWriter finalSubject = new StringWriter();
      try {
        template.merge(approvalContext, finalSubject);
      } finally {
        finalSubject.close();
      }
      return finalSubject.toString();
    } finally {
      reader.close();
    }
  }

  public void initChecklist() throws IllegalArgumentException, IllegalAccessException {
    LOG.debug("Initializing checklist on the context...");
    String sql = ExternalizedQuery.getSql("APPROVAL.GET_CHECKLIST");
    PreparedQuery query = new PreparedQuery(this.entityManager, sql);
    query.setParameter("REQ_ID", this.reqId);
    query.setForReadOnly(true);
    ProlifChecklist checklist = query.getSingleResult(ProlifChecklist.class);
    if (checklist == null) {
      this.requestContext.put("checklistitems", new ArrayList<>());
    } else {
      this.requestContext.put("checklistitems", ChecklistUtil.getItems(checklist, this.data.getCmrIssuingCntry(), ChecklistResponse.Yes));
    }

  }

  private DefaultApprovals getDefaultApproval(EntityManager entityManager, long defaultApprovalId) {
    DefaultApprovalsPK pk = new DefaultApprovalsPK();
    pk.setDefaultApprovalId(defaultApprovalId);
    DefaultApprovals approval = entityManager.find(DefaultApprovals.class, pk);
    if (approval != null) {
      entityManager.detach(approval);
      return approval;
    }
    return null;
  }
}
