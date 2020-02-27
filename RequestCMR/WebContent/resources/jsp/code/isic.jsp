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
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<link rel="stylesheet" href="${resourcesPath}/css/auto_config.css?${cmrv}"/>
<style>
span.radio-choice {
  font-size: 12px;
  color: #666;
  padding-left: 10px;
  vertical-align: top;
}
span.radio-choice input {
  vertical-align: sub;
}
div.isic-filter {
  float:right;
  font-size: 12px;
  font-family: IBM Plex Sans, Calibri;  
  text-transform: uppercase;
  font-weight:bold;
}
div.isic-filter input {
  font-size: 12px;
  font-family: IBM Plex Sans, Calibri;  
  margin-left: 4px;
}

table.isic-table {
  width:100%;
}
table.isic-table th, table.isic-table td {
  font-size: 12px;
  font-family: IBM Plex Sans, Calibri;  
}

table.isic-table input, table.isic-table select {
  font-size: 12px;
  font-family: IBM Plex Sans, Calibri;  
  border: 1px Solid #444;
  border-radius : 2px;
}
</style>
<div ng-app="IsicApp" ng-controller="IsicController" ng-cloak>
<cmr:boxContent>
  <cmr:tabs />
  
  <cmr:section>
    <cmr:row>
      <cmr:column span="6">
        <h3>ISIC Maintenance</h3>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="6">
        <cmr:note text="Please choose the GEO and click View ISIC List. Please take extra caution when modifying records."></cmr:note>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="1" width="80">
        <div class="exc-label">GEO:</div>
      </cmr:column>
      <cmr:column span="4">
          <span class="radio-choice"><input type="radio" ng-disabled="searching" ng-model="geoCd" value="US" name="geoCd" checked>US</span> 
          <span class="radio-choice"><input type="radio" ng-disabled="searching" ng-model="geoCd" value="WW" name="geoCd">WW</span>
          <input ng-show="!searching" type="button" value="View ISIC List" ng-click="showIsic()" style="margin-left:10px">
          <input ng-show="searching" type="button" value="Change GEO" ng-click="changeGEO()" style="margin-left:10px">
      </cmr:column>
    </cmr:row>

    <div ng-show="isicList.length > 0">    
    <cmr:row topPad="10">
      <cmr:column span="6">
        
          <table cellspacing="0" cellpadding="0" summary="ISIC List for {{geoCd}}" class="ibm-data-table ibm-sortable-table ibm-alternating isic-table">
            <caption>
              <em>ISIC List for {{geoCd}}
              <cmr:info text="The list displays the first 200 records. Use the filters to find the records you need. Deletions are not allowed to avoid issues in CMR processing."></cmr:info> 
              </em>
              <div class="isic-filter">
                Filter:
                <input ng-model="searchIsic" style="width:120px" maxlength="4" placeholder="ISIC Code">
                <input ng-model="searchIsicDesc" style="width:150px" placeholder="ISIC Desc">
                <input ng-model="searchSubInd" style="width:120px" maxlength="2" placeholder="SubIndustry Code">
                <input ng-model="searchSubIndDesc" style="width:150px" placeholder="SubIndustry Desc">
                <input type="button" value="New ISIC" ng-click="newIsic()">
              </div>
            </caption>
            <thead>
              <tr>
                <th scope="col" width="9%">ISIC Code</th>
                <th scope="col" width="40%">ISIC Description</th>
                <th scope="col" width="13%">Subindustry Code</th>
                <th scope="col" width="32%">Subindustry Description</th>
                <th scope="col" width="11%">&nbsp;</th>
              </tr>
            </thead>
            <tbody>
              <tr ng-show="adding">
                <td><input ng-model="newSic.reftUnsicCd" maxlength="4"></td>
                <td>
                  <input ng-model="newSic.reftUnsicAbbrevDesc"style="width:300px">
                </td>
                <td>
                  <select ng-model="newSic.subObj" style="width:43px" ng-options="sub.name for sub in subIndustries track by sub.id">
                  </select>
                </td>
                <td>-</td>
                <td>

                  <img src="${resourcesPath}/images/save.png" class="cmr-delta-icon" title="Save" ng-click="saveIsicNew()">
                  <img src="${resourcesPath}/images/refresh.png" class="cmr-delta-icon" title="Undo"ng-click="undoEditNew()">
                </td>
              </tr>
              <tr ng-repeat="isic in isicList | isicFilter:searchIsic | subIndFilter:searchSubInd | isicDescFilter:searchIsicDesc | subIndDescFilter:searchSubIndDesc | limitTo:200">
                <td>{{isic.reftUnsicCd}}</td>
                <td>
                  <span ng-show="!isic.edit">{{isic.reftUnsicAbbrevDesc}}</span>
                  <input ng-model="isic.reftUnsicAbbrevDesc" ng-show="isic.edit" style="width:300px">
                </td>
                <td>
                  <span ng-show="!isic.edit">{{isic.indclCd}}</span>
                  <select ng-model="isic.subObj" ng-show="isic.edit" style="width:43px" ng-options="sub.name for sub in subIndustries track by sub.id">
                  </select>
                </td>
                <td>{{isic.indclAbbrevDesc}}</td>
                <td>
                  <img src="${resourcesPath}/images/addr-edit-icon.png" class="cmr-error-icon" title="Edit ISIC" ng-show="!isic.edit" ng-click="editIsic(isic)" >

                  <img src="${resourcesPath}/images/save.png" class="cmr-delta-icon" title="Save" ng-show="isic.edit" ng-click="saveIsic(isic)">
                  <img src="${resourcesPath}/images/refresh.png" class="cmr-delta-icon" title="Undo" ng-show="isic.edit" ng-click="undoEdit(isic)">
                </td>
              </tr>
            </tbody>
          </table>
        
      </cmr:column>
    </cmr:row>
    </div>
    
    <cmr:row >
      &nbsp;
    </cmr:row>
    <cmr:row >
      <cmr:column span="2">
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
<script src="${resourcesPath}/js/system/isic.js?${cmrv}"></script>
