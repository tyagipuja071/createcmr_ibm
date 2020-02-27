<%@page import="com.ibm.cio.cmr.request.model.code.CnaeModel"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.FieldInfoModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  CnaeModel cnae = (CnaeModel) request.getAttribute("cnae");
			boolean newEntry = false;
			if (cnae.getState() == BaseModel.STATE_NEW) {
				newEntry = true;
			}
			String title = newEntry ? "Add BR CNAE Info" : "Update BR CNAE Info";
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}"
	type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
  	// isu cd
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
        var dropdownIsu = {
            listItems : modelIsu
        };        
        
        FilteringDropdown.loadFixedItems('isuCd', null, dropdownIsu);
        FilteringDropdown.loadItems('subIndustryCd', null,'bds', 'fieldId=Subindustry');
        FilteringDropdown.loadItems('isicCd', null,'bds', 'fieldId=ISIC');
	    FormManager.addValidator('cnaeNo', Validators.REQUIRED, [ 'CNAE No.' ]);
	    FormManager.addValidator('cnaeDescrip', Validators.REQUIRED, [ 'CNAE Description' ]);
	    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC Code' ]);
	    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ]);
	    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Sub Industry Code' ]);
	
  });
  
  function saveCnae() {
  	    FormManager.ready();
  	    var cnaeNo = FormManager.getActualValue('cnaeNo');
  	    if(cnaeNo != null && cnaeNo != '' && cnaeNo.length <= 4){  
        var result = cmr.query('CNAE.SAVE', {
        CNAE : cnaeNo
        });      
        if(result.ret1 > 0 && dojo.byId('frmCMR').title == 'Add BR CNAE Info'){
        cmr.showAlert("Entry with this CNAE NO. already exists.");
        }  
        else{
        if(!Number.isInteger(cnaeNo) && !isNaN(cnaeNo)){
         FormManager.save('frmCMR');
        }
        else{
         cmr.showAlert("Please enter a valid Cnae Number Fomart.");
        }
      }
    }
    else{
      cmr.showAlert("Please enter a valid Cnae Number Fomart(Max Length 4).");
    }
  }
  function backToList() {
    window.location = '${contextPath}/code/cnae';
  }
</script>
<cmr:boxContent>
	<cmr:tabs />

	<form:form method="POST" action="${contextPath}/code/cnaedetails"
		id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form"  title="<%=title%>"
		modelAttribute="cnae">
		<cmr:modelAction formName="frmCMR" />
		<cmr:section>
			<cmr:row topPad="8">
				<cmr:column span="6">
					<h3><%=title%></h3>
				</cmr:column>
			</cmr:row>
			<cmr:row>
				<cmr:column span="1" width="160">
					<p>
						<cmr:label fieldId="cnaeNo">CNAE No.: </cmr:label>
					</p>
				</cmr:column>
				<%if (newEntry) {%>
				<cmr:column span="2" width="200">
					<p>
						<form:input path="cnaeNo" id="cnaeNo"
							dojoType="dijit.form.TextBox" style="width:130px"
							value="${ cnae.cnaeNo }" />
					</p>
				</cmr:column>
				<%}else { %>
				<cmr:column span="2">
					<p>${cnae.cnaeNo}</p>
					<form:hidden id="cnaeNo" path="cnaeNo" />
				</cmr:column>
				<%} %>
			</cmr:row>
			<cmr:row>
				<cmr:column span="1" width="160">
					<p>
						<cmr:label fieldId="cnaeDescrip">CNAE Description: </cmr:label>
					</p>
				</cmr:column>
				<cmr:column span="2" width="200">
					<p>
						<form:input path="cnaeDescrip" id="cnaeDescrip"
							dojoType="dijit.form.TextBox" style="width:130px"
							value="${ cnae.cnaeDescrip }" />
					</p>
				</cmr:column>
			</cmr:row>
			<cmr:row>			
				<cmr:column span="1" width="160">
					<p>
						<cmr:label fieldId="isicCd">ISIC Code:</cmr:label>
					</p>
				</cmr:column>
				<cmr:column span="2" width="200">
					<form:select dojoType="dijit.form.FilteringSelect" id="isicCd"
						searchAttr="name" style="display: block; width:300px"
						maxHeight="200" required="false" path="isicCd"
						placeHolder="Select ISIC Code">
					</form:select>
				</cmr:column>
			</cmr:row>
			<cmr:row>
				<cmr:column span="1" width="160">
					<p>
						<cmr:label fieldId="isuCd">ISU Code:</cmr:label>
					</p>
				</cmr:column>
				<cmr:column span="2" width="200">
					<form:select dojoType="dijit.form.FilteringSelect" id="isuCd"
						searchAttr="name" style="display: block; width:170px"
						maxHeight="200" required="false" path="isuCd"
						placeHolder="Select ISU Code">
					</form:select>
				</cmr:column>
			</cmr:row>
			<cmr:row>
				<cmr:column span="1" width="160">
					<p>
						<cmr:label fieldId="subIndustryCd">Sub Industry Code:</cmr:label>
					</p>
				</cmr:column>
				 	<cmr:column span="2" width="200">
					 <form:select dojoType="dijit.form.FilteringSelect" id="subIndustryCd" searchAttr="name" style="display: block; width:240px" maxHeight="200"
                required="false" path="subIndustryCd" placeHolder="Select SubIndustry Code Code">
              </form:select>
				</cmr:column>
			</cmr:row>
		</cmr:section>
	</form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
	<cmr:buttonsRow>
		<%
		  if (newEntry) {
		%>
		<cmr:button label="Save" onClick="saveCnae()" highlight="true" />
		<cmr:button label="Back to CNAE List" onClick="backToList()"
			pad="true" />
		<%
		  } else {
		%>

		<cmr:button label="Update" onClick="saveCnae()" highlight="true" />
		<cmr:button label="Back to Cnae List" onClick="backToList()"
			pad="false" />
		<%
		  }
		%>
	</cmr:buttonsRow>
</cmr:section>

<cmr:model model="cnae" />