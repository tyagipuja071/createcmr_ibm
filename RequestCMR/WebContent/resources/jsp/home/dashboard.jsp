<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  String id = (String) request.getParameter("configId");
  boolean newConfig = true;
  if (!StringUtils.isBlank(id)){
    newConfig = false;
  }
%>
<script src="${resourcesPath}/js/system/system.js?${cmrv}"></script>
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<style>
  img.proc-img {
    width: 11px;
    height:11px;
    cursor: help;
    vertical-align: sub;
  }
  table.rec-table {
    margin-left: 10px;
    font-size: 11px;
    background: #EEE;
  }
  table.rec-table td, table.rec-table th{
    font-size: 11px;
    vertical-align: top;
    text-align: left;
  }
  table.cntry-table td, table.cntry-table th{
    font-size: 11px;
    vertical-align: top;
    text-align: left;
  }
  div.tab-head {
    margin-top : 10px;
    font-weight: bold;
    font-size:11px;
    text-transform: uppercase;
    text-align: center;
    background: #EEE;
  }
  div.proc-head {
    text-align: center;
    text-transform: uppercase;
    font-weight: bold;
    color: white;
    text-shadow: none;
  }
  div.proc-g {
    background : green;
  }
  div.proc-o {
    background : orange;
  }
  div.proc-r {
    background : red;
  }
  div.filters {
    float:right;
  }
  input.filter {
    font-size: 11px;
    margin-right: 10px;
  }
  select.filter {
    font-size: 11px;
    width: 180px;
  }
  td.filter {
    padding-right: 5px;
    text-align: right !important;
  }
