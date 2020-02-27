/**
 * 
 */
package com.ibm.cio.cmr.request.util.wtaas;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JeffZAMORA
 * 
 */
public class WtaasAddress {

  private String addressNo;
  private String addressUse;

  protected Map<WtaasQueryKeys.Address, String> values = new HashMap<WtaasQueryKeys.Address, String>();

  /**
   * Gets the value of the address key stored on the address
   * 
   * @param key
   * @return
   */
  public String get(WtaasQueryKeys.Address key) {
    return this.values.get(key);
  }

  public String getAddressNo() {
    return addressNo;
  }

  public void setAddressNo(String addressNo) {
    this.addressNo = addressNo;
  }

  public String getAddressUse() {
    return addressUse;
  }

  public void setAddressUse(String addressUse) {
    this.addressUse = addressUse;
  }

  public Map<WtaasQueryKeys.Address, String> getValues() {
    return values;
  }

  protected void setValues(Map<WtaasQueryKeys.Address, String> values) {
    this.values = values;
  }
}
