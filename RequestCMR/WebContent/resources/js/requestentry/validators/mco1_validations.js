/* Register MCO1 Javascripts */
var localScenarios = ["LSLOC", "NALOC", "SZLOC", "ZALOC"];
var crossScenarios = ["LSCRO", "NACRO", "SZCRO", "ZACRO"];
var _fstCntryCds = ['MU', 'ML', 'GQ', 'SN', 'CI', 'GA', 'CD', 'CG', 'DJ', 'GN', 'CM', 'MG', 'MR', 'TG', 'GM', 'CF', 'BJ', 'BF', 'SC', 'GW', 'NE', 'TD'];
var _landCntryHandler = null;
var scenarioToSkipCMRValidationZA = ['LSELC', 'SZELC', 'NAELC', 'LSLLC', 'NALLC', 'SZLLC', 'LSBLC', 'SZBLC', 'NABLC'];

function addMCO1LandedCountryHandler(cntry, addressMode, saving, finalSave) {
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      if (cntryRegion != '' && cntryRegion == '864') {
        FormManager.setValue('landCntry', 'ZA');
      } else if (cntryRegion != '' && cntryRegion == '864LS') {
        FormManager.setValue('landCntry', 'LS');
      } else if (cntryRegion != '' && cntryRegion == '864NA') {
        FormManager.setValue('landCntry', 'NA');
      } else if (cntryRegion != '' && cntryRegion == '864SZ') {
        FormManager.setValue('landCntry', 'SZ');
      }
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }

  if (_landCntryHandler == null && FormManager.getField('landCntry')) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      disablePOBox();
// postalCodeRequired();
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

// CREATCMR-2645 lock SORTL field for update action for REQUESTER
function lockSORTL() {
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var reqType = FormManager.getActualValue('reqType');
    var clientTier = FormManager.getActualValue('clientTier');
    if (reqType == 'U' && clientTier == 'Y') {
        if (role == 'REQUESTER') {
            FormManager.readOnly('salesBusOffCd');
        } else if (role == 'PROCESSOR') {
            FormManager.enable('salesBusOffCd');
        }
    }
}

var _addrTypesForZA = ['ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02'];
var _addrTypeHandler = [];
var _isuHandler = null;
var _ctcHandler = null;
var _reqReasonHandler = null;
var _vatExemptHandler = null;
var calledByIsuHandler = false;
var calledByCtcHandler = false;
var scenarioChangeSalesRepSBO = false;
var _subIndustryCdHandler = null;
var _oldIsuCd = null;
var _oldCtc = null;
var _oldSubInd = null;
var _oldEnt = null;

function addHandlersForZA() {
    _oldIsuCd = FormManager.getActualValue('isuCd');
    _oldCtc = FormManager.getActualValue('clientTier');
    _oldSubInd = FormManager.getActualValue('subIndustryCd');

    for (var i = 0; i < _addrTypesForZA.length; i++) {
        _addrTypeHandler[i] = null;
        if (_addrTypeHandler[i] == null) {
            _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForZA[i]), 'onClick', function(value) {
                disableAddrFieldsZA();
            });
        }
    }

    if (_subIndustryCdHandler == null) {
        _subIndustryCdHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
            if (_oldSubInd != FormManager.getActualValue('subIndustryCd') || typeof(_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != FormManager.getActualValue('custSubGrp')) {
                setDefaultEnterpriseBasedOnSubInd(value);
                _oldSubInd = FormManager.getActualValue('subIndustryCd');
            }
        });
    }

    if (_isuHandler == null) {
        _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
            if (FormManager.getActualValue('reqType') == 'C') {
                calledByIsuHandler = true;
                calledByCtcHandler = false;
                setCtcSalesRepSBO(value);
                setEnterpriseBehaviour();
            }
            if (_oldIsuCd != FormManager.getActualValue('isuCd') || typeof(_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != FormManager.getActualValue('custSubGrp')) {
                setDefaultEnterpriseBasedOnSubInd(value);
                _oldIsuCd = FormManager.getActualValue('isuCd');
            }
            setClientTierValues(value);
        });
    }
    if (_ctcHandler == null) {
        _ctcHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
            if (FormManager.getActualValue('reqType') == 'C') {
                calledByIsuHandler = false;
                calledByCtcHandler = true;
                setCtcSalesRepSBO(FormManager.getField('isuCd'));
                setSalesRepSORTL();
                setEnterpriseBehaviour();
            } else if (FormManager.getActualValue('reqType') == 'U') {
                calledByIsuHandler = false;
                calledByCtcHandler = true;
                var clientTier = FormManager.getActualValue('clientTier');
                if (clientTier == 'Y') {
                    lockSORTL();
                } else {
                    FormManager.enable('salesBusOffCd');
                }
            }
            if (_oldCtc != FormManager.getActualValue('clientTier') || typeof(_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != FormManager.getActualValue('custSubGrp')) {
                setDefaultEnterpriseBasedOnSubInd(value);
                _oldCtc = FormManager.getActualValue('clientTier');
            }
        });
    }

    if (FormManager.getActualValue('reqType') == 'C') {
        if (_vatExemptHandler == null) {
            _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
                resetVatRequired(true);
            });
        }
    }
}

function postalCodeRequired() {
    var reqType = FormManager.getActualValue('reqType');
    if (reqType != 'C') {
        return;
    }
    var landCntry = FormManager.getActualValue('landCntry');
    // Postal Code - mandatory for ZA, optional for CROSS
    if (landCntry == 'ZA') {
        FormManager.addValidator('postCd', Validators.REQUIRED, ['Postal Code']);
    } else {
        FormManager.removeValidator('postCd', Validators.REQUIRED);
    }
}

function setClientTierValues(isuCd) {
    if (FormManager.getActualValue('viewOnlyPage') == 'true') {
        return;
    }
    var reqType = FormManager.getActualValue('reqType');
    isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '5K' && reqType == 'C') {
        FormManager.removeValidator('clientTier', Validators.REQUIRED);
        FormManager.setValue('salesBusOffCd', '0000');
        FormManager.setValue('repTeamMemberNo', 'SALES0');
    }
    if (isuCd == '27') {
        FormManager.setValue('clientTier', 'E');
    } else if (isuCd == '34') {
        FormManager.setValue('clientTier', 'Q');
    } else if (isuCd == '36') {
        FormManager.setValue('clientTier', 'Y');
    } else {
        FormManager.setValue('clientTier', '');
    }
}

function setCtcSalesRepSBO(value) {
    var reqType = FormManager.getActualValue('reqType');
    var countryUse = FormManager.getActualValue('countryUse');
    var clientTier = FormManager.getActualValue('clientTier');
    var sbo = FormManager.getActualValue('salesBusOffCd');
    var scenario = FormManager.getActualValue('custSubGrp');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var internalBUScenarios = ['ZAINT', 'NAINT', 'LSINT', 'SZINT', 'ZAXIN', 'NAXIN', 'LSXIN', 'SZXIN', 'LSXBP', 'LSBP', 'NAXBP', 'NABP', 'SZXBP', 'SZBP', 'ZAXBP', 'ZABP', 'SZBLC', 'LSBLC', 'NABLC'];
    var isuCtc = value + clientTier;
    if (reqType != 'C') {
        return;
    }
    if (scenario && scenario != null && !internalBUScenarios.includes(scenario) && isuCtc != '34Q' && isuCtc != '34V' && isuCtc != '5K' && (countryUse == '864' || countryUse == '864LS' || countryUse == '864SZ' || countryUse == '864NA')) {
        if (calledByIsuHandler) {
            if (!scenarioChangeSalesRepSBO) {
                FormManager.setValue('salesBusOffCd', '');
                FormManager.setValue('repTeamMemberNo', '');
                calledByIsuHandler = false;
            }
        } else if (calledByCtcHandler) {
            if (!scenarioChangeSalesRepSBO) {
                FormManager.setValue('salesBusOffCd', '');
                FormManager.setValue('repTeamMemberNo', '');
                calledByCtcHandler = false;
            }
            scenarioChangeSalesRepSBO = false;
        }
    }
    if (isuCtc == '34Y' && (countryUse == '864' || countryUse == '864LS' || countryUse == '864SZ' || countryUse == '864NA')) {
        if (calledByIsuHandler) {
            if (!scenarioChangeSalesRepSBO) {
                FormManager.setValue('salesBusOffCd', '');
                FormManager.setValue('repTeamMemberNo', '');
                calledByIsuHandler = false;
            }
        } else if (calledByCtcHandler) {
            if (!scenarioChangeSalesRepSBO) {
                FormManager.setValue('salesBusOffCd', '');
                FormManager.setValue('repTeamMemberNo', '');
                calledByCtcHandler = false;
            }
            scenarioChangeSalesRepSBO = false;
        }
    }
}

