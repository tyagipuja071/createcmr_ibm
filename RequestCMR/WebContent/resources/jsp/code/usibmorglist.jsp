<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  String mandt = SystemConfiguration.getValue("MANDT").toString();
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FormManager.ready();
  });
  
  var mandt = '<%= mandt %>';
  
  var actions = (function() {
    return {
      add : function() {
        window.location = cmr.CONTEXT_ROOT + '/code/us_ibm_org_form';
      },
      update : function(aLevel1Value, aLevel2Value, aLevel3Value, aLevel4Value) {
        window.location = cmr.CONTEXT_ROOT + '/code/us_ibm_org_form?mandt=' + mandt + '&aLevel1Value=' + encodeURIComponent(aLevel1Value) + '&aLevel2Value=' + encodeURIComponent(aLevel2Value) + '&aLevel3Value=' + encodeURIComponent(aLevel3Value) + '&aLevel4Value=' + encodeURIComponent(aLevel4Value);
      },
      actionIcons : function(value, rowIndex) {
        var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
        var rowData = this.grid.getItem(0);
        
        if (rowData == null) {
          return ''; // not more than 1 record
        }
        
        rowData = this.grid.getItem(rowIndex);
        
        var actionsTag = '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="actions.update(\'' + rowData.aLevel1Value + '\', \'' + rowData.aLevel2Value + '\', \'' + rowData.aLevel3Value + '\', \'' + rowData.aLevel4Value + '\')" />';
        return actionsTag;
      },
      refresh : function(){
        var url = cmr.CONTEXT_ROOT + '/code/us_ibm_org_list.json';
        CmrGrid.refresh('usibmorgGrid', url, 'aLevel1Value=:aLevel1Value');
      }
    };
  })();
</script>

<form:form method="POST" action="#" name="frmCMR" class="ibm-column-form ibm-styled-form"
  modelAttribute="us_ibm_org" id="frmCMR">
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
      <cmr:model model="us_ibm_bo" />
      <cmr:modelAction formName="frmCMR" />
      
      <cmr:row topPad="8">
        <cmr:column span="2">
          <cmr:label fieldId="aLevel1Value">A_LEVEL_1_VALUE: 
            <cmr:info text="Enter A_LEVEL_1_VALUE of code."></cmr:info>
          </cmr:label>
          <input id="aLevel1Value" name="aLevel1Value" dojoType="dijit.form.TextBox"  />
        </cmr:column>
      
        <cmr:column span="2">
          <cmr:button label="Filter" onClick="actions.refresh()"></cmr:button>
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
          <h3>US IBM ORG List</h3>
        </cmr:column>
      </cmr:row>
      
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/us_ibm_org_list.json" id="usibmorgGrid" span="6" height="400" usePaging="true">
            <cmr:gridCol width="120px" field="aLevel1Value" header="A_LEVEL_1_VALUE" />
            <cmr:gridCol width="120px" field="aLevel2Value" header="A_LEVEL_2_VALUE" />
            <cmr:gridCol width="120px" field="aLevel3Value" header="A_LEVEL_3_VALUE" />
            <cmr:gridCol width="120px" field="aLevel4Value" header="A_LEVEL_4_VALUE" />
            <cmr:gridCol width="80px" field="action" header="Action" > 
              <cmr:formatter functionName="actions.actionIcons" />
            </cmr:gridCol>
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      
      <cmr:row topPad="10">
      </cmr:row>
      
    </cmr:section>
</cmr:boxContent>
</form:form>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Add US IBM ORG" onClick="actions.add()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>