/**
 * 
 */
package com.ibm.cio.cmr.request.masschange.obj;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation.ValidationRow;
import com.ibm.cio.cmr.request.util.at.ATUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cio.cmr.request.util.swiss.SwissUtil;

/**
 * Represents a template for mass update or create. This template is generated
 * from a configuration XML file
 * 
 * @author JeffZAMORA
 * 
 */
public class MassChangeTemplate {

  private static final Logger LOG = Logger.getLogger(MassChangeTemplate.class);
  private static final int ERROR_COL_WIDTH = 256 * 50;

  private String id;
  private String type;
  private List<TemplateTab> tabs = new ArrayList<>();

  /**
   * Generates the xlsx template based on the setup of this template
   * 
   * @param os
   * @throws IOException
   */
  public void generate(OutputStream os, EntityManager entityManager, String country, int maxRows) throws IOException {
    XSSFWorkbook book = new XSSFWorkbook();
    try {
      XSSFCellStyle colStyle = book.createCellStyle();
      colStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
      colStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      colStyle.setWrapText(true);
      XSSFFont font = book.createFont();
      font.setBold(true);
      colStyle.setFont(font);

      // protect the control sheet
      XSSFSheet control = book.createSheet("Control");

      // generate the sheets
      for (TemplateTab tab : this.tabs) {
        LOG.debug("Adding sheet " + tab.getName());
        tab.writeTo(entityManager, country, book, control, colStyle, maxRows);
      }

      // move control to last and hide
      book.setSheetOrder("Control", this.tabs.size());
      book.setSheetHidden(this.tabs.size(), true);
      // control.protectSheet(getRandomPassword());

      book.setActiveSheet(0);
      book.setFirstVisibleTab(0);
      book.setSelectedTab(0);

      // write
      book.write(os);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      book.close();
    }
  }

