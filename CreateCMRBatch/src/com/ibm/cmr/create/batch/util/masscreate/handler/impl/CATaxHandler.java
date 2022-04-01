package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * 
 * @author Joseph Ramos
 *
 */

public class CATaxHandler implements RowHandler {

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    if (row.isCreateByModel()) {
      return RowResult.passed();
    }

    RowResult result = new RowResult();
    String pstExempt = row.getData().getTaxExemp();
    if (StringUtils.isNotBlank(pstExempt) && "Y".equals(pstExempt) && StringUtils.isBlank(row.getData().getTaxPayerCustCd())) {
      result.addError("PST Exemption License No is required when PST Exempt is Y. ");
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
