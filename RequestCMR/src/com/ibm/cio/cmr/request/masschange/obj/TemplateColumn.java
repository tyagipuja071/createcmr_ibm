/**
 *
 */
package com.ibm.cio.cmr.request.masschange.obj;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.DropDownService;
import com.ibm.cio.cmr.request.util.IERPRequestUtils;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;

/**
 * Represents a column on the mass change template. The column writes the
 * correct basic validations during template generation, and performs the same
 * validation during template validation
 *
 * @author JeffZAMORA
 *
 */
public class TemplateColumn {

  private static final Logger LOG = Logger.getLogger(TemplateColumn.class);
  private static final int DEFAULT_WIDTH = 256 * 30;

  // node properties
  private String label;
  private int width;
  private int length;

  private TemplateDataType type;
  private String typeCode;

  // attributes
  private boolean number;
  private boolean exactLength;
  private Set<String> values = new LinkedHashSet<String>();
  private String lovId;
  private String bdsId;
  private String dbColumn;

  /**
   * Writes this column to the current {@link XSSFSheet} represented by the
   * {@link TemplateTab} object. The special validations for data type, length,
   * and list of values restrictions will be added to the column
   *
   * @param entityManager
   * @param country
   * @param book
   * @param sheet
   * @param columnStyle
   * @param tab
   * @param colNo
   * @param maxRows
   */
  protected void writeTo(EntityManager entityManager, String country, XSSFWorkbook book, XSSFSheet control, XSSFSheet sheet,
      XSSFCellStyle columnStyle, TemplateTab tab, int colNo, int maxRows) {

    // create the column and set styles and widths
    XSSFRow row = sheet.getRow(0);
    if (row == null) {
      row = sheet.createRow(0);
    }
    sheet.setColumnWidth(colNo, this.width > 0 ? 256 * this.width : DEFAULT_WIDTH);

    // generate the label
    XSSFCell columnCell = row.getCell(colNo);
    if (columnCell == null) {
      columnCell = row.createCell(colNo);
    }
    columnCell.setCellStyle(columnStyle);
    columnCell.setCellValue(this.label);

    // check any validations
    XSSFDataValidationHelper helper = new XSSFDataValidationHelper(sheet);
    XSSFDataValidationConstraint constraint = null;
    String errorMsg = null;
    XSSFDataValidation validation = null;
    if (this.number) {
      // add number validator
      if (this.length > 0) {
        // do a range of values
        String min = 0 + "";
        String max = StringUtils.leftPad("", this.length, '9');
        constraint = (XSSFDataValidationConstraint) helper.createNumericConstraint(ValidationType.INTEGER, OperatorType.BETWEEN, min, max);
        errorMsg = "Please input only numbers from " + min + " to " + max + ".";
      } else {
        // generic values, use 9 as numer length limit
        String min = 0 + "";
        String max = StringUtils.leftPad("", 9, '9');
        constraint = (XSSFDataValidationConstraint) helper.createNumericConstraint(ValidationType.INTEGER, OperatorType.BETWEEN, min, max);
        errorMsg = "Please input numbers only.";
      }
    } else if (this.values.size() > 0 || !StringUtils.isBlank(this.lovId) || !StringUtils.isBlank(this.bdsId)) {
      // list of values
      String[] listOfValues = null;
      if (this.values.size() > 0) {
        // specified on the XML template
        listOfValues = this.values.toArray(new String[0]);
        constraint = writeToControl(control, helper, listOfValues);
        errorMsg = "Please select values from the list.";
      } else if (!StringUtils.isBlank(this.lovId)) {
        // retrieved from LOV
        List<String> lovs = null;

        if (IERPRequestUtils.isCountryDREnabled(entityManager, country) || LAHandler.isLACountry(country)) {
          lovs = IERPRequestUtils.getLovsDR(entityManager, this.lovId, country, true);
          lovs = IERPRequestUtils.addSpecialCharToLovDR(lovs, country, true, this.lovId);
        } else if (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, country)) {
          lovs = getLovs(entityManager, this.lovId, country, true);
          lovs = LegacyDirectUtil.addSpecialCharToLov(lovs, country, true, this.lovId);
        } else {
          lovs = getLovs(entityManager, this.lovId, country, false);
        }

        if (lovs.size() > 0) {
          listOfValues = lovs.toArray(new String[0]);
          constraint = writeToControl(control, helper, listOfValues);
          errorMsg = "Please select values from the list.";
        }
      } else if (!StringUtils.isBlank(this.bdsId)) {
        // retrieved from BDS
        List<String> lovs = null;

        if (IERPRequestUtils.isCountryDREnabled(entityManager, country) || LAHandler.isLACountry(country)) {
          lovs = IERPRequestUtils.getBDSChoicesDR(entityManager, this.bdsId, country, true);
          lovs = IERPRequestUtils.addSpecialCharToLovDR(lovs, country, true, this.bdsId);
        } else if (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, country)) {
          lovs = getBDSChoices(entityManager, this.bdsId, country, true);
          lovs = LegacyDirectUtil.addSpecialCharToLov(lovs, country, true, this.bdsId);
        } else {
          lovs = getBDSChoices(entityManager, this.bdsId, country, false);
        }

        if (lovs.size() > 0) {
          listOfValues = lovs.toArray(new String[0]);
          constraint = writeToControl(control, helper, listOfValues);
          errorMsg = "Please select values from the list.";
        }
      }
    } else if (this.length > 0) {
      if (this.exactLength) {
        constraint = (XSSFDataValidationConstraint) helper.createTextLengthConstraint(OperatorType.EQUAL, this.length + "", null);
        errorMsg = "Value should be exactly " + this.length + " characters long.";
      } else {
        constraint = (XSSFDataValidationConstraint) helper.createTextLengthConstraint(OperatorType.LESS_OR_EQUAL, this.length + "", null);
        errorMsg = "Maximum length of the value is " + this.length + " characters.";
      }
    }

    if (constraint != null) {
      // add the validation. both validation and note will be added to the
      // column
      LOG.trace("Adding validation: " + errorMsg + " to " + this.label);
      validation = (XSSFDataValidation) helper.createValidation(constraint, new CellRangeAddressList(1, maxRows, colNo, colNo));
      validation.setShowErrorBox(true);
      validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
      validation.createErrorBox("Invalid Value", errorMsg);

      validation.setShowPromptBox(true);
      validation.createPromptBox("Note:", errorMsg);

      sheet.addValidationData(validation);
    }

  }

  @SuppressWarnings("deprecation")
  public void validate(EntityManager entityManager, TemplateValidation validation, XSSFWorkbook book, XSSFSheet sheet, String country, int colNo,
      int maxRows) {
    XSSFRow row = null;
    XSSFCell currCell = null;
    String value = null;
    DecimalFormat formatter = new DecimalFormat("#");
    // String cbCity = "";
    // String localCity = "";
    // String cbPostal = "";
    // String localPostal = "";
    // List<String> markedColumnsAsDups = new ArrayList<String>();

    for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
      row = sheet.getRow(rowIndex);
      if (row == null) {
        return; // stop immediately when row is blank
      }
      // iterate all the rows and check each column value
      currCell = row.getCell(colNo);
      if (currCell == null) {
        continue; // next value
      }

      switch (currCell.getCellType()) {
      case STRING:
        value = currCell.getStringCellValue();
        break;
      case NUMERIC:
        double nvalue = currCell.getNumericCellValue();
        if (nvalue > 0) {
          value = formatter.format(nvalue);
        }
        break;
      default:
        continue;
      }
      if (StringUtils.isBlank(value)) {
        continue;
      }
      System.err.println(" - " + this.label + " value: " + value);
      if (this.length > 0 && value.length() > this.length) {
        LOG.trace("Field Length Error for " + this.label);
        validation.addError(rowIndex, this.label, "Value exceeds maximum length of " + this.length);
      } else if (this.exactLength && value.length() != this.length) {
        LOG.trace("Exact Field Length Error for " + this.label);
        validation.addError(rowIndex, this.label, "Value length should be " + this.length + " characters.");
      }

      // if ("Cross Border City".equals(this.label)) {
      // cbCity = value;
      // }
      //
      // if ("Local City".equals(this.label)) {
      // localCity = value;
      // }
      //
      // if ("Cross Border Postal Code".equals(this.label)) {
      // cbPostal = value;
      // }
      //
      // if ("Local Postal Code".equals(this.label)) {
      // localPostal = value;
      // }
      //
      // if ("Cross Border City".equals(this.label)) {
      // LOG.debug(">> cbCity > " + cbCity);
      // LOG.debug(">> localCity > " + localCity);
      // if (!StringUtils.isEmpty(cbCity) && !StringUtils.isEmpty(localCity)) {
      // LOG.trace("Cross Border City and Local City must not be populated at
      // the same time. If one is populated, the other must be empty. >> "
      // + this.label);
      // validation.addError(rowIndex, this.label,
      // "Cross Border City and Local City must not be populated at the same
      // time. If one is populated, the other must be empty.");
      // }
      // }
      //
      // if ("Cross Border Postal Code".equals(this.label)) {
      // LOG.debug(">> cbPostal > " + cbPostal);
      // LOG.debug(">> localPostal > " + localPostal);
      //
      // if (!StringUtils.isEmpty(cbPostal) &&
      // !StringUtils.isEmpty(localPostal)) {
      // LOG.trace("Cross Border Postal Code and Local Postal Code must not be
      // populated at the same time. If one is populated, the other must be
      // empty. >>"
      // + this.label);
      // validation.addError(rowIndex, this.label,
      // "Cross Border Postal Code and Local Postal Code must not be populated
      // at the same time. If one is populated, the other must be empty.");
      // }
      // }

      // DTN: Defect 1898300: UKI - mass updates - addresses
      /*
       * Adding a check that if any of the address lines values that are set as
       * either value and both are filled out, it will throw an error message
       * that both can not be filled out.
       */
      // EMEAHandler emea = new EMEAHandler();
      // if (emea.isTemplateFieldForDualMarked(this.label, false) &&
      // !StringUtils.isEmpty(value)) {
      // markedColumnsAsDups.add("X");
      // }
      //
      // if (markedColumnsAsDups != null && markedColumnsAsDups.size() > 1) {
      // LOG.trace("There are fields dually marked on the template. This will be
      // returned as an error");
      // validation.addError(rowIndex, this.label,
      // "There are fields dually marked on the template. This will be returned
      // as an error");
      // }
    }
  }

  @SuppressWarnings("deprecation")
  public void validateSwiss(EntityManager entityManager, TemplateValidation validation, XSSFWorkbook book, XSSFSheet sheet, String country, int colNo,
      int maxRows, HashMap<String, String> hwFlagMap) {
    XSSFRow row = null;
    XSSFCell currCell = null;
    String value = null;
    DecimalFormat formatter = new DecimalFormat("#");
    String cbCity = "";
    String localCity = "";
    String cbPostal = "";
    String localPostal = "";
    String hwInstlMstrFlg = "";

    for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
      String CMRNo = "";
      row = sheet.getRow(rowIndex);
      if (row == null) {
        return; // stop immediately when row is blank
      }
      // iterate all the rows and check each column value
      currCell = row.getCell(colNo);
      if (currCell == null) {
        continue; // next value
      }

      switch (currCell.getCellType()) {
      case STRING:
        value = currCell.getStringCellValue();
        break;
      case NUMERIC:
        double nvalue = currCell.getNumericCellValue();
        if (nvalue > 0) {
          value = formatter.format(nvalue);
        }
        break;
      default:
        continue;
      }
      if (StringUtils.isBlank(value)) {
        continue;
      }
      System.err.println(" - " + this.label + " value: " + value);
      if (this.length > 0 && value.length() > this.length) {
        LOG.trace("Field Length Error for " + this.label);
        validation.addError(rowIndex, this.label, "Value exceeds maximum length of " + this.length);
      } else if (this.exactLength && value.length() != this.length) {
        LOG.trace("Exact Field Length Error for " + this.label);
        validation.addError(rowIndex, this.label, "Value length should be " + this.length + " characters.");
      }

      if ("Cross Border City".equals(this.label)) {
        cbCity = value;
      }

      if ("Local City".equals(this.label)) {
        localCity = value;
      }

      if ("Cross Border Postal Code".equals(this.label)) {
        cbPostal = value;
      }

      if ("Local Postal Code".equals(this.label)) {
        localPostal = value;
      }

      if ("Hardware Master".equals(this.label)) {
        hwInstlMstrFlg = value;
      }

      if ("Cross Border City".equals(this.label)) {
        LOG.debug(">> cbCity > " + cbCity);
        LOG.debug(">> localCity > " + localCity);
        if (!StringUtils.isEmpty(cbCity) && !StringUtils.isEmpty(localCity)) {
          LOG.trace("Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty. >> "
              + this.label);
          validation.addError(rowIndex, this.label,
              "Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty.");
        }
      }

      if ("Cross Border Postal Code".equals(this.label)) {
        LOG.debug(">> cbPostal > " + cbPostal);
        LOG.debug(">> localPostal > " + localPostal);

        if (!StringUtils.isEmpty(cbPostal) && !StringUtils.isEmpty(localPostal)) {
          LOG.trace(
              "Cross Border Postal Code and Local Postal Code must not be populated at the same time. If one is populated, the other must be empty. >>"
                  + this.label);
          validation.addError(rowIndex, this.label,
              "Cross Border Postal Code and Local Postal Code must not be populated at the same time. If one is populated, the other must be empty.");
        }
      }

      if ("Hardware Master".equals(this.label)) {
        LOG.debug(">> hwInstlMstrFlg > " + hwInstlMstrFlg);
        XSSFCell cmrCell = row.getCell(0);
        if (cmrCell != null) {
          switch (cmrCell.getCellType()) {
          case STRING:
            CMRNo = cmrCell.getStringCellValue();
            break;
          case NUMERIC:
            double nvalue = cmrCell.getNumericCellValue();
            if (nvalue > 0) {
              CMRNo = formatter.format(nvalue).toString();
            }
            break;
          default:
            continue;
          }
        }
        if (StringUtils.contains(hwInstlMstrFlg, "Y | Y")) {
          if (hwFlagMap.containsKey(CMRNo)) {
            LOG.trace("Only One Contract or Install at address can be selected as Hardware Master. >>" + this.label);
            validation.addError(rowIndex, this.label, "Only One Contract or Install at address can be selected as Hardware Master. ");
          } else {
            hwFlagMap.put(CMRNo, hwInstlMstrFlg);
            if (!StringUtils.equals(sheet.getSheetName(), "Contract") && !StringUtils.equals(sheet.getSheetName(), "Install At Address")) {
              LOG.trace("Only One Contract or Install at address can be selected as Hardware Master. >>" + this.label);
              validation.addError(rowIndex, this.label, "Only One Contract or Install at address can be selected as Hardware Master. ");
            }

          }

        }
      }

    }
  }

  // CMR-800
  @SuppressWarnings("deprecation")
  public void validateAT(EntityManager entityManager, TemplateValidation validation, XSSFWorkbook book, XSSFSheet sheet, String country, int colNo,
      int maxRows, HashMap<String, String> hwFlagMap) {
    XSSFRow row = null;
    XSSFCell currCell = null;
    String value = null;
    DecimalFormat formatter = new DecimalFormat("#");
    String cbCity = "";
    String localCity = "";
    String cbPostal = "";
    String localPostal = "";
    String hwInstlMstrFlg = "";

    for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
      String CMRNo = "";
      row = sheet.getRow(rowIndex);
      if (row == null) {
        return; // stop immediately when row is blank
      }
      // iterate all the rows and check each column value
      currCell = row.getCell(colNo);
      if (currCell == null) {
        continue; // next value
      }

      switch (currCell.getCellType()) {
      case STRING:
        value = currCell.getStringCellValue();
        break;
      case NUMERIC:
        double nvalue = currCell.getNumericCellValue();
        if (nvalue > 0) {
          value = formatter.format(nvalue);
        }
        break;
      default:
        continue;
      }
      if (StringUtils.isBlank(value)) {
        continue;
      }
      System.err.println(" - " + this.label + " value: " + value);
      if (this.length > 0 && value.length() > this.length) {
        LOG.trace("Field Length Error for " + this.label);
        validation.addError(rowIndex, this.label, "Value exceeds maximum length of " + this.length);
      } else if (this.exactLength && value.length() != this.length) {
        LOG.trace("Exact Field Length Error for " + this.label);
        validation.addError(rowIndex, this.label, "Value length should be " + this.length + " characters.");
      }

      if ("Cross Border City".equals(this.label)) {
        cbCity = value;
      }

      if ("Local City".equals(this.label)) {
        localCity = value;
      }

      if ("Cross Border Postal Code".equals(this.label)) {
        cbPostal = value;
      }

      if ("Local Postal Code".equals(this.label)) {
        localPostal = value;
      }

      if ("Hardware Master".equals(this.label)) {
        hwInstlMstrFlg = value;
      }

      if ("Cross Border City".equals(this.label)) {
        LOG.debug(">> cbCity > " + cbCity);
        LOG.debug(">> localCity > " + localCity);
        if (!StringUtils.isEmpty(cbCity) && !StringUtils.isEmpty(localCity)) {
          LOG.trace("Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty. >> "
              + this.label);
          validation.addError(rowIndex, this.label,
              "Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty.");
        }
      }

      if ("Cross Border Postal Code".equals(this.label)) {
        LOG.debug(">> cbPostal > " + cbPostal);
        LOG.debug(">> localPostal > " + localPostal);

        if (!StringUtils.isEmpty(cbPostal) && !StringUtils.isEmpty(localPostal)) {
          LOG.trace(
              "Cross Border Postal Code and Local Postal Code must not be populated at the same time. If one is populated, the other must be empty. >>"
                  + this.label);
          validation.addError(rowIndex, this.label,
              "Cross Border Postal Code and Local Postal Code must not be populated at the same time. If one is populated, the other must be empty.");
        }
      }

      if ("Hardware Master".equals(this.label)) {
        LOG.debug(">> hwInstlMstrFlg > " + hwInstlMstrFlg);
        XSSFCell cmrCell = row.getCell(0);
        if (cmrCell != null) {
          switch (cmrCell.getCellType()) {
          case STRING:
            CMRNo = cmrCell.getStringCellValue();
            break;
          case NUMERIC:
            double nvalue = cmrCell.getNumericCellValue();
            if (nvalue > 0) {
              CMRNo = formatter.format(nvalue).toString();
            }
            break;
          default:
            continue;
          }
        }
        if (StringUtils.contains(hwInstlMstrFlg, "Y | Y")) {
          if (hwFlagMap.containsKey(CMRNo)) {
            LOG.trace("Only One Sold to or Mail to address can be selected as Hardware Master. >>" + this.label);
            validation.addError(rowIndex, this.label, "Only One Sold to or Mail to address can be selected as Hardware Master. ");
          } else {
            hwFlagMap.put(CMRNo, hwInstlMstrFlg);
            if (!StringUtils.equals(sheet.getSheetName(), "Contract") && !StringUtils.equals(sheet.getSheetName(), "Install At Address")) {
              LOG.trace("Only One Sold to or Mail to address can be selected as Hardware Master. >>" + this.label);
              validation.addError(rowIndex, this.label, "Only One Sold to or Mail to address can be selected as Hardware Master. ");
            }

          }

        }
      }

    }
  }

  /**
   * Writes the LOV to the control sheet, and sets the formula for the column
   *
   * @param control
   * @param helper
   * @param values
   * @return
   */
  private XSSFDataValidationConstraint writeToControl(XSSFSheet control, XSSFDataValidationHelper helper, String[] values) {
    printValues(Arrays.asList(values));
    XSSFRow row = control.getRow(0);
    XSSFCell cell = null;
    int colIndex = 0;
    if (row == null) {
      row = control.createRow(0);
    }
    for (int i = 0; i < 100; i++) {
      cell = row.getCell(i);
      if (cell == null || StringUtils.isEmpty(cell.getStringCellValue())) {
        colIndex = i;
        break;
      }
    }
    LOG.trace("Col Index for " + this.label + " is " + colIndex);

    // create label
    if (cell == null) {
      cell = row.createCell(colIndex);
      cell.setCellValue(this.label + " Values");
    }

    // add the values
    int rowIndex = 1;
    for (String value : values) {
      row = control.getRow(rowIndex);
      if (row == null) {
        row = control.createRow(rowIndex);
      }
      cell = row.getCell(colIndex);
      if (cell == null) {
        cell = row.createCell(colIndex);
      }
      cell.setCellValue(value);
      rowIndex++;
    }

    // now create the formula
    String colLabel = CellReference.convertNumToColString(colIndex);
    String formula = "Control!$" + colLabel + "$2:$" + colLabel + "$" + rowIndex;
    LOG.trace("Formula for " + this.label + " = " + formula);
    control.setColumnWidth(colIndex, DEFAULT_WIDTH);

    XSSFDataValidationConstraint validation = (XSSFDataValidationConstraint) helper.createFormulaListConstraint(formula);
    return validation;
  }

  /**
   * Retrieves the LOV list given the Field ID and Issuing Country
   *
   * @param entityManager
   * @param lovId
   * @param country
   * @return
   */
  private List<String> getLovs(EntityManager entityManager, String lovId, String country, boolean codeOnly) {
    List<String> choices = new ArrayList<String>();
    String sql = ExternalizedQuery.getSql("LOV");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("FIELD_ID", "##" + lovId);
    query.setParameter("CMR_CNTRY", country);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      for (Object[] result : results) {
        if (codeOnly) {
          choices.add((String) result[0]);
        } else {
          choices.add(result[0] + " | " + result[1]);
        }

      }
    }
    return choices;
  }

  /**
   * Retrieves the BDS choices given the Field ID and Issuing Country
   *
   * @param entityManager
   * @param bdsId
   * @param country
   * @return
   */
  private List<String> getBDSChoices(EntityManager entityManager, String bdsId, String country, boolean codeOnly) {
    ParamContainer params = new ParamContainer();

    if (country != null && bdsId != null && ("848".equalsIgnoreCase(country) || "618".equalsIgnoreCase(country)) && ("StateProv").equals(bdsId)) {
      String sql = ExternalizedQuery.getSql("QUERY.QUICK.GET_DEFAULT_COUNTRY");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CNTRY", country);
      Object[] results = query.getSingleResult();
      if (results != null) {
        String landedCountry = (String) results[0];
        params.addParam("landCntry", landedCountry);
      }
    }

    List<String> choices = new ArrayList<String>();
    DropDownService service = new DropDownService();
    PreparedQuery query = service.getBDSSql(bdsId, entityManager, params, country);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      for (Object[] result : results) {
        if (codeOnly) {
          choices.add((String) result[0]);
        } else {
          choices.add(result[0] + " | " + result[1]);
        }
      }
    }
    return choices;
  }

  /**
   * Logger of list of values, TRACE only
   *
   * @param list
   */
  private void printValues(Collection<String> list) {
    if (LOG.isTraceEnabled()) {
      StringBuilder sb = new StringBuilder();
      for (String val : list) {
        sb.append(sb.length() > 0 ? ", " : "");
        sb.append(val);
      }
      LOG.trace("Values: " + sb.toString());
    }
  }

  public void addValue(String value) {
    this.values.add(value);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public boolean isNumber() {
    return number;
  }

  public void setNumber(boolean number) {
    this.number = number;
  }

  public boolean isExactLength() {
    return exactLength;
  }

  public void setExactLength(boolean exactLength) {
    this.exactLength = exactLength;
  }

  public String getLovId() {
    return lovId;
  }

  public void setLovId(String lovId) {
    this.lovId = lovId;
  }

  public Set<String> getValues() {
    return values;
  }

  public void setValues(Set<String> values) {
    this.values = values;
  }

  public String getBdsId() {
    return bdsId;
  }

  public void setBdsId(String bdsId) {
    this.bdsId = bdsId;
  }

  public String getDbColumn() {
    return dbColumn;
  }

  public void setDbColumn(String dbColumn) {
    this.dbColumn = dbColumn;
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
