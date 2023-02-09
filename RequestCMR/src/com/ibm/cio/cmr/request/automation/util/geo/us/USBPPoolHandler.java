package com.ibm.cio.cmr.request.automation.util.geo.us;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.DummyServletRequest;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;

/**
 * 
 * @author RoopakChugh
 *
 */

public class USBPPoolHandler extends USBPHandler {

  private static final List<String> POOL_CSO_SITES = Arrays.asList("YBV", "DV4");
  private static final List<String> T2_CSO_SITES = Arrays.asList("TT2");
  private static final String POOL = "POOL";
  private static final String T2 = "T2";
  private String cmrType = "";
  private String csoSite = "";

  private static final Logger LOG = Logger.getLogger(USBPPoolHandler.class);

  @Override
  public boolean doInitialValidations(Admin admin, Data data, Addr addr, AutomationResult<OverrideOutput> output, AutomationEngineData engineData) {

    if (StringUtils.isNotBlank(data.getSubIndustryCd()) && data.getSubIndustryCd().startsWith("Y")) {
      output.setResults("Skipped");
      output.setDetails("Federal Pool request detected. Request will be redirected to CMDE queue for review.");
      engineData.addNegativeCheckStatus("_federalPool", "Federal Pool request detected. Further review required.");
      return true;
    }

    if (StringUtils.isBlank(data.getPpsceid()) || !AutomationUtil.checkPPSCEID(data.getPpsceid())) {
      output.setResults("Invalid CEID");
      output.setDetails("Only BP or FSP with valid CEID is allowed to setup a Pool record, please check and confirm.");
      engineData.addRejectionComment("CEID", "Only BP or FSP with valid CEID is allowed to setup a Pool record, please check and confirm.", "", "");
      output.setOnError(true);
      return true;
    }
    return false;
  }

  @Override
  public boolean processRequest(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<OverrideOutput> output, StringBuilder details, boolean childCompleted, RequestData childRequest, GEOHandler handler,
      FindCMRRecordModel ibmCmr, OverrideOutput overrides) throws Exception {
    Data data = requestData.getData();

    this.csoSite = data.getCsoSite();
    USCeIdMapping mapping = null;
    if (StringUtils.isNotBlank(data.getEnterprise())) {
      mapping = USCeIdMapping.getByEnterprise(data.getEnterprise());
    }
    // else if (StringUtils.isNotBlank(data.getPpsceid())) {
    // mapping = USCeIdMapping.getByCeid(data.getPpsceid());
    // }

    if (mapping != null) {
      if (mapping.isDistributor()) {
        this.csoSite = "YBV";
      } else {
        this.csoSite = "DV4";
      }
    } else {
      this.csoSite = "TT2";
    }

    if (POOL_CSO_SITES.contains(this.csoSite)) {
      setCmrType(POOL);
    } else if (T2_CSO_SITES.contains(this.csoSite)) {
      setCmrType(T2);
    } else if (StringUtils.isBlank(this.csoSite)) {
      output.setResults("Blank CSO SITE");
      output.setDetails("Value for CSO site is not specified on the request. CMR Type Cannot be determined.");
      engineData.addNegativeCheckStatus("CSO", "Value for CSO site is not specified on the request. CMR Type Cannot be determined.");
      return false;
    } else {
      output.setResults("Invalid CSO SITE");
      output.setDetails("Value for CSO site is not allowed for BP Pool Scenario.");
      engineData.addNegativeCheckStatus("CSO", "\"Value for CSO site is not allowed for BP Pool Scenario.\"");
      return false;
    }

    return true;
  }

