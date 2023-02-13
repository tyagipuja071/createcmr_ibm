/*
 * File: ww_validations.js
 * Description:
 * Contains the functions necessary to handle country/GEO specific validations.
 */

var _GBL_NO_INIT = false;
var _scenarioTypeHandler = null;
/**
 * Validator for Address Standardization completion
 */
function addAddrStdValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (!dojo.byId('addrstdResultDisplay')) {
          return new ValidationResult(null, true);
        }
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (!GEOHandler.isTGMERequired(cntry)) {
          dojo.byId('addrstdResultDisplay').innerHTML = "Not Required";
          return new ValidationResult(null, true);
        } else {
          var result = dojo.byId('addrstdResultDisplay').innerHTML.trim();
          if (result == '' || result.trim().toUpperCase() == 'NOT DONE') {
            return new ValidationResult(null, false, 'Address Standarization has not been completed yet.');
          } else {
            return new ValidationResult(null, true);
          }
        }

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');

}

/**
 * Validator for only a single Sold-to record for the request
 */
function addSoldToValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var zs01ReqId = FormManager.getActualValue('reqId');
        qParams = {
          REQ_ID : zs01ReqId,
        };
        var record = cmr.query('GETZS01VALRECORDS', qParams);
        var zs01Reccount = record.ret1;
        // Story 1202261 : validation for Brazil
        if (FormManager.getActualValue('cmrIssuingCntry') == '631') {
          if (Number(zs01Reccount) > 1) {
            return new ValidationResult(null, false, 'Only one Sold to - Legal (LE) Address can be defined.');
          } else if (Number(zs01Reccount == 0)) {
            return new ValidationResult(null, false, 'At least one Sold to - Legal (LE) Address must be defined.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          if (Number(zs01Reccount) > 1) {
            return new ValidationResult(null, false, 'Only one Sold-To Address can be defined.');
          } else if (Number(zs01Reccount == 0)) {
            return new ValidationResult(null, false, 'At least one Sold-To Address must be defined.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

/**
 * Validator to check whether CMR search has been performed
 */
function addCMRSearchValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var result = FormManager.getActualValue('findCmrResult');
        var reqType = FormManager.getActualValue('reqType');
        if (reqType == 'C' && FormManager.getActualValue('sourceSystId').toUpperCase() == 'FEDCMR' && FormManager.getActualValue('custGrp') == '14'){
          return new ValidationResult(null, true);
        }
        if (result == '' || result.toUpperCase() == 'NOT DONE') {
          return new ValidationResult(null, false, 'CMR Search has not been performed yet.');
        }
        if (reqType == 'U' && result.toUpperCase() != 'ACCEPTED') {
          return new ValidationResult(null, false, 'An existing CMR for update must be searched for and imported properly to the request.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

/**
 * Validator to check whether D&B search has been performed
 */
function addDnBSearchValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var ifProspect = FormManager.getActualValue('prospLegalInd');
        if (dijit.byId('prospLegalInd')) {
          ifProspect = dijit.byId('prospLegalInd').get('value');
        }
       
        if (FormManager.getActualValue('dnbPrimary') != 'Y' && ifProspect != 'Y') {
          return new ValidationResult(null, true);
        }
        var reqType = FormManager.getActualValue('reqType');
        if (reqType == 'U') {
          return new ValidationResult(null, true);
        }
        var reqStatus = FormManager.getActualValue('reqStatus');
        if (reqStatus != 'DRA') {
          return new ValidationResult(null, true);
        }
        if (isSkipDnbMatching() && ifProspect != 'Y') {
          return new ValidationResult(null, true);
        }
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var result = FormManager.getActualValue('findDnbResult');
        if ((result == '' || result.toUpperCase() == 'NOT DONE') && cntry != SysLoc.CHINA) {
          return new ValidationResult(null, false, 'D&B Search has not been performed yet.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function addDnBMatchingAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqId = FormManager.getActualValue('reqId');
        var reqType = FormManager.getActualValue('reqType');
        var reqStatus = FormManager.getActualValue('reqStatus');
        var matchOverrideIndc = FormManager.getActualValue('matchOverrideIndc');
        var findDnbResult = FormManager.getActualValue('findDnbResult');
        var userRole = FormManager.getActualValue('userRole');
        var ifReprocessAllowed = FormManager.getActualValue('autoEngineIndc');
        if (reqId > 0 && reqType == 'C' && reqStatus == 'DRA' && userRole == 'Requester' && (ifReprocessAllowed == 'R' || ifReprocessAllowed == 'P' || ifReprocessAllowed == 'B')
            && !isSkipDnbMatching() && FormManager.getActualValue('matchOverrideIndc') == 'Y') {
          // FOR CN
          var cntry = FormManager.getActualValue('landCntry');
          var loc = FormManager.getActualValue('cmrIssuingCntry');
          var custSubGroup = FormManager.getActualValue('custSubGrp');
          if(cntry == 'CN' || loc == '641' && custSubGroup != 'CROSS') {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_CN_API_ATTACHMENT', {
              ID : id
            });
            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, "By overriding the D&B matching, you\'re obliged to provide either one of the following documentation as backup - "
                  + "client\'s official website, Secretary of State business registration proof, client\'s confirmation email and signed PO, attach it under the file content "
                  + "of <strong>Name and Address Change(China Specific)</strong>. Please note that the sources from Wikipedia, Linked In and social medias are not acceptable.");
            } else {
              return new ValidationResult(null, true);
            }
          }
          
          // FOR US Temporary
          var id = FormManager.getActualValue('reqId');
          var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
            ID : id
          });
          if (ret == null || ret.ret1 == null) {
            return new ValidationResult(null, false, "By overriding the D&B matching, you\'re obliged to provide either one of the following documentation as backup - "
                + "client\'s official website, Secretary of State business registration proof, client\'s confirmation email and signed PO, attach it under the file content "
                + "of <strong>Company Proof</strong>. Please note that the sources from Wikipedia, Linked In and social medias are not acceptable.");
          } else {
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

/**
 * Method to check whether for a scenario dnb matching is allowed or not
 */
function isSkipDnbMatching() {
   var custGrp = FormManager.getActualValue('custGrp');
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var dnbPrimary = FormManager.getActualValue("dnbPrimary");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var countryUse = FormManager.getActualValue("countryUse");
  var subRegionCd = countryUse != null && countryUse.length > 0 ? countryUse : cntry;
  if(SysLoc.INDIA == FormManager.getActualValue('cmrIssuingCntry')){
        return false;
    }
  if(dnbPrimary == 'Y') {
    if (custGrp != null && custGrp != '' && custSubGrp != null && custSubGrp != '') {
      var qParams = {
        CNTRY : cntry,
        CUST_TYP : custGrp,
        CUST_SUB_TYP : custSubGroup,
        SUBREGION_CD : subRegionCd
      };
      var result = cmr.query("AUTO.SKIP_VERIFICATION_INDC", qParams);
      if (result.ret1 != null && result.ret1 != "") {
        if(result.ret1=='Y'){
          return true;
        } else if (result.ret1=='N'){
          return false;
        }
      } else {
        qParams.CUST_SUB_TYP = "*";
        result = cmr.query("AUTO.SKIP_VERIFICATION_INDC", qParams);
        if (result.ret1 != null && result.ret1 != '') {
          if(result.ret1 == 'Y'){
            return true;
          } else if (result.ret1 == 'N'){
            return false;
          }
        } else {
          qParams.CUST_TYP = "*";
          result = cmr.query("AUTO.SKIP_VERIFICATION_INDC", qParams);
          if (result.ret1 != null && result.ret1 != '') {
            if(result.ret1=='Y'){
              return true;
            } else if (result.ret1=='N'){
              return false;
            }
          }
        }
      }
      return false;
    } else return true;
  } else {
    return true;
  }
}

/**
 * Validator whether DPL check has been performed. Should only be for processors
 */
function addDPLCheckValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var result = FormManager.getActualValue('dplChkResult');
        console.log('>>> RUNNING WW addDPLCheckValidator');
        if (result == '' || result.toUpperCase() == 'NOT DONE') {
          return new ValidationResult(null, false, 'DPL Check has not been performed yet.');
        } else if (result == '' || result.toUpperCase() == 'ALL FAILED') {
          return new ValidationResult(null, false, 'DPL Check has failed. This record cannot be processed.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

/**
 * Validator for tax code inputs
 */
function addTaxCodesValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var taxCd1 = FormManager.getActualValue('taxCd1');
        var taxCd2 = FormManager.getActualValue('taxCd2');
        var taxCd3 = FormManager.getActualValue('taxCd3');

        var lbl1 = FormManager.getLabel('LocalTax1');
        var lbl2 = FormManager.getLabel('LocalTax2');
        var lbl3 = FormManager.getLabel('LocalTax3');

        var cannotBeEqual = taxCd1.length > 0 && taxCd1 == taxCd2;
        if (cannotBeEqual) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'The values for ' + lbl1 + ' and ' + lbl2 + ' cannot be the same.');
        }
        cannotBeEqual = (taxCd1.length > 0 && taxCd1 == taxCd3);
        if (cannotBeEqual) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'The values for ' + lbl1 + ' and ' + lbl3 + ' cannot be the same.');
        }
        cannotBeEqual = (taxCd2.length > 0 && taxCd2 == taxCd3);
        if (cannotBeEqual) {
          return new ValidationResult({
            id : 'taxCd2',
            type : 'text',
            name : 'taxCd2'
          }, false, 'The values for ' + lbl2 + ' and ' + lbl3 + ' cannot be the same.');
        }

        var mustPreFill = taxCd1.length == 0 && taxCd2.length > 0;
        if (mustPreFill) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, lbl1 + ' must have a value if ' + lbl2 + ' is specified.');
        }
        mustPreFill = (taxCd1.length == 0 && taxCd3.length > 0);
        if (mustPreFill) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, lbl1 + ' must have a value if ' + lbl3 + ' is specified.');
        }
        mustPreFill = (taxCd2.length == 0 && taxCd3.length > 0);
        if (mustPreFill) {
          return new ValidationResult({
            id : 'taxCd2',
            type : 'text',
            name : 'taxCd2'
          }, false, lbl2 + ' must have a value if ' + lbl3 + ' is specified.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');

}

