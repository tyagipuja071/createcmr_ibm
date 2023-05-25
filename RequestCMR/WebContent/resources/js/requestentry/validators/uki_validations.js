/* Define the global variables here */

var _customerTypeHandler = null;
var _vatExemptHandler = null;
var _isuCdHandler = null;
var _postCdHandler = null
var _isuCdHandlerIE = null;
var _isicCdHandler = null;
var _requestingLOBHandler = null;
var _economicCdHandler = null;
var _custSubTypeHandler = null;
var _landCntryHandler = null;
var _stateProvITHandler = null;
var _internalDeptHandler = null;
var _isicHandler = null;
var addrTypeHandler = [];
var _hwMstrInstallFlagHandler = null;
var _isicCdCRNHandler = null;
var _crnExemptHandler = null;
var _postCdHandlerUK = null;
var _oldIsicCd = null;
var NORTHERN_IRELAND_POST_CD = [ 'BT' ];
var UK_LANDED_CNTRY = [ 'AI', 'BM', 'IO', 'VG', 'KY', 'FK', 'GI', 'MS', 'PN', 'SH', 'TC', 'GS', 'GG', 'JE', 'IM', 'AO', 'IE' ];
var _landCntryHandlerUK = null;
var _landCntryHandlerIE = null;
var _postalCdUKHandler = null;
var _cityUKHandler = null;

/* end of global vars */

function addHandlersUKI() {

  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  /* Common handlers */
  if ((cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') && (FormManager.getActualValue('cmrIssuingCntry') == '866' || FormManager.getActualValue('cmrIssuingCntry') == '754')) {
    for (var i = 0; i < _addrTypesForEMEA.length; i++) {
      addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForEMEA[i]), 'onClick', function(value) {
        disableAddrFieldsUKI();
      });
    }
  }
  if (_clientTierHandler == null && FormManager.getField('clientTier')) {
    _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      if (issuingCntry == '866') {
        autoSetSBO(value, _pagemodel.clientTier);
        setSrAndSboOnIsicUK(value, _pagemodel.clientTier);
        autoSetSboSrOnAddrSaveUK();
        setSboValueBasedOnIsuCtcUK(value);
      } else {
        addSBOSRLogicIE(value);
        setSboValueBasedOnIsuCtcIE(value);
      }
    });
  }

  /* UKI specific */
  if (issuingCntry == '866') {
    if (_isuCdHandler == null && FormManager.getField('isuCd')) {
      _isuCdHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
        setClientTierBasedOnIsuUKI();
        lockIsuCtcUKI();
        setSboValueBasedOnIsuCtcUK(value);
      });
    }

    if (_isicCdHandler == null && FormManager.getField('isicCd')) {
      _oldIsicCd = FormManager.getActualValue('isicCd');
      _isicCdHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
        var currentIsicCd = FormManager.getActualValue('isicCd');
        var isIsicNull = currentIsicCd == '' || currentIsicCd == null || currentIsicCd == undefined;
        if (_oldIsicCd != currentIsicCd) {
          if (isIsicNull) {
            FormManager.setValue('isicCd', _oldIsicCd);
          }
          autoSetSBO(value, _pagemodel.isicCd);
          setSrAndSboOnIsicUK(value, _pagemodel.isicCd);
        }
      });
    }

    if (_postCdHandlerUK == null && FormManager.getField('postCd')) {
      _postCdHandlerUK = dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
        autoSetSBO(value, _pagemodel.postCd);
        autoSetSboSrOnAddrSaveUK(value, _pagemodel.postCd);
      });
    }
  }

  if (_internalDeptHandler == null) {
    _internalDeptHandler = dojo.connect(FormManager.getField('ibmDeptCostCenter'), 'onChange', function(value) {
      autoSetAbbrevNmFrmDept();
    });
  }

  if (_isicCdCRNHandler == null) {
    _isicCdCRNHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', configureCRNForUKI);
  }

  if (_isicCdCRNHandler && _isicCdCRNHandler[0]) {
    _isicCdCRNHandler[0].onChange();
  }

  if (reqType == 'C' && (custSubType != '' && custSubType != undefined)) {
    autoSetAbbrNameUKI();
  }

  if (_customerTypeHandler == null) {
    var _custType = null;
    var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
    _customerTypeHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      _custType = FormManager.getActualValue('custSubGrp');
      autoSetSpecialTaxCdByScenario();
      if (_custType != '') {
        var qParams = {
          REQ_ID : FormManager.getActualValue('reqId'),
        };
        var result = cmr.query('DATA.GET.CUSTSUBGRP.BY_REQID', qParams);
        var custTypeinDB = result.ret1;
        autoSetCollectionCdByScenario(_custType);
        autoSetSBO(_custType, custTypeinDB);
        autoSetAbbrNameUKI();
        autoSetUIFieldsOnScnrioUKI();
        if (issu_cntry == SysLoc.IRELAND) {
          setSboValueBasedOnIsuCtcIE();
        }
      }
    });
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {

      if (PageManager.isReadOnly()) {
        return;
      }

      if (dijit.byId('vatExempt').get('checked')) {
        console.log(">>> Process vatExempt remove * >> ");
        FormManager.resetValidations('vat');
        FormManager.readOnly('vat');
      } else {
        console.log(">>> Process vatExempt add * >> ");
        if ("C" == FormManager.getActualValue('reqType')) {
          FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
        }
        FormManager.enable('vat');
      }
    });
  }

  if (_vatExemptHandler && _vatExemptHandler[0]) {
    _vatExemptHandler[0].onClick();
  }

  if (_crnExemptHandler == null) {
    _crnExemptHandler = dojo.connect(FormManager.getField('restrictInd'), 'onClick', configureCRNForUKI);
  }

  if (_crnExemptHandler && _crnExemptHandler[0]) {
    _crnExemptHandler[0].onClick();
  }

  if (_landCntryHandlerUK == null && issuingCntry == SysLoc.UK) {
    _landCntryHandlerUK = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      optionalRulePostalCodeUK();
    });
  }

  if (_landCntryHandlerUK && _landCntryHandlerUK[0]) {
    _landCntryHandlerUK[0].onChange();
  }

  if (_landCntryHandlerIE == null && issuingCntry == SysLoc.IE) {
    _landCntryHandlerIE = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      setClientTierBasedOnIsuUKI();
      lockIsuCtcUKI();
      setSboValueBasedOnIsuCtcIE();
    });
  }

  if (_landCntryHandlerIE && _landCntryHandlerIE[0]) {
    _landCntryHandlerIE[0].onChange();
  }

  if (_isuCdHandlerIE == null && FormManager.getField('isuCd') && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
    _isuCdHandlerIE = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierBasedOnIsuUKI();
      lockIsuCtcUKI();
      setSboValueBasedOnIsuCtcIE(value);
    });
  }

  if (_isicHandler == null) {
    _oldIsicCd = FormManager.getActualValue('isicCd');
    _isicHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      if (_oldIsicCd != FormManager.getActualValue('isicCd')) {
        setISUCTCOnISIC()
        setCustClassCd()
      }
    });
  }

}

function lockRequireFieldsUKI() {
  var reqType = FormManager.getActualValue('reqType').toUpperCase();
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var vat = FormManager.getActualValue('vat');
  var fieldsToDisable = new Array();
  var lockedFieldsForAllRoles = [ 'PRICU', 'BUSPR', 'INTER', 'IBMEM' ];
  var cmrNo = FormManager.getActualValue('cmrNo');
  
  FormManager.readOnly('inacCd');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (reqType == 'C') {
    if (custSubGroup == 'PRICU' || custSubGroup == 'BUSPR' || custSubGroup == 'INTER') {
      FormManager.readOnly('inacCd');
    }
  }

  if (role == 'REQUESTER') {
    FormManager.enable('abbrevNm');
    if (reqType == 'U' || reqType == 'X') {
    } else {
      FormManager.readOnly('embargoCd');
    }
  } else if (role == 'PROCESSOR') {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
    FormManager.enable('embargoCd');
  }

  if (lockedFieldsForAllRoles.includes(custSubGroup)) {
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('collectionCd');
  } else if (reqType == 'C' && role == 'PROCESSOR') {
    FormManager.enable('isuCd');
    FormManager.enable('clientTier');
    FormManager.enable('collectionCd');
  }

  if (reqType == 'C' && role == 'REQUESTER') {
    FormManager.readOnly('abbrevNm');
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    FormManager.readOnly('abbrevLocn');
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);
    FormManager.resetValidations('specialTaxCd');
    FormManager.readOnly('specialTaxCd');
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('collectionCd');
    FormManager.readOnly('buyingGroupId');
    FormManager.readOnly('globalBuyingGroupId');
    FormManager.readOnly('company');
    FormManager.readOnly('covId');
    FormManager.readOnly('geoLocationCode');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('dunsNo');
    FormManager.readOnly('soeReqNo');
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('embargoCd');

    if (custSubGroup != 'XBP' && custSubGroup != 'BUSPR') {
      FormManager.readOnly('ppsceid');
    } else {
      FormManager.enable('ppsceid');
    }
  }

  if (reqType == 'U' || reqType == 'X' && role == 'REQUESTER') {
    FormManager.readOnly('abbrevNm');
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    FormManager.readOnly('abbrevLocn');
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);
  } else if (role == 'PROCESSOR') {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
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

  if (reqType == 'U' && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }

  // CREATCMR-1727

  if (role != "PROCESSOR" || FormManager.getActualValue('viewOnlyPage') == 'true' || reqType == 'U') {
    FormManager.readOnly('cmrNo');
  } else {
    if (!cmrNo.startsWith('P')) {
      FormManager.enable('cmrNo');
    }
  }

  if (reqType == 'C') {
    FormManager.readOnly('custClass');
  }
  if (reqType == 'U' && role == 'REQUESTER') {
    FormManager.readOnly('custClass');
  } else if (reqType == 'U' && role == 'PROCESSOR') {
    FormManager.enable('custClass');
  }

  if (reqType == 'U' && role == 'REQUESTER') {
    FormManager.enable('abbrevNm');
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    FormManager.enable('abbrevLocn');
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);
  } else if (role == 'PROCESSOR') {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
  }

}

