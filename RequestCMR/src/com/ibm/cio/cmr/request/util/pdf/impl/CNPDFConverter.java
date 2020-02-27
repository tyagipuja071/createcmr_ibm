/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.GeoContactInfo;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.entity.ProlifChecklist;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.approval.ChecklistItem;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil.ChecklistResponse;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cio.cmr.request.util.pdf.RequestToPDFConverter;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
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

  public CNPDFConverter(String cmrIssuingCntry) throws IOException {
    super(cmrIssuingCntry);
    this.regularFont = PdfFontFactory.createFont(FontConstants.HELVETICA);

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
      checklistMain.addCell(createValueCell(admin.getMainCustNm1()
          + (!StringUtils.isEmpty(admin.getMainCustNm2()) ? " " + admin.getMainCustNm2() : "")));
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
          checklistSection.addCell(createValueCell(item.getLabel()));
          answer = "Y".equals(item.getAnswer()) ? "Yes" : "No";
          answerCell = createValueCell(answer);
          if ("Y".equals(item.getAnswer())) {
            answerCell.setFontColor(Color.RED);
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
    ClassLoader classLoader = getClass().getClassLoader();
    String FONT = null;
    PdfFont font = null;
    if ((text != null && (!text.isEmpty())) && (textContainingLanguage(text) != null)) {
      try {
        if (textContainingLanguage(text).equalsIgnoreCase("CHINESE")) {
          LOG.debug(">> classLoader.getResource('ARIALUNI.TTF') >> " + classLoader.getResource("ARIALUNI.TTF"));
          FONT = classLoader.getResource("ARIALUNI.TTF").getPath();
          font = PdfFontFactory.createFont(FONT, PdfEncodings.IDENTITY_H, true);
          cell.setFont(font);
          Paragraph label = new Paragraph();
          label.setFontSize(7);
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
}
