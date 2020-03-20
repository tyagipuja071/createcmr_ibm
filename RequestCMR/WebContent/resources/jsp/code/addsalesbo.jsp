<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page
	import="com.ibm.cio.cmr.request.controller.DropdownListController"%>
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
  String country = request.getParameter("issuingCntry");
  SalesBoModel salesBoMaintModel = (SalesBoModel) request.getAttribute("salesBoMaintModel");
  boolean newEntry = false;
  String countryDesc = "";
  if (salesBoMaintModel.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
  }
  countryDesc = DropdownListController.getDescription("CMRIssuingCountry", country, "all");
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>

<script>
  var sbo = {};

  var country =
<%="'" + country + "'"%>
  ;
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
	width: 17px;
	height: 17px;
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
					<h2><%=newEntry ? "Add Sales BO Mappings" : "Maintain Sales BO Mappings"%>
						<%
						  if (!StringUtils.isBlank(countryDesc)) {
						%>
						-
						<%=countryDesc%>
						<%
						  }
						%>
					</h2>
				</cmr:column>
				<cmr:column span="6">
				<cmr:note text="Please use <b>'Save Mappings'</b> button to save all mappings after making changes."></cmr:note>
				</cmr:column>
				
			</cmr:row>

			<cmr:row topPad="8">
				
			</cmr:row>
			<cmr:row>
				<table id="recipientsTable" cellspacing="0" cellpadding="0"
					border="0" summary="Info"
					class="ibm-data-table ibm-sortable-table ibm-alternating">
					<thead>
						<tr>
							<th scope="col" width="8%">Rep Team</th>
							<th scope="col" width="8%">Sales BO</th>
							<th scope="col" width="*">Sales BO Description</th>
							<th scope="col" width="8%">MRC</th>
							<th scope="col" width="17%">ISU Codes</th>
							<th scope="col" width="17%">CTC Code</th>
							<th scope="col" width="8%">Actions</th>
						</tr>
					</thead>
					<tbody>
						<tr ng-show="sboItems.length == 0">
							<td colspan="6">No data to show</td>
							<td><img class="add" title="Add Mapping"
								ng-click="addNew(0)" src="${resourcesPath}/images/add.png">
						</tr>
						<tr ng-repeat="sbo in sboItems track by $index"
							class="{{$index%2 == 1 ? 'ibm-alt-row' : ''}}">
							<td><input ng-model="sbo.repTeamCd" maxlength="6"
								ng-readonly="!sbo.edit"
								ng-class="{'lov-ro' : !sbo.edit, 'lov-del' : sbo.status == 'D'}"
								style="width: 95%"></td>
							<td><input ng-model="sbo.salesBoCd" maxlength="10"
								ng-readonly="!sbo.edit"
								ng-class="{'lov-ro' : !sbo.edit, 'lov-del' : sbo.status == 'D'}"
								style="width: 95%"></td>
							<td><input ng-model="sbo.salesBoDesc" maxlength="50"
								ng-readonly="!sbo.edit"
								ng-class="{'lov-ro' : !sbo.edit, 'lov-del' : sbo.status == 'D'}"
								style="width: 95%"></td>
							<td><input ng-model="sbo.mrcCd" maxlength="1"
								ng-readonly="!sbo.edit"
								ng-class="{'lov-ro' : !sbo.edit, 'lov-del' : sbo.status == 'D'}"
								style="width: 95%"></td>
							<td><input ng-model="sbo.isuCd" readonly="true"
								ng-class="{'lov-ro' : !sbo.edit, 'lov-del' : sbo.status == 'D'}"
								style="width: 75%"> &nbsp;<img class="up"
								title="Edit ISU" ng-click="!sbo.edit||editISUCTC($index,'isu')"
								src="${resourcesPath}/images/edit.png"></td>
							<td><input ng-model="sbo.clientTier" readonly="true"
								ng-class="{'lov-ro' : !sbo.edit, 'lov-del' : sbo.status == 'D'}"
								style="width: 75%"> &nbsp;<img class="up"
								title="Edit Client Tier"
								ng-click="!sbo.edit||editISUCTC($index,'ctc')"
								src="${resourcesPath}/images/edit.png"></td>
							<td><img class="add" title="Add Mapping"
								ng-click="addNew($index+1)" src="${resourcesPath}/images/add.png">
								<img ng-show="!sbo.edit && sbo.status!='D'" class="add" title="Edit Mapping"
								ng-click="editValue($index)" src="${resourcesPath}/images/edit.png">
								<img class="remove" title="Remove Mapping"
								ng-click="removeValue($index)"
								src="${resourcesPath}/images/remove.png"
								ng-show="sbo.status != 'D'"> <img class="remove"
								title="Undo Remove" ng-click="unRemoveValue($index)"
								src="${resourcesPath}/images/refresh.png"
								ng-show="sbo.status == 'D'"></td>
						</tr>
					</tbody>
				</table>
			</cmr:row>
		</cmr:section>
	</cmr:boxContent>
	<cmr:section alwaysShown="true">
		<cmr:buttonsRow>
			<input type="button" class="ibm-btn-cancel-pri ibm-btn-small"
				value="Save Mappings" ng-click="saveAll()">
			<input style="margin-left: 10px" type="button"
				class="ibm-btn-cancel-sec ibm-btn-small"
				value="Delete All Mappings" ng-click="deleteSBO()">
			<input style="margin-left: 10px" type="button"
				class="ibm-btn-cancel-sec ibm-btn-small" value="Define Mapping per Country"
				ng-click="backToList()">
		</cmr:buttonsRow>
		<br>
	</cmr:section>
	<cmr:modal title="" id="isuCodeModal">
		<h3>Add/Remove ISU Codes</h3>
		<table cellspacing="0" cellpadding="0" border="0"
			summary="ISU Code Mapping" class="ibm-data-table ibm-alternating"
			style="margin-right: 2px">
			<thead>
				<tr>
					<th><select ng-model="UI.isuCd" id="isuSelect"
						style="width: 100%;">
							<option ng-repeat="isu in isuCodes" value="{{isu.ret1}}">
								{{isu.ret1}} - {{isu.ret2}}</option>
					</select></th>
					<th><img class="add" title="Add Value" ng-click="addIsu()"
						src="${resourcesPath}/images/add.png"></th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="isu in current.isuCdList">
					<td>{{isu}} - {{getIsuDesc(isu)}}</td>
					<td><img class="remove" title="Remove Value"
						ng-click="removeIsu(isu)" src="${resourcesPath}/images/remove.png"></td>
				</tr>
			</tbody>
		</table>
		<hr>
		<cmr:buttonsRow>
			<input type="button" class="ibm-btn-cancel-pri ibm-btn-small"
				value="Save Values" ng-click="saveISU()">
			<input style="margin-left: 10px" type="button"
				class="ibm-btn-cancel-sec ibm-btn-small" value="Cancel"
				ng-click="cancelISUCTC('isu')">
		</cmr:buttonsRow>
	</cmr:modal>
	<cmr:modal title="" id="clientTierModal">
		<h3>Add/Remove Client Tier Codes</h3>
		<table cellspacing="0" cellpadding="0" border="0"
			summary="ISU Code Mapping"
			class="ibm-data-table ibm-sortable-table ibm-alternating">
			<thead>
				<tr>
					<th><select ng-model="UI.clientTier" id="ctcSelect"
						style="width: 100%;">
							<option ng-repeat="ctc in clientTierCodes" value="{{ctc.ret1}}">{{ctc.ret1}}
								- {{ctc.ret2}}</option>
					</select></th>
					<th><img class="add" title="Add Value"
						ng-click="addClientTier()" src="${resourcesPath}/images/add.png"></th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="ctc in current.clientTierList">
					<td>{{ctc}} - {{getClientTierDesc(ctc)}}</td>
					<td><img class="remove" title="Remove Value"
						ng-click="removeClientTier(ctc)"
						src="${resourcesPath}/images/remove.png"></td>
				</tr>
			</tbody>
		</table>
		<hr>
		<cmr:buttonsRow>
			<input type="button" class="ibm-btn-cancel-pri ibm-btn-small"
				value="Save Values" ng-click="saveCTC()">
			<input style="margin-left: 10px" type="button"
				class="ibm-btn-cancel-sec ibm-btn-small" value="Cancel"
				ng-click="cancelISUCTC('ctc')">
		</cmr:buttonsRow>

	</cmr:modal>
</div>
<script src="${resourcesPath}/js/system/salesbomaint.js?${cmrv}"></script>
