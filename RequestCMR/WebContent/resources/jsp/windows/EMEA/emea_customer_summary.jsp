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
<cmr:view forGEO="EMEA">

  <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="AbbrevName" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.abbrevNm}
        </cmr:column>

    <cmr:column span="1" width="90">
      <label><cmr:fieldLabel fieldId="AbbrevLocation" />: </label>
    </cmr:column>
    <cmr:column span="1" width="100">
          ${summary.data.abbrevLocn}
        </cmr:column>

  </cmr:row>

  <cmr:row addBackground="true">
    <cmr:column span="1" width="127"></cmr:column>
    <cmr:column span="1" width="130">
      <cmr:view exceptForCountry="862,666,726">
        <label><cmr:fieldLabel fieldId="SpecialTaxCd" />: </label>
      </cmr:view> 
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String taxCd = DropdownListController.getDescription("SpecialTaxCd", data.getSpecialTaxCd(), cntry);
      %>
      <%=taxCd != null ? taxCd : ""%>
    </cmr:column>
		<cmr:view forCountry="758">
			<cmr:column span="1" width="100">
				<label><cmr:fieldLabel fieldId="LocalTax1" />: </label>
			</cmr:column>
			<cmr:column span="1" width="130">${summary.data.taxCd1}</cmr:column>

		</cmr:view>
		<cmr:view exceptForGEO="MCO,MCO1,MCO2">
			<cmr:column span="1" width="130">
				<label><cmr:fieldLabel fieldId="EmbargoCode" />: </label>
			</cmr:column>
			<cmr:column span="1" width="200">
				${summary.data.embargoCd}
      		</cmr:column>
		</cmr:view>
	</cmr:row>
  <cmr:row addBackground="true">
  <cmr:view forCountry="758">
   <cmr:column span="1" width="127"> </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="IdentClient" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.identClient}
        </cmr:column>
    </cmr:view>    
  </cmr:row>
</cmr:view>
