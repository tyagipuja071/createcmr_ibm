package com.ibm.cio.cmr.utils.coverage.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a rule group containing one or more {@link Rule} instances
 * 
 * @author JeffZAMORA
 *
 */
public class RuleGroup {

  private int priority;
  private String description;
  private List<Rule> rules = new ArrayList<Rule>();

  public void addRule(Rule rule) {
    this.rules.add(rule);
  }

  public void addAllRules(List<Rule> rules) {
    this.rules.addAll(rules);
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<Rule> getRules() {
    return rules;
  }
}
