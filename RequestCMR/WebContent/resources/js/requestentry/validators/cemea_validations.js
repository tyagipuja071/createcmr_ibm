/* Register CEMEA Javascripts */

// Exclusive countries for GBM/SBM 
var CEMEA_EXCL = new Set([ '620', '767', '805', '823', '677', '680', '832' ]);

function addCEMEALandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  } else if (saving) {
    var landCntry = FormManager.getActualValue('landCntry');
    var postCode = FormManager.getActualValue('postCd');
    if (landCntry == 'SA' && postCode == '') {
      FormManager.setValue('postCd', '00000');
    }

    /**
     * Defect 1525544: disable copy address pop-up for G
     */
    if (FormManager.getActualValue('addrType') == 'ZP02') {
      GEOHandler.disableCopyAddress();
    } else {
      GEOHandler.enableCopyAddress(GEOHandler.CEMEA_COPY, validateCEMEACopy, [ 'ZD01' ]);
    }
  }
}

/**
 * imports data values from findCMR using enterprise as cmrNo
 */
function importByEnterprise() {
  var cmrNo = FormManager.getActualValue('enterprise');
  if (cmrNo == '') {
    cmr.showAlert('Please input enterprise number as CMR Number to search for.');
    return;
  }

  var hasAccepted = dojo.byId('findCMRResult_txt').innerHTML.trim() == 'Accepted';
  cmr.skipAddress = true;
  if (hasAccepted) {
    cmr.importcmr = cmrNo;
    cmr.showConfirm('doImportCmrs()',
        'Results from a previous CMR Search have already been accepted for this request. Importing will overwrite existing data records. Continue importing the CMR records?', null, null, {
          OK : 'Yes',
          CANCEL : 'Cancel'
        });
  } else {
    importCMRs(cmrNo);
  }

}

/**
 * lock Embargo Code field
 */
function lockEmbargo() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    FormManager.readOnly('embargoCd');
  } else {
    FormManager.enable('embargoCd');
  }
}

/**
 * lock OrdBlk field
 */
function lockOrdBlk() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    FormManager.readOnly('ordBlk');
  } else {
    FormManager.enable('ordBlk');
  }
  // CMR - 3389
  if (cntry == SysLoc.AUSTRIA && reqType == 'U') {
    FormManager.enable('ordBlk');
  }
}

function orderBlockValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') == '618') {
          var role = FormManager.getActualValue('userRole').toUpperCase();
          var ordBlk = FormManager.getActualValue('ordBlk');
          if (role == 'PROCESSOR') {
            if (ordBlk != '') {
              if (ordBlk == '88' || ordBlk == '94') {
              } else {
                return new ValidationResult(null, false, 'Only blank, 88, 94 are allowed.');
              }
            }
          }
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

/**
 * After config for CEMEA
 */
function afterConfigForCEMEA() {
  // for all requests
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly == 'true') {
    return;
  }

  FormManager.readOnly('cmrOwner');

  // Set abbrevLocn for Softlayer Scenario
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType == 'SOFTL') {
    FormManager.setValue('abbrevLocn', 'Softlayer');
  }

  // set defaultLandedCountry except 707 - use 'CS' for RS/CS/ME
  var landCntry = '';
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion && cntryRegion != '' && cntryRegion.length > 3 && FormManager.getActualValue('cmrIssuingCntry') != SysLoc.SERBIA) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }

  FormManager.readOnly('capInd');
  FormManager.setValue('capInd', true);
  FormManager.readOnly('subIndustryCd');

  if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
    FormManager.readOnly('sensitiveFlag');
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == '618') {
    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER') {
      FormManager.readOnly('custClass');
    } else {
      FormManager.enable('custClass');
    }
  }
  setAustriaUIFields();
  setExpediteReason();
  setTypeOfCustomerRequiredProcessor();
}

function setAustriaUIFields() {
  if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
    return;
  }
  FormManager.hide('CreditCd', 'creditCd');
  FormManager.hide('CurrencyCode', 'legacyCurrencyCd');

  if (FormManager.getActualValue('custSubGrp') == 'IBMEM') {
    FormManager.readOnly('vat');
    FormManager.setValue('vat', '');
    FormManager.readOnly('isicCd');
    FormManager.setValue('isicCd', '9500');
    FormManager.readOnly('subIndustryCd');
    FormManager.setValue('subIndustryCd', 'WQ');
    FormManager.readOnly('salesBusOffCd');
    FormManager.setValue('salesBusOffCd', '099');
    FormManager.readOnly('inacCd');
    FormManager.setValue('inacCd', '');
    FormManager.readOnly('enterprise');
    FormManager.setValue('enterprise', '');
  }

  var custType = FormManager.getActualValue('custGrp');
  var custSubType = FormManager.getActualValue('custSubGrp');
  // for Private customer
  if (custSubType != null && custSubType != '' && custSubType == 'PRICU') {
    FormManager.setValue("vat", "");
    FormManager.readOnly("vat");
    FormManager.setValue("inacCd", "");
    FormManager.readOnly("inacCd");
    FormManager.setValue("enterprise", "");
    FormManager.readOnly("enterprise");
    FormManager.readOnly("salesBusOffCd");
  }

  // for cross border - Business partner
  if (custSubType != null && custSubType != '' && (custSubType == 'BUSPR' || (custType != null && custType != '' && custType == 'CROSS' && custSubType == 'XBP'))) {
    FormManager.resetValidations('inacCd');
    FormManager.resetValidations('enterprise');
    FormManager.setValue("inacCd", "");
    FormManager.readOnly("inacCd");
    FormManager.setValue("enterprise", "");
    FormManager.readOnly("enterprise");
    FormManager.setValue("salesBusOffCd", "080");
    FormManager.readOnly("salesBusOffCd");

  }
  // FOR LOCAL CUSTOMER - Internal , Internal SO
  if (custType == 'LOCAL' && custSubType != null && custSubType != '' && (custSubType == 'INTER' || custSubType == 'INTSO')) {
    FormManager.readOnly('vat');
    FormManager.setValue('vat', '');
    FormManager.readOnly('salesBusOffCd');
    FormManager.setValue('salesBusOffCd', '000');
    FormManager.readOnly('inacCd');
    FormManager.setValue('inacCd', '');
    FormManager.readOnly('enterprise');
    FormManager.setValue('enterprise', '');
  }

}

/**
 * validates CMR number for selected scenarios only
 */
function addCmrNoValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('reqType') != 'C') {
          return new ValidationResult(null, true);
        }
        if (FormManager.getActualValue('viewOnlyPage') == 'true') {
          return new ValidationResult(null, true);
        }

        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrNo = FormManager.getActualValue('cmrNo');

        if (cmrNo != '' && cmrNo.length != 6) {
          return new ValidationResult(null, false, 'CMR Number should be exactly 6 digits long.');
        } else if (cmrNo != '' && custSubType != '' && custSubType.includes('IN') && !cmrNo.startsWith('99')) {
          return new ValidationResult(null, false, 'CMR Number should be in 99XXXX format for internal scenarios');
        } else if (cntry != SysLoc.AUSTRIA && cmrNo != '' && custSubType != '' && (custSubType == 'BUSPR' || custSubType.includes('BP')) && !(cmrNo >= 002000 && cmrNo <= 009999)) {
          return new ValidationResult(null, false, 'CMR Number should be within range: 002000 - 009999 for Business Partner scenarios');
        } else if (cmrNo != '' && custSubType != '' && custSubType == 'XCEM' && !(cmrNo >= 500000 && cmrNo <= 799999)) {
          return new ValidationResult(null, false, 'CMR Number should be within range: 500000 - 799999 for CEMEX scenarios');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function lockLandCntry() {
  var custType = FormManager.getActualValue('custGrp');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var addrType = FormManager.getActualValue('addrType');
  if (addrType == 'ZP02') {
    /* Defect : 1590750 */
    // FormManager.disable('landCntry');
    return;
  }
  var local = false;
  if (custType && custType.includes('LOC')) {
    local = true;
  } else if (custSubType && (custSubType == 'ELCOM' || custSubType == 'ELBP')) {
    local = true;
  }
  if (local && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
}

/**
 * After config handlers
 */
var _addrTypesForCEMEA = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'ZP02' ];
var _addrTypeHandler = [];
var _ISUHandler = null;
var _CTCHandler = null;
var _SalesRepHandler = null;
var _CTC2Handler = null;
var _SalesRep2Handler = null;
var _ExpediteHandler = null;
var _IMSHandler = null;
function addHandlersForCEMEA() {
  for (var i = 0; i < _addrTypesForCEMEA.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForCEMEA[i]), 'onClick', function(value) {
        lockLandCntry();
      });
    }
  }

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValues(value);
    });
  }

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setEnterpriseValues(value);
      // CMR-2101 Austria remove ISR
      if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
        setSalesRepValues(value);
      }
      setSBOValuesForIsuCtc();// CMR-2101
    });
  }

  if (_SalesRepHandler == null) {
    _SalesRepHandler = dojo.connect(FormManager.getField('repTeamMemberNo'), 'onChange', function(value) {
      setSBO(value);
    });
  }

  if (_ExpediteHandler == null) {
    _ExpediteHandler = dojo.connect(FormManager.getField('expediteInd'), 'onChange', function(value) {
      setExpediteReason();
    });
  }

  if (_IMSHandler == null && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    _IMSHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      // CMR-2101 Austria remove ISR
      // setSalesRepValues();
      setISUCTCOnIMSChange();
      setSBOValuesForIsuCtc();// CMR-2101
    });
  }

}

function setISUCTCOnIMSChange() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (!(custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'PRICU' || custSubGrp == 'IBMEM' || custSubGrp == 'BUSPR' || custSubGrp == 'XBP')) {
    if ('32' == isuCd && 'S' == clientTier && subIndustryCd.startsWith('B')) {
      FormManager.setValue('clientTier', 'N');
    } else if ('32' == isuCd && 'N' == clientTier && !subIndustryCd.startsWith('B')) {
      FormManager.setValue('clientTier', 'S');
    }
  }
}

var _vatExemptHandler = null;
function addVatExemptHandler() {
  if (!FormManager.getField('vatExempt')) {
    window.setTimeout('addVatExemptHandler()', 500);
  } else {
    if (_vatExemptHandler == null) {
      _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
        setVatRequired(value);
      });
    }
  }
}

var _cisHandler = null;
function addCISHandler() {
  if (!FormManager.getField('cisServiceCustIndc')) {
    window.setTimeout('addCISHandler()', 500);
  } else {
    if (_cisHandler == null) {
      _cisHandler = dojo.connect(FormManager.getField('cisServiceCustIndc'), 'onClick', function(value) {
        setCountryDuplicateFields(value);
      });
    }
  }
}

