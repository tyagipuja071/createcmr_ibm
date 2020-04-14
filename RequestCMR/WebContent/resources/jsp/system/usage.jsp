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
<script src="${resourcesPath}/js/system/svc_usage.js?${cmrv}" type="text/javascript"></script>
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<%
  
%>
<script>
  var _typeHandler = null;
  dojo.addOnLoad(function() {
    var ids1 = cmr.query('METRICS.ALL_PARTNERS', {_qall  : 'Y'});
    var model1 = { 
        identifier : "id", 
        label : "name",
        items : []
    };
    model1.items.push({id : '', name : ''});
    for (var i =0; i < ids1.length; i++){
      model1.items.push({id : ids1[i].ret1, name : ids1[i].ret1});
    }
    var dropdown1 = {
        listItems : model1
    };
    
    if (_typeHandler == null) {
      _typeHandler = dojo.connect(FormManager.getField('reportType'), 'onChange', function(value) {
          var type = FormManager.getActualValue('reportType');
          FormManager.resetValidations('groupByGeo');
          FormManager.resetValidations('countType');
          if (type == 'P'){
            FormManager.addValidator('groupByGeo', Validators.REQUIRED, [ 'Partner' ]);
            FormManager.enable('groupByGeo'); 
            FormManager.setValue('countType', '');
            FormManager.readOnly('countType'); 
          } else if (type == 'N'){
            FormManager.addValidator('countType', Validators.REQUIRED, [ 'Service Name' ]);
            FormManager.enable('countType'); 
            FormManager.setValue('groupByGeo', '');
            FormManager.readOnly('groupByGeo'); 
          } else {
            FormManager.setValue('groupByGeo', '');
            FormManager.readOnly('groupByGeo'); 
            FormManager.setValue('countType', '');
            FormManager.readOnly('countType'); 
          }
      });
    }

    FilteringDropdown.loadFixedItems('groupByGeo', null, dropdown1);
    FormManager.addValidator('dateFrom', Validators.REQUIRED, [ 'Date (from)' ]);
    FormManager.addValidator('dateTo', Validators.REQUIRED, [ 'Date (to)' ]);
    FormManager.addValidator('dateFrom', Validators.DATE('YYYY-MM-DD'), [ 'Date (from)' ]);
    FormManager.addValidator('dateTo', Validators.DATE('YYYY-MM-DD'), [ 'Date (to)' ]);
    FormManager.addValidator('reportType', Validators.REQUIRED, [ 'Report Type' ]);
    
    FormManager.addFormValidator((function() {
      return {
        validate : function() {
          return new ValidationResult(null, true);
          return new ValidationResult(null, false, 'Mailing, Billing and Installing addresses are mandatory. Only multiple Shipping & Installing addresses are allowed.');
        }
      }
    })(), null, 'frmCMR');
          

    FormManager.readOnly('countType');
    FormManager.readOnly('groupByGeo');
    FormManager.ready();
  });
</script>
<style>
div#filters {
  font-size: 11px;
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
div#filters input {
  font-size: 11px !important;
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

div#filterlabels table td, div#filterlabels table th {
  font-size: 11px;
}
</style>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:section>
    <form:form method="GET" action="${contextPath}/metrics/usage_export" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form"
      modelAttribute="usage">
      <cmr:row>
        <h3>Web Service Usage</h3>
      </cmr:row>
      <cmr:row>
        <cmr:column span="6" >
        <cmr:note text="Period specified cannot be more than 30 days for online charts."></cmr:note>
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
            <cmr:label fieldId="reportType">Report Type:</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="235">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="reportType" searchAttr="name" style="display: block; " maxHeight="200"
              required="false" path="reportType" placeHolder="Select Report Type">
              <form:option value=""></form:option>
              <form:option value="S">Total Usage (All Partners)</form:option>
              <form:option value="A">Total Usage (All Services)</form:option>
              <form:option value="N">Usage by Service</form:option>
              <form:option value="P">Usage by Partner</form:option>
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="countType">Service:</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="countType" searchAttr="name" style="display: block; " maxHeight="200"
              required="false" path="countType" placeHolder="Select Service">
              <form:option value=""></form:option>
              <form:option value="Find CMR">Find CMR</form:option>
              <form:option value="Prospect CMR">Prospect CMR</form:option>
              <form:option value="CMR Lite">CMR Lite</form:option>
              <form:option value="Handshake">Handshake</form:option>
              <form:option value="CMR Request Service">CMR Request Service</form:option>
              <form:option value="Convert Prospect to Legal">Convert Prospect to Legal</form:option>
              <form:option value="Query Requests">Query Requests</form:option>
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="groupByGeo">Partner:</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="groupByGeo" searchAttr="name" style="display: block; " maxHeight="200"
              required="false" path="groupByGeo" placeHolder="Select Partner">
              <form:option value=""></form:option>
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
        <cmr:column span="2">
          <cmr:button label="Generate Report" onClick="SvcUsage.generateReport()" highlight="true" pad="true" />
          <cmr:button label="Export to File" onClick="SvcUsage.exportReport()" highlight="false" pad="true" />
        </cmr:column>
      </cmr:row>
    </form:form>
    <cmr:row topPad="10">
      <div style="width: 100%">
        <canvas id="canvas" style="height:20px"></canvas>
      </div>
    </cmr:row>
    <div id="filters" style="display: none">
      <h3>Filter</h3>
      <div id="filterlabels"></div>
      <br>
      <a style="font-size:12px" href="javascript: SvcUsage.selectFilters(true)">Select All</a><br>
      <a style="font-size:12px" href="javascript: SvcUsage.selectFilters(false)">Remove All</a><br>
      <cmr:button label="Update Chart" onClick="SvcUsage.updateChart()" highlight="true"></cmr:button>
    </div>
    <iframe id="exportFrame" name="exportFrame" style="display:none">
  </cmr:section>

</cmr:boxContent>

