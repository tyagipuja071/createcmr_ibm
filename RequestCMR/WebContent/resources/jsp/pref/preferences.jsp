<%@page import="com.ibm.cio.cmr.request.model.pref.UserPrefModel"%>
<%@page import="com.ibm.cio.cmr.request.model.pref.UserPrefCountryModel"%>
<%@page import="java.util.List"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
AppUser user = AppUser.getUser(request);
List<String> roles = (List<String>) request.getAttribute("roles");
UserPrefModel model = (UserPrefModel) request.getAttribute("pref");
if (model != null && model.getDefaultNoOfRecords() == 0){
  model.setDefaultNoOfRecords(-1);
}
if (model.getDftCounty() == null){
  model.setDftCounty("N");
}
if (model.getDftStateProv() == null){
  model.setDftStateProv("O");
}
%>
<script src="${resourcesPath}/js/pref/preferences.js?${cmrv}" type="text/javascript"></script>
<script>
var _profilecomplete = false;
<%if (AppUser.getUser(request).isPreferencesSet()) {%>
_profilecomplete = true;
<%}%>
  dojo.addOnLoad(function() {
    FilteringDropdown.loadItems('dftIssuingCntry', 'dftIssuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');
    FilteringDropdown.loadItems('dftLandedCntry', 'dftLandedCntry_spinner', 'bds', 'fieldId=LandedCountry');
    FilteringDropdown.loadItems('procCenterNm', 'procCenterNm_spinner', 'proc_center');
    FilteringDropdown.loadItems('defaultLineOfBusn', 'requestingLob_spinner', 'lov', 'fieldId=RequestingLOB');
    FilteringDropdown.loadItems('defaultRequestRsn', 'reqReason_spinner', 'lov', 'fieldId=RequestReason');
    FilteringDropdown.loadOnChange('defaultReqType', 'defaultReqType_spinner', 'NEWREQUEST.TYPES', 'cntry=_dftIssuingCntry', 'dftIssuingCntry');     
<%if (user.isProcessor()){%>
    FilteringDropdown.loadItems('issuingCntry', 'issuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');
<%}%>
    if (FormManager) {
      FormManager.addValidator('receiveMailInd', Validators.REQUIRED, [ '${ui.receiveMailInd}' ]);
      FormManager.addValidator('delegateNm', Validators.REQUIRED, [ '${ui.delegateNm}' ]);
      FormManager.addValidator('delegateNm', Validators.BLUEPAGES, [ '${ui.delegateNm}' ]);
      FormManager.ready();
    }
    dojo.cookie('lastTab', 'x');
  });
</script>


