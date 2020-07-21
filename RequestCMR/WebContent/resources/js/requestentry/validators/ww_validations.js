/*
 * File: ww_validations.js
 * Description:
 * Contains the functions necessary to handle country/GEO specific validations.
 */

var _GBL_NO_INIT = false;
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
        if (result == '' || result.toUpperCase() == 'NOT DONE') {
          return new ValidationResult(null, false, 'CMR Search has not been performed yet.');
        }
        var reqType = FormManager.getActualValue('reqType');
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
        if (FormManager.getActualValue('dnbPrimary') != 'Y') {
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
        if (isSkipDnbMatching()) {
          return new ValidationResult(null, true);
        }
        var result = FormManager.getActualValue('findDnbResult');
        if (result == '' || result.toUpperCase() == 'NOT DONE') {
          return new ValidationResult(null, false, 'D&B Search has not been performed yet.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
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
  if (custGrp != null && custGrp != '' && custSubGrp != null && custSubGrp != '' && dnbPrimary == 'Y') {
    var qParams = {
      CNTRY : cntry,
      CUST_TYP : custGrp,
      CUST_SUB_TYP : custSubGroup,
      SUBREGION_CD : subRegionCd
    };
    var result = cmr.query("AUTO.SKIP_VERIFICATION_INDC", qParams);
    if (result.ret1 != null && result.ret1 == "Y") {
      return true;
    } else {
      qParams.CUST_SUB_GRP = "*";
      result = cmr.query("AUTO.SKIP_VERIFICATION_INDC", qParams);
      if (result.ret1 != null && result.ret1 == 'Y') {
        return true;
      } else {
        qParams.CUST_GRP = "*";
        result = cmr.query("AUTO.SKIP_VERIFICATION_INDC", qParams);
        if (result.ret1 != null && result.ret1 == 'Y') {
          return true;
        }
      }
    }
  } else if (dnbPrimary == 'N') {
    return true;
  }
  return false;
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
      } else if (value == 'V' || value == '4' || value == 'A' || value == '6' || value == 'E') {
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

        var vat = FormManager.getActualValue('vat');
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
              msg = result.errorMessage + '. Please refer to info bubble for the correct format.';
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
    };
  })(), null, 'frmCMR_addressModal');
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

function cmrNoValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var cmrNo = FormManager.getActualValue('cmrNo');
        // Condition For Prospect CMRs
        if (cmrNo.startsWith('P')){
          return;
        }
        var cmrNoRegEx = /^[0-9]*$/;
        if (cmrNo == '000000') {
          return new ValidationResult(null, false, 'CMR Number format error. Only digits are allowed Except -> 000000');
        } else if (cmrNo != '' && cmrNoRegEx.test(cmrNo)) {
          return new ValidationResult(null, true);
        } else if (cmrNo != undefined && cmrNo != '') {
          return new ValidationResult(null, false, 'CMR Number format error. Only digits are allowed.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function cmrNoValidatorForInternalScenario() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cmrNo = FormManager.getActualValue('cmrNo').substring(0, 2);
        var requestType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        // Condition For Create and Prospect CMRs
        if (requestType != 'C' || (cmrNo.startsWith('P'))) {
          return;
        }
        if ((cmrNo == '' || cmrNo == '99') && (custSubGrp == 'INTER' || custSubGrp == 'CRINT')) {
          return new ValidationResult(null, true);
        } else if (cmrNo != undefined && cmrNo != '99' && (custSubGrp == 'INTER' || custSubGrp == 'CRINT')) {
          return new ValidationResult(null, false, 'CMR Number format error. It Should Start with 99 For INTERNAL Scenario.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateExistCMRNum() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking requested cmr number...');
        var reqType = FormManager.getActualValue('reqType');
        var cmrNo = FormManager.getActualValue('cmrNo');
        // Condition For Prospect CMRs
        if (cmrNo.startsWith('P')){
          return;
        }
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (reqType == 'C' && cmrNo) {
          var exists = cmr.query('GETCMRNUMFORPROCESSOR', {
            CNTRY : cntry,
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
        return new ValidationResult({
          id : 'cmrNo',
          type : 'text',
          name : 'cmrNo'
        }, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
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
      '850', '865', '889', '618', '641', '846', '806', '702', '678', '624', '788', '848' ];
  GEOHandler.COUNTRIES_FOR_GEN_TEMPLATE_CRSSBORDER = [ '631', '866', '754', '724', '666', '726', '862', '755', '616', '615', '643', '738', '744', '749', '736', '778', '796', '818', '834', '652',
      '856', '852', '790', '822', '838', '815', '661', '629', '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718',
      '725', '745', '753', '764', '769', '770', '780', '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883', '613', '655',
      '663', '681', '683', '731', '735', '781', '799', '811', '813', '829', '869', '871', '358', '359', '363', '603', '607', '626', '644', '642', '651', '668', '693', '694', '695', '699', '704',
      '705', '708', '740', '741', '752', '768', '772', '787', '820', '821', '826', '849', '850', '865', '889', '618', '641' ];
  GEOHandler.NO_ADDR_STD = [ '613', '629', '631', '655', '661', '663', '681', '683', '731', '735', '781', '799', '811', '813', '815', '829', '869', '871', '755', '754', '866', '621', '791', '640',
      '759', '839', '859', '726', '862', '666', '616', '615', '643', '720', '738', '744', '749', '714', '736', '778', '646', '796', '818', '834', '652', '856', '852', '790', '822', '838', '758',
      '864', '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770', '780',
      '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883', '706', '729', '358', '359', '363', '603', '607', '620', '626',
      '644', '642', '651', '668', '677', '680', '693', '694', '695', '699', '704', '705', '707', '707', '708', '740', '741', '752', '762', '762', '767', '768', '772', '787', '805', '808', '808',
      '820', '821', '823', '826', '832', '849', '850', '865', '889', '618', '724', '641', '846', '806', '702', '678', '624', '788', '760', '848' ];

  GEOHandler.NO_ME_CEMEA = [ '631', '866', '754', '755', '726', '862', '666', '724', '616', '615', '643', '720', '738', '744', '749', '714', '736', '778', '646', '796', '818', '834', '652', '856',
      '852', '790', '822', '838', '815', '661', '629', '864', '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718',
      '725', '745', '753', '764', '769', '770', '780', '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883', '758', '706',
      '760', '613', '655', '663', '681', '683', '731', '735', '781', '799', '811', '813', '829', '869', '871', '358', '359', '363', '603', '607', '626', '644', '651', '668', '693', '694', '695',
      '699', '704', '705', '707', '708', '740', '741', '787', '820', '821', '826', '889', '618' ];

  GEOHandler.registerWWValidator(addCMRSearchValidator);
  GEOHandler.registerWWValidator(addDnBSearchValidator);
  // Story 1185886 : Address Standardization is not required for LA countries
  // and Israel
  GEOHandler.skipTGMEForCountries(GEOHandler.NO_ADDR_STD);

  // exclude for US
  GEOHandler.registerValidator(addSoldToValidator, [ '897', '755', '726', '866', '754', '862', '666', '822', '838', '864', '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667',
      '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770', '780', '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851',
      '857', '876', '879', '880', '881', '883', '358', '359', '363', '603', '607', '620', '626', '644', '642', '651', '668', '677', '680', '693', '694', '695', '699', '704', '705', '707', '708',
      '740', '741', '752', '762', '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832', '849', '850', '865', '889', '618', '758', '760', '848' ], null, false, true);
  GEOHandler.registerWWValidator(addAddrStdValidator);
  // exclude for LA
  GEOHandler.registerWWValidator(addTaxCodesValidator, GEOHandler.LA, null, false, true);

  // exclude for JP
  // GEOHandler.registerWWValidator(addDPLCheckValidator,GEOHandler.ROLE_PROCESSOR);
  GEOHandler.registerValidator(addDPLCheckValidator, [ '760' ], GEOHandler.ROLE_PROCESSOR, true, true);
  GEOHandler.registerValidator(addDPLCheckValidator, [ '862', '821' ], GEOHandler.ROLE_PROCESSOR, true, true);

  // not required anymore as part of 1308975
  // GEOHandler.registerWWValidator(addCovBGValidator,
  // GEOHandler.ROLE_PROCESSOR);
  
  //For Legacy PT,CY,GR 
  GEOHandler.registerValidator(cmrNoValidator, [ SysLoc.PORTUGAL, SysLoc.CYPRUS, SysLoc.GREECE ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(validateExistCMRNum, [ SysLoc.PORTUGAL, SysLoc.CYPRUS, SysLoc.GREECE ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(cmrNoValidatorForInternalScenario, [ SysLoc.PORTUGAL, SysLoc.CYPRUS, SysLoc.GREECE ], GEOHandler.ROLE_PROCESSOR, true);

  GEOHandler.addAfterConfig(initGenericTemplateHandler, GEOHandler.COUNTRIES_FOR_GEN_TEMPLATE);
  // exclude countries that will not be part of client tier logic
  GEOHandler.addAfterConfig(addClientTierDefaultLogic, [ SysLoc.HONG_KONG, SysLoc.MACAO, SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY, SysLoc.PORTUGAL,
      SysLoc.SPAIN, SysLoc.INDIA, SysLoc.SRI_LANKA, SysLoc.BANGLADESH, '616', '643', '646', '714', '720', '749', '778', '790', '796', '818', '834', '852', '856', '864', '373', '382', '383', '610',
      '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770', '780', '782', '804', '810', '825', '827',
      '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883', '358', '359', '363', '603', '607', '620', '626', '644', '642', '651', '668', '677', '680', '693',
      '694', '695', '699', '704', '705', '707', '708', '740', '741', '752', '762', '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832', '849', '850', '865', '889', '618',
      '706', '760', '758', '678', '702', '806', '846', '624', '788', '641', '848' ], true);
  GEOHandler.registerValidator(addCrossBorderValidator, GEOHandler.COUNTRIES_FOR_GEN_TEMPLATE_CRSSBORDER, null, true);

  /* 1427121 BDS Postal COde validation */
  GEOHandler.registerWWValidator(addGenericZIPValidator);
  // GEOHandler.registerValidator(addGenericZIPValidator,
  // GEOHandler.NO_ME_CEMEA, null, true);

  GEOHandler.registerWWValidator(addINACValidator);

  GEOHandler.VAT_RQD_CROSS_LNDCNTRY = [ 'AR', 'AT', 'BE', 'BG', 'BO', 'BR', 'CL', 'CO', 'CR', 'CY', 'CZ', 'DE', 'DO', 'EC', 'EG', 'ES', 'FR', 'GB', 'GR', 'GT', 'HN', 'HR', 'HU', 'IE', 'IL', 'IT',
      'LU', 'MT', 'MX', 'NI', 'NL', 'PA', 'PE', 'PK', 'PL', 'PT', 'PY', 'RO', 'RU', 'RS', 'SI', 'SK', 'SV', 'TR', 'UA', 'UY', 'ZA', 'VE' ];
});