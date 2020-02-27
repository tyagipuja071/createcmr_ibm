<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
  AppUser user = AppUser.getUser(request);
	String auth = user.getAuthCode();
	String url = SystemConfiguration.getValue("FIND_CMR_URL");
	String system = request.getParameter("system");
	String cntry = request.getParameter("cntry");
  String land1 = request.getParameter("land1");
  String details = (String) request.getAttribute("request");
  String reqId = request.getParameter("reqId");
%>
<html>
<head>
<title>Connecting to Find CMR</title>
<style>
body {
}

span.ibm-spinner {
	display: block;
	height: 38px !important;
	background: transparent
		url("//1.www.s81c.com/common/v17e/i/animated-progress-38x38c.gif")
		no-repeat scroll center center;
}

h3 {
	width: 100%;
	text-align: center;
	font-family: "Helvetica";
}

div.loader {
	border: 8px solid rgba(0, 0, 0, 0.2) !important;
	border-radius: 10px;
	width: 400px;
	height: 120px;
	margin: auto;
  background:#FFF;
  opacity:2.0;
  margin-top: 100px;
}

div.error {
  border: 8px solid rgba(255, 0, 0, 0.7) !important;
  border-radius: 10px;
  width: 700px;
  height: 110px;
  margin: auto;
  background:#FFF;
  opacity:2.0;
  margin-top: 100px;
  color:red;
}

div.bg {
	width: 100%;
	height: 100%;
  position: absolute;
  left: 0;
  top: 0;
	background: #999 none repeat scroll 0px 0px;
}
</style>
</head>
<body>
  <div class="bg">
    <form id="connectForm" action="<%=url%>/connect" method="GET">
      <input type="hidden" name="authCode" value="<%=auth%>"> 
      <input type="hidden" name="system" value="<%=system%>"> 
      <input type="hidden" name="cntry" value="<%=cntry%>">
      <input type="hidden" name="land1" value="<%=land1%>">
      <input type="hidden" name="request" value="<%=details%>">
      <input type="hidden" name="reqId" value="<%=reqId%>">
<%if (auth != null){ %>
      <div class="loader">
        <h3>Please wait while Find CMR is loading...</h3>
        <span class="ibm-spinner"></span>
      </div>
<%} else { %>
      <div class="error">
        <h3>Connection to the Find CMR application was not established. <br> 
        Please try logging out of the application then access the tool again.
        <br>If the error persists, please contact your system administrator.</h3>
      </div>
<%} %>
    </form>
  </div>
</body>
<%if (auth != null){ %>
<script>
    document.getElementById('connectForm').submit();
</script>
<%} %>
</html>