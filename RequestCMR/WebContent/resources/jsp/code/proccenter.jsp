<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.ProcCenterModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  ProcCenterModel procCenter = (ProcCenterModel) request.getAttribute("proccenter");
    boolean newEntry = false;
     if (procCenter.getState() == BaseModel.STATE_NEW) {
        newEntry = true;
      }
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>

  dojo.addOnLoad(function() {
    // Default values
    var procCenters = ["Hortolandia", "Kuala Lumpur", "Bratislava", "Dalian"];
    var procCenterModel = { identifier : "id", label : "name", items : [] };
    for ( var i = 0; i < procCenters.length; i++) {
      procCenterModel.items.push({ id : procCenters[i], name : procCenters[i] });
    }
    var fixedProcCenterNms = { listItems : procCenterModel };
    FilteringDropdown.loadFixedItems('procCenterNm', null, fixedProcCenterNms);
  
    var ids1 = cmr.query('SYSTEM.CNTRY_PROCCENTER', { _qall : 'Y' });
    var cntryModel = { identifier : "id", label : "name", items : [] };
    for ( var i = 0; i < ids1.length; i++) {
      cntryModel.items.push({ id : ids1[i].ret1, name : ids1[i].ret2 });
    }
    var countries = { listItems : cntryModel };
    FilteringDropdown.loadFixedItems('cmrIssuingCntry', null, countries);

    FormManager.addValidator('cmrIssuingCntry', Validators.REQUIRED, [ 'CMR Issuing Country' ]);
    FormManager.addValidator('procCenterNm', Validators.REQUIRED, [ 'Processing Center Name' ]);
    FormManager.addValidator('cmt', Validators.REQUIRED, [ 'Comment' ]);
    FormManager.ready();
  });

  var ProcCenterService = (function() {
    return {
      saveProcCenter : function(typeflag) {
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

        if (typeflag) {
          var check = cmr.query('CHECKPROCCENTER', {
            CMR_ISSUING_CNTRY : cmrIssuingCntry
          });
          if (check && check.ret1 == '1') {
            cmr.showAlert('Processing Center already exists in the system.');
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

  <form:form method="POST" action="${contextPath}/code/proccentermain" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="proccenter">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Processing Center" : "Update Processing Center"%></h3>
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
          <p>${proccenter.country}</p>
          <form:hidden id="cmrIssuingCntry" path="cmrIssuingCntry" />
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
          </p>
        </cmr:column>
       </cmr:row>
      <% } %>  
       <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="procCenterNm">Processing Center Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="procCenterNm" searchAttr="name" style="display: block;" maxHeight="200"
              path="procCenterNm" placeHolder="Select Processing Center">
            </form:select>          
          </p>
        </cmr:column>
       </cmr:row>
      <cmr:row>
        <cmr:column span="2">
          <p>          
            <cmr:label fieldId="cmt">Comment: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>      
            <form:input path="cmt" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
       </cmr:row>
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="ProcCenterService.saveProcCenter(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Save" onClick="ProcCenterService.saveProcCenter(false)" highlight="true" />
    <%} %>
    <cmr:button label="Back to Processing Center List" onClick="window.location = '${contextPath}/code/proccenters'" pad="true" />
  </cmr:buttonsRow>
  <br>
  
</cmr:section>
<cmr:model model="proccenter" />