/**
 * 
 */
package com.ibm.cio.cmr.request.controller.automation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.util.AutomationElementContainer;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.automation.AutoConfigCntryModel;
import com.ibm.cio.cmr.request.model.automation.AutoConfigElemModel;
import com.ibm.cio.cmr.request.model.automation.AutoConfigMapModel;
import com.ibm.cio.cmr.request.model.automation.AutoConfigModel;
import com.ibm.cio.cmr.request.model.automation.AutoExceptionEntryModel;
import com.ibm.cio.cmr.request.service.automation.AutoConfigService;

/**
 * Controller class for maintaining automation engine configurations
 * 
 * @author JeffZAMORA
 * 
 */
@Controller
public class AutoConfigController extends BaseController {

  private static final Logger LOG = Logger.getLogger(AutoConfigController.class);

  @Autowired
  private AutoConfigService service;

  @RequestMapping(
      value = "/auto/config/base")
  public ModelAndView showBasePage(HttpServletRequest request, HttpServletResponse response) throws Exception {
    LOG.debug("Displaying automation config base page");
    return new ModelAndView("auto_config_base");
  }

  @RequestMapping(
      value = "/auto/config/list")
  public ModelAndView showConfigList(HttpServletRequest request, HttpServletResponse response) throws Exception {
    LOG.debug("Displaying automation config listing");
    return new ModelAndView("auto_config_list");
  }

  @RequestMapping(
      value = "/auto/config/maint")
  public ModelAndView showMaintenancePage(HttpServletRequest request, HttpServletResponse response) throws Exception {
    LOG.debug("Displaying automation config maintenance page");
    return new ModelAndView("auto_config");
  }

