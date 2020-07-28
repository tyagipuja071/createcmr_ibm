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
%><%--
<script type="text/javascript">
  dojo.addOnLoad(function() {
    if (FormManager) {
      FormManager.addValidator('repTeamMemberName', Validators.BLUEPAGES, [ '${repTeamMemberNo}' ], 'MAIN_IBM_TAB');
      FormManager.ready();
    }
  });
</script>--%>
<cmr:view forGEO="EMEA">
  <cmr:row topPad="10">
    <!-- Story 1126634: SBO and Sales Rep-->
    <cmr:column span="2" containerForField="SalesBusOff" forCountry="866">
      <p>
        <cmr:label fieldId="salesBusOffCd">
          <cmr:fieldLabel fieldId="SalesBusOff" />:
             <!--cmr:delta text="{rdcdata.salesBusOffCd}" oldValue="${reqentry.salesBusOffCd}" id="delta-salesBusOffCd" />-->
        </cmr:label>
        <cmr:field fieldId="SalesBusOff" id="salesBusOffCd" path="salesBusOffCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="SalRepNameNo" exceptForCountry="862,666,726">
      <p>
        <cmr:label fieldId="repTeamMemberNo">
          <cmr:fieldLabel fieldId="SalRepNameNo" />:
           <cmr:delta text="${rdcdata.repTeamMemberNo}" oldValue="${reqentry.repTeamMemberNo}" id="delta-repTeamMemberNo" />
        </cmr:label>
        <cmr:field fieldId="SalRepNameNo" id="repTeamMemberNo" path="repTeamMemberNo" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <%-- *abner revert begin
    <cmr:view forCountry="666,726">
     --%>
    <cmr:view forCountry="862,666,726"> 
      <cmr:column span="2" containerForField="ISR">
        <p>
          <cmr:label fieldId="repTeamMemberNo">
            <cmr:fieldLabel fieldId="ISR" />:
             <cmr:delta text="${rdcdata.repTeamMemberNo}" oldValue="${reqentry.repTeamMemberNo}"  id="delta-repTeamMemberNo" />
          </cmr:label>
          <cmr:field fieldId="ISR" id="repTeamMemberNo" path="repTeamMemberNo" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>    
      <cmr:column span="2" containerForField="SalesSR">
        <p>
          <cmr:label fieldId="salesSR">
            <cmr:fieldLabel fieldId="SalesSR" />:
             <!--cmr:delta text="{rdcdata.salesTeamCd}" oldValue="${reqentry.salesTeamCd}" id="delta-salesTeamCd" />-->
          </cmr:label>
          <cmr:field fieldId="SalesSR" id="salesTeamCd" path="salesTeamCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>    
    </cmr:view>
    <cmr:column span="2" containerForField="Enterprise" forCountry="755">
      <p>
        <cmr:label fieldId="enterprise">
          <cmr:fieldLabel fieldId="Enterprise" />:
            <cmr:delta text="${rdcdata.enterprise}" oldValue="${reqentry.enterprise}" />
        </cmr:label>
        <cmr:field id="enterprise" path="enterprise" fieldId="Enterprise" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="SalesBusOff" exceptForCountry="866">
      <p>
        <cmr:label fieldId="salesBusOffCd">
          <cmr:fieldLabel fieldId="SalesBusOff" />:
             <!--cmr:delta text="{rdcdata.salesBusOffCd}" oldValue="${reqentry.salesBusOffCd}" id="delta-salesBusOffCd" />-->
        </cmr:label>
        <cmr:field fieldId="SalesBusOff" id="salesBusOffCd" path="salesBusOffCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <%-- END of SBO and Sales Rep --%>
  </cmr:row>

  <cmr:row topPad="10">
    <cmr:column span="2" containerForField="CollectionCd" exceptForCountry="862,666,726">
      <p>
        <cmr:label fieldId="collectionCd">
        <cmr:fieldLabel fieldId="CollectionCd" />: 
           <cmr:delta text="${rdcdata.collectionCd}" oldValue="${reqentry.collectionCd}" id="delta-engineeringBo" />
        </cmr:label>
        <cmr:field path="collectionCd" id="collectionCd" fieldId="CollectionCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    
    <cmr:view forCountry="862,666,726">
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
    <%} else if (reqentry.getReqType().equalsIgnoreCase("C")) { %>
      <cmr:column span="2" containerForField="CollectionCd2">
          <p>
            <cmr:label fieldId="collectionCd">
              <cmr:fieldLabel fieldId="CollectionCd" />:
             </cmr:label>
            <cmr:field fieldId="CollectionCd2" id="collectionCd2" path="collectionCd" tabId="MAIN_IBM_TAB"/>
          </p>
      </cmr:column> 
    <% } %>
    </cmr:view>
    
    <cmr:column span="2" containerForField="CodFlag" forCountry="755">
      <p>
        <cmr:label fieldId="codFlag">
          <cmr:fieldLabel fieldId="CodFlag" />:
           <cmr:delta text="${rdcdata.creditCd}" oldValue="${reqentry.creditCd}" id="delta-codFlag" />
        </cmr:label>
        <cmr:field fieldId="CodFlag" id="codFlag" path="creditCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    
    <cmr:column span="2" containerForField="EngineeringBo" forCountry="755">
      <p>
        <cmr:label fieldId="engineeringBo">
          <cmr:fieldLabel fieldId="EngineeringBo" />:
           <cmr:delta text="${rdcdata.engineeringBo}" oldValue="${reqentry.engineeringBo}" id="delta-engineeringBo" />
        </cmr:label>
        <cmr:field fieldId="EngineeringBo" id="engineeringBo" path="engineeringBo" tabId="MAIN_IBM_TAB" size="300"/>
      </p>
    </cmr:column>
  </cmr:row>
</cmr:view>
<cmr:view forCountry="754,866">
  <cmr:row topPad="10" addBackground="true">
    <cmr:column span="4" containerForField="InternalDept">
      <p>
        <cmr:label fieldId="ibmDeptCostCenter">
          <cmr:fieldLabel fieldId="InternalDept" />: 
     <div id = "deptInfo"><cmr:info text="${ui.info.department}"/> </div>
        </cmr:label>
        <cmr:field path="ibmDeptCostCenter" id="ibmDeptCostCenter" fieldId="InternalDept" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column> 
  </cmr:row>
</cmr:view>
