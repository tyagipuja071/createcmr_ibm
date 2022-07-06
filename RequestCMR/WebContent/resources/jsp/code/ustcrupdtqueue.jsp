<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.USTCRUpdtQueueModel"%>
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
  USTCRUpdtQueueModel us_tcr_updt_queue = (USTCRUpdtQueueModel) request.getAttribute("us_tcr_updt_queue");

  String mandt = SystemConfiguration.getValue("MANDT").toString();
%>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
    FormManager.ready();
  });
  
  function backToList(){
    window.location = '${contextPath}/code/us_tcr_updt_queue';
  }

</script>
<cmr:boxContent>
  <cmr:tabs />
  <cmr:form method="POST" action="${contextPath}/code/us_tcr_updt_queue_form" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="us_tcr_updt_queue">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>
            US TCR UPDT QUEUE Detail
          </h3>
        </cmr:column>
      </cmr:row>
      <form:hidden id="mandt" path="mandt" value="<%= mandt %>" />
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="tcrInputFileNm">TCR_INPUT_FILE_NM: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.tcrFileNm}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="seqNo">SEQ_NO: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.seqNo}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="lineContent">LINE_CONTENT: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.lineContent}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cmrNo">CMR_NO: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.cmrNo}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="taxCustTyp1">TAX_CUST_TYP_1: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.taxCustTyp1}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="taxClass1">TAX_CLASS_1: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.taxClass1}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="taxCustTyp2">TAX_CUST_TYP_2: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.taxCustTyp2}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="taxClass2">TAX_CLASS_2: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.taxClass2}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="taxCustTyp3">TAX_CUST_TYP_3: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.taxCustTyp3}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="taxClass3">TAX_CLASS_3: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.taxClass3}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="taxExemptStatus1">TAX_EXEMPT_STATUS_1: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.taxExemptStatus1}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="taxExemptStatus2">TAX_EXEMPT_STATUS_2: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.taxExemptStatus2}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="taxExemptStatus3">TAX_EXEMPT_STATUS_3: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.taxExemptStatus3}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="aLevel1Value">PROC_STATUS: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_tcr_updt_queue.procStatus == 'E'}">
            <span style='color:red'>Error</span>
          </c:if>
          <c:if test="${us_tcr_updt_queue.procStatus == 'I'}">
            <span style='color:blue'>In Progress</span>
          </c:if>
          <c:if test="${us_tcr_updt_queue.procStatus == 'P'}">
            <span style='color:green'>Pending</span>
          </c:if>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="procMsg">PROC_MSG: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.procMsg}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="createdBy">CREATED_BY: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.createdBy}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="createdTsStr">CREATE_DT: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.createdTsStr}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updatedBy">UPDATED_BY: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.updatedBy}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updatedTsStr">UPDATE_DT: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.updatedTsStr}
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="katr10">KATR10: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          ${us_tcr_updt_queue.katr10}
        </cmr:column>
      </cmr:row>
      
    </cmr:section>
  </cmr:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Back to US TCR UPDT QUEUE List" onClick="backToList()" />
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="us_tcr_updt_queue" />