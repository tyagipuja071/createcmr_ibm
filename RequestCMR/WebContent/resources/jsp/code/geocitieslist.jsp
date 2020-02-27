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
      var url = cmr.CONTEXT_ROOT + '/code/geocitiesdeflist.json';
      CmrGrid.refresh('geoCitiesGrid', url, 'cmrIssuingCntry=:cmrIssuingCntry');
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      FilteringDropdown.loadItems('stateProv', 'stateProv_spinner', 'bds', 'fieldId=StateProvConfig&cmrIssuingCntry=' + cntry);
    });	
  
   //FilteringDropdown.loadItems('stateProv', 'stateProv_spinner', 'bds', 'fieldId=StateProvConfig');
   dojo.connect(FormManager.getField('stateProv'), 'onChange', function(value) {
     var url = cmr.CONTEXT_ROOT + '/code/geocitiesdeflist.json';
     CmrGrid.refresh('geoCitiesGrid', url, 'stateProv=:stateProv&cmrIssuingCntry=:cmrIssuingCntry');
   });  
    
  FormManager.ready();
  });
  
var GeoCitiesService = (function() {
  return {
      linkFormatter : function(value, rowIndex) {
      console.log(">> ROW INDEX >> " + rowIndex);
      var id = this.grid.getItem(rowIndex).cityId[0];
      var id1 =  this.grid.getItem(rowIndex).cmrIssuingCntry[0];
      var id2 = this.grid.getItem(rowIndex).cityDesc[0];
      return '<a href="javascript: GeoCitiesService.open(\'' + id +'\',\''+id1+ '\',\''+id2+'\')">' + value + '</a>';
    },
     addgeocity : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/geocitiesmain';
    },
    open : function(value,value2,value3) {
      var loc = cmr.CONTEXT_ROOT + '/code/geocitiesmain?cityId=' + encodeURIComponent(value)+'&cmrIssuingCntry='+encodeURIComponent(value2)+'&cityDesc='+encodeURIComponent(value3);
      window.location = loc;
    } 
  };
})();  
function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}
</script>
<cmr:boxContent>
  <cmr:tabs />
  
  <form:form method="POST" action="${contextPath}/code/geocitieslist" name="frmCMRGeoCitiesList" class="ibm-column-form ibm-styled-form" modelAttribute="geocities">
  <cmr:model model="geocities" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Geo Cities Definition</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="8" addBackground="true">
        <cmr:column span="3" width="400"> 
          <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCntry" searchAttr="name" style="display: inline-block;" maxHeight="200"
            required="false" path="cmrIssuingCntry" placeHolder="CMR Issuing Country">
          </form:select>
        </cmr:column>
        <cmr:column span="3" width="400">
          for State/Province:  
          <form:select dojoType="dijit.form.FilteringSelect" id="stateProv" searchAttr="name" style="display: inline-block;" maxHeight="200"
            required="false" path="stateProv" placeHolder="State Province">
          </form:select>
        </cmr:column>
      </cmr:row>  
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/geocitiesdeflist.json" id="geoCitiesGrid" useFilter="true" usePaging="false">
            <cmr:gridCol width="7%" field="cmrIssuingCntry" header="CMR Issuing Country"/>
            <cmr:gridCol width="9%" field="cityId" header="City/Location ID">
              <cmr:formatter functionName="GeoCitiesService.linkFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="7%" field="cityDesc" header="City/Location Description"/>
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
    <cmr:button label="Add LA Geo City Definition" onClick="GeoCitiesService.addgeocity()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>