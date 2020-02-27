<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
  FormManager.ready();
  });
  
var ValUrlService = (function() {
  return {
    descFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var displaySeqNo = rowData.displaySeqNo;
      var cntryCd = rowData.cntryCd;
      var descrTxt = rowData.descrTxt;

      return '<a href="javascript: ValUrlService.open(\'' + displaySeqNo + '\', \'' + cntryCd + '\')">' + descrTxt + '</a>';
    },
    cntryCdFormatter : function(value, rowIndex) {
      return 
      var rowData = this.grid.getItem(rowIndex);
      var cntryCd = rowData.cntryCd;
      if (cntryCd == '000') {
        return 'WW';
      } else {
        return cntryCd;
      }
    },
    open : function(displaySeqNo, cntryCd) {
      document.forms['frmCMRVal'].setAttribute('action', cmr.CONTEXT_ROOT + '/code/validationurlsmain/?displaySeqNo=' + displaySeqNo + '&cntryCd=' + cntryCd);
      document.forms['frmCMRVal'].submit();    
    },
    addValUrl : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/validationurlsmain';
    }
  };
})();  
function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/validationurls" name="frmCMRVal" class="ibm-column-form ibm-styled-form" modelAttribute="validationurls">
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Validation URLs</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/validationurlslist.json" id="validationUrlsGrid" span="6" useFilter="true">
            <cmr:gridCol width="5%" field="displaySeqNo" header="Seq No." />
            <cmr:gridCol width="13%" field="comments" header="Type" />
            <cmr:gridCol width="28%" field="url" header="URL"/>
            <cmr:gridCol width="28%" field="descrTxt" header="Description">
              <cmr:formatter functionName="ValUrlService.descFormatter" />
            </cmr:gridCol>            
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
    <cmr:button label="Add Validation URL" onClick="ValUrlService.addValUrl()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="validationurls" />