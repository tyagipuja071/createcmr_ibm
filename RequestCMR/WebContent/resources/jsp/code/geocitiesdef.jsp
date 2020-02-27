<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.GeoCitiesModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  GeoCitiesModel user = (GeoCitiesModel) request.getAttribute("geoCitiesMain");
      boolean newEntry = false;
      if (user.getState() == BaseModel.STATE_NEW) {
        newEntry = true;
      } else {
        newEntry = false;
      }
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script>
  dojo.addOnLoad(function() {
<%if (newEntry) {%>
  FormManager.addValidator('cityId', Validators.REQUIRED, [ 'City ID' ]);
  FormManager.addValidator('cmrIssuingCntry', Validators.REQUIRED, [ 'CMR Issuing Country' ]);
  FormManager.addValidator('cityDesc', Validators.REQUIRED, [ 'City Description' ]);
  
  var ids1 = cmr.query('SYSTEM.GET_SUPP_CNTRY', {
      _qall : 'Y'
    });
    var model1 = {
      identifier : "id",
      label : "name",
      items : []
    };
    //model1.items.push({id : 'WW', name : 'WW'});
    for ( var i = 0; i < ids1.length; i++) {
      model1.items.push({
        id : ids1[i].ret1,
        name : ids1[i].ret2
      });
    }
    var dropdown1 = {
      listItems : model1
    };
    FilteringDropdown.loadFixedItems('cmrIssuingCntry', null, dropdown1);
    
<%}%>
  FormManager.ready();
  });

  var GeoCitiesService = (function() {
	return {
	 saveGeoCity : function(typeflag) {
	 	if (typeflag){
			var cmrIssuingCntry=FormManager.getActualValue('cmrIssuingCntry');
			var cityId=FormManager.getActualValue('cityId');
			var check = cmr.query('CHECKGEOCITY', {cityId : cityId, issuingCntry : cmrIssuingCntry });
			if (check && check.ret1 == '1'){
				cmr.showAlert('This collector number already exists in the system.');
				return;
			}
		}
		FormManager.save('frmCMR');
		}
	};
	})();
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/geocitiesmain" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="geoCitiesMain">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Create Ceo City Definition" : "Geo City Definition"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="cityId">City ID: </cmr:label>
          </p>
        </cmr:column>
      	<cmr:column span="2">
          <p>
          	<form:input path="cityId" value="${geoCitiesMain.cityId}" dojoType="dijit.form.TextBox" readonly="true"/>
          </p>
        </cmr:column>
         <cmr:column span="1" width="135">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Cntry: </cmr:label>
          </p>
        </cmr:column>
      	<cmr:column span="2">
          <p>
            <c:if test="${geoCitiesMain.cmrIssuingCntry != null}">
         		<form:input path="cmrIssuingCntry" value="${geoCitiesMain.cmrIssuingCntry}" dojoType="dijit.form.TextBox" readonly="true"/>
            </c:if>
            <c:if test="${geoCitiesMain.cmrIssuingCntry == null}">
               <form:input path="cmrIssuingCntry" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
      </cmr:row> 
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="cityDesc">City Desc: </cmr:label>
          </p>
        </cmr:column>
      	<cmr:column span="2">
          <p>
          	<form:input path="cityDesc" value="${geoCitiesMain.cityDesc}" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
         <cmr:column span="1" width="135">
          <p>
            &nbsp;
          </p>
        </cmr:column>
      	<cmr:column span="2">
          <p>
            &nbsp;
          </p>
        </cmr:column>
      </cmr:row>      
      <%
        } else {
      %>
       <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="cityId">City ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="cityId" dojoType="dijit.form.TextBox" placeHolder="City Id"/>
          </p>
        </cmr:column>
        <cmr:column span="1" width="135">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Cntry: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCntry" searchAttr="name" style="display: block;width:250px" maxHeight="200"
              required="false" path="cmrIssuingCntry" placeHolder="CMR Issuing Countries">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="cityDesc">City Desc: </cmr:label>
          </p>
        </cmr:column>
      	<cmr:column span="2">
          <p>
          	<form:input path="cityDesc" dojoType="dijit.form.TextBox" placeHolder="City Description"/>
          </p>
        </cmr:column>
         <cmr:column span="1" width="135">
          <p>
            &nbsp;
          </p>
        </cmr:column>
      	<cmr:column span="2">
          <p>
            &nbsp;
          </p>
        </cmr:column>
      </cmr:row> 
      <%
        }
      %>
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="GeoCitiesService.saveGeoCity(true)" highlight="true" />
    <%} else { %>
    <!-- 
      <cmr:button label="Delete" onClick="GeoCitiesService.delGeoCity()" highlight="true" />
      --> 
      <cmr:button label="Save" onClick="GeoCitiesService.saveGeoCity(false)" highlight="true" />
    <%} %>
    <cmr:button label="Back to List" onClick="window.location = '${contextPath}/code/geocitieslistdef'" pad="true" />
  </cmr:buttonsRow>
  <br>
</cmr:section>
<cmr:model model="geoCitiesMain" />