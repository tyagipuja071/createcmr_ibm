/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MachinesToInstall;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

/**
 * @author Rangoli Saxena
 * 
 */
public class NORDXPDFConverter extends DefaultPDFConverter {
  private static final Logger LOG = Logger.getLogger(DefaultPDFConverter.class);
  private final PdfFont regularFont;

  public NORDXPDFConverter(String cmrIssuingCntry) throws IOException {
    super(cmrIssuingCntry);
    this.regularFont = PdfFontFactory.createFont(FontConstants.HELVETICA);
  }

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

            }

            if (Arrays.asList("REACTIVATE", "DELETE", "R", "D").contains(admin.getReqType().toUpperCase())) {
              document.add(blankLine());
              addDeleteReactivateDetails(admin, entityManager, document);
            }

          } catch (Exception e) {
            System.out.println(e);
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
    } catch (Exception e) {
      LOG.error("Error in Generating PDF for Request ID " + admin.getId().getReqId(), e);
      return false;
    }
  }

  @Override
  protected void addExtraSections(EntityManager entityManager, Admin admin, Data data, String sysLoc, Addr soldTo, Document document) {
  }

  @Override
  protected void addMainDetails(Admin admin, Data data, Document document) {
    document.add(createSubHeader("General Information"));
    Table main = createDetailsTable();

    main.addCell(createLabelCell("Request ID:"));
    main.addCell(createValueCell(admin.getId().getReqId() + ""));
    main.addCell(createLabelCell("Request Type:"));
    if ("Y".equals(admin.getProspLegalInd())) {
      main.addCell(createValueCell(admin.getReqType() + " (Prospect to Legal Conversion)"));
    } else {
      main.addCell(createValueCell(admin.getReqType() + ""));
    }

    main.addCell(createLabelCell("Requester:"));
    main.addCell(createValueCell(admin.getRequesterNm() + ""));
    main.addCell(createLabelCell("Originator:"));
    main.addCell(createValueCell(admin.getOriginatorNm() + ""));

    main.addCell(createLabelCell("CMR Issuing Country:"));
    main.addCell(createValueCell(data.getCmrIssuingCntry() + ""));
    main.addCell(createLabelCell("CMR No:"));
    main.addCell(createValueCell(data.getCmrNo() + ""));

    main.addCell(createLabelCell("Customer Name:"));
    String customerName = StringUtils.isBlank(admin.getMainCustNm1()) ? "" : admin.getMainCustNm1();
    if (!StringUtils.isBlank(admin.getMainCustNm2())) {
      customerName += " " + admin.getMainCustNm2();
    }
    main.addCell(createValueCell(customerName + "", 1, 3));

    String[] cmrString = data.getCmrIssuingCntry().split("-");
    String cmrCntry = data.getCmrIssuingCntry() + "";
    String[] balkan = { "702", "678" };

    if (cmrString != null && cmrString.length > 0) {
      cmrCntry = cmrString[0].trim();
    }

    if (Arrays.asList(balkan).contains(cmrCntry)) {
      main.addCell(createLabelCell("Country Sub-Region:"));
      switch (data.getCountryUse()) {
      case "702":
        main.addCell(createValueCell("Finland"));
        break;
      case "702LV":
        main.addCell(createValueCell("Latvia"));
        break;
      case "702LT":
        main.addCell(createValueCell("Lithuania"));
        break;
      case "702EE":
        main.addCell(createValueCell("Estonia"));
        break;
      case "678":
        main.addCell(createValueCell("Denmark"));
        break;
      case "678GL":
        main.addCell(createValueCell("Greenland"));
        break;
      case "678FO":
        main.addCell(createValueCell("Faroe Islands"));
        break;
      case "678IS":
        main.addCell(createValueCell("Iceland"));
        break;
      }
    }

    main.addCell(createLabelCell("Requesting LOB:"));
    main.addCell(createValueCell(admin.getRequestingLob()));
    main.addCell(createLabelCell("Request Reason:"));
    main.addCell(createValueCell(admin.getReqReason(), 1, 3));
    document.add(main);

  }

  @Override
  protected Addr addAddressDetails(Admin admin, Data data, EntityManager entityManager, Document document) {
    document.add(createSubHeader("Address Information"));

    String sql = ExternalizedQuery.getSql("PDF.GETADDRESS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("REQ_ID", admin.getId().getReqId());

    List<Addr> addresses = query.getResults(Addr.class);

    Addr soldTo = null;
    for (Addr addr : addresses) {
      Table address = createDetailsTable();

      String[] cmrString = data.getCmrIssuingCntry().split("-");
      String cmrCntry = data.getCmrIssuingCntry();

      if (cmrString != null && cmrString.length > 0) {
        cmrCntry = cmrString[0].trim();
      }

      GEOHandler handler = RequestUtils.getGEOHandler(cmrCntry);

      if (handler.customerNamesOnAddress()) {
        address.addCell(createLabelCell("Customer Name:"));
        address.addCell(createValueCell(addr.getCustNm1(), 1, 3));
        address.addCell(createLabelCell("Customer Name Con't:"));
        address.addCell(createValueCell(addr.getCustNm2(), 1, 3));
        address.addCell(createLabelCell("Att. Person:"));
        address.addCell(createValueCell(addr.getCustNm3(), 1, 3));
      }
      address.addCell(createLabelCell("Address Type:"));
      address.addCell(createValueCell(addr.getId().getAddrType()));
      address.addCell(createLabelCell("Landed Country:"));
      address.addCell(createValueCell(addr.getLandCntry()));

      String lbl = "Address";
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getAddrTxt() + (StringUtils.isBlank(addr.getAddrTxt2()) ? "" : " " + addr.getAddrTxt2()), 1, 3));

      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "PostalCode", "Postal Code");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getPostCd()));

      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "City1", "City");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getCity1()));

      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "City1", "City");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getCity1()));

      address.addCell(createLabelCell("DPL Check Result:"));
      String dplCheck = addr.getDplChkResult();
      String dplCheckText = "";
      if ("P".equals(dplCheck)) {
        dplCheckText = "Passed";
      } else if ("F".equals(dplCheck)) {
        dplCheckText = "FAILED";
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
      String sql1 = ExternalizedQuery.getSql("MACHINES.SEARCH_MACHINES");
      PreparedQuery query1 = new PreparedQuery(entityManager, sql1);
      query1.setParameter("REQ_ID", addr.getId().getReqId());
      query1.setParameter("ADDR_TYPE", "ZI01");
      query1.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());

      List<MachinesToInstall> machine = query1.getResults(MachinesToInstall.class);

      for (MachinesToInstall machine1 : machine) {

        lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "MachineTyp", "Machine Typ");
        address.addCell(createLabelCell(lbl + ":"));
        address.addCell(createValueCell(machine1.getId().getMachineTyp()));

        lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "MachineSerialNo", "Machine Serial No");
        address.addCell(createLabelCell(lbl + ":"));
        address.addCell(createValueCell(machine1.getId().getMachineSerialNo()));
      }

      addConverterAddressDetails(entityManager, address, addr);

      document.add(address);
      document.add(blankLine());

      if (addr.getId().getAddrType().contains("(ZS01)")) {
        soldTo = addr;
      }
    }
    return soldTo;
  }

  @Override
  protected void addConverterCustomerDetails(EntityManager entityManager, Table customer, Data data) {

    customer.addCell(createLabelCell("VAT:"));
    customer.addCell(createValueCell(data.getVat()));
    customer.addCell(createLabelCell("VAT Exempt:"));
    customer.addCell(createValueCell("Y".equals(data.getVatExempt()) ? "Yes" : "No"));

    customer.addCell(createLabelCell("Abbreviated Location:"));
    customer.addCell(createValueCell(data.getAbbrevLocn()));

    customer.addCell(createLabelCell("Sensitive Flag:"));
    customer.addCell(createValueCell(data.getSensitiveFlag()));

    customer.addCell(createLabelCell("Tax Code:"));
    customer.addCell(createValueCell(data.getTaxCd1()));

    customer.addCell(createLabelCell("Collection Code:"));
    customer.addCell(createValueCell(data.getCollectionCd()));

    customer.addCell(createLabelCell("Payment Terms:"));
    customer.addCell(createValueCell(data.getModeOfPayment()));

    customer.addCell(createLabelCell("Embargo Code:"));
    customer.addCell(createValueCell(data.getEmbargoCd()));

  }

  @Override
  protected void addIBMDetails(EntityManager entityManager, Data data, Document document) {
    document.add(createSubHeader("IBM Information"));
    Table ibm = createDetailsTable();

    ibm.addCell(createLabelCell("ISU Code:"));
    ibm.addCell(createValueCell(data.getIsuCd()));

    ibm.addCell(createLabelCell("Client Tier:"));
    ibm.addCell(createValueCell(data.getClientTier()));

    ibm.addCell(createLabelCell("DUNS:"));
    ibm.addCell(createValueCell(data.getDunsNo()));

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

    ibm.addCell(createLabelCell("Coverage:"));
    if (data.getCovId() != null) {
      ibm.addCell(createValueCell(data.getCovId() + (data.getCovDesc() != null ? " - " + data.getCovDesc() : "")));
    } else {
      ibm.addCell(createValueCell(null));
    }

    addConverterIBMDetails(entityManager, ibm, data);
    document.add(ibm);
  }

  @Override
  protected void addConverterIBMDetails(EntityManager entityManager, Table ibm, Data data) {
    ibm.addCell(createLabelCell("INAC Code:"));
    ibm.addCell(createValueCell(data.getInacCd()));

    ibm.addCell(createLabelCell("Sales Rep:"));
    ibm.addCell(createValueCell(data.getRepTeamMemberNo()));

    ibm.addCell(createLabelCell("Selling Branch Office:"));
    ibm.addCell(createValueCell(data.getSalesBusOffCd()));

    ibm.addCell(createLabelCell("A/C Admin DSC:"));
    ibm.addCell(createValueCell(data.getEngineeringBo(), 1, 3));
  }

  public String textContainingLanguage(String text) {

    return "";
  }

  @Override
  protected Cell createValueCell(String text, int rowSpan, int colSpan) {
    Cell cell = new Cell(rowSpan, colSpan);
    if ((text != null && (!text.isEmpty())) && (textContainingLanguage(text) != null)) {
      try {
        ClassLoader classLoader = getClass().getClassLoader();
        String FONT = null;
        PdfFont font = null;
        switch (textContainingLanguage(text)) {
        case "ARABIC":
          FONT = classLoader.getResource("Behdad-Regular.ttf").getPath();
          font = PdfFontFactory.createFont(FONT, PdfEncodings.IDENTITY_H, true);
          break;
        case "RUSSIAN":
          FONT = classLoader.getResource("Lora-Regular.ttf").getPath();
          font = PdfFontFactory.createFont(FONT, PdfEncodings.IDENTITY_H, true);
          break;
        default:
          Paragraph label = new Paragraph();
          label.setFont(this.regularFont);
          label.setFontSize(7);
          label.add(text);
          cell.add(label);
          return cell;
        }

        if (FONT == null || font == null) {
          Paragraph label = new Paragraph();
          label.setFont(this.regularFont);
          label.setFontSize(7);
          cell.add(label);
          return cell;
        } else {
          Paragraph label = new Paragraph();
          label.add(text);
          cell.setFont(font);
          cell.add(label);
          return cell;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
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
    return cell;
  }

}
