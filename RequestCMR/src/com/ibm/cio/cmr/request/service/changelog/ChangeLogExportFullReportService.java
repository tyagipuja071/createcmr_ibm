package com.ibm.cio.cmr.request.service.changelog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.service.system.RequesterStatService;
import com.ibm.cio.cmr.request.util.system.StatXLSConfig;

@Component
public class ChangeLogExportFullReportService {

  private static final Logger LOG = Logger.getLogger(RequesterStatService.class);

  private static List<StatXLSConfig> config1 = null;
  private static List<StatXLSConfig> config2_0 = null;
  private static List<StatXLSConfig> config2_1 = null;
  private static List<StatXLSConfig> config2_2 = null;
  private static List<StatXLSConfig> config2_3 = null;
  private static List<StatXLSConfig> config2_4 = null;

  public void exportToExcel(List<Object[]> rdcList, List<Object[]> legacyList, HttpServletResponse response)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    if (config1 == null) {
      initConfig1();
    }

    if (config2_0 == null) {
      initConfig2_0();
      initConfig2_1();
      initConfig2_2();
      initConfig2_3();
      initConfig2_4();
    }

    LOG.info("Exporting records to excel..");

    XSSFWorkbook report = new XSSFWorkbook();
    try {

      XSSFSheet sheet0 = report.createSheet("RDc");

      Drawing drawing0 = sheet0.createDrawingPatriarch();
      CreationHelper helper = report.getCreationHelper();

      XSSFFont bold = report.createFont();
      bold.setBold(true);
      bold.setFontHeight(10);
      XSSFCellStyle boldStyle = report.createCellStyle();
      boldStyle.setFont(bold);

      XSSFFont regular = report.createFont();
      regular.setFontHeight(11);
      XSSFCellStyle regularStyle = report.createCellStyle();
      regularStyle.setFont(regular);
      regularStyle.setWrapText(true);
      regularStyle.setVerticalAlignment(VerticalAlignment.TOP);

      StatXLSConfig sc0 = null;
      for (int i = 0; i < config1.size(); i++) {
        sc0 = config1.get(i);
        sheet0.setColumnWidth(i, sc0.getWidth() * 256);
      }

      // create headers
      XSSFRow header1 = sheet0.createRow(0);
      createHeaders(header1, boldStyle, drawing0, config1, helper);

      // add the data
      XSSFRow row0 = null;
      int current0 = 1;

      for (Object[] obj : rdcList) {
        row0 = sheet0.createRow(current0);
        createDataLine0(row0, obj, regularStyle, config1);
        current0++;
      }

      XSSFSheet sheet1 = report.createSheet("Legacy");

      Drawing drawing1 = sheet1.createDrawingPatriarch();

      StatXLSConfig sc1 = null;
      for (int i = 0; i < config2_0.size(); i++) {
        sc1 = config2_0.get(i);
        sheet1.setColumnWidth(i, sc1.getWidth() * 256);
      }

      // create headers
      XSSFRow header2_0 = sheet1.createRow(0);
      createHeaders(header2_0, boldStyle, drawing1, config2_0, helper);

      XSSFRow header2_1 = sheet1.createRow(1);
      createHeaders(header2_1, boldStyle, drawing1, config2_1, helper);

      XSSFRow header2_2 = sheet1.createRow(2);
      createHeaders(header2_2, boldStyle, drawing1, config2_2, helper);

      XSSFRow header2_3 = sheet1.createRow(3);
      createHeaders(header2_3, boldStyle, drawing1, config2_3, helper);

      XSSFRow header2_4 = sheet1.createRow(4);
      createHeaders(header2_4, boldStyle, drawing1, config2_4, helper);

      // add the data
      XSSFRow row1 = null;
      int current1 = 5;

      for (Object[] obj : legacyList) {
        row1 = sheet1.createRow(current1);
        createDataLine1(row1, obj, regularStyle, config2_3);
        current1++;
      }

      if (response != null) {
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Type", "application/octet-stream");
        response.addHeader("Content-Disposition", "attachment; filename=\"ChangeLog RDc Legacy.xlsx\"");
        report.write(response.getOutputStream());
      } else {
        FileOutputStream fos = new FileOutputStream("C:/ChangeLog RDc Legacy.xlsx");
        try {
          report.write(fos);
        } finally {
          fos.close();
        }
      }

    } finally {
      report.close();
    }
  }

  private void initConfig1() {
    config1 = new ArrayList<StatXLSConfig>();

    config1.add(new StatXLSConfig("KATR6", "KATR6", 9, null));
    config1.add(new StatXLSConfig("ZZKV_CUSNO", "ZZKV_CUSNO", 9, null));
    config1.add(new StatXLSConfig("KTOKD", "KTOKD", 9, null));
    config1.add(new StatXLSConfig("CHGTS", "CHGTS", 24, null));
    config1.add(new StatXLSConfig("TAB", "TAB", 18, null));
    config1.add(new StatXLSConfig("FIELD", "FIELD", 18, null));
    config1.add(new StatXLSConfig("OLD", "OLD", 23, null));
    config1.add(new StatXLSConfig("NEW", "NEW", 23, null));
    config1.add(new StatXLSConfig("USERID", "USERID", 24, null));
    config1.add(new StatXLSConfig("REQ_ID", "REQ_ID", 12, null));
    config1.add(new StatXLSConfig("REQ_TYPE", "REQ_TYPE", 9, null));
    config1.add(new StatXLSConfig("REQUESTER_ID", "REQUESTER_ID", 24, null));
    config1.add(new StatXLSConfig("LOB", "LOB", 24, null));
    config1.add(new StatXLSConfig("REASON", "REASON", 24, null));
  }

  private void createHeaders(XSSFRow header, XSSFCellStyle style, Drawing drawing, List<StatXLSConfig> config, CreationHelper helper) {
    XSSFCell cell = null;

    StatXLSConfig sc = null;
    for (int i = 0; i < config.size(); i++) {
      sc = config.get(i);
      cell = header.createCell(i);
      cell.setCellValue(sc.getLabel());
      cell.setCellStyle(style);
      if (sc.getComment() != null) {
        addComment(header, cell, "CreateCMR", sc.getComment(), drawing, helper);
      }
    }

  }

  private void createDataLine0(XSSFRow row, Object[] record, XSSFCellStyle style, List<StatXLSConfig> config)
      throws IllegalArgumentException, IllegalAccessException {
    XSSFCell cell = null;

    Object value = null;
    StatXLSConfig sc = null;
    for (int i = 0; i < config.size(); i++) {
      sc = config.get(i);
      cell = row.createCell(i);
      cell.setCellStyle(style);
      value = getValue0(sc.getDbField(), record);
      if (value != null) {
        if (value instanceof String) {
          cell.setCellValue(value.toString());
        } else if (value instanceof Long) {
          Long longVal = (Long) value;
          if (longVal.longValue() >= 0) {
            cell.setCellValue(longVal);
          }
        } else if (value instanceof Integer) {
          Integer longVal = (Integer) value;
          if (longVal.intValue() >= 0) {
            cell.setCellValue(longVal);
          }
        } else if (value instanceof Date) {
          Timestamp newValueTs = new Timestamp(((Date) value).getTime());
          String strNewValueDate = newValueTs.toString();
          cell.setCellValue(strNewValueDate);
        }
      }

    }

  }

  private void createDataLine1(XSSFRow row, Object[] record, XSSFCellStyle style, List<StatXLSConfig> config)
      throws IllegalArgumentException, IllegalAccessException {
    XSSFCell cell = null;

    Object value = null;
    StatXLSConfig sc = null;
    for (int i = 0; i < config.size(); i++) {
      sc = config.get(i);
      cell = row.createCell(i);
      cell.setCellStyle(style);
      value = getValue1(sc.getDbField(), record);
      if (value != null) {
        if (value instanceof String) {
          cell.setCellValue(value.toString());
        } else if (value instanceof Long) {
          Long longVal = (Long) value;
          if (longVal.longValue() >= 0) {
            cell.setCellValue(longVal);
          }
        } else if (value instanceof Integer) {
          Integer longVal = (Integer) value;
          if (longVal.intValue() >= 0) {
            cell.setCellValue(longVal);
          }
        } else if (value instanceof Date) {
          Timestamp newValueTs = new Timestamp(((Date) value).getTime());
          String strNewValueDate = newValueTs.toString();
          cell.setCellValue(strNewValueDate);
        }
      }

    }

  }

  private void addComment(XSSFRow row, XSSFCell cell, String author, String content, Drawing drawing, CreationHelper helper) {
    ClientAnchor anchor = helper.createClientAnchor();
    anchor.setCol1(cell.getColumnIndex());
    anchor.setCol2(cell.getColumnIndex() + 3);
    anchor.setRow1(row.getRowNum());
    anchor.setRow2(row.getRowNum() + 5);
    Comment comment = drawing.createCellComment(anchor);
    RichTextString rtfs = helper.createRichTextString(content);
    comment.setAuthor(author);
    comment.setString(rtfs);
    cell.setCellComment(comment);
  }

  private Object getValue0(String columnName, Object[] record) throws IllegalArgumentException, IllegalAccessException {
    switch (columnName) {
    case "KATR6":
      return record[0];
    case "ZZKV_CUSNO":
      return record[1];
    case "KTOKD":
      return record[2];
    case "CHGTS":
      return record[3];
    case "TAB":
      return record[4];
    case "FIELD":
      return record[5];
    case "OLD":
      return record[6];
    case "NEW":
      return record[7];
    case "USERID":
      return record[8];
    case "REQ_ID":
      return record[9];
    case "REQ_TYPE":
      return record[10];
    case "REQUESTER_ID":
      return record[11];
    case "LOB":
      return record[12];
    case "REASON":
      return record[13];
    }
    return "";
  }

  private Object getValue1(String columnName, Object[] record) throws IllegalArgumentException, IllegalAccessException {
    switch (columnName) {
    case "I_TABLE":
      return record[0];
    case "I_CUST_ENTITY":
      return record[1];
    case "A_LEVEL_1_VALUE":
      return record[2];
    case "A_LEVEL_2_VALUE":
      return record[3];
    case "A_LEVEL_3_VALUE":
      return record[4];
    case "A_LEVEL_4_VALUE":
      return record[5];
    case "A_LEVEL_5_VALUE":
      return record[6];
    case "A_LEVEL_6_VALUE":
      return record[7];
    case "A_LEVEL_7_VALUE":
      return record[8];
    case "A_LEVEL_8_VALUE":
      return record[9];
    case "I_ENT":
      return record[10];
    case "I_CO":
      return record[11];
    case "C_REASON":
      return record[12];
    case "I_ACTIV_SEQ":
      return record[13];
    case "I_OPRTR_SERIAL":
      return record[14];
    case "C_CHANGE_OPRTR_LOC":
      return record[15];
    case "D_CHANGE":
      return record[16];
    case "D_CUST_CHANGE_EFF":
      return record[17];
    case "H_CHANGE_STAMP":
      return record[18];
    case "N_CHANGE_OPRTR":
      return record[19];
    case "N_ABBREV":
      return record[20];
    case "I_CUST_OFF_1":
      return record[21];
    case "I_CUST_OFF_2":
      return record[22];
    case "I_CUST_OFF_3":
      return record[23];
    case "I_CUST_OFF_4":
      return record[24];
    case "I_CUST_OFF_5":
      return record[25];
    case "I_CUST_OFF_6":
      return record[26];
    case "I_CUST_OFF_7":
      return record[27];
    case "I_CUST_OFF_8":
      return record[28];
    case "I_CUST_OFF_9":
      return record[29];
    case "N_CO_LEGAL":
      return record[30];
    case "C_REASON_DELTN":
      return record[31];
    case "A_CHANGE_VALUE":
      return record[32];
    }
    return "";
  }

  private void initConfig2_0() {
    config2_0 = new ArrayList<StatXLSConfig>();

    config2_0.add(new StatXLSConfig("", "", 9, null));
    config2_0.add(new StatXLSConfig("", "", 9, null));
    config2_0.add(new StatXLSConfig("", "", 9, null));
    config2_0.add(new StatXLSConfig("A", "", 6, null));
    config2_0.add(new StatXLSConfig("A", "", 6, null));
    config2_0.add(new StatXLSConfig("A", "", 6, null));
    config2_0.add(new StatXLSConfig("A", "", 6, null));
    config2_0.add(new StatXLSConfig("A", "", 6, null));
    config2_0.add(new StatXLSConfig("A", "", 6, null));
    config2_0.add(new StatXLSConfig("A", "", 6, null));
    config2_0.add(new StatXLSConfig("A", "", 6, null));
    config2_0.add(new StatXLSConfig("", "", 9, null));
    config2_0.add(new StatXLSConfig("", "", 9, null));
    config2_0.add(new StatXLSConfig("", "", 6, null));
    config2_0.add(new StatXLSConfig("", "", 6, null));
    config2_0.add(new StatXLSConfig("", "", 24, null));
    config2_0.add(new StatXLSConfig("C", "", 9, null));
    config2_0.add(new StatXLSConfig("", "", 24, null));
    config2_0.add(new StatXLSConfig("D", "", 6, null));
    config2_0.add(new StatXLSConfig("", "", 24, null));
    config2_0.add(new StatXLSConfig("", "", 12, null));
    config2_0.add(new StatXLSConfig("", "", 24, null));
    config2_0.add(new StatXLSConfig("I", "", 6, null));
    config2_0.add(new StatXLSConfig("I", "", 6, null));
    config2_0.add(new StatXLSConfig("I", "", 6, null));
    config2_0.add(new StatXLSConfig("I", "", 6, null));
    config2_0.add(new StatXLSConfig("I", "", 6, null));
    config2_0.add(new StatXLSConfig("I", "", 6, null));
    config2_0.add(new StatXLSConfig("I", "", 6, null));
    config2_0.add(new StatXLSConfig("I", "", 6, null));
    config2_0.add(new StatXLSConfig("I", "", 6, null));
    config2_0.add(new StatXLSConfig("", "", 24, null));
    config2_0.add(new StatXLSConfig("", "", 15, null));
    config2_0.add(new StatXLSConfig("", "", 15, null));

  }

  private void initConfig2_1() {
    config2_1 = new ArrayList<StatXLSConfig>();

    config2_1.add(new StatXLSConfig("", "", 0, null));
    config2_1.add(new StatXLSConfig("", "", 0, null));
    config2_1.add(new StatXLSConfig("I", "", 0, null));
    config2_1.add(new StatXLSConfig("LEVEL", "", 0, null));
    config2_1.add(new StatXLSConfig("LEVEL", "", 0, null));
    config2_1.add(new StatXLSConfig("LEVEL", "", 0, null));
    config2_1.add(new StatXLSConfig("LEVEL", "", 0, null));
    config2_1.add(new StatXLSConfig("LEVEL", "", 0, null));
    config2_1.add(new StatXLSConfig("LEVEL", "", 0, null));
    config2_1.add(new StatXLSConfig("LEVEL", "", 0, null));
    config2_1.add(new StatXLSConfig("LEVEL", "", 0, null));
    config2_1.add(new StatXLSConfig("", "", 0, null));
    config2_1.add(new StatXLSConfig("", "", 0, null));
    config2_1.add(new StatXLSConfig("", "", 0, null));
    config2_1.add(new StatXLSConfig("I", "", 0, null));
    config2_1.add(new StatXLSConfig("I", "", 0, null));
    config2_1.add(new StatXLSConfig("CHANGE", "", 0, null));
    config2_1.add(new StatXLSConfig("", "", 0, null));
    config2_1.add(new StatXLSConfig("CUST", "", 0, null));
    config2_1.add(new StatXLSConfig("H", "", 0, null));
    config2_1.add(new StatXLSConfig("N", "", 0, null));
    config2_1.add(new StatXLSConfig("", "", 0, null));
    config2_1.add(new StatXLSConfig("CUST", "", 0, null));
    config2_1.add(new StatXLSConfig("CUST", "", 0, null));
    config2_1.add(new StatXLSConfig("CUST", "", 0, null));
    config2_1.add(new StatXLSConfig("CUST", "", 0, null));
    config2_1.add(new StatXLSConfig("CUST", "", 0, null));
    config2_1.add(new StatXLSConfig("CUST", "", 0, null));
    config2_1.add(new StatXLSConfig("CUST", "", 0, null));
    config2_1.add(new StatXLSConfig("CUST", "", 0, null));
    config2_1.add(new StatXLSConfig("CUST", "", 0, null));
    config2_1.add(new StatXLSConfig("N", "", 0, null));
    config2_1.add(new StatXLSConfig("C", "", 0, null));
    config2_1.add(new StatXLSConfig("A", "", 0, null));
  }

  private void initConfig2_2() {
    config2_2 = new ArrayList<StatXLSConfig>();

    config2_2.add(new StatXLSConfig("", "", 0, null));
    config2_2.add(new StatXLSConfig("I", "", 0, null));
    config2_2.add(new StatXLSConfig("CUST", "", 0, null));
    config2_2.add(new StatXLSConfig("1", "", 0, null));
    config2_2.add(new StatXLSConfig("2", "", 0, null));
    config2_2.add(new StatXLSConfig("3", "", 0, null));
    config2_2.add(new StatXLSConfig("4", "", 0, null));
    config2_2.add(new StatXLSConfig("5", "", 0, null));
    config2_2.add(new StatXLSConfig("6", "", 0, null));
    config2_2.add(new StatXLSConfig("7", "", 0, null));
    config2_2.add(new StatXLSConfig("8", "", 0, null));
    config2_2.add(new StatXLSConfig("I", "", 0, null));
    config2_2.add(new StatXLSConfig("I", "", 0, null));
    config2_2.add(new StatXLSConfig("C", "", 0, null));
    config2_2.add(new StatXLSConfig("ACTIV", "", 0, null));
    config2_2.add(new StatXLSConfig("OPRTR", "", 0, null));
    config2_2.add(new StatXLSConfig("OPRTR", "", 0, null));
    config2_2.add(new StatXLSConfig("D", "", 0, null));
    config2_2.add(new StatXLSConfig("CHANGE", "", 0, null));
    config2_2.add(new StatXLSConfig("CHANGE", "", 0, null));
    config2_2.add(new StatXLSConfig("CHANGE", "", 0, null));
    config2_2.add(new StatXLSConfig("N", "", 0, null));
    config2_2.add(new StatXLSConfig("OFF", "", 0, null));
    config2_2.add(new StatXLSConfig("OFF", "", 0, null));
    config2_2.add(new StatXLSConfig("OFF", "", 0, null));
    config2_2.add(new StatXLSConfig("OFF", "", 0, null));
    config2_2.add(new StatXLSConfig("OFF", "", 0, null));
    config2_2.add(new StatXLSConfig("OFF", "", 0, null));
    config2_2.add(new StatXLSConfig("OFF", "", 0, null));
    config2_2.add(new StatXLSConfig("OFF", "", 0, null));
    config2_2.add(new StatXLSConfig("OFF", "", 0, null));
    config2_2.add(new StatXLSConfig("CO", "", 0, null));
    config2_2.add(new StatXLSConfig("REASON", "", 0, null));
    config2_2.add(new StatXLSConfig("CHANGE", "", 0, null));
  }

  private void initConfig2_3() {
    config2_3 = new ArrayList<StatXLSConfig>();

    config2_3.add(new StatXLSConfig("", "", 0, null));
    config2_3.add(new StatXLSConfig("TABLE", "I_TABLE", 0, null));
    config2_3.add(new StatXLSConfig("ENTITY", "I_CUST_ENTITY", 0, null));
    config2_3.add(new StatXLSConfig("VALUE", "A_LEVEL_1_VALUE", 0, null));
    config2_3.add(new StatXLSConfig("VALUE", "A_LEVEL_2_VALUE", 0, null));
    config2_3.add(new StatXLSConfig("VALUE", "A_LEVEL_3_VALUE", 0, null));
    config2_3.add(new StatXLSConfig("VALUE", "A_LEVEL_4_VALUE", 0, null));
    config2_3.add(new StatXLSConfig("VALUE", "A_LEVEL_5_VALUE", 0, null));
    config2_3.add(new StatXLSConfig("VALUE", "A_LEVEL_6_VALUE", 0, null));
    config2_3.add(new StatXLSConfig("VALUE", "A_LEVEL_7_VALUE", 0, null));
    config2_3.add(new StatXLSConfig("VALUE", "A_LEVEL_8_VALUE", 0, null));
    config2_3.add(new StatXLSConfig("ENT", "I_ENT", 0, null));
    config2_3.add(new StatXLSConfig("CO", "I_CO", 0, null));
    config2_3.add(new StatXLSConfig("REASON", "C_REASON", 0, null));
    config2_3.add(new StatXLSConfig("SEQ", "I_ACTIV_SEQ", 0, null));
    config2_3.add(new StatXLSConfig("SERIAL", "I_OPRTR_SERIAL", 0, null));
    config2_3.add(new StatXLSConfig("LOC", "C_CHANGE_OPRTR_LOC", 0, null));
    config2_3.add(new StatXLSConfig("CHANGE", "D_CHANGE", 0, null));
    config2_3.add(new StatXLSConfig("EFF", "D_CUST_CHANGE_EFF", 0, null));
    config2_3.add(new StatXLSConfig("STAMP", "H_CHANGE_STAMP", 0, null));
    config2_3.add(new StatXLSConfig("OPRTR", "N_CHANGE_OPRTR", 0, null));
    config2_3.add(new StatXLSConfig("ABBRE", "N_ABBREV", 0, null));
    config2_3.add(new StatXLSConfig("1", "I_CUST_OFF_1", 0, null));
    config2_3.add(new StatXLSConfig("2", "I_CUST_OFF_2", 0, null));
    config2_3.add(new StatXLSConfig("3", "I_CUST_OFF_3", 0, null));
    config2_3.add(new StatXLSConfig("4", "I_CUST_OFF_4", 0, null));
    config2_3.add(new StatXLSConfig("5", "I_CUST_OFF_5", 0, null));
    config2_3.add(new StatXLSConfig("6", "I_CUST_OFF_6", 0, null));
    config2_3.add(new StatXLSConfig("7", "I_CUST_OFF_7", 0, null));
    config2_3.add(new StatXLSConfig("8", "I_CUST_OFF_8", 0, null));
    config2_3.add(new StatXLSConfig("9", "I_CUST_OFF_9", 0, null));
    config2_3.add(new StatXLSConfig("LEGAL", "N_CO_LEGAL", 0, null));
    config2_3.add(new StatXLSConfig("DELTN", "C_REASON_DELTN", 0, null));
    config2_3.add(new StatXLSConfig("VALUE", "A_CHANGE_VALUE", 0, null));
  }

  private void initConfig2_4() {
    config2_4 = new ArrayList<StatXLSConfig>();

    config2_4.add(new StatXLSConfig("", "", 0, null));
    config2_4.add(new StatXLSConfig("-----", "", 0, null));
    config2_4.add(new StatXLSConfig("-----------", "", 0, null));
    config2_4.add(new StatXLSConfig("-----", "", 0, null));
    config2_4.add(new StatXLSConfig("-----", "", 0, null));
    config2_4.add(new StatXLSConfig("-----", "", 0, null));
    config2_4.add(new StatXLSConfig("-----", "", 0, null));
    config2_4.add(new StatXLSConfig("-----", "", 0, null));
    config2_4.add(new StatXLSConfig("-----", "", 0, null));
    config2_4.add(new StatXLSConfig("-----", "", 0, null));
    config2_4.add(new StatXLSConfig("-----", "", 0, null));
    config2_4.add(new StatXLSConfig("-----------", "", 0, null));
    config2_4.add(new StatXLSConfig("-----------", "", 0, null));
    config2_4.add(new StatXLSConfig("------", "", 0, null));
    config2_4.add(new StatXLSConfig("-----------", "", 0, null));
    config2_4.add(new StatXLSConfig("------", "", 0, null));
    config2_4.add(new StatXLSConfig("------", "", 0, null));
    config2_4.add(new StatXLSConfig("----------", "", 0, null));
    config2_4.add(new StatXLSConfig("----------", "", 0, null));
    config2_4.add(new StatXLSConfig("--------", "", 0, null));
    config2_4.add(new StatXLSConfig("----------------------", "", 0, null));
    config2_4.add(new StatXLSConfig("---------------", "", 0, null));
    config2_4.add(new StatXLSConfig("----", "", 0, null));
    config2_4.add(new StatXLSConfig("----", "", 0, null));
    config2_4.add(new StatXLSConfig("----", "", 0, null));
    config2_4.add(new StatXLSConfig("----", "", 0, null));
    config2_4.add(new StatXLSConfig("----", "", 0, null));
    config2_4.add(new StatXLSConfig("----", "", 0, null));
    config2_4.add(new StatXLSConfig("----", "", 0, null));
    config2_4.add(new StatXLSConfig("----", "", 0, null));
    config2_4.add(new StatXLSConfig("----", "", 0, null));
    config2_4.add(new StatXLSConfig("------------------------------", "", 0, null));
    config2_4.add(new StatXLSConfig("------", "", 0, null));
    config2_4.add(new StatXLSConfig(
        "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------",
        "", 0, null));
  }

}