  @RequestMapping(
      value = "/auto/config/cntry")
  public ModelAndView showCountryConfigPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
    LOG.debug("Displaying automation config base page");
    return new ModelAndView("auto_config_cntry");
  }

  @RequestMapping(
      value = "/auto/config/exceptions")
  public ModelAndView showExceptionsPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
    LOG.debug("Displaying automation exceptions page");
    return new ModelAndView("auto_config_excep");
  }
  // services

  /**
   * Retrieves the list of configurations defined
   * 
   * @param request
   * @param response
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/auto/config/getlist")
  @SuppressWarnings("unchecked")
  public ModelMap getConfigList(HttpServletRequest request, HttpServletResponse response) throws CmrException {
    LOG.debug("Getting automation engine configuration list..");
    ParamContainer params = new ParamContainer();
    params.addParam("action", AutoConfigService.ACTION_GET_CONFIGS);
    Map<String, Object> out = service.process(request, params);
    List<AutoConfigModel> list = (List<AutoConfigModel>) out.get(AutoConfigService.OUT_CONFIG_LIST);
    if (list == null) {
      list = new ArrayList<AutoConfigModel>();
    }
    return wrapAsPlainSearchResult(list);
  }

  /**
   * Retrieves all defined {@link AutomationElement} objects under
   * {@link AutomationElementRegistry}
   * 
   * @param request
   * @param response
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/auto/config/getelems")
  @SuppressWarnings("unchecked")
  public ModelMap getElemList(HttpServletRequest request, HttpServletResponse response) throws CmrException {
    LOG.debug("Getting automation elements list..");
    ParamContainer params = new ParamContainer();
    params.addParam("action", AutoConfigService.ACTION_GET_ELEMENTS);
    Map<String, Object> out = service.process(request, params);
    List<AutomationElementContainer> list = (List<AutomationElementContainer>) out.get(AutoConfigService.OUT_ELEMENT_LIST);
    if (list == null) {
      list = new ArrayList<AutomationElementContainer>();
    }
    return wrapAsPlainSearchResult(list);
  }

  /**
   * Saves the description of the configuration
   * 
   * @param request
   * @param response
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/auto/config/savedefn")
  public ModelMap saveDefinition(HttpServletRequest request, HttpServletResponse response) throws CmrException {
    ParamContainer params = new ParamContainer();
    LOG.debug("Saving configuration definition..");
    params.addParam("action", AutoConfigService.ACTION_SAVE_CONFIG_DEFN);
    Map<String, Object> out = service.process(request, params);
    ProcessResultModel result = (ProcessResultModel) out.get(AutoConfigService.OUT_PROCESS_RESULT);
    return wrapAsProcessResult(result);
  }

  /**
   * Deletes a configuration
   * 
   * @param request
   * @param response
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/auto/config/deleteconfig")
  public ModelMap deleteConfiguration(HttpServletRequest request, HttpServletResponse response) throws CmrException {
    ParamContainer params = new ParamContainer();
    LOG.debug("Deleting configuration..");
    params.addParam("action", AutoConfigService.ACTION_DELETE_CONFIG);
    Map<String, Object> out = service.process(request, params);
    ProcessResultModel result = (ProcessResultModel) out.get(AutoConfigService.OUT_PROCESS_RESULT);
    return wrapAsProcessResult(result);
  }

  /**
   * Saves the element configurations
   * 
   * @param model
   * @param request
   * @param response
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/auto/config/saveelems")
  public ModelMap saveConfiguration(@RequestBody AutoConfigElemModel model, HttpServletRequest request, HttpServletResponse response)
      throws CmrException {
    ParamContainer params = new ParamContainer();
    LOG.debug("Saving automation element configurations..");
    params.addParam("action", AutoConfigService.ACTION_SAVE_CONFIG_ELEMENTS);
    params.addParam("model", model);
    Map<String, Object> out = service.process(request, params);
    ProcessResultModel result = (ProcessResultModel) out.get(AutoConfigService.OUT_PROCESS_RESULT);
    return wrapAsProcessResult(result);
  }

  /**
   * Performs a generic save and calls the action on the service
   * 
   * @param model
   * @param request
   * @param response
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/auto/config/savemap")
  public ModelMap saveMapping(@RequestBody AutoConfigMapModel model, HttpServletRequest request, HttpServletResponse response) throws CmrException {
    ParamContainer params = new ParamContainer();
    LOG.debug("Mapping countries to automation configuration..");
    params.addParam("action", AutoConfigService.ACTION_MAP_COUNTRIES);
    params.addParam("model", model);
    Map<String, Object> out = service.process(request, params);
    ProcessResultModel result = (ProcessResultModel) out.get(AutoConfigService.OUT_PROCESS_RESULT);
    return wrapAsProcessResult(result);
  }

  /**
   * Performs a generic save and calls the action on the service
   * 
   * @param model
   * @param request
   * @param response
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/auto/config/savecntry")
  public ModelMap saveCountry(@RequestBody AutoConfigCntryModel model, HttpServletRequest request, HttpServletResponse response) throws CmrException {
    ParamContainer params = new ParamContainer();
    LOG.debug("Saving country counfigurations..");
    params.addParam("action", AutoConfigService.ACTION_SAVE_COUNTRIES);
    params.addParam("model", model);
    Map<String, Object> out = service.process(request, params);
    ProcessResultModel result = (ProcessResultModel) out.get(AutoConfigService.OUT_PROCESS_RESULT);
    return wrapAsProcessResult(result);
  }

  /**
   * Performs a generic save and calls the action on the service
   * 
   * @param model
   * @param request
   * @param response
   * @return
   * @throws CmrException
   */
  @RequestMapping(
      value = "/auto/config/saveexception")
  public ModelMap saveException(@RequestBody AutoExceptionEntryModel model, HttpServletRequest request, HttpServletResponse response)
      throws CmrException {
    ParamContainer params = new ParamContainer();
    LOG.debug("Saving exception");
    params.addParam("action", AutoConfigService.ACTION_SAVE_EXCEPTION);
    params.addParam("model", model);
    Map<String, Object> out = service.process(request, params);
    ProcessResultModel result = (ProcessResultModel) out.get(AutoConfigService.OUT_PROCESS_RESULT);
    return wrapAsProcessResult(result);
  }

}
