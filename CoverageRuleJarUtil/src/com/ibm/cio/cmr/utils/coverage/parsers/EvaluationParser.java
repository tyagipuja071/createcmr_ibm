/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.ibm.cio.cmr.utils.coverage.rules.Condition;
import com.ibm.cio.cmr.utils.coverage.rules.EvaluationGroup;
import com.ibm.cio.cmr.utils.coverage.rules.Resolvable;
import com.ibm.cio.cmr.utils.coverage.rules.RuleOperation;

/**
 * Parses class to extract {@link Resolvable} instances
 * 
 * @author JeffZAMORA
 *
 */
public class EvaluationParser {

  /**
   * Extracts a main {@link Resolvable} representing all {@link EvaluationGroup}
   * and {@link Condition} instances found on the <strong>evaluate</strong>
   * lines of IRL files
   * 
   * @param irlEvalLine
   * @return
   */
  public Resolvable extract(String irlEvalLine) {
    String evalLine = irlEvalLine.trim();

    evalLine = trimCode(evalLine);

    evalLine = minimizeFunctions(evalLine);

    return parseMinimizedEvalLine(evalLine);
  }

  /**
   * Removes the IRL specific code syntaxes
   * 
   * @param line
   * @return
   */
  private String trimCode(String line) {
    String evalLine = line;

    // remove hard-to-parse common expressions
    evalLine = evalLine.substring(0, evalLine.lastIndexOf(")") + 1);
    evalLine = evalLine.replaceAll("com\\.ibm\\.ilog\\.Util\\.isStartsOrEndsWith", "isStartsOrEndsWith");
    evalLine = evalLine.replaceAll("com\\.ibm\\.ilog\\.Util\\.isStartsWith", "isStartsWith");
    evalLine = evalLine.replaceAll("com\\.ibm\\.ilog\\.Util\\.isEndsWith", "isEndsWith");
    evalLine = evalLine.replaceAll("com\\.ibm\\.ilog\\.Util\\.isParseCode", "isParseCode");
    evalLine = evalLine.replaceAll("com\\.ibm\\.ilog\\.Util\\.isNotOneOf", "isNotOneOf");
    evalLine = evalLine.replaceAll("new java\\.lang\\.String\\[\\]", "");

    // evalLine = evalLine.replaceAll("\\.equals", " in ");
    evalLine = evalLine.replaceAll("\\(' '\\)", "\" \"");

    // evalLine =
    // evalLine.replaceAll("\\(\\(coverageInput.delegationAuthorized\\) ==
    // \\(false\\) && var\\$_\\$0.size\\(\\) == 0\\)\\)", "COVERAGE = BLANK");
    // evalLine =
    // evalLine.replaceAll("\\(\\(\\(coverageInput.delegationAuthorized\\) ==
    // \\(true\\) && var$_$1.size\\(\\) <= 1\\)\\)","COVERAGE = BLANK");

    evalLine = evalLine.replaceAll("\\(coverageInput.delegationAuthorized\\) == \\(false\\)", "COVERAGE = BLANK");
    evalLine = evalLine.replaceAll("\\(coverageInput.delegationAuthorized\\) == \\(true\\)", "COVERAGE = BLANK");
    evalLine = evalLine.replaceAll("var\\$_\\$0\\.size\\(\\) <= 1", "COVERAGE = BLANK");
    evalLine = evalLine.replaceAll("var\\$_\\$1\\.size\\(\\) <= 1", "COVERAGE = BLANK");
    evalLine = evalLine.replaceAll("var\\$_\\$2\\.size\\(\\) <= 1", "COVERAGE = BLANK");
    evalLine = evalLine.replaceAll("var\\$_\\$0\\.size\\(\\) == 0", "COVERAGE = BLANK");
    evalLine = evalLine.replaceAll("var\\$_\\$1\\.size\\(\\) == 0", "COVERAGE = BLANK");
    evalLine = evalLine.replaceAll("var\\$_\\$2\\.size\\(\\) == 0", "COVERAGE = BLANK");
    evalLine = evalLine.replaceAll("var\\$_\\$0\\.size\\(\\) == 1", "COVERAGE = BLANK");
    evalLine = evalLine.replaceAll("var\\$_\\$1\\.size\\(\\) == 1", "COVERAGE = BLANK");
    evalLine = evalLine.replaceAll("var\\$_\\$1\\.size\\(\\) == 2", "COVERAGE = BLANK");

    evalLine = evalLine.replaceAll("==", "=");
    evalLine = evalLine.replaceAll(" null ", " BLANK ");
    if (evalLine.contains("var$_$1.size() = 0") && evalLine.contains("var$_$0.size() = 0")) {
      evalLine = evalLine.replaceAll("var\\$_\\$0\\.size\\(\\) = 0", "coverageID = BLANK");
      evalLine = evalLine.replaceAll("var\\$_\\$1\\.size\\(\\) = 0", "COVERAGE = BLANK");
      evalLine = evalLine.replaceAll("var\\$_\\$1\\.size\\(\\) = 0", "COVERAGE = BLANK");
    } else {
      evalLine = evalLine.replaceAll("var\\$_\\$0\\.size\\(\\) = 0", "COVERAGE = BLANK");
      evalLine = evalLine.replaceAll("var\\$_\\$1\\.size\\(\\) = 0", "COVERAGE = BLANK");
    }

    evalLine = evalLine.replace("('  ')", "\" \"");
    return evalLine;
  }

