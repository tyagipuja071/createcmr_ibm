/**
 * 
 */
package com.ibm.cio.cmr.request.util.reports;

import java.util.List;

import javax.persistence.EntityManager;

/**
 * Trailer field for {@link ReportSpec} usage
 * 
 * @author 136786PH1
 *
 */
public interface Trailer {

  /**
   * Generates a trailer line for the report
   * 
   * @param entityManager
   * @param results
   * @param dateRange
   * @return
   */
  public String generateTrailer(EntityManager entityManager, List<Object[]> results, DateRangeContainer dateRange);
}
