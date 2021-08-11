package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;

public class ChinaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(ChinaUtil.class);
  public static final String SCENARIO_LOCAL_NRML = "NRML";
  public static final String SCENARIO_LOCAL_EMBSA = "EMBSA";
  public static final String SCENARIO_CROSS_CROSS = "CROSS";
  public static final String SCENARIO_LOCAL_AQSTN = "AQSTN";
  public static final String SCENARIO_LOCAL_BLUMX = "BLUMX";
  public static final String SCENARIO_LOCAL_MRKT = "MRKT";
  public static final String SCENARIO_LOCAL_BUSPR = "BUSPR";
  public static final String SCENARIO_LOCAL_INTER = "INTER";
  public static final String SCENARIO_LOCAL_PRIV = "PRIV";
  public static final String SCENARIO_ECOSYTEM_PARNER = "ECOSY";
  // private static final List<String> RELEVANT_ADDRESSES =
  // Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
  // CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO);
  // private static final List<String> NON_RELEVANT_ADDRESS_FIELDS =
  // Arrays.asList("Attention Person", "Phone #", "Collection Code");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {

    // ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil)
    // engineData.get("SCENARIO_EXCEPTIONS");
    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();
    Admin admin = requestData.getAdmin();
    if ("U".equals(admin.getReqType())) {

    }
    Addr soldTo = requestData.getAddress("ZS01");
    String custNm1 = soldTo.getCustNm1();
    String custNm2 = StringUtils.isNotBlank(soldTo.getCustNm2()) ? " " + soldTo.getCustNm2() : "";
    String customerName = custNm1 + custNm2;

    switch (scenario) {

    case SCENARIO_LOCAL_NRML:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_LOCAL_EMBSA:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    // case SCENARIO_CROSS_CROSS:
    // if (!("00000".equals(data.getSearchTerm()) ||
    // "04182".equals(data.getSearchTerm()))) {
    // LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + "
    // Scenario=" + data.getCustSubGrp());
    // result.setOnError(false);
    // details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + "
    // Scenario=" + data.getCustSubGrp() + " for the request.\n");
    // } else {
    // details.append("Cluster=" + data.getSearchTerm() + " should not be
    // allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
    // engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() +
    // " should not be allowed for this scenario", "", "");
    // result.setOnError(true);
    // }
    // break;
    case SCENARIO_LOCAL_AQSTN:
      if (!("00000".equals(data.getSearchTerm()) || "04182".equals(data.getSearchTerm()))) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("Cluster=" + data.getSearchTerm() + " should not be allowed for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should not be allowed for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_LOCAL_BLUMX:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_LOCAL_MRKT:
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      break;
    case SCENARIO_ECOSYTEM_PARNER:
      if ("08036".equals(data.getSearchTerm())) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details
            .append("Cluster=" + data.getSearchTerm() + " should be default (08036)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should default (08036)  for this scenario", "", "");
        result.setOnError(true);
      }
      break;
    case SCENARIO_LOCAL_BUSPR:
      if ("04182".equals(data.getSearchTerm())) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details
            .append("Cluster=" + data.getSearchTerm() + " should be default (04182)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should default (04182)  for this scenario", "", "");
        result.setOnError(true);
      }

      if ("7230".equals(data.getIsicCd())) {
        LOG.debug("ISIC allowed: ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("ISIC ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("ISIC=" + data.getIsicCd() + " should be default (7230)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "ISIC=" + data.getIsicCd() + " should default (7230)  for this scenario", "", "");
        result.setOnError(true);
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_RETRIEVE_VALUES);
      doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
      break;
    case SCENARIO_LOCAL_INTER:

      if (StringUtils.isNotBlank(customerName) && customerName.indexOf("IBM China") >= 0) {
        LOG.debug("English name=" + customerName + " for Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("English name=" + customerName + " for Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("English name should include 'IBM China' for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "English name should include 'IBM China' for this scenario", "", "");
        result.setOnError(true);
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_RETRIEVE_VALUES);
      break;
    case SCENARIO_LOCAL_PRIV:
      if ("00000".equals(data.getSearchTerm())) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details
            .append("Cluster=" + data.getSearchTerm() + " should be default (00000)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should default (00000)  for this scenario", "", "");
        result.setOnError(true);
      }
      if (StringUtils.isNotBlank(customerName) && (customerName.indexOf("Private Limited") < 0 && customerName.indexOf("Company") < 0
          && customerName.indexOf("Corporation") < 0 && customerName.indexOf("incorporate") < 0 && customerName.indexOf("organization") < 0
          && customerName.indexOf("Pvt Ltd") < 0 && customerName.indexOf("imited") < 0 && customerName.indexOf("Co., Ltd.") < 0)) {
        LOG.debug("English name=" + customerName + " for Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("English name=" + customerName + " for Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append(
            "English name can't contain 'Private Limited', 'Company', 'Corporation', 'incorporate', 'organization', 'Pvt Ltd','limited','Co., Ltd.' for Scenario="
                + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH",
            "English name can't contain 'Private Limited', 'Company', 'Corporation', 'incorporate', 'organization', 'Pvt Ltd','limited','Co., Ltd.' for this scenario",
            "", "");
        result.setOnError(true);
      }
      if ("9500".equals(data.getIsicCd())) {
        LOG.debug("ISIC allowed: ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("ISIC ISIC=" + data.getIsicCd() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details.append("ISIC=" + data.getIsicCd() + " should be default (9500)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "ISIC=" + data.getIsicCd() + " should default (9500)  for this scenario", "", "");
        result.setOnError(true);
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_RETRIEVE_VALUES);
      break;
    }

    String ret = geDocContent(entityManager, admin.getId().getReqId());
    if ("Y".equals(ret)) {
      details.append("An attachment of type 'Chinese Name And Address change' has been added. This Requester will be routed to CMDE.\n");
      engineData.addRejectionComment("OTH",
          "An attachment of type 'Chinese Name And Address change' has been added. This Requester will be routed to CMDE", "", "");
      result.setOnError(true);
    }

    return true;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    if ("C".equals(admin.getReqType())) {
      String scenario = data.getCustSubGrp();
      LOG.info("Starting Field Computations for Request ID " + data.getId().getReqId());
      if (StringUtils.isNotBlank(scenario) && SCENARIO_LOCAL_PRIV.equals(scenario)) {
        details.append("Skip processing of element.\n");
        results.setResults("Skipped");
        results.setDetails("Skip processing of element.");
        results.setOnError(false);
      } else if (StringUtils.isNotBlank(scenario) && SCENARIO_LOCAL_BUSPR.equals(scenario)) {
        List<String> managerID = SystemParameters.getList("AUTO_CN_MGR_BP_LIST");
        boolean managerCheck = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), managerID);
        if (!managerCheck) {
          details.append("BP CMR related,please contact Dalian BPCM team to raise request.Squad Leader:"
              + (managerID != null && managerID.size() > 0 ? managerID.get(0) : ""));
          engineData.addRejectionComment("OTH", "BP CMR related,please contact Dalian BPCM team to raise request.Squad Leader:"
              + (managerID != null && managerID.size() > 0 ? managerID.get(0) : ""), "", "");
          results.setOnError(true);
          results.setResults("Requester check fail");
        } else {
          details.append("Skipping validation for requester must be from BPSO team for requester - " + admin.getRequesterId() + ".\n");
          results.setOnError(false);
          results.setResults("Computed");
        }
      }
      // else {
      // String error = performISICCheck(entityManager, requestData);
      //
      // if (StringUtils.isNotBlank(error)) {
      // details.append(error);
      // engineData.addRejectionComment("OTH",
      // "If you insist to use different ISIC from D&B,please raise D&B ticket
      // to do investigation and raise jira ticket to CMDE team within 30 days
      // after D&B refresh.",
      // "", "");
      // results.setOnError(true);
      // } else {
      // details.append("ISIC is valid" + "\n");
      // details.append("Successful Execution.\n");
      // results.setOnError(false);
      // results.setResults("Computed");
      // }
      //
      // }

    } else {
      details.append("No specific fields to compute.\n");
      results.setResults("Skipped");
      results.setOnError(false);
    }
    results.setDetails(details.toString());

    return results;
  }

  /**
   * Checks to perform if ISIC updated
   * 
   * @param cedpManager
   * @param entityManager
   * @param requestData
   * @param updatedDataModel
   * @return a string with error message if some issues encountered during
   *         checks, null if validated.
   * @throws Exception
   */
  // private String performISICCheck(EntityManager entityManager, RequestData
  // requestData) throws Exception {
  // Data data = requestData.getData();
  // String isic = data.getIsicCd() == null ? "" : data.getIsicCd();
  // String dunsNo = "";
  // if (StringUtils.isNotBlank(data.getDunsNo())) {
  // dunsNo = data.getDunsNo();
  //
  // if (StringUtils.isNotBlank(dunsNo)) {
  // DnBCompany dnbData = DnBUtil.getDnBDetails(dunsNo);
  // if (dnbData != null && StringUtils.isNotBlank(dnbData.getIbmIsic())) {
  // if (!dnbData.getIbmIsic().equals(isic)) {
  // isic = dnbData.getIbmIsic();
  // return "ISIC validation failed by D&B,import and override the ISIC from D&B
  // directly";
  // } else {
  //
  // return null;
  //
  // }
  // }
  // } else {
  // return "\n- Duns No. is blank";
  // }
  // } else {
  // return "\n- Duns No. is blank";
  // }
  //
  // return null;
  // }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    StringBuilder details = new StringBuilder();
    String scenario = data.getCustSubGrp();
    if (StringUtils.isNotBlank(scenario) && SCENARIO_LOCAL_BUSPR.equals(scenario)
        || StringUtils.isNotBlank(data.getPpsceid()) && !"08036".equals(data.getSearchTerm())) {
      List<String> managerID = SystemParameters.getList("AUTO_CN_MGR_BP_LIST");
      boolean managerCheck = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), managerID);
      if (!managerCheck) {
        details.append("BP CMR related,please contact Dalian BPCM team to raise request.Squad Leader:"
            + (managerID != null && managerID.size() > 0 ? managerID.get(0) : ""));
        engineData.addRejectionComment("OTH", "BP CMR related,please contact Dalian BPCM team to raise request.Squad Leader:"
            + (managerID != null && managerID.size() > 0 ? managerID.get(0) : ""), "", "");
        output.setOnError(true);
        validation.setSuccess(false);
        validation.setMessage("Rejected");
      } else {
        details.append("Skipping validation for request must be from BPSO team for requester - " + admin.getRequesterId() + ".\n");
        validation.setSuccess(true);
        validation.setMessage("Successful");
      }

    } else {
      details.append("Skipping validation for this scenario. scenario = " + scenario + ".\n");
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }

    String ret = geDocContent(entityManager, admin.getId().getReqId());
    if ("Y".equals(ret)) {
      details.append("An attachment of type 'Chinese Name And Address change' has been added. This Requester will be routed to CMDE.\n");
      engineData.addRejectionComment("OTH",
          "An attachment of type 'Chinese Name And Address change' has been added. This Requester will be routed to CMDE", "", "");
    }

    output.setDetails(details.toString());
    output.setProcessOutput(validation);
    return true;
  }

  public boolean isUpdated(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData) throws Exception {

    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    RequestChangeContainer changes = new RequestChangeContainer(entityManager, data.getCmrIssuingCntry(), admin, requestData);

    // check if ZS01 name and address have been updated
    boolean zs01EnName1 = changes.isAddressFieldChanged("ZS01", "Customer Name English");
    boolean zs01CnName1 = changes.isAddressFieldChanged("ZS01", "Customer Name Chinese");
    boolean zs01EnName2 = changes.isAddressFieldChanged("ZS01", "Customer Name Con't English");
    boolean zs01CnName2 = changes.isAddressFieldChanged("ZS01", "Customer Name Con't Chinese");
    boolean zs01EnName3 = changes.isAddressFieldChanged("ZS01", "Customer Name Con't 2 English");
    boolean zs01CnName3 = changes.isAddressFieldChanged("ZS01", "Customer Name Con't Chinese 2");
    boolean zs01EnAddr1 = changes.isAddressFieldChanged("ZS01", "Street Address English");
    boolean zs01CnAddr1 = changes.isAddressFieldChanged("ZS01", "Street Address Chinese");
    boolean zs01EnAddr2 = changes.isAddressFieldChanged("ZS01", "Address Con't English");
    boolean zs01CnAddr2 = changes.isAddressFieldChanged("ZS01", "Street Address Con't Chinese");

    // check if ZI01 name and address have been updated
    boolean zi01EnName1 = changes.isAddressFieldChanged("ZI01", "Customer Name English");
    boolean zi01CnName1 = changes.isAddressFieldChanged("ZI01", "Customer Name Chinese");
    boolean zi01EnName2 = changes.isAddressFieldChanged("ZI01", "Customer Name Con't English");
    boolean zi01CnName2 = changes.isAddressFieldChanged("ZI01", "Customer Name Con't Chinese");
    boolean zi01EnName3 = changes.isAddressFieldChanged("ZI01", "Customer Name Con't 2 English");
    boolean zi01CnName3 = changes.isAddressFieldChanged("ZI01", "Customer Name Con't Chinese 2");
    boolean zi01EnAddr1 = changes.isAddressFieldChanged("ZI01", "Street Address English");
    boolean zi01CnAddr1 = changes.isAddressFieldChanged("ZI01", "Street Address Chinese");
    boolean zi01EnAddr2 = changes.isAddressFieldChanged("ZI01", "Address Con't English");
    boolean zi01CnAddr2 = changes.isAddressFieldChanged("ZI01", "Street Address Con't Chinese");

    // check if ZP01 name and address have been updated
    boolean zp01EnName1 = changes.isAddressFieldChanged("ZP01", "Customer Name English");
    boolean zp01CnName1 = changes.isAddressFieldChanged("ZP01", "Customer Name Chinese");
    boolean zp01EnName2 = changes.isAddressFieldChanged("ZP01", "Customer Name Con't English");
    boolean zp01CnName2 = changes.isAddressFieldChanged("ZP01", "Customer Name Con't Chinese");
    boolean zp01EnName3 = changes.isAddressFieldChanged("ZP01", "Customer Name Con't 2 English");
    boolean zp01CnName3 = changes.isAddressFieldChanged("ZP01", "Customer Name Con't Chinese 2");
    boolean zp01EnAddr1 = changes.isAddressFieldChanged("ZP01", "Street Address English");
    boolean zp01CnAddr1 = changes.isAddressFieldChanged("ZP01", "Street Address Chinese");
    boolean zp01EnAddr2 = changes.isAddressFieldChanged("ZP01", "Address Con't English");
    boolean zp01CnAddr2 = changes.isAddressFieldChanged("ZP01", "Street Address Con't Chinese");

    // check if ZD01 name and address have been updated
    boolean zd01EnName1 = changes.isAddressFieldChanged("ZD01", "Customer Name English");
    boolean zd01CnName1 = changes.isAddressFieldChanged("ZD01", "Customer Name Chinese");
    boolean zd01EnName2 = changes.isAddressFieldChanged("ZD01", "Customer Name Con't English");
    boolean zd01CnName2 = changes.isAddressFieldChanged("ZD01", "Customer Name Con't Chinese");
    boolean zd01EnName3 = changes.isAddressFieldChanged("ZD01", "Customer Name Con't 2 English");
    boolean zd01CnName3 = changes.isAddressFieldChanged("ZD01", "Customer Name Con't Chinese 2");
    boolean zd01EnAddr1 = changes.isAddressFieldChanged("ZD01", "Street Address English");
    boolean zd01CnAddr1 = changes.isAddressFieldChanged("ZD01", "Street Address Chinese");
    boolean zd01EnAddr2 = changes.isAddressFieldChanged("ZD01", "Address Con't English");
    boolean zd01CnAddr2 = changes.isAddressFieldChanged("ZD01", "Street Address Con't Chinese");

    // validation
    if (zs01EnName1 || zs01CnName1 || zs01EnName2 || zs01CnName2 || zs01EnName3 || zs01CnName3 || zs01EnAddr1 || zs01CnAddr1 || zs01EnAddr2
        || zs01CnAddr2 || zi01EnName1 || zi01CnName1 || zi01EnName2 || zi01CnName2 || zi01EnName3 || zi01CnName3 || zi01EnAddr1 || zi01CnAddr1
        || zi01EnAddr2 || zi01CnAddr2 || zp01EnName1 || zp01CnName1 || zp01EnName2 || zp01CnName2 || zp01EnName3 || zp01CnName3 || zp01EnAddr1
        || zp01CnAddr1 || zp01EnAddr2 || zp01CnAddr2 || zd01EnName1 || zd01CnName1 || zd01EnName2 || zd01CnName2 || zd01EnName3 || zd01CnName3
        || zd01EnAddr1 || zd01CnAddr1 || zd01EnAddr2 || zd01CnAddr2) {

      return true;
    }
    return false;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {

    Data data = requestData.getData();
    StringBuilder details = new StringBuilder();
    CNHandler handler = (CNHandler) RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    List<Addr> addrs = handler.getAddrByReqId(entityManager, data.getId().getReqId());
    Addr zs01addr = requestData.getAddress("ZS01");
    IntlAddr intlZS01Addr = handler.getIntlAddrById(zs01addr, entityManager);

    if (addrs.size() > 0) {
      for (int i = 0; i < addrs.size(); i++) {
        Addr addr = addrs.get(i);
        addr.setCustNm1(zs01addr.getCustNm1());
        addr.setCustNm2(zs01addr.getCustNm2());
        addr.setCustNm3(zs01addr.getCustNm3());
        addr.setCustNm4(zs01addr.getCustNm4());
        IntlAddr intlAddr = handler.getIntlAddrById(addr, entityManager);
        intlAddr.setIntlCustNm1(intlZS01Addr.getIntlCustNm1());
        intlAddr.setIntlCustNm2(intlZS01Addr.getIntlCustNm2());
        intlAddr.setIntlCustNm3(intlZS01Addr.getIntlCustNm3());
        intlAddr.setIntlCustNm4(intlZS01Addr.getIntlCustNm4());
        entityManager.merge(addr);
        entityManager.merge(intlAddr);
        entityManager.flush();
      }
      details.append("Chinese&English name updated automatically for all address types.");
      validation.setSuccess(true);
      validation.setMessage("Successful");
    } else {
      details.append("Chinese&English name no need to be updated automatically.");
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }

    output.setDetails(details.toString());
    output.setProcessOutput(validation);

    return true;
  }

  public String geDocContent(EntityManager entityManager, long req_id) {
    String sql = ExternalizedQuery.getSql("QUERY.CHECK_CN_API_ATTACHMENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", req_id);
    String result = "N";
    List<String> records = query.getResults(String.class);
    if (records != null && records.size() > 0) {
      result = "Y";
    }
    LOG.debug("result" + result);
    return result;
  }

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("C", "U", "M", "D", "R");
  }

  @Override
  public void tweakDnBMatchingRequest(GBGFinderRequest request, RequestData requestData, AutomationEngineData engineData) {
    Data data = requestData.getData();
    if (StringUtils.isNotBlank(data.getBusnType())) {
      request.setOrgId(data.getBusnType());
      request.setMinConfidence("9");
    }
  }
}
