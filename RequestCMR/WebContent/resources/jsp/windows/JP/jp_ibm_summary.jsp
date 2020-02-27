<%@page import="com.ibm.cio.cmr.request.entity.Data"%>
<%@page import="com.ibm.cio.cmr.request.controller.DropdownListController"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="com.ibm.cio.cmr.request.entity.Admin"%>
<%@page import="com.ibm.cio.cmr.request.entity.Addr"%>
<%@page import="com.ibm.cio.cmr.request.model.window.RequestSummaryModel"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestSummaryModel summary = (RequestSummaryModel) request.getAttribute("summary");
  Data data = summary.getData();
  Admin admin = summary.getAdmin();
  Addr addr = summary.getAddr();
  String cntry = data.getCmrIssuingCntry();
%>
<cmr:view forGEO="JP">

  <cmr:row addBackground="false" topPad="10">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CMRNumber" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String cmrNo = DropdownListController.getDescription("CMRNumber", data.getCmrNo(), cntry);
      %>
      <%=cmrNo != null ? cmrNo : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CMRNumber2" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String cmrNo2 = DropdownListController.getDescription("CMRNumber2", data.getCmrNo2(), cntry);
      %>
      <%=cmrNo2 != null ? cmrNo2 : ""%>
    </cmr:column>
  </cmr:row>

      <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="INACType" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String inacType = DropdownListController.getDescription("INACType", data.getInacType(), cntry);
      %>
      <%=inacType != null ? inacType : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="INACCode" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String inacCd = DropdownListController.getDescription("INACCode", data.getInacCd(), cntry);
      %>
      <%=inacCd != null ? inacCd : ""%>
    </cmr:column>
  </cmr:row>

  
            <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CoverageID" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String covId = DropdownListController.getDescription("CoverageID", data.getCovId(), cntry);
      %>
      <%=covId != null ? covId : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="SalRepNameNo" />:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String repTeamMemberNo = DropdownListController.getDescription("SalRepNameNo", data.getRepTeamMemberNo(), cntry);
      %>
      <%=repTeamMemberNo != null ? repTeamMemberNo : ""%>
    </cmr:column>
  </cmr:row>
  
              <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="SalesSR" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String salesTeamCd = DropdownListController.getDescription("SalesSR", data.getSalesTeamCd(), cntry);
      %>
      <%=salesTeamCd != null ? salesTeamCd : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="PrivIndc" />:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String privIndc = DropdownListController.getDescription("PrivIndc", data.getPrivIndc(), cntry);
      %>
      <%=privIndc != null ? privIndc : ""%>
    </cmr:column>
  </cmr:row>
  
                <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="SalesBusOff" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String salesBusOffCd = DropdownListController.getDescription("SalesBusOff", data.getSalesBusOffCd(), cntry);
      %>
      <%=salesBusOffCd != null ? salesBusOffCd : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="OriginatorNo" />:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String orgNo = DropdownListController.getDescription("OriginatorNo", data.getOrgNo(), cntry);
      %>
      <%=orgNo != null ? orgNo : ""%>
    </cmr:column>
  </cmr:row>
  
                  <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="ProdType" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String prodType = DropdownListController.getDescription("ProdType", data.getProdType(), cntry);
      %>
      <%=prodType != null ? prodType : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="ChargeCd" />:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String chargeCd = DropdownListController.getDescription("ChargeCd", data.getChargeCd(), cntry);
      %>
      <%=chargeCd != null ? chargeCd : ""%>
    </cmr:column>
  </cmr:row>
  
                    <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="ProjectCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String soProjectCd = DropdownListController.getDescription("ProjectCd", data.getSoProjectCd(), cntry);
      %>
      <%=soProjectCd != null ? soProjectCd : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CSDiv" />:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String csDiv = DropdownListController.getDescription("CSDiv", data.getCsDiv(), cntry);
      %>
      <%=csDiv != null ? csDiv : ""%>
    </cmr:column>
  </cmr:row>
  
                      <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="BillingProcCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String billingProcCd = DropdownListController.getDescription("BillingProcCd", data.getBillingProcCd(), cntry);
      %>
      <%=billingProcCd != null ? billingProcCd : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="InvoiceSplitCd" />:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String invoiceSplitCd = DropdownListController.getDescription("InvoiceSplitCd", data.getInvoiceSplitCd(), cntry);
      %>
      <%=invoiceSplitCd != null ? invoiceSplitCd : ""%>
    </cmr:column>
  </cmr:row>
  
                        <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CreditToCustNo" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String creditToCustNo = DropdownListController.getDescription("CreditToCustNo", data.getInvoiceSplitCd(), cntry);
      %>
      <%=creditToCustNo != null ? creditToCustNo : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CSBOCd" />:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String csBo = DropdownListController.getDescription("CSBOCd", data.getCsBo(), cntry);
      %>
      <%=csBo != null ? csBo : ""%>
    </cmr:column>
  </cmr:row>
  
                          <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="Tier2" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String tier2 = DropdownListController.getDescription("Tier2", data.getTier2(), cntry);
      %>
      <%=tier2 != null ? tier2 : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="BillToCustNo" />:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String billToCustNo = DropdownListController.getDescription("BillToCustNo", data.getTier2(), cntry);
      %>
      <%=billToCustNo != null ? billToCustNo : ""%>
    </cmr:column>
  </cmr:row>
  
                            <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="AdminDeptCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String adminDeptCd = DropdownListController.getDescription("AdminDeptCd", data.getAdminDeptCd(), cntry);
      %>
      <%=adminDeptCd != null ? adminDeptCd : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="AdminDeptLine" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String adminDeptLine = DropdownListController.getDescription("AdminDeptLine", data.getAdminDeptLine(), cntry);
      %>
      <%=adminDeptLine != null ? adminDeptLine : ""%>
    </cmr:column>
  </cmr:row>
  
</cmr:view>