  @Override
  public void copyAndFillIBMData(EntityManager entityManager, GEOHandler handler, RequestData requestData, AutomationEngineData engineData,
      StringBuilder details, OverrideOutput overrides, RequestData childRequest, FindCMRRecordModel ibmCmr) {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();

    details.append("\nComputing field values for IBM BP " + getCmrType() + "CMR:\n");

    boolean hasFieldErrors = false;

    if (StringUtils.isNotBlank(this.csoSite)) {
      details.append(" - CSO Site: " + this.csoSite + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "CSO_SITE", data.getCsoSite(), this.csoSite);
    }

    if (StringUtils.isBlank(data.getAffiliate()) || !data.getAffiliate().equals(data.getEnterprise())) {
      details.append(" - Affiliate: " + data.getEnterprise() + "\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "AFFILIATE", data.getAffiliate(), data.getEnterprise());
    }

    details.append(" - Dept/Attn: --------POOL--------\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "DEPT", zs01.getDept(), "--------POOL--------");
    details.append(" - Restricted Ind: Y\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_IND", data.getRestrictInd(), "Y");
    details.append(" - Restricted to: BPQS\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_TO", data.getRestrictTo(), "BPQS");
    // CREATCMR-6342
    if (!USUtil.CG_BY_MODEL.equals(data.getCustGrp())) {
      details.append(" - Misc Bill Code: I\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MISC_BILL_CD", data.getMiscBillCd(), "I");
    }
    // CREATCMR-6342
    details.append(" - Tax Code: J000\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "TAX_CD1", data.getTaxCd1(), "J000");
    details.append(" - Marketing Dept: EI3\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MKTG_DEPT", data.getMktgDept(), "EI3");
    details.append(" - SVC A/R Office: IKE\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "SVC_AR_OFFICE", data.getSvcArOffice(), "IKE");
    details.append(" - PCC A/R Dept: G8M\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "PCC_AR_DEPT", data.getPccArDept(), "G8M");

    if (T2.equals(getCmrType())) {
      hasFieldErrors = doFieldComputationsForT2CMR(entityManager, handler, requestData, engineData, details, overrides);
    } else if (POOL.equals(getCmrType())) {
      hasFieldErrors = doFieldComputationsForPoolCMR(entityManager, handler, requestData, engineData, details, overrides);
    }

    // check addresses if not add
    Addr zi01 = requestData.getAddress("ZI01");
    LOG.debug("Updating invoice to address based on CSO Site: " + csoSite);

    String divn = "";
    String address = "";
    String address2 = "";
    String city = "";
    String state = "";
    String postCd = "";
    String landCntry = "";
    if (POOL.equals(cmrType)) {
      details.append("\nUpdating invoice-to address to: \n IBM Credit LLC, 1 N Castle Dr MD NC313, Armonk, NY 10504-1725\n");
      divn = "IBM Credit LLC";
      address = "1 N Castle Dr MD NC313";
      city = "Armonk";
      state = "NY";
      postCd = "10504-1725";
      landCntry = "US";
    } else if (T2.equals(cmrType)) {
      details.append("\nAligning invoice to and install at addresses.\n");
      divn = zs01.getDivn();
      address = zs01.getAddrTxt();
      address2 = zs01.getAddrTxt2();
      city = zs01.getCity1();
      state = zs01.getStateProv();
      postCd = zs01.getPostCd();
      landCntry = zs01.getLandCntry();
    }
    
    if (!"Y".equals(admin.getProspLegalInd())){
    if (zi01 == null) {

      LOG.debug("Adding the main address..");
      AddressService addrService = new AddressService();
      AddressModel addrModel = new AddressModel();
      addrModel.setReqId(data.getId().getReqId());
      addrModel.setDivn(divn);
      addrModel.setLandCntry(landCntry);
      addrModel.setAddrTxt(address);
      addrModel.setAddrTxt2(address2);
      addrModel.setCity1(city);
      addrModel.setStateProv(state);
      addrModel.setPostCd(postCd);
      addrModel.setState(BaseModel.STATE_NEW);
      addrModel.setAction("ADD_ADDRESS");

      addrModel.setAddrType(CmrConstants.ADDR_TYPE.ZI01.toString());
      addrModel.setCmrIssuingCntry(data.getCmrIssuingCntry());
      try {
        AppUser user = new AppUser();
        user.setIntranetId(requestData.getAdmin().getRequesterId());
        user.setBluePagesName(requestData.getAdmin().getRequesterNm());
        DummyServletRequest dummyReq = new DummyServletRequest();
        if (dummyReq.getSession() != null) {
          LOG.trace("Session found for dummy req");
          dummyReq.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, user);
        } else {
          LOG.warn("Session not found for dummy req");
        }
        addrService.performTransaction(addrModel, entityManager, dummyReq);
      } catch (Exception e) {
        LOG.error("An error occurred while adding ZI01 address", e);
      }
        entityManager.flush();
      } else {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "DIVN", zi01.getDivn(), divn);
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "ADDR_TXT", zi01.getAddrTxt(), address);
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "CITY1", zi01.getCity1(), city);
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "STATE_PROV", zi01.getStateProv(), state);
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZI01", "POST_CD", zi01.getPostCd(), postCd);
      }
    }
    

    if (!hasFieldErrors) {
      details.append("Field computations performed successfully.");
      engineData.addPositiveCheckStatus(AutomationEngineData.BO_COMPUTATION);
    }

  }

