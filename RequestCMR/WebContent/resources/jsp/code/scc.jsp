<%@page import="com.ibm.cio.cmr.request.model.code.SCCModel"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.FieldInfoModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="org.apache.wink.common.internal.utils.StringUtils" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  SCCModel scc = (SCCModel) request.getAttribute("scc");
  boolean newEntry = false;
  if (scc.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
  }
  boolean nonUS = "Y".equals(request.getParameter("nonUS"));
  if (nonUS){
    scc.setnSt("''");
  }
  /* String title = newEntry ? "Add US SCC" : "View SCC"; */
  String title = newEntry ? "Add US SCC" : "Update SCC";
  title = nonUS ? "Add Non-US SCC" : title;
  
  String newZip = "" + scc.getcZip();
  
  if (newZip.length() < 5) {
    while (newZip.length() < 5) {
      newZip = "0" + newZip;
    }
  }
  
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FormManager.ready();
    FormManager.addValidator('nSt', Validators.REQUIRED,[ 'State Code' ]);
    FormManager.addValidator('nCity', Validators.REQUIRED, [ 'City' ]);
    FormManager.addValidator('cCity', Validators.REQUIRED,[ 'City Code' ]);
    FormManager.addValidator('cCity', Validators.NUMBER,[ 'City Code' ]);
    FormManager.addValidator('nCnty', Validators.REQUIRED, [ 'County' ]);
    FormManager.addValidator('cCnty', Validators.REQUIRED, [ 'County Code' ]);
    FormManager.addValidator('cCnty', Validators.NUMBER, [ 'County Code' ]);
    FormManager.addValidator('nLand', Validators.REQUIRED, [ 'Land' ]);
    FormManager.addValidator('cZip', Validators.REQUIRED, [ 'Zip' ]);
    FormManager.addValidator('cZip', Validators.NUMBER, [ 'Zip' ]);
    
    var cCityVal = FormManager.getActualValue('cCity');
    var cCntyVal = FormManager.getActualValue('cCnty');
    var cZipVal = FormManager.getActualValue('cZip');
        
    cCityVal == 0.0 ? FormManager.setValue('cCity', ''):FormManager.setValue('cCity', Number(cCityVal).toFixed(0));
    cCntyVal == 0.0 ? FormManager.setValue('cCnty', ''):FormManager.setValue('cCnty', Number(cCntyVal).toFixed(0));
    cZipVal == 0.0 ? FormManager.setValue('cZip', ''):FormManager.setValue('cZip', cZipVal);
    
    <%if (newEntry ){%>
        <%if (!nonUS ){%>
        FormManager.readOnly('nLand');
       <%}%>
    <%}%>
    
  dojo.connect(FormManager.getField('nSt'), 'onChange', function(value) {
      var cStVal =0;
  	  var nSt = FormManager.getActualValue('nSt');
  	  if(nSt=="''"){
  	  	cStVal=99;
  	  }else{
	      var result = cmr.query('SCC_NST', {
	        N_ST : nSt
	      });
	  	  cStVal = result.ret1;
  	  }
      FormManager.setValue('cSt', cStVal);
    });
    
    <%if (newEntry){%>
        var ids1 = cmr.query('SCCLANDS', {_qall  : 'Y'});
        var model1 = {
            identifier : "id",
            label : "name",
            selectedItem : null,
            items : []
        };
        for (var i =0; i < ids1.length; i++){
            model1.items.push({id : ids1[i].ret1, name : ids1[i].ret1});
        }
        var dropdown1 = {
            listItems : model1
        };
        FilteringDropdown.loadFixedItems('nLand', null, dropdown1);
      <%if (!nonUS){%>
        var ids = cmr.query('SCCSTATES', {_qall  : 'Y'});
        var model = { 
            identifier : "id", 
            label : "name",
            selectedItem : null,
            items : []
        };
        for (var i =0; i < ids.length; i++){
          if (ids[i].ret1 != "''"){
            model.items.push({id : ids[i].ret1, name : ids[i].ret1});
          }
        }
        var dropdown = {
            listItems : model
        };
        FilteringDropdown.loadFixedItems('nSt', null, dropdown);
                
      <%} else {%>
        var ids = cmr.query('SCCCOUNTRIES', {_qall  : 'Y'});
        var model = { 
            identifier : "id", 
            label : "name",
            selectedItem : null,
            items : []
        };
        for (var i =0; i < ids.length; i++){
          model.items.push({id : ids[i].ret1, name : ids[i].ret1});
        }
        var dropdown = {
            listItems : model
        };
        FilteringDropdown.loadFixedItems('nCnty', null, dropdown);
      <%}%>
    <%}%>
  });
  function backToList(){
    window.location = '${contextPath}/code/scclist';
  }

