/**
 * 
 */
package com.ibm.cio.cmr.request.automation;

import java.util.HashMap;

import com.ibm.cio.cmr.request.automation.impl.gbl.ANZBNValidationElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DPLCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DPLSearchElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DefaultApprovalsElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DnBCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DnBMatchingElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DupCMRCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.DupReqCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.EUVatValidationElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.FieldComputationElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.GBGMatchingElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.GBLScenarioCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.GOEDeterminationElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.ImportCMRElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.RetrieveIBMValuesElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.USAddrStdElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.USDuplicateCheckElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.UpdateSwitchElement;
import com.ibm.cio.cmr.request.automation.impl.la.br.BrazilCalculateIBMElement;
import com.ibm.cio.cmr.request.automation.impl.la.br.BrazilDupCMRCheckElement;
import com.ibm.cio.cmr.request.automation.impl.la.br.ImportExternalDataElement;
import com.ibm.cio.cmr.request.automation.impl.la.br.ScenarioCheckElement;
import com.ibm.cio.cmr.request.automation.impl.us.USDelReacCheckElement;

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
    put(GBL_GOE, GOEDeterminationElement.class);
    put(GBL_DPL_SEARCH, DPLSearchElement.class);
    put(GBL_CMR_IMPORT, ImportCMRElement.class);
    put(GBL_REQ_CHECK, DupReqCheckElement.class);
    put(GBL_DUP_CMR_CHECK, DupCMRCheckElement.class);
    put(GBL_FIELD_COMPUTE, FieldComputationElement.class);
    put(GBL_ADDR_STD, USAddrStdElement.class);
    put(GBL_SCENARIO_CHECK, GBLScenarioCheckElement.class);
    put(GBL_UPDATE_SWITCH, UpdateSwitchElement.class);
    put(GBL_DNB_CHECK, DnBCheckElement.class);
    // Brazil - 631
    put(BR_SCENARIO, ScenarioCheckElement.class);
    put(BR_DUP_CHECK, BrazilDupCMRCheckElement.class);
    put(BR_CALCULATE, BrazilCalculateIBMElement.class);
    put(BR_IMPORT, ImportExternalDataElement.class);

    // ANZ
    put(ANZ_BN_VALIDATION, ANZBNValidationElement.class);

    // EU
    put(EU_VAT_VALIDATION, EUVatValidationElement.class);

    // US
    put(US_DEL_REAC_CHECK, USDelReacCheckElement.class);
    put(US_DUP_CHK, USDuplicateCheckElement.class);
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
