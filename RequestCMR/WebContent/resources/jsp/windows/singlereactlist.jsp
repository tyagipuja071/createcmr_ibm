<%@page import="com.ibm.cio.cmr.request.model.window.SingleReactQueryRequest"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath()+"/window/singlereactprocess";
  String cntry =  (String)request.getAttribute("cmrCountry");
%>
<script>
function backToSearch(){
var reqId = FormManager.getActualValue('reqId'); 
  window.location = '<%=request.getContextPath()+"/window/singlereactsearch"%>?cntry='+<%=cntry%>+'&reqId='+reqId; 
}

function customerNoFormatter(value, rowIndex){
  var rowData = this.grid.getItem(rowIndex);
  var cmrName1 = rowData.cmrName1Plain;
  var cmrName2 = rowData.cmrName2Plain;
  var result= cmrName1 +"<br>" +cmrName2;
  var cntry = rowData.cmrIssuedBy;
  var kunnr = rowData.cmrSapNumber;
  return '<a href="javascript: showCustomerDetails(\'' + cntry + '\',\''+kunnr+'\')" title="Open Customer Details for '+value+'">'+result+'</a>';
  
}
function customerNameFormatter(value, rowIndex){
  var rowData = this.grid.getItem(rowIndex);
  var cmrName3 = rowData.cmrName3;
  var cmrName4 = rowData.cmrName4;
  return cmrName3 +"<br>" +cmrName4;
}
function showCustomerDetails(cntry, kunnr){
  	window.location = '<%=request.getContextPath()+"/window/singlereactdet/"%>'+cntry+'/'+kunnr;
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
    <jsp:include page="singlereactcriteria.jsp"></jsp:include>
    <cmr:row>
      <cmr:column span="6">
        <cmr:grid url="/singlereactlistget.json" id="singlereactlistgrid" span="6" width="1100" height="400" useFilter="true">
      
          <cmr:gridCol width="200px" field="cmrSapNumber" header="Customer Name 1-2" >
          <cmr:formatter functionName="customerNoFormatter" /> 
          </cmr:gridCol>
          <cmr:gridCol width="200px" field="cmrName3" header="Customer Name 3-4" >
          <cmr:formatter functionName="customerNameFormatter" /> 
          </cmr:gridCol>
          <cmr:gridCol width="200px" field="cmrNum" header="CMR#" />
          <cmr:gridCol width="90px" field="cmrIssuedBy" header="Issuing Country" />
          <cmr:gridCol width="70px" field="cmrCity" header="Address" />
          <cmr:gridCol width="70px" field="cmrCity" header="City1" />
         <cmr:gridCol width="60px" field="cmrState" header="State/Proviance" />
          <cmr:gridCol width="60px" field="cmrPostalCode" header="Postal Code" />
          <cmr:gridCol width="60px" field="cmrVat" header="Vat" />
        </cmr:grid>
      </cmr:column>
    </cmr:row>
  </form:form>
  <cmr:windowClose>
    <cmr:button label="Search Again" onClick="backToSearch()" highlight="true" pad="true"/>
  </cmr:windowClose>
</cmr:window>
