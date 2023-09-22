package com.ibm.cio.cmr.request.service.code;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
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
    String dateFrom = request.getParameter("dateFrom");
    String dateTo = request.getParameter("dateTo");

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
      q.setParameter("DATEFROM", dateFrom);
      q.setParameter("DATETO", dateTo);
    }
    if (timeframe.equals("RAONDEMAND")) {
      q.setParameter("DATEFROM", dateFrom);
      q.setParameter("DATETO", dateTo);
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
        crisReportModel.setUserId("'");
        crisReportModel.setDate((Timestamp) record[34]);
        crisReportModel.setCc("'");
        crisReportModel.setPayFrom((String) record[1]);
        crisReportModel.setBillTo((String) record[0]);

        crisReportModel.setDue1((String) record[4]);
        crisReportModel.setCycle1((String) record[5]);
        crisReportModel.setPay1((String) record[6]);
        crisReportModel.setG1("'");

        crisReportModel.setDue2((String) record[7]);
        crisReportModel.setCycle2((String) record[8]);
        crisReportModel.setPay2((String) record[9]);
        crisReportModel.setG2("'");

        crisReportModel.setDue3((String) record[10]);
        crisReportModel.setCycle3((String) record[11]);
        crisReportModel.setPay3((String) record[12]);
        crisReportModel.setG3("'");

        crisReportModel.setDue4((String) record[13]);
        crisReportModel.setCycle4((String) record[14]);
        crisReportModel.setPay4((String) record[15]);
        crisReportModel.setG4("'");

        crisReportModel.setDue5((String) record[16]);
        crisReportModel.setCycle5((String) record[17]);
        crisReportModel.setPay5((String) record[18]);
        crisReportModel.setG5("'");

        crisReportModel.setDue6((String) record[19]);
        crisReportModel.setCycle6((String) record[20]);
        crisReportModel.setPay6((String) record[21]);
        crisReportModel.setG6("'");

        crisReportModel.setDue7((String) record[22]);
        crisReportModel.setCycle7((String) record[23]);
        crisReportModel.setPay7((String) record[24]);
        crisReportModel.setG7("'");

        crisReportModel.setDue8((String) record[25]);
        crisReportModel.setCycle8((String) record[26]);
        crisReportModel.setPay8((String) record[27]);
        crisReportModel.setG8("'");

        crisReportModel.setReceiptInf("'");

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

  public void taigaDailyExportToTextFile(List<CrisReportModel> records, String timeframe, HttpServletResponse response)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    LOG.info("Exporting Japan CRIS Report for Users to .txt file..");

    String type = "text/plain";
    String fileName = "CRISReport";

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

      osw.write("*** ACCOUNT TAIGA CODE DAILY AUDIT REPORT *** AS OF DATE: " + dateFormat.format(date));
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

  public void taigaMonthlyExportToTextFile(List<CrisReportModel> records, String timeframe, HttpServletResponse response)
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

  public void rolDailyExportToTextFile(List<CrisReportModel> records, String timeframe, HttpServletResponse response)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    LOG.info("Exporting Japan CRIS Report for Users to .txt file..");

    String type = "text/plain";
    String fileName = "CRISReport";

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

      osw.write("*** ACCOUNT ROL FLAG DAILY AUDIT REPORT *** AS OF DATE: " + dateFormat.format(date));
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

  public void rolMonthlyExportToTextFile(List<CrisReportModel> records, String timeframe, HttpServletResponse response)
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
  public void raOnDemandExportToCsvFile(List<CrisReportModel> records, String timeframe, String dateFrom, String dateTo, HttpServletResponse response)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmSS");

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
        String formatDate = dateFormat.format(report.getDate());

        // G* fields should be blank as well as RECEIPT INF
        osw.write(report.getCpno() + "," + report.getUserId() + "," + formatDate + "," + report.getCc() + "," + report.getPayFrom() + ","
            + report.getBillTo() + "," + report.getDue1() + "," + report.getCycle1() + "," + report.getPay1() + "," + "'" + "," + report.getDue2()
            + "," + report.getCycle2() + "," + report.getPay2() + "," + "'" + "," + report.getDue3() + "," + report.getCycle3() + ","
            + report.getPay3() + "," + "'" + "," + report.getDue4() + "," + report.getCycle4() + "," + report.getPay4() + "," + "'" + ","
            + report.getDue5() + "," + report.getCycle5() + "," + report.getPay5() + "," + "'" + "," + report.getDue6() + "," + report.getCycle6()
            + "," + report.getPay6() + "," + "'" + "," + report.getDue7() + "," + report.getCycle7() + "," + report.getPay7() + "," + "'" + ","
            + report.getDue8() + "," + report.getCycle8() + "," + report.getPay8() + "," + "'" + "," + "");

        osw.write(System.lineSeparator());
      }
      osw.write(formatDateFrom + "-" + formatDateTo + "(OUTPUT DURATION: " + formatDateFrom + "-" + formatDateTo + ")");
    }
  }

}
