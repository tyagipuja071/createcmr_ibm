<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  String actionUrl = request.getContextPath() + "/auto/duplicate";
%>

<script src="${resourcesPath}/js/auto/automation.js?${cmrv}"
	type="text/javascript"></script>
<script src="${resourcesPath}/js/auto/LA/br_automation.js?${cmrv}"
	type="text/javascript"></script>
	<cmr:view forCountry="631">
<cmr:modal id="dupReq_modal" title="Request Matches found."
	widthId="570">
<cmr:form method="POST" action="<%=actionUrl%>" id="frmDupReq"
		name="frmDupReq" class="ibm-column-form ibm-styled-form"
		modelAttribute="dupChkModel">
	 		<cmr:grid url="/auto/duplicate/reqslist.json"
				id="dupReqMatchGrid" width="550" height="300"
				usePaging="false">
				<cmr:gridCol width="100px" field="issuingCountry" header="Issuing Country" />
				<cmr:gridCol width="80px" field="reqId" header="Request ID"/>
               <cmr:gridCol width="180px" field="customerName" header="Name and Address">
               <cmr:formatter functionName="matchDetailsFormatter" />
               </cmr:gridCol>
				<cmr:gridCol width="80px" field="matchType" header="Match Grade"/>
				 <cmr:gridCol width="150px" field="message" header="Actions" >
            <cmr:formatter functionName="Automation.notifyActionFormatter"/>
          </cmr:gridCol>
			</cmr:grid> 
			<br>
			<br>
         <cmr:buttonsRow>
			<br>     		<br>
     		<cmr:button label="No,I will check the request myself."
				onClick="Automation.redirectToWorkflow()" highlight="false"
				pad="true" />
		</cmr:buttonsRow>         
	</cmr:form>
</cmr:modal>
</cmr:view>