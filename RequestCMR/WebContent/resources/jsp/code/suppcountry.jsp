 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script>
  dojo.addOnLoad(function() {
  FormManager.ready();
  });
    
  var SuppCountryService = (function() {
    return {
      suppFormatter : function(value, rowIndex) {
        var rowData = this.grid.getItem(rowIndex);
        var countryCode = rowData.cntryCd;
        
        return '<a href="javascript: SuppCountryService.open(\'' + countryCode + '\')">' + countryCode + '</a>';

      },
      autoProcFormatter : function(value, rowIndex) {
        var rowData = this.grid.getItem(rowIndex);
        var processingType = rowData.processingTyp;
        if (value == 'Y') {
          return '<b>Enabled ('+processingType+')</b>';
        } else {
          return 'Disabled';
        }
      },
      tradestyleFormatter : function(value, rowIndex) {
        var rowData = this.grid.getItem(rowIndex);
        var processingType = rowData.processingTyp;
        if (value == 'R') {
          return '<b>Reject</b>';
        } else if (value == 'O') {
          return '<b>Override</b>';
        } else {
          return 'Allowed';
        }
      },
      startQSFormatter : function(value, rowIndex) {
        var rowData = this.grid.getItem(rowIndex);
        var processingType = rowData.processingTyp;
        if (value == 'Y') {
          return '<b>Enabled</b>';
        } else {
          return 'Disabled';
        }
      },
      reqTypeFormatter : function(value, rowIndex) {
        var types = value.split("");
        var formattedTypes = '';
        for (var i = 0; i < types.length; i++) {
          if (types[i] == 'C') {
           formattedTypes += formattedTypes.length > 0 ? '<br>' : '';
           formattedTypes += '<b>C</b>-Create';
           if (value.includes('C0')){
             formattedTypes += ' (manual)';
           }
          } else if (types[i] == 'U') {
           formattedTypes += formattedTypes.length > 0 ? '<br>' : '';
           formattedTypes += '<b>U</b>-Update';
           if (value.includes('U0')){
             formattedTypes += ' (manual)';
           }
          } else if (types[i] == 'M') {
            formattedTypes += formattedTypes.length > 0 ? '<br>' : '';
           formattedTypes += '<b>M</b>-Mass Update';
           if (value.includes('M0')){
             formattedTypes += ' (manual)';
           }
          } else if (types[i] == 'N') {
            formattedTypes += formattedTypes.length > 0 ? '<br>' : '';
           formattedTypes += '<b>N</b>-Mass Create';
           if (value.includes('N0')){
             formattedTypes += ' (manual)';
           }
          } else if (types[i] == 'R') {
            formattedTypes += formattedTypes.length > 0 ? '<br>' : '';
           formattedTypes += '<b>R</b>-Reactivate';
           if (value.includes('R0')){
             formattedTypes += ' (manual)';
           }
          } else if (types[i] == 'D') {
            formattedTypes += formattedTypes.length > 0 ? '<br>' : '';
           formattedTypes += '<b>D</b>-Delete';
           if (value.includes('D0')){
             formattedTypes += ' (manual)';
           }
          } else if (types[i] == 'E') {
            formattedTypes += formattedTypes.length > 0 ? '<br>' : '';
           formattedTypes += '<b>E</b>-Update by Enterprise';
           if (value.includes('E0')){
             formattedTypes += ' (manual)';
           }
          } else if (types[i] == 'X') {
            formattedTypes += formattedTypes.length > 0 ? '<br>' : '';
           formattedTypes += '<b>X</b>-Single Reactivate';
           if (value.includes('X0')){
             formattedTypes += ' (manual)';
           }
          }
        }
        return formattedTypes;
      },
      open : function(cntryCd) {
        document.forms['frmCMRSuppCntry'].setAttribute('action', cmr.CONTEXT_ROOT + '/code/addsuppcountrypage/?cntryCd=' + cntryCd);
        document.forms['frmCMRSuppCntry'].submit();
      },
      addSuppCntry : function() {
        window.location = cmr.CONTEXT_ROOT + '/code/addsuppcountrypage';
      },
      autoEngineFormatter : function(value, rowIndex) { 
        var rowData = this.grid.getItem(rowIndex);
        var dnb = rowData.dnbPrimaryIndc[0] == 'Y';
        if (value == 'R'){
          return 'Requesters Only ' + (dnb ? '<div class="dnb">D&B</div>' : '');
        }
        if (value == 'P'){
          return 'Processors Only' + (dnb ? '<div class="dnb">D&B</div>' : '');
        }
        if (value == 'B'){
          return 'Fully Enabled' + (dnb ? '<div class="dnb">D&B</div>' : '');
        }
        return 'Disabled';
      },
      recoveryFormatter : function(value, rowIndex) {
        if (value == 'B'){
          return 'To Requesters'
        }        
        return 'To Processors';
      },
      byModelFormatter : function(value, rowIndex) {
        if (value == 'Y'){
          return '<span style="font-weight:bold; color:red">Disabled</span>';
        }        
        return 'Enabled';
      },
      hideLocalLangDataFormatter : function(value, rowIndex) {
        if (value == 'Y'){
          return '<span style="font-weight:bold; color:red">Hidden</span>';
        }        
        return 'Enabled';
      }
  };
})();
</script>
<style>
  div.dnb {
    display: inline;
    border: 1px Solid #AAA;
    background: rgb(82,220,35);
    padding: 2px;
    margin-left: 3px;
    font-size: 9px;
    border-radius : 3px;
    font-weight: bold;
  }
</style>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:form method="POST" action="${contextPath}/code/suppcountry" name="frmCMRSuppCntry" class="ibm-column-form ibm-styled-form" modelAttribute="suppcountrymodel">   
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Supported Countries</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/suppcountrylisting.json" id="suppcountrylistingId" span="6" useFilter="true">
            <cmr:gridCol width="5%" field="cntryCd" header="Country Code" >
             <cmr:formatter functionName="SuppCountryService.suppFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="10%" field="nm" header="Country Name" />
            <cmr:gridCol width="10%" field="autoProcEnabled" header="Automatic Processing" >
              <cmr:formatter functionName="SuppCountryService.autoProcFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="8%" field="hostSysTyp" header="Host System Type" />
            <cmr:gridCol width="auto" field="suppReqType" header="Supported Request Types" >
              <cmr:formatter functionName="SuppCountryService.reqTypeFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="5%" field="defaultLandedCntry" header="Default Landed Country" />
            <cmr:gridCol width="9%" field="autoEngineIndc" header="Automation Engine" >
              <cmr:formatter functionName="SuppCountryService.autoEngineFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="10%" field="recoveryDirection" header="Recovery Direction" >
              <cmr:formatter functionName="SuppCountryService.recoveryFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="8%" field="startQuickSearch" header="Start From Quick Search" >
              <cmr:formatter functionName="SuppCountryService.startQSFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="8%" field="tradestyleNmUsage" header="TradeStyle Name Usage" >
              <cmr:formatter functionName="SuppCountryService.tradestyleFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="8%" field="disableCreateByModel" header="Create by Model" >
              <cmr:formatter functionName="SuppCountryService.byModelFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="8%" field="hideLocalLangData" header="Local Language Data" >
              <cmr:formatter functionName="SuppCountryService.hideLocalLangDataFormatter" />
            </cmr:gridCol>
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
      </cmr:row>
    </cmr:section>
  </cmr:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Add Supported Country" onClick="SuppCountryService.addSuppCntry()" highlight="true" />
    <cmr:button label="Back to Code Maintenance" onClick="window.location = '${contextPath}/code'" pad="true" /> 
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="suppcountrymodel" />