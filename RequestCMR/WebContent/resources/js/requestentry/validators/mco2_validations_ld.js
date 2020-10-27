/* Register MCO Javascripts */
var fstCEWA = [ "373", "382", "383", "635", "637", "656", "662", "667", "670", "691", "692", "700", "717", "718", "753", "810", "840", "841", "876", "879", "880", "881" ];
var othCEWA = [ "610", "636", "645", "669", "698", "725", "745", "764", "769", "770", "782", "804", "825", "827", "831", "833", "835", "842", "851", "857", "883" ];

function addMCO1LandedCountryHandler(cntry, addressMode, saving, finalSave) {
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
 * After config handlers
 */
var _ISUHandler = null;
var _CTCHandler = null;
var _SalesRepHandler = null;
var _reqLobHandler = null;
var _reqReasonHandler = null;
var _codHandler = null;
var _cofHandler = null;
var _vatExemptHandler = null;
var _streetHandler = null;
var _tinExemptHandler = null;
var _numeroExemptHandler = null;

function addHandlersForMCO2() {

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      limitClientTierValues(value);
      setSalesRepValues(value);
    });
  }

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setSalesRepValues(value);
    });
  }

  if (FormManager.getActualValue('reqType') == 'U') {
    if (_reqLobHandler == null) {
      _reqLobHandler = dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
        setCodFieldBehavior();
        setCofFieldBehavior();
      });
    }

    if (_reqReasonHandler == null) {
      _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
        setCodFieldBehavior();
        setCofFieldBehavior();
      });
    }

    if (_codHandler == null) {
      _codHandler = dojo.connect(FormManager.getField('codFlag'), 'onChange', function(value) {
        setCofValueByCod();
      });
    }

    if (_cofHandler == null) {
      _cofHandler = dojo.connect(FormManager.getField('commercialFinanced'), 'onChange', function(value) {
        setCodValueByCof();
      });
    }
  }

  if (_streetHandler == null) {
    _streetHandler = dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {
      setStreetContBehavior();
    });
  }

  if (FormManager.getActualValue('reqType') == 'C') {
    if (_vatExemptHandler == null) {
      _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
        resetVatRequired();
      });
    }

    if (_tinExemptHandler == null) {
      _tinExemptHandler = dojo.connect(FormManager.getField('taxCd2'), 'onClick', function(value) {
        resetTinRequired();
      });
    }

    if (_numeroExemptHandler == null) {
      _numeroExemptHandler = dojo.connect(FormManager.getField('taxCd2'), 'onClick', function(value) {
        resetNumeroRequired();
      });
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

function setCodFieldBehavior() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }

  if (FormManager.getActualValue('requestingLob') == 'AR' && FormManager.getActualValue('reqReason') == 'COD') {
    FormManager.enable('codFlag');
  } else {
    FormManager.readOnly('codFlag');
  }
}

function setCofFieldBehavior() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }

  if (FormManager.getActualValue('requestingLob') == 'IGF' && FormManager.getActualValue('reqReason') == 'COPT') {
    FormManager.enable('commercialFinanced');
  } else {
    FormManager.readOnly('commercialFinanced');
  }
}

function setIbmDeptCostCenterBehavior() {
  var cmrNo = FormManager.getActualValue('cmrNo');
  var reqType = FormManager.getActualValue('reqType');

  if (cmrNo != '' && reqType == 'U' && (cmrNo.substring(0, 2) != '99')) {
    FormManager.readOnly('ibmDeptCostCenter');
  }
}

function setCodValueByCof() {
  var cof = FormManager.getActualValue('commercialFinanced');
  if (cof == 'R' || cof == 'S' || cof == 'T') {
    FormManager.setValue('codFlag', 'N');
  }
}

function setCofValueByCod() {
  var cod = FormManager.getActualValue('codFlag');
  if (cod == 'Y') {
    FormManager.setValue('commercialFinanced', '');
  }
}

var _addrTypesForCEWA = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02' ];
var _addrTypeHandler = [];
function addHandlersForCEWA() {
  for (var i = 0; i < _addrTypesForCEWA.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForCEWA[i]), 'onClick', function(value) {
        disableAddrFieldsCEWA();
      });
    }
  }

}

/*
 * EmbargoCode field locked for REQUESTER
 */
function lockEmbargo() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    if (reqType != 'U') {
      FormManager.readOnly('embargoCd');
    }
  } else {
    FormManager.enable('embargoCd');
  }
}

/*
 * Lock CMROwner and Preferred Language
 */
