/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.reports.DBCSReportField;
import com.ibm.cio.cmr.request.util.reports.DateRangeContainer;
import com.ibm.cio.cmr.request.util.reports.DateTimeReportField;
import com.ibm.cio.cmr.request.util.reports.ReportField;
import com.ibm.cio.cmr.request.util.reports.ReportSpec;
import com.ibm.cmr.create.batch.util.reports.KscReportTrailer;

/**
 * Batch application to handle generating KSC reports
 * 
 * @author 136786PH1
 *
 */
public class KscReportsService extends BaseBatchService {

  private static final Logger LOG = Logger.getLogger(KscReportsService.class);

  private static final int JAPAN_DATE_ADJUST_FROM_GMT = 9;

  private static final String DAILY = "D";
  private static final String MONTHLY = "M";

  private Date referenceDate;

  /**
   * Mode of the batch. D - Daily, M - Monthly
   */
  private String mode = DAILY;

  private String timestamp;

  private File outputDir;

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    setTimestamp();

    String outDir = SystemParameters.getString("KSC.RPT.DIR");
    if (StringUtils.isBlank(outDir)) {
      outDir = "/ci/shared/data/jp/ksc";
    }
    this.outputDir = new File(outDir);

    DateRangeContainer dateRange = initDateRange(entityManager, this.mode);
    LOG.debug("Using date range " + dateRange.getFromDate() + " - " + dateRange.getToDate());
    generateReports(entityManager, dateRange, !MONTHLY.equals(this.mode));

