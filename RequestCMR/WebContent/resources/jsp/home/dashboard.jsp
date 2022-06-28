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
</style>
<div ng-app="DashboardApp" ng-controller="DashboardController" ng-cloak>
  <cmr:boxContent>
  <cmr:tabs />
  <cmr:section>
  <cmr:row>
    <cmr:column span="6">
      <h3>Dashboard</h3>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="6">
      <div class="proc-head" ng-class="{'proc-g' : process.processingStatus == 'GREEN', 'proc-r' : process.processingStatus == 'RED', 'proc-o' : process.processingStatus == 'YELLOW'}">Processing Status - {{process.processingStatus}}</div>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="2" width="220">
    <div class="processing" ng-show="report">
      <table width="100%" class="cntry-table">
        <tr>
          <td>Threshold (counts):</td>
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
      </table>
      <div class="tab-head">Country Requests</div>
      <div style="height:350px; overflow-y:auto">
      <table width="100%" class="cntry-table">
        <tr>
          <th>Country</th>
          <th>Counts</th>
        </tr>
        <tr ng-repeat="cntry in countries">
          <td>{{cntry}}</td>
          <td>{{process.pendingCounts[cntry]}}</td> 
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
        <tr ng-repeat="rec in report.processingRecords | filter:processFilterText">
          <td><a href="${contextPath}/request/{{rec.reqId}}" target="_blank">{{rec.reqId}}</a></td>
          <td>{{rec.reqStatus}} - {{rec.processBy}}</td>
          <td>{{rec.reqType}}</td>
          <td>{{rec.cmrIssuingCntry}}-{{rec.cntryNm}}</td>
          <td>
            <div ng-show="rec.custNm">{{rec.custNm.length > 15 ? rec.custNm.substring(0,15)+'...' : rec.custNm}}</div>
            
          </td>
          <td>{{rec.sourceSystId}}</td>
          <td>
            {{rec.processingTyp}}
            <img src="${resourcesPath}/images/remove.png" class="proc-img" ng-show="rec.hostDown == 'Y'">
          </td>
          <td>
            <div ng-show="rec.obsolete">{{rec.lastUpdated}}</div>
            <div ng-show="!rec.obsolete">{{rec.pendingTime}}</div>
          </td>
          <td>
            <div ng-show="rec.obsolete" >Obsolete</div>
            <div ng-show="rec.stuck" style="color:orange">Stuck</div>
            <div ng-show="!rec.obsolete && !rec.stuck" style="color:green">In Queue</div>
          </td>
        </tr>
      </table>
      </div>
    </cmr:column>
  </cmr:row>
  </cmr:section>
  </cmr:boxContent>

</div>

<script src="${resourcesPath}/js/home/dashboard.js?${cmrv}"></script>
