<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="com.ibm.cio.cmr.request.util.SystemParameters"%>
<%@page import="com.ibm.cio.cmr.request.ui.template.TemplateManager"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.DataModel"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
	DataModel rdcdata = (DataModel) request.getAttribute("rdcdata");
	boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
  boolean dnbPrimary = "Y".equals(request.getAttribute("dnbPrimary"));
	AppUser user = AppUser.getUser(request);
	String findCMRUrl = SystemConfiguration.getValue("FIND_CMR_URL");
	String actionUrl = request.getContextPath() + "/request";
			if (!newEntry) {
				actionUrl += "/" + reqentry.getReqId();
			}
	PageManager.initFor(request, reqentry);
	String tabsJS = newEntry ? "triggerSave" : "switchTabs";
	String mandt = SystemConfiguration.getValue("MANDT");
	String autoEngineIndc= (String) request.getAttribute("autoEngineIndc");
  boolean fromQs = "Y".equals(request.getParameter("qs"));
  String findCmrServer = findCMRUrl.contains("/") ? findCMRUrl.substring(0, findCMRUrl.lastIndexOf("/")) : findCMRUrl;
%>
<jsp:include page="approvals_status.jsp" />
<%
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
		if (readOnly == null) {
			readOnly = false;
			request.setAttribute("pageReadOnly", true);
		}
	String defaultLandedCountry = (String) request.getAttribute("defaultLandedCountry");
%>
<script src="${resourcesPath}/js/cmr-pagemanager.js?${cmrv}" type="text/javascript"></script>
<cmr:view forGEO="US">
  <script src="${resourcesPath}/js/cmr-scenarios_us_la.js?${cmrv}" type="text/javascript"></script>
</cmr:view>
<cmr:view exceptForGEO="US">
  <script src="${resourcesPath}/js/cmr-scenarios.js?${cmrv}" type="text/javascript"></script>
</cmr:view>
<script src="${resourcesPath}/js/cmr-services.js?${cmrv}" type="text/javascript"></script>

<!--  Main Scripts -->
<script src="${resourcesPath}/js/requestentry/requestentry.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/comment.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/address.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/tgmeaddrstd.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/notify.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/attachment.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/findcmrsearch.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/approvals.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/auto/automation.js?${cmrv}" type="text/javascript"></script>

<!--  Validators per country. Add to the validations jsp -->
<jsp:include page="validations_js.jsp" />

<jsp:include page="trans.jsp" />

