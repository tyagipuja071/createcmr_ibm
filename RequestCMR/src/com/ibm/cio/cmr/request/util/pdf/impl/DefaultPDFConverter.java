package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.window.RequestSummaryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.pdf.PDFConverter;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

/**
 * Generates PDF files for a <strong>single-request</strong> type
 * (Create,Update)
 * 
 * @author Jeffrey Zamora
 * 
 */
public class DefaultPDFConverter implements PDFConverter {

  private static final Logger LOG = Logger.getLogger(DefaultPDFConverter.class);

  private PdfFont regularFont;
  private PdfFont boldFont;
  private String cmrIssuingCntry;

  /**
   * New instance
   * 
   * @throws IOException
   */
  public DefaultPDFConverter(String cmrIssuingCntry) throws IOException {
    this.regularFont = PdfFontFactory.createFont(FontConstants.HELVETICA);
    this.boldFont = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  /**
   * Exports the request details into a PDF file written to the supplied
   * outputstream. Only Create and Update types are supported
   * 
   * @param entityManager
   * @param reqId
   * @param os
   * @return
   * @throws IOException
   */
  @Override
  public boolean exportToPdf(EntityManager entityManager, Admin admin, Data data, OutputStream os, String sysLoc) throws IOException {

    try {
      PdfWriter writer = new PdfWriter(os);
      try {
        PdfDocument pdf = new PdfDocument(writer);
        try {
          Document document = new Document(pdf);
          try {

            document.add(createHeader("Details for Request ID " + admin.getId().getReqId()));

            document.add(blankLine());
            addMainDetails(admin, data, document);

            if (Arrays.asList("CREATE", "UPDATE", "C", "U").contains(admin.getReqType().toUpperCase())) {
              document.add(blankLine());
              Addr soldTo = addAddressDetails(admin, data, entityManager, document);

              document.add(blankLine());
              addCustomerDetails(entityManager, data, document);

              document.add(blankLine());
              addIBMDetails(entityManager, data, document);

              addExtraSections(entityManager, admin, data, sysLoc, soldTo, document);

              if (Arrays.asList("UPDATE", "U").contains(admin.getReqType().toUpperCase())) {
                // additional section to highlight updates
                addUpdatedDataSection(entityManager, admin, data, sysLoc, document);
              }
            }

            if (Arrays.asList("REACTIVATE", "DELETE", "R", "D").contains(admin.getReqType().toUpperCase())) {
              document.add(blankLine());
              addDeleteReactivateDetails(admin, entityManager, document);
            }

          } finally {
            document.close();
          }
        } finally {
          pdf.close();
        }
      } finally {
        writer.close();
      }
      return true;
    } catch (IOException io) {
      LOG.warn("IO Error in Generating PDF for Request ID " + admin.getId().getReqId() + ":" + io.getMessage());
      return true;
    } catch (Exception e) {
      LOG.error("Error in Generating PDF for Request ID " + admin.getId().getReqId(), e);
      return false;
    }
  }

  protected void addDeleteReactivateDetails(Admin admin, EntityManager entityManager, Document document) {
    document.add(createSubHeader("List of CMRs"));

    String sql = ExternalizedQuery.getSql("DELETE.REACTIVATE.LIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("REQ_ID", admin.getId().getReqId());

    List<Object[]> records = query.getResults();
    for (Object[] record : records) {
      Table cmrTable = createDetailsTable();
      cmrTable.addCell(createLabelCell("CMR No"));
      cmrTable.addCell(createValueCell((String) record[1]));
      cmrTable.addCell(createLabelCell("Name"));
      cmrTable.addCell(createValueCell((String) record[2]));
      cmrTable.addCell(createLabelCell("Order Block"));
      cmrTable.addCell(createValueCell((String) record[3]));
      cmrTable.addCell(createLabelCell("Inactive"));
      cmrTable.addCell(createValueCell("X".equals(record[4]) ? "Yes" : ""));
      document.add(cmrTable);
      document.add(blankLine());
    }
  }

  /**
   * Adds the address information for the request
   * 
   * @param admin
   * @param entityManager
   * @param document
   */
  protected Addr addAddressDetails(Admin admin, Data data, EntityManager entityManager, Document document) {
    document.add(createSubHeader("Address Information"));

    List<Addr> addresses = getAddressList(entityManager, admin.getId().getReqId());

    Addr soldTo = null;
    for (Addr addr : addresses) {
      Table address = createDetailsTable();

      GEOHandler handler = RequestUtils.getGEOHandler(this.cmrIssuingCntry);

      if (handler.customerNamesOnAddress()) {
        address.addCell(createLabelCell("Customer Name:"));
        address.addCell(createValueCell(addr.getCustNm1(), 1, 3));
        address.addCell(createLabelCell("Customer Name Con't:"));
        address.addCell(createValueCell(addr.getCustNm2(), 1, 3));
      }
      address.addCell(createLabelCell("Address Type:"));
      address.addCell(createValueCell(addr.getId().getAddrType()));
      address.addCell(createLabelCell("Landed Country:"));
      address.addCell(createValueCell(addr.getLandCntry()));

      String lbl = "Address";
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getAddrTxt() + (StringUtils.isBlank(addr.getAddrTxt2()) ? "" : " " + addr.getAddrTxt2()), 1, 3));

      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "StateProv", "State/Province");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getStateProv()));
      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "PostalCode", "Postal Code");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getPostCd()));

      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "Department", "Department");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getDept()));
      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "Division", "Division");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getDivn()));

      address.addCell(createLabelCell("DPL Check Result:"));
      String dplCheck = addr.getDplChkResult();
      String dplCheckText = "";
      if ("P".equals(dplCheck)) {
        dplCheckText = "Passed";
      } else if ("F".equals(dplCheck)) {
        dplCheckText = "FAILED";
      } else if ("N".equals(dplCheck)) {
        dplCheckText = "Not Required";
      } else {
        dplCheckText = "Not Done";
      }
      Cell dplCell = createValueCell(dplCheckText, 1, 3);
      if ("F".equals(dplCheck)) {
        dplCell.setFontColor(Color.RED);
      }
      address.addCell(dplCell);

      if ("P".equals(dplCheck) || "F".equals(dplCheck)) {
        address.addCell(createLabelCell("DPL Check Date:"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        address.addCell(createValueCell(addr.getDplChkTs() != null ? sdf.format(addr.getDplChkTs()) : ""));
        if ("F".equals(dplCheck)) {
          address.addCell(createLabelCell("DPL Error Information:"));
          address.addCell(createValueCell(addr.getDplChkInfo()));
        } else {
          address.addCell(createValueCell(null));
          address.addCell(createValueCell(null));
        }
      }

      addConverterAddressDetails(entityManager, address, addr);

      document.add(address);
      document.add(blankLine());

      if (addr.getId() != null && addr.getId().getAddrType() != null && addr.getId().getAddrType().contains("(ZS01)")) {
        soldTo = addr;
      }
    }
    return soldTo;
  }

  protected void addConverterAddressDetails(EntityManager entityManager, Table address, Addr addr) {
    // NOOP
  }

  /**
   * Gets the list of addresses belonging to the request. Can be overwritten on
   * each converter
   * 
   * @param entityManager
   * @param reqId
   * @return
   */
  protected List<Addr> getAddressList(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("PDF.GETADDRESS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("REQ_ID", reqId);
    return query.getResults(Addr.class);
  }

  /**
   * Adds main details
   * 
   * @param admin
   * @param data
   * @param document
   */
  protected void addMainDetails(Admin admin, Data data, Document document) {
    document.add(createSubHeader("General Information"));
    Table main = createDetailsTable();

    main.addCell(createLabelCell("Request ID:"));
    main.addCell(createValueCell(admin.getId().getReqId() + ""));
    main.addCell(createLabelCell("Request Type:"));
    if ("Y".equals(admin.getProspLegalInd())) {
      main.addCell(createValueCell(admin.getReqType() + " (Prospect to Legal Conversion)"));
    } else {
      main.addCell(createValueCell(admin.getReqType()));
    }

    main.addCell(createLabelCell("Requester:"));
    main.addCell(createValueCell(admin.getRequesterNm()));
    main.addCell(createLabelCell("Originator:"));
    main.addCell(createValueCell(admin.getOriginatorNm()));

    main.addCell(createLabelCell("CMR Issuing Country:"));
    main.addCell(createValueCell(data.getCmrIssuingCntry()));
    main.addCell(createLabelCell("CMR No:"));
    main.addCell(createValueCell(data.getCmrNo()));

    main.addCell(createLabelCell("Customer Name:"));
    String customerName = StringUtils.isBlank(admin.getMainCustNm1()) ? "" : admin.getMainCustNm1();
    if (!StringUtils.isBlank(admin.getMainCustNm2())) {
      customerName += " " + admin.getMainCustNm2();
    }
    main.addCell(createValueCell(customerName, 1, 3));

    main.addCell(createLabelCell("Requesting LOB:"));
    main.addCell(createValueCell(admin.getRequestingLob()));
    main.addCell(createLabelCell("Request Reason:"));
    main.addCell(createValueCell(admin.getReqReason()));
    document.add(main);
  }

  /**
   * Adds customer information details
   * 
   * @param data
   * @param document
   */
  protected void addCustomerDetails(EntityManager entityManager, Data data, Document document) {
    document.add(createSubHeader("Customer Information"));
    Table customer = createDetailsTable();
    customer.addCell(createLabelCell("Abbreviated Name:"));
    customer.addCell(createValueCell(data.getAbbrevNm()));
    // customer.addCell(createLabelCell("Preferred Language:"));
    // customer.addCell(createValueCell(data.getCustPrefLang()));

    customer.addCell(createLabelCell("Subindustry:"));
    customer.addCell(createValueCell(data.getSubIndustryCd()));
    customer.addCell(createLabelCell("ISIC:"));
    customer.addCell(createValueCell(data.getIsicCd()));

    addConverterCustomerDetails(entityManager, customer, data);

    document.add(customer);
  }

  protected void addConverterCustomerDetails(EntityManager entityManager, Table customer, Data data) {
    // NOOP
  }

  protected void addIBMDetails(EntityManager entityManager, Data data, Document document) {
    document.add(createSubHeader("IBM Information"));
    Table ibm = createDetailsTable();

    ibm.addCell(createLabelCell("Enterprise No.:"));
    ibm.addCell(createValueCell(data.getEnterprise()));
    ibm.addCell(createLabelCell("Company No.:"));
    ibm.addCell(createValueCell(data.getCompany()));
    ibm.addCell(createLabelCell("Affiliate No.:"));
    ibm.addCell(createValueCell(data.getAffiliate(), 1, 3));

    ibm.addCell(createLabelCell("Search Term (SORTL):"));
    ibm.addCell(createValueCell(data.getSearchTerm()));
    ibm.addCell(createLabelCell("ISU Code:"));
    ibm.addCell(createValueCell(data.getIsuCd()));

    ibm.addCell(createLabelCell("INAC Type:"));
    ibm.addCell(createValueCell(data.getInacType()));
    ibm.addCell(createLabelCell("INAC Code:"));
    ibm.addCell(createValueCell(data.getInacCd()));

    ibm.addCell(createLabelCell("Coverage:"));
    if (data.getCovId() != null) {
      ibm.addCell(createValueCell(data.getCovId() + (data.getCovDesc() != null ? " - " + data.getCovDesc() : "")));
    } else {
      ibm.addCell(createValueCell(null));
    }
    ibm.addCell(createLabelCell("Client Tier:"));
    ibm.addCell(createValueCell(data.getClientTier()));

    ibm.addCell(createLabelCell("Buying Group:"));
    if (!StringUtils.isEmpty(data.getBgId())) {
      ibm.addCell(createValueCell(data.getBgId() + " - " + data.getBgDesc()));
    } else {
      ibm.addCell(createValueCell(null));
    }

    ibm.addCell(createLabelCell("Global Buying Group:"));
    if (!StringUtils.isEmpty(data.getGbgId())) {
      ibm.addCell(createValueCell(data.getGbgId() + " - " + data.getGbgDesc()));
    } else {
      ibm.addCell(createValueCell(null));
    }

    ibm.addCell(createLabelCell("GEO Location Code:"));
    if (!StringUtils.isEmpty(data.getGeoLocationCd())) {
      ibm.addCell(createValueCell(data.getGeoLocationCd() + " - " + data.getGeoLocDesc()));
    } else {
      ibm.addCell(createValueCell(null));
    }

    ibm.addCell(createLabelCell("DUNS:"));
    ibm.addCell(createValueCell(data.getDunsNo()));

    addConverterIBMDetails(entityManager, ibm, data);

    document.add(ibm);
  }

  protected void addConverterIBMDetails(EntityManager entityManager, Table ibm, Data data) {
    // NOOP
  }

  protected void addExtraSections(EntityManager entityManager, Admin admin, Data data, String sysLoc, Addr soldTo, Document document) {
    // noop
  }

  /**
   * Appends the updated data details as the LAST section
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @param sysLoc
   * @param document
   * @throws CmrException
   */
  protected void addUpdatedDataSection(EntityManager entityManager, Admin admin, Data data, String sysLoc, Document document) throws CmrException {
    RequestSummaryService service = new RequestSummaryService();
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", admin.getId().getReqId());
    RequestSummaryModel summary = service.process(null, params);

    Data newData = summary.getData();

    List<UpdatedDataModel> allData = service.getUpdatedData(newData, admin.getId().getReqId(), RequestSummaryService.TYPE_CUSTOMER);
    allData.addAll(service.getUpdatedData(newData, admin.getId().getReqId(), RequestSummaryService.TYPE_IBM));

    if (allData.size() > 0) {
      document.add(blankLine());
      document.add(createSubHeader("Updated Data"));
      Table updateTable = createDetailsTable(new float[] { 26, 37, 37 });
      updateTable.addCell(createLabelCell("Data Field"));
      updateTable.addCell(createLabelCell("Original CMR Data"));
      updateTable.addCell(createLabelCell("Change to Data"));
      for (UpdatedDataModel update : allData) {
        updateTable.addCell(createValueCell(update.getDataField()));
        updateTable.addCell(createValueCell(update.getOldData()));
        updateTable.addCell(createValueCell(update.getNewData()));
      }
      document.add(updateTable);
    }

    List<UpdatedNameAddrModel> addrData = service.getUpdatedNameAddr(admin.getId().getReqId());
    if (addrData.size() > 0) {
      document.add(blankLine());
      document.add(createSubHeader("Updated Address Data"));
      Table updateTable = createDetailsTable(new float[] { 18, 18, 32, 32 });
      updateTable.addCell(createLabelCell("Address Type"));
      updateTable.addCell(createLabelCell("Data Field"));
      updateTable.addCell(createLabelCell("Original CMR Data"));
      updateTable.addCell(createLabelCell("Change to Data"));
      for (UpdatedNameAddrModel update : addrData) {
        updateTable.addCell(createValueCell(update.getAddrType()));
        updateTable.addCell(createValueCell(update.getDataField()));
        updateTable.addCell(createValueCell(update.getOldData()));
        updateTable.addCell(createValueCell(update.getNewData()));
      }
      document.add(updateTable);
    }

  }

  protected Table createDetailsTable() {
    return createDetailsTable(new float[] { 20, 30, 20, 30 });
  }

  protected Table createDetailsTable(float[] percentArray) {
    Table table = new Table(UnitValue.createPercentArray(percentArray));
    table.setWidthPercent(100).setTextAlignment(TextAlignment.LEFT).setHorizontalAlignment(HorizontalAlignment.LEFT);
    return table;
  }

  protected Paragraph createHeader(String text) {
    Paragraph header = new Paragraph(text);
    header.setFont(this.boldFont);
    header.setFontSize(10);
    return header;
  }

  protected Paragraph createSubHeader(String text) {
    Paragraph header = new Paragraph(text);
    header.setFont(this.boldFont);
    header.setFontSize(9);
    return header;
  }

  protected Cell createLabelCell(String text) {
    return createLabelCell(text, 1, 1);
  }

  protected Cell createLabelCell(String text, int rowSpan, int colSpan) {
    Cell cell = new Cell(rowSpan, colSpan);

    cell.setHeight(10);
    Paragraph label = null;
    if (!StringUtils.isBlank(text)) {
      label = new Paragraph(text);
    } else {
      label = new Paragraph();
    }
    label.setFont(this.boldFont);
    label.setFontSize(7);
    cell.add(label);
    return cell;
  }

  protected Cell createValueCell(String text) {
    return createValueCell(text, 1, 1);
  }

  protected Cell createValueCell(String text, int rowSpan, int colSpan) {
    Cell cell = new Cell(rowSpan, colSpan);

    cell.setHeight(10);
    Paragraph label = null;
    if (!StringUtils.isBlank(text)) {
      label = new Paragraph(text);
    } else {
      label = new Paragraph();
    }
    label.setFont(this.regularFont);
    label.setFontSize(7);
    cell.add(label);
    return cell;
  }

  protected Paragraph blankLine() {
    Paragraph line = new Paragraph();
    return line;
  }

}
