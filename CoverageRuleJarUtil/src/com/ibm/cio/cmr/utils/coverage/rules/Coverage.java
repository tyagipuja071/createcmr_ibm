/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.rules;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Represents a coverage assignment with type and id
 * 
 * @author JeffZAMORA
 *
 */
public class Coverage {

  private String type;
  private String level;
  private String id;
  private String country;
  private boolean delegationAuthorized;

  private String isu;
  private String clientTier;
  private Rule attachedRule;

  @Override
  public String toString() {
    if (this.type == null && this.delegationAuthorized) {
      return "Delegation Authorized";
    }
    return this.type + this.id + (this.level != null && !this.level.isEmpty() ? " (" + this.level + ")" : "")
        + (this.country != null ? " [" + this.country + "]" : "");
  }

  @Override
  public Coverage clone() {
    Coverage coverage = new Coverage();
    try {
      PropertyUtils.copyProperties(coverage, this);
    } catch (Exception e) {
    }
    return coverage;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type != null ? type.replaceAll("\"", "").trim() : null;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id != null ? id.replaceAll("\"", "").trim() : null;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country != null ? country.replaceAll("\"", "").trim() : null;
  }

  public boolean isDelegationAuthorized() {
    return delegationAuthorized;
  }

  public void setDelegationAuthorized(boolean delegationAuthorized) {
    this.delegationAuthorized = delegationAuthorized;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level != null && !level.trim().isEmpty() ? level.replaceAll("\"", "").trim() : null;
  }

  public Rule getAttachedRule() {
    return attachedRule;
  }

  public void setAttachedRule(Rule attachedRule) {
    this.attachedRule = attachedRule;
  }

  public String getIsu() {
    return isu;
  }

  public void setIsu(String isu) {
    this.isu = isu;
  }

  public String getClientTier() {
    return clientTier;
  }

  public void setClientTier(String clientTier) {
    this.clientTier = clientTier;
  }

}
