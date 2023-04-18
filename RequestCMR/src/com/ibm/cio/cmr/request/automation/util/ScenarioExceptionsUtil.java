package com.ibm.cio.cmr.request.automation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

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
  private static final List<String> PRIVATE_SCENARIOS = Arrays.asList("AFPC", "BEPRI", "CHPRI", "CROPR", "CRPRI", "CSPC", "DKPRI", "EEPRI", "FIPRI",
      "FOPRI", "GLPRI", "ISPRI", "JOPC", "LIPRI", "LSPC", "LSXPC", "LTPRI", "LUPRI", "LVPRI", "MEPC", "NAPC", "NAXPC", "PKPC", "PRICU", "PRIPE",
      "PRISM", "PRIV", "PRIVA", "PSPC", "RSPC", "RSXPC", "SZPC", "SZXPC", "XPC", "XPRIC", "XPRIV", "ZAPC", "ZAXPC");
  boolean skipDuplicateChecks;
  boolean importDnbInfo;
  boolean skipChecks;
  boolean checkVATForDnB;
  boolean skipCompanyVerification;
  boolean manualReviewIndc;
  boolean reviewExtReqIndc;
  boolean skipFindGbgForPrivates;
  boolean isPrivateSubScenario;

  public ScenarioExceptionsUtil(EntityManager entityManager, String cmrIssuingCntry, String subRegion, String scenario, String subScenario) {

    String custSubGroup = subScenario;
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
      parseAddressResults(childRecord);
    }
    if (medianRecord != null) {
      parseAddressResults(medianRecord);
    }
    if (parentRecord != null) {
      parseAddressResults(parentRecord);
    }

    parseResult(childRecord, medianRecord, parentRecord);

    // initialize default address types for duplicate checks
    if (getAddressTypesForDuplicateRequestCheck().size() == 0) {
      getAddressTypesForDuplicateRequestCheck().add("ZS01");
    }
    if (getAddressTypesForDuplicateCMRCheck().size() == 0) {
      getAddressTypesForDuplicateCMRCheck().put("ZS01", Arrays.asList("ZS01"));
    }

    if (StringUtils.isNotBlank(custSubGroup)) {
      if (PRIVATE_SCENARIOS.contains(custSubGroup)) {
        setSkipFindGbgForPrivates(true);
      }
    }

  }

  private void parseAddressResults(ScenarioExceptions exceptions) {
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

  private void parseResult(ScenarioExceptions child, ScenarioExceptions median, ScenarioExceptions parent) {
    // SkipDuplicateChecksIndicator
    if (child != null && child.getSkipDupChecksIndc() != null && StringUtils.isNotBlank(child.getSkipDupChecksIndc())) {
      setSkipDuplicateChecks("Y".equals(child.getSkipDupChecksIndc()));
    } else if (median != null && median.getSkipDupChecksIndc() != null && StringUtils.isNotBlank(median.getSkipDupChecksIndc())) {
      setSkipDuplicateChecks("Y".equals(median.getSkipDupChecksIndc()));
    } else if (parent != null && parent.getSkipDupChecksIndc() != null && StringUtils.isNotBlank(parent.getSkipDupChecksIndc())) {
      setSkipDuplicateChecks("Y".equals(parent.getSkipDupChecksIndc()));
    } else {
      setSkipDuplicateChecks(false);
    }

    // SkipCompanyVerificationChecksIndicator
    if (child != null && child.getSkipVerificationIndc() != null && StringUtils.isNotBlank(child.getSkipVerificationIndc())) {
      setSkipCompanyVerification("Y".equals(child.getSkipVerificationIndc()));
    } else if (median != null && median.getSkipVerificationIndc() != null && StringUtils.isNotBlank(median.getSkipVerificationIndc())) {
      setSkipCompanyVerification("Y".equals(median.getSkipVerificationIndc()));
    } else if (parent != null && parent.getSkipVerificationIndc() != null && StringUtils.isNotBlank(parent.getSkipVerificationIndc())) {
      setSkipCompanyVerification("Y".equals(parent.getSkipVerificationIndc()));
    } else {
      setSkipCompanyVerification(false);
    }

    // ImportDnbInfoIndicator
    if (child != null && child.getImportDnbInfoIndc() != null && StringUtils.isNotBlank(child.getImportDnbInfoIndc())) {
      setImportDnbInfo("Y".equals(child.getImportDnbInfoIndc()));
    } else if (median != null && median.getImportDnbInfoIndc() != null && StringUtils.isNotBlank(median.getImportDnbInfoIndc())) {
      setImportDnbInfo("Y".equals(median.getImportDnbInfoIndc()));
    } else if (parent != null && parent.getImportDnbInfoIndc() != null && StringUtils.isNotBlank(parent.getImportDnbInfoIndc())) {
      setImportDnbInfo("Y".equals(parent.getImportDnbInfoIndc()));
    } else {
      setImportDnbInfo(false);
    }

    // CheckVatForDuplicatesIndicator
    if (child != null && child.getCheckVatIndc() != null && StringUtils.isNotBlank(child.getCheckVatIndc())) {
      setCheckVATForDnB("Y".equals(child.getCheckVatIndc()));
    } else if (median != null && median.getCheckVatIndc() != null && StringUtils.isNotBlank(median.getCheckVatIndc())) {
      setCheckVATForDnB("Y".equals(median.getCheckVatIndc()));
    } else if (parent != null && parent.getCheckVatIndc() != null && StringUtils.isNotBlank(parent.getCheckVatIndc())) {
      setCheckVATForDnB("Y".equals(parent.getCheckVatIndc()));
    } else {
      setCheckVATForDnB(false);
    }

    // SkipChecksIndicator
    if (child != null && child.getSkipChecksIndc() != null && StringUtils.isNotBlank(child.getSkipChecksIndc())) {
      setSkipChecks("Y".equals(child.getSkipChecksIndc()));
    } else if (median != null && median.getSkipChecksIndc() != null && StringUtils.isNotBlank(median.getSkipChecksIndc())) {
      setSkipChecks("Y".equals(median.getSkipChecksIndc()));
    } else if (parent != null && parent.getSkipChecksIndc() != null && StringUtils.isNotBlank(parent.getSkipChecksIndc())) {
      setSkipChecks("Y".equals(parent.getSkipChecksIndc()));
    } else {
      setSkipChecks(false);
    }

    // manual review indc
    if (child != null && child.getManualReviewIndc() != null && StringUtils.isNotBlank(child.getManualReviewIndc())) {
      setManualReviewIndc("Y".equals(child.getManualReviewIndc()));
    } else if (median != null && median.getManualReviewIndc() != null && StringUtils.isNotBlank(median.getManualReviewIndc())) {
      setManualReviewIndc("Y".equals(median.getManualReviewIndc()));
    } else if (parent != null && parent.getManualReviewIndc() != null && StringUtils.isNotBlank(parent.getManualReviewIndc())) {
      setManualReviewIndc("Y".equals(parent.getManualReviewIndc()));
    } else {
      setManualReviewIndc(false);
    }

    // review external requests indc
    if (child != null && child.getReviewExtReqIndc() != null && StringUtils.isNotBlank(child.getReviewExtReqIndc())) {
      setReviewExtReqIndc("Y".equals(child.getReviewExtReqIndc()));
    } else if (median != null && median.getReviewExtReqIndc() != null && StringUtils.isNotBlank(median.getReviewExtReqIndc())) {
      setReviewExtReqIndc("Y".equals(median.getReviewExtReqIndc()));
    } else if (parent != null && parent.getReviewExtReqIndc() != null && StringUtils.isNotBlank(parent.getReviewExtReqIndc())) {
      setReviewExtReqIndc("Y".equals(parent.getReviewExtReqIndc()));
    } else {
      setReviewExtReqIndc(false);
    }
  }

  public void test() {
    System.out.println("Addr Types for Duplicate Request Checks: " + getAddressTypesForDuplicateRequestCheck().toString());
    System.out.println("Addr Types for Duplicate CMR Checks: " + getAddressTypesForDuplicateCMRCheck().toString());
    System.out.println("Addr Types for Skip Checks: " + getAddressTypesForSkipChecks().toString());
    System.out.println("Skip Duplicate Checks Indc: " + (isSkipDuplicateChecks() ? "Y" : "N"));
    System.out.println("Check VAT for Duplicates Indc : " + (isCheckVATForDnB() ? "Y" : "N"));
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

  public boolean isSkipCompanyVerification() {
    return skipCompanyVerification;
  }

  public void setSkipCompanyVerification(boolean skipCompanyVerification) {
    this.skipCompanyVerification = skipCompanyVerification;
  }

  public boolean isCheckVATForDnB() {
    return checkVATForDnB;
  }

  public void setCheckVATForDnB(boolean checkVATForDnB) {
    this.checkVATForDnB = checkVATForDnB;
  }

  public boolean isManualReviewIndc() {
    return manualReviewIndc;
  }

  public void setManualReviewIndc(boolean manualReviewIndc) {
    this.manualReviewIndc = manualReviewIndc;
  }

  public boolean isReviewExtReqIndc() {
    return reviewExtReqIndc;
  }

  public void setReviewExtReqIndc(boolean reviewExtReqIndc) {
    this.reviewExtReqIndc = reviewExtReqIndc;
  }

  public boolean isSkipFindGbgForPrivates() {
    return skipFindGbgForPrivates;
  }

  public void setSkipFindGbgForPrivates(boolean skipFindGbgForPrivates) {
    this.skipFindGbgForPrivates = skipFindGbgForPrivates;
  }

}
