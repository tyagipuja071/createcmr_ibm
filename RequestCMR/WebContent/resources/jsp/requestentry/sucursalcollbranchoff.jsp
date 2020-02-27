<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<style>
input.cmr-padmore {
	padding-left: 100px;
}
</style>
<script>
var enterHandler = null;
dojo.addOnLoad(function() {
  enterHandler = dojo.connect(dojo.query('#sucursalCollBranchOffModel')[0], 'keypress', function(e){
    if (cmr.sucursalCollBOsearchon){
      var charCode = null;
      if (window.event) {
        e = window.event;
        charCode = e.keyCode;
      } else if (e && e.which) {
        charCode = e.which;
      }
      if (charCode == 13){
        searchSucursalcollBO();
      }
    }    
  });
});
</script>
<!--  Modal for the Workflow History Screen -->
<cmr:modal title="${ui.title.sucursalcollBOsearch}" id="sucursalCollBranchOffModel" widthId="980">
  <form:form id="frmCMRSucursalCollBO" method="GET" action="${contextPath}/request/${reqntry.reqId}" name="frmCMRSucursalCollBO" class="ibm-column-form ibm-styled-form" modelAttribute="sucursalCollBO">

    <cmr:row>
      <cmr:column span="6">
          <cmr:info text="${ui.info.enterpartofdescription}"/>
          <cmr:note text="${ui.info.enterpartofdescription}" />
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="160">
        <cmr:label fieldId="collBOIDSearch" cssClass="cmr-inline">${ui.collboid}:</cmr:label>
      </cmr:column>
      <cmr:column span="1" width="250">
        <form:input path="collBOIDSearch" maxlength="3" dojoType="dijit.form.TextBox" cssStyle="width:60px;" />
      </cmr:column>
      <cmr:column span="1" width="160">
        <cmr:label fieldId="collBODescSearch" cssClass="cmr-inline">${ui.collbodesc}:
        </cmr:label>
      </cmr:column>
      <cmr:column span="1" width="250">
        <form:input path="collBODescSearch" dojoType="dijit.form.TextBox" cssStyle="width:240px;" />
      </cmr:column>
    </cmr:row>
  
    <cmr:row topPad="15">
      <cmr:column span="2" width="430">
        <cmr:button label="${ui.btn.search}" onClick="searchSucursalcollBO()" highlight="true" styleClass="cmr-reqentry-btn" />
        <cmr:button label="${ui.btn.clear}" onClick="clearSucursalcollBOSearch()" pad="true" styleClass="cmr-reqentry-btn" />
      </cmr:column>
      <cmr:column span="1" width="120">
        <cmr:button label="${ui.btn.close}" onClick="closeSucursalcollBOSearch()" styleClass="cmr-reqentry-btn cmr-padmore" />
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="20">
      <cmr:column span="6">
        <cmr:grid url="/sucursalcollbranchoff/search.json" id="sucursalcollBOSearchGrid" span="6" width="900" height="250" usePaging="false">
        <cmr:gridParam fieldId="issuingCntrySearch" value="${reqntry.cmrIssuingCountry}" />
          <cmr:gridCol width="auto" field="select" header="&nbsp;">
            <cmr:formatter functionName="selectSucursalCollBOFormatter" />
          </cmr:gridCol>
          <cmr:gridCol width="200px" field="collBOIDSearch" header="${ui.grid.collboid}" />
          <cmr:gridCol width="500px" field="collBODescSearch" header="${ui.grid.collbodesc}" />
          
        </cmr:grid>
      </cmr:column>
    </cmr:row>
  </form:form>
</cmr:modal>
