<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
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
    
    
  });
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/scc/process" name="frmCMR" class="ibm-column-form ibm-styled-form"
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
        <cmr:column span="1" width="100">
          <p>
            <label>County/Country: </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="150">
          <form:input path="nCnty" id="nCnty" dojoType="dijit.form.TextBox" style="width:130px"/>
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
            <cmr:gridCol width="150px" field="nSt" header="State Code" >
              <cmr:formatter functionName="SCCService.stateFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="auto" field="nCity" header="City" >
              <cmr:formatter functionName="SCCService.cityFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="250px" field="nCnty" header="County / Country" />

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
    <cmr:button label="Add US SCC" onClick="SCCService.addSCC(false)" highlight="true" />
    <cmr:button label="Add Non-US SCC" onClick="SCCService.addSCC(true)" pad="true" />
    
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
