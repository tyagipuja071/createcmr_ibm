package com.ibm.cio.cmr.request.automation.util.geo.us;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;

/**
 * 
 * @author RoopakChugh
 *
 */
public class USBPEndUserHandler extends USBPHandler {
  private static final Logger LOG = Logger.getLogger(USBPEndUserHandler.class);
  public static final List<String> SPECIAL_TAX_STATES = Arrays.asList("AK", "DE", "MT", "NH", "OR");
  public static final List<String> FEDERAL_CLASSIFIED_ISIC = Arrays.asList("9042", "9043", "9065", "9121", "9185", "9186", "9195", "9199", "9200",
      "9203", "9204", "9240", "9269");

  @Override
  public boolean doInitialValidations(Admin admin, Data data, Addr addr, AutomationResult<OverrideOutput> output, AutomationEngineData engineData) {
    // check the scenario
    String custGrp = data.getCustGrp();
    String custSubGrp = data.getCustSubGrp();
    if ("BYMODEL".equals(custSubGrp)) {
      String type = admin.getCustType();
      if (!USUtil.BUSINESS_PARTNER.equals(type) || !"E".equals(data.getBpAcctTyp())
          || (!RESTRICT_TO_END_USER.equals(data.getRestrictTo()) && !RESTRICT_TO_MAINTENANCE.equals(data.getRestrictTo()))) {
        output.setResults("Skipped");
        output.setDetails("Non BP End User create by model scenario not supported.");
        return true;
      }
      // CMR-3856 add check for ehosting
      String deptAttn = addr.getDept() != null ? addr.getDept().toLowerCase() : "";
      if (deptAttn.contains("ehost") || deptAttn.contains("e-host") || deptAttn.contains("e host")) {
        output.setResults("Skipped");
        output.setDetails("Non BP End User create by model scenario not supported.");
        return true;
      }
    } else if ((!TYPE_BUSINESS_PARTNER.equals(custGrp) || !SUB_TYPE_BUSINESS_PARTNER_END_USER.equals(custSubGrp))
        && !SUB_TYPE_FSP_END_USER.equals(custSubGrp)) {
      output.setResults("Skipped");
      output.setDetails("Non BP End User or Non FSP End User scenario not supported.");
      return true;
    }

    // if (StringUtils.isBlank(data.getPpsceid())) {
    // String msg = "PPS CEID is required for Business Partner requests.";
    // engineData.addRejectionComment("OTH", msg, "", "");
    // output.setOnError(true);
    // output.setDetails(msg);
    // output.setResults("CEID Missing");
    // return true;
    // }

    return false;
  }

