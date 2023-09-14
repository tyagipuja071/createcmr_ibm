package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
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
import com.ibm.cio.cmr.request.entity.IntlAddrPK;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;

public class ChinaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(ChinaUtil.class);
  public static final String SCENARIO_LOCAL_NRMLC = "NRMLC";
  public static final String SCENARIO_LOCAL_EMBSA = "EMBSA";
  public static final String SCENARIO_CROSS_CROSS = "CROSS";
  public static final String SCENARIO_LOCAL_AQSTN = "AQSTN";
  public static final String SCENARIO_LOCAL_BLUMX = "BLUMX";
  public static final String SCENARIO_LOCAL_MRKT = "MRKT";
  public static final String SCENARIO_LOCAL_BUSPR = "BUSPR";
  public static final String SCENARIO_LOCAL_INTER = "INTER";
  public static final String SCENARIO_LOCAL_PRIV = "PRIV";
  public static final String SCENARIO_ECOSYTEM_PARNER = "ECOSY";
  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO);
  private static final List<String> RELEVANT_ADDRESS_FIELDS_ENHANCEMENT = Arrays.asList("City English", "City Chinese", "District English",
      "District Chinese", "Department English", "Building English");

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

    // case SCENARIO_LOCAL_NRML:
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
    // case SCENARIO_LOCAL_EMBSA:
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
    // case SCENARIO_LOCAL_AQSTN:
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
    case SCENARIO_CROSS_CROSS:
      boolean companyProofProvided = DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId());
      if (companyProofProvided) {
        details.append("Supporting documentation(Company Proof) is provided by the requester as attachment").append("\n");
        details.append("This Foreign Request will be routed to CMDE.\n");
        engineData.addRejectionComment("OTH", "This Foreign Request will be routed to CMDE.", "", "");
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
      // if ("08036".equals(data.getSearchTerm())) {
      // LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + "
      // Scenario=" + data.getCustSubGrp());
      // result.setOnError(false);
      // details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + "
      // Scenario=" + data.getCustSubGrp() + " for the request.\n");
      // } else {
      // details
      // .append("Cluster=" + data.getSearchTerm() + " should be default (08036)
      // for Scenario=" + data.getCustSubGrp() + " for the request.\n");
      // engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm()
      // + " should default (08036) for this scenario", "", "");
      // result.setOnError(true);
      // }
      break;
    case SCENARIO_LOCAL_BUSPR:
      if ("00075".equals(data.getSearchTerm())) {
        LOG.debug("Cluster allowed: Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp());
        result.setOnError(false);
        details.append("Cluster allowed:Cluster=" + data.getSearchTerm() + " Scenario=" + data.getCustSubGrp() + " for the request.\n");
      } else {
        details
            .append("Cluster=" + data.getSearchTerm() + " should be default (00075)  for Scenario=" + data.getCustSubGrp() + " for the request.\n");
        engineData.addRejectionComment("OTH", "Cluster=" + data.getSearchTerm() + " should default (00075)  for this scenario", "", "");
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

      // if (StringUtils.isNotBlank(customerName) && customerName.indexOf("IBM
      // China") >= 0) {
      // LOG.debug("English name=" + customerName + " for Scenario=" +
      // data.getCustSubGrp());
      // result.setOnError(false);
      // details.append("English name=" + customerName + " for Scenario=" +
      // data.getCustSubGrp() + " for the request.\n");
      // } else {
      // details.append("English name should include 'IBM China' for Scenario="
      // + data.getCustSubGrp() + " for the request.\n");
      // engineData.addRejectionComment("OTH", "English name should include 'IBM
      // China' for this scenario", "", "");
      // result.setOnError(true);
      // }
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
      details.append("An attachment of type 'Name and Address Change(China Specific)' has been added. This Requester will be routed to CMDE.\n");
      engineData.addRejectionComment("OTH",
          "An attachment of type 'Name and Address Change(China Specific)' has been added. This Requester will be routed to CMDE", "", "");
      engineData.addNegativeCheckStatus("OTH",
          "An attachment of type 'Name and Address Change(China Specific)' has been added. This Requester will be routed to CMDE");
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
      } else if (StringUtils.isNotBlank(scenario) && (SCENARIO_LOCAL_MRKT.equals(scenario) || SCENARIO_LOCAL_BLUMX.equals(scenario))) {
        List<String> managerID = SystemParameters.getList("AUTO_CN_MRKT_BLUMX_MGR");
        boolean managerCheck = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), managerID);
        if (!managerCheck) {
          details.append(
              "Please double check if your request scenario subtype is 'Bluemix or MaketPlace',for which request must be from DSW team and first line manager is "
                  + (managerID != null && managerID.size() > 0 ? managerID.get(0) : "")
                  + ", else please change your request scenario subtype into 'Normal/ESA/Acquistion/Ecosystem partners' based on your business requirement.");
          engineData.addRejectionComment("OTH",
              "Please double check if your request scenario subtype is 'Bluemix or MaketPlace',for which request must be from DSW team and first line manager is "
                  + (managerID != null && managerID.size() > 0 ? managerID.get(0) : "")
                  + ", else please change your request scenario subtype into 'Normal/ESA/Acquistion/Ecosystem partners' based on your business requirement.",
              "", "");
          results.setOnError(true);
          results.setResults("Requester check fail");
        } else {
          details.append("Skipping validation for requester must be from DSW team for requester - " + admin.getRequesterId() + ".\n");
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
    List<String> managerID = SystemParameters.getList("AUTO_CN_MGR_BP_LIST");
    boolean managerCheck = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), managerID);

    if (StringUtils.isNotBlank(scenario) && SCENARIO_LOCAL_BUSPR.equals(scenario) || "00075".equals(data.getSearchTerm())
        || (StringUtils.isBlank(data.getSearchTerm()) || data.getSearchTerm().equals("00000") || !StringUtils.isNumeric(data.getSearchTerm()))
            && (data.getCmrNo().startsWith("1") || data.getCmrNo().startsWith("2"))) {
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
      if (managerCheck) {
        details.append("BPCM team is not allowed to update information for End User.Thank You! Squad Leader:"
            + (managerID != null && managerID.size() > 0 ? managerID.get(0) : ""));
        engineData.addRejectionComment("OTH", "BPCM team is not allowed to update information for End User.Thank You! Squad Leader:"
            + (managerID != null && managerID.size() > 0 ? managerID.get(0) : ""), "", "");
        output.setOnError(true);
        validation.setSuccess(false);
        validation.setMessage("Rejected");
      } else {
        details.append("Skipping validation for this scenario. scenario = " + scenario + ".\n");
        validation.setSuccess(true);
        validation.setMessage("Successful");
      }
    }

    String ret = geDocContent(entityManager, admin.getId().getReqId());
    if ("Y".equals(ret)) {
      details.append("An attachment of type 'Name and Address Change(China Specific)' has been added. This Requester will be routed to CMDE.\n");
      engineData.addRejectionComment("OTH",
          "An attachment of type 'Name and Address Change(China Specific)' has been added. This Requester will be routed to CMDE", "", "");
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
    } else if (checkExtraUpdateFields(requestData, changes)) {
      return true;
    }
    return false;
  }

  private boolean checkExtraUpdateFields(RequestData requestData, RequestChangeContainer changes) {
    List<Addr> addresses = null;
    for (String addrType : RELEVANT_ADDRESSES) {
      if (changes.isAddressChanged(addrType)) {
        if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
          addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
        } else {
          addresses = requestData.getAddresses(addrType);
        }
        for (Addr addr : addresses) {
          if (addr != null) {
            List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
            if (addrChanges == null) {
              return false;
            }
            for (UpdatedNameAddrModel change : addrChanges) {
              if (RELEVANT_ADDRESS_FIELDS_ENHANCEMENT.contains(change.getDataField())) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    StringBuilder details = new StringBuilder();
    CNHandler handler = (CNHandler) RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    List<Addr> addrs = handler.getAddrByReqId(entityManager, data.getId().getReqId());
    Addr zs01addr = requestData.getAddress("ZS01");
    IntlAddr intlZS01Addr = handler.getIntlAddrById(zs01addr, entityManager);
    boolean cmdeReview = false;
    List<Addr> addresses = null;

    if (addrs.size() > 0) {
      for (int i = 0; i < addrs.size(); i++) {
        Addr addr = addrs.get(i);
        if ("ZS01".equals(addr.getId().getAddrType()) && !"CN".equals(addr.getLandCntry())) {
          boolean companyProofProvided = DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId());
          if (companyProofProvided) {
            details.append("Supporting documentation(Company Proof) is provided by the requester as attachment").append("\n");
            details.append("This Foreign Request will be routed to CMDE.\n");
            engineData.addRejectionComment("OTH", "This Foreign Request will be routed to CMDE.", "", "");
            return false;
          }
        }
        addr.setCustNm1(zs01addr.getCustNm1());
        addr.setCustNm2(zs01addr.getCustNm2());
        addr.setCustNm3(zs01addr.getCustNm3());
        addr.setCustNm4(zs01addr.getCustNm4());
        IntlAddr intlAddr = handler.getIntlAddrById(addr, entityManager);
        if (intlAddr == null) {
          intlAddr = new IntlAddr();
          IntlAddrPK intlAddrPK = new IntlAddrPK();
          intlAddrPK.setAddrSeq(addr.getId().getAddrSeq());
          intlAddrPK.setAddrType(addr.getId().getAddrType());
          intlAddrPK.setReqId(addr.getId().getReqId());
          intlAddr.setId(intlAddrPK);
          intlAddr.setLangCd("1");
          intlAddr.setAddrTxt(intlZS01Addr.getAddrTxt());
          intlAddr.setCity1(intlZS01Addr.getCity1());
          intlAddr.setCity2(intlZS01Addr.getCity2());
        }
        intlAddr.setIntlCustNm1(intlZS01Addr.getIntlCustNm1());
        intlAddr.setIntlCustNm2(intlZS01Addr.getIntlCustNm2());
        intlAddr.setIntlCustNm3(intlZS01Addr.getIntlCustNm3());
        intlAddr.setIntlCustNm4(intlZS01Addr.getIntlCustNm4());
        entityManager.merge(addr);
        entityManager.merge(intlAddr);
        entityManager.flush();
      }
      details.append("Chinese&English name updated automatically for all address types.\n");
      validation.setSuccess(true);
      validation.setMessage("Successful");
    } else {
      details.append("Chinese&English name no need to be updated automatically.\n");
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }

    for (String addrType : RELEVANT_ADDRESSES) {
      if (changes.isAddressChanged(addrType)) {
        if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
          addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
        } else {
          addresses = requestData.getAddresses(addrType);
        }
        for (Addr addr : addresses) {
          if ("Y".equals(addr.getChangedIndc())) {
            if (RELEVANT_ADDRESSES.contains(addrType)) {
              // if (isRelevantAddressFieldUpdated(changes, addr)) {
              // if customer name has been updated on any address , simply
              // send to CMDE
              List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
              for (UpdatedNameAddrModel change : addrChanges) {
                if (("Customer Name English".equals(change.getDataField()) || "Customer Name Chinese".equals(change.getDataField())
                    || "Customer Name Con't English".equals(change.getDataField()) || "Customer Name Con't Chinese".equals(change.getDataField())
                    || "Customer Name Con't 2 English".equals(change.getDataField()) || "Customer Name Con't Chinese 2".equals(change.getDataField()))
                    && CmrConstants.RDC_SOLD_TO.equalsIgnoreCase(addr.getId().getAddrType())) {
                  // CMDE Review
                  cmdeReview = true;
                  details.append(
                      "Update of Customer Name by the requester for " + addrType + "(" + addr.getId().getAddrSeq() + ") needs to be verified.\n");
                  engineData.addPositiveCheckStatus("Update of Customer Name for " + addrType + "(" + addr.getId().getAddrSeq()
                      + ") needs to be verified. Forwarding request to CMDE");
                }
              }
            }
          }
        }
      }
    }

    if (cmdeReview) {
      engineData.addNegativeCheckStatus("_esCheckFailed", "Updated elements cannot be checked automatically.");
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
    } else {
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

}
