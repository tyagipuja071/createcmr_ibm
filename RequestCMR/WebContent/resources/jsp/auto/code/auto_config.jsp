<%@page import="org.apache.commons.lang.StringUtils"%>
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
<link rel="stylesheet" href="${resourcesPath}/css/auto_config.css?${cmrv}"/>

<div ng-app="ConfigApp" ng-controller="ConfigController" ng-cloak>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:section>
    <cmr:row>
      <cmr:column span="6">
        <h3>Automation Engine Configuration
        <%if (!newConfig){ %>
          - <%=id%>
        <%}%>
        </h3>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="6">
      
        <div ng-if="!existing" >
          <table border="0" class="meta-table" width="100%">
            <tr>
              <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
              <th>Configuration ID: <cmr:info text="Accepts only letters and underscores."/></th>
              <td><input ng-model="config.configId" maxlength="15"></td>
            </tr>
            <tr>
              <th>Description:</th>
              <td>
                <textarea ng-model="config.configDefn" rows="8" cols="100" class="auto-desc"></textarea>
              </td>
            </tr>
            <tr>
              <td colspan="2">
                &nbsp;
              </td>
            </tr>
            <tr>
              <th>Copy From: <cmr:info text="If specified, the elements defined on the chosen configuration will automatically be copied to the new one."/></th>
              <td>
                <select class="auto-select" ng-model="config.copyFrom" style="width:650px">
                  <option ng-repeat="config in configIds" value="{{config.id}}">[{{config.id}}] - {{config.desc}}</option>
                </select>
              </td>
            </tr>
            <tr>
              <td colspan="2">&nbsp;</td>
            </tr>
          </table>
          <br>
          <a class="auto-action auto-h" ng-click="saveConfig()" style="cursor:pointer">Save Configuration<img src="${resourcesPath}/images/save.png" title="Save Description" class="auto-icon"></a>
        </div>
        <div ng-if="config.edit" >
          <textarea ng-if="config.edit" ng-model="config.configDefn" rows="8" cols="120" class="auto-desc"></textarea>
          <br>
          <br>
          <a class="auto-action" ng-click="saveConfig()" style="cursor:pointer">Save Description<img src="${resourcesPath}/images/save.png" title="Save Description" class="auto-icon"></a>
          <a class="auto-action" ng-click="undoSaveConfig()" style="cursor:pointer">Undo</a>
        </div>
  
        <div ng-if="existing && !config.edit" >
          <div ng-bind-html="config.configDefnHtml" class="auto-desc"></div>
          <br>
          <table border="0" style="width:70%" class="meta-table">
            <tr>
              <th>Created By:</th>
              <td>{{config.createBy}}</td>
              <th>Last Updated By:</th>
              <td>{{config.lastUpdtBy}}</td>
            </tr>
            <tr>
              <th>Create Date:</th>
              <td>{{config.createTs}}</td>
              <th>Last Update Date:</th>
              <td>{{config.lastUpdtTs}}</td>
            </tr>
          </table>
          <br>
          <a class="auto-action" ng-show="existing" ng-click="editConfig()" style="cursor:pointer">Edit Description<img src="${resourcesPath}/images/addr-edit-icon.png" title="Edit Description" class="auto-icon"></a>
          
          <a class="auto-action" style="margin-left:10px; color:red" ng-show="existing" ng-click="deleteConfig()" style="cursor:pointer">Delete Configuration<img src="${resourcesPath}/images/remove.png" title="Remove Configuration" class="auto-icon"></a>
        </div>
  
      </cmr:column>
    </cmr:row>  
    
    <cmr:row>
      &nbsp;
    </cmr:row>
  </cmr:section>
</cmr:boxContent>

