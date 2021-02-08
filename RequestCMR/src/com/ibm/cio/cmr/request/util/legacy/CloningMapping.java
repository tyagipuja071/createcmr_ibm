package com.ibm.cio.cmr.request.util.legacy;

import org.apache.log4j.Logger;

public class CloningMapping {

  private static final Logger LOG = Logger.getLogger(CloningMapping.class);

  private String cmrNoMin;
  private String cmrNoMax;

  public String getCmrNoMin() {
    return cmrNoMin;
  }

  public void setCmrNoMin(String cmrNoMin) {
    this.cmrNoMin = cmrNoMin;
  }

  public String getCmrNoMax() {
    return cmrNoMax;
  }

  public void setCmrNoMax(String cmrNoMax) {
    this.cmrNoMax = cmrNoMax;
  }

}
