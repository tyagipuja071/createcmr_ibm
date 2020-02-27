<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
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
.cmr-middle {
  text-align: center;
}
</style>
<script>
  
</script>
<cmr:section id="CONTACTINFO_REQ_TAB" hidden="true">
  <jsp:include page="detailstrip.jsp" />

  <cmr:row addBackground="true" topPad="10">
    <cmr:column span="1" width="180">
      <p>
        <cmr:label fieldId="contactInforList">${ui.contactInfo.topHeader}:</cmr:label>
      </p>
    </cmr:column>
  </cmr:row>
  <%
    String contextPath = request.getContextPath();
      String actions = UIMgr.getText("grid.actions");
      String infos = UIMgr.getText("info.contactInfo.actions");
      String actionsHeader = actions + "<img src=\"" + contextPath + "/resources/images/info-bubble-icon.png\" title=\"" + infos
          + "\" class=\"cmr-info-bubble\">";
  %>
  <cmr:row addBackground="true" topPad="10">
    <cmr:column span="6">
      <cmr:grid url="/request/contactinfo/list.json" id="CONTACTINFO_GRID" span="6" height="250" usePaging="false" ><%-- hasCheckbox="true"  checkBoxKeys="reqId,contactInfoId" --%>
        <cmr:gridParam fieldId="reqId" value="${reqentry.reqId}" />
        <cmr:gridParam fieldId="reqType" value="${reqentry.reqType}" />
        <cmr:gridParam fieldId="cmrIssuingCntry" value="${reqentry.cmrIssuingCntry}" />
        <cmr:gridCol width="80px" field="contactType" header="${ui.grid.contactTypeHeader}">
          <cmr:formatter functionName="contactTypeFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="150px" field="contactSeqNum" header="${ui.grid.contactSequenceHeader}" />
        <cmr:gridCol width="150px" field="contactName" header="${ui.grid.contactNameHeader}" />
        <cmr:gridCol width="150px" field="contactPhone" header="${ui.grid.contactPhoneHeader}" />
        <cmr:gridCol width="150px" field="contactEmail" header="${ui.grid.contactEmailHeader}" />
        <cmr:gridCol width="200px" field="action" header="<%=actionsHeader%>">
          <c:if test="${!readOnly}">
            <cmr:formatter functionName="contactInfoActionFormatter" />
          </c:if>
        </cmr:gridCol>
      </cmr:grid>
    </cmr:column>
  </cmr:row>
  

  <cmr:row topPad="5" addBackground="true">
    <div class="ibm-col-1-1 cmr-middle">
      <c:if test="${!readOnly}">
        <cmr:button label="${ui.btn.addContactInfo}" onClick="newContactInfo()" styleClass="cmr-middle" id="addContactInfoBtn" />
        <%-- <cmr:button label="${ui.btn.removeSelectedContact}" onClick="removeSelectedContacts()" styleClass="cmr-middle"
          id="removeSelectedContactsBtn" /> --%>
      </c:if>
    </div>
    <br />
    <br />
  </cmr:row>
</cmr:section>