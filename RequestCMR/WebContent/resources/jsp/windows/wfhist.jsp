<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<script>
  function sentToIdFormatter(value, rowIndex) {
    var rowData = this.grid.getItem(rowIndex);
    var nm = rowData.sendToNm;
    if (nm && dojo.strig.trim(nm) != '') {
      return nm;
    } else {
      return value;
    }
  }
  
 function histCmtFormatter(value, rowIndex) {
	rowData = this.grid.getItem(rowIndex);
	var cmtdata = rowData.cmt;
	if (cmtdata != null && cmtdata[0] != null) {
		cmtdata = cmtdata[0].replace(/\n/g, '<br>');
	}
	return '<span style="word-wrap: break-word">' + cmtdata + '</span>';
} 
  
  
  function openRequest(requestId){
    cmr.showConfirm('doOpenRequest("'+requestId+'")','The details for Request ID '+requestId+' will be opened on the main window. Proceed?', 'Warning');
  }
  
  function doOpenRequest(requestId){
    WindowMgr.openFromMain('/request/'+requestId);
  }
  
</script>
<%
  String contextPath = request.getContextPath();
  String statusetts = UIMgr.getText("grid.statusetts");
  String infoTxt = UIMgr.getText("info.timeStampInfo");
  String tz = SystemConfiguration.getValue("DATE_TIMEZONE");
  String title=statusetts+" ("+tz+")";
  String statusettsHeader =title+"<img src=\""+contextPath+"/resources/images/info-bubble-icon.png\" title=\""+infoTxt+"\" class=\"cmr-info-bubble\">";
%> 
<cmr:window>
  <form:form method="GET" action="${contextPath}/window/wfhist" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="wfhist">
    <input type="hidden" name="reqId" value="1129" id="reqId">
    <cmr:row>
      <cmr:column span="1">
        <label for="wfhist_reqId">${ui.requestId}:</label>
      </cmr:column>
      <cmr:column span="1">
        <div id="wfhist_reqId"><a href="javascript: openRequest(${wfhist.reqId})">${wfhist.reqId}</a></div>
      </cmr:column>
      <cmr:column span="2">
        <label for="wfhist_mainExpedite" class="cmr-inline">${ui.expedite}:</label>
        <div id="wfhist_mainExpedite" class="cmr-inline">${wfhist.expedite}</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <label for="wfhist_mainReqType">${ui.requestType}:</label>
      </cmr:column>
      <cmr:column span="1">
        <div id="wfhist_mainReqType">${wfhist.requestType}</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <label for="wfhist_mainCustName">${ui.customerName}:</label>
      </cmr:column>
      <cmr:column span="3">
        <div id="wfhist_mainCustName">${wfhist.customerName}</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="6">
        <cmr:grid url="/workflow/history/list.json" id="workflowHistoryGrid" span="6" width="900" height="300">
          <cmr:gridCol width="120px" field="createTsString" header="<%=statusettsHeader%>" />
          <cmr:gridCol width="120px" field="createByNm" header="${ui.grid.statusetby}" />
          <cmr:gridCol width="120px" field="reqStatusAct" header="${ui.grid.statusaction}" />
          <cmr:gridCol width="120px" field="overallStatus" header="${ui.grid.overallstatus}" />
          <cmr:gridCol width="100px" field="sentToId" header="${ui.grid.requestsentto}">
            <cmr:formatter functionName="sentToIdFormatter" />
          </cmr:gridCol>
          <cmr:gridCol width="130px" field="rejReason" header="${ui.grid.reasonforreject}" />
          <cmr:gridCol width="130px" field="cmt" header="${ui.grid.comments}" >
          <cmr:formatter functionName="histCmtFormatter" />
          </cmr:gridCol>
          <cmr:gridParam fieldId="reqId" value="${wfhist.reqId}" />
        </cmr:grid>
      </cmr:column>
    </cmr:row>
  </form:form>
  <cmr:windowClose />
</cmr:window>