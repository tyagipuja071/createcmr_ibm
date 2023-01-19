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
<cmr:view forGEO="MCO,MCO1,MCO2">
	<cmr:row topPad="10">
		<cmr:column span="2" containerForField="SalRepNameNo"
			exceptForCountry="780">
			<p>
				<cmr:label fieldId="repTeamMemberNo">
					<cmr:fieldLabel fieldId="SalRepNameNo" />:
           <cmr:delta text="${rdcdata.repTeamMemberNo}"
						oldValue="${reqentry.repTeamMemberNo}" id="delta-repTeamMemberNo" />
				</cmr:label>
				<cmr:field fieldId="SalRepNameNo" id="repTeamMemberNo"
					path="repTeamMemberNo" tabId="MAIN_IBM_TAB" />
			</p>
		</cmr:column>

		<cmr:column span="2" containerForField="Enterprise" forCountry="780,864">
			<p>
				<cmr:label fieldId="enterprise">
					<cmr:fieldLabel fieldId="Enterprise" />:
              <cmr:delta text="${rdcdata.enterprise}"
						oldValue="${reqentry.enterprise}" />
				</cmr:label>
				<cmr:field id="enterprise" path="enterprise" fieldId="Enterprise"
					tabId="MAIN_IBM_TAB" />
			</p>
		</cmr:column>
		
		<%-- <cmr:column span="2" containerForField="EngineeringBo" forCountry="838">
      <p>
        <cmr:label fieldId="engineeringBo">
          <cmr:fieldLabel fieldId="EngineeringBo" />:
           <cmr:delta text="${rdcdata.engineeringBo}" oldValue="${reqentry.engineeringBo}" id="delta-engineeringBo" />
        </cmr:label>
        <cmr:field fieldId="EngineeringBo" id="engineeringBo" path="engineeringBo" tabId="MAIN_IBM_TAB"/>
      </p>
    </cmr:column>     --%>
		<cmr:column span="2" containerForField="SalesBusOff"
			exceptForCountry="838">
			<p>
				<cmr:label fieldId="salesBusOffCd">
					<cmr:fieldLabel fieldId="SalesBusOff" />:
        </cmr:label>
				<cmr:field fieldId="SalesBusOff" id="salesBusOffCd"
					path="salesBusOffCd" tabId="MAIN_IBM_TAB" />
			</p>
		</cmr:column>

		<cmr:view forCountry="838">
			<%
			  if (reqentry.getReqType().equalsIgnoreCase("U")) {
			%>
			<cmr:column span="2" containerForField="SalesBusOff2">
				<p>
					<cmr:label fieldId="salesBusOffCd">
						<cmr:fieldLabel fieldId="SalesBusOff" />: 
               <cmr:delta text="${rdcdata.salesBusOffCd}"
							oldValue="${reqentry.salesBusOffCd}" id="delta-salesBusOffCd2" />
					</cmr:label>
					<cmr:field path="salesBusOffCd" id="salesBusOffCd2"
						fieldId="SalesBusOff2" tabId="MAIN_IBM_TAB" />
				</p>
			</cmr:column>
			<%
			  } else if (reqentry.getReqType().equalsIgnoreCase("C")) {
			%>
			<cmr:column span="2" containerForField="SalesBusOff">
				<p>
					<cmr:label fieldId="salesBusOffCd">
						<cmr:fieldLabel fieldId="SalesBusOff" />:
          </cmr:label>
					<cmr:field fieldId="SalesBusOff" id="salesBusOffCd"
						path="salesBusOffCd" tabId="MAIN_IBM_TAB" />
				</p>
			</cmr:column>

			<%
			  }
			%>
		</cmr:view>
		<%-- END of SBO and Sales Rep --%>

	</cmr:row>

	<cmr:view forGEO="MCO1,MCO2">
		<cmr:row>
		  <cmr:column span="2" containerForField="InternalDept" forCountry="864">
        <p>
          <cmr:label fieldId="ibmDeptCostCenter">
            <cmr:fieldLabel fieldId="InternalDept" />: 
          </cmr:label>
          <cmr:field path="adminDeptLine" id="ibmDeptCostCenter" fieldId="InternalDept" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      
			<cmr:column span="2" containerForField="InternalDept" exceptForCountry="864,780">
				<p>
					<cmr:label fieldId="ibmDeptCostCenter">
						<cmr:fieldLabel fieldId="InternalDept" />: 
          </cmr:label>
					<cmr:field path="ibmDeptCostCenter" id="ibmDeptCostCenter"
						fieldId="InternalDept" tabId="MAIN_IBM_TAB" />
				</p>
			</cmr:column>
			<%
			  if (reqentry.getReqType().equalsIgnoreCase("U")) {
			%>
			<cmr:view forGEO="MCO1,MCO2">
				<cmr:column span="2" containerForField="CollectionCd" exceptForCountry="780">
					<p>
						<cmr:label fieldId="collectionCd">
							<cmr:fieldLabel fieldId="CollectionCd" />: 
                  <cmr:delta text="${rdcdata.collectionCd}"
								oldValue="${reqentry.collectionCd}" id="delta-collectionCd" />
						</cmr:label>
						<cmr:field path="collectionCd" id="collectionCd"
							fieldId="CollectionCd" tabId="MAIN_IBM_TAB" />
					</p>
				</cmr:column>
			</cmr:view>
			<cmr:view forGEO="MCO1">
				<cmr:column span="2" containerForField="CollBranchOff">
					<p>
						<cmr:label fieldId="collBoId">
							<cmr:fieldLabel fieldId="CollBranchOff" />:
              <cmr:delta text="${rdcdata.collBoId}"
								oldValue="${reqentry.collBoId}" />
						</cmr:label>
						<cmr:field path="collBoId" id="collBoId" fieldId="CollBranchOff"
							tabId="MAIN_IBM_TAB" />
					</p>
				</cmr:column>
			</cmr:view>
			<%
			  }
			%>
		</cmr:row>
	</cmr:view>
	<cmr:view forCountry="838">
		<form:hidden path="engineeringBo" id="engineeringBo" />
	</cmr:view>
</cmr:view>