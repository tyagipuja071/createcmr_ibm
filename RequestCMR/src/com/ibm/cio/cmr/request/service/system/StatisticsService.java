/**
 * 
 */
package com.ibm.cio.cmr.request.service.system;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.system.MetricsModel;
import com.ibm.cio.cmr.request.model.system.RequestStatsModel;
import com.ibm.cio.cmr.request.model.system.SquadStatisticsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.system.RequestStatsContainer;
import com.ibm.cio.cmr.request.util.system.StatXLSConfig;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class StatisticsService extends BaseSimpleService<RequestStatsContainer> {

  private static final Logger LOG = Logger.getLogger(StatisticsService.class);
  private static List<StatXLSConfig> config = null;
  public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

  @Override
  protected RequestStatsContainer doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {

    RequestStatsContainer container = new RequestStatsContainer();
    MetricsModel model = (MetricsModel) params.getParam("model");

    Date from = null;
    Date to = null;
    from = FORMATTER.parse(model.getDateFrom());
    to = FORMATTER.parse(model.getDateTo());

    long diff = to.getTime() - from.getTime();
    double diffDays = Math.floor((diff / 1000) / 60 / 60 / 24);
    if (diffDays > 180 || diffDays < 0) {
      throw new CmrException(new Exception("Date range is either invalid or greater than 180 days."));
    }

    LOG.info("Extracting data from " + model.getDateFrom() + " to " + model.getDateTo());
    String sql = ExternalizedQuery.getSql("METRICS.REQUEST_STAT");
    String geo = model.getGroupByGeo();
    if (!StringUtils.isBlank(geo)) {
      sql += " and data.CMR_ISSUING_CNTRY in (select CMR_ISSUING_CNTRY from CREQCMR.CNTRY_GEO_DEF where GEO_CD = :GEO_CD) ";
    }
    String procCenter = model.getGroupByProcCenter();
    if (!StringUtils.isBlank(procCenter)) {
      sql += " and data.CMR_ISSUING_CNTRY in (select CMR_ISSUING_CNTRY from CREQCMR.PROC_CENTER where upper(PROC_CENTER_NM) = :PROC_CENTER)";
    }
    sql += " order by admin.REQ_ID";
    sql = StringUtils.replaceOnce(sql, ":FROM", model.getDateFrom());
    sql = StringUtils.replaceOnce(sql, ":TO", model.getDateTo());

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("GEO_CD", geo);
    query.setParameter("PROC_CENTER", procCenter != null ? procCenter.toUpperCase().trim() : "");
    query.setForReadOnly(true);

    List<RequestStatsModel> stats = query.getResults(RequestStatsModel.class);
    long smallestReqId = Long.MAX_VALUE;
    long largestReqId = 0;
    for (RequestStatsModel stat : stats) {
      if (stat.getId().getReqId() > largestReqId) {
        largestReqId = stat.getId().getReqId();
      }
      if (stat.getId().getReqId() < smallestReqId) {
        smallestReqId = stat.getId().getReqId();
      }
    }
    container.setRecords(stats);
    LOG.debug("Records retrieved. Request ID range: " + smallestReqId + " to " + largestReqId);

    Map<Long, List<String>> rejections = new HashMap<Long, List<String>>();
    sql = ExternalizedQuery.getSql("METRICS.REJECTIONS");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("SMALLEST", smallestReqId);
    query.setParameter("LARGEST", largestReqId);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();

    Long reqId = null;
    String reason = null;
    for (Object[] result : results) {
      reqId = (Long) result[0];
      reason = (String) result[1];

      if (!rejections.containsKey(reqId)) {
        rejections.put(reqId, new ArrayList<String>());
      }

      rejections.get(reqId).add(reason);
    }

    container.setRejectionReasons(rejections);
    LOG.debug("Rejections retrieved.");
    return container;
  }

  /**
   * Creates the excel report
   * 
   * @param model
   * @param stats
   * @param response
   * @throws IOException
   * @throws ParseException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  public void exportToExcel(RequestStatsContainer container, MetricsModel model, HttpServletResponse response)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    if (config == null) {
      initConfig();
    }

    List<RequestStatsModel> stats = container.getRecords();
    Map<Long, List<String>> rejectionReasons = container.getRejectionReasons();
    LOG.info("Exporting records to excel..");
    XSSFWorkbook report = new XSSFWorkbook();
    XSSFSheet sheet = report.createSheet("Statistics");

    Drawing drawing = sheet.createDrawingPatriarch();
    CreationHelper helper = report.getCreationHelper();

    XSSFFont bold = report.createFont();
    bold.setBold(true);
    bold.setFontHeight(10);
    XSSFCellStyle boldStyle = report.createCellStyle();
    boldStyle.setFont(bold);

    XSSFFont regular = report.createFont();
    regular.setFontHeight(10);
    XSSFCellStyle regularStyle = report.createCellStyle();
    regularStyle.setFont(regular);
    regularStyle.setWrapText(true);
    regularStyle.setVerticalAlignment(VerticalAlignment.TOP);

    StatXLSConfig sc = null;
    for (int i = 0; i < config.size(); i++) {
      sc = config.get(i);
      sheet.setColumnWidth(i, sc.getWidth() * 256);
    }
    // create title
    XSSFRow titleRow = sheet.createRow(0);
    XSSFCell cell = titleRow.createCell(0);
    cell.setCellStyle(boldStyle);
    String title = "Request Statistics";
    if (model != null) {
      title = "Request Statistics from " + model.getDateFrom() + " to " + model.getDateTo();
      if (!StringUtils.isEmpty(model.getGroupByProcCenter())) {
        title += " for " + model.getGroupByProcCenter();
      }
      if (!StringUtils.isEmpty(model.getGroupByGeo())) {
        title += " (" + model.getGroupByGeo() + ")";
      }
    }
    cell.setCellValue(title);

    // create headers
    XSSFRow header = sheet.createRow(1);
    createHeaders(header, boldStyle, drawing, config, helper);

    // add the data
    XSSFRow row = null;
    int current = 2;
    for (RequestStatsModel request : stats) {
      row = sheet.createRow(current);
      createDataLine(row, request, rejectionReasons != null ? rejectionReasons.get(request.getId().getReqId()) : null, regularStyle, config);
      current++;
    }
    String type = "application/octet-stream";
    String fileName = "RequestStats_";
    if (model != null) {
      fileName += StringUtils.replace(model.getDateFrom(), "-", "");
      fileName += "-";
      fileName += StringUtils.replace(model.getDateTo(), "-", "");
      if (!StringUtils.isEmpty(model.getGroupByProcCenter())) {
        fileName += "_" + model.getGroupByProcCenter();
      }
      if (!StringUtils.isBlank(model.getGroupByGeo())) {
        fileName += "_" + model.getGroupByGeo();
      }
    }
    if (response != null) {
      response.setContentType(type);
      response.addHeader("Content-Type", type);
      response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"");
      report.write(response.getOutputStream());
    } else {
      FileOutputStream fos = new FileOutputStream("C:/" + fileName + ".xlsx");
      try {
        report.write(fos);
      } finally {
        fos.close();
      }
    }

  }

  /**
   * Creates the headers
   * 
   * @param header
   * @param style
   * @param drawing
   * @param helper
   */
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

  /**
   * Creates a comment on the specific address
   * 
   * @param row
   * @param cell
   * @param author
   * @param content
   * @param drawing
   * @param helper
   */
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

  private void createDataLine(XSSFRow row, Object request, List<String> rejections, XSSFCellStyle style, List<StatXLSConfig> config)
      throws IllegalArgumentException, IllegalAccessException {
    XSSFCell cell = null;

    Object value = null;
    StatXLSConfig sc = null;
    for (int i = 0; i < config.size(); i++) {
      sc = config.get(i);
      cell = row.createCell(i);
      cell.setCellStyle(style);
      value = getValue(sc.getDbField(), request);
      if ("REJECTIONS".equals(sc.getDbField())) {
        if (rejections != null && !rejections.isEmpty()) {
          StringBuilder sb = new StringBuilder();
          int count = 1;
          for (String reason : rejections) {
            sb.append(sb.length() > 0 ? "\n\n" : "");
            sb.append(count + ". " + reason);
            count++;
          }
          cell.setCellValue(sb.toString());
        }
      } else if (value != null) {
        if (sc.getDbField().endsWith("_TAT")) {
          Long lVal = (Long) value;
          if (lVal != null && lVal.longValue() >= 0) {
            long millis = lVal.longValue() * 1000;
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            cell.setCellValue(hms);
          }
        } else {
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
          }
        }
      }

    }

  }

  private Object getValue(String columnName, Object request) throws IllegalArgumentException, IllegalAccessException {
    Column col = null;
    if (columnName.equals("REQ_ID") && (request instanceof RequestStatsModel)) {
      return ((RequestStatsModel) request).getId().getReqId();
    }
    for (Field field : request.getClass().getDeclaredFields()) {
      col = field.getAnnotation(Column.class);
      if (col != null && columnName.equals(col.name())) {
        field.setAccessible(true);
        return field.get(request);
      }
      if (columnName.toLowerCase().equals(field.getName().toLowerCase())) {
        field.setAccessible(true);
        return field.get(request);
      }
    }
    return null;
  }

  private void initConfig() {
    config = new ArrayList<StatXLSConfig>();

    config.add(new StatXLSConfig("Country Code", "CNTRY_CD", 12, null));
    config.add(new StatXLSConfig("Country", "CNTRY_DESC", 25, null));
    config.add(new StatXLSConfig("IOT", "IOT", 8, null));
    config.add(new StatXLSConfig("Company", "OWNER", 9, null));
    config.add(new StatXLSConfig("Request Number", "REQ_ID", 16, null));
    config.add(new StatXLSConfig("LOB", "LOB", 24, null));
    config.add(new StatXLSConfig("Request Type", "REQ_TYPE", 18, null));
    config.add(new StatXLSConfig("Prospect Conversion", "PROSPECT", 18, null));
    config.add(new StatXLSConfig("Customer Name", "CUST_NM", 35, null));
    config.add(new StatXLSConfig("CMR No.", "CMR_NO", 10, null));
    config.add(new StatXLSConfig("Request Reason", "REQ_REASON", 20, null));
    config.add(new StatXLSConfig("Scenario Type", "SCENARIO_TYPE_DESC", 20, null));
    config.add(new StatXLSConfig("Scenario Subtype", "SCENARIO_SUBTYPE_DESC", 20, null));
    config.add(new StatXLSConfig("Record Type", "CUST_TYPE", 10, null));
    config.add(new StatXLSConfig("Source System", "SOURCE_SYST_ID", 20, null));
    config.add(new StatXLSConfig("Requester", "REQUESTER_NM", 20, null));
    config.add(new StatXLSConfig("Requester ID", "REQUESTER_ID", 25, null));
    config.add(new StatXLSConfig("Reviewer", "REVIEWER_NM", 20, null));
    config.add(new StatXLSConfig("Reviewer ID", "REVIEWER_ID", 25, null));
    config.add(new StatXLSConfig("Approver", "APPROVER_NM", 20, null));
    config.add(new StatXLSConfig("Approver ID", "APPROVER_ID", 25, null));
    config.add(new StatXLSConfig("Processor", "PROCESSOR_NM", 20, null));
    config.add(new StatXLSConfig("Processor ID", "PROCESSOR_ID", 25, null));
    config.add(new StatXLSConfig("Request Year", "REQ_YR", 13, null));
    config.add(new StatXLSConfig("Request Month", "REQ_MONTH", 13, null));
    config.add(new StatXLSConfig("Request Date", "REQ_DT", 13, null));
    config.add(new StatXLSConfig("Closed Year", "CLOSE_YR", 13, null));
    config.add(new StatXLSConfig("Closed Month", "CLOSE_MONTH", 13, null));
    config.add(new StatXLSConfig("Closed Date", "CLOSE_DT", 13, null));
    config.add(new StatXLSConfig("Completion Timestamp", "COMPLETION_TS", 18, null));
    config.add(new StatXLSConfig("Due Date", "REQUEST_DUE_DATE", 16, null));
    config.add(new StatXLSConfig("Final Status", "FINAL_STATUS", 24, null));
    config.add(new StatXLSConfig("# of Rejections", "REJECT_TOTAL", 14, null));
    config.add(
        new StatXLSConfig("Last Reject Reason", "LAST_REJ_REASON", 25, "If at anytime the request was rejected, the reason for the last rejection"));
    config.add(new StatXLSConfig("Other Rejection Reasons", "REJECTIONS", 25, null));
    config.add(new StatXLSConfig("Processed within 24 Hrs", "DAY_PROCESS", 23, null));
    config.add(new StatXLSConfig("Request TAT", "REQUEST_TAT", 12,
        "Total time (hh:mm:ss) from request creation to an accepted review or cancellation. May include TAT for several iterations."));
    config.add(new StatXLSConfig("Processing Pending Time Total", "PENDING_TAT", 14,
        "Total idle time (hh:mm:ss) when the request is in an unlocked status for processors."));
    config.add(new StatXLSConfig("Review TAT", "REVIEW_TAT", 12, "Total time (in minutes) it took for all reviews/validations"));
    config.add(new StatXLSConfig("CMDE Approval TAT", "APPROVAL_TAT", 12, "Total time (hh:mm:ss) it took for all CMDE approval work"));
    config.add(new StatXLSConfig("External Approval TAT", "APPROVAL2_TAT", 12,
        "Total time (hh:mm:ss) it took for external approvals on this request to be responded to"));
    config.add(new StatXLSConfig("CMR Processing TAT", "PROCESS_TAT", 17,
        "Total time (hh:mm:ss) it took for creation of the CMR either by automatic processing or manual through legacy systems"));
    config.add(new StatXLSConfig("Overall TAT", "OVERALL_TAT", 12, "Total time (hh:mm:ss) it took from request creation to completion."));
    config.add(new StatXLSConfig("Submit-to-Complete TAT", "SUBMIT_TO_COMPLETE_TAT", 20,
        "Total time (hh:mm:ss) it took from last request submission that got completed to request closing."));

  }

  /**
   * Creates the excel report
   * 
   * @param model
   * @param stats
   * @param response
   * @throws IOException
   * @throws ParseException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  public void exportToSquadReport(RequestStatsContainer container, MetricsModel model, HttpServletResponse response)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    List<StatXLSConfig> config = new ArrayList<StatXLSConfig>();
    config.add(new StatXLSConfig("Squad", "SQUAD", 22, null));
    config.add(new StatXLSConfig("Tribe", "TRIBE", 22, null));
    config.add(new StatXLSConfig("IOT Name", "IOT", 22, null));
    config.add(new StatXLSConfig("IMT Name", "IMT", 22, null));
    config.add(new StatXLSConfig("Country Name", "CNTRY_NAME", 25, null));
    config.add(new StatXLSConfig("Date", "DISPLAY", 12, null));
    config.add(new StatXLSConfig("Quarter", "QUARTER", 12, null));
    config.add(new StatXLSConfig("Requests/Day", "TOTAL", 12, null));

    List<SquadStatisticsModel> stats = container.getSquadRecords();
    LOG.info("Exporting records to excel..");
    XSSFWorkbook report = new XSSFWorkbook();
    XSSFSheet sheet = report.createSheet("Statistics");

    Drawing drawing = sheet.createDrawingPatriarch();
    CreationHelper helper = report.getCreationHelper();

    XSSFFont bold = report.createFont();
    bold.setBold(true);
    bold.setFontHeight(10);
    XSSFCellStyle boldStyle = report.createCellStyle();
    boldStyle.setFont(bold);

    XSSFFont regular = report.createFont();
    regular.setFontHeight(10);
    XSSFCellStyle regularStyle = report.createCellStyle();
    regularStyle.setFont(regular);
    regularStyle.setWrapText(true);
    regularStyle.setVerticalAlignment(VerticalAlignment.TOP);

    StatXLSConfig sc = null;
    for (int i = 0; i < config.size(); i++) {
      sc = config.get(i);
      sheet.setColumnWidth(i, sc.getWidth() * 256);
    }
    // create title
    XSSFRow titleRow = sheet.createRow(0);
    XSSFCell cell = titleRow.createCell(0);
    cell.setCellStyle(boldStyle);
    String title = "Squad Report ";
    if (model != null) {
      title = "Squad Report from " + model.getDateFrom() + " to " + model.getDateTo();
      if (!StringUtils.isEmpty(model.getGroupByProcCenter())) {
        title += " for " + model.getGroupByProcCenter();
      }
      if (!StringUtils.isEmpty(model.getGroupByGeo())) {
        title += " (" + model.getGroupByGeo() + ")";
      }
    }
    cell.setCellValue(title);

    // create headers
    XSSFRow header = sheet.createRow(1);
    createHeaders(header, boldStyle, drawing, config, helper);

    // add the data
    XSSFRow row = null;
    int current = 2;
    for (SquadStatisticsModel request : stats) {
      row = sheet.createRow(current);
      createDataLine(row, request, null, regularStyle, config);
      current++;
    }
    String type = "application/octet-stream";
    String fileName = "SquadSummary_";
    if (model != null) {
      fileName += StringUtils.replace(model.getDateFrom(), "-", "");
      fileName += "-";
      fileName += StringUtils.replace(model.getDateTo(), "-", "");
      if (!StringUtils.isEmpty(model.getGroupByProcCenter())) {
        fileName += "_" + model.getGroupByProcCenter();
      }
      if (!StringUtils.isBlank(model.getGroupByGeo())) {
        fileName += "_" + model.getGroupByGeo();
      }
    }
    if (response != null) {
      response.setContentType(type);
      response.addHeader("Content-Type", type);
      response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"");
      report.write(response.getOutputStream());
    } else {
      FileOutputStream fos = new FileOutputStream("C:/" + fileName + ".xlsx");
      try {
        report.write(fos);
      } finally {
        fos.close();
      }
    }

  }

}