function setIBMFieldsMandtOptional() {
  console.log('setIBMFieldsMandtOptional.......')
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('reqType') == 'C') {
    if (role.toUpperCase() == 'REQUESTER') {
      FormManager.removeValidator('isuCd', Validators.REQUIRED);
      FormManager.removeValidator('clientTier', Validators.REQUIRED);
      FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
      FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);

    } else {
      FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], null);
      if (!('5K' == FormManager.getActualValue('isuCd'))) {
        FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], null);
      }

      FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep No' ], null);
      FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ], null);
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

function addUKAddressTypeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '866') {
          return new ValidationResult(null, true);
        }
        console.log(">>> validating addUKAddressTypeValidator..............");
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

function autoSetAbbrevNmFrmDept() {
  console.log('>>> running autoSetAbbrevNmFrmDept');
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

  if (('XINTR' == custType || 'INTER' == custType) && (SysLoc.IRELAND == cntry || SysLoc.UK == cntry)) {
    var dept = FormManager.getActualValue('ibmDeptCostCenter');

    if (tmInstallName1.length > 11) {
      tmInstallName1 = tmInstallName1.substring(0, 11);
    }
    if (!abbrevNm.includes('IBM/')) {
      abbrevNm = 'IBM/' + dept + '/' + tmInstallName1;
      FormManager.setValue('abbrevNm', abbrevNm);
      FormManager.readOnly('abbrevNm');
    }
  }
}

function getZS01LandCntry() {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var results = cmr.query('ADDR.GET.ZS01LANDCNTRY.BY_REQID', reqParam);
  var landCntry = results.ret2 != undefined ? results.ret2 : '';
  return landCntry;
}

function afterConfigForUKI() {
  console.log(">>> Process afterConfigForUKI.. ");
  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var companyNum = FormManager.getActualValue('taxCd1');
  var isicCd = FormManager.getActualValue('isicCd');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var zs01LandCntry = getZS01LandCntry();

  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  cmr.hideNode('deptInfo');
  // autoSetAbbrevLocnHandler();
  if (reqType == 'U' || reqType == 'X') {
    FormManager.resetValidations('collectionCd');
  }

  optionalRuleForVatUK();
  addressQuotationValidatorUKI();
}

function setClientTierBasedOnIsuUKI() {
  var reqType = FormManager.getActualValue('reqType');
  var isuCd = FormManager.getActualValue('isuCd');
  var isuCtcVals = {
    '32' : 'T',
    '34' : 'Q',
    '36' : 'Y'
  };
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (isuCd != null && isuCd != undefined && isuCd != '') {
    // reset
    FormManager.resetValidations('clientTier');
    if (isuCtcVals.hasOwnProperty(isuCd)) {
      FormManager.setValue('clientTier', isuCtcVals[isuCd]);
    } else {
      FormManager.setValue('clientTier', '');
    }
  }
}
function lockIsuCtcUKI() {
  var reqType = FormManager.getActualValue('reqType');
  var isuCd = FormManager.getActualValue('isuCd');
  var isuCtcVals = {
    '32' : 'T',
    '34' : 'Q',
    '36' : 'Y'
  };
  if (isuCd != null && isuCd != undefined && isuCd != '') {
    // reset
    FormManager.resetValidations('clientTier');
    if (isuCtcVals.hasOwnProperty(isuCd)) {
      FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'ClientTier' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('clientTier', Validators.REQUIRED);
    }
  }
}

function configureCRNForUKI() {
  var reqType = FormManager.getActualValue('reqType');
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var isicVal = FormManager.getActualValue('isicCd');
  var zs01LandCntry = getZS01LandCntry();
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (reqType == 'C') {
    if ("PRICU" == FormManager.getActualValue('custSubGrp') || "CROSS" == FormManager.getActualValue('custGrp') || FormManager.getActualValue('custSubGrp') == "INTER"
        || FormManager.getActualValue('custSubGrp') == "INFSL" || FormManager.getActualValue('custSubGrp') == 'IBMEM') {
      console.log(">>> Removing CRN Mandatory Validation >>>");
      FormManager.getField('restrictInd').checked = true;
      FormManager.resetValidations('taxCd1');
      FormManager.readOnly('taxCd1');
      FormManager.setValue('taxCd1', '');
      FormManager.readOnly('restrictInd');
    } else {
      if (!dijit.byId('restrictInd').get('checked')) {
        var sourceSystId = FormManager.getActualValue('sourceSystId');
        // PayGo_check
        var qParams = {
          SYST_ID : sourceSystId
        };
        var paygoUser = cmr.query('PAYGO.CHECK.CRN', qParams);
        var countpaygo = paygoUser.ret1;
        if (!(Number(countpaygo) == 1 && role == 'PROCESSOR')) {
          console.log(">>> Adding CRN Mandatory Validation >>>");
          FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Company Registration Number' ], 'MAIN_CUST_TAB');
          FormManager.enable('taxCd1');
        } else {
          console.log(">>> CRN is non mandatory for PayGO Users>>>");
          FormManager.resetValidations('taxCd1');
        }
      } else {
        console.log(">>> Removing CRN Mandatory Validation >>>");
        FormManager.resetValidations('taxCd1');
        FormManager.readOnly('taxCd1');
        FormManager.setValue('taxCd1', '');
      }
      FormManager.enable('restrictInd');
    }
  } else if (reqType == 'U') {
    console.log(">>> Removing CRN Mandatory Validation >>>");
    FormManager.getField('restrictInd').checked = false;
    FormManager.resetValidations('taxCd1');
    FormManager.readOnly('restrictInd');
  }
}

function setISUCTCOnISIC() {
  console.log('setISUCTCOnISIC........')
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var isic = FormManager.getActualValue('isicCd');
  if (reqType != 'C') {
    return;
  }
  var isicList = new Set([ '7230', '7240', '7290', '7210', '7221', '7229', '7250', '7123', '9802' ]);
  if (!(custSubGrp == 'INTER' || custSubGrp == 'PRICU' || custSubGrp == 'BUSPR')) {
    if ('32' == isuCd && 'S' == clientTier && isicList.has(isic)) {
      FormManager.setValue('clientTier', 'N');
    } else if ('32' == isuCd && 'N' == clientTier && !isicList.has(isic)) {
      FormManager.setValue('clientTier', 'S');
    }
  }
}

function setCustClassCd() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isic = FormManager.getActualValue('isicCd');
  var isicList = new Set([ '7320', '9808', '8512', '9818', '8030', '8090', '8010', '8021', '8022', '7704', '8511', '8519', '7707', '9231' ]);
  if (custSubGrp == 'GOVRN' || custSubGrp == 'XGOVR') {
    if (isicList.has(isic)) {
      FormManager.setValue('custClass', '16');
    } else {
      FormManager.setValue('custClass', '12');
    }
  }
}

