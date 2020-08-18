/* Register MCO Javascripts */
var _districtCdHandler = null;
var valCheckerInterval = null;
var fieldDisablerRepeaterInterval = null;
var abbrvNmProcessorInterval = null;
var crossborderScenariosInterval = null;
var _embargoCdHandler = null;
var _oldEmbargoCd = null;
var _oldReqReason = null;
var _oldOrdBlk = null;
var _importedIndc = null;
var _postalCodeHandler = null;
var _ISICHandler = null;

function afterConfigPT() {
  FormManager.enable('vat'); 
  FormManager.readOnly('subIndustryCd');
}

function afterTemplateLoadPT() {
  var _reqId = FormManager.getActualValue('reqId');
  var subCustGrp = FormManager.getActualValue('custSubGrp');
  var addrType = FormManager.getActualValue('addrType');
  var city1 = FormManager.getActualValue('city1');
  var custGrp = FormManager.getActualValue('custGrp');

  if (custGrp != 'LOCAL') {
    return;
  }

  if (subCustGrp == 'INTSO' || subCustGrp == 'THDPT') {
    var city2Params = {
      REQ_ID : _reqId,
      ADDR_TYPE : "ZI01",
    };
    var city2Result = cmr.query('ADDR.GET.CITY.BY_REQID_ADDRTYP', city2Params);
    var city1 = city2Result.ret1;
    if (city1 != '' && subCustGrp == 'INTSO' || subCustGrp == 'THDPT') {
      FormManager.setValue('abbrevLocn', city1);
    }
  } else if (subCustGrp != 'INTSO' || subCustGrp != 'THDPT') {
    var city1Params = {
      REQ_ID : _reqId,
      ADDR_TYPE : "ZS01",
    };
    var city1Result = cmr.query('ADDR.GET.CITY.BY_REQID_ADDRTYP', city1Params);
    var city1 = city1Result.ret1;
    if (city1 != '' && subCustGrp != 'SAAPA') {
      if (city1 && city1.length > 12) {
        city1 = city1.substring(0, 12);
      }
      FormManager.setValue('abbrevLocn', city1);
    } else if (subCustGrp == 'SAAPA') {
      FormManager.setValue('abbrevLocn', 'SAAS');
    }
  }

  var custNm1Params = {
    REQ_ID : _reqId,
    ADDR_TYPE : "ZI01",
  };
  var custNm1Result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', custNm1Params);
  var custNm1 = custNm1Result.ret1;

  var custNm1Params2 = {
    REQ_ID : _reqId,
    ADDR_TYPE : "ZS01",
  };
  var custNm1Result2 = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', custNm1Params2);
  var custNm2 = custNm1Result2.ret1;

  if (custNm1 != '' && subCustGrp == 'INTSO' || subCustGrp == 'THDPT') {
    FormManager.setValue('abbrevNm', custNm2.substring(0, 8) + " " + custNm2.substring(0, 9));
  } else {
    FormManager.setValue('abbrevNm', custNm2);
  }
  crossborderScenariosAbbrvLocOnChange();
}

function addHandlersForPT() {
  if (_postalCodeHandler == null) {
    _postalCodeHandler = dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
      setTaxCodeOnPostalCodePT();
    });
  }

  if (_ISICHandler == null) {
    _ISICHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      isuAndCtcBasedOnISIC();
    });
  }
}

function getImportedIndcForPT() {
  if (_importedIndc) {
    console.log('Returning imported indc = ' + _importedIndc);
    return _importedIndc;
  }
  var results = cmr.query('VALIDATOR.IMPORTED_PT', {
    REQID : FormManager.getActualValue('reqId')
  });
  if (results != null && results.ret1) {
    _importedIndc = results.ret1;
  } else {
    _importedIndc = 'N';
  }
  console.log('saving imported ind as ' + _importedIndc);
  return _importedIndc;
}

function setTaxCodeOnPostalCodePT(postCd) {

  var postCd = FormManager.getActualValue('postCd');
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (custSubGrp == 'CRISO' || custSubGrp == 'CRIINT' || custSubGrp == 'CRPRI' || custSubGrp == 'XCRO' || custSubGrp == 'XBP') {
    return;
  }
  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
  };

  var result = cmr.query('ADDR.GET.POST_CD.BY_REQID', qParams);
  var postCodeOrg = '';
  if (result != null && result.ret1 != undefined) {
    postCodeOrg = result.ret1;
  } else {
    postCodeOrg = '';
  }

  var postCode = parseInt(postCodeOrg.substring(0, 1));
  if (postCd && postCd != undefined && postCd != '') {
    postCode = postCd.substring(0, 1);
  }

  // set Tax code based on postal-Code logic
  var checkImportIndc = getImportedIndcForPT();
  if (checkImportIndc != 'Y') {
    if (custSubGrp == 'GOVRN' && postCode == 9) {
      FormManager.setValue('specialTaxCd', '18');
    } else if (custSubGrp != 'GOVRN' && postCode == 9) {
      FormManager.setValue('specialTaxCd', '23');
    }
  }
}

function addMCOLandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
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
  // Unlock EmbargoCode field for Spain
  if (SysLoc.SPAIN == FormManager.getActualValue('cmrIssuingCntry') && FormManager.getActualValue('reqType') == 'U') {
    FormManager.enable('embargoCd');
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    FormManager.readOnly('embargoCd');
  } else {
    FormManager.enable('embargoCd');
  }
}

