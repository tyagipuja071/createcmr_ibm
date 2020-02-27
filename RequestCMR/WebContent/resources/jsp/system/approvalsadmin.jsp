<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
String contextPath = request.getContextPath();
String createTs = UIMgr.getText("grid.approvalsCreateDateTime");
String infoTxt = UIMgr.getText("info.timeStampInfo");
String tz = SystemConfiguration.getValue("DATE_TIMEZONE");
String title=createTs+" ("+tz+")";
String createTsHeader =title+"<img src=\""+contextPath+"/resources/images/info-bubble-icon.png\" title=\""+infoTxt+"\" class=\"cmr-info-bubble\">";
%>
<script>
  var overrides = {};
  dojo.addOnLoad(function() {
    FormManager.ready();
    window.setTimeout('retrieveApprovals()', 1000);
  });
  
  function retrieveApprovals(){
    console.log('get approvals');
    CmrGrid.refresh('APPROVALS_GRID', cmr.CONTEXT_ROOT+'/approval/list.json', 'reqId=:reqId&adminOR=Y');
  }

  
  function adminActionFormatter(value, rowIndex){
    rowData = this.grid.getItem(rowIndex);
    var actions = '';
    var approvalId = rowData.approvalId[0];
    var displayName = rowData.displayName[0];
    if (displayName){
      displayName = displayName.replace(/'/g, "\\'");
    }
    var intranetId = rowData.intranetId[0];
    actions += '<input type="button" value="Override" class="cmr-grid-btn" onclick="showOverride(' + approvalId + ', \''+displayName+'\', \''+intranetId+'\')">';
    return actions;
  }
  
  function showOverride(approvalId, displayName, intranetId){
    overrides.approvalId = approvalId;
    overrides.displayName = displayName;
    overrides.intranetId = intranetId;
    cmr.showModal('adminApprovalModal');
  }
  
  /**
   * Formatter for NotesID
   * 
   */
  function notesIdFormatter(value, rowIndex) {
    rowData = this.grid.getItem(rowIndex);
    var intranetId = rowData.approverId[0];
    return '<span class="cmr-grid-tooltip" title="' + intranetId + '">' + value + '</span>';
  }

  function processOverride() {
    if (FormManager.validate('frmCMR_adminApprovalModal', true)) {
      cmr.modalmode = true;
      FormManager.doHiddenAction('frmCMR_adminApprovalModal', 'ADMIN_OVERRIDE', cmr.CONTEXT_ROOT + '/approval/action.json', true, refreshApprovalGrid, true);
    }

  }
  
  function adminApprovalModal_onLoad() {
    cmr.currentModalId = 'adminApprovalModal';
    MessageMgr.clearMessages(true);
    FormManager.setValue('approvalId', overrides.approvalId);
    FormManager.setValue('displayName', overrides.displayName);
    FormManager.setValue('approval_cmt', '');
    FormManager.setValue('status', '');
    FormManager.setValue('intranetId', overrides.intranetId);
    FormManager.setValue('displayName_bpcont', overrides.displayName+':'+overrides.intranetId);
    dojo.byId('displayName_readonly').innerHTML =  overrides.intranetId;
  }

  function refreshApprovalGrid(result){
    if (result.success) {
      try {
        cmr.hideModal('adminApprovalModal');
        CmrGrid.refresh('APPROVALS_GRID');
      } catch (e) {
        // safety for non-modal functions that used the method
      }
    }
  }
  
  function backToList(){
    window.location = cmr.CONTEXT_ROOT+'/approvalsadminlist';
  }

</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/approvalsadmin" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="approval">
    <form:hidden path="reqId"/>
    <cmr:section>
      <cmr:row addBackground="true">
        <cmr:column span="6">
          <h3>Pending Approvals for Request ${approval.reqId}</h3>
        </cmr:column>
      </cmr:row>
    </cmr:section>
  </form:form>
  <cmr:section>
    <cmr:row addBackground="true">
      <cmr:column span="6">
        <cmr:grid url="/approval/list.json" id="APPROVALS_GRID" span="6" height="250" usePaging="false">
          <cmr:gridParam fieldId="reqId" value="-1" />
          <cmr:gridParam fieldId="adminOR" value="Y" />
          <cmr:gridCol width="200px" field="notesId" header="${ui.grid.approvalsApprover}">
            <cmr:formatter functionName="notesIdFormatter" />
          </cmr:gridCol>
          <cmr:gridCol width="auto" field="type" header="${ui.grid.approvalsType}" />
          <cmr:gridCol width="170px" field="createTsString" header="<%=createTsHeader%>" />
          <cmr:gridCol width="140px" field="statusStr" header="${ui.grid.approvalsCurrentStatus}" />
          <cmr:gridCol width="120px" field="actions" header="${ui.grid.approvalsActions}">
            <cmr:formatter functionName="adminActionFormatter" />
          </cmr:gridCol>
        </cmr:grid>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">&nbsp;</cmr:row>
  </cmr:section>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
     <cmr:button label="Back to List" onClick="backToList()" highlight="false" pad="false"/>
  </cmr:buttonsRow>
</cmr:section>
<jsp:include page="/resources/jsp/system/approvalsoverride.jsp" />  
<cmr:model model="approval" />