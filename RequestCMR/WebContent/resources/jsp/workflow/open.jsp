<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
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

<%
  AppUser user = AppUser.getUser(request);
%>
var _wfgrid = 'openRequestgrid_GRID';
var _wfrec = 0;
function openRequestgrid_GRID_onLoad(){
  var filter = dojo.cookie(_wfgrid+'_filter');
  try {
    if (filter != null){
      var filterJson = JSON.parse(filter);
      if (filterJson != null && filterJson.length > 0){
        CmrGrid.GRIDS[_wfgrid].setFilter(filterJson);
      }
    }
  } catch (e){
    dojo.cookie(_wfgrid+'_filter', '', {expires: -1});
  }
  window.setTimeout('CmrGrid.GRIDS[_wfgrid].scrollToRow(_wfrec)', 1200);
  
  if (CmrGrid.GRIDS[_wfgrid]){
    console.log('override click');
    CmrGrid.GRIDS[_wfgrid].onClick = wfGridContext;
    CmrGrid.GRIDS[_wfgrid].onRowContextMenu = wfGridContext;
  }
}
</script>
<form:form method="POST" action="${contextPath}/workflow/open" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="wfreq">

  <form:hidden path="reqId" id="mainReqId" />

  <cmr:modelAction formName="frmCMR" />
  
  <cmr:boxContent>

    <cmr:tabs />

    <cmr:section id="GRIDSECTION">
      <br>
      <cmr:grid url="/workflow/open/list.json" id="openRequestgrid" useFilter="true">
        <cmr:gridCol width="80px" field="reqId" header="${ui.grid.requestID}" align="right">
          <cmr:formatter functionName="requestIdFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="90px" field="expediteInd" header="${ui.grid.expedite}" align="center">
          <cmr:formatter functionName="expediteFormatter" />
        </cmr:gridCol>
       
        
        <cmr:gridCol width="75px" field="cmrIssuingCntry" header="${ui.grid.cmrIssuingCntry}" >
        <cmr:formatter functionName="countryFormatter" />
        </cmr:gridCol>
         <cmr:gridCol width="75px" field="reqTypeText" header="${ui.grid.requestType}" >
           <cmr:formatter functionName="reqTypeFormatter"/>
         </cmr:gridCol>
        
        <cmr:gridCol width="100px" field="custName" header="${ui.grid.customerName}" >
          <cmr:formatter functionName="wfNameFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="80px" field="cmrNo" header="${ui.grid.cmrNo}" >
          <cmr:formatter functionName="cmrNoFormatter" />
        </cmr:gridCol>        
        <cmr:gridCol width="130px" field="overallStatus" header="${ui.grid.requestStatus}" >
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
      </cmr:grid>
      <br>
    </cmr:section>


  </cmr:boxContent>

<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
      <cmr:button label="${ui.btn.createNewEntry}" onClick="cmr.chooseNewEntry()" highlight="true" pad="false"/>
      <cmr:button label="${ui.btn.openByRequestID}" onClick="openRequestById()" highlight="false" pad="true" />
<%if (user.isHasCountries()) {%>
      <div style="float: right; font-size:13px">
       List is filtered according to preferred countries. Go to <a href="javascript: goToUrl('${contextPath}/preferences')">user preferences</a> to update your preferences.
      </div>
<%}%>
  </cmr:buttonsRow>
</cmr:section>

<input type="hidden" id="fromURL" name="fromUrl" value="" />
<input type="hidden" id="newReqCntry_h" name="newReqCntry" value="" />
<input type="hidden" id="newReqType_h" name="newReqType" value="" />
</form:form>
<cmr:model model="openReqModel" />

<jsp:include page="../massrequestentry/file_dl.jsp" />