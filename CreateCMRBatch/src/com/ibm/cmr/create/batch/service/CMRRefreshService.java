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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
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

    String sql = "select min(max(SHAD_UPDATE_TS), current timestamp) from SAPR3.KNA1 where MANDT = :MANDT";
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
    LOG.debug("Getting records updated up to " + hours + " hours earlier..");
    cal.add(Calendar.HOUR, hours * -1);
    cal.add(Calendar.MINUTE, -15);
    LOG.debug("Timestamp to use: " + cal.getTime());

    LOG.debug("Getting CMR Nos. updated within the given timeframe..");
    sql = "select KATR6, ZZKV_CUSNO, KUNNR from SAPR3.KNA1 where MANDT = :MANDT and LOEVM <> 'X' and SHAD_UPDATE_TS >= :TS";
    if (this.country != null) {
      sql += " and KATR6 = :KATR6";
    }
    sql += " order by KATR6, ZZKV_CUSNO, KUNNR";
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", this.country);
    query.setParameter("TS", cal.getTime());
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();

    int processed = 0;
    int error = 0;

    if (results != null && !results.isEmpty()) {
      LOG.info("Processing " + results.size() + " distinct CMRs..");

      IndexUpdateClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"),
          IndexUpdateClient.class);

      try (OutputStream fos = useLogDir
          ? new FileOutputStream(logDir + File.separator + "RefreshLog-" + LOG_FORMATTER.format(new Date()) + ".txt", true)
          : new EmptyOutputStream()) {

        try (PrintWriter pw = new PrintWriter(fos)) {
          for (Object[] record : results) {
            IndexUpdateRequest request = new IndexUpdateRequest();
            request.setMandt(mandt);
            request.setKatr6((String) record[0]);
            request.setCmrNo((String) record[1]);
            request.setKunnr((String) record[2]);
            request.setUpdate(true);

            try {
              IndexUpdateResponse response = client.executeAndWrap(IndexUpdateClient.BASIC_APP_ID, request, IndexUpdateResponse.class);
              if (response.isSuccess()) {
                LOG.trace("CMR No. " + record[1] + " under " + record[1] + " updated.");
                pw.println(record[0] + "\t" + record[1] + "\t" + record[2] + "\tSuccess");
                processed++;
              } else {
                LOG.warn("CMR No. " + record[1] + " under " + record[1] + " not updated successfully. " + response.getMsg());
                pw.println(record[0] + "\t" + record[1] + "\t" + record[2] + "\tError:" + response.getMsg());
                error++;
              }
            } catch (Exception e) {
              LOG.warn("CMR No. " + record[1] + " under " + record[1] + " not updated successfully. " + e.getMessage());
              pw.println(record[0] + "\t" + record[1] + "\t" + record[2] + "\tError:" + e.getMessage());
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
