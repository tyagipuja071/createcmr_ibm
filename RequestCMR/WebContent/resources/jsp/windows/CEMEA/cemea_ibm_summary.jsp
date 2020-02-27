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
          <label><cmr:fieldLabel fieldId="SalRepNameNo" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240">
        ${summary.data.repTeamMemberNo}
      </cmr:column>
      <cmr:view exceptForCountry="618">
        <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="EngineeringBo" />: </label>
        </cmr:column>
        <cmr:column span="1" width="180">
          ${summary.data.engineeringBo}
        </cmr:column>
      </cmr:view>
      <cmr:view forCountry="618">
        <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="CreditCd" />: </label>
        </cmr:column>
        <cmr:column span="1" width="240">
          ${summary.data.creditCd}
        </cmr:column>
      </cmr:view>
   </cmr:row>
   <cmr:row addBackground="true">
     <cmr:column span="1" width="127">
     </cmr:column>
     <cmr:column span="1" width="130">
        <label><cmr:fieldLabel fieldId="SalesBusOff" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240">
       ${summary.data.salesBusOffCd}
      </cmr:column>
      <cmr:view forCountry="618">
        <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="LocationNumber" />: </label>
        </cmr:column>
        <cmr:column span="1" width="240">
          ${summary.data.locationNumber}
        </cmr:column>
      </cmr:view>
   </cmr:row>
   <cmr:row addBackground="true">
      <cmr:column span="1" width="127">
      </cmr:column>
      <cmr:view forCountry="618">
        <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CurrencyCd" />: </label>
        </cmr:column>
        <cmr:column span="1" width="240">
          ${summary.data.legacyCurrencyCd}
        </cmr:column>
      </cmr:view>
      <cmr:view exceptForCountry="620,642,675,677,680,752,762,762,767,768,772,805,808,808,823,832,849,850,865,618">
        <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="AECISubDate" />: </label>
        </cmr:column>
        <cmr:column span="1" width="240">
          ${summary.data.agreementSignDate}
        </cmr:column>
      </cmr:view>
   </cmr:row>
   <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CMRNumber" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      ${summary.data.cmrNo}
    </cmr:column>
   </cmr:row>
</cmr:view>