/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * ISIC and Subindustry handler
 * 
 * @author Joseph Ramos
 * 
 */
public class WWSubindustryISICHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(WWSubindustryISICHandler.class);

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    MassCreateData data = row.getData();
    if (row.isCreateByModel() && StringUtils.isEmpty(data.getIsicCd())) {
      // no ISIC change
      return RowResult.passed();
    }

    LOG.debug("Request ID: " + row.getData().getId().getParReqId() + " ISIC " + data.getIsicCd());
    RowResult result = new RowResult();
    String trimmedIsic = StringUtils.isNotBlank(data.getIsicCd()) ? data.getIsicCd().substring(0, 4) : "";
    String[] combination = getSubIndustryISIC(entityManager, trimmedIsic);
    if (combination == null) {
      LOG.debug("ISIC " + data.getIsicCd() + " Not found.");
      result.addError("ISIC is invalid/not found. ");
    }
    return result;
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    MassCreateData data = row.getData();
    String trimmedIsic = StringUtils.isNotBlank(data.getIsicCd()) ? data.getIsicCd().substring(0, 4) : "";
    String[] combination = getSubIndustryISIC(entityManager, trimmedIsic);
    if (combination != null) {
      data.setSubIndustryCd(combination[1]);
    }
  }

  private String[] getSubIndustryISIC(EntityManager entityManager, String isicCd) {
    String sql = ExternalizedQuery.getSql("MC.WW.GET_ISIC_SUBIND");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISIC", isicCd);
    query.setParameter("GEO_CD", "WW");
    List<Object[]> result = query.getResults(1);
    if (result != null && result.size() > 0) {
      return new String[] { (String) result.get(0)[0], (String) result.get(0)[1] };
    }
    return null;
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
