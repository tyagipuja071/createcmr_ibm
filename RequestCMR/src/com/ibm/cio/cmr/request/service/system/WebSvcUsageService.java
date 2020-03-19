/**
 * 
 */
package com.ibm.cio.cmr.request.service.system;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import com.ibm.cio.cmr.request.model.system.MetricsChart;
import com.ibm.cio.cmr.request.model.system.MetricsDataSet;
import com.ibm.cio.cmr.request.model.system.MetricsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class WebSvcUsageService extends BaseSimpleService<MetricsChart> {

  public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

  @Override
  protected MetricsChart doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    boolean export = (boolean) params.getParam("export");
    validateParams(params);

    MetricsModel model = (MetricsModel) params.getParam("model");
    Date from = null;
    Date to = null;
    from = FORMATTER.parse(model.getDateFrom());
    to = FORMATTER.parse(model.getDateTo());

    if (!export) {
      long diff = to.getTime() - from.getTime();
      double diffDays = Math.floor((diff / 1000) / 60 / 60 / 24);
      if (diffDays > 30 || diffDays < 0) {
        throw new CmrException(new Exception("Date range is either invalid or greater than 30 days."));
      }
    }
    MetricsChart chart = null;

    String sqlKey = "METRICS.";
    switch (model.getReportType()) {
    case "S":
      sqlKey += "USAGE.BY_SERVICE_ID";
      break;
    case "N":
      sqlKey += "USAGE.BY_SERVICE_NAME";
      break;
    case "P":
      sqlKey += "USAGE.BY_PARTNER";
      break;
    default:
      throw new CmrException(new Exception("Invalid report type selected."));
    }

    chart = generateReport(entityManager, sqlKey, model, export);
    return chart;
  }

  private MetricsChart generateReport(EntityManager entityManager, String sqlKey, MetricsModel model, boolean export) throws ParseException {
    MetricsChart chart = new MetricsChart();

    String sql = ExternalizedQuery.getSql(sqlKey);
    sql = StringUtils.replaceOnce(sql, ":FROM", model.getDateFrom());
    sql = StringUtils.replaceOnce(sql, ":TO", model.getDateTo());
    if (!StringUtils.isEmpty(model.getGroupByGeo())) {
      sql = StringUtils.replaceOnce(sql, ":SERVICE_ID", "'" + model.getGroupByGeo() + "'");
    }
    if (!StringUtils.isEmpty(model.getCountType())) {
      sql = StringUtils.replaceOnce(sql, ":SERVICE_NAME", "'" + model.getCountType() + "'");
    }

    PreparedQuery query = new PreparedQuery(entityManager, sql);

    List<Object[]> results = query.getResults();

    List<String> dates = new ArrayList<String>();
    Map<String, Map<String, Integer>> counts = new HashMap<String, Map<String, Integer>>();
    List<String> geos = new ArrayList<String>();

    String date = null;
    String geo = null;
    Integer count = null;
    Map<String, Integer> countMap = null;
    if (results != null) {

      Date dtFrom = FORMATTER.parse(model.getDateFrom());
      Date dtTo = FORMATTER.parse(model.getDateTo());

      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(dtFrom);
      String dtToAdd = null;
      while (!cal.getTime().after(dtTo)) {
        dtToAdd = FORMATTER.format(cal.getTime());
        if (!dates.contains(dtToAdd)) {
          dates.add(dtToAdd);
        }
        if (!counts.containsKey(dtToAdd)) {
          counts.put(dtToAdd, new HashMap<String, Integer>());
        }
        cal.add(Calendar.DATE, 1);
      }

      for (Object[] result : results) {
        date = (String) result[1];
        geo = (String) result[0];
        count = (Integer) result[2];

        if (!dates.contains(date)) {
          dates.add(date);
        }
        if (!geos.contains(geo)) {
          geos.add(geo);
        }
        if (!counts.containsKey(date)) {
          counts.put(date, new HashMap<String, Integer>());
        }
        countMap = counts.get(date);
        if (!countMap.containsKey(geo)) {
          countMap.put(geo, count);
        }
      }
    }

    MetricsDataSet dataSet = null;

    for (String d : dates) {
      chart.addLabel(export ? d : d.substring(5));
    }
    for (String geoCd : geos) {
      dataSet = new MetricsDataSet();
      dataSet.setLabel(geoCd);
      for (String dateIter : dates) {
        countMap = counts.get(dateIter);
        if (countMap.get(geoCd) != null) {
          dataSet.addData(countMap.get(geoCd));
        } else {
          dataSet.addData(0);
        }
      }
      chart.addDataSet(dataSet);
    }
    return chart;
  }

  public void exportToExcel(MetricsModel model, MetricsChart chart, HttpServletResponse response) throws IOException, ParseException {
    XSSFWorkbook report = new XSSFWorkbook();
    try {
      XSSFSheet data = report.createSheet("Report");

      XSSFRow row = data.createRow(0);
      XSSFCell cell = row.createCell(0);

      XSSFFont bold = report.createFont();
      bold.setBold(true);
      bold.setFontHeight(10);
      XSSFCellStyle boldStyle = report.createCellStyle();
      boldStyle.setWrapText(true);
      boldStyle.setFont(bold);

      XSSFFont regular = report.createFont();
      regular.setFontHeight(10);
      XSSFCellStyle regularStyle = report.createCellStyle();
      regularStyle.setFont(regular);
      regularStyle.setWrapText(true);
      regularStyle.setVerticalAlignment(VerticalAlignment.TOP);

      String title = "";
      String fileName = "SvcUsage_";
      switch (model.getReportType()) {
      case "S":
        title += "Total Service Usage by Partners ";
        fileName += "Totals_";
        break;
      case "N":
        title += model.getCountType() + " Usage by Partners ";
        fileName += model.getCountType().replaceAll(" ", "") + "_";
        break;
      case "P":
        title += model.getGroupByGeo() + " Usage of Services ";
        fileName += model.getGroupByGeo() + "_";
      }

      title += "from " + model.getDateFrom() + " to " + model.getDateTo();
      if (!StringUtils.isBlank(model.getGroupByGeo())) {
        title += " (" + model.getGroupByGeo() + ")";
      }
      fileName += StringUtils.replace(model.getDateFrom(), "-", "") + "-" + StringUtils.replace(model.getDateTo(), "-", "");
      cell.setCellValue(title);
      cell.setCellStyle(boldStyle);

      int currRow = 2;
      int currCol = 0;
      // add the dates
      data.setColumnWidth(currCol, 11 * 256);
      for (String dateLabel : chart.getLabels()) {
        row = data.createRow(currRow);
        cell = row.createCell(currCol);
        cell.setCellValue(dateLabel);
        cell.setCellStyle(regularStyle);
        currRow++;
      }

      currCol = 1;
      currRow = 1;

      // add the criteria
      Collections.sort(chart.getDatasets());
      for (MetricsDataSet dataset : chart.getDatasets()) {
        row = data.getRow(currRow);
        if (row == null) {
          row = data.createRow(currRow);
        }
        cell = row.createCell(currCol);
        data.setColumnWidth(currCol, 17 * 256);
        cell.setCellValue(dataset.getLabel());
        cell.setCellStyle(boldStyle);
        currCol++;
      }

      currCol = 1;
      currRow = 2;
      // add the data
      for (MetricsDataSet dataset : chart.getDatasets()) {
        currRow = 2;
        for (int count : dataset.getData()) {
          row = data.getRow(currRow);
          if (row == null) {
            row = data.createRow(currRow);
          }
          cell = row.createCell(currCol);
          cell.setCellValue(count);
          currRow++;
        }
        currCol++;
      }

      String type = "application/octet-stream";
      response.setContentType(type);
      response.addHeader("Content-Type", type);
      response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"");
      report.write(response.getOutputStream());
    } finally {
      report.close();
    }
  }

  private void validateParams(ParamContainer params) throws CmrException {
    MetricsModel model = (MetricsModel) params.getParam("model");
    try {
      FORMATTER.parse(model.getDateFrom());
      FORMATTER.parse(model.getDateTo());
    } catch (Exception e) {
      throw new CmrException(new Exception("Invalid value from From/To date."));
    }

  }

}
