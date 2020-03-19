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
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<link rel="stylesheet" href="${resourcesPath}/css/quick_search.css?${cmrv}"/>

<%
  boolean viewOnly = "Y".equals(request.getParameter("viewOnly"));
%>

<style> 
</style>

<cmr:window>
  <div ng-app="QuickSearchApp">
    <div ng-controller="DetailsController">

      <!-- CMR Number view -->
      <div ng-show="cmrNo">
               <table cellspacing="0" cellpadding="0" border="0" summary="System Parameters" class="ibm-data-table ibm-sortable-table ibm-alternating search-results">
                 <caption>
                   <em> Record Details
                     <span class="exp-coll-all" ng-click="viewMode = 'S'" ng-show="viewMode == 'E'">Switch to Simple View</span>
                     <span class="exp-coll-all" ng-click="viewMode = 'E'" ng-show="viewMode == 'S'">Switch to Extended View</span>
                     <span ng-show="viewMode == 'E'">
                       <span class="exp-coll-all" ng-click="expCollAll(true)">Expand All Details</span>
                       <span class="exp-coll-all" ng-click="expCollAll(false)">Collapse All Details</span>
                     </span>
                     <%if (!viewOnly){ %>
                     <div class="det-btn">
                       <input type="button" value="Update CMR" title="Request for an Update of this CMR" ng-click="confirmImport(true)">
                       <input type="button" value="Create CMR by Model" title="Request for a new CMR modeled after this record" ng-click="confirmImport(false)">
                     </div>
                     <%} %>
                   </em>
                 </caption>
                 <thead>
                   <tr>
                     <th scope="col" width="8%">Issuing Country</th>
                     <th scope="col" width="7%">CMR No.</th>
                     <th scope="col" width="8%">Address Type</th>
                     <th scope="col" width="7%">Seq.</th>
                     <th scope="col" width="10%">MPP No.</th>
                     <th scope="col" width="*">
                      {{viewMode == 'E' ? 'Details' : 'Name/Address'}}
                     </th>
                   </tr>
                 </thead>
                 <tbody>
                   <tr ng-repeat="rec in records">
                     <td><span class="grid-code" title="{{rec.cmrIssuedByDesc}}">{{rec.cmrIssuedBy}}</span></td>
                     <td ng-class="{'soldto' : rec.cmrAddrTypeCode == 'ZS01'}">{{rec.cmrNum}}</td>
                     <td ng-class="{'soldto' : rec.cmrAddrTypeCode == 'ZS01'}">{{rec.cmrAddrType}}</td>
                     <td ng-class="{'soldto' : rec.cmrAddrTypeCode == 'ZS01'}">{{rec.cmrAddrSeq}}</td>
                     <td ng-class="{'soldto' : rec.cmrAddrTypeCode == 'ZS01'}">{{rec.cmrSapNumber}}</td>
                     <td>
                       <div ng-show="viewMode == 'S'" ng-class="{'soldto' : rec.cmrAddrTypeCode == 'ZS01'}">
                         <div class="det-name">
                           {{rec.cmrName1Plain}} {{rec.cmrName2Plain ? ' ' +rec.cmrName2Plain : ''}}
                         </div>
                         {{rec.cmrStreetAddress ? rec.cmrStreetAddress.toUpperCase() : ''}}
                         {{rec.cmrName3 ? ', ' + rec.cmrName3.toUpperCase() : ''}}
                         {{rec.cmrCity ? ', ' + rec.cmrCity.toUpperCase() : ''}}
                         {{rec.cmrStateDesc ? ', ' + rec.cmrStateDesc.toUpperCase() : ''}}
                         <br>
                         {{rec.cmrCountryLandedDesc ? rec.cmrCountryLandedDesc.toUpperCase() : ''}}
                         {{rec.cmrPostalCode ? ' ' + rec.cmrPostalCode : ''}}
                       </div>
                       <div  ng-show="viewMode == 'E'">
                       <div class="det-name">
                         {{rec.cmrName1Plain}} {{rec.cmrName2Plain ? ' ' +rec.cmrName2Plain : ''}}
                       </div>
                       <div>
                         <div class="det-header" ng-click="expColl(rec, 'colLoc')">
                           <img ng-show="rec.colLoc" src="${resourcesPath}/images/collapse2.png" class="exp-col"> 
                           <img ng-show="!rec.colLoc" src="${resourcesPath}/images/expand2.png" class="exp-col"> 
                           Address Details
                         </div>
                         <div class="det-content" ng-show="rec.colLoc">
                           <table cellspacing="0" cellpadding="0" class="det-table">
                             <tr>
                               <th width="20%">Street Address:</th>
                               <td width="30%">{{rec.cmrStreetAddress}} {{rec.cmrName3 ? ', ' + rec.cmrName3 : '' }}
                               </td>
                               <th width="20%">City:</th>
                               <td width="30%">{{rec.cmrCity}}</td>
                             </tr>
                             <tr>
                               <th>State/Province:</th>
                               <td>{{rec.cmrState}} {{rec.cmrStateDesc ? ' - '+rec.cmrStateDesc : ''}}</td>
                               </td>
                               <th>Postal Code:</th>
                               <td>{{rec.cmrPostalCode}}</td>
                             </tr>
                             <tr>
                               <th>Landed Country:</th>
                               <td>{{rec.cmrCountryLanded}} {{rec.cmrCountryLandedDesc ? ' - '+rec.cmrCountryLandedDesc : ''}}</td>
                               <th>County:</th>
                               <td>{{rec.cmrCounty}}</td>
                             </tr>
                           </table>
                           <br>
                         </div> 
                       </div>
                       
                       <div>
                         <div class="det-header" ng-click="expColl(rec, 'colCust')">
                           <img ng-show="rec.colCust" src="${resourcesPath}/images/collapse2.png" class="exp-col"> 
                           <img ng-show="!rec.colCust" src="${resourcesPath}/images/expand2.png" class="exp-col"> 
                           Customer Details
                         </div>
                         <div class="det-content" ng-show="rec.colCust">
                           <table cellspacing="0" cellpadding="0" class="det-table">
                             <tr>
                               <th width="20%">Abbreviated Name:</th>
                               <td width="30%">{{rec.cmrAbbrevNm}}</td>
                               </td>
                               <th width="20%">PPS CEID:</th>
                               <td width="30%">{{rec.cmrPpsceid}}</td>
                             </tr>
                             <tr>
                               <th>Subindustry:</th>
                               <td>{{rec.cmrSubIndustry}} {{ rec.cmrSubIndustryDesc ? ' - '+rec.cmrSubIndustryDesc : ''}}</td>
                               <th>ISIC:</th>
                               <td>{{rec.cmrIsic}} {{rec.cmrIsicDesc ? ' - '+rec.cmrIsicDesc : ''}}</td>
                             </tr>
                             <tr>
                               <th>VAT:</th>
                               <td>{{rec.cmrVat}}</td>
                               <th>Tax Code 1:</th>
                               <td>{{rec.cmrBusinessReg}}</td>
                             </tr>
                           </table>
                           <br>
                         </div>
                       </div>


                       <div>
                         <div class="det-header" ng-click="expColl(rec, 'colIbm')">
                           <img ng-show="rec.colIbm" src="${resourcesPath}/images/collapse2.png" class="exp-col"> 
                           <img ng-show="!rec.colIbm" src="${resourcesPath}/images/expand2.png" class="exp-col"> 
                           IBM Details
                         </div>
                         <div class="det-content" ng-show="rec.colIbm">
                           <table cellspacing="0" cellpadding="0" class="det-table">
                             <tr>
                               <th width="20%">RDc Create Date:</th>
                               <td width="30%">{{rec.cmrRdcCreateDate}}</td>
                               </td>
                               <th width="20%">CAP:</th>
                               <td width="30%">{{rec.cmrCapIndicator == 'Y' ? 'Yes' : 'No'}}</td>
                             </tr>
                             <tr>
                               <th>SORTL:</th>
                               <td>{{rec.cmrSortl}}</td>
                               <th>Site ID:</th>
                               <td>{{rec.cmrSitePartyID}}</td>
                             </tr>
                             <tr>
                               <th>ISU:</th>
                               <td>{{rec.cmrIsu}} {{rec.cmrIsuDesc ? ' - '+rec.cmrIsuDesc : ''}}</td>
                               <th>Company #:</th>
                               <td>{{rec.cmrCompanyNo}}</td>
                             </tr>
                             <tr>
                               <th>Client Tier:</th>
                               <td>{{rec.cmrTier}} {{rec.cmrTierDesc ? ' - '+rec.cmrTierDesc : ''}}</td>
                               <th>Enterprise #:</th>
                               <td>{{rec.cmrEnterpriseNumber}}</td>
                             </tr>
                             <tr>
                               <th>Order Block:</th>
                               <td>{{rec.cmrOrderBlock}} {{rec.cmrOrderBlockDesc ? ' - '+rec.cmrOrderBlockDesc : ''}}</td>
                               <th>Affiliate #:</th>
                               <td>{{rec.cmrAffiliate}}</td>
                             </tr>
                             <tr>
                               <th>Customer Class:</th>
                               <td>{{rec.cmrClass}} {{rec.cmrClassDesc ? ' - '+rec.cmrClassDesc : ''}}</td>
                               <th>NAC/INAC:</th>
                               <td>
                                 {{rec.cmrInacType ? 'Type: '+rec.cmrInacType : ''}}
                                 {{rec.cmrInac ? ', ' +rec.cmrInac : ''}}
                                 {{rec.cmrInacDesc ? ' - '+rec.cmrInacDesc : ''}}
                               </td>
                             </tr>
                             <tr>
                               <th>Final Coverage:</th>
                               <td>{{rec.cmrCoverage}} {{rec.cmrCoverageName ? ' - '+rec.cmrCoverageName : ''}}</td>
                               <th>Buying Group:</th>
                               <td>{{rec.cmrBuyingGroup}} {{rec.cmrBuyingGroupDesc ? ' - '+rec.cmrBuyingGroupDesc : ''}}</td>
                             </tr>
                             <tr>
                               <th>Base Coverage:</th>
                               <td>{{rec.cmrBaseCoverage}} {{rec.cmrBaseCoverageName ? ' - '+rec.cmrBaseCoverageName : ''}}</td>
                               <th>Global Buying Group:</th>
                               <td>{{rec.cmrGlobalBuyingGroup}} {{rec.cmrGlobalBuyingGroupDesc ? ' - '+rec.cmrGlobalBuyingGroupDesc : ''}}</td>
                             </tr>
                             <tr>
                               <th>GOE Status:</th>
                               <td>{{rec.cmrGOEIndicator == 'Y' ? 'Government-Owned' : (rec.cmrGOEIndicator == 'S' ? 'State-Owned' : (rec.cmrGOEIndicator == 'U' ? 'Unknown' : 'No'))}}</td>
                               <th>LDE Rule:</th>
                               <td>{{rec.cmrLde}}</td>
                             </tr>
                             <tr>
                               <th>DUNS No.:</th>
                               <td>{{rec.cmrDuns}}
                               <th>GU DUNS No.:</th>
                               <td>{{rec.cmrGuNumber}}</td>
                             </tr>
                           </table>
                           <br>
                         </div>
                       </div>
                       
                       <div ng-show="rec.cmrIntlSubLangCd || rec.cmrOtherIntlSubLangCd">
                         <div class="det-header" ng-click="expColl(rec, 'colAlt')">
                           <img ng-show="rec.colAlt" src="${resourcesPath}/images/collapse2.png" class="exp-col"> 
                           <img ng-show="!rec.colAlt" src="${resourcesPath}/images/expand2.png" class="exp-col"> 
                           Local Language
                         </div>
                         <div class="det-content" ng-show="rec.colAlt">
                           <table cellspacing="0" cellpadding="0" class="det-table">
                             <tr>
                               <th width="20%">Customer Name:</th>
                               <td colspan="3">{{rec.cmrIntlName1}} {{rec.cmrIntlName2}}</td>
                             </tr>
                             <tr>
                               <th width="20%">Street Address:</th>
                               <td colspan="3">{{rec.cmrIntlAddress}} {{rec.cmrIntlName3}}</td>
                             </tr>
                             <tr>
                               <th width="20%">City:</th>
                               <td width="30%">{{rec.cmrIntlCity1}} {{rec.cmrIntlCity2}} </td>
                               <th width="20%">Language Code:</th>
                               <td width="30%">{{rec.cmrIntlSubLangCd}}  {{rec.cmrIntlSubLangDesc ? ' - ' + rec.cmrIntlSubLangDesc : ''}}</td>
                             </tr>
                             <tr>
                               <th width="20%">Customer Name:</th>
                               <td colspan="3">{{rec.cmrOtherIntlBusinessName}}</td>
                             </tr>
                             <tr>
                               <th width="20%">Street Address:</th>
                               <td colspan="3">{{rec.cmrOtherIntlAddress}} {{rec.cmrOtherIntlName3}}</td>
                             </tr>
                             <tr>
                               <th width="20%">City:</th>
                               <td width="30%">{{rec.cmrOtherIntlCity1}} {{rec.cmrOtherIntlCity2}} </td>
                               <th width="20%">Language Code:</th>
                               <td width="30%">{{rec.cmrOtherIntlSubLangCd}} {{rec.cmrOtherIntlSubLangDesc ? ' - ' + rec.cmrOtherIntlSubLangDesc : ''}}</td>
                             </tr>
                           </table>
                           <br>
                         </div>
                       </div>
                         
                       
                       </div>
                     </td>
                   </tr>
                   <tr>
                     <td colspan="6">
                       <span class="exp-coll-all" ng-click="viewMode = 'S'" ng-show="viewMode == 'E'">Switch to Simple View</span>
                       <span class="exp-coll-all" ng-click="viewMode = 'E'" ng-show="viewMode == 'S'">Switch to Extended View</span>
                       <span ng-show="viewMode == 'E'">
                         <span class="exp-coll-all" ng-click="expCollAll(true)">Expand All Details</span>
                         <span class="exp-coll-all" ng-click="expCollAll(false)">Collapse All Details</span>
                       </span>
                       <%if (!viewOnly){ %>
                       <div class="det-btn">
                         <input type="button" value="Update CMR" title="Request for an Update of this CMR">
                         <input type="button" value="Create CMR by Model" title="Request for a new CMR modeled after this record">
                       </div>
                       <%} %>
                     </td>
                   </tr>
                 </tbody>
               </table>
        
      </div>
      
      <!--  DUNS View -->
      <div ng-show="dunsNo && !cmrNo">
               <table cellspacing="0" cellpadding="0" border="0" summary="System Parameters" class="ibm-data-table ibm-sortable-table ibm-alternating search-results">
                 <caption>
                   <em> Record Details
                     <div class="det-btn">
                       <input type="button" value="Create New CMR" title="Request for a new CMR using this D&B record." ng-click="confirmImport(false)">
                     </div>
                   </em>
                 </caption>
                 <tbody>
                  <tr>
                    <td colspan="4" class="dnb-sub">General Details</td>
                  </tr>
                  <tr>
                    <td width="20%" class="dnb-label">Company Name:</td>
                    <td width="30%" ng-bind-html="dnb.companyName"></td>
                    <td width="20%" class="dnb-label">Tradestyle Name:</td>
                    <td width="30%" ng-bind-html="dnb.tradestyleName"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label">DUNS No.:</td>
                    <td ng-bind-html="dnb.dunsNo"></td>
                    <td class="dnb-label">Transferred from DUNS No.:</td>
                    <td ng-bind-html="dnb.transferDunsNo"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label">DU DUNS No.:</td>
                    <td ng-bind-html="dnb.duDunsNo"></td>
                    <td class="dnb-label">DU Organization:</td>
                    <td ng-bind-html="dnb.duOrganizationName"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label">GU DUNS No.:</td>
                    <td ng-bind-html="dnb.guDunsNo"></td>
                    <td class="dnb-label">GU Organization:</td>
                    <td ng-bind-html="dnb.guOrganizationName"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label"><strong>IBM ISIC:</strong></td>
                    <td>
                      <strong>
                        <span ng-bind-html="dnb.ibmIsic"></span> - <span ng-bind-html="dnb.ibmIsicDesc"></span>
                      </strong>
                    </td>
                    <td class="dnb-label">&nbsp;</td>
                    <td class="dnb-label">&nbsp;</td>
                    </td>
                  </tr>
                  <tr>
                    <td colspan="4" class="dnb-sub">Primary Address</td>
                  </tr>
                  <tr>
                    <td class="dnb-label">Address:</td>
                    <td ng-bind-html="dnb.primaryAddress"></td>
                    <td class="dnb-label">City:</td>
                    <td ng-bind-html="dnb.primaryCity"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label">State:</td>
                    <td ng-bind-html="dnb.primaryStateName"></td>
                    <td class="dnb-label">County:</td>
                    <td ng-bind-html="dnb.primaryCounty"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label">Country:</td>
                    <td ng-bind-html="dnb.primaryCountry"></td>
                    <td class="dnb-label">Postal Code:</td>
                    <td ng-bind-html="dnb.primaryPostalCode"></td>
                  </tr>
                  <tr>
                    <td colspan="4" class="dnb-sub">Mailing Address</td>
                  </tr>
                  <tr>
                    <td class="dnb-label">Address:</td>
                    <td ng-bind-html="dnb.mailingAddress"></td>
                    <td class="dnb-label">City:</td>
                    <td ng-bind-html="dnb.mailingCity"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label">State:</td>
                    <td ng-bind-html="dnb.mailingStateName"></td>
                    <td class="dnb-label">County:</td>
                    <td ng-bind-html="dnb.mailingCounty"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label">Country:</td>
                    <td ng-bind-html="dnb.mailingCountry"></td>
                    <td class="dnb-label">Postal Code:</td>
                    <td ng-bind-html="dnb.mailingPostalCode"></td>
                  </tr>
                  <tr>
                    <td colspan="2" class="dnb-sub">Organization</td>
                  </tr>
                  <tr>
                    <td class="dnb-label">Organization Type:</td>
                    <td ng-bind-html="dnb.organizationType"></td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                  <tr>
                    <td class="dnb-label">Organization IDs:</td>
                    <td colspan="3">
                      <div ng-repeat="orgId in dnb.organizationIds">{{orgId}}</div>
                    </td>
                  </tr>
                  <tr>
                    <td class="dnb-label">D&B Industry Codes:</td>
                    <td ng-bind-html="dnb.dnbStandardIndustryCodes"></td>
                    <td class="dnb-label">Line of Business:</td>
                    <td ng-bind-html="dnb.lineOfBusiness"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label"># of Employees:</td>
                    <td ng-bind-html="dnb.individualEmployeeCount"></td>
                    <td class="dnb-label"># of Employees (Consolidated):</td>
                    <td ng-bind-html="dnb.consolidatedEmployeeCount"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label">Principal Person Name:</td>
                    <td ng-bind-html="dnb.principalName"></td>
                    <td class="dnb-label">Principal Person Title:</td>
                    <td ng-bind-html="dnb.principalTitle"></td>
                  </tr>
                  <tr>
                    <td class="dnb-label">Telephone No:</td>
                    <td ng-bind-html="dnb.phoneNo"></td>
                    <td class="dnb-label">Facsimile No:</td>
                    <td ng-bind-html="dnb.faxNo">{{dnb}}</td>
                  </tr>
                  <tr>
                    <td colspan="4">
                     <div class="det-btn">
                       <input type="button" value="Create New CMR" title="Request for a new CMR using this D&B record." ng-click="confirmImport(false)">
                     </div>
                    </td>
                  </tr>
                 </tbody>
              </table>
      </div>
    </div>
  </div>
  <cmr:row>
  </cmr:row>
  <cmr:windowClose />
<script src="${resourcesPath}/js/quick_search.js?${cmrv}"
  type="text/javascript"></script>
</cmr:window>
