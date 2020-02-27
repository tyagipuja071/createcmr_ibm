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
  if (readOnly == null) {
    readOnly = false;
  }
%>
<!--  Modal for the contact information details screen -->
<cmr:modal title="${ui.title.contactInfoDetails}" id="contactInfoDetailsModal" widthId="980">

  <cmr:row>
    <cmr:column span="2">
      <cmr:label fieldId="contactType_view">
        <span class="lbl-ContactType">${ui.grid.contactTypeHeader}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="contactType_view">-</div>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <cmr:column span="2">
      <cmr:label fieldId="contactSeqNumber_view">
        <span class="lbl-ContactSeqNumber">${ui.grid.contactSequenceHeader}</span>: </cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="contactSeqNumber_view">-</div>
    </cmr:column>
  </cmr:row>


  <cmr:row>
    <cmr:column span="2">
      <cmr:label fieldId="contactName_view">
        <span class="lbl-ContactName">${ui.grid.contactNameHeader}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="contactName_view">-</div>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <cmr:column span="2">
      <cmr:label fieldId="contactPhone_view">
        <span class="lbl-ContactPhone">${ui.grid.contactPhoneHeader}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="contactPhone_view">-</div>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <cmr:column span="2">
      <cmr:label fieldId="contactEmail_view">
        <span class="lbl-ContactEmail">${ui.grid.contactEmailHeader}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="contactEmail_view">-</div>
    </cmr:column>
  </cmr:row>
  <input type="hidden" name="reqId_view" id="reqId_view" />
  <input type="hidden" name="contactInfoId_view" id="contactInfoId_view" />
  <input type="hidden" name="contactFunc_view" id="contactFunc_view" />
  <input type="hidden" name="contactTreatment_view" id="contactTreatment_view" />

  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.close}" onClick="cmr.hideModal('contactInfoDetailsModal')" />
    <c:if test="${!readOnly}">
      <cmr:button label="${ui.btn.update}" id="updateContactBtnFromView" onClick="updateContactInfoFromDetails()" highlight="true" pad="true" />
    </c:if>
  </cmr:buttonsRow>
</cmr:modal>