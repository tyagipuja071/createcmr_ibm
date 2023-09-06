/* Register MCO Javascripts */
var fstCEWA = [ "373", "382", "383", "635", "637", "656", "662", "667", "670", "691", "692", "700", "717", "718", "753", "810", "840", "841", "876", "879", "880", "881" ];
var othCEWA = [ "610", "636", "645", "669", "698", "725", "745", "764", "769", "770", "782", "804", "825", "827", "831", "833", "835", "842", "851", "857", "883", "780" ];

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

function addHandlersForMCO2() {

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setSalesRepValues(value);
    });
  }

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setSalesRepValues(value);
    });
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
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    FormManager.readOnly('embargoCd');
  } else {
    FormManager.enable('embargoCd');
  }
}

function afterConfigForMCO2() {
  FormManager.setValue('capInd', true);
  FormManager.readOnly('capInd');
  // CREATCMR-788
  addressQuotationValidatorMCO2();
}

/**
 * sets fields to lock/mandatory, not scenario handled
 */
function lockRequireFieldsMCO2() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole');

  // fields locked for Requester
  if (reqType == 'C' && role == GEOHandler.ROLE_REQUESTER) {
    FormManager.readOnly('specialTaxCd');
    // FormManager.readOnly('salesBusOffCd');
    // FormManager.readOnly('repTeamMemberNo');
    // FormManager.readOnly('isuCd');
    // FormManager.readOnly('clientTier');
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

  // Phone - for shipping and EPL addresses
  if (addrType == 'ZD01' || addrType == 'ZS02') {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
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

          if (zs01Cnt == 0 || zp01Cnt == 0 || zi01Cnt == 0 || zd01Cnt == 0 || zs02Cnt == 0) {
            return new ValidationResult(null, false, 'All address types are mandatory.');
          } else if (zs01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Billing address is allowed.');
          } else if (zp01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Mailing address is allowed.');
          } else if (zi01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Installing address is allowed.');
          } else if (zs02Cnt > 1) {
            return new ValidationResult(null, false, 'Only one EPL address is allowed.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
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

  // CEWA - postal code should accept alphanumeric and spaces
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postCd = FormManager.getActualValue('postCd');
        if (postCd && postCd.length > 0 && !postCd.match("^[a-zA-Z0-9 ]*$")) {
          return new ValidationResult(null, false, postCd + ' is not a valid value for Postal Code. Only alphabets, numbers, and spaces combination is valid.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // addrCont + poBox should not exceed 28 characters
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var stCont = FormManager.getActualValue('addrTxt2');
        var poBox = FormManager.getActualValue('poBox');
        var val = stCont;

        if (poBox != '') {
          val += poBox;
          if (val != null && val.length > 28) {
            return new ValidationResult(null, false, 'Total computed length of Street Con\'t and PO Box should not exceed 28 characters.');
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
  FormManager.addValidator('custNm4', Validators.LATIN, [ 'Division/ Floor/ Building/ Department/ Attention Person' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN, [ 'Street Continuation' ]);
  FormManager.addValidator('city1', Validators.LATIN, [ 'City' ]);
  FormManager.addValidator('abbrevNm', Validators.LATIN, [ 'Abbreviated Name' ]);
  FormManager.addValidator('abbrevLocn', Validators.LATIN, [ 'Abbreviated Location' ]);

  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
}

function streetAvenueValidator() {
  dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {
    var addrVal = FormManager.getActualValue('addrTxt').toUpperCase();
    var addrPlain = FormManager.getActualValue('addrTxt');
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    var indexes = new Array();
    var isFST = false;
    var isOthers = false;
    var isPresent = false;

    if (fstCEWA.indexOf(cntry) > -1) {
      isFST = true;
    } else if (othCEWA.indexOf(cntry) > -1) {
      isOthers = true;
    }

    indexes.push(addrVal.match(/(^|\W)AVENUE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)AV($|\W)/));
    indexes.push(addrVal.match(/(^|\W)STREET($|\W)/));
    indexes.push(addrVal.match(/(^|\W)STR($|\W)/));
    indexes.push(addrVal.match(/(^|\W)ROAD($|\W)/));
    indexes.push(addrVal.match(/(^|\W)RD($|\W)/));

    indexes.push(addrVal.match(/(^|\W)STRASSE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)STAAT($|\W)/));
    indexes.push(addrVal.match(/(^|\W)STRAAT($|\W)/));
    indexes.push(addrVal.match(/(^|\W)AVENUR($|\W)/));
    indexes.push(addrVal.match(/(^|\W)NO($|\W)/));
    indexes.push(addrVal.match(/(^|\W)NR($|\W)/));
    indexes.push(addrVal.match(/(^|\W)AVENIDA($|\W)/));
    indexes.push(addrVal.match(/(^|\W)RUA($|\W)/));
    indexes.push(addrVal.match(/(^|\W)CIRCLE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)ST($|\W)/));
    indexes.push(addrVal.match(/(^|\W)SQUARE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)ESTATE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)ESTRADA($|\W)/));
    indexes.push(addrVal.match(/(^|\W)DRIVE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)PLAZA($|\W)/));
    indexes.push(addrVal.match(/(^|\W)AREA($|\W)/));
    indexes.push(addrVal.match(/(^|\W)LANE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)BUILDING($|\W)/));
    indexes.push(addrVal.match(/(^|\W)SUITE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)HOUSE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)FLOOR($|\W)/));
    indexes.push(addrVal.match(/(^|\W)PLACE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)BLDG($|\W)/));
    indexes.push(addrVal.match(/(^|\W)BLOCK($|\W)/));
    indexes.push(addrVal.match(/(^|\W)BRIDGE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)RIDGE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)BR($|\W)/));
    indexes.push(addrVal.match(/(^|\W)UNIT($|\W)/));
    indexes.push(addrVal.match(/(^|\W)PARK($|\W)/));
    indexes.push(addrVal.match(/(^|\W)FACTORY($|\W)/));
    indexes.push(addrVal.match(/(^|\W)SURREY($|\W)/));
    indexes.push(addrVal.match(/(^|\W)MIDDLESEX($|\W)/));
    indexes.push(addrVal.match(/(^|\W)BLD($|\W)/));
    indexes.push(addrVal.match(/(^|\W)S($|\W)/));
    indexes.push(addrVal.match(/(^|\W)B($|\W)/));
    indexes.push(addrVal.match(/(^|\W)P($|\W)/));

    indexes.push(addrVal.match(/(^|\W)RUE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)ROUTE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)BOULEVARD($|\W)/));
    indexes.push(addrVal.match(/(^|\W)AVE($|\W)/));
    indexes.push(addrVal.match(/(^|\W)WAY($|\W)/));
    indexes.push(addrVal.match(/(^|\W)CITICENTER($|\W)/));

    for (var i = 0; i < indexes.length; i++) {
      if (indexes[i] != null) {
        isPresent = true;
        break;
      }
    }

    if (!(isPresent)) {
      if (isFST) {
        if (addrPlain != null && addrPlain.length > 0) {
          FormManager.setValue('addrTxt', addrPlain.trim() + ' Avenue');
        }
      } else if (isOthers) {
        if (addrPlain != null && addrPlain.length > 0) {
          FormManager.setValue('addrTxt', addrPlain.trim() + ' Street');
        }
      }
    }
  });
}

