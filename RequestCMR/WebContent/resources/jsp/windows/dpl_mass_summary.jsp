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
  
%>

<script>

</script>
<style>
div.cmr-summary {
	border: 1px Solid #999999;
	border-radius: 5px;
	width: 950px;
}

form.ibm-column-form .ibm-columns label,form.ibm-column-form label {
	font-size: 13px !important;
}

#ibm-content .ibm-columns {
	padding: 0px 10px 5px;
}

.ibm-col-4-2,.ibm-col-4-3,.ibm-col-5-2,.ibm-col-5-3,.ibm-col-5-4,.ibm-col-6-3,.ibm-col-6-4,.ibm-col-6-5
	{
	font-size: 14px;
	line-height: 1.9rem;
}

.ibm-col-5-1,.ibm-col-6-1,#ibm-content-sidebar {
	font-size: 14px;
	line-height: 1.9rem;
}
</style>
<cmr:window>
  <div class="cmr-summary">
    <form:form method="GET" action="${contextPath}/window/summary/massupdate" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="summary">
      <!--  Main Details Section -->
      <jsp:include page="summary_dpl.jsp" />
      <cmr:row addBackground="true" topPad="10">
         <cmr:column span="5" width="750">
          <cmr:grid url="/summary/dplsummarry.json" id="dplSummaryMassUpdateGrid" span="4" width="900" height="300" innerWidth="900" useFilter="true">
            <cmr:gridCol width="100px" field="cmrNo" header="${ui.grid.massCmrNo}" />
            <cmr:gridCol width="80px" field="iterationId" header="${ui.grid.massIteration}" />
            <cmr:gridCol width="80px" field="seqNo" header="${ui.grid.massRowNo}" />
            <cmr:gridCol width="200px" field="dplChkStatus" header="${ui.grid.massDplCheckStatus}" />
            <cmr:gridCol width="395px" field="dplChkTS" header="${ui.grid.massDplCheckTS}" />
            <cmr:gridParam fieldId="reqId" value="${summary.admin.id.reqId}" />   
            <cmr:gridParam fieldId="reqType" value="${summary.admin.reqType}" />
          </cmr:grid>
        </cmr:column>
      </cmr:row>     
    </form:form>
  </div>
  <cmr:windowClose>
    <cmr:button label="${ui.btn.refresh}" onClick="window.location = window.location.href" pad="true" highlight="true" />
  </cmr:windowClose>
</cmr:window>