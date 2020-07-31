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
import com.ibm.cio.cmr.request.entity.ProlifChecklist;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.approval.ChecklistItem;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil.ChecklistResponse;
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
 * @author Eduard Bernardo
 * 
 */
public class CEMEAPDFConverter extends DefaultPDFConverter {
  private static final Logger LOG = Logger.getLogger(DefaultPDFConverter.class);
  private PdfFont regularFont;

  public CEMEAPDFConverter(String cmrIssuingCntry) throws IOException {
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
      checklistMain.addCell(createValueCell(admin.getMainCustNm1()));
      if (!StringUtils.isEmpty(checklist.getLocalCustNm())) {
        checklistMain.addCell(createLabelCell("Local Customer Name:"));
        checklistMain.addCell(createValueCell(checklist.getLocalCustNm()));
      }

      if (soldTo != null) {
        checklistMain.addCell(createLabelCell("Address:"));
        String address = soldTo.getAddrTxt() != null ? soldTo.getAddrTxt() : "";
        checklistMain.addCell(createValueCell(address + (!StringUtils.isEmpty(soldTo.getAddrTxt2()) ? " " + soldTo.getAddrTxt2() : "")));
      }

      document.add(checklistMain);
      document.add(blankLine());

      Table checklistSection = createDetailsTable(new float[] { 91, 9 });

      try {
        List<ChecklistItem> items = ChecklistUtil.getItems(checklist, "CEMEA", ChecklistResponse.Responded);
        String answer = null;
        Cell answerCell = null;
        checklistSection.addCell(createLabelCell("Questionnaire", 1, 2));
        checklistSection.addCell(createLabelCell("Item"));
        checklistSection.addCell(createLabelCell("Response"));
        for (ChecklistItem item : items) {
          checklistSection.addCell(createValueCell(item.getLabel()));
          answer = "Y".equals(item.getAnswer()) ? "Yes" : "No";
          answerCell = createValueCell(answer);
          if ("Y".equals(item.getAnswer())) {
            answerCell.setFontColor(Color.RED);
          }
          checklistSection.addCell(answerCell);

          // Re-export countries
          if (items.indexOf(item) == 7 && "Y".equals(item.getAnswer())) {
            checklistSection.addCell(createValueCell("Re-export countries: " + checklist.getFreeTxtField1(), 1, 2));
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      document.add(checklistSection);

      Table checklistOthers = createDetailsTable(new float[] { 25, 75 });
      document.add(blankLine());

      if (!StringUtils.isEmpty(checklist.getFreeTxtField2()) || !StringUtils.isEmpty(checklist.getFreeTxtField3())) {
        checklistOthers.addCell(createValueCell(
            "End Users who are identified by either our Business Partners or customers are also subject to the above screening requirements.", 1, 2));
      }
      if (!StringUtils.isEmpty(checklist.getFreeTxtField2())) {
        checklistOthers.addCell(createValueCell("Name, Customer number:"));
        checklistOthers.addCell(createValueCell(checklist.getFreeTxtField2()));
      }
      if (!StringUtils.isEmpty(checklist.getFreeTxtField3())) {
        checklistOthers.addCell(createValueCell("Name, Customer number:"));
        checklistOthers.addCell(createValueCell(checklist.getFreeTxtField3()));
      }

      document.add(checklistOthers);
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

    String[] cmrString = data.getCmrIssuingCntry().split("-");
    String cmrCntry = data.getCmrIssuingCntry();
    String[] balkan = { "707", "762", "808" };

    if (cmrString != null && cmrString.length > 0) {
      cmrCntry = cmrString[0].trim();
    }

    if (Arrays.asList(balkan).contains(cmrCntry)) {
      main.addCell(createLabelCell("Country Sub-Region:"));
      switch (data.getCountryUse()) {
      case "707":
        main.addCell(createValueCell("Serbia"));
        break;
      case "707CS":
        main.addCell(createValueCell("Kosovo"));
        break;
      case "707ME":
        main.addCell(createValueCell("Montenegro"));
        break;
      }
    }

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
        address.addCell(createLabelCell("Name 2:"));
        address.addCell(createValueCell(addr.getCustNm2(), 1, 3));
        address.addCell(createLabelCell("Name 3:"));
        address.addCell(createValueCell(addr.getCustNm3(), 1, 3));
      }
      address.addCell(createLabelCell("Address Type:"));
      address.addCell(createValueCell(addr.getId().getAddrType()));
      address.addCell(createLabelCell("Landed Country:"));
      address.addCell(createValueCell(addr.getLandCntry()));

      String lbl = "Address";
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getAddrTxt() + (StringUtils.isBlank(addr.getAddrTxt2()) ? "" : " " + addr.getAddrTxt2()), 1, 3));

      if (cmrCntry.equals("618")) {
        lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "StateProv", "State/Province");
        address.addCell(createLabelCell(lbl + ":"));
        address.addCell(createValueCell(addr.getStateProv()));

        address.addCell(createLabelCell("Attention Person:"));
        address.addCell(createValueCell(addr.getCustNm4()));

        lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "CustPhone", "Phone number");
        address.addCell(createLabelCell(lbl + ":"));
        address.addCell(createValueCell(addr.getCustPhone()));
      }

      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "PostalCode", "Postal Code");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getPostCd()));

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
    String[] cmrString = data.getCmrIssuingCntry().split("-");
    String cmrCntry = data.getCmrIssuingCntry();
    String[] ME = { "620", "767", "805", "823", "677", "680", "832" };

    if (cmrString != null && cmrString.length > 0) {
      cmrCntry = cmrString[0].trim();
    }
    customer.addCell(createLabelCell("VAT:"));
    customer.addCell(createValueCell(data.getVat()));
    customer.addCell(createLabelCell("VAT Exempt:"));
    customer.addCell(createValueCell("Y".equals(data.getVatExempt()) ? "Yes" : "No"));

    customer.addCell(createLabelCell("Abbreviated Location:"));
    customer.addCell(createValueCell(data.getAbbrevLocn()));

    if (Arrays.asList(ME).contains(cmrCntry)) {
      if (data.getBpSalesRepNo() != null && (!(data.getBpSalesRepNo().isEmpty()))) {
        customer.addCell(createLabelCell("Tele-coverage rep:"));
        customer.addCell(createValueCell(data.getBpSalesRepNo()));
      }
    }

    if (cmrCntry.equals("693")) {
      customer.addCell(createLabelCell("ICO:"));
      customer.addCell(createValueCell(data.getCompany()));
      customer.addCell(createLabelCell("DIC:"));
      customer.addCell(createValueCell(data.getTaxCd1()));
    }

    if (cmrCntry.equals("704")) {
      customer.addCell(createLabelCell("OIB:"));
      customer.addCell(createValueCell(data.getTaxCd1()));
    }

    if (!(cmrCntry.equals("618"))) {
      customer.addCell(createLabelCell("CoF (Commercial Financed):"));
      customer.addCell(createValueCell(data.getCommercialFinanced()));

      customer.addCell(createLabelCell("Phone #:"));
      customer.addCell(createValueCell(data.getPhone1()));
    }

    customer.addCell(createLabelCell("Embargo Code:"));
    customer.addCell(createValueCell(data.getEmbargoCd()));
  }

  @Override
  protected void addIBMDetails(EntityManager entityManager, Data data, Document document) {
    document.add(createSubHeader("IBM Information"));
    Table ibm = createDetailsTable();

    ibm.addCell(createLabelCell("Enterprise No.:"));
    ibm.addCell(createValueCell(data.getEnterprise()));

    ibm.addCell(createLabelCell("Search Term (SORTL):"));
    ibm.addCell(createValueCell(data.getSearchTerm()));
    ibm.addCell(createLabelCell("ISU Code:"));
    ibm.addCell(createValueCell(data.getIsuCd()));

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

  @Override
  protected void addConverterIBMDetails(EntityManager entityManager, Table ibm, Data data) {
    String[] cmrString = data.getCmrIssuingCntry().split("-");
    String cmrCntry = data.getCmrIssuingCntry();

    if (cmrString != null && cmrString.length > 0) {
      cmrCntry = cmrString[0].trim();
    }

    if (!(cmrCntry.equals("618"))) {
      ibm.addCell(createLabelCell("Collection Code:"));
      ibm.addCell(createValueCell(data.getCollectionCd()));
    }

    ibm.addCell(createLabelCell("Sales Rep:"));
    ibm.addCell(createValueCell(data.getRepTeamMemberNo()));

    ibm.addCell(createLabelCell("Selling Branch Office:"));
    ibm.addCell(createValueCell(data.getSalesBusOffCd()));

    if (cmrCntry.equals("821") && "Y".equals(data.getCisServiceCustIndc())) {
      ibm.addCell(createLabelCell("ISU of Duplicate CMR:"));
      ibm.addCell(createValueCell(data.getDupIsuCd()));

      ibm.addCell(createLabelCell("Client Tier of Duplicate CMR:"));
      ibm.addCell(createValueCell(data.getDupClientTierCd()));

      ibm.addCell(createLabelCell("Company of Duplicate CMR:"));
      ibm.addCell(createValueCell(data.getDupEnterpriseNo()));

      // ibm.addCell(createLabelCell("Sales Rep of Duplicate CMR:"));
      // ibm.addCell(createValueCell(data.getDupSalesRepNo()));

      ibm.addCell(createLabelCell("Enterprise of Duplicate CMR:"));
      ibm.addCell(createValueCell(data.getTaxCd3()));

      ibm.addCell(createLabelCell("SBO of Duplicate CMR:"));
      ibm.addCell(createValueCell(data.getSalesBusOffCd()));
    }

    ibm.addCell(createLabelCell("Engineering BO:"));
    ibm.addCell(createValueCell(data.getEngineeringBo(), 1, 3));

    ibm.addCell(createLabelCell("PPS CEID:"));
    ibm.addCell(createValueCell(data.getPpsceid()));

    ibm.addCell(createLabelCell("Membership Level:"));
    ibm.addCell(createValueCell(data.getMemLvl()));

    ibm.addCell(createLabelCell("BP Relation Type:"));
    ibm.addCell(createValueCell(data.getBpRelType()));

    ibm.addCell(createLabelCell("CMR Number:"));
    ibm.addCell(createValueCell(data.getCmrNo()));

    if (cmrCntry.equals("618")) {
      if (data.getCreditCd() != null && (!(data.getCreditCd().isEmpty()))) {
        ibm.addCell(createLabelCell("Credit Code:"));
        ibm.addCell(createValueCell(data.getCreditCd()));
      }

      ibm.addCell(createLabelCell("Customer Location Number:"));
      ibm.addCell(createValueCell(data.getLocationNumber()));

      ibm.addCell(createLabelCell("Currency Code:"));
      ibm.addCell(createValueCell(data.getLegacyCurrencyCd()));
    }
  }

  public String textContainingLanguage(String text) {
    for (char charac : text.toCharArray()) {
      if (Character.UnicodeBlock.of(charac) == Character.UnicodeBlock.ARABIC) {
        return "ARABIC";
      }
      if (Character.UnicodeBlock.of(charac) == Character.UnicodeBlock.CYRILLIC) {
        return "RUSSIAN";
      }
    }
    return null;
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
