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
<script>
  dojo.addOnLoad(function() {
  });
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/status_desc" name="frmCMRSearch" class="ibm-column-form ibm-styled-form" modelAttribute="statusdesc">
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Status Description</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/statusdesclist.json" id="statusDescGrid" span="6" >
            <cmr:gridCol width="5%" field="reqStatus" header="Request Status">
              <cmr:formatter functionName="statusdescFormatter" />
            </cmr:gridCol>
            
            <cmr:gridCol width="5%" field="cmrIssuingCntry" header="Issuing Country" />
            <cmr:gridCol width="15%" field="statusDesc" header="Status Description"//>
             
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
    <cmr:button label="Add Status Description" onClick="StatusDescService.addStatusDesc()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="statusdesc" />