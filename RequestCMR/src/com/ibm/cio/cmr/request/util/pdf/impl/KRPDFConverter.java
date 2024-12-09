/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.ProlifChecklist;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.approval.ChecklistItem;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil.ChecklistResponse;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
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
public class KRPDFConverter extends DefaultPDFConverter {
  private static final Logger LOG = Logger.getLogger(DefaultPDFConverter.class);
  private PdfFont regularFont;
  private PdfFont koreaFont;

  public KRPDFConverter(String cmrIssuingCntry) throws IOException {
    super(cmrIssuingCntry);
    this.regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
    String webConfig = ConfigUtil.getConfigDir();
    String fontLocation = webConfig + File.separator + "ARIALUNI.TTF";
    LOG.debug("Korea Font Location " + fontLocation);
    try {
      this.koreaFont = PdfFontFactory.createFont(fontLocation, PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);
    } catch (IOException e) {
      LOG.debug("Error in initializing Japanese font.", e);
      this.koreaFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
    }
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

          } finally {
            document.close();
          }
        } finally {
          if (!pdf.isClosed())
            pdf.close();
        }
      } finally {
        if (!writer.isCloseStream())
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

    String sql = ExternalizedQuery.getSql("APPROVAL.GET_CHECKLIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    query.setForReadOnly(true);
    ProlifChecklist checklist = query.getSingleResult(ProlifChecklist.class);
    if (checklist != null) {
      document.add(blankLine());
      document.add(createSubHeader("Proliferation Checklist"));

      Table checklistMain = createDetailsTable(new float[] { 25, 75 });

      checklistMain.addCell(createLabelCell("Customer Name:"));
      checklistMain
          .addCell(createValueCell(admin.getMainCustNm1() + (!StringUtils.isEmpty(admin.getMainCustNm2()) ? " " + admin.getMainCustNm2() : "")));
      if (!StringUtils.isEmpty(checklist.getLocalCustNm())) {
        checklistMain.addCell(createLabelCell("Local Customer Name:"));
        checklistMain.addCell(createValueCell(checklist.getLocalCustNm()));
      }

      if (soldTo != null) {
        checklistMain.addCell(createLabelCell("Address:"));
        String address = soldTo.getAddrTxt() != null ? soldTo.getAddrTxt() : "";
        checklistMain.addCell(createValueCell(address + (!StringUtils.isEmpty(soldTo.getAddrTxt2()) ? " " + soldTo.getAddrTxt2() : "")));
      }
      if (!StringUtils.isEmpty(checklist.getLocalAddr())) {
        checklistMain.addCell(createLabelCell("Local Address:"));
        checklistMain.addCell(createValueCell(checklist.getLocalAddr()));
      }
      if (!StringUtils.isEmpty(checklist.getFreeTxtField1())) {
        checklistMain.addCell(createLabelCell("Nature of Business:"));
        checklistMain.addCell(createValueCell(checklist.getFreeTxtField1()));
      }
      if (!StringUtils.isEmpty(checklist.getFreeTxtField2())) {
        checklistMain.addCell(createLabelCell("Products/Service etc. they want to buy?"));
        checklistMain.addCell(createValueCell(checklist.getFreeTxtField2()));
      }
      if (!StringUtils.isEmpty(checklist.getFreeTxtField3())) {
        checklistMain.addCell(createLabelCell("Purpose of End Use"));
        checklistMain.addCell(createValueCell(checklist.getFreeTxtField3()));
      }

      document.add(checklistMain);
      document.add(blankLine());

      Table checklistSection = createDetailsTable(new float[] { 91, 9 });

      try {
        List<ChecklistItem> items = ChecklistUtil.getItems(checklist, sysLoc, ChecklistResponse.Responded);
        String answer = null;
        Cell answerCell = null;
        checklistSection.addCell(createLabelCell("Questionnaire", 1, 2));
        checklistSection.addCell(createLabelCell("Item"));
        checklistSection.addCell(createLabelCell("Response"));
        for (ChecklistItem item : items) {
          int padding = 1;
          int textLength = item.getLabel().length();
          if (textLength > 150) {
            padding = textLength / 80;
          }
          if (items.indexOf(item) == 9) {
            padding = textLength / 70;
          }
          checklistSection.addCell(createValueCellExtended(item.getLabel(), 1, 1, padding));
          answer = "Y".equals(item.getAnswer()) ? "Yes" : "No";
          answerCell = createValueCell(answer);
          if ("Y".equals(item.getAnswer())) {
            answerCell.setFontColor(ColorConstants.RED);
          }
          checklistSection.addCell(answerCell);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      document.add(checklistSection);
    }
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
        address.addCell(createLabelCell("Customer Name_Korean:"));
        address.addCell(createValueCell(addr.getCustNm3(), 1, 3));
        address.addCell(createLabelCell("Cust. Name_Korean Con't:"));
        address.addCell(createValueCell(addr.getBillingPstlAddr(), 1, 3));
      }
      address.addCell(createLabelCell("Address Type:"));
      address.addCell(createValueCell(addr.getId().getAddrType()));

      address.addCell(createLabelCell("Address Seq:"));
      address.addCell(createValueCell(addr.getId().getAddrSeq()));

      address.addCell(createLabelCell("Landed Country:"));
      address.addCell(createValueCell(addr.getLandCntry()));

      address.addCell(createLabelCell("State/Province:"));
      address.addCell(createValueCell(addr.getStateProv()));

      address.addCell(createLabelCell("Street Address:"));
      address.addCell(createValueCell(addr.getAddrTxt()));

      address.addCell(createLabelCell("Address Con't:"));
      address.addCell(createValueCell(addr.getAddrTxt2()));

      address.addCell(createLabelCell("Street address_Korean:"));
      address.addCell(createValueCell(addr.getCustNm4()));

      address.addCell(createLabelCell("Street address_Korean Con't:"));
      address.addCell(createValueCell(addr.getDivn()));

      address.addCell(createLabelCell("City:"));
      address.addCell(createValueCell(addr.getCity1()));

      address.addCell(createLabelCell("City Korean:"));
      address.addCell(createValueCell(addr.getCity2()));

      address.addCell(createLabelCell("Postal Code:"));
      address.addCell(createValueCell(addr.getPostCd()));

      address.addCell(createLabelCell(""));
      address.addCell(createValueCell(""));

      address.addCell(createLabelCell("Name of person on Invoice_1:"));
      address.addCell(createValueCell(addr.getContact()));

      address.addCell(createLabelCell("Department Name_1:"));
      address.addCell(createValueCell(addr.getDept()));

      address.addCell(createLabelCell("E-mail_1:"));
      address.addCell(createValueCell(addr.getFloor()));

      address.addCell(createLabelCell("Telephone No_1:"));
      address.addCell(createValueCell(addr.getPoBox()));

      address.addCell(createLabelCell("Name of person on Invoice_2:"));
      address.addCell(createValueCell(addr.getCountyName()));

      address.addCell(createLabelCell("Department Name_2:"));
      address.addCell(createValueCell(addr.getPoBoxCity()));

      address.addCell(createLabelCell("E-mail_2:"));
      address.addCell(createValueCell(addr.getOffice()));

      address.addCell(createLabelCell("Telephone No_2:"));
      address.addCell(createValueCell(addr.getTaxOffice()));

      address.addCell(createLabelCell("E-mail_3:"));
      address.addCell(createValueCell(addr.getBldg()));

      address.addCell(createLabelCell("Company phone#:"));
      address.addCell(createValueCell(addr.getCustPhone()));

      address.addCell(createLabelCell("Transport Zone:"));
      address.addCell(createValueCell(addr.getTransportZone()));

      address.addCell(createLabelCell("SAP Number (KUNNR):"));
      address.addCell(createValueCell(addr.getSapNo()));

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
        dplCell.setFontColor(ColorConstants.RED);
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

      if (addr.getId().getAddrType().contains("(ZS01)")) {
        soldTo = addr;
      }
    }
    return soldTo;
  }

  @Override
  protected void addConverterCustomerDetails(EntityManager entityManager, Table customer, Data data) {

    customer.addCell(createLabelCell("Preferred Language:"));
    customer.addCell(createValueCell(data.getCustPrefLang()));

    customer.addCell(createLabelCell("Business registration No.:"));
    customer.addCell(createValueCell(data.getVat()));

    customer.addCell(createLabelCell("Abbreviated Location:"));
    customer.addCell(createValueCell(data.getAbbrevLocn()));

    customer.addCell(createLabelCell("Representative(CEO) name:"));
    customer.addCell(createValueCell(data.getContactName1()));

    customer.addCell(createLabelCell("Representative(CEO) Phone No.:"));
    customer.addCell(createValueCell(data.getRestrictTo()));

    customer.addCell(createLabelCell("Tax Invoice Type:"));
    customer.addCell(createValueCell(data.getInstallRep()));

    customer.addCell(createLabelCell("Business License Type:"));
    customer.addCell(createValueCell(data.getPhone1()));

  }

  @Override
  protected void addIBMDetails(EntityManager entityManager, Data data, Document document) {
    document.add(createSubHeader("IBM Information"));

    Table ibm = createDetailsTable();

    ibm.addCell(createLabelCell("CMR Owner:"));
    ibm.addCell(createValueCell(data.getCmrOwner()));

    ibm.addCell(createLabelCell("CAP:"));
    ibm.addCell(createValueCell("Y".equals(data.getCapInd()) ? "Yes" : "No"));

    ibm.addCell(createLabelCell("iERP Site Party ID:"));
    ibm.addCell(createValueCell(data.getSitePartyId()));

    ibm.addCell(createLabelCell("ISU Code:"));
    ibm.addCell(createValueCell(data.getIsuCd()));

    ibm.addCell(createLabelCell("GB segment:"));
    ibm.addCell(createValueCell(data.getClientTier()));

    ibm.addCell(createLabelCell("INAC Type:"));
    ibm.addCell(createValueCell(data.getInacType()));

    ibm.addCell(createLabelCell("INAC/NAC Code:"));
    ibm.addCell(createValueCell(data.getInacCd()));

    ibm.addCell(createLabelCell("Cluster ID:"));
    ibm.addCell(createValueCell(data.getSearchTerm()));

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

    ibm.addCell(createLabelCell("BG LDE Rule:"));
    if (!StringUtils.isEmpty(data.getBgRuleId())) {
      ibm.addCell(createValueCell(data.getBgRuleId()));
    } else {
      ibm.addCell(createValueCell(null));
    }

    ibm.addCell(createLabelCell("Coverage Type/ID:"));
    if (data.getCovId() != null) {
      ibm.addCell(createValueCell(data.getCovId() + (data.getCovDesc() != null ? " - " + data.getCovDesc() : "")));
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

  @Override
  protected void addConverterIBMDetails(EntityManager entityManager, Table ibm, Data data) {

    ibm.addCell(createLabelCell("CMR Number Prefix:"));
    ibm.addCell(createValueCell(data.getCmrNoPrefix()));

    ibm.addCell(createLabelCell("Sales Rep No:"));
    ibm.addCell(createValueCell(data.getRepTeamMemberNo()));

    ibm.addCell(createLabelCell("Parent Company No:"));
    ibm.addCell(createValueCell(data.getDealerNo()));

    ibm.addCell(createLabelCell("Market Responsibility Code:"));
    ibm.addCell(createValueCell(data.getMrcCd()));

    ibm.addCell(createLabelCell("Contract Type:"));
    ibm.addCell(createValueCell(data.getOrgNo()));

    ibm.addCell(createLabelCell("ROL Code:"));
    ibm.addCell(createValueCell(data.getCommercialFinanced()));

    ibm.addCell(createLabelCell("Business Type:"));
    ibm.addCell(createValueCell(data.getContactName2()));

    ibm.addCell(createLabelCell("MCI Code:"));
    ibm.addCell(createValueCell(data.getCreditCd()));

    ibm.addCell(createLabelCell("Product Type:"));
    ibm.addCell(createValueCell(data.getContactName3()));

    ibm.addCell(createLabelCell(""));
    ibm.addCell(createValueCell(""));

  }

  public String textContainingLanguage(String text) {
    for (char charac : text.toCharArray()) {
      if (Character.UnicodeBlock.of(charac) == Character.UnicodeBlock.HANGUL_SYLLABLES) {
        return "KOREA";
      } else if (Character.UnicodeBlock.of(charac) == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
        return "KOREA";
      }
    }
    return null;
  }

  @Override
  protected Cell createValueCell(String text, int rowSpan, int colSpan) {
    Cell cell = new Cell(rowSpan, colSpan);
    if ((text != null && (!text.isEmpty())) && (textContainingLanguage(text) != null)) {
      if (textContainingLanguage(text).equalsIgnoreCase("KOREA")) {
        cell.setFont(this.koreaFont);

        Paragraph label = new Paragraph();
        label.setFontSize(7);
        label.add(text);
        cell.setFont(this.koreaFont);
        cell.add(label);
        return cell;
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

  private Cell createValueCellExtended(String text, int rowSpan, int colSpan, int padding) {
    Cell cell = new Cell(rowSpan, colSpan);
    if ((text != null && (!text.isEmpty())) && (textContainingLanguage(text) != null)) {
      if (textContainingLanguage(text).equalsIgnoreCase("KOREA")) {
        cell.setFont(this.koreaFont);
        Paragraph label = new Paragraph();
        label.setFontSize(7);
        label.add(text);
        cell.setFont(this.koreaFont);
        cell.add(label);
        return cell;
      }
    } else {
      cell.setHeight(10 * padding);
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