<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath() + "/window/crisprocess";
%>
<script>
function backToSearch(){
  window.location = '<%=request.getContextPath() + "/window/crissearch"%>';
}

function checkCriteria(){
  var elem = null;
  var val = null;
  for (var i in document.forms['frmCMR'].elements){
    elem = document.forms['frmCMR'].elements[i];
    if (elem && elem.name){
      val = elem.value;
      if (val && val.trim() != '' && val.trim() != '0'){
        if (!elem.name.includes('Name')){
          console.log('elem '+elem.name+' '+val);
          return false;          
        }
      }
    }
  }
  return true;
}

function noResults(){
//  if (checkCriteria()){
  var result = {
      accepted : 'n',
      nodata : true
   };
   if (window.opener){
     window.opener.cmr.hideProgress();
     window.opener.doImportCRISRecord(result);
     WindowMgr.closeMe();    
   }
//  }
  //Defect 1713750
//   else {
//   WindowMgr.closeMe(); 
 //   cmr.showAlert('Please try searching again using Name fields only.');
//  }
    
}

function trackMe() {
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
    <cmr:row>
      <cmr:column span="6">
          <strong>
            <span id="cmr-error-box-msg" style="color:red">No record matched the search. 
            You can choose to Search Again or return back to the tool to record the results.
            </span>
          </strong>
      </cmr:column>
    </cmr:row>
  </form:form>
  <cmr:windowClose>
    <cmr:button label="Search Again" onClick="backToSearch()" highlight="true" pad="true"/>
    <cmr:button label="Return 'No Results'" onClick="noResults()" highlight="false" pad="true"/>
  </cmr:windowClose>
</cmr:window>
