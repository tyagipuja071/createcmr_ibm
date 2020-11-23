package com.ibm.cio.cmr.request.automation.util.geo.us;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;

public class USBPDevelopHandler extends USBPHandler {

  private static final Logger LOG = Logger.getLogger(USBPDevelopHandler.class);
  private static List<String> demoDev = Arrays.asList("DEMO DEVELOPMENT", "DEMO DEV", "DEVELOPMENT");
  private static List<String> leaseDev = Arrays.asList("ICC LEASE DEVELOPMENT/DEV", "IDL LEASE DEVELOPMENT/DEV");
  private String cmrType;
  private static final String T1 = "T1";
  private static final String T2 = "T2";

  @Override
  public boolean doInitialValidations(Admin admin, Data data, Addr addr, AutomationResult<OverrideOutput> output, AutomationEngineData engineData) {
    LOG.debug(">>>> Executing inital validations <<<<");
    if (StringUtils.isNotBlank(data.getEnterprise())) {
      USCeIdMapping mapping = USCeIdMapping.getByEnterprise(data.getEnterprise());
      if (mapping == null || !mapping.isDistributor()) {
        output.setResults("Non-Distributor BP");
        output.setDetails(
            "BP Development records are only allowed to be created under Distributors, please check and confirm with the Distributor for this transaction.");
        engineData.addRejectionComment("ENT",
            "BP Development records are only allowed to be created under Distributors, please check and confirm with the Distributor for this transaction.",
            "", "");
        output.setOnError(true);
        return true;
      }
    } else {
      output.setResults("Invalid Enterprise");
      output.setDetails(
          "BP Development records are only allowed to be created under Distributors, please check and confirm with the Distributor for this transaction.");
      engineData.addRejectionComment("ENT",
          "BP Development records are only allowed to be created under Distributors, please check and confirm with the Distributor for this transaction.",
          "", "");
      output.setOnError(true);
      return true;
    }

    if (StringUtils.isNotBlank(data.getPpsceid()) && !AutomationUtil.checkPPSCEID(data.getPpsceid())) {
      output.setResults("Invalid CEID");
      output.setDetails("The CEID provided on the request is not valid, please check and confirm.\n");
      engineData.addRejectionComment("CEID", "The CEID provided on the request is not valid, please check and confirm.", "", "");
      output.setOnError(true);
      return true;
    }

    return false;

  }

