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
  ApprovalResponseModel approval = (ApprovalResponseModel) request
					.getAttribute("approval");
%>
<script>
  cmr.NOSESSIONCHECK = true;
</script>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:section>
    <cmr:row>
      <cmr:column span="6">
        <h3>Approval's Status has Changed</h3>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="20">
      <cmr:column span="6">
        The approval request's status has been changed to <b><%=approval.getCurrentStatus()%></b>. The approval <span style="color:red">cannot</span> be processed right now.
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
