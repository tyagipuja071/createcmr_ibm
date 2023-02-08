/* Register EMEA Javascripts */
var _customerTypeHandler = null;
var _vatExemptHandler = null;
var _isuCdHandler = null;
var _isicCdHandler = null;
var _requestingLOBHandler = null;
var _economicCdHandler = null;
var _custSubTypeHandler = null;
var _custSubTypeHandlerGr = null;
var _custSalesRepHandlerGr = null;
var _landCntryHandler = null;
var _stateProvITHandler = null;
var _internalDeptHandler = null;
var addrTypeHandler = [];
var _hwMstrInstallFlagHandler = null;
var _scenarioSubTypeHandler = null;
var _addrTypeOnChangeHandler = [];
var _addrTypesForOnChange = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01' ];

var SCOTLAND_POST_CD = [ 'AB', 'KA', 'DD', 'KW', 'DG', 'KY', 'EH', 'ML', 'FK', 'PA', 'G1', 'G2', 'G3', 'G4', 'G5', 'G6', 'G7', 'G8', 'G9', 'PH', 'TD', 'IV' ];
var NORTHERN_IRELAND_POST_CD = [ 'BT' ];
var UK_LANDED_CNTRY = [ 'AI', 'BM', 'IO', 'VG', 'KY', 'FK', 'GI', 'MS', 'PN', 'SH', 'TC', 'GS', 'GG', 'JE', 'IM' ];
var _addrTypeITHandler = [];
var _streetITHandler = null;
var _custNameITHandler = null;
var _cityITHandler = null;
var _dropDownCityHandler = null;
var _addrTypesForIT = [ 'ZS01', 'ZP01', 'ZI01', 'ZS02' ];
var _identClientHandlerIT = null;
var _salesRepHandlerIT = null;
var _oldSalesRepIT = null;
var _oldSBOIT = null;
var _oldCollectionCdIT = null;
var _vatUpdateHandlerIT = null;
var _fiscalCodeUpdateHandlerIT = null;
var _CTCHandlerIT = null;
var _ISUHandlerIT = null;
var _oldISUIT = "";
var _oldCTCIT = "";
var _oldINACIT = "";
var _oldSpecialTaxCdIT = "";
var _oldAffiliateIT = "";
// DTN: Defect 1858294 : UKI: Internal FSL sub-scenario rules for abbreviated
// name
var _lobHandler = null;
var _landCntryHandlerUK = null;
var _postalCdUKHandler = null;
var _cityUKHandler = null;
var _custGrpIT = null;

var _importedIndc = null;

function getImportedIndcForItaly() {
  if (_importedIndc) {
    console.log('Returning imported indc = ' + _importedIndc);
    return _importedIndc;
  }
  var results = cmr.query('VALIDATOR.IMPORTED_IT', {
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

function addISUHandler() {
  var _CTCHandler = null;
  _isuCdHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    setClientTierValuesTR(value);
    setValuesWRTIsuCtc();
  });
  _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
    setClientTierValuesTR();
    setValuesWRTIsuCtc(value);
  });
}

// CREATCMR-2657 for TURKEY
function setSORTL() {
  if (FormManager.getActualValue('cmrIssuingCntry') != '862') {
    return;
  }
  _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
    var clientTier = FormManager.getActualValue('clientTier');
    var reqType = FormManager.getActualValue('reqType');
    var isu = FormManager.getActualValue('isuCd');
    if (isu == '34' && clientTier == 'Y' && reqType == 'C') {
      FormManager.setValue('salesBusOffCd', 'A20');
      FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), [ '', 'A20' ]);
    }
    lockSORTL();
  });
}

// CREATCMR-2657 lock SORTL field for turkey
function lockSORTL() {
  if (FormManager.getActualValue('cmrIssuingCntry') != '862') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var clientTier = FormManager.getActualValue('clientTier');
  if (reqType == 'U' || reqType == 'C') {
    if (role == 'REQUESTER') {
    } else if (role == 'PROCESSOR') {
    }
  }
}

function addEMEALandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }

  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry == 'GR') {
    var scenario = FormManager.getActualValue('custGrp');
    if ((scenario == 'LOCAL' || FormManager.getActualValue('reqType') == 'U') && FormManager.getActualValue('addrType') == 'ZP01') {
      GEOHandler.disableCopyAddress();
    }
  }
}

/**
 * Type Of Customer field
 */
function typeOfCustomer() {
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType != 'C') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (reqType == 'C') {
    if (FormManager.getActualValue('crosSubTyp') == ''
        && (custSubGrp == 'INTER' || custSubGrp == 'INTSM' || custSubGrp == 'INTVA' || custSubGrp == 'BUSPR' || custSubGrp == 'BUSSM' || custSubGrp == 'BUSVA' || custSubGrp == 'CROIN' || custSubGrp == 'CROBP')) {
      FormManager.enable('crosSubTyp');
      FormManager.addValidator('crosSubTyp', Validators.REQUIRED, [ 'Type Of Customer' ], 'MAIN_CUST_TAB');
    } else {
      FormManager.resetValidations('crosSubTyp');
    }
  }

  if ((custSubGrp == 'INTER') || (custSubGrp == 'INTSM') || (custSubGrp == 'INTVA') || (custSubGrp == 'CROIN')) {
    FormManager.enable('crosSubTyp');
    FormManager.limitDropdownValues(FormManager.getField('crosSubTyp'), [ '91', '92' ]);
  } else if (custSubGrp == 'LOCEN' || custSubGrp == 'LOCSM' || custSubGrp == 'LOCVA' || custSubGrp == 'CROLC') {
    FormManager.setValue('crosSubTyp', 'E');
    FormManager.readOnly('crosSubTyp');
  } else if (custSubGrp == 'GOVST' || custSubGrp == 'GOVSM' || custSubGrp == 'GOVVA' || custSubGrp == 'CROGO') {
    FormManager.setValue('crosSubTyp', 'G');
    FormManager.readOnly('crosSubTyp');
  } else if (custSubGrp == 'UNIVE' || custSubGrp == 'UNISM' || custSubGrp == 'UNIVA' || custSubGrp == 'CROUN') {
    FormManager.setValue('crosSubTyp', 'U');
    FormManager.readOnly('crosSubTyp');
  } else if (custSubGrp == '3PAIT' || custSubGrp == '3PASM' || custSubGrp == '3PAVA' || custSubGrp == 'CRO3P') {
    FormManager.setValue('crosSubTyp', 'IL');
    FormManager.readOnly('crosSubTyp');
  } else if (custSubGrp == 'CROPR' || custSubGrp == 'PRISM' || custSubGrp == 'PRIVA' || custSubGrp == 'PRICU' || custSubGrp == 'COMME' || custSubGrp == 'CROCM' || custSubGrp == 'COMSM'
      || custSubGrp == 'COMVA' || custSubGrp == 'NGOIT' || custSubGrp == 'NGOSM' || custSubGrp == 'NGOVA') {
    FormManager.readOnly('crosSubTyp');
    FormManager.setValue('crosSubTyp', '');
  }

  if (((custSubGrp == 'BUSPR') || (custSubGrp == 'BUSSM') || (custSubGrp == 'BUSVA') || (custSubGrp == 'CROBP')) && role != 'REQUESTER') {
    FormManager.enable('crosSubTyp');
    FormManager.limitDropdownValues(FormManager.getField('crosSubTyp'), [ '51', '52', '53' ]);
  } else if (((custSubGrp == 'BUSPR') || (custSubGrp == 'BUSSM') || (custSubGrp == 'BUSVA') || (custSubGrp == 'CROBP')) && role == 'REQUESTER') {
    FormManager.readOnly('crosSubTyp');
    FormManager.setValue('crosSubTyp', '52');
  }
}

/*
 * EmbargoCode field locked for REQUESTER
 */
function lockEmbargo() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (issu_cntry == '862') {
    lockSORTL();
  }
  if (role == 'REQUESTER') {
    if ((issu_cntry == '866' || issu_cntry == '754' || issu_cntry == '758') && (FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X')) {
      FormManager.enable('embargoCd');
    } else {
      FormManager.readOnly('embargoCd');
    }
  } else {
    FormManager.enable('embargoCd');
  }
}

function addUKAddressTypeValidator() {
  console.log("addUKAddressTypeValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '866') {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Mailing, Billing and Installing addresses are mandatory. Only multiple Shipping & Installing addresses are allowed.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var billingToCnt = 0;
          var installAtCnt = 0;
          var mailingCnt = 0;
          var softwafeCnt = 0;
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
            } else if (type == 'ZS01') {
              billingToCnt++;
            } else if (type == 'ZP01') {
              mailingCnt++;
            } else if (type == 'ZS02') {
              softwafeCnt++;
            }
          }
          if (billingToCnt == 0 || installAtCnt == 0 || mailingCnt == 0) {
            return new ValidationResult(null, false,
                'Mailing, Billing and Installing addresses are mandatory. Other addresses are optional. Only multiple Shipping & Installing addresses are allowed.');
          } else if (billingToCnt > 1) {
            return new ValidationResult(null, false, 'What we need is to have only One Billing address. Please remove the additional Billing address.');
          } else if (mailingCnt > 1) {
            return new ValidationResult(null, false, 'What we need is to have only One Mailing address. Please remove the additional Mailing address.');
          } else if (softwafeCnt > 1) {
            return new ValidationResult(null, false, 'What we need is to have only One Software Upgrade address. Please remove the additional Software Upgrade address.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

// CMR-2688 Turkey - Default Preferred Language to T for all Create Scenarios
function setDefaultValueForPreferredLanguage() {
  console.log("setDefaultValueForPreferredLanguage..............");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    var reqType = FormManager.getActualValue('reqType');
    console.log("reqType value is :" + reqType);
    if (reqType == 'C') {
      FormManager.setValue('custPrefLang', 'T');
    }
  }
}

// CMR-1804-Turkey Business Partner ISU code 8B, Commercial ISU code 32
function setDefaultValueForISU() {
  console.log("setDefaultValueForISU..............");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    var subType = FormManager.getActualValue('custSubGrp');
    console.log("sub-type scenario value is :" + subType);
    // Business Partner
    if (subType == 'BUSPR' || subType == 'XBP') {
      FormManager.setValue('isuCd', '8B');
      FormManager.enable('isuCd');
    }
    // Commercial
    /*
     * else if (subType == 'COMME' || subType == 'XINTS') {
     * FormManager.setValue('isuCd', '32'); FormManager.enable('isuCd'); }
     */
  }
}

function setISUDefaultValueOnSubTypeChange() {
  if (_scenarioSubTypeHandler == null && FormManager.getField('custSubGrp')) {
    _scenarioSubTypeHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      setDefaultValueForISU();
      controlFieldsBySubScenarioTR(value);
    });
  }
  // if (_scenarioSubTypeHandler && _scenarioSubTypeHandler[0]) {
  // _scenarioSubTypeHandler[0].onChange();
  // }
}

function disableTaxOfficeTR() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    console.log("disableTaxOfficeTR..............");
    var addressTypeValue = FormManager.getActualValue('addrType');
    if (addressTypeValue == 'ZP01') {
      FormManager.show('TaxOffice', 'taxOffice');
      var custType = FormManager.getActualValue('custGrp');
      if (custType == 'CROSS' || cmr.currentRequestType == 'U') {
        FormManager.removeValidator('taxOffice', Validators.REQUIRED);
      } else {
        checkAndAddValidator('taxOffice', Validators.REQUIRED, [ 'Tax Office' ]);
      }
    } else {
      FormManager.hide('TaxOffice', 'taxOffice');
      FormManager.removeValidator('taxOffice', Validators.REQUIRED);
    }
  }
}

function autoSetAbbrevNameUKIInterFSL(custType) {
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType != 'C') {
    return;
  }
  var lob = FormManager.getActualValue('requestingLob');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var abbrevNm = '';
  var tmInstallName1 = '';

  var qParams = {
    REQ_ID : FormManager.getActualValue('reqId'),
  };

  var result = cmr.query('UKI.GET_TOP_INSTALL_1', qParams);
  tmInstallName1 = result.ret4;

  var resultName = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', qParams);
  var abbrevNmDBVal = resultName.ret1;

  if (('INFSL' == custType || 'XINFS' == custType) && (SysLoc.IRELAND == cntry || SysLoc.UK == cntry)) {
    if (SysLoc.UK == cntry) {
      if ('IGF' == lob) {

        if (tmInstallName1.length > 10) {
          tmInstallName1 = tmInstallName1.substring(0, 10);
        }

        abbrevNm = 'IBM UK' + '/' + tmInstallName1 + ' ZG33';
      } else {

        if (tmInstallName1.length > 13) {
          tmInstallName1 = tmInstallName1.substring(0, 13);
        }

        abbrevNm = 'FSL' + '/' + tmInstallName1 + ' ZG33';
      }
    }
    if (SysLoc.IRELAND == cntry) {
      if ('IGF' == lob) {

        if (tmInstallName1.length > 10) {
          tmInstallName1 = tmInstallName1.substring(0, 10);
        }

        abbrevNm = 'IBM UK' + '/' + tmInstallName1 + ' ZG35';
      } else {

        if (tmInstallName1.length > 13) {
          tmInstallName1 = tmInstallName1.substring(0, 13);
        }

        abbrevNm = 'FSL' + '/' + tmInstallName1 + ' ZG35';
      }
    }

    FormManager.setValue('abbrevNm', abbrevNm);
    FormManager.readOnly('abbrevNm');
  } else if (('INTER' == custType || 'XINTR' == custType) && (SysLoc.IRELAND == cntry || SysLoc.UK == cntry)) {
    var dept = FormManager.getActualValue('ibmDeptCostCenter');

    if (tmInstallName1.length > 10) {
      tmInstallName1 = tmInstallName1.substring(0, 10);
    }

    abbrevNm = 'IBM/' + dept + '/' + tmInstallName1;
    FormManager.setValue('abbrevNm', abbrevNm);
    FormManager.readOnly('abbrevNm');

  } else {
    if (abbrevNmDBVal == '') {
      FormManager.setValue('abbrevNm', '');
    } else {
      FormManager.setValue('abbrevNm', abbrevNmDBVal);
    }
    // FormManager.setValue('abbrevNm', '');
    FormManager.enable('abbrevNm');
  }
}

function autoSetAbbrevNmFrmDept() {
  console.log('>>> Running autoSetAbbrevNmFrmDept');
  var reqType;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType != 'C') {
    return;
  }
  var custType = FormManager.getActualValue('custSubGrp');
  var lob = FormManager.getActualValue('requestingLob');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var abbrevNm = '';
  var tmInstallName1 = '';

  var qParams = {
    REQ_ID : FormManager.getActualValue('reqId'),
  };

  var result = cmr.query('UKI.GET_TOP_INSTALL_1', qParams);
  tmInstallName1 = result.ret4;

  if (('INTER' == custType || 'XINTR' == custType) && (SysLoc.IRELAND == cntry || SysLoc.UK == cntry)) {
    var dept = FormManager.getActualValue('ibmDeptCostCenter');

    if (tmInstallName1.length > 10) {
      tmInstallName1 = tmInstallName1.substring(0, 10);
    }
    abbrevNm = 'IBM/' + dept + '/' + tmInstallName1;
    FormManager.setValue('abbrevNm', abbrevNm);
    FormManager.readOnly('abbrevNm');
  }
}

function afterConfigForUKI() {
  console.log(" --->>> Process afterConfigForUKI. <<<--- ");
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  // autoSetAbbrevLocnHandler();
  if (issuingCntry == SysLoc.UK || issuingCntry == SysLoc.IRELAND) {
    if (FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X') {
      FormManager.resetValidations('collectionCd');
    }
  }
  optionalRuleForVatUK();
  if (_internalDeptHandler == null) {
    _internalDeptHandler = dojo.connect(FormManager.getField('ibmDeptCostCenter'), 'onChange', function(value) {
      autoSetAbbrevNmFrmDept();
    });
  }
  if (_customerTypeHandler == null) {
    var _custType = null;
    _customerTypeHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      _custType = FormManager.getActualValue('custSubGrp');
      if (_custType != '') {

        var qParams = {
          REQ_ID : FormManager.getActualValue('reqId'),
        };
        var result = cmr.query('DATA.GET.CUSTSUBGRP.BY_REQID', qParams);
        var custTypeinDB = result.ret1;
        autoSetVAT(_custType, custTypeinDB);
        autoSetSpecialTaxCdByScenario(_custType, custTypeinDB);
        autoSetCollectionCdByScenario(_custType);
        autoSetSBO(_custType, custTypeinDB);
        // autoSetAbbrevNmOnChanageUKI();
        autoSetAbbrevLocnOnChangeUKI();
        // DTN: Defect 1858294 : UKI: Internal FSL sub-scenario rules for
        // abbreviated name
        autoSetAbbrevNameUKIInterFSL(_custType);
        unlockINACForINTERUKI();
        autoSetISUClientTierUK();
        optionalRuleForVatUK();
      }
      ;
    });
  }
  if (_customerTypeHandler && _customerTypeHandler[0]) {
    _customerTypeHandler[0].onChange();
  }
  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      if (dijit.byId('vatExempt').get('checked')) {
        console.log(">>> Process vatExempt remove * >> ");
        FormManager.resetValidations('vat');
      } else {
        console.log(">>> Process vatExempt add * >> ");
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      }
    });
  }
  if (_lobHandler == null) {
    var _custType = FormManager.getActualValue('custSubGrp');
    ;
    _lobHandler = dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
      var lob = FormManager.getActualValue('requestingLob');
      if (lob != '') {
        autoSetAbbrevNameUKIInterFSL(_custType);
      }
    });
  }

  if (_landCntryHandlerUK == null && issuingCntry == SysLoc.UK) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      optionalRulePostalCodeUK();
    });
  }
  if (_landCntryHandlerUK && _landCntryHandlerUK[0]) {
    _landCntryHandlerUK[0].onChange();
  }
}

/*
 * function autoSetSpecialTaxCd(_custType, custTypeinDB) { console.log(">>>
 * Process autoSetSpecialTaxCd >> "); if (custTypeinDB != null && custTypeinDB ==
 * _custType) { return } if (FormManager.getActualValue('cmrIssuingCntry') ==
 * SysLoc.UK || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
 * if (_custType == 'INTER' || _custType == 'XINTR') {
 * FormManager.setValue('specialTaxCd', 'XX');
 * FormManager.readOnly('specialTaxCd'); } else if
 * (FormManager.getActualValue('specialTaxCd') == '' ||
 * FormManager.getActualValue('specialTaxCd') == 'XX' ||
 * FormManager.getActualValue('specialTaxCd') == '32' ||
 * FormManager.getActualValue('specialTaxCd') == 'Bl') { if (_custType ==
 * 'CROSS' && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
 * FormManager.setValue('specialTaxCd', '32');
 * FormManager.enable('specialTaxCd'); } else { if (_custType == 'INFSL') {
 * FormManager.setValue('specialTaxCd', 'XX');
 * FormManager.enable('specialTaxCd'); } else {
 * FormManager.setValue('specialTaxCd', 'Bl');
 * FormManager.enable('specialTaxCd'); } } } } }
 */
function autoSetSpecialTaxCdByScenario(_custType, custTypeinDB) {
  var reqType = FormManager.getActualValue('reqType');
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (custTypeinDB != null && custTypeinDB == _custType) {
    return;
  }
  if (reqType != 'C') {
    return;
  }
  if (issuingCntry == SysLoc.UK || issuingCntry == SysLoc.IRELAND) {
    if (_custType == 'INTER' || _custType == 'XINTR') {
      FormManager.setValue('specialTaxCd', 'XX');
      FormManager.readOnly('specialTaxCd');
    } else if (issuingCntry == SysLoc.UK && (_custType == 'CROSS' || _custType == 'XBSPR' || _custType == 'XPRIC' || _custType == 'XGOVR')) {
      FormManager.setValue('specialTaxCd', '32');
      FormManager.enable('specialTaxCd');
    } else {
      if (_custType == 'INFSL') {
        FormManager.setValue('specialTaxCd', 'XX');
        FormManager.enable('specialTaxCd');
      } else {
        FormManager.setValue('specialTaxCd', 'Bl');
        FormManager.enable('specialTaxCd');
      }
    }
    var custGrp = FormManager.getActualValue('custGrp');
    if (issuingCntry == SysLoc.UK && (custGrp != undefined && custGrp != '') && custGrp == 'CROSS') {
      var _reqId = FormManager.getActualValue('reqId');
      var params = {
        REQ_ID : _reqId,
        ADDR_TYPE : "ZS01"
      };

      var landCntryResult = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', params);
      var landCntry = landCntryResult.ret1;

      var custSubGrp = FormManager.getActualValue('custSubGrp');
      if (landCntry != 'undefined' && landCntry == 'IM') {
        FormManager.setValue('specialTaxCd', 'Bl');
        FormManager.enable('specialTaxCd');
      } else {
        if (custSubGrp != null && custSubGrp == 'XINTR') {
          FormManager.setValue('specialTaxCd', 'XX');
          FormManager.readOnly('specialTaxCd');
        } else {
          FormManager.setValue('specialTaxCd', '32');
          FormManager.enable('specialTaxCd');
        }
      }
    }
  }
}

function autoSetCollectionCdByScenario(_custType) {
  console.log(">>> Process autoSetCollectionCdByScenario custSubGrp >> " + _custType);
  autoSetCollectionCd();
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
    if (_custType == 'INTER' || _custType == 'XINTR')
      FormManager.setValue('collectionCd', '88');
    if (_custType == 'INFSL')
      FormManager.setValue('collectionCd', '69');
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
    if (_custType == 'INTER' || _custType == 'XINTR')
      FormManager.setValue('collectionCd', '88');
  }
}

function autoSetCollectionCd() {
  console.log(">>> Process autoSetCollectionCd .... >> ");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
    FormManager.enable('collectionCd');
    if (FormManager.getActualValue('reqType') == 'C') {
      FormManager.readOnly('collectionCd');
      if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK)
        FormManager.setValue('collectionCd', '17');
      if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND)
        FormManager.setValue('collectionCd', '45');
    }
  }
}

function autoSetAbbrevLocnHandler() {
  console.log("---->>>> autoSetAbbrevLocnHandler <<<<----");

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL) {
    if (FormManager.getActualValue('abbrevLocn') == '') {
      qParams = {
        REQ_ID : FormManager.getActualValue('reqId'),
      };
      var result = cmr.query('ADDR.GET.ZS01_CITY1', qParams);
      var address = result.ret1;
      if (address != null && address.length > 12) {
        address = address.substr(0, 12);
      }
      FormManager.setValue('abbrevLocn', address);
    }
  }
}

function setUKIAbbrevNmLocnOnAddressSave(cntry, addressMode, saving, finalSave, force) {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType != 'C') {
    return;
  }
  if (role != 'Requester') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId
  };
  var result = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', qParams);
  var abbrevNmDBValue = result.ret1;

  if (abbrevNmDBValue == '') {

    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (custSubGrp != undefined && (custSubGrp == 'IBMSN' || custSubGrp == 'IBMSC')) {
      /*
       * var qParams = { REQ_ID : reqId, ADDR_TYPE : "ZI01", }; var result =
       * cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams); result.ret1;
       */
      if (FormManager.getActualValue('addrType') == 'ZI01') {
        var _abbrevNmValue = FormManager.getActualValue('custNm1');

        if (_abbrevNmValue != null && custSubGrp == 'IBMSN') {
          var instIBSN = "IBM SUB ONLY " + _abbrevNmValue;
          if (instIBSN.length > 22) {
            instIBSN = instIBSN.substring(0, 22);
          }
          FormManager.setValue('abbrevNm', instIBSN);
        }
        if (_abbrevNmValue != null && custSubGrp == 'IBMSC') {
          var instIBMSC = "IBM SUB CH ONLY " + _abbrevNmValue;
          if (instIBMSC.length > 22) {
            instIBMSC = instIBMSC.substring(0, 22);
          }
          FormManager.setValue('abbrevNm', instIBMSC);
        }
      }
    } else if (custSubGrp != undefined && (custSubGrp == 'INTER' || custSubGrp == 'INFSL')) {
      console.log(">>>> Do nothing when Internal & Internal - FSL scenario...");
      /*
       * console.log(">>>> Perform INTERNAL scenario for ABBREV NAME"); if
       * (FormManager.getActualValue('addrType') == 'ZI01') { var _abbrevNmValue =
       * FormManager.getActualValue('custNm1'); if (_abbrevNmValue.length > 22) {
       * _abbrevNmValue = _abbrevNmValue.substring(0, 22); }
       * FormManager.setValue('abbrevNm', _abbrevNmValue); }
       */
    } else {
      /*
       * var qParams = { REQ_ID : reqId, ADDR_TYPE : "ZS01", }; var result =
       * cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams); result.ret1;
       */
      if (FormManager.getActualValue('addrType') == 'ZS01') {

        var _abbrevNmValue = FormManager.getActualValue('custNm1');
        if (_abbrevNmValue != null && _abbrevNmValue.length > 22) {
          _abbrevNmValue = _abbrevNmValue.substr(0, 22);
        }
        FormManager.setValue('abbrevNm', _abbrevNmValue);
      }
    }

  }

  if (finalSave || force || addressMode == 'ZI01') {
    var copyTypes = document.getElementsByName('copyTypes');
    var copyingToA = false;
    if (copyTypes != null && copyTypes.length > 0) {
      copyTypes.forEach(function(input, i) {
        if (input.value == 'ZI01' && input.checked) {
          copyingToA = true;
        }
      });
    }
    var addrType = FormManager.getActualValue('addrType');
    if (addrType == 'ZI01' || copyingToA) {/*
                                             * // auto set UKI Abbrev Name on
                                             * address save var custNm1 =
                                             * FormManager.getActualValue('custNm1');
                                             * var abbNm = custNm1; if (abbNm !=
                                             * null && abbNm.length > 22) {
                                             * abbNm = abbNm.substr(0, 22); }
                                             * 
                                             * if
                                             * (FormManager.getActualValue('cmrIssuingCntry') ==
                                             * SysLoc.IRELAND) { var custSubGrp =
                                             * FormManager.getActualValue('custSubGrp');
                                             * if (custSubGrp == "COOEM") { if
                                             * (abbNm != null && abbNm.length >
                                             * 18) { abbNm = abbNm.substr(0, 18) + "
                                             * OEM"; } else { abbNm = abbNm + "
                                             * OEM"; }
                                             * FormManager.setValue('abbrevNm',
                                             * abbNm); } else {
                                             * FormManager.setValue('abbrevNm',
                                             * abbNm); } } else if
                                             * (FormManager.getActualValue('cmrIssuingCntry') ==
                                             * SysLoc.UK) {
                                             * FormManager.setValue('abbrevNm',
                                             * abbNm); }
                                             */

      // auto set UKI Abbrev Location on address save
      // autoSetAbbrevLocnOnAddSaveUKI();

      // 1482148 - add Scotland and Northern Ireland Logic
      autoSetSboSrOnAddrSaveUK();
    }

    if (_cityUKHandler == null) {
      _cityUKHandler = dojo.connect(FormManager.getField('city1'), 'onChange', function(value) {
        autoPopulateABLocnUK();
      });
    }

    if (_cityUKHandler && _cityUKHandler[0]) {
      _cityUKHandler[0].onChange();
    }

    if (_postalCdUKHandler == null) {
      _postalCdUKHandler = dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
        autoPopulateABLocnUK();
      });
    }

    if (_postalCdUKHandler && _postalCdUKHandler[0]) {
      _postalCdUKHandler[0].onChange();
    }
  }
}

function autoSetAbbrevLocnOnAddSaveUKI(cntry, addressMode, saving, finalSave, force) {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType != 'C') {
    return;
  }
  if (role != 'Requester') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var _custType = FormManager.getActualValue('custSubGrp');
  var addressTyp = FormManager.getActualValue('addrType');
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var abbrevLocnDB = null;
  var qParams = {
    REQ_ID : _zs01ReqId,
  };
  var res = cmr.query('GET_ABBREV_LOCN_DB', qParams);
  abbrevLocnDB = res.ret1;

  if (abbrevLocnDB == undefined || abbrevLocnDB == '') {
    var copyTypes = document.getElementsByName('copyTypes');
    var copyingToA = false;
    if (copyTypes != null && copyTypes.length > 0) {
      copyTypes.forEach(function(input, i) {
        if ((input.value == 'ZI01' || input.value == 'ZS01') && input.checked) {
          copyingToA = true;
        }
      });
    }

    if (finalSave || force || copyingToA) {

      if (addressTyp == 'ZS01') {
        autoSetAbbrevLocUKI();
      } else if (addressTyp == 'ZI01') {
        var addressSeq = FormManager.getActualValue('addrSeq');
        var qParams = {
          REQ_ID : _zs01ReqId,
          ADDR_SEQ : addressSeq,
        };
        var _result = cmr.query('CHECK_IF_MAIN_ZI01', qParams);
        if (_result != 'undefined' && _result != '') {
          autoSetAbbrevLocUKI();
        }

      }
    }
  }
  /*
   * if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) { var
   * _abbrevLocn = null; if (_custType == 'SOFTL') { _abbrevLocn = "SOFTLAYER"; }
   * else if (_custType == 'CROSS') { var landCnty =
   * FormManager.getActualValue('landCntry'); var qParams = { COUNTRY_CD :
   * landCnty, }; var _result =
   * cmr.query('ADDR.GET.LANDCNTRYDESC.BY_COUNTRY_CD', qParams); _abbrevLocn =
   * _result.ret1; } else if (_custType== 'INTER' || _custType== 'INFSL' ){ var
   * city1 = FormManager.getActualValue('city1'); _abbrevLocn = city1; }
   * 
   * if (_abbrevLocn != null && _abbrevLocn.length > 12) { _abbrevLocn =
   * _abbrevLocn.substr(0, 12); }
   * 
   * FormManager.setValue('abbrevLocn', _abbrevLocn); } if
   * (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK && _custType==
   * 'INTER' || _custType== 'INFSL' ) { var postCd =
   * FormManager.getActualValue('postCd'); var _abbrevLocn = postCd;
   * 
   * if (_abbrevLocn != null && _abbrevLocn.length > 12) { _abbrevLocn =
   * _abbrevLocn.substr(0, 12); }
   * 
   * FormManager.setValue('abbrevLocn', _abbrevLocn); }
   */
}

function autoSetVAT(_custType, custTypeinDB) {
  console.log(">>> Process autoSetVAT ...>> " + _custType);
  console.log(">>> Process autoSetVAT 2...>> " + custTypeinDB);

  if (PageManager.isReadOnly()) {
    return;
  }

  // FormManager.enable('vat'); FormManager.enable('vatExempt');
  if (dijit.byId('vatExempt').get('checked')) {
    FormManager.resetValidations('vat');
  } else {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
  }

  if (custTypeinDB != null && custTypeinDB == _custType) {
    return

  }

  if (_custType == 'SOFTL' || _custType == 'INTER') {
    FormManager.resetValidations('vat');
    FormManager.setValue('vatExempt', true);
    FormManager.enable('vat');
  } else {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    FormManager.setValue('vatExempt', false);
    FormManager.enable('vat');
    FormManager.enable('vatExempt');
  }

  if (_custType == 'PRICU' || _custType == 'IBMEM') {
    FormManager.resetValidations('vat');
    FormManager.readOnly('vat');
    FormManager.setValue('vat', '');
    FormManager.disable('vatExempt');
  }

  if (_custType == 'XPRIC') {
    FormManager.resetValidations('vat');
    FormManager.setValue('vatExempt', true);
    FormManager.enable('vat');
  }
  /*
   * // start: Defect 1326375 Ireland: VAT commercial, business partner and
   * private // customer scenario if
   * (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND &&
   * (_custType == 'COMME' || _custType == 'BUSPR')) {
   * FormManager.disable('vatExempt'); }
   */

  // end
}

function autoSetSBO(value, valueInDB) {
  if (PageManager.isReadOnly()) {
    return;
  }
  if (value == 'undefined' || value == '') {
    return;
  }

  if (valueInDB != null && value == valueInDB && (_pagemodel.salesBusOffCd != undefined || _pagemodel.repTeamMemberNo != undefined)) {
    FormManager.setValue('salesBusOffCd', _pagemodel.salesBusOffCd);
    FormManager.setValue('repTeamMemberNo', _pagemodel.repTeamMemberNo);
    return;
  }

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
    FormManager.readOnly('salesBusOffCd');
    var salesRepValues = [ 'MMIR11' ];
    if (isuCd == '21' && (custSubGrp == 'INTER' || custSubGrp == 'BUSPR' || custSubGrp == 'XBSPR' || custSubGrp == 'XINTR')) {
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesRepValues);
      FormManager.setValue('repTeamMemberNo', 'MMIR11');
    } else {
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
    }
  }

  var sboValues = '';
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
    var _reqId = FormManager.getActualValue('reqId');
    var postCdParams = {
      REQ_ID : _reqId,
      ADDR_TYPE : "ZI01",
    };
    var postCdResult = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', postCdParams);
    var postCd = postCdResult.ret1;
    if (postCd != null && postCd.length > 2) {
      postCd = postCd.substring(0, 2);
    }

    var isuCd = FormManager.getActualValue('isuCd');
    var custSubGrp = FormManager.getActualValue('custSubGrp');

    if (custSubGrp == '') {
      return;
    } else if (custSubGrp == 'COMME' || custSubGrp == 'COMLC' || custSubGrp == 'COOEM' || custSubGrp == 'SOFTL' || custSubGrp == 'THDPT' || custSubGrp == 'CROSS') {
      FormManager.enable('salesBusOffCd');
      FormManager.enable('repTeamMemberNo');
      FormManager.resetDropdownValues(FormManager.getField('salesBusOffCd'));
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
      set32SBOLogicOnISIC();
    } else if (custSubGrp == 'PRICU' || custSubGrp == 'IBMEM' || custSubGrp == 'XPRIC') {
      FormManager.setValue('salesBusOffCd', "128");
      FormManager.setValue('repTeamMemberNo', "SPA128");
      FormManager.readOnly('salesBusOffCd');
      FormManager.readOnly('repTeamMemberNo');
    } else if (custSubGrp == 'INTER' || custSubGrp == 'XINTR') {
      FormManager.setValue('salesBusOffCd', "100");
      FormManager.setValue('repTeamMemberNo', "M01000");
      FormManager.readOnly('salesBusOffCd');
      FormManager.readOnly('repTeamMemberNo');
    } else if (custSubGrp == 'BUSPR' || custSubGrp == 'XBSPR') {
      // Defect 1867590 : UKI - Cross-border - sub-scenario specifics are not
      // working; DTN: Added 100, 104
      sboValues = [ '014', '114', '214', '314', '414', '514', '614', '100', '104' ];
      FormManager.enable('salesBusOffCd');
      FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), sboValues);
    } else if (custSubGrp == 'CROIN') {
      FormManager.enable('salesBusOffCd');
      sboCROIN = [ '1b', '1g', '1e', '11', '12', '13', '14' ];
      FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), sboCROIN);
    } else {
      FormManager.enable('salesBusOffCd');
      FormManager.enable('repTeamMemberNo');
      FormManager.resetDropdownValues(FormManager.getField('salesBusOffCd'));
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
    }

    // 1482148 - add Scotland and Northern Ireland logic
    if (isuCd == '32') {
      if (postCd != '' && isScotlandPostCd(postCd)) {
        console.log(">>>> Scotland Logic");
        // FormManager.setValue('clientTier', "C");
        FormManager.clearValue('salesBusOffCd');
        FormManager.clearValue('repTeamMemberNo');
        FormManager.enable('salesBusOffCd');
        FormManager.enable('repTeamMemberNo');
        FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), [ '116', '758' ]);
        FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ 'SPA116', 'SPA758' ]);
      }

      if (postCd != '' && isNorthernIrelandPostCd(postCd)) {
        // Mukesh: below line commented-it's value is dependent on ZS01 address
        // for UK
        // FormManager.setValue('clientTier', "C");
        set32SBOLogicOnISIC();
      }
    }
  }
}

function set32SBOLogicOnISIC() {
  var arryISICForSBO102 = [ '6511', '6519', '6591', '6592', '6599', '6711', '6712', '6719', '7020', '7705', '7715', '9806', '9819' ];
  var arryISICForSBO103 = [ '2211', '2212', '2213', '2219', '2221', '2222', '2230', '4010', '4020', '4030', '4100', '4888', '6420', '7413', '7430', '7700', '7709', '7717', '9000', '9111', '9112',
      '9120', '9191', '9192', '9199', '9211', '9212', '9213', '9214', '9219', '9220', '9232', '9233', '9241', '9249', '9801', '9811', '9821' ];
  var arryISICForSBO111 = [ '2423', '7511', '7512', '7513', '7514', '7521', '7522', '7523', '7530', '7706', '7707', '7720', '7721', '8511', '8512', '8519', '8532', '9231', '9807', '9808', '9900' ];
  var arryISICForSBO122 = [ '1010', '1020', '1030', '1110', '1120', '1200', '1310', '1320', '1410', '1421', '1422', '1429', '1711', '1712', '1722', '1723', '1729', '2010', '2021', '2022', '2023',
      '2029', '2101', '2102', '2109', '2310', '2320', '2330', '2411', '2412', '2413', '2421', '2422', '2429', '2430', '2511', '2519', '2520', '2610', '2691', '2692', '2693', '2694', '2695', '2696',
      '2699', '2710', '2720', '2731', '2732', '2811', '2812', '2813', '2891', '2892', '2893', '2899', '2911', '2912', '2913', '2914', '2915', '2919', '2921', '2922', '2923', '2924', '2925', '2926',
      '2927', '2929', '2930', '3000', '3110', '3120', '3130', '3140', '3150', '3190', '3210', '3220', '3230', '3311', '3312', '3313', '3320', '3330', '3410', '3420', '3430', '3511', '3512', '3520',
      '3530', '3591', '3592', '3599', '3610', '3691', '3692', '3693', '3694', '3699', '3710', '3720', '4510', '4520', '4530', '4540', '5010', '5040', '5050', '5141', '6030', '7010', '7421', '7708',
      '7710', '7711', '7713', '7718', '9813', '9816', '9817', '9822' ];
  var arryISICForSBO128 = [ '0111', '0112', '0113', '0121', '0122', '0130', '0140', '0150', '0200', '0501', '0502', '1511', '1512', '1513', '1514', '1520', '1531', '1532', '1533', '1541', '1542',
      '1543', '1544', '1549', '1551', '1552', '1553', '1554', '1600', '2424', '4550', '5020', '5110', '5121', '5122', '5131', '5139', '5142', '5143', '5149', '5151', '5152', '5159', '5190', '5260',
      '5510', '6010', '6021', '6022', '6023', '6110', '6120', '6210', '6220', '6301', '6302', '6303', '6304', '6309', '6411', '6412', '7111', '7112', '7113', '7121', '7122', '7123', '7129', '7130',
      '7210', '7221', '7229', '7230', '7240', '7250', '7290', '7310', '7411', '7412', '7414', '7422', '7491', '7492', '7493', '7494', '7495', '7499', '7701', '7702', '7703', '7716', '7719', '8520',
      '8531', '9301', '9302', '9303', '9309', '9500', '9802', '9812', '9820' ];
  var arryISICForSBO173 = [ '6601', '6602', '6603', '6720', '7712', '9814' ];
  var arryISICForSBO259 = [ '7320', '7704', '8010', '8021', '8022', '8030', '8090', '9818' ];
  var arryISICForSBO284 = [ '1721', '1730', '1810', '1820', '1911', '1912', '1920', '5030', '5211', '5219', '5220', '5231', '5232', '5233', '5234', '5239', '5240', '5251', '5252', '5259', '5520',
      '7714', '9804' ];

  var isuCdValue = FormManager.getActualValue('isuCd');
  var isicCdValue = FormManager.getActualValue('isicCd');
  var tierValue = FormManager.getActualValue('clientTier');

  if (isuCdValue == '32' && (tierValue == 'B' || tierValue == 'S' || tierValue == 'T' || tierValue == 'C')) {
    searchISICArryAndSetSBO(arryISICForSBO102, isicCdValue, "102", "SPA102");
    searchISICArryAndSetSBO(arryISICForSBO103, isicCdValue, "103", "SPA103");
    searchISICArryAndSetSBO(arryISICForSBO111, isicCdValue, "111", "SPA111");
    searchISICArryAndSetSBO(arryISICForSBO122, isicCdValue, "122", "SPA122");
    searchISICArryAndSetSBO(arryISICForSBO128, isicCdValue, "128", "SPA128");
    searchISICArryAndSetSBO(arryISICForSBO173, isicCdValue, "173", "SPA173");
    searchISICArryAndSetSBO(arryISICForSBO259, isicCdValue, "259", "SPA259");
    searchISICArryAndSetSBO(arryISICForSBO284, isicCdValue, "284", "SPA284");
  }
}

