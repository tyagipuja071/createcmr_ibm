/**
 * 
 */
package com.ibm.cio.cmr.request.util.external;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;

/**
 * @author JeffZAMORA
 * 
 */
public class CreateCMRBPHandler implements ExternalSystemHandler {

  @Override
  public void addEmailParams(EntityManager entityManager, List<Object> params, Admin admin) {
    String type = (String) params.get(4);
    String reqId = (String) params.get(0);
    if ("Create".equalsIgnoreCase(type)) {
      type = "C";
    } else if ("Update".equalsIgnoreCase(type)) {
      type = "U";
    }
    String reqStatus = (String) params.get(5);
    if (RequestUtils.STATUS_INPUT_REQUIRED.equalsIgnoreCase(reqStatus)) {
      reqStatus = "PRJ";
    }

    String cmrIssuingCountry = null;
    cmrIssuingCountry = getCmrIssuingCntry(entityManager, admin);

    String sql = ExternalizedQuery.getSql("BATCH.EMAILENGINE.GETADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Addr> addresses = query.getResults(Addr.class);
    String bpURL = SystemConfiguration.getValue("CREATECMR_BP_URL") + "/request?cmrIssuingCntry=" + cmrIssuingCountry + "&reqStatus=" + reqStatus
        + "&reqType=" + type + "&reqId=" + reqId;
    params.add(bpURL); // {12}

    if (cmrIssuingCountry != null) {
      if ("897".equalsIgnoreCase(cmrIssuingCountry)) {
        // String sql = ExternalizedQuery.getSql("BATCH.EMAILENGINE.GETADDR");
        // PreparedQuery query = new PreparedQuery(entityManager, sql);
        // query.setParameter("REQ_ID", admin.getId().getReqId());
        // List<Addr> addresses = query.getResults(Addr.class);
        if (addresses != null) {
          for (Addr addr : addresses) {
            if ("ZS01".equals(addr.getId().getAddrType())) {
              String endUser = "";
              if (!StringUtils.isBlank(addr.getDivn())) {
                endUser += addr.getDivn();
              }
              if (!StringUtils.isBlank(addr.getDept())) {
                endUser += (!StringUtils.isBlank(endUser) ? " " : "") + addr.getDept();
              }
              params.add(endUser); // {13}
              break;
            }
          }
        }
      } else {
        String endUser = "-";
        if (!StringUtils.isBlank(admin.getMainCustNm1())) {
          endUser = admin.getMainCustNm1();
        }
        if (!StringUtils.isBlank(admin.getMainCustNm2())) {
          endUser += " " + admin.getMainCustNm2();
        }
        endUser = endUser.trim();
        params.add(endUser); // {13}
      }
    } else {
      String endUser = "-";
      params.add(endUser); // {13}
    }

    String addressLine1 = "";
    String addressLine2 = "";
    if (addresses != null) {
      for (Addr addr : addresses) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          if (!StringUtils.isBlank(addr.getAddrTxt())) {
            addressLine1 += addr.getAddrTxt();
          }
          if (!StringUtils.isBlank(addr.getAddrTxt2())) {
            addressLine1 += " " + addr.getAddrTxt2();
          }
          if (!StringUtils.isBlank(addr.getCity1())) {
            addressLine2 += "  " + addr.getCity1();
          }
          if (!StringUtils.isBlank(addr.getStateProv())) {
            addressLine2 += ", " + addr.getStateProv();
          }
          if (!StringUtils.isBlank(addr.getPostCd())) {
            addressLine2 += " " + addr.getPostCd();
          }
          break;
        }
      }
    } else {
      addressLine1 = "-";
      addressLine2 = "-";
    }
    addressLine1 = addressLine1.trim();
    params.add(addressLine1); // {14}
    params.add(addressLine2); // {15}

  }

  private String getCmrIssuingCntry(EntityManager entityManager, Admin admin) {
    String cmrIssuingCntry = null;
    String sql = ExternalizedQuery.getSql("BATCH.EMAILENGINE.GETCMRISSUINGCNTRY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      cmrIssuingCntry = results.get(0);
    }
    return cmrIssuingCntry;
  }
}
