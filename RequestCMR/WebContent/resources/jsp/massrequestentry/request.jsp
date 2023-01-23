<%@page import="com.ibm.cio.cmr.request.ui.template.TemplateManager"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.DataModel"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
DataModel rdcdata = (DataModel) request.getAttribute("rdcdata");
boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
AppUser user = AppUser.getUser(request);
String findCMRUrl = SystemConfiguration.getValue("FIND_CMR_URL");
String actionUrl = request.getContextPath()+"/massrequest";
if (!newEntry){
  actionUrl += "/"+reqentry.getReqId();
} 
PageManager.initFor(request, reqentry);
String tabsJS = newEntry ? "triggerSave" : "switchTabs";
String mandt = SystemConfiguration.getValue("MANDT");
String autoEngineIndc= (String) request.getAttribute("autoEngineIndc");
String entUpdType="";
if(reqentry.getEntUpdTyp()!= null ){
  entUpdType = reqentry.getEntUpdTyp();
}
String findCmrServer = findCMRUrl.contains("/") ? findCMRUrl.substring(0, findCMRUrl.lastIndexOf("/")) : findCMRUrl;

%>
<jsp:include page="/resources/jsp/requestentry/approvals_status.jsp" />
<%
Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
if (readOnly == null){
  readOnly = false;
  request.setAttribute("pageReadOnly", true);
}
%>
<script src="${resourcesPath}/js/cmr-pagemanager.js?${cmrv}" type="text/javascript"></script>
<cmr:view forGEO="US,LA">
  <script src="${resourcesPath}/js/cmr-scenarios_us_la.js?${cmrv}" type="text/javascript"></script>
</cmr:view>
<cmr:view exceptForGEO="US,LA">
  <script src="${resourcesPath}/js/cmr-scenarios.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<!--  Main Scripts -->
<script src="${resourcesPath}/js/massrequestentry/massrequestentry.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/findcmrsearch.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/massrequestentry/processing.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/comment.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/notify.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/attachment.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/approvals.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/auto/automation.js?${cmrv}" type="text/javascript"></script>


<!--  Validators per country. Add to the validations jsp -->
<jsp:include page="validations_js_mass.jsp" />

