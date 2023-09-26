/**
 * 
 */
package com.ibm.cio.cmr.request.util.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

/**
 * Defines a fixed width flat file report and the needed information to generate
 * it. The report expects 2 parameters on the SQL retrieved: DATE_FROM and
 * DATE_TO, and will automatically add the MANDT parameter
 * 
 * @author 136786PH1
 *
 */
public class ReportSpec {

  private static final Logger LOG = Logger.getLogger(ReportSpec.class);

  private String sqlKey;
  private Map<String, Object> params = new HashMap<>();
  private List<ReportField> fields = new ArrayList<ReportField>();
  private String reportFilename;
  private Trailer trailer;

  private String filterKey;
  private String orderByKey;
  private int dateAdjust;

  public ReportSpec(String sqlKey, String reportFilename) {
    this.sqlKey = sqlKey;
    this.reportFilename = reportFilename;
  }

  /**
   * Generates a fixed-width report using the date range specified and writes
   * the file to the specified output directory using the filename configured
   * 
   * @param dateRange
   * @param records
   * @param outputDir
   * @throws IOException
   * @throws UnsupportedEncodingException
   */
  public void generate(EntityManager entityManager, DateRangeContainer dateRange, File outputDir) throws UnsupportedEncodingException, IOException {
    LOG.debug("Executing report extraction for query " + this.sqlKey);
    PreparedQuery query = prepareQuery(entityManager, dateRange);
    List<Object[]> results = query.getResults();

    LOG.debug(results.size() + " records retrieved.");
    try (FileOutputStream fos = new FileOutputStream(outputDir + File.separator + this.reportFilename)) {
      try (OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8")) {
        try (PrintWriter pw = new PrintWriter(osw)) {
          for (Object[] record : results) {
            for (int index = 0; index < record.length; index++) {
              ReportField field = this.fields.get(index);
              pw.print(field.getFixedWidthValue(record[index]));
              if (LOG.isTraceEnabled()) {
                LOG.trace("Field: " + field.getName() + " = " + field.getFixedWidthValue(record[index]));
              }
            }
            pw.println();
          }
          if (this.trailer != null) {
            pw.println(this.trailer.generateTrailer(entityManager, results, dateRange));
          }
        }
      }
    }
  }

  /**
   * Prepares the query to be executed by the report. MANDT, DATE_FROM, and
   * DATE_TO are assigned automatically from {@link SystemConfiguration} and
   * passed {@link DateRangeContainer} parameter
   * 
   * @param entityManager
   */
  private PreparedQuery prepareQuery(EntityManager entityManager, DateRangeContainer dateRange) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql(this.sqlKey));
    if (!StringUtils.isBlank(this.filterKey)) {
      query.append(ExternalizedQuery.getSql(this.filterKey));
    }
    if (!StringUtils.isBlank(this.orderByKey)) {
      query.append(ExternalizedQuery.getSql(this.orderByKey));
    }
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("DATE_FROM", adjustTime(dateRange.getFromDate()));
    query.setParameter("DATE_TO", adjustTime(dateRange.getToDate()));
    for (String key : this.params.keySet()) {
      query.setParameter(key, this.params.get(key));
    }
    query.setForReadOnly(true);
    return query;
  }

  /**
   * Adjusts the given time depending on the configured date adjust parameter
   * 
   * @param dateTime
   * @return
   */
  private Timestamp adjustTime(Timestamp dateTime) {
    Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(dateTime.getTime());
    cal.add(Calendar.HOUR, this.dateAdjust);
    return new Timestamp(cal.getTimeInMillis());
  }

  /**
   * Adds a parameter to the report outside of the 3 handled by default
   * 
   * @param key
   * @param value
   */
  public void addParam(String key, Object value) {
    this.params.put(key, value);
  }

  /**
   * Configures the fields to use on the report. REPLACES all current fields
   * 
   * @param fields
   */
  public void configureFields(ReportField... fields) {
    this.fields.clear();
    this.fields.addAll(Arrays.asList(fields));
  }

  /**
   * Appends a field at the end of the report
   * 
   * @param field
   */
  public void addField(ReportField field) {
    this.fields.add(field);
  }

  public Trailer getTrailer() {
    return trailer;
  }

  public void setTrailer(Trailer trailer) {
    this.trailer = trailer;
  }

  public String getFilterKey() {
    return filterKey;
  }

  public void setFilterKey(String filterKey) {
    this.filterKey = filterKey;
  }

  public String getOrderByKey() {
    return orderByKey;
  }

  public void setOrderByKey(String orderByKey) {
    this.orderByKey = orderByKey;
  }

  public int getDateAdjust() {
    return dateAdjust;
  }

  /**
   * Sets the adjustment to be done on the given date/time in HOURS
   * 
   * @param dateAdjust
   */
  public void setDateAdjust(int dateAdjust) {
    this.dateAdjust = dateAdjust;
  }

}
