/**
 * 
 */
package com.ibm.cio.cmr.request.controller.approval;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.approval.ApprovalCommentModel;
import com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel;
import com.ibm.cio.cmr.request.model.requestentry.AttachmentModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.approval.ApprovalService;
import com.ibm.cio.cmr.request.service.user.UserService;
import com.ibm.cio.cmr.request.ui.UIMgr;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.oauth.OAuthUtils;
import com.ibm.cio.cmr.request.util.oauth.UserHelper;
import com.ibm.cmr.services.client.auth.Authorization;

/**
 * Approvals pages controller
 * 
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class ApprovalController extends BaseController {

  private static final Logger LOG = Logger.getLogger(ApprovalController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private ApprovalService approvalService;

  private static final String AUTHORIZATION_HEADER = "Authorization";

  public static enum ApprovalType {
    A, C, R
  }

  @RequestMapping(value = "/approval")
  public ModelAndView showErrorPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
    return null;
  }

  @RequestMapping(value = "/approval/{approvalCode1}")
  public ModelAndView showApprovalPage(@PathVariable("approvalCode1") String approvalCode, HttpServletRequest request, HttpServletResponse response,
      ApprovalResponseModel modelFromRequest) throws Exception {
    boolean processing = CmrConstants.YES_NO.Y.toString().equals(request.getParameter("processing"));
    String view = "approve";
    String title = UIMgr.getText("title.approval");
    ApprovalResponseModel approval = new ApprovalResponseModel();

    // connect to W3 to build user profile
    try {
      approval = modelFromRequest;

      if (!processing) {
        if (OAuthUtils.isSSOActivated()) {
          Optional<ApprovalResponseModel> optionalAppr = authorizeFromSSO(request, response, approvalCode, approval);
          if (optionalAppr.isPresent()) {
            approval = optionalAppr.get();
          }
        } else {
          Optional<ApprovalResponseModel> optionalAppr = authorizeFromLDAP(request, response, approvalCode, approval);
          if (optionalAppr.isPresent()) {
            approval = optionalAppr.get();
          }
        }
      }

      if (approval == null) {
        view = "approvenotauth";
        title = UIMgr.getText("title.unauthorized");
      } else {
        String actualType = approval.getType();
        // check first the current status of the approval if still valid
        approval.setType("L");
        approvalService.processTransaction(approval, request);

        System.out.println("Req ID  " + approval.getReqId());
        createAppUser(approval, request);
        if (!approval.isProcessed()) {
          // show status changed if status is now invalid
          view = "approvechanged";
          title = UIMgr.getText("title.statuschanged");
        } else {
          // status is valid, process each approval type
          approval.setType(actualType);
          if (ApprovalType.R.toString().equals(approval.getType())) {
            view = "reject";
            title = UIMgr.getText("title.rejection");
            if (processing) {
              approvalService.processTransaction(approval, request);
            }
          } else if (ApprovalType.A.toString().equals(approval.getType())) {
            view = "approve";
            title = UIMgr.getText("title.approval");
            if (processing) {
              approvalService.processTransaction(approval, request);
            }
          } else if (ApprovalType.C.toString().equals(approval.getType())) {
            view = "condapprove";
            title = UIMgr.getText("title.conditional");
            if (processing) {
              approvalService.processTransaction(approval, request);
            }
          } else {
            view = "approvenotauth";
            title = UIMgr.getText("title.unauthorized");
          }
        }
      }
      if (approval != null) {
        approval.setApprovalCode(approvalCode);
      }
    } catch (Exception e) {
      LOG.error("Error when processing approvals..", e);
      view = "approveerror";
      title = UIMgr.getText("title.approveerror");
    }
    ModelAndView mv = new ModelAndView(view, "approval", approval);
    mv.addObject("approvalTitle", title);
    mv.addObject("attach", new AttachmentModel());
    return mv;
  }

  public Optional<ApprovalResponseModel> authorizeFromSSO(HttpServletRequest request, HttpServletResponse response, String approvalCode,
      ApprovalResponseModel approval) throws NoSuchAlgorithmException {

    UserHelper userHelper = new UserHelper();
    String ibmUniqueId = userHelper.getUNID();

    if (ibmUniqueId == null || ibmUniqueId.trim().isEmpty()) {
      tagUnauthorized(response);
    } else {
      LOG.debug("Approval Code: " + approvalCode);
      String userId = userHelper.getRegistrationId();
      approval = decodeUrlParam(approvalCode);
      if (approval != null && approval.getApproverId() != null && !approval.getApproverId().toUpperCase().equals(userId.toUpperCase())) {
        approval = null;
      } else {
        LOG.debug("Approval Details: " + approval.getApproverId() + " | " + approval.getApprovalId() + " | " + approval.getType());
      }
      return Optional.of(approval);
    }

    return Optional.empty();

  }

  private Optional<ApprovalResponseModel> authorizeFromLDAP(HttpServletRequest request, HttpServletResponse response, String approvalCode,
      ApprovalResponseModel approval) throws NoSuchAlgorithmException, Exception {

    if (!authorize(request)) {
      tagUnauthorized(response);
    } else {
      LOG.debug("Approval Code: " + approvalCode);
      String user = getUserIdFromAuth(request);
      approval = decodeUrlParam(approvalCode);
      if (approval != null && approval.getApproverId() != null && !approval.getApproverId().toUpperCase().equals(user.toUpperCase())) {
        approval = null;
      } else {
        LOG.debug("Approval Details: " + approval.getApproverId() + " | " + approval.getApprovalId() + " | " + approval.getType());
      }
      return Optional.of(approval);
    }

    return Optional.empty();
  }

  private void createAppUser(ApprovalResponseModel approval, HttpServletRequest request) throws CmrException {

    AppUser user = AppUser.getUser(request);
    if (user == null) {
      user.setIntranetId(approval.getApproverId().toLowerCase());
      Person person = BluePagesHelper.getPerson(approval.getApproverId());
      if (person != null) {
        user.setBluePagesName(person.getName());
        user.setNotesEmailId(person.getNotesEmail());
      } else {
        user.setBluePagesName(approval.getApproverId().toLowerCase());
        user.setNotesEmailId(approval.getApproverId().toLowerCase());
      }
      user.setApprover(true);
      user.setAuth(new Authorization());
      request.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, user);
      request.getSession().setAttribute("displayName", user.getBluePagesName());
    }
  }

  /**
   * Checks HTTP Basic Authentication on the request
   * 
   * @param request
   * @return
   * @throws Exception
   */
  protected boolean authorize(HttpServletRequest request) throws Exception {

    String authString = request.getHeader(AUTHORIZATION_HEADER);
    if (authString == null) {
      return false;
    }
    String[] authParts = authString.split("\\s+");
    String authInfo = authParts[1];
    // Decode the data back to original string
    byte[] bytes = null;
    bytes = Base64.getDecoder().decode(authInfo);
    String decodedAuth = new String(bytes);
    String[] credentials = decodedAuth.split(":");
    if (credentials.length != 2) {
      return false;
    }
    try {
      boolean authenticated = userService.authenticateUser(credentials[0], credentials[1]);
      return authenticated;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Extracts the User ID from the authorized HTTP Authentication
   * 
   * @param request
   * @return
   * @throws Exception
   */
  protected String getUserIdFromAuth(HttpServletRequest request) throws Exception {

    String authString = request.getHeader(AUTHORIZATION_HEADER);
    if (authString == null) {
      return "";
    }
    String[] authParts = authString.split("\\s+");
    String authInfo = authParts[1];
    // Decode the data back to original string
    byte[] bytes = null;
    bytes = Base64.getDecoder().decode(authInfo);
    String decodedAuth = new String(bytes);
    String[] credentials = decodedAuth.split(":");
    if (credentials.length != 2) {
      return "";
    }
    return credentials[0];
  }

  /**
   * Tags the response with HTTP 404 - Unauthorized to force basic http
   * authentication
   * 
   * @param response
   */
  protected void tagUnauthorized(HttpServletResponse response) {
    response.setStatus(HttpStatus.SC_UNAUTHORIZED);
    response.addHeader("WWW-Authenticate", "Basic realm=\"IBM Intranet\"");
  }

  /**
   * Encodes the parameters into a form that is not easily decipherable
   * 
   * @param approvalId
   * @param type
   * @param user
   * @return
   * @throws NoSuchAlgorithmException
   */
  public static String encodeUrlParam(long approvalId, ApprovalType type, String user) throws NoSuchAlgorithmException {
    String param = "";
    switch (type) {
    case A:
      param = "t=A&u=" + user + "&a=" + approvalId;
      break;
    case C:
      param = "u=" + user + "&t=C&a=" + approvalId;
      break;
    case R:
      param = "a=" + approvalId + "&u=" + user + "&t=R";
      break;
    }

    try {
      String l1 = Base64.getEncoder().encodeToString(param.getBytes());
      SecureRandom random = new SecureRandom();
      String salt = new BigInteger(130, random).toString(32);
      // salt the base 64 level 1
      l1 += salt.substring(0, 8);
      String l2 = Base64.getEncoder().encodeToString(l1.getBytes());
      // salt the base 64 level 2 for it not to be decoded
      l2 = salt.substring(16, 19).toUpperCase() + l2 + salt.substring(10, 15).toUpperCase();
      return l2;
    } catch (Throwable e) {
      LOG.warn("Error in generating URL", e);
      return "XXX";
    }
  }

  /**
   * Decodes the parameter into the format accepted by the system
   * 
   * @param param
   * @return
   * @throws NoSuchAlgorithmException
   */
  public static ApprovalResponseModel decodeUrlParam(String param) throws NoSuchAlgorithmException {
    if (param == null || param.length() < 10) {
      return null;
    }
    try {
      // first extract l2
      String l2 = param.substring(3, param.length() - 5);
      String l1 = new String(Base64.getDecoder().decode(l2.getBytes()));
      l1 = l1.substring(0, l1.length() - 8);
      String decoded = new String(Base64.getDecoder().decode(l1.getBytes()));
      String[] params = decoded.split("&");
      String[] valuePair = null;
      ApprovalResponseModel model = new ApprovalResponseModel();
      for (String paramPair : params) {
        valuePair = paramPair.split("=");
        if ("a".equals(valuePair[0])) {
          model.setApprovalId(Long.parseLong(valuePair[1]));
        }
        if ("u".equals(valuePair[0])) {
          model.setApproverId(valuePair[1]);
        }
        if ("t".equals(valuePair[0])) {
          model.setType(valuePair[1]);
        }
      }
      return model;
    } catch (Exception e) {
      return null;
    }
  }

  @RequestMapping(value = "/approval/list", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getApprovalList(HttpServletRequest request, HttpServletResponse response, ApprovalResponseModel model) throws CmrException {

    if (StringUtils.isNotEmpty(request.getParameter("reqId"))) {
      model.setReqId(Long.parseLong(request.getParameter("reqId")));
    } else {
      // return empty list if no ID
      List<ApprovalResponseModel> results = new ArrayList<ApprovalResponseModel>();
      ModelMap map = new ModelMap();
      map.addAttribute("items", results);
      return map;
    }

    List<ApprovalResponseModel> results = approvalService.search(model, request);
    return wrapAsSearchResult(results);
  }

  @RequestMapping(value = "/approval/process", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processApprovals(HttpServletRequest request, HttpServletResponse response, ApprovalResponseModel model) throws CmrException {

    ProcessResultModel result = new ProcessResultModel();
    try {
      approvalService.processTransaction(model, request);

      String action = model.getAction();

      if ("ADD_APPROVAL".equals(action)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_APPROVAL_ADD_LIST));
      }
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }

    return wrapAsProcessResult(result);
  }

  /**
   * Retrieves the comments
   * 
   * @param request
   * @param response
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(value = "/approval/comments", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getApprovalComments(HttpServletRequest request, HttpServletResponse response) throws CmrException {

    ApprovalResponseModel model = new ApprovalResponseModel();
    String reqId = request.getParameter("reqId");
    String approvalId = request.getParameter("approvalId");

    LOG.debug("Getting comments for Request ID " + reqId + " Approval ID: " + approvalId);

    if (StringUtils.isBlank(reqId) || StringUtils.isBlank(approvalId)) {
      // return empty list if no ID
      List<ApprovalCommentModel> results = new ArrayList<ApprovalCommentModel>();
      ModelMap map = new ModelMap();
      map.addAttribute("items", results);
      return map;
    } else {
      model.setReqId(Long.parseLong(reqId));
      model.setApprovalId(Long.parseLong(approvalId));
    }

    model.setAction("GET_COMMENTS");
    approvalService.processTransaction(model, request);
    ModelMap map = new ModelMap();

    LOG.debug(model.getCommentList() + " comments retrieved.");
    map.addAttribute("items", model.getCommentList());
    return map;
  }

  @RequestMapping(value = "/approval/action", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap processActions(HttpServletRequest request, HttpServletResponse response, ApprovalResponseModel model) throws CmrException {

    ProcessResultModel result = new ProcessResultModel();
    try {
      approvalService.processTransaction(model, request);

      result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_APPROVALSAVED_SUCCESSFULLY));

      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage(e.getMessage());
    }

    return wrapAsProcessResult(result);
  }

  @RequestMapping(value = "/approval/status", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap getApprovalStatus(HttpServletRequest request, HttpServletResponse response) throws CmrException {
    ModelMap map = new ModelMap();
    String reqIdStr = request.getParameter("reqId");
    if (!StringUtils.isBlank(reqIdStr)) {
      RequestEntryModel model = new RequestEntryModel();
      model.setReqId(Long.parseLong(reqIdStr));
      String status = approvalService.setApprovalResult(model);
      map.put("status", status);
      map.put("maxTs", model.getApprovalMaxTs());
    } else {
      map.put("status", CmrConstants.APPROVAL_RESULT_NONE);
      map.put("maxTs", "");
    }
    return map;
  }

}
