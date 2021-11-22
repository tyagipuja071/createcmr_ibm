/* Register Israel Javascripts */
var _CTCHandlerIL = null;
var _vatExemptHandler = null;
var _gtcAddrTypeHandlerIL = [];
var _gtcAddrTypesIL = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01', 'ZS02', 'CTYA', 'CTYB', 'CTYC' ];
var _prolifCountries = [ 'AF', 'AM', 'AZ', 'BH', 'BY', 'KH', 'CN', 'CU', 'EG', 'GE', 'IR', 'IQ', 'IL', 'JO', 'KZ', 'KP', 'KW', 'KG', 'LA', 'LB', 'LY', 'MO', 'MD', 'MN', 'MM', 'OM', 'PK', 'QA', 'RU',
    'SA', 'SD', 'SY', 'TW', 'TJ', 'TM', 'UA', 'AE', 'UZ', 'VE', 'VN', 'YE' ];
var _requestingLOBHandler = null;
var _streetHandler = null;

function addHandlersForIL() {
  for (var i = 0; i < _gtcAddrTypesIL.length; i++) {
    _gtcAddrTypeHandlerIL[i] = null;
    if (_gtcAddrTypeHandlerIL[i] == null) {
      _gtcAddrTypeHandlerIL[i] = dojo.connect(FormManager.getField('addrType_' + _gtcAddrTypesIL[i]), 'onClick', function(value) {
        countryUseAISRAEL();
        setAddrFieldsBehavior();
      });
    }
  }

  if (_CTCHandlerIL == null) {
    _CTCHandlerIL = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setEnterpriseSalesRepSBP();
    });
  }

  if (_streetHandler == null) {
    _streetHandler = dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {
      setStreetContBehavior();
    });
  }

}

function addEMEALandedCountryHandler(cntry, addressMode, saving, finalSave) {
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
}

function setChecklistStatus() {

  console.log('checklist status..');
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
    } else {
      document.getElementById("checklistStatus").innerHTML = "Complete";
      FormManager.setValue('checklistStatus', "Complete");
    }
  }
}

