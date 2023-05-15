<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="java.util.List" %>
<%@page import="com.ibm.cio.cmr.request.util.SystemParameters" %>
<%@page import="com.ibm.cio.cmr.request.util.BluePagesHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
    // CREATCMR-5447
    boolean showFlag = true;

    AppUser user = AppUser.getUser(request);
    if (BluePagesHelper.isUserInUSTAXBlueGroup(user.getIntranetId())) {
       showFlag = false;
    }
    
    String taxTeamFlag = request.getParameter("taxTeamFlag");
    if("Y".equals(taxTeamFlag)) {
      showFlag = false;
    }
    
    if((user.isAdmin() && !"Y".equals(taxTeamFlag)) && !BluePagesHelper.isUserInUSTAXBlueGroup(user.getIntranetId())){
      showFlag = true;
    }
    
    // CREATCMR-5447
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<style>
 .cmr-filter {
   height:20px;
   font-size: 12px;
   padding-top: 3px !important;
 }
</style>
<script>
  function backToSearchHome() {
    window.location = cmr.CONTEXT_ROOT + '/searchhome';
  }
  dojo.addOnLoad(function(){
    var ids = cmr.query('SCCSTATES', {_qall  : 'Y'});
    var model = { 
        identifier : "id", 
        label : "name",
        selectedItem : null,
        items : []
    };
    for (var i =0; i < ids.length; i++){
      if (ids[i].ret1 == "''"){
        model.items.push({id : ids[i].ret1, name : 'Non-US'});
      } else {
        model.items.push({id : ids[i].ret1, name : ids[i].ret1});
      }
    }
    var dropdown = {
        listItems : model
    };
    FilteringDropdown.loadFixedItems('nSt', null, dropdown);
    
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
    
  });

  // CREATCMR-9311: export the scc list to excel file
  var filterParams = '';
  function sccListGrid_GRID_onLoad(data){
    if(data && data.length>0) {
      var nSt = FormManager.getActualValue('nSt');
	    var cCity = FormManager.getActualValue('cCity');
      var cCnty = FormManager.getActualValue('cCnty');
      var nLand = FormManager.getActualValue('nLand');
      
      filterParams = 'nSt=' + nSt + '&nCity=' + cCity + '&nCnty=' + cCnty + '&nLand=' + nLand;
    } else {
      filterParams = '';
    }
  }
  // CREATCMR-9311 end
</script>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:form method="POST" action="${contextPath}/code/scc/process" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="scc" id="frmCMR">
    <cmr:model model="scc" />
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8" addBackground="true">
        <cmr:column span="1" width="80" >
          <p>
            <label>State Code: </label>
          </p>
        </cmr:column>
        <cmr:column span="1" width="120" >
          <form:select dojoType="dijit.form.FilteringSelect" id="nSt" searchAttr="name" style="display: block; width:100px" maxHeight="200"
            required="false" path="nSt" placeHolder="Select State Code">
          </form:select>
        </cmr:column>
        <cmr:column span="1" width="60">
          <p>
            <label>City: </label>
          </p>
        </cmr:column>
        <cmr:column span="1" width="170">
          <form:input path="nCity" id="nCity" dojoType="dijit.form.TextBox" style="width:150px"/>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <label>County/Country: </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="150">
          <form:input path="nCnty" id="nCnty" dojoType="dijit.form.TextBox" style="width:130px"/>
        </cmr:column>
        <cmr:column span="1" width="100">
          <p>
            <label>Land Cntry: </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="140">
          <form:select dojoType="dijit.form.FilteringSelect" id="nLand" searchAttr="name" style="display: block; width:130px" maxHeight="100"
               required="false" path="nLand" placeHolder="Select Land">
          </form:select>
        </cmr:column>
        <cmr:column span="1" width="100">
          <cmr:button label="Search" onClick="SCCService.searchSCC()" styleClass="cmr-filter"></cmr:button>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>SCC List</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/scclist.json" id="sccListGrid" span="6" height="400" usePaging="false">
            <cmr:gridParam fieldId="nCity" value=":nCity" />
            <cmr:gridParam fieldId="nSt" value=":nSt" />
            <cmr:gridCol width="120px" field="nLand" header="Land Cntry" />
            <cmr:gridCol width="80px" field="cSt" header="State Code">
              <cmr:formatter>
                function(value){
                  return ( "00" + value ).substr( -2 );
                }
              </cmr:formatter>
            </cmr:gridCol>
            <cmr:gridCol width="100px" field="cCnty" header="County Code">
              <cmr:formatter>
                function(value){
                  return ( "000" + value ).substr( -3 );
                }
              </cmr:formatter>
            </cmr:gridCol>
            <cmr:gridCol width="80px" field="cCity" header="City Code">
              <cmr:formatter>
                function(value){
                  return ( "0000" + value ).substr( -4 );
                }
              </cmr:formatter>
            </cmr:gridCol>
            <cmr:gridCol width="120px" field="nSt" header="State" >
              <cmr:formatter functionName="SCCService.stateFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="150px" field="nCnty" header="County / Country" />
            <cmr:gridCol width="150px" field="nCity" header="City" >
            <% if(showFlag) { %>
              <cmr:formatter functionName="SCCService.cityFormatter" />
            <% } %>
            </cmr:gridCol>
            <cmr:gridCol width="115px" field="cZip" header="Zip Code">
              <cmr:formatter functionName="SCCService.newZipFormatter"/>
            </cmr:gridCol>
            <cmr:gridCol width="100px" field="action" header="Action" >
            <% if(showFlag) { %>
              <cmr:formatter functionName="SCCService.sccActionIcons" />
            <% } %>
            </cmr:gridCol>
            <cmr:gridCol width="0px" field="sccId" header="sccId" />
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
      </cmr:row>
    </cmr:section>
  </cmr:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <% if(showFlag) { %>
    <cmr:button label="Add US SCC" onClick="SCCService.addSCC(false)" highlight="true" />
    <cmr:button label="Add Non-US SCC" onClick="SCCService.addSCC(true)" pad="true" />
    <cmr:button label="Export SCC to Excel" onClick="SCCService.exportToExcel(filterParams)" pad="true" />
    <% } %>  
    <% if("Y".equals(taxTeamFlag)) { %>
    <cmr:button label="Back to Search Home" onClick="backToSearchHome()" pad="true" />
    <%
      } else {
    %>
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
    <%
    }
    %>
  </cmr:buttonsRow>
</cmr:section>

<form _csrf="GhtjeYhfngleOImde2" id="sccListDownLoad" name="sccListDownLoad" method="POST">
  <input type="hidden" name="_csrf" id="_csrf" value="GhtjeYhfngleOImde2" />
</form>
