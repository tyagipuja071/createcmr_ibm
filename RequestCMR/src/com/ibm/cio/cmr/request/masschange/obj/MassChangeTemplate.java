/**
 * 
 */
package com.ibm.cio.cmr.request.masschange.obj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.util.geo.FranceUtil;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation.ValidationRow;
import com.ibm.cio.cmr.request.util.IERPRequestUtils;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.at.ATUtil;
import com.ibm.cio.cmr.request.util.geo.impl.FranceHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
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
  private static final int CMR_ROW_NO = 1;

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

  private boolean isRowValid(Row row) {
    // if row is empty, skip
    DataFormatter df = new DataFormatter();
    boolean hasContents = false;
    for (Cell cell : row) {
      String val = df.formatCellValue(cell);
      if (val != null && !StringUtils.isEmpty(val.trim())) {
        hasContents = true;
        break;
      }
    }
    return hasContents;
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
    ZipSecureFile.setMinInflateRatio(0);
    XSSFWorkbook book = new XSSFWorkbook(is);
    try {
      List<TemplateValidation> validations = new ArrayList<TemplateValidation>();
      if (SwissUtil.isCountrySwissEnabled(entityManager, country)) {
        String[] sheetNames = { "Data", "Contract", "Bill To Address", "Install At Address", "Ship To" };
        for (String name : sheetNames) {
          XSSFSheet sheet = book.getSheet(name);
          XSSFCell currCell = null;
          if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
            String isuCd = "";
            String ctc = "";
            List<String> isuNotBlankCtc = Arrays.asList("36", "34", "32");
            int rowIndex = 0;
            for (Row row : sheet) {
              rowIndex = row.getRowNum();
              if (row.getRowNum() < 1) {
                continue;
              }
              TemplateValidation error = new TemplateValidation(name);
              currCell = (XSSFCell) row.getCell(5);
              isuCd = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(6);
              ctc = validateColValFromCell(currCell);

              isuCd = !StringUtils.isBlank(isuCd) ? isuCd.substring(0, 2) : "";
              if ((StringUtils.isNotBlank(isuCd) && StringUtils.isBlank(ctc)) || (StringUtils.isNotBlank(ctc) && StringUtils.isBlank(isuCd))) {
                LOG.trace("The row " + (rowIndex + 1) + ":Note that both ISU and CTC value needs to be filled..");
                error.addError((rowIndex + 1), "Data Tab", ":Please fill both ISU and CTC value.<br>");
              } else if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
                if (StringUtils.isBlank(ctc) || !"Q".contains(ctc)) {
                  LOG.trace("The row " + (rowIndex + 1)
                      + ":Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((rowIndex + 1), "Client Tier",
                      ":Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
                if (StringUtils.isBlank(ctc) || !"Y".contains(ctc)) {
                  LOG.trace(
                      "The row " + (rowIndex + 1) + "Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((rowIndex + 1), "Client Tier",
                      ":Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
                if (StringUtils.isBlank(ctc) || !"T".contains(ctc)) {
                  LOG.trace("The row " + (rowIndex + 1)
                      + ":Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((rowIndex + 1), "Client Tier",
                      ":Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if ((!StringUtils.isBlank(isuCd) && !("34".equals(isuCd) || "32".equals(isuCd) || "36".equals(isuCd)))
                  && !"@".equalsIgnoreCase(ctc)) {
                LOG.trace("Client Tier should be '@' for the selected ISU Code.");
                error.addError((rowIndex + 1), "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isuCd + ".<br>");
              }
              validations.add(error);
            }
          } else {
            LOG.debug("validating name 3 for sheet " + name);
            for (Row row : sheet) {
              TemplateValidation error = new TemplateValidation(name);
              if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
                String dept = "";
                String building = "";
                String floor = "";
                Cell cmrCell1 = row.getCell(7);
                if (cmrCell1 != null) {
                  switch (cmrCell1.getCellType()) {
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
                  switch (cmrCell2.getCellType()) {
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
                  switch (cmrCell3.getCellType()) {
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
                  LOG.debug("Total computed length of building, department and floor should not exeed 30. Sheet: " + name + " Row: "
                      + (row.getRowNum() + 1));
                  error.addError((row.getRowNum() + 1), "building", "Total computed length of building, department and floor should not exeed 30");
                  validations.add(error);
                }
              }
            }
          }
        }
        HashMap<String, String> hwFlagMap = new HashMap<>();
        for (TemplateTab tab : this.tabs) {
          validations.add(tab.validateSwiss(entityManager, book, country, maxRows, hwFlagMap));
        }
      } else if (IERPRequestUtils.isCountryDREnabled(entityManager, country) || LAHandler.isLACountry(country)) {
        if (SystemLocation.GERMANY.equals(country)) {
          XSSFSheet dataSheet = book.getSheet("Data");
          int cmrRecords = 0;
          for (Row cmrRow : dataSheet) {
            if (cmrRow.getRowNum() >= CMR_ROW_NO) {
              if (isRowValid(cmrRow)) {
                cmrRecords++;
              }
            }
          }
          // validate if has CMR No. Present in DATA Sheet
          if (cmrRecords <= 1) {
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_NO_DATA);
          }
        }
        IERPRequestUtils.validateMassUpdateTemplateDupFills(validations, book, maxRows, country);
        for (TemplateTab tab : this.tabs) {
          validations.add(tab.validate(entityManager, book, country, maxRows));
        }
      } else if (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, country)) {
        LegacyDirectUtil.checkIsraelMassTemplate(this.tabs, book, country);
        // CREATCMR-2673
        if (SystemLocation.DENMARK.equals(country) || SystemLocation.NORWAY.equals(country) || SystemLocation.SWEDEN.equals(country)
            || SystemLocation.FINLAND.equals(country)) {
          LegacyDirectUtil.checkNordxlMassTemplate(this.tabs, book, country);
        }
        LegacyDirectUtil.validateMassUpdateTemplateDupFills(validations, book, maxRows, country);
        for (TemplateTab tab : this.tabs) {
          validations.add(tab.validate(entityManager, book, country, maxRows));
        }
      } else if (FranceUtil.isCountryFREnabled(entityManager, country)) {
        FranceHandler.validateFRMassUpdateTemplateDupFills(validations, book, maxRows, country);
        for (TemplateTab tab : this.tabs) {
          validations.add(tab.validate(entityManager, book, country, maxRows));
        }

      } else if (ATUtil.isCountryATEnabled(entityManager, country)) {// CMR-800
        String[] sheetNames = { "Data", "Sold To", "Mail to", "Bill To", "Ship To", "Install At" };// CMR-2065
        // installing
        // change
        // to
        // Sold
        // To
        for (String name : sheetNames) {
          XSSFSheet sheet = book.getSheet(name);
          XSSFCell currCell = null;
          if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
            String isuCd = "";
            String ctc = "";
            List<String> isuNotBlankCtc = Arrays.asList("36", "34", "32");
            for (Row row : sheet) {
              TemplateValidation error = new TemplateValidation(name);
              int rowIndex = row.getRowNum();
              if (row.getRowNum() < 1) {
                continue;
              }
              currCell = (XSSFCell) row.getCell(5);
              isuCd = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(6);
              ctc = validateColValFromCell(currCell);
              isuCd = !StringUtils.isBlank(isuCd) ? isuCd.substring(0, 2) : "";
              if ((StringUtils.isNotBlank(isuCd) && StringUtils.isBlank(ctc)) || (StringUtils.isNotBlank(ctc) && StringUtils.isBlank(isuCd))) {
                LOG.trace("The row " + (rowIndex + 1) + ":Note that both ISU and CTC value needs to be filled..");
                error.addError((rowIndex + 1), "Data Tab", ":Please fill both ISU and CTC value.<br>");
              } else if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
                if (StringUtils.isBlank(ctc) || !"Q".contains(ctc)) {
                  LOG.trace("The row " + (rowIndex + 1)
                      + ":Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((rowIndex + 1), "Client Tier",
                      ":Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
                if (StringUtils.isBlank(ctc) || !"Y".contains(ctc)) {
                  LOG.trace(
                      "The row " + (rowIndex + 1) + "Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((rowIndex + 1), "Client Tier",
                      ":Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
                if (StringUtils.isBlank(ctc) || !"T".contains(ctc)) {
                  LOG.trace("The row " + (rowIndex + 1)
                      + ":Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((rowIndex + 1), "Client Tier",
                      ":Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if ((!StringUtils.isBlank(isuCd) && !("34".equals(isuCd) || "32".equals(isuCd) || "36".equals(isuCd)))
                  && !"@".equalsIgnoreCase(ctc)) {
                LOG.trace("Client Tier should be '@' for the selected ISU Code.");
                error.addError((rowIndex + 1), "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isuCd + ".<br>");
              }
              validations.add(error);
            }
          } else {
            LOG.debug("validating name 3 for sheet " + name);
            for (Row row : sheet) {
              TemplateValidation error = new TemplateValidation(name);
              if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
                Cell cmrCell1 = row.getCell(4);
                if (cmrCell1 != null) {
                  String name3 = "";
                  switch (cmrCell1.getCellType()) {
                  case STRING:
                    name3 = cmrCell1.getStringCellValue();
                    break;
                  case NUMERIC:
                    double nvalue = cmrCell1.getNumericCellValue();
                    if (nvalue > 0) {
                      name3 = "" + nvalue;
                      break;
                    }
                  default:
                    continue;
                  }
                  if (name3.length() > 30) {
                    LOG.debug("Total computed length of name3 should not exeed 30. Sheet: " + name + ", Row: " + (row.getRowNum() + 1) + ", Name3:"
                        + name3);
                    error.addError((row.getRowNum() + 1), "building", "Total computed length of customer name3 should not exeed 30");
                    validations.add(error);
                  }
                }
              }
              // String dept = "";
              // String building = "";
              // String floor = "";
              // Cell cmrCell1 = row.getCell(7);
              // if (cmrCell1 != null) {
              // switch (cmrCell1.getCellTypeEnum()) {
              // case STRING:
              // dept = cmrCell1.getStringCellValue();
              // break;
              // case NUMERIC:
              // double nvalue = cmrCell1.getNumericCellValue();
              // if (nvalue > 0) {
              // dept = "" + nvalue;
              // }
              // break;
              // default:
              // continue;
              // }
              // }
              // Cell cmrCell2 = row.getCell(8);
              // if (cmrCell2 != null) {
              // switch (cmrCell2.getCellTypeEnum()) {
              // case STRING:
              // floor = cmrCell2.getStringCellValue();
              // break;
              // case NUMERIC:
              // double nvalue = cmrCell2.getNumericCellValue();
              // if (nvalue > 0) {
              // floor = nvalue + "";
              // }
              // break;
              // default:
              // continue;
              // }
              // }
              // Cell cmrCell3 = row.getCell(9);
              // if (cmrCell3 != null) {
              // switch (cmrCell3.getCellTypeEnum()) {
              // case STRING:
              // building = cmrCell3.getStringCellValue();
              // break;
              // case NUMERIC:
              // double nvalue = cmrCell3.getNumericCellValue();
              // if (nvalue > 0) {
              // building = nvalue + "";
              // }
              // break;
              // default:
              // continue;
              // }
              // }
              //
              // if (StringUtils.isNotBlank(dept) && !StringUtils.equals(dept,
              // "@")) {
              // name3 += dept;
              // if (StringUtils.isNotBlank(building) &&
              // !StringUtils.equals(building, "@")) {
              // name3 += ", ";
              // } else if (StringUtils.isNotBlank(floor) &&
              // !StringUtils.equals(floor, "@")) {
              // name3 += ", ";
              // }
              // }
              // if (StringUtils.isNotBlank(building) &&
              // !StringUtils.equals(building, "@")) {
              // name3 += building;
              // if (StringUtils.isNotBlank(floor) && !StringUtils.equals(floor,
              // "@")) {
              // name3 += ", ";
              // }
              // }
              // if (StringUtils.isNotBlank(floor) && !StringUtils.equals(floor,
              // "@")) {
              // name3 += floor;
              // }
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
      byte[] rgb = new byte[3];
      rgb[0] = (byte) 252;
      rgb[1] = (byte) 228;
      rgb[2] = (byte) 214;
      XSSFColor color = new XSSFColor(rgb, new DefaultIndexedColorMap());
      errorStyle.setFillForegroundColor(color);
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
              if (cell == null || (cell.getCellType() == CellType.STRING && StringUtils.isEmpty(cell.getStringCellValue()))
                  || (cell.getCellType() == CellType.NUMERIC && cell.getNumericCellValue() <= 0)
                  || (cell.getCellType() == CellType.STRING && "ERRORS".equalsIgnoreCase(cell.getStringCellValue()))) {
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

  protected static String validateColValFromCell(XSSFCell cell) {
    String colVal = "";
    if (cell != null) {
      switch (cell.getCellType()) {
      case STRING:
        colVal = cell.getStringCellValue();
        break;
      case NUMERIC:
        double nvalue = cell.getNumericCellValue();
        if (nvalue >= 0) {
          colVal = "" + nvalue;
        }

        BigDecimal bd = new BigDecimal(colVal);
        long val = bd.longValue();
        colVal = Long.toString(val);
        break;
      default:
        break;
      }
    }
    return colVal;
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

  public List<TemplateValidation> validate(EntityManager entityManager, InputStream is, Admin admin, String country, int maxRows) throws Exception {
    ZipSecureFile.setMinInflateRatio(0);
    XSSFWorkbook book = new XSSFWorkbook(is);
    try {
      List<TemplateValidation> validations = new ArrayList<TemplateValidation>();
      if (LAHandler.isLACountry(country)) {
        IERPRequestUtils.validateMassUpdateTemplateDupFills(validations, book, maxRows, country, admin);
        for (TemplateTab tab : this.tabs) {
          validations.add(tab.validate(entityManager, book, country, maxRows));
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

}