var _DupIssuingCntryCdHandler = null;
var _ISU2Handler = null;
var _CTC2Handler = null;
var _SalesRep2Handler = null;
function setCISFieldHandlers() {
  if (_DupIssuingCntryCdHandler == null) {
    _DupIssuingCntryCdHandler = dojo.connect(FormManager.getField('dupIssuingCntryCd'), 'onChange', function(value) {
      setDropdownField2Values(value);
    });
  }

  if (_ISU2Handler == null) {
    _ISU2Handler = dojo.connect(FormManager.getField('dupIsuCd'), 'onChange', function(value) {
      setClientTier2Values(value);
    });
  }

  if (_CTC2Handler == null) {
    _CTC2Handler = dojo.connect(FormManager.getField('dupClientTierCd'), 'onChange', function(value) {
      setEnterprise2Values(value);
    });
  }

  if (_SalesRep2Handler == null) {
    _SalesRep2Handler = dojo.connect(FormManager.getField('dupSalesRepNo'), 'onChange', function(value) {
      setSBO2(value);
    });
  }
}

function setExpediteReason() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('expediteInd') != 'Y') {
    FormManager.clearValue('expediteReason');
    FormManager.readOnly('expediteReason');
  }
}

function addAddressTypeValidator() {
  console.log("addAddressTypeValidator for CEMEA..........");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var reqLocalAddr = new Set([ '832', '821', '820', '693' ]);

        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Address types are mandatory.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var zs01Cnt = 0;
          var zp01Cnt = 0;
          var zi01Cnt = 0;
          var zd01Cnt = 0;
          var zs02Cnt = 0;
          var zp02Cnt = 0;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (type == 'ZS01') {
              zs01Cnt++;
            } else if (type == 'ZP01') {
              zp01Cnt++;
            } else if (type == 'ZI01') {
              zi01Cnt++;
            } else if (type == 'ZD01') {
              zd01Cnt++;
            } else if (type == 'ZS02') {
              zs02Cnt++;
            } else if (type == 'ZP02') {
              zp02Cnt++;
            }
          }

          if (reqLocalAddr.has(cntry) && (zs01Cnt == 0 || zp01Cnt == 0 || zi01Cnt == 0 || zd01Cnt == 0 || zs02Cnt == 0 || zp02Cnt == 0)) {
            return new ValidationResult(null, false, 'All address types are mandatory.');
          } else if (cntry == SysLoc.AUSTRIA) {
            var reqLob = FormManager.getActualValue('requestingLob');// request
            // LOB=IGF
            // will
            // have 2
            // additional
            // address
            // type to
            // own
            if (reqLob == 'IGF' && (zs01Cnt == 0 || zp01Cnt == 0 || zi01Cnt == 0 || zd01Cnt == 0 || zs02Cnt == 0)) {
              return new ValidationResult(null, false, 'All address types are mandatory.');
            } else if (zs01Cnt == 0) {
              // CMR-3389
              return new ValidationResult(null, false, 'Sold-to address is mandatory for CMR creation.');
            }
          } else if (zs01Cnt == 0 || zp01Cnt == 0 || zi01Cnt == 0 || zd01Cnt == 0 || zs02Cnt == 0) {
            return new ValidationResult(null, false, 'All address types are mandatory except G Address.');
          } else if (zs01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Installing address is allowed.');
          } else if (zp01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Billing address is allowed.');
          } else if (zi01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Mailing address is allowed.');
          } else if (zs02Cnt > 1) {
            return new ValidationResult(null, false, 'Only one EPL address is allowed.');
          } else if (zp02Cnt > 1) {
            return new ValidationResult(null, false, 'Only one G address is allowed.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

/* defect : 1864345 */

/* Street or PO Box should be required (Austria) */

function addStreetAndPoBoxFormValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
          return new ValidationResult(null, true);
        }
        if (FormManager.getActualValue('addrTxt') == '' && FormManager.getActualValue('poBox') == '') {
          return new ValidationResult(null, false, 'Please fill-out either Street Address or PO Box.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

/* defect : 1864345 */

// var _addrTypesForSwiss = [ 'ZD01', 'ZI01', 'ZP01', 'ZS01'];
/*
 * function addPoBoxValidator(){ FormManager.addFormValidator((function(){
 * return { validate : function(){ if (CmrGrid.GRIDS.ADDRESS_GRID_GRID &&
 * CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0){ var recordList = null; var
 * reqType = FormManager.getActualValue('reqType') var role =
 * FormManager.getActualValue('userRole').toUpperCase(); var isPoboxEmpty =
 * false;
 * 
 * for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++){
 * recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i); if (recordList ==
 * null && _allAddressData != null && _allAddressData[i] != null){ recordList =
 * _allAddressData[i]; } addPoBox = recordList.addPoBox;
 * 
 * if (typeof (addPoBox) == 'object') { addPoBox = addPoBox[0]; } if(reqType ==
 * 'C' && (addPoBox == null || addPoBox == '')){
 * 
 * isPoboxEmpty = true; } }
 * 
 * if(isPoboxEmpty == true){ return new ValidationResult(null, false, 'PO Box
 * should not be empty.'); } return new ValidationResult(null, true); } return
 * new ValidationResult(null, true); } }; })(), 'MAIN_NAME_TAB', 'frmCMR'); }
 */

/**
 * Add Latin character validation for address fields
 */
function addLatinCharValidator() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    return;
  }

  var restrictNonLatin = false;
  if (FormManager.getActualValue('addrType') != 'ZP02') {
    restrictNonLatin = true;
  }

  if (restrictNonLatin) {
    checkAndAddValidator('custNm1', Validators.LATIN, [ 'Customer Name (1)' ]);
    checkAndAddValidator('custNm2', Validators.LATIN, [ 'Customer Name (2)' ]);
    checkAndAddValidator('custNm3', Validators.LATIN, [ 'Customer Name (3)' ]);
    checkAndAddValidator('addrTxt', Validators.LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.LATIN, [ 'City' ]);
    checkAndAddValidator('postCd', Validators.LATIN, [ 'Postal Code' ]);
  } else {
    FormManager.removeValidator('custNm1', Validators.LATIN);
    FormManager.removeValidator('custNm2', Validators.LATIN);
    FormManager.removeValidator('custNm3', Validators.LATIN);
    FormManager.removeValidator('addrTxt', Validators.LATIN);
    FormManager.removeValidator('city1', Validators.LATIN);
    FormManager.removeValidator('postCd', Validators.LATIN);
  }
}

function addCrossBorderValidatorForCEMEA() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        // only Create type will be validated
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }

        var scenario = FormManager.getActualValue('custGrp');
        var subType = FormManager.getActualValue('custSubGrp');
        if (scenario != null && (scenario.includes('CRO') || subType.includes('EX'))) {
          scenario = 'CROSS';
        }

        var cntryRegion = FormManager.getActualValue('countryUse');
        var landCntry = FormManager.getActualValue('defaultLandedCountry');

        // except 707 - use 'CS' for RS/CS/ME
        if (cntryRegion != '' && cntryRegion.length > 3 && FormManager.getActualValue('cmrIssuingCntry') != SysLoc.SERBIA) {
          landCntry = cntryRegion.substring(3, 5);
        }

        var reqId = FormManager.getActualValue('reqId');
        var defaultLandCntry = landCntry;
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

/**
 * Validator for copy address
 */
function validateCEMEACopy(addrType, arrayOfTargetTypes) {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    return;
  }
  console.log('Addr Type: ' + addrType + " Targets: " + arrayOfTargetTypes);
  var localLang = addrType == 'ZP02';
  if (localLang
      && (arrayOfTargetTypes.indexOf('ZS01') >= 0 || arrayOfTargetTypes.indexOf('ZP01') >= 0 || arrayOfTargetTypes.indexOf('ZI01') >= 0 || arrayOfTargetTypes.indexOf('ZD01') >= 0 || arrayOfTargetTypes
          .indexOf('ZS02') >= 0)) {
    return 'Cannot copy local address to non-local addresses. Please select only local target addresses.';
  }
  if (!localLang && (arrayOfTargetTypes.indexOf('ZP02') >= 0)) {
    return 'Cannot copy non-local address to local addresses. Please select only non-local target addresses.';
  }
  return null;
}

/**
 * Validator for address fields
 */
function addAddressFieldValidators() {
  // CEEME: City + Postal Code should not exceed 30 characters
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (cntry == SysLoc.AUSTRIA) {
          return new ValidationResult(null, true);
        }
        var city = FormManager.getActualValue('city1');
        var postCd = FormManager.getActualValue('postCd');

        var val = city;
        if (postCd != '') {
          val += postCd;
        }
        if (val && val.length > 30) {
          return new ValidationResult(null, false, 'Total computed length of City and Postal Code should not exceed 30 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // phone + ATT should not exceed 30 characters
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var att = FormManager.getActualValue('custNm4');
        var custPhone = FormManager.getActualValue('custPhone');
        var val = att;

        if (custPhone != '') {
          val += custPhone;
          if (val != null && val.length > 30) {// CMR-816
            return new ValidationResult(null, false, 'Total computed length of Attention Person and Phone should not exceed 30 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // AT: addrTxt + poBox should not exceed 21 characters
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
          return new ValidationResult(null, true);
        }
        var addrTxt = FormManager.getActualValue('addrTxt');
        var poBox = FormManager.getActualValue('poBox');

        var val = addrTxt;
        if (poBox != null && poBox != '') {
          val += poBox;
          if (val != null && val.length > 21) {
            return new ValidationResult(null, false, 'Total computed length of Street and PO BOX should not exceed 21 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // ME: custNm2/custNm3 + poBox should not exceed 21 characters
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
          return new ValidationResult(null, true);
        }
        var custNm2 = FormManager.getActualValue('custNm2');
        var custNm3 = FormManager.getActualValue('custNm3');
        var poBox = FormManager.getActualValue('poBox');

        var val1 = custNm2;
        var val2 = custNm3;
        if (poBox != null && poBox != '') {
          val1 += poBox;
          val2 += poBox;
          if (val1.length > 21 && val2.length > 21) {
            return new ValidationResult(null, false, 'Customer Name (2)/Customer Name (3) and PO BOX should not exceed 21 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setPreferredLang() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if ('618' == cntry) {
    return;
  }
  FormManager.readOnly('custPrefLang');
  if ('693' == cntry) {
    FormManager.setValue('custPrefLang', 'Q');
  } else if ('668' == cntry) {
    FormManager.setValue('custPrefLang', 'C');
  } else if ('820' == cntry) {
    FormManager.setValue('custPrefLang', 'L');
  } else if ('708' == cntry) {
    FormManager.setValue('custPrefLang', '5');
  } else if ('642' == cntry) {
    FormManager.setValue('custPrefLang', 'F');
  } else if ('832' == cntry) {
    FormManager.setValue('custPrefLang', 'A');
  } else if ('821' == cntry) {
    FormManager.setValue('custPrefLang', 'R');
  } else {
    FormManager.setValue('custPrefLang', 'E');
  }
}

function setClientTierValues(isuCd) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
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
          FormManager.setValue('clientTier', clientTiers[0]);
        }
      }
    }
  }
}

function setClientTier2Values(dupIsuCd) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  dupIsuCd = FormManager.getActualValue('dupIsuCd');
  var dupIssuingCntryCd = FormManager.getActualValue('dupIssuingCntryCd');
  var clientTiers = [];
  if (dupIsuCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : dupIssuingCntryCd,
      ISU : '%' + dupIsuCd + '%'
    };
    var results = cmr.query('GET.CTCLIST.BYISU', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        clientTiers.push(results[i].ret1);
      }
      if (clientTiers != null) {
        FormManager.limitDropdownValues(FormManager.getField('dupClientTierCd'), clientTiers);
        if (clientTiers.length == 1) {
          FormManager.setValue('dupClientTierCd', clientTiers[0]);
        }
      }
    }
  }
}

