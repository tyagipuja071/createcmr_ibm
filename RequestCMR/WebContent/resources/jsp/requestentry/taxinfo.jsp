<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
if (readOnly == null){
  readOnly = false;
}
%>
<style>
.cmr-middle {
  text-align:center;
}
</style>
<cmr:section id="TAXINFO_REQ_TAB" hidden="true">
  <jsp:include page="detailstrip.jsp" />


  <cmr:row addBackground="true" topPad="10">
    <cmr:column span="1" width="180">
      <p>
        <cmr:label fieldId="taxinfoList">
              ${ui.taxinfoList}:
        </cmr:label>
      </p>
    </cmr:column>
  </cmr:row>

  <%
  String contextPath = request.getContextPath();
  String actions = UIMgr.getText("grid.actions");
  String infoTxt = UIMgr.getText("info.addTaxInfo");
  String actionsHeader = actions+"<img src=\""+contextPath+"/resources/images/info-bubble-icon.png\" title=\""+infoTxt+"\" class=\"cmr-info-bubble\">";
  %> 
  <cmr:row addBackground="true" topPad="10">
    <cmr:column span="6">
      <cmr:grid url="/request/taxinfo/list.json" id="TAXINFO_GRID" span="6" height="250" usePaging="false">
        <cmr:gridParam fieldId="reqId" value="${reqentry.reqId}" />
        <cmr:gridCol width="120px" field="taxCd" header="${ui.grid.laTaxCd}" >
        <cmr:formatter functionName="taxCodeFormatter" />
         </cmr:gridCol>
         <% if(null!= reqentry.getCmrIssuingCntry() && reqentry.getCmrIssuingCntry().equals("613")) {%> 
        <cmr:gridCol width="90px" field="taxNum" header="${ui.grid.taxNumber}" />
        <%} %>
        <cmr:gridCol width="120px" field="taxSeparationIndc" header="${ui.grid.taxSepInd}"/>
        <cmr:gridCol width="120px" field="billingPrintIndc" header="${ui.grid.billingPrintInd}" />
        <cmr:gridCol width="120px" field="contractPrintIndc" header="${ui.grid.contractPrintInd}" />
        <cmr:gridCol width="120px" field="cntryUse" header="${ui.grid.cntryUse}" />
        <cmr:gridCol width="190px" field="action" header="<%=actionsHeader%>">
<%if (!readOnly) {%>
          <cmr:formatter functionName="taxinfoFormatter" />
<%}%>
        </cmr:gridCol>
      </cmr:grid>
    </cmr:column>
  </cmr:row>

  <cmr:row topPad="5" addBackground="true">
    <div class="ibm-col-1-1 cmr-middle">
<%if (!readOnly) {%>
      <cmr:button label="${ui.btn.addTaxInfoButton}" onClick="doAddTaxInfo()" styleClass="cmr-middle" id="addTaxInfoButton"/>
<%} %>
    </div>
    <br><br>
  </cmr:row>
</cmr:section>