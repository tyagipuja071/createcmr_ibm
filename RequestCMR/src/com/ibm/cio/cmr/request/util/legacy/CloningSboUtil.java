package com.ibm.cio.cmr.request.util.legacy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.util.ConfigUtil;

public class CloningSboUtil {

  private static final Logger LOG = Logger.getLogger(CloningSboUtil.class);
  private static List<CloningSboMapping> sboMappings = new ArrayList<CloningSboMapping>();

  @SuppressWarnings("unchecked")
  public CloningSboUtil() {
    if (CloningSboUtil.sboMappings.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("mappings", ArrayList.class);

      digester.addObjectCreate("mappings/mapping", CloningSboMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/country", "country");
      digester.addBeanPropertySetter("mappings/mapping/sbo", "sbo");
      digester.addSetNext("mappings/mapping", "add");
      try {
        InputStream is = ConfigUtil.getResourceStream("cloning-sbo-mapping.xml");
        CloningSboUtil.sboMappings = (ArrayList<CloningSboMapping>) digester.parse(is);
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }
  }

  public String getSBOFromMapping(String country) {
    if (!sboMappings.isEmpty()) {
      for (CloningSboMapping mapping : sboMappings) {
        if (country.equals(mapping.getCountry()))
          return mapping.getSbo();
      }
    }
    return "";
  }

}
