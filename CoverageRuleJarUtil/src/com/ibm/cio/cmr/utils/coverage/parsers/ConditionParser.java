/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.parsers;

import java.util.ArrayList;
import java.util.List;

import com.ibm.cio.cmr.utils.coverage.rules.Condition;
import com.ibm.cio.cmr.utils.coverage.rules.RuleOperation;

/**
 * Parser class for conditions extraction
 * 
 * @author JeffZAMORA
 *
 */
public class ConditionParser {

  /**
   * Parses a condition statement in the form of [field] [operation]
   * [values]-{values2}
   * 
   * @param line
   * @return
   */
  public static Condition parseCondition(String line) {
    Condition cond = null;
    try {
      if (line.contains(" ")) {
        String field = line.substring(0, line.indexOf(" "));
        line = line.substring(line.indexOf(" ")).trim();

        String oper = line.substring(0, line.indexOf(" ")).trim();
        line = line.substring(line.indexOf(" ")).trim();

        String valueString = line;
        String valueString2 = null;
        RuleOperation operation = RuleOperation.fromString(oper);
        if (RuleOperation.StartsOrEndsWith.equals(operation)) {
          String[] valParts = valueString.split("\\-");
          valueString = valParts[0];
          valueString2 = valParts[1];
        }
        List<String> values = new ArrayList<>();
        if (valueString.contains("{")) {
          valueString = valueString.replace("{", "");
          valueString = valueString.replace("}", "");
        }
        valueString = valueString.replaceAll("\"", "");
        for (String value : valueString.split(",")) {
          values.add(value);
        }
        cond = new Condition(field, operation, values.toArray(new String[0]));

        if (valueString2 != null) {
          values = new ArrayList<>();
          if (valueString2.contains("{")) {
            valueString2 = valueString2.replace("{", "");
            valueString2 = valueString2.replace("}", "");
          }
          valueString2 = valueString2.replaceAll("\"", "");
          for (String value : valueString2.split(",")) {
            cond.addValue2(value);
          }
        }
        return cond;
      }
    } catch (Exception e) {
      System.err.println("Warning: Cannot parse condition: \"" + line + "\"");
    }
    return null;
  }
}
