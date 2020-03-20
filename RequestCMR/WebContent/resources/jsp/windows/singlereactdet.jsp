<%@page import="com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath()+"/window/singlereactprocess";
  FindCMRRecordModel rModel = (FindCMRRecordModel) request.getAttribute("record");
  String cmrNumber = rModel.getCmrNum();
  String cmrCntry = rModel.getCmrIssuedBy();

%>
<script>
function backToSearch(){
var reqId = FormManager.getActualValue('reqId');
  window.location = '<%=request.getContextPath()+"/window/singlereactsearch"%>?cntry='+<%=cmrCntry%>+'&reqId='+reqId;
}

function backToSingleReactList(){
  window.location = '<%=request.getContextPath()+"/window/singlereactlist"%>';
}

function chooseRecord() {
   var result = {
     accepted : 'y',
     type : 'X',
     data : {
       issuedBy : FormManager.getActualValue("cmrIssuedByHid"),
       cmrNum : FormManager.getActualValue("cmrNumHid")
     }
  };
  console.log(result);
  if (window.opener){
    window.opener.cmr.hideProgress();
    window.opener.doImportSREACTRecord(result);
    WindowMgr.closeMe();    
  }
}



function trackMe(){
  // noop
}
WindowMgr.trackMe = trackMe;  

</script>
<style>
div.ibm-columns {
  width: 1100px !important;
}
</style>
<cmr:window>
  <form:form method="POST" action="<%=actionUrl%>" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="sreact">
    <jsp:include page="singlereactcriteria.jsp" />
  </form:form>
  <form:form method="POST" action="<%=actionUrl%>" name="frmCMRDet" class="ibm-column-form ibm-styled-form" modelAttribute="record">
  
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="cmrNum">CMR#:</label>
      </cmr:column>
      <cmr:column span="3">
        ${record.cmrNum}
        <input type="hidden" id="cmrNumHid" value="${record.cmrNum}"/>
        <input type="hidden" id="cmrIssuedByHid" value="${record.cmrIssuedBy}"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="cmrShortName">Abbreviated Name:</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.cmrShortName}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="cmrName1Plain">Customer Name 1:</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.cmrName1Plain}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="cmrName2Plain">Customer Name 2:</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.cmrName2Plain}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="cmrState">Address:</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.cmrState}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="cmrPostalCode">Postal Code:</label>
      </cmr:column>
      <cmr:column span="2" width="200">
        ${record.cmrPostalCode}
      </cmr:column>
      <cmr:column span="1" width="180">
        <label for="cmrCity">City:</label>
      </cmr:column>
      <cmr:column span="2" width="200">
        ${record.cmrCity}
      </cmr:column>
    </cmr:row>
    <cmr:row>     
      <cmr:column span="1" width="110">
        <label for="cmrVat">VAT:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.cmrVat}
      </cmr:column>   
      <cmr:column span="1" width="180">
        <label for="cmrCity">ISIC(UN SIC) Code:</label>
      </cmr:column>
      <cmr:column span="2" width="200">
        ${record.cmrCity}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="110">
        <label for="cmrLocalTax2">Business Reg#/Local VAT:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.cmrLocalTax2}
      </cmr:column>
      <cmr:column span="1" width="180">
        <label for="cmrSubIndustry">Subindustry/IMS:</label>
      </cmr:column>
      <cmr:column span="2" width="200">
        ${record.cmrSubIndustry}
      </cmr:column>
    </cmr:row>
  </form:form>
  <cmr:windowClose>
    <cmr:button label="Import Customer" onClick="chooseRecord()" highlight="true" pad="true"/>
    <cmr:button label="Search Again" onClick="backToSearch()" highlight="true" pad="true" />
    <cmr:button label="Back to Single Reactivation List" onClick="backToSingleReactList()" highlight="false" pad="true" />
  </cmr:windowClose>
</cmr:window>
