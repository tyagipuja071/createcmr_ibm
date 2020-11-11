package com.ibm.cio.cmr.request.automation.util.geo.us;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.us.USDuplicateCheckElement;
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
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

public class USBPDevelopHandler extends USBPHandler {

  private static final Logger LOG = Logger.getLogger(USBPDevelopHandler.class);
  private static boolean checkForPoolCMR = false;
  private static boolean checkForT2PoolCMR = false;
  private static List<String> demoDev = Arrays.asList("DEMO DEVELOPMENT", "DEMO DEV", "DEVELOPMENT");
  private static List<String> leaseDev = Arrays.asList("ICC LEASE DEVELOPMENT/DEV", "IDL LEASE DEVELOPMENT/DEV");

  @Override
  public boolean doInitialValidations(Admin admin, Data data, Addr addr, AutomationResult<OverrideOutput> output, AutomationEngineData engineData) {
    USCeIdMapping mapping = null;
    boolean addRejection = true;
    if (!StringUtils.isBlank(data.getEnterprise())) {
      mapping = USCeIdMapping.getByEnterprise(data.getEnterprise());
    }
    if (mapping != null) {
      String reqCompNo = data.getCompany() != null ? data.getCompany() : "";
      String mappingCompNo = mapping.getCompanyNo();
      String reqCompnayNme = data.getCompanyNm() != null ? data.getCompanyNm() : "";
      String mappingCompName = mapping.getName();
      if (reqCompNo.equalsIgnoreCase(mappingCompNo) && reqCompnayNme.equalsIgnoreCase(mappingCompName)) {
        addRejection = false;
      }
    }

    if (addRejection) {
      String msg = "BP Development records only allow to create under Distributors, please check and confirm the Distributor for this transaction.";
      engineData.addRejectionComment("DISTRIBUTOR", msg, "", "");
      return false;
    }
    return true;
  }

