/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

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
 * Handler to transform the Postal Code for US (897)
 * 
 * @author Jeffrey Zamora
 * 
 */
public class USPostCodeAndStateHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(USPostCodeAndStateHandler.class);

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    RowResult result = new RowResult();

    String postCode = null;
    String originalValue = null;
    if (row.getAddresses() != null) {
      for (MassCreateAddr addr : row.getAddresses()) {
        postCode = addr.getPostCd();
        originalValue = postCode;
        if (originalValue == null) {
          originalValue = "";
        }
        if (!"US".equals(addr.getLandCntry())) {
          postCode = "00000";
          addr.setStateProv("''");
          row.mapRawValue(addr.getId().getAddrType() + "-STATE_PROV", "''");
          row.addUpdateCol(addr.getId().getAddrType() + "-STATE_PROV");
        }

        if (!StringUtils.isBlank(postCode)) {
          if (!StringUtils.isBlank(postCode)) {
            if (postCode.length() >= 10 && !postCode.contains("-")) {
              result.addError(addr.getId().getAddrType() + " Zip Code has too many digits.");
            } else {
              LOG.debug("Validating Postal Code: " + postCode);
              if ("US".equals(addr.getLandCntry())) {
                postCode = formatPostalCode(postCode);
              }
              LOG.debug("Assigned: " + postCode);
              if (!originalValue.equals(postCode)) {
                addr.setPostCd(postCode);
                row.mapRawValue(addr.getId().getAddrType() + "-POST_CD", postCode);
                row.addUpdateCol(addr.getId().getAddrType() + "-POST_CD");
              }
            }
          }
        }
        if ("US".equals(addr.getLandCntry()) && !StringUtils.isBlank(addr.getStateProv()) && !StringUtils.isBlank(addr.getLandCntry())) {
          LOG.debug("Validating State Code " + addr.getStateProv() + " for " + addr.getLandCntry());
          if (!stateExists(entityManager, addr.getStateProv(), addr.getLandCntry())) {
            LOG.debug("State Code " + addr.getStateProv() + " invalid.");
            result.addError(addr.getId().getAddrType() + " State Code is invalid.");
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

  public static String formatPostalCode(String postCd) {
    String stdPostCode = StringUtils.replace(postCd, "-", "");
    stdPostCode = StringUtils.replace(stdPostCode, " ", "");
    if (stdPostCode.length() < 9) {
      stdPostCode = StringUtils.rightPad(stdPostCode, 9, '0');
    }
    stdPostCode = stdPostCode.substring(0, 5) + "-" + stdPostCode.substring(5);
    return stdPostCode;
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
