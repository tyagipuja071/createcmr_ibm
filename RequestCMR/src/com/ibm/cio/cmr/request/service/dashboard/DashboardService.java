/**
 * 
 */
package com.ibm.cio.cmr.request.service.dashboard;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
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

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.dashboard.AutomationMonitor;
import com.ibm.cio.cmr.request.entity.dashboard.ProcessingMonitor;
import com.ibm.cio.cmr.request.model.DropdownItemModel;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.dashboard.AutoProcessModel;
import com.ibm.cio.cmr.request.model.dashboard.CountryAutoStats;
import com.ibm.cio.cmr.request.model.dashboard.DashboardResult;
import com.ibm.cio.cmr.request.model.dashboard.ProcessingModel;
import com.ibm.cio.cmr.request.model.dashboard.ServicesModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDowntimes;
import com.ibm.cmr.services.client.CROSServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.cros.CROSQueryRequest;
import com.ibm.cmr.services.client.cros.CROSQueryResponse;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * Handles the creation of the monitor dashboard with results of system checks
 * 
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
    appendQueryParams(query, params, false);
    List<ProcessingMonitor> procs = query.getResults(ProcessingMonitor.class);

    LOG.debug("Querying automation status..");
    sql = ExternalizedQuery.getSql("DASHBOARD.AUTOMATION");
    query = new PreparedQuery(entityManager, sql);
    appendQueryParams(query, params, true);
    List<AutomationMonitor> autos = query.getResults(AutomationMonitor.class);

    DashboardResult result = new DashboardResult();
    initFilters(entityManager, result);

    boolean excludeAutoPercentage = !"Y".equals(params.getParam("INCL_AUTO_PERCENT"));
    return analyzeResults(result, procs, autos, "Y".equals(params.getParam("LIST_RECORDS")), excludeAutoPercentage);
  }

  /**
   * Appends the SQL for the 3 available params
   * 
   * @param query
   * @param params
   */
  private void appendQueryParams(PreparedQuery query, ParamContainer params, boolean automation) {
    String country = (String) params.getParam("CNTRY");
    String source = (String) params.getParam("SOURCE");
    String type = (String) params.getParam("PROC_TYPE");

    if (!StringUtils.isBlank(country)) {
      query.append("and d.CMR_ISSUING_CNTRY = :CNTRY");
      query.setParameter("CNTRY", country);
    }
    if (!StringUtils.isBlank(source)) {
      query.append("and upper(a.SOURCE_SYST_ID) = :SOURCE");
      query.setParameter("SOURCE", source.toUpperCase());
    }
    if (!StringUtils.isBlank(type)) {
      query.append("and s.PROCESSING_TYP = :PROC_TYPE");
      query.setParameter("PROC_TYPE", type);
    }
    if (automation) {
      query.append(
          "order by case when a.REQ_STATUS = 'AUT' then 0 when a.REQ_STATUS = 'RET' then 1 when a.REQ_STATUS in ('PPN', 'PVA') then 2 else 3 end, a.LAST_UPDT_TS desc");
    } else {
      query.append(
          "order by case when timestampdiff(16,current timestamp - a.LAST_UPDT_TS) > 30 then 2 else 1 end, case when a.REQ_STATUS = 'COM' then 5 else 1 end, a.LAST_UPDT_TS desc");
    }
    query.append("fetch first 200 rows only");
    query.setForReadOnly(true);

  }

  /**
   * Analyzes the results
   * 
   * @param result
   * @param procs
   * @param autos
   * @param addRecords
   * @return
   */
  private DashboardResult analyzeResults(DashboardResult result, List<ProcessingMonitor> procs, List<AutomationMonitor> autos, boolean addRecords,
      boolean excludeAutoPct) {
    analyzeProcess(result, procs, addRecords);
    analyzeAutomation(result, autos, addRecords, excludeAutoPct);
    checkConnections(result);

    List<String> statuses = new ArrayList<>();
    statuses.add(result.getServices().getServicesStatus());
    statuses.add(result.getProcessing().getProcessingStatus());
    statuses.add(result.getAutomation().getAutomationStatus());
    result.setOverallStatus(DashboardResult.STATUS_OK);
    if (statuses.contains(DashboardResult.STATUS_DOWN)) {
      result.setOverallStatus(DashboardResult.STATUS_DOWN);
    } else if (statuses.contains(DashboardResult.STATUS_WARNING)) {
      result.setOverallStatus(DashboardResult.STATUS_WARNING);
    }
    LOG.debug("Returning monitoring results..");
    return result;
  }

  /**
   * Initializes the available values for the filters
   * 
   * @param entityManager
   * @param result
   */
  private void initFilters(EntityManager entityManager, DashboardResult result) {
    // countries
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

    // processes
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

    // partners
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

  /**
   * Analyzes the records processed and creates the stats
   * 
   * @param result
   * @param procs
   * @param listRecords
   */
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
    Map<String, Integer> errorCounts = new HashMap<String, Integer>();
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

    String errorThreshold = SystemParameters.getString("DASHBOARD.PROC.ERR");
    int errorMax = 3;
    if (!StringUtils.isBlank(errorThreshold) && !StringUtils.isNumeric(errorThreshold)) {
      errorMax = Integer.parseInt(errorThreshold);
    }

    String cntry = null;
    for (ProcessingMonitor proc : procs) {
      cntry = proc.getCmrIssuingCntry() + "-" + proc.getCntryNm();
      countryCounts.putIfAbsent(cntry, 0);
      errorCounts.putIfAbsent(cntry, 0);
      stuckCounts.putIfAbsent(StringUtils.isBlank(proc.getProcessingTyp()) ? "NONE" : proc.getProcessingTyp(), 0);
      minCounts.putIfAbsent(cntry, Long.MAX_VALUE);
      maxCounts.putIfAbsent(cntry, new Long(0));

      if (proc.getLockBy() != null && proc.getLockBy().contains("@")) {
        proc.setManual(true);
      }

      if (!"COM".equals(proc.getReqStatus())) {
        if (proc.getDiffDay() > 30) {
          proc.setObsolete(true);
        } else if (proc.getDiffMin() > minsMaxStuck && !"Y".equals(proc.getHostDown()) && !proc.isManual()) {
          proc.setStuck(true);
        }
      }

      long millis = proc.getDiffMin() * 1000 * 60;
      String duration = DurationFormatUtils.formatDuration(millis, "d'd' HH'h' mm'm'");
      proc.setPendingTime(duration);
      String lastUpdate = tsFormatter.format(proc.getLastUpdtTs());
      proc.setLastUpdated(lastUpdate);

      if ("COM".equals(proc.getReqStatus())
          && ("E".equals(proc.getProcessedFlag()) || "A".equals(proc.getRdcProcessingStatus()) || "N".equals(proc.getRdcProcessingStatus()))) {
        errorCounts.put(cntry, errorCounts.get(cntry) + 1);
      }
      if (!"COM".equals(proc.getReqStatus())) {
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
      case "COM":
        proc.setProcessBy("");
        break;
      case "PCO":
        if (proc.isManual()) {
          proc.setProcessBy("Manual");
        } else {
          proc.setProcessBy("In RDC");
        }
        break;
      case "PCP":
        proc.setProcessBy("");
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
    procModel.setErrorCounts(errorCounts);

    procModel.setProcessingStatus(DashboardResult.STATUS_OK);

    for (String key : stuckCounts.keySet()) {
      if (stuckCounts.get(key) > procMaxStuck * 2) {
        procModel.setProcessingStatus(DashboardResult.STATUS_DOWN);
      } else if (stuckCounts.get(key) > procMaxStuck) {
        procModel.setProcessingStatus(DashboardResult.STATUS_WARNING);
      }
    }

    StringBuilder alert = new StringBuilder();

    if (!stuckCountries.isEmpty()) {
      StringBuilder msg = new StringBuilder();
      for (String country : stuckCountries) {
        msg.append(msg.length() > 0 ? ", " : "");
        msg.append(country);
      }
      alert.append("Processing may be stuck for the following: " + msg.toString() + ".");
    }

    boolean exceed = false;
    for (String key : errorCounts.keySet()) {
      if (errorCounts.get(key) > errorMax * 2) {
        procModel.setProcessingStatus(DashboardResult.STATUS_DOWN);
        exceed = true;
      }
      if (errorCounts.get(key) > errorMax) {
        if (!DashboardResult.STATUS_DOWN.equals(procModel.getProcessingStatus())) {
          procModel.setProcessingStatus(DashboardResult.STATUS_WARNING);
          exceed = true;
        }
      }
    }
    if (exceed) {
      alert.append(alert.length() > 0 ? " " : "");
      alert.append("Legacy and/or RDC processing errors exceeded the threshold for some countries.");
    }
    procModel.setErrorThreshold(errorMax);
    procModel.setAlert(alert.toString());

    result.setProcessing(procModel);
  }

  /**
   * Analyzes the automation records and creates the stats
   * 
   * @param result
   * @param autos
   * @param listRecords
   */
  private void analyzeAutomation(DashboardResult result, List<AutomationMonitor> autos, boolean listRecords, boolean excludeAutoPct) {
    LOG.debug("Analyzing automation status..");

    if (listRecords) {
      result.setAutomationRecords(autos);
    }

    Map<String, List<AutomationMonitor>> countryRecs = new HashMap<String, List<AutomationMonitor>>();
    String cntry = null;
    // register all recs per country
    for (AutomationMonitor auto : autos) {
      cntry = auto.getCmrIssuingCntry() + "-" + auto.getCntryNm();
      countryRecs.putIfAbsent(cntry, new ArrayList<AutomationMonitor>());
      countryRecs.get(cntry).add(auto);
    }

    // 10 mins by default
    String autoThreshold = SystemParameters.getString("DASHBOARD.AUTO.PROC");
    int autoProcMax = 10;
    if (!StringUtils.isBlank(autoThreshold) && !StringUtils.isNumeric(autoThreshold)) {
      autoProcMax = Integer.parseInt(autoThreshold);
    }
    boolean autoProcExceeded = false;

    // 20 mins by default
    String completeThreshold = SystemParameters.getString("DASHBOARD.AUTO.COMP");
    int compMax = 20;
    if (!StringUtils.isBlank(completeThreshold) && !StringUtils.isNumeric(completeThreshold)) {
      compMax = Integer.parseInt(completeThreshold);
    }
    boolean compExceeded = false;

    // 70 %
    String manualThreshold = SystemParameters.getString("DASHBOARD.AUTO.MANUAL");
    int manualMax = 70;
    if (!StringUtils.isBlank(manualThreshold) && !StringUtils.isNumeric(manualThreshold)) {
      manualMax = Integer.parseInt(manualThreshold);
    }
    boolean manualExceeded = false;

    AutoProcessModel model = new AutoProcessModel();
    int allPending = 0;
    for (String key : countryRecs.keySet()) {

      long maxAutoDiff = 0;
      long maxCompleteDiff = 0;
      long maxFullAutoDiff = 0;
      List<Long> autoCompletes = new ArrayList<Long>();
      List<Long> allCompletes = new ArrayList<Long>();
      List<Long> processings = new ArrayList<Long>();
      long manualCount = 0;
      long fullAutoCount = 0;
      long total = 0;

      // iterate the records and compute stats
      List<AutomationMonitor> list = countryRecs.get(key);
      total = list.size();
      long pending = 0;
      for (AutomationMonitor rec : list) {
        long diff = rec.getDiffMin() - (rec.getAprMin() < rec.getDiffMin() ? rec.getAprMin() : 0);
        if ("COM".equals(rec.getReqStatus()) && !"Y".equals(rec.getManual())) {
          autoCompletes.add(diff);
          if (diff > maxFullAutoDiff) {
            maxFullAutoDiff = diff;
          }
          fullAutoCount++;
        }
        if ("AUT".equals(rec.getReqStatus()) || "RET".equals(rec.getReqStatus())) {
          pending++;
        }
        if ("COM".equals(rec.getReqStatus())) {
          allCompletes.add(diff);
          if (diff > maxCompleteDiff) {
            maxCompleteDiff = diff;
          }
        }

        if ("Y".equals(rec.getManual())) {
          manualCount++;
        }
        processings.add(rec.getDiffMinNxt());
        if (rec.getDiffMinNxt() > maxAutoDiff) {
          maxAutoDiff = rec.getDiffMinNxt();
        }

        switch (rec.getReqType()) {
        case "C":
          rec.setReqType("CRE");
          break;
        case "U":
          rec.setReqType("UPD");
          break;
        case "M":
          rec.setReqType("MU");
          break;
        case "N":
          rec.setReqType("MC");
          break;
        case "D":
          rec.setReqType("DEL");
          break;
        case "R":
          rec.setReqType("REA");
          break;
        case "E":
          rec.setReqType("ENT");
          break;
        }

      }
      CountryAutoStats stats = new CountryAutoStats();
      // churn numbers
      DecimalFormat pctFormat = new DecimalFormat("0.0");
      float manualPct = (float) manualCount / (float) total;
      stats.setManualPercentage(pctFormat.format(manualPct * 100));
      if (manualPct * 100 > manualMax) {
        manualExceeded = true;
      }

      float fullAutoPct = (float) fullAutoCount / (float) total;
      stats.setFullAutoPercentage(pctFormat.format(fullAutoPct * 100));

      boolean found = false;
      int index = 0;
      for (int i = 0; i < autoCompletes.size(); i++) {
        if (autoCompletes.get(i) == maxFullAutoDiff) {
          // LOG.trace(key + " Auto complete outlier: " + maxFullAutoDiff);
          found = true;
          index = i;
          break;
        }
      }
      if (found && autoCompletes.size() > 2) {
        autoCompletes.remove(index);
      }
      long totalMins = 0;
      for (Long mins : autoCompletes) {
        totalMins += mins * 1000 * 60;
      }
      long ave = (long) ((float) totalMins / (float) autoCompletes.size());
      if (ave < 0) {
        ave = 0;
      }
      // LOG.trace(key + " Auto Complete Average: " + ave);
      String duration = DurationFormatUtils.formatDuration(ave, "HH'h' mm'm'");
      String durMin = DurationFormatUtils.formatDuration(ave, "mm");
      stats.setFullAutoAverageMin(Long.parseLong(durMin));
      stats.setFullAutoAverage(duration);
      stats.setFullAutoOutlier(autoCompletes.size() > 5 ? maxFullAutoDiff : 0);
      if (stats.getFullAutoAverageMin() > compMax) {
        compExceeded = true;
      }
      found = false;

      index = 0;
      for (int i = 0; i < allCompletes.size(); i++) {
        if (allCompletes.get(i) == maxCompleteDiff) {
          // LOG.trace(key + " All complete outlier: " + maxCompleteDiff);
          found = true;
          index = i;
          break;
        }
      }
      if (found && allCompletes.size() > 2) {
        allCompletes.remove(index);
      }
      totalMins = 0;
      for (Long mins : allCompletes) {
        totalMins += mins * 1000 * 60;
      }
      ave = (long) ((float) totalMins / (float) allCompletes.size());
      if (ave < 0) {
        ave = 0;
      }
      // LOG.trace(key + " All Completes Average: " + ave);
      duration = DurationFormatUtils.formatDuration(ave, "HH'h' mm'm'");
      durMin = DurationFormatUtils.formatDuration(ave, "mm");
      stats.setCompletionAverageMin(Long.parseLong(durMin));
      stats.setCompletionAverage(duration);

      index = 0;
      for (int i = 0; i < processings.size(); i++) {
        if (processings.get(i) == maxAutoDiff) {
          // LOG.trace(key + " Processing outlier: " + maxAutoDiff);
          found = true;
          index = i;
          break;
        }
      }
      if (found && processings.size() > 2) {
        processings.remove(index);
      }
      totalMins = 0;
      for (Long mins : processings) {
        totalMins += mins * 1000 * 60;
      }
      ave = (long) ((float) totalMins / (float) processings.size());
      if (ave < 0) {
        ave = 0;
      }
      // LOG.trace(key + " Processing Average: " + ave);
      duration = DurationFormatUtils.formatDuration(ave, "mm'm'");
      durMin = DurationFormatUtils.formatDuration(ave, "mm");
      stats.setAutomationAverage(duration);
      stats.setAutomationOutlier(processings.size() > 5 ? maxAutoDiff : 0);
      stats.setAutomationAverageMin(Long.parseLong(durMin));
      if (stats.getAutomationAverageMin() > autoProcMax) {
        autoProcExceeded = true;
      }

      // no completes, remove completions
      if (allCompletes.isEmpty()) {
        stats.setCompletionAverage(null);
        stats.setCompletionAverageMin(0);
      }
      if (autoCompletes.isEmpty()) {
        stats.setFullAutoAverage(null);
        stats.setFullAutoAverageMin(0);
        stats.setFullAutoPercentage(null);
        stats.setFullAutoOutlier(0);
      }
      stats.setTotal(total);
      stats.setReviews(manualCount);
      stats.setCompletes(fullAutoCount);

      stats.setCurrentQueue(pending);
      allPending += pending;
      model.getCountryStats().put(key, stats);
    }

    // 20 requests by default
    String pendingThreshold = SystemParameters.getString("DASHBOARD.AUTO.MAX");
    int pendMaxStuck = 20;
    if (!StringUtils.isBlank(pendingThreshold) && !StringUtils.isNumeric(pendingThreshold)) {
      pendMaxStuck = Integer.parseInt(pendingThreshold);
    }

    model.setAutomationStatus(DashboardResult.STATUS_OK);
    StringBuilder alert = new StringBuilder();
    if (allPending > pendMaxStuck) {
      if (allPending > pendMaxStuck * 2) {
        model.setAutomationStatus(DashboardResult.STATUS_DOWN);
      }
      model.setAutomationStatus(DashboardResult.STATUS_WARNING);
      alert.append("Total Pending requests for automation is greater than threshold.");
    }
    if (autoProcExceeded) {
      if (!DashboardResult.STATUS_DOWN.equals(model.getAutomationStatus())) {
        model.setAutomationStatus(DashboardResult.STATUS_WARNING);
      }
      alert.append(alert.length() > 0 ? " " : "");
      alert.append("Automation validations for some countries take longer than the threshold.");
    }
    if (compExceeded) {
      if (!DashboardResult.STATUS_DOWN.equals(model.getAutomationStatus())) {
        model.setAutomationStatus(DashboardResult.STATUS_WARNING);
      }
      alert.append(alert.length() > 0 ? " " : "");
      alert.append("Completion turn-around times for some countries take longer than the threshold.");
    }
    if (manualExceeded) {
      if (!DashboardResult.STATUS_DOWN.equals(model.getAutomationStatus())) {
        model.setAutomationStatus(DashboardResult.STATUS_WARNING);
      }
      alert.append(alert.length() > 0 ? " " : "");
      alert.append("Manual review percentage for some countries are greater than the threshold.");
    }
    model.setAllPending(allPending);
    model.setTotalRecords(autos.size());
    model.setManualPctThreshold(manualMax);
    model.setPendingThreshold(pendMaxStuck);
    model.setProcessTimeThreshold(autoProcMax);
    model.setTatThreshold(compMax);
    model.setAlert(alert.toString());
    result.setAutomation(model);
  }

  private void checkConnections(DashboardResult result) {
    ServicesModel model = new ServicesModel();
    try {
      LOG.debug("Checking findCMR connection...");
      FindCMRResultModel cmr = SystemUtil.findCMRs("1000000", "897", 1);
      if (cmr != null && cmr.getItems() != null && !cmr.getItems().isEmpty()) {
        LOG.debug("FindCMR check successful");
        model.setFindCmr(true);
      } else {
        model.setFindCmr(false);
      }
    } catch (Exception e) {
      LOG.error("FindCMR error", e);
    }
    pingCmrservices(model);
    pingCIservices(model);
    model.setCros(true);
    // testCROS(model);

    model.setServicesStatus(DashboardResult.STATUS_OK);
    if (!model.isFindCmr() || !model.isCmrServices() || !model.isCiServices()) {
      model.setServicesStatus(DashboardResult.STATUS_DOWN);
      model.setAlert("One or more connections to main components are down.");
    } else if (!model.isCris() || !model.isCros() || !model.isMq() || !model.isUsCmr()) {
      model.setServicesStatus(DashboardResult.STATUS_WARNING);
      model.setAlert("One or more connections to secondary components are down.");
    }
    result.setServices(model);
  }

  private boolean pingCmrservices(ServicesModel model) {
    LOG.debug("Checking CMR services ping..");
    String cmrservices = SystemConfiguration.getValue("CMR_SERVICES_URL");
    if (StringUtils.isBlank(cmrservices)) {
      return false;
    }
    cmrservices += "/cmrs/ping";
    try {
      URL ping = new URL(cmrservices);
      LOG.debug("Connecting to CMR Services ping " + cmrservices);
      HttpURLConnection conn = (HttpURLConnection) ping.openConnection();
      conn.setDoInput(true);
      conn.setConnectTimeout(1000 * 30); // 30 seconds
      InputStream is = conn.getInputStream();
      StringBuilder results = new StringBuilder();
      try {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        try {
          BufferedReader br = new BufferedReader(isr);
          try {
            String line = null;
            while ((line = br.readLine()) != null) {
              results.append(line);
            }
          } finally {
            br.close();
          }
        } finally {
          isr.close();
        }
      } finally {
        is.close();
      }

      LOG.debug(" - ping inputstream read..");

      conn.disconnect();

      JSONObject json = JSONObject.parse(results.toString());
      JSONObject pingResults = (JSONObject) json.get("pingResults");
      JSONArray services = (JSONArray) pingResults.get("services");
      for (Object o : services) {
        JSONObject service = (JSONObject) o;
        if (service.containsKey("DatabaseService")) {
          JSONObject db = (JSONObject) service.get("DatabaseService");
          JSONObject databases = (JSONObject) db.get("databases");
          // USCMR sunset, remove check
          // if (databases.containsKey("USCMR")) {
          // String uscmr = (String) databases.get("USCMR");
          // if ("connected".equals(uscmr)) {
          // model.setUsCmr(true);
          // }
          // }
          if (databases.containsKey("CRIS")) {
            String cris = (String) databases.get("CRIS");
            if ("connected".equals(cris)) {
              model.setCris(true);
            }
          }
        }
        model.setMq(true);
        if (service.containsKey("WTAASQueryService")) {
          JSONObject mq = (JSONObject) service.get("WTAASQueryService");
          JSONArray managers = (JSONArray) mq.get("managers");
          for (Object o1 : managers) {
            JSONObject mgr = (JSONObject) o1;
            String mgrKey = null;
            if (mgr.containsKey("WTAS1")) {
              mgrKey = "WTAS2";
            } else if (mgr.containsKey("WTAS2")) {
              mgrKey = "WTAS2";
            } else if (mgr.containsKey("WTAS3")) {
              mgrKey = "WTAS3";
            }
            String status = (String) mgr.get(mgrKey);
            if (!"connected".equals(status)) {
              model.setMq(false);
            }
          }
        }
      }
      LOG.debug("CMR Services ping successful");
      model.setUsCmr(true);
      model.setCmrServices(true);
      return true;
    } catch (Exception e) {
      LOG.error("Error when pinging CMR Services", e);
      model.setCmrServices(false);
      return false;
    }
  }

  private boolean pingCIservices(ServicesModel model) {
    LOG.debug("Checking CI services ping..");
    String cmrservices = SystemConfiguration.getValue("BATCH_CI_SERVICES_URL");
    if (StringUtils.isBlank(cmrservices)) {
      return false;
    }
    cmrservices += "/service/ping";
    try {
      URL ping = new URL(cmrservices);
      LOG.debug("Connecting to CI Services ping " + cmrservices);
      HttpURLConnection conn = (HttpURLConnection) ping.openConnection();
      conn.setDoInput(true);
      conn.setConnectTimeout(1000 * 30); // 30 seconds
      InputStream is = conn.getInputStream();
      StringBuilder results = new StringBuilder();
      try {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        try {
          BufferedReader br = new BufferedReader(isr);
          try {
            String line = null;
            while ((line = br.readLine()) != null) {
              results.append(line);
            }
          } finally {
            br.close();
          }
        } finally {
          isr.close();
        }
      } finally {
        is.close();
      }

      LOG.debug(" - ping inputstream read..");

      conn.disconnect();

      JSONObject json = JSONObject.parse(results.toString());
      JSONObject pingResults = (JSONObject) json.get("pingResults");
      JSONArray services = (JSONArray) pingResults.get("services");
      for (Object o : services) {
        JSONObject service = (JSONObject) o;
        if (service.containsKey("SearchService")) {
          JSONObject db = (JSONObject) service.get("SearchService");
          JSONArray applications = (JSONArray) db.get("applications");
          for (Object o1 : applications) {
            JSONObject app = (JSONObject) o1;
            if (app.containsKey("findcmr")) {
              JSONObject findcmr = (JSONObject) app.get("findcmr");
              JSONObject test = (JSONObject) findcmr.get("test");
              String connection = (String) test.get("connect");
              if ("successful".equals(connection)) {
                model.setCiServices(true);
              }
            }
          }
        }
      }
      LOG.debug("CI Services ping successful");
      return true;
    } catch (Exception e) {
      LOG.error("Error when pinging CI Services", e);
      model.setCiServices(false);
      return false;
    }
  }

  public void testCROS(ServicesModel model) {
    LOG.debug("Checkin CROS connection..");
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    CROSQueryRequest request = new CROSQueryRequest();
    request.setIssuingCntry("631");
    request.setCmrNo("780080");

    try {
      CROSServiceClient client = CmrServicesFactory.getInstance().createClient(baseUrl, CROSServiceClient.class);
      CROSQueryResponse response = client.executeAndWrap(CROSServiceClient.QUERY_APP_ID, request, CROSQueryResponse.class);
      if (response != null && response.isSuccess()) {
        model.setCros(true);
      }
    } catch (Exception e) {
      LOG.error("Error when testing CROS.", e);
    }
  }

}