function addILChecklistValidator() {

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
        }

        // add check for checklist on DB
        var reqId = FormManager.getActualValue('reqId');
        var record = cmr.getRecord('GBL_CHECKLIST', 'ProlifChecklist', {
          REQID : reqId
        });
        if (!record || !record.sectionA1) {
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
  FormManager.setValue('codFlag', '3');
  FormManager.readOnly('codFlag');
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
  }
  return null;
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
      tierValues = [ '4', '6', 'A', 'E', 'Q', 'V', 'Y', 'Z' ];
    } else if (value != '') {
      tierValues = [ '7', 'Z' ];
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
              mailingUpdated = isAddrFieldsUpdatedExcludingLanded('ZS01', record);
            } else if (type == 'CTYA' && updateInd == 'U') {
              ctyACntUpdated = isAddrFieldsUpdatedExcludingLanded('CTYA', record);
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

function addAddressLandedPairingValidatorMailing() {
  console.log("addAddressPairingValidatorMailing..............");
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
              billingUpdated = isAddrFieldsUpdatedExcludingLanded('ZP01', record);
            } else if (type == 'CTYB' && updateInd == 'U') {
              ctyBCntUpdated = isAddrFieldsUpdatedExcludingLanded('CTYB', record);
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

function addAddressLandedPairingValidatorBilling() {
  console.log("addAddressPairingValidatorBilling..............");
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
  console.log("addAddressPairingValidatorShipping..............");
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
              mailingUpdated = isAddrFieldsUpdatedExcludingLanded('ZS01', record);
            } else if (type == 'CTYA' && updateInd == 'U') {
              ctyACntUpdated = isAddrFieldsUpdatedExcludingLanded('CTYA', record);
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
              billingUpdated = isAddrFieldsUpdatedExcludingLanded('ZP01', record);
            } else if (type == 'CTYB' && updateInd == 'U') {
              ctyBCntUpdated = isAddrFieldsUpdatedExcludingLanded('CTYB', record);
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
}

function adjustChecklistContact() {
  var custSubScnrio = FormManager.getActualValue('custSubGrp');

  if (custSubScnrio == 'CROSS') {
    document.getElementById("checklistcontact").innerHTML = "your Country ERC";
  } else {
    document.getElementById("checklistcontact").innerHTML = "Israeli ERC (Yifat Siner @ NOTES ID: IJB@il.ibm.com, Yifat Singer/Israel/IBM)";
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

function finalizeAbbrevName() {
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    reqType = FormManager.getActualValue('reqType');
  }
  if (reqType != 'C') {
    return;
  }

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp != null) {
    var installingAddr = getAddressByType('ZI01');
    var countryUseAAddr = getAddressByType('CTYA');
    var finalAbbrevName = '';

    if (custSubGrp == 'THDPT') {// Third Party Sub scenario
      if ((installingAddr != null && installingAddr != '') && (countryUseAAddr != null && countryUseAAddr != '')) {
        var installCustName = '' + installingAddr.custNm1;
        var cntryUseACustName = '' + countryUseAAddr.custNm1;
        if ((installCustName != null && installCustName != '') && (cntryUseACustName != null && cntryUseACustName != '')) {
          finalAbbrevName = cntryUseACustName.substring(0, 8) + ' for ' + installCustName.substring(0, 9);
        }
      }
    } else if (custSubGrp == 'INTSO') {// Internal SO Sub scenario
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
  checkAndAddValidator('poBox', Validators.DIGIT, [ 'PO Box' ]);
}

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

/**
 * Update address - disable custPhone for not mailing address
 */
function disableCustPhone() {
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

/*
 * EmbargoCode field locked for REQUESTER
 */
function lockEmbargo() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (role == 'REQUESTER') {
    if (FormManager.getActualValue('reqType') == 'X') {
      FormManager.readOnly('embargoCd');
    }
  }
}

function sboLengthValidator() {

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
  var addrType = FormManager.getActualValue('addrType');

  if (addrType == 'ZD01' || addrType == 'ZI01' || addrType == 'ZS02' || addrType == 'CTYC') {
    FormManager.hide('POBox', 'poBox');
    FormManager.clearValue('poBox');
    FormManager.removeValidator('addrTxt', Validators.NON_LATIN);
    checkAndAddValidator('addrTxt', Validators.REQUIRED, [ 'Street' ]);
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

function addEmbargoCodeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd').toUpperCase();
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        embargoCd = embargoCd.trim();
        if (embargoCd == '' || embargoCd == 'D' || embargoCd == 'J') {
          return new ValidationResult(null, true);
        } else {
          return new ValidationResult({
            id : 'embargoCd',
            type : 'text',
            name : 'embargoCd'
          }, false, 'Embargo Code value should be only D, J or Blank.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addPpsceidValidator() {
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
    if (custSubGrp == 'COMME' || custSubGrp == 'CROSS' || custSubGrp == 'BUSPR') {
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
    var kuklaValues = [ '11', '33', '35' ];
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
  }
}

function markAddrSaveSuperUser(cntry, addressMode, saving) {
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

function isAddrFieldsUpdatedExcludingLanded(type, addrRecord) {
  var reqId = FormManager.getActualValue('reqId');

  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
      ADDR_TYPE : type,
    };

    var origCustNm1;
    var origCustNm2;
    // var origLandCntry;
    var origAddrTxt;
    var origPOBox;
    var origCity1;
    var origPostCd;
    var origDept;

    var result = cmr.query('ADDR.GET.OLDADDR.BY_REQID_ADDRTYP_IL', reqParam);

    if (result != null && result != '') {
      origCustNm1 = result.ret1;
      origCustNm2 = result.ret2;
      // origLandCntry = result.ret3;
      origAddrTxt = result.ret4;
      origPOBox = result.ret5;
      origCity1 = result.ret6;
      origPostCd = result.ret7;
      origDept = result.ret8;
    }

    var curCustNm1 = (addrRecord.custNm1[0] == null) ? '' : addrRecord.custNm1[0];
    var curCustNm2 = (addrRecord.custNm2[0] == null) ? '' : addrRecord.custNm2[0];
    var curAddrTxt = (addrRecord.addrTxt[0] == null) ? '' : addrRecord.addrTxt[0];
    var curPOBox = (addrRecord.poBox[0] == null) ? '' : addrRecord.poBox[0];
    var curCity1 = (addrRecord.city1[0] == null) ? '' : addrRecord.city1[0];
    var curPostCd = (addrRecord.postCd[0] == null) ? '' : addrRecord.postCd[0];
    var curDept = (addrRecord.dept[0] == null) ? '' : addrRecord.dept[0];

    if (origCustNm1 != curCustNm1 || origCustNm2 != curCustNm2 || origAddrTxt != curAddrTxt || origPOBox != curPOBox || origCity1 != curCity1 || origPostCd != curPostCd || origDept != curDept) {
      return true;
    }

  }
  return false;
}

function addISICKUKLAValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var isicCD = FormManager.getActualValue('isicCd');
        var kukla = FormManager.getActualValue('custClass');
        var reqType = FormManager.getActualValue('reqType').toUpperCase();

        if (reqType != 'U') {
          return;
        }

        if (isicCD == '9500' && kukla != '60') {
          return new ValidationResult(null, false, 'Invalid value for ISIC/KUKLA.  ISIC value 9500 and KUKLA 60 should be linked together.');
        } else if (kukla == '60' && isicCD != '9500') {
          return new ValidationResult(null, false, 'Invalid value for ISIC/KUKLA.  ISIC value 9500 and KUKLA 60 should be linked together.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function setCapInd() {
  if (role.toUpperCase() != 'VIEWER') {
    var reqType = FormManager.getActualValue('reqType');
    if (reqType == 'C') {
      FormManager.readOnly('capInd');
      FormManager.setValue('capInd', true);
    }
  }
}

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

function validateCMRNumberForLegacy() {
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
  if (role == 'REQUESTER' || 'Y' == ifProspect || (role != 'REQUESTER' && cmrNO.startsWith('P'))) {
    FormManager.readOnly('cmrNo');
  } else if ('Y' != ifProspect && (role == 'PROCESSOR') && reqType != 'U') {
    FormManager.enable('cmrNo');
  } else {
    FormManager.readOnly('cmrNo');
  }
}

function addCollectionValidator() {
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
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var vatRequired = isVatRequired();
  if (vatRequired) {
    if (dijit.byId('vatExempt').get('checked')) {
      FormManager.clearValue('vat');
      FormManager.readOnly('vat');
      FormManager.removeValidator('vat', Validators.REQUIRED);
    } else {
      FormManager.enable('vat');
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    }
  }
}

/**
 * Downloads the DPL checklist file
 */
function downloadDPLChecklistTemplate() {
  var token = new Date().getTime();
  FormManager.setValue('dlDocType', 'TEMPLATE');
  FormManager.setValue('dlTokenId', token);
  FormManager.setValue('dlReqId', FormManager.getActualValue('reqId'));
  cmr.showProgress('Downloading file. Please wait...');
  document.forms['fileTemplateDownloadForm'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);

}

function toggleAddressTypesForIL(cntry, addressMode, details) {
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
  console.log("addAddressGridValidatorStreetPOBox..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;

          var missingPOBoxStreetAddrs = '';
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
              } else {
                missingPOBoxStreetAddrs += record.addrTypeText[0] + ' (' + record.addrSeq[0] + ') ';
              }
            }
          }

          if (missingPOBoxStreetAddrs != '') {
            return new ValidationResult(null, false, 'Please fill-out either Street or PO BOX for the following address: ' + missingPOBoxStreetAddrs);
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

    if (type == translatedType) {
      if (localLangData.addrSeq[0] == record.pairedSeq[0]) {
        translatedData = record;
        break;
      }
    }
  }

  return translatedData;
}

function getMismatchFields(localLangField, translatedField) {
  var mismatchFields = '';
  if (localLangField == null || translatedField == null) {
    return mismatchFields;
  }
  if (!hasMatchingFieldsFilled(localLangField.addrTxt[0], translatedField.addrTxt[0])) {
    mismatchFields += 'Street';
  }
  if (!hasMatchingFieldsFilled(localLangField.poBox[0], translatedField.poBox[0])) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'PO BOX';
  }
  if (!hasMatchingFieldsFilled(localLangField.dept[0], translatedField.dept[0])) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Att. Person';
  }
  if (!hasMatchingFieldsFilled(localLangField.custNm1[0], translatedField.custNm1[0])) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Customer Name';
  }
  if (!hasMatchingFieldsFilled(localLangField.postCd[0], translatedField.postCd[0])) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Postal Code';
  }
  if (!hasMatchingFieldsFilled(localLangField.city1[0], translatedField.city1[0])) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'City';
  }
  return mismatchFields;
}

