/* Register NORDX Javascripts */
//var _addrTypesForNORDX = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'ZP02' ];
var _addrTypesForNORDX = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02' ];
var _poBOXHandler = [];
var _MachineHandler = [];
// var _collCdArraySubTypes = [ 'INTER', 'INTSO', 'CBINT', 'CBISO' ];
// var EU_COUNTRIES = [ "AT", "BE", "BG", "HR", "CY", "CZ", "DE", "DK", "EE",
// "ES", "GL", "GR", "FI", "FO", "FR", "GB", "HU", "IE", "IT", "LT", "LV", "LU",
// "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK" ];
var EU_COUNTRIES = [ "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE" ];

var reqType = null;

function afterConfigForNORDX() {

  reqType = FormManager.getActualValue('reqType');
  var role = null;
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var vat = FormManager.getActualValue('vat');  
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  FormManager.readOnly('cmrOwner');
  FormManager.readOnly('subIndustryCd');// CMR-1993
  if (FormManager.getActualValue('countryUse') == '702') {
    FormManager.setValue('custPrefLang', 'U');
  }
  if (reqType == 'U') {
    if (FormManager.getActualValue('viewOnlyPage') == 'true') {
      cmr.hideNode("container-EngineeringBo"); // CREATCMR-2674
      cmr.hideNode("container-SalesBusOff"); // CMR-1650
      return;
    }

    if ((role == 'Requester' || role == 'Processor') && vat != '') {
      FormManager.readOnly('vat');
    }
    // FormManager.enable('collectionCd');
    FormManager.resetValidations('inacCd');
    // FormManager.resetValidations('sitePartyId');
    FormManager.resetValidations('engineeringBo');
  }

  if (reqType == 'C') {
    // FormManager.readOnly('collectionCd');
    FormManager.readOnly('capInd');
    // FormManager.readOnly('modeOfPayment');
    FormManager.setValue('capInd', true);
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'subIndustry Code' ], 'MAIN_CUST_TAB');// CMR-1993
    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB'); // CMR-1993
    if (role == 'Requester') {
      FormManager.readOnly('abbrevNm');
      FormManager.readOnly('abbrevLocn');
      FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    }
    if (role == 'Processor') {
      FormManager.enable('abbrevNm');
      FormManager.enable('repTeamMemberNo');
      FormManager.enable('engineeringBo');

      FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
      // CREATCMR-4293
      // FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client
      // Tier' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
      FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');

    }

    // if (custSubGrp.substring(2, 5) == 'INT' || custSubGrp.substring(2, 5) ==
    // 'BUS' || custSubGrp == 'BUSPR' || custSubGrp == 'INTER') {
    // FormManager.readOnly('engineeringBo');
    // FormManager.readOnly('repTeamMemberNo');
    // }

  }
  // if (reqType == 'C') {
  // if (!(_collCdArraySubTypes.indexOf(custSubGrp) > -1) &&
  // custSubGrp.substring(2, 5) != 'INT' && custSubGrp.substring(2, 5) != 'ISO')
  // {
  // FormManager.limitDropdownValues(FormManager.getField('collectionCd'), [ ''
  // ]);
  // } else {
  // FormManager.resetDropdownValues(FormManager.getField('collectionCd'), [ ''
  // ]);
  // }
  // }

  if (role == 'Processor' && reqType == 'C') {
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
    // CREATCMR-2674
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'SORTL' ], 'MAIN_IBM_TAB');
    // FormManager.addValidator('engineeringBo', Validators.REQUIRED, [ 'A/C
    // Admin DSC' ], 'MAIN_IBM_TAB'); // CMR-1903
  } else {
    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    // CREATCMR-2674
    FormManager.removeValidator('searchTerm', Validators.REQUIRED);
    // FormManager.removeValidator('engineeringBo', Validators.REQUIRED); //
    // CMR-1903
  }
  // EECRO
  var cntryBaltics = FormManager.getActualValue('countryUse');
  if ((role == 'Requester' || role == 'Processor') && (reqType == 'C' || reqType == 'U')) {
    if (cntryBaltics == '702EE' || cntryBaltics == '702LT' || cntryBaltics == '702LV') {
      checkAndAddValidator('custNm1', LATINBALTICS, [ 'Customer Name' ]);
      checkAndAddValidator('custNm2', LATINBALTICS, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('custNm3', LATINBALTICS, [ 'Additional Info' ]);
      checkAndAddValidator('custNm4', LATINBALTICS, [ 'Att. Person' ]);
      checkAndAddValidator('city1', LATINBALTICS, [ 'City' ]);
      checkAndAddValidator('addrTxt', LATINBALTICS, [ 'Street Address' ]);
      checkAndAddValidator('addrTxt2', LATINBALTICS, [ 'Street Con\'t' ]);
      checkAndAddValidator('custPhone', LATINBALTICS, [ 'Phone #' ]);
    }
  }

  var landCntry = '';
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion && cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }
  // FormManager.hide('StateProv', 'stateProv');
  // setVatValidatorNORDX();
  // setTaxCdValuesCROSS();
  setSBOForFinlandSubRegion();
  setPPSCEID();
  filterCmrnoP();
  // CMR-1650
  cmr.hideNode("container-SalesBusOff");

  // CREATCMR-1758
  vatInfoBubbleShowAndHide();

  // CREATCMR-1653
  currencyUIShowAndHide();

  // CREATCMR-1648
  setCollectionCd();

  // CREATCMR-1748&1744
  setTaxCdValues();
  setTaxCdValuesByCustSubGrp();

  // CREATCMR-1651
  setKukalValuesByCustSubGrp();
  
  // CREATCMR-1689
  setAbbreviatedNameValue();

  // CREATCMR-1638
  setModeOfPaymentValue();

  // CREATCMR-1690
  settingForProcessor();
  // vatAndVatExemptOnScenario();

  // CREATCMR-1656
  setCapRecordActivate();

  // CREATCMR-2144
  setCustPrefLangByCountry();
  setInacCd();

  // CREATCMR-2674
  setSortlLength();
  lockSalesRepAndSortl();
  hideACAdminDSC();
  
  //CREATCMR-788
  addressQuotationValidatorNORS();
}

function disableLandCntry() {
  var custType = FormManager.getActualValue('custGrp');
  if (FormManager.getActualValue('addrType') == 'ZS01') {
    var reqType = FormManager.getActualValue('reqType');
    if (custType == 'LOCAL' || custType.substring(2, 5) == 'LOC' || reqType == 'U') {
      FormManager.readOnly('landCntry');
    } else {
      FormManager.enable('landCntry');
    }
    if (custType == 'LOCAL' || custType.substring(2, 5) == 'LOC') {
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    }
  } else {
    FormManager.enable('landCntry');
  }
}

function addLandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
      var custType = FormManager.getActualValue('custGrp');
      if (custType == 'CROSS' || custType.substring(2, 5) == 'CRO') {
        FormManager.clearValue('landCntry');
      }
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
}

/**
 * Lock Embargo Code field
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

  // CREATCMR-1700
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    if (role == 'PROCESSOR') {
      embargoCodeValidator();
    }
  }

  if (reqType == 'U') {
    if (role == 'REQUESTER' || role == 'PROCESSOR') {
      FormManager.enable('embargoCd');
      embargoCodeValidator();
    }
  }
  // CREATCMR-1700

}

/**
 * After config handlers
 */
var _ISUHandler = null;
var _CTCHandler = null;
var _subIndCdHandler1 = null;
// var _SalesRepHandler = null;
var _AdminDSCHandler = null;
var _vatExemptHandler = null;
var _poSteertNorwayFin = null;
var _PostalCodeHandler = null;
var _ExpediteHandler = null;
var _ISICHandler = null; // CMR-1993
var _sortlHandler = null;
var sortlFlag = false;
var _landCntryHandler = null;
function addHandlersForNORDX() {

  // if (_ISUHandler == null) {
  // _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange',
  // function(value) {
  // setClientTierValues();
  // // CREATCMR-2674
  // console.log('_ISUHandler');
  // // setSalesRepValues();
  // // cleanupACdminDSAndSRValues();// CMR-1746
  // });
  // }

  // if (_CTCHandler == null) {
  // _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange',
  // function(value) {
  // // CREATCMR-2674
  // console.log('_CTCHandler');
  // // setSalesRepValues();
  // // cleanupACdminDSAndSRValues();// CMR-1746
  // });
  // }

  // if (_SalesRepHandler == null) {
  // _SalesRepHandler = dojo.connect(FormManager.getField('repTeamMemberNo'),
  // 'onChange', function(value) {
  // setAdminDSCValues(value);
  // });
  // }

  if (_PostalCodeHandler == null) {
    _PostalCodeHandler = dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
      setSBO(value);
    });
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      // setVatValidatorNORDX();
      resetVatRequired('', true);
    });
  }

  if (_ISICHandler == null) {
    _ISICHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      FormManager.readOnly('subIndustryCd'); // CMR-1993
    });
  }
  if (_subIndCdHandler1 == null) {
    _subIndCdHandler1 = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {

      if (!value) {
        return;
      }

      if (value != null && value.length > 1) {
        console.log('_subIndCdHandler1');
        // setSalesRepValues();
        setSRValuesBaseOnSubInd();
        FormManager.readOnly('subIndustryCd');// CMR-1993
      }

    });
  }

  // if (_poSteertNorwayFin == null) {
  // if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.NORWAY
  // || (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.FINLAND &&
  // FormManager.getActualValue('countryUse') == SysLoc.FINLAND)) {
  // _poSteertNorwayFin = dojo.connect(FormManager.getField('poBox'),
  // 'onChange', function(value) {
  // if (FormManager.getActualValue('poBox').length > 0 &&
  // FormManager.getActualValue('importInd') != 'Y') {
  // FormManager.disable('addrTxt');
  // FormManager.disable('addrTxt2');
  // } else {
  // FormManager.enable('addrTxt');
  // FormManager.enable('addrTxt2');
  // }
  //
  // });
  //
  if (_poSteertNorwayFin == null) {
    _poSteertNorwayFin = dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {
      streetContControll();
    });
  }
  // _poSteertNorwayFin = dojo.connect(FormManager.getField('addrTxt2'),
  // 'onChange', function(value) {
  //
  // if (FormManager.getActualValue('addrTxt2').length > 0 &&
  // FormManager.getActualValue('importInd') != 'Y') {
  // FormManager.disable('poBox');
  // } else {
  // if ((FormManager.getField('addrType_ZI01').checked ||
  // FormManager.getField('addrType_ZD01').checked)
  // && FormManager.getActualValue('importInd') != 'Y') {
  //
  // } else {
  // FormManager.enable('poBox');
  // }
  // }
  //
  // });
  // }
  // }

  if (_ExpediteHandler == null) {
    _ExpediteHandler = dojo.connect(FormManager.getField('expediteInd'), 'onChange', function(value) {
      setExpediteReason();
    });
  }
  
  if (_landCntryHandler == null) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      skipStateProvForFO();
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
function resetVatRequired(zs01Cntry, vatExemptChangedFlag) {
  var landCntry = FormManager.getActualValue('landCntry');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (vatExemptChangedFlag) {
      zs01Cntry = getLandedCountryByAddType('ZS01');
    }
    var custType = FormManager.getActualValue('custGrp');
    if (dijit.byId('vatExempt') != undefined && !dijit.byId('vatExempt').get('checked')
        && (GEOHandler.VAT_RQD_CROSS_LNDCNTRY.indexOf(zs01Cntry) >= 0 && landCntry != 'GB' || (custType != '' && custType.includes('LOC')))) {
      // checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
      FormManager.enable('vat');
    }
    if (dijit.byId('vatExempt') != undefined && !dijit.byId('vatExempt').get('checked') && (!GEOHandler.VAT_RQD_CROSS_LNDCNTRY.indexOf(zs01Cntry) >= 0 && (custType != '' && custType.includes('CRO')))) {
      FormManager.enable('vat');
    }
    if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt').get('checked')) {
      FormManager.setValue('vat', '');
      FormManager.readOnly('vat');
    }
  }
}
var _isScenarioChanged = false;
function checkScenarioChanged(fromAddress, scenario, scenarioChanged) {
  _isScenarioChanged = scenarioChanged;
}
function vatAndVatExemptOnScenario(value) {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    var custSubType = FormManager.getActualValue('custSubGrp');
    var isIBPriv = custSubType != '' && (custSubType.includes('IBM') || custSubType.includes('PRI'));
    var zs01Cntry = FormManager.getActualValue('cmrIssuingCntry');
    var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
      REQID : FormManager.getActualValue('reqId'),
      TYPE : 'ZS01'
    });
    if (ret && ret.ret1 && ret.ret1 != '') {
      zs01Cntry = ret.ret1;
    }
    var custType = FormManager.getActualValue('custGrp');
    if (GEOHandler.VAT_RQD_CROSS_LNDCNTRY.indexOf(zs01Cntry) >= 0 || (custType != '' && custType.includes('LOC'))) {
      FormManager.show('VATExempt', 'vatExempt');
      //FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      if (_isScenarioChanged) {
        if (isIBPriv) {
          FormManager.getField('vatExempt').set('checked', true);
        } else {
          FormManager.getField('vatExempt').set('checked', false);
        }
      }
    } else {
      if (FormManager.getField('vatExempt') != null) {
        FormManager.getField('vatExempt').set('checked', false);
        FormManager.hide('VATExempt', 'vatExempt');
        FormManager.removeValidator('vat', Validators.REQUIRED);
      }
    }
    resetVatRequired(zs01Cntry, false);
  } else if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.hide('VATExempt', 'vatExempt');
  }
}
function getLandedCountryByAddType(addType) {
  var landCountry = '';
  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
    var record = null;
    var type = null;
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      if (record == null && _allAddressData != null && _allAddressData[i] != null) {
        record = _allAddressData[i];
      }
      type = record.addrType;
      if (typeof (type) == 'object') {
        type = type[0];
      }
      if (type == addType) {
        landCountry = record.landCntry;
        if (typeof (landCountry) == 'object') {
          landCountry = landCountry[0];
        }
      }
    }
  }
  return landCountry;
}

/**
 * Set Client Tier Value
 */

function setClientTierValues() {
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  isuCtcVals = {
    '32' : 'T',
    '34' : 'Q',
    '36' : 'Y'
  };
  if (isuCd != null && isuCd != undefined && isuCd != '') {
    if (isuCtcVals.hasOwnProperty(isuCd)) {
      FormManager.setValue('clientTier', isuCtcVals[isuCd]);
      FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'ClientTier' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.setValue('clientTier', '');
      FormManager.removeValidator('clientTier', Validators.REQUIRED);
    }
  }
}

// function setClientTierValues(isuCd) {
// var reqType = FormManager.getActualValue('reqType');
// if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C')
// {
// return;
// }
//
// var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
// var countryUse = FormManager.getActualValue('countryUse');
// isuCd = FormManager.getActualValue('isuCd');
// var lockClientTier = false;
//
// if (cmrIssuingCntry == '678') {
// if ('19' == isuCd) {
// lockClientTier = true;
// }
// } else if (cmrIssuingCntry == '806' || countryUse == '702') {
// if ('04' == isuCd) {
// lockClientTier = true;
// }
// } else if (cmrIssuingCntry == '846') {
// if ([ '5E', '1R', '04' ].includes(isuCd)) {
// lockClientTier = true;
// }
// } else if (countryUse == '702EE' || countryUse == '702LT' || countryUse ==
// '702LV') {
// if ([ '21', '8B' ].includes(isuCd)) {
// lockClientTier = true;
// }
// }
// if (lockClientTier) {
// FormManager.removeValidator('clientTier', Validators.REQUIRED);
// FormManager.setValue('clientTier', '');
// FormManager.readOnly('clientTier');
// lockClientTier = false;
// } else {
// FormManager.enable('clientTier');
// }
// // var cntry = FormManager.getActualValue('cmrIssuingCntry');
// // var clientTiers = [];
// // if (isuCd != '') {
// // var qParams = {
// // _qall : 'Y',
// // ISSUING_CNTRY : cntry,
// // ISU : '%' + isuCd + '%'
// // };
// // var results = cmr.query('GET.CTCLIST.BYISU', qParams);
// // if (results != null) {
// // for (var i = 0; i < results.length; i++) {
// // clientTiers.push(results[i].ret1);
// // }
// // if (clientTiers != null) {
// // FormManager.limitDropdownValues(FormManager.getField('clientTier'),
// // clientTiers);
// // if (clientTiers.length == 1) {
// // FormManager.setValue('clientTier', clientTiers[0]);
// // }
// // }
// // }
// // }
// }
/**
 * NORDIX - CMR-1709
 */
function onSubIndustryChange() {
  console.log(">>>> onSubIndustryChange >>>>");
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    console.log(">>>> Exit onSubIndustChange for Update.");
    return;
  }

  if (_isScenarioChanged) {
    setSalesRepValues(value);
  }

  _subIndCdHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
    if (!value) {
      return;
    }
    if (value != null && value.length > 1) {
      setSRValuesBaseOnSubInd();
      // setSRValuesBaseOnSubIndOldLogic(value);
      FormManager.readOnly('subIndustryCd');// CMR-1993
    }
  });
  if (_subIndCdHandler && _subIndCdHandler[0]) {
    _subIndCdHandler[0].onChange();
  }
}

function addIsuHandler() {
  _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    setSalesRepValues(value);
    setClientTierValues();
    lockSalesRepAndSortl();
  });
}

function addCtcHandler() {
  _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
    setSalesRepValues(value);
    lockSalesRepAndSortl();
  });
}

/*
 * NORDX - sets Sales rep based on subIndustry Changed by CMR-1709
 */
function setSRValuesBaseOnSubIndOldLogic(subIndustry) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  if (FormManager.getActualValue('custSubGrp') == '' || subIndustry == '') {
    return;
  }
  var subIndPage = null;
  var subIndDB = null;
  if (typeof (_pagemodel) != 'undefined') {
    subIndPage = FormManager.getActualValue('subIndustryCd');
    subIndDB = _pagemodel.subIndustryCd;
  }

  if (subIndPage == subIndDB && subIndPage != null && subIndDB != null) {
    return;
  }

  var clientTier = FormManager.getActualValue('clientTier');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var isuCd = FormManager.getActualValue('isuCd');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
  if (isuCd != '') {
    var isuCtc = isuCd + clientTier;
    var ind = subIndustry.substring(0, 1);
    if (isuCtc == '34Q') {
      if (cntry == '678') {
        // DK/FO/GL
        var MSD993_34Q = "B,C,D,J,L,P,T,V";
        var MSD992_6644 = "G,Y,E,H,X,U";
        var MSD992_1375 = "A,K,F,N,S,Z";
        var MSD302_34Q = "M,R,W";
        if (ind != '') {
          if (MSD993_34Q.indexOf(ind) >= 0) {
            FormManager.setValue('repTeamMemberNo', "MSD993");
            FormManager.setValue('engineeringBo', '6880');
          } else if (MSD992_6644.indexOf(ind) >= 0) {
            FormManager.setValue('repTeamMemberNo', "MSD992");
            FormManager.setValue('engineeringBo', '6644');
          } else if (MSD992_1375.indexOf(ind) >= 0) {
            FormManager.setValue('repTeamMemberNo', "MSD992");
            FormManager.setValue('engineeringBo', '1375');
          } else if (MSD302_34Q.indexOf(ind) >= 0) {
            FormManager.setValue('repTeamMemberNo', "MSD302");
            FormManager.setValue('engineeringBo', '6881');
          }
        } else {
          FormManager.setValue('repTeamMemberNo', "MSD302");
          FormManager.setValue('engineeringBo', '6881');
        }
      } else if (cntry == '702' && geoCd == '') {
        // Finland
        var MSF107_6949 = "A,E,G,J,L,M,P,U,V,X,Y,Z";
        var MSF107_1379 = "B,D,R,T,W";
        var MSF702 = "C,F,H,K,N,S";
        if (MSF107_6949.indexOf(ind) >= 0) {
          FormManager.setValue('repTeamMemberNo', "MSF107");
          FormManager.setValue('engineeringBo', '6949');
        } else if (MSF107_1379.indexOf(ind) >= 0) {
          FormManager.setValue('repTeamMemberNo', "MSF107");
          FormManager.setValue('engineeringBo', '1379');
        } else if (MSF702.indexOf(ind) >= 0) {
          FormManager.setValue('repTeamMemberNo', "MSF702");
          FormManager.setValue('engineeringBo', '7864');
        }
      } else if (cntry == '846') {
        var MSS315 = "A,E,G,H,K,U,V,X,Y";
        var MSS596_1387 = "B,C,D,R,T,W,L,Z";
        var MSS596_6966 = "F,J,M,N,P,S,V";
        if (MSS315.indexOf(ind) >= 0) {
          FormManager.setValue('repTeamMemberNo', "MSS315");
          FormManager.setValue('engineeringBo', '6888');
        } else if (MSS596_1387.indexOf(ind) >= 0) {
          FormManager.setValue('repTeamMemberNo', "MSS596");
          FormManager.setValue('engineeringBo', '1387');
        } else if (MSS596_6966.indexOf(ind) >= 0) {
          FormManager.setValue('repTeamMemberNo', "MSS596");
          FormManager.setValue('engineeringBo', '6966');
        }
      }
    }
  }
}

