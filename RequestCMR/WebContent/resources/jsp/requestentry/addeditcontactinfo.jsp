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
  if (readOnly == null) {
    readOnly = false;
  }
%>
<style>
#addEditContactInfoModal div.ibm-columns {
  width: 700px !important;
}
</style>
<!--  Modal for contact information Add/Edit Screen -->
<cmr:modal title="" id="addEditContactInfoModal" widthId="750">
  <form:form method="GET" action="${contextPath}/request/contactinfo/process.json" name="frmCMR_contactInfoModal"
    class="ibm-column-form ibm-styled-form" modelAttribute="contactInfoModel" id="frmCMR_contactInfoModal">
    <cmr:modelAction formName="frmCMR_contactInfoModal" />
    <form:hidden path="reqId" id="contactInfo_reqId" value="${reqentry.reqId}" />
    <form:hidden path="contactInfoId" id="contactInfoId" />
    <form:hidden path="reqType" id="contactInfo_reqType" value="${reqentry.reqType}" />
    <form:hidden path="cmrIssuingCntry" id="contactInfo_cmrIssuingCntry" value="${reqentry.cmrIssuingCntry}" />
    <form:hidden path="currentEmail1" id="currentEmail1"/>
    <form:hidden path="contactFunc" id="contactFunc"/>
    <form:hidden path="contactTreatment" id="contactTreatment"/>
    <cmr:row topPad="10">
      <cmr:column span="4">
        <jsp:include page="../templates/messages_modal.jsp">
          <jsp:param value="addEditContactInfoModal" name="modalId" />
        </jsp:include>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="2">
        <cmr:label fieldId="contactType">
          <cmr:fieldLabel fieldId="ContactType" /> : 
        </cmr:label>
        <cmr:field fieldId="ContactType" id="contactType" path="contactType" />
      </cmr:column>
    </cmr:row>

    <cmr:row topPad="5">
      <cmr:column span="2" containerForField="ContactSeqNumber">
        <p>
          <label for="contactSeqNum"> <cmr:fieldLabel fieldId="ContactSeqNumber" /> : </label>
          <cmr:field fieldId="ContactSeqNumber" id="contactSeqNum" path="contactSeqNum" tabId="" />
        </p>
      </cmr:column>

      <cmr:column span="2" containerForField="ContactName">
        <p>
          <label for="contactName"> <cmr:fieldLabel fieldId="ContactName" /> : </label>
          <cmr:field fieldId="ContactName" id="contactName" path="contactName" tabId="" />
        </p>
      </cmr:column>

      <cmr:column span="2" containerForField="Phone">
        <p>
          <label for="contactPhone"> <cmr:fieldLabel fieldId="Phone" /> : </label>
          <cmr:field fieldId="Phone" id="contactPhone" path="contactPhone" tabId="" />
        </p>
      </cmr:column>

      <cmr:column span="2" containerForField="Email">
        <p>
          <label for="contactEmail"> <cmr:fieldLabel fieldId="Email" /> : </label>
          <cmr:field fieldId="Email" id="contactEmail" path="contactEmail" tabId="" />
        </p>
      </cmr:column>
    </cmr:row>

    <cmr:buttonsRow>
      <cmr:hr />
      <cmr:button label="${ui.btn.save}" onClick="addToContactInfoList()" highlight="true" pad="true" id="contactInfoBtn" />
      <cmr:button label="${ui.btn.cancel}" onClick="hideContactInfoModal()" highlight="false" pad="true" />
    </cmr:buttonsRow>
  </form:form>
</cmr:modal>