<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<form id="fileDlForm" name="fileDlForm" method="POST" action="${contextPath}/massrequest/download" target="fileDlFrame" >
  <input name="dlTokenId" id="dlTokenId" type="hidden">
  <input name="dlDocType" id="dlDocType" type="hidden">
  <input name="dlReqId" id="dlReqId" type="hidden">
  <input name="dlIterId" id="dlIterId" type="hidden">
  <input type="hidden" name="cmrIssuingCntry" value="${reqentry.cmrIssuingCntry}">
</form>
<iframe id="fileDlFrame" style="display:none" name="fileDlFrame"></iframe>