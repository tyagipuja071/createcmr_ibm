<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<%
  AppUser user = AppUser.getUser(request);
  boolean hasPref = user != null && user.isPreferencesSet();
%>
<ul id="ibm-navigation-trail">
  <%if (hasPref) {%>
    <li><a href="javascript: goToUrl('${contextPath}/home')">Home</a></li>
  <%}%>
  <c:if test="${primaryTabId ==  'HOME'}">
    <li><a href="#">Overview</a></li>
  </c:if>
  <c:if test="${primaryTabId ==  'ADMIN'}">
    <c:if test="${secondaryTabId ==  'SYS_CONFIG'}">
      <li><a href="#">System Configuration</a></li>
    </c:if>
    <c:if test="${secondaryTabId ==  'SYS_REFRESH'}">
      <li><a href="#">System Refresh</a></li>
    </c:if>
    <c:if test="${secondaryTabId ==  'FORCE_CHANGE'}">
      <li><a href="#">Forced Status Change</a></li>
    </c:if>
  </c:if>
  <c:if test="${primaryTabId ==  'WORKFLOW'}">
    <c:if test="${secondaryTabId ==  'OPEN_REQ'}">
      <li><a href="#">My Open Requests</a></li>
    </c:if>
    <c:if test="${secondaryTabId ==  'COMPLETED_REQ'}">
      <li><a href="#">My Completed Requests</a></li>
    </c:if>
    <c:if test="${secondaryTabId ==  'REJECTED_REQ'}">
      <li><a href="#">My Rejected Requests</a></li>
    </c:if>
    <c:if test="${secondaryTabId ==  'ALL_REQ'}">
      <li><a href="#">All My Requests</a></li>
    </c:if>
    <c:if test="${secondaryTabId ==  'SEARCH_REQUESTS'}">
      <li><a href="javascript: goToUrl('${contextPath}/workflow/search')">Search Requests</a></li>
    </c:if>
  </c:if>
  <c:if test="${primaryTabId ==  'REQUEST'}">
    <li><a href="#">Request Entry</a></li>
  </c:if>
</ul>
