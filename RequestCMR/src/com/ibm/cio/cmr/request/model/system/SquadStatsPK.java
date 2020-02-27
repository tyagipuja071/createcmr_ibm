package com.ibm.cio.cmr.request.model.system;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 
 * @author JeffZAMORA
 * 
 */
@Embeddable
public class SquadStatsPK {

  @Column(
      name = "CMR_ISSUING_CNTRY")
  private String cmrIssuingCntry;

  @Column(
      name = "PROCESS_DT")
  private String processDt;

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getProcessDt() {
    return processDt;
  }

  public void setProcessDt(String processDt) {
    this.processDt = processDt;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SquadStatsPK)) {
      return false;
    }
    SquadStatsPK pk = (SquadStatsPK) o;
    return this.cmrIssuingCntry.equals(pk.cmrIssuingCntry) && this.processDt.equals(pk.processDt);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 17;
    hash = hash * prime + this.cmrIssuingCntry.hashCode();
    hash = hash * prime + this.processDt.hashCode();

    return hash;
  }

}
