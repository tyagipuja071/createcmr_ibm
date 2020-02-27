/**
 * 
 */
package com.ibm.cio.cmr.request.model.requestentry;

import java.util.List;

/**
 * @author Jeffrey Zamora
 * 
 */
public class FindCMRResultModel {

  private boolean success;
  private String message;
  private List<FindCMRRecordModel> items;

  public List<FindCMRRecordModel> getItems() {
    return items;
  }

  public void setItems(List<FindCMRRecordModel> items) {
    this.items = items;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
