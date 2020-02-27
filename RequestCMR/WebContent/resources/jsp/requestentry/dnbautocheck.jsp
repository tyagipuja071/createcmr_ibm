<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<!-- Modal for the dnbMatches display -->
<cmr:modal title="${ui.title.dnbAutoCheck}" id="DnbAutoCheckModal"
widthId="570">
<form:form method="GET" action="${contextPath}/request/addrstd"
name="frmCMR" class="ibm-column-form ibm-styled-form"
modelAttribute="autoDnbModel">

There are matches against D&B for the record you are trying to create. Please choose one from the matches or override the matches by clicking the Override button.
<br>

<cmr:grid url="/request/dnb/matchlist.json"
        id="dnbMachesGrid" width="540" height="700"
        usePaging="false" >
        <cmr:gridCol width="30px" field="itemNo" header="Item No." />
        <cmr:gridCol width="60px" field="autoDnbDunsNo" header="DUNS Number" />
        <cmr:gridCol width="70px" field="autoDnbName" header="Company Name" />
        <cmr:gridCol width="100px" field="fullAddress" header="Full Address">
          <cmr:formatter functionName="matchDetailsFormatterUI" />
        </cmr:gridCol>
        <cmr:gridCol width="75px" field="ibmIsic" header="IBM ISIC">
          <cmr:formatter functionName="matchDetailsFormatterUI" />
        </cmr:gridCol>
        <cmr:gridCol width="60px" field="autoDnbMatchGrade" header="Match Grade" />
        <cmr:gridCol width="auto" field="autoDnbImportedIndc" header="Actions">
          <cmr:formatter functionName="autoDnbImportActFormatter" />
        </cmr:gridCol>
      </cmr:grid>

<cmr:row topPad="5">
<div class="ibm-col-1-1 cmr-left">
<cmr:button id="overrideDnBMatchButton" label="${ui.btn.overrideDnBMatch}" onClick="overrideDnBMatch()" styleClass="cmr-middle"/>
</div>
<br><br>
</cmr:row>
</form:form>
</cmr:modal> 