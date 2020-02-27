/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * {@link RowHandler} for {@link MassCreateAddr} records
 * 
 * @author Jeffrey Zamora
 * 
 */
public class AddressHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(AddressHandler.class);

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {

    RowResult result = new RowResult();

    if (row.getAddresses() != null) {
      for (MassCreateAddr addr : row.getAddresses()) {
        if (addr.isVirtual()) {
          LOG.debug(addr.getId().getAddrType() + " is virtual. Skipping.");
        } else {
          LOG.debug("Validating address lines for " + addr.getId().getAddrType());
          if (StringUtils.isBlank(addr.getLandCntry())) {
            result.addError(addr.getId().getAddrType() + " Landed Country is required.");
          }
          if (StringUtils.isBlank(addr.getAddrTxt())) {
            result.addError(addr.getId().getAddrType() + " Address is required.");
          }
          if (StringUtils.isBlank(addr.getStateProv())) {
            result.addError(addr.getId().getAddrType() + " State/Province is required.");
          }
          if (StringUtils.isBlank(addr.getCity1())) {
            result.addError(addr.getId().getAddrType() + " City is required.");
          }
        }
      }
    }

    return result;
  }

  @Override
  public boolean isCritical() {
    return false;
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
  }

}
