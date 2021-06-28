/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.rules;

/**
 * Represents an operation that can be associated with evaluating a
 * {@link Condition} resolvable
 * 
 * @author JeffZAMORA
 *
 */
public enum RuleOperation {

  Equals, NotEquals, In, NotIn, StartsWith, StartsOrEndsWith, EndsWith, GreaterThan, ParseCode, LessThan, GreaterThanOrEqual, LessThanOrEqual, LengthGreaterThan, LengthLessThan, LengthGreaterThanOrEqual, LengthLessThanOrEqual, LengthEqual;

  public static RuleOperation fromString(String operString) {
    switch (operString) {
    case "=":
      return Equals;
    case "!=":
      return NotEquals;
    case "<>":
      return NotEquals;
    case "sw":
      return StartsWith;
    case "sew":
      return StartsOrEndsWith;
    case "ew":
      return EndsWith;
    case ">":
      return GreaterThan;
    case ">=":
      return GreaterThanOrEqual;
    case "<":
      return LessThan;
    case "<=":
      return LessThanOrEqual;
    case "L>":
      return LengthGreaterThan;
    case "L>=":
      return LengthGreaterThanOrEqual;
    case "L<":
      return LengthLessThan;
    case "L<=":
      return LengthLessThanOrEqual;
    case "L=":
      return LengthEqual;
    case "pc":
      return ParseCode;
    case "in":
      return In;
    case "!in":
      return NotIn;
    default:
      throw new IllegalArgumentException("Unparseable operation string \"" + operString + "\"");
    }
  }
}
