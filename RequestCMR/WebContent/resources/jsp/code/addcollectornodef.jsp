<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.CollectorNameNoModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  CollectorNameNoModel user = (CollectorNameNoModel) request.getAttribute("collNoMain");
      boolean newEntry = false;
      if (user.getState() == BaseModel.STATE_NEW) {
        newEntry = true;
      } else {
      }
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script>
  dojo.addOnLoad(function() {
<%if (newEntry) {%>
  FormManager.addValidator('collectorNo', Validators.REQUIRED, [ 'Collector Number' ]);
  FormManager.addValidator('cmrIssuingCntry', Validators.REQUIRED, [ 'CMR Issuing Country' ]);
  
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

  var CollectorNameNoService = (function() {
	return {
	 saveCollectorNo : function(typeflag) {
	 	if (typeflag){
			var cmrIssuingCntry=FormManager.getActualValue('cmrIssuingCntry');
			var collectorNo=FormManager.getActualValue('collectorNo');
			var check = cmr.query('CHECKCOLLECTORNO', {collectorNo : collectorNo, issuingCntry : cmrIssuingCntry });
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

  <form:form method="POST" action="${contextPath}/code/collectornamenomain" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="collNoMain">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Create LA Collector Number Definition" : "LA Collector Number Definition"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="collectorNo">Collector Number: </cmr:label>
          </p>
        </cmr:column>
      	<cmr:column span="2">
          <p>
          	<form:input path="collectorNo" value="${collNoMain.collectorNo}" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
         <cmr:column span="1" width="135">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Cntry: </cmr:label>
          </p>
        </cmr:column>
      	<cmr:column span="2">
          <p>
            <c:if test="${collNoMain.cmrIssuingCntry != null}">
         		<form:input path="cmrIssuingCntry" value="${collNoMain.cmrIssuingCntry}" dojoType="dijit.form.TextBox" readonly="true"/>
            </c:if>
            <c:if test="${collNoMain.cmrIssuingCntry == null}">
               <form:input path="cmrIssuingCntry" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
      </cmr:row>      
      <%
        } else {
      %>
       <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="collectorNo">Collector Number: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="collectorNo" dojoType="dijit.form.TextBox" placeHolder="Collector Number"/>
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
      <%
        }
      %>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="createTs">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${collNoMain.createTs != null}">
              <fmt:formatDate type="date" value="${collNoMain.createTs}" pattern="yyyy-MM-dd" />
              <form:hidden id="createTs" path="createTs" />
            </c:if>
            <c:if test="${collNoMain.createTs == null}">
              -
            </c:if>
          </p>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="lastUpdtTs">Last Updated: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${collNoMain.lastUpdtTs != null}">
              <fmt:formatDate type="date" value="${collNoMain.lastUpdtTs}" pattern="yyyy-MM-dd" />
              <form:hidden id="lastUpdtTs" path="lastUpdtTs" />
            </c:if>
            <c:if test="${collNoMain.lastUpdtTs == null}">
              -
            </c:if>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="createBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${collNoMain.createBy != null}">
              ${collNoMain.createBy}
              <form:hidden id="createBy" path="createBy" />
            </c:if>
            <c:if test="${collNoMain.createBy == null}">
              -
            </c:if>
          </p>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="lastUpdtBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${collNoMain.lastUpdtBy != null}">
              ${collNoMain.lastUpdtBy}
              <form:hidden id="lastUpdtBy" path="lastUpdtBy" />
            </c:if>
            <c:if test="${collNoMain.lastUpdtBy == null}">
              -
            </c:if>
          </p>
        </cmr:column>
      </cmr:row>

    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="CollectorNameNoService.saveCollectorNo(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Delete" onClick="CollectorNameNoService.delCollectorNo()" highlight="true" />
      <cmr:button label="Save" onClick="CollectorNameNoService.saveCollectorNo(false)" highlight="true" />
      
    <%} %>
    <cmr:button label="Back to List" onClick="window.location = '${contextPath}/code/collectornodef'" pad="true" />
  </cmr:buttonsRow>
  <br>
</cmr:section>
<cmr:model model="collNoMain" />