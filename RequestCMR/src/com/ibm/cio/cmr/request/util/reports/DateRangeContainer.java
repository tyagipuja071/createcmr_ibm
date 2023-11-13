/**
 * 
 */
package com.ibm.cio.cmr.request.util.reports;

import java.sql.Timestamp;

/**
 * Contains to and from dates for use in reporting
 * 
 * @author 136786PH1
 *
 */
public class DateRangeContainer {

  private Timestamp fromDate;
  private Timestamp toDate;

  public DateRangeContainer(Timestamp fromDate, Timestamp toDate) {
    this.fromDate = fromDate;
    this.toDate = toDate;
  }

  public Timestamp getFromDate() {
    return fromDate;
  }

  public Timestamp getToDate() {
    return toDate;
  }

}
