<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<script>

function rejectSearch(){
  cmr.showModal('crisReject');
}

function proceedWithReject(){
  var choice = FormManager.getActualValue('crisReject');
  if (choice == ''){
    cmr.showAlert('Please select a reason for rejecting the result.')
    return;
  }
  var cmt = FormManager.getActualValue('crisRejectCmt');
  if (cmt == ''){
    cmr.showAlert('Please input comments.')
    return;
  }
  cmt = cmt.trim();
  if (cmt.length > 250){
    cmr.showAlert('Please specify up to 250 characters only for your comment. Current length is '+cmt.length);
    return;
  }
  var result = {
      accepted : 'n',
      reject : true,
      comment : cmt,
      reason : choice
   };
   if (window.opener){
     window.opener.cmr.hideProgress();
     window.opener.doImportCRISRecord(result);
     WindowMgr.closeMe();    
   }

}
</script>

<cmr:modal title="Please specify why you want to reject the results" id="crisReject" widthId="570">
  <cmr:row>
    <cmr:column span="6" width="550">
      <input type="radio" name="crisReject" value="Correct customer not found" checked>
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        Correct customer not found
      </cmr:label>
      <br>
      <input type="radio" name="crisReject" value="Correct name but wrong address">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        Correct name but wrong address
      </cmr:label>
      <br>
      <input type="radio" name="crisReject" value="Wrong coverage">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        Wrong coverage
      </cmr:label>
      <br>
      <input type="radio" name="crisReject" value="Exceptional approval">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        Exceptional approval
      </cmr:label>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="1" width="100">
      <cmr:label fieldId="" >
        Comments <span style="color:red">*</span>:
      </cmr:label>
    </cmr:column>
    <cmr:column span="3">
      <textarea name="crisRejectCmt" id="crisRejectCmt" rows="5" cols="30"></textarea>
    </cmr:column>
    
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="Reject Results" onClick="proceedWithReject()" highlight="true" pad="true" />
    <cmr:button label="Cancel" onClick="cmr.hideModal('crisReject')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>
