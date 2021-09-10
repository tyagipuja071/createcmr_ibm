package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;

/**
 * 
 * @author Shivangi
 *
 */

public class NordicsUtil extends AutomationUtil {

  // Denmark
  public static final String DK_COMME_LOCAL = "DKCOM";
  public static final String DK_INTER_LOCAL = "DKINT";
  public static final String DK_GOV_LOCAL = "DKGOV";
  public static final String DK_BUSPR_LOCAL = "DKBUS";
  public static final String DK_INTSO_LOCAL = "DKISO";
  public static final String DK_PRIPE_LOCAL = "DKPRI";
  public static final String DK_IBMEM_LOCAL = "DKIBM";
  // Finland
  public static final String FI_COMME_LOCAL = "FICOM";
  public static final String FI_INTER_LOCAL = "FIINT";
  public static final String FI_GOV_LOCAL = "FIGOV";
  public static final String FI_BUSPR_LOCAL = "FIBUS";
  public static final String FI_INTSO_LOCAL = "FIISO";
  public static final String FI_PRIPE_LOCAL = "FIPRI";
  public static final String FI_IBMEM_LOCAL = "FIIBM";
  // Norway and Sweden
  public static final String COMME_LOCAL = "COMME";
  public static final String INTER_LOCAL = "INTER";
  public static final String GOVRN_LOCAL = "GOVRN";
  public static final String BUSPR_LOCAL = "BUSPR";
  public static final String INTSO_LOCAL = "INTSO";
  public static final String PRIPE_LOCAL = "PRIPE";
  public static final String IBMEM_LOCAL = "IBMEM";
  // Cross
  public static final String CROSS_COMME = "CBCOM";
  public static final String CROSS_INTER = "CBINT";
  public static final String CROSS_BUSPR = "CBBUS";
  public static final String CROSS_INTSO = "CBISO";
  public static final String CROSS_IBMEM = "CBIBM";
  public static final String CROSS_PRIPE = "CBPRI";

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    // TODO Auto-generated method stub
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Addr zs01 = requestData.getAddress("ZS01");
    Addr zp01 = requestData.getAddress("ZP01");
    String customerName = getCustomerFullName(zs01);

    if ((DK_BUSPR_LOCAL.equals(scenario) || FI_BUSPR_LOCAL.equals(scenario) || BUSPR_LOCAL.equals(scenario) || CROSS_BUSPR.equals(scenario))
        && zp01 != null && !compareCustomerNames(zs01, zp01)) {
      details.append("Sold-to and Bill-to name are not identical for BP Scenario. Request will require CMDE review before proceeding.").append("\n");
      engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF", "Sold-to Bill-to name are not identical for BP Scenario.");
    }

    switch (scenario) {

    case COMME_LOCAL:
    case CROSS_COMME:
    case GOVRN_LOCAL:
    case DK_COMME_LOCAL:
    case DK_GOV_LOCAL:
    case FI_COMME_LOCAL:
    case FI_GOV_LOCAL:
      if (zp01 != null && !compareCustomerNames(zs01, zp01)) {
        details.append("Sold-to and Bill-to name are not identical. Request will require CMDE review before proceeding.").append("\n");
        engineData.addNegativeCheckStatus("SOLDTO_BILLTO_DIFF", "Sold-to and Bill-to name are not identical.");
      }
      break;

    case DK_BUSPR_LOCAL:
    case FI_BUSPR_LOCAL:
    case BUSPR_LOCAL:
    case CROSS_BUSPR:
      return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
    case DK_PRIPE_LOCAL:
    case FI_PRIPE_LOCAL:
    case PRIPE_LOCAL:
    case CROSS_PRIPE:
      return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName, details, false, requestData);
    }

    return true;

  }

  @Override
  public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      MatchingResponse<DuplicateCMRCheckResponse> response) {
    String[] scenariosToBeChecked = { "BUSPR", "FIBUS", "DKBUS", "CBBUS" };
    String scenario = requestData.getData().getCustSubGrp();
    String[] kuklaBuspr = { "43", "44", "45" };

    if (Arrays.asList(scenariosToBeChecked).contains(scenario)) {
      List<DuplicateCMRCheckResponse> matches = response.getMatches();
      List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
      for (DuplicateCMRCheckResponse match : matches) {
        if (StringUtils.isNotBlank(match.getCustClass())) {
          String kukla = match.getCustClass() != null ? match.getCustClass() : "";
          if (Arrays.asList(kuklaBuspr).contains(kukla) && Arrays.asList(scenariosToBeChecked).contains(scenario)) {
            filteredMatches.add(match);
          }
        }
      }
      // set filtered matches in response
      response.setMatches(filteredMatches);
    }
  }

}
