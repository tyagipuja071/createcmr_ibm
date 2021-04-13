<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
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
  AppUser user = AppUser.getUser(request);
  boolean noFindCMR = user.getAuthCode() == null;
  if (readOnly == null) {
    readOnly = false;
  }
  boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
%>

<cmr:view forGEO="TW">
 
 <cmr:row>
  <cmr:column span="2" containerForField="LocalTax1">
      <p>
        <label for="mktgDept"> 
          <cmr:fieldLabel fieldId="LocalTax1" />: 
          <cmr:delta text="${rdcdata.mktgDept}" oldValue="${reqentry.mktgDept}" />
        </label>
        <cmr:field path="mktgDept" id="mktgDept" fieldId="LocalTax1" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
      <cmr:column span="2" containerForField="LocalTax2">
        <p>
      <cmr:label fieldId="invoiceSplitCd">
            <cmr:fieldLabel fieldId="LocalTax2" />:
          </cmr:label>
          <cmr:field path="invoiceSplitCd" id="invoiceSplitCd" fieldId="LocalTax2" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="2" containerForField="CustAcctType">
        <p>
          <cmr:label fieldId="custAcctType">
            <cmr:fieldLabel fieldId="CustAcctType" />:
          </cmr:label>
          <cmr:field path="custAcctType" id="custAcctType" fieldId="CustAcctType" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
  
        <cmr:column span="2" containerForField="AbbrevLocation">
      <p>
        <label for="abbrevLocn"> <cmr:fieldLabel fieldId="AbbrevLocation" />: </label>
        <cmr:field fieldId="AbbrevLocation" id="abbrevLocn" path="abbrevLocn" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    
    <cmr:column span="2">
                  <cmr:buttonsRow>
                    <cmr:button  id="dnbSearchBtn" label="${ui.btn.dnbSrch}" onClick="doDnBSearch()" highlight="true" styleClass="cmr-reqentry-btn"/>
                    <cmr:info text="${ui.info.dnbSearch}" />
                    <%if (noFindCMR){%>
                    <img src="${resourcesPath}/images/warn-icon.png" class="cmr-warn-icon" title="${ui.info.nofindcmr}">
                    <%}%>
                  </cmr:buttonsRow>
              </cmr:column>
  </cmr:row>


  <cmr:row addBackground="false">
   <%--  <cmr:column span="2" containerForField="AbbrevLocation">
      <p>
        <label for="abbrevLocn"> <cmr:fieldLabel fieldId="AbbrevLocation" />: </label>
        <cmr:field fieldId="AbbrevLocation" id="abbrevLocn" path="abbrevLocn" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>--%>
    
    <cmr:column span="2" containerForField="OriginatorNo">
        <p>
          <cmr:label fieldId="orgNo">
            <cmr:fieldLabel fieldId="OriginatorNo" />:
          </cmr:label>
          <cmr:field path="orgNo" id="orgNo" fieldId="OriginatorNo" tabId="MAIN_IBM_TAB" />
        </p>
    </cmr:column>
    
    <cmr:column span="2" containerForField="RestrictTo" >
      <p>
        <label for="restrictTo"> 
          <cmr:fieldLabel fieldId="RestrictTo" />: 
          <cmr:delta text="${rdcdata.restrictTo}" oldValue="${reqentry.restrictTo}" code="L"/>
        </label>
        <cmr:field path="restrictTo" id="restrictTo" fieldId="RestrictTo" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    
    <cmr:column span="2" containerForField="CommercialFinanced" >
        <p>
          <cmr:label fieldId="commercialFinanced">
            <cmr:fieldLabel fieldId="CommercialFinanced" />:
              <cmr:delta text="${rdcdata.commercialFinanced}" oldValue="${reqentry.commercialFinanced}" />
          </cmr:label>
          <cmr:field path="commercialFinanced" id="commercialFinanced" fieldId="CommercialFinanced" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>

  <%--  <cmr:column span="2" containerForField="CollectionCd">
      <p>
        <cmr:label fieldId="collectionCd">
          <cmr:fieldLabel fieldId="CollectionCd" />: 
              <cmr:delta text="${rdcdata.collectionCd}" oldValue="${reqentry.collectionCd}" id="delta-collectionCd" />
        </cmr:label>
        <cmr:field path="collectionCd" id="collectionCd" fieldId="CollectionCd" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
     <cmr:column span="2" containerForField="LocalTax2">
        <p>
          <label for="taxCd2"> <cmr:fieldLabel fieldId="LocalTax2" />:
           <cmr:delta text="${rdcdata.taxCd2}" oldValue="${reqentry.taxCd2}" /> </label>
          <cmr:field path="taxCd2" id="taxCd2" fieldId="LocalTax2" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column> --%>
  </cmr:row>
  <cmr:row addBackground="false">
    <cmr:column span="2" containerForField="CSBOCd">
        <p>
      <cmr:label fieldId="csBo">
            <cmr:fieldLabel fieldId="CSBOCd" />:
          </cmr:label>
          <cmr:field path="csBo" id="csBo" fieldId="CSBOCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column> 
     
     <cmr:column span="2" containerForField="FootnoteTxt1">
      <p>
        <label for="footnoteTxt1"> <cmr:fieldLabel fieldId="FootnoteTxt1" />: 
        </label>
        <cmr:field fieldId="FootnoteTxt1" id="footnoteTxt1" path="footnoteTxt1" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
     
      <cmr:column span="2" containerForField="ExportCodesTDOdate">
      <p>
        <label for="nuclChecklstDate"> <cmr:fieldLabel fieldId="ExportCodesTDOdate" />: 
        </label>
        <cmr:field fieldId="ExportCodesTDOdate" id="bioChemMissleMfg" path="bioChemMissleMfg" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  
    <%--<cmr:column span="2" containerForField="EmbargoCode">
      <p>
        <cmr:label fieldId="embargoCd">
          <cmr:fieldLabel fieldId="EmbargoCode" />:
            <cmr:delta text="${rdcdata.embargoCd}" oldValue="${reqentry.embargoCd}" />
        </cmr:label>
        <cmr:field path="embargoCd" id="embargoCd" fieldId="EmbargoCode" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="EconomicCd2">
        <p>
          <cmr:label fieldId="economicCd">
            <cmr:fieldLabel fieldId="EconomicCd2" />:
           <cmr:delta text="${rdcdata.economicCd}" oldValue="${reqentry.economicCd}" code="R" />
          </cmr:label>
          <cmr:field path="economicCd" id="economicCd" fieldId="EconomicCd2" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>

    <cmr:column span="2" containerForField="InternalDept">
        <p>
          <cmr:label fieldId="ibmDeptCostCenter">
            <cmr:fieldLabel fieldId="InternalDept" />: 
            <cmr:delta text="${rdcdata.ibmDeptCostCenter}" oldValue="${reqentry.ibmDeptCostCenter}" />
          </cmr:label>
          <cmr:field path="ibmDeptCostCenter" id="ibmDeptCostCenter" fieldId="InternalDept" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>--%>
    
  </cmr:row>
    <cmr:row  addBackground="false">
      <cmr:column span="2" containerForField="ContactName2">
        <p>
          <label for="contactName2"> <cmr:fieldLabel fieldId="ContactName2" />: </label>
          <cmr:field fieldId="ContactName2" id="contactName2" path="contactName2" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      
      <cmr:column span="2" containerForField="Email1">
      <p>
        <label for="email1"> <cmr:fieldLabel fieldId="Email1" />: </label>
        <cmr:field fieldId="Email1" id="email1" path="email1" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    
    <cmr:column span="2" containerForField="ContactName1">
      <p>
        <label for="contactName1"> <cmr:fieldLabel fieldId="ContactName1" />: </label>
        <cmr:field fieldId="ContactName1" id="contactName1" path="contactName1" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    
     <%-- <c:if test="${reqentry.reqType == 'U'}">
		<cmr:column span="2" containerForField="ModeOfPayment">
		 <p>
			<cmr:label fieldId="modeOfPayment">
				<cmr:fieldLabel fieldId="ModeOfPayment" />: 
			</cmr:label>
			<cmr:field path="paymentMode" id="modeOfPayment" fieldId="ModeOfPayment" tabId="MAIN_CUST_TAB" />		 
		 </p>
		</cmr:column>
	  </c:if>--%>
	  
    </cmr:row>
    
    <cmr:row>
    <cmr:column span="2" containerForField="Sector">
      <p>
        <cmr:label fieldId="sectorCd"> <cmr:fieldLabel fieldId="Sector" />: </cmr:label>
        <cmr:field path="sectorCd" id="sectorCd" fieldId="Sector" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    
    <cmr:column span="2" containerForField="FootnoteTxt2">
      <p>
        <label for="footnoteTxt2"> <cmr:fieldLabel fieldId="FootnoteTxt2" />: 
        </label>
        <cmr:field fieldId="FootnoteTxt2" id="footnoteTxt2" path="footnoteTxt2" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    
    <cmr:column span="2" containerForField="ContactName3">
        <p>
          <label for="contactName3"> <cmr:fieldLabel fieldId="ContactName3" />: </label>
          <cmr:field fieldId="ContactName3" id="contactName3" path="contactName3" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
   </cmr:row>
   
   <cmr:row>
    <cmr:column span="2" containerForField="BPName">
      <p>
        <label for="bpName"> 
          <cmr:fieldLabel fieldId="BPName" />: 
          <cmr:delta text="${rdcdata.bpName}" oldValue="${reqentry.bpName}" code="L"/>
        </label>
        <cmr:field path="bpName" id="bpName" fieldId="BPName" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    
    <cmr:column span="2" containerForField="Email2">
        <p>
          <label for="email2"> <cmr:fieldLabel fieldId="Email2" />: </label>
          <cmr:field fieldId="Email2" id="email2" path="email2" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    <cmr:column span="2" containerForField="BusnType">
      <p>
        <cmr:label fieldId="busnType">
          <cmr:fieldLabel fieldId="BusnType" />: 
         </cmr:label>
        <cmr:field path="busnType" id="busnType" fieldId="BusnType" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
      
   </cmr:row>
   
  <cmr:row>
   <cmr:column span="2" containerForField="Affiliate" exceptForCountry="666,726,862,822,838,641" exceptForGEO="MCO1,MCO2,CEMEA,NORDX,BELUX,NL">
        <p>
          <cmr:label fieldId="affiliate">
            <cmr:fieldLabel fieldId="Affiliate" />: 
              <cmr:delta text="${rdcdata.affiliate}" oldValue="${reqentry.affiliate}" />
          </cmr:label>
          <cmr:field fieldId="Affiliate" id="affiliate" path="affiliate" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      
      <cmr:column span="2" containerForField="Email3">
        <p>
          <label for="email3"> <cmr:fieldLabel fieldId="Email3" />: </label>
          <cmr:field fieldId="Email3" id="email3" path="email3" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
  </cmr:row>
</cmr:view>

