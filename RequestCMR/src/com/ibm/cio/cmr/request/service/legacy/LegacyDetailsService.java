/**
 * 
 */
package com.ibm.cio.cmr.request.service.legacy;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtAddrLink;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class LegacyDetailsService extends BaseSimpleService<LegacyDirectObjectContainer> {

  @Override
  protected LegacyDirectObjectContainer doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String country = (String) params.getParam("country");
    String cmrNo = (String) params.getParam("cmrNo");
    String realCty = (String) params.getParam("realCty");
    
    LegacyDirectObjectContainer container = new LegacyDirectObjectContainer();
    if (!StringUtils.isBlank(country) && !StringUtils.isBlank(cmrNo)) {

      String sql = ExternalizedQuery.getSql("LEGACYD.GETCUST");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", country);
      query.setParameter("CMR_NO", cmrNo);
      query.setForReadOnly(true);
      CmrtCust cust = query.getSingleResult(CmrtCust.class);
      container.setCustomer(cust);

      sql = ExternalizedQuery.getSql("LEGACYD.GETCEXT");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", country);
      query.setParameter("CMR_NO", cmrNo);
      query.setForReadOnly(true);
      CmrtCustExt ext = query.getSingleResult(CmrtCustExt.class);
      container.setCustomerExt(ext);

      sql = ExternalizedQuery.getSql("LEGACYD.GETADDR");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", country);
      query.setParameter("CMR_NO", cmrNo);
      query.setForReadOnly(true);
      List<CmrtAddr> addresses = query.getResults(CmrtAddr.class);
      container.getAddresses().addAll(addresses);

      sql = ExternalizedQuery.getSql("LEGACYD.GETALNK");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", country);
      query.setParameter("CMR_NO", cmrNo);
      query.setForReadOnly(true);
      List<CmrtAddrLink> links = query.getResults(CmrtAddrLink.class);
      container.getLinks().addAll(links);

      sql = ExternalizedQuery.getSql("LEGACY.SEARCH.KNA1");
      query = new PreparedQuery(entityManager, sql);
      if (SystemLocation.IRELAND.equals(realCty)){
        query.setParameter("KATR6", realCty);
      } else {
        query.setParameter("KATR6", country);
      }
      query.setParameter("CMR_NO", cmrNo);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setForReadOnly(true);
      List<Kna1> rdc = query.getResults(Kna1.class);
      container.getRdcRecords().addAll(rdc);

    }
    return container;
  }

}
