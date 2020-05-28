<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<script src="${resourcesPath}/js/system/system.js?${cmrv}"></script>
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<link rel="stylesheet" href="${resourcesPath}/css/auto_config.css?${cmrv}"/>
<style>

</style>
<div ng-app="ConfigApp" ng-controller="ExceptionsController" ng-cloak>
<cmr:boxContent>
  <cmr:tabs />
  
  <cmr:section>
    <div  class="exc">
    <cmr:row>
      <cmr:column span="6">
        <h3>Scenario Exceptions</h3>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <div class="exc-label">Country:</div>
      </cmr:column>
      <cmr:column span="4">
        <select ng-model="currCountry" ng-disabled="configuring" ng-class="{'exc-ro' : configuring}" placeholder="Scenario Type">
          <option ng-repeat="country in countries" value="{{country.id}}">{{country.name}}</option>
        </select>
        <input type="button" value="Configure Country" ng-show="!configuring" ng-click="configure(true)" placeholder="Scenario SUb-type">
        <input type="button" value="Select Another Country" ng-show="configuring" ng-click="configure(false)">
        <a ng-show="configuring && viewMode == 'N'" ng-click="viewMode = 'C'" class="view-mode">Switch to Compact View</a>
        <a ng-show="configuring && viewMode == 'C'" ng-click="viewMode = 'N'" class="view-mode">Switch to Normal View</a>
      </cmr:column>
    </cmr:row>
    
    
    <div ng-show="configuring" ng-class="exc-config-table">
      <cmr:row topPad="10">
        <cmr:column span="1">
          <div class="exc-label">Scenario:</div>
        </cmr:column>
        <cmr:column span="4">
          <select ng-model="currScenario" ng-change="getSubScenarios()">
            <option ng-repeat="scenario in mainScenarios" value="{{scenario.id}}">{{scenario.name}}</option>
          </select>
          <select ng-model="currSubScenario">
            <option ng-repeat="subScenario in subScenarios" value="{{subScenario.id}}">{{subScenario.name}}</option>
          </select>
          <input type="button" value="Add Exception" ng-click="addException()">
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
        <cmr:column span="6">
          <div class="exc-note">Note: When the (default) option is chosen, the behavior for the scenario follows the value directly specified 
               in the parent scenario exception, or the global defaults when no parent scenario is configured.</div>
        </cmr:column>
      </cmr:row>
      <div  ng-show="viewMode == 'C'">
        <cmr:row topPad="5">
          <cmr:column span="6">
            <div class="exc-note"><strong>Note: The compact view is intended for administrators who are familiar with the exceptions settings. Proceed with caution.</strong></div>
          </cmr:column>
        </cmr:row>
      </div>
      
      <div ng-show="configuring && (!exceptions || exceptions.length == 0)" class="exc-cont">
        <cmr:row topPad="10">
          <div class="exc-none">
            No defined exceptions
          </div>
        </cmr:row>
      </div>
      <div ng-show="viewMode == 'C'">
        <div ng-show="exceptions && exceptions.length > 0" style="margin-top:20px">
          <div ng-repeat="cat in categories">
            <div class="com-cat">{{cat}}</div>
            <div class="com-exc" ng-repeat="exc in exceptions" ng-show="exc.typeDesc == cat" ng-class="{'exc-new2' : exc.status == 'N' || exc.status == 'M'}">
              <div class="com-exc-header">
                 {{exc.subTypeDesc}}
              </div>
              <div class="com-exc-cont">
                <table class="com-exc-table" cellspacing="0" cellpadding="0" width="100%">
                  <tr>
                    <th width="50%">Duplicate Checks:</th>
                    <td width="*">
                      <select ng-change="dirtyException(exc)" ng-model="exc.skipDupChecksIndc" ng-class="{'exc-deflt' : !exc.skipDupChecksIndc, 'exc-y' : exc.skipDupChecksIndc == 'Y'}">
                        <option value="">Default</option>
                        <option value="N">Execute</option>
                        <option value="Y">Skip</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th>Company Check:</th>
                    <td>
                      <select ng-change="dirtyException(exc)" ng-model="exc.skipVerificationIndc" ng-class="{'exc-deflt' : !exc.skipVerificationIndc, 'exc-y' : exc.skipVerificationIndc == 'Y'}">
                        <option value="">Default</option>
                        <option value="N">Execute</option>
                        <option value="Y">Skip</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th>D&B VAT:</th>
                    <td>
                      <select ng-change="dirtyException(exc)" ng-model="exc.checkVatIndc"  ng-class="{'exc-deflt' : !exc.checkVatIndc, 'exc-y' : exc.checkVatIndc == 'Y'}">
                        <option value="">Default</option>
                        <option value="N">Ignore</option>
                        <option value="Y">Verify</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th>D&B Import:</th>
                    <td>
                      <select ng-change="dirtyException(exc)" ng-model="exc.importDnbInfoIndc"  ng-class="{'exc-deflt' : !exc.importDnbInfoIndc, 'exc-y' : exc.importDnbInfoIndc == 'Y'}">
                        <option value="">Default</option>
                        <option value="N">No Address Info</option>
                        <option value="Y">With Address Info</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th>Skip Matching/Calcs:</th>
                    <td>
                      <select ng-change="dirtyException(exc)" ng-model="exc.skipChecksIndc" ng-class="{'exc-deflt' : !exc.skipChecksIndc, 'exc-y' : exc.skipChecksIndc == 'Y'}">
                        <option value="">Default</option>
                        <option value="N">No</option>
                        <option value="Y">Yes</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th colspan="2" style="font-weight:bold;padding-left:10px;padding-top:10px;text-align:left;">Duplicate Check Addresses:</th>
                  </tr>
                  <tr>
                    <td colspan="2">
                    
                      <table cellspacing="0" cellpadding="0">
                        <tr>
                          <td colspan="3" >
                            <span class="exc-none"  style="padding-left:20px;" ng-show="!exc.dupAddressChecks || exc.dupAddressChecks.length == 0">No mapped addresses.</span>
                            <ul>
                              <li style="font-weight:normal;padding:0;padding-left:20px" ng-repeat="dup in exc.dupAddressChecks">
                                <span class="exc-addr-span" style="font-weight:normal;">{{dup.cmrTypeDesc}}</span> >> 
                                <span class="exc-addr-span" style="font-weight:normal;">{{dup.rdcType}}</span>
                                <a class="exc-link2" title="Remove Mapping" ng-click="removeDupMapping(exc, dup)">Remove</a>
                              </li>
                            </ul>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <select ng-model="exc.dupCmrType" class="exc-addr-input" placeholder="CMR Type">
                              <option ng-repeat="cmrType in cmrTypes" value="{{cmrType.id}}">{{cmrType.id == '' ? 'CMR Type' : cmrType.name}}</option>
                            </select>
                          </td>
                          <td>
                            <select ng-model="exc.dupRdcType" class="exc-addr-input" placeholder="RDC Type">
                              <option value="">RDC Type</option>
                              <option value="ZS01">ZS01</option>
                              <option value="ZP01">ZP01</option>
                              <option value="ZI01">ZI01</option>
                              <option value="ZD01">ZD01</option>
                              <option value="ZS02">ZS02</option>
                            </select>
                          </td>
                          <td>
                            <a class="exc-link" style="padding:0" title="Add Mapping" ng-click="addDupMapping(exc)">Add</a>
                          </td>
                        </tr>
                      </table>
                    
                    </td>
                  </tr>
                  <tr>
                    <th colspan="2" style="font-weight:bold;padding-left:10px;padding-top:10px;text-align:left;">Ignore Address Updates for:</th>
                  </tr>
                  <tr>
                    <td colspan="2">

                      <table cellspacing="0" cellpadding="0">
                        <tr>
                          <td colspan="2">
                            <span class="exc-none" style="padding-left:20px;" ng-show="!exc.skipAddressChecks || exc.skipAddressChecks.length == 0">No mapped addresses.</span>
                            <ul>
                              <li style="font-weight:normal;padding:0;padding-left:20px" ng-repeat="addr in exc.skipAddressChecks">
                                <span class="exc-addr-span" style="font-weight:normal;">{{addr.description}}</span></span>
                                <a class="exc-link2" title="Remove Type" ng-click="removeSkipMapping(exc, addr)">Remove</a>
                              </li>
                            </ul>
                          </td>
                        </tr>
                        <tr>
                          <td colspan="2">
                            <select ng-model="exc.skipCmrType" class="exc-addr-input" placeholder="CMR Type">
                              <option ng-repeat="cmrType in cmrTypes" value="{{cmrType.id}}">{{cmrType.name}}</option>
                            </select>
                            <a class="exc-link" title="Add Address Type" ng-click="addSkipMapping(exc)">Add</a>
                          </td>
                        </tr>
                      </table>

                    </td>
                  </tr>
                  
                </table>
                <div class="exc-actions">
                  <input type="button" title="Save Exception" ng-show="exc.status == 'N' || exc.status == 'M'" class="exc-save" value="Save" ng-click="saveException(exc)">
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div ng-show="viewMode == 'N'">
        <div ng-show="exceptions && exceptions.length > 0" class="exc-cont" ng-class="{'exc-new' : exc.status == 'N' || exc.status == 'M'}" ng-repeat="exc in exceptions" title="{{exc.status == 'N' || exc.status == 'M' ? 'Unsaved' : ''}}">
          <cmr:row topPad="10">
            <div class="exc-header">
              Exception for <div style="text-transform:uppercase; display:inline">{{exc.description}}</div>
              <div class="exc-actions">
                <img class="remv" title="Remove Exception" ng-click="removeException(exc)" src="${resourcesPath}/images/remove.png">
              </div> 
            </div>
            <table cellspacing="0" cellpadding="0" class="exc-table" >
              <tr>
                <th width="40%">
                  <img class="exc-info" title="{{title.skipDupChecksIndc}}" src="${resourcesPath}/images/info-bubble-icon.png">
                  Skip Duplicate Checks (Request and CMR Levels):
                </th>
                <td width="*">
                  <select ng-change="dirtyException(exc)" ng-model="exc.skipDupChecksIndc" ng-class="{'exc-deflt' : !exc.skipDupChecksIndc, 'exc-y' : exc.skipDupChecksIndc == 'Y'}">
                    <option value="">(use default, country or parent scenario setting)</option>
                    <option value="N">No, do not skip duplicate checks</option>
                    <option value="Y">Yes, skip duplicate checks</option>
                  </select>
                  <span title="Overrides default behavior" class="override" ng-show="exc.skipDupChecksIndc == 'Y'">&nbsp;</span>
                </td>
              </tr>
              <tr>
                <th width="40%">
                  <img class="exc-info" title="{{title.skipCompanyVerificationChecks}}" src="${resourcesPath}/images/info-bubble-icon.png">
                  Skip Company Verification Checks :
                </th>
                <td width="*">
                  <select ng-change="dirtyException(exc)" ng-model="exc.skipVerificationIndc" ng-class="{'exc-deflt' : !exc.skipVerificationIndc, 'exc-y' : exc.skipVerificationIndc == 'Y'}">
                    <option value="">(use default, country or parent scenario setting)</option>
                    <option value="N">No, do not skip company verification checks</option>
                    <option value="Y">Yes, skip company verification checks</option>
                  </select>
                  <span title="Overrides default behavior" class="override" ng-show="exc.skipVerificationIndc == 'Y'">&nbsp;</span>
                </td>
              </tr>
              <tr>
                <th>
                  <img class="exc-info" title="{{title.checkVatIndc}}" src="${resourcesPath}/images/info-bubble-icon.png">
                  Check VAT for D&B Matching:
                </th>
                <td>
                  <select ng-change="dirtyException(exc)" ng-model="exc.checkVatIndc"  ng-class="{'exc-deflt' : !exc.checkVatIndc, 'exc-y' : exc.checkVatIndc == 'Y'}">
                    <option value="">(use default, country or parent scenario setting)</option>
                    <option value="N">No, use only name and address information</option>
                    <option value="Y">Yes, use VAT along with name and address information</option>
                  </select>
                  <span title="Overrides default behavior" class="override" ng-show="exc.checkVatIndc == 'Y'">&nbsp;</span>
                </td>
              </tr>
              <tr>
                <th>
                  <img class="exc-info" title="{{title.importDnbInfoIndc}}" src="${resourcesPath}/images/info-bubble-icon.png">
                  Information to Import from D&B :
                </th>
                <td>
                  <select ng-change="dirtyException(exc)" ng-model="exc.importDnbInfoIndc"  ng-class="{'exc-deflt' : !exc.importDnbInfoIndc, 'exc-y' : exc.importDnbInfoIndc == 'Y'}">
                    <option value="">(use default, country or parent scenario setting)</option>
                    <option value="N">Subindustry, ISIC, and DUNS No. only</option>
                    <option value="Y">All including name and address information</option>
                  </select>
                  <span title="Overrides default behavior" class="override" ng-show="exc.importDnbInfoIndc == 'Y'">&nbsp;</span>
                </td>
              </tr>
              <tr>
                <th>
                  <img class="exc-info" title="{{title.skipChecksIndc}}" src="${resourcesPath}/images/info-bubble-icon.png">
                  Skip Automated Matching Processes and Calculations:
                </th>
                <td>
                  <select ng-change="dirtyException(exc)" ng-model="exc.skipChecksIndc" ng-class="{'exc-deflt' : !exc.skipChecksIndc, 'exc-y' : exc.skipChecksIndc == 'Y'}">
                    <option value="">(use default, country or parent scenario setting)</option>
                    <option value="N">No, execute all processes</option>
                    <option value="Y">Yes, SKIP automated processes and calculations</option>
                  </select>
                  <span title="Overrides default behavior" class="override" ng-show="exc.skipChecksIndc == 'Y'">&nbsp;</span>
                </td>
              </tr>
              <tr>
                <th>
                  <img class="exc-info" title="{{title.dupAddressChecks}}" src="${resourcesPath}/images/info-bubble-icon.png">
                  Address Types to Check for Duplicates:
                </th>
                <td>
                  <table cellspacing="0" cellpadding="0">
                    <tr>
                      <td colspan="3">
                        <span class="exc-none" ng-show="!exc.dupAddressChecks || exc.dupAddressChecks.length == 0">No mapped addresses.</span>
                        <ul>
                          <li ng-repeat="dup in exc.dupAddressChecks">
                            <span class="exc-addr-span">{{dup.cmrTypeDesc}}</span> checked against <span class="exc-addr-span">{{dup.rdcTypeDesc}}</span>
                            <a class="exc-link2" title="Remove Mapping" ng-click="removeDupMapping(exc, dup)">Remove</a>
                          </li>
                        </ul>
                      </td>
                    </tr>
                    <tr>
                      <td colspan="3">&nbsp;</td>
                    </tr>
                    <tr>
                      <td class="exc-addr-td">CMR Type</td>
                      <td class="exc-addr-td">RDC Type</td>
                      <td>&nbsp;</td>
                    </tr>
                    <tr>
                      <td>
                        <select ng-model="exc.dupCmrType" class="exc-addr-input" placeholder="CMR Type">
                          <option ng-repeat="cmrType in cmrTypes" value="{{cmrType.id}}">{{cmrType.name}}</option>
                        </select>
                      </td>
                      <td>
                        <select ng-model="exc.dupRdcType" class="exc-addr-input" placeholder="RDC Type">
                          <option value=""></option>
                          <option value="ZS01">Sold-to (ZS01)</option>
                          <option value="ZP01">Bill-to (ZP01)</option>
                          <option value="ZI01">Install-at (ZI01)</option>
                          <option value="ZD01">Ship-to (ZD01)</option>
                          <option value="ZS02">Secondary Sold-to (ZS02)</option>
                        </select>
                      </td>
                      <td>
                        <a class="exc-link" title="Add Mapping" ng-click="addDupMapping(exc)">Add Mapping</a>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr>
                <td colspan="2">&nbsp;</td>
              </tr>
               <tr>
                <th>
                  <img class="exc-info" title="{{title.skipAddressChecks}}" src="${resourcesPath}/images/info-bubble-icon.png">
                  Skips Checks for Updates only to Address Types:
                </th>
                <td>
                  <table cellspacing="0" cellpadding="0">
                    <tr>
                      <td colspan="2">
                        <span class="exc-none" ng-show="!exc.skipAddressChecks || exc.skipAddressChecks.length == 0">No mapped addresses.</span>
                        <ul>
                          <li ng-repeat="addr in exc.skipAddressChecks">
                            <span class="exc-addr-span">{{addr.description}}</span></span>
                            <a class="exc-link2" title="Remove Type" ng-click="removeSkipMapping(exc, addr)">Remove</a>
                          </li>
                        </ul>
                      </td>
                    </tr>
                    <tr>
                      <td colspan="3">&nbsp;</td>
                    </tr>
                    <tr>
                      <td>
                        <select ng-model="exc.skipCmrType" class="exc-addr-input" placeholder="CMR Type">
                          <option ng-repeat="cmrType in cmrTypes" value="{{cmrType.id}}">{{cmrType.name}}</option>
                        </select>
                      </td>
                      <td>
                        <a class="exc-link" title="Add Address Type" ng-click="addSkipMapping(exc)">Add Type</a>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            <div class="exc-actions">
              <input type="button" title="Save Exception" ng-show="exc.status == 'N' || exc.status == 'M'" class="exc-save" value="Save" ng-click="saveException(exc)">
            </div>
          </cmr:row>
        </div>
      </div>
    </div>
    <cmr:row >
      &nbsp;
    </cmr:row>
    </div>
  </cmr:section>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Maintain by Engine Configuration"
      onClick="goToUrl('${contextPath}/auto/config/list')" pad="false"/>
    <cmr:button label="Maintain by Country Configuration"
      onClick="goToUrl('${contextPath}/auto/config/cntry')" pad="true"/>
    <cmr:button label="Back to Code Maintenance Home"
      onClick="backToCodeMaintHome()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>
</div>
<script src="${resourcesPath}/js/auto/auto_config.js?${cmrv}"></script>
