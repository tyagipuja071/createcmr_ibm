/**
 * 
 */
package com.ibm.cio.cmr.request.util.dpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JeffZAMORA
 *
 */
public class DPLResultsItemizer {

  private String searchArgument;
  private List<DPLResultCompany> records = new ArrayList<DPLResultCompany>();

  public DPLResultCompany get(String companyName) {
    for (DPLResultCompany company : records) {
      if (companyName.toUpperCase().equals(company.getCompanyName().toUpperCase())) {
        return company;
      }
    }
    return null;
  }

  public String getSearchArgument() {
    return searchArgument;
  }

  public void setSearchArgument(String searchArgument) {
    this.searchArgument = searchArgument;
  }

  public List<DPLResultCompany> getRecords() {
    return records;
  }

  public void setRecords(List<DPLResultCompany> records) {
    this.records = records;
  }
}