  private boolean doFieldComputationsForPoolCMR(EntityManager entityManager, GEOHandler handler, RequestData requestData,
      AutomationEngineData engineData, StringBuilder details, OverrideOutput overrides) {
    Data data = requestData.getData();

    details.append(" - Marketing A/R Dept: DI3\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), "DI3");

    details.append(" - BP Account Type: P\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_ACCT_TYP", data.getBpAcctTyp(), "P");

    if ("YBV".equals(this.csoSite)) {
      details.append(" - BP Name: Managing IR\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_NAME", data.getBpName(), BP_MANAGING_IR);
    } else {
      details.append(" - BP Name: Ind Rmkt Midrge\n");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_NAME", data.getBpName(), BP_INDIRECT_REMARKETER);
    }

    return false;
  }

  private boolean doFieldComputationsForT2CMR(EntityManager entityManager, GEOHandler handler, RequestData requestData,
      AutomationEngineData engineData, StringBuilder details, OverrideOutput overrides) {
    Data data = requestData.getData();

    details.append(" - Marketing A/R Dept: 7NZ\n");
    overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "MTKG_AR_DEPT", data.getMtkgArDept(), "7NZ");

    String processingType = RequestUtils.getProcessingType(entityManager, data.getCmrIssuingCntry());
    if ("TC".equals(processingType)) {
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_ACCT_TYP", data.getBpAcctTyp(), "P");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_NAME", data.getBpName(), BP_INDIRECT_REMARKETER);
    }

    if ("US".equals(processingType)) {
      if ("P".equals(data.getBpAcctTyp())) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_ACCT_TYP", data.getBpAcctTyp(), "");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "BP_NAME", data.getBpName(), "");
      }
    }

    return false;
  }

  @Override
  public void doFinalValidations(AutomationEngineData engineData, RequestData requestData, StringBuilder details, OverrideOutput overrides,
      FindCMRRecordModel ibmCmr, AutomationResult<OverrideOutput> result) {
    // NOOP
  }

  @Override
  protected FindCMRRecordModel getIBMCMRBestMatch(AutomationEngineData engineData, RequestData requestData, List<DuplicateCMRCheckResponse> matches)
      throws CmrException {
    // NOOP
    return null;
  }

  @Override
  protected void setChildRequestScenario(Data data, Data childData, Admin childAdmin, StringBuilder details) {
    // NOOP

  }

  @Override
  public boolean isEndUserSupported() {
    return false;
  }

  public String getCmrType() {
    return cmrType;
  }

  public void setCmrType(String cmrType) {
    this.cmrType = cmrType;
  }

}
