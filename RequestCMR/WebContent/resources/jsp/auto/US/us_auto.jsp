<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  AppUser user = AppUser.getUser(request);
%>
<!-- Automation Scripts for US -->
<cmr:view forGEO="US">

<!-- JS first -->
<script src="${resourcesPath}/js/auto/US/us_automation.js?${cmrv}" type="text/javascript"></script>

<!-- JSP -->
</cmr:view>
