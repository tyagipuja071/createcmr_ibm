/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
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

/**
 * for GCARS process
 * 
 */
public class GCARSService extends MultiThreadedBatchService<GCARSUpdtQueue> {

  private static final Logger LOG = Logger.getLogger(GCARSService.class);
  private static final String DEFAULT_DIR = "/ci/shared/data/gcars/";
  private static final String ARCHIVE_DIR = "/ci/shared/data/gcarsarchive/";
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
    Extract, Update
  };

  @Override
  public Queue<GCARSUpdtQueue> getRequestsToProcess(EntityManager entityManager) {
    switch (this.mode) {
    case Extract:
      return extractFromFiles(entityManager);
    case Update:
      return getPendingRecords(entityManager);
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
    String directory = SystemParameters.getString("GCARS.INPUT.DIR");
    if (StringUtils.isBlank(directory)) {
      directory = DEFAULT_DIR;
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

  /**
   * Gets the list of filenames under the path
   * 
   * @param path
   * @return
   */
  private List<String> getFileNames(String path) {
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

    List<GCARSUpdtQueue> gcarsList = new ArrayList<GCARSUpdtQueue>();
    if (gcarsFile == null || gcarsFile.equalsIgnoreCase("")) {
      LOG.error("ERROR: INVALID GCARS File!");
      return null;
    }
    LineIterator it = null;
    try {
      it = FileUtils.lineIterator(new File(gcarsFile), "UTF-8");
      String sql = ExternalizedQuery.getSql("GCARS.GET_LAST_SEQ.QUEUE");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      GCARSUpdtQueue queue = query.getSingleResult(GCARSUpdtQueue.class);
      String lastSeq = null;
      String lastSourceName = null;
      int nextSeq = 0;

      if (queue != null) {
        lastSourceName = queue.getId().getSourceName();
        lastSeq = lastSourceName.substring((lastSourceName.indexOf("-")) + 1);
      }
      if (!StringUtils.isBlank(lastSeq)) {
        nextSeq = Integer.valueOf(lastSeq) + 1;
      } else {
        nextSeq = 1;
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
          gcars.setProcMsg("Processing line number " + lineNumber + " of file " + fileName);
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
      LOG.debug("Error in parsing file " + fileName, e);
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
        queue.setUpdatedBy(GCARS_USER);
        queue.setUpdateDt(ts);
        try {
          String sql = ExternalizedQuery.getSql("GCARS.GET_RECORDS.RDC");
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("ZZKV_CUSNO", queue.getId().getCmrNo());
          query.setParameter("KATR6", queue.getId().getCmrIssuingCntry());
          query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          Kna1AddlBilling record = query.getSingleResult(Kna1AddlBilling.class);

          if (record != null) {
            // compare kna1billing vs flat file
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

              LOG.debug(" - KUNNR " + record.getId().getKunnr() + " for CMR No. " + queue.getId().getCmrNo() + " done.");

              queue.setProcStatus(STATUS_COMPLETED);
              queue.setProcMsg("Successfully processed");
            } else {
              queue.setProcStatus(STATUS_NOT_REQUIRED);
              queue.setProcMsg("Not Required");
            }

          } else {
            queue.setProcStatus(STATUS_ERROR);
            queue.setProcMsg("Records for CMR No. " + queue.getId().getCmrNo() + " not found.");
          }
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
    log.setLoadfilename(queue.getFileName());
    log.setOld(oldValue);
    log.setNewValue(newValue);
    log.setTabkey1(kna1AddlBilling.getId().getKunnr());
    log.setUserid(GCARS_USER);
    entityManager.persist(log);
  }

  @Override
  protected void cleanUp(EntityManager entityManager) {
    LOG.debug("Removing processed files from server..");
    String directory = SystemParameters.getString("GCARS.INPUT.DIR");
    if (StringUtils.isBlank(directory)) {
      directory = DEFAULT_DIR;
    }
    String archive = SystemParameters.getString("GCARS.ARCHIVE.DIR");
    if (StringUtils.isBlank(archive)) {
      archive = ARCHIVE_DIR;
    }

    for (String fileName : this.fileSizes.keySet()) {
      int expected = this.fileSizes.get(fileName);
      int processed = this.processedSizes.get(fileName);
      if (expected == processed) {
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
      }
    }
    this.fileSizes.clear();
    this.processedSizes.clear();
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