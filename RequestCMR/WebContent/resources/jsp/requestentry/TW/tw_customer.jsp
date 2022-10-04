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
    <!-- No5 -->
   <cmr:row addBackground="true">
     <cmr:column span="2" containerForField="FootnoteTxt1">
      <p>
        <label for="footnoteTxt1"> <cmr:fieldLabel fieldId="FootnoteTxt1" />: 
        </label>
        <cmr:field fieldId="FootnoteTxt1" id="footnoteTxt1" path="footnoteTxt1" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="ContactName3">
        <p>
          <label for="contactName3"> <cmr:fieldLabel fieldId="ContactName3" />: </label>
          <cmr:field fieldId="ContactName3" id="contactName3" path="contactName3" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="Email3">
        <p>
          <label for="email3"> <cmr:fieldLabel fieldId="Email3" />: </label>
          <cmr:field fieldId="Email3" id="email3" path="email3" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
  </cmr:row>
    <!-- No6 -->
   <cmr:row addBackground="true">
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
  </cmr:row>
</cmr:view>
