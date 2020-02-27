<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
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
String contextPath = request.getContextPath();
String createTs = UIMgr.getText("grid.approvalsCreateDateTime");
String infoTxt = UIMgr.getText("info.timeStampInfo");
String tz = SystemConfiguration.getValue("DATE_TIMEZONE");
String title=createTs+" ("+tz+")";
String createTsHeader =title+"<img src=\""+contextPath+"/resources/images/info-bubble-icon.png\" title=\""+infoTxt+"\" class=\"cmr-info-bubble\">";

%>
<!--  Modal for the Approval Comments Screen -->
<cmr:modal title="${ui.title.approvalComments}" id="viewApprCommentsModal" widthId="750">
  <form:form method="POST" action="${contextPath}/approval/comments" name="frmApprComments" class="ibm-column-form ibm-styled-form" modelAttribute="approval" id="frmApprComments">
      <cmr:column span="3" width="680">
        <cmr:grid url="/approval/comments.json" id="APPROVAL_CMT_GRID" span="3" width="680" innerWidth="680" height="200" usePaging="false">
          <cmr:gridParam fieldId="reqId" value="${reqentry.reqId}"></cmr:gridParam>
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
  </form:form>
</cmr:modal>
