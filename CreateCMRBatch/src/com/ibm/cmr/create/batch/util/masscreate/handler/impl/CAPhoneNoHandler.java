package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
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

public class CAPhoneNoHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(CAPhoneNoHandler.class);
  private Pattern caPhoneNoFormat = Pattern.compile("\\d{3}-\\d{3}-\\d{4}");

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    RowResult result = new RowResult();

    if (row.getAddresses() != null && row.getAddresses().size() > 0) {
      String custPhone = null;
      String landCntry = null;
      MassCreateData data = row.getData();

      for (MassCreateAddr addr : row.getAddresses()) {
        custPhone = addr.getCustPhone();
        landCntry = addr.getLandCntry();

        LOG.debug("Validating " + addr.getId().getAddrType() + " Phone# " + custPhone);
        if (data != null && StringUtils.isNotEmpty(data.getCustTyp()) && !"CROSS".equals(data.getCustTyp()) && StringUtils.isNotEmpty(landCntry)
            && "CA".equals(landCntry) && StringUtils.isNotEmpty(custPhone) && !caPhoneNoFormat.matcher(custPhone).matches()) {
          result.addError(addr.getId().getAddrType() + " Phone# Canada format should be NNN-NNN-NNNN. ");
          LOG.debug(" Invalid Canada Phone# format.");
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
