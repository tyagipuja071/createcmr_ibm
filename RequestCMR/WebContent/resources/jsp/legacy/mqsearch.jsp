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
<div ng-app="QueryApp" ng-controller="QueryController" ng-cloak>
  <cmr:boxContent>
    <cmr:tabs />

    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>SOF/WTAAS Search</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <table cellspacing="0" cellpadding="0" border="0" summary="Info"
          class="ibm-data-table ibm-sortable-table ibm-alternating legacy-crit">
          <thead>
            <tr>
              <th scope="col" width="20%"><span style="color:red">*</span> CMR Issuing Country: </th>
              <td width="30%">
                <select ng-model="country" value=""  style="width:252px">
                  <option value=""></option>
                  <option ng-repeat="country in countries"  value="{{country.id}}" >{{country.id}} - {{country.name}}</option>
                </select>
              </td>
              <th scope="col" width="8%"><span style="color:red">*</span> CMR No.:</th>
              <td width="42%">
                <input ng-model="cmrNo" value=""  style="width:90px" maxlength="6">
              </td>
            </tr>
            <tr>
              <td colspan="4">
                <input type="button" class="btn-search" value="Search" style="margin-left:10px" ng-click="search()"> 
                <input type="button" class="btn-reset" value="Reset" style="margin-left:10px" onclick="window.location.href = 'mqsearch'"> 
              </th>
            </tr>
          </thead>
          <tbody>
            <tr>
            </tr>
          </tbody>
        </table>
      </cmr:row>
      <div ng-show="loaded">
        <table cellspacing="0" cellpadding="0" border="0" summary="Results" class="ibm-data-table ibm-sortable-table ibm-alternating" ng-show="system == 'SOF'">
          <caption>
            <em>Search Results
            </em>
          </caption>
          <tbody>
            <tr>
              <td colspan="2"><strong>Data Elements</strong></td>
            </tr>
            <tr ng-repeat="data in dataElems">
              <td width="20%"> 
                {{data.name}}
              </td>
              <td width="80%">
                {{data.value}}
              </td>
            </tr>
            <tr>
              <td colspan="2"><strong>Addresses</strong></td>
            </tr>
            <tr ng-repeat="addr in addresses">
              <td> 
                {{addr.type}} ({{addr.seqNo}})
              </td>
              <td>
                <table cellspacing="0" cellpadding="0" border="0" summary="Results" class="ibm-data-table ibm-sortable-table ibm-alternating">
                  <tbody>
                    <tr ng-repeat="att in addr.atts">
                      <td width="20%">{{att.name}}</td>
                      <td width="80%">{{att.value}}</td>
                    </tr>
                  </tbody>
                </table>
              </td>
            </tr>
          </tbody>
        </table>
        
        <table cellspacing="0" cellpadding="0" border="0" summary="Results" class="ibm-data-table ibm-sortable-table ibm-alternating" ng-show="system == 'WTAAS'">
          <caption>
            <em>Search Results
            </em>
          </caption>
          <tbody>
            <tr>
              <td colspan="2"><strong>Data Elements</strong></td>
            </tr>
            <tr ng-repeat="data in dataElems">
              <td width="20%"> 
                {{data.name}}
              </td>
              <td width="80%">
                {{data.value}}
              </td>
            </tr>
            <tr>
              <td colspan="2"><strong>Addresses</strong></td>
            </tr>
            <tr ng-repeat="addr in addresses">
              <td> 
                <strong>Address No.:</strong> {{addr.seqNo}} <br>
                <strong>Address Uses:</strong> {{addr.type}} 
              </td>
              <td>
                <table cellspacing="0" cellpadding="0" border="0" summary="Results" class="ibm-data-table ibm-sortable-table ibm-alternating">
                  <tbody>
                    <tr ng-repeat="att in addr.atts">
                      <td width="20%">{{att.name}}</td>
                      <td width="80%">{{att.value}}</td>
                    </tr>
                  </tbody>
                </table>
              </td>
            </tr>
          </tbody>
        </table>
        
      </div>


    </cmr:section>
  </cmr:boxContent>
  <cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <input style="height:40px" type="button" class="ibm-btn-cancel-sec ibm-btn-small" value="Back to Search Home" onclick="backToCodeMaintHome()">
    </cmr:buttonsRow>
    <br>
  </cmr:section>
</div>
<script src="${resourcesPath}/js/legacy/mqsearch.js?${cmrv}"></script>