function addEmbargoCodeValidatorSpain() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd');

        if (embargoCd != '' && embargoCd.length > 0) {
          embargoCd = embargoCd.trim();
          if ((embargoCd != '' && embargoCd.length == 1) && (embargoCd == 'E' || embargoCd == 'J')) {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult({
              id : 'embargoCd',
              type : 'text',
              name : 'embargoCd'
            }, false, 'Please use valid value for Embargo code field.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function afterConfigForMCO() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  FormManager.setValue('capInd', true);
  FormManager.readOnly('capInd');
  FormManager.readOnly('cmrOwner');

}

function afterConfigForPT() {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');

  FormManager.readOnly('custPrefLang');

  // Control Type Of Customer
  if ((custSubGrp != 'GOVRN' && custSubGrp != 'INTER' && custSubGrp != 'INTSO' && custSubGrp != 'XGOV' && custSubGrp != 'XBP') && (reqType != 'U')) {
    FormManager.setValue('crosSubTyp', '');
  }

  if ((role == 'REQUESTER') && reqType != 'C') {
    FormManager.readOnly('crosSubTyp');
  } else {
    FormManager.enable('crosSubTyp');
  }

  if (role == 'REQUESTER') {
    FormManager.readOnly('cmrNo');
  } else {
    FormManager.enable('cmrNo');
  }

}

function addAddressTypeValidator() {
  console.log("addAddressTypeValidator for 822, 838..........");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (cntry != SysLoc.PORTUGAL && cntry != SysLoc.SPAIN) {
          return new ValidationResult(null, true);
        }
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
          } /*
             * else if (zi01Cnt > 1 && cntry != SysLoc.SPAIN) { return new
             * ValidationResult(null, false, 'Only one Installing address is
             * allowed.'); } else if (zd01Cnt > 1 && cntry != SysLoc.SPAIN &&
             * FormManager.getActualValue('reqType') == 'C') { return new
             * ValidationResult(null, false, 'Only one Shipping address is
             * allowed for create requests.'); }
             */else if (zs02Cnt > 1) {
            return new ValidationResult(null, false, 'Only one EPL address is allowed.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addAddressFieldValidators() {
  // Street Address and PostBox cannot be both empty
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (cntry != SysLoc.PORTUGAL && cntry != SysLoc.SPAIN) {
          return new ValidationResult(null, true);
        }
        if (FormManager.getActualValue('addrTxt') == '' && FormManager.getActualValue('poBox') == '') {
          if (cntry == SysLoc.PORTUGAL && (FormManager.getActualValue('addrType') != 'ZP01' || FormManager.getActualValue('addrType') != 'ZS01')) {
            return new ValidationResult(null, false, 'Please fill-out Street Address.');
          } else {
            return new ValidationResult(null, false, 'Please fill-out either Street Address or PostBox.');
          }

        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (cntry != SysLoc.PORTUGAL) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var addrTxtCount = 0;
          var count = 0;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }

            if (record.poBox == '' && record.addrTxt == '') {
              if (type != 'ZS01' || type != 'ZP01') {
                addrTxtCount++;
              } else {
                count++;
              }
            }
            if (addrTxtCount > 0) {
              return new ValidationResult(null, false, 'Street Address is mandatory for Installing, Shipping And EPL.');
            }
            if (count > 0) {
              return new ValidationResult(null, false, 'Please fill-out either Street Address or PostBox.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');

  // Name Con't, Address Con't and Attention person
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var custGroup = FormManager.getActualValue('custGrp');
        if (cntry == SysLoc.SPAIN) {
          if (custGroup == 'CROSS') {
            var addrFldCnt = 0;
            if (FormManager.getActualValue('custNm2') != '') {
              addrFldCnt++;
            }
            if (FormManager.getActualValue('addrTxt2') != '') {
              addrFldCnt++;
            }
            if (FormManager.getActualValue('custNm4') != '') {
              addrFldCnt++;
            }
            if (addrFldCnt > 1) {
              return new ValidationResult(null, false, 'For Customer Name Con\'t, Address Con\'t, and Attention Person, only one can be filled.');
            }
          } else {
            if (FormManager.getActualValue('custNm2') != '' && FormManager.getActualValue('addrTxt2') != '') {
              return new ValidationResult(null, false, 'Customer Name Con\'t and Address Con\'t cannot be filled together');
            }
          }
        } else if (cntry == SysLoc.PORTUGAL) {
          if (FormManager.getActualValue('custNm2') != '' && FormManager.getActualValue('addrTxt2') != '' && FormManager.getActualValue('custNm4') != '') {
            return new ValidationResult(null, false, 'For Customer Name Con\'t, Address Con\'t, and Attention Person, only two can be filled.');
          }
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var postCd = FormManager.getActualValue('postCd');
        if (cntry != SysLoc.PORTUGAL) {
          return new ValidationResult(null, true);
        } else if (FormManager.getActualValue('landCntry') != 'PT') {
          return new ValidationResult(null, true);
        } else {
          var postCodeRegEx = /[\d]{4}\-[\d]{3}/;
          if (postCd != '' && postCodeRegEx.test(postCd)) {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult(null, false, 'Postal Code format error. Please refer to info bubble for details. ');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var poBox = FormManager.getActualValue('poBox');
        var poBoxRegEx = /^[0-9]*$/;
        if (poBox != '' && poBoxRegEx.test(poBox)) {
          return new ValidationResult(null, true);
        } else if (poBox != undefined && poBox != '') {
          return new ValidationResult(null, false, 'PO Box format error. Only digits are allowed.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // Defect 1734476: Commented because using Generic validation
  /*
   * FormManager.addFormValidator((function() { return { validate : function() {
   * var cntry = FormManager.getActualValue('cmrIssuingCntry'); var postCd =
   * FormManager.getActualValue('postCd'); if (cntry != SysLoc.SPAIN) { return
   * new ValidationResult(null, true); } else if
   * (FormManager.getActualValue('landCntry') == 'ES') { return new
   * ValidationResult(null, true); } else { if (postCd != '' && postCd.length !=
   * 5) { return new ValidationResult(null, false, 'Postal Code should be 5
   * characters long.'); } else { return new ValidationResult(null, true); } }
   * return new ValidationResult(null, true); } }; })(), null,
   * 'frmCMR_addressModal');
   */

  // PTES: Street Address + PO Box should be less than 25 characters
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrTxt = FormManager.getActualValue('addrTxt');
        var poBox = FormManager.getActualValue('poBox');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var val = addrTxt;
        if (poBox != '') {
          val += poBox;
          if (val.length > 24) {
            return new ValidationResult(null, false, 'Total computed length of Street Address and PO Box should be less than 25 characters.');
          }
        } else {
          if (val.length > 30 && cntry != SysLoc.SPAIN) {
            return new ValidationResult(null, false, 'Street Address should not exceed 30 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // Postal Code + City should not exceed 29
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (cntry != SysLoc.SPAIN) {
          return new ValidationResult(null, true);
        }
        var postal = FormManager.getActualValue('postCd');
        var city = FormManager.getActualValue('city1');
        var val = postal + city;
        if (postal != undefined && city != undefined && postal != '' && city != '' && val.length > 29) {
          return new ValidationResult(null, false, 'Postal Code + City exceeds 29.Please adjust field length.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

}

var _addrTypesForPTES = [ 'ZP01', 'ZS01', 'ZI01', 'ZD01', 'ZS02', 'ZP02' ];
var _addrTypeHandler = [];
var _ISUHandler = null;
var _CTCHandler = null;
var _SalesRepHandler = null;
var _LocNumHandler = null;
var _vatExemptHandler = null;
var _isicHandler = null;
var _noISRLogicPT = new Set([ 'INTER', 'INTSO', 'ININV', 'IBMEM' ]);
var _noISRLogicES = new Set([ 'INTER', 'INTSO', 'XINTR', 'XINSO' ]);
function addHandlersForPTES() {
  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValues(value);
    });
  }

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setSalesRepValues(value);
      setEnterpriseValues(value);
    });
  }

  if (_SalesRepHandler == null) {
    _SalesRepHandler = dojo.connect(FormManager.getField('repTeamMemberNo'), 'onChange', function(value) {
      if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.PORTUGAL) {
        setSBOAndEBO();
      }
    });
  }

  if (_LocNumHandler == null) {
    _LocNumHandler = dojo.connect(FormManager.getField('locationNumber'), 'onChange', function(value) {
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      var custGroup = FormManager.getActualValue('custGrp');
      if (cntry == SysLoc.SPAIN && custGroup == 'CROSS') {
        setDPCEBObasedOnCntry();
      } else if (cntry == SysLoc.SPAIN) {
        setSBOAndEBO();
      } else {
        var req = FormManager.getActualValue('reqType').toUpperCase();
        if (req == 'U') {
          setSBOAndEBO();
          setDPCEBObasedOnCntry();
        }
      }
    });
  }

  for (var i = 0; i < _addrTypesForPTES.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForPTES[i]), 'onClick', function(value) {
        disableAddrFieldsPTES();
      });
    }
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      autoSetVatExemptFrPriCust();
      setVatValidatorPTES();
    });
  }

  if (_isicHandler == null) {
    _isicHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setISUCTCOnISIC();
    });
  }

}

function setClientTierValues(value) {
  value = FormManager.getActualValue('isuCd');
  var tierValues = null;
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.PORTUGAL) {
    if (value == '32') {
      tierValues = [ 'B', 'S', 'Z', 'M', 'N' ];
    } else if (value == '21' || value == '60') {
      tierValues = [ '7' ];
    } else if (value == '34') {
      tierValues = [ 'V', '6', 'A', 'Z' ];
    }
  } else if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.SPAIN) {
    if (value == '34') {
      tierValues = [ '6', 'A', 'V', 'Z' ];
    } else if (value == '32') {
      tierValues = [ 'B', 'S', 'T', 'N', 'Z', 'M' ];
    } else if (value == '21' || value == '5B' || value == '04' || value == '3T' || value == '60') {
      tierValues = [ '7' ];
    }
  }
  if (tierValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), tierValues);
    if (tierValues.length == 1) {
      FormManager.setValue('clientTier', tierValues[0]);
      setSalesRepValues();
      setEnterpriseValues();
    }
  } else {
    FormManager.resetDropdownValues(FormManager.getField('clientTier'));
  }

}

function setSalesRepValues(clientTier) {
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  if (cntry == SysLoc.PORTUGAL && _noISRLogicPT.has(custSubGroup)) {
    return;
  }
  if (cntry == SysLoc.SPAIN && _noISRLogicES.has(custSubGroup)) {
    return;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  clientTier = FormManager.getActualValue('clientTier');
  var salesReps = [];
  if (isuCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
      ISU : '%' + isuCd + clientTier + '%'
    };
    var results = cmr.query('GET.SRLIST.BYISU', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        salesReps.push(results[i].ret1);
      }
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesReps);
      if (salesReps.length == 1) {
        FormManager.setValue('repTeamMemberNo', salesReps[0]);
        setSBOAndEBO();
      }
    }
  }
}

function setEnterpriseValues(clientTier) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custGroup = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  clientTier = FormManager.getActualValue('clientTier');

  // For Spain, domestic with 32B, 32S, 32T & 217 ISUs, set enterprise based on
  // LocNo
  var isuCtc = isuCd + clientTier;
  if (custGroup != 'CROSS') {
    if (cntry == SysLoc.SPAIN && (isuCtc == '32B' || isuCtc == '32N' || isuCtc == '32S' || isuCtc == '32T' || isuCtc == '217')) {
      // FormManager.readOnly('enterprise');
      setSBOAndEBO();
      return;
    }
  }

  var enterprises = [];
  if (isuCd != '' && clientTier != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCd + clientTier + '%'
    };
    var results = cmr.query('GET.ENTLIST.BYISU', qParams);
    if (results != null) {
      FormManager.resetDropdownValues(FormManager.getField('enterprise'));
      for (var i = 0; i < results.length; i++) {
        enterprises.push(results[i].ret1);
      }
      if (enterprises != null) {
        FormManager.limitDropdownValues(FormManager.getField('enterprise'), enterprises);
        if (enterprises.length == 1) {
          FormManager.setValue('enterprise', enterprises[0]);
        }
      }
    }
  }
}

function setSBOAndEBO() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == SysLoc.PORTUGAL) {
    // Portugal: SBO - based on SalesRep selected
    var custSubGroup = FormManager.getActualValue('custSubGrp');
    var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
    if (_noISRLogicPT.has(custSubGroup)) {
      return;
    }
    if (repTeamMemberNo != '') {
      var qParams = {
        ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
        REP_TEAM_CD : repTeamMemberNo
      };
      var result = cmr.query('GET.SBO.BYSR', qParams);
      var salesBoCd = result.ret1;
      FormManager.setValue('salesBusOffCd', salesBoCd);
    } else {
      FormManager.setValue('salesBusOffCd', '');
    }
  } else if (cntry == SysLoc.SPAIN) {
    // Spain Domestic: SBO and EBO - based on Location Number
    if (FormManager.getActualValue('reqType') != 'C') {
      return;
    }
    if (FormManager.getActualValue('custGrp') == 'CROSS') {
      return;
    }
    var custSubGroup = FormManager.getActualValue('custSubGrp');
    var locationNumber = FormManager.getActualValue('locationNumber');
    var isuCd = FormManager.getActualValue('isuCd');
    var clientTier = FormManager.getActualValue('clientTier');
    var noSBOLogicES = new Set([ 'IGSGS', 'INTER', 'INTSO', 'XCRO', 'XIGS', 'GOVIG', 'THDIG', 'XINTR', 'XINSO' ]);

    if (locationNumber != '') {
      var isuCtc = isuCd + clientTier;
      var qParams = {
        ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
        REP_TEAM_CD : locationNumber,
        CLIENT_TIER : '%' + isuCtc + '%'
      };
      var result = cmr.query('GET.SBO.BYSR_ISUCTC', qParams);
      var salesBoCd = '';
      var eBo = '';
      var ent = '';
      if (Object.keys(result).length != 0) {
        salesBoCd = result.ret1;
        eBo = result.ret2;
        ent = result.ret3;
      } else {
        result = cmr.query('GET.SBO.BYSR_ES', {
          ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
          REP_TEAM_CD : locationNumber
        });
        salesBoCd = result.ret1;
        eBo = result.ret2;
        ent = result.ret3;
      }
      if (FormManager.getActualValue('reqType') == 'C') {
        FormManager.setValue('engineeringBo', eBo);
        if (custSubGroup != undefined && custSubGroup != '' && !noSBOLogicES.has(custSubGroup) && 'BUSPR' != custSubGroup) {
          FormManager.setValue('salesBusOffCd', salesBoCd);
        }
      }
      // For Spain, domestic with 32 & 21 ISU, set enterprise based on LocNo
      if (isuCtc == '32B' || isuCtc == '32S' || isuCtc == '32T' || isuCtc == '217' || isuCtc == '32N') {
        if (ent == undefined) {
          FormManager.setValue('enterprise', '');
        } else {
          FormManager.resetDropdownValues(FormManager.getField('enterprise'));
          FormManager.setValue('enterprise', ent);
        }
      }
    }
  }
}

