<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.JpIsicToJsicMapModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  JpIsicToJsicMapModel jpisictojsicmapform = (JpIsicToJsicMapModel) request.getAttribute("jpisictojsicmapform");

  boolean newEntry = false;
  boolean readOnly = true;

  if (jpisictojsicmapform.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
    readOnly = false;
  }
%>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
    FormManager.addValidator('mandt', Validators.REQUIRED , ['MANDT']);
    FormManager.addValidator('jsicCd', Validators.REQUIRED , ['JSIC_CD']);
    FormManager.ready();
  });
  
  var actions = (function() {
    return {
      save : function(flag) {
        if(flag){
          var checkIndc = cmr.query('JP.ISIC_TO_JSIC_MAP_EXISTS', {
            JSIC_CD: FormManager.getActualValue('jsicCd'),
            MANDT: FormManager.getActualValue('mandt'),
          });
          if (checkIndc.ret1 == '1') {
            cmr.showAlert('This record already exists in the system.');
            return;
          }
        } else {
          // todo
         }
        FormManager.save('frmCMR');
      }
    };
  })();
  
  function backToList(){
    window.location = '${contextPath}/code/jpisictojsicmap';
  }

</script>

<cmr:boxContent>
  <cmr:tabs />
  <cmr:form method="POST" action="${contextPath}/code/jpisictojsicmapform" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="jpisictojsicmapform" >
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>
          </h3>
        </cmr:column>
      </cmr:row>
      
	 <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="mandt">MANDT: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="mandt" path="mandt" dojoType="dijit.form.TextBox" placeHolder="MANDT"  maxlength="3" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="jsicCd">JSIC_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="jsicCd" path="jsicCd" dojoType="dijit.form.TextBox" placeHolder="JSIC_CD"  maxlength="5" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="isicCd">ISIC_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="isicCd" path="isicCd" dojoType="dijit.form.TextBox" placeHolder="ISIC_CD"  maxlength="4" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="createTs">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpisictojsicmapform.createTsStr != null}">
            ${jpisictojsicmapform.createTsStr}
          </c:if>
          <c:if test="${jpisictojsicmapform.createTsStr == null}">-</c:if>
          <form:hidden id="createTs" path="createTs" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="CreateBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpisictojsicmapform.createBy != null}">
            ${jpisictojsicmapform.createBy}
          </c:if>
          <c:if test="${jpisictojsicmapform.createBy == null}">-</c:if>
          <form:hidden id="createBy" path="createBy" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updateTs">Last Updated: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpisictojsicmapform.updateTsStr != null}">
            ${jpisictojsicmapform.updateTsStr}
          </c:if>
          <c:if test="${jpisictojsicmapform.updateTsStr == null}">-</c:if>
          <form:hidden id="updateTs" path="updateTs" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updateBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpisictojsicmapform.updateBy != null}">
            ${jpisictojsicmapform.updateBy}
          </c:if>
          <c:if test="${jpisictojsicmapform.updateBy == null}">-</c:if>
          <form:hidden id="updateBy" path="updateBy" />
        </cmr:column>
      </cmr:row>
      
    </cmr:section>
  </cmr:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%
      if (newEntry){
    %>
      <cmr:button label="Save" onClick="actions.save(true)" highlight="true" />
    <%
      } else {
    %>
      <cmr:button label="Update" onClick="actions.save(false)" highlight="true" />
    <%
      }
    %>
    <cmr:button label="Back to JP JSIC ISIC TO JSIC MAP List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="jpisictojsicmapform" />