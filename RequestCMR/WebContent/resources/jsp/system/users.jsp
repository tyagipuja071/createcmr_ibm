<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FilteringDropdown.loadItems('requesterFor', null, 'bds', 'fieldId=CMRIssuingCountry');
  });
</script>
<cmr:form method="POST" action="${contextPath}/users" name="frmCMRSearch" class="ibm-column-form ibm-styled-form" modelAttribute="users">
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
      <cmr:row>
        <cmr:column span="6">
          <cmr:note text="Use the filters to search for users. Part of the Name or ID can be used. Search is case-insensitive."></cmr:note>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="2">
          <cmr:label fieldId="userName">Name:</cmr:label>
          <input id="userName" dojoType="dijit.form.TextBox" name="userName">
        </cmr:column>
        <cmr:column span="2">
          <cmr:label fieldId="userId">User ID:</cmr:label>
          <input id="userId" dojoType="dijit.form.TextBox" name="userId">
        </cmr:column>
        <cmr:column span="1">
          <cmr:button label="Filter" onClick="UserService.filterUsers()"></cmr:button>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
        <cmr:column span="2">
          <cmr:label fieldId="role">Role:</cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="role" searchAttr="name" style="display: block;" maxHeight="200"
              required="false" path="role" placeHolder="Filter by Role">
                <option value=""></option>
                <option value="REQUESTER">Requester</option>
                <option value="PROCESSOR">Processor</option>
                <option value="CMDE">CMDE Administrator</option>
                <option value="ADMIN">System Administrator</option>
                <option value="WS_ADMIN">WebService Administrator</option>
                <option value="USER">FindCMR User</option>
            </form:select>
        </cmr:column>
        <cmr:column span="2">
          <cmr:label fieldId="requesterFor">Requester For:
            <cmr:info text="Filters the users who have SUBMITTED requests under the chosen country. Users who have created drafts only will not be part of the results." />
          </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="requesterFor" searchAttr="name" style="display: block;" maxHeight="200"
              required="false" path="requesterFor" placeHolder="Filter by Country">
            </form:select>
        </cmr:column>
      </cmr:row>
      <cmr:row>
         &nbsp;
      </cmr:row>
    </cmr:section>

  </cmr:boxContent>
  <cmr:boxContent>
    <cmr:tabs />

    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>System Users</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/userlist.json" id="userListGrid" span="6" useFilter="true" loadOnStartup="true" >
            <cmr:gridParam fieldId="userName" />
            <cmr:gridParam fieldId="userId" />
            <cmr:gridParam fieldId="role" />
            <cmr:gridParam fieldId="requesterFor" />
            <cmr:gridCol width="15%" field="userName" header="Name">
              <cmr:formatter functionName="UserService.userIdFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="15%" field="userId" header="Intranet ID" />
            <cmr:gridCol width="6%" field="status" header="Status">
              <cmr:formatter functionName="UserService.statusFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="12%" field="createBy" header="Created By" />
            <cmr:gridCol width="9%" field="createTsString" header="Create Date" />
            <cmr:gridCol width="12%" field="updateBy" header="Last Updated By" />
            <cmr:gridCol width="9%" field="updateTsString" header="Last Updated" />
            <cmr:gridCol width="*" field="comments" header="Comments" />

          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
      </cmr:row>
    </cmr:section>
  </cmr:boxContent>
</cmr:form>
<cmr:section alwaysShown="true">
  <%-- <cmr:buttonsRow>
    <cmr:button label="Add User" onClick="UserService.addUser()" highlight="true" />
  </cmr:buttonsRow>--%>
</cmr:section>
<cmr:model model="users" />