/**
 * NORDIX - sets SBO based on Postal Code value
 */
function setSBO(postCd) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (cntry != SysLoc.NORWAY) {
    return;
  }
  postCd = document.getElementById('postCd').value;
  if (!postCd || postCd == '') {
    return;
  }
  if (postCd >= 7000 && postCd <= 9999) {
    FormManager.setValue('salesBusOffCd', '700');
  } else if (postCd >= 4000 && postCd <= 4999) {
    FormManager.setValue('salesBusOffCd', '400');
  } else if (postCd >= 5000 && postCd <= 6999) {
    FormManager.setValue('salesBusOffCd', '500');
  } else if (postCd >= 0 && postCd <= 3999) {
    FormManager.setValue('salesBusOffCd', '100');
  }
}

function setSBOFORCross() {

  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (cntry != SysLoc.NORWAY) {
    return;
  }

  var custGrp = FormManager.getActualValue('custGrp');
  if (custGrp == 'CROSS') {
    FormManager.setValue('salesBusOffCd', '100');
  }
}

/*
 * NORDX - sets Sales rep based on isuCtc Changed by CMR-1746
 */
function setSalesRepValuesOldLogic(clientTier) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var clientTier = FormManager.getActualValue('clientTier');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var ims = FormManager.getActualValue('subIndustryCd');
  var isuCd = FormManager.getActualValue('isuCd');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);

  if (isuCd != '') {
    var isuCtc = isuCd + clientTier;
    // SalRep for Denmark & its Subregion
    if (cntry == '678') {
      if (isuCtc == '34Q') {
        if (geoCd == 'IS') {
          FormManager.setValue('repTeamMemberNo', "MSD997");
          FormManager.setValue('engineeringBo', '1376');
        } else {
          // DK/FO/GL
          var MSD993_34Q = "B,C,D,J,L,P,T,V";
          var MSD992_6644 = "G,Y,E,H,X,U";
          var MSD992_1375 = "A,K,F,N,S,Z";
          var MSD302_34Q = "M,R,W";

          var ind = ims.substring(0, 1);
          if (ind != '') {
            if (MSD993_34Q.indexOf(ind) >= 0) {
              FormManager.setValue('repTeamMemberNo', "MSD993");
              FormManager.setValue('engineeringBo', '6880');
            } else if (MSD992_6644.indexOf(ind) >= 0) {
              FormManager.setValue('repTeamMemberNo', "MSD992");
              FormManager.setValue('engineeringBo', '6644');
            } else if (MSD992_1375.indexOf(ind) >= 0) {
              FormManager.setValue('repTeamMemberNo', "MSD992");
              FormManager.setValue('engineeringBo', '1375');
            } else if (MSD302_34Q.indexOf(ind) >= 0) {
              FormManager.setValue('repTeamMemberNo', "MSD302");
              FormManager.setValue('engineeringBo', '6881');
            }
          } else {
            FormManager.setValue('repTeamMemberNo', "MSD302");
            FormManager.setValue('engineeringBo', '6881');
          }
        }
      } else if (isuCtc == '047') {
        FormManager.setValue('repTeamMemberNo', "FSD001");
        FormManager.setValue('engineeringBo', '1243');
      } else if (isuCtc == '197') {
        FormManager.setValue('repTeamMemberNo', "APD200");
        FormManager.setValue('engineeringBo', '0139');
      } else if (isuCtc == '217' || isuCtc == '219') {
        FormManager.setValue('repTeamMemberNo', "NOREP9");
        FormManager.setValue('engineeringBo', '4680');
      } else if (isuCtc == '8B7') {
        FormManager.setValue('repTeamMemberNo', "NOREP9");
        FormManager.setValue('engineeringBo', '0070');
      }

    } else if (cntry == '702') {
      // SalRep for Finland & its Subregion
      if (isuCtc == '34Q') {
        if (geoCd == 'EE') {
          FormManager.setValue('engineeringBo', '4422');
          FormManager.setValue('repTeamMemberNo', "NOREP9");
        } else if (geoCd == 'LV') {
          FormManager.setValue('engineeringBo', '4390');
          FormManager.setValue('repTeamMemberNo', "NOREP9");
        } else if (geoCd == 'LT') {
          FormManager.setValue('engineeringBo', '4394');
          FormManager.setValue('repTeamMemberNo', "NOREP9");
        } else {
          // Finland
          var MSF107_6949 = "A,E,G,J,L,M,P,U,V,X,Y,Z";
          var MSF107_1379 = "B,D,R,T,W";
          var MSF702 = "C,F,H,K,N,S";

          var ind = ims.substring(0, 1);
          if (ind != '') {
            if (MSF107_6949.indexOf(ind) >= 0) {
              FormManager.setValue('repTeamMemberNo', "MSF107");
              FormManager.setValue('engineeringBo', '6949');
            } else if (MSF107_1379.indexOf(ind) >= 0) {
              FormManager.setValue('repTeamMemberNo', "MSF107");
              FormManager.setValue('engineeringBo', '1379');
            } else if (MSF702.indexOf(ind) >= 0) {
              FormManager.setValue('repTeamMemberNo', "MSF702");
              FormManager.setValue('engineeringBo', '7864');
            }
          }
        }
      } else if ((isuCtc == '217' || isuCtc == '8B7') && (geoCd == 'EE' || geoCd == 'LV' || geoCd == 'LT')) {
        FormManager.setValue('engineeringBo', '1640');
        FormManager.setValue('repTeamMemberNo', "NOREP9");
      } else if (isuCtc == '219' || isuCtc == '217') {
        FormManager.setValue('repTeamMemberNo', "NOREP9");
        FormManager.setValue('engineeringBo', '4710');
      } else if (isuCtc == '8B7') {
        FormManager.setValue('repTeamMemberNo', "NOREP9");
        FormManager.setValue('engineeringBo', '0070');
      }
    } else if (cntry == '806') {
      if ((isuCtc == '34Q')) {
        FormManager.setValue('repTeamMemberNo', "MSN502");
        FormManager.setValue('engineeringBo', '1383');
      } else if (isuCtc == '217') {
        FormManager.setValue('repTeamMemberNo', "NOREP9");
        FormManager.setValue('engineeringBo', '4900');
      } else if (isuCtc == '8B7') {
        FormManager.setValue('repTeamMemberNo', "NOREP9");
        FormManager.setValue('engineeringBo', '0070');
      }
    } else if (cntry == '846') {
      if ((isuCtc == '34Q')) {
        var MSS315 = "A,E,G,H,K,U,V,X,Y";
        var MSS596_1387 = "B,C,D,R,T,W,L,Z";
        var MSS596_6966 = "F,J,M,N,P,S,V";

        var ind = ims.substring(0, 1);
        if (ind != '') {
          if (MSS315.indexOf(ind) >= 0) {
            FormManager.setValue('repTeamMemberNo', "MSS315");
            FormManager.setValue('engineeringBo', '6888');
          } else if (MSS596_1387.indexOf(ind) >= 0) {
            FormManager.setValue('repTeamMemberNo', "MSS596");
            FormManager.setValue('engineeringBo', '1387');
          } else if (MSS596_6966.indexOf(ind) >= 0) {
            FormManager.setValue('repTeamMemberNo', "MSS596");
            FormManager.setValue('engineeringBo', '6966');
          }
        }
      } else if (isuCtc == '217') {
        FormManager.setValue('repTeamMemberNo', "NOREP9");
        FormManager.setValue('engineeringBo', '4990');
      } else if (isuCtc == '8B7') {
        FormManager.setValue('repTeamMemberNo', "NOREP9");
        FormManager.setValue('engineeringBo', '0070');
      } else if (isuCtc == '047') {
        FormManager.setValue('repTeamMemberNo', "NGA001");
      } else if (isuCtc == '1R7') {
        FormManager.setValue('repTeamMemberNo', "DIS001");
      }
    }
  }
}

/*
 * Set Admin DSC Values based on isuCtc and SalsRep Change by CMR-1746
 */
function setAdminDSCValues(repTeamMemberNo) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
  var ims = FormManager.getActualValue('subIndustryCd');

  var isuCtc = isuCd + clientTier;

  // SalRep for Denmark & its Subregion
  if (cntry == '678') {
    if (isuCtc == '34Q') {
      if (geoCd == 'IS') {
        FormManager.setValue('engineeringBo', '1376');
      } else {
        // DK/FO/GL
        var MSD992_6644 = "G,Y,E,H,X,U";
        var MSD992_1375 = "A,K,F,N,S,Z";

        var ind = ims.substring(0, 1);
        if (ind != '') {
          if (repTeamMemberNo == 'MSD993') {
            FormManager.setValue('engineeringBo', '6880');
          } else if (repTeamMemberNo == 'MSD992' && (MSD992_6644.indexOf(ind) >= 0)) {
            FormManager.setValue('engineeringBo', '6644');
          } else if (repTeamMemberNo == 'MSD992' && MSD992_1375.indexOf(ind) >= 0) {
            FormManager.setValue('engineeringBo', '1375');
          } else if (repTeamMemberNo == 'MSD302') {
            FormManager.setValue('engineeringBo', '6881');
          }
        } else {
          FormManager.setValue('engineeringBo', '6881');
        }
      }
    } else if (isuCtc == '047' && repTeamMemberNo == 'FSD001') {
      FormManager.setValue('engineeringBo', '1243');
    } else if (isuCtc == '197' && repTeamMemberNo == 'APD200') {
      FormManager.setValue('engineeringBo', '0139');
    } else if ((isuCtc == '217' || isuCtc == '219') && repTeamMemberNo == 'NOREP9') {
      FormManager.setValue('engineeringBo', '4680');
    } else if (isuCtc == '8B7' && repTeamMemberNo == 'NOREP9') {
      FormManager.setValue('engineeringBo', '0070');
    }

  } else if (cntry == '702') {
    // SalRep for Finland & its Subregion
    if (isuCtc == '34Q') {
      if (geoCd == 'EE') {
        FormManager.setValue('engineeringBo', '4422');
      } else if (geoCd == 'LV') {
        FormManager.setValue('engineeringBo', '4390');
      } else if (geoCd == 'LT') {
        FormManager.setValue('engineeringBo', '4394');
      } else {
        // Finland
        var MSF107_6949 = "A,E,G,J,L,M,P,U,V,X,Y,Z";
        var MSF107_1379 = "B,D,R,T,W";

        var ind = ims.substring(0, 1);
        if (ind != '') {
          if (MSF107_6949.indexOf(ind) >= 0 && repTeamMemberNo == 'MSF107') {
            FormManager.setValue('engineeringBo', '6949');
          } else if (MSF107_1379.indexOf(ind) >= 0 && repTeamMemberNo == 'MSF107') {
            FormManager.setValue('engineeringBo', '1379');
          } else if (repTeamMemberNo == 'MSF702') {
            FormManager.setValue('engineeringBo', '7864');
          }
        }
      }
    } else if ((isuCtc == '217' || isuCtc == '8B7') && (geoCd == 'EE' || geoCd == 'LV' || geoCd == 'LT')) {
      FormManager.setValue('engineeringBo', '1640');
    } else if (isuCtc == '219' || isuCtc == '217') {
      FormManager.setValue('engineeringBo', '4710');
    } else if (isuCtc == '8B7') {
      FormManager.setValue('engineeringBo', '0070');
    }
  } else if (cntry == '806') {
    if (isuCtc == '34Q' && repTeamMemberNo == 'MSN502') {
      FormManager.setValue('engineeringBo', '1383');
    } else if (isuCtc == '217' && repTeamMemberNo == 'NOREP9') {
      FormManager.setValue('engineeringBo', '4900');
    } else if (isuCtc == '8B7' && repTeamMemberNo == 'NOREP9') {
      FormManager.setValue('engineeringBo', '0070');
    }
  } else if (cntry == '846') {
    if ((isuCtc == '34Q')) {
      var MSS596_1387 = "B,C,D,R,T,W,L,Z";
      var MSS596_6966 = "F,J,M,N,P,S,V";

      var ind = ims.substring(0, 1);
      if (ind != '') {
        if (repTeamMemberNo == 'MSS315') {
          FormManager.setValue('engineeringBo', '6888');
        } else if (MSS596_1387.indexOf(ind) >= 0 && repTeamMemberNo == 'MSS596') {
          FormManager.setValue('engineeringBo', '1387');
        } else if (MSS596_6966.indexOf(ind) >= 0 && repTeamMemberNo == 'MSS596') {
          FormManager.setValue('engineeringBo', '6966');
        }
      }
    } else if (isuCtc == '217' && repTeamMemberNo == 'NOREP9') {
      FormManager.setValue('engineeringBo', '4990');
    } else if (isuCtc == '8B7' && repTeamMemberNo == 'NOREP9') {
      FormManager.setValue('engineeringBo', '0070');
    } else if (isuCtc == '047' && repTeamMemberNo == 'NGA001') {
      FormManager.setValue('engineeringBo', "3747");
    } else if (isuCtc == '1R7' && repTeamMemberNo == 'DIS001') {
      FormManager.setValue('engineeringBo', "1241");
    }
  }

  var cntryUse = FormManager.getActualValue('countryUse');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if ((custSubGrp == 'CBCOM' || custSubGrp == 'CBISO')) {
    if ((cntryUse == '678IS')) {
      FormManager.setValue('engineeringBo', '1376');
    } else if ((cntryUse == '702FI')) {
      FormManager.setValue('engineeringBo', '1379');
    } else if ((cntryUse == '702EE')) {
      FormManager.setValue('engineeringBo', '4422');
    } else if ((cntryUse == '702LT')) {
      FormManager.setValue('engineeringBo', '4394');
    } else if ((cntryUse == '702LV')) {
      FormManager.setValue('engineeringBo', '4390');
    }
  }
}

/*
 * Validate Admin DSC Values when it was filled
 */
function addACAdminValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var acAdmin = FormManager.getActualValue('engineeringBo');
        if (acAdmin != '' && acAdmin != null) {
          if (acAdmin.length != 4) {
            return new ValidationResult(null, false, 'A/C Adimn DSC should be exactly 4 digits long.');
          } else if (isNaN(acAdmin)) {
            return new ValidationResult(null, false, 'A/C Adimn DSC should be only numbers.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/*
 * Validate ISIC Values CMR-1993
 */
function addISICValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var isicCD = FormManager.getActualValue('isicCd');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var reqType = FormManager.getActualValue('reqType');
        var subCntryCustType = FormManager.getActualValue('custSubGrp').substring(2, 5);
        var kukla = FormManager.getActualValue('custClass');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');

        if (isicCD == '9500') {
          if (reqType == 'C') {
            if (((cntry == '806' || cntry == '846') && custSubType != 'PRIPE' && custSubType != 'IBMEM')
                || ((cntry == '678' || cntry == '702') && subCntryCustType != 'IBM' && subCntryCustType != 'PRI'))
              return new ValidationResult(null, false, 'ISIC value 9500 is not allowed for other scenario than Private Person and IBM Employee');
          }

          if (reqType == 'U' && kukla != '71' && kukla != '60') {
            return new ValidationResult(null, false, 'ISIC value 9500 can be entered only for CMR with KUKLA 60 (Private Person) and 71 (IBM Employee)');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

/*
 * clean up SalsRep and AC Admin DSC value Create by CMR-1746
 */
function cleanupACdminDSAndSRValues() {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  if (FormManager.getActualValue('custSubGrp') == '') {
    return;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  var isuCtc = isuCd + clientTier;
  var ISUCTCLIST = "";

  // SalRep for Denmark & its Subregion
  if (cntry == '678') {
    ISUCTCLIST = "34Q,047,197,217,219,8B7";
  } else if (cntry == '702') {
    ISUCTCLIST = "34Q,217,219,8B7";
  } else if (cntry == '806') {
    ISUCTCLIST = "34Q,217,8B7";
  } else if (cntry == '846') {
    ISUCTCLIST = "34Q,047,1R7,217,8B7";
  }

  if (ISUCTCLIST.indexOf(isuCtc) < 0) {
    FormManager.setValue('repTeamMemberNo', "");
    FormManager.setValue('engineeringBo', '');
  }
}

function setSBOForFinlandSubRegion() {

  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);

  if ((custSubScnrio == 'CBBUS' || custSubScnrio == 'CBCOM' || custSubScnrio == 'CBINT' || custSubScnrio == 'CBISO') && geoCd == 'FI') {
    FormManager.setValue('salesBusOffCd', '345');
  }
  if ((custSubScnrio == 'CBBUS' || custSubScnrio == 'CBCOM' || custSubScnrio == 'CBINT' || custSubScnrio == 'CBISO') && geoCd == 'EE') {
    FormManager.setValue('salesBusOffCd', '037');
  }

  if ((custSubScnrio == 'CBBUS' || custSubScnrio == 'CBCOM' || custSubScnrio == 'CBINT' || custSubScnrio == 'CBISO') && geoCd == 'LV') {
    FormManager.setValue('salesBusOffCd', '038');
  }

  if ((custSubScnrio == 'CBBUS' || custSubScnrio == 'CBCOM' || custSubScnrio == 'CBINT' || custSubScnrio == 'CBISO') && geoCd == 'LT') {
    FormManager.setValue('salesBusOffCd', '039');
  }

}

/*
 * Set TaxCode Values for EU and NON EU Countries
 */
function setTaxCdValuesCROSS() {
  var isEUCntry = false;
  var reqId = FormManager.getActualValue('reqId');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var countryUse = FormManager.getActualValue('countryUse');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYPE : 'ZS01',
  };
  var _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
  var landCntry = _result.ret2;

  if ((custSubGrp == 'CBBUS' || custSubGrp == 'CBCOM')) {

    if (EU_COUNTRIES.indexOf(landCntry) > 0) {
      isEUCntry = true;
    }

    if (cmrIssuingCntry == '702') {
      if (countryUse == '702EE' || countryUse == '702LT' || countryUse == '702LV') {
        FormManager.enable('taxCd1');
        FormManager.setValue('taxCd1', '00');
      } else if (isEUCntry == true && landCntry != null) {
        FormManager.enable('taxCd1');
        FormManager.setValue('taxCd1', '14');
      } else if (isEUCntry != true && landCntry != null) {
        FormManager.enable('taxCd1');
        FormManager.setValue('taxCd1', '20');
      }
    } else {
      if (countryUse == '678IS' || countryUse == '678GL' || countryUse == '678FO' || cmrIssuingCntry == '806') {
        FormManager.enable('taxCd1');
        FormManager.setValue('taxCd1', '00');
      } else if (isEUCntry == true && landCntry != null) {
        FormManager.enable('taxCd1');
        FormManager.setValue('taxCd1', '14');
      } else if (isEUCntry != true && landCntry != null) {
        FormManager.enable('taxCd1');
        FormManager.setValue('taxCd1', '20');
      }
    }

  }
}
function setAbbrevName(cntry, addressMode, saving, finalSave, force) {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
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
    // city1

    var addrType = FormManager.getActualValue('addrType');
    var cmpnyName = FormManager.getActualValue('custNm1');
    var city1 = FormManager.getActualValue('city1');
    var landCntry = FormManager.getActualValue('landCntry');
    var cntryRegion = FormManager.getActualValue('countryUse');
    var mscenario = FormManager.getActualValue('custGrp');
    var subScenraio = FormManager.getActualValue('custSubGrp');
    var scenario = null;
    if (mscenario == 'CROSS') {
      scenario = 'CROSS';
    } else if (mscenario == ((cntryRegion.substring(3, 5) + "CRO"))) {
      scenario = 'CROSS';
    }
    if (addrType == 'ZS01' || copyingToA) {

      if (cmpnyName.length > 22) {
        // FormManager.setValue('abbrevNm', cmpnyName.substring(0, 22));
      } else {
        // FormManager.setValue('abbrevNm', cmpnyName);
      }
      if (scenario == 'CROSS') {
        if (landCntry.length > 12) {
          FormManager.setValue('abbrevLocn', landCntry.substring(0, 12));
        } else {
          FormManager.setValue('abbrevLocn', landCntry);
        }
      } else if (subScenraio != 'SOFTL' && subScenraio.substring(2, 5) != 'SOF') {
        if (city1.length > 12) {
          FormManager.setValue('abbrevLocn', city1.substring(0, 12));
        } else {
          FormManager.setValue('abbrevLocn', city1);
        }
      }
    }
    // FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    // FormManager.readOnly('abbrevNm');
  }
}

// function machineValidator() {
// FormManager.addFormValidator((function() {
// return {
// validate : function() {
// var reqId = FormManager.getActualValue('reqId');
// var reqParam = {
// _qall : 'Y',
// REQ_ID : reqId,
// ADDR_TYPE : "ZP02",
// };
// var results = cmr.query('GET_ZP02_COUNT', reqParam);
// if (results != null) {
// for (var i = 0; i < results.length; i++) {
// var ADDR_SEQ1 = results[i].ret1;
// var reqParam1 = {
// REQ_ID : reqId,
// ADDR_TYPE : "ZP02",
// ADDR_SEQ : ADDR_SEQ1,
// };
// var newResults = cmr.query('ZP02_SEARCH_MACHINES', reqParam1);
// if (newResults.ret1 == '0') {
// return new ValidationResult(null, false, 'All Additional Installing Address
// should have at least one Machine');
// }
//
// }
// return new ValidationResult(null, true);
// } else {
// return new ValidationResult(null, true);
// }
// }
// };
// })(), 'MAIN_CUST_TAB', 'frmCMR');
//
// }

function handleMahcineModel() {
  // if (FormManager.getActualValue('cmrIssuingCntry') == '702' || reqType ==
  // 'C') {
  // cmr.hideNode("machineSerialDiv");
  // return;
  // }
  // if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
  // if (FormManager.getActualValue('addrType') == 'ZI01' ||
  // FormManager.getActualValue('addrType') == 'ZP02') {
  // cmr.showNode("machineSerialDiv");
  //
  // } else {
  // cmr.hideNode("machineSerialDiv");
  // }
  // }
  cmr.hideNode("machineSerialDiv");
}

/*
 * Mandatory addresses ZS01/ZP01/ZI01 *Billing (Sold-to) *Installing (Install
 * at) *Mailing - not flowing into RDC!!
 */
function addNORDXAddressTypeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Mailing, Billing address are mandatory. Only one address for each address type should be defined when sending for processing.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var installingCnt = 0;
          var billingCnt = 0;
          var mailingCnt = 0;
          var shippingCnt = 0;
          var eplCnt = 0;

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
              mailingCnt++;
            } else if (type == 'ZP01') {
              billingCnt++;
            } else if (type == 'ZI01') {
              installingCnt++;
            } else if (type == 'ZD01') {
              shippingCnt++;
            } else if (type == 'ZS02') {
              eplCnt++;
            }
          }
          var custSubType = FormManager.getActualValue('custSubGrp');
          var reason = FormManager.getActualValue('reqReason');
          if (reason != 'TREC' && (billingCnt == 0 || mailingCnt == 0)) {
            return new ValidationResult(null, false, 'Billing, Mailing address are mandatory.');
          } else if ((custSubType.includes('SO') || custSubType.includes('3PA') || custSubType == 'THDPT') && installingCnt == 0) {
            return new ValidationResult(null, false, 'For Internal SO and Third Party sub-scenarios installing address is mandatory. Please add it.');
          } else if (reason != 'TREC' && billingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Billing address can be defined. Please remove the additional Billing address.');
          } else if (reason != 'TREC' && mailingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Mailing address can be defined. Please remove the additional Mailing address.');
          } else if (reason != 'TREC' && eplCnt > 1) {
            return new ValidationResult(null, false, 'Only one EPL address can be defined. Please remove the additional EPL mailing address.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addNORDXInstallingShipping() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var shippingBool = true;
        var installBool = true;
        var eplBool = true;

        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            updateInd = record.updateInd;
            importInd = record.importInd;
            street = record.addrTxt;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (updateInd) == 'object') {
              updateInd = updateInd[0];
            }
            if (typeof (importInd) == 'object') {
              importInd = importInd[0];
            }
            if (typeof (street) == 'object') {
              street = street[0];
            }
            if ((street == '' || street == null || street == undefined) && !(updateInd != 'U' && importInd == 'Y')) {
              if (type == 'ZD01') {
                shippingBool = false;
              } else if (type == 'ZI01') {
                installBool = false;
              } else if (type == 'ZS02') {
                eplBool = false;
              }
            }
          }
        }

        var errMsg = '';
        if (!installBool) {
          errMsg += 'Installing';
        }
        if (!shippingBool) {
          errMsg += ' and Shipping';
        }
        if (!eplBool) {
          errMsg += ' and EPL';
        }
        errMsg = errMsg.replace(/^ and /, "");
        if (shippingBool && installBool && eplBool) {
          return new ValidationResult(null, true);
        } else {
          return new ValidationResult(null, false, 'Street should not be empty in ' + errMsg + ' Address.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addAbbrevNmLengthValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevName = FormManager.getActualValue('abbrevNm');
        if (reqType != 'U' && role == 'Requester' && _abbrevName.length > 22) {
          return new ValidationResult({
            id : 'abbrevNm',
            type : 'text',
            name : 'abbrevNm'
          }, false, 'The length for Abbreviated Name  should be 22 characters.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');

}

function addISUClientMandatory() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType == 'U') {
    return;
  }
  if (role == 'Processor') {
    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
    // CREATCMR-4293
    // FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client
    // Tier' ], 'MAIN_IBM_TAB');
  }
}

