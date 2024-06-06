/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.SystParameters;
import com.ibm.cio.cmr.request.entity.SystParametersPK;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.reports.DBCSContactField;
import com.ibm.cio.cmr.request.util.reports.DBCSReportField;
import com.ibm.cio.cmr.request.util.reports.DateRangeContainer;
import com.ibm.cio.cmr.request.util.reports.DateTimeReportField;
import com.ibm.cio.cmr.request.util.reports.PostalCodeField;
import com.ibm.cio.cmr.request.util.reports.ReportField;
import com.ibm.cio.cmr.request.util.reports.ReportSpec;
import com.ibm.cmr.create.batch.util.CMRFTPConnection;
import com.ibm.cmr.create.batch.util.reports.KscReportTrailer;

/**
 * Batch application to handle generating KSC reports
 * 
 * @author 136786PH1
 *
 */
public class KscReportsService extends BaseBatchService {

  private static final Logger LOG = Logger.getLogger(KscReportsService.class);

  private static final int JAPAN_DATE_ADJUST_FROM_GMT = -9;

  private static final String DAILY = "D";
  private static final String MONTHLY = "M";

  private Date referenceDate;

  /**
   * Mode of the batch. D - Daily, M - Monthly
   */
  private String mode = DAILY;
  private boolean manualExecution = false;

  private static final String DEFAULT_OUTPUT_DIR = "/ci/shared/data/jp/ksc";
  private static final String DEFAULT_OUTPUT_DIR_EXTERNAL = "/ci/shared/data/external/ksc";

  private String timestampString;
  private Timestamp currTimestamp;
  private Timestamp serverTimestamp;

  private File outputDir;
  private boolean useExternalFtp = false;

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {

    this.useExternalFtp = "EXTERNAL".equals(SystemParameters.getString("SFTP.KSC"));
    if (this.useExternalFtp) {
      LOG.info("Running in EXTERNAL mode");
    }

    setTimestamp();

    String outDir = SystemParameters.getString("KSC.RPT.DIR");
    if (StringUtils.isBlank(outDir)) {
      outDir = DEFAULT_OUTPUT_DIR;
    }
    this.outputDir = new File(outDir);
    if (!this.outputDir.exists() || !this.outputDir.isDirectory()) {
      LOG.warn("Output directory " + this.outputDir.getAbsolutePath() + " is not a valid directory. Setting to " + DEFAULT_OUTPUT_DIR);
      this.outputDir = new File(DEFAULT_OUTPUT_DIR);
    }

    cleanup(this.outputDir);

    if (!isManualExecution()) {
      if (!setModeAndGenerate(entityManager)) {
        LOG.info("KSC Reports generation skipped for today.");
        return true;
      }
    } else {
      LOG.info("Manual mode parameter supplied. Forcing to run");
    }

    DateRangeContainer dateRange = initDateRange(entityManager, this.mode);
    LOG.debug("Using date range " + dateRange.getFromDate() + " - " + dateRange.getToDate());
    generateReports(entityManager, dateRange, !MONTHLY.equals(this.mode));

    if (this.useExternalFtp) {
      copyFilesToExternalSFTP(this.outputDir.getAbsolutePath());
    }
    return true;
  }

  /**
   * Cleanup the old files before generating new ones
   */
  private void cleanup(File outputDir) {

    LOG.debug("Cleaning up old files...");
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    List<File> toDelete = new ArrayList<File>();
    for (File file : outputDir.listFiles()) {
      String fileDate = file.getName().substring(file.getName().lastIndexOf(".") + 1);
      try {
        if (fileDate.toLowerCase().contains("receive")) {
          toDelete.add(file);
        } else {
          format.parse(fileDate);
          toDelete.add(file);
        }
      } catch (Exception e) {
        // unparseable
        LOG.warn("File " + file + " not in the correct format. Cannot cleanup.");
      }
    }
    for (File file : toDelete) {
      LOG.debug("Removing file " + file.getName());
      file.delete();
    }
    if (this.useExternalFtp) {
      cleanupExternalFiles();
    }
  }

