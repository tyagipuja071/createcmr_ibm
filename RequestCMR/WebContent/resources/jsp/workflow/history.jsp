<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<!--  Modal for the Workflow History Screen -->
<cmr:modal title="${ui.title.wfhist}" id="workflowHistoryModal" widthId="980">
  <form:form method="GET" action="${contextPath}/workflow/history" name="frmCMRHist" class="ibm-column-form ibm-styled-form" modelAttribute="wfhist">

    <form:hidden path="reqId" id="workflowRequestId" />
    <form:hidden path="mainReqType" />
    <form:hidden path="mainCustName" />
    <form:hidden path="mainExpedite" />
    
    <cmr:row>
      <cmr:column span="1">
        <label for="wfhist_reqId">${ui.requestId}:</label>
      </cmr:column>
      <cmr:column span="1">
        <div id="wfhist_reqId"></div>
      </cmr:column>
      <cmr:column span="2">
        <label for="wfhist_mainExpedite" class="cmr-inline">${ui.expedite}:</label>
        <div id="wfhist_mainExpedite" class="cmr-inline"></div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <label for="wfhist_mainReqType">${ui.requestType}:</label>
      </cmr:column>
      <cmr:column span="1">
        <div id="wfhist_mainReqType"></div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <label for="wfhist_mainCustName">${ui.customerName}:</label>
      </cmr:column>
      <cmr:column span="3">
        <div id="wfhist_mainCustName"></div>
      </cmr:column>
    </cmr:row>
     <%
  String contextPath = request.getContextPath();
  String statusetts = UIMgr.getText("grid.statusetts");
  String infoTxt = UIMgr.getText("info.timeStampInfo");
  String tz = SystemConfiguration.getValue("DATE_TIMEZONE");
  String title=statusetts+" ("+tz+")";
  String statusettsHeader =title+"<img src=\""+contextPath+"/resources/images/info-bubble-icon.png\" title=\""+infoTxt+"\" class=\"cmr-info-bubble\">";
  %> 
    <cmr:row>
      <cmr:column span="6">
        <cmr:grid url="/workflow/history/list.json" id="workflowHistoryGrid" span="6" width="900" height="300">
          <cmr:gridCol width="130px" field="createTsString" header="<%=statusettsHeader%>" />
          <cmr:gridCol width="120px" field="createByNm" header="${ui.grid.statusetby}" />
          <cmr:gridCol width="120px" field="reqStatusAct" header="${ui.grid.statusaction}" />
          <cmr:gridCol width="120px" field="overallStatus" header="${ui.grid.overallstatus}" />
          <cmr:gridCol width="100px" field="sentToId" header="${ui.grid.requestsentto}" >
            <cmr:formatter functionName="sentToIdFormatter" />
          </cmr:gridCol>
          <cmr:gridCol width="130px" field="rejReason" header="${ui.grid.reasonforreject}" />
          <cmr:gridCol width="130px" field="cmt" header="${ui.grid.comments}" >
          <cmr:formatter functionName="histCmtFormatter" />
          </cmr:gridCol>
        </cmr:grid>
      </cmr:column>
    </cmr:row>
    <cmr:buttonsRow>
      <cmr:hr />
      <cmr:button label="${ui.btn.close}" onClick="cmr.hideModal('workflowHistoryModal')" highlight="true" />
    </cmr:buttonsRow>
  </form:form>
</cmr:modal>
