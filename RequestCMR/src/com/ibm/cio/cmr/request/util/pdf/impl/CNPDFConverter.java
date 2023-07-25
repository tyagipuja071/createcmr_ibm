/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.GeoContactInfo;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.entity.ProlifChecklist;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.approval.ChecklistItem;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil.ChecklistResponse;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cio.cmr.request.util.pdf.RequestToPDFConverter;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

/**
 * @author Dennis Natad
 * 
 */
public class CNPDFConverter extends DefaultPDFConverter {
  private final PdfFont regularFont;
  private static final Logger LOG = Logger.getLogger(RequestToPDFConverter.class);
  private PdfFont chineseFont;

  public CNPDFConverter(String cmrIssuingCntry) throws IOException {
    super(cmrIssuingCntry);
    this.regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
    String webConfig = ConfigUtil.getConfigDir();
    String fontLocation = webConfig + File.separator + "ARIALUNI.TTF";
    LOG.debug("Chinese Font Location " + fontLocation);
    try {
      this.chineseFont = PdfFontFactory.createFont(fontLocation, PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);
    } catch (IOException e) {
      LOG.debug("Error in initializing Chinese font.", e);
      this.chineseFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
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
          if (items.indexOf(item) == 12 || items.indexOf(item) == 13) {
            padding = textLength / 70;
            padding += 1;
          }
          if (items.indexOf(item) == 11) {
            padding -= 2;
          }
          String text = item.getLabel() != null ? item.getLabel() : "";
          if (text.indexOf("\n") > 0) {
            padding += 1;
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

  public String textContainingLanguage(String text) {
    for (char charac : text.toCharArray()) {
      if (Character.UnicodeBlock.of(charac) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
        return "CHINESE";
      }
    }
    return null;
  }

  @Override
  protected Cell createValueCell(String text, int rowSpan, int colSpan) {
    Cell cell = new Cell(rowSpan, colSpan);
    if ((text != null && (!text.isEmpty())) && (textContainingLanguage(text) != null)) {
      if (textContainingLanguage(text).equalsIgnoreCase("CHINESE")) {
        cell.setFont(this.chineseFont);
        Paragraph label = new Paragraph();
        label.setFontSize(7);
        label.add(text);
        cell.setFont(this.chineseFont);
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

  @Override
  protected void addConverterAddressDetails(EntityManager entityManager, Table address, Addr addr) {
    // super.addConverterAddressDetails(entityManager, address, addr);
    CNHandler cnHandler = new CNHandler();
    try {
      IntlAddr iAddr = cnHandler.getIntlAddrById(addr, entityManager);
      GeoContactInfo geoContactInfo = cnHandler.getGeoContactInfoById(addr, entityManager);

      address.addCell(createLabelCell("Phone #"));
      address.addCell(createValueCell(addr.getCustPhone()));

      address.addCell(createLabelCell("Customer Name Chinese"));
      // LOG.debug(">>> " + iAddr.getIntlCustNm1());
      address.addCell(createValueCell((iAddr != null ? iAddr.getIntlCustNm1() : ""), 1, 3));

      address.addCell(createLabelCell("Cust. Name Con't Chinese"));
      address.addCell(createValueCell((iAddr != null ? iAddr.getIntlCustNm2() : ""), 1, 3));

      address.addCell(createLabelCell("Street Address Chinese"));
      address.addCell(createValueCell((iAddr != null ? iAddr.getAddrTxt() : ""), 1, 3));

      address.addCell(createLabelCell("St. Address Con't Chinese"));
      address.addCell(createValueCell((iAddr != null ? iAddr.getIntlCustNm4() : ""), 1, 3));

      address.addCell(createLabelCell("City Chinese"));
      address.addCell(createValueCell((iAddr != null ? iAddr.getCity1() : ""), 1, 3));

      address.addCell(createLabelCell("District Chinese"));
      address.addCell(createValueCell((iAddr != null ? iAddr.getCity2() : ""), 1, 3));

      if (geoContactInfo != null && geoContactInfo.getId().getReqId() != 0) {
        // address.addCell(createLabelCell("Phone #"));
        // address.addCell(createValueCell(geoContactInfo.getContactPhone(), 1,
        // 3));

        address.addCell(createLabelCell("Customer Contact's Job Title"));
        address.addCell(createValueCell(geoContactInfo.getContactFunc(), 1, 3));

        address.addCell(createLabelCell("Customer Contact's Name (include salutation)"));
        address.addCell(createValueCell(geoContactInfo.getContactName(), 1, 3));
      }

    } catch (Exception e) {
      LOG.debug(">>> AN ERROR OCCURED IN GENERATING THE PDF WITH CHINESE CHARACTERS!", e);
    }

  }

  @Override
  protected void addConverterCustomerDetails(EntityManager entityManager, Table customer, Data data) {

    // customer.addCell(createLabelCell("Abbreviated Location:"));
    // customer.addCell(createValueCell(data.getAbbrevLocn()));

  }

  @Override
  protected void addConverterIBMDetails(EntityManager entityManager, Table ibm, Data data) {

    // ibm.addCell(createLabelCell("Province Name:"));
    // ibm.addCell(createValueCell(data.getBusnType()));
    // ibm.addCell(createLabelCell("Province Code/BOID:"));
    // ibm.addCell(createValueCell(data.getTerritoryCd()));
    //
    // ibm.addCell(createLabelCell("Government Indicator:"));
    // ibm.addCell(createValueCell(data.getGovType()));
    // ibm.addCell(createLabelCell("ISBU:"));
    // ibm.addCell(createValueCell(data.getIsbuCd()));
    //
    // ibm.addCell(createLabelCell("Sector:"));
    // ibm.addCell(createValueCell(data.getSectorCd()));
    // ibm.addCell(createLabelCell("Market Responsibility Code (MRC):"));
    // ibm.addCell(createValueCell(data.getMrcCd()));
    //
    // ibm.addCell(createLabelCell("Region Code:"));
    // ibm.addCell(createValueCell(data.getMiscBillCd()));
    // ibm.addCell(createLabelCell("Collection Code:"));
    // ibm.addCell(createValueCell(data.getCollectionCd()));
    //
    // ibm.addCell(createLabelCell("Sales Rep No:"));
    // ibm.addCell(createValueCell(data.getRepTeamMemberNo()));
    // ibm.addCell(createLabelCell("Customer Service Code:"));
    // ibm.addCell(createValueCell(data.getEngineeringBo(), 1, 3));
    //
    // ibm.addCell(createLabelCell("Customer Billing Contact Name:"));
    // ibm.addCell(createValueCell(data.getContactName1()));
    //
    // ibm.addCell(createValueCell(null));
    // ibm.addCell(createValueCell(null));

  }

  private Cell createValueCellExtended(String text, int rowSpan, int colSpan, int padding) {
    Cell cell = new Cell(rowSpan, colSpan);
    if ((text != null && (!text.isEmpty())) && (textContainingLanguage(text) != null)) {
      if (textContainingLanguage(text).equalsIgnoreCase("CHINESE")) {
        cell.setFont(this.chineseFont);
        Paragraph label = new Paragraph();
        label.setFontSize(7);
        if (text.contains("C1")) {
          text = text.replace("C1", "C1.1");
        } else if (text.contains("C2")) {
          text = text.replace("C2", "C1.2");
        } else if (text.contains("C3")) {
          text = text.replace("C3", "C1.3");
        } else if (text.contains("C4")) {
          text = text.replace("C4", "C2.1");
        } else if (text.contains("C5")) {
          text = text.replace("C5", "C2.2");
        }
        label.add(text);
        cell.setFont(this.chineseFont);
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
