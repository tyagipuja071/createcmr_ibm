/* Register MCO1 Javascripts */
var localScenarios = [ "LSLOC", "NALOC", "SZLOC", "ZALOC" ];
var crossScenarios = [ "LSCRO", "NACRO", "SZCRO", "ZACRO" ];
var _fstCntryCds = [ 'MU', 'ML', 'GQ', 'SN', 'CI', 'GA', 'CD', 'CG', 'DJ', 'GN', 'CM', 'MG', 'MR', 'TG', 'GM', 'CF', 'BJ', 'BF', 'SC', 'GW', 'NE', 'TD' ];
var _landCntryHandler = null;

function addMCO1LandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }

  if (_landCntryHandler == null && FormManager.getField('landCntry')) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      disablePOBox();
    });
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
  if (reqType == 'C' && role == 'REQUESTER') {
    FormManager.readOnly('embargoCd');
  } else {
    FormManager.enable('embargoCd');
  }
}

var _addrTypesForZA = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02' ];
var _addrTypeHandler = [];
var _isuHandler = null;
var _ctcHandler = null;
var _reqReasonHandler = null;
function addHandlersForZA() {
  for (var i = 0; i < _addrTypesForZA.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForZA[i]), 'onClick', function(value) {
        disableAddrFieldsZA();
      });
    }
  }

  if (_reqReasonHandler == null) {
    _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
      // setCreditCdField();
    });
  }

  if (_isuHandler == null) {
    if (FormManager.getActualValue('reqType') == 'C') {
      _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
        setCtcSalesRepSBO(value);
      });
    }
  }
  if (_ctcHandler == null) {
    if (FormManager.getActualValue('reqType') == 'C') {
      _ctcHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
        setCtcSalesRepSBO(FormManager.getField('isuCd'));
      });
    }
  }

}

function setCtcSalesRepSBO(value) {
  var reqType = FormManager.getActualValue('reqType');
  var countryUse = FormManager.getActualValue('countryUse');
  var clientTier = FormManager.getActualValue('clientTier');
  var sbo = FormManager.getActualValue('salesBusOffCd');
  var scenario = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var internalBUScenarios = [ 'ZAINT', 'NAINT', 'LSINT', 'SZINT', 'ZAXIN', 'NAXIN', 'LSXIN', 'SZXIN', 'LSXBP', 'LSBP', 'NAXBP', 'NABP', 'SZXBP', 'SZBP', 'ZAXBP', 'ZABP' ];
  var isuCtc = value + clientTier;
  if (reqType != 'C' || role != 'REQUESTER') {
    return;
  }

  if (scenario != null && !internalBUScenarios.includes(scenario) && isuCtc != '32S' && isuCtc != '34V' && (countryUse == '864' || countryUse == '864LS' || countryUse == '864SZ')) {
    FormManager.setValue('salesBusOffCd', '');
    FormManager.setValue('repTeamMemberNo', '');
  }
  if (scenario != null && !internalBUScenarios.includes(scenario) && isuCtc != '34V' && isuCtc != '32C' && countryUse == '864NA') {
    FormManager.setValue('salesBusOffCd', '');
    FormManager.setValue('repTeamMemberNo', '');
  }
}

