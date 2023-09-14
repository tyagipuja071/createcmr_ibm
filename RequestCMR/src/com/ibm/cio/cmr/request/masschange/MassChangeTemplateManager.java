/**
 * 
 */
package com.ibm.cio.cmr.request.masschange;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

    configList.put("620", "config.620ME1.xml");
    configList.put("642", "config.642.xml");
    configList.put("677", "config.677ME1.xml");
    configList.put("680", "config.680.xml");
    configList.put("752", "config.752ME2.xml");
    configList.put("762", "config.762ME2.xml");
    configList.put("767", "config.767ME1.xml");
    configList.put("768", "config.768ME2.xml");
    configList.put("772", "config.772ME2.xml");
    configList.put("805", "config.805ME1.xml");
    configList.put("808", "config.808ME2.xml");
    configList.put("823", "config.823ME1.xml");
    configList.put("832", "config.832ME1.xml");
    configList.put("849", "config.849ME2.xml");
    configList.put("850", "config.850ME2.xml");
    configList.put("865", "config.865ME2.xml");
    configList.put("729", "config.729.xml");
    configList.put("675", "config.675.xml");
    configList.put("822", "config.822.xml");
    configList.put("666", "config.666.xml");
    configList.put("780", "config.780.xml");
    configList.put("864", "config.864.xml");
    // configList.put("848", "config.SWISS.xml");

    configList.put("706", "config.706.xml");
    configList.put("610", "config.CEWA.xml");
    configList.put("636", "config.CEWA.xml");
    configList.put("645", "config.CEWA.xml");
    configList.put("669", "config.CEWA.xml");
    configList.put("698", "config.CEWA.xml");
    configList.put("725", "config.CEWA.xml");
    configList.put("745", "config.CEWA.xml");
    configList.put("764", "config.764.xml");
    configList.put("769", "config.CEWA.xml");
    configList.put("770", "config.CEWA.xml");
    configList.put("782", "config.CEWA.xml");
    configList.put("804", "config.CEWA.xml");
    configList.put("825", "config.CEWA.xml");
    configList.put("827", "config.CEWA.xml");
    configList.put("831", "config.CEWA.xml");
    configList.put("833", "config.833.xml");
    configList.put("835", "config.CEWA.xml");
    configList.put("842", "config.CEWA.xml");
    configList.put("851", "config.851.xml");
    configList.put("857", "config.CEWA.xml");
    configList.put("883", "config.CEWA.xml");
    configList.put("373", "config.FST.xml");
    configList.put("382", "config.FST.xml");
    configList.put("383", "config.FST.xml");
    configList.put("635", "config.FST.xml");
    configList.put("637", "config.FST.xml");
    configList.put("656", "config.FST.xml");
    configList.put("662", "config.FST.xml");
    configList.put("667", "config.FST.xml");
    configList.put("670", "config.FST.xml");
    configList.put("691", "config.FST.xml");
    configList.put("692", "config.FST.xml");
    configList.put("700", "config.700.xml");
    configList.put("717", "config.FST.xml");
    configList.put("718", "config.FST.xml");
    configList.put("753", "config.FST.xml");
    configList.put("810", "config.FST.xml");
    configList.put("840", "config.FST.xml");
    configList.put("841", "config.FST.xml");
    configList.put("876", "config.FST.xml");
    configList.put("879", "config.FST.xml");
    configList.put("880", "config.FST.xml");
    configList.put("881", "config.FST.xml");

    configList.put("846", "config.846.xml");
    configList.put("702", "config.702.xml");
    configList.put("678", "config.678.xml");
    configList.put("806", "config.806.xml");
    configList.put("788", "config.788.xml");
    configList.put("624", "config.624.xml");
    configList.put("724", "config.724.xml");
    configList.put("649", "config.649.xml");
    configList.put("755", "config.755.xml");

    configList.put("781", "config.781.xml");
    configList.put("613", "config.LA.xml");
    configList.put("629", "config.LA.xml");
    configList.put("655", "config.LA.xml");
    configList.put("661", "config.LA.xml");
    configList.put("683", "config.LA.xml");
    configList.put("813", "config.LA.xml");
    configList.put("815", "config.LA.xml");
    configList.put("869", "config.LA.xml");
    configList.put("871", "config.LA.xml");
    configList.put("663", "config.LA.xml");
    configList.put("681", "config.LA.xml");
    configList.put("829", "config.LA.xml");
    configList.put("731", "config.LA.xml");
    configList.put("735", "config.LA.xml");
    configList.put("799", "config.LA.xml");
    configList.put("811", "config.LA.xml");
    configList.put("631", "config.631.xml");

    configList.put("760", "config.760.xml");

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
    if (configName == null && StringUtils.isNumeric(templateId)) {
      // secondary check
      configName = configList.get(templateId);
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
    if (configName == null && StringUtils.isNumeric(templateId)) {
      // secondary check
      configName = configList.get(templateId);
    }
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
