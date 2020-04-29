/**
 * 
 */
/* Register IERP Javascript */
var _vatExemptHandler = null;
var _scenarioSubTypeHandler = null;
var _deClientTierHandler = null;
var _reqReasonHandler = null;

function afterConfigForDE() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var _custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    disableAutoProcForProcessor();
  });
  if (_custSubGrpHandler && _custSubGrpHandler[0]) {
    _custSubGrpHandler[0].onChange();
  }
  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      console.log(">>> RUNNING!!!!");
      if (dijit.byId('vatExempt').get('checked')) {
        console.log(">>> Process vatExempt remove * >> ");
        FormManager.resetValidations('vat');
      } else {
        console.log(">>> Process vatExempt add * >> ");
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      }
    });
  }

  if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
  }

  if (_deClientTierHandler == null) {
    _deClientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      if (FormManager.getActualValue('reqType') == 'C') {
        setISUValues(value);
      } else if (FormManager.getActualValue('reqType') == 'U') {
        setISUValuesOnUpdate(value);
      }
    });
  }

  if (_reqReasonHandler == null) {
    _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
      checkOrderBlk();
    });
  }
  _reqReasonHandler[0].onChange();

  if (!PageManager.isReadOnly() && FormManager.getActualValue('reqType') == 'C') {
    if (role == 'REQUESTER') {
      FormManager.readOnly('ordBlk');
    } else {
      FormManager.enable('ordBlk');
    }
  }

  // Disable address copying for GERMANY
  GEOHandler.disableCopyAddress();
}

function autoSetIBMDeptCostCenter() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custClass = FormManager.getActualValue('custClass');
  if (FormManager.getActualValue('cmrIssuingCntry') == '724' && FormManager.getActualValue('reqType') == 'C') {
    if (_custSubGrp != 'undefined' && _custClass != 'undefined' && _custClass != '') {
      if (_custSubGrp == 'INTAM' || _custSubGrp == 'INTSO' || _custSubGrp == 'INTIN') {
        FormManager.show('IbmDeptCostCenter', 'ibmDeptCostCenter');
        if (_custClass == '81') {
          FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, [ 'IbmDeptCostCenter' ], 'MAIN_IBM_TAB');
        } else if (_custClass == '85') {
          FormManager.resetValidations('ibmDeptCostCenter');
        }
      } else {
        FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
      }
    } else {
      FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
    }
  } else {
    FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
  }

}

function defaultCapIndicator() {
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.getField('capInd').set('checked', true);
    FormManager.readOnly('capInd');
  }
}

function disableVatExemptForScenarios() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  if (FormManager.getActualValue('reqType') == 'C' && _custSubGrp != 'undefined' && _custSubGrp != '') {
    if (_custSubGrp != 'COMME' && _custSubGrp != '3PADC' && _custSubGrp != 'GOVMT' && _custSubGrp != 'BROKR' && _custSubGrp != 'CROSS') {
      FormManager.disable('vatExempt');
    } else {
      FormManager.enable('vatExempt');
      autoSetTax();
    }
  }
}
function autoSetTax() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  if (dijit.byId('vatExempt').get('checked')) {
    FormManager.resetValidations('vat');
  } else {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
  }
}

function setPrivacyIndcReqdForProc() {
  if (_pagemodel.userRole.toUpperCase() == "PROCESSOR" && FormManager.getActualValue('reqType') == 'C') {
    FormManager.addValidator('privIndc', Validators.REQUIRED, [ 'PrivacyIndc' ], 'MAIN_CUST_TAB');
  }
}

function setISUValues(value) {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  var _custSubGrp = FormManager.getActualValue('custSubGrp');

  if (reqType != 'C') {
    return;
  }

  if (_custSubGrp != 'PRIPE' && _custSubGrp != 'COMME' && _custSubGrp != '3PADC' && _custSubGrp != 'IBMEM' && _custSubGrp != 'CROSS') {

    if (!value) {
      value = FormManager.getField('clientTier');
    }

    if (!PageManager.isReadOnly()) {
      FormManager.enable('isuCd');
    }

    isuValues = null;

    if (value == '7') {
      if (_custSubGrp != 'undefined' && _custSubGrp != '' && _custSubGrp != 'BUSPR') {
        isuValues = [ '5E', '31', '4A', '4F', '19', '04', '3T', '28', '5B', '21' ];
      } else if (_custSubGrp != 'undefined' && _custSubGrp != '' && _custSubGrp == 'BUSPR') {
        isuValues = [ '8B' ];
      } else {
        isuValues = [ '5E', '31', '4A', '4F', '19', '04', '3T', '28', '5B', '21', '60' ];
      }
    } else if (value == 'A' || value == '6') {
      isuValues = [ '34' ];
    } else if (value == 'B') {
      isuValues = [ '32' ];
    } else if (value == 'Z') {
      isuValues = [ '21' ];
    } else if (value == 'V') {
      isuValues = [ '34', '60' ];
    } else {
      if (PageManager.isReadOnly()) {
        FormManager.readOnly('isuCd');
      } else {
        FormManager.enable('isuCd');
      }
    }

    if (isuValues != null) {
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuValues);
      if (isuValues.length == 1) {
        FormManager.setValue('isuCd', isuValues[0]);
        FormManager.readOnly('isuCd');
      }
    } else {
      FormManager.resetDropdownValues(FormManager.getField('isuCd'));
    }
  } else if (_custSubGrp == 'IBMEM') {
    FormManager.readOnly('isuCd');
  } else if (_custSubGrp == 'COMME' || _custSubGrp == '3PADC') {
    FormManager.enable('isuCd');
  } else if (_custSubGrp == 'CROSS') {
    FormManager.readOnly('isuCd');
  }

}