<script>
    _findCmrServer = '<%=findCmrServer%>';
    var _translateUrl = '<%=SystemParameters.getString("TRANSLATE.URL")%>';
    var _delayedLoadComplete = false;
  dojo.addOnLoad(function() {
    loadYourActionsDropDown();
    FormManager.setCheckFunction(promptForSaveBeforeLeave);
    FilteringDropdown.loadItems('rejectReason', 'rejectReason_spinner', 'lov', 'fieldId=RejectReasonProc');
    addCMRSearchHandler('${yourActionsViewOnly}' == 'true');
    cmr.currentTab = 'GENERAL_REQ_TAB';
    
    <%if ("Y".equals(reqentry.getDelInd())) {
				if (null != reqentry.getCmrIssuingCntry() && "897".equals(reqentry.getCmrIssuingCntry())) {%>
       FormManager.readOnly('custGrp');
       FormManager.readOnly('custSubGrp');
    <%}%>
    <%}%>
    delayLoadTemplate();
    enableYourActionsBar();
    <%if (null != reqentry.getCmrIssuingCntry() && ("852".equals(reqentry.getCmrIssuingCntry()) || "720".equals(reqentry.getCmrIssuingCntry()) || "738".equals(reqentry.getCmrIssuingCntry()) || "736".equals(reqentry.getCmrIssuingCntry()) || "646".equals(reqentry.getCmrIssuingCntry()) || "714".equals(reqentry.getCmrIssuingCntry()))) {%>
    getChecklistStatus();
    <%}%>    
    <%if (fromQs && "C".equals(reqentry.getReqType())){ %>
    // && (!"758".equals(reqentry.getCmrIssuingCntry())
    var cmrIssuingCntry = '<%= reqentry.getCmrIssuingCntry() %>';
    var cmrIssuingCntryArray = [ '758', '897' ];
    if (!cmrIssuingCntryArray.includes(cmrIssuingCntry)) {
      cmr.showProgress('Check and verify address created.<br>Please wait while the system opens the address...');
      window.setTimeout('forceAddressValidationFromQS()', 1000);
    }
    <%}%>
  });
  
  function forceAddressValidationFromQS(){
    var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
    if (FilteringDropdown.pending() || !_allAddressData || _allAddressData.length == 0){
      window.setTimeout('forceAddressValidationFromQS()', 500);
    }else if (cmrCntry == '755'){
      //CREATCMT-7670 
      var ctyaSeq = '6';
      for (var i = 0; i < _allAddressData.length; i++){
        if (_allAddressData[i].addrType == 'CTYA'){
          ctyaSeq = _allAddressData[i].addrSeq[0];
          break;
        }
      }
      cmr.hideProgress();
      doUpdateAddr(FormManager.getActualValue('reqId'),'CTYA', ctyaSeq, cmr.MANDT, true);
    } else {
      var soldToSeq = '1';
      for (var i = 0; i < _allAddressData.length; i++){
        if (_allAddressData[i].addrType == 'ZS01'){
          soldToSeq = _allAddressData[i].addrSeq[0];
          break;
        }
      }
      cmr.hideProgress();
      doUpdateAddr(FormManager.getActualValue('reqId'),'ZS01', soldToSeq, cmr.MANDT, true);
    }
  }
  
  function enableSupportal(){
    var error = dojo.byId('cmr-error-box-msg') ? dojo.byId('cmr-error-box-msg').innerHTML : null;
    if (error){
      $('#supportal').slideDown(1000);
    }
  }

  function enableQSPopup(){
    if (FormManager.getActualValue('dnbPrimary') == 'Y' && FormManager.getActualValue('reqId') == '0' && FormManager.getActualValue('reqType') == 'C'){
      $('#qs_pop').slideDown(1000);
    }
  }

  function enableYourActionsBar(){
    if (!FilteringDropdown.pending()){
      $('#cmr-your-actions').slideDown(500);
      enableSupportal();
      enableQSPopup();
    }  else {
      window.setTimeout('enableYourActionsBar()', 1500);      
    }
  }
  
  function delayLoadTemplate(){
    if (FormManager.getActualValue('custSubGrp') != '' && !FilteringDropdown.pending()){
      console.log('delayed load of template..');
      
      if (!TemplateService.initLoaded()){
        TemplateService.fill('reqentry');
      } else {
        console.log('already loaded once..');
      }
      <%if ("Y".equals(reqentry.getDelInd())) {%>
        FormManager.readOnly('custGrp');
        FormManager.readOnly('custSubGrp');
      <%}%>
      _delayedLoadComplete = true;
    } else {
      if ((_pagemodel.custSubGrp && _pagemodel.custSubGrp != '')){
        window.setTimeout('delayLoadTemplate()', 1500);      
      } else {
        _delayedLoadComplete = true;
      }
    }
  }
  
  function promptForSaveBeforeLeave(url){
    var readOnly = false;
    if ('${yourActionsViewOnly}' == 'true'){ 
      readOnly = true;
    }
    if (!readOnly){
      cmr.urlRedirect = url;
      cmr.showConfirm('saveBeforeLeave()','Unsaved data for the current request will be lost.  Do you want to Save before leaving this request?', null, 'noSaveBeforeLeave()', {OK : 'Save', CANCEL: 'No Save'});
    } else {
      dojo.cookie('lastTab', '', {expires: -1});
      window.location = url;
    }
  }
  

  function saveBeforeLeave(){
    var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
    var reqType = FormManager.getActualValue('reqType');
    if (cmrCntry == '' || reqType == ''){
      cmr.showAlert('CMR Issuing Country and Request Type must be specified in order for you to save the request.');
      return;
    }
    var url = cmr.urlRedirect;
    if (url.indexOf('${contextPath}') == 0){
      url = url.substring('${contextPath}'.length);
    }
    dojo.byId('redirectUrl').value = url;
    dojo.cookie('lastTab', '', {expires: -1});
    var cb = dojo.query('[type=checkbox]');
    for (var i = 0; i < cb.length; i++) { 
      if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled){
        cb[i].disabled = false;
        cb[i].removeAttribute('disabled');
      }
    }
    FormManager.doAction('frmCMR', YourActions.Save, true);
  }
  
  function noSaveBeforeLeave(){
    dojo.cookie('lastTab', '', {expires: -1});
    window.location = cmr.urlRedirect;
  }
  
  function loadYourActionsDropDown(){
    if ('${yourActionsViewOnly}' == 'true'){ 
      cmr.showNode('viewOnlyText');
    }
    PageManager.setAlwaysAvailable(['yourAction']);
    
    var paramString = 'reqstatus=${reqentry.reqStatus}&lockind=${yourActionsLockInd}&reqtype=${reqentry.reqType}';
    paramString += '&sepvalind=' + ('${reqentry.sepValInd}'.trim() == '' ? 'N' : '${reqentry.sepValInd}');
    paramString += '&disableautoproc=' + ('${reqentry.disableAutoProc}'.trim() == '' ? 'N' : '${reqentry.disableAutoProc}');
    FilteringDropdown.loadItems('yourAction', 'yourAction_spinner', '${yourActionsSqlId}', paramString, false, '${ui.info.noneavailable}');
  }
  function getChecklistStatus() {
    console.log('validating checklist..');
    var checklist = dojo.query('table.checklist');
    document.getElementById("checklistStatus").innerHTML="Not Done";
    var reqId = FormManager.getActualValue('reqId');

    var localNm = checklist.query('input[name="localCustNm"]');
    var localAddr = checklist.query('input[name="localAddr"]');
    var questions = checklist.query('input[type="radio"]');
    
    if (reqId != null && reqId.length > 0 && reqId != 0) {
      if (localNm.length > 0 && localNm[0].value.trim() == '') {
        document.getElementById("checklistStatus").innerHTML="Incomplete";
        FormManager.setValue('checklistStatus', "Incomplete");
      }else if (localAddr.length > 0 && localAddr[0].value.trim() == '') {
        document.getElementById("checklistStatus").innerHTML="Incomplete";
        FormManager.setValue('checklistStatus', "Incomplete");
      }else if (questions.length > 0) {
        var noOfQuestions = questions.length / 2;
        var checkCount = 0;
        for ( var i = 0; i < questions.length; i++) {
          if (questions[i].checked) {
            checkCount++;
          }
        }
        if (noOfQuestions != checkCount) {
          document.getElementById("checklistStatus").innerHTML="Incomplete";
          FormManager.setValue('checklistStatus', "Incomplete");
        }else{
         document.getElementById("checklistStatus").innerHTML="Complete";
         FormManager.setValue('checklistStatus', "Complete");
        }
      }else {
      document.getElementById("checklistStatus").innerHTML="Complete";
      FormManager.setValue('checklistStatus', "Complete");
     }
   }
  }
