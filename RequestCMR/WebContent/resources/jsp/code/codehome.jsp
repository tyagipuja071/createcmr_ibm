<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<style>
  table.ibm-data-table th, table.ibm-data-table td, table.ibm-data-table a, .ibm-type table caption em {
    letter-spacing: 1px;
  }
  table.ibm-data-table td:NTH-CHILD(3) {
    font-size: 12px;
  }
  th.subhead {
    font-size: 12px;
    text-transform: uppercase;
  }
  div.code-filter {
    float:right;
    font-size: 12px;
    font-family: IBM Plex Sans, Calibri;  
    text-transform: uppercase;
    font-weight:bold;
  }
  div.code-filter input {
    font-size: 12px;
    font-family: IBM Plex Sans, Calibri;  
    margin-left: 4px;
  }
</style>
<div class="ibm-columns">
  <!-- Main Content -->
  <div class="ibm-col-1-1">
    <div id="wwq-content">

      <div class="ibm-columns">
        <div class="ibm-col-1-1" style="width: 915px">
          <div ng-app="CodeApp" ng-controller="CodeController">
          <table id="codeTable" cellspacing="0" cellpadding="0" border="0" summary="System Parameters" class="ibm-data-table ibm-sortable-table ibm-alternating">
            <caption>
              <em>
                Code Table Maintenance
                <div class="code-filter">
                  Filter:
                  <input ng-model="searchCode" style="width:200px" maxlength="15" placeholder="Any Text">
                </div>
              </em>
            </caption>
            <thead>
              <tr>
                <th scope="col" width="25%">Name</th>
                <th scope="col" width="*">Description</th>
                <th scope="col" width="20%">Table Name</th>
              </tr>
            </thead>
            <tbody>
            <%if (SystemConfiguration.isAdmin(request)){ %> 
              <tr ng-repeat-start="group in groups | groupFilter : searchCode">
                <th colspan="3">{{group.name}}</th>
              </tr>
              <tr ng-repeat="link in group.links | linkFilter : searchCode">
                <td><a style="cursor:pointer;font-size:13px" ng-click="goTo(link.href)" title="Go to {{link.name}}" ng-show="!link.subType">{{link.name}}</a></td>
                <th class="subhead" ng-show="link.subType">{{link.name}}</th>
                <td ng-show="!link.subType" style="font-size:13px">{{link.description}}</td>
                <td style="font-size:12px">{{link.table}}</td>
              </tr>
              <tr ng-repeat-end>
              </tr>
            <%} else {%>
              <tr ng-repeat-start="group in groups | groupFilter : searchCode" ng-show="group.cmde">
                <th colspan="3">{{group.name}}</th>
              </tr>
              <tr ng-repeat="link in group.links | linkFilter : searchCode" ng-show="group.cmde">
                <td><a style="cursor:pointer;font-size:13px" ng-click="goTo(link.href)" title="Go to {{link.name}}" ng-show="!link.subType">{{link.name}}</a></td>
                <th class="subhead" ng-show="link.subType" style="font-size:13px">{{link.name}}</th>
                <td ng-show="!link.subType" style="font-size:12px">{{link.description}}</td>
                <td>{{link.table}}</td>
              </tr>
              <tr ng-repeat-end>
              </tr>
            <%} %>
            </tbody>
          </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<script src="${resourcesPath}/js/system/codehome.js?${cmrv}"></script>