/**
 * resets SalRepNo2 and Enterprise2 values based duplicate country
 */
function setDropdownField2Values() {
  var dupIssuingCntryCd = FormManager.getActualValue('dupIssuingCntryCd');
  FilteringDropdown.loadItems('dupSalesRepNo', 'dupSalesRepNo_spinner', 'lov', 'fieldId=SalRepNameNo&cmrIssuingCntry=' + dupIssuingCntryCd);
  FilteringDropdown.loadItems('dupEnterpriseNo', 'dupEnterpriseNo_spinner', 'lov', 'fieldId=Enterprise&cmrIssuingCntry=' + dupIssuingCntryCd);
}

function setVatRequired(value) {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    if (!value && !dijit.byId('vatExempt')) {
      window.setTimeout('setVatRequired()', 500);
    } else {
      FormManager.resetValidations('vat');
      if (!dijit.byId('vatExempt').get('checked')) {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        /*
         * if (cntry == SysLoc.AUSTRIA) { var custGroup =
         * FormManager.getActualValue('custGrp'); if (custGroup != 'CROSS') {
         * checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
         * cemeaCustomVATMandatory(); } } else
         */if (cntry == SysLoc.SERBIA) {
          var cntryUsed = '';
          var result = cmr.query('GET_CNTRYUSED', {
            REQ_ID : FormManager.getActualValue('reqId'),
          });
          if (result && result.ret1 && result.ret1 != '') {
            cntryUsed = result.ret1;
          }
          var addrType = 'ZP01';
          var zs01Cntry = '';

          var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
            REQID : FormManager.getActualValue('reqId'),
            TYPE : addrType ? addrType : 'ZP01'
          });
          if (ret && ret.ret1 && ret.ret1 != '') {
            zs01Cntry = ret.ret1;
          }

          if (cntryUsed != null && cntryUsed.length > 0 && zs01Cntry != null && zs01Cntry == 'CS') {
            switch (cntryUsed) {
            case '707ME':
              return;
              break;
            case '707CS':
              return;
              break;
            default:
              cemeaCustomVATMandatory();
              break;
            }
          } else {
            cemeaCustomVATMandatory();
          }

        }
        cemeaCustomVATMandatory();
      }
    }
  }
}

function setCountryDuplicateFields(value) {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole');

  if (!value && !dijit.byId('cisServiceCustIndc')) {
    window.setTimeout('setCountryDuplicateFields()', 500);
  } else {
    FormManager.resetValidations('dupIssuingCntryCd');
    if (dijit.byId('cisServiceCustIndc').get('checked')) {
      FormManager.show('ISU2', 'dupIsuCd');
      FormManager.show('ClientTier2', 'dupClientTierCd');
      FormManager.show('Enterprise2', 'dupEnterpriseNo');
      FormManager.show('SalRepNameNo2', 'dupSalesRepNo');
      if (role == GEOHandler.ROLE_PROCESSOR) {
        FormManager.show('SalesBusOff2', 'dupSalesBoCd');
      } else {
        FormManager.hide('SalesBusOff2', 'dupSalesBoCd');
      }

      setDropdownField2Values();
      if (viewOnlyPage != 'true') {
        FormManager.enable('dupIssuingCntryCd');
        checkAndAddValidator('dupIssuingCntryCd', Validators.REQUIRED, [ 'Country of Duplicate CMR' ]);
        setCISFieldHandlers();

        if (FormManager.getActualValue('reqType') == 'C') {
          checkAndAddValidator('dupIsuCd', Validators.REQUIRED, [ 'ISU Code 2' ]);
          checkAndAddValidator('dupClientTierCd', Validators.REQUIRED, [ 'Client Tier 2' ]);
          checkAndAddValidator('dupSalesRepNo', Validators.REQUIRED, [ 'Sales Rep 2' ]);
        }
      } else {
        FormManager.readOnly('dupIsuCd');
        FormManager.readOnly('dupClientTierCd');
        FormManager.readOnly('dupEnterpriseNo');
        FormManager.readOnly('dupSalesRepNo');
      }

    } else {
      FormManager.clearValue('dupIssuingCntryCd');
      FormManager.disable('dupIssuingCntryCd');

      FormManager.resetValidations('dupIsuCd');
      FormManager.resetValidations('dupClientTierCd');
      FormManager.resetValidations('dupSalesRepNo');
      FormManager.hide('ISU2', 'dupIsuCd');
      FormManager.hide('ClientTier2', 'dupClientTierCd');
      FormManager.hide('Enterprise2', 'dupEnterpriseNo');
      FormManager.hide('SalRepNameNo2', 'dupSalesRepNo');
      FormManager.hide('SalesBusOff2', 'dupSalesBoCd');
    }
  }
}

/**
 * Austria - sets Sales rep based on isuCtc
 */
function setSalesRepValues(clientTier) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
    return;
  }
  if (!clientTier || clientTier == '') {
    clientTier = FormManager.getActualValue('clientTier');
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var isuCd = FormManager.getActualValue('isuCd');
  var ims = FormManager.getActualValue('subIndustryCd');

  var salesReps = [];
  if (isuCd != '') {
    var isuCtc = isuCd + clientTier;
    var qParams = null;
    var results = null;
    // SalRep will be based on IMS for 32S/32T
    if (ims != '' && ims.length > 1 && (isuCtc == '32S' || isuCtc == '32T')) {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry,
        ISU : '%' + isuCd + clientTier + '%',
        CLIENT_TIER : '%' + ims.substring(0, 1) + '%'
      };
      results = cmr.query('GET.SRLIST.BYISUCTC', qParams);
    } else {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry,
        ISU : '%' + isuCd + clientTier + '%'
      };
      results = cmr.query('GET.SRLIST.BYISU', qParams);
    }

    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        // aad Defect 1816727-fix blank issue.
        if (results[i].ret1 != '000009') {
          salesReps.push(results[i].ret1);
        }
      }
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesReps);
      if (salesReps.length == 1) {
        FormManager.setValue('repTeamMemberNo', salesReps[0]);
        setSBO();
      }
    }
  }
  // Defect 1705993: Austria - ISU logic based on Subindustry fix
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp != null && (custSubGrp == 'COMME' || custSubGrp == 'XCOM')) {
    if (ims != '' && ims.length > 1 && ims.substring(0, 1).toUpperCase() == 'B') {
      FormManager.setValue('isuCd', '32');
      FormManager.setValue('clientTier', 'N');
    }
  }
}

/**
 * CMR-2101:Austria - sets SBO based on isuCtc
 */
function setSBOValuesForIsuCtc() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
    return;
  }

  if ('U' == FormManager.getActualValue('reqType')) {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  var ims = FormManager.getActualValue('subIndustryCd');
  var isuCtc = isuCd + clientTier;
  var qParams = null;
  // SBO will be based on IMS
  if (isuCd != '') {
    var results = null;
    if (ims != '' && ims.length > 1 && (isuCtc == '32S' || isuCtc == '32N')) {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry,
        ISU : '%' + isuCd + clientTier + '%',
        CLIENT_TIER : '%' + ims.substring(0, 1) + '%'
      };
      results = cmr.query('GET.SBOLIST.BYISUCTC', qParams);
    } else {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry,
        ISU : '%' + isuCd + clientTier + '%'
      };
      results = cmr.query('GET.SBOLIST.BYISU', qParams);
    }
    console.log("there are " + results.length + " SBO returned.");

    var readOnly = false;
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (custSubGrp == 'IBMEM' && results.length > 0) {
      FormManager.setValue('salesBusOffCd', "099");
      readOnly = true;
    } else if (custSubGrp == 'BUSPR' || custSubGrp == 'XBP') {
      FormManager.setValue('salesBusOffCd', "080");
      readOnly = true;
    } else if (custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'XINT' || custSubGrp == 'XISO') {
      FormManager.setValue('salesBusOffCd', "000");
      readOnly = true;
    } else if (results.length > 1) {
      FormManager.setValue('salesBusOffCd', "");
    } else if (results.length == 1) {
      FormManager.setValue('salesBusOffCd', results[0].ret1);
      readOnly = true;
    } else {
      FormManager.setValue('salesBusOffCd', "");
    }

    if (readOnly) {
      // experimental might need to remove later
      FormManager.readOnly('salesBusOffCd');
    }
  }
}

/**
 * CEMEA - sets SBO based on SR value
 */
function setSBO(repTeamMemberNo) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var role = FormManager.getActualValue('userRole');
  /**
   * CMR-2046 AT for update can also get SBO when change ISR
   */
  if (FormManager.getActualValue('reqType') != 'C' && cntry != SysLoc.AUSTRIA) {
    return;
  }

  if (cntry != SysLoc.AUSTRIA && role != GEOHandler.ROLE_PROCESSOR) {
    return;
  }

  if (!repTeamMemberNo || repTeamMemberNo == '') {
    repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  }

  var custGrp = FormManager.getActualValue('custGrp');

  // exclusive countries - use sbo mapping for GBM/SBM types only
  if (CEMEA_EXCL.has(cntry) && custGrp != null && !(custGrp == 'GBM' || custGrp == 'SBM')) {
    FormManager.setValue('salesBusOffCd', cntry + '0000');
    return;
  }

  if (repTeamMemberNo != '') {
    var qParams = {
      ISSUING_CNTRY : cntry,
      REP_TEAM_CD : repTeamMemberNo
    };
    var result = cmr.query('GET.SBO.BYSR', qParams);
    if (result != null && result.ret1 != '') {
      FormManager.setValue('salesBusOffCd', result.ret1);
    } else {
      FormManager.setValue('salesBusOffCd', '');
    }
  } else {
    // CMR-2053 AT can import SBO without ISR
    if (cntry != SysLoc.AUSTRIA) {
      FormManager.setValue('salesBusOffCd', '');
    }
  }
}