</script>
<style>
div.cmr-tabs {
  margin-top: 20px;
}

div#ibm-content-main {
  padding-top: 20px;
}

div#cmr-info-box, div#cmr-error-box, div#cmr-validation-box {
  padding-top: 15px !important;
}

</style>
<cmr:model model="reqentry" />
<cmr:boxContent>
  <cmr:form method="POST" action="<%=actionUrl%>" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="reqentry">
    <jsp:include page="actionstatus.jsp" />
    <input type="hidden" value="${yourActionsViewOnly}" id="viewOnlyPage">
    <input type="hidden" value="${dnbPrimary}" id="dnbPrimary">
    <input type="hidden" value="<%=mandt%>" id="mandt">
    <input type="hidden" value="<%=defaultLandedCountry%>" id="defaultLandedCountry">
    <input type="hidden" value="<%=autoEngineIndc%>" id="autoEngineIndc">
    <cmr:modelAction formName="frmCMR" />
    <form:hidden path="fromUrl" id="fromUrl" />
    <form:hidden path="userRole" />
    <form:hidden id="overallStatus" path="overallStatus" />
    <form:hidden id="lockByNm" path="lockByNm" />
    <form:hidden path="claimRole" />
    <form:hidden path="redirectUrl" />
    <form:hidden path="saveRejectScore" />
    <form:hidden path="hasError" />
    <form:hidden path="dplMessage" />
	
    <%
      if (!newEntry) {
    %>
    <form:hidden path="createTs" />
    <form:hidden path="mainAddrType" />
    <form:hidden path="reqStatus" />
    <form:hidden path="requesterId" />
    <form:hidden path="requesterNm" />
    <form:hidden path="lastUpdtTs" />
    <form:hidden path="lastUpdtBy" />
    <form:hidden path="processedFlag" />
    <form:hidden path="compVerifiedIndc" />
    <form:hidden path="scenarioVerifiedIndc" />
    <form:hidden path="compInfoSrc" />
    <form:hidden path="matchIndc" />
    <form:hidden path="matchOverrideIndc" />
    <c:if test="${reqentry.processedTs != null }">
      <form:hidden path="processedTs" />
    </c:if>
    <form:hidden path="internalTyp" />
    <form:hidden path="sepValInd" />
    <form:hidden path="rdcProcessingStatus" />
    <c:if test="${reqentry.rdcProcessingTs != null }">
      <form:hidden path="rdcProcessingTs" />
    </c:if>
    <form:hidden path="covBgRetrievedInd" />
    <form:hidden path="rdcProcessingMsg" />
    <cmr:view exceptForCountry="848,618,724,706,897,619,621,627,647,640,759,791,839,843,859">
    	<form:hidden path="ordBlk" />
    </cmr:view>
    <cmr:view forGEO="LA">
    <form:hidden path="crosTyp" />
    <form:hidden path="crosSubTyp" />
    </cmr:view>
    <cmr:view forCountry="613,629,655,661,663,681,683,829,731,735,781,799,811,813,815,869,871">
      <form:hidden path="taxCd2" />
      <form:hidden path="salesTeamCd" />
      <form:hidden path="installTeamCd" />
    </cmr:view>
    <cmr:view forCountry="663,681">
    	<form:hidden path="govType" />
    </cmr:view>
    <cmr:view forCountry="866,754">
  		<form:hidden path="acAdminBo"/>
  	</cmr:view>
    
    <%
      if (CmrConstants.YES_NO.Y.toString().equals(
    								reqentry.getLockInd())) {
    %>
    <form:hidden path="lockTs" />
    <form:hidden path="lockInd" />
    <form:hidden path="lockBy" />
    <%
      }
    %>
    <%
      }
    %>
    <c:if test="${reqentry.dplChkTs != null }">
      <form:hidden path="dplChkTs" />
    </c:if>
    <form:hidden path="dplChkResult" />
    <form:hidden path="dplChkUsrId" />
    <form:hidden path="dplChkUsrNm" />

    <form:hidden path="findCmrUsrId" />
    <form:hidden path="findCmrUsrNm" />
    <form:hidden path="findCmrRejReason" />
    <form:hidden path="findCmrRejCmt" />
    <form:hidden path="findCmrDate" />
    <c:if test="${reqentry.findCmrTs != null }">
      <form:hidden path="findCmrTs" />
    </c:if>

    <form:hidden path="findDnbUsrId" />
    <form:hidden path="findDnbUsrNm" />
    <form:hidden path="findDnbRejReason" />
    <form:hidden path="findDnbRejCmt" />
    <form:hidden path="findDnbDate" />
    <c:if test="${reqentry.findDnbTs != null }">
      <form:hidden path="findDnbTs" />
    </c:if>
    
    <cmr:view exceptForGEO="IERP,CND,CN,JP,SWISS,NORDX" exceptForCountry="706,618,862,780,866,754,644,668,693,704,708,740,820,821,826,358,359,363,603,607,626,651,694,695,699,705,707,787,741,889,838,620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,755,897,649">
    <form:hidden path="custClass" />
    </cmr:view>
    
    <%-- 
    <cmr:view forGEO="CN">
    <c:if test="${reqentry.userRole == 'Requester' }">
    <form:hidden path="custClass" />
    </c:if>
    </cmr:view>
    --%>
    <form:hidden path="oldCustNm1" />
    <form:hidden path="oldCustNm2" />
    <form:hidden path="delInd" />
    <cmr:view exceptForCountry="649">
    	<form:hidden path="modelCmrNo"/>
    </cmr:view>
    <form:hidden path="approvalResult" />
    
    
	<form:hidden path="dupCmrReason"/>

    
    <cmr:view forCountry="760">
      <form:hidden path="custType" />
    </cmr:view>
    
    <cmr:view forGEO="LA">
      <form:hidden path="embargoCd"/>
    </cmr:view>
    
    <cmr:view exceptForCountry="758">
    	<form:hidden path="hwSvcsRepTeamNo" />
    </cmr:view>
      <form:hidden path="paygoProcessIndc" />
	
    <%-- Canada Handling --%>
    <cmr:view exceptForCountry="649">
      <form:hidden path="invoiceDistCd" />
      <form:hidden path="cusInvoiceCopies" />
    </cmr:view>
    
    <cmr:view>
   <form:hidden path="sourceSystId" />
   </cmr:view>
    
    <!-- Your Actions Dropdown -->
    <div title="Your Actions" id="cmr-your-actions" class="cmr-actions ${yourActionsViewOnly == true ? " view-only" : " cmr-actions-locked"}" style="display: none">
      <c:if test="${sourceSystem != null }">
      <div class="cmr-source-sys-txt"><span class="cmr-source-sub">Source:</span> ${sourceSystem}</div>
      </c:if>
      <div class="cmr-action-dd">
        <form:select cssStyle="width:260px" dojoType="dijit.form.FilteringSelect" id="yourAction" searchAttr="name" style="display: inline-block;"
          maxHeight="200" required="false" path="yourAction" placeHolder="${ui.yourAction}" />
        <img title="Proceed with the selected Action" class="cmr-proceed-icon" src="${resourcesPath}/images/play.webp" onclick="processRequestAction()">
      </div>
      <div class="cmr-action-txt" id="viewOnlyText" style="display: none">View Only</div>
      <div class="cmr-action-txt" id="superUserModeText" style="display: none">SUPER USER MODE</div>
    </div>
    <script>
      addMoveHandler();
    </script>

    <%
      if (!"Viewer".equals(reqentry.getUserRole()) && CmrConstants.APPROVAL_RESULT_PENDING.equals(reqentry.getApprovalResult())) {
    %>
    <br>
    <cmr:row>
      <cmr:column span="6">
        <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
        <cmr:note text="${ui.info.pendingApproval}" />
      </cmr:column>
    </cmr:row>
    <br>
    <%
      }
    %>

    <%
      if ("Requester".equals(reqentry.getUserRole()) && CmrConstants.APPROVAL_RESULT_COND_APPROVED.equals(reqentry.getApprovalResult())) {
    %>
    <cmr:row>
      <cmr:column span="6">
        <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
        <cmr:note text="${ui.info.condApproved}" />
      </cmr:column>
    </cmr:row>
    <br>
    <%
      }
    %>   
     <%
      if ("Requester".equals(reqentry.getUserRole()) && CmrConstants.APPROVAL_RESULT_COND_CANCELLED.equals(reqentry.getApprovalResult())) {
    %>
    <cmr:row>
      <cmr:column span="6">
        <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
        <cmr:note text="${ui.info.condCancelleds}" />
      </cmr:column>
    </cmr:row>
    <br>
    <%
      }
    %>
    
    <cmr:view forGEO="LA">
    <%if ("Y".equals(reqentry.getEmbargoCd())){%>
     <cmr:row>
        <cmr:column span="6">
          <div class="embargo">
            <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
            <cmr:note text="${ui.infor.denialCode}" />
          </div>
        </cmr:column>
      </cmr:row>
    <%} %>
	</cmr:view>
	
    <cmr:view forGEO="CEMEA" exceptForCountry="618">
    <%if (!StringUtils.isEmpty(reqentry.getEmbargoCd())){%>
      <cmr:row>
        <cmr:column span="6">
          <div class="embargo">
            <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
            <cmr:note text="${ui.info.embargoCode}" />
          </div>
        </cmr:column>
      </cmr:row>
    <%} %>
    </cmr:view>
    <cmr:view forCountry="618">
    <%if (!StringUtils.isEmpty(reqentry.getOrdBlk()) && "88".equals(reqentry.getOrdBlk()) && "U".equals(reqentry.getReqType())){%>
      <cmr:row>
        <cmr:column span="6">
          <div class="embargo">
            <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
            <cmr:note text="${ui.info.cob}" />
          </div>
        </cmr:column>
      </cmr:row>
    <%} %>
    </cmr:view>
    <!-- CREATCMR-3298 US -->
    <cmr:view forCountry="897">
      <p></p>
      <div id="sccWarn" style="display:none">
      <cmr:row>
        <cmr:column span="6">
          <div class="embargo" >
            <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
            <cmr:note text="Unavailable - Undefined SCC value." />
          </div>
        </cmr:column>
      </cmr:row>
      </div>
      <div style="height: 5px"></div>
      <div id="sccMultipleWarn" style="display:none">
      <cmr:row>
        <cmr:column span="6">
          <div class="embargo" >
            <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
            <cmr:note text="multiple counties are mapped to the SCC(State / County / City)." />
          </div>
        </cmr:column>
      </cmr:row>
      </div>
    </cmr:view>
    <!-- CREATCMR-3298 US -->
    <cmr:view forGEO="AP">
    <%if ("Y".equals(reqentry.getGovType())){%>
      <cmr:row>
        <cmr:column span="6">
          <div class="govIndc">
            <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
            <cmr:note text="${ui.info.govTypeIndc}" />
          </div>
        </cmr:column>
      </cmr:row>
    <%} %>
    </cmr:view>
    <br>
      <cmr:view>
    <%if (!StringUtils.isEmpty(reqentry.getPaygoProcessIndc()) && "Y".equals(reqentry.getPaygoProcessIndc()) && reqentry.getUserRole() != null && reqentry.getUserRole().equalsIgnoreCase("PROCESSOR")){%>
      <cmr:row>
        <cmr:column span="6">
          <div class="paygoIndc">
            <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
            <cmr:note text="${ui.info.paygo}" />
          </div>
        </cmr:column>
      </cmr:row>
    <%} %>
    </cmr:view>
    <!-- Tabs section -->
    <cmr:tabs>
      <cmr:tab label="${ui.tab.general}" id="MAIN_GENERAL_TAB" active="true" sectionId="GENERAL_REQ_TAB" gridIds="COMMENT_LIST_GRID" />
      <cmr:tab label="${ui.tab.address}" id="MAIN_NAME_TAB" sectionId="NAME_REQ_TAB" gridIds="ADDRESS_GRID" />
      <cmr:tab label="${ui.tab.customer}" id="MAIN_CUST_TAB" sectionId="CUST_REQ_TAB" />
      <cmr:tab label="${ui.tab.ibm}" id="MAIN_IBM_TAB" sectionId="IBM_REQ_TAB" />
      <cmr:view forGEO="LA">
        <cmr:tab label="${ui.tab.contactInfo}" id="MAIN_CONTACTINFO_TAB" sectionId="CONTACTINFO_REQ_TAB" gridIds="CONTACTINFO_GRID" />
      </cmr:view>
      <cmr:view forCountry="852,720,834,738,736,714,646,358,359,363,607,620,626,651,675,677,680,694,695,713,741,752,762,767,768,772,787,805,808,821,823,832,849,850,865,889,641,766,858,755">
        <cmr:tab label="${ui.tab.checklist}" id="MAIN_CHECKLIST_TAB" sectionId="CHECKLIST_TAB" />
      </cmr:view>
      <cmr:tab label="${ui.tab.attach}" id="MAIN_ATTACH_TAB" sectionId="ATTACH_REQ_TAB" gridIds="ATTACHMENT_GRID" />
      <cmr:tab label="${ui.tab.notify}" id="MAIN_NOTIFY_TAB" sectionId="NOTIFY_REQ_TAB" gridIds="NOTIFY_LIST_GRID" />
      <cmr:view forGEO="LA">
        <cmr:tab label="${ui.tab.taxinfo}" id="MAIN_TAXINFO_TAB" sectionId="TAXINFO_REQ_TAB" gridIds="TAXINFO_GRID" />
      </cmr:view>
      <%
        if (!readOnly) {
      %>
      <cmr:tab label="${ui.tab.validations}" id="MAIN_VALIDATIONS_TAB" onClick="switchTabs('VALIDATIONS_REQ_TAB')" sectionId="VALIDATIONS_REQ_TAB" />
      <%
        }
      %>
      <cmr:tab label="${ui.tab.approvals}" id="MAIN_APPROVALS_TAB" sectionId="APPROVALS_REQ_TAB" gridIds="APPROVALS_GRID" />
    </cmr:tabs>

    <jsp:include page="general.jsp" />
    <%
      if (!newEntry) {
    %>
    <jsp:include page="address.jsp" />
    <jsp:include page="customer.jsp" />
    <cmr:view exceptForGEO="AP"><jsp:include page="ibm.jsp" /></cmr:view>
    <cmr:view forGEO="AP"><jsp:include page="AP/hk_mo_ibm.jsp" /></cmr:view>
    <jsp:include page="checklist.jsp" />
    <jsp:include page="attachment.jsp" />
    <jsp:include page="searchrejinfo_cmr.jsp" />
    <jsp:include page="searchrejinfo_dnb.jsp" />
    <jsp:include page="addressdetails.jsp" />
    <cmr:view forGEO="LA">
      <jsp:include page="taxinfo.jsp" />
      <jsp:include page="contactinfo.jsp"></jsp:include>
      <!-- !IMPORTANT : for address filtering  -->
      <form:hidden path="saveIndAftrTempLoad"/>
    </cmr:view>
    <%
      }
    %>

    <input type="hidden" name="statusChgCmt" id="statusChgCmt_main">
  </cmr:form>
  <%
    if (!newEntry) {
  %>
  <jsp:include page="notify.jsp" />
  <jsp:include page="addeditaddress.jsp" />
  <jsp:include page="applyaddress.jsp" />
  <jsp:include page="viewattachment.jsp" />
  <jsp:include page="addattachment.jsp" />
  <jsp:include page="addressstdresult.jsp" />
  <jsp:include page="addressstdrejreason.jsp" />
  <jsp:include page="addressstdgeneralrslt.jsp" />
  <jsp:include page="addressverification.jsp" />
  <jsp:include page="stdcity.jsp" />
  <jsp:include page="stdcityname.jsp" />
  <jsp:include page="dpldetails.jsp" />
  <jsp:include page="validationurl.jsp" />
  <jsp:include page="modals.jsp">
    <jsp:param value="E" name="isExisting"/>
  </jsp:include>
  <jsp:include page="dnbcheck.jsp" />
  <%
    }
  %>
  <%if (newEntry){ %>
  <jsp:include page="modals.jsp">
    <jsp:param value="N" name="isExisting"/>
  </jsp:include>
  <%}%>
  <jsp:include page="approvals.jsp" />
  <jsp:include page="approvalsAction.jsp" />
  <jsp:include page="approvalsComments.jsp" />
  <jsp:include page="templatevalue.jsp" />
 

