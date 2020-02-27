<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<%
boolean admin = false;
boolean cmde = false;
AppUser user = AppUser.getUser(request);
if (user != null){
  if (user.isAdmin()){
    admin = true;
  }
  if (user.isCmde()){
    cmde = true;
  }
} 
%>
<script>
  dojo.addOnLoad(function() {
  FormManager.ready();
  });
    
  var SysParameterService = (function() {
    var cmde = <%=cmde%>;
    var admin = <%=admin%>;
    return {
      suppFormatter : function(value, rowIndex) {
        var rowData = this.grid.getItem(rowIndex);
        var parameterCode = rowData.parameterCd;
        var cmdeMaintainable = rowData.cmdeMaintainableIndc;
        
        if ((!admin && cmde && cmdeMaintainable != 'Y')){
          return value;
        } else if (!admin){
          return value;
        }
        return '<a href="javascript: SysParameterService.open(\'' + parameterCode + '\')">' + parameterCode + '</a>';

      },
      autoProcFormatter : function(value, rowIndex) {
        if (value == 'Y') {
          return '<b>Yes</b>';
        } else {
          return 'No';
        }
      },
      typeFormatter : function(value, rowIndex) {
        if (value == 'T') {
          return 'Text';
        } else if (value == 'N'){
          return 'Number';
        } else {
          return 'not specified';
        }
      },
      open : function(parameterCd) {
        window.location = cmr.CONTEXT_ROOT + '/code/addsysparameterpage?parameterCd=' + parameterCd;
      },
      addSysParameter : function() {
        window.location = cmr.CONTEXT_ROOT + '/code/addsysparameterpage';
      },
      refresh : function(){
        var url = cmr.CONTEXT_ROOT + '/code/systemparameterlisting.json';
        CmrGrid.refresh('sysparameterlistingId', url, 'parameterCd=:parameterCd&parameterName=:parameterName');
      }
  };
})();
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/sysparameters" name="frmCMRSysParameter" class="ibm-column-form ibm-styled-form" modelAttribute="sysparametersmodel">   
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>System Parameters</h3>
        </cmr:column>
      </cmr:row>
     <cmr:row topPad="8" addBackground="true">
        <cmr:column span="1" width="70">
          <p>
            <label >Code: 
              <cmr:info text="Enter part of code. Case-insensitive."></cmr:info>
            </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="300">
          <form:input path="parameterCd" dojoType="dijit.form.TextBox"/>
        </cmr:column>
        <cmr:column span="1" width="70">
          <p>
            <label>Name: 
              <cmr:info text="Enter part of name. Case-insensitive."></cmr:info>
            </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <form:input path="parameterName" dojoType="dijit.form.TextBox"/>
        </cmr:column>
        <cmr:column span="1" width="100">
          <input type="button" value="Filter" onclick="SysParameterService.refresh()">
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/systemparameterlisting.json" id="sysparameterlistingId" span="6">
            <cmr:gridCol width="12%" field="parameterCd" header="Code" >
             <cmr:formatter functionName="SysParameterService.suppFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="20%" field="parameterName" header="Name" />
            <cmr:gridCol width="10%" field="parameterTyp" header="Expected Value" >
             <cmr:formatter functionName="SysParameterService.typeFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="auto" field="parameterValue" header="Value" />
            <cmr:gridCol width="20%" field="cmdeMaintainableIndc" header="CMDE Maintainable" >
              <cmr:formatter functionName="SysParameterService.autoProcFormatter" />
            </cmr:gridCol>
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
      </cmr:row>
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (admin){ %>
      <cmr:button label="Add System Parameters" onClick="SysParameterService.addSysParameter()" highlight="true" />
    <%} %>
    <cmr:button label="Back to Code Maintenance" onClick="window.location = '${contextPath}/code'" pad="true" /> 
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="sysparametersmodel" />