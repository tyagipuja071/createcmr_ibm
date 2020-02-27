/**
 * 
 */
package com.ibm.cio.cmr.request.util.system;

import java.util.List;
import java.util.Map;

import com.ibm.cio.cmr.request.model.system.RequestStatsModel;
import com.ibm.cio.cmr.request.model.system.SquadStatisticsModel;

/**
 * @author Jeffrey Zamora
 * 
 */
public class RequestStatsContainer {

  private List<RequestStatsModel> records;
  private Map<Long, List<String>> rejectionReasons;
  private List<SquadStatisticsModel> squadRecords;

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
}
