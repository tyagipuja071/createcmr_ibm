<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
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
if (readOnly == null){
readOnly = false;
}
%>
<style>
#addEditTaxInfoModal div.ibm-columns {
  width: 730px !important;
}

</style>
<!--  Modal for the Add/Edit Screen -->
<cmr:modal title="${ui.title.addEditTaxInfo}" id="addEditTaxInfoModal" widthId="750">
  <form:form method="GET" action="${contextPath}/request/taxinfo/process" name="frmCMR_taxInfoModal" class="ibm-column-form ibm-styled-form" modelAttribute="taxInfoModal" id="frmCMR_taxInfoModal">
    <cmr:modelAction formName="frmCMR_taxInfoModal" />
    <form:hidden path="reqId" id="taxinfo_reqId" value="${reqentry.reqId}" />
    <form:hidden path="geoTaxInfoId" id="geoTaxInfoId" />
    <cmr:row topPad="10">
      <cmr:column span="4">
        <jsp:include page="../templates/messages_modal.jsp">
          <jsp:param value="addEditTaxInfoModal" name="modalId" />
        </jsp:include>
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="10">
      <cmr:column span="2">
        <p>
          <cmr:label fieldId="taxCd">
             <cmr:fieldLabel fieldId="TaxCd" />:
          </cmr:label>
          <cmr:field fieldId="TaxCd" id="taxCd" path="taxCd" />
        </p>
      </cmr:column>
    </cmr:row>
    <% if(null!= reqentry.getCmrIssuingCntry() && reqentry.getCmrIssuingCntry().equals("613")) {%> 
    <cmr:row>
    <cmr:column span="2">
        <p>
          <cmr:label fieldId="taxNum">
             <cmr:fieldLabel fieldId="TaxNum" />:
          </cmr:label>
          <cmr:field fieldId="TaxNum" id="taxNum" path="taxNum" />
        </p>
      </cmr:column>
    </cmr:row> 
    <%} %>
    <cmr:row>
    <cmr:column span="2">
        <p>
          <cmr:label fieldId="taxSeparationIndc">
             <cmr:fieldLabel fieldId="TaxSeparationIndc" />:
          </cmr:label>
          <cmr:field fieldId="TaxSeparationIndc" id="taxSeparationIndc" path="taxSeparationIndc" />
        </p>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="2">
        <p>
          <cmr:label fieldId="billingPrintIndc">
             <cmr:fieldLabel fieldId="BillingPrintIndc" />: 
          </cmr:label>
          <cmr:field fieldId="BillingPrintIndc" id="billingPrintIndc" path="billingPrintIndc" />
        </p>
      </cmr:column>
      <cmr:column span="2">
        <p>
          <cmr:label fieldId="contractPrintIndc">
             <cmr:fieldLabel fieldId="ContractPrintIndc" />:
          </cmr:label>
          <cmr:field fieldId="ContractPrintIndc" id="contractPrintIndc" path="contractPrintIndc"/>
        </p>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="2">
        <p>
          <cmr:label fieldId="cntryUse">
             <cmr:fieldLabel fieldId="CountryUse" />:
          </cmr:label>
          <cmr:field fieldId="CountryUse" id="cntryUse" path="cntryUse" />
        </p>
      </cmr:column>  
    </cmr:row>
    
    <cmr:buttonsRow>
      <cmr:hr />
      <cmr:button label="${ui.btn.addTaxInfoButton}" onClick="doAddToTaxInfoList()" highlight="true" pad="true" id="taxInfoBtn"/>
      <cmr:button label="${ui.btn.cancel}" onClick="cancelTaxInfoModal()" highlight="false" pad="true" />
    </cmr:buttonsRow>
  </form:form>
</cmr:modal>