function disableAddrFieldsPTES() {
  var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
  var custType = FormManager.getActualValue('custGrp');
  var addrType = FormManager.getActualValue('addrType');
  var checkImportIndc = getImportedIndcForPT();

  // Sequence Number - enable for additional shipping
  if (cntryCd == SysLoc.SPAIN && cmr.currentRequestType == 'U' && checkImportIndc != 'Y' && addrType == 'ZD01') {
    FormManager.enable('prefSeqNo');
  } else {
    FormManager.setValue('prefSeqNo', '');
    FormManager.readOnly('prefSeqNo');
  }

  if (custType != 'CROSS' && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }

  if (addrType != 'ZS01' && addrType != 'ZD01') {
    FormManager.readOnly('custPhone');
    FormManager.setValue('custPhone', '');
  } else {
    FormManager.enable('custPhone');
  }

  if (addrType != 'ZS01' && addrType != 'ZP01') {
    FormManager.readOnly('poBox');
    FormManager.setValue('poBox', '');
  } else {
    FormManager.enable('poBox');
  }

  FormManager.setValue('dept', '');
  FormManager.readOnly('dept');

  // Phone: Create-billing address only, Update-also shipping address for ES
  if (cntryCd == SysLoc.SPAIN && cmr.currentRequestType == 'U' && addrType == 'ZD01') {
    FormManager.enable('custPhone');
  }

}

function setVatValidatorPTES() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    reqParam = {
      REQ_ID : FormManager.getActualValue('reqId'),
    };
    var results = cmr.query('ADDR.GET.ZS01LANDCNTRY.BY_REQID', reqParam);
    var zs01LandCntry = results.ret2;
    if (!dijit.byId('vatExempt').get('checked') && zs01LandCntry != 'US') {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
    }
  }
}

function validateMCOCopy(addrType, arrayOfTargetTypes) {
  return null;
}

function setCollectionCode() {
  var roleCheck = false;
  var reqCheck = false;
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType').toUpperCase();
  if (role != null && role.length > 0) {
    roleCheck = true;
  }
  if (reqType != null && reqType.length > 0) {
    reqCheck = true;
  }
  if (roleCheck && reqCheck) {
    if (role == 'REQUESTER' && reqType == 'U' && cntry == SysLoc.SPAIN && viewOnlyPage != 'true') {
      FormManager.enable('collectionCd');
    }
  }
  if (reqCheck) {
    if (reqType == 'U' && cntry == SysLoc.PORTUGAL && viewOnlyPage != 'true') {
      FormManager.enable('collectionCd');
    }
  }
}

function setDistrictCode() {
  var reqType = FormManager.getActualValue('reqType').toUpperCase();
  if (reqType == 'U') {
    if (_districtCdHandler == null) {
      _districtCdHandler = dojo.connect(FormManager.getField('collectionCd'), 'onChange', function(value) {
        var collectionCd = FormManager.getActualValue('collectionCd');
        if (collectionCd != '') {
          var qParams = {
            CMR_ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
            CD : collectionCd
          };
          var result = cmr.query('DATA.GET.DISTRICT_CD', qParams);
          var districtCd = result.ret2;
          FormManager.setValue('territoryCd', districtCd);
        } else {
          FormManager.setValue('territoryCd', '');
        }
      });
    }
    if (_districtCdHandler && _districtCdHandler[0]) {
      _districtCdHandler[0].onChange();
    }
  }
}

function changeAbbrevNmLocnSpain(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> onSavingAddress ");
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if ((finalSave || force || cmr.addressMode) && saving) {
    var copyTypes = document.getElementsByName('copyTypes');
    var copyingToA = false;
    if (copyTypes != null && copyTypes.length > 0) {
      copyTypes.forEach(function(input, i) {
        if (input.value == 'ZI01' && input.checked) {
          copyingToA = true;
        }
      });
    }
    var addrType = FormManager.getActualValue('addrType');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if ((addrType == 'ZI01' || copyingToA) && reqType == 'C' && role == 'REQUESTER') {
      var abbrevNm = FormManager.getActualValue('custNm1');
      var abbrevLocn = FormManager.getActualValue('city1');
      if ([ 'INTER', 'INTSO' ].includes(custSubGrp) && !abbrevNm.includes('IBM/')) {
        abbrevNm = "IBM/".concat(abbrevNm);
      } else if ([ 'IGSGS', 'GOVIG', 'XIGS', 'THDIG' ].includes(custSubGrp) && !abbrevNm.includes('IGS')) {
        abbrevNm = abbrevNm.length >= 18 ? abbrevNm.substring(0, 18).concat(' IGS') : abbrevNm.concat(' IGS');
      }
      if (abbrevNm && abbrevNm.length > 22) {
        abbrevNm = abbrevNm.substring(0, 22);
      }
      if (abbrevLocn && abbrevLocn.length > 12) {
        abbrevLocn = abbrevLocn.substring(0, 12);
      }
      FormManager.setValue('abbrevNm', abbrevNm);
      if (custSubGrp != 'SOFTL') {
        FormManager.setValue('abbrevLocn', abbrevLocn);
      }
    }

    if (addrType == 'ZS01') {
      // ES - Local: set locNo based from postCd
      var postCd = FormManager.getActualValue('postCd');
      changeLocationNoByPostCd(cntry, postCd, cmr.currentRequestType);
    }

  }
}

function changeAbbrevNmLocnPortugal(cntry, addressMode, saving, finalSave, force) {
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
    var reqType = FormManager.getActualValue('reqType');
    var addrType = FormManager.getActualValue('addrType');
    if (reqType == 'C' && addrType == 'ZS01' || copyingToA || reqType == 'U') {
      // generate Abbreviated Name/Location
      var role = FormManager.getActualValue('userRole').toUpperCase();
      var abbrevNm = FormManager.getActualValue('custNm1');
      var abbrevLocn = FormManager.getActualValue('city1');

      if (role == 'REQUESTER') {
        if (abbrevNm && abbrevNm.length > 22) {
          abbrevNm = abbrevNm.substring(0, 22);
        }
        if (abbrevLocn && abbrevLocn.length > 12) {
          abbrevLocn = abbrevLocn.substring(0, 12);
        }
        FormManager.setValue('abbrevNm', abbrevNm);
        var subCustGrp = FormManager.getActualValue('custSubGrp');
        if (subCustGrp != 'SOFTL') {
          FormManager.setValue('abbrevLocn', abbrevLocn);
        }
      }
    }
    if (addrType == 'ZS01') {
      // ES - Local: set locNo based from postCd
      var postCd = FormManager.getActualValue('postCd');
      changeLocationNoByPostCd(cntry, postCd, cmr.currentRequestType);
    }
  }
}

function setLocationNoByPostCd() {
  var custGroup = FormManager.getActualValue('custGrp');
  var reqType = FormManager.getActualValue('reqType');

  if (reqType == 'C' && custGroup != 'CROSS') {
    var qParams = {
      REQ_ID : FormManager.getActualValue('reqId')
    };
    var result = cmr.query('GET.ZS01POSTCD.BY_REQID', qParams);
    // Mukesh :Missing undefined check
    if (result != null && result != '' && result.ret1 != undefined) {
      changeLocationNoByPostCd(FormManager.getActualValue('cmrIssuingCntry'), result.ret1, reqType);
    }
  }
}

function changeLocationNoByPostCd(cntry, postCd, reqType) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custGroup = FormManager.getActualValue('custGrp');
  FormManager.enable('locationNumber');
  if (postCd != '') {
    if (cntry == SysLoc.SPAIN && custGroup != 'CROSS') {
      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry,
        POSTCD : '%' + postCd.substring(0, 2) + '%'
      };

      var locNos = [];
      var results = cmr.query('GET.LOCNO.BYPOSTCD', qParams);
      if (results != null && results != '') {
        for (var i = 0; i < results.length; i++) {
          locNos.push(results[i].ret1);
        }
        FormManager.limitDropdownValues(FormManager.getField('locationNumber'), locNos);
        if (locNos.length == 1) {
          FormManager.setValue('locationNumber', locNos[0]);
          FormManager.readOnly('locationNumber');
        }
      }
    }
  }
}

