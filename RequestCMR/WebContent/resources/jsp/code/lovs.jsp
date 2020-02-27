<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  boolean admin = AppUser.getUser(request).isAdmin();
%>
<script>
  dojo.addOnLoad(function() {

  var ids = cmr.query('LOVFIELDIDLIST', {_qall  : 'Y'});
    var model = { 
        identifier : "id", 
        label : "name",
        selectedItem : <%=request.getParameter("fieldId") != null ? "'"+request.getParameter("fieldId")+"'" : "null"%>,
        items : []
    };
    for (var i =0; i < ids.length; i++){
      model.items.push({id : ids[i].ret1, name : ids[i].ret1});
    }
    var dropdown = {
        listItems : model
    };
    FilteringDropdown.loadFixedItems('fieldId', null, dropdown);
    dojo.connect(FormManager.getField('fieldId'), 'onChange', function(value) {
      var url = cmr.CONTEXT_ROOT + '/code/lovslist.json';
      CmrGrid.refresh('lovUrlsGrid', url, 'fieldId=:fieldId&cmrIssuingCntry=:cmrIssuingCntry');
    });    
    dojo.connect(FormManager.getField('cmrIssuingCntry'), 'onChange', function(value) {
      var url = cmr.CONTEXT_ROOT + '/code/lovslist.json';
      CmrGrid.refresh('lovUrlsGrid', url, 'fieldId=:fieldId&cmrIssuingCntry=:cmrIssuingCntry');
    });    
    FormManager.ready();
    FilteringDropdown.loadItems('cmrIssuingCntry', 'cmrIssuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');
  });
  
var LovService = (function() {
  return {
     addlov : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/lovsmain';
    },
    linkFormatter : function(value, rowIndex) {
      var id = this.grid.getItem(rowIndex).fieldId[0];
      var id1 =  this.grid.getItem(rowIndex).cmrIssuingCntry[0];
      var id3 =  this.grid.getItem(rowIndex).cd[0];
      
      if (id1 == '*' && !<%=admin%>){
        return value;
      } 
      return '<a href="javascript: LovService.open(\'' + id +'\',\''+id1+ '\',\''+id3+ '\')">' + value + '</a>';
    },
    open : function(value,value2, value3) {
      window.location = cmr.CONTEXT_ROOT + '/code/lovsmain?fieldId=' + encodeURIComponent(value)+'&cmrIssuingCntry='+encodeURIComponent(value2) +'&cd='+encodeURIComponent(value3);
    } 
  };
})();  
function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/lovs" name="frmCMRLov" class="ibm-column-form ibm-styled-form" modelAttribute="lovs">
    <cmr:section>
     <cmr:row topPad="8" addBackground="true">
        <cmr:column span="1">
          <p>
            <label>Field ID: </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <form:select dojoType="dijit.form.FilteringSelect" id="fieldId" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="fieldId" placeHolder="Field ID">
          </form:select>
        </cmr:column>
        <cmr:column span="3" width="400">
          for 
          <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCntry" searchAttr="name" style="display: inline-block;" maxHeight="200"
            required="false" path="cmrIssuingCntry" placeHolder="CMR Issuing Country">
          </form:select>
        </cmr:column>
      </cmr:row>      
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>LOV</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/lovslist.json" id="lovUrlsGrid" span="6" >
            <cmr:gridParam fieldId="fieldId" value=":fieldId" />
            <cmr:gridCol width="15%" field="fieldId" header="Field ID" >
              <cmr:formatter functionName="LovService.linkFormatter" />
            </cmr:gridCol>  
            <cmr:gridCol width="10%" field="cmrIssuingCntry" header="CMR Issuing Cntry"/>
            <cmr:gridCol width="6%" field="cd" header="Code"/>
            <cmr:gridCol width="13%" field="txt" header="Text"/>
            <cmr:gridCol width="10%" field="defaultInd" header="Default Indicator"/>
             <cmr:gridCol width="8%" field="dispOrder" header="Display Order"/>           
             <cmr:gridCol width="7%" field="dispType" header="Display Type"/>
             <cmr:gridCol width="10%" field="cmt" header="Comment"/>                
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
      </cmr:row>
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Add LOV" onClick="LovService.addlov()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="lovs" />