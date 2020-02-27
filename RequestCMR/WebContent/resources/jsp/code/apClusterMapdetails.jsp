<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.ApCustClusterTierMapModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  ApCustClusterTierMapModel  acctmModel= (ApCustClusterTierMapModel) request
          .getAttribute("apClusterMapDetails");
      boolean newEntry = false;
      if (acctmModel.getState() == BaseModel.STATE_NEW) {
        newEntry = true;
      }
      String title = newEntry ? "Add AP Customer Cluster Tier Mapping" : "Update AP Customer Cluster Tier Mapping";
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />     
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>

 <script>
  dojo.addOnLoad(function() {
           var idsIsu = cmr.query('CNAE.ISU_CD', {_qall  : 'Y'});
           var modelIsu = { 
            identifier : "id", 
            label : "name",
            selectedItem : null,
            items : []
        };
        for (var i =0; i < idsIsu.length; i++){
          if (idsIsu[i].ret1 != "''"){
            modelIsu.items.push({id : idsIsu[i].ret1, name : idsIsu[i].ret1 + ' - ' + idsIsu[i].ret2});
          }
        }
        //issuing cntry
        var dropdownIsu = {
            listItems : modelIsu
        };     
                var ids1 = cmr.query('SYSTEM.SUPPCNTRY', {_qall  : 'Y'});
        var model1 = { 
        identifier : "id", 
        label : "name",
        items : []
    };
    model1.items.push({id : '*', name : '* - General'});
    for (var i =0; i < ids1.length; i++){
      model1.items.push({id : ids1[i].ret1, name : ids1[i].ret1 + ' - ' + ids1[i].ret2});
    }
    var dropdown1 = {
        listItems : model1
    };   

    FilteringDropdown.loadFixedItems('isuCode', null, dropdownIsu);
    FilteringDropdown.loadItems('clientTierCd', null,'bds', 'fieldId=ClientTier');
    FilteringDropdown.loadFixedItems('issuingCntry', null, dropdown1);
    FormManager.addValidator('issuingCntry', Validators.REQUIRED, [ 'Issuing Country' ]);
    FormManager.addValidator('apCustClusterId', Validators.REQUIRED, [ 'AP Customer Cluster Id' ]);
    FormManager.addValidator('clientTierCd', Validators.REQUIRED, [ 'Client Tier Code' ]);
   FormManager.addValidator('clusterDesc', Validators.REQUIRED, [ 'Cluster Description' ]);
    FormManager.addValidator('isuCode', Validators.REQUIRED, [ 'ISU Code' ]);
    FormManager.ready();
  });


    var ApCustClusterTierMapService = (function() {
    return {
      saveCluster : function() {
      var apCustClusterId = FormManager.getActualValue('apCustClusterId');
      var apCustClusterDesc = FormManager.getActualValue('clusterDesc');
      if(apCustClusterDesc.length > 50) {
      cmr.showAlert("Please enter a valid Cluster Desc (Max Length 50).");
      }     
      else{ 
      if(apCustClusterId.length <= 8){  
       var result = cmr.query('AP_CLUSTER_MAP.SAVE', {
        ISSUING_CNTRY : FormManager.getActualValue('issuingCntry'),
        ISU_CD : FormManager.getActualValue('isuCode'),
        CLIENT_TIER : FormManager.getActualValue('clientTierCd'),
        AP_CUST_CLUSTER_ID : apCustClusterId
        });      
        if(result.ret1 > 0 && dojo.byId('frmCMRApDetail').title == 'Add AP Customer Cluster Tier Mapping'){
        cmr.showAlert("Entry with these details already exists.");
        }  
        else{
        FormManager.save('frmCMRApDetail');
        }
        }
        else{
         cmr.showAlert("Please enter a valid AP Customer Cluster Id (Max Length 8).");
        }
        }
     
      },
      removeCluster : function(){
        FormManager.remove('frmCMRApDetail');
      }
    };
  })();
    
  function backToList() {
    window.location = '${contextPath}/code/apClusterMap';
  }
