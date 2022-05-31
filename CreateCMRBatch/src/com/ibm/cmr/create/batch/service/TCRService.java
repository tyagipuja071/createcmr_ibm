/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.File;
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
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.USTCRUpdtQueue;
import com.ibm.cio.cmr.request.entity.USTCRUpdtQueuePK;
import com.ibm.cio.cmr.request.entity.USTaxData;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * for TCR process
 * 
 */
public class TCRService extends MultiThreadedBatchService<USTCRUpdtQueue> {

  private static final Logger LOG = Logger.getLogger(TCRService.class);
  private static final String DEFAULT_DIR = "/ci/shared/data/tcr/";
  private Map<String, Integer> fileSizes = new HashMap<String, Integer>();
  private Map<String, Integer> processedSizes = new HashMap<String, Integer>();

  public static final String STATUS_PENDING = "P";
  public static final String STATUS_ERROR = "E";
  public static final String STATUS_ABORTED = "A";
  public static final String STATUS_COMPLETED = "C";
  public static final String TCR_USER = "TCR";
  private Mode mode = Mode.Extract;

  public static enum Mode {
    Extract, Update
  };

  @Override
  public Queue<USTCRUpdtQueue> getRequestsToProcess(EntityManager entityManager) {
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
  public Boolean executeBatchForRequests(EntityManager entityManager, List<USTCRUpdtQueue> list) throws Exception {
    switch (this.mode) {
    case Extract:
      insertQueueRecords(entityManager, list);
      break;
    case Update:
      updateTaxRecords(entityManager, list);
      break;
    }
    return true;
  }

  /**
   * Reads files and extracts the data into a series of {@link USTCRUpdtQueue}
   * objects
   * 
   * @param entityManager
   * @return
   */
  protected Queue<USTCRUpdtQueue> extractFromFiles(EntityManager entityManager) {
    String directory = SystemParameters.getString("TCR.INPUT.DIR");
    if (StringUtils.isBlank(directory)) {
      directory = DEFAULT_DIR;
    }
    Queue<USTCRUpdtQueue> queue = new LinkedList<USTCRUpdtQueue>();
    LOG.debug("TCR Input Directory: " + directory);

    List<String> nameList = getFileNames(directory);
    if (nameList.size() > 0) {
      for (String filename : nameList) {
        List<USTCRUpdtQueue> list = parseTCRFile(SystemConfiguration.getValue("MANDT"), directory + filename, filename);
        if (list != null && !list.isEmpty()) {
          LOG.debug(list.size() + " records added from file " + filename);
          this.fileSizes.putIfAbsent(filename, list.size());
          this.processedSizes.putIfAbsent(filename, 0);
          queue.addAll(list);
        }
      }
    }
    if (queue.isEmpty()) {
      LOG.debug("No TCR files found at this moment.");
    } else {
      LOG.debug("Gathered " + queue.size() + " records from TCR files.");
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
        LOG.info("TCR File " + file.getName() + " is loaded for processing.");
        nameList.add(file.getName());
      }
    }
    return nameList;
  }