<cmr:boxContent>
  <cmr:tabs />

  <cmr:section>
    <form:form method="POST" action="${contextPath}/preferences" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="pref">
      <cmr:modelAction formName="frmCMR" />
      <form:hidden path="managerName" />
      <%
        if (!AppUser.getUser(request).isPreferencesSet()) {
      %>
      <cmr:row>
        <cmr:column span="6">
          <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
          <cmr:note text="${ui.info.profile}" />
        </cmr:column>
      </cmr:row>
      <br>
      <%
        }
      %>
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="mainInfo">
              Main Preferences
            </cmr:label>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <cmr:label fieldId="requesterId">${ui.requesterId}: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="5">
          <p>${pref.requesterId}</p>
          <form:hidden id="requesterId" path="requesterId" />
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <cmr:label fieldId="requesterNm">${ui.requesterNm}:</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="5">
          <p>${pref.requesterNm}</p>
          <form:hidden id="requesterNm" path="requesterNm" />
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <cmr:label fieldId="assignedRoles">${ui.assignedRoles}:</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="5">
          <div style="padding-top:5px;">
          <%for (String role : roles){ %>
            <%=role%><br>
          <%} %>
          </div>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
        <cmr:column span="2" >
          <span style="color:red;font-size:16px;font-weight:bold">*</span> 
        <span style="font-size:14px;font-style:italic;color:#333">
          ${ui.mandatoryLegend}
        </span>
          </cmr:column>
          </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="6">
          <p>
            <cmr:label fieldId="receiveMailInd">
              ${ui.receiveMailInd}
              <cmr:info text="${ui.info.receiveMailInd}" />
            </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="receiveMailInd" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="receiveMailInd" cssStyle="width:60px">
              <form:option value="Y">Yes</form:option>
              <form:option value="N">No</form:option>
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
        <cmr:row addBackground="true">
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="dftIssuingCntry">
                ${ui.dftIssuingCntry}:
                <cmr:spinner fieldId="dftIssuingCntry" />
                <cmr:info text="${ui.info.dftIssuingCntry}" />
              </cmr:label>
  
              <form:select dojoType="dijit.form.FilteringSelect" id="dftIssuingCntry" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="dftIssuingCntry" placeHolder="${ui.dftIssuingCntryPH}">
              </form:select>
            </p>
          </cmr:column>
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="defaultReqType">
                ${ui.defaultRequestType}:
                <cmr:spinner fieldId="defaultReqType" />
                <cmr:info text="${ui.info.defaultRequestType}" />
              </cmr:label>
  
              <form:select dojoType="dijit.form.FilteringSelect" id="defaultReqType" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="defaultReqType" placeHolder="Select Request Type">
              </form:select>
            </p>
          </cmr:column>
        </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="`">
              ${ui.defaultLOB}
              <cmr:info text="${ui.info.requestingLOB}" />
            </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="defaultLineOfBusn" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="defaultLineOfBusn" cssStyle="width:250px" placeHolder="${pref.defaultLineOfBusn}">
            </form:select>
          </p>
        </cmr:column>
        <cmr:column span="3">
          <p>
            <cmr:label fieldId="reqReason">
              ${ui.defaultReqReason}
              <cmr:info text="${ui.info.defaultReqReason}" />
            </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="defaultRequestRsn" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="defaultRequestRsn" cssStyle="width:400px" placeHolder="${pref.defaultRequestRsn}">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="6">
          <p>
            <cmr:label fieldId="dftStateProv">
                Order of requests on the Workflow lists:
            </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="dftStateProv" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="dftStateProv" cssStyle="width:100px">
              <form:option value="O">Oldest first</form:option>
              <form:option value="Y">Latest first</form:option>
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="6">
          <p>
            <cmr:label fieldId="defaultNoOfRecords">
              ${ui.defaultRecords}
              <cmr:info text="${ui.info.defaultRecords}" />
            </cmr:label>
            
            <form:radiobutton path="defaultNoOfRecords" value="10"/>
            <cmr:label fieldId="defaultNoOfRecords_10" forRadioOrCheckbox="true">10</cmr:label>

            <form:radiobutton path="defaultNoOfRecords" value="20"/>
            <cmr:label fieldId="defaultNoOfRecords_20" forRadioOrCheckbox="true">20</cmr:label>

            <form:radiobutton path="defaultNoOfRecords" value="50"/>
            <cmr:label fieldId="defaultNoOfRecords_50" forRadioOrCheckbox="true">50</cmr:label>

            <form:radiobutton path="defaultNoOfRecords" value="100"/>
            <cmr:label fieldId="defaultNoOfRecords_100" forRadioOrCheckbox="true">100</cmr:label>

            <form:radiobutton path="defaultNoOfRecords" value="-1"/>
            <cmr:label fieldId="defaultNoOfRecords_ALL" forRadioOrCheckbox="true">Show All</cmr:label>


          </p>
        </cmr:column>
      </cmr:row>
      <%if (user.isProcessor()){%>
      <cmr:hr></cmr:hr>
      <cmr:row addBackground="false">
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="delegateList">
             Processor Preferences
            </cmr:label>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="false">
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="procCenterNm">
							${ui.procCenterNm}:
							<cmr:spinner fieldId="procCenterNm" />
              <cmr:info text="${ui.info.procCenterNm}" />
            </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="procCenterNm" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="procCenterNm" placeHolder="${ui.procCenterNmPH}">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="false">
        <cmr:column span="6">
          <p>
            <cmr:label fieldId="dftCounty">
              ${ui.showPendingOnly}
              <cmr:info text="${ui.info.showPendingOnly}:" />
            </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="dftCounty" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="dftCounty" cssStyle="width:60px">
              <form:option value="Y">Yes</form:option>
              <form:option value="N">No</form:option>
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <%}%>
      <cmr:hr></cmr:hr>
      <cmr:row addBackground="true">
        &nbsp;
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="2">
          <cmr:button label="${ui.btn.savePreferences}" onClick="FormManager.save('frmCMR')" highlight="true" />
          <cmr:button label="${ui.btn.cancel}" onClick="window.location = '${contextPath}/home'" pad="true" />
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        &nbsp;
      </cmr:row>
    </form:form>
  </cmr:section>