<div ng-if="existing">
<cmr:boxContent>
  <cmr:tabs />
  <cmr:section>
    <cmr:row addBackground="true">
      <cmr:column span="6">
        <h3>Automation Elements</h3>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="15" addBackground="true">
      <cmr:column span="6">
        <table border="0" cellpadding="1"  style="width:100%" class="element-table">
          <tr>
            <th width="5%">Order</th>
            <th width="*" class="left">Process</th>
            <th width="15%">Request Types</th>
            <th width="12%">Action on Error</th>
            <th width="10%">Override Data</th>
            <th width="10%">Stop on Error</th>
            <th width="9%">Active</th>
            <th width="12%">Actions</th>
          </tr>
          <tr ng-if="elements.length == 0">
            <td colspan="8">No elements defined yet</td>
          </tr>
          <tr ng-repeat="rec in elements">
            <td>{{$index + 1}}</td>
            <td class="left">
              <span ng-if="rec.existing">
                <span ng-class="rec.processPrefix == 'GBL' ? 'prefix-gbl' : 'prefix-cntry'">
                  {{rec.processPrefix}}
                </span>
                {{rec.processDesc}}
              </span>
              <select ng-model="rec.processCd" ng-if="!rec.existing">
                <option ng-repeat="elem in autoElements" value="{{elem.processCd}}">{{elem.processDesc}}</option>
              </select>
              <img ng-show="!rec.existing" src="${resourcesPath}/images/approve.png" title="Select" class="auto-icon" ng-click="selectElement($index, rec)">            
            </td>
            <td>
              <select ng-model="rec.requestTyp">
                <option value="C">Create</option>
                <option value="U">Update</option>
                <option value="CU">Create/Update</option>
              </select>
            </td>
            <td>
              <span ng-if="rec.processType == 'A'" style="font-size:13px">Wait</span>
              <select ng-if="rec.processType != 'A'" ng-model="rec.actionOnError" ng-class="rec.actionOnError == 'R' ? 'cred' : ''">
                <option value="">Proceed</option>
                <option value="I">Ignore</option>
                <option value="R">Reject</option>
              </select>
            </td>
            <td>
              <input ng-if="!rec.nonImportable && (rec.processType == 'D' || rec.processType == 'M')" type="checkbox" ng-model="rec.overrideDataIndc" ng-class="rec.overrideDataIndc ? 'chk-warn' : 'chk-go'">
            </td>
            <td>
              <input type="checkbox" ng-model="rec.stopOnErrorIndc" ng-class="rec.stopOnErrorIndc ? 'chk-warn' : 'chk-go'">
            </td>
            <td>
              <input type="checkbox" ng-model="rec.status" ng-class="!rec.status ? 'chk-warn' : 'chk-go'">
            </td>
            <td>
              <img src="${resourcesPath}/images/reject.png" title="Delete" class="auto-icon" ng-click="removeElement($index, rec)">            
              <img ng-show="rec.existing && $index != 0" src="${resourcesPath}/images/up.png" title="Move Up" class="auto-icon" ng-click="moveElementUp($index, rec)">            
              <img ng-show="!rec.existing || (rec.existing && $index == 0)" src="${resourcesPath}/images/up.png" title="Move Up" class="auto-icon" style="visibility:hidden">            
            </td>
          </tr>
        </table>
        <br>
        <a class="auto-action" ng-click="addElement()" style="cursor:pointer">Add Element<img src="${resourcesPath}/images/add.png" title="Edit Description" class="auto-icon"></a>
        <a class="auto-action auto-h" ng-click="saveElements()" style="cursor:pointer">Save Configuration<img src="${resourcesPath}/images/save.png" title="Save Configuration" class="auto-icon"></a>
        
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      &nbsp;
    </cmr:row>
  </cmr:section>
</cmr:boxContent>

