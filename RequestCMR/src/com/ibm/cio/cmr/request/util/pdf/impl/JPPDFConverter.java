/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;
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

public class JPPDFConverter extends DefaultPDFConverter {
  private final PdfFont regularFont;
  private static final Logger LOG = Logger.getLogger(RequestToPDFConverter.class);

  public JPPDFConverter(String cmrIssuingCntry) throws IOException {
    super(cmrIssuingCntry);
    this.regularFont = PdfFontFactory.createFont(FontConstants.HELVETICA);
  }

  @Override
  protected Cell createValueCell(String text, int rowSpan, int colSpan) {
    Cell cell = new Cell(rowSpan, colSpan);
    ClassLoader classLoader = getClass().getClassLoader();
    String FONT = null;
    PdfFont font = null;
    if ((text != null && (!text.isEmpty())) && (textContainingLanguage(text) != null)) {
      try {
        if (textContainingLanguage(text).equalsIgnoreCase("JAPANESE")) {
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

  public String textContainingLanguage(String text) {
    for (char charac : text.toCharArray()) {
      if (Character.UnicodeBlock.of(charac) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
        return "JAPANESE";
      }
    }
    return null;
  }

  @Override
  protected void addMainDetails(Admin admin, Data data, Document document) {
    String custGrpDesc = "";
    String custSubGrpDesc = "";
    String custType = "";

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

    main.addCell(createLabelCell("Scenario Type:"));
    if ("Create".equalsIgnoreCase(admin.getReqType())) {
      custGrpDesc = getCustGrpDesc(data.getCustGrp());
    } else if ("Update".equalsIgnoreCase(admin.getReqType())) {
      custGrpDesc = "";
    }
    main.addCell(createValueCell(custGrpDesc));

    main.addCell(createLabelCell("Scenario Sub-type:"));
    if ("Create".equalsIgnoreCase(admin.getReqType())) {
      custSubGrpDesc = getCustSubGrpDesc(data.getCustSubGrp());
    } else if ("Update".equalsIgnoreCase(admin.getReqType())) {
      custSubGrpDesc = "";
    }
    main.addCell(createValueCell(custSubGrpDesc));

    main.addCell(createLabelCell("Requested Records:"));
    if ("Create".equalsIgnoreCase(admin.getReqType())) {
      if ("CEA".equalsIgnoreCase(admin.getCustType())) {
        custType = "Company, Establishment, Account";
      } else if ("EA".equalsIgnoreCase(admin.getCustType())) {
        custType = "Establishment, Account";
      } else if ("C".equalsIgnoreCase(admin.getCustType())) {
        custType = "Subsidiary Company";
      } else if ("A".equalsIgnoreCase(admin.getCustType())) {
        custType = "Account";
      }
    } else if ("Update".equalsIgnoreCase(admin.getReqType())) {
      if ("CEA".equalsIgnoreCase(admin.getCustType())) {
        custType = "Company, Establishment, Account";
      } else if ("CE".equalsIgnoreCase(admin.getCustType())) {
        custType = "Company, Establishment";
      } else if ("C".equalsIgnoreCase(admin.getCustType())) {
        custType = "Subsidiary Company";
      }
    }
    main.addCell(createValueCell(custType));

    main.addCell(createLabelCell("Expedite ?:"));
    main.addCell(createValueCell(admin.getExpediteInd()));

    main.addCell(createLabelCell("Requesting LOB:"));
    main.addCell(createValueCell(admin.getRequestingLob()));
    main.addCell(createLabelCell("Request Reason:"));
    main.addCell(createValueCell(admin.getReqReason()));

    main.addCell(createLabelCell("OFCD /Sales(Team) No/Rep Sales No Change:"));
    main.addCell(createValueCell(data.getIcmsInd()));

    main.addCell(createLabelCell("Request Due Date:"));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    main.addCell(createValueCell(data.getRequestDueDate() != null ? sdf.format(data.getRequestDueDate()) : ""));

    document.add(main);
  }

  private String getCustGrpDesc(String custGrpVal) {
    String custGrpDesc = "";
    if (!StringUtils.isEmpty(custGrpVal)) {
      EntityManager em = JpaManager.getEntityManager();
      try {
        String sql = ExternalizedQuery.getSql("QUERY.GET.CUSTGRP_DESC_BY_CNTRY_CUSTGRPVAL");
        PreparedQuery query = new PreparedQuery(em, sql);
        query.setParameter("ISSUING_CNTRY", "760");
        query.setParameter("CUST_TYP_VAL", custGrpVal);
        List<String> results = query.getResults(String.class);
        if (results != null && !results.isEmpty()) {
          custGrpDesc = results.get(0);
        }
      } catch (Exception e) {
        LOG.error("Error in getting custGrpDesc when Generate PDF for custGrpVal " + custGrpVal);
      } finally {
        em.close();
      }
    }
    return custGrpDesc;
  }

  private String getCustSubGrpDesc(String custSubGrpVal) {
    String custSubGrpDesc = "";
    if (!StringUtils.isEmpty(custSubGrpVal)) {
      EntityManager em = JpaManager.getEntityManager();
      try {
        String sql = ExternalizedQuery.getSql("QUERY.GET.CUSTSUBGRP_DESC_BY_CNTRY_CUSTSUBGRPVAL");
        PreparedQuery query = new PreparedQuery(em, sql);
        query.setParameter("ISSUING_CNTRY", "760");
        query.setParameter("CUST_SUB_TYP_VAL", custSubGrpVal);
        List<String> results = query.getResults(String.class);
        if (results != null && !results.isEmpty()) {
          custSubGrpDesc = results.get(0);
        }
      } catch (Exception e) {
        LOG.error("Error in getting custSubGrpDesc when Generate PDF for custGrpVal " + custSubGrpVal);
      } finally {
        em.close();
      }
    }
    return custSubGrpDesc;
  }

  @Override
  protected Addr addAddressDetails(Admin admin, Data data, EntityManager entityManager, Document document) {
    document.add(createSubHeader("Address Information"));

    List<Addr> addresses = getAddressList(entityManager, admin.getId().getReqId());

    Addr soldTo = null;
    for (Addr addr : addresses) {
      Table address = createDetailsTable();

      address.addCell(createLabelCell("Address Type:"));
      address.addCell(createValueCell(addr.getId().getAddrType()));
      address.addCell(createLabelCell("Full English Name:"));
      address.addCell(createValueCell(addr.getCustNm3()));
      address.addCell(createLabelCell("Customer Name-KANJI:"));
      address.addCell(createValueCell(addr.getCustNm1()));
      address.addCell(createLabelCell("Name-KANJI Continue:"));
      address.addCell(createValueCell(addr.getCustNm2()));
      address.addCell(createLabelCell("Katakana:"));
      address.addCell(createValueCell(addr.getCustNm4()));
      address.addCell(createLabelCell("Address:"));
      address.addCell(createValueCell(addr.getAddrTxt()));
      address.addCell(createLabelCell("Postal Code:"));
      address.addCell(createValueCell(addr.getPostCd()));
      address.addCell(createLabelCell("Department:"));
      address.addCell(createValueCell(addr.getDept()));
      address.addCell(createLabelCell("Branch/Office:"));
      address.addCell(createValueCell(addr.getOffice()));
      address.addCell(createLabelCell("Building:"));
      address.addCell(createValueCell(addr.getBldg()));
      address.addCell(createLabelCell("Tel No:"));
      address.addCell(createValueCell(addr.getCustPhone()));
      address.addCell(createLabelCell("Location:"));
      address.addCell(createValueCell(addr.getLocationCode()));
      address.addCell(createLabelCell("FAX:"));
      address.addCell(createValueCell(addr.getCustFax()));
      address.addCell(createLabelCell("Estab Function Code:"));
      address.addCell(createValueCell(addr.getEstabFuncCd()));
      address.addCell(createLabelCell("Estab No:"));
      address.addCell(createValueCell(addr.getDivn()));
      address.addCell(createLabelCell("Company No:"));
      address.addCell(createValueCell(addr.getCity2()));
      address.addCell(createLabelCell("Company Size:"));
      address.addCell(createValueCell(Integer.toString(addr.getCompanySize())));
      address.addCell(createLabelCell("Contact:"));
      address.addCell(createValueCell(addr.getContact()));
      address.addCell(createLabelCell("ROL Flag:"));
      address.addCell(createValueCell(addr.getRol()));

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
      Cell dplCell = createValueCell(dplCheckText);
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
    customer.addCell(createLabelCell("Account Abbreviated Name:"));
    customer.addCell(createValueCell(data.getAbbrevNm(), 1, 3));
    customer.addCell(createLabelCell("Preferred Language:"));
    customer.addCell(createValueCell(data.getCustPrefLang()));

    customer.addCell(createLabelCell("JSIC:"));
    customer.addCell(createValueCell(data.getJsicCd()));
    customer.addCell(createLabelCell("Subindustry:"));
    customer.addCell(createValueCell(data.getSubIndustryCd()));
    customer.addCell(createLabelCell("ISIC:"));
    customer.addCell(createValueCell(data.getIsicCd()));

    addConverterCustomerDetails(entityManager, customer, data);

    document.add(customer);
  }

  @Override
  protected void addConverterCustomerDetails(EntityManager entityManager, Table customer, Data data) {
    customer.addCell(createLabelCell("Customer Name_Detail:"));
    customer.addCell(createValueCell(data.getEmail2()));
    customer.addCell(createLabelCell("OEM:"));
    customer.addCell(createValueCell(data.getOemInd()));
    customer.addCell(createLabelCell("Leasing:"));
    customer.addCell(createValueCell(data.getLeasingCompanyIndc()));
    customer.addCell(createLabelCell("Education Group:"));
    customer.addCell(createValueCell(data.getEducAllowCd()));
    customer.addCell(createLabelCell("Customer Group:"));
    customer.addCell(createValueCell(data.getCustAcctType()));
    customer.addCell(createLabelCell("Customer Class:"));
    customer.addCell(createValueCell(data.getCustClass()));
    customer.addCell(createLabelCell("IIN:"));
    customer.addCell(createValueCell(data.getIinInd()));
    customer.addCell(createLabelCell("VAR:"));
    customer.addCell(createValueCell(data.getValueAddRem()));
    customer.addCell(createLabelCell("Channel:"));
    customer.addCell(createValueCell(data.getChannelCd()));
    customer.addCell(createLabelCell("SI:"));
    customer.addCell(createValueCell(data.getSiInd()));
    customer.addCell(createLabelCell("CRS Code:"));
    customer.addCell(createValueCell(data.getCrsCd()));
    customer.addCell(createLabelCell("CAR Code:"));
    customer.addCell(createValueCell(data.getCreditCd()));
    customer.addCell(createLabelCell("Government Entity:"));
    customer.addCell(createValueCell(data.getGovType()));
    customer.addCell(createLabelCell("Outsourcing Service:"));
    customer.addCell(createValueCell(data.getOutsourcingService()));
    customer.addCell(createLabelCell("zSeries SW:"));
    customer.addCell(createValueCell(data.getZseriesSw()));
    customer.addCell(createLabelCell("Direct/BP:"));
    customer.addCell(createValueCell(data.getCreditBp()));
  }

  @Override
  protected void addIBMDetails(EntityManager entityManager, Data data, Document document) {
    document.add(createSubHeader("IBM Information"));
    Table ibm = createDetailsTable();

    ibm.addCell(createLabelCell("Search Term (SORTL):"));
    ibm.addCell(createValueCell(data.getSearchTerm()));

    ibm.addCell(createLabelCell("INAC Type:"));
    ibm.addCell(createValueCell(data.getInacType()));
    ibm.addCell(createLabelCell("INAC Code:"));
    ibm.addCell(createValueCell(data.getInacCd()));

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

    ibm.addCell(createLabelCell("DUNS:"));
    ibm.addCell(createValueCell(data.getDunsNo()));

    addConverterIBMDetails(entityManager, ibm, data);

    document.add(ibm);
  }

  @Override
  protected void addConverterIBMDetails(EntityManager entityManager, Table ibm, Data data) {

    ibm.addCell(createLabelCell("CMR Number 2:"));
    ibm.addCell(createValueCell(data.getCmrNo2()));
    ibm.addCell(createLabelCell("CMR Owner:"));
    ibm.addCell(createValueCell(data.getCmrOwner()));
    ibm.addCell(createLabelCell("Rep Sales No.:"));
    ibm.addCell(createValueCell(data.getRepTeamMemberNo()));
    ibm.addCell(createLabelCell("Sales/Team No (Dealer No.):"));
    ibm.addCell(createValueCell(data.getSalesTeamCd()));

    if ("1".equalsIgnoreCase(data.getPrivIndc())) {
      ibm.addCell(createLabelCell("Request For:"));
      ibm.addCell(createValueCell("So Projec/FHS-OAK"));
    } else if ("2".equalsIgnoreCase(data.getPrivIndc())) {
      ibm.addCell(createLabelCell("Request For:"));
      ibm.addCell(createValueCell("NOS Porject"));
    } else if ("3".equalsIgnoreCase(data.getPrivIndc())) {
      ibm.addCell(createLabelCell("Request For:"));
      ibm.addCell(createValueCell("So Infra/FHS-OTR(Infra)"));
    } else {
      ibm.addCell(createLabelCell("Request For:"));
      ibm.addCell(createValueCell(data.getPrivIndc()));
    }

    ibm.addCell(createLabelCell("Office Code:"));
    ibm.addCell(createValueCell(data.getSalesBusOffCd()));
    ibm.addCell(createLabelCell("Work No:"));
    ibm.addCell(createValueCell(data.getOrgNo()));

    ibm.addCell(createLabelCell("Charge Code:"));
    ibm.addCell(createValueCell(data.getChargeCd()));

    String prodType = data.getProdType() != null && data.getProdType().length() > 7 ? data.getProdType() : "00000000";
    String prodTypeTxt = "";
    if ("1".equals(prodType.substring(0, 1))) {
      prodTypeTxt = prodTypeTxt + "AAS HW, ";
    }
    if ("1".equals(prodType.substring(1, 2))) {
      prodTypeTxt = prodTypeTxt + " AAS z9/zSeries SW, ";
    }
    if ("1".equals(prodType.substring(2, 3))) {
      prodTypeTxt = prodTypeTxt + "Others expect AAS z9/zSeries SW, ";
    }
    if ("1".equals(prodType.substring(3, 4))) {
      prodTypeTxt = prodTypeTxt + "QCOS, ";
    }
    if ("1".equals(prodType.substring(4, 5))) {
      prodTypeTxt = prodTypeTxt + "Lenovo, ";
    }
    if ("1".equals(prodType.substring(5, 6))) {
      prodTypeTxt = prodTypeTxt + "CISCO, ";
    }
    if ("1".equals(prodType.substring(6, 7))) {
      prodTypeTxt = prodTypeTxt + "Demo used, ";
    }
    if ("1".equals(prodType.substring(7))) {
      prodTypeTxt = prodTypeTxt + "Others,";
    }
    ibm.addCell(createLabelCell("Product Type:"));
    ibm.addCell(createValueCell(prodTypeTxt, 1, 3));

    ibm.addCell(createLabelCell("Project Code:"));
    ibm.addCell(createValueCell(data.getSoProjectCd()));
    ibm.addCell(createLabelCell("CS DIV:"));
    ibm.addCell(createValueCell(data.getCsDiv()));
    ibm.addCell(createLabelCell("Billing Process Code:"));
    ibm.addCell(createValueCell(data.getBillingProcCd()));
    ibm.addCell(createLabelCell("Invoice Split Code:"));
    ibm.addCell(createValueCell(data.getInvoiceSplitCd()));
    ibm.addCell(createLabelCell("Credit Customer No:"));
    ibm.addCell(createValueCell(data.getCreditToCustNo()));
    ibm.addCell(createLabelCell("CS BO Code:"));
    ibm.addCell(createValueCell(data.getCsBo()));
    ibm.addCell(createLabelCell("TIER-2:"));
    ibm.addCell(createValueCell(data.getTier2()));
    ibm.addCell(createLabelCell("Bill to Customer No:"));
    ibm.addCell(createValueCell(data.getBillToCustNo()));
    ibm.addCell(createLabelCell("Admin Depart Code:"));
    ibm.addCell(createValueCell(data.getAdminDeptCd()));
    ibm.addCell(createLabelCell("Admin Depart Line:"));
    ibm.addCell(createValueCell(data.getAdminDeptLine()));
  }

}