  @Override
  public boolean processRequest(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<OverrideOutput> output, StringBuilder details, boolean childCompleted, RequestData childRequest, GEOHandler handler,
      FindCMRRecordModel ibmCmr, OverrideOutput overrides) throws Exception {

    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    Addr addr = requestData.getAddress("ZS01");
    long childReqId = admin.getChildReqId();

    // match against D&B
    DnBMatchingResponse dnbMatch = matchAgainstDnB(handler, requestData, addr, engineData, details, overrides, ibmCmr != null);

    // match against SOS-RPA
    matchAgainstSosRpa(handler, requestData, addr, engineData, details, overrides, ibmCmr != null);

    // check CEID
    boolean t1 = isTier1BP(data);
    if (!t1) {
      details.append("BP is NOT a Tier 1.\n");
      engineData.addNegativeCheckStatus("_usBpT1", "BP is not a T1 or status cannot be determined via PPS profile.");
    } else {
      details.append("BP is a Tier 1.\n");
    }

    if (ibmCmr == null) {

      LOG.debug("No IBM Direct CMR for the end user.");
      childReqId = createChildRequest(entityManager, requestData, engineData);
      details.append("No IBM Direct CMR for the end user.\n");

      if (childCompleted) {
        String childError = "Child Request " + childReqId + " was completed but Direct CMR cannot be determined. Manual validation needed.";
        details.append(childError + "\n");
        engineData.addNegativeCheckStatus("_usChildError", childError);
        output.setDetails(details.toString());
        output.setResults("Issues Encountered");
        return false;
      } else {
        String childErrorMsg = "- IBM Direct CMR request creation cannot be done, errors were encountered -";
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
            details.append("Child Request " + childReqId + " created for the IBM Direct CMR record of " + addr.getDivn()
                + (!StringUtils.isBlank(addr.getDept()) ? " " + addr.getDept() : "")
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
  public void copyAndFillIBMData(EntityManager entityManager, GEOHandler handler, RequestData requestData, AutomationEngineData engineData,
      StringBuilder details, OverrideOutput overrides, RequestData childRequest, FindCMRRecordModel ibmCmr) {

    Data data = requestData.getData();
    if (ibmCmr != null) {
      if (!StringUtils.isBlank(ibmCmr.getCmrSapNumber())) {
        details.append(
            "\nCopying IBM Codes from IBM CMR " + ibmCmr.getCmrNum() + " - " + ibmCmr.getCmrName() + " (" + ibmCmr.getCmrSapNumber() + "): \n");
      } else {
        details.append("\nCopying IBM Codes from IBM CMR " + ibmCmr.getCmrNum() + " - " + ibmCmr.getCmrName() + ": \n");
      }

      String affiliate = ibmCmr.getCmrEnterpriseNumber();
      String isic = ibmCmr.getCmrIsic();
      boolean federalPoa = isic != null && (isic.startsWith("90") || isic.startsWith("91") || isic.startsWith("92"));
      if (federalPoa) {
        affiliate = ibmCmr.getCmrEnterpriseNumber();
      }
      if (!StringUtils.isBlank(ibmCmr.getCmrEnterpriseNumber())) {
        LOG.debug(" - copyAndFillIBMData: Affiliate: " + ibmCmr.getCmrEnterpriseNumber());
        details.append(" - Affiliate: " + data.getEnterprise() + (federalPoa ? " (Enterprise of IBM CMR)" : "") + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "AFFILIATE", data.getAffiliate(), ibmCmr.getCmrEnterpriseNumber());
      } else {
        updateAffiliateEnterprise4Child(entityManager, childRequest, ibmCmr);
        if (!StringUtils.isBlank(ibmCmr.getCmrEnterpriseNumber())) {
          LOG.debug(" - copyAndFillIBMData: CmrAffiliate: " + ibmCmr.getCmrEnterpriseNumber());
          details.append(" - Affiliate: " + ibmCmr.getCmrEnterpriseNumber() + (federalPoa ? " (Enterprise of IBM CMR)" : "") + "\n");
          overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "AFFILIATE", data.getAffiliate(), ibmCmr.getCmrEnterpriseNumber());
        }
      }

      if (!StringUtils.isBlank(ibmCmr.getCmrIsu())) {
        details.append(" - ISU: " + ibmCmr.getCmrIsu() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "ISU_CD", data.getIsuCd(), ibmCmr.getCmrIsu());
        details.append(" - Client Tier: " + ibmCmr.getCmrTier() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CLIENT_TIER", data.getClientTier(), ibmCmr.getCmrTier());
      }
      // CREATCMR-5894
      if (!StringUtils.isBlank(ibmCmr.getCmrClass())) {
        details.append(" - Customer Class: " + ibmCmr.getCustClass() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CUST_CLASS", data.getCustClass(), ibmCmr.getCmrClass());
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

    // do final checks on request data
    //overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_IND", data.getRestrictInd(), "Y");
    // CREATCMR-6342
    if (!USUtil.CG_BY_MODEL.equals(data.getCustGrp())) {
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MISC_BILL_CD", data.getMiscBillCd(), "I");
    }
    // CREATCMR-6342
    Addr installAt = requestData.getAddress("ZS01");
    if (installAt != null && SPECIAL_TAX_STATES.contains(installAt.getStateProv())) {
      details.append("Tax Code set to J000 based on state.\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "TAX_CD1", data.getTaxCd1(), "J000");
    } else {
      details.append("Tax Code set to J666 based on state.\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "TAX_CD1", data.getTaxCd1(), "J666");
    }
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MKTG_DEPT", data.getMktgDept(), "EI3");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SVC_AR_OFFICE", data.getSvcArOffice(), "IKE");

    boolean hasFieldError = false;
    if (!RESTRICT_TO_END_USER.equals(data.getRestrictTo()) && !RESTRICT_TO_MAINTENANCE.equals(data.getRestrictTo())) {
      String msg = "Restrict To value is incorrect for BP End User request.";
      engineData.addNegativeCheckStatus("_usBpData", msg);
      details.append(msg + "\n");
      hasFieldError = true;
    } else {
      if (RESTRICT_TO_END_USER.equals(data.getRestrictTo())) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), "DI3");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "PCC_AR_DEPT", data.getPccArDept(), "G8G");
      } else if (RESTRICT_TO_MAINTENANCE.equals(data.getRestrictTo())) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), "2NS");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "PCC_AR_DEPT", data.getPccArDept(), "G8M");
      }
    }

    USCeIdMapping mapping = null;
    String mappingRule = null;
    if (!StringUtils.isBlank(data.getEnterprise())) {
      mapping = USCeIdMapping.getByEnterprise(data.getEnterprise());
      mappingRule = "E";
    }
    if (mapping == null && !StringUtils.isBlank(data.getPpsceid())) {
      mapping = USCeIdMapping.getByCeid(data.getPpsceid());
      mappingRule = "C";
    }
    details.append("\n");
    if (mapping == null) {
      String msg = "Cannot determine distributor status based on request data.";
      engineData.addNegativeCheckStatus("_usBpData", msg);
      details.append(msg + "\n");
      hasFieldError = true;
    } else {
      if (StringUtils.isBlank(data.getCompany())) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "COMPANY", data.getCompany(), mapping.getCompanyNo());
      }
      boolean distributor = mapping.isDistributor();
      details.append("BP is a distributor based on (" + ("E".equals(mappingRule) ? "Enterprise No." : "CE ID") + ").\n");
      if (distributor) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CSO_SITE", data.getCsoSite(), "YBV");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_NAME", data.getBpName(), BP_MANAGING_IR);
      } else {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CSO_SITE", data.getCsoSite(), "DV4");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_NAME", data.getBpName(), BP_INDIRECT_REMARKETER);
      }
    }

    createAddressOverrides(entityManager, handler, requestData, engineData, details, overrides, childRequest, ibmCmr);

    if (!hasFieldError) {
      details.append("Branch Office codes computed successfully.");
      engineData.addPositiveCheckStatus(AutomationEngineData.BO_COMPUTATION);
    }

  }

  private void updateAffiliateEnterprise4Child(EntityManager entityManager, RequestData childRequest, FindCMRRecordModel ibmCmr) {
    String konzs = "";
    String zzkvNode2 = "";
    String sql = ExternalizedQuery.getSql("US.GET.KNA1.KONZS_ZZKVNODE2");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("ZZKV_CUSNO", ibmCmr.getCmrNum());
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      konzs = (String) results.get(0)[0];
      zzkvNode2 = (String) results.get(0)[1];
    }
    Data data = childRequest.getData();
    if (!StringUtils.isBlank(konzs)) {
      data.setAffiliate(konzs);
      entityManager.merge(data);
      ibmCmr.setCmrAffiliate(konzs);
      LOG.debug(" - updateAffiliate4Child: CmrAffiliate: " + konzs);
    }
    if (!StringUtils.isBlank(zzkvNode2)) {
      data.setEnterprise(zzkvNode2);
      entityManager.merge(data);
      ibmCmr.setCmrEnterpriseNumber(zzkvNode2);
      LOG.debug(" - updateEnterprise4Child: CmrEnterprise: " + zzkvNode2);
    }
  }

  @Override
  public void doFinalValidations(AutomationEngineData engineData, RequestData requestData, StringBuilder details, OverrideOutput overrides,
      FindCMRRecordModel ibmCmr, AutomationResult<OverrideOutput> result) {
    // CMR-3334 - do some last checks on Enterprise/Affiliate/Company
    Data data = requestData.getData();
    details.append("\n");
    // String affiliate = data.getAffiliate();
    // if (ibmCmr != null && !StringUtils.isBlank(ibmCmr.getCmrAffiliate())) {
    // affiliate = ibmCmr.getCmrAffiliate();
    // }
    // if (StringUtils.isBlank(affiliate)) {
    // details.append("\nAffiliate cannot be computed automatically.");
    // engineData.addNegativeCheckStatus("_usBpAff", "Affiliate cannot be
    // computed automatically");
    // }

    // USCeIdMapping mapping = USCeIdMapping.getByCeid(data.getPpsceid());
    USCeIdMapping mapping = USCeIdMapping.getByEnterprise(data.getEnterprise());
    // String enterpriseNo = data.getEnterprise();
    if (mapping != null) {
      // if (!mapping.getEnterpriseNo().equals(enterpriseNo)) {
      // details.append("\nEnterprise No. updated to mapped value for the CEID
      // (" + mapping.getEnterpriseNo() + ").");
      // overrides.addOverride(getProcessCode(), "DATA", "ENTERPRISE",
      // enterpriseNo, mapping.getEnterpriseNo());
      // enterpriseNo = mapping.getEnterpriseNo();
      // }
      if (!mapping.getCompanyNo().equals(data.getCompany())) {
        details.append("\nCompany No. updated to mapped value for the CEID (" + mapping.getCompanyNo() + ").");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "COMPANY", data.getCompany(), mapping.getCompanyNo());
      }
    } else {
      details.append("\nEnterprise No. cannot be validated automatically.");
      engineData.addNegativeCheckStatus("_usBpEnt", "Enterprise No. cannot be validated automatically.");
    }
    // if (StringUtils.isBlank(enterpriseNo)) {
    // details.append("\nEnterprise No. cannot be computed automatically.\n");
    // engineData.addNegativeCheckStatus("_usBpEnt", "Enterprise No. cannot be
    // computed automatically");
    // }

  }

  @Override
  protected void performAction(AutomationEngineData engineData, String msg) {
    engineData.addNegativeCheckStatus("_usBpNoMatch", msg);
  }

  @Override
  protected FindCMRRecordModel getIBMCMRBestMatch(AutomationEngineData engineData, RequestData requestData, List<DuplicateCMRCheckResponse> matches)
      throws CmrException {
    for (DuplicateCMRCheckResponse record : matches) {
      LOG.debug(" - Duplicate: (Restrict To: " + record.getUsRestrictTo() + ", Grade: " + record.getMatchGrade() + ")" + record.getCompany() + " - "
          + record.getCmrNo() + " - " + record.getAddrType() + " - " + record.getStreetLine1());
      if (StringUtils.isBlank(record.getUsRestrictTo())) {
        // check US CMR DB first to confirm no restriction
        if (hasBlankRestrictionCodeInUSCMR(record.getCmrNo())) {

          // IBM Direct CMRs have blank restrict to
          LOG.debug("CMR No. " + record.getCmrNo() + " has BLANK restriction code in US CMR. Getting CMR Details..");
          String overrides = "addressType=ZS01&cmrOwner=IBM&showCmrType=R&customerNumber=" + record.getCmrNo();
          FindCMRResultModel result = CompanyFinder.getCMRDetails(SystemLocation.UNITED_STATES, record.getCmrNo(), 5, null, overrides);
          if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
            return result.getItems().get(0);
          }
        } else {
          LOG.debug("CMR No. " + record.getCmrNo() + " has non-blank restriction code in US CMR");
        }
      }
    }
    return null;
  }

  @Override
  protected void setChildRequestScenario(Data data, Data childData, Admin childAdmin, StringBuilder details) {

    String isic = data.getUsSicmen();
    if (StringUtils.isBlank(isic)) {
      isic = data.getIsicCd();
    }
    if (StringUtils.isBlank(isic)) {
      isic = "";
    }

    String affiliate = data.getAffiliate();

    String typeDesc = null;
    String subTypeDesc = null;
    String type = null;
    String subType = null;
    String custType = null;

    int isicNumeric = 0;
    if (!StringUtils.isEmpty(isic)) {
      if (StringUtils.isNumeric(isic.substring(0, 2))) {
        isicNumeric = Integer.parseInt(isic.substring(0, 2));
      }
    }
    if (isicNumeric >= 94 && isicNumeric <= 97) {
      typeDesc = "State and Local";
      type = TYPE_STATE_AND_LOCAL;
      custType = USUtil.STATE_LOCAL;
      switch (isicNumeric) {
      case 94:
        subTypeDesc = "State and Local - State";
        subType = SUB_TYPE_STATE_AND_LOCAL_STATE;
        break;
      case 95:
        subTypeDesc = "State and Local - County";
        subType = SUB_TYPE_STATE_AND_LOCAL_COUNTY;
        break;
      case 96:
        subTypeDesc = "State and Local - City";
        subType = SUB_TYPE_STATE_AND_LOCAL_CITY;
        break;
      case 97:
        subTypeDesc = "State and Local - District";
        subType = SUB_TYPE_STATE_AND_LOCAL_DISTRICT;
        break;
      }
    } else if (isicNumeric >= 90 && isicNumeric <= 92) {
      typeDesc = "Federal";
      type = TYPE_FEDERAL;
      if (AFFILIATE_FEDERAL.equals(affiliate)) {
        subTypeDesc = "Federal Gov't Regular";
        subType = SUB_TYPE_FEDERAL_REGULAR_GOVT;
        custType = USUtil.FEDERAL;
      } else if (FEDERAL_CLASSIFIED_ISIC.contains(data.getIsicCd())) {
        subTypeDesc = "Power of Attorney (Camouflaged)";
        subType = SUB_TYPE_FEDERAL_POA;
        custType = USUtil.POWER_OF_ATTORNEY;
      } else {
        subTypeDesc = "Power of Attorney (Non-restricted)";
        subType = SUB_TYPE_FEDERAL_POAN;
        custType = USUtil.POWER_OF_ATTORNEY;
      }
    } else {
      typeDesc = "Commercial";
      type = TYPE_COMMERCIAL;
      subTypeDesc = "Regular Commercial CMR";
      subType = SUB_TYPE_COMMERCIAL_REGULAR;
      custType = USUtil.COMMERCIAL;
    }

    details.append(" - Type: " + typeDesc + "\n");
    details.append(" - Sub-type: " + subTypeDesc + "\n");
    childData.setCustGrp(type);
    childData.setCustSubGrp(subType);
    childAdmin.setCustType(custType);
  }

}
