/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.NotifList;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;

/**
 * Service that sends the Client Satisfaction Survey mails to completed requests
 * 
 * @author JeffZAMORA
 * 
 */
public class NPSService extends BaseBatchService {

  private static final Logger LOG = Logger.getLogger(NPSService.class);
  private static final ResourceBundle FEEDBACK_BUNDLE = ResourceBundle.getBundle("feedback");

  private Map<String, String> requestTypeDescriptions = new HashMap<String, String>();

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {

    String sql = ExternalizedQuery.getSql("BATCH.NPS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Admin> pending = query.getResults(Admin.class);
    if (pending != null) {
      LOG.info(pending.size() + " requests pending for NPS Survey.");

      buildRequestTypeDescriptions(entityManager);

      String template = getEmailTemplate();

      Data data = null;
      DataPK dataPK = null;
      String feedbackUrl = null;

      String market = null;
      String tribe = null;
      String squad = null;
      String area = null;

      String urlElem = null;
      String[] urlParts = null;

      for (Admin admin : pending) {

        // check first if NPS needs to be sent
        if (shouldSendNPS(entityManager, admin)) {

          // get DATA for country specific survey
          dataPK = new DataPK();
          dataPK.setReqId(admin.getId().getReqId());
          data = entityManager.find(Data.class, dataPK);
          if (data != null) {

            // get the feedback URL
            feedbackUrl = FEEDBACK_BUNDLE.getString(data.getCmrIssuingCntry());

            if (StringUtils.isBlank(feedbackUrl)) {
              LOG.warn("Request " + admin.getId().getReqId() + " does not have a feedback URL. Skipping.");
              admin.setWaitRevInd("Y");
            } else {

              feedbackUrl = StringUtils.replace(feedbackUrl, "%20", " ");
              feedbackUrl = StringUtils.replace(feedbackUrl, "%26", "&");

              // if (feedbackUrl.contains("/survey/")) {

              // build the mail
              urlElem = feedbackUrl.substring(feedbackUrl.indexOf("/survey/") + 8);

              urlParts = urlElem.split("[/]");
              if (urlParts != null && urlParts.length == 4) {
                market = urlParts[0];
                tribe = urlParts[1];
                squad = urlParts[2];
                area = urlParts[3];
              }
              LOG.debug("Sending client statisfaction survey to Request " + admin.getId().getReqId());

              String npsMail = new String(template);
              npsMail = StringUtils.replace(npsMail, "{{REQUEST}}",
                  admin.getId().getReqId() + " (" + this.requestTypeDescriptions.get(admin.getReqType()) + ")");
              npsMail = StringUtils.replace(npsMail, "{{TRIBE}}", tribe);
              npsMail = StringUtils.replace(npsMail, "{{CMR}}", data.getCmrNo() + " (" + admin.getMainCustNm1()
                  + (!StringUtils.isBlank(admin.getMainCustNm2()) ? " " + admin.getMainCustNm2() : "") + ")");
              npsMail = StringUtils.replace(npsMail, "{{SQUAD}}", squad);
              npsMail = StringUtils.replace(npsMail, "{{MARKET}}", market);
              npsMail = StringUtils.replace(npsMail, "{{AREA}}", area);
              npsMail = StringUtils.replace(npsMail, "{{NPS_URL}}", SystemConfiguration.getValue("APPLICATION_URL") + "/nps");
              npsMail = StringUtils.replace(npsMail, "{{REQUEST_URL}}",
                  SystemConfiguration.getValue("APPLICATION_URL") + "/request/" + admin.getId().getReqId());
              npsMail = StringUtils.replace(npsMail, "{{FEEDBACK_URL}}", FEEDBACK_BUNDLE.getString(data.getCmrIssuingCntry()));

              Email mail = new Email();
              mail.setFrom(SystemConfiguration.getValue("MAIL_FROM"));
              mail.setTo(admin.getRequesterId());
              mail.setSubject("Request " + admin.getId().getReqId() + " - Client Satisfaction Survey");
              mail.setMessage(npsMail.toString());
              mail.setType(MessageType.HTML);

              LOG.info("Sending NPS survey to " + admin.getRequesterId() + " for Request " + admin.getId().getReqId());
              mail.send(SystemConfiguration.getValue("MAIL_HOST"));

              admin.setWaitRevInd("Y");
              // } else {
              // LOG.warn("Request " + admin.getId().getReqId() + " has
              // unparseable feedback URL. Skipping.");
              // admin.setWaitRevInd("Y");
              // }

            }
          } else {
            LOG.warn("Request " + admin.getId().getReqId() + " does not have a DATA entity. Skipping.");
            admin.setWaitRevInd("Y");
          }

        } else {
          LOG.warn("Request " + admin.getId().getReqId() + " does not need an NPS notif. Skipping.");
          admin.setWaitRevInd("Y");
        }
        entityManager.merge(admin);
        partialCommit(entityManager);

        entityManager.detach(admin);
      }
    }
    return Boolean.TRUE;
  }

  /**
   * Checks if the survey needs to be sent to the requester. <br>
   * Criteria:<br>
   * <ul>
   * <li>If the requester = processor, no need to send</li>
   * <li>If the requester is not an IBMer, no need to send</li>
   * </ul>
   * 
   * @param entityManager
   * @param admin
   * @return
   */
  private boolean shouldSendNPS(EntityManager entityManager, Admin admin) {
    String requesterId = admin.getRequesterId().toLowerCase();

    if (!requesterId.endsWith("ibm.com")) {
      return false;
    }
    String sql = ExternalizedQuery.getSql("BATCH.NPS.NOTIF");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<NotifList> notifList = query.getResults(NotifList.class);
    if (notifList != null) {
      if (notifList.size() < 2) {
        return false;
      }
    }

    return true;

  }

  /**
   * Reads nps.html from the classpath and returns the email template
   * 
   * @return
   * @throws IOException
   */
  private String getEmailTemplate() throws IOException {
    LOG.debug("Generating the NPS email template...");
    try (InputStream is = NPSService.class.getClassLoader().getResourceAsStream("nps.html")) {
      try (InputStreamReader isr = new InputStreamReader(is)) {
        try (BufferedReader br = new BufferedReader(isr)) {
          String line = null;
          StringBuilder template = new StringBuilder();
          while ((line = br.readLine()) != null) {
            template.append(line).append("\n");
          }
          return template.toString();
        }
      }
    }
  }

  /**
   * Builds the map of request type to description mapping
   * 
   * @param entityManager
   */
  private void buildRequestTypeDescriptions(EntityManager entityManager) {
    LOG.debug("Building Request Type-Description mapping..");
    String sql = ExternalizedQuery.getSql("BATCH.NPS.REQTYPES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Object[]> reqTypes = query.getResults();
    for (Object[] reqType : reqTypes) {
      this.requestTypeDescriptions.put((String) reqType[0], (String) reqType[1]);
    }
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

}