function handleMachineType() {
  // if (FormManager.getActualValue('cmrIssuingCntry') == '702' || reqType ==
  // 'C') {
  // cmr.hideNode("machineSerialDiv");
  // return;
  // }
  // if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
  // cmr.hideNode("machineSerialDiv");
  // for (var i = 0; i < _addrTypesForNORDX.length; i++) {
  // _MachineHandler[i] = null;
  // if (_MachineHandler[i] == null) {
  // var xx = FormManager.getField('addrType_ZP02');
  // _MachineHandler[i] = dojo.connect(FormManager.getField('addrType_' +
  // _addrTypesForNORDX[i]), 'onClick', function(value) {
  // if (FormManager.getField('addrType_ZI01').checked ||
  // FormManager.getField('addrType_ZP02').checked) {
  // cmr.showNode("machineSerialDiv");
  // cmr.hideNode('addMachineButton');
  //
  // } else {
  // cmr.hideNode("machineSerialDiv");
  // }
  // });
  // }
  // }
  // }
  //
  // if (cmr.addressMode == 'updateAddress') {
  // if (FormManager.getActualValue('addrType') == 'ZI01' ||
  // FormManager.getActualValue('addrType') == 'ZP02') {
  // cmr.showNode("machineSerialDiv");
  // cmr.showNode("addMachineButton");
  //
  // } else {
  // cmr.hideNode("machineSerialDiv");
  // }
  // }
  cmr.hideNode("machineSerialDiv");
}

function hidePOBoxandHandleStreet() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    for (var i = 0; i < _addrTypesForNORDX.length; i++) {
      _poBOXHandler[i] = null;
      if (_poBOXHandler[i] == null) {
        var poValue = FormManager.getActualValue('poBox');
        var phValue = FormManager.getActualValue('custPhone');
        _poBOXHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForNORDX[i]), 'onClick', function(value) {
          setPOBOXandSteet(poValue);
          setPhone(phValue);
          disableLandCntry();
        });
      }
    }
  }
  if (cmr.addressMode == 'updateAddress') {
    if (FormManager.getActualValue('addrType') == 'ZI01' || FormManager.getActualValue('addrType') == 'ZD01' || FormManager.getActualValue('addrType') == 'ZS02') {
      FormManager.hide('POBox', 'poBox');
      FormManager.setValue('poBox', '');
      var cntryRegion = FormManager.getActualValue('countryUse');
      // if (cntryRegion != '' && (cntryRegion == '678FO' || cntryRegion ==
      // SysLoc.DENMARK)) {
      FormManager.addValidator('addrTxt', Validators.REQUIRED, [ 'Street' ], '');
      // }
    } else {
      FormManager.show('POBox', 'poBox');
      FormManager.resetValidations('addrTxt');
    }
    if (FormManager.getActualValue('addrType') == 'ZS01') {
      FormManager.show('CustPhone', 'custPhone');
    } else {
      FormManager.hide('CustPhone', 'custPhone');
      FormManager.setValue('custPhone', '');

    }
  }
  var cntryBalticsUpd = FormManager.getActualValue('countryUse')
  if (cntryBalticsUpd == '702EE' || cntryBalticsUpd == '702LT' || cntryBalticsUpd == '702LV') {
    checkAndAddValidator('custNm1', LATINBALTICS, [ 'Customer Name' ]);
    checkAndAddValidator('custNm2', LATINBALTICS, [ 'Customer Name Con\'t' ]);
    checkAndAddValidator('custNm3', LATINBALTICS, [ 'Additional Info' ]);
    checkAndAddValidator('custNm4', LATINBALTICS, [ 'Att. Person' ]);
    checkAndAddValidator('city1', LATINBALTICS, [ 'City' ]);
    checkAndAddValidator('addrTxt', LATINBALTICS, [ 'Street Address' ]);
    checkAndAddValidator('addrTxt2', LATINBALTICS, [ 'Street Con\'t' ]);
    checkAndAddValidator('custPhone', LATINBALTICS, [ 'Phone #' ]);
  }
}

function setPOBOXandSteet(value) {
  if ((FormManager.getField('addrType_ZI01') != undefined && FormManager.getField('addrType_ZI01').checked)
      || (FormManager.getField('addrType_ZD01') != undefined && FormManager.getField('addrType_ZD01').checked)
      || (FormManager.getField('addrType_ZS02') != undefined && FormManager.getField('addrType_ZS02').checked)) {
    // FormManager.disable('poBox');
    // FormManager.setValue('poBox', '');
    FormManager.hide('POBox', 'poBox');
    FormManager.setValue('poBox', '');
    var cntryRegion = FormManager.getActualValue('countryUse');
    // if (cntryRegion != '' && (cntryRegion == '678FO' || cntryRegion ==
    // SysLoc.DENMARK)) {
    FormManager.addValidator('addrTxt', Validators.REQUIRED, [ 'Street' ], '');
    // }

  } else {
    // FormManager.enable('poBox');
    // FormManager.setValue('poBox', value);
    FormManager.show('POBox', 'poBox');
    FormManager.resetValidations('addrTxt');
  }
  var cntryBalticsM = FormManager.getActualValue('countryUse')
  if (cntryBalticsM == '702EE' || cntryBalticsM == '702LT' || cntryBalticsM == '702LV') {
    checkAndAddValidator('custNm1', LATINBALTICS, [ 'Customer Name' ]);
    checkAndAddValidator('custNm2', LATINBALTICS, [ 'Customer Name Con\'t' ]);
    checkAndAddValidator('custNm3', LATINBALTICS, [ 'Additional Info' ]);
    checkAndAddValidator('custNm4', LATINBALTICS, [ 'Att. Person' ]);
    checkAndAddValidator('city1', LATINBALTICS, [ 'City' ]);
    checkAndAddValidator('addrTxt', LATINBALTICS, [ 'Street Address' ]);
    checkAndAddValidator('addrTxt2', LATINBALTICS, [ 'Street Con\'t' ]);
    checkAndAddValidator('custPhone', LATINBALTICS, [ 'Phone #' ]);
  }
}

function setPhone(value) {
  if (FormManager.getField('addrType_ZS01').checked) {
    FormManager.show('CustPhone', 'custPhone');
    FormManager.setValue('custPhone', value);
  } else {
    FormManager.hide('CustPhone', 'custPhone');
    FormManager.setValue('custPhone', '');
  }
}

function validateNORDXCopy(addrType, arrayOfTargetTypes) {
  return null;
}

