/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.utils.coverage.objects.CoverageInput;

/**
 * Represents a group of {@link Resolvable} instances that can be evaluated
 * 
 * @author JeffZAMORA
 *
 */
public class EvaluationGroup implements Resolvable {

  private boolean or;
  private List<Resolvable> conditions = new ArrayList<Resolvable>();

  public void appendRuleConditions(List<Condition> ruleConditions) {
    for (Resolvable r : this.conditions) {
      if (r instanceof Condition) {
        ruleConditions.add((Condition) r);
      } else if (r instanceof EvaluationGroup) {
        ((EvaluationGroup) r).appendRuleConditions(ruleConditions);
      }
    }
  }

  public void addCondition(Resolvable condition) {
    if (condition.isValid()) {
      this.conditions.add(condition);
    }
  }

  public boolean hasConditionStatements() {
    for (Resolvable r : this.conditions) {
      if (r instanceof Condition) {
        return true;
      }
    }
    return false;
  }

  public void clean() {
    List<Resolvable> cleaned = new ArrayList<>();
    for (Resolvable r : conditions) {
      if (r.isValid()) {
        cleaned.add(r);
      }
    }
    this.conditions = cleaned;
  }

  @Override
  public boolean resolve(CoverageInput input, String integratedCoverageId) {
    int satisfyCount = 0;
    int nonSatisfyCount = 0;
    for (Resolvable resolvable : this.conditions) {
      boolean result = resolvable.resolve(input, integratedCoverageId);
      if (result) {
        satisfyCount++;
      } else {
        nonSatisfyCount++;
      }
    }
    boolean matched = this.or ? satisfyCount > 0 : nonSatisfyCount == 0;
    return matched;
  }

  @Override
  public boolean isValid() {
    return !this.conditions.isEmpty();
  }

  public boolean isOr() {
    return or;
  }

  public void setOr(boolean or) {
    this.or = or;
  }

  public List<Resolvable> getConditions() {
    return conditions;
  }

  @Override
  public void display(int nestLevel, StringBuilder displayBuilder) {
    int count = 0;
    for (Resolvable resolvable : this.conditions) {
      if (count > 0) {
        if (this.or) {
          displayBuilder.append(" OR ");
        } else {
          displayBuilder.append(" AND ");
        }
      }
      if (resolvable instanceof EvaluationGroup) {
        EvaluationGroup g = (EvaluationGroup) resolvable;
        if (g.hasConditionStatements()) {
          displayBuilder.append("\n");
          String pad = StringUtils.leftPad("", nestLevel);
          displayBuilder.append(pad);
        }
        resolvable.display(nestLevel + 1, displayBuilder);
      } else {
        resolvable.display(nestLevel, displayBuilder);
      }
      count++;
    }
    displayBuilder.append("\n");
  }

}
