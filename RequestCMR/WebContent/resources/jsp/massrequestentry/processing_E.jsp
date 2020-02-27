<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="java.util.List"%>
<%@page import="com.ibm.cio.cmr.request.ui.FieldInformation"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  AppUser user = AppUser.getUser(request);
			boolean noFindCMR = user.getAuthCode() == null;
			RequestEntryModel reqentry = (RequestEntryModel) request
					.getAttribute("reqentry");
			boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
			Boolean readOnly = (Boolean) request
					.getAttribute("yourActionsViewOnly");
			if (readOnly == null) {
				readOnly = false;
			}
			String mcFileVersion = SystemConfiguration
					.getValue("MASS_CREATE_TEMPLATE_VER");
			String procCenter = reqentry.getProcCenter() != null ? reqentry
					.getProcCenter() : "";
%>

<script>
  dojo.addOnLoad(function() {
  if (FormManager.getActualValue('cmrIssuingCntry') == '760') {
    //  FormManager.addValidator('entNo', Validators.REQUIRED, [ 'Enterprise Number' ]);
      //FormManager.addValidator('entNo', Validators.NUMBER, [ 'Enterprise Number' ]);
    if (FormManager.getActualValue('entUpdTyp') == 'A') {
      FormManager.addValidator('comp', Validators.REQUIRED, [ 'Company Number' ]);
    //  FormManager.addValidator('comp', Validators.NUMBER, [ 'Company Number' ]);
      FormManager.addValidator('cname1', Validators.REQUIRED, [ 'New Enterprise Number' ]);
      //FormManager.addValidator('cname1', Validators.NUMBER, [ 'New Enterprise Number' ]);
    }
    if (FormManager.getActualValue('entUpdTyp') == 'B') {
      FormManager.addValidator('comp1', Validators.REQUIRED, [ 'Company Number' ]);
      //FormManager.addValidator('comp1', Validators.NUMBER, [ 'Company Number' ]);
      FormManager.addValidator('newEntp', Validators.REQUIRED, [ 'New Establishment Number' ]);
      //FormManager.addValidator('newEntp', Validators.NUMBER, [ 'New Establishment Number' ]);
    }
    if (FormManager.getActualValue('entUpdTyp') == 'C') {
      FormManager.addValidator('newEntpName', Validators.REQUIRED, [ 'Establishment Number' ]);
     // FormManager.addValidator('newEntpName', Validators.NUMBER, [ 'Establishment Number' ]);
      FormManager.addValidator('newEntpNameCont', Validators.REQUIRED, [ 'New Account Number' ]);
     // FormManager.addValidator('newEntpNameCont', Validators.NUMBER, [ 'New Account Number' ]);
    }
  }else{
    FormManager.addValidator('entNo', Validators.REQUIRED, [ 'Enterprise No.' ]);
    FormManager.addValidator('entNo', Validators.NUMBER, [ 'Enterprise No.' ]);
    if (FormManager.getActualValue('entUpdTyp') == 'A') {
      FormManager.addValidator('comp', Validators.REQUIRED, [ 'Company Number' ]);
      FormManager.addValidator('comp', Validators.NUMBER, [ 'Company Number' ]);
      FormManager.addValidator('cname1', Validators.REQUIRED, [ 'New Company Name' ]);
    }
    if (FormManager.getActualValue('entUpdTyp') == 'B') {
      FormManager.addValidator('comp1', Validators.REQUIRED, [ 'Company Number' ]);
      FormManager.addValidator('comp1', Validators.NUMBER, [ 'Company Number' ]);
      FormManager.addValidator('newEntp', Validators.REQUIRED, [ 'New Enterprise No.' ]);
      FormManager.addValidator('newEntp', Validators.NUMBER, [ 'New Enterprise No.' ]);
    }
    if (FormManager.getActualValue('entUpdTyp') == 'C') {
      FormManager.addValidator('newEntpName', Validators.REQUIRED, [ 'New Enterprise Name' ]);
      //FormManager.addValidator('newEntpNameCont', Validators.REQUIRED, [ 'New Enterprise Name Cont.' ]);
    }
   }
    UpdateByEntSrvc.delayedConnectRadios();
<%if (readOnly) {%>
  UpdateByEntSrvc.disableEntFields();
<%}%>
  });
</script>
<jsp:include page="/resources/jsp/requestentry/detailstrip.jsp" />
<cmr:view exceptForCountry="760">
<cmr:row addBackground="true">
  <cmr:column span="6">
    <h3>Select the type of Update you want to do for the Enterprise:</h3>
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">
  <cmr:column span="6">
    <p>
      <cmr:label fieldId="entNo">
             Enterprise # <span style="color: red">*</span>: 
          </cmr:label>
      <form:input path="entNo" id="entNo" dojoType="dijit.form.TextBox" placeHolder="Enterprise # " maxlength="7" />
  </cmr:column>
