/**
 * 
 */
package com.ibm.cio.cmr.request.util.masscreate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.MassCreateAddrPK;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.entity.MassCreateDataPK;
import com.ibm.cio.cmr.request.entity.listeners.TrimListener;

/**
 * Represents the data on a mass create file
 * 
 * @author Jeffrey Zamora
 * 
 */
public class MassCreateFile {

  private static final Logger LOG = Logger.getLogger(MassCreateFile.class);

  public static enum ValidationResult {
    Passed, IncorrectVersion, NotValidated, InvalidFormat, HasErrors, UnknownError
  }

  private List<Map<String, Object>> records = new ArrayList<>();
  private List<MassCreateFileRow> rows = new ArrayList<MassCreateFileRow>();
  private Map<Integer, String> columnMap = new HashMap<Integer, String>();
  private static final int START_SEQUENCE_NO = 4;
  private static final String SEQ_NO_COL_NAME = "SEQ_NO";
  private static final String NOT_APPLICABLE = "na";
  private static final String PAR_REQ_ID_COL_NAME = "PAR_REQ_ID";
  private static final String ITERATION_ID_COL_NAME = "ITERATION_ID";
  private ValidationResult validationResult;
  private long reqId;
  private int iterationId;
  private String cmrIssuingCntry;

  protected MassCreateFile(long reqId, int iterationId) {
    this.reqId = reqId;
    this.iterationId = iterationId;
  }

  /**
   * Adds a record to the map
   * 
   * @param record
   */
  protected void addRecord(Map<String, Object> record) {
    this.records.add(record);
  }

  /**
   * Converts the records found on the file to a list of
   * {@link MassCreateFileRow} record with the given request id and iteration id
   * 
   * @param reqId
   * @param iterationId
   * @return
   * @throws Exception
   */
  protected void extractRows() throws Exception {
    List<MassCreateFileRow> rows = new ArrayList<MassCreateFileRow>();

    MassCreateFileRow row = null;
    MassCreateData data = null;
    List<MassCreateAddr> addresses = null;
    int seqNo = START_SEQUENCE_NO;
    for (Map<String, Object> record : this.records) {

      record.put(SEQ_NO_COL_NAME, seqNo);
      record.put(PAR_REQ_ID_COL_NAME, this.reqId);
      record.put(ITERATION_ID_COL_NAME, this.iterationId);

      row = new MassCreateFileRow();
      data = createDataObject(record);
      row.setData(data);
      row.setParentFile(this);

      addresses = createAddrObjects(record);
      row.addAddresses(addresses);

      for (String columnName : record.keySet()) {
        row.mapRawValue(columnName, record.get(columnName));
      }

      row.setSeqNo(seqNo);
      rows.add(row);
      seqNo++;
    }
    this.rows = rows;
    Collections.sort(this.rows);
  }

  /**
   * Updates the contents of the file present on the location with the contents
   * on this {@link MassCreateFile} object
   * 
   * @param parser
   * @param fileName
   * @throws IOException
   */
  public void updateFile(MassCreateFileParser parser, String fileName) throws IOException {
    updateFile(parser, fileName, false);
  }

  /**
   * Updates the contents of the file present on the location with the contents
   * on this {@link MassCreateFile} object
   * 
   * @param parser
   * @param fileName
   * @throws IOException
   */
  public void updateFile(MassCreateFileParser parser, String fileName, boolean cmrNosOnly) throws IOException {
    File targetFile = new File(fileName);
    if (!targetFile.exists()) {
      LOG.error("Target file does not exist.");
      return;
    }

    byte[] byteArray = null;
    // copy first the file to an output stream
    // needed to avoid stream contention
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      try (FileInputStream fis = new FileInputStream(targetFile)) {
        IOUtils.copy(fis, bos);
      }
      byteArray = bos.toByteArray();
    }

    // backup the file
    LOG.debug("Creating backup..");
    File backUp = new File(fileName + ".bk");
    try (FileOutputStream fos = new FileOutputStream(backUp)) {
      try (ByteArrayInputStream bis = new ByteArrayInputStream(byteArray)) {
        IOUtils.copy(bis, fos);
      }
    }

