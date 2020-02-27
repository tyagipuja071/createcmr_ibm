<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null){
    readOnly = false;
  }
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  String contextPath = request.getContextPath();
  String createTs = UIMgr.getText("grid.approvalsCreateDateTime");
  String infoTxt = UIMgr.getText("info.timeStampInfo");
  String tz = SystemConfiguration.getValue("DATE_TIMEZONE");
  String title=createTs+" ("+tz+")";
  String createTsHeader =title+"<img src=\""+contextPath+"/resources/images/info-bubble-icon.png\" title=\""+infoTxt+"\" class=\"cmr-info-bubble\">";
%> 
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script>
var role = "${reqentry.userRole}";
</script>

<cmr:section id="APPROVALS_REQ_TAB" hidden="true">
<form:form method="POST" action="${contextPath}/approvals/action" name="frmCMRApproval" class="ibm-column-form ibm-styled-form" modelAttribute="approval" id="frmCMRApproval">
  <jsp:include page="detailstrip.jsp" />
  <cmr:row addBackground="true" topPad="10">
      <cmr:column  span="1" width="120">
        <p><label for="approvalResult"> ${ui.approvalsResult}: </label>
      </cmr:column>
      <cmr:column span="1" width="120">
        <p><span style="margin: 0; text-align: left"> ${reqentry.approvalResult} </span>
      </cmr:column>
      <c:if test="${not empty reqentry.approvalDateStr}">
      <cmr:column  span="1" width="150">
        <p><label for="approvalDateStr"> ${ui.approvalsUpdtTs}: </label>
      </cmr:column>
      <cmr:column span="1" width="100">
        <p><span style="margin: 0; text-align: left"> ${reqentry.approvalDateStr} </span>
      </cmr:column>      
      </c:if>
  </cmr:row>
  <%if (reqentry.getUserRole() != null && !(reqentry.getUserRole().equalsIgnoreCase("PROCESSOR"))) { %>
  <cmr:row addBackground="true" topPad="10">
      <cmr:column  span="1" width="800">
        <span style="color:red;font-size:16px;font-weight:bold">*</span> 
        <span style="font-size:14px;font-style:italic;color:#333">
          ${ui.note.sendApproval}
        </span>
      </cmr:column>
  </cmr:row>
  <%}%>
  <cmr:row>
    <cmr:column span="6">
      <cmr:row addBackground="true">
        <cmr:column span="6">
          <cmr:grid url="/approval/list.json" id="APPROVALS_GRID" span="6" height="250" usePaging="false">
            <cmr:gridParam fieldId="reqId" value="${reqentry.reqId}" />
            <cmr:gridCol width="200px" field="notesId" header="${ui.grid.approvalsApprover}" >
              <cmr:formatter functionName="notesIdFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="170px" field="type" header="${ui.grid.approvalsType}" />            
            <cmr:gridCol width="170px" field="createTsString" header="<%=createTsHeader%>" />
            <cmr:gridCol width="120px" field="statusStr" header="${ui.grid.approvalsCurrentStatus}" />
            <cmr:gridCol width="100px" field="actions" header="${ui.grid.approvalsActions}">
              <%if (!readOnly) {%>
              <cmr:formatter functionName="actionsFormatter" />
              <%} else {%>
              <cmr:formatter functionName="actionsFormatterBlank" />
              <%} %>
            </cmr:gridCol>
            <cmr:gridCol width="50px" field="requiredIndc" header="${ui.grid.approvalsRequired}">
              <cmr:formatter functionName="requiredFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="80px" field="comments" header="${ui.grid.approvalsComments}">
              <cmr:formatter functionName="commentsFormatter" />
            </cmr:gridCol>          
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="5" addBackground="true">
          <div class="ibm-col-1-1 cmr-middle">
          <%if (!readOnly) {%>
            <cmr:button label="${ui.btn.addApproval}" onClick="doAddApproval()" highlight="true"/>
            <%if (reqentry.getUserRole() != null && reqentry.getUserRole().equalsIgnoreCase("PROCESSOR")) { %>
              <cmr:button label="${ui.btn.sendApprovalRequests}" onClick="sendApprovalRequest()" highlight="true" pad="true"/>
            <%} %> 
          <%}%>
          <%if (!"Viewer".equals(reqentry.getUserRole())){%>
            <cmr:button label="${ui.btn.refreshList}" onClick="refreshAprovalsList()" highlight="false" pad="true"/>
          <%}%>
          </div>
        <br><br>
      </cmr:row>
    </cmr:column>
  </cmr:row>
</form:form>
<style>
div.approval-notif {
  border-radius : 5px;
  padding-left:10px;
  position:fixed;
  bottom:10px;
  right: 10px;
  width: 300px;
  min-height: 25px;
  font-size:13px;
  font-weight: bold;
  border: 1px Solid Black;
  background: rgb(169,219,128); /* Old browsers */
  background: -moz-linear-gradient(top, rgba(169,219,128,1) 0%, rgba(150,197,111,1) 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top, rgba(169,219,128,1) 0%,rgba(150,197,111,1) 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom, rgba(169,219,128,1) 0%,rgba(150,197,111,1) 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#a9db80', endColorstr='#96c56f',GradientType=0 ); /* IE6-9 */
  z-index:9999;
}

img.approval-notif-close {
  width: 15px;
  height: 15px;
  padding-right: 3px;
  background: transparent;
  border: none;
  vertical-align:middle;
  cursor: pointer;
  
}
</style>
</cmr:section>
<div class="approval-notif" id="approval-notif" style="display:none"> 
  <img src="${resourcesPath}/images/close-icon.png" class="approval-notif-close" onclick="closeApprovalNotif()">
  The request approvals' statuses have changed. Please click Refresh List on the Approvals Tab to get the latest status.
</div>