  /**
   * Deletes the files from the external system
   */
  private void cleanupExternalFiles() {
    String extDirectory = SystemParameters.getString("KSC.INPUT.DIR.EXT");
    if (StringUtils.isBlank(extDirectory)) {
      extDirectory = DEFAULT_OUTPUT_DIR_EXTERNAL;
    }
    try {
      try (CMRFTPConnection sftp = new CMRFTPConnection()) {
        LOG.debug("Checking files under " + extDirectory);
        for (String filename : sftp.listFiles(extDirectory)) {
          LOG.debug("Deleting file " + filename);
          sftp.deleteFile(extDirectory + "/" + filename);
        }
      }
    } catch (Exception e) {
      LOG.error("An error occurred when deleting files from external SFTP", e);
      throw new RuntimeException("Cannot copy from external SFTP.");
    }
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

    int currentCycle = 1;
    String currentCyleString = SystemParameters.getString("KSC.CYCLE");
    if (!StringUtils.isBlank(currentCyleString) && StringUtils.isNumeric(currentCyleString)) {
      currentCycle = Integer.parseInt(currentCyleString);
    }
    LOG.info("Last Cycle: " + currentCycle);
    currentCycle++;

    ReportSpec spec = configureAccountsReport(daily);
    LOG.debug("Generating " + (daily ? "daily" : "monthly") + " accounts report..");
    spec.generate(entityManager, dateRange, this.outputDir, currentCycle + "");

    spec = configureAddressReport(daily);
    LOG.debug("Generating " + (daily ? "daily" : "monthly") + " address report..");
    spec.generate(entityManager, dateRange, this.outputDir, currentCycle + "");

    spec = configureCompanyReport(daily);
    LOG.debug("Generating " + (daily ? "daily" : "monthly") + " company report..");
    spec.generate(entityManager, dateRange, this.outputDir, currentCycle + "");

    spec = configureEstablishmentReport(daily);
    LOG.debug("Generating " + (daily ? "daily" : "monthly") + " establishment report..");
    spec.generate(entityManager, dateRange, this.outputDir, currentCycle + "");

    incrementCycle(entityManager, currentCycle);
  }

