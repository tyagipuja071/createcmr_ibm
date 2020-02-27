<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<a name="_MODALMESSAGES"></a>
<div class="ibm-columns" id="${param.modalId}-cmr-validation-box-modal" style="display:none">
	<div class="ibm-col-1-1 cmr-message-box-error" style="width:auto !important">
		<img class="cmr-error-icon" src="${resourcesPath}/images/error-icon.png"></img>
		<span>Please correct the following error(s):</span>
		<ul id="${param.modalId}-cmr-validation-box-list-modal"/>
	</div>
</div>
<div class="ibm-columns" id="${param.modalId}-cmr-error-box-modal" style="display:none">
	<div class="ibm-col-1-1" style="width:auto !important">
      <p class="ibm-error">
		<img class="cmr-error-icon" src="${resourcesPath}/images/error-icon.jpg"></img>
		<strong><span id="${param.modalId}-cmr-error-box-msg-modal" style="margin-left:20px !important;display:inline">${errorMessage}</span></strong>
	  </p>
	</div>
</div>
<div class="ibm-columns" id="${param.modalId}-cmr-info-box-modal" style="display:none">
	<div class="ibm-col-1-1" style="width:auto !important">
      <p style="color: green !important; ">
		<img class="cmr-info-icon" src="${resourcesPath}/images/info-icon.jpg"></img>
		<strong><span id="${param.modalId}-cmr-info-box-msg-modal" style="margin-left:20px !important;display:inline">${infoMessage}</span></strong>
	  </p>
	</div>
</div>