function TaxCdOnPostalChange() {

  dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
    var req = FormManager.getActualValue('reqType').toUpperCase();
    if (req == 'C') {
      setTaxCdByPostCd();
    }
  });
}

function setTaxCdByPostCd() {
  var reqType = FormManager.getActualValue('reqType');
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var addrType = FormManager.getActualValue('addrType');
  if (reqType != 'C') {
    return;
  }
  var postcd = FormManager.getActualValue('postCd');
  if (postcd != '' && postcd != undefined) {
    if (addrType == 'ZS01') {
      postcd = postcd.substring(0, 2);
    }
  } else {
    var qParams = {
      REQ_ID : FormManager.getActualValue('reqId')
    };
    var result = cmr.query('GET.ZS01POSTCD.BY_REQID', qParams);
    if (result != null && result != '' && result.ret1 != undefined) {
      postcd = result.ret1.substring(0, 2);
    }
  }
  console.log(postcd);
  var postalCdList = new Set([ '35', '38', '51', '52' ]);
  if (postalCdList.has(postcd)) {
    if (reqType == 'C' && (custSubGroup == 'GOVRN' || custSubGroup == 'GOVIG')) {
      FormManager.setValue('specialTaxCd', '18');
    } else if (reqType == 'C' && (custSubGroup != 'GOVRN' && custSubGroup != 'GOVIG' && custSubGroup != 'INTSO' && custSubGroup != 'INTER' && custSubGroup != 'XINSO' && custSubGroup != 'XINTR')) {
      FormManager.setValue('specialTaxCd', '23');
    }
  } else {
    var qParams = {
      CUST_TYP : FormManager.getActualValue('custSubGroup')
    };
    var result = cmr.query('GET.TAXCD.BY_CUSTSUBGRP', qParams);
    if (result != null && result != '' && result.ret1 != undefined) {
      FormManager.setValue('specialTaxCd', result.ret1);
    }

  }
}
function abbrvNmProcessorMandatory() {
  abbrvNmProcessorInterval = setInterval(function() {
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

    if (viewOnlyPage != 'true') {
      if (role != 'REQUESTER' && FormManager.getActualValue('reqType') != 'U') {
        FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'AbbrevName' ], 'MAIN_CUST_TAB');
      } else {
        FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
        if(FormManager.getActualValue('reqType') != 'U'){
          FormManager.readOnly('abbrevNm');
          FormManager.readOnly('abbrevLocn');
        }
      }
    }
  }, 1000);
}

function abbrvNmProcessorMandatoryOnChange() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  dojo.connect(FormManager.getField('abbrevNm'), 'onChange', function(value) {
    if (role != 'REQUESTER' && FormManager.getActualValue('reqType') != 'U') {
      FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'AbbrevName' ], 'MAIN_CUST_TAB');
    } else {
      if(FormManager.getActualValue('reqType') != 'U'){
        FormManager.readOnly('abbrevNm');
        FormManager.readOnly('abbrevLocn');
      }
      FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    }
  });
}

function setDistrictCodeMapping() {
  var collectionCd = FormManager.getActualValue('collectionCd');
  if (collectionCd != '') {
    var qParams = {
      CMR_ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
      CD : collectionCd
    };
    var result = cmr.query('DATA.GET.DISTRICT_CD', qParams);
    var districtCd = result.ret2;
    FormManager.setValue('territoryCd', districtCd);
  } else {
    FormManager.setValue('territoryCd', '');
  }
}

function addEnterpriseValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var entNo = FormManager.getActualValue('enterprise');
        if (entNo != '' && entNo.length != 6) {
          return new ValidationResult(null, false, 'Enterprise Number should have exactly 6 digits.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function setFieldMandatoryForProcessorPT() {
  var reqType = FormManager.getActualValue('reqType');

  if (typeof (_pagemodel) != 'undefined') {
    if (reqType == 'C' && _pagemodel.userRole == GEOHandler.ROLE_PROCESSOR) {
      checkAndAddValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ]);
      checkAndAddValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ]);
      checkAndAddValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ]);
      checkAndAddValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ]);
    }
  }
}

function disableVatIfNotEmptyPortugal() {
  var vat = FormManager.getActualValue('vat');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    if (vat != null && vat.length > 2) {
      var checkVatPrefix = vat.substring(0, 2);
      var checkVatLenReq = vat.substring(2, vat.length);
      if (checkVatPrefix.toUpperCase() == 'PT') {
        if (checkVatLenReq.length < 9) {
          FormManager.enable('vat');
        } else if (checkVatLenReq.length == 9 && checkVatLenReq.match(/^[0-9]+$/) != null) {
          var zs01Cntry = landCntry;

          var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
            REQID : FormManager.getActualValue('reqId')
          });
          if (ret && ret.ret1 && ret.ret1 != '') {
            zs01Cntry = ret.ret1;
          }
          console.log('ZS01 VAT Country: ' + zs01Cntry);

          var result = cmr.validateVAT(zs01Cntry, vat);
          if (result && !result.success) {
            FormManager.enable('vat');
          } else {
            FormManager.setValue('vat', checkVatPrefix.toUpperCase() + checkVatLenReq);
            FormManager.readOnly('vat');
          }
        }
      } else {
        FormManager.enable('vat');
      }
    } else {
      FormManager.enable('vat');
    }
  }
  afterConfigPT();
}

function disableVatIfNotEmptySpain() {
  var vat = FormManager.getActualValue('vat');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (role == 'REQUESTER' && FormManager.getActualValue('reqType') == 'U') {
    FormManager.enable('vat');
    return;
  }

  if (vat != null && vat.length > 2) {
    var checkVatPrefix = vat.substring(0, 2);
    var checkVatLenReq = vat.substring(2, vat.length);
    if (checkVatPrefix.toUpperCase() == 'ES') {
      if (checkVatLenReq.length < 9) {
        FormManager.enable('vat');
      } else if (checkVatLenReq.length == 9) {
        FormManager.setValue('vat', checkVatPrefix.toUpperCase() + checkVatLenReq);
        // FormManager.readOnly('vat');
      }
    } else {
      FormManager.enable('vat');
    }
  } else {
    FormManager.enable('vat');
  }
}

function forceLockScenariosPortugal() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  var fieldsToDisable = new Array();
  /*
   * FormManager.enable('isicCd'); FormManager.enable('isuCd');
   * FormManager.enable('clientTier'); FormManager.enable('specialTaxCd');
   * FormManager.enable('repTeamMemberNo');
   */

  if (custSubGroup == 'COMME') {
    fieldsToDisable.push('cmrOwner');
    fieldsToDisable.push('collectionCd');

  } else if (custSubGroup == 'BUSPR') {
    fieldsToDisable.push('isuCd');
    fieldsToDisable.push('clientTier');

  } else if (custSubGroup == 'INTSO') {
    fieldsToDisable.push('isicCd');
    fieldsToDisable.push('specialTaxCd');
    fieldsToDisable.push('isuCd');
    fieldsToDisable.push('clientTier');

  } else if (custSubGroup == 'INTER') {
    fieldsToDisable.push('isicCd');
    fieldsToDisable.push('specialTaxCd');
    fieldsToDisable.push('isuCd');
    fieldsToDisable.push('clientTier');

  } else if (custSubGroup == 'PRICU') {
    fieldsToDisable.push('isicCd');
    fieldsToDisable.push('isuCd');
    fieldsToDisable.push('clientTier');
    if (repTeamMemberNo != null && repTeamMemberNo != undefined && repTeamMemberNo != '') {
      fieldsToDisable.push('repTeamMemberNo');
    }
  } else if (custSubGroup == 'IBMEM') {
    fieldsToDisable.push('isicCd');
    fieldsToDisable.push('isuCd');
    fieldsToDisable.push('clientTier');
    fieldsToDisable.push('repTeamMemberNo');

  } else if (custSubGroup == 'XBP') {
    fieldsToDisable.push('isuCd');
    fieldsToDisable.push('clientTier');
  }

  // common to all scenarios
  if (role == 'REQUESTER') {
    fieldsToDisable.push('abbrevNm');
    fieldsToDisable.push('abbrevLocn');
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);

  }
  fieldsToDisable.push('cmrOwner');
  fieldsToDisable.push('collectionCd');
  fieldsToDisable.push('subIndustryCd');
  fieldsToDisable.push('salesBusOffCd');
  fieldsToDisable.push('modeOfPayment');

  for (var i = 0; i < fieldsToDisable.length; i++) {
    FormManager.readOnly(fieldsToDisable[i]);
  }
}

function fieldDisablerRepeater(fieldsToDisable) {
  fieldDisablerRepeaterInterval = setInterval(function() {
    for (var i = 0; i < fieldsToDisable.length; i++) {
      FormManager.readOnly(fieldsToDisable[i]);
    }
  }, 1000);
}

function crossborderScenariosAbbrvLoc() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  if (crossborderScenariosInterval != null) {
    clearInterval(crossborderScenarios);
  }
  crossborderScenariosInterval = setInterval(function() {
    var custGroup = FormManager.getActualValue('custGrp');

    if (custGroup != null && custGroup.length > 0) {
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
        clearInterval(crossborderScenariosInterval);
      } else {
        clearInterval(crossborderScenariosInterval);
      }
    }
  }, 1000);
}

function crossborderScenariosAbbrvLocOnChange() {
  dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
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
  });
}

function valueRechecker(fieldId, value) {
  if (valCheckerInterval != null) {
    clearInterval(valCheckerInterval);
  }
  valCheckerInterval = new Object();
  if ((fieldId != null && fieldId.length > 0) && (value != null && value.length > 0)) {
    valCheckerInterval = setInterval(function() {
      FormManager.setValue(fieldId, value);
    }, 1000);
  }
}

