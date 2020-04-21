/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util.geo.us;

/**
 * Stores relevant details for a CMR for US records
 * 
 * @author JeffZAMORA
 *
 */
public class USDetailsContainer {

  private String custTypCd;
  private String entType;
  private String leasingCo;
  private String bpAccTyp;
  private String cGem;
  private String usRestrictTo;
  private String companyNo;
  private String pccArDept;

  public String getCustTypCd() {
    return custTypCd;
  }

  public void setCustTypCd(String custTypCd) {
    this.custTypCd = custTypCd;
  }

  public String getEntType() {
    return entType;
  }

  public void setEntType(String entType) {
    this.entType = entType;
  }

  public String getLeasingCo() {
    return leasingCo;
  }

  public void setLeasingCo(String leasingCo) {
    this.leasingCo = leasingCo;
  }

  public String getBpAccTyp() {
    return bpAccTyp;
  }

  public void setBpAccTyp(String bpAccTyp) {
    this.bpAccTyp = bpAccTyp;
  }

  public String getcGem() {
    return cGem;
  }

  public void setcGem(String cGem) {
    this.cGem = cGem;
  }

  public String getUsRestrictTo() {
    return usRestrictTo;
  }

  public void setUsRestrictTo(String usRestrictTo) {
    this.usRestrictTo = usRestrictTo;
  }

  public String getCompanyNo() {
    return companyNo;
  }

  public void setCompanyNo(String companyNo) {
    this.companyNo = companyNo;
  }

  public String getPccArDept() {
    return pccArDept;
  }

  public void setPccArDept(String pccArDept) {
    this.pccArDept = pccArDept;
  }

}
