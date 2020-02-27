<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/workflow/workflow.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FilteringDropdown.loadItems('wfProcCentre', 'wfProcCentre_spinner', 'proc_center');
    FilteringDropdown.loadItems('requestStatus', 'requestStatus_spinner', 'lov', 'fieldId=SearchRequestStatus');
    FilteringDropdown.loadItems('cmrIssuingCountry', 'cmrIssuingCountry_spinner', 'bds', 'fieldId=CMRIssuingCountry');
    FilteringDropdown.loadItems('cmrOwnerCriteria', 'cmrOwnerCriteria_spinner', 'lov', 'fieldId=CMROwner');
    FilteringDropdown.loadItems('requestType', 'requestType_spinner', 'lov', 'fieldId=SearchRequestType');
    FilteringDropdown.loadItems('procStatus', 'procStatus_spinner', 'lov', 'fieldId=ProcessingStatus');
    if (FormManager) {
      FormManager.addValidator('wfProcCentre', validateWorkflow, []);
      FormManager.addValidator('cmrNoCriteria', validateCMRNum, []);
      FormManager.addValidator('wfReqName', Validators.BLUEPAGES, [ '${ui.wfReqName}' ]);
      FormManager.addValidator('wfOrgName', Validators.BLUEPAGES, [ '${ui.wfOrgName}' ]);
      FormManager.addValidator('wfClaimByName', Validators.BLUEPAGES, [ '${ui.wfClaimByName}' ]);
      FormManager.addValidator('createDateFrom', Validators.DATE('YYYY-MM-DD'), ['${ui.createDate} ${ui.from}']);
      FormManager.addValidator('createDateTo', Validators.DATE('YYYY-MM-DD'), ['${ui.createDate} ${ui.to}']);
      FormManager.addValidator('lastActDateFrom', Validators.DATE('YYYY-MM-DD'), ['${ui.lastActionDate} ${ui.from}']);
      FormManager.addValidator('lastActDateTo', Validators.DATE('YYYY-MM-DD'), ['${ui.lastActionDate} ${ui.to}']);
      FormManager.ready();
    }
  });
</script>
<style>

