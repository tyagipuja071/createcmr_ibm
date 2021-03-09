package com.ibm.cio.cmr.request.util.legacy;

import java.io.InputStream;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.util.ConfigUtil;

/**
 * @author PriyRanjan
 * 
 */

public class CloningRDCUtil {

  private static final Logger LOG = Logger.getLogger(CloningRDCUtil.class);

  public CloningRDCConfiguration getConfigDetails() {
    CloningRDCConfiguration config = null;
    try {
      InputStream is = ConfigUtil.getResourceStream("cloning-service-config.xml");
      CloningRDCDigester digester = new CloningRDCDigester();
      config = (CloningRDCConfiguration) digester.parse(is);
    } catch (Exception e) {
      LOG.debug("Error occured while digesting xml.", e);
    }
    return config;
  }

}
