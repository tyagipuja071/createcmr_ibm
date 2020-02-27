/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.cio.cmr.request.entity.Data;

/**
 * Utility to create feedback links
 * 
 * @author Jeffrey Zamora
 * 
 */
public class Feedback {

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("feedback");

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

}
