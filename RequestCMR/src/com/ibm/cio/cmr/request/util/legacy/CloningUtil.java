package com.ibm.cio.cmr.request.util.legacy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.ConfigUtil;

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

  public String getKuklaFromCMR(EntityManager entityManager, String cmrIssuingCntry, String cmrNo, String mandt) {
    String sql = ExternalizedQuery.getSql("GET.KNA1.KUKLA_VAL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", cmrIssuingCntry);
    query.setForReadOnly(true);
    String kukla = query.getSingleResult(String.class);
    return kukla;
  }

}
