<%@page import="com.ibm.cio.cmr.request.config.ConfigItem"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collection"%>
<%@page import="org.springframework.ui.ModelMap"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/chart.bundle.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/system/metrics.js?${cmrv}" type="text/javascript"></script>
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<%
  
%> 
<script>
  var _typeHandler = null;
  dojo.addOnLoad(function() {
    var ids1 = cmr.query('SYSTEM.GET_AVAILABLE_GEOS', {_qall  : 'Y'});
    var model1 = { 
        identifier : "id", 
        label : "name",
        items : []
    };
    model1.items.push({id : '', name : ''});
    for (var i =0; i < ids1.length; i++){
      model1.items.push({id : ids1[i].ret1, name : ids1[i].ret1 + ' only'});
    }
    var dropdown1 = {
        listItems : model1
    };
    
    FilteringDropdown.loadFixedItems('groupByGeo', null, dropdown1);
    FilteringDropdown.loadItems('groupByProcCenter', 'groupByProcCenter_spinner', 'proc_center');
    FormManager.addValidator('dateFrom', Validators.REQUIRED, [ 'Date (from)' ]);
    FormManager.addValidator('dateTo', Validators.REQUIRED, [ 'Date (to)' ]);
    FormManager.addValidator('dateFrom', Validators.DATE('YYYY-MM-DD'), [ 'Date (from)' ]);
    FormManager.addValidator('dateTo', Validators.DATE('YYYY-MM-DD'), [ 'Date (to)' ]);
    
    FormManager.ready();
  });
</script>
<style>
div#filters {
  position: absolute;
  right: 10px;
  top: 450px;
  width: 150px;
  border: 1px Solid Gray;
  padding:10px;
  border-radius:5px;
  background: #EEEEEE;
  min-height:100px;
}

div#filterlabels {
  width: 140px;
  max-height: 300px;
  overflow-y: auto;
}
div#filterlabels table {
  background:white;
  border: 1px Solid Gray;
}
</style>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:section>
    <form:form method="GET" action="${contextPath}/metrics/statexport" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form"
      modelAttribute="metrics">
      <cmr:row>
        <h3>Request Statistics</h3>
      </cmr:row>
      <cmr:row>
        <cmr:column span="6" >
        <cmr:note text="Generates statistics per request including Request Number, CMR No., Status, Turn-around Times, and responsible person for each major process."></cmr:note>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="dateFrom">Date (from):</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <cmr:date path="dateFrom" id="dateFrom" />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="dateTo">Date (to):</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <cmr:date path="dateTo" id="dateTo" />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="reportType">GEO:</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="235">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="groupByGeo" searchAttr="name" style="display: block; width:110px" maxHeight="200"
              required="false" path="groupByGeo" placeHolder="Filter by GEO">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row >
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="groupByProcCenter">
              Processing Center:
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="235">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="groupByProcCenter" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="groupByProcCenter" placeHolder="Filter by Processing Center">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
        <cmr:column span="6">
          <cmr:button label="Export Statistics to File" onClick="CmrMetrics.exportStats()" highlight="false" pad="true" />
          <cmr:button label="Export Squad Report to File" onClick="CmrMetrics.exportSquadStats()" highlight="false" pad="true" />
        </cmr:column>
      </cmr:row>
    </form:form>
    <cmr:row topPad="10">
      <div style="width: 100%">
        <canvas id="canvas" style="height:20px"></canvas>
      </div>
    </cmr:row>
    <iframe id="exportFrame" name="exportFrame" style="display:none">
  </cmr:section>

</cmr:boxContent>

