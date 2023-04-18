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
        window.location = cmr.CONTEXT_ROOT + '/code/us_enterprise_form';
      },
      update : function(entNo) {
        window.location = cmr.CONTEXT_ROOT + '/code/us_enterprise_form?mandt=' + mandt + '&entNo=' + entNo;
      },
      actionIcons : function(value, rowIndex) {
        var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
        var rowData = this.grid.getItem(0);
        
        if (rowData == null) {
          return ''; // not more than 1 record
        }
        
        rowData = this.grid.getItem(rowIndex);
        
        var actionsTag = '';
        actionsTag += '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="actions.update(\'' + rowData.entNo + '\')" />';
               
        return actionsTag;
      },
      refresh : function(){
        var url = cmr.CONTEXT_ROOT + '/code/us_enterprise_list.json';
        CmrGrid.refresh('usenterpriseGrid', url, 'entNo=:entNo');
      }
    };
  })();
</script>

<cmr:form method="POST" action="#" name="frmCMR" class="ibm-column-form ibm-styled-form"
  modelAttribute="us_enterprise" id="frmCMR">
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
      <cmr:model model="us_enterprise" />
      <cmr:modelAction formName="frmCMR" />
      
      <cmr:row topPad="8">
        <cmr:column span="2">
          <cmr:label fieldId="entNo">Enterprise: 
            <cmr:info text="Enter Enterprise Number."></cmr:info>
          </cmr:label>
          <input id="entNo" name="entNo" dojoType="dijit.form.TextBox" />
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
          <h3>US Enterprise List</h3>
        </cmr:column>
      </cmr:row>
      
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/us_enterprise_list.json" id="usenterpriseGrid" span="6" height="400" usePaging="true">
            <cmr:gridCol width="80px" field="entNo" header="Enterprise No.">
              <cmr:formatter>
                function(value){
                  return "<a href=\"javascript: actions.update('"+ value +"');\">" + value + "</a>";
                }
              </cmr:formatter>
            </cmr:gridCol>
            <cmr:gridCol width="120px" field="entLegalName" header="Enterprise Legal Name" />
            <cmr:gridCol width="50px" field="loevm" header="LOEVM">
              <cmr:formatter>
                function(value) {
                  if (value == 'X'){
                     return "<span style='color:red'>Deleted</span>";
                   } else {
                     return "<span style='color:green'>Active</span>";
                   }
                }
              </cmr:formatter>
            </cmr:gridCol>
	           
             <cmr:gridCol width="120px" field="updateBy" header="Last Updated by" />
              <cmr:gridCol width="120px" field="updateDt" header="Last Update Date" />
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
</cmr:form>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Add US Enterprise" onClick="actions.add()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>