  @Override
  public boolean processRequest(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<OverrideOutput> output, StringBuilder details, boolean childCompleted, FindCMRRecordModel ibmCmr, RequestData childRequest,
      GEOHandler handler, OverrideOutput overrides) throws Exception {

    LOG.debug(">>>> Executing process requests  <<<<");

    Admin admin = requestData.getAdmin();
    Addr zs01 = requestData.getAddress("ZS01");
    Data data = requestData.getData();
    long childReqId = admin.getChildReqId();

    String mainCustNm = admin.getMainCustNm1() + (StringUtils.isNotBlank(admin.getMainCustNm2()) ? " " + admin.getMainCustNm2() : "");
    String endUserNm = (StringUtils.isNotBlank(zs01.getDivn()) ? zs01.getDivn() : "");
    if ((StringUtils.isNotBlank(data.getEnterprise()) && data.getEnterprise().equals(data.getAffiliate()))
        || (StringUtils.isBlank(data.getAffiliate()) && AutomationUtil.getCleanString(mainCustNm).equals(AutomationUtil.getCleanString(endUserNm)))) {
      this.cmrType = T1;
      details.append("Processing BP Development request for Scenario #1\n");
    } else {
      this.cmrType = T2;
      details.append("Processing BP Development request for Scenario #2\n");
    }

    if (T2.equals(this.cmrType) && !AutomationUtil.checkPPSCEID(data.getPpsceid())) {
      output.setResults("Invalid CEID");
      output.setDetails("Only BP with valid CEID is allowed to setup a T2 Ehost record, please check and confirm.");
      engineData.addRejectionComment("CEID", "Only BP with valid CEID is allowed to setup a T2 Ehost record, please check and confirm.", "", "");
      output.setOnError(true);
      return false;
    }

    String childCmrNo = null;

    if (childRequest != null) {
      childCmrNo = childRequest.getData().getCmrNo();
    }
    // prioritize child request here
    if (childCompleted) {
      details.append("Copying CMR values direct CMR " + childCmrNo + " from Child Request " + childReqId + ".\n");
      ibmCmr = createIBMCMRFromChild(childRequest);
    } else {
      // check IBM Direct CMR
      if (ibmCmr == null) {
        // if a rejected child caused the retrieval of a child cmr
        ibmCmr = findIBMCMR(entityManager, handler, requestData, zs01, engineData, childCmrNo);
      }
      if (ibmCmr != null) {
        details.append("Copying CMR values CMR " + ibmCmr.getCmrNum() + " from FindCMR.\n");
        LOG.debug("IBM Direct CMR Found: " + ibmCmr.getCmrNum() + " - " + ibmCmr.getCmrName());
      }
    }

    // match against D&B
    DnBMatchingResponse dnbMatch = matchAgainstDnB(handler, requestData, zs01, engineData, details, overrides, ibmCmr != null);

    if (ibmCmr == null) {

      LOG.debug("No IBM Pool CMR for the end user.");
      childReqId = createChildRequest(entityManager, requestData, engineData);
      details.append("No IBM Pool CMR for the end user.\n");

      if (childCompleted) {
        String childError = "Child Request " + childReqId + " was completed but Pool CMR cannot be determined. Manual validation needed.";
        details.append(childError + "\n");
        engineData.addNegativeCheckStatus("_usChildError", childError);
        output.setDetails(details.toString());
        output.setResults("Issues Encountered");
        return false;
      } else {
        String childErrorMsg = "- IBM Pool CMR request creation cannot be done, errors were encountered -";
        if (childReqId <= 0) {
          details.append(childErrorMsg + "\n");
          // engineData.addNegativeCheckStatus("_usBpRejected", childErrorMsg);
          engineData.addRejectionComment("OTH", childErrorMsg, "", "");
          output.setDetails(details.toString());
          output.setOnError(true);
          output.setResults("Issues Encountered");
          return false;
        } else {
          String childDetails = completeChildRequestDataAndAddress(entityManager, requestData, engineData, childReqId, dnbMatch);
          if (childDetails == null) {
            details.append(childErrorMsg + "\n");
            // engineData.addNegativeCheckStatus("_usBpRejected",
            // childErrorMsg);
            engineData.addRejectionComment("OTH", childErrorMsg, "", "");
            output.setOnError(true);
            output.setResults("Issues Encountered");
            output.setDetails(details.toString());
            return false;
          } else {
            details.append("Child Request " + childReqId + " created for the Pool CMR record of " + zs01.getDivn()
                + (!StringUtils.isBlank(zs01.getDept()) ? " " + zs01.getDept() : "")
                + ".\nThe system will wait for completion of the child record bfore processing the request.\n");
            details.append(childDetails + "\n");
            setWaiting(true);
            output.setDetails(details.toString());
            output.setResults("Waiting on Child Request");
            output.setOnError(false);
            return false;
          }

        }
      }
    }
    return true;
  }

