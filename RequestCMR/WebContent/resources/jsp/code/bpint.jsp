<%@page import="com.ibm.cio.cmr.request.model.code.BPIntModel"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.FieldInfoModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  BPIntModel bpInt = (BPIntModel) request.getAttribute("bpInt");
  boolean newEntry = false;
  if (bpInt.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
  }

  String title = newEntry ? "ADD US BP CODE" : "UPDATE BP CODE";
  String mandt = SystemConfiguration.getValue("MANDT").toString();
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FormManager.ready();    
     FormManager.addValidator('bpCode', Validators.REQUIRED,[ 'BP CODE' ]);
     FormManager.addValidator('nBpFullNm', Validators.REQUIRED,[ 'BP Full Nm' ]); 
     FormManager.addValidator('nBpAbbrevNm', Validators.REQUIRED,[ 'BP ABBREV Nm' ]);  
  });
  function backToList(){
    window.location = '${contextPath}/code/bpintlist';
  }
var BPIService = (function() {
  return {
    saveBPI : function(newBP, bpCode) {
      var cBpCode = FormManager.getActualValue('bpCode');
      var nBpAbbrevNm = FormManager.getActualValue('nBpAbbrevNm');
      var nBpFullNm = FormManager.getActualValue('nBpFullNm');
      if (newBP) {
        /* Create */
        FormManager.doAction('frmCMR', 'I', false);
      } else {
        FormManager.doAction('frmCMR', 'U', false);
      }
    },
  };

})();

</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/bpidetails" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="bpInt">
    <cmr:modelAction formName="frmCMR" /> 
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=title%></h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="bpCode">BP CODE: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <form:hidden id="mandt" path="mandt" value="<%= mandt %>" />
          <%if (newEntry){ %>
             <form:input path="bpCode" id="bpCode"  maxlength="3"  />
          <%} else { %>
            ${bpInt.bpCode}
            <form:hidden path="bpCode"/>
          <%} %>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="nBpAbbrevNm">BP ABBREV NM: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
             <form:input path="nBpAbbrevNm" id="nBpAbbrevNm"  maxlength="10"  />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="nBpFullNm">BP FULL NM: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
             <form:input path="nBpFullNm" id="nBpFullNm"  maxlength="20"  />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160"> 
          <p>
          <%if (newEntry == false){ %>
            <cmr:label fieldId="nloevm">LOEVM: </cmr:label>
          <%} %>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <%if (newEntry == false){ %>
            <form:radiobutton path="nLoevm" value="" />
            Active &nbsp;&nbsp;
            <form:radiobutton path="nLoevm" value="X" />
            Deleted
          <%} %>
          </p>
        </cmr:column>
      </cmr:row>
<cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="status">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${bpInt.createDt != null}">
            ${bpInt.createDt}
            <form:hidden id="createDt" path="createDt" />
          </c:if>
          <c:if test="${bpInt.createDt == null}">-</c:if>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="status">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${bpInt.createdBy != null}">
            ${bpInt.createdBy}
            <form:hidden id="createdBy" path="createdBy" />
          </c:if>
          <c:if test="${bpInt.createdBy == null}">-</c:if>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="status">Last Updated: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${bpInt.updateDt != null}">
            ${bpInt.updateDt}
            <form:hidden id="updateDt" path="updateDt" />
          </c:if>
          <c:if test="${bpInt.updateDt == null}">-</c:if>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="status">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${bpInt.updatedBy != null}">
            ${bpInt.updatedBy}
            <form:hidden id="updatedBy" path="updatedBy" />
          </c:if>
          <c:if test="${bpInt.updatedBy == null}">-</c:if>
        </cmr:column>
      </cmr:row>
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
   <%if (newEntry){ %>
      <cmr:button label="Save" onClick="BPIService.saveBPI(true,'${bpInt.bpCode}')" highlight="true" />
      
   <%} else {%>
      <cmr:button label="Update" onClick="BPIService.saveBPI(false,'${bpInt.bpCode}')" highlight="true" />
   <%} %>
<cmr:button label="Back to BPI  List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="bpInt" />