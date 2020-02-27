package com.ibm.cio.cmr.request.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;

public class HkeyUtil {

  protected static final Logger LOGGER = Logger.getLogger(HkeyUtil.class);

  private static final String IBM_PROFILE_SERVLET_URL = "http://w3.ibm.com/eworkplace/servlet/w3.profile.GetProfile";

  private static final Pattern STATUS_PATTERN = Pattern.compile("STATUSTYPE\\s+=\\s+(.+)\\n");
  private static final Pattern HKEY_PATTERN = Pattern.compile("HKEY\\s+=\\s+(.+)\\n");

  /**
   * This method retrieves Hkey by the user email address.
   * 
   * @param userEmail
   * @return
   * @throws InvalidHkeyException
   */
  public static String getHkeyByUserEmail(String userEmail) throws CmrException {

    BufferedReader in = null;
    StringBuilder sb = null;

    try {

      URL urlSource = new URL(IBM_PROFILE_SERVLET_URL + "?email=" + userEmail + "&format=text");
      HttpURLConnection urlCon = (HttpURLConnection) urlSource.openConnection();
      urlCon.setRequestMethod("POST");
      urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      urlCon.setDoOutput(true);
      urlCon.setDoInput(true);
      HttpURLConnection.setFollowRedirects(true);
      urlCon.setUseCaches(false);
      urlCon.setDefaultUseCaches(false);

      in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
      sb = new StringBuilder();
      String temp = null;
      while ((temp = in.readLine()) != null) {
        sb.append(temp + '\n');
      }

      String response = sb.toString();

      Matcher statusMather = STATUS_PATTERN.matcher(response);
      if (statusMather.find()) {
        String status = statusMather.group(1).trim();
        if (!status.equalsIgnoreCase("SUCCESS")) {
          throw new CmrException(MessageUtil.ERROR_HKEY_INVALID);
        }
      }

      Matcher hKeyMatcher = HKEY_PATTERN.matcher(response);
      if (hKeyMatcher.find()) {
        return hKeyMatcher.group(1).trim();
      }

    } catch (final IOException ioe) {

      LOGGER.error("Caught java.io.EOFException while connecting to." + IBM_PROFILE_SERVLET_URL + "?email=" + userEmail + "&format=text", ioe);
      throw new CmrException(MessageUtil.ERROR_GENERAL);

    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (final IOException ioe) {
        LOGGER.error("Caught java.io.EOFException while closing connection.", ioe);
      }
    }
    throw new CmrException(MessageUtil.ERROR_HKEY_INVALID);
  }

}
