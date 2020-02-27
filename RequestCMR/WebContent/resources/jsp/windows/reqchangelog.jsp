<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  String reqId = request.getParameter("reqId");
%>

<cmr:window>
  <cmr:row topPad="10" addBackground="false">
    <cmr:column span="6">
      <cmr:grid url="/changeloglist.json" id="changeLogGrid" span="6" useFilter="true" usePaging="false">
        <cmr:gridParam fieldId="requestIdStr" value="<%=reqId%>" />
        <cmr:gridParam fieldId="userId" value="" />
        <cmr:gridParam fieldId="tablName" value="" />
        <cmr:gridParam fieldId="changeDateFrom" value="" />
        <cmr:gridParam fieldId="changeDateTo" value="" />
        <cmr:gridParam fieldId="loadRec" value="Y" />
        <cmr:gridCol width="80px" field="requestId" header="Request ID" />
        <cmr:gridCol width="120px" field="changeTsStr" header="Change Timestamp" />
        <cmr:gridCol width="90px" field="tablName" header="Table" />
        <cmr:gridCol width="80px" field="addrTyp" header="Address Type" />
        <cmr:gridCol width="70px" field="action" header="Action" />
        <cmr:gridCol width="90px" field="fieldName" header="Field" />
        <cmr:gridCol width="100px" field="oldValue" header="Old value" />
        <cmr:gridCol width="100px" field="newValue" header="New Value" />
        <cmr:gridCol width="auto" field="userId" header="User ID" />
      </cmr:grid>
    </cmr:column>
  </cmr:row>
  <cmr:windowClose>
    <cmr:button label="${ui.btn.refresh}"
      onClick="window.location.reload()" pad="true" highlight="true" />
  </cmr:windowClose>

</cmr:window>