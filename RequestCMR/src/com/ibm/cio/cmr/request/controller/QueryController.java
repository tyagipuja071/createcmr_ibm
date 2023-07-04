/**
 * 
 */
package com.ibm.cio.cmr.request.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * Handles General Queries, for AJAX
 * 
 * @author Jeffrey Zamora
 * 
 */
@Controller
public class QueryController {

  private static final Logger LOG = Logger.getLogger(QueryController.class);

  @RequestMapping(
      value = "/query/{q}")
  public ModelMap query(@PathVariable("q") String q, HttpServletRequest request, HttpServletResponse response) throws Exception {

    ModelMap map = new ModelMap();
    boolean qall = "Y".equals(request.getParameter("_qall"));
    Map<String, String> queryResults = new HashMap<String, String>();
    List<Map<String, String>> all = new ArrayList<Map<String, String>>();
    String sql = ExternalizedQuery.getSql("QUERY." + q.toUpperCase());
    if (!StringUtils.isEmpty(sql)) {
      EntityManager manager = JpaManager.getEntityManager();
      try {
        PreparedQuery query = new PreparedQuery(manager, sql);
        for (String param : request.getParameterMap().keySet()) {
          query.setParameter(param.toUpperCase(), request.getParameter(param));
        }
        if (!request.getParameterMap().containsKey("MANDT")){
          query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        }
        query.setForReadOnly(true);
        List<Object[]> results = query.getResults(qall ? -1 : 1);
        int i = 1;
        if (results != null && results.size() > 0) {
          if (qall) {
            Map<String, String> qmap = null;
            for (Object[] obj1 : results) {
              qmap = new HashMap<>();
              i = 1;
              for (Object obj : obj1) {
                if (obj instanceof Date || obj instanceof Timestamp) {
                  qmap.put("ret" + i, obj != null ? CmrConstants.DATE_TIME_FORMAT().format(obj) : "");
                } else {
                  qmap.put("ret" + i, obj != null ? obj.toString() : "");
                }
                i++;
              }
              all.add(qmap);
            }
          } else {
            Object[] obj1 = results.get(0);
            for (Object obj : obj1) {
              if (obj instanceof Date || obj instanceof Timestamp) {
                queryResults.put("ret" + i, obj != null ? CmrConstants.DATE_TIME_FORMAT().format(obj) : "");
              } else {
                queryResults.put("ret" + i, obj != null ? obj.toString() : "");
              }
              i++;
            }
          }
        }
      } finally {
        manager.clear();
        manager.close();
      }
    }
    map.put("result", qall ? all : queryResults);
    return map;
  }

  @RequestMapping(
      value = "/extquery/{q}")
  public ModelMap externalQuery(@PathVariable("q") String q, HttpServletRequest request, HttpServletResponse response) throws Exception {

    ModelMap map = new ModelMap();
    try {
      if (StringUtils.isBlank(request.getParameter("returnFields"))) {
        map.put("result", Collections.emptyList());
        return map;
      }
      String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
      String mandt = SystemConfiguration.getValue("MANDT");
      String dbId = QueryClient.RDC_APP_ID;

      List<Map<String, Object>> all = new ArrayList<Map<String, Object>>();
      String sql = ExternalizedQuery.getSql("EXTQUERY." + q.toUpperCase());
      if (!StringUtils.isEmpty(sql)) {
        for (String param : request.getParameterMap().keySet()) {
          sql = StringUtils.replace(sql, ":" + param.toUpperCase(), "'" + request.getParameter(param) + "'");
        }
      }
      sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");

      String[] fields = request.getParameter("returnFields").split(",");
      QueryRequest query = new QueryRequest();
      query.setSql(sql);
      for (String field : fields) {
        query.addField(field.toUpperCase());
      }
      System.err.println("external sql: " + sql);

      QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
      QueryResponse qResponse = client.executeAndWrap(dbId, query, QueryResponse.class);

      if (qResponse.isSuccess() && qResponse.getRecords() != null && qResponse.getRecords().size() != 0) {
        List<Map<String, Object>> records = qResponse.getRecords();

        all.addAll(records);

      }
      map.put("result", all);

      return map;
    } catch (Exception e) {
      LOG.error("Error in executing external query", e);
      map.put("result", Collections.emptyList());
      return map;
    }
  }

  @RequestMapping(
      value = "/record/{queryId}")
  public void getRecord(@PathVariable("queryId") String queryId, HttpServletRequest request, HttpServletResponse response) throws Exception {

    String model = request.getParameter("model");
    boolean qall = "Y".equals(request.getParameter("_qall"));
    Class<?> modelClass = null;
    try {
      modelClass = Class.forName(model);
    } catch (NoClassDefFoundError ncdfe) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    String sql = ExternalizedQuery.getSql("RECORD." + queryId.toUpperCase());
    if (!StringUtils.isEmpty(sql)) {
      EntityManager manager = JpaManager.getEntityManager();
      try {
        PreparedQuery query = new PreparedQuery(manager, sql);
        for (String param : request.getParameterMap().keySet()) {
          if (!"model".equalsIgnoreCase(param)) {
            query.setParameter(param.toUpperCase(), request.getParameter(param));
          }
        }
        List<?> results = query.getResults(qall ? -1 : 1, modelClass);
        if (results != null && results.size() > 0) {
          ObjectMapper mapper = new ObjectMapper();
          if (!qall) {
            Object o = results.get(0);
            sb.append(mapper.writeValueAsString(o));
          } else {
            sb.append(mapper.writeValueAsString(results));
          }
        }
      } finally {
        manager.clear();
        manager.close();
      }
    }
    response.setContentType("application/json; charset=UTF-8");
    response.addHeader("Content-Type", "application/json; charset=UTF-8");
    response.getOutputStream().write(sb.toString().getBytes("UTF-8"));
  }

}