  /**
   * Parses the TCR file and creates a list of {@link USTCRUpdtQueue} records
   * 
   * @param mandt
   * @param tcrFile
   * @param fileName
   * @return
   */
  @SuppressWarnings("deprecation")
  private List<USTCRUpdtQueue> parseTCRFile(String mandt, String tcrFile, String fileName) {
    LOG.debug("Parsing File: " + fileName);

    List<USTCRUpdtQueue> tcrList = new ArrayList<USTCRUpdtQueue>();
    if (tcrFile == null || tcrFile.equalsIgnoreCase("")) {
      LOG.error("ERROR: INVALID TCR File!");
      return null;
    }
    LineIterator it = null;
    try {
      it = FileUtils.lineIterator(new File(tcrFile), "UTF-8");
      int seq = 1;
      String tcrFileName = fileName;

      while (it.hasNext()) {
        String identity = "";
        String line = it.nextLine();
        USTCRUpdtQueue tcr = new USTCRUpdtQueue();
        USTCRUpdtQueuePK tcrPK = new USTCRUpdtQueuePK();
        tcrPK.setSeqNo(seq);
        tcrPK.setMandt(mandt);
        if (StringUtils.isNotBlank(tcrFileName)) {
          tcrPK.setTcrFileNm(tcrFileName);
        }
        if (line != null) {
          identity = line.substring(0, 3);
          tcr.setLineContent(line);

          tcrPK.setTcrFileNm(tcrFileName);

          if (line.startsWith("CMR")) {
            // this is record, processing

            String[] strArray = line.split("\\s+");
            identity = strArray[0].substring(0, 3);
            String cmrNum = strArray[0].substring(3, strArray[0].length());

            tcr.setId(tcrPK);
            tcr.setCmrNo(cmrNum);

            String fieldType1 = strArray[1].substring(0, 2);
            String fieldValue1 = strArray[1].substring(2, strArray[1].length());

            if ("71".equals(fieldType1.trim())) {
              tcr.setTaxCustTyp1(fieldValue1.substring(0, 1));
              tcr.setTaxClass1(fieldValue1.substring(1, fieldValue1.length()));
            } else if ("72".equals(fieldType1.trim())) {
              tcr.setTaxCustTyp2(fieldValue1.substring(0, 1));
              tcr.setTaxClass2(fieldValue1.substring(1, fieldValue1.length()));
            } else if ("73".equals(fieldType1.trim())) {
              tcr.setTaxCustTyp3(fieldValue1.substring(0, 1));
              tcr.setTaxClass3(fieldValue1.substring(1, fieldValue1.length()));
            } else if ("91".equals(fieldType1.trim())) {
              tcr.setTaxExemptStatus1(fieldValue1);
            } else if ("92".equals(fieldType1.trim())) {
              tcr.setTaxExemptStatus2(fieldValue1);
            } else if ("98".equals(fieldType1.trim())) {
              tcr.setTaxExemptStatus3(fieldValue1);
            }

            if (strArray.length == 3) {
              String fieldType2 = strArray[2].substring(0, 2);
              String fieldValue2 = strArray[2].substring(2, strArray[2].length());
              if ("71".equals(fieldType2.trim())) {
                tcr.setTaxCustTyp1(fieldValue2.substring(0, 1));
                tcr.setTaxClass1(fieldValue2.substring(1, fieldValue1.length()));
              } else if ("72".equals(fieldType2.trim())) {
                tcr.setTaxCustTyp2(fieldValue2.substring(0, 1));
                tcr.setTaxClass2(fieldValue2.substring(1, fieldValue1.length()));
              } else if ("73".equals(fieldType2.trim())) {
                tcr.setTaxCustTyp3(fieldValue2.substring(0, 1));
                tcr.setTaxClass3(fieldValue2.substring(1, fieldValue1.length()));
              } else if ("91".equals(fieldType2.trim())) {
                tcr.setTaxExemptStatus1(fieldValue2);
              } else if ("92".equals(fieldType2.trim())) {
                tcr.setTaxExemptStatus2(fieldValue2);
              } else if ("98".equals(fieldType2.trim())) {
                tcr.setTaxExemptStatus3(fieldValue2);
              }
            }
          }
          tcr.setCreateBy(BATCH_USER_ID);
          tcr.setProcStatus(STATUS_PENDING);
          tcr.setUpdateBy(BATCH_USER_ID);
          tcr.setKatr10("");
        }
        if ("CMR".equalsIgnoreCase(identity)) {
          LOG.info("TCR record CMR number " + tcr.getCmrNo() + ", seq " + tcr.getId().getSeqNo() + " load.");
          tcrList.add(tcr);
          seq++;
        } else if ("HDR".equalsIgnoreCase(identity)) {
          // do something for HDR line
        } else if ("TRL".equalsIgnoreCase(identity)) {
          // do something for TRL line
        }
      }
    } catch (Exception e) {
      LOG.debug("Error in parsing file " + fileName, e);
    } finally {
      LineIterator.closeQuietly(it);
    }
    return tcrList;
  }

