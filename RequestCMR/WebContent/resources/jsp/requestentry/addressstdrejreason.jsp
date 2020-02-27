<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<!--  Modal for the address details creen -->
<cmr:modal title="${ui.title.addrStdRejectReason}" id="addressStdRejectReasonModal" widthId="570">
  <form:form method="GET" action="${contextPath}/request/address/process" name="frmCMR_addressModalRej" class="ibm-column-form ibm-styled-form" modelAttribute="addressModal" id="frmCMR_addressModalRej">

  <cmr:row>
    <cmr:column span="2">
      <p>
        <cmr:label fieldId="rejectReasonAddrStd">
          ${ui.rejectReasonAddrStd} : <span class="ibm-required cmr-required-spacer">*</span>
        </cmr:label>
        <cmr:field model="addressModel" id="addrStdRejReason" path="addrStdRejReason" fieldId="AddrStdRejectReason" size="300"/>
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <cmr:column span="4" width="500">
      <label for="addrStdRejCmt"> ${ui.addrStdRejCmt}: <cmr:memoLimit maxLength="250" fieldId="addrStdRejCmtTA" /> </label>
      <textarea id="addrStdRejCmtTA" name="addrStdRejCmt" rows="6" cols="52" style="overflow-y: scroll"></textarea>
    </cmr:column>
  </cmr:row>




  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.save}" onClick="doSaveRejectReason()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="doCancelRejectReason()" highlight="false" pad="true"/>
  </cmr:buttonsRow>
</form:form>

</cmr:modal>