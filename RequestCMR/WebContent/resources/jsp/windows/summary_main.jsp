<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="com.ibm.cio.cmr.request.entity.Admin"%>
<%@page import="com.ibm.cio.cmr.request.model.window.RequestSummaryModel"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestSummaryModel summary = (RequestSummaryModel) request.getAttribute("summary");
  Admin admin = summary.getAdmin();
  String createDt = CmrConstants.DATE_FORMAT().format(admin.getCreateTs());
  String lastUpdDt = CmrConstants.DATE_FORMAT().format(admin.getLastUpdtTs());
  String type = request.getParameter("reqType");
  PageManager.initFor(request, summary.getData().getCmrIssuingCntry(), admin.getReqType());
  int width = "C".equals(type) ? 390 : 460;
%>
<script>
window.doFocus = function(){
  window.location = window.location.href;
};
</script>
<%if (!admin.getReqType().equals(type)){ %>
      <cmr:row topPad="8">
        <cmr:column span="5">
           <img src="${resourcesPath}/images/warn-icon.png"><span style="color:red">The Request Type has been changed on the main window. Please save the record first to update the summary screen.</span>
        </cmr:column>
      </cmr:row>
<%} %>
      <cmr:row topPad="5">
        <cmr:column span="5">
          <cmr:info text="" />
           <cmr:note text="${ui.note.summary }" />
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
        <cmr:column span="1" width="130">
          <label>${ui.requestId}:</label>
        </cmr:column>
        <cmr:column span="3" width="<%=width%>">
        ${summary.admin.id.reqId}
      </cmr:column>
        <cmr:column span="1" width="130">
          <label>${ui.createDate}:</label>
        </cmr:column>
        <cmr:column span="1" width="130">
          <%=createDt%>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="130">
          <label>${ui.requestType}:</label>
        </cmr:column>
        <cmr:column span="3" width="<%=width%>">
          <c:if test="${summary.admin.reqType == 'C'}">
          Create
        </c:if>
          <c:if test="${summary.admin.reqType == 'U'}">
          Update
        </c:if>
          <c:if test="${summary.admin.reqType == 'N'}">
          Mass Create
        </c:if>        
          <c:if test="${summary.admin.reqType == 'M'}">
          Mass Update
        </c:if>
        <c:if test="${summary.admin.reqType == 'R'}">
          Reactivate
        </c:if>
        <c:if test="${summary.admin.reqType == 'D'}">
          Delete
        </c:if>
        <c:if test="${summary.admin.reqType == 'E'}">
          Update by Enterprise #
        </c:if>
        <c:if test="${summary.admin.reqType == 'X'}">
          Single Reactivate
        </c:if>
        
        </cmr:column>
        <cmr:column span="1" width="130">
          <label>${ui.lastUpdatedDate}:</label>
        </cmr:column>
        <cmr:column span="1" width="130">
          <%=lastUpdDt%>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="130">
          <label>${ui.requester}:</label>
        </cmr:column>
        <cmr:column span="3" width="460">
        ${summary.admin.requesterNm} (${summary.admin.requesterId})
      </cmr:column>
      </cmr:row>

      <cmr:row addBackground="true">
          <cmr:column span="1" width="130">
            <label>${ui.summ.processingStatus}:</label>
          </cmr:column>
          <cmr:column span="3" width="460">
          ${summary.processingDesc}
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="1" width="130">
            <label>${ui.summ.rdcStatus}:</label>
          </cmr:column>
          <cmr:column span="3" width="390">
          <c:if test="${summary.admin.rdcProcessingStatus == null}">
            -
          </c:if>
          <c:if test="${summary.admin.rdcProcessingStatus == 'C'}">
            Completed
          </c:if>
          <c:if test="${summary.admin.rdcProcessingStatus == 'W'}">
            Completed with Warnings
          </c:if>
          <c:if test="${summary.admin.rdcProcessingStatus == 'N'}">
            Error
          </c:if>
          <c:if test="${summary.admin.rdcProcessingStatus == 'A'}">
            Aborted, will retry once
          </c:if>
          <c:if test="${summary.admin.rdcProcessingStatus == 'T'}">
            Currently Processing
          </c:if>
          <c:if test="${summary.admin.rdcProcessingStatus == 'I'}">
            RDc Processing Skipped
          </c:if>
          <c:if test="${summary.admin.rdcProcessingMsg != null}">
            (Message: ${summary.admin.rdcProcessingMsg})
          </c:if>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="130">
          <label>${ui.cmrIssuingCntry}:</label>
        </cmr:column>
        <cmr:column span="3" width="460">
        ${summary.data.cmrIssuingCntry} - ${summary.country}
      </cmr:column>
      </cmr:row>
