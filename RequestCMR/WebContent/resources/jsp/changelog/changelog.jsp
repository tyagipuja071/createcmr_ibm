<%@page import="java.util.List"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  AppUser user = AppUser.getUser(request);
%>
<script src="${resourcesPath}/js/changelog/changelog.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FilteringDropdown.loadItems('tablName', 'tablName_spinner', 'changelog.table');
  });
</script>



<form:form method="POST" action="${contextPath}/changelog" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="changelog">
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section alwaysShown="true">
      <cmr:row>
        <cmr:column span="6">
          <h4>Search Change Log</h4>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <label for="requestIdStr">Request ID: </label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input size="30" id="requestIdStr" path="requestIdStr" />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">      
        <p>
          <label for="userId">User: </label>
        </p>
        </cmr:column>
        <cmr:column span="2">      
        <p>
          <form:input size="30" id="userId" path="userId" />
        </p>
        </cmr:column>        
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <cmr:label fieldId="tablName">
              Table Name:
              <cmr:spinner fieldId="tablName" />
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="1">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="tablName" itemLabel="name" searchAttr="id" style="display: block;" maxHeight="200"
              path="tablName" placeHolder="Select Table">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <label for="changeDateFrom">Change Date From: </label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <cmr:date path="changeDateFrom" />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <label for="changeDateTo">Change Date To: </label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <cmr:date path="changeDateTo" />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <label for="cmrNo">CMR No.: </label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input size="30" id="cmrNo" path="cmrNo" />
          </p>
        </cmr:column>
      </cmr:row>
      <br>
      <cmr:buttonsRow>
        <cmr:button label="${ui.btn.search}" onClick="doSearchChangeLogs()" highlight="true" />
      </cmr:buttonsRow>
      <br>      
    </cmr:section>
  </cmr:boxContent>
   
  <cmr:boxContent>   
    <cmr:section alwaysShown="true">
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h4>Change Log Records</h4>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/changeloglist.json" id="changeLogGrid" span="6" useFilter="true" usePaging="false">
            <cmr:gridParam fieldId="requestIdStr" value=":requestIdStr" />
            <cmr:gridParam fieldId="userId" value=":userId" />
            <cmr:gridParam fieldId="tablName" value=":tablName" />
            <cmr:gridParam fieldId="changeDateFrom" value=":changeDateFrom" />
            <cmr:gridParam fieldId="changeDateTo" value=":changeDateTo" />
            <cmr:gridCol width="80px" field="requestId" header="Request ID" >
              <cmr:formatter functionName="reqIdFormatter" /> 
            </cmr:gridCol>
            <cmr:gridCol width="120px" field="changeTsStr" header="Change Timestamp" />
            <cmr:gridCol width="90px" field="tablName" header="Table" />
            <cmr:gridCol width="80px" field="addrTyp" header="Address Type" />
            <cmr:gridCol width="70px" field="action" header="Action" />
            <cmr:gridCol width="90px" field="fieldName" header="Field" />
            <cmr:gridCol width="100px" field="oldValue" header="Old value" />
            <cmr:gridCol width="100px" field="newValue" header="New Value" />
            <cmr:gridCol width="auto" field="userId" header="User ID" />           
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
      </cmr:row>
    </cmr:section>
  </cmr:boxContent>
</form:form>
