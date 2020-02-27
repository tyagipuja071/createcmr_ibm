/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * {@link RowHandler} for {@link MassCreateData} records
 * 
 * @author Jeffrey Zamora
 * 
 */
public class DataHandler implements RowHandler {

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    return RowResult.passed();
  }

  @Override
  public boolean isCritical() {
    return false;
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    MassCreateData data = row.getData();
    if (!StringUtils.isEmpty(data.getRestrictTo())) {
      data.setRestrictInd(CmrConstants.YES_NO.Y.toString());
    } else {
      data.setRestrictInd(CmrConstants.YES_NO.N.toString());
    }
    if (!CmrConstants.YES_NO.Y.toString().equals(data.getFedSiteInd())) {
      data.setFedSiteInd(CmrConstants.YES_NO.N.toString());
    }
    if (!CmrConstants.YES_NO.Y.toString().equals(data.getOemInd())) {
      data.setOemInd(CmrConstants.YES_NO.N.toString());
    }
    if (!CmrConstants.YES_NO.Y.toString().equals(data.getOutCityLimit())) {
      data.setOutCityLimit(CmrConstants.YES_NO.N.toString());
    }
  }

}
