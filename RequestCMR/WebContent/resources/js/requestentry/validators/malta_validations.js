/* Register Malta Javascripts */
var fstCEWA = [ "373", "382", "383", "635", "637", "656", "662", "667", "670", "691", "692", "700", "717", "718", "753", "810", "840", "841", "876", "879", "880", "881" ];
var othCEWA = [ "610", "636", "645", "669", "698", "725", "745", "764", "769", "770", "782", "804", "825", "827", "831", "833", "835", "842", "851", "857", "883", "780" ];
var _vatExemptHandler = null;

function addMaltaLandedCountryHandler(cntry, addressMode, saving, finalSave) {
  console.log(">>>> addMaltaLandedCountryHandler");
  var custGrp = FormManager.getActualValue('custGrp');
  var addrType = FormManager.getActualValue('addrType');
  var landCntry = 'MT'; // default to Malta
  // set default landed country
  FormManager.setValue('defaultLandedCountry', landCntry);

  if (!saving) {
    if (addressMode == 'newAddress') {
      if (custGrp != 'CROSS') {
        FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
        FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
      } else if (custGrp == 'CROSS' && addrType == 'ZS01') {
        FormManager.setValue('landCntry', '');
      }
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
  console.log(">>>> addHandlersForMT");
  for (var i = 0; i < _addrTypesForMT.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForMT[i]), 'onClick', function(value) {
        disableAddrFieldsMT();
      });
    }
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    if (_vatExemptHandler == null) {
      _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
        setVatValidatorMalta();
      });
    }
  }
}

function addISUHandler() {
  console.log(">>>> addISUHandler");
  var isuHandler = null;
  _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    setClientTierValuesMT(value);
    setValuesWRTIsuCtc();
  });
  _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
    setClientTierValuesMT();
    setValuesWRTIsuCtc(value);
  });
}

function addCTCHandler() {
  console.log(">>>> addCTCHandler");
  var _ctcHandler = null;
  _ctcHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    setClientTierValuesMT(value);
  });
}

/*
 * Order Block field
 */
function lockOrderBlock() {
  console.log(">>>> lockOrderBlock");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'U' && role == 'REQUESTER') {
    FormManager.readOnly('custAcctType');
  } else {
    FormManager.enable('custAcctType');
  }
  lockUnlockFieldForMALTA();
}

/*
 * Classification Code field
 */
function classFieldBehaviour() {
  console.log(">>>> classFieldBehaviour");
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
  lockUnlockFieldForMALTA();
}

function disableAddrFieldsMT(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> disableAddrFieldsMT");
  var custType = FormManager.getActualValue('custGrp');
  var addrType = FormManager.getActualValue('addrType');
  var reqType = FormManager.getActualValue('reqType');
  if ((custType == 'LOCAL' || reqType == 'U') && addrType == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    if (!saving && addressMode != 'updateAddress') {
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    }

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
  lockUnlockFieldForMALTA();

}

function addAddressTypeValidator() {
  console.log(">>>> addAddressTypeValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType_ZS01');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          var zs01MT = 0;

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
            }
          }
          if (zs01MT == 0) {
            return new ValidationResult(null, false, 'Sold-To address types is mandatory.');
          }
        }
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

function changeAbbrevNmLocn(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> changeAbbrevNmLocn");
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
  console.log(">>>> setAbbrvNmLoc");
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
  console.log(">>>> crossborderScenariosAbbrvLoc");
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
  console.log(">>>> scenariosAbbrvLocOnChange");
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
  console.log(">>>> addAddrValidatorMALTA");
  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.LATIN, [ 'Name 2' ]);
  FormManager.addValidator('custNm3', Validators.LATIN, [ 'Name 3' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN, [ 'Name 4' ]);
  FormManager.addValidator('city1', Validators.LATIN, [ 'City' ]);
  FormManager.addValidator('abbrevNm', Validators.LATIN, [ 'Abbreviated Name' ]);
  FormManager.addValidator('abbrevLocn', Validators.LATIN, [ 'Abbreviated Location' ]);
}

function streetAvenueValidator() {
  console.log(">>>> streetAvenueValidator");
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
      }
    }
  });
}

