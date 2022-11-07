<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.GCARSUpdtQueueModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.FieldInfoModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration" %>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  GCARSUpdtQueueModel gcars_updt_queue = (GCARSUpdtQueueModel) request.getAttribute("gcars_updt_queue");
%>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
    FormManager.ready();
  });
  
  function backToList(){
    window.location = '${contextPath}/code/gcars_updt_queue';
  }

</script>
<cmr:boxContent>
  <cmr:tabs />
  <cmr:form method="POST" action="${contextPath}/code/gcars_updt_queue_form" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="gcars_updt_queue">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>
            GCARS UPDT QUEUE Detail
          </h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="sourceName">File Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${gcars_updt_queue.sourceName}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="seqNo">Sequence No: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${gcars_updt_queue.seqNo}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="procStatus">Status: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${gcars_updt_queue.procStatus == 'E'}">
            <span style='color:red'>Error</span>
          </c:if>
          <c:if test="${gcars_updt_queue.procStatus == 'I'}">
            <span style='color:blue'>In Progress</span>
          </c:if>
          <c:if test="${gcars_updt_queue.procStatus == 'P'}">
            <span style='color:green'>Pending</span>
          </c:if>
          <c:if test="${gcars_updt_queue.procStatus == 'C'}">
            <span style='color:blue'>Completed</span>
          </c:if>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cmrNo">CMR No: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${gcars_updt_queue.cmrNo}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label>Update Type: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${gcars_updt_queue.codCondition != '' && gcars_updt_queue.codRsn != ''}">
            <span style='color:blue'>CODCOND/CODREAS</span>
          </c:if>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="procMsg">Message: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${gcars_updt_queue.procMsg}
        </cmr:column>
      </cmr:row>
      
    </cmr:section>
  </cmr:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Back to GCARS UPDT QUEUE List" onClick="backToList()" />
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="gcars_updt_queue" />