function isScotlandPostCd(postCd) {
  var isScotlandPostCdInd = false;
  for (var i = 0; i < SCOTLAND_POST_CD.length; i++) {
    if (postCd == SCOTLAND_POST_CD[i]) {
      isScotlandPostCdInd = true;
      break;
    }
  }
  if (isScotlandPostCdInd == true) {
    return true;
  } else {
    return false;
  }
}

function isNorthernIrelandPostCd(postCd) {
  var isNorthernIrelandPostCd = false;
  for (var i = 0; i < NORTHERN_IRELAND_POST_CD.length; i++) {
    if (postCd == NORTHERN_IRELAND_POST_CD[i]) {
      isNorthernIrelandPostCd = true;
      break;
    }
  }
  if (isNorthernIrelandPostCd == true) {
    return true;
  } else {
    return false;
  }
}

function set32SBOLogicOnFieldChange() {
  if (_isuCdHandler == null && FormManager.getField('isuCd')) {
    _isuCdHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      autoSetSBO(value, _pagemodel.isuCd);
    });
  }
  if (_isuCdHandler && _isuCdHandler[0]) {
    _isuCdHandler[0].onChange();
  }
  if (_clientTierHandler == null && FormManager.getField('clientTier')) {
    _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      autoSetSBO(value, _pagemodel.clientTier);
    });
  }
  if (_clientTierHandler && _clientTierHandler[0]) {
    _clientTierHandler[0].onChange();
  }
  if (_isicCdHandler == null && FormManager.getField('isicCd')) {
    _isicCdHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      autoSetSBO(value, _pagemodel.isicCd);
    });
  }
  if (_isicCdHandler && _isicCdHandler[0]) {
    _isicCdHandler[0].onChange();
  }
}

function setClientTierValuesTR(isuCd) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  } else {
    var reqType = FormManager.getActualValue('reqType');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (reqType != 'U') {
      if (custSubGrp == 'XINT' || custSubGrp == 'INTER' || custSubGrp == 'XBP' || custSubGrp == 'BUSPR') {
        FormManager.removeValidator('clientTier', Validators.REQUIRED);
      }
    }
    if (custSubGrp != 'IBMEM') {
    }
  }
}

function autoSetSboSrOnAddrSaveUK() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {

    var postCd = FormManager.getActualValue('postCd');
    if (postCd != null && postCd.length > 2) {
      postCd = postCd.substring(0, 2);
    }
    var isuCd = FormManager.getActualValue('isuCd');
    var custSubGrp = FormManager.getActualValue('custSubGrp');

    if (custSubGrp == 'COMME' || custSubGrp == 'COMLC' || custSubGrp == 'COOEM' || custSubGrp == 'SOFTL' || custSubGrp == 'THDPT') {
      FormManager.enable('salesBusOffCd');
      FormManager.enable('repTeamMemberNo');
      FormManager.resetDropdownValues(FormManager.getField('salesBusOffCd'));
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
      set32SBOLogicOnISIC();
    } else if (custSubGrp == 'PRICU' || custSubGrp == 'IBMEM') {
      FormManager.setValue('salesBusOffCd', "128");
      FormManager.setValue('repTeamMemberNo', "SPA128");
      FormManager.readOnly('salesBusOffCd');
      FormManager.readOnly('repTeamMemberNo');
    } else if (custSubGrp == 'INTER') {
      FormManager.setValue('salesBusOffCd', "100");
      FormManager.setValue('repTeamMemberNo', "M01000");
      FormManager.readOnly('salesBusOffCd');
      FormManager.readOnly('repTeamMemberNo');
    } else {
      FormManager.enable('salesBusOffCd');
      FormManager.enable('repTeamMemberNo');
      FormManager.resetDropdownValues(FormManager.getField('salesBusOffCd'));
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
    }

    // 1482148 - add Scotland and Northern Ireland logic
    if (isuCd == '32') {
      if (postCd != '' && isScotlandPostCd(postCd)) {
        // FormManager.setValue('clientTier', "C");
        FormManager.clearValue('salesBusOffCd');
        FormManager.clearValue('repTeamMemberNo');
        FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), [ '116', '758' ]);
        FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ 'SPA116', 'SPA758' ]);
      }

      if (postCd != '' && isNorthernIrelandPostCd(postCd)) {
        // FormManager.setValue('clientTier', "C");
        set32SBOLogicOnISIC();
      }
    }

  }
}

function setAbbrevNmLocationLockAndMandatoryUKI() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Requester') {
    FormManager.enable('abbrevNm');
    // FormManager.readOnly('abbrevLocn');
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);

  } else if (role == 'Processor') {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  }
}

function validateInternalDeptNumberLength() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = null;
        var scenario = null;
        if (typeof (_pagemodel) != 'undefined') {
          reqType = FormManager.getActualValue('reqType');
          scenario = FormManager.getActualValue('custSubGrp');
        }

        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }

        if (scenario != 'INTER') {
          return new ValidationResult(null, true);
        }

        if (FormManager.getActualValue('viewOnlyPage') == 'true') {
          return new ValidationResult(null, true);
        }

        if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
          var internalDept = FormManager.getActualValue('ibmDeptCostCenter');

          if (internalDept == '') {
            return new ValidationResult(null, true);
          } else {
            if (internalDept.length != 6) {
              return new ValidationResult(null, false, 'Internal Department Number should be 6 characters long.');
            } else {
              return new ValidationResult(null, true);
            }
          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function autoSetAbbrevNmOnChanageUKI() {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType != 'C') {
    return;
  }
  if (role != 'Requester') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {

    // avoid Abbrev Name changing back to their default value
    // after user input a new value and save
    var _custType = FormManager.getActualValue('custSubGrp');
    var custTypeinDB = _pagemodel.custSubGrp;
    if (custTypeinDB != null && _custType == custTypeinDB) {
      console.log(">>>> _custType == custTypeinDB, quit autoSetAbbrevNmOnChanageUKI ");
      FormManager.setValue('abbrevNm', _pagemodel.abbrevNm);
      return;
    }

    var zs01ReqId = FormManager.getActualValue('reqId');
    var qParams = {
      REQ_ID : zs01ReqId,
      ADDR_TYPE : "ZI01",
    };
    var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
    var _abbrevNmValue = result.ret1;

    if (_abbrevNmValue != null && _abbrevNmValue.length > 22) {
      _abbrevNmValue = _abbrevNmValue.substr(0, 22);
    }

    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (custSubGrp == "COOEM") {
      if (_abbrevNmValue != null && _abbrevNmValue.length > 18) {
        _abbrevNmValue = _abbrevNmValue.substr(0, 18) + " OEM";
      } else {
        _abbrevNmValue = _abbrevNmValue + " OEM";
      }
      FormManager.setValue('abbrevNm', _abbrevNmValue);
    } else {
      FormManager.setValue('abbrevNm', _abbrevNmValue);
    }
  } else if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
    var zs01ReqId = FormManager.getActualValue('reqId');
    var qParams = {
      REQ_ID : zs01ReqId,
      ADDR_TYPE : "ZI01",
    };
    var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
    var _abbrevNmValue = result.ret1;

    if (_abbrevNmValue != null && _abbrevNmValue.length > 22) {
      _abbrevNmValue = _abbrevNmValue.substr(0, 22);
    }

    FormManager.setValue('abbrevNm', _abbrevNmValue);
  }
}

function autoSetAbbrevLocnOnChangeUKI() {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType != 'C') {
    return;
  }
  /*
   * if (role != 'Requester') { return; }
   */
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var _custType = FormManager.getActualValue('custSubGrp');
  var custTypeinDB = _pagemodel.custSubGrp;
  // if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {

  // avoid Abbrev Location changing back to their default value
  // after user input a new value and save

  if (custTypeinDB != null && _custType == custTypeinDB) {
    console.log(">>>> _custType == custTypeinDB, quit autoSetAbbrevLocnOnChangeUKI ");
    FormManager.setValue('abbrevLocn', _pagemodel.abbrevLocn);
    return;
  }

  autoSetAbbrevLocUKI();
  /*
   * var _abbrevLocn = null; if (_custType == 'SOFTL') { _abbrevLocn =
   * "SOFTLAYER"; } else if (_custGrp == 'CROSS') { var _addrType = null;
   * if(_custType== 'XINTR' ){ _addrType= 'ZI01'; }else if(_custType == 'XBSPR' ||
   * _custType == 'XGOVR' || _custType == 'XPRIC' || _custType== 'CROSS'){
   * _addrType= 'ZS01'; } var _zs01ReqId = FormManager.getActualValue('reqId');
   * var qParams = { REQ_ID : _zs01ReqId, ADDR_TYPE : _addrType, }; var _result =
   * cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams); _abbrevLocn =
   * _result.ret1; } else { var _zs01ReqId =
   * FormManager.getActualValue('reqId'); var _addrType = null; if(_custType==
   * 'INTER' || _custType== 'INFSL' ){ _addrType= 'ZI01'; }else { _addrType=
   * 'ZS01'; } var qParams = { REQ_ID : _zs01ReqId, ADDR_TYPE : _addrType, };
   * var _result = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', qParams);
   * _abbrevLocn = _result.ret1; }
   * 
   * if (_abbrevLocn != null && _abbrevLocn.length > 12) { _abbrevLocn =
   * _abbrevLocn.substr(0, 12); }
   * 
   * FormManager.setValue('abbrevLocn', _abbrevLocn); if (_custType == 'SOFTL') {
   * FormManager.readOnly('abbrevLocn'); } else {
   * FormManager.enable('abbrevLocn'); } } if
   * (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) { var
   * _abbrevLocn = null; if (_custType == 'SOFTL') { _abbrevLocn = "SOFTLAYER";
   * FormManager.setValue('abbrevLocn', _abbrevLocn);
   * FormManager.readOnly('abbrevLocn'); } else if (_custGrp == 'CROSS') { var
   * _zs01ReqId = FormManager.getActualValue('reqId'); var _addrType = null;
   * if(_custType== 'XINTR' ){ _addrType= 'ZI01'; }else if(_custType == 'XBSPR' ||
   * _custType == 'XGOVR' || _custType == 'XPRIC' || _custType== 'CROSS'){
   * _addrType= 'ZS01'; } var qParams = { REQ_ID : _zs01ReqId, ADDR_TYPE :
   * _addrType, }; var _result =
   * cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams); _abbrevLocn =
   * _result.ret1; } else { var _zs01ReqId =
   * FormManager.getActualValue('reqId'); var _addrType = null; if(_custType==
   * 'INTER' || _custType== 'INFSL' ){ _addrType= 'ZI01'; }else { _addrType=
   * 'ZS01'; } var qParams = { REQ_ID : _zs01ReqId, ADDR_TYPE : _addrType, };
   * var _result = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', qParams);
   * _abbrevLocn = _result.ret1;
   * 
   * if (_abbrevLocn != null && _abbrevLocn.length > 12) { _abbrevLocn =
   * _abbrevLocn.substr(0, 12); }
   * 
   * FormManager.setValue('abbrevLocn', _abbrevLocn);
   * FormManager.enable('abbrevLocn'); } }
   */
}

function showDeptNoForInternalsOnlyUKI() {
  console.log(">>BEGIN showDeptNoForInternalsOnlyUKI >>");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'INTER' || custSubGrp == 'XINTR') {
    console.log(">>custSubGrp >>" + custSubGrp);
    console.log(">>SHOWING ibmDeptCostCenter >>");
    FormManager.show('InternalDept', 'ibmDeptCostCenter');
    FormManager.addValidator('ibmDeptCostCenter', Validators.NUMBER, [ 'Department number' ], 'MAIN_IBM_TAB');
  } else if (custSubGrp != 'INTER' && custSubGrp != 'XINTR') {
    // FormManager.clearValue('ibmDeptCostCenter');
    console.log(">>custSubGrp >>" + custSubGrp);
    console.log(">>HIDING ibmDeptCostCenter >>");
    FormManager.resetValidations('ibmDeptCostCenter');
    FormManager.hide('InternalDept', 'ibmDeptCostCenter');
  }
}

function searchISICArryAndSetSBO(isicArry, isicCdValue, sBOValue, salesRepValue) {
  for (var i = 0; i < isicArry.length; i++) {
    if (isicArry[i] == isicCdValue) {
      FormManager.setValue('salesBusOffCd', sBOValue);
      FormManager.setValue('repTeamMemberNo', salesRepValue);
    }
  }
}

function addIRAddressTypeValidator() {
  console.log("addIRAddressTypeValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '754') {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Mailing, Billing and Installing addresses are mandatory. Only multiple Shipping & Installing addresses are allowed.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var billingToCnt = 0;
          var installAtCnt = 0;
          var mailingCnt = 0;
          var softwareCnt = 0;
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
            } else if (type == 'ZS01') {
              billingToCnt++;
            } else if (type == 'ZP01') {
              mailingCnt++;
            } else if (type == 'ZS02') {
              softwareCnt++;
            }
          }

          if (billingToCnt == 0 || installAtCnt == 0 || mailingCnt == 0) {
            return new ValidationResult(null, false,
                'Mailing, Billing and Installing addresses are mandatory. Other addresses are optional. Only multiple Shipping & Installing addresses are allowed.');
          } else if (billingToCnt > 1) {
            return new ValidationResult(null, false, 'What we need is to have only One Billing address. Please remove the additional Billing address.');
          } else if (mailingCnt > 1) {
            return new ValidationResult(null, false, 'What we need is to have only One Mailing address. Please remove the additional Mailing address.');
          } else if (softwareCnt > 1) {
            return new ValidationResult(null, false, 'What we need is to have only One Software Upgrade address. Please remove the additional Software Upgrade address.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

/**
 * Street or PO Box should be required (Israel)
 */
function addStreetAddressFormValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.ISRAEL) {
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

/**
 * Customer Name Con't or Attention Person should be required (Israel)
 */
function addNameContAttnDeptValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.ISRAEL) {
          return new ValidationResult(null, true);
        }
        if (FormManager.getActualValue('custNm2') != '' && FormManager.getActualValue('dept') != '') {
          return new ValidationResult(null, false, 'Please fill-out either Customer Name Cont or Attention Person only.');
        }

        var val = FormManager.getActualValue('custNm2') != '' ? FormManager.getActualValue('custNm2') : FormManager.getActualValue('dept');
        if (FormManager.getActualValue('custPhone') != '') {
          val += (val != '' ? ', ' : '') + FormManager.getActualValue('custPhone');
        }

        if (val.length > 30) {
          return new ValidationResult(null, false, 'Total computed length of Customer Name Cont or Attention Person with Customer Phone should be less than 30 characters.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

/**
 * Customer Name Con't or Attention Person should be required (Israel)
 */
function addNameContAttnDeptValidatorUKI() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.UK && FormManager.getActualValue('cmrIssuingCntry') != SysLoc.IRELAND) {
          return new ValidationResult(null, true);
        }

        var nm2 = FormManager.getActualValue('custNm2');
        var dept = FormManager.getActualValue('dept');
        var phone = FormManager.getActualValue('custPhone');

        var val = nm2;
        if (dept != '') {
          val += (val.length > 0 ? ', ' : '') + dept;
        }
        if (phone != '') {
          val += (val.length > 0 ? ', ' : '') + phone;
        }
        if (val.length > 30) {
          return new ValidationResult(null, false, 'Total computed length of Customer Name Con\'t, Department/Attn, and Customer Phone should be less than 30 characters.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addDistrictPostCodeCityValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
          return new ValidationResult(null, true);
        }

        var city = FormManager.getActualValue('city1');
        var dept = FormManager.getActualValue('dept');
        var post = FormManager.getActualValue('postCd');

        var val = city;
        if (dept != '') {
          val += (val.length > 0 ? ' ' : '') + dept;
        }
        if (post != '') {
          val += (val.length > 0 ? ' ' : '') + post;
        }
        if (val.length > 30) {
          return new ValidationResult(null, false, 'Total computed length of District, Postal Code, and City should be less than 30 characters.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}
function addOccupationPOBoxValidator() {
  console.log("addOccupationPOBoxValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.GREECE && FormManager.getActualValue('cmrIssuingCntry') != SysLoc.CYPRUS) {
          return new ValidationResult(null, true);
        }
        if (FormManager.getActualValue('addrTxt2') != '' && FormManager.getActualValue('poBox') != '') {
          return new ValidationResult(null, false, 'Please fill-out either PO Box or Occupation only.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addOccupationPOBoxAttnPersonValidatorForGR() {
  console.log("GREECE - addOccupationPOBoxAttnPersonValidatorForGR..............");

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var filledCount = 0;

        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.GREECE) {
          return new ValidationResult(null, true);
        }

        if (FormManager.getActualValue('addrTxt') != '') {
          filledCount++;
        }

        if (FormManager.getActualValue('addrTxt2') != '') {
          filledCount++;
        }

        if (FormManager.getActualValue('poBox') != '') {
          filledCount++;
        }

        if (FormManager.getActualValue('custNm4') != '') {
          filledCount++;
        }

        if (filledCount > 2) {
          return new ValidationResult(null, false, 'Street Address,  Address Con\'t/Occupation, PO BOX, and Att. Person only 2 can be filled at the same time');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addStreetAddressFormValidatorGR() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.GREECE) {
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

function addCrossLandedCntryFormValidatorGR() {
  console.log("addCrossLandedCntryFormValidatorGR..............");

  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var isCrossborder = cmr.oldlandcntry != 'GR' && FormManager.getActualValue('reqType') == 'U';
        if ((FormManager.getActualValue('custGrp') == 'CROSS' || isCrossborder) && (FormManager.getActualValue('addrType') == 'ZP01' || FormManager.getActualValue('addrType') == 'ZS01')
            && FormManager.getActualValue('landCntry') == 'GR') {
          return new ValidationResult(null, false, 'Landed Country value should not be \'Greece - GR\' for Cross-border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function clearPhoneNoFromGrid() {
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    if (_allAddressData != null && _allAddressData[i] != null) {
      if (!(_allAddressData[i].addrType[0] == 'ZS01' || _allAddressData[i].addrType[0] == 'ZD01')) {
        _allAddressData[i].custPhone[0] = '';
      }
    }
  }
}

function clearPOBoxFromGrid() {
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    if (_allAddressData != null && _allAddressData[i] != null) {
      if (!(_allAddressData[i].addrType[0] == 'ZS01' || _allAddressData[i].addrType[0] == 'ZP01')) {
        _allAddressData[i].poBox[0] = '';
      }
    }
  }
}

/*
 * Disable VAT ID when user role is requester and request type is update
 */

function addVATDisabler() {
  var interval = new Object();
  var roleCheck = false;
  var reqCheck = false;

  interval = setInterval(function() {
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var req = FormManager.getActualValue('reqType').toUpperCase();
    var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

    FormManager.enable('vat');

    if (role != null && role.length > 0) {
      roleCheck = true;
    }

    if (req != null && req.length > 0) {
      reqCheck = true;
    }

    if (roleCheck && reqCheck) {
      if (role == 'REQUESTER' && req == 'U') {
        FormManager.readOnly('vat');
      }
      clearInterval(interval);
    }

    if (viewOnlyPage == 'true') {
      FormManager.readOnly('vat');
    }
  }, 1000);

}

/*
 * Update enterprise number by copying the Sales rep & move the leading 0 and
 * add it to the end of the Sales rep number.
 */
function updateEnterpriseNo() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var _ILentConnect = dojo.connect(FormManager.getField('repTeamMemberNo'), 'onChange', function(value) {
    var salesRepArr = Array.from(FormManager.getActualValue('repTeamMemberNo'));
    var newValue = '';

    if (salesRepArr[0] == '0') {
      for (var i = 1; i < salesRepArr.length; i++) {
        newValue += salesRepArr[i];
      }
      newValue += salesRepArr[0];
      FormManager.setValue('enterprise', newValue);
      FormManager.setValue('salesBusOffCd', newValue.substring(0, 3));
    } else {
      FormManager.setValue('enterprise', FormManager.getActualValue('repTeamMemberNo'));
      FormManager.setValue('salesBusOffCd', FormManager.getActualValue('repTeamMemberNo').length > 2 ? FormManager.getActualValue('repTeamMemberNo').substring(0, 3) : FormManager
          .getActualValue('repTeamMemberNo'));
    }
  });
  if (_ILentConnect && _ILentConnect[0]) {
    _ILentConnect[0].onChange();
  }
}

function fieldsReadOnlyIsrael() {
  var custType = FormManager.getActualValue('custSubGrp');
  FormManager.readOnly('enterprise');

  var role = null;

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Requester') {
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
  } else if (role == 'Processor') {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');

    // CREATCMR-4293
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (custSubGrp == 'XINT' || custSubGrp == 'INTER' || custSubGrp == 'XBP' || custSubGrp == 'BUSPR') {
      FormManager.removeValidator('clientTier', Validators.REQUIRED);
    }
  }

  if (custType == 'PRICU') {
    FormManager.resetValidations('vat');
    FormManager.readOnly('vat');
  }

}
// Defect 1459920 : Mukesh
function fieldsReadOnlyItaly(fromAddress, scenario, scenarioChanged) {
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqId = FormManager.getActualValue('reqId');
  if (reqType != 'C') {
    return;
  }
  var checkImportIndc = getImportedIndcForItaly();

  if (reqType == 'C' && checkImportIndc == 'N' && scenarioChanged) {
    if (custSubType == 'BUSPR' || custSubType == 'INTER' || custSubType == 'CROBP' || custSubType == 'CROIN' || custSubType == 'BUSSM' || custSubType == 'INTSM' || custSubType == 'BUSVA'
        || custSubType == 'INTVA') {
      FormManager.setValue('isuCd', '21');
      FormManager.setValue('clientTier', '');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('clientTier');
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ '0B14A0', '0B1BA0', '0B1GA0', '0B1EA0', '0B11A0', '0B12A0', '0B13A0' ]);
    } else if (custSubType != 'BUSPR' || custSubType != 'INTER' || custSubType != 'CROBP' || custSubType != 'CROIN' || custSubType != 'BUSSM' || custSubType != 'INTSM' || custSubType != 'BUSVA'
        || custSubType != 'INTVA') {
      /*
       * FormManager.setValue('isuCd', '32'); FormManager.setValue('clientTier',
       * 'S'); FormManager.enable('isuCd'); FormManager.enable('clientTier');
       */
    }
  }
  if (reqType == 'C' && role == 'REQUESTER') {
    console.log("fieldsReadOnlyItaly for REQUESTER..");

    // Defect: 1461349 - For Lock and unlocked
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
    FormManager.resetValidations('abbrevNm');

    if (checkImportIndc == "Y") {
      // fields for Legacy consolidation
      FormManager.readOnly('taxCd1');
      FormManager.readOnly('vat');
      FormManager.readOnly('enterprise');
      FormManager.resetValidations('taxCd1');
      FormManager.resetValidations('vat');
      FormManager.resetValidations('enterprise');
      FormManager.readOnly('identClient');
      FormManager.resetValidations('identClient');
      FormManager.enable('collectionCd');
    }

  } else if (reqType == 'C' && role == 'PROCESSOR') {
    if (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CROCM' || custSubType == 'CROBP' || custSubType == 'CROGO' || custSubType == 'CROLC'
        || custSubType == 'CROUN') {
      FormManager.enable('enterprise');
      FormManager.enable('affiliate');
    }
    if (custSubType == 'PRICU' || custSubType == 'PRISM' || custSubType == 'PRIVA') {
      FormManager.enable('enterprise');
      FormManager.enable('affiliate');
    }
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    if (custSubType == 'GOVST' || custSubType == 'LOCEN' || custSubType == 'UNIVE') {
      FormManager.enable('specialTaxCd');
    }
    if (checkImportIndc == "Y") {
      // fields for Legacy consolidation
      FormManager.readOnly('taxCd1');
      FormManager.readOnly('vat');
      FormManager.readOnly('enterprise');
      FormManager.resetValidations('taxCd1');
      FormManager.resetValidations('vat');
      FormManager.resetValidations('enterprise');
      FormManager.readOnly('identClient');
      FormManager.resetValidations('identClient');
      FormManager.enable('collectionCd');
    }
  }
}

/**
 * Add Latin character validation for address fields
 */
function addLatinCharValidator() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var custType = FormManager.getActualValue('custGrp');
  var validateNonLatin = false;

  // latin addresses
  var addrToChkForIL = new Set([ 'ZI01', 'ZS02', 'CTYA', 'CTYB', 'CTYC' ]);
  var addrToChkForGR = new Set([ 'ZS01', 'ZD01', 'ZI01' ]);
  var addrToChkForTR = new Set([ 'ZS01', 'ZD01', 'ZI01' ]);

  // for cross border
  if (custType == 'CROSS') {
    addrToChkForGR = new Set([ 'ZP01', 'ZS01', 'ZD01', 'ZI01' ]);
    addrToChkForTR = new Set([ 'ZP01', 'ZS01', 'ZD01', 'ZI01' ]);
  }

  if (cntry == SysLoc.ISRAEL && addrToChkForIL.has(addrType)) {
    validateNonLatin = true;
  } else if (cntry == SysLoc.GREECE && addrToChkForGR.has(addrType)) {
    validateNonLatin = true;
  } else if (cntry == SysLoc.TURKEY && addrToChkForTR.has(addrType)) {
    validateNonLatin = true;
  }

  if (validateNonLatin) {
    if (cntry == SysLoc.ISRAEL) {
      checkAndAddValidator('custNm2', Validators.LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.LATIN, [ 'Address Con\'t' ]);
      checkAndAddValidator('dept', Validators.LATIN, [ 'Attention Person' ]);
    } else if (cntry == SysLoc.GREECE) {
      checkAndAddValidator('custNm2', Validators.LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.LATIN, [ ' Address Con\'t/Occupation' ]);
      checkAndAddValidator('dept', Validators.LATIN, [ 'District' ]);
    } else if (cntry == SysLoc.TURKEY) {
      checkAndAddValidator('custNm2', Validators.LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.LATIN, [ 'Street Con\'t' ]);
      checkAndAddValidator('dept', Validators.LATIN, [ 'District' ]);
    }
    checkAndAddValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
    checkAndAddValidator('addrTxt', Validators.LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.LATIN, [ 'City' ]);
    checkAndAddValidator('postCd', Validators.LATIN, [ 'Postal Code' ]);
    checkAndAddValidator('poBox', Validators.LATIN, [ 'PO Box' ]);
    // checkAndAddValidator('custPhone', Validators.LATIN, [ 'Phone #' ]);
  } else {
    FormManager.removeValidator('custNm1', Validators.LATIN);
    FormManager.removeValidator('custNm2', Validators.LATIN);
    FormManager.removeValidator('addrTxt', Validators.LATIN);
    FormManager.removeValidator('addrTxt2', Validators.LATIN);
    FormManager.removeValidator('city1', Validators.LATIN);
    FormManager.removeValidator('postCd', Validators.LATIN);
    FormManager.removeValidator('dept', Validators.LATIN);
    FormManager.removeValidator('poBox', Validators.LATIN);
    // FormManager.removeValidator('custPhone', Validators.LATIN);
  }
}

/**
 * Add Non-Latin character validation for address fields
 */
function addNonLatinCharValidator() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var custType = FormManager.getActualValue('custGrp');
  var landCntry = FormManager.getActualValue('landCntry');

  // local addresses
  var addrToChkForIL = new Set([ 'ZS01', 'ZP01', 'ZD01' ]);
  var addrToChkForGR = new Set([ 'ZP01' ]);
  var validateLatin = false;

  // for cross border
  if (custType == 'CROSS' || landCntry != 'GR') {
    addrToChkForGR = new Set();
  }

  if (cntry == SysLoc.ISRAEL && addrToChkForIL.has(addrType)) {
    validateLatin = true;
  } else if (cntry == SysLoc.GREECE && addrToChkForGR.has(addrType)) {
    validateLatin = true;
  }

  if (validateLatin) {
    if (cntry == SysLoc.ISRAEL) {
      checkAndAddValidator('custNm2', Validators.NON_LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.NON_LATIN, [ 'Address Con\'t' ]);
      checkAndAddValidator('dept', Validators.NON_LATIN, [ 'Attention Person' ]);
      checkAndAddValidator('custNm1', Validators.NON_LATIN, [ 'Customer Name' ]);
    } else if (cntry == SysLoc.GREECE) {
      // commenting custNm2 for defect Fix 1359086
      // checkAndAddValidator('custNm2', Validators.NON_LATIN, [ 'Nickname' ]);
      checkAndAddValidator('addrTxt2', Validators.NON_LATIN, [ 'Address Con\'t/Occupation' ]);
      checkAndAddValidator('dept', Validators.NON_LATIN, [ 'District' ]);
      checkAndAddValidator('taxOffice', Validators.NON_LATIN, [ 'Tax Office' ]);
      checkAndAddValidator('custNm4', Validators.NON_LATIN, [ 'Att. Person' ]);
    }
    checkAndAddValidator('addrTxt', Validators.NON_LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.NON_LATIN, [ 'City' ]);
    checkAndAddValidator('postCd', Validators.NON_LATIN, [ 'Postal Code' ]);
    checkAndAddValidator('poBox', Validators.NON_LATIN, [ 'PO Box' ]);
    // checkAndAddValidator('custPhone', Validators.NON_LATIN, [ 'Phone #' ]);

    if (cntry == SysLoc.ISRAEL && custType == 'CROSS') {
      FormManager.removeValidator('postCd', Validators.NON_LATIN);
    }
  } else {
    FormManager.removeValidator('custNm1', Validators.NON_LATIN);
    FormManager.removeValidator('custNm2', Validators.NON_LATIN);
    FormManager.removeValidator('addrTxt', Validators.NON_LATIN);
    FormManager.removeValidator('addrTxt2', Validators.NON_LATIN);
    FormManager.removeValidator('city1', Validators.NON_LATIN);
    FormManager.removeValidator('postCd', Validators.NON_LATIN);
    FormManager.removeValidator('dept', Validators.NON_LATIN);
    FormManager.removeValidator('poBox', Validators.NON_LATIN);
    // FormManager.removeValidator('custPhone', Validators.NON_LATIN);
    FormManager.removeValidator('taxOffice', Validators.NON_LATIN);
  }
}

/**
 * No non-Latin characters for UKI
 */
function addLatinCharValidatorUKI() {
  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.LATIN, [ 'Customer Name Con\'t' ]);
  FormManager.addValidator('addrTxt', Validators.LATIN, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN, [ 'Street Con\'t' ]);
  FormManager.addValidator('city1', Validators.LATIN, [ 'City' ]);
}
// Defect 1468006: Local languages accepted by the tool :Mukesh
function addLatinCharValidatorITALY() {
  console.log("Allow Latin character only..");
  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
}

/**
 * Add Turkish character validation for address fields
 */
function addTurkishCharValidator() {
  var addrType = FormManager.getActualValue('addrType');
  var custType = FormManager.getActualValue('custGrp');
  var validateTurkish = false;

  // turkish addresses
  var addrToChkForTR = new Set([]);
  var land_cntry = FormManager.getActualValue('landCntry');
  // for LOCAL
  if (custType == 'LOCAL' || land_cntry == 'TR') {
    addrToChkForTR = new Set([ 'ZP01' ]);
  }

  if (addrToChkForTR.has(addrType)) {
    validateTurkish = true;
  }

  if (validateTurkish) {
    checkAndAddValidator('custNm1', turkish, [ 'Customer Name' ]);
    checkAndAddValidator('custNm2', turkish, [ 'Customer Name Con\'t' ]);
    checkAndAddValidator('custNm4', turkish, [ 'Name 4' ]);
    checkAndAddValidator('addrTxt', turkish, [ 'Street Address' ]);
    checkAndAddValidator('addrTxt2', turkish, [ 'Street Con\'t' ]);
    checkAndAddValidator('city1', turkish, [ 'City' ]);
    checkAndAddValidator('dept', turkish, [ 'District' ]);
    checkAndAddValidator('postCd', turkish, [ 'Postal Code' ]);
    checkAndAddValidator('poBox', turkish, [ 'PO Box' ]);
    checkAndAddValidator('taxOffice', turkish, [ 'Tax Office' ]);
  } else {
    FormManager.removeValidator('custNm1', turkish);
    FormManager.removeValidator('custNm2', turkish);
    FormManager.removeValidator('custNm4', turkish);
    FormManager.removeValidator('addrTxt', turkish);
    FormManager.removeValidator('addrTxt2', turkish);
    FormManager.removeValidator('city1', turkish);
    FormManager.removeValidator('postCd', turkish);
    FormManager.removeValidator('dept', turkish);
    FormManager.removeValidator('poBox', turkish);
    FormManager.removeValidator('taxOffice', turkish);
  }
}

function setEconomicCode() {
  var requestId = FormManager.getActualValue('reqId');
  qParams = {
    REQ_ID : requestId,
  };
  var result = cmr.query('GET.ECONOMIC_CD_BY_REQID', qParams);
  var economicCdResult = result.ret1;
  var economicCd = FormManager.getActualValue('economicCd');
  if (economicCdResult == '') {
    FormManager.setValue('economicCd', '');
  } else {
    FormManager.getActualValue('economicCd');
  }
}

function addPostalCodeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postal_cd = FormManager.getActualValue('postCd');
        /*
         * DENNIS Defect 1830120: PP: Getting 'Cannot get postal code error' for
         * postal codes for UK
         */
        // if (FormManager.getActualValue('landCntry') != 'GB') {
        // return new ValidationResult(null, true);
        // } else {
        var postcodeRegEx = /^[a-zA-Z]{1,2}([0-9]{1,2}|[0-9][a-zA-Z])\s*[0-9][a-zA-Z]{2}$/;
        if (postcodeRegEx.test(postal_cd)) {
          return new ValidationResult(null, true);
        } else {
          return new ValidationResult(null, false, 'Postal Code format error. Please refer to info bubble for details. ');
        }
        // }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addPostalCodeLengthValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postal_cd = FormManager.getActualValue('postCd');
        var land_cntry = FormManager.getActualValue('landCntry');
        if (land_cntry == 'IE') {
          if (postal_cd.length != 8) {
            return new ValidationResult(null, false, 'Postal Code should be 8 characters long.');
          }
        } else {
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

/**
 * postal code plus city should not exceed 28 char for UKI
 */
function addPostCdCityValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postal_cd = FormManager.getActualValue('postCd');
        var city = FormManager.getActualValue('city1');
        var total_length = postal_cd.length + city.length;
        var land_cntry = FormManager.getActualValue('landCntry');
        var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
        if ((issu_cntry == '866' && land_cntry != 'GB') || (issu_cntry == '754' && land_cntry != 'IE')) {
          if (total_length > 28) {
            return new ValidationResult(null, false, 'Limit for Postal code together with City exceeded max length 28 characters.');
          }
        } else {
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addAbbrevNmValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevNm = FormManager.getActualValue('abbrevNm');
        var reg = /[^\u0020-\u007e]/;
        if (_abbrevNm != '' && (_abbrevNm.length > 0 && !reg.test(_abbrevNm))) {
          return new ValidationResult({
            id : 'abbrevNm',
            type : 'text',
            name : 'abbrevNm'
          }, false, 'The value for Abbreviated name is invalid. Only ALPHANUMERIC characters are allowed.');
        } else {
          return new ValidationResult(null, true);
        }

      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addEMEAClientTierISULogic() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  if (!PageManager.isReadOnly()) {
    FormManager.enable('clientTier');
  }
  _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (!value) {
      value = FormManager.getActualValue('isuCd');
    }
    if (!value) {
      FormManager.setValue('clientTier', '');
    }
    var tierValues = null;
    if (value == '32') {
      tierValues = [ 'B', 'M', 'S' ];
    } else if (value == '34') {
      tierValues = [ 'A', 'E', 'V', '4', '6' ];
    } else if (value != '') {
      tierValues = [ 'Z' ];
    }

    if (tierValues != null) {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), tierValues);
    } else {
      FormManager.resetDropdownValues(FormManager.getField('clientTier'));
    }
  });
  if (_ISUHandler && _ISUHandler[0]) {
    _ISUHandler[0].onChange();
  }
}

function addIEClientTierISULogic() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  if (!PageManager.isReadOnly()) {
    FormManager.enable('clientTier');
  }
  _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (!value) {
      value = FormManager.getActualValue('isuCd');
    }
    if (!value) {
      FormManager.setValue('clientTier', '');
    }
    var tierValues = null;
    if (value == '32') {
      tierValues = [ 'B', 'C', 'M' ];
    } else if (value == '34') {
      tierValues = [ 'A', 'E', 'V', '4', '6' ];
    } else if (value != '') {
      tierValues = [ 'Z' ];
    }

    if (tierValues != null) {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), tierValues);
    } else {
      FormManager.resetDropdownValues(FormManager.getField('clientTier'));
    }

    addSBOSRLogicIE();
  });
  if (_ISUHandler && _ISUHandler[0]) {
    _ISUHandler[0].onChange();
  }
}

function addILClientTierISULogic() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  if (!PageManager.isReadOnly()) {
    FormManager.enable('clientTier');
  }
  _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (!value) {
      value = FormManager.getActualValue('isuCd');
    }
    if (!value) {
      FormManager.setValue('clientTier', '');
    }
    var tierValues = null;
    if (value == '32') {
      tierValues = [ 'B', 'M', 'N', 'S', 'Z' ];
    } else if (value == '34') {
      tierValues = [ 'A', 'E', 'V', '4', '6', 'Z' ];
    } else if (value != '') {
      tierValues = [ 'Z' ];
    }

    if (tierValues != null) {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), tierValues);
    } else {
      FormManager.resetDropdownValues(FormManager.getField('clientTier'));
    }
  });
  if (_ISUHandler && _ISUHandler[0]) {
    _ISUHandler[0].onChange();
  }
}

