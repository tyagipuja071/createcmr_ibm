<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<!--  Modal for the Search Rejection Info Screen -->
<cmr:modal title="${ui.title.addrStd}" id="AddrStdModal"
	widthId="750">
	<form:form method="GET" action="${contextPath}/request/addrstd"
		name="frmCMR_srchRejAddrStd" class="ibm-column-form ibm-styled-form"
		modelAttribute="reqentry">
		
		
		<h1>Display the results of Add std check here <h1>

		<cmr:buttonsRow>
			<cmr:hr />
			<cmr:button label="${ui.btn.close}"
				onClick="cmr.hideModal('AddrStdModal')" highlight="true" />
		</cmr:buttonsRow>
	</form:form>
</cmr:modal>