  @Override
  public boolean processRequest(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<OverrideOutput> output, StringBuilder details, boolean childCompleted, FindCMRRecordModel ibmCmr, RequestData childRequest,
      GEOHandler handler, OverrideOutput overrides) throws Exception {

    Admin admin = requestData.getAdmin();
    Addr addr = requestData.getAddress("ZS01");
    Data data = requestData.getData();
    long childReqId = admin.getChildReqId();

    USDuplicateCheckElement dupCheckElement = new USDuplicateCheckElement(null, null, false, false);
    MatchingResponse<DuplicateCMRCheckResponse> response = dupCheckElement.getCMRMatches(entityManager, requestData, engineData);
    List<String> existingCmrList = new ArrayList<String>();
    if (response.getSuccess() && !response.getMatches().isEmpty()) {
      List<DuplicateCMRCheckResponse> cmrCheckMatches = response.getMatches();
      for (DuplicateCMRCheckResponse cmrCheckRecord : cmrCheckMatches) {
        existingCmrList.add(cmrCheckRecord.getCmrNo());
      }
      String msg = "Request rejected because of existing BP Development CMR(s) - " + existingCmrList;
      engineData.addRejectionComment("Dup_BP_Dev", msg, "", "");
      details.append(msg);
      output.setDetails(details.toString());
      output.setOnError(true);
      output.setResults("BP Development CMR Exists.");
      return false;
    } else {
      // check if end user is same as BP
      String divn = StringUtils.isNotBlank(addr.getDivn()) ? addr.getDivn() : "";
      String dept = StringUtils.isNotBlank(addr.getDept()) ? addr.getDept() : "";
      String custNm1 = StringUtils.isNotBlank(admin.getMainCustNm1()) ? admin.getMainCustNm1() : "";
      String custNm2 = StringUtils.isNotBlank(admin.getMainCustNm2()) ? admin.getMainCustNm2() : "";
      String endUser = divn.concat(dept);
      String legalName = custNm1.concat(custNm2);

      if (StringUtils.isNotBlank(legalName) && legalName.equalsIgnoreCase(endUser)) {
        // scenario 1
        // check for existing Pool CMR
        checkForPoolCMR = true;
        ibmCmr = findIBMCMR(entityManager, handler, requestData, addr, engineData, null);
        if (ibmCmr == null) {
          // if no pool CMR exists, validate the legal name and address by
          // matching
          // against D&B
          DnBMatchingResponse dnbMatch = matchAgainstDnB(handler, requestData, addr, engineData, details, overrides, ibmCmr == null);
          if (dnbMatch != null) {
            // create a new pool cmr through child request
            childReqId = createChildRequest(entityManager, requestData, engineData);
            {
              String childErrorMsg = "- Pool request creation cannot be done, errors were encountered -";
              if (childReqId <= 0) {
                details.append(childErrorMsg + "\n");
                engineData.addRejectionComment("OTH", childErrorMsg, "", "");
                output.setDetails(details.toString());
                output.setOnError(true);
                output.setResults("Issues Encountered");
                return false;
              } else {
                requestData.getData().setRestrictTo("BPQS");
                requestData.getData().setBpAcctTyp("P");
                String childDetails = completeChildRequestDataAndAddress(entityManager, requestData, engineData, childReqId, dnbMatch);
                if (childDetails == null) {
                  details.append(childErrorMsg + "\n");
                  engineData.addRejectionComment("OTH", childErrorMsg, "", "");
                  output.setOnError(true);
                  output.setResults("Issues Encountered");
                  output.setDetails(details.toString());
                  return false;
                } else {
                  details.append("Child Request " + childReqId + " created for the Pool CMR record of " + addr.getDivn()
                      + (!StringUtils.isBlank(addr.getDept()) ? " " + addr.getDept() : "")
                      + ".\nThe system will wait for completion of the child record before processing the request.\n");
                  details.append(childDetails + "\n");
                  setWaiting(true);
                  output.setDetails(details.toString());
                  output.setResults("Waiting on Child Request...");
                  output.setOnError(false);
                  return false;
                }

              }
            }
          } else {
            // if legal name and address cannot be validated
            output.setResults("Review Required.");
            output.setDetails("Legal name and address could not be validated in DnB.");
            engineData.addNegativeCheckStatus("notValidated_DnB", "Legal name and address could not be validated in DnB.");
          }
        }
        return true;
      } else {
        // scenario 2
        if (StringUtils.isNotBlank(data.getPpsceid()) && AutomationUtil.checkPPSCEID(data.getPpsceid())) {
          output.setResults("Invalid CEID");
          output.setDetails("Only BP with valid CEID is allowed to setup a Pool record, please check and confirm.");
          engineData.addRejectionComment("CEID", "Only BP with valid CEID is allowed to setup a Pool record, please check and confirm.", "", "");
          output.setOnError(true);
          return false;
        } else {
          // check for T2 BP Pool CMR
          checkForT2PoolCMR = true;
          ibmCmr = findIBMCMR(entityManager, handler, requestData, addr, engineData, null);
          if (ibmCmr != null && StringUtils.isNotBlank(ibmCmr.getCmrNum())) {
            // existing CMR so , create BP Pool CMR
            return true;
          } else {
            // validate legal name in DnB
            DnBMatchingResponse dnbMatch = matchAgainstDnB(handler, requestData, addr, engineData, details, overrides, ibmCmr == null);
            if (dnbMatch != null) {
              // create a new T2 pool cmr through child request
              childReqId = createChildRequest(entityManager, requestData, engineData);
              {
                String childErrorMsg = "- Pool request creation cannot be done, errors were encountered -";
                if (childReqId <= 0) {
                  details.append(childErrorMsg + "\n");
                  engineData.addRejectionComment("OTH", childErrorMsg, "", "");
                  output.setDetails(details.toString());
                  output.setOnError(true);
                  output.setResults("Issues Encountered");
                  return false;
                } else {
                  requestData.getData().setRestrictTo("BPQS");
                  requestData.getData().setBpAcctTyp("TT2");
                  String childDetails = completeChildRequestDataAndAddress(entityManager, requestData, engineData, childReqId, dnbMatch);
                  if (childDetails == null) {
                    details.append(childErrorMsg + "\n");
                    engineData.addRejectionComment("OTH", childErrorMsg, "", "");
                    output.setOnError(true);
                    output.setResults("Issues Encountered");
                    output.setDetails(details.toString());
                    return false;
                  } else {
                    details.append("Child Request " + childReqId + " created for the Pool CMR record of " + addr.getDivn()
                        + (!StringUtils.isBlank(addr.getDept()) ? " " + addr.getDept() : "")
                        + ".\nThe system will wait for completion of the child record before processing the request.\n");
                    details.append(childDetails + "\n");
                    setWaiting(true);
                    output.setDetails(details.toString());
                    output.setResults("Waiting on Child Request...");
                    output.setOnError(false);
                    return false;
                  }

                }
              }
            } else {
              // if legal name and address cannot be validated
              output.setResults("Review Required.");
              output.setDetails("Legal name and address could not be validated in DnB.");
              engineData.addNegativeCheckStatus("notValidated_DnB", "Legal name and address could not be validated in DnB.");
            }

          }
        }
      }
      return true;
    }
  }