// CMR-2101 SBO is required for processor
function validateSBO() {
  if (FormManager.getActualValue('userRole') == GEOHandler.ROLE_PROCESSOR) {
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ], 'MAIN_IBM_TAB');
  }

}

/**
 * Russia CIS - sets SBO2 based on SR value and CMR Duplicate country
 */
function setSBO2(dupSalesRepNo) {
  if (FormManager.getActualValue('userRole') != GEOHandler.ROLE_PROCESSOR) {
    return;
  }
  if (!dupSalesRepNo || dupSalesRepNo == '') {
    dupSalesRepNo = FormManager.getActualValue('dupSalesRepNo');
  }

  var dupIssuingCntryCd = FormManager.getActualValue('dupIssuingCntryCd');

  if (dupSalesRepNo != '') {
    var qParams = {
      ISSUING_CNTRY : dupIssuingCntryCd,
      REP_TEAM_CD : dupSalesRepNo
    };
    var result = cmr.query('GET.SBO.BYSR', qParams);
    if (result != null && result.ret1 != '') {
      FormManager.setValue('dupSalesBoCd', result.ret1);
    } else {
      FormManager.setValue('dupSalesBoCd', '');
    }
  } else {
    FormManager.setValue('dupSalesBoCd', '');
  }
}

/**
 * CEEME - show CoF field for BP and COM scenarios only
 */
function setCommercialFinanced() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    return;
  }

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp != null && (custSubGrp.includes('COM') || custSubGrp.includes('BP') || custSubGrp.includes('BUSPR'))) {
    FormManager.show('CommercialFinanced', 'commercialFinanced');
  } else {
    FormManager.clearValue('commercialFinanced');
    FormManager.hide('CommercialFinanced', 'commercialFinanced');
  }
}

/**
 * CEEME - show TeleCoverageRep for GBM and SBM scenarios
 */
function setTelecoverageRep() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    return;
  }

  var custGrp = FormManager.getActualValue('custGrp');
  if (custGrp != null && (custGrp == 'GBM' || custGrp == 'SBM')) {
    checkAndAddValidator('bpSalesRepNo', Validators.REQUIRED, [ 'Tele-coverage rep.' ]);
    FormManager.show('TeleCoverageRep', 'bpSalesRepNo');
  } else {
    FormManager.resetValidations('bpSalesRepNo');
    FormManager.clearValue('bpSalesRepNo');
    FormManager.hide('TeleCoverageRep', 'bpSalesRepNo');
  }
}
/**
 * Validate Special Character for Abbreviated Name/Location
 */
function validateAbbrevNmLocn() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'PROCESSOR') {
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    /**
     * remove special chars check for CMR-811
     */
    // FormManager.addValidator('abbrevNm', Validators.NO_SPECIAL_CHAR, [
    // 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    // FormManager.addValidator('abbrevLocn', Validators.NO_SPECIAL_CHAR, [
    // 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  }
}

function validateAbbrevNmForCIS() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = FormManager.getActualValue('userRole');
        var abbrevNm = FormManager.getActualValue('abbrevNm');
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.RUSSIA) {
          return new ValidationResult(null, true);
        }
        if (role == GEOHandler.ROLE_PROCESSOR) {
          if (dijit.byId('cisServiceCustIndc').get('checked')) {
            if (abbrevNm != '' && !abbrevNm.endsWith(' CIS')) {
              return new ValidationResult(null, false, 'Abbreviated Name should end with \' CIS\' for CIS requests.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function executeBeforeSubmit() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U' && dijit.byId('cisServiceCustIndc').get('checked')) {
    cmr.showConfirm('proceedCIS()', 'You are updating record with duplicate, if you wish to continue click Yes, otherwise No.', null, 'cancelCIS()', {
      OK : 'Yes',
      CANCEL : 'No'
    });
  } else {
    proceedCIS();
  }
}

function proceedCIS() {
  cmr.showModal('addressVerificationModal');
}

function cancelCIS() {
  FormManager.setValue('cisServiceCustIndc', false);
  setCountryDuplicateFields();
  cmr.showModal('addressVerificationModal');
}

function changeAbbrevNmLocn(cntry, addressMode, saving, finalSave, force) {
  if (finalSave || force || addressMode == 'COPY') {
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
    if (addrType == 'ZS01' || copyingToA) {
      // generate Abbreviated Name/Location
      var role = FormManager.getActualValue('userRole').toUpperCase();
      var abbrevNm = FormManager.getActualValue('custNm1');
      var abbrevLocn = FormManager.getActualValue('city1');
      if (FormManager.getActualValue('reqType') != 'C') {
        return;
      }
      if (role == 'REQUESTER') {
        if (abbrevNm && abbrevNm.length > 30) {
          abbrevNm = abbrevNm.substring(0, 30);
        }
        if (abbrevLocn && abbrevLocn.length > 12) {
          abbrevLocn = abbrevLocn.substring(0, 12);
        }
        FormManager.setValue('abbrevNm', abbrevNm);
        FormManager.setValue('abbrevLocn', abbrevLocn);

      }
      // CMR-837
      changeBetachar();
    }
  }
}

function setAbbrvNmLoc() {
  console.log("setting abbrvName and location");
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
  var city = cmr.query('ADDR.GET.CITY1.BY_REQID', reqParam);
  var abbrvNm = custNm.ret1;
  var abbrevLocn = city.ret1;

  if (FormManager.getActualValue('reqType') == 'C') {
    if (abbrvNm && abbrvNm.length > 30) {
      abbrvNm = abbrvNm.substring(0, 30);
    }
    if (abbrevLocn && abbrevLocn.length > 12) {
      abbrevLocn = abbrevLocn.substring(0, 12);
    }
  }

  if (abbrevLocn != null) {
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }
  if (abbrvNm != null) {
    FormManager.setValue('abbrevNm', abbrvNm);
  }
}

function lockAbbrv() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (viewOnlyPage == 'true') {
    FormManager.readOnly('abbrevLocn');
    FormManager.readOnly('abbrevNm');
  } else {
    if (role == 'REQUESTER') {
      FormManager.readOnly('abbrevNm');
      FormManager.readOnly('abbrevLocn');
    }
  }
}
// CMR-852
function lockAbbrvLocnForScenrio() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType == 'SOFTL') {
    if (role == 'REQUESTER' || role == 'PROCESSOR') {
      FormManager.readOnly('abbrevLocn');
    }
  } else if (custSubType == 'IBMEM' || custSubType == 'COMME') {
    if (role == 'REQUESTER') {
      FormManager.readOnly('abbrevLocn');
    }
  }
}

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    $('label[for="custNm1_view"]').text('Customer Name:');
    $('label[for="custNm2_view"]').text('Customer Name Continuation:');
    $('label[for="custNm3_view"]').text('');
    $('label[for="landCntry_view"]').text('Country (Landed):');
    $('label[for="addrTxt_view"]').text('Street:');
  }
}

function custNmAttnPersonPhoneValidation() {
  var attn = FormManager.getActualValue('custNm4');
  var phone = FormManager.getActualValue('custPhone');
  var cust3 = FormManager.getActualValue('custNm3');

  if (cust3 != null && cust3.trim().length > 0) {
    FormManager.clearValue('custNm4');
    FormManager.disable('custNm4');
    FormManager.clearValue('custPhone');
    FormManager.disable('custPhone');
  } else if (cust3 == null || cust3.trim().length == 0) {
    FormManager.enable('custNm4');
    FormManager.enable('custPhone');
  }

  if ((attn != null && attn.trim().length > 0) || (phone != null && phone.trim().length > 0)) {
    FormManager.clearValue('custNm3');
    FormManager.disable('custNm3');
  } else if ((attn == null || attn.trim().length == 0) && (phone == null || phone.trim().length == 0)) {
    FormManager.enable('custNm3');
  }
}

function custNmAttnPersonPhoneValidationOnChange() {
  var fields = [ 'custNm3', 'custNm4', 'custPhone' ];

  for (var i = 0; i < fields.length; i++) {
    dojo.connect(FormManager.getField(fields[i]), 'onChange', function(value) {
      custNmAttnPersonPhoneValidation();
    });
  }
}

function reqReasonOnChange() {
  var reqReason = FormManager.getActualValue('reqReason');
  if (reqReason == 'IGF' && isZD01OrZP01ExistOnCMR()) {
    // FormManager.limitDropdownValues(FormManager.getField('custSubGrp'), [
    // 'BUSPR', 'COMME', 'GOVRN', 'IBMEM', 'XBP', 'XCOM', 'XGOV']);
    dojo.byId('radiocont_ZP02').style.display = 'inline-block';
    dojo.byId('radiocont_ZD02').style.display = 'inline-block';
  } else {
    dojo.byId('radiocont_ZP02').style.display = 'none';
    dojo.byId('radiocont_ZD02').style.display = 'none';
  }
  dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
    if (value == 'IGF' && isZD01OrZP01ExistOnCMR()) {
      // FormManager.limitDropdownValues(FormManager.getField('custSubGrp'), [
      // 'BUSPR', 'COMME', 'GOVRN', 'IBMEM', 'XBP', 'XCOM', 'XGOV']);
      dojo.byId('radiocont_ZP02').style.display = 'inline-block';
      dojo.byId('radiocont_ZD02').style.display = 'inline-block';
    } else {
      // FormManager.resetDropdownValues(FormManager.getField('custSubGrp'));
      dojo.byId('radiocont_ZP02').style.display = 'none';
      dojo.byId('radiocont_ZD02').style.display = 'none';
    }
  });
}

function isZD01OrZP01ExistOnCMR() {
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
    if ('U' == reqType && 'Y' == importInd && (type == 'ZD01' || type == 'ZP01')) {
      return true;
    }
  }
  return false;
}

function phoneNoValidation() {
  var phone = FormManager.getActualValue('custPhone');
  var attn = FormManager.getActualValue('custNm4');
  if (phone != null && phone.trim().length > 0) {
    FormManager.clearValue('custNm3');
    FormManager.disable('custNm3');
  } else if (attn == null || attn.trim().length == 0) {
    FormManager.enable('custNm3');
  }
}

