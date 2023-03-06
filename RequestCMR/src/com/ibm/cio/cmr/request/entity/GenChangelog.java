package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The persistent class for the GEN_CHANGE_LOG database table.
 * 
 * @author
 */
@Entity
@Table(name = "GEN_CHANGE_LOG", schema = "SAPR3")
public class GenChangelog implements Serializable {
  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private GenChangelogPK id;

  public GenChangelogPK getId() {
    return id;
  }

  public void setId(GenChangelogPK id) {
    this.id = id;
  }

  @Column(name = "OLD_VALUE")
  private String old_value;

  @Column(name = "NEW_VALUE")
  private String new_value;

  @Column(name = "ACTION_IND")
  private String action_ind;

  @Column(name = "CHANGE_BY")
  private String change_by;

  @Column(name = "CHANGE_SRC_TYP")
  private String change_src_typ;

  @Column(name = "CHANGE_SRC_ID")
  private String change_src_id;

  public String getOld_value() {
    return old_value;
  }

  public void setOld_value(String old_value) {
    this.old_value = old_value;
  }

  public String getNew_value() {
    return new_value;
  }

  public void setNew_value(String new_value) {
    this.new_value = new_value;
  }

  public String getAction_ind() {
    return action_ind;
  }

  public void setAction_ind(String action_ind) {
    this.action_ind = action_ind;
  }

  public String getChange_by() {
    return change_by;
  }

  public void setChange_by(String change_by) {
    this.change_by = change_by;
  }

  public String getChange_src_typ() {
    return change_src_typ;
  }

  public void setChange_src_typ(String change_src_typ) {
    this.change_src_typ = change_src_typ;
  }

  public String getChange_src_id() {
    return change_src_id;
  }

  public void setChange_src_id(String change_src_id) {
    this.change_src_id = change_src_id;
  }

}