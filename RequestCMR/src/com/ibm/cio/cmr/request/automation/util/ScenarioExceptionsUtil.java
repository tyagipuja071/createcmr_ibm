package com.ibm.cio.cmr.request.automation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.ScenarioExceptions;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;

/**
 * 
 * Utility for handling scenario specific exceptions for automation engine
 * processing
 * 
 * @author RoopakChugh
 *
 */

public class ScenarioExceptionsUtil {

  List<String> addressTypesForDuplicateRequestCheck = new ArrayList<String>();
  List<String> addressTypesForSkipChecks = new ArrayList<String>();
  Map<String, List<String>> addressTypesForDuplicateCMRCheck = new HashMap<>();
  boolean skipDuplicateChecks = false;
  boolean importDnbInfo = false;
  boolean skipChecks = false;
  boolean checkVatForDuplicates = false;

  public ScenarioExceptionsUtil(EntityManager entityManager, String cmrIssuingCntry, String subRegion, String scenario, String subScenario) {

    if (StringUtils.isNotBlank(subScenario) && SystemLocation.UNITED_STATES.equals(cmrIssuingCntry)) {
      String sqlKey = ExternalizedQuery.getSql("AUTOMATION.GET.US_SUBSCENARIO_KEY");
      PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
      query.setParameter("SCENARIO", StringUtils.isNotBlank(scenario) ? scenario : "");
      query.setParameter("SUB_SCENARIO", subScenario);
      query.setForReadOnly(true);
      String subScenarioKey = query.getSingleResult(String.class);
      if (StringUtils.isNotBlank(subScenarioKey)) {
        subScenario = subScenarioKey;
      } else {
        subScenario = subScenario.substring(0, 5);
      }
    }

    ScenarioExceptions childRecord = queryScenarios(entityManager, cmrIssuingCntry, subRegion, scenario, subScenario);
    ScenarioExceptions medianRecord = queryScenarios(entityManager, cmrIssuingCntry, subRegion, scenario, "*");
    ScenarioExceptions parentRecord = queryScenarios(entityManager, cmrIssuingCntry, subRegion, "*", "*");

    if (childRecord != null) {
      parseResult(childRecord);
    }
    if (medianRecord != null) {
      parseResult(medianRecord);
    }
    if (parentRecord != null) {
      parseResult(parentRecord);
    }

    // initialize default address types for duplicate checks
    if (getAddressTypesForDuplicateRequestCheck().size() == 0) {
      getAddressTypesForDuplicateRequestCheck().add("ZS01");
    }
    if (getAddressTypesForDuplicateCMRCheck().size() == 0) {
      getAddressTypesForDuplicateCMRCheck().put("ZS01", Arrays.asList("ZS01"));
    }
  }

  private ScenarioExceptions queryScenarios(EntityManager entityManager, String cmrIssuingCntry, String subRegion, String scenario,
      String subScenario) {
    String sqlKey = ExternalizedQuery.getSql("AUTOMATION.GET.SCENARIO_EXCEPTIONS");
    PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
    query.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    query.setParameter("SCENARIO", StringUtils.isNotBlank(scenario) ? scenario : "");
    query.setParameter("SUB_SCENARIO", StringUtils.isNotBlank(subScenario) ? subScenario : "");
    query.setParameter("SUBREGION_CD", (StringUtils.isNotBlank(subRegion) && subRegion.contains(cmrIssuingCntry)) ? subRegion : cmrIssuingCntry);
    query.setForReadOnly(true);
    return query.getSingleResult(ScenarioExceptions.class);
  }

