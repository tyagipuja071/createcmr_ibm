/**
 * 
 */
package com.ibm.cio.cmr.request.model.window;

/**
 * @author Jeffrey Zamora
 * 
 */
public class UpdatedDataModel {

  private String dataField;
  private String newData;
  private String oldData;

  public String getDataField() {
    return dataField;
  }

  public void setDataField(String dataField) {
    this.dataField = dataField;
  }

  public String getNewData() {
    return newData;
  }

  public void setNewData(String newData) {
    this.newData = newData;
  }

  public String getOldData() {
    return oldData;
  }

  public void setOldData(String oldData) {
    this.oldData = oldData;
  }

}
