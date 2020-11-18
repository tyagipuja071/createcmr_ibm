<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
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

<cmr:view forCountry="649">
  <%if ("U".equals(reqentry.getReqType())){ %>
  <cmr:row addBackground="true" topPad="20">
     <cmr:column span="6">
       <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
       <cmr:note text="For updates to any of the fields below, leave the fields below blank if you will not be changing the current values." />
     </cmr:column>
  </cmr:row>
  <%} %>

  <cmr:row>
  	<cmr:column span="2">
  		<p>
  			<cmr:label fieldId="ProfileNo">
        		<cmr:fieldLabel fieldId="ProfileNo" />:
          	</cmr:label>
        	<cmr:field path="modelCmrNo" id="modelCmrNo" fieldId="ProfileNo" tabId="MAIN_CUST_TAB" />	
  		</p>
  	</cmr:column>
  </cmr:row>
  
  <cmr:row addBackground="true" >
    <cmr:column span="2" containerForField="LocalTax1">
      <p>
        <label for="taxCd1"> 
          <cmr:fieldLabel fieldId="LocalTax1" />: 
          <cmr:delta text="${rdcdata.taxCd1}" oldValue="${reqentry.taxCd1}" />
        </label>
        <cmr:field path="taxCd1" id="taxCd1" fieldId="LocalTax1" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="PPSCEID">
      <p>
        <cmr:label fieldId="ppsceid">
          <cmr:fieldLabel fieldId="PPSCEID" />:
          <cmr:delta text="${rdcdata.ppsceid}" oldValue="${reqentry.ppsceid}"/>
        </cmr:label>
        <cmr:field fieldId="PPSCEID" id="ppsceid" path="ppsceid" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="VADNumber">
      <p>
        <label for="taxCd2"> 
          <cmr:fieldLabel fieldId="VADNumber" />: 
          <cmr:delta text="${rdcdata.taxCd2}" oldValue="${reqentry.taxCd1}" />
        </label>
        <cmr:field path="taxCd2" id="taxCd2" fieldId="VADNumber" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="SalesBusOff">
      <p>
        <label for="salesBusOffCd"> 
          <cmr:fieldLabel fieldId="SalesBusOff" />: 
          <cmr:delta text="${rdcdata.salesBusOffCd}" oldValue="${reqentry.salesBusOffCd}" />
        </label>
        <cmr:field path="salesBusOffCd" id="salesBusOffCd" fieldId="SalesBusOff" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="InstallBranchOff">
      <p>
        <label for="installBranchOff"> 
          <cmr:fieldLabel fieldId="InstallBranchOff" />: 
          <cmr:delta text="${rdcdata.installBranchOff}" oldValue="${reqentry.installBranchOff}" />
        </label>
        <cmr:field path="installBranchOff" id="installBranchOff" fieldId="InstallBranchOff" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="SalesSR">
      <p>
        <label for="repTeamMemberNo"> 
          <cmr:fieldLabel fieldId="SalesSR" />: 
          <cmr:delta text="${rdcdata.repTeamMemberNo}" oldValue="${reqentry.repTeamMemberNo}" />
        </label>
        <cmr:field path="repTeamMemberNo" id="repTeamMemberNo" fieldId="SalesSR" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
  
  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="CsBo">
      <p>
        <label for="salesTeamCd"> 
          <cmr:fieldLabel fieldId="CsBo" />: 
          <cmr:delta text="${rdcdata.salesTeamCd}" oldValue="${reqentry.salesTeamCd}" />
        </label>
        <cmr:field path="salesTeamCd" id="salesTeamCd" fieldId="CsBo" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="DistMktgBranch">
      <p>
        <label for="invoiceDistCd"> 
          <cmr:fieldLabel fieldId="DistMktgBranch" />: 
          <%-- uncomment after DM changes to RDC_DATA 
          <cmr:delta text="${rdcdata.mtkgArDept}" oldValue="${reqentry.invoiceDistCd}" />
          --%>
        </label>
        <cmr:field path="invoiceDistCd" id="invoiceDistCd" fieldId="DistMktgBranch" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="MarketingARDept">
      <p>
        <label for="adminDeptCd"> 
          <cmr:fieldLabel fieldId="MarketingARDept" />: 
          <%-- uncomment after DM changes or field remap 
          <cmr:delta text="${rdcdata.adminDeptCd}" oldValue="${reqentry.adminDeptCd}" />
          --%>
        </label>
        <cmr:field path="adminDeptCd" id="adminDeptCd" fieldId="MarketingARDept" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>

 <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="CreditCd">
      <p>
        <label for="creditCd"> 
          <cmr:fieldLabel fieldId="CreditCd" />: 
          <cmr:delta text="${rdcdata.creditCd}" oldValue="${reqentry.creditCd}" />
        </label>
        <cmr:field path="creditCd" id="creditCd" fieldId="CreditCd" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="BillingProcCd">
      <p>
        <label for="collectorNameNo"> 
          <cmr:fieldLabel fieldId="BillingProcCd" />: 
          <cmr:delta text="${rdcdata.collectorNo}" oldValue="${reqentry.collectorNameNo}" />
        </label>
        <cmr:field path="collectorNameNo" id="collectorNameNo" fieldId="BillingProcCd" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="CustomerData">
      <p>
        <label for="taxCd3"> 
          <cmr:fieldLabel fieldId="CustomerData" />: 
          <%-- uncomment after DM changes or remap field
          <cmr:delta text="${rdcdata.taxCd3}" oldValue="${reqentry.taxCd3}" />
          --%>
        </label>
        <cmr:field path="taxCd3" id="taxCd3" fieldId="CustomerData" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="LocationCode">
      <p>
        <label for="locationNumber"> 
          <cmr:fieldLabel fieldId="LocationCode" />: 
          <cmr:delta text="${rdcdata.locationNumber}" oldValue="${reqentry.locationNumber}" />
        </label>
        <cmr:field path="locationNumber" id="locationNumber" fieldId="LocationCode" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="SizeCode">
      <p>
        <label for="orgNo"> 
          <cmr:fieldLabel fieldId="SizeCode" />: 
          <%-- uncomment after DM changes or field remap 
          <cmr:delta text="${rdcdata.orgNo}" oldValue="${reqentry.orgNo}" />
          --%>
        </label>
        <cmr:field path="orgNo" id="orgNo" fieldId="SizeCode" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="InvoiceSplitCd">
      <p>
        <label for="cusInvoiceCopies"> 
          <cmr:fieldLabel fieldId="InvoiceSplitCd" />: 
          <%-- uncomment after DM changes or field remap 
          <cmr:delta text="${rdcdata.invoiceSplitCd}" oldValue="${reqentry.invoiceSplitCd}" />
          --%>
        </label>
        <cmr:field path="cusInvoiceCopies" id="cusInvoiceCopies" fieldId="InvoiceSplitCd" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>



  
</cmr:view>