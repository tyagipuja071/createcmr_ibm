/* Register NETHERLANDS Javascripts */
var _addrTypesForNL = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01' ];
var _poBOXHandler = [];
var _reqReasonHandler = null;
function afterConfigForNL() {
  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  FormManager.readOnly('capInd');
  FormManager.setValue('capInd', true);
  FormManager.readOnly('cmrOwner');
  FormManager.resetValidations('enterprise');

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  
//Set abbrevLocn for Softlayer Scenario
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType == 'SOFTL') {
    FormManager.setValue('abbrevLocn', 'Softlayer');
  }
  if (reqType == 'C' && role != 'Processor') {
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('subIndustryCd');
    FormManager.resetValidations('engineeringBo');
  }

  if (role == 'Processor' && reqType == 'C') {
    FormManager.enable('abbrevNm');
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('engineeringBo', Validators.REQUIRED, [ 'BO Team' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('isuCd', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED); 
    FormManager.removeValidator('engineeringBo', Validators.REQUIRED); 
  }
  if (reqType == 'U') {
    FormManager.resetValidations('engineeringBo');
    FormManager.resetValidations('collectionCd');
  }
  if (reqType == 'C') {
    FormManager.clearValue('collectionCd');
    FormManager.readOnly('collectionCd');
  }
  if(role == 'Processor' || reqType == 'U'  && FormManager.getActualValue('viewOnlyPage') != 'true'){
    FormManager.enable('abbrevLocn');
  } else{
    FormManager.readOnly('abbrevLocn');
  }
  var landCntry = '';
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion && cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }
  setVatValidatorNL();
  
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  if (custSubScnrio == 'PRICU' || custSubScnrio == 'PUBCU' || custSubScnrio == 'CBCOM' || custSubScnrio == 'CBBUS') {
    FormManager.removeValidator('taxCd2', Validators.REQUIRED);
  } else if(reqType != 'U'){
    FormManager.addValidator('taxCd2', Validators.REQUIRED, [ 'KVK' ], 'MAIN_CUST_TAB');
  }  
  
  setDeptartmentNumber();
  
}

/* Vat Handler */
function setVatValidatorNL() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (!dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
    }
  }
}

function setDeptartmentNumber(){
  var reqType = FormManager.getActualValue('reqType');
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  var cmrNo = FormManager.getActualValue('cmrNo');
  var intCmrRegEx = /(^99)/;
  var deptNum =   FormManager.getActualValue('ibmDeptCostCenter');


  if (reqType == 'C'){
    if(deptNum && deptNum != ''){
      FormManager.setValue('ibmDeptCostCenter',deptNum);
     }
    if(custSubScnrio != 'INTER' && custSubScnrio != '' && custSubScnrio){
      FormManager.readOnly('ibmDeptCostCenter');
      FormManager.setValue('ibmDeptCostCenter',''); 
    }
    else{
      FormManager.enable('ibmDeptCostCenter');
    }   
  } else {
    if(intCmrRegEx.test(cmrNo)){
      FormManager.enable('ibmDeptCostCenter');
    }
    else{
      FormManager.readOnly('ibmDeptCostCenter');
      FormManager.setValue('ibmDeptCostCenter','');
    }   
  }
}

function disableLandCntry() {
  var custType = FormManager.getActualValue('custGrp');
  if (custType == 'LOCAL' && FormManager.getActualValue('addrType') == 'ZS01') {
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
 * lock Embargo Code field
 */
function lockEmbargo() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
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
var _BOTeamHandler = null;
var _IMSHandler = null;
var _vatExemptHandler = null;
var _ExpediteHandler = null;

function addHandlersForNL() {

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValues(value);
    });
  }

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setBOTeamValues(value);
    });
  }

  if (_IMSHandler == null && FormManager.getActualValue('cmrIssuingCntry')) {
    _IMSHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      setBOTeamValues(value);
    });
  }

  if (_BOTeamHandler == null) {
    _BOTeamHandler = dojo.connect(FormManager.getField('engineeringBo'), 'onChange', function(value) {
      setINACValues(value);
      setEconomicCodeValues(value);
    });
  }
  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorNL();
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

