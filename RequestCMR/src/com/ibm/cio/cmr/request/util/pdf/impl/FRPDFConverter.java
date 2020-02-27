/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.Data;
import com.itextpdf.layout.element.Table;

public class FRPDFConverter extends DefaultPDFConverter {

  public FRPDFConverter(String cmrIssuingCntry) throws IOException {
    super(cmrIssuingCntry);
  }

  @Override
  protected void addConverterCustomerDetails(EntityManager entityManager, Table customer, Data data) {

  }

  @Override
  protected void addConverterIBMDetails(EntityManager entityManager, Table ibm, Data data) {

  }
}
