/**
 * 
 */
package com.ibm.cio.cmr.request.controller;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.ProcessResultModel;

/**
 * Base Controller class where all {@link Controller} classes should extend
 * 
 * @author Jeffrey Zamora
 * 
 */
public class BaseController {

  public static final String MODEL_KEY = "pageModel";
  private static final String AUTHORIZATION_HEADER = "Authorization";

  /**
   * Assigns the error properly on the model, for display on the page
   * 
   * @param exception
   * @param modelMap
   */
  protected void setError(Exception exception, ModelMap modelMap) {
    modelMap.addAttribute("errorMessage", exception.getMessage());
  }

  /**
   * Assigns the error properly on the mv, for display on the page
   * 
   * @param exception
   * @param mv
   */
  protected void setError(Exception exception, ModelAndView mv) {
    mv.addObject("errorMessage", exception.getMessage());
  }

  /**
   * Sets the keys for the tab highlights.
   * 
   * @param primarytabId
   *          - should be found in tabs.jsp as <id>_TAB
   * @param secondaryTabId
   *          - should be found in tabs.jsp as <id>_TAB
   * @param modelMap
   */
  protected void setPageKeys(String primarytabId, String secondaryTabId, ModelMap modelMap) {
    modelMap.addAttribute("primaryTabId", primarytabId);
    modelMap.addAttribute("secondaryTabId", secondaryTabId);
  }

  /**
   * Sets the keys for the tab highlights.
   * 
   * @param primarytabId
   *          - should be found in tabs.jsp as <id>_TAB
   * @param secondaryTabId
   *          - should be found in tabs.jsp as <id>_TAB
   * @param modelMap
   */
  protected void setPageKeys(String primarytabId, String secondaryTabId, ModelAndView mv) {
    mv.addObject("primaryTabId", primarytabId);
    mv.addObject("secondaryTabId", secondaryTabId);
  }

  /**
   * Checks if the model's properties show that processing should be done. It
   * checks both the model's state and action
   * 
   * @param model
   * @return
   */
  protected boolean shouldProcess(BaseModel model) {
    // no model, no process
    if (model == null) {
      return false;
    }
    // if insert, try to process
    if (BaseModel.STATE_NEW == model.getState() && BaseModel.ACT_INSERT.equals(model.getAction())) {
      return true;
    }
    // if update or delete, try to process
    if (BaseModel.STATE_EXISTING == model.getState()
        && (BaseModel.ACT_UPDATE.equals(model.getAction()) || BaseModel.ACT_DELETE.equals(model.getAction()))) {
      return true;
    }

    // if mass action, try to process
    if (BaseModel.STATE_EXISTING == model.getState() && BaseModel.ACT_PROCESS_SELECTED.equals(model.getAction())) {
      return true;
    }

    // no correct state - action combination, no process
    return false;

  }

  /**
   * Checks if the state of the model is for mass action
   * 
   * @param model
   * @return
   */
  protected boolean isMassProcess(BaseModel model) {
    if (BaseModel.STATE_EXISTING == model.getState() && BaseModel.ACT_PROCESS_SELECTED.equals(model.getAction())) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the state of the model is for update
   * 
   * @param model
   * @return
   */
  protected boolean isUpdating(BaseModel model) {
    if (BaseModel.STATE_EXISTING == model.getState() && BaseModel.ACT_UPDATE.equals(model.getAction())) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the state of the model is for delete
   * 
   * @param model
   * @return
   */
  protected boolean isDeleting(BaseModel model) {
    if (BaseModel.STATE_EXISTING == model.getState() && BaseModel.ACT_DELETE.equals(model.getAction())) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the state of the model is for insert
   * 
   * @param model
   * @return
   */
  protected boolean isInserting(BaseModel model) {
    if (BaseModel.STATE_NEW == model.getState() && BaseModel.ACT_INSERT.equals(model.getAction())) {
      return true;
    }
    return false;
  }

  /**
   * Wraps the results as a search result modelmap
   * 
   * @param list
   * @return
   */
  protected ModelMap wrapAsSearchResult(List<? extends BaseModel> list) {
    ModelMap map = new ModelMap();
    map.addAttribute("items", list);
    return map;
  }

  protected void authorize(HttpServletRequest request, HttpServletResponse response, String user, String password, String realm) throws Exception {

    boolean auth = true;
    String authString = request.getHeader(AUTHORIZATION_HEADER);
    if (authString == null) {
      auth = false;
    } else {
      String[] authParts = authString.split("\\s+");
      String authInfo = authParts[1];
      // Decode the data back to original string
      byte[] bytes = null;
      bytes = Base64.getDecoder().decode(authInfo);

      String decodedAuth = new String(bytes);
      String[] credentials = decodedAuth.split(":");
      if (credentials.length != 2) {
        auth = false;
      }

      if (!(user.equals(credentials[0]) && password.equals(credentials[1]))) {
        auth = false;
      }
    }
    if (!auth) {
      response.setStatus(HttpStatus.SC_UNAUTHORIZED);
      response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
    }
  }

  /**
   * Wraps the results as a search result modelmap
   * 
   * @param list
   * @return
   */
  protected ModelMap wrapAsPlainSearchResult(List<?> list) {
    ModelMap map = new ModelMap();
    map.addAttribute("items", list);
    return map;
  }

  /**
   * Wraps the results as a search result modelmap
   * 
   * @param list
   * @return
   */
  protected ModelMap wrapAsProcessResult(ProcessResultModel result) {
    ModelMap map = new ModelMap();
    map.addAttribute("result", result);
    return map;
  }

  @InitBinder
  public void binder(WebDataBinder binder) {
    binder.registerCustomEditor(Timestamp.class, new DateTimestampPropertyEditor());
    binder.registerCustomEditor(Date.class, new DateTimestampPropertyEditor());
  }

  private class DateTimestampPropertyEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String value) {
      try {
        SimpleDateFormat parser = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
        Date parsedDate = parser.parse(value);
        setValue(new Timestamp(parsedDate.getTime()));
      } catch (ParseException e) {
        setValue(null);
      }
    }

    @Override
    public String getAsText() {
      try {
        Object val = getValue();
        SimpleDateFormat parser = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
        return parser.format(val);
      } catch (Exception e) {
        return super.getAsText();
      }
    }
  }

}
