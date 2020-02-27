/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * @author Jeffrey Zamora
 * 
 */
public class InternalTypeAbbrevNameHandler implements RowHandler {

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    RowResult result = new RowResult();
    if (row.getData() != null && "INTERNAL".equals(row.getData().getCustSubGrp())) {
      if (StringUtils.isBlank(row.getData().getAbbrevNm())) {
        result.addError("Abbreviated Name is required for Internal Customers.");
      }
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
