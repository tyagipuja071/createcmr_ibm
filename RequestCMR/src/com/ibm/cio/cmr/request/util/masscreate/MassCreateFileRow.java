/**
 * 
 */
package com.ibm.cio.cmr.request.util.masscreate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.MassCreateData;

/**
 * Represents a row on the mass create file
 * 
 * @author Jeffrey Zamora
 * 
 */
public class MassCreateFileRow implements Comparable<MassCreateFileRow> {

  private int seqNo;
  private MassCreateData data;
  private String errorMessage;
  private List<MassCreateAddr> addresses = new ArrayList<MassCreateAddr>();
  private Map<String, Object> rawValueMap = new HashMap<String, Object>();
  private List<String> updatedCols = new ArrayList<String>();
  private MassCreateFile parentFile;
  private String cmrNo;

  /**
   * Gets the {@link MassCreateData} representing the row
   * 
   * @return
   */
  public MassCreateData getData() {
    return data;
  }

  /**
   * Maps the raw value from xls to the column
   * 
   * @param colName
   * @param value
   */
  public void mapRawValue(String colName, Object value) {
    this.rawValueMap.put(colName, value);
  }

  /**
   * Gets the raw xls value
   * 
   * @param colName
   * @return
   */
  public Object getRawValue(String colName) {
    return this.rawValueMap.get(colName);
  }

  /**
   * Adds the column to the list of updated columns
   * 
   * @param columnName
   */
  public void addUpdateCol(String columnName) {
    if (!this.updatedCols.contains(columnName)) {
      this.updatedCols.add(columnName);
    }
  }

  /**
   * Checks is this column was updated
   * 
   * @param columnName
   * @return
   */
  public boolean isUpdated(String columnName) {
    return this.updatedCols.contains(columnName);
  }

  public void setData(MassCreateData data) {
    this.data = data;
  }

  public List<MassCreateAddr> getAddresses() {
    return addresses;
  }

  public void addAddresses(List<MassCreateAddr> addresses) {
    this.addresses.addAll(addresses);
  }

  public int getSeqNo() {
    return seqNo;
  }

  public void setSeqNo(int seqNo) {
    this.seqNo = seqNo;
  }

  public boolean hasError() {
    return !StringUtils.isBlank(this.errorMessage);
  }

  @Override
  public int compareTo(MassCreateFileRow o) {
    if (o == null) {
      return -1;
    }
    return this.seqNo < o.seqNo ? -1 : (this.seqNo > o.seqNo ? 1 : 0);
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public boolean isCreateByModel() {
    return this.data != null && "BYMODEL".equals(this.data.getCustSubGrp());
  }

  public MassCreateFile getParentFile() {
    return parentFile;
  }

  public void setParentFile(MassCreateFile parentFile) {
    this.parentFile = parentFile;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

}
