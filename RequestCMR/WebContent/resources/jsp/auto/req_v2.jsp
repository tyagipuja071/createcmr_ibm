<%@page import="com.ibm.cio.cmr.request.model.auto.BaseV2RequestModel"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page
	import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  
  BaseV2RequestModel reqentry = (BaseV2RequestModel) request.getAttribute("reqentry");
  reqentry.setDplChkResult("Not Done"); 
  reqentry.setFindCmrResult("Not Done");
  reqentry.setFindDnbResult("Not Done");
  AppUser user = AppUser.getUser(request);
  PageManager.initFor(request, reqentry.getCmrIssuingCntry(), reqentry.getReqType());
  String mandt = SystemConfiguration.getValue("MANDT");
  String defaultLandedCountry = (String) request.getAttribute("defaultLandedCountry");
  String scenarioType = "CustomerScenarioType";
  String scenarioSubtype = "CustomerScenarioSubType";
  if ("897".equals(reqentry.getCmrIssuingCntry())){
    scenarioType = "CustomerGroup";
    scenarioSubtype = "CustomerSubGroup";
  }
%>
<script src="${resourcesPath}/js/cmr-pagemanager.js?${cmrv}"
	type="text/javascript"></script>
<cmr:view forGEO="US">
	<script src="${resourcesPath}/js/cmr-scenarios_us_la.js?${cmrv}"
		type="text/javascript"></script>
</cmr:view>
<cmr:view exceptForGEO="US">
	<script src="${resourcesPath}/js/cmr-scenarios.js?${cmrv}"
		type="text/javascript"></script>
</cmr:view>
<script src="${resourcesPath}/js/requestentry/comment.js?${cmrv}"
	type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/notify.js?${cmrv}"
	type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/attachment.js?${cmrv}"
	type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/geohandler.js?${cmrv}"
	type="text/javascript"></script>
<script src="${resourcesPath}/js/auto/automation.js?${cmrv}"
	type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/findcmrsearch.js?${cmrv}"
	type="text/javascript"></script>
<cmr:model model="reqentry" />
<style>
</style>
<script>

function afterConfigChange(){
  FormManager.readOnly('cmrIssuingCntry'); 
  FormManager.readOnly('reqType'); 
  if (_pagemodel.reqType == 'C'){
    FormManager.readOnly('cmrNo'); 
    document.getElementById("retrieve").style.display = "none";
  }
  _forceExecuteAfterConfigs = true;
  GEOHandler.executeAfterConfigs();
  FormManager.ready();
}

function enableSupportal(){
  var error = dojo.byId('cmr-error-box-msg') ? dojo.byId('cmr-error-box-msg').innerHTML : null;
  if (error){
    $('#supportal').slideDown(1000);
  }
}

</script>

