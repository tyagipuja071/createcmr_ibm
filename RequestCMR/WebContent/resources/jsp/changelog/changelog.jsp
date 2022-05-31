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
    FilteringDropdown.loadItems('requestStatus', 'requestStatus_spinner',  'lov', 'fieldId=ChangeLogReqStatus');
    FilteringDropdown.loadItems('cmrIssuingCountry', 'cmrIssuingCountry_spinner', 'changelog.cmrIssuingCountry'); 
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
      <cmr:row>
        <cmr:column span="1">
          <p>
            <cmr:label fieldId="cmrIssuingCountry">
              CMR Issuing Country:
              <cmr:spinner fieldId="cmrIssuingCountry" />
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="1">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCountry" itemLabel="name" searchAttr="id" style="display: block;" maxHeight="200"
              path="cmrIssuingCountry" placeHolder="Select CMR Issuing Country">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <cmr:label fieldId="requestStatus">
              Request Status:
              <cmr:spinner fieldId="requestStatus" />
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="1">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="requestStatus" searchAttr="name" style="display: block;" maxHeight="200"
              path="requestStatus" placeHolder="Select Request Status">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <br>
      <cmr:row>
        <cmr:button label="${ui.btn.search}" onClick="doSearchChangeLogs()" highlight="true" />
      </cmr:row>
      <br>      
    </cmr:section>
  </cmr:boxContent>
   
  <cmr:boxContent>   
    <cmr:tabs />
    <cmr:section alwaysShown="true">
      <cmr:row topPad="8">
        <cmr:column span="5">
          <h4>Change Log Records</h4>
        </cmr:column>
        <cmr:column span="1">
          <cmr:button label="${ui.btn.exportFullReport}" onClick="doDownloadExportFullReport()" />
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/changeloglist.json" id="changeLogGrid" span="6" useFilter="true" usePaging="true">
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

<form id="frmCMRFullReportDownLoad" name="frmCMRFullReportDownLoad" method="POST">
  <input type="hidden" id="katr6" name="katr6" />
  <input type="hidden" id="zzkvCusNo" name="zzkvCusNo" />
</form>
