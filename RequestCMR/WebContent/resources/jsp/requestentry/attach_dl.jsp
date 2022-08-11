<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<form _csrf="GhtjeYhfngleOImde2" id="attachDlForm" name="attachDlForm" method="POST" action="${contextPath}/request/attachment/download" target="attachDlFrame">
  <input name="reqId" id="attachDlReqId" type="hidden">
  <input name="docLink" id="attachDlDocLink" type="hidden">
  <input name="tokenId" id="attachDlTokenId" type="hidden">
  <input type="hidden" name="_csrf" id="_csrf" value="GhtjeYhfngleOImde2" />
</form>
<iframe id="attachDlFrame" style="display:none" name="attachDlFrame"></iframe>
<form _csrf="GhtjeYhfngleOImde2" id="attachRemoveForm" name="attachRemoveForm" method="POST" action="${contextPath}/request/attachment.json">
  <input name="action" id="attachRemoveForm_modelAction" type="hidden">
  <input name="reqId" id="attachRemoveReqId" type="hidden">
  <input name="docLink" id="attachRemoveDocLink" type="hidden">
  <input type="hidden" name="_csrf" id="_csrf" value="GhtjeYhfngleOImde2" />
</form>
<form _csrf="GhtjeYhfngleOImde2" name="frmPDF" id="frmPDF" action="${contextPath}/request/pdf" method="POST" target="attachDlFrame">
  <input type="hidden" id="pdfReqId" name="reqId">
  <input type="hidden" id="pdfTokenId" name="tokenId">
  <input type="hidden" name="_csrf" id="_csrf" value="GhtjeYhfngleOImde2" />
</form>
