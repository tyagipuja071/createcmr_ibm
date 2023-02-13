<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.SuppCountryModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="java.util.Date"%> 
<%@page import="java.text.SimpleDateFormat"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<style>
  span.reqtype {
    margin-left: 7px;
    font-size: 14px;
  }
  div.supported-type {
    padding-top: 10px;
  }
  div.cont {
    width:120px !important;
    display: inline-block;
  }
</style>
<%
SuppCountryModel suppModel = (SuppCountryModel) request.getAttribute("suppcountrymodel");
    boolean newEntry = false;
    if (suppModel.getState() == BaseModel.STATE_NEW) {
      newEntry = true;
      
    } 
    
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  var _handler = null;
  dojo.addOnLoad(function() {
<%if (newEntry) {%>
  FormManager.addValidator('cntryCd', Validators.REQUIRED, [ 'Country Code' ]);
<%}%>
    FormManager.addValidator('nm', Validators.REQUIRED, [ 'Country Name' ]);
  
    if ('<%=suppModel.getAutoProcEnabled()%>'== 'Y'){
      FormManager.enable('processingTyp');
      FormManager.addValidator('processingTyp', Validators.REQUIRED, [ 'Processing Type' ]);
    } else {
      FormManager.resetValidations('processingTyp');
      FormManager.setValue('processingTyp', '');
      FormManager.disable('processingTyp');
    }
    
    FilteringDropdown.loadItems('defaultLandedCntry', 'defaultLandedCntry_spinner', 'bds', 'fieldId=LandedCountry');
  
  
    _handler2 = dojo.connect(FormManager.getField('processingTyp'), 'onChange', function(value) {
        var val = FormManager.getActualValue('processingTyp');
        if (val == 'TC'){
          FormManager.enable('hostSysTyp');
          FormManager.addValidator('hostSysTyp', Validators.REQUIRED, [ 'Host System Type' ]);
        } else {
          FormManager.resetValidations('hostSysTyp');
          FormManager.readOnly('hostSysTyp');
        }
    });
    
    if (_handler2 && _handler2[0]){
      _handler2[0].onChange();
    }
    window.setTimeout('addCheckHandler()', 2000);
    
    FormManager.ready();

  });
  
  function addCheckHandler(){
    if (FormManager.getField('checkAutoProc')){
      console.log('adding handler..');
      _handler = dojo.connect(FormManager.getField('checkAutoProc'), 'onClick', function(evt) {
          var checked = evt.target.checked;
          if (checked){
            FormManager.enable('processingTyp');
            FormManager.addValidator('processingTyp', Validators.REQUIRED, [ 'Processing Type' ]);
          } else {
            FormManager.resetValidations('processingTyp');
            FormManager.setValue('processingTyp', '');
            FormManager.readOnly('processingTyp');
          }
      });
    }
  }
  
  

  var _TYPES = [ 'C', 'U', 'M', 'N', 'R', 'D', 'E', 'X' ];
  var SuppCountryService = (function() {
    return {
      saveSuppCntry : function(typeflag) {
        var cntryCd = FormManager.getActualValue('cntryCd');
        if (typeflag) {
          var check = cmr.query('SUPP_CNTRY', {
            CNTRY_CD : cntryCd
          });
          if (check && check.ret1 == '1') {
            cmr.showAlert('This Supported Country already exists in the system.');
            return;
          }
        }

        var types = '';
        for ( var i = 0; i < _TYPES.length; i++) {
          if (dojo.byId('check_' + _TYPES[i]).checked) {
            types += _TYPES[i];
            if (dojo.byId('check_' + _TYPES[i]+'0').checked){
              types += '0';
            }
          }
        }
        dojo.byId('suppReqType').value = types;
        FormManager.save('frmCMR');
      },
    };
  })();
