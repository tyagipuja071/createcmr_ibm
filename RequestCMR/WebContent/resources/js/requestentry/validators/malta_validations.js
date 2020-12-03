/* Register Malta Javascripts */
var fstCEWA = [ "373", "382", "383", "635", "637", "656", "662", "667", "670", "691", "692", "700", "717", "718", "753", "810", "840", "841", "876", "879", "880", "881" ];
var othCEWA = [ "610", "636", "645", "669", "698", "725", "745", "764", "769", "770", "782", "804", "825", "827", "831", "833", "835", "842", "851", "857", "883", "780" ];
var _vatExemptHandler = null;

function addMaltaLandedCountryHandler(cntry, addressMode, saving, finalSave) {
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
var _addrTypeHandler = [];
var _addrTypesForMT = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01' ];

function addHandlersForMT() {
  for (var i = 0; i < _addrTypesForMT.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForMT[i]), 'onClick', function(value) {
        disableAddrFieldsMT();
      });
    }
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorMalta();
    });
  }

}

/*
 * Order Block field
 */
function lockOrderBlock() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'U' && role == 'REQUESTER') {
    FormManager.readOnly('ordBlk');
  } else {
    FormManager.enable('ordBlk');
  }
}

/*
 * Classification Code field
 */
function classFieldBehaviour() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (reqType == 'C') {
    FormManager.readOnly('custClass');
  }

  if (reqType == 'U' && role == 'REQUESTER') {
    FormManager.readOnly('custClass');
  } else {
    FormManager.enable('custClass');
  }
}

function disableAddrFieldsMT() {
  var custType = FormManager.getActualValue('custGrp');
  var addrType = FormManager.getActualValue('addrType');

  if (custType == 'LOCAL' && addrType == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
  // Phone - for shipping
  if (addrType == 'ZD01') {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  }

  if (addrType != 'ZS01' && addrType != 'ZP01') {
    FormManager.readOnly('poBox');
    FormManager.setValue('poBox', '');
  } else {
    FormManager.enable('poBox');
  }

}

function addAddressTypeValidator() {
  console.log("addAddressTypeValidator for MALTA..........");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var zs01MT = 0;
          var zp01MT = 0;
          var zi01MT = 0;
          var zd01MT = 0;

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
              zs01MT++;
            } else if (type == 'ZP01') {
              zp01MT++;
            } else if (type == 'ZI01') {
              zi01MT++;
            } else if (type == 'ZD01') {
              zd01MT++;
            }
          }

          if (zs01MT == 0) {
            return new ValidationResult(null, false, 'Sold-To address types is mandatory.');
          } else if (zs01MT > 1) {
            return new ValidationResult(null, false, 'Only one Sold-To address is allowed.');
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

function addAddrValidatorMALTA() {
  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.LATIN, [ 'Name 2' ]);
  FormManager.addValidator('custNm3', Validators.LATIN, [ 'Name 3' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN, [ 'Name 4' ]);
  FormManager.addValidator('city1', Validators.LATIN, [ 'City' ]);
  FormManager.addValidator('abbrevNm', Validators.LATIN, [ 'Abbreviated Name' ]);
  FormManager.addValidator('abbrevLocn', Validators.LATIN, [ 'Abbreviated Location' ]);
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
    $('label[for="custNm2_view"]').text('Name 2:');
    $('label[for="custNm3_view"]').text('Name 3:');
    $('label[for="landCntry_view"]').text('Country (Landed):');
    $('label[for="addrTxt_view"]').text('Street:');
    $('label[for="addrTxt2_view"]').text('Name 4:');
  }
}

function lockAbbrv() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType').toUpperCase();

  if (viewOnlyPage == 'true') {
    FormManager.readOnly('abbrevLocn');
    FormManager.readOnly('abbrevNm');
  } else {
    if (role == 'REQUESTER' && reqType == 'C') {
      FormManager.readOnly('abbrevLocn');
      FormManager.readOnly('abbrevNm');
    }
  }
}

