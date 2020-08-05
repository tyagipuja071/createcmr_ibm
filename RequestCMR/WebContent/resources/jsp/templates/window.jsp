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
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<%
MessageUtil.checkMessages(request);
UIMgr.inject(request);
request.setAttribute("cmrv", SystemConfiguration.getSystemProperty("BUILD"));
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
<link rel="SHORTCUT ICON" href="//w3.ibm.com/favicon.ico" />
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

<title>${windowtitle}</title>
<script src="${resourcesPath}/js/w3.js" type="text/javascript">
  //
</script>

<%
AppUser user = AppUser.getUser(request);
%>
<script>
  dojo.config.parseOnLoad = true;
</script>
<script src="${resourcesPath}/js/cmr.js?${cmrv}" type="text/javascript"></script>
<script>
  cmr.CONTEXT_ROOT = '${contextPath}';
  cmr.MANDT = '<%=SystemConfiguration.getValue("MANDT")%>';
  cmr.NOSESSIONCHECK = true;
</script>
<script src="${resourcesPath}/js/cmr-message.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/cmr-validation.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/cmr-grid.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/cmr-dropdown.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/cmr-windows.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/facestypeahead-0.4.4.js"></script>
<script src="${resourcesPath}/js/moment.js" type="text/javascript"></script>
<link rel="stylesheet" type="text/css" href="//1.w3.s81c.com/common/v17e/css/w3.css" />
<link href="//1.www.s81c.com/common/v17e/css/form.css" rel="stylesheet" title="www" type="text/css" />
<link rel="stylesheet" href="${resourcesPath}/css/facestypeahead-0.4.4.css"/>

<!-- nihilo grid --> 
<link rel="stylesheet" type="text/css" href="//1.w3.s81c.com/common/js/dojo/1.6/dojox/grid/resources/nihiloGrid.css" />
<link rel="stylesheet" type="text/css" href="//1.w3.s81c.com/common/js/dojo/1.6/dijit/themes/nihilo/nihilo.css"/>

<!-- nihilo grid end -->
<link rel="stylesheet" type="text/css" href="//1.w3.s81c.com/common/js/dojo/1.6/dojox/grid/enhanced/resources/EnhancedGrid.css" />
<link rel="stylesheet" type="text/css" href="//1.w3.s81c.com/common/js/dojo/1.6/dojox/grid/enhanced/resources/EnhancedGrid_rtl.css" />

<link rel="stylesheet" type="text/css" href="${resourcesPath}/css/cmr.css?${cmrv}" />
<link rel="stylesheet" href="${resourcesPath}/css/ext/jquery-ui.css"/>
<script src="${resourcesPath}/js/ext/jquery-1.10.2.js"></script>
<script src="${resourcesPath}/js/ext/jquery-ui.js"></script>

<!-- typeahead, dates -->
<script>
  window.jQuery = null;
</script>
<style>
html,body {
  background: none;
}
div.cmr-window-content {
  margin-left: 10px;
  margin-right: 10px;
  margin-top: -5px;
  margin-bottm: 10px;
}

div.cmr-window-header {
  font-family: "HelveticaNeue-Bold","HelvBoldIBM","Arial,sans-serif";
  font-weight: 300;
}

#ibm-top {
  margin: 0 !important;
}

div.ibm-columns {
  width: 930px !important;
}
</style>
<jsp:include page="xbrowser.jsp" />
</head>
<body id="ibm-com" class="nihilo ibm-type" role="main" >

  <jsp:include page="/resources/jsp/templates/overlays.jsp" />

  <div id="ibm-loader-screen"></div>

  <div id="ibm-top" class="ibm-landing-page ibm-liquid">

<!-- MASTHEAD_BEGIN -->
<div id="ibm-masthead" style="display:none">
</div>
<!-- MASTHEAD_END -->


    <div id="ibm-pcon">
      <!-- CONTENT_BEGIN -->
      <div id="ibm-content">
        <div id="ibm-access-cntr"></div>

        <!-- CONTENT_BODY -->
        <div id="ibm-content-body">
          <div id="ibm-content-main">
            <tiles:insertAttribute name="body" />

            <!-- LEADSPACE_END -->
            
            
          </div>
        </div>
          <!-- CONTENT_BODY_END -->
      </div>
    </div>
  </div>
  <!-- CONTENT_END -->
</body>
<script>
WindowMgr.trackMe();
</script>
</html>