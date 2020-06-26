/**
 * 
 */
package com.ibm.cio.cmr.request.masschange;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.masschange.config.MassChangeTemplateDigester;
import com.ibm.cio.cmr.request.masschange.obj.MassChangeTemplate;
import com.ibm.cio.cmr.request.masschange.obj.TemplateColumn;
import com.ibm.cio.cmr.request.masschange.obj.TemplateTab;
import com.ibm.cio.cmr.request.util.ConfigUtil;

/**
 * Initializes and manages the templates from the XML configurations
 * 
 * @author JeffZAMORA
 * 
 */
public class MassChangeTemplateManager {

  private static final Logger LOG = Logger.getLogger(MassChangeTemplateManager.class);
  private static Map<String, String> templateMapCreate = new HashMap<String, String>();
  private static Map<String, String> templateMapUpdate = new HashMap<String, String>();
  private static Map<String, ValueValidator> validators = new HashMap<String, ValueValidator>();
  private static Map<String, String> configList = new HashMap<String, String>();
  static {
    configList.put("838", "config.838.xml");
    configList.put("866", "config.866.xml");
    configList.put("754", "config.754.xml");
    configList.put("758", "config.758.xml");
    configList.put("618", "config.618.xml");
    configList.put("726", "config.726.xml");
    configList.put("822", "config.822.xml");
    // *abner revert begin
    // configList.put("862", "config.862.xml");
    // *abner revert end
    // configList.put("848", "config.SWISS.xml");
  }

  /**
   * Initializes the templates
   * 
   * @throws IOException
   * @throws SAXException
   */
  public static void initTemplatesAndValidators(String issuingCntry) throws IOException, SAXException {
    templateMapCreate.clear();
    templateMapUpdate.clear();
    init(configList.get(issuingCntry));
  }

  /**
   * Initializes the templates
   * 
   * @throws IOException
   * @throws SAXException
   */
  public static void initTemplatesAndValidatorsSwiss() throws IOException, SAXException {
    templateMapCreate.clear();
    templateMapUpdate.clear();
    init("config.SWISS.xml");
  }

  /**
   * Initializes the templates
   * 
   * @param configName
   * @throws IOException
   * @throws SAXException
   */
  private static void init(String configName) throws IOException, SAXException {
    try (InputStream is = ConfigUtil.getResourceStream(configName)) {
      MassChangeTemplateDigester digester = new MassChangeTemplateDigester();
      MassChangeTemplate template = (MassChangeTemplate) digester.parse(is);
      if (template != null) {
        if (hasCmrNoOnAllTabs(template)) {
          if (CmrConstants.REQ_TYPE_MASS_UPDATE.equals(template.getType())) {
            templateMapUpdate.put(template.getId(), configName);
          } else if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(template.getType())) {
            templateMapCreate.put(template.getId(), configName);
          }
        } else {
          LOG.warn("Configuration " + configName + " is missing CMR No. on one of the tabs.");
        }
      } else {
        LOG.warn("Mass Change configuration " + configName + " cannot be read properly. ");
      }
    }
  }

  /**
   * Checks if the template has CMR No. field on all tabs, which is required
   * 
   * @param template
   * @return
   */
  private static boolean hasCmrNoOnAllTabs(MassChangeTemplate template) {
    for (TemplateTab tab : template.getTabs()) {
      boolean found = false;
      for (TemplateColumn col : tab.getColumns()) {
        if ("CMR_NO".equals(col.getDbColumn())) {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the Mass Update template with the given ID
   * 
   * @param templateId
   * @return
   * @throws IOException
   * @throws SAXException
   */
  public static MassChangeTemplate getMassUpdateTemplate(String templateId) throws IOException, SAXException {
    String configName = templateMapUpdate.get(templateId);
    if (configName != null) {
      return initTemplate(configName);
    }
    return null;
  }

  /**
   * Gets the Mass Create template with the given ID
   * 
   * @param templateId
   * @return
   * @throws IOException
   * @throws SAXException
   */
  public static MassChangeTemplate getMassCreateTemplate(String templateId) throws IOException, SAXException {
    String configName = templateMapCreate.get(templateId);
    if (configName != null) {
      return initTemplate(configName);
    }
    return null;
  }

  /**
   * Returns the validator registered to the particular field
   * 
   * @param field
   * @return
   */
  public ValueValidator getValidator(String field) {
    return validators.get(field);
  }

  /**
   * Parses the template and returns the {@link MassChangeTemplate} object
   * representing the configuration
   * 
   * @param configName
   * @return
   * @throws SAXException
   * @throws IOException
   */
  private static synchronized MassChangeTemplate initTemplate(String configName) throws IOException, SAXException {
    try (InputStream is = ConfigUtil.getResourceStream(configName)) {
      MassChangeTemplateDigester digester = new MassChangeTemplateDigester();
      MassChangeTemplate template = (MassChangeTemplate) digester.parse(is);
      if (template != null) {
        return template;
      }
    }
    return null;
  }
}
