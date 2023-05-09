/*
 * Copyright 2005 Frank W. Zammetti
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.ibm.cio.cmr.request.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;

/**
 * This filter can test to see if a session has expired, and if it has can
 * redirect or forward to a page of your choice. <br>
 * <br>
 * Init parameters are: <br>
 * <ul>
 * <li><b>pathSpec </b>- Either "include" or "exclude". This determines whether
 * the list of paths in the pathList parameter is a list of paths to include in
 * filter functionality, or a list of paths to exclude. Required: No. Default:
 * None.</li> <br>
 * <br>
 * <li><b>pathList </b>- This is a comma-separated list of paths, which can use
 * asterisk for wildcard support, that denotes either paths to include or
 * exclude from the functioning of this filter (depending on what pathSpec is
 * set to). The paths ARE case-senitive! There is no limit to how many items can
 * be specified, although for performance reasons a developer will probably want
 * to specify as few as possible to get the job done (each requested path is
 * matched via regex). Note also that you are of course still required to
 * specify a path for the filter itself as per the servlet spec. This parameter
 * however, together with pathSpec, gives you more control and flexibility than
 * that setting alone. Required: No. Default: None. <br>
 * <br>
 * General note on pathSpec and pathList: If pathSpec is not specified but
 * pathList IS, then 'exclude' is assumed for pathSpec. If pathSpec is specified
 * by pathList IS NOT, then the filter WILL NEVER EXECUTE (this is technically a
 * misconfiguration). If NEITHER is defined then the generic filter mapping will
 * be in effect only.</li>
 * </ul>
 * <br>
 * Example configuration in web.xml: <br>
 * <br>
 * &lt;filter&gt; <br>
 * &nbsp;&nbsp;&lt;filter-name&gt;SessionInactivityFilter&lt; /filter-name&gt;
 * <br>
 * &nbsp;&nbsp;&lt;filter-class&gt;javawebparts.filter.
 * SessionInactivityFilter&lt;/filter-class&gt; <br>
 * &nbsp;&nbsp;&lt;init-param&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;param-name&gt;forwardTo&lt;/param-name&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;param-value&gt;/SIFReject.jsp&lt;
 * /param-value&gt; <br>
 * &nbsp;&nbsp;&lt;/init-param&gt; <br>
 * &lt;/filter&gt; <br>
 * <br>
 * &lt;filter-mapping&gt; <br>
 * &nbsp;&nbsp;&lt;filter-name&gt;SessionInactivityFilter&lt; /filter-name&gt;
 * <br>
 * &nbsp;&nbsp;&lt;url-pattern&gt;/*&lt;/url-pattern&gt; <br>
 * &lt;/filter-mapping&gt;
 * 
 * @author <a href="mailto:fzammetti@omnytex.com">Frank W. Zammetti </a> with
 *         modifications by Tamas Szabo.
 */
public class SessionInactivityFilter implements Filter {

  private static final String HTTP_HEADER_AUTOMATED_REQUEST = "Automated-Request";
  protected static final Logger LOG = Logger.getLogger(SessionInactivityFilter.class);

  /**
   * This static initializer block tries to load all the classes this one
   * depends on (those not from standard Java anyway) and prints an error
   * meesage if any cannot be loaded for any reason.
   */
  static {
    try {
      Class.forName("javax.servlet.Filter");
      Class.forName("javax.servlet.FilterChain");
      Class.forName("javax.servlet.FilterConfig");
      Class.forName("javax.servlet.http.HttpServletRequest");
      Class.forName("javax.servlet.ServletException");
      Class.forName("javax.servlet.ServletRequest");
      Class.forName("javax.servlet.ServletResponse");
      Class.forName("org.apache.commons.logging.Log");
      Class.forName("org.apache.commons.logging.LogFactory");
    } catch (ClassNotFoundException e) {
      LOG.error(
          "SessionInactivityFilter" + " could not be loaded by classloader because classes it depends" + " on could not be found in the classpath...",
          e);
    }
  }

  /**
   * Whether pathList includes or excludes.
   */
  private String pathSpec;

  /**
   * List of paths for filter functionality determination.
   */
  private List<String> pathList = new ArrayList<String>();

  /**
   * A path to forward the user to if the user's session has expired
   */
  private String sessionTimeoutPath;

  /**
   * The number of minutes of inactivity allowed
   */
  public int timeoutLengthMinutes;

  /**
   * Destroy.
   */
  @Override
  public void destroy() {

  } // End destroy.

