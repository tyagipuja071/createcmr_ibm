/* Register NORDX Javascripts */
var _addrTypesForNORDX = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'ZP02' ];
var _poBOXHandler = [];
var _MachineHandler = [];
var _collCdArraySubTypes = [ 'INTER', 'INTSO', 'CBINT', 'CBISO' ];
var EU_COUNTRIES = [ "AT", "BE", "BG", "HR", "CY", "CZ", "DE", "DK", "EE", "ES", "GL", "GR", "FI", "FO", "FR", "GB", "HU", "IE", "IT", "LT", "LV", "LU", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK" ];
var reqType = null;
function afterConfigForNORDX() {
  reqType = FormManager.getActualValue('reqType');
  var role = null;
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  FormManager.readOnly('cmrOwner');
  FormManager.readOnly('salesBusOffCd');
  if (FormManager.getActualValue('countryUse') == '702') {
    FormManager.setValue('custPrefLang', 'U');
  }
  if (reqType == 'U') {
    if (FormManager.getActualValue('viewOnlyPage') == 'true') {
      return;
    }
    FormManager.enable('collectionCd');
    FormManager.resetValidations('inacCd');
    FormManager.resetValidations('sitePartyId');
    FormManager.resetValidations('engineeringBo');
  }
  
  if (reqType == 'C') {
    FormManager.readOnly('collectionCd');
    FormManager.readOnly('capInd');
    FormManager.readOnly('modeOfPayment');
    FormManager.setValue('capInd', true);
    if (role == 'Requester') {
      FormManager.readOnly('abbrevNm');
      FormManager.readOnly('abbrevLocn');
      if ((custSubGrp == 'LTBUS' || custSubGrp == 'EEBUS' || custSubGrp == 'LVBUS') || (custSubGrp == 'CBBUS' && geoCd != 'FI')) {
        FormManager.readOnly('engineeringBo');
      }
    }
    if (role == 'Processor') {
      FormManager.enable('abbrevNm');
      FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
      FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
      if ((custSubGrp != 'LTBUS' && custSubGrp != 'EEBUS' && custSubGrp != 'LVBUS' && custSubGrp != 'CBBUS')) {
        FormManager.addValidator('engineeringBo', Validators.REQUIRED, [ 'A/C Admin DSC' ], 'MAIN_IBM_TAB');
      } else {
        FormManager.resetValidations('engineeringBo');
        FormManager.readOnly('engineeringBo');
      }
    }
  }
  if (reqType == 'C') {
    if (!(_collCdArraySubTypes.indexOf(custSubGrp) > -1) && custSubGrp.substring(2, 5) != 'INT' && custSubGrp.substring(2, 5) != 'ISO') {
      FormManager.limitDropdownValues(FormManager.getField('collectionCd'), [ '' ]);
    } else {
      FormManager.resetDropdownValues(FormManager.getField('collectionCd'), [ '' ]);
    }
  }

  if (role == 'Processor' && reqType == 'C') {
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
  }

  var landCntry = '';
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion && cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }
  setVatValidatorNORDX();
  setSalesRepValues();
  setAdminDSCValues();
  setTaxCdValuesCROSS();
  setSBOForFinlandSubRegion();
}

