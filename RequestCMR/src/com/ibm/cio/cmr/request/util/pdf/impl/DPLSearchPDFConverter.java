/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.dpl.DPLSearchResult;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cmr.services.client.dpl.DPLRecord;
import com.ibm.cmr.services.client.dpl.DPLSearchResults;
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

  private List<DPLSearchResults> dplResults;
  private String user;
  private long searchTs;
  private String companyName;

  private Scorecard scorecard;

  /**
   * @throws IOException
   * 
   */
  public DPLSearchPDFConverter(String user, long searchTs, String companyName, List<DPLSearchResults> dplResults) throws IOException {
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
            // section.addCell(createLabelCell("DPL Search URL:"));
            // section.addCell(createValueCell(SystemParameters.getString("DPL_CHECK_URL")));
            section.addCell(createLabelCell("Main Company Name:"));
            section.addCell(createValueCell(this.companyName.toUpperCase()));
            document.add(section);
            document.add(blankLine());

            for (DPLSearchResults result : this.dplResults) {
              if (result.getDeniedPartyRecords() == null) {
                result.setDeniedPartyRecords(new ArrayList<DPLRecord>());
              }
              document.add(blankLine());
              document.add(blankLine());
              document.add(createSubHeader("Search Details - " + result.getSearchArgument().toUpperCase()));
              section = createDetailsTable(new float[] { 30, 70 });
              section.addCell(createLabelCell("Searching For:"));
              section.addCell(createValueCell(result.getSearchArgument().toUpperCase()));
              section.addCell(createLabelCell("Records Found:"));
              section.addCell(createValueCell(result.getDeniedPartyRecords().size() + ""));
              section.addCell(createLabelCell("Exact Match Found:"));
              section.addCell(createValueCell(DPLSearchResult.exactMatchFound(result) ? "Yes" : "No"));
              section.addCell(createLabelCell("Partial Match Found:"));
              section.addCell(createValueCell(DPLSearchResult.partialMatchFound(result) ? "Yes" : "No"));
              section.addCell(createLabelCell("Closest Name Match:"));
              StringBuilder closest = new StringBuilder();
              for (DPLRecord closestItem : DPLSearchResult.getTopMatches(result)) {
                String dplName = closestItem.getCompanyName();
                boolean person = false;
                if (StringUtils.isBlank(dplName) && !StringUtils.isBlank(closestItem.getCustomerLastName())) {
                  dplName = closestItem.getCustomerFirstName() + " " + closestItem.getCustomerLastName();
                  person = true;
                }
                if (!person) {
                  person = closestItem.getComments() != null && closestItem.getComments().contains("[Individual]");
                }
                if (dplName == null) {
                  dplName = "";
                }
                closest.append(closest.length() > 0 ? "\n" : "");
                closest.append(dplName + " (" + (person ? "Individual" : "Company") + ")");
              }
              section.addCell(createValueCell(closest.toString()));

              if (this.scorecard != null) {
                section.addCell(createLabelCell("Results Assessment:"));
                String assessment = this.scorecard.getDplAssessmentResult();
                if (assessment == null) {
                  assessment = "";
                }
                String assessResult = "Not Done";
                switch (assessment) {
                case "Y":
                  assessResult = "Matched DPL entities";
                  break;
                case "N":
                  assessResult = "No actual matches";
                  break;
                case "U":
                  assessResult = "Needs further review";
                  break;
                }
                section.addCell(createValueCell(assessResult));
              }
              section.addCell(createLabelCell("Assessed By:"));
              if (this.scorecard != null) {
                section.addCell(createValueCell(this.scorecard.getDplAssessmentBy() != null ? this.scorecard.getDplAssessmentBy() : ""));
              } else {
                section.addCell("");
              }
              section.addCell(createLabelCell("Assessed Date:"));
              if (this.scorecard != null) {
                if (this.scorecard.getDplAssessmentDate() != null) {
                  section.addCell(createValueCell(formatter.format(this.scorecard.getDplAssessmentDate())));
                } else {
                  section.addCell(createValueCell(""));
                }
              } else {
                section.addCell(createValueCell(""));
              }
              document.add(section);
              document.add(blankLine());

              document.add(createSubHeader("Results"));
              section = createDetailsTable(new float[] { 10, 10, 10, 50, 20 });
              section.addCell(createLabelCell("ID"));
              section.addCell(createLabelCell("Denial Country"));
              section.addCell(createLabelCell("Entity Type"));
              section.addCell(createLabelCell("Entity Name"));
              section.addCell(createLabelCell("Entity Address"));
              if (result.getDeniedPartyRecords() == null || result.getDeniedPartyRecords().isEmpty()) {
                section.addCell(createValueCell("No matches found", 1, 5));
              } else {
                // int itemNo = 1;
                for (DPLRecord item : result.getDeniedPartyRecords()) {
                  String dplName = item.getCompanyName();
                  boolean person = false;
                  if (StringUtils.isBlank(dplName) && !StringUtils.isBlank(item.getCustomerLastName())) {
                    dplName = item.getCustomerFirstName() + " " + item.getCustomerLastName();
                    person = true;
                  }
                  if (dplName == null) {
                    dplName = "";
                  }
                  if (!person) {
                    person = item.getComments() != null && item.getComments().contains("[Individual]");
                  }
                  section.addCell(createValueCell(item.getEntityId()));
                  section.addCell(createValueCell(item.getCountryCode()));
                  section.addCell(createValueCell(person ? "Individual" : "Company"));
                  section.addCell(createValueCell(dplName));
                  section.addCell(createValueCell(item.getEntityCity() + ", " + item.getEntityCountry()));
                  // itemNo++;
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
      LOG.error("Error in Generating PDF for " + this.companyName + "(" + this.user + ")", e);
      return false;
    }
  }

  public List<DPLSearchResults> getDplResults() {
    return dplResults;
  }

  public void setDplResults(List<DPLSearchResults> dplResults) {
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

  public Scorecard getScorecard() {
    return scorecard;
  }

  public void setScorecard(Scorecard scorecard) {
    this.scorecard = scorecard;
  }

}
