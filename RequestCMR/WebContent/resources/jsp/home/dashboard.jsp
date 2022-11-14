<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
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
  img.proc-img-m {
    width: 15px;
    height:15px;
    cursor: help;
    vertical-align: sub;
  }
  table.rec-table {
    margin-left: 10px;
    font-size: 11px;
    background: #EEE;
  }
  table.rec-table-h {
    margin-left: 10px;
    font-size: 11px;
    background: #FFF;
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
  div.alert {
    background: #eaefb5; /* Old browsers */
    background: -moz-linear-gradient(top,  #eaefb5 0%, #e1e9a0 100%); /* FF3.6-15 */
    background: -webkit-linear-gradient(top,  #eaefb5 0%,#e1e9a0 100%); /* Chrome10-25,Safari5.1-6 */
    background: linear-gradient(to bottom,  #eaefb5 0%,#e1e9a0 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
    filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#eaefb5', endColorstr='#e1e9a0',GradientType=0 ); /* IE6-9 */
    font-size: 14px;
    font-weight: bold;
    text-shadow: none;
    padding-left: 10px;
  }
  img.health {
    width: 70px;
    height: 70px;
    cursor: help;
  }
  div.health {
    text-align: center;
    font-size:13px;
    font-weight: bold;
    padding-bottom:5px;
  }
</style>
<div ng-app="DashboardApp" ng-controller="DashboardController" ng-cloak>
  <cmr:boxContent>
  <cmr:tabs />
  <cmr:section>
  <cmr:row addBackground="true">
    <cmr:column span="6">
      <h3><span ng-show="filterStatus">{{filterStatus}}</span></h3>
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="5">
    <cmr:column span="1" width="280">
      <table width="100%" class="cntry-table">
        <tr>
          <td class="filter">Country:</td>
          <td> 
            <select class="filter" ng-model="filterCountry" placeholder="Filter by country">
              <option value=""></option>
              <option ng-repeat="fc in fCountries" value="{{fc.id}}">{{fc.name}}</option>
            </select>
            
            <div style="padding-top:10px">
              <input type="button" class="filter" value="Filter Monitor" ng-click="extract()">
              <input type="button" class="filter" value="Check All" ng-click="checkAll()">
            </div>

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
    <cmr:column span="1" width="170">
      <div class="health">
        OVERALL STATUS
        <img src="${resourcesPath}/images/check.png" ng-show="report.overallStatus == 'GREEN'" class="health" title="System is healthy">
        <img src="${resourcesPath}/images/warning.png" ng-show="report.overallStatus == 'RED'" class="health" title="One or more components need to be checked urgently">
        <img src="${resourcesPath}/images/yellow.png" ng-show="report.overallStatus == 'ORANGE'" class="health" title="Components need to be further monitored in the next hours">
        <img src="${resourcesPath}/images/loading.gif" ng-show="!report" class="health" title="System is healthy">
      </div>
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
      <div ng-show="report" class="proc-head" ng-class="{'proc-g' : services.servicesStatus == 'GREEN', 'proc-r' : services.servicesStatus == 'RED', 'proc-o' : services.servicesStatus == 'ORANGE'}">Connections Status - {{services.servicesStatus}}</div>
      <div ng-show="services.alert" class="alert">{{services.alert}}</div>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="6">
      <div ng-show="report">
      <div class="tab-head">Application Connections</div>
      <table width="100%" cellspacing="0" cellpadding="0" margin="0" class="rec-table rec-table-h">
        <tr>
          <th>Component</th>
          <th>Classification</th>
          <th>Usage</th>
          <th>Status</th>
          <th>Check URL</th>
        </tr>
        <tr>
          <td>FindCMR</td>
          <td>Primary</td>
          <td>Used to search for CMRs for in updating CMRs</td>
          <td style="text-align:center">
            <img src="${resourcesPath}/images/approve.png" class="proc-img-m" ng-show="services.findCmr" title="Connection is successful">
            <img src="${resourcesPath}/images/reject.png" class="proc-img-m" ng-show="!services.findCmr" title="Connection is DOWN">
          </td>
          <td>
            <a href="<%=SystemConfiguration.getValue("FIND_CMR_URL")+"/getCMRData"%>" target="_blank">Web Service URL</a>
          </td>
        </tr>
        <tr>
          <td>CMR Services</td>
          <td>Primary</td>
          <td>Contains all external services and RDC processes used by CreateCMR</td>
          <td style="text-align:center">
            <img src="${resourcesPath}/images/approve.png" class="proc-img-m" ng-show="services.cmrServices" title="Connection is successful">
            <img src="${resourcesPath}/images/reject.png" class="proc-img-m" ng-show="!services.cmrServices" title="Connection is DOWN">
          </td>
          <td>
            <a href="<%=SystemConfiguration.getValue("CMR_SERVICES_URL")+"/cmrs/ping"%>" target="_blank">Ping URL</a>
          </td>
        </tr>
        <tr>
          <td>CI Services</td>
          <td>Primary</td>
          <td>Connects to the FindCMR Elastic Search index for searching</td>
          <td style="text-align:center">
            <img src="${resourcesPath}/images/approve.png" class="proc-img-m" ng-show="services.ciServices" title="Connection is successful">
            <img src="${resourcesPath}/images/reject.png" class="proc-img-m" ng-show="!services.ciServices" title="Connection is DOWN">
          </td>
          <td>
            <a href="<%=SystemConfiguration.getValue("BATCH_CI_SERVICES_URL")+"/service/ping"%>" target="_blank">Ping URL</a>
          </td>
        </tr>
          <tr>
          <td>US CMR (US Legacy)</td>
          <td>Secondary</td>
          <td>Retrieves US CMR values and primary point of creation and updates</td>
          <td style="text-align:center">
            <img src="${resourcesPath}/images/approve.png" class="proc-img-m" ng-show="services.usCmr" title="Connection is successful">
            <img src="${resourcesPath}/images/reject.png" class="proc-img-m" ng-show="!services.usCmr" title="Connection is DOWN">
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td>CRIS (Japan Legacy)</td>
          <td>Secondary</td>
          <td>Retrieves Japan CMR values and primary point of creation and updates</td>
          <td style="text-align:center">
            <img src="${resourcesPath}/images/approve.png" class="proc-img-m" ng-show="services.cris" title="Connection is successful">
            <img src="${resourcesPath}/images/reject.png" class="proc-img-m" ng-show="!services.cris" title="Connection is DOWN">
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td>MQ (WTAAS AP)</td>
          <td>Secondary</td>
          <td>Retrieves AP CMR values and primary point of creation and updates</td>
          <td style="text-align:center">
            <img src="${resourcesPath}/images/approve.png" class="proc-img-m" ng-show="services.mq" title="Connection is successful">
            <img src="${resourcesPath}/images/reject.png" class="proc-img-m" ng-show="!services.mq" title="Connection is DOWN">
          </td>
          <td>&nbsp;</td>
        </tr>
      </table>
      </div>
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
      <div ng-show="report" class="proc-head" ng-class="{'proc-g' : process.processingStatus == 'GREEN', 'proc-r' : process.processingStatus == 'RED', 'proc-o' : process.processingStatus == 'ORANGE'}">Processing Status - {{process.processingStatus}}</div>
      <div ng-show="process.alert" class="alert">{{process.alert}}</div>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="2" width="220">
    <div class="processing" ng-show="report">
      <table width="100%" class="cntry-table">
        <tr>
          <td width="50%">Threshold (stuck):</td>
          <td>{{process.countsThreshold}}</td>
        </tr>
        <tr>
          <td>Threshold (mins):</td>
          <td>{{process.minsThreshold}}</td>
        </tr>
        <tr>
          <td>Threshold (errors):</td>
          <td>{{process.errorThreshold}}</td>
        </tr>
        <tr>
          <td>Error Counts:</td>
          <td><strong>{{totalErrors}}</strong></td>
        </tr>
        <tr>
          <td>Pending Counts:</td>
          <td><strong>{{totalPending}}</strong></td>
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
          <td>
            {{process.stuckCounts[proc]}}
          </td> 
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
          <th>Counts / Err</th>
        </tr>
        <tr ng-repeat="cntry in countries">
          <td>{{cntry}}</td>
          <td>{{process.pendingCounts[cntry]}} / 
          <span style="color:red" ng-show="process.errorCounts[cntry] > 0">{{process.errorCounts[cntry]}}</span>
          <span ng-show="process.errorCounts[cntry] == 0">0</span>
          </td> 
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
      <div ng-show="report" style="background:#EEE; height:450px; overflow:auto" >
      <div class="tab-head">Pending Requests</div>
      <div class="filters">
        <input class="filter" ng-model="processFilterText" placeholder="Input any text filter"> 
      </div>
      <table width="100%" cellspacing="0" cellpadding="0" margin="0" class="rec-table" style="width:900px">
        <tr>
          <th>ID</th>
          <th>Status</th>
          <th>Type</th>
          <th>Country</th>
          <th>Customer</th>
          <th>Since</th>
          <th>State</th>
          <th>Errors</th>
          <th>Process</th>
          <th>Source</th>
        </tr>
        <tr ng-show="report.processingRecords == 0">
          <td colspan="9">No pending requests</td>
        </tr>
        <tr ng-repeat="rec in report.processingRecords | filter:processFilterText">
          <td><a href="${contextPath}/request/{{rec.reqId}}" target="_blank">{{rec.reqId}}</a></td>
          <td>{{rec.statusDesc}} {{rec.processBy ? '(' + rec.processBy + ')' : ''}}</td>
          <td>{{rec.reqType}}</td>
          <td>{{rec.cmrIssuingCntry}}-{{rec.cntryNm}}</td>
          <td>
            <div ng-show="rec.custNm" title="{{rec.custNm}}" style="cursor:pointer">{{rec.custNm.length > 15 ? rec.custNm.substring(0,15)+'...' : rec.custNm}}</div>
            
          </td>
          <td>
            <div ng-show="rec.obsolete">{{rec.lastUpdated}}</div>
            <div ng-show="!rec.obsolete">{{rec.pendingTime}}</div>
          </td>
          <td>
            <div ng-show="rec.reqStatus == 'COM'">Complete</div>
            <div ng-show="rec.reqStatus != 'COM'">
              <div ng-show="rec.obsolete" style="color:coral">Obsolete</div>
              <div ng-show="rec.stuck" style="color:orange">Stuck</div>
              <div ng-show="rec.manual && !rec.obsolete" style="color:brown">Manual</div>
              <div ng-show="!rec.obsolete && !rec.stuck && !rec.manual" style="color:green">In Queue</div>
            </div>
          </td>
          <td style="text-align:center">
            <img src="${resourcesPath}/images/error-icon.png" class="proc-img" ng-show="rec.reqStatus == 'COM' && (rec.processedFlag == 'E' || rec.rdcProcessingStatus == 'A' || rec.rdcProcessingStatus == 'N')"
            title="Errors in Legacy/DB2 and RDC were encountered. Please check request summary.">
          </td>
          <td>
            {{rec.processingTyp}}
            <img src="${resourcesPath}/images/remove.png" class="proc-img" ng-show="rec.hostDown == 'Y'" title="Host System is down right now">
          </td>
          <td>{{rec.sourceSystId}}</td>
        </tr>
      </table>
      </div>
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
      <div ng-show="report" class="proc-head" ng-class="{'proc-g' : automation.automationStatus == 'GREEN', 'proc-r' : automation.automationStatus == 'RED', 'proc-o' : automation.automationStatus == 'ORANGE'}">Automation Status - {{automation.automationStatus}}</div>
      <div ng-show="automation.alert" class="alert">{{automation.alert}}</div>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="2" width="220">
    <div class="processing" ng-show="report">
      <table width="100%" class="cntry-table">
        <tr>
          <td width="50%">Threshold (pending):</td>
          <td>{{automation.pendingThreshold}} requests</td>
        </tr>
        <tr>
          <td>Threshold (validation):</td>
          <td>{{automation.processTimeThreshold}}m</td>
        </tr>
        <tr>
          <td>Threshold (review %):</td>
          <td>{{automation.manualPctThreshold}}%</td>
        </tr>
        <tr>
          <td>Threshold (completion TAT):</td>
          <td>{{automation.tatThreshold}}m</td>
        </tr>
        <tr>
          <td>All Requests:</td>
          <td>{{automation.totalRecords}}</td>
        </tr>
        <tr>
          <td>Pending Automation:</td>
          <td><strong>{{automation.allPending}}</strong></td>
        </tr>
      </table>
    </div>
    </cmr:column>
    <cmr:column span="1" width="20">
      &nbsp;
    </cmr:column>
    <cmr:column span="4" width="780">
      <div ng-show="report" style="background:#EEE; height:300px; overflow-y:auto" >
      <div class="tab-head">Automation Statistics</div>
      <table width="100%" cellspacing="0" cellpadding="0" margin="0" class="rec-table">
        <tr>
          <th>Country</th>
          <th>Review %</th>
          <th>Validation</th>
          <th><div>TAT (All)</div></th>
          <th><div>TAT (Auto)</div></th>
          <th>Total</th>
          <th>Reviews</th>
          <th>Touchless (COM)</th>
          <th>Queue</th>
        </tr>
        <tr ng-show="autoContries == 0">
          <td colspan="9">No data found</td>
        </tr>
        <tr ng-repeat="rec in autoCountries">
          <td>{{rec.cntry}}</td>
          <td>
            {{rec.rec.manualPercentage}}%
          </td>
          <td>{{rec.rec.automationAverage}}</td>
          <td>{{rec.rec.completionAverage}}</td>
          <td>{{rec.rec.fullAutoAverage}}</td>
          <td>{{rec.rec.total}}</td>
          <td>{{rec.rec.reviews}}</td>
          <td>{{rec.rec.completes}}</td>
          <td>{{rec.rec.currentQueue}}</td>
        </tr>
      </table>
      </div>
    </cmr:column>
  </cmr:row>
  <cmr:row>
  <cmr:column span="6" >
  <div class="processing" ng-show="report">
    <div ng-show="report" style="background:#FFF; height:450px; overflow-y:auto" >
    <div class="tab-head">Today's Requests</div>
      <div class="filters">
        <input class="filter" ng-model="autoFilterText" placeholder="Input any text filter"> 
      </div>
    <table width="100%" class="cntry-table">
      <tr>
        <tr>
          <th>ID</th>
          <th>Status</th>
          <th>Type</th>
          <th>Country</th>
          <th>Customer</th>
          <th>Source</th>
          <th>Manual</th>
          <th>TAT</th>
          <th>Appr.</th>
          <th>Validation</th>
          <th>Pending</th>
        </tr>
      </tr>
      <tr ng-show="report.automationRecords == 0">
        <td colspan="10">No requests found</td>
      </tr>
      <tr ng-repeat="rec in report.automationRecords | filter:autoFilterText">
          <td><a href="${contextPath}/request/{{rec.reqId}}" target="_blank">{{rec.reqId}}</a></td>
          <td>{{rec.statusDesc}}</td>
          <td>{{rec.reqType}}</td>
          <td>{{rec.cmrIssuingCntry}}-{{rec.cntryNm}}</td>
          <td>
            <div ng-show="rec.custNm" title="{{rec.custNm}}" style="cursor:pointer">{{rec.custNm.length > 15 ? rec.custNm.substring(0,15)+'...' : rec.custNm}}</div>
          </td>
          <td>{{rec.sourceSystId}}</td>
          <td>{{rec.manual}}</td>
          <td>
            {{rec.diffMin}}m
          </td>
          <td>
            {{rec.aprMin}}m
          </td>
          <td>
            {{rec.diffMinNxt}}m
          </td>
          <td>
            {{rec.reqStatus == 'AUT' ? rec.diffWait +'m' :  ''}}
          </td>
      </tr>
    </table>
    </div>
  </div>
  </cmr:column>
  </cmr:row>
  
  </cmr:section>
  </cmr:boxContent>
  
  </div>

</div>

<script src="${resourcesPath}/js/home/dashboard.js?${cmrv}"></script>
