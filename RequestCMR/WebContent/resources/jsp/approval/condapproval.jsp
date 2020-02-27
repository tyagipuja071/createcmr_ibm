<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  ApprovalResponseModel approval = (ApprovalResponseModel) request.getAttribute("approval");
  String action = request.getContextPath()+"/approval/"+approval.getApprovalCode();
%>
<script src="${resourcesPath}/js/requestentry/attachment.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/myapprovals.js?${cmrv}" type="text/javascript"></script>
<script>
  cmr.NOSESSIONCHECK = true;
  cmr.reqId = '${approval.reqId}';
  dojo.addOnLoad(function(){
    if (FormManager) {
      FormManager.addValidator('comments', Validators.REQUIRED, [ 'Comments' ]);
      FormManager.ready();
    }
  });
  
  function submitApproval(){
    if (FormManager.validate('frmApproval')){
      cmr.showProgress('Processing...');
      document.forms['frmApproval'].submit();
    }
  }
</script>
<style>
#addAttachmentModal div.ibm-columns {
  width: 500px !important;
}
.reqdetailslink {
  font-size: 13px;
  margin-bottom: 5px;
}
</style>
<form:form method="POST" action="<%=action%>" name="frmApproval" class="ibm-column-form ibm-styled-form" modelAttribute="approval">
  <form:hidden path="approverId"/>
  <form:hidden path="type"/>
  <form:hidden path="approvalId"/>
  <form:hidden path="reqId"/>
  <form:hidden path="requester"/>
  <form:hidden path="requesterId"/>
  <form:hidden path="approverNm"/>
  <form:hidden path="currentStatus"/>
  <form:hidden path="approvalCode"/>
  <form:hidden path="actionDone"/>
  <input name="processing" type="hidden" value="Y">
<%if ("Y".equals(approval.getActionDone())){%>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:section>
    <cmr:row>
      <cmr:column span="6">
        <h3>Confirmation for Approver <%=approval.getApproverNm()%></h3>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="20">
      <cmr:column span="6">
        Your conditional approval has been processed. A notification has been sent to the requester <b><%=approval.getRequester()%></b>.
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="6">
        No further action is required.
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="6">
        <strong>You can click <a href="<%=(SystemConfiguration.getValue("APPLICATION_URL")+"/myappr")%>">here</a> to log into the application and view all your other approvals.</strong>
      </cmr:column>
    </cmr:row>
   </cmr:section>
</cmr:boxContent>
  <%} else  {%>
<cmr:boxContent>
  <cmr:tabs />
  <cmr:section>
    <cmr:row>
      <cmr:column span="6">
        <h3>Please provide your comments for the conditional approval</h3>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="20">
      <cmr:column span="3">
        <p>
          <cmr:label fieldId="comments">Comments:</cmr:label>
        </p>
      </cmr:column>
      <cmr:column span="2">
        <p>
          <cmr:label fieldId="reqdetails">Request Details:</cmr:label>
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="3">
        <form:textarea path="comments" id="comments" rows="5" cols="40"></form:textarea>
      </cmr:column>
      <cmr:column span="2" >
        <p>
           <a class="reqdetailslink" href="javascript: exportToPdf(${approval.reqId})">Download Request Details (PDF)</a>
           <br>
           <a class="reqdetailslink" href="javascript: openWorkflowHistory(${approval.reqId})">View Workflow History</a>
           <br>
           <a class="reqdetailslink" href="javascript: showChangeLog(${approval.reqId})">View Change Log</a>
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="5">
      &nbsp;
    </cmr:row>
  <cmr:row>
    <cmr:column span="6" width="700">
      <h3>Request Attachments</h3>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="6" width="650">
      <cmr:grid url="/search/attachment.json" id="ATTACHMENT_GRID" span="6" width="630" innerWidth="630" height="250" usePaging="false">
        <cmr:gridParam fieldId="reqId" value="${approval.reqId}" />
        <cmr:gridCol width="140px" field="docLink" header="${ui.grid.docLink}">
          <cmr:formatter functionName="fileNameFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="160px" field="docContent" header="${ui.grid.docContent}" />
        <cmr:gridCol width="auto" field="cmt" header="Comments" />
        <cmr:gridCol width="100px" field="action" header="${ui.grid.actions}">
          <cmr:formatter functionName="attchActFormatter" />
        </cmr:gridCol>
      </cmr:grid>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    &nbsp;
  </cmr:row>
  </cmr:section>
</cmr:boxContent>
<cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <cmr:button label="Submit" onClick="submitApproval()" highlight="true" />
      <cmr:button label="Add Attachment" onClick="doAddAttachment()" highlight="false" pad="true" />
      <cmr:button label="Add Screenshot" onClick="doAddScreenshot()" highlight="false" pad="true" />
    </cmr:buttonsRow>
</cmr:section>
<%} %>
</form:form>
<jsp:include page="../requestentry/attach_dl.jsp" />
<jsp:include page="../requestentry/viewattachment.jsp" />
<jsp:include page="../requestentry/addattachment.jsp" />
