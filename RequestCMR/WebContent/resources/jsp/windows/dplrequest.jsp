  <%@page import="com.ibm.cio.cmr.request.model.automation.DuplicateCheckModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<link href="${resourcesPath}/css/ext/data.css" rel="stylesheet" title="www" type="text/css" />
<link rel="stylesheet" href="${resourcesPath}/css/quick_search.css?${cmrv}"/>

<style> 
td.dnb-label {
  text-align: right;
  padding-right: 3px;
  font-weight: bold !important;
}

img.exp-col {
  width: 15px;
  height: 15px;
  cursor: pointer;
  vertical-align: sub;
}
div.ibm-columns {
  width:95%;
}
 div.use-m, div.use-b, div.use-i, div.use-s, div.use-e, div.use-l, div.use-gen {
   width:12px; 
   display: inline-block;
   border: 1px Solid #666;
   border-radius: 4px;
   padding-left: 2px;
   padding-right: 2px;
   margin-right: 2px;
   font-weight: bold;
   text-align: center;
   height: 18px;
   line-height: 18px;
   cursor: help;
   font-size: 10px;
 }
 div.use-m {
   background: rgb(255,171,87)
 }
 div.use-b {
   background: rgb(60,191,255)
 }
  div.use-i {
   background: rgb(128,255,128)
 }
 div.use-s {
   background: rgb(170,170,255)
 }
 div.use-e {
   background: rgb(255,174,174)
 }
 div.use-l {
   background: rgb(255,255,128)
 }
 div.use-gen {
   background: rgb(215,215,215)
 }

td.inner-det {
  border: none !important;
  padding: 2px !important;
}
span.bold {
  font-weight: bold;
}

 .btn-search, .btn-reset {
   min-width: 100px;
   font-size: 13px;
   height: 30px;
 }
</style>

<cmr:window>

  <jsp:include page="../dpl/dplcommon.jsp"></jsp:include>

  <cmr:row>
  </cmr:row>
  <cmr:windowClose />
<script src="${resourcesPath}/js/dpl/dplsearch.js?${cmrv}"
  type="text/javascript"></script>
</cmr:window>
