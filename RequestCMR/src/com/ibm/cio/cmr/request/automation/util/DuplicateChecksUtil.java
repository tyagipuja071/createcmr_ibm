/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.util.geo.SpainUtil;
import com.ibm.cio.cmr.request.automation.util.geo.USUtil;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.request.ReqCheckRequest;

/**
 * Utility class where requests for duplicate matching are directly manipulated
 * depending on country-specific logic
 * 
 * @author JeffZAMORA
 *
 */
public class DuplicateChecksUtil {

  /**
   * Sets country-specific values for duplicate request checks
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @param currAddr
   * @param engineData
   */
  public static void setCountrySpecificsForRequestChecks(EntityManager entityManager, Admin admin, Data data, Addr currAddr, ReqCheckRequest request,
      AutomationEngineData engineData) {
    String cmrIssuingCntry = StringUtils.isNotBlank(data.getCmrIssuingCntry()) ? data.getCmrIssuingCntry() : "";

    switch (cmrIssuingCntry) {
    case SystemLocation.SINGAPORE:
      if (StringUtils.isBlank(request.getCity())) {
        // here for now, find a way to move to common class
        request.setCity("SINGAPORE");
      }
      break;
    case SystemLocation.BRAZIL:
      if (StringUtils.isNotBlank(data.getCustSubGrp())) {
        request.setScenario(data.getCustSubGrp());
      }
      break;
    case SystemLocation.UNITED_STATES:
      String scenarioToMatch = (String) engineData.get(AutomationEngineData.REQ_MATCH_SCENARIO);

      if (StringUtils.isNotBlank(scenarioToMatch)) {
        request.setScenario(scenarioToMatch);
      }

      if (engineData.hasPositiveCheckStatus("US_ZI01_REQ_MATCH")) {
        request.setAddrType("ZI01");
      }

      break;
    }
  }

  /**
   * Sets country-specific values for duplicate CMR checks
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @param currAddr
   */
  public static void setCountrySpecificsForCMRChecks(EntityManager entityManager, Admin admin, Data data, Addr addr, DuplicateCMRCheckRequest request,
      AutomationEngineData engineData) {
    String cmrIssuingCntry = StringUtils.isNotBlank(data.getCmrIssuingCntry()) ? data.getCmrIssuingCntry() : "";
    switch (cmrIssuingCntry) {
    case SystemLocation.SINGAPORE:
      if (StringUtils.isBlank(request.getCity())) {
        // here for now, find a way to move to common class
        request.setCity("SINGAPORE");
      }
      break;
    case SystemLocation.UNITED_STATES:
      // if (addr.getPostCd() != null && addr.getPostCd().length() > 5) {
      // // request.setPostalCode(addr.getPostCd().substring(0, 5));
      // }
      if (engineData.hasPositiveCheckStatus("BP_EU_REQ")) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustomerName(StringUtils.isBlank(addr.getDivn()) ? "" : addr.getDivn());
        } else if ("ZI01".equals(addr.getId().getAddrType()) && admin.getMainCustNm1() != null) {
          request.setCustomerName(admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2()));
        }
      }
      request.setUsRestrictTo(data.getRestrictTo());
      break;
    case SystemLocation.SPAIN:
    	if(SpainUtil.SCENARIO_INTERNAL.equals(data.getCustSubGrp())){
    		if ("ZI01".equals(addr.getId().getAddrType())) {
    	          request.setCustClass("81");
    	        } 	
    	}
    	if(SpainUtil.SCENARIO_INTERNAL_SO.equals(data.getCustSubGrp())){
    		if ("ZI01".equals(addr.getId().getAddrType())) {
    	          request.setCustClass("85");
    	        } 	
    	}
    }

  }
}