  /**
   * Converts the condition statements found on the line in the format readable
   * by the {@link ConditionParser}
   * 
   * @param line
   * @return
   */
  private String minimizeFunctions(String line) {
    String evalLine = line;
    StartsWithEndsWithContainer cont = extractILOGStartsOrEndsWith(evalLine);
    if (cont.matched) {
      do {
        evalLine = evalLine.replace(cont.match, cont.replacement);

        cont = extractILOGStartsOrEndsWith(evalLine);

      } while (cont.matched);
    }

    GenericReplacementContainer replaceCont = extractEqualsOrStartsWith(evalLine);
    if (replaceCont.matched) {
      do {
        evalLine = evalLine.replace(replaceCont.match, replaceCont.replacement);

        replaceCont = extractEqualsOrStartsWith(evalLine);

      } while (replaceCont.matched);
    }

    replaceCont = extractParseCodes(evalLine);
    if (replaceCont.matched) {
      do {
        evalLine = evalLine.replace(replaceCont.match, replaceCont.replacement);

        replaceCont = extractParseCodes(evalLine);

      } while (replaceCont.matched);
    }

    replaceCont = extractLength(evalLine);
    if (replaceCont.matched) {
      do {
        evalLine = evalLine.replace(replaceCont.match, replaceCont.replacement);

        replaceCont = extractLength(evalLine);

      } while (replaceCont.matched);
    }

    replaceCont = convertCharAt(evalLine);
    if (replaceCont.matched) {
      do {
        evalLine = evalLine.replace(replaceCont.match, replaceCont.replacement);

        replaceCont = convertCharAt(evalLine);

      } while (replaceCont.matched);
    }

    return evalLine;
  }

