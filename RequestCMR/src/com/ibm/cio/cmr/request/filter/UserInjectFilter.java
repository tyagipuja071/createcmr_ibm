package com.ibm.cio.cmr.request.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.login.LogInUserModel;
import com.ibm.cio.cmr.request.service.user.UserService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.oauth.OAuthUtils;
import com.ibm.cio.cmr.request.util.oauth.SimpleJWT;
import com.ibm.cio.cmr.request.util.oauth.UserHelper;
import com.ibm.json.java.JSONObject;

/**
 * This filter ensures valid user is present at the request. If not, the request
 * redirects user to SSO. Retrieves and validates the JWT using JWK presented at
 * jwks endpoint.
 * 
 * @author Hesller Huller
 *
 */
@Component
@WebFilter(filterName = "UserInjectFilter", urlPatterns = "/*")
public class UserInjectFilter implements Filter {

  @Autowired
  UserService userService;

  protected static final Logger LOG = Logger.getLogger(UserInjectFilter.class);

  @Nullable
  private String encoding;

  private boolean forceRequestEncoding = false;

  private boolean forceResponseEncoding = false;

  public UserInjectFilter() {

  }

  public UserInjectFilter(String encoding) {
    this(encoding, false);
  }

  public UserInjectFilter(String encoding, boolean forceEncoding) {
    this(encoding, forceEncoding, forceEncoding);
  }

  public UserInjectFilter(String encoding, boolean forceRequestEncoding, boolean forceResponseEncoding) {
    this.encoding = encoding;
    this.forceRequestEncoding = forceRequestEncoding;
    this.forceResponseEncoding = forceResponseEncoding;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

    if (!OAuthUtils.isSSOActivated()) {
      filterChain.doFilter(request, response);
      return;
    }

    HttpServletRequest httpReq = (HttpServletRequest) request;
    HttpServletResponse httpResp = (HttpServletResponse) response;

    assignEncoding(this.encoding, httpReq, httpResp);

    HttpSession session = shouldCreateSession(httpReq);

    try {
      if (shouldFilterURLEndpoint(httpReq, httpResp)) {

        AppUser user = AppUser.getUser(httpReq);

        if (user == null) {
          LOG.warn("No user on the session yet...");
          UserHelper userHelper = new UserHelper();

          String ibmUniqueId = userHelper.getUNID();

          LOG.debug("Subject is set: " + ibmUniqueId);

          if (ibmUniqueId == null || ibmUniqueId.trim().isEmpty()) {
            LOG.debug("No IBM ID detected. Redirecting to W3 ID ...");
            httpReq.getSession().invalidate();
            setupPreviousReqID(httpReq, httpResp);

            httpResp.sendRedirect("/CreateCMR/oidc");
            return;
          } else {
            LOG.debug("IBM ID found. Injecting App User");
            LOG.trace("User:" + userHelper.getDisplayName() + "'s ibmUniqueId is " + userHelper.getUNID());
            LOG.trace("User:" + userHelper.getDisplayName() + "'s ibmFullName is " + userHelper.getPrincipalName());
          }
          session.setAttribute("userHelper", userHelper);
          setSessionAttributes(httpReq, httpResp);
          filterChain.doFilter(request, response);
          return;
        }
      }
    } catch (IllegalStateException e) {
      LOG.error("Error when attempting to redirect to W3 ID ", e);
    } catch (Exception e) {
      LOG.error("Error processing UserInjectFilter", e);
    }

    filterChain.doFilter(request, response);

  }

  private void assignEncoding(String enconding, HttpServletRequest httpReq, HttpServletResponse httpResp) {

    try {
      if (encoding != null) {
        if (isForceRequestEncoding() || httpReq.getCharacterEncoding() == null) {
          httpReq.setCharacterEncoding(encoding);
        }
        if (isForceResponseEncoding()) {
          httpResp.setCharacterEncoding(encoding);
        }
      }
    } catch (UnsupportedEncodingException e) {
      LOG.error("Unsuported enconding operation: " + e);
    }
  }

  public boolean isForceResponseEncoding() {
    return this.forceResponseEncoding;
  }

  public boolean isForceRequestEncoding() {
    return this.forceRequestEncoding;
  }

