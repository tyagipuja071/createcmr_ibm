<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/revivedcmrs/revivedcmrs.js" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    if (FormManager) {
	  // add validators here
      FormManager.ready();
    }
  });
</script>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:form method="POST" action="${contextPath}/revivedcmrs/process" name="frmCMRRevived" class="ibm-column-form ibm-styled-form" 
	 id="frmCMRRevived" enctype="multipart/form-data" target="processFrame">
    <cmr:section>
		      <cmr:row topPad="10" addBackground="true">
        <cmr:column span="7">
		    <p align="right">
		      <a href="javascript:downloadRevCMRTemplate()">${ui.massDnlTmpl}</a>
		    </p>
		</cmr:column>
	    <cmr:column span="2">
	      <p>
	      	
	        <label for="revivedcmrsFile"> ${ui.revCmrFileUpload}: </label>
	        <input type="file" id="revivedcmrsFile" accept=".xlsx" name="revivedcmrsFile">
	      </p>
    	</cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="true">
        <cmr:column span="3">
          <cmr:button label="Process Revived CMRS" onClick="submitRevCMRSFile()" highlight="true" />
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
      &nbsp;
      </cmr:row>	  
    </cmr:section>
    <input name="processTokenId" id="processTokenId" type="hidden">
  </cmr:form>
  <iframe id="processFrame" style="display:none" name="processFrame"></iframe>
        	<form>
			<input name="dummyform" type="hidden">
		</form>
		<form id="fileTemplateDownloadForm" name="fileTemplateDownloadForm" method="POST" action="${contextPath}/revcmrs/template/download" target="fileTemplateDownloadFrame">
		  <input name="dlTokenId" id="dlTokenId" type="hidden">
		  <input name="dlDocType" id="dlDocType" type="hidden">
		  <input name="dlReqId" id="dlReqId" type="hidden">
		  <input name="dlIterId" id="dlIterId" type="hidden">
		</form>
		<iframe id="fileTemplateDownloadFrame" style="display:none" name="fileTemplateDownloadFrame"></iframe>
</cmr:boxContent>