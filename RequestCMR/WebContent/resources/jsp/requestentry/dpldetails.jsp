<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<style>
#DplDetailsModal div.ibm-columns {
  width: 540px !important;
}

</style>
<!--  Modal for the DPL Check Details -->
<cmr:modal title="${ui.title.dplDetails}" id="DplDetailsModal"  widthId="570">
  <cmr:form name="fmrDPLDetails">
    <cmr:row addBackground="true">
      <cmr:column span="1" width="200">
        <cmr:label fieldId="dpl_result">${ui.dpl.Result}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="dpl_result"></div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1" width="200">
        <cmr:label fieldId="dpl_errorlist">${ui.dpl.DenialCode}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="dpl_errorlist"></div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1" width="200">
        <cmr:label fieldId="dpl_ts">${ui.dpl.Timestamp} (<%=SystemConfiguration.getValue("DATE_TIMEZONE", "GMT")%>):</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="dpl_ts"></div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1" width="200">
        <cmr:label fieldId="dpl_by">${ui.dpl.CheckBy}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="dpl_by"></div>
      </cmr:column>
    </cmr:row>
    <cmr:row >
      <cmr:column span="1" width="200">
        <cmr:label fieldId="dpl_data">${ui.dpl.SuppliedData}:</cmr:label>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="1" width="200">
        <cmr:label fieldId="dpl_custnm"><div style="padding-left:15px">${ui.dpl.CustName}:</div></cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="dpl_custnm"></div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="200">
        <cmr:label fieldId="dpl_cntry"><div style="padding-left:15px">${ui.dpl.Country}:</div></cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="dpl_cntry"></div>
      </cmr:column>
    </cmr:row>
    <cmr:buttonsRow>
      <cmr:hr />
      <a class="ibm-btn-pri" href="javascript: doDplSearchRequest()" title="Search against the DPL database using request data" style="display:inline">DPL Search</a>
      <cmr:info text="Searches the DPL Database using the saved information on the request" />
      <cmr:button label="${ui.btn.close}" onClick="cmr.hideModal('DplDetailsModal')" highlight="false" pad="true" />
    </cmr:buttonsRow>
  </cmr:form>
</cmr:modal>
