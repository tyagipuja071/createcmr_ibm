/**
 * 
 */
package com.ibm.cio.cmr.request.automation;

import java.util.HashMap;

import com.ibm.cio.cmr.request.automation.impl.ca.CACMDERequesterCheck;
import com.ibm.cio.cmr.request.automation.impl.gbl.ANZBNValidationElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.CMDERequesterCheck;
import com.ibm.cio.cmr.request.automation.impl.gbl.CNAPICheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.CNDupCMRCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.CNDupReqCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DPLCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DPLSearchElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DefaultApprovalsElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DnBCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DnBMatchingElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DnBOrgIdValidationElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DupAbbrevCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DupCMRCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DupReqCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.EUVatValidationElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.FieldComputationElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.GBGMatchingElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.GBLScenarioCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.GOEAssignmentElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.INGSTValidationElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.ImportCMRElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.RetrieveIBMValuesElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.USAddrStdElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.UpdateSwitchElement;
import com.ibm.cio.cmr.request.automation.impl.la.br.BrazilCalculateIBMElement;
import com.ibm.cio.cmr.request.automation.impl.la.br.BrazilDupCMRCheckElement;
import com.ibm.cio.cmr.request.automation.impl.la.br.ImportExternalDataElement;
import com.ibm.cio.cmr.request.automation.impl.la.br.ScenarioCheckElement;
import com.ibm.cio.cmr.request.automation.impl.us.USBusinessPartnerElement;
import com.ibm.cio.cmr.request.automation.impl.us.USDelReacCheckElement;
import com.ibm.cio.cmr.request.automation.impl.us.USDuplicateCheckElement;
import com.ibm.cio.cmr.request.automation.impl.us.USSosRpaCheckElement;

/**
 * Registry of all {@link AutomationElement} classes with their corresponding
 * codes.
 * 
 * @author JeffZAMORA
 * 
 */
public class AutomationElementRegistry extends HashMap<String, Class<? extends AutomationElement<?>>> {

  private static final long serialVersionUID = 1L;
  // globals
  public static final String GBL_RETRIEVE_VALUES = "GBL_RETRIEVE_VALUES";
  public static final String GBL_MATCH_GBG = "GBL_MATCH_GBG";
  public static final String GBL_CALC_COV = "GBL_CALC_COV";
  public static final String GBL_DPL_CHECK = "GBL_DPL_CHECK";
  public static final String GBL_REQ_CHECK = "GBL_REQ_CHECK";
  public static final String GBL_MATCH_DNB = "GBL_MATCH_DNB";
  public static final String GBL_APPROVALS = "GBL_APPROVALS";
  public static final String GBL_GOE = "GBL_GOE";
  public static final String GBL_DPL_SEARCH = "GBL_DPL_SEARCH";
  public static final String GBL_CMR_IMPORT = "GBL_CMR_IMPORT";
  public static final String GBL_DUP_CMR_CHECK = "GBL_DUP_CMR_CHECK";
  public static final String GBL_ADDR_STD = "GBL_ADDR_STD";
  public static final String GBL_SCENARIO_CHECK = "GBL_SCENARIO_CHECK";
  public static final String GBL_FIELD_COMPUTE = "GBL_FIELD_COMPUTE";
  public static final String GBL_UPDATE_SWITCH = "GBL_UPDATE_SWITCH";
  public static final String GBL_DNB_CHECK = "GBL_DNB_CHECK";
  public static final String GBL_CMDE_CHECK = "GBL_CMDE_CHECK";
  public static final String GBL_DNB_ORGID = "GBL_DNB_ORGID";
  // Brazil
  public static final String BR_SCENARIO = "BR_SCENARIO";
  public static final String BR_CALCULATE = "BR_CALCULATE";
  public static final String BR_DUP_CHECK = "BR_DUP_CHECK";
  public static final String BR_IMPORT = "BR_IMPORT";

  // ANZ
  public static final String ANZ_BN_VALIDATION = "ANZ_BN_VALIDATION";

  // EU
  public static final String EU_VAT_VALIDATION = "EU_VAT_VALIDATION";

