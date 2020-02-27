<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath()+"/window/crisprocess";
  String companyNo = request.getParameter("companyNo");
  if (companyNo == null){
    companyNo = "";
  }
  String establishmentNo = request.getParameter("establishmentNo");
  if (establishmentNo == null){
    establishmentNo = "";
  }
  String access = (String) request.getParameter("access");
  if (access == null){
    access = "";
  }
%>
<script>
function backToSearch(){
  window.location = '<%=request.getContextPath()+"/window/crissearch"%>';
}

function backToEstablishmentList(){
  window.location = '<%=request.getContextPath()+"/window/crisestablist?companyNo="+companyNo+"&access="+access%>';
}

function accountNoFormatter(value, rowIndex){
  var rowData = this.grid.getItem(rowIndex);
  var establishmentNo = rowData.establishmentNo;
  var companyNo = '<%=companyNo%>';
  return '<a href="javascript: showAccount(\'' + value + '\', \'' + establishmentNo + '\', \'' + companyNo + '\')" title="Open Account Details for '+value+'">'+value+'</a>';
}

function estabNoFormatter(value, rowIndex){
  var rowData = this.grid.getItem(rowIndex);
  var establishmentNo = rowData.establishmentNo;
  var companyNo = '<%=companyNo%>';
  return '<a href="javascript: showEstablishment(\'' + value + '\', \'' + companyNo + '\')" title="Open Establishment Details for '+value+'">'+value+'</a>';
}

function showAccount(accountNo, establishmentNo, companyNo){
  window.location = '<%=request.getContextPath()+"/window/crisaccountdet/"%>'+accountNo+'?companyNo='+companyNo+'&establishmentNo='+establishmentNo+'&fromAccount=Y&&access=<%=access%>';
}

function showEstablishment(establishmentNo, companyNo){
  window.location = '<%=request.getContextPath()+"/window/crisestabdet/"%>'+establishmentNo+'?companyNo='+companyNo+'&fromAccount=Y&&access=<%=access%>';
  
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
  <jsp:include page="crisreject.jsp" />
  <form:form method="POST" action="<%=actionUrl%>" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="crit">
    <jsp:include page="criscriteria.jsp" />
    <cmr:row>
      <cmr:column span="6">
        <cmr:grid url="/crisaccountlistget.json" id="accountlistgrid" span="6" width="1100" height="400" useFilter="true">
          <cmr:gridParam fieldId="establishmentNo" value="<%=establishmentNo%>"/>
          <cmr:gridCol width="80px" field="accountNo" header="Account No." >
            <cmr:formatter functionName="accountNoFormatter"/>
          </cmr:gridCol>
          <cmr:gridCol width="80px" field="establishmentNo" header="Establishment No." >
            <cmr:formatter functionName="estabNoFormatter"/>
          </cmr:gridCol>
          <cmr:gridCol width="150px" field="nameAbbr" header="Abbreviated Name" />
          <cmr:gridCol width="auto" field="nameKanji" header="Name (Kanji)" />
          <cmr:gridCol width="90px" field="locCode" header="Location Code" />
          <cmr:gridCol width="70px" field="companyCd" header="Company Code"/>
          <cmr:gridCol width="70px" field="sbo" header="Office Code"/>
          <cmr:gridCol width="80px" field="salesTeamCode" header="Sales/Team No."/>
          <cmr:gridCol width="70px" field="inacCode" header="INAC/NAC Code"/>
          <cmr:gridCol width="80px" field="creditToCustNo" header="Credit Customer No."/>
        </cmr:grid>
      </cmr:column>
    </cmr:row>
  </form:form>
  <cmr:windowClose>
    <cmr:button label="Search Again" onClick="backToSearch()" highlight="true" pad="true"/>
    <cmr:button label="I don't like the choices" onClick="rejectSearch()" highlight="true" pad="true"/>
<%if (!StringUtils.isBlank(establishmentNo)){%>
    <cmr:button label="Back to Establishment List" onClick="backToEstablishmentList()" highlight="false" pad="true" />
<%} %>
  </cmr:windowClose>
</cmr:window>