  /**
   * Extracts an instance of a call to ILOG's <em>isStartsWith</em>,
   * <em>isEndsWith</em>, <em>isStartsOrEndsWith</em>, or <em>isNotOneOf</em>
   * calls
   * 
   * @param line
   * @return
   */
  private StartsWithEndsWithContainer extractILOGStartsOrEndsWith(String line) {
    StartsWithEndsWithContainer cont = new StartsWithEndsWithContainer();
    if (!line.contains("Starts") && !line.contains("Ends") && !line.contains("OneOf")) {
      return cont;
    }

    // get the starts with line
    String matchPhrase = null;

    String checkPhrase = null;
    String oper = null;
    if (line.contains("isStartsOrEndsWith")) {
      checkPhrase = "isStartsOrEndsWith";
      oper = " sew ";
    } else if (line.contains("isStartsWith")) {
      checkPhrase = "isStartsWith";
      oper = " sw ";
    } else if (line.contains("isEndsWith")) {
      checkPhrase = "isEndsWith";
      oper = " ew ";
    } else if (line.contains("isOneOf")) {
      checkPhrase = "isOneOf";
      oper = " in ";
    } else if (line.contains("isNotOneOf")) {
      checkPhrase = "isNotOneOf";
      oper = " !in ";
    }
    if (line.contains(checkPhrase)) {
      cont.matched = true;
      matchPhrase = line.substring(line.indexOf(checkPhrase));
      if (matchPhrase.contains("('  ')")) {
        matchPhrase = matchPhrase.replace("('  ')", "\"\"");
      }
      matchPhrase = matchPhrase.substring(0, matchPhrase.indexOf(")") + 1);
      cont.match = matchPhrase;

      String params = matchPhrase.substring(matchPhrase.indexOf("(") + 1, matchPhrase.indexOf(")"));
      cont.field = params.substring(0, params.indexOf(",")).trim();
      params = params.substring(params.indexOf(",") + 1) + " ";
      cont.value1 = params.substring(0, params.indexOf("}") + 1).trim();
      params = params.substring(params.indexOf("}") + 2);
      if (params.contains("{")) {
        cont.value2 = params.trim();
      }
      cont.replacement = cont.field + oper + cont.value1 + (cont.value2 != null ? "-" + cont.value2 : "");
    }

    return cont;
  }

  /**
   * Extracts an instance of a call to ILOG's <em>isParseCode</em> call
   * 
   * @param line
   * @return
   */
  private GenericReplacementContainer extractParseCodes(String line) {
    GenericReplacementContainer cont = new GenericReplacementContainer();
    if (!line.contains("isParseCode")) {
      return cont;
    }

    cont.matched = true;
    String matchPhrase = line.substring(line.indexOf("isParseCode"));
    matchPhrase = matchPhrase.substring(0, matchPhrase.indexOf(")") + 1);
    cont.match = matchPhrase;

    String params = matchPhrase.substring(matchPhrase.indexOf("(") + 1, matchPhrase.indexOf(")"));
    cont.field = params.substring(0, params.indexOf(",")).trim();
    params = params.substring(params.indexOf(",") + 1) + " ";
    cont.value = params.substring(0, params.lastIndexOf("\"") + 1).trim();
    cont.replacement = cont.field + " pc " + cont.value;

    return cont;
  }

  /**
   * Extacts an instance to the code call to <em>.equals</em> or
   * <em>.startsWith</em> functions
   * 
   * @param line
   * @return
   */
  private GenericReplacementContainer extractEqualsOrStartsWith(String line) {
    GenericReplacementContainer cont = new GenericReplacementContainer();

    if (!line.contains(".equals") && !line.contains(".startsWith")) {
      return cont;
    }

    String operPhrase = null;
    if (line.contains(".equals")) {
      operPhrase = ".equals";
    } else if (line.contains(".startsWith")) {
      operPhrase = ".startsWith";
    }
    cont.matched = true;
    String equals = line.substring(line.indexOf(operPhrase) + operPhrase.length() + 1);
    equals = equals.substring(0, equals.indexOf(")"));
    cont.value = equals;
    String matchPhrase = line.substring(0, line.indexOf(operPhrase));
    List<String> temp = Arrays.asList(matchPhrase.split(""));
    Collections.reverse(temp);
    List<String> fieldBuilder = new ArrayList<>();
    for (String letter : temp) {
      if (!" ".equals(letter) && !"(".equals(letter) && !"&".equals(letter)) {
        fieldBuilder.add(letter);
      } else {
        break;
      }
    }
    Collections.reverse(fieldBuilder);
    String field = "";
    for (String letter : fieldBuilder) {
      field += letter;
    }
    cont.field = field;
    String oper = " = ";
    boolean addNot = false;
    if (cont.field.startsWith("!")) {
      cont.field = cont.field.substring(1);
      oper = " != ";
      addNot = true;
    }
    cont.match = (addNot ? "!" : "") + cont.field + operPhrase + "(" + cont.value + ")";
    cont.replacement = cont.field + oper + cont.value;

    return cont;
  }

