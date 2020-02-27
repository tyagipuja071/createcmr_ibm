<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<a name="_MESSAGES"></a>
<div class="ibm-columns" id="cmr-validation-box" style="display:none">
	<div class="ibm-col-1-1 cmr-message-box-error">
		<img class="cmr-error-icon" src="${resourcesPath}/images/error-icon.png"></img>
		<span>Please correct the following error(s):</span>
		<ul id="cmr-validation-box-list"/>
	</div>
</div>
<div class="ibm-columns" id="cmr-error-box" style="display:none">
	<div class="ibm-col-1-1">
      <p class="ibm-error">
		<img class="cmr-error-icon" src="${resourcesPath}/images/error-icon.jpg"></img>
		<strong><span id="cmr-error-box-msg">${errorMessage}</span></strong>
	  </p>
	</div>
</div>
<div class="ibm-columns" id="cmr-info-box" style="display:none">
	<div class="ibm-col-1-1">
      <p style="color: green !important; ">
		<img class="cmr-info-icon" src="${resourcesPath}/images/info-icon.jpg"></img>
		<strong><span id="cmr-info-box-msg">${infoMessage}</span></strong>
	  </p>
	</div>
</div>
<script>
dojo.addOnLoad(function(){
	<c:if test="${ errorMessage  != null }">
	  MessageMgr.showErrorMessage();
	</c:if>
	<c:if test="${ infoMessage  != null && errorMessage == null}">
  	  MessageMgr.showInfoMessage();
	</c:if>
});
</script>