</style>
<form:form method="POST" action="${contextPath}/workflow/results" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="requestSearchCriteriaModel">


  <cmr:boxContent>

    <cmr:tabs>
      <cmr:tab label="${ui.tab.criteria}" id="CRITERIA_TAB" sectionId="" onClick="doSearchRequests()" active="true" />
      <cmr:tab label="${ui.tab.result}" id="RESULT_TAB" sectionId="" onClick="doSearchRequests()" />
    </cmr:tabs>

    <cmr:section id="CRITERIASECTION">
      <br>
      <cmr:row>
        <cmr:column span="6">
          <span class="ibm-required">*</span>&nbsp;
					<cmr:note text="${ui.note.reqSrchMsg}" />
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="wfProcCentre">
							${ui.wfProcCentre}:
							<cmr:spinner fieldId="wfProcCentre" />

            </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="wfProcCentre" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="wfProcCentre" placeHolder="${ui.procCenterNmPH}">
            </form:select>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <label for="wfReqName">${ui.wfReqName}: </label>
            <cmr:bluepages model="requestSearchCriteriaModel" namePath="wfReqName" idPath="wfReqId" showId="true"/>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <label for="processedBy">${ui.processedBy}: 
            <cmr:info text="${ui.info.processedBy}"></cmr:info>
            </label>
            <cmr:bluepages model="requestSearchCriteriaModel" namePath="processedByName" idPath="processedBy" showId="true"/>
          </p>
        </cmr:column>
        </cmr:row>
        <cmr:row>
        <cmr:column span="2">
              <p>
              <cmr:label fieldId="cmrIssuingCountry">
                ${ui.cmrIssuingCntry}:
                <cmr:spinner fieldId="cmrIssuingCountry" />
              </cmr:label>
              <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCountry" searchAttr="name" style="display: block;" maxHeight="200" path="cmrIssuingCountry" placeHolder="">
              </form:select>
              </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <label for="wfOrgName">${ui.wfOrgName}: </label>
            <cmr:bluepages model="requestSearchCriteriaModel" namePath="wfOrgName" idPath="wfOrgId" showId="true"/>
          </p>
        </cmr:column>
        </cmr:row>
        <cmr:row>
        <cmr:column span="2">
              <p>
              <cmr:label fieldId="cmrOwnerCriteria">
                ${ui.cmrOwner}:
                <cmr:spinner fieldId="cmrOwnerCriteria" />
              </cmr:label>
              <form:select dojoType="dijit.form.FilteringSelect" id="cmrOwnerCriteria" searchAttr="name" style="display: block;" maxHeight="200" path="cmrOwnerCriteria" placeHolder="">
              </form:select>
              </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <label for="wfClaimByName">${ui.wfClaimByName}: </label>
            <cmr:bluepages model="requestSearchCriteriaModel" namePath="wfClaimByName" idPath="wfClaimById" showId="true"/>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="2">
          <p>
            <label for="cmrNoCriteria">${ui.cmrnum}: <cmr:info text="${ui.info.cmrnum}" /></label>
            <form:input size="30" id="cmrNoCriteria" path="cmrNoCriteria" />
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="requestType">
              ${ui.reqType}:
              <cmr:spinner fieldId="requestType" />
            </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="requestType" searchAttr="name" style="display: block;" maxHeight="200" path="requestType" placeHolder="">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
      <cmr:column span="2">
          <p>
            <cmr:label fieldId="requestStatus">
              ${ui.requestStatus}:
              <cmr:spinner fieldId="requestStatus" />

            </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="requestStatus" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="requestStatus">
            </form:select>
          </p>
        </cmr:column>
       
        <cmr:column span="2">
          <p>
            <label for="requestId">${ui.requestId}: </label>
            <form:input size="30" id="requestId" path="requestId" />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
         <cmr:column span="2">
          <p>
            <label for="customerName">${ui.customerName}:&nbsp;<cmr:info text="${ui.info.custname}" />
               <form:select path="searchCusType" id="searchCusType" cssClass="cmr-select">
                <form:option value="C">Contains</form:option>
                <form:option value="B">Begins With</form:option>
              </form:select> </label>
            <form:input size="30" id="customerName" path="customerName" />
          </p>
        </cmr:column>
        <cmr:column span="2">
          <br />
          <label for="expediteChk" style="display: inline"> ${ui.expediteChk }:&nbsp; </label>
          <form:checkbox id="expediteChk" path="expediteChk" value="Y" />
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="procStatus">
              ${ui.procStatus}:
              <cmr:spinner fieldId="procStatus" />
            </cmr:label>
            <form:select dojoType="dijit.form.FilteringSelect" id="procStatus" searchAttr="name" style="display: block;" maxHeight="200" path="procStatus" placeHolder="">
            </form:select>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <br />
          <label for="pendingAppr" style="display: inline"> ${ui.pendingApprCrit}:&nbsp; </label>
          <form:checkbox id="pendingAppr" path="pendingAppr" value="Y" />
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <label for="createDateFrom">${ui.createDate}:
            <span style="float:right">${ui.from}:</span>
            </label>
          </p>
        </cmr:column>
        <cmr:column span="1">
          <p>
            <cmr:date path="createDateFrom" />
          </p>
        </cmr:column>
        <cmr:column span="2" width="180">
          <p>
            <label for="lastActDateFrom">${ui.lastActionDate}:
            <span style="float:right">${ui.from}:</span>
             </label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <cmr:date path="lastActDateFrom" />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <p>
            <label for="createDateTo">
            <span style="float:right">${ui.to}:</span>
            </label>
          </p>
        </cmr:column>
        <cmr:column span="1">
          <p>
            <cmr:date path="createDateTo" />
          </p>
        </cmr:column>
        <cmr:column span="1" width="180">
          <p>
            <label for="lastActDateTo">
            <span style="float:right">${ui.to}:</span>
            </label>
          </p>
        </cmr:column>
        <cmr:column span="1">
          <p>
            <cmr:date path="lastActDateTo" />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="6">
          <p>
            <label for="resultRows">${ui.resultRows}: <span class="ibm-input-group ibm-radio-group"> <form:radiobutton id="cmr_row_50" path="resultRows" value="50" /> <label for="cmr_row_50">50 </label> <form:radiobutton
                  id="cmr_row_100" path="resultRows" value="100" /> <label for="cmr_row_100">100</label> <form:radiobutton id="cmr_row_500" path="resultRows" value="500" checked="checked" /> <label for="cmr_row_500">500</label> </span> </label>
          </p>
        </cmr:column>
      </cmr:row>
      <br>
      <cmr:buttonsRow>
        <cmr:button label="${ui.btn.search}" onClick="doSearchRequests()" highlight="true" />
        <cmr:button label="${ui.btn.clear}" onClick="window.location='${contextPath}/workflow/search?clear=Y'" pad="true" />
      </cmr:buttonsRow>
      <br>
    </cmr:section>

  </cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
      <cmr:button label="${ui.btn.createNewEntry}" onClick="cmr.chooseNewEntry()" highlight="true" pad="false"/>
      <cmr:button label="${ui.btn.openByRequestID}" onClick="openRequestById()" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<input type="hidden" id="fromURL" name="fromUrl" value="" />
<input type="hidden" id="newReqCntry_h" name="newReqCntry" value="" />
<input type="hidden" id="newReqType_h" name="newReqType" value="" />
</form:form>
<cmr:model model="requestSearchCriteriaModel" />