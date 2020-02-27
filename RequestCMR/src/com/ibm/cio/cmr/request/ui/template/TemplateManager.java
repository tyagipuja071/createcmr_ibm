/**
 * 
 */
package com.ibm.cio.cmr.request.ui.template;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.json.java.JSONObject;

/**
 * @author Jeffrey Zamora
 * 
 */
public class TemplateManager {

  private static final Logger LOG = Logger.getLogger(TemplateManager.class);

  private static Map<String, TemplateSupport> templateSupportMap = new HashMap<String, TemplateSupport>();
  private static Map<String, Map<String, JSONObject>> registeredTemplates = new HashMap<String, Map<String, JSONObject>>();

  public static void refresh() {
    templateSupportMap.clear();
    registeredTemplates.clear();
  }

  public static TemplateSupport getTemplateSupport(String cmrIssuingCountry) {
    TemplateSupport support = templateSupportMap.get(cmrIssuingCountry);
    if (support == null) {
      try {
        String templateSupportClass = SystemConfiguration.getSystemProperty("template." + cmrIssuingCountry);
        if (templateSupportClass == null) {
          return support;
        }
        support = (TemplateSupport) Class.forName(templateSupportClass).newInstance();
        if (support != null) {
          LOG.info("Initialized TemplateSupport for Country: " + cmrIssuingCountry);
          templateSupportMap.put(cmrIssuingCountry, support);
        }
      } catch (Exception e) {
        LOG.error("An error occurred when trying to initialize the templates.");
      }
    }
    return support;
  }

  public static synchronized Template getTemplate(String cmrIssuingCountry, HttpServletRequest request) throws Exception {
    TemplateSupport templateSupport = getTemplateSupport(cmrIssuingCountry);
    if (templateSupport == null) {
      return null;
    }
    TemplateDriver driver = templateSupport.getDriverField();
    String driverValue = request.getParameter(driver.getFieldName());

    if (StringUtils.isEmpty(driverValue)) {
      return null;
    }

    Map<String, JSONObject> templates = registeredTemplates.get(cmrIssuingCountry);
    if (templates == null) {
      registeredTemplates.put(cmrIssuingCountry, new HashMap<String, JSONObject>());
      templates = registeredTemplates.get(cmrIssuingCountry);
    }
    JSONObject template = templates.get(driverValue);
    if (template != null) {
      ObjectMapper mapper = new ObjectMapper();
      String jsonString = mapper.writeValueAsString(template);
      Template templ = mapper.readValue(jsonString, Template.class);
      return templ;
    } else {
      Template templ = templateSupport.getTemplate(driverValue, request);
      ObjectMapper mapper = new ObjectMapper();
      String jsonString = mapper.writeValueAsString(templ);
      template = JSONObject.parse(jsonString);
      templates.put(driverValue, template);
      LOG.debug("Successfully added template for " + driverValue + "(" + cmrIssuingCountry + "). Count is " + templates.size());
      return templ;
    }
  }

  public static String getDriverField(String cmrIssuingCountry) {
    TemplateSupport support = getTemplateSupport(cmrIssuingCountry);
    if (support != null) {
      return support.getDriverField().getFieldName();
    }
    return "";
  }
}