// CREATCMR-2645
function setSalesRepSORTL() {
    var clientTier = FormManager.getActualValue('clientTier');
    var reqType = FormManager.getActualValue('reqType');
    var isu = FormManager.getActualValue('isuCd');
    var cntryRegion = FormManager.getActualValue('countryUse');
    var isuCtc = isu.concat(clientTier)
    if (isuCtc == '34Q' || isuCtc == '36Y' || isuCtc == '27E' && reqType == 'C') {
        if (cntryRegion == '864') {
            FormManager.setValue('repTeamMemberNo', 'SALES9');
            FormManager.setValue('salesBusOffCd', '4961');
        } else if (cntryRegion == '864NA') {
            FormManager.setValue('repTeamMemberNo', 'SALES4');
            FormManager.setValue('salesBusOffCd', '4411');
        } else if (cntryRegion == '864LS') {
            FormManager.setValue('repTeamMemberNo', 'SALES5');
            FormManager.setValue('salesBusOffCd', '4511');
        } else if (cntryRegion == '864SZ') {
            FormManager.setValue('repTeamMemberNo', 'SALES6');
            FormManager.setValue('salesBusOffCd', '4611');
        }
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
    if ((custSubGrp != 'ZAGOV' && custSubGrp != 'LSGOV' && custSubGrp != 'SZGOV' && custSubGrp != 'NAGOV' && custSubGrp != 'ZAXGO' && custSubGrp != 'LSXGO' && custSubGrp != 'SZXGO' &&
            custSubGrp != 'NAXGO' && custSubGrp != 'ZAIBM' && custSubGrp != 'LSIBM' && custSubGrp != 'SZIBM' && custSubGrp != 'NAIBM' && custSubGrp != 'ZAXIB' && custSubGrp != 'LSXIB' &&
            custSubGrp != 'SZXIB' && custSubGrp != 'NAXIB') &&
        (reqType != 'U')) {
        FormManager.setValue('crosSubTyp', '');
    }
    if ((role == 'REQUESTER') && reqType != 'C') {
        FormManager.readOnly('crosSubTyp');
    } else {
        FormManager.enable('crosSubTyp');
    }

    lobChange();
    // enterpriseValidation();
    clearPoBoxPhoneAddrGridItems();
    showDeptNoForInternalsOnly();
    // CREATCMR-788
    addressQuotationValidatorZA();
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
    var viewOnly = FormManager.getActualValue('viewOnlyPage');
    if (viewOnly != '' && viewOnly == 'true') {
        return;
    }
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
        setCodFieldOnChange();
    });

    dojo.connect(FormManager.getField('codFlag'), 'onChange', function(value) {
        setCofFieldOnChange();
    });
}

function setCodFieldOnChange() {
    var cof = FormManager.getActualValue('commercialFinanced');
    var cod = FormManager.getActualValue('codFlag');
    var reqId = FormManager.getActualValue('reqId');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    // if (role == 'REQUESTER') {
    if (cof != undefined && (cof == 'R' || cof == 'S' || cof == 'T')) {
        FormManager.setValue('codFlag', 'N');
        // }
    } else if (cof != undefined && cof == '') {
        var result = cmr.query('LD.OLD_COD_COF_BY_REQID_SA', {
            REQ_ID: reqId
        });

        if (result) {
            var oldCOD = result.ret1;
            var oldCOF = result.ret2;
            if (cof == oldCOF) {
                FormManager.setValue('codFlag', oldCOD);
            }
        }

    }
}

function setCofFieldOnChange() {
    var cof = FormManager.getActualValue('commercialFinanced');
    var cod = FormManager.getActualValue('codFlag');
    var reqId = FormManager.getActualValue('reqId');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    // if (role == 'REQUESTER') {
    if (cod != undefined && cod == 'Y') {
        FormManager.setValue('commercialFinanced', '');
        // }
    } else if (cod != undefined && (cod == '' || cod == 'N')) {
        var result = cmr.query('LD.OLD_COD_COF_BY_REQID_SA', {
            REQ_ID: reqId
        });
        if (result) {
            var oldCOD = result.ret1;
            var oldCOF = result.ret2;
            if (cod == oldCOD) {
                FormManager.setValue('commercialFinanced', oldCOF);
            }
        }
    }

}

/**
 * sets fields to lock/mandatory, not scenario handled
 */
function lockRequireFieldsZA() {
    var reqType = FormManager.getActualValue('reqType');
    var role = FormManager.getActualValue('userRole');

    // fields locked for Requester
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('custPrefLang');
    FormManager.addValidator('custPrefLang', Validators.REQUIRED, ['Preferred Language'], 'MAIN_IBM_TAB');
    FormManager.addValidator('cmrOwner', Validators.REQUIRED, ['CMR Owner'], 'MAIN_IBM_TAB');
    if (reqType == 'U') {
        FormManager.removeValidator('custPrefLang', Validators.REQUIRED);
        if (role == GEOHandler.ROLE_REQUESTER) {
            FormManager.readOnly('specialTaxCd');
        }
    }
}

function disableAddrFieldsZA() {
    var custType = FormManager.getActualValue('custGrp');
    var addrType = FormManager.getActualValue('addrType');
    var reqType = FormManager.getActualValue('reqType');

    if (custType != null && custType.includes('LOC')) {
        custType = 'LOCAL';
    }

    if ((custType == 'LOCAL' || reqType == 'U') && addrType == 'ZS01') {
        FormManager.readOnly('landCntry');
    } else {
        FormManager.enable('landCntry');
    }

    // Phone - for mailing and shipping addresses
    if (addrType != 'ZS01' && addrType != 'ZD01') {
        FormManager.readOnly('custPhone');
        FormManager.setValue('custPhone', '');
    } else {
        FormManager.enable('custPhone');
    }

    disablePOBox();
}

function addAddressTypeValidator() {
    console.log("addAddressTypeValidator for MCO1..........");
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
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
                        if (typeof(type) == 'object') {
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
            validate: function() {
                var postCd = FormManager.getActualValue('postCd');
                var landCntry = FormManager.getActualValue('landCntry');

                if (postCd && postCd.length > 0 && !postCd.match("^[a-zA-Z0-9 ]*$") && (landCntry == 'ZA' || landCntry == 'SZ' || landCntry == 'LS' || landCntry == 'NA')) {
                    return new ValidationResult(null, false, postCd + ' is not a valid value for Postal Code. Only alphabets, numbers, and spaces combination is valid.');
                }
                return new ValidationResult(null, true);
            }
        };
    })(), null, 'frmCMR_addressModal');

    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                var att = FormManager.getActualValue('custNm4');
                if (att != null && att != '' && !hasAttPersonPrefix(att.toUpperCase()) && att.length > 25) {
                    return new ValidationResult(null, false, 'Total computed length of Att. Person should not exceed 25 characters.');
                } else if (att != null && att != '' && hasAttPersonPrefix(att.toUpperCase()) && att.length > 30) {
                    return new ValidationResult(null, false, 'Total computed length of Att. Person should not exceed 30 characters.');
                }
                return new ValidationResult(null, true);
            }
        };
    })(), null, 'frmCMR_addressModal');

    // Name Con't, Address Con't can't and POXOX be filled together
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                var cntryRegion = FormManager.getActualValue('countryUse');
                var landCntry = FormManager.getActualValue('landCntry');

                if (landCntry != 'ZA') {
                    if ((FormManager.getActualValue('custNm2') != '' && FormManager.getActualValue('addrTxt2') != '' && FormManager.getActualValue('poBox') != '') ||
                        ((FormManager.getActualValue('custNm2') != '' && FormManager.getActualValue('addrTxt2') != '') ||
                            (FormManager.getActualValue('custNm2') != '' && FormManager.getActualValue('poBox') != ''))) {
                        return new ValidationResult(null, false, 'Please fill-out either Customer Name Con\'t or Street Con\'t and/or POBox only for cross-borders.');
                    }
                }
                return new ValidationResult(null, true);
            }
        };
    })(), null, 'frmCMR_addressModal');

}

