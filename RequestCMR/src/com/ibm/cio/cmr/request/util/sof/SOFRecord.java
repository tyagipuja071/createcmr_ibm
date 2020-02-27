/**
 * 
 */
package com.ibm.cio.cmr.request.util.sof;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SOFRecord {

  private Map<String, SOFAttribute> dataElements = new LinkedHashMap<String, SOFAttribute>();
  private List<SOFAddress> addresses = new ArrayList<SOFAddress>();
  private String customerNo;
  private boolean success;
  private String msg;

  public SOFRecord(Map<String, String> dataElements, List<Map<String, String>> addresses) {

    if (StringUtils.isEmpty(dataElements.get("CustomerNo")) && StringUtils.isEmpty(dataElements.get("SourceCode"))) {
      this.success = false;
      this.msg = "Record not found.";
      return;
    }
    for (String key : dataElements.keySet()) {
      this.dataElements.put(key, new SOFAttribute(key, dataElements.get(key)));
      if ("CustomerNo".equals(key)) {
        this.customerNo = dataElements.get(key);
      }
    }

    for (Map<String, String> addr : addresses) {
      SOFAddress address = new SOFAddress();
      for (String key : addr.keySet()) {
        address.addAttribute(key, addr.get(key));
      }
      if (!StringUtils.isEmpty(address.getSeqNo())) {
        this.addresses.add(address);
      }
    }
    this.success = true;
  }

  public SOFRecord(String errorResponse) {
    this.success = false;
    this.msg = errorResponse;
  }

  public SOFAttribute getDataElement(String key) {
    return dataElements.get(key);
  }

  public List<SOFAddress> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<SOFAddress> addresses) {
    this.addresses = addresses;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getCustomerNo() {
    return customerNo;
  }

  public void setCustomerNo(String customerNo) {
    this.customerNo = customerNo;
  }

  public Map<String, SOFAttribute> getDataElements() {
    return dataElements;
  }

  public void setDataElements(Map<String, SOFAttribute> dataElements) {
    this.dataElements = dataElements;
  }
}
