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
  DefaultApprovalModel model = (DefaultApprovalModel)request.getAttribute("appr");
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
<style>
div.approval-notif {
  border-radius : 5px;
  padding-left:20px;
  padding-top:5px;
  padding-bottom:3px;
  position:fixed;
  bottom:10px;
  right: 10px;
  width: 210px;
  min-height: 25px;
  font-size:13px;
  font-weight: bold;
  border: 1px Solid Black;
  background: rgb(169,219,128); /* Old browsers */
  background: -moz-linear-gradient(top, rgba(169,219,128,1) 0%, rgba(150,197,111,1) 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top, rgba(169,219,128,1) 0%,rgba(150,197,111,1) 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom, rgba(169,219,128,1) 0%,rgba(150,197,111,1) 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#a9db80', endColorstr='#96c56f',GradientType=0 ); /* IE6-9 */
  z-index:9999;
}
button {
  margin-left: 10px;
}
table.ibm-data-table {
  margin:10px !important;
  width:98% !important;
}
table.ibm-data-table caption {
  width:99% !important;
}
span.mail {
  font-size: 12px;
  color: #666;
  margin-left: 0 !important;
  display: inline !important;
}
</style>
<script>
  var _geoCodeHandler = null;
  var _typHandler = null;
  dojo.addOnLoad(function() {

//    var ids1 = cmr.query('SYSTEM.GET_AVAILABLE_GEOS', {_qall  : 'Y'});
//    var model1 = { 
//        identifier : "id", 
//        label : "name",
//        items : []
//    };
//    model1.items.push({id : 'WW', name : 'WW'});
//    for (var i =0; i < ids1.length; i++){
//      model1.items.push({id : ids1[i].ret1, name : ids1[i].ret1});
//    }
//    var dropdown1 = {
//        listItems : model1
//    };
//    FilteringDropdown.loadFixedItems('geoCd', null, dropdown1);
//    if (_geoCodeHandler == null) {
//      _geoCodeHandler = dojo.connect(FormManager.getField('geoCd'), 'onChange', function(value) {
//          console.log('change geo code');
//          refreshTypeList(FormManager.getField('geoCd'));
//      });
//    }

    if (_typHandler == null) {
      _geoCodeHandler = dojo.connect(FormManager.getField('typId'), 'onChange', function(value) {
        getApprovalTypeDesc();
      });
    }


    FilteringDropdown.loadItems('requestTyp', 'requestTyp_spinner', 'lov', 'fieldId=SearchRequestType');
    <%if (newEntry){%>
      FormManager.addValidator('requestTyp', Validators.REQUIRED, [ 'Request Type' ]);
      FormManager.addValidator('requestTyp', Validators.INVALID_VALUE, [ 'Request Type' ]);
    <%} else {%>
      FormManager.readOnly('requestTyp');
    <%}%>
    //FormManager.addValidator('geoCd', Validators.REQUIRED, [ 'GEO Code' ]);
    FormManager.addValidator('defaultApprovalDesc', Validators.REQUIRED, [ 'Description' ]);
    FormManager.addValidator('geoCd', Validators.INVALID_VALUE, [ 'GEO Code' ]);
    FormManager.addValidator('typId', Validators.REQUIRED, [ 'Approval Type' ]);
    FormManager.addValidator('typId', Validators.INVALID_VALUE, [ 'Approval Type' ]);
    //FormManager.addValidator('typId', Validators.DIGITS, [ 'Type ID' ]);

    FormManager.addValidator('approvalMailSubject', function(input) {
      var value = FormManager.getActualValue(input);
      var body = FormManager.getActualValue('approvalMailBody');
      if ((value && !body) || (!value && body)){
        return new ValidationResult(input, false, 'Both mail subject and body are required if one is specified.');
      }
      return new ValidationResult(input, true);
    }, [ 'Mail Subject' ]);
    
    FormManager.ready();
    refreshTypeList('XX');
    getApprovalTypeDesc();
  });
  
  function getApprovalTypeDesc(){
    var typeId = FormManager.getActualValue('typId');
    if (typeId){
      var q = cmr.query('APPROVAL.GET_TYPE_DESC', {TYP_ID : typeId});
      if (q){
        dojo.byId('defaultContent').innerHTML =  '[Default] '+q.ret1;
        dojo.byId('defaultSubject').innerHTML =  '[Default] Your Approval Needed: '+q.ret2+' - CMR Request 999999';
      }
    }
  }
    
  function refreshTypeList(geoCd){

    
    var ids1 = cmr.query('SYSTEM.GET_APPROVAL_TYPES', {_qall  : 'Y', CODE : geoCd});
    var model1 = { 
        identifier : "id", 
        label : "name",
        items : []
    };
    //model1.items.push({id : 'WW', name : 'WW'});
    for (var i =0; i < ids1.length; i++){
      model1.items.push({id : ids1[i].ret1, name : ids1[i].ret2});
    }
    var dropdown1 = {
        listItems : model1
    };
    FilteringDropdown.loadFixedItems('typId', null, dropdown1);
    if (ids1.length == 0){
      FormManager.setValue('typId','');
    }
  }
  function backToList(){
    window.location  = cmr.CONTEXT_ROOT+'/code/defaultappr';
  }
  
  function saveApproval(){
    FormManager.save('frmCMR');
  }
</script>
<cmr:boxContent>
  <cmr:tabs />

 <cmr:section>
  <cmr:form method="POST" action="${contextPath}/code/defaultapprdetails" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="appr" id="frmCMR">
    <%if (!newEntry){%>
      <form:hidden path="createTs"/>
      <form:hidden path="lastUpdtTs"/>
    <%}%>
    <cmr:modelAction formName="frmCMR" />
      <cmr:row>
        <cmr:column span="6">
          <h3>Default Approval Details</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="defaultApprovalId">ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
           <%if (!newEntry){%>
                ${appr.defaultApprovalId}
           <%} else { %>
                Not Assigned
           <%} %>
            <form:hidden path="defaultApprovalId" id="defaultApprovalId"/>
          </p>
        </cmr:column>

        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="requestTyp">Request Type : </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="requestTyp" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="requestTyp" placeHolder="Select Request Type">
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="defaultApprovalDesc">Description: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="5" width="700">
          <p>
            <form:input path="defaultApprovalDesc" dojoType="dijit.form.TextBox" placeHolder="Type ID" cssStyle="width:650px"/>
          </p>
        </cmr:column>
      </cmr:row>
      <%-- 
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="geoCd">GEO Code : </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="geoCd" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="geoCd" placeHolder="Select GEO Code">
          </form:select>
          </p>
        </cmr:column>
        
      </cmr:row>
      --%>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="typId">Approval Type: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="5" width="700">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="typId" searchAttr="name" style="display: block;width:650px" maxHeight="200"
            required="false" path="typId" placeHolder="Select Approval Type">
          </form:select>
          <form:hidden path="geoCd"/>
          </p>
        </cmr:column>

      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="createBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            ${appr.createBy}
            <form:hidden path="createBy"/>
          </p>
        </cmr:column>

        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="lastUpdtBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            ${appr.lastUpdtBy}
            <form:hidden path="lastUpdtBy"/>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="createBy">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <%=createDate%>
         </p>
        </cmr:column>

        <cmr:column span="1" width="130">
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
      
      <!-- Subject / Content override -->
      <cmr:row addBackground="true" topPad="10">
        <cmr:column span="1" width="130">
          <h3>Mail Content</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="6">
          <cmr:note text="By default, approvals mails are sent in the format 'Your Approval Needed: (approval type) - CMR Request XXXXXX'. You can override the subject and first line of the mail using the fields below."></cmr:note>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="approvalMailSubject">Mail Subject: 
              <cmr:info text="Put here the short, high-level description of the approval, like 'Embargo Code Removal'"></cmr:info>
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="5">
          <p>
            <span class="mail">Your Approval Needed:</span>
              <form:input path="approvalMailSubject" dojoType="dijit.form.TextBox" placeHolder="Mail Subject" cssStyle="width:400px" maxlength="50"/>
            <span class="mail">- CMR Request 999999</span>
            <br>
            <span class="mail" id="defaultSubject" style="font-style:italic"></span>
         </p>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="approvalMailBody">Mail Content: 
              <cmr:info text="The first line of the mail body. Place here a short sentence to describe what the approval is about, like 'An approval is requested from you because of potential DPL matches.' By default it follows the content on the approval type."></cmr:info>
            </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="5">
          <p>
            <form:input path="approvalMailBody" dojoType="dijit.form.TextBox" placeHolder="Mail Body" cssStyle="width:650px" maxlength="190"/>
            <br>
            <span class="mail" id="defaultContent" style="font-style:italic"></span>
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        &nbsp;
      </cmr:row>
      <cmr:row>
        <p>
          <cmr:button label="Save" onClick="saveApproval()" highlight="true"/>
        </p>
      </cmr:row>
  </cmr:form>
  <cmr:model model="appr" />
<%if (!newEntry){%>

<div ng-app="DefaultApprovals">
     <cmr:row topPad="30">
       <div ng-controller="Recipients">
            <table  id="recipientsTable" cellspacing="0" cellpadding="0" border="0" summary="Recipients" class="ibm-data-table ibm-sortable-table ibm-alternating">
              <caption>
                <em>Recipients</em>
              </caption>
              <thead>
                <tr>
                  <th scope="col" width="*">Name</th>
                  <th scope="col" width="29%%">Intranet ID</th>
                  <th scope="col" width="29%">Notes ID</th>
                  <th scope="col" width="8%">&nbsp;</th>
                </tr>
              </thead>
              <tbody>
                <tr ng-show="recipients.length == 0">
                  <td colspan="4">{{loading ? 'Loading recipients...' : 'No recipients found.'}}</td>
                </tr>
                <tr ng-repeat="recipient in recipients">
                  
                  <td ng-show="!recipient.newEntry">
                    {{recipient.intranetId != 'BPMANAGER' ? recipient.displayName : 'Blue Pages Manager of Requester'}}
                  </td>
                  <td ng-show="!recipient.newEntry">
                    {{recipient.intranetId != 'BPMANAGER' ? recipient.intranetId : '(To Be Assigned)'}}
                  </td>
                  <td ng-show="!recipient.newEntry">
                    {{recipient.notesId != 'BPMANAGER' ? recipient.notesId : '(To Be Assigned)'}}
                   </td>
                  <td ng-show="!recipient.newEntry"><a style="cursor:pointer" ng-click="removeRecipient(recipient)">Remove</a>

                  <td ng-show="recipient.newEntry">
                    <input ng-model="recipient.displayName" id="recipient_{{recipient.seq}}" bpid="recipientId_{{recipient.seq}}" style="width:250px" onchange="bpOnChange(this)" maxlength="120">
                    <img class="cmr-w3-icon" src="${resourcesPath}/images/w3.ico" title="Enter a name to query BluePages">
                    <input id="recipient_{{recipient.seq}}_bpcont" type="hidden">
                  </td>
                  <td ng-show="recipient.newEntry">
                    <input ng-model="recipient.intranetId" ng-readonly="recipient.newEntry" id="recipientId_{{recipient.seq}}"  style="width:180px">
                  </td>
                  <td ng-show="recipient.newEntry">
                    <input ng-model="recipient.notesId" ng-readonly="recipient.newEntry" id="recipientNotesId_{{recipient.seq}}"  style="width:220px">
                  </td>
                  <td ng-show="recipient.newEntry"><a style="cursor:pointer" ng-click="saveRecipient(recipient)">Save</a>

                </tr>
              </tbody>
          </table>
           <button class="listbutton" ng-click="addRecipient()">Add Recipient</button>
           <button class="listbutton" ng-click="addBPManager()">Add Blue Pages Manager of Requester</button>
       </div>
     </cmr:row>
     <div ng-controller="Conditions">
       <div ng-init="addressVars = [{addr : false, conditions : nonAddrConditions},{addr: true, conditions : addrConditions}]">
         <div ng-repeat="address in addressVars" class="ibm-columns" style="padding-top:30px">
            <table  id="conditionsTable{{address.addr ? 'Y' : 'N'}}" cellspacing="0" cellpadding="0" border="0" summary="Conditions" class="ibm-data-table ibm-sortable-table">
              <caption>
                <em>{{address.addr? "Address Conditions" : "Non-Address Conditions"}}</em>
              </caption>
              <thead>
                <tr>
                  <th scope="col" width="15%">Level</th>
                  <th scope="col" width="20%">Field</th>
                  <th scope="col" width="10%">Operator</th>
                  <th scope="col" width="*">Value</th>
                  <th scope="col" width="10%">Prev?</th>
                  <th scope="col" width="10%">&nbsp;</th>
                </tr>
              </thead>
              <tbody>
                <tr ng-show="address.conditions.length == 0">
                  <td ng-show="!adddress.addr" colspan="6">{{loadingA ? 'Loading non-address conditions...' : 'No conditions found.'}}</td>
                  <td ng-show="adddress.addr" colspan="6">{{loadingB ? 'Loading address conditions...' : 'No conditions found.'}}</td>
                </tr>
                <tr ng-repeat="condition in address.conditions" class="{{condition.conditionLevel%2 == 1 ? 'ibm-alt-row' : ''}}">
                  <td>
                    <span ng-show="!condition.edit || condition.field.name == 'CMR_ISSUING_CNTRY'">
                      {{condition.conditionLevel}}&nbsp;
                      <a ng-show="condition.field.name != 'CMR_ISSUING_CNTRY'" style="cursor:pointer" ng-click="addCondition(condition.sequenceNo, condition.conditionLevel, address.addr)">Add to Level</a>
                    </span>
                    <span ng-show="condition.edit && condition.field.name != 'CMR_ISSUING_CNTRY'">
                      <input ng-model="condition.conditionLevel" style="width:60px" numbersonly maxlength="2">
                    </span>
                  </td>
                  <td>
                    <span ng-show="condition.edit && condition.field.name != 'CMR_ISSUING_CNTRY'">
                      <select ng-show="!address.addr" style="width:200px" ng-model="condition.field" ng-options="option.id for option in dataFields track by option.name">
                      </select>
                      <select ng-show="address.addr" style="width:200px" ng-model="condition.field" ng-options="option.id for option in addrFields track by option.name">
                      </select>
                    </span>
                    <span ng-show="!condition.edit || condition.field.name == 'CMR_ISSUING_CNTRY'">
                      <span class="cmr-grid-tooltip" style="cursor:help" title="{{condition.field.name}}">{{condition.field.id}}</span>
                    </span>
                  </td>
                  <td ng-show="condition.edit">
                    <select ng-model="condition.operator">
                      <option value="EQ">=</option>
                      <option value="LT">&lt;</option>
                      <option value="LTE">&lt;=</option>
                      <option value="GT">&gt;</option>
                      <option value="GTE">&gt;=</option>
                      <option value="NE">&lt;&gt;</option>
                      <option value="*">(any)</option>
                      <option value="$">is blank</option>
                      <option value="IN">in</option>
                      <option value="NIN">not in</option>
                      <option value="CON">contains</option>
                      <option value="NCO">doesn't contain</option>
                      <option value="STA">start with</option>
                      <option ng-show="requestType == 'U'" value="CHG">is changed</option>
                    </select>
                  </td>
                  <td ng-switch="condition.operator" ng-show="!condition.edit">
                    <span ng-switch-when="EQ">=</span>
                    <span ng-switch-when="LT">&lt;</span>
                    <span ng-switch-when="LTE">&lt;=</span>
                    <span ng-switch-when="GT">&gt;</span>
                    <span ng-switch-when="GTE">&gt;=</span>
                    <span ng-switch-when="NE">&lt;&gt;</span>
                    <span ng-switch-when="*">(any)</span>
                    <span ng-switch-when="$">is blank</span>
                    <span ng-switch-when="IN">in</span>
                    <span ng-switch-when="NIN">not in</span>
                    <span ng-switch-when="CON">contains</span>
                    <span ng-switch-when="NCO">doesn't contain</span>
                    <span ng-switch-when="STA">start with</span>
                    <span ng-switch-when="CHG">is changed</span>
                  </td>
                  <td>
                    <span ng-show="condition.edit">
                      <input ng-model="condition.value" ng-readonly="condition.operator == '$' || condition.operator == '*' || condition.operator == 'CHG'" style="width:220px">
                    </span>
                    <span ng-show="!condition.edit">
                      {{condition.value}}
                    </span>
                  </td>
                  <td>
                    <span ng-show="condition.edit">
                      <c:if test="${appr.requestTyp == 'U'}">
                      <select ng-model="condition.previousValueIndc">
                        <option value="Y">Yes</option>
                        <option value="">No</option>
                      </select>
                      </c:if>
                    </span>
                    <span ng-show="!condition.edit">
                      {{condition.previousValueIndc == 'Y'? 'Yes' : ''}}
                    </span>
                  </td>
                  <td>
                    <a ng-show="!condition.edit" style="cursor:pointer" ng-click="editCondition(condition)">Edit</a>
                    <a ng-show="!condition.edit && condition.field.name != 'CMR_ISSUING_CNTRY'" style="cursor:pointer" ng-click="removeCondition(condition, address.addr)">Remove</a>

                    <a ng-show="condition.edit" style="cursor:pointer" ng-click="saveCondition(condition,  address.addr)">Save</a>
                    <a ng-show="condition.edit && condition.newEntry" style="cursor:pointer" ng-click="cancelCondition(condition,  address.addr)">Cancel</a>
                  </td>
                </tr>
              </tbody>
          </table>
            <button ng-show="(requestType != 'E' && requstType != 'R' && requestType != 'D') || ((requestType == 'E' || requestType == 'D' || requestType == 'R') && !address.addr)" ng-click="addNewCondition(address.addr)">Add {{address.addr ? 'Address' : 'Non-Address'}} Condition</button>
         </div>
       </div>
     <cmr:row topPad="30">
       <div ng-controller="Preview" style="padding:10px; margin:10px; border: 1px Solid Gray; border-radius:10px" title="Evaluation Preview">
         <table ng-show="showPrev && recipients.length > 0" style="margin-left:20px; margin-bottom:20px" ng-init="multiplesAddedA = []">
           <tr>
             <td colspan="3">
               <div ng-show="showPrev && reqType != 'M' && reqType != 'N'">
                 If the 
                   <span ng-switch="reqType">
                     <strong>
                       <span ng-switch-when="U">Update</span>
                       <span ng-switch-when="C">Create</span>
                       <span ng-switch-when="R">Reactivate</span>
                       <span ng-switch-when="D">Delete</span>
                       <span ng-switch-when="E">Update by Enterprise #</span>
                     </strong>
                   </span>
                 request has 
               </div>
               <div ng-show="showPrev && (reqType == 'N' || reqType == 'M')">
                 If the <strong> {{reqType == 'M' ? 'Mass Update' : 'Mass Create'}} </strong> request has any row that has
               </div>
             </td>
             <td>
           </tr>
           <tr ng-repeat="condition in dataConditions" ng-init="multiple = multipleDataCondLevels.indexOf(condition.conditionLevel) >= 0">
             <td width="20">&nbsp;</td>
             <td width="65" ng-init="multiple && multiplesAddedA.indexOf(condition.conditionLevel) < 0 ? multiplesAddedA.push(condition.conditionLevel) : skip = true">
               <span ng-show="!multiple || !skip">
                {{condition.conditionLevel == 1 ? '' : 'and '}} {{multiple ? ' either ' : ''}}
               </span>
             </td>
             <td width="500">
                 <span ng-show="multiple && skip">
                   or
                 </span>
                  {{condition.field.id}}
                  <span ng-switch="condition.operator">
                    <span ng-switch-when="EQ">=</span>
                    <span ng-switch-when="LT">&lt;</span>
                    <span ng-switch-when="LTE">&lt;=</span>
                    <span ng-switch-when="GT">&gt;</span>
                    <span ng-switch-when="GTE">&gt;=</span>
                    <span ng-switch-when="NE">&lt;&gt;</span>
                    <span ng-switch-when="*">with any value</span>
                    <span ng-switch-when="$">is blank</span>
                    <span ng-switch-when="IN">with value of either one of</span>
                    <span ng-switch-when="NIN">with value not in the list</span>
                    <span ng-switch-when="CON">that contains</span>
                    <span ng-switch-when="NCO">that doesn't contain</span>
                    <span ng-switch-when="STA">that start with</span>
                    <span ng-switch-when="CHG">that has been updated</span>
                    </span>
              {{condition.value}}
              {{condition.previousValueIndc == 'Y' ? (condition.operator == 'CHG' ? ' from ' : 'on')+' the original record ' : ''}}
             </td>    
           </tr>
         </table>
         <table ng-show="showPrev && addrConditions.length > 0  && recipients.length > 0" style="margin-left:20px; margin-bottom:20px" ng-init="multiplesAddedB = []">
           <tr>
             <td colspan="3">
               <div ng-show="showPrev && reqType != 'M' && reqType != 'N'">
                 <strong>AND</strong> has any address record with 
               </div>
               <div ng-show="showPrev && (reqType == 'N' || reqType == 'M')">
                 <strong>AND</strong> the row has any address record with 
               </div>
             </td>
             <td>
           </tr>
           <tr ng-repeat="condition in addrConditions" ng-init="multiple = multipleAddrCondLevels.indexOf(condition.conditionLevel) >= 0">
             <td width="20">&nbsp;</td>
             <td width="65" ng-init="multiple && multiplesAddedB.indexOf(condition.conditionLevel) < 0 ? multiplesAddedB.push(condition.conditionLevel) : skip = true">
               <span ng-show="!multiple || !skip">
               {{condition.conditionLevel == 1 ? '' : 'and '}} {{multiple ? ' either ' : ''}}
               </span>
             </td>
             <td width="500">
                 <span ng-show="multiple && skip">
                   or
                 </span>
                  {{condition.field.id}}
                  <span ng-switch="condition.operator">
                    <span ng-switch-when="EQ">=</span>
                    <span ng-switch-when="LT">&lt;</span>
                    <span ng-switch-when="LTE">&lt;=</span>
                    <span ng-switch-when="GT">&gt;</span>
                    <span ng-switch-when="GTE">&gt;=</span>
                    <span ng-switch-when="NE">&lt;&gt;</span>
                    <span ng-switch-when="*">with any value</span>
                    <span ng-switch-when="$">is blank</span>
                    <span ng-switch-when="IN">with value of either one of</span>
                    <span ng-switch-when="NIN">with value not in the list</span>
                    <span ng-switch-when="CON">that contains</span>
                    <span ng-switch-when="NCO">that doesn't contain</span>
                    <span ng-switch-when="STA">that start with</span>
                    <span ng-switch-when="CHG">that has been updated</span>
                    </span>
              {{condition.value}}
              {{condition.previousValueIndc == 'Y' ? (condition.operator == 'CHG' ? ' from ' : 'on')+' the original record ' : ''}}
             </td>    
           </tr>
         </table>
         <table ng-show="showPrev && recipients.length > 0" style="margin-left:20px; margin-bottom:20px">
           <tr>
             <td colspan="2">Then an approval request will {{recipients.length > 1 ? ' each ' : ''}} be sent to:</td>
           </tr>
           <tr ng-repeat="recipient in recipients">
             <td width="20">&nbsp;</td>
             <td>{{recipient.displayName}}  ({{recipient.intranetId}})</td>
           </tr>
         </table>
         <table ng-show="showPrev  && recipients.length == 0" style="margin-left:20px; margin-bottom:20px">
           <tr>
             <td><strong>This approval will not be generated because there are no recipients.</strong></td>
           </tr>
         </table>
         <input type="button" ng-click="showPreview()" value="{{showPrev ? 'Update Preview' : 'Preview Evaluation'}}">
         <input ng-show="showPrev" type="button" ng-click="hidePreview()" value="Hide Preview">
       </div>
     </cmr:row>
     </div>
<%}%>
     <cmr:row>
       &nbsp;
     </cmr:row>
   </cmr:section>
  </cmr:boxContent>
</div>
  <cmr:section alwaysShown="true">
    <cmr:buttonsRow>
      <cmr:button label="Back to Default Approvals List" onClick="backToList()" />
    </cmr:buttonsRow>
  </cmr:section>
<script src="${resourcesPath}/js/system/defaultappr.js?${cmrv}"></script>
<div class="approval-notif" id="approval-notif" style="display:none"> 
  Process completed successfully.
</div>