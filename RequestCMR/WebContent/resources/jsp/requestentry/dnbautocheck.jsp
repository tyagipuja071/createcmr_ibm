<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<style>
#DnbAutoCheckModal div.ibm-columns {
  width: 730px !important;
}
</style>
<!-- Modal for the dnbMatches display -->
<cmr:modal title="D&B Matches Found" id="DnbAutoCheckModal"
widthId="750">
<form:form method="GET" action="${contextPath}/request/addrstd"
name="frmCMR" class="ibm-column-form ibm-styled-form"
modelAttribute="autoDnbModel">

<div style="font-size: 13px; font-weight:bold; display:inline-flex">
  <div style="width:25px; margin-left: 10px">
    <img src="${resourcesPath}/images/warn-icon.png" class="cmr-warn-icon">  
  </div>
  <div>
    There are matches against D&B for the record you are trying to create. 
    Please choose one from the matches or override the matches by clicking the override button below.
  </div>
</div>
<br>

<cmr:grid url="/request/dnb/matchlist.json"
        id="dnbMachesGrid" width="720" height="500" innerWidth="700" usePaging="false" >
        <cmr:gridCol width="50px" field="itemNo" header="Item No." />
        <cmr:gridCol width="80px" field="autoDnbDunsNo" header="DUNS Number" />
        <cmr:gridCol width="120px" field="autoDnbName" header="Company Name" />
        <cmr:gridCol width="auto" field="fullAddress" header="Full Address">
          <cmr:formatter functionName="matchDetailsFormatterUI" />
        </cmr:gridCol>
        <cmr:gridCol width="110px" field="ibmIsic" header="IBM ISIC">
          <cmr:formatter functionName="matchDetailsFormatterUI" />
        </cmr:gridCol>
        <cmr:gridCol width="80px" field="autoDnbMatchGrade" header="Confidence">
          <cmr:formatter functionName="autoDnbImportConfidenceFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="80px" field="autoDnbImportedIndc" header="Actions">
          <cmr:formatter functionName="autoDnbImportActFormatter" />
        </cmr:gridCol>
      </cmr:grid>

  <cmr:row topPad="5">
    <cmr:column span="2" width="400">
      <input id="overrideDnBMatchButton" type="button" class="cmr-grid-btn" style="font-size:13px;" value="Override D&B Matches" onClick="overrideDnBMatch()">
    </cmr:column>
  </cmr:row>
</form:form>
</cmr:modal> 