  /**
   * Gets the {@link USTCRUpdtQueue} records that are pending for processing
   * 
   * @param entityManager
   * @return
   */
  private Queue<USTCRUpdtQueue> getPendingRecords(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("TCR.GET_PENDING.RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<USTCRUpdtQueue> list = query.getResults(USTCRUpdtQueue.class);
    Queue<USTCRUpdtQueue> queue = new LinkedList<USTCRUpdtQueue>();
    if (list != null && !list.isEmpty()) {
      queue.addAll(list);
    }
    return queue;
  }

  /**
   * Inserts the prepared list of {@link USTCRUpdtQueue} records to the DB
   * 
   * @param entityManager
   * @param list
   */
  private void insertQueueRecords(EntityManager entityManager, List<USTCRUpdtQueue> list) {
    if (!list.isEmpty()) {
      LOG.info("There are " + list.size() + " records to process.");

      Timestamp ts = SystemUtil.getActualTimestamp();
      for (USTCRUpdtQueue queue : list) {
        queue.setCreateDt(ts);
        queue.setUpdateDt(ts);
        LOG.debug("Persisting " + queue.getId().getTcrFileNm() + " - " + queue.getId().getSeqNo());
        entityManager.persist(queue);
        entityManager.flush();

        synchronized (this) {
          this.processedSizes.put(queue.getId().getTcrFileNm(), this.processedSizes.get(queue.getId().getTcrFileNm()) + 1);
        }

      }
      partialCommit(entityManager);
    }
  }

  /**
   * Updates {@link Kna1}, {@link USTaxData}, and creates {@link Changelog}
   * records
   * 
   * @param entityManager
   * @param list
   */
  private void updateTaxRecords(EntityManager entityManager, List<USTCRUpdtQueue> list) {
    if (!list.isEmpty()) {
      for (USTCRUpdtQueue queue : list) {
        Timestamp ts = SystemUtil.getActualTimestamp();
        queue.setUpdateBy(BATCH_USER_ID);
        queue.setUpdateDt(ts);
        try {
          String sql = ExternalizedQuery.getSql("TCR.GET_RECORDS.RDC");
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          query.setParameter("CMR_NO", queue.getCmrNo());
          List<CompoundEntity> records = query.getCompundResults(USTaxData.class, USTaxData.TAX_ENTITY_MAPPING);
          if (records != null && !records.isEmpty()) {
            for (CompoundEntity record : records) {

              Kna1 kna1 = record.getEntity(Kna1.class);
              USTaxData tax = record.getEntity(USTaxData.class);
              LOG.debug("Processing KUNNR " + kna1.getId().getKunnr() + " for CMR No. " + kna1.getZzkvCusno());

              // check if tax type 1 has a value
              if (!StringUtils.isBlank(queue.getTaxCustTyp1())) {
                String taxCd2 = queue.getTaxCustTyp1() + queue.getTaxClass1();
                if (!taxCd2.equals(kna1.getStcd2())) {
                  createChangeLog(entityManager, kna1, queue, ts, "KNA1", "STCD2", kna1.getStcd2(), taxCd2);
                  kna1.setStcd2(queue.getTaxCustTyp1() + queue.getTaxClass1());
                  kna1.setShadUpdateTs(ts);
                  entityManager.merge(kna1);
                }

                if (!queue.getTaxCustTyp1().equals(tax.getiTypeCust1())) {
                  createChangeLog(entityManager, kna1, queue, ts, "US_TAX", "I_TYPE_CUST_1", tax.getiTypeCust1(), queue.getTaxCustTyp1());
                  tax.setiTypeCust1(queue.getTaxCustTyp1());
                }
                if (!queue.getTaxClass1().equals(tax.getiTaxClass1())) {
                  createChangeLog(entityManager, kna1, queue, ts, "US_TAX", "I_TAX_CLASS_1", tax.getiTaxClass1(), queue.getTaxClass1());
                  tax.setiTaxClass1(queue.getTaxClass1());
                }
              }

              // check if tax type 2 has a value
              if (!StringUtils.isBlank(queue.getTaxCustTyp2())) {
                if (!queue.getTaxCustTyp2().equals(tax.getiTypeCust2())) {
                  createChangeLog(entityManager, kna1, queue, ts, "US_TAX", "I_TYPE_CUST_2", tax.getiTypeCust2(), queue.getTaxCustTyp2());
                  tax.setiTypeCust2(queue.getTaxCustTyp2());
                }
                if (!queue.getTaxClass2().equals(tax.getiTaxClass2())) {
                  createChangeLog(entityManager, kna1, queue, ts, "US_TAX", "I_TAX_CLASS_2", tax.getiTaxClass2(), queue.getTaxClass2());
                  tax.setiTaxClass2(queue.getTaxClass2());
                }
              }

              // check if tax type 3 has a value
              if (!StringUtils.isBlank(queue.getTaxCustTyp3())) {
                if (!queue.getTaxCustTyp3().equals(tax.getiTypeCust3())) {
                  createChangeLog(entityManager, kna1, queue, ts, "US_TAX", "I_TYPE_CUST_3", tax.getiTypeCust3(), queue.getTaxCustTyp3());
                  tax.setiTypeCust3(queue.getTaxCustTyp3());
                }
                if (!queue.getTaxClass3().equals(tax.getiTaxClass3())) {
                  createChangeLog(entityManager, kna1, queue, ts, "US_TAX", "I_TAX_CLASS_3", tax.getiTaxClass3(), queue.getTaxClass3());
                  tax.setiTaxClass3(queue.getTaxClass3());
                }
              }

              // check exempt statuses
              if (queue.getTaxExemptStatus1() != null) {
                if (!queue.getTaxExemptStatus1().trim().equals(tax.getcTeCertST1())) {
                  createChangeLog(entityManager, kna1, queue, ts, "US_TAX", "C_TE_CERT_ST_1", tax.getcTeCertST1(), queue.getTaxExemptStatus1());
                  tax.setcTeCertST1(queue.getTaxExemptStatus1().trim());
                }
              }
              if (queue.getTaxExemptStatus2() != null) {
                if (!queue.getTaxExemptStatus2().trim().equals(tax.getcTeCertST2())) {
                  createChangeLog(entityManager, kna1, queue, ts, "US_TAX", "C_TE_CERT_ST_2", tax.getcTeCertST2(), queue.getTaxExemptStatus2());
                  tax.setcTeCertST2(queue.getTaxExemptStatus2().trim());
                }
              }
              if (queue.getTaxExemptStatus3() != null) {
                if (!queue.getTaxExemptStatus3().trim().equals(tax.getcTeCertST3())) {
                  createChangeLog(entityManager, kna1, queue, ts, "US_TAX", "C_TE_CERT_ST_3", tax.getcTeCertST3(), queue.getTaxExemptStatus3());
                  tax.setcTeCertST3(queue.getTaxExemptStatus3().trim());
                }
              }

              tax.setUpdateBy(TCR_USER);
              tax.setUpdateDt(ts);
              updateEntity(tax, entityManager);

              LOG.debug(" - KUNNR " + kna1.getId().getKunnr() + " for CMR No. " + kna1.getZzkvCusno() + " done.");

            }

            queue.setProcStatus(STATUS_COMPLETED);
          } else {
            queue.setProcStatus(STATUS_ERROR);
            queue.setProcMsg("Records for CMR No. " + queue.getCmrNo() + " not found.");
          }
          updateEntity(queue, entityManager);
        } catch (Exception e) {
          LOG.debug("Error when processing queue record " + queue.getId().getTcrFileNm() + "-" + queue.getId().getSeqNo(), e);
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
   * @param mandt
   * @param kunnr
   * @param table
   * @param field
   * @param oldValue
   * @param newValue
   */
  protected void createChangeLog(EntityManager entityManager, Kna1 kna1, USTCRUpdtQueue queue, Timestamp ts, String table, String field,
      String oldValue, String newValue) {
    ChangelogPK pk = new ChangelogPK();
    pk.setChgts(ts);
    pk.setMandt(kna1.getId().getMandt());
    pk.setKunnr(kna1.getId().getKunnr());
    pk.setTab(table);
    pk.setField(field);
    Changelog log = new Changelog();
    log.setId(pk);
    log.setActgrp(kna1.getKtokd());
    log.setAction("U");
    log.setChgpnt("Y");
    log.setLinenumber(queue.getId().getSeqNo() + "");
    log.setLoadfilename(queue.getId().getTcrFileNm());
    log.setOld(oldValue);
    log.setNewValue(newValue);
    log.setTabkey1(kna1.getId().getKunnr());
    log.setUserid(TCR_USER);
    entityManager.persist(log);
  }

  @Override
  protected void cleanUp(EntityManager entityManager) {
    LOG.debug("Removing processed files from server..");
    String directory = SystemParameters.getString("TCR.INPUT.DIR");
    if (StringUtils.isBlank(directory)) {
      directory = DEFAULT_DIR;
    }
    for (String fileName : this.fileSizes.keySet()) {
      int expected = this.fileSizes.get(fileName);
      int processed = this.processedSizes.get(fileName);
      if (expected == processed) {
        LOG.debug("File " + fileName + " processed fully. Removing..");
        FileUtils.deleteQuietly(new File(directory + File.separator + fileName));
      }
    }
  }

  @Override
  protected boolean hasCleanup() {
    return true;
  }

  @Override
  protected String getThreadName() {
    return "TCR-Get";
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