function forceLockScenariosSpain() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  var fieldsToDisable = new Array();
  FormManager.enable('isicCd');
  FormManager.enable('isuCd');
  FormManager.enable('clientTier');
  FormManager.enable('specialTaxCd');
  FormManager.enable('repTeamMemberNo');
  fieldsToDisable.push('cmrNo');
  fieldsToDisable.push('soeReqNo');

  if (custSubGroup == 'COMME') {
    fieldsToDisable.push('mailingCondition');

  } else if (custSubGroup == 'BUSPR') {
    fieldsToDisable.push('custClass');
  } else if (custSubGroup == 'INTER') {
    fieldsToDisable.push('isicCd');
    fieldsToDisable.push('specialTaxCd');
  } else if (custSubGroup == 'INTSO') {
    fieldsToDisable.push('isicCd');
    fieldsToDisable.push('specialTaxCd');
  } else if (custSubGroup == 'PRICU') {
    fieldsToDisable.push('isicCd');
    fieldsToDisable.push('vat');
  } else if (custSubGroup == 'XBP') {
    fieldsToDisable.push('custClass');
  } else if (custSubGroup == 'XINSO') {
    fieldsToDisable.push('isicCd');
    fieldsToDisable.push('specialTaxCd');
  } else if (custSubGroup == 'XINTR') {
    fieldsToDisable.push('isicCd');
    fieldsToDisable.push('specialTaxCd');
  }

  // common to all scenarios
  if (role == 'REQUESTER') {
    fieldsToDisable.push('abbrevNm');
    fieldsToDisable.push('abbrevLocn');
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);
    fieldsToDisable.push('isuCd');
    fieldsToDisable.push('clientTier');
    fieldsToDisable.push('inacCd');
    fieldsToDisable.push('enterprise');
    fieldsToDisable.push('buyingGroupId');
    fieldsToDisable.push('globalBuyingGroupId');
    fieldsToDisable.push('covId');
    fieldsToDisable.push('geoLocationCode');
    fieldsToDisable.push('dunsNo');
    if (custSubGroup != 'XBP' && custSubGroup != 'BUSPR') {
      fieldsToDisable.push('ppsceid');
      fieldsToDisable.push('salesBusOffCd');
    }
    fieldsToDisable.push('repTeamMemberNo');
  } else {
    FormManager.enable('isuCd');
    FormManager.enable('clientTier');
    FormManager.enable('inacCd');
    FormManager.enable('enterprise');
    FormManager.enable('dunsNo');
    if (custSubGroup == 'XBP' || custSubGroup == 'BUSPR') {
      FormManager.enable('ppsceid');
    } else {
      fieldsToDisable.push('ppsceid');
    }
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('salesBusOffCd');
  }
  fieldsToDisable.push('cmrOwner');
  fieldsToDisable.push('collectionCd');
  fieldsToDisable.push('subIndustryCd');
  fieldsToDisable.push('modeOfPayment');
  // fieldsToDisable.push('locationNumber');

  for (var i = 0; i < fieldsToDisable.length; i++) {
    FormManager.readOnly(fieldsToDisable[i]);
  }
}

function setAbbrvLocVatBasedOnCountry() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var vatNo = FormManager.getActualValue('vat');
  var isEU = false;
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var results = cmr.query('ADDR.GET.ZS01LANDCNTRY.BY_REQID', reqParam);
  var landCntry = results.ret1;
  var cntry = new Array();
  cntry.push('AUSTRIA');
  cntry.push('BELGIUM');
  cntry.push('BULGARIA');
  cntry.push('CROATIA');
  cntry.push('CYPRUS');
  cntry.push('CZECH REPUBLIC');
  cntry.push('GERMANY');
  cntry.push('DENMARK');
  cntry.push('ESTONIA');
  cntry.push('GREECE');
  cntry.push('FINLAND');
  cntry.push('FRANCE');
  cntry.push('UNITED KINGDOM');
  cntry.push('HUNGARY');
  cntry.push('IRELAND');
  cntry.push('ITALY');
  cntry.push('LITHUANIA');
  cntry.push('LUXEMBURG');
  cntry.push('LATVIA');
  cntry.push('MALTA');
  cntry.push('NETHERLANDS');
  cntry.push('POLAND');
  cntry.push('PORTUGAL');
  cntry.push('ROMANIA');
  cntry.push('SWEDEN');
  cntry.push('SLOVENIA');
  cntry.push('SLOVAKIA');

  for (var i = 0; i < cntry.length; i++) {
    if (landCntry != null && landCntry.toUpperCase() == cntry[i]) {
      isEU = true;

      if (vatNo != null && vatNo.length > 0) {
        if (vatNo.length > 12) {
          vatNo = vatNo.substring(0, 12);
        }
        var subCustGrp = FormManager.getActualValue('custSubGrp');
        if (subCustGrp != 'SOFTL') {
          FormManager.setValue('abbrevLocn', vatNo.substring(2, vatNo.length));
        }
      } else {
        FormManager.setValue('vat', 'CEE000000');
      }
      break;
    }
  }

  if (!(isEU)) {
    if (vatNo != null && vatNo.length > 0) {
      var subCustGrp = FormManager.getActualValue('custSubGrp');
      if (subCustGrp != 'SOFTL') {
        FormManager.setValue('abbrevLocn', vatNo.substring(2, vatNo.length));
      }
    } else {
      var results = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', reqParam);
      var abbrevLocn = results.ret1;
      if (abbrevLocn != null && abbrevLocn.length > 12) {
        abbrevLocn = abbrevLocn.substring(0, 12);
      }
      if (abbrevLocn != null) {
        var subCustGrp = FormManager.getActualValue('custSubGrp');
        if (subCustGrp != 'SOFTL') {
          FormManager.setValue('abbrevLocn', abbrevLocn);
        }
      }
      FormManager.setValue('vat', 'A00000000');
    }
  }
}

function setLocationNumberBaseOnCntry() {
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var isEU = false;
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var custGroup = FormManager.getActualValue('custGrp');
  var reqType = FormManager.getActualValue('reqType');

  if (reqType == 'C' && custGroup == 'CROSS') {
    var results = cmr.query('ADDR.GET.ZS01LANDCNTRY.BY_REQID', reqParam);
    var landCntry = results.ret1;
    var cntry = new Array();
    cntry.push('ANDORRA');
    cntry.push('AUSTRIA');
    cntry.push('BELGIUM');
    cntry.push('BULGARIA');
    cntry.push('CROATIA');
    cntry.push('CYPRUS');
    cntry.push('CZECH REPUBLIC');
    cntry.push('GERMANY');
    cntry.push('DENMARK');
    cntry.push('ESTONIA');
    cntry.push('GREECE');
    cntry.push('FINLAND');
    cntry.push('FRANCE');
    cntry.push('UNITED KINGDOM');
    cntry.push('HUNGARY');
    cntry.push('IRELAND');
    cntry.push('ITALY');
    cntry.push('LITHUANIA');
    cntry.push('LUXEMBURG');
    cntry.push('LATVIA');
    cntry.push('MALTA');
    cntry.push('NETHERLANDS');
    cntry.push('POLAND');
    cntry.push('PORTUGAL');
    cntry.push('ROMANIA');
    cntry.push('SWEDEN');
    cntry.push('SLOVENIA');
    cntry.push('SLOVAKIA');

    var cntryCode = new Array();
    cntryCode.push('AD000');
    cntryCode.push('AT999');
    cntryCode.push('BE999');
    cntryCode.push('BG999');
    cntryCode.push('HR999');
    cntryCode.push('CY999');
    cntryCode.push('CZ999');
    cntryCode.push('DE999');
    cntryCode.push('DK999');
    cntryCode.push('EE999');
    cntryCode.push('EL999');
    cntryCode.push('FI999');
    cntryCode.push('FR999');
    cntryCode.push('GB999');
    cntryCode.push('HU999');
    cntryCode.push('IE999');
    cntryCode.push('IT999');
    cntryCode.push('LT999');
    cntryCode.push('LU999');
    cntryCode.push('LV999');
    cntryCode.push('MT999');
    cntryCode.push('NL999');
    cntryCode.push('PL999');
    cntryCode.push('PT999');
    cntryCode.push('RO999');
    cntryCode.push('SE999');
    cntryCode.push('SI999');
    cntryCode.push('SK999');

    var cntryMap = new Array();
    for (var i = 0; i < cntry.length; i++) {
      cntryMap[cntry[i]] = cntryCode[i];
    }

    for (var i = 0; i < cntry.length; i++) {
      if (landCntry != null && landCntry.toUpperCase() == cntry[i]) {
        isEU = true;
        setLocNo(cntryMap[cntry[i]]);
        break;
      }
    }
    if (!(isEU)) {
      var results = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', reqParam);
      var landedCntry = results.ret1;
      if (landedCntry == 'SG') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'BA') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'GE') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'IL') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'CO') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'CR') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'SA') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'MA') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'SO') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'MU') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'SS') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'BI') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'ZA') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'AE') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'NA') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'ML') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'GU') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'CA') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry == 'AW') {
        setLocNo(landedCntry + '999');
      } else if (landedCntry != null && landedCntry != 'ES') {
        setLocNo(landedCntry + '000');
      }
    }
  }
}

function hideCustPhoneonSummary() {
  setInterval(function() {
    if (openAddressDetails.addrType != 'ZS01') {
      cmr.hideNode('custPhone_view');
      $('label[for="custPhone_view"]').hide();
    } else {
      cmr.showNode('custPhone_view');
      $('label[for="custPhone_view"]').show();
    }
  }, 1000);
}