</script>
<cmr:boxContent>
  <cmr:tabs />
  <form:form method="POST" action="${contextPath}/code/apClusterMapdetails" id = "frmCMRApDetail" name="frmCMRApDetail" title="<%=title%>" class="ibm-column-form ibm-styled-form"
    modelAttribute="apClusterMapDetails">
    <cmr:modelAction formName="frmCMRApDetail" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=title%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="issuingCntry">Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>${apClusterMapDetails.issuingCntry}</p>
          <form:hidden id="issuingCntry" path="issuingCntry" />
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="apCustClusterId">AP Customer Cluster Id: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>${apClusterMapDetails.apCustClusterId}</p>
          <form:hidden id="apCustClusterId" path="apCustClusterId" />
        </cmr:column>
      </cmr:row>
        
      <%
        } else {
      %>  
         <cmr:row>
         <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="issuingCntry">Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="issuingCntry"
            searchAttr="name" style="display: block; width:300px"
            maxHeight="200" required="true" path="issuingCntry"
            placeHolder="Select Issuing Country">
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>
       <cmr:row>
         <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="apCustClusterId">AP Customer Cluster Id: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="apCustClusterId" id="apCustClusterId" dojoType="dijit.form.TextBox" cssStyle="width:300px" value=" ${apClusterMapDetails.apCustClusterId}" />
          </p>
        </cmr:column>
      </cmr:row>
       <%
        }
      %>
       <cmr:row>     
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="clientTierCd">Client Tier Code:</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <form:select dojoType="dijit.form.FilteringSelect" id="clientTierCd"
            searchAttr="name" style="display: block; width:300px"
            maxHeight="200" required="true" path="clientTierCd"
            placeHolder="Select Client Tier Code">
          </form:select>
        </cmr:column>
      </cmr:row>
       <cmr:row>     
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="isuCode">ISU Code:</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <form:select dojoType="dijit.form.FilteringSelect" id="isuCode"
            searchAttr="name" style="display: block; width:300px"
            maxHeight="200" required="true" path="isuCode"
            placeHolder="Select ISU Code">
          </form:select>
        </cmr:column>
      </cmr:row> 
     
<%--           <cmr:row>
         <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="issuingCntry">Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="issuingCntry"
            searchAttr="name" style="display: block; width:300px"
            maxHeight="200" required="true" path="issuingCntry"
            placeHolder="Select Issuing Country">
          </form:select>
          </p>
        </cmr:column>
      </cmr:row> --%>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="clusterDesc">Cluster Description: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" >
          <p>
            <form:input path="clusterDesc" id="clusterDesc" dojoType="dijit.form.TextBox" cssStyle="width:400px" value="${apClusterMapDetails.clusterDesc }" />
          </p>
        </cmr:column>
      </cmr:row>
      <form:hidden id="createTs" path="createTs" />
      <form:hidden id="UpdtTs" path="UpdtTs" />
      <form:hidden id="createBy" path="createBy" />
      <form:hidden id="UpdtBy" path="UpdtBy" />
  </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%
      if (newEntry) {
    %>
    <cmr:button label="Save" onClick="ApCustClusterTierMapService.saveCluster()" highlight="true" />
    <cmr:button label="Back to Cluster List" onClick="backToList()" pad="true" />
    <%
      } else {
    %>
    <cmr:button label="Update" onClick="ApCustClusterTierMapService.saveCluster()" highlight="true" />
    <cmr:button label="Delete" onClick="ApCustClusterTierMapService.removeCluster()" pad="true" />
    <cmr:button label="Back to Cluster List" onClick="backToList()" pad="true" />
    <%
      }
    %>
  <%--  <cmr:button label="Back to Cluster Tier Map List" onClick="window.location = '${contextPath}/code/apClusterMap'" pad="true" /> --%>
</cmr:buttonsRow>
</cmr:section>
<cmr:model model="apClusterMapDetails" />