function disableLandCntry() {
  var custType = FormManager.getActualValue('custGrp');
  if ((custType == 'LOCAL' || custType.substring(2, 5) == 'LOC') && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
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
}

/**
 * After config handlers
 */
var _ISUHandler = null;
var _CTCHandler = null;
var _SalesRepHandler = null;
var _AdminDSCHandler = null;
var _IMSHandler = null;
var _vatExemptHandler = null;
var _poSteertNorwayFin = null;
var _PostalCodeHandler = null;
var _ExpediteHandler = null;

function addHandlersForNORDX() {

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValues(value);
    });
  }

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setSalesRepValues(value);
    });
  }

  if (_SalesRepHandler == null) {
    _SalesRepHandler = dojo.connect(FormManager.getField('repTeamMemberNo'), 'onChange', function(value) {
      setAdminDSCValues(value);
    });
  }

  if (_IMSHandler == null && FormManager.getActualValue('cmrIssuingCntry')) {
    _IMSHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      setSalesRepValues();
    });
  }

  if (_PostalCodeHandler == null) {
    _PostalCodeHandler = dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
      setSBO(value);
    });
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorNORDX();
    });
  }

  if (_poSteertNorwayFin == null) {
    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.NORWAY
        || (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.FINLAND && FormManager.getActualValue('countryUse') == SysLoc.FINLAND)) {
      _poSteertNorwayFin = dojo.connect(FormManager.getField('poBox'), 'onChange', function(value) {
        if (FormManager.getActualValue('poBox').length > 0 && FormManager.getActualValue('importInd') != 'Y') {
          FormManager.disable('addrTxt');
          FormManager.disable('addrTxt2');
        } else {
          FormManager.enable('addrTxt');
          FormManager.enable('addrTxt2');
        }

      });

      _poSteertNorwayFin = dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {

        if (FormManager.getActualValue('addrTxt').length > 0 && FormManager.getActualValue('importInd') != 'Y') {
          FormManager.disable('poBox');
        } else {
          if ((FormManager.getField('addrType_ZI01').checked || FormManager.getField('addrType_ZD01').checked) && FormManager.getActualValue('importInd') != 'Y' ) {

          } else {
            FormManager.enable('poBox');
          }
        }

      });
      _poSteertNorwayFin = dojo.connect(FormManager.getField('addrTxt2'), 'onChange', function(value) {

        if (FormManager.getActualValue('addrTxt2').length > 0 && FormManager.getActualValue('importInd') != 'Y') {
          FormManager.disable('poBox');
        } else {
          if ((FormManager.getField('addrType_ZI01').checked || FormManager.getField('addrType_ZD01').checked) && FormManager.getActualValue('importInd') != 'Y') {

          } else {
            FormManager.enable('poBox');
          }
        }

      });
    }
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
function setVatValidatorNORDX() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (!dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
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
      for ( var i = 0; i < results.length; i++) {
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

function setSBOFORCross(){
  
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  
  if (cntry != SysLoc.NORWAY) {
    return;
  }
  
  var custGrp = FormManager.getActualValue('custGrp');
    if (custGrp == 'CROSS'){
      FormManager.setValue('salesBusOffCd', '100');
    }
}

/*
 * NORDX - sets Sales rep based on isuCtc
 */
function setSalesRepValues(clientTier) {
  
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

  var salesReps = [];
  if (isuCd != '') {
    var isuCtc = isuCd + clientTier;
    var qParams = null;
    var results = null;

    // SalRep will be based on IMS for 32S/32T for Finland Subregion
    if (ims.length > 1 && (isuCtc == '32S' || isuCtc == '32T') && (geoCd == 'EE' || geoCd == 'LT' || geoCd == 'LV' || geoCd == 'IS' || cntry == '806')) {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry + geoCd,
        ISU : '%' + isuCd + clientTier + '%',
        CLIENT_TIER : '%%'
      };
      results = cmr.query('GET.SRLIST.BYISUCTC', qParams);
    } else if (ims != '' && ims.length > 1 && (isuCtc == '32S' || isuCtc == '32T') && (cntry == '846')) {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry + geoCd,
        ISU : '%' + isuCd + clientTier + '%',
        UPDATE_BY_ID : '%' + ims.substring(0, 1) + '%'
      };
      results = cmr.query('GET.SRLIST.SWEDEN', qParams);
    } else if (ims != '' && ims.length > 1 && (isuCtc == '32S' || isuCtc == '32T')) {
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
        ISU : '%' + isuCd + clientTier + '%',
      };
      results = cmr.query('GET.SRLIST.BYISU', qParams);
    }

    if (results != null) {
      for ( var i = 0; i < results.length; i++) {
        salesReps.push(results[i].ret1);
      }
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesReps);
      if (salesReps.length == 1) {
        FormManager.setValue('repTeamMemberNo', salesReps[0]);
      }
    }
  }
  var cntryUse = FormManager.getActualValue('countryUse');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if ((custSubGrp == 'CBCOM' || custSubGrp == 'CBISO')) {
    if ((cntryUse == '678IS')) {
      FormManager.setValue('repTeamMemberNo', 'MSD997');
    } else if ((cntryUse == '702FI')) {
      FormManager.setValue('repTeamMemberNo', 'MSF107');
    } else if ((cntryUse == '702EE')) {
      FormManager.setValue('repTeamMemberNo', 'NOREP9');
    } else if ((cntryUse == '702LT')) {
      FormManager.setValue('repTeamMemberNo', 'NOREP9');
    } else if ((cntryUse == '702LV')) {
      FormManager.setValue('repTeamMemberNo', 'NOREP9');
    }
  }
}

