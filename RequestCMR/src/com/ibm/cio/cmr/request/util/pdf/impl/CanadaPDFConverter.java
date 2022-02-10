/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;

/**
 * @author Rochelle Salazar
 * 
 */
public class CanadaPDFConverter extends DefaultPDFConverter {
  private static final Logger LOG = Logger.getLogger(DefaultPDFConverter.class);
  private PdfFont regularFont;

  public CanadaPDFConverter(String cmrIssuingCntry) throws IOException {
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
    } catch (IOException io) {
      LOG.warn("IO Error in Generating PDF for Request ID " + admin.getId().getReqId() + ":" + io.getMessage());
      return true;
    } catch (Exception e) {
      LOG.error("Error in Generating PDF for Request ID " + admin.getId().getReqId(), e);
      return false;
    }
  }

  @Override
  protected void addCustomerDetails(EntityManager entityManager, Data data, Document document) {
    LOG.debug("addCustomerDetails in canada");
    document.add(createSubHeader("Customer Information"));
    Table customer = createDetailsTable();
    customer.addCell(createLabelCell("Abbreviated Name:"));
    customer.addCell(createValueCell(data.getAbbrevNm()));
    customer.addCell(createLabelCell("Abbreviated Location:"));
    customer.addCell(createValueCell(data.getAbbrevLocn()));
    customer.addCell(createLabelCell("Preferred Language:"));
    customer.addCell(createValueCell(data.getCustPrefLang()));
    customer.addCell(createLabelCell("Sensitive Flag:"));
    customer.addCell(createValueCell(data.getSensitiveFlag()));
    customer.addCell(createLabelCell("Subindustry:"));
    customer.addCell(createValueCell(data.getSubIndustryCd()));
    customer.addCell(createLabelCell("ISIC:"));
    customer.addCell(createValueCell(data.getIsicCd()));
    customer.addCell(createLabelCell("GST/HST:"));
    customer.addCell(createValueCell(data.getVat()));
    customer.addCell(createLabelCell("QST:"));
    customer.addCell(createValueCell(data.getTaxCd3()));
    customer.addCell(createLabelCell("PST Exemption License Number:"));
    customer.addCell(createValueCell(data.getTaxPayerCustCd()));
    customer.addCell(createLabelCell("Authorization Exemption Type:"));
    customer.addCell(createValueCell(data.getSectorCd()));
    customer.addCell(createLabelCell("Profile EFC:"));
    customer.addCell(createValueCell(data.getIccTaxExemptStatus()));
    customer.addCell(createLabelCell("Order Block Code:"));
    customer.addCell(createValueCell(data.getCustAcctType()));
    customer.addCell(createLabelCell("Established Function Code:"));
    customer.addCell(createValueCell(data.getTaxCd1()));
    customer.addCell(createLabelCell("Purchase Order Number:"));
    customer.addCell(createValueCell(data.getContactName1()));
    customer.addCell(createLabelCell("PPS CEID:"));
    customer.addCell(createValueCell(data.getPpsceid()));
    customer.addCell(createLabelCell("Sales Branch Office:"));
    customer.addCell(createValueCell(data.getSalesBusOffCd()));
    customer.addCell(createLabelCell("Install Branch Office:"));
    customer.addCell(createValueCell(data.getInstallBranchOff()));
    customer.addCell(createLabelCell("CS Branch:"));
    customer.addCell(createValueCell(data.getSalesTeamCd()));
    customer.addCell(createLabelCell("AR-FAAR:"));
    customer.addCell(createValueCell(data.getAdminDeptCd()));
    customer.addCell(createLabelCell("Credit Code:"));
    customer.addCell(createValueCell(data.getCreditCd()));
    customer.addCell(createLabelCell("S/W Billing Frequency:"));
    customer.addCell(createValueCell(data.getCollectorNameNo()));
    customer.addCell(createLabelCell("Location/Province Code"));
    customer.addCell(createValueCell(data.getLocationNumber()));
    customer.addCell(createLabelCell("Number of Invoices:"));
    customer.addCell(createValueCell(data.getCusInvoiceCopies()));

    addConverterCustomerDetails(entityManager, customer, data);

    document.add(customer);
  }

  @Override
  protected void addIBMDetails(EntityManager entityManager, Data data, Document document) {
    document.add(createSubHeader("IBM Information"));
    Table ibm = createDetailsTable();

    ibm.addCell(createLabelCell("ISU Code:"));
    ibm.addCell(createValueCell(data.getIsuCd()));
    ibm.addCell(createLabelCell("Client Tier:"));
    ibm.addCell(createValueCell(data.getClientTier()));

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

}