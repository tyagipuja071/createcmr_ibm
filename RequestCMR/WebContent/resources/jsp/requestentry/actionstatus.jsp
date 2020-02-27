<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>


<!--  Modal for the Status Change Screen -->
<cmr:modal title="${ui.title.statuschange}" id="statusChangeModal" widthId="570">
  <cmr:row>
    <cmr:column span="1" width="200">
      <cmr:label fieldId="newReqStatus">${ui.newreqstatus}:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="newReqStatus">-</div>
    </cmr:column>
  </cmr:row>
  <div id="sendToBlock" style="display:none">
    <cmr:row>
      <cmr:column span="1" width="200">
        <cmr:label fieldId="sendTo">${ui.sendto}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="sendToProcessingCenter">-</div>
      </cmr:column>
    </cmr:row>
  </div>
  <div id="rejectReasonBlock" style="display:none">
    <cmr:row>
      <cmr:column span="1" width="200">
        <cmr:label fieldId="rejectReason">${ui.rejectreason}:
          <span class="ibm-required">*</span>
        </cmr:label>
      </cmr:column>
      <cmr:column span="2">
          <form:select dojoType="dijit.form.FilteringSelect" id="rejectReason"
            searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="rejectReason" placeHolder=""></form:select>
      </cmr:column>
    </cmr:row>
  </div>
    <cmr:row>
      <cmr:column span="3" width="500">
        <cmr:label fieldId="statusChgCmt" cssClass="cmr-status-cmt">
          ${ui.statuschangecmt}:
          <cmr:memoLimit maxLength="1000" fieldId="statusChgCmt" />
        </cmr:label>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="3">
        <form:textarea path="statusChgCmt" rows="5" cols="50"/>
      </cmr:column>
    </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.save}" onClick="doSaveChangeComments()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('statusChangeModal')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>
