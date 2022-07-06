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
    var model1 = { 
        identifier : "id", 
        label : "name",
        items : []
    };
    model1.items.push({id : '', name : ''});
    //AMERICAS,APAC,JP,EMEA
    model1.items.push({id : 'GEO-AMERICAS', name : 'AMERICAS - All Markets'});
    model1.items.push({id : 'GEO-APAC', name : 'APAC - All Markets'});
    model1.items.push({id : 'GEO-EMEA', name : 'EMEA - All Markets'});
    model1.items.push({id : 'GEO-JP', name : 'JP GEO/Market'});

    // CND,CA,LA,US
    model1.items.push({id : 'MKT-CND', name : 'AMERICAS - CND'});
    model1.items.push({id : 'MKT-CA', name : 'AMERICAS - CA'});
    model1.items.push({id : 'MKT-LA', name : 'AMERICAS - LA'});
    model1.items.push({id : 'MKT-US', name : 'AMERICAS - US'});

    //ANZ,ASEAN,GCG,ISA
    model1.items.push({id : 'MKT-ASEANZK', name : 'APAC - ASEANZK'});
    //model1.items.push({id : 'MKT-ASEAN', name : 'APAC - ASEAN'});
    model1.items.push({id : 'MKT-GCG', name : 'APAC - GCG'});
    model1.items.push({id : 'MKT-ISA', name : 'APAC - ISA'});
    //model1.items.push({id : 'MKT-KR', name : 'APAC - KR'});

    // BENELUX,CEE,DACH,FRANCE,ITALY,MEA,NORDICS,SPGI,UKI
    model1.items.push({id : 'MKT-BENELUX', name : 'EMEA - BENELUX'});
    model1.items.push({id : 'MKT-CEE', name : 'EMEA - CEE'});
    model1.items.push({id : 'MKT-DACH', name : 'EMEA - DACH'});
    model1.items.push({id : 'MKT-FRANCE', name : 'EMEA - FRANCE'});
    model1.items.push({id : 'MKT-ITALY', name : 'EMEA - ITALY'});
    model1.items.push({id : 'MKT-MEA', name : 'EMEA - MEA'});
    model1.items.push({id : 'MKT-NORDICS', name : 'EMEA - NORDICS'});
    model1.items.push({id : 'MKT-SPGI', name : 'EMEA - SPGI'});
    model1.items.push({id : 'MKT-UKI', name : 'EMEA - UKI'});

    var dropdown1 = {
        listItems : model1
    };
    
    FilteringDropdown.loadFixedItems('groupByGeo', null, dropdown1);
    FilteringDropdown.loadItems('groupByProcCenter', 'groupByProcCenter_spinner', 'proc_center');
    FormManager.addValidator('dateFrom', Validators.REQUIRED, [ 'Date (from)' ]);
    FormManager.addValidator('dateTo', Validators.REQUIRED, [ 'Date (to)' ]);
    FormManager.addValidator('dateFrom', Validators.DATE('YYYY-MM-DD'), [ 'Date (from)' ]);
    FormManager.addValidator('dateTo', Validators.DATE('YYYY-MM-DD'), [ 'Date (to)' ]);
    FormManager.addFormValidator((function() {
      return {
        validate : function() {
          var from = moment(FormManager.getActualValue('dateFrom'), "YYYY-MM-DD");
          var to = moment(FormManager.getActualValue('dateTo'), "YYYY-MM-DD");
          
          var duration = moment.duration(to.diff(from));
          if (duration.asDays() > 180){
            return new ValidationResult(null, false, 'Dates should not be more than 6 months.');
          } 
          return new ValidationResult(null, true);
        }
      };
    })(), null, 'frmCMR');
    
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
    <cmr:form method="GET" action="${contextPath}/metrics/statexport" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form"
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
            <cmr:label fieldId="reportType">GEO/Market:</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="235">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="groupByGeo" searchAttr="name" style="display: block;" maxHeight="200"
              required="false" path="groupByGeo" placeHolder="Filter by GEO/Market">
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
      <cmr:row >
        <cmr:column span="1" width="150">
          <p>
          </p>
        </cmr:column>
        <cmr:column span="2" width="300">
          <p>
            <form:checkbox path="excludeUnsubmitted" value="Y"/>
            <label for="OEMInd" class=" cmr-radio-check-label">
               <span id="cmr-fld-lbl-OEMInd">Exclude Non-submitted Requests</span>
               <cmr:info text="Excludes all requests in Drafts status which have not yet been submitted for processing once. Requests which were rejected and now in Draft status will be included." />
            </label>            
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
        <cmr:column span="6">
          <cmr:button label="Export Request Statistics" onClick="CmrMetrics.exportStats()" highlight="true" pad="true" />
          <cmr:button label="Export Squad Report" onClick="CmrMetrics.exportSquadStats()" highlight="false" pad="true" />
          <cmr:button label="Export Requester Statistics" onClick="CmrMetrics.exportRequesterStats()" highlight="false" pad="true" />
        </cmr:column>
      </cmr:row>
    </cmr:form>
    <cmr:row topPad="10">
      <div style="width: 100%">
        <canvas id="canvas" style="height:20px"></canvas>
      </div>
    </cmr:row>
    <iframe id="exportFrame" name="exportFrame" style="display:none"></iframe>
  </cmr:section>

</cmr:boxContent>

