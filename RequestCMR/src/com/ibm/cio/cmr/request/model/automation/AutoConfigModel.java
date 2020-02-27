/**
 * 
 */
package com.ibm.cio.cmr.request.model.automation;

import java.util.Date;

/**
 * @author JeffZAMORA
 * 
 */
public class AutoConfigModel {

  private String configId;

  private String configDefn;

  private String createBy;

  private Date createTs;

  private String lastUpdtBy;

  private Date lastUpdtTs;

  public String getConfigId() {
    return configId;
  }

  public void setConfigId(String configId) {
    this.configId = configId;
  }

  public String getConfigDefn() {
    return configDefn;
  }

  public void setConfigDefn(String configDefn) {
    this.configDefn = configDefn;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getLastUpdtBy() {
    return lastUpdtBy;
  }

  public void setLastUpdtBy(String lastUpdtBy) {
    this.lastUpdtBy = lastUpdtBy;
  }

  public Date getLastUpdtTs() {
    return lastUpdtTs;
  }

  public void setLastUpdtTs(Date lastUpdtTs) {
    this.lastUpdtTs = lastUpdtTs;
  }

}
