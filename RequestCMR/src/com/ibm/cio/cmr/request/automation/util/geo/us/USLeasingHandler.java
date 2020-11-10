package com.ibm.cio.cmr.request.automation.util.geo.us;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;

/**
 * 
 * @author Shivangi
 *
 */
public class USLeasingHandler extends USBPEndUserHandler {
  private static final Logger LOG = Logger.getLogger(USLeasingHandler.class);

  @Override
  public boolean doInitialValidations(Admin admin, Data data, Addr addr, AutomationResult<OverrideOutput> output, AutomationEngineData engineData) {
    // check the scenario
    String custGrp = data.getCustGrp();
    String custSubGrp = data.getCustSubGrp();
    if ("BYMODEL".equals(custSubGrp)) {
      String type = admin.getCustType();
      if (!USUtil.LEASING.equals(type)) {
        output.setResults("Skipped");
        output.setDetails("Non Leasing create by model scenario not supported.");
        return true;
      }
    } else if (!TYPE_LEASING.equals(custGrp) || ((!SUB_TYPE_LEASE_SVR_CONT.equals(custSubGrp)) && (!SUB_TYPE_LEASE_3CC.equals(custSubGrp))
        && (!(SUB_TYPE_LEASE_NO_RESTRICT.equals(custSubGrp) && !StringUtils.isBlank(addr.getDivn()))))) {
      output.setResults("Skipped");
      output.setDetails("Non Leasing scenario not supported.");
      return true;
    }
    ScenarioExceptionsUtil scenarioExceptions = (ScenarioExceptionsUtil) engineData.get("SCENARIO_EXCEPTIONS");
    if (scenarioExceptions != null) {
      scenarioExceptions.setSkipCompanyVerification(true);
    }
    return false;
  }

  @Override
  protected void performAction(AutomationEngineData engineData, String msg) {
    engineData.addRejectionComment("_usLeasingNoMatch", msg, "", "");
  }

  @Override
  public void dofinalchecks(RequestData requestData, StringBuilder details, Data data, OverrideOutput overrides) {
    // do final checks on request data
    Admin admin = requestData.getAdmin();
    String custSubGrp = data.getCustSubGrp();
    Addr installAt = requestData.getAddress("ZS01");
    if (SUB_TYPE_LEASE_NO_RESTRICT.equals(custSubGrp) && !StringUtils.isBlank(installAt.getDivn())) {
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ADMIN", "MAIN_CUST_NM1", admin.getMainCustNm1(), "");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "ENTERPRISE", data.getEnterprise(), "");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_TO", data.getRestrictTo(), "");
    } else if (SUB_TYPE_LEASE_3CC.equals(custSubGrp) || SUB_TYPE_LEASE_SVR_CONT.equals(custSubGrp)) {
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ADMIN", "MAIN_CUST_NM1", admin.getMainCustNm1(), "IBM Credit LLC");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "ENTERPRISE", data.getEnterprise(), "4482735");
      overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "COMPANY", data.getCompany(), "12003567");
      if (SUB_TYPE_LEASE_3CC.equals(custSubGrp)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_TO", data.getRestrictTo(), "ICC");
      } else if (SUB_TYPE_LEASE_SVR_CONT.equals(custSubGrp)) {
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "DATA", "RESTRICT_TO", data.getRestrictTo(), "");
      }
    }
  }

  @Override
  public void InvoiceToOverrides(RequestData requestData, OverrideOutput overrides, EntityManager entityManager) {
    Data data = requestData.getData();
    String custSubGrp = data.getCustSubGrp();
    if (SUB_TYPE_LEASE_3CC.equals(custSubGrp) || SUB_TYPE_LEASE_SVR_CONT.equals(custSubGrp)) {
      Addr invoiceTo = requestData.getAddress("ZI01");
      if (invoiceTo != null) {
        // update invoice to address
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "ADDR_TXT", invoiceTo.getAddrTxt(), "7100 Highlands Parkway");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "CITY1", invoiceTo.getCity1(), "Smyrna");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "STATE_PROV", invoiceTo.getStateProv(), "GA");
        overrides.addOverride(AutomationElementRegistry.US_BP_PROCESS, "ZS01", "POST_CD", invoiceTo.getPostCd(), "30082-4859");
      } else {
        AddrPK addrPk = new AddrPK();
        LOG.debug("Adding Invoice To Address to Request ID " + requestData.getAdmin().getId().getReqId());
        addrPk.setReqId(requestData.getAdmin().getId().getReqId());
        addrPk.setAddrType("ZI01");
        addrPk.setAddrSeq("1");
        invoiceTo = new Addr();
        invoiceTo.setId(addrPk);
        invoiceTo.setAddrTxt("7100 Highlands Parkway");
        invoiceTo.setCity1("Smyrna");
        invoiceTo.setStateProv("GA");
        invoiceTo.setPostCd("30082-4859");
      }
    }
  }

  @Override
  public void doFinalValidations(AutomationEngineData engineData, RequestData requestData, StringBuilder details, OverrideOutput overrides,
      AutomationResult<OverrideOutput> result, FindCMRRecordModel ibmCmr) {
    // NOOP
  }

}