/*
 * Set Admin DSC Values based on isuCtc and SalsRep
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

  var adminDSC = [];

  if (repTeamMemberNo != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry + geoCd,
      ISU : '%' + isuCd + clientTier + '%',
      REP_TEAM_CD : '%' + repTeamMemberNo + '%'
    };
    var results = cmr.query('GET.DSCLIST.BYSR', qParams);
    if (results != null) {
      for ( var i = 0; i < results.length; i++) {
        adminDSC.push(results[i].ret1);
      }
      if (adminDSC != null) {
        FormManager.limitDropdownValues(FormManager.getField('engineeringBo'), adminDSC);
        if (adminDSC.length == 1) {
          FormManager.setValue('engineeringBo', adminDSC[0]);
        }
      }
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
        FormManager.setValue('abbrevNm', cmpnyName.substring(0, 22));
      } else {
        FormManager.setValue('abbrevNm', cmpnyName);
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

function machineValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqId = FormManager.getActualValue('reqId');
        var reqParam = {
          _qall : 'Y',
          REQ_ID : reqId,
          ADDR_TYPE : "ZP02",
        };
        var results = cmr.query('GET_ZP02_COUNT', reqParam);
        if (results != null) {
          for ( var i = 0; i < results.length; i++) {
            var ADDR_SEQ1 = results[i].ret1;
            var reqParam1 = {
              REQ_ID : reqId,
              ADDR_TYPE : "ZP02",
              ADDR_SEQ : ADDR_SEQ1,
            };
            var newResults = cmr.query('ZP02_SEARCH_MACHINES', reqParam1);
            if (newResults.ret1 == '0') {
              return new ValidationResult(null, false, 'All Additional Installing Address should have at least one Machine');
            }

          }
          return new ValidationResult(null, true);
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');

}

function handleMahcineModel() {
  if (FormManager.getActualValue('cmrIssuingCntry') == '702' || reqType == 'C') {
    cmr.hideNode("machineSerialDiv");
    return;
  }
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    if (FormManager.getActualValue('addrType') == 'ZI01' || FormManager.getActualValue('addrType') == 'ZP02') {
      cmr.showNode("machineSerialDiv");

    } else {
      cmr.hideNode("machineSerialDiv");
    }
  }
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

          for ( var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
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
          if (billingCnt == 0 || mailingCnt == 0) {
            return new ValidationResult(null, false, 'Billing, Mailing address are mandatory.');
          } else if (installingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Main Installing address can be defined. Please remove the other Main Installing address.');
          } else if (billingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Billing address can be defined. Please remove the additional Billing address.');
          } else if (mailingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Mailing address can be defined. Please remove the additional Mailing address.');
          } else if (eplCnt > 1) {
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
        var reqId = FormManager.getActualValue('reqId');
        var addr = 'ZD01';
        var qParams = {
          _qall : 'Y',
          REQID : reqId,
          ADDR_TYPE : addr
        };

        var results = cmr.query('GET_STREET_ADDRSEQ', qParams);
        if (results != null) {
          for ( var i = 0; i < results.length; i++) {
            if (results[i].ret1.length < 1) {
              shippingBool = false;
            }
          }
        }
        addr = 'ZI01';
        qParams = {
          _qall : 'Y',
          REQID : reqId,
          ADDR_TYPE : addr
        };

        results = cmr.query('GET_STREET_ADDRSEQ', qParams);
        if (results != null) {
          for ( var i = 0; i < results.length; i++) {
            if (results[i].ret1.length < 1) {
              installBool = false;
            }
          }
        }

        if (shippingBool && installBool) {
          return new ValidationResult(null, true);
        } else if (!shippingBool && !installBool) {
          return new ValidationResult(null, false, 'Street should not be empty in Installing and Shipping Address.');
        } else if (!shippingBool) {
          return new ValidationResult(null, false, 'Street should not be empty in Shipping Address.');
        } else if (!installBool) {
          return new ValidationResult(null, false, 'Street should not be empty in Installing Address.');
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
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
  }
}

function handleMachineType() {
  if (FormManager.getActualValue('cmrIssuingCntry') == '702' || reqType == 'C') {
    cmr.hideNode("machineSerialDiv");
    return;
  }
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    cmr.hideNode("machineSerialDiv");
    for ( var i = 0; i < _addrTypesForNORDX.length; i++) {
      _MachineHandler[i] = null;
      if (_MachineHandler[i] == null) {
        var xx = FormManager.getField('addrType_ZP02');
        _MachineHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForNORDX[i]), 'onClick', function(value) {
          if (FormManager.getField('addrType_ZI01').checked || FormManager.getField('addrType_ZP02').checked) {
            cmr.showNode("machineSerialDiv");
            cmr.hideNode('addMachineButton');

          } else {
            cmr.hideNode("machineSerialDiv");
          }
        });
      }
    }
  }

  if (cmr.addressMode == 'updateAddress') {
    if (FormManager.getActualValue('addrType') == 'ZI01' || FormManager.getActualValue('addrType') == 'ZP02') {
      cmr.showNode("machineSerialDiv");
      cmr.showNode("addMachineButton");

    } else {
      cmr.hideNode("machineSerialDiv");
    }
  }
}

function hidePOBoxandHandleStreet() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    for ( var i = 0; i < _addrTypesForNORDX.length; i++) {
      _poBOXHandler[i] = null;
      if (_poBOXHandler[i] == null) {
        var poValue = FormManager.getActualValue('poBox');
        var phValue = FormManager.getActualValue('custPhone');
        _poBOXHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForNORDX[i]), 'onClick', function(value) {
          setPOBOXandSteet(poValue);
          setPhone(phValue);
        });
      }
    }
  }
  if (cmr.addressMode == 'updateAddress') {
    if (FormManager.getActualValue('addrType') == 'ZI01' || FormManager.getActualValue('addrType') == 'ZD01' || FormManager.getActualValue('addrType') == 'ZP02') {
      FormManager.disable('poBox');
      FormManager.setValue('poBox', '');
      var cntryRegion = FormManager.getActualValue('countryUse');
      if (cntryRegion != '' && (cntryRegion == '678FO' || cntryRegion == SysLoc.DENMARK)) {
        FormManager.addValidator('addrTxt', Validators.REQUIRED, [ 'Street' ], '');
      }
    } else {
      FormManager.enable('poBox');
      FormManager.resetValidations('addrTxt');
    }
    if (FormManager.getActualValue('addrType') == 'ZS01') {
      FormManager.enable('custPhone');
    } else {
      FormManager.disable('custPhone');
      FormManager.setValue('custPhone', '');

    }
  }
}

function setPOBOXandSteet(value) {
  if (FormManager.getField('addrType_ZI01').checked || FormManager.getField('addrType_ZD01').checked || FormManager.getField('addrType_ZP02').checked) {
    FormManager.disable('poBox');
    FormManager.setValue('poBox', '');
    var cntryRegion = FormManager.getActualValue('countryUse');
    if (cntryRegion != '' && (cntryRegion == '678FO' || cntryRegion == SysLoc.DENMARK)) {
      FormManager.addValidator('addrTxt', Validators.REQUIRED, [ 'Street' ], '');
    }

  } else {
    FormManager.enable('poBox');
    FormManager.setValue('poBox', value);
    FormManager.resetValidations('addrTxt');
  }
}

function setPhone(value) {
  if (FormManager.getField('addrType_ZS01').checked) {
    FormManager.enable('custPhone');
    FormManager.setValue('custPhone', value);
  } else {

    FormManager.disable('custPhone');
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
        if (cntry == SysLoc.NORWAY || cntry == SysLoc.SWEDEN) {
          var addrTxt = FormManager.getActualValue('addrTxt');
          var addrTxt2 = FormManager.getActualValue('addrTxt2');

          var val = addrTxt;
          if (addrTxt2 != '') {
            val += addrTxt2;
            if (val.length > 30) {
              return new ValidationResult(null, false, 'Total computed length of Street and Street Con\'t should not exceed 30 characters.');
            }
          } else {
            if (val.length > 30) {
              return new ValidationResult(null, false, 'Street should not exceed 30 characters.');
            }
          }
        }
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
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var showError = false;
        
        if (FormManager.getActualValue('custNm1') != ''
          && FormManager.getActualValue('custNm2') != ''
          && FormManager.getActualValue('custNm4') != ''
          && FormManager.getActualValue('addrTxt') != ''
            && FormManager.getActualValue('poBox') != ''
              && FormManager.getActualValue('postCd') != ''
                  && FormManager.getActualValue('city1') != '') {
          showError = true;
        }else{
          showError = false;
        }
        
        var cntryRegion = FormManager.getActualValue('countryUse');
        if (cntryRegion != '' && (cntryRegion == '678FO' || cntryRegion == '678GL' || cntryRegion == '678IS' || cntryRegion == '702EE' || cntryRegion == '702LT' || cntryRegion == '702LV')) {

          if (showError) {
            return new ValidationResult(null, false, 'For Customer Name Con\'t and Att. Person, only one can be filled.');
          }
        } else {
          var cntry = FormManager.getActualValue('cmrIssuingCntry');

          if (cntry == SysLoc.SWEDEN || cntry == SysLoc.NORWAY || cntry == SysLoc.DENMARK || cntry == SysLoc.FINLAND) {

            var landCntry = FormManager.getActualValue('landCntry');

            if (cntry == SysLoc.SWEDEN && landCntry != "SE") {
              if (showError) {
                return new ValidationResult(null, false, 'For Customer Name Con\'t and Att. Person, only one can be filled.');
              }
            }
            if (cntry == SysLoc.NORWAY && landCntry != "NO") {
              if (showError) {
                return new ValidationResult(null, false, 'For Customer Name Con\'t and Att. Person, only one can be filled.');
              }
            }
            if (cntry == SysLoc.DENMARK && landCntry != "DK") {
              if (showError) {
                return new ValidationResult(null, false, 'For Customer Name Con\'t and Att. Person, only one can be filled.');
              }
            }
            if (cntry == SysLoc.FINLAND && landCntry != "FI") {
              if (showError) {
                return new ValidationResult(null, false, 'For Customer Name Con\'t and Att. Person, only one can be filled.');
              }
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
        if (cntry == SysLoc.DENMARK || cntry == SysLoc.FINLAND) {
          var cntryRegion = FormManager.getActualValue('countryUse');
          if (cntryRegion != '' && (cntryRegion != SysLoc.FINLAND)) {

            var addrFldCnt = 0;
            if (FormManager.getActualValue('poBox') != '') {
              addrFldCnt++;
            }
            if (FormManager.getActualValue('addrTxt') != '') {
              addrFldCnt++;
            }
            /*if (dojo.byId('poBox').getAttribute('aria-readonly') == 'true') {
              if (addrFldCnt < 1) {
                return new ValidationResult({
                  id : 'addrTxt',
                }, false, '');
              }
            }*/
            if (addrFldCnt < 1) {
              return new ValidationResult(null, false, 'For Street and PostBox, atleast one should be filled.');
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
            return new ValidationResult(null, false, 'PostBox should be Numeric.');
          }

        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // Machine Type and Serial Number
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        if (FormManager.getActualValue('machineTyp').length == 0 && FormManager.getActualValue('machineSerialNo').length == 0) {
          if (cmr.addressMode == 'updateAddress') {
            if (FormManager.getActualValue('addrType') == 'ZP02') {
              var qParams = {
                _qall : 'Y',
                REQ_ID : FormManager.getActualValue('reqId'),
                ADDR_TYPE : FormManager.getActualValue('addrType'),
                ADDR_SEQ : FormManager.getActualValue('addrSeq'),
              };
              var results = cmr.query('SEARCH_MACHINES', qParams);
              if (results != null) {
                if (results.length > 0) {
                  return new ValidationResult(null, true);
                } else {
                  return new ValidationResult({
                    id : 'machineTyp',
                  }, false, 'Machine Type and Serial Number are Mandatory.');
                }
              } else {
                return new ValidationResult({
                  id : 'machineTyp',
                }, false, 'Machine Type and Serial Number are Mandatory.');
              }
            } else {

              return new ValidationResult(null, true);
            }

          } else if (FormManager.getActualValue('addrType') == 'ZP02' && (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress')) {
            // FormManager.addValidator('machineTyp', Validators.REQUIRED, [
            // 'Machine Type' ], '');
            // FormManager.addValidator('machineSerialNo', Validators.REQUIRED,
            // [ 'Machine Serial Number' ], '');
            return new ValidationResult({
              id : 'machineTyp',
            }, false, 'Machine Type and Serial Number are Mandatory.');
          }

        }
        if ((FormManager.getActualValue('addrType') == 'ZP02') && reqType != 'C') {

          if (FormManager.getActualValue('machineTyp').length != 4 && FormManager.getActualValue('machineSerialNo').length != 7) {
            return new ValidationResult({
              id : 'machineTyp',
            }, false, 'Machine Type and Serial number should be 4 and 7 characters long.');
          }
          if (FormManager.getActualValue('machineTyp').length != 4) {
            return new ValidationResult({
              id : 'machineTyp',
            }, false, 'Machine Type should be 4 characters long.');
          }
          if (FormManager.getActualValue('machineSerialNo').length != 7) {
            return new ValidationResult({
              id : 'machineSerialNo',
            }, false, 'Machine Serial Number should be 7 characters long.');
          }
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqId = FormManager.getActualValue('reqId');
        var addrType = FormManager.getActualValue('addrType');
        var reqType = FormManager.getActualValue('reqType');
        var addrSeq = FormManager.getActualValue('addrSeq');

        if (addrSeq != null && addrType == 'ZP02' && reqType != 'C') {
          var reqParam = {
            REQ_ID : reqId,
            ADDR_TYPE : "ZP02",
            ADDR_SEQ : addrSeq,
          };
          var results = cmr.query('ZP02_SEARCH_MACHINES', reqParam);
          if (results.ret1 == '0') {
            return new ValidationResult(null, false, 'All Additional Installing Address should have at least one entry of Machine details');
          }
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
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
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion == '702') {
    if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
      cmr.hideNode('radiocont_ZD01');
    }
  }
  if ((addressMode == 'newAddress' || addressMode == 'copyAddress') && cmr.currentRequestType == 'C') {
    cmr.hideNode('radiocont_ZP02');
  }

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
    FormManager.setValue('abbrevNm', abbrvNm);
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
		for ( var l = 0; l < newResults.length; l++) {
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
	for ( var i = 0; i < machType.length; i++) {
		machineDetails.push([ machType[i], serialNo[i] ]);
	}
	var row = table.insertRow(-1);
	for ( var i = 0; i < columnCount; i++) {
		var headerCell = document.createElement("TH");
		headerCell.innerHTML = colHeader[i];
		headerCell.style.fontSize = "smaller";
		row.appendChild(headerCell);
	}

	for ( var i = 0; i < machineDetails.length; i++) {
		row = table.insertRow(-1);
		for ( var j = 0; j < columnCount; j++) {
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

dojo.addOnLoad(function() {
  GEOHandler.NORDX = [ '846', '806', '702', '678' ];

  console.log('adding NORDX functions...');
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
  GEOHandler.enableCopyAddress(GEOHandler.NORDX, validateNORDXCopy, [ 'ZD01', 'ZP02' ]);
  GEOHandler.addAddrFunction(updateAddrTypeList, GEOHandler.NORDX);
  GEOHandler.registerValidator(addCrossBorderValidatorNORS, GEOHandler.NORDX, null, true);
  GEOHandler.addAfterConfig(setClientTierValues, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(setClientTierValues, GEOHandler.NORDX);
  // GEOHandler.addAfterTemplateLoad(setSalesRepValues, GEOHandler.NORDX);
  // GEOHandler.addAfterTemplateLoad(setAdminDSCValues, GEOHandler.NORDX);
  // GEOHandler.addAfterConfig(setSalesRepValues, GEOHandler.NORDX);
  // GEOHandler.addAfterConfig(setAdminDSCValues, GEOHandler.NORDX);
  // GEOHandler.addAfterConfig(setSBOForFinlandSubRegion, GEOHandler.NORDX);
  // GEOHandler.addAfterTemplateLoad(setSBOForFinlandSubRegion,
  // GEOHandler.NORDX);
  GEOHandler.addAfterConfig(addHandlersForNORDX, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(afterConfigForNORDX, GEOHandler.NORDX);
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.NORDX);
  GEOHandler.registerValidator(addNORDXAddressTypeValidator, GEOHandler.NORDX, null, true);
  GEOHandler.registerValidator(addNORDXInstallingShipping, GEOHandler.NORDX, null, true);
  GEOHandler.registerValidator(addGenericVATValidator('', 'MAIN_CUST_TAB', 'frmCMR', 'ZS01'), [ SysLoc.DENMARK, SysLoc.FINLAND, SysLoc.SWEDEN ], null, true);
  GEOHandler.registerValidator(norwayCustomVATValidator('', 'MAIN_CUST_TAB', 'frmCMR', 'ZS01'), [ SysLoc.NORWAY ], null, true);
  GEOHandler.addAddrFunction(disableLandCntry, GEOHandler.NORDX);
  GEOHandler.addAddrFunction(loadMachinesList, GEOHandler.NORDX);
  GEOHandler.addAddrFunction(handleMahcineModel, GEOHandler.NORDX);
  GEOHandler.addAddrFunction(addPhoneValidatorNORDX, GEOHandler.NORDX);
  // GEOHandler.registerValidator(machineValidator, GEOHandler.NORDX, null,
  // true);
  /* 1596058: All Nordics - DPL check failed */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.NORDX, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(setSBO, GEOHandler.NORDX);
  GEOHandler.addAfterTemplateLoad(setSBO, GEOHandler.NORDX);


});