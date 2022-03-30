package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * 
 * @author Joseph Ramos
 *
 */

public class CALocationNoHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(CALocationNoHandler.class);
  private static List<String> caLocationCd = Arrays.asList("AG000", "AI000", "AW000", "BS000", "BB000", "BM000", "BQ000", "BV000", "CW000", "DM000",
      "DO000", "GD000", "GP000", "GY000", "HT000", "KN000", "KY000", "JM000", "LC000", "MQ000", "MS000", "PR000", "SR000", "SX000", "TC000", "TT000",
      "VC000", "VG000");
  private static List<String> caribbeanCountryCd = Arrays.asList("AG", "AI", "AW", "BS", "BB", "BM", "BQ", "BV", "CW", "DM", "DO", "GD", "GP", "GY",
      "HT", "KN", "KY", "JM", "LC", "MQ", "MS", "PR", "SR", "SX", "TC", "TT", "VC", "VG");

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    MassCreateData data = row.getData();
    List<MassCreateAddr> addrs = row.getAddresses();
    RowResult result = new RowResult();

    if (!row.isCreateByModel() && data != null && StringUtils.isNotBlank(data.getLocationNumber()) && addrs != null && addrs.size() > 0) {
      String custSubGrp = data.getCustSubGrp();
      String custTyp = data.getCustTyp();
      String locationNo = data.getLocationNumber();

      for (MassCreateAddr massCreateAddr : addrs) {
        if ("ZS01".equals(massCreateAddr.getId().getAddrType())) {
          String landedCntry = massCreateAddr.getLandCntry();

          LOG.debug("Validating Location/Province Code: " + locationNo);
          if (!caLocationCd.contains(locationNo) && caribbeanCountryCd.contains(landedCntry) && StringUtils.isNotEmpty(custTyp)
              && "CROSS".equals(custTyp)) {
            result.addError("Invalid Province Code for Caribbean Countries. ");
            LOG.debug("Invalid Location/Province Code for Caribbean Country " + landedCntry);
          } else if (StringUtils.isNotEmpty(custTyp) && "LOCAL".equals(custTyp) && caLocationCd.contains(locationNo) && "CA".equals(landedCntry)) {
            result.addError("Invalid Province Code. ");
            LOG.debug(" Invalid Location/Province Code for Canada LOCAL Customer Type.");
          } else if (StringUtils.isNotEmpty(custSubGrp) && "USA".equals(custSubGrp) && !"99999".equals(locationNo)) {
            result.addError("Invalid Province Code. ");
            LOG.debug(" Invalid Location/Province Code for USA Customer SubType.");
          }
        }
      } // for
    }

    return result;
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
