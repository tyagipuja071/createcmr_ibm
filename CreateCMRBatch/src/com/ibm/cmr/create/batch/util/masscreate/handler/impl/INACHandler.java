package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

public class INACHandler implements RowHandler {

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    MassCreateData data = row.getData();
    if (row.isCreateByModel() && StringUtils.isEmpty(data.getInacType()) && StringUtils.isEmpty(data.getInacCd())) {
      return RowResult.passed();
    }
    RowResult result = new RowResult();

    if (!StringUtils.isEmpty(data.getInacType()) && StringUtils.isEmpty(data.getInacCd())) {
      result.addError("INAC Code is required when INAC Type is specified.");
    }
    if (StringUtils.isEmpty(data.getInacType()) && !StringUtils.isEmpty(data.getInacCd())) {
      result.addError("INAC Type is required when INAC Code is specified.");
    }
    if (!StringUtils.isEmpty(data.getInacCd()) && !StringUtils.isNumeric(data.getInacCd()) && "I".equals(data.getInacType())) {
      result.addError("INAC Code should be numeric for INAC Type I");
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