function setScenarioBehaviour() {
  var custGroup = FormManager.getActualValue('custGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var cntrySalesRep = cntry + cntry;
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.MALTA) {
    if (custGroup == 'LOCAL') {
      FormManager.setValue('repTeamMemberNo', cntrySalesRep);
    } else if (custGroup == 'CROSS') {
      FormManager.setValue('repTeamMemberNo', "016757");
    }
  }
}

function setScenarioBehaviourOnchange() {
  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    setScenarioBehaviour();
  });
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
    $('label[for="custNm4_view"]').text('Division/ Floor/ Building/ Department/ Attention Person:');
    $('label[for="addrTxt_view"]').text('Street:');
    $('label[for="addrTxt2_view"]').text('Street Continuation:');
    $('label[for="custPhone_view"]').text('Phone #:');
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
      FormManager.readOnly('abbrevLocn');
      FormManager.readOnly('abbrevNm');
    }
  }
}

function showDeptNoForInternalsOnly() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var subCustGrp = FormManager.getActualValue('custSubGrp');
  if (subCustGrp == 'INTER' || subCustGrp == 'XINTE') {
    checkAndAddValidator('ibmDeptCostCenter', Validators.REQUIRED, [ 'Internal Department Number' ]);
    FormManager.show('InternalDept', 'ibmDeptCostCenter');
  } else {
    FormManager.clearValue('ibmDeptCostCenter');
    FormManager.resetValidations('ibmDeptCostCenter');
    FormManager.hide('InternalDept', 'ibmDeptCostCenter');
  }
}

