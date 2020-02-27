<%@page import="com.ibm.cmr.services.client.cris.CRISCompany"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath()+"/window/crisprocess";
  CRISCompany company = (CRISCompany) request.getAttribute("record");
  String companyNo = company.getCompanyNo();
%>
<script>
function backToSearch(){
  window.location = '<%=request.getContextPath()+"/window/crissearch"%>';
}

function backToCompanyList(){
  window.location = '<%=request.getContextPath()+"/window/criscomplist"%>';
}

function showEstablishments(){
  
  window.location = '<%=request.getContextPath()+"/window/crisestablist?access=CD&companyNo="+companyNo%>';
}

function chooseRecord(){
  var result = {
     accepted : 'y',
     type : 'C',
     data : {
       issuedBy : '760',
       issuedByDesc : 'Japan',
       cmrNum : '<%=company.getCompanyNo()%>'
     }
  };
  console.log(result);
  if (window.opener){
    window.opener.cmr.hideProgress();
    window.opener.doImportCRISRecord(result);
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
  <form:form method="POST" action="<%=actionUrl%>" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="crit">
    <jsp:include page="criscriteria.jsp" />
  </form:form>
  <form:form method="POST" action="<%=actionUrl%>" name="frmCMRDet" class="ibm-column-form ibm-styled-form" modelAttribute="record">
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="companyNo">Company No:</label>
      </cmr:column>
      <cmr:column span="3">
        ${record.companyNo}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="nameAbbr">Abbreviated Name:</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.nameAbbr}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="nameKanji">Name (Kanji):</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.nameKanji}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="nameKana">Name (Katakana):</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.nameKana}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="address">Address:</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.address}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="postCode">Postal Code:</label>
      </cmr:column>
      <cmr:column span="2" width="200">
        ${record.postCode}
      </cmr:column>
      <cmr:column span="1" width="180">
        <label for="phoneShi">Phone:</label>
      </cmr:column>
      <cmr:column span="2" width="200">
        ${record.phoneShi} - ${record.phoneKyo} - ${record.phoneBango}
      </cmr:column>
    </cmr:row>
    <cmr:row>     
      <cmr:column span="1" width="110">
        <label for="jsic">JSIC:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.jsic}
      </cmr:column>   
      <cmr:column span="1" width="180">
        <label for="locCode">Location Code:</label>
      </cmr:column>
      <cmr:column span="2" width="200">
        ${record.locCode}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="110">
        <label for="dunsNo">DUNS:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.dunsNo}
      </cmr:column>
      <cmr:column span="1" width="180">
        <label for="employeeSize">Employee Size:</label>
      </cmr:column>
      <cmr:column span="2" width="200">
        ${record.employeeSize}
      </cmr:column>
    </cmr:row>
  </form:form>
  <cmr:windowClose>
    <cmr:button label="Import Company" onClick="chooseRecord()" highlight="true" pad="true"/>
    <cmr:button label="Search Again" onClick="backToSearch()" highlight="true" pad="true" />
    <cmr:button label="Back to Company List" onClick="backToCompanyList()" highlight="false" pad="true" />
    <cmr:button label="Show Establishments" onClick="showEstablishments()" highlight="false" pad="true" />
  </cmr:windowClose>
</cmr:window>
