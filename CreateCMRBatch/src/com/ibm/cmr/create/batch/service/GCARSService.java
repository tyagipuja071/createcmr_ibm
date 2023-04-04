/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Changelog;
import com.ibm.cio.cmr.request.entity.ChangelogPK;
import com.ibm.cio.cmr.request.entity.GCARSUpdtQueue;
import com.ibm.cio.cmr.request.entity.GCARSUpdtQueuePK;
import com.ibm.cio.cmr.request.entity.Kna1AddlBilling;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * for GCARS process
 * 
 */
public class GCARSService extends MultiThreadedBatchService<GCARSUpdtQueue> {

  private static final Logger LOG = Logger.getLogger(GCARSService.class);
  private static final String LOCAL_DIR = "/ci/shared/data/gcars/";
  private static final String ARCHIVE_DIR = "/ci/shared/data/gcarsarchive/";
  private static final String ERROR_DIR = "/ci/shared/data/gcarserror/";
  private static final String REMOTE_DIR = "/is/isdata/cmr_partners/gcarsBR/IBMgcars/";
  private static final String REMOTE_ARCHIVE_DIR = "/is/isdata/cmr_partners/gcarsBR/IBMgcarsarchive/";

  private static final String REMOTE_HOST = System.getProperty("GCARS_FTP_HOST");
  private static final String USERNAME = System.getProperty("GCARS_FTP_USER");
  private static final String PASSWORD = System.getProperty("GCARS_FTP_PASS");
  private static final String KNOWN_HOSTS = System.getProperty("GCARS_FTP_KNOWN_HOSTS");
  private static final String GCARS_FILE = "bbcro.z010.gcars.inbound.txt";
  private static final int REMOTE_PORT = 22;
  private static final int SESSION_TIMEOUT = 10000;
  private static final int CHANNEL_TIMEOUT = 5000;

  private Map<String, Integer> fileSizes = new HashMap<String, Integer>();
  private Map<String, Integer> processedSizes = new HashMap<String, Integer>();

  public static final String STATUS_PENDING = "P";
  public static final String STATUS_ERROR = "E";
  public static final String STATUS_ABORTED = "A";
  public static final String STATUS_COMPLETED = "C";
  public static final String STATUS_NOT_REQUIRED = "N";
  public static final String GCARS_USER = "GCARS-CCMR";
  private Mode mode = Mode.Extract;

  public static enum Mode {
    Extract, Update, Download
  };

  @Override
  public Queue<GCARSUpdtQueue> getRequestsToProcess(EntityManager entityManager) {
    switch (this.mode) {
    case Extract:
      return extractFromFiles(entityManager);
    case Update:
      return getPendingRecords(entityManager);
    case Download:
      return copyFromRDCServer(entityManager);
    default:
      // nothing here
      return new LinkedList<>();
    }
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<GCARSUpdtQueue> list) throws Exception {
    switch (this.mode) {
    case Extract:
      insertQueueRecords(entityManager, list);
      break;
    case Update:
      updateKna1AddlBillingRecords(entityManager, list);
      break;
    default:
      LOG.debug("No mode specified.");
      break;
    }
    return true;
  }

  /**
   * Reads files and extracts the data into a series of {@link GCARSUpdtQueue}
   * objects
   * 
   * @param entityManager
   * @return
   */
  protected Queue<GCARSUpdtQueue> extractFromFiles(EntityManager entityManager) {
    String directory = SystemConfiguration.getValue("GCARS_LOCAL_DIR");
    if (StringUtils.isBlank(directory)) {
      directory = LOCAL_DIR;
    }
    Queue<GCARSUpdtQueue> queue = new LinkedList<GCARSUpdtQueue>();
    LOG.debug("GCARS Input Directory: " + directory);

    List<String> nameList = getFileNames(directory);
    if (nameList.size() > 0) {
      for (String filename : nameList) {
        List<GCARSUpdtQueue> list = parseGCARSFile(directory + filename, filename, entityManager);
        if (list != null) {
          LOG.debug(list.size() + " records added from file " + filename);
          this.fileSizes.putIfAbsent(filename, list.size());
          this.processedSizes.putIfAbsent(filename, 0);
          queue.addAll(list);
        }
      }
    }
    if (queue.isEmpty()) {
      LOG.debug("No GCARS files found at this moment.");
    } else {
      LOG.debug("Gathered " + queue.size() + " records from GCARS files.");
    }
    return queue;
  }

