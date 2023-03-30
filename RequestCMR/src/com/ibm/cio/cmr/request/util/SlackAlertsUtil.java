/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * Handles posting of alerts to the designated slack channels.
 * 
 * @author 136786PH1
 *
 */
public class SlackAlertsUtil {

  private static final Logger LOG = Logger.getLogger(SlackAlertsUtil.class);

  public static void main(String[] args) throws IOException {
    ParamContainer params = new ParamContainer();
    params.addParam("cmr", new ImportCMRModel());
    params.addParam("admin", new Admin());
    System.out.println(new ObjectMapper().writeValueAsString(params));
  }

  /**
   * Posts an exception alert to slack
   * 
   * @param application
   * @param identifier
   * @param e
   * @throws IOException
   */
  public static void recordGenericAlert(String application, String identifier, String... message) {
    JSONObject jsonMsg = createAlertMessage(application, identifier, message);
    postToSlack(jsonMsg);
  }

  /**
   * 
   * @param application
   * @param identifier
   * @param e
   * @param params
   */
  public static void recordException(String application, String identifier, Throwable e, Object params) {
    if (e == null) {
      return;
    }
    Throwable t = e;
    boolean main = true;
    List<String> sections = new ArrayList<String>();
    while (t != null) {
      StringBuilder msgBody = new StringBuilder();
      msgBody.append((main ? "" : "Caused by: ") + t.getClass().getName() + ": " + t.getMessage()).append("\n");
      int count = 0;
      for (StackTraceElement trace : t.getStackTrace()) {
        if (count > 2) {
          break;
        }
        msgBody.append("  at " + trace.toString()).append("\n");
        count++;
      }
      t = main ? t.getCause() : null;
      main = false;
      if (msgBody.length() > 2900) {
        msgBody.delete(2900, msgBody.length());
      }
      sections.add(code(msgBody.toString()));
    }
    if (params != null) {
      try {
        String oParams = null;
        if (params instanceof JSONObject) {
          oParams = ((JSONObject) params).toString();
        } else {
          oParams = new ObjectMapper().writeValueAsString(params);
        }
        try {
          JSONObject paramsJson = JSONObject.parse(oParams);
          List<Object> toRemove = new ArrayList<Object>();
          for (Object okey : paramsJson.keySet()) {
            String key = okey.toString().toLowerCase();
            if (key.contains("pass") || key.contains("pwd") || key.contains("secret")) {
              toRemove.add(okey);
            }
          }
          for (Object o : toRemove) {
            paramsJson.remove(o);
          }
          oParams = paramsJson.toString();
        } catch (Exception ex) {
          // noop, ignore convert to json
        }
        if (oParams.length() > 2900) {
          oParams = oParams.substring(0, 2900);
        }
        sections.add(code("Params: " + oParams));
      } catch (Exception e1) {
        LOG.warn("Cannot convert params to JSON: " + e1.getMessage());
      }
    }
    JSONObject jsonMsg = createAlertMessage(application, identifier, sections.toArray(new String[0]));
    postToSlack(jsonMsg);
  }

  /**
   * Posts an exception alert to slack
   * 
   * @param application
   * @param identifier
   * @param e
   * @throws IOException
   */
  public static void recordException(String application, String identifier, Throwable e) {
    recordException(application, identifier, e, null);
  }

  /**
   * Formats a bold message
   * 
   * @param message
   * @return
   */
  public static String bold(String message) {
    return "*" + message.replaceAll("[*]", " ") + "*";
  }

  /**
   * Formats a code block
   * 
   * @param message
   * @return
   */
  public static String code(String message) {
    return "```" + message.replaceAll("[`]", "'") + "```";
  }

  /**
   * Formats the details into a {@link JSONObject} message for posting to slack
   * 
   * @param application
   * @param identifier
   * @param e
   * @return
   */
  private static JSONObject createAlertMessage(String application, String identifier, String... messages) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
    JSONObject json = new JSONObject();
    JSONArray blocks = new JSONArray();
    JSONObject headerSection = new JSONObject();
    headerSection.put("type", "section");
    JSONObject headerText = new JSONObject();
    headerText.put("type", "mrkdwn");
    headerText.put("text", ":warning: " + SystemConfiguration.getValue("SYSTEM_TYPE", "") + " [" + formatter.format(new Date()) + "] ("
        + Thread.currentThread().getName() + ") " + bold(application + " - " + identifier));
    headerSection.put("text", headerText);
    blocks.add(headerSection);

