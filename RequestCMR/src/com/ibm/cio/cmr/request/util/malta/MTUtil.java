package com.ibm.cio.cmr.request.util.malta;

import java.util.List;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

public class MTUtil {
  public static boolean isCountryMTEnabled(EntityManager entityManager, String cntry) {

    boolean isMD = false;

    String sql = ExternalizedQuery.getSql("DR.GET_SUPP_CNTRY_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    query.setForReadOnly(true);
    List<Integer> records = query.getResults(Integer.class);
    Integer singleObject = null;

    if (records != null && records.size() > 0) {
      singleObject = records.get(0);
      Integer val = singleObject != null ? singleObject : null;

      if (val != null) {
        isMD = true;
      } else {
        isMD = false;
      }
    } else {
      isMD = false;
    }
    return isMD;
  }

}
