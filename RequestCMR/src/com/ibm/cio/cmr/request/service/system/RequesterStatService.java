/**
 * 
 */
package com.ibm.cio.cmr.request.service.system;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
import com.ibm.cio.cmr.request.model.system.RequesterStatsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.geo.MarketUtil;
import com.ibm.cio.cmr.request.util.system.RequestStatsContainer;
import com.ibm.cio.cmr.request.util.system.StatXLSConfig;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class RequesterStatService extends BaseSimpleService<RequestStatsContainer> {

  private static final Logger LOG = Logger.getLogger(RequesterStatService.class);
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
    if (diffDays > 365 || diffDays < 0) {
      throw new CmrException(new Exception("Date range is either invalid or greater than 1 year."));
    }

    LOG.info("Extracting data from " + model.getDateFrom() + " to " + model.getDateTo());
    String sql = ExternalizedQuery.getSql("METRICS.REQUESTER_STAT");
    String geoMarket = model.getGroupByGeo();

    if (!StringUtils.isBlank(geoMarket)) {
      String[] parts = geoMarket.split("[-]");
      if (parts.length == 2) {
        if ("GEO".equals(parts[0])) {
          sql = StringUtils.replace(sql, "${CNTRY}", " and d.CMR_ISSUING_CNTRY in (" + MarketUtil.createCountryFilterForGeo(parts[1]) + ") ");
        } else if ("MKT".equals(parts[0])) {
          sql = StringUtils.replace(sql, "${CNTRY}", " and d.CMR_ISSUING_CNTRY in (" + MarketUtil.createCountryFilterForMarket(parts[1]) + ") ");
        }
      }
    } else {
      sql = StringUtils.replace(sql, "${CNTRY}", " and d.CMR_ISSUING_CNTRY is not null ");
    }

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CREATE_FROM", from);
    query.setParameter("CREATE_TO", to);
    query.setForReadOnly(true);

    Map<String, RequesterStatsModel> statMap = new LinkedHashMap<String, RequesterStatsModel>();
    List<Object[]> stats = query.getResults();

    String key = null;
    RequesterStatsModel requesterStat = null;
    for (Object[] stat : stats) {
      key = stat[0] + "-" + stat[1];
      if (!statMap.containsKey(key)) {
        statMap.put(key, new RequesterStatsModel());
      }
      requesterStat = statMap.get(key);
      requesterStat.setRequesterId((String) stat[0]);
      requesterStat.setRequesterName((String) stat[5]);
      requesterStat.setCountry((String) stat[1]);
      requesterStat.setCountryName((String) stat[4]);
      if ("COM".equals(stat[2])) {
        requesterStat.setNoIssueCount(stat[3] != null ? (int) stat[3] : 0);
      } else if ("PRJ".equals(stat[2])) {
        requesterStat.setRejectedCount(stat[3] != null ? (int) stat[3] : 0);
      } else if ("OPN".equals(stat[2])) {
        requesterStat.setOpenCount(stat[3] != null ? (int) stat[3] : 0);
      } else if ("REJ".equals(stat[2])) {
        requesterStat.setTotalRejections(stat[3] != null ? (int) stat[3] : 0);
      }
      statMap.put(key, requesterStat);
    }

    List<RequesterStatsModel> requesterStats = new ArrayList<RequesterStatsModel>();
    requesterStats.addAll(statMap.values());
    container.setRequesterRecords(requesterStats);

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

    List<RequesterStatsModel> stats = container.getRequesterRecords();
    LOG.info("Exporting records to excel..");
    XSSFWorkbook report = new XSSFWorkbook();
    try {
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
      String title = "Requester Statistics";
      if (model != null) {
        title = "Requester Statistics from " + model.getDateFrom() + " to " + model.getDateTo();
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
      for (RequesterStatsModel request : stats) {
        row = sheet.createRow(current);
        createDataLine(row, request, regularStyle, config);
        current++;
      }
      String type = "application/octet-stream";
      String fileName = "RequesterStats_";
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
    } finally {
      report.close();
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

  private void createDataLine(XSSFRow row, RequesterStatsModel record, XSSFCellStyle style, List<StatXLSConfig> config)
      throws IllegalArgumentException, IllegalAccessException {
    XSSFCell cell = null;

    Object value = null;
    StatXLSConfig sc = null;
    for (int i = 0; i < config.size(); i++) {
      sc = config.get(i);
      cell = row.createCell(i);
      cell.setCellStyle(style);
      value = getValue(sc.getDbField(), record);
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
        }
      }

    }

  }

  private Object getValue(String columnName, RequesterStatsModel record) throws IllegalArgumentException, IllegalAccessException {
    switch (columnName) {
    case "REQUESTER":
      return record.getRequesterId();
    case "USER_NAME":
      return record.getRequesterName();
    case "IOT":
      return MarketUtil.getGEO(record.getCountry());
    case "MARKET":
      return MarketUtil.getMarket(record.getCountry());
    case "CMR_ISSUING_CNTRY":
      return record.getCountry();
    case "VTEXT":
      return record.getCountryName();
    case "COM":
      return record.getNoIssueCount();
    case "PRJ":
      return record.getRejectedCount();
    case "OPN":
      return record.getOpenCount();
    case "REJ":
      return record.getTotalRejections();
    case "PCNT":
      long rej = record.getRejectedCount();
      long open = record.getOpenCount();
      long com = record.getNoIssueCount();
      return new DecimalFormat("#00.00").format(((float) (rej + open) / (float) (rej + com + open)) * 100);
    }
    return "";
  }

  private void initConfig() {
    config = new ArrayList<StatXLSConfig>();

    config.add(new StatXLSConfig("Requester ID", "REQUESTER", 28, null));
    config.add(new StatXLSConfig("Requester Name", "USER_NAME", 28, null));
    config.add(new StatXLSConfig("Geography", "IOT", 15, null));
    config.add(new StatXLSConfig("Market", "MARKET", 15, null));
    config.add(new StatXLSConfig("Issuing Country", "CMR_ISSUING_CNTRY", 12, null));
    config.add(new StatXLSConfig("Country Name", "VTEXT", 15, null));
    config.add(new StatXLSConfig("Clean Requests", "COM", 18, "Total number of requests completed for the user without rejections"));
    config.add(new StatXLSConfig("Rejected Requests", "PRJ", 18, "Total number of requests completed for the user with rejections"));
    config.add(new StatXLSConfig("Open Rejected Requests", "OPN", 18, "Total number of not completed requests for the user with rejections"));
    config.add(new StatXLSConfig("Rejection %", "PCNT", 18, "Percentage of rejected requests against completed requests with no issues"));
    config
        .add(new StatXLSConfig("Total Rejections", "REJ", 18, "Total number of rejections for the user's requests, can be more than 1 per request"));
  }

}
