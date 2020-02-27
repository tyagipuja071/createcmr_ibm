<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="contentPath" value="${contextPath}/resources"/>

<!-- MASTHEAD_BEGIN -->
<div id="ibm-masthead">
	<div id="ibm-mast-options">
		<ul>
			<li id="ibm-home"><a href="http://w3.ibm.com/">w3</a></li>
<%if (AppUser.isLoggedOn(request)){%>	 
			<li id="ibm-sso" class="ibm-profile-links-divider" style="display: none;">
				<span id="ibm-user-name">Welcome ${displayName}</span>
			</li>
			<li style="display:none"><a href="javascript: goToUrl('${contextPath}/logout')" id="cmr-sign-out" >Sign Out</a></li>	
<%}%>
		</ul>
	</div>
	

	<div id="ibm-universal-nav">
		<p id="ibm-site-title">
			<em>CreateCMR
				<span id="cmr-release-note" class="cmr-release-note">R<%=SystemConfiguration.getSystemProperty("RELEASE")%>.b<%=SystemConfiguration.getSystemProperty("BUILD")%> (<%=SystemConfiguration.getValue("SERVER_ALIAS")%>)</span>
				<span style="padding-left: 20% !important;"></span>
			</em>
		</p>
		
		<ul id="ibm-menu-links">
			<li><a href="http://w3.ibm.com/sitemap/us/en/">Site map</a>
			</li>
		</ul>
		<div id="ibm-search-module">
			<form id="ibm-search-form" action="http://w3.ibm.com/search/do/search" method="get">
				<p>
					<label for="q"><span class="ibm-access">Search</span></label>
					<input type="text" maxlength="100" value="" name="qt" id="q" />
					<input type="hidden" value="17" name="v" />
					<input value="en" type="hidden" name="langopt" />
					<input value="all" type="hidden" name="la" />
					<input type="submit" id="ibm-search" class="ibm-btn-search" value="Submit" />
				</p>
			</form>
		</div>	
	</div>
</div>
<!-- MASTHEAD_END -->