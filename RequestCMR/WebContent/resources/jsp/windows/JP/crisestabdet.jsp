<%@page import="com.ibm.cmr.services.client.cris.CRISEstablishment"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath()+"/window/crisprocess";
  String companyNo = (String) request.getParameter("companyNo");
  if (companyNo == null){
    companyNo = "";
  }
  String access = (String) request.getParameter("access");
  if (access == null){
    access = "";
  }
  CRISEstablishment estab = (CRISEstablishment) request.getAttribute("record");
  String establishmentNo = estab.getEstablishmentNo();
  boolean fromAccount = "Y".equals(request.getParameter("fromAccount"));
  boolean fromEstab = "Y".equals(request.getParameter("fromEstab"));
%>
<script>
function backToSearch(){
  window.location = '<%=request.getContextPath()+"/window/crissearch"%>';
}

function backToEstablishmentList(){
  window.location = '<%=request.getContextPath()+"/window/crisestablist?companyNo="+companyNo+"&access="+access%>';
}


function showAccounts(){
  
  window.location = '<%=request.getContextPath()+"/window/crisaccountlist?access="+access+"&establishmentNo="+establishmentNo+"&companyNo="+companyNo%>';
}

function chooseRecord(){
  var result = {
     nodata : true,
     accepted : 'y',
     type : 'E',
     data : {
       issuedBy : '760',
       issuedByDesc : 'Japan',
       cmrNum : '<%=estab.getEstablishmentNo()%>'
     }
  };
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
        <label for="estalishmentNo">Establishment No:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.establishmentNo}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="companyNo">Company No:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.companyNo}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="nameAbbr">Abbreviated Name:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.nameAbbr}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="companyNameKanji">Company Name:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${company.nameKanji}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="nameKanji">Name (Kanji):</label>
      </cmr:column>
      <cmr:column span="2" width="">
        ${record.nameKanji}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="companyAddr">Company Address:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${company.address}
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
      <cmr:column span="2" width="300">
        ${record.postCode}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="companyCd">Company Code:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.companyCd}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="phoneShi">Phone:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.phoneShi} -  ${record.phoneKyo} - ${record.phoneBango}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="jsic">JSIC:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.jsic}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="locCode">Location Code:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.locCode}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="funcCode">Function Code:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.funcCode}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="bldg">Building:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.bldg}
      </cmr:column>
      <cmr:column span="1" width="150">
      </cmr:column>
      <cmr:column span="2">
      </cmr:column>
    </cmr:row>
  </form:form>
  <cmr:windowClose>
    <cmr:button label="Import Establishment" onClick="chooseRecord()" highlight="true" pad="true"/>
    <cmr:button label="Search Again" onClick="backToSearch()" highlight="true" pad="true"/>
<%if (!fromAccount && fromEstab && !StringUtils.isBlank(companyNo)){ %>
    <cmr:button label="Back to Establishment List" onClick="backToEstablishmentList()" highlight="false" pad="true" />
<%} %>
    <cmr:button label="Show Accounts" onClick="showAccounts()" highlight="false" pad="true" />
  </cmr:windowClose>
</cmr:window>
