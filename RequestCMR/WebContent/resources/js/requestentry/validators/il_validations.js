/* Register Israel Javascripts */
var _CTCHandlerIL = null;
var _ILentConnect = null;
var _vatExemptHandler = null;
var _gtcAddrTypeHandlerIL = [];
var _gtcAddrTypesIL = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01', 'CTYA', 'CTYB', 'CTYC' ];

function addHandlersForIL() {
  for (var i = 0; i < _gtcAddrTypesIL.length; i++) {
    _gtcAddrTypeHandlerIL[i] = null;
    if (_gtcAddrTypeHandlerIL[i] == null) {
      _gtcAddrTypeHandlerIL[i] = dojo.connect(FormManager.getField('addrType_' + _gtcAddrTypesIL[i]), 'onClick', function(value) {
        countryUseAISRAEL();
      });
    }
  }

  if (_CTCHandlerIL == null) {
    _CTCHandlerIL = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL) {
        if (FormManager.getActualValue('isuCd') == '34' && FormManager.getActualValue('clientTier') == 'Y') {
          FormManager.setValue('enterprise', '003290');
          FormManager.setValue('salesBusOffCd', '000');
          FormManager.setValue('repTeamMemberNo', '000651');
        }
      }
    });
  }

  if (_ILentConnect == null) {
    _ILentConnect = dojo.connect(FormManager.getField('enterprise'), 'onChange', function(value) {
      setSrSboValuesOnEnterprise(value);
    });
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
  FormManager.addValidator('salesBusOffCd', Validators.NUMBER, [ 'SBO' ]);

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

/*
 * Set SR SBO Values based on Enterprise
 */
function setSrSboValuesOnEnterprise(enterprise) {
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var enterprise = FormManager.getActualValue('enterprise');

  if (reqType != 'C') {
    return;
  }

  var srlist = [];
  var sbolist = [];
  if (enterprise != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      SALES_BO_DESC : '%' + enterprise + '%'
    };
    var results = cmr.query('GET.SRSBOTLIST.BYENTR', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        srlist.push(results[i].ret1);
        sbolist.push(results[i].ret2);
      }
      if (srlist != null) {
        FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), srlist);
        if (srlist.length >= 1) {
          FormManager.setValue('repTeamMemberNo', srlist[0]);
        }
      }

      if (sbolist != null) {
        if (sbolist.length >= 1) {
          FormManager.setValue('salesBusOffCd', sbolist[0]);
        }
      }
    }
  }
}

function fieldsReadOnlyIsrael() {
  var custType = FormManager.getActualValue('custSubGrp');
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
  }

  if (custType == 'PRICU') {
    FormManager.resetValidations('vat');
    FormManager.readOnly('vat');
  }
  FormManager.addValidator('salesBusOffCd', Validators.NUMBER, [ 'SBO' ]);
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
    checkAndAddValidator('postCd', Validators.LATIN, [ 'Postal Code' ]);
    // checkAndAddValidator('custPhone', Validators.LATIN, [ 'Phone #' ]);
  } else {
    FormManager.removeValidator('custNm1', Validators.LATIN);
    FormManager.removeValidator('custNm2', Validators.LATIN);
    FormManager.removeValidator('addrTxt', Validators.LATIN);
    FormManager.removeValidator('addrTxt2', Validators.LATIN);
    FormManager.removeValidator('city1', Validators.LATIN);
    FormManager.removeValidator('postCd', Validators.LATIN);
    FormManager.removeValidator('dept', Validators.LATIN);
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
    checkAndAddValidator('postCd', Validators.NON_LATIN, [ 'Postal Code' ]);
    // checkAndAddValidator('custPhone', Validators.NON_LATIN, [ 'Phone #'
    // ]);

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
    // FormManager.removeValidator('custPhone', Validators.NON_LATIN);
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
  } else {
    FormManager.enable('custPhone');
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

function addPhoneValidatorEMEA() {
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
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
  // Lock land country when 'LOCAL' scenario
  var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
  if (cntryCd == SysLoc.ISRAEL) {
    var landCntry = FormManager.getActualValue('landCntry');
    if ((FormManager.getActualValue('custGrp') == 'LOCAL') && (FormManager.getActualValue('addrType') == 'CTYA' || FormManager.getActualValue('addrType') == 'ZS01')) {
      FormManager.setValue('landCntry', 'IL');
      FormManager.readOnly('landCntry');
    } else {
      FormManager.enable('landCntry');
    }
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

function showVatOnLocal(fromAddress, scenario, scenarioChanged) {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  var custGrp = FormManager.getActualValue('custGrp');
  if (scenarioChanged && custGrp == 'LOCAL') {
    cmr.showNode('vatInfo');
  } else {
    cmr.hideNode('vatInfo');
  }
}

dojo.addOnLoad(function() {
  GEOHandler.EMEA = [ SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.TURKEY, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.ITALY ];
  console.log('adding Israel functions...');
  GEOHandler.addAddrFunction(addEMEALandedCountryHandler, GEOHandler.EMEA);
  GEOHandler.enableCopyAddress(GEOHandler.EMEA, validateEMEACopy, [ 'ZD01', 'CTYC' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.EMEA);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.EMEA);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAddrFunction(addPhoneValidatorEMEA, [ SysLoc.ISRAEL ]);

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
  GEOHandler.addAfterConfig(setSrSboValuesOnEnterprise, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(fieldsReadOnlyIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterTemplateLoad(fieldsReadOnlyIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(updateAbbrevNmLocnIsrael, [ SysLoc.ISRAEL ]);
  GEOHandler.registerValidator(addNameContAttnDeptValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.addAddrFunction(addLatinCharValidator, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(addNonLatinCharValidator, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(resetSubIndustryCd, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(removeValidationInacNac, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(disableCustPhone, [ SysLoc.ISRAEL ]);
  GEOHandler.registerValidator(addPostalCodeLengthValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.addAfterConfig(setAbbrvLocCrossBorderScenario, [ SysLoc.ISRAEL ]);
  GEOHandler.addAfterConfig(setAbbrvLocCrossBorderScenarioOnChange, [ SysLoc.ISRAEL ]);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.EMEA, GEOHandler.ROLE_PROCESSOR, true);

  // For EmbargoCode
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.EMEA);

  GEOHandler.registerValidator(sboLengthValidator, [ SysLoc.ISRAEL ], null, true);
  GEOHandler.addAfterConfig(addHandlersForIL, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(countryUseAISRAEL, [ SysLoc.ISRAEL ]);
  GEOHandler.addAddrFunction(validatePoBox, [ SysLoc.ISRAEL ]);

  GEOHandler.addAfterTemplateLoad(showVatOnLocal, [ SysLoc.ISRAEL ]);
});