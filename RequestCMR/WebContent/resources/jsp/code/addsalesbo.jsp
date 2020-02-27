<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="com.ibm.cio.cmr.request.controller.DropdownListController"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.SalesBoModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
SalesBoModel salesBoMaintModel = (SalesBoModel) request.getAttribute("salesBoMaintModel");
boolean newEntry = false;
String countryDesc = "";
if (salesBoMaintModel.getState() == BaseModel.STATE_NEW) {
  newEntry = true;
} else {
  countryDesc = DropdownListController.getDescription("CMRIssuingCountry", salesBoMaintModel.getIssuingCntry(), "all");
}
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>

<script>
  var sbo = {
    issuingCntry : <%=salesBoMaintModel.getIssuingCntry() != null ? "'" + salesBoMaintModel.getIssuingCntry() + "'" : "null"%>,
    repTeamCd : <%= salesBoMaintModel.getRepTeamCd() != null ? "'" + salesBoMaintModel.getRepTeamCd() + "'" : "null"%>,
    salesBoCd : <%=salesBoMaintModel.getSalesBoCd() != null ? "'" + salesBoMaintModel.getSalesBoCd() + "'" : "null"%>,
    salesBoDesc : <%=salesBoMaintModel.getSalesBoDesc() != null ? "'" + salesBoMaintModel.getSalesBoDesc() + "'" : "null"%>,
    mrcCd: <%=salesBoMaintModel.getMrcCd()!=null ? "'"+salesBoMaintModel.getMrcCd()+"'":"null"%>,
    isuCd: <%=salesBoMaintModel.getIsuCd()!=null? "'"+salesBoMaintModel.getIsuCd()+"'":"null"%>,
    clientTier:<%=salesBoMaintModel.getClientTier()!=null? "'"+salesBoMaintModel.getClientTier()+"'":"null" %>
  };
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

table.lov-table td, table.lov-table td input, table.lov-table th input {
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

<div ng-app="SBOApp" ng-controller="SBOController" ng-cloak>
  <cmr:boxContent>
    <cmr:tabs />

    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Sales BO Mapping" : "Maintain Sales BO Mapping"%></h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <table id="recipientsTable" cellspacing="0" cellpadding="0" border="0" summary="Info"
          class="ibm-data-table ibm-sortable-table ibm-alternating">
          <thead>
            <tr>
              <th scope="col" width="20%">Issuing Country</th>
              <th scope="col" width="15%">Rep Team Code</th>
              <th scope="col" width="15%">Sales BO Code</th>
              <th scope="col" width="35%">Sales BO Description</th>
              <th scope="col" width="15%">MRC Code</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <span ng-show="existing">
                  <%if (!StringUtils.isBlank(countryDesc)){%>
                    <%=countryDesc%>
                  <%}else { %>
                  {{sbo.issuingCntry}} - {{getCountryDesc(sbo.issuingCntry)}}
                  <%} %>
                </span>
                <span ng-show="!existing">
                  <select ng-model="sbo.issuingCntry" id="cntrySelect">
                    <option ng-repeat="country in countries" value="{{country.ret1}}">
                      {{country.ret1}} - {{country.ret2}}
                    </option>
                  </select>
                </span>
              </td>
              <td>
                <span ng-show="existing">
                  {{sbo.repTeamCd}}
                </span>
                <span ng-show="!existing">
                  <input ng-model="sbo.repTeamCd" style="width: 98%" maxlength="6">
                </span>
              </td>
              <td>
              	<span ng-show="existing">
                  {{sbo.salesBoCd}}
                </span>
                <span ng-show="!existing">
              		<input ng-model="sbo.salesBoCd" style="width: 98%" maxlength="10">
              	</span>
              </td>
              <td>
                 <input ng-model="sbo.salesBoDesc" style="width: 98%" maxlength="50">
              </td>
              <td>
              	<input ng-model="sbo.mrcCd" style="width: 98%" maxlength="1">
              </td>
            </tr>
          </tbody>
        </table>
      </cmr:row>
      <cmr:row>
      	<h4>Mappings</h4>
        	<cmr:column span="3">
        	  <table cellspacing="0" cellpadding="0" border="0" summary="ISU Code Mapping"
          class="ibm-data-table ibm-alternating">
        	  <thead>
        	  <tr>
        	  	<th>ISU Codes | Add ISU Code: <select ng-model="UI.isuCd" id="isuSelect" style="width:60%;">
                    <option ng-repeat="isu in isuCodes" value="{{isu.ret1}}">
                      {{isu.ret1}} - {{isu.ret2}}
                    </option>
                  </select></th>
        	  	<th><img class="add" title="Add Value" ng-click="addIsu()" src="${resourcesPath}/images/add.png"></th>
        	  </tr>
        	  </thead>
        	  <tbody>
        	  <tr ng-repeat="isu in sbo.isuCdList">
        	  	<td>{{isu}} - {{getIsuDesc(isu)}}</td><td><img class="remove" title="Remove Value" ng-click="removeIsu(isu)" src="${resourcesPath}/images/remove.png"></td>
        	  </tr>
        	  </tbody>
        	  </table>
        	</cmr:column>
        	<cmr:column span="3">
        	  <table cellspacing="0" cellpadding="0" border="0" summary="ISU Code Mapping"
          		class="ibm-data-table ibm-sortable-table ibm-alternating">
        	  <thead>
        	  <tr>
        	  	<th>Client Tier Codes | Add Client Tier: <select ng-model="UI.clientTier" id="ctcSelect" style="width:47%;">
                    <option ng-repeat="ctc in clientTierCodes" value="{{ctc.ret1}}">
                      {{ctc.ret1}} - {{ctc.ret2}}
                    </option>
                  </select></th>
        	  	<th><img class="add" title="Add Value" ng-click="addClientTier()" src="${resourcesPath}/images/add.png"></th>
        	  </tr>
        	  </thead>
        	  <tbody>
        	  <tr ng-repeat="ctc in sbo.clientTierList">
        	  	<td>{{ctc}} - {{getClientTierDesc(ctc)}}</td><td><img class="remove" title="Remove Value" ng-click="removeClientTier(ctc)" src="${resourcesPath}/images/remove.png"></td>
        	  </tr>
        	  </tbody>
        	  </table>
        	</cmr:column>
        </cmr:row>
    </cmr:section>
  </cmr:boxContent>
  <cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <input type="button" class="ibm-btn-cancel-pri ibm-btn-small" value="Save Values" ng-click="saveAll()">
      <input style="margin-left:10px" type="button" class="ibm-btn-cancel-sec ibm-btn-small" ng-disabled="!existing" value="Delete Sales BO" ng-click="deleteSBO()">
      <input style="margin-left:10px" type="button" class="ibm-btn-cancel-sec ibm-btn-small" value="Back to SBO List" ng-click="backToList()">
    </cmr:buttonsRow>
    <br>
  </cmr:section>
</div>
<script src="${resourcesPath}/js/system/salesbomaint.js?${cmrv}"></script>
