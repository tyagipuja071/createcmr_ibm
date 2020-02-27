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
public class CMRNoHandler implements RowHandler {

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    return RowResult.passed();
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    if (!row.isCreateByModel()) {
      return;
    }
    String modelCmrNo = row.getData().getModelCmrNo();
    if (modelCmrNo != null && modelCmrNo.length() < 7) {
      modelCmrNo = StringUtils.leftPad(modelCmrNo, 7, '0');
      row.getData().setModelCmrNo(modelCmrNo);
      row.mapRawValue("MODEL_CMR_NO", modelCmrNo);
      row.addUpdateCol("MODEL_CMR_NO");
    }
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
