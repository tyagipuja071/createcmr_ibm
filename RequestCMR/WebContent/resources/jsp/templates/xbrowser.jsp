<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
String browser = request.getHeader("User-Agent").toUpperCase();
%>
<%if (browser.contains("MSIE")){%>
<link rel="stylesheet" type="text/css" href="${resourcesPath}/css/cmr-ie.css?${cmrv}" />
<%}%>
<%if (browser.contains("CHROME")){%>
<link rel="stylesheet" type="text/css" href="${resourcesPath}/css/cmr-chrome.css?${cmrv}" />
<%}%>

