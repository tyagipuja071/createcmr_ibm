package com.ibm.cio.cmr.request.util.legacy;

import java.io.InputStream;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.util.ConfigUtil;

public class CloningRDCUtil {

  private static final Logger LOG = Logger.getLogger(CloningRDCUtil.class);

  public CloningRDCConfiguration getConfigDetails() {
    CloningRDCConfiguration config = null;
    try {
      InputStream is = ConfigUtil.getResourceStream("cloning-service-config.xml");
      CloningRDCDigester digester = new CloningRDCDigester();
      config = (CloningRDCConfiguration) digester.parse(is);
      System.out.println("value my===" + config.getCountriesForKnb1Create());
      System.out.println("value my 22===" + config.getCountriesForKnvvCreate());
      System.out.println("value my 33===" + config.isProcessKnb1());
      System.out.println("value my 44===" + config.getTargetMandt());
      System.out.println("value my 55===" + config.getKatr10());
    } catch (Exception e) {
      LOG.debug("Error occured while digesting xml.", e);
    }
    return config;
  }

}
