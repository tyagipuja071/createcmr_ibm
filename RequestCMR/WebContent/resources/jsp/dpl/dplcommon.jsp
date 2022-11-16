<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
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

tr.top-match {
background: #e1ffff; /* Old browsers */
background: -moz-linear-gradient(top,  #e1ffff 0%, #e1ffff 7%, #e1ffff 12%, #fdffff 12%, #e6f8fd 30%, #c8eefb 54%, #bee4f8 75%, #b1d8f5 100%); /* FF3.6-15 */
background: -webkit-linear-gradient(top,  #e1ffff 0%,#e1ffff 7%,#e1ffff 12%,#fdffff 12%,#e6f8fd 30%,#c8eefb 54%,#bee4f8 75%,#b1d8f5 100%); /* Chrome10-25,Safari5.1-6 */
background: linear-gradient(to bottom,  #e1ffff 0%,#e1ffff 7%,#e1ffff 12%,#fdffff 12%,#e6f8fd 30%,#c8eefb 54%,#bee4f8 75%,#b1d8f5 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#e1ffff', endColorstr='#b1d8f5',GradientType=0 ); /* IE6-9 */
}

tr.top-match-e {
background: #eaefb5; /* Old browsers */
background: -moz-linear-gradient(top,  #eaefb5 0%, #e1e9a0 100%); /* FF3.6-15 */
background: -webkit-linear-gradient(top,  #eaefb5 0%,#e1e9a0 100%); /* Chrome10-25,Safari5.1-6 */
background: linear-gradient(to bottom,  #eaefb5 0%,#e1e9a0 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#eaefb5', endColorstr='#e1e9a0',GradientType=0 ); /* IE6-9 */
}
.dpl-y {
  background: #febbbb; /* Old browsers */
  background: -moz-linear-gradient(top,  #febbbb 0%, #fe9090 45%, #ff5c5c 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top,  #febbbb 0%,#fe9090 45%,#ff5c5c 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom,  #febbbb 0%,#fe9090 45%,#ff5c5c 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#febbbb', endColorstr='#ff5c5c',GradientType=0 ); /* IE6-9 */
  border: 1px Solid #999;
  cursor: pointer;
}
.dpl-n {
  background: #e4efc0; /* Old browsers */
  background: -moz-linear-gradient(top,  #e4efc0 0%, #abbd73 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top,  #e4efc0 0%,#abbd73 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom,  #e4efc0 0%,#abbd73 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#e4efc0', endColorstr='#abbd73',GradientType=0 ); /* IE6-9 */
  border: 1px Solid #999;
  cursor: pointer;
}
.dpl-u {
  cursor: pointer;
}
table.search-results-dpl td, table.search-results-dpl th {
  font-size:11px;
}
</style>

  <div ng-app="DPLSearchApp">
    <div ng-controller="DPLSearchController" ng-cloak>

        <table cellspacing="0" cellpadding="0" border="0" summary="Info" class="ibm-data-table ibm-sortable-table ibm-alternating legacy-crit" ng-show="plain">
          <thead>
            <tr>
              <th scope="col" width="20%"><span style="color:red">*</span> Name to Search: </th>
              <td width="*">
                <input ng-model="searchString" style="width:500px">
              </td>
            </tr>
            <tr>
              <td colspan="4">
                <input type="button" class="btn-search" value="Search DPL" style="margin-left:10px" ng-click="search()"> 
                <input type="button" class="btn-search" value="Export Directly to PDF" style="width:160px; margin-left:10px" ng-click="generatePDF()"> 
                <input type="button" class="btn-reset" value="Reset" style="margin-left:10px" onclick="resetSearch()"> 
              </th>
            </tr>
          </thead>
          <tbody>
            <tr>
            </tr>
          </tbody>
        </table>
      
      <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results" ng-show="!plain">
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
      <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results" ng-show="!plain">
        <caption>
          <em>Addresses for DPL Check</em>
        </caption>
        <tbody>
          <tr ng-repeat="addr in request.addresses ">
            <td class="dnb-label" width="10%">Name:</td>
            <td width="30%">
              {{addr.custNm1 ? addr.custNm1 : customerName}}
            </td>
            <td class="dnb-label" width="10%">Address:</td>
            <td width="36%">
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
      <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results" ng-show="!plain">
        <caption>
          <em>DPL Results Assessment</em>
        </caption>
        <tbody>
          <tr>
            <td class="dnb-label" width="10%">Status:</td>
            <td width="25%"><strong>{{dplAssessment}}</strong></td>
            <td class="dnb-label" width="10%">Assessed By:</td>
            <td width="*">{{dplAssessmentBy ? dplAssessmentBy : ''}}</td>
            <td class="dnb-label" width="10%">Date:</td>
            <td width="15%">{{dplAssessmentDate ? dplAssessmentDate : ''}}</td>
          </tr>
          <tr>
            <td class="dnb-label">Comments:</td>
            <td colspan="5">
              <div ng-show="!reassess">{{dplAssessmentCmt}}</div>
              <div ng-show="reassess">
                <textarea ng-model="dplAssessmentCmt" rows="5" cols="70"></textarea>
              </div>
            </td>
          </tr>
          <tr > 
            <td colspan="6">
              <input type="button" class="btn-search" value="Assess Results" style="margin-left:10px" ng-click="reassess = true" ng-show="!reassess"> 
              <input type="button" class="btn-search dpl-y" value="Yes, the Customer is on DPL" style="margin-left:10px" ng-click="assessDPL('Y')" ng-show="searchSuccess && results && results.length > 0 && reassess"> 
              <input type="button" class="btn-search dpl-n" value="No, the Customer is not on DPL" style="margin-left:10px" ng-click="assessDPL('N')" ng-show="searchSuccess && results && results.length > 0 && reassess"> 
              <input type="button" class="btn-search dpl-u" value="Needs further review" style="margin-left:10px" ng-click="assessDPL('U')" ng-show="reassess"> 
            </td>
          </tr>
        </tbody>
      </table>
      
      
      <br>
      <br>
      <div ng-show="searchSuccess && (!results || results.length == 0) && !plain">
        <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results search-results-dpl">
          <caption>
            <em><strong>Non-deterministic results returned. The DPL check and matching needs further review.</strong></em>
          </caption>
        </table>
      </div>
      <div ng-show="searchSuccess && (!results || results.length == 0) && plain">
        <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results search-results-dpl">
          <caption>
            <em><strong>Non-deterministic results returned. Please try searching against the DPL Database directly.</strong></em>
          </caption>
        </table>
      </div>
      <div ng-show="!searchSuccess">
        <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results search-results-dpl">
          <caption>
            <em>Search not yet done or encountered an error.</em>
          </caption>
        </table>
      </div>
      <div ng-show="results && results.length > 0">
        <div class="filter" style="display:inline-block;float:left;margin-bottom:20px;font-size:14px;width:700px;font-weight:bold">
          Showing results for searches against the name and variations. Results after the first one already filter out
          entities appearing on the previous name search.
        </div>
        <div class="filter" style="display:inline-block;float:right;margin-bottom:20px">
          <input ng-model="allTextFilter" placeholder="Type to filter by name" style="width:200px">
        </div>
      </div>
      <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results search-results-dpl" ng-repeat="result in results">
        <caption>
          <em>DPL Matches for "{{result.searchArgument}}" ({{result.records.length}} matches)</em>
          <img class="exp-col" title="Expand Details" src="${resourcesPath}/images/add.png" ng-click="result.exp = true" ng-show="!result.exp">
          <img class="exp-col" title="Collapse Details" src="${resourcesPath}/images/collapse2.png" ng-click="result.exp = false" ng-show="result.exp">
        </caption>
        <thead ng-show="result.exp">
          <tr>
            <th width="8%">Item</th>
            <th width="12%">Denial Country</th>
            <th width="*">Denied Party Name</th>
          </tr>
        </thead>
        <tbody ng-show="result.exp">
          <tr ng-repeat="top in result.topMatches" class="{{top.exact ? 'top-match-e' : 'top-match'}}">
            <td style="font-weight:bold">{{top.exact ? 'Exact Match' : 'Top Match'}}</td>
            <td colspan="2">
              <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results" style="margin-bottom:0">
                <tbody>
                  <tr style="border-top:1px solid #DDD">
                    <td width="10%" style="border-top:1px solid #DDD">
                      {{top.countryCode}}
                    </td>
                    <td width="*" style="border-top:1px solid #DDD" >
                      <img class="exp-col" title="Expand Details" src="${resourcesPath}/images/add.png" ng-click="top.exp = true" ng-show="!top.exp">
                      <img class="exp-col" title="Collapse Details" src="${resourcesPath}/images/collapse2.png" ng-click="top.exp = false" ng-show="top.exp">
                      {{top.companyName ? top.companyName : top.customerFirstName + ' ' +top.customerLastName}}      
                      
                      <div ng-show="top.exp" style="font-size:10px">
                        <br>
                        ENTITY ID: {{top.entityId}} ({{top.status}})
                        <br>
                        START DATE: {{top.startDate}} 
                        <br>
                        END DATE: {{top.endDate}}
                        <br>
                        ADDRESS: {{top.entityAddress}}, {{top.city}}, {{top.entityCountry}} {{top.entityPostalCode}}
                        <br>
                        DENIAL CODE: {{top.denialCodeDescription}} [{{top.denialCode}}]
                        <br>
                        COMMENTS: {{top.comments}} 
                        <br>
                        ADDL INFO:
                        {{top.additionalInfo}}
                      </div>      
                    </td>
                  </tr>
                </tbody>
              </table>
            </td>
          </tr>
          <tr ng-repeat="party in result.records| textFilter:allTextFilter">
            <td>{{party.itemNo}}</td>
            <td colspan="2">
              <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results" style="margin-bottom:0">
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
                        ENTITY ID: {{record.entityId}} ({{record.status}})
                        <br>
                        START DATE: {{record.startDate}} 
                        <br>
                        END DATE: {{record.endDate}}
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
      
       <table cellspacing="0" cellpadding="0" border="0" summary="Info" class="ibm-data-table ibm-sortable-table ibm-alternating legacy-crit" ng-show="results && results.length > 0">
          <thead>
            <tr>
              <td colspan="4">
                <input type="button" class="btn-search" value="Export to PDF" style="margin-left:10px" ng-click="generatePDF()" ng-show="plain"> 
                <input type="button" class="btn-search" value="Attach Results to Request" style="margin-left:10px" ng-click="attachToRequest()" ng-show="!plain"> 
              </th>
            </tr>
          </thead>
          <tbody>
          </tbody>
        </table>

      <iframe id="pdfFrame" style="display:none" name="pdfFrame"></iframe>
      <form _csrf="GhtjeYhfngleOImde2" name="frmPDF" method="POST" action="${contextPath}/dplsearch/pdf" target="pdfFrame">
        <input type="hidden" name="processType" value="SEARCH">
        <input id="pdfSearchString" type="hidden" name="searchString">
        <input type="hidden" name="_csrf" id="_csrf" value="GhtjeYhfngleOImde2" />
      </form>
       
    </div>
  </div>
