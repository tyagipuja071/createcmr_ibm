/**
 * 
 */
package com.ibm.cio.cmr.request.automation.dpl;

/**
 * @author JeffZAMORA
 * 
 */
public class DPLSearchItem implements Comparable<DPLSearchItem> {

  private String item;
  private String countryCode;
  private String partyName;
  private int distance = 100;

  @Override
  public int compareTo(DPLSearchItem o) {
    if (this.distance < o.distance) {
      return -1;
    }
    if (this.distance > o.distance) {
      return 1;
    }
    if (this.partyName.length() < o.partyName.length()) {
      return -1;
    }
    if (this.partyName.length() > o.partyName.length()) {
      return 1;
    }
    return this.partyName.compareTo(o.partyName);
  }

  public String getItem() {
    return item;
  }

  public void setItem(String item) {
    this.item = item;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getPartyName() {
    return partyName;
  }

  public void setPartyName(String partyName) {
    this.partyName = partyName;
  }

  public int getDistance() {
    return distance;
  }

  public void setDistance(int distance) {
    this.distance = distance;
  }

}
