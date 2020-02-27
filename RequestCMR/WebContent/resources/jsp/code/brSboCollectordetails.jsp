<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.BRSboCollectorModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  BRSboCollectorModel sboModel = (BRSboCollectorModel) request
          .getAttribute("brSboCollecDetails");
      boolean newEntry = false;
      if (sboModel.getState() == BaseModel.STATE_NEW) {
        newEntry = true;
      }
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
<%if (newEntry) {%>
  //FormManager.addValidator('displaySeqNo', Validators.REQUIRED, [ 'Display Sequence No.' ]);

    FormManager.addValidator('stateCd', Validators.REQUIRED, [ 'State Code' ]);
    FormManager.addValidator('stateName', Validators.REQUIRED, [ 'State Name' ]);
    FormManager.addValidator('sbo', Validators.REQUIRED, [ 'SBO' ]);
    FormManager.addValidator('mrcCd', Validators.REQUIRED, [ 'MRC Code' ]);
    FormManager.addValidator('collectorNo', Validators.REQUIRED, [ 'Collector No' ]);
<%}%>
    FormManager.addValidator('sbo', Validators.REQUIRED, [ 'SBO' ]);
    FormManager.addValidator('mrcCd', Validators.REQUIRED, [ 'MRC Code' ]);
    FormManager.addValidator('collectorNo', Validators.REQUIRED, [ 'Collector No' ]);
    FormManager.ready();
  });

  var SboCollectorService = (function() {
    return {
      saveSBO : function() {
        FormManager.save('frmCMR');
      },
      removeSBO : function(){
        FormManager.remove('frmCMR');
      }
    };
  })();
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/brSboCollectordetails" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="brSboCollecDetails">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add SBO/Collection Mapping" : "Update SBO/Collection Mapping"%></h3>
        </cmr:column>
      </cmr:row>

      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="stateCd">State Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${brSboCollecDetails.stateCd}</p>
          <form:hidden id="stateCd" path="stateCd" />
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="stateName">State Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${brSboCollecDetails.stateName}</p>
          <form:hidden id="stateName" path="stateName" />
        </cmr:column>
      </cmr:row>

      <%
        } else {
      %>
      <cmr:row>
         <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="stateCd">State Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="stateCd" dojoType="dijit.form.TextBox" cssStyle="width:600px" />
          </p>
        </cmr:column>
      </cmr:row>
       <cmr:row>
         <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="stateName">State Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="stateName" dojoType="dijit.form.TextBox" cssStyle="width:600px" />
          </p>
        </cmr:column>
      </cmr:row>
      <%
        }
      %>

      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="sbo">SBO: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="sbo" dojoType="dijit.form.TextBox" cssStyle="width:600px" />
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="mrcCd">MRC Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="mrcCd" dojoType="dijit.form.TextBox" cssStyle="width:600px" />
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="collectorNo">Collector No.: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="collectorNo" dojoType="dijit.form.TextBox" cssStyle="width:600px" />
          </p>
        </cmr:column>
      </cmr:row>
      <form:hidden id="createTs" path="createTs" />
      <form:hidden id="lastUpdtTs" path="lastUpdtTs" />
      <form:hidden id="createBy" path="createBy" />
      <form:hidden id="lastUpdtBy" path="lastUpdtBy" />

    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%
      if (newEntry) {
    %>
    <cmr:button label="Save" onClick="SboCollectorService.saveSBO()" highlight="true" />
    <%
      } else {
    %>
    <cmr:button label="Save" onClick="SboCollectorService.saveSBO()" highlight="true" />
    <cmr:button label="Delete" onClick="SboCollectorService.removeSBO()" pad="true" />
    <%
      }
    %>
    <cmr:button label="Back to SBO/Collector List" onClick="window.location = '${contextPath}/code/brSboCollector'" pad="true" />
  </cmr:buttonsRow>
  <br>

</cmr:section>
<cmr:model model="brSboCollecDetails" />