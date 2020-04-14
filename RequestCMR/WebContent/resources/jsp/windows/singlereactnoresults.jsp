<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath() + "/window/singlereactprocess";
%>
<script>
function backToSearch(){
	var cntry =FormManager.getActualValue("katr6");
 	var reqId = FormManager.getActualValue("reqId");
  window.location = '<%=request.getContextPath() + "/window/singlereactsearch"%>?cntry='+cntry+'&reqId='+reqId;
}

function trackMe() {
  // noop
}
WindowMgr.trackMe = trackMe;
</script>
<style>
div.ibm-columns {
  width: 1100px !important;
}
</style>
<cmr:window>
  <form:form method="POST" action="<%=actionUrl%>" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="sreact">
    <jsp:include page="singlereactcriteria.jsp" />
    <cmr:row>
      <cmr:column span="6">
          <strong>
            <span id="cmr-error-box-msg" style="color:red">No record matched the search. 
            You can choose to Search Again or return back to the tool to record the results.
            </span>
          </strong>
      </cmr:column>
    </cmr:row>
  </form:form>
  <cmr:windowClose>
    <cmr:button label="Search Again" onClick="backToSearch()" highlight="true" pad="true"/> 
  </cmr:windowClose>
</cmr:window>