function afterConfigForZA() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly == 'true') {
    return;
  }

  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var requestingLob = FormManager.getActualValue('requestingLob');
  var reqReason = FormManager.getActualValue('reqReason');
  var custType = FormManager.getActualValue('custGrp');
  var cntryRegion = FormManager.getActualValue('countryUse');
  var landCntry = ''; // default to South Africa

  if (cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  } else {
    landCntry = 'ZA';
  }

  FormManager.setValue('defaultLandedCountry', landCntry);
  FormManager.setValue('landCntry', landCntry);
  FormManager.setValue('capInd', true);
  FormManager.readOnly('capInd');

  if (custType != null && custType.includes('CRO')) {
    custType = 'CROSS';
  }

  // Postal Code - mandatory for ZA, optional for CROSS
  if (landCntry == 'ZA' && custType != 'CROSS') {
    checkAndAddValidator('postCd', Validators.REQUIRED, [ 'Postal Code' ]);
  }

  if (reqType == 'U') {
    cmr.showNode('cod');
  } else {
    cmr.hideNode('cod');
  }
  if (reqType == 'U') {
    FormManager.readOnly('commercialFinanced');
    FormManager.readOnly('codFlag');
  }

  // Control for Type of Customer FIELD
  if ((custSubGrp != 'ZAGOV' && custSubGrp != 'LSGOV' && custSubGrp != 'SZGOV' && custSubGrp != 'NAGOV' && custSubGrp != 'ZAXGO' && custSubGrp != 'LSXGO' && custSubGrp != 'SZXGO'
      && custSubGrp != 'NAXGO' && custSubGrp != 'ZAIBM' && custSubGrp != 'LSIBM' && custSubGrp != 'SZIBM' && custSubGrp != 'NAIBM' && custSubGrp != 'ZAXIB' && custSubGrp != 'LSXIB'
      && custSubGrp != 'SZXIB' && custSubGrp != 'NAXIB')
      && (reqType != 'U')) {
    FormManager.setValue('crosSubTyp', '');
  }
  if ((role == 'REQUESTER') && reqType != 'C') {
    FormManager.readOnly('crosSubTyp');
  } else {
    FormManager.enable('crosSubTyp');
  }

  lobChange();
  setCreditCdField();
  enterpriseValidation();
  clearPoBoxPhoneAddrGridItems();
}

function onLobchange() {
  dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
    lobChange();
  });
  dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
    lobChange();
  });
}
function lobChange() {
  if (FormManager.getActualValue('requestingLob') == 'IGF' && FormManager.getActualValue('reqReason') == 'COPT') {
    FormManager.enable('commercialFinanced');
  } else {
    FormManager.readOnly('commercialFinanced');
  }

  if (FormManager.getActualValue('requestingLob') == 'AR' && FormManager.getActualValue('reqReason') == 'COD') {
    FormManager.enable('codFlag');
  } else {
    FormManager.readOnly('codFlag');
  }

}

function setCofField() {
  dojo.connect(FormManager.getField('commercialFinanced'), 'onChange', function(value) {
    if (FormManager.getActualValue('codFlag') != "") {
      setCodFieldOnChange();
    }
  });

  dojo.connect(FormManager.getField('codFlag'), 'onChange', function(value) {
    if (FormManager.getActualValue('commercialFinanced') != "") {
      setCofFieldOnChange();
    }
  });
}

function setCodFieldOnChange() {
  var cof = FormManager.getActualValue('commercialFinanced');
  var cod = FormManager.getActualValue('codFlag');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  // if (role == 'REQUESTER') {
  if (cod != null && cod != "" && (cof == 'R' || cof == 'S' || cof == 'T')) {
    FormManager.setValue('codFlag', 'N');
    // }
  }
}
function setCofFieldOnChange() {
  var cof = FormManager.getActualValue('commercialFinanced');
  var cod = FormManager.getActualValue('codFlag');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  // if (role == 'REQUESTER') {
  if (cof != null && cof != "" && cod == 'Y') {
    FormManager.setValue('commercialFinanced', '');
    // }
  }
}

function setCreditCdField() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U' && FormManager.getActualValue('reqReason') == 'COD') {
    FormManager.setValue('repTeamMemberNo', 'AMSNBA');
    FormManager.setValue('salesBusOffCd', '0020');
    FormManager.setValue('collBoId', '0020');
  }
}

/**
 * sets fields to lock/mandatory, not scenario handled
 */
