/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * @author Jeffrey Zamora
 * 
 */
public class EnterpriseAffiliateHandler implements RowHandler {

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    RowResult result = new RowResult();
    MassCreateData data = row.getData();
    if (!StringUtils.isEmpty(data.getEnterprise()) && data.getEnterprise().trim().length() < 7) {
      result.addError("Enterprise should be exactly 7 characters.");
    }
    if (!StringUtils.isEmpty(data.getAffiliate()) && data.getAffiliate().trim().length() < 7) {
      result.addError("Affiliate should be exactly 7 characters.");
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
