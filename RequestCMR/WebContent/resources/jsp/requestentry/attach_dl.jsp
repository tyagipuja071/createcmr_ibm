<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<form id="attachDlForm" name="attachDlForm" method="POST" action="${contextPath}/request/attachment/download" target="attachDlFrame">
  <input name="reqId" id="attachDlReqId" type="hidden">
  <input name="docLink" id="attachDlDocLink" type="hidden">
  <input name="tokenId" id="attachDlTokenId" type="hidden">
</form>
<iframe id="attachDlFrame" style="display:none" name="attachDlFrame"></iframe>
<form id="attachRemoveForm" name="attachRemoveForm" method="POST" action="${contextPath}/request/attachment.json">
  <input name="action" id="attachRemoveForm_modelAction" type="hidden">
  <input name="reqId" id="attachRemoveReqId" type="hidden">
  <input name="docLink" id="attachRemoveDocLink" type="hidden">
</form>
<form name="frmPDF" id="frmPDF" action="${contextPath}/request/pdf" method="POST" target="attachDlFrame">
  <input type="hidden" id="pdfReqId" name="reqId">
  <input type="hidden" id="pdfTokenId" name="tokenId">
</form>
