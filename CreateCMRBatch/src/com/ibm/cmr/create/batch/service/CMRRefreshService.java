/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.IndexUpdateClient;
import com.ibm.cmr.services.client.index.IndexUpdateRequest;
import com.ibm.cmr.services.client.index.IndexUpdateResponse;

/**
 * Handles moving newly created or updated CMRs in RDC to FindCMR
 * 
 * @author JeffZAMORA
 *
 */
public class CMRRefreshService extends BaseBatchService {

  private static final Logger LOG = Logger.getLogger(CMRRefreshService.class);
  private static final SimpleDateFormat LOG_FORMATTER = new SimpleDateFormat("yyyyMMddHHmm");
  private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private String country;
  private int hours;

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    LOG.debug("Starting CMR Refresh service..");

    // NOTE: the SQLs are hardcoded here to contain the changes to this batch
    // only

    String mandt = SystemConfiguration.getValue("MANDT", "100");
    String logDir = SystemParameters.getString("CMR.REFRESH.LOG");
    LOG.debug("Log Directory: " + logDir);
    boolean useLogDir = !StringUtils.isBlank(logDir) && new File(logDir).exists() && new File(logDir).isDirectory();
    LOG.debug("Use Logging? " + useLogDir);

    // String sql = "select min(max(SHAD_UPDATE_TS), current timestamp) from
    // SAPR3.KNA1 where MANDT = :MANDT";
    String sql = "select current timestamp from SYSIBM.SYSDUMMY1";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", mandt);
    query.setForReadOnly(true);
    Timestamp ts = query.getSingleResult(Timestamp.class);

    LOG.debug("Timestamp retrieved from DB: " + ts);
    Calendar cal = new GregorianCalendar();
    cal.setTime(ts);
    int changeParam = SystemParameters.getInt("CMR.REFRESH.HOURS");
    int hours = changeParam > 0 ? changeParam : 1;
    if (this.hours > 0) {
      hours = this.hours;
    }
    LOG.debug("Getting records updated up to " + hours + " hour(s) earlier..");
    cal.add(Calendar.HOUR, hours * -1);
    cal.add(Calendar.MINUTE, -15);
    LOG.debug("Timestamp to use: " + cal.getTime());

    LOG.debug("Getting CMR Nos. updated within the given timeframe..");
    sql = "select distinct k.KATR6, k.ZZKV_CUSNO ";
    sql += "from SAPR3.KNA1 k ";
    sql += "left outer join SAPR3.KNA1EXT x ";
    sql += "  on k.MANDT = x.MANDT ";
    sql += "  and k.KUNNR = x.KUNNR ";
    sql += "left outer join SAPR3.BUYING_GROUP_EXT bg ";
    sql += "  on k.MANDT = bg.MANDT ";
    sql += "  and k.KUNNR = bg.KUNNR ";
    sql += "left outer join SAPR3.KDUNS_NEW duns ";
    sql += "  on k.MANDT = duns.MANDT ";
    sql += "  and k.KUNNR = duns.KUNNR ";
    sql += "where k.MANDT = :MANDT ";
    sql += "and k.LOEVM <> 'X' ";
    sql += "and k.KATR10 <> 'GTS' ";
    sql += "and ( ";
    sql += "  k.SHAD_UPDATE_TS > :TS or ";
    sql += "  x.CHG_TS > :TS or ";
    sql += " x.CREATE_DATE > :DT or ";
    sql += "  bg.UPDATE_TS > :TS or ";
    sql += "  bg.CREATE_TS >  :TS  or ";
    sql += "  duns.UPDATE_TS >  :TS or ";
    sql += "  duns.CREATE_TS >  :TS ";
    sql += ") ";
    if (this.country != null) {
      sql += " and k.KATR6 = :KATR6";
    }

    String hrBeforeDate = TIME_FORMATTER.format(cal.getTime());
    Timestamp hrBeforeTime = new Timestamp(cal.getTimeInMillis());
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", this.country);
    query.setParameter("TS", hrBeforeTime);
    query.setParameter("DT", hrBeforeDate);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();

    int processed = 0;
    int error = 0;

    int maxRecords = SystemParameters.getInt("CMR.REFRESH.MAX");
    if (maxRecords <= 0) {
      maxRecords = 10000;
    }

    if (results != null && results.size() > maxRecords) {
      LOG.warn("More than " + maxRecords + " records retrieved. Processing will be skipped.");
      return true;
    }

    if (results != null && !results.isEmpty()) {
      LOG.info("Processing " + results.size() + " distinct CMR(s)..");

      IndexUpdateClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"),
          IndexUpdateClient.class);

      try (OutputStream fos = useLogDir ? new FileOutputStream(logDir + File.separator + "RefreshLog-" + LOG_FORMATTER.format(ts) + ".txt", true)
          : new EmptyOutputStream()) {

        try (PrintWriter pw = new PrintWriter(fos)) {
          for (Object[] record : results) {
            IndexUpdateRequest request = new IndexUpdateRequest();
            request.setMandt(mandt);
            request.setKatr6((String) record[0]);
            request.setCmrNo((String) record[1]);
            request.setUpdate(true);

            try {
              IndexUpdateResponse response = client.executeAndWrap(IndexUpdateClient.BASIC_APP_ID, request, IndexUpdateResponse.class);
              if (response.isSuccess()) {
                LOG.trace("CMR No. " + record[1] + " under " + record[0] + " updated.");
                pw.println(record[0] + "\t" + record[1] + "\tSuccess");
                processed++;
              } else {
                LOG.warn("CMR No. " + record[1] + " under " + record[0] + " not updated successfully. " + response.getMsg());
                pw.println(record[0] + "\t" + record[1] + "\tError:" + response.getMsg());
                error++;
              }
            } catch (Exception e) {
              LOG.warn("CMR No. " + record[1] + " under " + record[0] + " not updated successfully. " + e.getMessage());
              pw.println(record[0] + "\t" + record[1] + "\tError:" + e.getMessage());
              error++;
              if (LOG.isTraceEnabled()) {
                LOG.error(e);
              }
            }
          }
        }
      }

    } else {
      LOG.info("No records updated from " + cal.getTime());
    }

    LOG.info("Total CMRs updated on the index: " + processed);
    LOG.info("Total CMRs with errors: " + error);
    return true;
  }

  private class EmptyOutputStream extends OutputStream {

    @Override
    public void write(int b) throws IOException {
    }

  }

  @Override
  protected boolean terminateOnLongExecution() {
    return false;
  }

  @Override
  protected boolean isTransactional() {
    return false;
  }

  @Override
  protected boolean useServicesConnections() {
    return true;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public int getHours() {
    return hours;
  }

  public void setHours(int hours) {
    this.hours = hours;
  }

}