  // US
  public static final String US_DEL_REAC_CHECK = "US_DEL_REAC_CHECK";
  public static final String US_DUP_CHK = "US_DUP_CHK";
  public static final String US_BP_PROCESS = "US_BP_PROCESS";
  public static final String US_SOS_RPA_CHECK = "US_SOS_RPA_CHECK";

  // CA
  public static final String CA_CMDE_CHECK = "CA_CMDE_CHECK";

  // EMEA
  public static final String EMEA_ABBREV_CHECK = "EMEA_ABBREV_CHECK";

  // India
  public static final String IN_GST_VALIDATION = "IN_GST_VALIDATION";

  // CN
  public static final String CN_API_CHECK = "CN_API_CHECK";
  public static final String CN_DUP_CMR_CHECK = "CN_DUP_CMR_CHECK";
  public static final String CN_DUP_REQ_CHECK = "CN_DUP_REQ_CHECK";
  private static AutomationElementRegistry registry = new AutomationElementRegistry();

  public static AutomationElementRegistry getInstance() {
    return registry;
  }

  {
    // register the automation elements and codes

    // GLOBALS
    put(GBL_RETRIEVE_VALUES, RetrieveIBMValuesElement.class);
    put(GBL_MATCH_GBG, GBGMatchingElement.class);
    put(GBL_CALC_COV, CalculateCoverageElement.class);
    put(GBL_DPL_CHECK, DPLCheckElement.class);
    put(GBL_MATCH_DNB, DnBMatchingElement.class);
    put(GBL_APPROVALS, DefaultApprovalsElement.class);
    put(GBL_GOE, GOEAssignmentElement.class);
    put(GBL_DPL_SEARCH, DPLSearchElement.class);
    put(GBL_CMR_IMPORT, ImportCMRElement.class);
    put(GBL_REQ_CHECK, DupReqCheckElement.class);
    put(GBL_DUP_CMR_CHECK, DupCMRCheckElement.class);
    put(GBL_FIELD_COMPUTE, FieldComputationElement.class);
    put(GBL_ADDR_STD, USAddrStdElement.class);
    put(GBL_SCENARIO_CHECK, GBLScenarioCheckElement.class);
    put(GBL_UPDATE_SWITCH, UpdateSwitchElement.class);
    put(GBL_DNB_CHECK, DnBCheckElement.class);
    put(GBL_CMDE_CHECK, CMDERequesterCheck.class);
    put(GBL_DNB_ORGID, DnBOrgIdValidationElement.class);
    // Brazil - 631
    put(BR_SCENARIO, ScenarioCheckElement.class);
    put(BR_DUP_CHECK, BrazilDupCMRCheckElement.class);
    put(BR_CALCULATE, BrazilCalculateIBMElement.class);
    put(BR_IMPORT, ImportExternalDataElement.class);

    // ANZ
    put(ANZ_BN_VALIDATION, ANZBNValidationElement.class);

    // EU
    put(EU_VAT_VALIDATION, EUVatValidationElement.class);
    // EMEA
    put(EMEA_ABBREV_CHECK, DupAbbrevCheckElement.class);
    // US
    put(US_DEL_REAC_CHECK, USDelReacCheckElement.class);
    put(US_DUP_CHK, USDuplicateCheckElement.class);
    put(US_BP_PROCESS, USBusinessPartnerElement.class);
    put(US_SOS_RPA_CHECK, USSosRpaCheckElement.class);
    put(IN_GST_VALIDATION, INGSTValidationElement.class);
    put(CN_API_CHECK, CNAPICheckElement.class);
    put(CN_DUP_CMR_CHECK, CNDupCMRCheckElement.class);
    put(CN_DUP_REQ_CHECK, CNDupReqCheckElement.class);

    // CA
    put(CA_CMDE_CHECK, CACMDERequesterCheck.class);
  }

  @Override
  public Class<? extends AutomationElement<?>> put(String key, Class<? extends AutomationElement<?>> value) {
    if (this.containsKey(key)) {
      Class<? extends AutomationElement<?>> clazz = this.get(key);
      throw new IllegalArgumentException(
          "Process code " + key + " is already registered under " + (clazz != null ? clazz.getName() : "(not class found)"));
    }
    return super.put(key, value);
  }

}
