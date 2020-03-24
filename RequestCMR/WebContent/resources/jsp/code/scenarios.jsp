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
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
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
table.ibm-data-table {
  margin: 10px !important;
  width: 98% !important;
}

table.ibm-data-table caption {
  width: 99% !important;
}

table#scenarioTable {
  margin: 10px;
  
}

table#valueTable {
  width:100%;
}
table#valueTable td.addrlbl {
  width:55%;
}
table a {
  font-size:12px !important;
  cursor: pointer;
  text-decoration: underline;
}
img.add {
  width: 19px;
  height: 19px;
  font-size: 10px;
  text-align: center;
  cursor: pointer;
  vertical-align: sub;
}

img.remove {
  width: 19px;
  height: 19px;
  font-size: 10px;
  text-align: center;
  cursor: pointer;
}

img.info {
  width: 19px;
  height: 19px;
  font-size: 10px;
  text-align: center;
  cursor: help;
  vertical-align: sub;
}
img.up {
  width: 19px;
  height: 19px;
  font-size: 10px;
  text-align: center;
  cursor: pointer;
}

table#scenarioTable th, table#scenarioTable td{
  border-top : 1px Solid Gray;
  border-left: 1px Solid Gray;
  padding:5px;
  font-size:12px;
}
table#scenarioTable th {
  font-family:Calibri !important;
  font-size:13px;
}
table#scenarioTable tr:last-child td{
  border-bottom : 1px Solid Gray;
}

table#scenarioTable td:last-child, table#scenarioTable  th:last-child{
  border-right: 1px Solid Gray;
}
table#valueTable th, table#valueTable td{
  border : none;
}

table#valueTable tr:last-child td{
  border : none;
}

table#valueTable td:last-child, table#valueTable  th:last-child{
  border : none;
}

tr.odd {
  background: #EEEEEE;
}
tr.even {
  background: #FFFFFF;
}

input, select {
  border-radius : 2px;
  padding:1px;
  border: 1px Solid #DDDDDD;
}

input[type=button] {
  border-radius : 3px;
  padding:2px;
  border: 1px Solid #AAAAAA;
  height:25px;
  min-width:120px;
  background: #DDDDDD;
  cursor : pointer;
  margin-right: 10px;
}

input[type=button].ibm-btn-small {
  height : 40px;
}

div#addValueDiv {
  position: absolute;
  width: 350px;
  top:200px;
  left: 30%;
  min-height: 100px;
  border: 2px Solid #333333;
  z-Index: 9999;
  background: #BBBBBB;
  border-radius : 10px;
  padding:10px
}
div#addValueDiv table {
  width:100%;
}
div#addValueDiv table td, div#addValueDiv table th {
  padding:5px;
}

