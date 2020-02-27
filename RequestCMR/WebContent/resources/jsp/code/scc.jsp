<%@page import="com.ibm.cio.cmr.request.model.code.SCCModel"%>
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
  SCCModel scc = (SCCModel) request.getAttribute("scc");
  boolean newEntry = false;
  if (scc.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
  }
  boolean nonUS = "Y".equals(request.getParameter("nonUS"));
  if (nonUS){
    scc.setnSt("''");
  }
  String title = newEntry ? "Add US SCC" : "View SCC";
  title = nonUS ? "Add Non-US SCC" : title;
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    <%if (newEntry){%>
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
    FormManager.ready();
    
    FormManager.addValidator('nSt', Validators.REQUIRED, [ 'State Code' ]);
    FormManager.addValidator('nCity', Validators.REQUIRED, [ 'City' ]);
    FormManager.addValidator('nCnty', Validators.REQUIRED, [ 'County' ]);
    
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
            ${scc.nCity}
            <form:hidden path="nCity"/>
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
            ${scc.nCnty}
            <form:hidden path="nCnty"/>
          <%} %>
          </p>
        </cmr:column>
      </cmr:row>
      <form:hidden path="cZip"/>
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
   <%if (newEntry){ %>
      <cmr:button label="Save" onClick="SCCService.saveSCC()" highlight="true" />
      <cmr:button label="Back to SCC  List" onClick="backToList()" pad="true"/>
   <%} else {%>
      <cmr:button label="Back to SCC  List" onClick="backToList()" pad="false"/>
   <%} %>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="scc" />