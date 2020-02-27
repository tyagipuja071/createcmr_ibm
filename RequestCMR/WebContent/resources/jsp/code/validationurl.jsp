<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.ValidationUrlModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  ValidationUrlModel validationurl = (ValidationUrlModel) request
					.getAttribute("validationurl");
			boolean newEntry = false;
			if (validationurl.getState() == BaseModel.STATE_NEW) {
				newEntry = true;
        validationurl.setDisplaySeqNo(1);
			}
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
<%if (newEntry) {%>
  //FormManager.addValidator('displaySeqNo', Validators.REQUIRED, [ 'Display Sequence No.' ]);

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
    FilteringDropdown.loadFixedItems('cntryCd', null, dropdown1);

    FormManager.addValidator('cntryCd', Validators.REQUIRED, [ 'Country Code' ]);
    FormManager.addValidator('cntryCd', Validators.INVALID_VALUE, [ 'Country Code' ]);
<%}%>
  FormManager.addValidator('url', Validators.REQUIRED, [ 'URL' ]);
    FormManager.addValidator('descrTxt', Validators.REQUIRED, [ 'Description Text' ]);
    FormManager.ready();
  });

  var ValUrlService = (function() {
    return {
      saveValUrl : function(typeflag) {
        var displaySeqNo = FormManager.getActualValue('displaySeqNo');
        var cntryCd = FormManager.getActualValue('cntryCd');
/*        if (displaySeqNo == '0') {
          cmr.showAlert('Display sequence number can not be 0.');
          return;
        }
        if (typeflag) {
          var check = cmr.query('CHECKVALIDATION_URL', {
            DISPLAY_SEQ_NO : displaySeqNo,
            CNTRY_CD : cntryCd
          });
          if (check && check.ret1 == '1') {
            cmr.showAlert('This Validation Url already exists in the system.');
            return;
          }
        }*/
        FormManager.save('frmCMR');
      },
      removeURL : function(){
        FormManager.remove('frmCMR');
      }
    };
  })();
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/validationurlsmain" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="validationurl">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Validation Url" : "Update Validation Url"%></h3>
        </cmr:column>
      </cmr:row>

      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="displaySeqNo">Sequence No.: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${validationurl.displaySeqNo}</p>
          <form:hidden id="displaySeqNo" path="displaySeqNo" />
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="cntryCd">Country Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${validationurl.cntryCd}</p>
          <form:hidden id="cntryCd" path="cntryCd" />
        </cmr:column>
      </cmr:row>

      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="displaySeqNo">Sequence No.: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            (Unassigned)
            <form:hidden path="displaySeqNo" />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="cntryCd">Country Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="cntryCd" searchAttr="name" style="display: block;width:400px" maxHeight="200"
              required="false" path="cntryCd" placeHolder="Select Country Code">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <%
        }
      %>

      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="url">URL: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="url" dojoType="dijit.form.TextBox" cssStyle="width:600px" />
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="descrTxt">Description: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="descrTxt" dojoType="dijit.form.TextBox" cssStyle="width:600px" />
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="140">
          <p>
            <cmr:label fieldId="comments">Comments: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="comments" dojoType="dijit.form.TextBox" cssStyle="width:600px" />
          </p>
        </cmr:column>
      </cmr:row>
      <form:hidden id="createTs" path="createTs" />
      <form:hidden id="updtTs" path="updtTs" />
      <form:hidden id="createBy" path="createBy" />
      <form:hidden id="updtBy" path="updtBy" />

    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%
      if (newEntry) {
    %>
    <cmr:button label="Save" onClick="ValUrlService.saveValUrl(true)" highlight="true" />
    <%
      } else {
    %>
    <cmr:button label="Save" onClick="ValUrlService.saveValUrl(false)" highlight="true" />
    <cmr:button label="Delete" onClick="ValUrlService.removeURL()" pad="true" />
    <%
      }
    %>
    <cmr:button label="Back to Validation Url List" onClick="window.location = '${contextPath}/code/validationurls'" pad="true" />
  </cmr:buttonsRow>
  <br>

</cmr:section>
<cmr:model model="validationurl" />