</script>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:form method="POST" action="${contextPath}/code/addsuppcountrypage" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="suppcountrymodel">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Supported Country" : "Update Supported Country"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cntryCd">Country code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${suppcountrymodel.cntryCd}</p>
          <form:hidden id="cntryCd" path="cntryCd" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cntryCd">Country Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="cntryCd" dojoType="dijit.form.TextBox" maxlength="3"/>
          </p>
        </cmr:column>
       </cmr:row>
      <%
        }
      %>
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nm">Country Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p><form:input path="nm" dojoType="dijit.form.TextBox" maxlength="50" value="${suppcountrymodel.nm}"/></p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="suppReqType">Default Landed Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:select dojoType="dijit.form.FilteringSelect" id="defaultLandedCntry" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="defaultLandedCntry" placeHolder="Select Landed Country">
          </form:select>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cmt">Comment: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p><form:input path="cmt" dojoType="dijit.form.TextBox" maxlength="100" value="${suppcountrymodel.cmt}"/></p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="autoProcEnabled">Automatic Processing: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:checkbox id="checkAutoProc" path="autoProcCheckBox"/>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="processingTyp">Processing Type: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="processingTyp" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="processingTyp" placeHolder="Processing Type">
            <form:option value=""></form:option>
            <form:option value="TC">Transaction Connect</form:option>
            <form:option value="MQ">MQ Interface</form:option>
            <form:option value="DR">iERP</form:option>
            <form:option value="LD">Legacy Direct</form:option>
            <form:option value="MA">Austria Processing</form:option>
            <form:option value="MD">Swiss Processing</form:option>
            <form:option value="FR">France Processing</form:option>
            <form:option value="US">US Processing</form:option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="hostSysTyp">Host System Type: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p><form:input path="hostSysTyp" id="hostSysTyp" dojoType="dijit.form.TextBox" maxlength="15" value="${suppcountrymodel.hostSysTyp}"/></p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="suppReqType">Supported Request Types: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <div class="supported-type">
            <span class="reqtype">
              <div class="cont">
                <input type="checkbox" id="check_C" name="reqtype" value="C" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("C") ? "checked" : ""%>> 
                Create 
              </div>
              <input type="checkbox" id="check_C0" name="reqtype" value="0" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("C0") ? "checked" : ""%>> 
              <cmr:note text="manual only" />
              <br>
            </span>
            <span class="reqtype">
              <div class="cont">
                <input type="checkbox" id="check_U" name="reqtype" value="U" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("U") ? "checked" : ""%>> 
                Update 
              </div>
              <input type="checkbox" id="check_U0" name="reqtype" value="0" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("U0") ? "checked" : ""%>> 
              <cmr:note text="manual only" />
              <br>
            </span>
            <span class="reqtype">
              <div class="cont">
                <input type="checkbox" id="check_M" name="reqtype" value="M" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("M") ? "checked" : ""%>> 
                Mass Update 
              </div>
              <input type="checkbox" id="check_M0" name="reqtype" value="0" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("M0") ? "checked" : ""%>> 
              <cmr:note text="manual only" />
              <br>
            </span>
            <span class="reqtype">
              <div class="cont">
                <input type="checkbox" id="check_N" name="reqtype" value="N" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("N") ? "checked" : ""%>> 
                Mass Create 
              </div>
              <input type="checkbox" id="check_N0" name="reqtype" value="0" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("N0") ? "checked" : ""%>> 
              <cmr:note text="manual only" />
              <br>
            </span>
            <span class="reqtype">
              <div class="cont">
                <input type="checkbox" id="check_R" name="reqtype" value="R" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("R") ? "checked" : ""%>> 
                Reactivate 
              </div>
              <input type="checkbox" id="check_R0" name="reqtype" value="0" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("R0") ? "checked" : ""%>> 
              <cmr:note text="manual only" />
              <br>
            </span>
            <span class="reqtype">
              <div class="cont">
                <input type="checkbox" id="check_D" name="reqtype" value="D" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("D") ? "checked" : ""%>> 
                Delete 
              </div>
              <input type="checkbox" id="check_D0" name="reqtype" value="0" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("D0") ? "checked" : ""%>> 
              <cmr:note text="manual only" />
              <br>
            </span>
            <span class="reqtype" >
              <div class="cont">
                <input type="checkbox" id="check_E" name="reqtype" value="E" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("E") ? "checked" : ""%>> 
                Update by Enterprise 
              </div>
              <input type="checkbox" id="check_E0" name="reqtype" value="0" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("E0") ? "checked" : ""%>> 
              <cmr:note text="manual only" />
              <br>
            </span>
            <span class="reqtype" >
              <div class="cont">
                <input type="checkbox" id="check_X" name="reqtype" value="X" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("X") ? "checked" : ""%>> 
                Single Reactivate 
              </div>
              <input type="checkbox" id="check_X0" name="reqtype" value="0" <%=suppModel.getSuppReqType()!=null && suppModel.getSuppReqType().contains("X0") ? "checked" : ""%>> 
              <cmr:note text="manual only" />
              <br>
            </span>
          </div>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="disableCreateByModel">Create by Model: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="disableCreateByModel" searchAttr="name" style="display: block; width:550px" maxHeight="200"
            required="false" path="disableCreateByModel" placeHolder="Create by Model Setting for CMR results">
            <form:option value="N">Allow Create by Model from CMR results</form:option>
            <form:option value="Y">DISABLE Create by Model from CMR results</form:option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      
       <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="hideLocalLangData">Local Language Data: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="hideLocalLangData" searchAttr="name" style="display: block; width:550px" maxHeight="200"
            required="false" path="hideLocalLangData" placeHolder="Local Language Setting for Prospect CMR results">
            <form:option value="N">Show  Local Language Data for Prospect CMR</form:option>
            <form:option value="Y">Hide Local Language Data for Prospect CMR</form:option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row addBackground="true">
        <cmr:column span="1" width="180">
          <h3>Automation Engine</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="1" width="220">
          <p>
            <cmr:label fieldId="autoEngineIndc">Automation Engine Enabled: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="autoEngineIndc" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="autoEngineIndc" placeHolder="Automation Engine">
            <form:option value="">Disabled</form:option>
            <form:option value="R">On Requester Side</form:option>
            <form:option value="P">On Processor Side</form:option>
            <form:option value="B">Both Requester and Processor</form:option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="1" width="220">
          <p>
            <cmr:label fieldId="recoveryDirection">Recovery Direction: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="recoveryDirection" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="recoveryDirection" placeHolder="Recovery Direction">
            <form:option value="">Default (To Processors)</form:option>
            <form:option value="F">To Processors</form:option>
            <form:option value="B">To Requesters</form:option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row addBackground="true">
        <cmr:column span="1" width="220">
          <p>
            <cmr:label fieldId="dnbPrimaryIndc">Company Information: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="dnbPrimaryIndc" searchAttr="name" style="display: block; width:550px" maxHeight="200"
            required="false" path="dnbPrimaryIndc" placeHolder="Company Information">
            <form:option value="">Use external sources (non D&B) to check and validate company information</form:option>
            <form:option value="Y">Use D&B as primary source of company information and verification</form:option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row addBackground="true">
        <cmr:column span="1" width="220">
          <p>
            <cmr:label fieldId="startQuickSearch">Start Requests from Quick Search: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="startQuickSearch" searchAttr="name" style="display: block; width:550px" maxHeight="200"
            required="false" path="startQuickSearch" placeHolder="Start Requests from Quick Search">
            <form:option value="">No, Start requests from Create CMR Screen</form:option>
            <form:option value="Y">Yes, Use Quick Search for starting new requests</form:option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row addBackground="true">
        <cmr:column span="1" width="220">
          <p>
            <cmr:label fieldId="tradestyleNmUsage">Trade Style Name Usage: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="tradestyleNmUsage" searchAttr="name" style="display: block; width:550px" maxHeight="200"
            required="false" path="tradestyleNmUsage" placeHolder="Trade Style Name Usage">
            <form:option value="">Allow requests with Trade Style Name</form:option>
            <form:option value="R">Reject requests with Trade Style Name</form:option>
            <form:option value="O">Override Trade Style Name with Legal Name</form:option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>

      
      <form:hidden path="suppReqType" id="suppReqType"></form:hidden>
      <form:hidden path="createDt" id="createDt"></form:hidden>
    </cmr:section>
  </cmr:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="SuppCountryService.saveSuppCntry(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Update" onClick="SuppCountryService.saveSuppCntry(false)" highlight="true" />
    <%} %>
    <cmr:button label="Back to Supported Country List" onClick="window.location = '${contextPath}/code/suppcountry'" pad="true" /> 
  </cmr:buttonsRow>
  <br>
  
</cmr:section>
<cmr:model model="suppcountrymodel" />