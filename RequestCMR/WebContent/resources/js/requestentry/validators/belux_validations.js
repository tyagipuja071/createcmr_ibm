/* Register BELUX Javascripts */

var reqType = null;
var role = null;
var _reqReasonHandler = null;
function afterConfigForBELUX() {
  var cntryUse = FormManager.getActualValue('countryUse');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  var reqType = FormManager.getActualValue('reqType');
  var custLang = FormManager.getActualValue('custPrefLang');
  FormManager.readOnly('capInd');
  FormManager.readOnly('sensitiveFlag');
  FormManager.setValue('capInd', true);
  FormManager.resetValidations('enterprise');
  FormManager.removeValidator('inacCd', Validators.REQUIRED);

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  /*
   * if (custGrp != null && custGrp != '') { FormManager.clearValue('inacCd'); }
   */

  if ((custSubGrp.substring(2, 5) == 'INT' || custSubGrp == 'CBBUS' || custSubGrp.substring(2, 5) == 'PRI' || custSubGrp.substring(2, 5) == 'ISO' || custSubGrp == 'BECOM' || custSubGrp == 'BEDAT'
      || custSubGrp == 'LUCOM' || custSubGrp == 'LUDAT')) {
    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
  } else {
    FormManager.removeValidator('isicCd', Validators.REQUIRED);
  }

  if ((custSubGrp.substring(2, 5) == 'INT' || custSubGrp == 'CBBUS' || custSubGrp.substring(2, 5) == 'PRI' || custSubGrp.substring(2, 5) == 'ISO')) {
    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
    // FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client
    // Tier' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectionCd', Validators.REQUIRED, [ 'Collection Code' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('economicCd', Validators.REQUIRED, [ 'Economic Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('isuCd', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.removeValidator('collectionCd', Validators.REQUIRED);
    FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
    FormManager.removeValidator('economicCd', Validators.REQUIRED);
    FormManager.removeValidator('searchTerm', Validators.REQUIRED);

    if (custSubGrp == 'BECOM' || custSubGrp == 'BEDAT' || custSubGrp == 'BE3PA') {
      FormManager.addValidator('collectionCd', Validators.REQUIRED, [ 'Collection Code' ], 'MAIN_CUST_TAB');
    }

    if (custSubGrp.substring(2, 5) == '3PA' || custSubGrp == 'BEBUS' || custSubGrp == 'BECOM' || custSubGrp == 'BEDAT' || custSubGrp == 'LUBUS' || custSubGrp == 'LUCOM' || custSubGrp == 'LUDAT') {
      if (role == 'Processor') {
        FormManager.addValidator('economicCd', Validators.REQUIRED, [ 'Economic Code' ], 'MAIN_IBM_TAB');
      }
    }
  }

  var custSubGrpLst3 = custSubGrp.substring(2, 5);
  // collectionCd
  if (custSubGrp == 'LUCOM' || custSubGrp == 'LUBUS' || custSubGrp == 'LUPUB' || custSubGrp == 'LU3PA' || custSubGrp == 'LUDAT' || custSubGrp == 'CBCOM' || custSubGrp == 'CBBUS') {
    if (role == 'Requester') {
      FormManager.readOnly('collectionCd');
    } else if (role == 'Processor') {
      FormManager.enable('collectionCd');
    }
  }

  // isuCd & clientTier
  if (custSubGrpLst3 == 'BUS' || custSubGrpLst3 == 'PRI' || custSubGrpLst3 == 'INT' || custSubGrpLst3 == 'ISO') {
    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
    // FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client
    // Tier' ], 'MAIN_IBM_TAB');
  }
  // economicCd
  if (custSubGrpLst3 == 'BUS' || custSubGrpLst3 == 'INT' || custSubGrpLst3 == 'ISO') {
    FormManager.addValidator('economicCd', Validators.REQUIRED, [ 'Economic Code' ], 'MAIN_IBM_TAB');
  }

  if ((custLang == null || custLang == '') && reqType == 'U') {
    FormManager.setValue('custPrefLang', 'V');
  }
  if (custGrp == 'CROSS' || custGrp == 'LUCRO') {
    FormManager.setValue('custPrefLang', 'E');
  }

  if (role == 'Processor') {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
    if (!custSubGrp.includes('IBM')) {
      FormManager.enable('inacCd');
    }
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ], 'MAIN_IBM_TAB');
  } else {
    if (custSubGrpLst3 == 'INT' || custSubGrpLst3 == 'BUS' || custSubGrpLst3 == 'ISO') {
      FormManager.readOnly('inacCd');
    }
    if (role == 'Requester' && reqType == 'C') {
      FormManager.readOnly('abbrevNm');
      FormManager.readOnly('abbrevLocn');
    }
    FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);

  }
  if (reqType == 'C' && role == 'Processor') {
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  }
  if (reqType == 'U') {
    FormManager.resetValidations('taxCd1');
  }

  var landCntry = '';
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion && cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }

  if ((custSubGrp == 'BEISO' || custSubGrp == 'LUISO') && role == 'Requester') {
    FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, [ 'Department Number' ], 'MAIN_CUST_TAB');
  } else {
    FormManager.removeValidator('ibmDeptCostCenter', Validators.REQUIRED);
  }
  if ((custSubGrp == 'CBCOM' || custSubGrp == 'CBBUS') && cntryUse == '624LU') {
    FormManager.setValue('taxCd1', '15');
  }

  addHandlerForReqRsn();
  disableModeOfPayment();
  // setAccTemNumValueOnScenarios();
  addAccTemNumValidate();
  disableSBO();
  disableIBMTab();

  // CREATCMR-788
  addressQuotationValidatorBELUX();

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

function disableIBMTab() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  var cntryUse = FormManager.getActualValue('countryUse');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'C' && role == 'Requester') {
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('enterprise');

    FormManager.readOnly('bgId');
    FormManager.readOnly('gbgId');
    FormManager.readOnly('bgRuleId');
    FormManager.readOnly('covId');
    FormManager.readOnly('geoLocationCd');
    FormManager.readOnly('dunsNo');
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('economicCd');
  } else if (reqType == 'C' && role == 'Processor') {
    FormManager.enable('cmrNo');
  }
  if (custSubGrp.includes('IBM')) {
    FormManager.readOnly('enterprise');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('dunsNo');
    FormManager.readOnly('cmrNo');
  } else {
    FormManager.enable('enterprise');
    FormManager.enable('inacCd');
    FormManager.enable('searchTerm');
    if (role == 'Processor') {
      FormManager.enable('cmrNo');
      FormManager.enable('dunsNo');
    }
  }
}

function disableModeOfPayment() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    // do nothing for Update request
  } else if (reqType == 'C') {
    FormManager.readOnly('modeOfPayment');
    FormManager.clearValue('modeOfPayment');
  }
}

function setPPSCEIDRequired() {
  var reqType = FormManager.getActualValue('reqType');
  var subGrp = FormManager.getActualValue('custSubGrp');
  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (subGrp.includes('BP') || subGrp.includes('BUS')) {
    FormManager.enable('ppsceid');
    FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.clearValue('ppsceid');
    FormManager.readOnly('ppsceid');
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
  }
}

