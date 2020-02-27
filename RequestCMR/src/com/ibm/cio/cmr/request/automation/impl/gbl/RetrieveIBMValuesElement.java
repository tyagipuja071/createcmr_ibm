/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import javax.persistence.EntityManager;

import org.springframework.ui.ModelMap;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.OverridingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.CmrClientService;

/**
 * Connects to the ODM services to retrieve GBG, GLC, Coverage, and LH
 * 
 * @author JeffZAMORA
 * 
 */
public class RetrieveIBMValuesElement extends OverridingElement {

  public RetrieveIBMValuesElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    long reqId = requestData.getAdmin().getId().getReqId();
    AutomationResult<OverrideOutput> results = buildResult(reqId);
    OverrideOutput overrides = new OverrideOutput(false);
    StringBuilder details = new StringBuilder();

    CmrClientService odmService = new CmrClientService();
    RequestEntryModel model = requestData.createModelFromRequest();
    Addr soldTo = requestData.getAddress("ZS01");
    ModelMap response = new ModelMap();

    // glc
    details.append("Values retrieved from ODM based on current request values:\n\n");
    boolean success = odmService.getGlc(entityManager, soldTo, model, response);
    String glcCode = (String) response.get("glcCode");
    String glcDesc = (String) response.get("glcDesc");
    if (!success) {
      details.append("GLC details were not successfully retrieved.\n");
    } else {
      if (glcCode != null) {
        details.append("GLC = " + glcCode + " (" + (glcDesc != null ? glcDesc : "no description") + ")\n");
        overrides.addOverride(getProcessCode(), "DATA", "GEO_LOCATION_CD", model.getGeoLocationCd(), glcCode);
      }
    }
    model.setGeoLocationCd(glcCode);

    // bg
    success = odmService.getBuyingGroup(entityManager, soldTo, model, response);
    String bgId = (String) response.get("buyingGroupID");
    String bgDesc = (String) response.get("buyingGroupDesc");
    String gbgId = (String) response.get("globalBuyingGroupID");
    String gbgDesc = (String) response.get("globalBuyingGroupDesc");
    String ldeRule = (String) response.get("odmRuleID");

    if (!success) {
      details.append("Buying Group details were not successfully retrieved.\n");
    } else {
      if (bgId != null) {
        details.append("BG = " + bgId + " (" + (bgDesc != null ? bgDesc : "no description") + ")\n");
        overrides.addOverride(getProcessCode(), "DATA", "BG_ID", model.getBgId(), bgId);
        overrides.addOverride(getProcessCode(), "DATA", "BG_DESC", model.getBgDesc(), bgDesc);
      }
      if (gbgId != null) {
        details.append("GBG = " + gbgId + " (" + (gbgDesc != null ? gbgDesc : "no description") + ")\n");
        overrides.addOverride(getProcessCode(), "DATA", "GBG_ID", model.getGbgId(), gbgId);
        overrides.addOverride(getProcessCode(), "DATA", "GBG_DESC", model.getGbgDesc(), gbgDesc);
      }
      if (ldeRule != null) {
        details.append("LDE Rule = " + ldeRule + " \n");
        overrides.addOverride(getProcessCode(), "DATA", "BG_RULE_ID", model.getBgRuleId(), ldeRule);
      }
    }

    model.setBgId(bgId);
    model.setGbgId(gbgId);
    model.setBgDesc(bgDesc);
    model.setGbgDesc(gbgDesc);
    model.setBgRuleId(ldeRule);

    success = odmService.getCoverage(entityManager, soldTo, model, response);
    String covType = (String) response.get("coverageType");
    String covId = (String) response.get("coverageID");
    String covDesc = (String) response.get("coverageDesc");
    if (!success) {
      details.append("Coverage details were not successfully retrieved.\n");
    } else {
      if (covId != null) {
        details.append("Coverage = " + covType + covId + " (" + covDesc + ")\n");
        overrides.addOverride(getProcessCode(), "DATA", "COV_ID", model.getCovId(), covType + covId);
        overrides.addOverride(getProcessCode(), "DATA", "COV_DESC", model.getCovDesc(), covDesc);
      }
    }

    overrides.addOverride(getProcessCode(), "ADMN", "COV_BG_RETRIEVED_IND", model.getCovBgRetrievedInd(), "Y");

    results.setResults("Successful Execution");
    results.setDetails(details.toString());
    results.setProcessOutput(overrides);
    return results;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_RETRIEVE_VALUES;
  }

  @Override
  public String getProcessDesc() {
    return "ODM Coverage, BG, and GLC";
  }

}
