package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the ReservedCMRNos database table.
 * 
 * @author Clint Mariano
 */
@Entity 
@Table(name = "RESERVED_CMR_NOS", schema = "CREQCMR")
public class ReservedCMRNos extends BaseEntity<ReservedCMRNosPK>  implements Serializable {
  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private ReservedCMRNosPK id;

public ReservedCMRNosPK getId() {
	return id;
}

public void setId(ReservedCMRNosPK id) {
	this.id = id;
}

private String comments;

  private String status;

  @Column(name = "INACTIVATE_RSN")
  private String inactivateRsn;

  @Column(name = "CREATE_TS")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createTs;

  @Column(name = "CREATE_BY")
  private String createBy;
																		
  @Column(name = "LAST_UPDT_TS")
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastUpdtTs;

  @Column(name = "LAST_UPDT_BY")
  private String lastUpdtBy;

public String getComments() {
	return comments;
}

public void setComments(String comments) {
	this.comments = comments;
}

public String getStatus() {
	return status;
}

public void setStatus(String status) {
	this.status = status;
}

public String getInactivateRsn() {
	return inactivateRsn;
}

public void setInactivateRsn(String inactivateRsn) {
	this.inactivateRsn = inactivateRsn;
}

public Date getCreateTs() {
	return createTs;
}

public void setCreateTs(Date createTs) {
	this.createTs = createTs;
}

public String getCreateBy() {
	return createBy;
}

public void setCreateBy(String createBy) {
	this.createBy = createBy;
}

public Date getLastUpdtTs() {
	return lastUpdtTs;
}

public void setLastUpdtTs(Date lastUpdtTs) {
	this.lastUpdtTs = lastUpdtTs;
}

public String getLastUpdtBy() {
	return lastUpdtBy;
}

public void setLastUpdtBy(String lastUpdtBy) {
	this.lastUpdtBy = lastUpdtBy;
}
  
}