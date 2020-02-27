 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script>
  dojo.addOnLoad(function() {
  FormManager.ready();
  });
    
  var RolesService = (function() {
    return {
      roleFormatter : function(value, rowIndex) {
        var rowData = this.grid.getItem(rowIndex);
        var roleId = rowData.roleId;
        
        return '<a href="javascript: RolesService.open(\'' + roleId + '\')">' + roleId + '</a>';
      },
      open : function(roleId) {
        document.forms['frmCMRRoles'].setAttribute('action', cmr.CONTEXT_ROOT + '/code/addRoles/?roleId=' + roleId);
        document.forms['frmCMRRoles'].submit();  
      },
      addRoles : function() {
        window.location = cmr.CONTEXT_ROOT + '/code/addRoles';
      }
    };
  })();
  
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/roles" name="frmCMRRoles" class="ibm-column-form ibm-styled-form" modelAttribute="rolesModel">   
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Roles</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/roleslist.json" id="roleslistId" span="6">
            <cmr:gridCol width="15%" field="roleId" header="Role Id" >
             <cmr:formatter functionName="RolesService.roleFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="12%" field="roleName" header="Role Name" />          
            <cmr:gridCol width="10%" field="applicationCdDefinition" header="Application Code" />
            <cmr:gridCol width="7%" field="createTsString" header="Created Timestamp" />
            <cmr:gridCol width="10%" field="createBy" header="Created By" />
            <cmr:gridCol width="7%" field="statusDefinition" header="Status" />
            <cmr:gridCol width="15%" field="comments" header="Comments" />
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
    <cmr:button label="Add" onClick="RolesService.addRoles()" highlight="true" />
    <cmr:button label="Back to Code Maintenance" onClick="window.location = '${contextPath}/code'" pad="true" /> 
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="rolesModel" />