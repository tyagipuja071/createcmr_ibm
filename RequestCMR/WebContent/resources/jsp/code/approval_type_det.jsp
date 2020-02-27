<%@page import="com.ibm.cio.cmr.request.model.approval.ApprovalTypeModel"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.DefaultApprovalModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/system/dbmap.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<%
  ApprovalTypeModel model = (ApprovalTypeModel)request.getAttribute("typ");
  boolean newEntry = BaseModel.STATE_NEW == model.getState();
  SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  String createDate = "-";
  if (model.getCreateTs() != null){
    createDate = formatter.format(model.getCreateTs());
  }
  String updateDate = "-";
  if (model.getLastUpdtTs() != null){
    updateDate = formatter.format(model.getLastUpdtTs());
  }
%>
<script>
  dojo.addOnLoad(function() {

    FormManager.addValidator('description', Validators.REQUIRED, [ 'Mail Content' ]);
    FormManager.addValidator('title', Validators.REQUIRED, [ 'Title / Mail Subject' ]);
    FormManager.addValidator('templateName', Validators.REQUIRED, [ 'Mail Template' ]);
    FormManager.addValidator('grpApprovalIndc', Validators.REQUIRED, [ 'Group Approval' ]);
    
    <%if (!newEntry){%>
      FormManager.readOnly('geoCd');
    <%}%>
    FormManager.ready();
  });
  function saveApproval(){
    FormManager.save('frmCMR');
  }  
  function deleteApproval(){
    FormManager.remove('frmCMR');
  }  
  function backToList(){
    window.location  = cmr.CONTEXT_ROOT+'/code/approval_types';
  }
</script>
<cmr:boxContent>
  <cmr:tabs />

 <cmr:section>
  <form:form method="POST" action="${contextPath}/code/approval_type_details" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="typ" id="frmCMR">
    <%if (!newEntry){%>
      <form:hidden path="createTs"/>
      <form:hidden path="lastUpdtTs"/>
    <%}%>
    <cmr:modelAction formName="frmCMR" />
      <cmr:row>
        <cmr:column span="6">
          <h3>Approval Type Details</h3>
        </cmr:column>
      </cmr:row>
      <%if (!newEntry){%>
       <cmr:row>
          <cmr:column span="6">
            <div class="embargo1">
              <img src="${resourcesPath}/images/warn-icon.png" class="cmr-error-icon">
              <cmr:note text="Modifying details of this type will affect all existing approval requests and default approval configurations." />
            </div>
          </cmr:column>
      </cmr:row>
      <%} %>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="typId">ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
           <%if (!newEntry){%>
                ${typ.typId}
           <%} else { %>
                Not Assigned
           <%} %>
            <form:hidden path="typId" id="typId"/>
          </p>
        </cmr:column>

        <cmr:column span="1" width="70">
          <p>
            <cmr:label fieldId="geoCd">GEO: 
             <cmr:info text="GEO is used for tagging the type only and does not in any way affect the content or process of approvals." />
            </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="geoCd" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="geoCd" placeHolder="Select GEO">
              <form:option value="WW">Worldwide</form:option>
              <form:option value="AP">AP</form:option>
              <form:option value="CND">CND</form:option>
              <form:option value="EMEA">EMEA</form:option>
              <form:option value="LA">LA</form:option>
              <form:option value="JP">Japan</form:option>
              <form:option value="CN">China</form:option>
              <form:option value="US">United States</form:option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="title">Title / Mail Subject: 
            <cmr:info text="The title of the approval. This title will also be included in the notifications sent out to approvers."></cmr:info>
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="5" width="700">
          <p>
            <form:input path="title" placeHolder="Title / Mail Subject" dojoType="dijit.form.TextBox" cssStyle="width:650px" maxlength="60" />
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="description">Mail Content: 
            <cmr:info text="The main content of the email sent out for this type. This text appears as the first line on the email, before the request details."></cmr:info>
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="5" width="700">
          <p>
            <form:textarea path="description" id="description" rows="5" cols="30" placeHolder="Mail Content" cssStyle="width:650px"/>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="templateName">Mail Template: 
            <cmr:info text="The template for the mail to use. All request attachments and the full request details will still be sent to approvers regardless of the template."></cmr:info>
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="templateName" searchAttr="name" style="display: block;" maxHeight="200"
              required="false" path="templateName" placeHolder="Select Template">
              <form:option value="WW/general.html">General</form:option>
              <form:option value="WW/namechange.html">Name Change</form:option>
              <form:option value="WW/sic.html">SIC Change</form:option>
              <form:option value="WW/accounts.html">Accounts Receivable</form:option>
              <form:option value="WW/ent_affiliate.html">Enterprise/Affiliate Change</form:option>
              <form:option value="WW/masschange.html">Generic Mass Change</form:option>
              <form:option value="AP/dpl.html">DPL Check</form:option>
              <form:option value="AP/ero.html">ERO (Proliferation Checklist)</form:option>
              <form:option value="US/arbo.html">ARBO Update</form:option>
              <form:option value="US/bos_update.html">BO Update</form:option>
              <form:option value="US/create_grnt.html">US - Create GRNTS</form:option>
              <form:option value="US/create_nonus.html">US - Create Non-US</form:option>
              <form:option value="US/create_ssi.html">US - Create SSI</form:option>
              <form:option value="US/create_svmp.html">US - Create SVMP</form:option>
              <form:option value="US/delete.html">US - Delete/Reactivate</form:option>
              <form:option value="US/remove_cod.html">US - Remove COD</form:option>
              <form:option value="US/remove_po.html">US - Remove PO</form:option>
              <form:option value="US/updt_restrictions.html">US - Update Restrictions</form:option>
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="grpApprovalIndc">Group Approval: 
            <cmr:info text="Indicates whether this type of approval will require only one approval from any approver or need all individual approvals."></cmr:info>
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="grpApprovalIndc" searchAttr="name" style="display: block; width:500px" maxHeight="200"
            required="false" path="grpApprovalIndc" placeHolder="Group Approval">
              <form:option value="N">No, all individual approval requests should be approved.</form:option>
              <form:option value="Y">Yes, an approval from one approver causes the others to be approved as well.</form:option>
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>

<%if (!newEntry){%>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="createBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            ${typ.createBy}
            <form:hidden path="createBy"/>
          </p>
        </cmr:column>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="lastUpdtBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            ${typ.lastUpdtBy}
            <form:hidden path="lastUpdtBy"/>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="createBy">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <%=createDate%>
         </p>
        </cmr:column>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="lastUpdtBy">Last Update Date: </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            <%=updateDate%>
          </p>
        </cmr:column>
      </cmr:row>
<%}%>
      <cmr:row>
        &nbsp;
      </cmr:row>
      <cmr:row>
        <cmr:column span="6">
          <cmr:button label="Save" onClick="saveApproval()" highlight="true"/>
          <cmr:button label="Delete" onClick="deleteApproval()" pad="true"/>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        &nbsp;
      </cmr:row>
  </form:form>
  <cmr:model model="typ" />
</cmr:section>
</cmr:boxContent>
  <cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <cmr:button label="Back to Approval Types List" onClick="backToList()" />
    </cmr:buttonsRow>
  </cmr:section>
