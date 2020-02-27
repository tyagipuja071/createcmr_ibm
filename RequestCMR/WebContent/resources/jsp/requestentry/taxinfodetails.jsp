<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
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
<!--  Modal for the Tax Info details screen -->
<cmr:modal title="${ui.title.taxInfoDetails}" id="TaxInfoDetailsModal" widthId="980">

  <input type="hidden" name="view_reqId" id="view_reqId">
  <input type="hidden" name="view_geoTaxInfoId" id="view_geoTaxInfoId">
    
    <cmr:row>
    <cmr:column span="1">
      <cmr:label fieldId="taxCd_view"><span class="lbl-TaxCd">${ui.taxCd}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="taxCd_view">-</div>
    </cmr:column>
  </cmr:row>
  
  <% if(null!= reqentry.getCmrIssuingCntry() && reqentry.getCmrIssuingCntry().equals("613")) {%> 
  <cmr:row>
    <cmr:column span="1">
      <cmr:label fieldId="taxNum_view"><span class="lbl-TaxNum">${ui.taxNum}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="taxNum_view">-</div>
    </cmr:column>
  </cmr:row>
  <%} %>
  
  <cmr:row>
    <cmr:column span="1">
      <cmr:label fieldId="taxSeparationIndc_view"><span class="lbl-TaxSeparationIndc">${ui.taxSeparationIndc}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="taxSeparationIndc_view">-</div>
    </cmr:column>
  </cmr:row>

<cmr:row>
    <cmr:column span="1">
      <cmr:label fieldId="contractPrintIndc_view"><span class="lbl-ContractPrintIndc">${ui.contractPrintIndc}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="contractPrintIndc_view">-</div>
    </cmr:column>
  </cmr:row>   
<cmr:row>
    <cmr:column span="1">
      <cmr:label fieldId="billingPrintIndc_view"><span class="lbl-BillingPrintIndc">${ui.billingPrintIndc}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="billingPrintIndc_view">-</div>
    </cmr:column>
  </cmr:row> 
    
  <cmr:row>
    <cmr:column span="1">
      <cmr:label fieldId="cntryUse_view"><span class="lbl-cntryUse">${ui.cntryUse}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="cntryUse_view">-</div>
    </cmr:column>
  </cmr:row> 
  
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.close}" onClick="cmr.hideModal('TaxInfoDetailsModal')"  />
<%if (!readOnly){%>
    <cmr:button label="${ui.btn.update}" id="updateButtonFromView" onClick="doUpdateTaxInfoFromDetails()" highlight="true" pad="true"/>
<%} %>
  </cmr:buttonsRow>
</cmr:modal>