/*
 * function setAccTemNumValueOnScenarios() { var cntryUse =
 * FormManager.getActualValue('countryUse'); var custSubGrp =
 * FormManager.getActualValue('custSubGrp'); var reqType =
 * FormManager.getActualValue('reqType'); if (typeof (_pagemodel) !=
 * 'undefined') { reqType = FormManager.getActualValue('reqType'); role =
 * _pagemodel.userRole; }
 * 
 * if (FormManager.getActualValue('viewOnlyPage') == 'true') { return; }
 * 
 * if (reqType != 'C') { return; }
 * 
 * if (custSubGrp == '') { return; }
 * 
 * if (custSubGrp == _pagemodel.custSubGrp) { return; }
 * 
 * switch (custSubGrp) { case 'BECOM': case 'BEDAT': case 'BE3PA': case 'BEPUB':
 * case 'CBCOM': break; case 'BEBUS': if (role == 'Requester') {
 * FormManager.setValue('searchTerm', 'BP0000'); } else if (role == 'processor') { //
 * do nothing } break; case 'BEINT': FormManager.setValue('searchTerm',
 * 'BU0000'); break; case 'BEISO': FormManager.setValue('searchTerm', 'BU0000');
 * break; case 'BEPRI': FormManager.setValue('searchTerm', 'BU0000'); break;
 * case 'CBBUS': if (cntryUse == '624') { if (role == 'Requester') {
 * FormManager.setValue('searchTerm', 'BP0000'); } else if (role == 'processor') { //
 * do nothing } } else if (cntryUse == '624LU') { if (role == 'Requester') {
 * FormManager.setValue('searchTerm', 'LP0000'); } else if (role == 'processor') { //
 * do nothing } } break; case 'LU3PA': case 'LUCOM': case 'LUDAT': case 'LUPUB':
 * break; case 'LUBUS': if (role == 'Requester') {
 * FormManager.setValue('searchTerm', 'LP0000'); } else if (role == 'processor') { //
 * do nothing } break; case 'LUINT': FormManager.setValue('searchTerm',
 * 'LU0000'); break; case 'LUISO': FormManager.setValue('searchTerm', 'LU0000');
 * break; case 'LUPRI': FormManager.setValue('searchTerm', 'LU0000'); break;
 * default: break; } }
 */

/*
 * function setSortlOnScenarios() { var cntryUse =
 * FormManager.getActualValue('countryUse'); var custSubGrp =
 * FormManager.getActualValue('custSubGrp'); var reqType =
 * FormManager.getActualValue('reqType'); if (typeof (_pagemodel) !=
 * 'undefined') { reqType = FormManager.getActualValue('reqType'); role =
 * _pagemodel.userRole; }
 * 
 * if (FormManager.getActualValue('viewOnlyPage') == 'true') { return; }
 * 
 * if (reqType != 'C') { return; }
 * 
 * if (custSubGrp == '') { return; }
 * 
 * switch (custSubGrp) { case 'CBCOM': if (role == 'Requester') { if (cntryUse ==
 * '624') { FormManager.setValue('commercialFinanced', 'T0003601'); } else if
 * (cntryUse == '624LU') { FormManager.setValue('commercialFinanced',
 * 'T0003500'); } } break; case 'CBBUS': if (role == 'Requester') { if (cntryUse ==
 * '624') { FormManager.setValue('commercialFinanced', 'BP0000'); } else if
 * (cntryUse == '624LU') { FormManager.setValue('commercialFinanced', 'LP0000'); } }
 * break; default: break; } }
 */

function addAccTemNumValidate() {
  var cntryUse = FormManager.getActualValue('countryUse');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (reqType == 'U') {
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    return;
  }

  if (reqType != 'C') {
    return;
  }

  switch (custSubGrp) {
  case 'BECOM':
  case 'BEDAT':
  case 'BE3PA':
  case 'BEPUB':
  case 'CBCOM':
    if (role == 'Requester') {
      FormManager.enable('searchTerm');
      FormManager.removeValidator('searchTerm', Validators.REQUIRED);
    } else if (role == 'Processor') {
      FormManager.enable('searchTerm');
      FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    }
    break;
  case 'BEBUS':
    if (role == 'Requester') {
      FormManager.readOnly('searchTerm');
      FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    } else if (role == 'Processor') {
      FormManager.enable('searchTerm');
      FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    }
    break;
  case 'BEINT':
    FormManager.readOnly('searchTerm');
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    break;
  case 'BEISO':
    FormManager.readOnly('searchTerm');
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    break;
  case 'BEPRI':
    FormManager.readOnly('searchTerm');
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    break;
  case 'CBBUS':
    if (cntryUse == '624') {
      if (role == 'Requester') {
        FormManager.readOnly('searchTerm');
        FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
      } else if (role == 'Processor') {
        FormManager.enable('searchTerm');
        FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
      }
    } else if (cntryUse == '624LU') {
      if (role == 'Requester') {
        FormManager.readOnly('searchTerm');
        FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
      } else if (role == 'Processor') {
        FormManager.enable('searchTerm');
        FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
      }
    }
    break;
  case 'LU3PA':
  case 'LUCOM':
  case 'LUDAT':
  case 'LUPUB':
    if (role == 'Requester') {
      FormManager.enable('searchTerm');
      FormManager.removeValidator('searchTerm', Validators.REQUIRED);
    } else if (role == 'Processor') {
      FormManager.enable('searchTerm');
      FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    }
    break;
  case 'LUBUS':
    if (role == 'Requester') {
      FormManager.readOnly('searchTerm');
      FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    } else if (role == 'Processor') {
      FormManager.enable('searchTerm');
      FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    }
    break;
  case 'LUINT':
    FormManager.readOnly('searchTerm');
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    break;
  case 'LUISO':
    FormManager.readOnly('searchTerm');
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    break;
  case 'LUPRI':
    FormManager.readOnly('searchTerm');
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
    break;
  default:
    break;
  }
}

function disableSBO() {
  var cntryUse = FormManager.getActualValue('countryUse');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }

  FormManager.readOnly('salesBusOffCd');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (reqType != 'C') {
    return;
  }

  if (custSubGrp == _pagemodel.custSubGrp) {
    return;
  }

  if (custSubGrp != '') {
    if (custSubGrp == 'BEPRI' || custSubGrp == 'LUPRI') {
      FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);
    }
  }
}