function setScenarioBehaviour() {
  console.log(">>>> setScenarioBehaviour");
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
  console.log(">>>> setScenarioBehaviourOnchange");
  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    setScenarioBehaviour();
  });
}

function addAttachmentValidator() {
  console.log(">>>> addAttachmentValidator");
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
              } /*
                 * else if (recordCount == 0) { return new
                 * ValidationResult(null, false, 'Proof of address is
                 * mandatory.'); }
                 */
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
                  } /*
                     * else if (recordCount == 0) { return new
                     * ValidationResult(null, false, 'Proof of address is
                     * mandatory.'); }
                     */
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
                  } /*
                     * else if (recordCount == 0) { return new
                     * ValidationResult(null, false, 'Proof of address is
                     * mandatory.'); }
                     */
                }
              } else {
                counter = counter + 1;
              }
            }

            if (counter > 0) {
              return new ValidationResult(null, true);
            } /*
               * else { return new ValidationResult(null, false, 'Proof of
               * address is mandatory.'); }
               */
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
  console.log(">>>> setAddressDetailsForView");
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
  console.log(">>>> lockAbbrv");
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
  lockUnlockFieldForMALTA();
}
var _CTCHandlerMT = null;
function enterpriseMalta() {
  console.log(">>>> enterpriseMalta");
  var reqType = FormManager.getActualValue('reqType').toUpperCase();
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custType = FormManager.getActualValue('custSubGrp');

  if (reqType != 'C') {
    return;
  }

  if (custType == 'BUSPR' || custType == 'XBP' || custType == 'INTER' || custType == 'XINTE') {
    FormManager.hide('Enterprise', 'enterprise');
  } else {
    FormManager.show('Enterprise', 'enterprise');
  }

  if (role != 'PROCESSOR') {
    FormManager.readOnly('enterprise');
  } else {
    FormManager.enable('enterprise');
  }

  if (_CTCHandlerMT == null) {
    _CTCHandlerMT = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.MALTA) {
        if (FormManager.getActualValue('isuCd') == '34' && FormManager.getActualValue('clientTier') == 'Y') {
          FormManager.setValue('enterprise', '985205');
          FormManager.setValue('salesBusOffCd', '0010');
        } else if (FormManager.getActualValue('isuCd') == '34' && FormManager.getActualValue('clientTier') == 'Q') {
          FormManager.setValue('enterprise', '985204');
          FormManager.setValue('salesBusOffCd', '0010');
        }
      }
    });
  }
  lockUnlockFieldForMALTA();
}

function setClientTierValuesMT(isu) {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  console.log(">>>> setClientTierValuesMT");
  var reqType = FormManager.getActualValue('reqType');
  var clientTier = FormManager.getActualValue('clientTier');
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var custSubGroupSet = new Set([ 'CRINT', 'CRBUS', 'BUSPR', 'INTER', 'IBMEM' ]);

  if (!isu) {
    isu = FormManager.getActualValue('isuCd');
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.MALTA) {
    if (isu == '34' && custSubGroup == 'PRICU') {
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('enterprise', '985204');
    } else if (isu == '34' && custSubGroup != 'PRICU') {
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('enterprise', '985204');
    } else if (isu == '36') {
      FormManager.setValue('clientTier', 'Y');
      FormManager.setValue('enterprise', '822830');
    } else if (isu == '34') {
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('enterprise', '822830');
    } else if (isu == '5K') {
      FormManager.setValue('clientTier', '');
      FormManager.setValue('enterprise', '822830');
    } else if (isu == '21' && custSubGroup == 'IBMEM') {
      FormManager.setValue('clientTier', '');
      FormManager.setValue('enterprise', '985999');
    } else if (isu == '21' && (custSubGroup == 'INTER' || custSubGroup == 'BUSPR')) {
      FormManager.setValue('clientTier', '');
      FormManager.enable('clientTier');
    }
    lockUnlockFieldForMALTA();
  }

}