/**
 * Validator whether interface values have been retrieved or not
 */
function addCovBGValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var result = FormManager.getActualValue('covBgRetrievedInd');
        if (result == '' || result.toUpperCase() != 'Y') {
          return new ValidationResult(null, false, 'Coverage/Buying Group/GLC/DUNS values have not been retrieved yet.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function initGenericTemplateHandler() {
  // templates/scenarios initialization. connect onchange of the customer type
  // to load the template
  console.log('init init');
  if (_templateHandler == null && FormManager.getField('custSubGrp')) {
    _templateHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      if (_delayedLoadComplete) {
        var val = FormManager.getActualValue('custSubGrp');
        if (val != '') {
          TemplateService.fill('reqentry');
        }
      }
    });
  }
  console.log(_templateHandler);
  if (_templateHandler && _templateHandler[0]) {
    _templateHandler[0].onChange();
  }
  TemplateService.init();
}

function addCrossBorderValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        // only Create type will be validated
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }

        var scenario = FormManager.getActualValue('custSubGrp');
        var mscenario = FormManager.getActualValue('custGrp');

        if (mscenario == 'CROSS') {          
            scenario = 'CROSS';           
        }

        var reqId = FormManager.getActualValue('reqId');
        var defaultLandCntry = FormManager.getActualValue('defaultLandedCountry');
        var result = cmr.query('VALIDATOR.CROSSBORDER', {
          REQID : reqId
        });
        if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function updateMainCustomerNames(cntry, addressMode, saving, finalSave, force) {
  if (finalSave || force) {
    var trgType = 'ZS01';
    if (typeof (GEOHandler) != 'undefined') {
      trgType = GEOHandler.getAddressTypeForName();
    }
    /*
     * if (cntry == SysLoc.IRELAND || cntry == SysLoc.UK) { trgType = 'ZI01'; }
     */
    console.log('Target Address Type: ' + trgType);

    var addrType = FormManager.getActualValue('addrType');
    if (addrType == trgType || force) {
      var nm1 = FormManager.getActualValue('custNm1');
      var nm2 = FormManager.getActualValue('custNm2');

      console.log('Setting Customer Names: ' + nm1 + ' - ' + nm2);
      FormManager.setValue('mainCustNm1', nm1);
      FormManager.setValue('mainCustNm2', nm2);
      FormManager.setValue('mainAddrType', addrType);

    }
  }
}

