package com.ibm.cio.cmr.request.util.fr;

import java.util.List;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

/**
 * Utilities handling the Austria processing
 * 
 * @author Paul
 * 
 */

public class FRUtil {

  public static boolean isCountryATEnabled(EntityManager entityManager, String cntry) {

    boolean isFR = false;

    String sql = ExternalizedQuery.getSql("FR.GET_SUPP_CNTRY_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    query.setForReadOnly(true);
    List<Integer> records = query.getResults(Integer.class);
    Integer singleObject = null;

    if (records != null && records.size() > 0) {
      singleObject = records.get(0);
      Integer val = singleObject != null ? singleObject : null;

      if (val != null) {
        isFR = true;
      } else {
        isFR = false;
      }
    } else {
      isFR = false;
    }
    return isFR;
  }

}