function hasMatchingFieldsFilled(localLangField, translatedField) {
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

function validateSalesRep() {
  console.log("validateSalesRep..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var salesRep = FormManager.getActualValue('repTeamMemberNo');

        if (salesRep != null && salesRep != undefined && salesRep != '') {
          if ((salesRep.length != 6) || !salesRep.match("^[0-9]*$")) {
            return new ValidationResult(null, false, 'Sales Rep should be of 6 numeric characters.');
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateEnterpriseNo() {
  console.log("validateEnterpriseNo..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var enterpriseNo = FormManager.getActualValue('enterprise');

        if (enterpriseNo != null && enterpriseNo != undefined && enterpriseNo != '') {
          if ((enterpriseNo.length != 6) || !enterpriseNo.match("^[0-9]*$")) {
            return new ValidationResult(null, false, 'Enterprise Number should be of 6 numeric characters.');
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function setEnterpriseSalesRepSBP() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');

  if (isuCd == '34' && clientTier == 'Q') {
    FormManager.setValue('enterprise', '006510');
    FormManager.setValue('salesBusOffCd', '006');
    FormManager.setValue('repTeamMemberNo', '000651');
  } else if (isuCd == '34' && clientTier == 'Y') {
    FormManager.setValue('enterprise', '003290');
    FormManager.setValue('salesBusOffCd', '006');
    FormManager.setValue('repTeamMemberNo', '000651');
  } else if (isuCd == '21' && clientTier == '7') {
    FormManager.setValue('enterprise', '985999');
    FormManager.setValue('salesBusOffCd', '009');
    FormManager.setValue('repTeamMemberNo', '000993');
  }
}

function checkCmrUpdateBeforeImport() {
  console.log("checkCmrUpdateBeforeImport..............");
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
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (reqType != 'C') {
    return;
  }
  if (reqType == 'C' && scenarioChanged) {

    if (scenario == 'BUSPR' || scenario == 'INTER' || scenario == 'INTSO') {
      FormManager.setValue('repTeamMemberNo', '000993');
      FormManager.setValue('enterprise', '985999');
      FormManager.setValue('salesBusOffCd', '009');

      FormManager.readOnly('repTeamMemberNo');
      FormManager.readOnly('enterprise');
      FormManager.readOnly('salesBusOffCd');
      requireSalesRepEnterpriseSBOByRole();
    } else {
      FormManager.setValue('repTeamMemberNo', '000651');
      FormManager.setValue('enterprise', '006510');
      FormManager.setValue('salesBusOffCd', '006');
      if (scenario == 'PRIPE') {
        FormManager.readOnly('repTeamMemberNo');
        FormManager.readOnly('enterprise');
        FormManager.readOnly('salesBusOffCd');
      }
      requireSalesRepEnterpriseSBOByRole();
    }
  } else if (reqType == 'C' && !scenarioChanged) {
    if (role == 'PROCESSOR') {
      if (scenario == 'BUSPR' || scenario == 'INTER' || scenario == 'INTSO' || scenario == 'PRIPE') {
        FormManager.readOnly('repTeamMemberNo');
        FormManager.readOnly('enterprise');
        FormManager.readOnly('salesBusOffCd');
      }
    }
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
  GEOHandler.addAfterConfig(addILClientTierISULogic, [ SysLoc.ISRAEL ]);
  GEOHandler.registerValidator(addILAddressTypeValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addILAddressTypeMailingValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addILAddressTypeBillingValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addShippingAddrTypeValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addILAddressTypeCntyAValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addILAddressTypeCntyBValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.ISRAEL, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addStreetAddressFormValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addEmbargoCodeValidator, SysLoc.ISRAEL, null, true);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [ SysLoc.ISRAEL ], null, true);

  GEOHandler.addAfterConfig(fieldsReadOnlyIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(fieldsReadOnlyIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(updateAbbrevNmLocnIsrael, [ SysLoc.ISRAEL ]);
  // GEOHandler.registerValidator(addNameContAttnDeptValidator, [ SysLoc.ISRAEL
  // ], null, true);
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

  GEOHandler.addAfterConfig(setCapInd, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(lockDunsNo, [ SysLoc.ISRAEL ]);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.EMEA, GEOHandler.ROLE_PROCESSOR, true);

  // For EmbargoCode
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.EMEA);

  GEOHandler.registerValidator(sboLengthValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.addAfterConfig(addHandlersForIL, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(countryUseAISRAEL, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(setAddrFieldsBehavior, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(validatePoBox, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(markAddrSaveSuperUser, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(toggleAddressTypesForIL, [ SysLoc.ISRAEL ]);

  GEOHandler.registerValidator(addPpsceidValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(validateCMRNumberForLegacy, [ SysLoc.ISRAEL ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(enableCmrNoForProcessor, [ SysLoc.ISRAEL ]);

  GEOHandler.addAfterConfig(showHideKuklaField, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(showHideKuklaField, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(lockCustomerClassByLob, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(finalizeAbbrevName, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(adjustChecklistContact, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(setSalesRepEnterpriseNoSBO, [ SysLoc.ISRAEL ]);

  GEOHandler.registerValidator(addISICKUKLAValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addCollectionValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addPostalCdCityValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addDPLChecklistAttachmentValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(addAddressGridValidatorStreetPOBox, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(validateSalesRep, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.registerValidator(validateEnterpriseNo, [ SysLoc.ISRAEL ], null, true);

  GEOHandler.addAfterTemplateLoad(preTickVatExempt, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(showVatExempt, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(setStreetContBehavior, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(requireSalesRepEnterpriseSBOByRole, [ SysLoc.ISRAEL ]);
});