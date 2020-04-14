<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath()+"/window/singlereactprocess";
  String katr6 = request.getParameter("cntry"); 
  String reqId = request.getParameter("reqId");;
%>
<script>
function searchSREACT(){
  if (validateParams()){
    cmr.showProgress('Performing search, please wait...');
    document.forms['frmCMR'].submit();
  } else {
    cmr.showAlert('Please specify at least 1 value on the criteria.');
  }
}

function validateParams(){
  var elem = null;
  var val = null;
  var count = 0;
  for (var i in document.forms['frmCMR'].elements){
    elem = document.forms['frmCMR'].elements[i];
    if (elem && elem.name){
      val = elem.value;
      
      if (val && val.trim() != ''){
      	count++;
        console.log('elem '+elem.name+' '+val);    
      }
      if(count ==3){
      return true;
      }
    }
  }
  return false;
}

function clearSearch(){
	var cntry =FormManager.getActualValue("katr6");
 	var reqId = FormManager.getActualValue("reqId");
 	window.location = '<%=request.getContextPath() + "/window/singlereactsearch"%>?clear=Y&cntry='+cntry+'&reqId='+reqId;
}
function trackMe(){
  // noop
}
WindowMgr.trackMe = trackMe;  

</script>
<style>
span.section {
  font-weight: bold;
  text-decoration: underline;
  
}
div.ibm-columns {
  width: 1100px !important;
}
</style>
<cmr:window>
<form:form method="POST" action="<%=actionUrl%>" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="sreact">
   	<form:hidden path="katr6" />
   	<form:hidden path="reqId" />
   
     <cmr:row>
      <cmr:column span="1" width="150">
        <label for="zzkvCusno">CMR Number:</label><!-- KNA1.ZZKV_CUSNO zzkvCusno-->
      </cmr:column>
      <cmr:column span="2" width="350">
      <form:input path="zzkvCusno" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="6"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="stras">Street Address:</label><!-- KNA1.STRAS stras-->
      </cmr:column>
      <cmr:column span="2" width="350">
       <form:input path="stras" dojoType="dijit.form.TextBox"/>
      </cmr:column>
    </cmr:row> 
    
        <cmr:row>
      <cmr:column span="1" width="150">
        <label for="name1">Customer Name:</label><!--KNA1.NAME1  name1-->
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="name1" dojoType="dijit.form.TextBox" cssStyle="width:300px"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="pstlz">Postal Code:</label> <!-- KNA1.PSTLZ pstlz-->
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="pstlz" dojoType="dijit.form.TextBox"/>
      </cmr:column>
    </cmr:row>
    
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="telx1">Abbreviated Name:</label><!-- KNA1.TELX1 telx1-->
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="telx1" dojoType="dijit.form.TextBox" cssStyle="width:300px"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="stcd1">Fiscal/Local Tax Code:</label><!-- KNA1.STCD1 stcd1-->
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="stcd1" dojoType="dijit.form.TextBox"/>
      </cmr:column>
    </cmr:row>
</form:form>
  <cmr:windowClose>
    <cmr:button label="Search" onClick="searchSREACT()" highlight="true" pad="true"/>
    <cmr:button label="Clear Search Criteria" onClick="clearSearch()" highlight="false" pad="true"/>
  </cmr:windowClose>
</cmr:window>
