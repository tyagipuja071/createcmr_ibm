<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/workflow/workflow.js?${cmrv}" type="text/javascript"></script>


<script>
var _wfgrid = 'requestResultGrid_GRID';
function requestResultGrid_GRID_onLoad(data){
  
  if (CmrGrid.GRIDS[_wfgrid]){
    console.log('override click');
    CmrGrid.GRIDS[_wfgrid].onClick = wfGridContext;
    CmrGrid.GRIDS[_wfgrid].onRowContextMenu = wfGridContext;
  }
  
}
</script>

<form:form method="POST" action="${contextPath}/workflow/results" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="requestSearchCriteriaModel">

  <form:hidden path="reqId" id="mainReqId" />

  <cmr:modelAction formName="frmCMR" />

  <form:hidden path="wfProcCentre" />
  <form:hidden path="customerName" />
  <form:hidden path="requestId" />
  <form:hidden path="requestStatus" />
  <form:hidden path="expediteChk" />
  <form:hidden path="createDateFrom" />
  <form:hidden path="lastActDateFrom" />
  <form:hidden path="createDateTo" />
  <form:hidden path="lastActDateTo" />
  <form:hidden path="resultRows" />
  <form:hidden path="searchCusType" />
  
  <form:hidden path="wfReqName" />
  <form:hidden path="wfReqId" />
  <form:hidden path="wfOrgName" />
  <form:hidden path="wfOrgId" />
  <form:hidden path="wfClaimByName" />
  <form:hidden path="wfClaimById" />
  <form:hidden path="cmrIssuingCountry" />
  <form:hidden path="cmrOwnerCriteria" />
  <form:hidden path="cmrNoCriteria" />
  <form:hidden path="requestType" />
  <form:hidden path="procStatus" />
  <form:hidden path="processedBy" />
  <form:hidden path="processedByName" />
  <form:hidden path="pendingAppr" />
  


  <cmr:boxContent>

    <cmr:tabs>
      <cmr:tab label="${ui.tab.criteria}" id="CRITERIA_TAB" sectionId="" onClick="window.location='${contextPath}/workflow/search'" />
      <cmr:tab label="${ui.tab.result}" id="RESULT_TAB" sectionId="" onClick="doSearchRequests()" active="true" />
    </cmr:tabs>

    <cmr:section id="GRIDSECTION">
      <br>
      <cmr:grid url="/workflow/search/results/list.json" id="requestResultGrid" useFilter="true">
        <cmr:gridParam fieldId="wfProcCentre" value="${requestSearchCriteriaModel.wfProcCentre}" />
        <cmr:gridParam fieldId="customerName" value="${requestSearchCriteriaModel.customerName}" />
        <cmr:gridParam fieldId="requestId" value="${requestSearchCriteriaModel.requestId}" />
        <cmr:gridParam fieldId="requestStatus" value="${requestSearchCriteriaModel.requestStatus}" />
        <cmr:gridParam fieldId="expediteChk" value="${requestSearchCriteriaModel.expediteChk}" />
        <cmr:gridParam fieldId="resultRows" value="${requestSearchCriteriaModel.resultRows}" />
        <cmr:gridParam fieldId="createDateFrom" value="${requestSearchCriteriaModel.createDateFrom}" />
        <cmr:gridParam fieldId="lastActDateFrom" value="${requestSearchCriteriaModel.lastActDateFrom}" />
        <cmr:gridParam fieldId="createDateTo" value="${requestSearchCriteriaModel.createDateTo}" />
        <cmr:gridParam fieldId="lastActDateTo" value="${requestSearchCriteriaModel.lastActDateTo}" />
        <cmr:gridParam fieldId="resultRows" value="${requestSearchCriteriaModel.resultRows}" />
        <cmr:gridParam fieldId="searchCusType" value="${requestSearchCriteriaModel.searchCusType}" />
        
        <cmr:gridParam fieldId="wfReqName" value="${requestSearchCriteriaModel.wfReqName}" />
        <cmr:gridParam fieldId="wfReqId" value="${requestSearchCriteriaModel.wfReqId}" />
        <cmr:gridParam fieldId="wfOrgName" value="${requestSearchCriteriaModel.wfOrgName}" />
        <cmr:gridParam fieldId="wfOrgId" value="${requestSearchCriteriaModel.wfOrgId}" />
        <cmr:gridParam fieldId="wfClaimByName" value="${requestSearchCriteriaModel.wfClaimByName}" />
        <cmr:gridParam fieldId="wfClaimById" value="${requestSearchCriteriaModel.wfClaimById}" />
        <cmr:gridParam fieldId="cmrIssuingCountry" value="${requestSearchCriteriaModel.cmrIssuingCountry}" />
        <cmr:gridParam fieldId="cmrOwnerCriteria" value="${requestSearchCriteriaModel.cmrOwnerCriteria}" />
        <cmr:gridParam fieldId="cmrNoCriteria" value="${requestSearchCriteriaModel.cmrNoCriteria}" />
        <cmr:gridParam fieldId="requestType" value="${requestSearchCriteriaModel.requestType}" />
        <cmr:gridParam fieldId="procStatus" value="${requestSearchCriteriaModel.procStatus}" />
        <cmr:gridParam fieldId="processedBy" value="${requestSearchCriteriaModel.processedBy}" />
        <cmr:gridParam fieldId="processedByName" value="${requestSearchCriteriaModel.processedByName}" />
        <cmr:gridParam fieldId="pendingAppr" value="${requestSearchCriteriaModel.pendingAppr}" />
        


        <cmr:gridCol width="80px" field="reqId" header="${ui.grid.requestID}" align="right">
          <cmr:formatter functionName="requestIdFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="90px" field="expediteInd" header="${ui.grid.expedite}" align="center">
          <cmr:formatter functionName="expediteFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="60px" field="cmrOwnerDesc" header="${ui.grid.cmrOwner}" />
        <cmr:gridCol width="75px" field="cmrIssuingCntry" header="${ui.grid.cmrIssuingCntry}" >
         <cmr:formatter functionName="countryFormatter" />
        </cmr:gridCol>
         <cmr:gridCol width="70px" field="reqTypeText" header="${ui.grid.requestType}" >
           <cmr:formatter functionName="reqTypeFormatter"/>
         </cmr:gridCol>
        <cmr:gridCol width="120px" field="custName" header="${ui.grid.customerName}" >
          <cmr:formatter functionName="wfNameFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="80px" field="cmrNo" header="${ui.grid.cmrNo}" >
          <cmr:formatter functionName="cmrNoFormatter" />
        </cmr:gridCol> 
        <cmr:gridCol width="130px" field="overallStatus" header="${ui.grid.requestStatus}">
          <cmr:formatter functionName="overallStatusFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="105px" field="claimField" header="${ui.grid.claim}" >
          <cmr:formatter functionName="claimFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="80px" field="pendingAppr" header="${ui.grid.pendingAppr}">
          <cmr:formatter functionName="pendingApprFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="120px" field="createTsString" header="${ui.grid.createDate}" />
        <cmr:gridCol width="120px" field="lastUpdtTsString" header="${ui.grid.lastAction}" />
        <cmr:gridCol width="120px" field="requesterNm" header="${ui.grid.requesterNm}" />
        <cmr:gridCol width="120px" field="originatorNm" header="${ui.grid.originatorNm}" />
        <cmr:gridCol width="120px" field="processingStatus" header="${ui.grid.processingStatus}" />
        <cmr:gridCol width="200px" field="reqReason" header="${ui.grid.reqReason}" />
      </cmr:grid>
      <br>
    </cmr:section>

  </cmr:boxContent>
 


  <cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <cmr:button label="${ui.btn.createNewEntry}" onClick="cmr.chooseNewEntry()" highlight="true" pad="false"/>
      <cmr:button label="${ui.btn.searchagain}" onClick="window.location = '${contextPath}/workflow/search'" pad="true" />
      <cmr:button label="${ui.btn.openByRequestID}" onClick="openRequestById()" highlight="false" pad="true" />
    </cmr:buttonsRow>
  </cmr:section>
<input type="hidden" id="fromURL" name="fromUrl" value="" />
<input type="hidden" id="newReqCntry_h" name="newReqCntry" value="" />
<input type="hidden" id="newReqType_h" name="newReqType" value="" />

</form:form>
<cmr:model model="requestSearchCriteriaModel" />
<jsp:include page="../massrequestentry/file_dl.jsp" />