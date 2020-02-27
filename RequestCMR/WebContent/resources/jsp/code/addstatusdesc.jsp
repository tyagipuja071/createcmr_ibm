<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.StatusDescModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  StatusDescModel statusdesc = (StatusDescModel) request.getAttribute("statusdesc");
    boolean newEntry = false;
	   if (statusdesc.getState() == BaseModel.STATE_NEW) {
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
  FormManager.addValidator('reqStatus', Validators.REQUIRED, [ 'Request Status' ]);
  FormManager.addValidator('cmrIssuingCntry', Validators.REQUIRED, [ 'Issuing Country' ]);
  FormManager.addValidator('statusDesc', Validators.REQUIRED, [ 'Status Description' ]);
<%}%>
  FormManager.ready();
  });
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/addstatusdesc" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="statusdesc">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Status Description" : "Update Status Description"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="reqStatus">Request Status: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${statusdesc.reqStatus}</p>
          <form:hidden id="reqStatus" path="reqStatus" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="reqStatus">Request Status: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="reqStatus" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
       </cmr:row>
      <%
        }
      %>
      
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${statusdesc.cmrIssuingCntry}</p>
          <form:hidden id="cmrIssuingCntry" path="cmrIssuingCntry" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="cmrIssuingCntry" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
       </cmr:row>
      <%
        }
      %>
    
        
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="statusDesc">Status Description: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="statusDesc" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>

    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="StatusDescService.saveStatusDesc(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Save" onClick="StatusDescService.saveStatusDesc(false)" highlight="true" />
    <%} %>
    <cmr:button label="Add Another Status Description" onClick="StatusDescService.addStatusDesc()" highlight="false" pad="true" />
    <cmr:button label="Back to Status Description List" onClick="window.location = '${contextPath}/code/status_desc'" pad="true" />
  </cmr:buttonsRow>
  <br>
  
</cmr:section>
<cmr:model model="statusdesc" />