function hasAttPersonPrefix(attPerson) {
    var attPrefixList = ['Att:', 'att:', 'ATT:'];
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
            validate: function() {
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
                    REQID: reqId
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
            REQ_ID: reqId,
        };
    }
    var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
    var city;
    var abbrevLocn = null;
    var abbrvNm = custNm.ret1;

    var cntryUse = FormManager.getActualValue('countryUse');

    if (cntryUse == '864NA' || cntryUse == '864LS' || cntryUse == '864SZ') {
        abbrevLocn = getLandedCountryDesc(reqId)
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
                    REQ_ID: reqId,
                    ADDR_TYPE: "ZS01",
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
            validate: function() {
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
                        case 'PC':
                            return new ValidationResult(null, true);
                            break;
                        case 'XPC':
                            return new ValidationResult(null, true);
                            break;
                        default:
                            var reqId = FormManager.getActualValue('reqId');
                            if (reqId != null) {
                                reqParam = {
                                    REQ_ID: reqId
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
                                        REQ_ID: reqId
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
                                        REQ_ID: reqId
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
            var reqId = FormManager.getActualValue('reqId');
            if (FormManager.getActualValue('reqType') != 'C') {
                return;
            }
            if (role == 'REQUESTER') {
                if (abbrevNm && abbrevNm.length > 22) {
                    abbrevNm = abbrevNm.substring(0, 22);
                }

                var cntryUse = FormManager.getActualValue('countryUse');

                if (cntryUse == '864NA' || cntryUse == '864LS' || cntryUse == '864SZ') {
                    abbrevLocn = getLandedCountryDesc(reqId)
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
    FormManager.addValidator('custNm1', Validators.LATIN, ['Customer Name']);
    FormManager.addValidator('custNm2', Validators.LATIN, ['Customer Name Continuation']);
    FormManager.addValidator('custNm4', Validators.LATIN, ['Attention Person']);
    FormManager.addValidator('addrTxt2', Validators.LATIN, ['Street Continuation']);
    FormManager.addValidator('city1', Validators.LATIN, ['City']);
    FormManager.addValidator('abbrevNm', Validators.LATIN, ['Abbreviated Name']);
    FormManager.addValidator('abbrevLocn', Validators.LATIN, ['Abbreviated Location']);

    FormManager.addValidator('custPhone', Validators.DIGIT, ['Phone #']);
    FormManager.addValidator('poBox', Validators.DIGIT, ['PO Box']);
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
    var reqType = FormManager.getActualValue('reqType');

    if (viewOnlyPage == 'true') {
        FormManager.readOnly('abbrevLocn');
        FormManager.readOnly('abbrevNm');
    } else {
        if (role == 'REQUESTER') {
            FormManager.readOnly('abbrevLocn');
            FormManager.readOnly('abbrevNm');
        } else if (role == 'PROCESSOR' && reqType != 'U') {
            FormManager.addValidator('abbrevNm', Validators.REQUIRED, ['Abbreviated Name'], 'MAIN_CUST_TAB');
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

/*
 * function enterpriseValidation() { var role =
 * FormManager.getActualValue('userRole'); if (role ==
 * GEOHandler.ROLE_PROCESSOR) { FormManager.addValidator('enterprise',
 * Validators.REQUIRED, [ 'Enterprise' ], 'MAIN_IBM_TAB'); } }
 */

function showDeptNoForInternalsOnly() {
    var scenario = FormManager.getActualValue('custSubGrp');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var requestCMR = FormManager.getActualValue('cmrNo');
    var reqType = FormManager.getActualValue('reqType');
    var flag = (requestCMR.match("[0-8][0-8].{4}") || requestCMR.match("[9][0-8].{4}") || requestCMR.match("[0-8][9].{4}"));

    if (reqType == 'U' && role == 'REQUESTER' && flag) {
        FormManager.readOnly('ibmDeptCostCenter');
    } else {
        FormManager.enable('ibmDeptCostCenter');
    }

    if (scenario != null && scenario.includes('IN')) {
        FormManager.show('InternalDept', 'ibmDeptCostCenter');
        FormManager.addValidator('ibmDeptCostCenter', Validators.NUMBER, ['Internal Department Number']);
        FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, ['Internal Department Number'], 'MAIN_IBM_TAB');
    } else if (scenario != null && scenario != '' && !scenario.includes('IN')) {
        FormManager.removeValidator('ibmDeptCostCenter', Validators.REQUIRED);
        FormManager.clearValue('ibmDeptCostCenter');
        FormManager.hide('InternalDept', 'ibmDeptCostCenter');
    }
}

function addIbmDeptCostCntrValidator() {
    console.log("ibmDeptCostCenter..............");
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                var scenario = FormManager.getActualValue('custSubGrp');
                var internalScenarios = ['ZAINT', 'NAINT', 'LSINT', 'SZINT', 'ZAXIN', 'NAXIN', 'LSXIN', 'SZXIN'];
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
            validate: function() {
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
                        return new ValidationResult(null, false, 'Max Allowed for PO BOX length is 10 characters');
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
            validate: function() {
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
            validate: function() {
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
            validate: function() {
                var addrPlain = FormManager.getActualValue('addrTxt').trim();

                if (addrPlain != null && addrPlain.length > 30) {
                    return new ValidationResult(FormManager.getField('addrTxt'), false, 'Street value should be at most 30 CHAR long.');
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
    var poBoxEnabledAddrList = ['ZS01', 'ZP01'];

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
                REQ_ID: reqId
            });

            if (result != null && result != '') {
                origISU = result.ret1;
                origClientTier = result.ret2;
                origRepTeam = result.ret3;
                origSbo = result.ret4;
                origInac = result.ret6;

                var savedSubGrp = getCommonSubgrpVal(_pagemodel.custSubGrp);

                var isComm = (savedSubGrp == 'XCO' || savedSubGrp == 'COM');
                var isGov = (savedSubGrp == 'XGO' || savedSubGrp == 'GOV');
                var isTPD = (savedSubGrp == 'XTP' || savedSubGrp == 'TP');
                if (scenario != _pagemodel.custSubGrp && (isComm || isGov || isTPD)) {
                    FormManager.setValue('isuCd', origISU);
                    FormManager.setValue('clientTier', origClientTier);
                    FormManager.setValue('repTeamMemberNo', origRepTeam);
                    FormManager.setValue('salesBusOffCd', origSbo);
                    FormManager.setValue('inacCd', origInac);
                } else {
                    FormManager.setValue('isuCd', origISU);
                    FormManager.setValue('clientTier', origClientTier);
                    FormManager.setValue('repTeamMemberNo', origRepTeam);
                    FormManager.setValue('salesBusOffCd', origSbo);
                    FormManager.setValue('inacCd', _pagemodel.inacCd);
                }
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
        REQID: FormManager.getActualValue('reqId')
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

function validateCMRForExistingGMLLCScenario() {
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                console.log('checking requested cmr number...');
                var requestCMR = FormManager.getActualValue('cmrNo');
                var reqType = FormManager.getActualValue('reqType');
                var cntry = FormManager.getActualValue('cmrIssuingCntry');
                var action = FormManager.getActualValue('yourAction');
                var requestID = FormManager.getActualValue('reqId');
                var landed = 'LANDED COUNTRY';
                var subCustGrp = FormManager.getActualValue('custSubGrp');
                var targetCntry = 'Kenya';
                var targetCntryCd = '764';
                var activeCMRExistLanded = false;
                var activeCMRExistKenya = false;
                var processingStatus = FormManager.getActualValue('rdcProcessingStatus');

                if (reqType == 'C' && requestCMR != '' && cmrNo && (subCustGrp == 'NAELC' || subCustGrp == 'SZELC' || subCustGrp == 'LSELC')) {

                    if (requestCMR.length < 6) {
                        return new ValidationResult({
                            id: 'cmrNo',
                            type: 'text',
                            name: 'cmrNo'
                        }, false, 'CMR: ' + requestCMR + ' is invalid. Please enter valid CMR Number');
                    }

                    if (requestCMR.startsWith('P')) {
                        return new ValidationResult({
                            id: 'cmrNo',
                            type: 'text',
                            name: 'cmrNo'
                        }, false, 'CMR: ' + requestCMR + ' is Prospect CMR#. Only legal CMR Number is allowed for GM LLC for already existing customer sub-scenario.');
                    }

                    var res = cmr.query('GET_LAND_CNTRY_ZS01', {
                        REQ_ID: requestID
                    });

                    if (res && res.ret1) {
                        landed = res.ret1;
                    }

                    var exist3 = cmr.query('LD.CHECK_EXISTING_CMR_NO_SA', {
                        COUNTRY: cntry,
                        CMR_NO: requestCMR,
                        MANDT: cmr.MANDT
                    });

                    if (exist3 && exist3.ret1 && exist3.ret2 == 'A' && action != 'PCM' && (processingStatus == '' || processingStatus == undefined)) {
                        activeCMRExistLanded = true;
                        return new ValidationResult({
                            id: 'cmrNo',
                            type: 'text',
                            name: 'cmrNo'
                        }, false, 'CMR: ' + requestCMR + ' is already in use in ' + cntry + '. Please use GM LLC sub-scenario in ' + landed + ' to create new CMR under both ' + targetCntry + ' and ' + landed);
                    }
                    exist4 = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC_SA', {
                        COUNTRY: cntry,
                        CMR_NO: requestCMR,
                        MANDT: cmr.MANDT
                    });

                    if (exist4 && exist4.ret1 && exist4.ret2 != 'X' && exist4.ret3 != '93' && action != 'PCM') {
                        activeCMRExistLanded = true;
                        return new ValidationResult({
                            id: 'cmrNo',
                            type: 'text',
                            name: 'cmrNo'
                        }, false, 'CMR: ' + requestCMR + ' is already in use in ' + cntry + '. Please use GM LLC sub-scenario in ' + landed + ' to create new CMR under both Kenya and ' + landed);
                    }

                    var exist1 = cmr.query('LD.CHECK_EXISTING_CMR_NO_SA', {
                        COUNTRY: targetCntryCd,
                        CMR_NO: requestCMR,
                        MANDT: cmr.MANDT
                    });

                    if (exist1.ret1 && exist1.ret2 == 'A' && exist3.ret1 && exist3.ret2 == 'C' && action != 'PCM') {
                        return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'Please note CMR ' + requestCMR + ' in ' + landed + ' is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' + landed +
                            ' and Kenya using GM LLC scenario under ' + landed);
                    }

                    if (exist1.ret1 == undefined && exist3.ret1 == undefined && action != 'PCM' && (processingStatus == '' || processingStatus == undefined)) {
                        return new ValidationResult({
                            id: 'cmrNo',
                            type: 'text',
                            name: 'cmrNo'
                        }, false, 'CMR: ' + requestCMR + ' does not exist in either ' + landed + ' or Kenya. Please use GM LLC under landed. Processors are able to enter specific CMR if needed.');
                    }
                    exist2 = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC_SA', {
                        COUNTRY: targetCntryCd,
                        CMR_NO: requestCMR,
                        MANDT: cmr.MANDT
                    });

                    if (exist2.ret3 != undefined && exist2.ret2 == 'X' && exist2.ret3 == '93' && exist4.ret3 != undefined && exist4.ret2 != 'X' && exist4.ret3 != '93' && action != 'PCM') {
                        return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'Please note CMR ' + requestCMR + ' in ' + landed + ' is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' + landed +
                            ' and Kenya using GM LLC scenario under ' + landed);
                    }

                    if (exist2.ret1 == undefined && exist4.ret1 == undefined && action != 'PCM') {
                        return new ValidationResult({
                            id: 'cmrNo',
                            type: 'text',
                            name: 'cmrNo'
                        }, false, 'CMR: ' + requestCMR + ' does not exist in either ' + landed + ' or Kenya. Please use GM LLC under landed. Processors are able to enter specific CMR if needed.');
                    }

                    if (exist1 && exist1.ret2 == 'C' && exist3.ret1 == undefined && action != 'PCM') {
                        return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'Please note CMR ' + requestCMR + ' in Kenya is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' + landed +
                            ' and Kenya using GM LLC scenario under ' + landed);
                    }

                    if (exist2 && exist2.ret2 == 'X' && exist2.ret3 == '93' && exist4.ret1 == undefined && action != 'PCM') {
                        return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'Please note CMR ' + requestCMR + ' in Kenya is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' + landed +
                            ' and Kenya using GM LLC scenario under ' + landed);
                    }

                    if (exist1 && exist1.ret2 == undefined && !activeCMRExistLanded && action != 'PCM') {
                        return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'Please note CMR ' + requestCMR + ' in ' + landed + ' is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' + landed +
                            ' and Kenya using GM LLC scenario under ' + landed);
                    }

                    if (exist2 == undefined && exist4 && exist4.ret2 == 'X' && exist4.ret3 == '93' && !activeCMRExistLanded && action != 'PCM') {
                        return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'Please note CMR ' + requestCMR + ' in ' + landed + ' is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' + landed +
                            ' and Kenya using GM LLC scenario under ' + landed);
                    }

                    if (exist1 && exist1.ret2 == 'C' && !activeCMRExistLanded && action != 'PCM') {
                        return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'Please note CMR ' + requestCMR + ' in ' + landed + ' and Kenya is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' +
                            landed + ' and Kenya using GM LLC scenario under ' + landed);
                    }

                    if (exist2 && exist2.ret2 == 'X' && exist2.ret3 == '93' && exist4 && exist4.ret2 == 'X' && exist4.ret3 == '93' && !activeCMRExistLanded && action != 'PCM') {
                        return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'Please note CMR ' + requestCMR + ' in ' + landed + ' and Kenya is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' +
                            landed + ' and Kenya using GM LLC scenario under ' + landed);
                    }

                }
                return new ValidationResult({
                    id: 'cmrNo',
                    type: 'text',
                    name: 'cmrNo'
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
    if (subCustGrp != undefined && (subCustGrp == 'NAELC' || subCustGrp == 'SZELC' || subCustGrp == 'LSELC')) {
        FormManager.enable('cmrNo');
        FormManager.addValidator('cmrNo', Validators.REQUIRED, ['CMR Number'], 'MAIN_IBM_TAB');
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
        if (_custType == 'LSBP' || _custType == 'SZBP' || _custType == 'ZABP' || _custType == 'NABP' || _custType == 'ZAXBP' || _custType == 'NAXBP' || _custType == 'LSXBP' || _custType == 'SZXBP' ||
            _custType == 'LSBLC' || _custType == 'SZBLC' || _custType == 'NABLC') {
            FormManager.enable('ppsceid');
            FormManager.addValidator('ppsceid', Validators.REQUIRED, ['PPS CEID'], 'MAIN_IBM_TAB');
        } else {
            FormManager.clearValue('ppsceid');
            FormManager.readOnly('ppsceid');
            FormManager.resetValidations('ppsceid');
        }
    }
}

function validateTypeOfCustomer() {
    var reqType = FormManager.getActualValue('reqType');
    if (reqType != 'U') {
        return;
    }
    FormManager.addValidator('crosSubTyp', Validators.NO_SPECIAL_CHAR, ['Type Of Customer'], 'MAIN_CUST_TAB');
}

function embargoCdValidator() {
    console.log("embargoCdValidator..............");
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                var embargoCd = FormManager.getActualValue('embargoCd');
                var validValues = ['Y', 'J', ''];

                if (!validValues.includes(embargoCd)) {
                    return new ValidationResult(null, false, 'Embargo Code can have either Y or J or empty as values');
                }
                return new ValidationResult(null, true);
            }
        };
    })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function validateCMRNumForProspect() {
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
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
                        var internalScenarios = ['ZAINT', 'NAINT', 'LSINT', 'SZINT', 'ZAXIN', 'NAXIN', 'LSXIN', 'SZXIN'];
                        if (_custSubGrp == 'INTER' || _custSubGrp == 'CRINT' || _custSubGrp == 'XINT' || internalScenarios.includes(_custSubGrp)) {
                            if (!cmrNo.startsWith("99")) {
                                return new ValidationResult(null, false, 'Internal CMR should begin with 99.');
                            }
                            if (cmrNo.length > 1 && !cmrNo.match(numPattern)) {
                                return new ValidationResult({
                                    id: 'cmrNo',
                                    type: 'text',
                                    name: 'cmrNo'
                                }, false, 'CMR Number should be number only.');
                            }
                        } else if (_custSubGrp != 'INTER' || _custSubGrp != 'CRINT' || _custSubGrp != 'XINT' || !internalScenarios.includes(_custSubGrp)) {
                            if (cmrNo.startsWith("99")) {
                                return new ValidationResult(null, false, 'CMR Starting with 99 is allowed for Internal Scenario Only.');
                            }
                            if (cmrNo.length > 1 && ((!cmrNo.startsWith('P') && isNaN(cmrNo.substring(0, 1))) || (!cmrSubNum.match(numPattern)))) {
                                return new ValidationResult({
                                    id: 'cmrNo',
                                    type: 'text',
                                    name: 'cmrNo'
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

function addStreetAddressValidator() {
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                var addrTxt = null;
                var type = null;
                var record = null;
                var reqType = FormManager.getActualValue('reqType').toUpperCase();
                var changedInd = null;

                if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
                    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
                        record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
                        if (record == null && _allAddressData != null && _allAddressData[i] != null) {
                            record = _allAddressData[i];
                        }
                        type = record.addrType;
                        addrTxt = record.addrTxt;
                        changedInd = record.updateInd;
                        if (typeof(type) == 'object') {
                            type = type[0];
                        }

                        if ((type != undefined && type != '')) {
                            if (addrTxt == '' && reqType != 'U') {
                                return new ValidationResult(null, false, 'Street Address is required for all address.');
                            } else if (addrTxt == '' && reqType == 'U' && (changedInd == 'U' || changedInd == 'N')) {
                                return new ValidationResult(null, false, 'Street Address is required for all address.');
                            }
                        }
                    }
                }
                return new ValidationResult(null, true);
            }
        };
    })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function restrictDuplicateAddrZA(cntry, addressMode, saving, finalSave, force) {
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                var requestId = FormManager.getActualValue('reqId');
                var addressSeq = FormManager.getActualValue('addrSeq');
                var addressType = FormManager.getActualValue('addrType');
                var dummyseq = "xx";
                var showDuplicateSoldToError = false;
                var qParams;
                if (addressMode == 'updateAddress') {
                    qParams = {
                        REQ_ID: requestId,
                        ADDR_SEQ: addressSeq,
                    };
                } else {
                    qParams = {
                        REQ_ID: requestId,
                        ADDR_SEQ: dummyseq,
                    };
                }
                if (addressType != undefined && addressType != '' && addressType == 'ZS01' && cmr.addressMode != 'updateAddress') {
                    var result = cmr.query('GETZS01RECORDS', qParams);
                    var zs01count = result.ret1;
                    showDuplicateSoldToError = Number(zs01count) >= 1 && addressType == 'ZS01';
                    if (showDuplicateSoldToError) {
                        return new ValidationResult(null, false, 'Only one Mailing address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
                    }
                }
                return new ValidationResult(null, true);
            }
        };
    })(), null, 'frmCMR_addressModal');

}

