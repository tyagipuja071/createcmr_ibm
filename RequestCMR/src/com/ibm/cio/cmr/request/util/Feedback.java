/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.PreparedQuery;

/**
 * Utility to create feedback links
 * 
 * @author Jeffrey Zamora
 * 
 */
public class Feedback {

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("feedback");
  private static final Logger LOG = Logger.getLogger(Feedback.class);

  public static String getLink(String cmrIssuingCountry) {
    try {
      if (BUNDLE.containsKey(cmrIssuingCountry)) {
        return BUNDLE.getString(cmrIssuingCountry);
      }
      return null;
    } catch (MissingResourceException e) {
      return null;
    }
  }

  public static String getContact(String cmrIssuingCountry) {
    try {
      if (BUNDLE.containsKey(cmrIssuingCountry + ".contact")) {
        return BUNDLE.getString(cmrIssuingCountry + ".contact");
      }
      return null;
    } catch (MissingResourceException e) {
      return null;
    }
  }

  public static String getLink(Data data) {
    if (SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry()) && "S".equals(data.getSensitiveFlag())) {
      return getLink(SystemLocation.UNITED_STATES + "S");
    }
    return getLink(data.getCmrIssuingCntry());
  }

  public static String generateEmeddedFeedbackLink(Data data) {
    String link = getLink(data);
    if (link == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("<br>\n");
    sb.append("<br>\n");
    sb.append("<div style=\"color:#0000CD; font-weight:bold\">\n");
    sb.append("We'd love to hear from you! Send us your feedback about the process by clicking ");
    sb.append("<a style=\"text-decoration:underline\" href=\"" + link + "\">here</a>.\n");
    sb.append("</div>\n");

    return sb.toString();
  }

  public static String generateEmeddedContactLink(Data data) {
    String contact = getContact(data.getCmrIssuingCntry());
    if (contact == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("<br>\n");
    sb.append("<br>\n");
    sb.append("<div style=\"color:#000033;\">\n");
    sb.append("The request will be processed shortly.  If you have any queries regarding the process, kindly reach out to <strong><a href=\"mailto:"
        + contact + "\">" + contact + "</a></strong>.");
    sb.append("</div>\n");

    return sb.toString();
  }

  /**
   * Gets the New NPS URL link for the request
   * 
   * @param entityManager
   * @param admin
   * @param country
   * @return
   */
  public static String getNPSUrl(EntityManager entityManager, Admin admin, String country) {
    String procCenter = RequestUtils.getProcessingCenter(entityManager, country);
    LOG.debug(" - Processing Center: " + procCenter);

    String feedbackUrl = SystemParameters.getString("NPS.URL");
    if (StringUtils.isBlank(feedbackUrl)) {
      feedbackUrl = "https://nps-survey-prod.dal1a.ciocloud.nonprod.intranet.ibm.com/";
    }
    LOG.debug("Request " + admin.getId().getReqId() + ":");
    String processor = getProcessor(entityManager, admin.getId().getReqId());
    boolean automated = StringUtils.isBlank(processor);

    LOG.debug(" - Requester: " + admin.getRequesterId() + ", Processor: " + processor + ", Automated: " + automated);
    feedbackUrl += "?type=" + (automated ? "automation" : "createcmr");
    feedbackUrl += "&requestID=" + admin.getId().getReqId();
    feedbackUrl += "&tribe=" + (procCenter != null ? procCenter.toLowerCase().replaceAll(" ", "") : "bratislava");

    return feedbackUrl;
  }

  public static String genEmbeddedNPSLink(EntityManager entityManager, Admin admin, String country) {
    String link = getNPSUrl(entityManager, admin, country);
    if (link == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("<br>\n");
    sb.append("<br>\n");
    sb.append("<div style=\"color:#0000CD; font-weight:bold\">\n");
    sb.append("We'd love to hear from you! Send us your feedback about the process by clicking ");
    sb.append("<a style=\"text-decoration:underline\" href=\"" + link + "\">here</a>.\n");
    sb.append("</div>\n");

    return sb.toString();

  }

  /**
   * Checks if the request is automated or not
   * 
   * @param entityManager
   * @param reqId
   * @return
   */
  public static String getProcessor(EntityManager entityManager, long reqId) {
    String sql = "select CREATE_BY_ID from CREQCMR.WF_HIST where REQ_ID = :REQ_ID and REQ_STATUS in ('PVA')";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

}