function addUKClientTierISULogic() {
  if (!PageManager.isReadOnly()) {
    FormManager.enable('clientTier');
  }
  _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (!value) {
      value = FormManager.getActualValue('isuCd');
    }
    if (_pagemodel.isuCd != value) {
      // FormManager.setValue('clientTier', '');
    }
    var tierValues = null;
    if (value == '32') {
      tierValues = [ 'B', 'N', 'S', 'T', 'C', 'M' ];
    } else if (value == '34') {
      tierValues = [ 'A', 'V', '6' ];
    } else if (value == '21') {
      tierValues = [ 'Z' ];
    } else if (value != '') {
      tierValues = [ '' ];
    }
    if (tierValues != null) {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), tierValues);
    } else {
      FormManager.resetDropdownValues(FormManager.getField('clientTier'));
    }
  });
  if (_ISUHandler && _ISUHandler[0]) {
    _ISUHandler[0].onChange();
  }
}

function addSBOSalesRepLogicIreland() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  FormManager.setValue('salesBusOffCd', '090');
  FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), [ '', '090' ]);
  _clientTier = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
    if (!value) {
      return;
    }
    addSBOSRLogicIE();
  });
  if (_clientTier && _clientTier[0]) {
    _clientTier[0].onChange();
  }
}

function addSBODependcyLogicUK() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), [ '' ]);
  _tierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
    if (!value) {
      return;
    }
    FormManager.setValue('salesBusOffCd', '');
    FormManager.setValue('repTeamMemberNo', '');
    FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ '' ]);
    var _isuCd = FormManager.getActualValue('isuCd');
    var _clientTier = FormManager.getActualValue('clientTier');
    var sbo = [];
    if (_isuCd != '' && _clientTier != '') {
      var qParams = {
        _qall : 'Y',
        ISU_CD : '%' + _isuCd + '%',
        CLIENT_TIER : _clientTier
      };
      var results = cmr.query('GET.SBO.UK', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          sbo.push(results[i].ret1);
        }
        FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), sbo);
        if (sbo.length == 1) {
          FormManager.setValue('salesBusOffCd', sbo[0]);
        }
      }
    }
  });
  if (_tierHandler && _tierHandler[0]) {
    _tierHandler[0].onChange();
  }
}

function addSalesRepDependcyLogicUK() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ '' ]);
  _sboHandler = dojo.connect(FormManager.getField('salesBusOffCd'), 'onChange', function(value) {
    if (!value) {
      return;
    }
    var _isuCd = FormManager.getActualValue('isuCd');
    var _clientTier = FormManager.getActualValue('clientTier');
    var _sbo = FormManager.getActualValue('salesBusOffCd');
    var salesRepValue = [];
    if (_isuCd != '' && _clientTier != '' && _sbo != '') {
      var qParams = {
        _qall : 'Y',
        ISU_CD : '%' + _isuCd + '%',
        CLIENT_TIER : _clientTier,
        SALES_BO_CD : _sbo
      };
      var results = cmr.query('GET.SALESREP.UK', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          salesRepValue.push(results[i].ret1);
        }
        FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesRepValue);
        if (salesRepValue.length == 1) {
          FormManager.setValue('repTeamMemberNo', salesRepValue[0]);
        }
      }
    }
  });
  if (_sboHandler && _sboHandler[0]) {
    _sboHandler[0].onChange();
  }
}

function addSalesRepLogicUK2018() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  _sboHandler = dojo.connect(FormManager.getField('salesBusOffCd'), 'onChange', function(value) {
    if (!value) {
      return;
    }

    // add Scotland Coverage 2018 logic between SBO and SR
    var _reqId = FormManager.getActualValue('reqId');
    var postCdParams = {
      REQ_ID : _reqId,
      ADDR_TYPE : "ZI01",
    };
    var postCdResult = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', postCdParams);
    var postCd = postCdResult.ret1;
    if (postCd != null && postCd.length > 2) {
      postCd = postCd.substring(0, 2);
    }
    var isuCd = FormManager.getActualValue('isuCd');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (isuCd == '32' && isScotlandPostCd(postCd)) {
      if (custSubGrp != 'INTER') {
        // Scotland logic
        FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ 'SPA116', 'SPA758' ]);
      }
    } else {
      // UK logic
      var _sbo = FormManager.getActualValue('salesBusOffCd');
      var salesRepValue = [];
      if (_sbo != '') {
        var qParams = {
          _qall : 'Y',
          SALES_BO_CD : _sbo
        };
        var results = cmr.query('GET.SALESREP.UK2018', qParams);
        if (results != null) {
          for (var i = 0; i < results.length; i++) {
            salesRepValue.push(results[i].ret1);
          }
          FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesRepValue);
          if (salesRepValue.length == 1) {
            FormManager.setValue('repTeamMemberNo', salesRepValue[0]);
          }
        }
      }
    }
  });
  if (_sboHandler && _sboHandler[0]) {
    _sboHandler[0].onChange();
  }
}

function updateAbbrevNmLocnIsrael(cntry, addressMode, saving, finalSave, force) {
  var role = null;
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    reqType = FormManager.getActualValue('reqType');
  }
  if (reqType != 'C') {
    return;
  }
  if (role != 'Requester') {
    // do not update for non-requesters
    return;
  }
  if (finalSave || force || addressMode == 'COPY') {
    var copyTypes = document.getElementsByName('copyTypes');
    var copyingToA = false;
    if (copyTypes != null && copyTypes.length > 0) {
      copyTypes.forEach(function(input, i) {
        if (input.value == 'CTYA' && input.checked) {
          copyingToA = true;
        }
      });
    }
    var addrType = FormManager.getActualValue('addrType');
    if (addrType == 'CTYA' || copyingToA) {
      // generate Abbreviated Name/Location
      var abbrevNm = FormManager.getActualValue('custNm1');
      var abbrevLocn = FormManager.getActualValue('city1');
      if (abbrevNm && abbrevNm.length > 22) {
        abbrevNm = abbrevNm.substring(0, 22);
      }
      if (abbrevLocn && abbrevLocn.length > 12) {
        abbrevLocn = abbrevLocn.substring(0, 12);
      }
      FormManager.setValue('abbrevNm', abbrevNm);
      FormManager.setValue('abbrevLocn', abbrevLocn);
    }
  }
}

function addILAddressTypeValidator() {
  console.log("addILAddressTypeValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '755') {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'All address types are mandatory.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var updateInd = null;
          var mailingCnt = 0;
          var billingCnt = 0;
          var installingCnt = 0;
          var shippingCnt = 0;
          var eplCnt = 0;
          var ctyACnt = 0;
          var ctyBCnt = 0;
          var ctyCCnt = 0;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            updateInd = record.updateInd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (updateInd) == 'object') {
              updateInd = updateInd[0];
            }
            if (type == 'ZS01') {
              mailingCnt++;
            } else if (type == 'ZP01') {
              billingCnt++;
            } else if (type == 'ZI01') {
              installingCnt++;
            } else if (type == 'ZD01') {
              shippingCnt++;
            } else if (type == 'ZS02') {
              eplCnt++;
            } else if (type == 'CTYA') {
              ctyACnt++;
            } else if (type == 'CTYB') {
              ctyBCnt++;
            } else if (type == 'CTYC') {
              ctyCCnt++;
            }
          }

          if (mailingCnt == 0 || ctyACnt == 0 || billingCnt == 0 || ctyBCnt == 0 || installingCnt == 0 || shippingCnt == 0 || ctyCCnt == 0 || eplCnt == 0) {
            return new ValidationResult(null, false, 'All address types are mandatory.');
          } else if (mailingCnt > 1 || ctyACnt > 1) {
            return new ValidationResult(null, false, 'For Mailing address, only one in Latin and in Hebrew is allowed.');
          } else if (billingCnt > 1 || ctyBCnt > 1) {
            return new ValidationResult(null, false, 'For Billing address, only one in Latin and in Hebrew is allowed.');
          } else if (installingCnt > 1) {
            return new ValidationResult(null, false, 'For Installing address, only one in Latin is allowed.');
          } else if (eplCnt > 1) {
            return new ValidationResult(null, false, 'For EPL address, only one in Latin is allowed.');
          } else if (shippingCnt != ctyCCnt) {
            return new ValidationResult(null, false, 'The number of Shipping and Country Use C addresses should be the same.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addILAddressTypeMailingValidator() {
  console.log("addILAddressTypeMailingValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '755') {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var updateInd = null;
          var mailingUpdated = false;
          var ctyACntUpdated = false;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            updateInd = record.updateInd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (updateInd) == 'object') {
              updateInd = updateInd[0];
            }
            if (type == 'ZS01' && updateInd == 'U') {
              mailingUpdated = true;
            } else if (type == 'CTYA' && updateInd == 'U') {
              ctyACntUpdated = true;
            }
          }
          if (FormManager.getActualValue('reqType') == 'U') {
            if (mailingUpdated == true && ctyACntUpdated == false) {
              return new ValidationResult(null, false, 'Mailing address is updated, please also update Country Use A (Mailing) address.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addILAddressTypeBillingValidator() {
  console.log("addILAddressTypeBillingValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '755') {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var updateInd = null;
          var billingUpdated = false;
          var ctyBCntUpdated = false;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            updateInd = record.updateInd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (updateInd) == 'object') {
              updateInd = updateInd[0];
            }
            if (type == 'ZP01' && updateInd == 'U') {
              billingUpdated = true;
            } else if (type == 'CTYB' && updateInd == 'U') {
              ctyBCntUpdated = true;
              ;
            }
          }
          if (FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X') {
            if (billingUpdated == true && ctyBCntUpdated == false) {
              return new ValidationResult(null, false, 'Billing address is updated, please also update Country Use B (Billing) address.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addILAddressTypeCntyAValidator() {
  console.log("addILAddressTypeCntyAValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '755') {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var updateInd = null;
          var mailingUpdated = false;
          var ctyACntUpdated = false;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            updateInd = record.updateInd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (updateInd) == 'object') {
              updateInd = updateInd[0];
            }
            if (type == 'ZS01' && updateInd == 'U') {
              mailingUpdated = true;
            } else if (type == 'CTYA' && updateInd == 'U') {
              ctyACntUpdated = true;
            }
          }
          if (FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X') {
            if (mailingUpdated == false && ctyACntUpdated == true) {
              return new ValidationResult(null, false, 'Country Use A (Mailing) address is updated, please also update Mailing address.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addILAddressTypeCntyBValidator() {
  console.log("addILAddressTypeCntyBValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '755') {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var updateInd = null;
          var billingUpdated = false;
          var ctyBCntUpdated = false;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            updateInd = record.updateInd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (updateInd) == 'object') {
              updateInd = updateInd[0];
            }
            if (type == 'ZP01' && updateInd == 'U') {
              billingUpdated = true;
            } else if (type == 'CTYB' && updateInd == 'U') {
              ctyBCntUpdated = true;
            }
          }
          if (FormManager.getActualValue('reqType') == 'U') {
            if (billingUpdated == false && ctyBCntUpdated == true) {
              return new ValidationResult(null, false, 'Country Use B (Billing) is updated, please also update Billing address.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

var _addrTypesIL = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'CTYA', 'CTYB', 'CTYC' ];
var _addrTypeHandler = [];
function afterConfigForIsrael() {
  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      FormManager.resetValidations('vat');
      if (!dijit.byId('vatExempt').get('checked')) {
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      }
    });
  }

  // addrType Handler
  for (var i = 0; i < _addrTypesIL.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesIL[i]), 'onClick', function(value) {
        setCustPhone(value);
      });
    }
  }

  GEOHandler.setAddressTypeForName('CTYA');

  // Israel COD Flag change
  if (FormManager.getActualValue('reqType') == 'C') {
    setCodFlagVal();
  }
}

/**
 * 1310266 - 'newAddress' enable custPhone for mailing address
 */
function setCustPhone(value) {
  if (FormManager.getField('addrType_ZS01').checked) {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  }
}

/**
 * Update address - disable custPhone for not mailing address
 */
function disableCustPhone() {
  var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
  if (cntryCd == SysLoc.ISRAEL && FormManager.getActualValue('addrType') != 'ZS01') {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  } else {
    FormManager.enable('custPhone');
  }
}

// reset value for subIndustryCd when ISIC is empty

function resetSubIndustryCd() {
  if (PageManager.isReadOnly()) {
    return;
  }
  var interval = new Object();
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  interval = setInterval(function() {
    var isicValue = FormManager.getActualValue('isicCd');
    if (isicValue == '') {
      if (typeof (_pagemodel) != 'undefined' && _pagemodel.isicCd != null && _pagemodel.isicCd != '') {
        isicValue = _pagemodel.isicCd;
      }
    }
    if (isicValue != null && isicValue.length > 0 && viewOnlyPage != 'true') {
      FormManager.enable('subIndustryCd');
      clearInterval(interval);
    } else {
      FormManager.readOnly('subIndustryCd');
    }
  }, 1000);
}

function removeValidationInacNac() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL) {
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('inacCd', Validators.NUMBER);
  }
}

function defaultCapIndicatorUKI() {
  if ((FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) && FormManager.getActualValue('reqType') == 'C') {
    if (FormManager.getField('capInd').set) {
      FormManager.getField('capInd').set('checked', true);
    } else if (FormManager.getField('capInd')) {
      FormManager.getField('capInd').checked = true;
    }
    FormManager.readOnly('capInd');
  }
}

function defaultCapIndicator() {
  if ((FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE
      || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.CYPRUS || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY)
      && FormManager.getActualValue('reqType') == 'C') {
    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY && FormManager.getField('capInd') == undefined) {
      return;
    }
    FormManager.getField('capInd').set('checked', true);
    FormManager.readOnly('capInd');
  }
}

function postalCodeNumericOnlyForDomestic() {
  dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {

    var custGroup = FormManager.getActualValue('custGrp');
    var postalCode = FormManager.getActualValue('postCd');
    var postCd = FormManager.getActualValue('postCd');

    if (custGroup == "LOCAL") {
      var postalCodeCheck = /^\d+$/.test(postalCode);
      if (!postalCodeCheck) {
        cmr.showAlert('Postal code should only allow numeric characters for domestic scenario.');
        FormManager.setValue(postCd, '');
      }
    }
  });
}

function validateEMEACopy(addrType, arrayOfTargetTypes) {
  console.log('Addr Type: ' + addrType + " Targets: " + arrayOfTargetTypes);
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custType = FormManager.getActualValue('custGrp');
  var reqType = FormManager.getActualValue('reqType');
  if (cntry == SysLoc.ISRAEL) {
    var hebrewSource = addrType == 'ZS01' || addrType == 'ZP01' || addrType == 'ZD01';
    if (hebrewSource
        && (arrayOfTargetTypes.indexOf('ZI01') >= 0 || arrayOfTargetTypes.indexOf('ZS02') >= 0 || arrayOfTargetTypes.indexOf('CTYA') >= 0 || arrayOfTargetTypes.indexOf('CTYB') >= 0 || arrayOfTargetTypes
            .indexOf('CTYC') >= 0)) {
      return 'Cannot copy Hebrew address to non-Hebrew addresses. Please select only Hebrew target addresses.';
    }
    if (!hebrewSource && (arrayOfTargetTypes.indexOf('ZS01') >= 0 || arrayOfTargetTypes.indexOf('ZP01') >= 0 || arrayOfTargetTypes.indexOf('ZD01') >= 0)) {
      return 'Cannot copy non-Hebrew address to Hebrew addresses. Please select only non-Hebrew target addresses.';
    }
  } else if (cntry == SysLoc.GREECE) {
    var greekSource = addrType == 'ZP01';
    if (greekSource && custType != 'CROSS' && (arrayOfTargetTypes.indexOf('ZS01') >= 0 || arrayOfTargetTypes.indexOf('ZD01') >= 0)) {
      return 'Cannot copy Greek address to non-Greek addresses. Please select only Greek target addresses.';
    }
    if (!greekSource && custType != 'CROSS' && arrayOfTargetTypes.indexOf('ZP01') >= 0) {
      return 'Cannot copy non-Greek address to Greek addresses. Please select only non-Greek target addresses.';
    }
  } else if (cntry == SysLoc.ITALY) {
    if (typeof (_allAddressData) == 'undefined' || !_allAddressData || _allAddressData.length == 0) {
      return null;
    }
    if (reqType == 'C') {
      var trg = null;
      for (var i = 0; i < arrayOfTargetTypes.length; i++) {
        trg = arrayOfTargetTypes[i];
        for (var j = 0; j < _allAddressData.length; j++) {
          console.log('Type: ' + _allAddressData[j].addrType[0] + ' Import: ' + _allAddressData[j].importInd[0]);
          if (trg == _allAddressData[j].addrType[0] && _allAddressData[j].importInd[0] == 'Y') {
            return 'Cannot copy to an address that cannot be modified.';
          }
        }
      }
    } else if (reqType == 'U' || reqType == 'X') {
      var cmrNo = FormManager.getActualValue('cmrNo');
      var trg = null;
      for (var i = 0; i < arrayOfTargetTypes.length; i++) {
        trg = arrayOfTargetTypes[i];
        for (var j = 0; j < _allAddressData.length; j++) {
          console.log('Type: ' + _allAddressData[j].addrType[0] + ' Par CMR: ' + _allAddressData[j].parCmrNo[0]);
          if (trg == _allAddressData[j].addrType[0] && _allAddressData[j].parCmrNo[0] != cmrNo) {
            return 'Cannot copy to an address under a different CMR No.';
          }
        }
      }
    }
  }
  return null;
}

function setAbbrvLocCrossBorderScenario() {
  var interval = new Object();

  interval = setInterval(function() {
    var custGroup = FormManager.getActualValue('custGrp');

    if (custGroup != null && custGroup.length > 0) {
      if (custGroup == "CROSS") {
        var reqId = FormManager.getActualValue('reqId');
        if (reqId != null) {
          reqParam = {
            REQ_ID : reqId,
          };
        }
        var results = cmr.query('ADDR.GET.CTYALANDCNTRY.BY_REQID', reqParam);
        var abbrevLocn = results.ret1;
        if (abbrevLocn != null) {
          if (abbrevLocn && abbrevLocn.length > 12) {
            abbrevLocn = abbrevLocn.substring(0, 12);
          }
          FormManager.setValue('abbrevLocn', abbrevLocn);
        } else {
          FormManager.setValue('abbrevLocn', '');
        }
        clearInterval(interval);
      } else {
        clearInterval(interval);
      }

    }
  }, 1000);
}

function setAbbrvLocCrossBorderScenarioOnChange() {
  dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {

    var custGroup = FormManager.getActualValue('custGrp');

    if (custGroup == "CROSS") {
      var reqId = FormManager.getActualValue('reqId');
      if (reqId != null) {
        reqParam = {
          REQ_ID : reqId,
        };
      }
      var results = cmr.query('ADDR.GET.CTYALANDCNTRY.BY_REQID', reqParam);
      var abbrevLocn = results.ret1;
      if (abbrevLocn != null) {
        if (abbrevLocn && abbrevLocn.length > 12) {
          abbrevLocn = abbrevLocn.substring(0, 12);
        }
        FormManager.setValue('abbrevLocn', abbrevLocn);
      } else {
        FormManager.setValue('abbrevLocn', '');
      }
    }
  });
}

function setSpecialTaxCodeOnScenarioIT() {
  // for legacy consolidation CMR-492
  var landCntryZS01 = null;
  var reqId = FormManager.getActualValue('reqId');
  var custGroup = FormManager.getActualValue('custGrp');

  if (FormManager.getActualValue('reqType') == 'C' && custGroup == 'CROSS') {
    landCntryZS01 = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', {
      REQ_ID : reqId,
      ADDR_TYPE : 'ZS01'
    });

    FormManager.setValue('specialTaxCd', '');
    if (landCntryZS01 != null && landCntryZS01.ret1 == 'IT') {
      FormManager.setValue('specialTaxCd', 'A');
    } else {
      FormManager.setValue('specialTaxCd', 'N');
    }
  }

}

function setSpecialTaxCodeOnAddressIT() {

  var custGroup = FormManager.getActualValue('custGrp');
  var landCntry = FormManager.getActualValue('landCntry');
  var addrType = FormManager.getActualValue('addrType');
  console.log("landCntry:" + landCntry + " addrType:" + addrType);
  if (FormManager.getActualValue('reqType') == 'C' && custGroup == 'CROSS') {
    if (landCntry != null && addrType != null && landCntry == 'IT' && addrType == 'ZS01') {
      FormManager.setValue('specialTaxCd', 'A');
    } else {
      FormManager.setValue('specialTaxCd', 'N');
    }
  }
}
// CMR-1500 Billing level fields are enabled for Create imports when Installing
// CMR is imported
function enableDisableTaxCodeCollectionCdIT() {

  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    FormManager.enable('specialTaxCd');
    FormManager.enable('collectionCd');
  }
  if (reqType == 'U' || reqType == 'X') {
    var qParams = {
      ADDR_TYPE : 'ZP01',
      REQ_ID : reqId
    };

    var billingResult = cmr.query('CHECK_BILLING_IMPORTED_IT', qParams);

    var billingCMR = '';
    if (billingResult != null && billingResult.ret1 != undefined) {
      billingCMR = billingResult.ret1;
    }
    console.log("billingCMR :>" + billingCMR);
    if (billingCMR == '') {
      FormManager.readOnly('specialTaxCd');
      FormManager.readOnly('collectionCd');
    } else {
      FormManager.enable('specialTaxCd');
      FormManager.enable('collectionCd');
    }
  }
}
function addGRAddressTypeValidator() {
  console.log("addGRAddressTypeValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.GREECE) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Local Language translation of Sold-to, Sold To, Ship To, and Install At addresses are required.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var zs01Cnt = 0;
          var zp01Cnt = 0;
          var zd01Cnt = 0;
          var zi01Cnt = 0;
          var zs01Data = null;
          var zp01Data = null;

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
              zs01Data = record;
              zs01Cnt++;
            } else if (type == 'ZP01') {
              zp01Data = record;
              zp01Cnt++;
            } else if (type == 'ZD01') {
              zd01Cnt++;
            } else if (type == 'ZI01') {
              zi01Cnt++;
            }
          }

          if (FormManager.getActualValue('custGrp') == 'LOCAL') {
            mismatchFields = getMismatchFields(zs01Data, zp01Data, false);
            if (mismatchFields != '') {
              return new ValidationResult(null, false, 'Sold-to mismatch, please update Local Language translation of Sold-to: ' + mismatchFields);
            }
          } else if (FormManager.getActualValue('custGrp') == 'CROSS' && zs01Data != null && zp01Data != null && !isTranslationAddrFieldsMatchForGR(zs01Data, zp01Data)) {
            return new ValidationResult(null, false, 'Local language not applicable for Cross-border, address must match sold to data.');
          } else if (FormManager.getActualValue('reqType') == 'U' && !isLandedCntryMatch(zs01Data, zp01Data)) {
            return new ValidationResult(null, false, '\'Country (Landed)\' of Local Language translation of Sold-to should match Sold-to.');
          } else if ((FormManager.getActualValue('reqType') == 'U')) {
            var mismatchFields = '';
            // GR then not crossborder
            if (zs01Data.landCntry[0] == 'GR') {
              mismatchFields = getMismatchFields(zs01Data, zp01Data, false);
            } else if (zs01Data.landCntry[0] != 'GR') {
              mismatchFields = getMismatchFields(zs01Data, zp01Data, true);
            }
            if (mismatchFields != '') {
              return new ValidationResult(null, false, 'Sold-to mismatch, please update Local Language translation of Sold-to: ' + mismatchFields);
            }
          }

          if (zs01Cnt == 0 || zp01Cnt == 0 || zd01Cnt == 0 || zi01Cnt == 0) {
            return new ValidationResult(null, false, 'Local Language translation of Sold-to, Sold To, Ship To, and Install At addresses are required.');
          } else if (zs01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Sold To address is allowed.');
          } else if (zp01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Local Language translation of Sold-to address is allowed.');
          }
          return new ValidationResult(null, true);

        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function isTranslationAddrFieldsMatchForGR(zs01Data, zp01Data) {

  if (zs01Data.custNm1[0] == zp01Data.custNm1[0] && zs01Data.custNm2[0] == zp01Data.custNm2[0] && zs01Data.custNm4[0] == zp01Data.custNm4[0] && zs01Data.addrTxt[0] == zp01Data.addrTxt[0]
      && zs01Data.addrTxt2[0] == zp01Data.addrTxt2[0] && zs01Data.poBox[0] == zp01Data.poBox[0] && zs01Data.postCd[0] == zp01Data.postCd[0] && zs01Data.city1[0] == zp01Data.city1[0]) {
    return true;
  }

  return false;
}

function getMismatchFields(zs01Data, zp01Data, isCrossborder) {
  console.log('isCrossborder: ' + isCrossborder);

  var mismatchFields = '';
  if (!hasMatchingFieldsFilled(zs01Data.addrTxt[0], zp01Data.addrTxt[0], isCrossborder)) {
    mismatchFields += 'Street Address';
  }
  if (!hasMatchingFieldsFilled(zs01Data.custNm2[0], zp01Data.custNm2[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Customer Name Con\'t';
  }
  if (!hasMatchingFieldsFilled(zs01Data.addrTxt2[0], zp01Data.addrTxt2[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Address Con\'t/Occupation';
  }
  if (!hasMatchingFieldsFilled(zs01Data.poBox[0], zp01Data.poBox[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'PO Box';
  }

  if (isCrossborder) {
    if (!hasMatchingFieldsFilled(zs01Data.custNm1[0], zp01Data.custNm1[0], isCrossborder)) {
      mismatchFields += mismatchFields != '' ? ', ' : '';
      mismatchFields += 'Customer Name';
    }
    if (!hasMatchingFieldsFilled(zs01Data.postCd[0], zp01Data.postCd[0], isCrossborder)) {
      mismatchFields += mismatchFields != '' ? ', ' : '';
      mismatchFields += 'Postal Code';
    }
    if (!hasMatchingFieldsFilled(zs01Data.city1[0], zp01Data.city1[0], isCrossborder)) {
      mismatchFields += mismatchFields != '' ? ', ' : '';
      mismatchFields += 'City';
    }
  }

  var mismatchFields = '';
  if (!hasMatchingFieldsFilled(zs01Data.addrTxt[0], zp01Data.addrTxt[0], isCrossborder)) {
    mismatchFields += 'Street Address';
  }
  if (!hasMatchingFieldsFilled(zs01Data.custNm2[0], zp01Data.custNm2[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Customer Name Con\'t';
  }
  if (!hasMatchingFieldsFilled(zs01Data.addrTxt2[0], zp01Data.addrTxt2[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Address Con\'t/Occupation';
  }
  if (!hasMatchingFieldsFilled(zs01Data.poBox[0], zp01Data.poBox[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'PO Box';
  }
  if (!hasMatchingFieldsFilled(zs01Data.custNm4[0], zp01Data.custNm4[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Att. Person';
  }

  if (isCrossborder) {
    if (!hasMatchingFieldsFilled(zs01Data.custNm1[0], zp01Data.custNm1[0], isCrossborder)) {
      mismatchFields += mismatchFields != '' ? ', ' : '';
      mismatchFields += 'Customer Name';
    }
    if (!hasMatchingFieldsFilled(zs01Data.postCd[0], zp01Data.postCd[0], isCrossborder)) {
      mismatchFields += mismatchFields != '' ? ', ' : '';
      mismatchFields += 'Postal Code';
    }
    if (!hasMatchingFieldsFilled(zs01Data.city1[0], zp01Data.city1[0], isCrossborder)) {
      mismatchFields += mismatchFields != '' ? ', ' : '';
      mismatchFields += 'City';
    }
  }

  return mismatchFields;
}

function hasMatchingFieldsFilled(zs01Field, zp01Field, isCrossborder) {
  console.log(zs01Field + ' : ' + zp01Field);
  if (!isCrossborder) {
    // local just check if empty or not
    if (zs01Field != '' && zs01Field != null) {
      if (zp01Field == '' || zp01Field == null) {
        return false;
      }
    }
    if (zp01Field != '' && zp01Field != null) {
      if (zs01Field == '' || zs01Field == null) {
        return false;
      }
    }
  } else {
    // check if it is matching
    if (zs01Field != zp01Field) {
      return false;
    }
  }
  return true;
}

function isLandedCntryMatch(zs01Data, zp01Data) {
  if (zs01Data.landCntry[0] == zp01Data.landCntry[0]) {
    return true;
  }

  return false;
}

function populateTranslationAddrWithSoldToData() {
  if (FormManager.getActualValue('custGrp') == 'CROSS' && CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && FormManager.getActualValue('addrType') == 'ZP01') {
    var record = null;
    var type = null;
    var zs01Data = null; // Sold-to

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
        zs01Data = record;
        break;
      }
    }

    // Populate Local language translation of sold to with Sold to data
    if (zs01Data != null) {
      FormManager.setValue('custNm1', zs01Data.custNm1);
      FormManager.setValue('custNm2', zs01Data.custNm2);
      FormManager.setValue('addrTxt', zs01Data.addrTxt);
      FormManager.setValue('addrTxt2', zs01Data.addrTxt2);
      FormManager.setValue('poBox', zs01Data.poBox);
      FormManager.setValue('postCd', zs01Data.postCd);
      FormManager.setValue('city1', zs01Data.city1);
    }
  }
}
// Add individual function to prevent different requirement in future
function populateTranslationAddrWithSoldToDataTR() {
  if (FormManager.getActualValue('custGrp') == 'CROSS' && CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && FormManager.getActualValue('addrType') == 'ZP01') {
    var record = null;
    var type = null;
    var zs01Data = null; // Sold-to

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
        zs01Data = record;
        break;
      }
    }

    // Populate Local language translation of sold to with Sold to data
    if (zs01Data != null) {
      FormManager.setValue('custNm1', zs01Data.custNm1);
      FormManager.setValue('custNm2', zs01Data.custNm2);
      FormManager.setValue('addrTxt', zs01Data.addrTxt);
      FormManager.setValue('addrTxt2', zs01Data.addrTxt2);
      FormManager.setValue('poBox', zs01Data.poBox);
      FormManager.setValue('postCd', zs01Data.postCd);
      FormManager.setValue('city1', zs01Data.city1);
      FormManager.setValue('dept', zs01Data.city1);
    }
  }
}

function clearAddrFieldsForGR() {
  FormManager.clearValue('custNm1');
  FormManager.clearValue('custNm2');
  FormManager.clearValue('addrTxt');
  FormManager.clearValue('addrTxt2');
  FormManager.clearValue('poBox');
  FormManager.clearValue('postCd');
  FormManager.clearValue('city1');
}

function clearAddrFieldsForTR() {
  FormManager.clearValue('custNm1');
  FormManager.clearValue('custNm2');
  FormManager.clearValue('addrTxt');
  FormManager.clearValue('addrTxt2');
  FormManager.clearValue('poBox');
  FormManager.clearValue('postCd');
  FormManager.clearValue('city1');
  FormManager.clearValue('dept');
}

var _addrSelectionHistGR = '';
function preFillTranslationAddrWithSoldToForGR(cntry, addressMode, saving) {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
    var custType = FormManager.getActualValue('custGrp');

    // for local don't proceed
    if (custType == 'LOCAL' || cmr.addressMode == 'copyAddress') {
      return;
    }
    if (!saving) {
      if (FormManager.getActualValue('addrType') == 'ZP01') {
        populateTranslationAddrWithSoldToData();
      } else if (FormManager.getActualValue('addrType') != 'ZP01' && addressMode != 'updateAddress' && _addrSelectionHistGR == 'ZP01') {
        // clear address fields when switching
        clearAddrFieldsForGR();
      }
    }
    _addrSelectionHistGR = FormManager.getActualValue('addrType');
  }
}

var _addrSelectionHistTR = '';
function preFillTranslationAddrWithSoldToForTR(cntry, addressMode, saving) {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    var custType = FormManager.getActualValue('custGrp');
    // for local don't proceed
    if (custType == 'LOCAL') {
      return;
    }
    if (!saving) {
      if (FormManager.getActualValue('addrType') == 'ZP01') {
        populateTranslationAddrWithSoldToDataTR();
      } else if (FormManager.getActualValue('addrType') != 'ZP01' && addressMode != 'updateAddress' && _addrSelectionHistTR == 'ZP01') {
        // clear address fields when switching
        clearAddrFieldsForTR();
      }
    }
    _addrSelectionHistTR = FormManager.getActualValue('addrType');
  }
}

function addTRAddressTypeValidator() {
  console.log("addTRAddressTypeValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Sold-To/Ship-To/Install-At/Local Language Translation of Sold-To are mandatory.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var updateInd = null;
          var zs01Cnt = 0;
          var zp01Cnt = 0;
          var zd01Cnt = 0;
          var zi01Cnt = 0;
          var zs01Copy;
          var zp01Copy;
          var zs01Upd = false;
          var custType = FormManager.getActualValue('custGrp');
          var compareFields = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'stateProv', 'postCd', 'dept', 'poBox', 'landCntry' ];
          var compareFieldsLocal = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'stateProv', 'postCd', 'dept', 'poBox', 'taxOffice' ];
          var compareFieldsNonLocal = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'stateProv', 'postCd', 'dept', 'poBox' ];
          var compareFieldsValue = null;
          var enErrMsg = '';
          var turkishErrMsg = '';
          var reqType = cmr.currentRequestType;
          if (reqType == undefined) {
            reqType = FormManager.getActualValue('reqType');
          }
          if (reqType == 'U') {
            compareFields.push('custNm4');
          }
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            updateInd = record.updateInd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            var addrTypeText = record.addrTypeText;
            if (typeof (addrTypeText) == 'object') {
              addrTypeText = addrTypeText[0];
            }
            if (typeof (updateInd) == 'object') {
              updateInd = updateInd[0];
            }
            // Valid english for all address types when
            // 1: All address types except ZP01
            // 2: For ZP01, create request => scenario is CROSS,
            // update request=> land country is not TR
            if (type != 'ZP01' || (type == 'ZP01' && (custType == 'CROSS' || (reqType == 'U' && record['landCntry'][0] != 'TR')))) {
              if (type == 'ZP01') {
                compareFieldsValue = compareFieldsLocal;
              } else {
                compareFieldsValue = compareFieldsNonLocal;
              }
              for (var j = 0; j < compareFieldsValue.length; j++) {
                var value = record[compareFieldsValue[j]];
                if (typeof (value) == 'object') {
                  value = value[0];
                }
                if ((value != null && value != undefined && value != '' && typeof (value) == 'string') && updateInd == 'U') {
                  var reg = /[^\u0000-\u007f]/;
                  if (reg.test(value)) {
                    enErrMsg += addrTypeText + ', ';
                    break;
                  }
                }
              }
            }

            if (type == 'ZS01') {
              zs01Cnt++;
              zs01Copy = record;
            } else if (type == 'ZP01') {
              zp01Cnt++;
              zp01Copy = record;
            } else if (type == 'ZD01') {
              zd01Cnt++;
            } else if (type == 'ZI01') {
              zi01Cnt++;
            }
          }

          if (enErrMsg != '') {
            enErrMsg = enErrMsg.substring(0, enErrMsg.lastIndexOf(','));
            enErrMsg += ' must be in English.';
            return new ValidationResult(null, false, enErrMsg);
          }

          if (zs01Cnt == 0 || zp01Cnt == 0 || zd01Cnt == 0 || zi01Cnt == 0) {
            return new ValidationResult(null, false, 'Sold-To/Ship-To/Install-At/Local Language Translation of Sold-To are mandatory.');
          } else if (zs01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Sold-To is allowed.');
          } else if (zp01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Local Language Translation of Sold-To is allowed.');
          }

          var mismatchErrMsg = '';
          for (var i = 0; i < compareFields.length; i++) {
            if (custType == 'CROSS' || (reqType == 'U' && zp01Copy['landCntry'][0] != 'TR')) {
              if (zs01Copy[compareFields[i]][0] != zp01Copy[compareFields[i]][0]) {
                mismatchErrMsg += mappingAddressField(compareFields[i]) + ', ';
              }
            } else if (custType == 'LOCAL' || (reqType == 'U' && zp01Copy['landCntry'][0] == 'TR')) {
              if ((zs01Copy[compareFields[i]] == '' || zs01Copy[compareFields[i]] == null || zs01Copy[compareFields[i]] == undefined)
                  && (zp01Copy[compareFields[i]] != '' && zp01Copy[compareFields[i]] != null && zp01Copy[compareFields[i]] != undefined)) {
                mismatchErrMsg += mappingAddressField(compareFields[i]) + ', ';
              }
              if ((zp01Copy[compareFields[i]] == '' || zp01Copy[compareFields[i]] == null || zp01Copy[compareFields[i]] == undefined)
                  && (zs01Copy[compareFields[i]] != '' && zs01Copy[compareFields[i]] != null && zs01Copy[compareFields[i]] != undefined)) {
                mismatchErrMsg += mappingAddressField(compareFields[i]) + ', ';
              }
            }
          }

          if (mismatchErrMsg != '') {
            mismatchErrMsg = mismatchErrMsg.substring(0, mismatchErrMsg.lastIndexOf(','));
            return new ValidationResult(null, false, 'Sold-to and Local Translation, mismatched fields: ' + mismatchErrMsg);
          }

          var zs01data = [];
          var zp01data = [];
          var zp01converted = [];
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            updateInd = record.updateInd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (updateInd) == 'object') {
              updateInd = updateInd[0];
            }

            if (type == 'ZS01') {
              if (updateInd == 'U') {
                zs01Upd = true;
              }
              zs01data.push(_allAddressData[i].custNm1[0]);
              zs01data.push(_allAddressData[i].custNm4[0]);
              zs01data.push(_allAddressData[i].addrTxt[0]);
              zs01data.push(_allAddressData[i].city1[0]);
              zs01data.push(_allAddressData[i].dept[0]);
            }
            if (type == 'ZP01') {
              zp01data.push(_allAddressData[i].custNm1[0]);
              zp01data.push(_allAddressData[i].custNm4[0]);
              zp01data.push(_allAddressData[i].addrTxt[0]);
              zp01data.push(_allAddressData[i].city1[0]);
              zp01data.push(_allAddressData[i].dept[0]);
            }
          }

          for (var i = 0; i < zp01data.length; i++) {
            zp01converted.push(modifyCharForTurk(zp01data[i]));
          }

          if (zs01Upd == true) {
            var errorMessage = '';
            if (zs01data[0] != zp01converted[0]) {
              errorMessage = errorMessage + 'Customer Name';
            }
            if (zs01data[1] != zp01converted[1]) {
              errorMessage = errorMessage + (errorMessage.length > 0 ? ', Name 4' : 'Name 4');
            }
            if (zs01data[2] != zp01converted[2]) {
              errorMessage = errorMessage + (errorMessage.length > 0 ? ', Street' : 'Street');
            }
            if (zs01data[3] != zp01converted[3]) {
              errorMessage = errorMessage + (errorMessage.length > 0 ? ', City' : 'City');
            }
            if (zs01data[4] != zp01converted[4]) {
              errorMessage = errorMessage + (errorMessage.length > 0 ? ', District' : 'District');
            }
            if (errorMessage.length > 0) {
              return new ValidationResult(null, false, "Field value mismatch for Sold-To and Local Language Translation of Sold To: " + errorMessage);
            }
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function mappingAddressField(key) {
  var value = '';
  if (key == 'custNm1') {
    value = 'Customer Name';
  } else if (key == 'custNm2') {
    value = 'Customer Name Con\'t';
  } else if (key == 'custNm4') {
    value = 'Name 4';
  } else if (key == 'addrTxt') {
    value = 'Street Address';
  } else if (key == 'addrTxt2') {
    value = 'Street Con\'t';
  } else if (key == 'city1') {
    value = 'City';
  } else if (key == 'stateProv') {
    value = 'State Province';
  } else if (key == 'postCd') {
    value = 'Postal Code';
  } else if (key == 'dept') {
    value = 'District';
  } else if (key == 'poBox') {
    value = 'PO Box';
  } else if (key == 'landCntry') {
    value = 'Country (Landed)';
  }
  return value;
}

function addCYAddressTypeValidator() {
  console.log("addCYAddressTypeValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.CYPRUS) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Mailing/Billing/Installing/Shipping/EPL address is required.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var zs01Cnt = 0;
          var zd01Cnt = 0;

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
            } else if (type == 'ZD01') {
              zd01Cnt++;
            }
          }

          if (zs01Cnt == 0) {
            return new ValidationResult(null, false, 'Mailing/Billing/Installing/Shipping/EPL address is required.');
          } else if (zs01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Mailing/Billing/Installing/Shipping/EPL is allowed.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

var _gtcISUHandler = null;
var _CTCHandler = null;
var _gtcISRHandler = null;
var _gtcAddrTypes = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01' ];
var _gtcAddrTypeHandler = [];
var _gtcVatExemptHandler = null;
function addHandlersForGRCYTR() {
  var custType = FormManager.getActualValue('custGrp');
  /*
   * if (_gtcISUHandler == null) { // Turkey create/update both need
   * setClientTierAndISR
   * 
   * if (FormManager.getActualValue('reqType') == 'C' ||
   * FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
   * _gtcISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange',
   * function(value) { if (FormManager.getActualValue('cmrIssuingCntry') !=
   * SysLoc.TURKEY) { FormManager.clearValue('repTeamMemberNo');
   * FormManager.setValue('salesBusOffCd', '');
   * FormManager.setValue('salesTeamCd', ''); } setClientTierAndISR(value); }); } }
   */

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
    if (_custSalesRepHandlerGr == null) {
      _custSalesRepHandlerGr = dojo.connect(FormManager.getField('repTeamMemberNo'), 'onChange', function(value) {
        setISRValuesGR();
      });
    }
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
    if (_custSubTypeHandlerGr == null) {
      _custSubTypeHandlerGr = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
        FormManager.setValue('salesTeamCd', '');
        setISRValuesGR();
        setSalesBoSboIbo();
        resetSubIndustryCdGR();
      });
    }
  }
  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {

      if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
        setISRValuesGR();
      } else {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
          setISRValues();
        }
      }

    });
  }

  if (_gtcISRHandler == null) {
    dojo.connect(FormManager.getField('repTeamMemberNo'), 'onChange', function(value) {
      // setSalesBoSboIbo();
    });
  }

  for (var i = 0; i < _gtcAddrTypes.length; i++) {
    _gtcAddrTypeHandler[i] = null;
    if (_gtcAddrTypeHandler[i] == null) {
      _gtcAddrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _gtcAddrTypes[i]), 'onClick', function(value) {
        convertToUpperCaseGR();
        disableAddrFieldsGRCYTR();
        disableAddrFieldsGR();
        preFillTranslationAddrWithSoldToForGR();
        preFillTranslationAddrWithSoldToForTR();
        disableTaxOfficeTR();
      });
    }
  }

  if (_gtcVatExemptHandler == null) {
    _gtcVatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorGRCYTR();
    });
  }
}

