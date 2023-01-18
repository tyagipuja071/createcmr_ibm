/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;
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
import com.ibm.cio.cmr.request.util.approval.ChecklistItem;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil;
import com.ibm.cio.cmr.request.util.approval.ChecklistUtil.ChecklistResponse;
import com.ibm.cio.cmr.request.util.pdf.RequestToPDFConverter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

/**
 * @author Jeffrey Zamora
 * 
 */
public class APPDFConverter extends DefaultPDFConverter {
  private PdfFont regularFont;
  private Logger LOG = Logger.getLogger(RequestToPDFConverter.class);
  private PdfFont chineseFont;

  public APPDFConverter(String cmrIssuingCntry) throws IOException {
    super(cmrIssuingCntry);
    this.regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

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
          if ("736".equals(sysLoc) || "738".equals(sysLoc)) {
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
          } else {
            checklistSection.addCell(createValueCell(item.getLabel()));
          }
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
  protected void addConverterCustomerDetails(EntityManager entityManager, Table customer, Data data) {

    customer.addCell(createLabelCell("Abbreviated Location:"));
    customer.addCell(createValueCell(data.getAbbrevLocn()));

  }

  @Override
  protected void addConverterIBMDetails(EntityManager entityManager, Table ibm, Data data) {

    ibm.addCell(createLabelCell("Province Name:"));
    ibm.addCell(createValueCell(data.getBusnType()));
    ibm.addCell(createLabelCell("Province Code/BOID:"));
    ibm.addCell(createValueCell(data.getTerritoryCd()));

    ibm.addCell(createLabelCell("Government Indicator:"));
    ibm.addCell(createValueCell(data.getGovType()));
    ibm.addCell(createLabelCell("ISBU:"));
    ibm.addCell(createValueCell(data.getIsbuCd()));

    ibm.addCell(createLabelCell("Sector:"));
    ibm.addCell(createValueCell(data.getSectorCd()));
    ibm.addCell(createLabelCell("Market Responsibility Code (MRC):"));
    ibm.addCell(createValueCell(data.getMrcCd()));

    ibm.addCell(createLabelCell("Region Code:"));
    ibm.addCell(createValueCell(data.getMiscBillCd()));
    ibm.addCell(createLabelCell("Collection Code:"));
    ibm.addCell(createValueCell(data.getCollectionCd()));

    ibm.addCell(createLabelCell("Sales Rep No:"));
    ibm.addCell(createValueCell(data.getRepTeamMemberNo()));
    ibm.addCell(createLabelCell("Customer Service Code:"));
    ibm.addCell(createValueCell(data.getEngineeringBo(), 1, 3));

    ibm.addCell(createLabelCell("Customer Billing Contact Name:"));
    ibm.addCell(createValueCell(data.getContactName1()));

    ibm.addCell(createValueCell(null));
    ibm.addCell(createValueCell(null));

  }

  private Cell createValueCellExtended(String text, int rowSpan, int colSpan, int padding) {
    Cell cell = new Cell(rowSpan, colSpan);
    if (text != null && (!text.isEmpty())) {
      cell.setHeight(10 * padding);
      Paragraph label = null;
      LOG.warn("text === " + text);
      if (!StringUtils.isBlank(text)) {
        label = new Paragraph(text);
      } else {
        label = new Paragraph();
      }
      label.setFont(this.regularFont);
      label.setFontSize(7);
      cell.add(label);
    }
    return cell;
  }

}
