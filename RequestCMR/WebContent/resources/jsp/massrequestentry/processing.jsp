<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="java.util.List"%>
<%@page import="com.ibm.cio.cmr.request.ui.FieldInformation"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  AppUser user = AppUser.getUser(request);
  boolean noFindCMR = user.getAuthCode() == null;
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
  String mcFileVersion = SystemConfiguration.getValue("MASS_CREATE_TEMPLATE_VER");
  String procCenter = reqentry.getProcCenter() != null ? reqentry.getProcCenter() : "";
%>
<style>
.cmr-middle {
  text-align:center;
}
</style>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<cmr:section id="PROC_REQ_TAB" hidden="true">

<%if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(reqentry.getReqType()) || CmrConstants.REQ_TYPE_MASS_UPDATE.equals(reqentry.getReqType())) {%>

<%-- Mass Update/Create --%>
<jsp:include page="processing_MN.jsp" />

<%} else if (reqentry.getReqType() != null && CmrConstants.REQ_TYPE_UPDT_BY_ENT.equals(reqentry.getReqType())) {%>

<%-- Update by Enterprise No. --%>
<jsp:include page="processing_E.jsp" />

<%} else {%>

<%-- Delete/Reactivate --%>
<jsp:include page="processing_RD.jsp" />

<%} %>

<iframe id="processFrame" style="display:none" name="processFrame"></iframe>

</cmr:section>