<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration" %>
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
    FilteringDropdown.loadItems('searchCriteria', 'searchCriteria_spinner', 'lov', 'fieldId=SearchCriteria&cmrIssuingCntry=*');
    FormManager.addValidator('searchCriteria', Validators.REQUIRED, [ 'Search Criteria' ]);
    FormManager.ready();
  });
  
  var actions = (function() {
    return {
      update : function(sourceName, seqNo) {
        window.location = cmr.CONTEXT_ROOT + '/code/gcars_updt_queue_form?sourceName=' + encodeURIComponent(sourceName) + '&seqNo=' + encodeURIComponent(seqNo);
      },
      actionIcons : function(value, rowIndex) {
        var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
        var rowData = this.grid.getItem(0);
        
        if (rowData == null) {
          return ''; // not more than 1 record
        }
        
        rowData = this.grid.getItem(rowIndex);
        
        var actionsTag = '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="actions.update(\'' + rowData.tcrFileNm + '\', \'' + rowData.seqNo + '\');" />';
        return actionsTag;
      },
      refresh : function(){
        var url = cmr.CONTEXT_ROOT + '/code/gcars_updt_queue_list.json';
        CmrGrid.refresh('gcarsUpdtQueueGrid', url, 'sourceName=:sourceName&searchCriteria=:searchCriteria');
      }
    };
  })();
</script>

<cmr:form method="POST" action="#" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="gcars_updt_queue" id="frmCMR">
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
      <cmr:model model="br_ibm_bo" />
      <cmr:modelAction formName="frmCMR" />
      
      <cmr:row topPad="8">
        <cmr:column span="2">
          <cmr:label fieldId="searchCriteria">Please select search criteria:</cmr:label>
						<form:select dojoType="dijit.form.FilteringSelect" id="searchCriteria"
							searchAttr="name" style="display: block;" maxHeight="200"
							required="false" path="searchCriteria" placeHolder="Select Search Type">
						</form:select>
          <cmr:label fieldId="sourceName">Source Name: 
            <cmr:info text="Enter Source Name:"></cmr:info>
          </cmr:label>
          <input id="sourceName" name="sourceName" dojoType="dijit.form.TextBox" />
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
          <h3>GCARS UPDT QUEUE List</h3>
        </cmr:column>
      </cmr:row>
      
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/gcars_updt_queue_list.json" id="gcarsUpdtQueueGrid" span="6" height="400" usePaging="true">
            <cmr:gridCol width="180px" field="sourceName" header="Source Name" />
            <cmr:gridCol width="60px" field="seqNo" header="Sequence No" />
            <cmr:gridCol width="60px" field="cmrNo" header="CMR No" />
            <cmr:gridCol width="90px" field="procStatus" header="Process Status">
              <cmr:formatter>
                function(value) {
                  if (value == 'E'){
                     return "<span style='color:red'>Error</span>";
                   } else if (value == 'C'){
                     return "<span style='color:blue'>Completed</span>";  
                   } else if(value == 'I') {
                     return "<span style='color:blue'>In Progress</span>";
                   } else if(value == 'P') {
                     return "<span style='color:green'>Pending</span>";
                   } else {
                   }
                }
              </cmr:formatter>
            </cmr:gridCol>
            <cmr:gridCol width="300px" field="procMsg" header="Process Message" />
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
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" />
  </cmr:buttonsRow>
</cmr:section>