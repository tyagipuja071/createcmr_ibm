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

  <form:form method="POST" action="${contextPath}/code/bds_tbl_info" name="frmCMRSearch" class="ibm-column-form ibm-styled-form" modelAttribute="bds">
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Business Data Source</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/bds/bdslist.json" id="bdsGrid" span="6" >
            <cmr:gridCol width="25%" field="fieldId" header="Field Id">
              <cmr:formatter functionName="bdsrecFormatter" />
            </cmr:gridCol>
            
            <cmr:gridCol width="15%" field="schema" header="Schema" />
            <cmr:gridCol width="30%" field="tbl" header="Table"/>            
            <cmr:gridCol width="25%" field="cd" header="Code"/>
            <cmr:gridCol width="30%" field="desc" header="Description"/>
            <cmr:gridCol width="15%" field="dispType" header="Dispaly Type"/>
            <cmr:gridCol width="20%" field="orderByField" header="Order by Field"/>
            <cmr:gridCol width="15%" field="cmt" header="Cmt"/>
             
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
    <cmr:button label="Add Business Data Source" onClick="BdsService.addBds()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="bds" />