<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.system.UserModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  UserModel user = (UserModel) request.getAttribute("user");
			boolean newEntry = false;
			if (user.getState() == BaseModel.STATE_NEW) {
				newEntry = true;
				user.setStatus("1");
			} else {
			}
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
<%if (newEntry) {%>
  FormManager.addValidator('userName', Validators.REQUIRED, [ 'User Name / ID' ]);
    FormManager.addValidator('userName', Validators.BLUEPAGES, [ 'User Name / ID' ]);
<%}%>
  FormManager.ready();
  });
</script>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:form method="POST" action="${contextPath}/user" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="user">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add User" : "Update User"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="userName">User Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${user.userName}</p>
          <form:hidden id="userName" path="userName" />
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="userId">User ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${user.userId}</p>
          <form:hidden id="userId" path="userId" />
        </cmr:column>
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="userName">User Name / ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <cmr:bluepages model="user" namePath="userName" idPath="userId" showId="true" />
          </p>
        </cmr:column>
      </cmr:row>
      <%
        }
      %>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="createTs">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${user.createTs != null}">
              <fmt:formatDate type="date" value="${user.createTs}" pattern="yyyy-MM-dd" />
              <form:hidden id="createTs" path="createTs" />
            </c:if>
            <c:if test="${user.createTs == null}">
              -
            </c:if>
          </p>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="updateTs">Last Updated: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${user.updateTs != null}">
              <fmt:formatDate type="date" value="${user.updateTs}" pattern="yyyy-MM-dd" />
              <form:hidden id="updateTs" path="updateTs" />
            </c:if>
            <c:if test="${user.updateTs == null}">
              -
            </c:if>
          </p>
        </cmr:column>
      </cmr:row>


      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="createTs">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${user.createBy != null}">
              ${user.createBy}
              <form:hidden id="createBy" path="createBy" />
            </c:if>
            <c:if test="${user.createBy == null}">
              -
            </c:if>
          </p>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="updateTs">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${user.updateBy != null}">
              ${user.updateBy}
              <form:hidden id="updateBy" path="updateBy" />
            </c:if>
            <c:if test="${user.updateBy == null}">
              -
            </c:if>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="status">Status: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:radiobutton path="status" value="1" />
            Active &nbsp;&nbsp;
            <form:radiobutton path="status" value="0" />
            Not Active
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="comments">Comments: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="comments" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>

    </cmr:section>
  </cmr:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="UserService.saveUser(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Save" onClick="UserService.saveUser(false)" highlight="true" />
    <%} %>
   <%-- <cmr:button label="Add Another User" onClick="UserService.addUser()" highlight="false" pad="true" />--%>
    <cmr:button label="Back to User List" onClick="window.location = '${contextPath}/users'" pad="true" />
  </cmr:buttonsRow>
  <br>
  <%if (!newEntry){ %>
  <cmr:boxContent>
    <cmr:form method="POST" action="${contextPath}/userroles" id="frmCMRRoles" name="frmCMRRoles" class="ibm-column-form ibm-styled-form"
      modelAttribute="userrole">
      <input type="hidden" name="userId" value="${user.userId}">
      <cmr:modelAction formName="frmCMRRoles" />
      <input type="hidden" name="comments" id="userrolecmt" value="">
      <input type="hidden" name="rolesToAdd" id="userrolestoadd" value="">
      <cmr:tabs />
      <cmr:section>
        <cmr:row topPad="8">
          <cmr:column span="6">
            <h3>Assigned Roles</h3>
          </cmr:column>
        </cmr:row>
        <cmr:row topPad="10" addBackground="false">
          <cmr:column span="6">
            <cmr:grid usePaging="false" url="/userrolelist.json" id="userRoleListGrid" hasCheckbox="false" checkBoxKeys="userId,roleId,subRoleId"
              span="6" height="200">
              <cmr:gridParam fieldId="userId" value="${user.userId}" />
              <cmr:gridCol width="12%" field="roleDesc" header="Role" />
              <cmr:gridCol width="12%" field="subRoleDesc" header="Sub-role" />
              <cmr:gridCol width="8%" field="roleStatus" header="Status">
                <cmr:formatter>
               function(value) {
                 if (value == '0'){
                   return 'Not Active';
                 } 
                 return 'Active';
               }
             </cmr:formatter>
              </cmr:gridCol>
              <cmr:gridCol width="12%" field="createBy" header="Created By" />
              <cmr:gridCol width="11%" field="createTsString" header="Create Date" />
              <cmr:gridCol width="12%" field="updateBy" header="Last Updated By" />
              <cmr:gridCol width="11%" field="updateTsString" header="Last Updated" />
              <cmr:gridCol width="*" field="comments" header="Comments" />

            </cmr:grid>
          </cmr:column>
        </cmr:row>
        <cmr:row>
        &nbsp;
      </cmr:row>
        <%--<cmr:buttonsRow>
          <cmr:button label="Add Roles" onClick="UserService.addRoles()" highlight="true" />
          <cmr:button label="Remove Roles" onClick="UserService.removeRoles()" pad="true" />
        </cmr:buttonsRow>--%>
        <br>
      </cmr:section>
    </cmr:form>
  </cmr:boxContent>
  <jsp:include page="addroles.jsp" />
  <%} %>
</cmr:section>
<cmr:model model="user" />