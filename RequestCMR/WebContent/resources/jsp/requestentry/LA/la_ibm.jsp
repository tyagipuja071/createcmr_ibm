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
<cmr:view forGEO="LA">
  <cmr:row topPad="10" addBackground="false">
    <cmr:view forCountry="631">
      <c:choose>
        <c:when test="${reqentry.getUserRole()=='Processor' && !readOnly}">
          <cmr:column span="2" containerForField="IBMBankNumber">
            <p>
              <cmr:label fieldId="ibmBankNumber">
                <cmr:fieldLabel fieldId="IBMBankNumber" />: 
          </cmr:label>
              <cmr:field fieldId="IBMBankNumber" id="ibmBankNumber" path="ibmBankNumber" tabId="MAIN_IBM_TAB" />
          </cmr:column>
        </c:when>
        <c:otherwise>
          <form:hidden path="ibmBankNumber" />
        </c:otherwise>
      </c:choose>
    </cmr:view>
    <cmr:column span="2">
      <p></p>
    </cmr:column>
  </cmr:row>
  <!-- Mukesh:Story 1067140: START Sucursal/Collection Branch Office -->
  <cmr:row topPad="10">
    <cmr:column span="2" containerForField="CollBranchOff">
      <p>
        <cmr:label fieldId="collBoId">
          <cmr:fieldLabel fieldId="CollBranchOff" />:
            <cmr:delta text="${rdcdata.collBoId}" oldValue="${reqentry.collBoId}" />
          <cmr:info text="${ui.info.collBranchOff}" />
        </cmr:label>
        <cmr:field path="collBoId" id="collBoId" fieldId="CollBranchOff" tabId="MAIN_IBM_TAB" placeHolder="Sucursal/Collection Branch Office" />
        <%
          if (!readOnly) {
        %>
        <img src="${resourcesPath}/images/remove-icon.png" class="cmr-remove-icon" onclick="clearSucursalcollBO()"
          title="Clear Sucursal/Collection Branch Office"> <img src="${resourcesPath}/images/search-icon.png" class="cmr-search-icon"
          onclick="doSucursalCollBOSearch()" title="Sucursal/Collection Branch Office">
        <%
          }
        %>
        <c:if test="${reqentry.collBODesc == null}">
          <span class="cmr-bluepages-ro" id="collBODesc">(none selected)</span>
        </c:if>
        <c:if test="${reqentry.collBODesc != null}">
          <span class="cmr-bluepages-ro" id="collBODesc">${reqentry.collBODesc}</span>
        </c:if>
      </p>
    </cmr:column>
    <%--DTN:Story 1064282; LA: Collector names from CROS Table needed --%>
    <cmr:column span="2" containerForField="CollectorNameNo">
      <p>
        <cmr:label fieldId="collectorNameNo">
          <cmr:fieldLabel fieldId="CollectorNameNo" />:
          <cmr:delta text="${rdcdata.collectorNameNo}" oldValue="${reqentry.collectorNameNo}" />
          </cmr:label>
        <cmr:field fieldId="CollectorNameNo" id="collectorNameNo" path="collectorNameNo" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <%--DTN:Story 1064282; LA: Collector names from CROS Table needed --%>
  </cmr:row>
  <!-- Mukesh:Story 1067140: END Sucursal/Collection Branch Office -->
  <%--DTN: START SALES BRANCH OFFICE --%>
  <cmr:row topPad="10" addBackground="false">
    <cmr:column span="2" containerForField="SalesBusOff">
      <p>
        <cmr:label fieldId="salesBusOffCd">
          <cmr:fieldLabel fieldId="SalesBusOff" />:
          <cmr:delta text="${rdcdata.salesBusOffCd}" oldValue="${reqentry.salesBusOffCd}" />
          </cmr:label>
        <cmr:field fieldId="SalesBusOff" id="salesBusOffCd" path="salesBusOffCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>

    <cmr:column span="2" containerForField="MrcCd">
      <p>
        <cmr:label fieldId="mrcCd">
          <cmr:fieldLabel fieldId="MrcCd" />:
          <cmr:delta text="${rdcdata.mrcCd}" oldValue="${reqentry.mrcCd}" />
          </cmr:label>
        <cmr:field fieldId="MrcCd" id="mrcCd" path="mrcCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <!-- Mukesh:Story 1164429: Hide MRC ISU  and SBO MRC for View Only Mode -->
    <%
      if (!readOnly) {
    %>
    <cmr:column span="1" containerForField="MRCISU">
      <p>
        <cmr:label fieldId="mrcIsu">
          <cmr:fieldLabel fieldId="MRCISU" />:
          </cmr:label>
        <cmr:field fieldId="MRCISU" id="mrcIsu" path="" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="1" containerForField="SBOMRC">
      <p>
        <cmr:label fieldId="sboMrc">
          <cmr:fieldLabel fieldId="SBOMRC" />:
          </cmr:label>
        <cmr:field fieldId="SBOMRC" id="sboMrc" path="" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <%
      }
    %>
  </cmr:row>
  
  <%--DTN: END SALES BRANCH OFFICE --%>
  <!-- Mukesh:Story 1067134: START For Sales Rep Name/No -->
  
  <cmr:row topPad="10">
    <cmr:view forCountry="631">
    <cmr:column span="2" containerForField="ProxiLocationNumber">
      <p>
        <cmr:label fieldId="proxiLocnNo">
          <cmr:fieldLabel fieldId="ProxiLocationNumber" />:
        </cmr:label>
        <cmr:field fieldId="ProxiLocationNumber" id="proxiLocnNo" path="proxiLocnNo" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
     <cmr:column span="2" containerForField="LocationNumber">
        <p>
          <cmr:label fieldId="locationNumber">
            <cmr:fieldLabel fieldId="LocationNumber" />: 
          </cmr:label>
          <cmr:field fieldId="LocationNumber" id="locationNumber" path="locationNumber" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
    </cmr:view>
    
    <%--commented to hide for deploy of PROD issue --%>
    <%--<cmr:column span="2" containerForField="InstallTeamCode">
      <p>
        <cmr:label fieldId="installTeamCd">
          <cmr:fieldLabel fieldId="InstallTeamCode" />:
        </cmr:label>
        <cmr:field fieldId="InstallTeamCode" id="installTeamCd" path="installTeamCd" size="80" tabId="MAIN_IBM_TAB" />
        </p>
    </cmr:column> --%>
  </cmr:row>
  <!-- Mukesh:Story 1067134: END For Sales Rep Name/No -->
</cmr:view>
