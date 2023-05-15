/* Register CEMEA Javascripts */

// Exclusive countries for GBM/SBM 
var CEMEA_EXCL = new Set([ '620', '767', '805', '823', '677', '680', '832' ]);
var CEE_INCL = new Set([ '603', '607', '626', '644', '651', '668', '693', '694', '695', '699', '704', '705', '707', '708', '740', '741', '787',
    '820', '821', '826', '889', '358', '359', '363' ]);
var ME_INCL = new Set(
    [ '620', '642', '675', '677', '680', '752', '762', '767', '768', '772', '805', '808', '823', '832', '849', '850', '865', '729' ]);
var GBM_SBM_INCL = new Set([ '677', '680', '620', '832', '805', '767', '823', '675' ]);
var ME_DUP_INCL = new Set([ '677', '680', '620', '832', '805', '767', '823', '675', '762', '768', '772', '849', '752' ]);
var isicCds = new Set([ '6010', '6411', '6421', '7320', '7511', '7512', '7513', '7514', '7521', '7522', '7523', '7530', '7704', '7706', '7707',
    '7720', '8010', '8021', '8022', '8030', '8090', '8511', '8512', '8519', '8532', '8809', '8813', '8818', '9900' ]);
var landedCntryMapping = {
  "XX" : "000",
  "AD" : "706",
  "AE" : "677",
  "AF" : "614",
  "AG" : "649",
  "AI" : "649",
  "AL" : "603",
  "AM" : "607",
  "AO" : "791",
  "AQ" : "610",
  "AR" : "613",
  "AS" : "897",
  "AT" : "618",
  "AU" : "616",
  "AW" : "649",
  "AX" : "702",
  "AZ" : "358",
  "BA" : "699",
  "BB" : "621",
  "BD" : "615",
  "BE" : "624",
  "BF" : "841",
  "BG" : "644",
  "BH" : "620",
  "BI" : "645",
  "BJ" : "840",
  "BL" : "000",
  "BM" : "627",
  "BN" : "643",
  "BO" : "629",
  "BQ" : "791",
  "BR" : "631",
  "BS" : "619",
  "BT" : "744",
  "BV" : "649",
  "BW" : "636",
  "BY" : "626",
  "BZ" : "649",
  "CA" : "649",
  "CC" : "616",
  "CD" : "662",
  "CF" : "810",
  "CG" : "667",
  "CH" : "848",
  "CI" : "637",
  "CK" : "796",
  "CL" : "655",
  "CM" : "692",
  "CN" : "641",
  "CO" : "661",
  "CR" : "663",
  "CS" : "707",
  "CU" : "897",
  "CV" : "669",
  "CW" : "791",
  "CX" : "616",
  "CY" : "666",
  "CZ" : "668",
  "DE" : "724",
  "DJ" : "670",
  "DK" : "678",
  "DM" : "649",
  "DO" : "681",
  "DZ" : "229",
  "EC" : "683",
  "EE" : "602",
  "EG" : "865",
  "EH" : "706",
  "ER" : "745",
  "ES" : "838",
  "ET" : "698",
  "FI" : "702",
  "FJ" : "796",
  "FK" : "866",
  "FM" : "897",
  "FO" : "678",
  "FR" : "706",
  "GA" : "656",
  "GB" : "866",
  "GD" : "649",
  "GE" : "651",
  "GF" : "706",
  "GG" : "866",
  "GH" : "725",
  "GI" : "866",
  "GL" : "678",
  "GM" : "753",
  "GN" : "706",
  "GP" : "706",
  "GQ" : "383",
  "GR" : "726",
  "GS" : "866",
  "GT" : "731",
  "GU" : "897",
  "GW" : "879",
  "GY" : "640",
  "HK" : "738",
  "HM" : "616",
  "HN" : "735",
  "HR" : "704",
  "HT" : "733",
  "HU" : "740",
  "ID" : "749",
  "IE" : "754",
  "IL" : "755",
  "IM" : "866",
  "IN" : "744",
  "IO" : "866",
  "IQ" : "752",
  "IR" : "750",
  "IS" : "742",
  "IT" : "758",
  "JE" : "000",
  "JM" : "759",
  "JO" : "762",
  "JP" : "760",
  "KE" : "764",
  "KG" : "695",
  "KH" : "852",
  "KI" : "796",
  "KM" : "706",
  "KN" : "649",
  "KP" : "766",
  "KR" : "766",
  "KW" : "767",
  "KY" : "647",
  "KZ" : "694",
  "LA" : "834",
  "LB" : "768",
  "LC" : "839",
  "LI" : "848",
  "LK" : "652",
  "LR" : "770",
  "LS" : "711",
  "LT" : "638",
  "LU" : "624",
  "LV" : "608",
  "LY" : "772",
  "MA" : "642",
  "MC" : "706",
  "MD" : "787",
  "ME" : "707",
  "MF" : "000",
  "MG" : "706",
  "MH" : "897",
  "MK" : "705",
  "ML" : "382",
  "MM" : "646",
  "MN" : "641",
  "MO" : "736",
  "MP" : "897",
  "MQ" : "706",
  "MR" : "717",
  "MS" : "649",
  "MT" : "780",
  "MU" : "706",
  "MV" : "834",
  "MW" : "769",
  "MX" : "781",
  "MY" : "778",
  "MZ" : "782",
  "NA" : "682",
  "NC" : "706",
  "NE" : "880",
  "NF" : "616",
  "NG" : "804",
  "NI" : "799",
  "NL" : "788",
  "NO" : "806",
  "NP" : "744",
  "NR" : "616",
  "NU" : "796",
  "NZ" : "796",
  "OM" : "805",
  "PA" : "811",
  "PE" : "815",
  "PF" : "706",
  "PG" : "616",
  "PH" : "818",
  "PK" : "808",
  "PL" : "820",
  "PM" : "706",
  "PN" : "866",
  "PR" : "897",
  "PS" : "762",
  "PT" : "822",
  "PW" : "897",
  "PY" : "813",
  "QA" : "823",
  "RE" : "706",
  "RO" : "826",
  "RS" : "707",
  "RU" : "821",
  "RW" : "831",
  "SA" : "832",
  "SB" : "616",
  "SC" : "876",
  "SD" : "842",
  "SE" : "846",
  "SG" : "834",
  "SH" : "866",
  "SI" : "708",
  "SJ" : "806",
  "SK" : "693",
  "SL" : "833",
  "SM" : "758",
  "SN" : "635",
  "SO" : "835",
  "SR" : "843",
  "SS" : "842",
  "ST" : "827",
  "SV" : "829",
  "SX" : "791",
  "SY" : "850",
  "SZ" : "853",
  "TC" : "619",
  "TD" : "881",
  "TF" : "706",
  "TG" : "718",
  "TH" : "856",
  "TJ" : "363",
  "TK" : "796",
  "TL" : "749",
  "TM" : "359",
  "TN" : "729",
  "TO" : "796",
  "TR" : "862",
  "TT" : "859",
  "TV" : "796",
  "TW" : "858",
  "TZ" : "851",
  "UA" : "889",
  "UG" : "857",
  "UM" : "897",
  "US" : "897",
  "UY" : "869",
  "UZ" : "741",
  "VA" : "758",
  "VC" : "839",
  "VE" : "871",
  "VG" : "649",
  "VI" : "897",
  "VN" : "852",
  "VU" : "706",
  "WF" : "706",
  "WS" : "796",
  "YE" : "849",
  "YT" : "706",
  "ZA" : "864",
  "ZM" : "883",
  "ZW" : "825"
}
function addCEMEALandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
      postCdMandatoryByLandCntry();
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
/*
 * function importByEnterprise() { var cmrNo =
 * FormManager.getActualValue('enterprise'); if (cmrNo == '') {
 * cmr.showAlert('Please input enterprise number as CMR Number to search for.');
 * return; }
 * 
 * var hasAccepted = dojo.byId('findCMRResult_txt').innerHTML.trim() ==
 * 'Accepted'; cmr.skipAddress = true; if (hasAccepted) { cmr.importcmr = cmrNo;
 * cmr.showConfirm('doImportCmrs()', 'Results from a previous CMR Search have
 * already been accepted for this request. Importing will overwrite existing
 * data records. Continue importing the CMR records?', null, null, { OK : 'Yes',
 * CANCEL : 'Cancel' }); } else { importCMRs(cmrNo); } }
 */

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

/**
 * After config handlers Oman
 */
var _vatExemptHandlerOM = null;
function addHandlersForOman() {
  if (_vatExemptHandlerOM == null) {
    _vatExemptHandlerOM = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorOMAN();
    });
  }
}

function omanVat() {
  if (FormManager.getActualValue('cmrIssuingCntry') != '805') {
    return;
  }

  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (role == 'REQUESTER') {
    if (custSubGrp == 'BUSPR' || custSubGrp == 'COMME' || custSubGrp == 'THDPT' || custSubGrp == 'ELBP' || custSubGrp == 'ELCOM') {
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    } else {
      FormManager.removeValidator('vat', Validators.REQUIRED);
    }
  }

  setVatValidatorOMAN();
}

function setVatValidatorOMAN() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (FormManager.getActualValue('cmrIssuingCntry') != '805') {
    return;
  }
  if (custSubGrp == 'INTER' || custSubGrp == 'PRICU') {
    return;
  }

  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (!dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
    }
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
  // Set 707 landed country base on sub region
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.SERBIA && (cntryRegion == undefined || cntryRegion == '' || cntryRegion == null)) {
    var result = cmr.query('GET_CNTRYUSED', {
      REQ_ID : FormManager.getActualValue('reqId'),
    });
    if (result && result.ret1 && result.ret1 != '') {
      cntryRegion = result.ret1;
    }
  }
  if (cntryRegion == '707ME') {
    landCntry = 'ME';
  } else if (cntryRegion == '707CS') {
    landCntry = 'RS';
  } else if (cntryRegion == '707') {
    landCntry = 'RS';
  } else if (cntryRegion == '808AF') {
    landCntry = 'AF';
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }

//  FormManager.readOnly('capInd');
//  if (FormManager.getActualValue('reqType') == 'C') {
//    FormManager.getField('capInd').set('checked', true);
//  }

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

  if (FormManager.getActualValue('cmrIssuingCntry') == '618') {
    if (custSubType == 'BUSPR' || custSubType == 'XBP') {
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    }
  }

  setAustriaUIFields();
  setExpediteReason();
  setTypeOfCustomerRequiredProcessor();
  // CREATCMR-788
  addressQuotationValidatorCEMEA();
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
  if (custSubType != null && custSubType != ''
      && (custSubType == 'BUSPR' || (custType != null && custType != '' && custType == 'CROSS' && custSubType == 'XBP'))) {
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
  if (custType != null && custType != '' && custType == 'LOCAL' && custSubType != null && custSubType != ''
      && (custSubType == 'INTER' || custSubType == 'INTSO')) {
    FormManager.resetValidations('inacCd');
    FormManager.resetValidations('enterprise');
    FormManager.setValue("vat", "");
    FormManager.readOnly("vat");
    FormManager.setValue("salesBusOffCd", "000");
    FormManager.readOnly("salesBusOffCd");
    FormManager.setValue("inacCd", "");
    FormManager.readOnly("inacCd");
    FormManager.setValue("enterprise", "");
    FormManager.readOnly("enterprise");
  }
  // PPS CEID locked for other than BP scenario
  if (custSubType != null && custSubType != '' && custSubType != 'BUSPR' && custSubType != 'XBP') {
    FormManager.readOnly("ppsceid");
  } else {
    FormManager.enable("ppsceid");
  }
}