function limitClientTierValuesOnCreate() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  if (_custSubGrp != 'undefined' && _custSubGrp != '') {
    if (_custSubGrp == 'COMME' || _custSubGrp == '3PADC' || _custSubGrp == 'BROKR' || _custSubGrp == 'GOVMT' || _custSubGrp == 'SENSI') {
      var clientTierValues = [ 'A', 'B', 'V', 'Z', '6', '7', 'T', 'S', 'C', 'N' ];
      if (clientTierValues != null) {
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTierValues);
      } else {
        FormManager.resetDropdownValues(FormManager.getField('clientTier'));
      }
    }
  }
}

function limitClientTierValuesOnUpdate() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'U') {
    return;
  }
  var clientTierValues = [ 'A', 'B', 'V', 'Z', '6', '7', 'T', 'S', 'C', 'N' ];
  if (clientTierValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTierValues);
  } else {
    FormManager.resetDropdownValues(FormManager.getField('clientTier'));
  }

}
// Defect 1370022: By Mukesh
function canRemoveAddress(value, rowIndex, grid) {
  console.log("Remove address button..");
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd;

  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    if (importInd == 'Y') {
      return false;
    } else {
      return true;
    }
  } else {
    return true;
  }
}
function canUpdateAddress(value, rowIndex, grid) {
  return true;
}
function canCopyAddress(value, rowIndex, grid) {
  return false;
}
function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  console.log(value + ' - ' + rowIndex);
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd;
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    if (importInd == 'Y') {
      return false;
    } else {
      return true;
    }
  } else {
    return true;
  }
}

function disableAutoProcForProcessor() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  if (_pagemodel.userRole.toUpperCase() == "PROCESSOR" && FormManager.getActualValue('reqType') == 'C' && _custSubGrp != 'undefined' && _custSubGrp != '' && _custSubGrp == 'BUSPR') {
    FormManager.show('DisableAutoProcessing', 'disableAutoProc');
    FormManager.enable('disableAutoProc');
    FormManager.getField('disableAutoProc').checked = false;
    FormManager.hide('DisableAutoProcessing', 'disableAutoProc');
    FormManager.show('DisableAutoProcessing', 'disableAutoProc');
  }
}

function setISUValuesOnUpdate(value) {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'U') {
    return;
  }
  if (!value) {
    value = FormManager.getField('clientTier');
  }
  if (!PageManager.isReadOnly()) {
    FormManager.enable('isuCd');
  }
  isuValues = null;
  if (value == '7') {
    isuValues = [ '5E', '31', '4A', '4F', '19', '04', '3T', '28', '5B', '8B', '21', '4D', '60' ];
  } else if (value == 'A' || value == '6') {
    isuValues = [ '34' ];
  } else if (value == 'B') {
    isuValues = [ '32' ];
  } else if (value == 'Z') {
    isuValues = [ '21' ];
  } else if (value == 'V') {
    isuValues = [ '34', '60' ];
  } else {
    if (PageManager.isReadOnly()) {
      FormManager.readOnly('isuCd');
    } else {
      FormManager.enable('isuCd');
    }
  }

  if (isuValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuValues);
    if (isuValues.length == 1) {
      FormManager.setValue('isuCd', isuValues[0]);
      FormManager.readOnly('isuCd');
    }
  } else {
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
  }
}

function restrictNonSoldToAddress(cntry, addressMode, saving, finalSave, force) {
  var scenarioType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
    if (scenarioType != undefined && scenarioType != '' && (scenarioType == 'PRIPE' || scenarioType == 'IBMEM')) {
      cmr.hideNode('radiocont_ZI01');
      cmr.hideNode('radiocont_ZP01');
      cmr.hideNode('radiocont_ZD01');
    } else {
      cmr.showNode('radiocont_ZI01');
      cmr.showNode('radiocont_ZP01');
      cmr.showNode('radiocont_ZD01');
    }
  }
}

