/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.dpl.DPLSearchItem;
import com.ibm.cio.cmr.request.automation.dpl.DPLSearchResult;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;

/**
 * Converts a list of {@link DPLSearchResult} objects into PDF details
 * 
 * @author JeffZAMORA
 * 
 */
public class DPLSearchPDFConverter extends DefaultPDFConverter {

  private static final Logger LOG = Logger.getLogger(DnBPDFConverter.class);

  private List<DPLSearchResult> dplResults;
  private String user;
  private long searchTs;
  private String companyName;

  /**
   * @throws IOException
   * 
   */
  public DPLSearchPDFConverter(String user, long searchTs, String companyName, List<DPLSearchResult> dplResults) throws IOException {
    super(null);
    this.user = user;
    this.searchTs = searchTs;
    this.dplResults = dplResults;
    this.companyName = companyName;
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

            document.add(createHeader("Denied Parties List Search"));
            Table section = createDetailsTable(new float[] { 25, 75 });
            section.addCell(createLabelCell("Search Performed By:"));
            section.addCell(createValueCell(this.user));
            section.addCell(createLabelCell("Date of Search:"));
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            section.addCell(createValueCell(formatter.format(new Date(this.searchTs))));
            section.addCell(createLabelCell("DPL Search URL:"));
            section.addCell(createValueCell(SystemParameters.getString("DPL_CHECK_URL")));
            section.addCell(createLabelCell("Company Name:"));
            section.addCell(createValueCell(this.companyName.toUpperCase()));
            document.add(section);
            document.add(blankLine());

            for (DPLSearchResult result : this.dplResults) {
              document.add(blankLine());
              document.add(blankLine());
              document.add(createSubHeader("Search Details - " + result.getName().toUpperCase()));
              section = createDetailsTable(new float[] { 30, 70 });
              section.addCell(createLabelCell("Searching For:"));
              section.addCell(createValueCell(result.getName().toUpperCase()));
              section.addCell(createLabelCell("DPL Result ID:"));
              section.addCell(createValueCell(result.getResultId()));
              section.addCell(createLabelCell("Records Found:"));
              section.addCell(createValueCell(result.getItems().size() + ""));
              section.addCell(createLabelCell("Exact Match Found:"));
              section.addCell(createValueCell(result.exactMatchFound() ? "Yes" : "No"));
              section.addCell(createLabelCell("Partial Match Found:"));
              section.addCell(createValueCell(result.partialMatchFound() ? "Yes" : "No"));
              section.addCell(createLabelCell("Closest Name Matches:"));
              StringBuilder closest = new StringBuilder();
              for (DPLSearchItem closestItem : result.getTopMatches()) {
                closest.append(closest.length() > 0 ? "\n" : "");
                closest.append(" - " + closestItem.getPartyName());
              }
              section.addCell(createValueCell(closest.toString()));
              document.add(section);
              document.add(blankLine());

              document.add(createSubHeader("Results"));
              section = createDetailsTable(new float[] { 10, 20, 70 });
              section.addCell(createLabelCell("Item"));
              section.addCell(createLabelCell("Denial Code Country"));
              section.addCell(createLabelCell("Denied Party Name"));
              if (result.getItems() == null || result.getItems().isEmpty()) {
                section.addCell(createValueCell("No matches found", 1, 3));
              } else {
                int itemNo = 1;
                for (DPLSearchItem item : result.getItems()) {
                  section.addCell(createValueCell(itemNo + ""));
                  section.addCell(createValueCell(item.getCountryCode()));
                  section.addCell(createValueCell(item.getPartyName()));
                  itemNo++;
                }
              }
              document.add(section);
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

  public List<DPLSearchResult> getDplResults() {
    return dplResults;
  }

  public void setDplResults(List<DPLSearchResult> dplResults) {
    this.dplResults = dplResults;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

}