<!-- Main Contents -->
<cmr:boxContent>
	<form:form method="POST" action="./auto/process" name="frmCMR"
		id="frmCMR" class="ibm-column-form ibm-styled-form"
		modelAttribute="reqentry">
		<input type="hidden" value="<%=mandt%>" id="mandt">
		<input type="hidden" value="<%=defaultLandedCountry%>"
			id="defaultLandedCountry">
		<form:hidden path="reqId" />
		<form:hidden path="dplChkResult" />
		<form:hidden path="findCmrResult" />
		<form:hidden path="findDnbResult" />
		<form:hidden path="custClass" />
		<form:hidden path="dupCmrReason"/>

		<cmr:tabs>
			<cmr:tab label="Request Details" id="MAIN_GENERAL_TAB" active="true"
				sectionId="GENERAL_REQ_TAB" gridIds="COMMENT_LIST_GRID" />
		</cmr:tabs>
		<!-- Fields start here -->

		<cmr:section id="GENERAL_REQ_TAB">
			<cmr:row>
				<cmr:column span="2">
					<p>
						<cmr:label fieldId="cmrIssuingCntry">
							<cmr:fieldLabel fieldId="CMRIssuingCountry" />: 
            </cmr:label>
						<cmr:field id="cmrIssuingCntry" path="cmrIssuingCntry"
							fieldId="CMRIssuingCountry" tabId="MAIN_GENERAL_TAB" />
					</p>
				</cmr:column>
				<cmr:column span="2">
					<p>
						<cmr:label fieldId="reqType">
							<cmr:fieldLabel fieldId="RequestType" />: 
            </cmr:label>
						<cmr:field id="reqType" path="reqType" fieldId="RequestType"
							tabId="MAIN_GENERAL_TAB" />
					</p>
				</cmr:column>
				<cmr:column span="2">
					<p>
						<cmr:label fieldId="cmrNo">
							<cmr:fieldLabel fieldId="CMRNumber" />: 
            </cmr:label>
						<cmr:field id="cmrNo" path="cmrNo" size="150" fieldId="CMRNumber"
							tabId="MAIN_GENERAL_TAB" />
						<br>
						<a id="retrieve" href="javascript: retrieveCMRData()">Retrieve</a>
					</p>
				</cmr:column>
			</cmr:row>
			<cmr:row>
				<cmr:column span="2" containerForField="RequestingLOB">
					<p>
						<cmr:label fieldId="requestingLob">
							<cmr:fieldLabel fieldId="RequestingLOB" />:
              <cmr:info text="${ui.info.requestingLOB}" />
						</cmr:label>
						<cmr:field fieldId="RequestingLOB" id="requestingLob"
							path="requestingLob" tabId="MAIN_GENERAL_TAB" />
					</p>
				</cmr:column>
				<cmr:column span="2" containerForField="RequestReason">
					<p>
						<cmr:label fieldId="reqReason">
							<cmr:fieldLabel fieldId="RequestReason" />:
            </cmr:label>
						<cmr:field fieldId="RequestReason" id="reqReason" path="reqReason"
							tabId="MAIN_GENERAL_TAB" />
					</p>
				</cmr:column>
			</cmr:row>

			<%if ("C".equals(reqentry.getReqType())){%>
			<cmr:row>
				<cmr:column span="2" containerForField="<%=scenarioType%>">
					<p>
						<cmr:label fieldId="custGrp">
							<cmr:fieldLabel fieldId="<%=scenarioType%>" />:
            </cmr:label>
						<cmr:field fieldId="<%=scenarioType%>" id="custGrp" path="custGrp"
							tabId="MAIN_GENERAL_TAB" />
					</p>
				</cmr:column>
				<cmr:column span="2" containerForField="<%=scenarioSubtype%>">
					<p>
						<cmr:label fieldId="custSubGrp">
							<cmr:fieldLabel fieldId="<%=scenarioSubtype%>" />:
            </cmr:label>
						<cmr:field fieldId="<%=scenarioSubtype%>" id="custSubGrp"
							path="custSubGrp" tabId="MAIN_GENERAL_TAB" />
					</p>
				</cmr:column>
			</cmr:row>
			<%}%>

			<!-- All specific countries are here -->
			<cmr:view forGEO="US">
				<jsp:include page="US/us_auto.jsp" />
			</cmr:view>
			<cmr:view forCountry="631">
				<jsp:include page="LA/br_auto.jsp" />
			</cmr:view>


			<cmr:row>
				<cmr:column span="2">
					<cmr:label fieldId="Comments">
            Comments:
          </cmr:label>
					<cmr:field fieldId="Comments" id="comment" path="comment"
						tabId="MAIN_GENERAL_TAB" rows="5" cols="58" />
				</cmr:column>
			</cmr:row>
			<cmr:row>
				<br>
			</cmr:row>

		</cmr:section>

		<jsp:include page="dup_cmr_chk.jsp" />
		<jsp:include page="dup_req_chk.jsp" />
	</form:form>
</cmr:boxContent>

<!-- Buttons -->
<cmr:section alwaysShown="true">
	<cmr:buttonsRow>
		<!-- <div style="float: left">
      <a style="font-size:12px" href="javascript: Automation.goToOldRequestPage()">Switch to Classic View</a>
    </div> -->
		<div style="float: right">
			<cmr:view>
				<cmr:button label="Submit Request"
					onClick="Automation.createRequest('frmCMR')" highlight="true" />
			</cmr:view>
		</div>
	</cmr:buttonsRow>
</cmr:section>




<%=PageManager.getScripts(request, false, true)%>