    for (String msg : messages) {
      JSONObject section = new JSONObject();
      section.put("type", "section");
      JSONObject text = new JSONObject();
      text.put("type", "mrkdwn");
      text.put("text", msg);
      section.put("text", text);
      blocks.add(section);
    }

    json.put("blocks", blocks);
    return json;
  }

  /**
   * Posts the input to the designated slack channel
   * 
   * @param slackInput
   * @throws IOException
   */
  private static void postToSlack(JSONObject slackInput) {
    String alertUrl = SystemParameters.getString("SLACK.ALERTS.URL");
    if (StringUtils.isBlank(alertUrl)) {
      LOG.warn("Slack Alerts URL is missing");
      return;
    }
    try {
      URL url = new URL(alertUrl);
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.addRequestProperty("Content-Type", "application/json");

      try (OutputStream out = conn.getOutputStream()) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(slackInput.toString().getBytes())) {
          LOG.debug("Writing to slack alert channel..");
          IOUtils.copy(bis, out);
        }
      }

      try (InputStream is = conn.getInputStream()) {
        if (is != null) {
          try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            IOUtils.copy(is, bos);
            String response = bos.toString();
            LOG.debug("Slack Response: " + response);
          }
        }
      }
      try (InputStream is = conn.getErrorStream()) {
        if (is != null) {
          try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            IOUtils.copy(is, bos);
            String response = bos.toString();
            LOG.debug("Slack Error Response: " + response);
          }
        }
      }
    } catch (Throwable e) {
      LOG.warn("Error in posting to slack: " + e.getMessage());
    }
  }

  /**
   * Posts the input to the designated slack channel
   * 
   * @param slackInput
   * @throws IOException
   */
  private static void postToBatchSlack(JSONObject slackInput) {
    String alertUrl = SystemParameters.getString("SLACK.BATCHES.ALERTS");
    if (StringUtils.isBlank(alertUrl)) {
      LOG.warn("Slack Batches Alerts URL is missing");
      return;
    }
    try {
      URL url = new URL(alertUrl);
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.addRequestProperty("Content-Type", "application/json");

      try (OutputStream out = conn.getOutputStream()) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(slackInput.toString().getBytes())) {
          LOG.debug("Writing to slack alert channel..");
          IOUtils.copy(bis, out);
        }
      }

      try (InputStream is = conn.getInputStream()) {
        if (is != null) {
          try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            IOUtils.copy(is, bos);
            String response = bos.toString();
            LOG.debug("Slack Response: " + response);
          }
        }
      }
      try (InputStream is = conn.getErrorStream()) {
        if (is != null) {
          try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            IOUtils.copy(is, bos);
            String response = bos.toString();
            LOG.debug("Slack Error Response: " + response);
          }
        }
      }
    } catch (Throwable e) {
      LOG.warn("Error in posting to slack: " + e.getMessage());
    }
  }

  /**
   * Posts a batch alert to slack
   * 
   * @param application
   * @param identifier
   * @param e
   * @throws IOException
   */
  public static void recordBatchAlert(String application, String identifier, String... message) {
    JSONObject jsonMsg = createBatchAlertMessage(application, identifier, message);
    postToBatchSlack(jsonMsg);
  }

  /**
   * Formats the details into a {@link JSONObject} message for posting to slack
   * 
   * @param application
   * @param identifier
   * @param e
   * @return
   */
  private static JSONObject createBatchAlertMessage(String application, String identifier, String... messages) {
    JSONObject json = new JSONObject();
    JSONObject headerSection = new JSONObject();
    JSONArray blocks = new JSONArray();
    headerSection.put("type", "section");
    JSONObject headerText = new JSONObject();
    headerText.put("type", "mrkdwn");
    headerText.put("text", ":warning: " + SystemConfiguration.getValue("SYSTEM_TYPE", ""));
    headerSection.put("text", headerText);
    // blocks.add(headerSection);

    for (String msg : messages) {
      JSONObject section = new JSONObject();
      section.put("type", "section");
      JSONObject text = new JSONObject();
      text.put("type", "mrkdwn");
      text.put("text", msg);
      section.put("text", text);
      blocks.add(section);
    }

    json.put("blocks", blocks);
    return json;
  }
}