var _clientTierHandler = null;
function addClientTierDefaultLogic() {
  if (_clientTierHandler == null) {
    _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      value = FormManager.getActualValue('clientTier');
      FormManager.enable('isuCd');
      if (value == 'B' || value == 'M' || value == 'W' || value == 'T' || value == 'S' || value == 'C' || value == 'N') {
        FormManager.setValue('isuCd', '32');
        FormManager.readOnly('isuCd');
      } else if (value == 'V' || value == '4' || value == 'A' || value == '6' || value == 'E' || value == 'Y') {
        FormManager.setValue('isuCd', '34');
        FormManager.readOnly('isuCd');
      } else if (value == 'Z') {
        FormManager.setValue('isuCd', '21');
        FormManager.readOnly('isuCd');
      } else {
        if (PageManager.isReadOnly()) {
          FormManager.readOnly('isuCd');
        } else {
          FormManager.enable('isuCd');
        }
      }
    });
  }
  if (_clientTierHandler && _clientTierHandler[0]) {
    _clientTierHandler[0].onChange();
  }
}

function addGenericVATValidator(cntry, tabName, formName, aType) {
  return function() {   
    FormManager.addFormValidator((function() {
      var landCntry = cntry;
      var addrType = aType;
      return {
        validate : function() {
          var reqType = FormManager.getActualValue('reqType');
          var vat = FormManager.getActualValue('vat');

          if (!vat || vat == '' || vat.trim() == '') {
            return new ValidationResult(null, true);
          } else if (reqType == 'U' && vat == '@') {
            // vat deletion for updates
            return new ValidationResult(null, true);
          }

          var zs01Cntry = landCntry;

          var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
            REQID : FormManager.getActualValue('reqId'),
            TYPE : addrType ? addrType : 'ZS01'
          });
          if (ret && ret.ret1 && ret.ret1 != '') {
            zs01Cntry = ret.ret1;
          }
          console.log('ZS01 VAT Country: ' + zs01Cntry);          
          var result = cmr.validateVAT(zs01Cntry, vat);
          if (result && !result.success) {
            if (result.errorPattern == null) {
              return new ValidationResult({
                id : 'vat',
                type : 'text',
                name : 'vat'
              }, false, result.errorMessage + '.');
            } else {
              var msg = result.errorMessage + '. Format should be ' + result.errorPattern.formatReadable;
              return new ValidationResult({
                id : 'vat',
                type : 'text',
                name : 'vat'
              }, false, msg);
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      };
    })(), tabName, formName);
  }; 

}

// TODO VAT is required for some cross landCntry
function requireVATForCrossBorder() {  
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var scenario = FormManager.getActualValue('custGrp');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var vat = FormManager.getActualValue('vat');
        var vatInd = FormManager.getActualValue('vatInd');
        
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }
        if (scenario != null && !scenario.includes('CRO')) {
          return new ValidationResult(null, true);
        }
        // MCO cross subType codes
        if (custSubGrp != null && (custSubGrp.includes('XSOFT') || custSubGrp.includes('XSL') || custSubGrp.includes('XPRIC') || custSubGrp.includes('XPC') || custSubGrp.includes('XGO'))) {
          return new ValidationResult(null, true);
        }
        
        if (vatInd == 'N' && vat == '') { 
          FormManager.resetValidations('vat');
        }
        
        var zs01Cntry = FormManager.getActualValue('cmrIssuingCntry');
        var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
          REQID : FormManager.getActualValue('reqId'),
          TYPE : 'ZS01'
        });
        if (ret && ret.ret1 && ret.ret1 != '') {
          zs01Cntry = ret.ret1;
        }

        if (!vat || vat == '' || vat.trim() == '') {
          if (GEOHandler.VAT_RQD_CROSS_LNDCNTRY.indexOf(zs01Cntry) >= 0) {
            var msg = "VAT for " + zs01Cntry + " # is required.";
            return new ValidationResult({
              id : 'vat',
              type : 'text',
              name : 'vat'
            }, false, msg);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR'); 

}

/**
 * Checks validator if exists before adding
 */
function checkAndAddValidator(field, validator, params) {
  var validators = FormManager.GETFIELD_VALIDATIONS[field];
  if (validators == null || validators.indexOf(validator) < 0) {
    FormManager.addValidator(field, validator, params);
  }
}

function addChecklistValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('validating checklist..');
        var checklist = dojo.query('table.checklist');

        // local customer name if found
        var localNm = checklist.query('input[name="localCustNm"]');
        if (localNm.length > 0 && localNm[0].value.trim() == '') {
          return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
        }

        // local customer name if found
        var localAddr = checklist.query('input[name="localAddr"]');
        if (localAddr.length > 0 && localAddr[0].value.trim() == '') {
          return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
        }

        var questions = checklist.query('input[type="radio"]');
        if (questions.length > 0) {
          var noOfQuestions = questions.length / 2;
          var checkCount = 0;
          for (var i = 0; i < questions.length; i++) {
            if (questions[i].checked) {
              checkCount++;
            }
          }
          if (noOfQuestions != checkCount) {
            return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
          }
        }

        // add check for checklist on DB
        var reqId = FormManager.getActualValue('reqId');
        var record = cmr.getRecord('GBL_CHECKLIST', 'ProlifChecklist', {
          REQID : reqId
        });
        if (!record || !record.sectionA1) {
          return new ValidationResult(null, false, 'Checklist has not been registered yet. Please execute a \'Save\' action before sending for processing to avoid any data loss.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CHECKLIST_TAB', 'frmCMR');
}

function initChecklistMainAddress() {
  var container = dojo.byId('checklist-main-addr');
  if (container != null) {
    if (typeof (_allAddressData) != 'undefined') {
      for (var i = 0; i < _allAddressData.length; i++) {
        if (_allAddressData[i].addrType && _allAddressData[i].addrType[0] == 'ZS01') {
          container.innerHTML = _allAddressData[i].addrTxt[0];
          return;
        }
      }
      container.innerHTML = '';
    }
  }
}
/**
 * Adds the generic postal code validator to the form
 */
function addGenericZIPValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('landCntry');
        var loc = FormManager.getActualValue('cmrIssuingCntry'); // skipped for
        // Brazil and
        // Peru
        if (!cntry || cntry == '' || cntry.trim() == '' || (loc == '631' || loc == '815' || loc == '681' || loc == '781')) {
          return new ValidationResult(null, true);
        }
        var postCd = FormManager.getActualValue('postCd');

        console.log('Country: ' + cntry + ' Postal Code: ' + postCd);
        if(cntry == 'LV') {
          if(postCd == ''){
            return new ValidationResult({
              id : 'postCd',
              type : 'text',
              name : 'postCd'
            }, false, ('Postal Code for LV is required.'));
          }
          
          if(!postCd.startsWith("LV-")){
            return new ValidationResult({
              id : 'postCd',
              type : 'text',
              name : 'postCd'
            }, false, ('Postal Code format should be LV-nnnn.'));
          } else if(postCd.length != '7') {
            return new ValidationResult({
              id : 'postCd',
              type : 'text',
              name : 'postCd'
            }, false, ('Postal Code format should be LV-nnnn.'));
          } else if(postCd.length == '7'){
            var numPattern = /^[0-9]+$/;
            if (!postCd.substr(3,7).match(numPattern)){
              return new ValidationResult({
                id : 'postCd',
                type : 'text',
                name : 'postCd'
              }, false, ('Postal Code format should be LV-nnnn.'));
            }
          }
        } else {
          var result = cmr.validateZIP(cntry, postCd, loc);
          if (result && !result.success) {
            if (result.errorPattern == null) {
              return new ValidationResult({
                id : 'postCd',
                type : 'text',
                name : 'postCd'
              }, false, (result.errorMessage ? result.errorMessage : 'Cannot get error message for Postal Code.') + '.');
            } else {
              var msg;
  
              if (loc == '754' || loc == '866') {
                msg = result.errorMessage + '. Format should be ' + result.errorPattern.formatReadable;
              } else {
                msg = result.errorMessage + '. Format should be ' + result.errorPattern.formatReadable;
              }
  
              return new ValidationResult({
                id : 'postCd',
                type : 'text',
                name : 'postCd'
              }, false, msg);
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

/**
 * Adds the generic postal code validator for Ireland as landed country - CMR -
 * 6033
 */
function addGenericPostalCodeValidator() {
        var cntry = FormManager.getActualValue('landCntry');
        var loc = FormManager.getActualValue('cmrIssuingCntry');
        var postCd = FormManager.getActualValue('postCd');
        var scenario= FormManager.getActualValue('custGrp');

        console.log('Country: ' + cntry + ' Postal Code: ' + postCd);
        if(scenario == 'CROSS' && cntry == 'IE' && loc != '754') {
          FormManager.removeValidator('postCd', Validators.REQUIRED);
        }
}

/**
 * Adds the general validation for failed DPL checks. This will require a DPL
 * Screening attachment
 */
function addFailedDPLValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (typeof (_pagemodel) != 'undefined') {
          if (_pagemodel.dplChkResult.trim() == 'SF' || _pagemodel.dplChkResult.trim() == 'AF') {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_DPL_ATTACHMENT', {
              ID : id
            });

            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, 'DPL Matching results has not been attached to the request. This is required since DPL checks failed for one or more addresses.');
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function addINACValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var inac = FormManager.getActualValue('inacCd');
        if (inac != '' && !isNaN(inac)) {
          var results = cmr.query('CHECK_INAC', {
            MANDT : cmr.MANDT,
            INAC : inac
          });
          if (results == null || (results != null && results.ret1 == null)) {
            return new ValidationResult({
              id : 'inacCd',
              type : 'text',
              name : 'inacCd'
            }, false, 'INAC value is invalid.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/*
 * validate CMRNumber for Legacy Countries
 */
function validateCMRNumberForLegacy() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cmrNo = FormManager.getActualValue('cmrNo');
        var _custSubGrp = FormManager.getActualValue('custSubGrp');

        var numPattern = /^[0-9]+$/;
        if (FormManager.getActualValue('reqType') != 'C') {
          return new ValidationResult(null, true);
        }
        if (cmrNo == '') {
          return new ValidationResult(null, true);
        } else {
          // Skip validation for Prospect Request
          var ifProspect = FormManager.getActualValue('prospLegalInd');
          if (dijit.byId('prospLegalInd')) {
            ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
          }
          console.log("validateCMRNumberForLegacy ifProspect:" + ifProspect);
          if ('Y' == ifProspect) {
            return new ValidationResult(null, true);
          }
          // Validation for Internal Scenario
          var internalScenarios = [ 'ZAINT', 'NAINT', 'LSINT', 'SZINT', 'ZAXIN', 'NAXIN', 'LSXIN', 'SZXIN' ];
          if (_custSubGrp == 'INTER' || _custSubGrp == 'CRINT' || _custSubGrp == 'XINT' || internalScenarios.includes(_custSubGrp)) {
            if (!cmrNo.startsWith("99")) {
              return new ValidationResult(null, false, 'Internal CMR should begin with 99.');
            }
          } else if (_custSubGrp != 'INTER' || _custSubGrp != 'CRINT' || _custSubGrp != 'XINT' || !internalScenarios.includes(_custSubGrp)) {
            if (cmrNo.startsWith("99")) {
              return new ValidationResult(null, false, 'CMR Starting with 99 is allowed for Internal Scenario Only.');
            }
          }
          if (cmrNo == '000000') {
            return new ValidationResult(null, false, 'CMR Number should be number only Except -> 000000');
          }
          if (cmrNo.length >= 1 && cmrNo.length != 6) {
            return new ValidationResult(null, false, 'CMR Number should be 6 digit long.');
          }
          if (cmrNo.length > 1 && !cmrNo.match(numPattern)) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'CMR Number should be number only.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/*
 * validate Existing CMRNo
 */
function validateExistingCMRNo() {
    FormManager.addFormValidator((function() {
      return {
        validate : function() {
          console.log('checking requested cmr number...');
          var reqType = FormManager.getActualValue('reqType');
          var cmrNo = FormManager.getActualValue('cmrNo');
          var cntry = FormManager.getActualValue('cmrIssuingCntry');
          var action = FormManager.getActualValue('yourAction');
          var _custSubGrp = FormManager.getActualValue('custSubGrp');
          if (reqType == 'C' && cmrNo) {
            if (cmrNo.startsWith('P')) { 
              return new ValidationResult(null, true);
            }          
            var exists = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC', {
              COUNTRY : cntry,
              CMR_NO : cmrNo,
              MANDT : cmr.MANDT
            });
            if (exists && exists.ret1 && action != 'PCM') {
              return new ValidationResult({
                id : 'cmrNo',
                type : 'text',
                name : 'cmrNo'
              }, false, 'The requested CMR Number ' + cmrNo + ' already exists in the system.');
            } else {
              exists = cmr.query('LD.CHECK_EXISTING_CMR_NO_RESERVED', {
                COUNTRY : cntry,
                CMR_NO : cmrNo,
                MANDT : cmr.MANDT
              });
              if (exists && exists.ret1) {
                return new ValidationResult({
                  id : 'cmrNo',
                  type : 'text',
                  name : 'cmrNo'
                }, false, 'The requested CMR Number ' + cmrNo + ' already exists in the system.');
              }
            }
          }
          return new ValidationResult({
            id : 'cmrNo',
            type : 'text',
            name : 'cmrNo'
          }, true);
        }
      };
    })(), 'MAIN_IBM_TAB', 'frmCMR');
}

 
/**
 * Validator for the DPL Assessment
 */
function addDPLAssessmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var dplResult = FormManager.getActualValue('dplChkResult').toUpperCase();
        if (dplResult != 'AF' && dplResult != 'SF'){
          return new ValidationResult(null, true);
        }
        var result = typeof(_pagemodel) != 'undefined' ? _pagemodel.intDplAssessmentResult : 'X';
        if (!result || result.trim() == '') {
          return new ValidationResult(null, false, 'DPL Assessment is required for failed DPL checks. ');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

/**
 * Validator for the DPL Assessment
 */
function addDPLAssessmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var dplResult = FormManager.getActualValue('dplChkResult').toUpperCase();
        if (dplResult != 'AF' && dplResult != 'SF'){
          return new ValidationResult(null, true);
        }
        var result = typeof(_pagemodel) != 'undefined' ? _pagemodel.intDplAssessmentResult : 'X';
        if (!result || result.trim() == '') {
          return new ValidationResult(null, false, 'DPL Assessment is required for failed DPL checks. ');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function resetVATValidationsForPayGo(){
  var systemId = FormManager.getActualValue('sourceSystId');
  var cntry= FormManager.getActualValue('cmrIssuingCntry');
  var vat = FormManager.getActualValue('vat');
  var vatInd = FormManager.getActualValue('vatInd');
  var results = cmr.query('GET_PARTNER_VAT_EXCEPTIONS', {
    COUNTRY : cntry,
    SERVICE_ID : systemId   
    
  }); 
  if((results!= null || results!= undefined || results.ret1!='') && results.ret1 == 'Y' && vat == ''){    
   FormManager.resetValidations('vat');
   //FormManager.getField('vatExempt').checked = true;
    console.log('VAT is non mandatory for PayGO');
  } 
  
  if (vatInd == 'N' && vat == '') { 
    FormManager.resetValidations('vat');
  }
  
}

function addIsuCdObsoleteValidator(){
  var oldIsuCd = _pagemodel.isuCd;
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
               var isuCd = FormManager.getActualValue('isuCd');
               if (reqType == 'C' && isuCd == '32') {
                 // CREATCMR-7884
                 var cntry = FormManager.getActualValue('cmrIssuingCntry');
                 if(cntry == '796'){
                   var custSubGrp = FormManager.getActualValue('custSubGrp');
                   var custSubGrpList = ['NRML','ESOSW','XESO','CROSS'];
                   if(custSubGrpList.includes(custSubGrp)){
                     console.log('>>> Skip ISU Obsolete Validator for NRML/ESOSW/XESO/CROSS when isuCd = 32');
                     return new ValidationResult(null, true);
                   }
                 }
                 return new ValidationResult(null, false, 'ISU-32 is obsoleted. Please select valid value for ISU. ');
               }else  if (reqType == 'U' && isuCd == '32' && oldIsuCd != '32') {
                 return new ValidationResult(null, false, 'ISU-32 is obsoleted. Please select valid value for ISU. ');
               }
               else {
               return new ValidationResult(null, true);
             }
      }
    }
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

// CREATCMR-6244
function vatOptionalForLandedUK() {
  var _reqId = FormManager.getActualValue('reqId');
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  var custGrp = FormManager.getActualValue('custGrp');
  var vat = FormManager.getActualValue('vat');
  var vatInd = FormManager.getActualValue('vatInd');
  
  var params = {
    REQ_ID : _reqId,
    ADDR_TYPE : "ZS01"
  };

  var landCntryResult = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', params);
  landCntry = landCntryResult.ret1;

  if (reqType == 'C') {
    if (landCntry == 'GB') {
      if ((issuingCntry != '866'  && custGrp == 'CROSS') || (issuingCntry == '866' && custGrp == 'LOCAL')) {
        FormManager.resetValidations('vat');
        FormManager.removeValidator('vat', Validators.REQUIRED);
      }
    }
  } 
  if (vatInd == 'N' && vat == '') { 
    FormManager.resetValidations('vat');
  }
  
}

function afterConfigForEMEA(){
// 6244
  if (_scenarioTypeHandler == null && FormManager.getField('custGrp')) {
    _scenarioTypeHandler = dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
      vatOptionalForLandedUK();
    });
  }
}


function addVatIndValidator(){

  var vat = FormManager.getActualValue('vat');
  var vatInd = FormManager.getActualValue('vatInd');
  var reqStatus = FormManager.getActualValue('reqStatus');     
  var cntry= FormManager.getActualValue('cmrIssuingCntry');
  var results = cmr.query('GET_COUNTRY_VAT_SETTINGS', {
    ISSUING_CNTRY : cntry
  });
  
    
  if ((results != null || results != undefined || results.ret1 != '') && results.ret1 == 'O' && vat == '' && vatInd == '') {
    FormManager.removeValidator('vat', Validators.REQUIRED);
    FormManager.setValue('vatInd', 'N');
  } else if ((results != null || results != undefined || results.ret1 != '') && vat != '' && vatInd != 'E' && vatInd != 'N' && vatInd != '') {
    FormManager.setValue('vatInd', 'T');
    FormManager.readOnly('vatInd');
  } else if ((results != null || results != undefined || results.ret1 != '') && results.ret1 == 'R' && vat == '' && vatInd != 'E' && vatInd != 'N' && vatInd != 'T' && vatInd != '') {
    FormManager.setValue('vat', '');
    FormManager.setValue('vatInd', '');
  } else if (vat && dojo.string.trim(vat) != '' && vatInd != 'E' && vatInd != 'N' && vatInd != '') {
    FormManager.setValue('vatInd', 'T');
    FormManager.readOnly('vatInd');
  } else if (vat && dojo.string.trim(vat) == '' && vatInd != 'E' && vatInd != 'T' && vatInd != '') {
    FormManager.removeValidator('vat', Validators.REQUIRED);
    FormManager.setValue('vatInd', 'N');
  }
  
  if ((vat && dojo.string.trim(vat) == '') || (vat && dojo.string.trim(vat) == null ) && vatInd == 'N'){
    FormManager.resetValidations('vat');
  }
  
}

function setToReadOnly() {
 
 var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
   
 if (viewOnlyPage == 'true') {  
   FormManager.resetValidations('vat');
   FormManager.resetValidations('vatInd');
   FormManager.readOnly('vat');
   FormManager.readOnly('vatInd');
 } 
 
}

function vatIndOnChange() {
  
  var _vatIndHandler = null;
    
    if (_vatIndHandler == null) {
    _vatIndHandler = dojo.connect(FormManager.getField('vatInd'), 'onChange', function(value) {
      var vatInd = FormManager.getActualValue('vatInd');
      var userRole = FormManager.getActualValue('userRole');
      if (userRole == 'Viewer') {
        return;
      }
      if (vatInd && dojo.string.trim(vatInd) == 'T') {
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
        FormManager.enable('vat');
        FormManager.setValue('vatExempt', 'N');        
        FormManager.setValue('vatInd', 'T');
      } else if (vatInd && dojo.string.trim(vatInd) == 'N') {
        FormManager.removeValidator('vat', Validators.REQUIRED);
        FormManager.readOnly('vat');
        FormManager.setValue('vat', '');        
        FormManager.setValue('vatInd', 'N');
      } else if (vatInd && dojo.string.trim(vatInd) == 'E') {
        FormManager.removeValidator('vat', Validators.REQUIRED);
        FormManager.enable('vat');
        FormManager.setValue('vatExempt', 'Y');       
        FormManager.setValue('vatInd', 'E');
      }
    });
  }
  
  if (_vatIndHandler && _vatIndHandler[0]) {
    _vatIndHandler[0].onChange();
  }
  
var _vatHandler = null;
  
  if (_vatHandler == null){   
    
    dojo.byId('vat').onkeyup = function() {
      var isReadOnly = dojo.byId('vat').readOnly;
      var vat = FormManager.getActualValue('vat');
      if (vat == '' && !isReadOnly) {
        FormManager.enable('vatInd');
        FormManager.setValue('vatInd', ''); 
      }
    }
  }
}

function isViewOnly() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  return viewOnlyPage == 'true';
}

function isImportingFromQuickSearch() {
  return dojo.cookie('qs') == 'Y' || new URLSearchParams(location.search).get('qs') == 'Y';
}

function isPrivateScenario() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  return ["PRICU", "PRIPE", "FIPRI", "DKPRI", "BEPRI", "CHPRI"].includes(custSubGrp);
}

function setVatIndFieldsForGrp1AndNordx() {
  if (isViewOnly()) {
    return;
  }
  var vat = FormManager.getActualValue('vat');
  var vatInd = FormManager.getActualValue('vatInd');

  // CREATCMR-7944
  if (isPrivateScenario()) {
    FormManager.setValue('vatInd', 'N');
    FormManager.enable('vatInd');
    FormManager.setValue('vat', '');
    FormManager.readOnly('vat');
  }
  // CREATCMR-7165
  else if (isImportingFromQuickSearch()) {
    dojo.cookie('qs', 'N');
    FormManager.enable('vatInd');
    
    if (vat != '' && vatInd == '') {
      FormManager.setValue('vatInd', 'T');
    } else if (vat == '') {
  	  // CREATCMR-7980 vatInd not imported for update request.
  	  if (FormManager.getActualValue('reqType') != 'U') {
        FormManager.setValue('vatInd', '');
        FormManager.enable('vat');
  	  }
    }
  } else if (vatInd == 'N') {
    FormManager.readOnly('vat');
    FormManager.setValue('vat', '');
  }
  if('T'== FormManager.getActualValue('vatInd') && vat==''){
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');    
  }
  
  if ('E' == FormManager.getActualValue('vatInd')) {
    FormManager.removeValidator('vat', Validators.REQUIRED);
  }
}

function updateProspectLegalInd() {
  var CMRDataRdc = "";
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  var result = cmr.query("GET.CMR.DATARDC", {
      REQ_ID : reqId
    });
   if (result != null && result.ret1 != '' && result.ret1 != null) {
    CMRDataRdc = result.ret1 ;
  }
  if (CMRDataRdc != '' && CMRDataRdc.includes("P") && reqType == 'C') {
    FormManager.setValue('prospLegalInd','Y');
  }
}

/* Register WW Validators */
dojo.addOnLoad(function() {
  console.log('adding WW validators...');
  GEOHandler.LA = [ '613', '629', '631', '655', '661', '663', '681', '683', '731', '735', '781', '799', '811', '813', '815', '829', '869', '871' ];
  GEOHandler.COUNTRIES_FOR_GEN_TEMPLATE = [ '631', '866', '754', '755', '726', '862', '666', '724', '616', '615', '643', '720', '738', '744', '749', '714', '736', '778', '646', '796', '818', '834',
      '652', '856', '852', '790', '822', '838', '815', '661', '629', '864', '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700',
      '717', '718', '725', '745', '753', '764', '769', '770', '780', '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883',
      '758', '706', '760', '613', '655', '663', '681', '683', '731', '735', '781', '799', '811', '813', '829', '869', '871', '358', '359', '363', '603', '607', '620', '626', '644', '642', '651',
      '668', '677', '680', '693', '694', '695', '699', '704', '705', '707', '708', '740', '741', '752', '762', '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832', '849',
      '850', '865', '889', '618', '641', '846', '806', '702', '678', '624', '788', '848', '729', '649','858','766' ];
  GEOHandler.COUNTRIES_FOR_GEN_TEMPLATE_CRSSBORDER = [ '631', '866', '754', '724', '666', '726', '862', '755', '616', '615', '643', '738', '744', '749', '736', '778', '796', '818', '834', '652',
      '856', '852', '790', '822', '838', '815', '661', '629', '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718',
      '725', '745', '753', '764', '769', '770', '780', '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883', '613', '655',
      '663', '681', '683', '731', '735', '781', '799', '811', '813', '829', '869', '871', '358', '359', '363', '603', '607', '626', '644', '642', '651', '668', '693', '694', '695', '699', '704',
      '705', '708', '740', '741', '752', '768', '772', '787', '820', '821', '826', '849', '850', '865', '889', '618', '641', '729' ];
  GEOHandler.NO_ADDR_STD = [ '613', '629', '631', '655', '661', '663', '681', '683', '731', '735', '781', '799', '811', '813', '815', '829', '869', '871', '755', '754', '866', '621', '791', '640',
      '759', '839', '859', '726', '862', '666', '616', '615', '643', '720', '738', '744', '749', '714', '736', '778', '646', '796', '818', '834', '652', '856', '852', '790', '822', '838', '758',
      '864', '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770', '780',
      '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883', '706', '729', '358', '359', '363', '603', '607', '620', '626',
      '644', '642', '651', '668', '677', '680', '693', '694', '695', '699', '704', '705', '707', '707', '708', '740', '741', '752', '762', '762', '767', '768', '772', '787', '805', '808', '808',
      '820', '821', '823', '826', '832', '849', '850', '865', '889', '618', '724', '641', '846', '806', '702', '678', '624', '788', '760', '848', '729','858','766' ];

  GEOHandler.NO_ME_CEMEA = [ '631', '866', '754', '755', '726', '862', '666', '724', '616', '615', '643', '720', '738', '744', '749', '714', '736', '778', '646', '796', '818', '834', '652', '856',
      '852', '790', '822', '838', '815', '661', '629', '864', '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718',
      '725', '745', '753', '764', '769', '770', '780', '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883', '758', '706',
      '760', '613', '655', '663', '681', '683', '731', '735', '781', '799', '811', '813', '829', '869', '871', '358', '359', '363', '603', '607', '626', '644', '651', '668', '693', '694', '695',
      '699', '704', '705', '707', '708', '740', '741', '787', '820', '821', '826', '889', '618' ];

  GEOHandler.AFRICA = [ '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770',
       '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883' ];

  GEOHandler.GROUP1 = [ '724', '848', '618', '624', '788', '624', '649', '866', '754' ];
  GEOHandler.AllCountries =  ['229' ,'358' ,'359' ,'363' ,'373' ,'382' ,'383' ,'428' ,'433' ,'440' ,'443' ,'446' ,'461' ,'465' ,'479' ,'498' ,'602' ,'603' ,'607' ,'608' ,'610' ,'613' ,'614' ,'615' ,'616' ,'618' ,'619' ,'620' ,'621' ,'624' ,'626' ,'627' ,'629' ,'631' ,'635' ,'636' ,'637' ,'638' ,'640' ,'641' ,'642' ,'643' ,'644' ,'645' ,'646' ,'647' ,'649' ,'651' ,'652' ,'655' ,'656' ,'661' ,'662' ,'663' ,'666' ,'667' ,'668' ,'669' ,'670' ,'677' ,'678' ,'680' ,'681' ,'682' ,'683' ,'691' ,'692' ,'693' ,'694' ,'695' ,'698' ,'699' ,'700' ,'702' ,'704' ,'705' ,'706' ,'707' ,'708' ,'711' ,'713' ,'717' ,'718' ,'724' ,'725' ,'726' ,'729' ,'731' ,'733' ,'735' ,'736' ,'738' ,'740' ,'741' ,'742' ,'744' ,'745' ,'749' ,'750' ,'752' ,'753' ,'754' ,'755' ,'756' ,'758' ,'759' ,'760' ,'762' ,'764' ,'766' ,'767' ,'768' ,'769' ,'770' ,'772' ,'778' ,'780' ,'781' ,'782' ,'787' ,'788' ,'791' ,'796' ,'799' ,'804' ,'805' ,'806' ,'808' ,'810' ,'811' ,'813' ,'815' ,'818' ,'820' ,'821' ,'822' ,'823' ,'825' ,'826' ,'827' ,'829' ,'831' ,'832' ,'833' ,'834' ,'835' ,'838' ,'839' ,'840' ,'841' ,'842' ,'843' ,'846' ,'848' ,'849' ,'850' ,'851' ,'852' ,'853' ,'855' ,'856' ,'857' ,'858' ,'859' ,'862' ,'864' ,'865' ,'866' ,'869' ,'871' ,'876' ,'881' ,'883' ,'889' ,'897' ,'714' ,'720' ,'790' ,'675' ,'879' ,'880'];
  
  GEOHandler.registerWWValidator(addCMRSearchValidator);
  GEOHandler.registerWWValidator(addDnBSearchValidator);
  GEOHandler.registerWWValidator(addDnBMatchingAttachmentValidator);
  // Story 1185886 : Address Standardization is not required for LA countries
  // and Israel
  GEOHandler.skipTGMEForCountries(GEOHandler.NO_ADDR_STD);

  // exclude for US
  GEOHandler.registerValidator(addSoldToValidator, [ '897', '755', '726', '866', '754', '862', '666', '822', '838', '864', '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667',
      '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770', '780', '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851',
      '857', '876', '879', '880', '881', '883', '358', '359', '363', '603', '607', '620', '626', '644', '642', '651', '668', '677', '680', '693', '694', '695', '699', '704', '705', '707', '708',
      '740', '741', '752', '762', '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832', '849', '850', '865', '889', '618', '758', '760', '848', '649', '729', '678', '702', '806', '846' ], null, false, true);
  GEOHandler.registerWWValidator(addAddrStdValidator);
  // exclude for LA
  GEOHandler.registerWWValidator(addTaxCodesValidator, GEOHandler.LA, null, false, true);

  // exclude for JP
  // GEOHandler.registerWWValidator(addDPLCheckValidator,GEOHandler.ROLE_PROCESSOR);

  GEOHandler.registerValidator(addDPLCheckValidator, [ '760' ], GEOHandler.ROLE_PROCESSOR, true, true);

  // not required anymore as part of 1308975
  // GEOHandler.registerWWValidator(addCovBGValidator,
  // GEOHandler.ROLE_PROCESSOR);

  // For Legacy GR, CY and PT
  GEOHandler.registerValidator(validateCMRNumberForLegacy, [ SysLoc.CYPRUS, SysLoc.GREECE, SysLoc.SOUTH_AFRICA, ...GEOHandler.AFRICA , SysLoc.MALTA ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(validateExistingCMRNo, [ SysLoc.PORTUGAL, SysLoc.CYPRUS, SysLoc.GREECE, ...GEOHandler.AFRICA , SysLoc.MALTA], GEOHandler.ROLE_PROCESSOR, true);

  GEOHandler.addAfterConfig(initGenericTemplateHandler, GEOHandler.COUNTRIES_FOR_GEN_TEMPLATE);
  // exclude countries that will not be part of client tier logic
  GEOHandler.addAfterConfig(addClientTierDefaultLogic, [ SysLoc.HONG_KONG, SysLoc.MACAO, SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY, SysLoc.PORTUGAL,
      SysLoc.SPAIN, SysLoc.INDIA, SysLoc.SRI_LANKA, SysLoc.BANGLADESH, '616', '643', '646', '714', '720', '749', '778', '790', '796', '818', '834', '852', '856', '864', '373', '382', '383', '610',
      '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770', '780', '782', '804', '810', '825', '827',
      '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883', '358', '359', '363', '603', '607', '620', '626', '644', '642', '651', '668', '677', '680', '693',
      '694', '695', '699', '704', '705', '707', '708', '740', '741', '752', '762', '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832', '849', '850', '865', '889', '618',
      '706', '760', '758', '678', '702', '806', '846', '624', '788', '641', '848', '729', '724','858', '649', '613', '629', '631', '655', '661', '663', '681', '683', '731', '735', '781', '799', 
      '811', '813', '815', '829', '869', '871' ], true);
  GEOHandler.registerValidator(addCrossBorderValidator, GEOHandler.COUNTRIES_FOR_GEN_TEMPLATE_CRSSBORDER, null, true);

  /* 1427121 BDS Postal COde validation */
  GEOHandler.registerWWValidator(addGenericZIPValidator);
  // GEOHandler.registerValidator(addGenericZIPValidator,
  // GEOHandler.NO_ME_CEMEA, null, true);
  GEOHandler.NEW_EMEA = [ '624', '788', '706', SysLoc.Switzerland, SysLoc.Sweden, SysLoc.FINLAND, SysLoc.NORWAY, SysLoc.AUSTRIA, SysLoc.GERMANY, SysLoc.SPAIN ];
  GEOHandler.addAfterConfig(afterConfigForEMEA, GEOHandler.NEW_EMEA);
  GEOHandler.addAfterTemplateLoad(afterConfigForEMEA, GEOHandler.NEW_EMEA);

  // Postal Code validation for Ireland as Landed Country - CMR - 6033
  GEOHandler.addAddrFunction(addGenericPostalCodeValidator, GEOHandler.GROUP1);
  
  GEOHandler.registerWWValidator(addINACValidator);
  // Removing this for coverage-2023 as ISU -32 is no longer obsoleted
  // GEOHandler.registerWWValidator(addIsuCdObsoleteValidator);
  GEOHandler.addAfterConfig(updateProspectLegalInd,  GEOHandler.AllCountries);
  
  GEOHandler.addAfterConfig(vatIndOnChange, ['724', '848', '618', '624', '788', '624', '866', '754','678','702','806','846']);  
  GEOHandler.addAfterConfig(setToReadOnly,['724', '848', '618', '624', '788', '624', '866', '754','678','702','806','846']); 
  GEOHandler.registerWWValidator(addVatIndValidator);

  GEOHandler.VAT_RQD_CROSS_LNDCNTRY = [ 'AR', 'AT', 'BE', 'BG', 'BO', 'BR', 'CL', 'CO', 'CR', 'CY', 'CZ', 'DE', 'DO', 'EC', 'EG', 'ES', 'FR', 'GB', 'GR', 'GT', 'HN', 'HR', 'HU', 'IE', 'IL', 'IT',
    'LU', 'MT', 'MX', 'NI', 'NL', 'PA', 'PE', 'PK', 'PL', 'PT', 'PY', 'RO', 'RU', 'RS', 'SI', 'SK', 'SV', 'TR', 'UA', 'UY', 'ZA', 'VE', 'AO', 'MG', 'TZ','TW', 'LT', 'LV', 'EE', 'IS', 'GL', 'FO', 'SE', 'NO', 'DK', 'FI' ];
});
