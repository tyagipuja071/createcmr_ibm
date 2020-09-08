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
    window.location = cmr.CONTEXT_ROOT + '/code';
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
<div ng-app="LegacySearchApp" ng-controller="LegacySearchController" ng-cloak>
  <cmr:boxContent>
    <cmr:tabs />

    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>DB2 Records Search</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <table cellspacing="0" cellpadding="0" border="0" summary="Info"
          class="ibm-data-table ibm-sortable-table ibm-alternating legacy-crit">
          <thead>
            <tr>
              <th scope="col" width="20%"><span style="color:red">*</span> CMR Issuing Country: </th>
              <td width="30%">
                <select ng-model="crit.realCtyCd" value=""  style="width:252px">
                  <option value=""></option>
                  <option ng-repeat="country in countries"  value="{{country.id}}" >{{country.id}} - {{country.name}}</option>
                </select>
              </td>
              <th scope="col" width="20%">CMR No.:</th>
              <td width="30%">
                <input ng-model="crit.customerNo" value=""  style="width:90px" maxlength="6">
              </td>
            </tr>
            <tr>
              <th scope="col">Customer Name:</th>
              <td >
                <input ng-model="crit.name" value=""  style="width:252px" maxlength="60">
              </td>
              <th scope="col">Abbreviated Name:</th>
              <td>
                <input ng-model="crit.abbrevNm" style="width:200px" maxlength="24">
              </td>
            </tr>
            <tr>
              <th scope="col">Street:</th>
              <td >
                <input ng-model="crit.street" value=""  style="width:252px" maxlength="60">
              </td>
              <th scope="col">City:</th>
              <td>
                <input ng-model="crit.city" style="width:120px" maxlength="30">
              </td>
            </tr>
            <tr>
              <th scope="col">Postal Code:</th>
              <td >
                <input ng-model="crit.zipCode" value=""  style="width:60px" maxlength="10">
              </td>
              <th scope="col">VAT:</th>
              <td>
                <input ng-model="crit.vat" style="width:120px" maxlength="30">
              </td>
            </tr>
            <tr>
              <th scope="col">Address Use:</th>
              <td colspan="3">
                
                <div class="checkinput">
                  <input ng-model="crit.addressUseM" ng-true-value="'M'" value="M" type="checkbox">
                  Mailing
                </div>
                <div class="checkinput">
                  <input ng-model="crit.addressUseB" ng-true-value="'B'" value="B" type="checkbox">
                  Billing
                </div>
                <div class="checkinput">
                  <input ng-model="crit.addressUseI" ng-true-value="'I'" value="I" type="checkbox">
                  Installing
                </div>
                <div class="checkinput">
                  <input ng-model="crit.addressUseS" ng-true-value="'S'" value="S" type="checkbox">
                  Shipping
                </div>
                <div class="checkinput">
                  <input ng-model="crit.addressUseE" ng-true-value="'E'" value="E" type="checkbox">
                  EPL
                </div>
                <div class="checkinput">
                  <input ng-model="crit.addressUseF" ng-true-value="'F'" value="F" type="checkbox">
                  F Address
                </div>
                <div class="checkinput">
                  <input ng-model="crit.addressUseG" ng-true-value="'G'" value="G" type="checkbox">
                  G Address
                </div>
                <div class="checkinput">
                  <input ng-model="crit.addressUseH" ng-true-value="'H'" value="H" type="checkbox">
                  H Address
                </div>
              </td>
            </tr>
            <tr>
              <th scope="col">Create Date:</th>
              <td>
                <input type="date" ng-model="crit.createTsFrom">
                <span class="to">to</span>
                <input type="date" ng-model="crit.createTsTo">
              </td>
              <th scope="col">CMR Status:</th>
              <td>
                <select ng-model="crit.status">
                  <option value="A">Active Only</option>
                  <option value="C">Inactive Only</option>
                  <option value="*">All</option>
                </select>
              </td>
            </tr> 
            <tr>
              <th scope="col">Update Date:</th>
              <td>
                <input type="date" ng-model="crit.updateTsFrom">
                <span class="to">to</span>
                <input type="date" ng-model="crit.updateTsTo">
              </td>
              <th scope="col">Result Count:</th>
              <td>
                <input ng-model="crit.recCount" type="radio" name="recCount" value="50">50
                <input ng-model="crit.recCount" type="radio" name="recCount" value="100">100
                <input ng-model="crit.recCount" type="radio" name="recCount" value="200">200
              </td>
            </tr>
            <tr>
              <td colspan="4">
                <input type="button" class="btn-search" value="Search" style="margin-left:10px" ng-click="search()"> 
                <input type="button" class="btn-reset" value="Reset" style="margin-left:10px" onclick="window.location.href = 'legacysearch'"> 
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
        <table cellspacing="0" cellpadding="0" border="0" summary="System Parameters" class="ibm-data-table ibm-sortable-table ibm-alternating">
          <caption>
            <em>Search Results
              <div class="filter" style="display:inline-block;float:right;">
                <input ng-model="allTextFilter" placeholder="Type to filter">
              </div>
            </em>
          </caption>
          <thead>
            <tr>
              <th scope="col" width="7%">Country</th>
              <th scope="col" width="8%">CMR No.</th>
              <th scope="col" width="10%">Addr Use</th>
              <th scope="col" width="7%">Addr No.</th>
              <th scope="col" width="*">Address Lines</th>
              <th scope="col" width="11%">VAT</th>
              <th scope="col" width="7%">ISIC</th>
              <th scope="col" width="7%">ISU</th>
              <th scope="col" width="9%">SBO</th>
              <th scope="col" width="9%">SR No.</th>
            </tr>
          </thead>
          <tbody>
            <tr ng-repeat="rec in results | textFilter:allTextFilter">
              <td>{{rec.realCtyCd}}</td>
              <td>
                <a ng-click="openDetails(rec)" style="cursor:pointer" class="legacy-det-link" title="Open details of CMR No. {{rec.customerNo}}">
                  {{rec.customerNo}}
                </a>
                <div ng-show="rec.status == 'C'" class="cancel">
                  INACTIVE
                </div>
              </td>
              <td ng-bind-html="formatAddrUse(rec)">
              </td>
              <td>{{rec.addrNo}}</td>
              <td>
                {{rec.addrLine1}}
                <br>
                {{rec.addrLine2}}
                <br>
                {{rec.addrLine3}}
                <br>
                {{rec.addrLine4}}
                <br>
                {{rec.addrLine5}}
                <br>
                {{rec.addrLine6}}
              </td>
              <td>{{rec.vat}}</td>
              <td>{{rec.isicCd}}</td>
              <td>{{rec.isuCd}}</td>
              <td>{{rec.sbo}}</td>
              <td>{{rec.salesRepNo}}</td>
            </tr>
          </tbody>
        </table>
      </div>


    </cmr:section>
  </cmr:boxContent>
  <cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <input style="height:40px" type="button" class="ibm-btn-cancel-sec ibm-btn-small" value="Back to Code Maintenance Home" onclick="backToCodeMaintHome()">
    </cmr:buttonsRow>
    <br>
  </cmr:section>
</div>
<script src="${resourcesPath}/js/legacy/legacysearch.js?${cmrv}"></script>
