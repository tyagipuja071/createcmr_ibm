<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<%
AppUser user = AppUser.getUser(request);
%> 
<style>
td.lbl {
  text-align: right;
  padding-right: 5px;
  font-weight: bold;
}

td.lbl-l {
  font-weight: bold;
}
table.kyc-res td, table.kyc-res th {
  padding:3px;
  font-family: Calibri;
  text-transform:uppercase;
}
img.load {
  width: 15px;
  height: 15px;
}
span.note {
  font-size: 11px;
  color: #555;
}
</style>
<script>
var sessionData = null;
</script>
<cmr:boxContent>
  <cmr:tabs />

 <cmr:section>
          <div ng-app="KYCApp" ng-controller="KYCController">
      <cmr:row>
        <cmr:column span="6">
          <h3>DPL Screening for KYC and EVS</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="6">
            <table class="kyc-res" cellspacing="1" cellpadding="2" style="width: 100%">
              <tr>
                <td class="lbl" style="width: 120px">Check:</td>
                <td>
                  <input type="radio" ng-model="checkType" value="N" name="checkType">By Name
                  <br>
                  <input type="radio" ng-model="checkType" value="F" name="checkType">By File
                </td>
              </tr>
              <tr ng-show="checkType == 'N'">
                <td class="lbl">Name:</td>
                <td>
                  <input ng-model="inputName" style="width:300px;">
                </td>
              </tr>
              <tr ng-show="checkType == 'F'">
                <td class="lbl">Input File:</td>
                <td>
                  <input type="file" id="inputFile"> <span class="note">(up to 50 names only per batch)</span>
                </td>
              </tr>
              <tr>
                <td class="lbl">Private Person:</td>
                <td>
                  <input type="radio" name="priv" ng-model="priv" value="Y">Yes
                  <input type="radio" name="priv" ng-model="priv" value="N">No
                </td>
              </tr>
              <tr>
                <td class="lbl">Show First Match:</td>
                <td>
                  <input type="radio" name="sample" ng-model="sample" value="Y">Yes
                  <input type="radio" name="sample" ng-model="sample" value="N">No
                </td>
              </tr>
              <tr>
                <td>
                  &nbsp;
                </td>
                <td>
                  <button ng-click="test()">Perform DPL Screening</button>
                </td>
              </tr>
            </table>
        </cmr:column>
      </cmr:row>
      <div ng-show="names && names.length > 0">
        <cmr:row>
          <h3>Results</h3>
              <table class="kyc-res" border="1" cellspacing="1" cellpadding="1" style="width: 100%">
                <tr>
                  <td class="lbl-l" style="width:80px">Item</td>
                  <td class="lbl-l" width="*">Name</td>
                  <td class="lbl-l" style="width:100px">EVS</td>
                  <td class="lbl-l" style="width:100px">KYC</td>
                </tr>
                <tr ng-repeat="name in names">
                  <td>{{name.index + 1}}</td>
                  <td>{{name.name}}</td>
                  <td><span id="evs_{{name.index}}"><img src="${resourcesPath}/images/loading.gif" class="load"></span></td>
                  <td><span id="kyc_{{name.index}}"><img src="${resourcesPath}/images/loading.gif" class="load"></span></td>
                </tr>
              </table>
        </cmr:row>
      </div>
      <cmr:row>
        &nbsp;
      </cmr:row>
          </div>
</cmr:section>
</cmr:boxContent>

<script src="${resourcesPath}/js/system/kyccompare.js?${cmrv}"></script>
    <script>

  window.onload = function() {
    var fileInput = document.getElementById('inputFile');
    fileInput.value = '';
    fileInput.addEventListener('change', function(e) {
      var file = fileInput.files[0];

      if (file && file.name.indexOf('.txt') > 0) {
        var reader = new FileReader();

        reader.onload = function(e) {
          sessionData = reader.result;
        };

        reader.readAsText(file);
      } else {
        alert('File not supported.');
        fileInput.value = '';
      }
    });
  };  
  </script>
  