/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.dashboard.AutoProcessModel;
import com.ibm.cio.cmr.request.model.dashboard.CountryAutoStats;
import com.ibm.cio.cmr.request.model.dashboard.DashboardResult;
import com.ibm.cio.cmr.request.model.dashboard.ProcessingModel;
import com.ibm.cio.cmr.request.model.dashboard.ServicesModel;
import com.ibm.cio.cmr.request.service.dashboard.DashboardService;

/**
 * @author 136786PH1
 *
 */
public class DashboardReporting implements Runnable {

  public static boolean keepRunning = true;
  private static final Logger LOG = Logger.getLogger(DashboardReporting.class);
  private DashboardService service = new DashboardService();

  @Override
  public void run() {
    LOG.debug("Dashboard monitor started..");
    try {
      while (keepRunning) {
        Thread.sleep(1000 * 60 * 30);
        LOG.debug("Extracting Dashboard system status..");
        ParamContainer params = new ParamContainer();
        DashboardResult result = this.service.process(null, params);

        List<String> alerts = new ArrayList<String>();
        boolean sendAlerts = false;
        if (!DashboardResult.STATUS_OK.equals(result.getOverallStatus())) {
          sendAlerts = true;
          alerts.add(SlackAlertsUtil.bold("Overall Status: " + result.getOverallStatus()));
        }
        ServicesModel serviceResult = result.getServices();
        if (!DashboardResult.STATUS_OK.equals(serviceResult.getServicesStatus())) {
          sendAlerts = true;
          StringBuilder alert = new StringBuilder();
          alert.append(SlackAlertsUtil.bold("Services: " + serviceResult.getServicesStatus() + " - " + serviceResult.getAlert()));
          if (!serviceResult.isFindCmr()) {
            alert.append("\n -" + SlackAlertsUtil.bold("FindCMR is down."));
          }
          if (!serviceResult.isCmrServices()) {
            alert.append("\n -" + SlackAlertsUtil.bold("CMRServices is down."));
          }
          if (!serviceResult.isCiServices()) {
            alert.append("\n -" + SlackAlertsUtil.bold("CIServices is down."));
          }
          if (!serviceResult.isCris()) {
            alert.append("\n -" + SlackAlertsUtil.bold("Cannot connect to CRIS (Japan)."));
          }
          if (!serviceResult.isMq()) {
            alert.append("\n -" + SlackAlertsUtil.bold("Cannot connect to WTAAS MQ (AP)."));
          }
          alerts.add(alert.toString());
        }

        ProcessingModel processResult = result.getProcessing();
        if (!DashboardResult.STATUS_OK.equals(processResult.getProcessingStatus())) {
          sendAlerts = true;
          StringBuilder alert = new StringBuilder();
          alert.append(SlackAlertsUtil.bold("Processing: " + processResult.getProcessingStatus() + " - " + processResult.getAlert()));
          Map<String, Integer> countMap = processResult.getStuckCounts();
          for (String cntry : countMap.keySet()) {
            Integer count = countMap.get(cntry);
            if (count > 0) {
              alert.append("\n " + SlackAlertsUtil.bold("- Stuck requests under " + cntry + " = " + count));
            }
          }
          countMap = processResult.getPendingCounts();
          for (String cntry : countMap.keySet()) {
            Integer count = countMap.get(cntry);
            if (count > 0) {
              alert.append("\n " + SlackAlertsUtil.bold("- Pending requests under " + cntry + " = " + count));
            }
          }
          countMap = processResult.getErrorCounts();
          for (String cntry : countMap.keySet()) {
            Integer count = countMap.get(cntry);
            if (count > 0) {
              alert.append("\n " + SlackAlertsUtil.bold("- Errors under " + cntry + " = " + count));
            }
          }
          alerts.add(alert.toString());
        }

        AutoProcessModel autoResult = result.getAutomation();
        if (!DashboardResult.STATUS_OK.equals(autoResult.getAutomationStatus())) {
          sendAlerts = true;
          StringBuilder alert = new StringBuilder();
          alert.append(SlackAlertsUtil.bold("Automation: " + autoResult.getAutomationStatus() + " - " + autoResult.getAlert()));
          if (autoResult.getAllPending() > 5) {
            alert.append("\n " + SlackAlertsUtil.bold("- Pending requests for automation = " + autoResult.getAllPending()));
          }
          Map<String, CountryAutoStats> stats = autoResult.getCountryStats();
          for (String key : stats.keySet()) {
            CountryAutoStats stat = stats.get(key);
            if (stat.getAutomationAverageMin() > autoResult.getProcessTimeThreshold()) {
              System.out.println("ave: " + stat.getAutomationAverageMin() + " threshold: " + autoResult.getProcessTimeThreshold());
              alert.append("\n " + SlackAlertsUtil
                  .bold("- Validation time for " + key + " = " + DurationFormatUtils.formatDuration(stat.getAutomationAverageMin(), "m'm'")));
            }
            if (stat.getFullAutoAverageMin() > autoResult.getTatThreshold()) {
              System.out.println("tat: " + stat.getFullAutoAverageMin() + " threshold: " + autoResult.getTatThreshold());
              alert.append("\n " + SlackAlertsUtil
                  .bold("- Touchless processing time for " + key + " = " + DurationFormatUtils.formatDuration(stat.getFullAutoAverageMin(), "m'm'")));
            }
          }

          alerts.add(alert.toString());
        }

        if (sendAlerts) {
          SlackAlertsUtil.recordGenericAlert("Dashboard", "Monitor Alert", alerts.toArray(new String[0]));
        }

      }
      LOG.debug("DashboardReporting ended.");
    } catch (Exception e) {
      LOG.warn("Dashboard reporting stopped due to an error", e);
      SlackAlertsUtil.recordException("DashboardReporting", "STOP", e);
    }
  }

}