function lockCmrOwnerPrefLang() {
  var reqType = FormManager.getActualValue('reqType');
  var isCmrImported = getImportedIndc();

  if (reqType == 'C' && isCmrImported == 'Y') {
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('custPrefLang');
  }
}
function afterConfigForMCO2() {
  limitClientTierValues();
  FormManager.setValue('capInd', true);
  FormManager.readOnly('capInd');
  FormManager.addValidator('ibmDeptCostCenter', Validators.DIGIT, [ 'Internal Department Number' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('specialTaxCd', Validators.ALPHANUM, [ 'Tax Code' ], 'MAIN_CUST_TAB');
  if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.addValidator('collectionCd', Validators.ALPHANUM, [ 'Collection Code' ], 'MAIN_IBM_TAB');

    var role = FormManager.getActualValue('userRole');
    var numero = FormManager.getActualValue('busnType');
    if (role == GEOHandler.ROLE_REQUESTER) {
      if (numero != null && numero != '') {
        FormManager.readOnly('busnType');
      }
    }
  }
}

/**
 * sets fields to lock/mandatory, not scenario handled
 */
function lockRequireFieldsMCO2() {
  console.log('lockRequireFieldsMCO2 LD.....');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole');

  // fields locked for Requester
  if (role == GEOHandler.ROLE_REQUESTER) {
    if (reqType != 'U') {
      FormManager.readOnly('specialTaxCd');
    }

    // FormManager.readOnly('salesBusOffCd');
    // FormManager.readOnly('repTeamMemberNo');
    // FormManager.readOnly('isuCd');
    // FormManager.readOnly('clientTier');
  }

  if (reqType == 'U') {
    setCodFieldBehavior();
    setCofFieldBehavior();
  }
}

function disableAddrFieldsCEWA() {
  var custType = FormManager.getActualValue('custGrp');
  var addrType = FormManager.getActualValue('addrType');

  if (custType == 'LOCAL' && addrType == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }

  // Phone - for shipping and Sold-to (FST = Installing and Non-FST = Mailing )
  if (addrType == 'ZD01' || addrType == 'ZS01') {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  }

  // PO Box allowed -> FST = Mail-to (ZS02), Bill-to (ZP01), Sold-to (ZS01)
  // Non FST = Mailing (ZS01), Billing (ZP01)
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrPOBoxEnabled = [ 'ZS01', 'ZP01' ]

  if (fstCEWA.includes(cntry)) {
    addrPOBoxEnabled.push('ZS02');
  }

  if (addrPOBoxEnabled.includes(addrType)) {
    FormManager.enable('poBox');
  } else {
    FormManager.setValue('poBox', '');
    FormManager.disable('poBox');
  }
}

function addAddressTypeValidator() {
  console.log("addAddressTypeValidator for MCO2..........");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'All address types are mandatory.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var zs01Cnt = 0;
          var zp01Cnt = 0;
          var zi01Cnt = 0;
          var zd01Cnt = 0;
          var zs02Cnt = 0;

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
            }
          }

          var cntry = FormManager.getActualValue('cmrIssuingCntry');

          if (zs01Cnt == 0 || zp01Cnt == 0 || zi01Cnt == 0 || zd01Cnt == 0 || zs02Cnt == 0) {
            return new ValidationResult(null, false, 'All address types are mandatory.');
          } else if (fstCEWA.includes(cntry)) {
            return fstAddressValidator(zp01Cnt, zs01Cnt, zs02Cnt);
          } else if (othCEWA.includes(cntry)) {
            return nonFstAddressValidator(zs01Cnt, zp01Cnt, zs02Cnt);
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function fstAddressValidator(zp01Cnt, zs01Cnt, zs02Cnt) {
  if (zp01Cnt > 1) {
    return new ValidationResult(null, false, 'Only one Bill-to address is allowed.');
  } else if (zs02Cnt > 1) {
    return new ValidationResult(null, false, 'Only one Mail-to address is allowed.');
  } else if (zs01Cnt > 1) {
    return new ValidationResult(null, false, 'Only one Sold-to address is allowed.');
  } else {
    return new ValidationResult(null, true);
  }
}

function nonFstAddressValidator(zs01Cnt, zp01Cnt, zs02Cnt) {
  if (zp01Cnt > 1) {
    return new ValidationResult(null, false, 'Only one Billing address is allowed.');
  } else if (zs01Cnt > 1) {
    return new ValidationResult(null, false, 'Only one Mailing address is allowed.');
  } else if (zs02Cnt > 1) {
    return new ValidationResult(null, false, 'Only one EPL address is allowed.');
  } else {
    return new ValidationResult(null, true);
  }
}

function addAddressFieldValidators() {
  // City + PostCd should not exceed 28 characters
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var city = FormManager.getActualValue('city1');
        var postCd = FormManager.getActualValue('postCd');
        var val = city;

        if (postCd != '') {
          val += postCd;
          if (val.length > 28) {
            return new ValidationResult(null, false, 'Total computed length of City and Postal Code should not exceed 28 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // addrCont + poBox should not exceed 21 characters
  // ",<space>PO<space>BOX<space>" is included when counting to 30 max
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var stCont = FormManager.getActualValue('addrTxt2');
        var poBox = FormManager.getActualValue('poBox');

        if (poBox != '' && stCont != '') {
          var stContPoBox = stCont + poBox;
          if (stContPoBox != null && stContPoBox.length > 21) {
            return new ValidationResult(null, false, 'Total computed length of Street Con\'t and PO Box should not exceed 21 characters.');
          }
        } else if (poBox != '') {
          if (poBox.length > 23) {
            return new ValidationResult(null, false, 'PO Box length should not exceed 23 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // phone + ATT should not exceed 29 characters (for Shipping & EPL only)
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType');

        if (addrType == 'ZD01' || addrType == 'ZS02') {
          var att = FormManager.getActualValue('custNm4');
          var custPhone = FormManager.getActualValue('custPhone');
          var val = att;

          if (custPhone != '') {
            val += custPhone;
            if (val != null && val.length > 29) {
              return new ValidationResult(null, false, 'Total computed length of Attention Person and Phone should not exceed 29 characters.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

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
  var city = cmr.query('ADDR.GET.CITY1.BY_REQID', reqParam);
  var abbrvNm = custNm.ret1;
  var abbrevLocn = city.ret1;

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

function validateMCOCopy(addrType, arrayOfTargetTypes) {
  return null;
}

function crossborderScenariosAbbrvLoc() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var custGroup = FormManager.getActualValue('custGrp');
  if (custGroup == "CROSS") {
    var reqId = FormManager.getActualValue('reqId');
    if (reqId != null) {
      reqParam = {
        REQ_ID : reqId,
        ADDR_TYPE : "ZS01",
      };
    }
    var results = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', reqParam);
    var abbrevLocn = results.ret1;
    if (abbrevLocn != null && abbrevLocn.length > 12) {
      abbrevLocn = abbrevLocn.substring(0, 12);
    }

    if (abbrevLocn != null) {
      FormManager.setValue('abbrevLocn', abbrevLocn);
    } else {
      FormManager.setValue('abbrevLocn', '');
    }
  }
}

function scenariosAbbrvLocOnChange() {
  dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
    var custGroup = FormManager.getActualValue('custGrp').toUpperCase();
    if (custGroup == 'LOCAL') {
      setAbbrvNmLoc();
    } else if (custGroup == 'CROSS') {
      crossborderScenariosAbbrvLoc();
    }
  });
}

function addAddrValidatorMCO2() {
  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.LATIN, [ 'Customer Name Continuation' ]);
  FormManager.addValidator('custNm4', Validators.LATIN, [ 'Additional Name or Address Information' ]);
  FormManager.addValidator('addrTxt', Validators.LATIN, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN, [ 'Street Continuation' ]);
  FormManager.addValidator('city1', Validators.LATIN, [ 'City' ]);
  FormManager.addValidator('postCd', Validators.LATIN, [ 'Postal Code' ]);

  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
  FormManager.addValidator('poBox', Validators.DIGIT, [ 'PO BOX' ]);
}

function addAbbrvNmAndLocValidator() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'PROCESSOR') {
    FormManager.addValidator('abbrevNm', Validators.LATIN, [ 'Abbreviated Name' ]);
    FormManager.addValidator('abbrevLocn', Validators.LATIN, [ 'Abbreviated Location' ]);
  }
}

function addAttachmentValidator() {
  console.log("addAttachmentValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var req = FormManager.getActualValue('reqType').toUpperCase();
        var subCustGrp = FormManager.getActualValue('custSubGrp');
        if (role == 'REQUESTER' && req != 'U') {
          switch (subCustGrp.toUpperCase()) {
          case 'SOFTL':
            return new ValidationResult(null, true);
            break;
          case 'BUSPR':
            return new ValidationResult(null, true);
            break;
          case 'INTER':
            return new ValidationResult(null, true);
            break;
          case 'XBP':
            return new ValidationResult(null, true);
            break;
          case 'XINTE':
            return new ValidationResult(null, true);
            break;
          case 'XPRIC':
            return new ValidationResult(null, true);
            break;
          default:
            var reqId = FormManager.getActualValue('reqId');
            if (reqId != null) {
              reqParam = {
                REQ_ID : reqId
              };
            }
            var results = cmr.query('COUNTATTACHMENTRECORDS', reqParam);
            var recordCount = results.ret1;

            if (recordCount != null) {
              if (recordCount > 0) {
                return new ValidationResult(null, true);
              } else if (recordCount == 0) {
                return new ValidationResult(null, false, 'Proof of address is mandatory.');
              }
            }
            break;
          }
        } else if (role == 'REQUESTER' && req == 'U') {
          if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
            var record = null;
            var updateInd = null;
            var importInd = null;
            var counter = 0;
            for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
              record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
              type = record.addrType;
              updateInd = record.updateInd;
              importInd = record.importInd;

              if (updateInd == 'U') {
                var reqId = FormManager.getActualValue('reqId');
                if (reqId != null) {
                  reqParam = {
                    REQ_ID : reqId
                  };
                }
                var results = cmr.query('COUNTATTACHMENTRECORDS', reqParam);
                var recordCount = results.ret1;

                if (recordCount != null) {
                  if (recordCount > 0) {
                    return new ValidationResult(null, true);
                  } else if (recordCount == 0) {
                    return new ValidationResult(null, false, 'Proof of address is mandatory.');
                  }
                }
              } else if (importInd != 'Y') {
                var reqId = FormManager.getActualValue('reqId');
                if (reqId != null) {
                  reqParam = {
                    REQ_ID : reqId
                  };
                }
                var results = cmr.query('COUNTATTACHMENTRECORDS', reqParam);
                var recordCount = results.ret1;

                if (recordCount != null) {
                  if (recordCount > 0) {
                    return new ValidationResult(null, true);
                  } else if (recordCount == 0) {
                    return new ValidationResult(null, false, 'Proof of address is mandatory.');
                  }
                }
              } else {
                counter = counter + 1;
              }
            }

            if (counter > 0) {
              return new ValidationResult(null, true);
            } else {
              return new ValidationResult(null, false, 'Proof of address is mandatory.');
            }
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    $('label[for="custNm1_view"]').text('Customer Name:');
    $('label[for="custNm2_view"]').text('Customer Name Continuation:');
    $('label[for="landCntry_view"]').text('Country (Landed):');
    $('label[for="custNm4_view"]').text('Additional Name or Address Information:');
    $('label[for="addrTxt_view"]').text('Street:');
    $('label[for="addrTxt2_view"]').text('Street Continuation:');
    $('label[for="custPhone_view"]').text('Phone #:');
  }
}

function lockAbbrv() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');

  if (viewOnlyPage == 'true') {
    FormManager.readOnly('abbrevLocn');
    FormManager.readOnly('abbrevNm');
  } else {
    if (role == 'REQUESTER') {
      if (reqType != 'U') {
        FormManager.readOnly('abbrevLocn');
        FormManager.readOnly('abbrevNm');
      }
    }
    if (role == 'PROCESSOR') {
      FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
    }
  }
}

function showDeptNoForInternalsOnly(fromAddress, scenario, scenarioChanged) {
  if (scenario == 'INTER' || scenario == 'XINTE') {
    FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, [ 'Internal Department Number' ], 'MAIN_IBM_TAB');
    FormManager.show('InternalDept', 'ibmDeptCostCenter');
  } else {
    FormManager.removeValidator('ibmDeptCostCenter', Validators.REQUIRED);
    FormManager.hide('InternalDept', 'ibmDeptCostCenter');
  }
  if (scenarioChanged && scenario != null && scenario != '') {
    FormManager.clearValue('ibmDeptCostCenter');
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

function setSalesRepValues(isuCd, clientTier) {

  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');

  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  var salesReps = [];
  if (isuCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCd + '%',
    };
    results = cmr.query('GET.MCO2SR.BYISU', qParams);
    if (cntry == '764' || cntry == '831' || cntry == '851' || cntry == '857') {
      if (isuCd == '32' && (clientTier == 'S' || clientTier == 'C' || clientTier == 'T')) {
        FormManager.setValue('salesBusOffCd', '0080');
      } else {
        FormManager.setValue('salesBusOffCd', '0010');
      }
    } else if (cntry == '698' || cntry == '745') {
      if (isuCd == '32' && (clientTier == 'S' || clientTier == 'C' || clientTier == 'T')) {
        FormManager.setValue('salesBusOffCd', '0060');
      } else {
        FormManager.setValue('salesBusOffCd', '0010');
      }
    }

    /*
     * if ('780' != FormManager.getActualValue('cmrIssuingCntry')) { var
     * custSubGrp = FormManager.getActualValue('custSubGrp'); if (custSubGrp !=
     * "BUSPR" && custSubGrp != "XBP") { FormManager.setValue('repTeamMemberNo',
     * '016757'); } else { FormManager.setValue('repTeamMemberNo', '780780'); } }
     */

  }
}

var _addrTypesForMCO2 = [ 'ZD01', 'ZI01', 'ZP01', 'ZS01', 'ZS02' ];
var addrTypeHandler = [];
/*
 * function diplayTinNumberforTZ() {
 * 
 * var role = FormManager.getActualValue('role');
 * 
 * if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress' &&
 * FormManager.getActualValue('cmrIssuingCntry') == '851') {
 * cmr.hideNode('tin'); //FormManager.clearValue('dept'); for (var i = 0; i <
 * _addrTypesForMCO2.length; i++) { if (addrTypeHandler[i] == null) {
 * addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' +
 * _addrTypesForMCO2[i]), 'onClick', function(value) { if
 * (FormManager.getField('addrType_ZP01').checked &&
 * FormManager.getActualValue('custGrp') == 'LOCAL') { cmr.showNode('tin');
 * if(role == 'REQUESTER'){ FormManager.addValidator('dept',
 * Validators.REQUIRED, [ 'TIN#' ], null); } } else { cmr.hideNode('tin');
 * //FormManager.clearValue('dept'); FormManager.removeValidator('dept',
 * Validators.REQUIRED); } }); } else { if
 * (FormManager.getField('addrType_ZP01').checked &&
 * FormManager.getActualValue('custGrp') == 'LOCAL') cmr.showNode('tin'); } } }
 * if (cmr.addressMode == 'updateAddress') { if
 * (FormManager.getActualValue('addrType') == 'ZP01') { cmr.showNode('tin');
 * if(role == 'REQUESTER'){ FormManager.addValidator('dept',
 * Validators.REQUIRED, [ 'TIN#' ], null); } } else { cmr.hideNode('tin'); //
 * FormManager.clearValue('dept'); FormManager.removeValidator('dept',
 * Validators.REQUIRED); } } }
 */

function diplayTinNumberforTZ() {

  var role = FormManager.getActualValue('userRole').toUpperCase();
  var scenario = FormManager.getActualValue('custGrp');

  // var role = FormManager.getActualValue('role');

  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress' && FormManager.getActualValue('cmrIssuingCntry') == '851') {
    cmr.hideNode('tin');
    // FormManager.clearValue('dept');
    for (var i = 0; i < _addrTypesForMCO2.length; i++) {
      if (addrTypeHandler[i] == null) {
        addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForMCO2[i]), 'onClick', function(value) {
          if (FormManager.getField('addrType_ZP01').checked) {
            if (cmr.currentRequestType == 'C' && scenario == 'LOCAL') {
              cmr.showNode('tin');
              if (role == 'REQUESTER') {
                FormManager.addValidator('dept', Validators.REQUIRED, [ 'TIN#' ], '');
              } else {
                FormManager.resetValidations('dept');
              }
            } else if (cmr.currentRequestType == 'U') {
              cmr.showNode('tin');
              FormManager.resetValidations('dept');
            } else {
              cmr.hideNode('tin');
              FormManager.resetValidations('dept');
            }
          } else {
            cmr.hideNode('tin');
            // FormManager.clearValue('dept');
            FormManager.resetValidations('dept');
            // FormManager.removeValidator('dept', Validators.REQUIRED);
          }
          // setTinNumber();
        });
      } else {
        if (FormManager.getField('addrType_ZP01').checked && ((cmr.currentRequestType == 'C' && scenario == 'LOCAL') || cmr.currentRequestType == 'U'))
          cmr.showNode('tin');
      }
    }
  }
  if (cmr.addressMode == 'updateAddress') {
    if (FormManager.getActualValue('addrType') == 'ZP01') {
      cmr.showNode('tin');
      if (role == 'REQUESTER' && cmr.currentRequestType == 'C' && scenario == 'LOCAL') {
        FormManager.addValidator('dept', Validators.REQUIRED, [ 'TIN#' ], '');
      } else {
        FormManager.resetValidations('dept');
      }
    } else {
      cmr.hideNode('tin');
      // FormManager.clearValue('dept');
      FormManager.resetValidations('dept');
      // FormManager.removeValidator('dept', Validators.REQUIRED);
    }
  }
}

/*
 * function setTinNumber(){ if (FormManager.getField('addrType_ZS01').checked ||
 * FormManager.getField('addrType_ZI01').checked ||
 * FormManager.getField('addrType_ZD01').checked ||
 * FormManager.getField('addrType_ZS02').checked){ //cmr.showNode('tin');
 * FormManager.setValue('dept', ' '); } }
 */

function addTinFormatValidationTanzania() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var dept = FormManager.getActualValue('dept');
        // var lbl1 = FormManager.getLabel('LocalTax1');
        if (FormManager.getField('addrType_ZP01').checked && dept && dept.length > 0 && !dept.match("([0-9]{3}-[0-9]{3}-[0-9]{3})|^(X{3})$")) {
          return new ValidationResult({
            id : 'dept',
            type : 'text',
            name : 'dept'
          }, false, 'Invalid format of TIN#. Format should be NNN-NNN-NNN or "XXX".');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

// TZ defect 1730979 : Tin validator at billing

function addTinBillingValidator() {
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

        var results = cmr.query('GET_TIN_ADDRSEQ', qParams);
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
          return new ValidationResult(null, false, 'Tin should not be empty in Billing Address.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setFieldsBehavior(fromAddress, scenario, scenarioChanged) {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Requester') {
    FormManager.removeValidator('isuCd', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  }
  if (role == 'Processor') {
    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
  }
}

function addStreetAddressFormValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('addrTxt') == '' && FormManager.getActualValue('poBox') == '') {
          return new ValidationResult(null, false, 'Please fill-out either Street or PO Box.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addAdditionalNameStreetContPOBoxValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var isUpdate = FormManager.getActualValue('reqType') == 'U';
        var isLocalBasedOnLanded = FormManager.getActualValue('defaultLandedCountry') == FormManager.getActualValue('landCntry');

        if (FormManager.getActualValue('custGrp') == 'LOCAL' || (isUpdate && isLocalBasedOnLanded)) {
          return new ValidationResult(null, true);
        }

        var isAddlNameFilled = false;
        var isStreetContPOBOXFilled = false;

        if (FormManager.getActualValue('custNm4') != '') {
          isAddlNameFilled = true;
        }

        if (FormManager.getActualValue('addrTxt2') != '' || FormManager.getActualValue('poBox') != '') {
          isStreetContPOBOXFilled = true;
        }

        if (isAddlNameFilled && isStreetContPOBOXFilled) {
          return new ValidationResult(null, false, 'Please fill-out either \'Additional Name or Address Information\' or \'Street Continuation\' and/or \'PO Box\' only.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setTypeOfCustomerBehavior() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true' && FormManager.getActualValue('reqType') == 'U') {
    FormManager.readOnly('crosSubTyp');
    return;
  }

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
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrPOBoxEnabled = [ 'ZS01', 'ZP01' ]

  if (fstCEWA.includes(cntry)) {
    addrPOBoxEnabled.push('ZS02');
  }
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    if (_allAddressData != null && _allAddressData[i] != null) {
      if (!(addrPOBoxEnabled.includes(_allAddressData[i].addrType[0]))) {
        _allAddressData[i].poBox[0] = '';
      }
    }
  }
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
                missingPOBoxStreetAddrs += ', ' + record.addrTypeText[0];
              } else {
                missingPOBoxStreetAddrs += record.addrTypeText[0];
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

function addInternalDeptNumberValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = null;
        var scenario = null;
        if (typeof (_pagemodel) != 'undefined') {
          reqType = FormManager.getActualValue('reqType');
          scenario = FormManager.getActualValue('custSubGrp');
        }

        if (!(scenario == 'INTER' || scenario == 'XINTE' || reqType == 'U')) {
          return new ValidationResult(null, true);
        }

        if (FormManager.getActualValue('viewOnlyPage') == 'true') {
          return new ValidationResult(null, true);
        }

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
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function retainImportValues(fromAddress, scenario, scenarioChanged) {
  var isCmrImported = getImportedIndc();

  if (FormManager.getActualValue('reqType') == 'C' && isCmrImported == 'Y' && scenarioChanged) {

    if (scenario == 'COMME' || scenario == 'XCOM' || scenario == 'GOVRN' || scenario == 'XGOV' || scenario == 'THDPT' || scenario == 'XTP') {
      var reqId = FormManager.getActualValue('reqId');

      var origISU;
      var origClientTier;
      var origEnterprise;
      var origInac;
      var origSbo;

      var result = cmr.query("GET.CMRINFO.IMPORTED_AFRICA", {
        REQ_ID : reqId
      });

      if (result != null && result != '') {
        origISU = result.ret1;
        origClientTier = result.ret2;
        origEnterprise = result.ret3;
        origInac = result.ret4;
        origSbo = result.ret5;

        FormManager.setValue('isuCd', origISU);
        FormManager.setValue('clientTier', origClientTier);
        FormManager.setValue('salesBusOffCd', origSbo);
        FormManager.setValue('inacCd', origInac);
        // FormManager.setValue('enterprise', origEnterprise);
      }
    } else {
      FormManager.setValue('inacCd', '');
    }
  }
}
function requireTaxRegistrationForLocalScenario(fromAddress, scenario, scenarioChanged) {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole');
  var taxReg = FormManager.getActualValue('busnType');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (reqType == 'C') {
    // Creates (by model and by scratch) both requester and processor
    // Mandatory for Local Scenarios except IBM employee and Private Customer
    // (for those 2 optional)
    if (scenario == 'BUSPR' || scenario == 'COMME' || scenario == 'GOVRN' || scenario == 'INTER' || scenario == 'THDPT') {
      FormManager.addValidator('busnType', Validators.REQUIRED, [ 'Numero Statistique du Client' ], 'MAIN_CUST_TAB');
    } else {
      // Optional for all Cross-border Scenarios
      FormManager.removeValidator('busnType', Validators.REQUIRED);
    }
  } else {
    // Locked on requester side if field has value
    if (role == 'REQUESTER') {
      if (taxReg != null && taxReg != '') {
        FormManager.readOnly('busnType');
      } else {
        // If field is blank, then editable.
        FormManager.enable('busnType');
      }
    } else if (role == 'PROCESSOR') {

      // On Processor side editable in all instances
      FormManager.enable('busnType');
    }
  }
  if (scenarioChanged && !fromAddress && scenario != null && scenario != '') {
    FormManager.clearValue('busnType');
  }
}
function setTinNumberBehaviorForTz(fromAddress, scenario, scenarioChanged) {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole');
  var tinNo = FormManager.getActualValue('taxCd1');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (reqType == 'U') {
    if (role == 'REQUESTER') {
      if (tinNo != null && tinNo != '') {
        FormManager.readOnly('taxCd1');
      } else {
        // If field is blank, then editable.
        FormManager.enable('taxCd1');
      }
    } else if (role == 'PROCESSOR') {
      // On Processor side editable in all instances
      FormManager.enable('taxCd1');
    }
  }

  if (scenarioChanged && !fromAddress && scenario != null && scenario != '') {
    FormManager.clearValue('taxCd1');
  }
}
function addTinNumberValidationTz() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var tinNumber = FormManager.getActualValue('taxCd1');

        if (tinNumber.length > 0 && !tinNumber.match("([0-9]{3}-[0-9]{3}-[0-9]{3})")) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'Invalid format of Tin Number. Format should be NNN-NNN-NNN.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}
function addTaxRegFormatValidationMadagascar() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var taxReg = FormManager.getActualValue('busnType');

        if (taxReg.length > 0 && !taxReg.match("([0-9]{5} [0-9]{2} [0-9]{4} [0-9]{1} [0-9]{5})")) {
          return new ValidationResult({
            id : 'busnType',
            type : 'text',
            name : 'busnType'
          }, false, 'Invalid format of Numero Statistique du Client. Format should be NNNNN NN NNNN N NNNNN.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}
function addAttachmentValidatorOnTaxRegMadagascar() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var taxCd1 = FormManager.getActualValue('busnType');
        var reqId = FormManager.getActualValue('reqId');
        var ret = cmr.query('CHECK_VATD_ATTACHMENT', {
          ID : reqId
        });
        if (taxCd1.length > 0) {
          if (ret == null || ret.ret1 == null) {
            return new ValidationResult(null, false, 'VAT/TAX Documentation has not been attached to the request.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}
var _importedIndc = null;
function getImportedIndc() {
  if (_importedIndc) {
    return _importedIndc;
  }
  var results = cmr.query('VALIDATOR.IMPORTED_ZS01', {
    REQID : FormManager.getActualValue('reqId')
  });
  if (results != null && results.ret1) {
    _importedIndc = results.ret1;
  } else {
    _importedIndc = 'N';
  }
  return _importedIndc;
}

function validateCMRForMCO2GMLLCScenario() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking requested cmr number...');
        var requestCMR = FormManager.getActualValue('cmrNo');
        var reqType = FormManager.getActualValue('reqType');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var action = FormManager.getActualValue('yourAction');
        var requestID = FormManager.getActualValue('reqId');
        var landed = 'LANDED COUNTRY';
        var subCustGrp = FormManager.getActualValue('custSubGrp');
        var targetCntry = 'Kenya';

        if (reqType == 'C' && requestCMR != '' && cmrNo && (subCustGrp == 'LLCEX' || subCustGrp == 'XLLCX')) {
          var cmrStatusOrig = getCMRStatus(cntry, requestCMR);

          if (requestCMR.length < 6) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'CMR: ' + requestCMR + ' is invalid. Please enter valid CMR Number');
          }

          var res = cmr.query('GET_LAND_CNTRY_ZS01', {
            REQ_ID : requestID
          });

          if (res && res.ret1) {
            landed = res.ret1;
          }

          var exists = cmr.query('LD.CHECK_EXISTING_CMR_NO', {
            COUNTRY : cntry,
            CMR_NO : requestCMR,
            MANDT : cmr.MANDT
          });

          if (exists && exists.ret1 && action != 'PCM' && cmrStatusOrig != 'C') {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'CMR: ' + requestCMR + ' is already in use in ' + cntry + '. Please use GM LLC sub-scenario in ' + landed + ' to create new CMR under both ' + targetCntry + ' and ' + landed);
          } else {
            exists = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC', {
              COUNTRY : cntry,
              CMR_NO : requestCMR,
              MANDT : cmr.MANDT
            });
            if (exists && exists.ret1 && cmrStatusOrig != 'C') {
              return new ValidationResult({
                id : 'cmrNo',
                type : 'text',
                name : 'cmrNo'
              }, false, 'CMR: ' + requestCMR + ' is already in use in ' + cntry + '. Please use GM LLC sub-scenario in ' + landed + ' to create new CMR under both Kenya and ' + landed);
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

function gmllcExistingCustomerAdditionalValidations() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking requested cmr number...');
        var requestCMR = FormManager.getActualValue('cmrNo');
        var reqType = FormManager.getActualValue('reqType');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var action = FormManager.getActualValue('yourAction');
        var requestID = FormManager.getActualValue('reqId');
        var landed = 'LANDED COUNTRY';
        var subCustGrp = FormManager.getActualValue('custSubGrp');
        var targetCntry = 'Kenya';
        var cntryDupl = '764';

        if (reqType == 'C' && requestCMR != '' && cmrNo && (subCustGrp == 'LLCEX' || subCustGrp == 'XLLCX')) {
          if (requestCMR.length < 6) {
            return;
          }

          var res = cmr.query('GET_LAND_CNTRY_ZS01', {
            REQ_ID : requestID
          });

          if (res && res.ret1) {
            landed = res.ret1;
          }

          var existInOrigCntry = checkIfCmrExist(cntry, requestCMR);
          var existInDuplCntry = checkIfCmrExist(cntryDupl, requestCMR);
          var cmrStatusOrig = getCMRStatus(cntry, requestCMR);
          var cmrStatusDupl = getCMRStatus(cntryDupl, requestCMR);

          if (!existInOrigCntry && !existInDuplCntry && action != 'PCM') {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'CMR does not exist in either ' + landed + ' or Kenya. Please use GM LLC under ' + landed + '. Processors are able to enter specific CMR if needed.');
          }

          if (!existInOrigCntry && cmrStatusDupl == 'C') {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'Please note CMR in Kenya is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' + landed
                + ' and Kenya using GM LLC scenario under ' + landed + '.');
          }

          if (cmrStatusOrig == 'C' && !existInDuplCntry) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'Please note CMR in ' + landed + ' is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' + landed
                + ' country and Kenya using GM LLC scenario under ' + landed + '.');
          } else if (cmrStatusOrig == 'C') {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'Please note CMR in ' + landed + ' is Cancelled. It needs to be either reactivated, or you can create a new CMR under both ' + landed
                + ' country and Kenya using GM LLC scenario under ' + landed + '.');
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

function checkIfCmrExist(cntry, requestCMR) {
  var cmrExist = cmr.query('LD.CHECK_EXISTING_CMR_NO', {
    COUNTRY : cntry,
    CMR_NO : requestCMR,
    MANDT : cmr.MANDT
  });

  if (cmrExist.ret1 == 'Y') {
    return true;
  }

  return false;
}

function getCMRStatus(cntry, requestCMR) {
  var cmrStatus = cmr.query('LD.GET_STATUS', {
    COUNTRY : cntry,
    CMR_NO : requestCMR
  });

  return cmrStatus.ret1;
}

function enableCMRNOMCO2GLLC() {
  console.log('enabling/disabling cmr no...');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var subCustGrp = FormManager.getActualValue('custSubGrp');

  if (role == 'REQUESTER' && (subCustGrp == 'LLCEX' || subCustGrp == 'XLLCX')) {
    FormManager.enable('cmrNo');
    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');
  } else if (role == 'REQUESTER' && (subCustGrp != 'LLCEX' || subCustGrp != 'XLLCX')) {
    FormManager.readOnly('cmrNo');
    FormManager.resetValidations('cmrNo');
  }
}

function enableCmrNumForProcessor() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var isProspect = FormManager.getActualValue('prospLegalInd');
  if (reqType != 'C') {
    return;
  }
  if (dijit.byId('prospLegalInd')) {
    isProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
  }
  console.log("validateCMRNumberForLegacy ifProspect:" + isProspect);
  if ('Y' == isProspect) {
    FormManager.readOnly('cmrNo');
  } else if (role == "PROCESSOR") {
    FormManager.enable('cmrNo');
  } else {
    FormManager.readOnly('cmrNo');
  }
}

function registerMCO2VatValidator() {
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  GEOHandler.registerValidator(addGenericVATValidator(issuingCntry, 'MAIN_CUST_TAB', 'frmCMR'), [ issuingCntry ], null, true);
}

function resetVatRequired() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    if (dijit.byId('vatExempt').get('checked')) {
      FormManager.removeValidator('vat', Validators.REQUIRED);
    } else {
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    }
  }
}

function resetTinRequired() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    if (cntry == '851') {
      if (dijit.byId('taxCd2').get('checked')) {
        FormManager.removeValidator('taxCd1', Validators.REQUIRED);
      } else {
        FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'TIN Number' ], 'MAIN_CUST_TAB');
      }
    }
  }
}

