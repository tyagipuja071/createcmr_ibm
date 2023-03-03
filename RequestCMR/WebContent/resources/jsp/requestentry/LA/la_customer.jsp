<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
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
  boolean laReactivateCapable = PageManager.laReactivateEnabled(reqentry.getCmrIssuingCntry(), reqentry.getReqType());
%>

<cmr:view forGEO="LA">
  <cmr:row addBackground="false">
  
    <cmr:column span="2" containerForField="ContactName1"
    exceptForCountry="631,613,629,655,661,663,681,683,829,731,735,781,799,811,813,815,869,871">
      <p>
        <label for="contactName1"> <cmr:fieldLabel fieldId="ContactName1" />: </label>
        <cmr:field fieldId="ContactName1" id="contactName1" path="contactName1" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>


    <cmr:column span="2" containerForField="Phone1"
      exceptForCountry="631,613,629,655,661,663,681,683,829,731,735,781,799,811,813,815,869,871">
      <p>
        <label for="phone1"> <cmr:fieldLabel fieldId="Phone1" />: </label>
        <cmr:field fieldId="Phone1" id="phone1" path="phone1" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>

    <cmr:column span="2" containerForField="Email1" forCountry="631,815">
      <p>
        <label for="email1"> <cmr:fieldLabel fieldId="Email1" />: </label>
        <cmr:field fieldId="Email1" id="email1" path="email1" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>


    <cmr:row addBackground="false">
      <cmr:column span="2" containerForField="ContactName2"
       exceptForCountry="631,613,629,655,661,663,681,683,829,731,735,781,799,811,813,815,869,871">
        <p>
          <label for="contactName2"> <cmr:fieldLabel fieldId="ContactName2" />: </label>
          <cmr:field fieldId="ContactName2" id="contactName2" path="contactName2" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>


      <cmr:column span="2" containerForField="Phone2"
       exceptForCountry="631,613,629,655,661,663,681,683,829,731,735,781,799,811,813,815,869,871">
        <p>
          <label for="phone2"> <cmr:fieldLabel fieldId="Phone2" />: </label>
          <cmr:field fieldId="Phone2" id="phone2" path="phone2" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>


      <cmr:column span="2" containerForField="Email2"
        exceptForCountry="631,613,629,655,661,663,681,683,829,731,735,781,799,811,813,815,869,871">
        <p>
          <label for="email2"> <cmr:fieldLabel fieldId="Email2" />: </label>
          <cmr:field fieldId="Email2" id="email2" path="email2" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>

    <cmr:row addBackground="false">
      <cmr:column span="2" containerForField="ContactName3"
        exceptForCountry="631,613,629,655,661,663,681,683,829,731,735,781,799,811,813,815,869,871">
        <p>
          <label for="contactName3"> <cmr:fieldLabel fieldId="ContactName3" />: </label>
          <cmr:field fieldId="ContactName3" id="contactName3" path="contactName3" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>


      <cmr:column span="2" containerForField="Phone3"
       exceptForCountry="631,613,629,655,661,663,681,683,829,731,735,781,799,811,813,815,869,871">
        <p>

          <label for="phone3"> <cmr:fieldLabel fieldId="Phone3" />: </label>
          <cmr:field fieldId="Phone3" id="phone3" path="phone3" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>


      <cmr:column span="2" containerForField="Email3"
        exceptForCountry="631,613,629,655,661,663,681,683,829,731,735,781,799,811,813,815,869,871">
        <p>
          <label for="email3"> <cmr:fieldLabel fieldId="Email3" />: </label>
          <cmr:field fieldId="Email3" id="email3" path="email3" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>


  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="BusinessType" forCountry="655">
      <p>
        <cmr:label fieldId="busnType">
          <cmr:fieldLabel fieldId="BusinessType" />: 
              </cmr:label>
        <cmr:field path="busnType" id="busnType" fieldId="BusinessType" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="ICMSContribution" forCountry="631">
      <p>
        <cmr:label fieldId="icmsInd">
          <cmr:fieldLabel fieldId="ICMSContribution" />:
              </cmr:label>
        <cmr:field id="icmsInd" path="icmsInd" fieldId="ICMSContribution" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="BillingName" forCountry="781">
      <p>
        <cmr:label fieldId="mexicoBillingName">
          <cmr:fieldLabel fieldId="BillingName" />:
          <cmr:delta text="${rdcdata.mexicoBillingName}" oldValue="${reqentry.mexicoBillingName}" />
        </cmr:label>
        <cmr:field path="mexicoBillingName" id="mexicoBillingName" fieldId="BillingName" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row addBackground="true">
    <cmr:view forCountry="869">
      <cmr:column span="2" containerForField="IBMBankNumber">
        <p>
          <label for="ibmBankNumber">
            <cmr:fieldLabel fieldId="IBMBankNumber" />:
          </label>
          <cmr:field path="ibmBankNumber" id="ibmBankNumber" fieldId="IBMBankNumber" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>
  </cmr:row>

  <c:if test="${reqentry.reqType != 'U'}">
    <cmr:row addBackground="true">
      <cmr:column span="2" containerForField="GovernmentType" forCountry="631">
        <p>
          <cmr:label fieldId="govType">
            <cmr:fieldLabel fieldId="GovernmentType" />:
                <cmr:delta text="${rdcdata.govType}" oldValue="${reqentry.govType}" />
          </cmr:label>
          <cmr:field id="govType" path="govType" fieldId="GovernmentType" tabId="MAIN_CUST_TAB" size="350" />
        </p>
      </cmr:column>
    </cmr:row>
  </c:if>
  
  <%if (laReactivateCapable && "93".equals(reqentry.getOrdBlk())){%>
  <cmr:row addBackground="true">
      <cmr:column span="2" containerForField="GovernmentType" forCountry="631">
        <p>
          <cmr:label fieldId="govType">
            <cmr:fieldLabel fieldId="GovernmentType" />:
                <cmr:delta text="${rdcdata.govType}" oldValue="${reqentry.govType}" />
          </cmr:label>
          <cmr:field id="govType" path="govType" fieldId="GovernmentType" tabId="MAIN_CUST_TAB" size="350" />
        </p>
      </cmr:column>
    </cmr:row>
  
  <%}%>
  
  <%if ("Y".equals(reqentry.getEmbargoCd())) {%>
  <cmr:row addBackground="false">
      <cmr:column span="1" containerForField="DPLBlocked">
        <p>
          <cmr:label fieldId="denialCusInd">
            <cmr:fieldLabel fieldId="DPLBlocked" />:
                <cmr:delta text="${reqentry.denialCusInd}" oldValue="${reqentry.denialCusInd}" />
          </cmr:label>
          <cmr:field id="denialCusInd" path="denialCusInd" fieldId="DPLBlocked" tabId="MAIN_CUST_TAB"/>
        </p>
      </cmr:column>
    </cmr:row>
  
  <%} %>
</cmr:view>
