<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="com.ibm.cio.cmr.request.controller.DropdownListController"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.RepTeamModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
String country = request.getParameter("issuingCntry");
RepTeamModel repTeamMaintModel = (RepTeamModel) request.getAttribute("repTeamMaintModel");
boolean newEntry = false;
String countryDesc = "";
if (repTeamMaintModel.getState() == BaseModel.STATE_NEW) {
  newEntry = true;
}
  countryDesc = DropdownListController.getDescription("CMRIssuingCountry", country , "all");
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />    
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>

<script>
  var rt = {} ;
  var country =
<%="'" + country + "'"%>
  
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

table.rt-table td, table.rt-table td input table.rt-table th input {
  font-size: 12px;
}

input.rt-ro {
  background: #DDD;
  border: 1px Solid #666;
}

input.rt-del {
  text-decoration: line-through;
  background: #ff6666;
}

span.rt-del {
  text-decoration: line-through;
  color: #ff6666;
}
</style>
<div ng-app="RTApp" ng-controller="RTController" ng-cloak>
  <cmr:boxContent>
    <cmr:tabs />

    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h2><%=newEntry ? "Add Rep Team Mappings" : "Maintain Rep Team Mappings"%></h2>
          <%
              if (!StringUtils.isBlank(countryDesc)) {
            %>
            
            <%=countryDesc%>
            <%
              }
            %>
        </cmr:column>
        <cmr:column span="6">
        <cmr:note text="Please use <b>'Save Mappings'</b> button to save all mappings after making changes."></cmr:note>
        </cmr:column>
     <%--    <cmr:column span="2">
        <cmr:buttonsRow>
        <input type="button" style="margin-top: 5px"
        class="ibm-btn-cancel-pri ibm-btn-small" value="Add New Record"
        ng-click="addNew()">
        </cmr:buttonsRow>
        </cmr:column> --%>
      </cmr:row>
      <cmr:row topPad="8">  
      </cmr:row>
      <cmr:row>
        <table id="recipientsTable" cellspacing="0" cellpadding="0" border="0" summary="Info"
          class="ibm-data-table ibm-sortable-table ibm-alternating">
           <thead>
            <tr>
              <th scope="col" width="20%">Rep Team Code</th>
              <th scope="col" width="20%">Rep Team Member No</th>
              <th scope="col" width="*">Rep Team Member Name</th>
              <th scope="col" width="8%">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr ng-show="rtItems.length == 0">
              <td colspan="7">No data to show</td>
              <td><img class="add" title="Add Mapping"
                ng-click="addNew(0)" src="${resourcesPath}/images/add.png">   
            </tr>
            <tr ng-repeat="rt in rtItems track by $index"
              class="{{$index%2 == 1 ? 'ibm-alt-row' : ''}}">
              <td><input ng-model="rt.repTeamCd" maxlength="6"
                ng-readonly="!rt.edit"
                ng-class="{'rt-ro' : !rt.edit, 'rt-del' : rt.status == 'D'}"
                style="width: 95%"></td>
              <td><input ng-model="rt.repTeamMemberNo" maxlength="10"
                ng-readonly="!rt.edit"
                ng-class="{'rt-ro' : !rt.edit, 'rt-del' : rt.status == 'D'}"
                style="width: 95%"></td>
              <td><input ng-model="rt.repTeamMemberName" maxlength="100"
                ng-readonly="!rt.edit"
                ng-class="{'rt-ro' : !rt.edit, 'rt-del' : rt.status == 'D'}"
                style="width: 95%"></td>
              
              <td><img class="add" title="Add Mapping"
                ng-click="addNew($index+1)" src="${resourcesPath}/images/add.png">
                 <img ng-show="!rt.edit && rt.status!='D'" class="add" title="Edit Mapping"
                ng-click="editValue($index)" src="${resourcesPath}/images/edit.png">
                <img class="remove" title="Remove Mapping"
                ng-click="removeValue($index)"
                src="${resourcesPath}/images/remove.png"
                ng-show="rt.status != 'D'"> 
                <img class="remove"
                title="Undo Remove" ng-click="unRemoveValue($index)"
                src="${resourcesPath}/images/refresh.png"
                ng-show="rt.status == 'D'"></td>
                
            </tr>
          </tbody>
        </table>
      </cmr:row>
    </cmr:section>
  </cmr:boxContent>
  <cmr:section alwaysShown="true">  
    <cmr:buttonsRow>
      <input type="button" class="ibm-btn-cancel-pri ibm-btn-small" value="Save Mappings" ng-click="saveAll()">
      <input style="margin-left:10px" type="button" class="ibm-btn-cancel-sec ibm-btn-small"  value="Delete All Mappings" ng-click="deleteRT()">
      <input style="margin-left:10px" type="button" class="ibm-btn-cancel-sec ibm-btn-small" value="Define Mapping per country" ng-click="backToList()">
    </cmr:buttonsRow>
    <br>
  </cmr:section>
</div>
<script src="${resourcesPath}/js/system/rtmaint.js?${cmrv}"></script>
