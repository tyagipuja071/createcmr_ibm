<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<link rel="stylesheet" href="${resourcesPath}/css/quick_search.css?${cmrv}"/>
<%
AppUser user = AppUser.getUser(request);
%> 
<style>
form.ibm-column-form .dijitTextBox INPUT {
  font-size: 14px;
}
</style>
<script>
  dojo.addOnLoad(function() {
    dojo.byId('quick_search_btn').style.display = 'none';
    // load dropdown values
    //FilteringDropdown.loadItems('issuingCntry', 'issuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');

    var ids1 = cmr.query('EXCEP.GET_COUNTRIES', {_qall  : 'Y'});
    var model1 = { 
        identifier : "id", 
        label : "name",
        items : []
    };
    model1.items.push({id : '', name : ''});
    for (var i =0; i < ids1.length; i++){
      model1.items.push({id : ids1[i].ret1, name : ids1[i].ret2});
    }
    var dropdown1 = {
        listItems : model1
    };
    FilteringDropdown.loadFixedItems('issuingCntry', null, dropdown1);
    
    
    FilteringDropdown.loadItems('countryCd', 'countryCd_spinner', 'bds', 'fieldId=LandedCountry');
    FilteringDropdown.loadOnChange('stateProv', 'stateProv_spinner', 'bds', 'fieldId=StateProv&cmrIssuingCntry=_issuingCntry&landCntry=_countryCd', 'countryCd');
    
    var ids2 = cmr.query('QUICK_SEARCH.RESTRICT_TO', {_qall  : 'Y'});
    var model2 = { 
        identifier : "id", 
        label : "name",
        items : []
    };
    model2.items.push({id : '', name : ''});
    for (var i =0; i < ids2.length; i++){
      model2.items.push({id : ids2[i].ret1, name : ids2[i].ret2});
    }
    var dropdown2 = {
        listItems : model2
    };
    
    FilteringDropdown.loadFixedItems('restrictTo', null, dropdown2);
    //FilteringDropdown.loadItems('restrictTo', null, 'lov', 'fieldId=RestrictTo&CMRIssuingCountry=897');
    
    
    // onchange handlers
    var _cntryHandler = dojo.connect(FormManager.getField('issuingCntry'), 'onChange', function(value) {
      if (value){
        if (value.length == 3){
          var ret = cmr.query('QUICK.GET_DEFAULT_COUNTRY', {CNTRY : value});
          console.log(ret);
          if (ret && ret.ret1){
            FormManager.setValue('countryCd', ret.ret1);
          }
        } else {
          FormManager.setValue('countryCd', value.substring(3));
        }
        var cntry = value.length == 3 ? value : value.substring(0,3);
        cmr.hideNode('siret-cont');
        cmr.hideNode('restrict-cont');
        FormManager.setValue('taxCd1','');
        FormManager.readOnly('taxCd1');
        FormManager.setValue('restrictTo','');
        FormManager.readOnly('restrictTo');
        if (cntry == '897'){
          cmr.showNode('restrict-cont');
          FormManager.enable('restrictTo');
        } else if (cntry == '706'){
          cmr.showNode('siret-cont');
          FormManager.enable('taxCd1');
        } 
      }
    });
    var _landCntryHandler = dojo.connect(FormManager.getField('countryCd'), 'onChange', function(value) {
      if (value != 'US'){
        //FormManager.readOnly('stateProv');
      } else {
        FormManager.enable('stateProv');
      }
    });

    window.setTimeout('setDefaults()', 1500);
    FormManager.addValidator('countryCd', Validators.REQUIRED, [ 'Landed Country' ]);
    FormManager.addValidator('issuingCntry', Validators.REQUIRED, [ 'CMR Issuing Country' ]);
    FormManager.addFormValidator((function() {
      return {
        validate : function() {
          var noCities = ['736', '738', '834']; // MACAO, SINGAPORE, HONG KONG
          var crit = buildSearchCriteria();
          if (!crit.cmrNo){
            if (noCities.indexOf(crit.issuingCntry) >=0){
              if (!crit.name || !crit.countryCd || !crit.streetAddress1){
                return new ValidationResult({
                  id : 'streetAddress1',
                  type : 'text',
                  name : 'streetAddress1'
                }, false, 'Company Name, Country, and Street should be specified if CMR No. is blank.');
              }
            } else {
              var orgIdSearch = crit.vat || crit.taxCd1;
              var nameSearch = crit.name && crit.streetAddress1 && crit.city;
              if (!orgIdSearch && !nameSearch){
                return new ValidationResult({
                  id : 'streetAddress1',
                  type : 'text',
                  name : 'streetAddress1'
                }, false, 'VAT or Tax Code, OR Company Name + Country + Street + City should be specified if CMR No. is blank.');
              }
/*
              if (!crit.name || !crit.countryCd || !crit.streetAddress1 || !crit.city){
                return new ValidationResult({
                  id : 'streetAddress1',
                  type : 'text',
                  name : 'streetAddress1'
                }, false, 'Company Name, Country, Street, and City should be specified if CMR No. is blank.');
              }
*/
            }
            if (crit.countryCd == 'US' && (!crit.stateProv || !crit.postCd)){
              return new ValidationResult({
                id : 'stateProv',
                type : 'text',
                name : 'stateProv'
              }, false, 'State and Postal Code are required for US searches.');
            }
          } 
          return new ValidationResult({
            id : 'streetAddress1',
            type : 'text',
            name : 'streetAddress1'
          }, true);
        }
      };
    })(), null, 'frmCMR');
    FormManager.ready();
  });
  
  function setDefaults(){
    <%if (request.getParameter("issuingCntry") != null) {%>
      FormManager.setValue('issuingCntry', '<%=request.getParameter("issuingCntry")%>');
    <%} else if (user != null && user.getCmrIssuingCntry() != null) {%>
      FormManager.setValue('issuingCntry', '<%=user.getCmrIssuingCntry()%>');
    <%}%>
  }
