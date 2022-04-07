package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * 
 * @author Joseph Ramos
 *
 */

public class CADefaultFields implements RowHandler {

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    return RowResult.passed();
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    // Set Issuing Country
    row.getData().setCmrIssuingCntry(SystemLocation.CANADA);

    // Set Abbreviated Name
    String custName = row.getData().getCustNm1();
    String custSubGrp = row.getData().getCustSubGrp();
    String abbrevName = row.getData().getAbbrevNm();

    if (StringUtils.isNotBlank(custName) && StringUtils.isNotBlank(custSubGrp) && StringUtils.isBlank(abbrevName)) {
      if ("OEM".equals(custSubGrp)) {
        abbrevName = (custName.length() > 16 ? custName.substring(0, 16).toUpperCase() + "/SWG" : custName + "SWG");
      } else {
        abbrevName = (custName.length() > 20 ? custName.substring(0, 20).toUpperCase() : custName.toUpperCase());
      }
      row.getData().setAbbrevNm(abbrevName);
    }

    // Set Abbreviated Location
    String addrType = null;
    String zs01City = null;
    for (MassCreateAddr addr : row.getAddresses()) {
      addrType = addr.getId().getAddrType();
      zs01City = addr.getCity1();

      if (StringUtils.isNotBlank(addrType) && "ZS01".equals(addrType) && StringUtils.isNotBlank(zs01City)) {
        row.getData().setCompany(zs01City.length() > 12 ? zs01City.substring(0, 12) : zs01City);
      }
    }
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