/*
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
 * NL - sets BO Team based on isuCtc
 */
function setBOTeamValues(clientTier) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var clientTier = FormManager.getActualValue('clientTier');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var isuCd = FormManager.getActualValue('isuCd');
  var ims = FormManager.getActualValue('subIndustryCd');
  var role = FormManager.getActualValue('userRole').toUpperCase();


  var boTeam = [];
  if (isuCd != '') {
    var isuCtc = isuCd + clientTier;
    var qParams = null;
    var results = null;

    // BO Team will be based on IMS for 32S
    if (ims != '' && ims.length > 1 && (isuCtc == '32S')) {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry,
        ISU : '%' + isuCd + clientTier + '%',
        CLIENT_TIER : '%' + ims.substring(0, 1) + '%'
      };
      results = cmr.query('GET.BOTEAMLIST.BYISUCTC', qParams);
    } else {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry,
        ISU : '%' + isuCd + clientTier + '%'
      };
      results = cmr.query('GET.BOTEAMLIST.BYISU', qParams);
    }

    if (results != null) {
      for ( var i = 0; i < results.length; i++) {
        boTeam.push(results[i].ret1);
      }
      FormManager.limitDropdownValues(FormManager.getField('engineeringBo'), boTeam);
      if (boTeam.length == 1) {
        FormManager.setValue('engineeringBo', boTeam[0]);
      }
    }
    
    var custSubScnrio = FormManager.getActualValue('custSubGrp');
    if (custSubScnrio == 'BUSPR') {
      FormManager.setValue('engineeringBo', '33P01');
      FormManager.readOnly('engineeringBo');
      FormManager.readOnly('economicCd');
    } else if (custSubScnrio == 'INTER' || custSubScnrio == 'PRICU') {
      FormManager.setValue('engineeringBo', '33U00');
      FormManager.readOnly('engineeringBo');
      FormManager.readOnly('economicCd');
    } else if (custSubScnrio == 'PUBCU' && role != 'PROCESSOR') {
      FormManager.readOnly('economicCd');
    } else {
      FormManager.enable('engineeringBo');
      if(custSubScnrio != 'CBBUS' && role != 'REQUESTER')
      FormManager.enable('economicCd');
    }
  }
}

/*
 * NL - INAC Code based on BOTEAM Values
 **/
function setINACValues(engineeringBo) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var engineeringBo = FormManager.getActualValue('engineeringBo');

  var inacCode = [];
  if (engineeringBo != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      EngineeringBo : '%' + engineeringBo + '%'
    };
    var results = cmr.query('GET.INACLIST.BYBO', qParams);
    if (results != null) {
      for ( var i = 0; i < results.length; i++) {
        inacCode.push(results[i].ret1);
      }
      if (inacCode != null) {
        FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCode);
        if (inacCode.length == 1) {
          FormManager.setValue('inacCd', inacCode[0]);
        }
        /*if (inacCode.length == 0) {
          FormManager.setValue('inacCd', '');
        }*/
      }
    }
  }
}

/*
 * NL - Economic Code based on BOTEAM Values
 **/
function setEconomicCodeValues(engineeringBo) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var engineeringBo = FormManager.getActualValue('engineeringBo');

  var economicCode = [];
  if (engineeringBo != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      REP_TEAM_MEMBER_NO : '%' + engineeringBo + '%'
    };
    var results = cmr.query('GET.ECONOMICLIST.BYST.NL', qParams);
    if (results != null) {
      for ( var i = 0; i < results.length; i++) {
        economicCode.push(results[i].ret1);
      }
      if (economicCode != null) {
        FormManager.limitDropdownValues(FormManager.getField('economicCd'), economicCode);
        if (economicCode.length == 1) {
          FormManager.setValue('economicCd', economicCode[0]);
        }
      }
    }
  }
}

