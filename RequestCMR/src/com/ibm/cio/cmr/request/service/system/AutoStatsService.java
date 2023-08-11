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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.Column;
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
import com.ibm.cio.cmr.request.model.system.AutomationStatsModel;
import com.ibm.cio.cmr.request.model.system.AutomationSummaryModel;
import com.ibm.cio.cmr.request.model.system.MetricsModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.geo.MarketUtil;
import com.ibm.cio.cmr.request.util.metrics.AutomationReviews;
import com.ibm.cio.cmr.request.util.metrics.ReviewCategory;
import com.ibm.cio.cmr.request.util.metrics.ReviewGroup;
import com.ibm.cio.cmr.request.util.system.RequestStatsContainer;
import com.ibm.cio.cmr.request.util.system.StatXLSConfig;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class AutoStatsService extends BaseSimpleService<RequestStatsContainer> {

  private static final Logger LOG = Logger.getLogger(AutoStatsService.class);
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
    String sql = ExternalizedQuery.getSql("METRICS.AUTOMATION_STATS");
    String geoMarket = model.getGroupByGeo();

    sql += " and admin.REQ_ID in (385352, 384920) ";

    if (!StringUtils.isBlank(geoMarket)) {
      String[] parts = geoMarket.split("[-]");
      if (parts.length == 2) {
        if ("GEO".equals(parts[0])) {
          sql += " and data.CMR_ISSUING_CNTRY in (" + MarketUtil.createCountryFilterForGeo(parts[1]) + ")";
        } else if ("MKT".equals(parts[0])) {
          sql += " and data.CMR_ISSUING_CNTRY in (" + MarketUtil.createCountryFilterForMarket(parts[1]) + ")";
        }
      }
    }

    // sql += "and admin.REQ_ID = 564592";
    String procCenter = model.getGroupByProcCenter();
    if (!StringUtils.isBlank(procCenter)) {
      sql += " and data.CMR_ISSUING_CNTRY in (select CMR_ISSUING_CNTRY from CREQCMR.PROC_CENTER where upper(PROC_CENTER_NM) = :PROC_CENTER)";
    }
    if ("Y".equals(model.getExcludeUnsubmitted())) {
      sql += "and admin.REQ_STATUS = 'COM' ";

    }

    if (!StringUtils.isBlank(model.getReqType()) && model.getReqType().length() == 1 && StringUtils.isAlpha(model.getReqType())) {
      sql += "and admin.REQ_TYPE = '" + model.getReqType() + "' ";
    }
    if ("Y".equals(model.getReqType())) {
    }

    if (!StringUtils.isBlank(model.getSourceSystId())) {
      sql += "and admin.SOURCE_SYST_ID = :SOURCE ";
    }

    String issuingCntry = model.getCountry();
    if (!StringUtils.isBlank(issuingCntry)) {
      sql += "and data.CMR_ISSUING_CNTRY = :COUNTRY ";
    }

    if ("Y".equals(model.getExcludeExternal())) {
      sql += "and ( trim(nvl(admin.SOURCE_SYST_ID,'')) = '' or admin.SOURCE_SYST_ID = 'CreateCMR') ";
    }
    if ("Y".equals(model.getExcludeChildRequests())) {
      sql += "and not exists (select 1 from CREQCMR.ADMIN ad where ad.CHILD_REQ_ID = admin.REQ_ID) ";
    }

    if (!"Y".equals(params.getParam("buildSummary"))) {
      sql += " order by admin.REQ_ID";
    }
    sql = StringUtils.replaceOnce(sql, ":CREATE_FROM", "timestamp('" + model.getDateFrom() + " 00:00:00')");
    sql = StringUtils.replaceOnce(sql, ":CREATE_TO", "timestamp('" + model.getDateTo() + " 23:59:59')");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    // query.setParameter("GEO_CD", geo);
    query.setParameter("PROC_CENTER", procCenter != null ? procCenter.toUpperCase().trim() : "");
    query.setParameter("COUNTRY", model.getCountry());
    query.setParameter("CREATE_FROM", from);
    query.setParameter("CREATE_TO", to);
    query.setParameter("SOURCE", model.getSourceSystId());
    query.setForReadOnly(true);

    List<AutomationStatsModel> stats = query.getResults(AutomationStatsModel.class);

    for (AutomationStatsModel stat : stats) {
      if ("Y".equals(stat.getReview())) {
        String processCd = "Unclassified";
        List<ReviewGroup> reviews = AutomationReviews.findCategory(stat);
        if (!reviews.isEmpty()) {
          ReviewGroup group = reviews.get(0);
          processCd = group.getName();
          stat.setProcessCd(processCd);
          ReviewCategory cat = group.getCurrentMatch() != null ? group.getCurrentMatch() : null;
          stat.setSubProcessCd(cat != null ? cat.getName() : "[Unclassified]");
          StringBuilder allRev = new StringBuilder();
          for (ReviewGroup grp : reviews) {
            cat = grp.getCurrentMatch() != null ? grp.getCurrentMatch() : null;
            allRev.append(allRev.length() > 0 ? "\n" : "");
            allRev.append(grp.getName() + " - " + (cat != null ? cat.getName() : "[Unclassified]"));
          }
          stat.setAllReviewCauses(allRev.toString());
        } else {
          stat.setProcessCd("[Unclassified]");
          stat.setAllReviewCauses("[Unclassified]");
        }
      }
    }

    container.setAutomationRecords(stats);

    if ("Y".equals(params.getParam("buildSummary"))) {
      LOG.debug("Building summary for statistics..");
      buildSummaries(container, stats, !StringUtils.isBlank(model.getCountry()), !StringUtils.isBlank(model.getSourceSystId()));
    }
    LOG.debug("Automation data retrieved.");
    return container;
  }

  private void buildSummaries(RequestStatsContainer container, List<AutomationStatsModel> stats, boolean buildCountrySummary,
      boolean buildPartnerSummary) {
    Map<String, AutomationSummaryModel> generalMap = new HashMap<String, AutomationSummaryModel>();
    Map<String, Map<String, AutomationSummaryModel>> weeklyMap = new HashMap<String, Map<String, AutomationSummaryModel>>();
    Map<String, AutomationSummaryModel> scenarioMap = new HashMap<String, AutomationSummaryModel>();
    Map<String, Long> partnerMap = new HashMap<String, Long>();

    String country = null;
    for (AutomationStatsModel stat : stats) {
      if (!generalMap.containsKey(stat.getCmrIssuingCntry())) {
        generalMap.put(stat.getCmrIssuingCntry(), new AutomationSummaryModel());
      }

      AutomationSummaryModel summary = generalMap.get(stat.getCmrIssuingCntry());
      summary.setCountry(stat.getCmrIssuingCntry() + " - " + stat.getCntryDesc());
      summary.setTouchless(summary.getTouchless() + ("Y".equals(stat.getFullAuto()) ? 1 : 0));
      summary.setLegacy(summary.getLegacy() + ("Y".equals(stat.getLegacy()) ? 1 : 0));
      summary.setReview(summary.getReview() + ("Y".equals(stat.getReview()) ? 1 : 0));
      if (!"Y".equals(stat.getFullAuto()) && !"Y".equals(stat.getLegacy()) && !"Y".equals(stat.getReview())) {
        summary.setNoStatus(summary.getNoStatus() + 1);
      }

      // CREATCMR-6871 - update categorization

      // jz 20220823 - remove old
      // if ("Y".equals(stat.getReview()) &&
      // !StringUtils.isBlank(stat.getProcessCd())) {
      // String processCd = stat.getProcessCd();
      // if ("S".equals(stat.getFailureIndc())) {
      // processCd = "System Error";
      // } else if ("CHECKS".equals(processCd)) {
      // processCd = extractProcessCause(stat, null);
      // } else if ("Y".equals(stat.getPaygo())) {
      // processCd = extractProcessCause(stat, processCd);
      // }
      // if (summary.getReviewMap().get(processCd) == null) {
      // summary.getReviewMap().put(processCd, (long) 0);
      // }
      // Map<String, Long> revMap = summary.getReviewMap();
      // revMap.put(processCd, revMap.get(processCd) + 1);
      // }

      if ("Y".equals(stat.getReview())) {
        String processCd = stat.getProcessCd();
        summary.getReviewMap().putIfAbsent(processCd, (long) 0);
        Map<String, Long> revMap = summary.getReviewMap();
        revMap.put(processCd, revMap.get(processCd) + 1);
      }

      if (buildCountrySummary) {
        country = stat.getCmrIssuingCntry();
        if (!weeklyMap.containsKey(stat.getCmrIssuingCntry())) {
          weeklyMap.put(stat.getCmrIssuingCntry(), new HashMap<String, AutomationSummaryModel>());
        }

        Map<String, AutomationSummaryModel> summaryMap = weeklyMap.get(stat.getCmrIssuingCntry());
        if (!summaryMap.containsKey(stat.getWeekOf())) {
          summaryMap.put(stat.getWeekOf(), new AutomationSummaryModel());
        }
        summary = summaryMap.get(stat.getWeekOf());
        summary.setCountry(stat.getCmrIssuingCntry() + " - " + stat.getCntryDesc());
        summary.setTouchless(summary.getTouchless() + ("Y".equals(stat.getFullAuto()) ? 1 : 0));
        summary.setLegacy(summary.getLegacy() + ("Y".equals(stat.getLegacy()) ? 1 : 0));
        summary.setReview(summary.getReview() + ("Y".equals(stat.getReview()) ? 1 : 0));
        if (!"Y".equals(stat.getFullAuto()) && !"Y".equals(stat.getLegacy()) && !"Y".equals(stat.getReview())) {
          summary.setNoStatus(summary.getNoStatus() + 1);
        }

        container.setWeeklyAutomationSummary(weeklyMap);

        String scenario = null;
        if ("Create".equals(stat.getReqType())) {
          scenario = stat.getCustGrp() != null && stat.getCustGrp().toUpperCase().contains("CROSS") ? "(Cross-border)" : stat.getCustSubGrp();
        } else {
          scenario = "-" + stat.getReqType() + "-";
        }
        if (StringUtils.isBlank(scenario)) {
          scenario = "[unspecified]";
        }
        if (!scenarioMap.containsKey(scenario)) {
          scenarioMap.put(scenario, new AutomationSummaryModel());
        }
        summary = scenarioMap.get(scenario);
        summary.setCountry(stat.getCmrIssuingCntry() + " - " + stat.getCntryDesc());
        summary.setTouchless(summary.getTouchless() + ("Y".equals(stat.getFullAuto()) ? 1 : 0));
        summary.setLegacy(summary.getLegacy() + ("Y".equals(stat.getLegacy()) ? 1 : 0));
        summary.setReview(summary.getReview() + ("Y".equals(stat.getReview()) ? 1 : 0));
        if (!"Y".equals(stat.getFullAuto()) && !"Y".equals(stat.getLegacy()) && !"Y".equals(stat.getReview())) {
          summary.setNoStatus(summary.getNoStatus() + 1);
        }
      }

      if (buildPartnerSummary) {
        if (!partnerMap.containsKey("touchless")) {
          partnerMap.put("touchless", (long) 0);
        }
        if (!partnerMap.containsKey("touchlessTotal")) {
          partnerMap.put("touchlessTotal", (long) 0);
        }
        if (!partnerMap.containsKey("review")) {
          partnerMap.put("review", (long) 0);
        }
        if (!partnerMap.containsKey("reviewTotal")) {
          partnerMap.put("reviewTotal", (long) 0);
        }
        if ("Y".equals(stat.getPool()) && "Update".equals(stat.getReqType())) {
        } else {
          partnerMap.put("touchless", "Y".equals(stat.getFullAuto()) ? partnerMap.get("touchless") + 1 : partnerMap.get("touchless"));
          partnerMap.put("review", "Y".equals(stat.getReview()) ? partnerMap.get("review") + 1 : partnerMap.get("review"));
          partnerMap.put("touchlessTotal",
              "Y".equals(stat.getFullAuto()) ? partnerMap.get("touchlessTotal") + stat.getOverallTat() : partnerMap.get("touchlessTotal"));
          partnerMap.put("reviewTotal",
              "Y".equals(stat.getReview()) ? partnerMap.get("reviewTotal") + stat.getOverallTat() : partnerMap.get("reviewTotal"));
        }
      }
    }
    container.setAutomationSummary(generalMap);
    if (buildCountrySummary) {
      Map<String, AutomationSummaryModel> raw = weeklyMap.get(country);
      if (raw != null) {
        Map<String, AutomationSummaryModel> ordered = new LinkedHashMap<>();
        List<String> weekKeys = new ArrayList<String>();
        weekKeys.addAll(raw.keySet());
        Collections.sort(weekKeys);
        for (String week : weekKeys) {
          ordered.put(week, raw.get(week));
        }
        weeklyMap.put(country, ordered);
      }
      container.setWeeklyAutomationSummary(weeklyMap);
      container.setScenarioSummary(scenarioMap);
    }
    if (buildPartnerSummary) {
      container.setPartnerSummary(partnerMap);
    }
  }

  /**
   * 
   * @param stat
   * @param defaultProcess
   * @return
   * @deprecated - recompute using {@link AutomationReviews} flow
   */
  @Deprecated
  protected String extractProcessCause(AutomationStatsModel stat, String defaultProcess) {
    String cmt = stat.getCmt();
    if (StringUtils.isBlank(cmt)) {
      return "Other Checks";
    }
    cmt = cmt.toUpperCase();
    if (cmt.contains("SCENARIO")) {
      return "Non-auto Scenario";
    } else if (cmt.contains("BRANCH OFFICE CODES")) {
      return "BO Codes";
    } else if (cmt.contains("CITY AND/OR COUNTY")) {
      return "City/County";
    } else if (cmt.contains("BUYING GROUP ID UNDER COVERAGE")) {
      return "BG/GBG Issue";
    } else if (cmt.contains("THE OVERRIDE VALUE COULD NOT BE DETERMINED")) {
      return "Coverage Issue";
    } else if (cmt.contains("DPL CHECK FAILED")) {
      return "Global - DPL Check";
    } else if (cmt.contains("D&B matches were chosen to be overridden".toUpperCase())) {
      return "D&B Overrides";
    } else if (cmt.contains("D&B".toUpperCase())) {
      return "D&B - Others";
    } else if (cmt.contains("VAT VALUE DID NOT MATCH WITH")) {
      return "VAT Mismatch";
    } else if (cmt.contains("UPDATED ELEMENTS CANNOT")) {
      return "Update Checks";
    }
    return defaultProcess != null ? defaultProcess : "Other Checks";
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
  @SuppressWarnings("rawtypes")
  public void exportToExcel(RequestStatsContainer container, MetricsModel model, HttpServletResponse response)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    if (config == null) {
      initConfig();
    }

    List<AutomationStatsModel> stats = container.getAutomationRecords();
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
      String title = "Automation Statistics";
      if (model != null) {
        title = "Automation Statistics from " + model.getDateFrom() + " to " + model.getDateTo();
        if (!StringUtils.isEmpty(model.getCountry())) {
          title += " for " + model.getCountry();
        } else {
          if (!StringUtils.isEmpty(model.getGroupByProcCenter())) {
            title += " for " + model.getGroupByProcCenter();
          }
          if (!StringUtils.isEmpty(model.getGroupByGeo())) {
            title += " (" + model.getGroupByGeo() + ")";
          }
        }
      }
      cell.setCellValue(title);

      // create headers
      XSSFRow header = sheet.createRow(1);
      createHeaders(header, boldStyle, drawing, config, helper);

      // add the data
      XSSFRow row = null;
      int current = 2;
      for (AutomationStatsModel request : stats) {
        row = sheet.createRow(current);
        createDataLine(row, request, regularStyle, config);
        current++;
      }
      String type = "application/octet-stream";
      String fileName = "AutomationStats_";
      if (model != null) {
        fileName += StringUtils.replace(model.getDateFrom(), "-", "");
        fileName += "-";
        fileName += StringUtils.replace(model.getDateTo(), "-", "");
        if (!StringUtils.isEmpty(model.getCountry())) {
          fileName += "_" + model.getCountry();
        } else {
          if (!StringUtils.isEmpty(model.getGroupByProcCenter())) {
            fileName += "_" + model.getGroupByProcCenter();
          }
          if (!StringUtils.isBlank(model.getGroupByGeo())) {
            fileName += "_" + model.getGroupByGeo();
          }
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
  @SuppressWarnings("rawtypes")
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
  @SuppressWarnings("rawtypes")
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

  private void createDataLine(XSSFRow row, AutomationStatsModel request, XSSFCellStyle style, List<StatXLSConfig> config)
      throws IllegalArgumentException, IllegalAccessException {
    XSSFCell cell = null;

    Object value = null;
    StatXLSConfig sc = null;
    for (int i = 0; i < config.size(); i++) {
      sc = config.get(i);
      cell = row.createCell(i);
      cell.setCellStyle(style);
      value = getValue(sc.getDbField(), request);
      if ("GEO".equals(sc.getDbField())) {
        cell.setCellValue(MarketUtil.getGEO(value.toString().trim()));
      } else if ("MARKET".equals(sc.getDbField())) {
        cell.setCellValue(MarketUtil.getMarket(value.toString().trim()));
      } else if ("PROCESS_CD".equals(sc.getDbField())) {
        String processCd = request.getProcessCd();
        // if (!"Y".equals(request.getReview())) {
        // processCd = "";
        // } else if ("Y".equals(request.getPaygo())) {
        // processCd = extractProcessCause(request, processCd);
        // }
        cell.setCellValue(processCd);
      } else if ("OVERALL_TAT".equals(sc.getDbField())) {
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

  private Object getValue(String columnName, AutomationStatsModel request) throws IllegalArgumentException, IllegalAccessException {
    Column col = null;

    if ("ERROR_CHECKS".equals(columnName)) {
      if (!"Y".equals(request.getReview())) {
        return "";
      }
      if ("Y".equals(request.getFullAuto())) {
        return "";
      }
      return request.getSubProcessCd();
    }
    if ("ALL_REVIEWS".equals(columnName)) {
      return request.getAllReviewCauses();
    }
    if ("AUTO_COMMENT".equals(columnName)) {
      if (!StringUtils.isBlank(request.getAutoComment())) {
        return request.getAutoComment();
      }
      if (!StringUtils.isBlank(request.getErrorCmt())) {
        return request.getErrorCmt();
      }
      if (!StringUtils.isBlank(request.getForceCmt())) {
        return "Forced Status Change";
      }
      return "";
    }
    if (columnName.equals("REQ_ID") && (request instanceof AutomationStatsModel)) {
      return request.getId().getReqId();
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

    config.add(new StatXLSConfig("Request ID", "REQ_ID", 16, null));
    config.add(new StatXLSConfig("Country Code", "CMR_ISSUING_CNTRY", 12, null));
    config.add(new StatXLSConfig("Country", "CNTRY_DESC", 25, null));
    config.add(new StatXLSConfig("Geography", "GEO", 10, null));
    config.add(new StatXLSConfig("Market", "MARKET", 10, null));
    config.add(new StatXLSConfig("Request Type", "REQ_TYPE", 18, null));
    config.add(new StatXLSConfig("Request Status", "REQ_STATUS", 25, null));
    config.add(new StatXLSConfig("Customer Name", "CUST_NM", 35, null));
    config.add(new StatXLSConfig("CMR No.", "CMR_NO", 10, null));
    config.add(new StatXLSConfig("VAT / GST#", "VAT", 20, null));
    config.add(new StatXLSConfig("Create Date", "CREATE_TS", 18, null));
    config.add(new StatXLSConfig("Scenario Code", "CUST_GRP", 20, null));
    config.add(new StatXLSConfig("Scenario Subtype Code", "CUST_SUB_GRP", 20, null));
    config.add(new StatXLSConfig("Source System", "SOURCE_SYST_ID", 20, null));
    config.add(new StatXLSConfig("PayGo", "PAYGO_INDC", 10, null));
    config.add(new StatXLSConfig("Pool", "POOL", 10, null));
    config.add(new StatXLSConfig("DnB Matching", "DNB_MATCHING_RESULT", 16, "Indicates whether the DnB matches were found."));
    config.add(new StatXLSConfig("RPA Matching", "RPA_MATCHING_RESULT", 16, "Indicates whether the RPA matches were found."));
    config.add(new StatXLSConfig("Touchless", "FULL_AUTO", 16, "Indicates whether the request was completed without any form of manual work."));
    config.add(new StatXLSConfig("Review Required", "REVIEW", 16, "Indicates whether the request needed manual CMDE review."));
    config.add(new StatXLSConfig("Review Category", "PROCESS_CD", 25, "Main category for the review cause"));
    config.add(new StatXLSConfig("Review Sub-category", "ERROR_CHECKS", 25, "Sub-category of the review cause under the main category"));
    config.add(new StatXLSConfig("Failure Type", "FAILURE_INDC", 16,
        "Specifies the automation element that caused automation to stop. S - System Error, P - Processing Error"));
    config.add(new StatXLSConfig("Rejected", "REJECT", 16, "Indicates whether the request was rejected by the automation engine."));
    config.add(new StatXLSConfig("Reject Reason", "REJ_REASON", 25, "Indicates whether the first rejection reason for the request."));
    config
        .add(new StatXLSConfig("Overall TAT", "OVERALL_TAT", 25, "Indicates the total time (hh:mm:ss) it took from request creation to completion."));
    config.add(new StatXLSConfig("Review Comments", "AUTO_COMMENT", 25, "Comment added by Automation Engine which caused the review."));
    config.add(new StatXLSConfig("All Categories", "ALL_REVIEWS", 25, "All Matched Categories for the review"));
    // config.add(new StatXLSConfig("Error Comment", "ERROR_CMT", 25,
    // "Processing error comments."));

  }

}
