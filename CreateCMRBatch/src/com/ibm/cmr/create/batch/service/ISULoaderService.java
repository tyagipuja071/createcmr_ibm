/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cmr.create.batch.util.isuload.ISULoadUtility;

/**
 * Service for the ISU Loader batch application
 * 
 * @author Jeffrey Zamora
 * @author David Partow
 * 
 */
public class ISULoaderService extends BaseBatchService {

  private String inFile;
  private String outFile;
  private static int BYTEREADLENGTH = 700;
  private static int AMOUNT_BATCH = 5000;

  public ISULoaderService(String inFile, String outFile) {
    super();
    this.inFile = inFile;
    this.outFile = outFile;
  }

  @Override
  public Boolean executeBatch(EntityManager entityManager) throws Exception {
    String sql = ExternalizedQuery.getSql("BATCH.ISULOAD.GETISU");
    String cmrNumber = "";
    int counter = 0;
    int skipped = 0;
    int debugMax = -1;

    LOG.info("Input File: " + this.inFile);
    LOG.info("Output File: " + this.outFile);
    File ofile = new File(this.outFile);
    File file = new File(this.inFile);
    DataInputStream dis = null;
    DataOutputStream dos = null;

    try {
      dos = new DataOutputStream(new FileOutputStream(ofile));
      dis = new DataInputStream(new FileInputStream(file));
      int bytesRead = 0;
      boolean debugflag = true;

      HashMap<String, byte[]> fileDataInfoHash = new HashMap<String, byte[]>();
      HashMap<String, String> isuInfoHash;
      ArrayList<String> cmrNumList = new ArrayList<String>();
      StringBuilder cmrBuilder = new StringBuilder();
      while (bytesRead != -1 && debugflag) {
        // returns -1 at EOF
        byte[] fileData = new byte[BYTEREADLENGTH];
        bytesRead = dis.read(fileData, 0, BYTEREADLENGTH);
        // cmr number byte data at position 167-170 (array index 166-169)
        cmrNumber = ISULoadUtility.extractCMRNumber(fileData, 166, 169);
        counter++;
        if (cmrBuilder.toString() != null && !cmrBuilder.toString().equals(""))
          cmrBuilder.append("','").append(cmrNumber);
        else
          cmrBuilder.append(cmrNumber);
        fileDataInfoHash.put(cmrNumber, fileData);
        cmrNumList.add(cmrNumber);
        if (counter % AMOUNT_BATCH == 0) {
          isuInfoHash = readISUCodeListFromRDC(entityManager, sql, cmrBuilder.toString());
          skipped = skipped + addISUcodeBatch(fileDataInfoHash, isuInfoHash, cmrNumList, dos);
          cmrBuilder.delete(0, cmrBuilder.length());
        }
        if (counter == debugMax) {
          debugflag = false;
        }
      }
      if (cmrBuilder.toString() != null && !cmrBuilder.toString().equals("")) {
        isuInfoHash = readISUCodeListFromRDC(entityManager, sql, cmrBuilder.toString());
        skipped = skipped + addISUcodeBatch(fileDataInfoHash, isuInfoHash, cmrNumList, dos);
        cmrBuilder.delete(0, cmrBuilder.length());
      }
    } catch (Exception e) {
      LOG.error("An error was encountered during processing. Aborting...", e);
      addError(e);
      return false;
    } finally {
      if (dis != null)
        dis.close();
      if (dos != null)
        dos.close();
    }

    LOG.info("Skipped: " + skipped + " records..");
    LOG.info("Processed: " + counter + " records..");

    return true;
  }

  /**
   * Connects to RDc and gets the ISU List for the CMR No in the batch.
   * 
   * @param entityManager
   * @param sql
   * @param cmrNumber
   *          list in the batch
   * @return
   * @throws SQLException
   */
  @SuppressWarnings("unchecked")
  private static HashMap<String, String> readISUCodeListFromRDC(EntityManager entityManager, String sql, String cmrNumber) throws SQLException {

    sql = StringUtils.replaceOnce(sql, ":CMR_NO", "'" + cmrNumber + "'");
    sql = StringUtils.replaceOnce(sql, ":MANDT", "'" + SystemConfiguration.getValue("MANDT") + "'");

    HashMap<String, String> isuInfoHash = null;
    List<Object[]> isuInfoList = entityManager.createNativeQuery(sql).getResultList();
    if (isuInfoList != null && isuInfoList.size() != 0) {
      isuInfoHash = new HashMap<String, String>();
      for (Object[] isuObj : isuInfoList)
        isuInfoHash.put((String) isuObj[1], (String) isuObj[0]);
    }
    return isuInfoHash;
  }

  /**
   * Adds the ISU code to the byte stream at positions 662-663
   * 
   * @param isuCode
   * @param fileData
   */
  private static void addISUcodeToDataStream(String isuCode, byte[] fileData) {
    // don't do anything if this is not a 2 digit ISU code
    if (isuCode != null && isuCode.trim().length() != 2) {
      return;
    }

    // convert isu code to ebcdic bytes
    byte abyte = ISULoadUtility.getEBCDICbyteForString(isuCode.substring(0, 1));
    byte bbyte = ISULoadUtility.getEBCDICbyteForString(isuCode.substring(1, 2));

    // stick it in the datastream at position 662-663 (array 661,662)
    fileData[661] = abyte;
    fileData[662] = bbyte;
  }

  @Override
  protected boolean isTransactional() {
    return false;
  }

  @Override
  protected String getPersistenceUnitName() {
    // RDC is for CIWEB, use the other persistence unit RDC_MAIN for main RDc
    return "RDC_MAIN";
  }

  /**
   * Get all ISU codes from DB in a Batch CMR Num
   * 
   * @param fileDataInfoHash
   *          , include all fileData in this Batch
   * @param isuInfoHash
   *          , to store all ISU codes
   * @param cmrNumList
   *          , include all CMR nums in this batch
   * @param DataOutputStream
   *          , to write all file Data with ISU code into new file.
   * @return int, the number of CMR num with no ISU codes.
   */
  public int addISUcodeBatch(HashMap<String, byte[]> fileDataInfoHash, HashMap<String, String> isuInfoHash, ArrayList<String> cmrNumList,
      DataOutputStream dos) throws Exception {
    int skipped = 0;
    String isuCode = null;
    byte[] fileData;
    for (int i = 0; i < cmrNumList.size(); i++) {
      fileData = fileDataInfoHash.get(cmrNumList.get(i));
      if (isuInfoHash != null)
        isuCode = isuInfoHash.get(cmrNumList.get(i));
      if (isuCode != null) {
        addISUcodeToDataStream(isuCode, fileData);
      } else {
        skipped++;
        LOG.trace("CMR Num skipped: " + cmrNumList.get(i));
      }
      dos.write(fileData, 0, BYTEREADLENGTH);
      fileData = null;
    }
    fileDataInfoHash.clear();
    if (isuInfoHash != null)
      isuInfoHash.clear();
    cmrNumList.clear();
    return skipped;
  }
}
