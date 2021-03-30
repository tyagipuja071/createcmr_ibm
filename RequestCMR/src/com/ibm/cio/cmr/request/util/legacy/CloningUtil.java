package com.ibm.cio.cmr.request.util.legacy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtAddrLink;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;

/**
 * @author PriyRanjan
 * 
 */

public class CloningUtil {

  private static final Logger LOG = Logger.getLogger(CloningUtil.class);

  public static List<CloningMapping> cloningCMRMappings = new ArrayList<CloningMapping>();

  @SuppressWarnings("unchecked")
  public CloningUtil() {
    if (CloningUtil.cloningCMRMappings.isEmpty()) {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("mappings", ArrayList.class);

      digester.addObjectCreate("mappings/mapping", CloningMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/cmrNoRange", "cmrNoRange");
      digester.addBeanPropertySetter("mappings/mapping/countries", "countries");
      digester.addSetNext("mappings/mapping", "add");
      try {
        InputStream is = ConfigUtil.getResourceStream("cloning-cmrno-mapping.xml");
        CloningUtil.cloningCMRMappings = (ArrayList<CloningMapping>) digester.parse(is);
      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }
  }

  public CloningMapping getCmrNoRangeFromMapping(String country) {
    if (!cloningCMRMappings.isEmpty()) {
      for (CloningMapping mapping : cloningCMRMappings) {
        List<String> countryValues = Arrays.asList(mapping.getCountries().replaceAll("\n", "").replaceAll(" ", "").split(","));
        if (countryValues.contains(country)) {
          return mapping;
        }
      }

    }
    return null;
  }

  public static String getKuklaFromCMR(EntityManager entityManager, String cmrIssuingCntry, String cmrNo, String mandt) {
    String sql = ExternalizedQuery.getSql("GET.KNA1.KUKLA_VAL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", cmrIssuingCntry);
    query.setForReadOnly(true);
    String kukla = query.getSingleResult(String.class);
    return kukla;
  }

  public static LegacyDirectObjectContainer getLegacyDBValues(EntityManager entityManager, String country, String cmrNo, boolean readOnly,
      boolean getLinks) throws Exception {
    LOG.debug("Getting Legacy DB values for " + country + " - " + cmrNo);
    String cmrIssuingCntry = country;
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }
    if (SystemLocation.ISRAEL.equals(cmrIssuingCntry)) {
      cmrIssuingCntry = SystemLocation.SAP_ISRAEL_SOF_ONLY;
    }

    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    // CMRTCUST
    String sql = ExternalizedQuery.getSql("LEGACYD.GETCUST_DUP_CREATE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    CmrtCust cust = query.getSingleResult(CmrtCust.class);
    if (cust == null) {
      throw new Exception("No CMR exists for target country");
    }
    legacyObjects.setCustomer(cust);
    legacyObjects.setCustomerNo(cmrNo);
    legacyObjects.setSofCntryCd(cmrIssuingCntry);

    // CMRTEXT
    sql = ExternalizedQuery.getSql("LEGACYD.GETCEXT");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    CmrtCustExt custExt = query.getSingleResult(CmrtCustExt.class);
    legacyObjects.setCustomerExt(custExt);

    // CMRTADDR
    sql = ExternalizedQuery.getSql("LEGACYD.GETADDR");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    List<CmrtAddr> addresses = query.getResults(CmrtAddr.class);
    if (addresses != null) {
      LOG.debug(">> Adding this number of addresses for CMR# " + cmrNo + " > " + addresses.size());

      legacyObjects.getAddresses().addAll(addresses);
    }

    if (getLinks) {
      // CMRTALNK
      sql = ExternalizedQuery.getSql("LEGACYD.GETALNK");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", cmrIssuingCntry);
      query.setParameter("CMR_NO", cmrNo);
      query.setForReadOnly(readOnly);
      List<CmrtAddrLink> addressLinks = query.getResults(CmrtAddrLink.class);
      if (addressLinks != null) {
        legacyObjects.getLinks().addAll(addressLinks);
      }
    }

    return legacyObjects;

  }

}