function lockIBMtab() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (role == 'REQUESTER') {
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('buyingGroupId');
    FormManager.readOnly('globalBuyingGroupId');
    FormManager.readOnly('covId');
    FormManager.readOnly('geoLocationCode');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('dunsNo');
    if (custSubType != 'XBP' && custSubType != 'BUSPR') {
      FormManager.readOnly('ppsceid');
    } else {
      FormManager.enable('ppsceid');
    }
    // FormManager.readOnly('soeReqNo'); field remove for CMR-6012
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('locationNumber');

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
        } else if (cntry != SysLoc.AUSTRIA
            && cmrNo != ''
            && custSubType != ''
            && (custSubType == 'BUSPR' || custSubType.includes('BP') || custSubType == 'CSBP' || custSubType.includes('MEBP')
                || custSubType == 'RSXBP' || custSubType.includes('RSBP')) && !(cmrNo >= 002000 && cmrNo <= 009999)) {
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
  var reqType = FormManager.getActualValue('reqType')
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
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    if (ME_INCL.has(cntry)) {
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    }
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
  if (reqType == 'U' && FormManager.getActualValue('addrType') == 'ZS01') {
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
var _landCntryHandler = null;
var _vatExempthandler = null;
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
      if (ME_INCL.has(FormManager.getActualValue('cmrIssuingCntry'))) {
        togglePPSCeidME();
      }
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
  
  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorCEMEA();
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
      if (ME_INCL.has(FormManager.getActualValue('cmrIssuingCntry'))) {
        togglePPSCeidME();
      }
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

function setVatValidatorCEMEA() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (FormManager.getActualValue('custSubGrp').includes('IBM')) {
      FormManager.readOnly('vat');
    }
    if (dijit.byId('vatExempt').get('checked')) {
      FormManager.clearValue('vat');
    }
    if (!dijit.byId('vatExempt').get('checked')) {
//      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
      FormManager.enable('vat');
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
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (cntry == '704') {
          setTaxCd1MandatoryCroatia();
        } else if (cntry == '668') {
          setTaxCd1MandatoryCzech();
        } else if (cntry == '693') {
          setICOAndDICMandatory();
        }
      });
    }
  }
}

var _checklistBtnHandler = [];
function addChecklistBtnHandler() {
  for (var i = 2; i <= 15; i++) {
    _checklistBtnHandler[i] = null;
    if (_checklistBtnHandler[i] == null) {
      _checklistBtnHandler[i] = dojo.connect(FormManager.getField('dijit_form_RadioButton_' + i), 'onClick', function(value) {
        freeTxtFieldShowHide(Number(value.target.id.split("_").pop()));
      });
    }
  }
}

function freeTxtFieldShowHide(buttonNo) {
var shouldDisplay = false;
  
  if (buttonNo <= 1) {
    return;
  }
  var fieldIdNo = getCheckListFieldNo(buttonNo);
  var element = document.getElementById('checklist_txt_field_' + fieldIdNo);
  var textFieldElement = document.getElementsByName('freeTxtField' + fieldIdNo)[0];
  
  if (buttonNo%2 == 0) {
    shouldDisplay = true;
  } else {
    shouldDisplay = false;
  }
  if (shouldDisplay) {
    element.style.display = 'block';
  } else {
    element.style.display = 'none';
    textFieldElement.value = '';
  }
}

function getCheckListFieldNo(buttonNo) {
  return ((buttonNo - (buttonNo % 2))/2) + 5;
}

