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
  enterHandler = dojo.connect(dojo.query('#subindustryIsicSearchModal')[0], 'keypress', function(e){
    if (cmr.isicsearchon){
      var charCode = null;
      if (window.event) {
        e = window.event;
        charCode = e.keyCode;
      } else if (e && e.which) {
        charCode = e.which;
      }
      if (charCode == 13){
        searchSubindIsic();
      }
    }    
  });
});
</script>
<!--  Modal for the Workflow History Screen -->
<cmr:modal title="${ui.title.subindisicsearch}" id="subindustryIsicSearchModal" widthId="980">
  <form:form id="frmCMRIsic" method="GET" action="${contextPath}/request/${reqntry.reqId}" name="frmCMRIsic" class="ibm-column-form ibm-styled-form" modelAttribute="subindisic">

    <cmr:row>
      <cmr:column span="6">
          <cmr:info text="${ui.info.enterpartofdescription}"/>
          <cmr:note text="${ui.info.enterpartofdescription}" />
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="160">
        <cmr:label fieldId="subIndustryCdSearch" cssClass="cmr-inline">${ui.subindcd}:</cmr:label>
      </cmr:column>
      <cmr:column span="1" width="250">
        <form:input path="subIndustryCdSearch" maxlength="2" dojoType="dijit.form.TextBox" cssStyle="width:60px;" />
      </cmr:column>
      <cmr:column span="1" width="120">
        <cmr:label fieldId="isicCdSearch" cssClass="cmr-inline">${ui.isiccd}:</cmr:label>
      </cmr:column>
      <cmr:column span="1" width="250">
        <form:input path="isicCdSearch" maxlength="4" dojoType="dijit.form.TextBox" cssStyle="width:80px;" />
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="160">
        <cmr:label fieldId="subIndustryDescSearch" cssClass="cmr-inline">${ui.subinddesc}:
        </cmr:label>
      </cmr:column>
      <cmr:column span="1" width="250">
        <form:input path="subIndustryDescSearch" dojoType="dijit.form.TextBox" cssStyle="width:240px;" />
      </cmr:column>
      <cmr:column span="1" width="120">
        <cmr:label fieldId="isicDescSearch" cssClass="cmr-inline">${ui.isicdesc}:
        </cmr:label>
      </cmr:column>
      <cmr:column span="1" width="250">
        <form:input path="isicDescSearch" dojoType="dijit.form.TextBox" cssStyle="width:240px;" />
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="15">
      <cmr:column span="2" width="430">
        <cmr:button label="${ui.btn.search}" onClick="searchSubindIsic()" highlight="true" styleClass="cmr-reqentry-btn" />
        <cmr:button label="${ui.btn.clear}" onClick="clearSubindIsicSearch()" pad="true" styleClass="cmr-reqentry-btn" />
      </cmr:column>
      <cmr:column span="1" width="120">
        <cmr:button label="${ui.btn.close}" onClick="closeSubindIsicSearch()" styleClass="cmr-reqentry-btn cmr-padmore" />
      </cmr:column>
    </cmr:row>
    <cmr:row topPad="20">
      <cmr:column span="6">
        <cmr:grid url="/subindisic/search.json" id="subindustryIsicSearchGrid" span="6" width="900" height="250" usePaging="false">
          <cmr:gridCol width="auto" field="select" header="&nbsp;">
            <cmr:formatter functionName="selectIsicFormatter" />
          </cmr:gridCol>
          <cmr:gridCol width="100px" field="subIndustryCdSearch" header="${ui.grid.subindcd}" />
          <cmr:gridCol width="300px" field="subIndustryDescSearch" header="${ui.grid.subinddesc}" />
          <cmr:gridCol width="100px" field="isicCdSearch" header="${ui.grid.isiccd}" />
          <cmr:gridCol width="300px" field="isicDescSearch" header="${ui.grid.isicdesc}" />
        </cmr:grid>
      </cmr:column>
    </cmr:row>
  </form:form>
</cmr:modal>