function disableLandCntry() {
  var custType = FormManager.getActualValue('custGrp');
  var reqType = FormManager.getActualValue('reqType');
  if ((custType == 'LOCAL' || custType.substring(2, 5) == 'LOC') && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
  if (reqType == 'U') {
    if (FormManager.getActualValue('addrType') == 'ZS01' || FormManager.getActualValue('addrType') == 'ZP01') {
      FormManager.readOnly('landCntry');
    } else {
      FormManager.enable('landCntry');
    }
  }
}

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

function addCrossBorderValidatorBELUX() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        // only Create type will be validated
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }
        var reqId = FormManager.getActualValue('reqId');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var cntryRegion = FormManager.getActualValue('countryUse');
        var scenario = FormManager.getActualValue('custSubGrp');
        var mscenario = FormManager.getActualValue('custGrp');

        if (mscenario == 'CROSS') {
          scenario = 'CROSS';
        } else if (mscenario == ((cntryRegion.substring(3, 5) + "CRO"))) {
          scenario = 'CRO';
        }

        if (cntryRegion.length > cntry.length) {
          var defaultcntry = cntryRegion.substring(3, 5);
          var result = cmr.query('VALIDATOR.CROSSBORDER', {
            REQID : reqId
          });
          if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultcntry != '' && result.ret1 != defaultcntry && scenario != 'CRO') {
            return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should be \'' + defaultcntry + '\' for Non Cross-Border customers.');
          } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultcntry != '' && result.ret1 == defaultcntry && scenario == 'CRO') {
            return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should not be \'' + defaultcntry + '\' for Cross-Border customers.');
          }

        } else {
          var defaultLandCntry = FormManager.getActualValue('defaultLandedCountry');
          var result = cmr.query('VALIDATOR.CROSSBORDER', {
            REQID : reqId
          });
          if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS') {
            return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
          } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS') {
            return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setAbbrevNameLocn(cntry, addressMode, saving, finalSave, force) {
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    return;
  }
  if (role != 'Requester') {
    console.log("Processor, return");
    return;
  }
  if (finalSave || force || addressMode == 'ZS01') {
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
    var cmpnyName = FormManager.getActualValue('custNm1');
    var city1 = FormManager.getActualValue('city1');
    if (addrType == 'ZS01' || copyingToA) {
      FormManager.setValue('abbrevNm', cmpnyName);
      FormManager.setValue('abbrevLocn', city1);
    }
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);
  }
}

function setAbbrevNameLocnProc(cntry, addressMode, saving, finalSave, force) {
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    return;
  }
  if (role != 'PROCESSOR') {
    console.log("REQUESTER, return");
    return;
  }
  if (finalSave || force || addressMode == 'ZS01') {
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
    var cmpnyName = FormManager.getActualValue('abbrevNm');
    var city1 = FormManager.getActualValue('city1');
    if (addrType == 'ZS01' || copyingToA) {
      FormManager.setValue('abbrevNm', cmpnyName);
      FormManager.setValue('abbrevLocn', city1);
    }
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);
  }
}

/**
 * Lock Embargo Code field
 */
function lockEmbargo() {
  reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER' && reqType != 'U') {
    FormManager.readOnly('embargoCd');
  } else {
    FormManager.enable('embargoCd');
  }
}

/**
 * After config handlers
 */
var _ISUHandler = null;
var _ISICHandler = null;
var _CTCHandler = null;
var _IMSHandler = null;
var _vatExemptHandler = null;
var _ExpediteHandler = null;
var _AccountTeamHandler = null;

function addBeIsuHandler() {
  _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (!value) {
      value = FormManager.getActualValue('isuCd');
    }
    reqType = FormManager.getActualValue('reqType');
    if (value == '32') {
      FormManager.setValue('clientTier', 'T');
    } else if (value == '34') {
      FormManager.setValue('clientTier', 'Q');
    } else if (value == '36') {
      FormManager.setValue('clientTier', 'Y');
    } else {
      FormManager.setValue('clientTier', '');
    }

    if (reqType == 'Processor') {
      FormManager.enable('commercialFinanced');
    }
  });
}

function addHandlersForBELUX() {

  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    lockFields();
  });

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValues(value);
      // setClientTierValuesForUpdate();
      setSBOValuesForIsuCtc();
    });
  }

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setAccountTeamNumberValues(value);
      setSBOValuesForIsuCtc();
    });
  }

  if (_AccountTeamHandler == null) {
    _AccountTeamHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function(value) {
      // setINACValues(value);
      setEconomicCodeValues(value);
      setSBO();
      // setSORTL();

    });
  }

  if (_ISICHandler == null) {
    _ISICHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setCollectionCodeValues(value);
    });
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorBELUX();
    });
  }
  if (_IMSHandler == null && FormManager.getActualValue('cmrIssuingCntry')) {
    _IMSHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      setAccountTeamNumberValues();
    });
  }
  if (_ExpediteHandler == null) {
    _ExpediteHandler = dojo.connect(FormManager.getField('expediteInd'), 'onChange', function(value) {
      setExpediteReason();
    });
  }
}

function setExpediteReason() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('expediteInd') != 'Y') {
    FormManager.clearValue('expediteReason');
    // FormManager.readOnly('expediteReason');
  }
}

/* Vat Handler */
function setVatValidatorBELUX() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (custSubGrp == 'BEPRI' || custSubGrp == 'LUPRI') {
      // FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.readOnly('vat');
    } else if (custSubGrp == 'BEPUB' || custSubGrp == 'LUPUB') {
      FormManager.removeValidator('vat', Validators.REQUIRED);
      return;
    }
    if (custSubGrp.includes('IBM')) {
      FormManager.readOnly('vat');
    }
    if (dijit.byId('vatExempt').get('checked')) {
      FormManager.clearValue('vat');
    }
    // if (!dijit.byId('vatExempt').get('checked')) {
    if (undefined != dijit.byId('vatExempt') && !dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
      FormManager.enable('vat');
    }
  }
}

/**
 * Set ISU drop down list on update request
 */
function setISUDropDown() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var reqType = FormManager.getActualValue('reqType');
  if (reqType == '') {
    window.setTimeout('setISUDropDown()', 500);
  } else {
    if (reqType != 'U') {
      return;
    }
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    var isuCds = [];
    if (cntry != '') {
      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry
      };
      var results = cmr.query('GET.ISULIST.UPDATE', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          isuCds.push(results[i].ret1);
        }
        if (isuCds != null) {
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCds);
        }
      }
    }
  }
}

/**
 * Set Client Tier drop down list on update request
 */
function setClientTierDropDown() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var reqType = FormManager.getActualValue('reqType');
  if (reqType == '') {
    window.setTimeout('setClientTierDropDown()', 500);
  } else {
    if (reqType != 'U') {
      return;
    }
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    var clientTiers = [];
    if (cntry != '') {
      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry
      };
      var results = cmr.query('GET.CTCLIST.UPDATE', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          clientTiers.push(results[i].ret1);
        }
        if (clientTiers != null) {
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTiers);
        }
      }
    }
  }
}

/**
 * Set Client Tier Value
 */
function setClientTierValues(isuCd) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  isuCd = FormManager.getActualValue('isuCd');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var clientTiers = [];
  if (isuCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCd + '%'
    };
    var results = cmr.query('GET.CTCLIST.BYISU', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        clientTiers.push(results[i].ret1);
      }
      if (clientTiers != null) {
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTiers);
        if (clientTiers.length == 1) {
          // CREATCMR-4293
          // FormManager.setValue('clientTier', clientTiers[0]);
        }
      }
    }
  }
}

/*
 * Set Account Team Number Values based on isuCTC
 */
function setAccountTeamNumberValues(clientTier) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var clientTier = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  var ims = FormManager.getActualValue('subIndustryCd');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);

  var accountTeamNumber = [];
  if (isuCd != '') {
    var isuCtc = isuCd + clientTier;
    var qParams = null;
    var results = null;

    // 32S changed to 34Q on 2021 2H Coverage
    // Account Team Number will be based on IMS for 32S
    // if (ims != '' && ims.length > 1 && (isuCtc == '32S')) {
    if (ims != '' && ims.length > 1 && (isuCtc == '34Q')) {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry + geoCd,
        ISU : '%' + isuCd + clientTier + '%',
        CLIENT_TIER : '%' + ims.substring(0, 1) + '%'
      };
      results = cmr.query('GET.SRLIST.BYISUCTC', qParams);
    } else {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry + geoCd,
        ISU : '%' + isuCd + clientTier + '%'
      };
      results = cmr.query('GET.SRLIST.BYISU', qParams);
    }

    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        accountTeamNumber.push(results[i].ret1);
      }
      FormManager.limitDropdownValues(FormManager.getField('searchTerm'), accountTeamNumber);
      if (accountTeamNumber.length == 1) {
        FormManager.setValue('searchTerm', accountTeamNumber[0]);
      }
    }
  }
}

