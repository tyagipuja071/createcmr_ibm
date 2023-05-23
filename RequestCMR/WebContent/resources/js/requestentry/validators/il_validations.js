/* Register Israel Javascripts */
var _CTCHandlerIL = null;
var _ISUHandlerIL = null;
var _SubindustryHandlerIL = null;
var _vatExemptHandler = null;
var _gtcAddrTypeHandlerIL = [];
var _gtcAddrTypesIL = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01', 'ZS02', 'CTYA', 'CTYB', 'CTYC' ];
var _prolifCountries = [ 'AF', 'AM', 'AZ', 'BH', 'BY', 'KH', 'CN', 'CU', 'EG', 'GE', 'IR', 'IQ', 'IL', 'JO', 'KZ', 'KP', 'KW', 'KG', 'LA', 'LB', 'LY', 'MO', 'MD', 'MN', 'MM', 'OM', 'PK', 'QA', 'RU',
    'SA', 'SD', 'SY', 'TW', 'TJ', 'TM', 'UA', 'AE', 'UZ', 'VE', 'VN', 'YE' ];
var _requestingLOBHandler = null;
var _streetHandler = null;
var _oldIsuCd = null;
var _oldCtc = null;
var _oldEnt = null;
var _oldIsu = null;
var _oldClientTier = null;
var _isrealCustGrpHandler = null;

if (_isrealCustGrpHandler == null && FormManager.getActualValue('reqType') == 'C' && FormManager.getActualValue('userRole').toUpperCase() == 'REQUESTER') {
  _isrealCustGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    lockUnlockFieldForISrael();
  });
}

function addHandlersForIL() {
  console.log(">>>> addHandlersForIL ");
  _oldIsu = FormManager.getActualValue('isuCd');
  _oldClientTier = FormManager.getActualValue('clientTier');
  getExitingValueOfCTCAndIsuCD();
  addRemoveValidator();
  lockUnlockFieldForISrael();
  for (var i = 0; i < _gtcAddrTypesIL.length; i++) {
    _gtcAddrTypeHandlerIL[i] = null;
    if (_gtcAddrTypeHandlerIL[i] == null) {
      _gtcAddrTypeHandlerIL[i] = dojo.connect(FormManager.getField('addrType_' + _gtcAddrTypesIL[i]), 'onClick', function(value) {
        countryUseAISRAEL();
        setAddrFieldsBehavior();
      });
    }
  }

  if (_ISUHandlerIL == null) {
    _ISUHandlerIL = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      if (_oldIsu != FormManager.getActualValue('isuCd') || (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != FormManager.getActualValue('custSubGrp'))) {
      setClientTierValuesIL();
      setEnterpriseSalesRepSBO();
      _oldIsu = FormManager.getActualValue('isuCd');
      }
    });
  }
  
  if (_CTCHandlerIL == null) {
    _CTCHandlerIL = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      if (_oldClientTier != FormManager.getActualValue('clientTier') || (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != FormManager.getActualValue('custSubGrp'))) {
      setEnterpriseSalesRepSBO();
      _oldClientTier = FormManager.getActualValue('clientTier');
      }
    });
  }

  if (_streetHandler == null) {
    _streetHandler = dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {
      setStreetContBehavior();
    });
  }

  if (_SubindustryHandlerIL == null) {
    _SubindustryHandlerIL = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      FormManager.readOnly('subIndustryCd');
    });
  }
}

function addEMEALandedCountryHandler(cntry, addressMode, saving, finalSave) {
  console.log(">>>> addEMEALandedCountryHandler");
  var scenario = FormManager.getActualValue('custGrp');
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      if (scenario != 'CROSS') {
        FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
      }
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }

    if (addressMode == 'updateAddress' && FormManager.getActualValue('reqType') == 'C') {
      FormManager.setValue('landCntry', cmr.oldlandcntry);
    }
  }
}

var _addrTypesIL = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'CTYA', 'CTYB', 'CTYC' ];
var _addrTypeHandler = [];
function afterConfigForIsrael() {
  console.log(">>>>  afterConfigForIsrael");
  // addrType Handler
  for (var i = 0; i < _addrTypesIL.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesIL[i]), 'onClick', function(value) {
        setCustPhone(value);
        addressQuotationValidatorIsrael();
      });
    }
  }

  GEOHandler.setAddressTypeForName('CTYA');

  // Israel COD Flag change
  if (FormManager.getActualValue('reqType') == 'C') {
    setCodFlagVal();

    if (_requestingLOBHandler == null) {
      var _custType = FormManager.getActualValue('custSubGrp');
      _requestingLOBHandler = dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
        var lob = FormManager.getActualValue('requestingLob');
        if (lob != '') {
          lockCustomerClassByLob(_custType);
        }
      });
    }

    if (_vatExemptHandler == null) {
      _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
        resetVatRequired();
      });
    }

    var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
    if (viewOnlyPage) {
      FormManager.hide('CollectionCd', 'collectionCd');
      var checklist = dojo.query('table.checklist');

      var questions = checklist.query('input[type="radio"]');

      for (var i = 0; i < questions.length; i++) {
        questions[i].disabled = true;
      }
      FormManager.hide('CodFlag', 'codFlag');
    }
  }

  if (FormManager.getActualValue('reqType') == 'U') {
    lockCollectionCdForUpdate();
    if (_requestingLOBHandler == null) {
      _requestingLOBHandler = dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
        var lob = FormManager.getActualValue('requestingLob');
        if (lob != '') {
          lockCollectionCdForUpdate();
        }
      });
    }
  }
  limitMODValues();
}

function setChecklistStatus() {

  console.log(">>>>  setChecklistStatus");
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
      if(isChecklistNotRequired()) {
        document.getElementById("checklistStatus").innerHTML = "Not Required";
        FormManager.setValue('checklistStatus', "Not Required");
      } else if (checkCount == 0) {
        document.getElementById("checklistStatus").innerHTML = "Not Done";
        FormManager.setValue('checklistStatus', "Not Done");
      } else if (noOfQuestions != checkCount) {
        document.getElementById("checklistStatus").innerHTML = "Incomplete";
        FormManager.setValue('checklistStatus', "Incomplete");
      } else {
        document.getElementById("checklistStatus").innerHTML = "Complete";
        FormManager.setValue('checklistStatus', "Complete");
      }
    } else {
      document.getElementById("checklistStatus").innerHTML = "Complete";
      FormManager.setValue('checklistStatus', "Complete");
    }
  }
}

function isChecklistNotRequired() {
  console.log(">>>>  isChecklistNotRequired");
  var reqType = FormManager.getActualValue('reqType');

  if (reqType == 'U') {
    var requestId = FormManager.getActualValue('reqId');
    var queryParams = {
      REQ_ID : requestId,
      ADDR_TYPE : 'CTYA'
    };
    var resultq = cmr.query('CHECK.ADDR.UPDATED_IL', queryParams);
    if (resultq.ret1 != '1') {
      return true;
    }
  } else if(reqType == 'C') {
    var scenario = FormManager.getActualValue('custSubGrp');
    var zs01ReqId = FormManager.getActualValue('reqId');
    var prolif = false;
    var qParams = {
      REQ_ID : zs01ReqId,
    };
    var result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', qParams);
    landCntry = result.ret1;
    if (_prolifCountries.includes(landCntry)) {
      prolif = true;
    }
    if (scenario == 'CROSS' && !prolif) {
      return true;
    }
  }
  return false;
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

function addILChecklistValidator() {
  console.log(">>>>  addILChecklistValidator");
  reqType = FormManager.getActualValue('reqType');

  if (reqType == 'U') {
    return;
  }

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        rType = FormManager.getActualValue('reqType');
        if (rType == 'U') {
          var requestId = FormManager.getActualValue('reqId');
          var queryParams = {
            REQ_ID : requestId,
            ADDR_TYPE : 'CTYA'
          };
          var resultq = cmr.query('CHECK.ADDR.UPDATED_IL', queryParams);
          if (resultq.ret1 != '1') {
            return;
          }
        }
        custSubScnrio = FormManager.getActualValue('custSubGrp');
        var zs01ReqId = FormManager.getActualValue('reqId');
        var prolif = false;
        var qParams = {
          REQ_ID : zs01ReqId,
        };
        var result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', qParams);
        landCntry = result.ret1;
        if (_prolifCountries.includes(landCntry)) {
          prolif = true;
        }
        if (custSubScnrio == 'CROSS' && !prolif) {
          return;
        }
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
            if (checklist.query('input[type="text"]')[i].value.trimEnd() == '' && ((i < 3 || i >= 10) || ((i >= 3 || i < 10) && document.getElementById('checklist_txt_field_' + (i+3)).style.display == 'block'))) {
              return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
            }
          }
          if (noOfQuestions != checkCount) {
            return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
          }
        }

        // add check for checklist on DB
        var reqId = FormManager.getActualValue('reqId');
        var record = cmr.getRecord('GBL_CHECKLIST', 'ProlifChecklist', {
          REQID : reqId
        });
        if (!record || !record.sectionA1) {
          var action = FormManager.getActualValue('yourAction')
          if(action == YourActions.Save) {
            return new ValidationResult(null, true);
          }
          return new ValidationResult(null, false, 'Checklist has not been registered yet. Please execute a \'Save\' action before sending for processing to avoid any data loss.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CHECKLIST_TAB', 'frmCMR');
}

/**
 * 1310266 - 'newAddress' enable custPhone for mailing address
 */
function setCustPhone(value) {
  console.log(">>>>  setCustPhone");
  if (FormManager.getField('addrType_ZS01').checked) {
    FormManager.show('CustPhone', 'custPhone');
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
    FormManager.hide('CustPhone', 'custPhone');
  }
}

function setCodFlagVal() {
  console.log(">>>>  setCodFlagVal");
  FormManager.setValue('codFlag', '3');
  FormManager.readOnly('codFlag');
}

function validateEMEACopy(addrType, arrayOfTargetTypes) {
  console.log(">>>>  validateEMEACopy");
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
  }
  return null;
}

function setClientTierValuesIL(value) {
  console.log(">>>>  setClientTierValuesIL");
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  
  if (isuCd == '34') {
    FormManager.setValue('clientTier', 'Q');
  } else if (isuCd == '36') {
    FormManager.setValue('clientTier', 'Y');
  } else if (isuCd == '32') {
    FormManager.setValue('clientTier', 'T');
  } else {
    FormManager.setValue('clientTier', '');
  }
  addRemoveValidator();
  lockUnlockFieldForISrael();
}

