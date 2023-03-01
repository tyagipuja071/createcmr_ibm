/**
 * 
 */
package com.ibm.cio.cmr.request.controller.login;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.entity.UserPref;
import com.ibm.cio.cmr.request.entity.UserPrefPK;
import com.ibm.cio.cmr.request.filter.SessionInactivityFilter;
import com.ibm.cio.cmr.request.model.approval.MyApprovalsModel;
import com.ibm.cio.cmr.request.model.login.LogInUserModel;
import com.ibm.cio.cmr.request.model.pref.UserPrefModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.user.UserService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.oauth.OAuthUtils;
import com.ibm.cmr.services.client.AuthorizationClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.auth.Authorization;
import com.ibm.cmr.services.client.auth.AuthorizationRequest;
import com.ibm.cmr.services.client.auth.AuthorizationRequest.ApplicationCode;
import com.ibm.cmr.services.client.auth.AuthorizationResponse;
import com.ibm.cmr.services.client.auth.Role;

/**
 * Handles the login page and home page and related functions such as logout,
 * timeout, and actual login process
 * 
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class LoginController extends BaseController {

  private static final Logger LOG = Logger.getLogger(LoginController.class);

  @Autowired
  private UserService userService;

  /**
   * Handles the display of the login page
   * 
   * @param request
   * @param response
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/login")
  public ModelAndView showLoginPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
    long reqId = 0;
    try {
      reqId = Long.parseLong(request.getParameter("r"));
    } catch (Exception e) {

    }
    AppUser user = AppUser.getUser(request);
    if (user != null) {
      SessionInactivityFilter filterInstance = new SessionInactivityFilter();
      filterInstance.timeoutLengthMinutes = 120;
      if (filterInstance.tooLongSinceLastUserTriggeredRequest(request)) {
        AppUser.remove(request);
      } else {
        filterInstance.updateLastUserTriggeredRequestDate(request);
        if (reqId > 0) {
          return new ModelAndView("redirect:/request/" + reqId, "loginUser", new LogInUserModel());
        } else {
          return new ModelAndView("redirect:/home", "loginUser", new LogInUserModel());
        }
      }
    }
    LogInUserModel userModel = new LogInUserModel();
    userModel.setR(reqId);
    return new ModelAndView("login", "loginUser", userModel);
  }

  /**
   * Handles the display of the Home page
   * 
   * @param request
   * @param response
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/home",
      method = RequestMethod.GET)
  public ModelAndView showHomePage(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
    ModelAndView mv = new ModelAndView("landingPage", "loginUser", new LogInUserModel());
    setPageKeys("HOME", "OVERVIEW", mv);
    return mv;
  }

  /**
   * This method handles transition to the timeout page.
   * 
   * @param model
   *          model map object
   * @return {@link ModelAndView} "login"
   */
  @RequestMapping(
      value = "/timeout")
  public ModelAndView showTimeoutPage(HttpServletRequest request, ModelMap model) {
    LogInUserModel newModel = new LogInUserModel();
    MessageUtil.setErrorMessage(model, MessageUtil.ERROR_TIMEOUT);
    Object reqId = request.getAttribute("r");
    String findcmrparams = (String) request.getAttribute("c");
    if (reqId != null && !"0".equals(reqId.toString().trim())) {
      model.addAttribute("r", reqId.toString());
      newModel.setR(Long.parseLong(reqId.toString()));
    }
    if (!StringUtils.isBlank(findcmrparams)) {
      model.addAttribute("c", findcmrparams);
      newModel.setC(findcmrparams);
    }
    return new ModelAndView("redirect:/login", "loginUser", new LogInUserModel());
  }

  /**
   * Handles the logout function
   * 
   * @param model
   * @param request
   * @return
   */
  @RequestMapping(
      value = "/logout",
      method = RequestMethod.GET)
  public ModelAndView performLogout(ModelMap model, HttpServletRequest request) {

    // revoke token
    String access_token = (String) request.getSession().getAttribute("accessToken");
    OAuthUtils.revokeToken(access_token);

    AppUser.remove(request);
    request.getSession().invalidate();

    ModelAndView mv = new ModelAndView("redirect:/login", "loginUser", new LogInUserModel());
    MessageUtil.setInfoMessage(mv, MessageUtil.INFO_LOGOUT);
    return mv;
  }

  /**
   * Handles the actual login process
   * 
   * @param loginUser
   * @param result
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(
      value = "/performLogin",
      method = RequestMethod.POST)
  public ModelAndView performLogin(@ModelAttribute("loginUser") LogInUserModel loginUser, HttpServletRequest request, HttpServletResponse response) {

    LOG.info("Logon Attempt (" + CmrConstants.DATE_TIME_FORMAT().format(SystemUtil.getCurrentTimestamp()) + ") by " + loginUser.getUsername());

    ModelAndView mv = null;
    try {
      boolean authenticated = userService.authenticateUser(loginUser.getUsername(), loginUser.getPassword());

      if (authenticated) {

        boolean isApprover = false;

        // implement the new Role mapping
        AuthorizationResponse authResp = authenticateViaService(loginUser.getUsername());

        // authResp.setAuthorized(false);

        if (authResp.isError()) {
          // error in service layer
          throw new CmrException(MessageUtil.ERROR_CANNOT_AUTHENTICATE);
        }

        EntityManager entityManager = JpaManager.getEntityManager();
        try {

          if (!authResp.isAuthorized()) {
            // user has no roles, check if approver first
            LOG.debug("User has no CreateCMR roles. Checking if approver..");
            isApprover = isApprover(entityManager, loginUser.getUsername());

            if (!isApprover) {
              throw new CmrException(MessageUtil.ERROR_BLUEGROUPS_AUTH);
            } else {
              Authorization auth = new Authorization();
              auth.setRoles(new ArrayList<Role>());
              authResp.setAuthorization(auth);
            }
          }

          LOG.debug("User " + loginUser.getUsername() + " authenticated and authorised successfully");

          String userCnum = userService.getUserCnum(loginUser.getUsername());

          Map<String, String> bpPersonDetails = BluePagesHelper.getBluePagesDetailsByIntranetAddr(loginUser.getUsername());

          boolean inAdminGroup = false;
          boolean inProcessorGroup = false;
          boolean inRequestorGroup = false;
          boolean inCMDEGroup = false;

          AppUser appUser = new AppUser();
          appUser.setUserCnum(userCnum);
          appUser.setIntranetId(loginUser.getUsername().toLowerCase());
          appUser.setEmpName(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_NAME));
          appUser.setCountryCode(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_COUNTRY_CODE));
          String notesEmail = bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_NOTES_MAIL);

          // set the user roles
          String roleKey = null;
          Authorization auth = authResp.getAuthorization();
          for (Role role : auth.getRoles()) {
            roleKey = role.getRoleId();
            if (roleKey.equals(CmrConstants.ROLES.ADMIN.toString())) {
              inAdminGroup = true;
            }
            if (roleKey.equals(CmrConstants.ROLES.PROCESSOR.toString())) {
              inProcessorGroup = true;
            }
            if (roleKey.equals(CmrConstants.ROLES.REQUESTER.toString())) {
              inRequestorGroup = true;
            }
            if (roleKey.equals(CmrConstants.ROLES.CMDE.toString())) {
              inCMDEGroup = true;
            }
            appUser.addRole(roleKey, role.getSubRoleId());
          }

          if (isApprover) {
            appUser.setApprover(true);
          } else if (isApprover(entityManager, loginUser.getUsername())) {
            appUser.setHasApprovals(true);
          }
          appUser.setAuth(auth);
          LOG.debug("User " + loginUser.getUsername() + ": Roles = " + auth.getRdcRoles().size() + " Auth Groups: " + auth.getAuthGroups().size());

          // set CMR Owner via blue pages' notes email
          appUser.setNotesEmailId(notesEmail);
          if (notesEmail.toUpperCase().contains("LENOVO")) {
            appUser.setCompanyCode("KAU");
          } else if (notesEmail.toUpperCase().contains("TRURO")) {
            appUser.setCompanyCode("TRU");
          } else if (notesEmail.toUpperCase().contains("FONSECA")) {
            appUser.setCompanyCode("FON");
          } else if (notesEmail.toUpperCase().contains("IBM")) {
            appUser.setCompanyCode("IBM");
          }

          PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("LOGIN.HAS_PREF"));
          query.setParameter("USER_ID", appUser.getIntranetId().toLowerCase());
          List<Object[]> results = query.getResults(2);
          boolean hasDelegate = false;
          boolean hasRecord = false;
          for (Object[] result : results) {
            hasRecord = true;
            if (!StringUtils.isEmpty((String) result[2])) {
              hasDelegate = true;
            }
            if (!StringUtils.isEmpty((String) result[3]) && appUser.getProcessingCenter() == null) {
              appUser.setProcessingCenter((String) result[3]);
            }
            if (!StringUtils.isEmpty((String) result[1]) && appUser.getCmrIssuingCntry() == null) {
              appUser.setCmrIssuingCntry((String) result[1]);
            }
            if (!StringUtils.isEmpty((String) result[4]) && appUser.getBluePagesName() == null) {
              appUser.setBluePagesName((String) result[4]);
            }
            if (!StringUtils.isEmpty((String) result[5])) {
              appUser.setDefaultLineOfBusn((String) result[5]);
            }
            if (!StringUtils.isEmpty((String) result[6])) {
              appUser.setDefaultRequestRsn((String) result[6]);
            }
            if (!StringUtils.isEmpty((String) result[7])) {
              appUser.setDefaultReqType((String) result[7]);
            }
            if (!StringUtils.isEmpty((String) result[8])) {
              appUser.setDefaultNoOfRecords(Integer.parseInt((String) result[8]));
            }
            if (!StringUtils.isEmpty((String) result[9])) {
              appUser.setHasCountries(true);
            }
            appUser.setShowPendingOnly("Y".equals(result[10]));
            appUser.setShowLatestFirst("Y".equals(result[11]));
          }
          if (hasDelegate) {
            appUser.setPreferencesSet(true);
            if (appUser.getBluePagesName() == null) {
              Person p = BluePagesHelper.getPerson(appUser.getIntranetId());
              if (p != null) {
                appUser.setBluePagesName(p.getName());
              }
            }

          } else if (!hasRecord) {
            // create the default record
            String name = createUserPrefRecord(entityManager, appUser.getIntranetId(), appUser.getEmpName(), appUser.getCountryCode(), notesEmail);
            appUser.setBluePagesName(name);
          }

          appUser.setAdmin(inAdminGroup);
          appUser.setProcessor(inProcessorGroup);
          appUser.setRequestor(inRequestorGroup);
          appUser.setCmde(inCMDEGroup);

          // Set it in the session so that it can be later accessed in UI
          // for display
          request.getSession().setAttribute("displayName", bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_NAME));

          request.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, appUser);

          request.getSession().setAttribute("cmr.last.request.date", new Date());

          request.getSession().setAttribute("logged-in", "true");

          request.getSession().setAttribute("WTMStatus", "P");

          request.getSession().setMaxInactiveInterval(60 * 60 * 3);

          response.addHeader("Set-Cookie", "countryCode=" + appUser.getCountryCode() + "; HttpOnly; Secure; SameSite=Strict");

          if (appUser.isPreferencesSet()) {
            if (loginUser.getR() > 0) {
              mv = new ModelAndView("redirect:/request/" + loginUser.getR(), "appUser", appUser);
            } else if (!StringUtils.isBlank(loginUser.getC())) {
              String c = loginUser.getC();
              String decoded = new String(Base64.getDecoder().decode(c));
              String params = decoded.substring(2);
              if (decoded.startsWith("f")) {
                mv = new ModelAndView("redirect:/findcmr?" + params, "appUser", appUser);
              } else if (decoded.startsWith("r")) {
                mv = new ModelAndView("redirect:/request?" + params, "appUser", appUser);
              } else {
                mv = new ModelAndView("redirect:/home", "appUser", appUser);
              }
            } else if (appUser.isApprover()) {
              mv = new ModelAndView("redirect:/myappr", "approval", new MyApprovalsModel());
            } else {
              mv = new ModelAndView("redirect:/home", "appUser", appUser);
            }
            // setPageKeys("HOME", "OVERVIEW", mv);
          } else {
            UserPrefModel pref = new UserPrefModel();
            pref.setRequesterId(appUser.getIntranetId());
            mv = new ModelAndView("redirect:/preferences", "pref", pref);
            // setPageKeys("PREFERENCE", "PREF_SUB", mv);
          }

          SystemParameters.logUserAccess("CreateCMR", appUser.getIntranetId());
          AuthCodeRetriever authCode = new AuthCodeRetriever(loginUser.getUsername(), request.getSession());
          Thread authThread = new Thread(authCode);
          authThread.start();

        } catch (Exception e) {
          LOG.error("Error in retrieving Preference settings.", e);
          if (e instanceof CmrException) {
            throw e;
          }
          throw new CmrException(MessageUtil.ERROR_GENERAL);
        } finally {
          entityManager.clear();
          entityManager.close();
        }

      }

    } catch (Exception ex) {
      mv = new ModelAndView("redirect:/login", "loginUser", loginUser);
      setError(ex, mv);
    }

    return mv;
  }

  private String createUserPrefRecord(EntityManager entityManager, String intranetId, String name, String cntryCode, String notesId)
      throws Exception {
    EntityTransaction tx = entityManager.getTransaction();
    tx.begin();
    try {
      LOG.debug("Creating user preferences for " + name + " (" + intranetId + ")");
      UserPref pref = new UserPref();
      UserPrefPK pk = new UserPrefPK();
      pk.setRequesterId(intranetId);
      pref.setId(pk);

      pref.setReceiveMailInd(CmrConstants.YES_NO.Y.toString());
      Person p = BluePagesHelper.getPerson(intranetId, notesId);
      if (p == null || StringUtils.isEmpty(p.getName())) {
        pref.setRequesterNm(name);
      } else {
        pref.setRequesterNm(p.getName());
      }
      LOG.debug("Pref being added: " + pref.getId().getRequesterId() + " (" + pref.getRequesterNm() + ")");
      pref.setDftIssuingCntry(getMappedCountryCode(cntryCode));

      entityManager.persist(pref);
      entityManager.flush();
      tx.commit();

      return pref.getRequesterNm();
    } catch (Exception e) {
      LOG.error("Error in creating User Preference record", e);
      tx.rollback();
      throw e;
    }
  }

  // for exceptions on the country code
  private String getMappedCountryCode(String countryCode) {
    if ("PH1".equals(countryCode)) {
      return "818";
    }
    return countryCode;
  }

  class AuthCodeRetriever implements Runnable {

    private String username;
    private HttpSession session;

    public AuthCodeRetriever(String username, HttpSession session) {
      this.username = username;
      this.session = session;
    }

    @Override
    public void run() {
      try {
        LOG.debug("Starting Authorization Code retrieval from Find CMR [" + SystemConfiguration.getValue("FIND_CMR_URL") + "]");
        StringBuilder sb = new StringBuilder();
        URL url = new URL(SystemConfiguration.getValue("FIND_CMR_URL") + "/authorize?username=" + this.username + "&appName=RequestCMR");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        InputStream is = conn.getInputStream();
        try {
          InputStreamReader isr = new InputStreamReader(is, "UTF-8");
          try {
            BufferedReader br = new BufferedReader(isr);
            try {
              String line = null;
              while ((line = br.readLine()) != null) {
                sb.append(line);
              }
            } finally {
              br.close();
            }
          } finally {
            isr.close();
          }
        } finally {
          is.close();
        }
        LOG.debug("Returned " + sb.toString());
        AppUser user = (AppUser) this.session.getAttribute(CmrConstants.SESSION_APPUSER_KEY);
        if (user != null) {
          user.setAuthCode(sb.toString());
        }
        conn.disconnect();
      } catch (Exception e) {
        LOG.error("Error in retrieving authorization code " + e.getMessage());
      }
    }

  }

  @RequestMapping(
      value = "/log",
      method = RequestMethod.GET)
  public ModelAndView showLog(HttpServletRequest request) {
    return new ModelAndView("log", "login", new LogInUserModel());
  }

  @RequestMapping(
      value = "/checkLoginStatus",
      method = RequestMethod.POST)
  public ModelMap checkLoginStatus(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
    AppUser user = AppUser.getUser(request);
    ModelMap map = new ModelMap();
    map.addAttribute("loginStatus", user != null ? "Y" : "N");
    return map;
  }

  private AuthorizationResponse authenticateViaService(String user) throws Exception {
    try {
      String url = SystemConfiguration.getValue("CMR_SERVICES_URL", "");
      if ("".equals(url)) {
        AuthorizationResponse resp = new AuthorizationResponse();
        Authorization det = new Authorization();
        resp.setAuthorized(false);
        resp.setAuthorization(det);
        return resp;
      }

      AuthorizationClient auth = CmrServicesFactory.getInstance().createClient(url, AuthorizationClient.class);
      AuthorizationRequest request = new AuthorizationRequest();
      request.setApplicationCode(ApplicationCode.CreateCMR);
      request.setUserId(user);

      AuthorizationResponse response = auth.executeAndWrap(request, AuthorizationResponse.class);
      return response;
    } catch (Exception e) {
      LOG.error("Error in connecting to the Authorization Service: " + e.getMessage());
      throw new CmrException(MessageUtil.ERROR_CANNOT_AUTHENTICATE);
    }
  }

  /**
   * Checks if the user is currently an approver
   * 
   * @param entityManager
   * @param intranetId
   * @return
   */
  private boolean isApprover(EntityManager entityManager, String intranetId) {
    String sql = ExternalizedQuery.getSql("APPROVAL.CHECK_APPROVER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", intranetId.toLowerCase());
    query.setForReadOnly(true);
    return query.exists();
  }

  @RequestMapping(
      method = RequestMethod.GET,
      value = "/oidcclient/redirect/createcmr")
  public ModelAndView handleOIDCRedirect(HttpServletRequest request, HttpServletResponse response) {

    ModelAndView mv = null;
    AppUser appUser = AppUser.getUser(request);
    LogInUserModel loginUser = (LogInUserModel) request.getSession().getAttribute("loggedInUserModel");

    if (appUser.isPreferencesSet()) {
      if (loginUser.getR() > 0) {
        mv = new ModelAndView("redirect:/request/" + loginUser.getR(), "appUser", appUser);
      } else if (!StringUtils.isBlank(loginUser.getC())) {
        String c = loginUser.getC();
        String decoded = new String(Base64.getDecoder().decode(c));
        String params = decoded.substring(2);
        if (decoded.startsWith("f")) {
          mv = new ModelAndView("redirect:/findcmr?" + params, "appUser", appUser);
        } else if (decoded.startsWith("r")) {
          mv = new ModelAndView("redirect:/request?" + params, "appUser", appUser);
        } else {
          mv = new ModelAndView("redirect:/home", "appUser", appUser);
        }
      } else if (appUser.isApprover()) {
        mv = new ModelAndView("redirect:/myappr", "approval", new MyApprovalsModel());
      } else {
        mv = new ModelAndView("redirect:/home", "appUser", appUser);
      }
      // setPageKeys("HOME", "OVERVIEW", mv);
    } else {
      UserPrefModel pref = new UserPrefModel();
      pref.setRequesterId(appUser.getIntranetId());
      mv = new ModelAndView("redirect:/preferences", "pref", pref);
      // setPageKeys("PREFERENCE", "PREF_SUB", mv);
    }

    return mv;
  }

}
