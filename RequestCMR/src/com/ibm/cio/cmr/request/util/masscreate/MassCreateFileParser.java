/**
 * 
 */
package com.ibm.cio.cmr.request.util.masscreate;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.listeners.TrimListener;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFile.ValidationResult;

/**
 * Generic parser for a mass create files
 * 
 * @author Jeffrey Zamora
 * 
 */
public class MassCreateFileParser {

  private static final Logger LOG = Logger.getLogger(MassCreateFileParser.class);

  public static final String DATA_SHEET_NAME = "Data";
  public static final String META_SHEET_NAME = "Meta";
  public static final int DATA_SHEET_FIELD_COLUMN = 1;
  public static final int DATA_SHEET_START_ROW = 3;
  public static final String CODE_DELIMITER = "|";
  public static final String NOT_USED_INDICATOR = "na";
  public static final String ERROR_MESSAGE_COLUMN = "ERROR_MSG";

  public static void main(String[] args) throws Exception {
    String loc = "C:\\projects\\RDC\\projects\\CMMA\\createCMR\\1.3\\MassCreateTemplateTest.xlsm";
    MassCreateFile createFile = null;
    try (FileInputStream fis = new FileInputStream(loc)) {
      MassCreateFileParser parser = new MassCreateFileParser();
      createFile = parser.parse(fis, 1, 1, false);
    }
    if (createFile != null) {
      ObjectMapper mapper = new ObjectMapper();
      for (MassCreateFileRow row : createFile.getRows()) {
        System.out.println("row");
        System.out.println(mapper.writeValueAsString(row.getData()));
        for (MassCreateAddr addr : row.getAddresses()) {
          System.out.println(mapper.writeValueAsString(addr));
        }
      }
    }
    System.out.println("ok");
  }

  /**
   * Check the format of the excel file. Check version, if it's validated, if
   * data sheet is available.
   * 
   * @param fileStream
   * @param close
   * @return
   * @throws IOException
   */
  public ValidationResult checkFile(InputStream fileStream, boolean close) throws IOException {
    try {
      LOG.debug("Validating input mass create file..");
      XSSFWorkbook book = new XSSFWorkbook(fileStream);
      try {

        return validateFile(book);
      } finally {
        if (close) {
          book.close();
        }
      }
    } catch (Exception e) {
      LOG.debug("Error when reading the file.", e);
      return ValidationResult.UnknownError;
    }
  }

  /**
   * Method to parse the file and check for the basic requirements for
   * pre-validation:<br>
   * <ul>
   * <li>Version No</li>
   * <li>Validation indicator</li>
   * <li>Basic format</li>
   * </ul>
   * 
   * @param book
   * @return
   */
  private ValidationResult validateFile(XSSFWorkbook book) {
    XSSFSheet sheet = book.getSheet(META_SHEET_NAME);
    String version = sheet.getRow(0).getCell(1).getStringCellValue();
    String currentVersion = SystemConfiguration.getValue("MASS_CREATE_TEMPLATE_VER", "0.1");
    if (StringUtils.isBlank(version) || !version.equals(currentVersion)) {
      LOG.debug("Invalid version:" + version + " Current: " + currentVersion);
      return ValidationResult.IncorrectVersion;
    }
    String validated = sheet.getRow(1).getCell(1).getStringCellValue();
    if (!CmrConstants.YES_NO.Y.toString().equals(validated)) {
      LOG.debug("File not fully validated.");
      return ValidationResult.NotValidated;
    }

    sheet = book.getSheet(DATA_SHEET_NAME);
    if (sheet == null) {
      LOG.debug("Data sheet is missing.");
      return ValidationResult.InvalidFormat;
    }

    LOG.debug("File is correct.");
    return ValidationResult.Passed;
  }

