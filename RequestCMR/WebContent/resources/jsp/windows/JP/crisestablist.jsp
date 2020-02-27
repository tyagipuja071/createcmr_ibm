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
  String access = (String) request.getParameter("access");
  if (access == null){
    access = "";
  }
%>
<script>
function backToSearch(){
  window.location = '<%=request.getContextPath()+"/window/crissearch"%>';
}

function backToCompanyList(){
  window.location = '<%=request.getContextPath()+"/window/criscomplist"%>';
}

function backToCompany(){
  window.location = '<%=request.getContextPath()+"/window/criscompdet/"+companyNo%>';
}

function actionsFormatter(value, rowIndex){
  var rowData = this.grid.getItem(rowIndex);
  var establishmentNo = rowData.establishmentNo;
  var companyNo = rowData.companyNo;
  return '<a href="javascript: showAccounts(\'' + establishmentNo + '\',\'' + companyNo + '\')">Show Accounts</a>';
}

function companyNoFormatter(value, rowIndex){
  return '<a href="javascript: showCompany(\'' + value + '\')" title="Open Company Details for '+value+'">'+value+'</a>';
}

function estabNoFormatter(value, rowIndex){
  var rowData = this.grid.getItem(rowIndex);
  var establishmentNo = rowData.establishmentNo;
  var companyNo = rowData.companyNo;
  return '<a href="javascript: showEstablishment(\'' + value + '\', \'' + companyNo + '\')" title="Open Establishment Details for '+value+'">'+value+'</a>';
}

function showCompany(companyNo){
  window.location = '<%=request.getContextPath()+"/window/criscompdet/"%>'+companyNo;
}

function showEstablishment(estabNo, companyNo){
  window.location = '<%=request.getContextPath()+"/window/crisestabdet/"%>'+estabNo+'?companyNo='+companyNo+'&fromEstab=Y&access=<%=access%>';
}

function showAccounts(establishmentNo, companyNo){
  window.location = '<%=request.getContextPath()+"/window/crisaccountlist"%>?establishmentNo='+establishmentNo+'&companyNo='+companyNo+'&access=<%=access%>';
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
        <cmr:grid url="/crisestablistget.json" id="complistgrid" span="6" width="1100" height="400" useFilter="true">
          <cmr:gridParam fieldId="companyNo" value="<%=companyNo%>"/>
          <cmr:gridCol width="80px" field="establishmentNo" header="Establishment No." >
            <cmr:formatter functionName="estabNoFormatter" /> 
          </cmr:gridCol>
          <cmr:gridCol width="80px" field="companyNo" header="Company No." >
            <cmr:formatter functionName="companyNoFormatter" /> 
          </cmr:gridCol>
          <cmr:gridCol width="110px" field="nameAbbr" header="Abbreviated Name" />
          <cmr:gridCol width="200px" field="nameKanji" header="Name (Kanji)" />
          <cmr:gridCol width="200px" field="address" header="Address" />
          <cmr:gridCol width="90px" field="postCode" header="Postal Code" />
          <cmr:gridCol width="70px" field="locCode" header="Location Code" />
          <cmr:gridCol width="70px" field="companyCd" header="Company Code" />
          <cmr:gridCol width="60px" field="jsic" header="JSIC" />
          <cmr:gridCol width="auto" field="bldg" header="Actions">
            <cmr:formatter functionName="actionsFormatter" /> 
          </cmr:gridCol>
        </cmr:grid>
      </cmr:column>
    </cmr:row>
  </form:form>
  <cmr:windowClose>
    <cmr:button label="Search Again" onClick="backToSearch()" highlight="true" pad="true"/>
    <cmr:button label="I don't like the choices" onClick="rejectSearch()" highlight="true" pad="true"/>
<%if (!StringUtils.isBlank(companyNo)){%>
  <%if ("CL".equals(access)){ %>
    <cmr:button label="Back to Company List" onClick="backToCompanyList()" highlight="false" pad="true" />
  <%} else if ("CD".equals(access)){%>
    <cmr:button label="Back to Company Details" onClick="backToCompany()" highlight="false" pad="true" />
  <%} %>
<%} %>
  </cmr:windowClose>
</cmr:window>