function addAbbrevLocnLengthValidator() {
  var reqType = FormManager.getActualValue('reqType');
  var role = _pagemodel.userRole;
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevLocn = FormManager.getActualValue('abbrevLocn');
        if (reqType != 'U' && role == 'Requester' && _abbrevLocn.length != 12) {
          return new ValidationResult({
            id : 'abbrevLocn',
            type : 'text',
            name : 'abbrevLocn'
          }, false, 'The length for Abbreviated Location should be 12 characters.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAbrrevNameLengthValidator() {
  
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevName = FormManager.getActualValue('abbrevNm');
        if (reqType != 'U' && role == 'Requester' && _abbrevName.length != 22) {
          return new ValidationResult({
            id : 'abbrevNm',
            type : 'text',
            name : 'abbrevNm'
          }, false, 'The length for Abbreviated Name should be 22 characters.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    
    }; 
  })(), 'MAIN_CUST_TAB', 'frmCMR');

}

function addKVKLengthValidator() {
  var reqType = FormManager.getActualValue('reqType');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _kvkLen = FormManager.getActualValue('taxCd2');
        if (reqType != 'U' && _kvkLen && _kvkLen.length > 0 && _kvkLen.length != 8) {
          return new ValidationResult({
            id : 'taxCd2',
            type : 'text',
            name : 'taxCd2'
          }, false, 'The length for KVK should be 8 characters.');
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
    FormManager.setValue('embargoCd', '5');
    FormManager.setValue('collectionCd', 'TCNL99');
  }
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

function addHandlerForReqRsn() {
  if (_reqReasonHandler == null) {
    _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
      chngesAfterBnkrptcy();
    });
  }
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
    };
  }
  var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
  var city;
  var abbrevLocn = null;
  var abbrvNm = custNm.ret1;
  var cntryRegion = FormManager.getActualValue('countryUse');
  var mscenario = FormManager.getActualValue('custGrp');
  var scenario = null;
  if (mscenario == 'CROSS') {
    scenario = 'CROSS';
  } else if (mscenario == ((cntryRegion.substring(3, 5) + "CRO"))) {
    scenario = 'CROSS';
  }
  if (scenario == 'CROSS') {
    city = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', reqParam);
    abbrevLocn = getLandCntryDesc(city.ret1);
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

  if (abbrevLocn != null) {
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }
  if (abbrvNm != null) {
    FormManager.setValue('abbrevNm', abbrvNm);
  }
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

function validateNLCopy() {
  return null;
}

function updateAddrTypeList(cntry, addressMode, saving) {
  // hide 'KVK/VAT' selection for copy
  if ((addressMode == 'newAddress' || addressMode == 'copyAddress') && cmr.currentRequestType == 'C' || cmr.currentRequestType == 'U') {
    cmr.hideNode('radiocont_ZKVK');
    cmr.hideNode('radiocont_ZVAT');
  }
}

/*
 * Mandatory addresses ZS01/ZP01/ZI01 *Billing (Sold-to) *Installing (Install
 * at) *Mailing - not flowing into RDC!!
 */
function addNLAddressTypeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'General address is mandatory. Only one address for each address type should be defined when sending for processing.');
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
            } else if (type == 'ZD01') {
              shippingCnt++;
            } else if (type == 'ZI02') {
              eplCnt++;
            }
          }
          if (mailingCnt == 0) {
            return new ValidationResult(null, false, 'General Address is mandatory.');
          } else if (billingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Billing address can be defined. Please remove the additional Billing address.');
          } else if (mailingCnt > 1) {
            return new ValidationResult(null, false, 'Only one General address can be defined. Please remove the additional Mailing address.');
          } else if (eplCnt > 1) {
            return new ValidationResult(null, false, 'Only one EPL Mailing address can be defined. Please remove the additional EPL Mailing address.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addNLVATValidator(cntry, tabName, formName, aType) {
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

function addAddressFieldValidators() {
  // Street and PO BOX for General Address
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType');
        var addrFldCnt = 0;
        var streetCont = 0;
        if (FormManager.getActualValue('poBox') != '') {
          addrFldCnt++;
        }
        if (FormManager.getActualValue('addrTxt') != '') {
          addrFldCnt++;
          streetCont++;
        }
        if (addrFldCnt < 2 && streetCont < 1 && addrType == 'ZS01') {
          return new ValidationResult(null, false, 'In General Address, Street is mandatory at all times. It can be filled along with PO BOX but PO BOX cannot be filled without Street.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');


  // Street and PO BOX
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType');
        var addrFldCnt = 0;
        if (FormManager.getActualValue('poBox') != '') {
          addrFldCnt++;
        }
        if (FormManager.getActualValue('addrTxt') != '') {
          addrFldCnt++;
        }
        if (addrFldCnt < 1 && addrType != 'ZS01') {
          return new ValidationResult(null, false, 'For Street+NBR and PostBox, atleast one should be filled.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // only 2 out of 4 can be filled
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var nameCont = FormManager.getActualValue('custNm2');
        var poBox = FormManager.getActualValue('poBox');
        var attPerson = FormManager.getActualValue('custNm4');
        var dept = FormManager.getActualValue('dept');
        var addrFldCnt1 = 0;
        if (nameCont != '') {
          addrFldCnt1++;
        }
        if (poBox != '') {
          addrFldCnt1++;
        }
        if (attPerson != '') {
          addrFldCnt1++;
        }
        if (dept != '') {
          addrFldCnt1++;
        }
        if (addrFldCnt1 > 2) {
          return new ValidationResult(null, false, 'department, PostBox, Attention person and Name Con\'t - only 2 out of 4 can be filled.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // ALL NL POBOX
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
  
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntryCd = FormManager.getActualValue('landCntry');
        var custGrp = FormManager.getActualValue('custGrp');
          var city = FormManager.getActualValue('city1');
          var postCd = FormManager.getActualValue('postCd');
          var  cntry = getLandCntryDesc(cntryCd);
           var val = city + postCd + cntry;
            if (val.length > 30 && custGrp == 'CROSS') {
              return new ValidationResult(null, false, 'Total computed length of City,Country and Postal Code should not exceed 30 characters.');
            }        
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');


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

function addCrossBorderValidatorNL() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var addrType = 'ZS01';
        var seqNo = '99901';
        var record = null;
        var type = null;
        var billingCnt = 0;
        // only Create type will be validated
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }

        var scenario = FormManager.getActualValue('custGrp');
        if (scenario != null && scenario.includes('CRO')) {
          scenario = 'CROSS';
        }

        var cntryRegion = FormManager.getActualValue('countryUse');
        var landCntry = 'NL'; // default to Netherlands
        if (cntryRegion != '' && cntryRegion.length > 3) {
          landCntry = cntryRegion.substring(3, 5);
        }
        
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
        for ( var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
          record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
          if (record == null && _allAddressData != null && _allAddressData[i] != null) {
            record = _allAddressData[i];
          }
          type = record.addrType;
          if (typeof (type) == 'object') {
            type = type[0];
          }
           if (type == 'ZP01') {
            billingCnt++;
          } 
        }
        
        if(billingCnt > 0){
          addrType = 'ZP01';
          seqNo = '29901';
        }        
        }
        var reqId = FormManager.getActualValue('reqId');
        var defaultLandCntry = landCntry;
        var result = cmr.query('VALIDATOR.CROSSBORDERNL', {
          REQID : reqId,
          ADDRTYPE : addrType,
          SEQNO : seqNo
        });
        if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS' && billingCnt == 0) {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS'  && billingCnt == 0) {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
        }else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS' && billingCnt > 0) {
          return new ValidationResult(null, false, 'Landed Country value of the Billing  Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS'  && billingCnt > 0) {
          return new ValidationResult(null, false, 'Landed Country value of the Billing  Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}


function hideCustPhone() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    for ( var i = 0; i < _addrTypesForNL.length; i++) {
      _poBOXHandler[i] = null;
      if (_poBOXHandler[i] == null) {
        _poBOXHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForNL[i]), 'onClick', function(value) {
          setPhone();
        });
      }
    }
  }
  if (cmr.addressMode == 'updateAddress') {
    if (FormManager.getActualValue('addrType') == 'ZS01') {
      FormManager.enable('custPhone');
    } else {
      FormManager.disable('custPhone');
      FormManager.setValue('custPhone', '');

    }
  }
}
function setPhone() {
  if (FormManager.getField('addrType_ZS01').checked) {
    FormManager.enable('custPhone');
  } else {
    FormManager.disable('custPhone');
    FormManager.setValue('custPhone', '');
  }
}

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    $('label[for="custNm1_view"]').text('Company Name:');
    $('label[for="custNm2_view"]').text('Name Con' + '\'' + 't:');
    $('label[for="custNm4_view"]').text('Att. Person');
    $('label[for="landCntry_view"]').text('Country (Landed):');
    $('label[for="addrTxt_view"]').text('Street+NBR');
    $('label[for="stateProv_view"]').text('State/Prov');
    $('label[for="postCd_view"]').text('Postal Code:');
    $('label[for="poBox_view"]').text('PostBox:');
    $('label[for="dept_view"]').text('Department:');
  }
}

function addPhoneValidatorNL() {
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
}

function setFieldsMandtOnSc(){
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if(custSubGrp == 'COMME'){
    FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Tax Code' ]);
  }
}

function attnFormatterNL(value, rowIndex){
  var rowData = this.grid.getItem(rowIndex);
  var attPerson = rowData.custNm4[0] ? rowData.custNm4[0] : '';
  var department = rowData.dept[0] ? rowData.dept[0] : '';
  return department + '<br>' + (attPerson ? ' ' + attPerson : '');
}


dojo.addOnLoad(function() {
  GEOHandler.NL = [ '788' ];
  console.log('adding NETHERLANDS functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.NL);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.NL);
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.NL);
  GEOHandler.addAfterConfig(afterConfigForNL, GEOHandler.NL);
  GEOHandler.addAfterConfig(addHandlersForNL, GEOHandler.NL);
  GEOHandler.addAfterConfig(addHandlerForReqRsn, GEOHandler.NL);
  GEOHandler.addAfterConfig(setClientTierValues, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setAbbrvNmLoc, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(afterConfigForNL, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setClientTierValues, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setBOTeamValues, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setINACValues, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setEconomicCodeValues, GEOHandler.NL);
  GEOHandler.registerValidator(addKVKLengthValidator, GEOHandler.NL, null, true);
  GEOHandler.addAfterTemplateLoad(setFieldsMandtOnSc, GEOHandler.NL);
  GEOHandler.registerValidator(addCrossBorderValidatorNL, GEOHandler.NL, null, true);
  //GEOHandler.registerValidator(addAbrrevNameLengthValidator, GEOHandler.NL,
   //null, true);
    
  GEOHandler.registerValidator(checkForBnkruptcy, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(addNLVATValidator('', 'MAIN_CUST_TAB', 'frmCMR', 'ZP01'), GEOHandler.NL, null, true);
  GEOHandler.enableCopyAddress(GEOHandler.NL, validateNLCopy, [ 'ZD01' ]);
  GEOHandler.addAddrFunction(updateAddrTypeList, GEOHandler.NL);
  GEOHandler.addAddrFunction(addLandedCountryHandler, GEOHandler.NL);
  GEOHandler.registerValidator(addNLAddressTypeValidator, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.NL, null, true);
  GEOHandler.addAddrFunction(hideCustPhone, GEOHandler.NL);
  GEOHandler.addAddrFunction(addPhoneValidatorNL, GEOHandler.NL);
  GEOHandler.addAddrFunction(disableLandCntry, GEOHandler.NL);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.NL, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.NL);
});