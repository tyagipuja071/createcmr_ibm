package com.ibm.cio.cmr.request.service.code;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.code.CrisReportModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

@Component
public class CrisReportService extends BaseSimpleService<List<CrisReportModel>> {

  private static final Logger LOG = Logger.getLogger(CrisReportService.class);

  private Date date = new Date();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");

  @Override
  protected List<CrisReportModel> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String timeframe = request.getParameter("timeframe");

    String jpDateFrom = dateFrom(request.getParameter("dateFrom").replace(" ", "T"));
    String jpDateTo = dateTo(request.getParameter("dateTo").replace(" ", "T"));

    PreparedQuery q = null;
    String sqlQuery = "";

    if (StringUtils.isNotBlank(timeframe)) {
      if (timeframe.equals("RAONDEMAND")) {
        sqlQuery = "QUERY.JP.CRISREPORT.RAREPORT";

      } else if (timeframe.equals("TAIGADAILY")) {
        sqlQuery = "QUERY.JP.CRISREPORT.TAIGADAILY";

      } else if (timeframe.equals("TAIGAMONTHLY")) {
        sqlQuery = "QUERY.JP.CRISREPORT.TAIGAMONTHLY";

      } else if (timeframe.equals("ROLDAILY")) {
        sqlQuery = "QUERY.JP.CRISREPORT.ROLDAILY";

      } else if (timeframe.equals("ROLMONTHLY")) {
        sqlQuery = "QUERY.JP.CRISREPORT.ROLMONTHLY";

      }
    }

    LOG.info("Querying selected CRIS User Report...");
    String sql = ExternalizedQuery.getSql(sqlQuery);
    q = new PreparedQuery(entityManager, sql);
    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    if (timeframe.equals("TAIGADAILY") || timeframe.equals("ROLDAILY")) {
      q.setParameter("DATEFROM", jpDateFrom);
      q.setParameter("DATETO", jpDateTo);
    }
    if (timeframe.equals("RAONDEMAND")) {
      q.setParameter("DATEFROM", jpDateFrom);
      q.setParameter("DATETO", jpDateTo);
    }

    List<CrisReportModel> results = new ArrayList<CrisReportModel>();
    CrisReportModel crisReportModel = null;

    Instant start = Instant.now();
    LOG.info("Query Start Time: " + start);
    List<Object[]> records = q.getResults();
    Instant finish = Instant.now();
    LOG.info("Query End Time: " + finish);
    long timeElapsed = Duration.between(start, finish).toMillis();
    LOG.info("Query Elapsed Time: " + timeElapsed + " milliseconds");