<jsp:include page="/resources/jsp/requestentry/trans.jsp" />
<script>
_findCmrServer = '<%=findCmrServer%>';
dojo.addOnLoad(function() {
    loadYourActionsDropDown();
    FormManager.setCheckFunction(promptForSaveBeforeLeave);
    FilteringDropdown.loadItems('rejectReason', 'rejectReason_spinner', 'lov', 'fieldId=RejectReasonProc');
    cmr.currentTab = 'GENERAL_REQ_TAB';
    <%if (reqentry.getReqType()!= null && (CmrConstants.REQ_TYPE_MASS_UPDATE.equals(reqentry.getReqType()) 
        || CmrConstants.REQ_TYPE_MASS_CREATE.equals(reqentry.getReqType())) && reqentry.getReqId() > 0){%>
        addMassFileValidator();
        
   <%if( (reqentry.getCmrIssuingCntry().equalsIgnoreCase("631")) && reqentry.getUserRole().equalsIgnoreCase("Processor")){%>
        //addDplCheckValidator();
    <%}%>
       <%if( (reqentry.getCmrIssuingCntry().equalsIgnoreCase("706")) && reqentry.getUserRole().equalsIgnoreCase("Processor")){%>
        addDPLValidator();
    <%}%>
    <%}%>
    
    <%if (reqentry.getReqType()!= null && (CmrConstants.REQ_TYPE_UPDT_BY_ENT.equals(reqentry.getReqType())) && reqentry.getReqId() > 0){%>
        addEnterpriseValidator();
    <%}%>
    <%--Defect 1745740: SPAIN_PP_Mass Delete Request_Requests getting completed even after no CMR are being added for deletion --%>
    <%if ((reqentry.getCmrIssuingCntry().equalsIgnoreCase("838") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("866") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("754") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("758")) && reqentry.getReqType()!= null && (CmrConstants.REQ_TYPE_DELETE.equals(reqentry.getReqType()) || CmrConstants.REQ_TYPE_REACTIVATE.equals(reqentry.getReqType())) && reqentry.getReqId() > 0){%>
        cmrsListGridValidator();
    <%}%>
    

    
    $('#cmr-your-actions').slideDown(500);
    enableSupportal();
  });
  
  function enableSupportal(){
    var error = dojo.byId('cmr-error-box-msg') ? dojo.byId('cmr-error-box-msg').innerHTML : null;
    if (error){
      $('#supportal').slideDown(1000);
    }
  }

  function promptForSaveBeforeLeave(url){
    var readOnly = false;
    if ('${yourActionsViewOnly}' == 'true'){ 
      readOnly = true;
    }
    if (!readOnly){
      cmr.urlRedirect = url;
      cmr.showConfirm('saveBeforeLeave()','Unsaved data for the current request will be lost.  Do you want to Save before leaving this request?', null, 'noSaveBeforeLeave()', {OK : 'Save', CANCEL: 'No Save'});
    } else {
      dojo.cookie('lastTab', '', {expires: -1});
      window.location = url;
    }
  }

  function saveBeforeLeave(){
    var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
    var reqType = FormManager.getActualValue('reqType');
    if (cmrCntry == '' || reqType == ''){
      cmr.showAlert('CMR Issuing Country and Request Type must be specified in order for you to save the request.');
      return;
    }
    var url = cmr.urlRedirect;
    if (url.indexOf('${contextPath}') == 0){
      url = url.substring('${contextPath}'.length);
    }
    dojo.byId('redirectUrl').value = url;
    dojo.cookie('lastTab', '', {expires: -1});
    FormManager.doAction('frmCMR', YourActions.Save, true);
  }
  
  function noSaveBeforeLeave(){
    dojo.cookie('lastTab', '', {expires: -1});
    window.location = cmr.urlRedirect;
  }
  
  function loadYourActionsDropDown(){
    if ('${yourActionsViewOnly}' == 'true'){ 
      cmr.showNode('viewOnlyText');
    }
    PageManager.setAlwaysAvailable(['yourAction']);
    
    var paramString = 'reqstatus=${reqentry.reqStatus}&lockind=${yourActionsLockInd}&reqtype=${reqentry.reqType}';
    paramString += '&sepvalind=' + ('${reqentry.sepValInd}'.trim() == '' ? 'N' : '${reqentry.sepValInd}');
    paramString += '&disableautoproc=' + ('${reqentry.disableAutoProc}'.trim() == '' ? 'N' : '${reqentry.disableAutoProc}');
    FilteringDropdown.loadItems('yourAction', 'yourAction_spinner', '${yourActionsSqlId}', paramString, false, '${ui.info.noneavailable}');
  }
</script>
<style>
  div.cmr-tabs {
    margin-top:20px;
  }
  div#ibm-content-main {
    padding-top: 20px;
  }
  div#cmr-info-box, div#cmr-error-box, div#cmr-validation-box {
    padding-top: 15px !important;
  }
</style>

