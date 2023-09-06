<%@page import="com.ibm.cio.cmr.request.model.auto.BaseV2RequestModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page
	import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  BaseV2RequestModel reqentry = (BaseV2RequestModel) request.getAttribute("reqentry");
  AppUser user = AppUser.getUser(request);
%>
<!-- Automation Scripts for Brazil -->
<cmr:view forCountry="631">

	<!-- JS first -->
	<script src="${resourcesPath}/js/auto/LA/br_automation.js?${cmrv}"
		type="text/javascript"></script>

	<!-- JSP -->

	<cmr:row addBackground="true">
		<cmr:column span="2" containerForField="VAT">
			<p>
				<cmr:label fieldId="vat">
					<cmr:fieldLabel fieldId="VAT" />: 
      </cmr:label>
				<cmr:field id="vat" path="vat" fieldId="VAT"
					tabId="MAIN_GENERAL_TAB" />
			</p>
		</cmr:column>
		<cmr:column span="2" containerForField="LocalTax2">
			<p>
				<cmr:label fieldId="municipalFiscalCode">
					<cmr:fieldLabel fieldId="LocalTax2" />: 
      </cmr:label>
				<cmr:field id="municipalFiscalCode" path="municipalFiscalCode"
					fieldId="LocalTax2" tabId="MAIN_GENERAL_TAB" />
			</p>
		</cmr:column>
		<cmr:column span="2" containerForField="TaxCodeBR">
			<p>
				<cmr:label fieldId="taxCode">
					<cmr:fieldLabel fieldId="TaxCodeBR" />: 
      </cmr:label>
				<cmr:field id="taxCode" path="taxCode" fieldId="TaxCodeBR"
					tabId="MAIN_GENERAL_TAB" />
			</p>
		</cmr:column>
	</cmr:row>

	<cmr:row addBackground="true">
		<cmr:column span="2" containerForField="EndUserVAT">
			<p>
				<cmr:label fieldId="vatEndUser">
					<cmr:fieldLabel fieldId="EndUserVAT" />: 
      </cmr:label>
				<cmr:field id="vatEndUser" path="vatEndUser" fieldId="EndUserVAT"
					tabId="MAIN_GENERAL_TAB" />
			</p>
		</cmr:column>
		<cmr:column span="2" containerForField="EndUserFiscalCode">
			<p>
				<cmr:label fieldId="municipalFiscalCodeEndUser">
					<cmr:fieldLabel fieldId="EndUserFiscalCode" />: 
      </cmr:label>
				<cmr:field id="municipalFiscalCodeEndUser"
					path="municipalFiscalCodeEndUser" fieldId="EndUserFiscalCode"
					tabId="MAIN_GENERAL_TAB" />
			</p>
		</cmr:column>
	</cmr:row>

	<cmr:row addBackground="true">
		<cmr:column span="2" containerForField="Email1">
			<p>
				<cmr:label fieldId="email1">
					<cmr:fieldLabel fieldId="Email1" />: 
      </cmr:label>
				<cmr:field id="email1" path="email1" fieldId="Email1"
					tabId="MAIN_GENERAL_TAB" />
			</p>
		</cmr:column>
		<cmr:column span="2" containerForField="Email2">
			<p>
				<cmr:label fieldId="email2">
					<cmr:fieldLabel fieldId="Email2" />: 
      </cmr:label>
				<cmr:field id="email2" path="email2" fieldId="Email2"
					tabId="MAIN_GENERAL_TAB" />
			</p>
		</cmr:column>
		<cmr:column span="2" containerForField="Email3">
			<p>
				<cmr:label fieldId="email3">
					<cmr:fieldLabel fieldId="Email3" />: 
      </cmr:label>
				<cmr:field id="email3" path="email3" fieldId="Email3"
					tabId="MAIN_GENERAL_TAB" />
			</p>
		</cmr:column>
	</cmr:row>
	<cmr:row addBackground="true">
		<cmr:column span="2" containerForField="ProxiLocationNumber">
			<p>
				<cmr:label fieldId="proxiLocnNo">
					<cmr:fieldLabel fieldId="ProxiLocationNumber" />:
      </cmr:label>
				<cmr:field fieldId="ProxiLocationNumber" id="proxiLocnNo"
					path="proxiLocnNo" tabId="MAIN_GENERAL_TAB" />
			</p>
		</cmr:column>
    <cmr:column span="2" containerForField="GovernmentType">
      <p>
        <cmr:label fieldId="govType">
          <cmr:fieldLabel fieldId="GovernmentType" />:
        </cmr:label>
        <cmr:field id="govType" path="govType" fieldId="GovernmentType" tabId="MAIN_GENERAL_TAB" size="400" />
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
		<cmr:column span="5">
			<p id="updReason">
				<cmr:label fieldId="updateReason">
              	Update Reason:
				</cmr:label>

				<form:radiobutton path="updateReason" value="AUCO" />
				<cmr:label fieldId="updateReason1" forRadioOrCheckbox="true">Add/Update Contacts Only</cmr:label>

				<form:radiobutton path="updateReason" value="UPCI" />
				<cmr:label fieldId="updateReason2" forRadioOrCheckbox="true">Update Company Information</cmr:label>

				<form:radiobutton path="updateReason" value="REAC" />
				<cmr:label fieldId="updateReason3" forRadioOrCheckbox="true">Reactivate CMR</cmr:label>

				<form:radiobutton path="updateReason" value="UPIC" />
				<cmr:label fieldId="updateReason4" forRadioOrCheckbox="true">Update IBM Codes</cmr:label>
			</p>
		</cmr:column>
	</cmr:row>
	<cmr:row addBackground="true">
		<cmr:column span="2" containerForField="Company">
			<p>
				<cmr:label fieldId="company">
					<cmr:fieldLabel fieldId="Company" />:
				</cmr:label>
				<cmr:field fieldId="Company" id="company" path="company" tabId="MAIN_IBM_TAB" />
			</p>
		</cmr:column>
		<cmr:column span="1" width="140" containerForField="INACCode">
			<p>
				<cmr:label fieldId="inacCd">
					<cmr:fieldLabel fieldId="INACCode" />:
				</cmr:label>
				<cmr:field size="80" fieldId="INACCode" id="inacCd" path="inacCd" />
			</p>
		</cmr:column>
	</cmr:row>
	
	<cmr:row addBackground="true">
		<cmr:column span="2" containerForField="CollectorNameNo">
			<p>
				<cmr:label fieldId="collectorNameNo">
					<cmr:fieldLabel fieldId="CollectorNameNo" />:
				</cmr:label>
				<cmr:field fieldId="CollectorNameNo" id="collectorNameNo" path="collectorNameNo" />
			</p>
		</cmr:column>
		<cmr:column span="2" containerForField="SalesBusOff">
			<p>
				<cmr:label fieldId="salesBusOffCd">
					<cmr:fieldLabel fieldId="SalesBusOff" />:
				</cmr:label>
				<cmr:field fieldId="SalesBusOff" id="salesBusOffCd" path="salesBusOffCd" />
			</p>
		</cmr:column>
	</cmr:row>
	<form:hidden path="isuCd" id="isuCd" />
	<!-- temporary placeholder for Update Reason value -->
	<form:hidden path="mexicoBillingName" id="mexicoBillingName" />
</cmr:view>