function phoneNoValidationOnChange() {
  dojo.connect(FormManager.getField('custPhone'), 'onChange', function(value) {
    phoneNoValidation();
  });
}

function setEnterpriseValues(clientTier) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || FormManager.getActualValue('custSubGrp') == 'IBMEM') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var isuCd = FormManager.getActualValue('isuCd');
  FormManager.enable('enterprise');
  clientTier = FormManager.getActualValue('clientTier');

  var enterprises = [];
  if (isuCd != '' && clientTier != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCd + clientTier + '%'
    };
    var results = cmr.query('GET.ENTLIST.BYISU', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        enterprises.push(results[i].ret1);
      }
      if (enterprises != null) {
        FormManager.limitDropdownValues(FormManager.getField('enterprise'), enterprises);
        if (enterprises.length == 1) {
          FormManager.setValue('enterprise', enterprises[0]);
        }
      }
    }
  }
}

function setEnterprise2Values(dupClientTierCd) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var dupIssuingCntryCd = FormManager.getActualValue('dupIssuingCntryCd');
  var dupIsuCd = FormManager.getActualValue('dupIsuCd');
  FormManager.enable('dupEnterpriseNo');
  dupClientTierCd = FormManager.getActualValue('dupClientTierCd');

  var enterprises = [];
  if (isuCd != '' && dupClientTierCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : dupIssuingCntryCd,
      ISU : '%' + dupIsuCd + dupClientTierCd + '%'
    };
    var results = cmr.query('GET.ENTLIST.BYISU', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        enterprises.push(results[i].ret1);
      }
      if (enterprises != null) {
        FormManager.limitDropdownValues(FormManager.getField('dupEnterpriseNo'), enterprises);
        if (enterprises.length == 1) {
          FormManager.setValue('dupEnterpriseNo', enterprises[0]);
        }
      }
    }
  }
}

function populateBundeslandercode() {
  var subindustry = FormManager.getActualValue('subIndustryCd');
  var landCntry = FormManager.getActualValue('landCntry');

  var custGrp = FormManager.getActualValue('custGrp');
  if (custGrp == 'CROSS' || landCntry != 'AT') {
    if (subindustry != null) {
      FormManager.setValue('locationNumber', '001' + subindustry);
      return;
    }
  }

  var postCd = getPostCode();
  var reqParam = {
    CD : postCd
  };
  var results = cmr.query('GETTXT.BYPOSTCODE.AUSTRIA', reqParam);
  if (results != null) {
    var postCd = results.ret1;
    if (postCd != null && subindustry != null) {
      FormManager.setValue('locationNumber', postCd + subindustry);
    }
  }
}

function populateBundeslandercodeOnChange() {
  dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
    populateBundeslandercode();
  });
  dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
    populateBundeslandercode();
  });
}

function getPostCode() {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var results = cmr.query('ADDR.GET.POSTCD.AUSTRIA', reqParam);
  if (results != null) {
    return results.ret1;
  }
}

function setAbbrvNmLocMandatoryProcessor() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnlyPage != 'true') {
    if (cntry == '618') {
      return;
    }
    if (role == 'PROCESSOR') {
      checkAndAddValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ]);
      checkAndAddValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ]);
    } else {
      FormManager.resetValidations('abbrevNm');
      FormManager.resetValidations('abbrevLocn');
    }
  }
}

var _addrTypesForMA = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'ZP02' ];
var addrTypeHandler = [];

function displayIceForMA() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var scenario = FormManager.getActualValue('custGrp');

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress' && FormManager.getActualValue('cmrIssuingCntry') == '642') {
    cmr.hideNode('ice');
    for (var i = 0; i < _addrTypesForMA.length; i++) {
      if (addrTypeHandler[i] == null) {
        addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForMA[i]), 'onClick', function(value) {
          if (FormManager.getField('addrType_ZP01').checked) {
            if (cmr.currentRequestType == 'C' && scenario == 'LOCAL') {
              cmr.showNode('ice');
              if (role == 'REQUESTER') {
                FormManager.addValidator('dept', Validators.REQUIRED, [ 'ICE#' ], '');
              } else {
                FormManager.resetValidations('dept');
              }
            } else if (cmr.currentRequestType == 'U') {
              cmr.showNode('ice');
              FormManager.resetValidations('dept');
            } else {
              cmr.hideNode('ice');
              FormManager.resetValidations('dept');
            }
          } else {
            cmr.hideNode('ice');
            FormManager.resetValidations('dept');
          }
        });
      } else {
        if (FormManager.getField('addrType_ZP01').checked && ((cmr.currentRequestType == 'C' && scenario == 'LOCAL') || cmr.currentRequestType == 'U'))
          cmr.showNode('ice');
      }
    }
  }
  if (cmr.addressMode == 'updateAddress') {
    if (FormManager.getActualValue('addrType') == 'ZP01') {
      cmr.showNode('ice');
      if (role == 'REQUESTER' && cmr.currentRequestType == 'C' && scenario == 'LOCAL') {
        FormManager.addValidator('dept', Validators.REQUIRED, [ 'ICE#' ], '');
      } else {
        FormManager.resetValidations('dept');
      }
    } else {
      cmr.hideNode('ice');
      FormManager.resetValidations('dept');
    }
  }
}

// Story 1733554 Moroco for ICE field Formator