<cmr:boxContent>
  <cmr:form id="frmCMR" method="POST" action="<%=actionUrl%>" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="reqentry">
    <jsp:include page="/resources/jsp/requestentry/actionstatus.jsp" />
    <input type="hidden" value="${yourActionsViewOnly}" id="viewOnlyPage">
    <input type="hidden" value="<%=mandt%>" id="mandt">
    <input type="hidden" value="<%=autoEngineIndc%>" id="autoEngineIndc">
    <cmr:modelAction formName="frmCMR" />
    <form:hidden path="fromUrl" id="fromUrl" />
    <form:hidden path="claimRole" /> 
    <form:hidden path="userRole" />
    <form:hidden id="overallStatus" path="overallStatus" />
    <form:hidden id="lockByNm" path="lockByNm" />
    <form:hidden path="redirectUrl"/>
    <form:hidden path="hasError"/>
    <form:hidden path="approvalResult" />    
    <%if (reqentry.getCmrIssuingCntry().equalsIgnoreCase("631")){%>
     <form:hidden path="saveRejectScore" />    
     <form:hidden path="dplMessage" />  
    <c:if test="${reqentry.dplChkTs != null }">
      <form:hidden path="dplChkTs" />
    </c:if>
    <c:if test="${reqentry.findCmrTs != null }">
      <form:hidden path="findCmrTs" />
    </c:if>
    <!-- to byPass db column defintions -->
    <form:hidden path="dplChkResult" />
    <form:hidden path="findCmrResult" />
    <form:hidden path="findDnbResult" />
    <% }%>
    
    <%if (!newEntry){%>
      <form:hidden path="createTs"/>
      <form:hidden path="reqStatus"/>
      <form:hidden path="requesterId"/>
      <form:hidden path="requesterNm"/>
      <form:hidden path="lastUpdtTs"/>
      <form:hidden path="lastUpdtBy"/>
      <form:hidden path="processedFlag" />
      <c:if test="${reqentry.processedTs != null }">
        <form:hidden path="processedTs" />
      </c:if>
      <form:hidden path="internalTyp" />
      <form:hidden path="sepValInd" />
      <form:hidden path="rdcProcessingStatus" />
      <c:if test="${reqentry.rdcProcessingTs != null }">
        <form:hidden path="rdcProcessingTs" />
      </c:if>
      <form:hidden path="covBgRetrievedInd" />
      <form:hidden path="rdcProcessingMsg" />
      <%if(CmrConstants.YES_NO.Y.toString().equals(reqentry.getLockInd())){%>
        <form:hidden path="lockTs"/>
        <form:hidden path="lockInd"/>
        <form:hidden path="lockBy"/> 
      <%}%>
      <input type="hidden" value="<%=entUpdType%>" id="entUpdType">  
    <%}%>
    
    <!-- Your Actions Dropdown -->
    <div title="Your Actions" id="cmr-your-actions" class="cmr-actions ${yourActionsViewOnly == true ? "view-only" : " cmr-actions-locked"}" style="display:none">
      <div class="cmr-action-dd">
        <form:select cssStyle="width:260px" dojoType="dijit.form.FilteringSelect" id="yourAction" searchAttr="name" style="display: inline-block;" maxHeight="200" required="false" path="yourAction" placeHolder="${ui.yourAction}" />
        <img title="Proceed with the selected Action" class="cmr-proceed-icon" src="${resourcesPath}/images/play.webp" onclick="processRequestAction()">
      </div>
      <div class="cmr-action-txt" id="viewOnlyText" style="display:none">View Only</div>
    </div>
    <script>
      addMoveHandler();
    </script>

    <%
      if (!"Viewer".equals(reqentry.getUserRole()) && CmrConstants.APPROVAL_RESULT_PENDING.equals(reqentry.getApprovalResult())) {
    %>
    <cmr:row>
      <cmr:column span="6">
        <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
        <cmr:note text="${ui.info.pendingApproval}" />
      </cmr:column>
    </cmr:row>
    <br>
    <%
      }
    %>

    <%
      if ("Requester".equals(reqentry.getUserRole()) && CmrConstants.APPROVAL_RESULT_COND_APPROVED.equals(reqentry.getApprovalResult())) {
    %>
    <br>
    <cmr:row>
      <cmr:column span="6">
        <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
        <cmr:note text="${ui.info.condApproved}" />
      </cmr:column>
    </cmr:row>
    <br>
    <%
      }
    %>
         <%
      if ("Requester".equals(reqentry.getUserRole()) && CmrConstants.APPROVAL_RESULT_COND_CANCELLED.equals(reqentry.getApprovalResult())) {
    %>
    <br>
    <cmr:row>
      <cmr:column span="6">
        <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
        <cmr:note text="${ui.info.condCancelled}" />
      </cmr:column>
    </cmr:row>
    <br>
    <%
      }
    %>

    <!-- Tabs section -->
    <cmr:tabs>
      <cmr:tab label="${ui.tab.general}" id="MAIN_GENERAL_TAB" active="true" sectionId="GENERAL_REQ_TAB" gridIds="COMMENT_LIST_GRID"/>
      <cmr:tab label="${ui.tab.processing}" id="MAIN_PROC_TAB" sectionId="PROC_REQ_TAB" gridIds="CMR_LIST_GRID"/>
      <cmr:tab label="${ui.tab.attach}" id="MAIN_ATTACH_TAB" sectionId="ATTACH_REQ_TAB" gridIds="ATTACHMENT_GRID"/>
      <cmr:tab label="${ui.tab.notify}" id="MAIN_NOTIFY_TAB" sectionId="NOTIFY_REQ_TAB" gridIds="NOTIFY_LIST_GRID"/>
      <cmr:tab label="${ui.tab.approvals}" id="MAIN_APPROVALS_TAB" sectionId="APPROVALS_REQ_TAB" gridIds="APPROVALS_GRID"/> 
    </cmr:tabs>

    <jsp:include page="general.jsp" />
      <%if (!newEntry){ %>
    <jsp:include page="/resources/jsp/requestentry/attachment.jsp"/>
      <%}%>
    <input type="hidden" name="statusChgCmt" id="statusChgCmt_main" >
    <%if (!newEntry && CmrConstants.REQ_TYPE_UPDT_BY_ENT.equals(reqentry.getReqType())){ %>
      <jsp:include page="processing.jsp" />
    <%}%>
  </cmr:form>

  <%if (!newEntry){ %>
  <jsp:include page="/resources/jsp/requestentry/notify.jsp" />
  <jsp:include page="/resources/jsp/requestentry/viewattachment.jsp" />
  <jsp:include page="/resources/jsp/requestentry/addattachment.jsp" />
  <%if (!CmrConstants.REQ_TYPE_UPDT_BY_ENT.equals(reqentry.getReqType())){ %>
      <jsp:include page="processing.jsp" />
  <%}%>
  <jsp:include page="/resources/jsp/requestentry/approvals.jsp" />  
  <jsp:include page="/resources/jsp/requestentry/approvalsAction.jsp" />  
  <jsp:include page="/resources/jsp/requestentry/approvalsComments.jsp" />    
  <%}%>
