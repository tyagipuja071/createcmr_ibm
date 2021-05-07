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
<cmr:view forGEO="KR">
  <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="SalRepNameNo" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
     ${summary.data.repTeamMemberNo}
    </cmr:column>
      <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="MrcCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      ${summary.data.mrcCd}
    </cmr:column>
    </cmr:row>
    <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
      <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="OrgNo" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      ${summary.data.orgNo}
    </cmr:column>
      <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CommercialFinanced" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      ${summary.data.commercialFinanced}
    </cmr:column>
    </cmr:row>
    <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
      <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="ContactName2" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      ${summary.data.contactName2}
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CreditCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      ${summary.data.creditCd}
    </cmr:column>
    </cmr:row>
    <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
      <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="ContactName3" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      ${summary.data.contactName3}
    </cmr:column>
    </cmr:row>
</cmr:view>