/*
 * NL - INAC Code based on AccountTeamNumber Values
 */
function setINACValues(searchTerm) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  searchTerm = FormManager.getActualValue('searchTerm');

  var inacCode = [];
  if (searchTerm != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      SEARCHTERM : '%' + searchTerm + '%'
    };
    var results = cmr.query('GET.INACLIST.BYST', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        inacCode.push(results[i].ret1);
      }
      if (inacCode != null) {
        FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCode);
        if (inacCode.length == 1) {
          FormManager.setValue('inacCd', inacCode[0]);
        }
        /*
         * if (inacCode.length == 0) { FormManager.setValue('inacCd', ''); }
         */
      }
    }
  }
}

/*
 * NL - EconomicCode based on AccountTeamNumber Values
 */
function setEconomicCodeValues(searchTerm) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var role = _pagemodel.userRole;
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var searchTerm = FormManager.getActualValue('searchTerm');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
  var custSubGrp = FormManager.getActualValue('custSubGrp').substring(2, 5);
  var custSGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');

  if (custSGrp == '') {
    return;
  }

  if ((custSGrp == _pagemodel.custSubGrp) && (searchTerm == _pagemodel.searchTerm)) {
    return;
  }

  var economicCode = [];
  if (searchTerm != '' && (custSubGrp == 'COM' || custSGrp == 'BEDAT' || custSubGrp == 'PUB' || custSubGrp == '3PA' || custSGrp == 'LUDAT')) {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry + geoCd,
      REP_TEAM_CD : '%' + searchTerm + '%'
    };
    var results = cmr.query('GET.ECONOMICLIST.BYST', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        economicCode.push(results[i].ret1);
      }
      if (economicCode != null) {
        FormManager.limitDropdownValues(FormManager.getField('economicCd'), economicCode);
        if (economicCode.length == 1) {
          FormManager.setValue('economicCd', economicCode[0]);
        }
        if (economicCode.length == 0) {
          FormManager.setValue('economicCd', '');
        }
      }
    }
  }
  if (custSubGrp == 'PUB' || custSubGrp == 'COM' || custSGrp == 'BEDAT' || custSubGrp == '3PA' || custSGrp == 'LUDAT') {
    // FormManager.setValue('economicCd', 'K11');
    if ('32' == isuCd && 'N' == clientTier) {
      FormManager.setValue('economicCd', 'K43');
    } else if ('5B' == isuCd && '7' == clientTier) {
      FormManager.setValue('economicCd', 'K44');
    }
  } else if (custSGrp == 'CBBUS' || custSubGrp == 'BUS') {
    FormManager.setValue('economicCd', 'K49');
    if (role == 'Requester')
      FormManager.readOnly('economicCd');
  } else if (custSubGrp == 'INT') {
    FormManager.setValue('economicCd', 'K81');
    FormManager.readOnly('economicCd');
  } else if (custSubGrp == 'ISO') {
    FormManager.setValue('economicCd', 'K85');
    FormManager.readOnly('economicCd');
  } else if (custSubGrp == 'PRI') {
    FormManager.setValue('economicCd', 'K60');
  }
}

/*
 * Set Collection Code Values based On ISIC
 */
function setCollectionCodeValues(isicCd) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
  var isicCd = FormManager.getActualValue('isicCd');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var countryUse = FormManager.getActualValue('countryUse');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');

  if (custSubGrp == '') {
    return;
  }

  if ((isicCd == _pagemodel.isicCd) && (custSubGrp == _pagemodel.custSubGrp)) {
    return;
  }

  var collectionCode = [];

  if (countryUse == '624LU') {
    if (isicCd != '') {
      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry + geoCd,
        REP_TEAM_CD : '%' + isicCd + '%'
      };
      var results = cmr.query('GET.CCLIST.BYISIC', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          collectionCode.push(results[i].ret1);
        }
        if (collectionCode != null) {
          FormManager.limitDropdownValues(FormManager.getField('collectionCd'), collectionCode);

          if (collectionCode.length == 1) {
            FormManager.setValue('collectionCd', collectionCode[0]);
          }
        }
      }
    }
  } else if (countryUse == '624') {
    var colleCd32SList = [];
    if (isicCd != '') {
      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry + geoCd
      };
      var results = cmr.query('GET.CCLIST.BYCNTRY', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          colleCd32SList.push(results[i].ret1);
        }
        if (colleCd32SList != null) {
          FormManager.limitDropdownValues(FormManager.getField('collectionCd'), colleCd32SList);
        }
      }

      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry + geoCd,
        REP_TEAM_CD : '%' + isicCd + '%'
      };
      var results = cmr.query('GET.CCLIST.BYISIC', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          collectionCode.push(results[i].ret1);
        }
        if (collectionCode != null && collectionCode.length == 1) {
          FormManager.setValue('collectionCd', collectionCode[0]);
        }
      }
    }
  }

}

/*
 * Set Preferred Language for 624LU
 */
function setPreferredLanguage() {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('countryUse') == '') {
    window.setTimeout('setPreferredLanguage()', 500);
  } else {
    FormManager.enable('custPrefLang');
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
  }

}

