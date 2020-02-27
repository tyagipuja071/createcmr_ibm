<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.RolesModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="java.util.Date"%> 
<%@page import="java.text.SimpleDateFormat"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
RolesModel roleModel = (RolesModel) request.getAttribute("rolesModel");
    boolean newEntry = false;
    if (roleModel.getState() == BaseModel.STATE_NEW) {
      newEntry = true;
    } 
    
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
<%if (newEntry) {%>
  FormManager.addValidator('roleId', Validators.REQUIRED, [ 'Role Id' ]);
  FormManager.addValidator('roleName', Validators.REQUIRED, [ 'Role Name' ]);
  FormManager.addValidator('applicationCd', Validators.REQUIRED, [ 'Application Code' ]);
  FormManager.addValidator('status', Validators.REQUIRED, [ 'Status' ]);
<%}%>
  FormManager.ready();
  });
  
  var RolesService = (function() {
    return {
      saveRole : function(typeflag) {   
        var roleId = FormManager.getActualValue('roleId');  
        if (typeflag) {
          var check = cmr.query('ROLES.CHECK_URL', {
            ROLE_ID : roleId
          });
          if (check && check.ret1 == '1') {
            cmr.showAlert('This Validation Url already exists in the system.');
            
            return;
          }
        }  
        FormManager.save('frmCMR');
      },
    };
  })();
  
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/addRoles" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="rolesModel">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Roles" : "Update Roles"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="roleId">Role ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${rolesModel.roleId}</p>
          <form:hidden id="roleId" path="roleId" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="roleId">Role ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="roleId" dojoType="dijit.form.TextBox" maxlength="25"/>
          </p>
        </cmr:column>
       </cmr:row>
      <%
        }
      %>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="roleName">Role Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p><form:input path="roleName" dojoType="dijit.form.TextBox" maxlength="100" value="${rolesModel.roleName}"/></p>
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="roleName">Role Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="roleName" dojoType="dijit.form.TextBox" maxlength="100"/>
          </p>
        </cmr:column>
       </cmr:row>
      <%
        }
      %>  
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="applicationCd">Application Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select path="applicationCd" dojoType="dijit.form.Select" maxlength="3">
              <form:option value="${rolesModel.applicationCd}" selected disabled>${rolesModel.applicationCdDefinition}</form:option>
              <form:option value="R" label="Request CMR"/>
              <form:option value="C" label="Create CMR"/>
              <form:option value="F" label="Find CMR"/>
              <form:option value="RCF" label="All"/>
            </form:select>
          </p>
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="applicationCd">Application Code: </cmr:label>
          </p>  
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select path="applicationCd" dojoType="dijit.form.Select" maxlength="3">
              <form:option value="R" label="Request CMR"/>
              <form:option value="C" label="Create CMR"/>
              <form:option value="F" label="Find CMR"/>
              <form:option value="RCF" label="All"/>
            </form:select>
          </p>
        </cmr:column>
       </cmr:row>
      <%
        }
      %>    
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="status">Status: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select path="status" dojoType="dijit.form.Select" maxlength="50">
              <form:option value="${rolesModel.status}" selected disabled>${rolesModel.statusDefinition}</form:option>
              <form:option value="1" label="Active"/>
              <form:option value="0" label="Inactive"/>
            </form:select>
          </p>
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="status">Status: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select path="status" dojoType="dijit.form.FilteringSelect" maxlength="50">
              <form:option value="1" label="Active"/>
              <form:option value="0" label="Inactive"/>
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <%
        }
      %>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="comments">Comments: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p><form:input path="comments" dojoType="dijit.form.TextBox" maxlength="255" value="${rolesModel.comments}" cssStyle="width:500px"/></p>
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="comments">Comments: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="comments" dojoType="dijit.form.TextBox" maxlength="255" cssStyle="width:500px"/>
          </p>
        </cmr:column>
      </cmr:row>
      <%
        }
      %>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="createTs">Create TimeStamp: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${rolesModel.createTs}</p>
          <form:hidden id="createTs" path="createTs" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="createTs">Create TimeStamp: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <% SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy"); %>
          <% Date date = new Date();%>
          <% String currentDate = sdf.format(date); %>
          <p><%= currentDate %></p>
        </cmr:column>
      </cmr:row>
      <%
        }
      %>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="createBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${rolesModel.createBy}</p>
          <form:hidden id="createBy" path="createBy" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="createBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${rolesModel.user}</p>
        </cmr:column>
      </cmr:row>
      <%
        }
      %>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="updateTs">Update TimeStamp: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${rolesModel.updateTs}</p>
          <form:hidden id="updateTs" path="updateTs" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="updateTs">Update TimeStamp: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <% SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy"); %>
          <% Date date = new Date();%>
          <% String currentDate = sdf.format(date); %>
          <p><%= currentDate %></p>
        </cmr:column>
      </cmr:row> 
      <%
        }
      %>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="updateBy">Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${rolesModel.updateBy}</p>
          <form:hidden id="updateBy" path="updateBy" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="updateBy">Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${rolesModel.user}</p>
        </cmr:column>
      </cmr:row>
      <%
        }
      %>
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="RolesService.saveRole(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Update" onClick="RolesService.saveRole(false)" highlight="true" />
    <%} %>
    <cmr:button label="Back to Roles" onClick="window.location = '${contextPath}/code/roles'" pad="true" /> 
  </cmr:buttonsRow>
  <br>
  
</cmr:section>
<cmr:model model="rolesModel" />