function onSavingAddress(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> onSavingAddress ");
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  if ((finalSave || force) && cmr.addressMode) {
    var copyTypes = document.getElementsByName('copyTypes');
    var copyingToA = false;
    if (copyTypes != null && copyTypes.length > 0) {
      copyTypes.forEach(function(input, i) {
        if (input.value == 'ZS01' && input.checked) {
          copyingToA = true;
        }
      });
    }
    var addrType = FormManager.getActualValue('addrType');
    if ((addrType == 'ZS01' || copyingToA)) {
      if (reqType == 'C')
        autoSetAbbrevNmLogic();
    }

  }
}

function autoSetAbbrevNmLogic() {
  console.log("autoSetAbbrevNmLogic");
  var _abbrevNm = null;
  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
  };
  var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', qParams);
  var custNm1 = FormManager.getActualValue('custNm1');
  if (custNm1 == '') {
    custNm1 = result.ret1;
  }
  _abbrevNm = custNm1;

  if (_abbrevNm && _abbrevNm.length > 30) {
    _abbrevNm = _abbrevNm.substring(0, 30);
  }
  FormManager.setValue('abbrevNm', _abbrevNm);
}

function validateAddressTypeForScenario() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestId = FormManager.getActualValue('reqId');
        var reqType = FormManager.getActualValue('reqType');
        var scenarioType = FormManager.getActualValue('custSubGrp');
        if (reqType == 'C' && scenarioType != undefined && scenarioType != '' && (scenarioType == 'PRIPE' || scenarioType == 'IBMEM')) {
          var qParams = {
            REQ_ID : requestId,
          };
          var record = cmr.query('COUNT_NON_SOLD_TO_ADDR', qParams);
          var count = record.ret1;
          var scenarioDesc = "";
          if (scenarioType == 'PRIPE') {
            scenarioDesc = 'Private Person';
          } else {
            scenarioDesc = 'IBM Employee';
          }
          if (Number(count) >= 1) {
            return new ValidationResult(null, false, 'Only Sold To Address is allowed for ' + scenarioDesc + ' scenario. Please remove other addresses.');
          } else {
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addSoldToAddressValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var zs01ReqId = FormManager.getActualValue('reqId');
        var addrType = FormManager.getActualValue('addrType');
        qParams = {
          REQ_ID : zs01ReqId,
        };
        var record = cmr.query('GETZS01VALRECORDS', qParams);
        var zs01Reccount = record.ret1;
        if (addrType == 'ZS01' && Number(zs01Reccount) == 1 && cmr.addressMode != 'updateAddress') {
          return new ValidationResult(null, false, 'Only one Sold-To Address can be defined.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setAbbrevNameDEUpdate(cntry, addressMode, saving, finalSave, force) {
  var zs01Reccount = '';
  var zs01ReqId = FormManager.getActualValue('reqId');
  var addrType = FormManager.getActualValue('addrType');
  if (cmr.currentRequestType != 'U') {
    return new ValidationResult(null, true);
  }

  if (addrType != null && (addrType == 'ZS01' || FormManager.getField('addrType_ZS01').checked) && finalSave) {
    if (cmr.addressMode != 'updateAddress') {
      qParams = {
        REQ_ID : zs01ReqId,
      };
      var record = cmr.query('GETZS01VALRECORDS', qParams);
      zs01Reccount = record.ret1;
    }

    var addrSeq = FormManager.getActualValue('addrSeq');
    qParams = {
      REQ_ID : zs01ReqId,
      ADDR_SEQ : addrSeq,
    };
    var record = cmr.query('GETZS01OLDCUSTNAME', qParams);
    var oldCustNm = record.ret1;
    var currCustNm = FormManager.getActualValue('custNm1');

    var addrType = FormManager.getActualValue('addrType');
    if ((zs01Reccount == '' || (zs01Reccount != '' && Number(zs01Reccount) == 0)) && (oldCustNm != undefined && oldCustNm != '' && currCustNm != '' && currCustNm != oldCustNm)) {
      FormManager.setValue('abbrevNm', FormManager.getActualValue('custNm1').substring(0, 30));
    }
  }
}

function addReqReasonValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue == 'C') {
          return new ValidationResult(null, true);
        }
        var reqReason = FormManager.getActualValue('reqReason');
        if (!reqReason) {
          return new ValidationResult(null, true);
        }
        var ordBlk = FormManager.getActualValue('ordBlk');
        if (!ordBlk) {
          var reqId = FormManager.getActualValue('reqId');
          if (!reqReason) {
            return new ValidationResult(null, true);
          }
          var qParams = {
            REQ_ID : reqId
          };

          var result = cmr.query('GET.DATA_RDC.EMBARGO_BY_REQID_SWISS', qParams);
          if (result.ret1 == '88') {
            if (reqReason == 'TREC') {
              return new ValidationResult(null, true);
            } else {
              return new ValidationResult({
                id : 'reqReason',
                type : 'text',
                name : 'reqReason'
              }, false, 'Request reason must be \'Temporary Reactivate of Embargo Code\' for Order Block 88 removal.');
            }
          } else {
            return new ValidationResult(null, true);
          }
        } else if (ordBlk && reqReason == 'TREC') {
          return new ValidationResult({
            id : 'reqReason',
            type : 'text',
            name : 'reqReason'
          }, false, 'Request reason must NOT be \'Temporary Reactivate of Embargo Code\' if Order Block will not be removed.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}
function addOrderBlockValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var reqId = FormManager.getActualValue('reqId');
        var qParams = {
          REQ_ID : reqId
        };
        var result = cmr.query('GET.DATA_RDC.EMBARGO_BY_REQID_SWISS', qParams);

        var oldVal = result ? result.ret1 : '';
        var ordBlk = FormManager.getActualValue('ordBlk');

        if (oldVal == '88' && ordBlk != '' && ordBlk != '88') {
          return new ValidationResult({
            id : 'ordBlk',
            type : 'text',
            name : 'ordBlk'
          }, false, 'Order Block can only be 88 or blank when updating CMRs with Order Block 88.');
        }
        return new ValidationResult(null, true);

        // below not used, keeping for now
        if (!ordBlk || ordBlk == '88') {
          return new ValidationResult(null, true);
        } else {

          return new ValidationResult({
            id : 'ordBlk',
            type : 'text',
            name : 'ordBlk'
          }, false, 'Value of Order Block can only be 88, 94 or blank.');
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function checkOrderBlk() {
  var value = FormManager.getActualValue('reqReason');
  if (value != 'TREC')
    return;
  var reqId = FormManager.getActualValue('reqId');
  var ordBlk = FormManager.getActualValue('ordBlk');
  var qParams = {
    REQ_ID : reqId
  };

  var result = cmr.query('GET.DATA_RDC.EMBARGO_BY_REQID_SWISS', qParams);
  if (result.ret1 == '88' && !ordBlk) {
    // correct, no alert
  } else {
    FormManager.clearValue('reqReason');
    cmr
        .showAlert('This request reason can be chosen only if the CMR\'s Order Block is 88 and the new value on the request is blank.<br><br>Please set the value of Order Block to blank then choose the request reason again.');
    return;
  }
}

dojo.addOnLoad(function() {
  GEOHandler.DE = [ SysLoc.GERMANY ];
  console.log('adding DE validators...');
  GEOHandler.addAfterConfig(afterConfigForDE, GEOHandler.DE);
  GEOHandler.addAfterConfig(autoSetTax, GEOHandler.DE);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.DE);
  GEOHandler.addAddrFunction(onSavingAddress, GEOHandler.DE);
  GEOHandler.addAddrFunction(setAbbrevNameDEUpdate, GEOHandler.DE);
  // DENNIS: COMMENTED BECAUSE THIS IS IN DUPLICATE OF THE VALIDATOR REGISTERED
  // ON WW
  // GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.DE,
  // GEOHandler.ROLE_PROCESSOR, false, false);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(autoSetIBMDeptCostCenter, GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(disableVatExemptForScenarios, GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(setPrivacyIndcReqdForProc, GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(limitClientTierValuesOnCreate, GEOHandler.DE);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAfterTemplateLoad(setISUValues, GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(disableAutoProcForProcessor, GEOHandler.DE);
  GEOHandler.addAfterConfig(limitClientTierValuesOnUpdate, GEOHandler.DE);
  GEOHandler.addAfterConfig(setISUValuesOnUpdate, GEOHandler.DE);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.GERMANY, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.GERMANY ], null, true);
  GEOHandler.addAfterConfig(defaultCapIndicator, SysLoc.GERMANY);
  GEOHandler.addAfterConfig(disableAutoProcForProcessor, GEOHandler.DE);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.DE, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAddrFunction(restrictNonSoldToAddress, GEOHandler.DE);
  GEOHandler.registerValidator(validateAddressTypeForScenario, GEOHandler.DE, null, true);
  GEOHandler.registerValidator(addSoldToAddressValidator, GEOHandler.DE);
  GEOHandler.registerValidator(addOrderBlockValidator, GEOHandler.DE, null, true);
  GEOHandler.registerValidator(addReqReasonValidator, GEOHandler.DE, null, true);
});