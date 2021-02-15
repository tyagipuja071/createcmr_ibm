package com.ibm.cio.cmr.request.util.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import com.ibm.cio.cmr.request.util.ConfigUtil;

public class CloningUtil {

  private static Map<String, String> configList = new HashMap<String, String>();
  public static List<CloningMapping> cloningMappings = new ArrayList<CloningMapping>();

  static {
    configList.put("838", "cloning-838-mapping.xml");
    configList.put("822", "cloning-822-mapping.xml");
    configList.put("862", "cloning-862-mapping.xml");
    configList.put("758", "cloning-758-mapping.xml");
    // CEE Countries
    configList.put("693", "cloning-CEE-mapping.xml");
  }

  /**
   * Gets the configuration with input country
   * 
   * @param countryId
   * @return
   * @throws IOException
   * @throws SAXException
   */
  public static List<CloningMapping> getCloningCofigCountry(String countryId) throws IOException, SAXException {
    String configName = configList.get(countryId);

    if (configName != null) {
      return initCountryConfig(configName);
    }
    return null;
  }

  /**
   * Parses the template and returns the country config representing the
   * configuration
   * 
   * @param configName
   * @return
   * @throws SAXException
   * @throws IOException
   */
  private static synchronized List<CloningMapping> initCountryConfig(String configName) throws IOException, SAXException {
    try (InputStream is = ConfigUtil.getResourceStream(configName)) {
      CloningMappingDigester digester = new CloningMappingDigester();
      CloningUtil.cloningMappings = (ArrayList<CloningMapping>) digester.parse(is);
      if (CloningUtil.cloningMappings != null) {
        return CloningUtil.cloningMappings;
      }

    }
    return null;
  }

}
