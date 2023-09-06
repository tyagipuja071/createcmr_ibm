<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.JPOfficeSectorInacMapModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  JPOfficeSectorInacMapModel jpofficesectorinacform = (JPOfficeSectorInacMapModel) request.getAttribute("jpofficesectorinacform");

  boolean newEntry = false;
  boolean readOnly = true;

  if (jpofficesectorinacform.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
    readOnly = false;
  }
%>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
    FormManager.addValidator('officeCd', Validators.REQUIRED , ['OFFICE_CD']);
    FormManager.addValidator('sectorCd', Validators.REQUIRED , ['SECTOR_CD']);
    FormManager.addValidator('inacCd', Validators.REQUIRED , ['INAC_CD']);
    FormManager.addValidator('apCustClusterId', Validators.REQUIRED , ['AP_CUST_CLUSTER_ID']);
    FormManager.ready();
  });
  
  var actions = (function() {
    return {
      save : function(flag) {
        if(flag){
          var checkIndc = cmr.query('JP.JP_OFFICE_SECTOR_INAC_MAPPING_EXISTS', {
            OFFICE_CD: FormManager.getActualValue('officeCd'),
            INAC_CD: FormManager.getActualValue('sectorCd'),
            SECTOR_CD: FormManager.getActualValue('inacCd'),
            AP_CUST_CLUSTER_ID: FormManager.getActualValue('apCustClusterId')
          });
          if (checkIndc.ret1 == '1') {
            cmr.showAlert('This record already exists in the system.');
            return;
          }
        } else {
          /* var checkIndc = cmr.query('JP.JP_OFFICE_SECTOR_INAC_MAPPING_EXISTS', {
            OFFICE_CD: FormManager.getActualValue('officeCd'),
            INAC_CD: FormManager.getActualValue('sectorCd'),
            SECTOR_CD: FormManager.getActualValue('inacCd'),
            AP_CUST_CLUSTER_ID: FormManager.getActualValue('apCustClusterId')
          });
          if (checkIndc.ret1 == null  ||  checkIndc.ret1 == '') {
            cmr.showAlert('No such record exists in the system.');
            return;
          } */
         }
        FormManager.save('frmCMR');
      }
    };
  })();
  
  function backToList(){
    window.location = '${contextPath}/code/jpofficesectorinacmap';
  }

</script>

<cmr:boxContent>
  <cmr:tabs />
  <cmr:form method="POST" action="${contextPath}/code/jpofficesectorinacform" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="jpofficesectorinacform" >
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
            <cmr:label fieldId="officeCd">OFFICE_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="officeCd" path="officeCd" dojoType="dijit.form.TextBox" placeHolder="OFFICE_CD"  maxlength="2" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="sectorCd">SECTOR_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="sectorCd" path="sectorCd" dojoType="dijit.form.TextBox" placeHolder="SECTOR_CD"  maxlength="4" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="inacCd">INAC_CD: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="inacCd" path="inacCd" dojoType="dijit.form.TextBox" placeHolder="INAC_CD"  maxlength="4" readOnly="<%= readOnly %>"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="apCustClusterId">AP_CUST_CLUSTER_ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="apCustClusterId" path="apCustClusterId" dojoType="dijit.form.TextBox" placeHolder="AP_CUST_CLUSTER_ID" maxlength="8" readOnly="<%= readOnly %>" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="createTs">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpofficesectorinacform.createTsStr != null}">
            ${jpofficesectorinacform.createTsStr}
          </c:if>
          <c:if test="${jpofficesectorinacform.createTsStr == null}">-</c:if>
          <form:hidden id="createTs" path="createTs" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="CreateBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpofficesectorinacform.createBy != null}">
            ${jpofficesectorinacform.createBy}
          </c:if>
          <c:if test="${jpofficesectorinacform.createBy == null}">-</c:if>
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
          <c:if test="${jpofficesectorinacform.updateTsStr != null}">
            ${jpofficesectorinacform.updateTsStr}
          </c:if>
          <c:if test="${jpofficesectorinacform.updateTsStr == null}">-</c:if>
          <form:hidden id="updateTs" path="updateTs" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updateBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${jpofficesectorinacform.updateBy != null}">
            ${jpofficesectorinacform.updateBy}
          </c:if>
          <c:if test="${jpofficesectorinacform.updateBy == null}">-</c:if>
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
    <cmr:button label="Back to JP OfficeCd Sector Inac Mapping List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="jpofficesectorinacform" />