function validateCMRNoFORGMLLC() {
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                var cmrNo = FormManager.getActualValue('cmrNo');
                var _custSubGrp = FormManager.getActualValue('custSubGrp');
                var targetCntryCd = '764';
                var cntry = FormManager.getActualValue('cmrIssuingCntry');
                var action = FormManager.getActualValue('yourAction');
                var requestID = FormManager.getActualValue('reqId');
                var landed = 'LANDED COUNTRY';
                var processingStatus = FormManager.getActualValue('rdcProcessingStatus');
                if (FormManager.getActualValue('reqType') != 'C') {
                    return new ValidationResult(null, true);
                }
                if (cmrNo == '') {
                    return new ValidationResult(null, true);
                }

                var res = cmr.query('GET_LAND_CNTRY_ZS01', {
                    REQ_ID: requestID
                });

                if (res && res.ret1) {
                    landed = res.ret1;
                }

                if (_custSubGrp != '' && (_custSubGrp == 'LSLLC' || _custSubGrp == 'NALLC' || _custSubGrp == 'SZLLC' || _custSubGrp == 'NABLC' || _custSubGrp == 'SZBLC' || _custSubGrp == 'LSBLC')) {
                    var exist1 = cmr.query('LD.CHECK_EXISTING_CMR_NO', {
                        COUNTRY: targetCntryCd,
                        CMR_NO: cmrNo,
                        MANDT: cmr.MANDT
                    });

                    if (exist1 && exist1.ret1 && action != 'PCM' && (processingStatus == '' || processingStatus == undefined)) {
                        return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'CMR: ' + cmrNo + ' is already in use in ' + targetCntryCd + '. Please use GM LLC for already existing customer sub-scenario in ' + landed +
                            ' to create new CMR under both Kenya and ' + landed);
                    }

                    exist2 = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC', {
                        COUNTRY: targetCntryCd,
                        CMR_NO: cmrNo,
                        MANDT: cmr.MANDT
                    });
                    if (exist2 && exist2.ret1 && action != 'PCM') {
                        return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'CMR: ' + cmrNo + ' is already in use in ' + targetCntryCd + '. Please use GM LLC for already existing customer sub-scenario in ' + landed +
                            ' to create new CMR under both Kenya and ' + landed);
                    }

                    var exist3 = cmr.query('LD.CHECK_EXISTING_CMR_NO', {
                        COUNTRY: cntry,
                        CMR_NO: cmrNo,
                        MANDT: cmr.MANDT
                    });
                    if (exist3 && exist3.ret1 && action != 'PCM' && (processingStatus == '' || processingStatus == undefined)) {
                        return new ValidationResult({
                            id: 'cmrNo',
                            type: 'text',
                            name: 'cmrNo'
                        }, false, 'The requested CMR: ' + cmrNo + ' already exists in the system for country : ' + cntry);
                    }

                    exist4 = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC', {
                        COUNTRY: cntry,
                        CMR_NO: cmrNo,
                        MANDT: cmr.MANDT
                    });
                    if (exist4 && exist4.ret1 && action != 'PCM' && !cmrNo.startsWith('P')) {
                        return new ValidationResult({
                            id: 'cmrNo',
                            type: 'text',
                            name: 'cmrNo'
                        }, false, 'The requested CMR: ' + cmrNo + ' already exists in the system for country : ' + cntry);
                    }
                }
                return new ValidationResult(null, true);
            }
        };
    })(), 'MAIN_NAME_TAB', 'frmCMR');

}

