<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
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
<style>
div.text {
	font-size: 15px;
	font-weight: bold;
}

div.lbl {
	font-size: 13px;
	display: inline-block;
	font-weight: bold;
}

table.ibm-data-table th, table.ibm-data-table td, table.ibm-data-table a,
	.ibm-type table caption em {
	letter-spacing: 1px;
	font-size: 12px;
	word-break: break-word;
}

table.ibm-data-table td:NTH-CHILD(3) {
	font-size: 12px;
}

th.subhead {
	font-size: 12px;
	text-transform: uppercase;
}

div.code-filter {
	float: right;
	font-size: 12px;
	font-family: IBM Plex Sans, Calibri;
	text-transform: uppercase;
	font-weight: bold;
	margin-right: 10px;
}

div.code-filter input {
	font-size: 12px;
	font-family: IBM Plex Sans, Calibri;
	margin-left: 4px;
}

img.icon-e {
	cursor: pointer;
}

td.field {
	font-weight: bold;
}

span.mod {
	font-weight: bold;
	color: blue;
}

td.mod {
	background: antiquewhite !important;
}

div.buttons {
	padding: 10px;
}

button.save {
  height: 30px;
  font-weight: bold;
}

button.action {
	border: 1px Solid black;
	border-radius: 4px;
	padding-left: 10px;
	padding-right: 10px;
  background: #cfe7fa; /* Old browsers */
  background: -moz-linear-gradient(top,  #cfe7fa 0%, #6393c1 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top,  #cfe7fa 0%,#6393c1 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom,  #cfe7fa 0%,#6393c1 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#cfe7fa', endColorstr='#6393c1',GradientType=0 ); /* IE6-9 */
  cursor: pointer;
}
button.action:hover {
  background: #c3d9ff; /* Old browsers */
  background: -moz-linear-gradient(top,  #c3d9ff 0%, #b1c8ef 41%, #98b0d9 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top,  #c3d9ff 0%,#b1c8ef 41%,#98b0d9 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom,  #c3d9ff 0%,#b1c8ef 41%,#98b0d9 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#c3d9ff', endColorstr='#98b0d9',GradientType=0 ); /* IE6-9 */
}
div.buttons {
 float:right;
}
[ng\:cloak], [ng-cloak], [data-ng-cloak], [x-ng-cloak], .ng-cloak, .x-ng-cloak {
  display: none !important;
}
</style>
<div ng-app="CorrectionsApp" ng-controller="CorrectionsController" ng-cloak>

  <cmr:boxContent>
    <cmr:tabs />

    <cmr:section id="CORRECTIONS">
      <cmr:row>
        <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
        <cmr:note
          text="This function should only be used by CMDE Administrators to do minor tweaks on request or CMR data. Please exercise caution when modifying values on the selected records. Note that knowledge on the DB fields and actual values is needed since modifications here are all in PLAIN TEXT."></cmr:note>

      </cmr:row>
      <div ng-show="!current">
      <cmr:row topPad="10">
        <cmr:column span="6">
          <div class="text">Please choose what type of record you want to do corrections for:</div>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="200">
          <input type="radio" ng-model="model.correctionType" name="correctionType" value="R">
          <div class="lbl" ng-click="model.correctionType = 'R'" style="cursor:pointer">CMR Request</div>
        </cmr:column>
        <cmr:column span="1" width="200">
          <input type="radio" ng-model="model.correctionType" name="correctionType" value="L">
          <div class="lbl" ng-click="model.correctionType = 'L'" style="cursor:pointer">DB2 Record</div>
        </cmr:column>
        <cmr:column span="1" width="200">
          <input type="radio" ng-model="model.correctionType" name="correctionType" value="P">
          <div class="lbl" ng-click="model.correctionType = 'P'" style="cursor:pointer">PayGo Addresses</div>
        </cmr:column>
      </cmr:row>
      <cmr:row>
               &nbsp;
      </cmr:row>

      <cmr:row topPad="10" addBackground="true">
        <div ng-show="model.correctionType == 'R'">
          <cmr:column span="1" width="300">
            <div class="lbl">Request ID:</div>
            <input ng-model="model.reqId">
          </cmr:column>
          <cmr:column span="2">
            <button ng-show="!current" class="action" ng-click="retrieveDetails()">Retrieve Details</button>
          </cmr:column>
        </div>
        <div ng-show="model.correctionType == 'L'">
          <cmr:column span="1" width="400">
            <div class="lbl">Issuing Country:</div>
                <select ng-model="model.cmrIssuingCntry" value=""  style="width:252px">
                  <option value=""></option>
                  <option ng-repeat="country in countries"  value="{{country.id}}" >{{country.id}} - {{country.name}}</option>
                </select>
          </cmr:column>
          <cmr:column span="1" width="280">
            <div class="lbl">CMR No.:</div>
            <input ng-model="model.cmrNo">
          </cmr:column>
          <cmr:column span="2">
            <button ng-show="!current" class="action" ng-click="retrieveDetails()">Retrieve Details</button>
          </cmr:column>
        </div>
        <div ng-show="model.correctionType == 'P'">
          <cmr:column span="1" width="300">
            <div class="lbl">Request ID:</div>
            <input ng-model="model.reqId">
          </cmr:column>
          <cmr:column span="2">
            <button ng-show="!current" class="action" ng-click="confirmFixPayGoAddresses()">Process Corrections</button>
          </cmr:column>
        </div>
      </cmr:row>

      </div>
      <cmr:row addBackground="true">
             &nbsp;
            <button ng-show="current" class="action" ng-click="loadAnother()">Load Another Record</button>
      </cmr:row>
    </cmr:section>

  </cmr:boxContent>



  <!-- Request Section -->
  <div ng-show="model.correctionType == 'R' && current">
    <cmr:boxContent>
      <cmr:tabs />

      <cmr:section id="REQUESTS">
        <cmr:row addBackground="true">
          <div class="text">Current Values for Request {{current.admin.id.reqId}}</div>
        </cmr:row>
        <cmr:row topPad="10">
          <cmr:column span="3" width="500">
            <div style="height: 300px; overflow-y: auto">
              <table id="adminTable" cellspacing="0" cellpadding="0" border="0" summary="ADMIN"
                class="ibm-data-table ibm-sortable-table ibm-alternating" width="100%">
                <caption>
                  <em> <strong>ADMIN</strong>
                    <div class="code-filter">
                      Filter: <input ng-model="adminFilter" style="width: 150px" maxlength="15" placeholder="Filter">
                    </div>
                  </em>
                </caption>
                <thead>
                  <tr>
                    <th scope="col" width="40%">Field</th>
                    <th scope="col" width="*">Value</th>
                  </tr>
                </thead>
                <tbody>
                  <tr ng-repeat="rec in admin | filter:adminFilter">
                    <td class="field">{{rec.field}}</td>
                    <td class="{{rec.value != rec.curr ? 'mod' : ''}}" title="Current Value: {{rec.curr}}">
                      <span ng-show="!rec.edit" class="{{rec.value != rec.curr ? 'mod' : ''}}">{{rec.value}}</span> 
                      <input ng-show="rec.edit" ng-model="rec.value">

                      <div style="float: right" ng-show="!rec.noEdit">
                        <img src="${resourcesPath}/images/edit2.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = true" ng-show="!rec.edit" title="Change Value"> 
                        <img
                          src="${resourcesPath}/images/check.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = false" ng-show="rec.edit" title="Save Value">
                      </div></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </cmr:column>
          <cmr:column span="1" width="30">
          </cmr:column>

          <cmr:column span="3" width="500">
            <div style="height: 300px; overflow-y: auto">
              <table id="reqDataTable" cellspacing="0" cellpadding="0" border="0" summary="DATA"
                class="ibm-data-table ibm-sortable-table ibm-alternating" width="100%">
                <caption>
                  <em> <strong>DATA</strong>
                    <div class="code-filter">
                      Filter: <input ng-model="dataFilter" style="width: 150px" maxlength="15" placeholder="Filter">
                    </div>
                  </em>
                </caption>
                <thead>
                  <tr>
                    <th scope="col" width="40%">Field</th>
                    <th scope="col" width="*">Value</th>
                  </tr>
                </thead>
                <tbody>
                  <tr ng-repeat="rec in data | filter:dataFilter">
                    <td class="field">{{rec.field}}</td>
                    <td class="{{rec.value != rec.curr ? 'mod' : ''}}" title="Current Value: {{rec.curr}}">
                      <span ng-show="!rec.edit" class="{{rec.value != rec.curr ? 'mod' : ''}}">{{rec.value}}</span> 
                      <input ng-show="rec.edit" ng-model="rec.value">

                      <div style="float: right" ng-show="!rec.noEdit">
                        <img src="${resourcesPath}/images/edit2.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = true" ng-show="!rec.edit" title="Change Value"> 
                        <img
                          src="${resourcesPath}/images/check.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = false" ng-show="rec.edit" title="Save Value">
                      </div></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </cmr:column>

        </cmr:row>
        <cmr:row addBackground="true">
          &nbsp;
        </cmr:row>
        <cmr:row topPad="10">
          <cmr:column span="6">
            <div style="max-height: 300px; width: 100%; overflow: auto">
              <table id="reqDataTable" cellspacing="0" cellpadding="0" border="0" summary="ADDRESSES"
                class="ibm-data-table ibm-sortable-table ibm-alternating" style="width: 2500px !important">
                <caption>
                  <em> <strong>ADDRESSES</strong>
                    <div class="code-filter" style="float: none; display: inline-block; margin-left: 30px">
                      Filter: <input ng-model="addrFilter" style="width: 150px" maxlength="15" placeholder="Filter">
                    </div>
                  </em>
                </caption>
                <thead>
                  <tr>
                    <th scope="col" width="5%">ADDR_TYPE</th>
                    <th scope="col" width="5%">ADDR_SEQ</th>
                    <th scope="col" width="10%">CUST_NM1</th>
                    <th scope="col" width="9%">CUST_NM2</th>
                    <th scope="col" width="10%">ADDR_TXT</th>
                    <th scope="col" width="8%">ADDR_TXT_2</th>
                    <th scope="col" width="5%">CITY1</th>
                    <th scope="col" width="6%">STATE_PROV</th>
                    <th scope="col" width="7%">POST_CD</th>
                    <th scope="col" width="8%">CUST_NM3</th>
                    <th scope="col" width="8%">CUST_NM4</th>
                    <th scope="col" width="8%">DIVN</th>
                    <th scope="col" width="7%">DEPT</th>
                    <th scope="col" width="4%%">IMPORT_IND</th>
                  </tr>
                </thead>
                <tbody>
                  <tr ng-repeat="addr in addresses | filter:addrFilter">
                    <td class="{{rec.value != rec.curr ? 'mod' : ''}}" ng-repeat="rec in addr" title="Current Value: {{rec.curr}}">
                      <span ng-show="!rec.edit" class="{{rec.value != rec.curr ? 'mod' : ''}}">{{rec.value}}</span> 
                      <input ng-show="rec.edit" ng-model="rec.value" style="width: 60%">

                      <div style="float: right" ng-show="!rec.noEdit">
                        <img src="${resourcesPath}/images/edit2.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = true" ng-show="!rec.edit" title="Change Value"> 
                        <img
                          src="${resourcesPath}/images/check.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = false" ng-show="rec.edit" title="Save Value">
                      </div></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </cmr:column>
        </cmr:row>
      </cmr:section>
    </cmr:boxContent>
  </div>
  <!-- End Request Section -->


  <!-- CMR Section -->
  <div ng-show="model.correctionType == 'L' && current">
    <cmr:boxContent>
      <cmr:tabs />

      <cmr:section id="CMR">
        <cmr:row addBackground="true">
          <div class="text">Current Values for CMR {{current.cust.realCtyCd}} - {{current.cust.id.customerNo}}</div>
        </cmr:row>
        <cmr:row topPad="10">
          <cmr:column span="3" width="500">
            <div style="height: 300px; overflow-y: auto">
              <table id="custTable" cellspacing="0" cellpadding="0" border="0" summary="CMRTCUST"
                class="ibm-data-table ibm-sortable-table ibm-alternating" width="100%">
                <caption>
                  <em> <strong>CMRTCUST</strong>
                    <div class="code-filter">
                      Filter: <input ng-model="custFilter" style="width: 150px" maxlength="15" placeholder="Filter">
                    </div>
                  </em>
                </caption>
                <thead>
                  <tr>
                    <th scope="col" width="40%">Field</th>
                    <th scope="col" width="*">Value</th>
                  </tr>
                </thead>
                <tbody>
                  <tr ng-repeat="rec in cust | filter:custFilter">
                    <td class="field">{{rec.field}}</td>
                    <td class="{{rec.value != rec.curr ? 'mod' : ''}}" title="Current Value: {{rec.curr}}">
                      <span ng-show="!rec.edit" class="{{rec.value != rec.curr ? 'mod' : ''}}">{{rec.value}}</span> 
                      <input ng-show="rec.edit" ng-model="rec.value">

                      <div style="float: right" ng-show="!rec.noEdit">
                        <img src="${resourcesPath}/images/edit2.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = true" ng-show="!rec.edit" title="Change Value"> 
                        <img
                          src="${resourcesPath}/images/check.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = false" ng-show="rec.edit" title="Save Value">
                      </div></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </cmr:column>
          <cmr:column span="1" width="30">
          </cmr:column>

          <cmr:column span="3" width="500">
            <div style="height: 300px; overflow-y: auto">
              <table id="extTable" cellspacing="0" cellpadding="0" border="0" summary="CMRTEXT"
                class="ibm-data-table ibm-sortable-table ibm-alternating" width="100%">
                <caption>
                  <em> <strong>CMRTEXT</strong>
                    <div class="code-filter">
                      Filter: <input ng-model="extFilter" style="width: 150px" maxlength="15" placeholder="Filter">
                    </div>
                  </em>
                </caption>
                <thead>
                  <tr>
                    <th scope="col" width="40%">Field</th>
                    <th scope="col" width="*">Value</th>
                  </tr>
                </thead>
                <tbody>
                  <tr ng-repeat="rec in custExt | filter:extFilter">
                    <td class="field">{{rec.field}}</td>
                    <td class="{{rec.value != rec.curr ? 'mod' : ''}}" title="Current Value: {{rec.curr}}">
                      <span ng-show="!rec.edit" class="{{rec.value != rec.curr ? 'mod' : ''}}">{{rec.value}}</span> 
                      <input ng-show="rec.edit" ng-model="rec.value">

                      <div style="float: right" ng-show="!rec.noEdit">
                        <img src="${resourcesPath}/images/edit2.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = true" ng-show="!rec.edit" title="Change Value"> 
                        <img
                          src="${resourcesPath}/images/check.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = false" ng-show="rec.edit" title="Save Value">
                      </div></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </cmr:column>

        </cmr:row>
        <cmr:row addBackground="true">
          &nbsp;
        </cmr:row>
        <cmr:row topPad="10">
          <cmr:column span="6">
            <div style="max-height: 300px; width: 100%; overflow: auto">
              <table id="reqDataTable" cellspacing="0" cellpadding="0" border="0" summary="ADDRESSES"
                class="ibm-data-table ibm-sortable-table ibm-alternating" style="width: 4000px !important">
                <caption>
                  <em> <strong>CMR ADDRESSES</strong>
                    <div class="code-filter" style="float: none; display: inline-block; margin-left: 30px">
                      Filter: <input ng-model="custAddrFilter" style="width: 150px" maxlength="15" placeholder="Filter">
                    </div>
                  </em>
                </caption>
                <thead>
                  <tr>
                    <th scope="col">ADDRNO</th>
                    <th scope="col">ADDRL1</th>
                    <th scope="col">ADDRL2</th>
                    <th scope="col">ADDRL3</th>
                    <th scope="col">ADDRL4</th>
                    <th scope="col">ADDRL5</th>
                    <th scope="col">ADDRL6</th>
                    <th scope="col">ADDRMAIL</th>
                    <th scope="col">ADDRBILL</th>
                    <th scope="col">ADDRINST</th>
                    <th scope="col">ADDRSHIP</th>
                    <th scope="col">ADDREPL</th>
                    <th scope="col">ADDRLIT</th>
                    <th scope="col">ADDRLI</th>
                    <th scope="col">ADDRLN</th>
                    <th scope="col">ADDRLO</th>
                    <th scope="col">ADDRLT</th>
                    <th scope="col">ADDRLU</th>
                    <th scope="col">SIGLAPROV</th>
                    <th scope="col">DESVAR</th>
                    <th scope="col">ADDRUSEA</th>
                    <th scope="col">ADDRUSEB</th>
                    <th scope="col">ADDRUSEC</th>
                    <th scope="col">ADDRUSED</th>
                    <th scope="col">ADDRUSEE</th>
                    <th scope="col">ADDRUSEF</th>
                    <th scope="col">ADDRUSEG</th>
                    <th scope="col">ADDRUSEH</th>
                  </tr>
                </thead>
                <tbody>
                  <tr ng-repeat="addr in custAddresses | filter:custAddrFilter">
                    <td class="{{rec.value != rec.curr ? 'mod' : ''}}" ng-repeat="rec in addr" title="Current Value: {{rec.curr}}">
                      <span ng-show="!rec.edit" class="{{rec.value != rec.curr ? 'mod' : ''}}">{{rec.value}}</span> 
                      <input ng-show="rec.edit" ng-model="rec.value" style="width: 60%">

                      <div style="float: right" ng-show="!rec.noEdit">
                        <img src="${resourcesPath}/images/edit2.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = true" ng-show="!rec.edit" title="Change Value"> 
                        <img
                          src="${resourcesPath}/images/check.png" class="cmr-error-icon icon-e"
                          ng-click="rec.edit = false" ng-show="rec.edit" title="Save Value">
                      </div></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </cmr:column>
        </cmr:row>
      </cmr:section>
    </cmr:boxContent>
  </div>
  <!-- End Request Section -->


  <!-- BUTTONS -->
  <div ng-show="current">
    <cmr:boxContent>
      <cmr:tabs />

      <cmr:section id="BUTTONS">
        <cmr:row addBackground="true">
          <div class="buttons">
            <button class="action save" onclick="confirmSave()">Save All Changes</button>
          </div>
        </cmr:row>
      </cmr:section>
    </cmr:boxContent>
  </div>

  <!-- End Controller -->
</div>
<script src="${resourcesPath}/js/system/corrections.js?${cmrv}"></script>