  protected Queue<GCARSUpdtQueue> copyFromRDCServer(EntityManager entityManager) {
    LOG.debug("GCARS copyFromRDCServer");
    Queue<GCARSUpdtQueue> queue = new LinkedList<GCARSUpdtQueue>();

    String localDir = SystemConfiguration.getValue("GCARS_LOCAL_DIR");
    if (StringUtils.isBlank(localDir)) {
      localDir = LOCAL_DIR;
    }
    LOG.debug("GCARS local directory " + localDir);

    String remoteDir = SystemConfiguration.getValue("GCARS_REMOTE_DIR");
    if (StringUtils.isBlank(remoteDir)) {
      remoteDir = REMOTE_DIR;
    }
    LOG.debug("GCARS remote directory " + remoteDir);

    String localFile = localDir + GCARS_FILE;
    String remoteFile = remoteDir + GCARS_FILE;
    Session jschSession = null;
    try {

      JSch jsch = new JSch();
      LOG.debug("Local file: " + localFile);
      LOG.debug("Remote file: " + remoteFile);
      LOG.debug("Known Hosts File: " + KNOWN_HOSTS);
      LOG.debug("Connecting to FTP Server " + REMOTE_HOST + ":" + REMOTE_PORT + " using " + USERNAME);
      jsch.setKnownHosts(KNOWN_HOSTS);
      jschSession = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT);

      jschSession.setPassword(PASSWORD);
      jschSession.connect(SESSION_TIMEOUT);

      Channel sftp = jschSession.openChannel("sftp");
      sftp.connect(CHANNEL_TIMEOUT);

      String archiveDir = SystemConfiguration.getValue("GCARS_REMOTE_ARCHIVE_DIR");
      if (StringUtils.isBlank(archiveDir)) {
        archiveDir = REMOTE_ARCHIVE_DIR;
      }

      ChannelSftp channelSftp = (ChannelSftp) sftp;

      channelSftp.get(remoteFile, localFile);

      SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy-HH-mm");
      Timestamp ts = SystemUtil.getActualTimestamp();
      String dateStr = formatter.format(ts);
      String fileSubstring = GCARS_FILE.replace(".txt", "-" + dateStr + ".txt");
      LOG.debug(" - Moving file to remote archive dir: " + remoteFile + dateStr);

      channelSftp.rename(remoteFile, archiveDir + fileSubstring);

      channelSftp.exit();

    } catch (JSchException | SftpException e) {
      LOG.warn("An error has occurred when trying to download files from FTP server", e);
    } catch (Exception ex) {
      LOG.debug("Error encountered in GCARS Download" + ex.getMessage());
    } finally {
      if (jschSession != null) {
        jschSession.disconnect();
      }
    }
    return queue;
  }

  @SuppressWarnings("unused")
  private List<String> getRDCFileNames(String path) {
    LOG.debug("GCARS getFileNames from " + path);
    List<String> nameList = new ArrayList<String>();
    File f = new File(path);
    File fileList[] = f.listFiles();
    for (File file : fileList) {
      if (file.isDirectory()) {
        continue;
      } else {
        LOG.info("GCARS File " + file.getName() + " is added for file transfer.");
        nameList.add(file.getName());
      }
    }
    return nameList;
  }

  /**
   * Gets the list of filenames under the path
   * 
   * @param path
   * @return
   */
  private List<String> getFileNames(String path) {
    LOG.debug("GCARS getFileNames from " + path);
    List<String> nameList = new ArrayList<String>();
    File f = new File(path);
    File fileList[] = f.listFiles();
    for (File file : fileList) {
      if (file.isDirectory()) {
        continue;
      } else {
        LOG.info("GCARS File " + file.getName() + " is loaded for processing.");
        nameList.add(file.getName());
      }
    }
    return nameList;
  }

  /**
   * Parses the GCARS file and creates a list of {@link GCARSUpdtQueue} records
   * 
   * @param gcarsFile
   * @param fileName
   * @param entityManager
   * @return
   */
  @SuppressWarnings("deprecation")
  private List<GCARSUpdtQueue> parseGCARSFile(String gcarsFile, String fileName, EntityManager entityManager) {
    LOG.debug("Parsing File: " + fileName);

    String directory = SystemConfiguration.getValue("GCARS_LOCAL_DIR");
    if (StringUtils.isBlank(directory)) {
      directory = LOCAL_DIR;
    }

    List<GCARSUpdtQueue> gcarsList = new ArrayList<GCARSUpdtQueue>();
    if (gcarsFile == null || gcarsFile.equalsIgnoreCase("")) {
      LOG.error("ERROR: INVALID GCARS File!");
      return null;
    }
    LineIterator it = null;
    try {
      it = FileUtils.lineIterator(new File(gcarsFile), "UTF-8");
      String sql = ExternalizedQuery.getSql("GCARS.GET_LAST_SEQ.QUEUE");
      LOG.debug("GCARS get last sequence from mass ftp queue");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      GCARSUpdtQueue queue = query.getSingleResult(GCARSUpdtQueue.class);
      String lastSeq = null;
      String lastSourceName = null;
      Long nextSeq = 0L;

      if (queue != null) {
        lastSourceName = queue.getId().getSourceName();
        lastSeq = lastSourceName.substring((lastSourceName.indexOf("-")) + 1);
      }
      if (!StringUtils.isBlank(lastSeq)) {
        nextSeq = Long.valueOf(lastSeq) + 1;
        LOG.debug("GCARS next sequence is " + nextSeq);
      } else {
        nextSeq = 1L;
      }
      int lineNumber = 1;

      while (it.hasNext()) {
        String line = it.nextLine();
        GCARSUpdtQueue gcars = new GCARSUpdtQueue();
        GCARSUpdtQueuePK gcarsPK = new GCARSUpdtQueuePK();
        gcarsPK.setSeqNo(lineNumber);

        Timestamp ts = SystemUtil.getActualTimestamp();

        if (line != null) {
          String countryCode = line.substring(0, 3);
          String cmrNum = line.substring(4, 12);
          String codCondition = line.substring(13, 14);
          String codReason = line.substring(15, 17);
          String codEffDate = line.substring(18, 23);
          String programName = line.substring(23, 31);

          String year = codEffDate.substring(0, 2);
          String month = codEffDate.substring(2, 4);

          year = Integer.parseInt(year) > 89 ? "19".concat(year) : "20".concat(year);

          gcarsPK.setCmrIssuingCntry(countryCode);
          gcarsPK.setCmrNo(cmrNum);
          if (StringUtils.isNotBlank(programName)) {
            gcarsPK.setSourceName(programName + "-" + nextSeq);
          }

          gcars.setFileName(fileName);
          gcars.setCodCondition(codCondition);
          gcars.setCodRsn(codReason);
          gcars.setCodEffDate(Date.valueOf(year + "-" + month + "-01"));
          gcars.setProcStatus(STATUS_PENDING);
          gcars.setProcMsg("Pending");
          gcars.setCreatedBy(GCARS_USER);
          gcars.setCreateDt(ts);
          gcars.setUpdatedBy(GCARS_USER);
          gcars.setId(gcarsPK);
          gcars.setUpdateDt(ts);
          gcars.setKatr10(""); // blank for IBM default
          gcarsList.add(gcars);
          lineNumber++;
        }
      }
    } catch (Exception e) {
      try {
        LOG.debug("Error in parsing file " + fileName, e);
        // Rename the file ERR-YYYY-MM-DD_filename
        // then alert CI OPs and GCARS Ops thru email
        File source = new File(directory + File.separator + fileName);
        File target = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Timestamp ts = SystemUtil.getActualTimestamp();
        String dateStr = formatter.format(ts);

        String error = SystemConfiguration.getValue("GCARS_ERROR_DIR");
        if (StringUtils.isBlank(error)) {
          error = ERROR_DIR;
        }

        // check number of retries it was processed, max is 3
        char firstChar = fileName.charAt(0);
        Boolean isDigit = Character.isDigit(firstChar);
        int retry = 0;

        if (isDigit) {
          retry = Character.getNumericValue(firstChar);
          retry += 1;
        }

        if (retry >= 3) {
          // move to error directory
          target = new File(error + File.separator + "3ERR-" + dateStr + "_" + fileName.substring(fileName.indexOf("b")));
        } else {
          if (retry >= 1) {
            target = new File(directory + File.separator + "2ERR-" + dateStr + "_" + fileName.substring(fileName.indexOf("b")));
          } else {
            target = new File(directory + File.separator + "1ERR-" + dateStr + "_" + fileName);
          }
        }

        FileUtils.copyFile(source, target);
        LOG.debug(" - Renaming file: " + source);
        LineIterator.closeQuietly(it);

        boolean renamed = FileUtils.deleteQuietly(source);
        LOG.debug(" - Renamed: " + renamed);

        if (retry == 3) {
          StringBuilder details = new StringBuilder();
          details.append("Hi, The batch interface from GCARS to CreateCMR via SFTP has failed, pls check " + target.getName());
          sendEmailNotification(fileName, details);
        }

      } catch (Exception ex) {
        LOG.debug("Error encountered in GCARS " + ex.getMessage());
      }
    } finally {
      LineIterator.closeQuietly(it);
    }
    return gcarsList;
  }

  /**
   * Gets the {@link GCARSUpdtQueue} records that are pending for processing
   * 
   * @param entityManager
   * @return
   */
  private Queue<GCARSUpdtQueue> getPendingRecords(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("GCARS.GET_PENDING.RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    LOG.debug("GCARS getPendingRecords from ftp queue");
    List<GCARSUpdtQueue> list = query.getResults(GCARSUpdtQueue.class);
    Queue<GCARSUpdtQueue> queue = new LinkedList<GCARSUpdtQueue>();
    if (list != null && !list.isEmpty()) {
      queue.addAll(list);
    }
    return queue;
  }

  /**
   * Inserts the prepared list of {@link GCARSUpdtQueue} records to the DB
   * 
   * @param entityManager
   * @param list
   */
  private void insertQueueRecords(EntityManager entityManager, List<GCARSUpdtQueue> list) {
    if (!list.isEmpty()) {
      LOG.info("There are " + list.size() + " records to process.");

      Timestamp ts = SystemUtil.getActualTimestamp();
      for (GCARSUpdtQueue queue : list) {
        queue.setCreateDt(ts);
        queue.setUpdateDt(ts);
        LOG.debug("Persisting " + queue.getId().getSourceName() + " - " + queue.getId().getCmrNo() + " - " + queue.getId().getSeqNo());
        entityManager.persist(queue);
        entityManager.flush();

        synchronized (this) {
          this.processedSizes.put(queue.getFileName(), this.processedSizes.get(queue.getFileName()) + 1);
        }
      }
      partialCommit(entityManager);
    }
  }

  /**
   * Updates {@link Kna1AddlBilling} and creates {@link Changelog} records
   * 
   * @param entityManager
   * @param list
   */
  private void updateKna1AddlBillingRecords(EntityManager entityManager, List<GCARSUpdtQueue> list) throws Exception {
    if (!list.isEmpty()) {
      for (GCARSUpdtQueue queue : list) {
        Timestamp ts = SystemUtil.getActualTimestamp();
        boolean needToUpdate = false;

        try {
          String sourceName = queue.getId().getSourceName() != null ? queue.getId().getSourceName() : "";
          String srcNm = !StringUtils.isBlank(sourceName) && sourceName.length() > 8 ? sourceName.substring(0, 8) : sourceName;

          LOG.debug("GCARS BR- Validate Source Name if XCARR08E");
          if (srcNm.equals("XCARR08E")) {
            LOG.debug("GCARS BR- Pull Kna1AddlBilling");
            String sql = ExternalizedQuery.getSql("GCARS.GET_RECORDS.RDC");
            PreparedQuery query = new PreparedQuery(entityManager, sql);
            query.setParameter("ZZKV_CUSNO", queue.getId().getCmrNo());
            query.setParameter("KATR6", queue.getId().getCmrIssuingCntry());
            query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
            Kna1AddlBilling record = query.getSingleResult(Kna1AddlBilling.class);

            if (record != null) {
              // Compare Kna1billing vs flat file
              String kna1Codcond = record.getCodCondition() != null ? record.getCodCondition() : "";
              String kna1Codreas = record.getCodReason() != null ? record.getCodReason() : "";
              String gcarsCodcond = queue.getCodCondition() != null ? queue.getCodCondition() : "";
              String gcarsCodreas = queue.getCodRsn() != null ? queue.getCodRsn() : "";

              if (!StringUtils.isBlank(gcarsCodcond)) {
                if (!gcarsCodcond.equals(kna1Codcond)) {
                  createChangeLog(entityManager, record, queue, ts, "AD_BILLING", "CODCOND", record.getCodCondition(), queue.getCodCondition());
                  record.setCodCondition(gcarsCodcond);
                  needToUpdate = true;
                }
              }

              if (!StringUtils.isBlank(gcarsCodreas)) {
                if (!gcarsCodreas.equals(kna1Codreas)) {
                  createChangeLog(entityManager, record, queue, ts, "AD_BILLING", "CODREAS", record.getCodReason(), queue.getCodRsn());
                  record.setCodReason(gcarsCodreas);
                  needToUpdate = true;
                }
              }

              if (needToUpdate) {
                record.setUpdatedBy(GCARS_USER);
                record.setUpdateDt(ts);
                updateEntity(record, entityManager);

                LOG.debug("GCARS BR- KUNNR " + record.getId().getKunnr() + " for CMR No. " + queue.getId().getCmrNo() + " done.");
                queue.setProcStatus(STATUS_COMPLETED);
                queue.setProcMsg("Successfully processed");
              } else {
                LOG.debug("GCARS BR- No Changes Required for CMR No. " + queue.getId().getCmrNo());
                queue.setProcStatus(STATUS_NOT_REQUIRED);
                queue.setProcMsg("Not Required");
              }
            } else {
              LOG.debug("GCARS BR- Records for CMR No. " + queue.getId().getCmrNo() + " not found.");
              queue.setProcStatus(STATUS_ERROR);
              queue.setProcMsg("Records for CMR No. " + queue.getId().getCmrNo() + " not found.");
            }
          } else {
            LOG.debug("GCARS BR- Invalid source name " + sourceName + " for CMR No. " + queue.getId().getCmrNo());
            queue.setProcStatus(STATUS_ERROR);
            queue.setProcMsg("Invalid source name " + sourceName + " for CMR No. " + queue.getId().getCmrNo());
          }

          queue.setUpdatedBy(GCARS_USER);
          queue.setUpdateDt(ts);
          updateEntity(queue, entityManager);

        } catch (Exception e) {
          LOG.debug("Error when processing queue record " + queue.getFileName() + "-" + queue.getId().getSeqNo(), e);
          queue.setProcMsg("Error in processing: " + e.getMessage());
          if (STATUS_ERROR.equals(queue.getProcStatus())) {
            queue.setProcStatus(STATUS_ABORTED);
            queue.setProcMsg("Aborted (2x error) - " + queue.getProcMsg());
          } else {
            queue.setProcStatus(STATUS_ERROR);
          }
          if (queue.getProcMsg() != null && queue.getProcMsg().length() > 500) {
            queue.setProcMsg(queue.getProcMsg().substring(0, 490) + "...");
          }
          updateEntity(queue, entityManager);
        }
        partialCommit(entityManager);
      }
    }
  }

  /**
   * Creates the {@link Changelog} record
   * 
   * @param table
   * @param field
   * @param oldValue
   * @param newValue
   */
  protected void createChangeLog(EntityManager entityManager, Kna1AddlBilling kna1AddlBilling, GCARSUpdtQueue queue, Timestamp ts, String table,
      String field, String oldValue, String newValue) {
    ChangelogPK pk = new ChangelogPK();
    pk.setChgts(ts);
    pk.setMandt(SystemConfiguration.getValue("MANDT"));
    pk.setKunnr(kna1AddlBilling.getId().getKunnr());
    pk.setTab(table);
    pk.setField(field);
    Changelog log = new Changelog();
    log.setId(pk);
    log.setActgrp("ZS01");
    log.setAction("U");
    log.setChgpnt("Y");
    log.setLinenumber(queue.getId().getSeqNo() + "");
    log.setLoadfilename(queue.getId().getSourceName());
    log.setOld(oldValue);
    log.setNewValue(newValue);
    log.setTabkey1(kna1AddlBilling.getId().getKunnr());
    log.setUserid(GCARS_USER);
    entityManager.persist(log);
  }

  @Override
  protected void cleanUp(EntityManager entityManager) {
    LOG.debug("Removing processed files from server..");
    String directory = SystemConfiguration.getValue("GCARS_LOCAL_DIR");
    if (StringUtils.isBlank(directory)) {
      directory = LOCAL_DIR;
    }
    String archive = SystemConfiguration.getValue("GCARS_ARCHIVE_DIR");
    if (StringUtils.isBlank(archive)) {
      archive = ARCHIVE_DIR;
    }

    for (String fileName : this.fileSizes.keySet()) {
      int expected = this.fileSizes.get(fileName);
      int processed = this.processedSizes.get(fileName);
      if (expected == processed && processed > 0) {
        // copy the file to archive first
        File source = new File(directory + File.separator + fileName);
        File target = new File(archive + File.separator + fileName);
        LOG.debug("File " + fileName + " processed fully. Archiving to " + target.getAbsolutePath());
        try {
          FileUtils.copyFile(source, target);
          LOG.debug(" - archived successfully.");
        } catch (IOException e) {
          LOG.warn("Cannot archive file " + source.getAbsolutePath());
        }
        LOG.debug("Removing file " + source.getAbsolutePath());
        boolean removed = FileUtils.deleteQuietly(source);
        LOG.debug(" - Deleted: " + removed);
      } else {
        LOG.debug("Number of records processed is " + processed + "/" + expected + " expected (" + fileName + ").");
      }
    }
    this.fileSizes.clear();
    this.processedSizes.clear();
  }

  private void sendEmailNotification(String fileName, StringBuilder details) {
    String gcarsCiOps = null;

    String host = SystemConfiguration.getValue("MAIL_HOST");
    String subject = "Brazil GCARS to CCMR SFTP Interface Error Report";
    String from = "GCARS_SFTP_Interface";
    String email = details.toString();

    try {
      gcarsCiOps = SystemParameters.getString("GCARS.NOTIF.GCARS_CI_OPS");
      if (StringUtils.isBlank(gcarsCiOps)) {
        gcarsCiOps = "barcelj@ph.ibm.com";
      }
    } catch (Exception e) {
      LOG.debug("Failed in getting emails (CI Ops/GCARS Ops)", e);
    }

    if (!StringUtils.isEmpty(gcarsCiOps) && !StringUtils.isEmpty(email)) {
      LOG.debug("Sending email notification to CI Ops/GCARS Ops as batch interface from GCARS to CreateCMR via SFTP has failed (" + fileName + ")");
      Email mail = new Email();
      mail.setSubject(subject);
      mail.setTo(gcarsCiOps);
      mail.setFrom(from);
      mail.setMessage(email);
      mail.setType(MessageType.HTML);
      mail.send(host);
    }
  }

  @Override
  protected boolean hasCleanup() {
    return true;
  }

  @Override
  protected String getThreadName() {
    return "GCARS-Get";
  }

  @Override
  public boolean isTransactional() {
    return true;
  }

  @Override
  protected boolean terminateOnLongExecution() {
    return false;
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }
}