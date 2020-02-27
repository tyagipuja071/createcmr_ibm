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
public class USPDFConverter extends DefaultPDFConverter {

  public USPDFConverter(String cmrIssuingCntry) throws IOException {
    super(cmrIssuingCntry);
  }

  @Override
  protected void addConverterCustomerDetails(EntityManager entityManager, Table customer, Data data) {

    customer.addCell(createLabelCell("Tax Class/Code1:"));
    customer.addCell(createValueCell(data.getTaxCd1()));
    customer.addCell(createLabelCell("Tax Class/Code2:"));
    customer.addCell(createValueCell(data.getTaxCd2()));

    customer.addCell(createLabelCell("Restricted:"));
    customer.addCell(createValueCell(data.getRestrictInd()));
    customer.addCell(createLabelCell("Restrict To:"));
    customer.addCell(createValueCell(data.getRestrictTo()));

    customer.addCell(createLabelCell("Non-IBM Company Code:"));
    customer.addCell(createValueCell(data.getNonIbmCompanyInd()));
    customer.addCell(createLabelCell("CSO Site:"));
    customer.addCell(createValueCell(data.getCsoSite()));

    customer.addCell(createLabelCell("ICC Tax Class:"));
    customer.addCell(createValueCell(data.getIccTaxClass()));
    customer.addCell(createLabelCell("ICC Tax Exempt Status:"));
    customer.addCell(createValueCell(data.getIccTaxExemptStatus()));

    customer.addCell(createLabelCell("Size Code:"));
    customer.addCell(createValueCell(data.getSizeCd()));
    customer.addCell(createLabelCell("Misc Bill Code:"));
    customer.addCell(createValueCell(data.getMiscBillCd()));

    customer.addCell(createLabelCell("BP Account Type:"));
    customer.addCell(createValueCell(data.getBpAcctTyp()));
    customer.addCell(createLabelCell("Business Partner Name:"));
    customer.addCell(createValueCell(data.getBpName()));
  }

  @Override
  protected void addConverterIBMDetails(EntityManager entityManager, Table ibm, Data data) {
    ibm.addCell(createLabelCell("Marketing Department:"));
    ibm.addCell(createValueCell(data.getMktgDept()));
    ibm.addCell(createLabelCell("Marketing A/R Department:"));
    ibm.addCell(createValueCell(data.getMtkgArDept()));

    ibm.addCell(createLabelCell("PCC Marketing Department:"));
    ibm.addCell(createValueCell(data.getPccMktgDept()));
    ibm.addCell(createLabelCell("PCC A/R Department:"));
    ibm.addCell(createValueCell(data.getPccArDept()));

    ibm.addCell(createLabelCell("SVC Territory Zone:"));
    ibm.addCell(createValueCell(data.getSvcTerritoryZone()));
    ibm.addCell(createLabelCell("SVC A/R Office:"));
    ibm.addCell(createValueCell(data.getSvcArOffice()));
  }

}