function enterpriseValidation() {
  console.log(">>>> enterpriseValidation");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var numPattern = /^[0-9]+$/;
        var reqType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var enterprise = FormManager.getActualValue('enterprise');
        if ((reqType != 'U') && (custSubGrp == 'BUSPR' || custSubGrp == 'XBP')) {
          return;
        }
        if (enterprise.length >= 1 && enterprise.length != 6) {
          return new ValidationResult(null, false, 'Enterprise Number should be 6 digit long.');
        }
        if (enterprise.length > 1 && !enterprise.match(numPattern) && (reqType != 'C')) {
          return new ValidationResult({
            id : 'enterprise',
            type : 'text',
            name : 'enterprise'
          }, false, 'Enterprise Number should be numeric only.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/* 1430539 - do not allow delete of imported addresses on update requests */

function canRemoveAddress(value, rowIndex, grid) {
  console.log(">>>> canRemoveAddress");
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
  console.log(">>>> hidePpsceidExceptBP");
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

function cmrNoforProspect() {
  console.log(">>>> cmrNoforProspect");
  var cmrNO = FormManager.getActualValue('cmrNo');
  var ifProspect = FormManager.getActualValue('prospLegalInd');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (dijit.byId('prospLegalInd')) {
    ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
  }
  console.log("cmrNoforProspect ifProspect:" + ifProspect);
  if (role == 'REQUESTER' || 'Y' == ifProspect || (role != 'REQUESTER' && cmrNO.startsWith('P'))) {
    FormManager.readOnly('cmrNo');
  } else if ('Y' != ifProspect && (role == 'PROCESSOR') && reqType != 'U') {
    FormManager.enable('cmrNo');
  } else {
    FormManager.readOnly('cmrNo');
  }
  lockUnlockFieldForMALTA();
}

function addAfterConfigMalta() {
  console.log(">>>> addAfterConfigMalta");
  lockOrderBlock();
  enterpriseMalta();
  cmrNoforProspect();
  classFieldBehaviour();
  // setVatValidatorMalta();
  setVatExemptValidatorMalta();
  disableEnableFieldsForMT();
  setAddressDetailsForView();
  // disable copy address
  GEOHandler.disableCopyAddress();
  FormManager.removeValidator('vat', Validators.REQUIRED);
  // CREATCMR-788
  addressQuotationValidatorMalta();
}

function addAfterTemplateLoadMalta(fromAddress, scenario, scenarioChanged) {
  console.log(">>>> addAfterTemplateLoadMalta");
  if (scenarioChanged) {
    if (FormManager.getActualValue('custPrefLang') == '') {
      FormManager.setValue('custPrefLang', 'E');
    }
  }
  enterpriseMalta();
  hidePpsceidExceptBP();
  setVatValidatorMalta();
  setVatExemptValidatorMalta();
  lockUnlockFieldForMALTA();
}

function canCopyAddress(value, rowIndex, grid) {
  return false;
}

function disableEnableFieldsForMT() {
  console.log(">>>> disableEnableFieldsForMT");

  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custType = FormManager.getActualValue('custGrp');
  var reqType = FormManager.getActualValue('reqType');
  var ifProspect = FormManager.getActualValue('prospLegalInd');
  if (dijit.byId('prospLegalInd')) {
    ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
  }

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
  } else if (reqType == 'C' && role == 'PROCESSOR' && ifProspect == 'Y') {
    FormManager.enable('cmrNo');
  } else if (reqType == 'C' && role == 'PROCESSOR') {
    FormManager.enable('cmrNo');
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

  var custType = FormManager.getActualValue('custSubGrp');
  if (custType == 'BUSPR' || custType == 'XBP') {
    FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPSCEID' ]);
  } else {
    FormManager.setValue('ppsceid', '');
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
  }
  lockUnlockFieldForMALTA();
}

function addOrdBlkValidator() {
  console.log(">>>> addOrdBlkValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = null;
        var ordBlk = FormManager.getActualValue('custAcctType');
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
  console.log(">>>> setVatValidatorMalta");
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C' && custGrp == 'LOCAL' || custGrp == 'CROSS') {
    FormManager.resetValidations('vat');
    if (custSubGrp == 'GOVRN' || custSubGrp == 'INTER' || custSubGrp == 'PRICU' || custSubGrp == 'IBMEM' || custSubGrp == 'XGOV') {
      FormManager.readOnly('vat');
    }
    if (dijit.byId('vatExempt').get('checked')) {
      FormManager.clearValue('vat');
      FormManager.readOnly('vat');
    }
    if (!dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
      FormManager.enable('vat');
    }
  }
}

function addISICValidatorForScenario() {
  console.log(">>>> addISICValidatorForScenario");
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
          if ((_custType != null && _isicCd != null) && (_custType != 'PRICU' || _custType != 'IBMEM') && _isicCd == '9500') {
            return new ValidationResult(null, false, 'ISIC value 9500 can be entered only for CMR with Classification code 60/71 (Private Person/IBM Employee).');
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
  console.log(">>>> addIsicClassificationCodeValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('reqType') == 'C' || FormManager.getActualValue('reqType') == 'U') {

          var field = FormManager.getField('custClass');
          var value = FormManager.getActualValue('isicCd');

          if (value == '9500' && (field == '60' || field == '71')) {
            return new ValidationResult(null, true);
          } else if (value != '9500' && (field == '60' || field == '71')) {
            return new ValidationResult(null, false, 'ISIC value 9500 can be entered only for CMR with Classification code 60/71 (Private Person/IBM Employee).');
          } else if (value == '9500' && (field != '60' && field != '71')) {
            return new ValidationResult(null, false, 'ISIC value 9500 can be entered only for CMR with Classification code 60/71 (Private Person/IBM Employee).');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function validateCMRNumForProspect() {
  console.log(">>>> validateCMRNumForProspect");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cmrNo = FormManager.getActualValue('cmrNo');
        var _custSubGrp = FormManager.getActualValue('custSubGrp');

        var numPattern = /^[0-9]+$/;
        var cmrSubNum = '';
        if (FormManager.getActualValue('reqType') != 'C') {
          return new ValidationResult(null, true);
        }
        if (cmrNo == '') {
          return new ValidationResult(null, true);
        } else {
          cmrSubNum = cmrNo.substring(1, 6);

          // Skip validation for Prospect Request
          var ifProspect = FormManager.getActualValue('prospLegalInd');
          if (dijit.byId('prospLegalInd')) {
            ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
          }
          console.log("validateCMRNumberForLegacy ifProspect:" + ifProspect);
          if ('Y' == ifProspect) {
            if (cmrNo == '000000') {
              return new ValidationResult(null, false, 'CMR Number should be number only Except -> 000000');
            }
            if (cmrNo.length >= 1 && cmrNo.length != 6) {
              return new ValidationResult(null, false, 'CMR Number should be 6 digit long.');
            }

            // Validation for Internal Scenario
            var internalScenarios = [ 'XINTE', 'INTER' ];
            if (internalScenarios.includes(_custSubGrp)) {
              if (!cmrNo.startsWith("99")) {
                return new ValidationResult(null, false, 'Internal CMR should begin with 99.');
              }
              if (cmrNo.length > 1 && !cmrNo.match(numPattern)) {
                return new ValidationResult({
                  id : 'cmrNo',
                  type : 'text',
                  name : 'cmrNo'
                }, false, 'CMR Number should be number only.');
              }
            } else if (!internalScenarios.includes(_custSubGrp)) {
              if (cmrNo.startsWith("99")) {
                return new ValidationResult(null, false, 'CMR Starting with 99 is allowed for Internal Scenario Only.');
              }
              if (cmrNo.length > 1 && ((!cmrNo.startsWith('P') && isNaN(cmrNo.substring(0, 1))) || (!cmrSubNum.match(numPattern)))) {
                return new ValidationResult({
                  id : 'cmrNo',
                  type : 'text',
                  name : 'cmrNo'
                }, false, 'CMR Number should have numbers only.');
              }
            }
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function hideCustPhoneonSummary() {
  console.log(">>>> hideCustPhoneonSummary");
  setInterval(function() {
    if (openAddressDetails.addrType != 'ZD01') {
      cmr.hideNode('custPhone_view');
      $('label[for="custPhone_view"]').hide();
    } else {
      cmr.showNode('custPhone_view');
      $('label[for="custPhone_view"]').show();
    }
  }, 1000);

}

var _isScenarioChanged = false;
function checkScenarioChanged(fromAddress, scenario, scenarioChanged) {
  _isScenarioChanged = scenarioChanged;
}

function setVatExemptValidatorMalta() {
  console.log(">>>> setVatExemptValidatorMalta");
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custGrp == 'CROSS') {
    return;
  }
  console.log("setVatExemptValidatorMalta for Malta..");
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    if (_isScenarioChanged && custSubGrp == 'COMME') {
      FormManager.setValue('vatExempt', false);
    }
  }
}

function setValuesWRTIsuCtc(ctc) {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  console.log(">>>> setValuesWRTIsuCtc");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var isu = FormManager.getActualValue('isuCd');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (ctc == null) {
    var ctc = FormManager.getActualValue('clientTier');
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (isu == '34' && ctc == 'Q') {
    FormManager.setValue('enterprise', '985204');

  } else if (isu == '36' && ctc == 'Y') {
    FormManager.setValue('enterprise', '985205');
  } else if (isu == '32' && ctc == 'T') {
    FormManager.setValue('enterprise', '985204');
  } else if (isu == '5K' && ctc == '') {
    FormManager.setValue('enterprise', '985999');
  }

  if (role == 'REQUESTER') {
    FormManager.removeValidator('enterprise', Validators.REQUIRED);
  } else {
    FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise' ]);
  }
  lockUnlockFieldForMALTA();
}

/* End 1430539 */

// CREATCMR-4293
function setCTCValues() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  console.log(">>>> setCTCValues");
  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  // Business Partner
  var custSubGrpArray = [ 'BUSPR', 'LSBP', 'LSXBP', 'NABP', 'NAXBP', 'SZBP', 'SZXBP', 'XBP', 'ZABP', 'ZAXBP', 'INTER', 'LSINT', 'LSXIN', 'NAINT', 'NAXIN', 'SZINT', 'SZXIN', 'XINTE', 'ZAINT', 'ZAXIN' ];

  // Business Partner OR Internal
  if (custSubGrpArray.includes(custSubGrp)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '21') {
      FormManager.setValue('clientTier', '');
    }
    lockUnlockFieldForMALTA();
  }

  function lockFieldsIBMEM() {
    console.log(">>>> lockFieldsIBMEM");
    if (FormManager.getActualValue('viewOnlyPage') == 'true') {
      FormManager.readOnly('inacCd');
      FormManager.readOnly('dunsNo');
      return;
    }
    if (FormManager.getActualValue('custSubGrp') == 'IBMEM') {
      FormManager.readOnly('inacCd');
      FormManager.readOnly('dunsNo');
    } else {
      FormManager.enable('inacCd');
      FormManager.enable('dunsNo');
    }
    lockUnlockFieldForMALTA();
  }

  function iSUCTCEnterpriseCombinedCodeValidator() {
    var reqType = FormManager.getActualValue('reqType');
    if (reqType != 'C') {
      return;
    }
  }
  console.log(">>>> iSUCTCEnterpriseCombinedCodeValidatorForCYPRUS");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var enterprise = FormManager.getActualValue('enterprise');

  var isuCdSet = new Set([ '34', '36', '5K' ]);
  var isuCdSet1 = new Set([ '21', '5K' ]);
  var custSubGroupSet = new Set([ 'BUSPR', 'INTER', 'IBMEM', 'XBP' ]);

  var isuCtc = isuCd + clientTier;
  var isuCtcEnterprise = isuCtc + enterprise;

  FormManager.removeValidator('isuCd', Validators.REQUIRED);
  FormManager.removeValidator('clientTier', Validators.REQUIRED);
  FormManager.removeValidator('enterprise', Validators.REQUIRED);

  if (custSubGroup == '') {
    return new ValidationResult(null, true);
  } else if (!isuCdSet.has(isuCd) && !custSubGroupSet.has(custSubGroup)) {
    return new ValidationResult({
      id : 'isuCd',
      type : 'text',
      name : 'isuCd'
    }, false, 'ISU can only accept \'34\', \'36\', \'5K\'.');
  } else if (isuCdSet1.has(isuCd) && clientTier != '') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can only accept blank.');
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
  } else if (isuCdSet1.has(isuCtc) && enterprise != '985999' && (custSubGroup != 'INTER' || custSubGroup != 'BUSPR' || custSubGroup != 'XBP')) {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'985999\'.');
  } else if (isuCd == '34' && enterprise != '985204') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'985204\'.');
  } else if (isuCd == '36' && enterprise != '985205') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'985205\'.');
  } else {
    return new ValidationResult(null, true);
  }
}

function clientTierCodeValidator() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  console.log(">>>> clientTierCodeValidator");
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');

  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (cntry == SysLoc.CYPRUS && reqType == 'C') {
    return iSUCTCEnterpriseCombinedCodeValidator();
  }

  if (((isuCode == '21' || isuCode == '8B' || isuCode == '5K') && reqType == 'C') || (isuCode != '34' && reqType == 'U')) {
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
// CREATCMR-4293

function clientTierValidator() {
  console.log(">>>> clientTierValidator");
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

function checkCmrUpdateBeforeImport() {
  console.log(">>>> checkCmrUpdateBeforeImport");
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

function lockUnlockFieldForMALTA() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custGrpSet1 = new Set([ 'BUSPR', 'INTER', 'XBP', 'IBMEM' ]);
  var _custGrpSet2 = new Set([ 'COMME', 'GOVRN', 'THDPT', 'XCOM', 'XGOV', 'XTP' ]);

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesTeamCd');
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('ppsceid');

  } else if (_custGrpSet1.has(custSubGrp)) {
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesTeamCd');
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('ppsceid');

  } else if (custSubGrp == 'PRICU') {
    FormManager.enable('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesTeamCd');
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('ppsceid');

  } else if (_custGrpSet2.has(custSubGrp)) {
    FormManager.enable('isuCd');
    FormManager.enable('clientTier');
    FormManager.enable('enterprise');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesTeamCd');
    FormManager.readOnly('salesBusOffCd');
  }
}
// CREATCMR-788
function addressQuotationValidatorMalta() {
  console.log(">>>> addressQuotationValidatorMalta");
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Name 2' ]);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, [ 'Name 3' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Name 4' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);

}
dojo.addOnLoad(function() {
  GEOHandler.MCO2 = [ '780' ];
  console.log('adding MALTA functions...');
  // GEOHandler.enableCopyAddress(GEOHandler.MCO2, validateMCOCopy, [ 'ZD01' ]);
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
  GEOHandler.registerValidator(enterpriseValidation, [ SysLoc.MALTA ], null, true);
  GEOHandler.registerValidator(addAddressTypeValidator, [ SysLoc.MALTA ], null, true);
  // GEOHandler.registerValidator(addISICValidatorForScenario, [ SysLoc.MALTA ],
  // null, true);
  GEOHandler.registerValidator(addIsicClassificationCodeValidator, [ SysLoc.MALTA ], null, true);
  GEOHandler.registerValidator(validateCMRNumForProspect, [ SysLoc.MALTA ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(hideCustPhoneonSummary, [ SysLoc.MALTA ]);
  GEOHandler.addAfterTemplateLoad(checkScenarioChanged, [ SysLoc.MALTA ]);
  GEOHandler.addAfterConfig(addISUHandler, [ SysLoc.MALTA ]);
  GEOHandler.addAfterConfig(addCTCHandler, [ SysLoc.MALTA ]);
  GEOHandler.addAfterConfig(setClientTierValuesMT, [ SysLoc.MALTA ]);
  GEOHandler.addAfterTemplateLoad(setClientTierValuesMT, [ SysLoc.MALTA ]);
  GEOHandler.addAfterTemplateLoad(addISUHandler, [ SysLoc.MALTA ]);

  // CREATCMR-4293
  GEOHandler.addAfterTemplateLoad(setCTCValues, GEOHandler.MCO2);
  GEOHandler.registerValidator(clientTierValidator, GEOHandler.MCO2, null, true);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [ SysLoc.MALTA ], null, true);
});