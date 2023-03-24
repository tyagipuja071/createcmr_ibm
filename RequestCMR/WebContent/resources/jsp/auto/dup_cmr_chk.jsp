<%@page
	import="com.ibm.cio.cmr.request.model.automation.DuplicateCheckModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  String actionUrl = request.getContextPath() + "/auto/duplicate";
%>

<style>
#dupCMR_modal div.ibm-columns {
	width: 550px !important;
}
</style>

<script src="${resourcesPath}/js/auto/automation.js?${cmrv}"
	type="text/javascript"></script>
<script src="${resourcesPath}/js/auto/LA/br_automation.js?${cmrv}"
	type="text/javascript"></script>
<cmr:view forCountry="631">

	<cmr:modal id="dupCMR_modal" title="Matches found." widthId="570">
		<cmr:form method="POST" action="<%=actionUrl%>" id="frmDupCMR"
			name="frmDupCMR" class="ibm-column-form ibm-styled-form"
			modelAttribute="dupChkModel">
			<cmr:grid url="/auto/duplicate/cmrslist.json" id="dupCMRMatchGrid"
				width="530" height="300" usePaging="false">
				<cmr:gridCol width="100px" field="issuingCountry"
					header="Issuing Country" />
				<cmr:gridCol width="80px" field="cmrNo" header="CMR No." />
				<cmr:gridCol width="auto" field="customerName"
					header="Name and Address">
					<cmr:formatter functionName="matchDetailsFormatter" />
				</cmr:gridCol>
				<cmr:gridCol width="110px" field="vat" header="VAT(CNPJ)" />
			</cmr:grid>
			<br>
			<br>
			<br>       
		   What would you like to do? 
			<br>
			<a href="javascript:cmr.showModal('dupCMRReasonModal')">Proceed
				to create a duplicate CMR(will trigger approvals)</a>
			<br>
			<a href="javascript:closeDupCMRChkModal()">Take me back to the
				request screen to enter another VAT(CNPJ)</a>
			<br>
			<a href="javascript:Automation.redirectToWorkflow()">This CMR
				looks good,cancel the request creation.</a>
			<br>

		</cmr:form>
	</cmr:modal>


	<!--  Modal for the Status Change Screen -->
	<cmr:modal title="Please state reason for creating duplicate CMR."
		id="dupCMRReasonModal" widthId="570">
		<cmr:row>
			<cmr:column span="3">
				<textarea id="dupCmrRsn" rows="5" cols="50"></textarea>
			</cmr:column>
		</cmr:row>
		<cmr:buttonsRow>
			<cmr:hr />
			<cmr:button label="${ui.btn.save}" onClick="closeDupCMRReasonModal()"
				highlight="true" />
		</cmr:buttonsRow>
	</cmr:modal>

</cmr:view>