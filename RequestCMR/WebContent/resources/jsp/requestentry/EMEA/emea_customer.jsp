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
		
		<cmr:view forCountry="726">
			<cmr:column span="2" containerForField="ModeOfPayment">
				<p>
					<cmr:label fieldId="modeOfPayment">
						<cmr:fieldLabel fieldId="ModeOfPayment" />: 
          			</cmr:label>
					<cmr:field path="paymentMode" id="modeOfPayment"
						fieldId="ModeOfPayment" tabId="MAIN_CUST_TAB" />
				</p>
			</cmr:column>		
		</cmr:view>
		
	</cmr:row>
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