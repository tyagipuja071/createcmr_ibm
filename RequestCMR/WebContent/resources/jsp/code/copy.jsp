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
    var message = 'This function affects the global processing of the tool. Incorrect usage may lead to application errors.';
    cmr.showConfirm(null, message, 'Warning', 'backToCodeMaintHome()', {OK : 'Proceed', CANCEL : 'Exit'});
  });
</script>
<style>
img.choice-icon {
  width:19px;
  height:19px;
  float:right;
  margin-top: 3px;
  margin-left: 2px;
}
div.choice {
  width: 300px;
  display: inline-block;
  border: 1px Solid #999999;
  padding: 5px;
  border-radius: 3px;
  vertical-align: top;
}

div.entry {
  min-width:100px;
  padding:2px;
  height: 25px;
  border: 1px Solid #DDDDDD;
  border-radius: 3px;
  padding:2px;
  margin: 2px;
  cursor: pointer;
}
div.selected {
  background: rgb(205,235,142); /* Old browsers */
  background: -moz-linear-gradient(top,  rgba(205,235,142,1) 0%, rgba(165,201,86,1) 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top,  rgba(205,235,142,1) 0%,rgba(165,201,86,1) 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom,  rgba(205,235,142,1) 0%,rgba(165,201,86,1) 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#cdeb8e', endColorstr='#a5c956',GradientType=0 ); /* IE6-9 */
  font-weight: bold;
}
</style>
<div ng-app="CopyConfigApp" ng-controller="CopyConfigController">
  <cmr:boxContent>
    <cmr:tabs />

    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Copy System Configuration</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="6">
        <table cellspacing="0" cellpadding="0" border="0" summary="Info"
          class="ibm-data-table ibm-sortable-table ibm-alternating">
          <thead>
            <tr>
              <th scope="col" width="18%">Source Country:</th>
              <td>
                <select ng-model="sourceCountry" value="" style="width:300px">
                  <option value=""></option>
                  <option ng-repeat="country in countries"  value="{{country.id}}" >{{country.id}} - {{country.name}}
                </select>
              </td>
            </tr>
            <tr>
              <th scope="col" width="18%">Configurations:</th>
              <td>
                <input type="checkbox" ng-model="configFI" value="FI">Field Information
                <br>
                <input type="checkbox" ng-model="configFL" value="FL">Field Labels
                <br>
                <input type="checkbox" ng-model="configLOV" value="LOV">List of Values
                <br>
                <input type="checkbox" ng-model="configSC" value="SC">Scenarios
                <br>
              </td>
            </tr>
            <tr>
              <th scope="col" width="18%">Copy To:</th>
              <td>
                <input type="radio" ng-model="target" value="G">GEOs
                &nbsp;&nbsp;
                <input type="radio" ng-model="target" value="C">Countries
              </td>
            </tr>
            <tr ng-show="target == 'G'">
              <th scope="col" width="18%">Target GEO:</th>
              <td>
                <select ng-model="targetGeo" value="" style="width:300px">
                  <option value=""></option>
                  <option ng-repeat="geo in geos"  value="{{geo.id}}" >{{geo.name}}
                </select>
              </td>
            </tr>
            <tr ng-show="target == 'C'">
              <th scope="col" width="18%">Target Countries:</th>
              <td>
                <div class="cont">
                  <div class="select">Please select the Countries to copy the configuration to</div>
                  <div class="choice">
                    <div class="entry {{cntry.selected ? 'selected' : ''}}" ng-repeat="cntry in countries"  ng-click="cntry.selected = !cntry.selected" title="{{cntry.name}}">
                      {{cntry.id}} - {{cntry.name}} 
                      <img src="${resourcesPath}/images/add.png" class="choice-icon" ng-show="!cntry.selected">
                      <img src="${resourcesPath}/images/remove.png" class="choice-icon" ng-show="cntry.selected">
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td colspan="2"><input type="button" ng-click="copyConfigurations()" value="Copy Configurations"></td>
            </tr>
          </tbody>
        </table>
        </cmr:column>
      </cmr:row>


    </cmr:section>
  </cmr:boxContent>
  <cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <input style="height:40px" type="button" class="ibm-btn-cancel-sec ibm-btn-small" value="Back to Code Maintenance Home" onclick="backToCodeMaintHome()">
    </cmr:buttonsRow>
    <br>
  </cmr:section>
</div>
<script src="${resourcesPath}/js/system/copyconfig.js?${cmrv}"></script>