function checkChecklistButtons() {
  for (var i = 2; i<=14; i=i+2) {
    if (document.getElementById('dijit_form_RadioButton_' + i).checked) {
      document.getElementById('checklist_txt_field_' + getCheckListFieldNo(i)).style.display = 'block';
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
// var _SalesRep2Handler = null;
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

  // if (_SalesRep2Handler == null) {
  // _SalesRep2Handler = dojo.connect(FormManager.getField('dupSalesRepNo'),
  // 'onChange', function(value) {
  // setSBO2(value);
  // });
  // }
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
function addPrefixVat() {
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  var vat = FormManager.getActualValue('vat');
  var vatExempt = dijit.byId('vatExempt').get('checked');

  if ('C' == reqType && custSubGrp != null && !custSubGrp.includes('IN') && 'LOCAL' == scenario && vatExempt != true) {
    if ('821' == issu_cntry) {
      if (vat != null && new RegExp("^[A-Za-z]{2}.*").test(vat)) {
        var prefix = vat.substring(0, 2);
        FormManager.setValue('vat', vat.replace(prefix, ''));
      }
    } else if (('644,668,693,704,708,740,820,826').indexOf(issu_cntry) > 0) {
      var prefix = '';
      switch (issu_cntry) {
      case '644':
        prefix = 'BG';
        break;
      case '668':
        prefix = 'CZ';
        break;
      case '693':
        prefix = 'SK';
        break;
      case '704':
        prefix = 'HR';
        break;
      case '708':
        prefix = 'SI';
        break;
      case '740':
        prefix = 'HU';
        break;
      case '820':
        prefix = 'PL';
        break;
      case '826':
        prefix = 'RO';
        break;
      default:
        prefix = '';
        break;
      }
      var addrType = 'ZP01';
      if ('LOCAL' == scenario) {
        addrType = 'ZS01';
      }
      var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
        REQID : FormManager.getActualValue('reqId'),
        TYPE : addrType
      });
      if (ret && ret.ret1 && ret.ret1 != '') {
        prefix = ret.ret1;
      }
      if (vat != null && new RegExp("^[A-Za-z]{2}.*").test(vat)) {
        FormManager.setValue('vat', vat);
      } else {
        FormManager.setValue('vat', prefix + vat);
      }
    }
  }
}

function addAddressTypeValidatorME() {
  console.log("addAddressTypeValidator for ME..........");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var reqLocalAddr = new Set([ '832' ]);

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
          } else if (zs01Cnt == 0 || zp01Cnt == 0 || zi01Cnt == 0 || zd01Cnt == 0 || zs02Cnt == 0) {
            return new ValidationResult(null, false, 'All address types are mandatory except G Address.');
          } else if (zs01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Sold-To address is allowed.');
          } else if (zp01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Bill-To address is allowed.');
          } else if (zs02Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Mail-To address is allowed.');
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
/* CMR-7266 Street or PO Box should be required (ME) */
function addStreetAndPoBoxFormValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
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
    // checkAndAddValidator('postCd', Validators.LATIN, [ 'Postal Code' ]);
    checkAndAddValidator('postCd', LATINForME, [ 'Postal Code' ]);
  } else {
    FormManager.removeValidator('custNm1', Validators.LATIN);
    FormManager.removeValidator('custNm2', Validators.LATIN);
    FormManager.removeValidator('custNm3', Validators.LATIN);
    FormManager.removeValidator('addrTxt', Validators.LATIN);
    FormManager.removeValidator('city1', Validators.LATIN);
    // FormManager.removeValidator('postCd', Validators.LATIN);
    FormManager.removeValidator('postCd', LATINForME);
  }
}
// Add Latin validation to ME, because common latin validation has issue when
// add required validation to the field that already has latin validation
function LATINForME(input) {
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

function addGaddrValidatorForCEE() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqId = FormManager.getActualValue('reqId');
        var result = cmr.query('VALIDATOR.GADDRCNTRY', {
          REQID : reqId
        });
        if (result == null || result.ret1 == "") {
          return new ValidationResult(null, false, 'Country Name value of the G Address is required.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');

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
        if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry
            && scenario != 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should be \'' + defaultLandCntry
              + '\' for Non Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry
            && scenario == 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should not be \'' + defaultLandCntry
              + '\' for Cross-Border customers.');
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
      && (arrayOfTargetTypes.indexOf('ZS01') >= 0 || arrayOfTargetTypes.indexOf('ZP01') >= 0 || arrayOfTargetTypes.indexOf('ZI01') >= 0
          || arrayOfTargetTypes.indexOf('ZD01') >= 0 || arrayOfTargetTypes.indexOf('ZS02') >= 0)) {
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

  // Customer Name (3) and PO BOX should not be input at the sametime.
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm3 = FormManager.getActualValue('custNm3');
        var poBox = FormManager.getActualValue('poBox');

        if (custNm3 != null && custNm3 != '' && custNm3 != undefined && poBox != null && poBox != '' && poBox != undefined) {
          if (custNm3.length > 0 && poBox.length > 0) {
            return new ValidationResult(null, false, 'Customer Name (3) and PO BOX should not be input at the sametime.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setPreferredLang() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  FormManager.readOnly('custPrefLang');
  var custGrp = FormManager.getActualValue('custGrp');
  if ('642' == cntry && ((reqType == 'C' && custGrp != '' && custGrp != undefined && custGrp != null && custGrp != 'CROSS') || reqType == 'U')) {
    FormManager.setValue('custPrefLang', 'F');
  } else if ('832' == cntry) {
    FormManager.setValue('custPrefLang', 'A');
  } else {
    var preLang = FormManager.getActualValue('custPrefLang');
    if (preLang == '' || preLang == null || preLang == undefined) {
      FormManager.setValue('custPrefLang', 'E');
    }
    FormManager.enable('custPrefLang');
  }
}

// function setClientTierValues(isuCd) {
// if (FormManager.getActualValue('viewOnlyPage') == 'true') {
// return;
// }
//
// isuCd = FormManager.getActualValue('isuCd');
// var cntry = FormManager.getActualValue('cmrIssuingCntry');
// var clientTiers = [];
// if (isuCd != '') {
// if (SysLoc.SLOVAKIA == cntry
// && (FormManager.getActualValue('custSubGrp') == 'XTP' ||
// FormManager.getActualValue('custSubGrp') == 'THDPT' ||
// FormManager.getActualValue('custSubGrp') == 'COMME'
// || FormManager.getActualValue('custSubGrp') == 'XCOM' ||
// FormManager.getActualValue('custSubGrp') == 'PRICU' ||
// FormManager.getActualValue('custSubGrp') == 'XPC')) {
// if (isuCd == '34') {
// clientTiers = [ '6', 'V' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'N', 'S' ];
// } else if (isuCd == '5B') {
// clientTiers = [ '7' ];
// }
// } else if ((SysLoc.TURKMENISTAN == cntry || SysLoc.TAJIKISTAN == cntry ||
// SysLoc.ALBANIA == cntry || SysLoc.ARMENIA == cntry || SysLoc.BELARUS == cntry
// || SysLoc.BULGARIA == cntry
// || SysLoc.GEORGIA == cntry || SysLoc.KAZAKHSTAN == cntry || SysLoc.KYRGYZSTAN
// == cntry || SysLoc.MACEDONIA == cntry || SysLoc.SERBIA == cntry ||
// SysLoc.UZBEKISTAN == cntry || SysLoc.UKRAINE == cntry)
// && (FormManager.getActualValue('custSubGrp') == 'XTP' ||
// FormManager.getActualValue('custSubGrp') == 'XCE' ||
// FormManager.getActualValue('custSubGrp') == 'THDPT'
// || FormManager.getActualValue('custSubGrp') == 'COMME' ||
// FormManager.getActualValue('custSubGrp') == 'XCOM' ||
// FormManager.getActualValue('custSubGrp') == 'PRICU'
// || FormManager.getActualValue('custSubGrp') == 'XPC' ||
// FormManager.getActualValue('custSubGrp') == 'CSCOM' ||
// FormManager.getActualValue('custSubGrp') == 'CSPC'
// || FormManager.getActualValue('custSubGrp') == 'CSTP' ||
// FormManager.getActualValue('custSubGrp') == 'MECOM' ||
// FormManager.getActualValue('custSubGrp') == 'MEPC'
// || FormManager.getActualValue('custSubGrp') == 'METP' ||
// FormManager.getActualValue('custSubGrp') == 'RSXCO' ||
// FormManager.getActualValue('custSubGrp') == 'RSXPC'
// || FormManager.getActualValue('custSubGrp') == 'RSXTP' ||
// FormManager.getActualValue('custSubGrp') == 'RSCOM' ||
// FormManager.getActualValue('custSubGrp') == 'RSPC' || FormManager
// .getActualValue('custSubGrp') == 'RSTP')) {
// if (isuCd == '34') {
// clientTiers = [ 'V' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'S' ];
// }
// } else if ((SysLoc.AZERBAIJAN == cntry)
// && (FormManager.getActualValue('custSubGrp') == 'XTP' ||
// FormManager.getActualValue('custSubGrp') == 'XCE' ||
// FormManager.getActualValue('custSubGrp') == 'THDPT'
// || FormManager.getActualValue('custSubGrp') == 'COMME' ||
// FormManager.getActualValue('custSubGrp') == 'XCOM' ||
// FormManager.getActualValue('custSubGrp') == 'PRICU'
// || FormManager.getActualValue('custSubGrp') == 'XPC' ||
// FormManager.getActualValue('custSubGrp') == 'CSCOM' ||
// FormManager.getActualValue('custSubGrp') == 'CSPC'
// || FormManager.getActualValue('custSubGrp') == 'CSTP' ||
// FormManager.getActualValue('custSubGrp') == 'MECOM' ||
// FormManager.getActualValue('custSubGrp') == 'MEPC'
// || FormManager.getActualValue('custSubGrp') == 'METP' ||
// FormManager.getActualValue('custSubGrp') == 'RSXCO' ||
// FormManager.getActualValue('custSubGrp') == 'RSXPC'
// || FormManager.getActualValue('custSubGrp') == 'RSXTP' ||
// FormManager.getActualValue('custSubGrp') == 'RSCOM' ||
// FormManager.getActualValue('custSubGrp') == 'RSPC' || FormManager
// .getActualValue('custSubGrp') == 'RSTP')) {
// if (isuCd == '34') {
// clientTiers = [ 'V' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'S' ];
// }
// } else if ((SysLoc.CZECH_REPUBLIC == cntry)
// && (FormManager.getActualValue('custSubGrp') == 'XTP' ||
// FormManager.getActualValue('custSubGrp') == 'THDPT' ||
// FormManager.getActualValue('custSubGrp') == 'COMME'
// || FormManager.getActualValue('custSubGrp') == 'XCOM' ||
// FormManager.getActualValue('custSubGrp') == 'PRICU' ||
// FormManager.getActualValue('custSubGrp') == 'XPC')) {
// if (isuCd == '34') {
// clientTiers = [ '6', 'V' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'S', 'N' ];
// } else if (isuCd == '5B') {
// clientTiers = [ '7' ];
// }
// } else if ((SysLoc.BOSNIA_HERZEGOVINA == cntry || SysLoc.SLOVENIA == cntry)
// && (FormManager.getActualValue('custSubGrp') == 'XTP' ||
// FormManager.getActualValue('custSubGrp') == 'THDPT' ||
// FormManager.getActualValue('custSubGrp') == 'COMME'
// || FormManager.getActualValue('custSubGrp') == 'XCOM' ||
// FormManager.getActualValue('custSubGrp') == 'PRICU' ||
// FormManager.getActualValue('custSubGrp') == 'XPC')) {
// if (isuCd == '34') {
// clientTiers = [ 'V' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'S', 'M' ];
// }
// } else if ((SysLoc.CROATIA == cntry)
// && (FormManager.getActualValue('custSubGrp') == 'XTP' ||
// FormManager.getActualValue('custSubGrp') == 'THDPT' ||
// FormManager.getActualValue('custSubGrp') == 'COMME'
// || FormManager.getActualValue('custSubGrp') == 'XCOM' ||
// FormManager.getActualValue('custSubGrp') == 'PRICU' ||
// FormManager.getActualValue('custSubGrp') == 'XPC')) {
// if (isuCd == '34') {
// clientTiers = [ '6', 'V' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'S' ];
// }
// } else if ((SysLoc.HUNGARY == cntry)
// && (FormManager.getActualValue('custSubGrp') == 'XTP' ||
// FormManager.getActualValue('custSubGrp') == 'THDPT' ||
// FormManager.getActualValue('custSubGrp') == 'COMME'
// || FormManager.getActualValue('custSubGrp') == 'XCOM' ||
// FormManager.getActualValue('custSubGrp') == 'PRICU' ||
// FormManager.getActualValue('custSubGrp') == 'XPC')) {
// if (isuCd == '34') {
// clientTiers = [ '6', 'V', 'A' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'S' ];
// } else if (isuCd == '5B') {
// clientTiers = [ '7' ];
// }
// } else if ((SysLoc.MOLDOVA == cntry || SysLoc.ROMANIA == cntry)
// && (FormManager.getActualValue('custSubGrp') == 'XTP' ||
// FormManager.getActualValue('custSubGrp') == 'THDPT' ||
// FormManager.getActualValue('custSubGrp') == 'COMME'
// || FormManager.getActualValue('custSubGrp') == 'XCOM' ||
// FormManager.getActualValue('custSubGrp') == 'PRICU' ||
// FormManager.getActualValue('custSubGrp') == 'XPC')) {
// if (isuCd == '34') {
// clientTiers = [ 'V' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'S', 'M' ];
// } else if (isuCd == '5B') {
// clientTiers = [ '7' ];
// }
//
// } else if ((SysLoc.POLAND == cntry || SysLoc.RUSSIA == cntry)
// && (FormManager.getActualValue('custSubGrp') == 'XTP' ||
// FormManager.getActualValue('custSubGrp') == 'THDPT' ||
// FormManager.getActualValue('custSubGrp') == 'COMME'
// || FormManager.getActualValue('custSubGrp') == 'XCOM' ||
// FormManager.getActualValue('custSubGrp') == 'PRICU' ||
// FormManager.getActualValue('custSubGrp') == 'XPC')) {
// if (isuCd == '34') {
// clientTiers = [ '6', 'V', 'A' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'S', 'N' ];
// } else if (isuCd == '5B') {
// clientTiers = [ '7' ];
// }
// } else if ((SysLoc.TUNISIA == cntry)// CMR-6057
// && (FormManager.getActualValue('custSubGrp') == 'XTP' ||
// FormManager.getActualValue('custSubGrp') == 'THDPT' ||
// FormManager.getActualValue('custSubGrp') == 'COMME'
// || FormManager.getActualValue('custSubGrp') == 'XCOM' ||
// FormManager.getActualValue('custSubGrp') == 'PRICU')) {
// if (isuCd == '34') {
// clientTiers = [ 'V' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'S', 'N', 'T', 'C' ];
// }
// } else if ('808' == cntry && FormManager.getActualValue('custSubGrp') != '')
// {// 808(Afganistan/Pakistan)
// if (FormManager.getActualValue('custSubGrp') == 'AFTP' ||
// FormManager.getActualValue('custSubGrp') == 'AFPC' ||
// FormManager.getActualValue('custSubGrp') == 'AFCOM') {
// if (isuCd == '34') {
// clientTiers = [ 'V', 'A' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'C', 'T', 'N' ];
// }
// } else if (FormManager.getActualValue('custSubGrp') == 'PKTP' ||
// FormManager.getActualValue('custSubGrp') == 'PKXTP' ||
// FormManager.getActualValue('custSubGrp') == 'PKCOM'
// || FormManager.getActualValue('custSubGrp') == 'PKXCO' ||
// FormManager.getActualValue('custSubGrp') == 'PKPC') {
//
// if (isuCd == '34') {
// clientTiers = [ 'V' ];
// } else if (isuCd == '32') {
// clientTiers = [ 'S', 'T', 'N' ];
// }
// } else {
// clientTiers = [ '7' ];
// }
// // End of CMR6057
// } else {
// var qParams = {
// _qall : 'Y',
// ISSUING_CNTRY : cntry,
// ISU : '%' + isuCd + '%'
// };
// var results = cmr.query('GET.CTCLIST.BYISU', qParams);
// if (results != null) {
// for (var i = 0; i < results.length; i++) {
// clientTiers.push(results[i].ret1);
// }
// }
// }
// if (clientTiers != null) {
// FormManager.limitDropdownValues(FormManager.getField('clientTier'),
// clientTiers);
// if (clientTiers.length == 1) {
// FormManager.setValue('clientTier', clientTiers[0]);
// }
// }
// }
// }

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
  // FilteringDropdown.loadItems('dupSalesRepNo', 'dupSalesRepNo_spinner',
  // 'lov', 'fieldId=SalRepNameNo&cmrIssuingCntry=' + dupIssuingCntryCd);
  FilteringDropdown.loadItems('dupEnterpriseNo', 'dupEnterpriseNo_spinner', 'lov', 'fieldId=Enterprise&cmrIssuingCntry=' + dupIssuingCntryCd);
}

function setVatRequired(value) {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    if (!value && !dijit.byId('vatExempt')) {
      window.setTimeout('setVatRequired()', 500);
    } else {
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      FormManager.resetValidations('vat');
      if (cntry == '642') {
        FormManager.resetValidations('phone3');
      }

      if (!dijit.byId('vatExempt').get('checked')) {
        cemeaCustomVATMandatory();
        if (cntry == '642') {
          initICEField();
        }
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
      FormManager.show('LocalTax3', 'taxCd3');
      // Mark as hide for CMR-4606
      FormManager.hide('SalRepNameNo2', 'dupSalesRepNo');
      // if (role == GEOHandler.ROLE_PROCESSOR) {
      // FormManager.show('SalesBusOff2', 'dupSalesBoCd');
      // } else {
      // FormManager.hide('SalesBusOff2', 'dupSalesBoCd');
      // }

      setDropdownField2Values();
      if (viewOnlyPage != 'true') {
        FormManager.enable('dupIssuingCntryCd');
        checkAndAddValidator('dupIssuingCntryCd', Validators.REQUIRED, [ 'Country of Duplicate CMR' ]);
        setCISFieldHandlers();

        if (FormManager.getActualValue('reqType') == 'C') {
          // CMR-4606 show alert message on tab
          checkAndAddValidator('dupIsuCd', Validators.REQUIRED, [ 'ISU Code 2' ], 'MAIN_IBM_TAB');
          checkAndAddValidator('dupClientTierCd', Validators.REQUIRED, [ 'Client Tier 2' ], 'MAIN_IBM_TAB');
          // checkAndAddValidator('dupSalesRepNo', Validators.REQUIRED, [ 'Sales
          // Rep 2' ]);
        }
      } else {
        FormManager.readOnly('dupIsuCd');
        FormManager.readOnly('dupClientTierCd');
        FormManager.readOnly('dupEnterpriseNo');
        // FormManager.readOnly('dupSalesRepNo');
        FormManager.readOnly('taxCd3');
      }

    } else {
      FormManager.clearValue('dupIssuingCntryCd');
      FormManager.disable('dupIssuingCntryCd');

      FormManager.resetValidations('dupIsuCd');
      FormManager.resetValidations('dupClientTierCd');
      // FormManager.resetValidations('dupSalesRepNo');
      FormManager.hide('ISU2', 'dupIsuCd');
      FormManager.hide('ClientTier2', 'dupClientTierCd');
      FormManager.hide('Enterprise2', 'dupEnterpriseNo');
      FormManager.hide('LocalTax3', 'taxCd3');
      FormManager.hide('SalRepNameNo2', 'dupSalesRepNo');
      // FormManager.hide('SalesBusOff2', 'dupSalesBoCd');
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

  if (ME_INCL.has(cntry) && FormManager.getActualValue('reqType') == 'C') {
    if (FormManager.getField('templatevalue-repTeamMemberNo') != undefined) {
      FormManager.getField('templatevalue-repTeamMemberNo').style.display = 'none';
    }
    // FormManager.setValue('salesBusOffCd', '0000000');
//    FormManager.setValue('repTeamMemberNo', '099998');
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

/**
 * ME - sets SBO based on Issuing cntry value(CMR-5993)
 */
function setMESBO(repTeamMemberNo) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var role = FormManager.getActualValue('userRole');
  var custGrp = FormManager.getActualValue('custGrp');
  var reqType = FormManager.getActualValue('reqType');
  var reqSBO = FormManager.getActualValue('salesBusOffCd');
  if (ME_INCL.has(cntry) && (reqType == 'C')) {
    if (GBM_SBM_INCL.has(cntry) && custGrp != null && (custGrp == 'GBM' || custGrp == 'SBM')) {
      FormManager.setValue('salesBusOffCd', '530');
      FormManager.setValue('repTeamMemberNo', '530530');
    } else {
      FormManager.setValue('salesBusOffCd', cntry);
      FormManager.setValue('repTeamMemberNo', cntry + cntry);
    }
    if (role != GEOHandler.ROLE_PROCESSOR) {
      disableSBO();
    }
    return;
  }

  if (ME_INCL.has(cntry) && (reqType == 'U')) {

    if (reqSBO == null || reqSBO == '') {
      FormManager.setValue('salesBusOffCd', cntry);
    }

    if (role != GEOHandler.ROLE_PROCESSOR) {
      disableSBO();
    }
    return;
  }
}

// CMR-5993 SBO is required for processor
function validateMESBO() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('userRole') == GEOHandler.ROLE_PROCESSOR) {
          var cntry = FormManager.getActualValue('cmrIssuingCntry');
          var sbo = FormManager.getActualValue('salesBusOffCd');
          var custGrp = FormManager.getActualValue('custGrp');
          var reqType = FormManager.getActualValue('reqType');

          if (sbo == null || sbo == '') {
            return new ValidationResult(null, false, 'Selling Branch Office can not be empty');
          }

          if (sbo != null && cntry != sbo.substring(0, 3) && (reqType == 'C')) {
            if (!(custGrp == 'GBM' || custGrp == 'SBM')) {
              return new ValidationResult(null, false, 'For ME country,Selling Branch Office should be its cntry');
            } else if (sbo.substring(0, 3) != '530') {
              return new ValidationResult(null, false, 'For GBM/SBM type, Selling Branch Office should be 530');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
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

// CMR-6019 add cmr exist check for duplicate issued country
function dupCMRExistCheck() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var role = FormManager.getActualValue('userRole').toUpperCase();

        if (!(FormManager.getActualValue('reqType') == 'U' && (role == 'REQUESTER' || role == 'PROCESSOR'))
            && !(FormManager.getActualValue('reqType') == 'C' && role == 'PROCESSOR')) {
          return new ValidationResult(null, true);
        }

        if (!ME_DUP_INCL.has(cntry) || !dijit.byId('dupCmrIndc')) {
          return new ValidationResult(null, true);
        }

        if (ME_DUP_INCL.has(cntry) && dijit.byId('dupCmrIndc').get('checked')) {

          var dupCEBO = cntry + '0000';
          var resultsD = cmr.query('CHECK.ME.DUP.EXIST.DB2', {
            CMRNO : cmrNo
          });
          if (resultsD.ret1 != null) {
            var existDupCEBO = resultsD.ret1;
            if (resultsD.ret1 == dupCEBO && FormManager.getActualValue('reqType') == 'U') {
              return new ValidationResult(null, true);
            } else {
              return new ValidationResult(null, false, 'There have exist dup 675 data under this CMR for country code:'
                  + existDupCEBO.substring(0, 3) + ',please use another CMRNO or uncheck the duplicate box.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}// End of dupCMRExistCheck() CMR-6019

/**
 * CEEME - show CoF field for Update req and LOB=IGF and reason=COPT
 */
function setCommercialFinanced() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    return;
  }
  processCoF();
  dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
    processCoF();
  });
  dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
    processCoF();
  });
}

function processCoF() {
  var reqType = FormManager.getActualValue('reqType');
  var lob = FormManager.getActualValue('requestingLob');
  var reason = FormManager.getActualValue('reqReason');
  if (reqType == 'U') {
    FormManager.show('CommercialFinanced', 'commercialFinanced');
    if (lob == 'IGF' && reason == 'COPT') {
      FormManager.enable('commercialFinanced');
    } else {
      FormManager.readOnly('commercialFinanced');
    }
  } else {
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
    FormManager.addValidator('bpSalesRepNo', Validators.DIGIT, [ 'Tele-coverage rep.' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('bpSalesRepNo', validTeleCoverageRep6Length, [ 'Tele-coverage rep.' ], 'MAIN_CUST_TAB');
    return;
  }
  var custGrp = FormManager.getActualValue('custGrp');
  if (custGrp != null && (custGrp == 'GBM' || custGrp == 'SBM')) {
    if (FormManager.getActualValue('cmrIssuingCntry') != '832') {
      FormManager.addValidator('bpSalesRepNo', Validators.REQUIRED, [ 'Tele-coverage rep.' ], 'MAIN_CUST_TAB');
    }
    FormManager.addValidator('bpSalesRepNo', Validators.DIGIT, [ 'Tele-coverage rep.' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('bpSalesRepNo', validTeleCoverageRep6Length, [ 'Tele-coverage rep.' ], 'MAIN_CUST_TAB');
    FormManager.show('TeleCoverageRep', 'bpSalesRepNo');
  } else {
    FormManager.resetValidations('bpSalesRepNo');
    FormManager.hide('TeleCoverageRep', 'bpSalesRepNo');
  }
}
function validTeleCoverageRep6Length(input) {
  var bpSalesRepNo = FormManager.getActualValue('bpSalesRepNo');
  if (bpSalesRepNo != '' && bpSalesRepNo != null && bpSalesRepNo != undefined) {
    if (bpSalesRepNo.length != 6) {
      return new ValidationResult(input, false, 'Tele-coverage rep. must be 6 length');
    }
  }
  return new ValidationResult(input, true);
}
/**
 * Validate Special Character for Abbreviated Name/Location
 */
function validateAbbrevNmLocn() {
  FormManager.addValidator('abbrevNm', Validators.LATIN, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.LATIN, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
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
    cmr.showConfirm('showAddressVerificationModal()', 'You are updating record with duplicate, if you wish to continue click Yes, otherwise No.', null, 'cancelCIS()',
        {
          OK : 'Yes',
          CANCEL : 'No'
        });
  } else {
    showAddressVerificationModal();
  }
}

function proceedCIS() {
  cmr.showModal('addressVerificationModal');
}

function cancelCIS() {
  FormManager.setValue('cisServiceCustIndc', false);
  setCountryDuplicateFields();
  showAddressVerificationModal();
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
        var custGrp = FormManager.getActualValue('custGrp');
        if (CEMEA_EXCL.has(cntry) && (custGrp == 'GBM' || custGrp == 'SBM')) {
          if (abbrevLocn && abbrevLocn.length > 8) {
            abbrevLocn = abbrevLocn.substring(0, 8);
          }
          abbrevLocn = abbrevLocn + ' EXC';
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
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (FormManager.getActualValue('reqType') == 'C') {
    if (abbrvNm && abbrvNm.length > 22) {
      abbrvNm = abbrvNm.substring(0, 22);
    }
    if (abbrevLocn && abbrevLocn.length > 12) {
      abbrevLocn = abbrevLocn.substring(0, 12);
    }
  }

  if (abbrevLocn != null) {
    var custGrp = FormManager.getActualValue('custGrp');
    if (CEMEA_EXCL.has(cntry) && (custGrp == 'GBM' || custGrp == 'SBM')) {
      if (abbrevLocn && abbrevLocn.length > 8) {
        abbrevLocn = abbrevLocn.substring(0, 8);
      }
      abbrevLocn = abbrevLocn + ' EXC';
    }
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
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (!CEE_INCL.has(cntry)) {
    if (FormManager.getActualValue('viewOnlyPage') == 'true' || custSubGrp == 'IBMEM' || custSubGrp == 'PRICU' || custSubGrp == 'BUSPR'
        || custSubGrp == 'XBP' || custSubGrp == 'INTER' || custSubGrp == 'INTSO') {
      return;
    } else {
      if (FormManager.getActualValue('viewOnlyPage') == 'true') {
        return;
      }
    }
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  if (role != 'REQUESTER') {
    FormManager.enable('enterprise');
  }
  clientTier = FormManager.getActualValue('clientTier');

  var enterprises = [];
  if (isuCd != '' && clientTier != '') {
    if (SysLoc.SERBIA == cntry) {
      enterprises = [ '' ];
    } else if (SysLoc.SLOVAKIA == cntry
        && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT'
            || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM'
            || FormManager.getActualValue('custSubGrp') == 'XPC' || FormManager.getActualValue('custSubGrp') == 'PRICU')) {
      if (isuCd == '34' && clientTier == '6') {
        enterprises = [ '985069', '985070' ];
      } else if (isuCd == '34' && clientTier == 'V') {
        enterprises = [ '985013' ];
      }
    } else if ((SysLoc.AZERBAIJAN == cntry)
        && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT'
            || FormManager.getActualValue('custSubGrp') == 'XPC' || FormManager.getActualValue('custSubGrp') == 'PRICU'
            || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '34' && clientTier == 'V') {
        enterprises = [ '' ];
      }
    } else if (SysLoc.CZECH_REPUBLIC == cntry
        && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT'
            || FormManager.getActualValue('custSubGrp') == 'XPC' || FormManager.getActualValue('custSubGrp') == 'PRICU'
            || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '34' && clientTier == '6') {
        enterprises = [ '985069', '985070' ];
      } else if (isuCd == '34' && clientTier == 'V') {
        enterprises = [ '985013', '985014', '985015', '985055' ];
      }
    } else if ((SysLoc.BOSNIA_HERZEGOVINA == cntry || SysLoc.SLOVENIA == cntry || SysLoc.MOLDOVA == cntry || SysLoc.ROMANIA == cntry)
        && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT'
            || FormManager.getActualValue('custSubGrp') == 'XPC' || FormManager.getActualValue('custSubGrp') == 'PRICU'
            || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '32' && clientTier == 'M') {
        enterprises = [ '985069', '985070' ];
      }
    } else if ((SysLoc.CROATIA == cntry)
        && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT'
            || FormManager.getActualValue('custSubGrp') == 'XPC' || FormManager.getActualValue('custSubGrp') == 'PRICU'
            || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '34' && clientTier == '6') {
        enterprises = [ '985069', '985070' ];
      }
    } else if (SysLoc.HUNGARY == cntry
        && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT'
            || FormManager.getActualValue('custSubGrp') == 'XPC' || FormManager.getActualValue('custSubGrp') == 'PRICU'
            || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '34' && clientTier == '6') {
        enterprises = [ '985069', '985070' ];
      } else if (isuCd == '34' && clientTier == 'V') {
        enterprises = [ '985014', '985055' ];
      }
    } else if ((SysLoc.UZBEKISTAN == cntry || SysLoc.TURKMENISTAN == cntry)
        && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT'
            || FormManager.getActualValue('custSubGrp') == 'XPC' || FormManager.getActualValue('custSubGrp') == 'PRICU'
            || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '32' && clientTier == 'S') {
        enterprises = [ '985014' ];
      }
    } else if (SysLoc.ALBANIA == cntry) {
      enterprises = [ '' ];
    } else if (SysLoc.POLAND == cntry
        && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT'
            || FormManager.getActualValue('custSubGrp') == 'XPC' || FormManager.getActualValue('custSubGrp') == 'PRICU'
            || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '32' && clientTier == 'S') {
        enterprises = [ '985050', '985065', '985066', '985062', '985063', '985064', '985068' ];
      } else if (isuCd == '32' && clientTier == 'N') {
        enterprises = [ '985003', '985004', '985067' ];
      } else if (isuCd == '34' && clientTier == '6') {
        enterprises = [ '985069', '985070' ];
      } else if (isuCd == '34' && clientTier == 'V') {
        enterprises = [ '985011', '985012', '985013', '985014', '985016', '985055' ];
      }
    } else if (SysLoc.RUSSIA == cntry
        && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT'
            || FormManager.getActualValue('custSubGrp') == 'XPC' || FormManager.getActualValue('custSubGrp') == 'PRICU'
            || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '32' && clientTier == 'S') {
        enterprises = [ '985026', '985031', '985042', '985051', '985052', '985053', '985054' ];
      } else if (isuCd == '32' && clientTier == 'N') {
        enterprises = [ '985083', '985084', '985067' ];
      } else if (isuCd == '34' && clientTier == '6') {
        enterprises = [ '985069', '985070' ];
      } else if (isuCd == '34' && clientTier == 'A') {
        enterprises = [ '985081' ];
      } else if (isuCd == '34' && clientTier == 'V') {
        enterprises = [ '985012', '985013', '985014', '985016', '985017', '985018', '985021', '985040', '985041', '985055', '985082' ];
      }
    } else if (FormManager.getActualValue('custSubGrp') == 'XINT' || FormManager.getActualValue('custSubGrp') == 'INTER'
        || FormManager.getActualValue('custSubGrp') == 'CSINT' || FormManager.getActualValue('custSubGrp') == 'MEINT'
        || FormManager.getActualValue('custSubGrp') == 'RSXIN' || FormManager.getActualValue('custSubGrp') == 'RSINT'
        || FormManager.getActualValue('custSubGrp') == 'XBP' || FormManager.getActualValue('custSubGrp') == 'BUSPR'
        || FormManager.getActualValue('custSubGrp') == 'BP' || FormManager.getActualValue('custSubGrp') == 'CSBP'
        || FormManager.getActualValue('custSubGrp') == 'MEBP' || FormManager.getActualValue('custSubGrp') == 'RSXBP'
        || FormManager.getActualValue('custSubGrp') == 'RSBP') {
      enterprises = [ '' ];
    } else {
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
      }
    }

    if (enterprises != null) {
      var field = FormManager.getField('enterprise');
      FormManager.limitDropdownValues(field, enterprises);
      if (enterprises.length == 1) {
        FormManager.setValue('enterprise', enterprises[0]);
      }
    }
  }
}

function addCmrNoValidatorForME() {
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
          } else if (cmrNo != '' && custSubType != '' && (custSubType.includes('IN') && (!cmrNo.startsWith('99')))) {
            return new ValidationResult(null, false, 'CMR Number should be in 99XXXX format for internal scenarios');
          } else if (cmrNo != '' && custSubType != '' && !custSubType.includes('IN') && cmrNo.startsWith('99')) {
            return new ValidationResult(null, false, 'Non Internal CMR Number should not be in 99XXXX for scenarios');
          } else if (cmrNo != '' && custSubType != '' && !(custSubType.includes('BP') || custSubType.includes('BUS')) && cmrNo.startsWith('00')) {
            return new ValidationResult(null, false, 'Non BP CMR Number should not be in 00XXXX for scenarios');
          } else if (cmrNo != '' && custSubType != '' && (custSubType.includes('BP') || custSubType.includes('BUS')) && !cmrNo.startsWith('00')) {
            return new ValidationResult(null, false, 'CMR Number should start with 00xxxx for BP scenarios');
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

function cmrNoEnableForME() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var cmrNo = FormManager.getActualValue('cmrNo');

  if (role != "PROCESSOR" || FormManager.getActualValue('viewOnlyPage') == 'true' || reqType == 'U') {
    FormManager.readOnly('cmrNo');
  } else {
    FormManager.enable('cmrNo');
  }
}

function setEnterprise2Values(dupClientTierCd) {
  // CMR-4606
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

// Remove ICE to customer tab
// var _addrTypesForMA = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'ZP02' ];
// var addrTypeHandler = [];
//
// function displayIceForMA() {
// var role = FormManager.getActualValue('userRole').toUpperCase();
// var scenario = FormManager.getActualValue('custGrp');
//
// if (FormManager.getActualValue('reqType') != 'C') {
// return;
// }
// if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress' &&
// FormManager.getActualValue('cmrIssuingCntry') == '642') {
// cmr.hideNode('ice');
// for (var i = 0; i < _addrTypesForMA.length; i++) {
// if (addrTypeHandler[i] == null) {
// addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' +
// _addrTypesForMA[i]), 'onClick', function(value) {
// if (FormManager.getField('addrType_ZP01').checked) {
// if (cmr.currentRequestType == 'C' && scenario == 'LOCAL') {
// cmr.showNode('ice');
// if (role == 'REQUESTER') {
// FormManager.addValidator('dept', Validators.REQUIRED, [ 'ICE#' ], '');
// } else {
// FormManager.resetValidations('dept');
// }
// } else if (cmr.currentRequestType == 'U') {
// cmr.showNode('ice');
// FormManager.resetValidations('dept');
// } else {
// cmr.hideNode('ice');
// FormManager.resetValidations('dept');
// }
// } else {
// cmr.hideNode('ice');
// FormManager.resetValidations('dept');
// }
// });
// } else {
// if (FormManager.getField('addrType_ZP01').checked && ((cmr.currentRequestType
// == 'C' && scenario == 'LOCAL') || cmr.currentRequestType == 'U'))
// cmr.showNode('ice');
// }
// }
// }
// if (cmr.addressMode == 'updateAddress') {
// if (FormManager.getActualValue('addrType') == 'ZP01') {
// cmr.showNode('ice');
// if (role == 'REQUESTER' && cmr.currentRequestType == 'C' && scenario ==
// 'LOCAL') {
// FormManager.addValidator('dept', Validators.REQUIRED, [ 'ICE#' ], '');
// } else {
// FormManager.resetValidations('dept');
// }
// } else {
// cmr.hideNode('ice');
// FormManager.resetValidations('dept');
// }
// }
// }

// Story 1733554 Moroco for ICE field Formator
// function addIceFormatValidationMorocco() {
// FormManager.addFormValidator((function() {
// return {
// validate : function() {
// var dept = FormManager.getActualValue('dept');
// var reqType = FormManager.getActualValue('reqType');
// // var lbl1 = FormManager.getLabel('LocalTax1');
// if (reqType == 'C') {
// if (FormManager.getField('addrType_ZP01').checked && dept && dept.length > 0
// && !dept.match("([0-9]{15})|^(X{3})$|^(x{3})$")) {
// return new ValidationResult({
// id : 'dept',
// type : 'text',
// name : 'dept'
// }, false, 'Invalid format of ICE#. Format should be NNNNNNNNNNNNNNN or "XXX"
// or "xxx"');
// }
// }
// if (reqType == 'U') {
// if (FormManager.getField('addrType_ZP01').checked && dept && dept.length > 0
// && !dept.match("([0-9]{15})|^(X{3})$|^(x{3})$|^(@{1})$")) {
// return new ValidationResult({
// id : 'dept',
// type : 'text',
// name : 'dept'
// }, false, 'Invalid format of ICE#. Format should be NNNNNNNNNNNNNNN or "XXX"
// or "xxx" or "@"');
// }
//
// }
// return new ValidationResult(null, true);
// }
// };
// })(), null, 'frmCMR_addressModal');
// }

function initICEField() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var scenario = FormManager.getActualValue('custGrp');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  FormManager.show('Phone3', 'phone3');
  if (reqType == 'C' && role == 'REQUESTER' && scenario == 'LOCAL' && (custSubType != 'INTER' && custSubType != 'PRICU')) {
    FormManager.addValidator('phone3', Validators.REQUIRED, [ 'ICE#' ], 'MAIN_CUST_TAB');
  } else {
    FormManager.resetValidations('phone3');
  }
}

function addIceFormatValidationMorocco() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var ice = FormManager.getActualValue('phone3');
        var reqType = FormManager.getActualValue('reqType');
        if (reqType == 'C') {
          // if (ice && ice.length > 0 &&
          // !ice.match("([0-9]{15})|^(X{3})$|^(x{3})$")) {
          if (ice && ice.length > 0 && !ice.match("([0-9]{15})")) {
            return new ValidationResult({
              id : 'phone3',
              type : 'text',
              name : 'phone3'
            }, false, 'Invalid format of ICE#. Format should be NNNNNNNNNNNNNNN');
          }
        }
        if (reqType == 'U') {
          // if (ice && ice.length > 0 &&
          // !ice.match("([0-9]{15})|^(X{3})$|^(x{3})$|^(@{1})$")) {
          if (ice && ice.length > 0 && !ice.match("([0-9]{15})")) {
            return new ValidationResult({
              id : 'phone3',
              type : 'text',
              name : 'phone3'
            }, false, 'Invalid format of ICE#. Format should be NNNNNNNNNNNNNNN');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

// function addIceBillingValidator() {
// FormManager.addFormValidator((function() {
// return {
// validate : function() {
// var billingBool = true;
// var reqType = FormManager.getActualValue('reqType');
// var reqId = FormManager.getActualValue('reqId');
// var addr = 'ZP01';
// var qParams = {
// _qall : 'Y',
// REQID : reqId,
// ADDR_TYPE : addr
// };
//
// var results = cmr.query('GET_ICE_ADDRSEQ', qParams);
// if (results != null) {
// for (var i = 0; i < results.length; i++) {
// if (results[i].ret1.length < 1) {
// billingBool = false;
// }
// }
// }
//
// if (billingBool) {
// return new ValidationResult(null, true);
// } else if (!billingBool && reqType == 'C') {
// return new ValidationResult(null, false, 'ICE should not be empty in Billing
// Address.');
// }
// return new ValidationResult(null, true);
// }
// };
// })(), 'MAIN_NAME_TAB', 'frmCMR');
// }

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
        var textBoxes = checklist.query('input[type="text"]');
        if (questions.length > 0 && textBoxes.length > 0) {
          var noOfQuestions = questions.length / 2;
          var noOfTextBoxes = textBoxes.length;
          var checkCount = 0;
          for (var i = 0; i < questions.length; i++) {
            if (questions[i].checked) {
              checkCount++;
            }
          }
          for (var i=0; i < noOfTextBoxes; i++) {
            if (checklist.query('input[type="text"]')[i].value.trimEnd() == '' && ((i < 3 || i >= 9) || ((i >= 3 || i < 9) && document.getElementById('checklist_txt_field_' + (i+3)).style.display == 'block'))) {
              return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
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
          // add check for checklist on DB
          var reqId = FormManager.getActualValue('reqId');
          var record = cmr.getRecord('GBL_CHECKLIST', 'ProlifChecklist', {
            REQID : reqId
          });
          if (!record || !record.sectionA1) {
            return new ValidationResult(null, false,
                'Checklist has not been registered yet. Please execute a \'Save\' action before sending for processing to avoid any data loss.');
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

function postCdFormatValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postCd = FormManager.getActualValue('postCd');
        var landed = FormManager.getActualValue('landCntry');
        if (postCd != undefined && postCd != null && postCd != '') {
          switch (landed) {
          case 'EG':
            return postCdLengthDigitValidation(5, postCd);
          case 'JO':
            return postCdLengthDigitValidation(5, postCd);
          case 'LB':
            if (!postCd.match("^[0-9]{4}[ ][0-9]{4}$")) {
              return new ValidationResult(FormManager.getField('postCd'), false, 'Postal Code should be NNNN NNNN.');
            }
            break;
          case 'MA':
            return postCdLengthDigitValidation(5, postCd);
          case 'OM':
            return postCdLengthDigitValidation(3, postCd);
          case 'PK':
            return postCdLengthDigitValidation(5, postCd);
            // case 'SA':
            // return postCdLengthDigitValidation(5, postCd);
            // case 'TN':
            // return postCdLengthDigitValidation(4, postCd);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function postCdLengthDigitValidation(length, value) {
  if (value.length != length || !value.match("^[0-9]+$")) {
    return new ValidationResult(FormManager.getField('postCd'), false, 'Postal Code should be exactly ' + length + ' digits long.');
  }
  return new ValidationResult(null, true);
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

function resetVatExemptMandatoryForLocalScenario() {
  var listVatMandatoryForLocal = [ '620', '677', '680', '865', '808', '832' ];
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (listVatMandatoryForLocal.indexOf(cntry) > -1 && custSubType != undefined && custSubType != null && custSubType != '') {
    var scenario = FormManager.getActualValue('custGrp');
    if (scenario == 'LOCAL' && !(custSubType.includes('IN') || custSubType == 'PRICU' || custSubType.includes('PC'))) {
      FormManager.resetValidations('vatExempt');
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
  FormManager
      .addFormValidator(
          (function() {
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
                if (custSubGrp != null
                    && (custSubGrp.includes('XSOFT') || custSubGrp.includes('XSL') || custSubGrp.includes('XPRIC') || custSubGrp.includes('XPC') || custSubGrp
                        .includes('XGO'))) {
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
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  console.log('1cemeaCustomVATMandatory ');
  var landCntry = '';
  var addrType = 'ZP01';
  var listVatReq = [ 'AT', 'BH', 'BE', 'BR', 'BG', 'HR', 'CY', 'CZ', 'DK', 'EG', 'EE', 'FI', 'FR', 'DE', 'GR', 'GL', 'HU', 'IS', 'IE', 'IL', 'IT',
      'KZ', 'LV', 'LT', 'LU', 'MT', 'MA', 'NL', 'NO', 'PK', 'PL', 'PT', 'RO', 'RU', 'SA', 'CS', 'SK', 'SI', 'ZA', 'ES', 'SE', 'CH', 'TR', 'UA', 'AE' ];
  var listVatMandatoryForLocal = [ '620', '677', '680', '865', '808', '832' ];
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (cntry == '642' && FormManager.getActualValue('custGrp').includes('LOC')) {
    FormManager.resetValidations('vat');
    return;
  }
  if (listVatMandatoryForLocal.indexOf(cntry) > -1 && custSubType != undefined && custSubType != null && custSubType != '') {
    var scenario = FormManager.getActualValue('custGrp');
    // CMR-6020 Local or GBM SBM Local
    if ((scenario.includes('LOC') && !(custSubType.includes('IN') || custSubType == 'PRICU' || custSubType.includes('PC')))
        || custSubType.includes('EL')) {
      // Make Vat Mandatory
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      console.log("Vat is Mandatory");
      return;
    }
  }
  // Internal, Softlayer, & Private scenario - set vat to optional
  if (custSubType != null && custSubType != ''
      && (custSubType.includes('IN') || custSubType == 'SOFTL' || custSubType.includes('SL') || custSubType == 'PRICU' || custSubType.includes('PC'))) {
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
  if (custSubType != null
      && custSubType != ''
      && (custSubType == 'COMME' || custSubType == 'BUSPR' || custSubType == 'XBP' || custSubType == 'XCOM' || custSubType == 'XGOV'
          || custSubType == 'XISO' || custSubType == 'XINT')) {
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
  FormManager
      .addFormValidator(
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
                    return new ValidationResult(
                        null,
                        false,
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
  // CREATCMR-788
  FormManager.addValidator('bldg', Validators.NO_QUOTATION, [ 'Country Name (Local Language)' ]);
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

function canCopyAddress(value, rowIndex, grid) {
  // CREATCMR-1961 (CEE, ME: Copy icon disappeared)
  return true;
}

function filterCmrnoForME() {
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

function togglePPSCeidME() {
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  var _custType = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var cmrNo = FormManager.getActualValue('cmrNo');
  if (_custType == 'BUSPR' || _custType.includes('BP')) {
    FormManager.show('PPSCEID', 'ppsceid');
    FormManager.enable('ppsceid');
    FormManager.resetValidations('ppsceid');
    FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
  } else if (reqType == 'U') {
    FormManager.show('PPSCEID', 'ppsceid');
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    FormManager.readOnly('ppsceid');
  } else {
    FormManager.clearValue('ppsceid');
    FormManager.hide('PPSCEID', 'ppsceid');
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
  }
}

function setICOAndDICMandatory() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!dijit.byId('vatExempt')) {
    window.setTimeout('setICOAndDICMandatory()', 500);
  } else if (dijit.byId('vatExempt').get('checked')) {
    FormManager.removeValidator('company', Validators.REQUIRED);
  } else {
    var _custType = FormManager.getActualValue('custSubGrp');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER' && (_custType == 'BUSPR' || _custType == 'COMME' || _custType == 'THDPT')) {
      FormManager.resetValidations('company');
      FormManager.addValidator('company', Validators.REQUIRED, [ 'IČO' ], 'MAIN_CUST_TAB');
    } else {
      FormManager.removeValidator('company', Validators.REQUIRED);
    }
  }
}

function setClassificationCodeME() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  FormManager.readOnly('custClass');
  if ('C' == FormManager.getActualValue('reqType')) {
    var _custType = FormManager.getActualValue('custSubGrp');
    var isicCd = FormManager.getActualValue('isicCd');
    if (_custType == 'BUSPR' || _custType.includes('BP')) {
      FormManager.setValue('custClass', '46');
    } else if (_custType.includes('IN')) {
      FormManager.setValue('custClass', '81');
    } else if (_custType == 'PRICU' || _custType.includes('PC')) {
      FormManager.setValue('custClass', '60');
    } else if (_custType.includes('IBM')) {
      FormManager.setValue('custClass', '71');
    } else if (isicCds.has(isicCd)) {
      FormManager.setValue('custClass', '13');
    } else {
      FormManager.setValue('custClass', '11');
    }
  }
}

function lockIsicCdME() {
  var reqType = FormManager.getActualValue('reqType');
  var isic = FormManager.getActualValue('isicCd');
  if ('U' == reqType) {
    var oldISIC = null;
    var requestId = FormManager.getActualValue('reqId');
    qParams = {
      REQ_ID : requestId,
    };
    var result = cmr.query('GET.ISIC_OLD_BY_REQID', qParams);
    var oldISIC = result.ret1;
    if (oldISIC == '9500' || oldISIC == '0000') {
      FormManager.enable('isicCd');
    }
  } else if ('C' == reqType) {
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (custSubGrp.includes('PC') || custSubGrp.includes('PRI')) {
      if ('9500' == isic) {
        FormManager.readOnly('isicCd');
      }
    } else if (custSubGrp.includes('IN')) {
      if ('0000' == isic) {
        FormManager.readOnly('isicCd');
      }
    }
  }
}

function validateIsicMEValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var reqType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var isic = FormManager.getActualValue('isicCd');

        if ('U' == reqType) {
          var oldISIC = null;
          var requestId = FormManager.getActualValue('reqId');
          qParams = {
            REQ_ID : requestId,
          };
          var result = cmr.query('GET.ISIC_OLD_BY_REQID', qParams);
          var oldISIC = result.ret1;
          if (('9500' == isic || '0000' == isic) && isic != oldISIC) {
            return new ValidationResult(null, false, 'ISIC should not be changed to ' + isic + ' for this Scenario Sub-type');
          } else if ((oldISIC == '0000' || oldISIC == '9500') && isic != oldISIC) {
            return new ValidationResult(null, false, 'ISIC should not be changed to ' + isic + ' for this Scenario Sub-type');
          } else {
            return new ValidationResult(null, true);
          }
        }
        if (('C' == reqType && ('9500' == isic || '0000' == isic))) {
          if (custSubGrp.includes('BP') || custSubGrp.includes('BUS') || custSubGrp.includes('CO') || custSubGrp.includes('TH')
              || custSubGrp.includes('TP')) {
            FormManager.enable('isicCd');
            return new ValidationResult(null, false, 'ISIC ' + isic + ' should not be used for this Scenario Sub-type');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function reqReasonOnChange() {
  var reqReason = FormManager.getActualValue('reqReason');
  var addressListIGF = [ 'ZP03', 'ZD02' ];
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
  if (addressType == 'ZP03') {
    addressType = 'ZP01';
  } else if (addressType == 'ZD02') {
    addressType = 'ZD01';
  }
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
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                var reqReason = FormManager.getActualValue('reqReason');
                var addressType = FormManager.getActualValue('addrType');
                if (addressType == 'ZP03' || addressType == 'ZD02') {
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
                if (addressType != undefined && addressType != '' && addressType == 'ZP03' && cmr.addressMode != 'updateAddress') {
                  showDuplicateIGFBillToError = Number(addCount) >= 1 && addressType == 'ZP03';
                  if (showDuplicateIGFBillToError) {
                    return new ValidationResult(null, false,
                        'Only one IGF Bill-To address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
                  }
                }

                if (addressType != undefined && addressType != '' && addressType == 'ZD02' && cmr.addressMode != 'updateAddress') {
                  showDuplicateIGFInstallAtToError = Number(addCount) >= 1 && addressType == 'ZD02';
                  if (showDuplicateIGFInstallAtToError) {
                    return new ValidationResult(null, false,
                        'Only one IGF Ship-To address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
                  }
                }

                return new ValidationResult(null, true);
              }
            };
          })(), null, 'frmCMR_addressModal');
}

function isicCdOnChangeME() {
  dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
    setClassificationCodeME();
  });
}

function setCustomerName2LblAndBubble() {
  var custType = FormManager.getActualValue('custGrp');
  var landCntry = FormManager.getActualValue('landCntry');
  if (custType == 'LOCAL' || landCntry == 'HU') {
    FormManager.changeLabel('CustomerName2', 'Customer Name (2)/Local VAT');
    document.getElementById('custNm2HUInfoBubble').getElementsByClassName('cmr-info-bubble')[0].style.display = '';
  } else {
    FormManager.changeLabel('CustomerName2', 'Customer Name (2)');
    document.getElementById('custNm2HUInfoBubble').getElementsByClassName('cmr-info-bubble')[0].style.display = 'none';
  }
}

function setTaxCd1MandatoryCzech() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!dijit.byId('vatExempt')) {
    window.setTimeout('setTaxCd1MandatoryCzech()', 500);
  } else if (dijit.byId('vatExempt').get('checked')) {
    FormManager.removeValidator('taxCd1', Validators.REQUIRED);
  } else {
    var _custType = FormManager.getActualValue('custSubGrp');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER' && (_custType == 'BUSPR' || _custType == 'COMME' || _custType == 'THDPT')) {
      FormManager.resetValidations('taxCd1');
      FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'IČ' ], 'MAIN_CUST_TAB');
    } else {
      FormManager.removeValidator('taxCd1', Validators.REQUIRED);
    }
  }
}

function setCityBubble() {
  var custType = FormManager.getActualValue('custGrp');
  if (custType == 'LOCAL') {
    document.getElementById('cityRomaniaInfoBubble').getElementsByClassName('cmr-info-bubble')[0].style.display = '';
  } else {
    document.getElementById('cityRomaniaInfoBubble').getElementsByClassName('cmr-info-bubble')[0].style.display = 'none';
  }
}

function setTaxCd1MandatoryCroatia() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!dijit.byId('vatExempt')) {
    window.setTimeout('setTaxCd1MandatoryCroatia()', 500);
  } else if (dijit.byId('vatExempt').get('checked')) {
    FormManager.removeValidator('taxCd1', Validators.REQUIRED);
  } else {
    var custType = FormManager.getActualValue('custGrp');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER' && custType == 'LOCAL') {
      FormManager.resetValidations('taxCd1');
      FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'OIB' ], 'MAIN_CUST_TAB');
    } else {
      FormManager.removeValidator('taxCd1', Validators.REQUIRED);
    }
  }
}

function disableSBO() {
  FormManager.readOnly('salesBusOffCd');
}

function setEngineeringBO() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole');
  var _custType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var cmrIssuing = FormManager.getActualValue('cmrIssuingCntry');
  var cntryRegion = FormManager.getActualValue('countryUse');
  FormManager.addValidator('engineeringBo', Validators.DIGIT, [ 'Engineering BO' ], 'MAIN_IBM_TAB');
  if (reqType == 'C') {
    if (cntryRegion == '808AF') {
      FormManager.setValue('engineeringBo', '6140000');
      FormManager.readOnly('engineeringBo');
    }
  } else if (reqType == 'U') {
    if (cmrIssuing == SysLoc.PAKISTAN) {
      var cebo = FormManager.getActualValue('engineeringBo');
      if (cebo == '6140000') {
        FormManager.readOnly('engineeringBo');
      }
    } else {
      if (role == GEOHandler.ROLE_REQUESTER) {
        FormManager.readOnly('engineeringBo');
      } else {
        FormManager.enable('engineeringBo');
      }
    }
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

function hideDisableAutoProcessingCheckBox() {
  FormManager.hide('DisableAutoProcessing', 'disableAutoProc');
}

function afterConfigTemplateLoadForME() {
  filterCmrnoForME();
  togglePPSCeidME();
  // disableSBO();
  setEngineeringBO();
  addLandCntryHandler();
}

function afterConfigForME() {
  isicCdOnChangeME();
  // reqReasonOnChange();
  hideDisableAutoProcessingCheckBox();
  // CREATCMR-788
  addressQuotationValidatorCEMEA();

}

function afterConfigForSlovakia() {
  setICOAndDICMandatory();
}

function initAddressPageHungary() {
  setCustomerName2LblAndBubble();
}

function afterConfigTemplateForCzech() {
  setTaxCd1MandatoryCzech();
}

function initAddressPageRomania() {
  setCityBubble();
}

function afterConfigTemplateForCroatia() {
  setTaxCd1MandatoryCroatia();
}

function addLandCntryHandler() {
  if (_landCntryHandler == null) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      postCdMandatoryByLandCntry();
    });
  }
}

function postCdMandatoryByLandCntry() {
  var postCdMandatoryCntryList = [ 'EG', 'IQ', 'JO', 'LB', 'MA', 'OM', 'PK', 'SA', 'TN' ];
  if (postCdMandatoryCntryList.indexOf(FormManager.getActualValue('landCntry')) > -1) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code' ], 'MAIN_NAME_TAB');
  } else {
    FormManager.removeValidator('postCd', Validators.REQUIRED);
  }
}
function validatorsDIGIT() {
  // FormManager.addValidator('EngineeringBo', Validators.DIGIT, [
  // 'EngineeringBo' ]);
  FormManager.addValidator('taxCd2', Validators.DIGIT, [ 'Enterprise Number' ]);
  FormManager.addValidator('taxCd2', validEnterpriseNumber6Length, [ 'Enterprise Number' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('enterprise', Validators.DIGIT, [ 'Company Number' ]);
  FormManager.addValidator('enterprise', validCompanyNumber6Length, [ 'Company Number' ], 'MAIN_IBM_TAB');
}

function validEnterpriseNumber6Length(input) {
  var taxCd2 = FormManager.getActualValue('taxCd2');
  if (taxCd2 != '' && taxCd2 != null && taxCd2 != undefined) {
    if (taxCd2.length != 6) {
      return new ValidationResult(input, false, 'Enterprise Number must be 6 length');
    }
  }
  return new ValidationResult(input, true);
}

function validCompanyNumber6Length(input) {
  var enterprise = FormManager.getActualValue('enterprise');
  if (enterprise != '' && enterprise != null && enterprise != undefined) {
    if (enterprise.length != 6) {
      return new ValidationResult(input, false, 'Company Number must be 6 length');
    }
  }
  return new ValidationResult(input, true);
}

// CMR-4606
function validatorsDIGITForDupField() {
  if (dijit.byId('cisServiceCustIndc') != undefined) {
    if (dijit.byId('cisServiceCustIndc').get('checked')) {
      FormManager.addValidator('taxCd3', Validators.DIGIT, [ 'Dup Enterprise Number' ]);
    }
  }
}

function addEmbargoCdValidatorForME() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = FormManager.getActualValue('userRole');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var embargoCd = FormManager.getActualValue('embargoCd');
        if (cntry == '808' || cntry == '865') {
          if (embargoCd && !(embargoCd == 'E' || embargoCd == 'S' || embargoCd == 'J' || embargoCd == 'C' || embargoCd == '')) {
            return new ValidationResult(null, false, 'Order Block Code should be only E, S, J, C, Blank allowed');
          }
        } else {
          if (embargoCd && !(embargoCd == 'E' || embargoCd == 'S' || embargoCd == 'J' || embargoCd == '')) {
            return new ValidationResult(null, false, 'Order Block Code should be only E, S, J, Blank allowed');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}
// CMR-4606
function checkGAddressExist() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var cmrNo = FormManager.getActualValue('cmrNo');
        if (FormManager.getActualValue('reqType') != 'U') {
          return new ValidationResult(null, true);
        } else {
          if (cntry == '821' && dijit.byId('cisServiceCustIndc').get('checked')) {
            var cntryDup = FormManager.getActualValue('dupIssuingCntryCd');
            var qParamsDup = {
              RCYAA : cntryDup,
              RCUXA : cmrNo
            };
            var resultD = cmr.query('GET_G_SEQ_FROM_LEGACY', qParamsDup);
            if (resultD && resultD.ret1 && resultD.ret1 != '') {
              return new ValidationResult(null, true);
            } else {
              return new ValidationResult(null, false, 'The Dup Country Missing a G address,please update it independent.');
            }
          }
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}
// CMR-6242
function addVATAttachValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var vat = FormManager.getActualValue('vat');
        if (vat != null && vat != undefined && vat != '') {
          var id = FormManager.getActualValue('reqId');
          var reqType = FormManager.getActualValue('reqType');
          var retVAT = cmr.query('GET.VAT_OLD_BY_REQID', {
            REQ_ID : id
          });
          var vatChangedStatus = false;
          if (retVAT == null || vat != retVAT.ret1) {
            vatChangedStatus = true;
          }
          if (reqType == 'C' || (reqType == 'U' && vatChangedStatus)) {
            var ret = cmr.query('CHECK_VATD_ATTACHMENT', {
              ID : id
            });
            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, 'VAT/TAX Documentation is mandatory, please add VAT/TAX Documentation attachment.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}
var currentChosenScenarioME = '';
function setIsuCtcOnScenarioChange() {
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  var scenario = FormManager.getActualValue('custSubGrp');
  var scenarioChanged = false;
  if (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != scenario) {
    scenarioChanged = true;
  }
  scenarioChanged = scenarioChanged || (currentChosenScenarioME != '' && currentChosenScenarioME != scenario);
  currentChosenScenarioME = scenario;
  if (scenario == 'BUSPR' || scenario.includes('BP')) {
    FormManager.setValue('isuCd', '8B');
    FormManager.setValue('clientTier', '');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
  } else if (scenario.includes('PC') || scenario.includes('PRI')) {
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('clientTier', 'Q'); 
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
  } else if (scenario.includes('IN') || scenario.includes('IBM')) {
    FormManager.setValue('isuCd', '21');
    FormManager.setValue('clientTier', '');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
  } else if (scenarioChanged) {
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('clientTier', 'Q');
    FormManager.enable('isuCd');
    FormManager.enable('clientTier');
  }
  // CREATCMR-816 No.3 Set company number 985518
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if ((SysLoc.UNITED_ARAB_EMIRATES == cntry || SysLoc.ABU_DHABI == cntry) && scenarioChanged) {
    if (scenario == 'BUSPR' || scenario.includes('BP') || scenario.includes('IN')) {
      FormManager.setValue('enterprise', '');
    } else {
      // FormManager.setValue('enterprise', '985518');
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

// CREATCMR-4293
function setCTCValues() {

  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  // Business Partner
  var custSubGrpForBusinessPartner = [ 'AFBP', 'BUSPR', 'ELBP', 'EXBP', 'JOBP', 'JOXBP', 'PKBP', 'PKXBP', 'PSBP', 'XBP' ];

  // Business Partner
  if (custSubGrpForBusinessPartner.includes(custSubGrp)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '8B') {
      FormManager.setValue('clientTier', '');
    }
  }

  // Internal
  var custSubGrpForInternal = [ 'AFINT', 'INTER', 'JOINT', 'JOXIN', 'PKINT', 'PKXIN', 'PSINT', 'XINT' ];

  // Internal
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
  var isuNonBlankCtc = ['32', '34', '36'];
  var ctcValues = ['Q', 'Y', 'T'];

  if (!isuNonBlankCtc.includes(isuCode)) {
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
  } else if (isuNonBlankCtc.includes(isuCode)) {
    if (isuCode == '34' && clientTierCode != 'Q') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept value \'Q\'.');
    } else if (isuCode == '32' && clientTierCode != 'T') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept value \'T\'.');
    } else if (isuCode == '36' && clientTierCode != 'Y') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept value \'Y\'.');
    }
  } else {
    if (ctcValues.includes(clientTierCode) || clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');
      $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\', \'Y\', \'T\' or Blank.');
    }
  }
}
// CREATCMR-4293
// CREATCMR-788
function addressQuotationValidatorCEMEA() {
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name (1)' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name (2)' ]);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, [ 'Customer Name (3)' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Attention Person' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street Address' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
  FormManager.addValidator('bldg', Validators.NO_QUOTATION, [ 'Country Name (Local Language)' ]);

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
            oldISU =  result.ret3 != null ? result.ret3 : '';
            
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

dojo
    .addOnLoad(function() {
      GEOHandler.CEMEA_COPY = [ '358', '359', '363', '603', '607', '620', '626', '644', '642', '651', '668', '677', '680', '693', '694', '695',
          '699', '704', '705', '707', '708', '740', '741', '752', '762', '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832',
          '849', '850', '865', '889', '729' ];
      GEOHandler.CEMEA = [ '358', '359', '363', '603', '607', '620', '626', '644', '642', '651', '668', '677', '680', '693', '694', '695', '699',
          '704', '705', '707', '708', '740', '741', '752', '762', '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832', '849',
          '850', '865', '889', '618', '729' ];
      GEOHandler.CEMEA_CHECKLIST = [ '358', '359', '363', '607', '620', '626', '651', '675', '677', '680', '694', '695', '713', '741', '752', '762',
          '767', '768', '772', '787', '805', '808', '821', '823', '832', '849', '850', '865', '889' ];
      GEOHandler.NON_CEE_CHECK = [ '620', '675', '677', '680', '713', '752', '762', '767', '768', '772', '805', '808', '823', '832', '849', '850',
          '865', '729' ];
      GEOHandler.CEE = [ '603', '607', '626', '644', '651', '668', '693', '694', '695', '699', '704', '705', '707', '708', '740', '741', '787',
          '820', '821', '826', '889', '358', '359', '363' ];
      GEOHandler.ME = [ '620', '642', '675', '677', '680', '752', '762', '767', '768', '772', '805', '808', '823', '832', '849', '850', '865', '729' ];
      GEOHandler.CEMEA_EXCLUDE_CEE = GEOHandler.CEMEA.filter(function(v) {
        return GEOHandler.CEE.indexOf(v) == -1
      });
      console.log('adding CEMEA functions...');
      GEOHandler.addAddrFunction(addCEMEALandedCountryHandler, GEOHandler.CEMEA);
      // GEOHandler.enableCopyAddress(GEOHandler.CEMEA, validateCEMEACopy);
      GEOHandler.enableCustomerNamesOnAddress(GEOHandler.CEMEA);
      GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.CEMEA);
      GEOHandler.setRevertIsicBehavior(false);

      GEOHandler.addAfterConfig(afterConfigForCEMEA, GEOHandler.CEMEA);
      GEOHandler.addAfterConfig(addHandlersForCEMEA, GEOHandler.CEMEA);
      GEOHandler.addAfterConfig(addChecklistBtnHandler, GEOHandler.CEMEA);
      GEOHandler.addAfterConfig(addVatExemptHandler, GEOHandler.CEMEA);
      GEOHandler.addAfterConfig(addCISHandler, [ SysLoc.RUSSIA ]);
      GEOHandler.addAfterConfig(setAbbrvNmLoc, GEOHandler.CEMEA);
      GEOHandler.addAfterTemplateLoad(setAbbrvNmLoc, GEOHandler.CEMEA);
      GEOHandler.addAfterConfig(lockAbbrv, GEOHandler.CEMEA);
      // CMR-801:comment out to unlock embargo code
      GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.CEMEA);

      // CMR-2096-Austria - "Central order block code"
      GEOHandler.addAfterConfig(lockOrdBlk, SysLoc.AUSTRIA);

      GEOHandler.addAfterConfig(custNmAttnPersonPhoneValidation, [ SysLoc.AUSTRIA ]);
      // GEOHandler.addAfterConfig(setScenarioTo3PA, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterTemplateLoad(lockAbbrvLocnForScenrio, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAddrFunction(lockAbbrvLocnForScenrio, [ SysLoc.AUSTRIA ]);
      // GEOHandler.addAddrFunction(setScenarioTo3PAOnAddrSave, [ SysLoc.AUSTRIA
      // ]);

      GEOHandler.addAfterConfig(custNmAttnPersonPhoneValidationOnChange, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterConfig(reqReasonOnChange, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterConfig(phoneNoValidation, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterConfig(phoneNoValidationOnChange, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterConfig(setEnterpriseValues, GEOHandler.CEMEA);
      GEOHandler.addAfterConfig(setVatRequired, GEOHandler.CEMEA);
      GEOHandler.addAfterConfig(setPreferredLang, GEOHandler.CEMEA);
      GEOHandler.addAfterTemplateLoad(setVatRequired, GEOHandler.CEMEA);
      // CMR-2101 Austria the func for Austria, setSBO also used by CEE
      // countries
      GEOHandler.addAfterConfig(setSBO, GEOHandler.CEMEA);
      GEOHandler.addAfterTemplateLoad(setSBO, GEOHandler.CEMEA);
      // GEOHandler.addAfterConfig(setSBO2, [ SysLoc.RUSSIA ]);
      // GEOHandler.addAfterTemplateLoad(setSBO2, [ SysLoc.RUSSIA ]);
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

      GEOHandler.addAfterTemplateLoad(cmrNoEnabled, GEOHandler.CEMEA_EXCLUDE_CEE);
      GEOHandler.addAfterConfig(cmrNoEnabled, GEOHandler.CEMEA_EXCLUDE_CEE);

      GEOHandler.addAfterConfig(cmrNoEnableForME, GEOHandler.ME);
      GEOHandler.addAfterTemplateLoad(cmrNoEnableForME, GEOHandler.ME);
      GEOHandler.registerValidator(addCmrNoValidatorForME, GEOHandler.ME);
      GEOHandler.registerValidator(addEmbargoCdValidatorForME, GEOHandler.ME);

      GEOHandler.addAfterTemplateLoad(afterConfigForCEMEA, GEOHandler.CEMEA);
      GEOHandler.addAfterConfig(setCountryDuplicateFields, SysLoc.RUSSIA);
      GEOHandler.addAfterTemplateLoad(setCountryDuplicateFields, SysLoc.RUSSIA);
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
      GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.CEMEA, null, true);
      GEOHandler.registerValidator(addCrossBorderValidatorForCEMEA, [ '707', '762', '808', '620', '767', '805', '823', '677', '680', '832' ], null,
          true);
      GEOHandler.registerValidator(addGaddrValidatorForCEE, GEOHandler.CEE, null, true);
      GEOHandler.registerValidator(postCdFormatValidator, GEOHandler.ME, null, true);
      // GEOHandler.registerValidator(postCdLenChecks, GEOHandler.CEMEA, null,
      // true);
      GEOHandler.registerValidator(requireVATForCrossBorderAT, [ SysLoc.AUSTRIA ], null, true);
      GEOHandler.registerValidator(addCmrNoValidator, GEOHandler.CEMEA, null, true, [ '603', '607', '626', '644', '651', '668', '693', '694', '695',
          '699', '704', '705', '707', '708', '740', '741', '787', '820', '821', '826', '889', '358', '359', '363' ]);
      GEOHandler.registerValidator(cemeaCustomVATValidator('', 'MAIN_CUST_TAB', 'frmCMR', 'ZP01'), GEOHandler.CEMEA, null, true);
      // GEOHandler.registerValidator(customCrossPostCdValidator,
      // GEOHandler.CEMEA,
      // null, true);

      GEOHandler.addAfterConfig(initICEField, [ SysLoc.MOROCCO ]);
      GEOHandler.addAfterTemplateLoad(initICEField, [ SysLoc.MOROCCO ]);
      GEOHandler.registerValidator(addIceFormatValidationMorocco, [ SysLoc.MOROCCO ], null, true);
      // GEOHandler.registerValidator(addIceBillingValidator, [ SysLoc.MOROCCO
      // ],
      // null, true);

      GEOHandler.registerValidator(validateAbbrevNmForCIS, [ SysLoc.RUSSIA ], null, true);

      // GEOHandler.registerValidator(addPoBoxValidator, [ SysLoc.AUSTRIA],
      // null,
      // true);

      GEOHandler.registerValidator(addStreetAndPoBoxFormValidator, [ SysLoc.AUSTRIA ], null, true);
      GEOHandler.registerValidator(addStreetAndPoBoxFormValidator, GEOHandler.ME);
      GEOHandler.registerValidator(restrictDuplicateAddrAT, [ SysLoc.AUSTRIA ]);

      // Checklist
      GEOHandler.addAfterConfig(setChecklistStatus, GEOHandler.CEMEA_CHECKLIST);
      GEOHandler.registerValidator(addCEMEAChecklistValidator, GEOHandler.CEMEA_CHECKLIST);

      /* 1438717 - add DPL match validation for failed dpl checks */
      GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.NON_CEE_CHECK, GEOHandler.ROLE_PROCESSOR, true);
      // GEOHandler.addAfterConfig(hideEngineeringBOForReq,
      // GEOHandler.CEMEA_EXCLUDE_CEE);
      GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.CEE, GEOHandler.ROLE_PROCESSOR, true);
      GEOHandler.addAfterConfig(validatorsDIGIT, GEOHandler.ME);
      // CMR-1912 Vat should be required for AT local-BP and Commercial
      GEOHandler.addAfterConfig(customVATMandatoryForAT, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterTemplateLoad(customVATMandatoryForAT, [ SysLoc.AUSTRIA ]);
      /*
       * GEOHandler.addAfterConfig(cemeaCustomVATMandatory, GEOHandler.CEMEA);
       * GEOHandler.addAfterTemplateLoad(cemeaCustomVATMandatory,
       * GEOHandler.CEMEA);
       */
      // GEOHandler.registerValidator(cemeaCustomVATMandatory('',
      // 'MAIN_CUST_TAB',
      // 'frmCMR', 'ZP01'), GEOHandler.CEMEA, null, true);
      /*
       * 1496135: Importing G address from SOF for Update Requests jz: add local
       * country name text box
       */
      // GEOHandler.registerValidator(similarAddrCheckValidator, [
      // SysLoc.AUSTRIA ],
      // null, true);
      GEOHandler.addToggleAddrTypeFunction(toggleLocalCountryName, GEOHandler.CEMEA);
      GEOHandler.addAddrFunction(toggleLocalCountryNameOnOpen, GEOHandler.CEMEA);
      // CMR-2101 SBO is required for processor
      GEOHandler.registerValidator(validateSBO, [ SysLoc.AUSTRIA ], GEOHandler.ROLE_PROCESSOR, true);
      GEOHandler.addAfterConfig(validateSBO, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterTemplateLoad(validateSBO, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterTemplateLoad(setISUCTCOnIMSChange, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterConfig(setISUCTCOnIMSChange, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterConfig(lockIBMtab, [ SysLoc.AUSTRIA ]);
      GEOHandler.addAfterTemplateLoad(lockIBMtab, [ SysLoc.AUSTRIA ]);
      // ME
      GEOHandler.addAfterConfig(afterConfigTemplateLoadForME, GEOHandler.ME);
      GEOHandler.addAfterTemplateLoad(afterConfigTemplateLoadForME, GEOHandler.ME);
      GEOHandler.addAfterConfig(afterConfigForME, GEOHandler.ME);
      GEOHandler.registerValidator(restrictDuplicateAddr, GEOHandler.CEE, null, true);
      GEOHandler.registerValidator(validateIsicMEValidator, GEOHandler.ME, null, true);
      GEOHandler.registerValidator(addAddressTypeValidatorME, GEOHandler.ME, null, true);
      GEOHandler.addAfterTemplateLoad(setVatValidatorCEMEA, GEOHandler.ME);
      // CMR-5993
      GEOHandler.registerValidator(validateMESBO, GEOHandler.ME, GEOHandler.ROLE_PROCESSOR, true);
      GEOHandler.addAfterConfig(setMESBO, GEOHandler.ME);
      GEOHandler.addAfterTemplateLoad(setMESBO, GEOHandler.ME);
      GEOHandler.registerValidator(dupCMRExistCheck, GEOHandler.ME, null, true);

      GEOHandler.registerValidator(checkGAddressExist, [ SysLoc.RUSSIA ], null, true);
      GEOHandler.addAfterConfig(validatorsDIGITForDupField, [ SysLoc.RUSSIA ]);
      GEOHandler.addAfterConfig(setClientTier2Values, [ SysLoc.RUSSIA ]);
      GEOHandler.addAfterTemplateLoad(setClientTier2Values, [ SysLoc.RUSSIA ]);

      // Slovakia
      GEOHandler.addAfterConfig(afterConfigForSlovakia, [ SysLoc.SLOVAKIA ]);
      GEOHandler.addAfterTemplateLoad(afterConfigForSlovakia, [ SysLoc.SLOVAKIA ]);
      // Hungary
      GEOHandler.addAddrFunction(initAddressPageHungary, [ SysLoc.HUNGARY ]);
      // GEOHandler.addAfterConfig(afterConfigTemplateForHungary, [
      // SysLoc.HUNGARY
      // ]);
      // GEOHandler.addAfterTemplateLoad(afterConfigTemplateForHungary, [
      // SysLoc.HUNGARY ]);
      // Czech
      GEOHandler.addAfterConfig(afterConfigTemplateForCzech, [ SysLoc.CZECH_REPUBLIC ]);
      GEOHandler.addAfterTemplateLoad(afterConfigTemplateForCzech, [ SysLoc.CZECH_REPUBLIC ]);
      // Romania
      GEOHandler.addAddrFunction(initAddressPageRomania, [ SysLoc.ROMANIA ]);
      // Croatia
      GEOHandler.addAfterConfig(afterConfigTemplateForCroatia, [ SysLoc.CROATIA ]);
      GEOHandler.addAfterTemplateLoad(afterConfigTemplateForCroatia, [ SysLoc.CROATIA ]);

      GEOHandler.addAfterConfig(lockIsicCdME, GEOHandler.ME);
      GEOHandler.addAfterTemplateLoad(lockIsicCdME, GEOHandler.ME);
      GEOHandler.addAfterConfig(resetVatExemptMandatoryForLocalScenario, GEOHandler.ME);
      GEOHandler.addAfterTemplateLoad(resetVatExemptMandatoryForLocalScenario, GEOHandler.ME);
      GEOHandler.addAfterTemplateLoad(setIsuCtcOnScenarioChange, GEOHandler.ME);
      GEOHandler.addAfterTemplateLoad(setClassificationCodeME, GEOHandler.ME);
      GEOHandler.registerValidator(addVATAttachValidation, [ SysLoc.EGYPT ], null, true);
      // GEOHandler.addAfterConfig(addPrefixVat, GEOHandler.CEE);
      // GEOHandler.addAfterTemplateLoad(addPrefixVat, GEOHandler.CEE);
      // GEOHandler.addAddrFunction(addPrefixVat, GEOHandler.CEE);

      GEOHandler.addAfterConfig(omanVat, GEOHandler.ME);
      GEOHandler.addAfterTemplateLoad(omanVat, GEOHandler.ME);
      GEOHandler.addAfterConfig(addHandlersForOman, GEOHandler.ME);
      GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.ME, null, true);

      // CREATCMR-4293
      GEOHandler.addAfterTemplateLoad(setCTCValues, GEOHandler.ME);
      GEOHandler.registerValidator(clientTierValidator, GEOHandler.ME, null, true);
      
      GEOHandler.addAfterConfig(checkChecklistButtons, GEOHandler.ME);

    });
