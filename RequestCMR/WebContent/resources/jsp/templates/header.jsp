<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="contentPath" value="${contextPath}/resources"/>

<!-- MASTHEAD_BEGIN -->
<%if (AppUser.isLoggedOn(request)){%>	 
  <div id="v2-logon">
		<span id="ibm-user-name">Welcome ${displayName}</span>
		<a href="javascript: goToUrl('${contextPath}/logout')" id="cmr-sign-out" >Sign Out</a>	
  </div>
<%}%>
	

	<div id="v2-title">
			CreateCMR
	 	  <span id="cmr-release-note" class="cmr-release-note">R<%=SystemConfiguration.getSystemProperty("RELEASE")%>.b-<%=SystemConfiguration.getSystemProperty("PREFIX")%><%=SystemConfiguration.getSystemProperty("BUILD")%> (<%=SystemConfiguration.getValue("SERVER_ALIAS")%>)</span>
			<span style="padding-left: 20% !important;"></span>
	</div>
<!-- MASTHEAD_END -->