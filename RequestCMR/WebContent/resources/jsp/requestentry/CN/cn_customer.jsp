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

<cmr:view forCountry="641">
<cmr:row addBackground="true">
    <cmr:column span="2" containerForField="PrivacyIndc">
      <p>
        <label for="privIndc"> <cmr:fieldLabel fieldId="PrivacyIndc" />: 
        </label>
        <cmr:field fieldId="PrivacyIndc" id="privIndc" path="privIndc" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="SocialCreditCd">
    <p>
      <cmr:label fieldId="busnType">
        <cmr:fieldLabel fieldId="SocialCreditCd" />:
             <cmr:delta text="-" id="delta-busnType" />
      </cmr:label>
      <cmr:field fieldId="SocialCreditCd" id="busnType" path="busnType" />
    </p>
  </cmr:column>
  </cmr:row>
  
  <cmr:row>
      <cmr:column span="2" containerForField="RdcComment">
    <p>
      <cmr:label fieldId="rdcComment">
        <cmr:fieldLabel fieldId="RdcComment" />:
             <cmr:delta text="-" id="delta-rdcComment" />
      </cmr:label>
      <cmr:field fieldId="RdcComment" id="rdcComment" path="rdcComment" />
    </p>
  </cmr:column>
  </cmr:row>
  
  <cmr:row>
  <cmr:column span="2" containerForField="ExportCodesCountry">
      <p>
        <label for="custAcctType"> <cmr:fieldLabel fieldId="ExportCodesCountry" />: 
        </label>
        <cmr:field fieldId="ExportCodesCountry" id="custAcctType" path="custAcctType" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  <cmr:column span="2" containerForField="ExportCodesTDOdate">
      <p>
        <label for="nuclChecklstDate"> <cmr:fieldLabel fieldId="ExportCodesTDOdate" />: 
        </label>
        <cmr:field fieldId="ExportCodesTDOdate" id="bioChemMissleMfg" path="bioChemMissleMfg" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  <cmr:column span="2" containerForField="ExportCodesTDOIndicator">
      <p>
        <label for="icmsInd"> <cmr:fieldLabel fieldId="ExportCodesTDOIndicator" />: 
        </label>
        <cmr:field fieldId="ExportCodesTDOIndicator" id="icmsInd" path="icmsInd" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>    

  </cmr:row>
 
</cmr:view>
