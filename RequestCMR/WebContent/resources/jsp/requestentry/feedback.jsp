<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="com.ibm.cio.cmr.request.util.Feedback"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
String cmrIssuingCntry = reqentry.getCmrIssuingCntry();
String feedbackUrl = Feedback.getLink(cmrIssuingCntry);
%>
<%if (reqentry.getReqId() > 0 && !StringUtils.isBlank(feedbackUrl)){ %>
<div id="feedback_button" onclick="openNPSFeedback('<%=feedbackUrl%>')" class="cmr-feedback-btn">Feedback</div>
<%}%>