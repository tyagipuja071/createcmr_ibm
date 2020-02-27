<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
  FormManager.ready();
  });
  
var SboCollectorService = (function() {
  return {
    stateFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var stateCd = rowData.stateCd;

      return '<a href="javascript: SboCollectorService.open(\'' + stateCd + '\')">' + stateCd + '</a>';
    },
    open : function(stateCd) {
      document.forms['frmCMRSbo'].setAttribute('action', cmr.CONTEXT_ROOT + '/code/brSboCollectordetails/?stateCd=' + stateCd);
      document.forms['frmCMRSbo'].submit();    
    },
    addSbo : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/brSboCollectordetails';
    }
  };
})();  
function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/brSboCollector" name="frmCMRSbo" class="ibm-column-form ibm-styled-form" modelAttribute="brSboCollectors">
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>SBO/Collector Mappings</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/sbolist.json" id="sboListGrid" span="6" useFilter="true">
            <cmr:gridCol width="5%" field="stateCd" header="State Code">
            <cmr:formatter functionName="SboCollectorService.stateFormatter" />
            </cmr:gridCol>  
            <cmr:gridCol width="13%" field="stateName" header="State Name" />
            <cmr:gridCol width="28%" field="sbo" header="SBO"/>
            <cmr:gridCol width="13%" field="mrcCd" header="MRC Code" />
            <cmr:gridCol width="13%" field="collectorNo" header="Collector No" />
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
    <!-- <cmr:button label="Add SBO/Collector" onClick="SboCollectorService.addSbo()" highlight="true" /> -->
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="brSboCollectors" />