function autoSetSpecialTaxCdByScenario() {
  var reqType = FormManager.getActualValue('reqType');
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var _custType = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  var exceptionPostCd = [ 'GX', 'GY', 'JE' ];
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var postCdParams = {
    REQ_ID : _zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var postCdResult = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', postCdParams);
  postCd = postCdResult.ret1;
  if (postCd != null && postCd.length > 2) {
    postCd = postCd.substring(0, 2);
  }
  if (reqType == 'C' && (issuingCntry == SysLoc.UK || issuingCntry == SysLoc.IRELAND)) {
    if (_custType == '') {
      FormManager.setValue('specialTaxCd', '');
    } else if (_custType == 'INTER' || _custType == 'XINTR' || _custType == 'INFSL') {
      FormManager.setValue('specialTaxCd', 'XX');
      FormManager.readOnly('specialTaxCd');
    } else if (issuingCntry == SysLoc.UK
        && ((_custType == 'CROSS' || _custType == 'XBSPR' || _custType == 'XPRIC' || _custType == 'XGOVR') || (exceptionPostCd.includes(postCd) && custGrp == 'LOCAL' && _custType != 'INTER' && _custType != 'INFSL'))) {
      FormManager.setValue('specialTaxCd', '32');
      // FormManager.enable('specialTaxCd');
    } else if (issuingCntry == SysLoc.UK && _custType != 'INTER' && _custType != 'INFSL' && custGrp == 'LOCAL' && !exceptionPostCd.includes(postCd.substr(0, 2))) {
      FormManager.setValue('specialTaxCd', 'Bl');
    } else if (issuingCntry == SysLoc.IRELAND && _custType != 'INTER' && _custType != 'INFSL') {
      FormManager.setValue('specialTaxCd', 'Bl');
    }
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
        // FormManager.enable('specialTaxCd');
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
    } else {
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
    if (addrType == 'ZI01' || copyingToA) {
      // 1482148 - add Scotland and Northern Ireland Logic
      autoSetSboSrOnAddrSaveUK();
    }

    if (_postalCdUKHandler == null) {
      _postalCdUKHandler = dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
        autoSetSpecialTaxCdByScenario();
      });
    }
    if (_postalCdUKHandler && _postalCdUKHandler[0]) {
      _postalCdUKHandler[0].onChange();
    }

    if (_cityUKHandler && _cityUKHandler[0]) {
      _cityUKHandler[0].onChange();
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
  var reqId = FormManager.getActualValue('reqId');
  var copyTypes = document.getElementsByName('copyTypes');
  var copyingToA = false;
  if (copyTypes != null && copyTypes.length > 0) {
    copyTypes.forEach(function(input, i) {
      if ((input.value == 'ZI01') && input.checked) {
        copyingToA = true;
      }
    });
  }

  if (finalSave || force || copyingToA) {
    var count = null;
    if (addressTyp == 'ZI01' || copyingToA) {
      if (cmr.addressMode != 'updateAddress') {
        qParams = {
          REQ_ID : reqId,
        };
        // check if install-at already present
        var record = cmr.query('GETZI01VALRECORDS', qParams);
        count = record.ret1;
      }

      if (count != null && Number(count) == 0) {
        autoSetAbbrevLocUKIOnAddrChange();
      } else if ((cmr.addressMode == 'updateAddress') && (FormManager.getActualValue('addrSeq') == '00001' || copyingToA)) {
        autoSetAbbrevLocUKIOnAddrChange();
      }
    }
  }
}

function autoSetVAT() {

  if (PageManager.isReadOnly()) {
    return;
  }

  var _custType = FormManager.getActualValue('custSubGrp');
  if (!_custType || _custType == null) {
    _custType = '';
  }
  console.log(">>> Process autoSetVAT ...>> " + _custType);
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var vatInd = FormManager.getActualValue('vatInd');
  if (PageManager.isReadOnly()) {
    return;
  }

  if (reqType != 'C') {
    FormManager.removeValidator('vat', Validators.REQUIRED);
    return;
  }

  // if (_custType == 'SOFTL') {
  // FormManager.getField('vatExempt').checked = true;
  // FormManager.enable('vatExempt');
  // } else if (_custType == 'INTER') {
  // FormManager.getField('vatExempt').checked = true;
  // if (role == 'REQUESTER') {
  // FormManager.readOnly('vatExempt');
  // } else {
  // FormManager.enable('vatExempt');
  // }
  // } else if (_custType == 'PRICU' || _custType == 'XPRIC') {
  // FormManager.getField('vatExempt').checked = true;
  // FormManager.readOnly('vatExempt');
  // } else {
  // FormManager.enable('vatExempt')
  // }

  if (vatInd && dojo.string.trim(vatInd) == 'T') {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    FormManager.enable('vat');
    // FormManager.setValue('vatExempt', 'N');
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
}

function autoSetSBO(value, valueInDB) {
  var custGrp = FormManager.getActualValue('custSubGrp');
  if (PageManager.isReadOnly() || custGrp == 'BUSPR') {
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
    } else if (custSubGrp == 'COMME' || custSubGrp == 'IGF' || custSubGrp == 'XIGF' || custSubGrp == 'COMLC' || custSubGrp == 'COOEM' || custSubGrp == 'SOFTL' || custSubGrp == 'THDPT'
        || custSubGrp == 'CROSS' || custSubGrp == 'XGOVR' || custSubGrp == 'INFSL' || custSubGrp == 'DC') {
      FormManager.resetDropdownValues(FormManager.getField('salesBusOffCd'));
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
      setSrAndSboOnIsicUK();
    }

    if (isuCd == '32') {
      if (postCd != '' && isNorthernIrelandPostCd(postCd)) {
        setSrAndSboOnIsicUK();
      }
    }

    var clientTier = FormManager.getActualValue('clientTier');
    if (postCd != '' && isNorthernIrelandPostCd(postCd)) {
      if (isuCd == '34' && clientTier == 'Q') {
        setSrAndSboOnIsicUK();
      }
    }
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
    setSboValueBasedOnIsuCtcIE();
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

function setSrAndSboOnIsicUK() {
  console.log("setSrAndSboOnIsicUK.....")
  var reqType = FormManager.getActualValue('reqType');
  var isuCdValue = FormManager.getActualValue('isuCd');
  var isicCdValue = FormManager.getActualValue('isicCd');
  var tierValue = FormManager.getActualValue('clientTier');

  if (reqType != 'C') {
    return;
  }

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

    // if (!(isNorthernIrelandPostCd(postCd))) {
    if (isuCdValue == '34' && tierValue == 'Q') {
      var qParams = {
        ISU_CD : '%' + isuCdValue + '%',
        CLIENT_TIER : '%' + tierValue + '%',
        ISIC_CD : isicCdValue
      };
      var result = cmr.query('UK.GET.SBOSR_FOR_ISIC', qParams);
      if (Object.keys(result).length > 0 && result.ret1 && result.ret2) {
        var sbo = result.ret1;
        var salesRep = result.ret2;
        FormManager.setValue('salesBusOffCd', sbo);
        FormManager.setValue('repTeamMemberNo', salesRep);
        if (role == 'Requester' && reqType == 'C') {
          FormManager.readOnly('repTeamMemberNo');
          FormManager.readOnly('salesBusOffCd');
        }
      }
    } else if (isicCdValue != null) {
      if (isuCdValue == '34' && tierValue == 'Y') {
        FormManager.setValue('salesBusOffCd', '015');
        FormManager.setValue('repTeamMemberNo', 'SPA015');
      }
    }
  }
  // }
}

function autoSetSboSrOnAddrSaveUK() {
  console.log('autoSetSboSrOnAddrSaveUK.......')
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
    var postCd = FormManager.getActualValue('postCd');
    if (postCd != null && postCd.length > 2) {
      postCd = postCd.substring(0, 2);
    }
    var isuCd = FormManager.getActualValue('isuCd');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var role = null;
    if (typeof (_pagemodel) != 'undefined') {
      role = _pagemodel.userRole;
    }

    if (custSubGrp == 'COMME' || custSubGrp == 'IGF' || custSubGrp == 'XIGF' || custSubGrp == 'COMLC' || custSubGrp == 'COOEM' || custSubGrp == 'SOFTL' || custSubGrp == 'THDPT'
        || custSubGrp == 'CROSS' || custSubGrp == 'XGOVR' || custSubGrp == 'INFSL' || custSubGrp == 'DC') {
      if (role == 'Processor') {
        FormManager.enable('salesBusOffCd');
        FormManager.enable('repTeamMemberNo');
      }
      FormManager.resetDropdownValues(FormManager.getField('salesBusOffCd'));
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
      setSrAndSboOnIsicUK();
    }

    var clientTier = FormManager.getActualValue('clientTier');
    if (postCd != '' && isNorthernIrelandPostCd(postCd)) {
      if (isuCd == '34' && clientTier == 'Q') {
        setSrAndSboOnIsicUK();
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
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);

  } else if (role == 'Processor') {
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
            if (!internalDept.match("[0-9A-Za-z]{6}")) {
              cmr.showNode('deptInfo');
              return new ValidationResult(null, false, 'Internal Department Number should have 6 digits/alphabets.');
            } else {
              cmr.hideNode('deptInfo');
              return new ValidationResult(null, true);
            }
          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function autoSetAbbrevLocUKIOnAddrChange() {
  console.log('Inside autoSetAbbrevLocUKIOnAddrChange');
  var _abbrevLocn = null;
  var issuCntry = FormManager.getActualValue('cmrIssuingCntry');
  if ((FormManager.getActualValue('landCntry') == 'GB' && issuCntry == '866') || (FormManager.getActualValue('landCntry') == 'IE' && issuCntry == '754')) {
    _abbrevLocn = FormManager.getActualValue('postCd')
    if ((_abbrevLocn == null || _abbrevLocn == '') && issuCntry == '754') {
      _abbrevLocn = FormManager.getActualValue('city1')
    }

  } else {
    var arr = document.getElementById('landCntry').value.split("-")
    _abbrevLocn = arr[0];
  }

  if (_abbrevLocn != null && _abbrevLocn.length > 12) {
    _abbrevLocn = _abbrevLocn.substr(0, 12);
  }
  FormManager.setValue('abbrevLocn', _abbrevLocn);
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
    // FormManager.addValidator('ibmDeptCostCenter', Validators.NUMBER, [
    // 'Department number' ], 'MAIN_IBM_TAB');
  } else if (custSubGrp != 'INTER' && custSubGrp != 'XINTR') {
    // FormManager.clearValue('ibmDeptCostCenter');
    console.log(">>custSubGrp >>" + custSubGrp);
    console.log(">>HIDING ibmDeptCostCenter >>");
    FormManager.resetValidations('ibmDeptCostCenter');
    FormManager.hide('InternalDept', 'ibmDeptCostCenter');
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

function addPostalCodeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postal_cd = FormManager.getActualValue('postCd');
        /*
         * DENNIS Defect 1830120: PP: Getting 'Cannot get postal code error' for
         * postal codes for UK
         */
        // if (FormManager.getActualValue('landCntry')
        // != 'GB') {
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
    var ctc = FormManager.getActualValue('clientTier');
    var custSubGrp = FormManager.getActualValue('custSubGrp');

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
  });
  if (_sboHandler && _sboHandler[0]) {
    _sboHandler[0].onChange();
  }
}

var _addrTypesIL = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'CTYA', 'CTYB', 'CTYC' ];
var _addrTypeHandler = [];

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
      setSalesBoSboIbo();
    }
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
    var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var custSubType = FormManager.getActualValue('custSubGrp');
    if (cmrIssuingCntry == '758'
        && (custSubType == 'COMME' || custSubType == 'GOVST' || custSubType == 'LOCEN' || custSubType == 'NGOIT' || custSubType == 'PRICU' || custSubType == '3PAIT' || custSubType == 'UNIVE')) {
      return;
    }
    var result = cmr.query('DATA.GET.SALES_BO_CD', qParams);
    var salesBoCd = result.ret1;
    var selsr = result.ret2;
    FormManager.setValue('salesBusOffCd', salesBoCd);
    FormManager.setValue('salesTeamCd', selsr);
  } else {
    FormManager.setValue('salesBusOffCd', '');
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

var oldIsuCdValueUK = null;
function setSboValueBasedOnIsuCtcUK(value) {
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var isuList = [ '34', '36', '32' ];
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (reqType == 'U') {
    return;
  }

  if ((value != false || value == undefined || value == null) && isuCd.concat(clientTier) != '' && oldIsuCdValueUK == null) {
    oldIsuCdValueUK = isuCd + clientTier;
  }

  if (oldIsuCdValueUK == null) {
    return;
  }

  if (isuCd == '32' && clientTier == 'T') {
    FormManager.setValue('salesBusOffCd', '123');
    FormManager.setValue('repTeamMemberNo', 'SPA123');
  } else if (isuCd == '5K' && clientTier == '') {
    FormManager.setValue('salesBusOffCd', '000');
    FormManager.setValue('repTeamMemberNo', 'SPA000');
  } else if (isuCd == '05' && clientTier == '') {
    FormManager.setValue('salesBusOffCd', '253');
    FormManager.setValue('repTeamMemberNo', '202886');
  } else if (isuCd == '11' && clientTier == '') {
    FormManager.setValue('salesBusOffCd', '111');
    FormManager.setValue('repTeamMemberNo', 'SPA111');
  } else if (isuCd == '34' && clientTier == 'Q') {
    setSrAndSboOnIsicUK();
  } else if (isuCd == '21' && clientTier == '') {
    if (custSubGrp == 'INTER' || custSubGrp == 'IBMEM') {
      FormManager.setValue('salesBusOffCd', '000');
      FormManager.setValue('repTeamMemberNo', 'SPA000');
    } else if (custSubGrp == 'BUSPR') {
      FormManager.setValue('salesBusOffCd', '114');
      FormManager.setValue('repTeamMemberNo', 'SPA114');
    }
  } else {
    FormManager.setValue('salesBusOffCd', '');
    FormManager.setValue('repTeamMemberNo', '');
  }
}

var oldIsuCdValueIE = null;
function setSboValueBasedOnIsuCtcIE(value) {
  var reqType = FormManager.getActualValue('reqType');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuList = [ '34', '36', '32' ];

  if (reqType == 'U') {
    return;
  }
  if ((value != false || value == undefined || value == null) && clientTier != '' && isuCd != '' && oldIsuCdValueIE == null) {
    oldIsuCdValueIE = isuCd + clientTier;
  }

  if (oldIsuCdValueIE == null) {
    return;
  }

  if (isuCd == '5K' && clientTier == '') {
    FormManager.setValue('salesBusOffCd', '000');
    FormManager.setValue('repTeamMemberNo', 'SPA000');
  } else if (isuCd == '32' && clientTier == 'T') {
    FormManager.setValue('salesBusOffCd', '123');
    FormManager.setValue('repTeamMemberNo', 'SPA123');
  } else if (isuCd == '34' && clientTier == 'Q' && getZS01LandCntry() == 'GB' && scenario == 'CROSS') {
    FormManager.setValue('salesBusOffCd', '057');
    FormManager.setValue('repTeamMemberNo', 'SPA057');
  } else if (isuCd == '34' && clientTier == 'Q') {
    FormManager.setValue('salesBusOffCd', '090');
    FormManager.setValue('repTeamMemberNo', 'MMIR11');
  } else if (isuCd == '21' && clientTier == '') {
    if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER' || custSubGrp == 'IBMEM') {
      FormManager.setValue('salesBusOffCd', '000');
      FormManager.setValue('repTeamMemberNo', 'SPA000');
    } else if (custSubGrp == 'PRICU') {
      FormManager.setValue('salesBusOffCd', '090');
      FormManager.setValue('repTeamMemberNo', 'MMIR11');
    }
  } else {
    FormManager.setValue('salesBusOffCd', '');
    FormManager.setValue('repTeamMemberNo', '');
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

/**
 * CMR-2093:Turkey - show CoF field for Update request only
 */
function showCommercialFinanced() {
  if (FormManager.getActualValue('reqType') != 'U' || FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
    FormManager.clearValue('commercialFinanced');
    FormManager.hide('CommercialFinanced', 'commercialFinanced');
  } else {
    FormManager.show('CommercialFinanced', 'commercialFinanced');
    FormManager.limitDropdownValues(FormManager.getField('commercialFinanced'), [ 'R', 'S', 'T' ]);
    FormManager.show('CustClass', 'custClass');
    var role = FormManager.getActualValue('userRole').toUpperCase();
  }
}

function addPhoneValidatorEMEA() {
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
}

function addPOBOXValidatorEMEA() {
  FormManager.addValidator('poBox', Validators.DIGIT, [ 'PO Box' ]);
}

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

function addSMAndVAPostalCDValidator() {
  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry != 'undefined' && 'SM' == landCntry) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code for San Marino - SM' ], 'MAIN_NAME_TAB');
  }
  if (landCntry != 'undefined' && 'VA' == landCntry) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code for Holy See (Vatican City State) - VA' ], 'MAIN_NAME_TAB');
  }
}

