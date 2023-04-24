<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page
	import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
  boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
%>

<cmr:view forGEO="EMEA">

	<!-- Add hidden fields to keep imported values -->
	<form:hidden path="orgNo" />
	<form:hidden path="sourceCd" />
	<form:hidden path="mrcCd" />
	<cmr:view exceptForCountry="758,726">
		<form:hidden path="sitePartyId" />
	</cmr:view>
	<form:hidden path="searchTerm" />
	<form:hidden path="currencyCd" />
	<cmr:view exceptForCountry="755">
		<form:hidden path="engineeringBo" />
	</cmr:view>
	<cmr:view forCountry="758">
		<form:hidden path="fiscalDataStatus" />
		<form:hidden path="fiscalDataCompanyNo" />
		<form:hidden path="nationalCusId" />
	</cmr:view>
	<cmr:row addBackground="true">
		<cmr:column span="2" containerForField="LocalTax1"
			forCountry="754,866">
			<p>
				<label for="taxCd1"> <cmr:fieldLabel fieldId="LocalTax1" />:
					<cmr:delta text="${rdcdata.taxCd1}" oldValue="${reqentry.taxCd1}" />
					<cmr:view forCountry="866">
						<cmr:info text="${ui.info.CRN_UK}" />
					</cmr:view> <cmr:view forCountry="754">
						<cmr:info text="${ui.info.CRN_I}" />
					</cmr:view>
				</label>
				<cmr:field path="taxCd1" id="taxCd1" fieldId="LocalTax1"
					tabId="MAIN_CUST_TAB" />
			</p>
		</cmr:column>
		<cmr:column span="2" containerForField="CRNExempt"
			forCountry="754,866">
			<p>
				<cmr:label fieldId="crnExempt">&nbsp;</cmr:label>
				<cmr:field fieldId="CRNExempt" id="restrictInd" path="restrictInd"
					tabId="MAIN_CUST_TAB" />
				<cmr:label fieldId="restrictInd" forRadioOrCheckbox="true">
					<cmr:fieldLabel fieldId="CRNExempt" />
				</cmr:label>
			</p>
		</cmr:column>
	</cmr:row>

	<cmr:row addBackground="false">
		<cmr:column span="2" containerForField="SpecialTaxCd"
			exceptForCountry="666,726,862">
			<p>

				<label for="specialTaxCd"> <cmr:fieldLabel
						fieldId="SpecialTaxCd" />:
				</label>
				<cmr:field fieldId="SpecialTaxCd" id="specialTaxCd"
					path="specialTaxCd" tabId="MAIN_CUST_TAB" />
			</p>
		</cmr:column>
		<cmr:view forCountry='758'>
			<c:if test="${reqentry.reqType != 'C'}">
				<%
				  if (!readOnly) {
				%>
				<cmr:column span="2" width="170">
					<p>

						<cmr:buttonsRow>
							<cmr:button id="validateFiscalData"
								label="${ui.btn.validateFiscalData}"
								onClick="doValidateFiscalDataModal()" highlight="true" />
						</cmr:buttonsRow>
					</p>
				</cmr:column>
				<%
				  }
				%>
			</c:if>
		</cmr:view>
		<cmr:column span="2" containerForField="AbbrevLocation">
			<p>
				<label for="abbrevLocn"> <cmr:fieldLabel
						fieldId="AbbrevLocation" />:
				</label>
				<cmr:field fieldId="AbbrevLocation" id="abbrevLocn"
					path="abbrevLocn" tabId="MAIN_CUST_TAB" />
			</p>
		</cmr:column>
		<!-- CMR-2093: add CoF field for Turkey -->
		<cmr:column span="2" containerForField="CommercialFinanced"
			forCountry="862">
			<p>
				<cmr:label fieldId="commercialFinanced">
					<cmr:fieldLabel fieldId="CommercialFinanced" />:
	              <cmr:delta text="${rdcdata.commercialFinanced}"
						oldValue="${reqentry.commercialFinanced}" />
				</cmr:label>
				<cmr:field path="commercialFinanced" id="commercialFinanced"
					fieldId="CommercialFinanced" tabId="MAIN_CUST_TAB" />
			</p>
		</cmr:column>
		<cmr:column span="2" containerForField="EmbargoCode">
			<p>
				<cmr:label fieldId="embargoCd">
					<cmr:fieldLabel fieldId="EmbargoCode" />:
				<cmr:delta text="${rdcdata.embargoCd}"
						oldValue="${reqentry.embargoCd}" />
				</cmr:label>
				<cmr:field path="embargoCd" id="embargoCd" fieldId="EmbargoCode"
					tabId="MAIN_CUST_TAB" />
			</p>
		</cmr:column>

		<!-- fields for Cyprus Legacy -->
		<cmr:view forCountry="666">
			<c:if test="${reqentry.reqType != 'C'}">
			<cmr:column span="2" containerForField="ModeOfPayment">
				<p>
				<cmr:label fieldId="modeOfPayment">
					<cmr:fieldLabel fieldId="ModeOfPayment" />: 
        					</cmr:label>
				<cmr:field path="paymentMode" id="modeOfPayment"
					fieldId="ModeOfPayment" tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>
			</c:if>
		</cmr:view>	

		<!-- Type Of Customer CY -->
		<c:if test="${reqentry.reqType != 'C'}">
		<cmr:view forCountry="666">
				<cmr:column span="2" containerForField="TypeOfCustomer">
	        		<p>
	          			<cmr:label fieldId="crosSubTyp">
							<cmr:fieldLabel fieldId="TypeOfCustomer" />:
							<cmr:info text="${ui.info.crosSubTyp}" />
						</cmr:label>
	          			<cmr:field path="crosSubTyp" id="crosSubTyp" fieldId="TypeOfCustomer" tabId="MAIN_CUST_TAB" />
	        		</p>
	      		</cmr:column>
		</cmr:view>
		</c:if>
		
		<cmr:view forCountry="726">
			<cmr:column span="2" containerForField="ModeOfPayment">
				<p>
					<cmr:label fieldId="modeOfPayment">
						<cmr:fieldLabel fieldId="ModeOfPayment" />:
						<cmr:delta text="${rdcdata.modeOfPayment}"
						oldValue="${reqentry.paymentMode}" /> 
          			</cmr:label>
					<cmr:field path="paymentMode" id="modeOfPayment"
						fieldId="ModeOfPayment" tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>
		</cmr:view>

	</cmr:row>
	<cmr:view forCountry="755">
		<cmr:row addBackground="false">
			<cmr:column span="2" containerForField="CustClass">
		        <p>
		          <cmr:label fieldId="custClass">
		            <cmr:fieldLabel fieldId="CustClass" />:
		            <cmr:delta text="${rdcdata.custClass}" oldValue="${reqentry.custClass}" code="L" /> 
		          </cmr:label>
		          <cmr:field path="custClass" id="custClass" fieldId="CustClass" tabId="MAIN_CUST_TAB" />
		        </p>
		    </cmr:column>
		</cmr:row>
	</cmr:view>
	<cmr:view forCountry="726">
		<cmr:row addBackground="true">

			<cmr:column span="2" containerForField="CrosSubTyp">
				<p>
					<cmr:label fieldId="crosSubTyp">
						<cmr:fieldLabel fieldId="CrosSubTyp" />:
					</cmr:label>
					<cmr:field path="crosSubTyp" id="crosSubTyp" fieldId="CrosSubTyp"
						tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>
		</cmr:row>
	</cmr:view>
	
	<cmr:view forCountry="XXXX">
		<cmr:row addBackground="false">
			<cmr:column span="2" containerForField="OrgNo">
				<p>

					<label for="orgNo"> <cmr:fieldLabel fieldId="OrgNo" />:
					</label>
					<cmr:field fieldId="OrgNo" id="orgNo" path="orgNo"
						tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>

			<cmr:column span="2" containerForField="SourceCd">
				<p>

					<label for="sourceCd"> <cmr:fieldLabel fieldId="SourceCd" />:
					</label>
					<cmr:field fieldId="SourceCd" id="sourceCd" path="sourceCd"
						tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>
			<cmr:column span="2" containerForField="AcAdminBo">
				<p>

					<label for="acAdminBo"> <cmr:fieldLabel fieldId="AcAdminBo" />:
					</label>
					<cmr:field fieldId="AcAdminBo" id="acAdminBo" path="acAdminBo"
						tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>
		</cmr:row>
	</cmr:view>

	<cmr:view forCountry="862">
		<cmr:row addBackground="true">
			<cmr:column span="2" containerForField="EconomicCd2">
				<p>
					<cmr:label fieldId="economicCd">
						<cmr:fieldLabel fieldId="EconomicCd2" />:
           <cmr:delta text="${rdcdata.economicCd}"
							oldValue="${reqentry.economicCd}" code="R" />
					</cmr:label>
					<cmr:field path="economicCd" id="economicCd" fieldId="EconomicCd2"
						tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>
			<cmr:column span="2" containerForField="CustClass">
		        <p>
		          <cmr:label fieldId="custClass">
		            <cmr:fieldLabel fieldId="CustClass" />:
		          </cmr:label>
		          <cmr:field path="custClass" id="custClass" fieldId="CustClass" tabId="MAIN_CUST_TAB" />
		        </p>
		    </cmr:column>
		</cmr:row>
		<cmr:row addBackground="true">
			<cmr:column span="2" containerForField="TypeOfCustomer">
		        <p>
		          <cmr:label fieldId="crosSubTyp">
		            <cmr:fieldLabel fieldId="TypeOfCustomer" />:
		          </cmr:label>
		          <cmr:field path="crosSubTyp" id="crosSubTyp" fieldId="TypeOfCustomer" tabId="MAIN_CUST_TAB" />
		        </p>
		    </cmr:column>
		</cmr:row>
	</cmr:view>

	<cmr:view forCountry="866,754">
		<cmr:row addBackground="true">
			<cmr:column span="2" containerForField="CustClass">
				<p>
					<label for="custClass">
						<cmr:fieldLabel fieldId="CustClass" />:
						<cmr:view>
							<span id="info">
								<cmr:info text="${ui.info.custClass}" />
							</span>
						</cmr:view>
					</label>
					<cmr:field fieldId="CustClass" id="custClass" path="custClass"
						tabId="MAIN_IBM_TAB" />
				</p>
			</cmr:column>
		</cmr:row>
	</cmr:view>

	<cmr:view forCountry="754">
		<div id="licenseFieldsDiv">
			<cmr:row>
				<cmr:column span="6">
					<p>
						<cmr:label fieldId="licenseTableDesc">
              ${ui.LicenseTableDesc}:
          <cmr:info text="${ui.info.LicenseTableDesc}" />
						</cmr:label>
					</p>
				</cmr:column>
			</cmr:row>
			<cmr:row>
				<cmr:column span="4">
					<cmr:grid url="/request/license/list.json" id="LICENSES_GRID"
						span="4" height="220" usePaging="false">

						<!-- Machine Type -->
						<cmr:gridParam fieldId="reqId" value="${reqentry.reqId}" />
						<cmr:gridCol width="150px" field="licenseNum"
							header="${ui.grid.licenseNumber}" />
						<cmr:gridCol width="150px" field="validFrom"
							header="${ui.grid.validDateFrom}" />

						<cmr:gridCol width="150px" field="validTo"
							header="${ui.grid.validDateTo}" />
						<cmr:gridCol width="75px" field="currentIndc"
							header="${ui.grid.change}">
							<cmr:formatter functionName="licenseImportIndFormatter" />
						</cmr:gridCol>

						<cmr:gridCol width="100px" field="action"
							header="${ui.grid.action}">
							<cmr:formatter functionName="removeLicenseFormatter" />
						</cmr:gridCol>

					</cmr:grid>
				</cmr:column>
			</cmr:row>
			<div id="licenseAddNewDiv">

				<cmr:row topPad="15">
					<cmr:column span="4">
						<p>
							<cmr:label fieldId="licenseInfo">
								<strong>${ui.addNewLicense}</strong>
								<cmr:info text="${ui.info.addNewLicenseInfo}" />
							</cmr:label>
						</p>
					</cmr:column>
				</cmr:row>
				<cmr:row>
					<cmr:column span="1" containerForField="LicenseNumber" width="130">
						<p>
							<cmr:label fieldId="LicenseNumber">
								<cmr:fieldLabel fieldId="LicenseNumber" />:

              </cmr:label>
							<cmr:field fieldId="LicenseNumber" id="licenseNumber"
								path="licenseNumber" size='120' />
						</p>
					</cmr:column>
					<cmr:column span="1" containerForField="LicenseValidFrom"
						width="130">
						<p>
							<cmr:label fieldId="LicenseValidFrom">
								<cmr:fieldLabel fieldId="LicenseValidFrom" />:
      </cmr:label>
							<cmr:date path="licenseValidFrom" format="yyyyMMdd" />
						</p>
					</cmr:column>
					<cmr:column span="1" containerForField="LicenseValidTo" width="130">
						<p>
							<cmr:label fieldId="LicenseValidTo">
								<cmr:fieldLabel fieldId="LicenseValidTo" />:
      </cmr:label>
							<cmr:date path="licenseValidTo" format="yyyyMMdd" />
						</p>
					</cmr:column>
					<cmr:column span="2">
						<div style="padding-top: 15px">
							<cmr:button label="${ui.btn.addLicense}" onClick="doAddLicense()"
								id="addLicenseButton" />
						</div>
					</cmr:column>
				</cmr:row>
			</div>
			<cmr:row topPad="15" />
		</div>
	</cmr:view>

	<cmr:view forCountry="755">
		<form:hidden path="economicCd" id="economicCd" />
	</cmr:view>

	<cmr:view forCountry="758">
		<cmr:row addBackground="true">
			<cmr:column span="2" containerForField="IdentClient">
				<p>
					<cmr:label fieldId="identClient">
						<cmr:fieldLabel fieldId="IdentClient" />:
                <cmr:info text="${ui.info.IDENTCLIENT}" />
					</cmr:label>
					<cmr:field path="identClient" id="identClient"
						fieldId="IdentClient" tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>

			<cmr:column span="2" containerForField="CrosSubTyp">
				<p>
					<cmr:label fieldId="crosSubTyp">
						<cmr:fieldLabel fieldId="CrosSubTyp" />:
                <cmr:info text="${ui.info.taxPayerCustCd}" />
					</cmr:label>
					<cmr:field path="crosSubTyp" id="crosSubTyp" fieldId="CrosSubTyp"
						tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>

			<c:if test="${reqentry.reqType != 'C'}">
				<cmr:column span="2" containerForField="ModeOfPayment">
					<p>
						<cmr:label fieldId="modeOfPayment">
							<cmr:fieldLabel fieldId="ModeOfPayment" />: 
          </cmr:label>
						<cmr:field path="paymentMode" id="modeOfPayment"
							fieldId="ModeOfPayment" tabId="MAIN_CUST_TAB" />
					</p>
				</cmr:column>
			</c:if>
		</cmr:row>
		<!--Start: New 4 fields for Italy Legacy -->
		<cmr:row>
			<cmr:column span="2" containerForField="TipoCliente">
				<p>
					<cmr:label fieldId="icmsInd">
						<cmr:fieldLabel fieldId="TipoCliente" />:
                <cmr:delta text="${rdcdata.icmsInd}"
							oldValue="${reqentry.icmsInd}" />
					</cmr:label>
					<cmr:field id="icmsInd" path="icmsInd" fieldId="TipoCliente"
						tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>
			<cmr:column span="2" containerForField="CodiceDestinatarioUfficio">
				<p>
					<cmr:label fieldId="hwSvcsRepTeamNo">
						<cmr:fieldLabel fieldId="CodiceDestinatarioUfficio" />:
				<cmr:delta text="${rdcdata.hwSvcsRepTeamNo}"
							oldValue="${reqentry.hwSvcsRepTeamNo}" />
					</cmr:label>
					<cmr:field path="hwSvcsRepTeamNo" id="hwSvcsRepTeamNo"
						fieldId="CodiceDestinatarioUfficio" tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>
			<cmr:column span="2" containerForField="PEC">
				<p>
					<cmr:label fieldId="email2">
						<cmr:fieldLabel fieldId="PEC" />:
				<cmr:delta text="${rdcdata.email2}" oldValue="${reqentry.email2}" />
					</cmr:label>
					<cmr:field path="email2" id="email2" fieldId="PEC"
						tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>
		</cmr:row>
		<cmr:row>
			<cmr:column span="2" containerForField="IndirizzoEmail">
				<p>
					<cmr:label fieldId="email3">
						<cmr:fieldLabel fieldId="IndirizzoEmail" />:
    	<cmr:delta text="${rdcdata.email3}" oldValue="${reqentry.email3}" />
					</cmr:label>
					<cmr:field path="email3" id="email3" fieldId="IndirizzoEmail"
						tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>
		</cmr:row>
		<!--End: New 4 fields for Italy Legacy -->
	</cmr:view>
</cmr:view>