</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/sccdetails" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="scc">
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
            <cmr:label fieldId="nSt">State Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <%if (newEntry){ %>
          
            <%if (!nonUS){ %>
              <form:select dojoType="dijit.form.FilteringSelect" id="nSt" searchAttr="name" style="display: block; width:100px" maxHeight="200"
                required="false" path="nSt" placeHolder="Select State Code">
              </form:select>
            <%} else {%>
            ${scc.nSt}
            <form:hidden path="nSt"/>
            <%} %>          
          <%} else { %>
            ${scc.nSt}
            <form:hidden path="nSt"/>
          <%} %>
          <form:hidden path="cSt"/>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="nCity">City: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <%if (newEntry){ %>
          <form:input path="nCity" id="nCity" maxlength="13"/>
          <%} else { %>
            <form:input path="nCity" id="nCity" maxlength="13"/>
          <%} %>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="cCity">City Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <%if (newEntry){ %>
          <form:input path="cCity" id="cCity" maxlength="4"/>
          <%} else { %>
            <form:input path="cCity" id="cCity" maxlength="4"/>
          <%} %>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="nCnty">County: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <%if (newEntry){ %>
            <%if (!nonUS){ %>
              <form:input path="nCnty" id="nCnty" maxlength="14"/>
            <%} else {%>
              <form:select dojoType="dijit.form.FilteringSelect" id="nCnty" searchAttr="name" style="display: block; width:200px" maxHeight="200"
                required="false" path="nCnty" placeHolder="Select Country">
              </form:select>
            <%} %>
          <%} else { %>
            <form:input path="nCnty" id="nCnty" maxlength="14"/>
          <%} %>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="cCnty">County Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <%if (newEntry){ %>
              <form:input path="cCnty" id="cCnty" maxlength="3"/>
          <%} else { %>
            <form:input path="cCnty" id="cCnty" maxlength="3"/>
          <%} %>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160"> 
          <p>
            <cmr:label fieldId="nLand">Land Cntry: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <%if (newEntry){ %>
          <%if (!nonUS){ %>
             <form:input path="nLand" id="nLand"  maxlength="2" value="US" />
             <%} else {%>
          <form:select dojoType="dijit.form.FilteringSelect" id="nLand" searchAttr="name" style="display: block; width:200px" maxHeight="200"  maxlength="2"
               required="false" path="nLand" placeHolder="Select Land">
             </form:select>
             <%} %>  
          <%} else { %>
            <form:input path="nLand" id="nLand"  maxlength="2" />
          <%} %>
          </p>
        </cmr:column>
      </cmr:row>
      <form:hidden path="sccId"/>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="cZip">Zip Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="200">
          <p>
          <%if (newEntry){ %>
          <form:input path="cZip" id="cZip" maxlength="5" />
          <%} else { %>
            <%-- <form:input path="cZip" id="cZip" maxlength="9" /> --%>
            <input type="text" id="cZip" name="cZip" maxlength="9" value="<%= newZip %>"/>
          <%} %>
          </p>
        </cmr:column>
      </cmr:row>
      <form:hidden path="sccId"/>
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
   <%if (newEntry){ %>
      <cmr:button label="Save" onClick="SCCService.saveSCC(true,'${scc.sccId}')" highlight="true" />
      <cmr:button label="Back to SCC  List" onClick="backToList()" pad="true"/>
   <%} else {%>
      <cmr:button label="Back to SCC  List" onClick="backToList()" pad="false"/>
      <cmr:button label="Delete" onClick="SCCService.removeSCC('${scc.sccId}')" pad="false"/>
      <cmr:button label="Update" onClick="SCCService.saveSCC(false,'${scc.sccId}')" highlight="true" />
   <%} %>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="scc" />