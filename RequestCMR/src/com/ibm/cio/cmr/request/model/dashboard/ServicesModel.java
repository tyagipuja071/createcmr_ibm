package com.ibm.cio.cmr.request.model.dashboard;

/**
 * 
 * @author 136786PH1
 *
 */
public class ServicesModel {

  private String servicesStatus;
  private String alert;
  private boolean findCmr;
  private boolean cmrServices;
  private boolean ciServices;
  private boolean usCmr;
  private boolean cros;
  private boolean cris;
  private boolean mq;

  public boolean isFindCmr() {
    return findCmr;
  }

  public void setFindCmr(boolean findCmr) {
    this.findCmr = findCmr;
  }

  public boolean isCmrServices() {
    return cmrServices;
  }

  public void setCmrServices(boolean cmrServices) {
    this.cmrServices = cmrServices;
  }

  public boolean isCiServices() {
    return ciServices;
  }

  public void setCiServices(boolean ciServices) {
    this.ciServices = ciServices;
  }

  public boolean isUsCmr() {
    return usCmr;
  }

  public void setUsCmr(boolean usCmr) {
    this.usCmr = usCmr;
  }

  public boolean isCros() {
    return cros;
  }

  public void setCros(boolean cros) {
    this.cros = cros;
  }

  public boolean isCris() {
    return cris;
  }

  public void setCris(boolean cris) {
    this.cris = cris;
  }

  public boolean isMq() {
    return mq;
  }

  public void setMq(boolean mq) {
    this.mq = mq;
  }

  public String getServicesStatus() {
    return servicesStatus;
  }

  public void setServicesStatus(String servicesStatus) {
    this.servicesStatus = servicesStatus;
  }

  public String getAlert() {
    return alert;
  }

  public void setAlert(String alert) {
    this.alert = alert;
  }

}
