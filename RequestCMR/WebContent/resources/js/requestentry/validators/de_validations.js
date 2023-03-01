/**
 * 
 */
/* Register IERP Javascript */
var _vatExemptHandler = null;
var _scenarioSubTypeHandler = null;
var _deClientTierHandler = null;
var _ISUHandler = null;
var _ISICHandler = null;
var _IMSHandler = null;
var _deIsuCdHandler = null;
var _subScenarioHandler = null;
var oldVatExempt = null;
var oldCustSubGrp = null;

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
      vatExemptIBMEmp();
    });
  }

  if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
  }

  if (_deClientTierHandler == null) {
    _deClientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      if (FormManager.getActualValue('reqType') == 'C') {
        setISUValues();
        setSboOnIMS(FormManager.getField('clientTier'));
      }
    });
  }

  if (_subScenarioHandler == null) {
    _subScenarioHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      setISUValues();
      // CREATCMR-7424_7425
      setAbbreviatedNameBasedOnAddressType();
    });
  }

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setSboOnIMS(FormManager.getField('isuCd'));
      if (!value) {
        value = FormManager.getActualValue('isuCd');
      }
      if (value == '32') {
        FormManager.setValue('clientTier', 'T');
      } else if (value == '34') {
        FormManager.setValue('clientTier', 'Q');
      } else if (value == '36') {
        FormManager.setValue('clientTier', 'Y');
      } else {
        FormManager.setValue('clientTier', '');
      }
    });
  }
  
  if (_IMSHandler == null) {
    _IMSHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setSboOnIMS(FormManager.getField('isicCd'));
    });
  }

  // Disable address copying for GERMANY
  GEOHandler.disableCopyAddress();
}

function saveVatExemptStat() {
  console.log(">>>> saveVatExemptStat");
  if (isParamtNull(oldVatExempt)) {
    oldVatExempt = dijit.byId('vatExempt').get('checked');
  }
}

function saveCustSubStat() {
  console.log(">>>> saveCustSubStat");
  if (isParamtNull(oldCustSubGrp)) {
    oldCustSubGrp = FormManager.getActualValue('custSubGrp');
  }
}

function vatExemptIBMEmp() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var vatExempt = dijit.byId('vatExempt').get('checked');
  saveCustSubStat();
  saveVatExemptStat();
  if (oldCustSubGrp == custSubGrp && oldVatExempt == vatExempt) {
    return;
  }

  if (FormManager.getActualValue('custSubGrp') == 'IBMEM') {
    FormManager.readOnly('vat');
    FormManager.setValue('vat', '');
    dijit.byId('vatExempt').set('checked', true);
  } else {
    FormManager.enable('vat');
    if (dijit.byId('vatExempt').get('checked')) {
      dijit.byId('vatExempt').set('checked', false);
      FormManager.resetValidations('vat');
      if (!dijit.byId('vatExempt').get('checked')) {
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      }
    }
  }
}

var oldIsicValue = null;
function saveOldIsic() {
  oldIsicValue = FormManager.getActualValue('isicCd');
}

