package com.ibm.cio.cmr.utils.coverage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.utils.coverage.archive.RulesArchive;
import com.ibm.cio.cmr.utils.coverage.archive.RulesContainer;
import com.ibm.cio.cmr.utils.coverage.objects.CoverageInput;
import com.ibm.cio.cmr.utils.coverage.parsers.RuleFileParser;
import com.ibm.cio.cmr.utils.coverage.rules.Condition;
import com.ibm.cio.cmr.utils.coverage.rules.Coverage;
import com.ibm.cio.cmr.utils.coverage.rules.Rule;
import com.ibm.cio.cmr.utils.coverage.rules.RuleGroup;

/**
 * Contains all coverage rules for a given instance. This class can only
 * accommodate one ruleset at any given time. If needed, create several
 * instances of the class to store different rulesets
 * 
 * @author JeffZAMORA
 *
 */
public class CoverageRules {

  private static final Logger LOG = Logger.getLogger(CoverageRules.class);
  private String rulesetId;
  private List<Rule> integratedCovRules;
  private List<Rule> delegationRules;
  private List<RuleGroup> countryRules = new ArrayList<RuleGroup>();
  private Map<String, List<Rule>> coverageMap = new LinkedHashMap<String, List<Rule>>();

  private boolean initialized;

  /**
   * Dummy main method for testing
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    LOG.info("Starting..");
    CoverageRules cov = new CoverageRules("1H2022");
    String zipFile = "C:/ci/shared/data/batch/coverage/zip/1H2022.jar";
    // cov.initializeFrom(zipFile);
    cov.initialize();

    String id = "T0001732";
    List<Rule> rules1 = cov.findRule(id);
    if (rules1 != null) {
      System.out.println("Rules for " + id);
      for (Rule rule : rules1) {
        for (Condition c : rule.getRuleConditions()) {
          System.out.println(c.getField() + " " + c.getOperation() + " " + c.getValues());
        }
        System.out.println();
      }
    } else {
      System.out.println("No rules found");
    }
    CoverageInput input = new CoverageInput();
    input.setCountryCode("US");
    List<Coverage> covs = cov.findCoverage(input);
    for (Coverage c : covs) {
      System.out.println(c.getType() + c.getId() + " : ");
      for (Condition c1 : c.getAttachedRule().getRuleConditions()) {
        System.out.println(c1.getField() + " " + c1.getOperation() + " " + c1.getValues());
      }

    }

  }

  /**
   * Does a system out of the current display. <em>Only for testing</em>
   * 
   * @param display
   * @return
   * @throws IOException
   */
  protected static String cleanDisplay(String display) throws IOException {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(display.getBytes())) {
      try (InputStreamReader isr = new InputStreamReader(bis, "UTF-8")) {
        try (BufferedReader br = new BufferedReader(isr)) {
          StringBuilder clean = new StringBuilder();
          String line = null;
          while ((line = br.readLine()) != null) {
            if (!"".equals(line.trim())) {
              clean.append(line).append("\n");
            }
          }
          return clean.toString();
        }
      }
    }
  }

  public CoverageRules(String rulesetId) {
    this.rulesetId = rulesetId;
  }

  /**
   * Initializes the coverage rules based on the location of the zipped archive
   * or raw rules jar. The full zip or rule jar is extracted into the configured
   * unpack root location in <strong>jar.unpacked.dir</strong> property of
   * rulejar.properties.
   * 
   * @param jarOrZipLocation
   * @throws Exception
   */
  public synchronized void initializeFrom(String jarOrZipLocation) throws Exception {
    LOG.debug("Initializing rules from " + jarOrZipLocation);
    if (isUnpacked()) {
      LOG.debug("Ruleset " + this.rulesetId + " already unpacked.");
    } else {
      File rulesJar = null;
      RulesArchive archive = new RulesArchive();
      File jarContentLoc = new File(JarProperties.getUnpackRootLocation() + File.separator + this.rulesetId);
      if (jarOrZipLocation.toLowerCase().endsWith(".zip")) {
        String zipTempLoc = JarProperties.getZipTempLocation();
        LOG.info("Unpacking ZIP file to " + zipTempLoc);
        rulesJar = archive.unpackArchivedRuleset(jarOrZipLocation, zipTempLoc);
        if (rulesJar == null) {
          throw new FileNotFoundException("The ruleset jar file was not found on this archive.");
        }
      } else {
        rulesJar = new File(jarOrZipLocation);
      }

      if (!jarContentLoc.exists()) {
        jarContentLoc.mkdirs();
      }
      LOG.info("Unpacking JAR file to " + jarContentLoc.getAbsolutePath());
      archive.unpackArchivedRuleset(rulesJar, jarContentLoc);

    }

    initialize();

  }

  /**
   * Initializes the coverage rules. Reads the IRL files and stores the coverage
   * information to cache
   * 
   * @throws Exception
   */
  public synchronized void initialize() throws Exception {
    if (!isUnpacked()) {
      throw new Exception("No coverage rules for " + this.rulesetId + " unpacked. The rules need to be initialized first from a ZIP or JAR.");
    }
    RulesArchive archive = new RulesArchive();
    File jarContentLoc = new File(JarProperties.getUnpackRootLocation() + File.separator + this.rulesetId);

    LOG.info("Extracting rules from " + jarContentLoc);
    RulesContainer container = archive.extractRuleFiles(jarContentLoc.getAbsolutePath());
    for (File f : container.getIntegratedCoverageFiles()) {
      RuleFileParser p = new RuleFileParser(f);
      this.integratedCovRules = p.extractRules();
      Collections.sort(this.integratedCovRules);
      for (Rule r : this.integratedCovRules) {
        if (this.coverageMap.get(r.getCoverage().getType() + r.getCoverage().getId()) == null) {
          this.coverageMap.put(r.getCoverage().getType() + r.getCoverage().getId(), new ArrayList<Rule>());
        }
        this.coverageMap.get(r.getCoverage().getType() + r.getCoverage().getId()).add(r);
      }
    }
    if (this.integratedCovRules == null) {
      this.integratedCovRules = new ArrayList<Rule>();
    }
    for (File f : container.getDelegationFiles()) {
      RuleFileParser p = new RuleFileParser(f);
      this.delegationRules = p.extractRules();
      Collections.sort(this.delegationRules);
    }

    int priority = 1;
    LOG.trace("Size of country coverage files: " + container.getCountryCoverageFiles().size());
    for (String key : container.getCountryCoverageFiles().keySet()) {
      RuleGroup ruleGroup = new RuleGroup();
      ruleGroup.setDescription(key);
      ruleGroup.setPriority(priority);
      for (File f : container.getCountryCoverageFiles().get(key)) {
        LOG.trace(" - parsing " + f);
        RuleFileParser p = new RuleFileParser(f);
        ruleGroup.addAllRules(p.extractRules());
      }
      Collections.sort(ruleGroup.getRules());
      for (Rule r : ruleGroup.getRules()) {
        // String cov = r.getCoverage().getType() + r.getCoverage().getId();
        if (this.coverageMap.get(r.getCoverage().getType() + r.getCoverage().getId()) == null) {
          this.coverageMap.put(r.getCoverage().getType() + r.getCoverage().getId(), new ArrayList<Rule>());
        }
        this.coverageMap.get(r.getCoverage().getType() + r.getCoverage().getId()).add(r);
        if ("I".equals(r.getCoverage().getType())) {
          this.integratedCovRules.add(r);
        }
      }
      this.countryRules.add(ruleGroup);
      priority++;
    }

    this.initialized = true;
  }

  /**
   * Checks if there is a valid unpacked location for the given ruleset
   * 
   * @return
   */
  private boolean isUnpacked() {
    File rulesDir = new File(JarProperties.getUnpackRootLocation() + File.separator + this.rulesetId);
    if (!rulesDir.exists()) {
      return false;
    }

    RulesArchive archive = new RulesArchive();
    RulesContainer container = archive.extractRuleFiles(rulesDir.getAbsolutePath());
    return container.isValid();

  }

  /**
   * Finds the coverage based on input
   * 
   * @param input
   * @return
   */
  public List<Coverage> findCoverage(CoverageInput input) {
    if (!initialized) {
      throw new RuntimeException(
          "The rules have not been initialized. The rules should be initialized from a file for first load, or a call to initialize() should have been made if rules are already loaded.");
    }

    Coverage intCoverage = null;
    Coverage countryCoverage = null;
    // check int cov first
    for (Rule rule : this.integratedCovRules) {
      if (rule.matches(input, null)) {
        intCoverage = rule.getCoverage().clone();
        break;
      }
    }
    if (intCoverage != null) {
      // there is an integrated coverage, need to check delegation
      String iaId = intCoverage.getId();
      boolean delegationAuthorized = false;
      for (Rule rule : this.delegationRules) {
        if (rule.matches(input, iaId)) {
          LOG.debug("Delegation Rule: " + rule.getName() + " (Priority:" + rule.getPriority() + ")");
          List<Condition> conditions = rule.getRuleConditions();
          for (Condition c : conditions) {
            LOG.debug(" - " + c.toString());
          }

          intCoverage.setDelegationAuthorized(true);
          delegationAuthorized = true;
          break;
        }
      }
      LOG.debug("Delegation Authorized: " + delegationAuthorized);
      if (delegationAuthorized) {
        countryCoverage = findCountryCoverage(input, null);
        if (countryCoverage != null) {
          countryCoverage.setDelegationAuthorized(true);
        }
      }
    } else {
      // no IA account, find country cov only
      countryCoverage = findCountryCoverage(input, null);
    }
    List<Coverage> coverages = new ArrayList<Coverage>();
    if (intCoverage != null) {
      if (countryCoverage != null) {
        intCoverage.setLevel("Final");
      } else {
        intCoverage.setLevel("Base/Final");
      }
      coverages.add(intCoverage);
    }
    if (countryCoverage != null) {
      if (intCoverage != null) {
        countryCoverage.setLevel("Base");
      } else {
        countryCoverage.setLevel("Base/Final");
      }
      coverages.add(countryCoverage);
    }
    return coverages;
  }

  private Coverage findCountryCoverage(CoverageInput input, String integratedCoverageId) {
    for (RuleGroup group : this.countryRules) {
      for (Rule rule : group.getRules()) {
        if (rule.matches(input, integratedCoverageId)) {
          return rule.getCoverage().clone();
        }
      }
    }
    return null;
  }

  /**
   * Tries to match the current input with a target coverage ID
   * 
   * @param input
   * @param coverageTypeAndId
   * @return
   */
  public List<Rule> matchWithCoverage(CoverageInput input, String coverageTypeAndId) {
    LOG.debug("Trying to match against Coverage " + coverageTypeAndId);
    List<Rule> rules = findRule(coverageTypeAndId);

    List<Rule> matchedRules = new ArrayList<Rule>();
    for (Rule rule : rules) {
      boolean match = rule.matches(input, null);
      if (match) {
        matchedRules.add(rule);
      }
    }
    return matchedRules;
  }

  /**
   * 
   * @param coverageTypeAndId
   * @return
   */
  public List<Rule> findRule(String coverageTypeAndId) {
    List<Rule> rule = this.coverageMap.get(coverageTypeAndId);
    // if (rule == null || rule.isEmpty()) {
    // throw new IllegalArgumentException("Coverage " + coverageTypeAndId + "
    // not found in current rules.");
    // }
    Collections.sort(rule);
    return rule;
  }

  /**
   * Checks all rules and finds all fields that affect coverage calculation for
   * the given country
   * 
   * @param country
   * @return
   */
  public List<String> findCoverageFields(String country) {
    List<String> fields = new ArrayList<String>();
    for (RuleGroup group : this.countryRules) {
      for (Rule rule : group.getRules()) {

        boolean addRule = false;
        for (Condition cond : rule.getRuleConditions()) {
          if ("countryCode".equals(cond.getField()) && cond.getValues() != null && cond.getValues().contains(country)) {
            addRule = true;
          }
        }
        if (addRule) {
          for (Condition cond : rule.getRuleConditions()) {
            if (!fields.contains(cond.getField()) && !"coverageID".equals(cond.getField()) && !"COVERAGE".equals(cond.getField())
                && !"countryCode".equals(cond.getField())) {
              fields.add(cond.getField());
            }
          }
        }
      }
    }
    return fields;
  }

  public List<Rule> getAllRules(String country) {
    List<Rule> fields = new ArrayList<Rule>();
    for (RuleGroup group : this.countryRules) {
      for (Rule rule : group.getRules()) {

        for (Condition cond : rule.getRuleConditions()) {
          if ("countryCode".equals(cond.getField()) && cond.getValues() != null && cond.getValues().contains(country)) {
            fields.add(rule);
          }
        }
      }
    }
    return fields;
  }

  public List<Rule> getIntegratedCovRules() {
    return integratedCovRules;
  }

  public void setIntegratedCovRules(List<Rule> integratedCovRules) {
    this.integratedCovRules = integratedCovRules;
  }

}
