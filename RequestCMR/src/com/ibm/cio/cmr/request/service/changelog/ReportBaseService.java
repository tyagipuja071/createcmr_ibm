package com.ibm.cio.cmr.request.service.changelog;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.ibm.cio.cmr.request.model.changelog.ChangeLogModel;

public class ReportBaseService {

  private OutputStream outputStream;

  private SXSSFWorkbook sxssfWorkBook;

  public ReportBaseService() {

  }

  public OutputStream getOutputStream() {
    return outputStream;
  }

  public void setOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public SXSSFWorkbook getSxssfWorkBook() {
    return sxssfWorkBook;
  }

  public void setSxssfWorkBook(SXSSFWorkbook sxssfWorkBook) {
    this.sxssfWorkBook = sxssfWorkBook;
  }

  public ReportBaseService(OutputStream pOutputStream) {
    super();

    setOutputStream(pOutputStream);
  }

  public void getReport(ChangeLogModel model, HttpServletRequest request, String reportName, SXSSFWorkbook pxwb,
      OutputStream pOutputStream){
    
  }

  void printHeader(String psReportName, String[] psaColumnHeader, Sheet sheet) {
    Object[] reportHeader = new Object[] { psReportName };

    Row rowhead1 = sheet.createRow((int) 0);
    outputData(reportHeader, rowhead1);
    Row rowhead2 = sheet.createRow((int) 1);
    outputData(psaColumnHeader, rowhead2);
  }

  String outputData(Object[] poaData, Row rowct) {
    StringBuffer lsbData = new StringBuffer();
    String lsData = null;

    for (int i = 0; poaData != null && i < poaData.length; i++) {
      lsData = poaData[i] == null ? "" : poaData[i].toString();
      rowct.createCell((int) i).setCellValue(lsData);
    }
    return lsbData.toString();
  }

  protected Logger initLogger() {
    // TODO Auto-generated method stub
    return null;
  }
}
