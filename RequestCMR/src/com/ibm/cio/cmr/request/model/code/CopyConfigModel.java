package com.ibm.cio.cmr.request.model.code;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * 
 * @author Jeffrey Zamora
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CopyConfigModel {

  private String sourceCountry;
  private String targetGeo;
  private String targetCountry;
  private String configType;

  public String getSourceCountry() {
    return sourceCountry;
  }

  public void setSourceCountry(String sourceCountry) {
    this.sourceCountry = sourceCountry;
  }

  public String getTargetGeo() {
    return targetGeo;
  }

  public void setTargetGeo(String targetGeo) {
    this.targetGeo = targetGeo;
  }

  public String getTargetCountry() {
    return targetCountry;
  }

  public void setTargetCountry(String targetCountry) {
    this.targetCountry = targetCountry;
  }

  public String getConfigType() {
    return configType;
  }

  public void setConfigType(String configType) {
    this.configType = configType;
  }
}
