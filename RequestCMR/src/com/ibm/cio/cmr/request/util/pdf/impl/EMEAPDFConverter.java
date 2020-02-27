/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.Data;
import com.itextpdf.layout.element.Table;

/**
 * @author Jeffrey Zamora
 * 
 */
public class EMEAPDFConverter extends DefaultPDFConverter {

  public EMEAPDFConverter(String cmrIssuingCntry) throws IOException {
    super(cmrIssuingCntry);
  }

  @Override
  protected void addConverterCustomerDetails(EntityManager entityManager, Table customer, Data data) {

    customer.addCell(createLabelCell("VAT:"));
    customer.addCell(createValueCell(data.getVat()));
    
    customer.addCell(createLabelCell("VAT Exempt:"));
    customer.addCell(createValueCell("Y".equals(data.getVatExempt()) ? "Yes" : "No"));

    customer.addCell(createLabelCell("Tax Code:"));
    customer.addCell(createValueCell(data.getSpecialTaxCd()));
    
    customer.addCell(createLabelCell("Abbreviated Location:"));
    customer.addCell(createValueCell(data.getAbbrevLocn()));
    
    customer.addCell(createLabelCell("Embargo Code:"));
    customer.addCell(createValueCell(data.getEmbargoCd()));

  }

  @Override
  protected void addConverterIBMDetails(EntityManager entityManager, Table ibm, Data data) {
    ibm.addCell(createLabelCell("SR:"));
    ibm.addCell(createValueCell(data.getRepTeamMemberNo()));
    ibm.addCell(createLabelCell("SBO:"));
    ibm.addCell(createValueCell(data.getSalesBusOffCd()));

    ibm.addCell(createLabelCell("Collection Code:"));
    ibm.addCell(createValueCell(data.getCollectionCd()));
    ibm.addCell(createLabelCell("Economic Code:"));
    ibm.addCell(createValueCell(data.getEconomicCd()));

    ibm.addCell(createLabelCell("DP/CE Branch Office:"));
    ibm.addCell(createValueCell(data.getEngineeringBo(), 1, 3));

  }
}
