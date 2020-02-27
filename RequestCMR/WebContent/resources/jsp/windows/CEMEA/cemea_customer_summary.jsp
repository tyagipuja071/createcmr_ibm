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
<cmr:view forGEO="CEMEA">
  <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="AbbrevName" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.abbrevNm}
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="AbbrevLocation" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
      ${summary.data.abbrevLocn}
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:view exceptForCountry="618">
      <cmr:column span="1" width="130">
        <label><cmr:fieldLabel fieldId="CommercialFinanced" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240">
            ${summary.data.commercialFinanced}
      </cmr:column>
    </cmr:view>
   </cmr:row>
   
  <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:view forCountry="677,680,620,767,805,823,832">
      <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="TeleCoverageRep" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240">
        ${summary.data.bpSalesRepNo}
      </cmr:column>
    </cmr:view>  
    <cmr:view exceptForCountry="618">   
      <cmr:column span="1" width="130">
        <label><cmr:fieldLabel fieldId="EmbargoCode" />: </label>
      </cmr:column>
      <cmr:column span="1" width="180">
        ${summary.data.embargoCd}
      </cmr:column>
    </cmr:view>
   </cmr:row>
   <cmr:view forCountry="618">
     <cmr:row addBackground="true">
      <cmr:column span="1" width="127">
      </cmr:column>
      <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="SpecialTaxCd" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240">
        ${summary.data.specialTaxCd}
      </cmr:column>
     </cmr:row>
   </cmr:view>
   <cmr:view forCountry="693">
     <cmr:row addBackground="true">
      <cmr:column span="1" width="127">
      </cmr:column>
      <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="Company" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240">
        ${summary.data.company}
      </cmr:column>
      <cmr:column span="1" width="130">
        <label><cmr:fieldLabel fieldId="LocalTax1" />: </label>
      </cmr:column>
      <cmr:column span="1" width="200">
        ${summary.data.taxCd1}
      </cmr:column>
     </cmr:row>
   </cmr:view>
   <cmr:view forCountry="704">
     <cmr:row addBackground="true">
      <cmr:column span="1" width="127">
      </cmr:column>
      <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="LocalTax1" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240">
        ${summary.data.taxCd1}
      </cmr:column>
     </cmr:row>
   </cmr:view>
   <cmr:view exceptForCountry="618">
    <cmr:column span="1" width="137">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CustPhone" />:</label>
    </cmr:column>
    <cmr:column span="1" width="170">
      ${summary.data.phone1}
    </cmr:column>       
   </cmr:view>
</cmr:view>