    if (timeframe.equals("TAIGADAILY") || timeframe.equals("ROLDAILY")) {
      for (Object[] record : records) {
        crisReportModel = new CrisReportModel();

        // entities for TAIGA & ROL both for Daily report
        crisReportModel.setKunnr((String) record[0]);
        crisReportModel.setChgts((Timestamp) record[1]);
        crisReportModel.setRecordNo((String) record[2]);
        crisReportModel.setRecType((String) record[3]);
        crisReportModel.setCompanyNo((String) record[7]);
        crisReportModel.setOldValue((String) record[4]);
        crisReportModel.setNewValue((String) record[5]);
        crisReportModel.setName((String) record[6]);
        crisReportModel.setCompName((String) record[8]);

        results.add(crisReportModel);
      }

    } else if (timeframe.equals("TAIGAMONTHLY") || timeframe.equals("ROLMONTHLY")) {
      for (Object[] record : records) {
        crisReportModel = new CrisReportModel();

        // entities for TAIGA & ROL Monthly report
        crisReportModel.setCompanyNo((String) record[0]);
        crisReportModel.setCompName((String) record[1]);
        crisReportModel.setNewValue((String) record[2]);

        results.add(crisReportModel);
      }
    } else if (timeframe.equals("RAONDEMAND")) {
      for (Object[] record : records) {
        crisReportModel = new CrisReportModel();

        // entities for RA ON DEMAND report
        crisReportModel.setCpno((String) record[0]);
        crisReportModel.setUserId((String) record[1]);
        crisReportModel.setDate((String) record[2]);
        crisReportModel.setCc((String) record[3]);
        crisReportModel.setPayFrom((String) record[4]);
        crisReportModel.setBillTo((String) record[5]);

        crisReportModel.setDue1((String) record[8]);
        crisReportModel.setCycle1((String) record[9]);
        crisReportModel.setPay1((String) record[10]);
        crisReportModel.setG1("'");

        crisReportModel.setDue2((String) record[11]);
        crisReportModel.setCycle2((String) record[12]);
        crisReportModel.setPay2((String) record[13]);
        crisReportModel.setG2("'");

        crisReportModel.setDue3((String) record[14]);
        crisReportModel.setCycle3((String) record[15]);
        crisReportModel.setPay3((String) record[16]);
        crisReportModel.setG3("'");

        crisReportModel.setDue4((String) record[17]);
        crisReportModel.setCycle4((String) record[18]);
        crisReportModel.setPay4((String) record[19]);
        crisReportModel.setG4("'");

        crisReportModel.setDue5((String) record[20]);
        crisReportModel.setCycle5((String) record[21]);
        crisReportModel.setPay5((String) record[22]);
        crisReportModel.setG5("'");

        crisReportModel.setDue6((String) record[23]);
        crisReportModel.setCycle6((String) record[24]);
        crisReportModel.setPay6((String) record[25]);
        crisReportModel.setG6("'");

        crisReportModel.setDue7((String) record[26]);
        crisReportModel.setCycle7((String) record[27]);
        crisReportModel.setPay7((String) record[28]);
        crisReportModel.setG7("'");

        crisReportModel.setDue8((String) record[29]);
        crisReportModel.setCycle8((String) record[30]);
        crisReportModel.setPay8((String) record[31]);
        crisReportModel.setG8("'");

        crisReportModel.setReceiptInf("");

        results.add(crisReportModel);
      }
    }

