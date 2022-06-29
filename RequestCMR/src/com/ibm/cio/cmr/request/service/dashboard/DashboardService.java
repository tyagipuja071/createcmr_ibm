/**
 * 
 */
package com.ibm.cio.cmr.request.service.dashboard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.entity.dashboard.AutomationMonitor;
import com.ibm.cio.cmr.request.entity.dashboard.ProcessingMonitor;
import com.ibm.cio.cmr.request.model.DropdownItemModel;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.dashboard.DashboardResult;
import com.ibm.cio.cmr.request.model.dashboard.ProcessingModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.legacy.LegacyDowntimes;

/**
 * @author 136786PH1
 *
 */
@Component
public class DashboardService extends BaseSimpleService<DashboardResult> {

  private static final Logger LOG = Logger.getLogger(DashboardService.class);

  @Override
  protected DashboardResult doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    LOG.debug("Querying processing status..");
    String sql = ExternalizedQuery.getSql("DASHBOARD.PROCESS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    appendQueryParams(query, params);
    List<ProcessingMonitor> procs = query.getResults(ProcessingMonitor.class);

    LOG.debug("Querying automation status..");
    sql = ExternalizedQuery.getSql("DASHBOARD.AUTOMATION");
    query = new PreparedQuery(entityManager, sql);
    appendQueryParams(query, params);
    List<AutomationMonitor> autos = query.getResults(AutomationMonitor.class);

    DashboardResult result = new DashboardResult();
    initFilters(entityManager, result);

    return analyzeResults(result, procs, autos, "Y".equals(params.getParam("LIST_RECORDS")));
  }

  private void appendQueryParams(PreparedQuery query, ParamContainer params) {
    String country = (String) params.getParam("CNTRY");
    String source = (String) params.getParam("SOURCE");
    String type = (String) params.getParam("PROC_TYPE");

    if (!StringUtils.isBlank(country)) {
      query.append("and d.CMR_ISSUING_CNTRY = :CNTRY");
      query.setParameter("CNTRY", country);
    }
    if (!StringUtils.isBlank(source)) {
      query.append("and a.SOURCE_SYST_ID = :SOURCE");
      query.setParameter("SOURCE", source);
    }
    if (!StringUtils.isBlank(type)) {
      query.append("and s.PROCESSING_TYP = :PROC_TYPE");
      query.setParameter("PROC_TYPE", type);
    }
    query.append("order by a.LAST_UPDT_TS desc");
    query.append("fetch first 200 rows only");
    query.setForReadOnly(true);

  }

  private DashboardResult analyzeResults(DashboardResult result, List<ProcessingMonitor> procs, List<AutomationMonitor> autos, boolean addRecords) {
    analyzeProcess(result, procs, addRecords);
    LOG.debug("Returning monitoring results..");
    return result;
  }

  private void initFilters(EntityManager entityManager, DashboardResult result) {
    String sql = "select CNTRY_CD, upper(NM), nvl(PROCESSING_TYP,'MAN') from CREQCMR.SUPP_CNTRY order by CNTRY_CD";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    List<DropdownItemModel> countries = new ArrayList<DropdownItemModel>();
    List<Object[]> results = query.getResults();
    for (Object[] rec : results) {
      DropdownItemModel cntry = new DropdownItemModel();
      cntry.setId((String) rec[0]);
      cntry.setName(rec[0] + "-" + rec[1]);
      countries.add(cntry);
    }
    result.setCountries(countries);

    List<DropdownItemModel> processes = new ArrayList<DropdownItemModel>();
    DropdownItemModel proc = null;

    proc = new DropdownItemModel();
    proc.setId("LD");
    proc.setName("LD - LegacyDirect (CMRDB2)");
    processes.add(proc);
    proc = new DropdownItemModel();
    proc.setId("DR");
    proc.setName("DR - iERP (RDC Only)");
    processes.add(proc);
    proc = new DropdownItemModel();
    proc.setId("TC");
    proc.setName("TC - TransactionConnect (Legacy)");
    processes.add(proc);
    proc = new DropdownItemModel();
    proc.setId("MQ");
    proc.setName("MQ - MQ Interface (Legacy)");
    processes.add(proc);
    proc = new DropdownItemModel();
    proc.setId("FR");
    proc.setName("FR - France");
    processes.add(proc);
    proc = new DropdownItemModel();
    proc.setId("MA");
    proc.setName("MA - Austria");
    processes.add(proc);
    proc = new DropdownItemModel();
    proc.setId("MD");
    proc.setName("MD - Switzerland");
    processes.add(proc);

    result.setProcTypes(processes);

    sql = "select distinct UPPER(SOURCE_SYST_ID),UPPER(SOURCE_SYST_ID) from CREQCMR.ADMIN";
    query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    List<DropdownItemModel> partners = new ArrayList<DropdownItemModel>();
    results = query.getResults();
    for (Object[] rec : results) {
      DropdownItemModel partner = new DropdownItemModel();
      partner.setId((String) rec[0]);
      partner.setName((String) rec[0]);
      partners.add(partner);
    }
    result.setPartners(partners);

  }

