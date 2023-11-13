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

<cmr:view forGEO="JP">
  <cmr:row addBackground="false">
    <cmr:column span="4" containerForField="AbbrevLocation">
      <p>
        <label for="email2"> 
          <cmr:fieldLabel fieldId="AbbrevLocation" />: 
        </label>
        <cmr:field fieldId="AbbrevLocation" id="email2" path="email2" size="570" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="IBMRelatedCMR">
      <p>
        <label for="proxiLocnNo" > 
          <cmr:fieldLabel fieldId="IBMRelatedCMR" />: 
        </label>
        <cmr:field fieldId="IBMRelatedCMR" id="proxiLocnNo" path="proxiLocnNo" tabId="MAIN_CUST_TAB" />
        <%if (!readOnly){%>
        	<img title="Import the CMR" id="btnIbmRelateCmr" class="cmr-proceed2-icon" src="${resourcesPath}/images/search2.png" onclick="findImportCMR()" />
        <%}%>
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="OEMInd">
        <p>
          <cmr:label fieldId="oemInd">
            <cmr:fieldLabel fieldId="OEMInd" />:
          </cmr:label>
          <cmr:field path="oemInd" id="oemInd" fieldId="OEMInd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    <cmr:column span="2" containerForField="LeasingCompIndc">
        <p>
          <cmr:label fieldId="leasingCompanyIndc">
            <cmr:fieldLabel fieldId="LeasingCompIndc" />:
          </cmr:label>
          <cmr:field path="leasingCompanyIndc" id="leasingCompanyIndc" fieldId="LeasingCompIndc" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    <cmr:column span="2" containerForField="EducationAllowance">
        <p>
          <cmr:label fieldId="educAllowCd">
            <cmr:fieldLabel fieldId="EducationAllowance" />:
          </cmr:label>
          <cmr:field path="educAllowCd" id="educAllowCd" fieldId="EducationAllowance" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="CustAcctType">
        <p>
          <cmr:label fieldId="custAcctType">
            <cmr:fieldLabel fieldId="CustAcctType" />:
          </cmr:label>
          <cmr:field path="custAcctType" id="custAcctType" fieldId="CustAcctType" tabId="MAIN_CUST_TAB" />
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
    <cmr:row>
      <cmr:column span="2" containerForField="IinInd">
        <p>
          <cmr:label fieldId="iinInd">
            <cmr:fieldLabel fieldId="IinInd" />:
          </cmr:label>
          <cmr:field path="iinInd" id="iinInd" fieldId="IinInd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="ValueAddRem">
        <p>
          <cmr:label fieldId="valueAddRem">
            <cmr:fieldLabel fieldId="ValueAddRem" />:
          </cmr:label>
          <cmr:field path="valueAddRem" id="valueAddRem" fieldId="ValueAddRem" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="ChannelCd">
        <p>
          <cmr:label fieldId="channelCd">
            <cmr:fieldLabel fieldId="ChannelCd" />:
          </cmr:label>
          <cmr:field path="channelCd" id="channelCd" fieldId="ChannelCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="2" containerForField="SiInd">
        <p>
          <cmr:label fieldId="siInd">
            <cmr:fieldLabel fieldId="SiInd" />:
          </cmr:label>
          <cmr:field path="siInd" id="siInd" fieldId="SiInd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="CrsCd">
        <p>
          <cmr:label fieldId="crsCd">
            <cmr:fieldLabel fieldId="CrsCd" />:
          </cmr:label>
          <cmr:field path="crsCd" id="crsCd" fieldId="CrsCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="CreditCd">
        <p>
          <cmr:label fieldId="creditCd">
            <cmr:fieldLabel fieldId="CreditCd" />:
          </cmr:label>
          <cmr:field path="creditCd" id="creditCd" fieldId="CreditCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="Government">
        <p>
          <cmr:label fieldId="govType">
            <cmr:fieldLabel fieldId="Government" />:
          </cmr:label>
          <cmr:field path="govType" id="govType" fieldId="Government" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="2" containerForField="OutsourcingServ">
        <p>
          <cmr:label fieldId="outsourcingService">
            <cmr:fieldLabel fieldId="OutsourcingServ" />:
          </cmr:label>
          <cmr:field path="outsourcingService" id="outsourcingService" fieldId="OutsourcingServ" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="DirectBp">
        <p>
          <cmr:label fieldId="creditBp">
            <cmr:fieldLabel fieldId="DirectBp" />:
          </cmr:label>
          <cmr:field path="creditBp" id="creditBp" fieldId="DirectBp" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="zSeriesSw">
        <p>
          <cmr:label fieldId="zseriesSw">
            <cmr:fieldLabel fieldId="zSeriesSw" />:
          </cmr:label>
          <cmr:field path="zseriesSw" id="zseriesSw" fieldId="zSeriesSw" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
</cmr:view>