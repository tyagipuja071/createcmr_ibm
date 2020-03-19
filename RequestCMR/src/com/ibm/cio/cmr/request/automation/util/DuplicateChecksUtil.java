/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckRequest;
import com.ibm.cmr.services.client.matching.request.ReqCheckRequest;

/**
 * Utility class where requests for duplucate matching are directly manipulated
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
   */
  public static void setCountrySpecificsForRequestChecks(EntityManager entityManager, Admin admin, Data data, Addr currAddr,
      ReqCheckRequest request) {

    if (StringUtils.isBlank(request.getCity()) && SystemLocation.SINGAPORE.equals(data.getCmrIssuingCntry())) {
      // here for now, find a way to move to common class
      request.setCity("SINGAPORE");
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
  public static void setCountrySpecificsForCMRChecks(EntityManager entityManager, Admin admin, Data data, Addr addr,
      DuplicateCMRCheckRequest request) {

    // country specifics, move to another class some time
    if (StringUtils.isBlank(request.getCity()) && SystemLocation.SINGAPORE.equals(data.getCmrIssuingCntry())) {
      // here for now, find a way to move to common class
      request.setCity("SINGAPORE");
    }

    // for US, use only first 5 in the postal code checks
    if (SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry()) && addr.getPostCd() != null && addr.getPostCd().length() > 5) {
      // request.setPostalCode(addr.getPostCd().substring(0, 5));
    }

  }
}
