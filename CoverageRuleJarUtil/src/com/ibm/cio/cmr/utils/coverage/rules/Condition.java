/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.utils.coverage.objects.CoverageInput;

/**
 * Represents a computable condition in the form of [field] [operation] [values]
 * 
 * @author JeffZAMORA
 *
 */
public class Condition implements Resolvable {

  private static final Logger LOG = Logger.getLogger(Condition.class);

  public static final String CURRENT_COVERAGE = "COVERAGE";
  public static final String IA_COVERAGE = "coverageID";
  public static final String BLANK = "BLANK";

  private String field;
  private List<String> values = new ArrayList<String>();
  private List<String> values2 = new ArrayList<String>();
  private RuleOperation operation;

  public Condition(String field, RuleOperation operation, String... values) {
    this.field = parseField(field);
    this.operation = operation;
    for (String value : values) {
      if (value != null && !value.trim().isEmpty() && !this.values.contains(value)) {
        this.values.add(value);
      }
    }
  }

  private String parseField(String rawField) {
    if (!rawField.contains(".")) {
      return rawField;
    }
    return rawField.substring(rawField.lastIndexOf(".") + 1).trim();
  }

  /**
   * Negates the current operation associated with the condition
   */
  public void negateOperation() {
    if (this.operation == null) {
      return;
    }
    RuleOperation negated = null;
    switch (this.operation) {
    case EndsWith:
      break;
    case Equals:
      negated = RuleOperation.NotEquals;
      break;
    case GreaterThan:
      negated = RuleOperation.LessThanOrEqual;
      break;
    case GreaterThanOrEqual:
      negated = RuleOperation.LessThan;
      break;
    case In:
      negated = RuleOperation.NotIn;
      break;
    case LengthEqual:
      break;
    case LengthGreaterThan:
      break;
    case LengthGreaterThanOrEqual:
      break;
    case LengthLessThan:
      break;
    case LengthLessThanOrEqual:
      break;
    case LessThan:
      negated = RuleOperation.GreaterThanOrEqual;
      break;
    case LessThanOrEqual:
      negated = RuleOperation.GreaterThan;
      break;
    case NotEquals:
      negated = RuleOperation.Equals;
      break;
    case NotIn:
      negated = RuleOperation.In;
      break;
    case ParseCode:
      break;
    case StartsOrEndsWith:
      break;
    case StartsWith:
      break;
    default:
      break;
    }
    if (negated != null) {
      this.operation = negated;
    }
  }

  @Override
  public boolean resolve(CoverageInput input, String integratedCoverageId) {
    String value = null;
    if (CURRENT_COVERAGE.equals(field) || IA_COVERAGE.equalsIgnoreCase(this.field)) {
      value = integratedCoverageId;
    } else {
      value = input.get(this.field);
    }
    boolean matches = false;
    switch (this.operation) {
    case EndsWith:
      matches = computeEndsWith(value, this.values);
      break;
    case Equals:
      matches = computeEquals(value, false);
      break;
    case GreaterThan:
      throw new UnsupportedOperationException("Operation " + this.operation.toString() + " not implemented.");
    case GreaterThanOrEqual:
      throw new UnsupportedOperationException("Operation " + this.operation.toString() + " not implemented.");
    case In:
      matches = computeIn(value, false);
      break;
    case LengthEqual:
      matches = computeLength(value, 0, true);
      break;
    case LengthGreaterThan:
      matches = computeLength(value, 1, false);
      break;
    case LengthGreaterThanOrEqual:
      matches = computeLength(value, 1, true);
      break;
    case LengthLessThan:
      matches = computeLength(value, -1, false);
      break;
    case LengthLessThanOrEqual:
      matches = computeLength(value, -1, true);
      break;
    case LessThan:
      throw new UnsupportedOperationException("Operation " + this.operation.toString() + " not implemented.");
    case LessThanOrEqual:
      throw new UnsupportedOperationException("Operation " + this.operation.toString() + " not implemented.");
    case NotEquals:
      matches = computeEquals(value, true);
      break;
    case NotIn:
      matches = computeIn(value, true);
      break;
    case ParseCode:
      matches = computeParseCode(value);
      break;
    case StartsOrEndsWith:
      boolean startsWith = computeStartsWith(value, this.values);
      if (startsWith) {
        matches = computeEndsWith(value, this.values2);
      }
      break;
    case StartsWith:
      matches = computeStartsWith(value, this.values);
      break;
    default:
      break;
    }
    LOG.trace(toString() + "  (Value: " + value + ", MATCH: " + matches + ")");
    return matches;
  }

  /**
   * Evaluates {@link RuleOperation#LengthEqual},
   * {@link RuleOperation#LengthGreaterThan},
   * {@link RuleOperation#LengthGreaterThanOrEqual},
   * {@link RuleOperation#LengthLessThan}, and
   * {@link RuleOperation#LengthLessThanOrEqual} operations
   * 
   * @param value
   * @param compareResult
   * @param inclusive
   * @return
   */
  private boolean computeLength(String value, int compareResult, boolean inclusive) {
    int length = 0;
    if (value != null) {
      length = value.trim().length();
    }
    try {
      Integer len1 = new Integer(length);
      Integer len2 = new Integer(Integer.parseInt(this.values.get(0).trim()));
      int result = len1.compareTo(len2);
      if (result == compareResult) {
        return true;
      } else if (result == 0 && inclusive) {
        return true;
      }
      return false;
    } catch (Exception e) {
      LOG.warn("length " + this.values.get(0) + " not a valid length");
      return true;
    }

  }