function addILAddressTypeValidator() {
  console.log(">>>>  addILAddressTypeValidator");
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

function addAddressLandedPairingValidatorMailing() {
  console.log(">>>>  addAddressLandedPairingValidatorMailing");
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
          var mailingLanded = '';
          var ctyALanded = '';
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
              mailingLanded = record.landCntry[0];
            } else if (type == 'CTYA') {
              ctyALanded = record.landCntry[0];
            }
          }
          if (mailingLanded != '' && ctyALanded != '') {
            if (mailingLanded != ctyALanded) {
              return new ValidationResult(null, false, '\'Country (Landed)\' of Mailing should match Country Use A (Mailing).');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addAddressLandedPairingValidatorBilling() {
  console.log(">>>>  addAddressLandedPairingValidatorBilling");
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
          var billingLanded = '';
          var ctyBLanded = '';
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }

            if (type == 'ZP01') {
              billingLanded = record.landCntry[0];
            } else if (type == 'CTYB') {
              ctyBLanded = record.landCntry[0];
            }
          }
          if (billingLanded != '' && ctyBLanded != '') {
            if (billingLanded != ctyBLanded) {
              return new ValidationResult(null, false, '\'Country (Landed)\' of Billing should match Country Use B (Billing).');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addAddressLandedPairingValidatorShipping() {
  console.log(">>>>  addAddressLandedPairingValidatorShipping");
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
          var shippingLanded = '';
          var shippingSeq = '';
          var ctyCLanded = '';
          var ctyCPairedSeq = '';
          var ctyCSeq = '';
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }

            if (type == 'ZD01') {
              shippingLanded = record.landCntry[0];
              shippingSeq = record.addrSeq[0];
              for (var j = 0; j < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; j++) {
                var ctyCrecord = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(j);
                if (ctyCrecord == null && _allAddressData != null && _allAddressData[j] != null) {
                  ctyCrecord = _allAddressData[j];
                }
                var ctyCtype = ctyCrecord.addrType;
                if (typeof (ctyCtype) == 'object') {
                  ctyCtype = ctyCtype[0];
                }

                if (ctyCtype == 'CTYC') {
                  ctyCLanded = ctyCrecord.landCntry[0];
                  ctyCPairedSeq = ctyCrecord.pairedSeq[0];
                  ctyCSeq = ctyCrecord.addrSeq[0];

                  if (shippingLanded != '' && ctyCLanded != '' && ctyCPairedSeq != null && shippingSeq != null && ctyCPairedSeq != '' && shippingSeq != '') {
                    if ((ctyCPairedSeq == shippingSeq) && (shippingLanded != ctyCLanded)) {
                      return new ValidationResult(null, false, '\'Country (Landed)\' of Shipping - (' + shippingSeq + ') should match Country Use C (Shipping) - (' + ctyCSeq + ')');
                    }
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

function validatePairedAddrFieldNumericValue() {
  console.log(">>>>  validatePairedAddrFieldNumericValue");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var arrErrorMsg = new Array();
          
          const addrTypeDisplayMap = new Map();
          addrTypeDisplayMap.set('ZS01', 'Mailing');
          addrTypeDisplayMap.set('ZP01', 'Billing');
          addrTypeDisplayMap.set('ZD01', 'Shipping');
          addrTypeDisplayMap.set('CTYA','Country Use A (Mailing)');
          addrTypeDisplayMap.set('CTYB','Country Use B (Billing)');
          addrTypeDisplayMap.set('CTYC','Country Use C (Shipping)');
          
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            var localAddress = null;
            var translatedAddress = null;
            var localAddrType = null;
            var translatedAddrType = null;
            var localAddrSeqNo = '';
            var translatedAddrSeqNo = '';
            var indentSpace = '&nbsp;&nbsp;&nbsp;&nbsp;';
            
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            
            if (type == 'ZS01') {
              localAddress = record;
              translatedAddress = getPairedTranslatedAddrData(localAddress, 'CTYA');
              if (translatedAddress == null) {
                translatedAddress = getAddressByType('CTYA');
              }
              localAddrType = 'ZS01';
              translatedAddrType = 'CTYA';
            } else if (type == 'ZP01') {
              localAddress = record;
              translatedAddress = getPairedTranslatedAddrData(localAddress, 'CTYB');
              if (translatedAddress == null) {
                translatedAddress = getAddressByType('CTYB');
              }
              localAddrType = 'ZP01';
              translatedAddrType = 'CTYB';
            } else if (type == 'ZD01') {
              localAddress = record;
              translatedAddress = getPairedTranslatedAddrData(localAddress, 'CTYC');
              if (translatedAddress == null) {
                translatedAddress = getAddressByType('CTYC');
              }
              localAddrType = 'ZD01';
              translatedAddrType = 'CTYC';
              if (localAddress != null && translatedAddress != null) {
                localAddrSeqNo = '(' + localAddress.addrSeq[0] + ')';
                translatedAddrSeqNo = '(' + translatedAddress.addrSeq[0] + ')';
              }
            }
            
            if (localAddress != null && translatedAddress != null && isAddrPairNewOrUpdated(localAddress, translatedAddress)) {
              let errorMsg = `Mismatch ${addrTypeDisplayMap.get(localAddrType)} ${localAddrSeqNo} and ${addrTypeDisplayMap.get(translatedAddrType)} ${translatedAddrSeqNo}`;
              // street
              if (!isNumericValueEqual(localAddress.addrTxt[0], translatedAddress.addrTxt[0])) {
                if (arrErrorMsg.length > 0) {
                  arrErrorMsg.push(indentSpace + errorMsg + ' Street numeric values.<br>');
                } else {
                  arrErrorMsg.push(errorMsg + ' Street numeric values.<br>');
                }
              }
              // address cont
              if (!isNumericValueEqual(localAddress.addrTxt2[0], translatedAddress.addrTxt2[0])) {
                if (arrErrorMsg.length > 0) {
                  arrErrorMsg.push(indentSpace + errorMsg + ' Address Cont numeric values.<br>');
                } else {
                  arrErrorMsg.push(errorMsg + ' Address Cont numeric values.<br>');
                }
              }
              // po box
              if (localAddrType != null && localAddrType != 'ZD01') {
                if (!isNumericValueEqual(localAddress.poBox[0], translatedAddress.poBox[0])) {
                  if (arrErrorMsg.length > 0) {
                    arrErrorMsg.push(indentSpace + errorMsg + ' PO Box numeric values.<br>');
                  } else {
                    arrErrorMsg.push(errorMsg + ' PO Box numeric values.<br>');
                  }
                }
              }
              // postal code
              if (!isNumericValueEqual(localAddress.postCd[0], translatedAddress.postCd[0])) {
                if (arrErrorMsg.length > 0) {
                  arrErrorMsg.push(indentSpace + errorMsg + ' Postal Code numeric values.<br>');
                } else {
                  arrErrorMsg.push(errorMsg + ' Postal Code numeric values.<br>');
                }
              }
            }
          } // for
          if (arrErrorMsg.length > 0) {
            return new ValidationResult(null, false, arrErrorMsg.join(''));
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
  
}

function isNumericValueEqual(strA, strB) {
  console.log(">>>>  isNumericValueEqual");
  var arrStrA = [];
  var arrStrB = [];
  
  if (strA != null && strA != '') {
    arrStrA = strA.replace(/[^0-9]/g,' ').trim().split(' ');
    arrStrA = arrStrA.filter(item => item);
  }
  if (strB != null && strB != '') {
    arrStrB = strB.replace(/[^0-9]/g,' ').trim().split(' ');
    arrStrB = arrStrB.filter(item => item);
  }
    
  if (areEqual(arrStrA, arrStrB)) {
    return true;
  }
  return false;
}

function areEqual(array1, array2) {
  console.log(">>>>  areEqual");
  if (array1.length === array2.length) {
    return array1.every(element => {
      if (array2.includes(element)) {
        return true;
      }
      return false;
    });
  }
  return false;
}

/**
 * Street or PO Box should be required (Israel)
 */
function addStreetAddressFormValidator() {
  console.log(">>>>  addStreetAddressFormValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.ISRAEL) {
          return new ValidationResult(null, true);
        }
        var addrPOBOXStreetEnabled = [ 'ZS01', 'ZP01', 'CTYA', 'CTYB' ];
        if (addrPOBOXStreetEnabled.includes(FormManager.getActualValue('addrType')) && FormManager.getActualValue('addrTxt') == '' && FormManager.getActualValue('poBox') == '') {
          return new ValidationResult(null, false, 'Please fill-out either Street Address or PO Box.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function fieldsReadOnlyIsrael() {
  console.log(">>>>  fieldsReadOnlyIsrael");
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  var custType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var role = null;

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (reqType == 'C') {
    if (role == 'Requester') {
      FormManager.readOnly('abbrevNm');
      FormManager.readOnly('abbrevLocn');

      FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
      FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);

    } else if (role == 'Processor') {
      FormManager.enable('abbrevNm');
      FormManager.enable('abbrevLocn');
    }
  } else if (reqType == 'U') {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');

    if (role == 'Requester') {
      FormManager.readOnly('sensitiveFlag');
    } else if (role == 'Processor') {

    }
  }
  if (custType == 'PRICU') {
    FormManager.resetValidations('vat');
    FormManager.readOnly('vat');
  }
  FormManager.readOnly('subIndustryCd');
}

function adjustChecklistContact() {
  console.log(">>>>  adjustChecklistContact");
  var custSubScnrio = FormManager.getActualValue('custSubGrp');

  if (custSubScnrio == 'CROSS') {
    document.getElementById("checklistcontact").innerHTML = "your Country ERC";
  } else {
    document.getElementById("checklistcontact").innerHTML = "Israeli ERC (Yifat Singer @ NOTES ID: IJB@il.ibm.com, Yifat Singer/Israel/IBM)";
  }
}

function updateAbbrevNmLocnIsrael(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>>  updateAbbrevNmLocnIsrael");
  var role = null;
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    reqType = FormManager.getActualValue('reqType');
  }
  
  if (reqType != 'C') {
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

function finalizeAbbrevName(fromAddress, scenario, scenarioChanged) {
  console.log(">>>>  finalizeAbbrevName");
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    reqType = FormManager.getActualValue('reqType');
  }
  if (reqType != 'C') {
    return;
  }

  if(scenarioChanged) {
    var installingAddr = getAddressByType('ZI01');
    var countryUseAAddr = getAddressByType('CTYA');
    var finalAbbrevName = '';

    if (scenario == 'THDPT') {// Third Party Sub scenario
      if ((installingAddr != null && installingAddr != '') && (countryUseAAddr != null && countryUseAAddr != '')) {
        var installCustName = '' + installingAddr.custNm1;
        var cntryUseACustName = '' + countryUseAAddr.custNm1;
        if ((installCustName != null && installCustName != '') && (cntryUseACustName != null && cntryUseACustName != '')) {
          finalAbbrevName = cntryUseACustName.substring(0, 8) + ' for ' + installCustName.substring(0, 9);
        }
      }
    } else if (scenario == 'INTSO') {// Internal SO Sub scenario
      if (installingAddr != null && installingAddr != '') {
        var installCustName = '' + installingAddr.custNm1;
        if (installCustName != null && installCustName != '') {
          finalAbbrevName = 'IBM for ' + installCustName.substring(0, 14);
        }
      }
    } else if (countryUseAAddr != null) {
      var cntryUseACustName = '' + countryUseAAddr.custNm1;
      if (cntryUseACustName != null && cntryUseACustName != '') {
        finalAbbrevName = cntryUseACustName.substring(0, 22)
      }
    }
    FormManager.setValue('abbrevNm', finalAbbrevName);
  }
}

function getAddressByType(addrType) {
  console.log(">>>>  getAddressByType");

  var record = null;
  var type = null;
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    type = record.addrType;

    if (typeof (type) == 'object') {
      type = type[0];
    }

    if (type == addrType) {
      return record;
    }
  }
  return null;
}

/**
 * Customer Name Con't or Attention Person should be required (Israel)
 */
function addNameContAttnDeptValidator() {
  console.log(">>>>  addNameContAttnDeptValidator");
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
 * Add Latin character validation for address fields
 */
function addLatinCharValidator() {
  console.log(">>>>  addLatinCharValidator");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var custType = FormManager.getActualValue('custGrp');
  var validateNonLatin = false;

  // latin addresses
  var addrToChkForIL = new Set([ 'ZI01', 'ZS02', 'CTYA', 'CTYB', 'CTYC' ]);

  if (cntry == SysLoc.ISRAEL && addrToChkForIL.has(addrType)) {
    validateNonLatin = true;
  }

  if (validateNonLatin) {
    if (cntry == SysLoc.ISRAEL) {
      checkAndAddValidator('custNm2', Validators.LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.LATIN, [ 'Address Con\'t' ]);
      checkAndAddValidator('dept', Validators.LATIN, [ 'Attention Person' ]);
    }

    checkAndAddValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
    checkAndAddValidator('addrTxt', Validators.LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.LATIN, [ 'City' ]);

  } else {
    FormManager.removeValidator('custNm1', Validators.LATIN);
    FormManager.removeValidator('custNm2', Validators.LATIN);
    FormManager.removeValidator('addrTxt', Validators.LATIN);
    FormManager.removeValidator('addrTxt2', Validators.LATIN);
    FormManager.removeValidator('city1', Validators.LATIN);
    FormManager.removeValidator('dept', Validators.LATIN);
  }
}

/**
 * Add Non-Latin character validation for address fields
 */
function addNonLatinCharValidator() {
  console.log(">>>>  addNonLatinCharValidator");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var custType = FormManager.getActualValue('custGrp');
  var landCntry = FormManager.getActualValue('landCntry');

  // local addresses
  var addrToChkForIL = new Set([ 'ZS01', 'ZP01', 'ZD01' ]);
  var validateLatin = false;

  if (cntry == SysLoc.ISRAEL && addrToChkForIL.has(addrType)) {
    validateLatin = true;
  }

  if (validateLatin) {
    if (cntry == SysLoc.ISRAEL) {
      checkAndAddValidator('custNm2', Validators.NON_LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.NON_LATIN, [ 'Address Con\'t' ]);
      checkAndAddValidator('dept', Validators.NON_LATIN, [ 'Attention Person' ]);
      checkAndAddValidator('custNm1', Validators.NON_LATIN, [ 'Customer Name' ]);
    }
    checkAndAddValidator('addrTxt', Validators.NON_LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.NON_LATIN, [ 'City' ]);

  } else {
    FormManager.removeValidator('custNm1', Validators.NON_LATIN);
    FormManager.removeValidator('custNm2', Validators.NON_LATIN);
    FormManager.removeValidator('addrTxt', Validators.NON_LATIN);
    FormManager.removeValidator('addrTxt2', Validators.NON_LATIN);
    FormManager.removeValidator('city1', Validators.NON_LATIN);
    FormManager.removeValidator('dept', Validators.NON_LATIN);
    FormManager.removeValidator('taxOffice', Validators.NON_LATIN);
  }
}

function validatePoBox() {
  console.log(">>>>  validatePoBox");
  checkAndAddValidator('poBox', Validators.DIGIT, [ 'PO Box' ]);
}

function resetSubIndustryCd() {
  console.log(">>>>  resetSubIndustryCd");
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
      if (FormManager.getActualValue('reqType') == 'U') {
        FormManager.readOnly('subIndustryCd');
      }
      clearInterval(interval);
    } else {
      FormManager.readOnly('subIndustryCd');
    }
  }, 1000);
}

function removeValidationInacNac() {
  console.log(">>>>  removeValidationInacNac");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL) {
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('inacCd', Validators.NUMBER);
  }
}

/**
 * Update address - disable custPhone for not mailing address
 */
function disableCustPhone() {
  console.log(">>>>  disableCustPhone");
  var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
  if (cntryCd == SysLoc.ISRAEL && FormManager.getActualValue('addrType') != 'ZS01') {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
    FormManager.hide('CustPhone', 'custPhone');
  } else {
    FormManager.enable('custPhone');
    FormManager.show('CustPhone', 'custPhone');
  }
}

function addPostalCodeLengthValidator() {
  console.log(">>>>  addPostalCodeLengthValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postal_cd = FormManager.getActualValue('postCd');
        var land_cntry = FormManager.getActualValue('landCntry');
        if (land_cntry == 'IE') {
          if (postal_cd.length != 8) {
            return new ValidationResult(null, false, 'Postal Code should be 8 characters long.');
          }
        } else if (land_cntry == 'IL') {
          if (postal_cd.length < 5 || postal_cd.length == 6) {
            return new ValidationResult(null, false, 'Postal Code should be either 5 or 7 characters long.');
          }
        } else {
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setAbbrvLocCrossBorderScenario() {
  console.log(">>>>  setAbbrvLocCrossBorderScenario");
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
  console.log(">>>>  setAbbrvLocCrossBorderScenarioOnChange");
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

/*
 * EmbargoCode field locked for REQUESTER
 */
function lockEmbargo() {
  console.log(">>>>  lockEmbargo");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (role == 'REQUESTER') {
    if (FormManager.getActualValue('reqType') == 'X') {
      FormManager.readOnly('embargoCd');
    }
    if (FormManager.getActualValue('reqType') == 'C' && issu_cntry == SysLoc.ISRAEL) {
      FormManager.readOnly('embargoCd');
    }
  }
}

function sboLengthValidator() {
  console.log(">>>>  sboLengthValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var sbo = FormManager.getActualValue('salesBusOffCd');
        if (sbo != null && sbo != undefined && sbo != '') {
          if ((sbo.length != 3) || !sbo.match("^[0-9]*$")) {
            return new ValidationResult(null, false, 'SBO should be of 3 numeric characters.');
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function countryUseAISRAEL() {
  console.log(">>>>  countryUseAISRAEL");
  // Lock land country when 'LOCAL' scenario or Update request
  var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  if (cntryCd == SysLoc.ISRAEL) {
    var landCntry = FormManager.getActualValue('landCntry');

    if (FormManager.getActualValue('addrType') == 'CTYA' || FormManager.getActualValue('addrType') == 'ZS01') {
      if (reqType == 'C' && FormManager.getActualValue('custGrp') == 'LOCAL') {
        FormManager.setValue('landCntry', 'IL');
        FormManager.readOnly('landCntry');
      } else if (reqType == 'U' && !cmr.superUser) {
        FormManager.readOnly('landCntry');
      } else {
        FormManager.enable('landCntry');
      }
    } else {
      FormManager.enable('landCntry');
    }
  }
}

function setAddrFieldsBehavior() {
  console.log(">>>>  setAddrFieldsBehavior");
  var addrType = FormManager.getActualValue('addrType');

  if (addrType == 'ZD01' || addrType == 'ZI01' || addrType == 'ZS02' || addrType == 'CTYC') {
    FormManager.hide('POBox', 'poBox');
    FormManager.clearValue('poBox');
    FormManager.removeValidator('addrTxt', Validators.NON_LATIN);

    // Fix for CREATCMR-4927 -- REQUIRED validator not working, if it is the
    // last
    // item in FormManager.GETFIELD_VALIDATIONS['addrTxt']
    // reset validations and add the validators again
    FormManager.resetValidations('addrTxt');
    FormManager.addValidator('addrTxt', Validators.REQUIRED, [ "Street" ], null);
    FormManager.addValidator('addrTxt', Validators.MAXLENGTH, [ "Street", 30 ], null);
    
    if (addrType == 'ZD01') {
      checkAndAddValidator('addrTxt', Validators.NON_LATIN, [ 'Street Address' ]);
    }
  } else {
    FormManager.show('POBox', 'poBox');
    FormManager.removeValidator('addrTxt', Validators.REQUIRED);
  }
}

/**
 * Toggles the REMOVE function on the address tab and prevents removing imported
 * address in update requests
 * 
 * @param value
 * @param rowIndex
 * @param grid
 * @returns
 */
function canRemoveAddress(value, rowIndex, grid) {
  console.log(">>>>  canRemoveAddress");
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd[0];
  var reqType = FormManager.getActualValue('reqType');
  if ('U' == reqType && ('Y' == importInd || 'L' == importInd)) {
    return false;
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  console.log(">>>>  ADDRESS_GRID_showCheck");
  return canRemoveAddress(value, rowIndex, grid);
}

function addEmbargoCodeValidator() {
  console.log(">>>>  addEmbargoCodeValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd').toUpperCase();
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var requestId = FormManager.getActualValue('reqId');
        var validEmbargoValues = [ 'D', 'J', '' ];
        var oldEmbargo = null;
        embargoCd = embargoCd.trim();

        if (embargoCd != null && embargoCd != undefined) {
          qParams = {
              REQ_ID : requestId,
          };
          
          var result = cmr.query('GET.IL.CLIENT_TIER_EMBARGO_CD_OLD_BY_REQID', qParams);
          
          if (result != null && result != '') {
            oldEmbargo = result.ret2 != null ? result.ret2 : '';
          }
          
          if (embargoCd != oldEmbargo) {
            if (!validEmbargoValues.includes(embargoCd)) {
              return new ValidationResult(null, false, 'Embargo Code value should be only D, J or Blank.');
            }
          }
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addPpsceidValidator() {
  console.log(">>>>  addPpsceidValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var ppsceid = FormManager.getActualValue('ppsceid');
        if (ppsceid != null && ppsceid != undefined && ppsceid != '') {
          if (ppsceid.length > 0 && !ppsceid.match("^[0-9a-z]*$")) {
            return new ValidationResult(null, false, ppsceid + ' for PPS CEID is invalid, please enter only digits and lowercase latin characters.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function showHideKuklaField() {
  console.log(">>>>  showHideKuklaField");
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (!custSubGrp) {
    custSubGrp = _pagemodel['custSubGrp'];
  }

  /*
   * In CREATES, KUKLA is visible only for COMMERCIAL (local & cross) & BP and
   * hidden from UI for other scenarios. In UPDATES, KUKLA is visible for all
   * scenarios
   */
  if (reqType == 'C') {
    if (custSubGrp == 'COMME' || custSubGrp == 'CROSS' || custSubGrp == 'BUSPR' || custSubGrp == 'IBMEM') {
      FormManager.show('CustClass', 'custClass');
      if (viewOnlyPage) {
        FormManager.readOnly('custClass');
        return;
      }
      limitCustomerClassValues(custSubGrp);
    } else {
      FormManager.hide('CustClass', 'custClass');
    }
  } else if (reqType == 'U') {
    if (viewOnlyPage) {
      FormManager.readOnly('custClass');
    } else {
      FormManager.show('CustClass', 'custClass');
    }
  }
}

function limitCustomerClassValues(value) {
  console.log(">>>>  limitCustomerClassValues");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!value) {
    value = FormManager.getActualValue('custSubGrp');
  }
  var kuklaValues = null;

  switch (value.toUpperCase()) {
  case 'BUSPR':
    var kuklaValues = [ '41', '42', '43', '44', '45', '46', '47', '48', '49' ];
    break;
  case 'COMME':
    var kuklaValues = [ '11', '33', '35' ];
    break;
  case 'CROSS':
    var kuklaValues = [ '11', '33', '35', '71' ];
    break;
  case 'CROSS':
    var kuklaValues = [ '71' ];
    break;
  default:
    break;
  }

  if (kuklaValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('custClass'), kuklaValues);
    lockCustomerClassByLob(value);
  }
}

function lockCustomerClassByLob(_custType) {
  console.log(">>>>  lockCustomerClassByLob");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!_custType) {
    _custType = FormManager.getActualValue('custSubGrp');
  }
  var lob = FormManager.getActualValue('requestingLob');
  if (_custType == 'COMME' || _custType == 'CROSS') {
    if (lob == 'SCT' || lob == 'IGF') {
      FormManager.enable('custClass');
    } else {
      FormManager.readOnly('custClass');
    }
  } else if (_custType == 'BUSPR') {
    FormManager.enable('custClass');
  } else if (_custType == 'IBMEM') {
    FormManager.readOnly('custClass');
  }
}

function markAddrSaveSuperUser(cntry, addressMode, saving) {
  console.log(">>>>  markAddrSaveSuperUser");
  if (saving) {
    if (FormManager.getActualValue('reqType') == 'U') {
      if (cmr.superUser) {
        FormManager.setValue('bldg', 'SUPERUSER');
      } else {
        FormManager.setValue('bldg', '');
      }
    }
  }
}

function isAddrFieldsUpdatedExcludingPhone(type, addrRecord) {
  console.log(">>>>  isAddrFieldsUpdatedExcludingPhone");
  var reqId = FormManager.getActualValue('reqId');

  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
      ADDR_TYPE : type,
    };

    var origCustNm1;
    var origCustNm2;
    var origLandCntry;
    var origAddrTxt;
    var origPOBox;
    var origCity1;
    var origPostCd;
    var origDept;
    var origAddrTxt2;

    var result = cmr.query('ADDR.GET.OLDADDR.BY_REQID_ADDRTYP_IL', reqParam);

    if (result != null && result != '') {
      origCustNm1 = result.ret1;
      origCustNm2 = result.ret2;
      origLandCntry = result.ret3;
      origAddrTxt = result.ret4;
      origPOBox = result.ret5;
      origCity1 = result.ret6;
      origPostCd = result.ret7;
      origDept = result.ret8;
      origAddrTxt2 = result.ret9;
    }

    var curCustNm1 = (addrRecord.custNm1[0] == null) ? '' : addrRecord.custNm1[0];
    var curCustNm2 = (addrRecord.custNm2[0] == null) ? '' : addrRecord.custNm2[0];
    var curAddrTxt = (addrRecord.addrTxt[0] == null) ? '' : addrRecord.addrTxt[0];
    var curPOBox = (addrRecord.poBox[0] == null) ? '' : addrRecord.poBox[0];
    var curCity1 = (addrRecord.city1[0] == null) ? '' : addrRecord.city1[0];
    var curPostCd = (addrRecord.postCd[0] == null) ? '' : addrRecord.postCd[0];
    var curDept = (addrRecord.dept[0] == null) ? '' : addrRecord.dept[0];
    var curLandCntry = (addrRecord.landCntry[0] == null) ? '' : addrRecord.landCntry[0];
    var curAddrTxt2 = (addrRecord.addrTxt2[0] == null) ? '' : addrRecord.addrTxt2[0];

    if (origCustNm1 != curCustNm1 || origCustNm2 != curCustNm2 || origAddrTxt != curAddrTxt || origPOBox != curPOBox || origCity1 != curCity1 
    || origPostCd != curPostCd || origDept != curDept || origLandCntry != curLandCntry || origAddrTxt2 != curAddrTxt2 ) {
      return true;
    }

  }
  return false;
}

function addISICKUKLAValidator() {
  console.log(">>>>  addISICKUKLAValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var isicCD = FormManager.getActualValue('isicCd');
        var kukla = FormManager.getActualValue('custClass');
        var reqType = FormManager.getActualValue('reqType').toUpperCase();

        var errMessage = '';
        
        if (reqType == 'C') {
          var custType = FormManager.getActualValue('custSubGrp');
          
          if ((custType != 'PRIPE' && custType != 'IBMEM') && isicCD == '9500') {
            if (isicCD == '9500' || kukla == '60' || kukla == '71') {
              errMessage = 'Invalid value for ISIC. ISIC 9500 can only be used in Private Person or IBM Employee scenario.';
            }
          }
       } else {
          var oldISIC = null;
          var oldKukla = null;
          
          var currentISIC = FormManager.getActualValue('isicCd');
          var currentKukla = FormManager.getActualValue('custClass');
          var requestId = FormManager.getActualValue('reqId');
          
          qParams = {
              REQ_ID : requestId,
          };
          var result = cmr.query('GET.IL.ISIC_KUKLA_OLD_BY_REQID', qParams);
          
          if (result != null && result != '') {
            oldISIC = result.ret1 != null ? result.ret1 : '';
            oldKukla = result.ret2 != null ? result.ret2 : '';
          }
          if ((currentISIC == '9500' && (currentKukla != '60' && currentKukla != '71')) || (currentISIC != '9500' && (currentKukla == '60' || currentKukla =='71'))) {
            if (currentISIC != oldISIC || currentKukla != oldKukla) {
              errMessage = 'Invalid value for ISIC/KUKLA.  ISIC value 9500 and KUKLA 60/71 should be linked together.';
            }
          }
        }
        if (errMessage != '' && errMessage.length > 0) {
          return new ValidationResult(null, false, errMessage);
        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function setCapInd() {
  console.log(">>>>  setCapInd");
  if (role.toUpperCase() != 'VIEWER') {
    var reqType = FormManager.getActualValue('reqType');
    if (reqType == 'C') {
      FormManager.readOnly('capInd');
      FormManager.setValue('capInd', true);
    }
  }
}

function lockDunsNo() {
  console.log(">>>>  lockDunsNo");
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

function validateCMRNumberForLegacy() {
  console.log(">>>>  validateCMRNumberForLegacy");
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
          // Skip validation for Prospect Request
          var ifProspect = FormManager.getActualValue('prospLegalInd');
          if (dijit.byId('prospLegalInd')) {
            ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
          }
          console.log("validateCMRNumberForLegacy ifProspect:" + ifProspect);
          if ('Y' == ifProspect) {
            return new ValidationResult(null, true);
          }
          // Validation for Internal Scenario
          if (_custSubGrp == 'INTER' || _custSubGrp == 'INTSO') {
            if (!cmrNo.startsWith("99")) {
              return new ValidationResult(null, false, 'Internal CMR should begin with 99.');
            }
          } else if (_custSubGrp != 'INTER' || _custSubGrp != 'INTSO') {
            if (cmrNo.startsWith("99")) {
              return new ValidationResult(null, false, 'CMR Starting with 99 is allowed for Internal Scenario only.');
            }
          }
          if (cmrNo == '000000') {
            return new ValidationResult(null, false, 'CMR Number should be number only Except -> 000000');
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

function enableCmrNoForProcessor() {
  console.log(">>>>  enableCmrNoForProcessor");
  var cmrNO = FormManager.getActualValue('cmrNo');
  var ifProspect = FormManager.getActualValue('prospLegalInd');
  console.log('enableCmrNoForProcessor... ' + ifProspect);
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'C') {
    return;
  }

  if (dijit.byId('prospLegalInd')) {
    console.log('prospLegalInd... ');
    ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
  }
  console.log("enableCmrNoForProcessor ifProspect:" + ifProspect);
  if (role == 'REQUESTER' || (role != 'REQUESTER' && cmrNO.startsWith('P'))) {
    FormManager.readOnly('cmrNo');
  } else if (role == 'PROCESSOR' && reqType != 'U') {
    FormManager.enable('cmrNo');
  } else {
    FormManager.readOnly('cmrNo');
  }
}

function addCollectionValidator() {
  console.log(">>>>  addCollectionValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var collectionCd = FormManager.getActualValue('collectionCd');
        if (collectionCd != null && collectionCd != undefined && collectionCd != '') {
          if (collectionCd.length > 0 && !collectionCd.match("^[0-9a-zA-Z]*$")) {
            return new ValidationResult(null, false, 'The value of Collection Code is invalid, please use only alphanumeric characters.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function lockCollectionCdForUpdate() {
  console.log(">>>>  lockCollectionCdForUpdate");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var lob = FormManager.getActualValue('requestingLob');
  if (role == 'REQUESTER') {
    if (lob == 'SCT' || lob == 'AR') {
      FormManager.enable('collectionCd');
    } else {
      FormManager.readOnly('collectionCd');
    }
  } else {
    FormManager.enable('collectionCd');
  }
}

function showVatExempt() {
  console.log(">>>>  showVatExempt");
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var vatRequired = isVatRequired();

  if (vatRequired) {
    // show
    FormManager.show('VATExempt', 'vatExempt');
    checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
  } else {
    // hide
    FormManager.hide('VATExempt', 'vatExempt');
    FormManager.removeValidator('vat', Validators.REQUIRED);
  }
}

function isVatRequired() {
  console.log(">>>>  isVatRequired");
  var landedCntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('reqId');
  // MAILING
  var retZs01 = cmr.query('VAT.IL_ADDR_BY_TYPE', {
    REQID : reqId,
    TYPE : 'ZS01'
  });
  if (retZs01 && retZs01.ret1 && retZs01.ret1 != '') {
    landedCntry = retZs01.ret1;
  }
  if (GEOHandler.VAT_RQD_CROSS_LNDCNTRY.indexOf(landedCntry) >= 0) {
    return true;
  }

  // COUNTRY USE A (MAILING)
  landedCntry = '';
  var retCtyA = cmr.query('VAT.IL_ADDR_BY_TYPE', {
    REQID : reqId,
    TYPE : 'CTYA'
  });
  if (retCtyA && retCtyA.ret1 && retCtyA.ret1 != '') {
    landedCntry = retCtyA.ret1;
  }
  if (GEOHandler.VAT_RQD_CROSS_LNDCNTRY.indexOf(landedCntry) >= 0) {
    return true;
  }

  return false;
}

function addDPLChecklistAttachmentValidator() {
  console.log(">>>>  addDPLChecklistAttachmentValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (typeof (_pagemodel) != 'undefined') {
          var zi01ReqId = FormManager.getActualValue('reqId');
          var prolif = false;
          var qParams = {
            REQ_ID : zi01ReqId,
          };
          var result = cmr.query('ADDR.GET.ZI01LAND.BY_REQID', qParams);
          landCntry = result.ret1;
          if (_prolifCountries.includes(landCntry)) {
            prolif = true;
          }
          if (custSubGrp == 'THDPT' && prolif) {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_DPL_CHECKLIST_ATTACHMENT', {
              ID : id
            });

            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, 'For 3rd party scenario, please download the template from Checklist tab, and fill and attach checklist for Installing address.');
            } else {
              return new ValidationResult(null, true);
            }

          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function preTickVatExempt(fromAddress, scenario, scenarioChanged) {
  console.log(">>>>  preTickVatExempt");
  if (FormManager.getActualValue('reqType') == 'C' && scenarioChanged) {
    if (scenario == 'PRIPE') {
      FormManager.setValue('vatExempt', true);
    } else {
      FormManager.setValue('vatExempt', false);
    }
  }
  resetVatRequired();
}

function resetVatRequired() {
  console.log(">>>>  resetVatRequired");
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var vatRequired = isVatRequired();
  if (vatRequired) {
    if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt').get('checked')) {
      FormManager.clearValue('vat');
      FormManager.readOnly('vat');
      FormManager.removeValidator('vat', Validators.REQUIRED);
    } else {
      FormManager.enable('vat');
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    }
  }
  lockUnlockFieldForISrael();
}

/**
 * Downloads the DPL checklist file
 */
function downloadDPLChecklistTemplate() {
  console.log(">>>>  downloadDPLChecklistTemplate");
  var token = new Date().getTime();
  FormManager.setValue('dlDocType', 'TEMPLATE');
  FormManager.setValue('dlTokenId', token);
  FormManager.setValue('dlReqId', FormManager.getActualValue('reqId'));
  cmr.showProgress('Downloading file. Please wait...');
  document.forms['fileTemplateDownloadForm'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);

}

function toggleAddressTypesForIL(cntry, addressMode, details) {
  console.log(">>>>  toggleAddressTypesForIL");
  if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
    var record = null;
    var type = null;
    var shippingCnt = 0;
    var ctyCCnt = 0;
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      if (record == null && _allAddressData != null && _allAddressData[i] != null) {
        record = _allAddressData[i];
      }
      type = record.addrType;
      if (typeof (type) == 'object') {
        type = type[0];
      }
      if (type == 'ZD01') {
        shippingCnt++;
      } else if (type == 'CTYC') {
        ctyCCnt++;
      }
    }

    if (shippingCnt > 0 && shippingCnt != ctyCCnt) {
      cmr.hideNode('radiocont_ZS01');
      cmr.hideNode('radiocont_ZP01');
      cmr.hideNode('radiocont_ZI01');
      cmr.hideNode('radiocont_ZD01');
      cmr.hideNode('radiocont_ZS02');
      cmr.hideNode('radiocont_CTYA');
      cmr.hideNode('radiocont_CTYB');
      cmr.showNode('radiocont_CTYC');
      FormManager.setValue('addrType_CTYC', true)
      setAddrFieldsBehavior();
      setCustPhone();
      countryUseAISRAEL();
    } else {
      cmr.showNode('radiocont_ZS01');
      cmr.showNode('radiocont_ZP01');
      cmr.showNode('radiocont_ZI01');
      cmr.showNode('radiocont_ZD01');
      cmr.showNode('radiocont_ZS02');
      cmr.showNode('radiocont_CTYA');
      cmr.showNode('radiocont_CTYB');
      cmr.hideNode('radiocont_CTYC');
    }
  }
}

function setStreetContBehavior() {
  console.log(">>>>  setStreetContBehavior");
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }

  var street = FormManager.getActualValue('addrTxt');
  if (street != null && street != '') {
    FormManager.enable('addrTxt2');
  } else {
    FormManager.clearValue('addrTxt2');
    FormManager.disable('addrTxt2');
  }
}

function addPostalCdCityValidator() {
  console.log(">>>>  addPostalCdCityValidator");
  // City + PostCd should not exceed 29 characters
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var city = FormManager.getActualValue('city1');
        var postCd = FormManager.getActualValue('postCd');
        var val = city;

        if (postCd != '') {
          val += postCd;
          if (val.length > 29) {
            return new ValidationResult(null, false, 'Total computed length of City and Postal Code should not exceed 29 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addAddressGridValidatorStreetPOBox() {
  console.log(">>>>  addAddressGridValidatorStreetPOBox");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;

          var missingPOBoxStreetAddrs = '';
          var missingAddrType = '';
          var addrStreetPOBox = [ 'ZS01', 'ZP01', 'CTYA', 'CTYB' ];
          
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }

            var addrIsNewOrUpdated = null;
            var reqType = FormManager.getActualValue('reqType');

            if (reqType == 'U') {
              if (record.updateInd[0] == 'U' || record.updateInd[0] == 'N') {
                addrIsNewOrUpdated = true;
              } else {
                addrIsNewOrUpdated = false;
              }
            } else {
              addrIsNewOrUpdated = true;
            }

            var isPOBoxOrStreetFilled = (record.poBox[0] != null && record.poBox[0] != '') || (record.addrTxt[0] != null && record.addrTxt[0] != '');
            if (!isPOBoxOrStreetFilled && addrIsNewOrUpdated) {
              if (missingPOBoxStreetAddrs != '') {
                missingPOBoxStreetAddrs += ', ' + record.addrTypeText[0] + ' (' + record.addrSeq[0] + ') ';
                missingAddrType += ', ' + record.addrType[0];
              } else {
                missingPOBoxStreetAddrs += record.addrTypeText[0] + ' (' + record.addrSeq[0] + ') ';
                missingAddrType += record.addrType[0];
              }
            }
          }

          if (missingPOBoxStreetAddrs != '') {
            for (index = 0; index < addrStreetPOBox.length; index++) {
              if(missingAddrType.includes(addrStreetPOBox[index])) {
                return new ValidationResult(null, false, 'Please fill-out either Street or PO BOX for the following address: ' + missingPOBoxStreetAddrs);
              } else {
                return new ValidationResult(null, false, 'Please fill-out Street for the following address: ' + missingPOBoxStreetAddrs);
              }
            }
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addPairedAddressFieldsMismatchValidatorMailing() {
  getAddrMismatchFieldsValidationError('ZS01', 'CTYA', false);
}

function addPairedAddressFieldsMismatchValidatorBilling() {
  getAddrMismatchFieldsValidationError('ZP01', 'CTYB', false);
}

function addPairedAddressFieldsMismatchValidatorShipping() {
  getAddrMismatchFieldsValidationError('ZD01', 'CTYC', true);
}

function getAddrMismatchFieldsValidationError(localLangType, translatedType, isMulti) {
  console.log(">>>>  getAddrMismatchFieldsValidationError");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var localLangData = null;
          var translatedData = null;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }

            if (!isMulti) {
              if (type == localLangType) {
                localLangData = record;
              } else if (type == translatedType) {
                translatedData = record;
              }
            } else {
              if (type == localLangType) {
                localLangData = record;
                translatedData = getPairedTranslatedAddrData(localLangData, translatedType);
                var mismatchFields = getMismatchFields(localLangData, translatedData);
                if (mismatchFields != '') {
                  return new ValidationResult(null, false, localLangData.addrTypeText[0] + ' (' + localLangData.addrSeq[0] + ') ' + ' and ' + translatedData.addrTypeText[0] + ' ('
                      + translatedData.addrSeq[0] + ') ' + ' mismatch, please make sure the same fields are filled.' + ' Mismatched fields: ' + mismatchFields);
                }
              }
            }
          }

          var mismatchFields = getMismatchFields(localLangData, translatedData);
          if (mismatchFields != '') {
            return new ValidationResult(null, false, localLangData.addrTypeText[0] + ' and ' + translatedData.addrTypeText[0] + ' mismatch, please make sure the same fields are filled.'
                + ' Mismatched fields: ' + mismatchFields);
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function getPairedTranslatedAddrData(localLangData, translatedType) {
  console.log(">>>>  getPairedTranslatedAddrData");
  var translatedData = null;

  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    if (record == null && _allAddressData != null && _allAddressData[i] != null) {
      record = _allAddressData[i];
    }
    type = record.addrType;
    if (typeof (type) == 'object') {
      type = type[0];
    }

    if (type == translatedType && localLangData.addrSeq[0] != null && record.pairedSeq[0] != null) {
      if (localLangData.addrSeq[0].padStart(5, '0') == record.pairedSeq[0].padStart(5, '0')) {
        translatedData = record;
        break;
      }
    }
  }

  return translatedData;
}

function getMismatchFields(localLangData, translatedData) {
  console.log(">>>>  getMismatchFields");
  var mismatchFields = '';
  if (localLangData == null || translatedData == null) {
    return mismatchFields;
  }
  if (isAddrPairNewOrUpdated(localLangData, translatedData) && !hasMatchingFieldsFilled(localLangData.addrTxt[0], translatedData.addrTxt[0])) {
    mismatchFields += 'Street';
  }
  if (isAddrPairNewOrUpdated(localLangData, translatedData) && !hasMatchingFieldsFilled(localLangData.poBox[0], translatedData.poBox[0])) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'PO BOX';
  }
  if (isAddrPairNewOrUpdated(localLangData, translatedData) && !hasMatchingFieldsFilled(localLangData.dept[0], translatedData.dept[0])) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Att. Person';
  }
  if (isAddrPairNewOrUpdated(localLangData, translatedData) && !hasMatchingFieldsFilled(localLangData.custNm1[0], translatedData.custNm1[0])) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Customer Name';
  }
  if (isAddrPairNewOrUpdated(localLangData, translatedData) && !hasMatchingFieldsFilled(localLangData.postCd[0], translatedData.postCd[0])) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Postal Code';
  }
  if (isAddrPairNewOrUpdated(localLangData, translatedData) && !hasMatchingFieldsFilled(localLangData.city1[0], translatedData.city1[0])) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'City';
  }
  return mismatchFields;
}

function hasMatchingFieldsFilled(localLangField, translatedField) {
  console.log(">>>>  hasMatchingFieldsFilled");
  if (localLangField != '' && localLangField != null) {
    if (translatedField == '' || translatedField == null) {
      return false;
    }
  }

  if (translatedField != '' && translatedField != null) {
    if (localLangField == '' || localLangField == null) {
      return false;
    }
  }

  return true;
}

function isAddrPairNewOrUpdated(localLang, translated) {
  console.log(">>>>  isAddrPairNewOrUpdated");
  var reqType = FormManager.getActualValue('reqType');
  if(reqType == 'C') {
    return true;
  }
  if(addrNewOrUpdated(localLang) || addrNewOrUpdated(translated)) {
    return true;
  }
  return false;
}

function addrNewOrUpdated(addrData) {
  console.log(">>>>  addrNewOrUpdated");
  if(addrData != null) {
    return (addrData.updateInd[0] == 'N' || addrData.updateInd[0] == 'U');  
  }
  return false;
}

function validateSalesRep() {
  console.log(">>>>  validateSalesRep");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var salesRep = FormManager.getActualValue('repTeamMemberNo');

        if (salesRep != null && salesRep != undefined && salesRep != '') {
          if ((salesRep.length != 6) || !salesRep.match("^[0-9]*$")) {
            return new ValidationResult(null, false, 'Sales Rep should be of 6 numeric characters.');
          }
          var modCust =  FormManager.getActualValue('miscBillCd');
          var salesRepInt = parseInt(salesRep);
          var minRange = 220;
          var maxRange = 239;
          
          if (modCust != null && modCust != undefined && modCust != '') {
            if (modCust == 'NO' || modCust == 'IBM') {
              if (salesRepInt >= minRange && salesRepInt <= maxRange) {
                return new ValidationResult(null, false, 'Only ESW WTC IL - IBM business type of MOD scenario can use Sales Rep from range 000220-000239. Please change it.');
              }
            } else if (modCust == 'WTC') {
              if (salesRepInt < minRange || salesRepInt > maxRange) {
                return new ValidationResult(null, false, 'You selected a type of MOD customer that can only use Sales Rep from range 000220-000239. Please change it.');
              }
            }
          }
          
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateEnterpriseNo() {
  console.log(">>>>  validateEnterpriseNo");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var enterpriseNo = FormManager.getActualValue('enterprise');

        if (enterpriseNo != null && enterpriseNo != undefined && enterpriseNo != '') {
          if (!enterpriseNo.match("^[0-9]*$")) {
            return new ValidationResult(null, false, 'Enterprise Number should be numeric only.');
          } else if ((enterpriseNo.length != 6)) {
            return new ValidationResult(null, false, 'Enterprise Number should be 6 digit long.');
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}


function setEnterpriseSalesRepSBO() {
  console.log(">>>>  setEnterpriseSalesRepSBO");
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');

  if (isuCd == '34' && clientTier == 'Q') {
    FormManager.setValue('enterprise', '006510');
    FormManager.setValue('salesBusOffCd', '006');
    FormManager.setValue('repTeamMemberNo', '000651');
  } else if (isuCd == '36' && clientTier == 'Y') {
    FormManager.setValue('enterprise', '');
    FormManager.setValue('salesBusOffCd', '006');
    FormManager.setValue('repTeamMemberNo', '000651');
  } else if (isuCd == '32' && clientTier == 'T') {
    FormManager.setValue('enterprise', '985985');
    FormManager.setValue('salesBusOffCd', '006');
    FormManager.setValue('repTeamMemberNo', '000651');
  }else if (isuCd == '21' && clientTier == '') {
    FormManager.setValue('enterprise', '985999');
    FormManager.setValue('salesBusOffCd', '009');
    FormManager.setValue('repTeamMemberNo', '000993');
  } else if (isuCd == '5K' && clientTier == '') {
    FormManager.setValue('enterprise', '985999');
    FormManager.setValue('salesBusOffCd', '006');
    FormManager.setValue('repTeamMemberNo', '000651');
  }else {
    FormManager.setValue('enterprise', '');
    FormManager.setValue('salesBusOffCd', '');
    FormManager.setValue('repTeamMemberNo', '');
  }
  addRemoveValidator();
  lockUnlockFieldForISrael();
}


function checkCmrUpdateBeforeImport() {
  console.log(">>>>  checkCmrUpdateBeforeImport");
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
          return new ValidationResult(null, true);
        }

        var resultsCC = cmr.query('GETUPTSRDC', {
          COUNTRY : cntry,
          CMRNO : cmrNo,
          MANDT : cmr.MANDT
        });

        if (resultsCC != null && resultsCC != undefined && resultsCC.ret1 != '') {
          uptsrdc = resultsCC.ret1;
        }

        var results11 = cmr.query('GETUPTSADDR', {
          REQ_ID : reqId
        });
        if (results11 != null && results11 != undefined && results11.ret1 != '') {
          lastupts = results11.ret1;
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

function requireSalesRepEnterpriseSBOByRole() {
  console.log(">>>>  requireSalesRepEnterpriseSBOByRole");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (role == 'PROCESSOR') {
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise Number' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ], 'MAIN_IBM_TAB');
  }
}
function setSalesRepEnterpriseNoSBO(fromAddress, scenario, scenarioChanged) {
  console.log(">>>>  setSalesRepEnterpriseNoSBO");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (reqType != 'C') {
    return;
  }
  if (reqType == 'C' && scenarioChanged && !fromAddress) {

    if (scenario == 'BUSPR' || scenario == 'INTER' || scenario == 'INTSO' || scenario == 'IBMEM') {
      FormManager.setValue('repTeamMemberNo', '000993');
      FormManager.setValue('enterprise', '985999');
      FormManager.setValue('salesBusOffCd', '009');
      requireSalesRepEnterpriseSBOByRole();
    } else {
      FormManager.setValue('repTeamMemberNo', '000651');
      FormManager.setValue('enterprise', '006510');
      FormManager.setValue('salesBusOffCd', '006');
      requireSalesRepEnterpriseSBOByRole();
    }
  } 
  
  lockUnlockFieldForISrael();
}

function lockCMROwner() {
  console.log(">>>>  lockCMROwner");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  FormManager.readOnly('cmrOwner');
}


function executeBeforeSubmit(action) {
  console.log(">>>>  executeBeforeSubmit");
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    var addrMismatchInUpdateErr = getAddrMismatchInUpdateMsg();
    if (addrMismatchInUpdateErr != '' && action != 'CRU') {
      cmr.showConfirm('showAddressVerificationModal()', addrMismatchInUpdateErr, 'Warning', null, {
        OK : 'Yes',
        CANCEL : 'No'
      });
    } else if (addrMismatchInUpdateErr != '' && action == 'CRU') {
      cmr.showConfirm('doYourAction()', addrMismatchInUpdateErr, 'Warning', null, {
        OK : 'Yes',
        CANCEL : 'No'
      });
    } else if(action == 'CRU') {
      doYourAction();
    } else {
      showAddressVerificationModal();
    }
  } else {
    showAddressVerificationModal();
  }
}

function getAddrMismatchInUpdateMsg() {
  console.log(">>>>  getAddrMismatchInUpdateMsg");
  var errorMsg = '';

  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
    var record = null;
    var type = null;
    var updateInd = null;
    var updatedLocalAddrs = [];
    var updatedTransAddrsPair = [];
    var allTransAddrsMap = new Map();
    var allLocalAddrMap = new Map();
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
      if ((type == 'ZS01' || type == 'ZP01' || type == 'ZD01')) {
        allLocalAddrMap.set(getKeyPrefix(type) + record.addrSeq[0].padStart(5, '0'), record);
        if(updateInd == 'U') {
          if(type == 'ZS01' && !isAddrFieldsUpdatedExcludingPhone(type, record)) {
            continue;
          }
          updatedLocalAddrs.push(getKeyPrefix(type) + record.addrSeq[0].padStart(5, '0'));  
        }
      } else if ((type == 'CTYA' || type == 'CTYB' || type == 'CTYC')) {
        allTransAddrsMap.set(getKeyPrefix(type) + record.pairedSeq[0].padStart(5, '0'), record);
        allTransAddrsMap.set(getKeyPrefix(type) + record.addrSeq[0].padStart(5, '0'), record);
        if(updateInd == 'U') {
          updatedTransAddrsPair.push(getKeyPrefix(type) + record.pairedSeq[0].padStart(5, '0'));  
        }
      }
    }
     var updateMismatchLocal = updatedLocalAddrs.filter(val => !updatedTransAddrsPair.includes(val));
     var updateMismatchTrans = updatedTransAddrsPair.filter(val => !updatedLocalAddrs.includes(val));

    if (updateMismatchTrans.length != 0 || updateMismatchLocal.length != 0) {
      for (var l = 0; l < updateMismatchLocal.length; l++) {
        if(errorMsg != '') {
          errorMsg += '<br>';
        }
        var mismatchKey = updateMismatchLocal[l];
        
        var addrSeqLocal = '';
        if (allLocalAddrMap.get(mismatchKey) != null) {
          addrSeqLocal = allLocalAddrMap.get(mismatchKey).addrSeq[0];
        }
        
        var addrSeqTrans = '';
        if(allTransAddrsMap.get(mismatchKey) != null) {
          addrSeqTrans = (allTransAddrsMap.get(mismatchKey).addrSeq[0]);
        }
        
        if(addrSeqLocal != '' && addrSeqTrans != '') {
          var addrTextLocal = (allLocalAddrMap.get(mismatchKey).addrTypeText[0]);
          var addrTextTrans = (allTransAddrsMap.get(mismatchKey).addrTypeText[0]);
          errorMsg += '<br>Address with sequence ' + addrSeqLocal + ' from pair ' + addrSeqLocal + ' (' + addrTextLocal + ')' + ' - ' + addrSeqTrans + ' ('+ addrTextTrans +') ' + ' was updated, but sequence ' + addrSeqTrans  + ' was not.';
        }
      }

      for (var t = 0; t < updateMismatchTrans.length; t++) {
        if(errorMsg != '') {
          errorMsg += '<br>';
        }
        
        var mismatchKey = updateMismatchTrans[t];
        
        var addrSeqTrans = '';
        if(allTransAddrsMap.get(mismatchKey) != null) {
          addrSeqTrans = (allTransAddrsMap.get(mismatchKey).addrSeq[0]);
        }

        var addrSeqLocal = '';
        if(allLocalAddrMap.get(mismatchKey) != null) {
          addrSeqLocal = (allLocalAddrMap.get(mismatchKey).addrSeq[0]);
        }

        if(addrSeqLocal != '' && addrSeqTrans != '') {
          var addrTextTrans = (allTransAddrsMap.get(mismatchKey).addrTypeText[0]);
          var addrTextLocal = (allLocalAddrMap.get(mismatchKey).addrTypeText[0]);
          errorMsg += '<br>Address with sequence ' + addrSeqTrans + ' from pair ' + addrSeqLocal + ' (' + addrTextLocal + ') ' + ' - ' + addrSeqTrans + ' ('+ addrTextTrans +') ' + ' was updated, but sequence ' + addrSeqLocal + ' was not.';
        }
      }
      
      if(errorMsg != '') {
        errorMsg += '<br><br>Do you want to proceed with this request?';
      }
    }
  }
  return errorMsg;
}

function getKeyPrefix(addrType) {
  console.log(">>>>  getKeyPrefix");
  if(addrType == 'ZS01' || addrType == 'CTYA') {
    return 'MAIL_';
  } else if (addrType == 'ZP01' || addrType == 'CTYB') {
    return 'BILL_';
  } else if (addrType == 'ZD01' || addrType == 'CTYC') {
    return 'SHIP_';
  }
}

function addAddrFieldsLimitation() {
  console.log(">>>>  addAddrFieldsLimitation");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var fieldsFilledCount = 0;
        if(FormManager.getActualValue('custNm1') != '') {
          fieldsFilledCount++;
        }
        
        if(FormManager.getActualValue('custNm2') != '') {
          fieldsFilledCount++;
        }  
       
        if(FormManager.getActualValue('addrTxt') != '') {
          fieldsFilledCount++;
        }
        
        if(FormManager.getActualValue('addrTxt2') != '') {
          fieldsFilledCount++;
        }

        if(FormManager.getActualValue('city1') != '' || FormManager.getActualValue('postCd') != '') {
          fieldsFilledCount++;
        }
        
        if(FormManager.getActualValue('poBox') != '') {
          fieldsFilledCount++;
        }
        
        if(FormManager.getActualValue('dept') != '') {
          fieldsFilledCount++;
        }
        
        if (FormManager.getActualValue('landCntry') != "IL") {
          fieldsFilledCount++;
        }

        console.log('fields filled count address: ' + fieldsFilledCount);
        if (fieldsFilledCount == 7) {
          return new ValidationResult(null, false, 'Please remove value from one of the optional fields. You exceeded limitation which allows only 6 lines to be sent to DB2.');
        } else if (fieldsFilledCount == 8) {
          return new ValidationResult(null, false, 'Please remove value from two of the optional fields. You exceeded limitation which allows only 6 lines to be sent to DB2.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function showVerificationModal() {
  cmr.showModal('addressVerificationModal');
}


function setCTCByScenario(fromAddress, scenario, scenarioChanged) {
  console.log(">>>>  setCTCByScenario");

  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var isuCd = FormManager.getActualValue('isuCd');
  var custType = FormManager.getActualValue('custSubGrp');
  
  if (isuCd != '' & isuCd != "34") {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  } else {
    checkAndAddValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ]);
  }
  
  if (scenarioChanged) {
    if (scenario == 'COMME') {
      FormManager.setValue('clientTier', 'Q');
    } else if (scenario == 'BUSPR') {
      FormManager.setValue('clientTier', '');
    } else if (scenario == 'INTER') {
      FormManager.setValue('clientTier', '');
    } else if (scenario == 'INTSO') {
      FormManager.setValue('clientTier', '');
    } else if (scenario == 'PRIPE') {
      FormManager.setValue('clientTier', 'Q');
    } else if (scenario == 'GOVRN') {
      FormManager.setValue('clientTier', 'Q');
    } else if (scenario == 'THDPT') {
      FormManager.setValue('clientTier', 'Q');
    } else if (scenario == 'CROSS') {
      FormManager.setValue('clientTier', 'Q');
    }
  }
  addRemoveValidator();
  lockUnlockFieldForISrael();
}

function showVatInfoOnLocal() {
  console.log(">>>>  showVatInfoOnLocal");
  
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  
  var custGrp = FormManager.getActualValue('custGrp');
  if (custGrp == 'LOCAL') {
    $("span[id='vatInfo'] img[class='cmr-info-bubble']").show();
  } else {
    $("span[id='vatInfo'] img[class='cmr-info-bubble']").hide();
  }
}


function getExitingValueOfCTCAndIsuCD() {
  console.log(">>>> getExitingValueOfCTCAndIsuCD");
  var reqType = FormManager.getActualValue('reqType');
  var requestId = FormManager.getActualValue('reqId');

  if (reqType != 'U' || _oldIsuCd != null || _oldCtc != null) {
    return;
  }

  var result = cmr.query('GET.CLIENT_TIER_EMBARGO_CD_OLD_BY_REQID', {
    REQ_ID : requestId,
  });

  if (result != null && result != '') {
    _oldCtc = result.ret1;
    _oldIsuCd = result.ret3;
    _oldEnt = result.ret4;

  }

}
function validateIsuClientTier() {
  console.log(">>>>  validateIsuClientTier"); 
  var reqType = FormManager.getActualValue('reqType');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCD323436Set = new Set([ '32', '34', '36' ])

  getExitingValueOfCTCAndIsuCD();
  if (reqType == 'U' && _oldIsuCd == isuCd && _oldCtc == clientTier) {
    return new ValidationResult(null, true);
  } else if (isuCD323436Set.has(isuCd) && clientTier == '') {
    return new ValidationResult(null, true);
  } else if (!isuCD323436Set.has(isuCd) && clientTier != '') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can be blank only.');
  } else if (isuCd == '32' && clientTier != 'T') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can only accept \'T\'.');
  } else if (isuCd == '34' && clientTier != 'Q') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can only accept \'Q\'.');
  } else if (isuCd == '36' && clientTier != 'Y') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can only accept \'Y\'.');
  } else if (reqType == 'C' && FormManager.getActualValue('enterprise') != '') {
    return validatorEnterpriseIL();
  } else {
    return new ValidationResult(null, true);
  }
}

function validatorEnterpriseIL(){
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var enterprise = FormManager.getActualValue('enterprise');
  var isuCdSet1 = new Set([ '21', '5K' ]);
  
  if (isuCdSet1.has(isuCd) && enterprise != '985999') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'985999\'.');
  } else if (isuCd == '34' && enterprise != '006510') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'006510\'.');
  } else if (isuCd == '32' && enterprise != '985985') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'985985\'.');
  } else if (isuCd == '36' && enterprise != '003290' && enterprise != '010023') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'003290\', \'010023\'.');
  } else {
    return new ValidationResult(null, true);
  }
}



function clientTierValidator() {
  console.log(">>>>  clientTierValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
          return validateIsuClientTier();
    
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function limitMODValues() {
  console.log(">>>>  limitMODValues");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var modValues = [ 'NO', 'IBM', 'WTC' ];
  if (modValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('miscBillCd'), modValues);
  }
}

function requiredSearchTerm(){
  console.log(">>>>  requiredSearchTerm");
  if(FormManager.getActualValue('reqType') == 'C'){
    FormManager.removeValidator('searchTerm', Validators.REQUIRED);
  }
}

function validateExistingCMRNo() {
  console.log(">>>>  validateExistingCMRNo");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking requested cmr number il...');
        var reqType = FormManager.getActualValue('reqType');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var action = FormManager.getActualValue('yourAction');
        var _custSubGrp = FormManager.getActualValue('custSubGrp');
        if (reqType == 'C' && cmrNo) {
          if (cmrNo.startsWith('P')) { 
            return new ValidationResult(null, true);
          }          
          var exists = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC', {
            COUNTRY : cntry,
            CMR_NO : cmrNo,
            MANDT : cmr.MANDT
          });
          
          var results2 = cmr.query('GET.CHECK_EXISTS_CMR_NO', {
            CMR_NO : cmrNo,
            CNTRY : cntry
          });
          
          if (exists && exists.ret1 && action != 'PCM') {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'The requested CMR Number ' + cmrNo + ' already exists in the system.');
          } else if (results2.ret1 != null && action != 'PCM') {
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

function inacValidator() {
  console.log(">>>>  inacValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var inacCd = FormManager.getActualValue('inacCd');
        if (inacCd != null && inacCd != undefined && inacCd != '') {
          if (inacCd.length != 4) {
            return new ValidationResult(null, false, 'INAC should be exactly 4 characters.');
          }
          
          if(!inacCd.match("^[0-9]*$")) {
            var firstTwoChars = inacCd.substring(0, 2);
            var lastTwoChars = inacCd.substring(2);
            if (!firstTwoChars.match(/^[A-Z]*$/) || !lastTwoChars.match(/^[0-9]+$/)) {
              return new ValidationResult(null, false, 'INAC should be 4 digits or two letters (Uppercase Latin characters) followed by 2 digits.');
            }
          }
          return new ValidationResult(null, true);
        } 
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateLandedCountry() {
  console.log(">>>>  validateLandedCountry");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
    var reqType = FormManager.getActualValue('reqType');
    var landCntry = FormManager.getActualValue('landCntry');

    if (reqType == 'U' && landCntry == '') {
          return new ValidationResult(null, false, 'Landed Country cannot be blanked out.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}
  
function validateAddressShippingPairing() {
  console.log(">>>>  validateAddressShippingPairing");
  // shipping pair should not contain any duplicates
  FormManager.addFormValidator((function() {
    
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        
        if (FormManager.getActualValue('cmrIssuingCntry') != '755' || reqType != 'U') {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var updateInd = null;
          var shippingPairs = [];
          var validShippingPairs = true;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }

            if (type == 'CTYC') {
              if(record != null && record.pairedSeq != null && record.pairedSeq[0] != '') {
                shippingPairs.push(record.pairedSeq[0]);
              } else {
                validShippingPairs = false;
                break;
              }
            } 
          }
          
          if(shippingPairs.length > 1 && hasDuplicates(shippingPairs)) {
            validShippingPairs = false;
          }
          
          if (!validShippingPairs) {
            return new ValidationResult(null, false, 'There is issue with shipping address and Use C translation not being linked correctly. Please contact CMDE, the CMR needs to be reviewed and fixed.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function hasDuplicates(array) {
  return (new Set(array)).size !== array.length;
}

function lockUnlockFieldForISrael() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custGrpSet1 = new Set([ 'COMME','GOVRN','THDPT']);
  var _custGrpSet2 = new Set(['BUSPR']);
  var reqType = FormManager.getActualValue('reqType');
  
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || (reqType == 'C' && !_custGrpSet1.has(custSubGrp) && custSubGrp != 'BUSPR')) {
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesTeamCd');
    FormManager.readOnly('salesBusOffCd');
    FormManager.clearValue('ppsceid');
    FormManager.readOnly('ppsceid');
  } 
  else if (_custGrpSet1.has(custSubGrp)) {
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    FormManager.enable('isuCd');
    FormManager.enable('clientTier');
    FormManager.enable('enterprise');
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('salesTeamCd');
    FormManager.enable('salesBusOffCd');
    FormManager.clearValue('ppsceid');
    FormManager.readOnly('ppsceid');
  }
  if(_custGrpSet2.has(custSubGrp)){
    FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'ppsceid' ], 'MAIN_IBM_TAB');
    FormManager.enable('ppsceid');
  }
}

function addRemoveClientTierValidator() {
  console.log(">>>> addRemoveClientTierValidator");
  var isuCd = FormManager.getActualValue('isuCd');
  FormManager.resetValidations('clientTier');
  if (isuCd != '32' && isuCd != '34' && isuCd != '36') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  } else {
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
  }
}

function addRemoveEnterperiseValidator() {
  var reqType = FormManager.getActualValue('reqType');
  FormManager.resetValidations('enterprise');
  if (reqType == 'C' || (reqType == 'U' && _oldEnt != null && _oldEnt != '')) {
    FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise Number' ], 'MAIN_IBM_TAB');
  }

}

function addRemoveValidator() {
  addRemoveClientTierValidator();
  addRemoveEnterperiseValidator();

}

function addressQuotationValidatorIsrael() {
  // CREATCMR-788 & CREATCMR-7972
  if (!['ZS01', 'ZP01', 'ZD01'].includes(FormManager.getActualValue('addrType'))) {
    FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
    FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
    FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street' ]);
    FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Street Cont' ]);
    FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
    FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
    FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'Att. Person' ]);
    FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
    FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);
  } else {
    FormManager.removeValidator('abbrevNm', Validators.NO_QUOTATION);
    FormManager.removeValidator('abbrevLocn', Validators.NO_QUOTATION);
    FormManager.removeValidator('custNm1', Validators.NO_QUOTATION);
    FormManager.removeValidator('custNm2', Validators.NO_QUOTATION);
    FormManager.removeValidator('addrTxt', Validators.NO_QUOTATION);
    FormManager.removeValidator('addrTxt2', Validators.NO_QUOTATION);
    FormManager.removeValidator('city1', Validators.NO_QUOTATION);
    FormManager.removeValidator('postCd', Validators.NO_QUOTATION);
    FormManager.removeValidator('dept', Validators.NO_QUOTATION);
    FormManager.removeValidator('poBox', Validators.NO_QUOTATION);
    FormManager.removeValidator('custPhone', Validators.NO_QUOTATION);
  }
}
dojo.addOnLoad(function() {
  GEOHandler.EMEA = [ SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.TURKEY, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.ITALY ];
  console.log('adding Israel functions...');
  GEOHandler.addAddrFunction(addEMEALandedCountryHandler, GEOHandler.EMEA);
  GEOHandler.enableCopyAddress(GEOHandler.EMEA, validateEMEACopy, [ 'ZD01', 'ZI01' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.EMEA);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.EMEA);
  GEOHandler.setRevertIsicBehavior(false);

  // Israel Specific
  GEOHandler.addAfterConfig(afterConfigForIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.registerValidator(addILAddressTypeValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(validateExistingCMRNo, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.ISRAEL, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addStreetAddressFormValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addEmbargoCodeValidator, SysLoc.ISRAEL, null, true);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [ SysLoc.ISRAEL ], null, true);

  GEOHandler.addAfterConfig(fieldsReadOnlyIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(fieldsReadOnlyIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(updateAbbrevNmLocnIsrael, [ SysLoc.ISRAEL ]);
  // GEOHandler.registerValidator(addNameContAttnDeptValidator, [ SysLoc.ISRAEL
  // ], null, true);
  GEOHandler.addAddrFunction(setAddrFieldsBehavior, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(addLatinCharValidator, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(addNonLatinCharValidator, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(resetSubIndustryCd, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(removeValidationInacNac, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(disableCustPhone, [ SysLoc.ISRAEL ]);
  GEOHandler.registerValidator(addPostalCodeLengthValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.addAfterConfig(setAbbrvLocCrossBorderScenario, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(setAbbrvLocCrossBorderScenarioOnChange, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(setChecklistStatus, [ SysLoc.ISRAEL ]);
  GEOHandler.registerValidator(addILChecklistValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addAddressLandedPairingValidatorMailing, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addAddressLandedPairingValidatorBilling, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addAddressLandedPairingValidatorShipping, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addPairedAddressFieldsMismatchValidatorMailing, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addPairedAddressFieldsMismatchValidatorBilling, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addPairedAddressFieldsMismatchValidatorShipping, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(validatePairedAddrFieldNumericValue, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(validateAddressShippingPairing, [ SysLoc.ISRAEL ], null, true);

  GEOHandler.addAfterConfig(setCapInd, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(lockDunsNo, [ SysLoc.ISRAEL ]);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.EMEA, GEOHandler.ROLE_PROCESSOR, true);

  // For EmbargoCode
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.EMEA);

  GEOHandler.registerValidator(sboLengthValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.addAfterConfig(addHandlersForIL, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(countryUseAISRAEL, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(validatePoBox, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(markAddrSaveSuperUser, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(toggleAddressTypesForIL, [ SysLoc.ISRAEL ]);

  GEOHandler.registerValidator(addPpsceidValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(validateCMRNumberForLegacy, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.addAfterConfig(enableCmrNoForProcessor, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(showVatInfoOnLocal, [ SysLoc.ISRAEL ]);

  GEOHandler.addAfterConfig(showHideKuklaField, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(showHideKuklaField, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(lockCustomerClassByLob, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(finalizeAbbrevName, [ SysLoc.ISRAEL ]);
//  GEOHandler.addAfterTemplateLoad(adjustChecklistContact, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(setSalesRepEnterpriseNoSBO, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(lockCMROwner, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(setCTCByScenario, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(showVatInfoOnLocal, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(setChecklistStatus, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(requiredSearchTerm, [ SysLoc.ISRAEL ]);

  GEOHandler.registerValidator(addISICKUKLAValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addCollectionValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addPostalCdCityValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addDPLChecklistAttachmentValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addAddressGridValidatorStreetPOBox, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(validateSalesRep, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(validateEnterpriseNo, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addAddrFieldsLimitation, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(clientTierValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(inacValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(validateLandedCountry, [ SysLoc.ISRAEL ], null, true);
  
  GEOHandler.addAfterTemplateLoad(preTickVatExempt, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(showVatExempt, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(setStreetContBehavior, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(requireSalesRepEnterpriseSBOByRole, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(lockCMROwner, [ SysLoc.ISRAEL ]);
  
  GEOHandler.addAfterConfig(addChecklistBtnHandler, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(checkChecklistButtons, [ SysLoc.ISRAEL ]);
  
});