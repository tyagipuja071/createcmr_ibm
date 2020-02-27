/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util;

import java.util.List;

import com.ibm.cio.cmr.utils.coverage.rules.Rule;

/**
 * @author JeffZAMORA
 *
 */
public class CoverageContainer {

  private List<Rule> baseCoverageRules;
  private List<Rule> finalCoverageRules;
  private String baseCoverage;
  private String finalCoverage;
  private String territoryId;

  public String getBaseCoverage() {
    return baseCoverage;
  }

  public void setBaseCoverage(String baseCoverage) {
    this.baseCoverage = baseCoverage;
  }

  public String getFinalCoverage() {
    return finalCoverage;
  }

  public void setFinalCoverage(String finalCoverage) {
    this.finalCoverage = finalCoverage;
  }

  public String getTerritoryId() {
    return territoryId;
  }

  public void setTerritoryId(String territoryId) {
    this.territoryId = territoryId;
  }

  public List<Rule> getBaseCoverageRules() {
    return baseCoverageRules;
  }

  public void setBaseCoverageRules(List<Rule> baseCoverageRules) {
    this.baseCoverageRules = baseCoverageRules;
  }

  public List<Rule> getFinalCoverageRules() {
    return finalCoverageRules;
  }

  public void setFinalCoverageRules(List<Rule> finalCoverageRules) {
    this.finalCoverageRules = finalCoverageRules;
  }

}
