/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.FieldInfo;
import com.ibm.cio.cmr.request.entity.USCMRScc;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.code.SCCModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.util.system.StatXLSConfig;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class SCCService extends BaseService<SCCModel, USCMRScc> {

  private static List<StatXLSConfig> config1 = null;

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SCCService.class);
  }

  @Override
  protected void performTransaction(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    if ("MASS_DELETE".equals(model.getMassAction())) {
      List<KeyContainer> keys = extractKeys(model);
      String fieldId = null;
      String cntry = null;
      String seqNo = null;
      FieldInfo info = null;
      String sql = "select * from CREQCMR.FIELD_INFO where FIELD_ID = :ID and CMR_ISSUING_CNTRY = :CNTRY and SEQ_NO = :SEQ_NO";
      PreparedQuery query = null;
      for (KeyContainer key : keys) {
        fieldId = key.getKey("fieldId");
        cntry = key.getKey("cmrIssuingCntry");
        seqNo = key.getKey("seqNo");
        if ("1".equals(seqNo)) {
          // sequence 1 cannot be deleted
          throw new CmrException(24);
        }
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("ID", fieldId);
        query.setParameter("CNTRY", cntry);
        query.setParameter("SEQ_NO", Integer.parseInt(seqNo));
        info = query.getSingleResult(FieldInfo.class);
        if (info != null) {
          deleteEntity(info, entityManager);
        }
      }
    }
  }

  @Override
  protected List<SCCModel> doSearch(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    String sql = ExternalizedQuery.getSql("SYSTEM.SCCLIST");
    String city = request.getParameter("nCity");
    String state = request.getParameter("nSt");
    String county = request.getParameter("nCnty");
    String land = request.getParameter("nLand");
    if (StringUtils.isBlank(state)) {
      // state = "xxx"; // to retrieve nothing
      state = ""; // to retrieve all state
    }
    if (StringUtils.isBlank(city)) {
      city = ""; // to retrieve all cities
    }
    if (StringUtils.isBlank(county)) {
      county = ""; // to retrieve all counties
    }
    if (StringUtils.isBlank(land)) {
      land = ""; // to retrieve all land
    }
    if (StringUtils.isBlank(land) && StringUtils.isBlank(state)) {
      state = "xxx"; // to retrieve nothing
    }
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("STATE", "%" + state.toUpperCase() + "%");
    q.setParameter("CITY", "%" + city.toUpperCase() + "%");
    q.setParameter("COUNTY", "%" + county.toUpperCase() + "%");
    q.setParameter("LAND", "%" + land.toUpperCase() + "%");
    q.setForReadOnly(true);
    List<USCMRScc> sccList = q.getResults(USCMRScc.class);
    List<SCCModel> list = new ArrayList<>();
    SCCModel sccModel = null;
    for (USCMRScc scc : sccList) {
      sccModel = new SCCModel();
      sccModel.setSccId(scc.getId().getSccId());
      sccModel.setcZip(scc.getcZip());
      sccModel.setnCity(scc.getnCity());
      sccModel.setnSt(scc.getnSt());
      sccModel.setnCnty(scc.getnCnty());
      sccModel.setcCnty(scc.getcCnty());
      sccModel.setcCity(scc.getcCity());
      sccModel.setcSt(scc.getcSt());
      sccModel.setnLand(scc.getnLand());
      list.add(sccModel);
    }
    return list;
  }

  @Override
  protected USCMRScc getCurrentRecord(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected USCMRScc createFromModel(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

  // CREATCMR-9311:export the scc list to excel file
  public void exportToExcel(List<SCCModel> results, HttpServletResponse response) throws IOException {
    if (config1 == null) {
      initConfig1();
    }

    log.info("Exporting records to excel..");

    XSSFWorkbook report = new XSSFWorkbook();
    try {

      XSSFSheet sheet0 = report.createSheet("SCCList");

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
      createHeaders(header1, boldStyle, config1);

      // add the data
      XSSFRow row0 = null;
      int current0 = 1;

      for (SCCModel obj : results) {
        row0 = sheet0.createRow(current0);

        int cellInd = 0;
        XSSFCell cell = row0.createCell(cellInd++);
        cell.setCellStyle(regularStyle);
        cell.setCellValue(obj.getnLand());
        cell = row0.createCell(cellInd++);
        cell.setCellStyle(regularStyle);
        float cSt = obj.getcSt();
        String formattedSt = String.format("%02d", (int) cSt);
        formattedSt = formattedSt.substring(0, 2);
        cell.setCellValue(formattedSt);

        cell = row0.createCell(cellInd++);
        cell.setCellStyle(regularStyle);
        float cCnty = obj.getcCnty();
        String formattedCnty = String.format("%03d", (int) cCnty);
        formattedCnty = formattedCnty.substring(0, 3);
        cell.setCellValue(formattedCnty);

        cell = row0.createCell(cellInd++);
        cell.setCellStyle(regularStyle);
        float cCity = obj.getcCity();
        String formattedCity = String.format("%04d", (int) cCity);
        formattedCity = formattedCity.substring(0, 4);
        cell.setCellValue(formattedCity);

        cell = row0.createCell(cellInd++);
        cell.setCellStyle(regularStyle);
        cell.setCellValue(obj.getnSt());

        cell = row0.createCell(cellInd++);
        cell.setCellStyle(regularStyle);
        cell.setCellValue(obj.getnCnty());

        cell = row0.createCell(cellInd++);
        cell.setCellStyle(regularStyle);
        cell.setCellValue(obj.getnCity());

        cell = row0.createCell(cellInd++);
        cell.setCellStyle(regularStyle);
        int cZip = obj.getcZip();
        String formattedZip = String.format("%05d", cZip);
        formattedZip = formattedZip.substring(0, 5);
        cell.setCellValue(formattedZip);

        current0++;
      }

      if (response != null) {
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Type", "application/octet-stream");
        response.addHeader("Content-Disposition", "attachment; filename=\"SCC List.xlsx\"");
        report.write(response.getOutputStream());
      }
      log.info("Export records to excel successfully.");
    } finally {
      report.close();
    }
  }

  private void initConfig1() {
    config1 = new ArrayList<StatXLSConfig>();

    config1.add(new StatXLSConfig("Land Cntry", "LAND_CNTRY", 12, null));
    config1.add(new StatXLSConfig("State Code", "C_ST", 12, null));
    config1.add(new StatXLSConfig("County Code", "C_CNTY", 12, null));
    config1.add(new StatXLSConfig("City Code", "C_CITY", 12, null));
    config1.add(new StatXLSConfig("State", "N_ST", 20, null));
    config1.add(new StatXLSConfig("County / Country", "N_CNTY", 20, null));
    config1.add(new StatXLSConfig("City", "N_CITY", 20, null));
    config1.add(new StatXLSConfig("Zip Code", "C_ZIP", 12, null));
  }

  private void createHeaders(XSSFRow header, XSSFCellStyle style, List<StatXLSConfig> config) {
    XSSFCell cell = null;

    StatXLSConfig sc = null;
    for (int i = 0; i < config.size(); i++) {
      sc = config.get(i);
      cell = header.createCell(i);
      cell.setCellValue(sc.getLabel());
      cell.setCellStyle(style);
    }
  }
  // CREATCMR-9311 end
}
