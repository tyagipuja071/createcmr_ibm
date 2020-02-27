package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;

/**
 * @author Mukesh Kumar
 * 
 */
public class LAPDFConverter extends DefaultPDFConverter {
  private static final Logger LOG = Logger.getLogger(DefaultPDFConverter.class);
  private final PdfFont regularFont;

  public LAPDFConverter(String cmrIssuingCntry) throws IOException {
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
    } catch (Exception e) {
      LOG.error("Error in Generating PDF for Request ID " + admin.getId().getReqId(), e);
      return false;
    }
  }

  @Override
  protected void addMainDetails(Admin admin, Data data, Document document) {
    document.add(createSubHeader("General Information"));
    Table main = createDetailsTable();

    main.addCell(createLabelCell("Request ID:"));
    main.addCell(createValueCell(admin.getId().getReqId() + ""));
    main.addCell(createLabelCell("Request Type:"));
    if ("Y".equals(admin.getProspLegalInd())) {
      main.addCell(createValueCell(admin.getReqType() + " (Prospect to Legal Conversion)"));
    } else {
      main.addCell(createValueCell(admin.getReqType()));
    }

    main.addCell(createLabelCell("Requester:"));
    main.addCell(createValueCell(admin.getRequesterNm()));
    main.addCell(createLabelCell("Originator:"));
    main.addCell(createValueCell(admin.getOriginatorNm()));

    main.addCell(createLabelCell("CMR Issuing Country:"));
    main.addCell(createValueCell(data.getCmrIssuingCntry()));
    main.addCell(createLabelCell("CMR No:"));
    main.addCell(createValueCell(data.getCmrNo()));

    main.addCell(createLabelCell("Customer Name:"));
    String customerName = StringUtils.isBlank(admin.getMainCustNm1()) ? "" : admin.getMainCustNm1();
    if (!StringUtils.isBlank(admin.getMainCustNm2())) {
      customerName += " " + admin.getMainCustNm2();
    }
    main.addCell(createValueCell(customerName, 1, 3));

    main.addCell(createLabelCell("Requesting LOB:"));
    main.addCell(createValueCell(admin.getRequestingLob()));
    main.addCell(createLabelCell("Request Reason:"));
    main.addCell(createValueCell(admin.getReqReason()));
    document.add(main);
  }

  @Override
  protected Addr addAddressDetails(Admin admin, Data data, EntityManager entityManager, Document document) {
    document.add(createSubHeader("Address Information"));

    List<Addr> addresses = getAddressList(entityManager, admin.getId().getReqId());

    Addr soldTo = null;
    for (Addr addr : addresses) {
      Table address = createDetailsTable();

      String[] cmrString = data.getCmrIssuingCntry().split("-");
      String cmrCntry = data.getCmrIssuingCntry();

      if (cmrString != null && cmrString.length > 0) {
        cmrCntry = cmrString[0].trim();
      }

      GEOHandler handler = RequestUtils.getGEOHandler(cmrCntry);

      if (handler.customerNamesOnAddress()) {
        address.addCell(createLabelCell("Customer Name:"));
        address.addCell(createValueCell(addr.getCustNm1(), 1, 3));
        address.addCell(createLabelCell("Customer Name Con't:"));
        address.addCell(createValueCell(addr.getCustNm2(), 1, 3));
      }
      address.addCell(createLabelCell("Address Type:"));
      address.addCell(createValueCell(addr.getId().getAddrType()));
      address.addCell(createLabelCell("Landed Country:"));
      address.addCell(createValueCell(addr.getLandCntry()));

      String lbl = "Address";
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getAddrTxt() + (StringUtils.isBlank(addr.getAddrTxt2()) ? "" : " " + addr.getAddrTxt2()), 1, 3));

      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "StateProv", "State/Province");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getStateProv()));
      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "PostalCode", "Postal Code");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getPostCd()));

      lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "Department", "Department");
      address.addCell(createLabelCell(lbl + ":"));
      address.addCell(createValueCell(addr.getDept()));

      if (!SystemLocation.BRAZIL.equals(data.getCmrIssuingCntry().subSequence(0, 3))) {
        lbl = PageManager.getLabel(data.getCmrIssuingCntry(), "Division", "Division");
        address.addCell(createLabelCell(lbl + ":"));
        address.addCell(createValueCell(addr.getDivn()));
      }

      address.addCell(createLabelCell("DPL Check Result:"));
      String dplCheck = addr.getDplChkResult();
      String dplCheckText = "";
      if ("P".equals(dplCheck)) {
        dplCheckText = "Passed";
      } else if ("F".equals(dplCheck)) {
        dplCheckText = "FAILED";
      } else if ("N".equals(dplCheck)) {
        dplCheckText = "Not Required";
      } else {
        dplCheckText = "Not Done";
      }
      Cell dplCell = createValueCell(dplCheckText, 1, 3);
      if ("F".equals(dplCheck)) {
        dplCell.setFontColor(Color.RED);
      }
      address.addCell(dplCell);

      if ("P".equals(dplCheck) || "F".equals(dplCheck)) {
        address.addCell(createLabelCell("DPL Check Date:"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        address.addCell(createValueCell(addr.getDplChkTs() != null ? sdf.format(addr.getDplChkTs()) : ""));
        if ("F".equals(dplCheck)) {
          address.addCell(createLabelCell("DPL Error Information:"));
          address.addCell(createValueCell(addr.getDplChkInfo()));
        } else {
          address.addCell(createValueCell(null));
          address.addCell(createValueCell(null));
        }
      }

      addConverterAddressDetails(entityManager, address, addr);

      document.add(address);
      document.add(blankLine());

      if (addr.getId().getAddrType().contains("(ZS01)")) {
        soldTo = addr;
      }
    }
    return soldTo;
  }

  @Override
  protected void addCustomerDetails(EntityManager entityManager, Data data, Document document) {
    document.add(createSubHeader("Customer Information"));
    Table customer = createDetailsTable();
    customer.addCell(createLabelCell("Abbreviated Name:"));
    customer.addCell(createValueCell(data.getAbbrevNm()));
    customer.addCell(createLabelCell("Preferred Language:"));
    customer.addCell(createValueCell(data.getCustPrefLang()));

    customer.addCell(createLabelCell("Subindustry:"));
    customer.addCell(createValueCell(data.getSubIndustryCd()));
    customer.addCell(createLabelCell("ISIC:"));
    customer.addCell(createValueCell(data.getIsicCd()));

    addConverterCustomerDetails(entityManager, customer, data);

    document.add(customer);
  }

  @Override
  protected void addIBMDetails(EntityManager entityManager, Data data, Document document) {
    document.add(createSubHeader("IBM Information"));
    Table ibm = createDetailsTable();

    ibm.addCell(createLabelCell("Enterprise No.:"));
    ibm.addCell(createValueCell(data.getEnterprise()));
    ibm.addCell(createLabelCell("Company No.:"));
    ibm.addCell(createValueCell(data.getCompany()));
    ibm.addCell(createLabelCell("Affiliate No.:"));
    ibm.addCell(createValueCell(data.getAffiliate(), 1, 3));

    ibm.addCell(createLabelCell("Search Term (SORTL):"));
    ibm.addCell(createValueCell(data.getSearchTerm()));
    ibm.addCell(createLabelCell("ISU Code:"));
    ibm.addCell(createValueCell(data.getIsuCd()));

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
    ibm.addCell(createLabelCell("Client Tier:"));
    ibm.addCell(createValueCell(data.getClientTier()));

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

    ibm.addCell(createLabelCell("Proxy Location Number:"));
    if (!StringUtils.isEmpty(data.getProxiLocnNo())) {
      String proxiLocnDesc = LAHandler.getProxiLocnDesc(entityManager, data.getCmrIssuingCntry(), data.getProxiLocnNo());
      ibm.addCell(createValueCell(data.getProxiLocnNo() + " - " + proxiLocnDesc));
    }

    addConverterIBMDetails(entityManager, ibm, data);

    document.add(ibm);
  }

}
