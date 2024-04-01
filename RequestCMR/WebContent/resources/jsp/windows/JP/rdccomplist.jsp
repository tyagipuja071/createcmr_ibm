<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath()+"/window/rdcprocess";
%>
<script>
function backToSearch(){
  window.location = '<%=request.getContextPath()+"/window/rdcsearch"%>';
}

function actionsFormatter(value, rowIndex){
  var rowData = this.grid.getItem(rowIndex);
  var companyNo = rowData.companyNo;
  return '<a href="javascript: showAccounts(\'' + companyNo + '\')">Show Accounts</a>';
}

function companyNoFormatter(value, rowIndex){
  return '<a href="javascript: showCompany(\'' + value + '\')" title="Open Company Details for '+value+'">'+value+'</a>';
}

function showAccounts(companyNo){
  window.location = '<%=request.getContextPath()+"/window/rdcaccountlist"%>?access=CL&companyNo='+companyNo;
}

function showCompany(companyNo){
  window.location = '<%=request.getContextPath()+"/window/rdccompdet/"%>'+companyNo;
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
  <cmr:form method="POST" action="<%=actionUrl%>" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="crit">
    <jsp:include page="criscriteria.jsp" />
    <cmr:row>
      <cmr:column span="6">
        <cmr:grid url="/rdccomplistget.json" id="complistgrid" span="6" width="1100" height="400" useFilter="true">
          <cmr:gridCol width="80px" field="companyNo" header="Company No." >
            <cmr:formatter functionName="companyNoFormatter" /> 
          </cmr:gridCol>
          <cmr:gridCol width="120px" field="nameAbbr" header="Abbreviated Name" />
          <cmr:gridCol width="200px" field="nameKanji" header="Name (Kanji)" />
          <cmr:gridCol width="200px" field="address" header="Address" />
          <cmr:gridCol width="90px" field="postCode" header="Postal Code" />
          <cmr:gridCol width="90px" field="locCode" header="Location Code" />
          <cmr:gridCol width="60px" field="jsic" header="JSIC" />
          <cmr:gridCol width="90px" field="dunsNo" header="DUNS" />
          <cmr:gridCol width="auto" field="bldg" header="Actions">
            <cmr:formatter functionName="actionsFormatter" /> 
          </cmr:gridCol>
        </cmr:grid>
      </cmr:column>
    </cmr:row>
  </cmr:form>
  <cmr:windowClose>
    <cmr:button label="Search Again" onClick="backToSearch()" highlight="true" pad="true"/>
    <cmr:button label="I don't like the choices" onClick="rejectSearch()" highlight="true" pad="true"/>
  </cmr:windowClose>
</cmr:window>
