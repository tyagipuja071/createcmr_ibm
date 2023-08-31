<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.JpBoCodesMapModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  JpBoCodesMapModel jpbocodesmapform = (JpBoCodesMapModel) request.getAttribute("jpbocodesmapform");

  boolean newEntry = false;
  boolean readOnly = true;

  if (jpbocodesmapform.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
    readOnly = false;
  }
%>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
    FormManager.addValidator('subsidiaryCd', Validators.REQUIRED , ['SUBSIDIARY_CD']);
    FormManager.addValidator('officeCd', Validators.REQUIRED , ['OFFICE_CD']);
    FormManager.addValidator('subOfficeCd', Validators.REQUIRED , ['SUB_OFFICE_CD']);
    FormManager.addValidator('boCd', Validators.REQUIRED , ['BO_CD']);
    FormManager.addValidator('fieldSalesCd', Validators.REQUIRED , ['FIELD_SALES_CD']);
    FormManager.addValidator('salesOfficeCd', Validators.REQUIRED , ['SALES_OFFICE_CD']);
    FormManager.addValidator('mktgDivCd', Validators.REQUIRED , ['MKTG_DIV_CD']);
    FormManager.addValidator('mrcCd', Validators.REQUIRED , ['MRC_CD']);
    FormManager.addValidator('mktgDeptName', Validators.REQUIRED , ['MKTG_DEPT_NAME']);
    FormManager.ready();
  });
  
  var actions = (function() {
    return {
      save : function(flag) {
        if(flag){
          var checkIndc = cmr.query('JP.BO_CODES_MAP_EXISTS', {
            SUBSIDIARY_CD: FormManager.getActualValue('subsidiaryCd'),
            OFFICE_CD: FormManager.getActualValue('officeCd'),
            SUB_OFFICE_CD: FormManager.getActualValue('subOfficeCd'),
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
    window.location = '${contextPath}/code/jpbocodesmap';
  }

</script>

<cmr:boxContent>
  <cmr:tabs />
  <cmr:form method="POST" action="${contextPath}/code/jpbocodesmapform" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="jpbocodesmapform" >
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
            <cmr:label fieldId="subsidiaryCd">SUBSIDIARY_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="subsidiaryCd" path="subsidiaryCd" dojoType="dijit.form.TextBox" placeHolder="SUBSIDIARY_CD"  maxlength="2" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="officeCd">OFFICE_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="officeCd" path="officeCd" dojoType="dijit.form.TextBox" placeHolder="OFFICE_CD"  maxlength="3" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="subOfficeCd">SUB_OFFICE_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="subOfficeCd" path="subOfficeCd" dojoType="dijit.form.TextBox" placeHolder="SUB_OFFICE_CD"  maxlength="3" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="boCd">BO_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="boCd" path="boCd" dojoType="dijit.form.TextBox" placeHolder="BO_CD"  maxlength="2" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="fieldSalesCd">FIELD_SALES_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="fieldSalesCd" path="fieldSalesCd" dojoType="dijit.form.TextBox" placeHolder="FIELD_SALES_CD"  maxlength="2" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="salesOfficeCd">SALES_OFFICE_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="salesOfficeCd" path="salesOfficeCd" dojoType="dijit.form.TextBox" placeHolder="SALES_OFFICE_CD" maxlength="2" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="mktgDivCd">MKTG_DIV_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="mktgDivCd" path="mktgDivCd" dojoType="dijit.form.TextBox" placeHolder="MKTG_DIV_CD"  maxlength="1" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="mrcCd">MRC_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="mrcCd" path="mrcCd" dojoType="dijit.form.TextBox" placeHolder="MRC_CD"  maxlength="1" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="deptCd">DEPT_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="deptCd" path="deptCd" dojoType="dijit.form.TextBox" placeHolder="DEPT_CD"  maxlength="5" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="mktgDeptName">MKTG_DEPT_NAME: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="mktgDeptName" path="mktgDeptName" dojoType="dijit.form.TextBox" placeHolder="MKTG_DEPT_NAME"  maxlength="50" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="clusterId">CLUSTER_ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="clusterId" path="clusterId" dojoType="dijit.form.TextBox" placeHolder="CLUSTER_ID"  maxlength="5" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="clientTierCd">CLIENT_TIER_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="clientTierCd" path="clientTierCd" dojoType="dijit.form.TextBox" placeHolder="CLIENT_TIER_CD"  maxlength="2" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="isuCdOverride">ISU_CD_OVERRIDE: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="isuCdOverride" path="isuCdOverride" dojoType="dijit.form.TextBox" placeHolder="ISU_CD_OVERRIDE"  maxlength="4" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="isicCd">ISIC_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="isicCd" path="isicCd" dojoType="dijit.form.TextBox" placeHolder="ISIC_CD"  maxlength="8" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="createTs">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpbocodesmapform.createTsStr != null}">
            ${jpbocodesmapform.createTsStr}
          </c:if>
          <c:if test="${jpbocodesmapform.createTsStr == null}">-</c:if>
          <form:hidden id="createTs" path="createTs" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="CreateBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpbocodesmapform.createBy != null}">
            ${jpbocodesmapform.createBy}
          </c:if>
          <c:if test="${jpbocodesmapform.createBy == null}">-</c:if>
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
          <c:if test="${jpbocodesmapform.updateTsStr != null}">
            ${jpbocodesmapform.updateTsStr}
          </c:if>
          <c:if test="${jpbocodesmapform.updateTsStr == null}">-</c:if>
          <form:hidden id="updateTs" path="updateTs" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updateBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpbocodesmapform.updateBy != null}">
            ${jpbocodesmapform.updateBy}
          </c:if>
          <c:if test="${jpbocodesmapform.updateBy == null}">-</c:if>
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
    <cmr:button label="Back to JP BO CODES MAP List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="jpbocodesmapform" />