function resetNumeroRequired() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    if (cntry == '700') {
      if (dijit.byId('taxCd2').get('checked')) {
        FormManager.removeValidator('busnType', Validators.REQUIRED);
      } else {
        FormManager.addValidator('busnType', Validators.REQUIRED, [ 'Numero Statistique du Client' ], 'MAIN_CUST_TAB');
      }
    }
  }
}

function requireVATForCrossMCO2() {
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
          TYPE : 'ZS01'
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

function limitClientTierValues(value) {
  var reqType = FormManager.getActualValue('reqType');

  if (!value) {
    value = FormManager.getActualValue('isuCd');
  }

  var tierValues = null;
  tierValues = [ '', 'C', 'S', 'T', 'N', 'V', 'A', '7' ]

  if (reqType == 'C') {
    if (value == '32') {
      tierValues = [ 'C', 'S', 'T', 'N' ];
    } else if (value == '34') {
      tierValues = [ 'V', 'A' ];
    } else if (value == '21' || value == '8B') {
      tierValues = [ '7' ];
    }
  }

  if (tierValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), tierValues);
    preSelectSingleValue(value, tierValues)
  }
}

function preSelectSingleValue(value, tierValues) {
  if ((value == '21' || value == '8B') && tierValues.includes('7')) {
    FormManager.setValue('clientTier', '7');
  }
}

