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
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.login.LogInUserModel;
import com.ibm.cio.cmr.request.service.user.UserService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.oauth.OAuthUtils;
import com.ibm.cio.cmr.request.util.oauth.Tokens;

/**
 * This filter ensures valid user is present at the request. If not, the request
 * redirects user to SSO. Retrieves and validates the JWT using JWK presented at
 * jwks endpoint.
 * 
 * @author Hesller Huller
 *
 */
@Component
@WebFilter(
    filterName = "AppUserInjectFilter",
    urlPatterns = "/*")
public class AppUserInjectFilter implements Filter {

  @Autowired
  private UserService userService;

  protected static final Logger LOG = Logger.getLogger(AppUserInjectFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

    // can be used to deactivate this filter completely
    String activateFilter = SystemConfiguration.getValue("ACTIVATE_SSO");
    if (activateFilter.equalsIgnoreCase("false")) {
      filterChain.doFilter(request, response);
      return;
    }

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    HttpSession session = req.getSession();

    String url = req.getRequestURI();
    String userIntranetEmail = null;

    try {
      if (shouldFilter(req)) {

        AppUser user = AppUser.getUser(req);
        if (user == null) {
          LOG.trace("Single Sign On injecting for this url: " + url);

          LOG.warn("No user on the session yet. Checking IBM ID...");

          // try to get user Intranet
          userIntranetEmail = (String) session.getAttribute("userIntranetEmail");

          // get all request params
          Set<String> paramNames = request.getParameterMap().keySet();

          // when request contains code and grant_id, means we are at second
          // step
          // of
          // SSO and need to get access token
          if (paramNames.contains("code") && paramNames.contains("grant_id")) {
            LOG.trace("Requesting access token for grant_id: " + request.getParameter("grant_id"));

            // get access_token and id_token
            String code = request.getParameter("code");
            String rawToken = OAuthUtils.getAccessToken(code);

            // parse raw token
            Tokens tokens = OAuthUtils.parseToken(rawToken);

            // validate JWT signature
            boolean isJWTValid = false;
            try {
              LOG.trace("Validating signature for grant_id: " + request.getParameter("grant_id"));
              isJWTValid = OAuthUtils.validateSignature();
              LOG.trace("JWT Signature validated successfully for grant_id: " + request.getParameter("grant_id"));
            } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
              // TODO interrupt the thread here
              LOG.error("An error occured when validating the signature: ", e);
            }

            // assign AppUser
            if (isJWTValid) {
              userIntranetEmail = (String) tokens.getId_token().getClaims().get("emailAddress");
              session.setAttribute("userIntranetEmail", userIntranetEmail);

              LogInUserModel loginUser = new LogInUserModel();
              loginUser.setUsername(userIntranetEmail);

              new OAuthUtils().authorizeAndSetRoles(loginUser, userService, req, resp);

              session.setAttribute("loggedInUserModel", loginUser);
              session.setAttribute("accessToken", tokens.getAccess_token());
              session.setAttribute("tokenExpiringTime", tokens.getExpires_in());

              filterChain.doFilter(req, resp);
              return;
            } else {
              LOG.trace("Invalid Token! Unable to proceed with the request.");
              // TODO what to do if token is invalid or expired?
              OAuthUtils.revokeToken(tokens.getAccess_token());
              AppUser.remove(req);
              session.invalidate();
              filterChain.doFilter(req, resp);
              return;
            }
          }

          LOG.debug("No IBM ID detected. Redirecting to W3 ID Provisioner...");
          HttpServletResponse httpResp = (HttpServletResponse) response;
          session.invalidate();
          httpResp.sendRedirect(OAuthUtils.getAuthorizationCodeURL());
          return;
        }
      }
    } catch (Exception e) {
      LOG.error("Error processing AppUserInjectFilter", e);
    }

    filterChain.doFilter(req, resp);

  }

  private long extractRequestId(HttpServletRequest request) {
    String reqIdParam = request.getParameter("reqId");
    if (reqIdParam != null && StringUtils.isNumeric(reqIdParam) && !"0".equals(reqIdParam)) {
      return Long.parseLong(reqIdParam);
    }
    Object reqIdAtt = request.getAttribute("reqId");
    if (reqIdAtt != null && StringUtils.isNumeric(reqIdAtt.toString()) && !"0".equals(reqIdAtt.toString())) {
      return Long.parseLong(reqIdAtt.toString());
    }

    String url = request.getRequestURI();
    if (url != null) {
      if (url.contains("?")) {
        url = url.substring(0, url.indexOf("?"));
      }
      if (url.matches(".*/request/[0-9]{1,}") || url.matches(".*/massrequest/[0-9]{1,}")) {
        String reqId = url.substring(url.lastIndexOf("/") + 1);
        if (StringUtils.isNumeric(reqId) && !"0".equals(reqId)) {
          return Long.parseLong(reqId);
        }
      }
    }

    return 0;
  }

  private boolean shouldFilter(HttpServletRequest request) {
    String url = request.getRequestURI();

    if (url.endsWith("/update")) {
      return false;
    }
    if (url.endsWith("/logout")) {
      return false;
    }
    if (url.endsWith("/external.json")) {
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
    SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    LOG.info("CreateCMR " + config.getFilterName() + " initialized.");
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
