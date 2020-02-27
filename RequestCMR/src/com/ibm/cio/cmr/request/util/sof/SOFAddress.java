/**
 * 
 */
package com.ibm.cio.cmr.request.util.sof;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SOFAddress {

  private String seqNo;
  private String type;
  private Map<String, SOFAttribute> attributes = new LinkedHashMap<String, SOFAttribute>();

  public void addAttribute(String name, String value) {
    this.attributes.put(name, new SOFAttribute(name, value));
    if (name.equals("AddressNumber")) {
      if (!StringUtils.isEmpty(value)) {
        this.seqNo = value;
      } else {
        this.seqNo = "00001";
      }
    }
    if (name.endsWith("Address1")) {
      this.type = name.substring(0, name.indexOf("Address1"));
    }
  }

  public String getSeqNo() {
    return seqNo;
  }

  public void setSeqNo(String seqNo) {
    this.seqNo = seqNo;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public SOFAttribute getAttribute(String key) {
    SOFAttribute att = attributes.get(key);
    if (att == null) {
      att = attributes.get(this.type + key);
    }
    return att;
  }

  public Map<String, SOFAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, SOFAttribute> attributes) {
    this.attributes = attributes;
  }

}