  /**
   * Extracts an instance of a call to the <em>.length()</em> function
   * 
   * @param line
   * @return
   */
  private GenericReplacementContainer extractLength(String line) {
    GenericReplacementContainer cont = new GenericReplacementContainer();

    if (!line.contains(".length()")) {
      return cont;
    }

    cont.matched = true;
    String equals = line.substring(line.indexOf(".length()") + 9);
    cont.value = "";
    for (String curr : equals.split("")) {
      if (!curr.equals("&") && !curr.equals("|") && !curr.equals(")")) {
        cont.value += curr;
      } else {
        break;
      }
    }
    String matchPhrase = line.substring(0, line.indexOf(".length"));
    List<String> temp = Arrays.asList(matchPhrase.split(""));
    Collections.reverse(temp);
    List<String> fieldBuilder = new ArrayList<>();
    for (String letter : temp) {
      if (!" ".equals(letter) && !"(".equals(letter) && !"&".equals(letter)) {
        fieldBuilder.add(letter);
      } else {
        break;
      }
    }
    Collections.reverse(fieldBuilder);
    String field = "";
    for (String letter : fieldBuilder) {
      field += letter;
    }
    cont.field = field;
    cont.match = cont.field + ".length()" + cont.value;
    cont.value = cont.value.trim();
    if (!cont.value.contains(" ")) {
      String temp1 = "";
      boolean hasOper = false;
      for (String s : cont.value.split("")) {
        if ("=".equals(s) || ">".equals(s) || "<".equals(s) || " ".equals(s)) {
          temp1 += s;
          hasOper = true;
        } else {
          if (!temp1.contains(" ") && hasOper) {
            temp1 += " ";
          }
          temp1 += s;
        }
      }
      cont.value = temp1;
    }
    cont.replacement = cont.field + " L" + cont.value.trim() + " ";

    return cont;
  }

  /**
   * Extracts an instance of a call to <em>.charAt(N)</em> to a
   * {@link RuleOperation#StartsWith} structure
   * 
   * @param line
   * @return
   */
  private GenericReplacementContainer convertCharAt(String line) {
    GenericReplacementContainer cont = new GenericReplacementContainer();

    if (!line.contains(".charAt(0)")) {
      return cont;
    }

    cont.matched = true;
    String equals = line.substring(line.indexOf(".charAt(0)") + 10);
    cont.value = "";
    for (String curr : equals.split("")) {
      if (!curr.equals("&") && !curr.equals("|") && !curr.equals(")")) {
        cont.value += curr;
      } else {
        break;
      }
    }
    String matchPhrase = line.substring(0, line.indexOf(".charAt"));
    List<String> temp = Arrays.asList(matchPhrase.split(""));
    Collections.reverse(temp);
    List<String> fieldBuilder = new ArrayList<>();
    for (String letter : temp) {
      if (!" ".equals(letter) && !"(".equals(letter) && !"&".equals(letter)) {
        fieldBuilder.add(letter);
      } else {
        break;
      }
    }
    Collections.reverse(fieldBuilder);
    String field = "";
    for (String letter : fieldBuilder) {
      field += letter;
    }
    cont.field = field;
    cont.match = cont.field + ".charAt(0)" + cont.value;
    cont.value = cont.value.replaceAll("=", "");
    cont.value = cont.value.replaceFirst("'", "{\"");
    cont.value = cont.value.replaceFirst("'", "\"}");
    cont.replacement = cont.field + " sw " + cont.value.trim() + " ";

    return cont;
  }

