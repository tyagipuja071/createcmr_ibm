<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  String actionUrl = request.getContextPath() + "/auto/import";
  String reqId = request.getParameter("reqId");
%>

<script src="${resourcesPath}/js/auto/automation.js?${cmrv}" type="text/javascript"></script>

<cmr:window>
    <form:form method="POST" action="<%=actionUrl%>" name="frmCMRAuto" class="ibm-column-form ibm-styled-form" modelAttribute="record">
    <cmr:row>
      <cmr:column span="6" >
        <h3>Results of Automated Checks for Request <%=reqId%></h3>
      </cmr:column>
    </cmr:row>
    
    <cmr:row>
      <cmr:column span="6">
        <cmr:grid url="/auto/results/list.json" id="autoResultsGrid" span="6" width="900" height="700" innerWidth="850">
          <cmr:gridCol width="70px" field="automationResultId" header="Result ID" />
          <cmr:gridCol width="80px" field="processTyp" header="Type" >
            <cmr:formatter functionName="Automation.typeFormatter"/>
          </cmr:gridCol>
          <cmr:gridCol width="110px" field="processDesc" header="Process" />
          <cmr:gridCol width="40px" field="failureIndc" header="Status" >
            <cmr:formatter functionName="Automation.failFormatter"/>
          </cmr:gridCol>
          <cmr:gridCol width="130px" field="processResult" header="Result" />
          <cmr:gridCol width="auto" field="detailedResults" header="Details">
            <cmr:formatter functionName="Automation.detailsFormatter"/>
          </cmr:gridCol>
          <cmr:gridCol width="140px" field="createBy" header="Actions" >
            <cmr:formatter functionName="Automation.actionsFormatter"/>
          </cmr:gridCol>
          <cmr:gridParam fieldId="reqId" value="<%=reqId%>" />
        </cmr:grid>
      </cmr:column>
    </cmr:row>
  </form:form>
  <jsp:include page="results_match_import.jsp" />
  <cmr:windowClose>
  </cmr:windowClose>
</cmr:window>