function getLandedCountryDesc(requestId) {
    if (requestId != undefined && requestId != '') {
        exist = cmr.query('LD.GET_LANDED_COUNTRY_DESC', {
            REQ_ID: requestId
        });

        if (exist.ret1 != undefined) {
            return exist.ret1;
        } else {
            return '';
        }
    }
}

function validateExistingCMRNoZA(scenriosToBeSkipped) {
    return function() {
        FormManager.addFormValidator((function() {
            return {
                validate: function() {
                    console.log('checking requested cmr number...');
                    var reqType = FormManager.getActualValue('reqType');
                    var cmrNo = FormManager.getActualValue('cmrNo');
                    var cntry = FormManager.getActualValue('cmrIssuingCntry');
                    var action = FormManager.getActualValue('yourAction');
                    var _custSubGrp = FormManager.getActualValue('custSubGrp');
                    if (reqType == 'C' && cmrNo) {
                        if (scenriosToBeSkipped != undefined && scenriosToBeSkipped != null && _custSubGrp != undefined && scenriosToBeSkipped.includes(_custSubGrp)) {
                            return new ValidationResult(null, true);
                        }
                        var exists = cmr.query('LD.CHECK_EXISTING_CMR_NO', {
                            COUNTRY: cntry,
                            CMR_NO: cmrNo,
                            MANDT: cmr.MANDT
                        });
                        if (exists && exists.ret1 && action != 'PCM') {
                            return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'The requested CMR Number ' + cmrNo + ' already exists in the system.');
                        } else {
                            exists = cmr.query('LD.CHECK_EXISTING_CMR_NO_RESERVED', {
                                COUNTRY: cntry,
                                CMR_NO: cmrNo,
                                MANDT: cmr.MANDT
                            });
                            if (exists && exists.ret1) {
                                return new ValidationResult({
                                    id: 'cmrNo',
                                    type: 'text',
                                    name: 'cmrNo'
                                }, false, 'The requested CMR Number ' + cmrNo + ' already exists in the system.');
                            }
                        }
                        var exists1 = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC', {
                            COUNTRY: cntry,
                            CMR_NO: cmrNo,
                            MANDT: cmr.MANDT
                        });
                        if (exists1 && exists1.ret1 && action != 'PCM' && !cmrNo.startsWith('P')) {
                            return new ValidationResult({
                                id: 'cmrNo',
                                type: 'text',
                                name: 'cmrNo'
                            }, false, 'The requested CMR: ' + cmrNo + ' already exists in the system for country : ' + cntry);
                        }
                    }
                    return new ValidationResult({
                        id: 'cmrNo',
                        type: 'text',
                        name: 'cmrNo'
                    }, true);
                }
            };
        })(), 'MAIN_IBM_TAB', 'frmCMR');
    };
}

/* Overriding Address Grid Formatters for SA */
function streetValueFormatter(value, rowIndex) {
    var rowData = this.grid.getItem(rowIndex);
    var streetCont = rowData.addrTxt2;
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    if (cntry == '864') {
        if (value && streetCont && streetCont[0]) {
            return value + '<br>' + streetCont;
        } else if (streetCont && streetCont[0]) {
            return streetCont;
        }
        return value;
    }

    if (streetCont && streetCont[0]) {
        return value + '<br>' + streetCont;
    }
    return value;
}

function addInacCodeValidator() {
    console.log("addInacCodeValidator SA..............");
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                var value = FormManager.getActualValue('inacCd');
                var inacCd1 = value.substring(0, 2);
                var inacCd2 = value.substring(2, 4);
                var result = false;
                if (value && value.length == 4) {
                    if (value && value.length > 0 && isNaN(value)) {
                        result = false;
                        if (inacCd1 && inacCd1.length > 0 && inacCd1.match("^[a-zA-Z]+$")) {
                            result = true;
                            if (isNaN(inacCd2)) {
                                result = false;
                            }
                        } else {
                            result = false;
                        }
                    } else {
                        result = true;
                    }
                } else {
                    result = false;
                }
                if (value.length == 0 || value == '') {
                    result = true;
                }
                if (!result) {
                    return new ValidationResult(null, false, 'Invalid value for INAC Code.');
                }
                return new ValidationResult(null, true);
            }
        };
    })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function requireVATForCrossBorder() {
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
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
                if (custSubGrp != null && (custSubGrp.includes('XSOFT') || custSubGrp.includes('XSL') || custSubGrp.includes('XPRIC'))) {
                    return new ValidationResult(null, true);
                }

                var vat = FormManager.getActualValue('vat');
                var zs01Cntry = FormManager.getActualValue('cmrIssuingCntry');
                var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
                    REQID: FormManager.getActualValue('reqId'),
                    TYPE: 'ZS01'
                });
                if (ret && ret.ret1 && ret.ret1 != '') {
                    zs01Cntry = ret.ret1;
                }

                if ((!vat || vat == '' || vat.trim() == '') && !dijit.byId('vatExempt').get('checked')) {
                    if (GEOHandler.VAT_RQD_CROSS_LNDCNTRY.indexOf(zs01Cntry) >= 0) {
                        var msg = "VAT for " + zs01Cntry + " # is required.";
                        return new ValidationResult({
                            id: 'vat',
                            type: 'text',
                            name: 'vat'
                        }, false, msg);
                    }
                }
                return new ValidationResult(null, true);
            }
        };
    })(), 'MAIN_CUST_TAB', 'frmCMR');
}

var _isScenarioChanged = false;

function checkScenarioChanged(fromAddress, scenario, scenarioChanged) {
    _isScenarioChanged = scenarioChanged;
    scenarioChangeSalesRepSBO = scenarioChanged;
    setCtcSalesRepSBO(FormManager.getActualValue('isuCd'));
}

function resetVatRequired(value) {
    var viewOnly = FormManager.getActualValue('viewOnlyPage');
    if (viewOnly != '' && viewOnly == 'true') {
        return;
    }
    if (FormManager.getActualValue('reqType') == 'C') {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var isIBPriv = custSubType != '' && (custSubType.includes('XPC') || custSubType.includes('XIB') || custSubType.includes('IB') || custSubType.includes('PC'));
        var zs01Cntry = FormManager.getActualValue('cmrIssuingCntry');
        var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
            REQID: FormManager.getActualValue('reqId'),
            TYPE: 'ZS01'
        });
        if (ret && ret.ret1 && ret.ret1 != '') {
            zs01Cntry = ret.ret1;
        }

        if (GEOHandler.VAT_RQD_CROSS_LNDCNTRY.indexOf(zs01Cntry) >= 0 && custSubType != '') {
            FormManager.enable('vatExempt');
            if (_isScenarioChanged && !value && (custSubType != '' && !isIBPriv)) {
                FormManager.getField('vatExempt').set('checked', false);
            }
            if (dijit.byId('vatExempt') != undefined && (dijit.byId('vatExempt').get('checked') ||
                    (!dijit.byId('vatExempt').get('checked') && isIBPriv))) {
                FormManager.removeValidator('vat', Validators.REQUIRED);
            }

        } else {
            FormManager.getField('vatExempt').set('checked', false);
            FormManager.hide('VATExempt', 'vatExempt');
            FormManager.removeValidator('vat', Validators.REQUIRED);

        }

    }
}

