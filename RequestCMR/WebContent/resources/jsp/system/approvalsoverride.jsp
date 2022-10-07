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
%>
<style>
#addApprovalModal div.ibm-columns {
  width: 710px !important;
}

</style>
<script>
  dojo.addOnLoad(function() {
    FormManager.addValidator('status', Validators.REQUIRED, [ 'New Status' ]);
    FormManager.addValidator('approval_cmt', Validators.REQUIRED, [ 'Comments' ]);
    FormManager.addValidator('displayName', Validators.REQUIRED, [ 'New Approver' ]);
    FormManager.addValidator('displayName', Validators.BLUEPAGES, [ 'New Approver' ]);
  });
</script>


<!--  Modal for the Add Approval Screen -->
<cmr:modal title="Override Approval" id="adminApprovalModal" widthId="750">
  <cmr:form method="POST" action="${contextPath}/approval/process" name="frmCMRApprovalAdmin" class="ibm-column-form ibm-styled-form" modelAttribute="approval" id="frmCMR_adminApprovalModal">
    <cmr:modelAction formName="frmCMR_adminApprovalModal" />
    <form:hidden path="reqId" id="reqId" />
    <form:hidden path="approvalId" id="approvalId" />
    <cmr:row>
      <cmr:column span="3">
        <jsp:include page="../templates/messages_modal.jsp">
          <jsp:param value="adminApprovalModal" name="modalId" />
        </jsp:include>
      </cmr:column>
    </cmr:row>
    
    <form:hidden path="typId"/>
    <div id="approverCont">
      <cmr:row>
        <cmr:column span="1">
          <p>
            <label for="displayName" id="displayName-lbl">
              New Approver:
            </label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
              <cmr:bluepages model="approval" namePath="displayName" idPath="intranetId" showId="true" />
          </p>
        </cmr:column>      
      </cmr:row>
    </div>    
      <cmr:row>
        <cmr:column span="1">
          <p>
            <label for="status" id="status-lbl">
              New Status:
            </label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
              <form:select dojoType="dijit.form.FilteringSelect" size="35"  
              id="status" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="status" >
                <form:option value=""></form:option>
                <form:option value="APR">Approved</form:option>
                <form:option value="CAPR">Conditionally Approved</form:option>
                <form:option value="PAPR">Pending Approval</form:option>
                <form:option value="PMAIL">Pending Mail</form:option>
                <form:option value="CAN">Cancelled</form:option>
                <form:option value="CCAN">Conditionally Cancelled</form:option>              
              </form:select>
          </p>
        </cmr:column>      
      </cmr:row>
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
    </cmr:row>
    <cmr:buttonsRow>
      <cmr:hr />
      <cmr:button label="Submit" onClick="processOverride()" highlight="true" id="approvalBtnSubmit"/>
      <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('adminApprovalModal')" highlight="false" pad="true" />
    </cmr:buttonsRow>
  </cmr:form>
</cmr:modal>