  /**
   * Parses the file from the filestream and creates a {@link MassCreateFile}
   * object representing the data found on the file
   * 
   * @param fileStream
   * @param reqId
   * @param iterationId
   * @param close
   * @return
   * @throws Exception
   */
  @SuppressWarnings("deprecation")
  public MassCreateFile parse(InputStream fileStream, long reqId, int iterationId, boolean close) throws Exception {
    MassCreateFile massCreateFile = new MassCreateFile(reqId, iterationId);
    XSSFWorkbook book = new XSSFWorkbook(fileStream);
    try {
      massCreateFile.setValidationResult(validateFile(book));
      DecimalFormat formatter = new DecimalFormat("#");
      XSSFSheet sheet = book.getSheet(DATA_SHEET_NAME);

      Map<Integer, String> columnMap = new HashMap<>();
      if (sheet != null) {
        String fieldName = null;
        XSSFCell sheetCell = null;
        XSSFRow sheetRow = sheet.getRow(DATA_SHEET_FIELD_COLUMN);

        int columnIndex = 0;
        int rowIndex = 0;
        int maxMappedCol = -1;
        // track the column names first
        for (Cell cell : sheetRow) {
          sheetCell = (XSSFCell) cell;
          fieldName = sheetCell.getStringCellValue();
          if (!StringUtils.isBlank(fieldName)) {
            columnMap.put(columnIndex, fieldName.toUpperCase());
            if (columnIndex > maxMappedCol) {
              maxMappedCol = columnIndex;
            }
          }
          columnIndex++;
        }
        massCreateFile.setColumnMap(columnMap);

        // parse the records
        Map<String, Object> record = null;
        String cellValue = null;
        boolean valid = false;
        int cellIndex = 0;
        for (Row row : sheet) {
          sheetRow = (XSSFRow) row;
          valid = false;
          if (rowIndex >= DATA_SHEET_START_ROW) {
            columnIndex = 0;
            for (cellIndex = 0; cellIndex <= maxMappedCol; cellIndex++) {
              // for (Cell cell : sheetRow) {
              sheetCell = sheetRow.getCell(cellIndex);
              if (sheetCell == null) {
                sheetCell = sheetRow.createCell(cellIndex);
              }
              if (sheetCell != null && columnIndex == 0 && !StringUtils.isBlank(sheetCell.getStringCellValue())) {
                record = new HashMap<>();
                valid = true;
              }
              if (sheetCell != null && valid) {
                switch (sheetCell.getCellTypeEnum()) {
                case STRING:
                  cellValue = sheetCell.getStringCellValue();
                  break;
                case NUMERIC:
                  cellValue = formatter.format(sheetCell.getNumericCellValue());
                  break;
                case BLANK:
                  cellValue = "";
                  break;
                default:
                  cellValue = sheetCell.getRawValue();
                }
                if (columnMap.get(columnIndex) != null) {
                  record.put(columnMap.get(columnIndex), cellValue != null ? TrimListener.removeInvalid(cellValue.trim()) : cellValue);
                }
              }
              columnIndex++;
            }
            if (valid) {
              massCreateFile.addRecord(record);
            }
          }
          rowIndex++;
        }
      }
    } finally {
      if (close) {
        book.close();
      }
    }
    massCreateFile.extractRows();
    return massCreateFile;
  }

  /**
   * /** Writes the {@link MassCreateFile} object to a targetstream based on the
   * source stream
   * 
   * @param massCreateFile
   * @param sourceStream
   * @param targetStream
   * @throws IOException
   */
  public void writeToFile(MassCreateFile massCreateFile, InputStream sourceStream, OutputStream targetStream) throws IOException {
    writeToFile(massCreateFile, sourceStream, targetStream, false);
  }