function lockRequireFieldsZA() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole');

  // fields locked for Requester
  if (reqType == 'U' && role == GEOHandler.ROLE_REQUESTER) {
    FormManager.readOnly('specialTaxCd');
  }
  FormManager.readOnly('cmrOwner');
  FormManager.readOnly('custPrefLang');
  FormManager.addValidator('custPrefLang', Validators.REQUIRED, [ 'Preferred Language' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('cmrOwner', Validators.REQUIRED, [ 'CMR Owner' ], 'MAIN_IBM_TAB');

}

function disableAddrFieldsZA() {
  var custType = FormManager.getActualValue('custGrp');
  var addrType = FormManager.getActualValue('addrType');

  if (custType != null && custType.includes('LOC')) {
    custType = 'LOCAL';
  }

  if (custType == 'LOCAL' && addrType == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }

  // Phone - for mailing and shipping addresses
  if (addrType == 'ZS01' || addrType == 'ZD01') {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  }

  disablePOBox();
}

function addAddressTypeValidator() {
  console.log("addAddressTypeValidator for MCO1..........");
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
            return new ValidationResult(null, false, 'Only one Mailing address is allowed.');
          } else if (zp01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Billing address is allowed.');
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
  // ZA - postal code should accept alphanumeric and spaces
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

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var att = FormManager.getActualValue('custNm4');
        if (att != null && !hasAttPersonPrefix(att) && att.length > 25) {
          return new ValidationResult(null, false, 'Total computed length of Att. Person should not exceed 25 characters.');
        } else if (att != null && hasAttPersonPrefix(att) && att.length > 30) {
          return new ValidationResult(null, false, 'Total computed length of Att. Person should not exceed 30 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // Name Con't, Address Con't can't and POXOX be filled together
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntryRegion = FormManager.getActualValue('countryUse');
        var landCntry = FormManager.getActualValue('landCntry');

        if (landCntry != 'ZA') {
          if (FormManager.getActualValue('custNm2') != '' && FormManager.getActualValue('addrTxt2') != '' && FormManager.getActualValue('poBox') != '') {
            return new ValidationResult(null, false, 'Customer Name Con\'t, Street Con\'t and POBox cannot be filled at once for cross-borders');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function hasAttPersonPrefix(attPerson) {
  var attPrefixList = [ 'Att:' ];
  var prefixFound = false;
  for (var i = 0; i < attPrefixList.length; i++) {
    if (!prefixFound) {
      if (attPerson.startsWith(attPrefixList[i])) {
        prefixFound = true;
      }
    }
  }
  return prefixFound;
}

function addCrossBorderValidatorForZA() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        // only Create type will be validated
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }

        var scenario = FormManager.getActualValue('custGrp');
        if (scenario != null && scenario.includes('CRO')) {
          scenario = 'CROSS';
        }

        var cntryRegion = FormManager.getActualValue('countryUse');
        var landCntry = 'ZA'; // default to South Africa
        if (cntryRegion != '' && cntryRegion.length > 3) {
          landCntry = cntryRegion.substring(3, 5);
        }

        var reqId = FormManager.getActualValue('reqId');
        var defaultLandCntry = landCntry;
        var result = cmr.query('VALIDATOR.CROSSBORDER', {
          REQID : reqId
        });
        if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function validateMCOCopy(addrType, arrayOfTargetTypes) {
  return null;
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

  var cntryUse = FormManager.getActualValue('countryUse');

  if (cntryUse == '864NA') {
    abbrevLocn = 'Namibia';
  } else if (cntryUse == '864LS') {
    abbrevLocn = 'Lesotho';
  } else if (cntryUse == '864SZ') {
    abbrevLocn = 'Swaziland';
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

function crossborderScenariosAbbrvLoc() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var custGroup = FormManager.getActualValue('custGrp');
  if (custGroup != null && custGroup.length > 0) {
    var cross = crossScenarios.indexOf(custGroup.toUpperCase());
    if (cross > -1) {
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
}

function scenariosAbbrvLocOnChange() {
  dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
    var custGroup = FormManager.getActualValue('custGrp');
    var loc = localScenarios.indexOf(custGroup.toUpperCase());
    var cross = crossScenarios.indexOf(custGroup.toUpperCase());
    if (loc > -1) {
      setAbbrvNmLoc();
    } else if (cross > -1) {
      crossborderScenariosAbbrvLoc();
    }
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
          var str = null;
          if (subCustGrp != null && subCustGrp.length > 0) {
            str = subCustGrp.toUpperCase();
            str = str.substring(2, str.length);
          }
          switch (str) {
          case 'SL':
            return new ValidationResult(null, true);
            break;
          case 'INT':
            return new ValidationResult(null, true);
            break;
          case 'BP':
            return new ValidationResult(null, true);
            break;
          case 'XBP':
            return new ValidationResult(null, true);
            break;
          case 'XIN':
            return new ValidationResult(null, true);
            break;
          case 'XSL':
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

        var cntryUse = FormManager.getActualValue('countryUse');

        if (cntryUse == '864NA') {
          abbrevLocn = 'Namibia';
        } else if (cntryUse == '864LS') {
          abbrevLocn = 'Lesotho';
        } else if (cntryUse == '864SZ') {
          abbrevLocn = 'Swaziland';
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

function addAddrValidatorMCO1() {
  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.LATIN, [ 'Customer Name Continuation' ]);
  FormManager.addValidator('custNm4', Validators.LATIN, [ 'Attention Person' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN, [ 'Street Continuation' ]);
  FormManager.addValidator('city1', Validators.LATIN, [ 'City' ]);
  FormManager.addValidator('abbrevNm', Validators.LATIN, [ 'Abbreviated Name' ]);
  FormManager.addValidator('abbrevLocn', Validators.LATIN, [ 'Abbreviated Location' ]);

  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
  FormManager.addValidator('poBox', Validators.DIGIT, [ 'PO Box' ]);
}

function addATTperson(cntry, addressMode, saving) {
  if (saving) {
    var dept = FormManager.getActualValue('custNm4').toUpperCase();
    if (dept == null || dept.length == 0) {
      FormManager.setValue('custNm4', 'Att: Accounts payable');
    }
  }
}

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    $('label[for="custNm1_view"]').text('Customer Name:');
    $('label[for="custNm2_view"]').text('Customer Name Con\'t:');
    $('label[for="landCntry_view"]').text('Country (Landed):');
    $('label[for="custNm4_view"]').text('Att. Person:');
    $('label[for="addrTxt_view"]').text('Street:');
    $('label[for="addrTxt2_view"]').text('Street Con\'t:');
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
    } else if (role == 'PROCESSOR') {
      FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
    }
  }
}

function onChangeSubCustGroup() {
  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    var custGroup = FormManager.getActualValue('custGrp');
    var loc = localScenarios.indexOf(custGroup.toUpperCase());
    if (loc > -1) {
      setAbbrvNmLoc();
    }
  });
}

function enterpriseValidation() {
  var role = FormManager.getActualValue('userRole');
  if (role == GEOHandler.ROLE_PROCESSOR) {
    FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise' ], 'MAIN_IBM_TAB');
  }
}

function showDeptNoForInternalsOnly() {
  var scenario = FormManager.getActualValue('custSubGrp');
  var internalScenarios = [ 'ZAINT', 'NAINT', 'LSINT', 'SZINT', 'ZAXIN', 'NAXIN', 'LSXIN', 'SZXIN' ];
  if (scenario != null && internalScenarios.includes(scenario)) {
    FormManager.show('InternalDept', 'ibmDeptCostCenter');
    FormManager.addValidator('ibmDeptCostCenter', Validators.NUMBER, [ 'Internal Department Number' ]);
    FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, [ 'Internal Department Number' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('ibmDeptCostCenter', Validators.REQUIRED);
    FormManager.clearValue('ibmDeptCostCenter');
    FormManager.hide('InternalDept', 'ibmDeptCostCenter');
  }
}

function addIbmDeptCostCntrValidator() {
  console.log("ibmDeptCostCenter..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var scenario = FormManager.getActualValue('custSubGrp');
        var internalScenarios = [ 'ZAINT', 'NAINT', 'LSINT', 'SZINT', 'ZAXIN', 'NAXIN', 'LSXIN', 'SZXIN' ];
        if (scenario != null && internalScenarios.includes(scenario)) {
          var value = FormManager.getActualValue('ibmDeptCostCenter');
          var result = false;
          if (value && value.length != 6) {
            result = true;
          }
          if (result) {
            return new ValidationResult(null, false, 'Internal Department Number should be exactly 6 digit.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addStreetContPoBoxLengthValidator() {
  console.log("addStreetContPoBoxLengthValidator..............");

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrTxt = FormManager.getActualValue('addrTxt2').trim();
        var poBox = FormManager.getActualValue('poBox');
        var combinedVal = addrTxt;
        if (combinedVal != '') {
          if (poBox != '') {
            combinedVal += poBox;
            if (combinedVal.length > 22) {
              return new ValidationResult(null, false, 'Total computed length of Street Con\'t and PO Box should be less than 22 characters.');
            }
          } else {
            if (combinedVal.length > 30) {
              return new ValidationResult(null, false, 'Street Con\'t should not exceed 30 characters.');
            }
          }
        } else {
          if (poBox.length > 10) {
            return new ValidationResult(null, false, 'Max Allowed fpr PO BOX length will be 10 characters');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addCityPostalCodeLengthValidator() {
  console.log("addCityPostalCodeLengthValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postCd = FormManager.getActualValue('postCd');
        var city = FormManager.getActualValue('city1');
        var combinedVal = postCd + city;
        if (postCd != undefined && city != undefined && postCd != '' && city != '' && combinedVal.length > 28) {
          return new ValidationResult(null, false, 'Total computed length of City and Postal Code should be less than 28 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addCrossLandedCntryFormValidator() {
  console.log("addCrossLandedCntryFormValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custGroup = FormManager.getActualValue('custGrp').toUpperCase();
        var isCrossborderScenario = crossScenarios.includes(custGroup);

        if (isCrossborderScenario && FormManager.getActualValue('addrType') == 'ZS01' && FormManager.getActualValue('landCntry') == 'ZA') {
          return new ValidationResult(null, false, 'Landed Country value should not be \'South Africa - ZA\' for Cross-border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function streetValidatorCustom() {
  console.log("streetValidatorCustom..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrPlain = FormManager.getActualValue('addrTxt').trim();

        if (addrPlain != null && addrPlain.length > 30) {
          return new ValidationResult(FormManager.getField('addrTxt'), false, 'Street value should be at most 23 CHAR long + "Street".');
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

function addValidatorStreet() {
  FormManager.removeValidator('addrTxt', Validators.MAXLENGTH);
}

function disablePOBox() {
  var landedCntry = FormManager.getActualValue('landCntry');
  var addrType = FormManager.getActualValue('addrType');
  var poBoxEnabledAddrList = [ 'ZS01', 'ZP01' ];

  if (_fstCntryCds.includes(landedCntry)) {
    poBoxEnabledAddrList.push('ZI01');
  }

  if (poBoxEnabledAddrList.includes(addrType)) {
    FormManager.enable('poBox');
  } else {
    FormManager.setValue('poBox', '');
    FormManager.disable('poBox');
  }
}

function clearPoBoxPhoneAddrGridItems() {
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    if (_allAddressData != null && _allAddressData[i] != null) {
      if (!(_allAddressData[i].addrType[0] == 'ZS01' || _allAddressData[i].addrType[0] == 'ZD01')) {
        _allAddressData[i].custPhone[0] = '';
      }

      if (!(_allAddressData[i].addrType[0] == 'ZS01' || _allAddressData[i].addrType[0] == 'ZP01')) {
        var clearPoBox = true;
        if (_allAddressData[i].addrType[0] == 'ZI01' && _fstCntryCds.includes(_allAddressData[i].landCntry[0])) {
          clearPoBox = false;
        }
        if (clearPoBox) {
          _allAddressData[i].poBox[0] = '';
        }
      }
    }
  }
}

function retainImportedValues(fromAddress, scenario, scenarioChanged) {
  var isCmrImported = getImportedIndc();

  if (FormManager.getActualValue('reqType') == 'C' && isCmrImported == 'Y' && scenarioChanged) {
    var reqId = FormManager.getActualValue('reqId');
    var subGrp = getCommonSubgrpVal(scenario);

    var isCommercial = (subGrp == 'XCO' || subGrp == 'COM');
    var isGovern = (subGrp == 'XGO' || subGrp == 'GOV');
    var isThirdParty = (subGrp == 'XTP' || subGrp == 'TP');

    if (isCommercial || isGovern || isThirdParty) {
      var origISU;
      var origClientTier;
      var origRepTeam;
      var origSbo;
      var origInac;

      var result = cmr.query("GET.CMRINFO.IMPORTED", {
        REQ_ID : reqId
      });

      if (result != null && result != '') {
        origISU = result.ret1;
        origClientTier = result.ret2;
        origRepTeam = result.ret3;
        origSbo = result.ret4;
        origInac = result.ret6;

        FormManager.setValue('isuCd', origISU);
        FormManager.setValue('clientTier', origClientTier);
        FormManager.setValue('repTeamMemberNo', origRepTeam);
        FormManager.setValue('salesBusOffCd', origSbo);
        FormManager.setValue('inacCd', origInac);
      }
    } else {
      FormManager.setValue('inacCd', '');
    }
  }
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

// common subgroup value of SA and sub-regions
function getCommonSubgrpVal(custSubGrp) {
  var val = null;
  if (custSubGrp != null && custSubGrp.length > 0) {
    val = custSubGrp.toUpperCase();
    val = val.substring(2, val.length);
  }
  return val;
}

function validateCMRForGMLLCScenario() {
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

        if (reqType == 'C' && requestCMR != '' && cmrNo && (subCustGrp == 'NAELC' || subCustGrp == 'SZELC' || subCustGrp == 'LSELC')) {

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
          if (exists && exists.ret1 && action != 'PCM') {
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
            if (exists && exists.ret1) {
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

function enableCMRNOSAGLLC() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var subCustGrp = FormManager.getActualValue('custSubGrp');
  if (role == 'REQUESTER' && (subCustGrp == 'NAELC' || subCustGrp == 'SZELC' || subCustGrp == 'LSELC')) {
    FormManager.enable('cmrNo');
    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');
  } else if (role == 'REQUESTER' && (subCustGrp != 'NAELC' || subCustGrp != 'SZELC' || subCustGrp != 'LSELC')) {
    FormManager.readOnly('cmrNo');
    FormManager.resetValidations('cmrNo');
  }
}

function enableCmrForProcessor() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'C') {
    return;
  }
  if (role == "PROCESSOR") {
    FormManager.enable('cmrNo');
  }
}

function mandatoryForBusinessPartner() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    var _custType = FormManager.getActualValue('custSubGrp');
    if (_custType == 'LSBP' || _custType == 'SZBP' || _custType == 'ZABP' || _custType == 'NABP' || _custType == 'ZAXBP' || _custType == 'NAXBP' || _custType == 'LSXBP' || _custType == 'SZXBP'
        || _custType == 'LSBLC' || _custType == 'SZBLC' || _custType == 'NABLC') {
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.resetValidations('ppsceid');
    }
  }
}

function validateTypeOfCustomer() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'U') {
    return;
  }
  FormManager.addValidator('crosSubTyp', Validators.NO_SPECIAL_CHAR, [ 'Type Of Customer' ], 'MAIN_CUST_TAB');
}

function embargoCdValidator() {
  console.log("embargoCdValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd');
        var validValues = [ 'Y', 'J', '' ];

        if (!validValues.includes(embargoCd)) {
          return new ValidationResult(null, false, 'Embargo Code can have either Y or J or empty as values');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

/* End 1430539 */
dojo.addOnLoad(function() {
  GEOHandler.MCO1 = [ SysLoc.SOUTH_AFRICA ];
  console.log('adding MCO1 functions...');
  GEOHandler.addAddrFunction(addMCO1LandedCountryHandler, GEOHandler.MCO1);
  GEOHandler.enableCopyAddress(GEOHandler.MCO1, validateMCOCopy, [ 'ZD01' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.MCO1);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.MCO1);
  GEOHandler.setRevertIsicBehavior(false);

  GEOHandler.addAfterConfig(afterConfigForZA, GEOHandler.MCO1);
  GEOHandler.addAfterConfig(lockRequireFieldsZA, GEOHandler.MCO1);
  GEOHandler.addAfterTemplateLoad(lockRequireFieldsZA, GEOHandler.MCO1);
  GEOHandler.addAfterConfig(addHandlersForZA, GEOHandler.MCO1);
  GEOHandler.addAfterConfig(setAbbrvNmLoc, GEOHandler.MCO1);
  GEOHandler.addAfterConfig(crossborderScenariosAbbrvLoc, GEOHandler.MCO1);
  GEOHandler.addAfterConfig(scenariosAbbrvLocOnChange, GEOHandler.MCO1);
  GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.MCO1);
  GEOHandler.addAfterConfig(lockAbbrv, GEOHandler.MCO1);
  GEOHandler.addAfterConfig(showDeptNoForInternalsOnly, GEOHandler.MCO1);
  GEOHandler.addAfterTemplateLoad(showDeptNoForInternalsOnly, GEOHandler.MCO1);
  GEOHandler.addAfterTemplateLoad(onChangeSubCustGroup, GEOHandler.MCO1);
  GEOHandler.addAfterTemplateLoad(addValidatorStreet, GEOHandler.MCO1);
  GEOHandler.addAfterConfig(addValidatorStreet, GEOHandler.MCO1);

  GEOHandler.registerValidator(addAddressTypeValidator, GEOHandler.MCO1, null, true);
  GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.MCO1, null, true);
  GEOHandler.registerValidator(addCrossBorderValidatorForZA, GEOHandler.MCO1, null, true);
  GEOHandler.registerValidator(addAttachmentValidator, GEOHandler.MCO1, null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.SOUTH_AFRICA, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.SOUTH_AFRICA ], null, true);
  GEOHandler.registerValidator(requireVATForCrossBorder, GEOHandler.MCO1, null, true);
  GEOHandler.registerValidator(streetValidatorCustom, GEOHandler.MCO1, null, true);

  GEOHandler.addAddrFunction(addAddrValidatorMCO1, GEOHandler.MCO1);
  GEOHandler.addAddrFunction(changeAbbrevNmLocn, GEOHandler.MCO1);
  GEOHandler.addAddrFunction(addATTperson, GEOHandler.MCO1);
  GEOHandler.addAddrFunction(disableAddrFieldsZA, GEOHandler.MCO1);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.MCO1, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.MCO1);

  GEOHandler.registerValidator(addStreetContPoBoxLengthValidator, GEOHandler.MCO1, null, true);
  GEOHandler.registerValidator(addCityPostalCodeLengthValidator, GEOHandler.MCO1, null, true);
  GEOHandler.registerValidator(addCrossLandedCntryFormValidator, GEOHandler.MCO1, null, true);
  GEOHandler.registerValidator(addIbmDeptCostCntrValidator, GEOHandler.MCO1, null, true);
  GEOHandler.addAfterTemplateLoad(retainImportedValues, GEOHandler.MCO1);

  GEOHandler.addAfterConfig(onLobchange, GEOHandler.MCO1);
  GEOHandler.addAfterTemplateLoad(onLobchange, GEOHandler.MCO1);
  GEOHandler.addAfterConfig(setCofField, GEOHandler.MCO1);
  GEOHandler.addAfterTemplateLoad(setCofField, GEOHandler.MCO1);
  GEOHandler.registerValidator(validateCMRForGMLLCScenario, [ SysLoc.SOUTH_AFRICA ], null, true);
  GEOHandler.addAfterConfig(enableCMRNOSAGLLC, SysLoc.SOUTH_AFRICA);
  GEOHandler.addAfterTemplateLoad(enableCMRNOSAGLLC, SysLoc.SOUTH_AFRICA);
  GEOHandler.addAfterConfig(enableCmrForProcessor, [ SysLoc.SOUTH_AFRICA ]);
  GEOHandler.addAfterConfig(mandatoryForBusinessPartner, [ SysLoc.SOUTH_AFRICA ]);
  GEOHandler.addAfterTemplateLoad(mandatoryForBusinessPartner, [ SysLoc.SOUTH_AFRICA ]);
  GEOHandler.addAfterConfig(validateTypeOfCustomer, GEOHandler.MCO1);

  GEOHandler.registerValidator(embargoCdValidator, [ SysLoc.SOUTH_AFRICA ], null, true);
});
