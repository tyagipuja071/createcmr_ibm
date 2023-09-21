<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.LovModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="${resourcesPath}/css/ext/data.css" rel="stylesheet" title="www" type="text/css" />
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>

<script>
  function backToCodeMaintHome() {
    window.location = cmr.CONTEXT_ROOT + '/searchhome';
  }
  dojo.addOnLoad(function() {
  });
</script>
<style>
 table.legacy-crit th {
   text-align: right;
   padding-right: 3px;
 }
 table.legacy-crit tr {
   border-top : none !important;
 }
 table.ibm-data-table tbody td, table.ibm-data-table thead th {
   font-size: 12px;
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
 div.checkinput {
   display: inline-block;
   font-size: 11px;
   text-transform: uppercase;
   color: #555;
   margin-right: 10px;
 }
 input[type="date"]{
   font-family: Tahoma;
 }
 div.cancel {
   border: 1px Solid Red;
   color: Red;
   border-radius: 3px;
   height: 15px;
   width: 50px;
   font-size: 10px;
   line-height: 15px;
   text-align: center;
   font-weight: bold;
 }
 .btn-search, .btn-reset {
   width: 100px;
   font-size: 13px;
   height: 30px;
 }
</style>

<div ng-app="CrisReportApp" ng-controller="CrisReportController" ng-cloak>
  <cmr:boxContent>
    <cmr:tabs />

    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Japan Report for CRIS User</h3>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <table cellspacing="0" cellpadding="0" border="0" summary="Info"
          class="ibm-data-table ibm-sortable-table ibm-alternating legacy-crit">
          <thead>
            <tr>
              <th scope="col" width="20%"><span style="color:red">*</span>Timeframe:</th>
              <td>
                <select ng-model="timeframe">
                  <!-- <option value="RAONDEMAND">RA On-Demand</option> -->
                  <option value="TAIGADAILY">TAIGA Daily</option>
                  <option value="TAIGAMONTHLY">TAIGA Monthly</option>
                  <option value="ROLDAILY">ROL Daily</option>
                  <option value="ROLMONTHLY">ROL Monthly</option>
                </select>
              </td>
            </tr>
            <tr>
              <td colspan="2">
                <input type="button" class="btn-search" value="Get Report" style="margin-left:10px" ng-click="getReport()">
                </th>
            </tr>
          </thead>
        </table>
      </cmr:row>

    </cmr:section>
  </cmr:boxContent>
  <cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <input style="height:40px" type="button" class="ibm-btn-cancel-sec ibm-btn-small" value="Back to Search Home"
        onclick="backToCodeMaintHome()">
    </cmr:buttonsRow>
    <br>
  </cmr:section>
</div>

<script src="${resourcesPath}/js/legacy/crisreport.js?${cmrv}"></script>