function addIceFormatValidationMorocco() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var dept = FormManager.getActualValue('dept');
        var reqType = FormManager.getActualValue('reqType');
        // var lbl1 = FormManager.getLabel('LocalTax1');
        if (reqType == 'C') {
          if (FormManager.getField('addrType_ZP01').checked && dept && dept.length > 0 && !dept.match("([0-9]{15})|^(X{3})$|^(x{3})$")) {
            return new ValidationResult({
              id : 'dept',
              type : 'text',
              name : 'dept'
            }, false, 'Invalid format of ICE#. Format should be NNNNNNNNNNNNNNN or "XXX" or "xxx"');
          }
        }
        if (reqType == 'U') {
          if (FormManager.getField('addrType_ZP01').checked && dept && dept.length > 0 && !dept.match("([0-9]{15})|^(X{3})$|^(x{3})$|^(@{1})$")) {
            return new ValidationResult({
              id : 'dept',
              type : 'text',
              name : 'dept'
            }, false, 'Invalid format of ICE#. Format should be NNNNNNNNNNNNNNN or "XXX" or "xxx" or "@"');
          }

        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addIceBillingValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var billingBool = true;
        var reqType = FormManager.getActualValue('reqType');
        var reqId = FormManager.getActualValue('reqId');
        var addr = 'ZP01';
        var qParams = {
          _qall : 'Y',
          REQID : reqId,
          ADDR_TYPE : addr
        };

        var results = cmr.query('GET_ICE_ADDRSEQ', qParams);
        if (results != null) {
          for (var i = 0; i < results.length; i++) {
            if (results[i].ret1.length < 1) {
              billingBool = false;
            }
          }
        }

        if (billingBool) {
          return new ValidationResult(null, true);
        } else if (!billingBool && reqType == 'C') {
          return new ValidationResult(null, false, 'ICE should not be empty in Billing Address.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setVatValidator() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custGroup = FormManager.getActualValue('custGrp');

  if ((role == 'PROCESSOR' || role == 'REQUESTER') && (cntry == '620') && custGroup == 'LOCAL') {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
  } else {
    FormManager.removeValidator('vat', Validators.REQUIRED);
  }
}

function setChecklistStatus() {
  console.log('validating checklist..');
  var checklist = dojo.query('table.checklist');
  document.getElementById("checklistStatus").innerHTML = "Not Done";
  var reqId = FormManager.getActualValue('reqId');
  var questions = checklist.query('input[type="radio"]');

  if (reqId != null && reqId.length > 0 && reqId != 0) {
    if (questions.length > 0) {
      var noOfQuestions = questions.length / 2;
      var checkCount = 0;
      for (var i = 0; i < questions.length; i++) {
        if (questions[i].checked) {
          checkCount++;
        }
      }
      if (noOfQuestions != checkCount) {
        document.getElementById("checklistStatus").innerHTML = "Incomplete";
        FormManager.setValue('checklistStatus', "Incomplete");
      } else {
        document.getElementById("checklistStatus").innerHTML = "Complete";
        FormManager.setValue('checklistStatus', "Complete");
      }

      if (questions[14].checked) {
        // if question 8 = YES, country field is required
        var country = checklist.query('input[name="freeTxtField1"]');
        if (country.length > 0 && country[0].value.trim() == '') {
          document.getElementById("checklistStatus").innerHTML = "Incomplete";
          FormManager.setValue('checklistStatus', "Incomplete");
        }
      }
    } else {
      document.getElementById("checklistStatus").innerHTML = "Complete";
      FormManager.setValue('checklistStatus', "Complete");
    }
  }
}

function addCEMEAChecklistValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('validating checklist..');
        var checklist = dojo.query('table.checklist');

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

          // if question 8 = YES, country field is required
          if (questions[14].checked) {
            var country = checklist.query('input[name="freeTxtField1"]');
            if (country.length > 0 && country[0].value.trim() == '') {
              return new ValidationResult(null, false, 'Checklist has not been fully accomplished. Item #8 Re-export field is required.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CHECKLIST_TAB', 'frmCMR');
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
/* End 1430539 */

function cmrNoEnabled() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('cmrNo');
    return;
  }
  FormManager.enable('cmrNo');
}

function postCdLenChecks() {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var results = cmr.query('DATA.GET.CMR.BY_REQID', reqParam);
  var cmrcntry = results.ret1;
  switch (cmrcntry) {
  case '741':
    lenValidator(6, cmrcntry);
    break;
  case '889':
    lenValidator(5, cmrcntry);
    break;
  case '694':
    lenValidator(6, cmrcntry);
    break;
  case '695':
    lenValidator(6, cmrcntry);
    break;
  case '699':
    lenValidator(5, cmrcntry);
    break;
  case '705':
    lenValidator(4, cmrcntry);
    break;
  case '708':
    lenValidator(4, cmrcntry);
    break;
  case '359':
    lenValidator(6, cmrcntry);
    break;
  case '363':
    lenValidator(6, cmrcntry);
    break;
  case '603':
    lenValidator(4, cmrcntry);
    break;
  case '607':
    lenValidator(4, cmrcntry);
    break;
  case '626':
    lenValidator(6, cmrcntry);
    break;
  case '644':
    lenValidator(4, cmrcntry);
    break;
  case '651':
    lenValidator(4, cmrcntry);
    break;
  case '693':
    lenValidator(6, cmrcntry);
    break;
  case '668':
    lenValidator(6, cmrcntry);
    break;
  }
}

function lenValidator(len, cmrcntry) {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var custGroup = FormManager.getActualValue('custGrp');
        var postCd = FormManager.getActualValue('postCd');
        var landed = FormManager.getActualValue('landCntry');
        var listNonAuto = new Array();
        listNonAuto = [ '741', '889', '694', '695', '699', '705', '708', '359', '363', '603', '607', '626', '644', '651', '693', '668' ];
        var cntryName = cmr.query('GET_CMT_FOR_LANDED', {
          COUNTRY_CD : landed
        });
        var cmt = cntryName.ret1;
        if (reqType == 'U') {
          return new ValidationResult(null, true);
        }

        if (custGroup == 'LOCAL') {
          if (postCd == null || postCd == '') {
            if ((cmrcntry == cmt) || (listNonAuto.indexOf(cmt) > -1)) {
              checkAndAddValidator('postCd', Validators.REQUIRED, [ 'Postal Code' ]);
              return new ValidationResult(FormManager.getField('postCd'), false, 'Postal Code is required.');
            } else {
              return new ValidationResult(null, true);
            }
          } else if ((postCd != '' && postCd.length != len) && (cmrcntry == cmt)) {
            return new ValidationResult(FormManager.getField('postCd'), false, 'Postal Code should be ' + len + ' characters long.');
          } else if ((postCd != '' && postCd.length != len) && (listNonAuto.indexOf(cmt) > -1)) {
            var table = new Array();
            table = [ {
              cntry : '741',
              len : 6
            }, {
              cntry : '889',
              len : 5
            }, {
              cntry : '694',
              len : 6
            }, {
              cntry : '695',
              len : 6
            }, {
              cntry : '699',
              len : 5
            }, {
              cntry : '705',
              len : 4
            }, {
              cntry : '708',
              len : 4
            }, {
              cntry : '359',
              len : 6
            }, {
              cntry : '363',
              len : 6
            }, {
              cntry : '603',
              len : 4
            }, {
              cntry : '607',
              len : 4
            }, {
              cntry : '626',
              len : 6
            }, {
              cntry : '644',
              len : 4
            }, {
              cntry : '651',
              len : 4
            }, {
              cntry : '693',
              len : 6
            }, {
              cntry : '668',
              len : 6
            }, ];
            for (var i = 0; i < table.length; i++) {
              if (table[i].cntry == cmt) {
                if (table[i].len == postCd.length) {
                  return new ValidationResult(null, true);
                  break;
                } else {
                  return new ValidationResult(FormManager.getField('postCd'), false, 'Postal Code should be ' + table[i].len + ' characters long.');
                  break;
                }
              }
            }
            return new ValidationResult(null, true);

          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function resetVatExempt() {
  var val = FormManager.getActualValue('vat');
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (val != null && val.length > 0) {
    var subGrp = new Array();
    subGrp = [ 'SOFTL', 'INTER', 'PRICU', 'CEMEX', 'XCOM', 'XCEM', 'XBP', 'XTP', 'XINT', 'XPC', 'XSL', 'ELCOM', 'ELBP', 'EXCOM', 'EXBP' ];
    for (var i = 0; i < subGrp.length; i++) {
      if (custSubType == subGrp[i]) {
        if (dijit.byId('vatExempt').get('checked')) {
          FormManager.getField('vatExempt').set('checked', false);
        }
        break;
      }
    }
  }
}

function resetVatExemptOnchange() {
  dojo.connect(FormManager.getField('vat'), 'onChange', function(value) {
    resetVatExempt();
  });
}

function lockLocationNo() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (viewOnlyPage == 'true') {
    FormManager.readOnly('locationNumber');
  } else {
    if (role == 'REQUESTER') {
      FormManager.readOnly('locationNumber');
    }
  }
}

function cemeaCustomVATValidator(cntry, tabName, formName, aType) {
  return function() {
    FormManager.addFormValidator((function() {
      var landCntry = cntry;
      var addrType = aType;
      return {
        validate : function() {
          var reqType = FormManager.getActualValue('reqType');
          var vat = FormManager.getActualValue('vat');
          var cntryUsed = '';
          var result = cmr.query('GET_CNTRYUSED', {
            REQ_ID : FormManager.getActualValue('reqId'),
          });
          if (result && result.ret1 && result.ret1 != '') {
            cntryUsed = result.ret1;
          }

          if (cntryUsed != null && cntryUsed.length > 0) {
            switch (cntryUsed) {
            /*
             * case '707ME': return new ValidationResult(null, true); break;
             * case '707CS': return new ValidationResult(null, true); break;
             * case '808AF': return new ValidationResult(null, true); break;
             */
            }
          }

          if (!vat || vat == '' || vat.trim() == '') {
            return new ValidationResult(null, true);
          } else if (reqType == 'U' && vat == '@') {
            // vat deletion for updates
            return new ValidationResult(null, true);
          }

          var zs01Cntry = landCntry;
          var addrExist = false;
          if (addrType != null && addrType != '') {
            var addrResult = cmr.query('GET.ADDR_BY_REQID_TYPE', {
              REQ_ID : FormManager.getActualValue('reqId'),
              ADDR_TYPE : addrType
            });
            if (addrResult && addrResult.ret1 && addrResult.ret1 != '') {
              addrExist = true;
            }
          }

          if (!addrExist) {
            addrType = 'ZS01';
          }

          var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
            REQID : FormManager.getActualValue('reqId'),
            TYPE : addrType
          });
          if (ret && ret.ret1 && ret.ret1 != '') {
            zs01Cntry = ret.ret1;
          }
          console.log(addrType + ' VAT Country: ' + zs01Cntry);

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

function customCrossPostCdValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var postCd = FormManager.getActualValue('postCd');
        var landed = FormManager.getActualValue('landCntry');
        var cmrIssuing = FormManager.getActualValue('cmrIssuingCntry');
        var listNonAuto = new Array();
        listNonAuto = [ '741', '889', '694', '695', '699', '705', '708', '359', '363', '603', '607', '626', '644', '651', '693', '668' ];
        var cntryName = cmr.query('GET_CMT_FOR_LANDED', {
          COUNTRY_CD : landed
        });
        var cmt = cntryName.ret1;
        if (reqType == 'U') {
          return new ValidationResult(null, true);
        }

        var scenario = FormManager.getActualValue('custGrp');
        var subType = FormManager.getActualValue('custSubGrp');
        if (scenario != null && (scenario.includes('CRO') || subType.includes('EX'))) {
          scenario = 'CROSS';
        }

        var isLocal;
        if (cmrIssuing == cmt) {
          isLocal = true;
        } else {
          isLocal = false;
        }

        if (scenario == 'CROSS' || (isLocal == false)) {
          if (postCd == null || postCd == '') {
            if (listNonAuto.indexOf(cmt) > -1) {
              checkAndAddValidator('postCd', Validators.REQUIRED, [ 'Postal Code' ]);
              return new ValidationResult(FormManager.getField('postCd'), false, 'Postal Code is required.');
            } else {
              var result = cmr.validateZIP(landed, postCd, cmt);
              if (result && !result.success) {
                if (result.errorPattern == null) {
                  return new ValidationResult({
                    id : 'postCd',
                    type : 'text',
                    name : 'postCd'
                  }, false, (result.errorMessage ? result.errorMessage : 'Cannot get error message for Postal Code.') + '.');
                } else {
                  var msg = result.errorMessage + '. Format should be ' + result.errorPattern.formatReadable;
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
          } else if (postCd != '' && (listNonAuto.indexOf(cmt) > -1)) {
            var table = new Array();
            table = [ {
              cntry : '741',
              len : 6
            }, {
              cntry : '889',
              len : 5
            }, {
              cntry : '694',
              len : 6
            }, {
              cntry : '695',
              len : 6
            }, {
              cntry : '699',
              len : 5
            }, {
              cntry : '705',
              len : 4
            }, {
              cntry : '708',
              len : 4
            }, {
              cntry : '359',
              len : 6
            }, {
              cntry : '363',
              len : 6
            }, {
              cntry : '603',
              len : 4
            }, {
              cntry : '607',
              len : 4
            }, {
              cntry : '626',
              len : 6
            }, {
              cntry : '644',
              len : 4
            }, {
              cntry : '651',
              len : 4
            }, {
              cntry : '693',
              len : 6
            }, {
              cntry : '668',
              len : 6
            }, ];
            for (var i = 0; i < table.length; i++) {
              if (table[i].cntry == cmt) {
                if (table[i].len == postCd.length) {
                  var result = cmr.validateZIP(landed, postCd, cmt);
                  if (result && !result.success) {
                    if (result.errorPattern == null) {
                      return new ValidationResult({
                        id : 'postCd',
                        type : 'text',
                        name : 'postCd'
                      }, false, (result.errorMessage ? result.errorMessage : 'Cannot get error message for Postal Code.') + '.');
                    } else {
                      var msg = result.errorMessage + '. Format should be ' + result.errorPattern.formatReadable;
                      return new ValidationResult({
                        id : 'postCd',
                        type : 'text',
                        name : 'postCd'
                      }, false, msg);
                    }
                  } else {
                    return new ValidationResult(null, true);
                  }
                  break;
                } else {
                  return new ValidationResult(FormManager.getField('postCd'), false, 'Postal Code should be ' + table[i].len + ' characters long.');
                  break;
                }
              }
            }
            return new ValidationResult(null, true);

          } else if (postCd != '' && (listNonAuto.indexOf(cmt) == -1)) {
            var result = cmr.validateZIP(landed, postCd, cmt);
            if (result && !result.success) {
              if (result.errorPattern == null) {
                return new ValidationResult({
                  id : 'postCd',
                  type : 'text',
                  name : 'postCd'
                }, false, (result.errorMessage ? result.errorMessage : 'Cannot get error message for Postal Code.') + '.');
              } else {
                var msg = result.errorMessage + '. Format should be ' + result.errorPattern.formatReadable;
                return new ValidationResult({
                  id : 'postCd',
                  type : 'text',
                  name : 'postCd'
                }, false, msg);
              }
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function hideEngineeringBOForReq() {
  if (FormManager.getActualValue('viewOnlyPage') != 'true') {
    var cmrIssuing = FormManager.getActualValue('cmrIssuingCntry');
    if (cmrIssuing != '618') {
      var role = FormManager.getActualValue('userRole');
      if (role == GEOHandler.ROLE_REQUESTER) {
        console.log("Engineering BO Hidden for CEE countries");
        FormManager.hide('EngineeringBo', 'engineeringBo');
      } else {
        console.log("Engineering BO Displayed");
        FormManager.show('EngineeringBo', 'engineeringBo');
      }
    }
  }
}

function requireVATForCrossBorderAT() {
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
          TYPE : 'ZP01'
        });
        if (ret && ret.ret1 && ret.ret1 != '') {
          zs01Cntry = ret.ret1;
        }

        if ((!vat || vat == '' || vat.trim() == '') && !dijit.byId('vatExempt').get('checked')) {
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

function cemeaCustomVATMandatory() {
  console.log('1cemeaCustomVATMandatory ');
  var landCntry = '';
  var addrType = 'ZP01';
  var listVatReq = [ 'AT', 'AE', 'BG', 'HR', 'CS', 'CZ', 'EG', 'HU', 'KZ', 'PK', 'PL', 'RO', 'RU', 'SA', 'RS', 'SK', 'SI', 'UA' ];

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  // Internal, Softlayer, & Private scenario - set vat to optional
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType != null && custSubType != '' && (custSubType.includes('IN') || custSubType == 'SOFTL' || custSubType.includes('SL') || custSubType == 'PRICU' || custSubType.includes('PC'))) {
    FormManager.resetValidations('vat');
    return;
  }

  /*
   * var result = cmr.query('GET_CNTRYUSED', { REQ_ID :
   * FormManager.getActualValue('reqId'), }); if (result && result.ret1 &&
   * result.ret1 != '') { cntryUsed = result.ret1; }
   */

  var zs01Cntry = landCntry;

  var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
    REQID : FormManager.getActualValue('reqId'),
    TYPE : addrType ? addrType : 'ZS01'
  });
  if (ret && ret.ret1 && ret.ret1 != '') {
    zs01Cntry = ret.ret1;
  }
  console.log('ZP01 VAT Country: ' + zs01Cntry);

  var indx = listVatReq.indexOf(zs01Cntry);

  if (indx > -1 && !dijit.byId('vatExempt').get('checked')) {
    // Make Vat Mandatory
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    console.log("Vat is Mandatory");
  }

};
// CMR-1912 VAT should be required for Local-BP and Commercial
function customVATMandatoryForAT() {
  console.log('customVATMandatoryForAT ');

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType != null && custSubType != ''
      && (custSubType == 'COMME' || custSubType == 'BUSPR' || custSubType == 'XBP' || custSubType == 'XCOM' || custSubType == 'XGOV' || custSubType == 'XISO' || custSubType == 'XINT')) {
    if (!dijit.byId('vatExempt').get('checked')) {
      // Make Vat Mandatory
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      console.log("Vat is Mandatory.");
    }
  }
};

/*
 * 1496135: Importing G address from SOF for Update Requests jz: add local
 * country name text box
 */
function toggleLocalCountryName(cntry, addressMode, details) {
  if (cntry == '618') {
    return;
  }
  var type = details != null ? details.ret2 : '';
  handleLocalLangCountryName(type);
}

/*
 * 1496135: Importing G address from SOF for Update Requests jz: add local
 * country name text box
 */
function toggleLocalCountryNameOnOpen(cntry, addressMode, saving, afterValidate) {
  if (cntry == '618') {
    return;
  }
  if (!saving) {
    var type = FormManager.getActualValue('addrType');
    handleLocalLangCountryName(type);
  }
}

/**
 * Override method on TemplateService
 */
function executeOnChangeOfAddrType() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == '618') {
    return;
  }
  var type = FormManager.getActualValue('addrType');
  handleLocalLangCountryName(type);
}

// CMR-1962 Austria UAT #12 - 'DSW' in Abbreviated name not needed
// function setAbbrvNmSuffix() {
// var abbrvNm = FormManager.getActualValue('abbrevNm');
// if (FormManager.getActualValue('reqType') == 'C') {
// if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA &&
// FormManager.getActualValue('requestingLob') == 'DSW') {
// if (abbrvNm == null || abbrvNm.length == 0 || abbrvNm.lastIndexOf(" DSW") >
// 0) {
// return;
// }
// if (abbrvNm.length > 30) {
// abbrvNm = abbrvNm.slice(0, 27) + ' DSW';
// } else if (abbrvNm != null && abbrvNm.length > 0) {
// abbrvNm = abbrvNm + ' DSW';
// }
// FormManager.setValue('abbrevNm', abbrvNm);
// }
// }
// }
// CMR-811
function changeBetachar() {
  var abbrvNm = FormManager.getActualValue('abbrevNm');
  var abbrevLocn = FormManager.getActualValue('abbrevLocn');
  if (abbrvNm.indexOf("β")) {
    abbrvNm = abbrvNm.replace(/β/g, "ss");
    FormManager.setValue('abbrevNm', abbrvNm);
  }
  if (abbrevLocn.indexOf("β")) {
    abbrevLocn = abbrevLocn.replace(/β/g, "ss");
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }

}

function handleRequestLOBChange() {
  if (FormManager.getActualValue('reqType') == 'C') {
    dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
      if (FormManager.getActualValue('cmrIssuingCntry') == '618' && FormManager.getActualValue('requestingLob') == 'DSW') {
        var abbrvNm = FormManager.getActualValue('abbrevNm');
        if (abbrvNm == null || abbrvNm.length == 0 || abbrvNm.lastIndexOf(" DSW") > 0) {
          return;
        } else {
          if (abbrvNm.length > 30) {
            abbrvNm = abbrvNm.slice(0, 27) + ' DSW';
          } else {
            abbrvNm = abbrvNm + ' DSW';
            FormManager.setValue('abbrevNm', abbrvNm);
          }
        }
      }
    });
  }
}

function filterCmrnoForAT() {
  var cmrNo = FormManager.getActualValue('cmrNo');
  if (cmrNo.length > 0 && cmrNo.substr(0, 1).toUpperCase() == 'P') {
    FormManager.setValue('cmrNo', '');
  }

  dojo.connect(FormManager.getField('cmrNo'), 'onChange', function(value) {
    if (value.length > 0 && value.substr(0, 1).toUpperCase() == 'P') {
      FormManager.setValue('cmrNo', '');
    }
  });
}

function restrictDuplicateAddrAT(cntry, addressMode, saving, finalSave, force) {
  FormManager.addFormValidator(
      (function() {
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
                return new ValidationResult(null, false,
                    'Only one IGF Bill To address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
              }
            }

            if (addressType != undefined && addressType != '' && addressType == 'ZD02' && cmr.addressMode != 'updateAddress') {
              showDuplicateIGFInstallAtToError = Number(addCount) >= 1 && addressType == 'ZD02';
              if (showDuplicateIGFInstallAtToError) {
                return new ValidationResult(null, false,
                    'Only one IGF Install At to address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
              }
            }

            return new ValidationResult(null, true);
          }
        };
      })(), null, 'frmCMR_addressModal');

}

function handleLocalLangCountryName(type) {
  FormManager.resetValidations('bldg');
  FormManager.resetValidations('landCntry');
  if (type == 'ZP02') {
    // local language
    /* Defect : 1590750 */
    // FormManager.disable('landCntry');
    FormManager.enable('bldg');
    FormManager.mandatory('bldg', 'LocalLangCountryName', null);
  } else {
    // FormManager.enable('landCntry');
    FormManager.disable('bldg');
    FormManager.mandatory('landCntry', 'LocalLangCountryName', null);
    lockLandCntry();
  }
}

/**
 * Override from address.js
 * 
 * @param value
 * @param rowIndex
 * @returns
 */
function countryFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var desc = rowData.countryDesc;
  var type = rowData.addrType;
  var name = rowData.bldg;
  if (type == 'ZP02') {
    return name;
  }
  if (value) {
    if (desc && '' != desc) {
      return '<span class="cmr-grid-tooltip" title="' + desc + '">' + value + '</span>';
    } else {
      return value;
    }
  } else {
    return '';
  }
}

function setTypeOfCustomerRequiredProcessor() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var bpAcctTyp = FormManager.getActualValue('bpAcctTyp');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (bpAcctTyp == '' || bpAcctTyp == null) {
    FormManager.setValue('bpAcctTyp', 'N');
  }
  if (role == 'REQUESTER') {
    FormManager.removeValidator('bpAcctTyp', Validators.REQUIRED);
  } else if (role == 'PROCESSOR') {
    checkAndAddValidator('bpAcctTyp', Validators.REQUIRED, [ 'Type Of Customer' ]);
  }
}

function setScenarioTo3PAOnAddrSave(cntry, addressMode, saving, finalSave, force) {
  if ((finalSave || force) && cmr.addressMode) {
    var addrType = FormManager.getActualValue('addrType');
    var copyTypes = document.getElementsByName('copyTypes');
    var copyingToA = false;
    if (copyTypes != null && copyTypes.length > 0) {
      copyTypes.forEach(function(input, i) {
        if (input.value == 'ZS01' && input.checked) {
          copyingToA = true;
        }
      });
    }
    if ((addrType == 'ZS01' || copyingToA)) {
      var custNm1 = FormManager.getActualValue('custNm1');
      var custNm2 = FormManager.getActualValue('custNm2');
      if (custNm1.concat(custNm2).includes("c/o")) {
        FormManager.setValue('custGrp', 'LOCAL');
        FormManager.setValue('custSubGrp', '3PA');
      }

    }
  }
}

function canCopyAddress(value, rowIndex, grid) {
  return false;
}

function setScenarioTo3PA() {
  // on importing an address , this check should happen
  var impIndc = getImportedIndcForSwiss();
  if (impIndc != 'N') {
    // get zs01 address from ADDR table
    var qParams = {
      _qall : 'Y',
      REQ_ID : FormManager.getActualValue('reqId')
    };
    var results = cmr.query('AT.GET.ADDR.ZS01', qParams);
    var custNm1 = results[0].ret1;
    var custNm2 = results[0].ret2;
    if (custNm1.concat(custNm2).includes("c/o")) {
      FormManager.setValue('custGrp', 'LOCAL');
      FormManager.setValue('custSubGrp', '3PA');
    }
  }
}

var _importedIndc = null;
function getImportedIndcForSwiss() {
  if (_importedIndc) {
    console.log('Returning imported indc = ' + _importedIndc);
    return _importedIndc;
  }
  var results = cmr.query('IMPORTED_ADDR_ZS01', {
    REQID : FormManager.getActualValue('reqId')
  });
  if (results != null && results.ret1) {
    _importedIndc = results.ret1;
  } else {
    _importedIndc = 'N';
  }
  console.log('saving imported ind as ' + _importedIndc);
  return _importedIndc;

}

function similarAddrCheckValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var req_id = FormManager.getActualValue('reqId');
        var req_type = FormManager.getActualValue('reqType');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
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
            addrTxt = record.addrTxt[0];
            city1 = record.city1[0] != null ? record.city1[0] : '';
            stateProv = record.stateProv[0] != null ? record.stateProv[0] : '';
            addrTxt2 = record.addrTxt2[0] != null ? record.addrTxt2[0] : '';
            ;
            landCntry = record.landCntry[0];
            dept = record.dept[0] != null ? record.dept[0] : '';
            importIndc = record.importInd[0] != null ? record.importInd[0] : '';

            if (req_type == 'U' && importIndc == 'Y') {
              continue; // skip the unique check for addresses in case of
              // import for Update requests.
            }
            var qParams = {
              REQ_ID : req_id,
              CUST_NM1 : custNm1,
              CUST_NM2 : custNm2,
              ADDR_TXT : addrTxt,
              CITY1 : city1,
              STATE_PROV : stateProv,
              POST_CD : postCd,
              LAND_CNTRY : landCntry
            };
            var results = cmr.query('GET.SME_ADDR_RECORDS_AT', qParams);
            if (results.ret1 > 1) {
              addrDupExists.push(record.addrTypeText);
            }
          }
          if (addrDupExists.length > 0) {
            return new ValidationResult(null, false, 'Duplicate address details exist for Addresses ' + addrDupExists + '. Delete other addresses except Contract address.');
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

dojo.addOnLoad(function() {
  GEOHandler.CEMEA_COPY = [ '358', '359', '363', '603', '607', '620', '626', '644', '642', '651', '668', '677', '680', '693', '694', '695', '699', '704', '705', '707', '708', '740', '741', '752',
      '762', '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832', '849', '850', '865', '889' ];
  GEOHandler.CEMEA = [ '358', '359', '363', '603', '607', '620', '626', '644', '642', '651', '668', '677', '680', '693', '694', '695', '699', '704', '705', '707', '708', '740', '741', '752', '762',
      '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832', '849', '850', '865', '889', '618' ];
  GEOHandler.CEMEA_CHECKLIST = [ '358', '359', '363', '603', '607', '620', '626', '651', '675', '677', '680', '694', '695', '699', '705', '707', '713', '741', '752', '762', '767', '768', '772',
      '787', '805', '808', '821', '823', '832', '849', '850', '865', '889' ];
  console.log('adding CEMEA functions...');
  GEOHandler.addAddrFunction(addCEMEALandedCountryHandler, GEOHandler.CEMEA);
  // GEOHandler.enableCopyAddress(GEOHandler.CEMEA, validateCEMEACopy);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.CEMEA);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.CEMEA);
  GEOHandler.setRevertIsicBehavior(false);

  GEOHandler.addAfterConfig(afterConfigForCEMEA, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(addHandlersForCEMEA, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(addVatExemptHandler, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(addCISHandler, [ SysLoc.RUSSIA ]);
  GEOHandler.addAfterConfig(setAbbrvNmLoc, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(lockAbbrv, GEOHandler.CEMEA);
  // CMR-801:comment out to unlock embargo code
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.CEMEA);

  // CMR-2096-Austria - "Central order block code"
  GEOHandler.addAfterConfig(lockOrdBlk, SysLoc.AUSTRIA);

  GEOHandler.addAfterConfig(custNmAttnPersonPhoneValidation, [ SysLoc.AUSTRIA ]);
  // GEOHandler.addAfterConfig(setScenarioTo3PA, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterTemplateLoad(lockAbbrvLocnForScenrio, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAddrFunction(lockAbbrvLocnForScenrio, [ SysLoc.AUSTRIA ]);
  // GEOHandler.addAddrFunction(setScenarioTo3PAOnAddrSave, [ SysLoc.AUSTRIA ]);

  GEOHandler.addAfterConfig(custNmAttnPersonPhoneValidationOnChange, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterConfig(reqReasonOnChange, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterConfig(phoneNoValidation, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterConfig(phoneNoValidationOnChange, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterConfig(setEnterpriseValues, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(setVatRequired, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(setPreferredLang, GEOHandler.CEMEA);
  GEOHandler.addAfterTemplateLoad(setVatRequired, GEOHandler.CEMEA);
  // CMR-2101 Austriathe func for Austria
  GEOHandler.addAfterConfig(setSBO, GEOHandler.CEMEA);
  GEOHandler.addAfterTemplateLoad(setSBO, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(setSBO2, [ SysLoc.RUSSIA ]);
  GEOHandler.addAfterTemplateLoad(setSBO2, [ SysLoc.RUSSIA ]);
  GEOHandler.addAfterConfig(setCommercialFinanced, GEOHandler.CEMEA);
  GEOHandler.addAfterTemplateLoad(setCommercialFinanced, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(setTelecoverageRep, GEOHandler.CEMEA);
  GEOHandler.addAfterTemplateLoad(setTelecoverageRep, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(lockLandCntry, GEOHandler.CEMEA);
  GEOHandler.addAfterTemplateLoad(lockLandCntry, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(populateBundeslandercode, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterConfig(populateBundeslandercodeOnChange, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterTemplateLoad(populateBundeslandercode, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterConfig(setAbbrvNmLocMandatoryProcessor, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(setVatValidator, GEOHandler.CEMEA);
  GEOHandler.addAfterTemplateLoad(setVatValidator, GEOHandler.CEMEA);
  GEOHandler.addAfterTemplateLoad(cmrNoEnabled, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(cmrNoEnabled, GEOHandler.CEMEA);
  GEOHandler.addAfterTemplateLoad(afterConfigForCEMEA, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(setCountryDuplicateFields, SysLoc.RUSSIA);
  GEOHandler.addAfterTemplateLoad(setCountryDuplicateFields, SysLoc.RUSSIA);
  GEOHandler.addAfterConfig(setClientTierValues, GEOHandler.CEMEA);
  GEOHandler.addAfterTemplateLoad(setClientTierValues, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(setSBOValuesForIsuCtc, [ SysLoc.AUSTRIA ]); // CMR-2101
  GEOHandler.addAfterConfig(resetVatExempt, GEOHandler.CEMEA);
  GEOHandler.addAfterTemplateLoad(resetVatExempt, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(resetVatExemptOnchange, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(lockLocationNo, [ SysLoc.AUSTRIA ]);

  // GEOHandler.addAfterConfig(setAbbrvNmSuffix, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterConfig(handleRequestLOBChange, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterConfig(filterCmrnoForAT, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterTemplateLoad(filterCmrnoForAT, [ SysLoc.AUSTRIA ]);
  // CMR-811
  GEOHandler.addAfterConfig(changeBetachar, [ SysLoc.AUSTRIA ]);

  GEOHandler.addAddrFunction(changeAbbrevNmLocn, GEOHandler.CEMEA);
  GEOHandler.addAfterConfig(validateAbbrevNmLocn, GEOHandler.CEMEA);
  GEOHandler.addAddrFunction(addLatinCharValidator, GEOHandler.CEMEA);

  GEOHandler.addAfterTemplateLoad(setPreferredLang, GEOHandler.CEMEA);

  GEOHandler.registerValidator(orderBlockValidation, [ SysLoc.AUSTRIA ], null, true);

  GEOHandler.registerValidator(addAddressTypeValidator, GEOHandler.CEMEA, null, true);
  GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.CEMEA, null, true);
  GEOHandler.registerValidator(addCrossBorderValidatorForCEMEA, [ '707', '762', '808', '620', '767', '805', '823', '677', '680', '832' ], null, true);
  // GEOHandler.registerValidator(postCdLenChecks, GEOHandler.CEMEA, null,
  // true);
  GEOHandler.registerValidator(requireVATForCrossBorderAT, [ SysLoc.AUSTRIA ], null, true);
  GEOHandler.registerValidator(addCmrNoValidator, GEOHandler.CEMEA, null, true);
  GEOHandler.registerValidator(cemeaCustomVATValidator('', 'MAIN_CUST_TAB', 'frmCMR', 'ZP01'), GEOHandler.CEMEA, null, true);
  // GEOHandler.registerValidator(customCrossPostCdValidator, GEOHandler.CEMEA,
  // null, true);

  GEOHandler.addAddrFunction(displayIceForMA, [ SysLoc.MOROCCO ]);
  GEOHandler.registerValidator(addIceFormatValidationMorocco, [ SysLoc.MOROCCO ], null, true);
  GEOHandler.registerValidator(addIceBillingValidator, [ SysLoc.MOROCCO ], null, true);

  GEOHandler.registerValidator(validateAbbrevNmForCIS, [ SysLoc.RUSSIA ], null, true);

  // GEOHandler.registerValidator(addPoBoxValidator, [ SysLoc.AUSTRIA], null,
  // true);

  GEOHandler.registerValidator(addStreetAndPoBoxFormValidator, [ SysLoc.AUSTRIA ], null, true);
  GEOHandler.registerValidator(restrictDuplicateAddrAT, [ SysLoc.AUSTRIA ]);

  // Checklist
  GEOHandler.addAfterConfig(setChecklistStatus, GEOHandler.CEMEA_CHECKLIST);
  GEOHandler.registerValidator(addCEMEAChecklistValidator, GEOHandler.CEMEA_CHECKLIST);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.CEMEA, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(hideEngineeringBOForReq, GEOHandler.CEMEA);
  // CMR-1912 Vat should be required for AT local-BP and Commercial
  GEOHandler.addAfterConfig(customVATMandatoryForAT, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterTemplateLoad(customVATMandatoryForAT, [ SysLoc.AUSTRIA ]);
  /*
   * GEOHandler.addAfterConfig(cemeaCustomVATMandatory, GEOHandler.CEMEA);
   * GEOHandler.addAfterTemplateLoad(cemeaCustomVATMandatory, GEOHandler.CEMEA);
   */
  // GEOHandler.registerValidator(cemeaCustomVATMandatory('', 'MAIN_CUST_TAB',
  // 'frmCMR', 'ZP01'), GEOHandler.CEMEA, null, true);
  /*
   * 1496135: Importing G address from SOF for Update Requests jz: add local
   * country name text box
   */
  // GEOHandler.registerValidator(similarAddrCheckValidator, [ SysLoc.AUSTRIA ],
  // null, true);
  GEOHandler.addToggleAddrTypeFunction(toggleLocalCountryName, GEOHandler.CEMEA);
  GEOHandler.addAddrFunction(toggleLocalCountryNameOnOpen, GEOHandler.CEMEA);
  // CMR-2101 SBO is required for processor
  GEOHandler.registerValidator(validateSBO, [ SysLoc.AUSTRIA ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(validateSBO, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterTemplateLoad(validateSBO, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterTemplateLoad(setISUCTCOnIMSChange, [ SysLoc.AUSTRIA ]);
  GEOHandler.addAfterConfig(setISUCTCOnIMSChange, [ SysLoc.AUSTRIA ]);
});
