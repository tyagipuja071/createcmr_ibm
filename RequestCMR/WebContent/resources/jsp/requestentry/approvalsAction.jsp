<%@page import="com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
ApprovalResponseModel approval = (ApprovalResponseModel) request.getAttribute("approval");
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
%>
<style>
#addApprovalModal div.ibm-columns {
  width: 710px !important;
}

</style>
<script>
  dojo.addOnLoad(function() {
    FilteringDropdown.loadItems('typId', 'approvalType_spinner', 'approval_typ', 'cmrIssuingCntry=' + '${reqentry.cmrIssuingCntry}');
    FormManager.addValidator('typId', Validators.REQUIRED, [ '${ui.approvalType}' ]);
    FormManager.addValidator('displayName', Validators.REQUIRED, [ '${ui.approvalApprover}ss' ]);
    FormManager.addValidator('approval_cmt', Validators.REQUIRED, [ '${ui.approvalCmt}' ]);
    
  });
</script>


<!--  Modal for the Add Approval Screen -->
<cmr:modal title="${ui.title.addApproval}" id="addApprovalModal" widthId="750">
  <form:form method="POST" action="${contextPath}/approval/process" name="frmCMRApprovalAdd" class="ibm-column-form ibm-styled-form" modelAttribute="approval" id="frmCMR_addApprovalModal">
    <cmr:modelAction formName="frmCMR_addApprovalModal" />
    <form:hidden path="reqId" id="reqId" value="${reqentry.reqId}" />
    <form:hidden path="approvalId" id="approvalId" />
    <form:hidden path="status" id="approvalstatus" />
    <form:hidden path="requiredIndc" id="requiredIndc" />
    <cmr:row>
      <cmr:column span="3">
        <jsp:include page="../templates/messages_modal.jsp">
          <jsp:param value="addApprovalModal" name="modalId" />
        </jsp:include>
      </cmr:column>
    </cmr:row>

    <div id="approvalTypeCont">
      <cmr:row>
        <cmr:column span="1">
          <p>
            <cmr:label fieldId="typId">${ui.approvalType}: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="typId" itemLabel="name" itemValue="id" style="display: block;" maxHeight="200" required="true" path="typId" placeHolder="Select Approval Type" cssStyle="width:475px"></form:select>
          </p>
        </cmr:column>
      </cmr:row>
    </div>

    <div id="approverCont">
      <cmr:row>
        <cmr:column span="1">
          <p>
            <label for="displayName" id="displayName-lbl">
              ${ui.approvalApprover}:
            </label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <cmr:field model="approval" path="displayName" idPath="intranetId" fieldId="ApproverName" />        
          </p>
        </cmr:column>      
      </cmr:row>
    </div>    
    <cmr:row>
      <cmr:column span="1">
        <p>
          <cmr:label fieldId="approval_cmt">${ui.approvalCmt}:
            <cmr:memoLimit maxLength="250" fieldId="approval_cmt" />
          </cmr:label>
        </p>
      </cmr:column>
      <cmr:column span="2" width="400">
        <p>
          <form:textarea path="comments" id="approval_cmt" rows="5" cols="48"/>
        </p>
      </cmr:column>      
      <input type="hidden" name="approvalToken" id="approvalToken">
    </cmr:row>
    <cmr:buttonsRow>
      <cmr:hr />
      <cmr:button label="Submit" onClick="processApprovalAction()" highlight="true" id="approvalBtnSubmit"/>
      <cmr:button label="${ui.btn.addApproval}" onClick="addApproval()" highlight="true" id="approvalBtnAdd"/>
      <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('addApprovalModal')" highlight="false" pad="true" />
    </cmr:buttonsRow>
  </form:form>
</cmr:modal>
