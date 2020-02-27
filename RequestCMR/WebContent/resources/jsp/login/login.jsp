<%@page import="com.ibm.cio.cmr.request.util.SystemParameters"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script src="${resourcesPath}/js/login.js?${cmrv}" type="text/javascript"></script>

<!-- start main content -->
<div class="ibm-columns">
  <div class="ibm-col-1-1">
    <div id="wwq-content">
      <div class="ibm-columns">
        <form:form method="POST" action="performLogin" id="login-form" name="frmCMR" class="ibm-column-form ibm-styled-form"
          modelAttribute="loginUser">
          <div class="ibm-col-2-1">
            <form:hidden path="r" />
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
          </div>
          <div class="ibm-col-2-1">
            <div id="forgotpwd">
              <p>
                Site secured by <br> 
                <a class="ibm-secure-link" href="https://w3.ibm.com/profile/update/password/en-us/index.html" alt="">
                  IBM Intranet Password
                </a>
              </p>
              <br>
            </div>
          </div>
        </form:form>
      </div>

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

    <div id="push"></div>
    <!-- stop main content -->
  </div>
</div>