</cmr:row>
<cmr:row topPad="10" addBackground="true">
  <cmr:column span="6">
    <input type="radio" id="entUpdTyp_A" name="entUpdTyp" value="A" onclick="UpdateByEntSrvc.clickRadio(this)"> Update Company Legal Name
  </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="comp">
             Company #: 
          </cmr:label>
      <form:input path="comp" id="comp" dojoType="dijit.form.TextBox" placeHolder="Company #" maxlength="8" />

    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="cname1">
             New Company Name: 
          </cmr:label>
      <form:input path="cname1" id="cname1" dojoType="dijit.form.TextBox" placeHolder="Company Name " maxlength="52" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row topPad="10" addBackground="true">
  <cmr:column span="6">
    <input type="radio" id="entUpdTyp_B" name="entUpdTyp" value="B" onclick="UpdateByEntSrvc.clickRadio(this)"> Move Company to new Enterprise
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="comp1">
             Company #: 
          </cmr:label>
      <form:input path="comp1" id="comp1" dojoType="dijit.form.TextBox" placeHolder="Company # " maxlength="8" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="newEntp">
             New Enterprise #: 
          </cmr:label>
      <form:input path="newEntp" id="newEntp" dojoType="dijit.form.TextBox" placeHolder="New Enterprise # " maxlength="7" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row topPad="10" addBackground="false">
  <cmr:column span="6">
    <input type="radio" id="entUpdTyp_C" name="entUpdTyp" value="C" onclick="UpdateByEntSrvc.clickRadio(this)"> Update Enterprise Name
  </cmr:column>
</cmr:row>
<cmr:row addBackground="false">
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="newEntpName">
             New Enterprise Name: 
          </cmr:label>
      <form:input path="newEntpName" id="newEntpName" dojoType="dijit.form.TextBox" placeHolder="New Enterprise Name " maxlength="28" />
    </p>
  </cmr:column>

  <cmr:column span="2">
    <p>
      <cmr:label fieldId="newEntpNameCont">
             New Enterprise Name Con't: 
          </cmr:label>
      <form:input path="newEntpNameCont" id="newEntpNameCont" dojoType="dijit.form.TextBox" placeHolder="New Enterprise Name Con't' " maxlength="24" />
    </p>
  </cmr:column>
</cmr:row>
</cmr:view>
<cmr:view forCountry="760">
<cmr:row addBackground="true">
  <cmr:column span="6">
    <h3>Select the type of structure change:</h3>
  </cmr:column>
</cmr:row>
<cmr:row topPad="10" addBackground="true">
  <cmr:column span="6">
    <input type="radio" id="entUpdTyp_A" name="entUpdTyp" value="A" onclick="UpdateByEntSrvc.clickRadio(this)"> Move Company to new Enterprise.
  </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="comp">
             Company No #: 
          </cmr:label>
      <form:input path="comp" id="comp" dojoType="dijit.form.TextBox" placeHolder="Company No #" maxlength="6" />

    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="cname1">
             New Enterprise No #: 
          </cmr:label>
      <form:input path="cname1" id="cname1" dojoType="dijit.form.TextBox" placeHolder="New Enterprise No # " maxlength="6" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row topPad="10" addBackground="true">
  <cmr:column span="6">
    <input type="radio" id="entUpdTyp_B" name="entUpdTyp" value="B" onclick="UpdateByEntSrvc.clickRadio(this)"> Move Establishment to new Company.
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="comp1">
             Establishment NO #: 
          </cmr:label>
      <form:input path="comp1" id="comp1" dojoType="dijit.form.TextBox" placeHolder="Establishment NO # " maxlength="6" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="newEntp">
             New Company No #: 
          </cmr:label>
      <form:input path="newEntp" id="newEntp" dojoType="dijit.form.TextBox" placeHolder="New Company No # " maxlength="6" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row topPad="10" addBackground="false">
  <cmr:column span="6">
    <input type="radio" id="entUpdTyp_C" name="entUpdTyp" value="C" onclick="UpdateByEntSrvc.clickRadio(this)"> Move Account to new Establishment.
  </cmr:column>
</cmr:row>
<cmr:row addBackground="false">
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="newEntpName">
             Account No #: 
          </cmr:label>
      <form:input path="newEntpName" id="newEntpName" dojoType="dijit.form.TextBox" placeHolder="Account No # " maxlength="6" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="newEntpNameCont">
             New Establishment No #: 
          </cmr:label>
      <form:input path="newEntpNameCont" id="newEntpNameCont" dojoType="dijit.form.TextBox" placeHolder="New Establishment No # " maxlength="6" />
    </p>
  </cmr:column>
</cmr:row>
<%
  if (!readOnly && CmrConstants.Role_Processor.equalsIgnoreCase(reqentry.getUserRole())) {
%>
<cmr:row topPad="5" addBackground="true">
  <cmr:column span="2">
    <div style="padding-top: 18px">
      <cmr:button label="${ui.btn.markAsCompleted}" onClick="markAsCompleted()" highlight="false"></cmr:button>
    </div>
  </cmr:column>
  <cmr:column span="2">
  </cmr:column>
</cmr:row>
<%
  }
%>
</cmr:view>