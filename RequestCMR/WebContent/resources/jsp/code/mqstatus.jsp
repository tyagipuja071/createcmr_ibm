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
<style>
  img.warn {
    padding-left: 10px;
  }
  img.action {
    width:17px;
    height:17px;
    vertical-align: sub;
    cursor: pointer;
    padding-right: 2px;
  }
</style>
<script>
  dojo.addOnLoad(function() {
    FilteringDropdown.loadItems('cmrIssuingCntry', 'cmrIssuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');
    FormManager.setValue('reqId', '');
    FormManager.setValue('queryReqId', '');
    FormManager.ready();
  });
  
  function backToCodeMaintHome() {
    window.location = cmr.CONTEXT_ROOT + '/code';
  }
  
  function cntryFormatter(value, index){
    var rowData = this.grid.getItem(index);
    var desc = rowData.lastUpdtBy;
    return '<span class="cmr-grid-tooltip" title="' + desc + '">' + value + '</span>';

  }

  function systemFormatter(value, index){
    var rowData = this.grid.getItem(index);
    var refNo = rowData.docmRefnNo[0];
    var srcCd = rowData.refnSourceCd[0];
    if (refNo != null && srcCd != null){
      return '<span class="cmr-grid-tooltip" title="Reference No. ' + refNo + '\nSource Code: '+srcCd+'">' + value + '</span>';
    } else {
      return value;
    }

  }

  function adminStatusFormatter(value, index){
    var rowData = this.grid.getItem(index);
    var adminStatus = rowData.adminStatus[0];
    var reqStatus = rowData.reqStatus[0];
    
    var warning = '<img src="'+cmr.CONTEXT_ROOT+'/resources/images/warn-icon.png" class="warn" title="Request status is out of sync. Consider stopping the processing.">';
    if (reqStatus == 'COM' && adminStatus != 'COM'){
      return '<strong>'+value+'</strong>'+warning;
    } else if (reqStatus.includes('SER') && adminStatus != 'COM' && adminStatus != 'PPN'){
      return '<strong>'+value+'</strong>'+warning;
    } else if ((reqStatus.includes('PUB') || reqStatus.includes('COM')) && reqStatus != 'COM' && adminStatus != 'PCR'){
      return '<strong>'+value+'</strong>'+warning;
    }
    return value;

  }

  function statusFormatter(value, index){
    if (value == null) {
      return '';
    }
    var rowData = this.grid.getItem(index);
    var system = rowData.targetSys;
    if (value == 'COM'){
      return '<span style="color:green;font-weight:bold">Completed</span>';
    }
    if (value.includes('PUB')){
      var stage = value.substring(3);
      if (stage == '0'){
        stage == '1';
      }
      var warning = '';
      if (rowData.warning == 'Y'){
        warning = '<img src="'+cmr.CONTEXT_ROOT+'/resources/images/warn-icon.png" class="warn" title="Pending for sometime. May be stuck.">';
      }
      return 'Sent ('+stage+')'+warning;
    }
    if (value.includes('SER')){
      var title = rowData.execpMessage;
      return '<span style="color:red;font-weight:bold;cursor:pointer" title="'+title+'">Error</span>';
    }
    if (value.includes('NEW')){
      return 'Pending';
    }
    if (value.includes('COM')){
      var stage = value.substring(3);
      if (stage == '0'){
        stage == '1';
      }
      return 'Replied ('+stage+')';
    }
    if (value.includes('RETRY') || value.includes('RESEND')){
      return 'For resend';
    }
    if (value.includes('WAIT')){
      return 'Waiting';
    }
    
    return '';
  }

  function uniqueIdFormatter(value, index){
    var rowData = this.grid.getItem(index);
    var hasData = rowData.hasData[0];
    if (hasData == 'Y'){
      return '<a title="Open XML contents for ' + value + '" href="javascript: openMQXml(\'' + value + '\')">' + value + '</a>';
    } else {
      return '<span title="No XML saved for this Unique ID">'+value+'</span>';
    }
  }

  function requestIdFormatter(value, index){
    return '<a title="Open Request Details for ' + value + '" href="${contextPath}/request/'+value+'">' + value + '</a>';
  }
  function reqTypeFormatter(value, index){
    if (value == 'C'){
      return 'Create';
    }
    if (value == 'U'){
      return 'Update';
    }
    return value + '(no description)';
  }
  
  function notifyFormatter(value, index){
    if (value == 'Y'){
      return 'Yes';
    }
    return '';
  }

  function actionsFormatter(value, index){
    var actions = '';
    var rowData = this.grid.getItem(index);
    var type = rowData.reqType[0];
    var status = rowData.reqStatus[0];
    var warn = rowData.warning[0];
    var qId = rowData.queryReqId[0];
    var adminStatus = rowData.adminStatus[0];
    if (adminStatus == 'PCR' && (status == 'PUB1' && type == 'C')){
      actions += '<img src="'+cmr.CONTEXT_ROOT+'/resources/images/refresh.png" class="action" title="Resend the request" onclick="resendRequest('+qId+')">';
    } 
    if (adminStatus == 'PCR' && status.includes('PUB') && type == 'U'){
      actions += '<img src="'+cmr.CONTEXT_ROOT+'/resources/images/refresh.png" class="action" title="Resend the request" onclick="resendRequest('+qId+')">';
    } 
    if ((warn == 'Y' && status.includes('PUB')) || status.includes('WAIT')){
      actions += '<img src="'+cmr.CONTEXT_ROOT+'/resources/images/stop.png" class="action" title="Stop request processing" onclick="stopRequest('+qId+')">';
    }
    return actions;
  }
  
  function filterRecords(){
    var url = cmr.CONTEXT_ROOT + '/mqstatuslist.json';
    var params = 'reqStatus=:reqStatus&cmrIssuingCntry=:cmrIssuingCntry';
    var reqId = FormManager.getActualValue('reqId');
    if (reqId != '' && !isNaN(reqId)){
      params += '&reqId=:reqId';
    }
    var qId = FormManager.getActualValue('queryReqId');
    if (qId != '' && !isNaN(qId)){
      params += '&queryReqId=:queryReqId';
    }
    CmrGrid.refresh('mqStatusGrid', url, params);
  }
  
  function resetRecords(){
    FormManager.setValue('reqId', '');
    FormManager.setValue('queryReqId', '');
    FormManager.setValue('cmrIssuingCntry', '');
    FormManager.setValue('reqStatus', '');
    filterRecords();
  }
  
  function resendRequest(qId){
    if (confirm('Resending a request must only be done if the message interchange is lost and confirmed with the backend team. Proceed?')){
      processRequest(qId, 'RESEND');
    }
  }
  function stopRequest(qId){
    if (confirm('Stopping will end processing and set the request back in an unlocked state. Proceed?')){
      processRequest(qId, 'STOP');
    }
  }
  
  function processRequest(qId, action){
    cmr.showProgress('Processing...');
    dojo.xhrPost({
      url : cmr.CONTEXT_ROOT+'/code/mqstatus/process.json',
      handleAs : 'json',
      content : {
        queryReqId : qId,
        action : action
      },
      method : 'POST',
      timeout : 50000,
      load : function(data, ioargs) {
        cmr.hideProgress();
        if (data && data.success){
          cmr.showAlert('Request '+qId+(action == 'STOP' ? ' stopped ' : ' requeued ')+' successfully', null, null, true);
          filterRecords();
        } else {
          cmr.showAlert('An error has occurred. Message = '+data.msg);
        }
      },
      error : function(error, ioargs) {
        cmr.hideProgress();
        cmr.showAlert('An general error has occurred');
      }
    });
  }

  function openMQXml(uniqueId){
    WindowMgr.open('MQXML', uniqueId, 'mqxml?uniqueId=' + uniqueId, null, 550);
  }
  </script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/mqstatus/process" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="mqstatus" id="frmCMR">
    <cmr:model model="mqstatus" />
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8" addBackground="true">
        <cmr:column span="1" width="90">
          <p>
            <label>Request ID: </label>
          </p>
        </cmr:column>
        <cmr:column span="1" width="120">
          <p>
            <form:input path="reqId" dojoType="dijit.form.TextBox" cssStyle="width:100px" />
          </p>
        </cmr:column>
        <cmr:column span="1" width="70">
          <p>
            <label>Unique ID: </label>
          </p>
        </cmr:column>
        <cmr:column span="1" width="120">
          <p>
            <form:input path="queryReqId" dojoType="dijit.form.TextBox" cssStyle="width:100px" />
          </p>
        </cmr:column>
        <cmr:column span="1" width="90">
          <p>
            <label>Status: </label>
          </p>
        </cmr:column>
        <cmr:column span="1" width="120">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="reqStatus" searchAttr="name" style="display: inline-block;" maxHeight="120"
            required="false" path="reqStatus" placeHolder="Status">
            <option value=""></option>
            <option value="P">Not processed</option>
            <option value="C">Completed</option>
            <option value="E">In Error</option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="8" addBackground="true">
        <cmr:column span="1" width="90">
          <p>
            <label>Country: </label>
          </p>
        </cmr:column>
        <cmr:column span="1" width="350">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCntry" searchAttr="name" style="display: inline-block;width:330px" maxHeight="200"
            required="false" path="cmrIssuingCntry" placeHolder="CMR Issuing Country">
          </form:select>
          </p>
        </cmr:column>
        <cmr:column span="1" width="200">
          <p>
            <input type="button" value="Filter" onclick="filterRecords()">
            <input type="button" value="Reset" onclick="resetRecords()">
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>MQ Interface Queue Status</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/mqstatuslist.json" id="mqStatusGrid" span="6" height="400" hasCheckbox="false" usePaging="true" useFilter="true" >
            <cmr:gridCol width="70px" field="queryReqId" header="Unique ID" >
              <cmr:formatter functionName="uniqueIdFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="70px" field="reqId" header="Request ID" >
              <cmr:formatter functionName="requestIdFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="65px" field="cmrIssuingCntry" header="Country" >
              <cmr:formatter functionName="cntryFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="50px" field="reqType" header="Type" >
              <cmr:formatter functionName="reqTypeFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="70px" field="cmrNo" header="CMR No." />
            <cmr:gridCol width="90px" field="reqStatus" header="Queue Status" >
              <cmr:formatter functionName="statusFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="auto" field="adminStatusDesc" header="Request Status" >
              <cmr:formatter functionName="adminStatusFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="60px" field="targetSys" header="System" >
              <cmr:formatter functionName="systemFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="150px" field="createBy" header="Created By" />
            <cmr:gridCol width="60px" field="mqInd" header="Notified" >
              <cmr:formatter functionName="notifyFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="75px" field="createByTs" header="Actions" >
              <cmr:formatter functionName="actionsFormatter" />
            </cmr:gridCol>
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
      </cmr:row>
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" />
  </cmr:buttonsRow>
</cmr:section>
