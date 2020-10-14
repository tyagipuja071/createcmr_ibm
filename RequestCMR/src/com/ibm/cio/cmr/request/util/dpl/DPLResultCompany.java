/**
 * 
 */
package com.ibm.cio.cmr.request.util.dpl;

import java.util.ArrayList;
import java.util.List;

import com.ibm.cmr.services.client.dpl.DPLRecord;

/**
 * @author JeffZAMORA
 *
 */
public class DPLResultCompany {

  private String companyName;
  private int itemNo;
  private List<DPLRecord> records = new ArrayList<DPLRecord>();

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public int getItemNo() {
    return itemNo;
  }

  public void setItemNo(int itemNo) {
    this.itemNo = itemNo;
  }

  public List<DPLRecord> getRecords() {
    return records;
  }

  public void setRecords(List<DPLRecord> records) {
    this.records = records;
  }

}