function addPOBoxValidatorGR() {
  FormManager.removeValidator('poBox', Validators.LATIN);
  FormManager.removeValidator('poBox', Validators.NON_LATIN);
  FormManager.addValidator('poBox', Validators.DIGIT, [ 'PO Box' ]);
}

function setVatValidatorGRCYTR() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (FormManager.getActualValue('custSubGrp') == 'IBMEM') {
      FormManager.readOnly('vat');
    }
    if (dijit.byId('vatExempt').get('checked')) {
      FormManager.clearValue('vat');
    }
    if (undefined != dijit.byId('vatExempt') && !dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
      FormManager.enable('vat');
    }
  }
}

function setClientTierAndISR(value) {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  /*
   * if (!PageManager.isReadOnly()) { FormManager.enable('clientTier'); }
   */
  if (!value) {
    value = FormManager.getActualValue('isuCd');
  }
  if (_pagemodel.isuCd != value) {
    // FormManager.setValue('clientTier', '');
  }
  tierValues = null;
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
    if (value == '34') {
      tierValues = [ 'V', '6', 'A', 'Z' ];
    } else if (value == '32') {
      tierValues = [ 'B', 'N', 'S', 'Z', 'M', '6' ];
    } else if (value == '5B') {
      tierValues = [ '' ];
    } else if (value == '21') {
      tierValues = [ '' ];
    }
  } else if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.CYPRUS) {
    if (value == '34') {
      tierValues = [ 'V', '6', 'A', 'Z' ];
    } else if (value == '32') {
      tierValues = [ 'B', 'N', 'S', 'Z', 'M' ];
    } else if (value == '21') {
      tierValues = [ '' ];
    }
  } else if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    if (value == '34') {
      tierValues = [ 'V', 'Q' ];
    } else if (value == '32') {
      // remove ISU+CTC=32B from all scenarioes
      tierValues = [ 'N', 'S', 'T' ];
    } else if (value == '5B' || value == '21' || value == '8B') {
      tierValues = [ '' ];
    }
  }
  if (tierValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), tierValues);
    if (tierValues.length == 1) {
      FormManager.setValue('clientTier', tierValues[0]);
    }
  } else {
    FormManager.resetDropdownValues(FormManager.getField('clientTier'));
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE
      && (FormManager.getActualValue('custSubGrp') != 'COMME' || FormManager.getActualValue('custSubGrp') != 'CROSS' || FormManager.getActualValue('custSubGrp') != 'GOVRN' || FormManager
          .getActualValue('custSubGrp') != 'PRICU')) {
    setISRValuesGR();
  } else if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    console.log("skip set ISR.");
  } else {
    setISRValues();
  }

}

function setISRValuesGR() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'SPAS') {
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.setValue('abbrevLocn', 'SAAS');
    FormManager.readOnly('repTeamMemberNo');
  } else if (custSubGrp == 'BUSPR' || custSubGrp == 'XBP') {
    FormManager.setValue('repTeamMemberNo', '200005');
    FormManager.readOnly('repTeamMemberNo');
  } else if (custSubGrp == 'INTER' || custSubGrp == 'XINTR') {
    FormManager.setValue('repTeamMemberNo', '000000');
  }
  var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  setEnterprise(repTeamMemberNo);
  FormManager.readOnly('subIndustryCd');
  if (custSubGrp == 'COMME' || custSubGrp == 'CROSS' || custSubGrp == 'PRICU' || custSubGrp == 'GOVRN' || custSubGrp == '') {
    setISRValues();
  }
  if (repTeamMemberNo == '') {
    FormManager.setValue('salesSR', '');
    FormManager.setValue('salesTeamCd', '');
    FormManager.setValue('salesBusOffCd', '');
  }
}

function setISRValuesGROnUpdate() {
  if (FormManager.getActualValue('reqType') == 'U') {
    setISRValuesGR();
  }
}
function setFieldsBehaviourGR() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  setEnterprise(repTeamMemberNo);
  if (custSubGrp == 'CROSS' || custSubGrp == 'COMME') {
    FormManager.enable('clientTier');
    FormManager.enable('repTeamMemberNo');
  }
  if (viewOnlyPage != 'true') {
    if (role == 'PROCESSOR') {
      FormManager.enable('abbrevLocn');
      FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
      FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');

      // CREATCMR-4293
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      if (custSubGrp == 'XINT' || custSubGrp == 'INTER' || custSubGrp == 'XBP' || custSubGrp == 'BUSPR') {
        FormManager.removeValidator('clientTier', Validators.REQUIRED);
      }
    }
    if (role == 'REQUESTER') {
      FormManager.resetValidations('isuCd');
      FormManager.resetValidations('clientTier');
      FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    }
  }
  FormManager.addValidator('custPrefLang', Validators.REQUIRED, [ 'Preferred Language' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('cmrOwner', Validators.REQUIRED, [ 'CMR Owner' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.resetValidations('subIndustryCd');
    FormManager.resetValidations('isicCd');
    FormManager.resetValidations('repTeamMemberNo');
    FormManager.resetValidations('isuCd');
    FormManager.resetValidations('clientTier');
    FormManager.readOnly('custPrefLang');
    FormManager.resetValidations('salesTeamCd');
  }
  FormManager.resetValidations('sitePartyId');
  FormManager.readOnly('sitePartyId');
  FormManager.readOnly('subIndustryCd');
}

function resetSubIndustryCdGR() {
  if (PageManager.isReadOnly()) {
    return;
  }
  var interval = new Object();
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  interval = setInterval(function() {
    var isicValue = FormManager.getActualValue('isicCd');
    if (isicValue == '') {
      if (typeof (_pagemodel) != 'undefined' && _pagemodel.isicCd != null && _pagemodel.isicCd != '') {
        isicValue = _pagemodel.isicCd;
      }
    }
    if (isicValue != null && isicValue.length > 0 && viewOnlyPage != 'true') {
      clearInterval(interval);
      FormManager.readOnly('subIndustryCd');
    } else {
      FormManager.readOnly('subIndustryCd');
    }
  }, 1000);
}

function setISRValues() {
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var isrs = [];
  if (isuCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
      ISU : '%' + isuCd + clientTier + '%'
    };
    var results = cmr.query('GET.ISRLIST.BYISU', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        isrs.push(results[i].ret1);
      }
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), isrs);
      if (isrs.length == 1) {
        setEnterprise(isrs[0]);
        FormManager.setValue('repTeamMemberNo', isrs[0]);
      }
      // setSalesBoSboIbo();
    }
  }
}

function setEnterprise(value) {
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var isu = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  if (cmrCntry == SysLoc.GREECE && value == 'R21180' && isu == '32' && ctc == 'M') {
    FormManager.setValue('enterprise', '822835');
  } else if (cmrCntry == SysLoc.GREECE && value == 'R21180' && isu == '32' && ctc == 'S') {
    FormManager.setValue('enterprise', '822806');

  } else if (cmrCntry == SysLoc.GREECE && value == '000000' && isu == '34' && ctc == '6') {
    FormManager.setValue('enterprise', '822836');
  } else if (cmrCntry == SysLoc.GREECE && value == '000000' && isu == '32' && ctc == '6') {
    FormManager.setValue('enterprise', '822835');
  } else if (cmrCntry == SysLoc.GREECE && value == '000000' && isu == '32' && ctc == 'S') {
    FormManager.setValue('enterprise', '822806');
  } else if (cmrCntry == SysLoc.CYPRUS && value == 'E33290' && isu == '32' && ctc == 'M') {
    FormManager.setValue('enterprise', '822835');
  } else {
    FormManager.setValue('enterprise', '');
  }
}

function hideCollectionCd() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY && FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('CollectionCd', 'collectionCd');
  } else {
    FormManager.resetValidations('collectionCd');
    FormManager.hide('CollectionCd', 'collectionCd');
  }
}

function setSalesBoSboIbo() {
  var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  if (repTeamMemberNo != '') {
    var qParams = {
      ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
      REP_TEAM_CD : repTeamMemberNo
    };
    var result = cmr.query('DATA.GET.SALES_BO_CD', qParams);
    var salesBoCd = result.ret1;
    var selsr = result.ret2;
    FormManager.setValue('salesBusOffCd', salesBoCd);
    FormManager.setValue('salesTeamCd', selsr);
  } else {
    // FormManager.setValue('salesBusOffCd', '');
  }
}

function addShippingAddrTypeValidator() {
  console.log("addShippingAddrTypeValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '755') {
          return new ValidationResult(null, true);
        }

        if (FormManager.getActualValue('reqType') == 'U') {
          if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
            var recordList = null;
            var addrType = null;
            var updateIndicator = null;
            var addrSequence = null;
            var recordLists = null;
            var updateIndicators = null;
            var pairedSequence = null;
            var pairedSequences = null;
            var addrSequences = null;
            for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
              recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
              if (recordList == null && _allAddressData != null && _allAddressData[i] != null) {
                recordList = _allAddressData[i];
              }
              addrType = recordList.addrType;
              updateIndicator = recordList.updateInd;
              addrSequence = recordList.addrSeq;
              pairedSequences = recordList.pairedSeq;

              if (typeof (updateIndicator) == 'object') {
                updateIndicator = updateIndicator[0];
              }
              if (typeof (addrSequence) == 'object') {
                addrSequence = addrSequence[0];
              }
              if (typeof (pairedSequences) == 'object') {
                pairedSequences = pairedSequences[0];
              }

              if (addrType == 'ZD01' && updateIndicator == 'U') {
                for (var j = 0; j < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; j++) {
                  recordLists = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(j);
                  updateIndicators = recordLists.updateInd;
                  pairedSequence = recordLists.pairedSeq;
                  if (typeof (pairedSequence) == 'object') {
                    pairedSequence = pairedSequence[0];
                  }
                  if (typeof (updateIndicators) == 'object') {
                    updateIndicators = updateIndicators[0];
                  }

                  if (recordLists.addrType == 'CTYC' && addrSequence == pairedSequence && updateIndicators != 'U') {
                    return new ValidationResult(null, false, 'The translated Country Use C address ' + addrSequence + ' should also be updated.');
                  }
                }
              }

              if (addrType == 'CTYC' && updateIndicator == 'U') {
                for (var j = 0; j < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; j++) {
                  recordLists = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(j);
                  updateIndicators = recordLists.updateInd;
                  pairedSequence = recordLists.pairedSeq;
                  addrSequences = recordLists.addrSeq;
                  if (typeof (addrSequences) == 'object') {
                    addrSequences = addrSequences[0];
                  }
                  if (typeof (pairedSequence) == 'object') {
                    pairedSequence = pairedSequence[0];
                  }
                  if (typeof (updateIndicators) == 'object') {
                    updateIndicators = updateIndicators[0];
                  }
                  if (recordLists.addrType == 'ZD01' && addrSequences == pairedSequences && updateIndicators != 'U') {
                    return new ValidationResult(null, false, 'The Shipping address ' + addrSequences + ' should also be updated.');
                  }
                }
              }
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function convertToUpperCaseGR(cntry, addressMode, saving) {
  var custType = FormManager.getActualValue('custGrp');

  // for cross border
  if (custType == 'CROSS') {
    return;
  }

  // Greek address - block lowercase
  var addrFields = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'postCd', 'custPhone', 'sapNo', 'taxOffice', 'custNm4' ];
  // add for CMR-2383-Tax Office
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    addrFields = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'postCd', 'custPhone', 'sapNo', 'taxOffice' ];
  }
  if (FormManager.getActualValue('addrType') == 'ZP01') {
    for (var i = 0; i < addrFields.length; i++) {
      dojo.byId(addrFields[i]).style.textTransform = 'uppercase';
      if (saving) {
        dojo.byId(addrFields[i]).value = dojo.byId(addrFields[i]).value.toUpperCase();
      }
    }
  } else {
    for (var i = 0; i < addrFields.length; i++) {
      dojo.byId(addrFields[i]).style.textTransform = 'none';
    }
  }
}

function updateAddrTypeList(cntry, addressMode, saving) {
  if (!saving && FormManager.getActualValue('cmrIssuingCntry') != '862') {
    // hide 'additional shipping' selection for creates .
    if ((addressMode == 'newAddress' || addressMode == 'copyAddress') && cmr.currentRequestType == 'C') {
      cmr.hideNode('radiocont_ZD01');
    }
    // if (FormManager.getActualValue('cmrIssuingCntry') == "862") {
    // cmr.showNode('radiocont_ZD01');
    // }
  }
}

function setFieldsToReadOnlyGRCYTR() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
  }
  FormManager.readOnly('salesTeamCd');
  FormManager.readOnly('subIndustryCd');
}

function updateAbbrevNmLocnGRCYTR(cntry, addressMode, saving, finalSave, force) {
  var role = null;
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    reqType = FormManager.getActualValue('reqType');
  }
  if (reqType != 'C') {
    return;
  }
  if (role != 'Requester') {
    // do not update for non-requesters
    return;
  } else {
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
        var abbrevNm = FormManager.getActualValue('custNm1');
        var abbrevLocn = FormManager.getActualValue('city1');
        if (abbrevNm && abbrevNm.length > 22) {
          abbrevNm = abbrevNm.substring(0, 22);
        }
        if ("TR" != FormManager.getActualValue("landCntry")) {
          if (document.getElementById('landCntry') != undefined) {
            abbrevLocn = document.getElementById('landCntry').value;
          }
        }
        if (abbrevLocn && abbrevLocn.length > 12) {
          abbrevLocn = abbrevLocn.substring(0, 12);
        }

        if (cntry !== null && cntry == SysLoc.TURKEY) {
          abbrevNm = modifyCharForTurk(abbrevNm);
          abbrevLocn = modifyCharForTurk(abbrevLocn);
        }

        FormManager.setValue('abbrevNm', abbrevNm);
        FormManager.setValue('abbrevLocn', abbrevLocn);
      }
    }
  }
}

function addrFunctionForGRCYTR(cntry, addressMode, saving) {
  if (!saving) {
    var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
    var custType = FormManager.getActualValue('custGrp');

    if ((addressMode == 'updateAddress' || addressMode == 'copyAddress') && FormManager.getActualValue('landCntry') == '') {
      FormManager.setValue('landCntry', cmr.oldlandcntry);
    }
    // for cross border
    if (custType == 'CROSS' && cmr.currentRequestType == 'U') {
      FormManager.readOnly('landCntry');
    }
    // for Turkey - cross border
    setDistrictMandatoryTR();
    checkAndAddValidator('landCntry', Validators.REQUIRED, [ 'Country (Landed)' ]);
  }
}

function setDistrictMandatoryTR() {
  var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
  if (cntryCd == SysLoc.TURKEY) {
    var landCntry = FormManager.getActualValue('landCntry');
    if (landCntry == 'TR') {
      checkAndAddValidator('dept', Validators.REQUIRED, [ 'District' ]);
    } else {
      FormManager.removeValidator('dept', Validators.REQUIRED);
    }
  }
}

function retainLandCntryValuesOnCopy() {
  if ((cmr.addressMode == 'copyAddress') && FormManager.getActualValue('landCntry') == '') {
    FormManager.setValue('landCntry', cmr.oldlandcntry);
  }
}

function disableAddrFieldsGRCYTR() {
  var cntryCd = FormManager.getActualValue('cmrIssuingCntry');

  // Tax Office - for mailing/billing address only
  if (cntryCd == SysLoc.GREECE && FormManager.getActualValue('addrType') != 'ZP01') {
    FormManager.setValue('taxOffice', '');
    FormManager.disable('taxOffice');
  } else {
    FormManager.enable('taxOffice');
  }

  // Phone - for mailing/billing address only
  if ((cntryCd == SysLoc.TURKEY) && FormManager.getActualValue('addrType') != 'ZS01') {
    FormManager.setValue('custPhone', '');
    FormManager.hide('CustPhone', 'custPhone');
  } else {
    FormManager.show('CustPhone', 'custPhone');
    FormManager.enable('custPhone');
  }
  // Lock land country when 'LOCAL' scenario
  if (cntryCd == SysLoc.TURKEY) {
    var landCntry = FormManager.getActualValue('landCntry');
    if ((FormManager.getActualValue('custGrp') == 'LOCAL') && landCntry == 'TR' && (FormManager.getActualValue('addrType') == 'ZP01' || FormManager.getActualValue('addrType') == 'ZS01')) {
      FormManager.readOnly('landCntry');
    } else {
      FormManager.enable('landCntry');
    }
  }

  // CMR-1616:PostBox only for cross-border scenrio
  var custGrp = FormManager.getActualValue('custGrp');
  if (cntryCd == SysLoc.TURKEY && custGrp != 'CROSS') {
    FormManager.setValue('poBox', '');
    FormManager.disable('poBox');
  } else {
    FormManager.enable('poBox');
  }
}

function disableAddrFieldsGR() {
  if (FormManager.getActualValue('addrType') != 'ZP01') {
    FormManager.setValue('taxOffice', '');
    FormManager.disable('taxOffice');
  } else {
    FormManager.enable('taxOffice');
  }

  // GR - Phone - for Sold-to and Ship-to
  if ((FormManager.getActualValue('addrType') == 'ZS01' || FormManager.getActualValue('addrType') == 'ZD01')) {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  }

  if (FormManager.getActualValue('addrType') != 'ZP01') {
    FormManager.setValue('taxOffice', '');
    FormManager.disable('taxOffice');
  } else {
    FormManager.enable('taxOffice');
  }

  if (FormManager.getActualValue('addrType') == 'ZP01' || FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.enable('poBox');
  } else {
    FormManager.setValue('poBox', '');
    FormManager.disable('poBox');

  }

  var landCntry = FormManager.getActualValue('landCntry');
  if (!(FormManager.getActualValue('custGrp') == 'CROSS' || isUpdateReqCrossborder()) && landCntry == 'GR'
      && (FormManager.getActualValue('addrType') == 'ZP01' || FormManager.getActualValue('addrType') == 'ZS01')) {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
}

function isUpdateReqCrossborder() {
  if (!(FormManager.getActualValue('custGrp') == 'LOCAL')) {
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      if (_allAddressData != null && _allAddressData[i] != null) {
        if (_allAddressData[i].addrType[0] == 'ZS01') {
          return _allAddressData[i].landCntry[0] != 'GR';
        }
      }
    }
  }
  return false;
}

function hideMOPAFieldForGR() {
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.hide('ModeOfPayment', 'modeOfPayment');
  } else if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('ModeOfPayment', 'modeOfPayment');
  }
}

function setTypeOfCustomerBehaviorForGR() {

  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.hide('CrosSubTyp', 'crosSubTyp');
  } else if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('CrosSubTyp', 'crosSubTyp');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER') {
      FormManager.readOnly('crosSubTyp');
    } else if (role == 'PROCESSOR') {
      FormManager.enable('crosSubTyp');
    }
  }
}

function addPostalCodeLenForTurGreCypValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postal_cd = FormManager.getActualValue('postCd');
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        var custType = FormManager.getActualValue('custGrp');
        if (custType != 'CROSS') {
          if (cmrIssuingCntry == SysLoc.GREECE || cmrIssuingCntry == SysLoc.TURKEY) {
            if (postal_cd.length != 5 && postal_cd.length != '') {
              return new ValidationResult(null, false, 'Postal Code should be 5 characters long.');
            }
          } else if (cmrIssuingCntry == SysLoc.CYPRUS) {
            if (postal_cd.length != 4 && postal_cd.length != '') {
              return new ValidationResult(null, false, 'Postal Code should be 4 characters long.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

// function setPostalCodeTurGreCypValidator() {
// var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
// var postal_cd = FormManager.getActualValue('postCd');
// var custType = FormManager.getActualValue('custGrp');
// if (custType == 'CROSS') {
// FormManager.removeValidator('postCd', Validators.NUMBER);
// } else {
// if (cmrIssuingCntry == SysLoc.GREECE || cmrIssuingCntry == SysLoc.TURKEY) {
// if (postal_cd.length == 5) {
// FormManager.addValidator('postCd', Validators.NUMBER, [ 'Postal Code' ]);
// }
// } else if (cmrIssuingCntry == SysLoc.CYPRUS) {
// if (postal_cd.length == 4) {
// FormManager.addValidator('postCd', Validators.NUMBER, [ 'Postal Code' ]);
// }
// }
// }
// }

function abbrvLocMandatory() {
  var interval = new Object();
  interval = setInterval(function() {
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
    FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');

    if (viewOnlyPage != 'true') {
      if (role != 'REQUESTER') {
        FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'AbbrevLocation' ], 'MAIN_CUST_TAB');
      }
    } else {
      clearInterval(interval);
    }

  }, 1000);
}

function abbrvLocMandatoryOnChange() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  dojo.connect(FormManager.getField('abbrevLocn'), 'onChange', function(value) {
    if (role != 'REQUESTER') {
      FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'AbbrevLocation' ], 'MAIN_CUST_TAB');
    }
  });
}

function setCommonCollectionCd() {
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.readOnly('collectionCd2');
    FormManager.setValue('collectionCd2', '');
  }
}

function hideCustPhoneonSummary() {
  setInterval(function() {
    if (openAddressDetails.addrType == 'ZS01') {
      cmr.hideNode('custPhone_view');
      $('label[for="custPhone_view"]').hide();
    } else {
      cmr.showNode('custPhone_view');
      $('label[for="custPhone_view"]').show();
    }
  }, 1000);

}

function addHandlerForCustSubTypeBpGRTRCY() {
  if (_custSubTypeHandler == null) {
    _custSubTypeHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      // setCustSubTypeBpGRTRCY();
    });
  }
}

function showClassificationForTRUpd() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    if (FormManager.getActualValue('reqType') == 'U') {
      FormManager.show('CustClass', 'custClass');
      var role = FormManager.getActualValue('userRole').toUpperCase();
      if (role != 'REQUESTER') {
        FormManager.enable('CustClass');
        FormManager.addValidator('custClass', Validators.REQUIRED, [ 'Classification Code' ], 'MAIN_CUST_TAB');
      }
    }
  }
}

function setCustSubTypeBpGRTRCY() {
  var custType = FormManager.getActualValue('custSubGrp');
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    if (custType == 'BUSPR') {
      FormManager.readOnly('clientTier');
      FormManager.setValue('clientTier', '');
      FormManager.readOnly('isuCd');
      FormManager.setValue('isuCd', '8B');
    } else if (custType == 'INTER') {
      FormManager.readOnly('clientTier');
      FormManager.setValue('clientTier', '');
      FormManager.readOnly('isuCd');
      FormManager.setValue('isuCd', '21');
    } else if (custType == 'PRICU' || custType == 'XPC') {
      FormManager.enable('clientTier');
      FormManager.setValue('clientTier', 'Q');
      FormManager.enable('isuCd');
      FormManager.setValue('isuCd', '34');
    } else {
      // NOT enable ctc and isu for turkey internal and bp scenario
      if (custType != 'XINT' && custType != 'XBP') {
        FormManager.enable('clientTier');
        FormManager.enable('isuCd');
      }
    }
    // Control Classification Code
    /*
     * if (custType == 'BUSPR' || custType == 'XBP') {
     * FormManager.show('CustClass', 'custClass');
     * FormManager.addValidator('custClass', Validators.REQUIRED, [
     * 'Classification Code' ], 'MAIN_CUST_TAB'); } else {
     * FormManager.hide('CustClass', 'custClass');
     * FormManager.setValue('custClass', '');
     * FormManager.resetValidations('custClass'); }
     */
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.CYPRUS) {
    if (custType == 'BUSPR') {
      FormManager.readOnly('clientTier');
      FormManager.setValue('clientTier', '');
      FormManager.readOnly('isuCd');
      FormManager.setValue('isuCd', '21');
    } else if (custType == 'INTER') {
      FormManager.readOnly('clientTier');
      FormManager.setValue('clientTier', 'Z');
      FormManager.readOnly('isuCd');
      FormManager.setValue('isuCd', '34');
    } else {
      FormManager.enable('clientTier');
      FormManager.enable('isuCd');
    }
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE
      && (FormManager.getActualValue('custSubGrp') != 'COMME' || FormManager.getActualValue('custSubGrp') != 'CROSS' || FormManager.getActualValue('custSubGrp') != 'GOVRN' || FormManager
          .getActualValue('custSubGrp') != 'PRICU')) {
    setISRValuesGR();
  } else if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    console.log("skip set ISR.");
  } else {
    setISRValues();
  }
}

function disableINACEnterpriseOnViewOnly() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    FormManager.readOnly('inacCd');
    FormManager.readOnly('enterprise');
  }
}

function viewOnlyAddressDetails() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnlyPage == 'true' && (cmrIssuingCntry == SysLoc.CYPRUS)) {
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="addrTxt2_view"]').text('Occupation:');
  }

  if (viewOnlyPage == 'true' && cmrIssuingCntry == SysLoc.GREECE) {
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="custNm2_view"]').text('Customer Name Con\'t');
    $('label[for="custNm4_view"]').text('Att. Person');
    $('label[for="addrTxt2_view"]').text(' Address Con\'t/Occupation');
  }

  if (viewOnlyPage == 'true' && cmrIssuingCntry == SysLoc.TURKEY) {
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="addrTxt2_view"]').text('Street Con' + '\'' + 't:');
    $('label[for="dept_view"]').text('District:');
  }
}

function salesSRforUpdate() {
  if (FormManager.getActualValue('reqType') == 'U') {
    // setSalesBoSboIbo();
  }
}

function salesSRforUpdateOnChange() {
  if (FormManager.getActualValue('reqType') == 'U') {
    dojo.connect(FormManager.getField('repTeamMemberNo'), 'onChange', function(value) {
      // setSalesBoSboIbo();
    });
  }

  setClassificationCodeTR();
  dojo.connect(FormManager.getField('commercialFinanced'), 'onChange', function(value) {
    setCOFClassificationCodeTR();
  });

  dojo.connect(FormManager.getField('crosSubTyp'), 'onChange', function(value) {
    setTypeOfCustomerClassificationCodeTR();
  });

  dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
    setIsicClassificationCodeTR(value);
  });
}

function modifyCharForTurk(field) {

  if (field != null && field.length > 0) {
    var modifiedVal = field;

    modifiedVal = modifiedVal.replace(/Ç/g, 'C');
    modifiedVal = modifiedVal.replace(/ç/g, 'c');
    modifiedVal = modifiedVal.replace(/Ğ/g, 'G');
    modifiedVal = modifiedVal.replace(/ğ/g, 'g');
    modifiedVal = modifiedVal.replace(/İ/g, 'I');
    modifiedVal = modifiedVal.replace(/ı/g, 'i');

    modifiedVal = modifiedVal.replace(/Ö/g, 'O');
    modifiedVal = modifiedVal.replace(/ö/g, 'o');
    modifiedVal = modifiedVal.replace(/Ş/g, 'S');
    modifiedVal = modifiedVal.replace(/ş/g, 's');
    modifiedVal = modifiedVal.replace(/Ü/g, 'U');
    modifiedVal = modifiedVal.replace(/ü/g, 'u');

    return modifiedVal;
  }
}
// Not used
function addTaxCodeForProcessorValidator() {
  console.log("register addTaxCodeForProcessorValidator . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var specialTaxCd = FormManager.getActualValue('specialTaxCd');
        var role = FormManager.getActualValue('userRole');
        if (FormManager.getActualValue('reqType') == 'C') {
          if (role == GEOHandler.ROLE_PROCESSOR && (specialTaxCd == null || specialTaxCd.length == 0)) {
            return new ValidationResult({
              id : 'specialTaxCd',
              type : 'text',
              name : 'specialTaxCd'
            }, false, 'Tax Code/ Code IVA should not be blank.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addFieldValidationForProcessorItaly() {
  var requestType = null;
  requestType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');

  if (requestType == 'C' && role == GEOHandler.ROLE_PROCESSOR) {
    var checkImportIndc = getImportedIndcForItaly();

    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('specialTaxCd', Validators.REQUIRED, [ 'Tax Code/ Code IVA' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO (SORTL)' ], 'MAIN_IBM_TAB');
    FormManager.resetValidations('taxCd1');

    FormManager.addValidator('identClient', Validators.REQUIRED, [ 'Ident Client' ], 'MAIN_CUST_TAB');
    // FormManager.addValidator('collectionCd', Validators.REQUIRED, [
    // 'Collection Code/ SSV Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');

    if (checkImportIndc != 'Y'
        && (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'PRICU' || custSubType == 'PRISM' || custSubType == 'PRIVA' || custSubType == 'GOVST'
            || custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'CROGO' || custSubType == 'UNIVE' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'CROUN'
            || custSubType == 'LOCEN' || custSubType == 'LOCSM' || custSubType == 'LOCVA' || custSubType == 'CROLC' || custSubType == 'COMME' || custSubType == 'COMSM' || custSubType == 'COMVA'
            || custSubType == 'CROCM' || custSubType == 'NGOIT' || custSubType == 'NGOSM' || custSubType == 'NGOVA' || custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP')) {
      FormManager.resetValidations('enterprise');
      FormManager.resetValidations('affiliate');
      if ("" != FormManager.getActualValue('isuCd') && ("32" != FormManager.getActualValue('isuCd') || "21" != FormManager.getActualValue('isuCd'))) {
        FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise' ], 'MAIN_IBM_TAB');
        FormManager.addValidator('affiliate', Validators.REQUIRED, [ 'Affiliate' ], 'MAIN_IBM_TAB');
        FormManager.enable('enterprise');
        FormManager.enable('affiliate');
      }
      if ("32" == FormManager.getActualValue('isuCd') || "21" == FormManager.getActualValue('isuCd')) {
        FormManager.resetValidations('enterprise');
        FormManager.resetValidations('affiliate');
        FormManager.enable('enterprise');
        FormManager.enable('affiliate');
      }
    }

    if (checkImportIndc != 'Y' && "32" == FormManager.getActualValue('isuCd')) {
      console.log("company not exit..");
      FormManager.addValidator('collectionCd', Validators.REQUIRED, [ 'Collection Code' ], 'MAIN_IBM_TAB');
      FormManager.enable('collectionCd');
    }

    if (custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'CROGO' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'CROUN' || custSubType == 'LOCSM'
        || custSubType == 'LOCVA' || custSubType == 'CROLC' || custSubType == 'COMSM' || custSubType == 'COMVA' || custSubType == 'CROCM' || custSubType == 'BUSSM' || custSubType == 'BUSVA'
        || custSubType == 'CROBP' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'NGOSM' || custSubType == 'NGOVA' || custSubType == 'PRISM' || custSubType == 'PRIVA') {
      FormManager.enable('collectionCd');
      console.log("addFieldValidationForProcessorItaly>>processor labal enable collection code");
    }
  }
  if (requestType == 'C' && role == GEOHandler.ROLE_PROCESSOR && (custSubType == 'UNISM' || custSubType == 'LOCSM' || custSubType == 'GOVSM')) {
    FormManager.enable('repTeamMemberNo');
  }
}

function addAddrValidationForProcItaly() {
  var requestType = null;
  requestType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  var landCntryVal = FormManager.getActualValue('landCntry');
  if (requestType == 'C' && addrType != 'ZS01') {
    if (landCntryVal == 'IT') {
      FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
    } /*
       * else { FormManager.addValidator('stateProvItaly', Validators.REQUIRED, [
       * 'State/Province' ], null); }
       */
  }
}

function afterConfigForIT() {

  if (_landCntryHandler == null) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      showHideCityStateProvIT();
    });
  }

  if (_stateProvITHandler == null) {
    _stateProvITHandler = dojo.connect(FormManager.getField('stateProvItaly'), 'onChange', function(value) {
      showHideOtherStateProvIT();
    });
  }

  if (_fiscalCodeUpdateHandlerIT == null) {
    _fiscalCodeUpdateHandlerIT = dojo.connect(FormManager.getField('taxCd1'), 'onChange', function(value) {
      autoResetFiscalDataStatus();
    });
  }

  if (_CTCHandlerIT == null) {
    _CTCHandlerIT = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setSalesRepValuesIT(value);
      // autoSetSBOSROnPostalCode(value, false);
      blankedOutCollectionCD();
    });
  }

  if (_ISUHandlerIT == null) {
    _ISUHandlerIT = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValuesIT(value);
      setSalesRepValuesIT(value);
      setAffiliateEnterpriseRequired();
      blankedOutCollectionCD();

    });
  }

  if (_salesRepHandlerIT == null) {
    _salesRepHandlerIT = dojo.connect(FormManager.getField('repTeamMemberNo'), 'onChange', function(value) {
      autoSetSBOOnSRValueIT();
    });
  }

  if (_identClientHandlerIT == null) {
    _identClientHandlerIT = dojo.connect(FormManager.getField('identClient'), 'onChange', function(value) {
      setVATOnIdentClientChangeIT();
      setFiscalCodeOnIdentClientCB_IT();
    });
  }
  if (_vatUpdateHandlerIT == null) {
    _vatUpdateHandlerIT = dojo.connect(FormManager.getField('vat'), 'onChange', function(value) {
      autoResetFiscalDataStatus();
    });
  }

  if (_custGrpIT == null) {
    _custGrpIT = dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
      setSpecialTaxCodeOnScenarioIT();
    });
  }

  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  FormManager.readOnly('cmrOwner');

  if (role.toUpperCase() == 'PROCESSOR' && reqType == 'C') {
    FormManager.addValidator('collectionCd', Validators.REQUIRED, [ 'Collection Code' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('collectionCd', Validators.REQUIRED);
  }

  var cntryRegion = FormManager.getActualValue('countryUse');
  var landCntry = 'IT'; // default to Italy
  if (cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  // set default landed country
  FormManager.setValue('defaultLandedCountry', landCntry);
  FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
  // Story 1594125
  FormManager.readOnly('company');

  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');

  if ((reqType == 'U' || reqType == 'X') && role == 'REQUESTER') {
    FormManager.readOnly('sensitiveFlag');
  } else if ((reqType == 'U' || reqType == 'X') && role == 'PROCESSOR') {
    FormManager.enable('sensitiveFlag');
  }
  if (reqType == 'C' && role == 'REQUESTER' && FormManager.getActualValue('isuCd') == '32') {
    FormManager.readOnly('collectionCd');
  } else if (reqType == 'C' && role == 'PROCESSOR' && FormManager.getActualValue('isuCd') == '32') {
    FormManager.enable('collectionCd');
  }

  if (FormManager.getActualValue('isuCd') != '32') {
    FormManager.enable('collectionCd');
  }

  if (reqType == 'U' || reqType == 'X') {
    var reqId = FormManager.getActualValue('reqId');
    var result = cmr.query('VALIDATOR.BILLING.CROSSBORDERIT', {
      REQID : reqId
    });

    if (result != null && result.ret1 != '' && result.ret1 != undefined && result.ret1 != 'IT') {
      FormManager.setValue('vat', '');
      FormManager.readOnly('vat');
    }
  }

  // Defect 1461349: Abbreviated name, Abbreviated location :Mukesh
  autoSetAbbrevNmOnChanageIT();
  autoSetAbbrevLocnOnChangeIT();

  if (_vatUpdateHandlerIT && _vatUpdateHandlerIT[0]) {
    // _vatUpdateHandlerIT[0].onChange();
  }

  if (_identClientHandlerIT && _identClientHandlerIT[0]) {
    _identClientHandlerIT[0].onChange();
  }

  if (_landCntryHandler && _landCntryHandler[0]) {
    _landCntryHandler[0].onChange();
  }

  if (_stateProvITHandler && _stateProvITHandler[0]) {
    _stateProvITHandler[0].onChange();
  }

  if (_salesRepHandlerIT && _salesRepHandlerIT[0]) {
    _salesRepHandlerIT[0].onChange();
  }

  if (_fiscalCodeUpdateHandlerIT && _fiscalCodeUpdateHandlerIT[0]) {
    // _fiscalCodeUpdateHandlerIT[0].onChange();
  }

  if (_custGrpIT && _custGrpIT[0]) {
    _custGrpIT[0].onChange();
  }

  var reqId = FormManager.getActualValue('reqId');
  if (reqType == 'C' && role == 'REQUESTER') {
    var checkImportIndc = getImportedIndcForItaly();
    // Story 1544496: Imported codes should not get overwritten
    if (checkImportIndc == 'Y') {
      console.log(">>importIndc is Y -Non editable fields- are ('clientTier', 'isuCd', 'repTeamMemberNo', 'salesBusOffCd','affiliate', 'specialTaxCd' , 'collectionCd','vat', 'taxCd1')");
      if (custSubType != undefined && custSubType != '' && (custSubType != '3PAIT' && custSubType != '3PASM' && custSubType != '3PAVA')) {
        FormManager.readOnly('isuCd');
        FormManager.readOnly('clientTier');
        // FormManager.readOnly('repTeamMemberNo');
        // FormManager.readOnly('salesBusOffCd');
        FormManager.readOnly('affiliate');
        FormManager.resetValidations('clientTier');
        FormManager.readOnly('inacCd');
      }
      if (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA') {
        FormManager.enable('inacCd');
      }
      FormManager.readOnly('identClient');

    } else {
      var custSubType = FormManager.getActualValue('custSubGrp');
      if (FormManager.getActualValue('isuCd') != null && "32" == FormManager.getActualValue('isuCd')) {
        if (custSubType == 'CROGO' || custSubType == 'CROUN' || custSubType == 'CROLC' || custSubType == 'CROCM' || custSubType == 'CROBP') {
          FormManager.setValue('collectionCd', 'CIT16');
          FormManager.readOnly('collectionCd');
        }
        if (custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'LOCSM' || custSubType == 'LOCVA' || custSubType == 'COMSM'
            || custSubType == 'COMVA' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'NGOSM' || custSubType == 'NGOVA'
            || custSubType == 'PRISM' || custSubType == 'PRIVA') {
          console.log("For SM/VA after config set default value of collection code CIT14");
          FormManager.setValue('collectionCd', 'CIT14');
          FormManager.readOnly('collectionCd');
        }
      }
    }
  }
  // CMR-1257
  if ('C' == FormManager.getActualValue('reqType')) {
    var _countryUse = '';
    var cntryRegion = FormManager.getActualValue('countryUse');
    var _landCntry = FormManager.getActualValue('landCntry');
    if (cntryRegion != '' && cntryRegion.length > 3) {
      _countryUse = cntryRegion.substring(3, 5);
    }
    if (_countryUse != '' && _landCntry != '' && (_landCntry == 'VA' || _landCntry == 'SM')) {
      FormManager.readOnly('landCntry');
    }
    if (_countryUse != '' && _landCntry != '' && _landCntry == 'VA') {
      FormManager.setValue('identClient', 'Y');
    }
  }

  if (role.toUpperCase() == 'REQUESTER' && (reqType == 'U' || reqType == 'X')) {
    var _countryUse = '';
    var cntryRegion = FormManager.getActualValue('countryUse');
    if (cntryRegion != '' && cntryRegion.length > 3) {
      _countryUse = cntryRegion.substring(3, 5);
    }
    if (_countryUse != '' && _countryUse == 'VA') {
      FormManager.readOnly('taxCd1');
      FormManager.readOnly('vat');
    }
  }
  addAfterTemplateLoadIT();
  // CMR-1363
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('cmrNo');
  }
}

/*
 * ITALY - sets Sales rep based on isuCTC
 */
var _importedSalesRep = '';
function setSalesRepValuesIT(isuCd, clientTier) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var checkImportIndc = getImportedIndcForItaly();

  if (checkImportIndc == 'Y' && _importedSalesRep == '') {
    _importedSalesRep = _pagemodel.repTeamMemberNo;
  }
  var custSubType = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var salesRep = FormManager.getActualValue('repTeamMemberNo');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var salesReps = [];
  if (clientTier != '') {
    qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCd + clientTier + '%',
    };
    results = cmr.query('GET.SRLIST.BYCTC', qParams);
    if (results != null && Object.keys(results).length > 0) {
      for (var i = 0; i < results.length; i++) {
        salesReps.push(results[i].ret1);
      }
      FormManager.enable('repTeamMemberNo');
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesReps);
      if (salesReps.length == 1) {
        FormManager.setValue('repTeamMemberNo', salesReps[0]);
      } else if (salesRep != null && salesRep != '') {
        FormManager.setValue('repTeamMemberNo', salesRep);
      }
    }
    if (custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN') {
      FormManager.setValue('isuCd', '21');
      FormManager.readOnly('clientTier', '');
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ '0B14A0', '0B1BA0', '0B1GA0', '0B1EA0', '0B11A0', '0B12A0', '0B13A0' ]);
    }
    if (custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP') {
      FormManager.setValue('isuCd', '21');
      FormManager.readOnly('clientTier', '');
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
      FormManager.enable('repTeamMemberNo');
      FormManager.setValue('repTeamMemberNo', '09ZPA0');
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ '09ZPA0', '09ZPB0' ]);
    }
    if (isuCd != '32' && clientTier != 'S'
        && !(custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP' || (custSubType == 'PRISM' && role == "REQUESTER"))) {
      FormManager.enable('repTeamMemberNo');
    } else if ((isuCd == '32' && clientTier == 'S') && (role == "REQUESTER") && (checkImportIndc == 'N')) {
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
      if (checkImportIndc == 'N') {
        autoSetSBOSROnPostalCode(false, false);
      } else {
        FormManager.setValue('repTeamMemberNo', _importedSalesRep);
        console.log("Kept imported sales rep value >>>" + _importedSalesRep);
      }
      FormManager.readOnly('repTeamMemberNo');
    } else if (isuCd != '32' && clientTier != 'S' && custSubType == 'PRISM' && role == "REQUESTER") {
      FormManager.readOnly('repTeamMemberNo');
    } else {
      FormManager.enable('repTeamMemberNo');
    }
  } else if (isuCd == '32') {
    FormManager.clearValue('repTeamMemberNo');
    FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
  }
  setAffiliateEnterpriseRequired();
}