</cmr:boxContent>

<%if (!newEntry) {%>
  <jsp:include page="commentLog.jsp" />
  <jsp:include page="dnbautocheck.jsp" />
<%} %>

<cmr:section alwaysShown="true">
<%if (user != null && !user.isApprover()){%>
  <cmr:buttonsRow>
    <%
      if (!newEntry) {
    %>
    <c:if test="${fn:trim(reqentry.fromUrl) != ''}">
      <div style="float: left">
        <cmr:button label="${ui.btn.backtowf}" onClick="goBackToWorkflow()" />
        <cmr:button label="${ui.btn.createNewEntry}" onClick="cmr.chooseNewEntry()" highlight="false" pad="true" />
        <%
          if (!readOnly) {
        %>
        <cmr:button label="${ui.btn.undoCurrentChanges}" onClick="undoCurrentChanges()" highlight="false" pad="true" />
        <%
          }
        %>
      </div>
      <div style="float: right">
        <cmr:button label="${ui.btn.openLog}" onClick="showChangeLog(${reqentry.reqId})" pad="true" />
        <cmr:button label="${ui.btn.openHistory}" onClick="openWorkflowHistory(${reqentry.reqId})" highlight="true" pad="true" />
      </div>
    </c:if>
    <c:if test="${fn:trim(reqentry.fromUrl) == ''}">
      <div style="float: left">
        <cmr:button label="${ui.btn.createNewEntry}" onClick="cmr.chooseNewEntry()" highlight="false" pad="false" />
        <%
          if (!readOnly) {
        %>
        <cmr:button label="${ui.btn.undoCurrentChanges}" onClick="undoCurrentChanges()" highlight="false" pad="true" />
        <%
          }
        %>
      </div>
      <div style="float: right">
        <cmr:button label="${ui.btn.openLog}" onClick="showChangeLog(${reqentry.reqId})" pad="true" />
        <cmr:button label="${ui.btn.openHistory}" onClick="openWorkflowHistory(${reqentry.reqId})" highlight="true" pad="true" />
      </div>
    </c:if>

    <%
      } else {
    %>
    <c:if test="${fn:trim(reqentry.fromUrl) != ''}">
      <cmr:button label="${ui.btn.backtowf}" onClick="goBackToWorkflow()" />
    </c:if>
    <%
      }
    %>
  </cmr:buttonsRow>
<%} else if (user != null && user.isApprover()){ %>
  <cmr:buttonsRow>
    <cmr:button label="Back to Approvals" onClick="window.location = '${contextPath}/myappr'" />
  </cmr:buttonsRow>
<%} %>
</cmr:section>
<jsp:include page="attach_dl.jsp" />
<jsp:include page="supportal.jsp" />
<%if (newEntry && "C".equals(reqentry.getReqType()) && dnbPrimary) {%>
  <jsp:include page="quick_search_popup.jsp" />
<%} %>
<jsp:include page="feedback.jsp" />

<!--  Customizability Scripts -->
<%=PageManager.getScripts(request, readOnly, newEntry)%>
<%=PageManager.generateTabScripts(request, "", "", "", true)%>
<!--  End Customizability Scripts -->