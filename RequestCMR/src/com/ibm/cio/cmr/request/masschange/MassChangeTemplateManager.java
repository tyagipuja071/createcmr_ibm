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
import com.ibm.cio.cmr.request.util.SystemLocation;

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
  private static Map<String, String> configCountryMap = new HashMap<String, String>();
  private static Map<String, String> configList = new HashMap<String, String>();
  static {
    configList.put("838", "config.838.xml");
    configList.put("866", "config.866.xml");
    configList.put("754", "config.754.xml");
    configList.put("758", "config.758.xml");
    configList.put("618", "config.618.xml");
    configList.put("862", "config.862.xml");
    configList.put("693", "config.693.xml");
    configList.put("695", "config.695.xml");
    configList.put("707", "config.707.xml");

    configList.put("358", "config.358.xml");
    configList.put("359", "config.359.xml");
    configList.put("363", "config.363.xml");
    configList.put("603", "config.603.xml");
    configList.put("607", "config.607.xml");
    configList.put("626", "config.626.xml");
    configList.put("644", "config.644.xml");
    configList.put("651", "config.651.xml");
    configList.put("694", "config.694.xml");
    configList.put("699", "config.699.xml");
    configList.put("705", "config.705.xml");
    configList.put("708", "config.708.xml");
    configList.put("740", "config.740.xml");
    configList.put("741", "config.741.xml");
    configList.put("787", "config.787.xml");
    configList.put("820", "config.820.xml");
    configList.put("821", "config.821.xml");
    configList.put("826", "config.826.xml");
    configList.put("889", "config.889.xml");
    configList.put("704", "config.704.xml");
    configList.put("668", "config.668.xml");
    configList.put("726", "config.726.xml");
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
    init(configList.get(issuingCntry), issuingCntry);
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
    init("config.SWISS.xml", SystemLocation.SWITZERLAND);
  }

  /**
   * Initializes the templates
   * 
   * @param configName
   * @throws IOException
   * @throws SAXException
   */
  private static void init(String configName, String issuingCountry) throws IOException, SAXException {
    try (InputStream is = ConfigUtil.getResourceStream(configName)) {
      MassChangeTemplateDigester digester = new MassChangeTemplateDigester();
      MassChangeTemplate template = (MassChangeTemplate) digester.parse(is);
      if (template != null) {
        if (hasCmrNoOnAllTabs(template)) {
          if (CmrConstants.REQ_TYPE_MASS_UPDATE.equals(template.getType())) {
            templateMapUpdate.put(template.getId(), configName);
            configCountryMap.put(issuingCountry, template.getId());
          } else if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(template.getType())) {
            templateMapCreate.put(template.getId(), configName);
            configCountryMap.put(issuingCountry, template.getId());
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
    if (configName == null) {
      configName = configCountryMap.get(templateId);
    }
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