function showHideCityStateProvIT() {
  console.log("--->>> showHideCityStateProvIT for IT ---<<<");
  var landCntryVal = FormManager.getActualValue('landCntry');
  var addrType = FormManager.getActualValue('addrType');

  if (landCntryVal != 'IT') {
    FormManager.show('City1', 'city1');
    FormManager.hide('StateProv', 'stateProv');
    FormManager.resetValidations('stateProv');
    FormManager.show('StateProvItaly', 'stateProvItaly');
    FormManager.readOnly('stateProvItaly');

    FormManager.addValidator('city1', Validators.REQUIRED, [ 'City' ], null);
    FormManager.setValue('crossbCntryStateProvMapIT', '');
    FormManager.setValue('crossbCntryStateProvMapIT', landCntryVal);
    FormManager.setValue('stateProvItaly', getDescription('crossbCntryStateProvMapIT'));

    var stateProvIT = FormManager.getActualValue('stateProvItaly');
    FormManager.setValue('crossbStateProvPostalMapIT', stateProvIT);
    FormManager.setValue('poBoxPostCd', getDescription('crossbStateProvPostalMapIT'));
  } else {
    FormManager.addValidator('dropDownCity', Validators.REQUIRED, [ 'City' ], null);
    FormManager.hide('StateProvItaly', 'stateProvItaly');
    FormManager.hide('StateProvItalyOth', 'stateProvItalyOth');
    FormManager.resetValidations('stateProvItaly');
    FormManager.resetValidations('stateProvItalyOth');
  }
  if (addrType == 'ZS01' || FormManager.getField('addrType_ZS01').checked) {
    FormManager.show('StateProv', 'stateProv');
  }
  if (landCntryVal == 'IT') {
    FormManager.show('StateProv', 'stateProv');
  } else {
    FormManager.hide('StateProv', 'stateProv');
  }
}

/*
 * Author: Dennis T Natad Purpose: Function for displayedValue retrieval in the
 * case of select objects and similar objects.
 */
function getDescription(fieldId) {
  var field = dijit.byId(fieldId);
  return field.displayedValue;
}

function showHideOtherStateProvIT() {
  console.log(">>> showHideOtherStateProvIT for IT");
  var stateProvIT = FormManager.getActualValue('stateProvItaly');
  var reqType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  var landCntryVal = FormManager.getActualValue('landCntry');
  // Defect :1474362, 1534200
  if (reqType == 'U' || reqType == 'X') {
    if (addrType == 'ZS01') {
      console.log(">>Hide stateProvItalyOth when installing address " + addrType);
      FormManager.hide('StateProvItalyOth', 'stateProvItalyOth');
    }
  }

  if (stateProvIT != 'OTH') {
    // hide the OTHER field
    FormManager.hide('StateProvItalyOth', 'stateProvItalyOth');
    FormManager.resetValidations('stateProvItalyOth');
    FormManager.setValue('crossbStateProvPostalMapIT', stateProvIT);
    // Defect 1452033 :Cross Boarder
    if (addrType == 'ZI01') {
      FormManager.setValue('poBoxPostCd', getDescription('crossbStateProvPostalMapIT'));
    }

  } else if (landCntryVal != 'IT' && addrType != 'ZS01') {
    console.log("show stateProvItalyOth for non ITand non installing address.");
    FormManager.show('StateProvItalyOth', 'stateProvItalyOth');
    FormManager.addValidator('stateProvItalyOth', Validators.REQUIRED, [ 'Other State/Province' ], null);
  } else {
    FormManager.resetValidations('stateProvItalyOth');
    FormManager.hide('StateProvItalyOth', 'stateProvItalyOth');
  }
}

function autoSetAbbrevNmOnChanageIT() {
  console.log("--->>> autoSetAbbrevNmOnChanageIT >> running");
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'C') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var _abbrevNmValue = null;
  var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
  _abbrevNmValue = result.ret1;

  if (_abbrevNmValue != null && _abbrevNmValue.length > 22) {
    _abbrevNmValue = _abbrevNmValue.substr(0, 22);
  }
  if (_abbrevNmValue == undefined) {
    FormManager.setValue('abbrevNm', '');
  } else {
    FormManager.setValue('abbrevNm', _abbrevNmValue);
  }
  console.log("AbbrevNM>>" + FormManager.getActualValue('abbrevNm'));
}

function autoSetAbbrevLocnOnChangeIT() {
  console.log(">>> autoSetAbbrevLocnOnChangeIT >> running");
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'C') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var _abbrevLocn = null;
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : _zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var _result = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', qParams);
  _abbrevLocn = _result.ret1;

  if (_abbrevLocn != null && _abbrevLocn.length > 12) {
    _abbrevLocn = _abbrevLocn.substr(0, 12);
  }
  if (_abbrevLocn == undefined) {
    FormManager.setValue('abbrevLocn', '');
  } else {
    FormManager.setValue('abbrevLocn', _abbrevLocn);
  }
  console.log("abbrevLocn>>" + FormManager.getActualValue('abbrevLocn'));
}
// CMR-2205
function autoSetAbbrevNmOnChanageTR() {
  console.log("--->>> autoSetAbbrevNmOnChanageTR >> running");
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var abbrName = FormManager.getActualValue("abbrevNm");
  if (abbrName != null && abbrName.length > 22) {
    abbrName = abbrName.substr(0, 22);
  }
  if (abbrName == undefined) {
    FormManager.setValue('abbrevNm', '');
  } else {
    FormManager.setValue('abbrevNm', abbrName);
  }
  console.log("AbbrevNM>>" + FormManager.getActualValue('abbrevNm'));
}

function autoSetAbbrevLocnOnChangeTR() {
  console.log(">>> autoSetAbbrevLocnOnChangeTR >> running");
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }

  var _abbrevLocn = FormManager.getActualValue('abbrevLocn');
  if (_abbrevLocn != null && _abbrevLocn.length > 12) {
    _abbrevLocn = _abbrevLocn.substr(0, 12);
  }
  if (_abbrevLocn == undefined) {
    FormManager.setValue('abbrevLocn', '');
  } else {
    FormManager.setValue('abbrevLocn', _abbrevLocn);
  }
  console.log("abbrevLocn>>" + FormManager.getActualValue('abbrevLocn'));
}

function updateAbbrLocWithZS01TR() {
  var _abbrevLocn = null;
  var addrType = FormManager.getActualValue('addrType');
  var reqType = FormManager.getActualValue('reqType');
  var isCross = true;
  if ("TR" == FormManager.getActualValue("landCntry")) {
    isCross = false;
  }
  if (("ZS01" != addrType && "ZP01" != addrType) || (!isCross && "ZP01" == addrType)) {
    return;
  }
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var newAddrCity = FormManager.getActualValue("city1");
  var newAddrLand = FormManager.getActualValue("landCntry");
  var qParams = {
    REQ_ID : _zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var _result = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', qParams);
  var oldAddrCity = _result.ret1;

  if (isCross) {
    _abbrevLocn = document.getElementById('landCntry').value;
    if (_abbrevLocn != null && _abbrevLocn.length > 12) {
      _abbrevLocn = _abbrevLocn.substr(0, 12);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);
  } else {
    if (newAddrCity != oldAddrCity) {
      if (newAddrCity != null && newAddrCity.length > 12) {
        newAddrCity = newAddrCity.substr(0, 12);
      }
      if (newAddrCity == undefined) {
        FormManager.setValue('abbrevLocn', '');
      } else {
        FormManager.setValue('abbrevLocn', newAddrCity);
      }
    }
  }
}

function updateAbbrNameWithZS01TR() {
  var reqType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  var abbrName = FormManager.getActualValue("abbrevNm");
  var newAddrName1 = FormManager.getActualValue("custNm1");
  if ("ZS01" != addrType) {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if ('PROCESSOR' == role) {
    var zs01ReqId = FormManager.getActualValue('reqId');
    var qParams = {
      REQ_ID : zs01ReqId,
    };
    var result = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', qParams);
    var oldAbbrName = result.ret1;

    if (abbrName != oldAbbrName) {
      return;
    }
  }

  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var oldAddrName = null;
  var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
  oldAddrName = result.ret1;

  if (oldAddrName != newAddrName1) {
    if (newAddrName1 != null && newAddrName1.length > 22) {
      newAddrName1 = newAddrName1.substr(0, 22);
    }
    if (newAddrName1 == undefined) {
      FormManager.setValue('abbrevNm', '');
    } else {
      FormManager.setValue('abbrevNm', newAddrName1);
    }
  }

}

/**
 * CMR-2093:Turkey - show CoF field for Update request only
 */
function showCommercialFinanced() {
  if (FormManager.getActualValue('reqType') != 'U' || FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
    FormManager.clearValue('commercialFinanced');
    FormManager.hide('CommercialFinanced', 'commercialFinanced');
  } else {
    FormManager.show('CommercialFinanced', 'commercialFinanced');
    // FormManager.limitDropdownValues(FormManager.getField('commercialFinanced'),
    // [
    // '','R', 'S', 'T' ]);
    // FormManager.show('CustClass', 'custClass');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER') {
      // FormManager.readOnly('custClass');
    } else {
      FormManager.enable('custClass');
    }
  }
}
function lockCmrOwner() {
  FormManager.setValue('cmrOwner', 'IBM');
  FormManager.readOnly('cmrOwner');
}
function autoSetSBOSROnPostalCode(clientTier, currPostCd) {
  var requestType = FormManager.getActualValue('reqType');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var isuCode = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  if (requestType != 'C') {
    return;
  }
  var reqId = FormManager.getActualValue('reqId');
  var result = cmr.query('VALIDATOR.POSTCODEIT', {
    REQID : reqId
  });

  var postCodeOrg = '';

  if (result != null && result.ret1 != undefined) {
    postCodeOrg = result.ret1;
  } else {
    postCodeOrg = '';
  }

  var postCode = parseInt(postCodeOrg.substring(0, 2));

  if (currPostCd && currPostCd != undefined && currPostCd != '' && currPostCd != postCodeOrg) {
    postCode = currPostCd.substring(0, 2);
  }

  // set collection code based on postalcode logic
  var checkImportIndc = getImportedIndcForItaly();
  if (checkImportIndc != 'Y') {
    if (postCodeOrg != '' && isuCode != '' && isuCode == '32' && ctc == 'S') {
      /*
       * if ((postCodeOrg == '55100')) { FormManager.setValue('collectionCd',
       * 'CIT03'); } else
       */if (postCode >= 00 && postCode <= 04) {
        FormManager.setValue('collectionCd', 'CIT14');
      } else if (postCode == 10 || postCode == 11 || postCode == 28) {
        FormManager.setValue('collectionCd', 'CIT04');
      } else if (postCode > 11 && postCode <= 19) {
        FormManager.setValue('collectionCd', 'CIT04');
      } else if ((postCode >= 21 && postCode <= 24) || postCode == 27) {
        FormManager.setValue('collectionCd', 'CIT02');
      } else if (postCode == 23 || postCode == 25 || postCode == 26) {
        FormManager.setValue('collectionCd', 'CIT02');
      } else if (postCode == 20) {
        FormManager.setValue('collectionCd', 'CIT16');
      } else if ((postCode >= 30 && postCode <= 35) || postCode == 45) {
        FormManager.setValue('collectionCd', 'CIT19');
      } else if ((postCode >= 36 && postCode <= 39) || postCode == 46) {
        FormManager.setValue('collectionCd', 'CIT19');
      } else if ((postCode >= 40 && postCode <= 44) || postCode == 29 || postCode == 47 || postCode == 48) {
        FormManager.setValue('collectionCd', 'CIT03');
      } else if (postCode >= 50 && postCode <= 59) {
        FormManager.setValue('collectionCd', 'CIT03');
      } else if (postCode == 60 || postCode == 61) {
        FormManager.setValue('collectionCd', 'CIT03');
      } else if (postCode == 05 || postCode == 06) {
        FormManager.setValue('collectionCd', 'CIT14');
      } else if ((postCode >= 62 && postCode <= 67) || (postCode >= 70 && postCode <= 76) || (postCode >= 85 && postCode <= 89) || (postCode >= 7 && postCode <= 9)) {
        FormManager.setValue('collectionCd', 'CIT14');
      } else if (postCode >= 80 && postCode <= 84) {
        FormManager.setValue('collectionCd', 'CIT14');
      } else if (postCode >= 90 && postCode <= 98) {
        FormManager.setValue('collectionCd', 'CIT14');
      }
    } else if (isuCode != '32') {
      FormManager.clearValue('collectionCd');
    }

    if (postCodeOrg != ''
        && isuCode != ''
        && isuCode == '32'
        && ctc == 'S'
        && (custSubType == 'COMME' || custSubType == '3PAIT' || custSubType == 'NGOIT' || custSubType == 'GOVST' || custSubType == 'LOCEN' || custSubType == 'PRICU' || custSubType == 'UNIVE'
            || custSubType == 'CROCM' || custSubType == 'CRO3P' || custSubType == 'CROLC' || custSubType == 'CROUN' || custSubType == 'CROGO' || custSubType == 'CROPR')) {
      /*
       * if ((postCodeOrg == '55100')) { FormManager.setValue('repTeamMemberNo',
       * '09NAMM'); FormManager.setValue('salesBusOffCd', 'NA'); } else
       */if (postCode >= 00 && postCode <= 04) {
        FormManager.setValue('repTeamMemberNo', '09NCMM');
        FormManager.setValue('salesBusOffCd', 'NC');
      } else if (postCode == 10 || postCode == 11 || postCode == 28) {
        FormManager.setValue('repTeamMemberNo', '09NBMM');
        FormManager.setValue('salesBusOffCd', 'NB');
      } else if (postCode > 11 && postCode <= 19) {
        FormManager.setValue('repTeamMemberNo', '09GEMM');
        FormManager.setValue('salesBusOffCd', 'GE');
      } else if ((postCode >= 21 && postCode <= 24) || postCode == 27) {
        FormManager.setValue('repTeamMemberNo', '09NLMM');
        FormManager.setValue('salesBusOffCd', 'NL');
      } else if (postCode == 23 || postCode == 25 || postCode == 26) {
        FormManager.setValue('repTeamMemberNo', '09GJMM');
        FormManager.setValue('salesBusOffCd', 'GJ');
      } else if (postCode == 20) {
        FormManager.setValue('repTeamMemberNo', '09GHMM');
        FormManager.setValue('salesBusOffCd', 'GH');
      } else if ((postCode >= 30 && postCode <= 35) || postCode == 45) {
        FormManager.setValue('repTeamMemberNo', '09NFMM');
        FormManager.setValue('salesBusOffCd', 'NF');
      } else if ((postCode >= 36 && postCode <= 39) || postCode == 46) {
        FormManager.setValue('repTeamMemberNo', '09GKMM');
        FormManager.setValue('salesBusOffCd', 'GK');
      } else if ((postCode >= 40 && postCode <= 44) || postCode == 29 || postCode == 47 || postCode == 48) {
        FormManager.setValue('repTeamMemberNo', '09NIMM');
        FormManager.setValue('salesBusOffCd', 'NI');
      } else if ((postCode >= 50 && postCode <= 59) || (postCode == 61)) {
        FormManager.setValue('repTeamMemberNo', '09RPMM');
        FormManager.setValue('salesBusOffCd', 'RP');
      } else if (postCode == 60) {
        FormManager.setValue('repTeamMemberNo', '09CPMM');
        FormManager.setValue('salesBusOffCd', 'CP');
      } else if (postCode == 05 || postCode == 06) {
        FormManager.setValue('repTeamMemberNo', '09NAMM');
        FormManager.setValue('salesBusOffCd', 'NA');
      } else if ((postCode >= 62 && postCode <= 67) || (postCode >= 70 && postCode <= 76) || (postCode >= 85 && postCode <= 89) || (postCode >= 7 && postCode <= 9)) {
        FormManager.setValue('repTeamMemberNo', '09CPMM');
        FormManager.setValue('salesBusOffCd', 'CP');
      } else if (postCode >= 80 && postCode <= 84) {
        FormManager.setValue('repTeamMemberNo', '09NGMM');
        FormManager.setValue('salesBusOffCd', 'NG');
      } else if (postCode >= 90 && postCode <= 98) {
        FormManager.setValue('repTeamMemberNo', '09NMMM');
        FormManager.setValue('salesBusOffCd', 'NM');
      }
    }

    // CMR-1496 - set specific SR/SBO for VA/SM fresh creates
    var countryUse = FormManager.getActualValue('countryUse');
    if (countryUse && isuCode == '32' && ctc == 'S') {
      if (countryUse == '758VA') {
        FormManager.setValue('repTeamMemberNo', '09NCMM');
        FormManager.setValue('salesBusOffCd', 'NC');
      }
      if (countryUse == '758SM') {
        FormManager.setValue('repTeamMemberNo', '09NIMM');
        FormManager.setValue('salesBusOffCd', 'NI');
      }
    }
  }
}

function addCrossBorderValidatorForIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        // only Create type will be validated
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }

        var scenario = FormManager.getActualValue('custGrp');
        if (scenario != null && scenario.includes('CRO')) {
          scenario = 'CROSS';
        }

        var cntryRegion = FormManager.getActualValue('countryUse');
        var landCntry = 'IT'; // default to Italy
        if (cntryRegion != '' && cntryRegion.length > 3) {
          landCntry = cntryRegion.substring(3, 5);
        }

        var reqId = FormManager.getActualValue('reqId');
        var defaultLandCntry = landCntry;
        var result = cmr.query('VALIDATOR.CROSSBORDERIT', {
          REQID : reqId
        });
        if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Bill-to (Billing) Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS'
            && (cntryRegion.substring(3, 5) != 'SM' || cntryRegion.substring(3, 5) != 'VA')) {
          return new ValidationResult(null, false, 'Landed Country value of the Bill-to (Billing) Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addPhoneValidatorEMEA() {
  if (FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
  }
}
function addPOBOXValidatorEMEA() {
  FormManager.addValidator('poBox', Validators.DIGIT, [ 'PO Box' ]);
}
function autoPopulateIdentClientIT() {
  /*
   * var cntryWhereVATIsMandatory = [ 'AT', 'BE', 'BG', 'HR', 'CY', 'CZ', 'EG',
   * 'FR', 'DE', 'GR', 'HU', 'IE', 'IL', 'IT', 'LU', 'MT', 'NL', 'PK', 'PL',
   * 'PT', 'RO', 'RU', 'RS', 'SK', 'SI', 'ZA', 'ES', 'UA', 'GB', 'TR', 'LV',
   * 'LT', 'DK', 'EE', 'FI', 'SE' ];
   */
  var reqType = FormManager.getActualValue('reqType');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');
  var identClientValuesCross = [ 'N', 'Y' ];
  var identClientValuesLocal = [ 'A', 'B', 'C', 'D', 'X' ];
  // only Create type will be validated
  if (reqType != 'C') {
    return new ValidationResult(null, true);
  }

  var scenario = FormManager.getActualValue('custGrp');
  if (scenario != null && scenario.includes('CRO')) {
    scenario = 'CROSS';
  }

  var checkImportIndc = getImportedIndcForItaly();

  var cntryRegion = FormManager.getActualValue('countryUse');
  var landCntry = 'IT'; // default to Italy
  if (cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry == 'SM' || landCntry == 'VA') {
    FormManager.setValue('identClient', 'N');
    FormManager.readOnly('identClient');
    FormManager.resetValidations('vat');
    FormManager.setValue('vat', '');
    FormManager.readOnly('vat');
    // FormManager.resetValidations('taxCd1');
    if (landCntry == 'VA') {
      FormManager.resetValidations('taxCd1');
      FormManager.setValue('taxCd1', '');
      FormManager.readOnly('taxCd1');
      FormManager.setValue('identClient', 'Y');
    }
    return;
  }

  if (scenario == 'CROSS') {
    FormManager.resetValidations('vat');
    FormManager.setValue('vat', '');
    FormManager.readOnly('vat');
    FormManager.resetValidations('taxCd1');
    FormManager.limitDropdownValues(FormManager.getField('identClient'), identClientValuesCross);
  } else if (scenario == 'LOCAL' && checkImportIndc != 'Y') {
    if (custSubType == 'COMME') {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'A', 'D' ]);
    } else if (custSubType == 'BUSPR') {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'A' ]);
    } else if (custSubType == 'PRICU') {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'X' ]);
    } else if (custSubType == 'LOCEN' || custSubType == 'UNIVE' || custSubType == 'GOVST') {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'B', 'C' ]);
    } else if (custSubType == 'NGOIT') {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'B' ]);
    } else {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), identClientValuesLocal);
    }
  } else if (scenario == 'LOCAL' && checkImportIndc == 'Y') {
    FormManager.limitDropdownValues(FormManager.getField('identClient'), identClientValuesLocal);
  } else {
    FormManager.resetDropdownValues(FormManager.getField('identClient'));
  }

  var result = cmr.query('VALIDATOR.CROSSBORDERIT', {
    REQID : reqId
  });

  /*
   * if (result != null && result.ret1 != '' && result.ret1 != undefined &&
   * cntryWhereVATIsMandatory.includes(result.ret1) && scenario == 'CROSS') {
   * FormManager.setValue('identClient', 'N'); } else if (result != null &&
   * result.ret1 != '' && result.ret1 != undefined &&
   * !cntryWhereVATIsMandatory.includes(result.ret1) && scenario == 'CROSS') {
   * FormManager.setValue('identClient', 'Y'); }
   */
  // Defect 1462395: Ident Client :Mukesh
  var custSubType = FormManager.getActualValue('custSubGrp');
  var fiscalCode = FormManager.getActualValue('taxCd1');
  var vat = FormManager.getActualValue('vat');
  if (custSubType != undefined && custSubType == 'LOCEN' && checkImportIndc != 'Y') {
    if ((fiscalCode != '' && fiscalCode.length == 11) && vat == '') {
      FormManager.setValue('identClient', 'B');
    }
    if ((fiscalCode != '' && fiscalCode.length == 11) && (vat != '' && vat.length == 13)) {
      FormManager.setValue('identClient', 'C');
    }
  }

  if (custSubType != undefined && custSubType == 'COMME' && checkImportIndc != 'Y') {
    if ((fiscalCode != '' && fiscalCode.length == 11) && (vat != '' && vat.length == 13)) {
      FormManager.setValue('identClient', 'A');
    }
    if ((fiscalCode != '' && fiscalCode.length == 16) && (vat != '' && vat.length == 13)) {
      FormManager.setValue('identClient', 'D');
    }
  }
}

/*
 * function autoSetIdentClientForBillingIT() { var reqType =
 * FormManager.getActualValue('reqType'); if (reqType != 'C') { return new
 * ValidationResult(null, true); } var addrType =
 * FormManager.getActualValue('addrType'); var scenario =
 * FormManager.getActualValue('custGrp'); if (scenario != null &&
 * scenario.includes('CRO')) { scenario = 'CROSS'; } if (addrType != null &&
 * addrType == 'ZP01' && scenario != null && scenario != '' && scenario ==
 * 'CROSS') { autoPopulateIdentClientIT(); } }
 */

function addCompanyAddrValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var zi01ReqId = FormManager.getActualValue('reqId');
        qParams = {
          REQ_ID : zi01ReqId,
        };
        var record = cmr.query('GETZI01VALRECORDS', qParams);
        var zi01Reccount = record.ret1;
        if (Number(zi01Reccount) > 1) {
          return new ValidationResult(null, false, 'Only one Company Address can be defined.');
        } else if (Number(zi01Reccount == 0)) {
          return new ValidationResult(null, false, 'At least one Company Address must be defined.');
        } else {
          return new ValidationResult(null, true);
        }

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addBillingAddrValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var zp01ReqId = FormManager.getActualValue('reqId');
        qParams = {
          REQ_ID : zp01ReqId,
        };
        var record = cmr.query('GETZP01VALRECORDS', qParams);
        var zp01Reccount = record.ret1;
        if (Number(zp01Reccount) > 1) {
          return new ValidationResult(null, false, 'Only one Billing Address can be defined.');
        } else if (Number(zp01Reccount == 0)) {
          return new ValidationResult(null, false, 'At least one Billing Address must be defined.');
        } else {
          return new ValidationResult(null, true);
        }

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setVATForItaly() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  // only Create type will be validated
  if (reqType != 'C') {
    return new ValidationResult(null, true);

  }
  var ident = FormManager.getActualValue('identClient');
  var reqId = FormManager.getActualValue('reqId');
  /*
   * var euCntryList = [ 'AT', 'IT', 'BE', 'LV', 'BG', 'LT', 'HR', 'LU', 'CY',
   * 'MT', 'CZ', 'NL', 'DK', 'PL', 'EE', 'PT', 'FI', 'RO', 'FR', 'DE', 'SK',
   * 'SI', 'GR', 'ES', 'HU', 'SE', 'IE', 'GB' ]; var reqId =
   * FormManager.getActualValue('reqId'); var result =
   * cmr.query('VALIDATOR.CROSSBORDERIT', { REQID : reqId });
   * 
   * if (result != null && result != '' && result != undefined && result.ret1 !=
   * undefined && euCntryList.includes(result.ret1) && role == 'REQUESTER' &&
   * ident!='' && (ident!='N' || ident!='Y')) { FormManager.addValidator('vat',
   * Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB'); } else if (result != null &&
   * result != undefined && result.ret1 != undefined && result != '' &&
   * !euCntryList.includes(result.ret1) && role == 'REQUESTER') {
   * FormManager.resetValidations('vat'); FormManager.setValue('vat', '');
   * FormManager.readOnly('vat'); } else { FormManager.enable('vat'); }
   */
  // Defect 1475328: Scenarios: Local Enterprise, University, Government : VAT
  // is mandatory :Mukesh
  var checkImportIndc = getImportedIndcForItaly();

  var custSubType = FormManager.getActualValue('custSubGrp');
  if ((custSubType == 'LOCEN' || custSubType == 'UNIVE' || custSubType == 'GOVST') && ident != 'C' && checkImportIndc == 'N') {
    FormManager.resetValidations('vat');
    FormManager.enable('vat');
  }
  // Added new changs in defect :1475328
  if ((role == 'REQUESTER') && (custSubType == 'COMME' || custSubType == 'BUSPR') && checkImportIndc == 'N') {
    FormManager.enable('vat');
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
  }
  if ((role == 'PROCESSOR') && (custSubType == 'COMME' || custSubType == 'BUSPR') && checkImportIndc == 'N') {
    FormManager.resetValidations('vat');
    FormManager.enable('vat');
  }
  if ((custSubType == 'PRICU' || custSubType == 'PRISM' || custSubType == 'PRIVA') && role == 'REQUESTER' && checkImportIndc == 'N') { // Defect
    // 1496242
    FormManager.resetValidations('vat');
    FormManager.setValue('vat', '');
    FormManager.readOnly('vat');
  }
}

/*
 * function autoSetVATForBilling() { var reqType =
 * FormManager.getActualValue('reqType'); if (reqType != 'C') { return new
 * ValidationResult(null, true); } var addrType =
 * FormManager.getActualValue('addrType'); if (addrType != null && addrType ==
 * 'ZP01') { setVATForItaly(); } }
 */
/* 1430539 - do not allow delete of imported addresses on update requests */

function canRemoveAddress(value, rowIndex, grid) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry != '758') {
    var rowData = grid.getItem(rowIndex);
    var importInd = rowData.importInd[0];
    var reqType = FormManager.getActualValue('reqType');
    if ('U' == reqType && 'Y' == importInd) {
      return false;
    }
    return true;
  } else {
    var type = FormManager.getActualValue('reqType');
    if ('C' == type) {
      var rowData = grid.getItem(rowIndex);
      // var importInd = rowData.importInd[0];
      var addrType = rowData.addrType[0];
      var ifProspect = FormManager.getActualValue('prospLegalInd');
      if (dijit.byId('prospLegalInd')) {
        ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
      }
      if (ifProspect == 'Y') {
        return (addrType != 'ZI01');
      } else {
        return ((addrType != 'ZI01') && (ifProspect != 'Y'));
      }
    } else {
      // no allowing to remove for updates
      return false;
    }
  }
}

function canUpdateAddress(value, rowIndex, grid) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == '758') {
    var rowData = grid.getItem(rowIndex);
    var importInd = rowData.importInd[0];
    var type = FormManager.getActualValue('reqType');
    var ifProspect = FormManager.getActualValue('prospLegalInd');
    if (dijit.byId('prospLegalInd')) {
      ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
    }
    if ('C' == type) {
      if (ifProspect == 'Y') {
        return true;
      } else {
        return ((importInd != 'Y') && (ifProspect != 'Y'));
      }
    } else {
      var cmrNo = FormManager.getActualValue('cmrNo');
      var company = FormManager.getActualValue('company');
      if (cmrNo == company) {
        return true;
      } else {
        var addrType = rowData.addrType[0];
        var currParCMR = rowData.parCmrNo[0];
        if (currParCMR == cmrNo) {
          return true;
        } else {
          return false;
        }
      }
    }
  } else {
    return true;
  }
}
// Defect 1509289 :Mukesh
function canCopyAddress(value, rowIndex, grid) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (cntry == '726') {
    return shouldShowCopyAddressInGrid(rowIndex, grid);
  }

  if (cntry != '758') {
    return true;
  }
  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 2) {
    return false;
  }
  return true;
}

function shouldShowCopyAddressInGrid(rowIndex, grid) {
  if (grid != null && rowIndex != null && grid.getItem(rowIndex) != null) {
    if (grid.getItem(rowIndex).addrType[0] == 'ZP01' && grid.getItem(rowIndex).landCntry[0] == 'GR') {
      return false;
    }
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  return canRemoveAddress(value, rowIndex, grid);
}

function autoSetSBOSRForCompany() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return new ValidationResult(null, true);
  }
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != null && addrType == 'ZS01') {
    var currPostCd = FormManager.getActualValue('postCd');
    autoSetSBOSROnPostalCode(false, currPostCd);
  }
}