    return true;
  }

  /**
   * Creates the reports for account, address, estab, and company:
   * <ul>
   * <li>LJ84.DS.KSCDIFF.ACCTD</li>
   * </ul>
   * 
   * @param entityManager
   * @throws IOException
   * @throws UnsupportedEncodingException
   */
  private void generateReports(EntityManager entityManager, DateRangeContainer dateRange, boolean daily)
      throws UnsupportedEncodingException, IOException {
    ReportSpec spec = configureAccountsReport(daily);
    LOG.debug("Generating " + (daily ? "daily" : "monthly") + " accounts report..");
    spec.generate(entityManager, dateRange, this.outputDir);

    spec = configureAddressReport(daily);
    LOG.debug("Generating " + (daily ? "daily" : "monthly") + " address report..");
    spec.generate(entityManager, dateRange, this.outputDir);

    spec = configureCompanyReport(daily);
    LOG.debug("Generating " + (daily ? "daily" : "monthly") + " company report..");
    spec.generate(entityManager, dateRange, this.outputDir);

    spec = configureEstablishmentReport(daily);
    LOG.debug("Generating " + (daily ? "daily" : "monthly") + " establishment report..");
    spec.generate(entityManager, dateRange, this.outputDir);

  }

  /**
   * Configures the ACCOUNT report spec into monthly or daily
   * 
   * @param daily
   * @return
   */
  private ReportSpec configureAccountsReport(boolean daily) {
    LOG.debug("Configuring daily accounts report..");
    ReportSpec spec = new ReportSpec("KSC.RPT.ACCT", "ABFAC" + (daily ? "D" : "M") + "1." + this.timestamp);
    spec.configureFields(new DateTimeReportField("DRXCN", 8, "yyyyMMdd"), new ReportField("RCUXA", 6), new ReportField("RESXA", 6),
        new DBCSReportField("UNCUX01", 62), new DBCSReportField("UNCUX02", 62), new ReportField("NCUXB", 22), new ReportField("CTXXA", 2),
        new ReportField("CLGXA", 1), new ReportField("REMXA", 6), new DateTimeReportField("DVOXA", 6, "yyMMdd"), new ReportField("RLCXB", 5),
        new ReportField("UBOCD", 3), new ReportField("CIYXA", 5), new ReportField("CCRXA", 2), new ReportField("UGPID", 1),
        new ReportField("CPCAE", 1), new ReportField("Blank", 1), new ReportField("DBYAA", 2), new ReportField("Blank", 2),
        new ReportField("DMAXA", 8), new ReportField("UTGCD", 2));
    if (daily) {
      spec.setFilterKey("KSC.RPT.ACCT.DAILY");
    }
    spec.setOrderByKey("KSC.RPT.ACCT.ORDER");
    spec.setTrailer(new KscReportTrailer());
    spec.setDateAdjust(JAPAN_DATE_ADJUST_FROM_GMT);
    return spec;
  }

  /**
   * Configures the ADDRESS report spec into monthly or daily
   * 
   * @param entityManager
   * @throws IOException
   * @throws UnsupportedEncodingException
   */
  private ReportSpec configureAddressReport(boolean daily) throws UnsupportedEncodingException, IOException {
    LOG.debug("Configuring daily accounts report..");
    ReportSpec spec = new ReportSpec("KSC.RPT.ADDR", "ABFAD" + (daily ? "D" : "M") + "1." + this.timestamp);
    spec.configureFields(new DateTimeReportField("DRXCN", 8, "yyyyMMdd"), new ReportField("RASXA", 5), new ReportField("RCUXA", 6),
        new DBCSReportField("CZIPA", 8), new DBCSReportField("TXTBA01", 62), new DBCSReportField("TXTBA02", 62), new DBCSReportField("TXTBA03", 62),
        new DBCSReportField("TXTBA06", 62), new DBCSReportField("TXTBA04", 62), new DBCSReportField("TXTBA05", 62), new DBCSReportField("NRPAA", 32),
        new ReportField("RPHAS", 17), new ReportField("RFAX", 17), new ReportField("UADUX", 16));
    if (daily) {
      spec.setFilterKey("KSC.RPT.ADDR.DAILY");
    }
    spec.setOrderByKey("KSC.RPT.ADDR.ORDER");
    spec.setTrailer(new KscReportTrailer());
    spec.setDateAdjust(JAPAN_DATE_ADJUST_FROM_GMT);
    return spec;
  }

  /**
   * Configures the COMPANY report spec into monthly or daily
   * 
   * @param entityManager
   * @throws IOException
   * @throws UnsupportedEncodingException
   */
  private ReportSpec configureCompanyReport(boolean daily) throws UnsupportedEncodingException, IOException {
    LOG.debug("Configuring daily accounts report..");
    ReportSpec spec = new ReportSpec("KSC.RPT.COMP", "ABFCO" + (daily ? "D" : "M") + "1." + this.timestamp);
    spec.configureFields(new DateTimeReportField("DRXCN", 8, "yyyyMMdd"), new ReportField("RCOXA", 6), new ReportField("RCOCS", 9),
        new DBCSReportField("UNCUX01", 62), new DBCSReportField("UNCUX02", 62), new ReportField("NCUXB", 22), new ReportField("RLCXB", 5),
        new ReportField("CIYXA", 5));
    if (daily) {
      spec.setFilterKey("KSC.RPT.COMP.DAILY");
    }
    spec.setOrderByKey("KSC.RPT.COMP.ORDER");
    spec.setTrailer(new KscReportTrailer());
    spec.setDateAdjust(JAPAN_DATE_ADJUST_FROM_GMT);
    return spec;
  }

  /**
   * Configures the ESTABLISHMENT report spec into monthly or daily
   * 
   * @param entityManager
   * @throws IOException
   * @throws UnsupportedEncodingException
   */
  private ReportSpec configureEstablishmentReport(boolean daily) throws UnsupportedEncodingException, IOException {
    LOG.debug("Configuring daily accounts report..");
    ReportSpec spec = new ReportSpec("KSC.RPT.ESTAB", "ABFES" + (daily ? "D" : "M") + "1." + this.timestamp);
    spec.configureFields(new DateTimeReportField("DRXCN", 8, "yyyyMMdd"), new ReportField("RESXA", 6), new ReportField("RCOXA", 6),
        new DBCSReportField("UNCUX01", 62), new DBCSReportField("UNCUX02", 62), new ReportField("NCUXB", 22));
    if (daily) {
      spec.setFilterKey("KSC.RPT.ESTAB.DAILY");
    }
    spec.setOrderByKey("KSC.RPT.ESTAB.ORDER");
    spec.setTrailer(new KscReportTrailer());
    spec.setDateAdjust(JAPAN_DATE_ADJUST_FROM_GMT);
    return spec;
  }

  /**
   * Gets the date range to use for the reports
   * 
   * @param entityManager
   * @param mode
   * @return
   */
  private DateRangeContainer initDateRange(EntityManager entityManager, String mode) {
    if (this.referenceDate != null) {
      // directly use supplied date as reference

      // add 9 hours for JP time
      Timestamp from = adjustTime(this.referenceDate, 9);

      // add 9 hours for JP time plus 24 hours for 1 day
      Timestamp to = adjustTime(this.referenceDate, 33);

      return new DateRangeContainer(from, to);
    } else {
      // connect to DB and use current dates as reference
      String sqlKey = "KSC.INIT.DAILY";
      if (MONTHLY.equals(this.mode)) {
        sqlKey = "KSC.INIT.MONTHLY";
      }
      PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql(sqlKey));
      Object[] result = query.getSingleResult();
      DateRangeContainer dateRange = new DateRangeContainer((Timestamp) result[0], (Timestamp) result[1]);
      return dateRange;
    }

  }

  /**
   * Adjusts the Date into a timestamp
   * 
   * @param date
   * @param adjustHours
   * @return
   */
  private Timestamp adjustTime(Date date, int adjustHours) {
    Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(date.getTime());
    cal.add(Calendar.HOUR, adjustHours);
    return new Timestamp(cal.getTimeInMillis());
  }

  /**
   * Sets the timestamp to use for report use
   */
  private void setTimestamp() {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    this.timestamp = format.format(SystemUtil.getActualTimestamp());
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public Date getReferenceDate() {
    return referenceDate;
  }

  public void setReferenceDate(Date referenceDate) {
    this.referenceDate = referenceDate;
  }

}