function setAbbrvPortugal() {
  var reqType = FormManager.getActualValue('reqType');
  var reqId = FormManager.getActualValue('reqId');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (reqType == 'C' && role == 'REQUESTER') {
    var addrType = 'ZS01';
    if (reqId != null) {
      reqParam = {
        REQ_ID : reqId,
        ADDR_TYPE : addrType
      };
    }
    var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_MCO', reqParam);
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
      var subCustGrp = FormManager.getActualValue('custSubGrp');
      if (subCustGrp != 'SOFTL') {
        FormManager.setValue('abbrevLocn', abbrevLocn);
      }
    }
    if (abbrvNm != null) {
      FormManager.setValue('abbrevNm', abbrvNm);
    }
  }
}

function setDPCEBObasedOnCntry() {
  var locNum = FormManager.getActualValue('locationNumber');
  var cntryCode = new Array();
  cntryCode.push('AD000');
  cntryCode.push('AT999');
  cntryCode.push('BE999');
  cntryCode.push('BG999');
  cntryCode.push('HR999');
  cntryCode.push('CY999');
  cntryCode.push('CZ999');
  cntryCode.push('DE999');
  cntryCode.push('DK999');
  cntryCode.push('EE999');
  cntryCode.push('EL999');
  cntryCode.push('FI999');
  cntryCode.push('FR999');
  cntryCode.push('GB999');
  cntryCode.push('HU999');
  cntryCode.push('IE999');
  cntryCode.push('IT999');
  cntryCode.push('LT999');
  cntryCode.push('LU999');
  cntryCode.push('LV999');
  cntryCode.push('MT999');
  cntryCode.push('NL999');
  cntryCode.push('PL999');
  cntryCode.push('PT999');
  cntryCode.push('RO999');
  cntryCode.push('SE999');
  cntryCode.push('SI999');
  cntryCode.push('SK999');
  cntryCode.push('EX000');

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  for (var i = 0; i < cntryCode.length; i++) {
    if (locNum != null && locNum == cntryCode[i] && (custSubGrp != 'XINSO' && custSubGrp != 'XINTR') && custSubGrp != 'BUSPR') {
      FormManager.setValue('engineeringBo', '8628628');
      FormManager.setValue('salesBusOffCd', '4515140');
    } else if (custSubGrp == 'XCRO' || custSubGrp == 'XBP' || custSubGrp == 'XIGS' || custSubGrp == 'XINSO' || custSubGrp == 'XINTR') {
      FormManager.setValue('engineeringBo', '8628628');
    }
  }
}

function disableVATforViewOnly() {
  var interval = new Object();
  interval = setInterval(function() {
    var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
    if (viewOnlyPage == 'true') {
      FormManager.readOnly('vat');
    } else {
      clearInterval(interval);
    }
  }, 1000);
}

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnlyPage == 'true' && cmrIssuingCntry == SysLoc.PORTUGAL) {
    $('label[for="landCntry_view"]').text('Country (Landed):');
    $('label[for="custNm4_view"]').text('Attention Person:');
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="custPhone_view"]').text('Phone #:');
    $('label[for="custNm1_view"]').text('Customer Name:');
    $('label[for="custNm2_view"]').text('Customer Name Con' + '\'' + 't:');
  }

  if (viewOnlyPage == 'true' && cmrIssuingCntry == SysLoc.SPAIN) {
    $('label[for="landCntry_view"]').text('Country (Landed):');
    $('label[for="custNm4_view"]').text('Att. Person:');
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="custPhone_view"]').text('Phone #:');
    $('label[for="custNm1_view"]').text('Customer Name:');
    $('label[for="custNm2_view"]').text('Customer Name Con' + '\'' + 't:');
  }
}

function setAbbrvNameLocLengthLimit() {
  var abbrvNm = FormManager.getActualValue('abbrevNm');
  var abbrvLoc = FormManager.getActualValue('abbrevLocn');

  dojo.connect(FormManager.getField('abbrvNm'), 'onChange', function(value) {
    if (abbrvNm && abbrvNm.length > 22) {
      abbrvNm = abbrvNm.substring(0, 22);
      FormManager.setValue('abbrevNm', abbrvNm);
    }
  });

  dojo.connect(FormManager.getField('abbrvLoc'), 'onChange', function(value) {
    if (abbrvLoc != null && abbrvLoc.length > 12) {
      abbrvLoc = abbrvLoc.substr(0, 12);
      var subCustGrp = FormManager.getActualValue('custSubGrp');
      if (subCustGrp != 'SOFTL') {
        FormManager.setValue('abbrevLocn', abbrvLoc);
      }
    }
  });
}

function setLocNo(value) {
  FormManager.enable('locationNumber');
  FormManager.resetDropdownValues(FormManager.getField('locationNumber'));
  FormManager.setValue('locationNumber', value);
  FormManager.readOnly('locationNumber');
}

function setLocNoOnChange() {
  dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
    setLocationNumberBaseOnCntry();
  });
}

function addVatValidator() {
  console.log("addVatValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var vat = FormManager.getActualValue('vat');
        var req = FormManager.getActualValue('reqType').toUpperCase();
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var reqId = FormManager.getActualValue('reqId');
        if (reqId != null) {
          reqParam = {
            REQ_ID : reqId,
            ADDR_TYPE : "ZS01",
          };
        }
        var results = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', reqParam);
        var landedCntry = results.ret1;
        if (role == 'REQUESTER') {
          if (req == 'U') {
            if (landedCntry == 'Portugal') {
              if (vat == null || vat.length < 11) {
                FormManager.enable('vat');
                return new ValidationResult(null, false, 'VAT length is less than required.');
              } else if (vat != null && vat.length > 2) {
                var checkVatPrefix = vat.substring(0, 2);
                var checkVatLenReq = vat.substring(2, vat.length);

                if (checkVatPrefix.toUpperCase() == 'PT') {
                  if (checkVatLenReq.length < 9) {
                    FormManager.enable('vat');
                    return new ValidationResult(null, false, 'VAT length is less than required.');
                  } else if (checkVatLenReq.length == 9 && checkVatLenReq.match(/^[0-9]+$/) != null) {
                    FormManager.setValue('vat', checkVatPrefix.toUpperCase() + checkVatLenReq);
                    FormManager.readOnly('vat');
                    return new ValidationResult(null, true);
                  } else if (checkVatLenReq.length == 9 && checkVatLenReq.match(/^[0-9]+$/) == null) {
                    FormManager.enable('vat');
                    return new ValidationResult(null, false, 'VAT format is incorrect.');
                  } else {
                    FormManager.enable('vat');
                    return new ValidationResult(null, false, 'VAT length is greater than required.');
                  }
                } else {
                  FormManager.enable('vat');
                  return new ValidationResult(null, false, 'VAT prefix must start with "PT".');
                }
              } else if (vat != null && vat.length <= 2) {
                FormManager.enable('vat');
                return new ValidationResult(null, false, 'VAT length is less than required.');
              }
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addPhoneValidatorEMEA() {
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
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
/*
 * Story 1643596: Enhance CreateCMR to support single reactivation requests
 */
function lockRequireFieldsSpain() {
  var reqType = FormManager.getActualValue('reqType').toUpperCase();
  var vat = FormManager.getActualValue('vat');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (reqType == 'C' && (role == 'PROCESSOR' || role == 'REQUESTER')) {
    FormManager.readOnly('custClass');
  }

  if (reqType == 'U' && role == 'REQUESTER') {
    FormManager.readOnly('custClass');
  }

  if (reqType == 'U' && FormManager.getActualValue('ordBlk') == '93') {
    FormManager.readOnly('reqReason');
  }
  if (reqType == 'C' && role == 'REQUESTER') {
    FormManager.readOnly('specialTaxCd');
    FormManager.readOnly('legacyCurrencyCd');
    var result = cmr.query('VAT.GET_ZS01_CNTRY', {
      REQID : FormManager.getActualValue('reqId'),
      TYPE : 'ZS01'
    });
    var zs01Cntry = '';
    if (result && result.ret1 && result.ret1 != '') {
      zs01Cntry = result.ret1;
    }
    if (zs01Cntry != 'US') {
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    } else {
      FormManager.removeValidator('vat', Validators.REQUIRED);
    }

  }
  // Story 1681921: SPAIN - mailing condition & collection code fields
  if (reqType == 'C' && role == 'PROCESSOR') {
    FormManager.enable('mailingCondition');
    FormManager.enable('collectionCd');
    FormManager.enable('specialTaxCd');
    FormManager.enable('legacyCurrencyCd');
  }
  /*
   * if (role == 'REQUESTER') { FormManager.readOnly('mailingCondition'); }
   */
}
function setFieldMandatoryForProcessorSpain() {
  var reqType = FormManager.getActualValue('reqType');

  if (typeof (_pagemodel) != 'undefined') {
    if (reqType == 'C' && _pagemodel.userRole == GEOHandler.ROLE_PROCESSOR) {
      FormManager.resetValidations('enterprise');
      FormManager.resetValidations('isuCd');
      FormManager.resetValidations('clientTier');
      FormManager.resetValidations('repTeamMemberNo');
      FormManager.resetValidations('salesBusOffCd');
      checkAndAddValidator('enterprise', Validators.REQUIRED, [ 'Enterprise Number' ]);
      checkAndAddValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ]);
      checkAndAddValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ]);
      checkAndAddValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ]);
      checkAndAddValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ]);

    }

    if (reqType == 'U' && FormManager.getActualValue('ordBlk') == '93' && _pagemodel.userRole == GEOHandler.ROLE_PROCESSOR) {
      FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_CUST_TAB');
      FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_CUST_TAB');
      FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise Number' ], 'MAIN_CUST_TAB');
      FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
    }
  }
}
// Task 1662963: UI Development: Support temporary reactivation requests due to
// Embargo Code handling
function tempReactEmbargoCDOnChange() {
  if (FormManager.getActualValue('reqType') != 'U') {
    return;
  }
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId,
  };
  // Defect 1751153
  var rdcEmbargoCd = '';
  var qresult = cmr.query('GET.DATA_RDC.EMBARGO_BY_REQID', qParams);
  rdcEmbargoCd = qresult.ret1;
  if (rdcEmbargoCd != '' && rdcEmbargoCd == 'E') {
    if (_embargoCdHandler == null) {
      _embargoCdHandler = dojo.connect(FormManager.getField('embargoCd'), 'onChange', function(value) {
        var embargoCd = FormManager.getActualValue('embargoCd');
        if (_oldEmbargoCd != null && _oldEmbargoCd != '' && _oldEmbargoCd == 'E') {
          console.log("Not null old EmbargoCd ==" + _oldEmbargoCd);
          if (embargoCd == '' || embargoCd == 'undefined') {
            FormManager.setValue('reqReason', 'TREC');
            FormManager.setValue('ordBlk', '');
          } else {
            FormManager.setValue('reqReason', _oldReqReason);
            FormManager.setValue('ordBlk', _oldOrdBlk);
          }
        }
      });
    }
  }

  if (_embargoCdHandler && _embargoCdHandler[0]) {
    _embargoCdHandler[0].onChange();
  }
}