function resetVatExempt() {
    var viewOnly = FormManager.getActualValue('viewOnlyPage');
    if (viewOnly != '' && viewOnly == 'true') {
        return;
    }
    var vat = FormManager.getActualValue('vat');

    if (vat != null && vat.length > 0) {
        if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt').get('checked')) {
            FormManager.getField('vatExempt').set('checked', false);
        }
    }
}

function vatExemptOnScenario() {
    var viewOnly = FormManager.getActualValue('viewOnlyPage');
    if (viewOnly != '' && viewOnly == 'true') {
        return;
    }
    var custSubType = FormManager.getActualValue('custSubGrp');
    var vat = FormManager.getActualValue('vat');
    var vatExempt = dijit.byId('vatExempt').get('checked');

    if (custSubType != '' && (custSubType.includes('XPC') || custSubType.includes('XIB') || custSubType.includes('IB') || custSubType.includes('PC'))) {
        if (_isScenarioChanged && (vat == null || vat.length == 0) && vatExempt != true) {
            FormManager.getField('vatExempt').set('checked', true);
            FormManager.removeValidator('vat', Validators.REQUIRED);
        }
    }
}

/* End 1430539 */


// CREATCMR-4293
function setCTCValues() {

    FormManager.removeValidator('clientTier', Validators.REQUIRED);

}

function clientTierCodeValidator() {
    var isuCode = FormManager.getActualValue('isuCd');
    var clientTierCode = FormManager.getActualValue('clientTier');
    var reqType = FormManager.getActualValue('reqType');

    if (((isuCode == '21' || isuCode == '8B' || isuCode == '5K') && reqType == 'C') || ((isuCode != '34' && isuCode != '27' && isuCode != '36') && reqType == 'U')) {
        if (clientTierCode == '') {
            $("#clientTierSpan").html('');

            return new ValidationResult(null, true);
        } else {
            $("#clientTierSpan").html('');

            return new ValidationResult({
                id: 'clientTier',
                type: 'text',
                name: 'clientTier'
            }, false, 'Client Tier can only accept blank.');
        }
    } else if (isuCode == '34') {
        if (clientTierCode == '') {
            return new ValidationResult({
                id: 'clientTier',
                type: 'text',
                name: 'clientTier'
            }, false, 'Client Tier code is Mandatory.');
        } else if (clientTierCode == 'Q') {
            return new ValidationResult(null, true);
        } else {
            return new ValidationResult({
                id: 'clientTier',
                type: 'text',
                name: 'clientTier'
            }, false, 'Client Tier can only accept \'Q\'\'.');
        }
    } else if (isuCode == '27') {
        if (clientTierCode == '') {
            return new ValidationResult({
                id: 'clientTier',
                type: 'text',
                name: 'clientTier'
            }, false, 'Client Tier code is Mandatory.');
        } else if (clientTierCode == 'E') {
            return new ValidationResult(null, true);
        } else {
            return new ValidationResult({
                id: 'clientTier',
                type: 'text',
                name: 'clientTier'
            }, false, 'Client Tier can only accept \'E\'\'.');
        }
    } else if (isuCode == '36') {
        if (clientTierCode == '') {
            return new ValidationResult({
                id: 'clientTier',
                type: 'text',
                name: 'clientTier'
            }, false, 'Client Tier code is Mandatory.');
        } else if (clientTierCode == 'Y') {
            return new ValidationResult(null, true);
        } else {
            return new ValidationResult({
                id: 'clientTier',
                type: 'text',
                name: 'clientTier'
            }, false, 'Client Tier can only accept \'Y\'\'.');
        }
    } else if (isuCode != '36' || isuCode != '34' || isuCode != '27') {
        if (clientTierCode == '') {

            return new ValidationResult(null, true);
        } else {
            return new ValidationResult({
                id: 'clientTier',
                type: 'text',
                name: 'clientTier'
            }, false, 'Client Tier can only accept blank.');
        }
    } else {
        if (clientTierCode == 'Q' || clientTierCode == 'Y' || clientTierCode == 'E' || clientTierCode == '') {
            $("#clientTierSpan").html('');

            return new ValidationResult(null, true);
        } else {
            $("#clientTierSpan").html('');
            $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

            return new ValidationResult({
                id: 'clientTier',
                type: 'text',
                name: 'clientTier'
            }, false, 'Client Tier can only accept \'Q\', \'Y\', \'E\' or blank.');
        }
    }
}

// CREATCMR-4293

