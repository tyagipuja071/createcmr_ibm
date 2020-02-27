<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  AppUser user = AppUser.getUser(request);
  String contextPath = request.getContextPath();
	String createTs = UIMgr.getText("grid.approvalsCreateDateTime");
	String infoTxt = UIMgr.getText("info.timeStampInfo");
	String tz = SystemConfiguration.getValue("DATE_TIMEZONE");
	String title = createTs + " (" + tz + ")";
	String createTsHeader = title + "<img src=\"" + contextPath + "/resources/images/info-bubble-icon.png\" title=\""
					+ infoTxt + "\" class=\"cmr-info-bubble\">";
  String pending = !"Y".equals(request.getParameter("all")) ? "Y" : "N";
	if (user != null && (user.isApprover() || user.isHasApprovals())) {
%>
<style>
#processApprovalModal div.ibm-columns {
  width: 700px !important;
}

.appr-link {
  font-size: 10px;
  padding-right: 3px;
}
</style>
<script src="${resourcesPath}/js/requestentry/attachment.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/myapprovals.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FilteringDropdown.loadItems('rejReason', 'rejReason_spinner', 'lov', 'fieldId=ApprovalRejReason&cmrIssuingCntry=*');
    FilteringDropdown.loadItems('rejReasonMass', 'rejReason_spinner', 'lov', 'fieldId=ApprovalRejReason&cmrIssuingCntry=*');
    FormManager.addValidator('comments', Validators.REQUIRED, [ 'Comments' ]);
    FormManager.addValidator('rejReason', Validators.REQUIRED, [ 'Reason' ]);
    FormManager.addValidator('rejReasonMass', Validators.REQUIRED, [ 'Reason' ]);
    FormManager.addValidator('mass_comments', Validators.REQUIRED, [ 'Comments' ]);
    FormManager.ready();
  });

</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/myappr/process" name="frmCMR" id="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="approval">
    <cmr:modelAction formName="frmCMR" />
    <form:hidden path="approvalId" id="approvalId" />
    <form:hidden path="reqId" id="reqId" />
    <form:hidden path="mass" id="mass" />
    <form:hidden path="rejReason" id="approval_rejReason" />
    <form:textarea path="comments" id="approval_comments" cssStyle="display:none"></form:textarea>
    <cmr:section id="GRIDSECTION">
      <cmr:row>
        <cmr:column span="6">
          <h3>List of <%="Y".equals(pending) ? "My Pending Approvals" : "All My Approvals"%></h3>
        </cmr:column>
      </cmr:row>
      <cmr:grid url="/myappr/list.json" id="myApprovalsGrid" useFilter="true" hasCheckbox="true" checkBoxKeys="reqId,approvalId">
        <cmr:gridParam fieldId="approverId" value="<%=user.getIntranetId()%>" />
        <cmr:gridParam fieldId="pendingOnly" value="<%=pending%>" />
        <cmr:gridCol width="60px" field="reqId" header="Request ID" align="right">
          <cmr:formatter functionName="requestIdFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="65px" field="reqType" header="Request Type">
          <cmr:formatter functionName="requestTypeFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="55px" field="cntryCd" header="Issuing Country">
          <cmr:formatter functionName="countryFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="145px" field="custNm" header="Customer Name">
          <cmr:formatter functionName="nameFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="auto" field="reqReason" header="Request Reason">
        </cmr:gridCol>
        <cmr:gridCol width="80px" field="dplChkResult" header="DPL Check">
          <cmr:formatter functionName="dplCheckFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="100px" field="approvalStatus" header="Approval Status">
          <cmr:formatter functionName="approvalStatusFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="110px" field="approvalType" header="Approval Type">
        </cmr:gridCol>
        <cmr:gridCol width="120px" field="approverNm" header="Actions">
          <cmr:formatter functionName="actionsFormatter" />
        </cmr:gridCol>
      </cmr:grid>
      <br>
    </cmr:section>
    <cmr:model model="approval" />
  </form:form>

</cmr:boxContent>

<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Approve" onClick="doMassApproval('A')" highlight="true" />
    <cmr:button label="Conditionally Approve" onClick="doMassApproval('C')" highlight="false" pad="true" />
    <cmr:button label="Reject" onClick="doMassApproval('R')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:section>

