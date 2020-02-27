/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.dnb.DnbData;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;

/**
 * Converts {@link DnbData} to PDF
 * 
 * @author JeffZAMORA
 * 
 */
public class DnBPDFConverter extends DefaultPDFConverter {

  private static final Logger LOG = Logger.getLogger(DnBPDFConverter.class);

  private DnBCompany dnbData;
  private String dunsNo;
  private long reqId;

  private long confidenceCode = -1;

  public DnBPDFConverter(DnBCompany dnbData, String dunsNo, long reqId) throws IOException {
    super(null);
    this.dnbData = dnbData;
    this.dunsNo = dunsNo;
    this.reqId = reqId;
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

            if (this.confidenceCode > 0) {
              document.add(createHeader("D&B Highest Match for Request " + this.reqId + " from DUNS " + this.dunsNo));
            } else {
              document.add(createHeader("D&B Data imported into Request " + this.reqId + " from DUNS " + this.dunsNo));
            }

            document.add(createSubHeader("General Details"));
            Table section = createDetailsTable(new float[] { 30, 70 });
            section.addCell(createLabelCell("Company Name:"));
            section.addCell(createValueCell(this.dnbData.getCompanyName()));
            section.addCell(createLabelCell("Tradestyle Name:"));
            section.addCell(createValueCell(this.dnbData.getTradestyleName()));
            section.addCell(createLabelCell("DUNS No:"));
            section.addCell(createValueCell(this.dnbData.getDunsNo()));
            section.addCell(createLabelCell("Transferred from DUNS No.:"));
            section.addCell(createValueCell(this.dnbData.getTransferDunsNo()));
            section.addCell(createLabelCell("Domestic Ultimate DUNS No.:"));
            section.addCell(createValueCell(this.dnbData.getDuDunsNo()));
            section.addCell(createLabelCell("Domestic Ultimate Organization:"));
            section.addCell(createValueCell(this.dnbData.getDuOrganizationName()));
            section.addCell(createLabelCell("Global Ultimate DUNS No.:"));
            section.addCell(createValueCell(this.dnbData.getGuDunsNo()));
            section.addCell(createLabelCell("Global Ultimate Organization:"));
            section.addCell(createValueCell(this.dnbData.getGuOrganizationName()));
            section.addCell(createLabelCell("IBM ISIC:"));
            if (!StringUtils.isEmpty(this.dnbData.getIbmIsic())) {
              section.addCell(createValueCell(this.dnbData.getIbmIsic()
                  + (this.dnbData.getIbmIsicDesc() != null ? " - " + this.dnbData.getIbmIsicDesc() : "")));
            } else {
              section.addCell(createValueCell(""));
            }
            if (this.confidenceCode > 0) {
              section.addCell(createLabelCell("Match Confidence Code:"));
              section.addCell(createValueCell(this.confidenceCode + ""));
            }
            document.add(section);

            document.add(blankLine());
            document.add(createSubHeader("Primary Address"));
            section = createDetailsTable(new float[] { 30, 70 });
            section.addCell(createLabelCell("Street Address:"));
            section.addCell(createValueCell(this.dnbData.getPrimaryAddress()));
            section.addCell(createLabelCell("City:"));
            section.addCell(createValueCell(this.dnbData.getPrimaryCity()));
            section.addCell(createLabelCell("State:"));
            section.addCell(createValueCell(this.dnbData.getPrimaryStateName()));
            section.addCell(createLabelCell("County:"));
            section.addCell(createValueCell(this.dnbData.getPrimaryCounty()));
            section.addCell(createLabelCell("Postal Code:"));
            section.addCell(createValueCell(this.dnbData.getPrimaryPostalCode()));
            section.addCell(createLabelCell("Country:"));
            section.addCell(createValueCell(this.dnbData.getPrimaryCountry()));
            document.add(section);

            document.add(blankLine());
            document.add(createSubHeader("Mailing Address"));
            section = createDetailsTable(new float[] { 30, 70 });
            section.addCell(createLabelCell("Street Address:"));
            section.addCell(createValueCell(this.dnbData.getMailingAddress()));
            section.addCell(createLabelCell("City:"));
            section.addCell(createValueCell(this.dnbData.getMailingCity()));
            section.addCell(createLabelCell("State:"));
            section.addCell(createValueCell(this.dnbData.getMailingStateName()));
            section.addCell(createLabelCell("County:"));
            section.addCell(createValueCell(this.dnbData.getMailingCounty()));
            section.addCell(createLabelCell("Postal Code:"));
            section.addCell(createValueCell(this.dnbData.getMailingPostalCode()));
            section.addCell(createLabelCell("Country:"));
            section.addCell(createValueCell(this.dnbData.getMailingCountry()));
            document.add(section);

            document.add(blankLine());
            document.add(createSubHeader("Organization"));
            section = createDetailsTable(new float[] { 30, 70 });
            section.addCell(createLabelCell("Organization Type:"));
            section.addCell(createValueCell(this.dnbData.getOrganizationType()));
            int count = 1;
            for (String orgId : this.dnbData.getOrganizationIds()) {
              section.addCell(createLabelCell("Organization ID (" + count + "):"));
              section.addCell(createValueCell(orgId));
              count++;
            }
            count = 1;
            for (String code : this.dnbData.getDnbStandardIndustryCodes()) {
              section.addCell(createLabelCell("D&B Industry Code (" + count + "):"));
              section.addCell(createValueCell(code));
              count++;
            }
            section.addCell(createLabelCell("Line of Business:"));
            section.addCell(createValueCell(this.dnbData.getLineOfBusiness()));
            section.addCell(createLabelCell("No. of Employees:"));
            section.addCell(createValueCell(this.dnbData.getIndividualEmployeeCount() > 0 ? this.dnbData.getIndividualEmployeeCount() + "" : ""));
            section.addCell(createLabelCell("No. of Employees (Consolidated):"));
            section.addCell(createValueCell(this.dnbData.getConsolidatedEmployeeCount() > 0 ? this.dnbData.getConsolidatedEmployeeCount() + "" : ""));
            section.addCell(createLabelCell("Principal Person Name:"));
            section.addCell(createValueCell(this.dnbData.getPrincipalName()));
            section.addCell(createLabelCell("Principal Person Title:"));
            section.addCell(createValueCell(this.dnbData.getPrincipalTitle()));
            section.addCell(createLabelCell("Telephone No.:"));
            section.addCell(createValueCell(this.dnbData.getPhoneNo()));
            section.addCell(createLabelCell("Facsimile No.:"));
            section.addCell(createValueCell(this.dnbData.getFaxNo()));

            document.add(section);

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

  public long getConfidenceCode() {
    return confidenceCode;
  }

  public void setConfidenceCode(long confidenceCode) {
    this.confidenceCode = confidenceCode;
  }

}