  /**
   * Initialize this filter.
   * 
   * @param filterConfig
   *          The configuration information for this filter.
   * @throws ServletException
   *           ServletException.
   */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

    // Do pathSpec and pathList init work.
    pathSpec = filterConfig.getInitParameter("pathSpec");
    pathList = Arrays.asList(filterConfig.getInitParameter("pathList").split(","));

    sessionTimeoutPath = filterConfig.getInitParameter("sessionTimeoutPath");
    timeoutLengthMinutes = Integer.parseInt(filterConfig.getInitParameter("timeoutLengthMinutes"));

  } // End init().

  /**
   * Do filter's work.
   * 
   * @param request
   *          The current request object.
   * @param response
   *          The current response object.
   * @param filterChain
   *          The current filter chain.
   * @throws ServletException
   *           ServletException.
   * @throws IOException
   *           IOException.
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    HttpServletRequest req = (HttpServletRequest) request;
    if (filterPath(request)) {

      // LOG.debug("Inside the Filter: "+req.getRequestURI());
      /*
       * If user has a valid session, perform the addition check for last user
       * triggered request
       */
      HttpSession session = req.getSession(false);
      if (session != null) {
        // if (session != null && req.isRequestedSessionIdValid()) {

        String accessToken = (String) session.getAttribute("accessToken");
        if (session.getAttribute(CmrConstants.SESSION_APPUSER_KEY) == null) {
          LOG.debug("No user session found");

          RequestUtils.performLogoutActivities(req);

          long reqId = RequestUtils.extractRequestId((HttpServletRequest) request);
          if (reqId > 0) {
            req.setAttribute("r", reqId);
          }
          String findCmrParams = extractFindCMRParams((HttpServletRequest) request);
          if (!StringUtils.isBlank(findCmrParams)) {
            req.setAttribute("c", findCmrParams);
          }
          req.getRequestDispatcher(sessionTimeoutPath).forward(req, response);
          return;
        }

        if (tooLongSinceLastUserTriggeredRequest(req)) {
          LOG.debug("User session has expired");

          RequestUtils.performLogoutActivities(req);

          long reqId = RequestUtils.extractRequestId((HttpServletRequest) request);

          if (reqId > 0) {
            req.setAttribute("r", reqId);
          }

          String findCmrParams = extractFindCMRParams((HttpServletRequest) request);
          if (!StringUtils.isBlank(findCmrParams)) {
            req.setAttribute("c", findCmrParams);
          }

          req.getRequestDispatcher(sessionTimeoutPath).forward(req, response);
          return;
        }

        LocalDateTime tokenExpiringTime = (LocalDateTime) session.getAttribute("tokenExpiringTime");
        if (tokenExpiringTime != null && LocalDateTime.now().isAfter(tokenExpiringTime)) {
          LOG.debug("Access token expired! ");

          RequestUtils.performLogoutActivities(req);

          long reqId = RequestUtils.extractRequestId((HttpServletRequest) request);
          if (reqId > 0) {
            req.setAttribute("r", reqId);
          }
          String findCmrParams = extractFindCMRParams((HttpServletRequest) request);
          if (!StringUtils.isBlank(findCmrParams)) {
            req.setAttribute("c", findCmrParams);
          }
          req.getRequestDispatcher(sessionTimeoutPath).forward(req, response);
          return;
        }

        if (!req.getRequestURI().contains("/sessioncheck")) {
          updateLastUserTriggeredRequestDate(req);
        }

      } else {
        // LOG.debug("User has no session");
        long reqId = RequestUtils.extractRequestId((HttpServletRequest) request);
        if (reqId > 0) {
          req.setAttribute("r", reqId);
        }
        String findCmrParams = extractFindCMRParams((HttpServletRequest) request);
        if (!StringUtils.isBlank(findCmrParams)) {
          req.setAttribute("c", findCmrParams);
        }
        req.getRequestDispatcher(sessionTimeoutPath).forward(req, response);
        return;
      }
    }

    filterChain.doFilter(request, response);

  } // End doFilter().

  private String extractFindCMRParams(HttpServletRequest request) {
    String url = request.getRequestURI();
    if (url.endsWith("findcmr")) {
      String cmrNo = request.getParameter("cmrNo");
      String cntry = request.getParameter("cntry");
      if (!StringUtils.isBlank(cmrNo) && !StringUtils.isBlank(cntry)) {
        return Base64.getEncoder().encodeToString(("f&cmrNo=" + cmrNo + "&cntry=" + cntry).getBytes());
      }
    } else {
      String reqType = request.getParameter("reqType");
      String cntry = request.getParameter("cmrIssuingCntry");
      if (!StringUtils.isBlank(reqType) && !StringUtils.isBlank(cntry)) {
        return Base64.getEncoder().encodeToString(("r&cmrIssuingCntry=" + cntry + "&reqType=" + reqType).getBytes());
      }
    }
    return null;
  }

  /**
   * Updates the last time the current user actually submitted a request. This
   * is to distinguish from automatic timed checks for urgent messages that
   * happen every two minutes. We don't want these kinds of requests to keep the
   * session active forever.
   * 
   * @param request
   *          Request object
   */
  public void updateLastUserTriggeredRequestDate(HttpServletRequest request) {
    String automatedRequest = request.getHeader(HTTP_HEADER_AUTOMATED_REQUEST);
    if (automatedRequest == null || !"true".equalsIgnoreCase(automatedRequest)) {
      request.getSession().setAttribute("cmr.last.request.date", new Date());
    }
  }

  /**
   * Returns true if it has been at least one hour since the user submitted an
   * HTTP request. Note! The urgent message check requests submitted
   * automatically by javascript don't count as user requests.
   * 
   * @param request
   *          last user request date extracted from this object.
   * @return true if session has expired
   */
  public boolean tooLongSinceLastUserTriggeredRequest(HttpServletRequest request) {
    Date dateOfLastUserRequest = (Date) request.getSession().getAttribute("cmr.last.request.date");
    if (dateOfLastUserRequest == null) {
      return false;
    }
    long millisecondsSinceLastRequest = new Date().getTime() - dateOfLastUserRequest.getTime();
    long secondsSinceLastRequest = millisecondsSinceLastRequest / (1000);
    String timeout = SystemConfiguration.getValue("SESSION_TIMEOUT", String.valueOf(timeoutLengthMinutes));
    boolean tooLong = secondsSinceLastRequest > Integer.parseInt(timeout) * 60; // convert
    if (tooLong) {
      AppUser user = AppUser.getUser(request);
      LOG.debug("Session needs to be timed out for " + (user != null ? user.getIntranetId() : "(no user)") + " [" + secondsSinceLastRequest + "/"
          + (Integer.parseInt(timeout) * 60) + "]");
    }
    return tooLong;
  }

  /**
   * Checks if the request path should be checked for session inactivity
   * 
   * @param request
   * @param pathList
   * @param inPathSpec
   * @return
   */
  private boolean filterPath(ServletRequest request) {

    // Quick check #1: if pathSpec and pathList are both null, return true
    // because the generic filter mapping is in effect only.
    if (pathList == null && pathSpec == null) {
      return true;
    }

    // Quick check #2: If pathSpec is null but pathList IS NOT null, then
    // we're going to pretend pathSpec is 'exclude'.
    if (pathSpec == null && pathList != null) {
      pathSpec = "exclude";
    }

    // Quick check #3: If pathSpec is not null and pathList IS null, then
    // we're going to just return false since this is really technically
    // a configuration error and we don't know for sure what to do here.
    if (pathSpec != null && pathList == null) {
      return false;
    }

    // Getting the requested path
    HttpServletRequest req = (HttpServletRequest) request;
    String path = req.getRequestURI();

    if (path.toLowerCase().endsWith("css") || path.toLowerCase().endsWith("js") || path.toLowerCase().endsWith("jpg")
        || path.toLowerCase().endsWith("png") || path.toLowerCase().endsWith("ico")) {
      return false;
    }

    if (path.toLowerCase().contains("auto/config")) {
      return true;
    }

    // See if the requested path matches any (or multiple) paths in the
    // pathList collection.
    boolean pathInCollection = false;
    for (Iterator<String> it = pathList.iterator(); it.hasNext();) {

      String np = it.next();
      if (path.indexOf(np) > -1) {
        pathInCollection = true;
        break;
      }
    }

    // If the path was in the collection and the pathSpec is include, or if
    // the path was NOT in the collection and pathSpec is exclude, then
    // we want the calling filter to do its work, so return true, otherwise
    // return false.
    boolean retVal = false;
    if ((pathInCollection && pathSpec.equalsIgnoreCase("include")) || (!pathInCollection && pathSpec.equalsIgnoreCase("exclude"))) {
      retVal = true;
    } else {
      retVal = false;
    }
    return retVal;

  } // Ebd filterPath().

} // End class.