function enterpriseMalta() {
  var reqType = FormManager.getActualValue('reqType').toUpperCase();
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custType = FormManager.getActualValue('custSubGrp');

  if (reqType != 'C') {
    return;
  }

  if (custType == 'COMME' || custType == 'PRICU' || custType == 'THDPT' || custType == 'GOVRN' || custType == 'XCOM' || custType == 'XGOV' || custType == 'XTP') {
    FormManager.show('Enterprise', 'enterprise');
  } else {
    FormManager.hide('Enterprise', 'enterprise');
  }

  if (reqType == 'C' && role == 'REQUESTER') {
    FormManager.readOnly('enterprise');
  } else {
    FormManager.enable('enterprise');
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

function hidePpsceidExceptBP() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var custType = FormManager.getActualValue('custSubGrp');

  if (custType == 'BUSPR' || custType == 'XBP') {
    FormManager.show('PPSCEID', 'ppsceid');
  } else {
    FormManager.hide('PPSCEID', 'ppsceid');
  }
}

function addValidatorStreet() {
  FormManager.removeValidator('addrTxt', Validators.MAXLENGTH);
}

var _addrTypesForMCO2 = [ 'ZD01', 'ZI01', 'ZP01', 'ZS01', 'ZS02' ];
var addrTypeHandler = [];

function addAfterConfigMalta() {
  lockOrderBlock();
  enterpriseMalta();
  hidePpsceidExceptBP();
  classFieldBehaviour();
  setVatValidatorMalta();
  disableEnableFieldsForMT();
  setAddressDetailsForView();
}

function addAfterTemplateLoadMalta(fromAddress, scenario, scenarioChanged) {
  if (scenarioChanged) {
    if (FormManager.getActualValue('custPrefLang') == '') {
      FormManager.setValue('custPrefLang', 'E');
    }
  }
  enterpriseMalta();
  hidePpsceidExceptBP();
}

function disableEnableFieldsForMT() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');

  if (reqType == 'C') {
    FormManager.readOnly('sensitiveFlag');
  }

  FormManager.setValue('capInd', true);
  FormManager.readOnly('capInd');

  if (reqType != 'C') {
    FormManager.readOnly('cmrNo');
  } else if (reqType == 'C' && role == 'REQUESTER') {
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('specialTaxCd');
    FormManager.readOnly('custPrefLang');
  } else {
    FormManager.enable('cmrNo');
    FormManager.enable('cmrOwner');
    FormManager.enable('specialTaxCd');
    FormManager.enable('custPrefLang');
  }

  if (role == 'REQUESTER') {
    FormManager.readOnly('custPrefLang');
  } else {
    FormManager.enable('custPrefLang');
  }

}

