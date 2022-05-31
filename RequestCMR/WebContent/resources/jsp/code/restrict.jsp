<%@page import="com.ibm.cio.cmr.request.model.code.RestrictModel"%>
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
  RestrictModel restrict = (RestrictModel) request.getAttribute("restrict");
  boolean newEntry = false;
  if (restrict.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
  }
  String title = newEntry ? "ADD US RESTRICT CODE" : "UPDATE RESTRICT CODE";
  String mandt = SystemConfiguration.getValue("MANDT").toString();
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FormManager.ready();    
     FormManager.addValidator('restrictToCd', Validators.REQUIRED,[ 'RESTRICT_TO_CD' ]);
     FormManager.addValidator('nRestrictToAbbrevNm', Validators.REQUIRED,[ 'RESTRICT_TO_ABBREV_NM' ]); 
     FormManager.addValidator('nRestrictToNm', Validators.REQUIRED,[ 'RESTRICT_TO_NM' ]);  
  });
  function backToList(){
    window.location = '${contextPath}/code/restrictlist';
  }
  dojo.addOnLoad(function(){
  });
  var RSTService = (function() {
  return {
    saveRST : function(newRST, restrictToCd) {
      var restrictToCd = FormManager.getActualValue('restrictToCd');
      if (newRST) {
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

  <form:form method="POST" action="${contextPath}/code/rstdetails" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="restrict">
    <cmr:modelAction formName="frmCMR" /> 
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=title%></h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="200">
          <p>
            <cmr:label fieldId="restrictToCd">RESTRICT_TO_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <form:hidden id="mandt" path="mandt" value="<%= mandt %>" />
          <%if (newEntry){ %>
             <form:input path="restrictToCd" id="restrictToCd"  maxlength="3"  />
          <%} else { %>
            ${restrict.restrictToCd}
            <form:hidden path="restrictToCd"/>
          <%} %>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="200">
          <p>
            <cmr:label fieldId="nRestrictToAbbrevNm">RESTRICT_TO_ABBREV_NM: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
             <form:input path="nRestrictToAbbrevNm" id="nRestrictToAbbrevNm"  maxlength="10"  />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="200">
          <p>
            <cmr:label fieldId="nRestrictToNm">RESTRICT_TO_NM: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
             <form:input path="nRestrictToNm" id="nRestrictToNm"  maxlength="20"  />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="200"> 
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
        <cmr:column span="1" width="200">
          <p>
            <cmr:label fieldId="status">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${restrict.createDt != null}">
            ${restrict.createDt}
            <form:hidden id="createDt" path="createDt" />
          </c:if>
          <c:if test="${restrict.createDt == null}">-</c:if>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="200">
          <p>
            <cmr:label fieldId="status">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${restrict.createdBy != null}">
            ${restrict.createdBy}
            <form:hidden id="createdBy" path="createdBy" />
          </c:if>
          <c:if test="${restrict.createdBy == null}">-</c:if>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="200">
          <p>
            <cmr:label fieldId="status">Last Updated: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${restrict.updateDt != null}">
            ${restrict.updateDt}
            <form:hidden id="updateDt" path="updateDt" />
          </c:if>
          <c:if test="${restrict.updateDt == null}">-</c:if>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="200">
          <p>
            <cmr:label fieldId="status">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${restrict.updatedBy != null}">
            ${restrict.updatedBy}
            <form:hidden id="updatedBy" path="updatedBy" />
          </c:if>
          <c:if test="${restrict.updatedBy == null}">-</c:if>
        </cmr:column>
      </cmr:row>
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
   <%if (newEntry){ %>
      <cmr:button label="Save" onClick="RSTService.saveRST(true,'${restrict.restrictToCd}')" highlight="true" />
   <%} else {%>
      <cmr:button label="Update" onClick="RSTService.saveRST(false,'${restrict.restrictToCd}')" highlight="true" />
   <%} %>
      <cmr:button label="Back to RST  List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="restrict" />