function doValidateFiscalDataModal() {
  cmr.setModalTitle('validateFiscalDataModal', 'Process Fiscal Data');
  var vat = FormManager.getActualValue('vat');
  var fiscalCd = FormManager.getActualValue('taxCd1');
  if (!vat && !fiscalCd) {
    FormManager.setValue('fiscalDataStatus', 'F');
    cmr.showAlert("No company addresses with this fiscal data found.", null, null, true);
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
      cmr.showAlert("No company addresses with this fiscal data found.", null, null, true);
    }
  }
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

function validateVATForINFSLScenarioUKI() {

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var vat = FormManager.getActualValue('vat');
        var country = "";
        if (SysLoc.IRELAND == FormManager.getActualValue('cmrIssuingCntry')) {
          country = "IE";
        } else if (SysLoc.UK == FormManager.getActualValue('cmrIssuingCntry')) {
          country = "GB";
        }

        if (custSubGrp == 'INFSL' && country != '') {
          if (vat == '') {
            return new ValidationResult(null, true);
          } else {
            var vatRet = cmr.validateVATUsingVies(country, vat);
            if (!vatRet.success) {
              return new ValidationResult({
                id : 'vat',
                type : 'text',
                name : 'vat'
              }, false, vatRet.errorMessage);
            } else {
              return new ValidationResult(null, true);
            }
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function lockCustClassUKI() {
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  FormManager.resetValidations('custClass');
  if (custSubType == 'BUSPR') {
    FormManager.enable('custClass');
    cmr.showNode('info');
  } else {
    cmr.hideNode('info');
  }

}

function addCustClassValidatorBP() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var custClass = FormManager.getActualValue('custClass');
        var custClassList = new Set([ '43', '45', '46', '49' ]);
        if (custSubType == 'BUSPR' && !custClassList.has(custClass)) {
          return new ValidationResult(null, false, 'Enter valid customer classification code.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
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

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqId = FormManager.getActualValue('reqId');
        var mailingCount = 0;
        var billingCount = 0;
        var addrType = "";
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && FormManager.getActualValue('custSubGrp') == 'INTER') {
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (recordList == null && _allAddressData != null && _allAddressData[i] != null) {
              recordList = _allAddressData[i];
            }
            addrType = recordList.addrType;

            if (typeof (addrType) == 'object') {
              addrType = addrType[0];
              if (addrType == 'ZS01') {
                billingCount++;
              }
              if (addrType == 'ZP01') {
                mailingCount++
              }
            }
          }

          var billNm = "";
          var mailNm = "";
          if (billingCount > 0) {
            // get billing name from db
            var res_billNm = cmr.query('GET.CUSTNM_ADDR', {
              REQ_ID : reqId,
              ADDR_TYPE : 'ZS01'
            });
            if (res_billNm.ret1 != undefined) {
              billNm = res_billNm.ret1 + res_billNm.ret2;
            }
          }
          if (mailingCount > 0) {
            // get billing name from db
            var res_mailNm = cmr.query('GET.CUSTNM_ADDR', {
              REQ_ID : reqId,
              ADDR_TYPE : 'ZP01'
            });
            if (res_mailNm.ret1 != undefined) {
              mailNm = res_mailNm.ret1 + res_mailNm.ret2;
            }
          }

          if ((!mailNm.includes('IBM') && mailingCount > 0) || (!billNm.includes('IBM') && billingCount > 0)) {
            return new ValidationResult(null, false, 'Mailing / Billing  does not contain IBM in their customer name.');
          } else {
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addSBOSRLogicIE(clientTier) {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var _isuCd = FormManager.getActualValue('isuCd');
  var _clientTier = FormManager.getActualValue('clientTier');

  var salesRepValue = [];
  var sboValues = [];
  if (_isuCd != '') {
    var isuCtc = _isuCd + _clientTier;
    var qParams = null;
    var results = null;

    qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU_CD : '%' + _isuCd + _clientTier + '%',
    };
    var results = cmr.query('GET.SALESREP.IRELAND', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        salesRepValue.push(results[i].ret1);
        sboValues.push(results[i].ret2);
      }
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesRepValue);
      FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), sboValues);
      if (salesRepValue != null) {
        if (salesRepValue.length == 1) {
          FormManager.setValue('repTeamMemberNo', salesRepValue[0]);
        }
      }
      if (sboValues != null) {
        if (sboValues.length >= 1) {
          FormManager.setValue('salesBusOffCd', sboValues[0]);
        }
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
        var role = FormManager.getActualValue('userRole').toUpperCase();

        if (role == 'REQUESTER' && reqType == 'C') {
          return new ValidationResult(null, true);
        }

        if (embargoCd != '' && embargoCd.length > 0) {
          embargoCd = embargoCd.trim();
          if ((embargoCd != '' && embargoCd.length == 1) && (embargoCd == 'E' || embargoCd == 'C')) {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult({
              id : 'embargoCd',
              type : 'text',
              name : 'embargoCd'
            }, false, 'Embargo Code value should be only E,C or Blank.');
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
      if (postCd != '' && (isNorthernIrelandPostCd(postCd))) {
        FormManager.setValue('isuCd', "34");
        FormManager.setValue('clientTier', "Q");
      } else {
        FormManager.setValue('clientTier', "Q");
      }
    }
  } else {
    FormManager.setValue('isuCd', "21");
    FormManager.setValue('clientTier', '');
  }
}

function disableAddrFieldsUKI() {
  var addrType = FormManager.getActualValue('addrType');
  var custGrp = FormManager.getActualValue('custGrp');
  if (addrType == 'ZS01' || addrType == 'ZD01' || addrType == '') {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  }

  if (addrType == 'ZP01' || addrType == 'ZS01' || addrType == '') {
    FormManager.enable('poBox');
  } else {
    FormManager.setValue('poBox', '');
    FormManager.disable('poBox');
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
  var reqType = FormManager.getActualValue('reqType');
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

function validateSalesRepForUKI() {
  // validate SalesRep for ITALY
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var salesRep = FormManager.getActualValue('repTeamMemberNo');
        if (salesRep.length != 6 && salesRep != undefined && salesRep != '') {
          return new ValidationResult({
            id : 'repTeamMemberNo',
            type : 'text',
            name : 'repTeamMemberNo'
          }, false, 'The Sales Rep should be six characters long');
        }
        if (salesRep != undefined && salesRep != '' && !salesRep.match("^[0-9a-zA-Z]*$")) {
          return new ValidationResult({
            id : 'repTeamMemberNo',
            type : 'text',
            name : 'repTeamMemberNo'
          }, false, 'The Sales Rep should consist of digits and alphabets only');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateSboSrForIsuCtcUK() {
  console.log(">>>> validateSboSrForIsuCtc");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var isu = FormManager.getActualValue('isuCd');
        var ctc = FormManager.getActualValue('clientTier');
        var isuCTC = isu.concat(ctc);
        var sbo = FormManager.getActualValue('salesBusOffCd').toUpperCase();
        var salRep = FormManager.getActualValue('repTeamMemberNo');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var reqType = FormManager.getActualValue('reqType');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var isSboNull = sbo == null || sbo == '' || sbo == undefined;
        var isSrNull = salRep == null || salRep == '' || salRep == undefined;
        var isSubGrpNull = custSubGrp == null || custSubGrp == '' || custSubGrp == undefined;
        if (isSubGrpNull || reqType != 'C') {
          return new ValidationResult(null, true);
        }

        var isuCtcList = [ '3T', '04', '05', '5K', '11', '12', '15', '18', '21', '28', '32T', '34Q', '36Y' ];

        if (!isuCtcList.includes(isuCTC) || isSboNull || isSrNull) {
          return new ValidationResult(null, true);
        }
        var results = fetchSboSrForIsuCtcUK(cntry, sbo, salRep, isu, ctc);
        var displayInvalidMsg = true;

        if (results != null && results.length > 0) {
          displayInvalidMsg = false;
        }

        if (displayInvalidMsg) {
          return new ValidationResult({
            id : 'salesBusOffCd',
            type : 'text',
            name : 'salesBusOffCd'
          }, false, 'Please enter the valid combination of ISU, ClientTier, SBO and Sales Rep.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateSboSrForIsuCtcIE() {
  console.log(">>>> validateSboSrForIsuCtcIE");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var isu = FormManager.getActualValue('isuCd');
        var ctc = FormManager.getActualValue('clientTier');
        var isuCTC = isu.concat(ctc);
        var sbo = FormManager.getActualValue('salesBusOffCd').toUpperCase();
        var salRep = FormManager.getActualValue('repTeamMemberNo');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var scenario = FormManager.getActualValue('custGrp');
        var reqType = FormManager.getActualValue('reqType');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var zs01LandCntry = getZS01LandCntry();
        var isSboNull = sbo == null || sbo == '' || sbo == undefined;
        var isSrNull = salRep == null || salRep == '' || salRep == undefined;
        var isSubGrpNull = custSubGrp == null || custSubGrp == '' || custSubGrp == undefined;
        if (isSubGrpNull || reqType != 'C') {
          return new ValidationResult(null, true);
        }
        var isuCtcList = [ '5K', '21', '32T', '34Q', '36Y' ];
        if (!isuCtcList.includes(isuCTC) || isSboNull || isSrNull) {
          return new ValidationResult(null, true);
        }

        if (isuCTC == '34Q') {
          if (zs01LandCntry == 'GB' && scenario == 'CROSS' && (sbo != '057' || salRep != 'SPA057')) {
            return new ValidationResult({
              id : 'salesBusOffCd',
              type : 'text',
              name : 'salesBusOffCd'
            }, false, 'Please enter the valid combination of ISU, ClientTier, SBO and Sales Rep.');
          } else if (zs01LandCntry != 'GB' && (sbo != '090' || salRep != 'MMIR11')) {
            return new ValidationResult({
              id : 'salesBusOffCd',
              type : 'text',
              name : 'salesBusOffCd'
            }, false, 'Please enter the valid combination of ISU, ClientTier, SBO and Sales Rep.');
          }
        } else {
          var results = fetchSboSrForIsuCtcIE(cntry, sbo, salRep, isuCTC);
          var displayInvalidMsg = true;

          if (results != null && results.length > 0) {
            displayInvalidMsg = false;
          }

          if (displayInvalidMsg) {
            return new ValidationResult({
              id : 'salesBusOffCd',
              type : 'text',
              name : 'salesBusOffCd'
            }, false, 'Please enter the valid combination of ISU, ClientTier, SBO and Sales Rep.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function fetchSboSrForIsuCtcIE(cntry, sbo, salRep, isuCTC) {
  console.log(">>>> fetchSboSrForIsuCtcIE");
  var results = null;
  var qParams = {
    _qall : 'Y',
    CNTRY : cntry,
    SBO : sbo,
    SALES_REP : salRep,
    ISU : '%' + isuCTC + '%'
  };
  results = cmr.query('IE.GET.SBOSR_FOR_ISU_CTC', qParams);
  return results;
}

function fetchSboSrForIsuCtcUK(cntry, sbo, salRep, isu, ctc) {
  console.log(">>>> fetchSboSrForIsuCtcUK");
  var results = null;
  var qParams = {
    _qall : 'Y',
    CNTRY : cntry,
    SBO : sbo,
    SALES_REP : salRep,
    ISU : isu,
    CTC : ctc
  };
  results = cmr.query('UK.GET.SBOSR_FOR_ISU_CTC', qParams);
  return results;
}

function addStPoValidatorUKI() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var strCont = FormManager.getActualValue('addrTxt2');
        var poBox = FormManager.getActualValue('poBox');
        var addrType = FormManager.getActualValue('addrType');
        var landCntry = FormManager.getActualValue('landCntry');
        var issuCntry = FormManager.getActualValue('cmrIssuingCntry');
        var attn = FormManager.getActualValue('dept');
        var custPh = FormManager.getActualValue('custPhone');

        if ((landCntry != 'GB' && issuCntry == '866') || (landCntry != 'IE' && issuCntry == '754')) {
          if (addrType == 'ZS01' || addrType == 'ZP01') {
            if ((strCont.length != undefined && strCont.length > 0) && (poBox.length != undefined && poBox.length > 0)) {
              return new ValidationResult(null, false, 'Only \'Street Cont\' or \'PO Box\' can be filled.');
            }
          }
        }
        if (addrType == 'ZD01') {
          if ((attn.length != undefined && attn.length > 0) && (custPh.length != undefined && custPh.length > 0)) {
            var attnCustPh = 'ATT ' + attn + (attn.length > 0 ? ', ' : '') + custPh;
            if (attnCustPh.length > 30) {
              return new ValidationResult(null, false, 'Computed length of \'Attn\'+\'Phone No.\' should be 24 characters or less');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addStAttPoValidatorForUKI() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var strCont = FormManager.getActualValue('addrTxt2');
        var poBox = FormManager.getActualValue('poBox');
        var attn = FormManager.getActualValue('dept');
        var addrType = FormManager.getActualValue('addrType');
        var custPh = FormManager.getActualValue('custPhone');
        var custGrp = FormManager.getActualValue('custGrp');
        var custCont = FormManager.getActualValue('custNm2');
        var landCntry = FormManager.getActualValue('landCntry');
        var issuCntry = FormManager.getActualValue('cmrIssuingCntry');

        if (reqType == 'C') {
          if ((landCntry != 'GB' && issuCntry == '866') || (landCntry != 'IE' && issuCntry == '754')) {
            if (addrType == 'ZS01' || addrType == 'ZP01') {
              if ((custCont.length != undefined && custCont.length > 0) && (attn.length != undefined && attn.length > 0)) {
                return new ValidationResult(null, false, 'Only \'Customer Name Cont\' or \'Attn\' can be filled.');
              }
            } else if (addrType == 'ZD01') {
              if ((custCont.length != undefined && custCont.length > 0) && ((attn.length != undefined && attn.length > 0) || (custPh.length != undefined && custPh.length > 0))) {
                return new ValidationResult(null, false, 'Only Customer Name Con\'t or Attn+Phone No/Attn/Phone No can be filled.');
              }
            } else if (addrType == 'ZI01' || addrType == 'ZS02') {
              if ((custCont.length != undefined && custCont.length > 0) && (attn.length != undefined && attn.length > 0)) {
                return new ValidationResult(null, false, 'Only \'Customer Name Cont\' or \'Attn\' can be filled.');
              }
            }

          } else {
            if (addrType == 'ZS01' || addrType == 'ZP01') {
              if (((strCont.length != undefined && strCont.length > 0) && (poBox.length != undefined && poBox.length > 0))
                  || ((attn.length != undefined && attn.length > 0) && (poBox.length != undefined && poBox.length > 0))
                  || ((attn.length != undefined && attn.length > 0) && (strCont.length != undefined && strCont.length > 0))
                  || ((attn.length != undefined && attn.length > 0) && (strCont.length != undefined && strCont.length > 0) && (poBox.length != undefined && poBox.length > 0))) {

                return new ValidationResult(null, false, 'For Street Con\'t, PO BOX and Attn only one of the three can be filled.');
              }
            } else if (addrType == 'ZD01') {
              if ((strCont.length != undefined && strCont.length > 0) && ((attn.length != undefined && attn.length > 0) || (custPh.length != undefined && custPh.length > 0))) {
                return new ValidationResult(null, false, 'Only Street Con\'t or Attn+Phone No/Attn/Phone No can be filled.');
              }
            } else if (addrType == 'ZI01' || addrType == 'ZS02') {
              if ((strCont.length != undefined && strCont.length > 0) && (attn.length != undefined && attn.length > 0)) {
                return new ValidationResult(null, false, 'Only \'Street Cont\' or \'Attn\' can be filled.');
              }
            }

          }
        } else if (reqType == 'U') {
          if (addrType == 'ZS01' || addrType == 'ZP01') {
            if ((landCntry != 'GB' && issuCntry == '866') || (landCntry != 'IE' && issuCntry == '754')) {
              if ((custCont.length != undefined && custCont.length > 0) && (attn.length != undefined && attn.length > 0)) {
                return new ValidationResult(null, false, 'Only \'Customer Name Cont\' or \'Attn\' can be filled.');
              }
            } else {
              if (((strCont.length != undefined && strCont.length > 0) && (poBox.length != undefined && poBox.length > 0))
                  || ((attn.length != undefined && attn.length > 0) && (poBox.length != undefined && poBox.length > 0))
                  || ((attn.length != undefined && attn.length > 0) && (strCont.length != undefined && strCont.length > 0))
                  || ((attn.length != undefined && attn.length > 0) && (strCont.length != undefined && strCont.length > 0) && (poBox.length != undefined && poBox.length > 0))) {

                return new ValidationResult(null, false, 'For Street Con\'t, PO BOX and Attn only one of the three can be filled.');
              }
            }
          } else if (addrType == 'ZD01') {
            if ((landCntry != 'GB' && issuCntry == '866') || (landCntry != 'IE' && issuCntry == '754')) {
              if ((custCont.length != undefined && custCont.length > 0) && ((attn.length != undefined && attn.length > 0) || (custPh.length != undefined && custPh.length > 0))) {
                return new ValidationResult(null, false, 'Only Customer Name Con\'t or Attn+Phone No/Attn/Phone No can be filled.');
              }
            } else {
              if ((strCont.length != undefined && strCont.length > 0) && ((attn.length != undefined && attn.length > 0) || (custPh.length != undefined && custPh.length > 0))) {
                return new ValidationResult(null, false, 'Only Street Con\'t or Attn+Phone No/Attn/Phone No can be filled.');
              }
            }
          } else if (addrType == 'ZI01' || addrType == 'ZS02') {
            if ((landCntry != 'GB' && issuCntry == '866') || (landCntry != 'IE' && issuCntry == '754')) {
              if ((custCont.length != undefined && custCont.length > 0) && (attn.length != undefined && attn.length > 0)) {
                return new ValidationResult(null, false, 'Only \'Customer Name Cont\' or \'Attn\' can be filled.');
              }
            } else {
              if ((strCont.length != undefined && strCont.length > 0) && (attn.length != undefined && attn.length > 0)) {
                return new ValidationResult(null, false, 'Only \'Street Cont\' or \'Attn\' can be filled.');
              }
            }
          }
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addUKIaddressContentValidator() {
  console.log("addUKAddressAddressContentValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '866' || FormManager.getActualValue('cmrIssuingCntry') != '754') {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var custGrp = FormManager.getActualValue('custGrp');
          var landCntry = FormManager.getActualValue('landCntry');
          var reqType = FormManager.getActualValue('reqType');
          var record = null;
          var type = null;
          var strCont = null;
          var poBox = null;
          var attn = null;
          var addrType = null;
          var custPh = null;
          var custCont = null;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }

            strCont = record.addrTxt2[0];
            poBox = record.poBox[0];
            attn = record.dept[0];
            addrType = record.addrType[0];
            custPh = record.custPhone[0];
            custCont = record.custNm2[0];

            if (reqType == 'C') {
              if (custGrp == 'CROSS') {
                if (addrType == 'ZS01' || addrType == 'ZP01') {
                  if (custCont != null && attn != null) {
                    if (custCont.length > 0 && attn.length > 0)
                      return new ValidationResult(null, false, 'Billing/Mailing Address: Only \'Customer Name Cont\' or \'Attn\' can be filled.');
                  }
                  if (strCont != null && poBox.length != null) {
                    if (strCont.length > 0 && poBox.length > 0)
                      return new ValidationResult(null, false, 'Billing/Mailing Address: Only \'Street Cont\' or \'PO Box\' can be filled.');
                  }
                } else if (addrType == 'ZD01') {
                  if (custCont != null && (attn != null || custPh != null)) {
                    if (custCont.length > 0 && (attn.length > 0 || custPh.length > 0))
                      return new ValidationResult(null, false, 'Shipping Address: Only \'Customer Name Cont\' or (\'Attn\'+\'Phone No.\') can be filled.');
                  }
                } else if (addrType == 'ZI01' || addrType == 'ZS02') {
                  if (attn != null && custCont != null) {
                    if (custCont.length > 0 && attn.length > 0)
                      return new ValidationResult(null, false, 'Installing/EPL Address: Only \'Street Cont\' or \'Attn\' can be filled.');
                  }
                }

              } else if (custGrp == 'LOCAL') {
                if (addrType == 'ZS01' || addrType == 'ZP01') {
                  if (strCont == null)
                    strCont = '';
                  if (attn == null)
                    attn = '';
                  if (poBox == null)
                    poBox = '';
                  if ((strCont.length > 0 && poBox.length > 0) || (attn.length > 0 && poBox.length > 0) || (strCont.length > 0 && attn.length > 0)
                      || (strCont.length > 0 && poBox.length > 0 && attn.length > 0)) {
                    return new ValidationResult(null, false, 'Billing/Mailing Address: For Street Con\'t, PO BOX and Attn only one of the three can be filled.');
                  }
                } else if (addrType == 'ZD01') {
                  if (strCont != null && (attn != null || custPh != null)) {
                    if (strCont.length > 0 && (attn.length > 0 || custPh.length > 0))
                      return new ValidationResult(null, false, 'Shipping Address: Only Street Con\'t or Attn+Phone No/Attn/Phone No can be filled.');
                  }
                } else if (addrType == 'ZI01' || addrType == 'ZS02') {
                  if (strCont != null && attn != null) {
                    if (strCont.length > 0 && attn.length > 0)
                      return new ValidationResult(null, false, 'Installing/EPL Address: Only \'Street Cont\' or \'Attn\' can be filled.');
                  }
                }
              }
            }
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function autoSetAbbrNameUKI() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var billingCustNm = '';
  var installingCustNm = '';
  var reqId = FormManager.getActualValue('reqId');
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var abbName = FormManager.getActualValue('abbrevNm');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  // CMR - 5063
  if (custSubGrp == 'THDPT') {
    var result = cmr.query('GET.CUSTNM1_ADDR_UKI', {
      REQ_ID : reqId,
      ADDR_TYPE : 'ZI01'
    });
    if (result.ret1 != undefined) {
      installingCustNm = result.ret1 + (result.ret2 != undefined ? result.ret2 : '');
      if (installingCustNm.match('^VR[0-9]{3}')) {
        FormManager.setValue('abbrevNm', installingCustNm.substring(0, 22));
      } else {
        var result2 = cmr.query('GET.CUSTNM1_ADDR_UKI', {
          REQ_ID : reqId,
          ADDR_TYPE : 'ZS01'
        });
        if (result2.ret1 != undefined) {
          billingCustNm = result2.ret1 + (result2.ret2 != undefined ? result2.ret2 : '');
        }
        billingCustNm = result2.ret1 != undefined ? result2.ret1 : '';
        if (billingCustNm != '') {
          var offset = 0;
          if (installingCustNm.length < 11) {
            offset = 11 - installingCustNm.length;
          }
          var installingCustNmTrim = installingCustNm.length > 11 ? installingCustNm.substring(0, 11) : installingCustNm;
          var billingCustNmTrim = billingCustNm.length > (offset + 8) ? billingCustNm.substring(0, offset + 8) : billingCustNm;
          FormManager.setValue('abbrevNm', (installingCustNmTrim + ' / ' + billingCustNmTrim));
        }
      }
    }
  } else if (('INFSL' == custSubGrp) && (SysLoc.IRELAND == cmrCntry || SysLoc.UK == cmrCntry)) {
    var qParams = {
      REQ_ID : FormManager.getActualValue('reqId'),
    };

    var result = cmr.query('UKI.GET_ABBNAME_DATA', qParams);
    if (result.ret1 != undefined && result.ret1 != '') {
      FormManager.setValue('abbrevNm', result.ret1);
    }
  } else if (('INTER' == custSubGrp)) {
    // Defect-6793
    autoSetAbbrevNmFrmDept();
  } else {
    if (role == "PROCESSOR") {
      var result = cmr.query('UKI.GET_ABBNAME_DATA', {
        REQ_ID : reqId
      });
    } else if (role == "REQUESTER") {
      var result = cmr.query('GET.CUSTNM1_ADDR_UKI', {
        REQ_ID : reqId,
        ADDR_TYPE : 'ZS01'
      });
    }
    if ((result != null || result != undefined) && result.ret1 != undefined) {
      {
        var _abbrevNmValue = result.ret1 + (result.ret2 != undefined ? result.ret2 : '');
        if (_abbrevNmValue != null && _abbrevNmValue.length > 22) {
          _abbrevNmValue = _abbrevNmValue.substr(0, 22);
        }
        FormManager.setValue('abbrevNm', _abbrevNmValue);
        FormManager.readOnly('abbrevNm');
      }
    }
  }
}

function autoSetUIFieldsOnScnrioUKI() {
  var reqType = FormManager.getActualValue('reqType');
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (custSubGrp == 'INTER') {
    FormManager.setValue('company', '');
    FormManager.readOnly('company');
    FormManager.setValue('collectionCd', '88');
    FormManager.readOnly('collectionCd');
  } else if (custSubGrp == 'INFSL') {
    FormManager.readOnly('collectionCd');
    FormManager.setValue('collectionCd', '69');
    FormManager.readOnly('custClass');
    FormManager.setValue('custClass', '33');
  }

  if (issuingCntry == '754') {
    if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER') {
      FormManager.setValue('clientTier', '');
    }
  }

}

function requestingLOBCheckFrIFSL() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (requestType != 'C' || custSubGrp != 'INFSL') {
          return new ValidationResult(null, true);
        }
        var reqLob = FormManager.getActualValue('requestingLob');
        var reqLobsAllowed = [ 'IGF', 'SCT' ];
        if (!reqLobsAllowed.includes(reqLob)) {
          return new ValidationResult(null, false, 'Requests with Internal/FSL Scenario should have Requesting LOB as IGF / Client ID/SC&T.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custGrp = FormManager.getActualValue('custGrp');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var reqLob = FormManager.getActualValue('requestingLob');
        if (cntry == SysLoc.UK || cntry == SysLoc.IRELAND) {
          if (custGrp == 'LOCAL' && custSubGrp == 'DC') {
            if (reqLob != 'TSS') {
              return new ValidationResult(null, false, 'Requests with Datacentre Scenario should have Requesting LOB as TSS. ');
            }
          }
        } else {
          return new ValidationResult(null, true, 'Allow request');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function addValidatorForCollectionCdUpdateUKI() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var reqId = FormManager.getActualValue('reqId');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        if (reqType == 'U' && role == 'REQUESTER') {
          var requestingLOB = FormManager.getActualValue('requestingLob');
          var collectionCd = FormManager.getActualValue('collectionCd');
          var result = cmr.query('GETDATARDCVALUESIT', {
            REQ_ID : reqId
          });
          var collCdOld = null;
          if (result && result.ret1 != null) {
            collCdOld = result.ret1;
          }
          if (collCdOld != null && collCdOld != collectionCd && requestingLOB != 'AR') {
            return new ValidationResult({
              id : 'requestingLob',
              name : 'Requesting LOB'
            }, false, 'Requesting LOB should be \'Accounts Receivable\' only, if Collection Code is updated.');
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

function validateCollectionCdValueUKI() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var collectionCd = FormManager.getActualValue('collectionCd');
        if (collectionCd == null || collectionCd == '') {
          return;
        }

        var reqId = FormManager.getActualValue('reqId');
        var result = cmr.query('GETDATARDCVALUESIT', {
          REQ_ID : reqId
        });

        var collCdOld = null;
        if (result && result.ret1 != null) {
          collCdOld = result.ret1;
        }

        if (FormManager.getActualValue('reqType') == 'U' && collCdOld == collectionCd) {
          return;
        }

        var collectionCdValid = true;
        if (collectionCd.length == 2 || collectionCd.length == 6) {
          var alphaNumeric = /^[0-9A-Z]+$/;
          if ((collectionCd.length == 6 && !collectionCd.match(alphaNumeric)) || (collectionCd.length == 2 && isNaN(collectionCd))) {
            collectionCdValid = false;
          }
        } else {
          collectionCdValid = false;
        }

        if (!collectionCdValid) {

          return new ValidationResult({
            id : 'collectionCd',
            type : 'text',
            name : 'collectionCd'
          }, false, 'Collection Code should either be 2 characters (both digits) or 6 characters (digits and/or uppercase latin).');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addValidatorForCompanyRegNum() {
  // CRN
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var companyNum = FormManager.getActualValue('taxCd1');
        var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        if (companyNum != null && companyNum != undefined && companyNum != '') {
          if (issuingCntry == SysLoc.IRELAND && ((companyNum.length != 4 && companyNum.length != 5 && companyNum.length != 6) || !companyNum.match("^[0-9a-zA-Z]*$"))) {
            return new ValidationResult(null, false, 'Company Registartion Number should be of 4 ,5 or 6 alphanumeric characters.');
          } else if (issuingCntry == SysLoc.UK && (companyNum.length != 8 || !companyNum.match("^[0-9a-zA-Z]*$"))) {
            return new ValidationResult(null, false, 'Company Registartion Number should be of 8 alphanumeric characters.');
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

function emeaPpsCeidValidator() {
  var _custType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');

  if (reqType == 'C') {
    if (_custType == 'BUSPR' || _custType == 'XBP') {
      FormManager.show('PPSCEID', 'ppsceid');
      FormManager.enable('ppsceid');
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.setValue('ppsceid', '');
      FormManager.readOnly('ppsceid');
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    }
  }
}

function clientTierCodeValidator() {
  console.log("Inside clientTierCodeValidator......")
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

// CREATCMR-4293
function setCTCValues() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {

    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var reqType = FormManager.getActualValue('reqType');
    // Business Partner OR Internal
    if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER') {
      FormManager.removeValidator('clientTier', Validators.REQUIRED);
      var isuCd = FormManager.getActualValue('isuCd');
      if (isuCd == '21') {
        FormManager.setValue('clientTier', '');
        if (reqType == 'U' || (reqType != 'U' && userRole == 'PROCESSOR')) {
          FormManager.enable('clientTier');
        }
      }
    }
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ITALY) {

    FormManager.removeValidator('clientTier', Validators.REQUIRED);

    var custSubGrp = FormManager.getActualValue('custSubGrp');
    // Business Partner OR Internal
    if (custSubGrp == 'CROBP' || custSubGrp == 'CROIN' || custSubGrp == 'INTER' || custSubGrp == 'BUSPR' || custSubGrp == 'BUSSM' || custSubGrp == 'INTSM' || custSubGrp == 'BUSVA'
        || custSubGrp == 'INTVA') {
      FormManager.removeValidator('clientTier', Validators.REQUIRED);
      var isuCd = FormManager.getActualValue('isuCd');
      if (isuCd == '21') {
        FormManager.setValue('clientTier', '');
        if (reqType == 'U' || (reqType != 'U' && userRole == 'PROCESSOR')) {
          FormManager.enable('clientTier');
        }
      }
    }
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {

    FormManager.removeValidator('clientTier', Validators.REQUIRED);

    var role = FormManager.getActualValue('userRole').toUpperCase();
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var reqType = FormManager.getActualValue('reqType');
    // Business Partner OR Internal
    if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER') {
      FormManager.removeValidator('clientTier', Validators.REQUIRED);
      var isuCd = FormManager.getActualValue('isuCd');
      if (isuCd == '21') {
        FormManager.setValue('clientTier', '');
        if (reqType == 'U' || (reqType != 'U' && userRole == 'PROCESSOR')) {
          FormManager.enable('clientTier');
        }
      }
    }
  }
}

function addCmrNoValidatorForUKI() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrNo = FormManager.getActualValue('cmrNo');

        if (FormManager.getActualValue('reqType') == 'U') {
          return new ValidationResult(null, true);
        }

        if (cmrNo != '' && cmrNo != null && !cmrNo.startsWith('P')) {
          if (cmrNo.length != 6) {
            return new ValidationResult(null, false, 'CMR Number should be exactly 6 digits long.');
          } else if (isNaN(cmrNo)) {
            return new ValidationResult(null, false, 'CMR Number should be only numbers.');
          } else if (cmrNo == "000000") {
            return new ValidationResult(null, false, 'CMR Number should not be 000000.');
          } else if (cmrNo != '' && custSubType != '' && custSubType == 'INTER' && !cmrNo.startsWith('99')) {
            return new ValidationResult(null, false, 'CMR Number should be in 99XXXX format for internal scenarios.');
          } else if (cmrNo != '' && custSubType != '' && custSubType != 'INTER' && cmrNo.startsWith('99')) {
            return new ValidationResult(null, false, 'Non Internal CMR Number should not be in 99XXXX for scenarios.');
          } else {
            var results1 = cmr.query('GET.CMR_BY_CNTRY_CUSNO_SAPR3_FOR_UKI', {
              CMRNO : cmrNo,
              MANDT : cmr.MANDT
            });

            var results2 = cmr.query('GET.CHECK_EXISTS_CMR_NO_FOR_UKI', {
              CMR_NO : cmrNo
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
// CREATCMR-1727

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

function addressQuotationValidatorUKI() {
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Street Cont' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'Attn' ]);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);
}

function clientTierCodeValidator() {
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
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

dojo.addOnLoad(function() {
  GEOHandler.UKI = [ SysLoc.UK, SysLoc.IRELAND ];
  console.log('adding EMEA functions...');
  GEOHandler.addAddrFunction(addEMEALandedCountryHandler, GEOHandler.UKI);
  GEOHandler.enableCopyAddress(GEOHandler.UKI, validateEMEACopy, [ 'ZD01', 'CTYC' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.UKI);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.UKI);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAfterTemplateLoad(emeaPpsCeidValidator, GEOHandler.UKI);
  GEOHandler.addAfterConfig(addHandlersUKI, GEOHandler.UKI);

  // UKI Specific
  GEOHandler.addAfterConfig(afterConfigForUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterConfig(defaultCapIndicatorUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterConfig(setAbbrevNmLocationLockAndMandatoryUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterConfig(addSalesRepLogicUK2018, [ SysLoc.UK ]);
  GEOHandler.registerValidator(addUKAddressTypeValidator, [ SysLoc.UK ], null, true);
  GEOHandler.registerValidator(addIRAddressTypeValidator, [ SysLoc.IRELAND ], null, true);
  GEOHandler.registerValidator(addPostCdCityValidator, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.addAddrFunction(addLatinCharValidatorUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.registerValidator(validateCompanyNoForUKI, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.UK, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.UK ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.IRELAND, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.IRELAND ], null, true);

  GEOHandler.registerValidator(addHwMstrInstFlgValidator, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.addAddrFunction(checkHwMstrInstallFlag, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.addAfterTemplateLoad(displayHwMstInstallFlagNew, [ SysLoc.UK, SysLoc.IRELAND ]);

  GEOHandler.addAddrFunction(setUKIAbbrevNmLocnOnAddressSave, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterConfig(showDeptNoForInternalsOnlyUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterTemplateLoad(showDeptNoForInternalsOnlyUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.registerValidator(validateInternalDeptNumberLength, [ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.addAddrFunction(autoSetAbbrevLocnOnAddSaveUKI, [ SysLoc.IRELAND, SysLoc.UK ]);

  GEOHandler.addAfterConfig(clearPhoneNoFromGrid, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterConfig(clearPOBoxFromGrid, [ SysLoc.UK ]);
  GEOHandler.addAfterTemplateLoad(clearPOBoxFromGrid, [ SysLoc.UK ]);
  GEOHandler.addAfterConfig(clearPOBoxFromGrid, [ SysLoc.IRELAND ]);
  GEOHandler.addAfterTemplateLoad(clearPOBoxFromGrid, [ SysLoc.IRELAND ]);
  GEOHandler.registerValidator(addStAttPoValidatorForUKI, [ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.registerValidator(addStPoValidatorUKI, [ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.registerValidator(addUKIaddressContentValidator, [ SysLoc.IRELAND, SysLoc.UK ], null, true);

  GEOHandler.addAfterConfig(clearPhoneNoFromGrid, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterConfig(clearPOBoxFromGrid, [ SysLoc.UK ]);
  GEOHandler.addAfterTemplateLoad(clearPOBoxFromGrid, [ SysLoc.UK ]);
  GEOHandler.addAfterConfig(clearPOBoxFromGrid, [ SysLoc.IRELAND ]);
  GEOHandler.addAfterTemplateLoad(clearPOBoxFromGrid, [ SysLoc.IRELAND ]);

  // Italy
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.UKI, GEOHandler.ROLE_PROCESSOR, true);

  // For Legacy Direct
  GEOHandler.addAfterConfig(optionalFieldsForUpdateReqUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  // GEOHandler.registerValidator(addUKILandedCountryValidtor, [ SysLoc.UK ],
  // null, true);
  GEOHandler.registerValidator(restrictDuplicateAddrUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.registerValidator(validateNumericValueUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterConfig(lockCustClassUKI, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterTemplateLoad(lockCustClassUKI, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterConfig(lockRequireFieldsUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterTemplateLoad(lockRequireFieldsUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.registerValidator(addStreetPoBoxValidatorUKI, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.registerValidator(addEmbargoCodeValidatorUKI, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.addAddrFunction(autoPopulateISUClientTierUK, [ SysLoc.UK ]);
  GEOHandler.addAddrFunction(disableAddrFieldsUKI, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAddrFunction(optionalRulePostalCodeUK, [ SysLoc.UK ]);
  GEOHandler.addAddrFunction(setTaxCdBasedOnlandCntryUK, [ SysLoc.UK ]);
  GEOHandler.addAddrFunction(addPOBOXValidatorEMEA, [ SysLoc.IRELAND, SysLoc.UK ]);

  GEOHandler.addAfterConfig(setIBMFieldsMandtOptional, [ SysLoc.IRELAND, SysLoc.UK ]);
  GEOHandler.addAfterTemplateLoad(setIBMFieldsMandtOptional, [ SysLoc.IRELAND, SysLoc.UK ]);

  GEOHandler.registerValidator(requestingLOBCheckFrIFSL, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.registerValidator(addValidatorForCollectionCdUpdateUKI, [ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.registerValidator(validateCollectionCdValueUKI, [ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.registerValidator(addValidatorForCompanyRegNum, [ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.registerValidator(addCustClassValidatorBP, [ SysLoc.IRELAND, SysLoc.UK ], null, true);

  GEOHandler.addAfterTemplateLoad(setCustClassCd, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterConfig(setCustClassCd, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterTemplateLoad(configureCRNForUKI, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterConfig(configureCRNForUKI, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterTemplateLoad(autoSetVAT, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterConfig(autoSetVAT, [ SysLoc.UK, SysLoc.IRELAND ]);

  GEOHandler.registerValidator(validateVATForINFSLScenarioUKI, [ SysLoc.IRELAND ], null, true);

  GEOHandler.registerValidator(clientTierValidator, [ SysLoc.IRELAND, SysLoc.UK ], null, true);
  GEOHandler.addAfterConfig(resetVATValidationsForPayGo, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterConfig(autoSetSpecialTaxCdByScenario, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterTemplateLoad(autoSetSpecialTaxCdByScenario, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterTemplateLoad(resetVATValidationsForPayGo, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterConfig(lockIsuCtcUKI, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterTemplateLoad(lockIsuCtcUKI, [ SysLoc.UK, SysLoc.IRELAND ]);
  // CREATCMR-1727

  GEOHandler.registerValidator(validateSalesRepForUKI, [ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.registerValidator(validateSboSrForIsuCtcUK, [ SysLoc.UK ], null, true);
  GEOHandler.registerValidator(validateSboSrForIsuCtcIE, [ SysLoc.IRELAND ], null, true);
  GEOHandler.registerValidator(addCmrNoValidatorForUKI, [ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.registerValidator(addVatIndValidator,[ SysLoc.UK, SysLoc.IRELAND ], null, true);
  GEOHandler.addAfterConfig(setVatIndFieldsForGrp1AndNordx, [ SysLoc.UK, SysLoc.IRELAND ]);
  GEOHandler.addAfterTemplateLoad(setVatIndFieldsForGrp1AndNordx,[ SysLoc.UK, SysLoc.IRELAND ]);

});