  private void analyzeProcess(DashboardResult result, List<ProcessingMonitor> procs, boolean listRecords) {
    LOG.debug("Analyzing processing status..");

    SimpleDateFormat tsFormatter = new SimpleDateFormat("yyyy-MMM-dd");
    if (listRecords) {
      result.setProcessingRecords(procs);
    } else {
      result.setProcessingRecords(new ArrayList<ProcessingMonitor>());
    }
    Map<String, Integer> countryCounts = new HashMap<String, Integer>();
    Map<String, Long> minCounts = new HashMap<String, Long>();
    Map<String, Long> maxCounts = new HashMap<String, Long>();
    Map<String, Integer> stuckCounts = new HashMap<String, Integer>();
    List<String> stuckCountries = new ArrayList<String>();

    String processThreshold = SystemParameters.getString("DASHBOARD.PROC.MAX");
    int procMaxStuck = 10;
    if (!StringUtils.isBlank(processThreshold) && !StringUtils.isNumeric(processThreshold)) {
      procMaxStuck = Integer.parseInt(processThreshold);
    }

    String minsThreshold = SystemParameters.getString("DASHBOARD.PROC.MINS");
    int minsMaxStuck = 15;
    if (!StringUtils.isBlank(minsThreshold) && !StringUtils.isNumeric(minsThreshold)) {
      minsMaxStuck = Integer.parseInt(minsThreshold);
    }

    String cntry = null;
    for (ProcessingMonitor proc : procs) {
      cntry = proc.getCmrIssuingCntry() + "-" + proc.getCntryNm();
      countryCounts.putIfAbsent(cntry, 0);
      stuckCounts.putIfAbsent(proc.getProcessingTyp(), 0);
      minCounts.putIfAbsent(cntry, Long.MAX_VALUE);
      maxCounts.putIfAbsent(cntry, new Long(0));

      if (proc.getLockBy() != null && proc.getLockBy().contains("@")) {
        proc.setManual(true);
      }

      if (proc.getDiffDay() > 30) {
        proc.setObsolete(true);
      } else if (proc.getDiffMin() > minsMaxStuck && !"Y".equals(proc.getHostDown()) && !proc.isManual()) {
        proc.setStuck(true);
      }

      long millis = proc.getDiffMin() * 1000 * 60;
      String duration = DurationFormatUtils.formatDuration(millis, "d'd' HH'h' mm'm'");
      proc.setPendingTime(duration);
      String lastUpdate = tsFormatter.format(proc.getLastUpdtTs());
      proc.setLastUpdated(lastUpdate);

      countryCounts.put(cntry, countryCounts.get(cntry) + 1);
      if (proc.getDiffMin() < minCounts.get(cntry) && !proc.isObsolete()) {
        minCounts.put(cntry, proc.getDiffMin());
      }
      if (proc.getDiffMin() > maxCounts.get(cntry) && !proc.isObsolete()) {
        maxCounts.put(cntry, proc.getDiffMin());
      }
      if (proc.isStuck()) {
        stuckCounts.put(proc.getProcessingTyp(), stuckCounts.get(proc.getProcessingTyp()) + 1);
        if (stuckCounts.get(proc.getProcessingTyp()) > procMaxStuck && !stuckCountries.contains(proc.getProcessingTyp())) {
          stuckCountries.add(proc.getProcessingTyp());
        }
      }
      switch (proc.getReqType()) {
      case "C":
        proc.setReqType("CRE");
        break;
      case "U":
        proc.setReqType("UPD");
        break;
      case "M":
        proc.setReqType("MU");
        break;
      case "N":
        proc.setReqType("MC");
        break;
      case "D":
        proc.setReqType("DEL");
        break;
      case "R":
        proc.setReqType("REA");
        break;
      case "E":
        proc.setReqType("ENT");
        break;
      }
      switch (proc.getReqStatus()) {
      case "PCR":
        if (proc.isManual()) {
          proc.setProcessBy("Manual");
        } else {
          proc.setProcessBy("In Legacy/DB2");
        }
        break;
      case "PCO":
        if (proc.isManual()) {
          proc.setProcessBy("Manual");
        } else {
          proc.setProcessBy("In RDC");
        }
        break;
      case "PCP":
        proc.setProcessBy("Pending");
        break;
      }

      if (!"Y".equals(proc.getHostDown())) {
        if (!LegacyDowntimes.isUp(proc.getCmrIssuingCntry(), proc.getTs())) {
          proc.setHostDown("Y");
        }
      }

    }
    for (

    String key : minCounts.keySet()) {
      if (minCounts.get(key) == Long.MAX_VALUE) {
        minCounts.put(key, (long) 0);
      }
    }

    ProcessingModel procModel = new ProcessingModel();
    procModel.setPendingCounts(countryCounts);
    procModel.setMinCounts(minCounts);
    procModel.setMaxCounts(maxCounts);
    procModel.setStuckCounts(stuckCounts);
    procModel.setCountsThreshold(procMaxStuck);
    procModel.setMinsThreshold(minsMaxStuck);

    procModel.setProcessingStatus("ORANGE");
    for (String key : stuckCounts.keySet()) {
      if (stuckCounts.get(key) > procMaxStuck * 2) {
        procModel.setProcessingStatus("RED");
      }
    }
    if (!stuckCountries.isEmpty()) {
      StringBuilder msg = new StringBuilder();
      for (String country : stuckCountries) {
        msg.append(msg.length() > 0 ? ", " : "");
        msg.append(country);
      }
      procModel.setAlert("Processing may be stuck for the following: " + msg.toString() + ".");
    } else {
      procModel.setProcessingStatus("GREEN");
    }

    result.setProcessing(procModel);
  }
}