function setSboOnIMS(value) {
  if (FormManager.getActualValue('reqType') != 'C' || (cmr.currentRequestType != undefined && cmr.currentRequestType != 'C') || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (value == undefined) {
    return;
  }
  if (value.id == 'isicCd' && oldIsicValue == FormManager.getActualValue('isicCd')) {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var ims = FormManager.getActualValue('subIndustryCd').substring(0, 1);
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  
  if (isuCd + clientTier != '34Q') {
    ims = '';
  }

  var result = cmr.query('DE.GET.SORTL_BY_ISUCTCIMS', {
    _qall : 'Y',
    ISU_CD : isuCd,
    CLIENT_TIER : clientTier,
    IMS : '%' + ims + '%'
  });

  if (result != null && Object.keys(result).length > 0 && Object.keys(result).length == 1) {
    FormManager.setValue('searchTerm', result[0].ret1);
  } else {
    FormManager.clearValue('searchTerm');
  }
}

function validateSBOValuesForIsuCtc() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('reqType') != 'C') {
          return;
        }
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var clientTier = FormManager.getActualValue('clientTier');
        var isuCd = FormManager.getActualValue('isuCd');
        var sbo = FormManager.getActualValue('searchTerm');
        var validSboList = [];
        var qParams = null;
        
        if (isuCd != '') {
          var results = null;
          if (isuCd + clientTier != '34Q') {
            qParams = {
              _qall : 'Y',
              ISU_CD : isuCd,
              CLIENT_TIER : clientTier,
              IMS : '%'
            };
            results = cmr.query('DE.GET.SORTL_BY_ISUCTCIMS', qParams);
          }
        }
        if (results == null || results.length == 0) {
          return new ValidationResult(null, true);
        } else {
          for (let i=0; i<results.length; i++) {
            validSboList.push(results[i].ret1);
          }
          if (!validSboList.includes(sbo)) {
            if (isuCd + clientTier == '32T') {
              return new ValidationResult(null, false, 
                  'The SBO provided is invalid for corresponding ISU+CTC.');
            } else {
              return new ValidationResult(null, false, 
                  'The SBO provided is invalid. It should be from the list: ' + validSboList);
            }
          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function lockCtcFieldOnIsu() {
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  var isuList = [ '34', '5K', '19', '3T', '4F' ];
  var reqType = FormManager.getActualValue('reqType');
  var userRole = _pagemodel.userRole.toUpperCase();
  var isuCd = FormManager.getActualValue('isuCd');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'IBMEM') {
    FormManager.setValue('clientTier', '');
  }
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
  var scenario3PADC = [ '3PA', 'DC', 'X3PA', 'XDC' ];
  if (FormManager.getActualValue('reqType') == 'C' && _custSubGrp != 'undefined' && _custSubGrp != '') {
    if (_custSubGrp != 'COMME' && !scenario3PADC.includes(_custSubGrp) && _custSubGrp != 'GOVMT' && _custSubGrp != 'BROKR' && _custSubGrp != 'CROSS') {
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
  var value = FormManager.getActualValue('clientTier');
  var scenario3PADC = [ '3PA', 'DC', 'X3PA', 'XDC' ];
  if (reqType != 'C') {
    return;
  }

  if (_custSubGrp != 'PRIPE' && _custSubGrp != 'COMME' && !scenario3PADC.includes(_custSubGrp) && _custSubGrp != 'IBMEM' && _custSubGrp != 'CROSS') {

    if (!value) {
      value = FormManager.getField('clientTier');
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
    } else if (value == 'Q') {
      isuValues = [ '34' ];
    }

    if (isuValues != null) {
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuValues);
      if (isuValues.length == 1) {
        FormManager.setValue('isuCd', isuValues[0]);
      }
    } else {
      FormManager.resetDropdownValues(FormManager.getField('isuCd'));
    }
  } else if (_custSubGrp == 'CROSS' && _pagemodel.userRole.toUpperCase() != "PROCESSOR") {
    FormManager.readOnly('isuCd');
  }
  // CREATCMR-710 Comments fix
  var role = FormManager.getActualValue('userRole').toUpperCase();
  lockIBMTabForDE();
}

function limitClientTierValuesOnCreate() {
  var reqType = null;
  var scenario3PADC = [ '3PA', 'DC', 'X3PA', 'XDC' ];
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  if (_custSubGrp != 'undefined' && _custSubGrp != '') {
    if (_custSubGrp == 'COMME' || scenario3PADC.includes(_custSubGrp) || _custSubGrp == 'BROKR' || _custSubGrp == 'GOVMT' || _custSubGrp == 'SENSI') {
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

// function setISUValuesOnUpdate(value) {
// var reqType = null;
// reqType = FormManager.getActualValue('reqType');
// if (reqType != 'U') {
// return;
// }
// if (!value) {
// value = FormManager.getField('clientTier');
// }
// if (!PageManager.isReadOnly()) {
// FormManager.enable('isuCd');
// }
// isuValues = null;
// if (value == '7') {
// isuValues = [ '5E', '31', '4A', '4F', '19', '04', '3T', '28', '5B', '8B',
// '21', '4D', '60' ];
// } else if (value == 'A' || value == '6') {
// isuValues = [ '34' ];
// } else if (value == 'B') {
// isuValues = [ '32' ];
// } else if (value == 'Z') {
// isuValues = [ '21' ];
// } else if (value == 'V') {
// isuValues = [ '34', '60' ];
// } else {
// if (PageManager.isReadOnly()) {
// FormManager.readOnly('isuCd');
// } else {
// FormManager.enable('isuCd');
// }
// }
//
// if (isuValues != null) {
// FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuValues);
// if (isuValues.length == 1) {
// FormManager.setValue('isuCd', isuValues[0]);
// FormManager.readOnly('isuCd');
// }
// } else {
// FormManager.resetDropdownValues(FormManager.getField('isuCd'));
// }
// }

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
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U' && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
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
        var systemId = FormManager.getActualValue('sourceSystId');
        var scenarioType3PADC = [ '3PA', 'DC', 'X3PA', 'XDC' ];
        if (reqType == 'C' && scenarioType != undefined && scenarioType != '' && (scenarioType == 'PRIPE' || scenarioType == 'IBMEM')) {
          var qParams = {
            REQ_ID : requestId,
          };
          var record = cmr.query('COUNT_NON_SOLD_TO_ADDR', qParams);

          // PayGo_check_DE_PrivateScenario
          var qParams = {
            SYST_ID : systemId,
          };
          var paygorecord = cmr.query('DE.PAYGO.CHECK', qParams);
          var countpaygo = paygorecord.ret1;
          if (Number(countpaygo) == 1) {
            return new ValidationResult(null, true);
          }
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
        } else if (reqType == 'C' && scenarioType != undefined && scenarioType != '' && scenarioType3PADC.includes(scenarioType)) {
          /*
           * Mandatory addresses ZS01/ZI01 *Sold-to *Installing (Install at)
           */
          var soldToCnt = getCountOfAddressByReqId(requestId, 'ZS01')
          var installingCnt = getCountOfAddressByReqId(requestId, 'ZI01');

          if (installingCnt == 0) {
            return new ValidationResult(null, false, 'For ' + ((scenarioType == '3PA' || scenarioType == 'X3PA') ? ' Third Party' : ' Data Center')
                + ' sub-scenarios installing address is mandatory. Please add it.');
          }
          if (soldToCnt == 0) {
            return new ValidationResult(null, false, 'Sold-to address is mandatory.');
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

    qParams = {
      REQ_ID : zs01ReqId,
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

function reqReasonOnChange() {
  var reqReason = FormManager.getActualValue('reqReason');
  var addressListIGF = [ 'ZP02', 'ZD02' ];
  for (var i = 0; i < addressListIGF.length; i++) {
    var addressType = addressListIGF[i];
    if (reqReason == 'IGF' && isZD01OrZP01ExistOnCMR(addressType)) {
      dojo.byId('radiocont_' + addressType).style.display = 'inline-block';
    } else {
      dojo.byId('radiocont_' + addressType).style.display = 'none';
    }
  }

  dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
    for (var i = 0; i < addressListIGF.length; i++) {
      var addressType = addressListIGF[i];
      if (value == 'IGF' && isZD01OrZP01ExistOnCMR(addressType)) {
        dojo.byId('radiocont_' + addressType).style.display = 'inline-block';
      } else {
        dojo.byId('radiocont_' + addressType).style.display = 'none';
      }
    }
  });
}

function isZD01OrZP01ExistOnCMR(addressType) {
  addressType = addressType.replace('2', '1');
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    if (record == null && _allAddressData != null && _allAddressData[i] != null) {
      record = _allAddressData[i];
    }
    var type = record.addrType;
    if (typeof (type) == 'object') {
      type = type[0];
    }
    var importInd = record.importInd[0];
    var reqType = FormManager.getActualValue('reqType');
    if ('U' == reqType && 'Y' == importInd && type == addressType) {
      return true;
    }
  }
  return false;
}

function restrictDuplicateAddr(cntry, addressMode, saving, finalSave, force) {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqReason = FormManager.getActualValue('reqReason');
        var addressType = FormManager.getActualValue('addrType');
        if (addressType == 'ZP02' || addressType == 'ZD02') {
          if (reqReason != 'IGF') {
            return new ValidationResult(null, false, 'Request Reason should be IGF.');
          }
        }
        var requestId = FormManager.getActualValue('reqId');
        var addressSeq = FormManager.getActualValue('addrSeq');
        var dummyseq = "xx";
        var showDuplicateIGFBillToError = false;
        var showDuplicateIGFInstallAtToError = false;
        var qParams;
        if (addressMode == 'updateAddress') {
          qParams = {
            REQ_ID : requestId,
            ADDR_SEQ : addressSeq,
            ADDR_TYPE : addressType
          };
        } else {
          qParams = {
            REQ_ID : requestId,
            ADDR_SEQ : dummyseq,
            ADDR_TYPE : addressType
          };
        }
        var result = cmr.query('GETADDRECORDSBYTYPE', qParams);
        var addCount = result.ret1;
        if (addressType != undefined && addressType != '' && addressType == 'ZP02' && cmr.addressMode != 'updateAddress') {
          showDuplicateIGFBillToError = Number(addCount) >= 1 && addressType == 'ZP02';
          if (showDuplicateIGFBillToError) {
            return new ValidationResult(null, false, 'Only one IGF Bill-To address is allowed.');
          }
        }

        if (addressType != undefined && addressType != '' && addressType == 'ZD02' && cmr.addressMode != 'updateAddress') {
          showDuplicateIGFInstallAtToError = Number(addCount) >= 1 && addressType == 'ZD02';
          if (showDuplicateIGFInstallAtToError) {
            return new ValidationResult(null, false, 'Only one IGF Ship-To address is allowed.');
          }
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function unlockCustGrpSubGrp() {
  var reqStatus = FormManager.getActualValue('overallStatus');
  var viewMode = FormManager.getActualValue('viewOnlyPage');

  if (reqStatus != 'Processing Create/Upd Pending' && viewMode != true) {
    FormManager.enable('custGrp');
    FormManager.enable('custSubGrp');
  }
}

function lockIBMTabForDE() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (reqType == 'C' && role == 'REQUESTER') {
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('buyingGroupId');
    FormManager.readOnly('globalBuyingGroupId');
    FormManager.readOnly('covId');
    FormManager.readOnly('geoLocationCode');
    FormManager.readOnly('dunsNo');
    if (custSubType != 'BUSPR') {
      FormManager.readOnly('ppsceid');
    } else {
      FormManager.enable('ppsceid');
    }
    FormManager.readOnly('soeReqNo');
    if (custSubType != 'INTIN' && custSubType != 'INTSO' && custSubType != 'INTAM') {
      FormManager.readOnly('ibmDeptCostCenter');
    } else {
      FormManager.enable('ibmDeptCostCenter');
    }
    FormManager.readOnly('custClass');
  }
  if (reqType == 'C' && role == 'PROCESSOR') {
    if (['INTIN', 'INTSO', 'INTAM', 'IBMEM', 'BUSPR', 'PRIPE'].includes(custSubType)) {
      FormManager.readOnly('searchTerm');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('clientTier');
    } else {
      FormManager.enable('searchTerm');
      FormManager.enable('isuCd');
      FormManager.enable('clientTier');
    }
  }
}

function validateDeptAttnBldg() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm3 = FormManager.getActualValue('custNm3');
        var custNm4 = FormManager.getActualValue('custNm4');
        var bldg = FormManager.getActualValue('bldg');
        var dept = FormManager.getActualValue('dept');
        if ((custNm3 != '' && (custNm3 == bldg || custNm3 == dept)) || (custNm4 != '' && (custNm4 == bldg || custNm4 == dept))) {
          return new ValidationResult(null, false, 'Department_ext and Building_ext must contain unique information.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (viewOnlyPage == 'true') {
    $('label[for="custNm1_view"]').text('Customer legal name');
    $('label[for="custNm2_view"]').text('Legal name continued');
    $('label[for="custNm3_view"]').text('Division/Department');
    $('label[for="custNm4_view"]').text('Attention To /Building/Floor/Office');
    $('label[for="addrTxt_view"]').text('Street Name And Number');
    $('label[for="bldg_view"]').text('Building_ext');
    $('label[for="dept_view"]').text('Department_ext');
  }
}

// CREATCMR-4293
function setCTCValues() {

  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  // Business Partner
  var custSubGrpForBusinessPartner = [ 'BUSPR' ];

  // Business Partner
  if (custSubGrpForBusinessPartner.includes(custSubGrp)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '8B') {
      FormManager.setValue('clientTier', '');
    }
  }

  // Internal AM, Internal - Internal, Internal SO
  var custSubGrpForInternal = [ 'INTAM', 'INTIN', 'INTSO' ];

  // Internal AM, Internal - Internal, Internal SO
  if (custSubGrpForInternal.includes(custSubGrp)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '21') {
      FormManager.setValue('clientTier', '');
    }
  }
}

function clientTierCodeValidator() {
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');

  if (((isuCode == '21' || isuCode == '8B' || isuCode == '5K') && reqType == 'C') || ((isuCode != '34' && isuCode != '32' && isuCode != '36') && reqType == 'U')) {
    if (clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept blank.');
    }
  } else if (isuCode == '34') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'Q') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\'\'.');
    }
  } else if (isuCode == '32') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'T') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'T\'\'.');
    }
  } else if (isuCode == '36') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'Y') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Y\'\'.');
    }
  } else if (isuCode != '36' || isuCode != '34' || isuCode != '32') {
    if (clientTierCode == '') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept blank.');
    }
  } else {
    if (clientTierCode == 'Q' || clientTierCode == 'Y' || clientTierCode == 'Y' || clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');
      $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\', \'Y\', \'T\' or blank.');
    }
  }
}
// CREATCMR-4293

function clientTierValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var clientTier = FormManager.getActualValue('clientTier');
        var isuCd = FormManager.getActualValue('isuCd');
        var reqType = FormManager.getActualValue('reqType');
        var valResult = null;

        var oldClientTier = null;
        var oldISU = null;
        var requestId = FormManager.getActualValue('reqId');

        if (reqType == 'C') {
          valResult = clientTierCodeValidator();
        } else {
          qParams = {
            REQ_ID : requestId,
          };
          var result = cmr.query('GET.CLIENT_TIER_EMBARGO_CD_OLD_BY_REQID', qParams);

          if (result != null && result != '') {
            oldClientTier = result.ret1 != null ? result.ret1 : '';
            oldISU = result.ret3 != null ? result.ret3 : '';

            if (clientTier != oldClientTier || isuCd != oldISU) {
              valResult = clientTierCodeValidator();
            }
          }
        }
        return valResult;
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateEnterpriseNum() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var enterprise = FormManager.getActualValue('enterprise');
        var letterNumber = /^[0-9a-zA-Z]+$/;
        if (enterprise != '') {

          if (enterprise.length >= 1 && enterprise.length != 6) {
            return new ValidationResult(null, false, 'Enterprise Number should be 6 characters long.');
          }

          if (enterprise.length >= 1 && !enterprise.match(letterNumber)) {
            return new ValidationResult({
              id : 'enterprise',
              type : 'text',
              name : 'enterprise'
            }, false, 'Enterprise Number should be alpha numeric.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

// CREATCMR-1815
function addLandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
}

function checkCmrUpdateBeforeImport() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var reqId = FormManager.getActualValue('reqId');
        var reqType = FormManager.getActualValue('reqType');
        var uptsrdc = '';
        var lastupts = '';

        if (reqType == 'C') {
          // console.log('reqType = ' + reqType);
          return new ValidationResult(null, true);
        }

        var resultsCC = cmr.query('GETUPTSRDC', {
          COUNTRY : cntry,
          CMRNO : cmrNo,
          MANDT : cmr.MANDT
        });

        if (resultsCC != null && resultsCC != undefined && resultsCC.ret1 != '') {
          uptsrdc = resultsCC.ret1;
          // console.log('lastupdatets in RDC = ' + uptsrdc);
        }

        var results11 = cmr.query('GETUPTSADDR', {
          REQ_ID : reqId
        });
        if (results11 != null && results11 != undefined && results11.ret1 != '') {
          lastupts = results11.ret1;
          // console.log('lastupdatets in CreateCMR = ' + lastupts);
        }

        if (lastupts != '' && uptsrdc != '') {
          if (uptsrdc > lastupts) {
            return new ValidationResult(null, false, 'This CMR has a new update , please re-import this CMR.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

// CREATCMR-7424_7425
function setAbbreviatedNameBasedOnAddressType() {
  console.log(">>>> setAbbreviatedNameBasedOnAddressType");
  var _reqId = FormManager.getActualValue('reqId');
  var subCustGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  var reqType = FormManager.getActualValue('reqType');
  var custNm1 = getCustNmByReqIdAndAddrTyp(_reqId, "ZI01");
  var custNm2 = getCustNmByReqIdAndAddrTyp(_reqId, "ZS01");

  if (reqType == 'U') {
    return;
  }

  if (custNm1 != undefined && custNm1 != '' && (subCustGrp == '3PA' || subCustGrp == 'X3PA')) {
    FormManager.setValue('abbrevNm', "C/O " + custNm1.substring(0, 20) + " 3P");
  } else if (custNm2 != undefined && custNm2 != '' && (subCustGrp == 'DC' || subCustGrp == 'XDC')) {
    FormManager.setValue('abbrevNm', custNm2.substring(0, 20) + " DC");
  } else if (((custNm1 == undefined || custNm1 == '') && (subCustGrp == '3PA' || subCustGrp == 'X3PA')) || ((custNm2 == undefined || custNm2 != '') && (subCustGrp == 'DC' || subCustGrp == 'XDC'))) {
    FormManager.setValue('abbrevNm', '');
  } else {
    autoSetAbbrevNmLogic();
  }
}

function getCountOfAddressByReqId(reqId, addrTyp) {
  console.log(">>>> getCountOfAddressByReqId");
  var count = 0;
  var countRecord = null;
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYP : addrTyp
  };
  countRecord = cmr.query('GET_COUNT_OF_RECORDS_BY_ADDRTYP', qParams);
  if (!isParamtNull(countRecord)) {
    count = countRecord.ret1;
  }
  return count;
}

function getCustNmByReqIdAndAddrTyp(reqId, addrTyp) {
  console.log(">>>> getCustNmByReqIdAndAddrTyp");
  var results = null;
  var custNmResult = null;
  var custNmParams = {
    REQ_ID : reqId,
    ADDR_TYPE : addrTyp
  };
  results = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', custNmParams);
  if (!isParamtNull(results)) {
    custNmResult = results.ret1;
  }
  return custNmResult;
}

function isParamtNull(param) {
  console.log(">>>> isParamtNull");
  var isNull = false;
  if (param == null || param == undefined || param == '') {
    isNull = true;
  }
  return isNull;
}

// CREATCMR-7424_7425
function similarAddrCheckValidator() {
  console.log('>>>>> similarAddrCheckValidator');
  FormManager.addFormValidator(
      (function() {
        return {
          validate : function() {
            var req_id = FormManager.getActualValue('reqId');
            var req_type = FormManager.getActualValue('reqType');
            var _custSubGrp = FormManager.getActualValue('custSubGrp');
            var scenario3PADC = [ '3PA', 'DC', 'X3PA', 'XDC' ];
            if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && scenario3PADC.includes(_custSubGrp)) {
              var record = null;
              var addrDupExists = [];
              var custNm1 = '';
              var custNm2 = '';
              var addrTxt = '';
              var city1 = '';
              var addrTxt2 = '';
              var stateProv = '';
              var landCntry = '';
              var importIndc = '';
              var dept = '';
              for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
                record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
                if (record == null && _allAddressData != null && _allAddressData[i] != null) {
                  record = _allAddressData[i];
                }
                custNm1 = record.custNm1[0];
                custNm2 = record.custNm2[0] != null ? record.custNm2[0] : '';
                addrTxt = record.addrTxt[0] != null ? record.addrTxt[0] : '';
                city1 = record.city1[0] != null ? record.city1[0] : '';
                stateProv = record.stateProv[0] != null ? record.stateProv[0] : '';
                addrTxt2 = record.addrTxt2[0] != null ? record.addrTxt2[0] : '';
                landCntry = record.landCntry[0];
                dept = record.dept[0] != null ? record.dept[0] : '';
                importIndc = record.importInd[0] != null ? record.importInd[0] : '';

                if (req_type == 'U' && importIndc == 'Y') {
                  continue; // skip the unique check for addresses in case of
                  // import for Update requests.
                }

                var resultMatches = getSimilarAddressMatches(req_id, custNm1, custNm2, addrTxt, addrTxt2, city1, stateProv, dept, landCntry);
                if (resultMatches > 1) {
                  addrDupExists.push(record.addrTypeText);
                }
              }
              if (addrDupExists.length > 0) {
                return new ValidationResult(null, false, 'Duplicate address details exist for Addresses ' + addrDupExists
                    + '. Please make sure Sold-To and Install-at address detail should be a unique.');
              } else {
                return new ValidationResult(null, true);
              }
            } else {
              return new ValidationResult(null, true);
            }
          }
        };
      })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function getSimilarAddressMatches(req_id, custNm1, custNm2, addrTxt, addrTxt2, city1, stateProv, dept, landCntry) {
  var results = null;
  var countMatches = 0;
  var qParams = {
    REQ_ID : req_id,
    CUST_NM1 : custNm1,
    CUST_NM2 : custNm2,
    ADDR_TXT : addrTxt,
    ADDR_TXT_2 : addrTxt2,
    CITY1 : city1,
    STATE_PROV : stateProv,
    DEPT : dept,
    LAND_CNTRY : landCntry
  };
  results = cmr.query('GET.SME_ADDR_RECORDS_DE', qParams);
  if (!isParamtNull(results)) {
    countMatches = results.ret1;
  }
  return countMatches;
}

dojo.addOnLoad(function() {
  GEOHandler.DE = [ SysLoc.GERMANY ];
  console.log('adding DE validators...');
  GEOHandler.addAfterConfig(afterConfigForDE, GEOHandler.DE);
  GEOHandler.addAfterConfig(autoSetTax, GEOHandler.DE);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.DE);
  GEOHandler.addAddrFunction(onSavingAddress, GEOHandler.DE);
  GEOHandler.addAddrFunction(setAbbrevNameDEUpdate, GEOHandler.DE);
  GEOHandler.addAddrFunction(addLandedCountryHandler, GEOHandler.DE);
  // DENNIS: COMMENTED BECAUSE THIS IS IN DUPLICATE OF THE VALIDATOR REGISTERED
  // ON WW
  // GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.DE,
  // GEOHandler.ROLE_PROCESSOR, false, false);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(unlockCustGrpSubGrp, GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(autoSetIBMDeptCostCenter, GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(disableVatExemptForScenarios, GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(setPrivacyIndcReqdForProc, GEOHandler.DE);
  // GEOHandler.addAfterTemplateLoad(limitClientTierValuesOnCreate,
  // GEOHandler.DE);
  GEOHandler.setRevertIsicBehavior(false);
  // GEOHandler.addAfterTemplateLoad(setISUValues, GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(disableAutoProcForProcessor, GEOHandler.DE);
  // GEOHandler.addAfterConfig(limitClientTierValuesOnUpdate, GEOHandler.DE);
  // GEOHandler.addAfterConfig(setISUValuesOnUpdate, GEOHandler.DE);
  GEOHandler.addAfterConfig(saveOldIsic, GEOHandler.DE);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.GERMANY, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.GERMANY ], null, true);
  GEOHandler.addAfterConfig(defaultCapIndicator, SysLoc.GERMANY);
  GEOHandler.addAfterConfig(disableAutoProcForProcessor, GEOHandler.DE);
  GEOHandler.addAfterConfig(reqReasonOnChange, GEOHandler.DE);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.DE, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAddrFunction(restrictNonSoldToAddress, GEOHandler.DE);
  GEOHandler.registerValidator(validateAddressTypeForScenario, GEOHandler.DE, null, true);
  GEOHandler.registerValidator(addSoldToAddressValidator, GEOHandler.DE);
  GEOHandler.registerValidator(addOrderBlockValidator, GEOHandler.DE, null, true);
  // GEOHandler.registerValidator(addReqReasonValidator, GEOHandler.DE, null,
  // true);
  GEOHandler.registerValidator(restrictDuplicateAddr, GEOHandler.DE, null, true);
  GEOHandler.registerValidator(validateDeptAttnBldg, GEOHandler.DE, null, true);
  GEOHandler.addAfterConfig(setAddressDetailsForView, SysLoc.GERMANY);
  // GEOHandler.addAfterTemplateLoad(setSboOnIMS, GEOHandler.DE);
//  GEOHandler.addAfterTemplateLoad(lockCtcFieldOnIsu, GEOHandler.DE);
//  GEOHandler.addAfterConfig(lockCtcFieldOnIsu, SysLoc.GERMANY);
  GEOHandler.addAfterTemplateLoad(vatExemptIBMEmp, GEOHandler.DE);

  // CREATCMR-4293
  GEOHandler.addAfterTemplateLoad(setCTCValues, [ SysLoc.GERMANY ]);
  GEOHandler.registerValidator(clientTierValidator, [ SysLoc.GERMANY ], null, true);
  GEOHandler.registerValidator(similarAddrCheckValidator, GEOHandler.DE, null, true);

  GEOHandler.addAfterConfig(lockIBMTabForDE, GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(lockIBMTabForDE, GEOHandler.DE);
//  GEOHandler.addAfterTemplateLoad(setSboOnIMS, GEOHandler.DE);
  GEOHandler.addAfterConfig(resetVATValidationsForPayGo, GEOHandler.DE);
  GEOHandler.addAfterTemplateLoad(resetVATValidationsForPayGo, GEOHandler.DE);
  GEOHandler.registerValidator(validateEnterpriseNum, GEOHandler.DE, null, true);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.DE, null, true);
  GEOHandler.registerValidator(validateSBOValuesForIsuCtc, GEOHandler.DE, null, true);
});