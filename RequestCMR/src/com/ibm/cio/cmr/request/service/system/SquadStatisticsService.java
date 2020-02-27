/**
 * 
 */
package com.ibm.cio.cmr.request.service.system;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.system.MetricsModel;
import com.ibm.cio.cmr.request.model.system.SquadStatisticsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.system.RequestStatsContainer;

/**
 * @author JeffZAMORA
 * 
 */
@Component
public class SquadStatisticsService extends BaseSimpleService<RequestStatsContainer> {
  private static final Logger LOG = Logger.getLogger(SquadStatisticsService.class);

  private static final ResourceBundle SQUAD_BUNDLE = ResourceBundle.getBundle("squad");
  public final SimpleDateFormat IN_FORMATTER = new SimpleDateFormat("yyyyMMdd");
  public final SimpleDateFormat OUT_FORMATTER = new SimpleDateFormat("dd-MM-yy");

  @Override
  protected RequestStatsContainer doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    RequestStatsContainer container = new RequestStatsContainer();
    MetricsModel model = (MetricsModel) params.getParam("model");

    LOG.info("Extracting data from " + model.getDateFrom() + " to " + model.getDateTo());
    String sql = ExternalizedQuery.getSql("METRICS.SQUAD");
    String geo = model.getGroupByGeo();
    if (!StringUtils.isBlank(geo)) {
      sql += " and CMR_ISSUING_CNTRY in (select CMR_ISSUING_CNTRY from CREQCMR.CNTRY_GEO_DEF where GEO_CD = :GEO_CD) ";
    }
    String procCenter = model.getGroupByProcCenter();
    if (!StringUtils.isBlank(procCenter)) {
      sql += " and CMR_ISSUING_CNTRY in (select CMR_ISSUING_CNTRY from CREQCMR.PROC_CENTER where upper(PROC_CENTER_NM) = :PROC_CENTER)";
    }

    sql += " group by CMR_ISSUING_CNTRY, CNTRY_NAME, PROCESS_DT";
    sql += " order by CNTRY_NAME, PROCESS_DT";

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("FROM", model.getDateFrom().replaceAll("-", ""));
    query.setParameter("TO", model.getDateTo().replaceAll("-", ""));
    query.setParameter("GEO_CD", geo);
    query.setParameter("PROC_CENTER", procCenter != null ? procCenter.toUpperCase().trim() : "");
    query.setForReadOnly(true);
    List<SquadStatisticsModel> stats = query.getResults(SquadStatisticsModel.class);

    String cntry = null;
    String dtIn = null;

    int month = 0;
    for (SquadStatisticsModel stat : stats) {
      entityManager.detach(stat);
      cntry = stat.getId().getCmrIssuingCntry();
      dtIn = stat.getId().getProcessDt();
      stat.setImt(SQUAD_BUNDLE.getString(cntry + ".IMT"));
      stat.setIot(SQUAD_BUNDLE.getString(cntry + ".IOT"));
      stat.setSquad(SQUAD_BUNDLE.getString(cntry + ".SQUAD"));
      stat.setTribe(SQUAD_BUNDLE.getString(cntry + ".TRIBE"));
      stat.setDisplay(OUT_FORMATTER.format(IN_FORMATTER.parse(dtIn)));

      month = Integer.parseInt(dtIn.substring(4, 6));
      if (month >= 1 && month <= 3) {
        stat.setQuarter("1Q " + dtIn.substring(0, 4));
      }
      if (month >= 4 && month <= 6) {
        stat.setQuarter("2Q " + dtIn.substring(0, 4));
      }
      if (month >= 7 && month <= 9) {
        stat.setQuarter("3Q " + dtIn.substring(0, 4));
      }
      if (month >= 10 && month <= 12) {
        stat.setQuarter("4Q " + dtIn.substring(0, 4));
      }
    }
    container.setSquadRecords(stats);
    return container;
  }

}
