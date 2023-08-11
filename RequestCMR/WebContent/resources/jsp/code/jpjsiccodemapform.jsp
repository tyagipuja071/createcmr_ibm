<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.JpJsicCodeMapModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  JpJsicCodeMapModel jpjsiccodemapform = (JpJsicCodeMapModel) request.getAttribute("jpjsiccodemapform");

  boolean newEntry = false;
  boolean readOnly = true;

  if (jpjsiccodemapform.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
    readOnly = false;
  }
%>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
    <%
      if (newEntry){
    %>
      FormManager.addValidator('jsicCd', Validators.REQUIRED , ['JSIC_CD']);
      FormManager.addValidator('subIndustryCd', Validators.REQUIRED , ['SUB_INDUSTRY_CD']);
      FormManager.addValidator('isuCd', Validators.REQUIRED , ['ISU_CD']);
      FormManager.addValidator('isicCd', Validators.REQUIRED , ['ISIC_CD']);
      FormManager.addValidator('dept', Validators.REQUIRED , ['DEPT']);
    <%
      }
    %>
    FormManager.addValidator('jsicCd', Validators.REQUIRED , ['JSIC_CD']);
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED , ['SUB_INDUSTRY_CD']);
    FormManager.addValidator('isuCd', Validators.REQUIRED , ['ISU_CD']);
    FormManager.addValidator('isicCd', Validators.REQUIRED , ['ISIC_CD']);
    FormManager.addValidator('dept', Validators.REQUIRED , ['DEPT']);
    FormManager.ready();
  });
  
  var actions = (function() {
    return {
      save : function(flag) {
        if(flag){
          var checkIndc = cmr.query('JP.SIC_CODE_MAP_EXISTS', {
            JSIC_CD: FormManager.getActualValue('jsicCd'),
            SUB_INDUSTRY_CD: FormManager.getActualValue('subIndustryCd'),
            ISU_CD: FormManager.getActualValue('isuCd'),
            ISIC_CD: FormManager.getActualValue('isicCd'),
            DEPT: FormManager.getActualValue('dept'),
          });
          if (checkIndc.ret1 == '1') {
            cmr.showAlert('This record already exists in the system.');
            return;
          }
        } else {
          /* var checkIndc = cmr.query('JP.SIC_CODE_MAP_EXISTS', {
            JSIC_CD: FormManager.getActualValue('jsicCd'),
            SUB_INDUSTRY_CD: FormManager.getActualValue('subIndustryCd'),
            ISU_CD: FormManager.getActualValue('isuCd'),
            ISIC_CD: FormManager.getActualValue('isicCd'),
            DEPT: FormManager.getActualValue('dept'),
          });
          if (checkIndc.ret1 == undefined) {
            cmr.showAlert('No such record exists in the system.');
            return;
          } */
         }
        FormManager.save('frmCMR');
      }
    };
  })();
  
  function backToList(){
    window.location = '${contextPath}/code/jpjsiccodemap';
  }

</script>

<cmr:boxContent>
  <cmr:tabs />
  <cmr:form method="POST" action="${contextPath}/code/jpjsiccodemapform" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="jpjsiccodemapform" >
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
            <cmr:label fieldId="subIndustryCd">SUB_INDUSTRY_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="subIndustryCd" path="subIndustryCd" dojoType="dijit.form.TextBox" placeHolder="SUB_INDUSTRY_CD"  maxlength="2" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="isuCd">ISU_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="isuCd" path="isuCd" dojoType="dijit.form.TextBox" placeHolder="ISU_CD"  maxlength="2" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="isicCd">ISIC_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="isicCd" path="isicCd" dojoType="dijit.form.TextBox" placeHolder="ISIC_CD"  maxlength="4" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="dept">DEPT: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="dept" path="dept" dojoType="dijit.form.TextBox" placeHolder="DEPT"  maxlength="3" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="sectorCd">SECTOR_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="sectorCd" path="sectorCd" dojoType="dijit.form.TextBox" placeHolder="SECTOR_CD" maxlength="4" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="createTs">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpjsiccodemapform.createTsStr != null}">
            ${jpjsiccodemapform.createTsStr}
          </c:if>
          <c:if test="${jpjsiccodemapform.createTsStr == null}">-</c:if>
          <form:hidden id="createTs" path="createTs" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="CreateBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpjsiccodemapform.createBy != null}">
            ${jpjsiccodemapform.createBy}
          </c:if>
          <c:if test="${jpjsiccodemapform.createBy == null}">-</c:if>
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
          <c:if test="${jpjsiccodemapform.updateTsStr != null}">
            ${jpjsiccodemapform.updateTsStr}
          </c:if>
          <c:if test="${jpjsiccodemapform.updateTsStr == null}">-</c:if>
          <form:hidden id="updateTs" path="updateTs" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updateBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpjsiccodemapform.updateBy != null}">
            ${jpjsiccodemapform.updateBy}
          </c:if>
          <c:if test="${jpjsiccodemapform.updateBy == null}">-</c:if>
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
    <cmr:button label="Back to JP JSIC CODE MAP List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="jpjsiccodemapform" />