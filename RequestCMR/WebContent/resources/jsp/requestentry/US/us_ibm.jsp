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

<cmr:view forCountry="897">
  <cmr:row>
    <cmr:column span="2" containerForField="MarketingDept">
      <p>
        <label for="mktgDept"> 
          <cmr:fieldLabel fieldId="MarketingDept" />: 
          <cmr:delta text="${rdcdata.mktgDept}" oldValue="${reqentry.mktgDept}" />
        </label>
        <cmr:field path="mktgDept" id="mktgDept" fieldId="MarketingDept" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="MarketingARDept">
      <p>
        <label for="mtkgArDept"> 
          <cmr:fieldLabel fieldId="MarketingARDept" />: 
          <cmr:delta text="${rdcdata.mtkgArDept}" oldValue="${reqentry.mtkgArDept}" />
        </label>
        <cmr:field path="mtkgArDept" id="mtkgArDept" fieldId="MarketingARDept" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <cmr:column span="2" containerForField="PCCMarketingDept">
      <p>
        <label for="pccMktgDept"> 
          <cmr:fieldLabel fieldId="PCCMarketingDept" />: 
          <cmr:delta text="${rdcdata.pccMktgDept}" oldValue="${reqentry.pccMktgDept}"/>
        </label>
        <cmr:field path="pccMktgDept" id="pccMktgDept" fieldId="PCCMarketingDept" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="PCCARDept">
      <p>
        <label for="pccArDept"> 
          <cmr:fieldLabel fieldId="PCCARDept" />: 
          <cmr:delta text="${rdcdata.pccArDept}" oldValue="${reqentry.pccArDept}"/>
        </label>
        <cmr:field path="pccArDept" id="pccArDept" fieldId="PCCARDept" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <cmr:column span="2" containerForField="SVCTerritoryZone">
      <p>
        <label for="svcTerritoryZone"> 
          <cmr:fieldLabel fieldId="SVCTerritoryZone" />: 
          <cmr:delta text="${rdcdata.svcTerritoryZone}" oldValue="${reqentry.svcTerritoryZone}"/>
        </label>
        <cmr:field path="svcTerritoryZone" id="svcTerritoryZone" fieldId="SVCTerritoryZone" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="SVCAROffice">
      <p>
        <label for="svcArOffice"> 
          <cmr:fieldLabel fieldId="SVCAROffice" />: 
          <cmr:delta text="${rdcdata.svcArOffice}" oldValue="${reqentry.svcArOffice}"/>
        </label>
        <cmr:field path="svcArOffice" id="svcArOffice" fieldId="SVCAROffice" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
</cmr:view>
