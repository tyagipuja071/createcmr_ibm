/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author JeffZAMORA
 * 
 */
@Entity
public class SquadStatisticsModel {

  @EmbeddedId
  private SquadStatsPK id;

  @Column(
      name = "CNTRY_NAME")
  private String cntryName;
  private int total;

  @Transient
  private String squad;
  @Transient
  private String tribe;
  @Transient
  private String iot;
  @Transient
  private String imt;
  @Transient
  private String quarter;
  @Transient
  private String display;

  public SquadStatsPK getId() {
    return id;
  }

  public void setId(SquadStatsPK id) {
    this.id = id;
  }

  public String getCntryName() {
    return cntryName;
  }

  public void setCntryName(String cntryName) {
    this.cntryName = cntryName;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public String getSquad() {
    return squad;
  }

  public void setSquad(String squad) {
    this.squad = squad;
  }

  public String getTribe() {
    return tribe;
  }

  public void setTribe(String tribe) {
    this.tribe = tribe;
  }

  public String getIot() {
    return iot;
  }

  public void setIot(String iot) {
    this.iot = iot;
  }

  public String getImt() {
    return imt;
  }

  public void setImt(String imt) {
    this.imt = imt;
  }

  public String getQuarter() {
    return quarter;
  }

  public void setQuarter(String quarter) {
    this.quarter = quarter;
  }

  public String getDisplay() {
    return display;
  }

  public void setDisplay(String display) {
    this.display = display;
  }
}
