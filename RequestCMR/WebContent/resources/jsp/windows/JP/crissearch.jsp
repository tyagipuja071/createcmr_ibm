<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath()+"/window/crisprocess";
%>
<script>
function searchCRIS(){
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
  for (var i in document.forms['frmCMR'].elements){
    elem = document.forms['frmCMR'].elements[i];
    if (elem && elem.name){
      val = elem.value;
      if (val && val.trim() != ''){
        console.log('elem '+elem.name+' '+val);
        return true;
      }
    }
  }
  return false;
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
  <form:form method="POST" action="<%=actionUrl%>" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="crit">
    <!-- Company  -->
    <cmr:row>
      <cmr:column span="6">
        <h3>Company</h3>
      </cmr:column>
    </cmr:row>
    
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="companyNo">Company No:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="companyNo" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="6"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="companyAddress">Address:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="compAddress" dojoType="dijit.form.TextBox"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="compNameAbbr">Abbreviated Name:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="compNameAbbr" dojoType="dijit.form.TextBox" cssStyle="width:300px"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="compPostCode">Postal Code:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="compPostCode" dojoType="dijit.form.TextBox"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="compNameKanji">Name (Kanji):</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="compNameKanji" dojoType="dijit.form.TextBox" cssStyle="width:300px"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="compLocCode">Location Code:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="compLocCode" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="5"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="compJSIC">JSIC:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="compJSIC" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="5"/>
      </cmr:column>
    </cmr:row>
    
    <cmr:hr />
    
    <!-- Establishment -->    
    <cmr:row>
      <cmr:column span="6">
        <h3>Establishment</h3>
      </cmr:column>
    </cmr:row>
    
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="estabCompanyNo">Company No:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="estabCompanyNo" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="6"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="establishmentNo">Establishment No:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="establishmentNo" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="6"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="estabAddress">Address:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="estabAddress" dojoType="dijit.form.TextBox"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="estabNameAbbr">Abbreviated Name:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="estabNameAbbr" dojoType="dijit.form.TextBox" cssStyle="width:300px"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="estabPostCode">Postal Code:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="estabPostCode" dojoType="dijit.form.TextBox"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="estabNameKanji">Name (Kanji):</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="estabNameKanji" dojoType="dijit.form.TextBox" cssStyle="width:300px"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="estabLocCode">Location Code:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="estabLocCode" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="5"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="estabJSIC">JSIC:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="estabJSIC" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="5"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="estabCompanyCd">Company Code:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="estabCompanyCd" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="2"/>
      </cmr:column>
    </cmr:row>
    <cmr:hr />
    
    <!-- Account -->
    <cmr:row>
      <cmr:column span="6">
        <h3>Account</h3>
      </cmr:column>
    </cmr:row>
    
    <cmr:row>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="accountNo">Account No:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountNo" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="6"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="accountEstablishmentNo">Establishment No:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountEstablishmentNo" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="6"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="accountNameAbbr">Abbreviated Name:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountNameAbbr" dojoType="dijit.form.TextBox" cssStyle="width:300px"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="accountAddress">Address:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountAddress" dojoType="dijit.form.TextBox"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="addrNameKanji">Account_Customer Name:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="addrNameKanji" dojoType="dijit.form.TextBox" cssStyle="width:300px"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="accountPostalCode">Postal Code:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountPostalCode" dojoType="dijit.form.TextBox"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="accountJSIC">JSIC:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountJSIC" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="5"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="accountLocCode">Location Code:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountLocNo" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="5"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="accountSBO">Office Code:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountSBO" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="3"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="accountCompanyCd">Company Code:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountCompanyCd" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="2"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="accountSalesTeamCode">Sales/Team No.:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountSalesTeamCode" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="6"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="accountInacCede">INAC/NAC Code:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountInacCode" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="4"/>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="150">
        <label for="accountSR">Rep. Sales No.:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountSR" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="6"/>
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="accountCredtitToCustNo">Credit Customer No.:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountCredtitToCustNo" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="6"/>
      </cmr:column>
    </cmr:row>
     <cmr:row>
      <cmr:column span="1" width="150">
        <label for="accountAttach">IBM Related CMR.:</label>
      </cmr:column>
      <cmr:column span="2" width="350">
        <form:input path="accountAttach" dojoType="dijit.form.TextBox" cssStyle="width:65px" maxlength="6"/>
      </cmr:column>
    </cmr:row>


  </form:form>
  <cmr:windowClose>
    <cmr:button label="Search" onClick="searchCRIS()" highlight="true" pad="true"/>
    <cmr:button label="Clear Search Criteria" onClick="window.location = 'crissearch?clear=Y'" highlight="false" pad="true"/>
  </cmr:windowClose>
</cmr:window>