  @Override
  public void copyAndFillIBMData(EntityManager entityManager, GEOHandler handler, FindCMRRecordModel ibmCmr, RequestData requestData,
      AutomationEngineData engineData, StringBuilder details, OverrideOutput overrides, RequestData childRequest) {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    LOG.debug(">>>> Executing copyAndFillIBMData  <<<<");
    if (ibmCmr != null) {

      if (!StringUtils.isBlank(ibmCmr.getCmrSapNumber())) {
        details.append(
            "\nCopying IBM Codes from IBM CMR " + ibmCmr.getCmrNum() + " - " + ibmCmr.getCmrName() + " (" + ibmCmr.getCmrSapNumber() + "): \n");
      } else {
        details.append("\nCopying IBM Codes from IBM CMR " + ibmCmr.getCmrNum() + " - " + ibmCmr.getCmrName() + ": \n");
      }

      if (!StringUtils.isBlank(ibmCmr.getCmrIsu())) {
        details.append(" - ISU: " + ibmCmr.getCmrIsu() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "ISU_CD", data.getIsuCd(), ibmCmr.getCmrIsu());
        details.append(" - Client Tier: " + ibmCmr.getCmrTier() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CLIENT_TIER", data.getClientTier(), ibmCmr.getCmrTier());
      }

      if (!StringUtils.isBlank(ibmCmr.getCmrInac())) {
        details.append(" - NAC/INAC: " + ("I".equals(ibmCmr.getCmrInacType()) ? "INAC" : ("N".equals(ibmCmr.getCmrInacType()) ? "NAC" : "-")) + " "
            + ibmCmr.getCmrInac() + (ibmCmr.getCmrInacDesc() != null ? "( " + ibmCmr.getCmrInacDesc() + ")" : ""));
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "INAC_TYPE", data.getInacType(), ibmCmr.getCmrInacType());
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "INAC_CD", data.getInacCd(), ibmCmr.getCmrInac());
      }

      // add here gbg and cov
      LOG.debug("Getting Buying Group/Coverage values");
      String bgId = ibmCmr.getCmrBuyingGroup();
      GBGResponse calcGbg = new GBGResponse();
      if (!StringUtils.isBlank(bgId)) {
        calcGbg.setBgId(ibmCmr.getCmrBuyingGroup());
        calcGbg.setBgName(ibmCmr.getCmrBuyingGroupDesc());
        calcGbg.setCmrCount(1);
        calcGbg.setGbgId(ibmCmr.getCmrGlobalBuyingGroup());
        calcGbg.setGbgName(ibmCmr.getCmrGlobalBuyingGroupDesc());
        calcGbg.setLdeRule(ibmCmr.getCmrLde());
      } else {
        calcGbg.setBgId("BGNONE");
        calcGbg.setBgName("None");
        calcGbg.setCmrCount(1);
        calcGbg.setGbgId("GBGNONE");
        calcGbg.setGbgName("None");
        calcGbg.setLdeRule("BG_DEFAULT");
      }
      if (!StringUtils.isBlank(calcGbg.getGbgId())) {
        details.append(" - GBG: " + calcGbg.getGbgId() + "(" + (StringUtils.isBlank(calcGbg.getGbgName()) ? "not specified" : calcGbg.getGbgName())
            + ")" + "\n");
      } else {
        details.append(" - GBG: none\n");
      }
      if (!StringUtils.isBlank(calcGbg.getBgId())) {
        details.append(
            " - BG: " + calcGbg.getBgId() + "(" + (StringUtils.isBlank(calcGbg.getBgName()) ? "not specified" : calcGbg.getBgName()) + ")" + "\n");
      } else {
        details.append(" - BG: none\n");
      }
      if (!StringUtils.isBlank(calcGbg.getBgId())) {
        details.append(" - LDE Rule: " + calcGbg.getLdeRule() + "\n");
      } else {
        details.append(" - LDE Rule: none\n");
      }
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
      engineData.put(AutomationEngineData.GBG_MATCH, calcGbg);

      LOG.debug("BG ID: " + calcGbg.getBgId());
      String calcCovId = ibmCmr.getCmrCoverage();
      if (StringUtils.isBlank(calcCovId)) {
        calcCovId = RequestUtils.getDefaultCoverage(entityManager, "US");
      }
      details.append(" - Coverage: " + calcCovId + (ibmCmr.getCmrCoverageName() != null ? " (" + ibmCmr.getCmrCoverageName() + ")" : "") + "\n");
      LOG.debug("Coverage: " + calcCovId);
      engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
      engineData.put(AutomationEngineData.COVERAGE_CALCULATED, calcCovId);

      details.append(" - SICMEN: " + ibmCmr.getCmrIsic() + "\n");
      details.append(" - ISIC: " + ibmCmr.getCmrIsic() + "\n");
      details.append(" - Subindustry: " + ibmCmr.getCmrSubIndustry() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "ISIC_CD", data.getIsicCd(), ibmCmr.getCmrIsic());
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "US_SICMEN", data.getUsSicmen(), ibmCmr.getCmrIsic());
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SUB_INDUSTRY_CD", data.getSubIndustryCd(), ibmCmr.getCmrSubIndustry());
    }

    if ("T1".equals(this.cmrType)) {
      LOG.debug("Overriding details for Pool CMR.");

      details.append(" - Affiliate: " + data.getEnterprise() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "AFFILIATE", data.getAffiliate(), data.getEnterprise());

      details.append(" - Tax Class / Code 1: " + "J000" + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", " TAX_CD1", data.getTaxCd1(), "J000");

      details.append(" - Tax Exempt Status: " + "Z" + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SPECIAL_TAX_CD", data.getSpecialTaxCd(), "Z");

    } else if ("T2".equals(this.cmrType)) {
      LOG.debug("Overriding details for T2 Pool CMR.");

      details.append(" - Affiliate: " + data.getEnterprise() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "AFFILIATE", data.getAffiliate(), data.getEnterprise());

      details.append(" - Tax Class / Code 1: " + "J666" + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", " TAX_CD1", data.getTaxCd1(), "J666");

      details.append(" - Tax Exempt Status: " + "" + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SPECIAL_TAX_CD", data.getSpecialTaxCd(), "");

    }

    createAddressOverrides(entityManager, handler, ibmCmr, requestData, engineData, details, overrides, childRequest);

    details.append(" - Dept/Attn: -------DEVELOPEMENT-------\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "DEPT", zs01.getDept(), "-------DEVELOPMENT-------");
    details.append(" - Restricted Ind: Y\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_IND", data.getRestrictInd(), "Y");
    details.append(" - Restricted to: BPQS\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_TO", data.getRestrictTo(), "BPQS");
    details.append(" - BP Account Type: " + "D" + "\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_ACCT_TYP", data.getBpAcctTyp(), "D");
    details.append(" - Marketing Dept: " + "EI3" + "\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MKTG_DEPT", data.getMktgDept(), "EI3");
    details.append(" - Miscellaneous Bill Code: " + "I" + "\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MISC_BILL_CD", data.getMiscBillCd(), "I");
    details.append(" - Business Partner Name: " + "MIR" + "\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_NAME", data.getBpName(), BP_MANAGING_IR);
    details.append(" - PCC A/R Dept: " + "G8M" + "\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "PCC_AR_DEPT", data.getPccArDept(), "G8M");
    details.append(" - SVC A/R Office : " + "IKE" + "\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SVC_AR_OFFICE", data.getSvcArOffice(), "IKE");

    String department = StringUtils.isNotBlank(zs01.getDept()) ? zs01.getDept().toUpperCase() : "";
    String addressCategory = "";
    for (String demo : demoDev) {
      if (department.contains(demo)) {
        addressCategory = "DEMO";
        break;
      }
    }

    for (String lease : leaseDev) {
      if (department.contains(lease)) {
        addressCategory = "LEASE";
        break;
      }
    }

    if ("DEMO".equals(addressCategory)) {
      LOG.debug("Address category is Demo Development.");
      details.append(" - CSO Site : " + "YBV" + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CSO_SITE", data.getCsoSite(), "YBV");

      details.append(" - Marketing A/R Dept  : " + "DI3" + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), "DI3");

    } else if ("LEASE".equals(addressCategory)) {
      LOG.debug("Address category is Lease Development.");
      details.append(" - CSO Site : " + "TF7" + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CSO_SITE", data.getCsoSite(), "TF7");

      details.append(" - Marketing A/R Dept  : " + "DI2" + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), "DI2");
    }
  }

  @Override
  public void doFinalValidations(AutomationEngineData engineData, RequestData requestData, StringBuilder details, OverrideOutput overrides,
      AutomationResult<OverrideOutput> result, FindCMRRecordModel ibmCmr) {
    Data data = requestData.getData();
    // details.append("\n");
    // String affiliate = data.getAffiliate();
    // if (ibmCmr != null && !StringUtils.isBlank(ibmCmr.getCmrAffiliate())) {
    // affiliate = ibmCmr.getCmrAffiliate();
    // }
    // if (StringUtils.isBlank(affiliate)) {
    // details.append("\nAffiliate cannot be computed automatically.");
    // engineData.addNegativeCheckStatus("_usBpAff", "Affiliate cannot be
    // computed automatically");
    // }

    USCeIdMapping mapping = USCeIdMapping.getByEnterprise(data.getEnterprise());
    if (mapping != null) {
      if (!mapping.getCompanyNo().equals(data.getCompany())) {
        details.append("\nCompany No. updated to mapped value for the CEID (" + mapping.getCompanyNo() + ").");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "COMPANY", data.getCompany(), mapping.getCompanyNo());
      }
    } else {
      details.append("\nEnterprise No. cannot be validated automatically.");
      engineData.addNegativeCheckStatus("_usBpEnt", "Enterprise No. cannot be validated automatically.");
    }
  }

  @Override
  protected FindCMRRecordModel getIBMCMRBestMatch(AutomationEngineData engineData, RequestData requestData, List<DuplicateCMRCheckResponse> matches)
      throws CmrException {

    for (DuplicateCMRCheckResponse record : matches) {
      LOG.debug(" - Duplicate: (Restrict To: " + record.getUsRestrictTo() + ", Grade: " + record.getMatchGrade() + ")" + record.getCompany() + " - "
          + record.getCmrNo() + " - " + record.getAddrType() + " - " + record.getStreetLine1());
      // IBM Direct CMRs have blank restrict to
      LOG.debug("CMR No. " + record.getCmrNo() + " matches " + this.cmrType + " Pool CMR Criteria. Getting CMR Details..");
      String overrides = "addressType=ZS01&cmrOwner=IBM&showCmrType=R&customerNumber=" + record.getCmrNo();
      FindCMRResultModel result = CompanyFinder.getCMRDetails(SystemLocation.UNITED_STATES, record.getCmrNo(), 5, null, overrides);
      if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
        return result.getItems().get(0);
      }
    }
    return null;

  }

  @Override
  protected void tweakFindCMRRequest(EntityManager entityManager, GEOHandler handler, RequestData requestData, DuplicateCMRCheckRequest request) {
    if (T1.equals(this.cmrType)) {
      LOG.debug("adding scenario specific request params >>>> ");
      LOG.debug("Restricted To: " + RESTRICT_TO_END_USER);
      request.setUsRestrictTo(RESTRICT_TO_END_USER);
      LOG.debug("BP Acc Type: P");
      request.setUsBpAccType("P");
    } else if (T2.equals(this.cmrType)) {
      LOG.debug("adding scenario specific request params >>>> ");
      LOG.debug("Restricted To: " + RESTRICT_TO_END_USER);
      request.setUsRestrictTo(RESTRICT_TO_END_USER);
      LOG.debug("CSO Site: TT2");
      request.setUsCsoSite("TT2");
    }
    LOG.trace(request);
  }

  @Override
  protected void performAction(AutomationEngineData engineData, String msg) {
    engineData.addNegativeCheckStatus("_usBpNoMatch", msg);
  }

  @Override
  protected void setChildRequestScenario(Data data, Data childData, Admin childAdmin, StringBuilder details) {
    if (childData != null) {
      LOG.debug("Setting child request's scenarios ...");
      childData.setCustGrp(USUtil.CG_THIRD_P_BUSINESS_PARTNER);
      childData.setCustSubGrp(USUtil.SC_BP_POOL);
      childAdmin.setCustType(USUtil.BUSINESS_PARTNER);

    }
  }

  @Override
  public boolean isChildRequestSupported() {
    return true;
  }

  @Override
  protected void modifyChildDataValues(RequestData requestData, RequestData childReqData, StringBuilder details) {
    Data childData = childReqData.getData();
    if (T1.equals(this.cmrType)) {
      childData.setBpAcctTyp("P");
      childData.setCsoSite("YBV");
    } else {
      childData.setCsoSite("TT2");
    }

    if (StringUtils.isNotBlank(childData.getPpsceid())) {
      childData.setMemLvl("IM");
    }

    childData.setEnterprise(requestData.getData().getAffiliate());

  }
}
