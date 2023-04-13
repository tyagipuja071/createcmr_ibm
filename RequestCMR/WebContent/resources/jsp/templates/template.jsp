<%@page import="com.ibm.cio.cmr.request.util.SystemParameters"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@page import="com.ibm.cio.cmr.request.util.MessageUtil"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%> 
<%@page import="com.ibm.cio.cmr.request.util.SystemUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles-extras" prefix="tilesx" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<%
MessageUtil.checkMessages(request);
UIMgr.inject(request);
request.setAttribute("cmrv", SystemConfiguration.getSystemProperty("BUILD"));
AppUser user = AppUser.getUser(request);
boolean approver = user != null && user.isApprover();
%>

<%-- Layout Tiles This layout create a html page with 
	<header>and<body>tags. 
	It renders a header, top menu, body and footer tile. @param title
    String use in page title @param header Header tile (jsp url or definition
    name) @param menu Menu @param body Body @param footer Footer --%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en-US" xml:lang="en-US">

<head>
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="schema.DC" href="//purl.org/DC/elements/1.0/" />
<link rel="icon" type="image/x-icon" href="${resourcesPath}/favicons.ico">
<meta name="DC.Rights" content="© Copyright IBM Corp. 2011" />
<meta name="DC.Date" scheme="iso8601" content="2011-07-12" />
<meta name="Source" content="v17 Template Generator, Template 17.02" />
<meta name="Security" content="Public" />
<meta name="Abstract" content="CMR Search" />
<meta name="IBM.Effective" scheme="W3CDTF" content="2011-07-12" />
<meta name="DC.Subject" scheme="IBM_SubjectTaxonomy"
	content="Application" />
<meta name="Robots" content="index,follow" />
<meta name="DC.Type" scheme="IBM_ContentClassTaxonomy"
	content="Web Application" />
<meta name="Description" content="CMR Search" />
<meta name="feedback" content="CMR Search Feedback" />
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" /> 

<title><tiles:getAsString name="title" /></title>
<script src="${resourcesPath}/js/w3.js?${cmrv}" type="text/javascript">
	//
</script>

<script>
	dojo.config.parseOnLoad = true;
</script>
<script src="${resourcesPath}/js/cmr.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/ci-supportal.js?${cmrv}" type="text/javascript"></script>
<script>
  cmr.CONTEXT_ROOT = '${contextPath}';
  cmr.MANDT = '<%=SystemConfiguration.getValue("MANDT")%>';
  CISupportal.init('<%=SystemParameters.getString("CI_SUPPORTAL_URL")%>', 'CreateCMR' <%=user != null ? ", '"+user.getIntranetId()+"'" : ""%>);
</script>
<script src="${resourcesPath}/js/cmr-message.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/cmr-validation.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/cmr-grid.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/cmr-dropdown.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/cmr-windows.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/facestypeahead-0.4.4.js"></script>
<script src="${resourcesPath}/js/moment.js" type="text/javascript"></script>
<link rel="stylesheet" href="${resourcesPath}/css/ext/w3.css?${cmrv}"/>
<link rel="stylesheet" href="${resourcesPath}/css/ext/form.css"/>
<link rel="stylesheet" href="${resourcesPath}/css/facestypeahead-0.4.4.css"/>

<!-- nihilo grid -->  
<link rel="stylesheet" type="text/css" href="https://1.w3.s81c.com/common/js/dojo/1.6/dojox/grid/resources/nihiloGrid.css" />
<link rel="stylesheet" type="text/css" href="https://1.w3.s81c.com/common/js/dojo/1.6/dijit/themes/nihilo/nihilo.css"/>

<!-- nihilo grid end -->
<link rel="stylesheet" type="text/css" href="https://1.w3.s81c.com/common/js/dojo/1.6/dojox/grid/enhanced/resources/EnhancedGrid.css" />
<link rel="stylesheet" type="text/css" href="https://1.w3.s81c.com/common/js/dojo/1.6/dojox/grid/enhanced/resources/EnhancedGrid_rtl.css" />
 
<link rel="stylesheet" type="text/css" href="${resourcesPath}/css/cmr.css?${cmrv}" />
<link rel="stylesheet" href="${resourcesPath}/css/ext/jquery-ui.css"/>
<script src="${resourcesPath}/js/ext/jquery-3.6.0.js"></script>
<script src="${resourcesPath}/js/ext/jquery-ui.js"></script>
<link rel="stylesheet" type="text/css" href="${resourcesPath}/css/cmr-v2.css?${cmrv}" />

<!-- typeahead, dates -->
<script>
  window.jQuery = null;
</script>
<style>
.ibm-spinner-large {
  background:
    url(${resourcesPath}/images/animated-progress-38x38c.webp)
    no-repeat center center transparent;
}
</style>
<jsp:include page="xbrowser.jsp" />
</head>
<body id="ibm-com" class="nihilo ibm-type" role="main" >
	<tilesx:useAttribute name="primaryTabId" ignore="true" />
	<tilesx:useAttribute name="secondaryTabId" ignore="true" />


	<!-- OVERLAYS --> 
	<jsp:include page="/resources/jsp/templates/overlays.jsp" />

	<div id="ibm-loader-screen"></div>
 
	<div id="ibm-top" class="ibm-landing-page ibm-liquid">
  <tiles:insertAttribute name="header" />

<%if (!AppUser.isLoggedOn(request)){%>	 
		<div id="ibm-leadspace-head" class="ibm-alternate ibm-no-tabs">
			<div id="ibm-leadspace-body">
				<div class="ibm-columns">
          <div class="ibm-col-1-1">
            &nbsp;
          </div>
					<div class="ibm-col-1-1" style="padding-bottom:20px;margin-left:100px;">
            <img src="${resourcesPath}/images/CreateCMRLogo.png" style="width:60px;height:60px;border-radius:8px;margin-right:10px">
						<h1 style="width:300px;display:inline-block;vertical-align:top">Login</h1>
					</div>
				</div>
			</div>
		</div>
		<div id="ibm-content-nav"></div>
<%} else {%>
		<div id="ibm-content-nav">
			<tiles:insertAttribute name="tabs" />
		</div>
<%}%>


		<div id="ibm-pcon">
			<!-- CONTENT_BEGIN -->
			<div id="ibm-content">
				<div id="ibm-access-cntr"></div>

				<!-- CONTENT_BODY -->
				<div id="ibm-content-body">
					<div id="ibm-content-main">
						<jsp:include page="messages.jsp" />
							<!-- LEADSPACE_BEGIN -->
						<tiles:putAttribute name="maintenance" value="/WEB-INF/content/jsp/layouts/common/maintenance.jsp" />
						<tiles:insertAttribute name="body" />

<%if (user != null) {%>
            <div id="quick_search_btn" style="top:260px" onclick="openQuickSearch()" class="cmr-feedback-btn cmr-quick-search-btn" 
              title="Easily search for existing CMRs or company information via D&B and create new requests directly from the results." >Quick Search</div>
<%} %>             
						<!--  The Export Form -->
						<iframe id="exportFrame" style="display:none" name="exportFrame"></iframe>

						<!-- LEADSPACE_END -->
						<div style="position:fixed;right:5px;bottom:5px;">
							<img src="${resourcesPath}/images/cedp.jpg" style = "width: 70px;height: 70px;">
					    </div>
						
					</div>
				</div>
					<!-- CONTENT_BODY_END -->
			</div>
		</div>
	</div>
	<!-- CONTENT_END -->
	<div>
	</div>
<%if (user != null && !user.isApprover()){%>
<jsp:include page="../system/newrequest.jsp" />
<%}%>
</body>
</html>