    try {
      // read the copied stream, update, then output
      try (ByteArrayInputStream bis = new ByteArrayInputStream(byteArray)) {
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
          parser.writeToFile(this, bis, fos, cmrNosOnly);
        }
      }
      LOG.debug("Removing backup..");
      backUp.delete();
    } catch (Exception e) {
      this.validationResult = ValidationResult.UnknownError;
      LOG.warn("An error occured while updating the file. Backup is being restored.");
      new File(fileName).delete();
      backUp.renameTo(new File(fileName));
    }
  }

  /**
   * Creates the {@link MassCreateData} object
   * 
   * @param record
   * @return
   * @throws Exception
   */
  protected MassCreateData createDataObject(Map<String, Object> record) throws Exception {

    MassCreateData data = new MassCreateData();
    populateObject(record, data, null);

    MassCreateDataPK id = new MassCreateDataPK();
    populateObject(record, id, null);
    data.setId(id);

    return data;
  }

  /**
   * Creates the {@link MassCreateAddr} object
   * 
   * @param record
   * @return
   * @throws Exception
   */
  protected List<MassCreateAddr> createAddrObjects(Map<String, Object> record) throws Exception {

    List<MassCreateAddr> addresses = new ArrayList<MassCreateAddr>();
    List<String> addressTypes = new ArrayList<String>();
    String type = null;

    // check all address types available on the values
    for (String colName : record.keySet()) {
      // address columns have format ADDR_TYPE-COL_NAME
      if (colName.contains("-")) {
        type = colName.substring(0, colName.indexOf("-"));
        if (!addressTypes.contains(type)) {
          addressTypes.add(type);
        }
      }
    }

    MassCreateAddr addr = null;
    MassCreateAddrPK id = null;
    int valCount = 0;
    for (String addrType : addressTypes) {
      addr = new MassCreateAddr();
      valCount = populateObject(record, addr, addrType);

      id = new MassCreateAddrPK();
      populateObject(record, id, null);

      id.setAddrType(addrType);
      addr.setId(id);
      if (valCount > 0) {
        addresses.add(addr);
      }
    }

    return addresses;
  }

  /**
   * Populates an object with the same name or {@link Column} annotation as a
   * provided map
   * 
   * @param record
   * @param object
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  private int populateObject(Map<String, Object> record, Object object, String prefix) throws IllegalArgumentException, IllegalAccessException {
    String pre = prefix != null ? prefix + "-" : "";
    Column column = null;
    Class<?> objectClass = object.getClass();
    int valueCount = 0;
    for (Field field : objectClass.getDeclaredFields()) {
      field.setAccessible(true);
      column = field.getAnnotation(Column.class);
      if (column != null && record.get(pre + column.name()) != null) {
        if (String.class.equals(field.getType())) {
          String val = record.get(pre + column.name()).toString();
          if (!StringUtils.isBlank(val)) {
            if (val.contains(MassCreateFileParser.CODE_DELIMITER)) {
              val = val.substring(0, val.indexOf(MassCreateFileParser.CODE_DELIMITER)).trim();
            }
            if (NOT_APPLICABLE.equals(val)) {
              val = null;
            }
            field.set(object, val != null ? TrimListener.removeInvalid(val.trim()) : val);
            valueCount++;
          }
        } else {
          field.set(object, record.get(pre + column.name()));
          valueCount++;
        }
      } else if (record.get(pre + field.getName().toUpperCase()) != null) {
        if (String.class.equals(field.getType())) {
          String val = record.get(pre + field.getName().toUpperCase()).toString();
          if (!StringUtils.isBlank(val)) {
            if (val.contains(MassCreateFileParser.CODE_DELIMITER)) {
              val = val.substring(0, val.indexOf(MassCreateFileParser.CODE_DELIMITER)).trim();
            }
            if (NOT_APPLICABLE.equals(val)) {
              val = null;
            }
            field.set(object, val != null ? TrimListener.removeInvalid(val.trim()) : val);
            valueCount++;
          }
        } else {
          field.set(object, record.get(pre + field.getName().toUpperCase()));
          valueCount++;
        }
      }
    }
    return valueCount;
  }

  public ValidationResult getValidationResult() {
    return validationResult;
  }

  public void setValidationResult(ValidationResult validationResult) {
    this.validationResult = validationResult;
  }

  public Map<Integer, String> getColumnMap() {
    return columnMap;
  }

  public void setColumnMap(Map<Integer, String> columnMap) {
    this.columnMap = columnMap;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public int getIterationId() {
    return iterationId;
  }

  public void setIterationId(int iterationId) {
    this.iterationId = iterationId;
  }

  public List<MassCreateFileRow> getRows() {
    return rows;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }
}
