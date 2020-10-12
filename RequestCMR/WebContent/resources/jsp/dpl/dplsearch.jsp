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
</style>

<cmr:window>
  <div ng-app="DPLSearchApp">
    <div ng-controller="DPLSearchController">
    
    
      <!-- Customer -->
      <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results">
        <tbody>
          <tr>
            <td class="dnb-label" width="15%">Request ID:</td>
            <td ng-bind-html="reqId" width="15%"></td>
            <td class="dnb-label" width="20%">Customer Name:</td>
            <td ng-bind-html="customerName" width="*"></td>
          </tr>
          <tr>
            <td class="dnb-label">DPL Check Result:</td>
            <td>
              <span style="font-weight:bold">
                {{dplResult}}
              </span>              
            </td>
            <td class="dnb-label">DPL Check Date:</td>
            <td ng-bind-html="dplCheckDate"></td>
          </tr>
        </tbody>
      </table>
      <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results">
        <caption>
          <em>Addresses</em>
        </caption>
        <tbody>
          <tr ng-repeat="addr in request.addresses ">
            <td class="dnb-label" width="10%">Name:</td>
            <td width="30%">
              {{addr.custNm1 ? addr.custNm1 : customerName}}
            </td>
            <td class="dnb-label" width="10%">Address:</td>
            <td width-="36%">
              {{addr.addrTxt}} {{addr.addrTxt2 ? ', ' + addr.addrTxt2 : ''}}
              <br>
              {{addr.city1}} {{addr.stateProv ? ', '+addr.stateProv : ''}}
              <br>
              {{addr.landCntry}} {{addr.postCd ? addr.postCd : ''}}
            </td>
            <td class="dnb-label" width="10%">DPL Check:</td>
            <td width="14%">
              {{addr.dplChkResult}}
            </td>
          </tr>
          
        </tbody>
      </table>
      
      
      <div ng-show="results">
        <div class="filter" style="display:inline-block;float:left;margin-bottom:20px;font-size:14px;width:700px;font-weight:bold">
          Showing results for searches against the name and variations. Results after the first one already filter out
          entities appearing on the previous name search.
        </div>
        <div class="filter" style="display:inline-block;float:right;margin-bottom:20px">
          <input ng-model="allTextFilter" placeholder="Type to filter by name" style="width:200px">
        </div>
      </div>
      <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results" ng-repeat="result in results">
        <caption>
          <em>DPL Matches for "{{result.searchArgument}}" ({{result.records.length}} matches)</em>
          <img class="exp-col" title="Expand Details" src="${resourcesPath}/images/add.png" ng-click="result.exp = true" ng-show="!result.exp">
          <img class="exp-col" title="Collapse Details" src="${resourcesPath}/images/collapse2.png" ng-click="result.exp = false" ng-show="result.exp">
        </caption>
        <thead ng-show="result.exp">
          <tr>
            <th width="8%">Item</th>
            <th width="*">DPL Information (Denial Country Code, Denied Party Name)</th>
          </tr>
        </thead>
        <tbody ng-show="result.exp">
          <tr ng-repeat="party in result.records| textFilter:allTextFilter">
            <td>{{party.itemNo}}</td>
            <td>
              <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results">
                <tbody>
                  <tr ng-repeat="record in party.records" style="border-top:1px solid #DDD">
                    <td width="10%" style="border-top:1px solid #DDD">
                      {{record.countryCode}}
                    </td>
                    <td width="*" style="border-top:1px solid #DDD" >
                      <img class="exp-col" title="Expand Details" src="${resourcesPath}/images/add.png" ng-click="record.exp = true" ng-show="!record.exp">
                      <img class="exp-col" title="Collapse Details" src="${resourcesPath}/images/collapse2.png" ng-click="record.exp = false" ng-show="record.exp">
                      {{record.companyName ? record.companyName : record.customerFirstName + ' ' +record.customerLastName}}      
                      
                      <div ng-show="record.exp" style="font-size:10px">
                        <br>
                        ENTITY ID: {{record.entityId}}
                        <br>
                        ADDRESS: {{record.entityAddress}}, {{record.city}}, {{record.entityCountry}} {{record.entityPostalCode}}
                        <br>
                        DENIAL CODE: {{record.denialCodeDescription}} [{{record.denialCode}}]
                        <br>
                        COMMENTS: {{record.comments}} 
                        <br>
                        ADDL INFO:
                        {{record.additionalInfo}}
                      </div>      
                    </td>
                  </tr>
                </tbody>
              </table>
            </td>
          </tr>
        </tbody>
      </table>
      
    </div>
  </div>
  <cmr:row>
  </cmr:row>
  <cmr:windowClose />
<script src="${resourcesPath}/js/dpl/dplsearch.js?${cmrv}"
  type="text/javascript"></script>
</cmr:window>