function getOldFieldValues() {
  _oldEmbargoCd = FormManager.getActualValue('embargoCd');
  _oldReqReason = FormManager.getActualValue('reqReason');
  _oldOrdBlk = FormManager.getActualValue('ordBlk');
}

function isuAndCtcBasedOnISIC() {
  if (FormManager.getActualValue('custSubGrp') == 'BUSPR' || FormManager.getActualValue('custSubGrp') == 'XBP') {
    return;
  }
  var isicCd = FormManager.getActualValue('isicCd');
  if (isicCd == '7230' || isicCd == '7240' || isicCd == '7290' || isicCd == '7210' || isicCd == '7221' || isicCd == '7229') {
    FormManager.setValue('isuCd', '32');
    FormManager.setValue('clientTier', 'N');
  }
  FormManager.disable('subIndustryCd');
}

/*
 * validate CMRNumber for Internal So Scenario
 */
function validateCMRNumberForISO() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cmrNo = FormManager.getActualValue('cmrNo');
        var _custSubGrp = FormManager.getActualValue('custSubGrp');
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
          // Validation for Internal SO Scenario
          if (_custSubGrp == 'INTSO') {
            if (!cmrNo.startsWith("997")) {
              return new ValidationResult(null, false, 'Internal SO CMR should begin with 997.');
            }
          } else if (_custSubGrp != 'INTSO') {
            if (cmrNo.startsWith("997")) {
              return new ValidationResult(null, false, 'CMR Starting with 997 is allowed for InternalSO Scenario Only.');
            }
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addMailingConditionValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var mailingCond = FormManager.getActualValue('mailingCondition');
        if (mailingCond != '' && mailingCond != '9') {
          return new ValidationResult(null, false, 'Mailing condition value can be only blank or 9.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');

function configureVATExemptOnScenariosPT(fromAddress, scenario, scenarioChanged) {
  if (FormManager.getActualValue('reqType') == 'C' && scenarioChanged) {
    if (scenario == 'SAAPA' || scenario == 'SOFTL' || scenario == 'PRICU') {
      FormManager.resetValidations('vat');
      FormManager.setValue('vatExempt', true);
    }
  }
}

function addAbbrevNmValidatorPT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevNm = FormManager.getActualValue('abbrevNm');
        var reg = /^[-_ a-zA-Z0-9]+$/;
        if (_abbrevNm != '' && (_abbrevNm.length > 0 && !_abbrevNm.match(reg))) {
          return new ValidationResult({
            id : 'abbrevNm',
            type : 'text',
            name : 'abbrevNm'
          }, false, 'The value for Abbreviated name is invalid. Only ALPHANUMERIC characters are allowed.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAbbrevLocationValidatorPT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevLocn = FormManager.getActualValue('abbrevLocn');
        var reg = /^[-_ a-zA-Z0-9]+$/;
        if (_abbrevLocn != '' && (_abbrevLocn.length > 0 && !_abbrevLocn.match(reg))) {
          return new ValidationResult({
            id : 'abbrevLocn',
            type : 'text',
            name : 'abbrevLocn'
          }, false, 'The value for Abbreviated Location is invalid. Only ALPHANUMERIC characters are allowed.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var billingCount = 0;
        var mailingCount = 0;
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && [ 'INTER', 'INTSO' ].includes(custSubGrp)) {
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (recordList == null && _allAddressData != null && _allAddressData[i] != null) {
              recordList = _allAddressData[i];
            }
            addrType = recordList.addrType;

            if (typeof (addrType) == 'object') {
              addrType = addrType[0];
              if (addrType == 'ZS01') {
                billingCount++;
              }
              if (addrType == 'ZP01') {
                mailingCount++
              }
            }
          }

          var billNm = "";
          var mailNm = "";
          var reqId = FormManager.getActualValue('reqId');
          if (billingCount > 0) {
            // get billing name from db
            var res_billNm = cmr.query('GET.CUSTNM_ADDR', {
              REQ_ID : reqId,
              ADDR_TYPE : 'ZS01'
            });
            if (res_billNm.ret1 != undefined) {
              billNm = res_billNm.ret1.trim() + ' ' + res_billNm.ret2;
            }
          }
          if (mailingCount > 0) {
            // get billing name from db
            var res_mailNm = cmr.query('GET.CUSTNM_ADDR', {
              REQ_ID : reqId,
              ADDR_TYPE : 'ZP01'
            });
            if (res_mailNm.ret1 != undefined) {
              mailNm = res_mailNm.ret1.trim() + ' ' + res_mailNm.ret2;
            }
          }

          if (!(mailNm.includes('IBM') || mailNm.toUpperCase().includes('INTERNATIONAL BUSINESS MACHINES'))
              || !(billNm.includes('IBM') || billNm.toUpperCase().includes('INTERNATIONAL BUSINESS MACHINES'))) {
            return new ValidationResult(null, false, 'Mailing / Billing  does not contain IBM in their customer name.');
          } else {
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setFieldsCharForScenarios() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (custSubGroup == 'PRICU') {
    FormManager.setValue('inacCd', '');
    FormManager.setValue('vat', '');
    FormManager.setValue('custClass', '60')
  }
  if (custSubGroup == 'INTER') {
    FormManager.setValue('inacCd', '');
    FormManager.setValue('custClass', '81')
  }
  if (custSubGroup == 'INTSO') {
    FormManager.setValue('inacCd', '');
    FormManager.setValue('custClass', '85')
  }
  if (custSubGroup == 'BUSPR') {
    FormManager.setValue('custClass', '45');
    FormManager.setValue('inacCd', '');
    FormManager.setValue('repTeamMemberNo', '1Ficti');
  }
  if (custSubGroup == 'XBP') {
    FormManager.setValue('custClass', '45');
    FormManager.setValue('inacCd', '');
    FormManager.setValue('repTeamMemberNo', '1Ficti');
  }

  if (custSubGroup == 'COMME' || custSubGroup == 'IGSGS' || custSubGroup == 'THDPT' || custSubGroup == 'THDIG' || custSubGroup == 'XCRO' || custSubGroup == 'XIGS') {
    FormManager.setValue('custClass', '11');
  }
  if (custSubGroup == 'GOVRN' || custSubGroup == 'GOVIG') {
    FormManager.setValue('custClass', '12');
  }

}

function setISUCTCOnISIC() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var isic = FormManager.getActualValue('isicCd');
  var isicList = new Set([ '7230', '7240', '7290', '7210', '7221', '7229' ]);
  if (reqType == 'C' && role == 'REQUESTER') {
    if (!(custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'PRICU' || custSubGrp == 'XINTR' || custSubGrp == 'XINSO' || custSubGrp == 'BUSPR' || custSubGrp == 'XBP' || custSubGrp == 'XCRO')) {
      if ('32' == isuCd && 'S' == clientTier && isicList.has(isic)) {
        FormManager.setValue('clientTier', 'N');
      } else if ('32' == isuCd && 'N' == clientTier && !isicList.has(isic)) {
        FormManager.setValue('clientTier', 'S');
      }
    }
  }
}

function changeAbbNmSpainOnScenario() {
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqId = FormManager.getActualValue('reqId');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var abbName = FormManager.getActualValue('abbrevNm');
  if (reqType == 'C' && role == 'REQUESTER') {
    var reqParam = {
      REQ_ID : reqId,
      ADDR_TYPE : 'ZI01'
    };
    var installingAddrName = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_MCO', reqParam);
    if ([ 'INTER', 'INTSO' ].includes(custSubGrp) && !abbName.includes('IBM/')) {
      abbName = installingAddrName.ret1 != undefined ? 'IBM/'.concat(installingAddrName.ret1) : abbName;
      if (abbName.length > 22)
        abbName = abbName.substring(0, 22);
    } else if ([ 'IGSGS', 'GOVIG', 'XIGS', 'THDIG' ].includes(custSubGrp) && installingAddrName.ret1 != undefined && !abbName.includes('IGS')) {
      abbName = installingAddrName.ret1.length >= 18 ? installingAddrName.ret1.substring(0, 18).concat(' IGS') : installingAddrName.ret1.concat(' IGS');
    }
    FormManager.setValue('abbrevNm', abbName);
  }
}
function addValidatorForCollectionCdUpdateSpain() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var reqId = FormManager.getActualValue('reqId');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        if (reqType == 'U' && role == 'REQUESTER') {
          var requestingLOB = FormManager.getActualValue('requestingLob');
          var collectionCd = FormManager.getActualValue('collectionCd');
          var result = cmr.query('GETDATARDCVALUESIT', {
            REQ_ID : reqId
          });
          var collCdOld = null;
          if (result && result.ret1 != null) {
            collCdOld = result.ret1;
          }
          if (collCdOld != null && collCdOld != collectionCd && requestingLOB != 'AR') {
            return new ValidationResult({
              id : 'requestingLob',
              name : 'Requesting LOB'
            }, false, 'Requesting LOB should be \'Accounts Receivable\' only, if Collection Code is updated.');
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

function autoSetVatExemptFrPriCust() {
  if (dijit.byId('vatExempt').get('checked')) {
    if (FormManager.getActualValue('custSubGrp') == 'PRICU') {
      FormManager.readOnly('vatExempt');
    }
    FormManager.readOnly('vat');
    FormManager.setValue('vat', '');
  } else {
    FormManager.enable('vatExempt');
    FormManager.enable('vat');
  }
}

function configureVATExemptOnScenariosPT(fromAddress, scenario, scenarioChanged) {
  if (FormManager.getActualValue('reqType') == 'C' && scenarioChanged) {
    if (scenario == 'SAAPA' || scenario == 'SOFTL' || scenario == 'PRICU') {
      FormManager.resetValidations('vat');
      FormManager.setValue('vatExempt', true);
    }
  }
}

function addAbbrevNmValidatorPT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevNm = FormManager.getActualValue('abbrevNm');
        var reg = /^[-_ a-zA-Z0-9]+$/;
        if (_abbrevNm != '' && (_abbrevNm.length > 0 && !_abbrevNm.match(reg))) {
          return new ValidationResult({
            id : 'abbrevNm',
            type : 'text',
            name : 'abbrevNm'
          }, false, 'The value for Abbreviated name is invalid. Only ALPHANUMERIC characters are allowed.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAbbrevLocationValidatorPT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevLocn = FormManager.getActualValue('abbrevLocn');
        var reg = /^[-_ a-zA-Z0-9]+$/;
        if (_abbrevLocn != '' && (_abbrevLocn.length > 0 && !_abbrevLocn.match(reg))) {
          return new ValidationResult({
            id : 'abbrevLocn',
            type : 'text',
            name : 'abbrevLocn'
          }, false, 'The value for Abbreviated Location is invalid. Only ALPHANUMERIC characters are allowed.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

/* End 1430539 */
dojo.addOnLoad(function() {
  GEOHandler.MCO = [ SysLoc.PORTUGAL, SysLoc.SPAIN ];
  console.log('adding MCO functions...');
  // GEOHandler.addAfterConfig(getOldFieldValues, [ SysLoc.SPAIN ]);
  // GEOHandler.addAddrFunction(getOldFieldValues, [ SysLoc.SPAIN ]);
  GEOHandler.addAddrFunction(addMCOLandedCountryHandler, GEOHandler.MCO);
  GEOHandler.enableCopyAddress(GEOHandler.MCO, validateMCOCopy, [ 'ZD01' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.MCO);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.MCO);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAddrFunction(addPhoneValidatorEMEA, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);

  GEOHandler.addAfterConfig(afterConfigForPT, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterConfig(afterConfigForMCO, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(addHandlersForPTES, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);

  GEOHandler.addAfterConfig(setClientTierValues, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setSalesRepValues, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setEnterpriseValues, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setFieldMandatoryForProcessorPT, [ SysLoc.PORTUGAL ]);
  // GEOHandler.addAfterConfig(setLocationNumber, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setCollectionCode, [ SysLoc.SPAIN, SysLoc.PORTUGAL ]);
  GEOHandler.addAfterConfig(setDistrictCode, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterConfig(abbrvNmProcessorMandatory, GEOHandler.MCO);
  GEOHandler.addAfterConfig(abbrvNmProcessorMandatoryOnChange, GEOHandler.MCO);
  GEOHandler.addAfterConfig(setCollectionCode, [ SysLoc.SPAIN, SysLoc.PORTUGAL ]);
  GEOHandler.addAfterConfig(disableVatIfNotEmptyPortugal, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterConfig(disableVatIfNotEmptySpain, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(forceLockScenariosPortugal, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterConfig(forceLockScenariosSpain, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterTemplateLoad(forceLockScenariosSpain, [ SysLoc.SPAIN ]);

  GEOHandler.addAfterConfig(crossborderScenariosAbbrvLoc, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(crossborderScenariosAbbrvLocOnChange, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(hideCustPhoneonSummary, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setVatValidatorPTES, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setAbbrvPortugal, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterConfig(setDPCEBObasedOnCntry, [ SysLoc.SPAIN ]);
  // GEOHandler.addAfterConfig(setDPCEBObasedOnCntryOnChange, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(disableVATforViewOnly, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setAddressDetailsForView, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setAbbrvNameLocLengthLimit, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterConfig(setLocationNumberBaseOnCntry, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setLocationNoByPostCd, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setLocNoOnChange, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(lockRequireFieldsSpain, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterTemplateLoad(lockRequireFieldsSpain, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setFieldMandatoryForProcessorSpain, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterTemplateLoad(setFieldMandatoryForProcessorSpain, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setFieldsCharForScenarios, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterTemplateLoad(setFieldsCharForScenarios, [ SysLoc.SPAIN ]);

  GEOHandler.addAfterTemplateLoad(setFieldMandatoryForProcessorPT, [ SysLoc.SPAIN, SysLoc.PORTUGAL ]);
  GEOHandler.addAfterTemplateLoad(setClientTierValues, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterTemplateLoad(setSalesRepValues, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterTemplateLoad(setEnterpriseValues, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);

  GEOHandler.addAfterTemplateLoad(setDPCEBObasedOnCntry, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterTemplateLoad(setVatValidatorPTES, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);
  GEOHandler.addAfterTemplateLoad(forceLockScenariosPortugal, [ SysLoc.PORTUGAL ]);

  GEOHandler.registerValidator(addEnterpriseValidator, [ SysLoc.PORTUGAL, SysLoc.SPAIN ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.PORTUGAL, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.PORTUGAL ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.SPAIN, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.SPAIN ], null, true);
  GEOHandler.registerValidator(addAddressFieldValidators, [ SysLoc.PORTUGAL, SysLoc.SPAIN ], null, true);
  GEOHandler.registerValidator(addAddressTypeValidator, [ SysLoc.PORTUGAL, SysLoc.SPAIN ], null, true);
  // GEOHandler.registerValidator(addVatValidator, [ SysLoc.PORTUGAL ], null,
  // true);
  GEOHandler.registerValidator(addMailingConditionValidator, [ SysLoc.SPAIN ], null, true);

  GEOHandler.addAddrFunction(changeAbbrevNmLocnSpain, [ SysLoc.SPAIN ]);
  GEOHandler.addAddrFunction(changeAbbrevNmLocnPortugal, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterTemplateLoad(changeAbbNmSpainOnScenario, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(changeAbbNmSpainOnScenario, [ SysLoc.SPAIN ]);
  GEOHandler.addAddrFunction(disableAddrFieldsPTES, [ SysLoc.PORTUGAL, SysLoc.SPAIN ]);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.MCO, GEOHandler.ROLE_PROCESSOR, true);

  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.MCO);
  // GEOHandler.addAfterConfig(tempReactEmbargoCDOnChange, [ SysLoc.SPAIN ]);

  GEOHandler.registerValidator(addEmbargoCodeValidatorSpain, [ SysLoc.SPAIN ], null, true);
  GEOHandler.registerValidator(addValidatorForCollectionCdUpdateSpain, [ SysLoc.SPAIN ], null, true);

  // PT Legacy
  GEOHandler.addAfterConfig(afterConfigPT, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterConfig(addHandlersForPT, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterConfig(setTaxCodeOnPostalCodePT, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAddrFunction(setTaxCodeOnPostalCodePT, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterTemplateLoad(afterTemplateLoadPT, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterTemplateLoad(setTaxCodeOnPostalCodePT, [ SysLoc.PORTUGAL ]);
  GEOHandler.addAfterTemplateLoad(setISUCTCOnISIC, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setISUCTCOnISIC, [ SysLoc.SPAIN ]);
  
  GEOHandler.addAfterConfig(TaxCdOnPostalChange, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterConfig(setTaxCdByPostCd, [ SysLoc.SPAIN ]);
  GEOHandler.addAfterTemplateLoad(setTaxCdByPostCd, [ SysLoc.SPAIN ]);
  GEOHandler.registerValidator(validateCMRNumberForISO, [ SysLoc.PORTUGAL ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterTemplateLoad(autoSetVatExemptFrPriCust, [ SysLoc.SPAIN ])

  GEOHandler.addAfterTemplateLoad(autoSetVatExemptFrPriCust, [ SysLoc.SPAIN ])
  GEOHandler.addAfterTemplateLoad(configureVATExemptOnScenariosPT, [ SysLoc.PORTUGAL ]);
  GEOHandler.registerValidator(addAbbrevNmValidatorPT, [ SysLoc.PORTUGAL ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(addAbbrevLocationValidatorPT, [ SysLoc.PORTUGAL ], GEOHandler.ROLE_PROCESSOR, true);

});
