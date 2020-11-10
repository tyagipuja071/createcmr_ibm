<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<script>
  function showDnBMatchModal() {
    cmr.showProgress("Loading D&B Matches... ");
    var req_id = FormManager.getActualValue('reqId');
    if (req_id > 0) {
      CmrGrid.refresh('dnbMachesGrid', cmr.CONTEXT_ROOT + '/request/dnb/matchlist.json', 'reqId=' + req_id);
    }
    setTimeout(function() {
      cmr.hideProgress();
      cmr.showModal('DnbAutoCheckModal');
    }, 3500);

  }
</script>

<!-- Modal for the dnbMatches display -->
<cmr:modal title="D&B Matches Found" id="DnbAutoCheckModal"
	widthId="750">
	<div style="font-size: 13px; font-weight: bold; display: inline;">
		<img src="${resourcesPath}/images/warn-icon.png" class="cmr-warn-icon">
		There are matches against D&B for the record you are trying to create.
		Please choose one from the matches or override the matches by clicking
		the override button below.
	</div>
	<br>
	<div style="margin-left: -20px">
		<cmr:grid url="/request/dnb/matchlist.json" id="dnbMachesGrid"
			width="700" innerWidth="700" usePaging="false">
			<cmr:gridCol width="50px" field="itemNo" header="Item No." />
			<cmr:gridCol width="80px" field="autoDnbDunsNo" header="DUNS Number" />
			<cmr:gridCol width="120px" field="autoDnbName" header="Company Name" />
			<cmr:gridCol width="auto" field="fullAddress" header="Full Address">
				<cmr:formatter functionName="matchDetailsFormatterUI" />
			</cmr:gridCol>
			<cmr:gridCol width="110px" field="ibmIsic" header="IBM ISIC">
				<cmr:formatter functionName="matchDetailsFormatterUI" />
			</cmr:gridCol>
			<cmr:gridCol width="80px" field="autoDnbMatchGrade"
				header="Confidence">
				<cmr:formatter functionName="autoDnbImportConfidenceFormatter" />
			</cmr:gridCol>
			<cmr:gridCol width="80px" field="autoDnbImportedIndc"
				header="Actions">
				<cmr:formatter functionName="autoDnbImportActFormatter" />
			</cmr:gridCol>
		</cmr:grid>
	</div>
	<cmr:row topPad="5">
		<input id="overrideDnBMatchButton" type="button" class="cmr-grid-btn"
			style="font-size: 13px;" value="Override D&B Matches"
			onClick="overrideDnBMatch()">
	</cmr:row>
</cmr:modal>