    return results;
  }

  /**
   * Creates the TAIGA DAILY text file report
   * 
   * @param timeframe
   * @param stats
   * @param response
   * @throws IOException
   * @throws ParseException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */

  public void taigaDailyExportToTextFile(HttpServletResponse response, List<CrisReportModel> records, String timeframe, String dateFrom,
      String dateTo) throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    LOG.info("Exporting Japan CRIS Report for Users to .txt file..");

    String type = "text/plain";
    String fileName = "CRISReport";

    // Format dateFrom and dateTo according to expected output
    String startDate = dateFrom.substring(2, 10);
    String endDate = dateTo.substring(2, 10);

    response.setContentType(type);
    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".txt\"");

    // Group records by reportNo
    Map<String, List<CrisReportModel>> groupedRecords = new HashMap<>();
    for (CrisReportModel report : records) {
      String recordNo = report.getRecordNo();
      if (!groupedRecords.containsKey(recordNo)) {
        groupedRecords.put(recordNo, new ArrayList<>());
      }
      groupedRecords.get(recordNo).add(report);
    }

    // Writing to HTTP response output stream
    try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

      osw.write("*** ACCOUNT TAIGA CODE DAILY AUDIT REPORT *** AS OF DATE: " + startDate + " - " + endDate);
      osw.write(System.lineSeparator());
      osw.write(System.lineSeparator());

      for (Map.Entry<String, List<CrisReportModel>> entry : groupedRecords.entrySet()) {
        List<CrisReportModel> groupedReports = entry.getValue();

        osw.write("COMPANY NUMBER: " + groupedReports.get(0).getCompanyNo() + "\t");
        osw.write("NEW TAIGA CODE: " + groupedReports.get(0).getNewValue());
        osw.write(System.lineSeparator());
        osw.write("COMPANY NAME: " + groupedReports.get(0).getCompName());
        osw.write(System.lineSeparator());
        osw.write(System.lineSeparator());
        osw.write("CUST # \t\t");
        osw.write("CUSTOMER NAME \t\t\t\t\t");
        osw.write("OLD CODE ");
        osw.write(System.lineSeparator());
        osw.write("-------- ");
        osw.write("----------------------------------------------------- ");
        osw.write("---------- ");
        osw.write(System.lineSeparator());

        for (CrisReportModel report : groupedReports) {
          if (report.getRecordNo() != null) {
            osw.write(report.getRecordNo() + "\t\t");
          } else {
            osw.write("" + "\t\t");
          }
          if (report.getName() != null) {
            osw.write(report.getName() + "\t\t\t");
          } else {
            osw.write("" + "\t\t\t");
          }
          if (report.getOldValue() != null) {
            osw.write(report.getOldValue());
          } else {
            osw.write("");
          }
          osw.write(System.lineSeparator());
        }

        osw.write("-------------------------------------------------------------------------");
        osw.write(System.lineSeparator());
        osw.write(System.lineSeparator());
      }

      osw.write("-------------------------------------------------------------------------");
      osw.write(System.lineSeparator());
      osw.write("\t\t" + "<<<*** TOTAL RECORDS = " + records.size() + " ***>>>");
    }
  }

  /**
   * Creates the TAIGA MONTHLY text file report
   * 
   * @param timeframe
   * @param stats
   * @param response
   * @throws IOException
   * @throws ParseException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */

  public void taigaMonthlyExportToTextFile(HttpServletResponse response, List<CrisReportModel> records, String timeframe)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    LOG.info("Exporting Japan CRIS Report for Users to .txt file..");

    String type = "text/plain";
    String fileName = "CRISReport";

    response.setContentType(type);
    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".txt\"");

    // Writing to HTTP response output stream
    try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

      osw.write("*** COMPANY TAIGA CODE MONTHLY STATUS REPORT *** AS OF DATE: " + dateFormat.format(date));
      osw.write(System.lineSeparator());
      osw.write(System.lineSeparator());
      osw.write("COMPANY # \t");
      osw.write("COMPANY NAME \t\t\t\t\t");
      osw.write("TAIGA CODE ");
      osw.write(System.lineSeparator());
      osw.write("-------- ");
      osw.write("----------------------------------------------------- ");
      osw.write("---------- ");
      osw.write(System.lineSeparator());

      for (CrisReportModel report : records) {
        if (report.getCompanyNo() != null) {
          osw.write(report.getCompanyNo() + "\t");
        } else {
          osw.write("" + "\t");
        }
        if (report.getCompName() != null) {
          osw.write(report.getCompName() + "\t\t\t");
        } else {
          osw.write("" + "\t\t\t");
        }
        if (report.getNewValue() != null) {
          osw.write(report.getNewValue());
        } else {
          osw.write("");
        }
        osw.write(System.lineSeparator());
      }

      osw.write("-------------------------------------------------------------------------");
      osw.write(System.lineSeparator());
      osw.write("\t\t" + "<<<*** TOTAL RECORDS = " + records.size() + " ***>>>");
    }
  }

  /**
   * Creates the ROL DAILY text file report
   * 
   * @param timeframe
   * @param stats
   * @param response
   * @throws IOException
   * @throws ParseException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */

  public void rolDailyExportToTextFile(HttpServletResponse response, List<CrisReportModel> records, String timeframe, String dateFrom, String dateTo)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    LOG.info("Exporting Japan CRIS Report for Users to .txt file..");

    String type = "text/plain";
    String fileName = "CRISReport";

    // Format dateFrom and dateTo according to expected output
    String startDate = dateFrom.substring(2, 10);
    String endDate = dateTo.substring(2, 10);

    response.setContentType(type);
    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".txt\"");

    // Group records by reportNo
    Map<String, List<CrisReportModel>> groupedRecords = new HashMap<>();
    for (CrisReportModel report : records) {
      String recordNo = report.getRecordNo();
      if (!groupedRecords.containsKey(recordNo)) {
        groupedRecords.put(recordNo, new ArrayList<>());
      }
      groupedRecords.get(recordNo).add(report);
    }

    // Writing to HTTP response output stream
    try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

      osw.write("*** ACCOUNT ROL FLAG DAILY AUDIT REPORT *** AS OF DATE: " + startDate + " - " + endDate);
      osw.write(System.lineSeparator());
      osw.write(System.lineSeparator());

      for (Map.Entry<String, List<CrisReportModel>> entry : groupedRecords.entrySet()) {
        List<CrisReportModel> groupedReports = entry.getValue();

        osw.write("COMPANY NUMBER: " + groupedReports.get(0).getCompanyNo() + "\t");
        osw.write("NEW ROL FLAG: " + groupedReports.get(0).getNewValue());
        osw.write(System.lineSeparator());
        osw.write("COMPANY NAME: " + groupedReports.get(0).getCompName());
        osw.write(System.lineSeparator());
        osw.write(System.lineSeparator());
        osw.write("CUST # \t\t");
        osw.write("CUSTOMER NAME \t\t\t\t\t");
        osw.write("OLD FLAG ");
        osw.write(System.lineSeparator());
        osw.write("-------- ");
        osw.write("----------------------------------------------------- ");
        osw.write("---------- ");

        for (CrisReportModel report : groupedReports) {
          osw.write(System.lineSeparator());
          if (report.getRecordNo() != null) {
            osw.write(report.getRecordNo() + "\t\t");
          } else {
            osw.write("" + "\t\t");
          }
          if (report.getName() != null) {
            osw.write(report.getName() + "\t\t\t");
          } else {
            osw.write("" + "\t\t\t");
          }
          if (report.getOldValue() != null) {
            osw.write(report.getOldValue());
          } else {
            osw.write("");
          }
        }

        osw.write(System.lineSeparator());
        osw.write("-------------------------------------------------------------------------");
        osw.write(System.lineSeparator());
        osw.write("\t\t" + "<<<*** COMPANY TOTAL = " + groupedReports.size() + " ***>>>");
        osw.write(System.lineSeparator());
        osw.write(System.lineSeparator());
      }

      osw.write("-------------------------------------------------------------------------");
      osw.write(System.lineSeparator());
      osw.write("\t\t" + "<<<*** TOTAL RECORDS OF IBM = " + records.size() + " ***>>>");
    }
  }

  /**
   * Creates the ROL MONTHLY text file report
   * 
   * @param timeframe
   * @param stats
   * @param response
   * @throws IOException
   * @throws ParseException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */

  public void rolMonthlyExportToTextFile(HttpServletResponse response, List<CrisReportModel> records, String timeframe)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    LOG.info("Exporting Japan CRIS Report for Users to .txt file..");

    String type = "text/plain";
    String fileName = "CRISReport";

    response.setContentType(type);
    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".txt\"");

    // Writing to HTTP response output stream
    try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

      osw.write("*** COMPANY ROL FLAG MONTHLY STATUS REPORT *** AS OF DATE: " + dateFormat.format(date));
      osw.write(System.lineSeparator());
      osw.write(System.lineSeparator());
      osw.write("CONDITION: \t");
      osw.write(System.lineSeparator());
      osw.write(System.lineSeparator());
      osw.write("COMPANY # \t");
      osw.write("COMPANY NAME \t\t\t\t\t");
      osw.write("ROL FLAG ");
      osw.write(System.lineSeparator());
      osw.write("-------- ");
      osw.write("----------------------------------------------------- ");
      osw.write("---------- ");
      osw.write(System.lineSeparator());

      for (CrisReportModel report : records) {
        if (report.getCompanyNo() != null) {
          osw.write(report.getCompanyNo() + "\t");
        } else {
          osw.write("" + "\t");
        }
        if (report.getCompName() != null) {
          osw.write(report.getCompName() + "\t\t\t");
        } else {
          osw.write("" + "\t\t\t");
        }
        if (report.getNewValue() != null) {
          osw.write(report.getNewValue());
        } else {
          osw.write("");
        }

        osw.write(System.lineSeparator());
      }

      osw.write("-------------------------------------------------------------------------");
      osw.write(System.lineSeparator());
      osw.write("\t\t" + "<<<*** TOTAL RECORDS OF IBM = " + records.size() + " ***>>>");
    }
  }

  /**
   * Creates the RA ON DEMAND csv file report
   * 
   * @param timeframe
   * @param stats
   * @param response
   * @throws IOException
   * @throws ParseException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  public void raOnDemandExportToCsvFile(HttpServletResponse response, List<CrisReportModel> records, String timeframe, String dateFrom, String dateTo)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    LOG.info("Exporting Japan CRIS Report for Users to .csv file..");

    // Format date to write
    String rmvDshDateFrom = dateFrom.replaceAll("\\-", "");
    String formatDateFrom = rmvDshDateFrom.substring(2, 8);
    String rmvDshDateTo = dateTo.replaceAll("\\-", "");
    String formatDateTo = rmvDshDateTo.substring(2, 8);

    String type = "text/csv";
    String fileName = "CRISReport";

    response.setContentType(type);
    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".csv\"");

    try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
      // Writing header
      osw.write("CPNO,USERID,DATE,CC,PAY FROM,BILL TO,DUE1,CYCLE1,PAY1,G1,DUE2,CYCLE2,PAY2,G2,DUE3,CYCLE3,PAY3,G3,"
          + "DUE4,CYCLE4,PAY4,G4,DUE5,CYCLE5,PAY5,G5,DUE6,CYCLE6,PAY6,G6,DUE7,CYCLE7,PAY7,G7,DUE8,CYCLE8,PAY8,G8,RECEIPT INF");
      osw.write(System.lineSeparator());

      // Writing data
      for (CrisReportModel report : records) {

        // G* fields should be blank as well as RECEIPT INF
        osw.write(formatFieldValue(report.getCpno()) + "," + report.getUserId() + "," + formatFieldValue(report.getDate()) + "," + report.getCc()
            + "," + formatFieldValue(report.getPayFrom()) + "," + formatFieldValue(report.getBillTo()) + "," + formatFieldValue(report.getDue1())
            + "," + formatFieldValue(report.getCycle1()) + "," + formatFieldValue(report.getPay1()) + "," + report.getG1() + ","
            + formatFieldValue(report.getDue2()) + "," + formatFieldValue(report.getCycle2()) + "," + formatFieldValue(report.getPay2()) + ","
            + report.getG2() + "," + formatFieldValue(report.getDue3()) + "," + formatFieldValue(report.getCycle3()) + ","
            + formatFieldValue(report.getPay3()) + "," + report.getG3() + "," + formatFieldValue(report.getDue4()) + ","
            + formatFieldValue(report.getCycle4()) + "," + formatFieldValue(report.getPay4()) + "," + report.getG4() + ","
            + formatFieldValue(report.getDue5()) + "," + formatFieldValue(report.getCycle5()) + "," + formatFieldValue(report.getPay5()) + ","
            + report.getG5() + "," + formatFieldValue(report.getDue6()) + "," + formatFieldValue(report.getCycle6()) + ","
            + formatFieldValue(report.getPay6()) + "," + report.getG6() + "," + formatFieldValue(report.getDue7()) + ","
            + formatFieldValue(report.getCycle7()) + "," + formatFieldValue(report.getPay7()) + "," + report.getG7() + ","
            + formatFieldValue(report.getDue8()) + "," + formatFieldValue(report.getCycle8()) + "," + formatFieldValue(report.getPay8()) + ","
            + report.getG8() + "," + report.getReceiptInf());

        osw.write(System.lineSeparator());
      }
      osw.write(formatDateFrom + "-" + formatDateTo + "(OUTPUT DURATION: " + formatDateFrom + "-" + formatDateTo + ")");
    }
  }

  /**
   * Convert dateFrom to Japan time zone
   *
   * @param dateFrom
   */
  public static String dateFrom(String dateFrom) {

    String fDateFrom = LocalDateTime.parse(dateFrom).atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Asia/Tokyo")).toString();

    // Format to SQL readable date format
    String jpDateFrom = fDateFrom.replace("T", " ").substring(0, 16).concat(":00");

    return jpDateFrom;

  }

  /**
   * Convert dateTo to Japan time zone
   *
   * @param dateFrom
   */
  public static String dateTo(String dateTo) {

    String fDateTo = LocalDateTime.parse(dateTo).atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Asia/Tokyo")).toString();

    // Format to SQL readable date format
    String jpDateTo = fDateTo.replace("T", " ").substring(0, 16).concat(":00");

    return jpDateTo;

  }

  /**
   * Append an ' and set ' if null
   *
   * @param fieldValue
   * @return
   */
  public String formatFieldValue(String fieldValue) {
    if (fieldValue != null) {
      fieldValue = "'" + fieldValue;
    } else {
      fieldValue = "'  ";
    }
    return fieldValue;
  }

}
