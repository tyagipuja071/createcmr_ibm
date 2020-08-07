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

<cmr:view forGEO="CEMEA">
  <cmr:row topPad="10">
   <% if (reqentry.getCmrIssuingCntry().equalsIgnoreCase("618") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("603")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("607") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("626")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("644") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("651")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("668") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("693")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("694") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("695")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("699") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("704")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("705") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("707")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("708") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("740")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("741") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("787")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("820") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("821")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("826") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("889")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("358") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("359")
|| reqentry.getCmrIssuingCntry().equalsIgnoreCase("363")){ %>
      <form:hidden path="repTeamMemberNo" id="repTeamMemberNo"/>
    <%} else { %>
    <cmr:column span="2" containerForField="SalRepNameNo">
      <p>
        <cmr:label fieldId="repTeamMemberNo">
          <cmr:fieldLabel fieldId="SalRepNameNo" />:
           <cmr:delta text="${rdcdata.repTeamMemberNo}" oldValue="${reqentry.repTeamMemberNo}" id="delta-repTeamMemberNo" />
        </cmr:label>
        <cmr:field fieldId="SalRepNameNo" id="repTeamMemberNo" path="repTeamMemberNo" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column><%} %>

    <cmr:column span="2" containerForField="EngineeringBo" exceptForCountry="618">
      <p>
        <cmr:label fieldId="engineeringBo">
          <cmr:fieldLabel fieldId="EngineeringBo" />:
           <cmr:delta text="${rdcdata.engineeringBo}" oldValue="${reqentry.engineeringBo}" id="delta-engineeringBo" />
        </cmr:label>
        <cmr:field fieldId="EngineeringBo" id="engineeringBo" path="engineeringBo" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>

<!-- CEEME - show SBO for processors only -->
    <%if (!reqentry.getCmrIssuingCntry().equalsIgnoreCase("603") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("363") 
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("607") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("626")
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("644") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("651")
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("668") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("693")
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("694") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("695")
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("699") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("704")
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("705") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("707")
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("708") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("740")
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("741") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("787")
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("820") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("821")
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("826") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("889")
&& !reqentry.getCmrIssuingCntry().equalsIgnoreCase("358") && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("359")
&& (!reqentry.getCmrIssuingCntry().equalsIgnoreCase("618") && !"Processor".equalsIgnoreCase(reqentry.getUserRole()))){%>
      <form:hidden path="salesBusOffCd" id="salesBusOffCd"/>
    <%} else { %>
      <cmr:column span="2" containerForField="SalesBusOff">
        <p>
          <cmr:label fieldId="salesBusOffCd">
            <cmr:fieldLabel fieldId="SalesBusOff" />:
          </cmr:label>
          <cmr:field fieldId="SalesBusOff" id="salesBusOffCd" path="salesBusOffCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>      
    <%} %>
  </cmr:row>
  <cmr:view forCountry="644,668,693,704,708,740,820,821,826,358,359,363,603,607,626,651,694,695,699,705,707,787,741,889">
  	<cmr:row topPad="10">
  		<cmr:column span="2" containerForField="LocalTax2">
	      <p>
	        <cmr:label fieldId="taxCd2">
	          <cmr:fieldLabel fieldId="LocalTax2" />: 
	            <cmr:delta text="${rdcdata.taxCd2}" oldValue="${reqentry.taxCd2}" />
	        </cmr:label>
	        <cmr:field fieldId="LocalTax2" id="taxCd2" path="taxCd2" tabId="MAIN_IBM_TAB" />
	      </p>
	    </cmr:column>
  	</cmr:row>
  </cmr:view>
  <!-- CIS Duplicate CMR -->
  <cmr:view forCountry="821">
  <cmr:row topPad="10">
  <!--CMR-4606 Add for Russia CIS Dup -->
    <cmr:column span="2" containerForField="LocalTax3">
      <p>
        <cmr:label fieldId="taxCd3">
          <cmr:fieldLabel fieldId="LocalTax3" />:
        </cmr:label>
        <cmr:field fieldId="LocalTax3" id="taxCd3" path="taxCd3" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    
    <cmr:column span="2" containerForField="SalRepNameNo2">
      <p>
        <cmr:label fieldId="dupSalesRepNo">
          <cmr:fieldLabel fieldId="SalRepNameNo2" />:
        </cmr:label>
        <cmr:field fieldId="SalRepNameNo2" id="dupSalesRepNo" path="dupSalesRepNo" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  <!-- Mark for del CMR-4606  
    <cmr:column span="2">
    </cmr:column>
    <cmr:column span="2" containerForField="SalesBusOff2">
      <p>
        <cmr:label fieldId="dupSalesBoCd">
          <cmr:fieldLabel fieldId="SalesBusOff2" />:
        </cmr:label>
        <cmr:field fieldId="SalesBusOff2" id="dupSalesBoCd" path="dupSalesBoCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>  -->
  </cmr:row>
  </cmr:view>

  <cmr:view exceptForCountry="618">
    <cmr:row addBackground="true">
      <%
        if (reqentry.getReqType().equalsIgnoreCase("U")) {
      %>
      <cmr:column span="2" containerForField="CollectionCd">
        <p>
          <cmr:label fieldId="collectionCd">
            <cmr:fieldLabel fieldId="CollectionCd" />: 
              <cmr:delta text="${rdcdata.collectionCd}" oldValue="${reqentry.collectionCd}" id="delta-collectionCd" />
          </cmr:label>
          <cmr:field path="collectionCd" id="collectionCd" fieldId="CollectionCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" exceptForCountry="620,642,675,677,680,752,762,762,767,768,772,805,808,808,823,832,849,850,865,644,668,693,704,708,740,820,826,358,359,363,603,607,626,651,694,695,699,705,707,787,741,889">
        <p>
          <cmr:label fieldId="agreementSignDate">
            <cmr:fieldLabel fieldId="AECISubDate" />:
           	  <cmr:view forCountry="821">
		        <cmr:info text="${ui.info.aeciSubDateRussia}" />
		      </cmr:view> 
          </cmr:label>
          <cmr:field path="agreementSignDate" id="agreementSignDate" fieldId="AECISubDate" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <%
        }
      %>
    </cmr:row>
  </cmr:view>

  <cmr:view forCountry="618">
    <cmr:row>
      <%
        if (reqentry.getReqType().equalsIgnoreCase("U")) {
      %>
      <cmr:column span="2" containerForField="CreditCd">
        <p>
          <cmr:label fieldId="creditCd">
            <cmr:fieldLabel fieldId="CreditCd" />: 
          </cmr:label>
          <cmr:field path="creditCd" id="creditCd" fieldId="CreditCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <%
        }
      %>
      <cmr:column span="2" containerForField="LocationNumber">
        <p>
          <cmr:label fieldId="locationNumber">
            <cmr:fieldLabel fieldId="LocationNumber" />: 
          </cmr:label>
          <cmr:field path="locationNo" id="locationNumber" fieldId="LocationNumber" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="CurrencyCode">
        <p>
          <cmr:label fieldId="legacyCurrencyCd">
            <cmr:fieldLabel fieldId="CurrencyCd" />: 
              <cmr:delta text="${rdcdata.legacyCurrencyCd}" oldValue="${reqentry.legacyCurrencyCd}" id="delta-legacyCurrencyCd" />
          </cmr:label>
          <cmr:field path="legacyCurrencyCd" id="legacyCurrencyCd" fieldId="CurrencyCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
  </cmr:view>

</cmr:view>