function validateCollectionCd() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = null;
        if (typeof (_pagemodel) != 'undefined') {
          reqType = FormManager.getActualValue('reqType');
        }
        if (reqType != 'U') {
          return new ValidationResult(null, true);
        }
        if (FormManager.getActualValue('viewOnlyPage') == 'true') {
          return new ValidationResult(null, true);
        }
        var collectionCd = FormManager.getActualValue('collectionCd');
        if (collectionCd == '') {
          return new ValidationResult(null, true);
        } else {
          if (collectionCd.length != 6) {
            return new ValidationResult(null, false, 'Collection Code should be 6 characters long.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addEmbargoCodeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd').toUpperCase();
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        if (role == 'REQUESTER' && reqType == 'C') {
          return new ValidationResult(null, true);
        }
        embargoCd = embargoCd.trim();
        if (embargoCd == '' || embargoCd == 'Y' || embargoCd == 'J') {
          return new ValidationResult(null, true);
        } else {
          return new ValidationResult({
            id : 'embargoCd',
            type : 'text',
            name : 'embargoCd'
          }, false, 'Embargo Code value should be only Y, J or Blank.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function resetVatExempt() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    var vat = FormManager.getActualValue('vat');
    var vatExempt = dijit.byId('vatExempt').get('checked');

    if (vat != null && vat.length > 0) {
      if (vatExempt == true) {
        FormManager.getField('vatExempt').set('checked', false);
      }
    }
  }
}

function resetNumeroExempt() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    var numero = FormManager.getActualValue('busnType');
    var numeroExempt = dijit.byId('taxCd2').get('checked');

    if (numero != null && numero.length > 0) {
      if (numeroExempt == true) {
        FormManager.getField('taxCd2').set('checked', false);
      }
    }
  }
}

