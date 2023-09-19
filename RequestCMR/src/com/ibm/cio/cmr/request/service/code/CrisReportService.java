package com.ibm.cio.cmr.request.service.code;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.code.CrisReportModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

@Component
public class CrisReportService extends BaseSimpleService<List<CrisReportModel>> {

  private static final Logger LOG = Logger.getLogger(CrisReportService.class);

  private Date date = new Date();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  @Override
  protected List<CrisReportModel> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String timeframe = request.getParameter("timeframe");

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

    LOG.debug("Querying selected CRIS User Report...");
    String sql = ExternalizedQuery.getSql(sqlQuery);
    q = new PreparedQuery(entityManager, sql);

    List<CrisReportModel> results = new ArrayList<CrisReportModel>();
    CrisReportModel crisReportModel = null;

    List<Object[]> records = q.getResults();
    for (Object[] record : records) {
      crisReportModel = new CrisReportModel();

      // entities for TAIGA & ROL
      // both for Daily and Monthly report
      crisReportModel.setKunnr((String) record[0]);
      crisReportModel.setChgts((Timestamp) record[1]);
      crisReportModel.setRecordNo((String) record[2]);
      crisReportModel.setRecType((String) record[3]);
      crisReportModel.setCompanyNo((String) record[7]);
      crisReportModel.setOldValue((String) record[4]);
      crisReportModel.setNewValue((String) record[5]);
      crisReportModel.setName((String) record[6]);
      crisReportModel.setCompName((String) record[8]);

      // entities for RA ON DEMAND report
      crisReportModel.setCpno((String) record[9]);
      crisReportModel.setUserId((String) record[10]);
      crisReportModel.setDate((String) record[11]);
      crisReportModel.setCc((String) record[12]);
      crisReportModel.setPayFrom((String) record[13]);
      crisReportModel.setBillTo((String) record[14]);

      crisReportModel.setDue1((String) record[15]);
      crisReportModel.setCycle1((String) record[16]);
      crisReportModel.setPay1((String) record[17]);
      crisReportModel.setG1((String) record[18]);

      crisReportModel.setDue2((String) record[19]);
      crisReportModel.setCycle2((String) record[29]);
      crisReportModel.setPay2((String) record[21]);
      crisReportModel.setG2((String) record[22]);

      crisReportModel.setDue3((String) record[23]);
      crisReportModel.setCycle3((String) record[24]);
      crisReportModel.setPay3((String) record[25]);
      crisReportModel.setG3((String) record[26]);

      crisReportModel.setDue4((String) record[27]);
      crisReportModel.setCycle4((String) record[28]);
      crisReportModel.setPay4((String) record[29]);
      crisReportModel.setG4((String) record[30]);

      crisReportModel.setDue5((String) record[31]);
      crisReportModel.setCycle5((String) record[32]);
      crisReportModel.setPay5((String) record[33]);
      crisReportModel.setG5((String) record[34]);

      crisReportModel.setDue6((String) record[35]);
      crisReportModel.setCycle6((String) record[36]);
      crisReportModel.setPay6((String) record[37]);
      crisReportModel.setG6((String) record[38]);

      crisReportModel.setDue7((String) record[39]);
      crisReportModel.setCycle7((String) record[40]);
      crisReportModel.setPay7((String) record[41]);
      crisReportModel.setG7((String) record[42]);

      crisReportModel.setDue8((String) record[43]);
      crisReportModel.setCycle8((String) record[44]);
      crisReportModel.setPay8((String) record[45]);
      crisReportModel.setG8((String) record[46]);

      crisReportModel.setReceiptInf((String) record[47]);

      results.add(crisReportModel);
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

    LOG.info("Exporting Japan CRIS Report for Users to a text file..");

    String type = "text/plain";
    String fileName = "CRISReport";

    response.setContentType(type);
    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".txt\"");

    // Writing to HTTP response output stream
    try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

      String companyNo = null;
      String newValue = null;
      String companyName = null;

      if (records.size() == 0) {
        companyNo = "";
        newValue = "";
        companyName = "";
      } else {
        companyNo = records.get(0).getCompanyNo();
        newValue = records.get(0).getNewValue();
        companyName = records.get(0).getCompName();
      }

      osw.write("*** ACCOUNT TAIGA CODE DAILY AUDIT REPORT *** AS OF DATE: " + dateFormat.format(date));
      osw.write(System.lineSeparator());
      osw.write(System.lineSeparator());
      osw.write("COMPANY NUMBER: " + companyNo + "\t");
      osw.write("NEW TAIGA CODE: " + newValue);
      osw.write(System.lineSeparator());
      osw.write("COMPANY NAME: " + companyName);
      osw.write(System.lineSeparator());
      osw.write("CUSTOMER # \t");
      osw.write("CUSTOMER NAME \t\t\t\t\t");
      osw.write("OLD CODE ");
      osw.write(System.lineSeparator());
      osw.write("-------- ");
      osw.write("----------------------------------------------------- ");
      osw.write("---------- ");
      osw.write(System.lineSeparator());

      for (CrisReportModel report : records) {
        osw.write(report.getKunnr() + "\t");
        osw.write(report.getName() + "\t\t\t");
        osw.write(report.getOldValue());
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

    LOG.info("Exporting Japan CRIS Report for Users to a text file..");

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
        osw.write(report.getKunnr() + "\t");
        osw.write(report.getName() + "\t\t\t");
        osw.write(report.getNewValue());
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

    LOG.info("Exporting Japan CRIS Report for Users to a text file..");

    String type = "text/plain";
    String fileName = "CRISReport";

    response.setContentType(type);
    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".txt\"");

    // Writing to HTTP response output stream
    try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

      osw.write("*** ACCOUNT ROL FLAG DAILY AUDIT REPORT *** AS OF DATE: ");
      osw.write(System.lineSeparator());
      osw.write(System.lineSeparator());

      for (CrisReportModel report : records) {
        osw.write("COMPANY NUMBER: " + report.getCompanyNo() + "\t");
        osw.write("NEW ROL FLAG: " + report.getNewValue());
        osw.write(System.lineSeparator());
        osw.write("COMPANY NAME: " + report.getCompName());
        osw.write(System.lineSeparator());
        osw.write(System.lineSeparator());
        osw.write("COMPANY # \t");
        osw.write("COMPANY NAME \t\t\t\t\t");
        osw.write("OLD FLAG ");
        osw.write(System.lineSeparator());
        osw.write("-------- ");
        osw.write("----------------------------------------------------- ");
        osw.write("---------- ");
        osw.write(System.lineSeparator());
        osw.write(report.getKunnr() + "\t");
        osw.write(report.getName() + "\t\t\t");
        osw.write(report.getOldValue());
        osw.write(System.lineSeparator());
        osw.write("-------------------------------------------------------------------------");
        osw.write(System.lineSeparator());
        osw.write("\t\t" + "<<<*** COMPANY TOTAL =  ***>>>");
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

    LOG.info("Exporting Japan CRIS Report for Users to a text file..");

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
        osw.write(report.getKunnr() + "\t");
        osw.write(report.getName() + "\t\t\t");
        osw.write(report.getNewValue());
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
  public void raOnDemandExportToCsvFile(List<CrisReportModel> records, String timeframe, HttpServletResponse response)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    LOG.info("Exporting Japan CRIS Report for Users to a text file..");

    String type = "text/csv";
    String fileName = "CRISReport";

    response.setContentType(type);
    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".csv\"");

    try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
    }

  }

}
