<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<!--  Toggles the state of the page depending on the approval status -->
<%
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
String approvalStatus = reqentry.getApprovalResult();
if (!"Viewer".equals(reqentry.getUserRole())){
  if (CmrConstants.APPROVAL_RESULT_PENDING.equals(approvalStatus)){
    // set page to readonly
    request.setAttribute("yourActionsViewOnly", true);
    // remove all actions
    request.setAttribute("yourActionsSqlId", "YOUR_ACTIONS_NONE");
  }
  if ("Requester".equals(reqentry.getUserRole()) && (CmrConstants.APPROVAL_RESULT_COND_APPROVED.equals(approvalStatus)|| CmrConstants.APPROVAL_RESULT_COND_CANCELLED.equals(approvalStatus))){
    // set page to readonly
    request.setAttribute("yourActionsViewOnly", true);
    // remove all actions
    request.setAttribute("yourActionsSqlId", "YOUR_ACTIONS_SFP");
  }
}
%>