/**
 * 
 */
package com.ibm.cio.cmr.request.model.window;

/**
 * @author Jeffrey Zamora
 * 
 */
public class UpdatedNameAddrModel extends UpdatedDataModel {

  private String addrType;
  private String sapNumber;

  public String getAddrType() {
    return addrType;
  }

  public void setAddrType(String addrType) {
    this.addrType = addrType;
  }

  public String getSapNumber() {
    return sapNumber;
  }

  public void setSapNumber(String sapNumber) {
    this.sapNumber = sapNumber;
  }

}