  /**
   * Evaluates {@link RuleOperation#ParseCode} operations
   * 
   * @param value
   * @return
   */
  private boolean computeParseCode(String value) {
    if (value == null) {
      value = "";
    }
    for (String parseCode : this.values) {
      if (parseCode.contains(" to ")) {
        String[] parts = parseCode.split("to");
        if (value.compareTo(parts[0].trim()) >= 0 && value.compareTo(parts[1].trim()) <= 0) {
          return true;
        }
      } else if (parseCode.toLowerCase().contains("x")) {
        String replace = parseCode.replace("x", "[A-Za-z0-9]");
        replace = replace.replace("X", "[A-Za-z0-9]").trim();
        if (value.trim().matches(replace)) {
          return true;
        }
        // String startsWith = parseCode.toLowerCase().substring(0,
        // parseCode.indexOf("x"));
        // if (value.startsWith(startsWith)) {
        // return true;
        // }
      }
    }
    return false;
  }

  /**
   * Evaluates {@link RuleOperation#StartsWith} operations
   * 
   * @param value
   * @param values
   * @return
   */
  private boolean computeStartsWith(String value, List<String> values) {
    if (value == null) {
      value = "";
    }
    for (String compareValue : values) {
      compareValue = compareValue.trim();
      if (compareValue.endsWith("*")) {
        compareValue = compareValue.substring(0, compareValue.indexOf("*"));
        if (value.startsWith(compareValue)) {
          return true;
        }
      } else if (compareValue.startsWith("!")) {
        if (!value.startsWith(compareValue.substring(1))) {
          return true;
        }
      } else {
        if (value.startsWith(compareValue)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * {@link RuleOperation#EndsWith}. Used in conjuction with
   * {@link #computeStartsWith(String, List)} to compute for
   * {@link RuleOperation#StartsOrEndsWith} operations
   * 
   * @param value
   * @param values
   * @return
   */
  private boolean computeEndsWith(String value, List<String> values) {
    if (value == null) {
      value = "";
    }
    for (String compareValue : values) {
      compareValue = compareValue.trim();
      if (compareValue.startsWith("*")) {
        compareValue = compareValue.substring(compareValue.indexOf("*"));
        if (value.endsWith(compareValue)) {
          return true;
        }
      } else if (compareValue.startsWith("!")) {
        if (!value.endsWith(compareValue.substring(1))) {
          return true;
        }
      } else {
        if (value.endsWith(compareValue)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Computes {@link RuleOperation#In} and {@link RuleOperation#NotIn}
   * 
   * @param value
   * @param notIn
   * @return
   */
  private boolean computeIn(String value, boolean notIn) {
    if (value == null) {
      value = "";
    }
    if (!notIn) {
      // in,only one match is a match
      for (String val : this.values) {
        if (val.startsWith("!")) {
          if (!val.trim().equals(value)) {
            return true;
          }
        } else {
          if (val.trim().equals(value)) {
            return true;
          }
        }
      }
    } else {
      for (String val : this.values) {
        if (val.startsWith("!")) {
          if (!val.trim().equals(value)) {
            return false;
          }
        } else {
          if (val.trim().equals(value)) {
            return false;
          }
        }

      }
      return true;
    }
    return false;
  }

  /**
   * {@link RuleOperation#Equals} and {@link RuleOperation#NotEquals} operations
   * 
   * @param value
   * @param notEquals
   * @return
   */
  private boolean computeEquals(String value, boolean notEquals) {
    if (this.values.isEmpty()) {
      return true;
    }
    String compareValue = this.values.get(0);
    if (value == null) {
      value = "";
    }
    if ("".equals(value.trim()) && BLANK.equals(compareValue)) {
      return notEquals ? false : true;
    }
    boolean matches = value.equals(compareValue);
    return notEquals ? !matches : matches;
  }

  /**
   * Gets the first value from the list that will satisy the
   * {@link RuleOperation} associated with this condition
   * 
   * @return
   */
  public String getFirstUsableValue() {
    String firstVal = !this.values.isEmpty() ? this.values.get(0) : null;

    switch (this.operation) {
    case EndsWith:
      if (firstVal.contains("*")) {
        return firstVal.substring(firstVal.indexOf("*"));
      } else {
        return firstVal;
      }
    case ParseCode:
      if (firstVal.contains(" to ")) {
        String[] parts = firstVal.split("to");
        return parts[0].trim();
      } else if (firstVal.contains("X")) {
        return firstVal.replace("X", "0");
      }
      return firstVal;
    case StartsOrEndsWith:
      if (firstVal.contains("*")) {
        return firstVal.substring(0, firstVal.indexOf("*"));
      } else {
        return firstVal;
      }
    case StartsWith:
      if (firstVal.contains("*")) {
        return firstVal.substring(0, firstVal.indexOf("*"));
      } else {
        return firstVal;
      }
    default:
      return firstVal;
    }
  }

  public boolean containsValue(String value) {
    if (this.values == null || value == null) {
      return false;
    }
    if (this.values.contains(value)) {
      return true;
    }
    for (String val : this.values) {
      if (val.trim().equals(value.trim())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isValid() {
    return this.field != null && this.operation != null && !this.values.isEmpty();
  }

  @Override
  public String toString() {
    return this.field + " " + (this.operation != null ? this.operation.toString() : "-") + " " + this.values
        + (!this.values2.isEmpty() ? ", " + this.values2 : "");
  }

  public void addValue(String value) {
    this.values.add(value);
  }

  public void addValue2(String value) {
    this.values2.add(value);
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public List<String> getValues() {
    return values;
  }

  public RuleOperation getOperation() {
    return operation;
  }

  public void setOperation(RuleOperation operation) {
    this.operation = operation;
  }

  public List<String> getValues2() {
    return values2;
  }

  @Override
  public void display(int nestLevel, StringBuilder displayBuilder) {
    displayBuilder.append("(" + toString() + ")");
  }

}
