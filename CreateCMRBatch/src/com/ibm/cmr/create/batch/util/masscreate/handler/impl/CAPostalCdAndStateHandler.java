package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * 
 * @author Joseph Ramos
 *
 */

public class CAPostalCdAndStateHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(CAPostalCdAndStateHandler.class);
  private static Map<String, ArrayList<String>> caStatePostalCd;

  static {
    caStatePostalCd = new HashMap<String, ArrayList<String>>();
    caStatePostalCd.put("NL", new ArrayList<String>(Arrays.asList("A")));
    caStatePostalCd.put("NS", new ArrayList<String>(Arrays.asList("B")));
    caStatePostalCd.put("PE", new ArrayList<String>(Arrays.asList("C")));
    caStatePostalCd.put("99", new ArrayList<String>(Arrays.asList("D")));
    caStatePostalCd.put("NB", new ArrayList<String>(Arrays.asList("E")));
    caStatePostalCd.put("QC", new ArrayList<String>(Arrays.asList("G", "H", "J")));
    caStatePostalCd.put("ON", new ArrayList<String>(Arrays.asList("K", "L", "M", "N", "P", "W")));
    caStatePostalCd.put("MB", new ArrayList<String>(Arrays.asList("R")));
    caStatePostalCd.put("SK", new ArrayList<String>(Arrays.asList("S")));
    caStatePostalCd.put("AB", new ArrayList<String>(Arrays.asList("T")));
    caStatePostalCd.put("BC", new ArrayList<String>(Arrays.asList("V")));
    caStatePostalCd.put("YT", new ArrayList<String>(Arrays.asList("Y")));
    caStatePostalCd.put("NU", new ArrayList<String>(Arrays.asList("X0A", "X0B", "X0C")));
    caStatePostalCd.put("NT", new ArrayList<String>(Arrays.asList("X0E", "X0G", "X1A")));
  }

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    RowResult result = new RowResult();

    if (row.getAddresses() != null) {
      String postalCd = null;
      String stateProv = null;
      String addrType = null;

      for (MassCreateAddr addr : row.getAddresses()) {
        postalCd = addr.getPostCd();
        stateProv = addr.getStateProv();
        addrType = addr.getId().getAddrType();

        if (StringUtils.isNotBlank(addr.getLandCntry()) && "CA".equals(addr.getLandCntry())) {
          // validate State/Prov
          LOG.debug("Validating State/Prov: " + stateProv);
          if (StringUtils.isEmpty(stateProv)) {
            result.addError(addrType + " State/Prov is empty.");
          } else if (StringUtils.isNotBlank(stateProv) && !stateExists(entityManager, stateProv, addr.getLandCntry())) {
            result.addError(addrType + " State/Prov is invalid.");
          }

          // validate Postal Cd
          LOG.debug("Validating Postal Code: " + postalCd);
          if (StringUtils.isNotBlank(stateProv) && StringUtils.isNotBlank(postalCd)) {
            if (!stateProv.equals("NU") && !stateProv.equals("NT") && caStatePostalCd.get(stateProv) != null
                && !caStatePostalCd.get(stateProv).contains(postalCd.substring(0, 1))) {
              result.addError(addrType + " Postal Code first character should be " + caStatePostalCd.get(stateProv).get(0));
            } else if ((stateProv.equals("NU") || stateProv.equals("NT")) && caStatePostalCd.get(stateProv) != null
                && !caStatePostalCd.get(stateProv).contains(postalCd.substring(0, 3))) {
              result.addError(addrType + " Postal Code first 3 characters should be " + String.join("or ", caStatePostalCd.get(stateProv)));
            }
          }
        }
      }
    }

    return result;
  }

  private boolean stateExists(EntityManager entityManager, String stateCode, String countryCode) {
    String sql = ExternalizedQuery.getSql("BATCH.VALIDATE_STATE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("STATE", stateCode);
    query.setParameter("CNTRY", countryCode);
    return query.exists();
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
