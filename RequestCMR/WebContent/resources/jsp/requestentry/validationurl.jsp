<%@page import="java.util.Collections"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collection"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.ValidationUrlModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
if (readOnly == null){
  readOnly = false;
}
  ArrayList<ValidationUrlModel> validations = new ArrayList<ValidationUrlModel>();
  
  if (reqentry.getValidations() != null) {
    validations = (ArrayList<ValidationUrlModel>) reqentry.getValidations();
  }
%>

<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<form:form method="POST" action="${contextPath}/request/validations" name="frmCMRVal" class="ibm-column-form ibm-styled-form" modelAttribute="requestentry" id="frmCMRVal">

<cmr:section id="VALIDATIONS_REQ_TAB" hidden="true">
  <jsp:include page="detailstrip.jsp" />
  
  <%if (!readOnly && "Processor".equalsIgnoreCase(reqentry.getUserRole())) {%>
  
  <cmr:row topPad="10" addBackground="true">
    <cmr:column span="2" width="200">
      <p>
        <cmr:label fieldId="compVerifiedIndc">${ui.compVerifiedIndc}:</cmr:label>
        <%if ("Y".equalsIgnoreCase(reqentry.getCompVerifiedIndc())) {%>
          Verified
        <%} else {%>
          Not Verified
        <%} %>
      </p>
    </cmr:column>
    <cmr:column span="2" width="250">
      <p>
        <cmr:label fieldId="compInfoSrc">${ui.compInfoSrc}:</cmr:label>
          <%if (reqentry.getCompInfoSrc()== null || "".equalsIgnoreCase(reqentry.getCompInfoSrc())) {%>
            Not Available
          <%} else {%>
           ${reqentry.compInfoSrc}
          <%} %>
      </p>
    </cmr:column>
    <cmr:column span="2" width="200">
      <p>
        <cmr:label fieldId="scenarioVerifiedIndc">${ui.scenarioVerifiedIndc}:</cmr:label>
          <%if ("Y".equalsIgnoreCase(reqentry.getScenarioVerifiedIndc())) {%>
            Verified
          <%} else {%>
            Not Verified
          <%} %>
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="5" addBackground="true">
   <div class="ibm-col-1-1 cmr-left">
      <cmr:button id="verifyCompanyButton" label="${ui.btn.verifyCompany}" onClick="verifyCompany()" styleClass="cmr-middle"/>
      <cmr:button id="verifyScenarioButton" label="${ui.btn.verifyScenario}" onClick="verifyScenario()" styleClass="cmr-middle" pad="true"/>
    </div>
    <br><br>
  </cmr:row>
  <%} %>
  
  <cmr:row>
    <cmr:column span="6">
      <table class="ibm-data-table ibm-sortable-table ibm-alternating">
        <thead>                
        <tr>
          <th width="130">Type</th>
          <th width="400">URL</th>
          <th width="*">Description</th>
        </tr>
        </thead>
        <tbody>
        <%for (int i = 0; i < validations.size(); i++){
        ValidationUrlModel val = (ValidationUrlModel) validations.get(i);
        if(val.getUrl().toLowerCase().startsWith("notes:")){ 
        %>
        <tr>
            <td><%=val.getCntryCd()%></td>
            <td><a href="<%=val.getUrl()%>"><%=val.getUrl()%></a></td>
            <td><%=val.getDescrTxt()%></td>
          </tr>
       <% }
        else { 
        %>
          <tr>
            <td><%=val.getCntryCd()%></td>
            <td><a href="javascript: showValidationUrl('<%=val.getUrl()%>','<%=val.getDisplaySeqNo()%>')"><%=val.getUrl()%></a></td>
            <td><%=val.getDescrTxt()%></td>
          </tr>
         <% 
         }
        }
        %>
        </tbody>
      </table>
    </cmr:column>
  </cmr:row>
</cmr:section>
</form:form>