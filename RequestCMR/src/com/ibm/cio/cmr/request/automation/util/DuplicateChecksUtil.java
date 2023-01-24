/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.util.geo.BeLuxUtil;
import com.ibm.cio.cmr.request.automation.util.geo.NetherlandsUtil;
import com.ibm.cio.cmr.request.automation.util.geo.NordicsUtil;
import com.ibm.cio.cmr.request.automation.util.geo.SpainUtil;
import com.ibm.cio.cmr.request.automation.util.geo.UKIUtil;
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

  public static String[] FIINTER = { "FIINT", "CBINT", "LTINT", "LVINT", "EEINT" };
  public static String[] FIPRIPE = { "FIPRI", "LTPRI", "LVPRI", "EEPRI" };
  public static String[] FIIBMEM = { "FIIBM", "LTIBM", "LVIBM", "EEIBM" };

  public static String[] DKINTER = { "DKINT", "CBINT", "FOINT", "GLINT", "ISINT" };
  public static String[] DKPRIPE = { "DKPRI", "FOPRI", "ISPRI", "GLPRI" };
  public static String[] DKIBMEM = { "DKIBM", "FOIBM", "GLIBM", "ISIBM" };

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
      if (USUtil.SC_BP_POOL.equals(data.getCustSubGrp())
          || (StringUtils.isNotBlank(addr.getDept()) && addr.getDept().toUpperCase().contains("POOL"))) {
        if ("TT2".equals(data.getCsoSite())) {
          request.setUsCsoSite("TT2");
        } else if ("P".equals(data.getBpAcctTyp())) {
          request.setUsBpAccType("P");
        }
      }

      if (USUtil.SC_BP_DEVELOP.equals(data.getCustSubGrp()) || (StringUtils.isNotBlank(addr.getDept())
          && (addr.getDept().toUpperCase().contains("DEMO DEV") || addr.getDept().toUpperCase().contains("DEVELOPMENT")))) {
        request.setUsBpAccType("D");
      }

      if (USUtil.SC_BP_E_HOST.equals(data.getCustSubGrp()) || (StringUtils.isNotBlank(addr.getDept())
          && (addr.getDept().toUpperCase().contains("E-HOST") || addr.getDept().toUpperCase().contains("EHOST")))) {
        request.setUsBpAccType("E");
      }

      request.setUsRestrictTo(data.getRestrictTo());
      break;
    case SystemLocation.SPAIN:
      if (SpainUtil.SCENARIO_INTERNAL.equals(data.getCustSubGrp())) {
        if ("ZI01".equals(addr.getId().getAddrType())) {
          request.setCustClass("81");
        }
      }
      if (SpainUtil.SCENARIO_INTERNAL_SO.equals(data.getCustSubGrp())) {
        if ("ZI01".equals(addr.getId().getAddrType())) {
          request.setCustClass("85");
        }
      }
      break;
    case SystemLocation.UNITED_KINGDOM:
    case SystemLocation.IRELAND:
      List<String> scenariosList = new ArrayList<String>();
      scenariosList.add(UKIUtil.SCENARIO_INTERNAL);
      scenariosList.add(UKIUtil.SCENARIO_INTERNAL_FSL);
      scenariosList.add(UKIUtil.SCENARIO_IGF);
      scenariosList.add(UKIUtil.SCENARIO_PRIVATE_PERSON);
      if (scenariosList.contains(data.getCustSubGrp())) {
        request.setCustClass(data.getCustClass());
      }
      
      if(UKIUtil.SCENARIO_PRIVATE_PERSON.equalsIgnoreCase(data.getCustSubGrp())){
    	 request.setStreetLine1(addr.getAddrTxt()); 
      }
      break;
    case SystemLocation.NETHERLANDS:
      if (NetherlandsUtil.SCENARIO_INTERNAL.equals(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("81");
        }
      }
      if (NetherlandsUtil.SCENARIO_PRIVATE_CUSTOMER.equals(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("60");
        }
      }
      if (NetherlandsUtil.SCENARIO_BP_LOCAL.equals(data.getCustSubGrp()) || NetherlandsUtil.SCENARIO_BP_CROSS.equals(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("49");
        }
      }
    case SystemLocation.BELGIUM:
      if (BeLuxUtil.SCENARIO_INTERNAL.equals(data.getCustSubGrp()) || BeLuxUtil.SCENARIO_INTERNAL_LU.equals(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("81");
        }
      }
      if (BeLuxUtil.SCENARIO_INTERNAL_SO.equals(data.getCustSubGrp()) || BeLuxUtil.SCENARIO_INTERNAL_SO_LU.equals(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("85");
        }
      }
      if (BeLuxUtil.SCENARIO_BP_LOCAL.equals(data.getCustSubGrp()) || BeLuxUtil.SCENARIO_BP_LOCAL_LU.equals(data.getCustSubGrp())
          || BeLuxUtil.SCENARIO_BP_CROSS.equals(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("49");
        }
      }
    case SystemLocation.SWEDEN:
    case SystemLocation.NORWAY:
      if (NordicsUtil.INTER_LOCAL.equals(data.getCustSubGrp()) || NordicsUtil.CROSS_INTER.equals(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("81");
        }
      }
      if (NordicsUtil.PRIPE_LOCAL.equals(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("60");
        }
      }
      if (NordicsUtil.IBMEM_LOCAL.equals(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("71");
        }
      }
    case SystemLocation.FINLAND:
      if (Arrays.asList(FIINTER).contains(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("81");
        }
      }
      if (Arrays.asList(FIPRIPE).contains(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("60");
        }
      }
      if (Arrays.asList(FIIBMEM).contains(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("71");
        }
      }
    case SystemLocation.DENMARK:
      if (Arrays.asList(DKINTER).contains(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("81");
        }
      }
      if (Arrays.asList(DKPRIPE).contains(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("60");
        }
      }
      if (Arrays.asList(DKIBMEM).contains(data.getCustSubGrp())) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          request.setCustClass("71");
        }
      }
    }

  }
}