</cmr:boxContent>

<cmr:boxContent>
  <cmr:tabs />
  <cmr:section>
    <form:form method="POST" action="${contextPath}/preferences/delegate" name="frmCMRDel" class="ibm-column-form ibm-styled-form" modelAttribute="del" id="frmCMRDel">
      <form:hidden path="userId" />
      <cmr:modelAction formName="frmCMRDel" />
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="delegateList">
							${ui.delegateList}:<span class="ibm-required">*</span>
              <cmr:info text="${ui.info.delegateList}" />
            </cmr:label>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="3">
          <cmr:grid url="/search/delegate.json" id="DELEGATE_GRID" span="3" height="150" usePaging="false">
            <cmr:gridCol width="190px" field="delegateNm" header="${ui.grid.delegateNm}" />
            <cmr:gridCol width="150px" field="delegateId" header="${ui.grid.delegateId}" />
            <cmr:gridCol width="auto" field="action" header="${ui.grid.action}">
              <cmr:formatter functionName="removeFormatter" />
            </cmr:gridCol>
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="5">
        <cmr:column span="2">
          <cmr:button label="${ui.btn.addmanager}" onClick="doAddManager()" />
          <cmr:info text="${ui.info.addmanager}" />
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="15">
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="addNewDelegate">
							<strong>${ui.addNewDelegate}</strong>
              <cmr:info text="${ui.info.delegateNm}" />
						</cmr:label>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="3">
          <p>
            <cmr:label fieldId="delegateNm">
							${ui.delegateNm}:
            </cmr:label>
            <div class="cmr-inline">
              <cmr:bluepages model="pref" namePath="delegateNm" idPath="delegateId" />
              <span style="padding-left:5px">&nbsp;</span>
              <cmr:button label="${ui.btn.addToDelegateList}" onClick="doAddDelegate()" />
            </div>
          </p>
        </cmr:column>
      </cmr:row>
    </form:form>
  </cmr:section>
</cmr:boxContent>
    
    
    
<%if (user.isProcessor()){%>

<cmr:boxContent>
  <cmr:tabs />
  <cmr:section>
    <form:form method="POST" action="${contextPath}/preferences/country" name="frmCMRCntry" class="ibm-column-form ibm-styled-form" modelAttribute="cntry" id="frmCMRCntry">
      <form:hidden path="requesterId" />
      <form:hidden path="removeCntry" />
      <cmr:modelAction formName="frmCMRCntry" />
      <cmr:row addBackground="true">
        <cmr:column span="3">
          <p>
            <cmr:label fieldId="countryList">
              ${ui.prefCntryList}:
              <cmr:info text="${ui.info.prefCntryList}" />
            </cmr:label>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="3">
          <cmr:grid url="/search/userprefctry.json" id="PREF_CNTRY_GRID" span="3" height="300" usePaging="true" width="650" innerWidth="650">
            <cmr:gridCol width="350px" field="createBy" header="${ui.grid.prefCountry}" />
            <cmr:gridCol width="150px" field="createTsString" header="${ui.grid.addedOn}" />
            <cmr:gridCol width="auto" field="action" header="${ui.grid.action}">
              <cmr:formatter functionName="removeCntryFormatter" />
            </cmr:gridCol>
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="1">
          <p>
            <cmr:label fieldId="issuingCntry">CMR Issuing Country:</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="3">
          <p>
            <select name="issuingCntry" dojoType="dijit.form.FilteringSelect" id="issuingCntry" itemLabel="name" itemValue="id" style="display: inline-block;" maxHeight="200"
              required="true" path="issuingCntry" placeHolder="Select CMR Issuing Country" cssStyle="width:300px"></select>
            <cmr:button label="Add Country" onClick="doAddCntry()" />
          </p>
        </cmr:column>
      </cmr:row>
    </form:form>
  </cmr:section>
</cmr:boxContent>

<%}%>
<cmr:model model="pref" />
