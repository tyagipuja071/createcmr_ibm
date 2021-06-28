/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.rules;

import java.util.ArrayList;
import java.util.List;

import com.ibm.cio.cmr.utils.coverage.objects.CoverageInput;

/**
 * Represents a defined coverage rule
 * 
 * @author JeffZAMORA
 *
 */
public class Rule implements Comparable<Rule> {

  private String name;
  private String decisionTable;
  private Resolvable ruleChain;
  private Coverage coverage;
  private int priority = 10;

  public boolean matches(CoverageInput input, String integratedCoverageId) {
    return ruleChain.resolve(input, integratedCoverageId);
  }

  public List<Condition> getRuleConditions() {
    List<Condition> conditions = new ArrayList<Condition>();
    EvaluationGroup group = (EvaluationGroup) ruleChain;
    group.appendRuleConditions(conditions);
    return conditions;
  }

  @Override
  public int compareTo(Rule o) {
    return this.priority > o.priority ? -1 : (this.priority < o.priority ? 1 : 0);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Coverage getCoverage() {
    return coverage;
  }

  public void setCoverage(Coverage coverage) {
    this.coverage = coverage;
    this.coverage.setAttachedRule(this);
  }

  public Resolvable getRuleChain() {
    return ruleChain;
  }

  public void setRuleChain(Resolvable ruleChain) {
    this.ruleChain = ruleChain;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getDecisionTable() {
    return decisionTable;
  }

  public void setDecisionTable(String decisionTable) {
    this.decisionTable = decisionTable;
  }

}
