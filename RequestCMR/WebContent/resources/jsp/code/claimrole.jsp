<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.ClaimRoleModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  ClaimRoleModel claimRole = (ClaimRoleModel) request.getAttribute("claimrole");
    boolean newEntry = false;
     if (claimRole.getState() == BaseModel.STATE_NEW) {
        newEntry = true;
      }
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>

  dojo.addOnLoad(function() {
    
    // Default values
    var roles = ["REQUESTER", "PROCESSOR"];
    var subRoles = ["", "PROC_BASIC", "PROV_VALIDATOR", "PROC_SUBMITTER"];

    var roleModel = { identifier : "id", label : "name", items : [] };
    for ( var i = 0; i < roles.length; i++) {
      roleModel.items.push({ id : roles[i], name : roles[i] });
    }
    var fixedClaimRoles = {
      listItems : roleModel
    };
    
    var subRoleModel = { identifier : "id", label : "name", items : [] };
    for ( var i = 0; i < subRoles.length; i++) {
      subRoleModel.items.push({ id : subRoles[i], name : subRoles[i] });
    }
    var fixedClaimSubRoles = { listItems : subRoleModel };
    
    FilteringDropdown.loadFixedItems('claimRoleId', null, fixedClaimRoles);
    FilteringDropdown.loadFixedItems('claimSubRoleId', null, fixedClaimSubRoles);
    
<%if (newEntry) {%>
    FormManager.disable('internalTyp');
    
    var ids1 = cmr.query('SYSTEM.CNTRY_CLAIMROLES', { _qall : 'Y' });
    var cntryModel = { identifier : "id", label : "name", items : [] };
    for ( var i = 0; i < ids1.length; i++) {
      cntryModel.items.push({ id : ids1[i].ret1, name : ids1[i].ret2 });
    }
    var countries = { listItems : cntryModel };
    FilteringDropdown.loadFixedItems('cmrIssuingCntry', null, countries);

    dojo.connect(dijit.byId('cmrIssuingCntry'), "onChange", function(value) {
      FormManager.setValue('internalTyp', '');
      if (value != '') {
        FormManager.enable('internalTyp');
        FilteringDropdown.loadItems('internalTyp', 'internalTyp_spinner', 'cmr_internal_types', 'cmrIssuingCntry=' + value);
      } else {
        FormManager.disable('internalTyp');
      }
    });
    FilteringDropdown.loadItems('reqStatus', 'reqStatus_spinner', 'lov', 'fieldId=RequestStatus');

    FormManager.addValidator('cmrIssuingCntry', Validators.REQUIRED, [ 'CMR Issuing Country' ]);
    FormManager.addValidator('internalTyp', Validators.REQUIRED, [ 'Internal Type' ]);
    FormManager.addValidator('reqStatus', Validators.REQUIRED, [ 'Request Status' ]);
<%}%>
    FormManager.addValidator('claimRoleId', Validators.REQUIRED, [ 'Claim Role ID' ]);
    FormManager.ready();
  });

  var ClaimRolesService = (function() {
    return {
      saveClaimRole : function(typeflag) {
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        var internalTyp = FormManager.getActualValue('internalTyp');
        var reqStatus = FormManager.getActualValue('reqStatus');

        if (typeflag) {
          var check = cmr.query('CHECKCLAIMROLE', {
            CMR_ISSUING_CNTRY : cmrIssuingCntry,
            INTERNAL_TYP : internalTyp,
            REQ_STATUS : reqStatus
          });
          if (check && check.ret1 == '1') {
            cmr.showAlert('Claim Role already exists in the system.');
            return;
          }
        }
        FormManager.save('frmCMR');
      },
    };
  })();
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/claimrolesmain" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="claimrole">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Claim Role" : "Update Claim Role"%></h3>
        </cmr:column>
      </cmr:row>
      
      <% if (!newEntry) { %>
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${claimrole.country}</p>
          <form:hidden id="cmrIssuingCntry" path="cmrIssuingCntry" />
        </cmr:column>
      </cmr:row>      
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="internalTyp">Internal Type: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${claimrole.internalTypDesc}</p>
          <form:hidden id="internalTyp" path="internalTyp" />
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="reqStatus">Request Status: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${claimrole.reqStatusDesc}</p>
          <form:hidden id="reqStatus" path="reqStatus" />
        </cmr:column>
      </cmr:row>      
      
      
      <% } else { %>
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCntry" searchAttr="name" style="display: block;" maxHeight="200"
              path="cmrIssuingCntry" placeHolder="Select CMR Issuing Country">
            </form:select>
            <%-- <form:input path="cmrIssuingCntry" dojoType="dijit.form.TextBox" /> --%>
          </p>
        </cmr:column>
       </cmr:row>
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="internalTyp">Internal Type: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="internalTyp" searchAttr="name" style="display: block;" maxHeight="200"
              path="internalTyp" placeHolder="Select CMR Internal Type">
            </form:select>
            <%-- <form:input path="internalTyp" dojoType="dijit.form.TextBox" /> --%>
          </p>
        </cmr:column>
       </cmr:row>
      <cmr:row>
        <cmr:column span="2">
          <p>          
            <cmr:label fieldId="reqStatus">Request Status: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="reqStatus" searchAttr="name" style="display: block;" maxHeight="200"
              path="reqStatus" placeHolder="Select Request Status">
            </form:select>           
            <%-- <form:input path="reqStatus" dojoType="dijit.form.TextBox" /> --%>
          </p>
        </cmr:column>
       </cmr:row>
      <% } %>
      
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="claimRoleId">Claim Role ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="claimRoleId" searchAttr="name" style="display: block;" maxHeight="200"
              path="claimRoleId" placeHolder="Select Claim Role ID">
            </form:select>          
            <%-- <form:input path="claimRoleId" dojoType="dijit.form.TextBox"/> --%>
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="claimSubRoleId">Claim Sub-Role ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="claimSubRoleId" searchAttr="name" style="display: block;" maxHeight="200"
              path="claimSubRoleId" placeHolder="Select Claim Sub Role ID">
            </form:select>            
            <%-- <form:input path="claimSubRoleId" dojoType="dijit.form.TextBox" /> --%>
          </p>
        </cmr:column>
      </cmr:row>   
      
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="status">Status: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="status" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
      <form:hidden id="createTs" path="createTs"/>
      <form:hidden id="updateTs" path="updateTs"/>
      <form:hidden id="createBy" path="createBy"/>
      <form:hidden id="updateBy" path="updateBy"/>      
      
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="ClaimRolesService.saveClaimRole(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Save" onClick="ClaimRolesService.saveClaimRole(false)" highlight="true" />
    <%} %>
    <cmr:button label="Back to Claim Role List" onClick="window.location = '${contextPath}/code/claimroles'" pad="true" />
  </cmr:buttonsRow>
  <br>
  
</cmr:section>
<cmr:model model="claimrole" />