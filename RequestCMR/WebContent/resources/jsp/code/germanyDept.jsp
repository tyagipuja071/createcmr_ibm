<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.GermanyDeptModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<script src="${resourcesPath}/js/system/system.js?${cmrv}"></script>
 <%
  boolean admin = AppUser.getUser(request).isAdmin();
      String department = (String) request.getParameter("deptName");
%>
<script>
 var GermanyDeptService = (function() {
    return { 
    
      findDept : function() {
      var deptName = FormManager.getActualValue('deptName');
      debugger;
      if(deptName == "" || deptName.length < 4){
      alert("Please specify atleast 4 characters in Department");
      return;
      }
      
      if(deptName.length > 6)
      {
      alert("Exceeds length");
      }
      
      if(deptName.length == 6){
       var result = cmr.query('COUNT_GERMANY_DEPT', {
       DEPT : FormManager.getActualValue('deptName'+"%")
    });
    if (result != null && result.ret1 == 0){
    cmr.alert("Add Department");
    }
    
      }
    },
        linkFormatter : function(value, rowIndex) {
         var rowData = this.grid.getItem(0);
         if (rowData == null) {
         return ''; // not more than 1 record
        }
        rowData = this.grid.getItem(rowIndex);
        var deptName = rowData.deptName;
        var currInd = rowData.currentIndc[0];
        if (currInd == 'Y') {
        return '';
        }
       var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
       return '<img src="' + imgloc + 'addr-remove-icon.png"  class="addr-icon" title = "Remove Entry" onclick = "doRemoveFromMachineList(\'' + deptName + '\')">';
     },
}
   })();
  
   function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
  } 
  </script>
<link rel="stylesheet" href="${resourcesPath}/css/auto_config.css?${cmrv}"/>
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
<div ng-app="GDApp" ng-controller="GDController" ng-cloak>
 <form:form method="POST" action="${contextPath}/code/germanyDept" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="germanyDept">
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
      <cmr:row>
        <cmr:column span="6">
          <cmr:note text="Use find department to search for Germany IBM Department Number"></cmr:note>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <cmr:label fieldId="deptName">IBM Department No:</cmr:label>
          <input id="deptName" type="text" name="deptName" />
        </cmr:column> 
         <cmr:row>
         &nbsp;
      </cmr:row>
        <cmr:column span="2">
       <input type="button" id="test" value="Find Department" ng-click="findDept()" >
        </cmr:column>
      </cmr:row>
      <cmr:row>
         &nbsp;
      </cmr:row>
    </cmr:section>
  </cmr:boxContent>
  
  <div ng-show="deptList.length > 0">  
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Germany Department List </h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
      <table id="deptListGrid" cellspacing="0" cellpadding="0" border="0" summary="Info"
          class="ibm-data-table ibm-sortable-table ibm-alternating">
           <thead>
            <tr>
              <th scope="col" width="20%">Code</th>
              <th scope="col" width="8%">Actions</th>
            </tr>
          </thead>
          <tbody>
          <tr ng-repeat="rt in deptList track by $index"
              class="{{$index%2 == 1 ? 'ibm-alt-row' : ''}}">
              <td><input ng-model="rt.deptName" maxlength="6"
                style="width: 95%"></td>
                
           <td>   <img class="remove" title="Remove Mapping"
                ng-click="removeValue($index)"
                src="${resourcesPath}/images/remove.png"
                ng-show="rt.status == 'D'"> </td>     
            </tr>
          </tbody> 
        </table>
      </cmr:row>
      <cmr:row topPad="10">
      </cmr:row>
    </cmr:section>
    </div>
    <cmr:row >
      &nbsp;
    </cmr:row>
    <cmr:row >
      <cmr:column span="2">
      </cmr:column>
    </cmr:row>
  <cmr:section alwaysShown="true">
  <cmr:buttonsRow>
  <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="false" />
  </cmr:buttonsRow>
</cmr:section>
  </cmr:boxContent>
</form:form>
</div> 
<script src="${resourcesPath}/js/system/germanyDept.js?${cmrv}"></script>

 
 
 
 