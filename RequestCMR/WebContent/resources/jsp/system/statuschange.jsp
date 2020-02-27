<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FilteringDropdown.loadItems('newReqStatus', 'newReqStatus_spinner', 'lov', 'fieldId=RequestStatus');
    FilteringDropdown.loadOnChange('newLockedInd', 'newLockedInd_spinner', 'FORCELOCK', 'newReqStatus=_newReqStatus','newReqStatus');
    FilteringDropdown.loadItems('newProcessedFlag', 'newProcessedFlag_spinner', 'lov', 'fieldId=ProcessingStatus');
    dojo.connect(dijit.byId('newLockedInd'),"onChange", function(value){
      if (value == 'Y'){
        FormManager.enable('newLockedByNm');
      } else {
        FormManager.disable('newLockedByNm');
      }
    });
    dojo.connect(dijit.byId('newReqStatus'),"onChange", function(value){
      if (value == 'PCP'){
        FormManager.setValue('newProcessedFlag', 'N');
        FormManager.readOnly('newProcessedFlag');
      } else {
        FormManager.enable('newProcessedFlag');
      }
    });
    if (FormManager) {
      FormManager.addValidator('searchReqId', Validators.NUMBER, [ '${ui.requestId}' ]);
      FormManager.addValidator('newReqStatus', Validators.REQUIRED, [ '${ui.newreqstatus}' ]);
      FormManager.addValidator('newLockedInd', Validators.REQUIRED, [ '${ui.newlockstate}' ]);
      FormManager.addValidator('newProcessedFlag', Validators.REQUIRED, [ 'New Processing Status' ]);
      FormManager.addValidator('newLockedByNm', Validators.BLUEPAGES, [ '${ui.newlockby}' ]);
      FormManager.addConditionalValidator('newLockedByNm', [ '${ui.newlockby}', '${ui.newlockstate}', 'Locked' ], 'newLockedInd', 'Y');
      FormManager.addValidator('cmt', Validators.REQUIRED, [ '${ui.statuscmt}' ]);
      FormManager.disable('newLockedByNm');
      FormManager.ready();
    }
  });


</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/statuschange" name="frmCMRSearch" class="ibm-column-form ibm-styled-form" modelAttribute="status">
    <cmr:section>
      <cmr:row topPad="10" addBackground="true">
        <cmr:column span="3">
          <cmr:note text="${ui.note.statuschangenote}" />
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="3">
          <p>
            <cmr:label fieldId="searchReqId" >
            ${ui.requestId}:
          </cmr:label>
          </p>
          <form:input path="searchReqId" size="30" />
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="true">
        <cmr:column span="3">
          <cmr:button label="${ui.btn.retrieverecord}" onClick="retrieveRequest()" highlight="true" />
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
      &nbsp;
      </cmr:row>

    </cmr:section>
  </form:form>
  <c:if test="${status.reqId > 0}">
    <form:form method="POST" action="${contextPath}/statuschange/process" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="status">
      <cmr:modelAction formName="frmCMR" />
      <form:hidden path="reqId" />
      <cmr:section>
        <cmr:row topPad="10">
          <cmr:column span="3">
            <p>
                <cmr:label fieldId="header"><strong>Request Details</strong></cmr:label>            
            </p>
          </cmr:column>
        </cmr:row>
      <cmr:row>
        <cmr:column span="2" >
          <span style="color:red;font-size:16px;font-weight:bold">*</span> 
        <span style="font-size:14px;font-style:italic;color:#333">
          ${ui.mandatoryLegend}
        </span>
          </cmr:column>
          </cmr:row>
        <cmr:row>
          <cmr:column span="3">
            <p>
              <cmr:label fieldId="reqId">${ui.requestId}: <a title="Open Request Details" href="${contextPath}/request/${status.reqId}">${status.reqId}</a>
            </cmr:label>
            </p>
          </cmr:column>
        </cmr:row>
        <cmr:row>
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="reqStatus">${ui.currreqstatus}:</cmr:label>
              ${status.reqStatus}
            </p>
          </cmr:column>
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="lockInd">${ui.currlockstate}:</cmr:label>
              ${status.lockInd}
            </p>
          </cmr:column>
        </cmr:row>
        <cmr:row>
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="newReqStatus">
                ${ui.newreqstatus}:
              <cmr:spinner fieldId="newReqStatus" />
              </cmr:label>
              <form:select dojoType="dijit.form.FilteringSelect" size="35"  id="newReqStatus" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="newReqStatus">
              </form:select>
            </p>
          </cmr:column>
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="newLockedInd">
                ${ui.newlockstate }:
              <cmr:spinner fieldId="newLockedInd" />
        </cmr:label>
              <form:select dojoType="dijit.form.FilteringSelect" id="newLockedInd" searchAttr="name" style="display: block;" maxHeight="200" path="newLockedInd" />
            </p>
          </cmr:column>
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="newLockedByNm">
                ${ui.newlockby}:
             </cmr:label>
              <cmr:bluepages model="status" namePath="newLockedByNm" idPath="newLockedById" />
            </p>
          </cmr:column>
        </cmr:row>
        <cmr:row addBackground="true">
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="ProcessingStatus">Current Processing Status:</cmr:label>
              ${status.processedFlag}
            </p>
          </cmr:column>
        </cmr:row>
        <cmr:row addBackground="true">
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="newProcessedFlag">New Processing Status:</cmr:label>
              <form:select dojoType="dijit.form.FilteringSelect" id="newProcessedFlag" searchAttr="name" style="display: block;" maxHeight="200" path="newProcessedFlag" />
            </p>
          </cmr:column>
        </cmr:row>
        <cmr:row>
          <cmr:column span="3">
            <p>
              <cmr:label fieldId="cmt">
                ${ui.statuscmt}:
                <cmr:memoLimit maxLength="208" fieldId="cmt" />
              </cmr:label>
              <form:textarea path="cmt" rows="6" cols="46" />
            </p>
          </cmr:column>
        </cmr:row>
        <cmr:row>
          <cmr:column span="2">
        &nbsp;
        </cmr:column>
        </cmr:row>
      </cmr:section>
    </form:form>
  </c:if>
</cmr:boxContent>
<c:if test="${status.reqId > 0}">
  <cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <cmr:button label="${ui.btn.save}" onClick="changeStatus()" highlight="true" />
      <cmr:button label="${ui.btn.cancel}" onClick="window.location='${contextPath}/statuschange'" pad="true" />
    </cmr:buttonsRow>
  </cmr:section>
</c:if>
<cmr:model model="status" />