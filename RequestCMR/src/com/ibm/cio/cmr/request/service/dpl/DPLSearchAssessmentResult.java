package com.ibm.cio.cmr.request.service.dpl;

import java.util.List;

import com.ibm.cmr.services.client.dpl.DPLSearchResults;

public class DPLSearchAssessmentResult {

  private boolean noWatsonxMatches;
  private List<DPLSearchResults> results;
  private boolean success;

  public boolean isNoWatsonxMatches() {
    return noWatsonxMatches;
  }

  public void setNoWatsonxMatches(boolean noWatsonxMatches) {
    this.noWatsonxMatches = noWatsonxMatches;
  }

  public List<DPLSearchResults> getResults() {
    return results;
  }

  public void setResults(List<DPLSearchResults> results) {
    this.results = results;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }
}
