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
<style>
div.status-green {
  font-weight:bold;
  color: green;
}
div.status-red {
  font-weight:bold;
  color: red;
}
input.ro {
  background: #EEE;
  border: 1px Solid #AAA;
}
</style>
<div ng-app="DivDeptApp" ng-controller="DivDeptController" ng-cloak>
<cmr:boxContent>
  <cmr:tabs />
  
  <cmr:section>
    <cmr:row>
      <cmr:column span="6">
        <h3>IBM Internal Division/Department (US)</h3>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="6">
        <cmr:note text="Please specify the combination of IBM Division and Department codes below."></cmr:note>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="1">
        <div class="exc-label">Division:</div>
      </cmr:column>
      <cmr:column span="4">
        <input ng-model="div" maxlength="2" style="width:50px" ng-readonly="mapStatus" ng-class="{'ro' : mapStatus}">
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="1">
        <div class="exc-label">Department:</div>
      </cmr:column>
      <cmr:column span="4">
        <input ng-model="dept" maxlength="3" style="width:50px" ng-readonly="mapStatus" ng-class="{'ro' : mapStatus}">
      </cmr:column>
    </cmr:row>
    <div ng-show="mapStatus">
      <cmr:row topPad="10">
        <cmr:column span="1">
          <div class="exc-label">Status:</div>
        </cmr:column>
        <cmr:column span="5">
          <div class="status-green" ng-show="mapStatus == 'Y'">Division and Department mapping is active and usable.</div>
          <div class="status-red" ng-show="mapStatus == 'N'">
            Division and Department mapping is NOT active. Division and/or Department not found.
          </div>
          <div class="status-red" ng-show="mapStatus == 'X'">
            Division and Department mapping is NOT active. Department is currently mapped to Division {{currDiv}}.
          </div>
        </cmr:column>
      </cmr:row>
      <div ng-show="mapStatus == 'N' || mapStatus == 'X'">
        <cmr:row topPad="10">
          <cmr:column span="1">
             &nbsp;
          </cmr:column>
          <cmr:column span="5">
            <input type="button" value="Map Division and Department" ng-click="mapDivDept()" >
          </cmr:column>
        </cmr:row>
      </div>
    </div>
    <cmr:row >
      &nbsp;
    </cmr:row>
    <cmr:row >
      <cmr:column span="2">
        <input type="button" value="Check Status" ng-click="checkStatus()" ng-show="!mapStatus">
        <input type="button" value="Do Another Check" ng-click="mapStatus = ''" ng-show="mapStatus">
      </cmr:column>
    </cmr:row>
    <cmr:row >
      &nbsp;
    </cmr:row>
  </cmr:section>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Back to Code Maintenance Home"
      onClick="backToCodeMaintHome()" pad="false"/>
  </cmr:buttonsRow>
</cmr:section>
</div>
<script src="${resourcesPath}/js/system/div_dept.js?${cmrv}"></script>