</cmr:boxContent>

<%if (!newEntry) {%>
  <jsp:include page="../requestentry/commentLog.jsp" />
<%} %>


<cmr:section alwaysShown="true">
<%if (user != null && !user.isApprover()){%>
  <cmr:buttonsRow>
    <%if (!newEntry) {%>
      <c:if test="${fn:trim(reqentry.fromUrl) != ''}">
        <div style="float:left">
        <cmr:button label="${ui.btn.backtowf}" onClick="goBackToWorkflow()" />
        <cmr:button label="${ui.btn.createNewEntry}" onClick="cmr.chooseNewEntry()" highlight="false" pad="true"/>
        <%if (!readOnly){ %>
          <cmr:button label="${ui.btn.undoCurrentChanges}" onClick="undoCurrentChanges()" highlight="false" pad="true"/>
        <%}%>
        </div>
        <div style="float:right">
        <cmr:button label="${ui.btn.openLog}" onClick="showChangeLog(${reqentry.reqId})" pad="true"/>
        <cmr:button label="${ui.btn.openHistory}" onClick="openWorkflowHistory(${reqentry.reqId})" highlight="true" pad="true"/>
        </div>
      </c:if>
      <c:if test="${fn:trim(reqentry.fromUrl) == ''}">
        <div style="float:left">
        <cmr:button label="${ui.btn.createNewEntry}" onClick="cmr.chooseNewEntry()" highlight="false" pad="false"/>
        <%if (!readOnly){ %>
          <cmr:button label="${ui.btn.undoCurrentChanges}" onClick="undoCurrentChanges()" highlight="false" pad="true"/>
        <%}%>
        </div>
        <div style="float:right">
        <cmr:button label="${ui.btn.openLog}" onClick="showChangeLog(${reqentry.reqId})" pad="true"/>
        <cmr:button label="${ui.btn.openHistory}" onClick="openWorkflowHistory(${reqentry.reqId})" highlight="true" pad="true"/>
        </div>
      </c:if>
      
    <%} else {%>
      <c:if test="${fn:trim(reqentry.fromUrl) != ''}">
        <cmr:button label="${ui.btn.backtowf}" onClick="goBackToWorkflow()" />
      </c:if>
    <%} %>
  </cmr:buttonsRow>
<%} else if (user != null && user.isApprover()){ %>
  <cmr:buttonsRow>
    <cmr:button label="Back to Approvals" onClick="window.location = '${contextPath}/myappr'" />
  </cmr:buttonsRow>
<%} %>
</cmr:section>
<cmr:model model="reqentry" />
<jsp:include page="/resources/jsp/requestentry/attach_dl.jsp" />
<jsp:include page="file_dl.jsp" />
<jsp:include page="/resources/jsp/requestentry/supportal.jsp" />
<jsp:include page="/resources/jsp/requestentry/feedback.jsp" />

<!--  Customizability Scripts -->
<%=PageManager.getScripts(request, readOnly, newEntry)%>
<%=PageManager.generateTabScripts(request, "", "", "", true)%>
<!--  End Customizability Scripts -->