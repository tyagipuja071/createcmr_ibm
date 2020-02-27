<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<script src="${resourcesPath}/js/system/system.js?${cmrv}"></script>
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<link rel="stylesheet" href="${resourcesPath}/css/auto_config.css?${cmrv}"/>

<div ng-app="ConfigApp" ng-controller="ConfigCntryController" ng-cloak>
<cmr:boxContent>
  <cmr:tabs />
  
  <cmr:section>

  <cmr:row>
    <cmr:column span="6">
      <h3>Automation Engine Configurations by Country</h3>
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="15">
    <cmr:column span="6">
          <div style="float:right">
            <span class="filter-lbl" style="font-size:12px; font-weight:bold; padding-right:6px">Search:</span>
            <input type="text" ng-model="countryFilter" style="width:250px">
          </div>
          <table border="0" cellpadding="1"  style="width:100%" class="element-table">
            <tr>
              <th width="5%"><input type="checkbox" ng-model="selectAll" ng-change="doSelectAll(selectAll)"></th>
              <th width="20%" class="left">Country</th>
              <th width="15%" class="left">Enablement</th>
              <th width="17%" class="left">Process on Completion</th>
              <th width="*" class="left">Automation Engine</th>
              <th width="10%" class="left">Exceptions</th>
              <th width="10%" class="left">Actions</th>
            </tr>
          </table>
        <div style="max-height:300px;overflow:auto">
          <table border="0" cellpadding="1"  style="width:100%" class="element-table">
            <tr ng-if="countries.length == 0">
              <td colspan="4">No countries mapped to configurations</td>
            </tr>
            <tr ng-repeat="country in countries | selectCountryFilter:countryFilter">
              <td width="5%"><input type="checkbox" ng-model="country.selected"></td>
              <td width="20%" class="left">{{country.cmrIssuingCntry.substring(0,3)}} - {{country.name}}</td>
              <td width="15%" class="left">
                <span ng-if="!country.edit">{{country.autoEngineIndcText}}</span>
                <select ng-if="country.edit  && country.cmrIssuingCntry.length == 3" ng-model="country.autoEngineIndc" style="width:90%">
                  <option value="">Disabled</option>
                  <option value="R">Requesters Only</option>
                  <option value="P">Processors Only</option>
                  <option value="B">Fully Enabled</option>
                </select>
                <span ng-if="country.edit && country.cmrIssuingCntry.length > 3">-configure from parent-</span>
              </td>
              <td width="17%" class="center">
            <!--     {{country.processOnCompletion}} --> 
             <span ng-if="!country.edit"> 
             <span ng-if="country.processOnCompletion == 'Y'">Yes</span>
             <span ng-if="country.processOnCompletion == ''">No</span>
             <span ng-if="country.processOnCompletion == 'N'">No</span>
              <span ng-if="country.processOnCompletion == 'U'">Updates Only</span>           
              <span ng-if="country.processOnCompletion == 'C'">Creates Only</span>              
              </span> 
                <select ng-if="country.edit" ng-model="country.processOnCompletion" style="width:90%">
                  <option value="N">No</option>
                  <option value="Y">Yes</option>
                  <option value="C">Creates Only</option>
                  <option value="U">Updates Only</option>
                </select>
              </td>
              <td width="*" class="left">
                <a ng-if="!country.edit" style="cursor:pointer" title="{{country.configDefn}}" href="maint?configId={{country.configId}}" >{{country.configId}}</a>
                <select ng-if="country.edit" ng-model="country.configId" style="width:90%">
                  <option ng-repeat="config in configIds" value="{{config.id}}">[{{config.id}}] - {{config.desc}}</option>
                </select>
              </td>
              <td width="10%" class="left">
                <a href="exceptions?cntry={{country.cmrIssuingCntry}}" ng-show="country.exceptions == 'Y'">Defined</a>
                <a href="exceptions?cntry={{country.cmrIssuingCntry}}" ng-show="country.exceptions != 'Y'">None</a>
              </td>
              <td width="10%"" class="left">
                <img ng-show="!country.edit" src="${resourcesPath}/images/addr-edit-icon.png" title="Edit" class="auto-icon" ng-click="editCountry(country)">            
                <img ng-show="country.edit" src="${resourcesPath}/images/refresh.png" title="Undo" class="auto-icon" ng-click="undoEditCountry(country)">            
                <img ng-show="country.edit" src="${resourcesPath}/images/reject.png" title="Undo" class="auto-icon" ng-click="deleteCountry(country)">            
                <img ng-show="country.edit" src="${resourcesPath}/images/save.png" title="Save" class="auto-icon" ng-click="saveCountry(country)">            
              </td>
            </tr>
          </table>
        </div>
        <br>
        <a ng-show="!editEnablement && !editMapping && !editProcessOnCompletion" class="auto-action" ng-click="unmapCountries()" style="cursor:pointer">Unmap Countries</a>
        <a ng-show="!editEnablement && !editMapping && !editProcessOnCompletion" class="auto-action pad" ng-click="changeEnablement()" style="cursor:pointer">Change Enablement</a>
        <a ng-show="!editEnablement && !editMapping && !editProcessOnCompletion" class="auto-action pad" ng-click="changeProcessOnCompletion()" style="cursor:pointer">Change Process on Completion</a>
        <a ng-show="!editEnablement && !editMapping && !editProcessOnCompletion" class="auto-action pad" ng-click="mapToEngine()" style="cursor:pointer">Map to Automation Engine</a>
          
    </cmr:column>
  </cmr:row>
  <cmr:row >
    &nbsp;
  </cmr:row>

  <div ng-show="editEnablement">
    <cmr:row addBackground="true">
      &nbsp;
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="6">
          <span class="filter-lbl" style="font-size:12px; font-weight:bold; padding-right:6px">Change Automation Engine enablement of selected countries to:</span>
          <select ng-model="enablementVal" style="width:150px" class="auto-select">
            <option value="">Disabled</option>
            <option value="R">Requesters Only</option>
            <option value="P">Processors Only</option>
            <option value="B">Fully Enabled</option>
          </select>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true" topPad="10">
      <cmr:column span="6">
        <a class="auto-action" ng-click="undoEdit()" style="cursor:pointer">Undo</a>
        <a class="auto-action auto-h" ng-click="saveEnablement()" style="cursor:pointer">Save</a>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      &nbsp;
    </cmr:row>
  </div>

  <div ng-show="editMapping">
    <cmr:row addBackground="true">
      &nbsp;
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="6">
          <span class="filter-lbl" style="font-size:12px; font-weight:bold; padding-right:6px">Change Automation Engine mapping of selected countries to:</span>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="6">
         <select style="width:90%" class="auto-select" ng-model="configMappingVal">
           <option value="">&nbsp;</option>
           <option ng-repeat="config in configIds" value="{{config.id}}">[{{config.id}}] - {{config.desc}}</option>
         </select>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true" topPad="10">
      <cmr:column span="6">
        <a class="auto-action" ng-click="undoEdit()" style="cursor:pointer">Undo</a>
        <a class="auto-action auto-h" ng-click="saveMapping()" style="cursor:pointer">Save</a>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      &nbsp;
    </cmr:row>
  </div>

  <div ng-show="editProcessOnCompletion">
    <cmr:row addBackground="true">
      &nbsp;
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="6">
          <span class="filter-lbl" style="font-size:12px; font-weight:bold; padding-right:6px">Change Process on Completion setting of selected countries to:</span>
          <select ng-model="processOnCompletionVal" style="width:80px" class="auto-select">
            <option value="N">No</option>
            <option value="Y">Yes</option>
            <option value="C">Creates Only</option>
            <option value="U">Updates Only</option>           
          </select>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true" topPad="10">
      <cmr:column span="6">
        <a class="auto-action" ng-click="undoEdit()" style="cursor:pointer">Undo</a>
        <a class="auto-action auto-h" ng-click="saveProcessOnCompletion()" style="cursor:pointer">Save</a>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      &nbsp;
    </cmr:row>
  </div>


  </cmr:section>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="New Configuration"
      onClick="goToUrl('${contextPath}/auto/config/maint')" highlight="true"/>
    <cmr:button label="Maintain by Engine Configuration"
      onClick="goToUrl('${contextPath}/auto/config/list')" pad="true"/>
    <cmr:button label="Scenario Exceptions"
      onClick="goToUrl('${contextPath}/auto/config/exceptions')" pad="true"/>
    <cmr:button label="Back to Code Maintenance Home"
      onClick="backToCodeMaintHome()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>
</div>
<script src="${resourcesPath}/js/auto/auto_config.js?${cmrv}"></script>
