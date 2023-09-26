/**
 * 
 */
package com.ibm.cmr.create.batch.util.reports;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.util.reports.DateRangeContainer;
import com.ibm.cio.cmr.request.util.reports.Trailer;

/**
 * Generate the trailer line for KSC reports
 * 
 * @author 136786PH1
 *
 */
public class KscReportTrailer implements Trailer {

  @Override
  public String generateTrailer(EntityManager entityManager, List<Object[]> results, DateRangeContainer dateRange) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    DecimalFormat countFormat = new DecimalFormat("000000");
    String formattedDate = dateFormat.format(dateRange.getFromDate());
    return "TRL ASOF DATE=" + formattedDate + " " + countFormat.format(results.size()) + " RUN DATE=" + formattedDate + "    ";
  }

}
