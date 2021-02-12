package com.ibm.cio.cmr.request.util.legacy;

import org.apache.log4j.Logger;

public class CloningMapping {

  private static final Logger LOG = Logger.getLogger(CloningMapping.class);

  private String cmrNoRange;
  private String countries;

  public String getCmrNoRange() {
    return cmrNoRange;
  }

  public void setCmrNoRange(String cmrNoRange) {
    this.cmrNoRange = cmrNoRange;
  }

  public String getCountries() {
    return countries;
  }

  public void setCountries(String countries) {
    this.countries = countries;
  }

}