function addAddressFieldValidators() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var addrTxt = FormManager.getActualValue('addrTxt');
        var addrTxt2 = FormManager.getActualValue('addrTxt2');

        var val = addrTxt;
        if (val.length > 30) {
          return new ValidationResult(null, false, 'Street should not exceed 30 characters.');
        }
        // if (addrTxt2 != '') {
        // val += addrTxt2;
        // if (val.length > 30) {
        // return new ValidationResult(null, false, 'Total computed length of
        // Street and Street Con\'t should not exceed 30 characters.');
        // }
        // } else {
        // if (val.length > 30) {
        // return new ValidationResult(null, false, 'Street should not exceed 30
        // characters.');
        // }
        // }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // city and postal code length
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var city = FormManager.getActualValue('city1');
        var postCd = FormManager.getActualValue('postCd');

        var val = city;
        if (city != '') {
          val += postCd;
          if (val.length > 29) {
            return new ValidationResult(null, false, 'Total computed length of City and Postal Code should not exceed 29 characters.');
          }
        } else {
          if (val.length > 29) {
            return new ValidationResult(null, false, 'City should not exceed 30 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // Name Con't and Attention person ( 1 out of 2) Defect 1609336 fix
  // FormManager
  // .addFormValidator(
  // (function() {
  // return {
  // validate : function() {
  // var showError = false;
  //
  // if (FormManager.getActualValue('custNm1') != '' &&
  // FormManager.getActualValue('custNm2') != ''
  // && FormManager.getActualValue('custNm4') != '' &&
  // FormManager.getActualValue('addrTxt') != ''
  // && FormManager.getActualValue('poBox') != '' &&
  // FormManager.getActualValue('postCd') != ''
  // && FormManager.getActualValue('city1') != '') {
  // showError = true;
  // } else {
  // showError = false;
  // }
  //
  // var cntryRegion = FormManager.getActualValue('countryUse');
  // if (cntryRegion != ''
  // && (cntryRegion == '678FO' || cntryRegion == '678GL' || cntryRegion ==
  // '678IS' || cntryRegion == '702EE'
  // || cntryRegion == '702LT' || cntryRegion == '702LV')) {
  //
  // if (showError) {
  // return new ValidationResult(null, false, 'For Customer Name Con\'t and Att.
  // Person, only one can be filled.');
  // }
  // } else {
  // var cntry = FormManager.getActualValue('cmrIssuingCntry');
  //
  // if (cntry == SysLoc.SWEDEN || cntry == SysLoc.NORWAY || cntry ==
  // SysLoc.DENMARK || cntry == SysLoc.FINLAND) {
  //
  // var landCntry = FormManager.getActualValue('landCntry');
  //
  // if (cntry == SysLoc.SWEDEN && landCntry != "SE") {
  // if (showError) {
  // return new ValidationResult(null, false, 'For Customer Name Con\'t and Att.
  // Person, only one can be filled.');
  // }
  // }
  // if (cntry == SysLoc.NORWAY && landCntry != "NO") {
  // if (showError) {
  // return new ValidationResult(null, false, 'For Customer Name Con\'t and Att.
  // Person, only one can be filled.');
  // }
  // }
  // if (cntry == SysLoc.DENMARK && landCntry != "DK") {
  // if (showError) {
  // return new ValidationResult(null, false, 'For Customer Name Con\'t and Att.
  // Person, only one can be filled.');
  // }
  // }
  // if (cntry == SysLoc.FINLAND && landCntry != "FI") {
  // if (showError) {
  // return new ValidationResult(null, false, 'For Customer Name Con\'t and Att.
  // Person, only one can be filled.');
  // }
  // }
  //
  // }
  //
  // }
  //
  // return new ValidationResult(null, true);
  // }
  // };
  // })(), null, 'frmCMR_addressModal');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var showError = false;
        var count = 0;
        if (FormManager.getActualValue('custNm2') != '') {
          count++
        }
        if (FormManager.getActualValue('custNm3') != '') {
          count++
        }
        if (FormManager.getActualValue('custNm4') != '') {
          count++
        }
        if (FormManager.getActualValue('poBox') != '' || FormManager.getActualValue('addrTxt2') != '') {
          count++
        }
        if (FormManager.getActualValue('addrTxt') != '') {
          count++
        }

        var cntryRegion = FormManager.getActualValue('countryUse');
        var cntryRegionSubString = cntryRegion.slice(-2);
        var landCntry = FormManager.getActualValue('landCntry');
        if (cntryRegion == '678FO' || cntryRegion == '678GL' || cntryRegion == '678IS') {
          if (count > 3) {
            return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX only 3 fields can be filled at the same time.');
          }
        } else {
          if (cntryRegion != '' && cntryRegion.length > 3 && cntryRegionSubString == landCntry && count > 4) {
            return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX only 4 fields can be filled at the same time.');
          }
          if (cntryRegion != '' && cntryRegion.length > 3 && cntryRegionSubString != landCntry && count > 3) {
            return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX only 3 fields can be filled at the same time.');
          }
        }
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (cntryRegion == '' || cntryRegion.length == 3) {
          if (cntry == SysLoc.SWEDEN) {
            if (landCntry != "SE" && count > 3) {
              return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX only 3 fields can be filled at the same time.');
            } else if (landCntry == "SE" && count > 4) {
              return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX 4 fields can be filled at the same time.');
            }
          }
          if (cntry == SysLoc.NORWAY) {
            if (landCntry != "NO" && count > 3) {
              return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX only 3 fields can be filled at the same time.');
            } else if (landCntry == "NO" && count > 4) {
              return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX only 4 fields can be filled at the same time.');
            }
          }
          if (cntry == SysLoc.DENMARK) {
            if (landCntry != "DK" && count > 3) {
              return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX only 3 fields can be filled at the same time.');
            } else if (landCntry == "DK" && count > 4) {
              return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX only 4 fields can be filled at the same time.');
            }
          }
          if (cntry == SysLoc.FINLAND) {
            if (landCntry != "FI" && count > 3) {
              return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX only 3 fields can be filled at the same time.');
            } else if (landCntry == "FI" && count > 4) {
              return new ValidationResult(null, false, 'Customer name con\'t, Additional info, Att. Person, Street and Street Con\'t and/or PO BOX only 4 fields can be filled at the same time.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
  // Street and PO BOX DENMARK and FO
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if ((cntry == SysLoc.DENMARK || cntry == SysLoc.FINLAND) && (FormManager.getActualValue('addrType') == 'ZS01' || FormManager.getActualValue('addrType') == 'ZP01')) {
          var cntryRegion = FormManager.getActualValue('countryUse');
          if (cntryRegion != '' && (cntryRegion != SysLoc.FINLAND)) {

            var addrFldCnt = 0;
            if (FormManager.getActualValue('poBox') != '') {
              addrFldCnt++;
            }
            if (FormManager.getActualValue('addrTxt') != '') {
              addrFldCnt++;
            }
            /*
             * if (dojo.byId('poBox').getAttribute('aria-readonly') == 'true') {
             * if (addrFldCnt < 1) { return new ValidationResult({ id :
             * 'addrTxt', }, false, ''); } }
             */
            if (addrFldCnt < 1) {
              return new ValidationResult(null, false, 'For Street and PO BOX, atleast one should be filled.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // ALL NORDICS POBOX
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var cntry = FormManager.getActualValue('landCntry');
        if (cntry != '') {
          var POBox = FormManager.getActualValue('poBox');
          if (isNaN(POBox)) {
            return new ValidationResult(null, false, 'PO BOX should be Numeric.');
          }

        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
  // Additional info and Street Con't can't be filled at the same time
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var addrTxt2 = FormManager.getActualValue('addrTxt2');
        var additionalInfo = FormManager.getActualValue('custNm3');
        if (addrTxt2 != '' && additionalInfo != '') {
          return new ValidationResult(null, false, 'Additional info and Street Con\'t can\'t be filled at the same time.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
  // if both street con't and PO BOX are filled computed length must be 30
  // characters
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var addrtxt2Length = FormManager.getActualValue('addrTxt2').length;
        var poBoxLength = FormManager.getActualValue('poBox').length;
        var totalLength = addrtxt2Length + poBoxLength;
        if ((poBoxLength > 0 && addrtxt2Length > 0 && totalLength > 21) || (poBoxLength > 0 && !addrtxt2Length > 0 && totalLength > 23) || (!poBoxLength > 0 && totalLength > 30)) {
          return new ValidationResult(null, false, 'Total computed length of PO BOX (including PO BOX prefix) and Street Con\'t should not exceed limitation.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // Machine Type and Serial Number
  // FormManager.addFormValidator((function() {
  // return {
  // validate : function() {
  //
  // if (FormManager.getActualValue('machineTyp').length == 0 &&
  // FormManager.getActualValue('machineSerialNo').length == 0) {
  // if (cmr.addressMode == 'updateAddress') {
  // if (FormManager.getActualValue('addrType') == 'ZP02') {
  // var qParams = {
  // _qall : 'Y',
  // REQ_ID : FormManager.getActualValue('reqId'),
  // ADDR_TYPE : FormManager.getActualValue('addrType'),
  // ADDR_SEQ : FormManager.getActualValue('addrSeq'),
  // };
  // var results = cmr.query('SEARCH_MACHINES', qParams);
  // if (results != null) {
  // if (results.length > 0) {
  // return new ValidationResult(null, true);
  // } else {
  // return new ValidationResult({
  // id : 'machineTyp',
  // }, false, 'Machine Type and Serial Number are Mandatory.');
  // }
  // } else {
  // return new ValidationResult({
  // id : 'machineTyp',
  // }, false, 'Machine Type and Serial Number are Mandatory.');
  // }
  // } else {
  //
  // return new ValidationResult(null, true);
  // }
  //
  // } else if (FormManager.getActualValue('addrType') == 'ZP02' &&
  // (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress')) {
  // // FormManager.addValidator('machineTyp', Validators.REQUIRED, [
  // // 'Machine Type' ], '');
  // // FormManager.addValidator('machineSerialNo', Validators.REQUIRED,
  // // [ 'Machine Serial Number' ], '');
  // return new ValidationResult({
  // id : 'machineTyp',
  // }, false, 'Machine Type and Serial Number are Mandatory.');
  // }
  //
  // }
  // if ((FormManager.getActualValue('addrType') == 'ZP02') && reqType != 'C') {
  //
  // if (FormManager.getActualValue('machineTyp').length != 4 &&
  // FormManager.getActualValue('machineSerialNo').length != 7) {
  // return new ValidationResult({
  // id : 'machineTyp',
  // }, false, 'Machine Type and Serial number should be 4 and 7 characters
  // long.');
  // }
  // if (FormManager.getActualValue('machineTyp').length != 4) {
  // return new ValidationResult({
  // id : 'machineTyp',
  // }, false, 'Machine Type should be 4 characters long.');
  // }
  // if (FormManager.getActualValue('machineSerialNo').length != 7) {
  // return new ValidationResult({
  // id : 'machineSerialNo',
  // }, false, 'Machine Serial Number should be 7 characters long.');
  // }
  // }
  //
  // return new ValidationResult(null, true);
  // }
  // };
  // })(), null, 'frmCMR_addressModal');

  // FormManager.addFormValidator((function() {
  // return {
  // validate : function() {
  // var reqId = FormManager.getActualValue('reqId');
  // var addrType = FormManager.getActualValue('addrType');
  // var reqType = FormManager.getActualValue('reqType');
  // var addrSeq = FormManager.getActualValue('addrSeq');
  //
  // if (addrSeq != null && addrType == 'ZP02' && reqType != 'C') {
  // var reqParam = {
  // REQ_ID : reqId,
  // ADDR_TYPE : "ZP02",
  // ADDR_SEQ : addrSeq,
  // };
  // var results = cmr.query('ZP02_SEARCH_MACHINES', reqParam);
  // if (results.ret1 == '0') {
  // return new ValidationResult(null, false, 'All Additional Installing Address
  // should have at least one entry of Machine details');
  // }
  // return new ValidationResult(null, true);
  // }
  // return new ValidationResult(null, true);
  // }
  // };
  // })(), null, 'frmCMR_addressModal');
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

function updateAddrTypeList(cntry, addressMode) {
  // var cntryRegion = FormManager.getActualValue('countryUse');
  // if (cntryRegion == '702') {
  // if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
  // cmr.hideNode('radiocont_ZD01');
  // }
  // }
  // if ((addressMode == 'newAddress' || addressMode == 'copyAddress') &&
  // cmr.currentRequestType == 'C') {
  // cmr.hideNode('radiocont_ZP02');
  // }
  cmr.hideNode('radiocont_ZP02');
}

function addCrossBorderValidatorNORS() {
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

function setAbbrvNmLoc() {
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
      ADDR_TYPE : "ZS01",
    };
  }
  var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
  var city;
  var abbrevLocn = null;
  var abbrvNm = custNm.ret1;
  var cntryRegion = FormManager.getActualValue('countryUse');
  var mscenario = FormManager.getActualValue('custGrp');
  var subScenraio = FormManager.getActualValue('custSubGrp');
  var scenario = null;
  if (mscenario == 'CROSS') {
    scenario = 'CROSS';
  } else if (mscenario == ((cntryRegion.substring(3, 5) + "CRO"))) {
    scenario = 'CROSS';
  }
  if (scenario == 'CROSS') {
    city = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', reqParam);
    abbrevLocn = city.ret1;
    if (abbrevLocn != null && abbrevLocn.length > 12) {
      abbrevLocn = abbrevLocn.substring(0, 12);
    }
  } else {
    city = cmr.query('ADDR.GET.CITY1.BY_REQID', reqParam);
    abbrevLocn = city.ret1;
  }

  if (abbrvNm && abbrvNm.length > 22) {
    abbrvNm = abbrvNm.substring(0, 22);
  }
  if (abbrevLocn && abbrevLocn.length > 12) {
    abbrevLocn = abbrevLocn.substring(0, 12);
  }
  if (abbrevLocn != null && (subScenraio != 'SOFTL' && subScenraio.substring(2, 5) != 'SOF')) {
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }
  if (abbrvNm != null) {
    // FormManager.setValue('abbrevNm', abbrvNm);
  }
}

/* Machines Scripts on Address Tab */

var _currentMachineProcess = '';

/**
 * Refreshes the machine list
 * 
 * @param cntry
 * @param addressMode
 * @param saving
 * @param afterValidate
 */
function loadMachinesList(cntry, addressMode, saving, afterValidate) {
  if (!saving) {
    CmrGrid.refresh('MACHINES_GRID', cmr.CONTEXT_ROOT + '/request/address/machines/list.json', 'reqId=:reqId&addrType=:addrType&addrSeq=:addrSeq');
  }
}
/**
 * Does the actual adding to the list
 */
function doAddMachines() {
  var mType = FormManager.getActualValue('machineTyp');
  var mSerial = FormManager.getActualValue('machineSerialNo');
  if (mType == '' || mSerial == '') {
    cmr.showAlert('Please input both Machine Type and Serial No.');
    return;
  }
  if (mType.length != 4 && mSerial.length != 7) {
    cmr.showAlert('Machine Type and Serial Number should be exactly 4 and 7 characters long respectively.');
    return;
  }
  cmr.addrReqId = FormManager.getActualValue('reqId');
  cmr.currentModalId = 'addEditAddressModal';
  cmr.currentAddressType = FormManager.getActualValue('addrType');
  cmr.currentAddressSeq = FormManager.getActualValue('addrSeq');
  cmr.showConfirm('actualAddMachine()', 'Add Machine <strong>' + FormManager.getActualValue('machineTyp') + FormManager.getActualValue('machineSerialNo') + '</strong> to the Machine List?');
}

/**
 * Called after the confirm
 */
function actualAddMachine() {
  _currentMachineProcess = 'I';
  FormManager.doHiddenAction('frmCMR_addressModal', 'ADD_MACHINE', cmr.CONTEXT_ROOT + '/request/address/process.json?reqId=' + cmr.addrReqId, true, refreshMachinesAfterResult, true);
}

/**
 * 
 * @param result
 */
function refreshMachinesAfterResult(result) {
  if (result.success) {
    if (_currentMachineProcess == 'I') {
      cmr.showAlert('Machine added successfully.', 'Success', null, true);
      FormManager.setValue('machineTyp', '');
      FormManager.setValue('machineSerialNo', '');
    } else {
      cmr.showAlert('Machine removed successfully.', 'Success', null, true);
    }

    _currentMachineProcess = '';
    CmrGrid.refresh('MACHINES_GRID', cmr.CONTEXT_ROOT + '/request/address/machines/list.json', 'reqId=:reqId&addrType=:addrType&addrSeq=:addrSeq');
  }
}

/**
 * Formatter for the Action column of the Delegate list grid
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function removeMachineFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(0);
  if (rowData == null) {
    return ''; // not more than 1 record
  }
  rowData = this.grid.getItem(rowIndex);
  var machineTyp = rowData.machineTyp;
  var machineSerialNo = rowData.machineSerialNo;
  var currInd = rowData.currentIndc[0];
  if (currInd == 'Y') {
    return '';
  }
  var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
  return '<img src="' + imgloc + 'addr-remove-icon.png"  class="addr-icon" title = "Remove Entry" onclick = "doRemoveFromMachineList(\'' + machineTyp + '\',\'' + machineSerialNo + '\')">';
}

/**
 * Removes the delegate with the given ID and name
 * 
 * @param id
 * @param name
 */
function doRemoveFromMachineList(machineTyp, machineSerialNo) {
  dojo.byId('machineTyp').value = machineTyp;
  dojo.byId('machineSerialNo').value = machineSerialNo;
  cmr.showConfirm('actualRemoveFromMachineList()', 'Remove Machine <strong>' + machineTyp + machineSerialNo + '</strong> from the Machine List?');
}

function addPhoneValidatorNORDX() {
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
}

/**
 * Called after confirm
 */
function actualRemoveFromMachineList() {
  _currentMachineProcess = 'D';
  var reqId = FormManager.getActualValue('reqId');
  FormManager.doHiddenAction('frmCMR_addressModal', 'REMOVE_MACHINE', cmr.CONTEXT_ROOT + '/request/address/process.json?reqId=' + reqId, true, refreshMachinesAfterResult, true);
  dojo.byId('machineTyp').value = '';
  dojo.byId('machineSerialNo').value = '';
}

function setAddrDetailsForView(addrType, addrSeq) {
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  var machType = [];
  var serialNo = [];

  var reqParam1 = {
    _qall : 'Y',
    REQ_ID : reqId,
    ADDR_TYPE : addrType,
    ADDR_SEQ : addrSeq,
  };
  var newResults = cmr.query('ZP02_GET_MACHINES', reqParam1);
  if (newResults != null && reqType != 'C') {
    for (var l = 0; l < newResults.length; l++) {
      machType.push(newResults[l].ret2);
      serialNo.push(newResults[l].ret1);
    }
  }

  addDataToAddrDetailsTbl(machType, serialNo);

}

function addDataToAddrDetailsTbl(machType, serialNo) {
  var colHeader = new Array();
  var table = document.createElement("TABLE");
  var machineDetails = new Array();
  var columnCount = 2;
  table.border = "1";

  colHeader.push([ "Machine Type" ], [ "Serial Number" ]);
  for (var i = 0; i < machType.length; i++) {
    machineDetails.push([ machType[i], serialNo[i] ]);
  }
  var row = table.insertRow(-1);
  for (var i = 0; i < columnCount; i++) {
    var headerCell = document.createElement("TH");
    headerCell.innerHTML = colHeader[i];
    headerCell.style.fontSize = "smaller";
    row.appendChild(headerCell);
  }

  for (var i = 0; i < machineDetails.length; i++) {
    row = table.insertRow(-1);
    for (var j = 0; j < columnCount; j++) {
      var cell = row.insertCell(-1);
      cell.innerHTML = machineDetails[i][j];
    }
  }

  var dvTable = document.getElementById("dvTable");
  dvTable.innerHTML = "";
  dvTable.appendChild(table);
}

function _assignAddrDetailsValue(queryId, value) {
  var result = dojo.query(queryId);
  if (result != null && result.length > 0) {
    result[0].innerHTML = value;
  }
}

function norwayCustomVATValidator(cntry, tabName, formName, aType) {
  return function() {
    FormManager.addFormValidator((function() {
      var landCntry = cntry;
      var addrType = aType;
      var role = null;
      if (typeof (_pagemodel) != 'undefined') {
        role = _pagemodel.userRole;
      }
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

          if (role == 'Requester' && !vat.match("NO\\d{9}MVA")) {
            return new ValidationResult({
              id : 'vat',
              type : 'text',
              name : 'vat'
            }, false, 'Invalid format of VAT for NO. Format should be NO999999999MVA');
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

function setPPSCEID() {
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  checkAndAddValidator('ppsceid', LATINNORDX, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
  checkAndAddValidator('ppsceid', lowercaseLatinValidatorNordx, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
  var role = null;

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'C' && custSubType != '' && custSubType != undefined && custSubType != null) {
    if (custSubType.includes('BUS')) {
      if (role == 'Viewer') {
        FormManager.readOnly('ppsceid');
      } else {
        FormManager.enable('ppsceid');
      }
      checkAndAddValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.readOnly('ppsceid');
      FormManager.clearValue('ppsceid');
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    }
  }
}

function lowercaseLatinValidatorNordx(input) {
  var value = FormManager.getActualValue(input);
  if (!value || value == '' || value.length == 0) {
    return new ValidationResult(input, true);
  }
  var lowercaseValue = value.toLowerCase();
  if (lowercaseValue != value) {
    return new ValidationResult(input, false, '{0} Should be lowercase latin');
  } else {
    return new ValidationResult(input, true);
  }
}

function LATINNORDX(input) {
  var value = FormManager.getActualValue(input);
  if (!value || value == '' || value.length == 0) {
    return new ValidationResult(input, true);
  }
  var reg = /[^\u0000-\u007f]/;
  if (reg.test(value)) {
    return new ValidationResult(input, false, MessageMgr.MESSAGES.LATIN);
  } else {
    return new ValidationResult(input, true);
  }
}

function LATINBALTICS(input) {
  var value = FormManager.getActualValue(input);
  if (!value || value == '' || value.length == 0) {
    return new ValidationResult(input, true);
  }
  var reg = /[^\u0000-\u00bf\u00d7\u00f7\u0100-\u024f]/;
  if (reg.test(value)) {
    return new ValidationResult(input, false, MessageMgr.MESSAGES.LATIN1);
  } else {
    return new ValidationResult(input, true);
  }
}

function filterCmrnoP() {
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

// CREATCMR-1758
function vatInfoBubbleShowAndHide() {
  if (reqType == 'C') {
    var custType = FormManager.getActualValue('custGrp');
    if (custType == 'LOCAL' || custType == 'FOLOC' || custType == 'GLLOC' || custType == 'ISLOC' || custType == 'EELOC' || custType == 'LTLOC' || custType == 'LVLOC') {
      $("span[id='vatInfoBubble'] img[class='cmr-info-bubble']").show();
    } else {
      $("span[id='vatInfoBubble'] img[class='cmr-info-bubble']").hide();
    }
  } else if (reqType == 'U') {
    var landCntry = FormManager.getActualValue('defaultLandedCountry');
    var zs01Cntry = '';

    var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
      REQID : FormManager.getActualValue('reqId'),
      TYPE : 'ZS01'
    });

    if (ret && ret.ret1 && ret.ret1 != '') {
      zs01Cntry = ret.ret1;
    }

    if (zs01Cntry == landCntry) {
      $("span[id='vatInfoBubble'] img[class='cmr-info-bubble']").show();
    } else {
      $("span[id='vatInfoBubble'] img[class='cmr-info-bubble']").hide();
    }
  }
}

// CREATCMR-1653
function currencyUIShowAndHide() {
  reqType = FormManager.getActualValue('reqType');

  if (reqType == 'C') {

    var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    if (cmrIssuingCntry == '678') {
      FormManager.setValue('currencyCd', 'DKK');
    } else if (cmrIssuingCntry == '702') {
      FormManager.setValue('currencyCd', 'EUR');
    } else if (cmrIssuingCntry == '806') {
      FormManager.setValue('currencyCd', 'NOK');
    } else if (cmrIssuingCntry == '846') {
      FormManager.setValue('currencyCd', 'SEK');
    }
    cmr.hideNode("container-CurrencyCd");
  }

  if (reqType == 'U') {
    FormManager.show('CurrencyCd', 'currencyCd');
  }
}

// CREATCMR-1648
function setCollectionCd() {
  reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  var role = null;

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Requester') {
    if (reqType == 'C') {
      cmr.hideNode("container-CollectionCd");
      if (custSubGrp == 'CBINT' || custSubGrp == 'DKINT' || custSubGrp == 'FOINT' || custSubGrp == 'GLINT' || custSubGrp == 'ISINT' || custSubGrp == 'FIINT' || custSubGrp == 'EEINT'
          || custSubGrp == 'LTINT' || custSubGrp == 'LVINT' || custSubGrp == 'INTER') {
        FormManager.setValue('collectionCd', '000INT');
      } else if (custSubGrp == 'CBISO' || custSubGrp == 'DKISO' || custSubGrp == 'FOISO' || custSubGrp == 'GLISO' || custSubGrp == 'ISISO' || custSubGrp == 'FIISO' || custSubGrp == 'EEISO'
          || custSubGrp == 'LTISO' || custSubGrp == 'LVISO' || custSubGrp == 'INTSO') {
        FormManager.setValue('collectionCd', '0000SO');
      } else {
        FormManager.setValue('collectionCd', '');
      }
    }

    if (reqType == 'U') {
      var requestingLob = FormManager.getActualValue('requestingLob');
      if (requestingLob == 'AR' || requestingLob == 'SCT' || requestingLob == 'PRE') {
        FormManager.enable('collectionCd');
      } else {
        FormManager.readOnly('collectionCd');
      }

      if (requestingLob == 'SCT') {
        FormManager.enable('repTeamMemberNo');
      } else {
        FormManager.readOnly('repTeamMemberNo');
      }

    }
  }

  if (reqType == 'C') {
    if (role == 'Viewer' || role == 'Processor') {
      cmr.hideNode("container-CollectionCd");
    }
  }

}

function requestingLobOnChange() {

  var role = null;

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (role == 'Requester') {
    dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
      var requestingLob = FormManager.getActualValue('requestingLob');

      if (requestingLob == 'AR' || requestingLob == 'SCT' || requestingLob == 'PRE') {
        FormManager.setValue('collectionCd', _pagemodel.collectionCd);
        FormManager.enable('collectionCd');
        collectionCdValidation();
      } else {
        reqType = FormManager.getActualValue('reqType');
        if (reqType == 'C') {
          FormManager.setValue('collectionCd', '');
        } else if (reqType == 'U') {
          FormManager.setValue('collectionCd', _pagemodel.collectionCd);
        }
        FormManager.readOnly('collectionCd');
      }

      if (reqType == 'U') {
        if (requestingLob == 'SCT') {
          FormManager.enable('repTeamMemberNo');
        } else {
          FormManager.readOnly('repTeamMemberNo');
        }
      }

    });
  }

  if (role == 'Processor') {
    FormManager.enable('collectionCd');
    collectionCdValidation();
  }
}

function collectionCdValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var collectionCd = FormManager.getActualValue('collectionCd');
        var alphanumeric = /^[0-9a-zA-Z]*$/;
        if (collectionCd == '') {
          return new ValidationResult(null, true);
        } else {
          if (!collectionCd.match(alphanumeric)) {
            return new ValidationResult({
              id : 'collectionCd',
              type : 'text',
              name : 'collectionCd'
            }, false, 'The value of Collection Code is invalid, please use only alphanumeric characters.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function streetContControll() {
  var streetValue = FormManager.getActualValue('addrTxt');
  if (streetValue != undefined && streetValue.length > 0) {
    FormManager.enable('addrTxt2');
  } else {
    FormManager.setValue('addrTxt2', '');
    FormManager.disable('addrTxt2');
  }
}

// CREATCMR-1748&1744
function setTaxCdValues() {
  var field = FormManager.getField('taxCd1');

  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var countryUse = FormManager.getActualValue('countryUse');

  if (cmrIssuingCntry == '678') {
    // Faroe Islands, Greenland
    if (countryUse == '678FO' || countryUse == '678GL') {
      var taxCodeArray = [ '', '01', '14', '20', '24', '60', '61', '00', '25' ];
      FormManager.limitDropdownValues(field, taxCodeArray);
      FormManager.enable(field);
    } else if (countryUse == '678IS') { // Iceland
      var taxCodeArray = [ '', '00', '24' ];
      FormManager.limitDropdownValues(field, taxCodeArray);
      FormManager.enable(field);
    } else { // Denmark
      var taxCodeArray = [ '', '01', '14', '20', '24', '60', '61', '00', '25' ];
      FormManager.limitDropdownValues(field, taxCodeArray);
      FormManager.enable(field);
    }
  }
}

var pageModelFlag = 'N';

function setTaxCdValuesByCustSubGrp() {

  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {

    var custSubGrp = FormManager.getActualValue('custSubGrp');

    // Denmark, Faroe Islands, Greenland
    if (custSubGrp == 'DKCOM' || custSubGrp == 'DKBUS' || custSubGrp == 'DKPRI' || custSubGrp == 'DKIBM' || custSubGrp == 'DKGOV' || custSubGrp == 'DK3PA' || custSubGrp == 'FOCOM'
        || custSubGrp == 'FOBUS' || custSubGrp == 'FOPRI' || custSubGrp == 'FOIBM' || custSubGrp == 'FOGOV' || custSubGrp == 'FO3PA' || custSubGrp == 'GLCOM' || custSubGrp == 'GLBUS'
        || custSubGrp == 'GLPRI' || custSubGrp == 'GLIBM' || custSubGrp == 'GLGOV' || custSubGrp == 'GL3PA') {
      if (pageModelFlag == 'Y') {
        FormManager.setValue('taxCd1', '01');
      } else {
        FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '01' : _pagemodel.taxCd1);
        pageModelFlag = 'Y';
      }
    } else if (custSubGrp == 'DKINT' || custSubGrp == 'DKISO' || custSubGrp == 'FOINT' || custSubGrp == 'FOISO' || custSubGrp == 'GLINT' || custSubGrp == 'GLISO') {
      if (pageModelFlag == 'Y') {
        FormManager.setValue('taxCd1', '00');
      } else {
        FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '00' : _pagemodel.taxCd1);
        pageModelFlag = 'Y';
      }
    }

    // Iceland
    if (custSubGrp == 'ISCOM' || custSubGrp == 'ISBUS' || custSubGrp == 'ISPRI' || custSubGrp == 'ISIBM' || custSubGrp == 'ISGOV' || custSubGrp == 'IS3PA' || custSubGrp == 'ISINT'
        || custSubGrp == 'ISISO') {
      if (pageModelFlag == 'Y') {
        FormManager.setValue('taxCd1', '00');
      } else {
        FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '00' : _pagemodel.taxCd1);
        pageModelFlag = 'Y';
      }
    }

    // Finland
    if (custSubGrp == 'FICOM' || custSubGrp == 'FIBUS' || custSubGrp == 'FIPRI' || custSubGrp == 'FIIBM' || custSubGrp == 'FIGOV' || custSubGrp == 'FI3PA') {
      if (pageModelFlag == 'Y') {
        FormManager.setValue('taxCd1', '11');
      } else {
        FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '11' : _pagemodel.taxCd1);
        pageModelFlag = 'Y';
      }
    } else if (custSubGrp == 'FIINT' || custSubGrp == 'FIISO') {
      if (pageModelFlag == 'Y') {
        FormManager.setValue('taxCd1', '00');
      } else {
        FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '00' : _pagemodel.taxCd1);
        pageModelFlag = 'Y';
      }
    }

    // Estonia, Lithuania, Latvia
    if (custSubGrp == 'EECOM' || custSubGrp == 'EEBUS' || custSubGrp == 'EEPRI' || custSubGrp == 'EEIBM' || custSubGrp == 'EEGOV' || custSubGrp == 'EE3PA' || custSubGrp == 'LTCOM'
        || custSubGrp == 'LTBUS' || custSubGrp == 'LTPRI' || custSubGrp == 'LTIBM' || custSubGrp == 'LTGOV' || custSubGrp == 'LT3PA' || custSubGrp == 'LVCOM' || custSubGrp == 'LVBUS'
        || custSubGrp == 'LVPRI' || custSubGrp == 'LVIBM' || custSubGrp == 'LVGOV' || custSubGrp == 'LV3PA') {
      if (pageModelFlag == 'Y') {
        FormManager.setValue('taxCd1', '01');
      } else {
        FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '01' : _pagemodel.taxCd1);
        pageModelFlag = 'Y';
      }
    } else if (custSubGrp == 'EEINT' || custSubGrp == 'EEISO' || custSubGrp == 'LTINT' || custSubGrp == 'LTISO' || custSubGrp == 'LVINT' || custSubGrp == 'LVISO') {
      if (pageModelFlag == 'Y') {
        FormManager.setValue('taxCd1', '00');
      } else {
        FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '00' : _pagemodel.taxCd1);
        pageModelFlag = 'Y';
      }
    }

    // Norway
    if (cmrIssuingCntry == '806') {
      if (custSubGrp == 'COMME' || custSubGrp == 'BUSPR' || custSubGrp == 'PRIPE' || custSubGrp == 'IBMEM' || custSubGrp == 'GOVRN' || custSubGrp == 'THDPT') {
        if (pageModelFlag == 'Y') {
          FormManager.setValue('taxCd1', '07');
        } else {
          FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '07' : _pagemodel.taxCd1);
          pageModelFlag = 'Y';
        }
      }

      if (custSubGrp == 'INTER' || custSubGrp == 'INTSO') {
        if (pageModelFlag == 'Y') {
          FormManager.setValue('taxCd1', '00');
        } else {
          FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '00' : _pagemodel.taxCd1);
          pageModelFlag = 'Y';
        }
      }
    }

    // Sweden
    if (cmrIssuingCntry == '846') {
      if (custSubGrp == 'COMME' || custSubGrp == 'BUSPR' || custSubGrp == 'PRIPE' || custSubGrp == 'IBMEM' || custSubGrp == 'GOVRN' || custSubGrp == 'THDPT') {
        if (pageModelFlag == 'Y') {
          FormManager.setValue('taxCd1', '01');
        } else {
          FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '01' : _pagemodel.taxCd1);
          pageModelFlag = 'Y';
        }
      }

      if (custSubGrp == 'INTER' || custSubGrp == 'INTSO') {
        if (pageModelFlag == 'Y') {
          FormManager.setValue('taxCd1', '00');
        } else {
          FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '00' : _pagemodel.taxCd1);
          pageModelFlag = 'Y';
        }
      }
    }

    if (custSubGrp == 'CBCOM' || custSubGrp == 'CBBUS') {

      var isEUCntry = false;
      var countryUse = FormManager.getActualValue('countryUse');

      var reqId = FormManager.getActualValue('reqId');
      var reqParam = {
        REQ_ID : reqId,
        ADDR_TYPE : 'ZS01',
      };
      var _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', reqParam);
      var landCntry = _result.ret2;

      if (EU_COUNTRIES.indexOf(landCntry) > -1) {
        isEUCntry = true;
      }

      if (cmrIssuingCntry == '678') {
        // Faroe Islands, Greenland, Iceland
        if (countryUse == '678FO' || countryUse == '678GL' || countryUse == '678IS') {
          if (pageModelFlag == 'Y') {
            FormManager.setValue('taxCd1', '00');
          } else {
            FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '00' : _pagemodel.taxCd1);
            pageModelFlag = 'Y';
          }
        } else { // Denmark
          if (isEUCntry == true && landCntry != null) {
            FormManager.setValue('taxCd1', '14');
          } else if (isEUCntry == false && landCntry != null) {
            FormManager.setValue('taxCd1', '20');
          }
        }
      }

      if (cmrIssuingCntry == '702') {
        // Estonia, Lithuania, Latvia
        if (countryUse == '702EE' || countryUse == '702LT' || countryUse == '702LV') {
          if (pageModelFlag == 'Y') {
            FormManager.setValue('taxCd1', '00');
          } else {
            FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '00' : _pagemodel.taxCd1);
            pageModelFlag = 'Y';
          }
        } else { // Finland
          if (isEUCntry == true && landCntry != null) {
            FormManager.setValue('taxCd1', '14');
          } else if (isEUCntry == false && landCntry != null) {
            FormManager.setValue('taxCd1', '20');
          }
        }
      }

      // Norway
      if (cmrIssuingCntry == '806') {
        if (isEUCntry == true && landCntry != null) {
          FormManager.setValue('taxCd1', '00');
        } else if (isEUCntry == false && landCntry != null) {
          FormManager.setValue('taxCd1', '00');
        }
      }

      // Sweden
      if (cmrIssuingCntry == '846') {
        if (isEUCntry == true && landCntry != null) {
          FormManager.setValue('taxCd1', '14');
        } else if (isEUCntry == false && landCntry != null) {
          FormManager.setValue('taxCd1', '20');
        }
      }
    } else if (custSubGrp == 'CBINT' || custSubGrp == 'CBISO') {
      if (pageModelFlag == 'Y') {
        FormManager.setValue('taxCd1', '00');
      } else {
        FormManager.setValue('taxCd1', _pagemodel.taxCd1 == null ? '00' : _pagemodel.taxCd1);
        pageModelFlag = 'Y';
      }
    }
  });
}

// CREATCMR-1651
var pageModelFlag1 = 'N';

function setKukalValuesByCustSubGrp() {

  reqType = FormManager.getActualValue('reqType');
  var field = FormManager.getField('custClass');

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  FormManager.resetDropdownValues(FormManager.getField('custClass'));

  if (reqType == 'C') {

    FormManager.setValue('custClass', '');

    if (custSubGrp == 'CBCOM' || custSubGrp == 'DKCOM' || custSubGrp == 'FOCOM' || custSubGrp == 'GLCOM' || custSubGrp == 'ISCOM' || custSubGrp == 'FICOM' || custSubGrp == 'EECOM'
        || custSubGrp == 'LTCOM' || custSubGrp == 'LVCOM' || custSubGrp == 'COMME') {
      var requestingLob = FormManager.getActualValue('requestingLob');
      if (requestingLob == 'IGF' || requestingLob == 'SCT') {
        FormManager.enable('custClass');

        // Commercial
        FormManager.limitDropdownValues(field, [ '11', '33', '35' ]);

        if (pageModelFlag1 == 'Y') {
          FormManager.setValue(field, '11');
        } else {
          FormManager.setValue(field, _pagemodel.custClass == null ? '11' : _pagemodel.custClass);
          pageModelFlag1 = 'Y';
        }
      } else {
        FormManager.readOnly('custClass');
        FormManager.limitDropdownValues(field, [ '11' ]);
        FormManager.setValue(field, '11');
      }
    } else if (custSubGrp == 'CBBUS' || custSubGrp == 'DKBUS' || custSubGrp == 'FOBUS' || custSubGrp == 'GLBUS' || custSubGrp == 'ISBUS' || custSubGrp == 'FIBUS' || custSubGrp == 'EEBUS'
        || custSubGrp == 'LTBUS' || custSubGrp == 'LVBUS' || custSubGrp == 'BUSPR') {

      // Business Partner
      FormManager.limitDropdownValues(field, [ '43', '45', '46' ]);

      if (pageModelFlag1 == 'Y') {
        FormManager.setValue(field, '45');
      } else {
        FormManager.setValue(field, _pagemodel.custClass == null ? '45' : _pagemodel.custClass);
        pageModelFlag1 = 'Y';
      }
    } else if (custSubGrp == 'DKGOV' || custSubGrp == 'FOGOV' || custSubGrp == 'GLGOV' || custSubGrp == 'ISGOV' || custSubGrp == 'FIGOV' || custSubGrp == 'EEGOV' || custSubGrp == 'LTGOV'
        || custSubGrp == 'LVGOV' || custSubGrp == 'GOVRN') {
      // Government
      FormManager.limitDropdownValues(field, [ '13' ]);
      FormManager.setValue(field, '13');
    } else if (custSubGrp == 'DKINT' || custSubGrp == 'FOINT' || custSubGrp == 'GLINT' || custSubGrp == 'ISINT' || custSubGrp == 'FIINT' || custSubGrp == 'EEINT' || custSubGrp == 'LTINT'
        || custSubGrp == 'LVINT' || custSubGrp == 'INTER' || custSubGrp == 'CBINT') {
      // Internal
      FormManager.limitDropdownValues(field, [ '81' ]);
      FormManager.setValue(field, '81');
    } else if (custSubGrp == 'CBISO' || custSubGrp == 'DKISO' || custSubGrp == 'FOISO' || custSubGrp == 'GLISO' || custSubGrp == 'ISISO' || custSubGrp == 'FIISO' || custSubGrp == 'EEISO'
        || custSubGrp == 'LTISO' || custSubGrp == 'LVISO' || custSubGrp == 'INTSO') {
      // Internal SO
      FormManager.limitDropdownValues(field, [ '85' ]);
      FormManager.setValue(field, '85');
    } else if (custSubGrp == 'DK3PA' || custSubGrp == 'FO3PA' || custSubGrp == 'GL3PA' || custSubGrp == 'IS3PA' || custSubGrp == 'FI3PA' || custSubGrp == 'EE3PA' || custSubGrp == 'LT3PA'
        || custSubGrp == 'LV3PA' || custSubGrp == 'THDPT') {
      // Third Party
      FormManager.limitDropdownValues(field, [ '11' ]);
      FormManager.setValue(field, '11');
    } else if (custSubGrp == 'DKPRI' || custSubGrp == 'FOPRI' || custSubGrp == 'GLPRI' || custSubGrp == 'ISPRI' || custSubGrp == 'FIPRI' || custSubGrp == 'EEPRI' || custSubGrp == 'LTPRI'
        || custSubGrp == 'LVPRI' || custSubGrp == 'PRIPE') {
      // Private person
      FormManager.limitDropdownValues(field, [ '60' ]);
      FormManager.setValue(field, '60');
    } else if (custSubGrp == 'DKIBM' || custSubGrp == 'FOIBM' || custSubGrp == 'GLIBM' || custSubGrp == 'ISIBM' || custSubGrp == 'FIIBM' || custSubGrp == 'EEIBM' || custSubGrp == 'LTIBM'
        || custSubGrp == 'LVIBM' || custSubGrp == 'IBMEM') {
      // IBM employee
      FormManager.limitDropdownValues(field, [ '71' ]);
      FormManager.setValue(field, '71');
    } else {
      FormManager.setValue('custClass', '');
    }
    

  } else if (reqType == 'U') {
    FormManager.limitDropdownValues(field, [ '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '32', '33', '34', '35', '36', '41', '42', '43', '44', '45',
        '46', '47', '48', '49', '50', '51', '52', '60', '71', '72', '81', '82', '83', '84', '85', '88', '99' ]);
    FormManager.setValue(field, _pagemodel.custClass);
  }

  dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var requestingLob = FormManager.getActualValue('requestingLob');

    if (requestingLob == 'IGF' || requestingLob == 'SCT') {

      if (custSubGrp == 'CBCOM' || custSubGrp == 'DKCOM' || custSubGrp == 'FOCOM' || custSubGrp == 'GLCOM' || custSubGrp == 'ISCOM' || custSubGrp == 'FICOM' || custSubGrp == 'EECOM'
          || custSubGrp == 'LTCOM' || custSubGrp == 'LVCOM' || custSubGrp == 'COMME') {
        FormManager.enable('custClass');

        FormManager.limitDropdownValues(FormManager.getField('custClass'), [ '11', '33', '35' ]);
        FormManager.setValue('custClass', '11');
      }
    } else {
      if (custSubGrp == 'CBCOM' || custSubGrp == 'DKCOM' || custSubGrp == 'FOCOM' || custSubGrp == 'GLCOM' || custSubGrp == 'ISCOM' || custSubGrp == 'FICOM' || custSubGrp == 'EECOM'
          || custSubGrp == 'LTCOM' || custSubGrp == 'LVCOM' || custSubGrp == 'COMME') {
        FormManager.readOnly('custClass');

        FormManager.limitDropdownValues(FormManager.getField('custClass'), [ '11' ]);
        FormManager.setValue('custClass', '11');
      }
    }

  });
}

// CREATCMR-1689
function setAbbreviatedNameValue() {
  var _reqId = FormManager.getActualValue('reqId');

  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    kuklaShowAndHide();

    if (custSubGrp == 'CBCOM' || custSubGrp == 'CBBUS' || custSubGrp == 'CBINT' || custSubGrp == 'DKCOM' || custSubGrp == 'DKBUS' || custSubGrp == 'DKINT' || custSubGrp == 'DKPRI'
        || custSubGrp == 'DKIBM' || custSubGrp == 'DKGOV' || custSubGrp == 'FOCOM' || custSubGrp == 'FOBUS' || custSubGrp == 'FOINT' || custSubGrp == 'FOPRI' || custSubGrp == 'FOIBM'
        || custSubGrp == 'FOGOV' || custSubGrp == 'GLCOM' || custSubGrp == 'GLBUS' || custSubGrp == 'GLINT' || custSubGrp == 'GLPRI' || custSubGrp == 'GLIBM' || custSubGrp == 'GLGOV'
        || custSubGrp == 'ISCOM' || custSubGrp == 'ISBUS' || custSubGrp == 'ISINT' || custSubGrp == 'ISPRI' || custSubGrp == 'ISIBM' || custSubGrp == 'ISGOV' || custSubGrp == 'FICOM'
        || custSubGrp == 'FIBUS' || custSubGrp == 'FIINT' || custSubGrp == 'FIPRI' || custSubGrp == 'FIIBM' || custSubGrp == 'FIGOV' || custSubGrp == 'EECOM' || custSubGrp == 'EEBUS'
        || custSubGrp == 'EEINT' || custSubGrp == 'EEPRI' || custSubGrp == 'EEIBM' || custSubGrp == 'EEGOV' || custSubGrp == 'LTCOM' || custSubGrp == 'LTBUS' || custSubGrp == 'LTINT'
        || custSubGrp == 'LTPRI' || custSubGrp == 'LTIBM' || custSubGrp == 'LTGOV' || custSubGrp == 'LVCOM' || custSubGrp == 'LVBUS' || custSubGrp == 'LVINT' || custSubGrp == 'LVPRI'
        || custSubGrp == 'LVIBM' || custSubGrp == 'LVGOV' || custSubGrp == 'COMME' || custSubGrp == 'BUSPR' || custSubGrp == 'INTER' || custSubGrp == 'PRIPE' || custSubGrp == 'IBMEM'
        || custSubGrp == 'GOVRN') {
      var custNm = getAbbreviatedNameByAddrType(_reqId, "ZS01");
      if (custNm != "") {
        FormManager.setValue('abbrevNm', custNm.substring(0, 22));
      } else {
        FormManager.setValue('abbrevNm', "");
      }
    }

    if (custSubGrp == 'DK3PA' || custSubGrp == 'FO3PA' || custSubGrp == 'GL3PA' || custSubGrp == 'IS3PA' || custSubGrp == 'FI3PA' || custSubGrp == 'EE3PA' || custSubGrp == 'LT3PA'
        || custSubGrp == 'LV3PA' || custSubGrp == 'THDPT') {
      // var custNm1 = getAbbreviatedNameByAddrType(_reqId, "ZS01");
      var custNm = getAbbreviatedNameByAddrType(_reqId, "ZI01");
      if (custNm != "") {
        FormManager.setValue('abbrevNm', custNm.substring(0, 22));
        // FormManager.setValue('abbrevNm', custNm1.substring(0, 8) + " c/o " +
        // custNm2.substring(0, 9));
      } else {
        FormManager.setValue('abbrevNm', "");
      }
    }

    if (custSubGrp == 'CBISO' || custSubGrp == 'DKISO' || custSubGrp == 'FOISO' || custSubGrp == 'GLISO' || custSubGrp == 'ISISO' || custSubGrp == 'FIISO' || custSubGrp == 'EEISO'
        || custSubGrp == 'LTISO' || custSubGrp == 'LVISO' || custSubGrp == 'INTSO') {
      var custNm = getAbbreviatedNameByAddrType(_reqId, "ZI01");
      if (custNm != "") {
        FormManager.setValue('abbrevNm', "IBM c/o " + custNm.substring(0, 14));
      } else {
        FormManager.setValue('abbrevNm', "IBM c/o ");
      }
    }

    var reqStatus = null;
    if (typeof (_pagemodel) != 'undefined') {
      reqStatus = _pagemodel.reqStatus;
    }

    if (reqStatus == 'PPN' || reqStatus == 'PVA' || reqStatus == 'PCP' || reqStatus == 'PCO' || reqStatus == 'COM') {
      var result = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', qParams);
      var abbrevNmDBValue = result.ret1;
      if (abbrevNmDBValue != '') {
        FormManager.setValue('abbrevNm', abbrevNmDBValue);
      }
    }

  });

  var qParams = {
    REQ_ID : _reqId
  };

  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (role == 'Processor') {
    var result = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', qParams);
    var abbrevNmDBValue = result.ret1;
    if (abbrevNmDBValue != '') {
      FormManager.setValue('abbrevNm', abbrevNmDBValue);
    }
  }
}

function getAbbreviatedNameByAddrType(_reqId, _addrType) {

  var custNm1Params = {
    REQ_ID : _reqId,
    ADDR_TYPE : _addrType
  };

  var custNmResult = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_MCO', custNm1Params);
  var custNm = custNmResult.ret1;
  if (custNm != undefined && custNm != '') {
    return custNm;
  } else {
    return "";
  }

}

function setAbbrevNmAddressSave(cntry, addressMode, saving, finalSave, force) {

  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {

    var _reqId = FormManager.getActualValue('reqId');

    if (finalSave || force || addressMode == 'ZS01' || addressMode == 'ZI01') {

      var addrType = FormManager.getActualValue('addrType');
      var zs01CustNm = '';
      var zi01CustNm = '';

      if (addrType == 'ZS01') {
        zs01CustNm = FormManager.getActualValue('custNm1');
      }

      if (addrType == 'ZI01') {
        zi01CustNm = FormManager.getActualValue('custNm1');
      }

      var custSubGrp = FormManager.getActualValue('custSubGrp');
      if (custSubGrp == 'CBCOM' || custSubGrp == 'CBBUS' || custSubGrp == 'CBINT' || custSubGrp == 'DKCOM' || custSubGrp == 'DKBUS' || custSubGrp == 'DKINT' || custSubGrp == 'DKPRI'
          || custSubGrp == 'DKIBM' || custSubGrp == 'DKGOV' || custSubGrp == 'FOCOM' || custSubGrp == 'FOBUS' || custSubGrp == 'FOINT' || custSubGrp == 'FOPRI' || custSubGrp == 'FOIBM'
          || custSubGrp == 'FOGOV' || custSubGrp == 'GLCOM' || custSubGrp == 'GLBUS' || custSubGrp == 'GLINT' || custSubGrp == 'GLPRI' || custSubGrp == 'GLIBM' || custSubGrp == 'GLGOV'
          || custSubGrp == 'ISCOM' || custSubGrp == 'ISBUS' || custSubGrp == 'ISINT' || custSubGrp == 'ISPRI' || custSubGrp == 'ISIBM' || custSubGrp == 'ISGOV' || custSubGrp == 'FICOM'
          || custSubGrp == 'FIBUS' || custSubGrp == 'FIINT' || custSubGrp == 'FIPRI' || custSubGrp == 'FIIBM' || custSubGrp == 'FIGOV' || custSubGrp == 'EECOM' || custSubGrp == 'EEBUS'
          || custSubGrp == 'EEINT' || custSubGrp == 'EEPRI' || custSubGrp == 'EEIBM' || custSubGrp == 'EEGOV' || custSubGrp == 'LTCOM' || custSubGrp == 'LTBUS' || custSubGrp == 'LTINT'
          || custSubGrp == 'LTPRI' || custSubGrp == 'LTIBM' || custSubGrp == 'LTGOV' || custSubGrp == 'LVCOM' || custSubGrp == 'LVBUS' || custSubGrp == 'LVINT' || custSubGrp == 'LVPRI'
          || custSubGrp == 'LVIBM' || custSubGrp == 'LVGOV' || custSubGrp == 'COMME' || custSubGrp == 'BUSPR' || custSubGrp == 'INTER' || custSubGrp == 'PRIPE' || custSubGrp == 'IBMEM'
          || custSubGrp == 'GOVRN') {
        if (addrType == 'ZS01') {
          FormManager.setValue('abbrevNm', zs01CustNm.substring(0, 22));
        }
      }

      if (custSubGrp == 'DK3PA' || custSubGrp == 'FO3PA' || custSubGrp == 'GL3PA' || custSubGrp == 'IS3PA' || custSubGrp == 'FI3PA' || custSubGrp == 'EE3PA' || custSubGrp == 'LT3PA'
          || custSubGrp == 'LV3PA' || custSubGrp == 'THDPT') {
        FormManager.setValue('abbrevNm', zi01CustNm.substring(0, 22));
      }

      if (custSubGrp == 'CBISO' || custSubGrp == 'DKISO' || custSubGrp == 'FOISO' || custSubGrp == 'GLISO' || custSubGrp == 'ISISO' || custSubGrp == 'FIISO' || custSubGrp == 'EEISO'
          || custSubGrp == 'LTISO' || custSubGrp == 'LVISO' || custSubGrp == 'INTSO') {
        FormManager.setValue('abbrevNm', "IBM c/o " + zi01CustNm.substring(0, 14));
      }

    }
  }
}
// CREATCMR-1689

// CREATCMR-1700
function embargoCodeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd');
        if (embargoCd == 'D' || embargoCd == 'J' || embargoCd == 'K' || embargoCd == '') {
          return new ValidationResult(null, true);
        } else {
          return new ValidationResult({
            id : 'embargoCd',
            type : 'text',
            name : 'embargoCd'
          }, false, 'Embargo Code value should be only \'D\', \'J\', \'K\' or blank.');
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}
// CREATCMR-1700

// CREATCMR-1638
function setModeOfPaymentValue() {
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    FormManager.setValue('modeOfPayment', 'A001');
    cmr.hideNode("container-ModeOfPayment");
  }

  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (reqType == 'U') {
    if (role == 'Requester') {

      FormManager.readOnly('modeOfPayment');

      var requestingLob = FormManager.getActualValue('requestingLob');
      if (requestingLob == 'AR' || requestingLob == 'IGF' || requestingLob == 'SCT') {
        FormManager.enable('modeOfPayment');
      } else {
        FormManager.readOnly('modeOfPayment');
      }

      dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
        var requestingLob = FormManager.getActualValue('requestingLob');
        if (requestingLob == 'AR' || requestingLob == 'IGF' || requestingLob == 'SCT') {
          FormManager.enable('modeOfPayment');
        } else {
          FormManager.readOnly('modeOfPayment');
        }
      });
      modeOfPaymentValidation();
    }

    if (role == 'Processor') {
      FormManager.enable('modeOfPayment');
      modeOfPaymentValidation();
    }

  }
}

function modeOfPaymentValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var modeOfPayment = FormManager.getActualValue('modeOfPayment');

        var result = cmr.query('GET.ND.MODE_OF_PAYMENT_FOR_DATA_RDC', {
          REQ_ID : FormManager.getActualValue('reqId')
        });

        if (modeOfPayment == '') {
          // if (result.ref1 != '') {
          if (result && result.ret1) {
            return new ValidationResult({
              id : 'modeOfPayment',
              type : 'text',
              name : 'modeOfPayment'
            }, false, 'Payment Terms shouldn\'t be blanked.');
          }
          return;
        }

        var alphanumeric = /^[0-9a-zA-Z]*$/;

        if (!modeOfPayment.match(alphanumeric)) {
          return new ValidationResult({
            id : 'modeOfPayment',
            type : 'text',
            name : 'modeOfPayment'
          }, false, 'The value of Payment Terms is invalid, please use only alphanumeric characters.');
        }

        var modeOfpaymentArray = [ 'A001', 'A002', 'A003', 'A004', 'A005', 'A006', 'A007', 'A008', 'A009', 'A010', 'A014', 'A015', 'A016', 'A017', 'A018', 'A019', 'A020', 'A021', 'A022', 'A023',
            'A024', 'A025', 'A026', 'A027', 'A028', 'A029', 'A030', 'A031', 'A032', 'A033', 'A034', 'A035', 'A036', 'A037', 'A038', 'A039', 'A040', 'A043', 'A044', 'A045', 'A046', 'A047', 'A048',
            'A049', 'A050', 'A051', 'A052', 'A053', 'A054', 'A055', 'A056', 'A075', 'A0XX', 'ACOD', 'AR15', 'AR1D', 'AR30', 'AR45', 'AR60', 'AR90', 'ARCC', 'ARPP', 'ARQT', 'AS10', 'AS11', 'AS12',
            'AS13', 'AS14', 'AS15', 'BD00', 'BE00', 'BG00', 'BH00', 'BI00', 'BS00', 'BV00', 'CA00', 'CB00', 'CC00', 'CD00', 'CE00', 'CF00', 'CG00', 'CH00', 'CI00', 'CK00', 'CL00', 'CN00', 'CP00',
            'CT00', 'CX00', 'CY00', 'DD00', 'DJ00', 'EB00', 'ED00', 'EF00', 'EH00', 'EJ00', 'EK00', 'EM00', 'ER00', 'FE00', 'FI00', 'FJ00', 'FK00', 'FL00', 'FV00', 'GA00', 'GC00', 'GD00', 'GE00',
            'GF00', 'GG00', 'GH00', 'GH01', 'GI00', 'GJ00', 'GK00', 'GL00', 'GM00', 'GN00', 'GQ00', 'GR00', 'GS00', 'GT00', 'GU00', 'GV00', 'GZ00', 'HB00', 'HD00', 'IB00', 'IC00', 'ID00', 'IF00',
            'IH00', 'IH01', 'II00', 'II30', 'IK00', 'IL00', 'IM00', 'IN00', 'IP00', 'IS00', 'IY00', 'JA00', 'JB00', 'JC00', 'KB00', 'KD00', 'KE00', 'LA01', 'LA02', 'LA03', 'LA04', 'LA05', 'LA06',
            'MB00', 'MC00', 'MD00', 'NB00', 'NC00', 'ND00', 'NT08', 'NT30', 'NT45', 'NT60', 'P001', 'P003', 'P004', 'P015', 'P017', 'P021', 'P029', 'P037', 'P038', 'P039', 'P041', 'P042', 'P046',
            'P100', 'P110', 'P120', 'P130', 'P140', 'P150', 'P190', 'P200', 'P210', 'P220', 'P230', 'P240', 'P250', 'P260', 'P263', 'P270', 'P300', 'P310', 'P400', 'P404', 'P700', 'P800', 'P900',
            'P901', 'PA00', 'PA10', 'PB00', 'PC00', 'PD00', 'PE00', 'PF00', 'PG00', 'PH00', 'PI00', 'PI61', 'PI62', 'PJ00', 'PK00', 'PL00', 'PM00', 'PN00', 'PO00', 'PP00', 'PQ00', 'PR00', 'PS00',
            'PT00', 'PU00', 'PV00', 'PW00', 'PY00', 'PZ00', 'RA65', 'RN01', 'RN03', 'T000', 'TB00', 'TC00', 'TD00', 'TE00', 'TF00', 'TG00', 'TH00', 'TI00', 'TJ00', 'TK00', 'TL00', 'TM00', 'TO00',
            'TS00', 'TT00', 'WCFI', 'XA00', 'XC00', 'XG00', 'XK00', 'XL00', 'XM00', 'XP00', 'XR00', 'XS00', 'XT00', 'XU00', 'XV00', 'XW00', 'YA00', 'YB00', 'YB00', 'YC00', 'YC00', 'YC00', 'YD00',
            'YD00', 'YD00', 'YE00', 'YE00', 'YE00', 'YE00', 'YG00', 'YG00', 'YG00', 'YG00', 'YG00', 'Z01S', 'ZSDD', 'ZZI1' ];

        if (!modeOfpaymentArray.includes(modeOfPayment)) {
          return new ValidationResult({
            id : 'modeOfPayment',
            type : 'text',
            name : 'modeOfPayment'
          }, false, 'The value of Payment Terms is invalid.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');

}
// CREATCMR-1638