function resetTinExempt() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    var tin = FormManager.getActualValue('taxCd1');
    var tinExempt = dijit.byId('taxCd2').get('checked');

    if (tin != null && tin.length > 0) {
      if (tinExempt == true) {
        FormManager.getField('taxCd2').set('checked', false);
      }
    }
  }
}

function vatExemptOnScenario() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    var custSubType = FormManager.getActualValue('custSubGrp');
    var vat = FormManager.getActualValue('vat');
    var found = false;
    var subGrp = new Array();
    subGrp = [ 'IBMEM', 'PRICU', 'XIBME', 'XPRIC' ];
    for (var i = 0; i < subGrp.length; i++) {
      if (custSubType == subGrp[i]) {
        if ((vat == null || vat.length == 0)) {
          FormManager.getField('vatExempt').set('checked', true);
          FormManager.removeValidator('vat', Validators.REQUIRED);
        } else {
          FormManager.getField('vatExempt').set('checked', false);
          FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
        }
        found = true;
        break;
      }
    }
    if (found != true) {
      FormManager.getField('vatExempt').set('checked', false);
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    }
  }
}

function numeroExemptOnScenario() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    var custSubType = FormManager.getActualValue('custSubGrp');
    var numero = FormManager.getActualValue('busnType');
    var found = false;
    var subGrp = new Array();
    subGrp = [ 'IBMEM', 'PRICU', 'XBP', 'XCOM', 'XGOV', 'XIBME', 'XINTE', 'XLLCX', 'XPRIC', 'XTP' ];
    for (var i = 0; i < subGrp.length; i++) {
      if (custSubType == subGrp[i]) {
        if ((numero == null || numero.length == 0)) {
          FormManager.getField('taxCd2').set('checked', true);
          FormManager.removeValidator('busnType', Validators.REQUIRED);
        } else {
          FormManager.getField('taxCd2').set('checked', false);
          FormManager.addValidator('busnType', Validators.REQUIRED, [ 'Numero Statistique du Client' ], 'MAIN_CUST_TAB');
        }
        found = true;
        break;
      }
    }
    if (found != true) {
      FormManager.getField('taxCd2').set('checked', false);
      FormManager.addValidator('busnType', Validators.REQUIRED, [ 'Numero Statistique du Client' ], 'MAIN_CUST_TAB');
    }
  }
}

