<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.model.approval.ApprovalResponseModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
AppUser user = AppUser.getUser(request);
if (user != null){
%>
<style>
#openRequestByIdModal div.ibm-columns {
  width: 360px !important;
}
</style>
<script>
  dojo.addOnLoad(function() {
    FilteringDropdown.loadItems('newReqCntry', 'newReqCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry&newRequest=Y');
     //FilteringDropdown.loadItems('newReqType', 'newReqType_spinner', 'lov', 'fieldId=SearchRequestType');
     FilteringDropdown.loadOnChange('newReqType', 'newReqType_spinner', 'NEWREQUEST.TYPES', 'cntry=_newReqCntry', 'newReqCntry');     
     FormManager.setValue('newReqCntry', '<%=user.getCmrIssuingCntry()%>');
     FilteringDropdown['val_newReqCntry'] = '<%=user.getCmrIssuingCntry()%>';
     FormManager.setValue('newReqCntry', '<%=user.getCmrIssuingCntry()%>');
     
     <%if (!StringUtils.isEmpty(user.getDefaultReqType())){%>
       FormManager.setValue('newReqType', '<%=user.getDefaultReqType()%>');
       FilteringDropdown['val_newReqType'] = '<%=user.getDefaultReqType()%>';
       FormManager.setValue('newReqType', '<%=user.getDefaultReqType()%>');
     <%}%>
  
  });
</script>


<!--  Modal for the Add Approval Screen -->
<cmr:modal title="New Request" id="newRequestModal" widthId="570">
    <cmr:row>
      <cmr:column span="3" width="520">
        <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
        <cmr:note text="Only Request Types that are supported by the chosen CMR Issuing Country will be listed."></cmr:note>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <p>
          <cmr:label fieldId="newReqCntry">CMR Issuing Country:</cmr:label>
        </p>
      </cmr:column>
      <cmr:column span="2">
        <p>
          <select dojoType="dijit.form.FilteringSelect" id="newReqCntry" itemLabel="name" itemValue="id" style="display: block;" maxHeight="200"
            required="true" path="newReqCntry" placeHolder="Select CMR Issuing Country" cssStyle="width:300px"></select>
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <p>
          <cmr:label fieldId="newReqType">Request Type:</cmr:label>
        </p>
      </cmr:column>
      <cmr:column span="2">
        <p>
          <select dojoType="dijit.form.FilteringSelect" id="newReqType" itemLabel="name" itemValue="id" style="display: block;" maxHeight="200"
            required="true" path="newReqType" placeHolder="Select Request Type" cssStyle="width:300px"></select>
        </p>
      </cmr:column>
    </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="Create Request" onClick="createNewEntry()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('newRequestModal')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>

<cmr:modal title="Open Request by ID" id="openRequestByIdModal" widthId="390">
    <cmr:row>
      <cmr:column span="1" width="80">
        <p>
          <cmr:label fieldId="requestById">Request ID:</cmr:label>
        </p>
      </cmr:column>
      <cmr:column span="2" width="160">
        <p>
          <input dojoType="dijit.form.TextBox" id="requestById" name="requestById" style="width:150px">
        </p>
      </cmr:column>
    </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.openRequest}" onClick="doOpenRequestById()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('openRequestByIdModal')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>
<%}%>