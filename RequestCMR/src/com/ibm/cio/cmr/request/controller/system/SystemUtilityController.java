/**
 * 
 */
package com.ibm.cio.cmr.request.controller.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.util.AutomationElementContainer;
import com.ibm.cio.cmr.request.config.ConfigItem;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.controller.DropdownListController;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.ExternalProcessService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.ui.UIMgr;
import com.ibm.cio.cmr.request.ui.template.TemplateManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.validator.BlueGroupValidator;
import com.ibm.cio.cmr.request.util.validator.BooleanValidator;
import com.ibm.cio.cmr.request.util.validator.DateFormatValidator;
import com.ibm.cio.cmr.request.util.validator.IntegerValidator;
import com.ibm.cio.cmr.request.util.validator.NumberValidator;
import com.ibm.cio.cmr.request.util.validator.ParamValidator;
import com.ibm.cio.cmr.request.util.validator.TimeValidator;
import com.ibm.cio.cmr.request.util.validator.TimezoneValidator;

/**
 * Handles the system administration pages
 * 
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class SystemUtilityController extends BaseController {

  private static final Logger LOG = Logger.getLogger(SystemUtilityController.class);

  private static final String ERROR_MSG = "You do not have authority to access this function. Make sure you were given access to this facility by the Administrator.";
  private static Map<String, ParamValidator> validators = new HashMap<String, ParamValidator>();

  static {
    validators.put("integer", new IntegerValidator());
    validators.put("number", new NumberValidator());
    validators.put("boolean", new BooleanValidator());
    validators.put("time", new TimeValidator());
    validators.put("timezone", new TimezoneValidator());
    validators.put("bluegroup", new BlueGroupValidator());
    validators.put("dateformat", new DateFormatValidator());
  }

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/systemRefresh",
      method = RequestMethod.GET)
  public @ResponseBody ModelAndView refresh(HttpServletRequest request, ModelMap model) {
    ModelMap map = new ModelMap();
    Map<String, String> status = new HashMap<String, String>();

    AppUser user = AppUser.getUser(request);

    if (user == null) {
      map.put("ERROR", ERROR_MSG);
    } else {
      if (!user.isAdmin()) {
        map.put("ERROR", ERROR_MSG);
      } else {

        String rid = request.getParameter("rid");
        if (rid == null) {
          ModelAndView mv = new ModelAndView("systemRefresh", "status", map);
          setPageKeys("ADMIN", "SYS_REFRESH", mv);
          return mv;
        }

        if ("query".equalsIgnoreCase(rid) || "all".equalsIgnoreCase(rid)) {
          // all that need to be refreshed, place here
          try {
            ExternalizedQuery.refresh();
            status.put("query", "Y");
          } catch (Exception e) {
            status.put("query", "N");
          }
        }

        if ("msg".equalsIgnoreCase(rid) || "all".equalsIgnoreCase(rid)) {
          // all that need to be refreshed, place here
          try {
            MessageUtil.refresh();
            status.put("msg", "Y");
          } catch (Exception e) {
            status.put("msg", "N");
          }
        }

        if ("ui".equalsIgnoreCase(rid) || "all".equalsIgnoreCase(rid)) {
          // all that need to be refreshed, place here
          try {
            UIMgr.refresh();
            status.put("ui", "Y");
          } catch (Exception e) {
            status.put("ui", "N");
          }
        }

        if ("syspar".equalsIgnoreCase(rid) || "all".equalsIgnoreCase(rid)) {
          // all that need to be refreshed, place here
          try {
            SystemConfiguration.refresh();
            status.put("syspar", "Y");
            // TimeZone.setDefault(TimeZone.getTimeZone(SystemConfiguration.getValue("DATE_TIMEZONE")));
          } catch (Exception e) {
            status.put("syspar", "N");
          }
        }

        if ("cache".equalsIgnoreCase(rid) || "all".equalsIgnoreCase(rid)) {
          // all that need to be refreshed, place here
          try {
            updateRefreshTime();

            DropdownListController.refresh();
            RequestUtils.refresh();
            PageManager.init();
            TemplateManager.refresh();
            SystemParameters.refresh();
            ExternalProcessService.refresh();
            AutomationElementContainer.refresh();
            status.put("cache", "Y");
          } catch (Exception e) {
            status.put("cache", "N");
          }
        }

        map.put("items", status);
      }
    }
    ModelAndView mv = new ModelAndView("systemRefresh", "map", map);
    setPageKeys("ADMIN", "SYS_REFRESH", mv);
    return mv;
  }

  private void updateRefreshTime() {
    try {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
      EntityManager entityManager = JpaManager.getEntityManager();
      try {

        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();

        String newTs = formatter.format(SystemUtil.getCurrentTimestamp());

        String sql = ExternalizedQuery.getSql("SYSPAR.UPDATE_REFRESH");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("NEW_VALUE", newTs);
        int updated = query.executeSql();
        if (updated == 1) {
          LOG.debug("Last Refresh Updated to " + newTs);
          SystemConfiguration.LAST_REFRESH_TIME = newTs;
        }

        tx.commit();
      } catch (Exception e) {
        if (entityManager != null && entityManager.getTransaction() != null && entityManager.getTransaction().isActive()) {
          entityManager.getTransaction().rollback();
        }
      } finally {
        // empty the manager
        entityManager.clear();
        entityManager.close();
      }
    } catch (Exception e) {
      LOG.error("Error when trying to update refresh time.", e);
    }
  }

  /**
   * Handles the param listing page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/systemParameters",
      method = RequestMethod.GET)
  public ModelAndView viewSystemParameters(HttpServletRequest request, ModelMap model) {
    ModelMap map = new ModelMap();
    AppUser user = AppUser.getUser(request);
    if (user == null) {
      map.put("ERROR", ERROR_MSG);
    } else {
      if (!user.isAdmin()) {
        map.put("ERROR", ERROR_MSG);
      } else {
        Collection<ConfigItem> items = SystemConfiguration.asList();
        map.put("items", items);
      }
    }
    ModelAndView mv = new ModelAndView("systemParams", "map", map);
    setPageKeys("ADMIN", "SYS_CONFIG", mv);
    return mv;

  }

  /**
   * Handles the parameter edit page
   * 
   * @param configId
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(
      value = "/systemParameterEdit",
      method = RequestMethod.GET)
  public ModelAndView editSystemParameters(@RequestParam("configItem") String configId, HttpServletRequest request, ModelMap model) {
    ModelMap map = new ModelMap();
    AppUser user = AppUser.getUser(request);
    if (user == null) {
      map.put("ERROR", ERROR_MSG);
    } else {
      if (!user.isAdmin()) {
        map.put("ERROR", ERROR_MSG);
      } else {
        ConfigItem item = SystemConfiguration.getParameter(configId);
        if (item != null) {
          if ("save".equals(request.getParameter("action"))) {
            String value = request.getParameter("itemvalue");
            map.put("OLDVALUE", value);
            if (item.isRequired() && (value == null || "".equals(value.trim()))) {
              map.put("VALIDATION", "Value cannot be empty.");
            } else {
              ParamValidator validator = validators.get(item.getType());
              boolean valid = validator == null ? true : (!item.isRequired() && StringUtils.isEmpty(value) ? true : validator.validate(value));
              // try to update the value
              if (!valid) {
                String message = value + " is not a valid " + item.getType() + " value.";
                if (validator != null && validator.getErrorMessage() != null) {
                  message = validator.getErrorMessage();
                }
                map.put("VALIDATION", message);
              } else {
                SystemConfiguration.update(configId, value);
                try {
                  SystemConfiguration.export();
                  SystemConfiguration.refresh();
                } catch (Exception e) {
                  map.put("VALIDATION", "An error occured while updating the configuration. Please contact your system administrator");
                }
              }
              map.put("INFO", "Parameter updated successfully.");
            }
          }
        }
        map.put("items", item);
      }
    }
    ModelAndView mv = new ModelAndView("systemParamEdit", "map", map);
    setPageKeys("ADMIN", "SYS_CONFIG_EDIT", mv);
    return mv;
  }

  @RequestMapping(
      value = "/config/process")
  public void configProcess(HttpServletRequest request, HttpServletResponse response, ModelMap model)
      throws FileNotFoundException, IOException, FileUploadException {
    String name = request.getParameter("configdownload");
    response.setContentType("application/octet-stream");
    response.addHeader("Content-Type", "application/octet-steam");
    response.addHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
    SystemConfiguration.download(response.getOutputStream(), name);

  }

  @RequestMapping(
      value = "/config/upload")
  public ModelAndView configUpload(HttpServletRequest request, HttpServletResponse response, ModelMap model)
      throws FileNotFoundException, IOException, FileUploadException {
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    ModelAndView mv = new ModelAndView("redirect:/config");
    if (isMultipart) {
      DiskFileItemFactory factory = new DiskFileItemFactory();
      String tmpDir = "tmp";
      File tmp = new File(tmpDir);
      if (!tmp.exists()) {
        tmp.mkdirs();
      }
      // Set factory constraints
      factory.setSizeThreshold(5000);
      factory.setRepository(tmp);

      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);
      List<FileItem> items = upload.parseRequest(request);

      String name = null;
      for (FileItem item : items) {
        if (item.isFormField()) {
          if ("configuploadname".equals(item.getFieldName())) {
            name = item.getString();
          }
        }
      }
      if (name != null) {
        for (FileItem item : items) {
          if (!item.isFormField()) {
            if ("configupload".equals(item.getFieldName())) {
              FileOutputStream fos = new FileOutputStream(SystemConfiguration.dirLocation.getAbsolutePath() + "/" + name);
              try {
                IOUtils.copy(item.getInputStream(), fos);
              } finally {
                fos.close();
              }
              break;
            }
          }
        }
      }
      mv.addObject("uploadstatus", "Y");
    }
    setPageKeys("ADMIN", "SYS_CONFIG", mv);
    return mv;
  }

  @RequestMapping(
      value = "/config",
      method = RequestMethod.GET)
  public ModelAndView configuration(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
    String user = SystemConfiguration.getSystemProperty("config.user");
    String pass = SystemConfiguration.getSystemProperty("config.pass");
    authorize(request, response, user, pass, "CreateCMRAdmin");

    ModelMap map = new ModelMap();
    return new ModelAndView("config", "map", map);

  }

  @RequestMapping(
      value = "/convert/{type}")
  public void convert(@PathVariable("type") String type, @RequestParam("time") long time, HttpServletResponse response) throws IOException {
    String val = "";
    if ("D".equalsIgnoreCase(type)) {
      val = CmrConstants.DATE_FORMAT().format(new Date(time));
    } else if ("T".equalsIgnoreCase(type)) {
      val = CmrConstants.DATE_TIME_FORMAT().format(new Date(time));
    }
    response.getOutputStream().write(val.getBytes());
    response.flushBuffer();
  }

}
