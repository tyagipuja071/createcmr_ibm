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
  
var CntryGeoDefService = (function() {
  return {
      linkFormatter : function(value, rowIndex) {
      var id = this.grid.getItem(rowIndex).geoCd[0];
      var id1 =  this.grid.getItem(rowIndex).cmrIssuingCntry[0];
      return '<a href="javascript: CntryGeoDefService.open(\'' + id +'\',\''+id1+ '\')">' + value + '</a>';
    },
     addcntrygeodef : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/cntrygeodefmain';
    },
    open : function(value,value2) {
      window.location = cmr.CONTEXT_ROOT + '/code/cntrygeodefmain?geoCd=' + encodeURIComponent(value)+'&cmrIssuingCntry='+encodeURIComponent(value2);
    } 
  };
})();  
function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}
</script>
<cmr:boxContent>
  <cmr:tabs />
  
  <form:form method="POST" action="${contextPath}/code/cntrygeodef" name="frmCMRCntryGeoDef" class="ibm-column-form ibm-styled-form" modelAttribute="cntrygeodef">
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Country-GEO Definition</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/cntrygeodeflist.json" id="cntrygeodefUrlsGrid" span="6" >
            <cmr:gridCol width="7%" field="geoCd" header="Geo Code" />
            <cmr:gridCol width="7%" field="cmrIssuingCntry" header="CMR Issuing Country"/>
            <cmr:gridCol width="9%" field="cntryDesc" header="Country Description">
              <cmr:formatter functionName="CntryGeoDefService.linkFormatter" />
            </cmr:gridCol>  
            <cmr:gridCol width="9%" field="comments" header="Comments"/>                   
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
    <cmr:button label="Add Country-GEO Definition" onClick="CntryGeoDefService.addcntrygeodef()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="cntrygeodef" />