  @Override
  public void copyAndFillIBMData(EntityManager entityManager, GEOHandler handler, FindCMRRecordModel ibmCmr, RequestData requestData,
      AutomationEngineData engineData, StringBuilder details, OverrideOutput overrides, RequestData childRequest) {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");

    if (childRequest != null) {
      Data childData = childRequest.getData();
      if ("BPQS".equalsIgnoreCase(childData.getRestrictTo()) && "P".equalsIgnoreCase(childData.getBpAcctTyp())) {

        details.append(" - Affiliate: " + data.getEnterprise() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "AFFILIATE", data.getAffiliate(), ibmCmr.getCmrEnterpriseNumber());

        details.append(" - Tax Class / Code 1: " + data.getTaxCd1() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", " TAX CLASS / CODE 1", data.getTaxCd1(), "J000");

        details.append(" - Tax Exempt Status: " + data.getSpecialTaxCd() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "TAX EXEMPT", data.getSpecialTaxCd(), "Z");

        details.append(" - Subindustry: " + data.getSubIndustryCd() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SUBINDUSTRY", data.getSubIndustryCd(), ibmCmr.getCmrSubIndustry());

        details.append(" - ISIC: " + data.getIsicCd() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "ISIC", data.getIsicCd(), ibmCmr.getCmrIsic());

        details.append(" - INAC: " + data.getIsicCd() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "INAC", data.getInacCd(), ibmCmr.getCmrInac());

      } else if ("BPQS".equalsIgnoreCase(childData.getRestrictTo()) && "TT2".equalsIgnoreCase(childData.getCsoSite())) {
        details.append(" - DIVISION: " + zs01.getDivn() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ADDR", "DIVISION", zs01.getDivn(), ibmCmr.getCmrName());

        details.append(" - ISIC: " + data.getIsicCd() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "ISIC", data.getIsicCd(), ibmCmr.getCmrIsic());

        details.append(" - AFFILIATE: " + zs01.getDivn() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "AFFILIATE", data.getAffiliate(), ibmCmr.getCmrAffiliate());

        details.append(" - INAC: " + data.getIsicCd() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "INAC", data.getInacCd(), ibmCmr.getCmrInac());

        details.append(" - Tax Class / Code 1: " + data.getTaxCd1() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", " TAX CLASS / CODE 1", data.getTaxCd1(), "J666");

        details.append(" - Tax Exempt Status: " + data.getSpecialTaxCd() + "\n");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "TAX EXEMPT", data.getSpecialTaxCd(), "");

      }

      details.append(" - Restrict To: " + data.getRestrictTo() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT TO", data.getRestrictTo(), ibmCmr.getUsCmrRestrictTo());

      details.append(" - BP Account Type: " + data.getBpAcctTyp() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP ACCOUNT TYPE", data.getBpAcctTyp(), "D");

      details.append(" - Marketing Dept: " + data.getMktgDept() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MARKETING DEPT", data.getMktgDept(), "EI3");

      details.append(" - Miscellaneous Bill Code: " + data.getMiscBillCd() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MISCELLANEOUS BILL CD", data.getMiscBillCd(), "I");

      details.append(" - Business Partner Name: " + data.getBpName() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BUSINESS PARTNER NAME", data.getBpName(), "Managing IR");

      details.append(" - PCC A/R Dept: " + data.getPccArDept() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "PCC A/R Dept", data.getPccArDept(), "G8M");

      details.append(" - SVC A/R Office : " + data.getSvcArOffice() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SVC A/R Office ", data.getSvcArOffice(), "IKE");

    }

    createAddressOverrides(entityManager, handler, ibmCmr, requestData, engineData, details, overrides, childRequest);

    String divn_attn = StringUtils.isNotBlank(zs01.getDivn()) ? zs01.getDivn().concat(zs01.getDept()) : "";

    if (demoDev.contains(divn_attn)) {
      details.append(" - CSO Site : " + data.getCsoSite() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CSO SITE", data.getCsoSite(), "YBV");

      details.append(" - Marketing A/R Dept  : " + data.getMtkgArDept() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MARKETING A/R DEPT", data.getMtkgArDept(), "DI3");

    } else if (leaseDev.contains(divn_attn)) {
      details.append(" - CSO Site : " + data.getCsoSite() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CSO SITE", data.getCsoSite(), "TF7");

      details.append(" - Marketing A/R Dept  : " + data.getMtkgArDept() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MARKETING A/R DEPT", data.getMtkgArDept(), "DI2");
    }
  }

  @Override
  public void doFinalValidations(AutomationEngineData engineData, RequestData requestData, StringBuilder details, OverrideOutput overrides,
      AutomationResult<OverrideOutput> result, FindCMRRecordModel ibmCmr) {
    // NOOP
  }

  @Override
  protected FindCMRRecordModel getIBMCMRBestMatch(AutomationEngineData engineData, RequestData requestData, List<DuplicateCMRCheckResponse> matches)
      throws CmrException {
    if (checkForPoolCMR) {
      for (DuplicateCMRCheckResponse record : matches) {
        LOG.debug(" - Duplicate Record with: (Restrict To: " + record.getUsRestrictTo() + ", Grade: " + record.getMatchGrade() + ")"
            + record.getCompany() + " - " + record.getCmrNo() + " - " + record.getAddrType() + " - " + record.getStreetLine1());

        // checking for Pool CMR
        if ("BPQS".equalsIgnoreCase(record.getUsRestrictTo()) && "P".equalsIgnoreCase(record.getUsBpAccType())) {
          LOG.debug("CMR No. " + record.getCmrNo() + " has BPQS restriction code in US CMR and BP Acc Type is P. Getting CMR Details..");
          String overrides = "addressType=ZS01&cmrOwner=IBM&showCmrType=R&customerNumber=" + record.getCmrNo();
          FindCMRResultModel result = CompanyFinder.getCMRDetails(SystemLocation.UNITED_STATES, record.getCmrNo(), 5, null, overrides);
          if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
            return result.getItems().get(0);
          }
        }
      }
    } else if (checkForT2PoolCMR) {
      // checking for TT2 Pool CMR
      for (DuplicateCMRCheckResponse record : matches) {
        LOG.debug(" - Duplicate Record with: (Restrict To: " + record.getUsRestrictTo() + ", Grade: " + record.getMatchGrade() + ")"
            + record.getCompany() + " - " + record.getCmrNo() + " - " + record.getAddrType() + " - " + record.getStreetLine1());
        if ("BPQS".equalsIgnoreCase(record.getUsRestrictTo()) && "TT2".equalsIgnoreCase(record.getUsCsoSite())) {
          LOG.debug("CMR No. " + record.getCmrNo() + " has BPQS restriction code in US CMR and CSO Site  is TT2. Getting CMR Details..");
          String overrides = "addressType=ZS01&cmrOwner=IBM&showCmrType=R&customerNumber=" + record.getCmrNo();
          FindCMRResultModel result = CompanyFinder.getCMRDetails(SystemLocation.UNITED_STATES, record.getCmrNo(), 5, null, overrides);
          if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
            return result.getItems().get(0);
          }
        }
      }
    }
    return null;
  }

  @Override
  protected void setChildRequestScenario(Data data, Data childData, Admin childAdmin, StringBuilder details) {
    if (childData != null) {
      childData.setCustGrp(USUtil.CG_THIRD_P_BUSINESS_PARTNER);
      childData.setCustSubGrp(USUtil.SC_BP_POOL);
      childAdmin.setCustType(USUtil.BUSINESS_PARTNER);

    }
  }

  @Override
  public boolean isChildRequestSupported() {
    return true;
  }

}