function clientTierValidator() {
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
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
                        REQ_ID: requestId,
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
    FormManager.addFormValidator((function() {
        return {
            validate: function() {

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
                    COUNTRY: cntry,
                    CMRNO: cmrNo,
                    MANDT: cmr.MANDT
                });

                if (resultsCC != null && resultsCC != undefined && resultsCC.ret1 != '') {
                    uptsrdc = resultsCC.ret1;
                    // console.log('lastupdatets in RDC = ' + uptsrdc);
                }

                var results11 = cmr.query('GETUPTSADDR', {
                    REQ_ID: reqId
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
// CREATCMR-788
function addressQuotationValidatorZA() {
    FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, ['Abbreviated Name'], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, ['Abbreviated Location'], 'MAIN_CUST_TAB');
    FormManager.addValidator('custNm1', Validators.NO_QUOTATION, ['Customer Name']);
    FormManager.addValidator('custNm2', Validators.NO_QUOTATION, ['Customer Name Con\'t']);
    FormManager.addValidator('custNm4', Validators.NO_QUOTATION, ['Att. Person']);
    FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, ['Street']);
    FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, ['Street Con\'t']);
    FormManager.addValidator('city1', Validators.NO_QUOTATION, ['City']);
    // FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal
    // Code'
    // ]);
    FormManager.addValidator('poBox', Validators.NO_QUOTATION, ['PO Box']);
    FormManager.addValidator('custPhone', Validators.NO_QUOTATION, ['Phone #']);

}

function setEnterpriseBehaviour() {
    console.log("Inside setEnterpriseBehaviour ");
    var reqType = FormManager.getActualValue('reqType').toUpperCase();
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var custType = FormManager.getActualValue('custSubGrp');
    var isuCode = FormManager.getActualValue('isuCd');
    var clientTierCode = FormManager.getActualValue('clientTier');
    var isuCtc = isuCode + clientTierCode;
    var countryUse = FormManager.getActualValue('countryUse');
    var countryUseSubRegions = ['864NA', '864LS', '864SZ'];

    if (reqType == 'U') {
        FormManager.removeValidator('enterprise', Validators.REQUIRED);
        return;
    }

    // hide enterprise for BP, IBM employee and Internal
    var custTypeHideList = ['LSBP', 'LSXBP', 'LSBLC', 'NABP', 'NAXBP', 'NABLC', 'SZBP', 'SZXBP', 'SZBLC', 'ZABP', 'ZAXBP', 'LSIBM', 'LSXIB', 'LSXIN', 'LSINT', 'NAIBM', 'NAXIB', 'NAXIN', 'NAINT', 'SZIBM', 'SZXIB', 'SZXIN', 'SZINT', 'ZAIBM', 'ZAXIB', 'ZAXIN', 'ZAINT'];
    var custTypePrivList = ['LSPC', 'LSXPC', 'NAPC', 'NAXPC', 'SZPC', 'SZXPC', 'ZAPC', 'ZAXPC'];
    if (custTypeHideList.includes(custType)) {
        FormManager.hide('Enterprise', 'enterprise');
    } else {
        FormManager.show('Enterprise', 'enterprise');
        if (custTypePrivList.includes(custType)) {
            FormManager.readOnly('enterprise');
            FormManager.addValidator('enterprise', Validators.REQUIRED, ['Enterprise'], 'MAIN_IBM_TAB');
        } else if (isuCtc == '36Y' || ((isuCtc == '27E' || isuCtc == '34Q') && countryUse == '864')) {
            FormManager.enable('enterprise');
            FormManager.removeValidator('enterprise', Validators.REQUIRED);
        } else if (isuCtc == '27E' && countryUseSubRegions.includes(countryUse)) {
            FormManager.readOnly('enterprise');
            FormManager.addValidator('enterprise', Validators.REQUIRED, ['Enterprise'], 'MAIN_IBM_TAB');
        } else {
            FormManager.enable('enterprise');
            FormManager.removeValidator('enterprise', Validators.REQUIRED);
        }
    }
}

function setDefaultCovCBMEA() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');
  var subRegion = FormManager.getActualValue('countryUse');
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd == '') {
    subIndustryCd = _pagemodel.subIndustryCd;
  }
  if (subIndustryCd != null && subIndustryCd.length > 1) {
    subIndustryCd = subIndustryCd.substring(0, 1);
  }
var crossSubTypes = [ 'ZAXCO', 'ZAXGO', 'ZAXPC', 'ZAXTP', 'SZXCO', 'SZXGO', 'SZXPC', 'SZXTP', 'NAXCO', 'NAXGO', 'NAXPC', 'NAXTP' ];
var  privScenarios = ['ZAXPC','SZXPC','NAXPC','SZ'];
var  SOUTH_AFRICA_LC = ['ZA','NA','LS','SZ'];
var  TURKEY_LC = ['TR'];
var  CEWA_LC = ['DZ', 'TN', 'LY', 'AO', 'BW', 'CV', 'CD', 'MG', 'MW', 'MU', 'MZ', 'ST', 'SC', 'ZM', 'ZW', 'GH', 'LR', 'NG', 'SL', 'BI', 'ER', 'ET', 'DJ', 'KE', 'RW', 'SO', 'SD', 'TZ', 'UG', 'BJ', 'BF', 'CM', 'CF', 'TD', 'CG', 'GQ', 'GA', 'GM', 'GN', 'GW', 'CI', 'ML', 'MR', 'NE', 'SN', 'TG'];
var  ME_LC = ['LY', 'TN', 'MA', 'PK', 'AF', 'EG', 'BH', 'AE', 'AE', 'IQ', 'JO', 'PS', 'KW', 'LB', 'OM', 'QA', 'SA', 'YE', 'SY'];

  var landCntry = '';
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  // GET LANDCNTRY in case of CB
  var result = cmr.query('LANDCNTRY.IT', {
    REQID : reqId
  });
  if (result != null && result.ret1 != undefined) {
    landCntry = result.ret1;
  }

  if (reqType != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true' || !crossSubTypes.includes(custSubType) || landCntry == '' || !(CEWA_LC.includes(landCntry) || ME_LC.includes(landCntry) || TURKEY_LC.includes(landCntry) || SOUTH_AFRICA_LC.includes(landCntry))) {
    return;
  }
  
  // Set Defualt ISU CTC for CROSS MEA REGION
  var MEA34Q = ['PK','AF' ,'MA','TN','LY','QA','EG'];
  var MEAsubIndustry34Q =['E','G','V','Y','H','X'];
  if( CEWA_LC.includes(landCntry) || MEA34Q.includes(landCntry) || (landCntry == 'SA' && MEAsubIndustry34Q.includes(subIndustryCd))) {
      // CEWA MEA REGION
          FormManager.setValue('isuCd', '34');
          FormManager.setValue('clientTier', 'Q');
      }
  var isu = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  var isuCTC = isu.concat(ctc);

    if(crossSubTypes.includes(custSubType)) {     
     if( TURKEY_LC.includes(landCntry)) {
       // TURKEY MEA REGION

      if(subIndustryCd == 'G') {
        ims = _pagemodel.subIndustryCd;
       if(ims != '' && !(ims.endsWith('B') || ims.endsWith('F') || ims.endsWith('J') || ims.endsWith('N'))) {
         FormManager.setValue('enterprise', '911715');
       } else {
         FormManager.setValue('enterprise', '911718');
       }
      } else {
          var qParams = {
          _qall : 'Y',
          ISSUING_CNTRY : cntry,
          SALES_BO_CD : '%' + subIndustryCd + '%',
          SALES_BO_DESC : '%' + landCntry + '%',
          ISU_CD : '%' + isuCTC + '%'
        };
        var results = cmr.query('GET.ENTERPRISEVALUE.BYSUBINDUSTRY', qParams);
        var enterprise = results.ret1;
    FormManager.setValue('enterprise', enterprise);
      }
     }      
    else if( SOUTH_AFRICA_LC.includes(landCntry)) {
    // SOUTH AFRICA MEA REGION
    if (subRegion == '864LS') {
      FormManager.setValue('enterprise', '910510');
    } else if (subRegion  == '864NA') {
      FormManager.setValue('enterprise', '909813');
    } else if (subRegion == '864SZ') {
      FormManager.setValue('enterprise', '910509');
    } else {
       var qParams = {
          _qall : 'Y',
          ISSUING_CNTRY : cntry,
          SALES_BO_CD : '%' + subIndustryCd + '%',
          SALES_BO_DESC : '%' + landCntry + '%',
          ISU_CD : '%' + isuCTC + '%'
        };
        var results = cmr.query('GET.ENTERPRISEVALUE.BYSUBINDUSTRY', qParams);
        var enterprise = results.ret1;
    FormManager.setValue('enterprise', enterprise);
    }

     } else if( CEWA_LC.includes(landCntry)) {
    // CEWA MEA REGION
    var qParams = {
          _qall : 'Y',
          ISSUING_CNTRY : cntry,
          SALES_BO_DESC : '%' + landCntry + '%',
          ISU_CD : '%' + isuCTC + '%'
        };
        var results = cmr.query('GET.ENTERPRISEVALUE.BYLANDCNTRY', qParams);
        var enterprise = results.ret1;
    FormManager.setValue('enterprise', enterprise);
     
     } else if( ME_LC.includes(landCntry)) {
    // ME MEA REGION
        var qParams = {
          _qall : 'Y',
          ISSUING_CNTRY : cntry,
          SALES_BO_CD : '%' + subIndustryCd + '%',
          SALES_BO_DESC : '%' + landCntry + '%',
          ISU_CD : '%' + isuCTC + '%'
        };
        var results = cmr.query('GET.ENTERPRISEVALUE.BYSUBINDUSTRY', qParams);
        var enterprise = results.ret1;
    FormManager.setValue('enterprise', enterprise);
     }     
    }  
    }

function enterpriseValidator() {
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                var numPattern = /^[0-9]+$/;
                var reqType = FormManager.getActualValue('reqType');
                var custSubGrp = FormManager.getActualValue('custSubGrp');
                var enterprise = FormManager.getActualValue('enterprise');
                var countryUse = FormManager.getActualValue('countryUse');
                var isuCode = FormManager.getActualValue('isuCd');
                var clientTierCode = FormManager.getActualValue('clientTier');
                var isuCtc = isuCode + clientTierCode;
                var isuCtcWithBlank = ['04', '12', '28', '4F', '5K'];
                if (reqType == 'U') {
                  return;
              }
                if (enterprise.length >= 1 && enterprise.length != 6) {
                    return new ValidationResult(null, false, 'Enterprise Number should be 6 digit long.');
                }

                if (enterprise.length >= 1 && !enterprise.match(numPattern)) {
                    return new ValidationResult({
                        id: 'enterprise',
                        type: 'text',
                        name: 'enterprise'
                    }, false, 'Enterprise Number should be numeric only.');
                }

                if(isuCtc == '36Y'){
                  return checkCondition36Y(countryUse, enterprise, isuCtc);
                } else if (isuCtc == '34Q'){
                  return checkCondition34Q(countryUse, enterprise, isuCtc);
                } else if (isuCtc == '27E'){
                  return checkCondition27E(countryUse, enterprise, isuCtc);
                } else if(isuCtcWithBlank.includes(isuCtc))  {
                  return checkConditionIsuCtcWithBlank(enterprise, isuCtc);
                }
                return new ValidationResult(null, true);
            }
        };
    })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function checkCondition36Y(countryUse, enterprise, isuCtc) {
    var validEnterpriseIdsZA_36Y = ['008028', '010032', '012430'];
    var validEnterpriseIdsLS = ['012097', '012098', '012099'];
    var validEnterpriseIdsNA = ['009814', '012095', '012096'];
    var validEnterpriseIdsSZ = ['012100', '012101', '012102'];
      if((!validEnterpriseIdsZA_36Y.includes(enterprise) && countryUse == '864')){
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : ' + validEnterpriseIdsZA_36Y);
      } else if(!validEnterpriseIdsLS.includes(enterprise) && countryUse == '864LS'){
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : ' + validEnterpriseIdsLS);
      } else if(!validEnterpriseIdsNA.includes(enterprise) && countryUse == '864NA'){
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : ' + validEnterpriseIdsNA);
      } else if(!validEnterpriseIdsSZ.includes(enterprise) && countryUse == '864SZ'){
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : ' + validEnterpriseIdsSZ);
      }
}

function checkCondition34Q(countryUse, enterprise, isuCtc) {
    var validEnterpriseIdsZA_34Q = ['004179', '011678'];
    if (!validEnterpriseIdsZA_34Q.includes(enterprise) && countryUse == '864') {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : ' + validEnterpriseIdsZA_34Q);
    }
}

function checkConditionIsuCtcWithBlank(enterprise, isuCtc) {
    var isuCtcWithBlank = ['04', '12', '28', '4F', '5K'];
    if (isuCtcWithBlank.includes(isuCtc) && enterprise != '') {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept Blank');
    }
}

function checkCondition27E(countryUse, enterprise, isuCtc) {
    var validEnterpriseIdsZA_27E = ['011675', '011677', '011676', '011672', '011673', '011674', '011680', '011684', '011679', '011681', '011682', '011683'];
    var validEnterpriseIdsLS = '910510';
    var validEnterpriseIdsNA = '909813';
    var validEnterpriseIdsSZ = '910509';
    if(!validEnterpriseIdsZA_27E.includes(enterprise) && countryUse == '864'){
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : ' + validEnterpriseIdsZA_27E);
    } else if(!validEnterpriseIdsLS.includes(enterprise) && countryUse == '864LS'){
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : ' + validEnterpriseIdsLS);
    } else if(!validEnterpriseIdsNA.includes(enterprise) && countryUse == '864NA'){
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : ' + validEnterpriseIdsNA);
    } else if(!validEnterpriseIdsSZ.includes(enterprise) && countryUse == '864SZ'){
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : ' + validEnterpriseIdsSZ);
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
        REQ_ID: requestId,
    });

    if (result != null && result != '') {
        _oldCtc = result.ret1;
        _oldIsuCd = result.ret3;
        _oldEnt = result.ret4;
    }
}

