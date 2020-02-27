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
<cmr:view forCountry="631">
<cmr:row>
<cmr:column span="1" width="127">
</cmr:column>
<cmr:column span="1" width="130">
<label>Proxy Location Number:</label>
</cmr:column>
<cmr:column span="1" width="170">
<%if(summary.getData().getProxiLocnNo() != null && summary.getData().getProxiLocnNo() != ""){%>
${summary.data.proxiLocnNo != null ? summary.data.proxiLocnNo : ""} - ${summary.proxyLocnDesc != null ? summary.proxyLocnDesc : ""}
<%} %>
</cmr:column>
</cmr:row>
  
</cmr:view>