function addOrdBlkValidator() {
  console.log("addOrdBlkValidator for Malta..");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = null;
        var ordBlk = FormManager.getActualValue('ordBlk');
        if (typeof (_pagemodel) != 'undefined') {
          reqType = FormManager.getActualValue('reqType');
        }
        if ("C" == reqType) {
          if (ordBlk == null || ordBlk == '88' || ordBlk == '94' || ordBlk == '') {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult(null, false, 'Value of Order block can only be 88 or 94 or blank.');
          }
        } else {
          if (ordBlk == null || ordBlk == '88' || ordBlk == '94' || ordBlk == '@' || ordBlk == '') {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult(null, false, 'Value of Order block can only be 88 or 94 or @ or blank.');
          }
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function setVatValidatorMalta() {
  console.log("setVatValidatorMalta for Malta..");
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (!dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
    }
  }
}

function addISICValidatorForScenario() {
  console.log("addISICValidatorForScenario for Malta..");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = null;
        var _custType = FormManager.getActualValue('custSubGrp');
        var _isicCd = FormManager.getActualValue('isicCd');
        if (typeof (_pagemodel) != 'undefined') {
          reqType = FormManager.getActualValue('reqType');
        }
        if ("C" == reqType) {
          if ((_custType != null && _isicCd != null) && _custType != 'PRICU' && _isicCd == '9500') {
            return new ValidationResult(null, false, 'ISIC value 9500 can be entered only for CMR with Classification code 60 (Private Person).');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function countryScenarioProcessorRules() {
  return true;
}

function addIsicClassificationCodeValidator() {
  console.log("Validator for ISIC & Classification Code for Malta.");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('reqType') == 'C' || FormManager.getActualValue('reqType') == 'U') {

          var field = FormManager.getField('custClass');
          var value = FormManager.getActualValue('isicCd');

          if (value == '9500' && field == '60') {
            return new ValidationResult(null, true);
          } else if (value != '9500' && field == '60') {
            return new ValidationResult(null, false, 'ISIC value 9500 can be entered only for CMR with Classification code 60 (Private Person)');
          } else if (value == '9500' && field != '60') {
            return new ValidationResult(null, false, 'ISIC value 9500 can be entered only for CMR with Classification code 60 (Private Person)');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

/* End 1430539 */
dojo.addOnLoad(function() {
  GEOHandler.MCO2 = [ '780' ];
  console.log('adding MALTA functions...');
  GEOHandler.enableCopyAddress(GEOHandler.MCO2, validateMCOCopy, [ 'ZD01' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.MCO2);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.MCO2);
  GEOHandler.setRevertIsicBehavior(false);

  GEOHandler.addAfterConfig(setAbbrvNmLoc, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(crossborderScenariosAbbrvLoc, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(scenariosAbbrvLocOnChange, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(streetAvenueValidator, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(lockAbbrv, GEOHandler.MCO2);
  // GEOHandler.addAfterTemplateLoad(setSalesRepValue, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(setScenarioBehaviour, GEOHandler.MCO2);
  GEOHandler.addAfterTemplateLoad(addValidatorStreet, GEOHandler.MCO2);
  GEOHandler.addAfterConfig(addValidatorStreet, GEOHandler.MCO2);

  GEOHandler.registerValidator(addAttachmentValidator, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.MALTA, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.MALTA ], null, true);

  // GEOHandler.registerValidator(addTinInfoValidator, GEOHandler.MCO2,
  // GEOHandler.REQUESTER,true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.TANZANIA, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.TANZANIA ], null, true);
  GEOHandler.registerValidator(requireVATForCrossBorder, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(streetValidatorCustom, GEOHandler.MCO2, null, true);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.MCO2, GEOHandler.ROLE_PROCESSOR, true);

  // Malta Legacy
  GEOHandler.addAfterConfig(addHandlersForMT, [ SysLoc.MALTA ]);
  GEOHandler.addAfterConfig(addAfterConfigMalta, [ SysLoc.MALTA ]);
  GEOHandler.addAddrFunction(changeAbbrevNmLocn, [ SysLoc.MALTA ]);
  GEOHandler.addAddrFunction(disableAddrFieldsMT, [ SysLoc.MALTA ]);
  GEOHandler.addAddrFunction(addAddrValidatorMALTA, [ SysLoc.MALTA ]);
  GEOHandler.addAddrFunction(addMaltaLandedCountryHandler, [ SysLoc.MALTA ]);
  GEOHandler.addAfterTemplateLoad(addAfterTemplateLoadMalta, [ SysLoc.MALTA ]);
  GEOHandler.registerValidator(addOrdBlkValidator, [ SysLoc.MALTA ], null, true);
  GEOHandler.registerValidator(addAddressTypeValidator, [ SysLoc.MALTA ], null, true);
  GEOHandler.registerValidator(addAddressFieldValidators, [ SysLoc.MALTA ], null, true);
  GEOHandler.registerValidator(addISICValidatorForScenario, [ SysLoc.MALTA ], null, true);
  GEOHandler.registerValidator(addIsicClassificationCodeValidator, [ SysLoc.MALTA ], null, true);

});