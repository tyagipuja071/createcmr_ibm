/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.code.CopyConfigModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class CopyConfigService extends BaseSimpleService<ProcessResultModel> {

  private static final Logger LOG = Logger.getLogger(CopyConfigService.class);

  @Override
  protected ProcessResultModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    ProcessResultModel result = new ProcessResultModel();

    CopyConfigModel model = (CopyConfigModel) params.getParam("model");
    if (model == null) {
      result.setSuccess(false);
      result.setMessage("No parameters found on the request.");
      return result;
    }
    try {
      String src = model.getSourceCountry();

      List<String> countries = null;

      if (!StringUtils.isEmpty(model.getTargetGeo())) {
        countries = extractCountries(entityManager, model.getTargetGeo(), src);
      } else {
        countries = new ArrayList<String>();
        countries.addAll(Arrays.asList(model.getTargetCountry().split(",")));
      }

      if (countries == null || countries.isEmpty()) {
        result.setSuccess(false);
        result.setMessage("No country found to copy to.");
        return result;
      }

      String deleteSqlKey = null;
      String insertSqlKey = null;
      for (String config : model.getConfigType().split(",")) {
        if ("FI".equals(config)) {
          deleteSqlKey = "COPY.CLEAR.FIELD_INFO";
          insertSqlKey = "COPY.CREATE.FIELD_INFO";
          copyConfiguration(entityManager, src, countries, "Field Information", deleteSqlKey, insertSqlKey);
        } else if ("FL".equals(config)) {
          deleteSqlKey = "COPY.CLEAR.FIELD_LBL";
          insertSqlKey = "COPY.CREATE.FIELD_LBL";
          copyConfiguration(entityManager, src, countries, "Field Labels", deleteSqlKey, insertSqlKey);
        } else if ("LOV".equals(config)) {
          deleteSqlKey = "COPY.CLEAR.LOV";
          insertSqlKey = "COPY.CREATE.LOV";
          copyConfiguration(entityManager, src, countries, "LOVs", deleteSqlKey, insertSqlKey);
        } else if ("SC".equals(config)) {
          deleteSqlKey = "COPY.CLEAR.SCENARIOS.TYPE";
          insertSqlKey = "COPY.CREATE.SCENARIOS.TYPE";
          copyConfiguration(entityManager, src, countries, "Scenario Types", deleteSqlKey, insertSqlKey);

          deleteSqlKey = "COPY.CLEAR.SCENARIOS.SUBTYPE";
          insertSqlKey = "COPY.CREATE.SCENARIOS.SUBTYPE";
          copyConfiguration(entityManager, src, countries, "Scenario Sub Types", deleteSqlKey, insertSqlKey);

          deleteSqlKey = "COPY.CLEAR.SCENARIOS";
          insertSqlKey = "COPY.CREATE.SCENARIOS";
          copyConfiguration(entityManager, src, countries, "Scenarios", deleteSqlKey, insertSqlKey);

        }

        result.setSuccess(true);
      }
    } catch (Exception e) {
      LOG.error("Error in copying configuration", e);
      result.setSuccess(false);
      result.setMessage("An error occurred while copying configurations.");
    }
    return result;
  }

  private void copyConfiguration(EntityManager entityManager, String src, List<String> targetCountries, String type, String deleteSqlKey,
      String insertSqlKey) {
    for (String trgCntry : targetCountries) {
      if (src.equals(trgCntry)) {
        continue;
      }
      LOG.trace("Copying " + type + " from " + src + " to " + trgCntry);
      String sql = ExternalizedQuery.getSql(deleteSqlKey);
      sql = StringUtils.replace(sql, ":CNTRY", src);
      sql = StringUtils.replace(sql, ":TRG_CNTRY", trgCntry);
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      int count = query.executeSql();
      LOG.trace(count + " records removed from " + trgCntry + ".");

      sql = ExternalizedQuery.getSql(insertSqlKey);
      sql = StringUtils.replace(sql, ":CNTRY", src);
      sql = StringUtils.replace(sql, ":TRG_CNTRY", trgCntry);
      query = new PreparedQuery(entityManager, sql);
      count = query.executeSql();
      LOG.trace(count + " records copied to " + trgCntry + ".");
    }
    entityManager.flush();
  }

  private List<String> extractCountries(EntityManager entityManager, String geo, String sourceCntry) {
    String sql = ExternalizedQuery.getSql("COPY.GET_GEO_COUNTRIES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("GEO", geo);
    query.setParameter("CNTRY", sourceCntry);
    query.setForReadOnly(true);
    return query.getResults(String.class);
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }
}