  /**
   * Validates the file and returns a list of validation results
   * 
   * @param entityManager
   * @param is
   * @param country
   * @param maxRows
   * @return
   * @throws Exception
   */
  public List<TemplateValidation> validate(EntityManager entityManager, InputStream is, String country, int maxRows) throws Exception {
    XSSFWorkbook book = new XSSFWorkbook(is);
    try {
      List<TemplateValidation> validations = new ArrayList<TemplateValidation>();
      if (SwissUtil.isCountrySwissEnabled(entityManager, country)) {
        String[] sheetNames = { "Contract", "Bill To Address", "Install At Address", "Ship To" };
        for (String name : sheetNames) {
          XSSFSheet sheet = book.getSheet(name);
          LOG.debug("validating name 3 for sheet " + name);
          for (Row row : sheet) {
            if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
              String dept = "";
              String building = "";
              String floor = "";
              Cell cmrCell1 = row.getCell(7);
              if (cmrCell1 != null) {
                switch (cmrCell1.getCellTypeEnum()) {
                case STRING:
                  dept = cmrCell1.getStringCellValue();
                  break;
                case NUMERIC:
                  double nvalue = cmrCell1.getNumericCellValue();
                  if (nvalue > 0) {
                    dept = "" + nvalue;
                  }
                  break;
                default:
                  continue;
                }
              }
              Cell cmrCell2 = row.getCell(8);
              if (cmrCell2 != null) {
                switch (cmrCell2.getCellTypeEnum()) {
                case STRING:
                  floor = cmrCell2.getStringCellValue();
                  break;
                case NUMERIC:
                  double nvalue = cmrCell2.getNumericCellValue();
                  if (nvalue > 0) {
                    floor = nvalue + "";
                  }
                  break;
                default:
                  continue;
                }
              }
              Cell cmrCell3 = row.getCell(9);
              if (cmrCell3 != null) {
                switch (cmrCell3.getCellTypeEnum()) {
                case STRING:
                  building = cmrCell3.getStringCellValue();
                  break;
                case NUMERIC:
                  double nvalue = cmrCell3.getNumericCellValue();
                  if (nvalue > 0) {
                    building = nvalue + "";
                  }
                  break;
                default:
                  continue;
                }
              }
              String name3 = "";
              if (StringUtils.isNotBlank(dept) && !StringUtils.equals(dept, "@")) {
                name3 += dept;
                if (StringUtils.isNotBlank(building) && !StringUtils.equals(building, "@")) {
                  name3 += ", ";
                } else if (StringUtils.isNotBlank(floor) && !StringUtils.equals(floor, "@")) {
                  name3 += ", ";
                }
              }
              if (StringUtils.isNotBlank(building) && !StringUtils.equals(building, "@")) {
                name3 += building;
                if (StringUtils.isNotBlank(floor) && !StringUtils.equals(floor, "@")) {
                  name3 += ", ";
                }
              }
              if (StringUtils.isNotBlank(floor) && !StringUtils.equals(floor, "@")) {
                name3 += floor;
              }
              if (name3.length() > 30) {
                LOG.debug("Total computed length of building, department and floor should not exeed 30. Sheet: " + name + " Row: " + row.getRowNum());
                TemplateValidation error = new TemplateValidation(name);
                error.addError(row.getRowNum(), "building", "Total computed length of building, department and floor should not exeed 30");
                validations.add(error);
              }
            }
          }
        }
        HashMap<String, String> hwFlagMap = new HashMap<>();
        for (TemplateTab tab : this.tabs) {
          validations.add(tab.validateSwiss(entityManager, book, country, maxRows, hwFlagMap));
        }
      } else if (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, country)) {
        LegacyDirectUtil.validateMassUpdateTemplateDupFills(validations, book, maxRows, country);
        for (TemplateTab tab : this.tabs) {
          validations.add(tab.validate(entityManager, book, country, maxRows));
        }
      } else if (ATUtil.isCountryATEnabled(entityManager, country)) {// CMR-800
        String[] sheetNames = { "Sold To", "Mail to", "Bill To", "Ship To", "Install At" };// CMR-2065
                                                                                    // installing
                                                                                    // change
                                                                                    // to
                                                                                    // Sold
                                                                                    // To
        for (String name : sheetNames) {
          XSSFSheet sheet = book.getSheet(name);
          LOG.debug("validating name 3 for sheet " + name);
          for (Row row : sheet) {
            if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
              String dept = "";
              String building = "";
              String floor = "";
              Cell cmrCell1 = row.getCell(7);
              if (cmrCell1 != null) {
                switch (cmrCell1.getCellTypeEnum()) {
                case STRING:
                  dept = cmrCell1.getStringCellValue();
                  break;
                case NUMERIC:
                  double nvalue = cmrCell1.getNumericCellValue();
                  if (nvalue > 0) {
                    dept = "" + nvalue;
                  }
                  break;
                default:
                  continue;
                }
              }
              Cell cmrCell2 = row.getCell(8);
              if (cmrCell2 != null) {
                switch (cmrCell2.getCellTypeEnum()) {
                case STRING:
                  floor = cmrCell2.getStringCellValue();
                  break;
                case NUMERIC:
                  double nvalue = cmrCell2.getNumericCellValue();
                  if (nvalue > 0) {
                    floor = nvalue + "";
                  }
                  break;
                default:
                  continue;
                }
              }
              Cell cmrCell3 = row.getCell(9);
              if (cmrCell3 != null) {
                switch (cmrCell3.getCellTypeEnum()) {
                case STRING:
                  building = cmrCell3.getStringCellValue();
                  break;
                case NUMERIC:
                  double nvalue = cmrCell3.getNumericCellValue();
                  if (nvalue > 0) {
                    building = nvalue + "";
                  }
                  break;
                default:
                  continue;
                }
              }
              String name3 = "";
              if (StringUtils.isNotBlank(dept) && !StringUtils.equals(dept, "@")) {
                name3 += dept;
                if (StringUtils.isNotBlank(building) && !StringUtils.equals(building, "@")) {
                  name3 += ", ";
                } else if (StringUtils.isNotBlank(floor) && !StringUtils.equals(floor, "@")) {
                  name3 += ", ";
                }
              }
              if (StringUtils.isNotBlank(building) && !StringUtils.equals(building, "@")) {
                name3 += building;
                if (StringUtils.isNotBlank(floor) && !StringUtils.equals(floor, "@")) {
                  name3 += ", ";
                }
              }
              if (StringUtils.isNotBlank(floor) && !StringUtils.equals(floor, "@")) {
                name3 += floor;
              }
              if (name3.length() > 30) {
                LOG.debug("Total computed length of building, department and floor should not exeed 30. Sheet: " + name + " Row: " + row.getRowNum());
                TemplateValidation error = new TemplateValidation(name);
                error.addError(row.getRowNum(), "building", "Total computed length of building, department and floor should not exeed 30");
                validations.add(error);
              }
            }
          }
        }
        HashMap<String, String> hwFlagMap = new HashMap<>();
        for (TemplateTab tab : this.tabs) {
          validations.add(tab.validateAT(entityManager, book, country, maxRows, hwFlagMap));
        }
      } else {
        for (TemplateTab tab : this.tabs) {
          validations.add(tab.validate(entityManager, book, country, maxRows));
        }
      }
      return validations;
    } finally {
      book.close();
    }
  }

  /**
   * Merges the validations to the excel workbook denoted by the parameter is
   * 
   * @param validations
   *          - list of validations to merge
   * @param is
   *          - the input stream containing the excel file
   * @param os
   *          - the outputstream where to write the merged excel
   * @throws IOException
   */
  @SuppressWarnings("deprecation")
  public void merge(List<TemplateValidation> validations, InputStream is, OutputStream os, int maxRows) throws IOException {
    XSSFWorkbook book = new XSSFWorkbook(is);
    try {
      XSSFSheet sheet = null;
      XSSFRow row = null;
      XSSFCell cell = null;
      int errorColIndex = -1;
      XSSFCellStyle errorStyle = book.createCellStyle();
      errorStyle.setFillForegroundColor(new XSSFColor(new Color(252, 228, 214)));
      errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      errorStyle.setWrapText(true);

      XSSFCellStyle normalStyle = book.createCellStyle();
      normalStyle.setFillPattern(FillPatternType.NO_FILL);
      normalStyle.setWrapText(true);

      for (TemplateValidation validation : validations) {
        if (validation.getRows().size() > 0) {
          LOG.trace("Merging error messages for " + validation.getTabName());
          sheet = book.getSheet(validation.getTabName());
          if (sheet != null) {
            row = sheet.getRow(0);
            if (row == null) {
              // no row? must be invalid sheet
              continue;
            }

            // determine the error column
            for (int colIndex = 0; colIndex < 50; colIndex++) {
              cell = row.getCell(colIndex);
              if (cell == null || (cell.getCellTypeEnum() == CellType.STRING && StringUtils.isEmpty(cell.getStringCellValue()))
                  || (cell.getCellTypeEnum() == CellType.NUMERIC && cell.getNumericCellValue() <= 0)
                  || (cell.getCellTypeEnum() == CellType.STRING && "ERRORS".equalsIgnoreCase(cell.getStringCellValue()))) {
                // found the error col
                if (cell == null) {
                  cell = row.createCell(colIndex);
                }
                cell.setCellValue("Errors");
                cell.setCellStyle(normalStyle);
                errorColIndex = colIndex;
                break;
              }
            }
            if (errorColIndex > 0) {
              sheet.setColumnWidth(errorColIndex, ERROR_COL_WIDTH);
              // now supply the error messages
              List<Integer> errorRows = new ArrayList<Integer>();
              for (ValidationRow validationRow : validation.getRows()) {
                if (!validationRow.isSuccess()) {
                  errorRows.add(validationRow.getRowNumber());
                  row = sheet.getRow(validationRow.getRowNumber());
                  if (row != null) {
                    cell = row.getCell(errorColIndex);
                    if (cell == null) {
                      cell = row.createCell(errorColIndex);
                    }
                    cell.setCellValue(validationRow.getError());
                    cell.setCellStyle(errorStyle);
                  }
                  // apply error style to whole row
                  for (int i = 0; i < errorColIndex; i++) {
                    cell = row.getCell(i);
                    if (cell == null) {
                      cell = row.createCell(i);
                    }
                    cell.setCellStyle(errorStyle);
                  }
                }
              }
              // now clear the line with no errors
              for (int rowIndex = 1; rowIndex < maxRows; rowIndex++) {
                if (!errorRows.contains(rowIndex)) {
                  row = sheet.getRow(rowIndex);
                  System.out.println("Applying default to row " + rowIndex + " of sheet " + sheet.getSheetName());
                  if (row != null) {
                    // apply default style to whole row
                    for (int i = 0; i < errorColIndex; i++) {
                      cell = row.getCell(i);
                      if (cell == null) {
                        cell = row.createCell(i);
                      }
                      cell.setCellStyle(normalStyle);
                    }
                  } else {
                    break;
                  }
                }
              }
            }
          }
        }
      }

      book.write(os);
    } finally {
      book.close();
    }
  }

  /**
   * Clones the input stream, needed to avoid stream closing
   * 
   * @param is
   * @return
   * @throws IOException
   */
  public byte[] cloneStream(InputStream is) throws IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      IOUtils.copy(is, bos);
      return bos.toByteArray();
    }
  }

  /**
   * Generates a random password based on UUID
   * 
   * @return
   */
  protected String getRandomPassword() {
    UUID uuid = UUID.randomUUID();
    return uuid.toString().substring(0, 8);
  }

  public void addAll(List<TemplateTab> tabs) {
    this.tabs.addAll(tabs);
  }

  public void add(TemplateTab tab) {
    this.tabs.add(tab);
  }

  public List<TemplateTab> getTabs() {
    return tabs;
  }

  public void setTabs(List<TemplateTab> tabs) {
    this.tabs = tabs;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