</style>
<div ng-app="DashboardApp" ng-controller="DashboardController" ng-cloak>
  <cmr:boxContent>
  <cmr:tabs />
  <cmr:section>
  <cmr:row>
    <cmr:column span="6">
      <h3>Dashboard <span ng-show="filterStatus"> - {{filterStatus}}</span></h3>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="1" width="280">
      <table width="100%" class="cntry-table">
        <tr>
          <td class="filter">Country:</td>
          <td> 
            <select class="filter" ng-model="filterCountry" placeholder="Filter by country">
              <option value=""></option>
              <option ng-repeat="fc in fCountries" value="{{fc.id}}">{{fc.name}}</option>
            </select>
          </td>
        </tr>
      </table>
    </cmr:column>
    <cmr:column span="1" width="280">
      <table width="100%" class="cntry-table">
        <tr>
          <td class="filter">Partner:</td>
          <td> 
            <select class="filter" ng-model="filterPartner" placeholder="Filter by partner">
              <option value=""></option>
              <option ng-repeat="fp in fPartners" value="{{fp.id}}">{{fp.name}}</option>
            </select>
          </td>
        </tr>
      </table>
    </cmr:column>
    <cmr:column span="1" width="280">
      <table width="100%" class="cntry-table">
        <tr>
          <td class="filter">Process:</td>
          <td> 
            <select class="filter" ng-model="filterProcess" placeholder="Filter by process">
              <option value=""></option>
              <option ng-repeat="fr in fProcess" value="{{fr.id}}">{{fr.name}}</option>
            </select>
          </td>
        </tr>
      </table>
    </cmr:column>
    <cmr:column span="1" width="180">
      <input type="button" class="filter" value="Filter Monitor" ng-click="extract()">
      <input type="button" class="filter" value="Check All" ng-click="checkAll()">
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="6">
      &nbsp;
    </cmr:column>
  </cmr:row>
  </cmr:section>
  </cmr:boxContent>
  
  <div ng-show="report">
  <cmr:boxContent>
  <cmr:tabs />

  <cmr:section>
  <cmr:row>
    <cmr:column span="6">
      <div ng-show="report" class="proc-head" ng-class="{'proc-g' : process.processingStatus == 'GREEN', 'proc-r' : process.processingStatus == 'RED', 'proc-o' : process.processingStatus == 'YELLOW'}">Processing Status - {{process.processingStatus}}</div>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="2" width="220">
    <div class="processing" ng-show="report">
      <table width="100%" class="cntry-table">
        <tr>
          <td width="50%">Threshold (counts):</td>
          <td>{{process.countsThreshold}}</td>
        </tr>
        <tr>
          <td>Threshold (mins):</td>
          <td>{{process.minsThreshold}}</td>
        </tr>
        <tr>
          <td>Pending Counts:</td>
          <td>{{totalPending}}</td>
        </tr>
      </table>
      <div class="tab-head">Stuck Processes</div>
      <table width="100%" class="cntry-table">
        <tr>
          <th>Process</th>
          <th>Counts</th>
        </tr>
        <tr ng-repeat="proc in stuckProcess">
          <td>{{proc}}</td>
          <td>{{process.stuckCounts[proc]}}</td> 
        </tr>
        <tr ng-show="stuckProcess.length == 0">
          <td colspan="2">No stuck processes</td>
        </tr>
      </table>
      <div class="tab-head">Country Requests</div>
      <div style="height:280px; overflow-y:auto">
      <table width="100%" class="cntry-table">
        <tr>
          <th>Country</th>
          <th>Counts</th>
        </tr>
        <tr ng-repeat="cntry in countries">
          <td>{{cntry}}</td>
          <td>{{process.pendingCounts[cntry]}}</td> 
        </tr>
        <tr ng-show="countries.length == 0">
          <td colspan="2">No pending requests</td>
        </tr>
      </table>
      </div>
    </div>
    </cmr:column>
    <cmr:column span="1" width="20">
      &nbsp;
    </cmr:column>
    <cmr:column span="4" width="780">
      <div ng-show="report" style="background:#EEE; height:450px; overflow-y:auto" >
      <div class="tab-head">Pending Requests</div>
      <div class="filters">
        <input class="filter" ng-model="processFilterText" placeholder="Input any text filter"> 
      </div>
      <table width="100%" cellspacing="0" cellpadding="0" margin="0" class="rec-table">
        <tr>
          <th>ID</th>
          <th>Status</th>
          <th>Type</th>
          <th>Country</th>
          <th>Customer</th>
          <th>Source</th>
          <th>Process</th>
          <th>Since</th>
          <th>State</th>
        </tr>
        <tr ng-show="report.processingRecords == 0">
          <td colspan="9">No pending requests</td>
        </tr>
        <tr ng-repeat="rec in report.processingRecords | filter:processFilterText">
          <td><a href="${contextPath}/request/{{rec.reqId}}" target="_blank">{{rec.reqId}}</a></td>
          <td>{{rec.reqStatus}} - {{rec.processBy}}</td>
          <td>{{rec.reqType}}</td>
          <td>{{rec.cmrIssuingCntry}}-{{rec.cntryNm}}</td>
          <td>
            <div ng-show="rec.custNm" title="{{rec.custNm}}" style="cursor:pointer">{{rec.custNm.length > 15 ? rec.custNm.substring(0,15)+'...' : rec.custNm}}</div>
            
          </td>
          <td>{{rec.sourceSystId}}</td>
          <td>
            {{rec.processingTyp}}
            <img src="${resourcesPath}/images/remove.png" class="proc-img" ng-show="rec.hostDown == 'Y'" title="Host System is down right now">
          </td>
          <td>
            <div ng-show="rec.obsolete">{{rec.lastUpdated}}</div>
            <div ng-show="!rec.obsolete">{{rec.pendingTime}}</div>
          </td>
          <td>
            <div ng-show="rec.obsolete" >Obsolete</div>
            <div ng-show="rec.stuck" style="color:orange">Stuck</div>
            <div ng-show="rec.manual && !rec.obsolete" style="color:brown">Manual</div>
            <div ng-show="!rec.obsolete && !rec.stuck && !rec.manual" style="color:green">In Queue</div>
          </td>
        </tr>
      </table>
      </div>
    </cmr:column>
  </cmr:row>
  </cmr:section>
  </cmr:boxContent>
  </div>

</div>

<script src="${resourcesPath}/js/home/dashboard.js?${cmrv}"></script>
