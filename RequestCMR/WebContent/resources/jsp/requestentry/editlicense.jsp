<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page
	import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.LicenseModel"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
LicenseModel license = (LicenseModel) request.getAttribute("licenseModel");
%>
<style>
span.role {
	margin-left: 20px;
}
#addUpdateLicenseModal {
  width: 600px;
}
</style>
<!--  Modal for adding CMRs -->
<cmr:modal title="License Entry:" id="addUpdateLicenseModal"
	widthId="700">
	<cmr:form method="GET" action="${contextPath}/request/license/process" name="addUpdateLicenseModal" class="ibm-column-form ibm-styled-form"
    modelAttribute="licenseModel" id="addUpdateLicenseModal">
	<cmr:row>
	</cmr:row>
		<cmr:hr />
		<cmr:row>
		</cmr:row>
		<cmr:row>
			<cmr:column span="1" containerForField="LicenseNumber" width="150">
				<p>
					<cmr:label fieldId="LicenseNumber">
						<cmr:fieldLabel fieldId="LicenseNumber" />:
              		</cmr:label>
					<cmr:field fieldId="LicenseNumber" id="licenseNum" path="licenseNum" size='130' />
				</p>
			</cmr:column>
			<cmr:column span="1" containerForField="LicenseValidFrom" width="190">
				<p>
					<cmr:label fieldId="LicenseValidFrom">
						<cmr:fieldLabel fieldId="LicenseValidFrom" />:
      				</cmr:label>
					<cmr:date path="validFrom" format="yyyyMMdd" />
				</p>
			</cmr:column>
			<cmr:column span="1" containerForField="LicenseValidTo" width="190">
				<p>
					<cmr:label fieldId="LicenseValidTo">
						<cmr:fieldLabel fieldId="LicenseValidTo" />:
      				</cmr:label>
					<cmr:date path="validTo" format="yyyyMMdd" />
				</p>
			</cmr:column>
			<input type="hidden" id="oldLicenseNumber" name="oldLicenseNumber" />

		</cmr:row>	
		<cmr:hr />
		<cmr:buttonsRow>
		<cmr:button label="Add/Update License" onClick="performAddUpdate()"
			highlight="true" pad="true" id="licenseActionBtn" />
			
		<cmr:button label="Cancel" onClick="closeLicenseModal()"
			highlight="false" pad="true" />
			<cmr:hr />
	</cmr:buttonsRow>
	</cmr:form>
</cmr:modal>
