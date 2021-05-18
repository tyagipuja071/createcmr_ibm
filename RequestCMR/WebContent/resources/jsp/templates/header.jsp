<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources"/>

<style>
  img.ibm-logo {
    width: 50px;
    vertical-align: sub;
  }
</style>
<!-- MASTHEAD_BEGIN -->
<%if (AppUser.isLoggedOn(request)){%>	 
  <div id="v2-logon">
		<span id="ibm-user-name">Welcome ${displayName}</span>
		<a href="javascript: goToUrl('${contextPath}/logout')" id="cmr-sign-out" >Sign Out</a>	
  </div>
<%}%>
	

	<div id="v2-title">
      <img class="ibm-logo" src="${resourcesPath}/images/ibm-logo.png"/>
			<span class="v2-name">CreateCMR</span>
	 	  <span id="cmr-release-note" class="cmr-release-note">R<%=SystemConfiguration.getSystemProperty("RELEASE")%>.b-<%=SystemConfiguration.getSystemProperty("PREFIX")%><%=SystemConfiguration.getSystemProperty("BUILD")%> (<%=SystemConfiguration.getValue("SERVER_ALIAS")%>)</span>
			<span style="padding-left: 20% !important;"></span>
	</div>
<!-- MASTHEAD_END -->