function disableHideFieldsOnAddrIT() {
  var addrType = FormManager.getActualValue('addrType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var landCntryVal = FormManager.getActualValue('landCntry');

  // Story 1377871 -added Postal Address in case of Billing address :Mukesh
  /*
   * if (addrType != '' && addrType == 'ZP01') {
   * FormManager.show('BillingPstlAddr', 'billingPstlAddr'); } else {
   * FormManager.clearValue('billingPstlAddr');
   * FormManager.hide('BillingPstlAddr', 'billingPstlAddr'); }
   */

  FormManager.setValue('poBoxPostCd', getDescription('crossbStateProvPostalMapIT'));

  /*
   * if (addrType != 'ZI01' && !FormManager.getField('addrType_ZI01').checked &&
   * cmr.addressMode == 'newAddress') { console.log("AddrType other than ZI01");
   * FormManager.setValue('postCd', ''); // for addressMode=newAddress only }
   */

  /*
   * if (addrType != '' && (FormManager.getField('addrType_ZS01').checked ||
   * addrType == 'ZS01')) { // FormManager.hide('StateProv', 'stateProv');
   * FormManager.resetValidations('stateProv');
   * FormManager.hide('StateProvItaly', 'stateProvItaly');
   * FormManager.resetValidations('stateProvItaly'); } else if (addrType != '' &&
   * !FormManager.getField('addrType_ZS01').checked && addrType != 'ZS01') {
   */
  if (landCntryVal == 'IT') {
    FormManager.show('StateProv', 'stateProv');
  }
  /*
   * if (landCntryVal != 'IT') { FormManager.show('StateProvItaly',
   * 'stateProvItaly'); }
   */
  // }
  if (addrType != '' && FormManager.getField('addrType_ZP01').checked && role == 'REQUESTER') {
    FormManager.show('StreetAbbrev', 'streetAbbrev');
    FormManager.show('AddrAbbrevName', 'addrAbbrevName');
    FormManager.show('AddrAbbrevLocn', 'addrAbbrevLocn');
    if (cmr.addressMode == 'newAddress') {
      FormManager.clearValue('addrAbbrevName');
      FormManager.clearValue('streetAbbrev');
    }
    FormManager.readOnly('streetAbbrev');
    FormManager.readOnly('addrAbbrevName');
    FormManager.readOnly('addrAbbrevLocn');

  } else if (addrType != '' && FormManager.getField('addrType_ZP01').checked && role == 'PROCESSOR') {
    FormManager.show('StreetAbbrev', 'streetAbbrev');
    FormManager.show('AddrAbbrevName', 'addrAbbrevName');
    FormManager.show('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.enable('streetAbbrev');
    FormManager.enable('addrAbbrevName');
    FormManager.enable('addrAbbrevLocn');
  } else if (addrType != '' && !FormManager.getField('addrType_ZP01').checked) {
    FormManager.hide('StreetAbbrev', 'streetAbbrev');
    FormManager.hide('AddrAbbrevName', 'addrAbbrevName');
    FormManager.hide('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.resetValidations('streetAbbrev');
    FormManager.resetValidations('addrAbbrevName');
    FormManager.resetValidations('addrAbbrevLocn');
  } else {
    FormManager.hide('StreetAbbrev', 'streetAbbrev');
    FormManager.hide('AddrAbbrevName', 'addrAbbrevName');
    FormManager.hide('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.resetValidations('streetAbbrev');
    FormManager.resetValidations('addrAbbrevName');
    FormManager.resetValidations('addrAbbrevLocn');
  }
}

function autoSetAddrFieldsForIT() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    FormManager.hide('StreetAbbrev', 'streetAbbrev');
    FormManager.hide('AddrAbbrevName', 'addrAbbrevName');
    FormManager.hide('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.resetValidations('streetAbbrev');
    FormManager.resetValidations('addrAbbrevName');
    FormManager.resetValidations('addrAbbrevLocn');
    for (var i = 0; i < _addrTypesForIT.length; i++) {
      _addrTypeITHandler[i] = null;
      if (_addrTypeITHandler[i] == null) {
        _addrTypeITHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForIT[i]), 'onClick', function(value) {
          disableHideFieldsOnAddrIT();
        });
      }
    }

  }

  if (cmr.addressMode == 'updateAddress' && FormManager.getActualValue('addrType') == 'ZP01' && role == 'REQUESTER') {
    FormManager.show('StreetAbbrev', 'streetAbbrev');
    FormManager.show('AddrAbbrevName', 'addrAbbrevName');
    FormManager.show('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.readOnly('streetAbbrev');
    FormManager.readOnly('addrAbbrevName');
    FormManager.readOnly('addrAbbrevLocn');
  }

  if (cmr.addressMode == 'updateAddress' && FormManager.getActualValue('addrType') == 'ZP01' && role == 'PROCESSOR') {
    FormManager.show('StreetAbbrev', 'streetAbbrev');
    FormManager.show('AddrAbbrevName', 'addrAbbrevName');
    FormManager.show('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.enable('streetAbbrev');
    FormManager.enable('addrAbbrevName');
    FormManager.enable('addrAbbrevLocn');
  }

  if (cmr.addressMode == 'updateAddress' && FormManager.getActualValue('addrType') != 'ZP01') {
    FormManager.hide('StreetAbbrev', 'streetAbbrev');
    FormManager.hide('AddrAbbrevName', 'addrAbbrevName');
    FormManager.hide('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.resetValidations('streetAbbrev');
    FormManager.resetValidations('addrAbbrevName');
    FormManager.resetValidations('addrAbbrevLocn');
  }
}

function setStreetAbbrevIT() {
  var reqType = FormManager.getActualValue('reqType');
  /*
   * if (reqType != 'C') { return new ValidationResult(null, true); }
   */
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != null && (addrType == 'ZP01' || FormManager.getField('addrType_ZP01').checked)) {
    FormManager.setValue('streetAbbrev', FormManager.getActualValue('addrTxt').substring(0, 18));
  }
}

function setAddrAbbrevNameIT() {
  var reqType = FormManager.getActualValue('reqType');
  /*
   * if (reqType != 'C') { return new ValidationResult(null, true); }
   */
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != null && (addrType == 'ZP01' || FormManager.getField('addrType_ZP01').checked)) {
    FormManager.setValue('addrAbbrevName', FormManager.getActualValue('custNm1').substring(0, 22));
  }
}

function setAddrAbbrevLocnIT() {
  var reqType = FormManager.getActualValue('reqType');
  /*
   * if (reqType != 'C') { return new ValidationResult(null, true); }
   */
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != null && (addrType == 'ZP01' || FormManager.getField('addrType_ZP01').checked)) {
    FormManager.setValue('addrAbbrevLocn', FormManager.getActualValue('city1').substring(0, 12));
  }
}

function addressLoadHandlerIT(cntry, addressMode, saving, afterValidate) {
  if (saving) {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  /*
   * if (reqType != 'C') { return new ValidationResult(null, true); }
   */

  if (_custNameITHandler == null) {
    _custNameITHandler = dojo.connect(FormManager.getField('custNm1'), 'onChange', function(value) {
      setAddrAbbrevNameIT();
    });
  }

  if (_custNameITHandler && _custNameITHandler[0]) {
    // _custNameITHandler[0].onChange();
  }

  if (_cityITHandler == null) {
    _cityITHandler = dojo.connect(FormManager.getField('city1'), 'onChange', function(value) {
      setAddrAbbrevLocnIT();
    });
  }

  if (_cityITHandler && _cityITHandler[0]) {
    // _cityITHandler[0].onChange();
  }

  if (_streetITHandler == null) {
    _streetITHandler = dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {
      setStreetAbbrevIT();
    });
  }

  if (_streetITHandler && _streetITHandler[0]) {
    // _streetITHandler[0].onChange();
  }
  /*
   * if (_dropDownCityHandler == null) { _dropDownCityHandler =
   * dojo.connect(FormManager.getField('dropDownCity'), 'onChange',
   * function(value) { setAddrAbbrevLocnIT(); }); }
   * 
   * if (_dropDownCityHandler && _dropDownCityHandler[0]) {
   * _dropDownCityHandler[0].onChange(); }
   */
}
/* End 1430539 */

/**
 * Override to not format for greece and cyprus
 */
function streetValueFormatter(value, rowIndex) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == '726' || cntry == '666') {
    return value;
  }
  var rowData = this.grid.getItem(rowIndex);
  var streetCont = rowData.addrTxt2;
  if (streetCont && streetCont[0]) {
    return value + '<br>' + streetCont;
  }
  return value;
}
// Defect 1459920: Enterprise number, Affiliate number :Mukesh
function setAffiliateEnterpriseRequired() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');
  if (FormManager.getActualValue('reqType') == 'C') {
    var checkImportIndc = getImportedIndcForItaly();
    if (checkImportIndc != 'Y') {
      var isu = FormManager.getActualValue('isuCd');
      FormManager.resetValidations('enterprise');
      FormManager.resetValidations('affiliate');
      FormManager.resetValidations('inacCd');
      if (custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN' || custSubType == 'PRISM' || custSubType == 'PRICU' || custSubType == 'PRIVA'
          || custSubType == 'CROPR') {
        FormManager.clearValue('enterprise');
        FormManager.clearValue('affiliate');
        FormManager.clearValue('inacCd');
        FormManager.readOnly('enterprise');
        FormManager.readOnly('affiliate');
      } else {
        if (isu == '21') {
          FormManager.clearValue('affiliate');
          FormManager.clearValue('inacCd');
          FormManager.enable('enterprise');
          FormManager.readOnly('affiliate');
          FormManager.readOnly('inacCd');
        } else if (isu == '32') {
          FormManager.enable('enterprise');
          FormManager.enable('affiliate');
          FormManager.enable('inacCd');
        } else {
          FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise Number/ CODICE ENTERPRISE' ], 'MAIN_IBM_TAB');
          FormManager.addValidator('affiliate', Validators.REQUIRED, [ 'Affiliate Number' ], 'MAIN_IBM_TAB');
          FormManager.enable('enterprise');
          FormManager.enable('affiliate');
          FormManager.enable('inacCd');
        }
      }
    } else if ('C' == FormManager.getActualValue('reqType') && checkImportIndc == 'Y') {
      if (custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN' || custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA'
          || custSubType == 'CROBP') {
        FormManager.clearValue('inacCd');
        FormManager.clearValue('affiliate');
        FormManager.readOnly('inacCd');
        FormManager.readOnly('affiliate');
      } else if (role == "REQUESTER" && (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CRO3P')) {
        FormManager.enable('inacCd');
        FormManager.enable('affiliate');
        FormManager.readOnly('repTeamMemberNo');
      } else if (role == "REQUESTER" && !(custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CRO3P')) {
        FormManager.readOnly('inacCd');
        // FormManager.readOnly('affiliate');
      } else {
        FormManager.enable('inacCd');
        FormManager.enable('affiliate');
      }
      if ((role != "REQUESTER") && (custSubType == 'PRISM' || custSubType == 'PRICU' || custSubType == 'PRIVA' || custSubType == 'CROPR')) {
        FormManager.enable('isuCd');
        FormManager.enable('clientTier');
      }
      if (role == "REQUESTER" && !(custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN')) {
        FormManager.readOnly('repTeamMemberNo');
      }
    }
  }
}

function getOldValuesIT(fromAddress, scenario, scenarioChanged) {
  if (scenarioChanged) {
    var custSubType = scenario;
    var reqId = FormManager.getActualValue('reqId');
    var checkImportIndc = getImportedIndcForItaly();
    if (_oldISUIT == "" && _oldCTCIT == "") {
      var result = cmr.query("GET.CMRINFO.IMPORTED", {
        REQ_ID : reqId
      });
      if (result != null && result.ret1 != '' && result.ret1 != null) {
        _oldISUIT = result.ret1;
        _oldCTCIT = result.ret2;
        _oldSalesRepIT = result.ret3;
        _oldSBOIT = result.ret4;
        _oldINACIT = result.ret6;
        _oldSpecialTaxCdIT = result.ret7;
        _oldAffiliateIT = result.ret8;
      }
    }

    // Story 1594125: Company number field should be locked
    FormManager.readOnly('company');
    // Defect 1553451
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (FormManager.getActualValue('reqType') == 'C' && role == 'REQUESTER') {
      if ((custSubType != undefined && FormManager.getActualValue('isuCd') != undefined)
          && (custSubType == 'COMME' || custSubType == 'COMSM' || custSubType == 'COMVA' || custSubType == 'CROCM' || custSubType == 'NGOIT' || custSubType == 'NGOSM' || custSubType == 'NGOVA'
              || custSubType == 'PRICU' || custSubType == 'PRISM' || custSubType == 'PRIVA') && (FormManager.getActualValue('isuCd') == '32') && FormManager.getField('clientTier')) {
        FormManager.readOnly('repTeamMemberNo');
        FormManager.readOnly('salesBusOffCd');
      }
      // Defect 1556300 (Old Defect 1526869)
      if (custSubType == 'GOVST' || custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'CROGO' || custSubType == 'LOCEN' || custSubType == 'LOCSM' || custSubType == 'LOCVA'
          || custSubType == 'CROLC' || custSubType == 'UNIVE' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'CROUN') {
        FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
      } else {
        FormManager.resetValidations('repTeamMemberNo');
      }

      if (FormManager.getActualValue('isuCd') != null
          && "32" != FormManager.getActualValue('isuCd')
          && (custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'CROGO' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'CROUN' || custSubType == 'LOCSM'
              || custSubType == 'LOCVA' || custSubType == 'CROLC' || custSubType == 'COMSM' || custSubType == 'COMVA' || custSubType == 'CROCM' || custSubType == 'BUSSM' || custSubType == 'BUSVA'
              || custSubType == 'CROBP' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'NGOSM' || custSubType == 'NGOVA' || custSubType == 'PRISM' || custSubType == 'PRIVA')) {
        FormManager.setValue('collectionCd', '');
        FormManager.enable('collectionCd');
        console.log("isu!=32 , collection code -blank and locked for the requester for CB/SM or VA ");
      }

      // Story 1544496: Imported codes should not get overwritten
      if (checkImportIndc == 'Y') {
        console
            .log(">getOldValuesIT> importIndc is Y -Non editable fields- are ('clientTier', 'isuCd', 'repTeamMemberNo', 'salesBusOffCd', 'inacCd','affiliate', 'specialTaxCd' , 'collectionCd','vat', 'taxCd1')");
        if (custSubType != undefined && custSubType != '' && (custSubType != '3PAIT' && custSubType != '3PASM' && custSubType != '3PAVA')) {
          FormManager.readOnly('isuCd');
          FormManager.readOnly('clientTier');
          FormManager.readOnly('affiliate');
          FormManager.resetValidations('clientTier');
          FormManager.readOnly('inacCd');
        }
        if (FormManager.getActualValue('isuCd') != null && "32" == FormManager.getActualValue('isuCd')) {
          if (custSubType == 'CROGO' || custSubType == 'CROUN' || custSubType == 'CROLC' || custSubType == 'CROCM' || custSubType == 'CROBP') {
            // console.log("For CB getOld set default value of collection code
            // CIT16");
            FormManager.setValue('collectionCd', 'CIT16');
            FormManager.readOnly('collectionCd');
          }
          if (custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'LOCSM' || custSubType == 'LOCVA' || custSubType == 'COMSM'
              || custSubType == 'COMVA' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'NGOSM' || custSubType == 'NGOVA'
              || custSubType == 'PRISM' || custSubType == 'PRIVA') {
            console.log("For SM/VA getOld set default value of collection code CIT14");
            FormManager.setValue('collectionCd', 'CIT14');
            FormManager.readOnly('collectionCd');
          }
        }
      }
    }
    if (checkImportIndc == 'Y') {
      if (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CRO3P') {
        if (custSubType != 'CRO3P') {
          FormManager.enable('inacCd');
        }
        FormManager.enable('isuCd');
        FormManager.enable('clientTier');
        FormManager.enable('specialTaxCd');
      }
      FormManager.readOnly('identClient');
      // if imported and scenario is 3rd person
      if (!(custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN' || custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP')) {
        FormManager.setValue('isuCd', _oldISUIT);
        FormManager.setValue('clientTier', _oldCTCIT);
        FormManager.setValue('repTeamMemberNo', _oldSalesRepIT);
        FormManager.setValue('salesBusOffCd', _oldSBOIT);
        FormManager.setValue('inacCd', _oldINACIT);
        FormManager.setValue('affiliate', _oldAffiliateIT);
      } else {
        FormManager.clearValue('affiliate');
      }
      FormManager.setValue('specialTaxCd', _oldSpecialTaxCdIT);
    }
  }
}

// Blanking Out Collection Code Based On ISU and CTC and Scenario
function blankedOutCollectionCD() {
  console.log("---->> blankedOutCollectionCD. <<----");
  // var custSubType = FormManager.getActualValue('custSubGrp');
  // var role = FormManager.getActualValue('userRole').toUpperCase();
  var checkImportIndc = getImportedIndcForItaly();
  var reqType = FormManager.getActualValue('reqType');
  var isuCd = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  if (checkImportIndc != 'Y' && reqType == 'C') {
    FormManager.resetValidations('collectionCd');
    if (isuCd == '32' && ctc == 'S') {
      FormManager.readOnly('collectionCd');
      autoSetSBOSROnPostalCode();
    } else {
      FormManager.enable('collectionCd');
      FormManager.clearValue('collectionCd');
    }
  }
}

function setMRCOnScenariosIT() {
  var requestType = FormManager.getActualValue('reqType');
  if (requestType != 'C') {
    return new ValidationResult(null, true);
  }
  var custSubType = FormManager.getActualValue('custSubGrp');
  var isuCode = FormManager.getActualValue('isuCd');
  if (custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP') {
    FormManager.setValue('mrcCd', '5');
  } else if (custSubType != null && isuCode != null && isuCode == '34' && (custSubType == 'COMME' || custSubType == 'COMSM' || custSubType == 'COMVA' || custSubType == 'CROCM')) {
    FormManager.setValue('mrcCd', 'M');
  } else {
    FormManager.setValue('mrcCd', '2');
  }
}
// Defect 1494371: Postal Code for San Marino optional : Mukesh
function addSMAndVAPostalCDValidator() {
  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry != 'undefined' && 'SM' == landCntry) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code for San Marino - SM' ], 'MAIN_NAME_TAB');
  }
  if (landCntry != 'undefined' && 'VA' == landCntry) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code for Holy See (Vatican City State) - VA' ], 'MAIN_NAME_TAB');
  }
}

function checkIfVATFiscalUpdatedIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        if (requestType != 'U') {
          return new ValidationResult(null, true);
        }
        var currentFiscalCode = FormManager.getActualValue('taxCd1');
        var currentVAT = FormManager.getActualValue('vat');
        var fiscalStatus = FormManager.getActualValue('fiscalDataStatus');
        qParams = {
          REQ_ID : FormManager.getActualValue('reqId'),
        };
        var result = cmr.query('GET.OLD_FISCAL_DATA', qParams);
        var oldVAT = result.ret1;
        var oldFiscalCode = result.ret2;
        var oldCompany = result.ret3;
        if (result != null && oldVAT != null && oldFiscalCode != null && (oldFiscalCode != currentFiscalCode || oldVAT != currentVAT)) {
          if (fiscalStatus == null || fiscalStatus == '') {
            return new ValidationResult(null, false, 'Fiscal Data has been modified, please validate fiscal data');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function doValidateFiscalDataModal() {
  cmr.setModalTitle('validateFiscalDataModal', 'Process Fiscal Data');
  var vat = FormManager.getActualValue('vat');
  var fiscalCd = FormManager.getActualValue('taxCd1');
  if (!vat && !fiscalCd) {
    FormManager.setValue('fiscalDataStatus', 'F');
    cmr.showAlert("No company addresses with this fiscal data found.", null, null, null);
  } else {
    var qParams = {
      VAT : vat,
      FISCAL_CD : fiscalCd,
      ZZKV_CUSNO : FormManager.getActualValue('cmrNo'),
      MANDT : cmr.MANDT
    };
    var result = cmr.query('GET.RDC_FISCAL_DATA_NEW', qParams);
    if (result.ret1 != null && result.ret1 != '') {
      cmr.showModal('validateFiscalDataModal');
      loadFiscalDataModal(result);
    } else {
      FormManager.setValue('fiscalDataStatus', 'F');
      cmr.showAlert("No company addresses with this fiscal data found.", null, null, null);
    }
  }
}

/*
 * function doValidateFiscalData() { var currentFiscalCode =
 * FormManager.getActualValue('taxCd1'); var currentVAT =
 * FormManager.getActualValue('vat'); var currentCMRNo =
 * FormManager.getActualValue('cmrNo'); var currentCompanyNo =
 * FormManager.getActualValue('company'); if (currentCMRNo != '') { qParams = {
 * _qall : 'Y', VAT : currentVAT, FISCAL_CD : currentFiscalCode, MANDT :
 * FormManager.getActualValue('mandt'), }; var result =
 * cmr.query('GET.RDC_FISCAL_DATA', qParams); if (result != null &&
 * result.length > 1) { if (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', true); }
 * FormManager.disable('disableAutoProc');
 * FormManager.setValue('fiscalDataStatus', 'A'); cmr.showAlert("More than 1 CMR
 * exists in RDC with the current VAT and fiscal code.", "Warning"); return; }
 * if (result != null && result.length == 1) { var rdcCmrNo = result[0].ret1;
 * var rdcVATValue = result[0].ret2; var rdcFiscalCode = result[0].ret3; } if
 * (result != null && rdcCmrNo > currentCMRNo) { console.log('Request has old
 * data'); FormManager.setValue('fiscalDataStatus', 'O');
 * FormManager.setValue('fiscalDataCompanyNo', rdcCmrNo);
 * FormManager.setValue('taxCd1', rdcFiscalCode); FormManager.setValue('vat',
 * rdcVATValue); FormManager.setValue('company', rdcCmrNo);
 * 
 * 
 * if (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', true); }
 * FormManager.disable('disableAutoProc');
 * 
 * 
 * if (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', false); }
 * FormManager.enable('disableAutoProc');
 * 
 * cmr.showAlert("Request has Old Data. Fiscal Data fields values has been
 * updated to newer values on Request.", "Warning"); } else if (result != null &&
 * rdcCmrNo < currentCMRNo) { console.log('Request has New data');
 * FormManager.setValue('fiscalDataStatus', 'N');
 * FormManager.setValue('fiscalDataCompanyNo', rdcCmrNo);
 * 
 * 
 * if (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', true); }
 * FormManager.disable('disableAutoProc');
 * 
 * 
 * if (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', false); }
 * FormManager.enable('disableAutoProc');
 * 
 * cmr.showAlert("Request has Newer Data. Old CMR=" + rdcCmrNo, "Warning"); }
 * else { console.log("No action needed as data on request and RDC is same or no
 * record found on KNA1 with current vat and fiscal code.");
 * FormManager.setValue('fiscalDataStatus', 'U'); if
 * (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', false); }
 * FormManager.enable('disableAutoProc'); cmr.showAlert("No action needed as
 * data on request and RDC is same or no record found on KNA1 with current vat
 * and fiscal code.", "Warning"); } } else { cmr.showAlert("Fiscal Data cannot
 * be validated as CMR number is blank", "Warning"); } }
 */

// Defect 1470471: Expedite Reason not blanked out when Expedite flag changed
// from YES to NO :Mukesh
function setExpediteReasonOnChange() {
  dojo.connect(FormManager.getField('expediteInd'), 'onChange', function(value) {
    if (FormManager.getActualValue('viewOnlyPage') == 'true') {
      return;
    }
    if (FormManager.getActualValue('expediteInd') == 'N') {
      FormManager.clearValue('expediteReason');
    }
  });
}

function validateVATFiscalLengthOnIdentIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        if (requestType != 'C') {
          return new ValidationResult(null, true);
        }
        var ident = FormManager.getActualValue('identClient');
        var fiscal = FormManager.getActualValue('taxCd1');
        var lbl1 = FormManager.getLabel('LocalTax1');
        var custSubGrp = FormManager.getActualValue('custSubGrp');

        if (ident == 'A' && fiscal != undefined && fiscal != '' && (fiscal.length != 11 || !fiscal.match("^[0-9]*$")) && (role == 'PROCESSOR' || (role == 'REQUESTER' && custSubGrp == 'BUSPR'))) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client A ' + lbl1 + ' must be of 11 digits only');
        }
        if (ident == 'B' && fiscal != undefined && fiscal != '' && (fiscal.length != 11 || !fiscal.match("^[0-9]*$"))) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client B ' + lbl1 + ' must be of 11 digits only');
        }
        if (ident == 'C' && fiscal != undefined && fiscal != '' && (fiscal.length != 11 || !fiscal.match("^[0-9]*$"))) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client C ' + lbl1 + ' must be of 11 digits only');
        }
        if (ident == 'D' && fiscal != undefined && fiscal != '' && (fiscal.length != 16 || !fiscal.match("^[0-9a-zA-Z]*$")) && role == 'PROCESSOR') {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client D ' + lbl1 + ' must be of 16 alphanumerics only');
        }
        if (ident == 'X' && fiscal != undefined && fiscal != '' && fiscal.length != 16) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client X ' + lbl1 + ' must be of 16 chars');
        }
        if (ident == 'X' && fiscal != undefined && fiscal != '' && !fiscal.match("^(?=.*[a-zA-Z])(?=.*[0-9])[0-9a-zA-Z]*$")) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client X ' + lbl1 + ' must contain alphanumeric characters.');
        }
        if (ident == 'N' && fiscal != undefined && fiscal != '' && !fiscal.match("^[0-9a-zA-Z]*$") && role == 'PROCESSOR') {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client N ' + lbl1 + ' must contain digits and alphabets only');
        }
        // Defect 1593720
        var cntryRegion = FormManager.getActualValue('countryUse');
        var tempCntryRegion = '';
        if (cntryRegion != '' && cntryRegion.length > 3) {
          tempCntryRegion = cntryRegion.substring(3, 5);
        }
        console.log("cntryRegion is>>" + cntryRegion);
        if (tempCntryRegion != 'VA') {
          console.log("For Not VA CntryRegion>>" + tempCntryRegion);
          if (ident == 'N' && fiscal != undefined && (fiscal == '' || fiscal.length > 16) && role == 'PROCESSOR') {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, 'For Ident Client N ' + lbl1 + ' must be of 1-16 chars');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function setVATOnIdentClientChangeIT() {
  var requestType = FormManager.getActualValue('reqType');
  if (requestType != 'C') {
    return new ValidationResult(null, true);
  }

  var role = FormManager.getActualValue('userRole').toUpperCase();

  var ident = FormManager.getActualValue('identClient');
  if (ident != '' && (ident == 'A' || ident == 'D') && role == 'REQUESTER') {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
  }
  if (ident != '' && (ident == 'A' || ident == 'D') && role == 'PROCESSOR') {
    FormManager.resetValidations('vat');
  }
  if (ident != '' && ident == 'B') {
    FormManager.resetValidations('vat');
  }
  if (ident != '' && ident == 'C' && role == 'PROCESSOR') {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
  }
  if (ident != '' && ident == 'C' && role == 'REQUESTER') {
    FormManager.resetValidations('vat');
  }

  if (ident != '' && (ident == 'X' || ident == 'N' || ident == 'Y')) {
    FormManager.resetValidations('vat');
    FormManager.setValue('vat', '');
    FormManager.readOnly('vat');
  }

  var checkImportIndc = getImportedIndcForItaly();
  if (checkImportIndc != 'Y' && ident != '' && (ident == 'A' || ident == 'C')) {
    FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Fiscal Code' ], 'MAIN_CUST_TAB');
    FormManager.enable('taxCd1');
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    FormManager.enable('vat');
  }

}

function setFiscalCodeOnIdentClientCB_IT() {
  var ident = FormManager.getActualValue('identClient');
  if (FormManager.getActualValue('reqType') == 'C' && 'CROSS' == FormManager.getActualValue('custGrp')) {
    var reqId = FormManager.getActualValue('reqId');
    var checkImportIndc = getImportedIndcForItaly();

    if (checkImportIndc != 'Y') {
      FormManager.enable('identClient');
      if (ident == 'N') {
        FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Fiscal Code' ], 'MAIN_CUST_TAB');
        FormManager.enable('taxCd1');
      } else if (ident == 'Y') {
        FormManager.resetValidations('taxCd1');
        FormManager.setValue('taxCd1', '');
        FormManager.readOnly('taxCd1');
      }
    }
  }
}
// Defect 1532076 : Mukesh
function validateEnterpriseNumForIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var enterprise = FormManager.getActualValue('enterprise');
        var letterNumber = /^[0-9a-zA-Z]+$/;
        // Story 1592370
        if (enterprise != '') {
          if (enterprise.length == 1 && enterprise != '#') {
            return new ValidationResult(null, false, 'Enterprise Number/ CODICE ENTERPRISE should be # only.');
          }
          if (enterprise.length > 1 && enterprise.length != 6) {
            return new ValidationResult(null, false, 'Enterprise Number/ CODICE ENTERPRISE should be 6 characters long.');
          }

          if (enterprise.length > 1 && !enterprise.match(letterNumber)) {
            return new ValidationResult({
              id : 'enterprise',
              type : 'text',
              name : 'enterprise'
            }, false, 'Enterprise Number/ CODICE ENTERPRISE should be alpha numeric.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/*
 * function validateSRForIT() { FormManager.addFormValidator((function() {
 * return { validate : function() { var isu =
 * FormManager.getActualValue('isuCd'); var salesRep =
 * FormManager.getActualValue('repTeamMemberNo'); var lbl1 =
 * FormManager.getLabel('SalRepNameNo'); if (isu != undefined && isu != 32) { if
 * (salesRep == '09NIMM' || salesRep == '09GEMM' || salesRep == '09NCMM' ||
 * salesRep == '09NAMM' || salesRep == '09NBMM' || salesRep == '09NLMM' ||
 * salesRep == '09NPMM' || salesRep == '09NFMM' || salesRep == '09RPMM' ||
 * salesRep == '09KAMM' || salesRep == '09CPMM' || salesRep == '09NGMM' ||
 * salesRep == '09NMMM' || salesRep == '09GKMM' || salesRep == '09GJMM' ||
 * salesRep == '09GHMM' ) { return new ValidationResult({ id :
 * 'repTeamMemberNo', type : 'text', name : 'repTeamMemberNo' }, false, 'The ' +
 * lbl1 + ' cannot be default when ISU differs from 32.'); } } return new
 * ValidationResult(null, true); } }; })(), 'MAIN_IBM_TAB', 'frmCMR'); }
 */

function useCountryScenarioRules(scenario, name, value) {
  var cty = FormManager.getActualValue('cmrIssuingCntry');
  if (cty != '758') {
    return false;
  }
  // Defect 1579935
  // italy do not override values for ISU, CTC, SBO, SR
  var reqId = FormManager.getActualValue('reqId');

  var importIndc = getImportedIndcForItaly();
  var custSubType = FormManager.getActualValue('custSubGrp');

  if (importIndc == 'N') {
    return false;
  }
  // ISU, CTC, INAC, SBO, Sales Rep, Affiliate
  var noOverrides = [ 'clientTier', 'isuCd', 'repTeamMemberNo', 'salesBusOffCd', 'inacCd', 'affiliate', 'identClient' ];
  if (custSubType != undefined && custSubType != '' && (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA')) {
    noOverrides = [ 'identClient' ];
  }
  if (noOverrides.indexOf(name) >= 0) {
    var currVal = FormManager.getActualValue(name);
    if (currVal != '') {
      return true;
    }
  }
  return false;
}

function setStateProvReqdPostalCodeIT() {
  var postCode = FormManager.getActualValue('postCd');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var landCnty = FormManager.getActualValue('landCntry');
  var addrType = FormManager.getActualValue('addrType');
  if (postCode != '' && postCode.length >= 2 && landCnty == 'IT') {
    postCode = postCode.substring(0, 2);
    postCode = postCode.trim() + '%';
    qParams = {
      _qall : 'Y',
      TXT : postCode,
    };
    var results = cmr.query('GETSPVALFORPOSTCDIT', qParams);
    if (results != null) {
      if (role == 'REQUESTER' && results.length > 1 && (addrType != 'ZS01' || !FormManager.getField('addrType_ZS01').checked)) {
        FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
      }
      if (results.length == 1 && role == 'REQUESTER' && (addrType != 'ZS01' || !FormManager.getField('addrType_ZS01').checked)) {
        FormManager.resetValidations('stateProv');
      }
    }
  }
}

/*
 * ITALY - sets SBO/IBO based on Sales rep
 */
function autoSetSBOOnSRValueIT() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var sbo = FormManager.getActualValue('salesBusOffCd');
  var salesRep = FormManager.getActualValue('repTeamMemberNo');
  if (salesRep != undefined && salesRep != '' && salesRep.length >= 4) {
    sbo = salesRep.substring(2, 4);
  }
  if (sbo != '' && sbo.length == 2 && !(FormManager.getActualValue('salesBusOffCd') == sbo || FormManager.getActualValue('salesBusOffCd') == '0' + sbo + 'B000')) {
    FormManager.setValue('salesBusOffCd', sbo);
    FormManager.readOnly('salesBusOffCd');
    console.log('setting SBO=' + sbo);
  }
  if (salesRep == '') {
    FormManager.clearValue('salesBusOffCd');
  }
}

function addInstallingAddrValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var zs01ReqId = FormManager.getActualValue('reqId');
        qParams = {
          REQ_ID : zs01ReqId,
        };
        var record = cmr.query('GETZS01VALRECORDS', qParams);
        var zs01Reccount = record.ret1;
        if (Number(zs01Reccount) > 1) {
          return new ValidationResult(null, false, 'Only one Installing Address can be defined.');
        } else if (Number(zs01Reccount == 0)) {
          return new ValidationResult(null, false, 'At least one Installing Address must be defined.');
        } else {
          return new ValidationResult(null, true);
        }

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

// CMR-2085
function validateCMRNumExistForTR() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking requested cmr number...');
        var reqType = FormManager.getActualValue('reqType');
        var cmrNo = FormManager.getActualValue('cmrNo');
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

function enableCMRNUMForPROCESSOR() {
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if ('C' == FormManager.getActualValue('reqType')) {
    if (role == "PROCESSOR") {
      FormManager.enable('cmrNo');
    } else {
      FormManager.readOnly('cmrNo');
    }
  }
}

function checkIfStateProvBlankForProcIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        var landCountry = FormManager.getActualValue('landCountry');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        if (requestType != 'C') {
          return new ValidationResult(null, true);
        }
        if (landCountry != 'IT') {
          return new ValidationResult(null, true);
        }
        if (role == 'PROCESSOR') {
          var qParams = {
            REQ_ID : FormManager.getActualValue('reqId'),
          };
          var results = cmr.query('GETZP01STATEPROV', qParams);
          if (results != null && results.ret1 != undefined) {
            if (results.ret1 == '') {
              return new ValidationResult(null, false, 'State Prov cannot be blank for the Billing address');
            }

            var results = cmr.query('GETZI01STATEPROV', qParams);
            if (results != null && results.ret1 != undefined) {
              if (results.ret1 == '') {
                return new ValidationResult(null, false, 'State Prov cannot be blank for the Company address');
              }
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function autoResetFiscalDataStatus() {
  var requestType = FormManager.getActualValue('reqType');
  if (requestType != 'U') {
    return new ValidationResult(null, true);
  }
  var currentFiscalCode = FormManager.getActualValue('taxCd1');
  var currentVAT = FormManager.getActualValue('vat');
  qParams = {
    REQ_ID : FormManager.getActualValue('reqId'),
  };
  var result = cmr.query('GET.OLD_FISCAL_DATA', qParams);
  var oldVAT = result.ret1;
  var oldFiscalCode = result.ret2;
  if (oldFiscalCode != currentFiscalCode || oldVAT != currentVAT) {
    console.log('Setting fiscalDataStatus and fiscalDataCompanyNo to blank');
    FormManager.setValue('fiscalDataStatus', '');
    FormManager.setValue('fiscalDataCompanyNo', '');
  } else {
    console.log("Retaining old values for company, enterprise and identclient: " + _pagemodel.company + ", " + _pagemodel.enterprise + ", " + _pagemodel.identClient);
    FormManager.setValue('company', _pagemodel.company);
    FormManager.setValue('enterprise', _pagemodel.enterprise);
    FormManager.setValue('identClient', _pagemodel.identClient);
  }
}

function disableAutoProcessingOnFiscalUpdate() {
  var requestType = FormManager.getActualValue('reqType');
  if (requestType != 'U' || requestType != 'X') {
    return new ValidationResult(null, true);
  }
  console.log('Setting disable auto processing Y and readonly');
  if (FormManager.getActualValue('fiscalDataStatus') != '' && FormManager.getActualValue('fiscalDataStatus') == 'A') {
    if (dijit.byId('disableAutoProc')) {
      FormManager.getField('disableAutoProc').set('checked', true);
    }
    FormManager.disable('disableAutoProc');
  } else {
    if (dijit.byId('disableAutoProc')) {
      FormManager.getField('disableAutoProc').set('checked', false);
    }
    FormManager.enable('disableAutoProc');
  }

}

function allowCopyOfNonExistingAddressOnly(cntry, addressMode, saving, afterValidate) {
  if (saving) {
    return;
  }

  if (cmr.addressMode == 'copyAddress') {
    var requestId = FormManager.getActualValue('reqId');
    var requestType = FormManager.getActualValue('reqType');
    if (requestType != 'C') {
      return new ValidationResult(null, true);
    }
    qParams = {
      REQ_ID : requestId,
    };
    var record1 = cmr.query('GETZS01VALRECORDS', qParams);
    var zs01Reccount = record1.ret1;
    if (Number(zs01Reccount) == 1) {
      cmr.hideNode('radiocont_ZS01');
    } else {
      cmr.showNode('radiocont_ZS01');
    }
    var record2 = cmr.query('GETZI01VALRECORDS', qParams);
    var zi01Reccount = record2.ret1;
    if (Number(zi01Reccount) == 1) {
      cmr.hideNode('radiocont_ZI01');
    } else {
      cmr.showNode('radiocont_ZI01');
    }
    var record3 = cmr.query('GETZP01VALRECORDS', qParams);
    var zp01Reccount = record3.ret1;
    if (Number(zp01Reccount) == 1) {
      cmr.hideNode('radiocont_ZP01');
    } else {
      cmr.showNode('radiocont_ZP01');
    }
  }
}

function stateProvValidatorCBforIT(cntry, addressMode, saving, afterValidate) {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var landCntryVal = FormManager.getActualValue('landCntry');
        var addrType = FormManager.getActualValue('addrType');
        if (landCntryVal != 'IT' && addrType != 'ZS01') {
          var expectedStateProv = getDescription('crossbCntryStateProvMapIT');
          var currentStateProv = FormManager.getActualValue('stateProvItaly');
          if (expectedStateProv != undefined && currentStateProv != undefined && expectedStateProv != '' && currentStateProv != '' && expectedStateProv != currentStateProv) {
            return new ValidationResult(null, false, 'Invalid value for State/Prov. Please select ' + expectedStateProv);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setCodFlagVal() {
  FormManager.setValue('codFlag', '3');
  FormManager.readOnly('codFlag');
}

function optionalFieldsForUpdateReqUKI() {
  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType != 'U' || reqType != 'X') {
    return;
  }
  /*
   * if (role != 'Requester') { return; }
   */
  FormManager.resetValidations('abbrevNm');
  FormManager.resetValidations('subIndustryCd');
  FormManager.resetValidations('isicCd');
  FormManager.resetValidations('isuCd');
  FormManager.resetValidations('clientTier');
}

/*
 * Story 1808695: DEV: Scenario Sub-types available for Cross Boarder Scenario
 * Type Author: Dennis Natad Description: Adds landed country validator to put
 * in an error message if scenario is cross, landed must not be GB. If scenario
 * is local, landed must be GB. All on the sold-to address.
 */
function addUKILandedCountryValidtor() {
  console.log("register addUKILandedCountryValidtor . . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                var reqType = FormManager.getActualValue('reqType');
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, genericMsg = 'Landed Country value of the Sold-to (Main) Address should not be "GB" for Cross-Border customers.';
                  if (addressItems != null && addressItems.length != 0) {
                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], landCtry = '', scenario = FormManager.getActualValue('custGrp');
                      var addrType = currentAddr.addrType[0];
                      landCtry = currentAddr.landCntry[0];
                      console.log('addrType >> ' + addrType);
                      console.log('landCntry >> ' + landCtry);
                      if (addrType == 'ZS01' && landCtry == 'GB' && reqType == 'C' && scenario == 'CROSS') {
                        return new ValidationResult(null, false, genericMsg);
                      }
                      if (addrType == 'ZS01' && landCtry != 'GB' && reqType == 'C' && scenario == 'LOCAL') {
                        genericMsg = 'Landed Country value of the Sold-to (Main) Address should be "GB" for Local customers.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                    } // for
                  }
                } else {
                  console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR'); // validator body
}

function restrictDuplicateAddrUKI(cntry, addressMode, saving, finalSave, force) {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestId = FormManager.getActualValue('reqId');
        var addressSeq = FormManager.getActualValue('addrSeq');
        var addressType = FormManager.getActualValue('addrType');
        var dummyseq = "xx";
        var showDuplicateSoldToError = false;
        var showDuplicateMailToError = false;
        var showDuplicateSoftwareError = false;
        var qParams;
        if (addressMode == 'updateAddress') {
          qParams = {
            REQ_ID : requestId,
            ADDR_SEQ : addressSeq,
          };
        } else {
          qParams = {
            REQ_ID : requestId,
            ADDR_SEQ : dummyseq,
          };
        }
        if (addressType != undefined && addressType != '' && addressType == 'ZS01' && cmr.addressMode != 'updateAddress') {
          var result = cmr.query('GETZS01RECORDS', qParams);
          var zs01count = result.ret1;
          showDuplicateSoldToError = Number(zs01count) >= 1 && addressType == 'ZS01';
          if (showDuplicateSoldToError) {
            return new ValidationResult(null, false, 'Only one Billing address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
          }
        }

        if (addressType != undefined && addressType != '' && addressType == 'ZP01' && cmr.addressMode != 'updateAddress') {
          var result = cmr.query('GETZP01ADDRRECORDS', qParams);
          var zp01count = result.ret1;
          showDuplicateMailToError = Number(zp01count) >= 1 && addressType == 'ZP01';
          if (showDuplicateMailToError) {
            return new ValidationResult(null, false, 'Only one Mailing address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
          }
        }

        if (addressType != undefined && addressType != '' && addressType == 'ZS02' && cmr.addressMode != 'updateAddress') {
          var result = cmr.query('GETZS02ADDRRECORDS', qParams);
          var zs02count = result.ret1;
          showDuplicateSoftwareError = Number(zs02count) >= 1 && addressType == 'ZS02';
          if (showDuplicateSoftwareError) {
            return new ValidationResult(null, false,
                'Only one Software Upgrade address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

}
function validateNumericValueUKI() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custPhone = FormManager.getActualValue('custPhone');
        var numPattern = /^[0-9]+$/;

        if (custPhone == '') {
          return new ValidationResult(null, true);
        }
        if (custPhone.length >= 1 && !custPhone.match(numPattern)) {
          return new ValidationResult({
            id : 'custPhone',
            type : 'text',
            name : 'custPhone'
          }, false, 'Phone # should be numeric value only.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function validateCompanyNoForUKI() {

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var company = FormManager.getActualValue('company');
        var numPattern = /^[0-9]+$/;

        if (company == '') {
          return new ValidationResult(null, true);
        } else {
          if (company.length >= 1 && company.length != 6) {
            return new ValidationResult(null, false, 'Company Number should be 6 digit long.');
          }
          if (company.length > 1 && !company.match(numPattern)) {
            return new ValidationResult({
              id : 'company',
              type : 'text',
              name : 'company'
            }, false, 'Company Number should be number only.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function lockRequireFieldsUKI() {
  var reqType = FormManager.getActualValue('reqType').toUpperCase();
  var vat = FormManager.getActualValue('vat');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (cntry == SysLoc.IRELAND) {
    FormManager.readOnly('salesBusOffCd');
  }

  if ((reqType == 'U' || reqType == 'X') && role == 'REQUESTER') {
    FormManager.readOnly('abbrevLocn');
  }

  if ((reqType == 'U' || reqType == 'X') && FormManager.getActualValue('ordBlk') == '93') {
    FormManager.readOnly('reqReason');
    if (role == 'REQUESTER') {
      if (vat == '' || vat == 'undefined') {
        FormManager.enable('vat');
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      } else {
        FormManager.readOnly('vat');
      }
    }
  }
}

function addStreetPoBoxValidatorUKI() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrTxt = FormManager.getActualValue('addrTxt');
        var poBox = FormManager.getActualValue('poBox');
        var addrType = FormManager.getActualValue('addrType');
        if ((addrType != undefined && addrType != '') && (addrType == 'ZS01' || addrType == 'ZP01')) {
          if (addrTxt == '' && poBox == '') {
            return new ValidationResult(null, false, 'Please fill-out either Street Address or PO Box.');
          }
          return new ValidationResult(null, true);
        }
        if ((addrType != undefined && addrType != '') && (addrType == 'ZI01' || addrType == 'ZD01' || addrType == 'ZS02')) {
          if (addrTxt == '') {
            return new ValidationResult({
              id : 'addrTxt',
              type : 'text',
              name : 'addrTxt'
            }, false, 'Street Address is required.');
          }
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}
function addSBOSRLogicIE() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var _isuCd = FormManager.getActualValue('isuCd');
  var _clientTier = FormManager.getActualValue('clientTier');
  var salesRepValue = [];
  if (_isuCd != '' && _clientTier != '') {
    var qParams = {
      _qall : 'Y',
      ISU_CD : _isuCd,
      CLIENT_TIER : '%' + _clientTier + '%',
    };
    var results = cmr.query('GET.SALESREP.IRELAND', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        salesRepValue.push(results[i].ret1);
      }

      if (_isuCd == '21' && _clientTier == '7') {
        salesRepValue.push('MMIR11');
      } else if (_isuCd == '4F' && _clientTier == '7') {
        salesRepValue.push('I72089');
      } else if (_isuCd == '31' && _clientTier == '7') {
        salesRepValue.push('I72089');
      } else if (_isuCd == '18' && _clientTier == '7') {
        salesRepValue.push('MMIRE2');
      } else if (_isuCd == '15' && _clientTier == '7') {
        salesRepValue.push('MMIRE2');
      } else if (_isuCd == '34' && _clientTier == '6') {
        salesRepValue.push('MMSN11');
      } else if (_isuCd == '32' && _clientTier == 'M') {
        salesRepValue.push('MMIR12');
        salesRepValue.push('MMSN11');
      }

      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesRepValue);
      if (salesRepValue.length == 1) {
        FormManager.setValue('repTeamMemberNo', salesRepValue[0]);
      }
    }
  }
}
function addEmbargoCodeValidatorUKI() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd');
        var reqType = FormManager.getActualValue('reqType');

        if (reqType != 'U' || reqType != 'X') {
          return new ValidationResult(null, true);
        }
        if (embargoCd != '' && embargoCd.length > 0) {
          embargoCd = embargoCd.trim();
          if ((embargoCd != '' && embargoCd.length == 1) && (embargoCd == 'E' || embargoCd == 'C' || embargoCd == 'J')) {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult({
              id : 'embargoCd',
              type : 'text',
              name : 'embargoCd'
            }, false, 'Embargo Code value should be only E or C or J.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function autoPopulateISUClientTierUK() {
  var reqType = FormManager.getActualValue('reqType');
  if (cmr.currentRequestType != 'C') {
    return;
  }
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var noScenario = new Set([ 'INTER', 'XINTR', 'BUSPR', 'XBSPR' ]);

  if (custSubGroup != undefined && custSubGroup != '' && !noScenario.has(custSubGroup)) {

    var addrType = FormManager.getActualValue('addrType');
    var postCd = FormManager.getActualValue('postCd');
    if (postCd != null && postCd.length > 2) {
      postCd = postCd.substring(0, 2);
    }
    if (addrType != '' && addrType == 'ZS01') {
      if (postCd != '' && (isNorthernIrelandPostCd(postCd) || isScotlandPostCd(postCd))) {
        FormManager.setValue('isuCd', "32");
        FormManager.setValue('clientTier', "C");
      } else {
        FormManager.setValue('clientTier', "S");
      }
    }
  } else {
    FormManager.setValue('isuCd', "21");
    FormManager.setValue('clientTier', "7");
  }
}

function autoPopulateAbbNameCopySoldToTR() {
  var reqType = FormManager.getActualValue('reqType');
  if (cmr.currentRequestType != 'C') {
    return;
  }
  var custGroup = FormManager.getActualValue('custGrp');

  if (custGroup != undefined && (custGroup == 'CROSS' || "TR" != FormManager.getActualValue("landCntry"))) {

    var addrType = FormManager.getActualValue('addrType');
    var abbName = FormManager.getActualValue('custNm1');

    console.log('addrType = ' + addrType);
    console.log('abbName = ' + abbName);
    if (addrType != '' && addrType == 'ZP01') {

      FormManager.setValue('abbrevNm', abbName);
    }
  }
}

function autoSetISUClientTierUK() {
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var noScenario = new Set([ 'INTER', 'XINTR', 'BUSPR', 'XBSPR' ]);

  if (reqType != 'C') {
    return;
  }

  if ('866' == FormManager.getActualValue('cmrIssuingCntry')) {
    if (custSubGroup != undefined && custSubGroup != '' && !noScenario.has(custSubGroup)) {

      var _reqId = FormManager.getActualValue('reqId');
      var postCdParams = {
        REQ_ID : _reqId,
        ADDR_TYPE : "ZS01",
      };

      var postCdResult = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', postCdParams);
      var postCd = postCdResult.ret1;
      if (postCd != null && postCd.length > 2) {
        postCd = postCd.substring(0, 2);
      }

      if (postCd != '' && (isNorthernIrelandPostCd(postCd) || isScotlandPostCd(postCd))) {
        FormManager.setValue('isuCd', "32");
        FormManager.setValue('clientTier', "C");
      } else {
        FormManager.setValue('clientTier', "S");
      }
    } else {
      FormManager.setValue('isuCd', '21');
      FormManager.setValue('clientTier', '');
    }
  }
}
function disableAddrFieldsUKI() {
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != 'ZS01' && addrType != 'ZD01') {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  } else {
    FormManager.enable('custPhone');
  }
}

function autoSetAbbrevLocUKI() {
  var _custType = FormManager.getActualValue('custSubGrp');
  var _custGrp = FormManager.getActualValue('custGrp');
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var _abbrevLocn = null;
  var _addrType = null;
  var _result = null;
  if (_custGrp == 'CROSS') {
    if (_custType == 'XINTR') {
      _addrType = 'ZI01';
    } else if (_custType == 'XBSPR' || _custType == 'XGOVR' || _custType == 'XPRIC' || _custType == 'CROSS') {
      _addrType = 'ZS01';
    }
    var qParams = {
      REQ_ID : _zs01ReqId,
      ADDR_TYPE : _addrType,
    };

    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
      _result = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', qParams);
    } else {
      _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
    }

    // _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
    _abbrevLocn = _result.ret1;
  } else {
    if (_custType == 'SOFTL') {
      _abbrevLocn = "SOFTLAYER";
    } else {
      var _zs01ReqId = FormManager.getActualValue('reqId');
      var _addrType = null;
      if (_custType == 'INTER' || _custType == 'INFSL' || (_custType == 'THDPT' && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND)) {
        _addrType = 'ZI01';
      } else {
        _addrType = 'ZS01';
      }
      var qParams = {
        REQ_ID : _zs01ReqId,
        ADDR_TYPE : _addrType,
      };

      if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
        _result = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', qParams);
      } else {
        _result = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', qParams);
      }
      _abbrevLocn = _result.ret1;
    }
  }

  if (_abbrevLocn != null && _abbrevLocn.length > 12) {
    _abbrevLocn = _abbrevLocn.substr(0, 12);
  }
  FormManager.setValue('abbrevLocn', _abbrevLocn);
  if (_custType == 'SOFTL') {
    FormManager.readOnly('abbrevLocn');
  } else {
    FormManager.enable('abbrevLocn');
  }
}
function islandedCountry(landCntry) {
  var islandedCountry = false;
  for (var i = 0; i < UK_LANDED_CNTRY.length; i++) {
    if (landCntry == UK_LANDED_CNTRY[i]) {
      islandedCountry = true;
      break;
    }
  }
  if (islandedCountry == true) {
    return true;
  } else {
    return false;
  }
}

function optionalRulePostalCodeUK() {
  var landCntry = FormManager.getActualValue('landCntry');
  var reqType = FormManager.getActualValue('reqType');

  if (reqType != 'C') {
    return;
  }

  if (landCntry != 'undefined' && islandedCountry(landCntry)) {
    FormManager.resetValidations('postCd');
  } else {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code' ], 'MAIN_NAME_TAB');
  }
}

function optionalRuleForVatUK() {

  if (reqType != 'C') {
    return;
  }
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custGrp = FormManager.getActualValue('custGrp');
  var landCntry = FormManager.getActualValue('landCntry');

  if (issuingCntry == SysLoc.UK && (custGrp != undefined && custGrp != '') && custGrp == 'CROSS') {
    var _reqId = FormManager.getActualValue('reqId');
    var params = {
      REQ_ID : _reqId,
      ADDR_TYPE : "ZS01"
    };

    var landCntryResult = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', params);
    landCntry = landCntryResult.ret1;

    if (landCntry != 'undefined' && islandedCountry(landCntry)) {
      FormManager.resetValidations('vat');
      FormManager.enable('vat');
    } else {
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    }
  }
}
function setTaxCdBasedOnlandCntryUK() {
  var custGrp = FormManager.getActualValue('custGrp');
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (issuingCntry == SysLoc.UK && (custGrp != undefined && custGrp != '') && custGrp == 'CROSS') {
    var landCntry = FormManager.getActualValue('landCntry');
    var addrType = FormManager.getActualValue('addrType');
    var custSubGrp = FormManager.getActualValue('custSubGrp');

    if (addrType != null && addrType == 'ZS01') {
      if (landCntry != 'undefined' && landCntry == 'IM') {
        FormManager.setValue('specialTaxCd', 'Bl');
        FormManager.enable('specialTaxCd');
      } else {
        if (custSubGrp != null && custSubGrp == 'XINTR') {
          FormManager.setValue('specialTaxCd', 'XX');
          FormManager.readOnly('specialTaxCd');
        } else {
          FormManager.setValue('specialTaxCd', '32');
          FormManager.enable('specialTaxCd');
        }
      }
    }
  }
}

function autoSetABLocnAddr(_addrType) {
  var _custType = FormManager.getActualValue('custSubGrp');
  var _custGrp = FormManager.getActualValue('custGrp');
  var _abbrevLocn = null;
  var _result = null;
  if (_custGrp == 'CROSS') {
    if (_addrType == 'ZS01' && (_custType == 'XBSPR' || _custType == 'XGOVR' || _custType == 'XPRIC' || _custType == 'CROSS')) {
      _abbrevLocn = FormManager.getActualValue('landCntry');
    }
  } else {
    if (_custType == 'SOFTL') {
      _abbrevLocn = "SOFTLAYER";
    } else {
      if (_custType == 'INTER' || _custType == 'INFSL') {
        return;
      } else if (_addrType == 'ZS01') {
        if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
          _abbrevLocn = FormManager.getActualValue('postCd');
        } else {
          _abbrevLocn = FormManager.getActualValue('city1');
        }
      }
    }
  }

  if (_abbrevLocn != null && _abbrevLocn.length > 12) {
    _abbrevLocn = _abbrevLocn.substr(0, 12);
  }
  FormManager.setValue('abbrevLocn', _abbrevLocn);
  if (_custType == 'SOFTL') {
    FormManager.readOnly('abbrevLocn');
  } else {
    FormManager.enable('abbrevLocn');
  }
}

function autoPopulateABLocnUK(cntry, addressMode, saving, finalSave, force) {
  var reqType = null;
  var role = null;
  var _zs01ReqId = FormManager.getActualValue('reqId');
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType != 'C') {
    return;
  }
  if (role != 'Requester') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (saving || finalSave) {
    return;
  }

  var addressTyp = FormManager.getActualValue('addrType');
  var oldVal = null;
  var currAbbrevLocn = FormManager.getActualValue('abbrevLocn');

  if (addressTyp == 'ZS01') {
    var qParams = {
      REQ_ID : _zs01ReqId,
      ADDR_TYPE : addressTyp,
    };

    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
      var res = cmr.query('GET_OLD_POST_CD', qParams);
      oldVal = res.ret1;
    }
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
    var res = cmr.query('GET_OLD_CITY', qParams);
    oldVal = res.ret1;
  }

  if (oldVal != null && oldVal != undefined && currAbbrevLocn == oldVal) {
    if (addressTyp == 'ZS01') {
      autoSetABLocnAddr(addressTyp);
    }
  }
}

function toggleBPRelMemType() {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole.toUpperCase();
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType == 'C') {
    var _custType = FormManager.getActualValue('custSubGrp');
    if (_custType == 'BUSPR' || _custType == 'BUSSM' || _custType == 'BUSVA' || _custType == 'CROBP') {
      FormManager.show('PPSCEID', 'ppsceid');
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
      FormManager.show('MembLevel', 'memLvl');
      FormManager.show('BPRelationType', 'bpRelType');
      FormManager.resetValidations('bpRelType');
      FormManager.resetValidations('memLvl');
      // FormManager.readOnly('bpRelType');
      // FormManager.readOnly('memLvl');
      FormManager.addValidator('memLvl', Validators.REQUIRED, [ 'Membership Level' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('bpRelType', Validators.REQUIRED, [ 'BP Relation Type' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.resetValidations('ppsceid');
      FormManager.hide('PPSCEID', 'ppsceid');
      FormManager.hide('MembLevel', 'memLvl');
      FormManager.hide('BPRelationType', 'bpRelType');
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
      FormManager.removeValidator('memLvl', Validators.REQUIRED);
      FormManager.removeValidator('bpRelType', Validators.REQUIRED);
    }
  } else if ((reqType == 'U' || reqType == 'X') && role == 'REQUESTER') {
    FormManager.readOnly('ppsceid');
    FormManager.resetValidations('ppsceid');
    FormManager.readOnly('memLvl');
    FormManager.resetValidations('memLvl');
    FormManager.readOnly('bpRelType');
    FormManager.resetValidations('bpRelType');
  } else if ((reqType == 'U' || reqType == 'X') && role == 'PROCESSOR') {
    FormManager.enable('ppsceid');
    if (FormManager.getActualValue('ppsceid') != '') {
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    }
  }
}

function toggleBPRelMemTypeForTurkey() {
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType == 'U') {
    FormManager.show('MembLevel', 'memLvl');
    FormManager.show('BPRelationType', 'bpRelType');
    FormManager.resetValidations('bpRelType');
    FormManager.resetValidations('memLvl');
  } else {
    var _custType = FormManager.getActualValue('custSubGrp');
    if (_custType == 'BUSPR' || _custType == 'XBP') {
      FormManager.show('PPSCEID', 'ppsceid');
      FormManager.show('MembLevel', 'memLvl');
      FormManager.show('BPRelationType', 'bpRelType');
      FormManager.resetValidations('ppsceid');
      FormManager.resetValidations('bpRelType');
      FormManager.resetValidations('memLvl');
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('memLvl', Validators.REQUIRED, [ 'Membership Level' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('bpRelType', Validators.REQUIRED, [ 'BP Relation Type' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.hide('PPSCEID', 'ppsceid');
      FormManager.hide('MembLevel', 'memLvl');
      FormManager.hide('BPRelationType', 'bpRelType');
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
      FormManager.removeValidator('memLvl', Validators.REQUIRED);
      FormManager.removeValidator('bpRelType', Validators.REQUIRED);
    }
  }
}

function toggleTypeOfCustomerForTR() {
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType == 'U') {
    FormManager.show('TypeOfCustomer', 'crosSubTyp');
  } else {
    FormManager.hide('TypeOfCustomer', 'crosSubTyp');
  }
  // Lock type of customer and classification code when update requester
  if (reqType == 'U' && role == 'REQUESTER') {
    FormManager.readOnly('crosSubTyp');
    FormManager.readOnly('custClass');
  }
}

function unlockINACForINTERUKI() {
  var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custType = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (issu_cntry == '866' || issu_cntry == '754') {
    if ((custType == 'INTER' || custType == 'XINTR') && role == 'REQUESTER') {
      FormManager.readOnly('inacCd');
    } else if ((custType == 'INTER' || custType == 'XINTR') && role == 'PROCESSOR') {
      FormManager.enable('inacCd');
    }
  }
}

function checkHwMstrInstallFlag() {
  var addrType = FormManager.getActualValue('addrType');
  var siteId = '';
  var addrSeq = '';
  var reqId = FormManager.getActualValue('reqId');

  if (_hwMstrInstallFlagHandler == null && (addrType != null || addrType != '')) {
    _hwMstrInstallFlagHandler = dojo.connect(FormManager.getField('hwInstlMstrFlg'), 'onClick', function(value) {
      if (FormManager.getField('hwInstlMstrFlg').checked == true) {
        var qParams = {
          REQ_ID : reqId,
        };
        var result = cmr.query('ADDR.GET.HW_INSTL_MSTR_FLG', qParams);
        var result2 = cmr.query('ADDR.GET.HW_INSTL_MSTR_FLG_DETAILS', qParams);
        if (result.ret1 != null && result2 != null) {
          if (result.ret1 >= 1) {
            addrSeq = result2.ret1;
            siteId = result2.ret2;
            if (siteId != null && siteId != '') {
              cmr.showAlert("Address with Site ID: " + siteId + " is already a hardware master.");
              FormManager.getField('hwInstlMstrFlg').set('checked', false);
              return;
            }
            if (addrSeq != null && addrSeq != '') {
              cmr.showAlert("Address with Sequence: " + addrSeq + " is already a hardware master.");
              FormManager.getField('hwInstlMstrFlg').set('checked', false);
              return;
            }

          }
        }
      }
    });
  }
}

function addHwMstrInstFlgValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var recordList = null;
          var isInstMstrFlgValid = true;
          var adddrInstMstrFlg = "";
          var addrType = "";
          var adddrInstMstrFlgCount = 0;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (recordList == null && _allAddressData != null && _allAddressData[i] != null) {
              recordList = _allAddressData[i];
            }
            adddrInstMstrFlg = recordList.hwInstlMstrFlg;
            addrType = recordList.addrType;

            if (typeof (adddrInstMstrFlg) == 'object') {
              adddrInstMstrFlg = adddrInstMstrFlg[0];
            }

            if (typeof (addrType) == 'object') {
              addrType = addrType[0];
            }

            if ((addrType == 'ZI01' || addrType == 'ZS01' || addrType == 'ZS02') && adddrInstMstrFlg == 'Y') {
              isInstMstrFlgValid = true;
              adddrInstMstrFlgCount++;
            } else if (addrType != 'ZI01' && addrType != 'ZS01' && addrType != 'ZS02' && adddrInstMstrFlg == 'Y') {
              isInstMstrFlgValid = false;
            }
          }

          if (isInstMstrFlgValid == false) {
            return new ValidationResult(null, false, 'Hardware Install Master address can only be selected for Install-At/Sold-To Address.');
          } else if (isInstMstrFlgValid == true && adddrInstMstrFlgCount > 1) {
            return new ValidationResult(null, false, 'Hardware Install Master address can  be selected only for one Install-At/Sold-To Address. Please remove additional ones.');
          }

          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}
var _addrTypesForEMEA = [ 'ZD01', 'ZP01', 'ZI01', 'ZS01', 'ZS02' ];
function displayHwMstInstallFlagNew() {
  console.log(">>> Executing displayHwMstInstallFlagNew");
  console.log('>> cmr.addressMode >>' + cmr.addressMode);
  if (cmr.addressMode == 'newAddress'
      && (FormManager.getActualValue('cmrIssuingCntry') == '838' || FormManager.getActualValue('cmrIssuingCntry') == '866' || FormManager.getActualValue('cmrIssuingCntry') == '754')) {
    var _addrTypesForEMEA = [ 'ZD01', 'ZP01', 'ZI01', 'ZS01', 'ZS02' ];
    console.log('>> BEGIN displayHwMstInstallFlagNew newAddress or copyAddress function ');
    for (var i = 0; i < _addrTypesForEMEA.length; i++) {
      console.log('>> INSIDE for loop');

      addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForEMEA[i]), 'onClick', function(value) {
        if (FormManager.getField('addrType_ZI01').checked || FormManager.getField('addrType_ZS01').checked) {
          console.log(' >> 1 >> ' + FormManager.getField('addrType_ZI01').checked);
          console.log(' >> 2 >> ' + FormManager.getField('addrType_ZS01').checked);
          cmr.showNode('hwFlag');
        } else {
          FormManager.getField('hwInstlMstrFlg').set('checked', false);
          cmr.hideNode('hwFlag');
        }
      });
    }
  }
}
function displayHwMstrInstallFlag() {
  console.log(">>> Executing displayHwMstInstallFlag");
  console.log('>> cmr.addressMode >>' + cmr.addressMode);
  if (cmr.addressMode == 'updateAddress' && (FormManager.getActualValue('addrType') == 'ZI01' || FormManager.getActualValue('addrType') == 'ZS01')) {
    cmr.showNode('hwFlag');
  } else {
    cmr.hideNode('hwFlag');
  }
}

function validateFiscalCodeForCreatesIT() {
  console.log("register FiscalCode validator for Creates IT . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = FormManager.getActualValue('userRole');
        var taxCd1 = FormManager.getActualValue('taxCd1');
        var reqId = FormManager.getActualValue('reqId');
        var checkImportIndc = getImportedIndcForItaly();
        if (FormManager.getActualValue('reqType') == 'C' && checkImportIndc == 'N') {
          if ((taxCd1 != null && taxCd1.length != 0)) {
            qParams = {
              FISCAL_CODE : FormManager.getActualValue('taxCd1'),
              MANDT : cmr.MANDT
            }
            var result = cmr.query('IT.CHECK.DUPLICATE_FISCAL_ADDRESS', qParams);
            if (result.ret1 != null && result.ret1 != '') {
              return new ValidationResult({
                id : 'taxCd1',
                type : 'text',
                name : 'taxCd1'
              }, false, 'Fiscal Code on the request is already available on CMR No. ' + result.ret1 + '. Please mention another new fiscal code or import the CMR into the request.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function validateLandCntryForCBCreatesIT() {
  console.log("register Landed country validator with new requirement for IT . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('reqType') == 'C') {
          var scenario = FormManager.getActualValue('custGrp');
          var reqId = FormManager.getActualValue('reqId');
          // Billing Address
          var landCntryZP01 = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', {
            REQ_ID : reqId,
            ADDR_TYPE : 'ZP01'
          });
          // Company Address
          var landCntryZI01 = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', {
            REQ_ID : reqId,
            ADDR_TYPE : 'ZI01'
          });

          if (scenario == 'CROSS') {
            console.log("Italy Cross boarder Scenario......");

            if (landCntryZP01.ret1 != null && landCntryZI01.ret1 != null && landCntryZP01.ret1 != landCntryZI01.ret1) {
              return new ValidationResult({
                id : 'landCntry',
                type : 'text',
                name : 'Landed Country'
              }, false, 'Please make sure landed country of billing and company addresses is the same.');
            }
            return new ValidationResult(null, true);
          } else {
            console.log("Italy Local Scenario......");
            var cntryRegion = FormManager.getActualValue('countryUse');
            var landCntry = 'IT'; // default to Italy
            if (cntryRegion != '' && cntryRegion.length > 3) {
              landCntry = cntryRegion.substring(3, 5);
            }
            var defaultLandCntry = landCntry;

            if (landCntryZP01.ret1 != null && defaultLandCntry != '' && landCntryZP01.ret1 != defaultLandCntry) {
              return new ValidationResult(null, false, 'Landed Country value of the Bill-to (Billing) Address should be \'' + defaultLandCntry + '\' for Local.');
            } else if (landCntryZP01.ret1 != null && landCntryZI01.ret1 != null && landCntryZP01.ret1 != landCntryZI01.ret1) {
              return new ValidationResult({
                id : 'landCntry',
                type : 'text',
                name : 'Landed Country'
              }, false, 'Please make sure landed country of billing and company addresses is the same.');
            }
            return new ValidationResult(null, true);
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setPostCdItalyVA(cntry, addressMode, saving, finalSave) {
  if (addressMode == 'newAddress' || addressMode == 'updateAddress' || addressMode == 'copyAddress' && cmr.currentRequestType == 'C') {
    var _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      var landCntry = FormManager.getActualValue('landCntry');
      if (landCntry == 'VA') {
        if (_pagemodel.userRole.toUpperCase() == 'REQUESTER') {
          FormManager.setValue('postCd', '00120');
          FormManager.readOnly('postCd');
        } else {
          if (FormManager.getActualValue('postCd') == '') {
            FormManager.setValue('postCd', '00120');
          }
          FormManager.enable('postCd');
        }
      } else {
        FormManager.enable('postCd');
      }
    });
    if (_landCntryHandler && _landCntryHandler[0]) {
      _landCntryHandler[0].onChange();
    }
  }
}

function validateSBOForIT() {
  FormManager.addFormValidator(
      (function() {
        return {
          validate : function() {
            var isu = FormManager.getActualValue('isuCd');
            var sbo = FormManager.getActualValue('salesBusOffCd').toUpperCase();
            var lbl1 = FormManager.getLabel('SalesBusOff');
            var _custType = FormManager.getActualValue('custSubGrp');
            if (isu != undefined && isu == 21 && _custType != undefined && (_custType == 'INTER' || _custType == 'CROIN' || _custType == 'INTSM' || _custType == 'INTVA') && sbo != undefined
                && sbo != '') {
              if (sbo != '1B' && sbo != '1G' && sbo != '1E' && sbo != '11' && sbo != '12' && sbo != '13' && sbo != '14') {
                return new ValidationResult({
                  id : 'salesBusOffCd',
                  type : 'text',
                  name : 'salesBusOffCd'
                }, false, 'The ' + lbl1 + ' value for Internal scenario is invalid.');
              }
            }
            return new ValidationResult(null, true);
          }
        };
      })(), 'MAIN_IBM_TAB', 'frmCMR');

}

function validateCodiceDesIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var codicDes = FormManager.getActualValue('hwSvcsRepTeamNo');
        var alphanumeric = /^[0-9a-zA-Z]*$/;

        if (codicDes == '') {
          return new ValidationResult(null, true);
        } else {
          if (codicDes.length <= 5) {
            return new ValidationResult(null, false, 'CODICE DESTINATARIO/UFFICIO should be 6 or 7 characters.');
          }
          if (!codicDes.match(alphanumeric)) {
            return new ValidationResult({
              id : 'hwSvcsRepTeamNo',
              type : 'text',
              name : 'hwSvcsRepTeamNo'
            }, false, 'CODICE DESTINATARIO/UFFICIO should be alphanumeric only.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function setIBMFieldsMandtOptional() {
  if (FormManager.getActualValue('reqType') == 'C') {
    if (role.toUpperCase() == 'REQUESTER') {
      FormManager.removeValidator('isuCd', Validators.REQUIRED);
      FormManager.removeValidator('clientTier', Validators.REQUIRED);
      FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
      FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);

    } else {
      FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], null);
      FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], null);
      FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep No' ], null);
      FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ], null);

      // CREATCMR-4293
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      if (custSubGrp == 'XINT' || custSubGrp == 'INTER' || custSubGrp == 'XBP' || custSubGrp == 'BUSPR') {
        FormManager.removeValidator('clientTier', Validators.REQUIRED);
      }

    }
  }
}
function validateCMRNumberForIT() {

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cmrNo = FormManager.getActualValue('cmrNo');
        var numPattern = /^[0-9]+$/;
        if (FormManager.getActualValue('reqType') != 'C') {
          return new ValidationResult(null, true);
        }
        if (cmrNo == '') {
          return new ValidationResult(null, true);
        } else {
          // prod issue: skip validation for prospect request
          var ifProspect = FormManager.getActualValue('prospLegalInd');
          if (dijit.byId('prospLegalInd')) {
            ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
          }
          console.log("validateCMRNumberForIT ifProspect:" + ifProspect);
          if ('Y' == ifProspect) {
            return new ValidationResult(null, true);
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

function validateCMRNumberForTR() {

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
          // prod issue: skip validation for prospect request
          var ifProspect = FormManager.getActualValue('prospLegalInd');
          if (dijit.byId('prospLegalInd')) {
            ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
          }
          console.log("validateCMRNumberForTR ifProspect:" + ifProspect);
          if ('Y' == ifProspect) {
            return new ValidationResult(null, true);
          }
          if (_custSubGrp == 'INTER' || _custSubGrp == 'INTSO' || _custSubGrp == 'XINT' || _custSubGrp == 'XISO') {
            if (!cmrNo.startsWith("99")) {
              return new ValidationResult(null, false, 'Internal CMR should begin with 99.');
            }
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

function saveFiscalValues() {
  var choice = FormManager.getActualValue('cmrNewOld');
  if (choice == '') {
    cmr.showAlert('Please select either import or fiscal before save.');
    return;
  }

  if (document.getElementById('W').checked) {
    FormManager.setValue('company', dojo.byId('oldCmrNo').innerText);
    FormManager.setValue('fiscalDataStatus', 'W');
    FormManager.setValue('nationalCusId', 'W');
    FormManager.setValue('fiscalDataCompanyNo', dojo.byId('newCmrNo').innerText);
    FormManager.setValue('taxCd1', dojo.byId('oldTaxCd1').innerText);
    FormManager.setValue('vat', dojo.byId('oldVat').innerText);
    FormManager.setValue('enterprise', dojo.byId('oldEnterprise').innerText);
    FormManager.setValue('identClient', dojo.byId('oldIdentClient').innerText);
    FormManager.enable('taxCd1');
    FormManager.enable('vat');
    FormManager.enable('enterprise');
    FormManager.enable('identClient');
    cmr.hideModal('validateFiscalDataModal');
  } else if (document.getElementById('L').checked) {
    FormManager.setValue('company', dojo.byId('newCmrNo').innerText);
    FormManager.setValue('fiscalDataStatus', 'L');
    FormManager.setValue('nationalCusId', 'L');
    FormManager.setValue('fiscalDataCompanyNo', dojo.byId('newCmrNo').innerText);
    FormManager.setValue('taxCd1', dojo.byId('newTaxCd1').innerText);
    FormManager.setValue('vat', dojo.byId('newVat').innerText);
    FormManager.setValue('enterprise', dojo.byId('newEnterprise').innerText);
    FormManager.setValue('identClient', dojo.byId('newIdentClient').innerText);
    FormManager.readOnly('company');
    FormManager.readOnly('taxCd1');
    FormManager.readOnly('vat');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('identClient');
    cmr.hideModal('validateFiscalDataModal');
  }
}

function disableCompanyLevelFieldsIT() {

  console.log("Disabling company level fields");
  if (FormManager.getActualValue('reqType') == 'U') {
    var _cmrNo = FormManager.getActualValue('cmrNo');
    var _company = FormManager.getActualValue('company');
    if (_cmrNo != null && _company != null && _cmrNo != _company) {
      FormManager.readOnly('cmrNo');
      FormManager.readOnly('company');
      FormManager.readOnly('taxCd1');
      FormManager.readOnly('vat');
      FormManager.readOnly('enterprise');
      FormManager.readOnly('identClient');
    }
  }
}

function addEmbargoCodeValidatorIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd');
        var reqType = FormManager.getActualValue('reqType');

        /*
         * if (reqType != 'U') { return new ValidationResult(null, true); }
         */
        if (embargoCd != '' && embargoCd.length > 0) {
          embargoCd = embargoCd.trim();
          if ((embargoCd != '' && embargoCd.length == 1) && (embargoCd == 'Y' || embargoCd == 'J')) {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult({
              id : 'embargoCd',
              type : 'text',
              name : 'embargoCd'
            }, false, 'Please use valid value for Embargo code field.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

/**
 * Set Client Tier Value
 */
function setClientTierValuesIT(isuCd) {

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
          FormManager.setValue('clientTier', clientTiers[0]);
        }
      }
    }
  }
}

function validateExistingCMRNo() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking requested cmr number...');
        var reqType = FormManager.getActualValue('reqType');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (reqType == 'C' && cmrNo) {
          var exists = cmr.query('LD.CHECK_EXISTING_CMR_NO', {
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

function addAfterTemplateLoadIT() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if ('C' == FormManager.getActualValue('reqType')) {
    if (role == "REQUESTER" && _custSubGrp == 'INTER' || _custSubGrp == 'CROIN' || _custSubGrp == 'INTSM' || _custSubGrp == 'INTVA') {
      FormManager.enable('cmrNo');
    } else {
      FormManager.readOnly('cmrNo');
    }
    if (role == "PROCESSOR") {
      FormManager.enable('cmrNo');
    }
  }
  autoSetSBOSROnPostalCode(false, false);

  var ident = FormManager.getActualValue('identClient');
  if (FormManager.getActualValue('reqType') == 'C' && 'CROSS' == FormManager.getActualValue('custGrp')) {

    var reqId = FormManager.getActualValue('reqId');
    var checkImportIndc = getImportedIndcForItaly();
    if (checkImportIndc != 'Y') {
      FormManager.enable('identClient');
      if (ident == 'N') {
        FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Fiscal Code' ], 'MAIN_CUST_TAB');
        FormManager.enable('taxCd1');
      } else if (ident == 'Y') {
        FormManager.resetValidations('taxCd1');
        FormManager.setValue('taxCd1', '');
        FormManager.readOnly('taxCd1');
      }
    }
  }
  if ('C' == FormManager.getActualValue('reqType')) {
    FormManager.readOnly('salesBusOffCd');
  } else {
    FormManager.enable('salesBusOffCd');
  }

  if ('C' == FormManager.getActualValue('reqType') && role == "PROCESSOR" && custSubGrp == 'PRISM') {
    FormManager.enable('salesBusOffCd');
  }
}

function checkIsicCodeValidationIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        if (requestType != 'C') {
          return new ValidationResult(null, true);
        }
        var isicCode = FormManager.getActualValue('isicCd');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (isicCode == '9500') {
          if (custSubGrp == 'PRISM' || custSubGrp == 'PRICU' || custSubGrp == 'PRIVA' || custSubGrp == 'CROPR') {
            console.log('no validation required');
          } else {
            return new ValidationResult(null, false, 'ISIC code value should be other than 9500 for Non-Private customer.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addCustNm4ValidatorForTR() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm4 = FormManager.getActualValue('custNm4');
        var streetCont = FormManager.getActualValue('addrTxt2');

        // var reqId = FormManager.getActualValue('reqId');
        // var addressType = FormManager.getActualValue('addrType');
        // var addrSeq = FormManager.getActualValue('addrSeq');
        // var reqType = FormManager.getActualValue('reqType');
        // var qParams = {
        // ADDR_TYPE : addressType,
        // ADDR_SEQ : addrSeq,
        // REQ_ID : reqId
        // };
        // var results = cmr.query('GET.NAME4STR.TR', qParams);
        // if ('U' == reqType) {
        // if (results.ret2 != null && results.ret1 != null && results.ret2 ==
        // streetCont && results.ret1 == custNm4) {
        // return new ValidationResult(null, true);
        // }
        // }

        if ((custNm4.length != undefined && custNm4.length > 0) && (streetCont.length != undefined && streetCont.length > 0)) {
          return new ValidationResult(null, false, 'Only \'Street Cont\' or \'Name 4\' can be filled.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addAfterConfigItaly() {
  afterConfigForIT();
  fieldsReadOnlyItaly();
  setExpediteReasonOnChange();
  disableAutoProcessingOnFiscalUpdate();
  toggleBPRelMemType();
  typeOfCustomer();
  enableDisableTaxCodeCollectionCdIT();
  disableCompanyLevelFieldsIT();
  addFieldValidationForRequestorItaly();
  disableProcpectCmrIT();

}

function addAfterTemplateLoadItaly(fromAddress, scenario, scenarioChanged) {
  addFieldValidationForProcessorItaly();
  getOldValuesIT(fromAddress, scenario, scenarioChanged);
  autoPopulateIdentClientIT();
  setVATForItaly();
  fieldsReadOnlyItaly(fromAddress, scenario, scenarioChanged);
  setMRCOnScenariosIT();
  toggleBPRelMemType();
  typeOfCustomer();
  setSpecialTaxCodeOnScenarioIT();
  enableDisableTaxCodeCollectionCdIT();
  setClientTierValuesIT();
  addAfterTemplateLoadIT();
  setSalesRepValuesIT();
  blankedOutCollectionCD();
  setAffiliateEnterpriseRequired();
  addFieldValidationForRequestorItaly();
  disableProcpectCmrIT();
}

function addAddrFunctionItaly(cntry, addressMode, saving, finalSave) {
  addNonLatinCharValidator(cntry, addressMode, saving, finalSave);
  autoSetSBOSRForCompany(cntry, addressMode, saving, finalSave);
  autoSetAddrFieldsForIT(cntry, addressMode, saving, finalSave);
  addressLoadHandlerIT(cntry, addressMode, saving, finalSave);
  addLatinCharValidatorITALY(cntry, addressMode, saving, finalSave);
  toggleBPRelMemType(cntry, addressMode, saving, finalSave);
  setStateProvReqdPostalCodeIT(cntry, addressMode, saving, finalSave);
  allowCopyOfNonExistingAddressOnly(cntry, addressMode, saving, finalSave);
  setSpecialTaxCodeOnAddressIT(cntry, addressMode, saving, finalSave);
  setPostCdItalyVA(cntry, addressMode, saving, finalSave);

}
// CMR-1524 ITALY - Ident Client for CBs
function addFieldValidationForRequestorItaly() {
  var requestType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var scenario = FormManager.getActualValue('custGrp');
  if (requestType != 'C') {
    return;
  }
  if (scenario.includes('CRO')) {
    var scenarioType = 'CROSS';

    if (requestType == 'C' && scenarioType != undefined && scenarioType == 'CROSS') {
      FormManager.addValidator('identClient', Validators.REQUIRED, [ 'Ident Client' ], 'MAIN_CUST_TAB');
    } else {
      FormManager.resetValidations('identClient');
    }
  }
}
function disableProcpectCmrIT() {
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var ifProspect = FormManager.getActualValue('prospLegalInd');
  if (dijit.byId('prospLegalInd')) {
    ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
  }
  console.log("disable prospect CMR");
  if ('Y' == ifProspect) {
    FormManager.readOnly('cmrNo');
  }
}

/**
 * CMR-2279:Turkey - sets SBO based on isuCtc
 */
function setSBOValuesForIsuCtc(value) {

  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  var isuCtc = ((value != undefined ? value : isuCd) + clientTier);

  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType != 'C') {
    return;
  }

  /*
   * if (role == "REQUESTER") { if (custSubGrp == 'INTER' || custSubGrp == 'XINT ') {
   * FormManager.setValue('salesBusOffCd', "A10");
   * FormManager.disable("salesBusOffCd"); } else if (custSubGrp == 'BUSPR' ||
   * custSubGrp == 'XBP') { FormManager.setValue('salesBusOffCd', "140");
   * FormManager.disable("salesBusOffCd"); } } else {
   * FormManager.enable("salesBusOffCd"); }
   */

  var qParams = null;
  console.log("begin setSBO:" + '%' + isuCtc + '%');
  if (isuCd != '') {
    var results = null;
    qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCtc + '%'
    };
    results = cmr.query('GET.SBOLIST.BYISU', qParams);
    console.log("there are " + results.length + " SBO returned.");
    if (results.length > 0) {
      var sboList = new Array();
      for (var i = 0; i < results.length; i++) {
        sboList[i] = results[i].ret1;
      }
      if (results.length != 0) {
        FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), sboList);
      }
      // if (sboList.length == 1) {
      if (sboList.length == 1 || (sboList.length > 1 && _isScenarioChanged)) {
        FormManager.setValue('salesBusOffCd', sboList[0]);
      } else if (!_isScenarioChanged) {
        var oldSbo = null;
        qParams = {
          REQ_ID : FormManager.getActualValue('reqId')
        };
        oldSbo = cmr.query('GET.SBO.BYREQ', qParams);
        if (oldSbo != null && oldSbo != "") {
          if (sboList.indexOf(oldSbo.ret1) >= 0) {
            FormManager.setValue('salesBusOffCd', oldSbo.ret1);
          }
        }
      }
    } else {
      // FormManager.setValue('salesBusOffCd', "");
      console.log("Reset sbo as no list available for " + isuCtc);
      FormManager.resetDropdownValues(FormManager.getField('salesBusOffCd'));
    }
  } else {
    FormManager.resetDropdownValues(FormManager.getField('salesBusOffCd'));
    console.log("reset SBO start.");

    var oldSbo = null;
    qParams = {
      REQ_ID : FormManager.getActualValue('reqId')
    };
    oldSbo = cmr.query('GET.SBO.BYREQ', qParams);
    if (oldSbo != null && oldSbo != "") {
      FormManager.setValue('salesBusOffCd', oldSbo.ret1);
      console.log("reseting SBO ." + oldSbo);
    }
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('salesBusOffCd');
  }
}

function setSBOLogicOnISUChange() {
  if (_isuCdHandler == null && FormManager.getField('isuCd')) {
    _isuCdHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setSBOValuesForIsuCtc(value);
    });
  }
  /*
   * if (_isuCdHandler && _isuCdHandler[0]) { _isuCdHandler[0].onChange(); }
   */
  if (_clientTierHandler == null && FormManager.getField('clientTier')) {
    _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setSBOValuesForIsuCtc(FormManager.getActualValue('isuCd'));
    });
  }
  /*
   * if (_clientTierHandler && _clientTierHandler[0]) {
   * _clientTierHandler[0].onChange(); }
   */
}

var _isuHandler = null;
function onIsuChangeHandler() {
  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setISUCTCBasedScenarios();
    });
  }
}

function setISUCTCBasedScenarios() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  var clientTier = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');

  if (reqType == 'C' && cmrIssuingCntry == '862') {
    if (custSubGrp == 'BUSPR' || custSubGrp == 'XBP') {
      if (role == 'REQUESTER') {
        FormManager.readOnly('clientTier');
        FormManager.readOnly('salesBusOffCd');
      } else if (role == 'PROCESSOR') {
        FormManager.readOnly('isuCd');
        FormManager.enable('salesBusOffCd');
      }
    } else if ((custSubGrp == 'IBMEM' || custSubGrp == 'XINT' || custSubGrp == 'INTER' || custSubGrp == 'PRICU' || custSubGrp == 'XPC') && (role == 'REQUESTER' || role == 'PROCESSOR')) {
      FormManager.readOnly('clientTier');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('salesBusOffCd');
    } else if ((role == 'PROCESSOR' || role == 'REQUESTER') && custSubGrp == 'COMME' || custSubGrp == 'XINTS' || custSubGrp == 'GOVRN' || custSubGrp == 'THDPT' || custSubGrp == 'XGOV'
        || custSubGrp == 'XIGF' || custSubGrp == 'XTP' || custSubGrp == 'IGF') {
      if (isuCd == '34' && clientTier == 'Q') {
        FormManager.readOnly('salesBusOffCd');
      } else if (isuCd == '36') {
        FormManager.setValue('clientTier', 'Y');
        FormManager.enable('salesBusOffCd');
        FormManager.setValue('salesBusOffCd', 'A20');
      } else if (isuCd == '34') {
        FormManager.setValue('clientTier', 'Q');
        FormManager.readOnly('salesBusOffCd');
        FormManager.setValue('salesBusOffCd', 'A20');
      } else if (isuCd == '32') {
        FormManager.setValue('clientTier', 'T');
        FormManager.setValue('salesBusOffCd', 'A00');
        FormManager.enable('salesBusOffCd');
      } else {
        FormManager.setValue('clientTier', '');
      }
    }
  }
}

function mandatoryForBusinessPartnerCY() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    var _custType = FormManager.getActualValue('custSubGrp');
    if (_custType == 'BUSPR' || _custType == 'CRBUS') {
      FormManager.show('PPSCEID', 'ppsceid');
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.resetValidations('ppsceid');
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    }
  }
}

function addALPHANUMValidatorForEnterpriseNumber() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _number = FormManager.getActualValue('enterprise');
        if (_number && _number.length > 0 && !_number.match("^[0-9a-zA-Z]*$")) {
          return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise Number should be alphanumeric.');
        }
        return new ValidationResult(null, true, null);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addALPHANUMValidatorForTypeOfCustomer() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _value = FormManager.getActualValue('crosSubTyp');
        if (_value && _value.length > 0 && !_value.match("^[0-9a-zA-Z]*$")) {
          return new ValidationResult(FormManager.getField('crosSubTyp'), false, 'Type Of Customer should be alphanumeric.');
        }
        return new ValidationResult(null, true, null);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}
function addTRLandedCountryValidtor() {
  console.log("register addTRLandedCountryValidtor . . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                var reqType = FormManager.getActualValue('reqType');
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, genericMsg = 'Landed Country value of the Sold-to (Main) Address should not be "TR" for Cross-Border customers.';
                  if (addressItems != null && addressItems.length != 0) {
                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], landCtry = '', taxOffice = '', scenario = FormManager.getActualValue('custGrp');
                      var addrType = currentAddr.addrType[0];
                      landCtry = currentAddr.landCntry[0];
                      taxOffice = currentAddr.taxOffice[0];
                      console.log('addrType >> ' + addrType);
                      console.log('landCntry >> ' + landCtry);
                      if (addrType == 'ZS01' && landCtry == 'TR' && reqType == 'C' && scenario == 'CROSS') {
                        return new ValidationResult(null, false, genericMsg);
                      }
                      if (addrType == 'ZS01' && landCtry != 'TR' && reqType == 'C' && scenario == 'LOCAL') {
                        genericMsg = 'Landed Country value of the Sold-to (Main) Address should be "TR" for Local customers.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      if (addrType == 'ZP01' && reqType == 'C' && scenario == 'LOCAL' && (taxOffice == '' || taxOffice == null || taxOffice == undefined)) {
                        genericMsg = 'Tax office of the Local Language translation of Sold-to Address is required.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                    } // for
                  }
                } else {
                  console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR'); // validator body
}

function setClassificationCodeTR() {
  var field = FormManager.getField('custClass');
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.limitDropdownValues(field, [ '45', '46', '71' ]);
  } else if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.limitDropdownValues(field, [ '11', '13', '33', '35', '45', '46', '60', '71' ]);
  }
}

function setCOFClassificationCodeTR() {
  var cofVal = FormManager.getActualValue('commercialFinanced');
  var field = FormManager.getField('custClass');
  if (cofVal == 'R' || cofVal == 'S' || cofVal == 'T') {
    FormManager.limitDropdownValues(field, [ '11' ]);
  } else {
    setClassificationCodeTR();
  }
}

function setTypeOfCustomerClassificationCodeTR() {
  var typeOfCustomer = FormManager.getActualValue('crosSubTyp');
  var field = FormManager.getField('custClass');
  if (typeOfCustomer == 'G') {
    FormManager.limitDropdownValues(field, [ '13' ]);
  } else if (typeOfCustomer == 'BP') {
    FormManager.limitDropdownValues(field, [ '45', '46' ]);
  } else if (typeOfCustomer == '91') {
    FormManager.limitDropdownValues(field, [ '81' ]);
  } else {
    setClassificationCodeTR();
  }
}

function controlFieldsBySubScenarioTR(value) {
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType != 'C') {
    return;
  }
  if (reqType == 'C') {
    if (!value) {
      value = FormManager.getActualValue('custSubGrp');
    }
    FormManager.removeValidator('crosSubTyp', Validators.REQUIRED);
    // Control Type Of Customer
    if (value == 'BUSPR' || value == 'XBP') {
      FormManager.setValue('crosSubTyp', 'BP');
    } else if (value == 'XGOV' || value == 'GOVRN') {
      FormManager.setValue('crosSubTyp', 'G');
    } else if (value == 'INTER' || value == 'XINT') {
      FormManager.setValue('crosSubTyp', '91');
    } else if (value == 'COMME' || value == 'IGF' || value == 'OEM' || value == 'PRICU' || value == 'THDPT' || value == 'XINTS' || value == 'XPC' || value == 'XIBME' || value == 'XTP'
        || value == 'XIGF') {
      FormManager.setValue('crosSubTyp', '');
    }
  }
}

function lockFieldsIBMEm() {
  if (!(FormManager.getActualValue('custSubGrp') == 'IBMEM' && FormManager.getActualValue('userRole') == 'Requester')) {
    return;
  }
  FormManager.readOnly('isuCd');
  FormManager.readOnly('clientTier');
  FormManager.readOnly('custClass');
}

function setIsicClassificationCodeTR(value) {
  var field = FormManager.getField('custClass');
  if (!value) {
    value = FormManager.getActualValue('isicCd');
  }

  if (value == '9500') {
    FormManager.limitDropdownValues(field, [ '60', '71' ]);
  } else {
    setClassificationCodeTR();
  }
}

function validateSingleReactParentCMR() {

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking Inactive Parents of Single Reactivation CMR..');
        var reqType = FormManager.getActualValue('reqType');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        console.log('ReqType:' + reqType + ' <>cmrNo:' + cmrNo + ' <>cntry:' + cntry);
        if (reqType == 'X' && cmrNo) {
          var qParams = {
            COUNTRY : cntry,
            CMR_NO : cmrNo
          };
          var resultComp = cmr.query('LD.SINGLE_REACT_CHECK_ACTIVE_PARENT_COMPANY', qParams);

          var resultBill = cmr.query('LD.SINGLE_REACT_CHECK_ACTIVE_PARENT_BILLING', qParams);

          if (resultComp.ret1 != null && 'C' == resultComp.ret1) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'Parents CMR#' + resultComp.ret2 + ' is inactive So, first reactivate this.');
          } else if (resultBill.ret1 != null && ('C' == resultBill.ret1 && cmrNo != resultBill.ret2)) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'Parents CMR#' + resultBill.ret2 + ' is inactive So, first reactivate this.');
          } else {
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}
function addDPLCheckValidatorTR() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var result = FormManager.getActualValue('dplChkResult');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var reqType = FormManager.getActualValue('reqType');
        var role = null;
        if (typeof (_pagemodel) != 'undefined') {
          role = _pagemodel.userRole;
        }
        /*
         * if (CmrGrid.GRIDS.ADDRESS_GRID_GRID &&
         * CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) { var record = null;
         * var type = null; var updateInd = null; var updateIndCount = 0; for
         * (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
         * record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i); if (record ==
         * null && _allAddressData != null && _allAddressData[i] != null) {
         * record = _allAddressData[i]; } type = record.addrType; updateInd =
         * record.updateInd; if (typeof (type) == 'object') { type = type[0]; }
         * if (typeof (updateInd) == 'object') { updateInd = updateInd[0]; } if
         * (updateInd == 'U' || updateInd == 'N') { updateIndCount =
         * ++updateIndCount; } console.log('updateIndCount =' + updateIndCount);
         * console.log('updateInd =' + updateInd); if (updateInd == 'U' ||
         * updateInd == 'N') { if (role == 'Processor' && reqType == 'U') { if
         * (result == '' || result.toUpperCase() == 'NR') { return new
         * ValidationResult(null, false, 'DPL Check has not been performed
         * yet.'); } else if (result == '' || result.toUpperCase() == 'ALL
         * FAILED') { return new ValidationResult(null, false, 'DPL Check has
         * failed. This record cannot be processed.'); } else { return new
         * ValidationResult(null, true); } } } } }
         */
        console.log('result =' + result);
        console.log('reqType =' + reqType);
        console.log('role =' + role);
        // console.log('updateIndCount =' + updateIndCount);
        // console.log('updateInd =' + updateInd);
        if (role == 'Processor' && reqType == 'U') {
          if (result == '' || result.toUpperCase() == 'NOT DONE') {
            return new ValidationResult(null, false, 'DPL Check has not been performed yet.');
          } else if (result == '' || result.toUpperCase() == 'ALL FAILED') {
            return new ValidationResult(null, false, 'DPL Check has failed. This record cannot be processed.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function turkish(input) {
  var value = FormManager.getActualValue(input);
  if (!value || value == '' || value.length == 0) {
    return true;
  }
  // var reg =
  // /^[0-9ABDEFHJ-NPQRTV-Zabdefhj-npqrtv-zÇçĞğİıÖöŞşÜü\'\"\,\.\!\-\$\(\)\?\:\s|“|”|‘|’|！|＂|．|？|：|。|，]+/;
  var reg = /[a-zA-Z0-9ğüşöçİĞÜŞÖÇ]+/;
  if (!value.match(reg)) {
    return new ValidationResult(input, false, '{1} is not a valid value for {0}. Please enter turkish characters only.');
  } else {
    return new ValidationResult(input, true);
  }
}

function filterCmrnoForTR() {
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

function afterConfigForTR() {
  if (_landCntryHandler == null) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      setDistrictMandatoryTR();
      addTurkishCharValidator();
    });
  }
  // CREATCMR-788
  addressQuotationValidatorTR();
}

var _isScenarioChanged = false;
function checkScenarioChanged(fromAddress, scenario, scenarioChanged) {
  _isScenarioChanged = scenarioChanged;
  setSBOValuesForIsuCtc(FormManager.getActualValue('isuCd'));
  if (_isScenarioChanged) {
    setCustSubTypeBpGRTRCY();
  }
}

function handleClassCode() {
  var custType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType != 'C') {
    return;
  }
  if (custType == 'BUSPR' || custType == 'XBP' || custType == 'IBMEM') {
    FormManager.show('CustClass', 'custClass');
    FormManager.addValidator('custClass', Validators.REQUIRED, [ 'Classification Code' ], 'MAIN_CUST_TAB');
  } else {
    FormManager.hide('CustClass', 'custClass');
    FormManager.setValue('custClass', '');
    FormManager.resetValidations('custClass');
  }
}

