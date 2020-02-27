
<%@page import="org.apache.commons.lang.StringUtils"%>
<%
  String score = request.getParameter("score");
  String market = request.getParameter("market");
  String tribe = request.getParameter("tribe");
  String squad = request.getParameter("squad");
  String area = request.getParameter("area");
  String note = request.getParameter("note");
  String email = request.getParameter("user_email");
%>

<html>
<head>
<title>Client Satisfaction Survey</title>
<meta content="text/html; charset=utf-8" http-equiv="Content-Type">
<style>
div.progress {
  width : 300px;
  border: 2px Solid #444444;
  margin : auto;
  text-align: center;
  font-weight: bold;
  font-family: IBM Plex Sans;
  border-radius: 10px;
  padding: 10px;
  box-shadow: 2px 2px #666666;
}

.ibm-spinner-large {
  padding: 0;
  padding-top: 10px;
}
</style>
</head>
<%if (!StringUtils.isBlank(score)){%>
<body>
  <div class="progress">
   Sending your feedback...
   <a class="ibm-spinner-large" href="#" onclick="return false" title="Dialog overlay content">&nbsp;</a>
  </div>
  <form id="npsform" action="https://cdoclientsurvey.w3ibm.mybluemix.net/create_record" method="POST">
    <input type="hidden" name="market" value="<%=market%>" />
    <input type="hidden" name="tribe" value="<%=tribe%>" /> 
    <input type="hidden" name="squad" value="<%=squad%>" /> 
    <input type="hidden" name="area" value="<%=area%>" /> 
    <input type="hidden" name="score" value="<%=score%>" /> 
    <input type="hidden" name="note" value="<%=note%>" /> 
    <input type="hidden" name="user_email" value="<%=email%>" /> 
    <input type="hidden" name="_method" value="put" /> 
  </form>
</body>
<script>
  document.forms[0].submit();
</script>
<%} else { %>
<body>
  <div class="progress">
   <span style="color:red">Please select a score from the survey to submit your feedback.</span>
  </div>
</body>
<%} %>
</html>