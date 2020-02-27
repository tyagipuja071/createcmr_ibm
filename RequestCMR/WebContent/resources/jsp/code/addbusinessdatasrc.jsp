<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.BusinessDataSrcModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  BusinessDataSrcModel bds = (BusinessDataSrcModel) request.getAttribute("bds");
    boolean newEntry = false;
	   if (bds.getState() == BaseModel.STATE_NEW) {
        newEntry = true;
        
      } else {
      }
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
<%if (newEntry) {%>
  FormManager.addValidator('fieldId', Validators.REQUIRED, [ 'Field Id' ]);
  FormManager.addValidator('schema', Validators.REQUIRED, [ 'Schema' ]);
  FormManager.addValidator('tbl', Validators.REQUIRED, [ 'Table' ]);
  FormManager.addValidator('cd', Validators.REQUIRED, [ 'Code' ]);
  FormManager.addValidator('desc', Validators.REQUIRED, [ 'Description' ]);
  FormManager.addValidator('dispType', Validators.REQUIRED, [ 'Display Type' ]);
  FormManager.addValidator('orderByField', Validators.REQUIRED, [ 'Order by Field' ]);
<%}%>
  FormManager.ready();
  });
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/addbds" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="bds">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Business Data Source" : "Update Business Data Source"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="fieldId">Field Id: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${bds.fieldId}</p>
          <form:hidden id="fieldId" path="fieldId" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="fieldId">Field Id: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="fieldId" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
       </cmr:row>
      <%
        }
      %>
    
    <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="schema">Schema: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="schema" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>    
    
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="tbl">Table: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="tbl" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="cd">Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="cd" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="desc">Description: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="desc" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="dispType">Display Type: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="dispType" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="orderByField">Order By Field: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="orderByField" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="cmt">CMT: </cmr:label>
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
      <cmr:button label="Save" onClick="BdsService.saveBds(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Save" onClick="BdsService.saveBds(false)" highlight="true" />
    <%} %>
    <cmr:button label="Add Another Business Data Source" onClick="BdsService.addBds()" highlight="false" pad="true" />
    <cmr:button label="Back to Business Data Source List" onClick="window.location = '${contextPath}/code/bds_tbl_info'" pad="true" />
  </cmr:buttonsRow>
  <br>
  
</cmr:section>
<cmr:model model="bds" />