function hideCollectionCode() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.hide('CollectionCd2', 'collectionCd2');
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

function setValuesWRTIsuCtc(ctc) {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var isu = FormManager.getActualValue('isuCd');
  if (ctc == null) {
    var ctc = FormManager.getActualValue('clientTier');
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (isu == '34' && ctc == 'Y') {
    FormManager.setValue('salesBusOffCd', 'A20');
    if (role == 'REQUESTER' || role == 'PROCESSOR') {
      FormManager.enable('salesBusOffCd');
    }
  } else if (isu == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.setValue('salesBusOffCd', 'A00');
    if (role == 'REQUESTER' || role == 'PROCESSOR') {
      FormManager.enable('salesBusOffCd');
    }
  }
}

// CREATCMR-4293
function setCTCValues() {

  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  // Business Partner
  if (custSubGrp == 'XBP' || custSubGrp == 'BUSPR') {
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '8B') {
      FormManager.setValue('clientTier', _pagemodel.clientTier == null ? '' : _pagemodel.clientTier);
      FormManager.readOnly('clientTier');
    }

  }

  // Internal
  if (custSubGrp == 'XINT' || custSubGrp == 'INTER') {
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '21') {
      FormManager.setValue('clientTier', _pagemodel.clientTier == null ? '' : _pagemodel.clientTier);
      FormManager.enable('clientTier');
    }
  }

}

function clientTierCodeValidator() {
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');

  if (isuCode == '34') {
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
      }, false, 'Client Tier can only accept value Q.');
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
      }, false, 'Client Tier can only accept value T.');
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
      }, false, 'Client Tier can only accept value Y.');
    }
  } else {
    if (clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');
      $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept blank.');
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
// creatcmr-6032
function vatValidatorTR() {
  console.log('>>Running VAT validation for Turkey.');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var custGrp = FormManager.getActualValue('custGrp');
        var vat = FormManager.getActualValue('vat');
        var reqId = FormManager.getActualValue('reqId');
        var cmrResult = FormManager.getActualValue('findCmrResult');
        var dnbResult = FormManager.getActualValue('findDnbResult');
        var districts = [ 'GAZIMAGUSA', 'GIRNE', 'GUZELYURT', 'YENI ISKELE', 'LEFKE', 'LEFKOSA' ];
        var soldToDistrict = null;
        var vatPattern = null;
        var vatDistrictChkUpdate = true;
        if (_allAddressData.length > 0) {
          for (var i = 0; i < _allAddressData.length; i++) {
            if (_allAddressData[i].addrType[0] == 'ZS01') {
              soldToDistrict = _allAddressData[i].dept[0];
            }
          }
        }
        var requestId = FormManager.getActualValue('reqId');
        qParams = {
          REQ_ID : requestId,
        };
        var result = cmr.query('GET.VAT_OLD_BY_REQID', qParams);
        var oldVAT = result.ret1;
        qParams = {
          REQ_ID : requestId,
        };
        var result = cmr.query('GET.OLD_ZS01_DEPT_BY_REQID', qParams);
        var oldZS01DEPT = result.ret1;

        if (result.ret1 == '' && result.ret1 != null) {
          vatDistrictChkUpdate = true;
        }
        if ((cmrResult != '' && cmrResult == 'Accepted') || (dnbResult != '' && cmrResult == 'Accepted')) {
          if (oldVAT == FormManager.getActualValue('vat') && oldZS01DEPT == soldToDistrict) {
            return new ValidationResult(null, true);
          }
        }
        if (cmrResult == 'No Results' || cmrResult == 'Rejected' || dnbResult == 'No Results' || dnbResult == 'Rejected') {
          if (vat == '') {
            return new ValidationResult(null, true);
          }
        }
        if (soldToDistrict != null && districts.includes(soldToDistrict.toUpperCase()) && (reqType == 'C' || (reqType == 'U' && vatDistrictChkUpdate))) {
          vatPattern = /^[0-9]{9}$/;
          if (vat.match(vatPattern) != null && vat.match(vatPattern).length > 0) {
            return new ValidationResult(null, true);
          } else {
            if (vat != '' || oldVAT != '' || (oldVAT != '' && oldZS01DEPT != soldToDistrict)) {
              return new ValidationResult({
                id : 'vat',
                type : 'text',
                name : 'vat'
              }, false, 'Format for VAT is incorrect. Correct format is 999999999.');
            }
          }
        } else {
          if (vat.match(/^[0-9]{10}$/) || vat.match(/^[0-9]{11}$/)) {
            return new ValidationResult(null, true);
          }
          if (vat != '' || oldVAT != '' || (oldVAT != '' && oldZS01DEPT != soldToDistrict)) {
            return new ValidationResult({
              id : 'vat',
              type : 'text',
              name : 'vat'
            }, false, 'Invalid VAT for TR. Length should be 10, or 11 characters long.');
          }
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}
function addressQuotationValidatorTR() {
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street Address' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Street Con\'t' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'District' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);
  FormManager.addValidator('taxOffice', Validators.NO_QUOTATION, [ 'Tax Office' ]);
}

dojo.addOnLoad(function() {
  GEOHandler.EMEA = [ SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.TURKEY, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.ITALY ];
  console.log('adding EMEA functions...');
  GEOHandler.addAddrFunction(addEMEALandedCountryHandler, GEOHandler.EMEA);
  GEOHandler.enableCopyAddress(GEOHandler.EMEA, validateEMEACopy, [ 'ZD01', 'CTYC' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.EMEA);
  // GEOHandler.addAfterConfig(addEMEAClientTierISULogic, [ SysLoc.ITALY]);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.EMEA);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAddrFunction(addPhoneValidatorEMEA, [ SysLoc.ISRAEL, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);

  // UKI Specific
  GEOHandler.addAfterConfig(afterConfigForUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterConfig(defaultCapIndicatorUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterConfig(addIEClientTierISULogic, [ SysLoc.IRELAND ]);
  GEOHandler.addAfterConfig(addUKClientTierISULogic, [ SysLoc.UK ]);
  GEOHandler.addAfterConfig(setAbbrevNmLocationLockAndMandatoryUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterConfig(lockCmrOwner, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(lockCmrOwner, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(addSBOSalesRepLogicIreland, [ SysLoc.IRELAND ]);
  // GEOHandler.addAfterConfig(addSBODependcyLogicUK, [ SysLoc.UK ]);
  // GEOHandler.addAfterConfig(addSalesRepDependcyLogicUK, [ SysLoc.UK ]);
  GEOHandler.addAfterConfig(addSalesRepLogicUK2018, [ SysLoc.UK ]);
  GEOHandler.addAfterConfig(set32SBOLogicOnFieldChange, [ SysLoc.UK ]);
  GEOHandler.registerValidator(addUKAddressTypeValidator, [ SysLoc.UK ], null, true);
  GEOHandler.registerValidator(addIRAddressTypeValidator, [ SysLoc.IRELAND ], null, true);
  // GEOHandler.registerValidator(addPostalCodeLengthValidator, [ SysLoc.IRELAND
  // ], null, true);
  // GEOHandler.registerValidator(addPostalCodeValidator, [ SysLoc.IRELAND,
  // SysLoc.UK ], null, true);
  GEOHandler.registerValidator(addPostCdCityValidator, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.addAddrFunction(addLatinCharValidatorUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  // GEOHandler.registerValidator(addAbbrevNmValidator, [ SysLoc.IRELAND,
  // SysLoc.UK ], null, true);
  GEOHandler.registerValidator(addNameContAttnDeptValidatorUKI, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.registerValidator(validateCompanyNoForUKI, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.UK, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.UK ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.IRELAND, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.IRELAND ], null, true);

  GEOHandler.registerValidator(addHwMstrInstFlgValidator, [ SysLoc.IRELAND, SysLoc.UK, SysLoc.SPAIN ], null, true);
  GEOHandler.addAddrFunction(checkHwMstrInstallFlag, [ SysLoc.IRELAND, SysLoc.UK, SysLoc.SPAIN ], null, true);
  GEOHandler.addAddrFunction(displayHwMstrInstallFlag, [ SysLoc.SPAIN, SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.addAfterTemplateLoad(displayHwMstInstallFlagNew, [ SysLoc.SPAIN, SysLoc.UK, SysLoc.IRELAND ]);

  GEOHandler.addAddrFunction(setUKIAbbrevNmLocnOnAddressSave, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterConfig(showDeptNoForInternalsOnlyUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterTemplateLoad(showDeptNoForInternalsOnlyUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.registerValidator(validateInternalDeptNumberLength, [ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.addAddrFunction(autoSetAbbrevLocnOnAddSaveUKI, [ SysLoc.IRELAND, SysLoc.UK ]);

  // Israel Specific
  GEOHandler.addAfterConfig(afterConfigForIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(addILClientTierISULogic, [ SysLoc.ISRAEL ]);
  GEOHandler.registerValidator(addILAddressTypeValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addILAddressTypeMailingValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addILAddressTypeBillingValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addShippingAddrTypeValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addILAddressTypeCntyAValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addILAddressTypeCntyBValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.ISRAEL, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addStreetAddressFormValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.addAfterConfig(updateEnterpriseNo, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(fieldsReadOnlyIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(fieldsReadOnlyIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(updateAbbrevNmLocnIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.registerValidator(addNameContAttnDeptValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.addAddrFunction(addLatinCharValidator, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(addNonLatinCharValidator, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(resetSubIndustryCd, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(removeValidationInacNac, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(disableCustPhone, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(postalCodeNumericOnlyForDomestic, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(setAbbrvLocCrossBorderScenario, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(setAbbrvLocCrossBorderScenarioOnChange, [ SysLoc.ISRAEL ]);

  // Turkey
  GEOHandler.addAfterConfig(setFieldsToReadOnlyGRCYTR, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setFieldsToReadOnlyGRCYTR, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(addrFunctionForGRCYTR, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(disableAddrFieldsGRCYTR, [ SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(preFillTranslationAddrWithSoldToForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(addTurkishCharValidator, [ SysLoc.TURKEY ]);
  GEOHandler.registerValidator(addTRAddressTypeValidator, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(vatValidatorTR, [ SysLoc.TURKEY ], null, true);
  // GEOHandler.registerValidator(addGenericVATValidator(SysLoc.TURKEY,
  // 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addDistrictPostCodeCityValidator, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addALPHANUMValidatorForEnterpriseNumber, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addALPHANUMValidatorForTypeOfCustomer, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addCustNm4ValidatorForTR, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addTRLandedCountryValidtor, [ SysLoc.TURKEY ], null, true);
  GEOHandler.addAfterConfig(salesSRforUpdate, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(salesSRforUpdateOnChange, [ SysLoc.TURKEY ]);
  // GEOHandler.registerValidator(addDPLCheckValidatorTR, [ SysLoc.TURKEY ],
  // null, true);
  GEOHandler.addAfterConfig(setClientTierValuesTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setClientTierValuesTR, [ SysLoc.TURKEY ]);

  GEOHandler.addAddrFunction(updateAbbrNameWithZS01TR, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(updateAbbrLocWithZS01TR, [ SysLoc.TURKEY ]);

  GEOHandler.addAfterConfig(showClassificationForTRUpd, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setSBOValuesForIsuCtc, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(checkScenarioChanged, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setSBOLogicOnISUChange, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setSBOLogicOnISUChange, [ SysLoc.TURKEY ]);
  // GEOHandler.addAfterTemplateLoad(setISUCTCBasedScenarios, [ SysLoc.TURKEY
  // ]);
  GEOHandler.addAfterTemplateLoad(setVatValidatorGRCYTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(toggleBPRelMemTypeForTurkey, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(controlFieldsBySubScenarioTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(toggleTypeOfCustomerForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(controlFieldsBySubScenarioTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(filterCmrnoForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(filterCmrnoForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(afterConfigForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(afterConfigForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(addLatinCharValidator, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(addISUHandler, [ SysLoc.TURKEY ]);

  // Greece
  GEOHandler.addAfterConfig(addHandlersForGRCYTR, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  // GEOHandler.addAfterConfig(setClientTierAndISR, [ SysLoc.GREECE,
  // SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(addVATDisabler, [ SysLoc.GREECE, SysLoc.CYPRUS ]);
  GEOHandler.addAfterConfig(hideMOPAFieldForGR, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(setTypeOfCustomerBehaviorForGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(disableAddrFieldsGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(addLatinCharValidator, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(addNonLatinCharValidator, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(addPOBoxValidatorGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(updateAddrTypeList, [ SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(convertToUpperCaseGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(preFillTranslationAddrWithSoldToForGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(updateAbbrevNmLocnGRCYTR, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.registerValidator(addGRAddressTypeValidator, [ SysLoc.GREECE ], null, true);
  GEOHandler.registerValidator(addOccupationPOBoxValidator, [ SysLoc.CYPRUS ], null, true);
  GEOHandler.registerValidator(addOccupationPOBoxAttnPersonValidatorForGR, [ SysLoc.GREECE ], null, true);
  GEOHandler.registerValidator(addStreetAddressFormValidatorGR, [ SysLoc.GREECE ], null, true);
  GEOHandler.registerValidator(addCrossLandedCntryFormValidatorGR, [ SysLoc.GREECE ], null, true);
  GEOHandler.addAfterConfig(clearPhoneNoFromGrid, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(clearPOBoxFromGrid, [ SysLoc.GREECE ]);
  GEOHandler.addAfterTemplateLoad(retainLandCntryValuesOnCopy, [ SysLoc.GREECE ]);

  // GEOHandler.registerValidator(addPostalCodeLenForTurGreCypValidator, [
  // SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ], null, true);
  // GEOHandler.addAddrFunction(setPostalCodeTurGreCypValidator, [
  // SysLoc.GREECE,
  // SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(abbrvLocMandatory, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(abbrvLocMandatoryOnChange, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.GREECE, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.GREECE ], null, true);
  GEOHandler.addAfterConfig(hideCustPhoneonSummary, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(addHandlerForCustSubTypeBpGRTRCY, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  // GEOHandler.addAfterTemplateLoad(setCustSubTypeBpGRTRCY, [ SysLoc.GREECE,
  // SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setISRValuesGROnUpdate, [ SysLoc.GREECE ]);

  // Cyprus
  GEOHandler.addAddrFunction(addLatinCharValidator, [ SysLoc.CYPRUS ]);
  GEOHandler.addAddrFunction(addNonLatinCharValidator, [ SysLoc.CYPRUS ]);
  GEOHandler.registerValidator(addCYAddressTypeValidator, [ SysLoc.CYPRUS ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.CYPRUS, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.CYPRUS ], null, true);

  // common greece/cyprus/turkey
  GEOHandler.addAfterConfig(setCommonCollectionCd, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(disableINACEnterpriseOnViewOnly, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(viewOnlyAddressDetails, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setVatValidatorGRCYTR, [ SysLoc.GREECE, SysLoc.TURKEY, ]);

  // common israel/greece/cyprus/turkey
  GEOHandler.addAfterConfig(defaultCapIndicator, [ SysLoc.ISRAEL, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);

  // Italy
  GEOHandler.checkRoleBeforeAddAddrFunction(addAddrValidationForProcItaly, [ SysLoc.ITALY ], null, GEOHandler.ROLE_PROCESSOR);
  // GEOHandler.addAddrFunction(autoSetAbbrevNmOnChanageIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(addCrossBorderValidatorForIT, [ SysLoc.ITALY ], null, true);
  // GEOHandler.addAfterConfig(autoSetSBOSROnPostalCode, [ SysLoc.ITALY ]);
  // GEOHandler.addAfterTemplateLoad(autoSetSBOSROnPostalCode, [ SysLoc.ITALY
  // ]);
  // GEOHandler.addAddrFunction(autoSetIdentClientForBillingIT, [
  // SysLoc.ITALY]);
  GEOHandler.registerValidator(addCompanyAddrValidator, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addBillingAddrValidator, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.ITALY, 'MAIN_CUST_TAB', 'frmCMR', 'ZP01'), [ SysLoc.ITALY ], null, true);
  // GEOHandler.addAddrFunction(autoSetVATForBilling, [ SysLoc.ITALY ]);
  // GEOHandler.addAddrFunction(addSMAndVAPostalCDValidator, [ SysLoc.ITALY ]);
  GEOHandler.addAddrFunction(addPOBOXValidatorEMEA, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.registerValidator(validateFiscalCodeForCreatesIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateLandCntryForCBCreatesIT, [ SysLoc.ITALY ], null, true);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.EMEA, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(checkIfVATFiscalUpdatedIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(validateVATFiscalLengthOnIdentIT, [ SysLoc.ITALY ]);
  // GEOHandler.registerValidator(validateSRForIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(addInstallingAddrValidator, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateEnterpriseNumForIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(checkIfStateProvBlankForProcIT, [ SysLoc.ITALY ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(stateProvValidatorCBforIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(validateSingleReactParentCMR, [ SysLoc.ITALY ], null, true);
  // CMR-2085 Turkey:CMR number: manual insertion for processor, validation
  // numeric, existing
  GEOHandler.registerValidator(validateCMRNumExistForTR, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(validateCMRNumberForTR, [ SysLoc.TURKEY ], null, true);
  GEOHandler.addAfterConfig(enableCMRNUMForPROCESSOR, [ SysLoc.TURKEY ]);

  // For EmbargoCode
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.EMEA);

  // For Legacy Direct
  GEOHandler.addAfterConfig(optionalFieldsForUpdateReqUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  /* 1808695 - DEV: Scenario Sub-types available for Cross Boarder Scenario Type */
  GEOHandler.registerValidator(addUKILandedCountryValidtor, [ SysLoc.UK ], null, true);
  GEOHandler.registerValidator(restrictDuplicateAddrUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.registerValidator(validateNumericValueUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterConfig(lockRequireFieldsUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterTemplateLoad(lockRequireFieldsUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.registerValidator(addStreetPoBoxValidatorUKI, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.registerValidator(addEmbargoCodeValidatorUKI, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.addAddrFunction(autoPopulateISUClientTierUK, [ SysLoc.UK ]);
  GEOHandler.addAddrFunction(disableAddrFieldsUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAddrFunction(optionalRulePostalCodeUK, [ SysLoc.UK ]);
  GEOHandler.addAddrFunction(setTaxCdBasedOnlandCntryUK, [ SysLoc.UK ]);
  GEOHandler.addAfterConfig(unlockINACForINTERUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterTemplateLoad(unlockINACForINTERUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  // GEOHandler.addAfterConfig(setSpecialTaxCodeOnScenarioOnChangeIT,
  // [SysLoc.ITALY]);
  GEOHandler.registerValidator(validateSBOForIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(validateCodiceDesIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.addAfterTemplateLoad(setIBMFieldsMandtOptional, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.registerValidator(validateCMRNumberForIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addEmbargoCodeValidatorIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateExistingCMRNo, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(checkIsicCodeValidationIT, [ SysLoc.ITALY ]);

  GEOHandler.addAfterConfig(addAfterConfigItaly, [ SysLoc.ITALY ]);
  GEOHandler.addAfterTemplateLoad(addAfterTemplateLoadItaly, [ SysLoc.ITALY ]);
  GEOHandler.addAddrFunction(addAddrFunctionItaly, [ SysLoc.ITALY ]);
  // CMR-2205
  GEOHandler.addAfterConfig(autoSetAbbrevNmOnChanageTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(autoSetAbbrevLocnOnChangeTR, [ SysLoc.TURKEY ]);

  // CMR-2688
  GEOHandler.addAfterConfig(setDefaultValueForPreferredLanguage, [ SysLoc.TURKEY ]);
  // CMR-1804 Turkey - Fields values validations - ISU Default on UI
  GEOHandler.addAfterConfig(setDefaultValueForISU, [ SysLoc.TURKEY ]);
  // CMR-2657
  GEOHandler.addAfterConfig(setSORTL, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(lockSORTL, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(lockSORTL, [ SysLoc.TURKEY ]);

  GEOHandler.addAfterConfig(setISUDefaultValueOnSubTypeChange, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setISUDefaultValueOnSubTypeChange, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(disableTaxOfficeTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(autoPopulateAbbNameCopySoldToTR, [ SysLoc.TURKEY ]);

  // CMR-2093

  GEOHandler.addAfterConfig(setFieldsBehaviourGR, [ SysLoc.GREECE ]);
  GEOHandler.addAfterTemplateLoad(setFieldsBehaviourGR, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(resetSubIndustryCdGR, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(showCommercialFinanced, [ SysLoc.TURKEY ]);

  // CYPRUS Legacy
  GEOHandler.addAfterConfig(mandatoryForBusinessPartnerCY, [ SysLoc.CYPRUS ]);
  GEOHandler.addAddrFunction(mandatoryForBusinessPartnerCY, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterTemplateLoad(mandatoryForBusinessPartnerCY, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterTemplateLoad(handleClassCode, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(hideCollectionCode, [ SysLoc.TURKEY ]);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [ SysLoc.TURKEY ], null, true);
  GEOHandler.addAfterTemplateLoad(addISUHandler, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(addISUHandler, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(lockFieldsIBMEm, [ SysLoc.TURKEY ]);

  // CREATCMR-4293
  GEOHandler.addAfterTemplateLoad(setCTCValues, [ SysLoc.TURKEY ]);
  GEOHandler.registerValidator(clientTierValidator, [ SysLoc.TURKEY ], null, true);
  GEOHandler.addAfterConfig(setEconomicCode, [ SysLoc.TURKEY ]);

  // CREATCMR-7984
  GEOHandler.addAfterConfig(onIsuChangeHandler, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setISUCTCBasedScenarios, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setISUCTCBasedScenarios, [ SysLoc.TURKEY ]);
});