<cmr:modal title="${ui.title.approvalComments}" id="viewApprCommentsModal" widthId="750">
  <cmr:column span="3" width="680">
    <cmr:grid url="/approval/comments.json" id="APPROVAL_CMT_GRID" span="3" width="660" innerWidth="660" height="200" usePaging="false">
      <cmr:gridParam fieldId="reqId" value="0"></cmr:gridParam>
      <cmr:gridParam fieldId="approvalId" value="0"></cmr:gridParam>
      <cmr:gridCol width="180px" field="commentBy" header="${ui.grid.commentBy}" />
      <cmr:gridCol width="125px" field="createTs" header="<%=createTsHeader%>" />
      <cmr:gridCol width="140px" field="status" header="${ui.grid.status}" />
      <cmr:gridCol width="auto" field="comments" header="${ui.grid.comments}" />
    </cmr:grid>
  </cmr:column>
  <br>
  <cmr:buttonsRow>
    <br>
    <cmr:button label="${ui.btn.close}" onClick="cmr.hideModal('viewApprCommentsModal')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>

<cmr:modal title="Process Approval" id="processApprovalModal" widthId="750">
    <div id="rejectMainLine">
    <cmr:row>
      <cmr:column span="6" width="700">
        <h3>Please provide your comments and reason for rejection (for reject actions)</h3>
      </cmr:column>
    </cmr:row>
    </div>
    <div id="approveMainLine">
    <cmr:row>
      <cmr:column span="6" width="700">
        <h3>Please provide your comments and reason for approval (for approve actions)</h3>
      </cmr:column>
    </cmr:row>
    </div>
    <div id="rejectblock">
    <cmr:row topPad="20">
      <cmr:column span="3">
        <p>
          <cmr:label fieldId="rejReason">Reason:</cmr:label>
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="3">
        <select dojoType="dijit.form.FilteringSelect" name="rejReason" id="rejReason">
        </select>
      </cmr:column>
    </cmr:row>
    </div>
  <cmr:row topPad="10">
    <cmr:column span="2">
      <p>
        <cmr:label fieldId="comments">Comments:</cmr:label>
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="10">
    <cmr:column span="6" width="700">
      <textarea name="comments" rows="5" cols="50" id="comments"></textarea>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="6" width="700">
      <h3>Request Attachments</h3>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="6" width="650">
      <cmr:grid url="/search/attachment.json" id="ATTACHMENT_GRID" span="6" width="630" innerWidth="630" height="250" usePaging="false">
        <cmr:gridParam fieldId="reqId" value="0" />
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

  <cmr:buttonsRow>
    <br>
    <cmr:button label="Submit" onClick="executeApproval()" highlight="true" />
    <cmr:button label="Add Attachment" onClick="doAddAttachment()" highlight="false" pad="true" />
    <cmr:button label="Add Screenshot" onClick="doAddScreenshot()" highlight="false" pad="true" />
    <cmr:button label="${ui.btn.close}" onClick="cmr.hideModal('processApprovalModal')" highlight="false" pad="true" />
  </cmr:buttonsRow>

</cmr:modal>

<cmr:modal title="Process Approval" id="processMassApprovalModal" widthId="750">
    <div id="rejectMainLine_mass">
    <cmr:row>
      <cmr:column span="6" width="700">
        <h3>Please provide your comments and reason for rejection (for reject actions)</h3>
      </cmr:column>
    </cmr:row>
    </div>
    <div id="approveMainLine_mass">
    <cmr:row>
      <cmr:column span="6" width="700">
        <h3>Please provide your comments and reason for approval (for approve actions)</h3>
      </cmr:column>
    </cmr:row>
    </div>
    <div id="rejectblock_mass">
    <cmr:row topPad="20">
      <cmr:column span="3">
        <p>
          <cmr:label fieldId="rejReasonMass">Reason:</cmr:label>
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="3">
        <select dojoType="dijit.form.FilteringSelect" name="rejReasonMass" id="rejReasonMass">
        </select>
      </cmr:column>
    </cmr:row>
    </div>
  <cmr:row topPad="10">
    <cmr:column span="2">
      <p>
        <cmr:label fieldId="comments">Comments: <span style="color:red">*</span></cmr:label>
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="10">
    <cmr:column span="6" width="700">
      <textarea name="comments" rows="5" cols="50" id="mass_comments"></textarea>
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <br>
    <cmr:button label="Submit" onClick="executeMassApproval()" highlight="true" />
    <cmr:button label="${ui.btn.close}" onClick="cmr.hideModal('processMassApprovalModal')" highlight="false" pad="true" />
  </cmr:buttonsRow>

</cmr:modal>
<jsp:include page="../requestentry/attach_dl.jsp" />
<jsp:include page="../requestentry/viewattachment.jsp" />
<jsp:include page="../requestentry/addattachment.jsp" />
<%
  }
%>