function tinExemptOnScenario() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    var custSubType = FormManager.getActualValue('custSubGrp');
    var tin = FormManager.getActualValue('taxCd1');
    var found = false;
    var subGrp = new Array();
    subGrp = [ 'IBMEM', 'PRICU', 'XBP', 'XCOM', 'XGOV', 'XIBME', 'XINTE', 'XLLCX', 'XPRIC', 'XTP' ];
    for (var i = 0; i < subGrp.length; i++) {
      if (custSubType == subGrp[i]) {
        if ((tin == null || tin.length == 0)) {
          FormManager.getField('taxCd2').set('checked', true);
          FormManager.removeValidator('taxCd1', Validators.REQUIRED);
        } else {
          FormManager.getField('taxCd2').set('checked', false);
          FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'TIN Number' ], 'MAIN_CUST_TAB');
        }
        found = true;
        break;
      }
    }
    if (found != true) {
      FormManager.getField('taxCd2').set('checked', false);
      FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'TIN Number' ], 'MAIN_CUST_TAB');
    }
  }
}

function addSalesBusOffValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('viewOnlyPage') == 'true') {
          return new ValidationResult(null, true);
        }
        var input = FormManager.getActualValue('salesBusOffCd');
        if (input && input.length > 0 && isNaN(input)) {
          return new ValidationResult(null, false, input + ' is not a valid numeric value for SBO/Search Term (SORTL).');
        } else {
          return new ValidationResult(input, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addSBOLengthValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = null;
        var scenario = null;
        if (FormManager.getActualValue('viewOnlyPage') == 'true') {
          return new ValidationResult(null, true);
        }
        var input = FormManager.getActualValue('salesBusOffCd');
        if (input.length != 4) {
          return new ValidationResult(null, false, 'SBO/Search Term (SORTL) should be 4 characters long.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/* End 1430539 */
dojo.addOnLoad(function() {
  GEOHandler.MCO2 = [ '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770',
      '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883' ];
  console.log('adding MCO2 functions...');
  GEOHandler.addAddrFunction(addMCO1LandedCountryHandler, GEOHandler.MCO2);
  GEOHandler.enableCopyAddress(GEOHandler.MCO2, validateMCOCopy, [ 'ZD01', 'ZI01' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.MCO2);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.MCO2);
  GEOHandler.setRevertIsicBehavior(false);

  GEOHandler.addAfterConfig(afterConfigForMCO2, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(lockRequireFieldsMCO2, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(lockRequireFieldsMCO2, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(addHandlersForCEWA, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(setAbbrvNmLoc, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(crossborderScenariosAbbrvLoc, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(scenariosAbbrvLocOnChange, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(setIbmDeptCostCenterBehavior, GEOHandler.MCO2);

  GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(setTypeOfCustomerBehavior, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(lockAbbrv, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(lockCmrOwnerPrefLang, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(showDeptNoForInternalsOnly, GEOHandler.MCO2);
  // GEOHandler.addAfterTemplateLoad(setSalesRepValue, GEOHandler.MCO2);

  // GEOHandler.addAfterConfig(showDeptNoForInternalsOnly, GEOHandler.MCO2);

  GEOHandler.registerValidator(addAddressTypeValidator, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(addAttachmentValidator, GEOHandler.MCO2, null, true);
  // Story 1718889: Tanzania: new mandatory TIN number field fix
  // GEOHandler.addAddrFunction(diplayTinNumberforTZ, [ SysLoc.TANZANIA ]);
  // GEOHandler.registerValidator(addTinFormatValidationTanzania, [
  // SysLoc.TANZANIA ], null, true);

  // GEOHandler.registerValidator(addTinBillingValidator, [ SysLoc.TANZANIA ],
  // null, true);

  // GEOHandler.registerValidator(addTinInfoValidator, GEOHandler.MCO2,
  // GEOHandler.REQUESTER,true);

  GEOHandler.registerValidator(requireVATForCrossMCO2, GEOHandler.MCO2, null, true);

  GEOHandler.addAddrFunction(addAddrValidatorMCO2, GEOHandler.MCO2);
  GEOHandler.addAddrFunction(disableAddrFieldsCEWA, GEOHandler.MCO2);
  GEOHandler.addAddrFunction(changeAbbrevNmLocn, GEOHandler.MCO2);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.MCO2, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.MCO2);

  GEOHandler.addAfterConfig(addHandlersForMCO2, GEOHandler.MCO2);

  GEOHandler.addAfterTemplateLoad(setFieldsBehavior, GEOHandler.MCO2);
  GEOHandler.registerValidator(addStreetAddressFormValidator, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(addAdditionalNameStreetContPOBoxValidator, GEOHandler.MCO2, null, true);
  GEOHandler.addAfterConfig(clearPhoneNoFromGrid, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(clearPOBoxFromGrid, GEOHandler.MCO2);
  GEOHandler.registerValidator(addAddressGridValidatorStreetPOBox, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(addInternalDeptNumberValidator, GEOHandler.MCO2, null, true);
  GEOHandler.addAfterTemplateLoad(requireTaxRegistrationForLocalScenario, SysLoc.MADAGASCAR);
  GEOHandler.registerValidator(addTaxRegFormatValidationMadagascar, [ SysLoc.MADAGASCAR ], null, true);
  GEOHandler.registerValidator(addAttachmentValidatorOnTaxRegMadagascar, [ SysLoc.MADAGASCAR ], null, true);
  GEOHandler.registerValidator(addTinNumberValidationTz, [ SysLoc.TANZANIA ], null, true);
  GEOHandler.addAfterTemplateLoad(setTinNumberBehaviorForTz, SysLoc.TANZANIA);
  GEOHandler.addAfterTemplateLoad(retainImportValues, GEOHandler.MCO2);

  GEOHandler.registerValidator(validateCMRForMCO2GMLLCScenario, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(gmllcExistingCustomerAdditionalValidations, GEOHandler.MCO2, null, true);

  GEOHandler.addAfterConfig(enableCMRNOMCO2GLLC, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(enableCMRNOMCO2GLLC, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(enableCmrNumForProcessor, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(registerMCO2VatValidator, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(resetVatRequired, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(resetTinRequired, SysLoc.TANZANIA);
  GEOHandler.addAfterTemplateLoad(resetNumeroRequired, SysLoc.MADAGASCAR);
  GEOHandler.addAfterConfig(addAbbrvNmAndLocValidator, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(setStreetContBehavior, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(limitClientTierValues, GEOHandler.MCO2);
  GEOHandler.registerValidator(validateCollectionCd, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(addEmbargoCodeValidator, GEOHandler.MCO2, null, true);
  GEOHandler.addAfterConfig(resetVatExempt, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(resetVatExempt, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(resetNumeroExempt, SysLoc.MADAGASCAR);
  GEOHandler.addAfterTemplateLoad(resetNumeroExempt, SysLoc.MADAGASCAR);
  GEOHandler.addAfterConfig(resetTinExempt, SysLoc.TANZANIA);
  GEOHandler.addAfterTemplateLoad(resetTinExempt, SysLoc.TANZANIA);
  GEOHandler.addAfterTemplateLoad(vatExemptOnScenario, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(numeroExemptOnScenario, SysLoc.MADAGASCAR);
  GEOHandler.addAfterTemplateLoad(tinExemptOnScenario, SysLoc.TANZANIA);
  GEOHandler.registerValidator(addSalesBusOffValidator, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(addSBOLengthValidator, GEOHandler.MCO2, null, true);
});