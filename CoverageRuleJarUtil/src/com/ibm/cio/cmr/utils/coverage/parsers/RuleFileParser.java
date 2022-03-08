/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.utils.coverage.rules.Coverage;
import com.ibm.cio.cmr.utils.coverage.rules.Resolvable;
import com.ibm.cio.cmr.utils.coverage.rules.Rule;

/**
 * Parser class to read and parse IRL files
 * 
 * @author JeffZAMORA
 *
 */
public class RuleFileParser {

  private File ruleFile;

  // parsing variables
  private Rule currentRule;
  private boolean readingRule;
  private boolean readingEval;
  private StringBuilder evalBuilder = new StringBuilder();

  /**
   * 
   */
  public RuleFileParser(File ruleFile) {
    this.ruleFile = ruleFile;
  }

  /**
   * Extracts the list or {@link Rule} instances found on the given IRL file
   * 
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public List<Rule> extractRules() throws FileNotFoundException, IOException {
    List<Rule> rules = new ArrayList<Rule>();
    try (FileInputStream fis = new FileInputStream(this.ruleFile)) {
      try (InputStreamReader isr = new InputStreamReader(fis, "UTF-8")) {
        try (BufferedReader br = new BufferedReader(isr)) {
          String line = null;
          while ((line = br.readLine()) != null) {
            if (line.trim().startsWith("rule")) {
              if (this.currentRule != null) {
                if (this.currentRule.getCoverage() == null) {
                  throw new ExceptionInInitializerError(
                      "Coverage for rule " + this.currentRule.getName() + " under " + this.ruleFile.getAbsolutePath() + " not registered properly.");
                }
                rules.add(this.currentRule);
              }
              this.currentRule = new Rule();
              this.readingRule = true;
            }
            if (line.contains("property ilog.rules.business_name = ")) {
              String name = line.substring(line.indexOf("property ilog.rules.business_name") + 35);
              name = name.replace("\"", "");
              name = name.replace(";", "").trim();
              this.currentRule.setName(name);
            }
            if (line.contains("property ilog.rules.dt = ")) {
              String dt = line.substring(line.lastIndexOf("=") + 1);
              dt = dt.replace("\"", "");
              dt = dt.replace(";", "").trim();
              this.currentRule.setDecisionTable(dt);
            }
            if (line.trim().startsWith("priority = ")) {
              String priority = line.substring(line.lastIndexOf("=") + 1);
              priority = priority.replace(";", "");
              if (StringUtils.isNumeric(priority.trim())) {
                this.currentRule.setPriority(Integer.parseInt(priority.trim()));
              }
            }
            if (this.readingRule && line.trim().startsWith("evaluate")) {
              this.readingEval = true;
              this.evalBuilder.delete(0, this.evalBuilder.length());
              this.evalBuilder.append(line.trim()).append(" ");
            } else if (this.readingEval && !line.contains(" then ")) {
              this.evalBuilder.append(line.trim()).append(" ");
            }
            if (this.readingEval && line.contains(" then ")) {
              EvaluationParser evalParser = new EvaluationParser();
              String evalLine = evalBuilder.toString();
              Resolvable ruleChain = evalParser.extract(evalLine);
              this.currentRule.setRuleChain(ruleChain);
              this.readingEval = false;
            }
            if (this.readingRule && (line.trim().startsWith("coverageOutput.addCoverage") || line.trim().startsWith("coverageOutput.addUSCoverage")
                || line.trim().startsWith("coverageInput.delegationAuthorized"))) {
              Coverage coverage = CoverageParser.extract(line.trim());
              this.currentRule.setCoverage(coverage);
              this.readingRule = false;
            }
          }
          if (this.currentRule != null) {
            if (this.currentRule.getCoverage() == null) {
              throw new ExceptionInInitializerError(
                  "Coverage for rule " + this.currentRule.getName() + " under " + this.ruleFile.getAbsolutePath() + " not registered properly.");
            }
            rules.add(this.currentRule);
          }
        }
      }
    }
    return rules;
  }

}