function addAbbrevLocnLengthValidator() {
  reqType = FormManager.getActualValue('reqType');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevLocn = FormManager.getActualValue('abbrevLocn');
        if (reqType != 'U' && role == 'Requester' && _abbrevLocn.length != 12) {
          return new ValidationResult({
            id : 'abbrevLocn',
            type : 'text',
            name : 'abbrevLocn'
          }, false, 'The length of Abbreviated Location should be 12 characters.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAbbrevNameLengthValidator() {
  reqType = FormManager.getActualValue('reqType');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevName = FormManager.getActualValue('abbrevNm');
        if (reqType != 'U' && role == 'Requester' && _abbrevName.length != 22) {
          return new ValidationResult({
            id : 'abbrevNm',
            type : 'text',
            name : 'abbrevNm'
          }, false, 'The length of Abbreviated Name should be 22 characters.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function checkForBnkruptcy() {
  reqType = FormManager.getActualValue('reqType');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _reqreason = FormManager.getActualValue('reqReason');
        if (reqType != 'U' && role == 'Requester' && _reqreason == 'BKSC') {
          return new ValidationResult(null, false, 'The Request Reason cannot be chosen as "Bankruptcy" for Create requests.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function chngesAfterBnkrptcy() {
  reqType = FormManager.getActualValue('reqType');
  var reqRsn = FormManager.getActualValue('reqReason');
  if (reqRsn == 'BKSC' && reqType == 'U') {
    FormManager.setValue('collectionCd', 'TCBE99');
  }
}

function addHandlerForReqRsn() {
  if (_reqReasonHandler == null) {
    _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
      chngesAfterBnkrptcy();
    });
  }
}

function setINACfrLux() {
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
}

/* 1430539 - do not allow delete of imported addresses on update requests */

function canRemoveAddress(value, rowIndex, grid) {
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd[0];
  var reqType = FormManager.getActualValue('reqType');
  if ('U' == reqType && 'Y' == importInd) {
    return false;
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  return canRemoveAddress(value, rowIndex, grid);
}

/*
 * Mandatory addresses ZS01/ZP01/ZI01 *Billing (Sold-to) *Installing (Install
 * at) *Mailing - not flowing into RDC!!
 */
function addBELUXAddressTypeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Sold-to, Mail-To, Bill-to address are mandatory. Only one address for each address type should be defined when sending for processing.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var installAtCnt = 0;
          var billToCnt = 0;
          var mailToCnt = 0;
          var shipToCnt = 0;
          var soldToCnt = 0;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (type == 'ZI01') {
              installAtCnt++;
            } else if (type == 'ZP01') {
              billToCnt++;
            } else if (type == 'ZS01') {
              soldToCnt++;
            } else if (type == 'ZD01') {
              shipToCnt++;
            } else if (type == 'ZS02') {
              mailToCnt++;
            }
          }
          if (soldToCnt == 0 || mailToCnt == 0 || billToCnt == 0) {
            return new ValidationResult(null, false, 'Sold-to, Mail-To, Bill-To address are mandatory.');
          } else if (billToCnt > 1) {
            return new ValidationResult(null, false, 'Only one Bill-to address can be defined. Please remove the additional Bill-to address.');
          } else if (mailToCnt > 1) {
            return new ValidationResult(null, false, 'Only one Mail-To address can be defined. Please remove the additional Mail-to address.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addAddressFieldValidators() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrTxt = FormManager.getActualValue('addrTxt');
        var poBox = FormManager.getActualValue('poBox');

        var addrFldCnt = 0;
        var computedLength = addrTxt;
        if (FormManager.getActualValue('poBox') != '') {
          addrFldCnt++;
          computedLength += poBox;
        }
        if (FormManager.getActualValue('addrTxt') != '') {
          addrFldCnt++;
        }

        if (addrFldCnt == 1) {
          if (poBox != null && poBox != '' && poBox.length > 23) {
            return new ValidationResult(null, false, 'PO BOX should not exceed 23 characters.');
          } else if (addrTxt != null && addrTxt != '' && addrTxt.length > 30) {
            return new ValidationResult(null, false, 'Street Address should not exceed 30 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // post code + city
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var city = FormManager.getActualValue('city1');
        var postCd = FormManager.getActualValue('postCd');
        var val = postCd + ' ' + city;
        if (val.length > 30) {
          return new ValidationResult(null, false, 'Total computed length of City and Postal Code should not exceed 30 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // Street and PO BOX
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        // if (FormManager.getActualValue('reqType') != 'C') {
        // return;
        // }
        var addrFldCnt1 = 0;
        if (FormManager.getActualValue('poBox') != '') {
          addrFldCnt1++;
        }
        if (FormManager.getActualValue('addrTxt') != '') {
          addrFldCnt1++;
        }
        if (addrFldCnt1 < 1) {
          return new ValidationResult(null, false, 'For Street Address and PO Box, atleast one should be filled.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // only 1 out of 3 can be filled
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm3 = FormManager.getActualValue('custNm3');
        var poBox = FormManager.getActualValue('poBox');
        var attPerson = FormManager.getActualValue('custNm4');
        var addrFldCnt1 = 0;
        if (custNm3 != '') {
          addrFldCnt1++;
        }
        if (poBox != '') {
          addrFldCnt1++;
        }
        if (attPerson != '') {
          addrFldCnt1++;
        }
        if (addrFldCnt1 > 1) {
          return new ValidationResult(null, false, 'Customer Name 3, Attention person and PO Box - only 1 out of 3 can be filled.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // ALL BELUX POBOX
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var cntry = FormManager.getActualValue('landCntry');
        if (cntry != '') {
          var POBox = FormManager.getActualValue('poBox');
          if (isNaN(POBox)) {
            return new ValidationResult(null, false, 'PO Box should be Numeric.');
          }

        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setVatInfoBubble() {
  var reqId = FormManager.getActualValue('reqId');
  var vatCntry = null;
  var infoText = "";
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
    var landCntry = null;
    landCntry = FormManager.getActualValue('countryUse');
    if (landCntry != null) {
      vatCntry = landCntry;
      switch (vatCntry) {
      case "624LU":
        infoText = "For Local customers, insert country prefix in front of the VAT number (LU12345678)";
        break;
      default:
        infoText = "For Local customers, insert country prefix in front of the VAT number (BE0123456789)";
      }

    }

    document.getElementById('vatInfoBubble').getElementsByClassName('cmr-info-bubble')[0].setAttribute('title', infoText);

  }
  return;
}

function setAbbrvNmLoc() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqParam = null;

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId
    };
  }
  var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
  var city;
  var abbrevLocn = null;
  var abbrvNm = custNm.ret1;
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');

  if (custSubGrp == 'CBCOM' || custGrp == 'LOCAL' || custGrp.substring(2, 5) == 'LOC') {
    city = cmr.query('ADDR.GET.CITY1.BY_REQID', reqParam);
    abbrevLocn = city.ret1;
  } else {
    city = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', reqParam);
    abbrevLocn = getLandCntryDesc(city.ret1);
  }

  if (abbrvNm && abbrvNm.length > 22) {
    abbrvNm = abbrvNm.substring(0, 22);
  }
  if (abbrevLocn && abbrevLocn.length > 12) {
    abbrevLocn = abbrevLocn.substring(0, 12);
  }

  if (abbrevLocn != null && custSubGrp.substring(2, 5) != 'SOF') {
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }
  if (abbrvNm != null) {
    FormManager.setValue('abbrevNm', abbrvNm);
  }
}

// CMR - 1282

function setAbbrvNmLocFrProc() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqParam = null;

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  if (role != 'PROCESSOR') {
    return;
  }

  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId
    };
  }
  var custNm = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', reqParam);
  var city;
  var abbrevLocn = null;
  var abbrvNm = custNm.ret1;
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');

  if (custSubGrp == 'CBCOM' || custGrp == 'LOCAL' || custGrp.substring(2, 5) == 'LOC') {
    city = cmr.query('ADDR.GET.CITY1.BY_REQID', reqParam);
    abbrevLocn = city.ret1;
  } else {
    city = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', reqParam);
    abbrevLocn = getLandCntryDesc(city.ret1);
  }

  if (abbrvNm && abbrvNm.length > 22) {
    abbrvNm = abbrvNm.substring(0, 22);
  }
  if (abbrevLocn && abbrevLocn.length > 12) {
    abbrevLocn = abbrevLocn.substring(0, 12);
  }

  if (abbrevLocn != null && custSubGrp.substring(2, 5) != 'SOF') {
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }
  if (abbrvNm != null) {
    FormManager.setValue('abbrevNm', abbrvNm);
  }
}

function addBELUXVATValidator(cntry, tabName, formName, aType) {
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
          } else {
            ret = cmr.query('VAT.GET_ZS01_CNTRY', {
              REQID : FormManager.getActualValue('reqId'),
              TYPE : 'ZS01'
            });
            if (ret && ret.ret1 && ret.ret1 != '') {
              zs01Cntry = ret.ret1;
            }
          }
          console.log('VAT Country: ' + zs01Cntry);

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
function addPhoneValidatorBELUX() {
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
}

function validateBELUXCopy(addrType, arrayOfTargetTypes) {
  return null;
}

function setSBO(searchTerm) {
  accTeam = document.getElementById('searchTerm').value;
  if (!accTeam || accTeam == '') {
    return;
  }
  if (FormManager.getActualValue('countryUse') == '624') {
    FormManager.setValue('salesBusOffCd', '0'.concat(accTeam.substring(0, 2) + '0000'));
    if (accTeam == 'B3V200') {
      FormManager.setValue('salesBusOffCd', '0B30001');
    } else if (accTeam == 'B3V400') {
      FormManager.setValue('salesBusOffCd', '0B30002');
    } else if (accTeam == 'B3V500') {
      FormManager.setValue('salesBusOffCd', '0B30003');
    } else if (accTeam == 'B3V600') {
      FormManager.setValue('salesBusOffCd', '0B30004');
    } else if (accTeam == 'B3V800') {
      FormManager.setValue('salesBusOffCd', '0B30005');
    } else if (accTeam == 'B3V900') {
      FormManager.setValue('salesBusOffCd', '0B30006');
    }
  } else if (FormManager.getActualValue('countryUse') == '624LU') {
    FormManager.setValue('salesBusOffCd', '0'.concat(accTeam.substring(0, 2) + '0001'));
  }
}

/*
 * function setSORTL() {
 * 
 * if (FormManager.getActualValue('viewOnlyPage') == 'true') { return; }
 * 
 * var acctTeamNum = FormManager.getActualValue('searchTerm'); var custSubType =
 * FormManager.getActualValue('custSubGrp'); var reqType =
 * FormManager.getActualValue('reqType');
 * 
 * if (reqType != 'C') { return; } switch (custSubType) { case 'BEINT': case
 * 'BEISO': case 'BEPRI': case 'BEBUS': if (acctTeamNum !=
 * _pagemodel.searchTerm) { FormManager.setValue('commercialFinanced',
 * acctTeamNum); } break; case 'BECOM': case 'BEPUB': case 'BE3PA': case
 * 'BEDAT': // do nothing break; case 'LUINT': case 'LUISO': case 'LUPRI': case
 * 'LUBUS': if (acctTeamNum != _pagemodel.searchTerm) {
 * FormManager.setValue('commercialFinanced', acctTeamNum); } break; case
 * 'LUCOM': case 'LUPUB': case 'LU3PA': case 'LUDAT': // do nothing break; case
 * 'CBCOM': case 'CBBUS': // do nothing break; default: break; } }
 */

var oldIsu = null;
var oldCtc = null;
function saveOldIsuCtc() {
  oldIsu = FormManager.getActualValue('isuCd');
  oldCtc = FormManager.getActualValue('clientTier');
}

function setSBOValuesForIsuCtc() {
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType == 'U') {
    return;
  }
  var role = FormManager.getActualValue('userRole');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var countryUse = FormManager.getActualValue('countryUse');
  var subGrp = FormManager.getActualValue('custSubGrp');
  var isuList = [ '34', '36', '28', '32' ];
  var beSubGrpsList = [ 'BEINT', 'BEISO', 'BEPRI', 'IBMEM' ];
  var luSubGrpsList = [ 'LUINT', 'LUISO', 'LUPRI', 'LUIBM' ]
  if (role == 'Processor') {
    if (oldIsu == null || oldCtc == null) {
      saveOldIsuCtc();
    }
    if (oldIsu == isuCd && oldCtc == clientTier) {
      return;
    }
  }
  if (countryUse == '624') {
    if (isuCd == '34' && clientTier == 'Q') {
      FormManager.setValue('commercialFinanced', 'T0003601');
      if (role == 'Requester') {
        FormManager.readOnly('commercialFinanced');
        FormManager.readOnly('clientTier');
      }
      if (role == 'Processor') {
        FormManager.enable('isuCd');
        FormManager.enable('commercialFinanced');
        FormManager.enable('clientTier');
      }
    } else if (isuCd == '32' && clientTier == 'T') {
      FormManager.setValue('commercialFinanced', 'T0010421');
    } else if (isuCd == '36' && clientTier == 'Y') {
      FormManager.setValue('commercialFinanced', 'T0007967');
    } else if (isuCd == '28' && clientTier == '') {
      FormManager.setValue('commercialFinanced', 'A0004504');
    } else if (isuCd == '21' && (subGrp == 'BEBUS' || subGrp == 'CBBUS') && clientTier == '') {
      FormManager.setValue('commercialFinanced', 'P0000003');
    } else if (isuCd == '21' && (beSubGrpsList.includes(subGrp)) && clientTier == '') {
      FormManager.setValue('commercialFinanced', 'BU0000');
    }
  }

  if (countryUse == '624LU') {
    if (isuCd == '34' && clientTier == 'Q') {
      FormManager.setValue('commercialFinanced', 'T0003500');
      if (role == 'Requester') {
        FormManager.readOnly('commercialFinanced');
        FormManager.readOnly('clientTier');
      }
      if (role == 'Processor') {
        FormManager.enable('isuCd');
        FormManager.enable('commercialFinanced');
        FormManager.enable('clientTier');
      }
    } else if (isuCd == '36' && clientTier == 'Y') {
      FormManager.setValue('commercialFinanced', 'T0007968');
    } else if (isuCd == '5K' && clientTier == '') {
      FormManager.setValue('commercialFinanced', 'T0009902');
    } else if (isuCd == '21' && subGrp == 'LUBUS' && clientTier == '') {
      FormManager.setValue('commercialFinanced', 'P0000046');
    } else if (isuCd == '21' && (luSubGrpsList.includes(subGrp)) && clientTier == '') {
      FormManager.setValue('commercialFinanced', 'LU0000');
    }
  }
  oldIsu = FormManager.getActualValue('isuCd');
  oldCtc = FormManager.getActualValue('clientTier');
}

function getLandCntryDesc(cntryCd) {
  if (cntryCd != null) {
    reqParam = {
      COUNTRY : cntryCd,
    };
  }
  var results = cmr.query('GET.CNTRY_DESC', reqParam);
  _cntryDesc = results.ret1;
  return _cntryDesc;
}

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    $('label[for="custNm1_view"]').text('Customer Name 1:');
    $('label[for="custNm2_view"]').text('Customer Name 2');
    $('label[for="custNm3_view"]').text('Customer Name 3');
    $('label[for="custNm4_view"]').text('Attention Person');
    $('label[for="landCntry_view"]').text('Country (Landed):');
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="poBox_view"]').text('PO Box:');
    $('label[for="stateProv_view"]').text('State/Prov');
    $('label[for="postCd_view"]').text('Postal Code:');
  }
}

function streetValueFormatterBELUX(value, rowIndex) {
  var display = '';
  var rowData = this.grid.getItem(rowIndex);
  var custNm3 = rowData.custNm3;
  if (custNm3 && custNm3[0]) {
    display += ' ' + custNm3;
  }
  var custNm4 = rowData.custNm4;
  if (custNm4 && custNm4[0]) {
    display += (display ? '<br>' : '') + 'ATT ' + custNm4;
  }
  var poBox = rowData.poBox;
  if (poBox && poBox[0]) {
    display += (display ? '<br>' : '') + 'PO BOX ' + poBox;
  }
  return display;
}

/**
 * Override WW validator
 */
function addCMRSearchValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var result = FormManager.getActualValue('findCmrResult');
        if (reqType == 'U') {
          if (result == '' || result.toUpperCase() == 'NOT DONE') {
            return new ValidationResult(null, false, 'CMR Search has not been performed yet.');
          }
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
 * Override WW validator Validator to check whether D&B search has been
 * performed
 */
function addDnBSearchValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var ifProspect = FormManager.getActualValue('prospLegalInd');
        if (dijit.byId('prospLegalInd')) {
          ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
        }
        var reqType = FormManager.getActualValue('reqType');
        var result = FormManager.getActualValue('findDnbResult');
        var reqStatus = FormManager.getActualValue('reqStatus');
        if ((result == '' || result.toUpperCase() == 'NOT DONE') && reqType == 'C' && reqStatus == 'DRA' && ifProspect == 'Y') {
          return new ValidationResult(null, false, 'D&B Search has not been performed yet.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function disbleCreateByModel() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    FormManager.disable('cmrSearchBtn');
    dojo.removeClass(dojo.byId('cmrSearchBtn'), 'ibm-btn-cancel-pri');
    dojo.addClass(dojo.byId('cmrSearchBtn'), 'ibm-btn-cancel-disabled');
    FormManager.disable('dnbSearchBtn');
    dojo.removeClass(dojo.byId('dnbSearchBtn'), 'ibm-btn-cancel-pri');
    dojo.addClass(dojo.byId('dnbSearchBtn'), 'ibm-btn-cancel-disabled');
  }
}

function addCmrNoValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrNo = FormManager.getActualValue('cmrNo');
        if (FormManager.getActualValue('reqType') == 'U') {
          return new ValidationResult(null, true);
        }
        if (cmrNo != '' && cmrNo != null) {
          if (cmrNo.length != 6) {
            return new ValidationResult(null, false, 'CMR Number should be exactly 6 digits long.');
          } else if (isNaN(cmrNo)) {
            return new ValidationResult(null, false, 'CMR Number should be only numbers.');
          } else if (cmrNo == "000000") {
            return new ValidationResult(null, false, 'CMR Number should not be 000000.');
          } else if (custSubType != '' && (custSubType.includes('INT') && (cmrNo < '990000' || cmrNo > '996999'))) {
            return new ValidationResult(null, false, 'for internal scenario the scope of CMR Number is 990000 ~ 996999');
          } else if (custSubType != '' && (custSubType.includes('ISO') && ((!cmrNo.startsWith('997')) && (!cmrNo.startsWith('998'))))) {
            return new ValidationResult(null, false, 'CMR Number should be in 997XXX or 998XXX format for internal SO scenario');
          } else if (custSubType != '' && !custSubType.includes('IN') && cmrNo.startsWith('99')) {
            return new ValidationResult(null, false, 'Non Internal CMR Number should not be in 99XXXX for scenarios');
          } else {
            var qParams = {
              CMRNO : cmrNo,
              CNTRY : cntry,
              MANDT : cmr.MANDT
            };
            var results = cmr.query('GET.CMR.ME', qParams);
            if (results.ret1 != null) {
              return new ValidationResult(null, false, 'The CMR Number already exists.');
            } else {
              results = cmr.query('LD.CHECK_EXISTING_CMR_NO_RESERVED', {
                COUNTRY : cntry,
                CMR_NO : cmrNo,
                MANDT : cmr.MANDT
              });
              if (results && results.ret1) {
                return new ValidationResult({
                  id : 'cmrNo',
                  type : 'text',
                  name : 'cmrNo'
                }, false, 'The requested CMR Number ' + cmrNo + ' already exists in the system.');
              }
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addDepartmentNumberValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var departmentNumber = FormManager.getActualValue('ibmDeptCostCenter');
        var pattern = /^[0-9A-Za-z]+$/;
        if (reqType == 'C' || reqType == 'U') {
          if (departmentNumber != null && departmentNumber != '') {
            if (departmentNumber.length >= 1 && departmentNumber.length != 6) {
              return new ValidationResult(null, false, 'Department number should be exactly 6 digits.');
            }
            if (departmentNumber.length >= 1 && !departmentNumber.match(pattern)) {
              return new ValidationResult({
                id : 'ibmDeptCostCenter',
                type : 'text',
                name : 'ibmDeptCostCenter'
              }, false, 'Department Number should be 6 digits.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addREALCTYValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var countryUse = FormManager.getActualValue('countryUse');
        var cmrNo = FormManager.getActualValue('cmrNo');
        if (reqType == 'U') {
          var qParams = {
            COUNTRY : '624',
            CMR_NO : cmrNo
          };
          var result = cmr.query('BENELUX.CHECK_REALCTY', qParams);
          if (result != null && result.ret1 != null) {
            console.log(result);
            var realcty = result.ret1;
            if (realcty == '623' && countryUse != '624LU') {
              return new ValidationResult(null, false, 'Please create request for Issuing country Luxembourg.');
            } else if (realcty == '624' && countryUse != '624') {
              return new ValidationResult(null, false, 'Please create request for Issuing country Belgium.');
            }
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/*
 * function setClientTierValuesForUpdate() { var reqType =
 * FormManager.getActualValue('reqType'); if
 * (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C' ||
 * FormManager.getActualValue('custSubGrp').includes('IBM')) { return; } var
 * isuCd = FormManager.getActualValue('isuCd'); if (isuCd == '28') {
 * FormManager.setValue('clientTier', ''); FormManager.readOnly('clientTier'); }
 * else { FormManager.enable('clientTier'); } if
 * (FormManager.getActualValue('custSubGrp').includes('IBM')) {
 * FormManager.readOnly('clientTier'); } }
 */

// CREATCMR-4293
function setCTCValues() {

  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  var custSubGrpArray = [ 'BEBUS', 'BEINT', 'BEISO', 'CBBUS', 'LUBUS', 'LUINT', 'LUISO' ];

  // Business Partner OR Internal OR Internal SO
  if (custSubGrpArray.includes(custSubGrp)) {
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
  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  // if (((isuCode == '21' || isuCode == '8B' || isuCode == '5K') && reqType ==
  // 'C') || (isuCode != '34' && reqType == 'U')) {
  var activeIsuCd = [ '32', '34', '36' ];
  var activeCtc = [ 'Q', 'Y', 'T' ];

  if (!activeIsuCd.includes(isuCode)) {
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

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept blank.');
    }
  } else if (isuCode == '34') {
    if (clientTierCode == 'Q') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\'.');
    }
  } else if (isuCode == '36') {
    if (clientTierCode == 'Y') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Y\'.');
    }
  } else if (isuCode == '32') {
    if (clientTierCode == 'T') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'T\'.');
    }
  } else {
    if (activeCtc.includes(clientTierCode) || clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');
      $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\', \'Y\' , \'T\' or blank.');
    }
  }
}
// CREATCMR-4293

function lockFields() {
  var reqTyp = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var lockSubGrpList = [ 'CBBUS', 'BUSPR', 'BEINT', 'BEISO', 'IBMEM', 'BEPRI', 'LUBUS', 'LUIBM', 'LUINT', 'LUISO', 'LUPRI' ];

  if (lockSubGrpList.includes(custSubGrp)) {
    FormManager.readOnly('commercialFinanced');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
  }
}

function beluxSortlValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var commercialFinanced = FormManager.getActualValue('commercialFinanced');
        var isuCd = FormManager.getActualValue('isuCd');
        var clientTier = FormManager.getActualValue('clientTier');
        var reqType = FormManager.getActualValue('reqType');
        var valResult = null;
        var oldSearchTerm = null;
        var oldClientTier = null;
        var oldISU = null;
        var requestId = FormManager.getActualValue('reqId');

        if (reqType == 'C') {
          valResult = sortlCheckValidator();
        } else {
          qParams = {
            REQ_ID : requestId,
          };
          var result = cmr.query('GET.BENELUX.CLIENT_TIER_ISU_SBO_CD_OLD_BY_REQID', qParams);

          if (result != null && result != '') {
            oldClientTier = result.ret1 != null ? result.ret1 : '';
            oldSearchTerm = result.ret3 != null ? result.ret3 : '';
            oldISU = result.ret2 != null ? result.ret2 : '';

            if (clientTier != oldClientTier || isuCd != oldISU || commercialFinanced != oldSearchTerm) {
              valResult = sortlCheckValidator();
            }
          }
        }
        return valResult;
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function sortlCheckValidator() {
  var reqTyp = FormManager.getActualValue('reqType');
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var commercialFinanced = FormManager.getActualValue('commercialFinanced');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var countryUse = FormManager.getActualValue('countryUse');
  var isuCtc = isuCode.concat(clientTierCode);
  var subIndustry = FormManager.getActualValue('subIndustryCd');
  var ind = subIndustry.substring(0, 1);

  var scenariosToBlock = [ 'CBBUS', 'BEBUS', 'BEINT', 'IBMEM', 'BEPRI', 'LUBUS', 'LUINT', 'LUIBM', 'LUPRI', 'LUISO' ];

  var accSeq_624 = {
    '34Q' : [ 'T0003601' ],
    '36Y' : [ 'T0007967' ],
    '32T' : [ 'T0010421', 'T0010425', 'T0010403', 'T0010422', 'T0010426', 'T0010423', 'T0010424' ],
    '5K' : [ 'T0009066' ],
    '21' : [ 'BU0000', 'P0000003' ],
    '28' : [ 'A0004504' ]
  };

  var accSeq_624LU = {
    '34Q' : [ 'T0003500' ],
    '21' : [ 'P0000046', 'LU0000' ],
    '36Y' : [ 'T0007968' ],
    '5K' : [ 'T0009902' ]
  };

  if (!scenariosToBlock.includes(custSubGrp) && isuCtc != '' && isuCtc != undefined && isuCtc != null) {
    if (cmrIssuingCntry == '624') {
      if (countryUse == '624') {
        if (accSeq_624.hasOwnProperty(isuCtc) && !accSeq_624[isuCtc].includes(commercialFinanced)) {
          return new ValidationResult({
            id : 'commercialFinanced',
            type : 'text',
            name : 'commercialFinanced'
          }, false, 'SORTL can only accept ' + accSeq_624[isuCtc]);
        }
      }
      if (cmrIssuingCntry == '624') {
        if (countryUse == '624LU') {
          if (accSeq_624LU.hasOwnProperty(isuCtc) && !accSeq_624LU[isuCtc].includes(commercialFinanced)) {
            return new ValidationResult({
              id : 'commercialFinanced',
              type : 'text',
              name : 'commercialFinanced'
            }, false, 'SORTL can only accept ' + accSeq_624LU[isuCtc]);
          }
        }
      }
    }
  } else {
    return new ValidationResult(null, true);
  }
}

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

// CREATCMR-788
function addressQuotationValidatorBELUX() {
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name 1' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name 2' ]);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, [ 'Customer Name 3' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Attention Person' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street Address' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);
}
dojo.addOnLoad(function() {
  GEOHandler.BELUX = [ '624' ];

  console.log('adding BELUX functions...');
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.BELUX);
  GEOHandler.enableCopyAddress(GEOHandler.BELUX, validateBELUXCopy, [ 'ZD01' ]);

  // GEOHandler.addAddrFunction(setAbbrevNameLocnProc, GEOHandler.BELUX);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(addHandlersForBELUX, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(setCollectionCodeValues, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(afterConfigForBELUX, GEOHandler.BELUX);
  // GEOHandler.addAfterConfig(setAbbrvNmLoc, GEOHandler.BELUX);
  // GEOHandler.addAfterConfig(addHandlerForReqRsn, GEOHandler.BELUX);
  // GEOHandler.addAfterConfig(setPreferredLanguage, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(setClientTierValues, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(setVatInfoBubble, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.BELUX);
  // GEOHandler.addAfterConfig(disbleCreateByModel, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(setISUDropDown, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(setClientTierDropDown, GEOHandler.BELUX);

  GEOHandler.addAfterTemplateLoad(setAccountTeamNumberValues, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(addHandlersForBELUX, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setCollectionCodeValues, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(afterConfigForBELUX, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setAbbrvNmLoc, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setAbbrvNmLocFrProc, GEOHandler.BELUX);
  // GEOHandler.addAfterTemplateLoad(setINACfrLux, GEOHandler.BELUX);
  // GEOHandler.addAfterTemplateLoad(setPreferredLanguage, GEOHandler.BELUX);
  // GEOHandler.addAfterTemplateLoad(setINACValues, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setEconomicCodeValues, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setClientTierValues, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setISUDropDown, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setClientTierDropDown, GEOHandler.BELUX);
  // GEOHandler.addAfterTemplateLoad(setSortlOnScenarios, GEOHandler.BELUX);

  GEOHandler.addAddrFunction(disableLandCntry, GEOHandler.BELUX);
  GEOHandler.addAddrFunction(addLandedCountryHandler, GEOHandler.BELUX);
  GEOHandler.addAddrFunction(addPhoneValidatorBELUX, GEOHandler.BELUX);

  GEOHandler.registerValidator(checkForBnkruptcy, GEOHandler.BELUX, null, true);
  // GEOHandler.registerValidator(addAbbrevLocnLengthValidator,GEOHandler.BELUX,
  // null, true);
  // GEOHandler.registerValidator(addAbbrevNameLengthValidator,GEOHandler.BELUX,
  // null, true);
  GEOHandler.registerValidator(addBELUXVATValidator('', 'MAIN_CUST_TAB', 'frmCMR', 'ZP01'), GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(addCrossBorderValidatorBELUX, GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(addBELUXAddressTypeValidator, GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.BELUX, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(resetVATValidationsForPayGo, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(resetVATValidationsForPayGo, GEOHandler.BELUX);
  GEOHandler.registerValidator(addCMRSearchValidator, GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(addCmrNoValidator, GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(addDepartmentNumberValidator, GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(addREALCTYValidator, GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.BELUX, null, true);
  GEOHandler.addAfterConfig(setSBOValuesForIsuCtc, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setSBOValuesForIsuCtc, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setPPSCEIDRequired, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setVatValidatorBELUX, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(addBeIsuHandler, GEOHandler.BELUX);

  // CREATCMR-4293
  GEOHandler.addAfterTemplateLoad(setCTCValues, GEOHandler.BELUX);
  GEOHandler.registerValidator(clientTierValidator, GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(beluxSortlValidator, GEOHandler.BELUX, null, true);
  GEOHandler.addAfterTemplateLoad(lockFields, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(lockFields, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(saveOldIsuCtc, GEOHandler.BELUX);
});