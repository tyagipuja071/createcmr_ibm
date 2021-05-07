package com.ibm.cio.cmr.request.util.legacy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.util.ConfigUtil;

public class CloningOverrideUtil {

  private static final Logger LOG = Logger.getLogger(CloningOverrideUtil.class);
  private static List<CloningOverrideMapping> overrideMappings = new ArrayList<CloningOverrideMapping>();
  private static List<CloningOverrideMapping> overrideMappingsIbm = new ArrayList<CloningOverrideMapping>();
  // private static List<CloningOverrideMapping> returnMappings = new
  // ArrayList<CloningOverrideMapping>();
  private List<CloningOverrideMapping> returnMappings = null;

  @SuppressWarnings("unchecked")
  public CloningOverrideUtil() {
    if (CloningOverrideUtil.overrideMappings.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("mappings", ArrayList.class);

      digester.addObjectCreate("mappings/override", CloningOverrideMapping.class);

      digester.addBeanPropertySetter("mappings/override/table", "table");
      digester.addBeanPropertySetter("mappings/override/field", "field");
      digester.addBeanPropertySetter("mappings/override/value", "value");
      digester.addBeanPropertySetter("mappings/override/countries", "countries");
      digester.addSetNext("mappings/override", "add");
      try {
        InputStream is = ConfigUtil.getResourceStream("cloning-override-mapping.xml");
        CloningOverrideUtil.overrideMappings = (ArrayList<CloningOverrideMapping>) digester.parse(is);
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }

    if (CloningOverrideUtil.overrideMappingsIbm.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("mappings", ArrayList.class);

      digester.addObjectCreate("mappings/override", CloningOverrideMapping.class);

      digester.addBeanPropertySetter("mappings/override/table", "table");
      digester.addBeanPropertySetter("mappings/override/field", "field");
      digester.addBeanPropertySetter("mappings/override/value", "value");
      digester.addBeanPropertySetter("mappings/override/countries", "countries");
      digester.addSetNext("mappings/override", "add");
      try {
        InputStream is = ConfigUtil.getResourceStream("cloning-override-ibm-mapping.xml");
        CloningOverrideUtil.overrideMappingsIbm = (ArrayList<CloningOverrideMapping>) digester.parse(is);
      } catch (Exception e) {
        LOG.error("Error occured while digesting ibm xml.", e);
      }
    }
  }

  public List<CloningOverrideMapping> getOverrideValueFromMapping(String country, String configName) {
    if (!overrideMappings.isEmpty() && "GTS".equalsIgnoreCase(configName)) {
      returnMappings = new ArrayList<CloningOverrideMapping>();
      for (CloningOverrideMapping mapping : overrideMappings) {
        List<String> countryValues = Arrays.asList(mapping.getCountries().replaceAll("\n", "").replaceAll(" ", "").split(","));
        if (countryValues.contains(country)) {
          returnMappings.add(mapping);

        }
      }
      return returnMappings;
    } else if (!overrideMappingsIbm.isEmpty() && "IBM".equalsIgnoreCase(configName)) {
      returnMappings = new ArrayList<CloningOverrideMapping>();
      for (CloningOverrideMapping mapping : overrideMappingsIbm) {
        List<String> countryValues = Arrays.asList(mapping.getCountries().replaceAll("\n", "").replaceAll(" ", "").split(","));
        if (countryValues.contains(country)) {
          returnMappings.add(mapping);

        }
      }
      return returnMappings;
    }
    return null;
  }

}
