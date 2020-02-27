<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  String actionUrlMatching = request.getContextPath() + "/auto/import";
    String reqId = request.getParameter("reqId");
    String processNm = request.getParameter("processNm");
%>

<script src="${resourcesPath}/js/auto/automation.js?${cmrv}"
	type="text/javascript"></script>
<cmr:modal id="result_match_modal" title="${processNm} Results"
	widthId="570">
	<form:form method="POST" action="<%=actionUrlMatching%>" id="frmCMRAutoImportMatch"
		name="frmCMRAutoImportMatch" class="ibm-column-form ibm-styled-form"
		modelAttribute="matchedRecord">
			<cmr:grid url="/auto/results/matches/import/matching_list.json"
				id="autoResults_match_importGrid" width="510" height="400"
				usePaging="false">
				<cmr:gridCol width="50px" field="itemNo" header="Item Number" />
				<cmr:gridCol width="175px" field="matchKeyValue"
					header="Importable Details">
					<cmr:formatter functionName="matchDetailsFormatter" />
				</cmr:gridCol>
				<cmr:gridCol width="150px" field="matchGradeValue"
					header="Match Grade">
					<cmr:formatter functionName="matchDetailsFormatter" />
				</cmr:gridCol>
				<cmr:gridCol width="100px" field="importedIndc" header="Actions">
					<cmr:formatter functionName="matchImportFormatter" />
				</cmr:gridCol>
			</cmr:grid>

		<cmr:buttonsRow>
			<br>
			<cmr:button label="${ui.btn.close}"
				onClick="cmr.hideModal('result_match_modal')" highlight="false"
				pad="true" />
		</cmr:buttonsRow>
	</form:form>
</cmr:modal>