function enterpriseMalta() {
  var req = FormManager.getActualValue('reqType').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (cntry != '780') {
    FormManager.hide('Enterprise', 'enterprise');
    return;
  }

  if (req == 'U' || (cntry == '780' && req == 'C')) {
    FormManager.show('Enterprise', 'enterprise');

  } else {
    FormManager.hide('Enterprise', 'enterprise');
  }

}

function setEntpMaltaValues() {
  var req = FormManager.getActualValue('reqType').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole');

  if ((cntry == '780' && req == 'C')) {
    if (custSubGrp != 'BUSPR' && custSubGrp != 'XBP') {
      FormManager.setValue('enterprise', '985204');
    } else {
      FormManager.clearValue('enterprise');
    }

    if (role == 'Requester') {
      FormManager.readOnly('enterprise');
    } else {
      FormManager.enable('enterprise');
    }
  }
}

function streetValidatorCustom() {
  console.log("streetValidatorCustom..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrPlain = FormManager.getActualValue('addrTxt').trim();
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var isFST = false;
        var isOthers = false;

        if (fstCEWA.indexOf(cntry) > -1) {
          isFST = true;
        } else if (othCEWA.indexOf(cntry) > -1) {
          isOthers = true;
        }

        if (isFST) {
          if (addrPlain != null && addrPlain.length > 30) {
            return new ValidationResult(FormManager.getField('addrTxt'), false, 'Street value should be at most 23 CHAR long + "Avenue".');
          } else {
            return new ValidationResult(null, true);
          }
        } else if (isOthers) {
          if (addrPlain != null && addrPlain.length > 30) {
            return new ValidationResult(FormManager.getField('addrTxt'), false, 'Street value should be at most 23 CHAR long + "Street".');
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

function hideCustName4() {
  var custGroup = FormManager.getActualValue('custGrp');
  if (custGroup == "CROSS") {
    FormManager.hide('CustomerName4', 'custNm4');
  } else {
    FormManager.show('CustomerName4', 'custNm4');
  }
}

function addValidatorStreet() {
  FormManager.removeValidator('addrTxt', Validators.MAXLENGTH);
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

  if (cntry == SysLoc.MALTA) {
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
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        salesReps.push(results[i].ret1);
      }
      if (salesReps != null) {
        FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesReps);
        if (salesReps.length == 1) {
          FormManager.setValue('repTeamMemberNo', salesReps[0]);
        }
        if (salesReps.length == 0) {
          FormManager.setValue('repTeamMemberNo', '');
        }
      }
    }

    if (cntry == '764' || cntry == '831' || cntry == '851' || cntry == '857') {
      if (isuCd == '32' && (clientTier == 'S' || clientTier == 'C' || clientTier == 'T')) {
        FormManager.setValue('repTeamMemberNo', 'DUMMY8');
        FormManager.setValue('salesBusOffCd', '0080');
      } else {
        FormManager.setValue('salesBusOffCd', '0010');
      }
    } else if (cntry == '698' || cntry == '745') {
      if (isuCd == '32' && (clientTier == 'S' || clientTier == 'C' || clientTier == 'T')) {
        FormManager.setValue('repTeamMemberNo', 'DUMMY6');
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

/* End 1430539 */

// CREATCMR-4293
function setCTCValues() {

  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  // Business Partner
  var custSubGrpForBusinessPartner = [ 'BUSPR', 'LSBP', 'LSXBP', 'NABP', 'NAXBP', 'SZBP', 'SZXBP', 'XBP', 'ZABP', 'ZAXBP', 'LLCBP' ];

  // Business Partner
  if (custSubGrpForBusinessPartner.includes(custSubGrp)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '8B') {
      FormManager.setValue('clientTier', '');
    }
  }

  // Internal
  var custSubGrpForInternal = [ 'INTER', 'LSINT', 'LSXIN', 'NAINT', 'NAXIN', 'SZINT', 'SZXIN', 'XINTE', 'ZAINT', 'ZAXIN' ];

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
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var isuCode = FormManager.getActualValue('isuCd');
        var clientTierCode = FormManager.getActualValue('clientTier');

        if (isuCode == '21' || isuCode == '8B') {
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
            FormManager.addValidator('clientTier', Validators.REQUIRED);
            return new ValidationResult({
              id : 'clientTier',
              type : 'text',
              name : 'clientTier'
            }, false, 'Client Tier code is Mandatory.');
          } else if (clientTierCode == 'Q' || clientTierCode == 'Y') {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult({
              id : 'clientTier',
              type : 'text',
              name : 'clientTier'
            }, false, 'Client Tier can only accept \'Q\' or \'Y\'.');
          }
        } else {
          if (clientTierCode == 'Q' || clientTierCode == 'Y' || clientTierCode == '') {
            $("#clientTierSpan").html('');

            return new ValidationResult(null, true);
          } else {
            $("#clientTierSpan").html('');
            $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

            return new ValidationResult({
              id : 'clientTier',
              type : 'text',
              name : 'clientTier'
            }, false, 'Client Tier can only accept \'Q\', \'Y\' or blank.');
          }
        }

      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}
// CREATCMR-4293

function setPPSCEIDRequired() {
  var reqType = FormManager.getActualValue('reqType');
  var subGrp = FormManager.getActualValue('custSubGrp');
  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (subGrp.includes('BP') || subGrp.includes('BUS')) {
    FormManager.enable('ppsceid');
    FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.clearValue('ppsceid');
    FormManager.readOnly('ppsceid');
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
  }
}
// CREATCMR-788
function addressQuotationValidatorMCO2() {
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Continuation' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Additional Name or Address Information' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Street Continuation' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);

}
dojo.addOnLoad(function() {
  GEOHandler.MCO2 = [ '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770',
      '780', '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883' ];
  console.log('adding MCO2 functions...');
  GEOHandler.addAddrFunction(addMCO1LandedCountryHandler, GEOHandler.MCO2);
  GEOHandler.enableCopyAddress(GEOHandler.MCO2, validateMCOCopy, [ 'ZD01' ]);
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
  GEOHandler.addAfterConfig(streetAvenueValidator, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(enterpriseMalta, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(setEntpMaltaValues, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(lockAbbrv, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(showDeptNoForInternalsOnly, GEOHandler.MCO2);
  // GEOHandler.addAfterTemplateLoad(setSalesRepValue, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(setScenarioBehaviour, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(showDeptNoForInternalsOnly, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(hideCustName4, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(hideCustName4, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(addValidatorStreet, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(addValidatorStreet, GEOHandler.MCO2);

  GEOHandler.registerValidator(addAddressTypeValidator, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(addAttachmentValidator, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.MALTA, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.MALTA ], null, true);
  // Story 1718889: Tanzania: new mandatory TIN number field fix
  GEOHandler.addAddrFunction(diplayTinNumberforTZ, [ SysLoc.TANZANIA ]);
  GEOHandler.registerValidator(addTinFormatValidationTanzania, [ SysLoc.TANZANIA ], null, true);

  GEOHandler.registerValidator(addTinBillingValidator, [ SysLoc.TANZANIA ], null, true);

  // GEOHandler.registerValidator(addTinInfoValidator, GEOHandler.MCO2,
  // GEOHandler.REQUESTER,true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.TANZANIA, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.TANZANIA ], null, true);
  GEOHandler.registerValidator(requireVATForCrossBorder, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(streetValidatorCustom, GEOHandler.MCO2, null, true);

  GEOHandler.addAddrFunction(addAddrValidatorMCO2, GEOHandler.MCO2);
  GEOHandler.addAddrFunction(disableAddrFieldsCEWA, GEOHandler.MCO2);
  GEOHandler.addAddrFunction(changeAbbrevNmLocn, GEOHandler.MCO2);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.MCO2, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.MCO2);

  GEOHandler.addAfterConfig(addHandlersForMCO2, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(setPPSCEIDRequired, GEOHandler.MCO2);

  // CREATCMR-4293
  GEOHandler.addAfterTemplateLoad(setCTCValues, GEOHandler.MCO2);
  GEOHandler.registerValidator(clientTierCodeValidator, GEOHandler.MCO2, null, true);

});