// CREATCMR-1690
function settingForProcessor() {
  reqType = FormManager.getActualValue('reqType');

  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (reqType == 'C') {
    if (role == 'Processor') {
      FormManager.enable('cmrNo');
      $('#cmrNo').attr('maxlength', '6');
    }
  }
}

function addCmrNoValidatorForNordx() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrNo = FormManager.getActualValue('cmrNo');

        if (cmrNo != '' && cmrNo != null) {
          if (cmrNo.length != 6) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'CMR Number should be exactly 6 digits long.');
          } else if (isNaN(cmrNo)) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'CMR Number should be only numbers.');
          } else if (cmrNo == "000000") {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'CMR Number should not be 000000.');
          } else if (cmrNo != '' && custSubType != '') {
            if (custSubType == 'CBINT' || custSubType == 'DKINT' || custSubType == 'FOINT' || custSubType == 'GLINT' || custSubType == 'ISINT' || custSubType == 'FIINT' || custSubType == 'EEINT'
                || custSubType == 'LTINT' || custSubType == 'LVINT' || custSubType == 'INTER') {
              if (!cmrNo.startsWith('99')) {
                return new ValidationResult({
                  id : 'cmrNo',
                  type : 'text',
                  name : 'cmrNo'
                }, false, 'CMR Number should be in 99XXXX format for Internal scenarios.');
              }
              if (cmrNo.startsWith('997')) {
                return new ValidationResult({
                  id : 'cmrNo',
                  type : 'text',
                  name : 'cmrNo'
                }, false, '997XXX range can be used only for Internal SO scenario. For Internal Scenario, please use CMR from any other 99XXXX range.');
              }
            } else if (custSubType == 'CBISO' || custSubType == 'DKISO' || custSubType == 'FOISO' || custSubType == 'GLISO' || custSubType == 'ISISO' || custSubType == 'FIISO'
                || custSubType == 'EEISO' || custSubType == 'LTISO' || custSubType == 'LVISO' || custSubType == 'INTSO') {
              if (!cmrNo.startsWith('997')) {
                return new ValidationResult({
                  id : 'cmrNo',
                  type : 'text',
                  name : 'cmrNo'
                }, false, 'CMR Number should be in 997XXX format for Internal SO scenarios.');
              }
            } else {
              if (cmrNo.startsWith('99')) {
                return new ValidationResult({
                  id : 'cmrNo',
                  type : 'text',
                  name : 'cmrNo'
                }, false, 'Only Internal Scenarios can be assigned CMRs from 99XXXX range.');
              }
            }

            var results1 = cmr.query('GET.CMR_BY_CNTRY_CUSNO_SAPR3', {
              CMRNO : cmrNo,
              CNTRY : cntry,
              MANDT : cmr.MANDT
            });

            var results2 = cmr.query('GET.CHECK_EXISTS_CMR_NO', {
              CMR_NO : cmrNo,
              CNTRY : cntry
            });

            if (results1.ret1 != null) {
              return new ValidationResult({
                id : 'cmrNo',
                type : 'text',
                name : 'cmrNo'
              }, false, 'The CMR Number already exists.');
            } else if (results2.ret1 != null) {
              return new ValidationResult({
                id : 'cmrNo',
                type : 'text',
                name : 'cmrNo'
              }, false, 'The CMR Number already exists.');
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
// CREATCMR-1690

// CREATCMR-1656
function setCapRecordActivate() {
  var params = {
    USERID : _pagemodel.requesterId
  };

  var result1 = cmr.query('GET.ND.USER_ROLE', params);
  var result2 = cmr.query('GET.ND.USER_PROC_CENTER_NM', params);

  reqType = FormManager.getActualValue('reqType');

  if (reqType == 'U') {
    if (result1.ret1 > 0 && result2.ret1 == 'Bratislava') {
      FormManager.enable('capInd');
    }
  }
}
// CREATCMR-1656

function kuklaShowAndHide() {
  var custClassLabel = $("#custClass_label img").attr('title');
  var kukla = FormManager.getActualValue('custClass');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');

  if (custClassLabel != undefined) {
    if (custClassLabel.match(kukla)) {
      $("#custClass_label img").remove();
    } else {
      var altArray = custClassLabel.split('-');
      $("#custClass_label img").attr('title', altArray[0]);
    }
  }
  cmr.hideNode("container-CustClass");
  if (custSubGrp.includes('COM') || custSubGrp.includes('BUS') || reqType == 'U') {
    cmr.showNode("container-CustClass");
  }
}

// CREATCMR-2144
function setCustPrefLangByCountry() {

  var field = FormManager.getField('custPrefLang');

  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var countryUse = FormManager.getActualValue('countryUse');

  reqType = FormManager.getActualValue('reqType');

  // CREATCMR-5437
  var reqId = FormManager.getActualValue('reqId');
  var custPrefLang = '';
  var results = cmr.query('GET.DATA_CUST_PREF_LANG_FOR_NORDX', {
    REQ_ID : reqId
  });

  if (results != null && results != undefined && results.ret1 != '') {
    custPrefLang = results.ret1;
  }

  // Denmark, Faroe Islands, Greenland, Iceland
  if (cmrIssuingCntry == '678') {
    if (countryUse == '678' || countryUse == '678FO' || countryUse == '678GL' || countryUse == '678IS') {
      FormManager.limitDropdownValues(field, [ 'K', 'E' ]);
      if (reqType == 'U') {
        if (typeof (_pagemodel) != 'undefined') {
          // CREATCMR-5437
          // FormManager.setValue('custPrefLang', _pagemodel.custPrefLang);
          FormManager.setValue('custPrefLang', custPrefLang);
          return;
        }
      }
      FormManager.setValue(field, _pagemodel.custPrefLang == null ? 'K' : _pagemodel.custPrefLang);
    }
  }

  if (cmrIssuingCntry == '702') {
    if (countryUse == '702') {
      // Finland
      FormManager.limitDropdownValues(field, [ 'U', 'V', 'E' ]);

      if (reqType == 'U') {
        if (typeof (_pagemodel) != 'undefined') {
          // CREATCMR-5437
          // FormManager.setValue('custPrefLang', _pagemodel.custPrefLang);
          FormManager.setValue('custPrefLang', custPrefLang);
          return;
        }
      }

      FormManager.setValue(field, _pagemodel.custPrefLang == null ? 'U' : _pagemodel.custPrefLang);
    } else if (countryUse == '702EE' || countryUse == '702LT' || countryUse == '702LV') {
      // Estonia, Lithuania, Latvia
      FormManager.limitDropdownValues(field, [ 'U', 'V', 'E' ]);

      if (reqType == 'U') {
        if (typeof (_pagemodel) != 'undefined') {
          // CREATCMR-5437
          // FormManager.setValue('custPrefLang', _pagemodel.custPrefLang);
          FormManager.setValue('custPrefLang', custPrefLang);
          return;
        }
      }

      FormManager.setValue(field, _pagemodel.custPrefLang == null ? 'E' : _pagemodel.custPrefLang);
    }
  }

  // Norway
  if (cmrIssuingCntry == '806') {
    FormManager.limitDropdownValues(field, [ 'O', 'E' ]);

    if (reqType == 'U') {
      if (typeof (_pagemodel) != 'undefined') {
        // CREATCMR-5437
        // FormManager.setValue('custPrefLang', _pagemodel.custPrefLang);
        FormManager.setValue('custPrefLang', custPrefLang);
        return;
      }
    }

    FormManager.setValue(field, _pagemodel.custPrefLang == null ? 'O' : _pagemodel.custPrefLang);
  }

  // Sweden
  if (cmrIssuingCntry == '846') {
    FormManager.limitDropdownValues(field, [ 'V', 'E' ]);

    if (reqType == 'U') {
      if (typeof (_pagemodel) != 'undefined') {
        // CREATCMR-5437
        // FormManager.setValue('custPrefLang', _pagemodel.custPrefLang);
        FormManager.setValue('custPrefLang', custPrefLang);
        return;
      }
    }

    FormManager.setValue(field, _pagemodel.custPrefLang == null ? 'V' : _pagemodel.custPrefLang);
  }

}

function lockTaxCode(){
  var role = FormManager.getActualValue('userRole');
  if (role == 'Viewer') {
    FormManager.readOnly('taxCd1');
  }
}

// CREATCMR-2144

// CREATCMR-1709
// CREATCMR-5128
/*function resetCustPrefLang() {
  _pagemodel.custPrefLang = null;
}*/
// CREATCMR-1709

function setInacCd() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType == 'C') {
    var custSubType = FormManager.getActualValue('custSubGrp');
    if (custSubType.includes('BUS') || (custSubType.includes('INT') && custSubType != 'INTSO') || custSubType.includes('PRI') || custSubType.includes('IBM')) {
      FormManager.setValue('inacCd', '');
      FormManager.readOnly('inacCd');
    } else {
      FormManager.enable('inacCd');
    }
  }
}

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage == 'true') {
    $('label[for="custNm1_view"]').text('Customer Name:');
    $('label[for="custNm2_view"]').text('Customer Name Con\'t:');
    $('label[for="custNm3_view"]').text('Additional Info');
    $('label[for="custNm4_view"]').text(' Att. Person');
  }
}

function setSensitiveFlag() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'C') {
    if (role == 'Requester') {
      FormManager.setValue('sensitiveFlag', 'REG');
      FormManager.readOnly('sensitiveFlag');
      FormManager.resetValidations('sensitiveFlag');
    } else if (role == 'Processor') {
      FormManager.enable('sensitiveFlag');
      FormManager.addValidator('sensitiveFlag', Validators.REQUIRED, [ 'Sensitive Flag' ], 'MAIN_CUST_TAB');
    }
  } else if (reqType == 'U') {
    FormManager.resetValidations('sensitiveFlag');
    if (role == 'Requester') {
      FormManager.readOnly('sensitiveFlag');
    } else if (role == 'Processor') {
      FormManager.enable('sensitiveFlag');
    }
  }
}

// CREATCMR-1657
function lockDunsNo() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();

  reqType = FormManager.getActualValue('reqType');

  if (reqType == 'U') {
    if (role == 'REQUESTER' || role == 'PROCESSOR') {
      FormManager.readOnly('dunsNo');
    }
  }

}
// CREATCMR-1657

