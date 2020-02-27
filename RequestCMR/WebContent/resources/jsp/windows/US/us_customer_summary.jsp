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
<cmr:view forGEO="US">
  <cmr:row  addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label> <cmr:fieldLabel fieldId="RestrictTo" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String restrictTo = DropdownListController.getDescription("RestrictTo", data.getRestrictTo(), cntry);
      %>
      <%=restrictTo != null ? restrictTo : ""%>
      <br>
      <%
        if ("Y".equals(data.getRestrictInd())) {
      %>
      <input type="checkbox" id="restrictedInd" value="Y" disabled checked>
      <cmr:label fieldId="" forRadioOrCheckbox="true">Restricted</cmr:label>
      <%
        }
      %>
    </cmr:column>
    <cmr:column span="2" width="350">
      <input type="checkbox" id="oemInd" value="Y" disabled <%="Y".equals(data.getOemInd()) ? "checked" : ""%>>
      <cmr:label fieldId="" forRadioOrCheckbox="true">OEM</cmr:label> 
      &nbsp;

      <input type="checkbox" id="fedSiteInd" value="Y" disabled <%="Y".equals(data.getFedSiteInd()) ? "checked" : ""%>>
      <cmr:label fieldId="" forRadioOrCheckbox="true">Federal Site</cmr:label>
      &nbsp;

      <input type="checkbox" id="outCityLimits" value="Y" disabled <%="Y".equals(data.getOutCityLimit()) ? "checked" : ""%>>
      <cmr:label fieldId="" forRadioOrCheckbox="true">Out of City Limits</cmr:label> 
      &nbsp;
    </cmr:column>
  </cmr:row>
  <cmr:row  addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label>
        <cmr:fieldLabel fieldId="NonIBMCompInd" />:
      </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String nonIbm = DropdownListController.getDescription("NonIBMCompInd", data.getNonIbmCompanyInd(), cntry);
      %>
      <%= nonIbm != null ? nonIbm : "" %>
    </cmr:column>
    <cmr:column span="1" width="160">
      <label>
        <cmr:fieldLabel fieldId="CSOSite" />:
      </label>
    </cmr:column>
    <cmr:column span="1" width="170">
       ${summary.data.csoSite}
    </cmr:column>
  </cmr:row>

  <cmr:row  addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label>
        <cmr:fieldLabel fieldId="ICCTaxClass" />:
      </label>
    </cmr:column>
    <cmr:column span="1" width="240">
       ${summary.data.iccTaxClass}
    </cmr:column>
    <cmr:column span="1" width="160">
      <label>
        <cmr:fieldLabel fieldId="ICCTaxExemptStatus" />:
      </label>
    </cmr:column>
    <cmr:column span="1" width="170">
       ${summary.data.iccTaxExemptStatus}
    </cmr:column>
  </cmr:row>

  <cmr:row  addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label>
        <cmr:fieldLabel fieldId="SizeCode" />:
      </label>
    </cmr:column>
    <cmr:column span="1" width="240">
       ${summary.data.sizeCd}
    </cmr:column>
    <cmr:column span="1" width="160">
      <label>
        <cmr:fieldLabel fieldId="MiscBillCode" />:
      </label>
    </cmr:column>
    <cmr:column span="1" width="170">
       ${summary.data.miscBillCd}
    </cmr:column>
  </cmr:row>

  <cmr:row  addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label>
        <cmr:fieldLabel fieldId="BPAccountType" />:
      </label>
    </cmr:column>
    <cmr:column span="1" width="240">
       ${summary.data.bpAcctTyp}
    </cmr:column>
    <cmr:column span="1" width="160">
      <label>
        <cmr:fieldLabel fieldId="BPName" />:
      </label>
    </cmr:column>
    <cmr:column span="1" width="170">
      <%
        String bpName = DropdownListController.getDescription("BPName", data.getBpName(), cntry);
      %>
      <%= bpName != null ? bpName : "" %>
    </cmr:column>
  </cmr:row>


</cmr:view>