  /**
   * Parses the main {@link EvaluationGroup} as a {@link Resolvable} instance
   * based on the simplified and minified evaluation line
   * 
   * @param evalLine
   * @return
   */
  private Resolvable parseMinimizedEvalLine(String evalLine) {
    if (!evalLine.startsWith("evaluate")) {
      throw new IllegalArgumentException("Evaluation line not valid: " + evalLine);
    }
    Map<Integer, Condition> condMap = new HashMap<Integer, Condition>();
    Map<Integer, String> condLineMap = new HashMap<Integer, String>();

    Condition cond = null;

    // first iteration: map conditions on the line and minimize the eval line
    evalLine = evalLine.trim();
    boolean readCond = false;
    StringBuilder condBuilder = new StringBuilder();
    int condIndex = 0;
    int andOrCount = 0;
    for (String token : evalLine.split("")) {
      if (("&".equals(token) || "|".equals(token))) {
        andOrCount++;
      }
      if ("&".equals(token) || "|".equals(token) || ")".equals(token)) {
        if (readCond) {
          if (!condBuilder.toString().trim().isEmpty()) {
            cond = ConditionParser.parseCondition(condBuilder.toString().trim());
            condMap.put(condIndex, cond);
            condLineMap.put(condIndex, condBuilder.toString().trim());
            condIndex++;
          }
          condBuilder.delete(0, condBuilder.length());
          readCond = false;
        }
      }
      if (readCond) {
        condBuilder.append(token);
      }
      if ("(".equals(token) || andOrCount == 2) {
        readCond = true;
        condBuilder.delete(0, condBuilder.length());
        andOrCount = 0;
      }
    }

    for (Integer index : condLineMap.keySet()) {
      String condLine = condLineMap.get(index);
      String replacement = index + "";
      evalLine = evalLine.replace(condLine, replacement);
      evalLine = evalLine.replace("(" + replacement + ")", replacement);
    }
    evalLine = evalLine.replace("&&", "&");
    evalLine = evalLine.replace("||", "|");
    evalLine = evalLine.replace("( ", "(");
    evalLine = evalLine.replace(" )", ")");
    evalLine = evalLine.trim();
    evalLine = evalLine.substring("evaluate".length());

    // second iteration parse as objects

    EvaluationGroup currEval = null;
    Stack<Resolvable> evalStack = new Stack<>();
    EvaluationGroup mainGroup = null;
    String currLogicalOper = null;

    for (String token : evalLine.split("")) {
      if (!"".equals(token)) {
        if ("(".equals(token)) {
          currEval = new EvaluationGroup();
          evalStack.push(currEval);
          condBuilder.delete(0, condBuilder.length());
        } else if ("&".equals(token) || "|".equals(token) || ")".equals(token)) {
          if ("&".equals(token) || "|".equals(token)) {
            currLogicalOper = token;
          }
          if (!condBuilder.toString().trim().isEmpty()) {
            boolean negate = false;
            String condKey = condBuilder.toString().trim();
            if (condKey.startsWith("!")) {
              condKey = condKey.substring(1);
              negate = true;
            }
            int index = Integer.parseInt(condKey);
            cond = condMap.get(index);
            if (negate) {
              cond.negateOperation();
            }
            currEval = (EvaluationGroup) evalStack.peek();
            currEval.setOr("|".equals(currLogicalOper));
            currEval.addCondition(cond);
          }
          condBuilder.delete(0, condBuilder.length());
          if (")".equals(token)) {
            currLogicalOper = null;
            EvaluationGroup group = (EvaluationGroup) evalStack.pop();
            if (!evalStack.isEmpty()) {
              EvaluationGroup parent = (EvaluationGroup) evalStack.peek();
              if (parent != null && group != null) {
                parent.addCondition(group);
              }
              mainGroup = parent;
            }
          }
        } else {
          condBuilder.append(token);
        }
      }

    }

    mainGroup.clean();
    // displayChain(mainGroup, 1);
    return mainGroup;
  }

  /**
   * Helper method just to display the contents of the {@link Resolvable}
   * instance
   * 
   * @param resolvable
   * @param level
   */
  protected void displayChain(Resolvable resolvable, int level, StringBuilder displayBuilder) {
    if (resolvable instanceof Condition) {
      Condition condition = (Condition) resolvable;
      condition.display(level, displayBuilder);
    } else if (resolvable instanceof EvaluationGroup) {
      EvaluationGroup group = (EvaluationGroup) resolvable;
      for (Resolvable nestedResolvable : group.getConditions()) {
        displayChain(nestedResolvable, level + 1, displayBuilder);
      }
    }
  }

  private class StartsWithEndsWithContainer {
    private String match;
    private String field;
    private String value1;
    private String value2;
    private boolean matched;
    private String replacement;

  }

  private class GenericReplacementContainer {
    private String match;
    private String field;
    private String value;
    private boolean matched;
    private String replacement;

  }

}