  private void parseResult(ScenarioExceptions exceptions) {

    // AddressTypesForDuplicateReqChecks
    if (exceptions.getDupCheckAddrTypes() != null && StringUtils.isNotBlank(exceptions.getDupCheckAddrTypes())
        && (getAddressTypesForDuplicateRequestCheck().size() == 0 || getAddressTypesForDuplicateCMRCheck().size() == 0)) {
      List<String> addrTypesForDupReqChecks = new ArrayList<>();
      HashMap<String, List<String>> addrTypesForDupCMRChecks = new HashMap<>();
      String[] addrTypesMap = exceptions.getDupCheckAddrTypes().split(",");
      for (String addrTypes : addrTypesMap) {
        String[] addrMap = addrTypes.split("-");
        if (addrMap.length == 2) {
          addrTypesForDupReqChecks.add(addrMap[0].trim());
          if (addrTypesForDupCMRChecks.containsKey(addrMap[0].trim())) {
            addrTypesForDupCMRChecks.get(addrMap[0].trim()).add(addrMap[1].trim());
          } else {
            List<String> addrType = new ArrayList<>();
            addrType.add(addrMap[1].trim());
            addrTypesForDupCMRChecks.put(addrMap[0].trim(), addrType);
          }
        }
      }
      if (getAddressTypesForDuplicateCMRCheck().size() == 0 && addrTypesForDupCMRChecks.size() > 0) {
        setAddressTypesForDuplicateCMRCheck(addrTypesForDupCMRChecks);
      }
      if (getAddressTypesForDuplicateRequestCheck().size() == 0 && addrTypesForDupReqChecks.size() > 0) {
        setAddressTypesForDuplicateRequestCheck(addrTypesForDupReqChecks);
      }
    }

    // AddressTypesForSkipChecks
    if (exceptions.getSkipCheckAddrTypes() != null && StringUtils.isNotBlank(exceptions.getSkipCheckAddrTypes())
        && getAddressTypesForSkipChecks().size() == 0) {
      List<String> addrTypes = Arrays.asList(exceptions.getSkipCheckAddrTypes().split(","));
      setAddressTypesForSkipChecks(addrTypes);
    }

    // SkipDuplicateChecksIndicator
    if (exceptions.getSkipDupChecksIndc() != null && StringUtils.isNotBlank(exceptions.getSkipDupChecksIndc()) && !isSkipDuplicateChecks()) {
      setSkipDuplicateChecks("Y".equals(exceptions.getSkipDupChecksIndc()));
    }

    // ImportDnbInfoIndicator
    if (exceptions.getImportDnbInfoIndc() != null && StringUtils.isNotBlank(exceptions.getImportDnbInfoIndc()) && !isImportDnbInfo()) {
      setImportDnbInfo("Y".equals(exceptions.getImportDnbInfoIndc()));
    }

    // CheckVatForDuplicatesIndicator
    if (exceptions.getCheckVatIndc() != null && StringUtils.isNotBlank(exceptions.getCheckVatIndc()) && !isCheckVatForDuplicates()) {
      setCheckVatForDuplicates("Y".equals(exceptions.getCheckVatIndc()));
    }

    // SkipChecksIndicator
    if (exceptions.getSkipChecksIndc() != null && StringUtils.isNotBlank(exceptions.getSkipChecksIndc()) && !isSkipChecks()) {
      setSkipChecks("Y".equals(exceptions.getSkipChecksIndc()));
    }
  }

  public void test() {
    System.out.println("Addr Types for Duplicate Request Checks: " + getAddressTypesForDuplicateRequestCheck().toString());
    System.out.println("Addr Types for Duplicate CMR Checks: " + getAddressTypesForDuplicateCMRCheck().toString());
    System.out.println("Addr Types for Skip Checks: " + getAddressTypesForSkipChecks().toString());
    System.out.println("Skip Duplicate Checks Indc: " + (isSkipDuplicateChecks() ? "Y" : "N"));
    System.out.println("Check VAT for Duplicates Indc : " + (isCheckVatForDuplicates() ? "Y" : "N"));
    System.out.println("Skip Checks Indc: " + (isSkipChecks() ? "Y" : "N"));
    System.out.println("Import DnB info Indc: " + (isImportDnbInfo() ? "Y" : "N"));
  }

  public List<String> getAddressTypesForSkipChecks() {
    return addressTypesForSkipChecks;
  }

  public void setAddressTypesForSkipChecks(List<String> addressTypesForSkipChecks) {
    this.addressTypesForSkipChecks = addressTypesForSkipChecks;
  }

  public boolean isSkipDuplicateChecks() {
    return skipDuplicateChecks;
  }

  public void setSkipDuplicateChecks(boolean skipDuplicateChecks) {
    this.skipDuplicateChecks = skipDuplicateChecks;
  }

  public boolean isImportDnbInfo() {
    return importDnbInfo;
  }

  public void setImportDnbInfo(boolean importDnbInfo) {
    this.importDnbInfo = importDnbInfo;
  }

  public boolean isSkipChecks() {
    return skipChecks;
  }

  public void setSkipChecks(boolean skipChecks) {
    this.skipChecks = skipChecks;
  }

  public boolean isCheckVatForDuplicates() {
    return checkVatForDuplicates;
  }

  public void setCheckVatForDuplicates(boolean checkVatForDuplicates) {
    this.checkVatForDuplicates = checkVatForDuplicates;
  }

  public List<String> getAddressTypesForDuplicateRequestCheck() {
    return addressTypesForDuplicateRequestCheck;
  }

  public void setAddressTypesForDuplicateRequestCheck(List<String> addressTypesForDuplicateRequestCheck) {
    this.addressTypesForDuplicateRequestCheck = addressTypesForDuplicateRequestCheck;
  }

  public Map<String, List<String>> getAddressTypesForDuplicateCMRCheck() {
    return addressTypesForDuplicateCMRCheck;
  }

  public void setAddressTypesForDuplicateCMRCheck(Map<String, List<String>> addressTypesForDuplicateCMRCheck) {
    this.addressTypesForDuplicateCMRCheck = addressTypesForDuplicateCMRCheck;
  }

}
