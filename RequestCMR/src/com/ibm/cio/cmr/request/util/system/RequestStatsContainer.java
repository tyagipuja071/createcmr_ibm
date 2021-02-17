/**
 * 
 */
package com.ibm.cio.cmr.request.util.system;

import java.util.List;
import java.util.Map;

import com.ibm.cio.cmr.request.model.system.AutomationStatsModel;
import com.ibm.cio.cmr.request.model.system.AutomationSummaryModel;
import com.ibm.cio.cmr.request.model.system.RequestStatsModel;
import com.ibm.cio.cmr.request.model.system.RequesterStatsModel;
import com.ibm.cio.cmr.request.model.system.SquadStatisticsModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class RequestStatsContainer {

  private List<RequestStatsModel> records;
  private Map<Long, List<String>> rejectionReasons;
  private List<SquadStatisticsModel> squadRecords;
  private List<RequesterStatsModel> requesterRecords;
  private List<AutomationStatsModel> automationRecords;
  private Map<String, AutomationSummaryModel> automationSummary;
  private Map<String, Map<String, AutomationSummaryModel>> weeklyAutomationSummary;
  private Map<String, AutomationSummaryModel> scenarioSummary;

  private Map<String, Long> partnerSummary;

  public List<RequestStatsModel> getRecords() {
    return records;
  }

  public void setRecords(List<RequestStatsModel> records) {
    this.records = records;
  }

  public Map<Long, List<String>> getRejectionReasons() {
    return rejectionReasons;
  }

  public void setRejectionReasons(Map<Long, List<String>> rejectionReasons) {
    this.rejectionReasons = rejectionReasons;
  }

  public List<SquadStatisticsModel> getSquadRecords() {
    return squadRecords;
  }

  public void setSquadRecords(List<SquadStatisticsModel> squadRecords) {
    this.squadRecords = squadRecords;
  }

  public List<RequesterStatsModel> getRequesterRecords() {
    return requesterRecords;
  }

  public void setRequesterRecords(List<RequesterStatsModel> requesterRecords) {
    this.requesterRecords = requesterRecords;
  }

  public List<AutomationStatsModel> getAutomationRecords() {
    return automationRecords;
  }

  public void setAutomationRecords(List<AutomationStatsModel> automationRecords) {
    this.automationRecords = automationRecords;
  }

  public Map<String, AutomationSummaryModel> getAutomationSummary() {
    return automationSummary;
  }

  public void setAutomationSummary(Map<String, AutomationSummaryModel> automationSummary) {
    this.automationSummary = automationSummary;
  }

  public Map<String, Map<String, AutomationSummaryModel>> getWeeklyAutomationSummary() {
    return weeklyAutomationSummary;
  }

  public void setWeeklyAutomationSummary(Map<String, Map<String, AutomationSummaryModel>> weeklyAutomationSummary) {
    this.weeklyAutomationSummary = weeklyAutomationSummary;
  }

  public Map<String, AutomationSummaryModel> getScenarioSummary() {
    return scenarioSummary;
  }

  public void setScenarioSummary(Map<String, AutomationSummaryModel> scenarioSummary) {
    this.scenarioSummary = scenarioSummary;
  }

  public Map<String, Long> getPartnerSummary() {
    return partnerSummary;
  }

  public void setPartnerSummary(Map<String, Long> partnerSummary) {
    this.partnerSummary = partnerSummary;
  }
}