// CREATCMR-2674
var oldIsuCtc = null;
function setSalesRepValues(value) {
  console.log('setSalesRepValues=====');
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || FormManager.getActualValue('reqType') == 'U') {
    return;
  }
  reqType = FormManager.getActualValue('reqType');

  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var countryUse = FormManager.getActualValue('countryUse');

  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCtc = isuCd.concat(clientTier);

  var subIndustry = FormManager.getActualValue('subIndustryCd');

  if ((value != false || value == undefined || value == null) && isuCtc != '' && oldIsuCtc == null) {
    oldIsuCtc = isuCd + clientTier;
  }

  if (oldIsuCtc == null) {
    return;
  }

  /*
   * var subIndPage = null; var subIndDB = null; if (typeof (_pagemodel) !=
   * 'undefined') { subIndPage = FormManager.getActualValue('subIndustryCd');
   * subIndDB = _pagemodel.subIndustryCd; }
   * 
   * console.log("subIndPage===>" + subIndPage); console.log("subIndDB===>" +
   * subIndDB);
   * 
   * if (!checkFlag) { if (subIndPage == subIndDB && subIndPage != null &&
   * subIndDB != null) { checkFlag = true; return; } }
   */

  if (reqType == 'C') {
    if (isuCd != '') {

      var isuAndCtc = isuCd + clientTier;
      var ind = subIndustry.substring(0, 1);

      if (cmrIssuingCntry == '678') {
        if (reqType == 'C') {
          FormManager.setValue('repTeamMemberNo', 'NOREP0');
        }

        if (countryUse == '678' || countryUse == '678FO' || countryUse == '678GL') {
          if (isuAndCtc == '34Q') {
            // Denmark, Faroe Islands, Greenland
            var T0001375 = [ 'K', 'U', 'A', 'F', 'N', 'S' ];
            var T0006880 = [ 'D', 'W', 'T', 'R' ];
            var T0006881 = [ 'V', 'J', 'P', 'L', 'M' ];
            var T0006644 = [ 'G', 'Y', 'E', 'H', 'X' ];
            var T0006607 = [ 'B', 'C' ];

            if (ind != '') {
              if (T0001375.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0001375');
              } else if (T0006880.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0006880');
              } else if (T0006881.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0006881');
              } else if (T0006644.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0006644');
              } else if (T0006607.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0006607');
              } else {
                FormManager.setValue('searchTerm', '');
              }
            } else {
              FormManager.setValue('searchTerm', '');
            }
          } else if (isuAndCtc == '21') {
            FormManager.setValue('searchTerm', 'T0000468');
          } else if (isuAndCtc == '8B') {
            FormManager.setValue('searchTerm', 'P0000007');
          } else {
            FormManager.setValue('searchTerm', '');
          }
        } else if (countryUse == '678IS') {
          // Iceland
          if (isuAndCtc == '34Q') {
            var T0007879 = [ 'B', 'C' ];

            if (ind != '') {
              if (T0007879.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0007879');
              } else {
                FormManager.setValue('searchTerm', 'T0001376');
              }
            } else {
              FormManager.setValue('searchTerm', '');
            }
          } else if (isuAndCtc == '21') {
            FormManager.setValue('searchTerm', 'T0000468');
          } else if (isuAndCtc == '8B') {
            FormManager.setValue('searchTerm', 'P0000007');
          } else {
            FormManager.setValue('searchTerm', '');
          }
        }

        if (isuAndCtc == '5K') {
          FormManager.setValue('searchTerm', 'T0009096');
        } else if (isuAndCtc == '19') {
          FormManager.setValue('searchTerm', 'A0005230');
        }

      }

      if (cmrIssuingCntry == '702') {

        if (reqType == 'C') {
          FormManager.setValue('repTeamMemberNo', "NOREP0");
        }

        if (countryUse == '702') {
          if (isuAndCtc == '34Q') {
            var T0001379 = [ 'D', 'W', 'T', 'R' ];
            var T0006974 = [ 'V', 'J', 'P', 'L', 'M' ];
            var T0007864 = [ 'K', 'U', 'A' ];
            var T0006609 = [ 'B', 'C' ];
            var T0006949 = [ 'G', 'Y', 'E', 'H', 'X' ];
            var T0007561 = [ 'F', 'N', 'S' ];

            if (ind != '') {
              if (T0001379.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0001379');
              } else if (T0006974.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0006974');
              } else if (T0007864.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0007864');
              } else if (T0006609.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0006609');
              } else if (T0006949.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0006949');
              } else if (T0007561.includes(ind)) {
                FormManager.setValue('searchTerm', 'T0007561');
              } else {
                FormManager.setValue('searchTerm', '');
              }
            } else {
              FormManager.setValue('searchTerm', '');
            }
          } else if (isuAndCtc == '5K') {
            FormManager.setValue('searchTerm', 'T0009094');
          } else if (isuAndCtc == '04') {
            FormManager.setValue('searchTerm', 'A0004751');
          } else if (isuAndCtc == '21') {
            FormManager.setValue('searchTerm', 'T0000471');
          } else if (isuAndCtc == '8B') {
            FormManager.setValue('searchTerm', 'P0000007');
          } else {
            FormManager.setValue('searchTerm', '');
          }
        } else if (countryUse == '702EE') {
          if (isuAndCtc == '34Q') {
            FormManager.setValue('searchTerm', 'T0004422');
          } else if (isuAndCtc == '5K') {
            FormManager.setValue('searchTerm', 'T0009073');
          } else if (isuAndCtc == '21') {
            FormManager.setValue('searchTerm', 'T0001661');
          } else if (isuAndCtc == '8B') {
            FormManager.setValue('searchTerm', 'P0000024');
          } else {
            FormManager.setValue('searchTerm', '');
          }
        } else if (countryUse == '702LV') {
          if (isuAndCtc == '34Q') {
            FormManager.setValue('searchTerm', 'T0004390');
          } else if (isuAndCtc == '5K') {
            FormManager.setValue('searchTerm', 'T0009071');
          } else if (isuAndCtc == '21') {
            FormManager.setValue('searchTerm', 'T0001661');
          } else if (isuAndCtc == '8B') {
            FormManager.setValue('searchTerm', 'P0000024');
          } else {
            FormManager.setValue('searchTerm', '');
          }
        } else if (countryUse == '702LT') {
          if (isuAndCtc == '34Q') {
            FormManager.setValue('searchTerm', 'T0004394');
          } else if (isuAndCtc == '5K') {
            FormManager.setValue('searchTerm', 'T0009072');
          } else if (isuAndCtc == '21') {
            FormManager.setValue('searchTerm', 'T0001661');
          } else if (isuAndCtc == '8B') {
            FormManager.setValue('searchTerm', 'P0000024');
          } else {
            FormManager.setValue('searchTerm', '');
          }
        }
      }
      if (cmrIssuingCntry == '806') {

        if (reqType == 'C') {
          FormManager.setValue('repTeamMemberNo', 'NOREP0');
        }

        if (isuAndCtc == '34Q') {
          var T0006611 = [ 'B', 'C' ];

          if (ind != '') {
            if (T0006611.includes(ind)) {
              FormManager.setValue('searchTerm', 'T0006611');
            } else {
              FormManager.setValue('searchTerm', 'T0001383');
            }
          } else {
            FormManager.setValue('searchTerm', 'T0001383');
          }
        } else if (isuAndCtc == '04') {
          FormManager.setValue('searchTerm', 'A0004752');
        } else if (isuAndCtc == '5K') {
          FormManager.setValue('searchTerm', 'T0009095');
        } else if (isuAndCtc == '21') {
          FormManager.setValue('searchTerm', 'T0000490');
        } else if (isuAndCtc == '8B') {
          FormManager.setValue('searchTerm', 'P0000007');
        } else {
          FormManager.setValue('searchTerm', '');
        }
      }
      if (cmrIssuingCntry == '846') {

        if (reqType == 'C') {
          FormManager.setValue('repTeamMemberNo', 'NOREP0');
        }

        if (isuAndCtc == '34Q') {
          var T0001387 = [ 'D', 'W', 'T', 'R' ];
          var T0006613 = [ 'B', 'C' ];
          var T0006966 = [ 'V', 'J', 'P', 'L', 'M', 'F', 'N', 'S' ];
          var T0007593 = [ 'K', 'U', 'A' ];
          var T0006888 = [ 'G', 'Y', 'E', 'H', 'X' ];

          if (ind != '') {
            if (T0001387.includes(ind)) {
              FormManager.setValue('searchTerm', 'T0001387');
            } else if (T0006613.includes(ind)) {
              FormManager.setValue('searchTerm', 'T0006613');
            } else if (T0006966.includes(ind)) {
              FormManager.setValue('searchTerm', 'T0006966');
            } else if (T0007593.includes(ind)) {
              FormManager.setValue('searchTerm', 'T0007593');
            } else if (T0006888.includes(ind)) {
              FormManager.setValue('searchTerm', 'T0006888');
            } else {
              FormManager.setValue('searchTerm', '');
            }
          } else {
            FormManager.setValue('searchTerm', '');
          }
        } else if (isuAndCtc == '04') {
          FormManager.setValue('searchTerm', 'I0000272');
        } else if (isuAndCtc == '1R') {
          FormManager.setValue('searchTerm', 'I0000271');
        } else if (isuAndCtc == '3T') {
          FormManager.setValue('searchTerm', 'A0003748');
        } else if (isuAndCtc == '5E') {
          FormManager.setValue('searchTerm', 'A0003748');
        } else if (isuAndCtc == '5K') {
          FormManager.setValue('searchTerm', 'T0009097');
        } else if (isuAndCtc == '21') {
          FormManager.setValue('searchTerm', 'T0000499');
        } else if (isuAndCtc == '8B') {
          FormManager.setValue('searchTerm', 'P0000007');
        } else {
          FormManager.setValue('searchTerm', '');
        }
      }
    }

  }

}

var subIndFlag = 'N';

function setSRValuesBaseOnSubInd() {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
    return;
  }

  // if (FormManager.getActualValue('reqType') != 'C') {
  // return;
  // }

  // if (FormManager.getActualValue('custSubGrp') == '' || subIndustry == '') {
  // return;
  // }

  var subIndPage = null;
  var subIndDB = null;
  if (typeof (_pagemodel) != 'undefined') {
    subIndPage = FormManager.getActualValue('subIndustryCd');
    subIndDB = _pagemodel.subIndustryCd;
  }

  console.log("subIndPage===>" + subIndPage);
  console.log("subIndDB===>" + subIndDB);

  // if (subIndPage == subIndDB && subIndPage != null && subIndDB != null) {
  // FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' :
  // _pagemodel.searchTerm);
  // return;
  // }

  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var countryUse = FormManager.getActualValue('countryUse');

  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');

  var subIndustry = FormManager.getActualValue('subIndustryCd');

  if (isuCd != '') {
    var isuAndCtc = isuCd + clientTier;
    var ind = subIndustry.substring(0, 1);

    if (cmrIssuingCntry == '678') {

      if (reqType == 'C') {
        FormManager.setValue('repTeamMemberNo', 'NOREP0');
      }

      if (countryUse == '678' || countryUse == '678FO' || countryUse == '678GL') {
        if (isuAndCtc == '34Q') {
          // Denmark, Faroe Islands, Greenland
          var T0001375 = [ 'K', 'U', 'A', 'F', 'N', 'S' ];
          var T0006880 = [ 'D', 'W', 'T', 'R' ];
          var T0006881 = [ 'V', 'J', 'P', 'L', 'M' ];
          var T0006644 = [ 'G', 'Y', 'E', 'H', 'X' ];
          var T0006607 = [ 'B', 'C' ];

          if (ind != '') {
            if (T0001375.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0001375');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0001375' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else if (T0006880.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0006880');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006880' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else if (T0006881.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0006881');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006881' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else if (T0006644.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0006644');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006644' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else if (T0006607.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0006607');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006607' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', '');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            }
          } else {
            FormManager.setValue('searchTerm', '');
          }
        } else if (isuAndCtc == '21') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0000468');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0000468' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '8B') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'P0000007');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'P0000007' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', '');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        }

      } else if (countryUse == '678IS') {
        // Iceland
        if (isuAndCtc == '34Q') {
          var T0007879 = [ 'B', 'C' ];

          if (ind != '') {
            if (T0007879.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0007879');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0007879' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0001376');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0001376' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            }
          } else {
            FormManager.setValue('searchTerm', '');
          }
        } else if (isuAndCtc == '21') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0000468');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0000468' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '8B') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'P0000007');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'P0000007' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', '');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        }
      }

      if (isuAndCtc == '5K') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'T0009096');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0009096' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      } else if (isuAndCtc == '19') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'A0005230');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'A0005230' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      }
    }

    if (cmrIssuingCntry == '702') {

      if (reqType == 'C') {
        FormManager.setValue('repTeamMemberNo', "NOREP0");
      }

      if (countryUse == '702') {
        if (isuAndCtc == '34Q') {
          var T0001379 = [ 'D', 'W', 'T', 'R' ];
          var T0006974 = [ 'V', 'J', 'P', 'L', 'M' ];
          var T0007864 = [ 'K', 'U', 'A' ];
          var T0006609 = [ 'B', 'C' ];
          var T0006949 = [ 'G', 'Y', 'E', 'H', 'X' ];
          var T0007561 = [ 'F', 'N', 'S' ];

          if (ind != '') {
            if (T0001379.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0001379');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0001379' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else if (T0006974.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0006974');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006974' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else if (T0007864.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0007864');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0007864' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else if (T0006609.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0006609');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006609' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else if (T0006949.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0006949');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006949' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else if (T0007561.includes(ind)) {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', 'T0007561');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0007561' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            } else {
              if (subIndFlag == 'Y') {
                FormManager.setValue('searchTerm', '');
              } else {
                FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
                subIndFlag = 'Y';
              }
            }
          } else {
            FormManager.setValue('searchTerm', '');
          }
        } else if (isuAndCtc == '04') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'A0004751');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'A0004751' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '5K') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0009094');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0009094' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '21') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0000471');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0000471' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '8B') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'P0000007');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'P0000007' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', '');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        }
      } else if (countryUse == '702EE') {
        if (isuAndCtc == '34Q') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0004422');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0004422' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '5K') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0009073');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0009073' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '21') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0001661');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0001661' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '8B') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'P0000024');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'P0000024' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', '');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        }
      } else if (countryUse == '702LT') {
        if (isuAndCtc == '34Q') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0004394');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0004394' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '5K') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0009072');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0009072' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '21') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0001661');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0001661' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '8B') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'P0000024');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'P0000024' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', '');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        }
      } else if (countryUse == '702LV') {
        if (isuAndCtc == '34Q') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0004390');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0004390' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '5K') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0009071');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0009071' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '21') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0001661');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0001661' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else if (isuAndCtc == '8B') {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'P0000024');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'P0000024' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        } else {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', '');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        }
      }

    }

    if (cmrIssuingCntry == '806') {

      if (reqType == 'C') {
        FormManager.setValue('repTeamMemberNo', 'NOREP0');
      }

      if (isuAndCtc == '34Q') {
        var T0006611 = [ 'B', 'C' ];

        if (ind != '') {
          if (T0006611.includes(ind)) {
            if (subIndFlag == 'Y') {
              FormManager.setValue('searchTerm', 'T0006611');
            } else {
              FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006611' : _pagemodel.searchTerm);
              subIndFlag = 'Y';
            }

          } else {
            if (subIndFlag == 'Y') {
              FormManager.setValue('searchTerm', 'T0001383');
            } else {
              FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0001383' : _pagemodel.searchTerm);
              subIndFlag = 'Y';
            }
          }
        } else {
          if (subIndFlag == 'Y') {
            FormManager.setValue('searchTerm', 'T0001383');
          } else {
            FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0001383' : _pagemodel.searchTerm);
            subIndFlag = 'Y';
          }
        }
      } else if (isuAndCtc == '04') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'A0004752');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'A0004752' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }

      } else if (isuAndCtc == '5K') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'T0009095');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0009095' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      } else if (isuAndCtc == '21') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'T0000490');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0000490' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      } else if (isuAndCtc == '8B') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'P0000007');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'P0000007' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      } else {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', '');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      }

    }

    if (cmrIssuingCntry == '846') {

      if (reqType == 'C') {
        FormManager.setValue('repTeamMemberNo', 'NOREP0');
      }

      if (isuAndCtc == '34Q') {
        var T0001387 = [ 'D', 'W', 'T', 'R' ];
        var T0006613 = [ 'B', 'C' ];
        var T0006966 = [ 'V', 'J', 'P', 'L', 'M', 'F', 'N', 'S' ];
        var T0007593 = [ 'K', 'U', 'A' ];
        var T0006888 = [ 'G', 'Y', 'E', 'H', 'X' ];

        if (ind != '') {
          if (T0001387.includes(ind)) {
            if (subIndFlag == 'Y') {
              FormManager.setValue('searchTerm', 'T0001387');
            } else {
              FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0001387' : _pagemodel.searchTerm);
              subIndFlag = 'Y';
            }
          } else if (T0006613.includes(ind)) {
            if (subIndFlag == 'Y') {
              FormManager.setValue('searchTerm', 'T0006613');
            } else {
              FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006613' : _pagemodel.searchTerm);
              subIndFlag = 'Y';
            }
          } else if (T0006966.includes(ind)) {
            if (subIndFlag == 'Y') {
              FormManager.setValue('searchTerm', 'T0006966');
            } else {
              FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006966' : _pagemodel.searchTerm);
              subIndFlag = 'Y';
            }
          } else if (T0007593.includes(ind)) {
            if (subIndFlag == 'Y') {
              FormManager.setValue('searchTerm', 'T0007593');
            } else {
              FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0007593' : _pagemodel.searchTerm);
              subIndFlag = 'Y';
            }
          } else if (T0006888.includes(ind)) {
            if (subIndFlag == 'Y') {
              FormManager.setValue('searchTerm', 'T0006888');
            } else {
              FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0006888' : _pagemodel.searchTerm);
              subIndFlag = 'Y';
            }
          } else {
            if (subIndFlag == 'Y') {
              FormManager.setValue('searchTerm', '');
            } else {
              FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
              subIndFlag = 'Y';
            }
          }
        } else {
          FormManager.setValue('searchTerm', '');
        }
      } else if (isuAndCtc == '04') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'I0000272');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'I0000272' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      } else if (isuAndCtc == '1R') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'I0000271');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'I0000271' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      } else if (isuAndCtc == '5E') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'A0003748');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'A0003748' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      } else if (isuAndCtc == '21') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'T0000499');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0000499' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      } else if (isuAndCtc == '8B') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'P0000007');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'P0000007' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      } else if (isuAndCtc == '5K') {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', 'T0009097');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? 'T0009097' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      } else {
        if (subIndFlag == 'Y') {
          FormManager.setValue('searchTerm', '');
        } else {
          FormManager.setValue('searchTerm', _pagemodel.searchTerm == null ? '' : _pagemodel.searchTerm);
          subIndFlag = 'Y';
        }
      }

    }
    console.log('subIndFlag====>' + subIndFlag);
  }
}

function lockSalesRepAndSortl() {
  reqType = FormManager.getActualValue('reqType');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (reqType == 'C') {
    FormManager.readOnly('repTeamMemberNo');

    var role = FormManager.getActualValue('userRole').toUpperCase();
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var countryUse = FormManager.getActualValue('countryUse');
    var isuCode = FormManager.getActualValue('isuCd');
    var clientTierCode = FormManager.getActualValue('clientTier');
    var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var isuCtc = isuCode.concat(clientTierCode);

    var lockCustSugGrpForRequester = [ 'CBBUS', 'DKBUS', 'DKINT', 'DKIBM', 'FOBUS', 'FOINT', 'FOIBM', 'ISBUS', 'ISIBM', 'ISINT', 'GLBUS', 'GLINT', 'GLIBM', 'FIBUS', 'FIINT', 'FIIBM', 'EEBUS',
        'EEINT', 'EEIBM', 'LTBUS', 'LTINT', 'LTIBM', 'LVBUS', 'LVINT', 'LVIBM', 'BUSPR', 'INTER', 'IBMEM', 'CBBUS', 'CBINT', 'PRIPE', 'DKPRI', 'FOPRI', 'GLPRI', 'ISPRI', 'FIPRI', 'LTPRI', 'LVPRI',
        'EEPRI' ];

    if (role == 'REQUESTER') {
      if (lockCustSugGrpForRequester.includes(custSubGrp)) {
        FormManager.readOnly('searchTerm');
      } else {
        FormManager.enable('searchTerm');
      }
    }

    var lockCustSugGrpForProcessor = [ 'CBBUS', 'DKBUS', 'DKINT', 'DKIBM', 'FOBUS', 'FOINT', 'FOIBM', 'ISBUS', 'ISIBM', 'ISINT', 'GLBUS', 'GLINT', 'GLIBM', 'FIBUS', 'FIINT', 'FIIBM', 'EEBUS',
        'EEINT', 'EEIBM', 'LTBUS', 'LTINT', 'LTIBM', 'LVBUS', 'LVINT', 'LVIBM', 'BUSPR', 'INTER', 'IBMEM', 'CBBUS', 'CBINT', 'PRIPE', 'DKPRI', 'FOPRI', 'GLPRI', 'ISPRI', 'FIPRI', 'LTPRI', 'LVPRI',
        'EEPRI' ];
    if (role == 'PROCESSOR') {
      if (lockCustSugGrpForProcessor.includes(custSubGrp)) {
        FormManager.readOnly('searchTerm');
      } else {
        FormManager.enable('searchTerm');
      }
    }

    if ((role == 'REQUESTER' || role == 'PROCESSOR') && cmrIssuingCntry == '702' && (countryUse == '702EE' || countryUse == '702LT' || countryUse == '702LV')) {
      if (isuCtc != null && isuCtc != '' && isuCtc != undefined && isuCtc == '34Q') {
        FormManager.readOnly('repTeamMemberNo');
      } else {
        FormManager.enable('repTeamMemberNo');
      }
    }
  }
}

function setSortlLength() {
  var countryUse = FormManager.getActualValue('countryUse');

  if (countryUse == '702EE' || countryUse == '702LT' || countryUse == '702LV') {
    $('#searchTerm').attr('maxlength', '8'); // CMR-4530
  } else {
    $('#searchTerm').attr('maxlength', '8');
  }
}

function searchTermValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var searchTerm = FormManager.getActualValue('searchTerm');
        var alphanumeric = /^[0-9a-zA-Z]*$/;

        if (_sortlHandler == null) {
          _sortlHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function(value) {
            console.log('_SortlHandler');
            sortlFlag = true;
          });
        }

        if (searchTerm == '') {
          return new ValidationResult(null, true);
        } else {
          if (!searchTerm.match(alphanumeric)) {
            return new ValidationResult({
              id : 'searchTerm',
              type : 'text',
              name : 'searchTerm'
            }, false, 'The value of SORTL is invalid, please use only alphanumeric characters.');
          }

          // CREATCMR - 5358
          var dataSearchTerm = FormManager.getActualValue('searchTerm');
          var rdcSearchTerm = FormManager.getActualValue('rdcSearchTerm');

          // CREATCMR - 5358
          // if (dataSearchTerm == rdcSearchTerm) {
          // return new ValidationResult(null, true);
          // }
          if (dataSearchTerm != rdcSearchTerm) {
            var countryUse = FormManager.getActualValue('countryUse');
            if (countryUse == '702EE' || countryUse == '702LT' || countryUse == '702LV') {
              if (searchTerm.length != 8 && sortlFlag) {
                return new ValidationResult({
                  id : 'searchTerm',
                  type : 'text',
                  name : 'searchTerm'
                }, false, 'SORTL should be exactly 8 digits long.');
              }
              return new ValidationResult(null, true);
            } else {
              if (searchTerm.length != 8) {
                return new ValidationResult({
                  id : 'searchTerm',
                  type : 'text',
                  name : 'searchTerm'
                }, false, 'SORTL should be exactly 8 digits long.');
              }
              return new ValidationResult(null, true);
            }
          }

        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function hideACAdminDSC() {
  cmr.hideNode("container-EngineeringBo");
}
// CREATCMR-2674

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
// CREATCMR-4200
function lockIBMTabFields() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var subGrpBPCds = [ 'BUSPR', 'CBBUS', 'DKBUS', 'ISBUS', 'FOBUS', 'GLBUS', 'FIBUS', 'LTBUS', 'LVBUS', 'EEBUS' ];

  reqType = FormManager.getActualValue('reqType');

  if (reqType == 'C' && role == 'REQUESTER') {
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('company');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('dunsNo');
    if (!subGrpBPCds.includes(custSubGrp)) {
      FormManager.readOnly('ppsceid');
    }
  }
}

// CREATCMR-4293
function setCTCValues() {

  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  // Business Partner
  var custSubGrpForBusinessPartner = [ 'BUSPR', 'CBBUS', 'DKBUS', 'EEBUS', 'FIBUS', 'FOBUS', 'GLBUS', 'ISBUS', 'LTBUS', 'LVBUS' ];

  // Business Partner
  if (custSubGrpForBusinessPartner.includes(custSubGrp)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '8B') {
      FormManager.setValue('clientTier', '');
    }
  }

  // Internal
  var custSubGrpForInternalIbme = [ 'DKINT', 'DKIBM', 'FOINT', 'FOIBM', 'ISIBM', 'ISINT', 'GLINT', 'GLIBM', 'FIINT', 'FIIBM', 'EEINT', 'EEIBM', 'LTINT', 'LTIBM', 'LVINT', 'LVIBM', 'INTER', 'IBMEM',
      'CBINT' ];

  // Internal
  if (custSubGrpForInternalIbme.includes(custSubGrp)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '21') {
      FormManager.setValue('clientTier', '');
    }
  }
}

function searchTermCodeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqTyp = FormManager.getActualValue('reqType');
        var isuCode = FormManager.getActualValue('isuCd');
        var clientTierCode = FormManager.getActualValue('clientTier');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var searchTerm = FormManager.getActualValue('searchTerm');
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        var countryUse = FormManager.getActualValue('countryUse');
        var isuCtc = isuCode.concat(clientTierCode);
        var subIndustry = FormManager.getActualValue('subIndustryCd');
        var ind = subIndustry.substring(0, 1);

        var scenariosToBlock = [ 'CBBUS', 'DKBUS', 'DKINT', 'DKIBM', 'FOBUS', 'FOINT', 'FOIBM', 'ISBUS', 'ISIBM', 'ISINT', 'GLBUS', 'GLINT', 'GLIBM', 'FIBUS', 'FIINT', 'FIIBM', 'EEBUS', 'EEINT',
            'EEIBM', 'LTBUS', 'LTINT', 'LTIBM', 'LVBUS', 'LVINT', 'LVIBM', 'BUSPR', 'INTER', 'IBMEM', 'CBBUS', 'CBINT' ];

        var accSeq_678 = {
          '34Q' : [ 'T0006607', 'T0006644', 'T0006880', 'T0006881', 'T0001375' ],
          '36Y' : [ 'T0007973', 'T0010026', 'T0007977' ],
          '32T' : [ 'T0010429', 'T0010431', 'T0010432', 'T0010427', 'T0010419', 'T0010428' ],
          '5K' : [ 'T0009096' ],
          '04' : [ 'A0001243', 'A0004750' ],
          '19' : [ 'A0005230' ]
        };
        var accSeq_678IS = {
          '34Q' : [ 'T0001376', 'T0007879' ],
          '36Y' : [ 'T0007973', 'T0010026', 'T0007977' ],
          '32T' : [ 'T0010429', 'T0010431', 'T0010432', 'T0010427', 'T0010419', 'T0010428' ],
          '5K' : [ 'T0009096' ],
          '04' : [ 'A0001243', 'A0004750' ],
          '19' : [ 'A0005230' ]
        };
        var accSeq_702 = {
          '32T' : [ 'T0010461', 'T0010463' ],
          '34Q' : [ 'T0001379', 'T0006609', 'T0006949', 'T0006974', 'T0007561', 'T0007864' ],
          '36Y' : [ 'T0007974', 'T0010025' ],
          '5K' : [ 'T0009094' ],
          '04' : [ 'A0004751' ]
        };
        var accSeq_702EE = {
          '34Q' : [ 'T0004422' ],
          '36Y' : [ 'T0008052' ],
          '5K' : [ 'T0009073' ]
        };
        var accSeq_702LT = {
          '34Q' : [ 'T0004394' ],
          '36Y' : [ 'T0008054' ],
          '5K' : [ 'T0009072' ]
        };
        var accSeq_702LV = {
          '34Q' : [ 'T0004390' ],
          '36Y' : [ 'T0008053' ],
          '5K' : [ 'T0009071' ]
        };
        var accSeq_806 = {
          '34Q' : [ 'T0001383', 'T0006611' ],
          '36Y' : [ 'T0007975', 'T0010024' ],
          '32T' : [ 'T0010317', 'T0010316' ],
          '5K' : [ 'T0009095' ],
          '04' : [ 'A0004752' ]
        };
        var accSeq_846 = {
          '34Q' : [ 'T0001387', 'T0006613', 'T0006888', 'T0006966', 'T0007593' ],
          '36Y' : [ 'T0007976', 'T0010027', 'T0010028' ],
          '32T' : [ 'T0010453', 'T0010444', 'T0010448', 'T0010450', 'T0010445', 'T0010446' ],
          '5K' : [ 'T0009097' ],
          '04' : [ 'I0000272' ],
          '1R' : [ 'I0000271' ],
          '3T' : [ 'A0003748' ]
        };
        if (reqTyp == 'C') {
          if (!scenariosToBlock.includes(custSubGrp) && isuCtc != '' && isuCtc != undefined && isuCtc != null) {
            if (cmrIssuingCntry == '678') {
              if (countryUse == '678' || countryUse == '678FO' || countryUse == '678GL') {
                // Denmark, Faroe Islands, Greenland
                var subIndSearchTerm = {
                  'T0001375' : [ 'K', 'U', 'A', 'F', 'N', 'S' ],
                  'T0006880' : [ 'D', 'W', 'T', 'R' ],
                  'T0006881' : [ 'V', 'J', 'P', 'L', 'M' ],
                  'T0006644' : [ 'G', 'Y', 'E', 'H', 'X' ],
                  'T0006607' : [ 'B', 'C' ]
                };
                if (accSeq_678.hasOwnProperty(isuCtc) && !accSeq_678[isuCtc].includes(searchTerm)) {
                  return new ValidationResult({
                    id : 'searchTerm',
                    type : 'text',
                    name : 'searchTerm'
                  }, false, 'SearchTerm can only accept ' + accSeq_678[isuCtc]);
                }
                if (accSeq_678.hasOwnProperty(isuCtc) && accSeq_678[isuCtc].includes(searchTerm)) {
                  if (ind != '' && subIndSearchTerm.hasOwnProperty(searchTerm) && !subIndSearchTerm[searchTerm].includes(ind)) {
                    console.log(subIndSearchTerm[searchTerm]);
                    return new ValidationResult({
                      id : 'searchTerm',
                      type : 'text',
                      name : 'searchTerm'
                    }, false, 'SORTL and Subindustry combination mismatch.');
                  }
                }
              } else if (countryUse == '678IS') {
                var subIndSearchTerm = {
                  'T0007879' : [ 'B', 'C' ]
                };
                if (accSeq_678IS.hasOwnProperty(isuCtc) && !accSeq_678IS[isuCtc].includes(searchTerm)) {
                  return new ValidationResult({
                    id : 'searchTerm',
                    type : 'text',
                    name : 'searchTerm'
                  }, false, 'SearchTerm can only accept ' + accSeq_678IS[isuCtc]);
                }
                if (accSeq_678IS.hasOwnProperty(isuCtc) && accSeq_678IS[isuCtc].includes(searchTerm)) {
                  if (ind != '' && subIndSearchTerm.hasOwnProperty(searchTerm) && !subIndSearchTerm[searchTerm].includes(ind)) {
                    return new ValidationResult({
                      id : 'searchTerm',
                      type : 'text',
                      name : 'searchTerm'
                    }, false, 'SearchTerm and SubIndustry combination mismatch.');
                  }
                }
              }
            } else if (cmrIssuingCntry == '702') {
              if (countryUse == '702') {
                if (accSeq_702.hasOwnProperty(isuCtc) && !accSeq_702[isuCtc].includes(searchTerm)) {
                  return new ValidationResult({
                    id : 'searchTerm',
                    type : 'text',
                    name : 'searchTerm'
                  }, false, 'SearchTerm can only accept ' + accSeq_702[isuCtc]);
                }
              } else if (countryUse == '702EE') {
                if (accSeq_702EE.hasOwnProperty(isuCtc) && !accSeq_702EE[isuCtc].includes(searchTerm)) {
                  return new ValidationResult({
                    id : 'searchTerm',
                    type : 'text',
                    name : 'searchTerm'
                  }, false, 'SearchTerm can only accept ' + accSeq_702EE[isuCtc]);
                }
              } else if (countryUse == '702LT') {
                if (accSeq_702LT.hasOwnProperty(isuCtc) && !accSeq_702LT[isuCtc].includes(searchTerm)) {
                  return new ValidationResult({
                    id : 'searchTerm',
                    type : 'text',
                    name : 'searchTerm'
                  }, false, 'SearchTerm can only accept ' + accSeq_702LT[isuCtc]);
                }
              } else if (countryUse == '702LV') {
                if (accSeq_702LV.hasOwnProperty(isuCtc) && !accSeq_702LV[isuCtc].includes(searchTerm)) {
                  return new ValidationResult({
                    id : 'searchTerm',
                    type : 'text',
                    name : 'searchTerm'
                  }, false, 'SearchTerm can only accept ' + accSeq_702LV[isuCtc]);
                }
              }
            } else if (cmrIssuingCntry == '806') {
              if (accSeq_806.hasOwnProperty(isuCtc) && !accSeq_806[isuCtc].includes(searchTerm)) {
                return new ValidationResult({
                  id : 'searchTerm',
                  type : 'text',
                  name : 'searchTerm'
                }, false, 'SearchTerm can only accept ' + accSeq_806[isuCtc]);
              }
            } else if (cmrIssuingCntry == '846') {
              if (accSeq_846.hasOwnProperty(isuCtc) && !accSeq_846[isuCtc].includes(searchTerm)) {
                return new ValidationResult({
                  id : 'searchTerm',
                  type : 'text',
                  name : 'searchTerm'
                }, false, 'SearchTerm can only accept ' + accSeq_846[isuCtc]);
              }
            } else {
              return new ValidationResult(null, true);
            }
          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function clientTierCodeValidator() {
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');
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
      }, false, 'Client Tier can only accept \'Q\'.');
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
      }, false, 'Client Tier can only accept \'Y\'.');
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

function addVatIndValidator() {
  var _vatHandler = null;
  var _vatIndHandler = null;
  var vat = FormManager.getActualValue('vat');
  var vatInd = FormManager.getActualValue('vatInd');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    FormManager.resetValidations('vat');
    FormManager.readOnly('vat');
  } else {
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
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
    if ((vat && dojo.string.trim(vat) == '') || (vat && dojo.string.trim(vat) == null) && vatInd == 'N') {
      FormManager.resetValidations('vat');
    }
  }
}

// CREATCMR-788
function addressQuotationValidatorNORS() {
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, [ 'Additional Info' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Att. Person' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Street Con\'t' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);

}
function skipStateProvForFO() {
  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry == 'FO') {
    cmr.hideNode('StateProv');
  } else {
    cmr.showNode("StateProv");
  }
}

function isuCtcSortlCovBehaviour() {
  var role = null;
  var reqTyp = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  var scenariosToBlock = [ 'CBBUS', 'DKBUS', 'DKINT', 'DKIBM', 'FOBUS', 'FOINT', 'FOIBM', 'ISBUS', 'ISIBM', 'ISINT', 'GLBUS', 'GLINT', 'GLIBM', 'FIBUS', 'FIINT', 'FIIBM', 'EEBUS', 'EEINT', 'EEIBM',
      'LTBUS', 'LTINT', 'LTIBM', 'LVBUS', 'LVINT', 'LVIBM', 'BUSPR', 'INTER', 'IBMEM', 'CBBUS', 'CBINT' ];
  if (reqTyp == 'C') {
    if (role == 'Requester') {
      FormManager.readOnly('clientTier');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('searchTerm');
    } else if (role == 'Processor' && !scenariosToBlock.includes(custSubGrp)) {
      FormManager.enable('clientTier');
      FormManager.enable('isuCd');
      FormManager.enable('searchTerm');
    }
  } else if (reqTyp == 'U' && !scenariosToBlock.includes(custSubGrp)) {
    FormManager.enable('clientTier');
    FormManager.enable('isuCd');
    FormManager.enable('searchTerm');
  }

}

dojo.addOnLoad(function() {
  GEOHandler.NORDX = [ '846', '806', '702', '678' ];

  console.log('adding NORDX functions...');
  GEOHandler.addAfterTemplateLoad(checkScenarioChanged, GEOHandler.NORDX);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.NORDX);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.NORDX);
  GEOHandler.addAfterConfig(afterConfigForNORDX, GEOHandler.NORDX);
  GEOHandler.addAddrFunction(addLandedCountryHandler, GEOHandler.NORDX);
  /*
   * GEOHandler.addAddrFunction(setAbbrevName, GEOHandler.NORDX);
   * GEOHandler.addAfterConfig(setAbbrevName, GEOHandler.NORDX);
   */
  GEOHandler.addAfterTemplateLoad(setAbbrvNmLoc, GEOHandler.NORDX);
  GEOHandler.registerValidator(addAbbrevNmLengthValidator, GEOHandler.NORDX, null, true);
  GEOHandler.addAfterConfig(addISUClientMandatory, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(setSBOFORCross, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(setSBOFORCross, GEOHandler.NORDX);
  GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.NORDX, null, true);
  GEOHandler.addAddrFunction(hidePOBoxandHandleStreet, GEOHandler.NORDX);
  GEOHandler.addAddrFunction(handleMachineType, GEOHandler.NORDX);
  GEOHandler.enableCopyAddress(GEOHandler.NORDX, validateNORDXCopy, [ 'ZI01', 'ZD01', 'ZP02' ]);
  GEOHandler.addAddrFunction(updateAddrTypeList, GEOHandler.NORDX);
  GEOHandler.registerValidator(addCrossBorderValidatorNORS, GEOHandler.NORDX, null, true);
  // GEOHandler.addAfterTemplateLoad(setSalesRepValues, GEOHandler.NORDX);
  // GEOHandler.addAfterTemplateLoad(setAdminDSCValues, GEOHandler.NORDX);
  // GEOHandler.addAfterConfig(setSalesRepValues, GEOHandler.NORDX);
  // GEOHandler.addAfterConfig(setAdminDSCValues, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(addHandlersForNORDX, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(afterConfigForNORDX, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.NORDX);
  GEOHandler.registerValidator(addNORDXAddressTypeValidator, GEOHandler.NORDX, null, true);
  // GEOHandler.registerValidator(addNORDXInstallingShipping, GEOHandler.NORDX,
  // null, true);
  GEOHandler.registerValidator(addGenericVATValidator('', 'MAIN_CUST_TAB', 'frmCMR', 'ZS01'), [ SysLoc.DENMARK, SysLoc.FINLAND, SysLoc.SWEDEN, SysLoc.NORWAY ], null, true);
  // GEOHandler.registerValidator(norwayCustomVATValidator('', 'MAIN_CUST_TAB',
  // 'frmCMR', 'ZS01'), [ SysLoc.NORWAY ], null, true);
  // GEOHandler.registerValidator(addACAdminValidator, GEOHandler.NORDX, null,
  // true);// CMR-1746
  GEOHandler.registerValidator(addISICValidator, GEOHandler.NORDX, null, true);// CMR-1993
  GEOHandler.addAddrFunction(disableLandCntry, GEOHandler.NORDX);
  GEOHandler.addAddrFunction(loadMachinesList, GEOHandler.NORDX);
  GEOHandler.addAddrFunction(handleMahcineModel, GEOHandler.NORDX);
  // GEOHandler.addAddrFunction(addPhoneValidatorNORDX, GEOHandler.NORDX);
  // GEOHandler.registerValidator(machineValidator, GEOHandler.NORDX, null,
  // true);
  /* 1596058: All Nordics - DPL check failed */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.NORDX, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(setSBO, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(setSBO, GEOHandler.NORDX);
  GEOHandler.addAddrFunction(streetContControll, GEOHandler.NORDX);

  GEOHandler.addAfterConfig(requestingLobOnChange, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(requestingLobOnChange, GEOHandler.NORDX);

  // CREATCMR-1709
  // CREATCMR-5128
  // commented for CREATCMR-7669
  //GEOHandler.addAfterConfig(resetCustPrefLang, GEOHandler.NORDX);
  //GEOHandler.addAfterTemplateLoad(resetCustPrefLang, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(onSubIndustryChange, GEOHandler.NORDX);//
  // CMR-1709

  // CREATCMR-1690
  GEOHandler.registerValidator(addCmrNoValidatorForNordx, GEOHandler.NORDX);

  // CREATCMR-1657
  GEOHandler.addAfterConfig(lockDunsNo, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(kuklaShowAndHide, GEOHandler.NORDX);

  // CREATCMR-2430
  GEOHandler.addAfterConfig(setCustPrefLangByCountry, GEOHandler.NORDX);
  // CREATCMR-1689
  GEOHandler.addAddrFunction(setAbbrevNmAddressSave, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(setSensitiveFlag, GEOHandler.NORDX);

  // CREATCMR-2674
  GEOHandler.registerValidator(searchTermValidation, GEOHandler.NORDX, null, true);
  // GEOHandler.addAfterConfig(searchTermValidation, GEOHandler.NORDX);
  // GEOHandler.addAfterTemplateLoad(searchTermValidation, GEOHandler.NORDX);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.NORDX, null, true);
  // CREATCMR-4200
  GEOHandler.addAfterConfig(lockIBMTabFields, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(lockIBMTabFields, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(setCTCValues, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(setKukalValuesByCustSubGrp, GEOHandler.NORDX);

  // CREATCMR-4293
  GEOHandler.addAfterTemplateLoad(setCTCValues, GEOHandler.NORDX);
  GEOHandler.registerValidator(clientTierValidator, GEOHandler.NORDX, null, true);
  
  GEOHandler.registerValidator(addVatIndValidator, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(setVatIndFieldsForGrp1AndNordx, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(setVatIndFieldsForGrp1AndNordx, GEOHandler.NORDX);
  
  GEOHandler.addAfterConfig(skipStateProvForFO, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(lockTaxCode, GEOHandler.NORDX);
  GEOHandler.registerValidator(searchTermCodeValidator, GEOHandler.NORDX, null, true);

  GEOHandler.addAfterTemplateLoad(setSalesRepValues, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(addCtcHandler, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(addIsuHandler, GEOHandler.NORDX);
  
  GEOHandler.addAfterConfig(resetVATValidationsForPayGo, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(resetVATValidationsForPayGo, GEOHandler.NORDX);
});
