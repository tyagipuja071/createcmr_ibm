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
  
  FilteringDropdown.loadItems('cmrIssuingCntry', 'cmrIssuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');
  dojo.connect(FormManager.getField('cmrIssuingCntry'), 'onChange', function(value) {
      var url = cmr.CONTEXT_ROOT + '/code/collectornolist.json';
      CmrGrid.refresh('collectorNoGrid', url, 'cmrIssuingCntry=:cmrIssuingCntry');
    });
  FormManager.ready();
  });
  
var CollectorNameNoService = (function() {
  return {
      linkFormatter : function(value, rowIndex) {
      var id = this.grid.getItem(rowIndex).collectorNo[0];
      var id1 =  this.grid.getItem(rowIndex).cmrIssuingCntry[0];
      return '<a href="javascript: CollectorNameNoService.open(\'' + id +'\',\''+id1+ '\')">' + value + '</a>';
    },
     addcollectorno : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/collectornamenomain';
    },
    open : function(value,value2) {
      window.location = cmr.CONTEXT_ROOT + '/code/collectornamenomain?collectorno=' + encodeURIComponent(value)+'&cmrIssuingCntry='+encodeURIComponent(value2);
    } 
  };
})();  
function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}
</script>
<cmr:boxContent>
  <cmr:tabs />
  
  <form:form method="POST" action="${contextPath}/code/collectornolist" name="frmCMRCollectorNoList" class="ibm-column-form ibm-styled-form" modelAttribute="collector">
  <cmr:model model="collector" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Collector Number Definition</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="8" addBackground="true">
        <cmr:column span="3" width="400"> 
          <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCntry" searchAttr="name" style="display: inline-block;" maxHeight="200"
            required="false" path="cmrIssuingCntry" placeHolder="CMR Issuing Country">
          </form:select>
        </cmr:column>
      </cmr:row>  
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/collectornolist.json" id="collectorNoGrid" span="6" usePaging="true">
            <cmr:gridCol width="7%" field="cmrIssuingCntry" header="CMR Issuing Country"/>
            <cmr:gridCol width="9%" field="collectorNo" header="Collector Number">
              <cmr:formatter functionName="CollectorNameNoService.linkFormatter" />
            </cmr:gridCol>
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
    <cmr:button label="Add LA Collector No Definition" onClick="CollectorNameNoService.addcollectorno()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>