  /**
   * Configures the ACCOUNT report spec into monthly or daily
   * 
   * @param daily
   * @return
   */
  private ReportSpec configureAccountsReport(boolean daily) {
    LOG.debug("Configuring " + (daily ? "daily" : "monthly") + " accounts report..");
    ReportSpec spec = new ReportSpec("KSC.RPT.ACCT", "ABFAC" + (daily ? "D" : "M") + "1." + this.timestampString);
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
    LOG.debug("Configuring " + (daily ? "daily" : "monthly") + " address report..");
    ReportSpec spec = new ReportSpec("KSC.RPT.ADDR", "ABFAD" + (daily ? "D" : "M") + "1." + this.timestampString);
    spec.configureFields(new DateTimeReportField("DRXCN", 8, "yyyyMMdd"), new ReportField("RASXA", 5), new ReportField("RCUXA", 6),
        new PostalCodeField("CZIPA", 8), new DBCSReportField("TXTBA01", 62), new DBCSReportField("TXTBA02", 62), new DBCSReportField("TXTBA03", 62),
        new DBCSReportField("TXTBA06", 62), new DBCSReportField("TXTBA04", 62), new DBCSReportField("TXTBA05", 62), new DBCSContactField("NRPAA", 32),
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
    LOG.debug("Configuring " + (daily ? "daily" : "monthly") + " company report..");
    ReportSpec spec = new ReportSpec("KSC.RPT.COMP", "ABFCO" + (daily ? "D" : "M") + "1." + this.timestampString);
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
    LOG.debug("Configuring " + (daily ? "daily" : "monthly") + " establishment report..");
    ReportSpec spec = new ReportSpec("KSC.RPT.ESTAB", "ABFES" + (daily ? "D" : "M") + "1." + this.timestampString);
    spec.configureFields(new DateTimeReportField("DRXCN", 8, "yyyyMMdd"), new ReportField("RESXA", 6), new ReportField("RCOXA", 6),
        new DBCSReportField("UNCUX01", 62), new DBCSReportField("UNCUX02", 62), new ReportField("NCUXB", 22));
    if (daily) {
      spec.setFilterKey("KSC.RPT.ESTAB.DAILY");
    }
    spec.setOrderByKey("KSC.RPT.ESTAB.ORDER");
    spec.setTrailer(new KscReportTrailer());
    spec.setDateAdjust(JAPAN_DATE_ADJUST_FROM_GMT);
    spec.setLimitDuplicateKeys(true);
    spec.setKeyField("RESXA");
    return spec;
  }

  /**
   * Copies all the generated files from local to external SFTP
   * 
   * @param outputDir
   */
  private void copyFilesToExternalSFTP(String outputDir) {
    LOG.debug("Uploading generated files to external SFTP..");
    String extDirectory = SystemParameters.getString("KSC.INPUT.DIR.EXT");
    if (StringUtils.isBlank(extDirectory)) {
      extDirectory = DEFAULT_OUTPUT_DIR_EXTERNAL;
    }
    try {
      try (CMRFTPConnection sftp = new CMRFTPConnection()) {
        for (File file : new File(outputDir).listFiles()) {
          sftp.putFile(file.getAbsolutePath(), extDirectory + "/" + file.getName());
        }
      }
    } catch (Exception e) {
      LOG.error("An error occurred when copying files from external SFTP", e);
      throw new RuntimeException("Cannot copy from external SFTP.");
    }

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

      // minus 9 hours for JP time
      Timestamp from = new Timestamp(this.referenceDate.getTime());

      // to date is current timestamp, to cater for longer days
      Timestamp to = this.currTimestamp;

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
    this.currTimestamp = SystemUtil.getActualTimestamp();
    this.serverTimestamp = new Timestamp(this.currTimestamp.getTime());
    this.currTimestamp = adjustTime(this.currTimestamp, +9);
    this.timestampString = format.format(this.currTimestamp);
  }

  private void incrementCycle(EntityManager entityManager, int newCycle) {
    SystParametersPK pk = new SystParametersPK();
    pk.setParameterCd("KSC.CYCLE");
    SystParameters param = entityManager.find(SystParameters.class, pk);
    if (param == null) {
      param = new SystParameters();
      param.setId(pk);
    }
    LOG.info("Incrementing KSC Cycle to " + newCycle);
    param.setParameterValue(newCycle + "");
    entityManager.merge(param);
    entityManager.flush();
  }

  private boolean setModeAndGenerate(EntityManager entityManager) {
    boolean generate = true;
    Calendar curr = new GregorianCalendar();
    curr.setTime(this.currTimestamp);
    LOG.info("Current Server Time: " + this.serverTimestamp);
    LOG.info("Current Japan Time: " + new Timestamp(curr.getTimeInMillis()) + ", Date: " + curr.get(Calendar.DATE) + ", Day: "
        + curr.get(Calendar.DAY_OF_WEEK));

    if (curr.get(Calendar.DATE) == 1) {
      this.mode = MONTHLY;
      LOG.info("Setting mode to MONTHLY (first day of month)");
    } else {
      if (curr.get(Calendar.DAY_OF_WEEK) < 3 && curr.get(Calendar.DAY_OF_WEEK) > 0) {
        LOG.info("Skipping report generation for day " + curr.get(Calendar.DAY_OF_WEEK));
        generate = false;
      } else {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String currentJpDate = formatter.format(curr.getTime());
        LOG.debug("Checking holiday for " + currentJpDate);
        String holidayCheck = "select 1 from creqcmr.lov where field_id = '##JPHolidays' and cmr_issuing_cntry = '760' and cd = :HOLIDAY";
        PreparedQuery query = new PreparedQuery(entityManager, holidayCheck);
        query.setParameter("HOLIDAY", currentJpDate);
        query.setForReadOnly(true);
        boolean holiday = query.exists();
        LOG.debug("Date is " + (holiday ? "" : "NOT ") + "a holiday.");
        if (holiday) {
          LOG.info("Date " + currentJpDate + " is a holiday. Skipping report generation.");
          generate = false;
        } else {
          LOG.info("Setting mode to DAILY");
          this.mode = DAILY;

        }
      }
    }
    return generate;
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

  public boolean isManualExecution() {
    return manualExecution;
  }

  public void setManualExecution(boolean manualExecution) {
    this.manualExecution = manualExecution;
  }

}
