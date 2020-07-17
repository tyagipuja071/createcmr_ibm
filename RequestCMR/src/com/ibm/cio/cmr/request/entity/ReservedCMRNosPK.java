package com.ibm.cio.cmr.request.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.StringUtils;

/**
 * The primary key class for the ReservedCMRNos database table.
 * 
 * @author Mukesh
 */
@Embeddable
public class ReservedCMRNosPK extends BaseEntityPk implements Serializable {

  // default serial version id, required for serializable classes.
  private static final long serialVersionUID = 1L;
  
  private String mandt;

  @Column(name = "CMR_ISSUING_CNTRY")
  private String cmrIssuingCntry;

  @Column(name = "CMR_NO")
  private String cmrNo;
  
  public String getMandt() {
		return mandt;
	}


	public void setMandt(String mandt) {
		this.mandt = mandt;
	}


	public String getCmrIssuingCntry() {
		return cmrIssuingCntry;
	}


	public void setCmrIssuingCntry(String cmrIssuingCntry) {
		this.cmrIssuingCntry = cmrIssuingCntry;
	}


	public String getCmrNo() {
		return cmrNo;
	}


	public void setCmrNo(String cmrNo) {
		this.cmrNo = cmrNo;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cmrIssuingCntry == null) ? 0 : cmrIssuingCntry.hashCode());
		result = prime * result + ((cmrNo == null) ? 0 : cmrNo.hashCode());
		result = prime * result + ((mandt == null) ? 0 : mandt.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReservedCMRNosPK other = (ReservedCMRNosPK) obj;
		if (cmrIssuingCntry == null) {
			if (other.cmrIssuingCntry != null)
				return false;
		} else if (!cmrIssuingCntry.equals(other.cmrIssuingCntry))
			return false;
		if (cmrNo == null) {
			if (other.cmrNo != null)
				return false;
		} else if (!cmrNo.equals(other.cmrNo))
			return false;
		if (mandt == null) {
			if (other.mandt != null)
				return false;
		} else if (!mandt.equals(other.mandt))
			return false;
		return true;
	}


	@Override
	protected boolean allKeysAssigned() {
		// TODO Auto-generated method stub
		return !StringUtils.isEmpty(this.cmrIssuingCntry) && !StringUtils.isEmpty(this.cmrNo) && !StringUtils.isEmpty(this.mandt);
	}
 
}