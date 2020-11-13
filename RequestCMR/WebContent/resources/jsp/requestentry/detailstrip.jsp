<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
if (readOnly == null){
  readOnly = false;
}
String extraClass = "";
if ("DRA".equals(reqentry.getReqStatus())){
    extraClass= " cmr-overall-status-dra";
} else if ("PRJ".equals(reqentry.getReqStatus())){
    extraClass= " cmr-overall-status-rej";
} else if ("COM".equals(reqentry.getReqStatus())){
  extraClass= " cmr-overall-status-com";
}
%><cmr:row topPad="10">
  <cmr:column span="2" width="170">
    <p>
      <cmr:label fieldId="reqId"><cmr:fieldLabel fieldId="RequestID" />:</cmr:label>
      <c:if test="${reqentry.reqId > 0}">
        ${reqentry.reqId}
      </c:if>
      <c:if test="${reqentry.reqId <= 0}">
        Not assigned
      </c:if>
    </p>
  </cmr:column>
  <cmr:column span="2" width="250" >
    <p>
      <cmr:label fieldId="lockedBy">${ui.lockedBy}:</cmr:label>
      ${reqentry.lockByNm}
    </p>
  </cmr:column>
  <cmr:column span="1" width="200">
            <p>
              <label for="lockTs">${ui.yourRole}: </label>
             ${reqentry.userRole}
            </p>
          </cmr:column>
<%if (!newEntry){ %>
  <cmr:column span="1" width="200">
    <div style="padding-top:8px">
        <cmr:button label="${ui.btn.requestSummary}" onClick="showSummaryScreen(${reqentry.reqId}, '${reqentry.reqType}')" highlight="false" pad="false"/>
            <img class="pdf" title="Export Request Details to PDF" onclick="exportToPdf()" src="${resourcesPath}/images/pdf-icon.png">
     </div>
  </cmr:column>
<%} %>
  
  <div class="ibm-col-6-1" style="width:180px;float:right;">
    <div class="cmr-overall-status<%=extraClass%>" style="margin-top:0;">
      ${reqentry.overallStatus}<br>
    </div>
  </div>  

</cmr:row>
<div class="cmr-role-tag">
</div>
      <cmr:row topPad="10">
<%if (!readOnly && false){ %>
        <cmr:column span="2" >
          <span style="color:red;font-size:16px;font-weight:bold">*</span> 
        <span style="font-size:14px;font-style:italic;color:#333">
          ${ui.mandatoryLegend}
        </span>
          </cmr:column>
<%}%>
          </cmr:row>