function setDefaultEnterpriseBasedOnSubInd(value) {
    // '27E' Logic
    var reqType = FormManager.getActualValue('reqType');
    var countryUse = FormManager.getActualValue('countryUse');
    var subInd = FormManager.getActualValue('subIndustryCd');
    var isuCd = FormManager.getActualValue('isuCd');
    var ctc = FormManager.getActualValue('clientTier');
    var isuCtc = isuCd + ctc;
    var isuCtcForBlankEntp = ['04', '12', '28', '4F', '5K'];
    if (reqType == 'U') {
      return;
    }
    if (isuCtc != '27E' && isuCtcForBlankEntp.includes(isuCtc)){
      FormManager.setValue('enterprise', '');
      return;
    } else {
        if (subInd != null || subInd != undefined || subInd != '') {
            subInd = subInd[0];
        }
        if (countryUse == '864') {
            var subIndEntGrp = {
                '011675': ['A', 'K', 'U'],
                '011677': ['B', 'C'],
                '011676': ['D', 'R', 'T', 'W'],
                '011672': ['E', 'G', 'H', 'X', 'Y'],
                '011673': ['F', 'N', 'S'],
                '011674': ['J', 'L', 'M', 'P', 'V']
            };
            var entps = Object.keys(subIndEntGrp);
            entps.forEach(entp => {
                var subIndTyp = subIndEntGrp[entp];
                if (subIndTyp.includes(subInd))
                    FormManager.setValue('enterprise', entp);
            });
        } else if (countryUse == '864LS') {
            FormManager.setValue('enterprise', '910510');
        } else if (countryUse == '864NA') {
            FormManager.setValue('enterprise', '909813');
        } else if (countryUse == '864SZ') {
            FormManager.setValue('enterprise', '910509');
        }
    }
}

function StcOrderBlockValidation() {
    FormManager.addFormValidator((function() {
        return {
            validate: function() {
                // var role =
                // FormManager.getActualValue('userRole').toUpperCase();
                var ordBlk = FormManager.getActualValue('embargoCd');
                var stcOrdBlk = FormManager.getActualValue('taxExemptStatus3');
                if (ordBlk == null || ordBlk == '') {
                    if (stcOrdBlk == 'ST' || stcOrdBlk == '') {} else {
                        return new ValidationResult(null, false, 'Only ST and blank STC order block code allowed.');
                    }
                } else if (ordBlk != '' && stcOrdBlk != '') {
                    return new ValidationResult(null, false, 'Please fill either STC order block code or Embargo Code field');
                }
                return new ValidationResult(null, true);
            }
        };
    })(), 'MAIN_CUST_TAB', 'frmCMR');
}

dojo.addOnLoad(function() {
    GEOHandler.MCO1 = [SysLoc.SOUTH_AFRICA];
    console.log('adding MCO1 functions...');
    GEOHandler.addAddrFunction(addMCO1LandedCountryHandler, GEOHandler.MCO1);
    GEOHandler.enableCopyAddress(GEOHandler.MCO1, validateMCOCopy, ['ZD01']);
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
    GEOHandler.addAfterTemplateLoad(showDeptNoForInternalsOnly, GEOHandler.MCO1);
    GEOHandler.addAfterTemplateLoad(onChangeSubCustGroup, GEOHandler.MCO1);
    GEOHandler.addAfterTemplateLoad(addValidatorStreet, GEOHandler.MCO1);
    GEOHandler.addAfterConfig(addValidatorStreet, GEOHandler.MCO1);

    GEOHandler.registerValidator(addAddressTypeValidator, GEOHandler.MCO1, null, true);
    // GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.MCO1,
    // null, true);
    GEOHandler.registerValidator(addCrossBorderValidatorForZA, GEOHandler.MCO1, null, true);
    GEOHandler.registerValidator(addAttachmentValidator, GEOHandler.MCO1, null, true);
    GEOHandler.registerValidator(addGenericVATValidator(SysLoc.SOUTH_AFRICA, 'MAIN_CUST_TAB', 'frmCMR'), [SysLoc.SOUTH_AFRICA], null, true);
    GEOHandler.registerValidator(requireVATForCrossBorder, GEOHandler.MCO1, null, true);
    GEOHandler.registerValidator(streetValidatorCustom, GEOHandler.MCO1, null, true);

    GEOHandler.addAddrFunction(addAddrValidatorMCO1, GEOHandler.MCO1);
    GEOHandler.addAddrFunction(changeAbbrevNmLocn, GEOHandler.MCO1);
    GEOHandler.addAddrFunction(addATTperson, GEOHandler.MCO1);
    GEOHandler.addAddrFunction(disableAddrFieldsZA, GEOHandler.MCO1);

    /* 1438717 - add DPL match validation for failed dpl checks */
    GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.MCO1, GEOHandler.ROLE_PROCESSOR, true);
    GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.MCO1);
    GEOHandler.addAfterConfig(lockSORTL, GEOHandler.MCO1);
    GEOHandler.addAfterTemplateLoad(lockSORTL, GEOHandler.MCO1);
    GEOHandler.registerValidator(addStreetContPoBoxLengthValidator, GEOHandler.MCO1, null, true);
    GEOHandler.registerValidator(addCityPostalCodeLengthValidator, GEOHandler.MCO1, null, true);
    GEOHandler.registerValidator(addCrossLandedCntryFormValidator, GEOHandler.MCO1, null, true);
    GEOHandler.registerValidator(addIbmDeptCostCntrValidator, GEOHandler.MCO1, null, true);

    GEOHandler.addAfterConfig(onLobchange, GEOHandler.MCO1);
    GEOHandler.addAfterTemplateLoad(onLobchange, GEOHandler.MCO1);
    GEOHandler.addAfterConfig(setCofField, GEOHandler.MCO1);
    GEOHandler.addAfterConfig(setSalesRepSORTL, GEOHandler.MCO1);
    GEOHandler.addAfterTemplateLoad(setCofField, GEOHandler.MCO1);
    GEOHandler.registerValidator(validateCMRForExistingGMLLCScenario, [SysLoc.SOUTH_AFRICA], null, true);
    GEOHandler.addAfterConfig(enableCMRNOSAGLLC, SysLoc.SOUTH_AFRICA);
    GEOHandler.addAfterTemplateLoad(enableCMRNOSAGLLC, SysLoc.SOUTH_AFRICA);
    GEOHandler.addAfterConfig(enableCmrForProcessor, [SysLoc.SOUTH_AFRICA]);
    GEOHandler.addAfterTemplateLoad(mandatoryForBusinessPartner, [SysLoc.SOUTH_AFRICA]);
    GEOHandler.addAfterConfig(validateTypeOfCustomer, GEOHandler.MCO1);
    // GEOHandler.registerValidator(addInacCodeValidator, [ SysLoc.SOUTH_AFRICA
    // ],
    // null, true);
    GEOHandler.registerValidator(embargoCdValidator, [SysLoc.SOUTH_AFRICA], null, true);
    GEOHandler.registerValidator(validateCMRNumForProspect, [SysLoc.SOUTH_AFRICA], GEOHandler.ROLE_PROCESSOR, true);
    GEOHandler.registerValidator(addStreetAddressValidator, [SysLoc.SOUTH_AFRICA], null, true);
    GEOHandler.registerValidator(restrictDuplicateAddrZA, [SysLoc.SOUTH_AFRICA], null, true);
    GEOHandler.registerValidator(validateCMRNoFORGMLLC, [SysLoc.SOUTH_AFRICA], null, true);
    GEOHandler.registerValidator(validateExistingCMRNoZA([...scenarioToSkipCMRValidationZA]), [SysLoc.SOUTH_AFRICA], GEOHandler.ROLE_PROCESSOR, true);


    GEOHandler.addAfterTemplateLoad(checkScenarioChanged, GEOHandler.MCO1);
    GEOHandler.addAfterTemplateLoad(retainImportedValues, GEOHandler.MCO1);
    GEOHandler.addAfterTemplateLoad(resetVatRequired, GEOHandler.MCO1);
    GEOHandler.addAfterConfig(resetVatExempt, GEOHandler.MCO1);
    GEOHandler.addAfterTemplateLoad(resetVatExempt, GEOHandler.MCO1);
    GEOHandler.addAfterTemplateLoad(vatExemptOnScenario, GEOHandler.MCO1);

    // CREATCMR-4293
    GEOHandler.addAfterTemplateLoad(setCTCValues, GEOHandler.MCO1);
    GEOHandler.registerValidator(clientTierValidator, GEOHandler.MCO1, null, true);
    GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [SysLoc.SOUTH_AFRICA], null, true);

    // CREATCMR-7985
    GEOHandler.addAfterTemplateLoad(setEnterpriseBehaviour, [SysLoc.SOUTH_AFRICA]);
    GEOHandler.addAfterConfig(setEnterpriseBehaviour, [SysLoc.SOUTH_AFRICA]);
    GEOHandler.addAfterTemplateLoad(setDefaultCovCBMEA, [SysLoc.SOUTH_AFRICA]);
    GEOHandler.registerValidator(enterpriseValidator, [SysLoc.SOUTH_AFRICA], null, true);
    GEOHandler.registerValidator(StcOrderBlockValidation, GEOHandler.MCO1, null, true);

});