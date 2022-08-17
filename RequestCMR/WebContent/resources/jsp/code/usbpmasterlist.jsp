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
        window.location = cmr.CONTEXT_ROOT + '/code/us_bp_master_form';
      },
      update : function(companyNo) {
        window.location = cmr.CONTEXT_ROOT + '/code/us_bp_master_form?mandt='+mandt+'&companyNo='+encodeURIComponent(companyNo);
      },
      actionIcons : function(value, rowIndex) {
        var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
        var rowData = this.grid.getItem(0);
        
        if (rowData == null) {
          return '';
        }
        
        rowData = this.grid.getItem(rowIndex);
        
        var actionsTag = '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="actions.update(\'' + rowData.companyNo + '\')" />';
        return actionsTag;
      },
      refresh : function(){
        var url = cmr.CONTEXT_ROOT + '/code/us_bp_master_list.json';
        CmrGrid.refresh('usbpmasterGrid', url, 'companyNo=:companyNo');
      }
    };
  })();
</script>

<cmr:form method="POST" action="#" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="us_bp_master" id="frmCMR">
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
    
      <cmr:model model="us_bp_master" />
      <cmr:modelAction formName="frmCMR" />
      
      <cmr:row topPad="8">
        <cmr:column span="2">
          <cmr:label fieldId="companyNo">Company No: 
            <cmr:info text="Enter Company No of code."></cmr:info>
          </cmr:label>
          <input id="companyNo" dojoType="dijit.form.TextBox" name="companyNo">
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
          <h3>US Business Partner Master List</h3>
        </cmr:column>
      </cmr:row>
      
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/us_bp_master_list.json" id="usbpmasterGrid" span="6" height="400" usePaging="true">
            <cmr:gridCol width="120px" field="companyNo" header="Company No">
              <cmr:formatter>
                function(value) {
                  return "<a href=\"javascript: actions.update('"+ value +"');\">" + value + "</a>";
                }
              </cmr:formatter>
            </cmr:gridCol>
            <cmr:gridCol width="120px" field="cmrNo" header="CMR No" />
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
            <cmr:gridCol width="150px" field="createdTsStr" header="Create Date" />
            <cmr:gridCol width="150px" field="createdBy" header="Created By" />
            <cmr:gridCol width="150px" field="updatedTsStr" header="Update Date" />
            <cmr:gridCol width="150px" field="updatedBy" header="Updated By" />
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
    <cmr:button label="Add US Business Partner" onClick="actions.add()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>