</script>
<cmr:boxContent>
  <cmr:tabs />

 <cmr:section>
  <form:form method="POST" action="${contextPath}/quick_search/process" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="search" id="frmCMR">
    
      <cmr:row>
        <cmr:column span="6">
          <h3>Search for CMRs and Companies</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="6">
          <div class="embargo1">
            <img src="${resourcesPath}/images/info-bubble-icon.png" class="cmr-error-icon">
            <cmr:note text="You can quickly search for existing CMRs and D&B records based on company details. The list also shows current requests with similar details. Requests can directly be created from the results. 
            If a CMR No. is not specified, you will have to provide Name, Country, Street, and City values." />
          </div>
        </cmr:column>
      </cmr:row>
    
    
      <cmr:row topPad="15">
        <cmr:column span="1" width="185">
          <p>
            <cmr:label fieldId="issuingCntry">CMR Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="issuingCntry" searchAttr="name" style="display: block;" maxHeight="200"
              required="false" path="issuingCntry" placeHolder="Select CMR Issuing Country">
            </form:select>
          </p>
        </cmr:column>

        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="cmrNo">CMR No.: 
            </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            <form:input path="cmrNo" placeHolder="CMR No." dojoType="dijit.form.TextBox" maxlength="7" cssStyle="width: 80px"/>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="185">
          <p>
            <cmr:label fieldId="name">Customer Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="4">
          <p>
            <form:input path="name" placeHolder="Customer Name" dojoType="dijit.form.TextBox" maxlength="70" cssStyle="width:520px"/>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="185">
          <p>
            <cmr:label fieldId="countryCd">Landed Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="countryCd" searchAttr="name" style="display: block;" maxHeight="200"
              required="false" path="countryCd" placeHolder="Select Landed Country">
            </form:select>
          </p>
        </cmr:column>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="stateProv">State/Province: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="stateProv" searchAttr="name" style="display: block;" maxHeight="200"
              required="false" path="stateProv" placeHolder="Select State/Province">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="185">
          <p>
            <cmr:label fieldId="streetAddress1">Street Line 1: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:input path="streetAddress1" placeHolder="Street Line 1" dojoType="dijit.form.TextBox" maxlength="30"/>
          </p>
        </cmr:column>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="city">City: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:input path="city" placeHolder="City" dojoType="dijit.form.TextBox" maxlength="30"/>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="185">
          <p>
            <cmr:label fieldId="streetAddress2">Street Line 2: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:input path="streetAddress2" placeHolder="Street Line 2" dojoType="dijit.form.TextBox" maxlength="30"/>
          </p>
        </cmr:column>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="postCd">Postal Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:input path="postCd" placeHolder="Postal Code" dojoType="dijit.form.TextBox" maxlength="10"/>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="185">
          <p>
            <cmr:label fieldId="vat">VAT# / Business Reg #: 
            <cmr:info text="The primary tax identifier for the company. This can be VAT Number, ABN, NBN, and Tax Registration Number, to name a few. The value of this varies per country business rules on Tax."></cmr:info>
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250" >
          <p> 
            <form:input path="vat" placeHolder="VAT# / Business Reg #" dojoType="dijit.form.TextBox" maxlength="16"/>
          </p>
        </cmr:column>
        <div id="siret-cont" style="display:none">
          <cmr:column span="1" width="150">
            <p>
              <cmr:label fieldId="vat">SIRET: 
              <cmr:info text="For France companies only."></cmr:info>
              </cmr:label>
            </p>
          </cmr:column>
          <cmr:column span="2" width="250">
            <p> 
              <form:input path="taxCd1" placeHolder="SIRET" dojoType="dijit.form.TextBox" maxlength="14"/>
            </p>
          </cmr:column>
        </div>
        <div id="restrict-cont" style="display:none">
          <cmr:column span="1" width="150">
            <p>
              <cmr:label fieldId="vat">Restricted To: 
              <cmr:info text="Restriction Code, for US records only."></cmr:info>
              </cmr:label>
            </p>
          </cmr:column>
          <cmr:column span="2" width="250">
            <p> 
              <form:select dojoType="dijit.form.FilteringSelect" id="restrictTo" searchAttr="name" style="display: block;" maxHeight="200"
                required="false" path="restrictTo" placeHolder="Select Restriction">
              </form:select>
            </p>
          </cmr:column>
        </div>
      </cmr:row>


      <cmr:row>
        &nbsp;
      </cmr:row>
      <div ng-app="QuickSearchApp">
        <div ng-controller="QuickSearchController">
        <cmr:row>
          <cmr:column span="6">
            <input type="button" value="Find Company Records" class="search-btn search-pos" ng-click="findCompanies()">
            <input type="button" value="Reset" class="search-btn" ng-click="clearCriteria()">
          </cmr:column>
        </cmr:row>
        <div ng-show="searched">
          <cmr:row topPad="15">
            <cmr:column span="6">
               <table cellspacing="0" cellpadding="0" border="0" summary="System Parameters" class="ibm-data-table ibm-sortable-table ibm-alternating search-results">
                 <caption>
                   <em> Search Results
                     <div class="search-filter">
                       <input ng-model="recordsFilter" style="width:300px" placeholder="Filter Records">
                       <a ng-click="recordsFilter = ''" class="search-clear">Clear</a>
                     </div>
                   </em>
                 </caption>
                 <thead>
                   <tr>
                     <th scope="col" width="7%">Record</th>
                     <th scope="col" width="8%">CMR No./DUNS</th>
                     <th scope="col" width="8%">Match Grade</th>
                     <th scope="col" width="22%">Customer Name</th>
                     <th scope="col" width="*">Address</th>
                     <th scope="col" width="18%">VAT/Org IDs</th>
                     <th scope="col" width="17%">Actions</th>
                   </tr>
                 </thead>
                 <tbody>
                   <tr ng-show="!records || records.length == 0">
                     <td colspan="7">No records found for the given criteria.</td>
                   </tr>
                   <tr ng-repeat="rec in records | recFilter:recordsFilter">
                     <td><span ng-class="{'type-cmr' : rec.recType == 'CMR', 'type-dnb' : rec.recType == 'DNB', 'type-req' : rec.recType == 'REQ'}">{{rec.recType == 'DNB' ? 'D&B' : (rec.recType == 'REQ' ? 'Request' : rec.recType)}}</span></td>
                     <td>
                       <a ng-click="openDetails(rec)" title="Open details of the record">{{rec.recType == 'DNB' ? rec.dunsNo : rec.cmrNo}}</a>
                       <span ng-show="rec.revenue > 0 && !rec.highestRevenue" title="With revenue" style="cursor:help">
                         <br>
                         <img src="${resourcesPath}/images/money.png" class="money">
                       </span> 
                       <span ng-show="rec.revenue > 0 && rec.highestRevenue" title="Highest revenue among same match grades" style="cursor:help">
                         <br>
                         <img src="${resourcesPath}/images/money.png" class="money">
                         <img src="${resourcesPath}/images/money.png" class="money">
                         <img src="${resourcesPath}/images/money.png" class="money">
                       </span> 
                     </td>
                     <td>
                       <div ng-show="rec.recType == 'CMR'">
                         <span ng-show="rec.matchGrade != 'A'" title="{{titles[rec.matchGrade]}}" ng-class="{'match-e' : rec.matchGrade.indexOf('E') == 0, 'match-f' : rec.matchGrade.indexOf('F') == 0, 'match-duns' : rec.matchGrade.indexOf('DUNS') == 0 || rec.matchGrade.indexOf('VAT') == 0 || rec.matchGrade.indexOf('LANG') == 0, 'match-a' : rec.matchGrade.indexOf('A') == 0}">{{rec.matchGrade}}</span>
                         <img src="${resourcesPath}/images/check.png" title="{{titles[rec.matchGrade]}}" style="width:20px; height:20px; cursor:help" ng-show="rec.matchGrade == 'A'">
                       </div>
                       <div ng-show="rec.recType == 'DNB'">
                         <span title="D&B Match Confidence (0 - lowest, 10 - highest)" class="match-dnb" ng-class="{'match-e' : rec.matchGrade == '10' || rec.matchGrade == '09', 'match-f' : rec.matchGrade == '08', 'match-r' : rec.matchGrade == '07'}">{{rec.matchGrade}}</span>
                       </div>
                       <div ng-show="rec.recType == 'REQ'">
                         <span title="Match Quality (0 - lowest, 100 - highest)" class="match-dnb">{{rec.matchGrade}}</span>
                       </div>
                     </td>
                     <td>
                       {{rec.name}} 
                       <span ng-if="rec.altName" style="color:rgb(217,108,0)" title="Local Language Data">
                         <br>
                         {{rec.altName}}
                       </span>
                       <span ng-if="rec.restrictTo" class="restrict" title="Restriction Code">
                         {{rec.restrictTo}}
                       </span>
                     </td>
                     <td>
                       {{rec.streetAddress1}} {{rec.streetAddress2 ? ', '  +rec.streetAddress2: ''}}
                       
                       <br> {{rec.city}} {{rec.stateProv ? ', ' + rec.stateProv : ''}}
                       <br> {{rec.countryCd}} {{rec.postCd}}
                       <span ng-if="rec.altName" style="color:rgb(217,108,0)" title="Local Language Data">
                         <br>
                         {{rec.altStreet}}
                         <br>
                         {{rec.altCity}}
                       </span>
                         
                       
                     </td>
                     <td>
                       <span ng-show="rec.recType == 'CMR'">{{rec.vat ?  rec.vat +' (VAT Number)' : ''}}</span> 
                       <span ng-show="rec.recType == 'DNB'">
                         <div ng-repeat="orgId in parseVat(rec.vat)">
                           {{orgId}}
                         </div>
                       </span> 
                       <img ng-show="rec.orgIdMatch" src="${resourcesPath}/images/approve.png" title="Matches VAT/Tax Code/Org ID on the search criteria" style="width:20px; height:20px; cursor:help">
                     </td>
                     <td>
                       <div ng-show="rec.recType == 'DNB'">
                         <input ng-show="rec.operStatusCode != 'O'" type="button" class="cmr-grid-btn" value="Create New CMR" title="Request for a new CMR using this D&B record." ng-click="confirmImport(rec, false)">
                         <span ng-show="rec.operStatusCode == 'O'" style="font-weight:bold;color:red">Out of business</span>
                       </div>
                       <div ng-show="rec.recType == 'CMR'">
                         <input ng-show="rec.cmrNo.indexOf('P') != 0" type="button" class="cmr-grid-btn" value="Create by Model" title="Request for a new CMR modeled after this record" ng-click="confirmImport(rec, false)">
                         <input ng-show="rec.cmrNo.indexOf('P') == 0" type="button" class="cmr-grid-btn" value="Convert to Legal CMR" title="Request for conversion of this Prospect to Legal CMR" ng-click="confirmImport(rec, false)">
                         <input ng-show="rec.cmrNo.indexOf('P') != 0" type="button" class="cmr-grid-btn" value="Update CMR" title="Request for an Update of this CMR" ng-click="confirmImport(rec, true)">
                       </div>
                       <div ng-show="rec.recType == 'REQ'">
                         <img class="pdf" title="Export Request Details to PDF" ng-click="exportToPdf(rec)" src="${resourcesPath}/images/pdf-icon.png">
                       </div>
                     </td> 
                   </tr>
                   <tr>
                     <td colspan="7">
                       <input ng-show="!cmrNo && issuingCtry != '760' && issuingCntry != '641' && !orgIdSearch" type="button" value="Request CMR with Address" title="Request for a new CMR using the information specified under the search criteria." class="search-btn" ng-click="createNewCmr()">
                       <input type="button" value="Request CMR with blank data" title="Request for a new CMR with only CMR Issuing Country specified." class="search-btn" ng-click="confirmCreateNew()">
                     </td>
                   </tr>
                 </tbody>
               </table>
            </cmr:column>
          </cmr:row>
        </div>
        
        <cmr:row>
          &nbsp;
        </cmr:row>
          </div>
      </div>
  </form:form>
  <cmr:model model="search" />
</cmr:section>
</cmr:boxContent>
<form name="frmPDF" id="frmPDF" action="${contextPath}/request/pdf" method="POST" target="attachDlFrame">
  <input type="hidden" id="pdfReqId" name="reqId">
  <input type="hidden" id="pdfTokenId" name="tokenId">
</form>
<iframe id="attachDlFrame" style="display:none" name="attachDlFrame"></iframe>

<script src="${resourcesPath}/js/quick_search.js?${cmrv}"></script>
<jsp:include page="quick_search_modal.jsp" />
  