  /**
   * Writes the {@link MassCreateFile} object to a targetstream based on the
   * source stream
   * 
   * @param massCreateFile
   * @param sourceStream
   * @param targetStream
   * @throws IOException
   */
  public void writeToFile(MassCreateFile massCreateFile, InputStream sourceStream, OutputStream targetStream, boolean cmrNosOnly) throws IOException {
    LOG.debug("Writing data to outputstream...");
    XSSFWorkbook book = new XSSFWorkbook(sourceStream);
    XSSFSheet sheet = book.getSheet(DATA_SHEET_NAME);
    Map<Integer, String> columnMap = massCreateFile.getColumnMap();
    XSSFRow xlsRow = null;
    XSSFCell xlsCell = null;
    String columnName = null;
    Object rawValue = null;

    XSSFFont updatedFont = null;
    XSSFCellStyle updatedStyle = null;
    int maxColumn = 0;

    for (int i : columnMap.keySet()) {
      if (i > maxColumn) {
        maxColumn = i;
      }
    }
    maxColumn += 1;

    if (cmrNosOnly) {
      xlsRow = sheet.getRow(DATA_SHEET_START_ROW - 1);
      xlsCell = xlsRow.getCell(maxColumn);
      if (xlsCell == null) {
        xlsCell = xlsRow.createCell(maxColumn);
      }
      xlsCell.setCellValue("CMR No");
    }
    for (MassCreateFileRow row : massCreateFile.getRows()) {
      // sequence no - 1 for POI
      xlsRow = sheet.getRow(row.getSeqNo() - 1);
      if (xlsRow == null) {
        xlsRow = sheet.createRow(row.getSeqNo() - 1);
      }
      if (cmrNosOnly) {

        if (!StringUtils.isBlank(row.getCmrNo())) {
          xlsCell = xlsRow.getCell(maxColumn);
          if (xlsCell == null) {
            xlsCell = xlsRow.createCell(maxColumn);
          }
          xlsCell.setCellValue(row.getCmrNo());
          // create new style and font to not affect the existing styles
          updatedStyle = book.createCellStyle();
          updatedStyle.setLocked(xlsCell.getCellStyle().getLocked());
          updatedFont = book.createFont();
          updatedFont.setColor(new XSSFColor(new Color(0, 0, 255)));
          updatedStyle.setFont(updatedFont);
          xlsCell.setCellStyle(updatedStyle);
        }
      } else {
        for (int i : columnMap.keySet()) {
          columnName = columnMap.get(i);
          rawValue = row.getRawValue(columnName);
          if (ERROR_MESSAGE_COLUMN.equals(columnName)) {
            if (row.hasError()) {
              rawValue = row.getErrorMessage();
            } else {
              rawValue = "";
            }
          }
          if (rawValue != null) {
            xlsCell = xlsRow.getCell(i);
            if (xlsCell == null) {
              xlsCell = xlsRow.createCell(i);
            }
            xlsCell.setCellValue(rawValue.toString());
            if (row.isUpdated(columnName)) {
              // create new style and font to not affect the existing styles
              updatedStyle = book.createCellStyle();
              updatedStyle.setLocked(xlsCell.getCellStyle().getLocked());
              updatedFont = book.createFont();
              updatedFont.setColor(new XSSFColor(new Color(0, 0, 255)));
              updatedStyle.setFont(updatedFont);
              xlsCell.setCellStyle(updatedStyle);
            }
          }
        }
      }

    }
    book.write(targetStream);
    LOG.debug("Data written successfully.");
  }

  public void copyErrorRows(String fileName, Map<Integer, String> errorMessages, OutputStream targetStream, long reqId, int iterationId)
      throws Exception {
    LOG.debug("Copying rows with errors to target stream...");
    try (FileInputStream fis = new FileInputStream(fileName)) {
      XSSFWorkbook book = new XSSFWorkbook(fis);
      XSSFSheet sheet = book.getSheet(DATA_SHEET_NAME);
      XSSFRow xlsRow = null;
      XSSFCell xlsCell = null;
      LOG.debug("Data written successfully.");

      xlsRow = sheet.getRow(DATA_SHEET_FIELD_COLUMN);

      int columnIndex = 0;
      String fieldName = null;

      int errorColIndex = -1;
      for (Cell cell : xlsRow) {
        xlsCell = (XSSFCell) cell;
        fieldName = xlsCell.getStringCellValue();
        if (!StringUtils.isBlank(fieldName) && ERROR_MESSAGE_COLUMN.equals(fieldName)) {
          errorColIndex = columnIndex;
          break;
        }
        columnIndex++;
      }

      if (errorColIndex >= 0) {
        // copy error message to the row
        for (Row row : sheet) {
          xlsRow = (XSSFRow) row;
          if (xlsRow.getRowNum() >= DATA_SHEET_START_ROW) {
            if (errorMessages.get(xlsRow.getRowNum()) != null) {
              LOG.debug("Writing error to Row " + xlsRow.getRowNum());
              xlsRow.getCell(errorColIndex).setCellValue("Processing Error:\n" + errorMessages.get(xlsRow.getRowNum()));
            } else {
              xlsRow.getCell(errorColIndex).setCellValue("");
            }
          }
        }

        // delete the rows without errors
        for (int i = DATA_SHEET_START_ROW; i <= sheet.getLastRowNum(); i++) {
          xlsRow = sheet.getRow(i);
          if (xlsRow != null) {
            if (xlsRow.getCell(errorColIndex) == null || StringUtils.isBlank(xlsRow.getCell(errorColIndex).getStringCellValue())) {
              sheet.removeRow(xlsRow);
            }
          }
        }
      } else {
        LOG.warn("Error Message column cannot be found. Book may not be in the proper format.");
      }

      book.write(targetStream);
    }
  }
}
