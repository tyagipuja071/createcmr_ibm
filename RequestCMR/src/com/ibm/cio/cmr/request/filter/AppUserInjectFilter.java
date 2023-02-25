package com.ibm.cio.cmr.request.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.oauth.OAuthUtils;
import com.ibm.cio.cmr.request.util.oauth.UserHelper;

/**
 * This filter ensures valid user is present at the request. If not, the request
 * redirects user to SSO. Retrieves and validates the JWT using JWK presented at
 * jwks endpoint.
 * 
 * @author Hesller Huller
 *
 */
@WebFilter(
    filterName = "AppUserInjectFilter",
    urlPatterns = "/*")
public class AppUserInjectFilter implements Filter {

  protected static final Logger LOG = Logger.getLogger(AppUserInjectFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    String url = req.getRequestURI();

    if (shouldFilter(req)) {

      LOG.trace("Single Sign On injecting for this url: " + url);
      AppUser user = AppUser.getUser(req);
      if (user == null) {
        LOG.warn("No user on the session yet. Checking IBM ID...");

        // get all request params
        Set<String> paramNames = request.getParameterMap().keySet();

        // when request contains code and grant_id, means we are at second step
        // of
        // SSO and need to get access token
        if (paramNames.contains("code") && paramNames.contains("grant_id")) {
          LOG.trace("Obtaining access token for grant_id: " + request.getParameter("grant_id"));

          // get access_token and id_token
          String code = request.getParameter("code");
          String rawToken = OAuthUtils.getAccessToken(code);

          // parse raw token
          OAuthUtils.parseToken(rawToken);

          // validate JWT signature
          try {
            OAuthUtils.validateSignature();
          } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            // TODO interrupt the thread here
            LOG.error(e.getMessage());

            // close the thread as signature validation was invalid.
            Thread.currentThread().interrupt();
          }

          // ============= work done till here =================
        }

        // iterating over parameter names and get its value
        for (String name : paramNames) {
          String value = request.getParameter(name);
          System.out.println(name + ": " + value);
          System.out.println("<br>");
        }

        String body = getBody((HttpServletRequest) request);

        UserHelper userHelper = new UserHelper();

        String ibmUniqueId = userHelper.getUNID();

        if (ibmUniqueId == null || ibmUniqueId.trim().isEmpty()) {
          // redirect to /ibmid
          LOG.debug("No IBM ID detected. Redirecting to IBM ID intercept..");
          HttpServletResponse httpResp = (HttpServletResponse) response;
          req.getSession().invalidate();
          httpResp.sendRedirect(OAuthUtils.getAuthorizationCodeURL());
          return;
        } else {
          LOG.debug("IBM ID found. Injecting App User");
          LOG.trace("User:" + userHelper.getDisplayName() + "'s ibmUniqueId is " + userHelper.getUNID());
          LOG.trace("User:" + userHelper.getDisplayName() + "'s ibmFullName is " + userHelper.getPrincipalName());
          LOG.trace("User:" + userHelper.getDisplayName() + "'s ibmId is " + userHelper.getEmailAddress());
        }
      }
    }

    filterChain.doFilter(request, response);

  }

  private boolean shouldFilter(HttpServletRequest request) {
    String url = request.getRequestURI();

    if (url.endsWith("/api")) {
      return false;
    }
    // if (url.contains("/") &&
    // url.substring(url.lastIndexOf("/")).contains(".")) {
    // return false;
    // }

    return true;
  }

  @Override
  public void destroy() {
    // NOOP
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    LOG.info("CreateCMR AppUser inject Filter initialized.");
  }

  public String getBody(HttpServletRequest request) throws IOException {

    String body = null;
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader bufferedReader = null;

    try {
      InputStream inputStream = request.getInputStream();
      if (inputStream != null) {
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        char[] charBuffer = new char[128];
        int bytesRead = -1;
        while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
          stringBuilder.append(charBuffer, 0, bytesRead);
        }
      } else {
        stringBuilder.append("");
      }
    } catch (IOException ex) {
      throw ex;
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException ex) {
          throw ex;
        }
      }
    }

    body = stringBuilder.toString();
    return body;
  }
}
