<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.RepTeamModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  boolean admin = AppUser.getUser(request).isAdmin();
			String issuingCntry = (String) request.getParameter("issuingCntry");
%>
<script>
var countries= {};
  dojo.addOnLoad(function() {
    cmr.hideNode('add_rt');
    dojo.connect(FormManager.getField('issuingCntry'), 'onChange', function(value) {
      var url = cmr.CONTEXT_ROOT + '/code/repTeamList.json';
      CmrGrid.refresh('rtUrlsGrid', url, 'issuingCntry=:issuingCntry');
        var result = cmr.query('CHECK_REP_TEAM_RECORDS', {
        CNTRY : FormManager.getActualValue('issuingCntry')
    });
     if (result != null && result.ret1 == '1') {
        cmr.hideNode('add_rt');
      } else {
        cmr.showNode('add_rt');
      }
    });
    FormManager.ready();
    FilteringDropdown.loadItems('issuingCntry', 'cmrIssuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');
    console.log(
<%=issuingCntry%>
  );
    countries = cmr.query('SYSTEM.SUPPCNTRY', {
          _qall : 'Y'
        });
  });
  
  var getCountryDesc = function(id){
      if(id !=''){
        let desc='';
        countries.forEach(item=>{
          var val =item.ret1.trim();
          if(val==id){
            desc= item.ret2;
          }
        });
        return desc;
      } else {
        return "No Description Found";
      }
    };
  
  var RepTeamService = (function() {
    return {
      addRepTeam : function() {
      var country = FormManager.getActualValue('issuingCntry');
      if(country == null || country == ''){
      cmr.showAlert("Please choose the issuing country from the dropdown before proceeding to add new records");
      return;
        }
        window.location = cmr.CONTEXT_ROOT + '/code/repTeamMaint?issuingCntry='+ country;
      },
      linkFormatter : function(value, rowIndex) {
        var id = this.grid.getItem(rowIndex).issuingCntry[0];
        var country =  getCountryDesc(id);
        console.log(country);
        if (id == '' && !<%=admin%> && country == null ) {
          return value;
        }
        return '<a href="javascript: RepTeamService.open(\'' + id + '\')">' + value + " - " + country + '</a>';
      },
      open : function(value) {
        window.location = cmr.CONTEXT_ROOT + '/code/repTeamMaint?issuingCntry=' + encodeURIComponent(value);
      },
      removeSelectedMappings : function() {
        FormManager.gridHiddenAction('frmCMR', 'REMOVE_MAPPINGS', cmr.CONTEXT_ROOT + '/code/repTeam/process.json', true, refreshAfterMappingsRemove, false, 'Remove selected record(s)?');
      }
    };
  })();
  function backToCodeMaintHome() {
    window.location = cmr.CONTEXT_ROOT + '/code';
  }
  function refreshAfterMappingsRemove(result) {
    if (result.success) {
      CmrGrid.refresh('rtUrlsGrid');
    }
  }
</script>
<cmr:boxContent>
	<cmr:tabs />

	<form:form method="POST" action="${contextPath}/code/repTeam"
		name="frmCMRRt" class="ibm-column-form ibm-styled-form"
		modelAttribute="repTeamModel" id="frmCMR">

		<cmr:model model="repTeamModel" />
		<cmr:modelAction formName="frmCMR" />
		<cmr:section>
			<cmr:row topPad="8" addBackground="true">
				<cmr:column span="2">
					<p>
						<label>CMR Issuing Country: </label>
					</p>
				</cmr:column>
				<cmr:column span="2" width="350">
					<form:select dojoType="dijit.form.FilteringSelect"
						id="issuingCntry" searchAttr="name" style="display: inline-block;"
						maxHeight="200" required="false" path="issuingCntry"
						placeHolder="CMR Issuing Country">
					</form:select>
				</cmr:column>
			</cmr:row>
			<cmr:row topPad="8">
				<cmr:column span="6">
					<h3>Rep Team List </h3>
				</cmr:column>
			</cmr:row>
			<cmr:row topPad="10" addBackground="false">
				<cmr:column span="6">
					<cmr:grid url="/code/repTeamList.json" id="rtUrlsGrid" span="6"
						usePaging="true" hasCheckbox="true"
						checkBoxKeys="issuingCntry,repTeamCd,repTeamMemberNo">
						<cmr:gridParam fieldId="issuingCntry" value=":issuingCntry" />
						<cmr:gridCol width="16%" field="issuingCntry"
							header="Issuing Country">
							<cmr:formatter functionName="RepTeamService.linkFormatter" /> 
						</cmr:gridCol>
						<cmr:gridCol width="20%" field="repTeamCd" header="Rep Team Code" />
						<cmr:gridCol width="20%" field="repTeamMemberNo" header="Rep Team Member No" />
						<cmr:gridCol width="35%" field="repTeamMemberName"	header="Rep Team Member Name" />
					</cmr:grid>
				</cmr:column>
			</cmr:row>
			<cmr:row topPad="10">
			</cmr:row>
		</cmr:section>
	</form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
	<cmr:buttonsRow>
		<cmr:button label="Add Rep Team " id="add_rt" onClick="RepTeamService.addRepTeam()"
			highlight="true" />
		<cmr:button label="Remove selected records"
			onClick="RepTeamService.removeSelectedMappings()" highlight="true"
			pad="true" />
		<cmr:button label="Back to Code Maintenance Home"
			onClick="backToCodeMaintHome()" pad="true" />
	</cmr:buttonsRow>
</cmr:section>