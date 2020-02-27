/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.DefaultApprovalRecipients;
import com.ibm.cio.cmr.request.entity.DefaultApprovalRecipientsPK;
import com.ibm.cio.cmr.request.model.code.DefaultApprovalRecipentsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class DefaultApprovalRecipientService extends BaseService<DefaultApprovalRecipentsModel, DefaultApprovalRecipients> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(DefaultApprovalRecipientService.class);
  }

  @Override
  protected void performTransaction(DefaultApprovalRecipentsModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    if ("ADD_RECIPIENT".equals(model.getAction())) {
      DefaultApprovalRecipients recipient = createFromModel(model, entityManager, request);
      this.log.debug("Adding recipient " + model.getIntranetId());
      createEntity(recipient, entityManager);
    } else if ("REMOVE_RECIPIENT".equals(model.getAction())) {
      DefaultApprovalRecipients current = getCurrentRecord(model, entityManager, request);
      this.log.debug("Removing recipient " + model.getIntranetId());
      deleteEntity(current, entityManager);
    }
  }

  @Override
  protected List<DefaultApprovalRecipentsModel> doSearch(DefaultApprovalRecipentsModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    return null;
  }

  @Override
  protected DefaultApprovalRecipients getCurrentRecord(DefaultApprovalRecipentsModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.DEFAULT_APPROVALS.GET_RECIPIENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("DEFAULT_APPROVAL_ID", model.getDefaultApprovalId());
    query.setParameter("INTRANET_ID", model.getIntranetId());
    return query.getSingleResult(DefaultApprovalRecipients.class);
  }

  @Override
  protected DefaultApprovalRecipients createFromModel(DefaultApprovalRecipentsModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    DefaultApprovalRecipientsPK pk = new DefaultApprovalRecipientsPK();
    DefaultApprovalRecipients recipient = new DefaultApprovalRecipients();
    recipient.setId(pk);
    copyValuesToEntity(model, recipient);
    return recipient;
  }

  @Override
  protected void doBeforeInsert(DefaultApprovalRecipients entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("SYSTEM.DEFAULT_APPROVALS.GET_RECIPIENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("DEFAULT_APPROVAL_ID", entity.getId().getDefaultApprovalId());
    query.setParameter("INTRANET_ID", entity.getId().getIntranetId());
    if (query.exists()) {
      throw new CmrException(new Exception("User is already part of the recipients."));
    }
  }

}
