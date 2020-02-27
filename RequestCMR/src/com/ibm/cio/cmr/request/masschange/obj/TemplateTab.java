/**
 * 
 */
package com.ibm.cio.cmr.request.masschange.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Represents a tab/sheet on the mass change template
 * 
 * @author JeffZAMORA
 * 
 */
public class TemplateTab {

  private static final Logger LOG = Logger.getLogger(TemplateTab.class);

  private String name;
  private TemplateDataType type;
  private String typeCode;
  private List<TemplateColumn> columns = new ArrayList<TemplateColumn>();

  /**
   * Adds the {@link TemplateTab} details to the workbook
   * 
   * @param book
   */
  protected void writeTo(EntityManager entityManager, String country, XSSFWorkbook book, XSSFSheet control, XSSFCellStyle columnStyle, int maxRows) {
    // create the sheet
    XSSFSheet sheet = book.createSheet(this.name);

    // render the columns
    int currCol = 0;
    for (TemplateColumn column : this.columns) {
      LOG.trace("Rendering column " + column.getLabel());
      column.writeTo(entityManager, country, book, control, sheet, columnStyle, this, currCol, maxRows);
      currCol++;
    }

    // freeze first row
    sheet.createFreezePane(0, 1);

    // write the delimiter row
    writeDelimiter(book, sheet, maxRows);
  }

  protected TemplateValidation validate(EntityManager entityManager, XSSFWorkbook book, String country, int maxRows) throws Exception {
    TemplateValidation validation = new TemplateValidation(this.name);
    int currCol = 0;
    XSSFSheet sheet = book.getSheet(this.name);
    if (sheet == null) {
      throw new Exception("File not in correct format.");
    }
    LOG.debug("Tab: " + this.name);
    for (TemplateColumn col : this.columns) {
      col.validate(entityManager, validation, book, sheet, country, currCol, maxRows);
      currCol++;
    }
    return validation;
  }

  protected TemplateValidation validateSwiss(EntityManager entityManager, XSSFWorkbook book, String country, int maxRows,
      HashMap<String, String> hwFlagMap) throws Exception {
    TemplateValidation validation = new TemplateValidation(this.name);
    int currCol = 0;
    XSSFSheet sheet = book.getSheet(this.name);
    if (sheet == null) {
      throw new Exception("File not in correct format.");
    }
    LOG.debug("Tab: " + this.name);
    for (TemplateColumn col : this.columns) {
      col.validateSwiss(entityManager, validation, book, sheet, country, currCol, maxRows, hwFlagMap);
      currCol++;
    }
    return validation;
  }

  // CMR-800
  protected TemplateValidation validateAT(EntityManager entityManager, XSSFWorkbook book, String country, int maxRows,
      HashMap<String, String> hwFlagMap) throws Exception {
    TemplateValidation validation = new TemplateValidation(this.name);
    int currCol = 0;
    XSSFSheet sheet = book.getSheet(this.name);
    if (sheet == null) {
      throw new Exception("File not in correct format.");
    }
    LOG.debug("Tab: " + this.name);
    for (TemplateColumn col : this.columns) {
      col.validateAT(entityManager, validation, book, sheet, country, currCol, maxRows, hwFlagMap);
      currCol++;
    }
    return validation;
  }

  /**
   * Writes the merged row containing the delimiter text to inform the user that
   * they cannot input anything beyond the row
   * 
   * @param book
   * @param sheet
   * @param maxRows
   */
  private void writeDelimiter(XSSFWorkbook book, XSSFSheet sheet, int maxRows) {
    // add the DO NOT WRITE BEYOND THIS ROW label
    XSSFRow row = sheet.getRow(maxRows + 1);
    if (row == null) {
      row = sheet.createRow(maxRows + 1);
    }
    XSSFCell cell = row.getCell(0);
    if (cell == null) {
      cell = row.createCell(0);
    }
    LOG.trace("Writing delimiter row..");
    cell.setCellValue("DO NOT INPUT ANYTHING BEYOND THIS LINE. THIS IS THE MAXIMUM ALLOWED NUMBER OF ENTRIES.");

    // set the style
    XSSFCellStyle delimiterStyle = book.createCellStyle();
    delimiterStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    delimiterStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    delimiterStyle.setWrapText(true);
    delimiterStyle.setAlignment(HorizontalAlignment.CENTER);
    XSSFFont font = book.createFont();
    font.setBold(true);
    delimiterStyle.setFont(font);
    cell.setCellStyle(delimiterStyle);

    // add the merged region
    sheet.addMergedRegion(new CellRangeAddress(maxRows + 1, maxRows + 1, 0, this.columns.size() - 1));
  }

  public void addAll(List<TemplateColumn> tabs) {
    this.columns.addAll(tabs);
  }

  public void add(TemplateColumn column) {
    this.columns.add(column);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<TemplateColumn> getColumns() {
    return columns;
  }

  public void setColumns(List<TemplateColumn> columns) {
    this.columns = columns;
  }

  public TemplateDataType getType() {
    return type;
  }

  public void setType(TemplateDataType type) {
    this.type = type;
  }

  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
  }

}
