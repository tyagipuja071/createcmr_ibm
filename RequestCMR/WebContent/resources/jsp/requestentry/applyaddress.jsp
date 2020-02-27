<%@page import="com.ibm.cio.cmr.request.model.requestentry.CopyAddressModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  request.setAttribute("copyAddr", new CopyAddressModel());
%>
<script>
  dojo.addOnLoad(function() {
  });
</script>
<style>
#applyAddrChangesModal div.ibm-columns {
  width: 550px !important;
}
</style>

<!--  Modal for the Apply Address Changes Screen -->
<cmr:modal title="${ui.title.applyAddrChanges}" id="applyAddrChangesModal" widthId="570">
  <form:form method="POST" action="${contextPath}/request/address/apply" name="frmCMRCopyAddrChanges" class="ibm-column-form ibm-styled-form"
    modelAttribute="copyAddr" id="frmCMRCopyAddrChanges" >
    <cmr:modelAction formName="frmCMRCopyAddrChanges" />
    <form:hidden path="reqId" id="apply_reqId" value="${reqentry.reqId}" />
    <form:hidden path="addrType" id="copy_addrType" />
    <form:hidden path="addrSeq" id="copy_addrSeq" />
    <form:hidden path="createOnly"  id="copy_createOnly" />
    <form:hidden path="cmrIssuingCntry" id="copy_cmrIssuingCntry" value="${reqentry.cmrIssuingCntry}" />
    <cmr:row topPad="10">
      <cmr:column span="3" width="500">
        <jsp:include page="../templates/messages_modal.jsp">
          <jsp:param value="applyAddrChangesModal" name="modalId" />
        </jsp:include>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="3" width="500">
      <cmr:note text="${ui.note.applychanges}"></cmr:note>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <p>
          <cmr:label fieldId="applyAddressTypes">Address Types:</cmr:label>
        </p>
      </cmr:column>
      <cmr:column span="2">
         <div id="applyAddressTypesChoices">
         </div>
      </cmr:column>
    </cmr:row>
    <cmr:buttonsRow>
      <cmr:hr />
      <cmr:button label="${ui.btn.applyChanges}" onClick="copyAddressData()" highlight="true" />
      <cmr:button label="${ui.btn.cancel}" onClick="cancelCopyAddress()" highlight="false" pad="true" />
    </cmr:buttonsRow>
  </form:form>
</cmr:modal>
