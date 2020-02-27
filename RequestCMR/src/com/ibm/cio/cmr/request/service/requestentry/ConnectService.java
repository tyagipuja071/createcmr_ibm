/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author JeffZAMORA
 * 
 */
@Component
public class ConnectService extends BaseSimpleService<String> {

  private static final Logger LOG = Logger.getLogger(ConnectService.class);

  @Override
  protected String doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {

    Long reqId = (Long) params.getParam("reqId");
    if (reqId == null || reqId == 0) {
      return "";
    }
    String sql = ExternalizedQuery.getSql("CONNECT.GET_MAIN_DETAILS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("REQ_ID", reqId);

    try {
      LOG.debug("Retrieving main details for Request ID " + reqId);
      List<CompoundEntity> results = query.getCompundResults(1, Addr.class, Addr.CONNECT_MAPPING);
      if (results != null && !results.isEmpty()) {
        CompoundEntity entity = results.get(0);
        Admin admin = entity.getEntity(Admin.class);
        Addr addr = entity.getEntity(Addr.class);

        String nm1 = admin.getMainCustNm1();
        String nm2 = admin.getMainCustNm2();

        if (StringUtils.isEmpty(nm1)) {
          nm1 = addr.getCustNm1();
          nm2 = addr.getCustNm2();
        }

        return nm1.toUpperCase() + (!StringUtils.isEmpty(nm2) ? " " + nm2.toUpperCase() : "") + "|" + formatAddress(addr);
      }
    } catch (Exception e) {
      LOG.warn("Details cannot be retrieved for Request ID " + reqId, e);
    }
    return "";
  }

  private String formatAddress(Addr addr) {
    StringBuilder addrStr = new StringBuilder();

    addrStr.append(!StringUtils.isEmpty(addr.getAddrTxt()) ? addr.getAddrTxt().toUpperCase() : "");
    addrStr.append(!StringUtils.isEmpty(addr.getAddrTxt2()) ? " " + addr.getAddrTxt2().toUpperCase() : "");
    addrStr.append(!StringUtils.isEmpty(addr.getCity1()) ? " " + addr.getCity1().toUpperCase() : "");
    addrStr.append(!StringUtils.isEmpty(addr.getStateProv()) ? " " + addr.getStateProv().toUpperCase() : "");
    addrStr.append(!StringUtils.isEmpty(addr.getLandCntry()) ? " " + addr.getLandCntry().toUpperCase() : "");
    addrStr.append(!StringUtils.isEmpty(addr.getPostCd()) ? " " + addr.getPostCd().toUpperCase() : "");
    return addrStr.toString();
  }

}