<cmr:boxContent>
  <cmr:tabs />
  <cmr:section>
    <cmr:row>
      <cmr:column span="6">
        <h3>Automation Engine - Countries</h3>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="15">
      <cmr:column span="6">
          <div style="float:right">
          <span class="filter-lbl" style="font-size:12px; font-weight:bold; padding-right:6px">Search:</span>
          <input type="text" ng-model="countryFilter" style="width:250px">
          </div>
          <table border="0" cellpadding="1"  style="width:85%" class="element-table">
            <tr>
              <th width="5%"><input type="checkbox" ng-model="selectAll" ng-change="doSelectAll(selectAll)"></th>
              <th width="*" class="left">Country</th>
              <th width="20%" class="left">System GEO Code</th>
              <th width="22%" class="left">Process On Completion</th>
              <th width="18%" class="left">Enablement</th>
              <th width="10%" class="left">Exceptions</th>
            </tr>
          </table>
        <div style="max-height:300px;overflow:auto">
          <table border="0" cellpadding="1"  style="width:85%" class="element-table">
            <tr ng-if="countries.length == 0">
              <td colspan="4">No countries mapped yet</td> 
            </tr>
            <tr ng-repeat="country in countries | selectCountryFilter:countryFilter">
              <td width="5%"><input type="checkbox" ng-model="country.selected"></td>
              <td width="*" class="left">{{country.cmrIssuingCntry.substring(0,3)}} - {{country.name}}</td>
              <td width="20%" class="left">{{country.geo}}</td>
              <td width="22%" class="left">{{formatProcessOnCompletion(country.processOnCompletion)}}</td>
              <td width="18%" class="left">
                <a href="${contextPath}/code/addsuppcountrypage/?cntryCd={{country.cmrIssuingCntry.substring(0,3)}}">{{country.enablement}}
                <span ng-show="country.cmrIssuingCntry.length > 3" style="font-size:10px"><br>(from parent {{country.cmrIssuingCntry.substring(0,3)}})</span></a>
              </td>
              <td width="10%" class="left">
                <a href="exceptions?cntry={{country.cmrIssuingCntry}}" ng-show="country.exceptions == 'Y'">Defined</a>
                <a href="exceptions?cntry={{country.cmrIssuingCntry}}" ng-show="country.exceptions != 'Y'">None</a>
              </td>
            </tr>
          </table>
        </div>
        <br>
        <a ng-if="!mapping && !editProcessOnCompletion" class="auto-action" ng-click="removeCountries()" style="cursor:pointer">Remove Countries from Config</a>
        <a ng-if="!mapping && !editProcessOnCompletion" class="auto-action pad" ng-click="changeProcessOnCompletion(true)" style="cursor:pointer">Change Process On Completion</a>
        <a ng-if="!mapping && !editProcessOnCompletion" class="auto-action auto-h" ng-click="mapCountries()" style="cursor:pointer">Map Other Countries</a>
        <span ng-if="!mapping && editProcessOnCompletion" style="font-size:14px; font-weight:bold; padding-right:6px">Choose Process On Completion for selected countries:</span>
        <select ng-show="!mapping && editProcessOnCompletion" ng-model="processOnCompletionVal" style="width:25%" class="auto-select pad">
            <option value="N">No</option>
            <option value="Y">Yes</option>
            <option value="C">Creates Only</option>
            <option value="U">Updates Only</option>           
          </select>
        <a ng-if="!mapping && editProcessOnCompletion" class="auto-action pad" ng-click="updateCountries(processOnCompletionVal)" style="cursor:pointer">Save Process On Completion</a>
        <a ng-if="!mapping && editProcessOnCompletion" class="auto-action pad" ng-click="changeProcessOnCompletion(false)" style="cursor:pointer">Undo</a>
        
        
        
      </cmr:column>
      <div ng-show="mapping" class="mapping-panel" style="padding-bottom:0;display:block">
        <span class="filter-lbl" style="font-size:12px; font-weight:bold; padding-right:6px">Filter Country:</span>
        <input type="text" ng-model="mapFilter" style="width:250px">
      </div>
      <div ng-if="mapping" class="mapping-panel">
        <div class="to-add-panel">
          <div ng-repeat="cntry in toAdd | filter:mapFilter" class="to-add-choice" title="Map to Configuration" ng-click="mapCountry($index, cntry)">
            {{cntry.cmrIssuingCntry.substring(0,3)}} - {{cntry.name}}
            <img src="${resourcesPath}/images/move-right.png" style="float:right" title="Map to Configuration" class="auto-icon-2">
          </div>
        </div>
        <div class="to-map-panel">
          <div ng-repeat="cntry in toMap" class="to-map-choice" ng-click="unmapCountry($index, cntry)">
            <img src="${resourcesPath}/images/move-left.png" style="padding-right:3px" title="Unmap" class="auto-icon-2">
            {{cntry.cmrIssuingCntry.substring(0,3)}} - {{cntry.name}}
          </div>
        </div>
        <div class="to-map-panel" style="border:0">
        <span style="font-size:14px; font-weight:bold; padding-right:6px">Choose Process On Completion for selected countries:</span>
          <select ng-model="processOnCompletionVal" style="width:100%" class="auto-select">
            <option value="N">No</option>
            <option value="Y">Yes</option>
            <option value="C">Creates Only</option>
            <option value="U">Updates Only</option>           
          </select>
          <br><br>
        <a class="auto-action" ng-click="assignCountries(processOnCompletionVal)" style="cursor:pointer">Map Selected Countries</a>
          
          <a class="auto-action pad" ng-click="undoMap()" style="cursor:pointer">Cancel</a>
        </div>
      </div>
    </cmr:row>
    <cmr:row>
      &nbsp;
    </cmr:row>
  </cmr:section>
</cmr:boxContent>


</div>

<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Back to Configuration List"
      onClick="goToUrl('${contextPath}/auto/config/list')" />
    <cmr:button label="Maintain by Country Configuration"
      onClick="goToUrl('${contextPath}/auto/config/cntry')" pad="true"/>
    <cmr:button label="Scenario Exceptions"
      onClick="goToUrl('${contextPath}/auto/config/exceptions')" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>
</div>

<script src="${resourcesPath}/js/auto/auto_config.js?${cmrv}"></script>
