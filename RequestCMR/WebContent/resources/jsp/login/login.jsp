<%@page import="com.ibm.cio.cmr.request.util.SystemParameters"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<style>
  img.key {
    width : 20px;
    height: 20px;
    vertical-align: sub;
  }
  div.cmr-message-box-error, #cmr-info-box p, #cmr-error-box p {
    margin-left: 100px;
  } 
  
div#ibm-top {
  min-height: 450px;
}  
</style>
<script src="${resourcesPath}/js/login.js?${cmrv}" type="text/javascript"></script>

<!-- start main content -->
<div class="ibm-columns">
  <div class="ibm-col-1-1">
    <div id="wwq-content">
      <div class="ibm-columns">
        <cmr:form method="POST" action="performLogin" id="login-form" name="frmCMR" class="ibm-column-form ibm-styled-form"
          modelAttribute="loginUser">
          <input type="hidden" name="csrf" value="">
          <div class="ibm-col-2-1" style="width:100px">
            &nbsp;
          </div>
          <div class="ibm-col-2-1">
            <form:hidden path="r" />
            <input type="hidden" name="c" value="<%=request.getParameter("c")%>">
            <!-- Username Field -->
            <p>
              <form:label path="username" for="username">User Name:</form:label>
              <span style="margin-left: 120px">
                <form:input path="username" size="22" title="Type your IBM intranet user name; for example, yourname@us.ibm.com" /> 
              </span> 
              <span class="ibm-item-note" style="margin-left: 150px">(e.g. xyz@us.ibm.com)</span>
            </p>
            <!-- Password Field -->
            <p>
              <form:label path="password" for="password">Password:</form:label>
              <span style="margin-left: 120px"> 
                <form:password path="password" size="22" title="Type your IBM intranet password." /> 
              </span>
            </p>
            <!-- Submit button -->
          <p>
            <input id="cmr-login-btn" type="button" style="display: none" value="Login" class="ibm-btn-arrow-pri ibm-btn-small"
              title="Click to login to Create CMR" onclick="login.validateAndSubmit();" />
          </p>
          </div>
          <div class="ibm-col-2-1">
            <div id="forgotpwd">
              <div class="login-info" style="padding-bottom:10px">
                <img src="${resourcesPath}/images/lock.png" class="key">
                  Site secured by <a href="https://w3.ibm.com/profile/update/password/en-us/index.html" target="_blank">IBM Intranet Password</a>
                </a>
              </div>
              <div class="login-info" style="padding-bottom:10px">
                <img src="${resourcesPath}/images/key.png" class="key">
                  Access to the tool can be requested using AccessHUB. 
                  For access instructions and the user guide please follow this 
                  <a href="<%=SystemParameters.getString("ACCESS_LINK")%>" target="_blank">link</a> or go to 
                  <a href="https://ibm.idaccesshub.com/ECM/login/index" target="_blank">AccessHUB</a> directly              
              </div> 
              <div class="login-info" style="padding-bottom:10px">
                <img src="${resourcesPath}/images/question.png" class="key">
                  For support, please contact the help desk by sending an email to <a href="mailto:CCM Worldwide Support/Raleigh/Contr/IBM">CCM Worldwide Support/Raleigh/Contr/IBM</a>
                </a>
            </div>
          </div>
          </div>
        </cmr:form>
      </div>

<%--
      <div class="ibm-columns">
        <div class="ibm-col-6-1" style="width:500px">
          <p>
            <input id="cmr-login-btn" type="button" style="display: none" value="Login" class="ibm-btn-arrow-pri ibm-btn-small"
              title="Click to login to Create CMR" onclick="login.validateAndSubmit();" />
            <%
              if (SystemParameters.getString("CI_SUPPORTAL_URL") != null) {
            %>
            <span id="supportal-link" class="support-link" style="display: none">Having login problems? Click <a
              href="javascript: login.openSupportal()">here</a> to report the issue.</span>
            <%
              }
            %>
          </p>
        </div>
      </div>
    </div>
--%>
    <div id="push"></div>
    <!-- stop main content -->
  </div>
</div>

