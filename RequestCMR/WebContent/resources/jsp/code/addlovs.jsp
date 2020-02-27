<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="com.ibm.cio.cmr.request.controller.DropdownListController"%>
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
LovModel lovsmain = (LovModel) request.getAttribute("lovsmain");
boolean newEntry = false;
String countryDesc = "";
if (lovsmain.getState() == BaseModel.STATE_NEW) {
  newEntry = true;
} else {
  countryDesc = DropdownListController.getDescription("CMRIssuingCountry", lovsmain.getCmrIssuingCntry(), "all");
}
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>

<script>
  var lov = {
    cmrIssuingCntry : <%=lovsmain.getCmrIssuingCntry() != null ? "'" + lovsmain.getCmrIssuingCntry() + "'" : "null"%>,
    fieldId : <%=lovsmain.getFieldId() != null ? "'" + lovsmain.getFieldId() + "'" : "null"%>,
    cmt : <%=lovsmain.getCmt() != null ? "'" + lovsmain.getCmt() + "'" : "null"%>,
    dispType : <%=lovsmain.getDispType() != null ? "'" + lovsmain.getDispType() + "'" : "null"%>
  };
  
  function backToList(){
    
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

img.add {
  width: 19px;
  height: 19px;
  font-size: 10px;
  text-align: center;
  cursor: pointer;
}

img.remove {
  width: 19px;
  height: 19px;
  font-size: 10px;
  text-align: center;
  cursor: pointer;
}

img.up {
  width: 19px;
  height: 19px;
  font-size: 10px;
  text-align: center;
  cursor: pointer;
}

table.lov-table td, table.lov-table td input {
  font-size: 12px;
}

input.lov-ro {
  background: #DDD;
  border: 1px Solid #666;
}

input.lov-del {
  text-decoration: line-through;
  background: #ff6666;
}

span.lov-del {
  text-decoration: line-through;
  color: #ff6666;
}
</style>
<div ng-app="LOVApp" ng-controller="LOVController" ng-cloak>
  <cmr:boxContent>
    <cmr:tabs />

    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add List of Values" : "Maintain List of Values"%></h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <table id="recipientsTable" cellspacing="0" cellpadding="0" border="0" summary="Info"
          class="ibm-data-table ibm-sortable-table ibm-alternating">
          <thead>
            <tr>
              <th scope="col" width="20%">Field ID</th>
              <th scope="col" width="18%">CMR Issuing Country</th>
              <th scope="col" width="14%">Display Type</th>
              <th scope="col" width="*">Comments</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <span ng-show="existing">
                  {{lov.fieldId}}
                </span>
                <span ng-show="!existing">
                  <select ng-model="lov.fieldId" ng-change="checkExistingLov()" id="fieldIdSelect">
                    <option ng-repeat="id in ids" value="{{id.ret1}}">
                      {{id.ret1}}
                    </option>
                  </select>
                </span>
              </td>
              <td>
                <span ng-show="existing">
                  <%if (!StringUtils.isBlank(countryDesc)){%>
                    <%=countryDesc%>
                  <%}else { %>
                  {{lov.cmrIssuingCntry}}
                  <%} %>
                </span>
                <span ng-show="!existing">
                  <select ng-model="lov.cmrIssuingCntry" ng-change="checkExistingLov()" id="cntrySelect">
                    <option ng-repeat="country in countries" value="{{country.ret1}}">
                      {{country.ret1}} - {{country.ret2}}
                    </option>
                  </select>
                </span>
              </td>
              <td><select ng-model="lov.dispType">
                  <option value="C">Code Only</option>
                  <option value="T">Text Only</option>
                  <option value="B">Code and Text</option>
              </select>
              </td>
              <td><input ng-model="lov.cmt" style="width: 98%">
              </td>
            </tr>
          </tbody>
        </table>
        <div style="height: 20px">&nbsp;</div>
        <table cellspacing="0" cellpadding="0" border="0" summary="Values" class="lov-table ibm-data-table ibm-sortable-table ibm-alternating">
          <caption>
            <em>Values</em>
          </caption>
          <thead>
            <tr>
              <th scope="col" width="8%">Order</th>
              <th scope="col" width="20%">Code</th>
              <th scope="col" width="*">Text</th>
              <th scope="col" width="6%">Default</th>
              <th scope="col" width="13%">&nbsp;</th>
            </tr>
          </thead>
          <tbody>
            <tr ng-show="lov.values.length == 0">
              <td colspan="4">No data to show</td>
              <td><img class="add" title="Add Value" ng-click="addValue({dispOrder : 0})" src="${resourcesPath}/images/add.png">
              </td>
            </tr>
            <tr ng-repeat="entry in lov.values track by $index" class="{{entry.dispOrder%2 == 1 ? 'ibm-alt-row' : ''}}">
              <td><span class="{{entry.status == 'D' ? 'lov-del' : ''}}">{{entry.dispOrder}}</span></td>
              <td><input ng-model="entry.cd" ng-readonly="entry.status != 'N' || entry.cd == '-BLANK-'" ng-class="{'lov-ro' : entry.cd == '-BLANK-' || entry.status != 'N', 'lov-del' : entry.status == 'D'}" style="width: 95%"></td>
              <td><input ng-model="entry.txt" ng-readonly="entry.status == 'E' || entry.status == 'D'" ng-class="{'lov-ro' : entry.status == 'E' || entry.status == 'D', 'lov-del' : entry.status == 'D'}" style="width: 95%"></td>
              <td><input ng-model="entry.defaultInd" ng-disabled="entry.status == 'E' || entry.status == 'D'" type="checkbox" ng-click="handleDefault(entry)"></td>
              <td>
                <img class="add" title="Edit Value" ng-click="editValue(entry)" src="${resourcesPath}/images/edit.png"> 
                <img class="add" title="Add Value" ng-click="addValue(entry)" src="${resourcesPath}/images/add.png"> 
                <img class="remove" title="Remove Value" ng-click="removeValue(entry)" src="${resourcesPath}/images/remove.png" ng-show="entry.status != 'D'"> 
                <img class="remove" title="Undo Remove" ng-click="unRemoveValue(entry)" src="${resourcesPath}/images/refresh.png" ng-show="entry.status == 'D'"> 
                <img class="up" title="Move Up" ng-click="moveValueUp(entry)" src="${resourcesPath}/images/up.png">
                <img ng-show="$index == lov.values.length - 1" class="add" title="Add Blank Value" ng-click="addBlankValue(entry)" src="${resourcesPath}/images/add-blank.png"> 
              </td>
            </tr>
          </tbody>
        </table>
      </cmr:row>
    </cmr:section>
  </cmr:boxContent>
  <cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <input type="button" class="ibm-btn-cancel-pri ibm-btn-small" value="Save Values" ng-click="saveAll()">
      <input style="margin-left:10px" type="button" class="ibm-btn-cancel-sec ibm-btn-small" ng-disabled="!existing || lov.cmrIssuingCntry == '*'" value="Delete LOV List" ng-click="deleteLOV()">
      <input style="margin-left:10px" type="button" class="ibm-btn-cancel-sec ibm-btn-small" value="Back to LOV List" ng-click="backToList()">
    </cmr:buttonsRow>
    <br>
  </cmr:section>
</div>
<script src="${resourcesPath}/js/system/lovmaint.js?${cmrv}"></script>