div#addTypeDiv {
  position: absolute;
  width: 500px;
  top:200px;
  left: 30%;
  min-height: 100px;
  border: 2px Solid #333333;
  z-Index: 9;
  background: #BBBBBB;
  border-radius : 10px;
  padding:10px
}
div#addTypeDiv table {
  width:100%;
}
div#addTypeDiv table td, div#addTypeDiv table th {
  padding:5px;
}
</style>
<div ng-app="ScenariosApp" ng-controller="ScenariosController">
  <cmr:boxContent>
    <cmr:tabs />

    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Customer Scenarios</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <table cellspacing="0" cellpadding="0" border="0" summary="Info"
          class="ibm-data-table ibm-sortable-table ibm-alternating">
          <thead>
            <tr>
              <th scope="col" width="18%">CMR Issuing Country:</th>
              <td>
                <select ng-model="issuingCntry" value="" ng-change="getTypes(issuingCntry)" ng-disabled="loaded" style="width:300px">
                  <option value=""></option>
                  <option ng-repeat="country in countries"  value="{{country.id}}" >{{country.id}} - {{country.name}}
                </select>
              </td>
            </tr>
            <tr>
              <th scope="col" width="18%">Scenario Type:</th>
              <td>
                <select ng-model="custType" ng-change="getSubTypes(issuingCntry, custType)" ng-disabled="loaded" style="width:300px">
                  <option ng-repeat="type in types" value="{{type.id}}">{{type.name}} ({{type.id}})
                </select>
                <img class="add" title="Add Type" ng-click="addType(issuingCntry)" src="${resourcesPath}/images/add.png" ng-show="issuingCntry && !loaded"> 
              </td>
            </tr>
            <tr>
              <th scope="col" width="18%">Scenario Sub-Type:</th>
              <td>
                <select ng-model="custSubType" ng-disabled="loaded" style="width:300px">
                  <option ng-repeat="subtype in subtypes" value="{{subtype.id}}">{{subtype.name}} ({{subtype.id}})
                </select>
                <img class="add" title="Add Sub Type" ng-click="addSubType(issuingCntry, custType)" src="${resourcesPath}/images/add.png" ng-show="issuingCntry && custType && !loaded"> 
              </td>
            </tr>
            <tr>
              <th scope="col"  colspan="2">
                <input type="button" value="Load Scenario" ng-disabled="!issuingCntry || !custType || !custSubType || loaded" ng-click="loadScenario(issuingCntry, custType, custSubType)"> 
                <input type="button" value="Select Another Scenario" ng-disabled="!loaded" ng-click="selectScenario()"> 
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
        <cmr:row topPad="20">
          <table id="scenarioTable" cellspacing="0" cellpadding="1" border="0" width="98%">
            <tr>
              <th width="4%">
                &nbsp;
              </th>
              <th width="23%">
                Field
              </th>
              <th width="14%">
                Address Type
              </th>
              <th width="*">
                Value(s)
              </th>
              <th width="10%">Retain Value</th>
              <th width="15%">Condition</th>
              <th width="15%">Locking</th>
            </tr>
            <tr ng-show="scenarios.length == 0">
              <td colspan="7">No fields to show</td>
            </tr>
            <tr ng-repeat="scenario in scenarios" class="{{$index%2 == 0 ? 'odd' : 'even'}}" title="{{scenario.fieldId}}" >
              <td>
                <img class="remove" title="Remove Row" ng-click="removeRow(scenario)" src="${resourcesPath}/images/remove.png"> 
                <img class="add" title="Propagate Field" ng-click="propagate(scenario)" src="${resourcesPath}/images/dist.png" ng-show="scenario.existing && scenario.values.length < 2"> 
              </td>
              <td>
                <select ng-model="scenario.internalId" ng-disabled="scenario.existing">
                  <option ng-repeat="field in allFields" value="{{field.internalId}}">{{field.lbl}}</option>
                </select>
                <img class="remove" title="Commit Field" ng-click="commitRow(scenario)" src="${resourcesPath}/images/check-icon.png" ng-show="!scenario.existing"> 
              </td>
              <td>
                {{addrMap[scenario.addrTyp]}}
              </td>
              <td>
                <span>
                  <span ng-repeat="value in scenario.values">
                    <div style="display:inline-block;width:80px">{{value}}</div>
                    <span>
                      <a ng-click="removeValue(scenario, value)" >Remove</a>
                    </span>
                    <br>
                  </span>
                  <span ng-show="scenario.existing">
                    <div style="display:inline-block;width:80px">&nbsp;</div>
                    <span>
                      <a ng-click="addValue(scenario)" >Add Value</a>
                    </span>
                  </span>
                </span>
              </td>
              <td>
                <input type="checkbox" ng-model="scenario.retainValInd" ng-disabled="scenario.values.length > 1">
              </td>
              <td>
                <select ng-model="scenario.reqInd" ng-disabled="scenario.values.length > 1">
                  <option value=""></option>
                  <option value="R">Required</option>
                  <option value="O">Optional</option>
                  <option value="D">Read Only</option>
                  <option value="G">Disabled</option>
                </select>
              </td>
              <td>
                <select ng-model="scenario.lockedIndc" ng-disabled="scenario.values.length > 1">
                  <option value=""></option>
                  <option value="Y">Always Locked</option>
                  <option value="R">Locked for Requesters</option>
                  <option value="P">Locked for Processors</option>
                  <option value="N">Always Editable</option>
                </select>
              </td>
            </tr>
            
          </table>
          
          <table id="scenarioTable" cellspacing="0" cellpadding="1" border="0" width="98%">
            <tr>
              <td>
                <input type="button" value="Add Row" ng-click="addRow()" ng-disabled="pending">
                <input type="button" value="Save Scenario" ng-click="saveScenarios()" ng-disabled="pending">
                <input type="button" value="Delete Scenario" ng-click="deleteScenario()" ng-disabled="pending">
              </td>
            </tr>
          </table>
          
        </cmr:row>
        
      </div>
        <div id="addValueDiv" style="display:none">
            <div >
            <table>
              <tr>
                <th colspan="2">Add Value</th>
              </tr>
              <tr id="addValueAddrTypeRow">
                <th>Address Type:</th>
                <td>
                  <select id="addValueType" ng-disabled="currentScenario != null && currentScenario.addrTyp != ''">
                    <option ng-repeat="type in addrTypes" value="{{type.id}}">{{type.name}}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>Value:</th>
                <td>
                  <input id="addValueVal" style="width:200px">
                </td>
              </tr>
              <tr>
                <td colspan="2">
                  <input type="button" value="Add" ng-click="saveValue()" >
                  <input type="button" value="Cancel" ng-click="hideAddValue()">
                </td>
              </tr>
            </table>
          </div>
        </div>


        <div id="addTypeDiv"  style="display:none">
            <div >
            <table>
              <tr>
                <th colspan="2">Add Type/Sub-type</th>
              </tr>
              <tr id="addValueAddrTypeRow">
                <th>Code:</th>
                <td>
                  <input id="typeCode" style="width:80px" maxlength="5">
                </td>
              </tr>
              <tr>
                <th>Name:</th>
                <td>
                  <input id="typeName" style="width:400px" maxlength="50">
                </td>
              </tr>
              <tr>
                <td colspan="2">
                  <input type="button" value="Add" ng-click="saveType()" >
                  <input type="button" value="Cancel" ng-click="hideAddType()">
                </td>
              </tr>
            </table>
          </div>
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
<script src="${resourcesPath}/js/system/scenariofields.js?${cmrv}"></script>
<script src="${resourcesPath}/js/system/scenariosmaint.js?${cmrv}"></script>