  private HttpSession shouldCreateSession(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    if (session == null) {
      return req.getSession();
    }
    return session;
  }

  private void setSessionAttributes(HttpServletRequest request, HttpServletResponse response) throws CmrException, Exception {
    HttpSession session = request.getSession();

    UserHelper userHelper = (UserHelper) session.getAttribute("userHelper");
    SimpleJWT idToken = userHelper.getIDToken();
    String accessToken = userHelper.getAccessToken();
    Long expiresIn = Long.parseLong(userHelper.getPrivateHashtableAttr("expires_in"));
    JSONObject claims = idToken.getClaims();

    String userIntranetEmail = ((String) claims.get("emailAddress")).toLowerCase();
    session.setAttribute("userIntranetEmail", userIntranetEmail);

    LogInUserModel loginUser = new LogInUserModel();
    loginUser.setUsername(userIntranetEmail);

    session.setAttribute("loggedInUserModel", loginUser);
    session.setAttribute("accessToken", accessToken);
    session.setAttribute("tokenExpiringTime", LocalDateTime.now().plus(expiresIn, ChronoUnit.SECONDS));

    session.setAttribute("loginUser", loginUser);

    // AppUser appUser = new AppUser();
    // appUser.setIntranetId(loginUser.getUsername().toLowerCase());
    // request.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY,
    // appUser);

    OAuthUtils oAuthUtils = new OAuthUtils();
    oAuthUtils.authorizeAndSetRoles(loginUser, userService, request, response);

  }

  /**
   * Verifies if the current URL should be filtered by SSO
   * 
   * @param request
   * @param response
   * @return
   */
  private boolean shouldFilterURLEndpoint(HttpServletRequest request, HttpServletResponse response) {
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

    if (request.getParameterMap().keySet().contains("errorMessage") && url.endsWith("/login")) {
      return false;
    }

    if (url.contains("/") && url.substring(url.lastIndexOf("/")).contains(".")) {
      // static resources
      return false;
    }

    return true;
  }

  @Nullable
  public String getEncoding() {
    return this.encoding;
  }

  public void setForceRequestEncoding(boolean forceRequestEncoding) {
    this.forceRequestEncoding = forceRequestEncoding;
  }

  public void setForceResponseEncoding(boolean forceResponseEncoding) {
    this.forceResponseEncoding = forceResponseEncoding;
  }

  private void setupPreviousReqID(HttpServletRequest request, HttpServletResponse response) {
    String requestURI = request.getRequestURI();
    response.addCookie(new Cookie("previousURI", requestURI));
    request.getSession().setAttribute("previousURI", requestURI);
    String referer = request.getHeader("referer");

    if (StringUtils.isNotBlank(referer)) {
      setReqIDFromReferer(referer).ifPresent(reqID -> request.setAttribute("r", reqID));
      setReqId(referer).ifPresent(reqID -> request.getSession().setAttribute("previousURI", "/CreateCMR/request/" + reqID));
    }
  }

  private Optional<String> setReqIDFromReferer(String referer) {
    if (referer.matches("(.*)logout\\?r=[0-9]+")) {
      Pattern pattern = Pattern.compile("\\?r=[0-9]+");
      Matcher matcher = pattern.matcher(referer);
      if (matcher.find()) {
        return Optional.of(matcher.group().substring(3));
      }
    }
    return Optional.empty();
  }

  private Optional<String> setReqId(String referer) {
    if (referer.matches("(.*)logout\\?r=[0-9]+")) {
      Pattern pattern = Pattern.compile("\\?r=[0-9]+");
      Matcher matcher = pattern.matcher(referer);
      if (matcher.find()) {
        return Optional.of(matcher.group().substring(3));
      }
    }
    return Optional.empty();
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    LOG.info("CreateCMR " + config.getFilterName() + " initialized.");
    setEncoding("UTF-8");
    setForceEncoding(true);
  }

  public void setEncoding(@Nullable String encoding) {
    this.encoding = encoding;
  }

  public void setForceEncoding(boolean forceEncoding) {
    this.forceRequestEncoding = forceEncoding;
    this.forceResponseEncoding = forceEncoding;
  }

  @Override
  public void destroy() {
    // NOOP
  }

}
