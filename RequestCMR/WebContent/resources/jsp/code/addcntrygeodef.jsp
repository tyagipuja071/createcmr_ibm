<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.CntryGeoDefModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  CntryGeoDefModel user = (CntryGeoDefModel) request.getAttribute("cntrygeodefmain");
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
  FormManager.addValidator('geoCd', Validators.REQUIRED, [ 'Geo Code' ]);
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

var CntryGeoDefService = (function() {
  return {
    saveCntryGeo : function(typeflag) {
      if (typeflag){
        var cmrIssuingCntry=FormManager.getActualValue('cmrIssuingCntry');
        var geoCd=FormManager.getActualValue('geoCd');
        var check = cmr.query('CHECKCNTRYGEODEF', {geoCd : geoCd,cmrIssuingCntry : cmrIssuingCntry });
        if (check && check.ret1 == '1'){
          cmr.showAlert('This country geo default already exists in the system.');
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

  <form:form method="POST" action="${contextPath}/code/cntrygeodefmain" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="cntrygeodefmain">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Create Country-GEO Definition" : "Update Country-GEO Definition"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="geoCd">Geo Code: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cntrygeodefmain.geoCd != null}">
              
         <form:input path="geoCd" value="${cntrygeodefmain.geoCd}" dojoType="dijit.form.TextBox" readonly="true" />
              
            </c:if>
            <c:if test="${cntrygeodefmain.geoCd == null}">
               <form:input path="geoCd" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
         <cmr:column span="1" width="135">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Cntry: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cntrygeodefmain.cmrIssuingCntry != null}">
              
         <form:input path="cmrIssuingCntry" value="${cntrygeodefmain.cmrIssuingCntry}" dojoType="dijit.form.TextBox" readonly="true"/>
              
            </c:if>
            <c:if test="${cntrygeodefmain.cmrIssuingCntry == null}">
               <form:input path="cmrIssuingCntry" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
         
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="cntryDesc">Country Desc: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cntrygeodefmain.cntryDesc != null}">
              
         <form:input path="cntryDesc" value="${cntrygeodefmain.cntryDesc}" dojoType="dijit.form.TextBox" />
              
            </c:if>
            <c:if test="${cntrygeodefmain.cntryDesc == null}">
               <form:input path="cntryDesc" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
          <cmr:column span="1" width="135">
          <p>
            <cmr:label fieldId="comments">Comments: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cntrygeodefmain.comments != null}">
              
         <form:input path="comments" value="${cntrygeodefmain.comments}" dojoType="dijit.form.TextBox" />
              
            </c:if>
            <c:if test="${cntrygeodefmain.comments == null}">
               <form:input path="comments" dojoType="dijit.form.TextBox" />
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
            <cmr:label fieldId="geoCd">Geo Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="geoCd" dojoType="dijit.form.TextBox" placeHolder="Geo Code"/>
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
            <cmr:label fieldId="cntryDesc">Country Desc: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="cntryDesc" dojoType="dijit.form.TextBox" placeHolder="Country Desc"/>
          </p>
        </cmr:column>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="comments">Comments: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="comments" dojoType="dijit.form.TextBox" placeHolder="Comment"/>
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
            <c:if test="${cntrygeodefmain.createTs != null}">
              <fmt:formatDate type="date" value="${cntrygeodefmain.createTs}" pattern="yyyy-MM-dd" />
              <form:hidden id="createTs" path="createTs" />
            </c:if>
            <c:if test="${cntrygeodefmain.createTs == null}">
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
            <c:if test="${cntrygeodefmain.lastUpdtTs != null}">
              <fmt:formatDate type="date" value="${cntrygeodefmain.lastUpdtTs}" pattern="yyyy-MM-dd" />
              <form:hidden id="lastUpdtTs" path="lastUpdtTs" />
            </c:if>
            <c:if test="${cntrygeodefmain.lastUpdtTs == null}">
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
            <c:if test="${cntrygeodefmain.createBy != null}">
              ${cntrygeodefmain.createBy}
              <form:hidden id="createBy" path="createBy" />
            </c:if>
            <c:if test="${cntrygeodefmain.createBy == null}">
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
            <c:if test="${cntrygeodefmain.lastUpdtBy != null}">
              ${cntrygeodefmain.lastUpdtBy}
              <form:hidden id="lastUpdtBy" path="lastUpdtBy" />
            </c:if>
            <c:if test="${cntrygeodefmain.lastUpdtBy == null}">
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
      <cmr:button label="Save" onClick="CntryGeoDefService.saveCntryGeo(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Save" onClick="CntryGeoDefService.saveCntryGeo(false)" highlight="true" />
      
    <%} %>
    <cmr:button label="Back to List" onClick="window.location = '${contextPath}/code/cntrygeodef'" pad="true" />
  </cmr:buttonsRow>
  <br>
</cmr:section>
<cmr:model model="cntrygeodefmain" />