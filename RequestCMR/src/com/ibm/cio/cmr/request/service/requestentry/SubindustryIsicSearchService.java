/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.SubindustryIsicSearchModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class SubindustryIsicSearchService extends BaseSimpleService<List<SubindustryIsicSearchModel>> {

  @Override
  protected List<SubindustryIsicSearchModel> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params)
      throws CmrException {
    SubindustryIsicSearchModel model = (SubindustryIsicSearchModel) params.getParam("model");
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.SUBINDISIC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.append(" where MANDT = :MANDT");
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));

    if (!StringUtils.isEmpty(model.getSubIndustryCdSearch())) {
      query.append(" and upper(BRAN1) like :SUBIND_CD");
      query.setParameter("SUBIND_CD", "%" + model.getSubIndustryCdSearch().toUpperCase() + "%");
    }
    if (!StringUtils.isEmpty(model.getSubIndustryDescSearch())) {
      query.append(" and upper(BRAN1_DESC) like :SUBIND_DESC");
      query.setParameter("SUBIND_DESC", "%" + model.getSubIndustryDescSearch().toUpperCase() + "%");
    }
    if (!StringUtils.isEmpty(model.getIsicCdSearch())) {
      query.append(" and upper(ZZKV_SIC) like :ISIC_CD");
      query.setParameter("ISIC_CD", "%" + model.getIsicCdSearch().toUpperCase() + "%");
    }
    if (!StringUtils.isEmpty(model.getIsicDescSearch())) {
      query.append(" and upper(ZZKV_SIC_DESC) like :ISIC_DESC");
      query.setParameter("ISIC_DESC", "%" + model.getIsicDescSearch().toUpperCase() + "%");
    }

    query.append(" order by BRAN1 asc");
    List<Object[]> qResults = query.getResults();

    List<SubindustryIsicSearchModel> results = new ArrayList<SubindustryIsicSearchModel>();
    SubindustryIsicSearchModel resultModel = null;
    for (Object[] result : qResults) {
      resultModel = new SubindustryIsicSearchModel();
      resultModel.setSubIndustryCdSearch((String) result[0]);
      resultModel.setSubIndustryDescSearch((String) result[1]);
      resultModel.setIsicCdSearch((String) result[2]);
      resultModel.setIsicDescSearch((String) result[3]);
      results.add(resultModel);
    }
    return results;
  }

}
